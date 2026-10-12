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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OptionalDataException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.ArrayData;
import org.jcamp.spectrum.Assignment;
import org.jcamp.spectrum.IAssignmentTarget;
import org.jcamp.spectrum.IDataArray1D;
import org.jcamp.spectrum.IOrderedDataArray1D;
import org.jcamp.spectrum.Multiplicity;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.OrderedArrayData;
import org.jcamp.spectrum.Pattern;
import org.jcamp.spectrum.Peak;
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.spectrum.assignments.AtomReference;
import org.jcamp.spectrum.notes.NoteDescriptor;
import org.jcamp.units.CommonUnit;
import org.jcamp.units.Unit;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.isomorphism.matchers.Expr;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.QueryBond;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.modules.actions.portlets.result.ResultAction;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLCml;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.RoCrate.RoCrateBuilder;

import de.unikoeln.chemie.nmr.data.NMR2DSpectrum;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.NmreData.NmredataVersion;
import de.unikoeln.chemie.nmr.data.Peak2D;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import de.unikoeln.chemie.nmr.io.NmredataWriter;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import uk.ac.ebi.orchem.fingerprint.OrchemFingerprinter;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    15. Mai 2002
 */
public class DBMolecule
     extends org.openscience.nmrshiftdb.om.BaseDBMolecule
     implements Persistent {
  /**
   *  Description of the Field
   */
  public double maxsimilarity = -1;

  public IAtomContainer cdkMolecule = null;
  public int setid = -1;
  IAtomContainer cdkMoleculeWithoutHs = null;
  IAtomContainer cdkMoleculeWithoutHsAndCount = null;
  String filename = null;
  public DBAtom[] dbatoms = null;
  public DBAtom[] dbatomsWithoutHs = null;
  long fppro[] = null;
  String autonomeName = null;


  /**
   *Constructor for the DBMolecule object
   */
  public DBMolecule() {
    super();
  }


  /**
   *Constructor for the DBMolecule object
   *
   * @param  mol                 The molecule without Hs.
   * @param  molWithH            The molecule with Hs.
   * @param  now                 Save time.
   * @param  casNumber           cas number.
   * @param  nmrshiftdbnr        nmrshiftdb number.
   * @param  userKey             UserId of submitting user.
   * @param  smilesNonChiral     The non chiral smiles.
   * @param  smilesChiral        The chiral smiles.
   * @param  isChiral            Is the molecule chiral.
   * @param  numberOfAtomsInMdl  atom count of molecule.
   * @param  mfa                 an mfa of this molecule.
   * @param  allrings			 The set of all rings in this molecule.
   * @throws CDKException 
   * @throws TorqueException 
   */
  public DBMolecule(IAtomContainer mol, IAtomContainer molWithH, Date now, String casNumber, int userKey, String smilesNonChiral, String smilesChiral, boolean isChiral, int numberOfAtomsInMdl, IMolecularFormula mfa, IRingSet allrings, int sssr) throws CDKException, TorqueException{
    super();
    initDBCanonicalNames();
    initDBChemicalNames();
    initDBAtoms();
    setDate(now);
    setCasNumber(casNumber);
    try{
    	setMolecularWeight(MolecularFormulaManipulator.getNaturalExactMass(mfa));
    }catch(Exception ex){
    	setMolecularWeight(0);
    	//if this fails (which it should not) we do nothing
    }
    setSmilesString(smilesNonChiral.getBytes());
    setFingerprint(molWithH);
    try{
    	setDbe(MolecularFormulaManipulator.getDBE(mfa));
    }catch(Exception ex){
    	GeneralUtils.logError(ex,"dbmolecule constructor/dbe",null,false);
    }
    try{
    	setSar(allrings.getAtomContainerCount());
    }catch(Exception ex){
    	GeneralUtils.logError(ex,"dbmolecule constructor/sar",null,false);
    }
    try{
    	setSssr(sssr);
    }catch(Exception ex){
     	GeneralUtils.logError(ex,"dbmolecule constructor/sssr",null,false);
    }
    if (isChiral) {
      setSmilesStringChiral(smilesChiral.getBytes());
    }
    setUserId(userKey);
    setFormula(MolecularFormulaManipulator.getString(mfa));
    int[] mults=new int[4];
    for(int k=0;k<mol.getAtomCount();k++){
      if( mol.getAtom(k).getSymbol().equals("C")){
		if(mol.getAtom(k).getImplicitHydrogenCount()==null)
			mults[0]++;
	   	else if(mol.getAtom(k).getImplicitHydrogenCount()<4)
		    mults[mol.getAtom(k).getImplicitHydrogenCount()]++;
      }
    }
    setMultiplicity0(mults[0]);
    setMultiplicity1(mults[1]);
    setMultiplicity2(mults[2]);
    setMultiplicity3(mults[3]);
  }


  /**
   *  Gets a molecule as a cdk molecule with the user input coordinates, with all Hs.
   *
   * @param  molid          The id of the molecule to return.
   * @return                The CDK Molecule.
   * @exception  Exception  Database problems.
   */
  public static IAtomContainer getAsCDKMoleculeFromId(int molid) throws Exception {
    return (getAsCDKMolecule(molid, 1));
  }


  /**
   *  Gets a molecule with a certain ID a CDK Molecule object, with all Hs.
   *
   * @param  molid            The id of the molecule to be build.
   * @param  coordinateSetId  The coordinate set type id of the coordinate to include.
   * @return                  The CDK Molecule.
   * @exception  TorqueException    Database problems.
   * @throws IOException 
   * @throws OptionalDataException 
 * @throws DataSetException 
   */
  public static IAtomContainer getAsCDKMolecule(int molid, int coordinateSetId) throws TorqueException, OptionalDataException, IOException, DataSetException {
      IAtomContainer cdkMolecule = new org.openscience.cdk.AtomContainer();
    IsotopeFactory isotopeFactory = Isotopes.getInstance();
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    DBAtom[] atoms = getAtomsAsArrayInMdlOrder(molid, map);
    boolean has3d=true;
    List<Record> l=DBCoordinateSet2dPeer.executeQuery("select COORDINATE_SET_3D.* from COORDINATE_SET_3D join ATOM using (ATOM_ID) where MOLECULE_ID="+ molid +" and COORDINATE_SET_TYPE_ID="+coordinateSetId+" order by ATOM_NUMBER, ATOM_ID");
    if(l.size()==0){
    	l=DBCoordinateSet2dPeer.executeQuery("select COORDINATE_SET_2D.* from COORDINATE_SET_2D join ATOM using (ATOM_ID) where MOLECULE_ID="+ molid +" and COORDINATE_SET_TYPE_ID="+coordinateSetId+" order by ATOM_NUMBER, ATOM_ID");
    	has3d=false;
    }
    for (int i = 0; i < atoms.length; i++) {
      IAtom atom = null;
      double x=0;
      double y=0;
      double z=0;
      if(i<l.size()){
	      Record rec=(Record) l.get(i);
	      x = rec.getValue(3).asDouble();
	      y = rec.getValue(4).asDouble();
	      if(has3d)
	    	  z = rec.getValue(5).asDouble();
      }else{
    	  GeneralUtils.sendEmailToEventReceivers("not enough coordinates","There are not coordinates for all atom in molecule "+molid,null,0);
      }
      String element = atoms[i].getSymbol();
      try {
        atom = isotopeFactory.configure(new org.openscience.cdk.Atom(element, new Point2d(x, y)));
      } catch (NullPointerException exception) {
        //atoms[0].log.error("Atom " + element + " is not an regular element. Creating a PseudoAtom.");
        atom = new org.openscience.cdk.PseudoAtom(element, new Point2d(x, y));
      }
      if(has3d) {
        atom.setPoint3d(new Point3d(x, y, z));
      }
      if(atoms[i].getAtomicMass()==0)
    	  atom.setMassNumber(null);
      else
    	  atom.setMassNumber(atoms[i].getAtomicMass());
      atom.setFormalCharge(atoms[i].getFormalCharge());
      if (atoms[i].getIsAromatic().equals(NmrshiftdbConstants.TRUE)) {
        atom.setIsAromatic(true);
      }
      atom.setProperty(SubmittingData.VISIBLE, atoms[i].getIsVisible());
      atom.setID("a"+(i+1));
      cdkMolecule.addAtom(atom);
    }
    l = DBAtomPeer.executeQuery("select BOND_ATOM.ATOM_ID, BOND_ID from ATOM, BOND_ATOM where MOLECULE_ID=" + molid + " and BOND_ATOM.ATOM_ID=ATOM.ATOM_ID order by BOND_ID, BOND_ATOM.ORDER_NUMBER");
    ListIterator<Record> it = l.listIterator();
    List<DBBond> bonds = DBBondPeer.getBonds(molid);
    for (int i = 0; i < bonds.size(); i++) {
      DBBond thisBond = ((DBBond) bonds.get(i));
      int order = thisBond.getDegree();
      int stereo = thisBond.getStereo();
      int mdl1 = -1;
      int mdl2 = -1;
      while (it.hasNext()) {
        Record r = (Record) it.next();
        if (r.getValue(2).asInt()==thisBond.getBondId()) {
          if (mdl1 == -1) {
            mdl1 = r.getValue(1).asInt();
          } else {
            mdl2 = r.getValue(1).asInt();
            break;
          }
        }
      }
      IBond bond = DefaultChemObjectBuilder.getInstance().newInstance(IBond.class, cdkMolecule.getAtom(map.get(new Integer(mdl1)).intValue()), cdkMolecule.getAtom(map.get(new Integer(mdl2)).intValue()), AtomUtils.getOrderFromInt(order));
      try {
          bond = DefaultChemObjectBuilder.getInstance().newInstance(IBond.class, cdkMolecule.getAtom(map.get(new Integer(mdl1)).intValue()), cdkMolecule.getAtom(map.get(new Integer(mdl2)).intValue()), AtomUtils.getOrderFromInt(order), AtomUtils.getStereoFromInt(stereo));
      } catch (Exception e) {
          GeneralUtils.logError(e, e.getMessage(), null, false);
      }
      if (thisBond.getIsAromatic().equals(NmrshiftdbConstants.TRUE)) {
        bond.setIsAromatic(true);
      }
      cdkMolecule.addBond(bond);
    }
    DBMolecule mol = DBMoleculePeer.retrieveByPK(molid);
    cdkMolecule.setProperty(CDKConstants.TITLE, mol.getChemicalNamesAsOneString(false, false, false));
    cdkMolecule.setProperty(CDKConstants.REMARK, "nmrshiftdb2 " + mol.getMoleculeId());
    return (cdkMolecule);
  }


  /**
   *  Gets the atoms of a molecule as Array In Order of mdl numbers.
   *
   * @param  molid          The molecule of which the atoms are wished.
   * @param  map            This map gets filled with (atomId, mdl number) pairs if !=null.
   * @return                The atomsAsArrayInMdlOrder value.
   * @exception  TorqueException  Database problems.
   */
  public static DBAtom[] getAtomsAsArrayInMdlOrder(int molid, HashMap<Integer, Integer> map) throws TorqueException {
    List<DBAtom> v = DBAtomPeer.getAtoms(molid);
    DBAtom[] atoms = new DBAtom[v.size()];
    Iterator<DBAtom> it = v.iterator();
    int i = 0;
    while (it.hasNext()) {
      DBAtom atom = it.next();
      atoms[i] = atom;
      if (map != null) {
        map.put(new Integer(atom.getAtomId()), new Integer(i));
      }
      i++;
    }
    return (atoms);
  }
  
  
  /**
   *  Gets the atoms of a molecule as Array In Order of mdl numbers without the Hs.
   *
   * @param  molid          The molecule of which the atoms are wished.
   * @param  map            This map gets filled with (atomId, mdl number) pairs if !=null.
   * @return                The atomsAsArrayInMdlOrder value.
   * @exception  Exception  Database problems.
   */
  public static DBAtom[] getAtomsAsArrayInMdlOrderWithoutHs(int molid, HashMap<Integer, Integer> map) throws Exception {
    List<DBAtom> v = DBAtomPeer.getAtomsWithoutHs(molid);
    DBAtom[] atoms = new DBAtom[v.size()];
    Iterator<DBAtom> it = v.iterator();
    int i = 0;
    while (it.hasNext()) {
      DBAtom atom = ((DBAtom) it.next());
      atoms[i] = atom;
      if (map != null) {
        map.put(new Integer(atom.getAtomId()), new Integer(i));
      }
      i++;
    }
    return (atoms);
  }
  
  
  /**
   *  Gets the atom with mdl number i.
   *
   * @param  i              atom number to find
   * @return                The atom at i.
   */
  public DBAtom getAtomAt(int i) throws TorqueException{
    return(getAtomsAsArrayInMdlOrder()[i]);
  }


  /**
   * Sets the fingerprint values of the DBMolecule object, taking the molecule from db.
   *
   * @throws IOException 
   * @exception  Exception  Description of Exception,
   */
  public void setFingerprint() throws TorqueException, CDKException, IOException {
	String mdl = getStructureFile(1,false);
	mdl = AtomUtils.replace9by6inmolfile(mdl);
    MDLV2000Reader mdlreader = new MDLV2000Reader(new StringReader(mdl));
    IAtomContainer mol2 = mdlreader.read(new org.openscience.cdk.AtomContainer());
    mdlreader.close();
    int i=0;
    for(IBond bond : mol2.bonds()) {
          if(bond.getOrder()==null || this.getBonds().get(i).getDegree()==0)
              bond.setOrder(IBond.Order.ZERO);
    }
    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol2);
    ElectronDonation model       = ElectronDonation.cdk();
    CycleFinder      cycles      = Cycles.cdkAromaticSet();
    Aromaticity      aromaticity = new Aromaticity(model, cycles);
    aromaticity.apply(mol2);
    setFingerprint(mol2);
  }


  /**
   *  Sets the fingerprint values of the DBMolecule object.
   *
   * @param  mol            The cdk molecule of this molecule
   * @throws CDKException 
   * @exception  Exception  Description of Exception.
   */
  public void setFingerprint(IAtomContainer mol) throws CDKException {
    IAtomContainer molgo = (IAtomContainer)AtomContainerManipulator.removeHydrogens(mol);
    for (int i = 0; i < molgo.getAtomCount(); i++) {
      molgo.getAtom(i).setImplicitHydrogenCount(0);
    }
    try {
	    BitSet bsor = new OrchemFingerprinter().getFingerprint(molgo);
        BitSet bs=new BitSet(OrchemFingerprinter.FINGERPRINT_SIZE+2);
        bs.or(bsor);
        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE, AtomUtils.hasOrganometall(molgo));
        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE+1, AtomUtils.hasZeroBond(molgo));
        StringBuffer fingerprintstring = new StringBuffer();
        fingerprintstring.append("(");	    setFp0(AtomUtils.getBigIntegerValue(bs, 0).longValue());
	    setFp1(AtomUtils.getBigIntegerValue(bs, 1).longValue());
	    setFp2(AtomUtils.getBigIntegerValue(bs, 2).longValue());
	    setFp3(AtomUtils.getBigIntegerValue(bs, 3).longValue());
	    setFp4(AtomUtils.getBigIntegerValue(bs, 4).longValue());
	    setFp5(AtomUtils.getBigIntegerValue(bs, 5).longValue());
	    setFp6(AtomUtils.getBigIntegerValue(bs, 6).longValue());
	    setFp7(AtomUtils.getBigIntegerValue(bs, 7).longValue());
	    setFp8(AtomUtils.getBigIntegerValue(bs, 8).longValue());
	    setFp9(AtomUtils.getBigIntegerValue(bs, 9).longValue());
	    setFp10(AtomUtils.getBigIntegerValue(bs, 10).longValue());
	    setFp11(AtomUtils.getBigIntegerValue(bs, 11).longValue());
	    setFp12(AtomUtils.getBigIntegerValue(bs, 12).longValue());
	    setFp13(AtomUtils.getBigIntegerValue(bs, 13).longValue());
	    setFp14(AtomUtils.getBigIntegerValue(bs, 14).longValue());
	    setFp15(AtomUtils.getBigIntegerValue(bs, 15).longValue());
    }catch(IllegalArgumentException ex) {
    	//this happes in particular with small molecules sometimes, we ignore it 
    }
  }


  /**
   *  Gets the autonomeName attribute of the DBMolecule object
   *
   * @return                The autonomeName value
   * @exception  Exception  Description of Exception
   */
  public String getAutonomeName() throws Exception {
    if (autonomeName == null) {
      Criteria crit = new Criteria();
      crit.add(DBCanonicalNamePeer.MOLECULE_ID, getMoleculeId());
      crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID, 1);
      List<DBCanonicalName> v = DBCanonicalNamePeer.doSelect(crit);
      if (v.size() > 0) {
        autonomeName = ((DBCanonicalName) v.get(0)).getName();
      } else {
        autonomeName = "";
      }
    }
    return (autonomeName);
  }


  /**
   *Gets the number of non-H atoms of the DBMolecule object
   *
   * @return                The numberOfNonHAtoms value
   * @exception  Exception  Something wrong with reading database
   */
  public int getNumberOfNonHAtoms() throws Exception {
    List<DBAtom> v = this.getDBAtoms();
    int k = 0;
    for (int i = 0; i < v.size(); i++) {
      if (!((DBAtom) v.get(i)).getSymbol().equals("H")) {
        k++;
      }
    }
    return (k);
  }


  /**
   *Gets the links of the DBMolecule object
   *
   * @return                The links
   * @exception  TorqueException  Database problems
   */
  public List<DBMoleculeHyperlink> getLinks() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBMoleculeHyperlinkPeer.MOLECULE_ID, this.getMoleculeId());
    return (DBMoleculeHyperlinkPeer.doSelect(crit));
  }


  /**
   *Gets the chemical names of the DBMolecule object as one string with ; seperation
   *
   * @param  escape do any escape?
   * @param  escapeForHtml  true=make html code, false=esacpe for sql.
   * @return                The chemical names
   * @exception  TorqueException  Something wrong with reading database
   */
  public String getChemicalNamesAsOneString(boolean escape, boolean escapeForHtml) throws TorqueException {
    return (getChemicalNamesAsOneString(escape, escapeForHtml, false));
  }


  /**
   *Gets the chemical names of the DBMolecule object as one string with either ; or \n\r seperation.
   *
   * @param  escape do any escape?
   * @param  escapeForHtml  true=make html code, false=esacpe for sql.
   * @param  cr             true=use \n\r, false=use ;.
   * @return                The chemical names.
   * @exception  TorqueException  Something wrong with reading database.
   */
  public String getChemicalNamesAsOneString(boolean escape, boolean escapeForHtml, boolean cr) throws TorqueException {
    List<DBChemicalName> v = this.getDBChemicalNames();
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      if (cr) {
        s.append(((DBChemicalName) v.get(i)).getName() + "\n\r");
      } else {
        s.append(((DBChemicalName) v.get(i)).getName() + "; ");
      }
    }
    GeneralUtils.removeLastComma(s);
    if (!escape)
      return s.toString();
    if (escapeForHtml) {
      return (GeneralUtils.escapeForHtml(s.toString(), false));
    } else {
      return (GeneralUtils.escape(s.toString(),false));
    }
  }


  /**
   *Gets the chemical names of the DBMolecule object as one string with ; seperation and fallback to canonicalname/smiles.
   *
   * @return                The chemicalNamesAsOneStringWithFallback value
   * @exception  TorqueException  Something wrong with reading database
   */
  public String getChemicalNamesAsOneStringWithFallback() throws TorqueException {
    String name = getChemicalNamesAsOneString(true, true, false);
    if (name.equals("")) {
      name = getCanonicalNameNormal();
    }
    return (name);
  }
  
  public String getChemicalNamesAsOneStringWithFallbackEasy() throws TorqueException {
    String name = getChemicalNamesAsOneString(true, true, false);
    if (name.equals("") && this.getDBCanonicalNames().size()>0) {
      name = getDBCanonicalNames().get(0).getName();
    }
    return (name);
  }


  /**
   *  Gets the chemical names of this molecule shortened. If 
   *  more than 27 characters long, they are cut off and become ...
   *
   * @return                The shortened chemical names.
   * @exception  TorqueException  Database problems.
   */
  public String getChemicalNamesMaxChars() throws TorqueException{
    String inscription = getChemicalNamesAsOneString(true,true);
    if (inscription.length() > 27) {
      inscription = inscription.substring(0, 23) + "...";
    }
    return (inscription);
  }



  /**
   *  Gets the description of the links of the DBMolecule object as one string with ; seperation
   *
   * @return                The moleculeLinksAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getMoleculeLinksAsOneString() throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBMoleculeHyperlinkPeer.MOLECULE_ID, this.getMoleculeId());
    List<DBMoleculeHyperlink> v = DBMoleculeHyperlinkPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      s.append(((DBMoleculeHyperlink) v.get(i)).getDescription() + "; ");
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *  Gets the keywords of the DBMolecule object as one string with ; seperation
   *
   * @return                The moleculeKeywordsAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getMoleculeKeywordsAsOneString() throws Exception {
    return (getMoleculeKeywordsAsOneString("; "));
  }


  /**
   *  Gets the keywords of the DBMolecule object as one string with a given seperation
   *
   * @param  separator      The separation character
   * @return                The moleculeKeywordsAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getMoleculeKeywordsAsOneString(String separator) throws TorqueException {
    Criteria crit = new Criteria();
    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBMoleculeDBKeywordPeer.MOLECULE_ID);
    crit.addJoin(DBMoleculeDBKeywordPeer.KEYWORD_ID, DBKeywordPeer.KEYWORD_ID);
    crit.add(DBMoleculePeer.MOLECULE_ID, this.getMoleculeId());
    List<DBKeyword> v = DBKeywordPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      s.append(((DBKeyword) v.get(i)).getKeyword() + separator);
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *  Gets the keywords of all spectra of the DBMolecule object as one string with ; seperation
   *
   * @return                The spectrumKeywordsAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getSpectrumKeywordsAsOneString() throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBKeywordPeer.SPECTRUM_ID);
    crit.addJoin(DBSpectrumDBKeywordPeer.KEYWORD_ID, DBKeywordPeer.KEYWORD_ID);
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
    crit.add(DBMoleculePeer.MOLECULE_ID, this.getMoleculeId());
    List<DBKeyword> v = DBKeywordPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      s.append(((DBKeyword) v.get(i)).getKeyword() + "; ");
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *  Gets the literature attributes of all spectra of the DBMolecule object as one string with ; seperation
   *
   * @return                The literatureAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getLiteratureAsOneString() throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
    List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      List<DBLiterature> v2 = ((DBSpectrum) v.get(i)).getLiteraturesNew();
      for (int k = 0; k < v2.size(); k++) {
        s.append(((DBLiterature) v2.get(k)).toString() + ";");
      }
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *  Gets the comments of all spectra of this molecule as one string with ; seperation
   *
   * @return                The commentAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getCommentAsOneString() throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
    List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      if (((DBSpectrum) v.get(i)).getComment().length != 0) {
        s.append(new String(((DBSpectrum) v.get(i)).getComment()) + "; ");
      }
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *  Gets the links of all spectra of the DBMolecule object as one string with ; seperation
   *
   * @return                The spectrumLinksAsOneString value
   * @exception  Exception  Something wrong with reading database
   */
  public String getSpectrumLinksAsOneString() throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumHyperlinkPeer.SPECTRUM_ID);
    crit.add(DBMoleculePeer.MOLECULE_ID, this.getMoleculeId());
    List<DBSpectrumHyperlink> v = DBSpectrumHyperlinkPeer.doSelect(crit);
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      s.append(((DBSpectrumHyperlink) v.get(i)).getDescription() + "; ");
    }
    GeneralUtils.removeLastComma(s);
    return (s.toString());
  }


  /**
   *Gets the chemical names of the DBMolecule object as one string mit /n/r seperation
   *
   * @return                The chemical names
   * @exception  TorqueException  Something wrong with reading database
   */
  public String getChemicalNamesAsOneStringWithCR() throws TorqueException {
    return (getChemicalNamesAsOneString(true, true, true));
  }


  /**
   *Gets the links+their descriptoin of the DBMolecule object as one string with one links+description in one line
   *
   * @return                The links value
   * @exception  TorqueException  Something wrong with reading database
   */
  public String getLinksAsString() throws TorqueException {
    StringBuffer links = new StringBuffer();
    List<DBMoleculeHyperlink> v = getLinks();
    for (int i = 0; i < v.size(); i++) {
      links.append(((DBMoleculeHyperlink) v.get(i)).getHyperlinkSimple() + ";" + ((DBMoleculeHyperlink) v.get(i)).getDescription() + "\n");
    }
    return (links.toString());
  }

  
  /**
   *Gets the canonicalName of the DBMolecule object as a string
   *
   * @return                The canonicalName value
   * @exception  TorqueException  Description of Exception
   */
  public String getCanonicalNameNormal() throws TorqueException {
    StringBuffer name = new StringBuffer("");
    List<DBCanonicalName> v = getDBCanonicalNames();
    for (int i = 0; i < v.size(); i++) {
      name.append(((DBCanonicalName) v.get(i)).getName() + " (" + ((DBCanonicalName) v.get(i)).getDBCanonicalNameType().getCanonicalNameType() + "), ");
    }
    name.append(getSmilesStringStr() + " (SMILES), ");
    if (getSmilesStringChiral() != null) {
      name.append(getSmilesStringChiralStr() + " (chiral SMILES), ");
    }
    return (GeneralUtils.removeLastComma(name).toString());
  }
  
  
  /**
   *Gets the canonicalName of the DBMolecule object as a string, the names shortened and with a link to complete name.
   *
   * @return                The canonicalName value
   * @exception  Exception  Description of Exception
   */
  public String getCanonicalName() throws Exception {
    StringBuffer name = new StringBuffer("");
    List<DBCanonicalName> v = getDBCanonicalNames();
    for (int i = 0; i < v.size(); i++) {
      name.append(GeneralUtils.getShort(((DBCanonicalName) v.get(i)).getName(), 30) + " (" + ((DBCanonicalName) v.get(i)).getDBCanonicalNameType().getCanonicalNameType() + "), ");
    }
    name.append(GeneralUtils.getShort(getSmilesStringStr(), 30) + " (SMILES), ");
    if (getSmilesStringChiral() != null) {
      name.append(GeneralUtils.getShort(getSmilesStringChiralStr(), 30) + " (chiral SMILES), ");
    }
    return (GeneralUtils.removeLastComma(name).toString());
  }


  /**
   *  Gets all DBBond objects connected with this DBMolecule
   *
   * @return                The bonds value
   * @exception  Exception  Something wrong with reading database
   */
  public List<DBBond> getBonds() throws TorqueException {
    return (DBBondPeer.getBonds(getMoleculeId()));
  }


  /**
   *  Gets the inscription of the result field if a user wants to see his spectra
   *
   * @param  user           The user logged in
   * @return                The mySpectraInscription value
   * @exception  Exception  Database problems
   */
  public String getMySpectraInscription(User user) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
    crit.addJoin(DBSpectrumPeer.USER_ID, NmrshiftdbUserPeer.USER_ID);
    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, user.getUserName());
    List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
    int rejectedS = 0;
    int trueS = 0;
    int falseS = 0;
    int editedS = 0;
    int privateS = 0;
    for (int i = 0; i < v.size(); i++) {
      String flag = ((DBSpectrum) v.get(i)).getReviewFlag();
      if (flag.equals(NmrshiftdbConstants.TRUE)) {
        trueS++;
      }
      if (flag.equals(NmrshiftdbConstants.FALSE)) {
        falseS++;
      }
      if (flag.equals(NmrshiftdbConstants.REJECTED)) {
        rejectedS++;
      }
      if (flag.equals(NmrshiftdbConstants.EDITED)) {
        editedS++;
      }
      if (flag.equals(NmrshiftdbConstants.PRIVATE)) {
          privateS++;
      }
    }
    return ("OK:" + trueS + ", U:" + falseS + ", R:" + rejectedS + ", E:" + editedS + ", P:" + privateS + " *");
  }


  /**
   *  Gets this molecule as a CDK Molecule object, with all Hs.
   *
   * @param  setid          The coordinate set id of coordinate set to use..
   * @return                The CDK Molecule.
   * @exception  TorqueException  Database problems.
   * @throws IOException 
   * @throws OptionalDataException 
   * @throws DataSetException 
   */
  public IAtomContainer getAsCDKMolecule(int setid) throws TorqueException, OptionalDataException, IOException, DataSetException {
    if (cdkMolecule == null || setid != this.setid) {
      this.setid = setid;
      cdkMolecule = getAsCDKMolecule(getMoleculeId(), setid);
      for(IAtom atom : cdkMolecule.atoms()){
          atom.setImplicitHydrogenCount(0);
      }
    }
    return (cdkMolecule);
  }
  
  
  /**
   *  Gets this molecule as a CDK Molecule object, with 3d coordinates, if any, or null.
   *
   * @return                The CDK Molecule with 3d coords or null.
   * @exception  TorqueException  Database problems.
   * @throws DataSetException 
   * @throws IOException 
   * @throws OptionalDataException 
   */
  public IAtomContainer getAsCDKMoleculeWithAny3d() throws TorqueException, OptionalDataException, IOException, DataSetException{
    DBAtom firstatom=getAtomsAsArrayInMdlOrder()[0];
    List<DBCoordinateSet3d> v=firstatom.getDBCoordinateSet3ds();
    if(v.size()==0){
      return null;
    }else{
      IAtomContainer localmol=getAsCDKMolecule(((DBCoordinateSet3d)v.get(0)).getCoordinateSetTypeId());
      for(int i=0;i<localmol.getAtomCount();i++){
    	DBCoordinateSet2d set2d=DBCoordinateSet2dPeer.retrieveByPK(1, this.getAtomsAsArrayInMdlOrder()[i].getAtomId());
    	localmol.getAtom(i).setPoint2d(new Point2d(set2d.getXCoordinate(),set2d.getYCoordinate()));    	  
      }
      return localmol;
    }
  }

  
  /**
   *  Gets this molecule as an MDL mol file, with Hs entered by user as explicit Hs, with no sdfields.
   *
   * @param  setid          The coordinate set id of coordinate set to use.
   * @param  fallbak        true=use coord set 1 if coord setid do not exist, false=fail.
   * @return                The mol file.
   * @throws IOException 
   * @exception  Exception  Database problems.
   */
  public String getStructureFile(int setid, boolean fallback) throws TorqueException, IOException {
    return(getStructureFile(setid,null, fallback));
  }
  
  
  /**
   *  Gets this molecule as an MDL mol file, with Hs entered by user as explicit Hs, without hcount.
   *
   * @param  setid          The coordinate set id of coordinate set to use.
   * @param  map            Data to put in the file as sd fields.
   * @param  fallbak        true=use coord set 1 if coord setid do not exist, false=fail.
   * @return                The mol file.
   * @throws IOException 
   * @exception  Exception  Database problems.
   */
  public String getStructureFile(int setid, Map<String,String> map, boolean fallback) throws TorqueException, IOException {
	  return(getStructureFile(setid,null, fallback, false));
  }
  
  
  /**
   *  Gets this molecule as an MDL mol file, with Hs entered by user as explicit Hs, without hcount.
   *
   * @param  setid          The coordinate set id of coordinate set to use.
   * @param  fallbak        true=use coord set 1 if coord setid do not exist, false=fail.
   * @return                The mol file.
   * @throws IOException 
   * @exception  Exception  Database problems.
   */
  public String getStructureFileWithHcount(int setid, boolean fallback) throws TorqueException, IOException {
	  return(getStructureFile(setid,null, fallback, false));
  }
  
  /**
   *  Gets this molecule as an MDL mol file, with Hs entered by user as explicit Hs.
   *
   * @param  setid          The coordinate set id of coordinate set to use.
   * @param  map            Data to put in the file as sd fields.
   * @param  fallbak        true=use coord set 1 if coord setid do not exist, false=fail.
   * @return                The mol file.
   * @throws IOException 
   * @exception  Exception  Database problems.
   */
  public String getStructureFile(int setid, Map<String,String> map, boolean fallback, boolean hcount) throws TorqueException, IOException {
      MDLV2000Writer mdlwriter=null;
	  try{
        if(fallback && !hasCoordinates(setid))
          setid=1;
        StringWriter sw = new StringWriter();
        IAtomContainer molold=getAsCDKMoleculeAsEntered(setid,hcount);
        IQueryAtomContainer mol= new QueryAtomContainer(molold.getBuilder());
        int i=0;
        for(IBond bond : molold.bonds()) {
              if(bond.getOrder()==null || bond.getOrder()==IBond.Order.ZERO) {
                  IQueryBond newbond=new QueryBond(bond.getAtom(0),bond.getAtom(1),Expr.Type.SINGLE_OR_AROMATIC);
                  mol.addBond(newbond);
              }else {
            	  mol.addBond(bond);
              }
              i++;
        }
        for(IAtom atom : molold.atoms()) {
        	mol.addAtom(atom);
        }
        mdlwriter=new MDLV2000Writer(sw);
        if(map!=null){
            for(String key : map.keySet()){
                mol.setProperty(key, map.get(key));
            }
        }
        mol.setProperty(CDKConstants.TITLE, this.getChemicalNamesAsOneString(false, false, false));
        mol.setProperty(CDKConstants.REMARK, "nmrshiftdb2 " + this.getMoleculeId());
        mdlwriter.writeMolecule(mol);
        return (sw.toString());
	  }catch(Exception ex){
		  throw new TorqueException(ex);
	  }
	  finally{
		  if(mdlwriter!=null)
			  mdlwriter.close();
	  }
  }
  
  
  /**
   *  Tells if a coordinateset with setid exists.
   *
   * @param  setid          The coordinate set id of coordinate set to look for.
   * @exception  Exception  Database problems.
   */
  public boolean hasCoordinates(int setid) throws TorqueException{
    Criteria crit=new Criteria();
    crit.add(DBCoordinateSet2dPeer.ATOM_ID,((DBAtom)getDBAtoms().get(0)).getAtomId());
    crit.add(DBCoordinateSet2dPeer.COORDINATE_SET_TYPE_ID,setid);
    List<DBCoordinateSet2d> v=DBCoordinateSet2dPeer.doSelect(crit);
    if(v.size()>0)
      return true;
    crit=new Criteria();
    crit.add(DBCoordinateSet3dPeer.ATOM_ID,((DBAtom)getDBAtoms().get(0)).getAtomId());
    crit.add(DBCoordinateSet3dPeer.COORDINATE_SET_TYPE_ID,setid);
    List<DBCoordinateSet3d> v2=DBCoordinateSet3dPeer.doSelect(crit);
    if(v2.size()>0)
      return true;
    return false;
  }


  /**
   *  Gets this molecule as CML.
   *
   * @param setid              The coordinate set to use
   * @param withh              Shall implicit hs be reported?
   * @return                   The cml.
 * @throws DataSetException 
 * @throws IOException 
 * @throws TorqueException 
 * @throws OptionalDataException 
   */
  public Element getCML(int setid, boolean withh) throws OptionalDataException, TorqueException, IOException, DataSetException {
    CMLCml cml=new CMLCml();
    IAtomContainer ac;
    if(withh){
    	ac=this.getAsCDKMolecule(setid);
        Iterator<IAtom> it=ac.atoms().iterator();
        while(it.hasNext()){
        	IAtom atom=it.next();
           	atom.setImplicitHydrogenCount(0);
        }    	
    }else{
    	ac=getAsCDKMoleculeAsEntered(setid,true);
    }
    cml.appendChild(new Convertor(true,null).cdkAtomContainerToCMLMolecule(ac));
    ((Element) cml.getChildCMLElements("molecule").get(0)).addAttribute(new Attribute("id", "nmrshiftdb"+getMoleculeId()));
    return (cml);
  }


  /**
   *  Gets this molecule as CML, with Hs as entered.
   *
   * @return                   The cml.
 * @throws DataSetException 
 * @throws IOException 
 * @throws TorqueException 
 * @throws OptionalDataException 
   * @exception  Exception     Database problems.
   * @exception  CMLException  Description of Exception
   */
  public Element getCML(int setid) throws OptionalDataException, TorqueException, IOException, DataSetException {
	  return getCML(setid,false);
  }

  
  /**
   *  Gets this molecule as CML as a string.
   *
   * @return                                        The cml.
   * @exception  Exception                          Database problems.
   * @exception  TransformerConfigurationException  Description of Exception
   * @exception  TransformerException               Description of Exception
   * @exception  CMLException                       Description of Exception
   */
  public String getCMLString(int setid) throws TransformerConfigurationException, TransformerException, Exception {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Serializer ser = new Serializer(out, "ISO-8859-1");
      ser.setIndent(1);
      ser.write(new Document(getCML(setid)));
      return new String(out.toString().getBytes());
  }


  /**
   *  Gets this molecule as a CDK Molecule object without the Hs and h counts.
   *
   * @return                             The asCDKMolecule value
   * @exception  IOException             Description of Exception
   * @exception  ClassNotFoundException  Description of Exception
   * @exception  Exception               Description of Exception
   */
  public IAtomContainer getAsCDKMoleculeWithoutHAndCount() throws IOException, ClassNotFoundException, Exception {
    if (cdkMoleculeWithoutHsAndCount == null) {
      cdkMoleculeWithoutHsAndCount = getAsCDKMolecule(getMoleculeId());
      cdkMoleculeWithoutHsAndCount = AtomUtils.removeHydrogens(cdkMoleculeWithoutHsAndCount);
      for (int i = 0; i < cdkMoleculeWithoutHsAndCount.getAtomCount(); i++) {
          cdkMoleculeWithoutHsAndCount.getAtom(i).setImplicitHydrogenCount(0);
      }
    }
    return (cdkMoleculeWithoutHsAndCount);
  }


  /**
   *  Gets this molecule as a CDK Molecule object without the Hs
   *
   * @return                             The asCDKMolecule value
   * @exception  IOException             Description of Exception
   * @exception  ClassNotFoundException  Description of Exception
   * @exception  Exception               Description of Exception
   */
  public IAtomContainer getAsCDKMoleculeWithoutH() throws IOException, ClassNotFoundException, Exception {
    if (cdkMoleculeWithoutHs == null) {
      cdkMoleculeWithoutHs = getAsCDKMolecule(1).clone();
      for(IAtom atom : cdkMoleculeWithoutHs.atoms()){
          atom.setImplicitHydrogenCount(0);
      }
      cdkMoleculeWithoutHs = AtomUtils.removeHydrogens(cdkMoleculeWithoutHs);
    }
    return (cdkMoleculeWithoutHs);
  }


  /**
   *  Gets the conditions of a certain type of the spectra of this molecule.
   *
   * @param  conditionName  The condition type the conditions should be of.
   * @return                A string with all conditions.
   * @exception  Exception  Database problems.
   */
  public String getCondition(String conditionName) throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBConditionPeer.SPECTRUM_ID);
    crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
    crit.addJoin(DBConditionPeer.CONDITION_TYPE_ID, DBConditionTypePeer.CONDITION_TYPE_ID);
    crit.add(DBConditionTypePeer.CONDITION_NAME, conditionName);
    crit.add(DBSpectrumPeer.MOLECULE_ID, getMoleculeId());
    List<DBCondition> v = DBConditionPeer.doSelect(crit);
    StringBuffer returnValue = new StringBuffer();
    for (int i = 0; i < v.size(); i++) {
      returnValue.append(((DBCondition) v.get(i)).getValue() + ";");
    }
    return (conditionName + ": " + returnValue.toString());
  }


  /**
   *  Gets the chemicalNames or, if empty, the id attribute of the DBMolecule object.
   *
   * @return                The chemicalNames or id value.
   * @exception  Exception  Description of Exception
   */
  public String getChemicalNamesOrId() throws Exception {
    if (getChemicalNamesAsOneString(false, false).equals("")) {
      return (getMoleculeId()+"");
    } else {
      return (getChemicalNamesAsOneString(true, true));
    }
  }

  /**
   *  Gets the atoms of this molecule as Array In Order of mdl numbers.
   *
   * @return                The atomsAsArrayInMdlOrder value.
   * @exception  TorqueException  Database problems.
   */
  public DBAtom[] getAtomsAsArrayInMdlOrder() throws TorqueException {
    if (dbatoms == null) {
      dbatoms = getAtomsAsArrayInMdlOrder(this.getMoleculeId(), null);
    }
    return (dbatoms);
  }


  /**
   *  Gets the atoms of this molecule as Array In Order of mdl numbers without Hs.
   *
   * @return                The atomsAsArrayInMdlOrder value.
   * @exception  Exception  Database problems.
   */
  public DBAtom[] getAtomsAsArrayInMdlOrderWithoutHs() throws Exception {
    if (dbatomsWithoutHs == null) {
    	dbatomsWithoutHs = getAtomsAsArrayInMdlOrderWithoutHs(this.getMoleculeId(), null);
    }
    return (dbatomsWithoutHs);
  }
  
  
  /**
   *  Gets the molecular formula of the DBMolecule object.
   *
   * @param  withFormat     true=formula gets formatted with html, false=plain text.
   * @return                The molecular formula value.
   * @exception  Exception  Description of Exception
   */
  public String getMolecularFormula(boolean withFormat) throws TorqueException {
    List<DBAtom> atoms = getDBAtoms();
    String symbol;
    String mf = "";
    SortedMap<String, Integer> symbols = new TreeMap<String,Integer>();
    DBAtom atom = null;
    for (int f = 0; f < atoms.size(); f++) {
      atom = (DBAtom) atoms.get(f);
      symbol = atom.getSymbol();
      if (symbols.get(symbol) != null) {
        symbols.put(symbol, new Integer(symbols.get(symbol).intValue() + 1));
      } else {
        symbols.put(symbol, new Integer(1));
      }
    }
    mf = GeneralUtils.addSymbolToFormula(symbols, "C", mf, withFormat);
    mf = GeneralUtils.addSymbolToFormula(symbols, "H", mf, withFormat);
    mf = GeneralUtils.addSymbolToFormula(symbols, "N", mf, withFormat);
    mf = GeneralUtils.addSymbolToFormula(symbols, "O", mf, withFormat);
    mf = GeneralUtils.addSymbolToFormula(symbols, "S", mf, withFormat);
    mf = GeneralUtils.addSymbolToFormula(symbols, "P", mf, withFormat);
    Iterator<String> it = symbols.keySet().iterator();
    while (it.hasNext()) {
      Object key = it.next();
      if (!((String) key).equals("C") && !((String) key).equals("H") && !((String) key).equals("N") && !((String) key).equals("O") && !((String) key).equals("S") && !((String) key).equals("P")) {
        mf = GeneralUtils.addSymbolToFormula(symbols, (String) key, mf, withFormat);
      }
    }
    return mf;
  }


  /**
   *  Gets the filename attribute of the DBMolecule object
   *
   * @return    The filename value
   */
  public String getFilename() {
    return (filename);
  }


  /**
   *  Gets the id of an unreviewed spectrum of this molecule assigned to a certain user.
   *
   * @param  user           The user the spectrum should be assigned to.
   * @return                The id of an unreviewed spectrum.
   * @exception  Exception  Database problems.
   */
  public int getIdOfUnreviewedSpectrum(NmrshiftdbUser user) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.FALSE);
    crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBUserDBSpectrumPeer.SPECTRUM_ID);
    crit.add(DBUserDBSpectrumPeer.USER_ID, user.getUserId());
    List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
    return (((DBSpectrum) v.get(0)).getSpectrumId());
  }



  private void appendSmallImageURL(StringBuffer link, String sessionid, int molid) throws IOException {
	  link.append("<img src=\"");
      if(UrlTool.doImageCache()){
    	  link.append("images/molecules/small/"+molid+".jpeg");
      }else {
    	  link.append("download/NmrshiftdbServlet/" + molid + ".jpg;jsessionid="+sessionid+"?nmrshiftdbaction=exportmol&moleculeid=" + molid + "&format=jpeg&size=small&shownumbers=false&backcolor=14737632");
      }
      link.append("\">");
  }

  /**
   *  Builds a link to isomers of this molecule.
   *
   * @param  lastaction     The current action parameter.
   * @param  molnumber      The current molnumber parameter, used for display of resultsPortlet.
   * @param  startmolecule  The current startmolecule parameter, used for display of resultsPortlet.
   * @return                The isomerLinks value.
   * @throws TorqueException 
   * @throws DataSetException 
   * @exception  Exception  Database problems.
   */
  public String getIsomerLinks(String lastaction, String molnumber, String startmolecule, String sessionid) throws TorqueException, DataSetException, IOException {
      String inchi=this.getInchi();
      StringBuffer link = new StringBuffer();
      if(StringUtils.countMatches(inchi, "/")>3){
          StringTokenizer st=new StringTokenizer(inchi,"/");
          StringBuffer sb=new StringBuffer();
          for(int i=0;i<4;i++){
              sb.append(st.nextToken()).append("/");
          }
          String inchinonchiral=sb.substring(0,sb.length()-1);
          String sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME = '"+inchinonchiral+"'";
          List<Record> l = DBMoleculePeer.executeQuery(sql);
          if(l.size()>0){
              link.append("This molecule without stereoconfiguration:<br><a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((Record) l.get(0)).getValue(1).asInt() + "\">");
              appendSmallImageURL(link,sessionid, ((Record) l.get(0)).getValue(1).asInt());
              link.append("</a> ");
          }
          sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME like '"+inchinonchiral+"%' and CANONICAL_NAME.NAME not like '"+inchi+"%' and CANONICAL_NAME.NAME != '"+inchinonchiral+"'";
          l = DBMoleculePeer.executeQuery(sql);
          if (l.size() > 0) {
              link.append("<br>This molecule with other stereoconfigurations:<br>");
              for (int i = 0; i < l.size(); i++) {
                link.append("<a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((Record) l.get(i)).getValue(1).asInt() + "\">");
                appendSmallImageURL(link,sessionid, ((Record) l.get(0)).getValue(1).asInt());
                link.append("</a> ");
              }
          }
      }else{
          String sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME like '"+inchi+"%' and CANONICAL_NAME.NAME != '"+inchi+"'";
          List<Record> l = DBMoleculePeer.executeQuery(sql);
          if (l.size() > 0) {
              link.append("<br>This molecule with some stereoconfiguration:<br>");
              for (int i = 0; i < l.size(); i++) {
                link.append("<a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((Record) l.get(i)).getValue(1).asInt() + "\">");
                appendSmallImageURL(link,sessionid, ((Record) l.get(0)).getValue(1).asInt());
                link.append("</a> ");
              }
          }
      }    
      return link.toString();
      /*try {
      StringBuffer link = new StringBuffer();
      if (getSmilesStringChiral() != null) {
        Criteria crit = new Criteria();
        crit.add(DBMoleculePeer.SMILES_STRING, getSmilesStringStr());
        crit.add(DBMoleculePeer.SMILES_STRING_CHIRAL, null);
        crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
        crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
        crit.setDistinct();
        List v = DBMoleculePeer.doSelect(crit);
        if (v.size() > 0) {
          link.append("This molecule without stereoconfiguration:<br><a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((DBMolecule) v.get(0)).getMoleculeId() + "\"><img src=\"download/NmrshiftdbServlet/" + ((DBMolecule) v.get(0)).getMoleculeId() + ".jpg;jsessionid="+sessionid+"?nmrshiftdbaction=exportmol&moleculeid=" + ((DBMolecule) v.get(0)).getMoleculeId() + "&format=jpeg&size=100x100&shownumbers=false&backcolor=14737632\"></a> ");
        }
      }
      Criteria crit = new Criteria();
      crit.add(DBMoleculePeer.SMILES_STRING, getSmilesStringStr());
      crit.add(DBMoleculePeer.SMILES_STRING_CHIRAL, (Object) getSmilesStringChiralStr(), Criteria.NOT_EQUAL);
      crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
      crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
      crit.setDistinct();
      List v = DBMoleculePeer.doSelect(crit);
      if (v.size() > 0) {
    	link.append("<br>This molecule with other stereoconfigurations:<br>");
        for (int i = 0; i < v.size(); i++) {
          link.append("<a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((DBMolecule) v.get(i)).getMoleculeId() + "\"><img src=\"download/NmrshiftdbServlet/" + ((DBMolecule) v.get(i)).getMoleculeId() + ".jpg;jsessionid="+sessionid+"?nmrshiftdbaction=exportmol&moleculeid=" + ((DBMolecule) v.get(i)).getMoleculeId() + "&format=jpeg&size=100x100&shownumbers=false&backcolor=14737632\"></a> ");
        }
      }
      return (link.toString());
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "isomer links", null, true);
    }
    return ("error");*/
  }



  /**
   *  Builds a link to tautomers of this molecule.
   *
   * @param  lastaction     The current action parameter.
   * @param  molnumber      The current molnumber parameter, used for display of resultsPortlet.
   * @param  startmolecule  The current startmolecule parameter, used for display of resultsPortlet.
   * @return                The isomerLinks value.
   * @exception  Exception  Database problems.
   */
  public String getTautomerLinks(String lastaction, String molnumber, String startmolecule, String sessionid) throws Exception {
    try {
      StringBuffer link = new StringBuffer();
      Criteria crit = new Criteria();
      crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID, 4);
      crit.add(DBCanonicalNamePeer.NAME,this.getInchi());
      crit.add(DBCanonicalNamePeer.MOLECULE_ID, this.getMoleculeId(), Criteria.NOT_EQUAL);
      List<DBCanonicalName> v = DBCanonicalNamePeer.doSelect(crit);
      if (v.size() > 0) {
          for(int i=0;i<v.size();i++){
        	  DBMolecule potentialTautomer=((DBCanonicalName)v.get(i)).getDBMolecule();
        	  //We only want those where the inchi with h layer is different (test for != null is for unlikely event of no inchi with h layer)
        	  if(potentialTautomer.getInchiWithHLayer()!=null && this.getInchiWithHLayer()!=null && !potentialTautomer.getInchiWithHLayer().equals(this.getInchiWithHLayer())){
        		  if(link.toString().equals(""))
        			  link.append("Other tautomers of this molecule:<br>");
        		  link.append("<a href=\"portal/js_pane/P-Results;jsessionid="+sessionid+"?nmrshiftdbaction=" + lastaction + "&molNumber=" + molnumber + "&startMolecule=" + startmolecule + "&stereolink=stereolink&stereonumber=" + ((DBCanonicalName) v.get(0)).getMoleculeId() + "\">");
        		  appendSmallImageURL(link,sessionid, ((DBCanonicalName) v.get(0)).getMoleculeId());
        		  link.append("</a> ");
        	  }
          }
      }
      return (link.toString());
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "isomer links", null, true);
    }
    return ("error");
  }

  
  /**
   * Gets the inchi with fixed h layer (from db, not generatad) for this molecule.
   * 
   * @return The inchi with fixed h layer.
   * @throws TorqueException Database problems.
   */  
  public String getInchiWithHLayer() throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBCanonicalNamePeer.MOLECULE_ID,this.getMoleculeId());
		crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID,6);
		List<DBCanonicalName> l=DBCanonicalNamePeer.doSelect(crit);
		if(l.size()>0)
			return ((DBCanonicalName)l.get(0)).getName();
		else
			return null;
  }


  /**
   *  Gets the smilesString attribute of the DBMolecule object as a string.
   *
   * @return    The smilesString value
   */
  public String getSmilesStringStr() {
    if (getSmilesString() == null) {
      return (null);
    } else {
      return (new String(super.getSmilesString()));
    }
  }


  /**
   *  Gets the smilesStringChiral attribute of the DBMolecule object as a string.
   *
   * @return    The smilesStringChiral value.
   */
  public String getSmilesStringChiralStr() {
    if (getSmilesStringChiral() == null) {
      return (null);
    } else {
      return (new String(super.getSmilesStringChiral()));
    }
  }


  /**
   *  Tells if this molecule contains any valid chirality (i. e. if the chiral smiles is different from normal smiles)
   *
   * @return    true/false
   */
  public boolean isChiral() {
    if (getSmilesStringChiral() == null) {
      return (false);
    } else {
      return (true);
    }
  }


  /**
   *  Gets a link for imediate display of this molecule.
   *
   * @param  req            The current requst.
   * @return                The easylink value.
   */
  public String getEasylink(HttpServletRequest req){
    return ("molecule/" + getMoleculeId());
  }


  /**
   *  Gets an atom of this molecule with a HOSE code starting with hc.
   *
   * @param  hc             The HOSE code start wished.
   * @return                An atom having hc.
   * @exception  Exception  Description of Exception.
   */
  public DBAtom getAtomByHoseCode(String hc) throws Exception {
    String sql = "select ATOM_ID from ATOM where MOLECULE_ID=" + this.getMoleculeId() + " and (HOSE_CODE like '" + GeneralUtils.escape(hc,true) + "%' or HOSE_CODE_WITH_RINGS like '" + GeneralUtils.escape(hc,true) + "%');";
    List<Record> l = DBAtomPeer.executeQuery(sql);
    if (l.size() == 0) {
      throw new NmrshiftdbException("No atom with HOSE code " + hc + " for Molecule " + getMoleculeId());
    }
    return (DBAtomPeer.retrieveByPK(((Record) l.get(0)).getValue(1).asInt()));
  }


  /**
   *  Gets the coordinateSetNames attribute of the DBMolecule object
   *
   * @return                The coordinateSetNames value
   * @exception  Exception  Description of Exception
   */
  public List<List<String>> getCoordinateSetNames() throws Exception {
    List<List<String>> v = new Vector<List<String>>();
    List<DBCoordinateSet2d> sets2 = ((DBAtom) getDBAtoms().get(0)).getDBCoordinateSet2ds();
    List<DBCoordinateSet3d> sets3 = ((DBAtom) getDBAtoms().get(0)).getDBCoordinateSet3ds();
    for (int i = 0; i < sets2.size(); i++) {
      List<String> local = new Vector<String>();
      DBCoordinateSetType type = ((DBCoordinateSet2d) sets2.get(i)).getDBCoordinateSetType();
      local.add(type.getCoordinateSetTypeId() + ".2");
      local.add(new String(type.getDescription()) + " (2D)");
      v.add(local);
    }
    for (int i = 0; i < sets3.size(); i++) {
      List<String> local = new Vector<String>();
      DBCoordinateSetType type = ((DBCoordinateSet3d) sets3.get(i)).getDBCoordinateSetType();
      local.add(type.getCoordinateSetTypeId() + ".3");
      local.add(new String(type.getDescription()) + " (3D)");
      v.add(local);
    }
    return (v);
  }


  /**
   *Gets the DBSpectra associated with this DBMolecule (reviewed as true)
   *
   * @return                The spectra
   * @exception  Exception  Something wrong with reading database
   */
  public List<DBSpectrum> selectSpectra() throws TorqueException {
    return (selectSpectraByUser(null, true, false, null));
  }


  /**
   *Gets the DBSpectra associated with this DBMolecule (reviewed as true) except one given by editSpectrum.
   *
   * @param  editSpectrum   The spectrum to be ignored, null if nothing to ignore.
   * @return                The spectra.
   * @exception  Exception  Something wrong with reading database.
   */
  public List<DBSpectrum> selectSpectra(int editSpectrum) throws TorqueException {
    List<DBSpectrum> v = selectSpectraByUser(null, true, false, null);
    for (int i = 0; i < v.size(); i++) {
      if (v.get(i).getSpectrumId()==editSpectrum) {
        v.remove(i);
        break;
      }
    }
    return (v);
  }
  
  /**
   * Tells if this molecule has at least one spectrum reviewed as true, not private, and mark at least minimum.mark.display
   * 
   * @return true=there is such a spectrum, false=none
   * @throws IOException 
   * @throws FileNotFoundException 
   * @throws DataSetException 
   * @throws TorqueException 
   * @throws NumberFormatException 
   */
  public boolean hasValidPublicSpectra() throws FileNotFoundException, IOException, NumberFormatException, TorqueException, DataSetException {
	  List<DBSpectrum> v = this.getDBSpectrums();
	  for(DBSpectrum spectrum : v) {
		  if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.TRUE) && spectrum.getMark()>=spectrum.getDisplaymark())
			  return true;
	  }
	  return false;
  }


  /**
   * Gets the DBSpectra of a certain user associated with this DBMolecule (reviewed as true, false, rejected)
   *
   * @param  userName       The user of whom the spectra are to be selected.
   * @param  userId         The user of whom the spectra are to be selected (should match userName).
   * @param  trueOnly       Select only spectra reviewed as true.
   * @param  fromUserOnly   If false, userName/userId is ignored, spectra of all user are selected.
   * @param  spectrumType   If not null, only spectra of this type (a valid name entry from SPECTRUM_TYPE table) are selected.
   * @return                The spectra
   * @exception  TorqueException  Something wrong with reading database
   */
  public List<DBSpectrum> selectSpectraByUser(User turbineUser, boolean trueOnly, boolean fromUserOnly, String spectrumType) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
    crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_TYPE_ID);
    crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_ID);
    if (fromUserOnly && turbineUser.getUserName() != null && !turbineUser.getUserName().equals("anon")) {
      crit.addJoin(DBSpectrumPeer.USER_ID, NmrshiftdbUserPeer.USER_ID);
      crit.add(NmrshiftdbUserPeer.LOGIN_NAME, turbineUser.getUserName());
    }
    if (trueOnly) {
        if(turbineUser!=null && !turbineUser.getUserName().equals("anon")){
        	Criteria.Criterion critTrue=crit.getNewCriterion(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE,Criteria.EQUAL);
        	Criteria.Criterion critPrivate=crit.getNewCriterion(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.PRIVATE,Criteria.EQUAL);
        	Criteria.Criterion critUserid=crit.getNewCriterion(DBSpectrumPeer.USER_ID, ((BaseJetspeedUser)turbineUser).getUserId(),Criteria.EQUAL);
            crit.add(critTrue.or(critPrivate.and(critUserid)));
        }else{
            crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);        	
        }
    } else {
    	Criteria.Criterion critTrue=crit.getNewCriterion(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE,Criteria.EQUAL);
    	Criteria.Criterion critFalse=crit.getNewCriterion(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.FALSE,Criteria.EQUAL);
    	if(turbineUser!=null && !turbineUser.getUserName().equals("anon")){
        	Criteria.Criterion critPrivate=crit.getNewCriterion(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.PRIVATE,Criteria.EQUAL);
        	Criteria.Criterion critUserid=crit.getNewCriterion(DBSpectrumPeer.USER_ID, ((BaseJetspeedUser) turbineUser).getUserId(),Criteria.EQUAL);
            crit.add(critTrue.or(critFalse).or(critPrivate.and(critUserid)));
    	}else{
    		crit.add(critTrue.or(critFalse));
    	}
    }
    if(spectrumType!=null){
        crit.addJoin(DBSpectrumPeer.SPECTRUM_TYPE_ID, DBSpectrumTypePeer.SPECTRUM_TYPE_ID);
        crit.or(DBSpectrumTypePeer.NAME, spectrumType);
    }
    return DBSpectrumPeer.doSelect(crit);
  }


  /**
   *Gets the DBSpectra associated with this DBMolecule and matching a fingerprint
   *
   * @param  fingerprint    The fingerprint to search by
   * @return                The spectra
   * @exception  Exception  Something wrong with reading database
   */
  public List<DBSpectrum> selectSpectra(long fingerprint) throws TorqueException {
    Connection dbconn = null;
    List<DBSpectrum> spectraInDB = new ArrayList<DBSpectrum>();
    try {
      dbconn = Torque.getConnection();
      Statement stmt = dbconn.createStatement();
      String query = "SELECT SPECTRUM.SPECTRUM_ID FROM SPECTRUM WHERE MOLECULE_ID=" + this.getMoleculeId() + " AND fingerprint_compare64(SPECTRUM.FINGERPRINT, " + fingerprint + ") = \"Y\" AND SPECTRUM.REVIEW_FLAG='true' order by SPECTRUM_TYPE_ID;";
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        Criteria crit = new Criteria();
        crit.add(DBSpectrumPeer.SPECTRUM_ID, rs.getString(1));
        List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
        spectraInDB.add(v.get(0));
      }
      stmt.close();
    } catch (Exception ex) {
      throw new TorqueException(ex);
    } finally {
      try {
        Torque.closeConnection(dbconn);
      } catch (Exception ex) {
        throw new TorqueException(ex);
      }
    }
    return (spectraInDB);
  }


  /**
   *Gets the non-H atoms of the DBMolecule
   *
   * @return                The atoms
   * @exception  Exception  Something wrong with reading database
   */
  public List<DBAtom> selectNonHAtoms() throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID, this.getMoleculeId());
    crit.add(DBAtomPeer.SYMBOL, (Object) "H", Criteria.NOT_EQUAL);
    crit.addAscendingOrderByColumn(DBAtomPeer.ATOM_ID);
    List<DBAtom> v = DBAtomPeer.doSelect(crit);
    return v;
  }


  /**
   *  Tells if any double bond is specified in this molecule
   *
   * @return                True=at least one is specified, false=none is specified
   * @exception  Exception  Database problems
   */
  public List<List<DBAtom>> containsDoubleBondsSpecified() throws Exception {
    List<List<DBAtom>> returnV = new Vector<List<DBAtom>>();
    List<DBBond> bonds = getBonds();
    for (int i = 0; i < bonds.size(); i++) {
      if (((DBBond) bonds.get(i)).getIsConfigurationSpecified().equals(NmrshiftdbConstants.TRUE)) {
        returnV.add(((DBBond) bonds.get(i)).getAtoms());
      }
    }
    return (returnV);
  }


  /**
   *  Deletes this molecule plus all associated rows (including spectra, independent of review flag).
   *
   * @param  dbconn         The connection to use.
   * @exception  Exception  Database problems.
   */
  public void delete(Connection dbconn) throws Exception {
    List<DBSpectrum> v = getDBSpectrums();
    for (int i = 0; i < v.size(); i++) {
      ((DBSpectrum) v.get(i)).delete(dbconn,false);
    }
    int molId = getMoleculeId();
    Criteria crit = new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID, molId);
    List<DBAtom> v5 = DBAtomPeer.doSelect(crit);
    Vector<DBBond> v3 = new Vector<DBBond>();
    for (int k = 0; k < v5.size(); k++) {
      DBAtom atom = ((DBAtom) v5.get(k));
      Vector<DBBond> v2 = atom.getBonds();
      for (int m = 0; m < v2.size(); m++) {
        v3.add(v2.get(m));
      }
      crit = new Criteria();
      crit.add(DBBondDBAtomPeer.ATOM_ID, atom.getAtomId());
      List<DBBondDBAtom> v4 = DBBondDBAtomPeer.doSelect(crit, dbconn);
      DBBondDBAtomPeer.doDelete(crit, dbconn);
      for (int l = 0; l < v2.size(); l++) {
        Criteria crit2 = new Criteria();
        crit.add(DBBondPeer.BOND_ID, ((DBBondDBAtom) v4.get(l)).getBondId());
        DBBondPeer.doDelete(crit2, dbconn);
      }
      crit = new Criteria();
      crit.add(DBIdentifierPeer.ATOM_ID, atom.getAtomId());
      DBIdentifierPeer.doDelete(crit,dbconn);
      atom.delete(dbconn);
    }
    for (int k = 0; k < v3.size(); k++) {
      ((DBBond) v3.get(k)).delete(dbconn);
    }
    crit = new Criteria();
    crit.add(DBChemicalNamePeer.MOLECULE_ID, molId);
    DBChemicalNamePeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBMoleculeHyperlinkPeer.MOLECULE_ID, molId);
    DBMoleculeHyperlinkPeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBMoleculeDBKeywordPeer.MOLECULE_ID, molId);
    DBMoleculeDBKeywordPeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBCanonicalNamePeer.MOLECULE_ID, molId);
    DBCanonicalNamePeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBDatasetDoiPeer.MOLECULE_ID, molId);
    DBDatasetDoiPeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBDatasetReviewKeyPeer.MOLECULE_ID, molId);
    DBDatasetReviewKeyPeer.doDelete(crit, dbconn);
    crit = new Criteria();
    crit.add(DBMoleculePeer.MOLECULE_ID, molId);
    DBMoleculePeer.doDelete(crit, dbconn);
  }


  /**
   *  Adds values of an existing molecule to a SubmittingData object.
   *
   * @param  subData          The SubmittingData object.
   * @param  spectrum         The data of this spectrum are added.
   * @param  no3d             generate 2d coordinates from 3d if true.
   * @param  setMolecule      if true, the atom in the submittingdata gets replaces, if false the atom is not set.
   * @throws DataSetException 
   * @throws IOException 
   * @throws TorqueException 
   * @throws OptionalDataException 
   * @exception  IOException  Problems deserializing molecule.
   * @throws ClassNotFoundException 
   * @throws CDKException 
   * @exception  Exception    Database problems.
   */
  public void addValuesToSubmittingData(SubmittingData subData, DBSpectrum spectrum, boolean no3d, boolean setMolecule) throws OptionalDataException, TorqueException, IOException, DataSetException, CDKException, ClassNotFoundException, NmrshiftdbException {
      if(setMolecule){
          IAtomContainer cdkmol = getAsCDKMolecule(1);
    	  for(int i=0;i<getDBAtoms().size();i++){
    		  if(((DBAtom)getDBAtoms().get(i)).getIsVisible().equals(NmrshiftdbConstants.FALSE)){
    			  cdkmol.getAtom(i).setProperty(SubmittingData.VISIBLE,NmrshiftdbConstants.FALSE);
    		  }
    	  }
    	  subData.setMol(cdkmol, false, false, no3d);
      }
	  subData.setChemName(getChemicalNamesAsOneString(false,false,true));
    subData.setMoleculeLinks(getLinksAsString());
    subData.setCasNumber(getCasNumber());
    subData.setMoleculeKeywords(getMoleculeKeywordsAsOneString("\n"));
    subData.setSmilesNonChiralProv(getSmilesStringStr());
    if(StringUtils.countMatches(this.getInchi(), "/")<=3){
    //if(getSmilesStringChiralStr()==null){
      subData.setIsChiralProv(false);
    }else{
      subData.setIsChiralProv(true);
      subData.setSmilesChiralProv(getSmilesStringChiralStr());
    }
    subData.setExistingMoleculeId(getMoleculeId());
    List<DBBond> bonds = getBonds();
    boolean[] isDoubleBondSpecified = new boolean[bonds.size()];
    boolean anyBondSpecified = false;
    for (int i = 0; i < bonds.size(); i++) {
      if (((DBBond) bonds.get(i)).getIsConfigurationSpecified().equals(NmrshiftdbConstants.TRUE)) {
        isDoubleBondSpecified[i] = true;
        anyBondSpecified = true;
      }
    }
    if (anyBondSpecified) {
      subData.setDoubleBondConfiguration(anyBondSpecified);
      subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
    }
    if (spectrum != null) {
      DBAtom[] atoms = getAtomsAsArrayInMdlOrder();
      if(spectrum.getDBSpectrumType().getDimensionality()==1){
	      for (int i = 0; i < atoms.length; i++) {
	        if (atoms[i].getAnySignal(spectrum.getSpectrumId()) != null && !atoms[i].getAnySignal(spectrum.getSpectrumId()).getMultiplicity().equals("")) {
	        	AtomUtils.setChildElement(subData.getCMLMolecule().getAtoms().get(i), SubmittingData.MULTIPLICITY, atoms[i].getAnySignal(spectrum.getSpectrumId()).getMultiplicity());
	        }
	      }
      }
      for (int i = 0; i < atoms.length; i++) {
        for (int k = 0; k < atoms[i].getCouplings(spectrum).size(); k++) {
          DBCoupling coupling = (DBCoupling) atoms[i].getCouplings(spectrum).get(k);
          if (coupling.getDBAtomRelatedByAtomId1().getMdlNumber() > coupling.getDBAtomRelatedByAtomId2().getMdlNumber()) {
              Element couplingelement = new Element(SubmittingData.COUPLING);
              couplingelement.addAttribute(new Attribute("otherAtom",coupling.getDBAtomRelatedByAtomId2().getMdlNumber()+""));
              couplingelement.addAttribute(new Attribute("value",coupling.getConstant()+""));
              subData.getCMLMolecule().getAtoms().get(coupling.getDBAtomRelatedByAtomId1().getMdlNumber()).appendChild(couplingelement);
          } else {
              Element couplingelement = new Element(SubmittingData.COUPLING);
              couplingelement.addAttribute(new Attribute("otherAtom",coupling.getDBAtomRelatedByAtomId1().getMdlNumber()+""));
              couplingelement.addAttribute(new Attribute("value",coupling.getConstant()+""));
              subData.getCMLMolecule().getAtoms().get(coupling.getDBAtomRelatedByAtomId2().getMdlNumber()).appendChild(couplingelement);
          }
        }
      }
    }
  }


  /**
   *  A save which handles fingerprints via prepared statements, avoiding sql problems.
   *
   * @exception  Exception  Description of Exception
   */
  public void save() throws TorqueException {
      Connection dbconn=null;
	  try{
        if (getMoleculeId() != 0) {
          //setFingerprint();
        }
        dbconn = Torque.getConnection();
        handleFingerprints1();
        super.save(dbconn);
        handleFingerprints2(dbconn);
	  }catch(Exception ex){
		  throw new TorqueException(ex);
	  }
	  finally{
	    Torque.closeConnection(dbconn);
	  }
  }


  /**
   *  A save which handles fingerprints via prepared statements, avoiding sql problems.
   *
   * @param  dbconn         Description of Parameter
   * @exception  Exception  Description of Exception
   */
  public void save(Connection dbconn) throws TorqueException {
	  try{
    if (getMoleculeId() != 0) {
      //setFingerprint();
    }
    handleFingerprints1();
    super.save(dbconn);
    handleFingerprints2(dbconn);
	  }catch(SQLException ex){
		  throw new TorqueException(ex);
  	/*}catch(CDKException ex){
  		throw new TorqueException(ex);
    }catch(IOException ex){
        throw new TorqueException(ex);*/
  	}
  }


  /**
   *  Sets fingerprints to 0 and saves old values.
   */
  public void handleFingerprints1() {
    if (fppro != null) {
      return;
    }
    fppro = new long[16];
    fppro[0] = getFp0();
    setFp0(0);
    fppro[1] = getFp1();
    setFp1(0);
    fppro[2] = getFp2();
    setFp2(0);
    fppro[3] = getFp3();
    setFp3(0);
    fppro[4] = getFp4();
    setFp4(0);
    fppro[5] = getFp5();
    setFp5(0);
    fppro[6] = getFp6();
    setFp6(0);
    fppro[7] = getFp7();
    setFp7(0);
    fppro[8] = getFp8();
    setFp8(0);
    fppro[9] = getFp9();
    setFp9(0);
    fppro[10] = getFp10();
    setFp10(0);
    fppro[11] = getFp11();
    setFp11(0);
    fppro[12] = getFp12();
    setFp12(0);
    fppro[13] = getFp13();
    setFp13(0);
    fppro[14] = getFp14();
    setFp14(0);
    fppro[15] = getFp15();
    setFp15(0);
  }


  /**
   *  Inserts fingerprint values from handleFingerprints1 via a prepared statement.
   *
   * @param  dbconn         Description of Parameter
   * @exception  Exception  Description of Exception
   */
  public void handleFingerprints2(Connection dbconn) throws TorqueException, SQLException {
    StringBuffer query = new StringBuffer("Update MOLECULE set ");
    for (int i = 0; i < 16; i++) {
      query.append("fp" + i + "=" + fppro[i] + ",");
    }
    query = GeneralUtils.removeLastComma(query);
    query.append(" where MOLECULE_ID=" + getMoleculeId());
    PreparedStatement ps = dbconn.prepareStatement(query.toString());
    ps.executeUpdate();
  }


  /**
   *  Gets this molecule as a cdk molecule with explicit hs as painted by user. Implicit Hs are set depending on setImplicitHydrogens.
   *
   * @param  setid          The coordinate set type id of coordinate set to use.
   * @param  setImplicitHydrogens If true hydrogen count of atoms is set to total count (including explicit ones).
   * @return                The molecule.
   * @exception  TorqueException  Database problems.
   * @throws IOException 
   * @throws OptionalDataException 
   * @throws DataSetException 
   */
  public IAtomContainer getAsCDKMoleculeAsEntered(int setid, boolean setImplicitHydrogens) throws TorqueException, OptionalDataException, IOException, DataSetException {
      IAtomContainer promol = new org.openscience.cdk.AtomContainer();
    for (int i = 0; i < getAsCDKMolecule(setid).getAtomCount(); i++) {
      promol.addAtom(getAsCDKMolecule(setid).getAtom(i));
    }
    for (int i = 0; i < getAsCDKMolecule(setid).getBondCount(); i++) {
      promol.addBond(getAsCDKMolecule(setid).getBond(i));
    }
    DBAtom[] atoms = getAtomsAsArrayInMdlOrder();
    boolean[] visibility = new boolean[atoms.length];
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getIsVisible().equals("true")) {
        visibility[i] = true;
      }
    }
    promol.setProperty(CDKConstants.TITLE, getChemicalNamesAsOneString(false, false));
    promol.setProperty(CDKConstants.REMARK, "nmrshiftdb2 " + getMoleculeId());
    for(int i=0;i<promol.getAtomCount();i++){
      promol.getAtom(i).setFlag(CDKConstants.ISAROMATIC, false);
      if(i<promol.getBondCount())
        promol.getBond(i).setFlag(CDKConstants.ISAROMATIC, false);
    }
    //We set all explicit hydrogens as implicit
    if(setImplicitHydrogens){
        Iterator<IAtom> it=promol.atoms().iterator();
        while(it.hasNext()){
        	IAtom atom=it.next();
        	atom.setImplicitHydrogenCount(AtomContainerManipulator.countExplicitHydrogens(promol,atom));
        }
    }
    for (int i = promol.getAtomCount() - 1; i > -1; i--) {
      if (!visibility[i]) {
        promol.removeAtomAndConnectedElectronContainers(promol.getAtom(i));
      }
    }
    //remaining explicit hydrogens are substracted to get correct implicit hydrogen count
    if(setImplicitHydrogens){
        Iterator<IAtom> it=promol.atoms().iterator();
        while(it.hasNext()){
            IAtom atom=it.next();
            atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount()-AtomContainerManipulator.countExplicitHydrogens(promol,atom));
        }
    }
    return (promol);
  }


  /**
   * Get the MolecularWeight
   * @return double
   */
  public float getMolecularWeightFloat()
  {
    return (float)getMolecularWeight();
  }


  /**
   *  Adds 3d coords from cdk modelbuilder3d to this molecule.
   *
   * @param  conn           DBConnection to use.
   * @param  mol            This molecule as cdk molecule.
   * @param  atoms          DBAtoms of this molecule.
   * @return                mol with 3d coords added.
   * @exception  Exception  Database problems.
   */
  public IAtomContainer addCdkCoords(Connection conn, IAtomContainer mol, DBAtom[] atoms) throws Exception{
    for(int k=0;k<mol.getAtomCount();k++){
      mol.getAtom(k).setFlag(CDKConstants.ISPLACED, false);
    }
    IAtomContainer mol3d = GeneralUtils.getModelBuilder("mm2").generate3DCoordinates(mol,false);
    for(int k=0;k<mol3d.getAtomCount();k++){
      DBCoordinateSet3d c3=new DBCoordinateSet3d(mol3d.getAtom(k).getPoint3d().x,mol3d.getAtom(k).getPoint3d().y,mol3d.getAtom(k).getPoint3d().z,3,atoms[k].getAtomId());
      c3.save(conn);
    }
    return mol3d;
  }
  
  public DBWishlist getDBWishlist(int pk) throws Exception{
    Criteria crit = new Criteria();
    crit.add(DBWishlistPeer.MOLECULE_ID, getMoleculeId());
    crit.add(DBWishlistPeer.SPECTRUM_TYPE_ID, pk+"");
    return (DBWishlist)DBWishlistPeer.doSelect(crit).get(0);
  }
  
  public boolean hasAny3d() throws Exception{
    Criteria crit = new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID, getMoleculeId());
    crit.addJoin(DBAtomPeer.ATOM_ID, DBCoordinateSet3dPeer.ATOM_ID);
    List<DBAtom> v=DBAtomPeer.doSelect(crit);
    if(v.size()>0)
      return true;
    else
      return false;
  }

  public String getIUPACName() throws TorqueException {
	Criteria crit=new Criteria();
	crit.add(DBCanonicalNamePeer.MOLECULE_ID,this.getMoleculeId());
	crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID,2);
	List<DBCanonicalName> l=DBCanonicalNamePeer.doSelect(crit);
	if(l.size()>0)
		return ((DBCanonicalName)l.get(0)).getName();
	else
		return null;
  }

  /**
   * Gets the standard inchi (from db, not generatad) for this molecule.
   * 
   * @return The inchi.
   * @throws TorqueException Database problems.
   */
  public String getInchi() throws TorqueException {
	Criteria crit=new Criteria();
	crit.add(DBCanonicalNamePeer.MOLECULE_ID,this.getMoleculeId());
	crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID,4);
	List<DBCanonicalName> l=DBCanonicalNamePeer.doSelect(crit);
	if(l.size()>0)
		return ((DBCanonicalName)l.get(0)).getName();
	else
		return null;
  }


  /**
   * Gets the inchiKey (from db, not generatad) for this molecule.
   * 
   * @return The inchiKey.
   * @throws TorqueException Database problems.
   */
  public String getInchiKey() throws TorqueException {
    Criteria crit=new Criteria();
    crit.add(DBCanonicalNamePeer.MOLECULE_ID,this.getMoleculeId());
    crit.add(DBCanonicalNamePeer.CANONICAL_NAME_TYPE_ID,5);
    List<DBCanonicalName> l=DBCanonicalNamePeer.doSelect(crit);
    if(l.size()>0)
        return ((DBCanonicalName)l.get(0)).getName();
    else
        return null;
  }
  
	/**
	 * Gets a string with the ids of all valid spectra of this molecule
	 * 
	 * @return The string
	 * @throws TorqueException Database problems
	 */
	public String getSpectrumIds() throws TorqueException {
	    List<DBSpectrum> v = selectSpectra();
	    StringBuffer result = new StringBuffer();
	    for (int i = 0; i < v.size(); i++) {
	      result.append(((DBSpectrum) v.get(i)).getSpectrumId() + ";");
	    }
	    return (GeneralUtils.removeLastComma(result).toString());	  
	}
	
	/**
	 * Gets the current moleculeIds of all spectra, which were once
	 * assigned to this molecule and have been edited and are now assigned to another spectrum.
	 * 
	 * @return The ids.
	 * @throws TorqueException Database problems.
	 */
	public List<List<Integer>> getNewMoleculeIdsOfOldSpectra() throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBSpectrumPeer.MOLECULE_ID,this.getMoleculeId());
		crit.add(DBSpectrumPeer.REVIEW_FLAG,NmrshiftdbConstants.EDITED);
		List<DBSpectrum> oldSpectra=DBSpectrumPeer.doSelect(crit);
		List<List<Integer>> result=new ArrayList<List<Integer>>();
		for(int i=0;i<oldSpectra.size();i++){
			DBSpectrum oldSpectrum=(DBSpectrum)oldSpectra.get(i);
			DBSpectrum currentSpectrum=oldSpectrum.getCurrentSpectrum();
			if(!oldSpectrum.getMoleculeId().equals(currentSpectrum.getMoleculeId())){
				List<Integer> ids=new ArrayList<Integer>();
				ids.add(oldSpectrum.getSpectrumId());
				ids.add(currentSpectrum.getMoleculeId());
				result.add(ids);
			}
		}
		return result;
	}
	


	/**
	 * Gives a multiplicity string (SxDyTzQa) based on HCount of Cs.
	 * 
	 * @return The multiplicity string
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public String getPotentialMultiplicities() throws IOException, ClassNotFoundException, Exception {
	    IAtomContainer cdkmol=getAsCDKMoleculeWithoutH();
        int[] mults=new int[4];
        for(int k=0;k<cdkmol.getAtomCount();k++){
          if(cdkmol.getAtom(k).getSymbol().equals("C")){
		if(cdkmol.getAtom(k).getImplicitHydrogenCount()==null)
			mults[0]++;
        	else if(cdkmol.getAtom(k).getImplicitHydrogenCount()<4)
		        mults[cdkmol.getAtom(k).getImplicitHydrogenCount()]++;
          }
        }
        return "S"+mults[0]+"D"+mults[1]+"T"+mults[2]+"Q"+mults[3];
	}


	/**
	 * Tells if any of the valid spectra of this molecule have been created by a user 
	 * who is a member of any labgroup.
	 * 
	 * @return Any spectrum from a labgroup member?
	 * @throws TorqueException 
	 * @throws DataSetException 
	 */
	public boolean hasAnyLabgroupSpec() throws TorqueException, DataSetException {
		List<Record> l = DBSpectrumPeer.executeQuery("select count(*) from SPECTRUM join TURBINE_USER using (USER_ID ) join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null) where LABGROUP_ID >0 and MOLECULE_ID="+this.getMoleculeId());
		return ((Record)l.get(0)).getValue(1).asInt()>0;
	}


	/**
	 * Tells if this molecule has a csearch predicted spectrum (via condition)
	 *
	 * @throws TorqueException 
	 */
	public boolean hasCSearchSpectrum() throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
		crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBConditionPeer.SPECTRUM_ID);
		crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
		crit.add(DBConditionPeer.VALUE,"CSEARCH");
		List<DBSpectrum> spectra = DBSpectrumPeer.doSelect(crit);
		if(spectra.size()>0)
			return true;
		else
			return false;
	}


    /**
     * Returns true if this molecule has a public spectrum.
     * 
     * @return
     * @throws TorqueException
     * @throws SQLException 
     */
    public boolean isPublic() throws TorqueException, SQLException {
        List<DBSpectrum> spectra = this.selectSpectra();
        for(Object spectrum : spectra){
            if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.TRUE))
                return true;
            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.FALSE))
                return true;
        }
        return false;
    }
    
    
    /**
     * Returns true if this molecule has a public spectrum or a private one from user.
     * 
     * @param user
     * @return
     * @throws TorqueException
     * @throws SQLException 
     */
    public boolean isPublicOrOwnPrivate(TurbineUser user, String viewkey) throws TorqueException, SQLException {
        List<DBSpectrum> spectra = this.getDBSpectrums();
        for(Object spectrum : spectra){
            if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.TRUE))
                return true;
            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.FALSE))
                return true;
            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.FALSE_OM))
                return true;
            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && user!=null && ((DBSpectrum)spectrum).getUserId()==user.getUserId())
                return true;
            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && ((DBSpectrum)spectrum).getReviewKey().equals(viewkey))
                return true;
        }
        if(user!=null){
	        List<DBSample> samples = this.getDBSamples();
	        for(Object sample : samples){
	        	//labgroupworkers can always see structures which have a sample
	        	if(user.hasPermission("labgroupworker"))
	        		return true;
	        	if(((DBSample)sample).getUserId()==user.getUserId())
	        		return true;
	        }
        }
        return false;
    }
    
    /**
     * Gets the links from Unichem for this structure, relies on existance of inchi key.
     *
     * @param data The current rundata object.
     * @return The links as one string, separated by;
     */
    public String getUnichemLinks(RunData data) {
        //Do the unichem links
		try {
	    	BufferedReader in = null;
	        try{
	            if(this.getInchiKey()!=null && !this.getInchiKey().equals("")){
	                URL url = new URL("https://www.ebi.ac.uk/unichem/rest/inchikey/"+this.getInchiKey());
	                HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	                String strorig;
	                StringBuffer links = new StringBuffer();
	                while((strorig = in.readLine()) !=null){
                          StringTokenizer st=new StringTokenizer(strorig,"}");
                          String str="";
                          String currentid=null;
                          while(st.hasMoreTokens()){
                            str=st.nextToken();
	                    if(str.indexOf("src_compound_id")>-1){
	                        currentid = str.substring(str.indexOf("src_compound_id\":")+19).trim();
                                currentid = currentid.substring(0,currentid.indexOf('"'));
	                    }
                            if(str.indexOf("src_id")>-1){
	                        String source = str.substring(str.indexOf("src_id\":")+10).trim();
                                source=source.substring(0,source.indexOf('"'));
	                        //24 is nmrshiftdb2, we don't show link to ourselves
	                        if(!source.equals("24")){
	                	    	BufferedReader in2 = null;
	                	        try{
		                            URL url2 = new URL("https://www.ebi.ac.uk/unichem/api/v1/sources/"+source);
		                            HttpsURLConnection con2 = (HttpsURLConnection)url2.openConnection();
		                            in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
		                            String str2;
		                            String baseurl=null;
		                            while((str2 = in2.readLine()) !=null){
		                                if(str2.indexOf("baseIdUrl")>-1){
		                                        baseurl=str2.substring(str2.indexOf("baseIdUrl\":")+12).trim();
                                                        baseurl=baseurl.substring(0,baseurl.indexOf('"'));
		                                }
                                                if(str2.indexOf("nameLong")>-1){
                                                    String name=str2.substring(str2.indexOf("nameLong\":")+11);
                                                    name=name.substring(0,name.indexOf('"'));
		                                    links.append("<a href=\"derefer.jsp?url="+baseurl+currentid+"\">"+name+"</a>;");
		                                }
		                            }
	                	        }finally {
	                	        	if(in2!=null)
	                	        		in2.close();
	                	        }
	                        }
	                    }
                          }
	                }
	                in.close();
	                return GeneralUtils.removeLastComma(links).toString();
	            }
	            return "";
	        }
	        finally {
	        	if(in!=null)
					in.close();
	        }
        }
        catch(Exception ex){
            //We log errors, but show no error in web interface
            GeneralUtils.logError(ex, "getting unichem links for "+this.getMoleculeId(), data, false);
            return "";
        }
    }
    
    
    /**
     * Gives the link to the small image of the molecule. Takes image caching into account and sets background for lab group contributions.
     * 
     * @param runData
     * @return The link (relative).
     * @throws TorqueException
     * @throws IOException
     * @throws SQLException
     * @throws DataSetException
     */
    public String getSymbolImageLink(RunData runData, ValuesForVelocityBean lastSearch) throws TorqueException, IOException, SQLException, DataSetException{
        if(UrlTool.doImageCache() && lastSearch!=null && !lastSearch.getDisplayText().equals(NmrshiftdbConstants.HOSECODE) && isPublic()){
            return "images/molecules/small/"+getMoleculeId()+".jpeg";
        }else{
            StringBuffer sb= new StringBuffer("download/NmrshiftdbServlet/").append(getMoleculeId()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid=").append(getMoleculeId()).append("&format=jpeg&size=small&shownumbers=false&backcolor=");
            if(this.hasAnyLabgroupSpec())
                sb.append(ResultAction.LABGROUP_BACKGROUND_COLOR);
            else
                sb.append(ResultAction.STANDARD_BACKGROUND_COLOR);
            return sb.toString();
        }
    }
    
    public String getEquivalenceImageLink(RunData runData) {
    	return "download/NmrshiftdbServlet/"+getMoleculeId()+".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+getMoleculeId()+"&format=jpeg&size=450x450&shownumbers=false&backcolor="+ResultAction.STANDARD_BACKGROUND_COLOR+"&equivalences=true"; 
    }


	public NmreData getNmreData(DBSpectrum spectrum, HttpServletRequest req, List<String> locations, boolean datasetonly, boolean bysolventandtemp, String absolutePath) throws TorqueException, OptionalDataException, IOException, DataSetException, JCAMPException, NmrshiftdbException, CDKException, CloneNotSupportedException {
        NmreData data=new NmreData();
        data.setVersion(NmredataVersion.ONEPOINTONE);
        String solvent=spectrum.getSolvent();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.*?)\\((.*?)\\)");
		Matcher matcher = pattern.matcher(solvent);
		if(matcher.find())
			solvent=matcher.group(2);
        data.setSolvent(solvent);
        IAtomContainer molwithh=spectrum.getDBMolecule().getAsCDKMolecule(1);
        data.setMolecule(molwithh);
        try {
        	data.setTemperature(Double.parseDouble(spectrum.getTemperature()));
        }catch(NumberFormatException ex) {
        	//this should only happen for unreported/unkown, we use no temp in that case
        }
        double[][] xy=new double[0][];
        int l=0;
        for(DBSpectrum spectrumlocal : spectrum.getDBMolecule().selectSpectra()){
        	if((bysolventandtemp && spectrumlocal.getSolvent().equals(spectrum.getSolvent()) && spectrumlocal.getTemperature().equals(spectrum.getTemperature())) || ((!datasetonly && spectrumlocal.getSpectrumId()==spectrum.getSpectrumId()) || (datasetonly && spectrumlocal.getSolvent().equals(spectrum.getSolvent()) && spectrumlocal.getKeywords().equals(spectrum.getKeywords())))){
                double freq=0;
                try{
                	freq=Double.parseDouble(spectrumlocal.getFrequency());
                }catch(NumberFormatException ex){
                	//do nothing
                }
	            String location = spectrumlocal.getDBMolecule().getEasylink(req);
	            if(absolutePath!=null && spectrumlocal.getRawData()!=null) {
	            	location=spectrumlocal.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, absolutePath);
	            }
	            if(locations!=null && spectrumlocal.getRawData()!=null){
	            	location = locations.get(l);
	            	l++;
	            }
	        	spectrumlocal.getNmreData(datasetonly, bysolventandtemp, spectrum, freq, xy, location, absolutePath, data);
        	}
        }
        StringBuffer sb=new StringBuffer();
        if(this.getDoi()!=null){
        	sb.append("Doi="+this.getDoi()+"\\");
        }
        sb.append("DB_ID="+this.getMoleculeId());
        data.setID(sb.toString());
        return data;
	}


	/**
	 * Returns all spectra for a certain solvent and keyword.
	 * 
	 * @param solvent The solvent (name as in db)
	 * @param keyword The keywords, separated by ;
	 * @return The spectra, empty list of none.
	 * @throws TorqueException
	 */
	public  List<DBSpectrum> selectSpectra(String solvent, String keyword, boolean showprivate) throws TorqueException {
	    Criteria crit = new Criteria();
	    crit.add(DBSpectrumPeer.MOLECULE_ID, this.getMoleculeId());
	    crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_TYPE_ID);
	    crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_ID);
	    if(showprivate)
	        crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.PRIVATE);
	    else
	    	crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);        	
        crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBConditionPeer.SPECTRUM_ID);
        crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
        crit.add(DBConditionPeer.VALUE, solvent);
        crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBKeywordPeer.SPECTRUM_ID);
        crit.addJoin(DBSpectrumDBKeywordPeer.KEYWORD_ID, DBKeywordPeer.KEYWORD_ID);
        StringTokenizer st=new StringTokenizer(keyword,";");
    	Criteria.Criterion c0 = crit.getNewCriterion(DBKeywordPeer.KEYWORD, st.nextToken(), Criteria.EQUAL);
        while(st.hasMoreTokens()){
        	Criteria.Criterion c1 = crit.getNewCriterion(DBKeywordPeer.KEYWORD, st.nextToken(), Criteria.EQUAL);
        	c0.or(c1);
        }
        crit.add(c0);
        crit.setDistinct();
	    return DBSpectrumPeer.doSelect(crit);
	}


	public static void deleteCacheFiles(int moleculeId, ServletConfig servcon, String relPath) {
		File dir = new File(ServletUtils.expandRelative(servcon, relPath+"WEB-INF/templates/vm/portlets/html/result/detailcache/"));
		FilenameFilter fileFilter = new MyFilenameFilter(Integer.toString(moleculeId));
		File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}		
	}
	
	public static class MyFilenameFilter implements FilenameFilter {

		private String start;

		public MyFilenameFilter(String start) {
			this.start = start;
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith(start+"_");
		}

	}
	
	
	public RoCrate getRoCrate(int nmredatasize) throws TorqueException{
        RoCrateBuilder builder= new RoCrateBuilder("nmrshiftdb"+getMoleculeId(), "A RO-Crate for nmrshiftdb2 molecule id "+getMoleculeId())
                .addUrlToContext("https://w3id.org/ro/crate/1.1/context");
        for(DBSpectrum spectrum : this.selectSpectra()) {
            if(spectrum.getRawData()!=null && spectrum.getRawData().length!=0) {
                    String filename=new String(spectrum.getSpectrumId()+"_"+spectrum.getSpectrumTypeName()+".zip");
                    builder.addDataEntity(
                          new FileEntity.FileEntityBuilder()
                            .setId(filename)
                            .addProperty("name", "raw data for "+spectrum.getDBSpectrumType().getName())
                            .addProperty("contentSize", spectrum.getRawDataDecoded().length)
                            .addProperty("encodingFormat", "application/zip")
                            .build()
                    );
            }
        }
        HashMap<String,List<String>> solventandtemp=new HashMap<>();
        for(DBSpectrum spectrumlocal : this.getDBSpectrums()) {
        	if(!solventandtemp.keySet().contains(spectrumlocal.getSolvent()))
        		solventandtemp.put(spectrumlocal.getSolvent(),new ArrayList<String>());
        	if(!solventandtemp.get(spectrumlocal.getSolvent()).contains(spectrumlocal.getTemperature())) {
                builder.addDataEntity(
                        new FileEntity.FileEntityBuilder()
                          .setId("nmrshiftdb"+getMoleculeId()+"_"+spectrumlocal.getSolvent()+"_"+spectrumlocal.getTemperature()+".nmredata.sd")
                          .addProperty("name", "nmredata for molecule "+getMoleculeId())
                          .addProperty("contentSize", nmredatasize)
                          .addProperty("encodingFormat", "chemical/x-mdl-sdfile")
                          .build()
                );
                solventandtemp.get(spectrumlocal.getSolvent()).add(spectrumlocal.getTemperature());
        	}
        }
        RoCrate roCrate=builder.build();
        return roCrate;
	}
}


