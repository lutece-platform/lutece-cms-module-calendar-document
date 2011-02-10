<jsp:include page="../../../../insert/InsertServiceHeader.jsp" />

<jsp:useBean id="CalendarDocInsertService" scope="session" class="fr.paris.lutece.plugins.calendar.modules.document.web.CalendarDocInsertServiceJspBean" />

<%= CalendarDocInsertService.getSelectDocument( request ) %>
