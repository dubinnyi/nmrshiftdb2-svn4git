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
package org.openscience.nmrshiftdb.om;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

// JDK classes


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    15. Mai 2002
 */
public class DBSpectrumType
     extends org.openscience.nmrshiftdb.om.BaseDBSpectrumType
     implements Persistent {
       
       boolean choosen=true;
       
       public DBSpectrumType(){
    	   super();
       }
       
       
       public DBSpectrumType(int id, String name, int dimensionality) throws Exception{
    	   super();
    	   this.setSpectrumTypeId(id);
    	   this.setName(name);
    	   this.setDimensionality(dimensionality);
       }
       
       
       public void setChoosen(boolean choosen){
         this.choosen=choosen;
       }
       
       
       public boolean getChoosen(){
         return choosen;
       }
       
       
  /**
   *Gets the elementSymbol of the isotope of theDBSpectrumType
   *
   * @return                The elementSymbol value
   * @exception  Exception  Something wrong with reading database
   */
  public String getElementSymbol() throws TorqueException {
	  StringBuffer sb=new StringBuffer();
	  for(int i=1;i<=this.getDBIsotopes().size();i++){
		  sb.append(((DBIsotope)this.getDBIsotope(i)).getElementSymbol());
	  }
	  return (sb.toString());
  }


  /**
   * Tells if this spectrum type requries peaks in the input (this is using 
   * the PEAKS_MANDATORY column in the spectrum type table).
   * 
   * @return True=peaks must be entered, false=no peaks required.
   */
  public boolean requiresPeaks(){
    if(this.getPeaksMandatory().equals(NmrshiftdbConstants.TRUE))
      return true;
    else
      return false;
  }


  /**
   *  Gets the name of the DBSpectrumType object formatted for HTML.
   *
   * @return                The name value.
   * @exception  Exception  Database problems.
   */
  public String getNameFormatted() throws Exception {
    StringBuffer name = new StringBuffer();
    name.append("<sup>");
    name.append(getDBIsotope(1).getAtomicMass());
    name.append("</sup>");
    name.append(getElementSymbol());
    return (name.toString());
  }
  
  
  public String getNameAsString() {
    return new String(getName());
  }


  /**
   *  Gets the dBIsotope in a certain axis.
   *
   * @param  axis           The axis.
   * @return                The dBIsotope value
   * @exception  TorqueException  Database problems.
   */
  public DBIsotope getDBIsotope(int axis) throws TorqueException {
    Criteria criteria = new Criteria();
    criteria.add(DBIsotopePeer.SPECTRUM_TYPE_ID, getSpectrumTypeId());
    criteria.add(DBIsotopePeer.AXIS, axis);
    return ((DBIsotope) DBIsotopePeer.doSelect(criteria).get(0));
  }
}


