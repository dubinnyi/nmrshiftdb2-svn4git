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
package org.openscience.nmrshiftdb.daemons;

//jetspeed stuff
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.turbine.util.Log;
import org.openscience.nmrshiftdb.om.DBSessionsPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.UserInformation;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
A daemon that looks for the nmrshiftdb2 server with the lowest load and writes this to DB


@author Stefan Kuhn
*/
public class LoadBalancerDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon) {
        Log.info("looking for server with lowest load");
        this.setResult( Daemon.RESULT_PROCESSING );
        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        try {
          Vector<StringAndInt> servers=new Vector<StringAndInt>();
          DocumentBuilder builder = factory.newDocumentBuilder();
          if(GeneralUtils.getNmrshiftdbProperty("useloadbalancing",servcon).equals("true")){
            Document document = builder.parse(GeneralUtils.getNmrshiftdbProperty("serverdirectory",servcon));
            NodeList serversList=document.getElementsByTagName("server");
            for(int i=0;i<serversList.getLength();i++)
            {
              NodeList attlist=serversList.item(i).getChildNodes();
              StringAndInt sai=new StringAndInt();
              for(int j=0;j<attlist.getLength();j++)
              {
                String name=attlist.item(j).getNodeName();
                if(name.equals("name")){
                  sai.myString=attlist.item(j).getFirstChild().getNodeValue();
                }
                if(name.equals("loadfactor")){
                  sai.myInt=new Integer(attlist.item(j).getFirstChild().getNodeValue()).intValue();
                }
              }
              servers.add(sai);
            }
            int lowestLoad=Integer.MAX_VALUE;
            String lowestLoadServer="";
            for(int i=0;i<servers.size();i++){
              String url="";
              try{
                //We check if this server is actually available
                URL u = new URL("http://"+((StringAndInt)servers.get(i)).myString+"/NmrshiftdbServlet?nmrshiftdbaction=test");
                HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                huc.setUseCaches(false);
                huc.setRequestMethod("GET");
                huc.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream())); 
                String inputLine;
                boolean hasSuccess=false;
                while ((inputLine = in.readLine()) != null) { 
                  if(inputLine.indexOf("success")!=-1)
                    hasSuccess=true;
                } 
                in.close(); 
                if(hasSuccess){
                	String usernumber=new UrlTool().getUsers(((StringAndInt)servers.get(i)).myString);
                	if(!usernumber.equals("Unavailable")){
	                    if((new Integer(usernumber).intValue()/((StringAndInt)servers.get(i)).myInt)<lowestLoad){
	                      lowestLoadServer=((StringAndInt)servers.get(i)).myString;
	                      lowestLoad=new Integer(usernumber).intValue()/((StringAndInt)servers.get(i)).myInt;
	                    }
                	}else{
                        Log.error ("Problem calling usernumber web service on " + ((StringAndInt)servers.get(i)).myString);
                	}
                }
              }
              catch(Exception ex){
                Log.error("Error while getting user info from "+url);
              }
            }
            DBSessionsPeer.executeStatement("UPDATE SESSIONS SET LOWEST_LOAD_SERVER='"+lowestLoadServer+"';");
            this.setResult( Daemon.RESULT_SUCCESS );
          }
        }
        catch(Exception ex){
          GeneralUtils.logError(ex,"LoadBalancerDaemon",null,false);
          try{
            DBSessionsPeer.executeStatement("UPDATE SESSIONS SET LOWEST_LOAD_SERVER=NULL;");
          }
          catch(Exception e){
            GeneralUtils.logError(e,"LoadBalancerDaemon blaeh",null,false);
          }
        }
        try{
          HashMap<String, UserInformation> users=UrlTool.getUsersMap();
          Iterator<String> iterator = users.keySet().iterator();
          while (iterator.hasNext()) {
            String userid = (String) iterator.next();
            long value=users.get(userid).loginTime;
            if(System.currentTimeMillis()-value>600000){
              iterator.remove();
              UrlTool.getTuser().remove(userid);
            }
          }
        }
        catch(Exception ex){
          GeneralUtils.logError(ex,"LoadBalancerDaemon/users map",null,false);
        }
    }
}
