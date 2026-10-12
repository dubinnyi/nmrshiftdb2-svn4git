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
package org.openscience.nmrshiftdb.om;

// JDK classes
import org.apache.torque.om.Persistent;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    25. Juni 2002
 */
public class DBKeyword
		 extends org.openscience.nmrshiftdb.om.BaseDBKeyword
		 implements Persistent {
	/**
	 *Used to rembemer if a Condition is choosen durig submit
	 */
	private boolean isChoosen = false;


	/**
	 *Sets the isChoosen attribute of the DBKeyword object
	 *
	 * @param  b  The new isChoosen value
	 */
	public void setIsChoosen(boolean b) {
		isChoosen = b;
	}


	/**
	 *Gets the isChoosen attribute of the DBKeyword object
	 *
	 * @return    The isChoosen value
	 */
	public boolean getIsChoosen() {
		return (isChoosen);
	}
}


