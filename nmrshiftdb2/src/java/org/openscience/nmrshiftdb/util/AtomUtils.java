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
package org.openscience.nmrshiftdb.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Point2d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.invariant.ConjugatedPiSystemsDetector;
import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.layout.HydrogenPlacer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.normalize.Normalizer;
import org.openscience.cdk.qsar.descriptors.atomic.AtomHybridizationVSEPRDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.AtomValenceDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.BondsToAtomDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.EffectiveAtomPolarizabilityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.IsProtonInAromaticSystemDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.IsProtonInConjugatedPiSystemDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialPiChargeDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PartialSigmaChargeDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PeriodicTablePositionDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.PiElectronegativityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.RDFProtonDescriptor_G3R;
import org.openscience.cdk.qsar.descriptors.atomic.RDFProtonDescriptor_GDR;
import org.openscience.cdk.qsar.descriptors.atomic.RDFProtonDescriptor_GHR;
import org.openscience.cdk.qsar.descriptors.atomic.RDFProtonDescriptor_GHR_topol;
import org.openscience.cdk.qsar.descriptors.atomic.RDFProtonDescriptor_GSR;
import org.openscience.cdk.qsar.descriptors.atomic.SigmaElectronegativityDescriptor;
import org.openscience.cdk.qsar.descriptors.atomic.VdWRadiusDescriptor;
import org.openscience.cdk.qsar.descriptors.atompair.PiContactDetectionDescriptor;
import org.openscience.cdk.qsar.result.BooleanResult;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.FixBondOrdersTool;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.nmrshiftdb.PredictionTool;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBAtomPeer;
import org.openscience.nmrshiftdb.om.DBBond;
import org.openscience.nmrshiftdb.om.DBBondDBAtom;
import org.openscience.nmrshiftdb.om.DBBondDBAtomPeer;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBIdentifierPeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.w3c.dom.Document;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.workingdogs.village.DataSetException;

import nu.xom.Attribute;
import nu.xom.Element;


/**
 *Contains some utils for Molecules/Atoms
 *
 * @author     shk3
 * @created    12. April 2002
 */
public class AtomUtils {

  static String rowstext = "";
  static int rownumber = 0;
  static int colsnumber = 0;
  static VdWRadiusDescriptor vdwrdes = null;
  static SigmaElectronegativityDescriptor sedes = null;
  static PiElectronegativityDescriptor pedes = null;
  static BondsToAtomDescriptor btades = new BondsToAtomDescriptor();
  static PeriodicTablePositionDescriptor ptpdes = new PeriodicTablePositionDescriptor();
  final static Boolean FALSE = new Boolean(false);
  static HOSECodeGenerator hcg = null;
  static RDFProtonDescriptor_G3R rdfg3r = null;
  static RDFProtonDescriptor_GDR rdfgdr = null;
  static RDFProtonDescriptor_GHR rdfghr = null;
  static RDFProtonDescriptor_GSR rdfgsr = null;
  static RDFProtonDescriptor_GHR_topol rdfghrtopol = null;
  static IsProtonInConjugatedPiSystemDescriptor ipicpsdes = null;
  static IsProtonInAromaticSystemDescriptor ipiasdes = null;
  static EffectiveAtomPolarizabilityDescriptor epdes = null;
  static PartialPiChargeDescriptor ppichargedes=null;
  static PartialSigmaChargeDescriptor psigmachargedes=null;
  static PiContactDetectionDescriptor picondes = new PiContactDetectionDescriptor();
  private static Document normalizerdoc = null;
  private static IAtomContainer oldmol = null;
  private static IRingSet allrings = null;
  private static Integer[] maxparameters = {new Integer(20)};



  /**
   *Constructor for the AtomUtils object (private since only static methods)
   */
  private AtomUtils() { }


  /**
   *Gets the signals assigned to an atom out of the specfile as contained in specfile attribute of Spectrum in the database as a String like 1.2,3.4,...
   *
   * @param  atom      The number of the atom
   * @param  specFile  The specfile
   * @return           The signals value
   */
  public static String getValuesForAtom(int atom, String specFile) {
    return (getValuesForAtomWithNumber(atom, specFile, -1, -1));
  }


  /**
   *Gets the signals assigned to an atom out of the specfile as contained in specfile attribute of Spectrum in the database as a String like 1.2,3.4,... and it adds a text like Hx: to a certain signal
   *
   * @param  atom      The number of the atom
   * @param  specFile  The specfile
   * @param  value     The value a text should be added to
   * @param  number    This text is added to value in a form like Hnumber:
   * @return           The signals value
   */
  public static String getValuesForAtomWithNumber(int atom, String specFile, double value, int number) {
	    String result = "";
	    List<CMLPeak> valueTriples = ParseUtils.parseSpectrumFromSpecFile(specFile);
	    for (int i = 0; i < valueTriples.size(); i++) {
	      CMLPeak thisTriple = valueTriples.get(i);
	      if(thisTriple.getAtomRefs()!=null){
	    	  if (Arrays.asList(thisTriple.getAtomRefs()).contains("a"+atom)) {
	            if (!result.equals("")) {
	              result += " + ";
	            }
	            if (thisTriple.getXValue() == value) {
	              result += "H" + number + ":" + thisTriple.getXValue();
	            } else {
	              result += thisTriple.getXValue();
	            }
	          }
	      }
	    }
	    if (result.equals("")) {
	      return ("");
	    } else {
	      return (result);
	    }
	  }


  /**
   *Gets a BigIntger value representation of the Fingerprint bitset
   *
   * @param  bs   Fingerprint bitset
   * @param  num  A number between 63 and 1023 representing one of the 16 sectors of the Fingerprint
   * @return      The BigInteger representations of the specified Fingerprint sector
   */
  public static BigInteger getBigIntegerValue(BitSet bs, int num) {
    BigInteger bi = new BigInteger("0");
    for (int i = 0; i < 64; i++) {
      if (bs.get(i + (num * 64))) {
    	  bi = bi.add(new BigInteger("2").pow(i));
      }
    }
    return bi;
  }


  /**
   *  Gets the proton Class of a proton according to aires-do-sousa/gasteiger system.
   *
   * @param  cdkmol         The molecule to analyze
   * @param  atomnumber     The atom to classify
   * @param  arf            An existing instace of allringsfinder, to save time.
   * @return                The proton class (1-4)
   * @exception  Exception  Description of Exception
   */
  public static int getProtonClass(IAtomContainer cdkmol, int atomnumber, IRingSet ringSetL) {
    if (cdkmol != oldmol) {
      allrings=ringSetL;
    }
    int protonclass = 4;
    boolean class2 = false;
    for (int l = 0; l < cdkmol.getBondCount(); l++) {
      if (cdkmol.getBond(l).getAtom(0) == cdkmol.getConnectedAtomsList(cdkmol.getAtom(atomnumber)).get(0) || cdkmol.getBond(l).getAtom(1) == cdkmol.getConnectedAtomsList(cdkmol.getAtom(atomnumber)).get(0)) {
        if (cdkmol.getBond(l).getOrder() == IBond.Order.DOUBLE) {
          class2 = true;
        }
      }
    }
    if (((IAtom)cdkmol.getConnectedAtomsList(cdkmol.getAtom(atomnumber)).get(0)).getFlag(CDKConstants.ISAROMATIC)) {
      protonclass = 1;
    } else if (allrings.getRings((IAtom)cdkmol.getConnectedAtomsList(cdkmol.getAtom(atomnumber)).get(0)).getAtomContainerCount() > 0) {
      protonclass = 3;
    } else if (class2) {
      protonclass = 2;
    }
    return protonclass;
  }

  /**
   *  Gets the carbon Class of a proton according to aires-do-sousa/gasteiger system.
   *
   * @param  cdkmol         The molecule to analyze
   * @param  atomnumber     The atom to classify
   * @param  arf            An existing instace of allringsfinder, to save time.
   * @return                The proton class (1-4)
   * @exception  Exception  Description of Exception
   */
  public static int getCarbonClass(IAtomContainer cdkmol, int atomnumber, IRingSet ringSetL) throws Exception {
    if (cdkmol != oldmol) {
      allrings=ringSetL;
    }
    int protonclass = 4;
    boolean class2 = false;
    for (int l = 0; l < cdkmol.getBondCount(); l++) {
      if (cdkmol.getBond(l).getAtom(0) == cdkmol.getAtom(atomnumber) || cdkmol.getBond(l).getAtom(1) == cdkmol.getAtom(atomnumber)) {
        if (cdkmol.getBond(l).getOrder() == IBond.Order.DOUBLE) {
          class2 = true;
        }
      }
    }
    if (cdkmol.getAtom(atomnumber).getFlag(CDKConstants.ISAROMATIC)) {
      protonclass = 1;
    } else if (allrings.getRings(cdkmol.getAtom(atomnumber)).getAtomContainerCount() > 0) {
      protonclass = 3;
    } else if (class2) {
      protonclass = 2;
    }
    return protonclass;
  }

  /**
   *  Gets the allRings attribute, set with last run of getProtonClass.
   *
   * @return    The allRings value
   */
  public static IRingSet getAllRings() {
    return allrings;
  }


  /**
   *  Creates a set of Desctiptors taken from Aires-do-Sousa publications.
   *
   * @param  ac                  The atom container.
   * @param  a                   The atom for which descriptors should be calculated.
   * @param  atomInFocusInteger  The number of a as an Integer.
   * @param  rs                  The setOfAllRings of ac.
   * @return                     The descriptorvalues.
   * @throws CDKException 
   * @exception  Exception       Description of Exception.
   */
  public static float[] getDescriptorSetDoSousa(IAtomContainer ac, IAtom a, IAtom atomInFocus, IRingSet rs, int protonclass) throws CDKException {
    if (ppichargedes == null) {
      ppichargedes=new PartialPiChargeDescriptor();
      psigmachargedes=new PartialSigmaChargeDescriptor();
    }
    //Integer atomInteger = new Integer(ac.getAtomNumber(a));
    float[] floats = new float[148];
    initminusone(floats);
    if (ipicpsdes == null) {
      ipicpsdes = new IsProtonInConjugatedPiSystemDescriptor();
    }
    Object[] parameters = {FALSE};
    ipicpsdes.setParameters(parameters);
    BooleanResult b2 = (BooleanResult) ipicpsdes.calculate(a,ac).getValue();
    floats[0] = (b2.booleanValue() ? 1 : 0);
    if (ipiasdes == null) {
      ipiasdes = new IsProtonInAromaticSystemDescriptor();
    }
    Object[] parameters1 = {FALSE};
    ipiasdes.setParameters(parameters1);
    IntegerResult b = (IntegerResult) ipiasdes.calculate(a,ac).getValue();
    floats[1] = ((float) b.intValue());
    if (epdes == null) {
      epdes = new EffectiveAtomPolarizabilityDescriptor();
    }
    Object[] parameters2 = {};
    epdes.setParameters(parameters2);
    DoubleResult l = (DoubleResult) epdes.calculate(a,ac).getValue();
    floats[2] = ((float) l.doubleValue());
    floats[3] = (float)((DoubleResult)ppichargedes.calculate(a,ac).getValue()).doubleValue();
    floats[4] = (float)((DoubleResult)psigmachargedes.calculate(a,ac).getValue()).doubleValue();
    floats[5]=floats[4]+floats[3];
    floats[6] = (float) ((DoubleResult)ppichargedes.calculate((IAtom)ac.getConnectedAtomsList(a).get(0),ac).getValue()).doubleValue();
    floats[7] = (float) ((DoubleResult)psigmachargedes.calculate((IAtom)ac.getConnectedAtomsList(a).get(0),ac).getValue()).doubleValue();
    floats[8]=floats[7]+floats[6];
    if (hcg == null) {
      hcg = new HOSECodeGenerator();
    }
    hcg.getHOSECode(ac, a, 2);
    List<IAtom> atoms = hcg.getNodesInSphere(2);
    int ccount = 0;
    for (int k = 0; k < atoms.size(); k++) {
      if (((IAtom) atoms.get(k)).getSymbol().equals("C")) {
        ccount++;
      }
    }
    atoms = hcg.getNodesInSphere(2);
    int ocount = 0;
    for (int k = 0; k < atoms.size(); k++) {
      if (atoms.get(k) != null && ((IAtom) atoms.get(k)).getSymbol().equals("O")) {
        ocount++;
      }
    }
    floats[9] = (((float) ccount));
    floats[10] = (((float) ocount));
    if (sedes == null) {
      sedes = new SigmaElectronegativityDescriptor();
      pedes = new PiElectronegativityDescriptor();
    }
    sedes.setParameters(maxparameters);
    pedes.setParameters(maxparameters);
    DoubleResult dr = (DoubleResult) sedes.calculate(atomInFocus,ac).getValue();
    floats[11] = ((float) dr.doubleValue());
    dr = (DoubleResult) pedes.calculate(atomInFocus,ac).getValue();
    floats[12] = ((float) dr.doubleValue());
    double charge2pi = 0;
    double charge2minpi = Float.MAX_VALUE;
    double charge2maxpi = 0;
    double charge2sigma = 0;
    double charge2minsigma = Float.MAX_VALUE;
    double charge2maxsigma = 0;
    double se2 = 0;
    double se2max = 0;
    double se2min = Double.MAX_VALUE;
    double pe2 = 0;
    double pe2max = 0;
    double pe2min = Double.MAX_VALUE;
    for (int k = 0; k < atoms.size(); k++) {
      double se = ((DoubleResult) sedes.calculate((IAtom) atoms.get(k),ac).getValue()).doubleValue();
      double pe = ((DoubleResult) pedes.calculate((IAtom) atoms.get(k),ac).getValue()).doubleValue();
      pe2 += pe;
      float picharge=(float) ((DoubleResult)ppichargedes.calculate((IAtom) atoms.get(k),ac).getValue()).doubleValue();
      float sigmacharge=(float) ((DoubleResult)psigmachargedes.calculate((IAtom) atoms.get(k),ac).getValue()).doubleValue();
      charge2pi += picharge;
      charge2sigma += sigmacharge;
      if (picharge > charge2maxpi) {
        charge2maxpi = picharge;
      }
      if (picharge < charge2minpi) {
        charge2minpi = charge2pi;
      }
      if (sigmacharge > charge2maxsigma) {
          charge2maxsigma = sigmacharge;
      }
      if (sigmacharge < charge2minsigma) {
          charge2minsigma = charge2sigma;
      }
      if (se < se2min) {
        se2min = se;
      }
      if (se > se2max) {
        se2max = se;
      }
      if (pe < pe2min) {
        pe2min = pe;
      }
      if (pe > pe2max) {
        pe2max = pe;
      }
    }
    floats[13] = ((float) charge2pi / atoms.size());
    floats[14] = ((float) charge2sigma / atoms.size());
    floats[15] = ((float) (charge2sigma / atoms.size()+charge2pi / atoms.size()));
    floats[16] = ((float) se2);
    floats[17] = ((float) se2min);
    floats[18] = ((float) se2max);
    floats[19] = ((float) pe2);
    floats[20] = ((float) pe2min);
    floats[21] = ((float) pe2max);
    floats[22] = ((float) charge2minsigma);
    floats[23] = ((float) charge2maxsigma);
    floats[24] = ((float) charge2minpi);
    floats[25] = ((float) charge2maxpi);
    floats[26] = ((float) (charge2maxsigma+charge2maxpi));
    floats[27] = ((float) (charge2minpi+charge2minsigma));
    if (rdfg3r== null) {
          rdfg3r = new RDFProtonDescriptor_G3R(); 
          rdfgdr = new RDFProtonDescriptor_GDR();
          rdfghr = new RDFProtonDescriptor_GHR();
          rdfgsr = new RDFProtonDescriptor_GSR();
          rdfghrtopol = new RDFProtonDescriptor_GHR_topol();
    }
    rdfg3r.setParameters(parameters);
    rdfgdr.setParameters(parameters);
    rdfghr.setParameters(parameters);
    rdfgsr.setParameters(parameters);
    rdfghrtopol.setParameters(parameters);
    DoubleArrayResult result = (DoubleArrayResult) rdfg3r.calculate(a,ac, rs).getValue();
    for(int i=0;i<result.length();i++){
        if(!Double.isNaN(result.get(i)))
            floats[28+i]=(float)result.get(i);
    }
    result = (DoubleArrayResult) rdfgdr.calculate(a,ac, rs).getValue();
    for(int i=0;i<result.length();i++){
        if(!Double.isNaN(result.get(i)))
            floats[43+i]=(float)result.get(i);
    }
    result = (DoubleArrayResult) rdfghr.calculate(a,ac, rs).getValue();
    for(int i=0;i<result.length();i++){
        if(!Double.isNaN(result.get(i)))
            floats[58+i]=(float)result.get(i);
    }
    result = (DoubleArrayResult) rdfgsr.calculate(a,ac, rs).getValue();
    for(int i=0;i<result.length();i++){
        if(!Double.isNaN(result.get(i)))
            floats[73+i]=(float)result.get(i);
    }
    result = (DoubleArrayResult) rdfghrtopol.calculate(a,ac, rs).getValue();
    for(int i=0;i<result.length();i++){
        if(!Double.isNaN(result.get(i)))
            floats[88+i]=(float)result.get(i);
    }
    if(protonclass==2){
        IAtom firstheavy=(IAtom)ac.getConnectedAtomsList(a).get(0);
        List<IBond> bondsoffirstheavy=ac.getConnectedBondsList(firstheavy);
        IBond doublebond=null;
        for(int k=0;k<bondsoffirstheavy.size();k++){
            if(((IBond)bondsoffirstheavy.get(k)).getOrder()==IBond.Order.DOUBLE){
                doublebond=(IBond)bondsoffirstheavy.get(k);
                break;
            }
        }
        IAtom secondheavy=(IAtom)doublebond.getAtom(0);
        if(firstheavy==secondheavy)
            secondheavy=(IAtom)doublebond.getAtom(1);
        floats[103]=(float) ((DoubleResult)psigmachargedes.calculate(secondheavy,ac).getValue()).doubleValue();
        floats[104]=(float) ((DoubleResult)ppichargedes.calculate(secondheavy,ac).getValue()).doubleValue();
        floats[105]=floats[101]+floats[100];
        Object[] parameters4 = {};
        dr = (DoubleResult) sedes.calculate(secondheavy,ac).getValue();
        floats[106] = ((float) dr.doubleValue());
        dr = (DoubleResult) pedes.calculate(secondheavy,ac).getValue();
        floats[107] = ((float) dr.doubleValue());
        epdes.setParameters(parameters4);
        dr = (DoubleResult) epdes.calculate(secondheavy,ac).getValue();
        floats[108] = ((float) dr.doubleValue());
        List<IAtom> atomsonfirst=ac.getConnectedAtomsList(firstheavy);
        IAtom otheronfirstheavy=null;
        for(int k=0;k<atomsonfirst.size();k++){
            if(atomsonfirst.get(k)!=secondheavy && atomsonfirst.get(k)!=a){
                otheronfirstheavy=(IAtom)atomsonfirst.get(k);
                break;
            }
        }
        doPidescriptors(ac, otheronfirstheavy, floats, 109);
        IAtom firstonsecond=null;
        IAtom secondonsecond=null;
        List<IAtom> connectedtosecond=ac.getConnectedAtomsList(secondheavy);
        for(int k=0;k<connectedtosecond.size();k++){
            if(connectedtosecond.get(k)!=firstheavy){
                if(firstonsecond==null){
                    firstonsecond=(IAtom)connectedtosecond.get(k);
                }else{
                    secondonsecond=(IAtom)connectedtosecond.get(k);
                    break;
                }
            }
        }
        try{
            if(BondTools.isCisTrans(firstonsecond,secondheavy,firstheavy,otheronfirstheavy,ac)){
                doPidescriptors(ac, firstonsecond, floats, 121);
                doPidescriptors(ac, secondonsecond, floats, 133);
            }else{
                doPidescriptors(ac, firstonsecond, floats, 133);
                doPidescriptors(ac, secondonsecond, floats, 121);
            }
        }catch(Exception ex){
          if(firstonsecond!=null)
            doPidescriptors(ac, firstonsecond, floats, 133);
          if(secondonsecond!=null)
            doPidescriptors(ac, secondonsecond, floats, 121);
        }
    }
    return (floats);
  }
  
  private static void doPidescriptors(IAtomContainer ac, IAtom otheronfirstheavy, float[] floats, int startfield) throws CDKException{
      floats[startfield]=(float) ((DoubleResult)psigmachargedes.calculate(otheronfirstheavy,ac).getValue()).doubleValue();
      floats[startfield+1]=(float) ((DoubleResult)psigmachargedes.calculate(otheronfirstheavy,ac).getValue()).doubleValue();
      floats[startfield+2]=floats[startfield+1]+floats[startfield];
      Object[] parameters5 = {};
      DoubleResult dr = (DoubleResult) sedes.calculate(otheronfirstheavy,ac).getValue();
      floats[startfield+3] = ((float) dr.doubleValue());
      dr = (DoubleResult) pedes.calculate(otheronfirstheavy,ac).getValue();
      floats[startfield+4] = ((float) dr.doubleValue());
      epdes.setParameters(parameters5);
      dr = (DoubleResult) epdes.calculate(otheronfirstheavy,ac).getValue();
      floats[startfield+5] = ((float) dr.doubleValue());
      IAtomContainerSet acSet = ConjugatedPiSystemsDetector.detect(ac);
      floats[startfield+6]=0;
      for(int l=0;l<acSet.getAtomContainerCount();l++){
          for(int k=0;k<acSet.getAtomContainer(l).getAtomCount();k++){
              if(acSet.getAtomContainer(l).getAtom(k)==otheronfirstheavy)
                  floats[startfield+6]=1;
          }
      }       
      Object[] parameters6 = {new Boolean(false)};
      ipiasdes.setParameters(parameters6);
      IntegerResult b = (IntegerResult) ipiasdes.calculate(otheronfirstheavy,ac).getValue();
      floats[startfield+7] = ((float) b.intValue());
      if(otheronfirstheavy.getSymbol().equals("C"))
        floats[startfield+8]=1;
      else
        floats[startfield+8]=0;
      if(otheronfirstheavy.getSymbol().equals("H"))
        floats[startfield+9]=1;
      else
        floats[startfield+9]=0;
      if(otheronfirstheavy.getSymbol().equals("O"))
        floats[startfield+10]=1;
      else
        floats[startfield+10]=0;
      if(otheronfirstheavy.getSymbol().equals("N"))
        floats[startfield+11]=1;
      else
        floats[startfield+11]=0;
  }
  
  private static void initminusone(float[] floats){
	  for(int i=0;i<floats.length;i++){
		  floats[i]=-1;
	  }
  }

  /**
   *  Creates a set of Desctiptors taken from Jens Meilers's publications and Aires-do-Sousa..
   *
   * @param  temp           The temperature.
   * @param  field          The field strength.
   * @param  solvent        The solvent.
   * @param  ac             The atomContainer.
   * @param  a              The atom to create descriptors for.
   * @param  rs             The setOfAllRings of ac
   * @return                The descriptors
   * @throws CDKException 
   * @throws IOException 
   * @throws ClassNotFoundException 
   * @throws TorqueException 
   * @exception  Exception  Description of Exception
   */
  public static float[] getDescriptorSet(DBCondition temp, DBCondition field, DBCondition solvent, IAtomContainer ac, IAtom a, IRingSet rs, int protonclass) throws CDKException, ClassNotFoundException, IOException, TorqueException {
      IAtom atomInFocus = a;
      if(a.getSymbol().equals("H")) {
    	  a=(IAtom)ac.getConnectedAtomsList(a).get(0);
      }
      Integer atomInFocusInt = new Integer(ac.getAtomNumber(atomInFocus));
      AtomHybridizationVSEPRDescriptor hybdes = new AtomHybridizationVSEPRDescriptor();
      float[] floats = new float[515];
      initminusone(floats);
      if (sedes == null) {
        sedes = new SigmaElectronegativityDescriptor();
        pedes = new PiElectronegativityDescriptor();
      }
      Object[] parameters9 = {atomInFocusInt};
      Object[] parameters3 = {};
      sedes.setParameters(maxparameters);
      pedes.setParameters(maxparameters);
      DoubleResult dr = (DoubleResult) sedes.calculate(atomInFocus,ac).getValue();
      floats[0] = ((float) dr.doubleValue());
      dr = (DoubleResult) pedes.calculate(atomInFocus,ac).getValue();
      floats[1] = ((float) dr.doubleValue());
      if (vdwrdes == null) {
        vdwrdes = new VdWRadiusDescriptor();
      }
      vdwrdes.setParameters(parameters3);
      dr = (DoubleResult) vdwrdes.calculate(atomInFocus,ac).getValue();
      floats[2] = ((float) dr.doubleValue());
      hybdes.setParameters(parameters3);
      
      IntegerResult ir = (IntegerResult) hybdes.calculate(atomInFocus,ac).getValue();
      System.err.println(atomInFocus.getSymbol()+" "+ir+" "+atomInFocus.getFormalNeighbourCount());
      floats[3] = (ir.intValue() == CDKConstants.HYBRIDIZATION_SP2 ? 1 : 0);
      floats[4] = (float) (atomInFocus.getFormalNeighbourCount() - floats[3]);
      int substituents = 0;
      List<IAtom> v = ac.getConnectedAtomsList(atomInFocus);
      for (int i = 0; i < v.size(); i++) {
        if (!((IAtom)v.get(i)).getSymbol().equals("H")) {
          substituents++;
        }
      }
      floats[5] = (float) substituents;
      AtomValenceDescriptor avdes = new AtomValenceDescriptor();
      avdes.setParameters(parameters3);
      ir = (IntegerResult) avdes.calculate(atomInFocus,ac).getValue();
      floats[6] = ((float) ir.intValue());
      double sumDistance = 0;
      double sumElectronegativity = 0;
      double numberElectronegativity = 0;
      double sumHybridization = 0;
      int sumValenceElectrons = 0;
      List<IAtom> closestInSpace = GeometryTools.findClosestInSpace(ac, atomInFocus, 16);
      List<IAtom> connectedAtoms = ac.getConnectedAtomsList(atomInFocus);
      for (int i = 0; i < closestInSpace.size(); i++) {
        IAtom closest = (IAtom) closestInSpace.get(i);
        Integer closestInt = new Integer(ac.getAtomNumber(closest));
        Object[] parameters4 = {closestInt};
        dr = (DoubleResult) sedes.calculate(closest,ac).getValue();
        if (closest.getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
          sumElectronegativity += dr.doubleValue() * 1 / closest.getPoint3d().distance(atomInFocus.getPoint3d());
        }
        numberElectronegativity++;
        floats[11 + (i * 11) + 0] = ((float) dr.doubleValue());
        dr = (DoubleResult) pedes.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 1] = ((float) dr.doubleValue());
        vdwrdes.setParameters(parameters4);
        dr = (DoubleResult) vdwrdes.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 2] = ((float) dr.doubleValue());
        hybdes.setParameters(parameters4);
        ir = (IntegerResult) hybdes.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 3] = (ir.intValue() == CDKConstants.HYBRIDIZATION_SP2 ? 1 : 0);
        if (closest.getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
          sumHybridization += floats[10 + (i * 10) + 2];
        }
        ptpdes.setParameters(parameters4);
        ir = (IntegerResult) ptpdes.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 4] = ((float) ir.intValue());
        avdes.setParameters(parameters4);
        ir = (IntegerResult) avdes.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 5] = ((float) ir.intValue());
        if (closest.getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
          sumValenceElectrons += ir.intValue() * 1 / closest.getPoint3d().distance(atomInFocus.getPoint3d());
        }
        if (closest.getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
          sumDistance += 1 / closest.getPoint3d().distance(atomInFocus.getPoint3d());
        }
        BooleanResult br = (BooleanResult) picondes.calculate(closest,atomInFocus,ac).getValue();
        floats[11 + (i * 11) + 6] = (br.booleanValue() == false ? 0 : 1);
        btades.setParameters(parameters9);
        ir = (IntegerResult) btades.calculate(closest,ac).getValue();
        floats[11 + (i * 11) + 7] = ((float) ir.intValue());
        floats[11 + (i * 11) + 8] = (float) atomInFocus.getPoint3d().distance(closest.getPoint3d());
        double distanceMin = Double.MAX_VALUE;
        double distanceAll = 0;
        int numberofhs = 0;
        for (int k = 0; k < connectedAtoms.size(); k++) {
          if (((IAtom)connectedAtoms.get(k)).getSymbol().equals("H")) {
            numberofhs++;
            distanceAll += ((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closest.getPoint3d());
            if (((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closest.getPoint3d()) < distanceMin) {
              distanceMin = ((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closest.getPoint3d());
            }
          }
        }
        floats[11 + (i * 11) + 9] = (float) (distanceAll / numberofhs);
        floats[11 + (i * 11) + 10] = (float) distanceMin;
      }
      IAtom[] closestByBond = PathTools.findClosestByBond(ac, atomInFocus, 16);
      for(int i=0;i<closestByBond.length;i++){
          if(closestInSpace.contains(closestByBond[i])){
              closestByBond[i]=null; 
          }
      }
      int counter=0;
      for (int i = 0; i < closestByBond.length; i++) {
        if(closestByBond[i]!=null){
            Object[] parameters5 = {};
            dr = (DoubleResult) sedes.calculate(closestByBond[i],ac).getValue();
            if (closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
              sumElectronegativity += dr.doubleValue() * 1 / closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d());
            }
            numberElectronegativity++;
            floats[187 + (counter * 11) + 0] = ((float) dr.doubleValue());
            dr = (DoubleResult) pedes.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 1] = ((float) dr.doubleValue());
            vdwrdes.setParameters(parameters5);
            dr = (DoubleResult) vdwrdes.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 2] = ((float) dr.doubleValue());
            hybdes.setParameters(parameters5);
            ir = (IntegerResult) hybdes.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 3] = (ir.intValue() == CDKConstants.HYBRIDIZATION_SP2 ? 1 : 0);
            if (closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
              sumHybridization += floats[170 + (counter * 10) + 2];
            }
            ptpdes.setParameters(parameters5);
            ir = (IntegerResult) ptpdes.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 4] = ((float) ir.intValue());
            avdes.setParameters(parameters5);
            ir = (IntegerResult) avdes.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 5] = ((float) ir.intValue());
            if (closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
              sumValenceElectrons += ir.intValue() * 1 / closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d());
            }
            if (closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d()) != 0) {
              sumDistance += 1 / closestByBond[i].getPoint3d().distance(atomInFocus.getPoint3d());
            }
            BooleanResult br = (BooleanResult) picondes.calculate(closestByBond[i],atomInFocus,ac).getValue();
            floats[187 + (counter * 11) + 6] = (br.booleanValue() == false ? 0 : 1);
            Object[] parameters8 = {atomInFocusInt};
            btades.setParameters(parameters8);
            ir = (IntegerResult) btades.calculate(closestByBond[i],ac).getValue();
            floats[187 + (counter * 11) + 7] = ((float) ir.intValue());
            floats[187 + (counter * 11) + 8] = (float) atomInFocus.getPoint3d().distance(closestByBond[i].getPoint3d());
            double distanceMin = Double.MAX_VALUE;
            double distanceAll = 0;
            int numberofhs = 0;
            for (int k = 0; k < connectedAtoms.size(); k++) {
              if (((IAtom)connectedAtoms.get(k)).getSymbol().equals("H")) {
                numberofhs++;
                distanceAll += ((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closestByBond[i].getPoint3d());
                if (((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closestByBond[i].getPoint3d()) < distanceMin) {
                  distanceMin = ((IAtom)connectedAtoms.get(k)).getPoint3d().distance(closestByBond[i].getPoint3d());
                }
              }
            }
            floats[187 + (counter * 11) + 9] = (float) (distanceAll / numberofhs);
            floats[187 + (counter * 11) + 10] = (float) distanceMin;
            counter++;
        }
      }
      floats[7] = (float) (sumDistance / numberElectronegativity);
      floats[8] = (float) (sumElectronegativity / numberElectronegativity);
      floats[9] = (float) (sumValenceElectrons / numberElectronegativity);
      floats[10] = (float) (sumHybridization / numberElectronegativity);
      if (temp==null || temp.getValue().equals("Unreported") || temp.getValue().equals("Unknown")) {
        floats[364] = -1;
      } else {
        floats[364] = Float.parseFloat(temp.getValue());
      }
      if (field==null || field.getValue().equals("Unreported") || field.getValue().equals("Unknown")) {
        floats[365] = -1;
      } else {
        floats[365] = Float.parseFloat(field.getValue());
      }
      Criteria crit = new Criteria();
      crit.add(DBConditionPeer.CONDITION_TYPE_ID, 2);
      List<DBCondition> solvents = DBConditionPeer.doSelect(crit);
      if (field.getValue().equals("Unreported") || field.getValue().equals("Unknown")) {
          floats[366] = -1;
      } else {
          floats[366] = solvents.indexOf(solvent);
      }
      float[] dosousa = getDescriptorSetDoSousa(ac, a, atomInFocus, rs, protonclass);
      for (int i = 0; i < dosousa.length; i++) {
        floats[367 + i] = dosousa[i];
      }
      return (floats);
  } 


  /**
   *  Exuecutes cdk normalizer with given xml file
   *
   * @param  ac                                                        The structure to normalize.
   * @param  path                                                      The file to use.
   * @return                                                           true=changed, false=unchanged.
   * @exception  org.openscience.cdk.exception.InvalidSmilesException  Description of Exception
   * @exception  org.xml.sax.SAXException                              Description of Exception
   * @exception  FileNotFoundException                                 Description of Exception
   * @exception  IOException                                           Description of Exception
   * @exception  ParserConfigurationException                          Description of Exception
   * @exception  CDKException                                          Description of Exception
   */
  public static boolean normalize(IAtomContainer ac, String path) throws InvalidSmilesException, CDKException{
	if(path==null)
		return false;
    try{
      if (normalizerdoc == null) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        normalizerdoc = db.parse(new FileInputStream(path));
      }
      return Normalizer.normalize(ac, normalizerdoc);
    }catch(Exception ex){
      GeneralUtils.logError(ex,"normalize",null,false);
      return false;
    }
  }


  /**
   *  This method predicts the range and the shift of a C atom from the database.
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
   * @param  solvent                    The solvent to predict for. If null or the id is -1 all are used.
   * @param  maxHoseCode				If not null, this contains the 6 sphere 3d hose code (not the one actually used).
   * @return                            An array of doubles. Meaning: 0=lower limit, 1=mean, 2=upper limit calculated via confidence limits, 3=median, 4=used spheres, 5=number of values, 6=standard deviation, 7=min value, 8=max value, 9=values from labgroup used (0=no, 1=yes), 10=chiral smiles used (0=no, 1=yes).
   * @exception  Exception              Database problems.
   * @exception  CDKException           Description of Exception
   */
  public static double[] predictRange(IAtomContainer mol, IAtom a, boolean calculated, boolean measured, boolean hose3d, boolean force3d, int ignoreSpectrum, StringBuffer comment, boolean commentWithMinMax, boolean withRange, RunData runData, Map<Double, double[]> predictionValuesForApplet, int maxSpheresToUse, boolean cache, StringBuffer hoseCodeOut, boolean trueonly, DBCondition solvent, StringBuffer maxhosecode) throws CDKException, TorqueException, NmrshiftdbException, DataSetException, IOException, CloneNotSupportedException {
    return PredictionTool.generalPredict(mol, a, calculated, measured, hose3d, force3d, (ignoreSpectrum == 0 ? -1 : ignoreSpectrum), -1, comment, commentWithMinMax, withRange, runData, predictionValuesForApplet, maxSpheresToUse, cache, hoseCodeOut, maxSpheresToUse==-1 ? Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres")) : maxSpheresToUse, true, trueonly, solvent, maxhosecode);
  }

  /**
   *  This method predicts the range and the shift of a C atom from the database.
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
   * @param  solvent                    The solvent to predict for. If null or the id is -1 all are used.
   * @return                            An array of doubles. Meaning: 0=lower limit, 1=mean, 2=upper limit calculated via confidence limits, 3=median, 4=used spheres, 5=number of values, 6=standard deviation, 7=min value, 8=max value, 9=values from labgroup used (0=no, 1=yes), 10=chiral smiles used (0=no, 1=yes).
   * @exception  Exception              Database problems.
   * @exception  CDKException           Description of Exception
   */
  public static double[] predictRange(IAtomContainer mol, IAtom a, boolean calculated, boolean measured, boolean hose3d, boolean force3d, int ignoreSpectrum, StringBuffer comment, boolean commentWithMinMax, boolean withRange, RunData runData, Map<Double, double[]> predictionValuesForApplet, int maxSpheresToUse, boolean cache, StringBuffer hoseCodeOut, boolean trueonly, DBCondition solvent) throws CDKException, TorqueException, NmrshiftdbException, DataSetException, IOException, CloneNotSupportedException {
    return PredictionTool.generalPredict(mol, a, calculated, measured, hose3d, force3d, (ignoreSpectrum == 0 ? -1 : ignoreSpectrum), -1, comment, commentWithMinMax, withRange, runData, predictionValuesForApplet, maxSpheresToUse, cache, hoseCodeOut, maxSpheresToUse==-1 ? Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres")) : maxSpheresToUse, true, trueonly, solvent);
  }


  /**
   * Produces a list of double bonds with potential double bond configurations in a molecule.
   *
   * @param  mol            The molecule.
   * @param  dbbonds        Description of Parameter
   * @param  specification  Description of Parameter
   * @param  sssr           The smallest set of smallest rings of this molecule.
   * @return                A ArrayList of StringAndInts, each containing the number of a double bond (in StringAndInt.myInt) and a description (in String and Int.myString).
   */
  public static ArrayList<StringAndIntAndBoolean> makeDoubleBondSet(IAtomContainer mol, List<DBBond> dbbonds, boolean[] specification, IRingSet sssr) {
    ArrayList<StringAndIntAndBoolean> v = new ArrayList<StringAndIntAndBoolean>();
    for (int i = 0; i < mol.getBondCount(); i++) {
      if (isValidDoubleBondConfiguration(mol, mol.getBond(i), sssr)) {
        StringAndIntAndBoolean sai = new StringAndIntAndBoolean();
        sai.myString = new String((mol.getAtomNumber(mol.getBond(i).getAtom(0)) + 1) + " = " + (mol.getAtomNumber(mol.getBond(i).getAtom(1)) + 1));
        sai.myInt = i;
        if (dbbonds != null) {
          if (((DBBond) dbbonds.get(i)).getIsConfigurationSpecified().equals(NmrshiftdbConstants.TRUE)) {
            sai.myBoolean = true;
          }
        }
        if (specification != null && specification[i] == true) {
          sai.myBoolean = true;
        }
        v.add(sai);
      }
    }
    return (v);
  }


  /**
   * Parses a fuzzy formula search criteria string.
   *
   * @param  criteria                    The criteria to analyze.
   * @param  checkElements               Tells if element name should be checked for validity.
   * @param  checkmultis                 Description of Parameter
   * @return                             A map with elements names as keys and an array of two strings as value which are lower and upper limit.
   * @exception  NmrshiftdbException     Criteria not correct (see message).
   * @exception  IOException             From IsotopeFactory.
   * @exception  ClassNotFoundException  From IsotopeFactory.
   */
  public static Map<String, String[]> parseFuzzyCriteria(String criteria, boolean checkElements, boolean checkmultis) throws NmrshiftdbException, IOException, ClassNotFoundException {
    if (Character.isLetter(criteria.charAt(criteria.length() - 1))) {
      criteria = criteria + "1";
    }
    if (!Character.isLetter(criteria.charAt(0))) {
      throw new NmrshiftdbException("Syntax error");
    }
    boolean addingChars = true;
    HashMap<String,String[]> result = new HashMap<String,String[]>();
    StringBuffer chars = new StringBuffer();
    StringBuffer ranges = new StringBuffer();
    for (int i = 0; i < criteria.length(); i++) {
      if (addingChars && i + 1 < criteria.length() && (!Character.isLetter(criteria.charAt(i + 1)) || Character.isUpperCase(criteria.charAt(i + 1)))) {
        addingChars = false;
        chars.append(criteria.charAt(i));
      }
      if (addingChars) {
        chars.append(criteria.charAt(i));
        if (i + 1 == criteria.length()) {
          addingChars = false;
        }
      } else {
        if (!Character.isLetter(criteria.charAt(i))) {
          ranges.append(criteria.charAt(i));
        }
        if (i == criteria.length() - 1 || (i + 1 < criteria.length() && Character.isLetter(criteria.charAt(i + 1)))) {
          addingChars = true;
          if (!chars.toString().equals("")) {
            if (ranges.toString().equals("")) {
              ranges.append("1");
            }
            if (checkElements && Isotopes.getInstance().getElement(chars.toString()) == null) {
              throw new NmrshiftdbException("Invalid element name");
            }
            String lowerLimit = "";
            String upperLimit = "";
            if (ranges.toString().indexOf("-") == -1) {
              if (ranges.toString().equals("*")) {
                lowerLimit = "0";
                upperLimit = "" + Integer.MAX_VALUE;
              } else {
                try {
                  lowerLimit = "" + Integer.parseInt(ranges.toString());
                  upperLimit = lowerLimit;
                } catch (NumberFormatException ex) {
                  throw new NmrshiftdbException("One of the ranges contains no -, but is not a figure as well");
                }
              }
            } else {
              try {
                lowerLimit = "" + Integer.parseInt(ranges.toString().substring(0, ranges.toString().indexOf("-")));
                upperLimit = "" + Integer.parseInt(ranges.toString().substring(ranges.toString().indexOf("-") + 1, ranges.toString().length()));
              } catch (NumberFormatException ex) {
                throw new NmrshiftdbException("One of the ranges contains a -, but before or after it is not a figure!");
              }
            }
            if (result.get(chars.toString()) != null) {
            	result.put(chars.toString(), new String[]{new String(""+(Integer.parseInt(lowerLimit)+Integer.parseInt(((String[])result.get(chars.toString()))[0]))), new String(""+(upperLimit+Integer.parseInt(((String[])result.get(chars.toString()))[1])))});
            }else{
            	result.put(chars.toString(), new String[]{lowerLimit, upperLimit});
            }
          } else {
            throw new NmrshiftdbException("Syntax error");
          }
          chars = new StringBuffer();
          ranges = new StringBuffer();
        }
      }
    }
    if (!checkElements && checkmultis) {
      if (result.get("S") == null && result.get("s") == null) {
        result.put("S", new String[]{"0", "0"});
      }
      if (result.get("D") == null && result.get("d") == null) {
        result.put("D", new String[]{"0", "0"});
      }
      if (result.get("T") == null && result.get("t") == null) {
        result.put("T", new String[]{"0", "0"});
      }
      if (result.get("Q") == null && result.get("q") == null) {
        result.put("Q", new String[]{"0", "0"});
      }
    }
    return (result);
  }
  
  /**
   * Adds implicit hydrogens to a molecule to satisfy valency rules.
   * 
   * @param mol The molecule to saturate.
   * @throws CDKException
   */
  public static void addImplicitHydrogensToSatisfyValency(IAtomContainer mol) throws CDKException{
	CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(mol.getBuilder());    	
    CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
	for (IAtom atom : mol.atoms()) {
		IAtomType type = matcher.findMatchingAtomType(mol, atom);
		if(type!=null){
		    AtomTypeManipulator.configure(atom, type);
		    hAdder.addImplicitHydrogens(mol, atom);
		}if(atom.getSymbol().equals("C")) {
			int bondsum=0;
			for(IBond bond : mol.bonds()){
				if(bond.getAtom(0)==atom || bond.getAtom(1)==atom)
				        bondsum+=bond.getOrder().ordinal();
			}
			atom.setImplicitHydrogenCount(4-bondsum);
		}
	}
  }


  /**
   * Adds explicit hydrogens to a molecule to satisfy valency rules (no layout done).
   * 
   * @param mol The molecule to saturate.
   * @throws CDKException
   */
  public static void addExplicitHydrogensToSatisfyValency(IAtomContainer mol) throws CDKException{
    addImplicitHydrogensToSatisfyValency(mol);
	AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
  }

  /**
   *  Adds and places hydrogens to a molecule. Also makes up/down bonds if there is 
   *  only one up/down bond and one h an a stereocenter.
   *
   * @param  container                   The molecule to saturate.
   * @exception  IOException             Description of Exception.
   * @exception  ClassNotFoundException  Description of Exception.
   * @exception  CDKException            Description of Exception
   */
  public static void addAndPlaceHydrogens(IAtomContainer container) throws IOException, ClassNotFoundException, CDKException {
	addExplicitHydrogensToSatisfyValency(container);
    for (int i = 0; i < container.getAtomCount(); i++) {
      IAtom a = container.getAtom(i);
      if (container.getConnectedAtomsList(a).size() == 4) {
        int up = 0;
        int down = 0;
        int hs = 0;
        IAtom h = null;
        for (int k = 0; k < 4; k++) {
          if (container.getBond(a, (IAtom)container.getConnectedAtomsList(a).get(k)).getStereo() == IBond.Stereo.UP) {
            up++;
          }
          if (container.getBond(a, (IAtom)container.getConnectedAtomsList(a).get(k)).getStereo() == IBond.Stereo.DOWN) {
            down++;
          }
          if (container.getBond(a, (IAtom)container.getConnectedAtomsList(a).get(k)).getStereo() == IBond.Stereo.NONE && ((IAtom)container.getConnectedAtomsList(a).get(k)).getSymbol().equals("H")) {
            h = (IAtom)container.getConnectedAtomsList(a).get(k);
            hs++;
          }
        }
        if (up == 0 && down == 1 && h != null && hs == 1) {
          container.getBond(a, h).setStereo(IBond.Stereo.UP);
        }
        if (up == 1 && down == 0 && h != null && hs == 1) {
          container.getBond(a, h).setStereo(IBond.Stereo.DOWN);
        }
      }
    }
    double bondLength = GeometryTools.getBondLengthAverage(container);
    new HydrogenPlacer().placeHydrogens2D(container, bondLength);
  }


  /**
   *  Sorts atoms like Cs-other heavy atoms-Hs.
   *
   * @param  mol            The molecule to rearrange.
   * @return                The new molecule as mdl file.
   */
  public static IAtomContainer rearangeAtoms(IAtomContainer mol) {
    Iterator<IAtom> atomsold = mol.atoms().iterator();
    IAtom[] atomsnew = new IAtom[mol.getAtomCount()];
    int k = 0;
    while(atomsold.hasNext()) {
      IAtom atom=atomsold.next();
      if (atom.getSymbol().equals("C")) {
        atomsnew[k++] = atom;
      }
    }
    atomsold = mol.atoms().iterator();
    while(atomsold.hasNext()) {
      IAtom atom=(IAtom)atomsold.next();
      if (!atom.getSymbol().equals("C") && !atom.getSymbol().equals("H")) {
        atomsnew[k++] = atom;
      }
    }
    atomsold = mol.atoms().iterator();
    while(atomsold.hasNext()) {
      IAtom atom=(IAtom)atomsold.next();
      if (atom.getSymbol().equals("H")) {
        atomsnew[k++] = atom;
      }
    }
    mol.setAtoms(atomsnew);
    return (mol);
  }

  /**
   * Converts in int into an IBond.Order value. 1 equals single, 2 equals double and so on.
   * 
   * @param order The value to translate.
   * @return The order value.
   */
  public static IBond.Order getOrderFromInt(int order){
	  return order==0 ? IBond.Order.ZERO : order==1 ? IBond.Order.SINGLE : order==2 ? IBond.Order.DOUBLE : order ==3 ? IBond.Order.TRIPLE : IBond.Order.QUADRUPLE;
	  
  }


  /**
   * Translates an int into an IBond.Stereo value. It is the reverse of IBond.Stero.ordinal(), so
   * getStereo(x).ordinal()==x. 
   * 
   * @param stereo The int value to translate
   * @return The stereo value.
   * @throws NmrshiftdbException There is no stereo representation for that int. 
   */
  public static IBond.Stereo getStereoFromInt(int stereo) throws NmrshiftdbException {
        if(stereo==IBond.Stereo.NONE.ordinal())
                return IBond.Stereo.NONE;
        else if(stereo==IBond.Stereo.UP.ordinal())
                return IBond.Stereo.UP;
        else if(stereo==IBond.Stereo.UP_INVERTED.ordinal())
                return IBond.Stereo.UP_INVERTED;
        else if(stereo==IBond.Stereo.DOWN.ordinal())
                return IBond.Stereo.DOWN;
        else if(stereo==IBond.Stereo.DOWN_INVERTED.ordinal())
                return IBond.Stereo.DOWN_INVERTED;
        else if(stereo==IBond.Stereo.UP_OR_DOWN.ordinal())
                return IBond.Stereo.UP_OR_DOWN;
        else if(stereo==IBond.Stereo.UP_OR_DOWN_INVERTED.ordinal())
            return IBond.Stereo.UP_OR_DOWN_INVERTED;
        else if(stereo==IBond.Stereo.E_OR_Z.ordinal())
            return IBond.Stereo.E_OR_Z;
        else if(stereo==IBond.Stereo.E.ordinal())
            return IBond.Stereo.E;
        else if(stereo==IBond.Stereo.Z.ordinal())
            return IBond.Stereo.Z;
        else if(stereo==IBond.Stereo.E_Z_BY_COORDINATES.ordinal())
            return IBond.Stereo.E_Z_BY_COORDINATES;
        else throw new NmrshiftdbException("No such stereo value: "+stereo); 
  }

  /**
   * Adds a multiplicity child to an atom or changes the existing, if there is one.
   * 
   * @param atom    The atom to change.
   * @param value The new multiplicity value.
   */
  public static void setChildElement(CMLAtom atom, String tagname, String value){
	  if(atom.getChildElements(tagname).size()>0){
		  atom.getChildElements(tagname).get(0).getAttribute("value").setValue(value);
	  }else{
        	Element el = new Element(tagname);
        	el.addAttribute(new Attribute("value", value));
        	atom.appendChild(el);		  
	  }
  }
  
  /**
   * Gets the value of the value attribute of a child named tagname of an atom, if none, ""
   * 
   * @param atom The atom.
   * @param tagname The tagname.
   * @return The value.
   */
  public static String getChildElement(CMLAtom atom, String tagname){
	  if(atom.getChildElements(tagname).size()>0 && atom.getChildElements(tagname).get(0).getAttribute("value")!=null){
		  return atom.getChildElements(tagname).get(0).getAttribute("value").getValue();
	  }else{
        	return "";		  
	  }
  }

  /**
   * Gets a +-concatenated list of the xvalues of the CMLPeaks an atom with identifier aX is assigned to. 
   * 
   * @param atom  The identifier.
   * @param peaks The peaks to search in.
   * @return The shifts.
   */
  public static String getValuesForAtom(int atom, CMLPeakList peaks) {
	    String result = "";
	    for (int i = 0; i < peaks.getPeakElements().size(); i++) {
	      CMLPeak thisTriple = peaks.getPeakElements().get(i);
	      if(thisTriple.getAtomRefs()!=null){
	          if (ArrayUtils.contains(thisTriple.getAtomRefs(),"a"+(atom+1))>=0) {
	            if (!result.equals("")) {
	              result += " + ";
	            }
	            if (thisTriple.getXValue() == -1) {
	              result += "H" + -1 + ":" + thisTriple.getXValue();
	            } else {
	              result += thisTriple.getXValue();
	            }
	          }
	      }
	    }
	    return (result);
	}


  	/**
	 * Gets an atom from a moleucle by ID (not atom number), null if none found. 
	 * 
	 * @param molWithH The molecule.
	 * @param string   The ID.
	 * @return The atom found or null.
	 */
	public static IAtom getAtomById(IAtomContainer molWithH, String string) {
	 for(int i=0;i<molWithH.getAtomCount();i++){
	  if(molWithH.getAtom(i).getID().equals(string))
		  return molWithH.getAtom(i);
	 }
	 return null;
	}
	
	/**
	 * Reads a molecule from smles and lays it out using cdk StructureDiagramGenerator.
	 * 
	 * @param smiles The smiles to use.
	 * @return The molecule.
	 * @throws CDKException
	 * @throws IOException 
	 */
	public static IAtomContainer readFromSmilesAndLayout(String smiles) throws CDKException, IOException{
		SMILESReader reader = new SMILESReader(new StringReader(smiles));
        AtomContainerSet som = (AtomContainerSet)reader.read(new AtomContainerSet());
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(som.getAtomContainer(0));
        FixBondOrdersTool dbst = new FixBondOrdersTool();
        IAtomContainer molecule = dbst.kekuliseAromaticRings(som.getAtomContainer(0));
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(molecule);
        sdg.generateCoordinates();
        reader.close();
        return sdg.getMolecule();
	}

	
	//Improved version of cdk BondTools.makeUpDownBonds
    public static void  makeUpDownBonds(IAtomContainer container){
	    for (int i = 0; i < container.getAtomCount(); i++) {
	        IAtom a = container.getAtom(i);
	        if (container.getConnectedAtomsList(a).size() == 4) {
	          int up = 0;
	          int down = 0;
	          int hs = 0;
	          IAtom h = null;
	          for (int k = 0; k < 4; k++) {
	        	  IAtom conAtom = container.getConnectedAtomsList(a).get(k);
	        	  IBond conBond = container.getBond(a,conAtom);
	        	  IBond.Stereo stereo = conBond.getStereo();
	        	  if (stereo == IBond.Stereo.UP && conBond.getAtom(1)==conAtom || stereo == IBond.Stereo.UP_INVERTED && conBond.getAtom(0)==conAtom) {
	        		  up++;
	        	  }
	        	  else if (stereo == IBond.Stereo.DOWN && conBond.getAtom(1)==conAtom || stereo == IBond.Stereo.DOWN_INVERTED && conBond.getAtom(0)==conAtom) {
	        		  down++;
	        	  }
	        	  else if (stereo == IBond.Stereo.NONE && conAtom.getSymbol().equals("H")) {
	        		  h = conAtom;
	        		  hs++;
	        	  } else {
	        		  h = null;
	        	  }
	          }
	          if (up == 0 && down == 1 && h != null && hs == 1) {
	            container.getBond(a, h).setStereo(IBond.Stereo.UP);
	          }
	          if (up == 1 && down == 0 && h != null && hs == 1) {
	            container.getBond(a, h).setStereo(IBond.Stereo.DOWN);
	          }
	        }
	      }
	}

    
    public static int isTetrahedral(IAtomContainer container, IAtom atom, boolean strict)
	{
		List<IAtom> atoms = container.getConnectedAtomsList(atom);
		if (atoms.size() != 4)
		{
			return (0);
		}
		java.util.List<IBond> bonds = container.getConnectedBondsList(atom);
        int up = 0;
		int down = 0;
        for (IBond bond : bonds) {
            if (bond.getStereo() == IBond.Stereo.NONE ||
            	bond.getStereo() == CDKConstants.UNSET) {
            } else
            if ((bond.getStereo() == IBond.Stereo.UP && bond.getAtom(0)==atom) || (bond.getStereo() == IBond.Stereo.UP_INVERTED && bond.getAtom(1)==atom)) {
                up++;
            } else
            if ((bond.getStereo() == IBond.Stereo.DOWN && bond.getAtom(0)==atom) || (bond.getStereo() == IBond.Stereo.DOWN_INVERTED && bond.getAtom(1)==atom)) {
                down++;
            }
        }
		if (up == 1 && down == 1)
		{
			return 1;
		}
		if (up == 2 && down == 2)
		{
			if (BondTools.stereosAreOpposite(container, atom))
			{
				return 2;
			}
			return 0;
		}
		if (up == 1 && down == 0 && !strict)
		{
			return 3;
		}
		if (down == 1 && up == 0 && !strict)
		{
			return 4;
		}
		if (down == 2 && up == 1 && !strict)
		{
			return 5;
		}
		if (down == 1 && up == 2 && !strict)
		{
			return 6;
		}
		return 0;
	}
    
    
    /**
     *  Tells if a certain bond is center of a valid double bond configuration.
     *  Copy of cdk BondTools method, with improvements
     *
     * @param  container  The atomcontainer.
     * @param  bond       The bond.
     * @param  sssr       The smallestSetOfSmallestRings of the container.
     * @return            true=is a potential configuration, false=is not.
     */
    public static boolean isValidDoubleBondConfiguration(IAtomContainer container, IBond bond, IRingSet sssr) {
    	if(bond.getFlag(CDKConstants.ISAROMATIC) || (sssr!=null && containsInRingSmallerThanEight(sssr, bond)))
    		return false;
      List<IAtom> connectedAtoms = container.getConnectedAtomsList(bond.getAtom(0));
      IAtom from = null;
        for (IAtom connectedAtom : connectedAtoms) {
            if (connectedAtom != bond.getAtom(1)) {
                from = connectedAtom;
            }
        }
      boolean[] array = new boolean[container.getBondCount()];
      for (int i = 0; i < array.length; i++) {
        array[i] = true;
      }
      if (isStartOfDoubleBond(container, bond.getAtom(0), from, array) && isEndOfDoubleBond(container, bond.getAtom(1), bond.getAtom(0), array) && !bond.getFlag(CDKConstants.ISAROMATIC)) {
        return (true);
      } else {
        return (false);
      }
    }

    
    /**
     * Tells if this ring set contains this bond in a ring of a size smaller eight atoms.
     * 
     * @param sssr The ring set.
     * @param bond The bond.
     * @return true=is in a ring smaller eight, false=is not.
     */
    private static boolean containsInRingSmallerThanEight(IRingSet sssr,
			IBond bond) {
		for(IAtomContainer ring : sssr.atomContainers()){
			if(ring.getAtomCount()<8 && ring.contains(bond))
				return true;
		}
		return false;
	}


	/**
     *  Says if an atom is the end of a double bond configuration
     *  Copy of cdk BondTools method, with improvements
     *
     * @param  atom                     The atom which is the end of configuration
     * @param  container                The atomContainer the atom is in
     * @param  parent                   The atom we came from
     * @param  doubleBondConfiguration  The array indicating where double bond
     *      configurations are specified (this method ensures that there is
     *      actually the possibility of a double bond configuration)
     * @return                          false=is not end of configuration, true=is
     */
    private static boolean isEndOfDoubleBond(IAtomContainer container, IAtom atom, IAtom parent, boolean[] doubleBondConfiguration) {
      if (container.getBondNumber(atom, parent) == -1 || doubleBondConfiguration.length <= container.getBondNumber(atom, parent) || !doubleBondConfiguration[container.getBondNumber(atom, parent)]) {
        return false;
      }

        int hcount;
        if (atom.getImplicitHydrogenCount() == CDKConstants.UNSET) hcount = 0;
        else hcount = atom.getImplicitHydrogenCount();

      int lengthAtom = container.getConnectedAtomsList(atom).size() + hcount;

         if (parent.getImplicitHydrogenCount() == CDKConstants.UNSET) hcount = 0;
        else hcount = parent.getImplicitHydrogenCount();

      int lengthParent = container.getConnectedAtomsList(parent).size() + hcount;
        
      if (container.getBond(atom, parent) != null) {
        if (container.getBond(atom, parent).getOrder() == Order.DOUBLE && (lengthAtom == 3 || (lengthAtom == 2 && atom.getSymbol().equals("N"))) && (lengthParent == 3 || (lengthParent == 2 && parent.getSymbol().equals("N")))) {
          List<IAtom> atoms = container.getConnectedAtomsList(atom);
          IAtom one = null;
          IAtom two = null;
            for (IAtom conAtom : atoms) {
                if (conAtom != parent && one == null) {
                    one = conAtom;
                } else if (conAtom != parent && one != null) {
                    two = conAtom;
                }
            }
          String[] morgannumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
          if ((one != null && two == null && atom.getSymbol().equals("N") && Math.abs(BondTools.giveAngleBothMethods(parent, atom, one, true)) > Math.PI / 10) || (!atom.getSymbol().equals("N") && one != null && two != null && !morgannumbers[container.getAtomNumber(one)].equals(morgannumbers[container.getAtomNumber(two)]))) {
            return (true);
          } else {
            return (false);
          }
        }
      }
      return (false);
    }


    /**
     *  Says if an atom is the start of a double bond configuration
     *  Copy of cdk BondTools method, with improvements
     *
     * @param  a                        The atom which is the start of configuration
     * @param  container                The atomContainer the atom is in
     * @param  parent                   The atom we came from
     * @param  doubleBondConfiguration  The array indicating where double bond
     *      configurations are specified (this method ensures that there is
     *      actually the possibility of a double bond configuration)
     * @return                          false=is not start of configuration, true=is
     */
    private static boolean isStartOfDoubleBond(IAtomContainer container, IAtom a, IAtom parent, boolean[] doubleBondConfiguration) {
        int hcount;
        if (a.getImplicitHydrogenCount() == CDKConstants.UNSET) hcount = 0;
        else hcount = a.getImplicitHydrogenCount();

      int lengthAtom = container.getConnectedAtomsList(a).size() + hcount;
        
      if (lengthAtom != 3 && (lengthAtom != 2 && !(a.getSymbol().equals("N")))) {
        return (false);
      }
      List<IAtom> atoms = container.getConnectedAtomsList(a);
      IAtom one = null;
      IAtom two = null;
      boolean doubleBond = false;
      IAtom nextAtom = null;
        for (IAtom atom : atoms) {
            if (atom != parent && container.getBond(atom, a).getOrder() == Order.DOUBLE && isEndOfDoubleBond(container, atom, a, doubleBondConfiguration)) {
                doubleBond = true;
                nextAtom = atom;
            }
            if (atom != nextAtom && one == null) {
                one = atom;
            } else if (atom != nextAtom && one != null) {
                two = atom;
            }
        }
      String[] morgannumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
      if (one != null && ((!a.getSymbol().equals("N") && two != null && !morgannumbers[container.getAtomNumber(one)].equals(morgannumbers[container.getAtomNumber(two)]) && doubleBond && doubleBondConfiguration[container.getBondNumber(a, nextAtom)]) || (doubleBond && a.getSymbol().equals("N") && Math.abs(BondTools.giveAngleBothMethods(nextAtom, a, parent, true)) > Math.PI / 10))) {
      	return (true);
      } else {
      	return (false);
      }
    }


	public static int getHcount(IAtomContainer mol, IAtom atom) {
		List<IAtom> atoms = mol.getConnectedAtomsList(atom);
		int NumberOfHs=0;
        for (int k = 0; k < atoms.size(); k++) {
          if (atoms.get(k).getSymbol().equals("H")) {
            NumberOfHs++;
          }
        }		
		return NumberOfHs;
	}
	

	/**
	 * Replaces all dative bonds=9 bonds with 6 in a mol file.
	 * 
	 * @param molfile The molfile.
	 * @return The molfile with replacements.
	 */
	public static String replace9by6inmolfile(String molfile){
	    return replace9by6inmolfile(molfile,false);
	}
	
	
	/**
 	 * Replaces all dative bonds=9 or search bonds=8 with 6 in a mol file (reverse==false) or the other way round (revers==true).
	 * 
	 * @param molfile The molfile.
	 * @param reverse false=9 becomes 6, true=6 becomes 9
	 * @return The molfile with replacements.
	 */
	public static String replace9by6inmolfile(String molfile, boolean reverse) {
	    char tosearch='8';
	    char tosearch2='9';
	    char replace='6';
	    if(reverse){
	      tosearch='6';
	      replace='8';
	    }
	    String[] lines = molfile.split("\r\n|\n");
		StringBuffer newmol=new StringBuffer();
		newmol.append(lines[0]+"\r\n");
		newmol.append(lines[1]+"\r\n");
		newmol.append(lines[2]+"\r\n");
		newmol.append(lines[3]+"\r\n");
		int numatoms=Integer.parseInt(lines[3].substring(0,3).trim());
		int numbonds=Integer.parseInt(lines[3].substring(3,6).trim());
		for(int i=0;i<numatoms;i++) {
			newmol.append(lines[4+i]+"\r\n");
		}
		for(int i=0;i<numbonds;i++) {
            String line=lines[4+numatoms+i];
            if(line.charAt(8)==tosearch || line.charAt(8)==tosearch2)
                    newmol.append(line.substring(0,8)+replace+line.substring(9)+"\r\n");
            else
                    newmol.append(line+"\r\n");
		}
		newmol.append(lines[4+numatoms+numbonds]+"\r\n");
		return newmol.toString();
	}
	
	/**
	 * Does a saturation with explict Hs following the rules in Clarke. If dbmol is set, it is done in the database as well. Removes hydrogens if necessary.
	 * 
	 * @param ac	The atomcontainer to saturate
	 * @param dbmol	null==do nothing, else save new atoms to db as well.
	 * @throws TorqueException
	 * @throws CDKException
	 * @throws DataSetException
	 */
	public static void doSimpleSaturation(IAtomContainer ac, DBMolecule dbmol) throws TorqueException, CDKException, DataSetException {
		ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
		for(IAtom atom : ac.atoms()) {
		  boolean doatom=false;
		  int valencenumber=0;
		  if(atom.getSymbol().equals("C")) {
			  doatom=true;
			  valencenumber=4;
		  }else if(atom.getSymbol().equals("N") || atom.getSymbol().equals("P")) {
			  doatom=true;
			  valencenumber=3;			  
		  }else if(atom.getSymbol().equals("O") || atom.getSymbol().equals("S")) {
			  doatom=true;
			  valencenumber=2;
		  }
		  if(doatom){
			  atom.setImplicitHydrogenCount(0);
			  int bondsum=0;
			  for(IBond bond : ac.getConnectedBondsList(atom)){
			          bondsum+=bond.getOrder().ordinal();
			  }
			  if(atom.getSymbol().equals("C"))
				  bondsum+=Math.abs(atom.getFormalCharge());
			  else
				  bondsum-=Math.abs(atom.getFormalCharge());
			  if(bondsum<valencenumber) {
				  for(int i=bondsum;i<valencenumber;i++) {
					  IAtom h=ac.getBuilder().newAtom();
					  h.setSymbol("H");
					  h.setProperty(SubmittingData.VISIBLE,"false");
					  h.setPoint2d(new Point2d(0,0));
					  h.setImplicitHydrogenCount(0);
					  ac.addAtom(h);
					  IBond b=ac.getBuilder().newBond();
					  b.setAtom(h, 0);
					  b.setAtom(atom, 1);
					  b.setOrder(IBond.Order.SINGLE);
					  ac.addBond(b);
					  if(dbmol!=null) {
	                                     if (hcg == null) {
					        hcg = new HOSECodeGenerator();
                                             }
                                             String sql="select max(ATOM_NUMBER) from ATOM where MOLECULE_ID="+dbmol.getMoleculeId();
                                             int newatomnumber=DBIdentifierPeer.executeQuery(sql).get(0).getValue(1).asInt()+1;
                                             String hc=null;
                                             String ehc=null;
                                             try {
                                            	 hc=hcg.getHOSECode(ac, h, 6);
                                                 ehc=ehcg.getHOSECode(ac, h, 6);
                                             }catch(Exception ex) {
                                            	 //ignore
                                             }
					     DBAtom dbatom=new DBAtom(h, ac, dbmol.getMoleculeId(), hc, ehc, false, newatomnumber, 0);
	                      dbatom.save();
	                      DBBond bond=new DBBond(b,false);
	                      bond.save();
	                      DBBondDBAtom bondatom=new DBBondDBAtom();
	                      bondatom.setAtomId(dbatom.getAtomId());
	                      bondatom.setBondId(bond.getBondId());
	                      bondatom.setOrderNumber(0);
	                      bondatom.save();
	                      bondatom=new DBBondDBAtom();
	                      bondatom.setAtomId(dbmol.getAtomsAsArrayInMdlOrder()[ac.getAtomNumber(atom)].getAtomId());
	                      bondatom.setBondId(bond.getBondId());
	                      bondatom.setOrderNumber(1);
	                      bondatom.save();
					  }
				  }
			  }
                          if(bondsum>valencenumber) {
				  int htoremove=bondsum-valencenumber;
				  List<IAtom> connatoms = ac.getConnectedAtomsList(atom);
				  for(IAtom connatom : connatoms) {
                      if(connatom.getSymbol().equals("H") && (dbmol==null || dbmol.getAtomAt(ac.getAtomNumber(connatom)).getDBSignalDBAtoms().size()==0) && htoremove>0){
                    	  if(dbmol!=null) {
	                          DBAtom dbatom=dbmol.getAtomAt(ac.getAtomNumber(connatom));
	                          Vector<DBBond> bondsfromh=dbatom.getBonds();
	                          for(DBBond bondlocal : bondsfromh){
	                                  DBBondDBAtomPeer.delete(bondlocal,null);
	                                  bondlocal.delete(null);
	                          }
	                          Criteria crit = new Criteria();
	                          crit.add(DBIdentifierPeer.ATOM_ID, dbatom.getAtomId());
	                          DBIdentifierPeer.doDelete(crit);
	                          DBAtomPeer.doDelete(crit);
                    	  }
                          ac.removeAtomAndConnectedElectronContainers(connatom);
                          htoremove--;
					  }
				  }
			  }
		  }
		}
	    double bondLength = GeometryTools.getBondLengthAverage(ac);
	    new HydrogenPlacer().placeHydrogens2D(ac, bondLength);
	}
	
	
	/**
	 * Tells if there is any atom in the molecule from the list in NmrshiftdbConstants.METALS.
	 * 
	 * @param ac The molecule.
	 * @return true=one or more metals are there, falso=none.
	 */
	public static boolean hasOrganometall(IAtomContainer ac) {
	    for(IAtom atom : ac.atoms()) {
                if(NmrshiftdbConstants.METALS.contains(atom.getSymbol())) {
	    		return true;
	    	}
	    }
	    return false;
	}
	
	/**
	 * Tells if there is a zero bond somewhere in the molecule.
	 * 
	 * @param ac The molecule.
	 * @return true/false.
	 */
	public static boolean hasZeroBond(IAtomContainer ac) {
		for(IBond bond : ac.bonds()) {
		    if(bond.getOrder()==IBond.Order.ZERO) {
		        return true;
		    }
		}
		return false;
	}
	
	/**
	 * Tells if a compound is organometallic. Organometallics are defined by having at least one zero bond and a metal from the list in NmrshiftdbConstants.
	 * 
	 * @param ac The molecule to check.
	 * @return True=is organometallic, false=is not.
	 */
	public static boolean isOrganometallic(IAtomContainer ac) {
	    //distinguish organometalics
	    boolean haszerobond=hasOrganometall(ac);
	    boolean hasmetal=hasZeroBond(ac);
	    return haszerobond && hasmetal;
	}
	
	public static void makeOrganometallicEquivalences(IAtomContainer molh, List<Integer> equivalences) throws CDKException {
        AllRingsFinder arf = new AllRingsFinder();
        IRingSet rings=arf.findAllRings(molh);
        IAtom metal=null;
        for(IAtom atom : molh.atoms()) {
                if(NmrshiftdbConstants.METALS.contains(atom.getSymbol())) {
                        metal=atom;
                        break;
                }
        }
        Iterator<IAtomContainer> itr = rings.atomContainers().iterator();
        while(itr.hasNext()){
        	if(itr.next().contains(metal))
        		itr.remove();
        }
        int ringcount=1;
        for(IAtom atom : molh.getConnectedAtomsList(metal)) {
            for(IAtomContainer ring : rings.atomContainers()) {
                if(ring.getAtomCount()%2==1 && ring.contains(atom) && ring.getAtomNumber(atom)==0) {
                    for(IAtom atom2 : ring.atoms()){
                        atom2.setProperty("equivalence",ringcount);
                    }
                    equivalences.add(ringcount);
                    ringcount++;
                }
            }
        }
	}

	/**
	 * This sets bonds and atoms to aromatic in rings which are connected to the metal centre and either have equal number of single and double bonds or have one single bond more. No cloning.
	 * 
	 * @param molh The molecule to deal with.
	 * @throws CDKException
	 */
	public static void doSimpleAromaticyForOrganometallicEquivalences(IAtomContainer molh) throws CDKException {
        AllRingsFinder arf = new AllRingsFinder();
        IRingSet rings=arf.findAllRings(molh);
        IAtom metal=null;
        for(IAtom atom : molh.atoms()) {
                if(NmrshiftdbConstants.METALS.contains(atom.getSymbol())) {
                        metal=atom;
                        break;
                }
        }
        Iterator<IAtomContainer> itr = rings.atomContainers().iterator();
        while(itr.hasNext()){
        	if(itr.next().contains(metal))
        		itr.remove();
        }
        int ringcount=1;
        for(IAtom atom : molh.getConnectedAtomsList(metal)) {
            for(IAtomContainer ring : rings.atomContainers()) {
            	if(ring.contains(atom) && ring.getAtomNumber(atom)==0){
	            	int doublebondcount=0;
	            	for(IBond bond : ring.bonds()) {
	            		if(bond.getOrder()==IBond.Order.DOUBLE)
	            			doublebondcount++;
	            	}
	                if(doublebondcount==ring.getBondCount()/2d || doublebondcount==ring.getBondCount()/2) {
	                	for(IAtom atom2 : ring.atoms()) {
	                		atom2.setIsAromatic(true);
	                		atom2.setFlag(CDKConstants.ISAROMATIC,true);
	                	}
	                	for(IBond bond2 : ring.bonds()) {
	                		bond2.setIsAromatic(true);
	                		bond2.setFlag(CDKConstants.ISAROMATIC,true);
	                	}
	                }
            	}
            }
        }
	}
	
	/**
	 * This replaces all single bonds from a metal centre (as defined in nmrshiftdbconstants) with a zero bond. No cloning.
	 * 
	 * @param molh The molecule.
	 * @throws CDKException
	 * @throws CloneNotSupportedException
	 */
	public static void doReplacement(IAtomContainer molh) throws CDKException, CloneNotSupportedException {
		AllRingsFinder arf = new AllRingsFinder();
        IRingSet rings=arf.findAllRings(molh);
        IAtom metal=null;
        for(IAtom atom : molh.atoms()) {
                if(NmrshiftdbConstants.METALS.contains(atom.getSymbol())) {
                        metal=atom;
                        break;
                }
        }
        Iterator<IAtomContainer> itr = rings.atomContainers().iterator();
        while(itr.hasNext()){
        	if(itr.next().contains(metal))
        		itr.remove();
        }
        int ringcount=1;
        for(IAtom atom : molh.getConnectedAtomsList(metal)) {
            for(IAtomContainer ring : rings.atomContainers()) {
            	if(ring.contains(atom) && ring.getAtomNumber(atom)==0){
                        for(IAtom atom2 : ring.atoms()) {
                		for(IBond bond2 : molh.getConnectedBondsList(atom2)) {
                			if((bond2.getAtom(0)==metal || bond2.getAtom(1)==metal) && bond2.getOrder()==IBond.Order.SINGLE) {
                				bond2.setOrder(IBond.Order.ZERO);
                				break;
                			}
                		}
                	}
                	for(IBond bond2 : ring.bonds()) {
                		if(bond2.getOrder()==IBond.Order.DOUBLE)
                			bond2.setOrder(IBond.Order.SINGLE);
                	}
            	}
            }
        }
	}


	/**
	 * Clones an atom container, removes Hs and set implicit h counter from those removed.
	 * 
	 * @param mol The molecule with explicit Hs.
	 * @return The molecule with implicit Hs.
	 * @throws CDKException
	 * @throws CloneNotSupportedException
	 */
	public static IAtomContainer convertImplicitToExplicitHydrogens(IAtomContainer mol)  throws CDKException, CloneNotSupportedException  {
		IAtomContainer withimplicit=(IAtomContainer)mol.clone();
		for(IAtom atom : withimplicit.atoms()) {
			if(atom.getImplicitHydrogenCount()==null)
				atom.setImplicitHydrogenCount(0);
		}
		List<IAtom> toremove=new ArrayList<>();
		for(IAtom atom : withimplicit.atoms()) {
			if(atom.getSymbol().equals("H")) {
				withimplicit.getConnectedAtomsList(atom).get(0).setImplicitHydrogenCount(withimplicit.getConnectedAtomsList(atom).get(0).getImplicitHydrogenCount()+1);
				toremove.add(atom);
			}
		}
		for(IAtom atom : toremove)
			withimplicit.removeAtomAndConnectedElectronContainers(atom);
		return withimplicit;
	}
	
	/**
	 * Reads a file as either V2000 or V3000 mol file. In case of V3000, dative bonds (9) become type 6. 
	 * 
	 * @param mdl The file content.
	 * @return The result.
	 * @throws CDKException
	 * @throws IOException
	 */
	public static IAtomContainer readMolBothVersions(String mdl) throws CDKException, IOException {
            DefaultChemObjectReader mdlreader2=null;
            try {
	        if(mdl.indexOf("V3000")>-1) {
	          mdlreader2 = new MDLV3000Reader(new StringReader(mdl));
	        }else {
	          mdl = AtomUtils.replace9by6inmolfile(mdl);
	          mdlreader2 = new MDLV2000Reader(new StringReader(mdl));
	        }
	        return (IAtomContainer) mdlreader2.read(new org.openscience.cdk.AtomContainer());
		}
            finally{
        	mdlreader2.close();
            }
	}
	
	
	/**
	 * Tells the number (starting from 0) of an atom in an atomcontainer.
	 * 
	 * @param mol The molecule.
	 * @param atom The atom.
	 * @return number or -1 if not found.
	 */
	public static int getAtomNumberInCdkMol(IAtomContainer mol, IAtom atom) {
		int i=0;
		for(IAtom atomloop :  mol.atoms()) {
			if(atomloop==atom)
				return i;
			i++;
		}
		return -1;
	}
	
	
	/**
	 * Created a shallow copy which has all Hs removed. This is irrespective of 
	 * wedge bonds etc. (as opposed to AtomContainerManipulator.removeHydrogens), 
	 * which may leave some hydrogens.
	 * 
	 * @param ac
	 * @return a shallow copy of ac without Hs.
	 */
	public static IAtomContainer removeHydrogens(IAtomContainer ac) throws CloneNotSupportedException {
	   ac=ac.clone();
       List<IAtom> toremove=new ArrayList<>();
       for(IAtom atom : ac.atoms()) {
           if(atom.getSymbol().equals("H"))
               toremove.add(atom);
       }
       for(IAtom atom : toremove) {
           ac.removeAtomAndConnectedElectronContainers(atom);
       }
       return ac;
	}
}

