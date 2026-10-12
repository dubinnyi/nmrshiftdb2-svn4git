/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012,2015 Stefan Kuhn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscience.nmrshiftdb.modules.actions.screens;

// java.util
import java.util.List;

import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.torque.util.Criteria;
import org.apache.turbine.modules.Action;
import org.apache.turbine.om.security.peer.TurbineUserPeer;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;


/**
 *This action sets a new password for a user and sends it by email.
 *
 * @author     shk3
 * @created    April 29, 2003
 */


public class RememberPassword extends Action
{
	/**
	 *  Performs the action.
	 *
	 * @param  data           The RunData object.
	 * @exception  Exception  Description of Exception
	 */
	public void doPerform(RunData data)
		throws Exception
	{
			String email = data.getParameters().getString("email", "");
      Criteria crit=new Criteria();
      crit.add(NmrshiftdbUserPeer.EMAIL,email);
      if(data.getParameters().getString("username")!=null)
    	  crit.add(TurbineUserPeer.USERNAME, data.getParameters().getString("username"));
      List<TurbineUser> v=NmrshiftdbUserPeer.doSelect(crit);
      if(v.size()==0){
				data.setMessage("There is no such email");
				data.setScreenTemplate("RememberPassword");
				return;
      }else if(v.size()>1){
			data.setMessage("More than one account, please enter username as well");
			data.setScreenTemplate("RememberPassword");
			data.getParameters().add("enterusername", "true");
			return;    	  
      }
      String password=""+"" + new Double("" + Math.random() * 100000).intValue();
      TurbineUser user=(TurbineUser)v.get(0);
      if(user.getSalt()==null){
          user.setSalt(GeneralUtils.getNextSalt());
      }
      user.setPasswordValue(JetspeedSecurity.encryptPassword(password+user.getSalt()));
      user.save();
      MailMessage emailmess = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(data), email, GeneralUtils.getAdminEmail(data), "New nmrshiftdb2 password", "You requested a new password for nmrshiftdb2. The new passwod is " + password + ". You can change this as soon as you are logged in!" + GeneralUtils.getEmailSignature(data));
      if (emailmess.send() == false) {
        GeneralUtils.sendEmailToEventReceivers("Sending new password failed","User "+v.get(0).getLoginName()+" requested a new password, but sending the email with the password failed",data,2);
      }
      data.setMessage("The new password has been emailed to your address!");
			data.setScreenTemplate("ShowMessage");
	}
}

