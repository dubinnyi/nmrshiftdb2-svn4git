/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2012 Stefan Kuhn
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
package org.openscience.nmrshiftdb.om;


// JDK classes
import java.util.List;

import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Criteria.Criterion;



/** 
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public  class DBLabGroup 
    extends org.openscience.nmrshiftdb.om.BaseDBLabGroup
    implements Persistent
{
	/**
	 * Gets all members of this labgroup. ordered by last name/first name.
	 * 
	 * @return A list of NmrshiftdbUsers.
	 * @throws TorqueException
	 */
	public List<TurbineUser> getMembers() throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBUserHistoryPeer.LABGROUP_ID,this.getLabgroupId());
		crit.add(DBUserHistoryPeer.LEAVE_DATE, (Object)new String(DBUserHistoryPeer.LEAVE_DATE+" is NULL"), Criteria.CUSTOM);
		crit.addJoin(DBUserHistoryPeer.USER_ID, NmrshiftdbUserPeer.USER_ID);
		crit.addAscendingOrderByColumn(NmrshiftdbUserPeer.LAST_NAME);
		crit.addAscendingOrderByColumn(NmrshiftdbUserPeer.FIRST_NAME);
		return NmrshiftdbUserPeer.doSelect(crit);
	}
	
	/**
	 * Gets all valid machines of this labgroup.
	 * 
	 * @return A list of DBMachines.
	 * @throws TorqueException
	 */
	public List<DBMachine> getDBMachinesValid() throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBMachinePeer.VALID,"true");
		return super.getDBMachines(crit);
	}
	
	/**
	 * Gets all valid machines of this labgroup which are in operator mode.
	 * 
	 * @return A list of DBMachines.
	 * @throws TorqueException
	 */
	public List<DBMachine> getDBMachinesValidOperator() throws TorqueException{
		Criteria crit=new Criteria();
		crit.and(DBMachinePeer.VALID,"true");
		Criterion c1= crit.getNewCriterion(DBMachinePeer.MEASURE_OPERATOR,"true",Criteria.EQUAL);
		Criterion c2= crit.getNewCriterion(DBMachinePeer.MEASURE_OPERATOR_NIGHT,"true",Criteria.EQUAL);
		crit.and(c1.or(c2));
		return super.getDBMachines(crit);
	}

	/**
	 * Gets all valid experiments of this group.
	 * 
	 * @return A list of DBExperiments.
	 * @throws TorqueException
	 */
	public List<DBExperiment> getDBExperimentsValid() throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBExperimentPeer.VALID,"true");
		return super.getDBExperiments(crit);
	}
}



