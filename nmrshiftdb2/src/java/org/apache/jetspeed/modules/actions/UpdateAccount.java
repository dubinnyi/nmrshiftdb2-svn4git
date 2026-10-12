/*
 * Copyright 2000-2004 The Apache Software Foundation.
 * Copyright 2010,2011,2014 Stefan Kuhn
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


// Java
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import javax.servlet.http.Cookie;

import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.customlocalization.CustomLocalization;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.security.JetspeedSecurityException;
import org.apache.turbine.modules.Action;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.util.GenerateUniqueId;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.om.DBAffiliation2LovPeer;
import org.openscience.nmrshiftdb.om.DBUserHistory;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.UrlTool;

/**
 *
 *   Updates an Account in the User and save the User object to backing store.
 *   You must have been logged in in order
 *   to update the account.
 */
public class UpdateAccount extends Action
{
    public void doPerform( RunData rundata ) throws Exception
    {
        JetspeedRunData data = (JetspeedRunData)rundata;

        // check to make sure the user has logged in before accessing this screen
        if ( ! data.getUser().hasLoggedIn() )
        {
            data.setScreenTemplate( JetspeedResources.getString( "services.JspService.screen.error.NotLoggedIn", "Error" ) );
            return;
        }

        String cancelBtn = data.getParameters().getString( "CancelBtn" , "" );
        String username  = data.getParameters().getString( "username" , "" );
        String oldPassword  = JetspeedSecurity.convertPassword(data.getParameters().getString( "old_password" , "" ));
        String password  = JetspeedSecurity.convertPassword(data.getParameters().getString( "password" , "" ));
        String password2 = JetspeedSecurity.convertPassword(data.getParameters().getString( "password_confirm" , "" ));
        String firstname = data.getParameters().getString( "firstname", "" );
        String lastname  = data.getParameters().getString( "lastname" , "" );
        String email     = data.getParameters().getString( "email" , "" );
		String title = data.getParameters().getString("title", "");
		String address = data.getParameters().getString("address", "");
		String city = data.getParameters().getString("city", "");
		String state = data.getParameters().getString("state", "");
		String zipcode = data.getParameters().getString("zipcode", "");
		String country = data.getParameters().getString("country", "");
		String webpage = data.getParameters().getString("webpage", "");
		String hostid = data.getParameters().getString("hostid", "");
		String affiliation1 = data.getParameters().getString("affiliation1", "");
		String affiliation2 = data.getParameters().getString("affiliation2", "");
        boolean userRequestsRememberMe = data.getParameters().getBoolean( "rememberme" , false );

        // Save user input in case there is an error and 
        // we have to go back to the EditAccount screen
        Hashtable screenData = new Hashtable();
        screenData.put( "username",  username );
        screenData.put( "firstname", firstname );
        screenData.put( "lastname",  lastname );
        screenData.put( "email",     email );
		screenData.put("title", title);
		screenData.put("address", address);
		screenData.put("city", city);
		screenData.put("state", state);
		screenData.put("zipcode", zipcode);
		screenData.put("country", country);
		screenData.put("webpage", webpage);
		screenData.put("hostid", hostid);
		screenData.put("affiliation1", affiliation1);
		screenData.put("affiliation2", affiliation2);
        data.getRequest().setAttribute( "ScreenDataEditAccount", screenData );

        // CANCEL BUTTON
        //
        // check to see if the Cancel button was pressed.
        // if so, return to the screen we were previously on
        // defined by nextscreen in the EditAccount screen
        if ( ! cancelBtn.equalsIgnoreCase( "" ) )            
        {
            return;
        }

        // PASSWORD
        //
        // if the fields are empty, then don't do anything to the passwords
        boolean changepass = false;
        if ( password.trim().length() > 0 && password2.trim().length() > 0 )
        {
            changepass = true;
        }
        
        if ( changepass == true && ! password.equals( password2 ) )
        {
            data.setMessage(CustomLocalization.getString("UPDATEACCOUNT_PWNOTMATCH", rundata));
            backToEditAccount( data, screenData );
            return;
        }
        
        if ( changepass == true && password.equals( oldPassword ) )
        {
            // old password = new passwod, so do not change.
            changepass = false;
        }

            // FIRSTNAME
        //
        // make sure the firstname exists
        if ( firstname.length() == 0 )
        {
            data.setMessage(CustomLocalization.getString("UPDATEACCOUNT_NOFIRSTNAME", rundata));
            backToEditAccount( data, screenData );
            return;
        }

        // LASTNAME
        //
        // make sure the lastname exists
        if ( lastname.length() == 0 )
        {
            data.setMessage(CustomLocalization.getString("UPDATEACCOUNT_NOLASTNAME", rundata));
            backToEditAccount( data, screenData );
            return;
        }

		// AUTOMATIC LOGIN
        //
        // if automatic login is enabled, then handle the remember me checkbox
        if ( JetspeedResources.getBoolean("automatic.logon.enable", false) )
        {
          if ( ! userRequestsRememberMe ) 
          {
            if ( data.getRequest().getCookies() != null && data.getRequest().getCookies().length>0 &&
                 data.getCookies().getString("username") != null &&
                 data.getCookies().getString("logincookie") != null )
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

              data.getCookies().remove("username");
              data.getCookies().remove("logincookie");
            }
          } 
          else 
          {
            if ( data.getRequest().getCookies() == null || data.getRequest().getCookies().length==0 || 
                 !data.getCookies().getString("username","").equals(data.getUser().getUserName()) ||
                 !data.getCookies().getString("logincookie","").equals(data.getUser().getPerm("logincookie")) )
            {
              String loginCookieValue = (String)data.getUser().getPerm("logincookie");
              if (loginCookieValue == null || loginCookieValue.length() == 0)
              {
                loginCookieValue = ""+Math.random();
                data.getUser().setPerm("logincookie",loginCookieValue);
                JetspeedSecurity.saveUser( data.getJetspeedUser() );
              }

              Cookie userName = new Cookie("username",data.getUser().getUserName());
              Cookie loginCookie = new Cookie("logincookie",loginCookieValue);

              int maxage = JetspeedResources.getInt("automatic.logon.cookie.maxage",-1);
              String comment = JetspeedResources.getString("automatic.logon.cookie.comment","");
              String domain = JetspeedResources.getString("automatic.logon.cookie.domain");
              String path = JetspeedResources.getString("automatic.logon.cookie.path","/");

              if (domain == null)
              {
                String server = data.getServerName();
                domain = "." + server;
              }

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

              data.getCookies().add("username",data.getUser().getUserName());
              data.getCookies().add("logincookie",loginCookieValue);
            }
          }
        }

		// EMAIL
		//
		//shk3: I replaced the Localization.getString("") with direct strings since 
		//Localization.getString("") gives an error
		if (email.length() < 8)
		{
			data.setMessage("You must give an email!");
			backToEditAccount(data, screenData);
			return;
		}

        boolean enableMail = JetspeedResources.getBoolean("newuser.confirm.enable", false);

        String currentEmail = (String) data.getUser().getEmail();
        if ( enableMail && ( currentEmail == null || ! currentEmail.equalsIgnoreCase(email) ) )
            {
                //Send confirmation email if different than current
                data.getUser().setEmail( email );
                data.getUser().setConfirmed( GenerateUniqueId.getIdentifier() );
                JetspeedSecurity.saveUser( data.getJetspeedUser() );
                ActionLoader.getInstance().exec(data, "SendConfirmationEmail");
                // add in the username to the parameters because ConfirmRegistration needs it
                data.getParameters().add("username", data.getUser().getUserName() );
                data.setMessage(CustomLocalization.getString("UPDATEACCOUNT_NEWEMAILCONFIRM", rundata));
                data.setScreenTemplate("ConfirmRegistration");
            }
        else
            {       
                JetspeedSecurity.saveUser( data.getJetspeedUser() );
            }
            
        // update currently logged in information that might have changed
        data.getUser().setFirstName( firstname );
        data.getUser().setLastName( lastname );
        data.getUser().setEmail( email );
        JetspeedSecurity.saveUser( data.getJetspeedUser() );
        
        
        TurbineUser tuser=TurbineUserPeer.retrieveByPK(Integer.parseInt(data.getUserId()));
		tuser.setTitle(data.getParameters().getString("title"));
		tuser.setAddress(data.getParameters().getString("address"));
		tuser.setCity(data.getParameters().getString("city"));
		tuser.setState(data.getParameters().getString("state"));
		tuser.setZipCode(data.getParameters().getString("zipcode"));
		tuser.setCountry(data.getParameters().getString("country"));
		tuser.setWebPage(data.getParameters().getString("webpage"));
		if(data.getParameters().getString("hostid")!=null && !data.getParameters().getString("hostid").isEmpty()) {
			tuser.setHostId(data.getParameters().getString("hostid"));
			tuser.setHostIdTime(new Date());
		}
	    if (data.getParameters().getString("realname")==null){
	        tuser.setHallOfFame("false");
	    }else{
	        tuser.setHallOfFame("true");
	    }
	    tuser.save();
        TurbineUserPeer.executeStatement("update TURBINE_USER set TITLE='"+data.getParameters().getString("title")+"', ADDRESS='"+data.getParameters().getString("address")+"', CITY='"+data.getParameters().getString("city")+"', STATE='"+data.getParameters().getString("state")+"', ZIP_CODE='"+data.getParameters().getString("zipcode")+"', COUNTRY='"+data.getParameters().getString("country")+"', WEB_PAGE='"+data.getParameters().getString("webpage")+"' where USER_ID="+tuser.getUserId());
        if(data.getParameters().getString("hostid")!=null && !data.getParameters().getString("hostid").isEmpty()) {
                java.util.Date today=new Date();
                java.sql.Date date=new java.sql.Date(today.getTime()); //your SQL date object
                SimpleDateFormat simpDate = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                TurbineUserPeer.executeStatement("update TURBINE_USER set HOST_ID='"+data.getParameters().getString("hostid")+"', HOST_ID_TIME='"+simpDate.format(date)+"' where USER_ID="+tuser.getUserId());
        }
		DBUserHistory userHistory = tuser.getCurrentUserHistory();
		userHistory.setLeaveDate(new Date());
		userHistory.save();
		DBUserHistory newUserHistory=new DBUserHistory();
		newUserHistory.setUserId(tuser.getUserId());
		if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true") || new UrlTool().isLabserver() && !data.getParameters().getString("aff2").equals("")){
			newUserHistory.setAffiliation2(data.getParameters().getString("aff2"));
		}else{
			newUserHistory.setAffiliation2(data.getParameters().getString("affiliation2"));
		}
        if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true")){
           newUserHistory.setAffiliation1(DBAffiliation2LovPeer.retrieveByName(newUserHistory.getAffiliation2()).getDBAffiliation1Lov().getAffiliation1());
        }else if(new UrlTool().isLabserver() && !data.getParameters().getString("aff1").equals("")){
            newUserHistory.setAffiliation1(data.getParameters().getString("aff1"));
        }else{
            newUserHistory.setAffiliation1(data.getParameters().getString("affiliation1"));
        }
		newUserHistory.setJoinDate(userHistory.getLeaveDate());
		newUserHistory.setLabgroupId(userHistory.getLabgroupId());
		newUserHistory.save();

		
        UrlTool.getTuser().remove(data.getUserId());
        
        if ( changepass )
        {
            try
            {
                if(tuser.getSalt()==null){
                    tuser.setSalt(GeneralUtils.getNextSalt());
                }
                tuser.setPasswordValue(JetspeedSecurity.encryptPassword(password+tuser.getSalt()));
                data.getUser().setPassword(JetspeedSecurity.encryptPassword(password+tuser.getSalt()));
                tuser.save();
                String sql="update TURBINE_USER set SALT='"+tuser.getSalt()+"' where LOGIN_NAME='"+tuser.getLoginName()+"'";
                TurbineUserPeer.executeQuery(sql);
              //shk3 changed for salt, was: JetspeedSecurity.changePassword(data.getJetspeedUser(),oldPassword, password+salt);
            } catch (JetspeedSecurityException e)
            {
                data.setMessage(e.getMessage());
                backToEditAccount( data, screenData );
                return;
            }
        }
        
        //allow sub-classes to update additional information
        updateUser(data);

        data.setMessage (CustomLocalization.getString("UPDATEACCOUNT_DONE", rundata));
        
    }

    /**
     * updateUser updates the user object.
     * Subclasses can extend this class and override this method - adding additional custom settings as needed.
     * Note the default implementation does nothing - so no need to call the super version.
     *
     * @param data Turbine request/session information.
     */
    protected void updateUser(RunData data)
    {
        //default version does nothing
    }

    private void backToEditAccount( RunData rundata, Hashtable screenData )
    {   
        rundata.getRequest().setAttribute( "ScreenDataEditAccount",
                                           screenData );
        rundata.setScreenTemplate("EditAccount");
    }

}
