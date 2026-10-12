/*
 * Copyright 2000-2004 The Apache Software Foundation.
 * Copyright 2010-2014 Stefan Kuhn
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


// java.util
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.customlocalization.CustomLocalization;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.security.JetspeedSecurityException;
import org.apache.jetspeed.util.template.JetspeedLink;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.torque.util.Transaction;
import org.apache.turbine.modules.Action;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.util.GenerateUniqueId;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.nmrshiftdb.CaptchaServlet;
import org.openscience.nmrshiftdb.om.DBAffiliation2LovPeer;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBLabGroupPeer;
import org.openscience.nmrshiftdb.om.DBUserHistory;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.UrlTool;

/**
    This action validates the form input from the NewAccount Screen.
    If it is valid, then it will check to make sure that the user account
    does not already exist. If it does, then it will show the NewAccount
    screen again. If it doesn't alread exist, then it will create the new
    user and set the CONFIRM_VALUE to be the users session id. This part should
    probably be re-done to get a better less hackable CONFIRM_VALUE, but this
    should work for now. If everything goes well, this action will send the user
    a confirmation email and then show the ConfirmRegistration screen.

    @author Jon S. Stevens <a href="mailto:jon@clearink.com">jon@clearink.com</a>
    @author David S. Taylor <a href="mailto:david@bluesunrise.com">david@bluesunrise.com</a>
    @author Tom Adams <a href="mailto:tom@PIsoftware.com">tom@PIsoftware.com</a>

*/
public class CreateNewUserAndConfirm extends Action
{
    
    /**
     * Static initialization of the logger for this class
     */    
    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(CreateNewUserAndConfirm.class.getName());    
    
    public void doPerform( RunData data ) throws Exception
    {
		Connection conn=null;
        try
        {

            String submit = "submit1";
            String str = (String) data.getUser().getTemp ( submit, "asdfasdf" );
            if ( str != null && str
                .equalsIgnoreCase( data.getParameters().getString(submit, "")) )
            {
                data.getUser().removeTemp(submit);
                data.setScreenTemplate( TurbineTemplate.getDefaultScreen() );
                return;
            }

            String pass1 = data.getParameters().getString("password", "");
            String pass2 = data.getParameters().getString("password_confirm", "");

            // make sure the passwords are not empty
            if ( (pass1.length() == 0 || pass2.length() == 0 )
                || ! pass1.equals ( pass2 ) )
            {
                data.setMessage(CustomLocalization.getString("CREATENEWUSERANDCONFIRM_PWNOTMATCH", data));
                data.setScreenTemplate("NewAccount");
                return;
            }

            String username = data.getParameters().getString("username", "");

            // convert case if configured
            username = JetspeedSecurity.convertUserName(username);
            pass1 = JetspeedSecurity.convertPassword(pass1);
            pass2 = JetspeedSecurity.convertPassword(pass2);

            // make sure the username exists
            if ( username.length() == 0 )
            {
                data.setMessage(CustomLocalization.getString("CREATENEWUSERANDCONFIRM_NOUSERNAME", data));
                data.setScreenTemplate("NewAccount");
                return;
            }

            String email = data.getParameters().getString("email", "");
            // make sure the email exists
            if ( email.length() == 0 )
            {
                data.setMessage(CustomLocalization.getString("CREATENEWUSERANDCONFIRM_NOEMAIL", data));
                data.setScreenTemplate("NewAccount");
                return;
            }
            
            //shk3
            //test the captcha
            if(GeneralUtils.getNmrshiftdbProperty("usecaptcha",data).equals("true")){
                String sessionToken = CaptchaServlet.getToken(data.getSession());
                String answer = data.getParameters().getString("captcha");
                if (!sessionToken.equals(answer)) {
					data.setMessage("You did not enter the code from the image correctly. Sorry for this, but we need to prevent bots from registration!");
					data.setScreenTemplate("NewAccount");
					return;            	
	            }
            }
			if (data.getParameters().getString("policy")==null||!data.getParameters().getString("policy").equals("policy"))
			{
				data.setMessage("You did not check the privacy policy field. You need to accept our (fair:-) privacy policy in order to register !");
				data.setScreenTemplate("NewAccount");
				return;
			}
			if (email.length() < 8)
			{
				data.setMessage("Your email is shorter than 8 charycters. This seems impossible.");
				data.setScreenTemplate("NewAccount");
				return;
			}
			
			String CHNAME = CustomLocalization.getString("CREATENEWUSERANDCONFIRM_DUPLICATEMSG", data);

            boolean accountExists = true;
            try
            {
                JetspeedSecurity.getUser(username);
            }
            catch(JetspeedSecurityException e)
            {
                accountExists = false;
            }

            if (!accountExists)
            {
                Date now = new Date();

                JetspeedUser user = JetspeedSecurity.getUserInstance();

                user.setUserName( username );
                user.setCreateDate(now);
                user.setLastLogin(new Date(0));
                user.setFirstName( data.getParameters().getString("firstname") );
                user.setLastName( data.getParameters().getString("lastname") );
                user.setEmail( data.getParameters().getString("email") );
				user.setCreateDate(new Date());
				user.setLastLogin(new Date());
				user.setPasswordChanged(new Date());
				
                createUser(user, data);

                // create a unique confirmation string for the new user
                String confirmValue = GenerateUniqueId.getIdentifier();

                // allow for disabling of email for configurations without a mail server
                boolean newUserNotification = JetspeedResources.getBoolean("newuser.notification.enable", false);
                boolean newUserApproval = JetspeedResources.getBoolean("newuser.approval.enable", false);
                boolean enableMail = JetspeedResources.getBoolean("newuser.confirm.enable", false);
                if (false == enableMail)
                    confirmValue = JetspeedResources.CONFIRM_VALUE;

                if (true == newUserApproval)
                    confirmValue = JetspeedResources.CONFIRM_VALUE_PENDING;

                user.setConfirmed( confirmValue );

                // Store the user object.
                data.setUser(user);

                String salt = GeneralUtils.getNextSalt();
                user.setPassword(pass1+salt);
                JetspeedSecurity.addUser(user);
                
                //shk3
                TurbineUser tuser=TurbineUserPeer.retrieveByPK(Integer.parseInt(user.getUserId()));
				tuser.setTitle(data.getParameters().getString("title"));
				tuser.setAddress(data.getParameters().getString("address"));
				tuser.setCity(data.getParameters().getString("city"));
				tuser.setState(data.getParameters().getString("state"));
				tuser.setZipCode(data.getParameters().getString("zipcode"));
				tuser.setCountry(data.getParameters().getString("country"));
				tuser.setWebPage(data.getParameters().getString("webpage"));
				if(!data.getParameters().getString("hostid").isEmpty()) {
					tuser.setHostId(data.getParameters().getString("hostid"));
					tuser.setHostIdTime(new Date());
				}else {
					tuser.setHostId(null);
					tuser.setHostIdTime(null);
				}
				tuser.setSalt(salt);
				if (data.getParameters().getString("realname")==null){
					tuser.setHallOfFame("false");
				}else{
					tuser.setHallOfFame("true");
				}
				tuser.save();
				DBUserHistory userHistory=new DBUserHistory();
				userHistory.setUserId(tuser.getUserId());
				if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true") || (new UrlTool().isLabserver() && !data.getParameters().getString("aff2").equals(""))){
					userHistory.setAffiliation2(data.getParameters().getString("aff2"));
				}else{
					userHistory.setAffiliation2(data.getParameters().getString("affiliation2"));
				}
		        if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true")){
		            userHistory.setAffiliation1(DBAffiliation2LovPeer.retrieveByName(userHistory.getAffiliation2()).getDBAffiliation1Lov().getAffiliation1());
		        }else if(new UrlTool().isLabserver() && !data.getParameters().getString("aff1").equals("")){
		            userHistory.setAffiliation1(data.getParameters().getString("aff1"));
                }else{
                    userHistory.setAffiliation1(data.getParameters().getString("affiliation1"));
                }
				userHistory.setJoinDate(tuser.getCreated());
				if(data.getParameters().getString("labgroup")!=null && !data.getParameters().getString("labgroup").equals("-1")){
				  userHistory.setLabgroupId(Integer.parseInt(data.getParameters().getString("labgroup")));
		          DBLabGroup labgroup=DBLabGroupPeer.retrieveByPK(Integer.parseInt(data.getParameters().getString("labgroup")));
		          MailMessage mailmessage = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(data), labgroup.getNmrshiftdbUser().getEmail(), GeneralUtils.getAdminEmail(data), "User wants to become member of your labgroup "+  labgroup.getLabgroupName(), "The user "+user.getFirstName()+" "+user.getLastName()+", email: "+user.getEmail()+", affiliation: "+userHistory.getAffiliation1()+"/"+userHistory.getAffiliation2()+" wants to become a member of your workgroup. Either accept her/him by on the administrator page or contact her/him directly!" + GeneralUtils.getEmailSignature(data));
		          if (mailmessage.send() == false) {
		            Log.error("Sending email for labgroup membershift failed for user " + username);
		          }
		        }else{
		        	userHistory.setLabgroupId(null);
		        }
				userHistory.save();
                JetspeedSecurity.grantRole(user.getUserName(),"contributor","Jetspeed");
				if(data.getParameters().getString("labgroup")!=null && !data.getParameters().getString("labgroup").equals("-1")){
		        	  if(data.getParameters().getString("groupmember").equals("coworker")){
		        		  JetspeedSecurity.grantRole(user.getUserName(),"labgroup_worker","Jetspeed"); 
		        	  }else{
		        		  JetspeedSecurity.grantRole(user.getUserName(),"labgroup_user","Jetspeed");
		        	  }
		        }
		        conn=Transaction.begin("default");
		        Statement stmt = conn.createStatement();
		        String sql = "select PROFILE from JETSPEED_USER_PROFILE where USER_NAME ='anon' and MEDIA_TYPE ='html' limit 1";
		        if(data.getParameters().getString("labgroup")!=null && !data.getParameters().getString("labgroup").equals("-1")){
		            sql = "select PROFILE from JETSPEED_USER_PROFILE where USER_NAME ='testuser' and MEDIA_TYPE ='html' limit 1";
		            if(data.getParameters().getString("groupmember").equals("coworker")){
		              sql = "select PROFILE from JETSPEED_USER_PROFILE where USER_NAME ='testworker' and MEDIA_TYPE ='html' limit 1";
		            }
                }
				ResultSet rs=stmt.executeQuery(sql);
		        rs.next();
		        sql="update JETSPEED_USER_PROFILE set PROFILE ='"+GeneralUtils.escape(rs.getString(1),false)+"' where MEDIA_TYPE ='html' and PAGE='default.psml' and USER_NAME='"+user.getUserName()+"'";
		        NmrshiftdbUserPeer.executeStatement(sql);
		        GeneralUtils.sendEmailToEventReceivers("New user "+user.getUserName()+" registered","A new user has registered for nmrshiftdb2. He is "+user.getFirstName()+" "+user.getLastName()+", his email is "+user.getEmail()+".", data, 0);
		        
		        //send confirmation email
                MailMessage mailmessage = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(data), user.getEmail(), GeneralUtils.getAdminEmail((RunData)null), "You succesfully registered for nmrshiftdb2", "You successfully registered as user "+user.getUserName()+" for the nmrshiftdb2 database on server "+javax.servlet.http.HttpUtils.getRequestURL(data.getRequest()).toString().substring(0,javax.servlet.http.HttpUtils.getRequestURL(data.getRequest()).toString().indexOf('/', 10))+". You should already be logged in. Thank you for your registration!" + GeneralUtils.getEmailSignature(data));
                if (mailmessage.send() == false) {
                    Log.error("Sending confirmation email for "+user.getUserName()+" failed");
                }

				//-shk3
				
                if (!enableMail && !newUserApproval)
                {
                  user.setHasLoggedIn(new Boolean (true));
                  user.setLastLogin(new Date(0));
                }
                data.setMessage(CustomLocalization.getString("CREATENEWUSERANDCONFIRM_CREATE", data));
                if (enableMail || newUserNotification || newUserApproval)
                {
                    data.setUser(JetspeedSecurity.getAnonymousUser());
                    data.getParameters().add("username", username);
                    data.getParameters().add("password", pass1);
                    if ( ! newUserApproval )
                    {
                        ActionLoader.getInstance().exec(data, "SendConfirmationEmail");
                        data.setScreenTemplate("ConfirmRegistration");
                    }
                    else
                    {
                        data.setScreenTemplate("NewUserAwaitingAcceptance");
                    }
                    // FIXME: Should notification be set when request is made, or when
                    //        user is accepted?
                    if ( newUserNotification )
                    {
                        ActionLoader.getInstance().exec(data, "SendNewUserNotificationEmail");
                    }
                    Transaction.commit(conn);
                    conn=null;
                }
                else
                {
                    bypassConfirmMail(data, username, pass1+salt);
                }

            }
            else // username exists. show the screen again.
            {
                data.setMessage(CustomLocalization.getString("CREATENEWUSERANDCONFIRM_CHOOSENEWNAME", data));
                data.setScreenTemplate("NewAccount");
                // set the username to be the CHNAME string so that it is
                // clear that this needs to be replaced
                data.getParameters().add("username", CHNAME);
            }
        }
        catch (Exception e)
        {
          logger.error("CreateNewUserAndConfirm",e);
          data.setMessage(e.toString());
          data.setStackTrace(StringUtils.stackTrace(e), e);
          data.setScreenTemplate(JetspeedResources.getString("template.error","Error"));
        }
		finally{
			Transaction.safeRollback(conn);
		}
    }

    /**
     * createUser creates a new user.
     * Subclasses can override this method - adding additional user property settings as needed.
     * The default implementation does nothing.
     *
     * @param user the new user that has been created
     * @param data the current RunData instance
     *
     * @throws Exception passed up from JetspeedSecurity
     */
    protected void createUser(JetspeedUser user, RunData data) throws Exception
    {
    }

    /**
     * bypassConfirmMail allows configurations to bypass sending the confirmation email
     * The new user is logged on and then redirected to the home page
     *
     * @param data Turbine information.
     * @param username The user's username.
     * @param password The user's password.
     */
    private void bypassConfirmMail(RunData data, String username, String password)
    {
        JetspeedUser usr = null;
        try
        {
          // Authenticate the user and get the object.
          usr = JetspeedSecurity.login( username, password );

          // bring logged on user to homepage via redirect
          JetspeedLink jslink = JetspeedLinkFactory.getInstance(data);
          data.setRedirectURI(jslink.getHomePage().toString());
          JetspeedLinkFactory.putInstance(jslink);
        }
        catch ( Exception e )
        {
            logger.error("Exception", e);
            data.setMessage(e.toString());
            data.setStackTrace(StringUtils.stackTrace(e), e);
            data.setScreenTemplate(JetspeedResources.getString("template.error","Error"));
        }
      }

}
