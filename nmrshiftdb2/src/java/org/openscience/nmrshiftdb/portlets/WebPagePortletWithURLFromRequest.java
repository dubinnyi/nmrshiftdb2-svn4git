/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (c) 2010-2015 Stefan Kuhn
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *     "Apache Jetspeed" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" or
 *    "Apache Jetspeed", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This class was modified by Stefan Kuhn in order to accept a URL to diplay as Parameter (URL=)
 * Certain URLs are also rewritten to display inside of nmrshiftdb2
 */

package org.openscience.nmrshiftdb.portlets;

import java.io.File;
//Element Construction Set
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;

import org.apache.ecs.ClearElement;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.jetspeed.portal.PortletConfig;
import org.apache.jetspeed.portal.PortletException;
import org.apache.jetspeed.portal.portlets.AbstractPortlet;
import org.apache.jetspeed.util.HTMLRewriter;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;


/**
 * A class that loads a web page and filters it to have certain features
 * deleted. This class was modified by Stefan Kuhn in order to accept a URL to diplay as Parameter (URL=)
 * Certain URLs are also rewritten to display inside of nmrshiftdb2.
 *
 *@author <a href="mailto:rammer@sycom.at">Ingo Rammer</a>
 *@author <a href="mailto:sgala@apache.org">Santiago Gala</a>
 *@author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 */
@SuppressWarnings("serial")
public class WebPagePortletWithURLFromRequest extends AbstractPortlet {

    HTMLRewriter rewriter = null;
    boolean pageCached = false;

    public void init() throws PortletException {
  
        if (pageCached) // Why is init called more than once per portlet?
            return;

        PortletConfig config = this.getPortletConfig();
        
        try 
        {
            //FIXME: HTMLRewriter should take a Reader, and work
            rewriter = new HTMLRewriter(
                    ! config.getInitParameter("dont_remove_script","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_style","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_noscript","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_meta","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_applet","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_object","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_head","no")
                        .equalsIgnoreCase("yes"),
                    ! config.getInitParameter("dont_remove_onsomething","no")
                        .equalsIgnoreCase("yes")
                 );

        } catch (Exception e) {
            Log.info("Exception occurred:" + e.toString());
            e.printStackTrace();
            throw new PortletException( e.toString() );
        }
    }
    
    /**
     * took this from FileServerPortlet as it was private 
     *
    */
    // FIXME: Currently only the expiration the HTTP Reponse header is honored. 
    //        Expiration information in <meta> tags are not honored 
    private Reader getReader(String url) throws IOException 
    {
        URL            pageUrl = new URL(url);
        URLConnection  pageConn = pageUrl.openConnection();
        long           pageExpiration = pageConn.getExpiration();
        String         encoding = pageConn.getContentEncoding();
        if(encoding == null)
        {
            // Standard HTTP encoding
            encoding = "iso-8859-1";
        }
        Reader rdr = new InputStreamReader(pageConn.getInputStream(),
                                           encoding );
        // Only set the page expiration it the page has not expired
        if (pageExpiration > System.currentTimeMillis())
        {
            Log.debug( "WebPagePortlet caching URL: " + 
                       url + 
                       " Expiration: " + 
                       pageExpiration +
                       ", " +
                       (pageExpiration - System.currentTimeMillis() ) +
                       " milliseconds into the future" );
            setExpirationMillis(pageExpiration);
        }

        return rdr;
    }

    /**
  
    public boolean isCacheable() {
        return getExpirationMillis() != null;
    }
  */

    /**
    This methods outputs the content of the portlet for a given 
    request.

    @param data the RunData object for the request
    @return the content to be displayed to the user-agent
    */
    public ConcreteElement getContent( RunData data ) 
    {
        PortletConfig config = this.getPortletConfig();

        try
        {
            if (pageCached)
            {
                if (null == getExpirationMillis())
                    return getContent( data, null, true);

                if (getExpirationMillis().longValue() <= System.currentTimeMillis())
                    return getWebPageContent(data, config);
    
                return getContent( data, null , true );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // cache page first time
        return getWebPageContent(data, config);
    }

    public ConcreteElement getWebPageContent( RunData data, PortletConfig config )
    {    
        
        String convertedString = null;  // parsed and re-written HTML
        ClearElement element = null;
        File cacheDir=new File(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/cache"));
        String url=data.getRequest().getParameter("URL");
        if(url==null)
            url=data.getRequest().getParameter("url");
        //if no url given we show the index page
        if(url==null)
          url= "t1.html";
        //only local or nmrshiftdb2.sf.net urls are valid for security reasons
        if(url.indexOf("//")>-1 && url.indexOf("//nmrshiftdb2.sourceforge.io")==-1)
        	return new StringElement("Illegal URL");
        String urlfilename=new Base64().encodeToString(url.getBytes());
        if(new File(cacheDir.getAbsolutePath()+System.getProperty("file.separator")+urlfilename).exists()) {
	        try 
	        {
	        	convertedString=Files.readString(new File(cacheDir.getAbsolutePath()+System.getProperty("file.separator")+urlfilename).toPath());
	        } catch (Exception e) {
	            Log.info("Exception occurred:" + e.toString());
	            e.printStackTrace();
	        }        
        }else {
	        try 
	        {
	            String weburl=data.getRequest().getRequestURL().toString();
	            if(url.indexOf("http://")==-1 && url.indexOf("https://")==-1){
	              if(weburl.indexOf("portal")!=-1){
	                url=weburl.substring(0,weburl.indexOf("portal"))+"/nmrshiftdbhtml/"+url;
	              }else{
	            	  //this is in case nmrshiftdb runs as an application called nmrshiftdb in tomcat
	            	  if(weburl.indexOf("/nmrshiftdb", weburl.indexOf("/nmrshiftdb")+5)>-1)
	            		  url=weburl.substring(0,weburl.indexOf("/nmrshiftdb", weburl.indexOf("/nmrshiftdb")+5))+"/nmrshiftdbhtml/"+url;
	            	  else
	            		  url=weburl.substring(0,weburl.indexOf("/nmrshiftdb"))+"/nmrshiftdbhtml/"+url;
	              }
	            }
	            //if(url.indexOf("https")!=-1)
	            //  url="http"+url.substring(5);
	            Reader htmlReader = getReader( url );
	            //FIXME: HTMLRewriter should take a Reader, and work
	            convertedString = rewriter.convertURLs(htmlReader, weburl);
	            //we replace urls to pages in nmrshiftdb with internal references
	            convertedString = GeneralUtils.replace (convertedString, "t1", "portal/js_pane/P-Help;jsessionid="+data.getSession().getId()+"?URL=t1");
	            convertedString = GeneralUtils.replace (convertedString, "using.htm", "portal/js_pane/P-Help;jsessionid="+data.getSession().getId()+"?URL=using.htm");
	            convertedString = GeneralUtils.replace (convertedString, "technology.htm", "portal/js_pane/P-Help;jsessionid="+data.getSession().getId()+"?URL=technology.htm");
	            convertedString = GeneralUtils.replace (convertedString, "credits.htm", "portal/js_pane/P-Help;jsessionid="+data.getSession().getId()+"?URL=credits.htm");
	            if(url.indexOf("using.html")>-1){
	            	convertedString = GeneralUtils.replace (convertedString, "src/html/", "nmrshiftdbhtml/");
	            }
	            convertedString = GeneralUtils.replace (convertedString, "/portal/js_pane/images", "/images");
	            convertedString = GeneralUtils.replace (convertedString, "/portal/js_pane/nmrshiftdbhtml", "/nmrshiftdbhtml");
	            convertedString = GeneralUtils.replace (convertedString, "/portal/js_pane/portal/js_pane/P-", "/portal/js_pane/P-");
	            convertedString = convertedString.replaceAll("/portal/media-type/html/user/.*/page/default.psml/images/","/images/");
	            convertedString = GeneralUtils.replace (convertedString, "/portal/images/", "/images/");
	            convertedString = GeneralUtils.replace (convertedString, "minimum.mark.display", GeneralUtils.getNmrshiftdbProperty("minimum.mark.display"));
	            convertedString = GeneralUtils.replace (convertedString, "minimum.mark.prediction", GeneralUtils.getNmrshiftdbProperty("minimum.mark.prediction"));
	            convertedString = GeneralUtils.replace (convertedString, "$serverproviderlink", GeneralUtils.getNmrshiftdbProperty("serverproviderlink"));
	            convertedString = GeneralUtils.replace (convertedString, "$serverproviderlogo", GeneralUtils.getNmrshiftdbProperty("serverproviderlogo"));
	            convertedString = GeneralUtils.replace (convertedString, "$serverprovideralttext", GeneralUtils.getNmrshiftdbProperty("serverprovideralttext"));
	            convertedString = GeneralUtils.replace (convertedString, "$contributorslist", NmrshiftdbUserPeer.getUserTableHtml());
	            convertedString = GeneralUtils.replace (convertedString, "$sessionid", data.getSession().getId());
	            if(url.indexOf("technology.html")==-1)
	              convertedString = GeneralUtils.replace (convertedString, "administrator", "<a href=\"mailto:"+GeneralUtils.getAdminEmail(data)+"\">administrator</a>");
	            pageCached = false;
	            File cachefile=new File(cacheDir.getAbsolutePath()+System.getProperty("file.separator")+urlfilename);
	            Files.write(cachefile.toPath(), convertedString.getBytes());
	            //FIXME: We should do a clearContent() for the media type, not ALL media types
	            this.clearContent();  // doing this because setContent() is not overwriting current content.
	            this.setContent(element);
	
	            htmlReader.close();
	
	        } catch (Exception e) {
	            Log.info("Exception occurred:" + e.toString());
	            e.printStackTrace();
	        }        
        }
        element = new ClearElement(convertedString);
        return element;
    }

}
