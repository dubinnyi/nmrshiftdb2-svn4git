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
package org.openscience.nmrshiftdb.util.comparator;

import java.util.Comparator;

import com.workingdogs.village.Record;

/**
 * This is an implementation of Comparator used for sorting Records of solvents by name.
 * The record must come from a statement where VALUE is one of the selected fields.
 *
 * @author     shk3
 * @created    1/2/2017
 */
public class SolventComparator implements Comparator<Record> {
	/**
	 * The compare method
	 *
	 * @param  obj1  The first solvent
	 * @param  obj2  The second solvent
	 * @return       -1,0,1
	 */
	public int compare(Record obj1, Record obj2){
		try {
			return obj1.getValue("VALUE").asString().compareToIgnoreCase(obj2.getValue("VALUE").asString());
		} catch (Exception e) {
			//We do nothing here
			return 0;
		}
	}
}

