<?xml version="1.0" encoding="iso-8859-1"?>
<!--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!--
Author:  Kevin A Burton (burton@apache.org)
Author:  Santiago Gala (sgala@hisitech.com)
Author:  Raphaël Luta (raphael@apache.org)

    SGP: Changed to support quoting of $ as $$ to avoid problems under WML

$Id: rss-wml.xsl,v 1.6 2004/03/17 19:24:10 jford Exp $
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:downlevel="http://my.netscape.com/rdf/simple/0.9/"
                exclude-result-prefixes="downlevel rdf"
                version="1.0">

  <xsl:output indent="yes" 
              method="xml"
             omit-xml-declaration="yes"/>

  <xsl:param name="itemdisplayed" select="number(5)"/>
  <xsl:param name="showdescription" select="'false'"/>
  <xsl:param name="showtitle" select="'false'"/>
    
  <xsl:template match="/rss">
    <xsl:apply-templates select="channel"/>
  </xsl:template>

  <xsl:template match="/rdf:RDF">
    <xsl:apply-templates select="downlevel:channel"/>
  </xsl:template>
    
  <xsl:template match="channel">
      <xsl:variable name="description" select="description"/>    
      <card id="channel">
      <p><xsl:apply-templates select="title"/>
      <xsl:if test="$showtitle = 'true' and $description">
        <br/><xsl:apply-templates select="$description" />
      </xsl:if></p>
      <xsl:apply-templates select="item[$itemdisplayed&gt;=position()]"/>
      </card>
  </xsl:template>

  <xsl:template match="item">
      <xsl:variable name="description" select="description"/>    
    <p><a href="{link}"><xsl:apply-templates select="title"/></a>
    <xsl:if test="$showdescription = 'true' and $description">
      <br/><xsl:apply-templates select="$description"/>
    </xsl:if></p>
  </xsl:template>
    
  <xsl:template match="downlevel:channel">
    <xsl:variable name="description" select="downlevel:description"/>
    <card id="channel">
    <p><xsl:apply-templates select="downlevel:title"/>
    <xsl:if test="$showtitle = 'true' and $description">
      <br/><xsl:apply-templates select="$description"/>
    </xsl:if></p>
    <xsl:apply-templates select="../downlevel:item[$itemdisplayed&gt;=position()]"/>
    </card>
  </xsl:template>

  <xsl:template match="downlevel:item">
    <xsl:variable name="description" select="downlevel:description"/>
    <p><a href="{downlevel:link}"><xsl:apply-templates select="downlevel:title"/></a>
    <xsl:if test="$showdescription = 'true' and $description">
      <br/><xsl:apply-templates select="$description"/>
    </xsl:if></p>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:call-template name="dollar-cleaner">
      <xsl:with-param name="chars">
        <xsl:value-of select="."/>            
        </xsl:with-param>
      </xsl:call-template>
  </xsl:template>

  <xsl:template name="dollar-cleaner">
    <xsl:param name="chars"></xsl:param>
    <xsl:choose>
    <xsl:when test="contains($chars,'$') and 
                    not(starts-with(substring-after($chars,'$'), '$'))" >
      <xsl:value-of select="substring-before($chars,'$')"
            />$$<xsl:call-template
                  name="dollar-cleaner">
                  <xsl:with-param name="chars">
                    <xsl:value-of select="substring-after($chars,'$')" />
                  </xsl:with-param>
                </xsl:call-template>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$chars" /></xsl:otherwise>
    </xsl:choose> 
  </xsl:template>
    
</xsl:stylesheet>
