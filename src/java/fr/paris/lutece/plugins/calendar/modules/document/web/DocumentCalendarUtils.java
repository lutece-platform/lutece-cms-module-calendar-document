/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import fr.paris.lutece.plugins.calendar.web.Constants;
import fr.paris.lutece.plugins.calendar.web.IAppUtils;
import fr.paris.lutece.plugins.document.business.Document;
import fr.paris.lutece.plugins.document.business.DocumentHome;
import fr.paris.lutece.plugins.document.business.DocumentType;
import fr.paris.lutece.plugins.document.business.DocumentTypeHome;
import fr.paris.lutece.portal.service.html.XmlTransformerService;

import javax.servlet.http.HttpServletRequest;


public class DocumentCalendarUtils implements IAppUtils
{
    private static final int NO_PORTLET = -1;

    public DocumentCalendarUtils(  )
    {
    }

    /**
     * Returns the html code of the Document
     * @param nDocumentId The id of a document
     * @param request The HTTP Servlet request
     * @Return the html code
     */
    public String getTemplateDocument( int nDocumentId, HttpServletRequest request )
    {
        Document document = DocumentHome.findByPrimaryKey( nDocumentId );
        String strPortletContent = Constants.EMPTY_STRING;

        try
        {
            DocumentType dcCalendar = DocumentTypeHome.findByPrimaryKey( Constants.PLUGIN_NAME );
            strPortletContent = XmlTransformerService.transformBySource( document.getXml( request, NO_PORTLET ),
                    dcCalendar.getContentServiceXsl(  ), null, null );
        }
        catch ( NullPointerException e )
        {
        }

        return strPortletContent;
    }
}
