/*
 * Copyright (c) 2002-2012, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.calendar.modules.document.service.search;

import fr.paris.lutece.plugins.calendar.business.Event;
import fr.paris.lutece.plugins.calendar.business.OccurrenceEvent;
import fr.paris.lutece.plugins.calendar.service.AgendaResource;
import fr.paris.lutece.plugins.calendar.service.CalendarPlugin;
import fr.paris.lutece.plugins.calendar.service.Utils;
import fr.paris.lutece.plugins.calendar.web.Constants;
import fr.paris.lutece.plugins.document.business.DocumentFilter;
import fr.paris.lutece.plugins.document.business.DocumentHome;
import fr.paris.lutece.plugins.document.business.attributes.DocumentAttribute;
import fr.paris.lutece.plugins.lucene.service.indexer.IFileIndexer;
import fr.paris.lutece.plugins.lucene.service.indexer.IFileIndexerFactory;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.search.IndexationService;
import fr.paris.lutece.portal.service.search.SearchIndexer;
import fr.paris.lutece.portal.service.search.SearchItem;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.demo.html.HTMLParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DocumentCalendarIndexer implements SearchIndexer
{
    //properties
    private static final String PROPERTY_INDEXER_NAME = "calendar-document.indexer.name";
    private static final String ENABLE_VALUE_TRUE = "1";
    private static final String PROPERTY_INDEXER_DESCRIPTION = "calendar-document.indexer.description";
    private static final String PROPERTY_INDEXER_VERSION = "calendar-document.indexer.version";
    private static final String PROPERTY_INDEXER_ENABLE = "calendar-document.indexer.enable";
    private static final String PROPERTY_DOCUMENT_CALENDAR_TYPE = "calendar-document.calendar.document.type";
    private static final String PROPERTY_DOCUMENT_SHORT_NAME = "dcld";
    private static final String CALENDAR_SHORT_NAME = "cld";
    /** uses calendar search page */
    private static final String JSP_SEARCH_CALENDAR = "jsp/site/Portal.jsp?page=calendar&action=search";
    private static IFileIndexerFactory _factoryIndexer = (IFileIndexerFactory) SpringContextService.getBean( IFileIndexerFactory.BEAN_FILE_INDEXER_FACTORY );

    /**
     * Index all documents
     *
     * @throws IOException the exception
     * @throws InterruptedException the exception
     * @throws SiteMessageException the exception
     */
    public void indexDocuments(  ) throws IOException, InterruptedException, SiteMessageException
    {
        String sRoleKey = "";

        DocumentFilter docFilter = new DocumentFilter(  );
        docFilter.setCodeDocumentType( AppPropertiesService.getProperty( PROPERTY_DOCUMENT_CALENDAR_TYPE ) );

        for ( fr.paris.lutece.plugins.document.business.Document document : DocumentHome.findByFilter( docFilter,
                I18nService.getDefaultLocale(  ) ) )
        {
            for ( AgendaResource agenda : Utils.getAgendaResourcesWithOccurrences(  ) )
            {
                sRoleKey = agenda.getRole(  );

                String strAgenda = agenda.getId(  );

                for ( Object oOccurrence : agenda.getAgenda(  ).getEvents(  ) )
                {
                    OccurrenceEvent occurrence = (OccurrenceEvent) oOccurrence;

                    if ( occurrence.getDocumentId(  ) == document.getId(  ) )
                    {
                        indexSubject( document, sRoleKey, occurrence, strAgenda );
                    }
                }
            }
        }
    }

    /**
     * Recursive method for indexing a calendar event
     *
     * @param faq the faq linked to the subject
     * @param subject the subject
     * @throws IOException I/O Exception
     * @throws InterruptedException interruptedException
     */
    public void indexSubject( fr.paris.lutece.plugins.document.business.Document document, String sRoleKey,
        OccurrenceEvent occurrence, String strAgenda )
        throws IOException, InterruptedException
    {
        String strPortalUrl = AppPathService.getPortalUrl(  );

        UrlItem urlEvent = new UrlItem( strPortalUrl );
        urlEvent.addParameter( XPageAppService.PARAM_XPAGE_APP, CalendarPlugin.PLUGIN_NAME );
        urlEvent.addParameter( Constants.PARAMETER_ACTION, Constants.ACTION_SHOW_RESULT );
        urlEvent.addParameter( Constants.PARAMETER_EVENT_ID, occurrence.getEventId(  ) );
        urlEvent.addParameter( Constants.PARAMETER_DOCUMENT_ID, document.getId(  ) );
        urlEvent.addParameter( Constants.PARAM_AGENDA, strAgenda );

        org.apache.lucene.document.Document docSubject = null;
        try
        {
        	docSubject = getDocument( document, sRoleKey, occurrence, strAgenda,
                    urlEvent.getUrl(  ) );
        }
        catch ( Exception e )
        {
        	String strMessage = "Document ID : " + document.getId(  ) + " - Agenda ID : " + strAgenda + " - Occurrence ID " + occurrence.getId(  );
        	IndexationService.error( this, e, strMessage );
        }
        if ( docSubject != null )
        {
        	IndexationService.write( docSubject );
        }
    }

    /**
     * Get the calendar document
     * @param strDocument id of the subject to index
     * @return The list of lucene documents
     * @throws IOException the exception
     * @throws InterruptedException the exception
     * @throws SiteMessageException the exception
     */
    public List<Document> getDocuments( String strDocument )
        throws IOException, InterruptedException, SiteMessageException
    {
        List<org.apache.lucene.document.Document> listDocs = new ArrayList<org.apache.lucene.document.Document>(  );
        String sRoleKey = "";
        DocumentFilter docFilter = new DocumentFilter(  );
        docFilter.setCodeDocumentType( AppPropertiesService.getProperty( PROPERTY_DOCUMENT_CALENDAR_TYPE ) );

        for ( fr.paris.lutece.plugins.document.business.Document document : DocumentHome.findByFilter( docFilter,
                I18nService.getDefaultLocale(  ) ) )
        {
            for ( AgendaResource agenda : Utils.getAgendaResourcesWithOccurrences(  ) )
            {
                sRoleKey = agenda.getRole(  );

                String strAgenda = agenda.getId(  );

                for ( Object oOccurrence : agenda.getAgenda(  ).getEvents(  ) )
                {
                    OccurrenceEvent occurrence = (OccurrenceEvent) oOccurrence;

                    if ( occurrence.getDocumentId(  ) == document.getId(  ) )
                    {
                        String strPortalUrl = AppPathService.getPortalUrl(  );

                        UrlItem urlEvent = new UrlItem( strPortalUrl );
                        urlEvent.addParameter( XPageAppService.PARAM_XPAGE_APP, CalendarPlugin.PLUGIN_NAME );
                        urlEvent.addParameter( Constants.PARAMETER_ACTION, Constants.ACTION_SHOW_RESULT );
                        urlEvent.addParameter( Constants.PARAMETER_EVENT_ID, occurrence.getEventId(  ) );
                        urlEvent.addParameter( Constants.PARAMETER_DOCUMENT_ID, document.getId(  ) );
                        urlEvent.addParameter( Constants.PARAM_AGENDA, strAgenda );

                        org.apache.lucene.document.Document doc = getDocument( document, sRoleKey, occurrence,
                                strAgenda, urlEvent.getUrl(  ) );
                        listDocs.add( doc );
                        ;
                    }
                }
            }
        }

        return listDocs;
    }

    /**
     * Returns the indexer service name
     * @return the indexer service name
     */
    public String getName(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_NAME );
    }

    /**
     * Returns the indexer service version
     * @return the indexer service version
     */
    public String getVersion(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_VERSION );
    }

    /**
     * Returns the indexer service description
     * @return the indexer service description
     */
    public String getDescription(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_DESCRIPTION );
    }

    /**
     * Tells whether the service is enable or not
     * @return true if enable, otherwise false
     */
    public boolean isEnable(  )
    {
        boolean bReturn = false;
        String strEnable = AppPropertiesService.getProperty( PROPERTY_INDEXER_ENABLE );

        if ( ( strEnable != null ) &&
                ( strEnable.equalsIgnoreCase( Boolean.TRUE.toString(  ) ) || strEnable.equals( ENABLE_VALUE_TRUE ) ) &&
                PluginService.isPluginEnable( CalendarPlugin.PLUGIN_NAME ) )
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * Builds a document which will be used by Lucene during the indexing of the pages of the site with the following
     * fields : summary, uid, url, contents, title and description.
     *
     * @param document the document to index
     * @param strUrl the url of the documents
     * @param strRole the lutece role of the page associate to the document
     * @param strPortletDocumentId the document id concatened to the id portlet with a & in the middle
     * @return the built Document
     * @throws IOException The IO Exception
     * @throws InterruptedException The InterruptedException
     */
    public static org.apache.lucene.document.Document getDocument( 
        fr.paris.lutece.plugins.document.business.Document document, String strRole, Event occurrence,
        String strAgenda, String strOccurrenceUrl ) throws IOException, InterruptedException
    {
        // make a new, empty document
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document(  );

        doc.add( new Field( Constants.FIELD_CALENDAR_ID, strAgenda + "_" + CALENDAR_SHORT_NAME, Field.Store.NO,
                Field.Index.NOT_ANALYZED ) );

        // Add the last modified date of the file a field named "modified".
        // Use a field that is indexed (i.e. searchable), but don't tokenize
        // the field into words.
        String strDate = Utils.getDate( occurrence.getDate(  ) );
        doc.add( new Field( SearchItem.FIELD_DATE, strDate, Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        // Add the url as a field named "url".  Use an UnIndexed field, so
        // that the url is just stored with the question/answer, but is not searchable.
        doc.add( new Field( SearchItem.FIELD_URL, strOccurrenceUrl, Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        // Add the uid as a field, so that index can be incrementally maintained.
        // This field is not stored with document, it is indexed, but it is not
        // tokenized prior to indexing.
        String strOccurrenceId = String.valueOf( occurrence.getId(  ) );
        doc.add( new Field( SearchItem.FIELD_UID, strOccurrenceId + "_" + PROPERTY_DOCUMENT_SHORT_NAME,
                Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        String strContentToIndex = getContentToIndex( document );
        StringReader readerPage = new StringReader( strContentToIndex );
        HTMLParser parser = new HTMLParser( readerPage );

        //the content of the article is recovered in the parser because this one
        //had replaced the encoded caracters (as &eacute;) by the corresponding special caracter (as ?)
        Reader reader = parser.getReader(  );
        int c;
        StringBuffer sb = new StringBuffer(  );

        while ( ( c = reader.read(  ) ) != -1 )
        {
            sb.append( String.valueOf( (char) c ) );
        }

        reader.close(  );

        // Add the tag-stripped contents as a Reader-valued Text field so it will
        // get tokenized and indexed.
        doc.add( new Field( SearchItem.FIELD_CONTENTS, sb.toString(  ), Field.Store.NO, Field.Index.ANALYZED ) );

        // Add the title as a separate Text field, so that it can be searched
        // separately.
        doc.add( new Field( SearchItem.FIELD_TITLE, document.getTitle(  ), Field.Store.YES, Field.Index.NO ) );

        doc.add( new Field( SearchItem.FIELD_TYPE, CalendarPlugin.PLUGIN_NAME, Field.Store.YES, Field.Index.ANALYZED ) );

        doc.add( new Field( SearchItem.FIELD_ROLE, strRole, Field.Store.YES, Field.Index.NOT_ANALYZED ) );

        // return the document
        return doc;
    }

    /**
     * Get the content from the document
     * @param document the document to index
     * @return the content
     */
    private static String getContentToIndex( fr.paris.lutece.plugins.document.business.Document document )
    {
        StringBuffer sbContentToIndex = new StringBuffer(  );
        sbContentToIndex.append( document.getTitle(  ) );

        for ( DocumentAttribute attribute : document.getAttributes(  ) )
        {
            if ( attribute.isSearchable(  ) )
            {
                if ( !attribute.isBinary(  ) )
                {
                    // Text attributes
                    sbContentToIndex.append( attribute.getTextValue(  ) );
                    sbContentToIndex.append( " " );
                }
                else
                {
                    // Binary file attribute
                    // Gets indexer depending on the ContentType (ie: "application/pdf" should use a PDF indexer)
                    IFileIndexer indexer = _factoryIndexer.getIndexer( attribute.getValueContentType(  ) );

                    if ( indexer != null )
                    {
                        try
                        {
                            ByteArrayInputStream bais = new ByteArrayInputStream( attribute.getBinaryValue(  ) );
                            sbContentToIndex.append( indexer.getContentToIndex( bais ) );
                            sbContentToIndex.append( " " );
                            bais.close(  );
                        }
                        catch ( IOException e )
                        {
                            AppLogService.error( e.getMessage(  ), e );
                        }
                    }
                }
            }
        }

        // Index Metadata
        sbContentToIndex.append( document.getXmlMetadata(  ) );

        return sbContentToIndex.toString(  );
    }

    /**
     * Defined by Calendar indexer.
     */
	public List<String> getListType()
	{
		return Collections.emptyList();
	}

	/**
     * Defined by Calendar indexer.
     */
	public String getSpecificSearchAppUrl()
	{
		return StringUtils.EMPTY;
	}
}
