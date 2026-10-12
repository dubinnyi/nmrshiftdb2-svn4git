/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010 Stefan Kuhn
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
package org.openscience.nmrshiftdb.util;

import java.util.ArrayList;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.turbine.util.Log;
import org.openscience.nmrshiftdb.om.DBSessionsPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
/**
 *  This is a vector which writes itself to the db when unbound from session
 *
 * @author     shk3
 * @created    3. Januar 2003
 */
@SuppressWarnings("serial")
public class VectorWithSessionBinding<T> extends ArrayList<T> implements HttpSessionBindingListener {
  private String username = null;
  public static final String REQUEST_IP="request_ip";
  /**
   *Constructor for the VectorWithSessionBinding object
   *
   * @param  username  It might be the user is already unbound from session when this object is unbound, so we remember the user.
   */
  public VectorWithSessionBinding(String username) {
    super();
    this.username = username;
  }


  /**
   *  This writes this object serialized with jsx to a field in the TURBINE_USER table which has the name under which this object is bound to the session
   *
   * @param  event  HttpSessionBindingEvent
   */
  public void valueUnbound(HttpSessionBindingEvent event) {
    //We use unbinding of this object as an indicator that a session is closed
    try{
      if(event.getName().equals(NmrshiftdbConstants.SEARCHHISTORY)){
      	UrlTool.getUsersMap().remove(event.getSession().getId());
      	DBSessionsPeer.executeStatement("UPDATE SESSIONS SET SESSIONS="+UrlTool.getUsersMap().size());
      }
    }
    catch(Exception ex){
      Log.error(ex.getMessage());
    }
    if (username != null && username !="anon") {
      try {
        while (size() > 50) {
          remove(0);
        }
        String seri = GeneralUtils.escape(GeneralUtils.getAsXMLString(this),false);
        String field = event.getName();
        String sql = "UPDATE TURBINE_USER set " + field + "=\"" + seri + "\" where LOGIN_NAME=\"" + username + "\";";
        NmrshiftdbUserPeer.executeStatement(sql);
      } catch (Exception ex) {
        Log.error(ex.getMessage());
      }
    }
  }


  /**
   *  Takes care of session tracking
   *
   * @param  event  HttpSessionBindingEvent
   */
  public void valueBound(HttpSessionBindingEvent event) {
    //We use binding of this object as an indicator that a session is opened
    try{
      if(event.getName().equals(NmrshiftdbConstants.SEARCHHISTORY)){
    	  if(UrlTool.getUsersMap().get(event.getSession().getId())==null){
    		  DBSessionsPeer.executeStatement("UPDATE SESSIONS SET SESSIONS_ALL=SESSIONS_ALL+1");
    	  }
    	  UrlTool.getUsersMap().put(event.getSession().getId(), new UserInformation(System.currentTimeMillis(), (String)event.getSession().getAttribute(REQUEST_IP)));
    	  DBSessionsPeer.executeStatement("UPDATE SESSIONS SET SESSIONS="+UrlTool.getUsersMap().size());
      }
    }
    catch(Exception ex){
      Log.error(ex.getMessage());
    }
  }
}


