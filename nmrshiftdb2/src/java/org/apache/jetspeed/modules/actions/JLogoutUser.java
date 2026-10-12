/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * Copyright 2010,2014 Stefan Kuhn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jetspeed.modules.actions;

 
// Java Core Classes

import javax.servlet.http.Cookie;

// Turbine Modules
import org.apache.turbine.modules.ActionEvent;
import org.apache.turbine.util.RunData;
import org.apache.turbine.TurbineConstants;

// Jetspeed Stuff
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.util.template.JetspeedLink;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.jetspeed.om.security.JetspeedUser;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;



/**
    This class is responsible for logging a user out of the system.
*/
public class JLogoutUser extends ActionEvent
{

    /**
     * Static initialization of the logger for this class
     */
    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(JLogoutUser.class.getName());
    
    public void doPerform( RunData data ) throws Exception
    {    
        logger.info("Entering action JLogoutUser");

        // if automatic login is enabled, then remove cookies when user logs out
        if ( JetspeedResources.getBoolean("automatic.logon.enable", false) )
        {
          // remove cookies by re-adding them with zero MaxAge, which deletes them
          Cookie userName = new Cookie("username","");
          Cookie loginCookie = new Cookie("logincookie","");

          String comment = JetspeedResources.getString("automatic.logon.cookie.comment","");
          String domain = JetspeedResources.getString("automatic.logon.cookie.domain");
          String path = JetspeedResources.getString("automatic.logon.cookie.path","/");

          if (domain == null)
          {
            String server = data.getServerName();
            domain = "." + server;
          }

          userName.setMaxAge(0);
          userName.setComment(comment);
          userName.setDomain(domain);
          userName.setPath(path);

          loginCookie.setMaxAge(0);
          loginCookie.setComment(comment);
          loginCookie.setDomain(domain);
          loginCookie.setPath(path);

          data.getResponse().addCookie(userName);
          data.getResponse().addCookie(loginCookie);


          // also need to remove the cookies from the current request - otherwise the session validator will log user in again
          if ( data.getRequest().getCookies() != null)
          {
            data.getCookies().remove("logincookie");
            data.getCookies().remove("username");
          }
        }        

        // use the standard turbine logout facility
        if ( JetspeedResources.getBoolean("automatic.logout.save", false) )
        {
            JetspeedSecurity.saveUser((JetspeedUser)data.getUserFromSession());
        }

        JetspeedSecurity.logout();
        //shk3
        data.getSession().removeAttribute(NmrshiftdbConstants.SEARCHHISTORY);
        data.getSession().removeAttribute("results");
        data.getSession().removeAttribute("searches");
        data.getSession().removeAttribute("moltxt");
        data.getSession().removeAttribute(NmrshiftdbConstants.STRUCTURESHISTORY);
        data.getSession().removeAttribute(NmrshiftdbConstants.STRUCTURESHISTORY_SMILES);

        data.setMessage(JetspeedResources.getString(
            TurbineConstants.LOGOUT_MESSAGE));

        JetspeedLink jsLink = null;

        data.setScreen(JetspeedResources.getString(
            TurbineConstants.SCREEN_HOMEPAGE));

        try
        {
            jsLink = JetspeedLinkFactory.getInstance(data);
        } catch (Exception e)
        {
            logger.error("Error getting jsLink", e);
        }
        data.setRedirectURI(jsLink.getHomePage().toString());
        JetspeedLinkFactory.putInstance(jsLink);
        jsLink = null;
    }

}