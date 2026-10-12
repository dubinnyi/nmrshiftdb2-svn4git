/*
 * Copyright 2000-2004 The Apache Software Foundation.
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.Cookie;

import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.TemplateLocator;
import org.apache.jetspeed.services.customlocalization.CustomLocalization;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.security.AccountExpiredException;
import org.apache.jetspeed.services.security.CredentialExpiredException;
import org.apache.jetspeed.services.security.FailedLoginException;
import org.apache.jetspeed.services.security.JetspeedSecurityException;
import org.apache.jetspeed.services.security.LoginException;
import org.apache.jetspeed.services.security.nosecurity.FakeJetspeedUser;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.modules.ActionEvent;
import org.apache.turbine.services.rundata.DefaultTurbineRunData;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.mail.SimpleEmail;
import org.apache.velocity.context.Context;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.VectorWithSessionBinding;

/**
 * This class is responsible for logging a user into the system. It is also
 * responsible for making sure that the user has been marked as confirmed.
 * If the user is not marked as confirmed, then it will show them the
 * confirmation from.
 *
 * @author <a href="mailto:mark_orciuch@ngsltd">Mark Orciuch</a>
 * @version $Id: JLoginUser.java,v 1.41 2004/08/29 22:31:49 jford Exp $ 
 */
public class JLoginUser extends ActionEvent
{

    /**
     * Static initialization of the logger for this class
     */    
    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(JLoginUser.class.getName());    
    
    /**
    * called when the password reminder button is pressed.
    * sends a user their password
    **/
    public void doReminder( RunData rundata ) throws Exception
    {
        JetspeedRunData data = (JetspeedRunData)rundata;

        try {
            String username = data.getParameters().getString("username", "");

            JetspeedUser user = null;

            try {
                user = JetspeedSecurity.getUser(username);
            } catch (JetspeedSecurityException ignored) {
            }

            if (user == null)
            {
                data.setScreenTemplate("LoginHelp");
                data.setMessage(CustomLocalization.getString("JLOGINUSER_PASSWORDREMINDER_INVALIDUSER", rundata));
                if (logger.isDebugEnabled())
                    logger.debug(CustomLocalization.getString("JLOGINUSER_PASSWORDREMINDER_INVALIDUSER", rundata));
                return;
            }

            user.setHasLoggedIn( Boolean.FALSE);
            data.setUser(user);
 
            DynamicURI url = new DynamicURI(data);

            //build body via template
            StringWriter email_body = new StringWriter();

            Context context = TurbineVelocity.getContext(data);
            context.put( "data", data );
            context.put( "user", user );
            context.put("userurl",url);
            context.put("config",new JetspeedResources());

            //determine the language to be used for the notification email
            String lang = (String)user.getPerm("language");
            String ctry = (String)user.getPerm("country");
            Locale loc = null;
            if (lang != null && ctry != null)
            {
                loc = new Locale(lang,ctry); 
            }

            String templatePath = TemplateLocator.locateEmailTemplate(data, JetspeedResources.getString("password.reminder.template"), loc);

            SimpleEmail se = new SimpleEmail();

            context.put("email",se);

            TurbineVelocity.handleRequest(context, templatePath, email_body);

            se.setMsg(email_body.toString());

            Properties props = System.getProperties();
            String mailServerMachine = JetspeedResources.getString( "mail.server" );
            props.put("mail.host", mailServerMachine );
            props.put("mail.smtp.host", mailServerMachine);

            se.send();

            data.setMessage (CustomLocalization.getString("JLOGINUSER_PASSWORDREMINDER_SENT", rundata));
            logger.info( "Password for user " + user.getUserName() + " was sent to " + user.getEmail());
            logger.info(CustomLocalization.getString("JLOGINUSER_PASSWORDREMINDER_SENT", rundata));
            data.setScreenTemplate("Login");
        } catch ( Exception e ) {
            data.setScreenTemplate("LoginHelp");
            String errorTitle = CustomLocalization.getString("JLOGINUSER_PASSWORDREMINDER_ERROR", rundata) ;
            String errorMessage = errorTitle + e.toString();

            logger.warn( errorMessage, e );
            data.setMessage ( errorMessage );
        }
    }


    public void doPerform( RunData rundata ) throws Exception
    {
        JetspeedRunData data = (JetspeedRunData)rundata;
        
        String username = data.getParameters().getString("username", "");
        String password = data.getParameters().getString("password", "");
        //shk3
        rundata.getSession().setAttribute("pwd",password);

        boolean newUserApproval = JetspeedResources.getBoolean("newuser.approval.enable", false);
        String secretkey = (String) data.getParameters().getString("secretkey", null);
        if ( secretkey != null )
        {

            // its the first logon - we are verifying the secretkey

            // handle the buttons on the ConfirmRegistration page
            String button1 = data.getParameters().getString ( "submit1", null );
            if ( button1 != null && button1.equalsIgnoreCase("Cancel") )
            {
                data.setScreenTemplate(TurbineTemplate.getDefaultScreen());
                return;
            }
            
            // check to make sure the user entered the right confirmation key
            // if not, then send them to the ConfirmRegistration screen            
            JetspeedUser user = JetspeedSecurity.getUser(username);

            if (user == null)
            {
                logger.warn("JLogin User: Unexpected condition : user is NULL");
                return;   
            }
            String confirm_value = user.getConfirmed();
            if ( ! secretkey.equals ( confirm_value ) && ! confirm_value.equals ( JetspeedResources.CONFIRM_VALUE ) )
            {
                if ( newUserApproval )
                {
                    data.setMessage(CustomLocalization.getString("JLOGINUSER_KEYNOTVALID", rundata));
                    data.setScreenTemplate("NewUserAwaitingAcceptance");
                    return;
                }
                else
                {
                  if ( user.getConfirmed().equals(JetspeedResources.CONFIRM_VALUE_REJECTED))
                  {
                    data.setMessage(CustomLocalization.getString("JLOGINUSER_KEYNOTVALID", rundata));
                    data.setScreenTemplate("NewUserRejected");
                    return;
                  }
                  else
                  {
                    data.setMessage(CustomLocalization.getString("JLOGINUSER_KEYNOTVALID", rundata));
                    data.setScreenTemplate("ConfirmRegistration");
                    return;
                  }
                }
            }
             
            user.setConfirmed( JetspeedResources.CONFIRM_VALUE );
            data.setMessage (CustomLocalization.getString("JLOGINUSER_WELCOME", rundata));
            JetspeedSecurity.saveUser(user);
        }
        
        JetspeedUser user = null;
        try
        {
            TurbineUser tuser=NmrshiftdbUserPeer.getByName(username);
            if(tuser.getSalt()==null){
                user = JetspeedSecurity.login(username, password);
                JetspeedSecurity.saveUser(user);
                tuser.setSalt(GeneralUtils.getNextSalt());
                String encrypted = JetspeedSecurity.encryptPassword(password+tuser.getSalt());
                tuser.setPasswordValue(encrypted);
                tuser.save();
            }else{
                user = JetspeedSecurity.login(username, password+tuser.getSalt());
                JetspeedSecurity.saveUser(user);
            }
        }
        catch (LoginException e)
        {            
            data.setScreenTemplate(JetspeedResources.getString(TurbineConstants.TEMPLATE_LOGIN));
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            data.setMessage(message);
            data.setUser(JetspeedSecurity.getAnonymousUser());
            data.getUser().setHasLoggedIn(new Boolean (false) );            

            if (e instanceof FailedLoginException)
            {
                if (!disableCheck(data))
                {                
                    logger.info("JLoginUser: Credential Failure on login for user: " + username);
                    data.setMessage(CustomLocalization.getString("PASSWORDFORM_FAILED_MSG", rundata));
                }
            }
            else if (e instanceof AccountExpiredException)
            {
                logger.info("JLoginUser: Account Expired for user " + username);
            } 
            else if (e instanceof CredentialExpiredException)
            {
                logger.info("JLoginUser: Credentials expired for user: " + username);
                data.setScreenTemplate(
                    JetspeedResources.getString(JetspeedResources.CHANGE_PASSWORD_TEMPLATE, "ChangePassword")
                    );
                data.setMessage(CustomLocalization.getString("PASSWORDFORM_EXPIRED_MSG", rundata));
                data.getParameters().setString("username", username);
            } 

            return;
        }
        catch (TorqueException ex){
        	//this happens if there is no user with that name
        	logger.info("JLoginUser: Credential Failure on login for user: " + username);
            data.setMessage(CustomLocalization.getString("PASSWORDFORM_FAILED_MSG", rundata));
            return;
        }
        catch (Throwable other)
        {
            data.setScreenTemplate(JetspeedResources.getString(TurbineConstants.TEMPLATE_ERROR));
            String message = other.getMessage() != null ? other.getMessage() : other.toString();
            data.setMessage(message);
            data.setStackTrace(org.apache.turbine.util.StringUtils.stackTrace(other), other);
            JetspeedUser juser = new FakeJetspeedUser(JetspeedSecurity.getAnonymousUserName(), false);
            data.setUser(juser);
            return;
        }
        if (user.getDisabled())
        {
            data.setMessage(CustomLocalization.getString("JLOGINUSER_ACCOUNT_DISABLED", rundata));
            data.setScreenTemplate(JetspeedResources.getString("logon.disabled.form"));
            data.getUser().setHasLoggedIn(new Boolean (false) );
            data.setUser(JetspeedSecurity.getAnonymousUser());            
            return;
        }

        // check for being confirmed before allowing someone to finish logging in
        if ( data.getUser().hasLoggedIn())
        {
            if  (JetspeedSecurity.isDisableAccountCheckEnabled())
            {
                // dst: this needs some refactoring. I don't believe this api is necessary
                JetspeedSecurity.resetDisableAccountCheck(data.getParameters().getString("username", ""));
            }        

            String confirmed = data.getUser().getConfirmed();
            if (confirmed == null || !confirmed.equals(JetspeedResources.CONFIRM_VALUE ))
            {
                if (confirmed != null && confirmed.equals(JetspeedResources.CONFIRM_VALUE_REJECTED))
                {
                  data.setMessage(CustomLocalization.getString("JLOGINUSER_KEYNOTVALID", rundata));
                  data.setScreenTemplate("NewUserRejected");
                  data.getUser().setHasLoggedIn(new Boolean (false) );
                  return;
                }
                else
                {
                  data.setMessage(CustomLocalization.getString("JLOGINUSER_CONFIRMFIRST", rundata));
                  data.setScreenTemplate("ConfirmRegistration");
                  data.getUser().setHasLoggedIn(new Boolean (false) );
                  return;
                }
            }

            // user has logged in successfully at this point
  
            TurbineUser loggedInUser = TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId()));
            if(loggedInUser.getStructureshistory()!=null && !loggedInUser.getStructureshistory().equals("")){
            	ArrayList<String> structureshistory = ((ArrayList)GeneralUtils.getFromXMLString(new String(loggedInUser.getStructureshistory())));
            	data.getSession().setAttribute(NmrshiftdbConstants.STRUCTURESHISTORY,structureshistory);
            	//we create the structureshistory as smiles in the session as well - this is used later to keep entries in it unique
                VectorWithSessionBinding vsmiles = null;
                if(DefaultTurbineRunData.getUserFromSession(data.getSession())==null){
                	vsmiles = new VectorWithSessionBinding(null);
                }else{
                	vsmiles = new VectorWithSessionBinding(DefaultTurbineRunData.getUserFromSession(data.getSession()).getUserName());
                }
                for(int i=0;i<structureshistory.size();i++){
                	IAtomContainer mol = AtomUtils.readMolBothVersions(structureshistory.get(i));
                    String smiles = SmilesGenerator.unique().aromatic().create(mol);
                    vsmiles.add(smiles);
                }                
                data.getSession().setAttribute(NmrshiftdbConstants.STRUCTURESHISTORY_SMILES, vsmiles);
            }
            
            boolean automaticLogonEnabled = JetspeedResources.getBoolean("automatic.logon.enable", false);
            if (automaticLogonEnabled)
            {
              //Does the user want to use this facility?
              boolean userRequestsRememberMe = data.getParameters().getBoolean("rememberme",false);
              if (userRequestsRememberMe)
              {
                //save cookies on the users machine.
                int maxage = JetspeedResources.getInt("automatic.logon.cookie.maxage",-1);
                String comment = JetspeedResources.getString("automatic.logon.cookie.comment","");
                String domain = JetspeedResources.getString("automatic.logon.cookie.domain");
                String path = JetspeedResources.getString("automatic.logon.cookie.path","/");

                if (domain == null)
                {
                  String server = data.getServerName();
                  domain = "." + server;
                }

                String loginCookieValue = null;

                if ( JetspeedResources.getString("automatic.logon.cookie.generation","everylogon").equals("everylogon") )
                {
                  loginCookieValue = ""+Math.random();
                  data.getUser().setPerm("logincookie",loginCookieValue);
                  JetspeedSecurity.saveUser( data.getJetspeedUser() );
                } 
                else 
                {
                  loginCookieValue = (String)data.getUser().getPerm("logincookie");
                  if (loginCookieValue == null || loginCookieValue.length() == 0)
                  {
                    loginCookieValue = ""+Math.random();
                    data.getUser().setPerm("logincookie",loginCookieValue);
                    JetspeedSecurity.saveUser( data.getJetspeedUser() );
                  }
                }

                Cookie userName = new Cookie("username",data.getUser().getUserName());
                Cookie loginCookie = new Cookie("logincookie",loginCookieValue);

                userName.setMaxAge(maxage);
                userName.setComment(comment);
                userName.setDomain(domain);
                userName.setPath(path);

                loginCookie.setMaxAge(maxage);
                loginCookie.setComment(comment);
                loginCookie.setDomain(domain);
                loginCookie.setPath(path);

                data.getResponse().addCookie(userName);
                data.getResponse().addCookie(loginCookie);

              }
                    
            }

        }
        else
        {
            disableCheck(data);
        }

    }

    private boolean disableCheck(JetspeedRunData data) throws Exception
    {
        boolean disabled = false;
        // disable user after a configurable number of strikes
        if  (JetspeedSecurity.isDisableAccountCheckEnabled())
        {
            disabled = JetspeedSecurity.checkDisableAccount(data.getParameters().getString("username", ""));
            
            if (disabled)
            {
                data.setMessage(CustomLocalization.getString("JLOGINUSER_ACCOUNT_DISABLED", data));
                data.setScreenTemplate(JetspeedResources.getString("logon.disabled.form"));
                data.getUser().setHasLoggedIn(new Boolean (false) );
                data.setUser(JetspeedSecurity.getAnonymousUser());                
            }
        }
        return disabled;
    }
}
