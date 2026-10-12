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
package org.openscience.nmrshiftdb.util.comparator;

import java.util.Comparator;

import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLPeak;

/**
 *This is implememtation of Comparator used for sorting CMLPeaks according to xvalue, i. e. the shift
 *
 * @author     shk3
 * @created    4. M�rz 2002
 */
public class SignalComparator implements Comparator<CMLElement> {
	
	boolean descending=false;
	
	public SignalComparator(boolean descending){
		this.descending = descending;
	}
	/**
	 *The compare method
	 *
	 * @param  obj1  The first ValueTriple
	 * @param  obj2  The second ValueTriple
	 * @return       -1,0,1
	 */
	public int compare(CMLElement obj1, CMLElement obj2) {
		if (((CMLPeak) obj1).getXValue() < ((CMLPeak) obj2).getXValue()) {
			if(descending)
				return (1);
			else
				return -1;
		}
		if (((CMLPeak) obj1).getXValue() > ((CMLPeak) obj2).getXValue()) {
			if(descending)
				return (-1);
			else
				return 1;
		}
		return (0);
	}
}

