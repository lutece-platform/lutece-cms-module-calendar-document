/*
 * Copyright (c) 2002-2011, Mairie de Paris
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
package fr.paris.lutece.plugins.calendar.modules.document.web;

import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.DocumentFilter;
import fr.paris.lutece.plugins.document.business.DocumentHome;
import fr.paris.lutece.plugins.document.business.portlet.DocumentListPortletHome;
import fr.paris.lutece.plugins.document.service.publishing.PublishingService;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.page.PageHome;
import fr.paris.lutece.portal.business.portlet.Portlet;
import fr.paris.lutece.portal.business.portlet.PortletHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the user interface to insert an id of a document
 *
 */
public class CalendarDocInsertServiceJspBean
{
    private static final long serialVersionUID = 2694692453596836769L;

    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String REGEX_ID = "^[\\d]+$";

    // Templates
    private static final String TEMPLATE_INSERT_INTO_ELEMENT = "admin/plugins/calendar/modules/document/admin/insert/insert_into_element.html";
    private static final String TEMPLATE_SELECTOR_DOCUMENT = "admin/plugins/calendar/modules/document/admin/insert/document_selector.html";

    // Parameters
    private static final String PARAMETER_DOCUMENT_ID = "document_id";
    private static final String PARAMETER_INPUT = "input";

    // Marker
    private static final String MARK_DOCUMENTS_LIST = "documents_list";
    private static final String MARK_INPUT = "input";
    private static final String MARK_INSERT = "insert";
    private static final String PROPERTY_DOCUMENT_CALENDAR_TYPE = "calendar-document.calendar.document.type";

    // private
    private AdminUser _user;
    private String _input;

    /**
     * Initialize data
     *
     * @param request The HTTP request
     */
    public void init( HttpServletRequest request )
    {
        _user = AdminUserService.getAdminUser( request );
        _input = request.getParameter( PARAMETER_INPUT );
    }

    /**
     * Entry point of the insert service
     * Return the html form for document selection.
     *
     * @param request The HTTP request
     * @return The html form of the document selection page
     */
    public String getSelectDocument( HttpServletRequest request )
    {
        init( request );

        DocumentFilter docFilter = new DocumentFilter(  );
        docFilter.setCodeDocumentType( AppPropertiesService.getProperty( PROPERTY_DOCUMENT_CALENDAR_TYPE ) );

        Collection<Document> listDocuments = DocumentHome.findByFilter( docFilter, I18nService.getDefaultLocale(  ) );

        HashMap model = getDefaultModel(  );
        model.put( MARK_INPUT, _input );
        model.put( MARK_DOCUMENTS_LIST, listDocuments );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SELECTOR_DOCUMENT, _user.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
    * Insert the specified url into HTML content
    *
    * @param request The HTTP request
    * @return The url
    */
    public String doInsertUrl( HttpServletRequest request )
    {
        init( request );

        String strDocumentId = request.getParameter( PARAMETER_DOCUMENT_ID );

        if ( ( strDocumentId == null ) || !strDocumentId.matches( REGEX_ID ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        String strInsert = request.getParameter( PARAMETER_DOCUMENT_ID );

        Map<String, String> model = new HashMap<String, String>(  );
        model.put( MARK_INPUT, _input );
        model.put( MARK_INSERT, strInsert );

        HtmlTemplate template;

        template = AppTemplateService.getTemplate( TEMPLATE_INSERT_INTO_ELEMENT, request.getLocale(  ), model );

        return template.getHtml(  );
    }

    /**
     * Get the default model for selection templates
     *
     * @return The default model
     */
    private HashMap getDefaultModel(  )
    {
        HashMap model = new HashMap(  );
        model.put( MARK_INPUT, _input );

        return model;
    }
}
