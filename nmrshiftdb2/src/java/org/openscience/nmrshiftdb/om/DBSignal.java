/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012,2013,2015 Stefan Kuhn
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    15. Mai 2002
 */
public class DBSignal
     extends org.openscience.nmrshiftdb.om.BaseDBSignal
     implements Persistent {

	
  static DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
  
  static{
	format.applyPattern("0.00");
  }

  /**
   * This is used temporarily in the similarity calculation
   */
  private DBShift[] shiftArray = null;
  private List<DBAtom> atoms=null;
  public Map<Integer, DBShift> shifts=new HashMap<Integer, DBShift>();
  //this is used in making the report only
  public int[] atomnumbers;
  
  public void setShifts(Map<Integer, DBShift> shifts2) {
	this.shifts = shifts2;
  }


  protected List<DBSignalDBAtom> collDBSignalDBAtomsSortByAtomIdAndAxis;

  /**
   * This inits DBxx collections in this object to empty. This should only be used for 
   * "dummy" objects as in the toDBSpectrum method of submitting data. Calling this on 
   * objects retrieved from db will cause relations not being fetched properly. 
   */
  public void initEmptyCollections(){
	  initDBShifts();
  }

  
  /**
   *Sets the dBShiftsArray attribute of the DBSignal object
   *
   * @param  dbshifts  The new dBShiftsArray value
   */
  public void setDBShiftsArray(DBShift[] dbshifts) {
    shiftArray = dbshifts;
  }

  public List<List<DBAtom>> getDBAtomsByAxis() throws TorqueException{
	  List<List<DBAtom>> atoms=new ArrayList<List<DBAtom>>();
	  int atomidcount=-1;
	  int oldaxis=-1;
	  for(int i=0;i<getDBSignalDBAtomsSortByAxis().size();i++){
		  atomidcount++;
		  if(((DBSignalDBAtom)getDBSignalDBAtomsSortByAxis().get(i)).getAxis()!=oldaxis){
			  atomidcount=0;
			  oldaxis=((DBSignalDBAtom)getDBSignalDBAtomsSortByAxis().get(i)).getAxis();
		  }
		  if(((DBSignalDBAtom)getDBSignalDBAtomsSortByAxis().get(i)).getAxis()==1){
			  List<DBAtom> atomsInner=new ArrayList<DBAtom>();
			  atoms.add(atomsInner);
		  }
		  atoms.get(atomidcount).add(((DBSignalDBAtom)getDBSignalDBAtomsSortByAxis().get(i)).getDBAtom());
	  }
	  return atoms;	  
  }
  
  /**
   * If this collection has already been initialized, returns
   * the collection. Otherwise returns the results of
   * getDBSignalDBAtoms(new Criteria())
   *
   * @throws TorqueException
   */
  public List<DBSignalDBAtom> getDBSignalDBAtomsSortByAxis() throws TorqueException
  {
      if (collDBSignalDBAtomsSortByAtomIdAndAxis == null)
      {
          collDBSignalDBAtomsSortByAtomIdAndAxis = getDBSignalDBAtomsSortByAtomIdAndAxis(new Criteria(10));
      }
      return collDBSignalDBAtomsSortByAtomIdAndAxis;
  }
  
  /**
   * If this collection has already been initialized with
   * an identical criteria, it returns the collection.
   * Otherwise if this DBSignal has previously
   * been saved, it will retrieve related DBSignalDBAtoms from storage.
   * If this DBSignal is new, it will return
   * an empty collection or the current collection, the criteria
   * is ignored on a new object.
   *
   * @throws TorqueException
   */
  public List<DBSignalDBAtom> getDBSignalDBAtomsSortByAtomIdAndAxis(Criteria criteria) throws TorqueException
  {
      if (collDBSignalDBAtomsSortByAtomIdAndAxis == null)
      {
          if (isNew())
          {
             collDBSignalDBAtomsSortByAtomIdAndAxis = new ArrayList<DBSignalDBAtom>();
          }
          else
          {
                    criteria.add(DBSignalDBAtomPeer.SIGNAL_ID, getSignalId() );
                    criteria.addAscendingOrderByColumn(DBSignalDBAtomPeer.AXIS);
                    collDBSignalDBAtomsSortByAtomIdAndAxis = DBSignalDBAtomPeer.doSelect(criteria);
          }
      }

      return collDBSignalDBAtomsSortByAtomIdAndAxis;
  }

  public DBAtom getDBAtom(int axis, int number) throws TorqueException{
	  int count=0;
	  for(int i=0;i<getDBSignalDBAtoms().size();i++){
		  if(((DBSignalDBAtom)getDBSignalDBAtoms().get(i)).getAxis()==axis){
			  if(count==number){
				  return ((DBSignalDBAtom)getDBSignalDBAtoms().get(i)).getDBAtom();
			  }else{
				  count++;
			  }
		  }
	  }
	  return null;
  }
  
  public DBShift getDBShift(int axis) throws TorqueException{
	  if(shifts.get(new Integer(axis))==null){
		  for(int i=0;i<this.getDBShifts().size();i++){
			  if(((DBShift)this.getDBShifts().get(i)).getAxis()==axis)
				  shifts.put(new Integer(axis), (DBShift)this.getDBShifts().get(i));
		  }
	  }
	  if(shifts.get(new Integer(axis))==null)
		  throw new TorqueException("No shift on axis "+axis);
	  return shifts.get(new Integer(axis));
  }

  /**
   *Gets the atoms of the DBSignal object
   *
   * @return                The atoms
   * @exception  TorqueException  Something wrong with reading database
   */
  public List<DBAtom> getAtoms() throws TorqueException {
    if(atoms==null){
      Criteria crit = new Criteria();
      crit.addJoin(DBSignalDBAtomPeer.ATOM_ID, DBAtomPeer.ATOM_ID);
      crit.add(DBSignalDBAtomPeer.SIGNAL_ID, this.getSignalId());
      atoms = DBAtomPeer.doSelect(crit);
    }
    return (atoms);
  }
  
  /**
   * Sets the atoms attribute. This will cause getAtoms() not to retrieve from db. Use with caution!
   * 
   * @param atoms The atoms of this Signal.
   */
  public void setAtoms(List<DBAtom> atoms){
	  this.atoms=atoms;
  }


  /**
   *Gets the atoms of the DBSignal object
   *
   * @return                The atoms
   * @exception  Exception  Something wrong with reading database
   */
  public List<DBAtom> getAtoms(Connection dbconn) throws Exception {
    if(atoms==null){
      Criteria crit = new Criteria();
      crit.addJoin(DBSignalDBAtomPeer.ATOM_ID, DBAtomPeer.ATOM_ID);
      crit.add(DBSignalDBAtomPeer.SIGNAL_ID, this.getSignalId());
      atoms = DBAtomPeer.doSelect(crit,dbconn);
    }
    return (atoms);
  }


  public String getCoupling() throws TorqueException{
    if(this.getDBCouplings().size()>0)
      return Double.toString(((DBCoupling)this.getDBCouplings().get(0)).getConstant());
    else
      return "";
  }


  /**
   *Gets the Shift of the first dimension of the DBSignal object
   *
   * @return                The Shift value
   * @exception  Exception  DatabaseProblems
   */
  public DBShift getFirstShift() throws TorqueException {
	  return this.getDBShift(1);
  }


  /**
   *Gets the dBShiftsArray attribute of the DBSignal object
   *
   * @return    The dBShiftsArray value
   * @throws TorqueException 
   */
  public DBShift[] getDBShiftsArray() throws TorqueException {
	  if(shiftArray==null){
		  shiftArray=new DBShift[this.getDBShifts().size()];
		  for(int i=0;i<shiftArray.length;i++)
			  shiftArray[i]=(DBShift)this.getDBShifts().get(i);
	  }
	  return (shiftArray);
  }


  /**
   *  Gets the intensity attribute of the DBSignal object formatted like x.xx.
   *
   * @return    The intensityFormatted value.
   */
  public String getIntensityFormatted() {
    return (format.format(getIntensity()));
  }
  
  
  /**
   *  Tells if a signal has a multiplictiy (i. e. if !getMulitplicity.equals(""))
   *
   * @return    Does this has a multiplicity?
   */  
  public boolean hasMultiplicity(){
	  return !this.getMultiplicity().equals("");
  }


   public List<DBSignalDBAtom> getDBSignalDBAtoms() throws TorqueException {
       ArrayList<DBSignalDBAtom> results = new ArrayList<DBSignalDBAtom>();
       for(int i=0;i<this.getDBShifts().size();i++){
           results.addAll(((DBShift)this.getDBShifts().get(i)).getDBSignalDBAtomsRelatedBySignalId());
       }
       return results;
   }
   
   public String getHAtomString(int spectrumid) throws TorqueException, NmrshiftdbException {
	   StringBuffer sb=new StringBuffer();
	   List<Integer> alreadydone=new ArrayList<>();
	   for(DBAtom atom : this.getAtoms()) {
		   if(atom.getIsVisible().equals(NmrshiftdbConstants.TRUE)) {
			   if(atom.getIdentifier(spectrumid).equals("")) {
				   sb.append(atom.getIdentifier(spectrumid)+", ");
			   }else {
				   sb.append(atom.getMdlNumberPlus1()+", ");
			   }
		   }else {
			   if(!alreadydone.contains(atom.getMdlNumberPlus1Heavy())) {
				   sb.append("C-"+atom.getMdlNumberPlus1HeavyOrIdentifier(spectrumid)+", ");
				   alreadydone.add(atom.getMdlNumberPlus1Heavy());
			   }
		   }
	   }
	   return GeneralUtils.removeLastComma(sb).toString();
   }

}

