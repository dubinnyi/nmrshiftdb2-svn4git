/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011 Stefan Kuhn
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

import java.util.List;

import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

// JDK classes

/** 
  *  You should add additional methods to this class to meet the
  *  application requirements.  This class will only be generated as
  *  long as it does not already exist in the output directory.
  */
public class DBMachinePeer 
    extends org.openscience.nmrshiftdb.om.BaseDBMachinePeer
{

	/**
	 * Gets all valid machines for a certain lab group.
	 * 
	 * @param labgroupId The id of the lab group.
	 * @return A list of DBMachines.
	 * @throws NoRowsException
	 * @throws TooManyRowsException
	 * @throws TorqueException
	 */
	public static List<DBMachine> getValidMachinesOfGroup(int labgroupId) throws NoRowsException, TooManyRowsException, TorqueException {
		return DBLabGroupPeer.retrieveByPK(labgroupId).getDBMachinesValid();
	}
	
	/**
	 * Gets all valid machines for a certain lab group which are open for operator measurements.
	 * 
	 * @param labgroupId The id of the lab group.
	 * @return A list of DBMachines.
	 * @throws NoRowsException
	 * @throws TooManyRowsException
	 * @throws TorqueException
	 */
	public static List<DBMachine> getValidOperatorMachinesOfGroup(int labgroupId) throws NoRowsException, TooManyRowsException, TorqueException {
		return DBLabGroupPeer.retrieveByPK(labgroupId).getDBMachinesValidOperator();
	}

	/**
	 * Returns a machine by name.
	 * 
	 * @param parameter The name.
	 * @return The machine, or null if no suche machine.
	 * @throws TorqueException
	 */
	public static DBMachine retrieveByName(String parameter) throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBMachinePeer.NAME, parameter);
		List<DBMachine> machines=DBMachinePeer.doSelect(crit);
		if(machines.size()>0)
			return machines.get(0);
		else
			return null;
	}
}

