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
package org.openscience.nmrshiftdb;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.turbine.util.RunData;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.BremserOneSphereHOSECodePredictor;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.ExtendedHOSECodeGenerator;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.comparator.ShiftsAsRecordComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import VisualNumerics.math.Statistics;

/**
 * For everything related to the standalone prediction tool read http://sourceforge.net/p/nmrshiftdb2/wiki/PredictorJars/
 *
 * @author     shk3
 * @created    September 23, 2004
 */
public class PredictionTool {
  public static Map<String,Map<String,Object>> mapsmap = new HashMap<String,Map<String,Object>>(500000);
  public static Map<String,double[]> usedHoseCodes = new HashMap<String,double[]>();

  public static double[] generalPredict(IAtomContainer mol, IAtom a, boolean calculated, boolean measured, boolean hose3d, boolean force3d, int ignoreSpectrum, int ignoreSpectrumEnd, StringBuffer comment, boolean commentWithMinMax, boolean withRange, RunData runData, Map<Double, double[]> predictionValuesForApplet, int maxSpheresToUse, boolean cache, StringBuffer hoseCodeOut, int spheresMax, boolean fromDB, boolean trueonly, DBCondition solvent) throws CDKException, TorqueException, NmrshiftdbException, DataSetException, CloneNotSupportedException {
	  return generalPredict(mol, a, calculated, measured, hose3d, force3d, ignoreSpectrum, ignoreSpectrumEnd, comment, commentWithMinMax, withRange, runData, predictionValuesForApplet, maxSpheresToUse, cache, hoseCodeOut, spheresMax, fromDB, trueonly, solvent, null);
  }
  /**
   *  This method does a prediction, either from the database or from the mapsmap initialized in the constructor. This should not be used directly when using the stand-alone predictor; use predict() then.
   *
   * @param  comment                    Contains additional text after processing predictRange().
   * @param  mol                        The molecule the atoms comes from.
   * @param  a                          The atom the shift of which to be predicted.
   * @param  commentWithMinMax          Shall min/max values be included in comments.
   * @param  ignoreSpectrum             A molecule to be ignored in the prediction (-1=none).
   * @param  withRange                  Is the range to be calculated as well ? (use only when needed for performance reasons).
   * @param  calculated                 Use calculated spectra.
   * @param  measured                   Use measured spectra.
   * @param  hose3d						true=Use 3d hose codes, false=use normal hose codes.
   * @param  force3d					true=Use 3d hose codes always, 2d predictions are ignored, false=fall back on 2d for every sphere.
   * @param  runData                    The current runData object.
   * @param  predictionValuesForApplet  Will become the String to diplay the histogram in the applet, null if not wished.
   * @param  maxSpheresToUse            Restrict number of spheres to use, to use max spheres set -1.
   * @param  cache                      true=Use HOSE_CODES table, false=do join query.
   * @param  hoseCodeOut                Contains the used HOSE_CODE.
   * @param  spheresMax                 Default maximum spheres to use.
   * @param  fromDB                     Do prediction from db or mapsmap?
   * @param  trueonly                   Use only spectra review as true
   * @param  solvent                    The solvent to predict for. If null or the id is -1 all are used.
   * @param  maxHoseCode				If not null, this contains the 6 sphere 3d hose code (not the one actually used).
   * @return                            An array of doubles. Meaning: 0=lower limit, 1=mean, 2=upper limit calculated via confidence limits, 3=median, 4=used spheres, 5=number of values, 6=standard deviation, 7=min value, 8=max value, 9=values from labgroup used (0=no, 1=yes), 10=chiral smiles used (0=no, 1=yes).
   * @throws NmrshiftdbException 
   * @throws DataSetException 
   * @throws CDKException
   * @exception  TorqueException              Database problems.
 * @throws CloneNotSupportedException 
   */
  public static double[] generalPredict(IAtomContainer mol, IAtom a, boolean calculated, boolean measured, boolean hose3d, boolean force3d, int ignoreSpectrum, int ignoreSpectrumEnd, StringBuffer comment, boolean commentWithMinMax, boolean withRange, RunData runData, Map<Double, double[]> predictionValuesForApplet, int maxSpheresToUse, boolean cache, StringBuffer hoseCodeOut, int spheresMax, boolean fromDB, boolean trueonly, DBCondition solvent, StringBuffer maxHoseCode) throws CDKException, TorqueException, NmrshiftdbException, DataSetException, CloneNotSupportedException {
	  IAtomContainer molwithouth=AtomUtils.convertImplicitToExplicitHydrogens(mol);
	  int atomnumber=mol.getAtomNumber(a);
	  HOSECodeGenerator hcg = new HOSECodeGenerator();
          ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
	  if (maxSpheresToUse == -1 || maxSpheresToUse > spheresMax) {
			  maxSpheresToUse = spheresMax;
	  }
	  if (fromDB) {
		  cache=true;
		DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
		format.applyPattern("0.00");
		List<Record> l = null;
		int spheres;
		String usedHoseCode = "";
		double[] returnValues = new double[11];
		List<Record> resultShift = null;
		int usedSpheres = -1;
		String simpleHoseCode = null;
		BremserOneSphereHOSECodePredictor bremser = null;
		String hoseCodeOrigExtended=null;
		if(hose3d || maxHoseCode!=null) {
		    hoseCodeOrigExtended=ehcg.getHOSECode(mol, mol.getAtom(atomnumber), maxSpheresToUse,true);
		    if(maxHoseCode!=null) {
		    	maxHoseCode.append(hoseCodeOrigExtended);
		    }
		}
		String hoseCodeOrig=null;
		if(mol.getAtom(atomnumber).getSymbol().equals("H"))
			hoseCodeOrig=hcg.getHOSECode(mol, mol.getAtom(atomnumber), maxSpheresToUse,false);
		else
			hoseCodeOrig=hcg.getHOSECode(molwithouth, molwithouth.getAtom(atomnumber), maxSpheresToUse,false);
		StringBuffer firstPartOfQuery = new StringBuffer();
		StringBuffer secondPartOfQuery = new StringBuffer();
		  if (cache) {
			  firstPartOfQuery.append("SELECT DISTINCT VALUE, SPECTRUM_ID, LABGROUP_ID FROM HOSE_CODES WHERE SYMBOL='" + mol.getAtom(atomnumber).getSymbol() + "' AND ");
			  secondPartOfQuery.append("%'" + (!trueonly ? " and REVIEW_FLAG='true'" : ""));
			  if (ignoreSpectrum >-1) {
				  if(ignoreSpectrumEnd==-1){
					  secondPartOfQuery.append(" and SPECTRUM_ID <> " + ignoreSpectrum);
				  }else{
					  secondPartOfQuery.append(" and (SPECTRUM_ID < " + ignoreSpectrum+" or SPECTRUM_ID>"+ignoreSpectrumEnd+")");
				  }
			  }
		  } else {
			  firstPartOfQuery.append("SELECT DISTINCT SHIFT.VALUE, SPECTRUM.SPECTRUM_ID, NMR_SIGNAL.SIGNAL_ID, USER_HISTORY.LABGROUP_ID FROM SPECTRUM_TYPE, ISOTOPE, SPECTRUM, ATOM, SIGNAL_ATOM, NMR_SIGNAL, SHIFT, MOLECULE, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE, TURBINE_USER, USER_HISTORY    WHERE (SPECTRUM_TYPE.DIMENSIONALITY='1') AND (ISOTOPE.ELEMENT_SYMBOL='" + mol.getAtom(atomnumber).getSymbol() + "') AND (SPECTRUM.REVIEW_FLAG='true'"+ (trueonly ? "or SPECTRUM.REVIEW_FLAG='false'" : "") +") and SPECTRUM.SPECTRUM_ID=SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID=`NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID=CONDITION_TYPE.CONDITION_TYPE_ID AND ATOM.ATOM_ID=SIGNAL_ATOM.ATOM_ID AND SIGNAL_ATOM.SIGNAL_ID=NMR_SIGNAL.SIGNAL_ID AND NMR_SIGNAL.SPECTRUM_ID=SPECTRUM.SPECTRUM_ID AND NMR_SIGNAL.SIGNAL_ID=SHIFT.SIGNAL_ID AND SPECTRUM.SPECTRUM_TYPE_ID=SPECTRUM_TYPE.SPECTRUM_TYPE_ID AND SPECTRUM.MOLECULE_ID=MOLECULE.MOLECULE_ID AND SPECTRUM_TYPE.SPECTRUM_TYPE_ID=ISOTOPE.SPECTRUM_TYPE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null and ");
			  secondPartOfQuery.append("%'");
			  if (ignoreSpectrum >-1) {
				  if(ignoreSpectrumEnd==-1){
					  secondPartOfQuery.append(" and SPECTRUM.SPECTRUM_ID <> " + ignoreSpectrum);
				  }else{
					  secondPartOfQuery.append(" and (SPECTRUM.SPECTRUM_ID < " + ignoreSpectrum+" or SPECTRUM.SPECTRUM_ID>"+ignoreSpectrumEnd+")");
				  }
			  }
		  }
		  if (measured ^ calculated) {
			  if (measured) {
				  secondPartOfQuery.append(" and CONDITION_TYPE='m'");
			  } else {
				  secondPartOfQuery.append(" and (CONDITION_TYPE='c' or CONDITION_TYPE='p')");
			  }
		  }
		  if(solvent!=null && solvent.getConditionId()>-1){
			  secondPartOfQuery.append(" and CONDITION_ID="+solvent.getConditionId()+" ");
		  }
		  secondPartOfQuery.append(" ORDER BY VALUE;");
		//Check starting from the max sphere hose code if there at least one value
		//(this will be the prediction and will be in resultShift) and if a range is 
		//desired check for at least 10 values (these will be in l)
		for (spheres = maxSpheresToUse; spheres > 0; spheres--) {
			if(hose3d){
			  StringTokenizer st = new StringTokenizer(hoseCodeOrigExtended, "()/");
			  StringBuffer hoseCodeBuffer = new StringBuffer();
			  for (int k = 0; k < spheres; k++) {
			    if (st.hasMoreTokens()) {
			      String partcode = st.nextToken();
			      hoseCodeBuffer.append(partcode);
			    }
			    if (k == 0) {
			      hoseCodeBuffer.append("(");
			    } else if (k == 3) {
			      hoseCodeBuffer.append(")");
			    } else {
			      hoseCodeBuffer.append("/");
			    }
			  }
			  String hoseCodeUnescaped = hoseCodeBuffer.toString();
			  String hoseCode = GeneralUtils.escape(hoseCodeUnescaped,false);
			  hoseCode = GeneralUtils.replace(hoseCode,"\\\\", "\\\\\\\\");
			  StringBuffer query = new StringBuffer();
			  query.append(firstPartOfQuery.toString());
			  if(cache)
				  query.append("HOSE_CODE like '");
			  else
				  query.append("HOSE_CODE_WITH_RINGS like '");
			  query.append(hoseCode);
			  query.append(secondPartOfQuery);
              l = BasePeer.executeQuery(query.toString());
			  if (l.size() > 0) {
			    if (resultShift == null) {
			      resultShift = l;
			      usedSpheres = spheres;
			      usedHoseCode = hoseCodeUnescaped;
			      if(hoseCodeUnescaped.indexOf('@')>-1 || hoseCodeUnescaped.indexOf('|')>-1 || hoseCodeUnescaped.indexOf('\\')>-1)
			    	  returnValues[10]=1;
			    }
			    if (!withRange) {
			      break;
			    }
			  }
			}
		  if (resultShift == null && !force3d) {
		        StringTokenizer st = new StringTokenizer(hoseCodeOrig, "()/");
				StringBuffer hoseCodeBuffer = new StringBuffer();
		        for (int k = 0; k < spheres; k++) {
		          if (st.hasMoreTokens()) {
		            String partcode = st.nextToken();
		            hoseCodeBuffer.append(partcode);
		          }
		          if (k == 0) {
		            hoseCodeBuffer.append("(");
		          } else if (k == 3) {
		            hoseCodeBuffer.append(")");
		          } else {
		            hoseCodeBuffer.append("/");
		          }
		        }
		        String hoseCodeUnescaped = hoseCodeBuffer.toString();
		        String hoseCode = GeneralUtils.escape(hoseCodeUnescaped,false);
				hoseCode = GeneralUtils.replace(hoseCode,"\\\\", "\\\\\\\\");
				StringBuffer query = new StringBuffer();
				query.append(firstPartOfQuery.toString());
				query.append("HOSE_CODE like '");
				query.append(hoseCode);
				query.append(secondPartOfQuery);
		        l = BasePeer.executeQuery(query.toString());
		        if (l.size() > 0) {
		          if (resultShift == null) {
		            resultShift = l;
		            usedSpheres = spheres;
		            usedHoseCode = hoseCodeUnescaped;
		            returnValues[10]=0;
		          }
		          if (!withRange) {
		            break;
		          }
		        }
		  }
		  if (l.size() > 10) {
		    break;
		  }
		}
		  //handle diastereotopic carbons
		  //I must be a CH3 and there must be at least two values
		  if(mol.getAtom(atomnumber).getSymbol().equals("C") && AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(atomnumber))==3 && resultShift!=null && resultShift.size()>1){
			  IAtom nextAtom = mol.getConnectedAtomsList(mol.getAtom(atomnumber)).get(0);
			  //these are first sphere atoms around neighbour, incl myself
			  List<IAtom> connatoms=mol.getConnectedAtomsList(nextAtom);
			  //neighbours must be 4: me, other CH3, the chiral center, h
			  if(connatoms.size()==4){
				  boolean hasOtherCh3=false;
				  IAtom chiralCenter=null;
				  boolean hash=false;
				  for(IAtom connatom : connatoms){
					  //do nothing with myself
					  if(connatom!=mol.getAtom(atomnumber)){
						  if(connatom.getSymbol().equals("C") && AtomContainerManipulator.countExplicitHydrogens(mol, connatom)==3){
							  hasOtherCh3=true;
						  }else if(AtomUtils.isTetrahedral(mol, connatom, true)!=0){
							  chiralCenter=connatom;
						  }else if(connatom.getSymbol().equals("H")){
							  hash=true;
						  }
					  }
				  }
				  if(hasOtherCh3 && chiralCenter!=null && hash){
					  Collections.sort(resultShift, new ShiftsAsRecordComparator());
					  //divide shifts in two halfes
					  if(BondTools.isLeft(mol.getAtom(atomnumber), chiralCenter, nextAtom)){
						  resultShift=resultShift.subList(0, ((int)resultShift.size()/2));
					  }else{
						  resultShift=resultShift.subList(((int)resultShift.size()/2),((int)resultShift.size()));
					  }
				  }
			  }
		  }
	        //handle diastereotopic hydrogens
	        //I must be a H, must be part of a CH2 and there must be at least two values
	        /*if(a.getSymbol().equals("H") && AtomContainerManipulator.countExplicitHydrogens(mol, mol.getConnectedAtomsList(a).get(0))==2 && resultShift.size()>1){
	            IAtom nextAtom = mol.getConnectedAtomsList(a).get(0);
	            //these are first sphere atoms around neighbour, incl mysql
	            List<IAtom> connatoms=mol.getConnectedAtomsList(nextAtom);
	            boolean isfirst=true;
	            for(IAtom connatom : connatoms){
	                //do nothing with myself
	                if(connatom.getSymbol().equals("H")){
	                    if(connatom!=a){
	                        isfirst=true;
	                        break;
	                    } else{
	                        isfirst=false;
	                        break;
	                    }
	                }
	            }
	            Collections.sort(resultShift, new ShiftsAsRecordComparator());
	            //divide shifts in two halfes
	            if(isfirst){
	                resultShift=resultShift.subList(0, ((int)resultShift.size()/2));
	            }else{
	                resultShift=resultShift.subList(((int)resultShift.size()/2),((int)resultShift.size()));
	            }
	        }*/
		//If there isn't one even for one sphere, fall back to BremserOneSphereHOSECodePredictor for prediction
		if (resultShift == null) {
		  if (mol.getAtom(atomnumber).getSymbol().equals("C")) {
	        hoseCodeOrig=hcg.getHOSECode(mol, mol.getAtom(atomnumber), 1,false);
		    StringTokenizer st = new StringTokenizer(hoseCodeOrig, "()/");
		    String hc = st.nextToken() + "(//)";
		    st = new StringTokenizer(hc, ";");
		    st.nextToken();
		    bremser = new BremserOneSphereHOSECodePredictor();
		    simpleHoseCode = st.nextToken();
		    returnValues[1] = bremser.predict(simpleHoseCode);
		    spheres = -1;
		  } else {
		    throw new NmrshiftdbException("impossible");
		  }
		} else {
		  double[] values = new double[resultShift.size()];
		  boolean fromLabGroup = false;
		  for (int i = 0; i < resultShift.size(); i++) {
		      values[i] = ((Record) resultShift.get(i)).getValue(1).asDouble();
		      if(((Record) resultShift.get(i)).getValue(3).asDouble()>0)
		    	  fromLabGroup=true;
		  }
		  returnValues[1] = Statistics.average(values);
		  returnValues[7] = values[0];
		  returnValues[8] = values[values.length - 1];
		  returnValues[5] = values.length;
		  returnValues[6] = Statistics.standardDeviation(values);
		  returnValues[3] = Statistics.median(values);
		  returnValues[9] = fromLabGroup ? 1 : 0;
		  if (predictionValuesForApplet != null && predictionValuesForApplet.get(new Double(returnValues[1])) == null) {
		    predictionValuesForApplet.put(new Double(returnValues[1]), values);
		  }
		}
		if (hoseCodeOut != null) {
		  hoseCodeOut.append(usedHoseCode);
		}
		if (withRange) {
		  //If there aren't 10 values even for one sphere, fall back to BremserOneSphereHOSECodePredictor for range
		  if (l.size() < 11 && mol.getAtom(atomnumber).getSymbol().equals("C")) {
		    if (bremser == null) {
		      String hc = hcg.getHOSECode(mol, mol.getAtom(atomnumber), 1, true);
		      StringTokenizer st = new StringTokenizer(hc, ";");
		      st.nextToken();
		      bremser = new BremserOneSphereHOSECodePredictor();
		      simpleHoseCode = st.nextToken();
		    }
		    try {
			    double confidenceLimit = bremser.getConfidenceLimit(simpleHoseCode);
			    returnValues[0] = returnValues[1] - confidenceLimit;
			    returnValues[2] = returnValues[1] + confidenceLimit;
		    }catch(CDKException ex) {
		    	//this should be a No confidence limit available, we ignore this
		    }
		  } else {
		    returnValues[0]=returnValues[1];
		    returnValues[2]=returnValues[1];
		    for (int i = 0; i < l.size(); i++) {
		      double value= ((Record) l.get(i)).getValue(1).asDouble();
		      if(value<returnValues[0])
		        returnValues[0]=value;
		      if(value>returnValues[2])
		        returnValues[2]=value;
		    }
		  }
		  if(returnValues[0]==returnValues[1])
		    returnValues[0]=returnValues[1]-(returnValues[2]-returnValues[1]);
		  if(returnValues[2]==returnValues[1])
		    returnValues[3]=returnValues[1]+(returnValues[1]-returnValues[0]);
		}
		comment.append((spheres == -1 ? "cdk prediction" : usedSpheres + " spheres/"+ (returnValues[10]==1 ? "stereo HOSE": "2D HOSE")) + ", " + (returnValues[5] > 999 ? ">999" : (int) returnValues[5] + "") + " shift values, ");
		if (commentWithMinMax) {
		  comment.append((returnValues[2] - returnValues[0] > 50 ? "<a href=\"javascript:alert('Such unlikely large confidence limits may appear in same cases\nsince limits are statistically calculated. It does not mean\nthat the min/max values are actually in the db!')\">" : "") + "min: " + format.format(returnValues[7]) + ", max: " + format.format(returnValues[8]) + (returnValues[2] - returnValues[0] > 100 ? "</a>" : "") + ", ");
		}
		comment.append("median: " + format.format(returnValues[3]) + ", mean: " + format.format(returnValues[1]) + ",<br> Standard deviation: " + (returnValues[5]>1 ? format.format(returnValues[6]) : "-"));
		returnValues[4] = usedSpheres;
		return (returnValues);
	  }else{
		  double[] returnValues = new double[9];
		    int spheres;
		    String hoseCodeOrigExtended=null;
		    if(hose3d){
            	hoseCodeOrigExtended=ehcg.getHOSECode(mol, mol.getAtom(atomnumber), maxSpheresToUse,true);
            }
            if(usedHoseCodes.containsKey(hoseCodeOrigExtended)){
                return usedHoseCodes.get(hoseCodeOrigExtended);
            }
            String hoseCode = hcg.getHOSECode(mol, mol.getAtom(atomnumber), maxSpheresToUse, false);
            for (spheres = maxSpheresToUse; spheres > 0; spheres--) {
		        StringBuffer hoseCodeBuffer = new StringBuffer();
		        StringBuffer hoseCodeBufferExtended = new StringBuffer();
		        StringTokenizer st = new StringTokenizer(hoseCode, "()/");
		        StringTokenizer stExtended = null;
		        if(hose3d){
		            stExtended = new StringTokenizer(hoseCodeOrigExtended, "()/");
		        }
		        for (int k = 0; k < spheres; k++) {
		          if (st.hasMoreTokens()) {
		            String partcode = st.nextToken();
		            hoseCodeBuffer.append(partcode);
		            if(hose3d){
		                partcode=stExtended.nextToken();
		                hoseCodeBufferExtended.append(partcode);
		            }
		          }
		          if (k == 0) {
		            hoseCodeBuffer.append("(");
		            if(hose3d)
		                hoseCodeBufferExtended.append("(");
		          } else if (k == 3) {
		            hoseCodeBuffer.append(")");
		            if(hose3d)
		                hoseCodeBufferExtended.append(")");
		          } else {
		            hoseCodeBuffer.append("/");
		            if(hose3d)
		                hoseCodeBufferExtended.append("/");
		          }
		        }
		        ValueBean l = null;
		        if(hose3d)
		            l = ((ValueBean) ((HashMap<String, Object>) mapsmap.get(mol.getAtom(atomnumber).getSymbol())).get(hoseCodeBufferExtended.toString()));
		        if(l!=null){
	          	  returnValues[0]=l.min;
	        	  returnValues[1]=l.average;
	        	  returnValues[2]=l.max;
	        	  returnValues[4]=spheres;
	        	  usedHoseCodes.put(hoseCodeOrigExtended, returnValues);
	              return returnValues;
		        }else{
			        l = ((ValueBean) ((HashMap<String, Object>) mapsmap.get(mol.getAtom(atomnumber).getSymbol())).get(hoseCodeBuffer.toString()));
		          if (l != null) {
		          	  returnValues[0]=l.min;
		        	  returnValues[1]=l.average;
		        	  returnValues[2]=l.max;
		        	  returnValues[4]=spheres;
		        	  usedHoseCodes.put(hoseCodeOrigExtended, returnValues);
		        	  return returnValues;
		          }
		    }
		    }
      	  returnValues[0]=-1;
    	  returnValues[1]=-1;
    	  returnValues[2]=-1;
    	  returnValues[4]=-1;
    	  usedHoseCodes.put(hoseCodeOrigExtended, returnValues);
		    return returnValues;
	  }
  }
  
  public static class ValueBean{
	  public float min;
	  public float average;
	  public float max;
	  public int numValues;
	  
	  public ValueBean(float a, float b, float c, int numValues){
		  min=a;
		  average=b;
		  max=c;
		  this.numValues=numValues;
	  }
  }
}

