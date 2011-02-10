<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

     <xsl:template match="calendar">
        <p>
            <strong><xsl:value-of select="document-title" /></strong>
        </p>
        <p>
            <xsl:choose>
                <xsl:when test="calendar-image/file-resource!=''">
                       <xsl:apply-templates select="calendar-image/file-resource" />
                </xsl:when>
                <xsl:otherwise>               
               </xsl:otherwise>        
            </xsl:choose>
        </p>   
	<div>
              <xsl:value-of disable-output-escaping="yes" select="calendar-description" /> 
	</div>
	  <p>
              <xsl:value-of disable-output-escaping="yes" select="calendar-contact" /> 
	  </p>
        <br />
        <br />
        <br />
        <br />
     </xsl:template>

 
   <xsl:template match="file-resource">
        <xsl:choose>
            <xsl:when test="(resource-content-type='image/jpeg' or resource-content-type='image/jpg' or  resource-content-type='image/pjpeg' or resource-content-type='image/gif' or resource-content-type='image/png')" >
                <img src="document?id={resource-document-id}&amp;id_attribute={resource-attribute-id}" align="right" />
            </xsl:when>
            <xsl:otherwise>
                <a href="document?id={resource-document-id}&amp;id_attribute={resource-attribute-id}"> 
                    <img src="images/admin/skin/plugins/document/filetypes/file.png" border="0" />
                </a>
            </xsl:otherwise>        
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>