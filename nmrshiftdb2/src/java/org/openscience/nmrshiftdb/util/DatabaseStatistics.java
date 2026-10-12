/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2012,2014-2015 Stefan Kuhn
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
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.om.DBSessionsPeer;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;


/**
 *This class contains the statistics displayed in the top.vm
 *
 * @author     shk3
 * @created    22. M�rz 2002
 */
public class DatabaseStatistics implements Serializable{
  /**
	 * 
	 */
  private static final long serialVersionUID = -8117597137123034738L;
  private static String numberOfUsers = "";
  private static String numberOfStructures = "";
  private static String numberOfSpectra = "";
  private static String numberOfMeasuredSpectra = "";
  private static String numberOfCalculatedSpectra = "";
  private static String numberOfLabUsers = "";
  private static String numberOfLabStructures = "";
  private static String numberOfLabSpectra = "";
  private static String numberOfLabMeasuredSpectra = "";
  private static String numberOfLabCalculatedSpectra = "";
  private static long lastUpdate=0;

  
  /**
   *Constructor for the DatabaseStatistics object. Initializes the attributes from the DB
   */
  public DatabaseStatistics(){
      if(System.currentTimeMillis()-lastUpdate>60*60*1000){
          lastUpdate=System.currentTimeMillis();
        Connection dbconn = null;
        try {      
          dbconn = Torque.getConnection();
          Statement stmt = dbconn.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TURBINE_USER;");
          rs.next();
          numberOfUsers = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT(MOLECULE_ID)) FROM SPECTRUM WHERE REVIEW_FLAG='true';");
          rs.next();
          numberOfStructures = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT SPECTRUM.SPECTRUM_ID) FROM SPECTRUM, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE WHERE REVIEW_FLAG='true' AND SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (CONDITION_TYPE.CONDITION_TYPE=\"c\" OR CONDITION_TYPE.CONDITION_TYPE=\"p\");");
          rs.next();
          numberOfCalculatedSpectra = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT SPECTRUM.SPECTRUM_ID) FROM SPECTRUM, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE WHERE REVIEW_FLAG='true' AND SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (CONDITION_TYPE.CONDITION_TYPE=\"m\");");
          rs.next();
          numberOfMeasuredSpectra = rs.getString(1);
          numberOfSpectra = new String("" + (new Integer(numberOfMeasuredSpectra).intValue() + new Integer(numberOfCalculatedSpectra).intValue()));
          rs = stmt.executeQuery("SELECT COUNT(*) FROM TURBINE_USER join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null) where LABGROUP_ID >0;");
          rs.next();
          numberOfLabUsers = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT(MOLECULE_ID)) FROM SPECTRUM join TURBINE_USER using (USER_ID) join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null) WHERE REVIEW_FLAG='true'and LABGROUP_ID>0;");
          rs.next();
          numberOfLabStructures = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT SPECTRUM.SPECTRUM_ID) FROM SPECTRUM, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE, TURBINE_USER join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null) WHERE REVIEW_FLAG='true' AND SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (CONDITION_TYPE. CONDITION_TYPE=\"c\" OR CONDITION_TYPE.CONDITION_TYPE=\"p\") and SPECTRUM.USER_ID =TURBINE_USER.USER_ID and LABGROUP_ID>0;");
          rs.next();
          numberOfLabCalculatedSpectra = rs.getString(1);
          rs = stmt.executeQuery("SELECT COUNT(DISTINCT SPECTRUM.SPECTRUM_ID) FROM SPECTRUM, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE, TURBINE_USER join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null) WHERE REVIEW_FLAG='true' AND SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and CONDITION_TYPE. CONDITION_TYPE=\"m\" and SPECTRUM.USER_ID =TURBINE_USER.USER_ID and LABGROUP_ID>0;");
          rs.next();
          numberOfLabMeasuredSpectra = rs.getString(1);
          numberOfLabSpectra = new String("" + (new Integer(numberOfLabMeasuredSpectra).intValue() + new Integer(numberOfLabCalculatedSpectra).intValue()));
          stmt.close();
        } catch (Exception ex) {
          Log.error("Error when trying to read database statistics");
          numberOfUsers = numberOfStructures = numberOfSpectra = "Currently unavailable";
        } finally {
          try {
            Torque.closeConnection(dbconn);
          } catch (Exception e) {
            Log.error("Problems releasing database connection in databaseStatistics: " + e.getMessage());
          }
        }
      }
  }


  /**
   *Gets the numberOfLabUsers attribute of the DatabaseStatistics object
   *
   * @return    The numberOfLabUsers value
   */
  public String getNumberOfLabUsers() {
    return (numberOfLabUsers);
  }


  /**
   *Gets the numberOfLabStructures attribute of the DatabaseStatistics object
   *
   * @return    The numberOfLabStructures value
   */
  public String getNumberOfLabStructures() {
    return (numberOfLabStructures);
  }


  /**
   *Gets the numberOfLabSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfLabSpectra value
   */
  public String getNumberOfLabSpectra() {
    return (numberOfLabSpectra);
  }


  /**
   *Gets the numberOfLabMeasuredSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfLabMeasuredSpectra value
   */
  public String getNumberOfLabMeasuredSpectra() {
    return (numberOfLabMeasuredSpectra);
  }


  /**
   *Gets the numberOfLabCalculatedSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfLabCalculatedSpectra value
   */
  public String getNumberOfLabCalculatedSpectra() {
    return (numberOfLabCalculatedSpectra);
  }


  /**
   *Gets the numberOfUsers attribute of the DatabaseStatistics object
   *
   * @return    The numberOfUsers value
   */
  public String getNumberOfUsers() {
    return (numberOfUsers);
  }


  /**
   *Gets the numberOfStructures attribute of the DatabaseStatistics object
   *
   * @return    The numberOfStructures value
   */
  public String getNumberOfStructures() {
    return (numberOfStructures);
  }


  /**
   *Gets the numberOfSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfSpectra value
   */
  public String getNumberOfSpectra() {
    return (numberOfSpectra);
  }


  /**
   *Gets the numberOfMeasuredSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfMeasuredSpectra value
   */
  public String getNumberOfMeasuredSpectra() {
    return (numberOfMeasuredSpectra);
  }


  /**
   *Gets the numberOfCalculatedSpectra attribute of the DatabaseStatistics object
   *
   * @return    The numberOfCalculatedSpectra value
   */
  public String getNumberOfCalculatedSpectra() {
    return (numberOfCalculatedSpectra);
  }


  /**
   *  Gets the number of open sessions on this server
   *
   * @return    The sessionsNumber value
   */
  public String getSessionsNumber(){
    String sessionUsers="";
    sessionUsers= new String(UrlTool.getUsersMap().size()+"");
    return(sessionUsers);
  }
  
  /**
   *  Gets the number of all sessions on this server (i. e. the value of the sessions_all column of sessions table)
   *
   * @return    The sessionsNumber value
   */
  public String getSessionsNumberAll() throws TorqueException, DataSetException{
	  List<Record> l = DBSessionsPeer.executeQuery("select SESSIONS_ALL from SESSIONS");
	  return ((Record) l.get(0)).getValue(1).asString();
  }

  /**
   *  Gets the start date of the all sessions count on this server (i. e. the value of the sessions_all_date column of sessions table)
   *
   * @return    The sessionsNumber value
   */
  public String getSessionsNumberAllDate() throws TorqueException, DataSetException{
	  List<Record> l = DBSessionsPeer.executeQuery("select SESSIONS_ALL_DATE from SESSIONS");
	  return ((Record) l.get(0)).getValue(1).asString();
  }
  
  public String getImpressumLink(RunData runData) throws FileNotFoundException, IOException{
	  if(GeneralUtils.getNmrshiftdbProperty("impressumlink", runData)!=null){
		  return "<a href=\""+GeneralUtils.getNmrshiftdbProperty("impressumlink", runData)+"\">Impressum</a>";		  
	  }else{
		  return "<img src=\"images/blank.gif\" width=10 height=1>";
	  }
  }
}

