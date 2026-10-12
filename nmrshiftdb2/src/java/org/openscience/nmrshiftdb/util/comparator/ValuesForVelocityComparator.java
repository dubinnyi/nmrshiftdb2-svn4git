/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2012-2013 Stefan Kuhn
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.torque.TorqueException;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

/**
 *This is implementation of Comparator used for sorting ValuesForVelocityBeans according to the desired columns.
 *
 * @author     shk3
 * @created    02 October 2012
 */
public class ValuesForVelocityComparator implements Comparator<ValuesForVelocityBean> {
	
	private int ordercolumn=1;
	boolean updown=false;
	int dimensionality=1;
	
	//TODO explain ordercolumn
	public ValuesForVelocityComparator(int ordercolumn, boolean updown, int dimensionality) {
		super();
		this.ordercolumn = ordercolumn;
		this.updown=updown;
		this.dimensionality=dimensionality;
	}
	/**
	 *The compare method
	 *
	 * @param  obj1  The first ValueaForVelocityBean
	 * @param  obj2  The second ValueaForVelocityBean
	 * @return       -1,0,1
	 * @throws NmrshiftdbException 
	 * @throws TorqueException 
	 */
	public int compare(ValuesForVelocityBean obj1, ValuesForVelocityBean obj2) {
		try{
			if(dimensionality==1){
				if(ordercolumn==-1){
					if(((ValuesForVelocityBean) obj1).getValue()>((ValuesForVelocityBean) obj2).getValue()){
						if(updown)
							return -1;
						else
							return 1;
					}else if(((ValuesForVelocityBean) obj1).getValue()<((ValuesForVelocityBean) obj2).getValue()){
						if(updown)
							return 1;
						else
							return -1;
					}else{
						return 0;
					}
				}else if(ordercolumn==-2){
				    return(obj1.getNameForElements().compareTo(obj2.getNameForElements()));
				}else if(ordercolumn==-3){
					String id1=obj1.getIdentifier();
					if(id1.equals(""))
						id1=Integer.toString(obj1.getValue().intValue()+1);
					String id2=obj2.getIdentifier();
					if(id2.equals(""))
						id2=Integer.toString(obj2.getValue().intValue()+1);
			        String patternString = "^[0-9]+";
			        String text1=id1;
			        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
			        Matcher matcher = pattern.matcher(text1);
			        int intpart1=-1;
			        String rest1="";
			        if(matcher.find()) {
			        	intpart1=Integer.parseInt(text1.substring(0,matcher.end()));
			            rest1=text1.substring(matcher.end());
			        }
			        String text2=id2;
			        matcher = pattern.matcher(text2);
			        int intpart2=-1;
			        String rest2="";
			        if(matcher.find()) {
			        	intpart2=Integer.parseInt(text2.substring(0,matcher.end()));
			            rest2=text2.substring(matcher.end());
			        }
		        	if(intpart1==intpart2) {
				        if(!updown)
				            return(rest1.compareTo(rest2));
				        else
				            return(rest2.compareTo(rest1));
		        	} else  if(intpart1>intpart2){
	                        if(updown)
	                            return -1;
	                        else
	                            return 1;
                    }else {
                        if(updown)
                            return 1;
                        else
                            return -1;
                    }
				}
				if(((ValuesForVelocityBean) obj1).getShifts()!=null && ((ValuesForVelocityBean) obj2).getShifts()!=null &&((ValuesForVelocityBean) obj1).getShifts().length>=ordercolumn && ((ValuesForVelocityBean) obj2).getShifts().length>=ordercolumn){
					if(((ValuesForVelocityBean) obj1).getShifts()[ordercolumn-1]>((ValuesForVelocityBean) obj2).getShifts()[ordercolumn-1]){
						if(updown)
							return -1;
						else
							return 1;
					}else if(((ValuesForVelocityBean) obj1).getShifts()[ordercolumn-1]<((ValuesForVelocityBean) obj2).getShifts()[ordercolumn-1]){
						if(updown)
							return 1;
						else
							return -1;
					}else{
						return 0;
					}
				}else{
					return 1;
				}
			}else{
				if(ordercolumn==-3){
					ordercolumn=1;
				}
				int axis=ordercolumn/2+1;
				boolean isAtomNumber =ordercolumn %2==0;
				if(isAtomNumber){
					if(((ValuesForVelocityBean) obj1).getSignal().getDBAtom(axis, 0).getMdlNumber()>((ValuesForVelocityBean) obj2).getSignal().getDBAtom(axis, 0).getMdlNumber()){
						if(updown)
							return -1;
						else
							return 1;
					}else if(((ValuesForVelocityBean) obj1).getSignal().getDBAtom(axis, 0).getMdlNumber() < ((ValuesForVelocityBean) obj2).getSignal().getDBAtom(axis, 0).getMdlNumber()){
						if(updown)
							return 1;
						else
							return -1;
					}else{
						return 0;
					}				
				}else if(!isAtomNumber){
					if(((ValuesForVelocityBean) obj1).getSignal().getDBShift(axis).getValue()>((ValuesForVelocityBean) obj2).getSignal().getDBShift(axis).getValue()){
						if(updown)
							return -1;
						else
							return 1;
					}else if(((ValuesForVelocityBean) obj1).getSignal().getDBShift(axis).getValue() < ((ValuesForVelocityBean) obj2).getSignal().getDBShift(axis).getValue()){
						if(updown)
							return 1;
						else
							return -1;
					}else{
						return 0;
					}
				}else{
					return 0;
				}
			}
		}catch(Exception ex){
			throw new IllegalArgumentException(ex);
		}
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}
