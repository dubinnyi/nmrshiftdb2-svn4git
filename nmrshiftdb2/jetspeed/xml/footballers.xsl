<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:template match="footballers">
            <xsl:for-each select="player">
                <table>
                <tr>
                <td>
                <h2>
                    <xsl:value-of select="firstname"/>
                </h2>
                </td>                    
                <td>    
                <h2>
                    <xsl:value-of select="lastname"/>                    
                </h2>                    
                     </td>                    
                    </tr>                    
                </table>                    
                <table width="70%">
                    <tr valign="top">
                        <td width="60%">
                        </td>
                        <td width="40%">
                            <table>
                                <tr>
                                    <td>
                                        <b><small>Caps:</small></b>
                                    </td>
                                    <td>
                                        <xsl:value-of select="caps"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <b><small>Goals</small></b>
                                    </td>
                                    <td>
                                        <xsl:value-of select="goals"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <b><small>Position:</small></b>
                                    </td>
                                    <td>
                                        <xsl:apply-templates select="position"/>
                                    </td>
                                    <td>
                                        <b><small>Country:</small></b>
                                    </td>                                    
                                    <td>
                                        <xsl:apply-templates select="country"/>
                                    </td>                                    
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
