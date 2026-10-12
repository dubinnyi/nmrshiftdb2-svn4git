/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2012,2015 Stefan Kuhn
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.om.DBAffiliation1Lov;
import org.openscience.nmrshiftdb.om.DBAffiliation1LovPeer;
import org.openscience.nmrshiftdb.om.DBAffiliation2Lov;
import org.openscience.nmrshiftdb.om.DBAffiliation2LovPeer;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBLabGroupPeer;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.workingdogs.village.Record;

/**
 * This class is mostly used as a jetspeed tool and offers various methods to build urls and 
 * other things based on a RunData object. If used independently, it must be initialized via init().
 *
 * @author     shk3.
 * @created    April 29, 2003.
 */
public class UrlTool implements ApplicationTool {

  private RunData data = null;
  private static HashMap<String,UserInformation> users=new HashMap<String,UserInformation>();
  private static HashMap<String,TurbineUser> tuser=new HashMap<String,TurbineUser>();


  /**
   *  Gets the logon wiht(out) link.
   *
   * @return    The link.
   */
  public String getAppropriateURL() {
    String url = data.getRequest().getRequestURL().toString();
    if (url.indexOf("https") != -1) {
      url = "<a href=\"" + GeneralUtils.replace(url, "https", "http") + "\">Logon without SSL</a>";
    } else {
      url = "<a href=\"" + GeneralUtils.replace(url, "http", "https") + "\">Logon with SSL</a>";
    }
    return (url);
  }


  /**
   *  Tells if the current connection is by https or not.
   *
   * @return    The isHttps value.
   */
  public String getIsHttps() {
    if (data.getRequest().getRequestURL().toString().indexOf("https") != -1) {
      return ("true");
    } else {
      return ("false");
    }
  }


  /**
   *  Gets the number of users on a certain server.
   *
   * @param  servername  The name of the server.
   * @return             The users value.
   */
  public String getUsers(String servername) {
	  try {
	    DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Service  service = new Service();
	    Call     c    = (Call) service.createCall();
	    c.setOperationName("getUserNumber");
	    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    SOAPBodyElement[] input = new SOAPBodyElement[1];
	    Document doc = builder.newDocument();
	    Element cdataElem;
	    Options opts = new Options(new String[0]);
	    opts.setDefaultURL("http://"+servername+"/axis/services/NMRShiftDB");
	    c.setTargetEndpointAddress( new URL(opts.getURL()) );
	    cdataElem = doc.createElementNS(opts.getURL(), "getUserNumber");
	    input[0] = new SOAPBodyElement(cdataElem);
	    @SuppressWarnings("rawtypes")
		Vector          elems = (Vector) c.invoke( input );
	    SOAPBodyElement elem  = null ;
	    Element         e     = null ;
	    elem = (SOAPBodyElement) elems.get(0);
	    e    = elem.getAsDOM();
	    String usernumber=e.getFirstChild().getNodeValue();
	    return usernumber;
      }catch(Exception ex){
    	  GeneralUtils.logError(ex,"UrlTool.getUsers, "+servername, null, false);
    	  return("Unavailable");
      }
  }


  /**
   *  Gets the baseUrl attribute of the UrlTool object
   *
   * @return    The baseUrl value
   */
  public String getBaseUrl() {
	  return getBaseUrl(data.getRequest()); 
  }

  /**
   *  Gets the baseUrl attribute of the UrlTool object
   *
   * @return    The baseUrl value
   */
  public String getBaseUrl(HttpServletRequest request) {
    StringTokenizer st = new StringTokenizer(request.getRequestURL().toString(), "/");
    StringBuffer url = new StringBuffer(st.nextToken());
    url.append("//");
    url.append(st.nextToken()+"/nmrshiftdb/portal");
    return (url.toString());
  }


  /**
   *  Init from ApplicationTool interface.
   *
   * @param  data  RunData.
   */
  public void init(Object data) {
    this.data = (RunData) data;
    try {
      Cookie[] cookies = this.data.getRequest().getCookies();
      if (cookies != null) {
        String username = "";
        String password = "";
        for (int k = 0; k < cookies.length; k++) {
          Cookie CTemp = cookies[k];
          if(!CTemp.getValue().equals("invalidnmrshiftdb")){
            CTemp.setMaxAge(3000000);
            this.data.getResponse().addCookie(CTemp);
            if (CTemp.getName().equals("NmrshiftdbUser")) {
              username = CTemp.getValue();
            }
            if (CTemp.getName().equals("NmrshiftdbPassword")) {
              password = CTemp.getValue();
            }
          }
        }
        if(!username.equals("")){
          User user = TurbineSecurity.getAuthenticatedUser(username, password);
          this.data.setUser(user);
          user.setHasLoggedIn(new Boolean(true));
          user.updateLastLogin();
          this.data.save();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      //We do nothing if this fails
    }
  }


  /**
   *  Refresh from ApplicationTool interface.
   */
  public void refresh() { }
  
  public static HashMap<String,UserInformation> getUsersMap(){
    return(users);
  }
  
  public List<DBLabGroup> getLabGroups() throws Exception{
    Criteria crit=new Criteria();
    return(DBLabGroupPeer.doSelect(crit));
  }
  
  public int getRandomId() throws Exception{
      String sql="select MOLECULE_ID from MOLECULE ORDER BY rand() LIMIT 1;";
      List<Record> l=DBMoleculePeer.executeQuery(sql);
      return ((Record)l.get(0)).getValue(1).asInt();
  }
  
  public List<String> getAff1() throws Exception{
      List<String> v2=new Vector<String>();
      if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true")){
          Criteria criteria=new Criteria();
          List<DBAffiliation1Lov> aff1 = DBAffiliation1LovPeer.doSelect(criteria);
          for(Object aff1value : aff1){
              v2.add(((DBAffiliation1Lov)aff1value).getAffiliation1());
          }
      }else{
          v2.add("");
    	  List<Record> v=DBAffiliation1LovPeer.executeQuery("select distinct AFFILIATION_1 from USER_HISTORY where LABGROUP_ID>0 and LEAVE_DATE is null order by AFFILIATION_1");
    	  for(int i=0;i<v.size();i++){
    		  v2.add(((Record)v.get(i)).getValue(1).asString());
    	  }
      }
      return v2;
  }
  
  public List<String> getAff2() throws Exception{
      List<String> v2=new Vector<String>();
      if(GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true")){
          Criteria criteria=new Criteria();
          List<DBAffiliation2Lov> aff2 = DBAffiliation2LovPeer.doSelect(criteria);
          for(Object aff2value : aff2){
              v2.add(((DBAffiliation2Lov)aff2value).getAffiliation2());
          }
      }else{
          v2.add("");
    	  List<Record> v=DBAffiliation1LovPeer.executeQuery("select distinct AFFILIATION_2 from USER_HISTORY where LABGROUP_ID>0 and LEAVE_DATE is null order by AFFILIATION_2");
    	  for(int i=0;i<v.size();i++){
    		  v2.add(((Record)v.get(i)).getValue(1).asString());
    	  }
      }
	  return v2;
  }
  
  public boolean getAff1NotSet() throws FileNotFoundException, IOException{
      return GeneralUtils.getNmrshiftdbProperty("affiliation1")==null;
  }
  
  public boolean getAff2NotSet() throws FileNotFoundException, IOException{
      return GeneralUtils.getNmrshiftdbProperty("affiliation2")==null;
  }
  
  public TurbineUser getTurbineUser(JetspeedRunData data) throws TooManyRowsException, TorqueException{
      return getTurbineUser(data.getUserId());
  }
  
  public TurbineUser getTurbineUser(String userId) throws TooManyRowsException, TorqueException{
	  if(tuser.get(userId)==null)
		  tuser.put(userId,TurbineUserPeer.retrieveByPK(Integer.parseInt(userId)));
	  return tuser.get(userId);
  }
  
  public boolean isLabserver() throws IOException{
	  return GeneralUtils.getNmrshiftdbProperty("rawfiledirectory")!=null;
  }

  public boolean showCaptcha() throws IOException{
	  return GeneralUtils.getNmrshiftdbProperty("usecaptcha").equals("true");
  }

  public static boolean doImageCache() throws IOException{
	  return GeneralUtils.getNmrshiftdbProperty("doimagecache").equals("true");
  }

  public boolean use2DSpectra() throws IOException{
      return GeneralUtils.getNmrshiftdbProperty("use2dspectra").equals("true");
  }
  

  public boolean useFixedAffiliations() throws IOException{
      return GeneralUtils.getNmrshiftdbProperty("fixedaffiliations").equals("true");
  }  
  
  public static HashMap<String, TurbineUser> getTuser() {
	return tuser;
  }
	
	public static String getShortQueryString(RunData data){
	  try{
	  StringBuffer uri=new StringBuffer();
	  String query=data.getRequest().getQueryString();
	  StringTokenizer st=new StringTokenizer(query,"&");
	  while(st.hasMoreElements()){
		  String parameter=(String)st.nextElement();
		  if(parameter.indexOf("applet")==-1 && parameter.indexOf("ordercolumn")==-1 && parameter.indexOf("updown")==-1){
			  uri.append(parameter+"&");
		  }
	  }
	  if(uri.length()>1)
	  	return uri.substring(0,uri.length()-1);
	  else
		return uri.toString();
	  }catch(Exception ex){
		  ex.printStackTrace();
		  return "";
	  }
	}
	
	public static String removesessionid(String url) {
	    if(url.indexOf(";jsessionid=")>0) {
	        if(url.indexOf("?")>0) {
	            return url.substring(0,url.indexOf(";jsessionid="))+url.substring(url.indexOf("?"));
	        }else {
	            return url.substring(0, url.indexOf(";jsessionid="));
	        }
	    }
	    return url;
	}
	
	public boolean editorExists() throws TorqueException {
        String sql="select TURBINE_USER.USER_ID from TURBINE_USER join TURBINE_USER_GROUP_ROLE using (USER_ID ) join TURBINE_ROLE_PERMISSION using (ROLE_ID) join TURBINE_PERMISSION using (PERMISSION_ID ) where PERMISSION_NAME =\"edit\"";
        List<Record> records = NmrshiftdbUserPeer.executeQuery(sql);
        return records.size()!=0;
	}

	public String escapeHtml(String input){
	   return org.apache.commons.lang.StringEscapeUtils.escapeHtml(input);
	}
}

