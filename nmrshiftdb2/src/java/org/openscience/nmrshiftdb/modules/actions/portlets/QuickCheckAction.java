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
package org.openscience.nmrshiftdb.modules.actions.portlets;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.security.LoginException;
import org.apache.jetspeed.services.security.turbine.TurbineAuthentication;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.Assignment;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.spectrum.assignments.AtomReference;
import org.jcamp.spectrum.assignments.TwoAtomsReference;
import org.jcamp.spectrum.notes.Note;
import org.jcamp.spectrum.notes.NoteDescriptor;
import org.openscience.cdk.Bond;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.graph.invariant.Canon;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBQuickCheck;
import org.openscience.nmrshiftdb.om.DBQuickCheckPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.JcampToCMLSpectrumMapper;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.ReportUtil;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ValuesForVelocityComparator;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.unikoeln.chemie.nmr.data.NMR2DSpectrum;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.Peak2D;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import nu.xom.Builder;
import nu.xom.Element;

/**
 * This is the portlet handling Quick Checks.
 * 
 * @author Stefan Kuhn
 * @created 18. Februar 2002
 */
public class QuickCheckAction extends VelocityPortletAction {

    /* (non-Javadoc)
     * @see org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction#buildNormalContext(org.apache.jetspeed.portal.portlets.VelocityPortlet, org.apache.velocity.context.Context, org.apache.turbine.util.RunData)
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
        fillContext(context, data);
    }

    /**
     * Gets the content to be displayed in the portlet as HTML
     * 
     * @param runData Created by turbine
     * @return The content to be displayed as the portlet
     * @since
     */
    private void fillContext(Context context, RunData runData) {
        try {
            if (context.get("messages")==null) {
                runData.getSession().removeAttribute("subDataQCc");
                runData.getSession().removeAttribute("subDataQCh");
            }
            SubmittingData subDataC = (SubmittingData) runData.getSession().getAttribute("subDataQCc");
            SubmittingData subDataH = (SubmittingData) runData.getSession().getAttribute("subDataQCh");
            //Create new SubmittingData object in case there is none in the session, i. e. no quick check is currently going on
            if (subDataC == null){
                subDataC = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
                subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
                runData.getSession().setAttribute("subDataQCc", subDataC);
            }
            if (subDataH == null){
                subDataH = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
                subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
                runData.getSession().setAttribute("subDataQCh", subDataH);
            }
            if(context.get("atoms")==null){
                List<Integer> atoms=new ArrayList<Integer>();
                for(int i=0;i<20;i++){
                	atoms.add(i);
                }
                context.put("atoms",  atoms);
                context.put("show2d", false);
            }
            if(subDataC.getMolOriginal()!=null){
               context.put("showoldinput", "true");
               if(GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("JCP")){
                   //The mol is put into the MView applet via a file
                   File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/" + System.currentTimeMillis() + "submit.mol"));
                   FileWriter out = new FileWriter(outputFile);
                   out.write(subDataC.getInputMolAsMdl(null,null,true));
                   out.close();
                   context.put("molfile", "nmrshiftdbhtml/" + outputFile.getName());
               }else{
                   context.put("molfile", GeneralUtils.replace(subDataC.getInputMolAsMdl(null,null,true),"\n","\\n"));
               }
            }
            context.put("subdatac",subDataC);
            context.put("subdatah",subDataH);
            context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",runData));
            //we might already have set showatomnumbers, if not, set it to true
            if(context.get("showatomnumbers")==null)
            	context.put("showatomnumbers","true");
            context.put("session",runData.getSession());
            if(runData.getUser().hasLoggedIn()){
                Criteria crit=new Criteria();
                crit.add(DBQuickCheckPeer.USER_ID, ((BaseJetspeedUser) runData.getUser()).getUserId());
                crit.addDescendingOrderByColumn(DBQuickCheckPeer.DATE);
                List<DBQuickCheck> quickchecks=DBQuickCheckPeer.doSelect(crit);
                context.put("quickchecks",  quickchecks);
            }
            Criteria crit=new Criteria();
            crit.add(DBConditionTypePeer.CONDITION_TYPE_ID,2);
            context.put("condition", DBConditionTypePeer.doSelect(crit).get(0));
        } catch (Throwable ex) {
            context.put("error",GeneralUtils.logError(ex,"predict",runData,true));
        }
    }
    
    /**
     * This action deletes a quick check.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doDeletequickcheck(RunData runData, Context context){
        try{
            if(runData.getParameters().getString("confirm", "false").equals("true")){
                DBQuickCheckPeer.doDelete(new NumberKey(runData.getParameters().get("quickcheckid")));
            }
        } catch (Throwable ex) {
            context.put("error",GeneralUtils.logError(ex,"predict",runData,true));
        }            
    }
    
    /**
     * This action does a quick check.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doSubmitquickcheck(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              context.put("messages", messages);
              //we look if there is an mnova or other file
              String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
              File mnovafile=new File(relativepath+"/uploadmnova"+runData.getSession().getId());
              File topspinfile=new File(relativepath+"/uploadtopspin"+runData.getSession().getId());
              File nmredatafile=new File(relativepath+"/uploadnmredata"+runData.getSession().getId());
              if(runData.getParameters().getString("MolTxt").equals("") && !mnovafile.exists() && !topspinfile.exists() && !nmredatafile.exists()){
            	  messages.append("You did not enter a structure!<br>");
              }
              SubmittingData subDataC = (SubmittingData) runData.getSession().getAttribute("subDataQCc");
              SubmittingData subDataH = (SubmittingData) runData.getSession().getAttribute("subDataQCh");
              int result = -1;
              if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("JCP") || session.getAttribute("applet") !=null && session.getAttribute("applet").equals("applet")){
                  result = subDataC.setMol(runData.getParameters().getString("MolTxt"), false, false, false);
                  if(subDataC.getMolWithH()!=null){
                      boolean[] isDoubleBondSpecified = new boolean[subDataC.getMolWithH().getBondCount()];
                      StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                      DBMolecule oldMol = GeneralUtils.getExistingMolecule(subDataC.getMolWithHCount(), subDataC.getMolWithH(), isDoubleBondSpecified, runData.getServletConfig(), subDataC.getInputMolAsMdl(null,null,false), ssb);
                      subDataC.setSmilesNonChiralProv(ssb.string1);
                      subDataC.setSmilesChiralProv(ssb.string2);
                      subDataC.setIsChiralProv(ssb.bool);
                      if (oldMol != null) {
                          subDataC.setExistingMoleculeId(oldMol.getMoleculeId());
                        oldMol.addValuesToSubmittingData(subDataC,null,false,false);
                      }
                  }
              }else{
                  FileItem[] fi = runData.getParameters().getFileItems("datei");
                  if(fi!=null){
                    GeneralUtils.processFileItem(fi[0],messages,runData,"moltxt");
                    IAtomContainer mol=(IAtomContainer)runData.getSession().getAttribute("moltxt");
                    if(mol!= null && mol.getAtomCount()>0){
                        result = subDataC.setMol(mol, false, false, false);
                    }
                  }
                  runData.getSession().removeAttribute("moltxt");
              }
              if(result==1 && !mnovafile.exists() && !topspinfile.exists() && !nmredatafile.exists()){
                  messages.append("You did not enter a structure!<br>");
              }else if(result!=1){
                  IAtomContainer mol = subDataC.getMolWithH();
                  boolean allhaveidentifiers=true;
                  for (int i = 0; i < mol.getAtomCount(); i++) {
                	  if(subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE)!=null && subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE).equals("true") && subDataC.getIdentifiers().get(i)!=null){
                		  allhaveidentifiers=false;
                	  }
                      if(mol.getAtom(i) instanceof PseudoAtom){
                          messages.append("You included a pseudo atom in your structure - pseudo atoms are not possible!<br>");
                          return;
                      }
                      if (mol.getAtom(i).getSymbol().equals("C")) {
                          try{
                              AtomUtils.setChildElement(subDataC.getCMLMolecule().getAtoms().get(i), SubmittingData.MULTIPLICITY, GeneralUtils.getMultiplicity(mol.getAtom(i), mol, false));
                          }catch(Exception ex){
                              //we get npes here for unclear reasons. a bit dirty like this, but no time to do it better
                          }
                      }
                  }
                  if(!allhaveidentifiers)
                	  context.put("showatomnumbers","false");
              }
              Map<Integer, ValuesForVelocityBean> carbons=new HashMap<Integer,ValuesForVelocityBean>();
              Map<Integer, ValuesForVelocityBean> hydrogens=new HashMap<Integer,ValuesForVelocityBean>();
              List<Integer> atoms=new ArrayList<Integer>();
              List<Double> predictionsC=new ArrayList<Double>();
              List<Double> predictionsH=new ArrayList<Double>();
              List<Integer> noncarbonatoms=new ArrayList<Integer>();
              //if we have a structure we can do the actual quick check
              if(subDataC.getMolWithH()!=null && !mnovafile.exists() && !topspinfile.exists() && !nmredatafile.exists()){
                  subDataH.setMol(runData.getParameters().getString("MolTxt"), false, false, false);
                  boolean[] isDoubleBondSpecified = new boolean[subDataH.getMolWithH().getBondCount()];
                  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                  DBMolecule oldMol = GeneralUtils.getExistingMolecule(subDataH.getMolWithHCount(), subDataH.getMolWithH(), isDoubleBondSpecified, runData.getServletConfig(), subDataH.getInputMolAsMdl(null,null,false), ssb);
                  subDataH.setSmilesNonChiralProv(ssb.string1);
                  subDataH.setSmilesChiralProv(ssb.string2);
                  subDataH.setIsChiralProv(ssb.bool);
                  if (oldMol != null) {
                      subDataH.setExistingMoleculeId(oldMol.getMoleculeId());
                    oldMol.addValuesToSubmittingData(subDataH,null,false,false);
                  }
                  GeneralUtils.addToStructureHistory(subDataC.getInputMolAsMdl(null,null,false), runData);
                  context.put("existingmol", oldMol);
                  IAtomContainer molWithH = subDataC.getMolWithH();
                  IAtomContainer molAsEntered = subDataC.getMolOriginal();
                  long[] morgan=Canon.symmetry(molAsEntered, GraphUtil.toAdjList(molAsEntered));
                  subDataC.setSignalstable(new CMLPeakList());
                  subDataH.setSignalstable(new CMLPeakList());
                  try{
                      Isotopes.getInstance().configureAtoms(molAsEntered);
                  }catch(Exception ex){
                      messages.append("Unspecific problem with structure input!<br>");
                  }
          		  DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
        		  format.applyPattern("#####0.00");
                  Map<Integer, double[]> cPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> cPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  Map<Integer, double[]> hPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> hPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  //Parse the shifts from the textfield
                  String  spectrum = GeneralUtils.replace(runData.getParameters().getString("shiftsc"), " ", "");
                  if(spectrum.startsWith("Shiftsforautoassignment"))
                      spectrum="";
                  CMLPeakList cShiftsToAssign=new CMLPeakList(); 
                  try{
                      cShiftsToAssign = ParseUtils.parseSpectrum(spectrum,null);
                  }catch(Exception ex){
                      messages.append("It seems your carbon shifts are not in the required format!<br>");
                      context.put("ctoassign", runData.getParameters().getString("shiftsc"));
                  }
                  //we look if there are jcamp files
                  File jcampcfile=new File(relativepath+"/uploadjcampc"+runData.getSession().getId());
                  if(jcampcfile.exists()){
                	  try{
	                      if(runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)).booleanValue()==true)
	                          cShiftsToAssign = GeneralUtils.parseJcamp(FileUtils.readFileToString(jcampcfile), JCAMPReader.STRICT);
	                      else
	                          cShiftsToAssign = GeneralUtils.parseJcamp(FileUtils.readFileToString(jcampcfile), JCAMPReader.RELAXED);
	                      jcampcfile.delete();
                	  }catch(Exception ex){
                		  messages.append("Problems reading your jcamp-dx file: "+ex.getMessage()+" - check if it is a correct file !<br>");
                	  }
                  }
                  SpectrumUtils.scaleIntensities(cShiftsToAssign.getPeakElements());
                  spectrum = GeneralUtils.replace(runData.getParameters().getString("shiftsh"), " ", "");
                  if(spectrum.startsWith("Shiftsforautoassignment"))
                      spectrum="";
                  //We remember shifts with + in front in this map
                  Map<Double,Double> diastereotopichs=new HashMap<Double,Double>();
                  CMLPeakList hShiftsToAssign = new CMLPeakList();
                  try{
                      hShiftsToAssign = ParseUtils.parseSpectrum(spectrum, diastereotopichs);
                  }catch(Exception ex){
                      messages.append("It seems your hydrogen shifts are not in the required format!<br>");
                      context.put("htoassign", runData.getParameters().getString("shiftsh"));
                  }
                  File jcamphfile=new File(relativepath+"/uploadjcamph"+runData.getSession().getId());
                  if(jcamphfile.exists()){
                      if(runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)).booleanValue()==true)
                          hShiftsToAssign = GeneralUtils.parseJcamp(FileUtils.readFileToString(jcamphfile), JCAMPReader.STRICT);
                      else
                          hShiftsToAssign = GeneralUtils.parseJcamp(FileUtils.readFileToString(jcamphfile), JCAMPReader.RELAXED);
                      jcamphfile.delete();
                  }
                  SpectrumUtils.scaleIntensities(hShiftsToAssign.getPeakElements());
                  //We prepare two pdf reports
                  Document documentc = new Document();
                  OutputStream baosc = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcc"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documentc, baosc);
                  documentc.open();
                  Document documenth = new Document();
                  OutputStream baosh = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qch"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documenth, baosh);
                  documenth.open();
                  //we keep track of paired hdrogends
                  Map<Integer,Integer> hpairs=new HashMap<Integer,Integer>();
                  int counts[]=analyzequickcheck(runData, context, carbons, hydrogens, cPredictionsForAutoassignment, cPredictionsForAutoassignmentPeaks,  hPredictionsForAutoassignment, hPredictionsForAutoassignmentPeaks, diastereotopicHPredictionsForAutoassignmentPeaks, cShiftsToAssign, hShiftsToAssign, hpairs, noncarbonatoms, atoms, morgan, false, predictionsC, predictionsH, messages);
                  //We deal with shifts to assign, if any
                  StringBuffer unusedStringc = new StringBuffer();
                  if (cShiftsToAssign.getPeakElements().size()>0) {
                      GeneralUtils.removeDuplicates(cShiftsToAssign);
                      Collections.sort(cPredictionsForAutoassignmentPeaks, new SignalComparator(false));
                      List<CMLPeak> cPeaksToAssign = cShiftsToAssign.getPeakChildren();
                      Collections.sort(cPeaksToAssign, new SignalComparator(false));
                      Map<Double, Double> predictionsMap=null;
                      if (cPredictionsForAutoassignmentPeaks.size() >= cPeaksToAssign.size()) {
                          predictionsMap = GeneralUtils.getMatchingSearchShift(cPredictionsForAutoassignmentPeaks, cPeaksToAssign, 100, new Vector<Double>(), true);
                      } else {
                          Map<Double, Double> mapWrongWayRound = GeneralUtils.getMatchingSearchShift(cPeaksToAssign, cPredictionsForAutoassignmentPeaks, 100, new Vector<Double>(), false);
                          predictionsMap = new HashMap<Double, Double>();
                          for (Double value : mapWrongWayRound.keySet()) {
                              predictionsMap.put(mapWrongWayRound.get(value), value);
                          }
                      }
                      for(Double prediction : predictionsMap.keySet()){
                          for(int atomid : cPredictionsForAutoassignment.keySet()){	                          
                              if(Math.abs(cPredictionsForAutoassignment.get(atomid)[1]-prediction)<.0001){
                                  CMLPeak peak = new CMLPeak();
                                  peak.setXValue(predictionsMap.get(new Double(cPredictionsForAutoassignment.get(atomid)[1])));
                                  peak.setYValue(0);
                                  for(CMLPeak peakoriginal : cShiftsToAssign.getPeakElements()){
                                	  if(peak.getXValue()==peakoriginal.getXValue()){
                                		  peak.setPeakMultiplicity(peakoriginal.getPeakMultiplicity());
                                		  break;
                                	  }
                                  }
                                  peak.setAtomRefs(new String[]{molWithH.getAtom(atomid).getID()});
                                  subDataC.addToSignalstable(peak);
                                  double difference = Math.abs((double) peak.getXValue() - cPredictionsForAutoassignment.get(atomid)[1]);
                                  cPredictionsForAutoassignment.get(atomid)[3] = difference;
                              }
                          }
                      }
                      for (int i = 0; i < cShiftsToAssign.getPeakChildren().size(); i++) {
                          if (!predictionsMap.containsValue(new Double(cShiftsToAssign.getPeakChildren().get(i).getXValue())))
                              unusedStringc.append(cShiftsToAssign.getPeakChildren().get(i).getXValue() + (cShiftsToAssign.getPeakChildren().get(i).getPeakMultiplicity()!=null ? cShiftsToAssign.getPeakChildren().get(i).getPeakMultiplicity() : "") + "\n");
                      }
                      context.put("ctoassign", unusedStringc);
                  }
                  StringBuffer unusedStringh = new StringBuffer();
                  if (hShiftsToAssign.getPeakElements().size()>0) {
                      GeneralUtils.removeDuplicates(hShiftsToAssign);
                      //We deal with diastereotopic shifts first
                      CMLPeakList diastereotopicHShiftsToAssign=new CMLPeakList();
                      Map<Double,Double> replacedshifts=new HashMap<Double,Double>();
                      for(CMLPeak peak : hShiftsToAssign.getPeakChildren()){
                          //if a shift is for a diastereotopic h we use the average for the prediction
                          if(diastereotopichs.containsKey(peak.getXValue())){
                              double newshift=(peak.getXValue()+diastereotopichs.get(peak.getXValue()))/2;
                              replacedshifts.put(newshift, peak.getXValue());
                              peak.setXValue(newshift);
                              diastereotopicHShiftsToAssign.addPeak(peak);
                          }
                      }
                      Map<Double, Double> predictionsMap=null;
                      if(diastereotopicHShiftsToAssign.getPeakChildren().size()>0){
                          //we assign diastereotopic shifts to CH2 groups only
                          Collections.sort(diastereotopicHPredictionsForAutoassignmentPeaks, new SignalComparator(false));
                          List<CMLPeak> diastereotopicHPeaksToAssign = diastereotopicHShiftsToAssign.getPeakChildren();
                          Collections.sort(diastereotopicHPeaksToAssign, new SignalComparator(false));
                          if (diastereotopicHPredictionsForAutoassignmentPeaks.size() >= diastereotopicHPeaksToAssign.size()) {
                              predictionsMap = GeneralUtils.getMatchingSearchShift(diastereotopicHPredictionsForAutoassignmentPeaks, diastereotopicHPeaksToAssign, 100, new Vector<Double>(), true);
                          } else {
                              messages.append("You have "+diastereotopicHPeaksToAssign.size()+" shift pairs to assign, but only "+diastereotopicHPredictionsForAutoassignmentPeaks.size()+" CH<sub>2</sub> atoms are in the molecule. Pleasee correct!");
                              context.put("htoassign", runData.getParameters().getString("shiftsh"));
                              return;
                          }
                          for(CMLPeak peak : diastereotopicHPredictionsForAutoassignmentPeaks){
                              if(!predictionsMap.containsKey(peak.getXValue()))
                                  hPredictionsForAutoassignmentPeaks.add(peak);
                          }
                          for(Double prediction : predictionsMap.keySet()){
                              for(int atomid : hPredictionsForAutoassignment.keySet()){                           
                                  if(hPredictionsForAutoassignment.get(atomid)!=null && Math.abs(hPredictionsForAutoassignment.get(atomid)[1]-prediction)<.0001 && hpairs.containsKey(atomid)){
                                      //the predicted value was for diastereotopic, we put both inputs into the assignments for the different Hs
                                      CMLPeak peak = new CMLPeak();
                                      peak.setXValue(replacedshifts.get(predictionsMap.get(new Double(hPredictionsForAutoassignment.get(atomid)[1]))));
                                      peak.setYValue(0);
                                      peak.setAtomRefs(new String[]{molWithH.getAtom(atomid).getID()});
                                      subDataH.addToSignalstable(peak);
                                      CMLPeak peak2 = new CMLPeak();
                                      peak2.setXValue(diastereotopichs.get(peak.getXValue()));
                                      peak2.setYValue(0);
                                      peak2.setAtomRefs(new String[]{molWithH.getAtom(hpairs.get(atomid)).getID()});
                                      subDataH.addToSignalstable(peak2);
                                      double difference = Math.abs((peak.getXValue()+peak2.getXValue())/2 - hPredictionsForAutoassignment.get(atomid)[1]);
                                      hPredictionsForAutoassignment.get(atomid)[3] = difference;
                                  }
                              }
                          }
                      }else if(diastereotopicHPredictionsForAutoassignmentPeaks.size()>0){
                          for(CMLPeak peak : diastereotopicHPredictionsForAutoassignmentPeaks){
                              hPredictionsForAutoassignmentPeaks.add(peak);
                          }
                      }
                      //now we do the remaining assignments
                      Collections.sort(hPredictionsForAutoassignmentPeaks, new SignalComparator(false));
                      List<CMLPeak> hPeaksToAssign = hShiftsToAssign.getPeakChildren();
                      Collections.sort(hPeaksToAssign, new SignalComparator(false));
                      if (hPredictionsForAutoassignment.size() >= hPeaksToAssign.size()) {
                          predictionsMap = GeneralUtils.getMatchingSearchShift(hPredictionsForAutoassignmentPeaks, hPeaksToAssign, 100, new Vector<Double>(), true);
                      } else {
                          Map<Double, Double> mapWrongWayRound = GeneralUtils.getMatchingSearchShift(hPeaksToAssign, hPredictionsForAutoassignmentPeaks, 100, new Vector<Double>(), false);
                          predictionsMap = new HashMap<Double, Double>();
                          for (Double value : mapWrongWayRound.keySet()) {
                              predictionsMap.put(mapWrongWayRound.get(value), value);
                          }
                      }
                      for(Double prediction : predictionsMap.keySet()){
                          for(int atomid : hPredictionsForAutoassignment.keySet()){                           
                              if(Math.abs(hPredictionsForAutoassignment.get(atomid)[1]-prediction)<.0001){
                                  CMLPeak peak = new CMLPeak();
                                  //if the predicted value was for diastereotopic, we put both inputs into the assignments for the different Hs
                                  if(replacedshifts.get(predictionsMap.get(new Double(hPredictionsForAutoassignment.get(atomid)[1])))!=null){
                                      peak.setXValue(replacedshifts.get(predictionsMap.get(new Double(hPredictionsForAutoassignment.get(atomid)[1]))));
                                      CMLPeak peak2 = new CMLPeak();
                                      peak2.setXValue(diastereotopichs.get(peak.getXValue()));
                                      peak2.setYValue(0);
                                      peak2.setAtomRefs(new String[]{molWithH.getAtom(hpairs.get(atomid)).getID()});
                                      subDataH.addToSignalstable(peak2);
                                      double difference = Math.abs((peak.getXValue()+peak2.getXValue())/2 - hPredictionsForAutoassignment.get(atomid)[1]);
                                      hPredictionsForAutoassignment.get(atomid)[3] = difference;
                                  }else{
                                      peak.setXValue(predictionsMap.get(new Double(hPredictionsForAutoassignment.get(atomid)[1])));
                                      double difference = Math.abs((double) peak.getXValue() - hPredictionsForAutoassignment.get(atomid)[1]);
                                      hPredictionsForAutoassignment.get(atomid)[3] = difference;
                                  }
                                  peak.setYValue(0);
                                  peak.setAtomRefs(new String[]{molWithH.getAtom(atomid).getID()});
                                  subDataH.addToSignalstable(peak);
                              }
                          }
                      }
                      for (int i = 0; i < hShiftsToAssign.getPeakChildren().size(); i++) {
                          if (!predictionsMap.containsValue(new Double(hShiftsToAssign.getPeakChildren().get(i).getXValue())))
                              unusedStringh.append(hShiftsToAssign.getPeakChildren().get(i).getXValue() + "\n");
                      }
                      context.put("htoassign", unusedStringh);
                  }
                  //if this is null, no overall report is done
                  Document reportpdf=null;
                  PdfPTable table2d =null;
                  double[] result2d=null;
                  //read 2d
                  if(runData.getParameters().get("enter2d")!=null && runData.getParameters().get("enter2d").equals("enter2d")) {
                	  //this means a 2d was submitted
                	  IAtomContainer mol=subDataC.getMolWithH();
                	  IAtomContainer molOriginal=subDataC.getMolOriginal();
                	  int i=1;
                	  boolean entertocsy=runData.getParameters().getString("entertocsy","false").equals("true");
                	  boolean enternoesy=runData.getParameters().getString("enternoesy","false").equals("true");
                	  if(entertocsy)
                		  context.put("showtocsy", "true");
                	  if(enternoesy)
                		  context.put("shownoesy", "true");
                	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
                	  Map<Color,List<IBond>> cosycouplings=new HashMap<>();
                	  cosycouplings.put(Color.BLUE, new ArrayList<IBond>());
                	  Map<Color,List<IBond>> tocsycouplings=new HashMap<>();
                	  tocsycouplings.put(Color.BLUE, new ArrayList<IBond>());
                	  Map<Color,List<IBond>> hmbccouplings=new HashMap<>();
                	  hmbccouplings.put(Color.BLUE, new ArrayList<IBond>());
                	  for(CMLPeak peak1 : subDataH.getSignalstableOrderedAscending()) {
                		  int k=1;
                    	  for(CMLPeak peak2 : subDataH.getSignalstableOrderedAscending()) {
                    		  if(k>i) {
    								IAtom atom1cdk=AtomUtils.getAtomById(mol,((CMLPeak)peak1).getAtomRefs()[0]);
    	          				  	if(atom1cdk.getSymbol().equals("H") && mol.getAtomNumber(atom1cdk)<molOriginal.getAtomCount())
    	          				  		atom1cdk=molOriginal.getAtom(mol.getAtomNumber(atom1cdk));
    	          					else if(mol.getAtomNumber(mol.getConnectedAtomsList(atom1cdk).get(0))<molOriginal.getAtomCount())
    	          						atom1cdk=molOriginal.getAtom(mol.getAtomNumber(mol.getConnectedAtomsList(atom1cdk).get(0)));
    	          					else
    	          						atom1cdk=null;
    								IAtom atom2cdk=AtomUtils.getAtomById(mol,((CMLPeak)peak2).getAtomRefs()[0]);
    	          				  	if(atom2cdk.getSymbol().equals("H") && mol.getAtomNumber(atom2cdk)<molOriginal.getAtomCount())
    	          				  		atom2cdk=molOriginal.getAtom(mol.getAtomNumber(atom2cdk));
    	          					else if(mol.getAtomNumber(mol.getConnectedAtomsList(atom2cdk).get(0))<molOriginal.getAtomCount())
    	          						atom2cdk=molOriginal.getAtom(mol.getAtomNumber(mol.getConnectedAtomsList(atom2cdk).get(0)));
    	          					else
    	          						atom2cdk=null;
    	          				  	if(atom1cdk!=null && atom2cdk!=null) {
	                    			  boolean cosy=runData.getParameters().getString("cosy"+i+""+k,"false").equals("true");
	                    			  if(cosy) {
	                    				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
	                    				  cosypeaks.add(peak);
	                    				  cosycouplings.get(Color.BLUE).add(new Bond(atom1cdk, atom2cdk));
	                    			  }
	                    			  if(entertocsy) {
	                    				  boolean tocsy=runData.getParameters().getString("tocsy"+i+""+k,"false").equals("true");
	                        			  if(tocsy) {
	                        				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
	                        				  tocsypeaks.add(peak);
	                        				  tocsycouplings.get(Color.BLUE).add(new Bond(atom1cdk, atom2cdk));
	                        			  }
	                    			  }
	                    			  if(enternoesy) {
	                    				  boolean noesy=runData.getParameters().getString("noesy"+i+""+k,"false").equals("true");
	                        			  if(noesy) {
	                        				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
	                        				  noesypeaks.add(peak);
	                        			  }
	                    			  }
    	          				  	}
                    		  }
                    		  k++;
                    	  }            		  
                		  k=1;
                    	  for(CMLPeak peak2 : subDataC.getSignalstableOrderedAscending()) {
                   			  boolean hsqc=runData.getParameters().getString("hsqc"+k+""+i,"false").equals("true");
                    		  if(hsqc) {
                    			Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                 				hsqcpeaks.add(peak);
                   			  }
                   			  boolean hmbc=runData.getParameters().getString("hmbc"+k+""+i,"false").equals("true");
                    		  if(hmbc) {
                    			Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                 				hmbcpeaks.add(peak);
  								IAtom atom1cdk=AtomUtils.getAtomById(mol,((CMLPeak)peak1).getAtomRefs()[0]);
	          				  	if(atom1cdk.getSymbol().equals("H") && mol.getAtomNumber(atom1cdk)<molOriginal.getAtomCount())
	          				  		atom1cdk=molOriginal.getAtom(mol.getAtomNumber(atom1cdk));
	          					else if(mol.getAtomNumber(mol.getConnectedAtomsList(atom1cdk).get(0))<molOriginal.getAtomCount())
	          						atom1cdk=molOriginal.getAtom(mol.getAtomNumber(mol.getConnectedAtomsList(atom1cdk).get(0)));
	          					else
	          						atom1cdk=null;
								IAtom atom2cdk=AtomUtils.getAtomById(mol,((CMLPeak)peak2).getAtomRefs()[0]);
	          				  	if(atom2cdk.getSymbol().equals("H") && mol.getAtomNumber(atom2cdk)<molOriginal.getAtomCount())
	          				  		atom2cdk=molOriginal.getAtom(mol.getAtomNumber(atom2cdk));
	          					else if(mol.getAtomNumber(mol.getConnectedAtomsList(atom2cdk).get(0))<molOriginal.getAtomCount())
	          						atom2cdk=molOriginal.getAtom(mol.getAtomNumber(mol.getConnectedAtomsList(atom2cdk).get(0)));
	          					else
	          						atom2cdk=null;
	          				  	if(atom1cdk!=null && atom2cdk!=null) {
	          				  		hmbccouplings.get(Color.BLUE).add(new Bond(atom1cdk, atom2cdk));
	          				  	}
                   			  }
                    		  k++;
                    	  }            		  
                		  i++;
                	  }
                	  context.put("cosypeaks",  cosypeaks);
                	  runData.getSession().setAttribute("cosyqc", cosypeaks);
        			  if(entertocsy) {
        				  context.put("tocsypeaks",  tocsypeaks);
        				  runData.getSession().setAttribute("tocsyqc", tocsypeaks);
        			  }else {
        				  context.put("tocsypeaks",  new ArrayList<Peak2D>());
        			  }
        			  if(enternoesy) {
        				  runData.getSession().setAttribute("noesyqc", noesypeaks);
        				  context.put("noesypeaks",  noesypeaks);
        			  }else {
        				  context.put("noesypeaks",  new ArrayList<Peak2D>());
        			  }
                	  context.put("hmbcpeaks",  hmbcpeaks);
                	  runData.getSession().setAttribute("hmbcqc", hmbcpeaks);
                	  context.put("hsqcpeaks",  hsqcpeaks);
                	  runData.getSession().setAttribute("hsqcqc", hsqcpeaks);
                	  //We start the overall report
                  	  reportpdf=new Document();
                      OutputStream baosall = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcall"+runData.getSession().getId()+".pdf")));
                      PdfWriter.getInstance(reportpdf, baosall);
                      reportpdf.open();
                      List<String> types=new ArrayList<>();
                      List<String> frequencies=new ArrayList<>();
                      List<String> temperatures=new ArrayList<>();
                      List<String> comments=new ArrayList<>();
                      String frequency = subDataC.getFrequency().equals("unknown")|| subDataC.getFrequency().equals("") ? "?" : subDataC.getFrequency();
                      String temperature=subDataC.getTemperature().equals("unknown") || subDataC.getTemperature().equals("")? "?" : subDataC.getTemperature();
                      if(tocsypeaks.size()>0) {
                    	  types.add("TOCSY");
                    	  frequencies.add(frequency);
                    	  temperatures.add(temperature);
                    	  comments.add("");
                      }
                      if(cosypeaks.size()>0) {
                    	  types.add("COSY");
                    	  frequencies.add(frequency);
                    	  temperatures.add(temperature);
                    	  comments.add("");
                      }
                      if(hmbcpeaks.size()>0) {
                    	  types.add("HMBC");
                    	  frequencies.add(frequency);
                    	  temperatures.add(temperature);
                    	  comments.add("");
                      }
                      ReportUtil.startFullReport(reportpdf, runData.getServletConfig(), molOriginal, mol, subDataC.getSolvent().getCurrentValue(), subDataC.getSmilesNonChiralProv(), temperatures, types, frequencies, comments, null, null, null, null, 0);
                      PdfPTable table = new PdfPTable(2);
            	      table2d = new PdfPTable(2);
            		  ReportUtil.make2d(runData.getServletConfig(), "HMBC", molOriginal, mol, subDataH, subDataC, table, table2d, hmbccouplings, hmbcpeaks);
            		  ReportUtil.make2d(runData.getServletConfig(), "TOCSY", molOriginal, mol, subDataH, subDataC, table, table2d, tocsycouplings, tocsypeaks);
            		  ReportUtil.make2d(runData.getServletConfig(), "COSY", molOriginal, mol, subDataH, subDataC, table, table2d, cosycouplings, cosypeaks);
            		  //We build the 2d report
                      Document document2d = new Document();
                      OutputStream baos2d = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qc2d"+runData.getSession().getId()+".pdf")));
                      PdfWriter.getInstance(document2d, baos2d);
                      document2d.open();
                      result2d=SpectrumUtils.calculateScore2d(molWithH, subDataC, subDataH, cosypeaks, tocsypeaks, noesypeaks, hmbcpeaks, hsqcpeaks, document2d, runData.getServletConfig(), entertocsy, enternoesy);
                      int overallMark2d=(int)result2d[0];
                      String report2d=SpectrumUtils.makeJudgement(overallMark2d, -1, true);
                      context.put("report2d", report2d);
                      document2d.close();                  
                      reportpdf.add(table);
                      reportpdf.add(new Paragraph(new Chunk("   ")));
                      PdfPTable tableouterprediction = new PdfPTable(2);
                      tableouterprediction.setWidthPercentage(100);
                      tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                      tableouterprediction.setWidths(new float[]{1, 1});
                      PdfPTable tablegreenred = new PdfPTable(2);
                      tablegreenred.setWidthPercentage(100);
                  }else{
                	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
                	  int i=1;
                	  for(CMLPeak peak1 : subDataH.getSignalstableOrderedAscending()) {
                		  int k=1;
                    	  for(CMLPeak peak2 : subDataH.getSignalstableOrderedAscending()) {
                    		  if(k>i) {
                    			  if(subDataH.isValidCrosspeak("cosy", peak1, peak2)) {
                    				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                    				  cosypeaks.add(peak);                        				  
                    			  }
                    			  if(subDataH.isValidCrosspeak("tocsy", peak1, peak2)) {
                    				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                    				  tocsypeaks.add(peak);
                    			  }
                    			  if(subDataH.isValidCrosspeak("noesy", peak1, peak2)) {
                       				  Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                       				  noesypeaks.add(peak);
                    			  }                    		  }
                    		  k++;
                    	  }            		  
                		  k=1;
                    	  for(CMLPeak peak2 : subDataC.getSignalstableOrderedAscending()) {
                			  if(subDataH.isValidCrosspeak("hsqc", peak1, peak2)) {
                    			Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                 				hsqcpeaks.add(peak);
                   			  }
                			  if(subDataH.isValidCrosspeak("hmbc", peak1, peak2)) {
                    			Peak2D peak=new Peak2D(peak1.getXValue(), peak2.getXValue(), 0);
                 				hmbcpeaks.add(peak);
                   			  }
                    		  k++;
                    	  }            		  
                		  i++;
                	  }
                	  context.put("submitagainmessage", "Check your crosspeaks and transfer again to get a quality report!");
                	  context.put("cosypeaks",  cosypeaks);
                	  runData.getSession().setAttribute("cosyqc", cosypeaks);
       				  context.put("tocsypeaks",  tocsypeaks);
       				  runData.getSession().setAttribute("tocsyqc", tocsypeaks);
    				  context.put("noesypeaks",  noesypeaks);
    				  runData.getSession().setAttribute("noesyqc", noesypeaks);
                	  context.put("hmbcpeaks",  hmbcpeaks);
                	  runData.getSession().setAttribute("hmbcqc", hmbcpeaks);
                	  context.put("hsqcpeaks",  hsqcpeaks);
                	  runData.getSession().setAttribute("hsqcqc", hsqcpeaks);
                  }
                  PdfPTable tableouterprediction = new PdfPTable(2);
                  tableouterprediction.setWidthPercentage(100);
                  tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  tableouterprediction.setWidths(new float[]{1, 1});
                  PdfPTable tablegreenred = new PdfPTable(2);
                  tablegreenred.setWidthPercentage(100);
                  PdfPTable table1d = new PdfPTable(2);
                  table1d.setWidthPercentage(100);
                  table1d.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  table1d.setWidths(new float[]{1, 1});
                  List<com.lowagie.text.Element> footnotes=new ArrayList<com.lowagie.text.Element>();
	              //We build the c report
                  double[] resultC=ReportUtil.start1d(runData.getServletConfig(), "13","C", GeneralUtils.getPredictionLimits("13C"),subDataC, carbons, documentc, morgan, table1d, tableouterprediction, runData.getRequest(), footnotes);
                  int overallMarkC=(int)resultC[0];
                  String reportc=SpectrumUtils.makeJudgement(overallMarkC,resultC[5],false);
                  context.put("reportc", SpectrumUtils.makeJudgement(overallMarkC,resultC[5],true));
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), reportc, tablegreenred, false);
                  //We build the h report
        		  double[] resultH=ReportUtil.start1d(runData.getServletConfig(), "1", "H", GeneralUtils.getPredictionLimits("1H"), subDataH, hydrogens, documenth, morgan, table1d, tableouterprediction, runData.getRequest(), null);
                  int overallMarkH=(int)resultH[0];
                  String reporth=SpectrumUtils.makeJudgement(overallMarkH,resultH[5],false);
                  documentc.close();
                  documenth.close();
                  context.put("reporth", SpectrumUtils.makeJudgement(overallMarkH,resultH[5],true));
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), reporth, tablegreenred, false);
                  if(reportpdf!=null) {
	                  reportpdf.add(table1d);
	                  ReportUtil.endReport(runData.getServletConfig(), reportpdf, null, null, null, null, null, tableouterprediction, tablegreenred, table2d, (int)result2d[0], footnotes);
	                  reportpdf.close();
                  }
                  if(runData.getUser().hasLoggedIn() && !runData.getParameters().getString("quickcheckid").equals("")){
                      DBQuickCheck quickCheck=DBQuickCheckPeer.retrieveByNameAndUser(runData.getParameters().getString("quickcheckid"),Integer.parseInt(((BaseJetspeedUser) runData.getUser()).getUserId()));
                      if(quickCheck==null){
                          quickCheck=new DBQuickCheck();
                          quickCheck.setName(runData.getParameters().getString("quickcheckid"));
                          quickCheck.setUserId(Integer.parseInt(((BaseJetspeedUser) runData.getUser()).getUserId()));
                      }
                      quickCheck.setDate(new Date());
                      context.put("quickcheckid",quickCheck.getName());
                      StringBuffer cml=new StringBuffer();
                      cml.append("<quickcheck>"+subDataC.data.toXML()+subDataH.data.toXML());
                      if(session.getAttribute("cosyqc")!=null) {
                      	write2d("cosyqc", cml, session);
                      }
                      if(session.getAttribute("tocsyqc")!=null) {
                      	write2d("tocsyqc", cml, session);
                      }
                      if(session.getAttribute("noesyqc")!=null) {
                      	write2d("noesyqc", cml, session);
                      }
                      if(session.getAttribute("hsqcqc")!=null) {
                       	write2d("hsqcqc", cml, session);
                      }
                      if(session.getAttribute("hmbcqc")!=null) {
                       	write2d("hmbcqc", cml, session);
                      }
                      cml.append("<reportc>"+overallMarkC+"</reportc><reporth>"+overallMarkH+"</reporth></quickcheck>");
                      quickCheck.setCml(cml.toString());
                      quickCheck.save();
                  }
                  if(subDataC.getSignalstable().getPeakChildren().size()+StringUtils.countMatches(unusedStringc.toString(), "\n")!=counts[1])
                	  context.put("ccountwarning","<br>There are "+counts[0]+" different carbons, but "+(subDataC.getSignalstable().getPeakChildren().size()+StringUtils.countMatches(unusedStringc.toString(),"\n"))+" shifts are given!<br>");
                  if(subDataH.getSignalstable().getPeakChildren().size()+StringUtils.countMatches(unusedStringh.toString(), "\n")!=counts[1])
                	  context.put("hcountwarning","<br>There are "+counts[1]+" different hydrogens, but "+(subDataH.getSignalstable().getPeakChildren().size()+StringUtils.countMatches(unusedStringh.toString(), "\n"))+" shifts are given!<br>");
                  GeneralUtils.logToSql("QuickCheck - " + subDataC.getInputMolAsMdl(null, null,false), runData);
              }else if(mnovafile.exists()){
                  //an MNova file has been uploaded
            	  //we need new submittingdata objects and 2d peak lists
            	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
                  subDataC = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
                  subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
                  runData.getSession().setAttribute("subDataQCc", subDataC);
                  subDataH = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
                  subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
                  runData.getSession().setAttribute("subDataQCh", subDataH);
            	  context.put("cosypeaks",  cosypeaks);
            	  runData.getSession().setAttribute("cosyqc", cosypeaks);
   				  context.put("tocsypeaks",  tocsypeaks);
   				  runData.getSession().setAttribute("tocsyqc", tocsypeaks);
				  context.put("noesypeaks",  noesypeaks);
				  runData.getSession().setAttribute("noesyqc", noesypeaks);
            	  context.put("hmbcpeaks",  hmbcpeaks);
            	  runData.getSession().setAttribute("hmbcqc", hmbcpeaks);
            	  context.put("hsqcpeaks",  hsqcpeaks);
            	  runData.getSession().setAttribute("hsqcqc", hsqcpeaks);
            	  GeneralUtils.readMnovafile(mnovafile, subDataC, subDataH, messages, cosypeaks, tocsypeaks, hmbcpeaks, hsqcpeaks, noesypeaks);
            	  mnovafile.delete();
            	  //We start the overall report
              	  Document reportpdf=new Document();
                  OutputStream baosall = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcall"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(reportpdf, baosall);
                  reportpdf.open();
                  List<String> types=new ArrayList<>();
                  List<String> frequencies=new ArrayList<>();
                  List<String> temperatures=new ArrayList<>();
                  List<String> comments=new ArrayList<>();
                  String frequency = subDataH.getFrequency();
                  String temperature=subDataH.getTemperature();
                  for(int i=0;i<6;i++){
                	  if(i==0)
                		  types.add("1H");
                	  else if(i==1)
                		  types.add("13C");
                	  else if(i==2)
                		  types.add("H,H-sel. TOCSY\"");
                	  else if(i==3)
                		  types.add("H,H-sel. TOCSY");
                	  else if(i==4)
                		  types.add("H,H-NOESY");
                	  else if(i==5)
                		  types.add("H,C-HMBC");
                  	  frequencies.add(frequency);
                  	  temperatures.add(temperature);
                  	  comments.add("");
                  }                     
                  ReportUtil.startFullReport(reportpdf, runData.getServletConfig(), subDataC.getMolOriginal(), subDataC.getMolWithH(), subDataC.getSolvent().getCurrentValue(), subDataC.getSmilesNonChiralProv(), temperatures, types, frequencies, comments, null, null, null, null, 0);
                  PdfPTable table = new PdfPTable(2);
        	      PdfPTable table2d = new PdfPTable(2);
        	      for(int i=0;i<4;i++){
        	    	  List<Peak2D> peaks;
        	    	  String spectrumtype;
        	    	  if(i==0) {
        	    		  spectrumtype="H,C-HMBC";
        	    		  peaks=hmbcpeaks;
        	    	  } else if(i==1) {
        	    		  spectrumtype="H,H-NOESY";
        	    		  peaks=noesypeaks;
        	    	  } else if(i==2) {
        	    		  spectrumtype="H,H-sel. TOCSY";
        	    		  peaks=tocsypeaks;
        	    	  }else {
        	    		  spectrumtype="H,H-sel. TOCSY";
        	    		  peaks=cosypeaks;
        	    	  }
            		  Map<Color,List<IBond>> couplings=new HashMap<>();
            		  couplings.put(Color.BLUE, new ArrayList<IBond>());
            		  IAtomContainer cdkmol=subDataH.getMolWithH();
            		  IAtomContainer cdkmolorig=subDataH.getMolOriginal();
               		  for (Peak2D peak2d : peaks) {
								IAtom atom1cdk=AtomUtils.getAtomById(cdkmol,peak2d.getAtoms1List().get(0));
	          				  	if(atom1cdk.getSymbol().equals("H"))
	          				  		atom1cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom1cdk).get(0)));
	          					else
	          						atom1cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(atom1cdk));
								IAtom atom2cdk=AtomUtils.getAtomById(cdkmol,peak2d.getAtoms2List().get(0));
	          				  	if(atom2cdk.getSymbol().equals("H"))
	          				  		atom2cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom2cdk).get(0)));
	          					else
	          						atom2cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(atom2cdk));
	    						couplings.get(Color.BLUE).add(new Bond(atom1cdk, atom2cdk));
               		  }
               		  ReportUtil.make2d(runData.getServletConfig(), spectrumtype, cdkmolorig, cdkmol, subDataH, subDataC, table, table2d, couplings, peaks);
                  }
        	      reportpdf.add(table);
                  reportpdf.add(new Paragraph(new Chunk("   ")));
                  DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
          		  format.applyPattern("#####0.00");
                  PdfPTable tableouterprediction = new PdfPTable(2);
                  tableouterprediction.setWidthPercentage(100);
                  tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  tableouterprediction.setWidths(new float[]{1, 1});
                  PdfPTable tablegreenred = new PdfPTable(2);
                  tablegreenred.setWidthPercentage(100);
                  Map<Integer, double[]> cPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> cPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  Map<Integer, double[]> hPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> hPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  CMLPeakList cShiftsToAssign=new CMLPeakList(); 
                  CMLPeakList hShiftsToAssign=new CMLPeakList();
                  Map<Integer,Integer> hpairs=new HashMap<Integer,Integer>();
                  long[] morgan=Canon.symmetry(subDataC.getMolOriginal(), GraphUtil.toAdjList(subDataC.getMolOriginal()));
                  analyzequickcheck(runData, context, carbons, hydrogens, cPredictionsForAutoassignment, cPredictionsForAutoassignmentPeaks, hPredictionsForAutoassignment, hPredictionsForAutoassignmentPeaks, diastereotopicHPredictionsForAutoassignmentPeaks, cShiftsToAssign, hShiftsToAssign, hpairs, noncarbonatoms, atoms, morgan, true, predictionsC, predictionsH, messages);
                  //We prepare two pdf reports
                  Document documentc = new Document();
                  OutputStream baosc = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcc"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documentc, baosc);
                  documentc.open();
                  Document documenth = new Document();
                  OutputStream baosh = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qch"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documenth, baosh);
                  documenth.open();
                  PdfPTable table1d = new PdfPTable(2);
                  table1d.setWidthPercentage(100);
                  table1d.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  table1d.setWidths(new float[]{1, 1});
                  List<com.lowagie.text.Element> footnotes=new ArrayList<com.lowagie.text.Element>();
	              //We build the c report
                  double[] resultC=SpectrumUtils.calculateScore(subDataC.getMolWithH(), subDataC.getMolOriginal(), subDataC, carbons, GeneralUtils.getPredictionLimits("13C"), "", documentc, runData.getServletConfig(), morgan, new UrlTool().getBaseUrl(runData.getRequest()), null, null, null, null, null, null, new StringBuffer(), footnotes);
                  int overallMarkC=(int)resultC[0];
                  String reportc=SpectrumUtils.makeJudgement(overallMarkC,resultC[5],true);
                  context.put("reportc", reportc);
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), SpectrumUtils.makeJudgement(overallMarkC,resultC[5],false), tablegreenred, false);
                  //We build the h report
                  double[] resultH=SpectrumUtils.calculateScore(subDataH.getMolWithH(), subDataC.getMolOriginal(), subDataH, hydrogens, GeneralUtils.getPredictionLimits("1H"), "", documenth, runData.getServletConfig(), morgan, new UrlTool().getBaseUrl(runData.getRequest()), null, null, null, null, null, null, new StringBuffer(), null);
                  int overallMarkH=(int)resultH[0];
                  String reporth=SpectrumUtils.makeJudgement(overallMarkH,resultH[5],true);
                  documentc.close();
                  documenth.close();
                  context.put("reporth", reporth);
                  boolean allhaveidentifiers=true;
                  for(int i=0;i<subDataC.getMolWithH().getAtomCount();i++){
                	  if(subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE)!=null && subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE).equals("true") && (subDataC.getIdentifiers().get(i)==null || subDataC.getIdentifiers().get(i).equals(""))){
                		  allhaveidentifiers=false;
                		  break;
                	  }
                  }
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), SpectrumUtils.makeJudgement(overallMarkH,resultH[5],false), tablegreenred, false);
                  reportpdf.add(table1d);
                  //Build 2d report
                  Document document2d = new Document();
                  OutputStream baos2d = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qc2d"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(document2d, baos2d);
                  document2d.open();
                  double[] result2d=SpectrumUtils.calculateScore2d(subDataC.getMolWithH(), subDataC, subDataH, cosypeaks, tocsypeaks, noesypeaks, hmbcpeaks, hsqcpeaks, document2d, runData.getServletConfig(), false, true);
                  int overallMark2d=(int)result2d[0];
                  String report2d=SpectrumUtils.makeJudgement(overallMark2d, -1, true);
                  context.put("report2d", report2d);
                  document2d.close();
                  ReportUtil.endReport(runData.getServletConfig(), reportpdf, null, null, null, null, null, tableouterprediction, tablegreenred, table2d, (int)result2d[0], footnotes);
                  reportpdf.close();
                  if(allhaveidentifiers)
                	  context.put("showatomnumbers","false");
                  else
                	  context.put("showatomnumbers","true");
                  GeneralUtils.logToSql("QuickCheck - " + subDataC.getInputMolAsMdl(null, null,false), runData);
              }else if(nmredatafile.exists()){
                  //an nmredata file has been uploaded
                  NmredataReader reader = new NmredataReader(new FileInputStream(nmredatafile));
                  NmreData data = null;
                  try {
                	  data=reader.read();
                  }catch(Exception ex) {
                	  messages.append("Looks like there is a problem with your file: "+ex.getMessage()+" - please check!");
                  }
                  if(data!=null && data.getMolecule().getAtomCount()>0 && data.getMolecule().getAtom(0).getPoint2d()==null && data.getMolecule().getAtom(0).getPoint3d()==null) {
                	  data=null;
                	  messages.append("There are no coordiantes in the file - we cannot handle that!");
                  }
                  nmredatafile.delete();
                  if(data!=null) {
                	  //We unify 1d and 2d shifts
                	  NMRSpectrum spectrum1h=null;
                	  NMRSpectrum spectrum13c=null;
                      for(Spectrum spectrum : data.getSpectra()){
                    	  if(spectrum instanceof NMRSpectrum && ((NMRSpectrum)spectrum).getNucleus().equals("1H"))
                    		  spectrum1h=(NMRSpectrum)spectrum;
                    	  if(spectrum instanceof NMRSpectrum && ((NMRSpectrum)spectrum).getNucleus().equals("13C"))
                    		  spectrum13c=(NMRSpectrum)spectrum;
                      }
                      for(Spectrum spectrum : data.getSpectra()){
                    	  if(spectrum instanceof NMR2DSpectrum){
                    		  int k=0;
                    		  for(Peak2D peak : ((NMR2DSpectrum) spectrum).getPeakTable()) {
                    			  if(((NMR2DSpectrum)spectrum).getAssignments()!=null && ((NMR2DSpectrum)spectrum).getAssignments()[k]!=null) {
                    				  for(int l=0;l<2;l++) {
                    					  double originalpeak=peak.getFirstShift();
                    					  String nucleus=((NMR2DSpectrum) spectrum).getXNucleus();
                    					  int atomnumber=((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber1()[0];
                    					  if(l==1) {
                    						  nucleus=((NMR2DSpectrum) spectrum).getYNucleus();
                    						  atomnumber=((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber2()[0];
                    						  originalpeak=peak.getSecondShift();
                    					  }
	                    				  double peak1d=getPeak(nucleus.equals("1H") ? spectrum1h : spectrum13c, atomnumber, originalpeak);
	                    				  if(l==0) {
		                    				  if(peak1d!=Double.MIN_VALUE && peak1d!=peak.getFirstShift()) {
		                    					  peak.setFirstShift(peak1d);
		                    				  }
	                    				  }else {
		                    				  if(peak1d!=Double.MIN_VALUE && peak1d!=peak.getSecondShift()) {
		                    					  peak.setSecondShift(peak1d);
		                    				  }	                    					  
	                    				  }
                    				  }
                    			  }
                    			  k++;
                    		  }
                    	  }
                      }                	  
                	  //We start the overall report
                  	  Document reportpdf=new Document();
                      OutputStream baosall = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcall"+runData.getSession().getId()+".pdf")));
                      PdfWriter.getInstance(reportpdf, baosall);
                      reportpdf.open();
                      List<String> types=new ArrayList<>();
                      List<String> frequencies=new ArrayList<>();
                      List<String> temperatures=new ArrayList<>();
                      List<String> comments=new ArrayList<>();
                      String frequency = Double.toString(((NMRSpectrum)data.getSpectra().get(0)).getFrequency());
                      String temperature=Double.toString(data.getTemperature());
                      for(Spectrum spectrum : data.getSpectra()){
                	  		if(spectrum instanceof NMRSpectrum){
                	  			types.add(((NMRSpectrum)spectrum).getNucleus());
                	  		}else{
                    	  		NoteDescriptor noteDescriptor=new NoteDescriptor("CorType");
                    	  		if(spectrum.getNotes(noteDescriptor)!=null) {
                    	  			String type=(String)((Note)spectrum.getNotes(noteDescriptor).get(0)).getValue();
                	  				types.add(type);
                    	  		} else {
                	  				types.add("unknowns");
                    	  		}
                	  		}
                      	  frequencies.add(frequency);
                      	  temperatures.add(temperature);
                      	  comments.add("");
                      }                     
                      ReportUtil.startFullReport(reportpdf, runData.getServletConfig(), data.getMoleculeOriginal(), data.getMolecule(), data.getSolvent(), data.getSmiles(), temperatures, types, frequencies, comments, null, null, null, null, 0);
                	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
                	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
	                  subDataC = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	                  subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
	                  runData.getSession().setAttribute("subDataQCc", subDataC);
	                  subDataH = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	                  subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
	                  runData.getSession().setAttribute("subDataQCh", subDataH);
	                  PdfPTable table = new PdfPTable(2);
	        	      PdfPTable table2d = new PdfPTable(2);
	                  for(Spectrum spectrum : data.getSpectra()) {
	                	  SubmittingData subData=null;
	                	  StringBuffer unusedString = new StringBuffer();
	                	  if(spectrum instanceof NMRSpectrum && (((NMRSpectrum)spectrum).getNucleus().equals("13C") || ((NMRSpectrum)spectrum).getNucleus().equals("C"))) {
	                          subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	                          subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
	                          subDataC=subData;
	                          runData.getSession().setAttribute("subDataQCc", subDataC);
	                          context.put("ctoassign", unusedString);
	                	  }else if(spectrum instanceof NMRSpectrum && (((NMRSpectrum)spectrum).getNucleus().equals("1H")|| ((NMRSpectrum)spectrum).getNucleus().equals("H"))) {
	                          subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	                          subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
	                          subDataH=subData;
	                          runData.getSession().setAttribute("subDataQCh", subDataH);
	                          context.put("htoassign", unusedString);
	                	  }else if(spectrum instanceof NMR2DSpectrum){
	                		  String spectrumtype=null;
	                		  for(Object note :  ((NMR2DSpectrum)spectrum).getNotes()){
	                			  if(note instanceof Note && ((Note)note).getDescriptor().getKey().equals("CorType")){
	                				  spectrumtype=(String)((Note)note).getValue();
	                				  break;
	                			  }
	                		  }
	                		  if(spectrumtype!=null){
		                		  List<Peak2D> peaks=new ArrayList<Peak2D>();
		                		  Map<Color,List<IBond>> couplings=new HashMap<>();
		                		  couplings.put(Color.BLUE, new ArrayList<IBond>());
		                		  IAtomContainer cdkmol=data.getMolecule();
		                		  IAtomContainer cdkmolorig=data.getMoleculeOriginal();
		                		  if(spectrumtype.equals("HSQC") || spectrumtype.equals("HMBC") || spectrumtype.equals("TOCSY") || spectrumtype.equals("NOESY") || spectrumtype.equals("COSY")){
		                			    int k=0;
		                				for (Peak2D peak2d : ((NMR2DSpectrum)spectrum).getPeakTable()) {
		                					if(peak2d.getPosition()[0]>peak2d.getPosition()[1]){
		                						double third=peak2d.getPosition()[0];
		                						peak2d.getPosition()[0]=peak2d.getPosition()[1];
		                						peak2d.getPosition()[1]=third;
		                					}
		                					peaks.add(peak2d);
		                					if(((NMR2DSpectrum)spectrum).getAssignments()!=null && ((NMR2DSpectrum)spectrum).getAssignments()[k]!=null)
		                						couplings.get(Color.BLUE).add(new Bond(cdkmolorig.getAtom(((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber1()[0]),cdkmolorig.getAtom(((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber2()[0])));
	                						k++;
		                				}
		                		  }
		                		  ReportUtil.make2d(runData.getServletConfig(), spectrumtype, cdkmolorig, cdkmol, subDataH, subDataC, table, table2d, couplings, peaks);
		                		  if(spectrumtype.equals("HSQC")){
		                           	  context.put("hsqcpeaks",  peaks);
		                           	  hsqcpeaks=peaks;
		                        	  runData.getSession().setAttribute("hsqcqc", peaks);
		                		  }
		                		  if(spectrumtype.equals("HMBC")){
		                           	  context.put("hmbcpeaks",  peaks);
		                           	  hmbcpeaks=peaks;
		                        	  runData.getSession().setAttribute("hmbcqc", peaks);
		                		  }
		                		  if(spectrumtype.equals("TOCSY")){
		                           	  context.put("tocsypeaks",  peaks);
		                           	  tocsypeaks=peaks;
		                        	  runData.getSession().setAttribute("tocsyqc", peaks);
	                        		  context.put("showtocsy", "true");
		                  		  }
		                		  if(spectrumtype.equals("NOESY")){
		                           	  context.put("noesypeaks",  peaks);
		                           	  noesypeaks=peaks;
		                        	  runData.getSession().setAttribute("noesyqc", peaks);
		                       		  context.put("shownoesy", "true");
		                		  }
		                		  if(spectrumtype.equals("COSY")){
		                           	  context.put("cosypeaks",  peaks);
		                           	  cosypeaks=peaks;
		                        	  runData.getSession().setAttribute("cosyqc", peaks);
		                		  }
	                		  }
	                	  }
	                	  if(subData!=null) {
	                		  subData.setMol(data.getMolecule(), false, false, false);
	                		  subData.setSignalstable(JcampToCMLSpectrumMapper.mapJcampToCMLSpectrum(spectrum, runData).getPeakListElements().get(0));
	                		  boolean[] assigned=new boolean[subData.getSignalstable().getPeakChildren().size()];
	                		  if(((NMRSpectrum)spectrum).getAssignments()!=null) {
		                		  for(Assignment assignment : ((NMRSpectrum)spectrum).getAssignments()) {
		                			  int i=0;
		                			  for(CMLPeak peak : subData.getSignalstable().getPeakChildren()) {
		                				  if(peak.getXValue()==assignment.getPattern().getPosition()[0]) {
		                					  int atomnumber=((AtomReference)assignment.getTargets()[0]).getAtomNumber();
		                					  if(subData.getChoosenSpectrumType().getName().equals("1H") && atomnumber>0 && !subData.getMolWithH().getAtom(atomnumber-1).getSymbol().equals("H")) {
		                						  IAtomContainer ac=subData.getMolWithH();
		                						  for(int k=0;k<ac.getConnectedAtomsCount(ac.getAtom(atomnumber));k++){
		                							  if(ac.getConnectedAtomsList(ac.getAtom(atomnumber)).get(k).getSymbol().equals("H")){
		                	                        	atomnumber=ac.getAtomNumber(ac.getConnectedAtomsList(ac.getAtom(atomnumber)).get(k));
		                	                        	break;
		                	                        }
		                						  }
		                					  }
		                					  peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), "a"+(atomnumber+1)));
		                					  assigned[i]=true;
		                				  }
			                			  i++;
		                			  }
		                		  }
	                		  }
	                		  for(int i=0;i<assigned.length;i++) {
	                			  if(!assigned[i])
	                				  unusedString.append(subData.getSignalstable().getPeakChildren().get(i).getXValue()+"\r\n");
	                		  }
	                	  }
	                  }
	                  reportpdf.add(table);
	                  reportpdf.add(new Paragraph(new Chunk("   ")));
	                  DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
	          		  format.applyPattern("#####0.00");
	                  PdfPTable tableouterprediction = new PdfPTable(2);
	                  tableouterprediction.setWidthPercentage(100);
	                  tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
	                  tableouterprediction.setWidths(new float[]{1, 1});
	                  PdfPTable tablegreenred = new PdfPTable(2);
	                  tablegreenred.setWidthPercentage(100);
                      double[] result2d=null;
                      List<com.lowagie.text.Element> footnotes=new ArrayList<com.lowagie.text.Element>();
		              if(subDataC.getMolWithH()==null && subDataH.getMolWithH()!=null)
	                	  subDataC.setMol(subDataH.getMolOriginal(), false, false, false);
	                  if(subDataC.getMolWithH()==null) {
	                      messages.append("It seems there is no structure in your file!<br>");
	                  }else{
		            	  Map<Integer, double[]> cPredictionsForAutoassignment = new HashMap<Integer, double[]>();
		                  List<CMLPeak> cPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
		                  Map<Integer, double[]> hPredictionsForAutoassignment = new HashMap<Integer, double[]>();
		                  List<CMLPeak> hPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
		                  List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
		                  CMLPeakList cShiftsToAssign=new CMLPeakList(); 
		                  CMLPeakList hShiftsToAssign=new CMLPeakList();
		                  Map<Integer,Integer> hpairs=new HashMap<Integer,Integer>();
		                  long[] morgan=Canon.symmetry(subDataC.getMolOriginal(), GraphUtil.toAdjList(subDataC.getMolOriginal()));
		                  analyzequickcheck(runData, context, carbons, hydrogens, cPredictionsForAutoassignment, cPredictionsForAutoassignmentPeaks, hPredictionsForAutoassignment, hPredictionsForAutoassignmentPeaks, diastereotopicHPredictionsForAutoassignmentPeaks, cShiftsToAssign, hShiftsToAssign, hpairs, noncarbonatoms, atoms, morgan, true, predictionsC, predictionsH, messages);
		                  //We prepare two pdf reports
		                  Document documentc = new Document();
		                  OutputStream baosc = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcc"+runData.getSession().getId()+".pdf")));
		                  PdfWriter.getInstance(documentc, baosc);
		                  documentc.open();
		                  Document documenth = new Document();
		                  OutputStream baosh = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qch"+runData.getSession().getId()+".pdf")));
		                  PdfWriter.getInstance(documenth, baosh);
		                  documenth.open();
		                  PdfPTable table1d = new PdfPTable(2);
		                  table1d.setWidthPercentage(100);
		                  table1d.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		                  table1d.setWidths(new float[]{1, 1});
		                  //We build the c report
		                  double[] resultC=ReportUtil.start1d(runData.getServletConfig(), "13","C", GeneralUtils.getPredictionLimits("13C"), subDataC, carbons, documentc, morgan, table1d, tableouterprediction, runData.getRequest(), footnotes);
		                  int overallMarkC=(int)resultC[0];
		                  String reportc=SpectrumUtils.makeJudgement(overallMarkC,resultC[5],false);
		                  context.put("reportc", SpectrumUtils.makeJudgement(overallMarkC,resultC[5],true));
		                  ReportUtil.end1d(runData.getServletConfig(), data.getMolecule(), data.getMoleculeOriginal(), reportc, tablegreenred, false);
		                  //We build the h report
		        		  double[] resultH=ReportUtil.start1d(runData.getServletConfig(), "1", "H", GeneralUtils.getPredictionLimits("1H"), subDataH, hydrogens, documenth, morgan, table1d, tableouterprediction, runData.getRequest(), null);
		                  int overallMarkH=(int)resultH[0];
		                  String reporth=SpectrumUtils.makeJudgement(overallMarkH,resultH[5],false);
		                  documentc.close();
		                  documenth.close();
		                  context.put("reporth", SpectrumUtils.makeJudgement(overallMarkH,resultH[5],true));
		                  ReportUtil.end1d(runData.getServletConfig(), data.getMolecule(), data.getMoleculeOriginal(), reporth, tablegreenred, false);
		                  reportpdf.add(table1d);
		        		  //Build 2d report
		                  Document document2d = new Document();
		                  OutputStream baos2d = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qc2d"+runData.getSession().getId()+".pdf")));
		                  PdfWriter.getInstance(document2d, baos2d);
		                  document2d.open();
		                  result2d=SpectrumUtils.calculateScore2d(subDataC.getMolWithH(), subDataC, subDataH, cosypeaks, tocsypeaks, noesypeaks, hmbcpeaks, hsqcpeaks, document2d, runData.getServletConfig(), false, true);
		                  int overallMark2d=(int)result2d[0];
		                  String report2d=SpectrumUtils.makeJudgement(overallMark2d, -1, true);
		                  context.put("report2d", report2d);
		                  document2d.close();
		                  boolean allhaveidentifiers=true;
		                  for(int i=0;i<subDataC.getMolWithH().getAtomCount();i++){
		                	  if(subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE)!=null && subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE).equals("true") && (subDataC.getIdentifiers().get(i)==null || subDataC.getIdentifiers().get(i).equals(""))){
		                		  allhaveidentifiers=false;
		                		  break;
		                	  }
		                  }
		                  if(allhaveidentifiers)
		                	  context.put("showatomnumbers","false");
		                  else
		                	  context.put("showatomnumbers","true");
		                  GeneralUtils.logToSql("QuickCheck - " + subDataC.getInputMolAsMdl(null, null,false), runData);
	                  }
	                  ReportUtil.endReport(runData.getServletConfig(), reportpdf, null, null, null, null, null, tableouterprediction, tablegreenred, table2d, (int)result2d[0], footnotes);
	                  reportpdf.close();
	              }
              }else if(topspinfile.exists()){
                  //an topspin file has been uploaded
            	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
            	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
            	  Map<String, SubmittingData> otherspectra = new HashMap<String,SubmittingData>();
            	  GeneralUtils.analyzeTopspinDir(runData, context, subDataC, subDataH, otherspectra, cosypeaks, tocsypeaks, hmbcpeaks, hsqcpeaks, noesypeaks, messages);
            	  if(!messages.toString().trim().equals(""))
            		  return;
            	  topspinfile.delete();
               	  context.put("cosypeaks",  cosypeaks);
            	  runData.getSession().setAttribute("cosyqc", cosypeaks);
   				  context.put("tocsypeaks",  tocsypeaks);
   				  runData.getSession().setAttribute("tocsyqc", tocsypeaks);
				  context.put("noesypeaks",  noesypeaks);
				  runData.getSession().setAttribute("noesyqc", noesypeaks);
            	  context.put("hmbcpeaks",  hmbcpeaks);
            	  runData.getSession().setAttribute("hmbcqc", hmbcpeaks);
            	  context.put("hsqcpeaks",  hsqcpeaks);
            	  runData.getSession().setAttribute("hsqcqc", hsqcpeaks);
            	  if(tocsypeaks.size()>0)
            		  context.put("showtocsy", "true");
            	  if(noesypeaks.size()>0)
            		  context.put("shownoesy", "true");
            	  //We start the overall report
              	  Document reportpdf=new Document();
                  OutputStream baosall = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcall"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(reportpdf, baosall);
                  reportpdf.open();
                  List<String> types=new ArrayList<>();
                  List<String> frequencies=new ArrayList<>();
                  List<String> temperatures=new ArrayList<>();
                  List<String> comments=new ArrayList<>();
                  String frequency = subDataC.getFrequency().equals("unknown")|| subDataC.getFrequency().equals("") ? "?" : subDataC.getFrequency();
                  String temperature=subDataC.getTemperature().equals("unknown") || subDataC.getTemperature().equals("")? "?" : subDataC.getTemperature();
                  if(tocsypeaks.size()>0) {
                	  types.add("TOCSY");
                	  frequencies.add(frequency);
                	  temperatures.add(temperature);
                	  comments.add("");
                  }
                  if(cosypeaks.size()>0) {
                	  types.add("COSY");
                	  frequencies.add(frequency);
                	  temperatures.add(temperature);
                	  comments.add("");
                  }
                  if(hmbcpeaks.size()>0) {
                	  types.add("HMBC");
                	  frequencies.add(frequency);
                	  temperatures.add(temperature);
                	  comments.add("");
                  }
                  ReportUtil.startFullReport(reportpdf, runData.getServletConfig(), subDataC.getMolOriginal(), subDataC.getMolWithH(), subDataC.getSolvent().getCurrentValue(), subDataC.getSmilesNonChiralProv(), temperatures, types, frequencies, comments, null, null, null, null, 0);
                  PdfPTable table = new PdfPTable(2);
        	      PdfPTable table2d = new PdfPTable(2);
                  for(String key : otherspectra.keySet()) {
                	  SubmittingData subData=otherspectra.get(key);
            		  String spectrumtype=subData.getChoosenSpectrumType().getName();
                	  if(subData.getChoosenSpectrumType().getDimensionality()==2 && (spectrumtype.contains("HMBC") || spectrumtype.contains("TOCSY") || spectrumtype.contains("COSY"))){
                		  List<Peak2D> peaks=new ArrayList<Peak2D>();
                		  Map<Color,List<IBond>> couplings=new HashMap<>();
                		  couplings.put(Color.BLUE, new ArrayList<IBond>());
                		  IAtomContainer cdkmol=subData.getMolWithH();
                		  IAtomContainer cdkmolorig=subData.getMolOriginal();
                		  for (CMLElement peak2d : subData.getSignalstable().getChildCMLElements()) {
	        					if(((CMLPeak)peak2d).getXValue()>((CMLPeak)peak2d).getYValue())
	            					peaks.add(new Peak2D(((CMLPeak)peak2d).getYValue(),((CMLPeak)peak2d).getXValue(),0));
	        					else
	            					peaks.add(new Peak2D(((CMLPeak)peak2d).getXValue(),((CMLPeak)peak2d).getYValue(),0));
								IAtom atom1cdk=AtomUtils.getAtomById(cdkmol,((CMLPeak)peak2d).getAtomRefs()[0]);
	          				  	if(atom1cdk.getSymbol().equals("H"))
	          				  		atom1cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom1cdk).get(0)));
	          					else
	          						atom1cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(atom1cdk));
								IAtom atom2cdk=AtomUtils.getAtomById(cdkmol,((CMLPeak)peak2d).getAtomRefs()[1]);
	          				  	if(atom2cdk.getSymbol().equals("H"))
	          				  		atom2cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom2cdk).get(0)));
	          					else
	          						atom2cdk=cdkmolorig.getAtom(cdkmol.getAtomNumber(atom2cdk));
	    						couplings.get(Color.BLUE).add(new Bond(atom1cdk, atom2cdk));
	        				}
                		  ReportUtil.make2d(runData.getServletConfig(), spectrumtype, cdkmolorig, cdkmol, subDataH, subDataC, table, table2d, couplings, peaks);
                		  if(spectrumtype.equals("HMBC")){
                           	  hmbcpeaks=peaks;
                		  }
                		  if(spectrumtype.equals("TOCSY")){
                           	  tocsypeaks=peaks;
                		  }
                		  if(spectrumtype.equals("COSY")){
                           	  cosypeaks=peaks;
                		  }
            		  }
                  }
                  reportpdf.add(table);
                  reportpdf.add(new Paragraph(new Chunk("   ")));
                  DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
          		  format.applyPattern("#####0.00");
                  PdfPTable tableouterprediction = new PdfPTable(2);
                  tableouterprediction.setWidthPercentage(100);
                  tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  tableouterprediction.setWidths(new float[]{1, 1});
                  PdfPTable tablegreenred = new PdfPTable(2);
                  tablegreenred.setWidthPercentage(100);
                  Map<Integer, double[]> cPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> cPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  Map<Integer, double[]> hPredictionsForAutoassignment = new HashMap<Integer, double[]>();
                  List<CMLPeak> hPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
                  CMLPeakList cShiftsToAssign=new CMLPeakList(); 
                  CMLPeakList hShiftsToAssign=new CMLPeakList();
                  Map<Integer,Integer> hpairs=new HashMap<Integer,Integer>();
                  long[] morgan=Canon.symmetry(subDataC.getMolOriginal(), GraphUtil.toAdjList(subDataC.getMolOriginal()));
                  analyzequickcheck(runData, context, carbons, hydrogens, cPredictionsForAutoassignment, cPredictionsForAutoassignmentPeaks, hPredictionsForAutoassignment, hPredictionsForAutoassignmentPeaks, diastereotopicHPredictionsForAutoassignmentPeaks, cShiftsToAssign, hShiftsToAssign, hpairs, noncarbonatoms, atoms, morgan, true, predictionsC, predictionsH, messages);
                  //We prepare two pdf reports
                  Document documentc = new Document();
                  OutputStream baosc = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qcc"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documentc, baosc);
                  documentc.open();
                  Document documenth = new Document();
                  OutputStream baosh = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qch"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(documenth, baosh);
                  documenth.open();
                  PdfPTable table1d = new PdfPTable(2);
                  table1d.setWidthPercentage(100);
                  table1d.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                  table1d.setWidths(new float[]{1, 1});
                  List<com.lowagie.text.Element> footnotes=new ArrayList<com.lowagie.text.Element>();
                  //We build the c report
                  double[] resultC=ReportUtil.start1d(runData.getServletConfig(), "13","C",GeneralUtils.getPredictionLimits("13C"), subDataC, carbons, documentc, morgan, table1d, tableouterprediction, runData.getRequest(), footnotes);
                  int overallMarkC=(int)resultC[0];
                  String reportc=SpectrumUtils.makeJudgement(overallMarkC,resultC[5],false);
                  context.put("reportc", SpectrumUtils.makeJudgement(overallMarkC,resultC[5],true));
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), reportc, tablegreenred, true);
                  //We build the h report
        		  double[] resultH=ReportUtil.start1d(runData.getServletConfig(), "1", "H", GeneralUtils.getPredictionLimits("1H"), subDataH, hydrogens, documenth, morgan, table1d, tableouterprediction, runData.getRequest(),footnotes);
                  int overallMarkH=(int)resultH[0];
                  String reporth=SpectrumUtils.makeJudgement(overallMarkH,resultH[5],false);
                  documentc.close();
                  documenth.close();
                  context.put("reporth", SpectrumUtils.makeJudgement(overallMarkH,resultH[5],true));
                  ReportUtil.end1d(runData.getServletConfig(), subDataC.getMolWithH(), subDataC.getMolOriginal(), reporth, tablegreenred, true);
                  reportpdf.add(table1d);
                  //Build 2d report
                  Document document2d = new Document();
                  OutputStream baos2d = new FileOutputStream(new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/tmp/qc2d"+runData.getSession().getId()+".pdf")));
                  PdfWriter.getInstance(document2d, baos2d);
                  document2d.open();
                  double[] result2d=SpectrumUtils.calculateScore2d(subDataC.getMolWithH(), subDataC, subDataH, cosypeaks, tocsypeaks, noesypeaks, hmbcpeaks, hsqcpeaks, document2d, runData.getServletConfig(), false, true);
                  int overallMark2d=(int)result2d[0];
                  String report2d=SpectrumUtils.makeJudgement(overallMark2d, -1, true);
                  context.put("report2d", report2d);
                  document2d.close();
                  boolean allhaveidentifiers=true;
                  for(int i=0;i<subDataC.getMolWithH().getAtomCount();i++){
                	  if(subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE)!=null && subDataC.getMolWithH().getAtom(i).getProperty(SubmittingData.VISIBLE).equals("true") && (subDataC.getIdentifiers().get(i)==null || subDataC.getIdentifiers().get(i).equals(""))){
                		  allhaveidentifiers=false;
                		  break;
                	  }
                  }
                  if(allhaveidentifiers)
                	  context.put("showatomnumbers","false");
                  else
                	  context.put("showatomnumbers","true");
                  ReportUtil.endReport(runData.getServletConfig(), reportpdf, null, null, null, null, null, tableouterprediction, tablegreenred, table2d, (int)result2d[0], footnotes);
                  reportpdf.close();
                  GeneralUtils.logToSql("QuickCheck - " + subDataC.getInputMolAsMdl(null, null,false), runData);
              }else{
	              //if no structure we just show the values again
	              Map<Integer,String> oldvaluesc=new HashMap<Integer,String>();
	              context.put("oldvaluesc",oldvaluesc);
	              Map<Integer,String> oldvaluesh=new HashMap<Integer,String>();
	              context.put("oldvaluesh",oldvaluesh);
	              Map<Integer,String> oldvalues2h=new HashMap<Integer,String>();
	              context.put("oldvalues2h",oldvalues2h);
	              for(int i=0;i<20;i++){
	                  if(!runData.getParameters().getString("inputc"+i).equals(""))
	                      oldvaluesc.put(i, runData.getParameters().getString("inputc"+i));
	                  if(!runData.getParameters().getString("inputh"+i).equals(""))
	                      oldvaluesh.put(i, runData.getParameters().getString("inputh"+i));
	                  if(!runData.getParameters().getString("input2h"+i).equals(""))
	                      oldvalues2h.put(i, runData.getParameters().getString("input2h"+i));
	                  atoms.add(i);
	              }
              }
              //We do the sorting if needed
              boolean ascending=true;
              int sortcolumn=runData.getParameters().getInt("sortcolumn", 0);
              //if a column was clicked a second time we need to go to descending
              if(runData.getParameters().getInt("oldsortcolumn",-1)==sortcolumn){
                  if(runData.getParameters().getString("descending")!=null && !runData.getParameters().getString("descending").equals("descending")){
                      ascending=false;
                      context.put("descending", "descending");
                  }
              }
              if(sortcolumn==0 && carbons.size()>0){
                  ValuesForVelocityComparator vfvbc=new ValuesForVelocityComparator(-3, !ascending, 1);
                  List<ValuesForVelocityBean> list = new ArrayList<ValuesForVelocityBean>(carbons.values());
                  Collections.sort(list,vfvbc);
                  List<Integer> newAtomsOrder=new ArrayList<Integer>();
                  for(ValuesForVelocityBean vfvb : list){
                     newAtomsOrder.add(Integer.parseInt(vfvb.getNameForElements()));
                  }
                  newAtomsOrder.addAll(noncarbonatoms);
                  atoms=newAtomsOrder;
              }
              if(sortcolumn==1 && carbons.size()>0){
                  if(!ascending)
                      Collections.sort(predictionsC, Collections.reverseOrder());
                  else
                      Collections.sort(predictionsC);
                  List<Integer> newAtomsOrder=new ArrayList<Integer>();
                  newAtomsOrder.addAll(noncarbonatoms);
                  for(double shift : predictionsC){
                      for(int key : carbons.keySet()){
                          if(carbons.get(key).statistics!=null && carbons.get(key).statistics[1]==shift)
                              newAtomsOrder.add(key);
                      }
                  }
                  atoms=newAtomsOrder;
              }
              if(sortcolumn==2 && carbons.size()>0){
                  if(!ascending)
                      Collections.sort(predictionsH, Collections.reverseOrder());
                  else
                      Collections.sort(predictionsH);
                  List<Integer> newAtomsOrder=new ArrayList<Integer>();
                  for(double shift : predictionsH){
                      for(int key : hydrogens.keySet()){
                          if(hydrogens.get(key).statistics[1]==shift)
                              newAtomsOrder.add(key);
                      }
                  }
                  for(int i : atoms){
                      if(!newAtomsOrder.contains(i))
                          newAtomsOrder.add(i);
                  }
                  atoms=newAtomsOrder;
              }
              context.put("atoms",  atoms);
              context.put("show2d", true);
              context.put("carbons", carbons);
              context.put("hydrogens", hydrogens);
              context.put("sortcolumn", sortcolumn);
              fillContext(context, runData);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    
    private double getPeak(NMRSpectrum spectrum, int atomnumber, double originalpeak) {
    	if(spectrum==null)
    		return Double.MIN_VALUE;
    	double distancemin=Double.MAX_VALUE;
    	int i=0;
    	for(Assignment assignment : ((NMRSpectrum)spectrum).getAssignments()) {
    		if(((AtomReference)assignment.getTargets()[0]).getAtomNumber()==atomnumber && Math.abs(spectrum.getPeakTable()[i].getPosition()[0]-originalpeak)<distancemin) 
    			distancemin=Math.abs(spectrum.getPeakTable()[i].getPosition()[0]-originalpeak);
    		i++;
    	}
    	i=0;
    	for(Assignment assignment : ((NMRSpectrum)spectrum).getAssignments()) {
    		if(((AtomReference)assignment.getTargets()[0]).getAtomNumber()==atomnumber && Math.abs(spectrum.getPeakTable()[i].getPosition()[0]-originalpeak)==distancemin)
    			return assignment.getPattern().getPosition()[0];
    		i++;
    	}
		return Double.MIN_VALUE;
	}

	private void write2d(String string, StringBuffer cml, HttpSession session) {
    	List<Peak2D> peaks2d = (List<Peak2D>)session.getAttribute(string);
    	cml.append("<").append(string).append(">");
    	for(Peak2D peak : peaks2d) {
    		cml.append("<peak x=\"").append(peak.getPosition()[0]).append("\" y=\"").append(peak.getPosition()[1]).append("\"/>");
    	}
    	cml.append("</").append(string).append(">");
	}

	/**
     * Performs the actual QuickCheck.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     * @throws Exception 
     * @throws SQLException 
     * @throws TorqueException 
     */
    private int[] analyzequickcheck(RunData runData, Context context, Map<Integer, ValuesForVelocityBean> carbons, Map<Integer, ValuesForVelocityBean> hydrogens, Map<Integer, double[]> cPredictionsForAutoassignment, List<CMLPeak> cPredictionsForAutoassignmentPeaks, Map<Integer, double[]> hPredictionsForAutoassignment, List<CMLPeak> hPredictionsForAutoassignmentPeaks, List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks, CMLPeakList cShiftsToAssign, CMLPeakList hShiftsToAssign, Map<Integer,Integer> hpairs, List<Integer> noncarbonatoms, List<Integer> atoms, long[] morgan, boolean mnovaread, List<Double> predictionsC, List<Double> predictionsH, StringBuffer messages) throws TorqueException, SQLException, Exception{
          context.put("messages", messages);
          SubmittingData subDataC = (SubmittingData) runData.getSession().getAttribute("subDataQCc");
          SubmittingData subDataH = (SubmittingData) runData.getSession().getAttribute("subDataQCh");
          DBMolecule oldMol = GeneralUtils.getExistingMolecule((IAtomContainer)AtomContainerManipulator.removeHydrogens(subDataC.getMolWithH()), subDataC.getMolWithH(), null, runData.getServletConfig(), subDataC.getInputMolAsMdl(null,null,false), new StringAndStringAndBoolean());
          context.put("existingmol", oldMol);
          IAtomContainer molWithH = subDataC.getMolWithH();
          IAtomContainer molAsEntered = subDataC.getMolOriginal();
          if(carbons==null)
              carbons=new HashMap<Integer,ValuesForVelocityBean>();
          if(hydrogens==null)
        	  hydrogens=new HashMap<Integer,ValuesForVelocityBean>();
          Isotopes.getInstance().configureAtoms(molAsEntered);
  			DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
  			format.applyPattern("#####0.00");
          Map<Long,Integer> morganmap = new HashMap<Long,Integer>();
          Map<Long,List<Integer>> symetricatoms=new HashMap<Long, List<Integer>>();
          List<Integer> atomsWith2Hydrogens=new ArrayList<Integer>();
          int diffhcount=0;//counts differnt hs
          int diffccount=0;//counts differnt cs
          Map<Integer,String> oldvaluesc=new HashMap<Integer,String>();
          context.put("oldvaluesc",oldvaluesc);
          Map<Integer,String> oldvaluesh=new HashMap<Integer,String>();
          context.put("oldvaluesh",oldvaluesh);
          Map<Integer,String> oldvalues2h=new HashMap<Integer,String>();
          context.put("oldvalues2h",oldvalues2h);
          boolean secondround=subDataC.getSignalstable().getChildCount()>0;
          CMLPeakList oldSignalstableC = subDataC.getSignalstable();
          Map<Integer, ValuesForVelocityBean> secondHydrogens=new HashMap<Integer,ValuesForVelocityBean>();
          List<Double> hshifts=new ArrayList<Double>();
          for(CMLPeak peak : subDataH.getSignalstable().getPeakChildren()){
              hshifts.add(peak.getXValue());
          }
          for (int i = 0; i < molWithH.getAtomCount(); i++) {
        	  boolean newatom=false;
              //We use morgan numbers to identify symmetries
        	  if(i<morgan.length){
            	  if(!morganmap.containsKey(morgan[i])){
                      morganmap.put(morgan[i], i);
                      symetricatoms.put(morgan[i], new ArrayList<Integer>());
                      newatom=true;
                  }
                  symetricatoms.get(morgan[i]).add(i);
              }
              if(i<molAsEntered.getAtomCount())
                  atoms.add(i);
              if (molWithH.getAtom(i).getSymbol().equals("C")) {
            	  diffccount++;
                  double shift=-1;
                  if(!mnovaread && runData.getParameters().getString("inputc"+i)!=null && !runData.getParameters().getString("inputc"+i).equals("")){
                      try{
                          //We test if the input is valid
                          String inputstring=runData.getParameters().getString("inputc"+i,"0").replace(',','.');
                          Double.parseDouble(inputstring);
                          if(Double.parseDouble(inputstring)!=0){
                              shift=Double.parseDouble(inputstring);
                          }                              
                      }catch(NumberFormatException ex){
                          oldvaluesc.put(i, runData.getParameters().getString("inputc"+i));
                          messages.append("Input is not a number for carbon "+(i+1)+"!<br>");
                      }
                  }else if(!mnovaread && morganmap.containsKey(morgan[i]) && runData.getParameters().getDouble("inputc"+ morganmap.get(morgan[i]),0)!=0){
                      shift=runData.getParameters().getDouble("inputc"+ morganmap.get(morgan[i]));
                  }else if(!subDataC.getShift(i).equals("")){
                	  shift=Double.parseDouble(subDataC.getShift(i));	
                  }
                  if(secondround && runData.getParameters().getString("assignc"+i)!=null && !runData.getParameters().getString("assignc"+i).equals("assignc"+i) && !runData.getParameters().getString("assignc"+i).equals("keep")){
                	  shift=Double.parseDouble(runData.getParameters().getString("assignc"+i));
                  }
                  if(shift!=-1){
                      StringBuffer hoseCode = new StringBuffer();
                      StringBuffer comment = new StringBuffer();
                      CMLPeak peak = new CMLPeak();
                      peak.setXValue(shift);
                      peak.setYValue(0);
                      for(CMLPeak oldpeak : oldSignalstableC.getPeakChildren()){
                    	  if(oldpeak.getXValue()==shift){
                              peak.setPeakMultiplicity(oldpeak.getPeakMultiplicity());
                    	  }
                      }
                      try{
                          double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(i), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                          double difference = Math.abs((double) peak.getXValue() - statistics[1]);
                          statistics[3] = difference;
                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String(format.format(statistics[0]) + "-<b>" + format.format(statistics[1]) + "</b>-" + format.format(statistics[2])) + "; " + comment);
                          if(subDataC.getIdentifiers().get(i)!=null)
                              vfvb.setIdentifier(subDataC.getIdentifiers().get(i));
                          else
                              vfvb.setIdentifier(Integer.toString(i+1));
                          vfvb.statistics=statistics;
                          vfvb.setDelta(hoseCode.toString());
                          carbons.put(i,vfvb);
                          if(!predictionsC.contains(statistics[1]))
                              predictionsC.add(statistics[1]);
                          //if we are in second round and the box is not checked, it is a candidate for auto-assignment
                          if(secondround && runData.getParameters().getString("assignc"+i)!=null && runData.getParameters().getString("assignc"+i).equals("assignc"+i)){
                              cPredictionsForAutoassignment.put(i, statistics);
                              CMLPeak peakPred = new CMLPeak();
                              peakPred.setXValue(statistics[1]);
                              peakPred.setYValue(0);
                              peakPred.setPeakMultiplicity(GeneralUtils.getMultiplicity(AtomUtils.getHcount(molWithH, molWithH.getAtom(i)),false));
                              cPredictionsForAutoassignmentPeaks.add(peakPred);
                              cShiftsToAssign.addPeak(peak);
                          }else{
                              //We assign it directly
                              peak.setAtomRefs(new String[]{molWithH.getAtom(i).getID()});
                          }
                      }catch(Exception ex){
                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                          if(subDataC.getIdentifiers().get(i)!=null)
                              vfvb.setIdentifier(subDataC.getIdentifiers().get(i));
                          else
                              vfvb.setIdentifier(Integer.toString(i+1));
                          carbons.put(i,vfvb);
                          peak.setAtomRefs(new String[]{molWithH.getAtom(i).getID()});
                      }
                      subDataC.addToSignalstable(peak);
                  }else{
                      //This means it's a candidate for auto-assignment
                      StringBuffer hoseCode = new StringBuffer();
                      StringBuffer comment = new StringBuffer();
                      try{
                          double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(i), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                          cPredictionsForAutoassignment.put(i, statistics);
                          if(!predictionsC.contains(statistics[1]))
                              predictionsC.add(statistics[1]);
                          addPeak(statistics, carbons, cPredictionsForAutoassignmentPeaks,i, comment, subDataC, GeneralUtils.getMultiplicity(AtomUtils.getHcount(molWithH, molWithH.getAtom(i)), false));
                      }catch(Exception ex){
                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                          if(subDataC.getIdentifiers().get(i)!=null)
                              vfvb.setIdentifier(subDataC.getIdentifiers().get(i));
                          else
                              vfvb.setIdentifier(Integer.toString(i+1));
                          carbons.put(i,vfvb);
                      }
                  }
              }
              int hcount=0;
              int hnumber=0;
              int hnumber2=0;
              boolean firsthfound=false;
              for(int k=0;k<molWithH.getConnectedAtomsCount(molWithH.getAtom(i));k++){
                  if(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k).getSymbol().equals("H")){
                      hcount++;
                      if(firsthfound){
                          hnumber2=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k));
                          hpairs.put(hnumber, hnumber2);
                      }else{
                          hnumber=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k));
                          firsthfound=true;
                      }
                  }
              }
              if(hcount>0){
            	  if(newatom)
            		  diffhcount++;
            	  if(!molWithH.getAtom(i).getSymbol().equals("C")){
            		  noncarbonatoms.add(i);
            	  }
                  double shift=-1;
                  if(!mnovaread && runData.getParameters().getString("inputh"+i)!=null && !runData.getParameters().getString("inputh"+i).equals("")){
                      try{
                          //We test if the input is valid
                          String inputstring=runData.getParameters().getString("inputh"+i,"0").replace(',','.');
                          Double.parseDouble(inputstring);
                          if(Double.parseDouble(inputstring)!=0){
                              shift=Double.parseDouble(inputstring);
                          }else if(morganmap.containsKey(morgan[i]) && runData.getParameters().getDouble("inputh"+ morganmap.get(morgan[i]),0)!=0){
                              shift=runData.getParameters().getDouble("inputh"+ morganmap.get(morgan[i]));
                          }
                      }catch(NumberFormatException ex){
                          oldvaluesh.put(i, runData.getParameters().getString("inputh"+i));
                          messages.append("Input is not a number for hydrogen "+(i+1)+"!<br>");
                      }
                  }else if(!mnovaread && morganmap.containsKey(morgan[i]) && runData.getParameters().getDouble("inputh"+ morganmap.get(morgan[i]),0)!=0){
                      shift=runData.getParameters().getDouble("inputh"+ morganmap.get(morgan[i]));
                  }else if(!subDataH.getShiftH(i, 1).equals("")){
                	  shift=Double.parseDouble(subDataH.getShiftH(i, 1));
                  }
                  if(secondround && runData.getParameters().getString("assignh"+i)!=null && !runData.getParameters().getString("assignh"+i).equals("assignh"+i) && !runData.getParameters().getString("assignh"+i).equals("keep")){
                	  shift=Double.parseDouble(runData.getParameters().getString("assignh"+i));
                  }
                  if(shift!=-1){
                      StringBuffer hoseCode = new StringBuffer();
                      StringBuffer comment = new StringBuffer();
                      CMLPeak peak = new CMLPeak();
                      peak.setXValue(shift);
                      peak.setYValue(0);
                      try{
                          double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(hnumber), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                          if(molWithH.getAtom(i).getSymbol().equals("C")){
                              double assignedshift = peak.getXValue();
                              String inputstring=runData.getParameters().getString("input2h"+i,"0").replace(',','.');
                              if(hcount==2 && Double.parseDouble(inputstring)!=0){
                                  assignedshift=(assignedshift + Double.parseDouble(inputstring))/2;
                                  assignedshift = assignedshift*100;
                                  assignedshift = (double)((int) assignedshift);
                                  assignedshift = assignedshift /100;
                              }
	                          double difference = Math.abs(assignedshift - statistics[1]);
	                          statistics[3] = difference;
	                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String(format.format(statistics[0]) + "-<b>" + format.format(statistics[1]) + "</b>-" + format.format(statistics[2])) + "; " + comment);
	                          vfvb.statistics=statistics;
	                          hydrogens.put(i,vfvb);
                              if(!predictionsH.contains(statistics[1]))
                                  predictionsH.add(statistics[1]);
                          }else{
                                  hydrogens.put(i, null);
                          }
                          //if we are in second round and the box is checked, it is a candidate for auto-assignment
                          if(secondround && runData.getParameters().getString("assignh"+i)!=null && runData.getParameters().getString("assignh"+i).equals("assignh"+i)){
                              hPredictionsForAutoassignment.put(i, statistics);
                              CMLPeak peakPred = new CMLPeak();
                              peakPred.setXValue(statistics[1]);
                              peakPred.setYValue(0);
                              if(hcount==2)
                                  diastereotopicHPredictionsForAutoassignmentPeaks.add(peakPred);
                              else
                                  hPredictionsForAutoassignmentPeaks.add(peakPred);
                              hShiftsToAssign.addPeak(peak);
                          }else{
                              //We assign it directly
                              peak.setAtomRefs(new String[]{molWithH.getAtom(hnumber).getID()});
                          }
                      }catch(Exception ex){
                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                          hydrogens.put(i,vfvb);
                      }
                      subDataH.addToSignalstable(peak);
                  }else{
                      //This means it's a candidate for auto-assignment
                      StringBuffer hoseCode = new StringBuffer();
                      StringBuffer comment = new StringBuffer();
                      try{
                          double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(hnumber), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                          hPredictionsForAutoassignment.put(hnumber, statistics);
                          if(hcount==2)
                              addPeak(statistics, hydrogens, diastereotopicHPredictionsForAutoassignmentPeaks,i, comment, subDataH, null);
                          else
                              addPeak(statistics, hydrogens, hPredictionsForAutoassignmentPeaks,i, comment, subDataH, null);
                          if(!predictionsH.contains(statistics[1]))
                              predictionsH.add(statistics[1]);
                      }catch(Exception ex){
                          ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                          hydrogens.put(i,vfvb);
                      }
                  }
                  if(molWithH.getAtom(hnumber).getProperty(SubmittingData.VISIBLE)!=null && molWithH.getAtom(hnumber).getProperty(SubmittingData.VISIBLE).equals("true")  && hydrogens!=null && hydrogens.get(i)!=null)
                	  hydrogens.get(i).setAtom(hnumber);
                  if(hcount==2){
                	  if(newatom)
                		  diffhcount++;
                	  atomsWith2Hydrogens.add(i);
                      //if there are two Hs, we handle a second hydrogen
                      shift=-1;
                      if(!mnovaread && runData.getParameters().getString("input2h"+i)!=null && !runData.getParameters().getString("input2h"+i).equals("")){
                          try{
                              //We test if the input is valid
                              String inputstring=runData.getParameters().getString("input2h"+i,"0").replace(',', '.');
                              Double.parseDouble(inputstring);
                              if(Double.parseDouble(inputstring)!=0){
                                  shift=Double.parseDouble(inputstring);
                              }else if(morganmap.containsKey(morgan[i]) && runData.getParameters().getDouble("input2h"+ morganmap.get(morgan[i]),0)!=0){
                                  shift=runData.getParameters().getDouble("input2h"+ morganmap.get(morgan[i]));
                              }
                          }catch(NumberFormatException ex){
                              oldvalues2h.put(i, runData.getParameters().getString("input2h"+i));
                              messages.append("Input is not a number for second hydrogen "+(i+1)+"!<br>");
                          }
                      }else if(mnovaread && morganmap.containsKey(morgan[i]) && runData.getParameters().getDouble("input2h"+ morganmap.get(morgan[i]),0)!=0){
                          shift=runData.getParameters().getDouble("input2h"+ morganmap.get(morgan[i]));
                      }else if(!subDataH.getShiftH(i, 2).equals("")){
                    	  shift=Double.parseDouble(subDataH.getShiftH(i, 2));
                      }
                      if(shift!=-1){
                          StringBuffer hoseCode = new StringBuffer();
                          StringBuffer comment = new StringBuffer();
                          CMLPeak peak = new CMLPeak();
                          peak.setXValue(shift);
                          peak.setYValue(0);
                          try{
                              double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(hnumber2), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                              double assignedshift=peak.getXValue();
                              if(!runData.getParameters().getString("inputh"+i).equals("")){
                                  assignedshift=(assignedshift + Double.parseDouble(runData.getParameters().getString("inputh"+i)))/2;
                                  assignedshift = assignedshift*100;
                                  assignedshift = (double)((int) assignedshift);
                                  assignedshift = assignedshift /100;
                              }
                              double difference = Math.abs(assignedshift - statistics[1]);
                              statistics[3] = difference;
                              ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String(format.format(statistics[0]) + "-<b>" + format.format(statistics[1]) + "</b>-" + format.format(statistics[2])) + "; " + comment);
                              vfvb.statistics=statistics;
                              secondHydrogens.put(i,vfvb);
                              if(!predictionsH.contains(statistics[1]))
                                  predictionsH.add(statistics[1]);	                                      
                              //if we are in second round and the box is checked, it is a candidate for auto-assignment
                              if(secondround && runData.getParameters().getString("assignh"+i)!=null && runData.getParameters().getString("assignh"+i).equals("assignh"+i)){
                                  hPredictionsForAutoassignment.put(i, statistics);
                                  CMLPeak peakPred = new CMLPeak();
                                  peakPred.setXValue(statistics[1]);
                                  peakPred.setYValue(0);
                                  hShiftsToAssign.addPeak(peak);
                              }else{
                                  //We assign it directly
                                  peak.setAtomRefs(new String[]{molWithH.getAtom(hnumber2).getID()});
                              }
                          }catch(Exception ex){
                              ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                              secondHydrogens.put(i,vfvb);
                          }
                          subDataH.addToSignalstable(peak);
                      }else{
                          //This means it's a candidate for auto-assignment
                          StringBuffer hoseCode = new StringBuffer();
                          StringBuffer comment = new StringBuffer();
                          try{
                              double[] statistics = AtomUtils.predictRange(molWithH, molWithH.getAtom(hnumber2), true, true, true, false, 0, comment, false, true, null, null, 6, true, hoseCode, true, null);
                              hPredictionsForAutoassignment.put(hnumber2, statistics);
                              addPeak(statistics, secondHydrogens, null,i, comment, subDataH, null);
                              if(!predictionsH.contains(statistics[1]))
                                  predictionsH.add(statistics[1]);
                          }catch(Exception ex){
                              ValuesForVelocityBean vfvb = new ValuesForVelocityBean(Float.NaN, i + "", 0, 1000, i + "", "");
                              secondHydrogens.put(i,vfvb);
                          }
                      }
                      if(molWithH.getAtom(hnumber2).getProperty(SubmittingData.VISIBLE)!=null && molWithH.getAtom(hnumber2).getProperty(SubmittingData.VISIBLE).equals("true"))
                    	  secondHydrogens.get(i).setAtom(hnumber2);
                  }
              }
          }
          context.put("atomswith2hydrogens", atomsWith2Hydrogens);
          context.put("symmetricatoms",symetricatoms);
          context.put("secondHydrogens",secondHydrogens);
          Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.BLACK, Color.GRAY, Color.MAGENTA, Color.YELLOW};
          Map<Integer, String> symetricatomscolors=new HashMap<Integer, String>();
          int i=0;
          for(long morgannumber : symetricatoms.keySet()){
        	  if(symetricatoms.get(morgannumber).size()>1){
            	  for(Integer atomnumber : symetricatoms.get(morgannumber)){
            		  symetricatomscolors.put(atomnumber, toHexString(colors[i%colors.length]));
            	  }
            	  i++;
        	  }
          }
          context.put("symmetricatoms",symetricatomscolors);
          fillContext(context, runData);
          return new int[]{diffccount, diffhcount};
    }

    
    /**
     * Adds a peak to a list of existing peaks, either by creating a new entry or by adding the assignment, if the peak already exists.
     * 
     * @param newpeak The peak to add, stemming from a prediction.
     * @param atomsMap A map containing atom number-prediction pairs. This is filled alongside the existingPeaks list.
     * @param existingPeaks The peak list where the peak should be added.
     * @param atomNumber The number of the atom (heavy atom in case of hydrogen) we are dealing with. This is used to build the atomsMap.
     * @param comment The comment string coming fromt he prediction.
     * @param subData The current submittingData object.
     * @param multiplicity The multiplicity for the shift.
     * @throws CDKException
     */
    private void addPeak(double[] newpeak, Map<Integer, ValuesForVelocityBean> atomsMap, List<CMLPeak> existingPeaks, int atomNumber, StringBuffer comment, SubmittingData subData, String multiplicity) throws CDKException {
        boolean found=false;
        for(CMLPeak peak : existingPeaks){
            if(peak.getXValue()==newpeak[1])
                found=true;
        }
        if(!found && existingPeaks!=null){
            CMLPeak peak = new CMLPeak();
            peak.setXValue(newpeak[1]);
            if(multiplicity!=null)
            	peak.setPeakMultiplicity(multiplicity);
            existingPeaks.add(peak);
        }
		DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
		format.applyPattern("#####0.00");
        ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(newpeak[1]), atomNumber + "", 0, 1000, atomNumber + "", new String(format.format(newpeak[0]) + "-<b>" + format.format(newpeak[1]) + "</b>-" + format.format(newpeak[2])) + "; " + comment);
        if(subData.getIdentifiers().get(atomNumber)!=null)
            vfvb.setIdentifier(subData.getIdentifiers().get(atomNumber));
        else
            vfvb.setIdentifier(Integer.toString(atomNumber+1));
        vfvb.statistics=newpeak;
        atomsMap.put(atomNumber,vfvb);
    }

    /**
     * The action for linking to the quickcheck page if a user is not necessarily logged in.
     * This checks if user is logged in, if yes, forwards to the action in the nextaction parameter, 
     * else shows a login page, which then forwards to nextaction.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doChecklogin(RunData runData, Context context){
        String nextaction=runData.getParameters().get("nextaction");
        if(runData.getUser().getUserName().equals("anon")){
            context.put("spectrum",GeneralUtils.replace(GeneralUtils.replace(runData.getParameters().get("spectrum"),"\"","&#x22;"),"'","&#x27;"));
            context.put("molecule",GeneralUtils.replace(GeneralUtils.replace(runData.getParameters().get("molecule"),"\"","&#x22;"),"'","&#x27;"));
            context.put("nextaction",nextaction);
            context.put("submitlevel",Integer.parseInt(runData.getParameters().get("submitlevel")));
            context.put("message", "You are not logged in. Please do so!");
        }else{
            doNextaction(nextaction, runData, context);
        }
    }

    
    /**
     * Performs an action, used from doChecklogin.
     * 
     * @param action The action to execute
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    private void doNextaction(String action, RunData runData, Context context){
      try {
        if(action.equals("loginforid")){
            fillContext(context, runData);
        }
      } catch (Exception ex) {
        context.put("error",GeneralUtils.logError(ex,"submit/donextaction",runData,true));
      }
    }


    /**
     * This action is called from the login form shown by doChecklogin if user is not logged in.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doLoginafteraddspectrum(RunData runData, Context context) {
      String username = runData.getParameters().get("username");
      String password = runData.getParameters().get("password");
      String nextaction=runData.getParameters().get("nextaction");
      try {
        // Authenticate the user and get the object.
        TurbineAuthentication service = ((TurbineAuthentication) TurbineServices.getInstance().getService("PortalAuthentication"));
        service.login(username, password);
      } catch (LoginException ex) {
          context.put("login","true");
          context.put("spectrum",GeneralUtils.replace(GeneralUtils.replace(runData.getParameters().get("spectrum"),"\"","&#x22;"),"'","&#x27;"));
          context.put("molecule",GeneralUtils.replace(GeneralUtils.replace(runData.getParameters().get("molecule"),"\"","&#x22;"),"'","&#x27;"));
          context.put("nextaction",nextaction);
          context.put("message", "Problem: "+ex.getMessage()+" logging in. Try again!");
      }
      context.put("reload","true");
      doNextaction(nextaction, runData, context);
    }
    
    /**
     * This action is called when an existing QuickCheck is to be shown.
     * 
     * @param runData  The current RunData object.
     * @param context  The current context.
     */
    public void doShowquickcheck(RunData runData, Context context){
        try {
            StringBuffer messages = new StringBuffer();
            context.put("messages", messages);
            DBQuickCheck quickcheck = DBQuickCheckPeer.retrieveByPK(Integer.parseInt(runData.getParameters().get("quickcheckid")));
            Builder builder = new Builder();
            nu.xom.Document quickcheckXML = builder.build(new ByteArrayInputStream(quickcheck.getCml().getBytes()));
            CMLBuilder cmlbuilder = new CMLBuilder(false);
            CMLCml cml= (CMLCml) cmlbuilder.parseString(quickcheckXML.getChild(0).getChild(0).toXML());
            ((SubmittingData)runData.getSession().getAttribute("subDataQCc")).data=cml;
            cml= (CMLCml) cmlbuilder.parseString(quickcheckXML.getChild(0).getChild(1).toXML());
            ((SubmittingData)runData.getSession().getAttribute("subDataQCh")).data=cml;
            context.put("quickcheckid",quickcheck.getName());
            Map<Integer, double[]> cPredictionsForAutoassignment = new HashMap<Integer, double[]>();
            List<CMLPeak> cPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
            Map<Integer, double[]> hPredictionsForAutoassignment = new HashMap<Integer, double[]>();
            List<CMLPeak> hPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
            List<CMLPeak> diastereotopicHPredictionsForAutoassignmentPeaks = new ArrayList<CMLPeak>();
            CMLPeakList cShiftsToAssign=new CMLPeakList(); 
            CMLPeakList hShiftsToAssign=new CMLPeakList(); 
            Map<Integer,Integer> hpairs=new HashMap<Integer,Integer>();
            List<Integer> noncarbonatoms=new ArrayList<Integer>();
            List<Integer> atoms=new ArrayList<Integer>();
            long[] morgan=Canon.symmetry(((SubmittingData)runData.getSession().getAttribute("subDataQCc")).getMolOriginal(), GraphUtil.toAdjList(((SubmittingData)runData.getSession().getAttribute("subDataQCc")).getMolOriginal()));
            analyzequickcheck(runData, context, null, null, cPredictionsForAutoassignment, cPredictionsForAutoassignmentPeaks,  hPredictionsForAutoassignment, hPredictionsForAutoassignmentPeaks, diastereotopicHPredictionsForAutoassignmentPeaks, cShiftsToAssign, hShiftsToAssign, hpairs, noncarbonatoms, atoms, morgan, false, new ArrayList<Double>(), new ArrayList<Double>(), messages);
            if(quickcheckXML.getChild(0).getChildCount()==5){
                if(quickcheckXML.getChild(0).getChild(4).getValue().equals("requested"))
                    context.put("csearchreport", "requested");
                else
                    context.put("csearchreport", "true");
            }
        	List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
        	List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
        	List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
        	List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
        	List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
            if(((Element)quickcheckXML.getChild(0)).getChildElements("cosyqc").size()>0) {
            	for(int i=0; i<((Element)quickcheckXML.getChild(0)).getChildElements("cosyqc").get(0).getChildElements("peak").size();i++) {
            		Element peakel=((Element)quickcheckXML.getChild(0)).getChildElements("cosyqc").get(0).getChildElements("peak").get(i);
            		cosypeaks.add(new Peak2D(Double.parseDouble(peakel.getAttribute("x").getValue()), Double.parseDouble(peakel.getAttribute("y").getValue()), 0));
            	}
            }
            if(((Element)quickcheckXML.getChild(0)).getChildElements("tocsyqc").size()>0) {
            	for(int i=0; i<((Element)quickcheckXML.getChild(0)).getChildElements("tocsyqc").get(0).getChildElements("peak").size();i++) {
            		Element peakel=((Element)quickcheckXML.getChild(0)).getChildElements("tocsyqc").get(0).getChildElements("peak").get(i);
            		tocsypeaks.add(new Peak2D(Double.parseDouble(peakel.getAttribute("x").getValue()), Double.parseDouble(peakel.getAttribute("y").getValue()), 0));
            	}
            	context.put("showtocsy", "true");
            }
            if(((Element)quickcheckXML.getChild(0)).getChildElements("noesyqc").size()>0) {
            	for(int i=0; i<((Element)quickcheckXML.getChild(0)).getChildElements("noesyqc").get(0).getChildElements("peak").size();i++) {
            		Element peakel=((Element)quickcheckXML.getChild(0)).getChildElements("noesyqc").get(0).getChildElements("peak").get(i);
            		noesypeaks.add(new Peak2D(Double.parseDouble(peakel.getAttribute("x").getValue()), Double.parseDouble(peakel.getAttribute("y").getValue()), 0));
            	}
            	context.put("shownoesy", "true");
            }
            if(((Element)quickcheckXML.getChild(0)).getChildElements("hsqcqc").size()>0) {
            	for(int i=0; i<((Element)quickcheckXML.getChild(0)).getChildElements("hsqcqc").get(0).getChildElements("peak").size();i++) {
            		Element peakel=((Element)quickcheckXML.getChild(0)).getChildElements("hsqcqc").get(0).getChildElements("peak").get(i);
            		hsqcpeaks.add(new Peak2D(Double.parseDouble(peakel.getAttribute("x").getValue()), Double.parseDouble(peakel.getAttribute("y").getValue()), 0));
            	}
            }
            if(((Element)quickcheckXML.getChild(0)).getChildElements("hmbcqc").size()>0) {
            	for(int i=0; i<((Element)quickcheckXML.getChild(0)).getChildElements("hmbcqc").get(0).getChildElements("peak").size();i++) {
            		Element peakel=((Element)quickcheckXML.getChild(0)).getChildElements("hmbcqc").get(0).getChildElements("peak").get(i);
            		hmbcpeaks.add(new Peak2D(Double.parseDouble(peakel.getAttribute("x").getValue()), Double.parseDouble(peakel.getAttribute("y").getValue()), 0));
            	}
            }
            context.put("hsqcpeaks",  hsqcpeaks);
            runData.getSession().setAttribute("hsqcqc", hsqcpeaks);
            context.put("hmbcpeaks",  hmbcpeaks);
      	  	runData.getSession().setAttribute("hmbcqc", hmbcpeaks);
      	  	context.put("cosypeaks",  cosypeaks);
      	  	runData.getSession().setAttribute("cosyqc", cosypeaks);
            context.put("tocsypeaks",  tocsypeaks);
            runData.getSession().setAttribute("tocsyqc", tocsypeaks);
            context.put("noesypeaks",  noesypeaks);
            runData.getSession().setAttribute("noesyqc", noesypeaks);
            context.put("show2d", true);
        } catch (Exception e) {
            context.put("error",GeneralUtils.logError(e,"show quickcheck",runData,true));
        }
    }

    
    /**
     * This action is called when the clear button is hit.
     * 
     * @param runData  The current RunData object.
     * @param context  The current context.
     */
    public void doClear(RunData runData, Context context){
        try {
            runData.getSession().removeAttribute("subDataQCc");
            runData.getSession().removeAttribute("subDataQCh");
        } catch (Exception e) {
            context.put("error",GeneralUtils.logError(e,"show quickcheck",runData,true));
        }
        
    }
    
    private String toHexString(Color colour) throws NullPointerException {
    	  String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
    	  if (hexColour.length() < 6) {
    	    hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
    	  }
    	  return "#" + hexColour;
    	}
}
