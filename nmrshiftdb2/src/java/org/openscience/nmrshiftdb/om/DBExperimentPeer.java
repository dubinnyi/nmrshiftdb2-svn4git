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

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
@SuppressWarnings("serial")
public class DBExperimentPeer
    extends org.openscience.nmrshiftdb.om.BaseDBExperimentPeer
{
	/**
	 * Gets all valid experiments for a certain lab group.
	 * 
	 * @param labgroupId The id of the labgroup.
	 * @return A list of DBExperiments.
	 * @throws NoRowsException
	 * @throws TooManyRowsException
	 * @throws TorqueException
	 */
	public static List<DBExperiment> getValidExperimentsOfGroup(int labgroupId) throws NoRowsException, TooManyRowsException, TorqueException {
		return DBLabGroupPeer.retrieveByPK(labgroupId).getDBExperimentsValid();
	}
}
