/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2015 Stefan Kuhn
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
package org.apache.jetspeed.om.security.turbine;


import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBLabGroupPeer;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMachine;
import org.openscience.nmrshiftdb.om.DBMachineDBUserExcludedPeer;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBReviewGroupDBUserPeer;
import org.openscience.nmrshiftdb.om.DBReviewGroupPeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserHistory;
import org.openscience.nmrshiftdb.om.DBUserHistoryPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
@SuppressWarnings("serial")
public  class TurbineUser
    extends org.apache.jetspeed.om.security.turbine.BaseTurbineUser
    implements Persistent
{
	  private int contributions;
	  private List<DBLiterature> literatureOldSubmit=null;


	  /**
	   *Sets the contributions attribute of the NmrshiftdbUser object
	   *
	   * @param  c  The new contributions value
	   */
	  public void setContributions(int c) {
	    contributions = c;
	  }


	  /**
	   *Gets the contributions attribute of the NmrshiftdbUser object
	   *
	   * @return    The contributions value
	   */
	  public int getContributions() {
	    return (contributions);
	  }


	  /**
	   *Tells if the user is supposed to get reveiws
	   *
	   * @return                The reviewer value
	   * @exception  Exception  Database problems
	   */
	  public boolean isAssignReviews() throws Exception {
	    Criteria crit = new Criteria();
	    crit.addJoin(NmrshiftdbUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
	    crit.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePeer.ROLE_ID);
	    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, this.getLoginName());
	    List<TurbineRole> v = TurbineRolePeer.doSelect(crit);
	    for (int i = 0; i < v.size(); i++) {
	      if (((TurbineRole) v.get(i)).getRoleName().equals("assign_reviews")) {
	        return (true);
	      }
	    }
	    return (false);
	  }


	  /**
	   *Tells if the user is a reviewer
	   *
	   * @return                The reviewer value
	   * @exception  Exception  Database problems
	   */
	  public boolean isReviewer() throws TorqueException {
	    Criteria crit = new Criteria();
	    crit.addJoin(NmrshiftdbUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
	    crit.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePeer.ROLE_ID);
	    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, this.getLoginName());
	    List<TurbineRole> v =TurbineRolePeer.doSelect(crit);
	    for (int i = 0; i < v.size(); i++) {
	      if (((TurbineRole) v.get(i)).getRoleName().equals("reviewer")) {
	        return (true);
	      }
	    }
	    return (false);
	  }


	  /**
	   *Tells if the user is a contributor.
	   *
	   * @return                The contributor value
	   * @exception  Exception  Database problems
	   */
	  public boolean isContributor() throws Exception {
	    Criteria crit = new Criteria();
	    crit.addJoin(NmrshiftdbUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
	    crit.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePeer.ROLE_ID);
	    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, this.getLoginName());
	    List<TurbineRole> v = TurbineRolePeer.doSelect(crit);
	    for (int i = 0; i < v.size(); i++) {
	      if (((TurbineRole) v.get(i)).getRoleName().equals("contributor")) {
	        return (true);
	      }
	    }
	    return (false);
	  }


	  /**
	   *Gets the reviewGroups of the NmrshiftdbUser object
	   *
	   * @return                The reviewGroups
	   * @exception  Exception  Database problems
	   */
	  public List<DBReviewGroup> getReviewGroups() throws TorqueException {
	    Criteria crit = new Criteria();
	    crit.addJoin(DBReviewGroupDBUserPeer.REVIEWGROUP_ID, DBReviewGroupPeer.REVIEWGROUP_ID);
	    crit.add(DBReviewGroupDBUserPeer.USER_ID, this.getUserId());
	    return (DBReviewGroupPeer.doSelect(crit));
	  }


	  /**
	   *  Gets all molecules which have a private spectrum contributed by this user.
	   *
	   * @param  data           The current runData object.
	   * @return                The private contributions, Vector of DBMolecules.
	   * @exception  Exception  Database problems.
	   */
	  public List<DBSpectrum> getMyPrivateSpectra(RunData data) {
	    List<DBSpectrum> myunreviewed = new ArrayList<DBSpectrum>();
	    String query = "select distinct SPECTRUM.SPECTRUM_ID from SPECTRUM where SPECTRUM.REVIEW_FLAG =\"private\" and USER_ID=" + this.getUserId() + ";";
	    fillvector(query, myunreviewed, data);
	    return (myunreviewed);
	  }

	  
	  /**
	   *  Gets all molecules which have an unreviewed spectrum contributed by this user.
	   *
	   * @param  data           The current runData object.
	   * @return                The unreviewed contributions, Vector of DBMolecules.
	   * @exception  Exception  Database problems.
	   */
	  public List<DBSpectrum> getMyUnreviewedContributions(RunData data) {
	    List<DBSpectrum> myunreviewed = new ArrayList<DBSpectrum>();
	    String query = "select distinct SPECTRUM.SPECTRUM_ID from SPECTRUM where SPECTRUM.REVIEW_FLAG =\"false\" and USER_ID=" + this.getUserId() + ";";
	    fillvector(query, myunreviewed, data);
	    return (myunreviewed);
	  }

	  /**
	   * Tells how many spectra of this user are serialized and not yet saved.
	   * 
	   * @param data The current runData object.
	   * @return The count of unsaved spectra.
	   */
	  public int getUnsavedSpectraCount(RunData data) {
	      File serializedDir=new File(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/xml"));
	      File[] filesindir=serializedDir.listFiles();
	      int count=0;
	      for(int i=0;i<filesindir.length;i++){
	         if(filesindir[i].getName().substring(filesindir[i].getName().indexOf("_")+1,filesindir[i].getName().length()-4).equals(Integer.toString(this.getUserId()))){
	             count++;
	         }
	      }
	      return count;
	  }
	  

	  /**
	   *  Gets all molecules which have an unreviewed spectrum, assigned to this user.
	   *
	   * @param  data           The current runData object.
	   * @return                The unreviewed contributions, Vector of DBMolecules.
	   * @exception  Exception  Database problems.
	   */
	  public List<DBSpectrum> getMyMissingReviews(RunData data) {
	    List<DBSpectrum> mymissingreviews = new ArrayList<DBSpectrum>();
	    String query = " select distinct SPECTRUM.SPECTRUM_ID from SPECTRUM join USER_SPECTRUM as us on (SPECTRUM.SPECTRUM_ID=us.SPECTRUM_ID and not exists(select 1 from USER_SPECTRUM as us1 where us1.SPECTRUM_ID=SPECTRUM.SPECTRUM_ID and us1.ASSIGNMENT_DATE>us.ASSIGNMENT_DATE )) where SPECTRUM.REVIEW_FLAG =\"false\" and us.USER_ID="+this.getUserId()+" and REVIEW_DATE is null";
	    fillvector(query, mymissingreviews, data);
	    return (mymissingreviews);
	  }


	  /**
	   *  Fills a vector with the result of a certain sql query and also writes MDLs of result to files.
	   *
	   * @param  query          The query to execute.
	   * @param  tofill         The vector to fill.
	   * @param  data           The current runData object.
	   * @exception  Exception  Database problems.
	   */
	  private void fillvector(String query, List<DBSpectrum> tofill, RunData data){
		  Connection dbconn=null;
		  try{
			    dbconn = Torque.getConnection();
			    Statement stmt = dbconn.createStatement();
			    ResultSet rs = stmt.executeQuery(query);
			    while (rs.next()) {
			      DBSpectrum mol = DBSpectrumPeer.retrieveByPK(new NumberKey(rs.getString(1)));
			      tofill.add(mol);
			    }
		  }catch(Exception ex){
			  GeneralUtils.logError(ex,"fillvector",null,false);
		  }finally{
			  Torque.closeConnection(dbconn);
		  }
	  }
	  
	  
	  public DBLabGroup getDBLabGroup() throws TorqueException{
	      if(getLabgroupId()==null)
	          return null;
	      else
	          return DBLabGroupPeer.retrieveByPK(getLabgroupId());
	  }
	  
	  public DBUserHistory getCurrentUserHistory() throws TorqueException{
			Criteria crit=new Criteria();
			crit.add(DBUserHistoryPeer.LEAVE_DATE, (Object)new String(DBUserHistoryPeer.LEAVE_DATE+" is NULL"), Criteria.CUSTOM);
			crit.add(DBUserHistoryPeer.USER_ID, this.getUserId());
			List<DBUserHistory> l = DBUserHistoryPeer.doSelect(crit);
			if (l.size()!=1)
				throw new TorqueException("Not a unique current entry in user history");
			return (DBUserHistory)l.get(0);		  
	  }
	  
	  public Integer getLabgroupId() throws TorqueException {
		  return getCurrentUserHistory().getLabgroupId();
	}


	public boolean isLabgroupWorker() throws SQLException, TorqueException{
	      return hasPermission("labgroupworker") && hasPermission("labgroupmember");
	}

	public boolean isLabgroupWorker(RunData runData, Portlet portlet) throws Exception{
	      return hasPermission("labgroupworker") && hasPermission("labgroupmember");
	  }


	  public boolean isLabgroupUser(RunData runData, Portlet portlet) throws Exception{
		return hasPermission("labgroupuser") && hasPermission("labgroupmember");
	  }

	  public boolean isLabgroupMember(RunData runData, Portlet portlet) throws Exception{
			return hasPermission("labgroupmember");
	  }

	public List<DBLiterature> getLiteratureOldSubmit() {
		return literatureOldSubmit;
	}


	public void setLiteratureOldSubmit(List<DBLiterature> literatureOldSubmit) {
		this.literatureOldSubmit = literatureOldSubmit;
	}
	
	public List<DBSample> getDBSamples() throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBSamplePeer.USER_ID, this.getUserId());
		return DBSamplePeer.doSelect(crit);
	}

	/**
	 * Tells if this user has the labgrup_leader role
	 * 
	 * @return true=user is labgroup_leader, false=he is not.
	 * @throws Exception
	 */
	public boolean isLabgroupLeader() throws Exception{
		return JetspeedSecurity.hasRole(this.getLoginName(), "labgroup_leader");
	}
	
	/**
	 * Tells if the current user has a certain permission.
	 * NOTE: This should work via Jetspeed, but for me something like new TurbineAccessController().checkPermission((JetspeedUser)runData.getUserFromSession(), portlet, "labgroupmember") always returns true.
	 * 
	 * @param permissionName The name of the permission, as it is in TURBINE_PERMISSION table.
	 * @return
	 * @throws TorqueException
	 * @throws SQLException
	 */
	public boolean hasPermission(String permissionName) throws TorqueException, SQLException{
		Connection conn=null;
		try{
			conn=Torque.getConnection();
	        Statement stmt = conn.createStatement();
	        String sql = "select TURBINE_PERMISSION.PERMISSION_NAME from TURBINE_USER_GROUP_ROLE join TURBINE_ROLE_PERMISSION using (ROLE_ID) join TURBINE_PERMISSION using (PERMISSION_ID ) where USER_ID ="+this.getUserId();
	        ResultSet rs=stmt.executeQuery(sql);
	        while(rs.next()){
	        	if(rs.getString(1).equals(permissionName))
	        		return true;
	        }
	        return false;
		}
		finally{
			Torque.closeConnection(conn);
		}
	}
	
	public String getAffiliation1() throws TorqueException{
		return getCurrentUserHistory().getAffiliation1();
	}
	
	public String getAffiliation2() throws TorqueException{
		return getCurrentUserHistory().getAffiliation2();
	}


	public boolean isMachineForbidden(DBMachine dbMachine) throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBMachineDBUserExcludedPeer.MACHINE_ID, dbMachine.getMachineId());
		crit.add(DBMachineDBUserExcludedPeer.USER_ID, this.getUserId());
		if(DBMachineDBUserExcludedPeer.doSelect(crit).size()>0)
			return true;
		else
			return false;
	}
}
