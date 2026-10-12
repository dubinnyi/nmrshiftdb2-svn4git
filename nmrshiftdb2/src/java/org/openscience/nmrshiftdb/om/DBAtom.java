/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2015 Stefan Kuhn
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
import java.io.IOException;
import java.io.OptionalDataException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    31. Juli 2002
 */
@SuppressWarnings("serial")
public class DBAtom
     extends org.openscience.nmrshiftdb.om.BaseDBAtom
     implements Persistent {

  DBCoordinateSet3d coords3 = null;
  DBCoordinateSet2d coords2 = null;
  public boolean force3d=false;
  private List<DBCoupling> couplings=null;
  //if these are set, their values override the getmdlNumberPlus1Heavy/getIdentifier methods
  public int mdlNumberPlus1Heavy=-1;
  public String identifier=null;


  /**
   *Constructor for the DBAtom object
   */
  public DBAtom() {
    super();
  }


  /**
   *Constructor for the DBAtom object.
   *
   * @param  atom           The cdk atom this atom is supposed to represent.
   * @param  dbmol          The molecule it comes from
   * @param  hoseCode       The hose code.
   * @param  visible        Should this atom be visible.
   * @param  atomnumber     The number this atom shall have in the molecule.
   * @exception  Exception  Description of Exception.
   */
  public DBAtom(IAtom atom, IAtomContainer ac, int dbmol, String hoseCode, String hoseCodeWithRings, boolean visible, int atomnumber, int equivalencenumber) throws TorqueException {
    this();
    setSymbol(atom.getSymbol());
    setMoleculeId(dbmol);
    if(atom.getMassNumber()!=null)
    	setAtomicMass(atom.getMassNumber());
    else
        setAtomicMass(0);
    if(atom.getFormalCharge()!=null)
    	setFormalCharge(atom.getFormalCharge());
    if (atom.getPoint3d()!=null && ((!Double.isNaN(atom.getPoint3d().z) && atom.getPoint3d().z != 0) || force3d)) {
      coords3 = new DBCoordinateSet3d(atom.getPoint2d().x, atom.getPoint2d().y, atom.getPoint3d().z, 1);
    } else {
      coords2 = new DBCoordinateSet2d(atom.getPoint2d().x, atom.getPoint2d().y, 1);
    }
    if (atom.getFlag(CDKConstants.ISAROMATIC)) {
      setIsAromatic(NmrshiftdbConstants.TRUE);
    } else {
      setIsAromatic(NmrshiftdbConstants.FALSE);
    }
    if (hoseCode != null) {
      setHoseCode(hoseCode.getBytes());
      setHoseCodeWithRings(hoseCodeWithRings.getBytes());
    } else {
      setHoseCode(new byte[0]);
      setHoseCodeWithRings(new byte[0]);
    }
    if(atom.getSymbol().equals("H") && ac.getConnectedAtomsList(atom).size()>0 && !((IAtom)ac.getConnectedAtomsList(atom).get(0)).getSymbol().equals("C")){
    	this.setHetero("true");
    }
    if (visible) {
      setIsVisible("true");
    }
    setAtomNumber(atomnumber);
    setEquivalenceNumber(equivalencenumber);
  }


  /**
   *Gets the bonds of the DBAtom object
   *
   * @return                The bonds
   * @exception  TorqueException  Database problems
   */
  public Vector<DBBond> getBonds() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBondDBAtomPeer.ATOM_ID, this.getAtomId());
    List<DBBondDBAtom> v = DBBondDBAtomPeer.doSelect(crit);
    Vector<DBBond> bonds = new Vector<DBBond>();
    for (int i = 0; i < v.size(); i++) {
      bonds.add(DBBondPeer.retrieveByPK(((DBBondDBAtom) v.get(i)).getBondId()));
    }
    return bonds;
  }


  /**
   *  Gets all atoms connected to dbatoms.
   *
   * @param mol The cdk molecule the atoms are in.
   * @param dbatoms the atoms for which connected atoms are searched (must all come from one molecule).
   * @return                		The connected atoms
   * @throws NmrshiftdbException 	See message.
   */
  public Vector<DBAtom> getConnectedAtoms(IAtomContainer mol, DBAtom[] dbatoms) throws NmrshiftdbException {
	  Vector<DBAtom> connecteddbatoms=new Vector<DBAtom>();
	  for (int i = 0; i < dbatoms.length; i++) {
		  if(this.getAtomId()==dbatoms[i].getAtomId()){
			  List<IAtom> connectedatoms=mol.getConnectedAtomsList(mol.getAtom(i));
			  for(int k=0;k<connectedatoms.size();k++){
				  connecteddbatoms.add(dbatoms[mol.getAtomNumber((IAtom)connectedatoms.get(k))]);
			  }
			  return connecteddbatoms;
		  }
	  }
	  throw new NmrshiftdbException("no such atom in dbatoms in getConnectedAtoms. dbatoms are from another molecule than myself");
  }
  
  
  /**
   *  Gets the atoms connected with this atom
   *
   * @return                The connected atoms
   * @exception  TorqueException  Database Problems
   */
  public Vector<DBAtom> getConnectedAtoms() throws TorqueException {
    Vector<DBAtom> connectedAtoms = new Vector<DBAtom>();
    Vector<DBBond> bonds = getBonds();
    for (int i = 0; i < bonds.size(); i++) {
      Vector<DBAtom> atoms = ((DBBond) bonds.get(i)).getAtoms();
      for (int k = 0; k < atoms.size(); k++) {
        if (!((DBAtom) atoms.get(k)).equals(this)) {
          connectedAtoms.add((DBAtom)atoms.get(k));
        }
      }
    }
    return (connectedAtoms);
  }


  /**
   * Gets the number of hydrogens connected to the DBAtom
   *
   * @return                The hydrogenCount value
   * @exception  TorqueException  Database problems
   */
  public int getHydrogenCount() throws TorqueException{
    int hcount = 0;
    Vector<DBAtom> v = getConnectedAtoms();
    for (int i = 0; i < v.size(); i++) {
      if (((DBAtom) v.get(i)).getSymbol().equals("H")) {
        hcount++;
      }
    }
    return (hcount);
  }
  
  
  /**
   * Gets the DBSignal associated with this atom in a certain spectrum, if there is exactly one. null if none or several signals.
   *
   * @param  spectrumId     The spectrum of which the signal should be.
   * @return                A random DBSignal value.
   * @exception  TorqueException  Database problems.
   */
  public DBSignal getAnySignal(int spectrumId) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBSignalDBAtomPeer.ATOM_ID, this.getAtomId());
    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
    crit.add(DBSignalPeer.SPECTRUM_ID, spectrumId);
    List<DBSignal> v = DBSignalPeer.doSelect(crit);
    if (v.size() != 1) {
      return (null);
      //There is more than one signal for spectrum spectrumId and atom getAtomId()
    }
    return ((DBSignal) v.get(0));
  }


  /**
   *  Gets the mdlNumber value + 1, if atom is a heavy atom of the atom itself, if a hydrogen, of the heavy atom it is attached to.
   *
   * @return                The mdlNumberPlus1 value
   * @exception  TorqueException
   * @exception  NmrshiftdbException
   */
  public int getMdlNumberPlus1Heavy() throws NmrshiftdbException, TorqueException {
	  if(mdlNumberPlus1Heavy!=-1)
		  return mdlNumberPlus1Heavy;
	  if(this.getSymbol().equals("H")){
		  return ((DBAtom)this.getConnectedAtoms().get(0)).getMdlNumberPlus1();
	  }else{
		  return (getMdlNumber() + 1);
	  }
  }
  
  
  /**
   * Gets the either the mdlNumber value + 1, if atom is a heavy atom of the atom itself, if a hydrogen, of the heavy atom it is attached to. If 
   * there is an identifier for the atom or the heavy atom this is returned instead of the number.
   * 
   * @param spectrumId
   * @return
   * @throws NmrshiftdbException
   * @throws TorqueException
   */
  public String getMdlNumberPlus1HeavyOrIdentifier(int spectrumId) throws NmrshiftdbException, TorqueException {
	  String identifier;
	  if(this.getSymbol().equals("H")){
		  identifier=((DBAtom)this.getConnectedAtoms().get(0)).getIdentifier(spectrumId);
	  }else{
		  identifier=this.getIdentifier(spectrumId);
	  }
	  if(identifier.equals("")){
		  return Integer.toString(getMdlNumberPlus1Heavy());
	  }else{
		  return identifier;
	  }		  
  }
  
  /**
   * Gets either the number of the atom or, if it exists, the identifier of that atom in a particular spectrum.
   * 
   * @param spectrumId
   * @return
   * @throws NmrshiftdbException
   * @throws TorqueException
   */
  public String getMdlNumberPlus1OrIdentifier(int spectrumId) throws NmrshiftdbException, TorqueException {
	  if(identifier!=null){
		  return identifier;
	  }else{
		  String identifier=this.getIdentifier(spectrumId);
		  if(identifier.equals(""))
			  return Integer.toString(getMdlNumberPlus1Heavy());
		  else
			  return identifier;
	  }
  }
  
  /**
   * Returns the identifier of this atom in a spectrum if there is one, empty string else.
   * 
   * @param spectrum id of spectrum of which identifiers are searched.
   * @return Identifier or empty string.
   * @throws TorqueException
   */
  public String getIdentifier(int spectrum) throws TorqueException{
	  if(identifier!=null)
		  return identifier;
	  Criteria crit=new Criteria();
	  crit.add(DBSpectrumPeer.SPECTRUM_ID,spectrum);
	  crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBIdentifierPeer.SPECTRUM_ID);
	  crit.add(DBIdentifierPeer.ATOM_ID,this.getAtomId());
	  List<DBIdentifier> v=DBIdentifierPeer.doSelect(crit);
	  if(v.size()==0)
		  return "";
	  else
		  return ((DBIdentifier)v.get(0)).getIdentifier();
  }
  
  
  /**
   * Gets the mdlNumber value + 1
   *
   * @return                The mdlNumberPlus1 value
   * @exception  Exception  Description of Exception
   */
  public int getMdlNumberPlus1() throws TorqueException, NmrshiftdbException {
    return (getMdlNumber() + 1);
  }


  /**
   *  Gets the mdl number of the Atom. The mdl number is the number of the atom in the molecule, in the order as they were in 
   *  the mdl of the original submit. Starts with 0.
   *
   * @return                The mdlNumber value.
   * @exception  TorqueException  Database problems.
   * @throws NmrshiftdbException 
   */
  public int getMdlNumber() throws TorqueException, NmrshiftdbException {
    List<DBAtom> v = DBMoleculePeer.retrieveByPK(this.getMoleculeId()).getDBAtoms();
    int i = 0;
    Iterator<DBAtom> it = v.iterator();
    while (it.hasNext()) {
      DBAtom atom = (DBAtom) it.next();
      double[] coords = atom.getCoordinates(1);
      if (atom.getSymbol().equals(this.getSymbol()) && coords[0] == this.getCoordinates(1)[0] && coords[1] == this.getCoordinates(1)[1] && coords[2] == this.getCoordinates(1)[2]) {
        return (i);
      } else {
        i++;
      }
    }
    throw new NmrshiftdbException("This should never happen. Error is in DBAtom.getMdlNumber");
  }
  
  /**
   *  Gets the mdl number of the Atom. The mdl number is the number of the atom in the molecule, in the order as they were in 
   *  the mdl of the original submit, disregarding invisible Hs. Starts with 0.
   *
   * @return                The mdlNumber value.
   * @exception  TorqueException  Database problems.
   * @throws NmrshiftdbException 
   */
  public int getMdlNumberVisible() throws TorqueException, NmrshiftdbException {
    List<DBAtom> v = DBMoleculePeer.retrieveByPK(this.getMoleculeId()).getDBAtoms();
    int i = 0;
    Iterator<DBAtom> it = v.iterator();
    while (it.hasNext()) {
      DBAtom atom = (DBAtom) it.next();
      double[] coords = atom.getCoordinates(1);
      if (atom.getSymbol().equals(this.getSymbol()) && coords[0] == this.getCoordinates(1)[0] && coords[1] == this.getCoordinates(1)[1] && coords[2] == this.getCoordinates(1)[2]) {
        return (i);
      } else if(atom.getIsVisible().equals(NmrshiftdbConstants.TRUE)){
        i++;
      }
    }
    throw new NmrshiftdbException("This should never happen. Error is in DBAtom.getMdlNumber");
  }


  /**
   * Gets this atom as a cdk atom, i. e. the atom from the cdk molecule of the DBMolecule this atom is in which corresponds to this atom.
   *
   * @return The CDK atom.
   * @throws IOException 
   * @throws OptionalDataException 
   * @exception  TorqueException
   * @exception  DataSetException
   */
  public IAtom getAsCdkAtom() throws DataSetException, TorqueException, OptionalDataException, IOException {
    DBAtom[] atoms = getDBMolecule().getAtomsAsArrayInMdlOrder();
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].equals(this)) {
        return (getDBMolecule().getAsCDKMolecule(1).getAtom(i));
      }
    }
    return null;
  }


  /**
   * Gets the identifier attribute of the DBAtom object in a certain spectrum.
   *
   * @param  spectrum       The spectrum the identifier should be in.
   * @return                The identifier value.
   * @exception  TorqueException  Database problems.
   */
  public String getIdentifier(DBSpectrum spectrum) throws TorqueException {
    if (spectrum == null) {
      return ("");
    }
    return getIdentifier(spectrum.getSpectrumId());
  }


  /**
   * Gets a string giving the multiplicity and coupling constants of the signal of this atom in a certain spectrum.
   *
   * @param  spectrum       Spectrum in which the signal should be.
   * @return                The multiplicity value.
   * @throws DataSetException 
   * @exception  Exception  Database problems.
   */
  public String getMultiplicity(DBSpectrum spectrum) throws TorqueException, DataSetException {
    StringBuffer multi = new StringBuffer();
    DBSignal signal = getAnySignal(spectrum.getSpectrumId());
    if (signal != null) {
      multi.append(signal.getMultiplicity());
      List<Record> v = DBCouplingPeer.getBySpectrumAndAtom(spectrum, this);
      if (v.size() > 0) {
        multi.append(" (");
        for (int i = 0; i < v.size(); i++) {
          multi.append(((Record) v.get(i)).getValue(1).asString() + ",");
        }
        GeneralUtils.removeLastComma(multi);
        multi.append(")");
      }
    }
    return (multi.toString());
  }


  /**
   * Gets the couplings of this atom in a certain spectrum.as a string
   *
   * @param  spectrum       The spectrum.
   * @return                The couplings.
   * @exception  Exception  Database problems.
   */
  public boolean hasCouplings(DBSpectrum spectrum) throws Exception {
	  getCouplings(spectrum);
	  return couplings.size()>0;
  }

  
  /**
   * Gets the couplings of this atom in a certain spectrum.as a string
   *
   * @param  spectrum       The spectrum.
   * @return                The couplings.
   * @exception  Exception  Database problems.
   */
  public String getCouplingsString(DBSpectrum spectrum) throws Exception {
	  getCouplings(spectrum);
	  StringBuffer couplingsstring=new StringBuffer();
	  for(int i=0;i<couplings.size();i++){
		  couplingsstring.append(((DBCoupling)couplings.get(i)).getConstant()+",");
	  }
	  return (GeneralUtils.removeLastComma(couplingsstring).toString());
  }

  
  /**
   * Gets the couplings of this atom in a certain spectrum.
   *
   * @param  spectrum       The spectrum.
   * @return                The couplings.
   * @exception  TorqueException  Database problems.
   */
  public List<DBCoupling> getCouplings(DBSpectrum spectrum) throws TorqueException {
	  if(couplings==null){
	    Criteria crit = new Criteria();
	    crit.add(DBCouplingPeer.ATOM_ID_1, getAtomId());
	    crit.add(DBCouplingPeer.SPECTRUM_ID, spectrum.getSpectrumId());
	    List<DBCoupling> v1 = DBCouplingPeer.doSelect(crit);
	    couplings=new Vector<DBCoupling>();
	    couplings.addAll(v1);
	  }
	  return (couplings);
  }


  /**
   * Gets the coordinates of this atom in a coordinate set with a certain coordinate set id.
   *
   * @param  coordinateSetId  The coordinate set id.
   * @return                  The coordinates value [0]=x,[1]=y,[2]=z only with 3d coordinates.
   * @exception  TorqueException    Database problems.
   * @throws NmrshiftdbException No such coordinate set. 
   */
  public double[] getCoordinates(int coordinateSetId) throws TorqueException, NmrshiftdbException {
    double[] coords = new double[3];
    List<DBCoordinateSet3d> v = getDBCoordinateSet3ds();
    boolean has3d = false;
    if (v.size() > 0) {
      for (int i = 0; i < v.size(); i++) {
        if (((DBCoordinateSet3d) v.get(0)).getDBCoordinateSetType().getCoordinateSetTypeId() == coordinateSetId) {
          coords[0] = ((DBCoordinateSet3d) v.get(i)).getXCoordinate();
          coords[1] = ((DBCoordinateSet3d) v.get(i)).getYCoordinate();
          coords[2] = ((DBCoordinateSet3d) v.get(i)).getZCoordinate();
          has3d = true;
        }
      }
    }
    if (!has3d) {
      List<DBCoordinateSet2d> v2 = getDBCoordinateSet2ds();
      for (int i = 0; i < v2.size(); i++) {
        if (((DBCoordinateSet2d) v2.get(0)).getDBCoordinateSetType().getCoordinateSetTypeId() == coordinateSetId) {
          coords[0] = ((DBCoordinateSet2d) v2.get(0)).getXCoordinate();
          coords[1] = ((DBCoordinateSet2d) v2.get(0)).getYCoordinate();
          has3d=true;
        }
      }
    }
    if(!has3d)
      throw new NmrshiftdbException("No coordinates with id "+coordinateSetId+" for atom "+getAtomId());
    return coords;
  }
  
  
  /**
   * Tells if a coordinate of this atom with a certain coordindate set id is a 3d coordinate.
   * 
   * @param coordinateSetId The coordinate set id.
   * @return true=is 3d, false=2d or not existing.
   * @throws TorqueException 
   */
  public boolean isCoordinate3d(int coordinateSetId) throws TorqueException {
    List<DBCoordinateSet3d> v=getDBCoordinateSet3ds();
    for(int i=0;i<v.size();i++){
      if(((DBCoordinateSet3d)v.get(i)).getCoordinateSetTypeId()==coordinateSetId){
        return true;
      }
    }
    return false;
  }


  /**
   * Gets all shifts of this atom in spectra reviewed as true.
   *
   * @return                The dBSignals value
   * @exception  Exception  Description of Exception
   */
  public List<DBSignal> getDBSignals() throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBSignalDBAtomPeer.ATOM_ID, getAtomId());
    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
    crit.addJoin(DBSignalPeer.SPECTRUM_ID, DBSpectrumPeer.SPECTRUM_ID);
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
    return (DBSignalPeer.doSelect(crit));
  }


  /**
   * Gets the dBSignals attribute of the DBAtom object.
   *
   * @param type			m=only measured signals,c=only calculated signals included
   * @return                The dBSignals value
   * @exception  Exception  Description of Exception
   */
  public List<DBSignal> getDBSignals(char type) throws TorqueException {
	  if(type!='c' && type!='m')
		  throw new TorqueException("type must be c or m");
    Criteria crit = new Criteria();
    crit.add(DBSignalDBAtomPeer.ATOM_ID, getAtomId());
    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
    crit.addJoin(DBSignalPeer.SPECTRUM_ID, DBSpectrumDBConditionPeer.SPECTRUM_ID);
    crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
    crit.addJoin(DBConditionPeer.CONDITION_TYPE_ID, DBConditionTypePeer.CONDITION_TYPE_ID);
    crit.add(DBConditionTypePeer.CONDITION_TYPE,Character.toString(type));
    crit.setDistinct();
    return (DBSignalPeer.doSelect(crit));
  }
  
  
  /**
   * Gets the dBSignals attribute of the DBAtom object
   *
   * @param      spectrum   The spectrum the signal should come from
   * @return                The dBSignals value
   * @exception  Exception  Description of Exception
   */
  public List<DBSignal> getDBSignals(DBSpectrum spectrum) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBSignalDBAtomPeer.ATOM_ID, getAtomId());
    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
    crit.add(DBSignalPeer.SPECTRUM_ID, spectrum.getSpectrumId());
    return (DBSignalPeer.doSelect(crit));
  }
  
  /**
   * Saves this object incl. coordinates.
   *
   * @param  dbCon          A database connection.
   * @exception  Exception  Description of Exception
   */
  public void save(Connection dbCon) throws TorqueException {
    super.save(dbCon);
    if (coords2 != null) {
      coords2.setDBAtom(this);
      coords2.save(dbCon);
    }
    if (coords3 != null) {
      coords3.setDBAtom(this);
      coords3.save(dbCon);
    }
  }


  /**
   * Deletes this atom.
   *
   * @param  conn           The description to use.
   * @exception  Exception  Database problems.
   */
  public void delete(Connection conn) throws TorqueException {
    try{
	    Criteria crit = new Criteria();
	    crit.add(DBCoordinateSet3dPeer.ATOM_ID, getAtomId());
	    DBCoordinateSet3dPeer.doDelete(crit, conn);
	    crit = new Criteria();
	    crit.add(DBCoordinateSet2dPeer.ATOM_ID, getAtomId());
	    DBCoordinateSet2dPeer.doDelete(crit, conn);
	    crit = new Criteria();
	    crit.add(DBCouplingPeer.ATOM_ID_1, getAtomId());
	    DBCouplingPeer.doDelete(crit, conn);
	    crit = new Criteria();
	    crit.add(DBCouplingPeer.ATOM_ID_2, getAtomId());
	    DBCouplingPeer.doDelete(crit, conn);
	    crit = new Criteria();
	    crit.add(DBAtomPeer.ATOM_ID, getAtomId());
	    DBAtomPeer.doDelete(crit, conn);
	    String sql="delete from DESCRIPTORS_1 where ATOM_ID="+getAtomId();
	    DBAtomPeer.executeStatement(sql,conn);
	    sql="delete from DESCRIPTORS_2 where ATOM_ID="+getAtomId();
	    DBAtomPeer.executeStatement(sql,conn);
	    sql="delete from DESCRIPTORS_3 where ATOM_ID="+getAtomId();
	    DBAtomPeer.executeStatement(sql,conn);
	    sql="delete from DESCRIPTORS_4 where ATOM_ID="+getAtomId();
	    DBAtomPeer.executeStatement(sql,conn);
	    sql="delete from DESCRIPTORS_ALL where ATOM_ID="+getAtomId();
	    DBAtomPeer.executeStatement(sql,conn);
    }catch(Exception ex){
      GeneralUtils.logError(ex,"problem deleting "+getAtomId(),null,true);
      throw ex;
    }
    return;
  }


  /**
   * Gives all signals of a certain type assigned to the atom.
   * 
   * @param typname Type name as in the NAME column of the spectrum_Type table.
   * @return The signals.
   * @throws TorqueException
   */
  public List<DBSignal> getDBSignalsBySpectrumType(String typname) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBSignalDBAtomPeer.ATOM_ID, this.getAtomId());
    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
    crit.addJoin(DBSignalPeer.SPECTRUM_ID, DBSpectrumPeer.SPECTRUM_ID);
    crit.addJoin(DBSpectrumPeer.SPECTRUM_TYPE_ID,DBSpectrumTypePeer.SPECTRUM_TYPE_ID);
    crit.add(DBSpectrumTypePeer.NAME,typname);
    return(DBSignalPeer.doSelect(crit));
  }
  
  /**
   * Returns either the hydrogen count of this atom if it is not hydrogen, or the hydrogen count 
   * of the atom this atom is attached to otherwise.
   * 
   * @param cdkmol The cdk molecule the atoms are in.
   * @param atoms the atoms for which connected atoms are searched (must all come from one molecule).
   * @return The hydrogen count.
   * @throws NmrshiftdbException
   * @throws TorqueException
   */
  public int getIntegralStuff(DBAtom[] atoms, IAtomContainer cdkmol) throws NmrshiftdbException, TorqueException {
	  if(this.getSymbol().equals("H")){
		  return ((DBAtom)this.getConnectedAtoms(cdkmol,atoms).get(0)).getHydrogenCount();
	  }else{
		  return this.getHydrogenCount();
	  }
  }
  
  /**
   * Returns either the hydrogen count of this atom if it is not hydrogen, or the hydrogen count 
   * of the atom this atom is attached to otherwise.
   * 
   * @return
   * @throws TorqueException
   */
  public int getIntegralStuff() throws TorqueException{
	  if(this.getSymbol().equals("H")){
		  return ((DBAtom)this.getConnectedAtoms().get(0)).getHydrogenCount();
	  }else{
		  return this.getHydrogenCount();
	  }
  }
}


