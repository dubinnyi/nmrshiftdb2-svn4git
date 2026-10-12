/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2013 Stefan Kuhn
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

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * This sorts Record object according to their 1 column, which  must be a double (used for shifts).
 * 
 * @author shk3
 *
 */
public class ShiftsAsRecordComparator implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		double shift0=0;
		double shift1=0;
		try {
			shift1 = ((Record) arg1).getValue(1).asDouble();
			shift0 = ((Record) arg0).getValue(1).asDouble();
		} catch (DataSetException e) {
			throw new RuntimeException(e);
		}
		if(shift0<shift1)
			return -1;
		else if(shift1<shift0)
			return 1;
		else
			return 0;
	}

}
