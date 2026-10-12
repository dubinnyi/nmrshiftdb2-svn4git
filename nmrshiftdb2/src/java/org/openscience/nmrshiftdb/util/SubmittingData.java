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
 */package org.openscience.nmrshiftdb.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletConfig;

import org.apache.commons.codec.binary.Base64;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.jcamp.spectrum.assignments.TwoAtomsReference;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBBond;
import org.openscience.nmrshiftdb.om.DBBondDBAtom;
import org.openscience.nmrshiftdb.om.DBCanonicalName;
import org.openscience.nmrshiftdb.om.DBChemicalName;
import org.openscience.nmrshiftdb.om.DBChemicalNamePeer;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.om.DBCoordinateSet2d;
import org.openscience.nmrshiftdb.om.DBCoordinateSet2dPeer;
import org.openscience.nmrshiftdb.om.DBCoordinateSet3d;
import org.openscience.nmrshiftdb.om.DBCoordinateSet3dPeer;
import org.openscience.nmrshiftdb.om.DBCoupling;
import org.openscience.nmrshiftdb.om.DBIdentifier;
import org.openscience.nmrshiftdb.om.DBKeyword;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculeDBKeyword;
import org.openscience.nmrshiftdb.om.DBMoleculeDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBMoleculeHyperlink;
import org.openscience.nmrshiftdb.om.DBMoleculeHyperlinkPeer;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBRawFileDBSpectrum;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSignalDBAtom;
import org.openscience.nmrshiftdb.om.DBSignalPeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBCondition;
import org.openscience.nmrshiftdb.om.DBSpectrumDBKeyword;
import org.openscience.nmrshiftdb.om.DBSpectrumFile;
import org.openscience.nmrshiftdb.om.DBSpectrumHyperlink;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypeDBConditionPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumEdit;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ValuesForVelocityComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.base.CMLAttribute;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLName;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

import com.workingdogs.village.DataSetException;

import de.unikoeln.chemie.nmr.data.Peak2D;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * This class is used for holding the data of a molecule and an assigned spectrum in the session while the user is doing the sumbmit.
 * This class is mainly a wrapper round a CMLCml object which holds all data for submit. 
 *
 * @author     shk3
 * @created    4. March 2002
 */
public class SubmittingData {
  public static final String VISIBLE="visible";
  public final static String IDENTIFIER="identifier";
  public final static String MULTIPLICITY="multiplicity";
  public static final String COUPLING = "coupling";
  //The actual data
  transient public CMLCml data;
  //These attributes are only used during the submit process and do not represent actual data
  private int spectrumid = -1;
  private int moleculeid = -1;
  public boolean enterconstants = false;
  public String literaturecomplete="false";
  public String shiftlist="0";
  public NumberKey accept=null;
  private IAtomContainer moleculeWith3d;
  public int coordinatesetid3=-1;//this can be set to a coordinateSetId for 3d coordinate; the coordintes from molcooord are then saved with this id. only to be used in special cases (bulkload)
  public IAtomContainer molcoord=null;
  String keepAssignmentEntries = "";
  public DBAtom[] dbatoms = null;
  String path;
  public DBRawFile rawfile=null;//this is only used with the lab sytem to invalidate a rawfile entry after assignment
  boolean showpeakpicker=false;
  public String timeformatted;
  public int fullfillsOrder=0;
  boolean doubleBondConfiguration = false;
  public boolean assignmentGuessed=false;
  private Map<String,Integer> multiplicityMap=new HashMap<String,Integer>();
  public boolean updown;
  public int ordercolumn=-3;

  /**
   * Constructor for the SubmittingData object from a CMLCml object according to nmrshiftdb convention.
   * @param cml The data.
   */
  public SubmittingData(CMLCml cml){
	  this.data=cml;
  }
  
  /**
   * Constructor for the SubmittingData object.
   *
   * @param path The path to the normalizer configuration file.
   */
  public SubmittingData(String path) {
    this.path=path;
    data = new CMLCml();
    //this initializes the conditions vect
    setMeasured();
  }


  /**
   *Constructor for the SubmittingData object which sets choosenspectrumtype to something.
   *
   * @param  st             The wished spectrum type.
   * @param path The path to the normalizer configuration file.
   * @exception  TorqueException  Database problems.
   * @throws NmrshiftdbException 
   * @throws IOException 
   */
  public SubmittingData(DBSpectrumType st, String path) throws CDKException, TorqueException, NmrshiftdbException, IOException {
    this(path);
    setChoosenSpectrumType(st);
  }

  /**
   * Tells if the assignment is guessed (i. e. done automatically from predictions or user submitted).
   * 
   * @return true or false.
   */
  public boolean isAssignmentGuessed(){
	  return assignmentGuessed;
  }
  
  /**
   *Sets the chemName attribute of the SubmittingData object
   *
   * @param  chemName  The new chemName value
   */
  public void setChemName(String chemName) {
      StringTokenizer namest =
          new StringTokenizer( chemName,
                               "\n\r" );
	  CMLMolecule molecule =
	          (CMLMolecule)data.getChildCMLElements( "molecule" )
	                  .get( 0 );
	  for ( int i = 0; i < molecule.getNameElements().size(); i++ ) {
	      molecule.removeChild( molecule.getNameElements().get( 0 ) );
	  }
	  while ( namest.hasMoreTokens() ) {
		  String  nameString = namest.nextToken();
		  if(!nameString.trim().equals("")){
		      CMLName name = new CMLName();
		      name.setXMLContent( nameString );
		      molecule.addName( name );
		  }
	  }
  }

  /**
   * Gets the molecule part of the CMLCml in this class.
   * 
   * @return The molecule part.
   */
  public CMLMolecule getCMLMolecule(){
	  if(data.getChildCMLElements("molecule").size()>0)
		  return (CMLMolecule)data.getChildCMLElements("molecule").get(0);
	  else
		  return null;
  }

  /**
   *Sets the casNumber attribute of the SubmittingData object
   *
   * @param  casNumber  The new casNumber value
   */
  public void setCasNumber(String casNumber) {
      CMLMolecule molecule = getCMLMolecule();
      for(int k=0;k<molecule.getNameElements().size();k++){
          if(molecule.getNameElements().get(k).getAttribute("convention")!=null && molecule.getNameElements().get(k).getAttribute("convention").getValue().equals("CAS")){
              molecule.getNameElements().get(k).setXMLContent(casNumber);
              return;
          }
      }
      CMLName name = new CMLName();
      CMLAttribute att = new CMLAttribute("convention");
      att.setValue("CAS");
      name.addAttribute(att);
      name.setXMLContent(casNumber);
      molecule.addName(name);
  }


  /**
   *Sets the level attribute of the SubmittingData object
   *
   * @param  level  The new level value
   */
  public void setLevel(int level) {
      if(data.getFirstChildElement("level","http://www.nmrshiftdb.org/")==null){
          Element element = new Element("level","http://www.nmrshiftdb.org/");
          data.appendChild(element);
      }
      Attribute attribute = new Attribute("level",Integer.toString(level));
      data.getFirstChildElement("level","http://www.nmrshiftdb.org/").addAttribute(attribute);
  }


  /**
   *Sets the autonomeName attribute of the SubmittingData object
   *
   * @param  name  The new autonomeName value
   */
  public void setAutonomeName(String name) {
      CMLMolecule molecule = getCMLMolecule();
      for(int k=0;k<molecule.getNameElements().size();k++){
          if(molecule.getNameElements().get(k).getAttribute("convention")!=null && molecule.getNameElements().get(k).getAttribute("convention").getValue().equals("autonome")){
              molecule.getNameElements().get(k).setXMLContent(name);
              return;
          }
      }
      CMLName cmlname = new CMLName();
      CMLAttribute att = new CMLAttribute("convention");
      att.setValue("autonome");
      cmlname.addAttribute(att);
      cmlname.setXMLContent(name);
      molecule.addName(cmlname);
  }


  /**
   *  Sets the doubleBondConfiguration attribute of the SubmittingData object
   *
   * @param  bool  The new doubleBondConfiguration value
   */
  public void setDoubleBondConfiguration(boolean bool) {
    doubleBondConfiguration = bool;
  }


  /**
   *  Sets the isDoubleBondSpecified attribute of the SubmittingData object
   *
   * @param  isDoubleBondSpecified  The new isDoubleBondSpecified value
   * @throws CDKException 
   * @throws IOException 
   */
  public void setIsDoubleBondSpecified(boolean[] isDoubleBondSpecified) throws CDKException, IOException {
      IAtomContainer molwithh = getMolWithH();
      for(int k=0;k<molwithh.getBondCount();k++){
    	  Elements scalars = getCMLMolecule().getBonds().get(k).getChildElements("scalar");
    	  for(int i=0;i<scalars.size();i++){
    		  if(((CMLScalar)scalars.get(i)).getTitle().equals("doublebondconfiguration"))
    			  getCMLMolecule().getBonds().get(k).removeChild(scalars.get(i));
    	  }
          if(isDoubleBondSpecified[k]){
        	  CMLScalar scalar= new CMLScalar();
        	  scalar.setTitle("doublebondconfiguration");
        	  scalar.setDataType("xsd:string");
        	  scalar.setValue("true");
        	  getCMLMolecule().getBonds().get(k).appendChild(scalar);
          }
      }
  }


  /**
   *Sets the comment attribute of the SubmittingData object
   *
   * @param  comment  The new comment value
   */
  public void setComment(String comment) {
      if(comment==null)
          return;
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      if(spectrum.getMetadataListElements().size()==0){
          spectrum.addMetadataList(new CMLMetadataList());
      }
      if(GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(),"nmr:comment").size()>0){
	      GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(),"nmr:comment").get(0).setContent(comment);
      }else{
          CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
          CMLMetadata md = new CMLMetadata();
          md.setName("nmr:comment");
          md.setContent(comment);
          mlist.addMetadata(md);
      }
  }


  /**
   *Sets the literature attribute of the SubmittingData object
   *
   * @param  literature  The new literature value
   * @throws TorqueException 
   * @throws TooManyRowsException 
   * @throws NoRowsException 
   */
  public void setLiterature(List<DBLiterature> literature) throws NoRowsException, TooManyRowsException, TorqueException {
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      Elements els=data.getChildCMLElements("spectrum").get(0).getChildElements("entry","http://bibtexml.sf.net/");
      for(int k=0;k<els.size();k++){
          data.getChildCMLElements("spectrum").get(0).removeChild(els.get(k));
      }
      for(int i=0;i<literature.size();i++){
          data.getChildCMLElements("spectrum").get(0).appendChild(literature.get(i).getAsBibtex());
      }
  }


  /**
   *Sets the conditions attribute of the SubmittingData object, needs to be a ArrayList of DBConditonType objects
   *
   * @param  conds  The new conditions value
   */
  public void setConditions(List<DBConditionType> conds) {
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      for (int l = 0; l < conds.size(); l++) {
          if(conds.get(l).getCmlEntryType().equals("metadata")){
              if(spectrum.getMetadataListElements().size()==0)
                  spectrum.addMetadataList(new CMLMetadataList());

              if(GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(),conds.get(l).getDictRef()).size()>0){
            	  GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(),conds.get(l).getDictRef()).get(0).setContent(conds.get(l).getCurrentValue());
              }else{
                  CMLMetadata metadata = new CMLMetadata();
                  metadata.setName(conds.get(l).getDictRef());
                  metadata.setContent(conds.get(l).getCurrentValue());
                  spectrum.getMetadataListElements().get(0).addMetadata(metadata);
              }
          } else if(conds.get(l).getCmlEntryType().equals("condition")){
              if(spectrum.getConditionListElements().size()==0)
                  spectrum.addConditionList(new CMLConditionList());
              CMLElements<CMLScalar> scalars = spectrum.getConditionListElements().get(0).getScalarElements();
              boolean set = false;
              for(int i=0;i<scalars.size();i++){
                  if(scalars.get(i).getDictRef().equals(conds.get(l).getDictRef())){
                      scalars.get(i).setValue(conds.get(l).getCurrentValue());
                      set=true;
                  }
              }
              if(!set){
                  CMLScalar scalar = new CMLScalar();
                  scalar.setDictRef(conds.get(l).getDictRef());
                  scalar.setValue(conds.get(l).getCurrentValue());
                  spectrum.getConditionListElements().get(0).addScalar(scalar);
              }
          } else if(conds.get(l).getCmlEntryType().equals("substance")){
              if(spectrum.getChildCMLElements("substanceList").size()==0)
                  spectrum.appendChild(new CMLSubstanceList());
              CMLElements<CMLSubstance> substances = ((CMLSubstanceList)spectrum.getChildCMLElement("substanceList", 0)).getSubstanceElements();
              boolean set = false;
              for(int i=0;i<substances.size();i++){
                  if(((CMLSubstance)substances.get(i)).getDictRef().equals(conds.get(l).getDictRef())){
                      ((CMLSubstance)substances.get(i)).setTitle(conds.get(l).getCurrentValue());
                      set=true;
                  }                  
              }              
              if(!set){
                  CMLSubstance scalar = new CMLSubstance();
                  scalar.setDictRef(conds.get(l).getDictRef());
                  scalar.setTitle(conds.get(l).getCurrentValue());
                  spectrum.getChildCMLElement("substanceList", 0).appendChild(scalar);
              }
          }
      }
  }


  /**
   *Sets the moleculeLink attribute of the SubmittingData object
   *
   * @param  link  The new moleculeLink value
   */
  public void setMoleculeLinks(String link) {
      if(link.equals(""))
          return;
      CMLMolecule molecule=((CMLMolecule)data.getChildCMLElements("molecule").get(0));
      if(molecule.getChildCMLElements("metadataList").size()==0){
          molecule.appendChild(new CMLMetadataList());
      }
      CMLMetadataList mlist=(CMLMetadataList)molecule.getChildCMLElements("metadataList").get(0);
      List<CMLMetadata> metadatas=GeneralUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"nmr:link");
      CMLMetadata md = new CMLMetadata();
      md.setName("nmr:link");
      if(metadatas.size()>0)
          md=metadatas.get(0);
      md.setContent(link);
      mlist.addMetadata(md);
  }


  /**
   *Sets the spectrumLink attribute of the SubmittingData object
   *
   * @param  link  The new spectrumLink value
   */
  public void setSpectrumLinks(String link) {
      if(link==null || link.equals(""))
          return;
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      if(spectrum.getMetadataListElements().size()==0){
          spectrum.addMetadataList(new CMLMetadataList());
      }
      CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
      List<CMLMetadata> metadatas=GeneralUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"nmr:link");
      CMLMetadata md = new CMLMetadata();
      md.setName("nmr:link");
      if(metadatas.size()>0)
          md=metadatas.get(0);
      md.setContent(link);
      mlist.addMetadata(md);
  }


  /**
   *Sets the signalstable attribute of the SubmittingData object
   *
   * @param  v  The new signalstable value
   */

  public void setSignalstable(CMLPeakList v) {
	  if(data.getChildCMLElements("spectrum").size()==0)
		  data.appendChild(new CMLSpectrum());
	  if(data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").size()>0)
		  data.getChildCMLElements("spectrum").get(0).replaceChild(data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0), v);
	  else
		  data.getChildCMLElements("spectrum").get(0).appendChild(v);
  }


  /**
   *Sets the keepAssignmentEntries attribute of the SubmittingData object
   *
   * @param  b  The new keepAssignmentEntries value
   */
  public void setKeepAssignmentEntries(String b) {
    keepAssignmentEntries = b;
  }


  /**
   *Sets the measured attribute of the SubmittingData object
   */
  public void setMeasured() {
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum = (CMLSpectrum) data.getChildCMLElements("spectrum").get(0);
      if(spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/")==null){
          Element element = new Element("morc","http://www.nmrshiftdb.org/");
          spectrum.appendChild(element);
      }
      Attribute attribute = new Attribute("morc","m");
      spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/").addAttribute(attribute);
  }


  /**
   *Sets the calculated attribute of the SubmittingData object
   */
  public void setCalculated() {
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum = (CMLSpectrum) data.getChildCMLElements("spectrum").get(0);
      if(spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/")==null){
          Element element = new Element("morc","http://www.nmrshiftdb.org/");
          spectrum.appendChild(element);
      }
      Attribute attribute = new Attribute("morc","c");
      spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/").addAttribute(attribute);
  }
  
  /**
   * Gives a string containing all set double bond configurations for display in ui (like x to y, z to...).
   * 
   * @return The string.
   * @throws CDKException
 * @throws IOException 
   */
  public String getConfigurationString() throws CDKException, IOException{
      StringBuffer doubleBondConfigurationDisplay = new StringBuffer();
      IAtomContainer mol = getMolWithH();
      if (getDoubleBondConfiguration()) {
          for (int i = 0; i < mol.getBondCount(); i++) {
            if (getIsDoubleBondSpecified()[i]) {
              doubleBondConfigurationDisplay.append((mol.getAtomNumber(mol.getBond(i).getAtom(0)) + 1) + " to " + (mol.getAtomNumber(mol.getBond(i).getAtom(1)) + 1) + ";");
            }
          }
      }
      return GeneralUtils.removeLastComma(doubleBondConfigurationDisplay).toString();
  }
  
  /**
   * Gives a string containing unassigned singals for display in ui.
   * 
   * @return The string.
   */
  public String getUnassignedSignals(){
	  StringBuffer unassigned=new StringBuffer();
	  for(int i=0;i<this.getSignalstable().getPeakElements().size();i++){
		  if(this.getSignalstable().getPeakElements().get(i).getAtomRefs()==null || this.getSignalstable().getPeakElements().get(i).getAtomRefs().length==0)
			  unassigned.append(this.getSignalstable().getPeakElements().get(i).getXValue()+"; ");
	  }
	  if(!unassigned.toString().equals("")){
		  return "Unassigned shifts: "+GeneralUtils.removeLastComma(unassigned);
	  }else{
		  return "";
	  }
  }
  
  public String getMultiplicity(int atomid){
      for(int i=0;i<this.getSignalstable().getPeakElements().size();i++){
          if(isAssignedToAtom(this.getSignalstable().getPeakElements().get(i), atomid+1) && this.getSignalstable().getPeakElements().get(i).getPeakMultiplicity()!=null)
              return this.getSignalstable().getPeakElements().get(i).getPeakMultiplicity();
      }
      return "";
  }
  
  public String getShift(int atomid){
      for(int i=0;i<this.getSignalstable().getPeakElements().size();i++){
          if(isAssignedToAtom(this.getSignalstable().getPeakElements().get(i), atomid+1))
              return Double.toString(this.getSignalstable().getPeakElements().get(i).getXValue());
      }
      return "";
  }
  
  public CMLPeak getPeak(int atomid){
      for(int i=0;i<this.getSignalstable().getPeakElements().size();i++){
          if(isAssignedToAtom(this.getSignalstable().getPeakElements().get(i), atomid+1))
              return this.getSignalstable().getPeakElements().get(i);
      }
      return null;
  }
  
  public String getShiftH(int atomid, int hposition) throws CDKException, IOException{
      IAtomContainer mol=getMolWithH();
      if(mol==null || atomid>=mol.getAtomCount())
          return "";
      List<IAtom> connatoms = mol.getConnectedAtomsList(mol.getAtom(atomid));
      boolean secondh=false;
      IAtom foundatom=null;
      for(IAtom atom : connatoms){
          if(atom.getSymbol().equals("H")){
              if(hposition==1){
                  foundatom=atom;
                  break;
              }
              if(hposition==2 && secondh==true){
                  foundatom=atom;
                  break;
              }
              if(hposition==2 && secondh==false){
                  secondh=true;
              }
          }
      }
      if(foundatom==null)
          return ""; //since for quickcheck initally fields are shown which do not correspond to a H, we can have that situation
      int hindex=mol.getAtomNumber(foundatom);
      return getShift(hindex);
  }
  
  /**
   * Tells if a certain peak is assigned to a certain atom.
   * 
   * @param peak The peak.
   * @param atomnumber The number of the atom (starting with 1!).
   * @return Is the peak assigned to atom?
   */
  public boolean isAssignedToAtom(CMLPeak peak, int atomnumber){
      if(peak.getAtomRefs()!=null && ArrayUtils.contains(peak.getAtomRefs(),"a"+(atomnumber))>=0)
          return true;
      else
          return false;
  }
  

  /**
   * Gets the shifts in the CMLCml object as a list of ValuesForVelocityBeans. This is used for displaying shift tables in submit page, sorted ascending by atom number. 
   * 
   * @return The shifts.
   * @throws CDKException 
   * @throws NmrshiftdbException 
   * @throws TorqueException 
   * @throws NumberFormatException 
   * @throws IOException 
   */
  public List<ValuesForVelocityBean> getConfirmShifts() throws NumberFormatException, TorqueException, NmrshiftdbException, CDKException, IOException{
	  return getConfirmShifts(-1,false);
  }
  

    /**
     * Gets the shifts in the CMLCml object as a list of ValuesForVelocityBeans. This is used for displaying shift tables in submit page.
     * 
     * @param ordercolumn The column to sort by (-1=atom numbers, 1=shifts).
     * @param updown true=sort descending, false=sort ascending.
     * @return The shifts.
     * @throws CDKException 
     * @throws NmrshiftdbException 
     * @throws TorqueException 
     * @throws NumberFormatException 
     * @throws IOException 
     */
    public List<ValuesForVelocityBean> getConfirmShifts(int ordercolumn, boolean updown) throws NumberFormatException, TorqueException, NmrshiftdbException, CDKException, IOException{
        ArrayList<ValuesForVelocityBean> atomsForVelocity = new ArrayList<ValuesForVelocityBean>();
        if (getChoosenSpectrumType() == null || getMolOriginal()==null)
            return atomsForVelocity;
        IAtomContainer mol = getMolWithH();
        Map<Integer,String> secondCoupling=new HashMap<Integer,String>();
        for (int i = 0; i < mol.getAtomCount(); i++) {
            if ((getChoosenSpectrumType() != null && (!getChoosenSpectrumType().getName().equals("1H") && mol.getAtom(i).getSymbol().equals(this.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol())) || (this.getChoosenSpectrumType().getName().equals("1H") && !mol.getAtom(i).getSymbol().equals("H")))) {
                StringBuffer displayText = new StringBuffer();
                if (this.getIdentifiers().get(new Integer(i)) != null)
                    displayText.append((String) this.getIdentifiers().get(new Integer(i)));
                else
                    displayText.append(Integer.toString(i + 1));
                String shifts = "";
                float shiftsFloat = 0;
                List<IAtom> connatoms = mol.getConnectedAtomsList(mol.getAtom(i));
                if (getChoosenSpectrumType().getElementSymbol().equals("H")) {
                    displayText.append("-H");
                    boolean hvisible=false;
                    String hstring="";
                    for (int k = 0; k < connatoms.size(); k++) {
                        if (((IAtom) connatoms.get(k)).getSymbol().equals("H")) {
                            hstring = hstring + "H " + (mol.getAtomNumber((IAtom) connatoms.get(k)) + 1) + " ";
                            shifts = shifts + " " + AtomUtils.getValuesForAtom(mol.getAtomNumber((IAtom) connatoms.get(k)), this.getSignalstable());
                            if (!AtomUtils.getValuesForAtom(mol.getAtomNumber((IAtom) connatoms.get(k)), this.getSignalstable()).equals(""))
                                shiftsFloat = Float.parseFloat(AtomUtils.getValuesForAtom(mol.getAtomNumber((IAtom) connatoms.get(k)), this.getSignalstable()));
                            if(((IAtom) connatoms.get(k)).getProperty(SubmittingData.VISIBLE)!=null && ((IAtom) connatoms.get(k)).getProperty(SubmittingData.VISIBLE).equals("true"))
                                hvisible=true;
                        }
                    }
                    if(hvisible)
                        displayText.append(" ("+hstring.trim()+")");
                } else {
                    shifts = AtomUtils.getValuesForAtom(i, this.getSignalstable());
                    if (!AtomUtils.getValuesForAtom(i, this.getSignalstable()).equals(""))
                        shiftsFloat = Float.parseFloat(AtomUtils.getValuesForAtom(i, this.getSignalstable()));
                }
                atomsForVelocity.add(new ValuesForVelocityBean(new Float(i), "", 0, 0, shifts, displayText.toString()));
                atomsForVelocity.get(atomsForVelocity.size() - 1).setShift(1, 0, shiftsFloat);
                if (this.getIdentifiers().get(new Integer(i)) != null)
                    ((ValuesForVelocityBean) atomsForVelocity.get(atomsForVelocity.size() - 1)).setIdentifier((String) this.getIdentifiers().get(new Integer(i)));
                else
                    ((ValuesForVelocityBean) atomsForVelocity.get(atomsForVelocity.size() - 1)).setIdentifier(Integer.toString(i + 1));
                StringBuffer multi = new StringBuffer("");
                if (mol.getAtom(i).getSymbol().equals(this.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol()) && mol.getAtom(i).getProperty(MULTIPLICITY) != null)
                    multi.append((String) mol.getAtom(i).getProperty(MULTIPLICITY));
                for (int k = 0; k < connatoms.size(); k++) {
                    if (((IAtom) connatoms.get(k)).getSymbol().equals("H") && ((IAtom) connatoms.get(k)).getProperty(MULTIPLICITY) != null) {
                        multi.append((String) ((IAtom) connatoms.get(k)).getProperty(MULTIPLICITY) + ";");
                    }
                }
                multi = GeneralUtils.removeLastComma(multi);
                StringBuffer couplings = new StringBuffer();
                couplings.append(" (");
                for (int k = 0; k < this.getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").size(); k++) {
                    couplings.append(this.getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(k).getAttribute("value").getValue() + ";");
                    secondCoupling.put(Integer.parseInt(this.getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(k).getAttribute("otherAtom").getValue()),this.getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(k).getAttribute("value").getValue());
                }
                for (int k = 0; k < connatoms.size(); k++) {
                    if (((IAtom) connatoms.get(k)).getSymbol().equals("H")) {
                        int number = mol.getAtomNumber(((IAtom) connatoms.get(k)));
                        if (this.getCMLMolecule().getAtom(number) != null) {
                            for (int l = 0; l < this.getCMLMolecule().getAtom(number).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").size(); l++) {
                                couplings.append(this.getCMLMolecule().getAtom(number).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(l).getAttribute("value").getValue() + ";");
                                secondCoupling.put(mol.getAtomNumber(mol.getConnectedAtomsList(mol.getAtom(Integer.parseInt(this.getCMLMolecule().getAtom(number).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(l).getAttribute("otherAtom").getValue()))).get(0)),this.getCMLMolecule().getAtom(number).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(l).getAttribute("value").getValue());
                            }
                        }
                    }
                }
                multi.append(couplings.toString());
                ((ValuesForVelocityBean) atomsForVelocity.get(atomsForVelocity.size() - 1)).setMultiplicity(multi.toString());
            }
        }
        for(int atomnumber : secondCoupling.keySet()){
        	((ValuesForVelocityBean) atomsForVelocity.get(atomnumber)).setMultiplicity(((ValuesForVelocityBean) atomsForVelocity.get(atomnumber)).getMultiplicity()+secondCoupling.get(atomnumber)+";");
        }
        for(ValuesForVelocityBean vfvb : atomsForVelocity){
        	if(vfvb.getMultiplicity().charAt(vfvb.getMultiplicity().length()-1)==';')
        		vfvb.setMultiplicity(vfvb.getMultiplicity().substring(0,vfvb.getMultiplicity().length()-1));
        	vfvb.setMultiplicity(vfvb.getMultiplicity().substring(0,vfvb.getMultiplicity().length())+")");
            if (vfvb.getMultiplicity().contains(" ()")){
            	vfvb.setMultiplicity(vfvb.getMultiplicity().substring(0,vfvb.getMultiplicity().length()-3));
            }
        }
        Collections.sort(atomsForVelocity, new ValuesForVelocityComparator(ordercolumn, updown, 1));
        return atomsForVelocity;
    }


  /**
   * Sets the identifier of the ith atom in the molecule.
   *
   * @param  i  The atom number.
   * @param  identifier The new identifier.
   * @throws CDKException 
   */
  public void addIdentifier(Integer i, String identifier) throws CDKException {
	  AtomUtils.setChildElement(getCMLMolecule().getAtom(i), IDENTIFIER, identifier);
  }
  
  /**
   * Removes the identifier of the ith atom in the molecule.
   *
   * @param  i  The atom number.
   * @throws CDKException 
   */
  public void removeIdentifier(Integer i) throws CDKException {
      if(getCMLMolecule().getAtom(i).getChildElements(IDENTIFIER).size()>0){
          getCMLMolecule().getAtom(i).removeChild(getCMLMolecule().getAtom(i).getChildElements(IDENTIFIER).get(0));
      }
  }


  /**
   * Sets the choosenSpectrumType attribute of the SubmittingData object
   *
   * @param  spectype       The new choosenSpectrumType value
   * @throws Exception 
   * @throws CDKException 
   * @throws NmrshiftdbException 
   * @throws IOException 
   */
  public void setChoosenSpectrumType(DBSpectrumType spectype) throws CDKException, TorqueException, NmrshiftdbException, IOException {
	  if(data.getChildCMLElements("spectrum").size()==0)
	      data.appendChild(new CMLSpectrum());
	  if(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().size()==0)
	      ((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).addMetadataList(new CMLMetadataList());
      if(GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").size()==0){
          CMLMetadata md=new CMLMetadata();
          md.setName("nmr:OBSERVENUCLEUS");
          ((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).addMetadata(md);
      }
	  GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").get(0).setContent(spectype.getName());
      if(getMolWithH()!=null && spectype.getDBIsotopes().size()>0){
        IAtomContainer mol = getMolWithH();
        for (int i = 0; i < mol.getAtomCount(); i++) {
          if (mol.getAtom(i).getSymbol().equals(spectype.getDBIsotope(1).getElementSymbol())) {
            if (!spectype.getDBIsotope(1).getElementSymbol().equals("H") && getCMLMolecule().getAtoms().get(i).getChildElements(SubmittingData.MULTIPLICITY).size()==0) {
            	try{
                    AtomUtils.setChildElement(getCMLMolecule().getAtoms().get(i), SubmittingData.MULTIPLICITY, GeneralUtils.getMultiplicity(mol.getAtom(i), mol, false));
            	}catch(Exception ex){
            		//we get npes here for unclear reasons. a bit dirty like this, but no time to do it better
            	}
            }
          }
        }
      }
      if(GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").size()==0){
          List<DBConditionType> conds = getConditions();
          for(int i=0;i<conds.size();i++){
            DBConditionType cond=conds.get(i);
            if(cond.getConditionTypeId()==3 && !conds.get(i).getCurrentValue().equals("") && !conds.get(i).getCurrentValue().equals(NmrshiftdbConstants.UNREPORTED) && !conds.get(i).getCurrentValue().equals(NmrshiftdbConstants.UNKNOWN)){
                float frequency1H=Float.parseFloat(conds.get(i).getCurrentValue());
                conds.get(i).setCurrentValue(Double.toString(frequency1H/spectype.getFrequencyRatio()));
            }
          }
          setConditions(conds);
      }
  }

  /**
   * Sets the multiplicity of a certain atom in the CMLCml data.
   * 
   * @param atomNumber The atom to set.
   * @param multiplicity The multiplicity to set.
   */
  public void setMultiplicity(int atomNumber, String multiplicity){
	AtomUtils.setChildElement(getCMLMolecule().getAtoms().get(atomNumber), SubmittingData.MULTIPLICITY, multiplicity);
  }
  
  /**
   * Sets all molecule related attributes, taking the hydrogens from input.
   *
   * @param  original		        The molecule with all Hs
   * @param  checkOnlyOneStructure  Perform check if it is only one structure.
   * @param  changeOrder            true=Change oder of atoms to cs go first, Hs last, false=leave as is.
   * @param  no3d                   generate 2d coordinates from 3d if true.
   * @return                        0=all OK, 1=empty file, 2=NO2 group replaced, 3=multiple structures.
   * @exception  CDKException       Description of Exception.
   * @exception  IOException        Description of Exception.
   * @exception  ClassNotFoundException          Description of Exception.
   * @throws TorqueException 
   */
  public int setMol(IAtomContainer original, boolean checkOnlyOneStructure, boolean changeOrder, boolean no3d) throws CDKException, IOException, ClassNotFoundException, TorqueException, DataSetException {

	  
    if (original.getAtomCount() == 0) {
	      return (1);
    }
    if (checkOnlyOneStructure && !ConnectivityChecker.isConnected(original)) {
	      return (3);
    }
    if(no3d){
	      for(int i=0;i<original.getAtomCount();i++){
	        if(original.getAtom(i).getPoint2d()==null)
	        	original.getAtom(i).setPoint2d(new javax.vecmath.Point2d(original.getAtom(i).getPoint3d().x,original.getAtom(i).getPoint3d().y));
	      }
	}
    if (getEditSpectrum() == 0 && changeOrder) {
    	original=AtomUtils.rearangeAtoms(original);
    }
  	for(int i=0;i<original.getAtomCount();i++){
  		if(original.getAtom(i).getProperty(VISIBLE)==null)
  			original.getAtom(i).setProperty(VISIBLE,"true");
  	}
    AtomUtils.addAndPlaceHydrogens(original);
    boolean found=setMolWithH(original);
    if (found) {
      return (2);
    } else {
      return (0);
    }
  }  
  
  /**
   * Sets all molecule related attributes, adding hydrogend automically.
   *
   * @param  original		        The molecule as entred in mol format.
   * @param  checkOnlyOneStructure  Perform check if it is only one structure.
   * @param  changeOrder            true=Change oder of atoms to cs go first, Hs last, false=leave as is.
   * @param  no3d                   generate 2d coordinates from 3d if true
   * @return                        0=all OK, 1=empty file, 2=NO2 group replaced, 3=multiple structures.
   * @exception  CDKException       Description of Exception.
   * @exception  IOException        Description of Exception.
   * @exception  ClassNotFoundException          Description of Exception.
   */
  public int setMol(String original, boolean checkOnlyOneStructure, boolean changeOrder, boolean no3d) throws CDKException, IOException, ClassNotFoundException, TorqueException, DataSetException {
      IAtomContainer mol = AtomUtils.readMolBothVersions(original);
      for(IBond bond : mol.bonds()) {
        if(bond.getOrder()==null)
          bond.setOrder(IBond.Order.ZERO);
      }
      if(AtomUtils.isOrganometallic(mol)) {
         AtomUtils.doSimpleSaturation(mol,null);
         AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(mol);
      }else {
     	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
      	Isotopes.getInstance().configureAtoms(mol);
        ElectronDonation model       = ElectronDonation.cdk();
        CycleFinder      cycles      = Cycles.cdkAromaticSet();
        Aromaticity      aromaticity = new Aromaticity(model, cycles);
    	aromaticity.apply(mol);
      }
      int result = setMol(mol, checkOnlyOneStructure, changeOrder, no3d);
      //We read atom labels
      StringTokenizer st=new StringTokenizer(original, "\n");
      while(st.hasMoreTokens()){
          String line=st.nextToken();
          if(line.startsWith("A  ")){
              //the atom labels start with 1
              int atomnumber=Integer.parseInt(line.substring(3).trim())-1;
              String label=st.nextToken().trim();
              this.addIdentifier(atomnumber, label);
    	  }else if(line.startsWith("M  ZZC")){
              StringTokenizer st2=new StringTokenizer(line, " ");
              st2.nextToken();
              st2.nextToken();
              int atomnumber=Integer.parseInt(st2.nextToken())-1;
              String label=st2.nextToken().trim();
              this.addIdentifier(atomnumber, label);
          }
      }      
      return result;
  }
  
  
  /**
   * Sets the molWithH and the molWithHCount attribute of the SubmittingData object
   *
   * @param  molWithH       The new molWithH value
   * @return                Description of the Returned Value
   * @throws TorqueException 
   * @throws DataSetException 
   * @exception  Exception  Description of Exception
   */
  private boolean setMolWithH(IAtomContainer molOriginal) throws CDKException, TorqueException, DataSetException {
    if(AtomUtils.isOrganometallic(molOriginal)) {
        AtomUtils.doSimpleSaturation(molOriginal,null);
        AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(molOriginal);
    }else {
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molOriginal);
	    ElectronDonation model       = ElectronDonation.cdk();
	    CycleFinder      cycles      = Cycles.cdkAromaticSet();
	    Aromaticity      aromaticity = new Aromaticity(model, cycles);
	    aromaticity.apply(molOriginal);
    }
    Convertor convertor =new Convertor(true, null);
    CMLMolecule cmlmol = convertor.cdkAtomContainerToCMLMolecule(molOriginal);
    if(data.getChildCMLElements("molecule").size()>0){
		data.removeChild( data.getChildCMLElements("molecule").get( 0 ) );
    }
    data.appendChild(cmlmol);
    boolean found = AtomUtils.normalize(molOriginal,path);
    return (found);
  }


  /**
   *  Sets the editSpectrum attribute of the SubmittingData object
   *
   * @param  spec  The new editSpectrum value
   */
  public void setEditSpectrum(int spec) {
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      if(spectrum.getChildCMLElements("metadataList").size()==0){
          spectrum.appendChild(new CMLMetadataList());
      }
      CMLMetadataList mlist=(CMLMetadataList)spectrum.getChildCMLElements("metadataList").get(0);
      List<CMLMetadata> metadatas=GeneralUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),NmrshiftdbConstants.nmrid);
      CMLMetadata md = new CMLMetadata();
      md.setName(NmrshiftdbConstants.nmrid);
      if(metadatas.size()>0)
          md=metadatas.get(0);
      md.setContent(Integer.toString(spec));
      mlist.addMetadata(md);
  }


  /**
   * Sets the moleculeKeywords attribute of the SubmittingData object
   *
   * @param  kw  The new moleculeKeywords value
   */
  public void setMoleculeKeywords(String kw) {
      CMLMolecule molecule=((CMLMolecule)data.getChildCMLElements("molecule").get(0));
      if(molecule.getChildCMLElements("metadataList").size()==0){
          molecule.appendChild(new CMLMetadataList());
      }
      CMLMetadataList mlist=(CMLMetadataList)molecule.getChildCMLElements("metadataList").get(0);
      List<CMLMetadata> metadatas=GeneralUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"nmr:keyword");
      CMLMetadata md = new CMLMetadata();
      md.setName("nmr:keyword");
      if(metadatas.size()>0)
          md=metadatas.get(0);
      md.setContent(kw);
      mlist.addMetadata(md);
  }


  /**
   * Sets the spectrumKeywords attribute of the SubmittingData object
   *
   * @param  kw  The new spectrumKeywords value
   */
  public void setSpectrumKeywords(String kw) {
      if(kw==null || kw.equals(""))
          return;
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      if(spectrum.getMetadataListElements().size()==0){
          spectrum.addMetadataList(new CMLMetadataList());
      }
      CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
      List<CMLMetadata> metadatas=GeneralUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"nmr:keyword");
      CMLMetadata md = new CMLMetadata();
      md.setName("nmr:keyword");
      if(metadatas.size()>0)
          md=metadatas.get(0);
      md.setContent(kw);
      mlist.addMetadata(md);
  }


  /**
   * Gets the identifiers set in the molecule.
   *
   * @return    The identifier values.
   * @throws CDKException 
   */
  public Map<Integer, String> getIdentifiers() throws CDKException {
      Map<Integer, String> ids = new HashMap<Integer, String>();
      if(getCMLMolecule()==null)
    	  return ids;
      for(int i=0;i<getCMLMolecule().getAtomCount();i++){
    	  if(getCMLMolecule().getAtom(i).getChildElements(IDENTIFIER).size()>0 && !getCMLMolecule().getAtom(i).getFirstChildElement(IDENTIFIER).getAttribute("value").getValue().equals(""))
              ids.put(new Integer(i), getCMLMolecule().getAtom(i).getFirstChildElement(IDENTIFIER).getAttribute("value").getValue());
      }
      return (ids);
  }


  /**
   *  Gets the measuredOrCalculated attribute of the SubmittingData object
   *
   * @return    The measuredOrCalculated value
   */
  public char getMeasuredOrCalculated() {
      if(data.getChildCMLElements("spectrum").size()==0)
          return 'm';
      CMLSpectrum spectrum = (CMLSpectrum) data.getChildCMLElements("spectrum").get(0);
      if(spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/")==null)
          return 'm';
      return spectrum.getFirstChildElement("morc","http://www.nmrshiftdb.org/").getAttribute("morc").getValue().charAt(0);
  }

  
  /**
   * Gives a spectrum string in the format required for the tooltips in the JCP view applet.
   * 
   * @return The spectrum string.
   * @throws CDKException
   * @throws IOException 
   */
  public String getTooltipString() throws CDKException, IOException{
	  StringBuffer tooltips = new StringBuffer();
	  IAtomContainer molWithH = getMolWithH();
	  if(getSignalstable()!=null){
    	  for(CMLPeak peak : getSignalstable().getPeakChildren()){
    	      if(peak.getAtomRefs()!=null){
        		  for(String atom : peak.getAtomRefs()){
        			  if(AtomUtils.getAtomById(molWithH, atom).getSymbol().equals("H"))
        				  tooltips.append((molWithH.getAtomNumber(molWithH.getConnectedAtomsList(AtomUtils.getAtomById(molWithH, atom)).get(0)) + 1) + "|" + peak.getXValue() + "|");
        			  else
        				  tooltips.append((molWithH.getAtomNumber(AtomUtils.getAtomById(molWithH, atom)) + 1) + "|" + peak.getXValue() + "|");
        		  }
    		  }
              }
	  }
	  return tooltips.toString();
  }

  /**
   *  Gets the doubleBondConfiguration attribute of the SubmittingData object
   *
   * @return    The doubleBondConfiguration value
   */
  public boolean getDoubleBondConfiguration() {
    return (doubleBondConfiguration);
  }


  /**
   *  Gets the isDoubleBondSpecified attribute of the SubmittingData object
   *
   * @return    The isDoubleBondSpecified value
   */
  public boolean[] getIsDoubleBondSpecified() {
      CMLMolecule cmlmol = (CMLMolecule)data.getChildCMLElements("molecule").get(0);
      boolean[] doublebondconfigurationsprov=new boolean[cmlmol.getBondCount()];
      for(int k=0;k<cmlmol.getBondCount();k++){
          CMLBond bond=cmlmol.getBonds().get(k);
    	  Elements scalars = bond.getChildCMLElements("scalar");
    	  for(int i=0;i<scalars.size();i++){
    		  if(((CMLScalar)scalars.get(i)).getTitle().equals("doublebondconfiguration") && ((CMLScalar)scalars.get(i)).getValue().equals("true"))
    			  doublebondconfigurationsprov[k]=true;
          }
      }
      return (doublebondconfigurationsprov);
  }


  /**
   *  Gets the editSpectrum attribute of the SubmittingData object
   *
   * @return    The editSpectrum value
   */
  public int getEditSpectrum() {
	  if(data.getChildCMLElements("spectrum").size()>0 && ((CMLSpectrum)data.getChildCMLElement("spectrum", 0)).getMetadataListElements().size()>0 && GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElement("spectrum", 0)).getMetadataListElements().get(0).getMetadataDescendants(), NmrshiftdbConstants.nmrid).size()>0)
		return Integer.parseInt(GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElement("spectrum", 0)).getMetadataListElements().get(0).getMetadataDescendants(), NmrshiftdbConstants.nmrid).get(0).getContent());
	  else
		return 0;
  }

  
  /**
   * Tells the review flag of the spectrum currently edited, or null if no edit.
   * 
   * @return The review flag or null
   * @throws NoRowsException
   * @throws TooManyRowsException
   * @throws TorqueException
   */
  public String getEditSpectrumFlag() throws NoRowsException, TooManyRowsException, TorqueException{
      if(getEditSpectrum()==0)
          return null;
      else
          return DBSpectrumPeer.retrieveByPK(getEditSpectrum()).getReviewFlag();
  }

  /**
   * Gets the Keywords of the molecule in data.
   *
   * @return    The molecule Keywords.
   */
  public String getMoleculeKeywords() {
      if(data.getChildCMLElements("molecule").size()>0){
          CMLMolecule molecule=((CMLMolecule)data.getChildCMLElements("molecule").get(0));
          if(molecule.getChildCMLElements("metadataList").size()>0){
              CMLMetadataList mlist=(CMLMetadataList)molecule.getChildCMLElements("metadataList").get(0);
              List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
              for(int k=0;k<metadatas.size();k++){
                  if(metadatas.get(k).getName()!=null && metadatas.get(k).getName().equals("nmr:keyword") && metadatas.get(k).getContent()!=null)
                      return metadatas.get(k).getContent();
              }
          }
      }
      return "";
  }

  /**
   * Gets the Keywords in the spectrum in data.
   *
   * @return    The spectrum Keywords.
   */
  public String getSpectrumKeywords() {
      if(data.getChildCMLElements("spectrum").size()>0){
          CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
          if(spectrum.getMetadataListElements().size()>0){
              CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
              List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
              for(int k=0;k<metadatas.size();k++){
                  if(metadatas.get(k).getName()!=null && metadatas.get(k).getName().equals("nmr:keyword") && metadatas.get(k).getContent()!=null)
                      return metadatas.get(k).getContent();
              }
          }
      }
      return "";
   }


  /**
   * Gets the autonomeName in data, "" if not set.
   *
   * @return    The autonomeName value
   */
  public String getAutonomeName() {
      CMLMolecule molecule = getCMLMolecule();
      if(molecule!=null){
	      for(int k=0;k<molecule.getNameElements().size();k++){
	          if(molecule.getNameElements().get(k).getAttribute("convention")!=null && molecule.getNameElements().get(k).getAttribute("convention").getValue().equals("autonome")){
	              return molecule.getNameElements().get(k).getXMLContent();
	          }
	      }
      }
      return "";
  }


  /**
   *Gets the comment in data, "" if not set.
   *
   * @return    The comment value
   */
  public String getComment() {
      if(data.getChildCMLElements("spectrum").size()>0){
          CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
          if(spectrum.getMetadataListElements().size()>0){
              CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
              List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
              for(int k=0;k<metadatas.size();k++){
                  if(metadatas.get(k).getName()!=null && metadatas.get(k).getName().equals("nmr:comment") && metadatas.get(k).getContent()!=null)
                      return metadatas.get(k).getContent();
              }
          }
      }
      return "";
  }


  /**
   * Gets the chemical names in data, newline separated, "" if not set.
   *
   * @return    The chemName value
   */
  public String getChemName() {
        if(data.getChildCMLElements( "molecule" ).size()==0)
          return "";
		StringBuffer returnValue = new StringBuffer();
		for(int i=0;i<((CMLMolecule)data.getChildCMLElements( "molecule" )
				.get( 0 )).getNameElements().size();i++){
		  if(((CMLMolecule)data.getChildCMLElements( "molecule" )
				    .get( 0 )).getNameElements().get(i)
				    .getAttribute("convention")==null){
		      String name = ((CMLMolecule)data.getChildCMLElements( "molecule" )
	                .get( 0 )).getNameElements().get(i)
				    .getXMLContent();
		      returnValue.append(name + "\n");
		  }
		}
		return returnValue.toString();
  }


  /**
   * Gets the chemical names in data, ; separated, "" if not set.
   *
   * @return    The chemNameWithSemicolon value
   */
  public String getChemNameWithSemicolon() {
		StringBuffer returnValue = new StringBuffer();
		if(data.getChildCMLElements( "molecule" ).size()>0){
    		for(int i=0;i<((CMLMolecule)data.getChildCMLElements( "molecule" )
    				.get( 0 )).getNameElements().size();i++){
    	        if(((CMLMolecule)data.getChildCMLElements( "molecule" )
                        .get( 0 )).getNameElements().get(i)
                        .getAttribute("convention")==null){
        		  String name = ((CMLMolecule)data.getChildCMLElements( "molecule" )
        				    .get( 0 )).getNameElements().get(i).getXMLContent();
        		  returnValue.append(name + ";");
    	        }
    		}
		}
		return (GeneralUtils.removeLastComma(returnValue).toString());
  }


  /**
   * Gets the casNumber in data, "" if not set.
   *
   * @return    The casNumber value
   */
  public String getCasNumber() {
      CMLMolecule molecule = getCMLMolecule();
      if(molecule!=null){
	      for(int k=0;k<molecule.getNameElements().size();k++){
	          if(molecule.getNameElements().get(k).getAttribute("convention")!=null && molecule.getNameElements().get(k).getAttribute("convention").getValue().equals("CAS")){
	              return molecule.getNameElements().get(k).getXMLContent();
	          }
	      }
	      //i had cases where the cas number was changed to this
	      for(int k=0;k<molecule.getScalarElements().size();k++){
	          if(molecule.getScalarElements().get(k).getAttribute("title")!=null && molecule.getScalarElements().get(k).getAttribute("title").getValue().equals("CAS_RN")){
	              return molecule.getScalarElements().get(k).getXMLContent();
	          }
	      }
      }
      return "";
  }


  /**
   * Gets the level attribute of the SubmittingData object, 1 if not set. Level is used for display during submit.
   *
   * @return    The level value
   */
  public int getLevel() {
      if(data.getFirstChildElement("level","http://www.nmrshiftdb.org/")==null){
          return 1;
      }else{
          return Integer.parseInt(data.getFirstChildElement("level","http://www.nmrshiftdb.org/").getAttribute("level").getValue());
      }
  }


  /**
   * Gets all literature entries in the data, converted to DBLiterature items (these must not exist in database).
   *
   * @return    The literature entries.
   */
  public List<DBLiterature> getLiterature() {
      List<DBLiterature> literatures = new ArrayList<DBLiterature>();
      if(data.getChildCMLElements("spectrum").size()>0){
          Elements els=data.getChildCMLElements("spectrum").get(0).getChildElements("entry","http://bibtexml.sf.net/");
          for(int k=0;k<els.size();k++){
              literatures.add(new DBLiterature(els.get(k)));
          }
      }
      return (literatures);
  }


  /**
   * Gets the condition entries of the data, converted to DBConditionType objects, with current values set.
   *
   * @return    The condition entries.
   * @throws TorqueException 
   * @throws NmrshiftdbException 
   */
  public List<DBConditionType> getConditions() throws TorqueException, NmrshiftdbException {
      Criteria crit = new Criteria();
      crit.add(DBConditionTypePeer.CONDITION_TYPE, "" + getMeasuredOrCalculated());
      if(this.getChoosenSpectrumType()!=null && this.getChoosenSpectrumType().getDimensionality()==1)
          crit.add(DBConditionTypePeer.FOR_1D,NmrshiftdbConstants.TRUE);
      if(this.getChoosenSpectrumType()!=null && this.getChoosenSpectrumType().getDimensionality()==2)
          crit.add(DBConditionTypePeer.FOR_2D,NmrshiftdbConstants.TRUE);
      if(this.getChoosenSpectrumType()!=null) {
	      crit.addJoin(DBConditionTypePeer.CONDITION_TYPE_ID, DBSpectrumTypeDBConditionPeer.CONDITION_TYPE_ID);
	      crit.add(DBSpectrumTypeDBConditionPeer.SPECTRUM_TYPE_ID, this.getChoosenSpectrumType().getSpectrumTypeId());
      }
      List<DBConditionType> conditions = DBConditionTypePeer.doSelect(crit);
      if(data.getChildCMLElements("spectrum").size()==0)
          data.appendChild(new CMLSpectrum());
      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
      for(int i=0;i<conditions.size();i++){
          String dictRef=((DBConditionType)conditions.get(i)).getDictRef();
          if(spectrum.getConditionListElements().size()>0){
              Elements els=spectrum.getConditionListElements().get(0).getChildCMLElements("scalar");
              for(int k=0;k<els.size();k++){
                  if(els.get(k).getAttribute("dictRef")!=null && els.get(k).getAttribute("dictRef").getValue().equals(dictRef)){
                      ((DBConditionType)conditions.get(i)).setCurrentValue(((CMLScalar)els.get(k)).getValue());
                      break;
                  }
              }
          }
          if(spectrum.getMetadataListElements().size()>0){
              Elements els=spectrum.getMetadataListElements().get(0).getChildCMLElements("metadata");
              for(int k=0;k<els.size();k++){
                  if(els.get(k).getAttribute("name")!=null && els.get(k).getAttribute("name").getValue().equals(dictRef)){
                      if(((CMLMetadata)els.get(k)).getContent()!=null)
                          ((DBConditionType)conditions.get(i)).setCurrentValue(((CMLMetadata)els.get(k)).getContent());
                      break;
                  }
              }
          }
          if(spectrum.getCMLChildCount("substanceList")>0){
              Elements els=((CMLSubstanceList)spectrum.getChildCMLElements("substanceList").get(0)).getChildCMLElements("substance");
              for(int k=0;k<els.size();k++){
                  if(els.get(k).getAttribute("dictRef")!=null && els.get(k).getAttribute("dictRef").getValue().equals(dictRef)){
                      if(((CMLSubstance)els.get(k)).getTitle()!=null)
                          ((DBConditionType)conditions.get(i)).setCurrentValue(((CMLSubstance)els.get(k)).getTitle());
                      break;
                  }
              }
          }
      }
      return (conditions);
  }
  
  /**
   * Tells if all conditions have a value set.
   * 
   * @return true=all conditions have values, false=some have none.
   * @throws TorqueException
   * @throws NmrshiftdbException 
   */
  public boolean allConditionsHaveValues() throws TorqueException, NmrshiftdbException{
      for(int i=0;i<getConditions().size();i++){
          if(getConditions().get(i).getCurrentSelectedCondition()==null && (getConditions().get(i).getCurrentValue()==null || getConditions().get(i).getCurrentValue().equals(""))){
              return false;
          }
      }
      return true;
  }


  /**
   * Gets the molecule links in the data, "" if not set.
   *
   * @return    The moleculeLink value
   */
  public String getMoleculeLinks() {
      if(data.getChildCMLElements("molecule").size()>0){
          CMLMolecule molecule=((CMLMolecule)data.getChildCMLElements("molecule").get(0));
          if(molecule.getChildCMLElements("metadataList").size()>0){
              CMLMetadataList mlist=(CMLMetadataList)molecule.getChildCMLElements("metadataList").get(0);
              List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
              for(int k=0;k<metadatas.size();k++){
                  if(metadatas.get(k).getName()!=null && metadatas.get(k).getName().equals("nmr:link"))
                      return metadatas.get(k).getContent();
              }
          }
      }
      return "";
  }


  /**
   * Gets the spectrumLinks, formatted as html.
   *
   * @return    The spectrumLink value
   */
  public String getSpectrumLinksAsLinks() {
    StringTokenizer st = new StringTokenizer(getSpectrumLinks(), "\n\r");
    StringBuffer returnValue=new StringBuffer();
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      StringTokenizer localst = new StringTokenizer(line, ";");
      String link = localst.nextToken();
      String des = "";
      if (localst.hasMoreTokens()) {
        des = localst.nextToken();
      }
      if(des.equals(""))
    	  des=link;
      returnValue.append("<a href=\""+link+"\">"+des+"</a> ");
    }
    return (returnValue.toString());
  }


  /**
   * Gets the moleculeLinks, formatted as html.
   *
   * @return    The moleculeLink value
   */
  public String getMoleculeLinksAsLinks() {
    StringTokenizer st = new StringTokenizer(getMoleculeLinks(), "\n\r");
    StringBuffer returnValue=new StringBuffer();
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      StringTokenizer localst = new StringTokenizer(line, ";");
      String link = localst.nextToken();
      String des = "";
      if (localst.hasMoreTokens()) {
        des = localst.nextToken();
      }
      if(des.equals(""))
    	  des=link;
      returnValue.append("<a href=\""+link+"\">"+des+"</a> ");
    }
    return (returnValue.toString());
  }


  /**
   * Gets the spectrum links in the data, "" if not set.
   *
   * @return    The spectrumLink value
   */
  public String getSpectrumLinks() {
      if(data.getChildCMLElements("spectrum").size()>0){
          CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
          if(spectrum.getMetadataListElements().size()>0){
              CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
              List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
              for(int k=0;k<metadatas.size();k++){
                  if(metadatas.get(k).getName()!=null && metadatas.get(k).getName().equals("nmr:link"))
                      return metadatas.get(k).getContent();
              }
          }
      }
      return "";
  }



  /**
   * Gets the CMLPeakList in the data, an empty one is built if not yet existing.
   *
   * @return    The CMLPeakList.
   */
  public CMLPeakList getSignalstable() {
	  if(data.getChildCMLElements("spectrum").size()==0){
		  data.appendChild(new CMLSpectrum());
	  }
	  if(data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").size()==0){
		  data.getChildCMLElements("spectrum").get(0).appendChild(new CMLPeakList());
	  }
	  return (CMLPeakList)data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0);
  }


  /**
   * Gets the CMLPeaks in the data, an empty one is built if not yet existing, ordered in ascending order.
   *
   * @return    The CMLPeaks.
   */
  public List<CMLPeak> getSignalstableOrderedAscending() {
	  if(data.getChildCMLElements("spectrum").size()==0){
		  data.appendChild(new CMLSpectrum());
	  }
	  if(data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").size()==0){
		  data.getChildCMLElements("spectrum").get(0).appendChild(new CMLPeakList());
	  }
	  List<CMLPeak> listordered=new ArrayList<CMLPeak>();
	  listordered.addAll(((CMLPeakList)data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0)).getPeakChildren());
	  Collections.sort(listordered, new SignalComparator(false));
	  return listordered;
  }
  
  
  /**
   * Gets the CMLPeaks in the data, an empty one is built if not yet existing, ordered in descending order.
   *
   * @return    The CMLPeaks.
   */
  public List<CMLPeak> getSignalstableOrderedDescending() {
	  if(data.getChildCMLElements("spectrum").size()==0){
		  data.appendChild(new CMLSpectrum());
	  }
	  if(data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").size()==0){
		  data.getChildCMLElements("spectrum").get(0).appendChild(new CMLPeakList());
	  }
	  List<CMLPeak> listordered=new ArrayList<CMLPeak>();
	  listordered.addAll(((CMLPeakList)data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0)).getPeakChildren());
	  Collections.sort(listordered, new SignalComparator(true));
	  return listordered;
  }

  
  /**
   * Gets the keepAssignmentEntries value of the data, used during submit only.
   *
   * @return    The keepAssignmentEntries value
   */
  public String getKeepAssignmentEntries() {
    return (keepAssignmentEntries);
  }


  /**
   * Gets the choosenSpectrumType of the data, null if not set.
   *
   * @return    The choosenSpectrumType value
   * @throws NmrshiftdbException 
   * @throws TorqueException 
   */
  public DBSpectrumType getChoosenSpectrumType() throws TorqueException, NmrshiftdbException  {
    if(data.getChildCMLElements("spectrum").size()==0)
        return null;
    if(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().size()==0)
        return null;
    if(GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").size()==0)
        return null;
    return DBSpectrumTypePeer.getByName(GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").get(0).getContent());
  }


  /**
   * Gets the molecule in the data with the hydrogens set as hydrogen count.
   *
   * @return    The molecule, null if not yet set.
   * @throws CDKException 
   * @throws IOException 
   */
  public IAtomContainer getMolWithHCount() throws CDKException, IOException {
	  if(data==null || data.getChildCMLElements("molecule").size()==0)
		  return null;
	  CMLReader cmlreader = new CMLReader(new ByteArrayInputStream(data.getChildCMLElement("molecule", 0).toXML().getBytes()));
	  IAtomContainer molWithH = ((IChemFile) cmlreader.read(new org.openscience.cdk.ChemFile())).getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
	  IAtomContainer molWithHCount = (IAtomContainer)AtomContainerManipulator.removeHydrogens(molWithH);
	  for(int i=0;i<molWithHCount.getAtomCount();i++){
	  	  //sets null hydrogen counts to 0
		  try{
			  molWithHCount.getAtom(i).getImplicitHydrogenCount();
		  }catch(NullPointerException ex){
			  molWithHCount.getAtom(i).setImplicitHydrogenCount(0);
		  }
	  }
	  cmlreader.close();
      return molWithHCount;
  }


  /**
   * Gets the molecule in the data with explicit hydrogens.
   *
   * @return    The molecule, null if not yet set.
   * @throws CDKException 
 * @throws IOException 
   */
  public IAtomContainer getMolWithH() throws CDKException, IOException {
	  if(data==null || data.getChildCMLElements("molecule").size()==0)
		  return null;
	  CMLReader cmlreader = new CMLReader(new ByteArrayInputStream(data.getChildCMLElement("molecule", 0).toXML().getBytes()));
	  IAtomContainer molWithH = ((IChemFile) cmlreader.read(new org.openscience.cdk.ChemFile())).getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
	  List<CMLAtom> atoms = ((CMLMolecule)data.getChildCMLElement("molecule", 0)).getAtoms();
	  for(int i=0;i<atoms.size();i++){
		  if(atoms.get(i).getChildElements(MULTIPLICITY).size()>0){
			  molWithH.getAtom(i).setProperty(MULTIPLICITY, atoms.get(i).getChildElements(MULTIPLICITY).get(0).getAttribute("value").getValue());
		  }
	  }
	  cmlreader.close();
	  for(int i=0;i<getCMLMolecule().getBondArray().getBondElements().size();i++) {
		  if(getCMLMolecule().getBondArray().getBondElements().get(i).getAttribute("order")!=null) {
 			  String order=getCMLMolecule().getBondArray().getBondElements().get(i).getAttribute("order").getValue();
			  if(order.equals("S"))
				  molWithH.getBond(i).setOrder(IBond.Order.SINGLE);
			  else if(order.equals("D"))
				  molWithH.getBond(i).setOrder(IBond.Order.DOUBLE);
			  else if(order.equals("T"))
				  molWithH.getBond(i).setOrder(IBond.Order.TRIPLE);
  		  }else {
  			molWithH.getBond(i).setOrder(IBond.Order.ZERO);
  		  }
 	  }
	  return molWithH;
  }


  /**
   * Tells if the current spectrum is measured.
   *
   * @return    The measured value
   */
  public boolean isMeasured() {
    if (getMeasuredOrCalculated() == 'm') {
      return true;
    } else {
      return false;
    }
  }


  /**
   * Tells if the current spectrum is calculated.
   *
   * @return    The calculated value
   */
  public boolean isCalculated() {
    if (getMeasuredOrCalculated() == 'c') {
      return true;
    } else {
      return false;
    }
  }


  /**
   * Tells if an atom is assigned to no signal
   *
   * @param  atom  The number of the atom in the mol, starting with 1.
   * @return       false=is assigned, true=not assigned
   */
  public boolean isAssignedToNone(int atom) {
    if (atom == -1) {
      return (false);
    }
    for (int i = 0; i < getSignalstable().getPeakElements().size(); i++) {
      if(getSignalstable().getPeakElements().get(i).getAtomRefs()!=null){
              for (int k = 0; k < getSignalstable().getPeakElements().get(i).getAtomRefs().length; k++) {
                if (getSignalstable().getPeakElements().get(i).getAtomRefs()[k].equals("a"+atom)) {
                  return false;
                }
              }
      }
    }
    return (true);
  }


  /**
   * Returns the spectrum in a String, formatted as needed for the spectrumApplet without atom numbers (12;.9|... format).
   *
   * @return  The spectrum
   * @throws CDKException 
 * @throws IOException 
   */
  public String getSpectrumForAppletSimple() throws CDKException, IOException {
    Collections.sort(getSignalstable().getChildCMLElements(), new SignalComparator(false));
    StringBuffer returnValue = new StringBuffer();
    for (int i = 0; i < getSignalstable().getPeakElements().size(); i++) {
      CMLPeak it = getSignalstable().getPeakElements().get(i);
      String multiplicity = "";
      if(it.getAtomRefs()!=null && it.getAtomRefs().length>0){
          CMLAtom cmlatom = getCMLMolecule().getAtom(getAtomNumber( AtomUtils.getAtomById(getMolWithH(), it.getAtomRefs()[0]), getMolWithH()));
          if(it.getAtomRefs()!=null && it.getAtomRefs().length>0 && cmlatom.getChildElements(MULTIPLICITY).size()>0){
            multiplicity = cmlatom.getChildElements(MULTIPLICITY).get(0).getAttribute("value").getValue();
          }
      }
  	  returnValue.append(it.getXValue()).append(";").append(it.getYValue()).append(multiplicity).append("|");
    }    
    if (returnValue.equals("")) {
      return ("|");
    } else {
      return (returnValue.toString());
    }
  }
  
  
  /**
   *  Calls getSpectrumForApplet(false).
   *
   * @return    The spectrumForApplet value
   * @throws CDKException 
   * @throws IOException 
   */
  public String getSpectrumForApplet() throws CDKException, IOException {
    return (getSpectrumForApplet(false));
  }


  /**
   * Returns the spectrum in a String, formatted as needed for the spectrumApplet with atom numbers (12;.9|... format).
   *
   * @param  withMultiplicities  Shall S D T Q get appended?
   * @return                     Description of the Returned Value
   * @throws CDKException 
   * @throws IOException 
   */
  public String getSpectrumForApplet(boolean withMultiplicities) throws CDKException, IOException {
    Collections.sort(getSignalstable().getChildCMLElements(), new SignalComparator(false));
    StringBuffer returnValue = new StringBuffer();
    IAtomContainer molWithH=getMolWithH();
    for (int i = 0; i < getSignalstable().getPeakElements().size(); i++) {
      CMLPeak it = getSignalstable().getPeakElements().get(i);
      if(it.getAtomRefs()==null || it.getAtomRefs().length==0){
    	  returnValue.append(it.getXValue()).append(";").append(it.getYValue()).append("|");
      }else{
	      for (int k = 0; k < it.getAtomRefs().length; k++) {
	        int mainAtomNumber = 0;
	        if (GeneralUtils.getMetadataDescendantsByName(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getMetadataListElements().get(0).getMetadataDescendants(),"nmr:OBSERVENUCLEUS").get(0).getContent().equals("1H")) {
	        	mainAtomNumber = molWithH.getAtomNumber((IAtom)molWithH.getConnectedAtomsList(AtomUtils.getAtomById(molWithH,it.getAtomRefs()[k])).get(0));
	        } else {
	        	mainAtomNumber = molWithH.getAtomNumber(AtomUtils.getAtomById(molWithH,it.getAtomRefs()[k]));
	        }
	        String multiplicity = "";
	        CMLAtom cmlatom = getCMLMolecule().getAtom(getAtomNumber( AtomUtils.getAtomById(getMolWithH(), it.getAtomRefs()[0]), getMolWithH()));
	        if(it.getAtomRefs()!=null && it.getAtomRefs().length>0 && cmlatom.getChildElements(MULTIPLICITY).size()>0){
	          multiplicity = cmlatom.getChildElements(MULTIPLICITY).get(0).getAttribute("value").getValue();
	        }
	        returnValue.append(it.getXValue()).append(";").append(it.getYValue()).append(multiplicity).append(";").append( mainAtomNumber).append("|");
	      }
      }
    }    
    if (returnValue.equals("")) {
      return ("|");
    } else {
      return (returnValue.toString());
    }
  }


  /**
   *  Gets couplings[i][k].
   *
   * @param  ik  row count,column count.
   * @return     The coupling value.
   * @throws CDKException 
   * @throws IOException 
   */
  public String getCoupling(String ik) throws CDKException, IOException {
    StringTokenizer tokenizer = new StringTokenizer(ik, ",");
    if (!tokenizer.hasMoreTokens()) {
      return ("");
    }
    int firstatom = Integer.parseInt(tokenizer.nextToken());
    if (!tokenizer.hasMoreTokens()) {
      return ("");
    }
    String secondatom= tokenizer.nextToken();
    IAtomContainer mol = this.getMolWithH();
    for(int i=0;i<mol.getAtomCount();i++){
    	if(i==firstatom){
	    	for(int k=0;k<mol.getAtomCount();k++){
	    		if(getCMLMolecule().getAtom(i).getChildElements(COUPLING,"http://www.nmrshiftdb.org/").size()>0){
	    			for(int l=0;l<getCMLMolecule().getAtom(i).getChildElements(COUPLING,"http://www.nmrshiftdb.org/").size();l++){
	    				if(getCMLMolecule().getAtom(i).getChildElements(COUPLING,"http://www.nmrshiftdb.org/").get(l).getAttribute("otherAtom").getValue().equals(secondatom)){
	    					return getCMLMolecule().getAtom(i).getChildElements(COUPLING,"http://www.nmrshiftdb.org/").get(l).getAttribute("value").getValue();
	    				}
	    			}
	    		}
	    	}
    	}
    }
    return "";
  }


  /**
   *  Gets the multiplicity of a certain atom.
   *
   * @param  atoms  The atom number as in molWithH as string.
   * @return        The multiplicity value.
   * @throws CDKException 
 * @throws IOException 
   */
  public String getMultiplicity(String atoms) throws CDKException, IOException {
    int atom = Integer.parseInt(atoms);
    if (getMolWithH().getAtom(atom).getProperty(MULTIPLICITY) == null) {
      return ("");
    } else {
      return ((String)getMolWithH().getAtom(atom).getProperty(MULTIPLICITY));
    }
  }


  /**
   *  Removes all assignments from signalstable.
   */
  public void clearAssignments() {
    for (int i = 0; i < getSignalstable().getPeakElements().size(); i++) {
    	getSignalstable().getPeakElements().get(i).setAtomRefs(new String[0]);
   	}
  }


  /**
   * Adds a peak to the spectrum in this data.
   *
   * @param  signal  The signal to be added to the spectrum.
   */
  public void addToSignalstable(CMLPeak signal) {
	  if(data.getChildCMLElements("spectrum").size()==0)
		  data.appendChild(new CMLSpectrum());
	  if(((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).getPeakListElements().size()==0)
		  ((CMLSpectrum)data.getChildCMLElements("spectrum").get(0)).appendChild(new CMLPeakList());
	  signal.detach();
	  for(CMLPeak oldpeak : ((CMLPeakList)data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0)).getPeakChildren()){
		  if(oldpeak.getXValue()==signal.getXValue() && oldpeak.getYValue()==signal.getYValue() && ((oldpeak.getPeakMultiplicity()!=null && oldpeak.getPeakMultiplicity().equals(signal.getPeakMultiplicity())) || (oldpeak.getPeakMultiplicity()==null && signal.getPeakMultiplicity()==null))){
			  if(signal.getAtomRefs()!=null && signal.getAtomRefs().length>0 && ArrayUtils.contains(oldpeak.getAtomRefs(), signal.getAtomRefs()[0])==-1)
				  oldpeak.setAtomRefs(SubmittingData.addAtomToAtomRefs(oldpeak.getAtomRefs(),signal.getAtomRefs()[0]));
			  return;
		  }			  
	  }
	  data.getChildCMLElements("spectrum").get(0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0).appendChild(signal);
  }
  

  /**
   * Saves this data to the database, using values for date etc. from this class.
   *
   * @param  servcon        The current ServletConfig
   * @exception  Exception  Problems with database
   */
  public void save(ServletConfig servCon) throws Exception {
	for(int i=0;i<this.getLiterature().size();i++){
		((DBLiterature)this.getLiterature().get(i)).setNew(true);
		((DBLiterature)this.getLiterature().get(i)).setModified(true);
	}
	save(getThisUserId(), getReviewFlag(), getThisURL(), getThisDate(), servCon, Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres", servCon)), false, AtomUtils.isOrganometallic(this.getMolWithH()));
  }

  public void save(int user, String reviewFlag, String URL, Date now, ServletConfig servcon, int numberOfSpheres, boolean saveFromBulk) throws Exception{
	  save(user, reviewFlag, URL, now, servcon, numberOfSpheres, saveFromBulk, false);
  }

  /**
   * Saves this data to the database.
   *
   * @param  user             The logged in user
   * @param  reviewFlag       The review flag the new spectrum is supposed to have.
   * @param  URL              The URL of the machine the program runs on
   * @param  now              The current date
   * @param  servcon          The current ServletConfig
   * @param  numberOfSpheres  The number of spheres to use (as in nmrshiftdb.properties)
   * @param  saveFromBulk     Is this a save from a bulkload ?
   * @exception  Exception    Problems with database
   */
  public void save(int user, String reviewFlag, String URL, Date now, ServletConfig servcon, int numberOfSpheres, boolean saveFromBulk, boolean isOrganometal) throws Exception{
    DBMolecule existingMolecule = null;
    if (getExistingMoleculeId() != 0) {
      existingMolecule = DBMoleculePeer.retrieveByPK(getExistingMoleculeId());
    }else{
        StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
        DBMolecule oldMol = GeneralUtils.getExistingMolecule(getMolWithHCount(), getMolWithH(), getIsDoubleBondSpecified(), servcon, getInputMolAsMdl(null,null,false), ssb, isOrganometal);
        if (oldMol != null) {
          setExistingMoleculeId(oldMol.getMoleculeId());
          existingMolecule = DBMoleculePeer.retrieveByPK(getExistingMoleculeId());
          setSmilesNonChiralProv(ssb.string1);
          setSmilesChiralProv(ssb.string2);
          setIsChiralProv(ssb.bool);
        }
    }
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_SSSS", Locale.US);
    timeformatted=formatter.format(now);
    StringTokenizer st = new StringTokenizer(URL, "/");
    URL = st.nextToken();
    if (st.hasMoreTokens()) {
      URL = st.nextToken();
    }
    setThisURL(URL);
    setThisDate(now);
    setReviewFlag(reviewFlag);
    Connection dbconn = null;
    Statement stmt = null;
    try {
      dbconn = Torque.getConnection();
      stmt = dbconn.createStatement();
      try {
        stmt.executeUpdate("BEGIN");
      } catch (SQLException ex) {
        //Since hsql does not understand transactions, we do nothing
      }
      IAtomContainer molwithh = getMolWithH();
      if(AtomUtils.isOrganometallic(molwithh)) {
          AtomUtils.doSimpleSaturation(molwithh,null);
          AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(molwithh);
      }else {
	      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molwithh);
	      ElectronDonation model       = ElectronDonation.cdk();
	      CycleFinder      cycles      = Cycles.cdkAromaticSet();
	      Aromaticity      aromaticity = new Aromaticity(model, cycles);
	      aromaticity.apply(molwithh);
      }
      AllRingsFinder arf = new AllRingsFinder();
      IRingSet ringSet = arf.findAllRings(molwithh);
      List<RMap> list = null;
      List<CMLAtom> oldatoms=getCMLMolecule().getAtomArray().getAtoms();
      CMLElements<CMLBond> oldcmlbonds = getCMLMolecule().getBondArray().getBondElements();
      if (existingMolecule == null) {
        //If the molecule is new, we save it
        moleculeid = saveMolecule(data, user, servcon, numberOfSpheres, dbconn, ringSet, isOrganometal);
      } else {
        DBMolecule dbmollocal=existingMolecule;
        //if not, we take the existing one
        moleculeid = existingMolecule.getMoleculeId();
        //We change the order of atoms/bonds as in the existing atom
        UniversalIsomorphismTester uit = new UniversalIsomorphismTester();
        uit.setTimeout(-1);
        list = uit.getIsomorphAtomsMap(molwithh, existingMolecule.getAsCDKMolecule(1));
        if (list != null) {
          CMLAtom[] newAtoms1 = new CMLAtom[molwithh.getAtomCount()];
          //Sometimes odd null atoms are in newAtoms1, so handle this 
          for (int i = 0; i < list.size(); i++) {
        	newAtoms1[((RMap) list.get(i)).getId2()] = getCMLMolecule().getAtom(((RMap) list.get(i)).getId1());
          }
          
          int shiftatoms=0;
          for (int i = 0; i < newAtoms1.length; i++) {
        	  if(newAtoms1[i]==null)
        		  shiftatoms++;
          }
          CMLAtom[] newAtoms2=new CMLAtom[newAtoms1.length-shiftatoms];
          if(shiftatoms==0){
        	  newAtoms2=newAtoms1;
          }else{
        	  shiftatoms=0;
        	  for (int i = 0; i < newAtoms1.length; i++) {
        		  if(newAtoms1[i]==null){
        			  shiftatoms++;
        		  }else{
        			  newAtoms2[i-shiftatoms]=newAtoms1[i];
        		  }
        	  }
          }
    	  while(getCMLMolecule().getAtomArray().size()>0){
    			  getCMLMolecule().getAtomArray().removeAtom(getCMLMolecule().getAtom(0));
    	  }
          for(CMLAtom atom : newAtoms2){
        	  getCMLMolecule().addAtom(atom);
          }
          for(CMLBond bond : oldcmlbonds.getList()){
        	  this.getCMLMolecule().getBondArray().addBond(bond);
          }
        }
        //This is for the case that the user changed the existing molecule, but did not make a really new one out of it.
        DBAtom[] olddbatoms = existingMolecule.getAtomsAsArrayInMdlOrder();
        if (getEditSpectrum() != 0 && list!=null) {
          for (int i = 0; i < list.size(); i++) {
            DBCoordinateSet2d coords=DBCoordinateSet2dPeer.getByAtomId(olddbatoms[i].getAtomId());
            if(coords==null){
              DBCoordinateSet3d coords3=DBCoordinateSet3dPeer.getByAtomId(olddbatoms[i].getAtomId());
              if(!Double.isNaN(getMolWithH().getAtom(i).getPoint2d().x))
                coords3.setXCoordinate(getMolWithH().getAtom(i).getPoint2d().x);
              if(!Double.isNaN(getMolWithH().getAtom(i).getPoint2d().y))
                coords3.setYCoordinate(getMolWithH().getAtom(i).getPoint2d().y);
              if(!Double.isNaN(getMolWithH().getAtom(i).getPoint3d().z))
                coords3.setZCoordinate(getMolWithH().getAtom(i).getPoint3d().z);
              coords3.save(dbconn);
            }else{
              if(!Double.isNaN(getMolWithH().getAtom(i).getPoint2d().x))
                coords.setXCoordinate(getMolWithH().getAtom(i).getPoint2d().x);
              if(!Double.isNaN(getMolWithH().getAtom(i).getPoint2d().y))
                coords.setYCoordinate(getMolWithH().getAtom(i).getPoint2d().y);
              coords.save(dbconn);
            }
          }
          List<DBBond> oldbonds = existingMolecule.getBonds();
          for (int i = 0; i < oldbonds.size(); i++) {
            ((DBBond) oldbonds.get(i)).setStereo(getMolWithH().getBond(i).getStereo().ordinal());
            ((DBBond) oldbonds.get(i)).setDegree(getMolWithH().getBond(i).getOrder().ordinal());
            ((DBBond) oldbonds.get(i)).save(dbconn);
          }
          //This deals with visibility of Hs
          IAtomContainer molOriginal = getMolOriginal();
          List<RMap> list2 = new UniversalIsomorphismTester().getSubgraphAtomsMap(existingMolecule.getAsCDKMolecule(1), molOriginal);
          if(list2!=null){
            boolean[] visible = new boolean[olddbatoms.length];
            for (int k = 0; k < list2.size(); k++) {
              visible[((RMap) list2.get(k)).getId1()] = true;
            }
            for (int i = 0; i < list.size(); i++) {
              if (visible[i]) {
                olddbatoms[i].setIsVisible(NmrshiftdbConstants.TRUE);
              } else {
                olddbatoms[i].setIsVisible(NmrshiftdbConstants.FALSE);
              }
              olddbatoms[i].save(dbconn);
            }
          }
        }
        if(coordinatesetid3>-1){
	        for(int i=0;i<getMolWithH().getAtomCount();i++){
	        	//look for dbatom for this atom via map and save coordinates
	        	int atomid=olddbatoms[((RMap) list.get(i)).getId2()].getAtomId();
                DBCoordinateSet3d set3d=new DBCoordinateSet3d(molcoord.getAtom(((RMap) list.get(i)).getId1()).getPoint3d().x,molcoord.getAtom(((RMap) list.get(i)).getId1()).getPoint3d().y,molcoord.getAtom(((RMap) list.get(i)).getId1()).getPoint3d().z,coordinatesetid3,atomid);
                set3d.save(dbconn);
	        }
        }
        //This means the user edited a spectrum and did not change the molecule, so we need to update the molecule
        if (!getChemName().equals("")) {
          if (!saveFromBulk) {
            Criteria crit = new Criteria();
            crit.add(DBChemicalNamePeer.MOLECULE_ID, moleculeid);
            DBChemicalNamePeer.doDelete(crit);
          }
          StringTokenizer tokenizer = new StringTokenizer(getChemName(), "\n\r");
          ArrayList<DBChemicalName> chemNames = new ArrayList<DBChemicalName>();
          while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            if(!name.trim().equals("")){
              DBChemicalName dbChemName = new DBChemicalName(moleculeid, name);
              dbChemName.save(dbconn);
              chemNames.add(dbChemName);
            }
          }
        }
        if (!getMoleculeLinks().equals("")) {
          if (!saveFromBulk) {
            Criteria crit = new Criteria();
            crit.add(DBMoleculeHyperlinkPeer.MOLECULE_ID, moleculeid);
            DBMoleculeHyperlinkPeer.doDelete(crit);
          }
          ArrayList<DBMoleculeHyperlink> moleculeHyperlinks = new ArrayList<DBMoleculeHyperlink>();
          st = new StringTokenizer(getMoleculeLinks(), "\n\r");
          while (st.hasMoreTokens()) {
            String line = st.nextToken();
            StringTokenizer localst = new StringTokenizer(line, ";");
            String link = localst.nextToken();
            String des = "";
            if (localst.hasMoreTokens()) {
              des = localst.nextToken();
            }
            DBMoleculeHyperlink mhl = new DBMoleculeHyperlink(moleculeid, link, des);
            if (!moleculeHyperlinks.contains(mhl)) {
              mhl.save(dbconn);
            }
            moleculeHyperlinks.add(mhl);
          }
        }
        if (!getMoleculeKeywords().equals("")) {
          if (!saveFromBulk) {
            Criteria crit = new Criteria();
            crit.add(DBMoleculeDBKeywordPeer.MOLECULE_ID, moleculeid);
            DBMoleculeDBKeywordPeer.doDelete(crit);
          }
          StringTokenizer tokenizer = new StringTokenizer(getMoleculeKeywords(), "\n\r");
          while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            new DBMoleculeDBKeyword(line, moleculeid).save(dbconn);
          }
        }
        if (!getCasNumber().equals("")) {
          if (!saveFromBulk) {
            dbmollocal.setCasNumber(getCasNumber());
          } else {
            if (!dbmollocal.getCasNumber().equals("")) {
              dbmollocal.setCasNumber(getCasNumber());
            }
          }
        }
        dbmollocal.save(dbconn);
        if (!getAutonomeName().equals("")) {
          boolean hasautonom=false;
          List<DBCanonicalName> v=dbmollocal.getDBCanonicalNames();
          for(int i=0;i<v.size();i++){
            if(((DBCanonicalName)v.get(i)).getCanonicalNameTypeId()==1){
              ((DBCanonicalName)v.get(i)).setName(getAutonomeName());
              ((DBCanonicalName)v.get(i)).setNameSoundex(Soundex.soundex(getAutonomeName(), "9", new StringAndInt()));
              hasautonom=true;
              break;
            }
          }
          if(!hasautonom){
            new DBCanonicalName(getAutonomeName(),moleculeid).save(dbconn);
          }
        }
        //We need to fill the dbatoms ArrayList with the atoms of the already existing molecule in order to link the signals to them
        DBAtom[] dbatomslocal = dbmollocal.getAtomsAsArrayInMdlOrder();
        dbatoms=new DBAtom[dbatomslocal.length];
        for(int i=0;i<dbatoms.length;i++)
          dbatoms[i]=dbatomslocal[i];
        moleculeWith3d=dbmollocal.getAsCDKMoleculeWithAny3d();
      }
      //Add spectrum to DB
      spectrumid = saveSpectrum(data, now, this.getLiterature(), moleculeid, user, this.getChoosenSpectrumType(), this.getChoosenSpectrumType().getDimensionality()==1 ? this.getSpectrumForApplet() : "", this.getConditions(), this.getSignalstable(), reviewFlag, this.getSpectrumLinks(), servcon, existingMolecule, dbconn, ringSet);
      //In case of an edit, set old spectrum to edited
      if (getEditSpectrum() != 0) {
        DBSpectrum oldSpectrum = DBSpectrumPeer.retrieveByPK(getEditSpectrum());
        oldSpectrum.setReviewFlag(NmrshiftdbConstants.EDITED);
        oldSpectrum.save(dbconn);
        DBUserDBSpectrumEdit edit=new DBUserDBSpectrumEdit();
        edit.setUserId(this.getThisEditor());
        edit.setEdit(new Date());
        edit.setSpectrumIdOld(oldSpectrum.getSpectrumId());
        edit.setSpectrumIdNew(spectrumid);
        edit.save(dbconn);
      }
      if(rawfile!=null){
    	  stmt.executeUpdate("update RAW_FILE set ASSIGNED ='true' where RAW_FILE_ID=" + rawfile.getRawFileId());
          //rawfile.setAssigned("true");
          //rawfile.save();
      }
      if(list!=null){
          while(getCMLMolecule().getAtomArray().size()>0){
              getCMLMolecule().getAtomArray().removeAtom(getCMLMolecule().getAtom(0));
          }
          for(CMLAtom atom : oldatoms){
              getCMLMolecule().addAtom(atom);
          }
          for(CMLBond bond : oldcmlbonds.getList()){
              this.getCMLMolecule().getBondArray().addBond(bond);
          }
      }
      try {
        stmt.executeUpdate("COMMIT");
      } catch (SQLException ex) {
        //Since hsql does not understand transactions, we do nothing
      }
    } catch (Exception ex) {
      try {
        stmt.executeUpdate("ROLLBACK");
      } catch (SQLException e) {
        //Since hsql does not understand transactions, we do nothing
      }
      throw ex;
    }
    finally{
        Torque.closeConnection(dbconn);
    }
  }


  /**
   * Saves a molecule to db (no values need to be set before, but molecule properties may be set).
   *
   * @param  mdl            The molecule to save.
   * @param  runData        The current runData object.
   * @param  user           The user this molecule is expected to belong to.
   * @return                The saved molecule or this molecule from db, if already existing.
   * @exception  Exception  Databae problems.
   */
  public int saveMoleculeOnly(IAtomContainer original, RunData runData, TurbineUser user) throws Exception {
    setMol(original, false, false, false);
    Date now = new Date();
    StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
    DBMolecule provmol=GeneralUtils.getExistingMolecule(getMolWithHCount(), getMolWithH(), new boolean[getMolWithH().getBondCount()], runData.getServletConfig(), getInputMolAsMdl(null,null,false), ssb, false);
	int existingMolecule =-1;
	if(provmol!=null)
		existingMolecule = provmol.getMoleculeId();
    if (existingMolecule != -1) {
      return (existingMolecule);
    } else {
      setSmilesNonChiralProv(ssb.string1);
      setIsChiralProv(ssb.bool);
      if(ssb.bool){
    	  setSmilesChiralProv(ssb.string2);
      }
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss_SSSS", Locale.US);
      timeformatted=formatter.format(now);
      Connection dbconn = null;
      Statement stmt = null;
      try {
        dbconn = Torque.getConnection();
        stmt = dbconn.createStatement();
        stmt.executeUpdate("BEGIN");
        IAtomContainer ac = getMolWithH();
        if(AtomUtils.isOrganometallic(ac)) {
            AtomUtils.doSimpleSaturation(ac,null);
            AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(ac);
        }else {
	        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
	        ElectronDonation model       = ElectronDonation.cdk();
	        CycleFinder      cycles      = Cycles.cdkAromaticSet();
	        Aromaticity      aromaticity = new Aromaticity(model, cycles);
	        aromaticity.apply(ac);
        }
        AllRingsFinder arf = new AllRingsFinder();
        IRingSet ringSet = null;
        //If we get a timeout or other error from allringsfinder, it's ok, components should be preparted to work without
        try{
            ringSet = arf.findAllRings(ac);
        }
        catch(Exception ex){        
        }
        existingMolecule = saveMolecule(data, user.getUserId(), runData.getServletConfig(), Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres", runData)), dbconn, ringSet, false);
        stmt.executeUpdate("COMMIT");
        return (existingMolecule);
      } catch (Exception ex) {
        stmt.executeUpdate("ROLLBACK");
        throw ex;
      }
      finally{
          Torque.closeConnection(dbconn);
          
      }
    }
  }

  /**
   *  Gets the atom Number of an atom in a molecule by comparing the coordinates.
   *
   * @param  atom  The atom.
   * @param  ac    The molecule.
   * @return       The atomNumber value
   */
  private int getAtomNumber(IAtom atomcompare, IAtomContainer ac) {
    Iterator<IAtom> atoms = ac.atoms().iterator();
    int i=0;
    while(atoms.hasNext()){
      IAtom atom=(IAtom)atoms.next();
      if (atom.getPoint2d()!=null && atom.getPoint2d().equals(atomcompare.getPoint2d())) {
        return (i);
      }
      if (atom.getPoint3d()!=null && atom.getPoint3d().equals(atomcompare.getPoint3d())) {
        return (i);
      }
      i++;
    }
    return (-1);
  }


  /**
   * Indicates if no assignemnts were done.
   *
   * @return    true=no assignemnts, yes=some assignments.
   */
  public boolean notYetAnyAssignments() {
    for (int i = 0; i < getSignalstable().getPeakElements().size(); i++) {
      if (getSignalstable().getPeakElements().get(i).getAtomRefs()!=null && getSignalstable().getPeakElements().get(i).getAtomRefs().length!=0) {
        return (false);
      }
    }
    return (true);
  }


  /**
   * Saves a molecule + bonds + atoms to the database
   *
   * @param  mol              The molecule as a cdk molecule without the implicit hs, but with HydrogenCount set
   * @param  molWithH         The molecule as a cdk molecule with the implicit hs
   * @param  now              The date
   * @param  casNumber        The cas number
   * @param  nmrshiftdbnr     The nmrshiftdbnumber
   * @param  userKey          The submitter
   * @param  chemName         The chemical names, as typed by the user
   * @param  moleculeLinks    The links for the molecule
   * @param  isChiral         Is there any chirality in the molecule
   * @param  servcon          The current ServletConfig
   * @param  smilesNonChiral  Description of Parameter
   * @param  smilesChiral     Description of Parameter
   * @param  numberOfSpheres  The number of spheres to use (as in nmrshiftdb.properties)
   * @param  dbconn           Description of Parameter
   * @param  ringSet          The set of all rings of the structure.
   * @return                  The saved molecule
   * @exception  Exception
   */
  private int saveMolecule(CMLCml data, int userKey, ServletConfig servcon, int numberOfSpheres, Connection dbconn, IRingSet ringSet, boolean isOrganometal) throws Exception {
      MDLV2000Writer mdlwriter=null;
      try{
        IAtomContainer molWithH = getMolWithH();
        IAtomContainer molWithHCount = getMolWithHCount();
        int numberOfAtomsInMdl = getMolOriginal().getAtomCount();
        SSSRFinder sssr=new SSSRFinder(molWithH);
        DBMolecule dbmollocal = new DBMolecule(molWithHCount, molWithH, new Date(), this.getCasNumber(), userKey, getSmilesNonChiralProv(), getSmilesChiralProv(), getIsChiralProv(), numberOfAtomsInMdl, MolecularFormulaManipulator.getMolecularFormula(molWithH), ringSet,sssr.findSSSR().getAtomContainerCount());
        dbmollocal.save(dbconn);
        moleculeid=dbmollocal.getMoleculeId();
        StringWriter sw = new StringWriter();
        mdlwriter = new MDLV2000Writer(sw);
        mdlwriter.writeMolecule(molWithH);
        if (!getAutonomeName().equals("")) {
          new DBCanonicalName(getAutonomeName(),moleculeid).save(dbconn);
        }
        try{
          String inchi;
          if(isOrganometal) {
          	  inchi=GeneralUtils.generateTucan(molWithH);
              new DBCanonicalName(inchi,moleculeid,7).save(dbconn);
          }else {
        	  inchi=GeneralUtils.generateInchi(getMolWithH(),false);
              new DBCanonicalName(inchi,moleculeid,4).save(dbconn);
          }
        }catch(Exception ex){
          //do nothing if inchi generatin fails
        }
        try{
        	if(!isOrganometal)
        		new DBCanonicalName(GeneralUtils.generateInchiKey(getMolWithH()),moleculeid,5).save(dbconn);
        }catch(Throwable ex){
            //do nothing if inchi generatin fails
        }
        try{
        	if(!isOrganometal)
        		new DBCanonicalName(GeneralUtils.generateInchi(getMolOriginal(),true),moleculeid,6).save(dbconn);
          }catch(Exception ex){
            //do nothing if inchi generatin fails
          }
        if (servcon != null) {
          GeneralUtils.logToSql("Save molecule - " + moleculeid, null);
        }
        Log.debug("after insert molecule");
        for(int i=0;i<((CMLMolecule)data.getChildCMLElements( "molecule" )
        		.get( 0 )).getNameElements().size();i++){
          if(((CMLMolecule)data.getChildCMLElements( "molecule" )
              .get( 0 )).getNameElements().get(i)
              .getAttribute("convention")==null){
            String name = ((CMLMolecule)data.getChildCMLElements( "molecule" )
        		    .get( 0 )).getNameElements().get(i).getXMLContent();
            DBChemicalName dbChemName = new DBChemicalName();
            dbChemName.setMoleculeId(moleculeid);
            dbChemName.setName(GeneralUtils.replace(name, "\ufffd", "&#946;"));
            dbChemName.setNameSoundex(Soundex.soundex(name, "9", new StringAndInt()));
            dbChemName.save(dbconn);
          }
        }
        Log.debug("after insert chemName");
        //Add atoms to db
        dbatoms = new DBAtom[molWithH.getAtomCount()];
        HOSECodeGenerator hcg = new HOSECodeGenerator();
        ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
        int countnoh=0;
        for (int i = 0; i < molWithH.getAtomCount(); i++) {
          IAtomContainer molforhose=(IAtomContainer)molWithH.clone();
          if(isOrganometal) {
        	  AtomUtils.doReplacement(molforhose);
          }
          IAtomContainer molwithouths=AtomContainerManipulator.removeHydrogens(molforhose);
          String hoseCode = null;
          String hoseCodeWithRings = null;
          if (GeneralUtils.getAtomsWithHoseCode().contains(molforhose.getAtom(i).getSymbol())) {
            hoseCode = hcg.getHOSECode(molwithouths, molwithouths.getAtom(countnoh), numberOfSpheres,false);
            hoseCodeWithRings = ehcg.getHOSECode(molforhose, molforhose.getAtom(i), numberOfSpheres, true);
          }
          if(molforhose.getAtom(i).getSymbol().equals("H")){
            hoseCode = hcg.getHOSECode(molforhose, molforhose.getAtom(i), numberOfSpheres,false);
            hoseCodeWithRings = ehcg.getHOSECode(molforhose, molforhose.getAtom(i), numberOfSpheres, true);
          }else{
            countnoh++;
          }
          DBAtom atom = new DBAtom(molWithH.getAtom(i), molWithH, moleculeid, hoseCode, hoseCodeWithRings, molWithH.getAtom(i).getProperty(VISIBLE)!=null && molWithH.getAtom(i).getProperty(VISIBLE).equals("true"),i,molWithH.getAtom(i).getProperty("equivalence")!=null ? Integer.parseInt(molWithH.getAtom(i).getProperty("equivalence")) : 0);
          //atom.force3d=true;
          atom.save(dbconn);
          dbatoms[i]=atom;
        }
        Log.debug("after insert atoms");
        //Add bonds to db
        for (int i = 0; i <  molWithH.getBondCount(); i++) {
          DBBond bond = new DBBond(molWithH.getBond(i), getIsDoubleBondSpecified()[i]);
          bond.save(dbconn);
          //Add bonds-atoms relationship to DB
          Iterator<IAtom> neighbourAtoms = molWithH.getBond(i).atoms().iterator();
          int k=0;
          while(neighbourAtoms.hasNext()){
            DBBondDBAtom bondatom = new DBBondDBAtom();
            bondatom.setAtomId(dbatoms[molWithH.getAtomNumber((IAtom)neighbourAtoms.next())].getAtomId());
            bondatom.setBondId(bond.getBondId());
            bondatom.setOrderNumber(k);
            bondatom.save(dbconn);
            k++;
          }
        }
        Log.debug("after insert bonds");
        ArrayList<DBMoleculeHyperlink> moleculeHyperlinks = new ArrayList<DBMoleculeHyperlink>();
        StringTokenizer st = new StringTokenizer(getMoleculeLinks(), "\n\r");
        while (st.hasMoreTokens()) {
          String line = st.nextToken();
          StringTokenizer localst = new StringTokenizer(line, ";");
          String link = localst.nextToken();
          String des = "";
          if (localst.hasMoreTokens()) {
            des = localst.nextToken();
          }
          DBMoleculeHyperlink mhl = new DBMoleculeHyperlink(moleculeid, link, des);
          if (!moleculeHyperlinks.contains(mhl)) {
            mhl.save(dbconn);
          }
          moleculeHyperlinks.add(mhl);
        }
        Log.debug("after insert molcule links");
        st = new StringTokenizer(getMoleculeKeywords(), "\n\r");
        while (st.hasMoreTokens()) {
          String line = st.nextToken();
          try{
            new DBMoleculeDBKeyword(line, moleculeid).save(dbconn);
          } catch (TorqueException ex) {
            //This is to avoid problems if a keyword is given multiple times.
          }
        }
        Log.debug("after insert molcule keywords");
        /*try{
          moleculeWith3d=dbmollocal.addCdkCoords(dbconn,molWithH, dbatoms);
        }catch(Exception ex){
          GeneralUtils.logError(ex,"generatring 3d for"+moleculeid,null,false);
        }*/
        return (moleculeid);
      }
      finally{
    	  if(mdlwriter!=null)mdlwriter.close();
      }
  }
  
  /**
   * Tells the molecular weight of the current molecule.
   * 
   * @return The molecular weight.
   * @throws CDKException
   * @throws IOException 
   */
  public double getMolecularWeight() throws CDKException, IOException{
	  return MolecularFormulaManipulator.getNaturalExactMass(MolecularFormulaManipulator.getMolecularFormula(getMolWithH()));
  }

  /**
   *  Gets the molecular formula of the DBMolecule object.
   *
   * @param  withFormat     true=formula gets formatted with html, false=plain text.
   * @return                The molecular formula value.
   * @exception  Exception  Description of Exception
   */
  public String getMolecularFormula(boolean withFormat) throws Exception {
    String symbol;
    String mf = "";
    SortedMap<String,Integer> symbols = new TreeMap<String,Integer>();
    IAtom atom = null;
    for (int f = 0; f < getMolWithH().getAtomCount(); f++) {
      atom = getMolWithH().getAtom(f);
      symbol = atom.getSymbol();
      if (symbols.get(symbol) != null) {
        symbols.put(symbol, new Integer(((Integer) symbols.get(symbol)).intValue() + 1));
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
   * Saves a spectrum + signals + the rest to the database
   *
   * @param  now                  The date
   * @param  literature           Literature
   * @param  dbmol                The Molecule the spectrum is assigned to
   * @param  userKey              The submitter
   * @param  choosenSpectrumType  The spectrum type of this spectrum
   * @param  spectrumForApplet    The spectrum as given by the getSpectrumForApplet method of subData
   * @param  originalConditions   The conditions as in the conditions ArrayList of submittingData
   * @param  signalstable         The signals as a ArrayList of valueTriples
   * @param  molWithH             The molecule as cdk molecule including implicit hs
   * @param  reviewFlag           The value the reviewFlag should be set to
   * @param  spectrumLinks        The links for the spectrum
   * @param  servcon              The current ServletConfig
   * @param  existingMolecule     Description of Parameter
   * @param  dbconn               Description of Parameter
   * @param  ringSet              The set of all rings of the structure of this spectrum. 
   * @return                      The saved spectrum
   * @throws TorqueException      Something wrong with the database
   * @throws CDKException 
   * @throws IOException 
   * @throws NmrshiftdbException 
   */
  private int saveSpectrum(CMLCml data, Date now, List<DBLiterature> literature, int dbmol, int userKey, DBSpectrumType choosenSpectrumType, String spectrumForApplet, List<DBConditionType> originalConditions, CMLPeakList signalstable, String reviewFlag, String spectrumLinks, ServletConfig servcon, DBMolecule existingMolecule, Connection dbconn, IRingSet ringSet) throws TorqueException, DataSetException, CDKException, IOException, NmrshiftdbException {
	  	DBSpectrum spectrum = toDBSpectrum(data, now, literature, dbmol, userKey, choosenSpectrumType, spectrumForApplet, originalConditions, signalstable, reviewFlag, spectrumLinks);
	  	for(DBSpectrumDBCondition speccon : (List<DBSpectrumDBCondition>)spectrum.getDBSpectrumDBConditions()){
	  		speccon.getDBCondition().save(dbconn);
	  		//This is needed in case new conditions are created - not completely sure why
	  		speccon.setConditionId(speccon.getDBCondition().getConditionId());
	  	}
	    spectrum.save(dbconn);
	    for(String multiplicity : multiplicityMap.keySet()){
	    	DBSignalPeer.executeStatement("insert into PC values ("+spectrum.getSpectrumId()+",'"+multiplicity+"',"+multiplicityMap.get(multiplicity)+")", dbconn);
	    }
	    DBSignalPeer.executeStatement("insert into d2 values ("+spectrum.getSpectrumId()+","+multiplicityMap.keySet().size()+")");
	    spectrumid=spectrum.getSpectrumId();
	    if (servcon != null) {
	      GeneralUtils.logToSql("Save spectrum - " + spectrum.getSpectrumId(), null);
	    }
	    long fingerprint=SpectrumUtils.makeFingerprint(spectrum);
	    spectrum = null;
	    DBSpectrumPeer.executeStatement("update SPECTRUM set FINGERPRINT="+fingerprint+" where SPECTRUM_ID="+ spectrumid,dbconn);
	    Log.debug("after insert spectrum");
	    for (int i = 0; i < literature.size(); i++) {
	        ((DBLiterature) literature.get(i)).save(spectrumid, dbconn);
	    }
	    Log.debug("after insert literature");
	    /*if(choosenSpectrumType.getName().equals("1H")){
	        try{
	          IAtomContainer cdkmol=moleculeWith3d;
	          Statement stmt = dbconn.createStatement();
	          ResultSetMetaData[] meta=new ResultSetMetaData[5];
	          for(int i=1;i<5;i++){
	            ResultSet rs = stmt.executeQuery("select * from DESCRIPTORS_"+i+" limit 1");
	            meta[i]=rs.getMetaData();
	          }
	          spectrum=DBSpectrumPeer.retrieveByPK(new NumberKey(spectrumid),dbconn);
	          DBCondition temp=spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.TEMPERATURE,dbconn);
	          DBCondition field=spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,dbconn);
	          DBCondition solvent=spectrum.getMeasurementConditionWithName("Solvent",dbconn);
	          for(int i=0;i<spectrum.getDBSignals(dbconn).size();i++){
	            DBSignal signal=(DBSignal)spectrum.getDBSignals(dbconn).get(i);
	            for(int h=0;h<signal.getAtoms(dbconn).size();h++){
	              int atomnumber=0;
	              for(int k=0;k<dbatoms.length;k++){
	                if(((DBAtom)signal.getAtoms(dbconn).get(h)).getAtomId()==dbatoms[k].getAtomId()){
	                  atomnumber=k;
	                  break;
	                }
	              }
	              int protonclass=AtomUtils.getProtonClass(cdkmol, atomnumber, ringSet);
	              float[] descriptors = AtomUtils.getDescriptorSet(temp, field, solvent, cdkmol, cdkmol.getAtom(atomnumber), AtomUtils.getAllRings(),protonclass);
	              StringBuffer sql1=new StringBuffer("insert into DESCRIPTORS_"+protonclass+" values("+((DBAtom)signal.getAtoms(dbconn).get(h)).getAtomId()+","+signal.getSignalId()+",");
	              int colsnumber = meta[protonclass].getColumnCount();
	              for(int k=3;k<=colsnumber;k++){
	                String label=meta[protonclass].getColumnLabel(k);
	                String sql2="SELECT MIN_VALUE, DIVISOR from DESCRIPTOR_FACTORS where DESCRIPTOR='"+label+"' and PROTONCLASS="+protonclass;
	                ResultSet rs=stmt.executeQuery(sql2);
	                rs.next();
	                float min=rs.getFloat(1);
	                float divisor=rs.getFloat(2);
	                float valueNew=(descriptors[k-3]-min)/divisor;
	                sql1.append(valueNew+",");
	              }
	              stmt.executeUpdate(sql1.substring(0,sql1.length()-1)+")");
	              StringBuffer sql=new StringBuffer("insert into DESCRIPTORS_"+protonclass+" values("+((DBAtom)signal.getAtoms().get(h)).getAtomId()+","+signal.getSignalId()+",");
	              for(int k=0;k<descriptors.length;k++){
	                sql.append(descriptors[k]+",");
	              }
	              stmt.executeUpdate(sql.toString().substring(0,sql.toString().length()-1)+")");
	            }
	          }
	        }
	        catch(Exception ex){
	          GeneralUtils.logError(ex,"creating descriptors during save",null,false);
	        }
	      }*/
	    return spectrumid;
  }
  
  /**
   * Gives the current data as a DBSpectrum object. All related DBXx objects are set, expect the molecule. Data are not saved.
   * 
   * @return The current data.
   * @throws TorqueException
   * @throws CDKException
   * @throws NmrshiftdbException
   * @throws IOException 
   */
  public DBSpectrum toDBSpectrum() throws TorqueException, CDKException, NmrshiftdbException, IOException{
	  return toDBSpectrum(this.data, new Date(), this.getLiterature(), -1, -1,this.getChoosenSpectrumType(), this.getSpectrumForApplet(), this.getConditions(), this.getSignalstable(), "false",this.getSpectrumLinks());
  }
  
  /**
   * Gives the current data as a DBSpectrum object. All related DBXx objects are set, expect the molecule. Data are not saved.
   * 
   * @param data
   * @param now
   * @param literature
   * @param dbmol
   * @param userKey
   * @param choosenSpectrumType
   * @param spectrumForApplet
   * @param originalConditions
   * @param signalstable
   * @param reviewFlag
   * @param spectrumLinks
   * @return The current data.
   * @throws TorqueException
   * @throws CDKException
   * @throws IOException 
   * @throws NmrshiftdbException 
   */
  private DBSpectrum toDBSpectrum(CMLCml data, Date now, List<DBLiterature> literature, int dbmol, int userKey, DBSpectrumType choosenSpectrumType, String spectrumForApplet, List<DBConditionType> originalConditions, CMLPeakList signalstable, String reviewFlag, String spectrumLinks) throws TorqueException, CDKException, IOException, NmrshiftdbException{
	  if(choosenSpectrumType==null)
		  return null;
    DBSpectrum spectrum = new DBSpectrum();
    spectrum.initEmptyCollections();
    spectrum.setDate(now);
    spectrum.setReviewFlag(reviewFlag);
    String reviewKey = "" + new Double("" + Math.random() * 100000).intValue();
    spectrum.setReviewKey(reviewKey);
    spectrum.setMoleculeId(dbmol);
    spectrum.setUserId(userKey);
    spectrum.setDBMixture(null);
    spectrum.setMixtureId(null);
    //Without this, 0 is written as fk and gives error
    spectrum.setDBSubstance(null);
    spectrum.setSubstanceId(null);
    spectrum.setSpectrumTypeId(choosenSpectrumType.getSpectrumTypeId());
    spectrum.setCorrelationSpecfile("");//this is fille din the BuildHoseCodeTableDaemon if 13C and 1H spectrum are available
    if(this.getChoosenSpectrumType().getDimensionality()==1){
    	spectrum.setSpecfile(spectrumForApplet);
        spectrum.setSimpleSpecfile(getSpectrumForAppletSimple());
    }else{
    	spectrum.setSpecfile("");
    	spectrum.setSimpleSpecfile("");
    }
    spectrum.setCorrelationSpecfile("");
    spectrum.setFingerprint(0);
    spectrum.setComment(getComment().getBytes());
    spectrum.setCommentSoundex(Soundex.soundex(getComment(), "9", new StringAndInt()));
    spectrum.setPublished(NmrshiftdbConstants.FALSE);
    if(getJcamp()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.JCAMP_TYPE);
        specfile.setFormat("jcamp-dx");
        specfile.setData(this.getJcamp().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
    if(getPdf()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.PDF_TYPE);
        specfile.setFormat("pdf");
        specfile.setData(data.getFirstChildElement("pdf","http://www.nmrshiftdb.org/").getAttribute("pdf").getValue().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
    if(getRawData()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.RAW_DATE_TYPE);
        String format=data.getFirstChildElement("rawdata","http://www.nmrshiftdb.org/").getAttribute("format").getValue();
        specfile.setFormat(format.indexOf('.')>-1 ? format.substring(format.indexOf('.')+1) : format);
        specfile.setData(data.getFirstChildElement("rawdata","http://www.nmrshiftdb.org/").getAttribute("rawdata").getValue().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
    if(getImage()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.IMAGE_TYPE);
        specfile.setFormat(data.getFirstChildElement("image","http://www.nmrshiftdb.org/").getAttribute("format").getValue());
        specfile.setData(data.getFirstChildElement("image","http://www.nmrshiftdb.org/").getAttribute("image").getValue().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
    if(getOtherFiles()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.OTHERFILES_TYPE);
        specfile.setFormat("zip");
        specfile.setData(data.getFirstChildElement("otherfiles","http://www.nmrshiftdb.org/").getAttribute("otherfiles").getValue().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
    if(getCmcseFile()!=null){
        DBSpectrumFile specfile = new DBSpectrumFile();
        specfile.setType(DBSpectrum.CMCSE_TYPE);
        specfile.setFormat("zip");
        specfile.setData(data.getFirstChildElement("cmcse","http://www.nmrshiftdb.org/").getAttribute("cmcse").getValue().getBytes());
        spectrum.addDBSpectrumFile(specfile);
    }
	if(dbatoms==null){
	    IAtomContainer molWithH = getMolWithH();
		dbatoms=new DBAtom[molWithH.getAtomCount()];
	    for (int i = 0; i < molWithH.getAtomCount(); i++) {
	        DBAtom atom = new DBAtom(molWithH.getAtom(i), molWithH, moleculeid, "", "", molWithH.getAtom(i).getProperty(VISIBLE)!=null && molWithH.getAtom(i).getProperty(VISIBLE).equals("true"),i,molWithH.getAtom(i).getProperty("equivalence")!=null ? molWithH.getAtom(i).getProperty("equivalence") : 0);
	        if(molWithH.getAtom(i).getSymbol().equals("H")){
		  		atom.mdlNumberPlus1Heavy= molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(0));
			}else{
				  atom.mdlNumberPlus1Heavy= i + 1;
			}
	        if(getIdentifiers().get(new Integer(i))!=null)
	        	atom.identifier=getIdentifiers().get(new Integer(i));
	        else
	        	atom.identifier="";
	        dbatoms[i]=atom;
	    }
	    DBMolecule localdbmol=new DBMolecule();
	    localdbmol.dbatoms=dbatoms;
	    localdbmol.setid=1;
	    localdbmol.cdkMolecule=getMolWithH();
	    localdbmol.setMoleculeId(dbmol);
	    spectrum.setDBMolecule(localdbmol);
	}
    //Add measurement conditions to db
    for (int i = 0; i < originalConditions.size(); i++) {
      String value = null;
      if (((DBConditionType) originalConditions.get(i)).getCurrentValue().equals("") && ((DBConditionType) originalConditions.get(i)).getCurrentSelectedCondition() != null) {
        value = ((DBConditionType) originalConditions.get(i)).getCurrentSelectedCondition().getValue();
      } else {
        value = ((DBConditionType) originalConditions.get(i)).getCurrentValue();
      }
      DBSpectrumDBCondition speccon = new DBSpectrumDBCondition();
      speccon.setSpectrumId(spectrumid);
      speccon.setDBCondition(DBConditionPeer.makeNewOrGetExisting(((DBConditionType) originalConditions.get(i)),value,userKey));
      spectrum.addDBSpectrumDBCondition(speccon);
    }
    Log.debug("after insert conditions");
    if(getChoosenSpectrumType()==null || getChoosenSpectrumType().getDimensionality()==1 || this.getChoosenSpectrumType().getDimensionality()==2){
        DBSignal[] signals = new DBSignal[signalstable.getPeakElements().size()];
        for (int i = 0; i < signalstable.getPeakElements().size(); i++) {
          //if there is more than one multiplicity for that shift, we need to split it
          /*Set<String> multiplicities = new HashSet<String>();
          for(int k=0;k<signalstable.getPeakElements().get(i).getAtomRefs().length;k++){
        	  multiplicities.add(getCMLMolecule().getAtom(getAtomNumber( getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[k]), getMolWithH())).getAttribute(MULTIPLICITY).getValue());
          }
          Iterator<String> multiit = multiplicities.iterator();
          while(multiit.hasNext()){
        	  String multiplicity = multiit.next();
        	  List<String> oldRefs = new ArrayList<String>();
        	  signalstable.addPeak((CMLPeak)signalstable.getPeakElements().get(i).copy());
        	  signalstable.getPeakElements().get(signalstable.getPeakElements().size()).setAtomRefs("");
        	  for(int l=0;l<signalstable.getPeakElements().get(i).getAtomRefs().length;l++){
        		  CMLAtom refAtom = getCMLMolecule().getAtom(getAtomNumber( getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[l]), getMolWithH()));
        		  if(refAtom.getAttribute(MULTIPLICITY).getValue().equals(multiplicity)){
        			  signalstable.getPeakElements().get(signalstable.getPeakElements().size()).addAtom(refAtom);
        		  }else{
        			  oldRefs.add(signalstable.getPeakElements().get(i).getAtomRefs()[l]);
        		  }
        	  }
    		  signalstable.getPeakElements().get(i).setAtomRefs((String[])oldRefs.toArray());
          }*/
          signals[i] = new DBSignal();
          signals[i].initEmptyCollections();
          if(this.getChoosenSpectrumType().getDimensionality()==1)
        	  signals[i].setIntensity((float)signalstable.getPeakElements().get(i).getYValue());
          else
        	  signals[i].setIntensity(0);
          signals[i].setSpectrumId(spectrumid);
          //TODO use code from above to handle different multiplicities
    	  if(this.getChoosenSpectrumType().getDimensionality()==1 && signalstable.getPeakElements().get(i).getAtomRefs()!=null && signalstable.getPeakElements().get(i).getAtomRefs().length>0){
    		IAtom atomloc = AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[0]);
    		CMLAtom atomcmlloc = getCMLMolecule().getAtom(getAtomNumber( atomloc, getMolWithH()));
    		signals[i].setMultiplicity(AtomUtils.getChildElement(atomcmlloc, MULTIPLICITY));
    		if(multiplicityMap.get(signals[i].getMultiplicity())==null){
    			multiplicityMap.put(signals[i].getMultiplicity(), 1);
    		}else{
    			multiplicityMap.put(signals[i].getMultiplicity(), multiplicityMap.get(signals[i].getMultiplicity()).intValue()+1);
    		}			
    	  }
    	  spectrum.addDBSignal(signals[i]);
          DBShift shift = new DBShift();
          shift.setDBSignal(signals[i]);
          shift.setValue((float)signalstable.getPeakElements().get(i).getXValue());
          shift.setAxis(1);
          DBShift shift2 = new DBShift();
          if(this.getChoosenSpectrumType().getDimensionality()==2){
              shift2.setDBSignal(signals[i]);
              shift2.setValue((float)signalstable.getPeakElements().get(i).getYValue());
              shift2.setAxis(2);        	  
              signals[i].addDBShift(shift2);
          }
          signals[i].addDBShift(shift);
          List<DBAtom> dbatomsforsignal=new ArrayList<DBAtom>();
          if(this.getChoosenSpectrumType().getDimensionality()==2){
  	        DBSignalDBAtom signalAtom = new DBSignalDBAtom();
  	        signalAtom.setDBShiftRelatedByAxis(shift);
  	        signalAtom.setDBAtom(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[1]), getMolWithH())]);
  	        signalAtom.setAxis(1);
  	        shift.addDBSignalDBAtomRelatedBySignalId(signalAtom);
  	        dbatomsforsignal.add(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[1]), getMolWithH())]);
	        DBSignalDBAtom signalAtom2 = new DBSignalDBAtom();
	        signalAtom2.setDBShiftRelatedByAxis(shift2);
	        signalAtom2.setDBAtom(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[0]), getMolWithH())]);
	        signalAtom2.setAxis(2);
	        shift2.addDBSignalDBAtomRelatedBySignalId(signalAtom2);
	        dbatomsforsignal.add(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[0]), getMolWithH())]);        	  
          }else{
	          if(signalstable.getPeakElements().get(i).getAtomRefs()!=null){
	    	      for (int k = 0; k < signalstable.getPeakElements().get(i).getAtomRefs().length; k++) {
	    	        DBSignalDBAtom signalAtom = new DBSignalDBAtom();
	    	        signalAtom.setDBShiftRelatedByAxis(shift);
	    	        signalAtom.setDBAtom(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[k]), getMolWithH())]);
	    	        signalAtom.setAxis(1);
	    	        shift.addDBSignalDBAtomRelatedBySignalId(signalAtom);
	    	        dbatomsforsignal.add(dbatoms[getAtomNumber( AtomUtils.getAtomById(getMolWithH(), signalstable.getPeakElements().get(i).getAtomRefs()[k]), getMolWithH())]);
	    	      }
	          }
          }
    	  signals[i].setAtoms(dbatomsforsignal);
        }
    }
    Log.debug("after insert signals");
    StringTokenizer st = new StringTokenizer(spectrumLinks, "\n\r");
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      StringTokenizer localst = new StringTokenizer(line, ";");
      DBSpectrumHyperlink shl = new DBSpectrumHyperlink();
      shl.setSpectrumId(spectrumid);
      String link = localst.nextToken();
      if (link.length() < 8 || (!link.substring(0, 7).equals("http://") && !link.substring(0, 8).equals("https://"))) {
        link = "http://" + link;
      }
      shl.setHyperlink(link);
      if (localst.hasMoreTokens()) {
        String des = localst.nextToken();
        shl.setDescription(des);
        shl.setDescriptionSoundex(Soundex.soundex(des, "9", new StringAndInt()));
      } else {
        shl.setDescription(link);
        shl.setDescriptionSoundex("");
      }
      if (!spectrum.getDBSpectrumHyperlinks().contains(shl)) {
        spectrum.addDBSpectrumHyperlink(shl);
      }
    }
    Log.debug("after insert spectrum links");
    st = new StringTokenizer(getSpectrumKeywords(), "\n\r");
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      Criteria crit = new Criteria();
      crit.add(DBKeywordPeer.KEYWORD, line);
      List<DBKeyword> v = DBKeywordPeer.doSelect(crit);
      if (v.size() != 0) {
        DBSpectrumDBKeyword mk = new DBSpectrumDBKeyword();
        mk.setSpectrumId(spectrumid);
        mk.setDBKeyword((DBKeyword) v.get(0));
        try {
        	spectrum.addDBSpectrumDBKeyword(mk);
        } catch (TorqueException ex) {
          //This is to avoid problems if a keyword is given multiple times.
        }
      } else {
        Criteria crit2 = new Criteria();
        crit2.add(DBKeywordPeer.KEYWORD, line);
        crit2.add(DBKeywordPeer.KEYWORD_SOUNDEX, Soundex.soundex(line, "9", new StringAndInt()));
        NumberKey ok = (NumberKey) DBKeywordPeer.doInsert(crit2);
        DBSpectrumDBKeyword mk = new DBSpectrumDBKeyword();
        mk.setSpectrumId(spectrumid);
        mk.setKeywordId(ok.intValue());
        spectrum.addDBSpectrumDBKeyword(mk);
      }
    }
    Log.debug("after insert spectrum keywords");
    if (getIdentifiers().size() > 0) {
      for (int i = 0; i < dbatoms.length; i++) {
        if (getIdentifiers().get(new Integer(i)) != null) {
          DBIdentifier identifier = new DBIdentifier(dbatoms[i].getAtomId(), spectrum, (String) getIdentifiers().get(new Integer(i)));
          spectrum.addDBIdentifier(identifier);
        }
      }
    }
    //insert couplings
    IAtomContainer mol = this.getMolWithH();
    for(int i=0;i<mol.getAtomCount();i++){
		for(int l=0;l<getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").size();l++){
            DBCoupling coupling = new DBCoupling(dbatoms[i].getAtomId(), dbatoms[Integer.parseInt(getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(l).getAttribute("otherAtom").getValue())].getAtomId(), spectrumid, Double.parseDouble(getCMLMolecule().getAtom(i).getChildElements(COUPLING, "http://www.nmrshiftdb.org/").get(l).getAttribute("value").getValue()));
            spectrum.addDBCoupling(coupling);
		}
    }
    if(fullfillsOrder!=0){
    	spectrum.addDBRawFileDBSpectrum(new DBRawFileDBSpectrum(fullfillsOrder, spectrumid));
    }
    return spectrum;
  }


    /**
     * Gets the id under which the molecule in this class was saved, -1 if yet unsaved.
     * 
  	 * @return The id used for saving the molecule in MOLECULE table
  	 */
  	public int getMoleculeId() {
  	    return moleculeid;
  	}

    /**
     * Sets the id under which the molecule in this class was saved, -1 if yet unsaved (can be used if submitting data is created from database).
     * 
     * @param moleculeid
     */
    public void setMoleculeid(int moleculeid) {
    	this.moleculeid=moleculeid;
    }

	/**
	 * Gets the id under which the spectrum in this class was saved, -1 if yet unsaved.
	 * 
	 * @return The id used for saving the spectrum in SPECTRUM table
	 */
    public int getSpectrumid() {
		return spectrumid;
	}
    
    
    /**
     * Sets the id under which the spectrum in this class was saved, -1 if yet unsaved(can be used if submitting data is created from database).
     * 
     * @param spectrumid
     */
    public void setSpectrumid(int spectrumid) {
    	this.spectrumid=spectrumid;
    }


	/**
	 * Returns the id of the existing molecule entry, if any.
	 * 
	 * @return If no new entry was put in MOLECULE, this will contain the id of the existing entry used (if new entry 0)
	 */
    public int getExistingMoleculeId() {
        if(data.getFirstChildElement("existingMoleculeId","http://www.nmrshiftdb.org/")==null){
            return 0;
        }else{
            return Integer.parseInt(data.getFirstChildElement("existingMoleculeId","http://www.nmrshiftdb.org/").getAttribute("existingMoleculeId").getValue());
        }
	}

    /**
	 * If you set this and the smilesNonChiralProv, smilesChiralProv, isChiralProv before save, we do not look for an
	 * existing molecule, but use this
	 *
	 * @param exist the id of the existing molecule
	 */
    public void setExistingMoleculeId(int exist) {
        if(data.getFirstChildElement("existingMoleculeId","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("existingMoleculeId","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("existingMoleculeId",Integer.toString(exist));
        data.getFirstChildElement("existingMoleculeId","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}


	public boolean isShowpeakpicker() {
		return showpeakpicker;
	}


	public void setShowpeakpicker(boolean showpeakpicker) {
		this.showpeakpicker = showpeakpicker;
	}
	
	public String getFrequency() throws TorqueException, NmrshiftdbException{
	    return getCondition(NmrshiftdbConstants.FREQUENCY);
	}
	
	private String getCondition(String conditionName) throws TorqueException, NmrshiftdbException{
		for(int i=0;i<getConditions().size();i++){
			if(getConditions().get(i).getConditionName().equals(conditionName))
			    if(getConditions().get(i).getCurrentValue().equals("") && getConditions().get(i).getCurrentSelectedCondition() != null)
			        getConditions().get(i).getCurrentSelectedCondition().getValue();
			    else
			        return getConditions().get(i).getCurrentValue();
		}
		return "unknown";
	}	

    public String getTemperature() throws TorqueException, NmrshiftdbException{
        return getCondition(NmrshiftdbConstants.temperature);
    }
	
	/**
	 * Gets the molecule of this submit as a mol file.
	 * 
	 * @param comment If not null, this will be in the comment (=third) line of the mol file.
	 * @param title If not null, this will be in the title (=first) line of the mol file.
	 * @param useidentifiers If true, the identifiers will be put in the file as "A   x" fields
	 * @return The mol file, with Hs only of explicitly entered by user.
	 * @throws Exception
	 */
	public String getInputMolAsMdl(String comment, String title, boolean useidentifiers) throws Exception{
        IAtomContainer molwithh = getMolOriginal();
        if(comment!=null)
			molwithh.setProperty(CDKConstants.REMARK, comment);
		if(title!=null)
			molwithh.setProperty(CDKConstants.TITLE, title);
        StringWriter sw = new StringWriter();
        MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
		if(useidentifiers && this.getIdentifiers().size()>0){
		    //we get problems with empty lines, so we set some dummy values
		    if(comment==null)
		        molwithh.setProperty(CDKConstants.REMARK, "dummy");
		    if(title==null)
		        molwithh.setProperty(CDKConstants.TITLE, "dummy");
	        mdlwriter.writeMolecule(molwithh);
		    StringTokenizer st=new StringTokenizer(sw.toString(),"\n");
		    mdlwriter.close();
		    StringBuffer newmol=new StringBuffer();
		    while(st.hasMoreTokens()){
		        String line = st.nextToken();
		        if(line.equals("M  END")){
		            for(int atomnumber : this.getIdentifiers().keySet()){
		                newmol.append("A  "+formatMDLInt(atomnumber+1, 3)).append(System.getProperty("line.separator"));
		                newmol.append(this.getIdentifiers().get(atomnumber)).append(System.getProperty("line.separator"));
		            }
		        }
		        newmol.append(line).append(System.getProperty("line.separator"));
		    }
		    return newmol.toString();
		}else{
	        mdlwriter.writeMolecule(molwithh);
	        mdlwriter.close();
		    return sw.toString();
		}
	}
	
    protected static String formatMDLInt(int i, int l) {
        String s = "", fs = "";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setParseIntegerOnly(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(l);
        nf.setGroupingUsed(false);
        s = nf.format(i);
        l = l - s.length();
        for (int f = 0; f < l; f++)
            fs += " ";
        fs += s;
        return fs;
    }
	
	public IAtomContainer getMolOriginal() throws CDKException, IOException{
	    IAtomContainer molwithh = getMolWithH();
		if(molwithh==null)
			return null;
        for(int i=molwithh.getAtomCount()-1;i>-1;i--){
      	  if(molwithh.getAtom(i).getProperty(VISIBLE)==null || molwithh.getAtom(i).getProperty(VISIBLE).equals("false")){
      		molwithh.removeAtomAndConnectedElectronContainers(molwithh.getAtom(i));
      	  }
        }
        return molwithh;
	}
	
	public static String[] addAtomToAtomRefs(String[] oldRefs, String reftoadd){
		if(oldRefs==null)
			oldRefs = new String[0];
		String[] newRefs=new String[oldRefs.length+1];
		boolean found=false;
		for(int i=0;i<oldRefs.length;i++){
			newRefs[i]=oldRefs[i];
			if(newRefs[i].equals(reftoadd))
				found=true;
		}
		newRefs[newRefs.length-1]=reftoadd;
		if(found)
			return oldRefs;
		else
			return newRefs;
	}
	
	public static String[] addAtomToAtomRefs(String[] oldRefs, IAtom atomtoadd){
		if(oldRefs==null)
			oldRefs=new String[0];
		String[] newRefs=new String[oldRefs.length+1];
		boolean found=false;
		for(int i=0;i<oldRefs.length;i++){
			newRefs[i]=oldRefs[i];
			if(newRefs[i].equals(atomtoadd.getID()))
				found=true;
		}
		newRefs[newRefs.length-1]=atomtoadd.getID();
		if(found)
			return oldRefs;
		else
			return newRefs;
	}


	public int getThisUserId() {
        if(data.getFirstChildElement("userId","http://www.nmrshiftdb.org/")==null){
            return -1;
        }else{
            return Integer.parseInt(data.getFirstChildElement("userId","http://www.nmrshiftdb.org/").getAttribute("userId").getValue());
        }
	}
	public void setThisUserId(int thisUserId) {
        if(data.getFirstChildElement("userId","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("userId","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("userId",Integer.toString(thisUserId));
        data.getFirstChildElement("userId","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public int getThisEditor() {
        if(data.getFirstChildElement("editorId","http://www.nmrshiftdb.org/")==null){
            return -1;
        }else{
            return Integer.parseInt(data.getFirstChildElement("editorId","http://www.nmrshiftdb.org/").getAttribute("editorId").getValue());
        }
	}
	public void setThisEditorId(int thisUserId) {
        if(data.getFirstChildElement("editorId","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("editorId","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("editorId",Integer.toString(thisUserId));
        data.getFirstChildElement("editorId","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public String getThisURL() {
        if(data.getFirstChildElement("userId","http://www.nmrshiftdb.org/")==null){
            return null;
        }else{
            return data.getFirstChildElement("userId","http://www.nmrshiftdb.org/").getAttribute("userId").getValue();
        }
	}
	public void setThisURL(String thisURL) {
        if(data.getFirstChildElement("url","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("url","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("url",thisURL);
        data.getFirstChildElement("url","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public String getJcamp() {
        if(data.getFirstChildElement("jcamp","http://www.nmrshiftdb.org/")==null){
            return null;
        }else{
        	if(data.getFirstChildElement("jcamp","http://www.nmrshiftdb.org/").getAttribute("jcamp").getValue()=="")
        		return null;
        	else
        		return data.getFirstChildElement("jcamp","http://www.nmrshiftdb.org/").getAttribute("jcamp").getValue();
        }
	}
	public byte[] getRawData() throws IOException{
	    return getBase64Encoded("rawdata");
	}
    public byte[] getPdf() throws IOException{
        return getBase64Encoded("pdf");
    }
    public byte[] getImage() throws IOException{
        return getBase64Encoded("image");
    }
    public String getImageFormat() throws IOException{
    	if(getImage()!=null){
    		return data.getFirstChildElement("image","http://www.nmrshiftdb.org/").getAttribute("format").getValue();
        }else{
        	return null;
        }
    }
    public String getRawdataFormat() throws IOException{
    	if(getImage()!=null){
    		return data.getFirstChildElement("rawdata","http://www.nmrshiftdb.org/").getAttribute("format").getValue();
        }else{
        	return null;
        }
    }
    public byte[] getOtherFiles() throws IOException{
        return getBase64Encoded("otherfiles");
    }
    public byte[] getCmcseFile() throws IOException{
        return getBase64Encoded("cmcse");
    }
    
    
    /**
     * Gets an xml element with name xmlName as byte array and decodes it as base64.
     * 
     * @param xmlName The name for the xml element.
     * @return The decoded content.
     */
    public byte[] getBase64Encoded(String xmlName){
        if(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/")==null){
            return null;
        }else{
            if(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/").getAttribute(xmlName).getValue()=="")
                return null;
            else
                return new Base64().decode(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/").getAttribute(xmlName).getValue().getBytes());
        }
    }
    public void setRawData(byte[] image, String format) {
        setBase64Encoded(image, "rawdata");
        if(image!=null){
            Attribute attribute = new Attribute("format",format);
            data.getFirstChildElement("rawdata","http://www.nmrshiftdb.org/").addAttribute(attribute);
        }
    }
    public void setPdf(byte[] pdf) {
        setBase64Encoded(pdf, "pdf");
    }
    public void setOtherFiles(byte[] pdf) {
        setBase64Encoded(pdf, "otherfiles");
    }
    public void setCmcseFile(byte[] pdf) {
        setBase64Encoded(pdf, "cmcse");
    }
    public void setImage(byte[] rawData, String format) {
        setBase64Encoded(rawData, "image");
        if(rawData!=null){
            Attribute attribute = new Attribute("format",format);
            data.getFirstChildElement("image","http://www.nmrshiftdb.org/").addAttribute(attribute);
        }
    }
    
    
    /**
     * This sets a byte array as an xml element with name xmlName and encodes it as base64.
     * 
     * @param filedata The data. If null, the element is removed.
     * @param xmlName Name used for the xml element.
     */
    private void setBase64Encoded(byte[] filedata, String xmlName){
        if(filedata==null){
            if(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/")!=null){
                data.removeChild(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/"));
            }
            return;
        }
        if(data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/")==null){
            Element element = new Element(xmlName,"http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute(xmlName,new String(new Base64().encode(filedata)));
        data.getFirstChildElement(xmlName,"http://www.nmrshiftdb.org/").addAttribute(attribute);
    }
    public void setJcamp(String jcamp) {
		if(jcamp==null)
			jcamp="";
        if(data.getFirstChildElement("jcamp","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("jcamp","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("jcamp",jcamp);
        data.getFirstChildElement("jcamp","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public Date getThisDate() {
        if(data.getFirstChildElement("date","http://www.nmrshiftdb.org/")==null){
            return null;
        }else{
            return new Date(Long.parseLong(data.getFirstChildElement("date","http://www.nmrshiftdb.org/").getAttribute("date").getValue()));
        }
	}
	public void setThisDate(Date thisDate) {
        if(data.getFirstChildElement("date","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("date","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("date",Long.toString(thisDate.getTime()));
        data.getFirstChildElement("date","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public String getReviewFlag() {
        if(data.getFirstChildElement("reviewFlag","http://www.nmrshiftdb.org/")==null){
            return null;
        }else{
            return data.getFirstChildElement("reviewFlag","http://www.nmrshiftdb.org/").getAttribute("reviewFlag").getValue();
        }
	}
	public void setReviewFlag(String reviewFlag) {
        if(data.getFirstChildElement("reviewFlag","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("reviewFlag","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("reviewFlag",reviewFlag);
        data.getFirstChildElement("reviewFlag","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	public void setSmilesNonChiralProv(String smilesNonChiral) {
        if(data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesNonChiral","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("smilesNonChiral","http://www.nmrshiftdb.org/");
            data.getChildCMLElement("molecule", 0).appendChild(element);
        }
		Attribute smilesNonChiralProv = new Attribute("smilesNonChiral",smilesNonChiral);
		data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesNonChiral","http://www.nmrshiftdb.org/").addAttribute(smilesNonChiralProv);
	}
	public void setSmilesChiralProv(String smilesChiral) {
		if(smilesChiral==null)
			smilesChiral="";
        if(data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesChiral","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("smilesChiral","http://www.nmrshiftdb.org/");
            data.getChildCMLElement("molecule", 0).appendChild(element);
        }
		Attribute smilesChiralProv = new Attribute("smilesChiral",smilesChiral);
		data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesChiral","http://www.nmrshiftdb.org/").addAttribute(smilesChiralProv);
	}
	public void setIsChiralProv(boolean isChiral) {
        if(data.getChildCMLElement("molecule", 0).getFirstChildElement("isChiral","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("isChiral","http://www.nmrshiftdb.org/");
            data.getChildCMLElement("molecule", 0).appendChild(element);
        }
		Attribute isChiralProv = new Attribute("isChiral",isChiral ? "true" : "false");
        data.getChildCMLElement("molecule", 0).getFirstChildElement("isChiral","http://www.nmrshiftdb.org/").addAttribute(isChiralProv);
	}
	public String getSmilesNonChiralProv() {
        if(data.getChildCMLElements("molecule").size()>0 && data.getChildCMLElement("molecule", 0).getChildElements("smilesNonChiral","http://www.nmrshiftdb.org/").size()>0)
            return data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesNonChiral","http://www.nmrshiftdb.org/").getAttributeValue("smilesNonChiral");
        else
            return null;
	}
	public boolean getIsChiralProv() {
        if(data.getChildCMLElements("molecule").size()>0 && data.getChildCMLElement("molecule", 0).getChildElements("isChiral","http://www.nmrshiftdb.org/").size()>0)
            return data.getChildCMLElement("molecule", 0).getFirstChildElement("isChiral","http://www.nmrshiftdb.org/").getAttributeValue("isChiral").equals("true");
        else
            return false;
	}
	public String getSmilesChiralProv() {
		if(data.getChildCMLElements("molecule").size()>0 && data.getChildCMLElement("molecule", 0).getChildElements("smilesChiral","http://www.nmrshiftdb.org/").size()>0){
			return data.getChildCMLElement("molecule", 0).getFirstChildElement("smilesChiral","http://www.nmrshiftdb.org/").getAttributeValue("smilesChiral");
		}else{
			return null;
		}
	}
	
	public int getSpectrumForIdentifiers() {
	    if(data.getFirstChildElement("spectrumForIdentifiers","http://www.nmrshiftdb.org/")==null){
	        return -1;
	    }else{
	        return Integer.parseInt(data.getFirstChildElement("spectrumForIdentifiers","http://www.nmrshiftdb.org/").getAttribute("spectrumForIdentifiers").getValue());
	    }
	}

	public void setSpectrumForIdentifiers(int literatureForIdentifiers) {
	    if(data.getFirstChildElement("spectrumForIdentifiers","http://www.nmrshiftdb.org/")==null){
	        Element element = new Element("spectrumForIdentifiers","http://www.nmrshiftdb.org/");
	        data.appendChild(element);
	    }
	    Attribute attribute = new Attribute("spectrumForIdentifiers",Integer.toString(literatureForIdentifiers));
	    data.getFirstChildElement("spectrumForIdentifiers","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}

	public String getSendEmailUserId() {
        if(data.getFirstChildElement("sendEmailUserId","http://www.nmrshiftdb.org/")==null){
            return "-1";
        }else{
            return data.getFirstChildElement("sendEmailUserId","http://www.nmrshiftdb.org/").getAttribute("sendEmailUserId").getValue();
        }
	}

	public void setSendEmailUserId(String sendEmailUserId) {
        if(data.getFirstChildElement("sendEmailUserId","http://www.nmrshiftdb.org/")==null){
            Element element = new Element("sendEmailUserId","http://www.nmrshiftdb.org/");
            data.appendChild(element);
        }
        Attribute attribute = new Attribute("sendEmailUserId",sendEmailUserId);
        data.getFirstChildElement("sendEmailUserId","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}

	public boolean isNoreview() {
		if(data.getChildCMLElements("spectrum").size()==0)
			return false;
	    CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
        if(spectrum.getFirstChildElement("noreview","http://www.nmrshiftdb.org/")==null){
            return false;
        }else{
            return Boolean.parseBoolean(spectrum.getFirstChildElement("noreview","http://www.nmrshiftdb.org/").getAttribute("noreview").getValue());
        }
	}

	public void setNoreview(boolean noreview) {
		  if(data.getChildCMLElements("spectrum").size()==0)
			  data.appendChild(new CMLSpectrum());
	      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
	      if(spectrum.getFirstChildElement("noreview","http://www.nmrshiftdb.org/")==null){
	          Element element = new Element("noreview","http://www.nmrshiftdb.org/");
	          spectrum.appendChild(element);
	      }
	      Attribute attribute = new Attribute("noreview",Boolean.toString(noreview));
	      spectrum.getFirstChildElement("noreview","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}
	
	public boolean isSendCsearch() {
		if(data.getChildCMLElements("spectrum").size()==0)
			return false;
	    CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
        if(spectrum.getFirstChildElement("sendcsearch","http://www.nmrshiftdb.org/")==null){
            return false;
        }else{
            return Boolean.parseBoolean(spectrum.getFirstChildElement("sendcsearch","http://www.nmrshiftdb.org/").getAttribute("sendcsearch").getValue());
        }
	}

	public void setSendCsearch(boolean sendcsearch) {
		  if(data.getChildCMLElements("spectrum").size()==0)
			  data.appendChild(new CMLSpectrum());
	      CMLSpectrum spectrum=((CMLSpectrum)data.getChildCMLElements("spectrum").get(0));
	      if(spectrum.getFirstChildElement("sendcsearch","http://www.nmrshiftdb.org/")==null){
	          Element element = new Element("sendcsearch","http://www.nmrshiftdb.org/");
	          spectrum.appendChild(element);
	      }
	      Attribute attribute = new Attribute("sendcsearch",Boolean.toString(sendcsearch));
	      spectrum.getFirstChildElement("sendcsearch","http://www.nmrshiftdb.org/").addAttribute(attribute);
	}

	  /**
	   * Assigns shifts to atoms according to the "minimum difference" algorithm used for spectrum search.
	   * @throws Exception 
	   * @throws NmrshiftdbException 
	   * @throws CDKException 
	   * @throws DataSetException 
	   * @throws RuntimeException 
	   * @throws TorqueException 
	   * @throws Exception 
	   * @return The prediction results for later use, a map with atom number as key.
	   * @throws CloneNotSupportedException 
	   */
	  public Map<Integer,ValuesForVelocityBean> guessAssignment() throws NmrshiftdbException, CDKException, TorqueException, RuntimeException, DataSetException, IOException, CloneNotSupportedException {
		  	List<CMLPeak> predictions=new ArrayList<CMLPeak>();
		  	Map<Integer,ValuesForVelocityBean> predictionsMap = new TreeMap<Integer,ValuesForVelocityBean>();
		  	IAtomContainer mol=this.getMolWithH();
            if(mol==null)
                return predictionsMap;
            DecimalFormat f = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
    		f.applyPattern("#0.00"); 
		  	for (int i = 0; i < mol.getAtomCount(); i++) {
	            if (mol.getAtom(i).getSymbol().equals(this.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol()) || this.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")) {
	              try {
	                IAtom hatom=null;
	                double[] statistics=null;
	                if(this.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")){
	                  List<IAtom> connatoms=mol.getConnectedAtomsList(mol.getAtom(i));
	                  for(int k=0;k<connatoms.size();k++){
	                    if(((IAtom)connatoms.get(k)).getSymbol().equals("H")){
	                      hatom=(IAtom)connatoms.get(k);
	                      statistics = AtomUtils.predictRange(mol, hatom , false, true, true, false, 0, new StringBuffer(), true, false, null, null, -1, true, null, true, null);
	                      hatom.setProperty("shift", new Double(statistics[1]));
		                  boolean alreadyContainsShift=false;
		                  CMLPeak vt = new CMLPeak();
		                  for(int l=0;l<predictions.size();l++){
			                  	if(predictions.get(l).getXValue()==statistics[1] && predictions.get(l).getXMin()==mol.getAtom(i).getImplicitHydrogenCount()){
			                  		vt=predictions.get(l);
			                  		alreadyContainsShift=true;
			  					  ValuesForVelocityBean vfvb=new ValuesForVelocityBean(new Float(-1),f.format(statistics[0])+" - <b>"+f.format(statistics[1])+"</b> - "+f.format(statistics[2]),statistics[0],statistics[2],"","");
								  predictionsMap.put(new Integer(mol.getAtomNumber(hatom)+1),vfvb);
			                  		break;
			                  	}
		                  }
		                  if(!alreadyContainsShift){
		                      vt.setXValue(statistics[1]);
		                      vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(), mol.getAtom(i)));
		                      vt.setXMin(mol.getAtom(i).getImplicitHydrogenCount());
		  	            	predictions.add(vt);
		  					  ValuesForVelocityBean vfvb=new ValuesForVelocityBean(new Float(-1),f.format(statistics[0])+" - <b>"+f.format(statistics[1])+"</b> - "+f.format(statistics[2]),statistics[0],statistics[2],"","");
							  predictionsMap.put(new Integer(mol.getAtomNumber(hatom)+1),vfvb);
		                  }
	                    }
	                  }
	                }else{
	                  statistics = AtomUtils.predictRange(mol, mol.getAtom(i), false, true, true, false, 0, new StringBuffer(), true, false, null, null, -1, true, null, true, null);
	                  mol.getAtom(i).setProperty("shift", new Double(statistics[1]));
	                  CMLPeak vt = new CMLPeak();
	                  boolean alreadyContainsShift=false;
	                  for(int k=0;k<predictions.size();k++){
	                  	if(predictions.get(k).getXValue()==statistics[1] && predictions.get(k).getXMin()==mol.getAtom(i).getImplicitHydrogenCount()){
	                  		vt=predictions.get(k);
	                  		alreadyContainsShift=true;
	  					  ValuesForVelocityBean vfvb=new ValuesForVelocityBean(new Float(-1),f.format(statistics[0])+" - <b>"+f.format(statistics[1])+"</b> - "+f.format(statistics[2]),statistics[0],statistics[2],"","");
						  predictionsMap.put(new Integer(i+1),vfvb);
	                  		break;
	                  	}
	                  }
	                  if(!alreadyContainsShift){
	                      vt.setXValue(statistics[1]);
	                      vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(), mol.getAtom(i)));
	                      vt.setXMin(mol.getAtom(i).getImplicitHydrogenCount());
	                      vt.setPeakMultiplicity(GeneralUtils.getMultiplicity(AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(i)), false));
	  	            	predictions.add(vt);
	  					  ValuesForVelocityBean vfvb=new ValuesForVelocityBean(new Float(-1),f.format(statistics[0])+" - <b>"+f.format(statistics[1])+"</b> - "+f.format(statistics[2]),statistics[0],statistics[2],"","");
						  predictionsMap.put(new Integer(i+1),vfvb);
	                  }
	                }
	              } catch (CDKException ex) {
	                GeneralUtils.logError(ex, "prediction", null,true);
	              } catch (NmrshiftdbException ex) {
		            GeneralUtils.logError(ex, "prediction", null,true);
		          }
	            }
	        }
		  Collections.sort(predictions, new SignalComparator(false));
		  Map<Double,Double> map;
		  if(predictions.size()>=this.getSignalstable().getPeakChildren().size()){
	    		map = GeneralUtils.getMatchingSearchShift(predictions, this.getSignalstable().getPeakChildren(), 100, new Vector<Double>(), true);
	    	}else{
	    		Map<Double,Double> mapWrongWayRound = GeneralUtils.getMatchingSearchShift(this.getSignalstable().getPeakChildren(), predictions, 100, new Vector<Double>(), false);
	    		map=new HashMap<Double,Double>();
	    		for(Double value : mapWrongWayRound.keySet()){
	    			map.put(mapWrongWayRound.get(value), value);
	    		}
	    	}
		  List<CMLPeak> shifts=this.getSignalstable().getPeakChildren();
		  for(int i=0;i<predictions.size();i++){
			  if(map.containsKey(predictions.get(i).getXValue())){
				  double shift=map.get(predictions.get(i).getXValue());
				  for(int k=0;k<mol.getAtomCount();k++){
					  if(mol.getAtom(k).getProperty("shift")!=null && ((Double)mol.getAtom(k).getProperty("shift")).doubleValue()==predictions.get(i).getXValue()){
						  for(int l=0;l<shifts.size();l++){
							  if(new Double(shifts.get(l).getXValue()).equals(new Double(shift))){
								  shifts.get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(shifts.get(l).getAtomRefs(),mol.getAtom(k)));
								  predictionsMap.get(new Integer(k+1)).setValue((float)shift);
								  break;
							  }
						  }
					  }
				  }
			  }
		  }
		  assignmentGuessed=true;
		  return predictionsMap;
	}

    /**
     * Return the conditions with id 11 (=assignment method).
     * 
     * @return The assignment method.
     * @throws TorqueException
     * @throws NmrshiftdbException 
     */
    public DBConditionType getAssignmentMethod() throws TorqueException, NmrshiftdbException {
        List<DBConditionType> conds=getConditions();
        for(DBConditionType cond : conds){
            if(cond.getConditionTypeId()==11)
                return cond;
        }
        return null;
    }

    /**
     * Return the conditions with id 2 (=solvent).
     * 
     * @return The solvent.
     * @throws TorqueException
     * @throws NmrshiftdbException 
     */
    public DBConditionType getSolvent() throws TorqueException, NmrshiftdbException {
        List<DBConditionType> conds=getConditions();
        for(DBConditionType cond : conds){
            if(cond.getConditionTypeId()==2)
                return cond;
        }
        return null;
    }
    
    public String getLabel(String atomref) throws CDKException, IOException {
    	try{
    	IAtomContainer molWithH=this.getMolWithH();
    	IAtom atom=AtomUtils.getAtomById(molWithH, atomref);
    	IAtom atomheavy=AtomUtils.getAtomById(molWithH, atomref);
    	if(AtomUtils.getAtomById(molWithH, atomref).getSymbol().equals("H"))
    		atomheavy=molWithH.getConnectedAtomsList(AtomUtils.getAtomById(molWithH, atomref)).get(0);
    	IAtomContainer moloriginal=this.getMolOriginal();
    	if(!atom.getSymbol().equals("H") || molWithH.getAtomNumber(atom)>=moloriginal.getAtomCount()) {
    		if(getIdentifiers().get(molWithH.getAtomNumber(atomheavy))!=null)
    			return getIdentifiers().get(molWithH.getAtomNumber(atomheavy));
    		else
    			return Integer.toString(molWithH.getAtomNumber(atomheavy)+1);
    	}else{
    		String heavyid=Integer.toString(molWithH.getAtomNumber(atomheavy)+1);
    		if(getIdentifiers().get(molWithH.getAtomNumber(atomheavy))!=null)
    			heavyid=getIdentifiers().get(molWithH.getAtomNumber(atomheavy));
    		if(getIdentifiers().get(molWithH.getAtomNumber(atom))!=null)
    			return heavyid+"-H ("+ getIdentifiers().get(molWithH.getAtomNumber(atom))+")";
    		else
    			return heavyid+"-H ("+ Integer.toString(molWithH.getAtomNumber(atom)+1)+")";
    	}
    	}catch(Exception ex) {
    		ex.printStackTrace();
    		return "a";
    	}
    }
    
    /**
     * Tells if a list of 2d peaks contains a peak, which is given by x and y values. There is a tolerance of 0.25 ppm.
     * 
     * @param list The 2d peaks.
     * @param x The x shift (only value of the peak is considered).
     * @param y The y shift (only value of the peak is considered).
     * @param swap If true, peaks with x and y swapped count as well, if not, it must be the correct order.
     * @return
     */
    public static boolean containsPeak(List<Peak2D> list, CMLPeak x, CMLPeak y, boolean swap) {
    	if(list==null)
    		return false;
    	for(Peak2D peak : list) {
    		if(peak!=null && x!=null && y!=null && peak.getPosition()!=null && peak.getPosition().length>1 && Math.abs(peak.getPosition()[0]-x.getXValue())<0.25 && Math.abs(peak.getPosition()[1]-y.getXValue())<0.25) {
    			if((peak.assignments==null || peak.assignments.get(0).getTargets()==null || !(peak.assignments.get(0).getTargets()[0] instanceof TwoAtomsReference))) {
    				if(Math.abs(peak.getPosition()[0]-x.getXValue())<0.0001 && Math.abs(peak.getPosition()[1]-y.getXValue())<0.0001) {
    					return true;
    				}
    			}else {
	    			boolean correct1=false;
	    			for(String atomref : x.getAtomRefs()) {
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber1()) {
	    					//System.out.println("1 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    					if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct1=true;
	    				}
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber2()) {
	    					//System.out.println("1 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    					if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct1=true;
	    				}
	    			}
	    			boolean correct2=false;
	    			for(String atomref : y.getAtomRefs()) {
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber1()) {
	    					//System.out.println("2 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    					if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct2=true;
	    				}
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber2()) {
	    					//System.out.println("2 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    					if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct2=true;
	    				}
	    			}
	    			//System.out.println(correct1+" "+correct2);
	    			if(correct1 && correct2)
	    				return correct1 && correct2;
    			}
    		}
    		if(swap && peak!=null && x!=null && y!=null && peak.getPosition()!=null && peak.getPosition().length>1 && Math.abs(peak.getPosition()[1]-x.getXValue())<0.25 && Math.abs(peak.getPosition()[0]-y.getXValue())<0.25) {
    			if((peak.assignments==null || peak.assignments.get(0).getTargets()==null || !(peak.assignments.get(0).getTargets()[0] instanceof TwoAtomsReference))) {
    				if(Math.abs(peak.getPosition()[1]-x.getXValue())<0.0001 && Math.abs(peak.getPosition()[0]-y.getXValue())<0.0001) {
    					return true;
    				}
    			}else {
	    			boolean correct1=false;
	    			for(String atomref : x.getAtomRefs()) {
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber1()) {
	    					//System.out.println("11 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    					if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct1=true;
	    				}
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber2()) {
	    					//System.out.println("11 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    	    			if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct1=true;
	    				}
	    			}
	    			boolean correct2=false;
	    			for(String atomref : y.getAtomRefs()) {
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber1()) {
	    					//System.out.println("22 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    	    			if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct2=true;
	    				}
	    				for(int ass : ((TwoAtomsReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber2()) {
	    					//System.out.println("22 "+atomref.substring(1)+" "+Integer.toString(ass+1));
	    	    			if(atomref.substring(1).equals(Integer.toString(ass+1)))
	    						correct2=true;
	    				}
	    			}
	    			//System.out.println(correct1+" "+correct2);
	    			if(correct1 && correct2)
	    				return correct1 && correct2;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Tells if two peaks are a valid crosspeak. This only considers positions in the molecule, as given by the atomRefs, no shifts.
     * 
     * @param type tocsy, cosy, hmbc, or hsqc.
     * @param x The x shift (atomRefs must be set).
     * @param y The y shift (atomRefs must be set).
     * @return true or false.
     * @throws CDKException
     * @throws IOException
     */
    public boolean isValidCrosspeak(String type, CMLPeak x, CMLPeak y) throws CDKException, IOException{
    	if(x.getAtomRefs()==null || y.getAtomRefs()==null)
    		return false;
    	IAtomContainer ac=this.getMolWithH();
    	IAtomContainer ac2=new AtomContainer(ac);
    	if(type.contains("tocsy")){
        	for(IAtom atom : ac2.atoms()){
        		if(!atom.getSymbol().equals("H")){
	        		boolean hashydrogen=false;
	        		for(IAtom neighbour : ac2.getConnectedAtomsList(atom)){
	        			if(neighbour.getSymbol().equals("H")){
	        				hashydrogen=true;
	        				break;
	        			}
	        		}
	        		if(!hashydrogen)
	        			ac2.removeAtomAndConnectedElectronContainers(atom);
        		}
        	}
    	}
    	int mindistance=Integer.MAX_VALUE;
    	int maxdistance=0;
    	boolean connected=false;
    	for(String s1 : x.getAtomRefs()){
			IAtom a1=AtomUtils.getAtomById(ac, s1);
			if(a1!=null){
	    		for(String s2 : y.getAtomRefs()){
	    			IAtom a2=AtomUtils.getAtomById(ac, s2);
	    			if(type.contains("tocsy")){
	    				IAtom a1new=null;
	    				IAtom a2new=null;
	    				for(IAtom atom : ac2.atoms()){
	    					if(atom.getID().equals(a1.getID()))
	    							a1new=atom;
	    					if(atom.getID().equals(a2.getID()))
	    							a2new=atom;
	    				}
	    				ShortestPaths sp = new ShortestPaths(ac2, a1new);
	    				if(sp.nPathsTo(a2new)>0)
	    					connected=true;
	    			}else{
	    				ShortestPaths sp = new ShortestPaths(ac, a1);
		    		    int distance = sp.distanceTo(a2);
		    		    if(distance<mindistance)
		    		    	mindistance=distance;
		    		    if(distance>maxdistance)
		    		    	maxdistance=distance;
	    			}
	    		}
			}
    	}
    	if(type.contains("hmbc") && ((mindistance<=2 && maxdistance>=2) || (mindistance<=3 && maxdistance>=3)))
    		return true;
    	else if(type.contains("hsqc") && mindistance<=1 && maxdistance>=1)
    		return true;
    	else if(type.contains("cosy") && mindistance<=3 && maxdistance>=3)//4?
    		return true;
    	else if(type.contains("tocsy") && connected)
    		return true;//noesy in space
    	else
    		return false;
	}
}
