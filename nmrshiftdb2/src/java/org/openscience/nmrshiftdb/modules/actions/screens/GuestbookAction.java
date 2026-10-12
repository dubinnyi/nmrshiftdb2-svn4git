/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012 Stefan Kuhn
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

import java.util.Date;

import org.apache.turbine.modules.Action;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.CaptchaServlet;
import org.openscience.nmrshiftdb.om.DBGuestbook;
import org.openscience.nmrshiftdb.util.GeneralUtils;

/**
 * Handles actions for the Guestbook screen
 *
 * @author     shk3
 * @created    10. February 2004
 */
public class GuestbookAction extends Action {
	public static final String ENTRIES_KEY = "entries";

    /**
	 * saves an entry to the guestbook
	 */
	public void doPerform( RunData data ) throws Exception { 
      String text=data.getParameters().get("text");
      //test the captcha
      boolean captcharight = false;
      if(GeneralUtils.getNmrshiftdbProperty("usecaptcha",data).equals("true")){
          String sessionToken = CaptchaServlet.getToken(data.getSession());
          String answer = data.getParameters().getString("captcha");
          if (!sessionToken.equals(answer)) {
				data.setMessage(text);
				return;            	
	        }else{
	        	captcharight=true;
	        }
      }else{
    	  captcharight=true;
      }
      if(captcharight){
    	  DBGuestbook entry=new DBGuestbook();
    	  if(data.getUser().hasLoggedIn())
    		  entry.setUserId(Integer.parseInt(((org.apache.jetspeed.om.security.BaseJetspeedUser)data.getUser()).getUserId()));
    	  entry.setDate(new Date());
    	  entry.setText(text);
    	  entry.setValid("true");
    	  entry.save();
      }
	}
}
