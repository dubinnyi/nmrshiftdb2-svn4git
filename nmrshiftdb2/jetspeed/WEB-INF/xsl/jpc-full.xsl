<?xml version="1.0"?> 
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
<xsl:stylesheet version="1.0"
                xmlns:jcm="http://jakarta.apache.org/jetspeed/xml/jetspeed-portal-content"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/jcm:content">

        <table border="0" cellspacing="0" cellpadding="0">


            <!--
            <xsl:call-template name="newest-topics"/>
            -->
            <xsl:apply-templates select="/jcm:content/jcm:channel/jcm:item"/>
        </table>
    </xsl:template>

    <xsl:template match="/jcm:content/jcm:channel/jcm:item">

  <xsl:if test="not(position() = 1)">
    <tr width="100%">
      <td width="100%" colspan="2">
        <hr noshade="noshade"><!--breaker--></hr>
      </td>
    </tr>
  </xsl:if>
  
        <tr width="100%">
        <!--
        BEGIN Add the topic icon
        -->

        
        <td  align="left" valign="top"> 
        <xsl:call-template name="topics">
            <xsl:with-param name="topic"><xsl:value-of select="jcm:topic"/></xsl:with-param>
        </xsl:call-template>
     <!--   </td> -->
        <!--
        END Add the topic icon
        -->

     <!--   <td width="100%" align="left" valign="top"> -->
        

        <a href="{jcm:link}">
        <b>
        <xsl:value-of select="jcm:title"/>
        </b>
        </a>
        

        <!--
        Add the quote if any...
        -->

        <xsl:apply-templates select="jcm:quote"/>
        

        <p align="left" clear="left">    
        <xsl:value-of select="jcm:description"/>
        </p>
        </td>
        </tr>

    </xsl:template>

    <xsl:template match="jcm:quote">

        <p align="left">    
        from: 
        <a href="{jcm:link}" target="_new">
        <xsl:value-of select="jcm:author"/>        
        </a>
        </p>
    
        <xsl:apply-templates select="jcm:p"/>

    </xsl:template>

    <xsl:template match="jcm:p">
      <p>
          <i>
              <xsl:value-of select="."/>
          </i>
      </p>
    </xsl:template>
    
    
    <xsl:template name="topics">
        <xsl:param name="topic"/>
        <xsl:variable name="link"          select="/jcm:content/jcm:channel/jcm:topics/jcm:entry[@name=$topic]/jcm:image/jcm:link"/>
        <xsl:variable name="url"           select="/jcm:content/jcm:channel/jcm:topics/jcm:entry[@name=$topic]/jcm:image/jcm:url"/>
        <xsl:variable name="title"         select="/jcm:content/jcm:channel/jcm:topics/jcm:entry[@name=$topic]/jcm:image/jcm:title"/>
        <a href="{$link}">
        <img src="{$url}" border="0" alt="{$title}" align="left" />
        </a>
    </xsl:template>


    <!--
    Get an index of the most recent topics
    -->
    <xsl:template name="newest-topics">

        <tr width="100%">
        <td colspan="2">
        <table>
        <tr width="100%" align="right">
        <td width="100%"><!-- align --> </td>
        
        <xsl:call-template name="get-entry-topic">
            <xsl:with-param name="itemId">0</xsl:with-param>
        </xsl:call-template>
    
        <xsl:call-template name="get-entry-topic">
            <xsl:with-param name="itemId">2</xsl:with-param>
        </xsl:call-template>


        </tr>
        </table>
        </td>
        </tr>
    </xsl:template>
    
    <!--
    Given an id... get the image entry for a specific topic.
    -->
    <xsl:template name="get-entry-topic">
        <xsl:param name="itemId"/>
        <!-- first get the topic name of the iten you requested -->
        
        <xsl:variable name="topic" select="/jcm:content/jcm:channel/jcm:item[$itemId]/jcm:topic"/>

        <td>

        <xsl:call-template name="topics">
            <xsl:with-param name="topic"><xsl:value-of select="$topic"/></xsl:with-param>
        </xsl:call-template>

        </td>
        

    </xsl:template>
    
</xsl:stylesheet>

