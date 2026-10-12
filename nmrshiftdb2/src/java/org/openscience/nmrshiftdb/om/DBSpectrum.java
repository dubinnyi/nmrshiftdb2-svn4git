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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.codec.binary.Base64;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.RunData;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.parser.JCAMPWriter;
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
import org.jcamp.spectrum.Spectrum1D;
import org.jcamp.spectrum.assignments.AtomReference;
import org.jcamp.spectrum.assignments.TwoAtomsReference;
import org.jcamp.spectrum.notes.NoteDescriptor;
import org.jcamp.units.CommonUnit;
import org.jcamp.units.Unit;
import org.nmrshiftdb.www.schemas._2006._10.nmrshiftdbtypes.PublishParameters;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.PublishRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.PublishResponseValueDocument;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.comparator.DBSignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ValuesForVelocityComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.openscience.nmrshiftdb.webservices.NMRShiftDBStub;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLPeakStructure;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import de.unikoeln.chemie.nmr.data.NMR2DSpectrum;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.Peak2D;
import de.unikoeln.chemie.nmr.data.SelectiveNMR1DSpectrum;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 */
public class DBSpectrum
     extends org.openscience.nmrshiftdb.om.BaseDBSpectrum
     implements Persistent {
	
      static final long serialVersionUID=1023290511226111144L;
      public static final String IMAGE_TYPE = "image";
      public static final String PDF_TYPE = "pdf";
      public static final String RAW_DATE_TYPE = "rawdata";
      public static final String JCAMP_TYPE = "jcamp";
      public static final String CSEARCH_TYPE = "csearchreport";
      public static final String OTHERFILES_TYPE = "otherfiles";
      public static final String NMRSHIFTDB_TYPE = "nmrshiftdb2";
	  public static final String CMCSE_TYPE = "cmcse";
      
	  /**
	   *  Description of the Field
	   */
	  public RunData runData = null;
	  public boolean predict=false;
	  public List<DBCondition> measurementConditions=null;

	  double similarity = -1;
	  Map<Double,Double> searchSignalMap = null;
	  int intensities = -1;
	  Vector<ValuesForVelocityBean> options = null;
	  String sortorder="";
	  StringBuffer unusedtext = new StringBuffer("");
	  DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
	  List<DBLiterature> literaturesNew=null;
	  List<DBSpectrumDBKeyword> keywordsVector=null;
	  int otherspectrastringsize=-1;
	  private String molImage;
	  
	  /**
	   *Constructor for the DBMolecule object
	   */
	  public DBSpectrum() {
	    super();
	    format.applyPattern("0.00");
	  }

	  /**
	   * This inits DBxx collections in this object to empty. This should only be used for 
	   * "dummy" objects as in the toDBSpectrum method of submitting data. Calling this on 
	   * objects retrieved from db will cause relations not being fetched properly. 
	   */
	  public void initEmptyCollections(){
		  initDBSpectrumDBConditions();
		  initDBSignals();
		  initDBSpectrumDBKeywords();
		  initDBSpectrumHyperlinks();
		  initDBCouplings();
	  }

	  /**
	   *Gets the condition type of this DBSpectrum object
	   *
	   * @return                The conditionType, "m"=measured spectrum, "c"=calculatedSpectrum, "p"=predicted spectrum.
	   * @exception  Exception  Description of Exception
	   */
	  public String getConditionType()
	       throws TorqueException {
	    if(collDBSpectrumDBConditions!=null && collDBSpectrumDBConditions.size()>0)
			  return ((DBConditionType)((DBCondition)((DBSpectrumDBCondition)collDBSpectrumDBConditions.get(0)).getDBCondition()).getDBConditionType()).getConditionType();
			List<DBCondition> l=this.getMeasurementConditions();
			if(l.size()==0)
				return "u";
		    Criteria crit = new Criteria();
		    crit.add(DBConditionTypePeer.CONDITION_TYPE_ID, ((DBCondition) l.get(0)).getConditionTypeId());
		    List<DBConditionType> l2=DBConditionTypePeer.doSelect(crit);
		    if(l2.size()==0)
		    	return "u";
		    DBConditionType condType = l2.get(0);
		    return (condType.getConditionType());
	  }


	  /**
	   *Gets the links of the DBSpectrum object
	   *
	   * @return                The links
	   * @exception  Exception  Database problems
	   */
	  public List<DBSpectrumHyperlink> getLinks() throws Exception {
	    Criteria crit = new Criteria();
	    crit.add(DBSpectrumHyperlinkPeer.SPECTRUM_ID, this.getSpectrumId());
	    return (DBSpectrumHyperlinkPeer.doSelect(crit));
	  }


	  /**
	   *Returns the similarity in percent between this spectrum and another spectrum, set by initSimilarity
	   *
	   * @return    A double from 0 to 100
	   */
	  public double getSimilarity() {
	    return (similarity);
	  }


	  /**
	   *  Tells if the similarity of this spectrum is zero or not
	   *
	   * @return    The similarityNot0 value
	   */
	  public boolean isSimilarityNot0() {
	    return (similarity != 0);
	  }


	  /**
	   *Returns the similarity with two digits after .
	   *
	   * @return    A double from 0 to 100
	   */
	  public double getSimilarityFormatted() {
	    return ((java.lang.Math.floor(similarity * 100)) / 100);
	  }


	  /**
	   *Gets the shifts table for this spectrum. This is a vector of ValuesForVelocityBean objects. In case the runData object is set a prediction is done.
	   *
	   * @return                The shifts table.
	   * @throws NmrshiftdbException 
	   * @throws DataSetException 
	   * @throws IOException 
	   * @exception  Exception  Database problems.
	   */
	  public List<ValuesForVelocityBean> getOptions(int ordercolumn,boolean updown) throws TorqueException, NmrshiftdbException, DataSetException, IOException {
		  try{
	    if (options == null || !sortorder.equals(ordercolumn+""+sortorder)) {
	      if(runData!=null && runData.getSession().getAttribute("trueonly")==null)
	        runData.getSession().setAttribute("trueonly",new Boolean(true));
	      Criteria crit=new Criteria();
	      crit.add(DBSignalPeer.SPECTRUM_ID,getSpectrumId());
	      crit.addJoin(DBSignalPeer.SIGNAL_ID,DBShiftPeer.SIGNAL_ID);
	      List<DBShift> allshifts=DBShiftPeer.doSelect(crit);
	      int highestNumberOfDigitsAfterComma=0;
	      for(int k=0;k<allshifts.size();k++){
	        int numberOfDigitsAfterComma=GeneralUtils.getDigitsAfterComma(allshifts.get(k).getValue());
	        if(numberOfDigitsAfterComma>highestNumberOfDigitsAfterComma)
	          highestNumberOfDigitsAfterComma=numberOfDigitsAfterComma;
	      }
	      Vector<ValuesForVelocityBean> v = new Vector<ValuesForVelocityBean>();
	      boolean isHSpectrum = getDBSpectrumType().getElementSymbol().equals("H");
	      if(this.getDBMolecule()!=null){
	    	  if(this.getDBSpectrumType().getDimensionality()>1){
		    	  List<DBSignal> l=this.getDBSignals();
		    	  for(int i=0;i<l.size();i++){
			    	  ValuesForVelocityBean vfvb=new ValuesForVelocityBean(l.get(i));
			    	  v.add(vfvb);
		    	  }
	    	  }else{
		    	  DBAtom[] dbatoms = getDBMolecule().getAtomsAsArrayInMdlOrder();
			      int atomnumber = 0;
			      int csearchspectrumid=0;
			      crit=new Criteria();
			      crit.add(DBSpectrumPeer.MOLECULE_ID,this.getMoleculeId());
			      crit.add(DBSpectrumPeer.COMMENT,"Prediction via CSEARCH");
			      List<DBSpectrum> spectra=DBSpectrumPeer.doSelect(crit);
			      if(spectra.size()>0)
			    	  csearchspectrumid=spectra.get(0).getSpectrumId();
			      for (int k = 0; k < dbatoms.length; k++) {
			        if (!dbatoms[k].getSymbol().equals("H")) {
			          if (isHSpectrum) {
			        	Vector<DBAtom> neighbouringAtoms = dbatoms[k].getConnectedAtoms(getDBMolecule().getAsCDKMolecule(1),dbatoms);
			            int numberOfHs = 0;
			            for (int l = 0; l < neighbouringAtoms.size(); l++) {
			              if (neighbouringAtoms.get(l).getSymbol().equals("H")) {
			                numberOfHs++;
			              }
			            }
			            if (numberOfHs > 0) {
			              Vector<Float> shifts = new Vector<Float>();
			              StringBuffer intensities = new StringBuffer();
			              Vector<StringBuffer> comments = new Vector<StringBuffer>();
			              StringBuffer displayString = new StringBuffer();
			              StringBuffer searchShift = new StringBuffer();
			              StringBuffer delta = new StringBuffer();
			              StringBuffer multis = new StringBuffer();
			              double[] prediction=null;
			              for (int l = 0; l < neighbouringAtoms.size(); l++) {
			                if (neighbouringAtoms.get(l).getSymbol().equals("H")) {
			                  DBSignal anysignal=neighbouringAtoms.get(l).getAnySignal(this.getSpectrumId());
			                  if (anysignal!= null) {
			                    float shift = anysignal.getFirstShift().getValue();
			                    float deltaPpm = -1;
			                    if (!shifts.contains(new Float(shift))) {
			                      shifts.add(new Float(shift));
			                      if (anysignal.getIntensity() != 0) {
			                        intensities.append(anysignal.getIntensity() + ";");
			                      }
			                      comments.add(new StringBuffer());
			                	  StringBuffer comment=new StringBuffer();
			                      if (searchSignalMap != null) {
			                        if (searchSignalMap.get(new Double(shift)) != null) {
			                          deltaPpm = Math.abs((searchSignalMap.get(new Double(shift))).floatValue() - shift);
			                        }
			                        searchShift.append((searchSignalMap.get(new Double(shift)) == null ? "" : searchSignalMap.get(new Double(shift)).toString() + ","));
			                      }else{
			                    	  if (runData != null && predict) {
			                    		  try{
			                    			  prediction = AtomUtils.predictRange(getDBMolecule().getAsCDKMolecule(1), getDBMolecule().getAsCDKMolecule(1).getAtom(neighbouringAtoms.get(l).getMdlNumber()), false, true, true, false, 0, comment, false, true, runData, null, -1, true,null, ((Boolean)runData.getSession().getAttribute("trueonly")).booleanValue(), null);
			                    			  searchShift.append(new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment);
			                    		  }catch(Exception ex){
			                    			  prediction=new double[7];
			                    			  searchShift.append(new String("No prediction possible!"));
			                    		  }
			                    	  }
			                      }
			                    }
			                    for (int m = 0; m < shifts.size(); m++) {
			                      if (shifts.get(m).floatValue() == shift && neighbouringAtoms.get(l).getIsVisible().equals(NmrshiftdbConstants.TRUE) ) {
			                        if(neighbouringAtoms.get(l).getIdentifier(this).equals(""))
			                            comments.get(m).append("H " +getMdlNumberPlus1(neighbouringAtoms.get(l),dbatoms) + ", ");
			                        else
			                            comments.get(m).append("H " +neighbouringAtoms.get(l).getIdentifier(this) + ", ");
			                        break;
			                      }
			                    }
			                    delta.append(deltaPpm==-1 ? "" : format.format(deltaPpm) + ",");
			                  }
			                }
			                if (!neighbouringAtoms.get(l).getMultiplicity(this).equals("")) {
			                  multis.append(neighbouringAtoms.get(l).getMultiplicity(this) + ";");
			                }
			              }
			              for (int m = 0; m < shifts.size(); m++) {
			                displayString.append(shifts.get(m) + "");
			                int digitsAfterComma=GeneralUtils.getDigitsAfterComma(shifts.get(m).floatValue());
			                for(int n=digitsAfterComma;n<highestNumberOfDigitsAfterComma;n++){
			                  if(displayString.indexOf(".0")!=displayString.length()-2 || n>digitsAfterComma)
			                    displayString.append("0");
			                }
			                if(!GeneralUtils.removeLastComma(comments.get(m)).toString().equals(""))
			                    displayString.append(" (" + GeneralUtils.removeLastComma(comments.get(m)) + ")");
			                displayString.append(", ");
			              }
			              GeneralUtils.removeLastComma(displayString);
			              if (prediction == null) {
			                GeneralUtils.removeLastComma(searchShift);
			                v.add(new ValuesForVelocityBean(new Float(atomnumber), searchShift.toString(), 0, 0, displayString.toString(), (atomnumber + 1) + (isHSpectrum ? "-H" : ""), delta.toString(), GeneralUtils.removeLastComma(multis).toString()));
			              } else {
			                v.add(new ValuesForVelocityBean(new Float(atomnumber), searchShift.toString(), prediction[0], prediction[2], displayString.toString(), (atomnumber + 1) + (isHSpectrum ? "-H" : ""), delta.toString(), GeneralUtils.removeLastComma(multis).toString()));
			              }
			              v.get(v.size() - 1).setIntensity(GeneralUtils.removeLastComma(intensities).toString());
			              if(!dbatoms[k].getIdentifier(this).equals(""))
			                  v.get(v.size() - 1).setIdentifier(dbatoms[k].getIdentifier(this)+ (isHSpectrum ? "-H" : ""));
			              if(shifts.size()>0)
			            	  v.get(v.size() - 1).setShift(1,0,shifts.get(0));
			              else
			            	  v.get(v.size() - 1).setShift(1,0,0);
			              v.get(v.size() - 1).setCsearch("Not possible for H");
			            }
			          } else {
			            float[] shifts = getShifts(k, this.getDBMolecule());
			            float shift;
			            if (shifts.length > 0) {
			              shift = shifts[0];
			              int digitsAfterComma=GeneralUtils.getDigitsAfterComma(shift);
			              float deltaPpm = -1;
			              if (searchSignalMap != null) {
			                if (searchSignalMap.get(new Double(shift)) != null) {
			                  deltaPpm = Math.abs((searchSignalMap.get(new Double(shift))).floatValue() - shift);
			                }
			                v.add(new ValuesForVelocityBean(new Float(atomnumber), (searchSignalMap.get(new Double(shift)) == null ? "" : searchSignalMap.get(new Double(shift)).toString()), 0, 0, (shift == -1 ? "" : shift + ""), (atomnumber + 1) + (isHSpectrum ? "-H" : ""), deltaPpm==-1 ? "": format.format(deltaPpm)));
			              } else {
			                if (runData == null || !predict) {
			                  v.add(new ValuesForVelocityBean(new Float(atomnumber), "", 0, 0, (shift == -1 ? "" : shift + "") + "", (atomnumber + 1) + (isHSpectrum ? "-H" : "")));
			                } else {
			                  try {
			                    IAtomContainer cdkMolWithH = DBMolecule.getAsCDKMolecule(getDBMolecule().getMoleculeId(),1);
			                    StringBuffer comment = new StringBuffer();

			                    double[] prediction = AtomUtils.predictRange(cdkMolWithH, cdkMolWithH.getAtom(k), false, true, true, false, 0, comment, false, true, runData, null, -1, true,null, ((Boolean)runData.getSession().getAttribute("trueonly")).booleanValue(), null);
			                    if (prediction[0] > prediction[1]) {
			                      prediction[0] = prediction[1];
			                    }
			                    if (prediction[2] < prediction[1]) {
			                      prediction[2] = prediction[1];
			                    }
			                    if (shift < prediction[0] || shift > prediction[2]) {
			                      v.add(new ValuesForVelocityBean(new Float(atomnumber), new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment, (java.lang.Math.floor(prediction[0] * 100)) / 100, (java.lang.Math.ceil(prediction[2] * 100)) / 100, (shift == -1 ? "" : shift + "") + "", (atomnumber + 1) + (isHSpectrum ? "-H" : ""), false));
			                    } else {
			                      v.add(new ValuesForVelocityBean(new Float(atomnumber), new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment, (java.lang.Math.floor(prediction[0] * 100)) / 100, (java.lang.Math.ceil(prediction[2] * 100)) / 100, (shift == -1 ? "" : shift + "") + "", (atomnumber + 1) + (isHSpectrum ? "-H" : "")));
			                    }
			                  } catch (CDKException ex) {
			                    v.add(new ValuesForVelocityBean(new Float(atomnumber), "No data available", 0, 1000, (shift == -1 ? "" : shift + "") + "", (atomnumber + 1) + (isHSpectrum ? "-H" : "")));
			                  } catch (Exception ex) {
			                    v.add(new ValuesForVelocityBean(new Float(atomnumber), "No data available", 0, 1000, (shift == -1 ? "" : shift + "") + "", (atomnumber + 1) + (isHSpectrum ? "-H" : "")));
			                    GeneralUtils.logError(ex, "predict", runData,true);
			                  }
			                }
			              }
			              if (((DBSignal) getSignals(k,this.getDBMolecule()).get(0)).getIntensity() != 0) {
			                v.get(v.size() - 1).setIntensity(((DBSignal) getSignals(k,getDBMolecule()).get(0)).getIntensity() + "");
			              }
			              v.get(v.size() - 1).setMultiplicity(dbatoms[k].getMultiplicity(this));
			              v.get(v.size() - 1).setIdentifier(dbatoms[k].getIdentifier(this));
			              v.get(v.size() - 1).setShift(1, 0, shift);
			              if(this.getSpectrumTypeName().equals("13C")){
			            	  if(csearchspectrumid>0){
			            		  crit= new Criteria();
			            		  crit.addJoin(DBSignalPeer.SIGNAL_ID, DBSignalDBAtomPeer.SIGNAL_ID);
			            		  crit.add(DBSignalPeer.SPECTRUM_ID,csearchspectrumid);
			            		  crit.add(DBSignalDBAtomPeer.ATOM_ID,dbatoms[k].getAtomId());
			            		  List<DBSignal> l = DBSignalPeer.doSelect(crit);
			            		  if(l.size()>0)
			            		  v.get(v.size() - 1).setCsearch(Float.toString(l.get(0).getFirstShift().getValue()));
			            	  }else{
			            		  v.get(v.size() - 1).setCsearch("CSEARCH prediction has not yet arrived");
			            	  }
			              }else{
			                  v.get(v.size() - 1).setCsearch("Not possible for "+this.getSpectrumTypeName());
			              }
			              if(!v.get(v.size() - 1).getRange().equals("-1")){
			                StringBuffer x=new StringBuffer(v.get(v.size() - 1).getRange());
			                for(int n=digitsAfterComma;n<highestNumberOfDigitsAfterComma;n++){
			                  x.append("0");
			                }
			                v.get(v.size() - 1).setRange(x.toString());
			              }
			            }
			          }
			        }
			        if (dbatoms[k].getIsVisible().equals(NmrshiftdbConstants.TRUE)) {
			          atomnumber++;
			        }
			      }
	    	  }
	          Collections.sort(v, new ValuesForVelocityComparator(ordercolumn,updown, this.getDBSpectrumType().getDimensionality()));
	      }
	      options = v;
	    }
	    return (options);
		  }catch(TorqueException ex){
			  ex.printStackTrace();
			  GeneralUtils.logError(ex, "getoptions", null, true);
			  throw ex;
          }catch(NmrshiftdbException ex){
              ex.printStackTrace();
              GeneralUtils.logError(ex, "getoptions", null, true);
              throw ex;
          }catch(DataSetException ex){
              ex.printStackTrace();
              GeneralUtils.logError(ex, "getoptions", null, true);
              throw ex;
          }catch(IOException ex){
              ex.printStackTrace();
              GeneralUtils.logError(ex, "getoptions", null, true);
              throw ex;
          }
	  }

	  
		/**
		 *Gets the mdl number of an atom, similar to getMdlNumberPlus1 in DBAtom, but much faster for repeated calls
		 *
		 * @param      atom       The atom to ask for
		 * @param      atoms      All atoms of this molecule, as you get them with DBMolecule.getAtomsAsArrayInMDLOrder
		 * @return                The mdl number
		 * @exception  NmrshiftdbException  No such atom in atoms. This means the atoms passed where from another molecule than the atom.
		 */
	  private int getMdlNumberPlus1(DBAtom atom,DBAtom[] atoms)throws NmrshiftdbException{
		  for(int i=0;i<atoms.length;i++){
			  if(atoms[i].getAtomId()==atom.getAtomId())
				  return 1+i;
		  }
		  throw new NmrshiftdbException("no such atom in DBSpectrum.getMdlNumberPlus1");
	  }

	  /**
	   *Gets the Conditions of this DBSpectrum object
	   *
	   * @return                A vector of DBConditions
	   * @exception  Exception  Description of Exception
	   */
	  public List<DBCondition> getMeasurementConditions()
	       throws TorqueException {
	    if(measurementConditions==null){
	    	Connection conn=null;
	    	try{
	    	    conn=Torque.getConnection();
	    		return getMeasurementConditions(conn);
	    	}
	    	finally{
	    		Torque.closeConnection(conn);
	    	}
	    }
	    return(measurementConditions);
	  }


	  /**
	   *Gets the Conditions of this DBSpectrum object
	   *
	   * @return                A vector of DBConditions
	   * @exception  Exception  Description of Exception
	   */
	  public List<DBCondition> getMeasurementConditions(Connection dbconn)
	       throws TorqueException {
	    if(measurementConditions==null){
	      Criteria crit = new Criteria();
	      crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
	      crit.add(DBSpectrumDBConditionPeer.SPECTRUM_ID, this.getSpectrumId());
	      //this is to get a defined order - the join to DBConditionType should not be needed, but gives npe with crit.addAscendingOrderByColumn(DBConditionTypePeer.CONDITION_ID);
	      crit.addJoin(DBConditionPeer.CONDITION_TYPE_ID,DBConditionTypePeer.CONDITION_TYPE_ID);
	      crit.addAscendingOrderByColumn(DBConditionTypePeer.CONDITION_TYPE_ID);
	      measurementConditions=DBConditionPeer.doSelect(crit,dbconn);
	      for(int i=0;i<measurementConditions.size();i++){
	        measurementConditions.get(i).getDBConditionType(dbconn);
	      }
	    }
	    return(measurementConditions);
	  }


	  /**
	   *Gets the value of a certain condition of this DBSpectrum object
	   *
	   * @return                The value.
	   * @param  name           The name of the conditon to tell.
	   * @exception  Exception  Description of Exception.
	   */
	  public DBCondition getMeasurementConditionWithName(String name)
	       throws TorqueException {
	    return getMeasurementConditionWithName(name, null);
	  }

	  
	  /**
	   * Gets the value of the condition of type frequency assigned to this spectrum.
	   * 
	   * @return The frequency value.
	   * @throws TorqueException Database problems.
	   */
	  public String getFrequency() throws TorqueException{
		  return this.getMeasurementConditionValueWithName(NmrshiftdbConstants.FREQUENCY);
	  }

	  /**
	   * Gets the value of the condition of type temperature assigned to this spectrum.
	   * 
	   * @return The temperature value.
	   * @throws TorqueException Database problems.
	   */
	  public String getTemperature() throws TorqueException{
		  return this.getMeasurementConditionValueWithName(NmrshiftdbConstants.TEMPERATURE);
	  }

	  /**
	   *Gets the value of a certain condition of this DBSpectrum object
	   *
	   * @return                The value.
	   * @param  name           The name of the conditon to tell.
	   * @exception  Exception  Description of Exception.
	   */
	  public DBCondition getMeasurementConditionWithName(String name, Connection dbconn)
	       throws TorqueException {
	    if(measurementConditions==null){
	      if(dbconn!=null)
	        getMeasurementConditions(dbconn);
	      else
	        getMeasurementConditions();
	    }
	    for(int i=0;i<measurementConditions.size();i++){
	      if(measurementConditions.get(i).getDBConditionType().getConditionName().equals(name)){
	        return measurementConditions.get(i);
	      }
	    }
	    return null;
	  }


	  public String getMeasurementConditionValueWithName(String name)
	       throws TorqueException {
	    DBCondition cond=getMeasurementConditionWithName(name,null);
	    if(cond==null)
	      return "Unknown";
	    else
	      return cond.getValue();
	  }


	  /**
	   *Gets the specFile attribute of the DBSpectrum object as a string
	   *
	   * @return    The specFile value
	   */
	  public String getSpectrumForApplet() {
	    return (this.getSpecfile());
	  }


	  /**
	   *Gets the contributor of this DBSpectrum object
	   *
	   * @return                The contributor value
	   * @exception  Exception  Something went wrong with reading the database
	   */
	  public TurbineUser getContributor()
	       throws Exception {
	    return (TurbineUserPeer.retrieveByPK(this.getUserId()));
	  }
	  
	  
	  /**
	   *Gets the spectrumType name of the DBSpectrum object
	   *
	   * @return                The spectrumType name value
	   * @exception  Exception  Problems with reading database
	   */
	  public String getSpectrumTypeName()
	       throws TorqueException {
	    return this.getDBSpectrumType().getName();
	  }


	  /**
	   * Gets all DBUserDBSpectrum objects related with this spectrum
	   *
	   * @return                The reviewAssignments, vector of DBUserDBSpectrum obejects
	   * @exception  Exception  Database problems
	   */
	  public List<DBUserDBSpectrum> getReviewAssignments()
	       throws TorqueException {
	    Criteria crit = new Criteria();
	    crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
	    crit.addAscendingOrderByColumn(DBUserDBSpectrumPeer.ASSIGNMENT_DATE);
	    List<DBUserDBSpectrum> v = DBUserDBSpectrumPeer.doSelect(crit);
	    return (v);
	  }
	  
	  /**
       * Gets the DBUserDBSpectrum objects related with this spectrum where the 
       * review data is not null, i. e. a review was actually done and not just 
       * assigned. There should be only one or none.
       *
       * @return                The reviewAssignments or null.
       * @exception  Exception  Database problems
       */
      public DBUserDBSpectrum getReviewDone()
           throws TorqueException {
        Criteria crit = new Criteria();
        crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
        crit.add(DBUserDBSpectrumPeer.REVIEW_DATE, (Object)"REVIEW_DATE is not NULL", Criteria.CUSTOM);
        crit.addAscendingOrderByColumn(DBUserDBSpectrumPeer.ASSIGNMENT_DATE);
        List<DBUserDBSpectrum> v = DBUserDBSpectrumPeer.doSelect(crit);
        if(v.size()==0)
            return null;
        else
            return ((DBUserDBSpectrum)v.get(0));
      }


	  /**
	   *Gets the name of the last reviwer the DBSpectrum object was assigned to
	   *
	   * @return                The name
	   * @exception  TorqueException  Database problems
	   */
	  public String getNameOfAsssignedReviewer() throws TorqueException {
	    if (getReviewAssignments().size() > 0) {
	      return (NmrshiftdbUserPeer.retrieveByPK(((DBUserDBSpectrum) getReviewAssignments().get(getReviewAssignments().size() - 1)).getUserId()).getFirstName().substring(0, 1) + ". " + NmrshiftdbUserPeer.retrieveByPK(((DBUserDBSpectrum) getReviewAssignments().get(getReviewAssignments().size() - 1)).getUserId()).getLastName());
	    } else {
	      return ("No assignment");
	    }
	  }


      /**
       * Gets the date of the last review the DBSpectrum object was assigned to.
       *
       * @return                The date.
       * @exception  TorqueException  Database problems
       */
      public Date getReviewDate() throws TorqueException {
        if (getReviewDone()!=null) {
          return (getReviewDone().getReviewDate());
        } else {
          return (null);
        }
      }
      
      
      /**
       *Gets the name of the last reviwer the DBSpectrum object was assigned to
       *
       * @return                The name
       * @exception  TorqueException  Database problems
       */
      public String getNameOfActualReviewer() throws TorqueException {
        if (getReviewDone()!=null) {
          return (NmrshiftdbUserPeer.retrieveByPK(getReviewDone().getUserId()).getFirstName().substring(0, 1) + ". " + NmrshiftdbUserPeer.retrieveByPK(getReviewDone().getUserId()).getLastName());
        } else {
          return ("No assignment");
        }
      }
      
      
      /**
       *Gets the email of the last reviewer the DBSpectrum object was assigned to
       *
       * @return                The email
       * @exception  TorqueException  Database problems
       */
      public String getEmailOfActualReviewer() throws TorqueException {
        if (getReviewDone()!=null) {
          return (NmrshiftdbUserPeer.retrieveByPK(getReviewDone().getUserId()).getEmail());
        } else {
          return ("No assignment");
        }
      }

      
      /**
	   *Gets the email of the last reviewer the DBSpectrum object was assigned to
	   *
	   * @return                The email
	   * @exception  TorqueException  Database problems
	   */
	  public String getEmailOfAsssignedReviewer() throws TorqueException {
	    if (getReviewAssignments().size() > 0) {
	      return (NmrshiftdbUserPeer.retrieveByPK(((DBUserDBSpectrum) getReviewAssignments().get(getReviewAssignments().size() - 1)).getUserId()).getEmail());
	    } else {
	      return ("No assignment");
	    }
	  }


	  /**
	   *Gets the Date the last assignment of the DBSpectrum object took place
	   *
	   * @return                The asssignment Date value
	   * @exception  Exception  Database problems
	   */
	  public java.util.Date getAsssignedDate() throws Exception {
	    List<DBUserDBSpectrum> v = getReviewAssignments();
	    if (v.size() > 0) {
	      return (((DBUserDBSpectrum) v.get(v.size() - 1)).getAssignmentDate());
	    } else {
	      return (new java.util.Date());
	    }
	  }


	  /**
	   *Gets the comment attribute of the DBSpectrum object
	   *
	   * @return    The comment value
	   */
	  public String getCommentAsString() {
	    if (getComment() != null) {
	      return new String(getComment());
	    } else {
	      return ("");
	    }
	  }


	  /**
	   *Gets the links of the DBSpectrum object as a string
	   *
	   * @return                The links value
	   * @exception  Exception  Description of Exception
	   */
	  public String getLinksAsString() throws Exception {
	    StringBuffer links = new StringBuffer();
	    List<DBSpectrumHyperlink> v = getLinks();
	    for (int i = 0; i < v.size(); i++) {
	      links.append(((DBSpectrumHyperlink) v.get(i)).getHyperlinkSimple() + ";" + ((DBSpectrumHyperlink) v.get(i)).getDescription() + "\n");
	    }
	    return (links.toString());
	  }


	  /**
	   *  Gets the DBLiterature objects of the DBSpectrum object
	   *
	   * @return                The literature value
	   * @throws Exception 
	   * @throws NmrshiftdbException 
	   * @exception  Exception  Description of Exception
	   */
	  public List<DBLiterature> getLiteraturesNew() throws NmrshiftdbException, TorqueException {
	    if(literaturesNew==null){
	      literaturesNew=new Vector<DBLiterature>();
	      Criteria crit = new Criteria();
	      crit.add(DBSpectrumDBLiteraturePeer.SPECTRUM_ID, this.getSpectrumId());
	      List<DBSpectrumDBLiterature> v = DBSpectrumDBLiteraturePeer.doSelect(crit);
	      for (int i = 0; i < v.size(); i++) {
	        DBLiterature lit = DBLiteraturePeer.retrieveByPK(((DBSpectrumDBLiterature) v.get(i)).getLiteratureId());
	        literaturesNew.add(lit.read());
	      }
	    }
	    return (literaturesNew);
	  }


	  /**
	   * Gets all keywords of this DBSpectrum separated by ,.
	   *
	   * @return                The keywords value.
	   * @exception  Exception  Database problems.
	   */
	  public String getKeywords() throws TorqueException {
	    return (getKeywords(false));
	  }


	  /**
	   * Gets all keywords of this DBSpectrum separated by , or newline.
	   *
	   * @param  separatedByNewline  If true keywords are separated by \n else by ,.
	   * @return                     The keywords value.
	   * @exception  Exception       Database problems.
	   */
	  public String getKeywords(boolean separatedByNewline) throws TorqueException {
	    StringBuffer keywords = new StringBuffer();
	    getDBKeywords();
	    for (int i = 0; i < keywordsVector.size(); i++) {
	      if (separatedByNewline) {
	        keywords.append(((DBSpectrumDBKeyword) keywordsVector.get(i)).getDBKeyword().getKeyword() + "\n");
	      } else {
	        keywords.append(((DBSpectrumDBKeyword) keywordsVector.get(i)).getDBKeyword().getKeyword() + ";");
	      }
	    }
	    return (GeneralUtils.removeLastComma(keywords).toString());
	  }


	  /**
	   *  This makes a submittingData object populated with the values from this spectrum and its molecule.
	   *
	   * @param  useUser        If true, owner of this spectrum is set as submitter in submittingData.
	   * @param  path           Path to the normalizer file.
	   * @param  no3d           Generate 2d coordinates from 3d if true
	   * @param  sendEmailUserId The user to which the email about review assignment is sent (usually the submitter of the spectrum).
	   * @return                The SubmittingData object.
	   * @exception  Exception  Database problems.
	   */
	  public SubmittingData getMeAsSubmittingData(boolean useUser, String path, boolean no3d, String sendEmailUserId) throws Exception {
	    DBMolecule mol = this.getDBMolecule();
	    DBSpectrum spectrum = this;
	    SubmittingData subData = new SubmittingData(path);
	    subData.setChoosenSpectrumType(spectrum.getDBSpectrumType());
	    mol.addValuesToSubmittingData(subData, this, no3d, true);
	    if (useUser) {
	      subData.setThisUserId(spectrum.getNmrshiftdbUser().getUserId());
	    }
	    subData.setThisDate((java.util.Date) spectrum.getDate());
	    subData.setSmilesNonChiralProv(new String(mol.getSmilesString()));
	    subData.setIsChiralProv(mol.getSmilesStringChiral()!=null);
	    if(mol.getSmilesStringChiral()!=null)
	       subData.setSmilesChiralProv(new String(mol.getSmilesStringChiral()));
	    List<DBSignal> signals = getDBSignals();
	    for (int i = 0; i < signals.size(); i++) {
	      CMLPeak peak = new CMLPeak();
	      if(this.getDBSpectrumType().getDimensionality()==1){
		      peak.setXValue(Double.parseDouble(Float.toString(((DBSignal) signals.get(i)).getFirstShift().getValue())));
		      peak.setYValue(Double.parseDouble(Float.toString(((DBSignal) signals.get(i)).getIntensity())));
		      List<DBAtom> assignedAtoms = ((DBSignal) signals.get(i)).getAtoms();
		      for (int k = 0; k < assignedAtoms.size(); k++) {
		    	  peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(((DBAtom) assignedAtoms.get(k)).getMdlNumber())));
		      }
	      }else{
	    	  peak.setXValue(Double.parseDouble(Float.toString(((DBSignal) signals.get(i)).getDBShift(1).getValue())));
	    	  peak.setYValue(Double.parseDouble(Float.toString(((DBSignal) signals.get(i)).getDBShift(2).getValue())));
	    	  String[] atomrefs=new String[2];
	    	  List<DBSignalDBAtom> assignedAtoms = ((DBSignal) signals.get(i)).getDBSignalDBAtomsSortByAxis();
	    	  atomrefs[1]="a"+assignedAtoms.get(0).getDBAtom().getMdlNumberPlus1();
	    	  atomrefs[0]="a"+assignedAtoms.get(1).getDBAtom().getMdlNumberPlus1();
	    	  peak.setAtomRefs(atomrefs);
	      }
	      subData.getSignalstable().appendChild(peak);
	    }
	    signals = null;
	    subData.setLiterature(spectrum.getLiteraturesNew());
	    subData.setReviewFlag(spectrum.getReviewFlag());
	    List<DBConditionType> conditionsold = subData.getConditions();
	    for (int k = 0; k < conditionsold.size(); k++) {
	      List<DBCondition> conditions=getMeasurementConditions();
	      DBConditionType ct = conditionsold.get(k);
	      for(int l=0;l<conditions.size();l++){
	        if(((DBCondition)conditions.get(l)).getDBConditionType().getConditionName().equals(ct.getConditionName())){
	          ct.setCurrentValue(((DBCondition)conditions.get(l)).getValue(), this.getDBSpectrumType(), false);
	          break;
	        }
	      }
	    }
	    List<DBCoupling> couplings = this.getDBCouplings();
	    for(int i=0;i<couplings.size();i++){
	        DBCoupling coupling = (DBCoupling)couplings.get(i);
            Element cmlcoupling = new Element(SubmittingData.COUPLING,"http://www.nmrshiftdb.org/");
            cmlcoupling.addAttribute(new Attribute("otherAtom",coupling.getDBAtomRelatedByAtomId2().getMdlNumber()+""));
            cmlcoupling.addAttribute(new Attribute("value",Double.toString(coupling.getConstant())));
            subData.getCMLMolecule().getAtoms().get(coupling.getDBAtomRelatedByAtomId1().getMdlNumber()).appendChild(cmlcoupling);
	    }
	    subData.setConditions(conditionsold);
	    subData.setSpectrumLinks(spectrum.getLinksAsString());
	    List<Record> l = DBSpectrumPeer.executeQuery("select distinct IS_CONFIGURATION_SPECIFIED, BOND.BOND_ID from BOND, BOND_ATOM, ATOM where BOND.BOND_ID=BOND_ATOM.BOND_ID and BOND_ATOM.ATOM_ID=ATOM.ATOM_ID and MOLECULE_ID=" + mol.getMoleculeId() + " order by BOND_ID");
	    l = DBSpectrumPeer.executeQuery("select ATOM.ATOM_ID, IDENTIFIER from ATOM left join IDENTIFIER on (ATOM.ATOM_ID=IDENTIFIER.ATOM_ID) where SPECTRUM_ID="+this.getSpectrumId()+" order by ATOM_ID");
	    for (int k = 0; k < l.size(); k++) {
	      if (((Record) l.get(k)).getValue(2).asString()!=null && !((Record) l.get(k)).getValue(2).asString().equals("")) {
	    	  subData.addIdentifier(DBAtomPeer.retrieveByPK(((Record) l.get(k)).getValue(1).asInt()).getMdlNumber(), ((Record) l.get(k)).getValue(2).asString());
	      }
	    }
	    l = null;
	    subData.setSpectrumKeywords(spectrum.getKeywords(true));
	    subData.setAutonomeName(mol.getAutonomeName());
	    subData.setSpectrumid(this.getSpectrumId());
	    subData.setMoleculeid(this.getDBMolecule().getMoleculeId());
	    subData.setSendEmailUserId(sendEmailUserId);
	    List<DBCanonicalName> v = mol.getDBCanonicalNames();
	    for (int i = 0; i < v.size(); i++) {
	      if (((DBCanonicalName) v.get(i)).getCanonicalNameTypeId() == 1) {
	        subData.setAutonomeName(((DBCanonicalName) v.get(i)).getName());
	        break;
	      }
	    }
	   	subData.setComment(spectrum.getCommentAsString());
	   	if(getJcamp()!=null)
	   		subData.setJcamp(new String(this.getJcamp()));
	   	if(getPdf()!=null)
	   		subData.setPdf(getPdfDecoded());
        if(getRawData()!=null)
            subData.setRawData(getRawDataDecoded(),getRawDataFormat());
        if(getImage()!=null)
        	subData.setImage( this.getImageDecoded(), this.getImageFormat());
        if(getCmcseFile()!=null)
        	subData.setCmcseFile(this.getCmcseFilesDecoded());
	    return (subData);
	  }


    public byte[] getImage() throws TorqueException {
        return getDataFromSpectrumFile(IMAGE_TYPE);
    }
    
    public String getImageFormat() throws TorqueException {
        return getFormatFromSpectrumFile(IMAGE_TYPE);
    }

    public String getRawDataFormat() throws TorqueException {
        return getFormatFromSpectrumFile(RAW_DATE_TYPE);
    }
    
    public byte[] getPdf() throws TorqueException {
        return getDataFromSpectrumFile(PDF_TYPE);
    }
    
    public byte[] getOtherFiles() throws TorqueException {
        return getDataFromSpectrumFile(OTHERFILES_TYPE);
    }
    
    public byte[] getCmcseFile() throws TorqueException {
        return getDataFromSpectrumFile(CMCSE_TYPE);
    }

    public byte[] getRawData() throws TorqueException {
        return getDataFromSpectrumFile(RAW_DATE_TYPE);
    }
    

    public byte[] getJcamp() throws TorqueException {
        return getDataFromSpectrumFile(JCAMP_TYPE);
    }
    
    public byte[] getReportUrl() throws TorqueException {
        return getDataFromSpectrumFile(CSEARCH_TYPE);
    }
    
    public byte[] getReportNmrshiftdb() throws TorqueException {
        return getDataFromSpectrumFile(NMRSHIFTDB_TYPE);
    }
    
    public byte[] getDataFromSpectrumFile(String type) throws TorqueException {
        Criteria crit=new Criteria();
        crit.add(DBSpectrumFilePeer.SPECTRUM_ID, this.getSpectrumId());
        crit.add(DBSpectrumFilePeer.TYPE, type);
        List<DBSpectrumFile> result = DBSpectrumFilePeer.doSelect(crit);
        if(result.size()==0)
            return null;
        else
            return ((DBSpectrumFile)result.get(0)).getData();
    }
    
    public String getFormatFromSpectrumFile(String type) throws TorqueException {
        Criteria crit=new Criteria();
        crit.add(DBSpectrumFilePeer.SPECTRUM_ID, this.getSpectrumId());
        crit.add(DBSpectrumFilePeer.TYPE, type);
        List<DBSpectrumFile> result = DBSpectrumFilePeer.doSelect(crit);
        if(result.size()==0)
            return null;
        else
            return ((DBSpectrumFile)result.get(0)).getFormat();
    }   
    
    
    public String getUrlForSpectrumFile(String type, String baseUrl) throws TorqueException {
    	return baseUrl+"download/NmrshiftdbServlet/"+this.getSpectrumId()+"_"+this.getDBSpectrumType().getName()+".zip?spectrumid="+this.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=rawdata";
    }

    
    /**
     * Gets the Pdf attribute decoded from base64.
     * 
     * @return
     * @throws TorqueException 
     */
    public byte[] getPdfDecoded() throws TorqueException {
		return new Base64().decode(getPdf());
	}
    
    /**
     * Gets the RawData attribute decoded from base64.
     * 
     * @return
     * @throws TorqueException 
     */
    public byte[] getRawDataDecoded() throws TorqueException {
        return new Base64().decode(getRawData());
    }

    /**
     * Gets the CMC-SE file attribute decoded from base64.
     * 
     * @return
     * @throws TorqueException
     */
	public byte[] getCmcseFilesDecoded() throws TorqueException {
        return new Base64().decode(getCmcseFile());
	}

	/**
     * Gets the OtherFiles attribute decoded from base64.
     * 
     * @return
     * @throws TorqueException 
     */
    public byte[] getOtherFilesDecoded() throws TorqueException {
        return new Base64().decode(getOtherFiles());
    }
    

    /**
     * Gets the image attribute decoded from base64.
     * 
     * @return
     * @throws TorqueException 
     */
    public byte[] getImageDecoded() throws TorqueException {
        return new Base64().decode(getImage());
    }

    
    
    /**
	   *  Gets the mark of the DBSpectrum object
	   *
	   * @return                The mark value
	   * @throws TorqueException 
	   * @throws DataSetException 
	   * @throws NumberFormatException 
	   * @exception  Exception  Description of Exception
	   */
	  public int getMark() throws TorqueException, NumberFormatException, DataSetException  {
	    List<Record> l = DBSpectrumPeer.executeQuery("select avg(MARK) from USER_SPECTRUM_MARK where SPECTRUM_ID =" + getSpectrumId());
	    if (((Record) l.get(0)).getValue(1).asString() == null) {
	      return (10);
	    } else {
	      return (int)Float.parseFloat(((Record) l.get(0)).getValue(1).asString());
	    }
	  }


	  /**
	   *  Gets the Comments for marks of the DBSpectrum object.
	   *
	   * @return                The comment value.
	   * @exception  Exception  Database problems.
	   */
	  public String getMarkComment() throws Exception {
	    List<Record> l = DBSpectrumPeer.executeQuery("select COMMENT, MARK, USER_ID from USER_SPECTRUM_MARK where SPECTRUM_ID =" + getSpectrumId());
	    if (l.size() == 0) {
	      return ("");
	    } else {
	      StringBuffer comment = new StringBuffer();
	      for (int i = 0; i < l.size(); i++) {
	        comment.append(((Record) l.get(i)).getValue(2).asString() + ": " + ((Record) l.get(i)).getValue(1).asString() + " (" + NmrshiftdbUserPeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(3).asString())).getLoginName() + "), ");
	      }
	      return ("(" + GeneralUtils.removeLastComma(comment) + ")");
	    }
	  }


	  /**
	   *  Returns the mark necessary for a spectrum to be displayed
	   *
	   * @return                            The displaymark value
	   * @exception  FileNotFoundException  Description of Exception
	   * @exception  IOException            Description of Exception
	   */
	  public int getDisplaymark() throws FileNotFoundException, IOException {
		  if(GeneralUtils.getNmrshiftdbProperty("minimum.mark.display")==null)
			  return 0;
		  return (new Integer(GeneralUtils.getNmrshiftdbProperty("minimum.mark.display")).intValue());
	  }
	  
	  
	  /**
	   *  Gets this spectrum as cmlSpect.
	   *
	   * @param      withMol       Should the molecule be included?
	   * @return                   The cmlSpect value.
	   * @throws TorqueException   Something wrong with database reading. 
	   * @throws DataSetException 
	   * @throws IOException
	   * @throws NmrshiftdbException 
	   */
	  public CMLCml getCmlSpect(boolean withMol, String baseUrl) throws TorqueException, IOException, DataSetException, NmrshiftdbException {
	    CMLCml cml=new CMLCml();
	    CMLSpectrum spectrum = new CMLSpectrum();
	    spectrum.addNamespaceDeclaration("siUnits","http://www.xml-cml.org/units/siUnits");
	    spectrum.addNamespaceDeclaration("units","http://www.xml-cml.org/units/units");
	    spectrum.addNamespaceDeclaration("nmr","http://www.nmrshiftdb.org/dict");
	    spectrum.addNamespaceDeclaration("cmlDict","http://www.xml-cml.org/dict/cmlDict");
	    spectrum.addNamespaceDeclaration("cml","http://www.xml-cml.org/dict/cml");
	    spectrum.addNamespaceDeclaration("macie","http://www.xml-cml.org/dict/macie");
	    spectrum.addNamespaceDeclaration("subst","http://www.xml-cml.org/dict/substDict");
	    CMLPeakList peaklist = new CMLPeakList();
	    CMLConditionList cl=new CMLConditionList();
	    spectrum.addConditionList(cl);
	    CMLMetadataList ml=new CMLMetadataList();
	    spectrum.addMetadataList(ml);
	    CMLSubstanceList sl=new CMLSubstanceList();
	    spectrum.appendChild(sl);
	    List<DBSpectrumDBCondition> v=this.getDBSpectrumDBConditions();
		for(int i=0;i<v.size();i++){
			  DBCondition condition=((DBSpectrumDBCondition)v.get(i)).getDBCondition();
			  DBConditionType condtype=condition.getDBConditionType();
			  if(condtype.getCmlEntryType().equals(NmrshiftdbConstants.CONDITION)){
				  CMLScalar scalar=new CMLScalar(condition.getValue());
				  scalar.setDictRef(condition.getDBConditionType().getDictRef());
				  if(!condition.getDBConditionType().getUnits().equals("none"))
					  scalar.setUnits(condition.getDBConditionType().getUnits());
				  cl.addScalar(scalar);
			  }
			  if(condtype.getCmlEntryType().equals(NmrshiftdbConstants.METADATA)){
				  CMLMetadata metadata=new CMLMetadata();
				  metadata.setName(condtype.getDictRef());
				  metadata.setContent(condition.getValue());
				  ml.addMetadata(metadata);
			  }
			  if(condtype.getCmlEntryType().equals(NmrshiftdbConstants.SUBSTANCE)){
				  CMLSubstance substance = new CMLSubstance();
				  substance.setDictRef(condtype.getDictRef());
				  substance.setRole(condtype.getUnits());
				  substance.setTitle(condition.getValue());
				  sl.addSubstance(substance);
			  }
		}
		CMLMetadata metadata=new CMLMetadata();
		metadata.setName("nmr:OBSERVENUCLEUS");
		metadata.setContent(this.getDBSpectrumType().getName());
		ml.addMetadata(metadata);
	    spectrum.addPeakList(peaklist);
	    DBSpectrum dbspectrum = this;
	    spectrum.setId("nmrshiftdb"+dbspectrum.getSpectrumId());
	    if(dbspectrum.getMoleculeId()!=-1)
	        spectrum.setMoleculeRef("nmrshiftdb"+dbspectrum.getDBMolecule().getMoleculeId());
	    spectrum.setType("NMR");
	    if(baseUrl!=null && this.getRawData()!=null) {
	    	Attribute att=new Attribute("rawdata", this.getUrlForSpectrumFile(RAW_DATE_TYPE,baseUrl));
	    	spectrum.addAttribute(att);
	    }
	    List<DBSignal> signals = dbspectrum.getDBSignals();
	    for (int i = 0; i < signals.size(); i++) {
	      CMLPeak peak = new CMLPeak();
	      try{
	    	DBSignal signal=((DBSignal) signals.get(i));
	        peak.setXValue(Double.parseDouble(Float.toString(signal.getFirstShift().getValue())));
	        peak.setXUnits("units:ppm");
	        peak.setPeakShape("sharp");
	        if (((DBSignal) signals.get(i)).getIntensity() != 0) {
	          peak.setPeakHeight(Double.parseDouble(Float.toString(((DBSignal) signals.get(i)).getIntensity())));
	        }
	        if(signal.getMultiplicity()!=null && !signal.getMultiplicity().equals("")){
	        	peak.setPeakMultiplicity(signal.getMultiplicity());
	        }
	        peak.setId("p" + i);
	        List<DBAtom> atoms = signal.getAtoms();
	        String[] atomrefs=new String[atoms.size()];
	        for (int k = 0; k < atoms.size(); k++) {
	          CMLAtom atom = new CMLAtom();
	          if(((DBAtom) atoms.get(k)).getMoleculeId()!=-1)
	              atom.setId("a" + ((DBAtom) atoms.get(k)).getMdlNumberPlus1());
	          atomrefs[k]=atom.getId();
	          List<Record> l=DBCouplingPeer.executeQuery("SELECT ATOM_ID_1, ATOM_ID_2, CONSTANT FROM COUPLING WHERE (COUPLING.SPECTRUM_ID="+this.getSpectrumId()+") AND ((COUPLING.ATOM_ID_2="+((DBAtom) atoms.get(k)).getAtomId()+") OR (COUPLING.ATOM_ID_1="+((DBAtom) atoms.get(k)).getAtomId()+"))");
	          for(int m=0;m<l.size();m++){
	              CMLPeakStructure peakStructure=new CMLPeakStructure();
	              peakStructure.setType("coupling");
	              peakStructure.setAtomRefs(new String[]{((Record) l.get(m)).getValue(2).asInt()==((DBAtom) atoms.get(k)).getAtomId() ? "a"+DBAtomPeer.retrieveByPK(new NumberKey(((Record) l.get(m)).getValue(1).asInt())).getMdlNumberPlus1() : "a"+DBAtomPeer.retrieveByPK(new NumberKey(((Record) l.get(m)).getValue(2).asInt())).getMdlNumberPlus1()});
	        	  peakStructure.setUnits("unit:hertz");
	        	  peakStructure.setCMLValue(((Record) l.get(m)).getValue(3).asFloat()+"");
	              peak.addPeakStructure(peakStructure);
	          }
	          peak.setAtomRefs(atomrefs);
	        }
	        peaklist.addPeak(peak);
	      }catch(Exception ex){
	        GeneralUtils.logError(ex,"Problem reading spectrum "+getSpectrumId()+" , signal "+ ((DBSignal) signals.get(i)).getSignalId(),runData,true);
	      }
	    }
		for(int m=0;m<this.getLiteraturesNew().size();m++){
			spectrum.appendChild(this.getLiteraturesNew().get(m).getAsBibtex());
		}
	    cml.appendChild(spectrum);    
	    if(withMol)
	    	cml.appendChild(((CMLElement)this.getDBMolecule().getCML(1)).getChildCMLElements("molecule").get(0));
	    return (cml);
	  }


	  /**
	   *  Returns a text telling unsed spectra. Initialized by initSimilarity().
	   *
	   * @return    The unusedText value.
	   */
	  public String getUnusedText() {
	    return unusedtext.toString();
	  }


	  /**
	   *  Tells if any signal in this spectrum has an intensity != 0.
	   *
	   * @return                True=yes, false=no.
	   * @exception  Exception  Database problems.
	   */
	  public boolean hasAnyIntensities() throws Exception {
	    if (intensities == -1) {
	      intensities = 0;
	      List<DBSignal> v = getDBSignals();
	      for (int i = 0; i < v.size(); i++) {
	        if (((DBSignal) v.get(i)).getIntensity() != 0) {
	          intensities = 1;
	          break;
	        }
	      }
	    }
	    if (intensities == 1) {
	      return (true);
	    } else {
	      return (false);
	    }
	  }


	  /**
	   *Sets the similarity between this and another spectrum
	   *
	   * @param  isSubSpectrum  Is this a subspectrum search ?
	   * @param  otherSignalsL  Description of Parameter
	   * @return                Description of the Returned Value
	   * @throws TorqueException 
	   */
	  public Map<Double,Double> initSimilarity(List<CMLPeak> otherSignalsL, boolean isSubSpectrum) throws TorqueException{
		float similarityPerSignal;
		float similarity = 0;
	    int greaterNumber;
	    int smallerNumber;
	    List<CMLPeak> smallerArray;
	    List<CMLPeak> greaterArray;
	    List<CMLPeak> signalsL = new ArrayList<CMLPeak>();
	    List<DBSignal> peaks = this.getDBSignals();
	    for(int i=0;i<peaks.size();i++){
	    	CMLPeak peak = new CMLPeak();
	    	peak.setXValue(((DBSignal)peaks.get(i)).getFirstShift().getValue());
	    	peak.setYValue(((DBSignal)peaks.get(i)).getIntensity());
	    	peak.setPeakMultiplicity(((DBSignal)peaks.get(i)).getMultiplicity());
	    	signalsL.add(peak);
	    }
	    //If it's a subspectrum search, the search spectrum is always taken as basis for search
	    //The similarity per signal is calculated from that spectrum
	    //If there is a signal in the spectrum in the DB for every signal in the search spectrum, similarity will be 100 %
	    boolean smallervectorissearchspectrum = true;
	    if (isSubSpectrum) {
	      smallerNumber = otherSignalsL.size();
	      greaterNumber = signalsL.size();
	      smallerArray = otherSignalsL;
	      greaterArray = signalsL;
	      similarityPerSignal = 100f / smallerNumber;
	    } else {
	      //The variables smallerArraya/smallerNubmer and greaterArray/greaterNumber are set so that calculation is independent of which spectrum having more values
	      //For 100% similarity the spectra need to have the same number of signals
	      if (signalsL.size() > otherSignalsL.size()) {
	        greaterNumber = signalsL.size();
	        smallerNumber = otherSignalsL.size();
	        smallerArray = otherSignalsL;
	        greaterArray = signalsL;
	      } else {
	        greaterNumber = otherSignalsL.size();
	        smallerNumber = signalsL.size();
	        smallerArray = signalsL;
	        greaterArray = otherSignalsL;
	        smallervectorissearchspectrum = false;
	      }
	      //From the 100% maximum similarity, one signal can have 100/greater number of signals at best
	      similarityPerSignal = 100f / greaterNumber;
	    }
	    Vector<Double> unused = new Vector<Double>();
	    Map<Double,Double> map = GeneralUtils.getMatchingSearchShift(greaterArray, smallerArray, similarityPerSignal, unused, smallervectorissearchspectrum);
	    similarity=SpectrumUtils.calculateSimilarity(map, similarityPerSignal, greaterArray, unused);
	    Set<Double> set = map.keySet();
	    Iterator<Double> iterator = set.iterator();
	    searchSignalMap = new HashMap<Double,Double>();
	    while (iterator.hasNext()) {
	      Double element = iterator.next();
	      double difference = java.lang.Math.abs(element - map.get(element));
	      if (difference < similarityPerSignal) {
	        if (greaterArray == otherSignalsL) {
	          searchSignalMap.put(map.get(element), element);
	        } else {
	          searchSignalMap.put(element, map.get(element));
	        }
	      }
	    }
	    unusedtext = new StringBuffer("");
	    for (int i = 0; i < otherSignalsL.size(); i++) {
	      if(!searchSignalMap.containsValue(new Double(otherSignalsL.get(i).getXValue())))
	        unusedtext.append(otherSignalsL.get(i).getXValue() + "; ");
	    }
	    unusedtext = GeneralUtils.removeLastComma(unusedtext);
	    if(!unusedtext.toString().trim().equals(""))
	      unusedtext=new StringBuffer("The following signals in your search spectrum were not used for similarity calculation: "+unusedtext.toString());
	    unusedtext.append(" Threshold is "+ format.format(similarityPerSignal));
	    this.similarity = similarity;
	    return (searchSignalMap);
	  }


	  /**
	   * Tells if there is any atom identifier for this spectrum.
	   *
	   * @return                True=yes, false=no.
	   * @exception  TorqueException  Database problems.
	   */
	  public boolean hasAnyIdentifier() throws TorqueException {
	    DBAtom[] atoms = DBMolecule.getAtomsAsArrayInMdlOrder(getMoleculeId(), null);
	    for (int i = 0; i < atoms.length; i++) {
	      if (!atoms[i].getIdentifier(this).equals("")) {
	        return (true);
	      }
	    }
	    return (false);
	  }
	  
	  public boolean hasValue(float clow,float chigh, float hlow, float hhigh) throws TorqueException, NmrshiftdbException{
		  for(int i=0;i<this.getDBSignals().size();i++){
			  if(((DBSignal)this.getDBSignals().get(i)).getDBShift(1).getValue()>=clow && ((DBSignal)this.getDBSignals().get(i)).getDBShift(1).getValue()<=chigh && ((DBSignal)this.getDBSignals().get(i)).getDBShift(2).getValue()>=hlow && ((DBSignal)this.getDBSignals().get(i)).getDBShift(2).getValue()<=hhigh){
				  return true;
			  }
		  }
		  return false;
	  }


	  /**
	   *  Adds a mark to this DBSpectrum.
	   *
	   * @param  mark           The mark.
	   * @param  user           The user marking.
	   * @param  comment        The feature to be added to the Mark attribute
	   * @exception  Exception  Database problems.
	   */
	  public void addMark(int mark, User user, String comment, ServletConfig servcon) throws Exception {
	    Criteria crit = new Criteria();
	    crit.add(DBUserDBSpectrumMarkPeer.USER_ID, ((BaseJetspeedUser) user).getUserId());
	    crit.add(DBUserDBSpectrumMarkPeer.SPECTRUM_ID, getSpectrumId());
	    List<DBUserDBSpectrumMark> v = DBUserDBSpectrumMarkPeer.doSelect(crit);
	    if (v.size() > 0) {
	      throw new NmrshiftdbException("The user " + user.getUserName() + " has already marked this spectrum!");
	    }
	    DBUserDBSpectrumMark usermark = new DBUserDBSpectrumMark();
	    usermark.setMark(mark);
	    usermark.setDBSpectrum(this);
	    usermark.setNmrshiftdbUserKey(new NumberKey(((BaseJetspeedUser) user).getUserId()));
	    usermark.setMarkDate(new java.util.Date());
	    usermark.setComment(comment);
	    usermark.save();
		DBMolecule.deleteCacheFiles(this.getMoleculeId(), servcon, "");
	    /*crit = new Criteria();
	    crit.add(DBUserDBSpectrumMarkPeer.SPECTRUM_ID, getSpectrumId());
	    v = DBUserDBSpectrumMarkPeer.doSelect(crit);
	    if (v.size() == 1) {
	      addMark(10, NmrshiftdbUserPeer.getByName("admin"), "Default mark");
	    }*/
	    if (getMark() < new Integer(GeneralUtils.getNmrshiftdbProperty("minimum.mark.prediction")).intValue()) {
	      GeneralUtils.sendEmailToEventReceivers("Mark below threshold for prediction", "The mark of spectrum " + getSpectrumId() + " has fallen below the threshold.", runData, 0);
	    }
	  }


	  /**
	   *  Deletes this spectrum plus all associated rows.
	   *
	   * @param  dbconn         The connection to use.
	   * @exception  Exception  Database problems.
	   */
	  public void delete(Connection dbconn, boolean doMolecule) throws Exception {
	    Statement stmt = dbconn.createStatement();
	    Vector<DBMolecule> mols = new Vector<DBMolecule>();
	    //find it's signals
	    Criteria critSignals = new Criteria();
	    String sqlSignals = "DELETE FROM NMR_SIGNAL WHERE SPECTRUM_ID=" + getSpectrumId() + ";";
	    critSignals.add(DBSignalPeer.SPECTRUM_ID, getSpectrumId());
	    List<DBSignal> signals = DBSignalPeer.doSelect(critSignals);
	    // The signal-atom relations of these signals have to go
	    String sqlSigAtoms = "DELETE FROM SIGNAL_ATOM WHERE ";
	    for (int k = 0; k < signals.size(); k++) {
	      sqlSigAtoms += "SIGNAL_ID=" + ((DBSignal) signals.get(k)).getSignalId() + " OR ";
	    }
	    if (signals.size() > 0) {
	      sqlSigAtoms += "1>2;";
	      stmt.executeUpdate(sqlSigAtoms);
	    }
	    // and their shifts ((doDelete(crit) does not work if crit contains criterions, it deletes everything, therefore the sql)
	    if (signals.size() > 0) {
	      for (int k = 0; k < signals.size(); k++) {
	        String sqlShifts = "DELETE FROM SHIFT WHERE SIGNAL_ID=" + ((DBSignal) signals.get(k)).getSignalId() + ";";
	        stmt.executeUpdate(sqlShifts);
		    String sql="delete from DESCRIPTORS_1 where SIGNAL_ID="+((DBSignal) signals.get(k)).getSignalId();
		    DBSignalPeer.executeStatement(sql);
		    sql="delete from DESCRIPTORS_2 where SIGNAL_ID="+((DBSignal) signals.get(k)).getSignalId();
		    DBSignalPeer.executeStatement(sql);
		    sql="delete from DESCRIPTORS_3 where SIGNAL_ID="+((DBSignal) signals.get(k)).getSignalId();
		    DBSignalPeer.executeStatement(sql);
		    sql="delete from DESCRIPTORS_4 where SIGNAL_ID="+((DBSignal) signals.get(k)).getSignalId();
		    DBSignalPeer.executeStatement(sql);
		    sql="delete from DESCRIPTORS_ALL where SIGNAL_ID="+((DBSignal) signals.get(k)).getSignalId();
		    DBSignalPeer.executeStatement(sql);
	      }
	      stmt.executeUpdate(sqlSignals);
	    }
	    //Look if there are any molecules for these spectra who have no other spectra
	    Criteria crit = new Criteria();
	    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
	    crit.add(DBMoleculePeer.MOLECULE_ID, getMoleculeId());
	    crit.add(DBSpectrumPeer.SPECTRUM_ID, (Object) getSpectrumId(), Criteria.NOT_EQUAL);
	    crit.add(DBSpectrumPeer.REVIEW_FLAG, (Object) "rejected", Criteria.NOT_EQUAL);
	    List<DBMolecule> v = DBMoleculePeer.doSelect(crit);
	    if (v.size() == 0 && getDBMolecule().getDBWishlists().size()==0 && getDBMolecule().getDBSamples().size()==0) {
	      mols.add(getDBMolecule());
	    }
	    //Delete the identifiers
	    Criteria critIds = new Criteria();
	    critIds.add(DBIdentifierPeer.SPECTRUM_ID, getSpectrumId());
	    DBIdentifierPeer.doDelete(critIds, dbconn);
	    //Delete the conditions
	    Criteria critConds = new Criteria();
	    critConds.add(DBSpectrumDBConditionPeer.SPECTRUM_ID, getSpectrumId());
	    DBSpectrumDBConditionPeer.doDelete(critConds, dbconn);
	    //Delete the review assignments
	    Criteria critAssign = new Criteria();
	    critAssign.add(DBUserDBSpectrumPeer.SPECTRUM_ID, getSpectrumId());
	    DBUserDBSpectrumPeer.doDelete(critAssign, dbconn);
	    critAssign = new Criteria();
	    critAssign.add(DBUserDBSpectrumEditPeer.SPECTRUM_ID_OLD, getSpectrumId());
	    DBUserDBSpectrumEditPeer.doDelete(critAssign, dbconn);
	    try {
	      critAssign = new Criteria();
	      critAssign.add(DBUserDBSpectrumMarkPeer.SPECTRUM_ID, getSpectrumId());
	      DBUserDBSpectrumMarkPeer.doDelete(critAssign, dbconn);
	    } catch (TorqueException ex) {
	      //Since user_spectrum_mark does not exist in standalone client, we do nothing if this fails.
	    }
	    //Delete spectrum_literatures
	    critAssign = new Criteria();
	    critAssign.add(DBSpectrumDBLiteraturePeer.SPECTRUM_ID, getSpectrumId());
	    DBSpectrumDBLiteraturePeer.doDelete(critAssign, dbconn);
	    critAssign = new Criteria();
	    critAssign.add(DBSpectrumHyperlinkPeer.SPECTRUM_ID, getSpectrumId());
	    DBSpectrumHyperlinkPeer.doDelete(critAssign, dbconn);
	    critAssign = new Criteria();
	    critAssign.add(DBSpectrumDBKeywordPeer.SPECTRUM_ID, getSpectrumId());
	    DBSpectrumDBKeywordPeer.doDelete(critAssign, dbconn);
	    stmt.executeUpdate("DELETE from COUPLING where SPECTRUM_ID=" + getSpectrumId());
	    critAssign = new Criteria();
	    critAssign.add(DBSpectrumFilePeer.SPECTRUM_ID, getSpectrumId());
	    DBSpectrumFilePeer.doDelete(critAssign, dbconn);
	    //And the spectrum finally
	    critAssign = new Criteria();
	    crit.add(DBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
	    stmt.executeUpdate("DELETE from SPECTRUM where SPECTRUM_ID=" + getSpectrumId());
	    //Care about literatures
	    String sql = "select LITERATURE.LITERATURE_ID from LITERATURE left join SPECTRUM_LITERATURE on LITERATURE.LITERATURE_ID =SPECTRUM_LITERATURE.LITERATURE_ID where SPECTRUM_LITERATURE.SPECTRUM_ID is null;";
	    List<Record> l = DBLiteraturePeer.executeQuery(sql);
	    for (int i = 0; i < l.size(); i++) {
	      int literatureId = ((Record) l.get(i)).getValue(1).asInt();
	      stmt.executeUpdate("delete from IS_AUTHOR where LITERATURE_ID=" + literatureId);
	      stmt.executeUpdate("delete from IS_EDITOR where LITERATURE_ID=" + literatureId);
	      stmt.executeUpdate("delete from BOOK where LITERATURE_ID=" + literatureId);
	      stmt.executeUpdate("delete from BOOK_PUBLISHER where LITERATURE_ID=" + literatureId);
	      stmt.executeUpdate("delete from ARTICLE where LITERATURE_ID=" + literatureId);
	      stmt.executeUpdate("delete from LITERATURE where LITERATURE_ID=" + literatureId);
	    }
	      sql = "select AUTHOR.AUTHOR_ID from AUTHOR left join IS_AUTHOR on AUTHOR.AUTHOR_ID =IS_AUTHOR.AUTHOR_ID where IS_AUTHOR.LITERATURE_ID is null and AUTHOR.AUTHOR_ID in (select AUTHOR.AUTHOR_ID from AUTHOR left join IS_EDITOR on AUTHOR.AUTHOR_ID =IS_EDITOR.AUTHOR_ID where IS_EDITOR.LITERATURE_ID is null);";
	      l = DBAuthorPeer.executeQuery(sql);
	      for (int i = 0; i < l.size(); i++) {
	        int authorId = ((Record) l.get(i)).getValue(1).asInt();
	        stmt.executeUpdate("delete from AUTHOR where AUTHOR_ID=" + authorId);
	      }
	      stmt.executeUpdate("delete PUBLISHER from PUBLISHER left join BOOK_PUBLISHER using (PUBLISHER_ID) where BOOK_PUBLISHER.LITERATURE_ID is null;");
	      stmt.executeUpdate("delete JOURNAL_VOLUME from JOURNAL_VOLUME left join ARTICLE on JOURNAL_VOLUME.JOURNAL_VOLUME_ID=ARTICLE.PUBLISHED_IN_JOURNAL where ARTICLE.LITERATURE_ID is null;");
	      stmt.executeUpdate("delete JOURNAL from JOURNAL left join JOURNAL_VOLUME using (JOURNAL_ID) where JOURNAL_VOLUME.JOURNAL_VOLUME_ID is null;");
	    //Without this commit/beginn, we get into a strange sort of endless recursion
	    try {
	      stmt.executeUpdate("COMMIT");
	      stmt.executeUpdate("BEGIN");
	    } catch (SQLException ex) {
	      //Since no trancations in hsql, we do nothing if this fails.
	    }
	    stmt.close();
	    if(doMolecule){
		    for (int i = 0; i < mols.size(); i++) {
		      mols.get(i).delete(dbconn);
		    }
	    }
	  }


	  /**
	   *  Gets the shifts of a certain atom in this spectrum.
	   *
	   * @param  atomNumber     The mdl number of the atom to look for.
	   * @return                The shifts value.
	   * @exception  Exception  Database problems.
	   */
	  private float[] getShifts(int atomNumber, DBMolecule mol) throws TorqueException {
	    DBAtom[] atoms = mol.getAtomsAsArrayInMdlOrder();
	    int atomId = atoms[atomNumber].getAtomId();
	    Criteria crit = new Criteria();
	    crit.add(DBAtomPeer.ATOM_ID, atomId);
	    crit.addJoin(DBAtomPeer.ATOM_ID, DBSignalDBAtomPeer.ATOM_ID);
	    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
	    crit.addJoin(DBSignalPeer.SPECTRUM_ID, DBSpectrumPeer.SPECTRUM_ID);
	    crit.add(DBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
	    crit.addJoin(DBSignalPeer.SIGNAL_ID, DBShiftPeer.SIGNAL_ID);
	    List<DBShift> v = DBShiftPeer.doSelect(crit);
	    float[] shifts = new float[v.size()];
	    for (int i = 0; i < v.size(); i++) {
	      shifts[i] = ((DBShift) v.get(i)).getValue();
	    }
	    return (shifts);
	  }
	  
	  
	  /**
	   *  Gets the shifts of a certain atom in this spectrum.
	   *
	   * @param  atomNumber     The mdl number of the atom to look for.
	   * @return                The shifts value.
	   * @throws TorqueException Database problems.
	   */
	  public List<DBSignal> getSignals(int atomNumber, DBMolecule mol) throws TorqueException  {
	    DBAtom[] atoms = mol.getAtomsAsArrayInMdlOrder();
	    int atomId = atoms[atomNumber].getAtomId();
	    Criteria crit = new Criteria();
	    crit.add(DBAtomPeer.ATOM_ID, atomId);
	    crit.addJoin(DBAtomPeer.ATOM_ID, DBSignalDBAtomPeer.ATOM_ID);
	    crit.addJoin(DBSignalDBAtomPeer.SIGNAL_ID, DBSignalPeer.SIGNAL_ID);
	    crit.addJoin(DBSignalPeer.SPECTRUM_ID, DBSpectrumPeer.SPECTRUM_ID);
	    crit.add(DBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
	    return DBSignalPeer.doSelect(crit);
	  }
	  
  
	  /**
	   *  Gets the spectrum in the format needed for the new spectrumapplet (R 1.0). Assignments are to heavy atoms in H spectra.
	   *
	   * @return                The shifts value.
	   * @throws DataSetException 
	   * @throws IOException 
	   * @throws NmrshiftdbException 
	   * @throws TorqueException 
	   * @throws OptionalDataException 
	   */
	  public String getSpectrumForAppletNewFormat() throws OptionalDataException, TorqueException, NmrshiftdbException, IOException, DataSetException{
		  return getSpectrumForAppletNewFormat(getDBMolecule().getAtomsAsArrayInMdlOrder(), getDBMolecule().getAsCDKMolecule(1));
	  }

	  
	  /**
	   *  Gets the spectrum in the format needed for the new spectrumapplet (R 1.0). Assignments are to heavy atoms in H spectra.
	   *
	   * @return                The shifts value.
	   * @throws TorqueException Database problems.
	   * @throws NmrshiftdbException See message.
	   */
	  public String getSpectrumForAppletNewFormat(DBAtom[] atoms, IAtomContainer cdkmol) throws NmrshiftdbException, TorqueException{
		  StringBuffer appletstring=new StringBuffer();
		  for(int i=0;i<atoms.length;i++){
			  List<DBSignal> signals=atoms[i].getDBSignals(this);
			  for(int l=0;l<signals.size();l++){
				  DBSignal signal=(DBSignal)signals.get(l);
				  List<DBAtom> localatoms=signal.getAtoms();
				  if(localatoms.size()>0){
					  for(int k=0;k<localatoms.size();k++){
						  appletstring.append("S("+signal.getIntensity()+","+((DBAtom)localatoms.get(k)).getIntegralStuff()+","+(((DBAtom)localatoms.get(k)).getSymbol().equals("H") ? ((DBAtom)((DBAtom)localatoms.get(k)).getConnectedAtoms().get(0)).getMdlNumberPlus1() : (k+1))+","+((DBSignal)signals.get(l)).getMultiplicity()+")="+((DBSignal)signals.get(l)).getFirstShift().getValue()+"|");
					  }
				  }else{
					  for(int k=0;k<signals.size();k++){
						  appletstring.append("S("+((DBSignal)signals.get(k)).getIntensity()+","+atoms[i].getIntegralStuff(atoms, cdkmol)+","+(atoms[i].getSymbol().equals("H") ? getMdlNumberPlus1(((DBAtom)atoms[i].getConnectedAtoms(cdkmol,atoms).get(0)),atoms) : (i+1))+","+((DBSignal)signals.get(k)).getMultiplicity()+")="+((DBSignal)signals.get(k)).getFirstShift().getValue()+"|");
					  }
				  }
			  }
		  }
		  if(appletstring.length()>0)
			  return(appletstring.substring(0,appletstring.length()-1));
		  else
	          return "";
	  }
	  
	  
	  public String getOtherSpectraStringHeader() throws Exception{
		  StringBuffer sb=new StringBuffer();
		  Criteria crit=new Criteria();
		  crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_ID);
		  crit.add(DBSpectrumPeer.REVIEW_FLAG,"true");
		  crit.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, this.getSpectrumTypeId());
		  crit.add(DBSpectrumPeer.SPECTRUM_ID,this.getSpectrumId(),Criteria.NOT_EQUAL);
		  crit.add(DBSpectrumPeer.MOLECULE_ID,this.getMoleculeId());
		  List<DBSpectrum> spectra=this.getDBMolecule().getDBSpectrums(crit);
		  for(int i=0;i<spectra.size();i++){
			  	  DBSpectrum spectrum=((DBSpectrum)spectra.get(i));
				  sb.append("<th bgcolor=\"#0000F8\"><div style=\"color:white\">"+(spectrum.getConditionType().equals("m") ? "expt-"+i : (spectrum.getConditionType().equals("c") ? "calc-"+i : "Prediction "+i)) +"<br>"+ spectrum.getKeywords()+"</div></th>");
		  }
		  return sb.toString();
	  }

	  
	  public String getOtherSpectraString(int atomNumber) throws Exception{
		  DBAtom assignedTo=this.getDBMolecule().getAtomAt(atomNumber);
		  StringBuffer sb=new StringBuffer();
		  Criteria crit=new Criteria();
		  crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_ID);
		  crit.add(DBSpectrumPeer.REVIEW_FLAG,"true");
		  crit.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, this.getSpectrumTypeId());
		  crit.add(DBSpectrumPeer.SPECTRUM_ID,this.getSpectrumId(),Criteria.NOT_EQUAL);
		  crit.add(DBSpectrumPeer.MOLECULE_ID,this.getMoleculeId());
		  List<DBSpectrum> spectra=this.getDBMolecule().getDBSpectrums(crit);
		  for(int i=0;i<spectra.size();i++){
			  DBSpectrum spectrum=(DBSpectrum)spectra.get(i);
			  if(this.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")){
				  Vector<DBAtom> connatoms=assignedTo.getConnectedAtoms();
				  int connhs=0;
				  for(int k=0;k<connatoms.size();k++){
					  if(connatoms.get(k).getSymbol().equals("H")){
						  connhs++;
					  }
				  }
				  if(connhs==2){
					  for(int k=0;k<connatoms.size();k++){
						  if(connatoms.get(k).getSymbol().equals("H")){
							  Criteria crit2=new Criteria();
							  crit2.addJoin(DBSignalDBAtomPeer.SIGNAL_ID,DBSignalPeer.SIGNAL_ID);
							  crit2.add(DBSignalDBAtomPeer.ATOM_ID,connatoms.get(k).getAtomId());
							  crit2.add(DBSignalPeer.SPECTRUM_ID,spectrum.getSpectrumId());
							  List<DBSignal> v=DBSignalPeer.doSelect(crit2);
							  if(v.size()>0){
								  DBSignal signal=(DBSignal)v.get(0);
								  sb.append("<td bgcolor=\"#E8E8E8\">"+signal.getFirstShift().getValue()+"</td>");
								  break;							  
							  }
						  }
					  }
				  }else{
					  for(int k=0;k<connatoms.size();k++){
						  if(connatoms.get(k).getSymbol().equals("H")){
							  Criteria crit2=new Criteria();
							  crit2.addJoin(DBSignalDBAtomPeer.SIGNAL_ID,DBSignalPeer.SIGNAL_ID);
							  crit2.add(DBSignalDBAtomPeer.ATOM_ID,connatoms.get(k).getAtomId());
							  crit2.add(DBSignalPeer.SPECTRUM_ID,spectrum.getSpectrumId());
							  List<DBSignal> v=DBSignalPeer.doSelect(crit2);
							  if(v.size()>0){
								  DBSignal signal=(DBSignal)v.get(0);
								  sb.append("<td bgcolor=\"#E8E8E8\">"+signal.getFirstShift().getValue()+"</td>");
								  break;
							  }
						  }
					  }
				  }
			  }else{
				  Criteria crit2=new Criteria();
				  crit2.addJoin(DBSignalDBAtomPeer.SIGNAL_ID,DBSignalPeer.SIGNAL_ID);
				  crit2.add(DBSignalDBAtomPeer.ATOM_ID,assignedTo.getAtomId());
				  crit2.add(DBSignalPeer.SPECTRUM_ID,spectrum.getSpectrumId());
				  List<DBSignal> v=DBSignalPeer.doSelect(crit2);
				  if(v.size()>0){
					  DBSignal signal=(DBSignal)v.get(0);
					  sb.append("<td bgcolor=\"#E8E8E8\">"+signal.getFirstShift().getValue()+"</td>");
				  }
			  }
		  }
		  return sb.toString();
	  }
	  
	  
	  public int getOtherSpectraStringSize() throws Exception{
		  if(otherspectrastringsize>-1)
			  return otherspectrastringsize;
		  Criteria crit=new Criteria();
		  crit.add(DBSpectrumPeer.REVIEW_FLAG,"true");
		  crit.add(DBSpectrumPeer.MOLECULE_ID,this.getMoleculeId());
		  crit.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, this.getSpectrumTypeId());
		  crit.add(DBSpectrumPeer.SPECTRUM_ID,this.getSpectrumId(),Criteria.NOT_EQUAL);
		  List<DBSpectrum> spectra=this.getDBMolecule().getDBSpectrums(crit);
		  return(spectra.size());
	  }
	  
	  public List<DBSignal> getDBSignalsUnassigned(int ordercolumn, boolean updown) throws Exception{
		  List<DBSignal> result=new ArrayList<DBSignal>();
		  List<DBSignal> l=this.getDBSignals();
		  for(int i=0;i<l.size();i++){
			  if(((DBSignal)l.get(i)).getDBSignalDBAtoms().size()==0){
				  result.add((DBSignal)l.get(i));
			  }
		  }
		  Collections.sort(result,new DBSignalComparator(ordercolumn,updown));
		  return result;	  
	  }

		/**
		 * Gives you the current spectrum of the edit chain of this spectrum. If this
		 * spectrum has not been edited, this spectrum will be returned.
		 * 
		 * @return The current instance of this spectrum.
		 * @throws NoRowsException
		 * @throws TooManyRowsException
		 * @throws TorqueException
		 */
		public DBSpectrum getCurrentSpectrum() throws NoRowsException, TooManyRowsException, TorqueException {
			DBSpectrum currentsearch=this;
			//we want to avoid endless loops
			int i=0;
			while(true && i<50){
				Criteria crit=new Criteria();
				crit.add(DBUserDBSpectrumEditPeer.SPECTRUM_ID_OLD,currentsearch.getSpectrumId());
				crit.addDescendingOrderByColumn(DBUserDBSpectrumEditPeer.EDIT);
				List<DBUserDBSpectrumEdit> l=DBUserDBSpectrumEditPeer.doSelect(crit);
				if(l.size()>0){
					currentsearch=DBSpectrumPeer.retrieveByPK(((DBUserDBSpectrumEdit)l.get(0)).getSpectrumIdNew());
				}else{
					return currentsearch;
				}
				i++;
			}
			return this;
		}
		
		public String getPredictionProgram() throws TorqueException {
			return getCondition("Program");
		}
		
		  /**
		   *  Gets the conditions of a certain type of the spectra of this molecule.
		   *
		   * @param  conditionName  The condition type the conditions should be of.
		   * @return                A string with all conditions.
		   * @exception  Exception  Database problems.
		   */
		  public String getCondition(String conditionName) throws TorqueException {
		    Criteria crit = new Criteria();
		    crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSpectrumDBConditionPeer.SPECTRUM_ID);
		    crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
		    crit.addJoin(DBConditionPeer.CONDITION_TYPE_ID, DBConditionPeer.CONDITION_TYPE_ID);
		    crit.add(DBConditionTypePeer.CONDITION_NAME, conditionName);
		    crit.add(DBSpectrumPeer.SPECTRUM_ID, this.getSpectrumId());
		    List<DBCondition> v = DBConditionPeer.doSelect(crit);
		    StringBuffer returnValue = new StringBuffer();
		    for (int i = 0; i < v.size(); i++) {
		      returnValue.append(((DBCondition) v.get(i)).getValue() + ";");
		    }
		    return (conditionName + ": " + returnValue.toString());
		  }


		/**
		 * Gets this spectrum as a JCAMP-DX file.
		 * 
		 * @return The JCAMP-DX.
		 * @throws TorqueException
		 * @throws JCAMPException
		 */
		public String getPeakJcamp() throws TorqueException, JCAMPException {
			double[][] xy = new double[2][this.getDBSignals().size()];
			//this array needs to be filled with x/y values in 0/1 position respectivly
			IOrderedDataArray1D x = new OrderedArrayData(xy[0],CommonUnit.mz);
			IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);
			double frequency=0;
			try{
				frequency=Double.parseDouble(this.getFrequency());
			}
			catch(NumberFormatException ex){
			}
			Spectrum1D jdxspectrum = new NMRSpectrum(x, y, this.getDBSpectrumType().getName(), frequency, 0D, false, JCAMPReader.STRICT);
			Peak1D[] jcampPeaks = new Peak1D[this.getDBSignals().size()];
			for(int i=0;i<this.getDBSignals().size();i++){
				Peak1D jcampPeak = new Peak1D(((DBSignal)this.getDBSignals().get(i)).getFirstShift().getValue(),((DBSignal)this.getDBSignals().get(i)).getIntensity());
				jcampPeaks[i] = jcampPeak;
			}
			jdxspectrum.setPeakTable(jcampPeaks);
			JCAMPWriter jcamp = JCAMPWriter.getInstance();
			String jcampString = jcamp.toJCAMP(jdxspectrum);
			return jcampString;
		}

		public String publish(String server, String username, String password) throws FactoryConfigurationError, Exception {
	    	Element data = new Element("data");
	    	appendRecords("MOLECULE","where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("ATOM","where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("BOND","join BOND_ATOM using (BOND_ID) join ATOM using (ATOM_ID) where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("BOND_ATOM","join ATOM using (ATOM_ID) where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("COORDINATE_SET_2D","join ATOM using (ATOM_ID) where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("COORDINATE_SET_3D","join ATOM using (ATOM_ID) where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("CANONICAL_NAME","where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("CHEMICAL_NAME","where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("MOLECULE_HYPERLINK"," where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("KEYWORD","join MOLECULE_KEYWORD using (KEYWORD_ID) where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("MOLECULE_KEYWORD"," where MOLECULE_ID="+this.getMoleculeId(),data);
	    	appendRecords("SPECTRUM","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("COUPLING","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("RAW_FILE","join RAW_FILE_SPECTRUM using (RAW_FILE_ID) where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("RAW_FILE_SPECTRUM","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("NMR_SIGNAL","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("NMR_CONDITION","join SPECTRUM_CONDITION using (CONDITION_ID) where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SPECTRUM_CONDITION","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SPECTRUM_HYPERLINK","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("KEYWORD","join SPECTRUM_KEYWORD using (KEYWORD_ID) where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SPECTRUM_KEYWORD","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SPECTRUM_FILE","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	List<DBLiterature> literatures=this.getLiteraturesNew();
	    	for(int i=0;i<literatures.size();i++){
	    		data.appendChild(literatures.get(i).getAsBibtex());
	    	}
	    	appendRecords("USER_SPECTRUM","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("USER_SPECTRUM_MARK","where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SHIFT","join `NMR_SIGNAL` using (SIGNAL_ID) where SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("SIGNAL_ATOM","join NMR_SIGNAL using (SIGNAL_ID ) where NMR_SIGNAL.SPECTRUM_ID="+this.getSpectrumId(),data);
	    	appendRecords("IDENTIFIER","where SPECTRUM_ID="+this.getSpectrumId(),data);
			NMRShiftDBStub stub = new NMRShiftDBStub(server+"axis/services/NMRShiftDB");

			PublishRequestParametersDocument req = PublishRequestParametersDocument.Factory.newInstance();
	        PublishParameters reqparams = req.addNewPublishRequestParameters();
	        reqparams.setPassword(password);
	        reqparams.setUsername(username);
	        reqparams.setBase64(data.toXML());
	        PublishResponseValueDocument res = stub.publish(req);
		    if(res.getPublishResponseValue().intValue()==0){
		    	this.setPublished(NmrshiftdbConstants.TRUE);
		    	this.save();
		    	return "Spectrum "+this.getSpectrumId()+" has been published!";
		    } else if(res.getPublishResponseValue().intValue()==2){
		    	return "It seems this spectrum is already on the server!";
		    } else {
		    	return "Problem publishing spectrum "+this.getSpectrumId()+", notify admin!";
		    }
	}



		  /**
		   * Appends a columns to an xml element.
		   * 
		   * @param table table name.
		   * @param sql sql to use for select.
		   * @param data The element to append to.
		   * @throws TorqueException
		   * @throws DataSetException
		   * @throws IOException 
		   */
		  private void appendRecords(String table, String sql, Element data) throws TorqueException, DataSetException, IOException {
		  	List<Record> records=DBSignalPeer.executeQuery("select distinct "+table+".* from "+table+" "+sql);
			for(int i=0;i<records.size();i++){
				Element tableElement=new Element(table);
				appendColumns(tableElement, (Record)records.get(i));
				data.appendChild(tableElement);
			}
		  }


		  /**
		   * Appends all columns of a record to an xml element.
		   * 
		   * @param parentElement The element to append to.
		   * @param record The record to append.
		   * @throws DataSetException
		   * @throws IOException 
		   * @throws TorqueException 
		   */	
		  private void appendColumns(Element parentElement, Record record) throws DataSetException, TorqueException, IOException {
			for(int i=1;i<=record.schema().numberOfColumns();i++){
				Element value=new Element(record.schema().column(i).name());
				value.addAttribute(new Attribute("type",record.schema().column(i).dbType()));
				if(record.getValue(i).isNull())
					value.appendChild("null");
				else if(parentElement.getLocalName().equals("SPECTRUM") && value.getLocalName().equals("RAW_DATA") && this.getDBRawFileDBSpectrums().size()>0)
				    value.appendChild(getRawFileBase64Encoded(((DBRawFileDBSpectrum)this.getDBRawFileDBSpectrums().get(0)).getDBRawFile().getUrl()));
				else
					value.appendChild(record.getValue(i).toString());
				parentElement.appendChild(value);
			}	
		  }

        private String getRawFileBase64Encoded(String url) throws IOException {
            byte[] content = GeneralUtils.downloadUrl(url);
            return new Base64().encodeToString(content);
        }
        
        /**
         * Get the ReportUrl as String
         *
         * @return String
         * @throws TorqueException 
         */
        public String getReportUrlString() throws TorqueException
        {
            return new String(getReportUrl());
        }
        
        /**
         * Gives a list of all edits done on this spectrum by tracking back via the edited spectra.
         * 
         * @return A list of all DBUserDBSpectrumEdit associated with this spectrum (directly or indirectly).
         * @throws TorqueException
         */
        public List<DBUserDBSpectrumEdit> getDBUserDBSpectrumEditsAll() throws TorqueException{
            List<DBUserDBSpectrumEdit> edits= new ArrayList<DBUserDBSpectrumEdit>();
            DBSpectrum spectrum = this;
            while(spectrum.getDBUserDBSpectrumEditsRelatedBySpectrumIdNew().size()>0){
                spectrum=((DBUserDBSpectrumEdit)this.getDBUserDBSpectrumEditsRelatedBySpectrumIdNew().get(0)).getDBSpectrumRelatedBySpectrumIdOld();
                edits.addAll((List<DBUserDBSpectrumEdit>)this.getDBUserDBSpectrumEditsRelatedBySpectrumIdNew());
                if(((DBUserDBSpectrumEdit)this.getDBUserDBSpectrumEditsRelatedBySpectrumIdNew().get(0)).getSpectrumIdNew()==((DBUserDBSpectrumEdit)this.getDBUserDBSpectrumEditsRelatedBySpectrumIdNew().get(0)).getSpectrumIdOld())
                    break;
            }
            return edits;
        }
        
        /**
         * Tells if this spectrum has any raw files attached in the RAW_FILE table (not in the rawData field of this class).
         * 
         * @return true=there is at least one rawfile, false=there is none.
         * @throws TorqueException
         */
        public boolean hasRawDataInFilesystem() throws TorqueException{
            return this.getDBRawFileDBSpectrums().size()>0;
        }

        /**
         * Tells the URL of the first raw file of this spectrum (we cannot have more than one currently). Will fail if there is no rawfile. 
         * 
         * @return The URL.
         * @throws TorqueException
         */
        public String getRawDataFromFileSystemURL() throws TorqueException {
            return ((DBRawFileDBSpectrum)this.getDBRawFileDBSpectrums().get(0)).getDBRawFile().getUrl();
        }

        /**
         * Return all DBKeyword objects associated with this spectrum.
         * 
         * @return The DBKeywords of this spectrum.
         * @throws TorqueException
         */
        public List<DBKeyword> getDBKeywords() throws TorqueException {
            List<DBKeyword> keywords=new ArrayList<DBKeyword>();
            if(keywordsVector==null){
                Criteria crit = new Criteria();
                crit.add(DBSpectrumDBKeywordPeer.SPECTRUM_ID, this.getSpectrumId());
                keywordsVector = DBSpectrumDBKeywordPeer.doSelect(crit);
                for(int i=0;i<keywordsVector.size();i++)
                    ((DBSpectrumDBKeyword)keywordsVector.get(i)).getDBKeyword();

            }
            for(int i=0;i<keywordsVector.size();i++)
                keywords.add(((DBSpectrumDBKeyword)keywordsVector.get(i)).getDBKeyword());
            return keywords;
        }
        
        /**
         * Gets the solvent of this spectrum. Relies on the existence of a condition called "Solvent".
         * 
         * @return The solvent. If there is no condition with name "Solvent" this is an empty string.
         * @throws TorqueException
         */
        public String getSolvent() throws TorqueException{
            List<DBCondition> l=getMeasurementConditions();
            for(int i=0;i<l.size();i++){
                if(((DBCondition)l.get(i)).getConditionName().equals("Solvent"))
                    return ((DBCondition)l.get(i)).getValue();
            }
            return "";
        }

		/**
		 * Returns the datasetname for this spectrum ("General" if no keyword).
		 * 
		 * @return
		 * @throws TorqueException
		 */
		public String getDatasetName() throws TorqueException {
			if(this.getDBKeywords().size()>0)
				return this.getDBKeywords().get(0).getKeyword()+"/"+this.getSolvent();
			else
				return "General";
		}
        
        public String getMolImage() {
            return molImage;
        }

        public void setMolImage(String molImage) {
            this.molImage = molImage;
        }
        
        public DBSignal getAssignedSignal(DBAtom atom) throws TorqueException{
        	for(DBSignal signal : this.getDBSignals()){
        		for(DBAtom atomlocal : signal.getAtoms()){
        			if(atomlocal.getAtomId()==atom.getAtomId())
        				return signal;
        		}
        	}
        	return null;
        }

		public int getDatasetReviewKey(String datasetkeyword, String datasetsolvent) throws DataSetException, TorqueException {
			StringBuffer sql=new StringBuffer("SELECT DATASET_REVIEW_KEY.REVIEW_KEY FROM NMR_CONDITION, DATASET_REVIEW_KEY, ");
            StringTokenizer st=new StringTokenizer(datasetkeyword,"_");
            int i=1;
            while(st.hasMoreTokens()) {
            	if(i>1)
            		sql.append(", ");
            	sql.append("DATASET_REVIEW_KEY_KEYWORD as d"+i+", KEYWORD as k"+i+" ");
            	st.nextToken();
            	i++;
            }
            sql.append("WHERE ");
            i=1;
            st=new StringTokenizer(datasetkeyword,"_");
            while(st.hasMoreTokens()) {
            	sql.append("DATASET_REVIEW_KEY.DATASET_REVIEW_KEY_ID=d"+i+".DATASET_REVIEW_KEY_ID and d"+i+".KEYWORD_ID=k"+i+".KEYWORD_ID and k"+i+".KEYWORD='"+st.nextToken()+"' and ");
            	i++;
            }	
            sql.append("NMR_CONDITION.CONDITION_ID=DATASET_REVIEW_KEY.CONDITION_ID AND DATASET_REVIEW_KEY.MOLECULE_ID="+this.getDBMolecule().getMoleculeId()+" AND NMR_CONDITION.VALUE='"+datasetsolvent+"'");
            List<Record> result = DBDatasetReviewKeyPeer.executeQuery(sql.toString());
            if(result.size()>0)
            	return result.get(0).getValue(1).asInt();
            else
            	return 0;
		}
		
		public List<DBSignal> signalsprov;
		
	    public List<DBSignal> getDBSignalsSorted()
	            throws TorqueException
	        {
	    	if(signalsprov!=null) {
	    		return signalsprov;
	        }else {
	            Criteria crit=new Criteria();
	            crit.addJoin(DBSignalPeer.SIGNAL_ID,  DBShiftPeer.SIGNAL_ID);
	            crit.addAscendingOrderByColumn(DBShiftPeer.VALUE);
	            return this.getDBSignals(crit);
	        }
	        }

		public void getNmreData(boolean datasetonly, boolean bysolventandtemp, DBSpectrum spectrum, double freq, double[][] xy, String location, String absolutePath, NmreData data) throws TorqueException, NumberFormatException, JCAMPException, NmrshiftdbException {
        	if((bysolventandtemp && this.getSolvent().equals(spectrum.getSolvent()) && this.getTemperature().equals(spectrum.getTemperature())) || ((!datasetonly && this.getSpectrumId()==spectrum.getSpectrumId()) || (datasetonly && this.getSolvent().equals(spectrum.getSolvent()) && this.getKeywords().equals(spectrum.getKeywords())))){
	           	Peak[] peaks=null;
	           	Spectrum nmrspectrum=null;
	           	if(this.getDBSpectrumType().getDimensionality()==1){
	           		Set<Double> doneshifts = new HashSet<Double>();
	           		peaks = new Peak1D[this.getDBSignals().size()];
	                for(int i=0;i<this.getDBSignals().size();i++){
	                	peaks[i]=new Peak1D(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getFirstShift().getValue()).toString()).doubleValue(),0);
	                }
	                Unit xUnit =  CommonUnit.hertz;
	                Unit yUnit = CommonUnit.intensity;
	                double reference = 0;
	                IOrderedDataArray1D x = new OrderedArrayData(new double[0], xUnit);
	                IDataArray1D y = new ArrayData(new double[0], yUnit);
	                if(peaks.length>0){
	        	        xy = NmredataReader.peakTableToPeakSpectrum((Peak1D[])peaks);
	        	        x = new OrderedArrayData(xy[0], xUnit);
	        	        y = new ArrayData(xy[1], yUnit);
	                }
	            	//TODO freq
	            	String type=this.getDBSpectrumType().getDBIsotope(1).getAtomicMass()+this.getDBSpectrumType().getDBIsotope(1).getElementSymbol();
	            	if(this.getDBSpectrumType().getNmredataDimensionality()==1) {
		                nmrspectrum = new NMRSpectrum(x, y, type, freq, reference, false, JCAMPReader.RELAXED);
	            	}else {
	            		nmrspectrum = new SelectiveNMR1DSpectrum(x, y, freq, reference, this.getDBSpectrumType().getDBIsotope(1).getAtomicMass()+this.getDBSpectrumType().getDBIsotope(1).getElementSymbol(), this.getDBSpectrumType().getDBIsotope(2).getAtomicMass()+this.getDBSpectrumType().getDBIsotope(2).getElementSymbol());
	            	}
	                ((NMRSpectrum)nmrspectrum).setPeakTable((Peak1D[])peaks);
		            NoteDescriptor noteDescriptor2=new NoteDescriptor("CorType");
		            nmrspectrum.setNote(noteDescriptor2, this.getDBSpectrumType().getNmredataName());
		            List<Assignment> assignmentslist=new ArrayList<>();
		            for(int i=0;i<this.getDBSignals().size();i++){
		            	for(int k=0;k<this.getDBSignals().get(i).getAtoms().size();k++){
		            		if(this.getDBMolecule().setid==13)
			            		assignmentslist.add(new Assignment(new Pattern(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue(), Multiplicity.UNKNOWN), new IAssignmentTarget[]{new AtomReference(null, this.getDBSignals().get(i).getAtoms().get(k).getAtomNumber())}));
		            		else
			            		assignmentslist.add(new Assignment(new Pattern(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue(), Multiplicity.UNKNOWN), new IAssignmentTarget[]{new AtomReference(null, this.getDBSignals().get(i).getAtoms().get(k).getMdlNumber())}));
				            doneshifts.add(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue());
		            	}
		            }
		    		Assignment[] assignmentslocal=(Assignment[])assignmentslist.toArray(new Assignment[assignmentslist.size()]);
	           		((NMRSpectrum)nmrspectrum).setAssignments(assignmentslocal);
	            }else if(this.getDBSpectrumType().getDimensionality()==2){
	            	Set<Pattern> doneshifts = new HashSet<Pattern>();
  	                peaks = new Peak2D[this.getDBSignals().size()];
	                for(int i=0;i<this.getDBSignals().size();i++){
	                	peaks[i]=new Peak2D(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue(),Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(2).getValue()).toString()).doubleValue(),0);
	                }
	                Unit xUnit =  CommonUnit.hertz;
	                Unit yUnit =  CommonUnit.hertz;
	                Unit zUnit = CommonUnit.intensity;
	                double reference = 0;
	                IOrderedDataArray1D x = new OrderedArrayData(new double[0], xUnit);
	                IOrderedDataArray1D y = new OrderedArrayData(new double[0], yUnit);
	                IDataArray1D z = new ArrayData(new double[0], zUnit);
	                if(peaks.length>0){
	        	        xy = NmredataReader.peakTableToPeakSpectrum(peaks);
	        	        x = new OrderedArrayData(xy[0], xUnit);
	        	        y = new OrderedArrayData(xy[1], yUnit);
	        	        z = new ArrayData(xy[2], zUnit);
	                }
	            	String[] type=new String[2];
	            	type[0]=this.getDBSpectrumType().getDBIsotope(1).getAtomicMass()+this.getDBSpectrumType().getDBIsotope(1).getElementSymbol();
	            	type[1]=this.getDBSpectrumType().getDBIsotope(2).getAtomicMass()+this.getDBSpectrumType().getDBIsotope(2).getElementSymbol();
	            	nmrspectrum = new NMR2DSpectrum(x, y, z, type, new double[] { freq, freq}, new double[] {reference,reference});
	                ((NMR2DSpectrum)nmrspectrum).setPeakTable((Peak2D[])peaks);
		            NoteDescriptor noteDescriptor2=new NoteDescriptor("CorType");
		            nmrspectrum.setNote(noteDescriptor2, this.getDBSpectrumType().getNmredataName());
		            List<Assignment> assignmentslist=new ArrayList<>();
		            for(int i=0;i<this.getDBSignals().size();i++){
		            	for(int k=0;k<this.getDBSignals().get(i).getAtoms().size()/1;k++){
		            		Pattern pattern = new Pattern(Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue(), Multiplicity.UNKNOWN);
		            		pattern.setPosition(new double[] {Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(1).getValue()).toString()).doubleValue(),Double.valueOf(Float.valueOf(this.getDBSignals().get(i).getDBShift(2).getValue()).toString()).doubleValue()});
	            			if(!doneshifts.contains(pattern)) {
	            				if(this.getDBMolecule().setid==13)
		            				assignmentslist.add(new Assignment(pattern, new IAssignmentTarget[]{new TwoAtomsReference(null, new int[] {this.getDBSignals().get(i).getDBAtom(1, k).getAtomNumber()}, new int[] {this.getDBSignals().get(i).getDBAtom(2, k).getAtomNumber()})}));
		            			else
		            				assignmentslist.add(new Assignment(pattern, new IAssignmentTarget[]{new TwoAtomsReference(null, new int[] {this.getDBSignals().get(i).getDBAtom(1, k).getMdlNumber()}, new int[] {this.getDBSignals().get(i).getDBAtom(2, k).getMdlNumber()})}));
		            			doneshifts.add(pattern);
		            		}
		            	}
		            }
		    		Assignment[] assignmentslocal=(Assignment[])assignmentslist.toArray(new Assignment[assignmentslist.size()]);
	           		((NMR2DSpectrum)nmrspectrum).setAssignments(assignmentslocal);	           		
	            }
	            NoteDescriptor noteDescriptor=new NoteDescriptor("Spectrum_Location");
	            if(absolutePath!=null && this.getRawData()!=null) {
	            	location=this.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, absolutePath);
	            }
	    		nmrspectrum.setNote(noteDescriptor, location);
	            data.addSpectrum(nmrspectrum);
        	}
		}
}


