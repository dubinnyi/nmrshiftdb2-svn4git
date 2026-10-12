/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-,2014 Stefan Kuhn
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
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    16. April 2002
 */
public class DBCondition
     extends org.openscience.nmrshiftdb.om.BaseDBCondition
     implements Persistent {
  /**
   *Gets the conditionName attribute of the DBConditionType object this DBCondition is associated with
   *
   * @return                The conditionName value
   * @exception  Exception  Description of Exception
   */
  public String getConditionName() throws TorqueException {
    return (getDBConditionType().getConditionName());
  }
  
  public DBConditionType getDBConditionType(Connection dbconn) throws TorqueException{
	  return DBConditionTypePeer.retrieveByPK(SimpleKey.keyFor(this.getConditionTypeId()),dbconn);
  }

	public DBMachineCondition getDBMachineCondition(DBMachine dbMachine) throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBMachineConditionPeer.CONDITION_ID,  this.getConditionId());
		crit.add(DBMachineConditionPeer.MACHINE_ID, dbMachine.getMachineId());
		List<DBMachineCondition> result = DBMachineConditionPeer.doSelect(crit);
		return result.get(0);
	}
}


