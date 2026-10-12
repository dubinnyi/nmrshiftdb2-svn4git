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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.torque.TorqueException;
import org.openscience.nmrshiftdb.om.DBSignal;

/**
 *This is implememtation of Comparator used for sorting DBSignals according to shift1
 *
 * @author     shk3
 * @created    4. M�rz 2002
 */
public class DBSignalComparator implements Comparator<DBSignal>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1402438545078383116L;

	
	private int ordercolumn=1;
	boolean updown=false;
	
	public DBSignalComparator(){
		super();
	}
	
	public DBSignalComparator(int ordercolumn, boolean updown) {
		super();
		this.ordercolumn = ordercolumn;
		this.updown=updown;
	}

	/**
	 *The compare method
	 *
	 * @param  obj1  The first ValueaForVelocityBean
	 * @param  obj2  The second ValueaForVelocityBean
	 * @return       -1,0,1
	 */
	public int compare(DBSignal obj1, DBSignal obj2){
		int returnvalue=0;
		try{
			if(((DBSignal) obj1).getDBShifts().size()>=ordercolumn && ((DBSignal) obj2).getDBShifts().size()>=ordercolumn){
				if(((DBSignal) obj1).getDBShift(ordercolumn).getValue()>((DBSignal) obj2).getDBShift(ordercolumn).getValue()){
					if(updown)
						returnvalue= -1;
					else
						returnvalue= 1;
				}else if(((DBSignal) obj1).getDBShift(ordercolumn).getValue()<((DBSignal) obj2).getDBShift(ordercolumn).getValue()){
					if(updown)
						returnvalue= 1;
					else
						returnvalue= -1;
				}else{
					returnvalue= 0;
				}
			}else{
				returnvalue= 2;
			}
		}catch(TorqueException ex){
			returnvalue= 3;
		}
		return returnvalue;
	}
}

