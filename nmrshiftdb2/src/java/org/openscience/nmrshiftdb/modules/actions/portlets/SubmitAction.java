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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.security.LoginException;
import org.apache.jetspeed.services.security.turbine.TurbineAccessController;
import org.apache.jetspeed.services.security.turbine.TurbineAuthentication;
import org.apache.jetspeed.util.template.BaseJetspeedLink;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.context.Context;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBAuthorPeer;
import org.openscience.nmrshiftdb.om.DBBookPeer;
import org.openscience.nmrshiftdb.om.DBCanonicalName;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBExperiment;
import org.openscience.nmrshiftdb.om.DBExperimentDBSample;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBIdentifier;
import org.openscience.nmrshiftdb.om.DBJournalPeer;
import org.openscience.nmrshiftdb.om.DBKeyword;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBPublisher;
import org.openscience.nmrshiftdb.om.DBPublisherPeer;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBRawFilePeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBLiterature;
import org.openscience.nmrshiftdb.om.DBSpectrumDBLiteraturePeer;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ValuesForVelocityComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.owasp.encoder.Encode;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLName;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingdogs.village.Record;

import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * These are the actions for the portlet handling submitting spectra/molecules. All data are collected 
 * in an instance of SubmittingData and the Velocity macro displays this class. buildNormalContext fills 
 * the context depending on how advanced the submit is. All other actions change the SubmittingData instance 
 * as required. The stage of the submit is also in SubmittingData, via get/setLevel.
 *
 * @author     Stefan Kuhn
 * @created    18. Februar 2002
 */
public class SubmitAction extends VelocityPortletAction {


  /**
   * Checks the Submitting Data object for completeness. This does not check for meaningful content, it just checks if certain minimum content is there.
   * 
   * @param messages The messages object to append messages to.
   * @param subData The SubmittingData object to check.
   * @throws CDKException
   * @throws TorqueException
   * @throws IOException 
   * @throws FileNotFoundException 
   * @throws NmrshiftdbException 
   */
  private void doChecksFinish(StringBuffer messages, SubmittingData subData) throws CDKException, TorqueException, FileNotFoundException, IOException, NmrshiftdbException{
          //do some checks
          if(subData.getChoosenSpectrumType()!=null && subData.getChoosenSpectrumType().getDimensionality()==1 && subData.getChoosenSpectrumType().requiresPeaks() && (subData.getSignalstable()==null || subData.getSignalstable().getPeakElements().size()==0)){
              messages.append("You did not enter any shift!<br>");
          }
          for(int i=0;i<subData.getConditions().size();i++){
              if(subData.getConditions().get(i).getCurrentSelectedCondition()==null && subData.getConditions().get(i).getCurrentValue().equals("")){
                  messages.append(subData.getConditions().get(i).getConditionName()+" has no value!<br>");
              }
          }
          if(subData.getMolOriginal()==null){
              messages.append("You did not enter a structure!<br>");
          }
          if(subData.getChoosenSpectrumType()!=null && subData.getChoosenSpectrumType().getDimensionality()==2 && subData.getImage()==null){
              messages.append("For 2D spectra an image file is mandatory!<br>");
          }
  }

  /**
   *Saves the submitting data object to disk
   *
   * @param  subData  The submittinData object
   * @param  runData  The current runData object
   */
  private void saveSubdataToDisk(SubmittingData subData, RunData runData) {
    try {
    	TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
    	if(subData.getEditSpectrum()!=0)
    		tuser.setBrokenEdit(subData.data.toXML().getBytes());
    	else
    		tuser.setBrokensubmit(subData.data.toXML().getBytes());
      tuser.save();
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "failed to write serialized submitting data object to disk", runData,true);
    }
  }


  /**
   * Default behavior for the portlet action.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  @Override
  protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData runData) {
      try{
          UrlTool urltool = new UrlTool();
          urltool.init(runData);
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          //Create new SubmittingData object in case there is none in the session, i. e. no submission currently going on
          if (context.get("messages")==null || subData == null){
              subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
              runData.getSession().removeAttribute("editspectrum");
              runData.getSession().setAttribute("subData", subData);
          }
          HttpServletRequest req = runData.getRequest();
          HttpSession session = req.getSession();
          GeneralUtils.initProps(runData.getServletConfig());
          //In case there is a submittingData object saved, we display the choice to continue
          //it seems the perm object in User is not correctly filled, i. e. BROKENSUBMIT is not in it. So we do that manually
          boolean brokensubmitexists=false;
          boolean brokeneditexists=false;
          if (context.get("messages")==null && runData.getUser().getUserName() != null) {
            List<Record> l = TurbineUserPeer.executeQuery("SELECT BROKENSUBMIT, BROKEN_EDIT FROM TURBINE_USER  where LOGIN_NAME=\"" + runData.getUser().getUserName() + "\";");
            String brokensubmit = ((Record) l.get(0)).getValue(1).asString();
            if (brokensubmit == null) {
              brokensubmit = "";
            }
            runData.getUser().setPerm("BROKENSUBMIT", brokensubmit);
            if (!subData.getDoubleBondConfiguration() && !brokensubmit.equals("") && subData.getMolWithH()==null) {
              brokensubmitexists=true;
            }
            String brokenedit = ((Record) l.get(0)).getValue(2).asString();
            if (brokenedit == null) {
              brokenedit = "";
            }
            brokenedit=new String(brokenedit+"");
            String brokenedit2="";
            if(brokenedit!=null && brokenedit.length()>5)
              brokenedit2=brokenedit;
            runData.getUser().setPerm("BROKEN_EDIT", brokenedit2);
            if (!subData.getDoubleBondConfiguration() && !brokenedit2.equals("") && subData.getMolWithH()==null) {
              brokeneditexists=true;
            }
          }
          if(brokensubmitexists)
              context.put("brokensubmitfile", "true");
          if(brokeneditexists)
              context.put("brokeneditfile", "true");
          //This does file upload in case of jchempaint and non-java mode
          /* TODO reactivate if(req.getContentType()!=null && req.getContentType().indexOf("multipart/form-data")>-1){
              FileItem[] fi = runData.getParameters().getFileItems("Datei");
              StringBuffer messages=new StringBuffer();
              GeneralUtils.processFileItem(fi[0],messages,runData,"moltxt");
              ((StringBuffer)context.get("messages")).append(messages.toString());
              subData.setMol((IMolecule)session.getAttribute("moltxt"), false, true,false);
              session.removeAttribute("moltxt");
              StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
              DBMolecule oldMol = GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithHCount().getBondCount()], runData.getServletConfig(), subData.getInputMolAsMdl(null,null), ssb);
              subData.setSmilesNonChiralProv(ssb.string1);
              subData.setSmilesChiralProv(ssb.string2);
              subData.setIsChiralProv(ssb.bool);
              if (oldMol != null) {
                  subData.setExistingMoleculeId(oldMol.getMoleculeId());
                  oldMol.addValuesToSubmittingData(subData,null,false);
              }
          }*/
          //Handle samples
          if(subData.getLevel()==8){
              Date end=(Date)runData.getSession().getAttribute("endsample");
              if(end==null){
                  end=new Date();
                  runData.getSession().setAttribute("endsample",end);
              }
              Date start=(Date)runData.getSession().getAttribute("startsample");
              if(start==null){
                  long millis=System.currentTimeMillis();
                  start=new Date(millis-(7*24*60*60*1000));
                  runData.getSession().setAttribute("startsample",start);
              }
              context.put("start",start);
              context.put("end",end);
              List<DBSample> v3=new Vector<DBSample>();
              Criteria crit=new Criteria();
              crit.add(DBSamplePeer.USER_ID, ((BaseJetspeedUser)runData.getUser()).getUserId());
              crit.addJoin(DBSamplePeer.SAMPLE_ID,DBRawFilePeer.SAMPLE_ID);
              crit.setDistinct();
              crit.addDescendingOrderByColumn(DBSamplePeer.DATE);
              List<DBSample> v=DBSamplePeer.doSelect(crit);
              SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
              for(int i=0;i<v.size();i++){
                Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(end);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                Date dateplus = calendar.getTime();
                  String sql="select distinct SAMPLE.SAMPLE_ID from SAMPLE join RAW_FILE using(SAMPLE_ID) left join RAW_FILE_SPECTRUM using (RAW_FILE_ID) where SAMPLE.SAMPLE_ID ="+((DBSample)v.get(i)).getSampleId()+" and RAW_FILE_SPECTRUM.SPECTRUM_ID is null and SAMPLE.DATE>='"+formatter.format(start)+"' and SAMPLE.DATE<='"+formatter.format(dateplus)+"' and RAW_FILE.ASSIGNED='false'";
                  List<Record> v4=DBSamplePeer.executeQuery(sql);
                  if(v4.size()>0)
                    v3.add(DBSamplePeer.retrieveByPK(new NumberKey(v4.get(0).getValue(1).asInt())));
              }
              context.put("samples",v3);
              Vector<Integer> days=new Vector<Integer>();
              for (int i=1;i<32;i++){
                days.add(new Integer(i));
              }
              context.put("days",days );
              Vector<Integer> months=new Vector<Integer>();
              for (int i=1;i<13;i++){
                months.add(new Integer(i));
              }
              context.put("months",months );
              Vector<Integer> years=new Vector<Integer>();
              for (int i=2000;i<=new Date().getYear()+1900;i++){
                years.add(new Integer(i));
              }
              context.put("years",years );
          }
          //The number of Atoms in the current molecule
          context.put("choosenspectrumtype", subData.getChoosenSpectrumType());
          if(subData.getLevel() == 3 && subData.getMolOriginal()==null){
              ((StringBuffer)context.get("messages")).append("You must enter a structure before you can do an assignment!<br>");
          }else if (subData.getLevel() == 3 && session.getAttribute("shifts") == null) {
            if(subData.getChoosenSpectrumType()==null){
                Criteria crit = new Criteria();
                crit.add(DBSpectrumTypePeer.NAME, "13C");
                List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
                subData.setChoosenSpectrumType(v.get(0));
            }
            //The atoms in the current molecule of the element of the choosen spectrum type
            ArrayList<ValuesForVelocityBean> elementsOfChoosenType = new ArrayList<ValuesForVelocityBean>();
            IAtomContainer mol = subData.getMolWithH();
            if(AtomUtils.isOrganometallic(mol))
            	AtomUtils.doReplacement(mol);
            int highestNonHAtom = 0;
            for (int i = mol.getAtomCount() - 1; i > -1; i--) {
              if (!mol.getAtom(i).getSymbol().equals("H")) {
                highestNonHAtom = i + 1;
                break;
              }
            }
            boolean isHSpectrum = subData.getChoosenSpectrumType()!=null && subData.getChoosenSpectrumType().getElementSymbol().equals("H");
    		DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
    		format.applyPattern("#####0.00");
            ArrayList<String> multiheaders=new ArrayList<String>();
            if(runData.getSession().getAttribute("trueonly")==null)
              runData.getSession().setAttribute("trueonly",new Boolean(true));
            int ignoreMolecule = 0;
            if (subData.getEditSpectrum() != 0) {
              ignoreMolecule = subData.getEditSpectrum();
            }
            for (int i = 0; i < highestNonHAtom; i++) {
              int NumberOfHs = 0;
              int NumberOfH1s = 0;
              boolean twoHs = false;
              String identifierHeavyAtom=subData.getIdentifiers().get(i);
              String nametouse=identifierHeavyAtom;
              if(identifierHeavyAtom==null)
                  nametouse=Integer.toString(i+1);
              if (isHSpectrum) {
                StringBuffer hnumbers = new StringBuffer();
                List<IAtom> atoms = mol.getConnectedAtomsList(mol.getAtom(i));
                IAtom hatom=null;
                for (int k = 0; k < atoms.size(); k++) {
                  if (atoms.get(k).getSymbol().equals("H")) {
                    NumberOfHs++;
                    if (atoms.get(k).getSymbol().equals("H")) {
                      NumberOfH1s++;
                      hatom=atoms.get(k);
                    }
                  }
                }
                if (NumberOfH1s != 0) {
                  ArrayList<Integer> v = new ArrayList<Integer>();
                  boolean hvisible=false;
                    for (int k = 0; k < atoms.size(); k++) {
                      if (atoms.get(k).getSymbol().equals("H")) {
                        v.add(new Integer(mol.getAtomNumber(atoms.get(k)) + 1));
                        if(atoms.get(k).getProperty(SubmittingData.VISIBLE)!=null && atoms.get(k).getProperty(SubmittingData.VISIBLE).equals("true"))
                            hvisible=true;
                      }
                    }
                    StringBuffer comment = new StringBuffer();
                    double prediction[]=new double[20];
                    try{
                        prediction = AtomUtils.predictRange(mol, hatom, false, true, true, false, ignoreMolecule, comment, false, true, runData, null, -1,true, null,((Boolean)runData.getSession().getAttribute("trueonly")).booleanValue(), null);
                    }catch(Exception ex){
                        //no prediction possible
                    }
                    if (prediction[0] > prediction[1]) {
                      prediction[0] = prediction[1];
                    }
                    if (prediction[2] < prediction[1]) {
                      prediction[2] = prediction[1];
                    }
                    if (NumberOfHs == 2 && NumberOfH1s == 2) {
                      twoHs = true;
                      if (v.size() > 0) {
                        elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i + ".1"), i + "001", prediction.length==20 ? 0 : prediction[0], prediction.length==20 ? 1000 : prediction[2], prediction.length==20 ? new String("No data available") : "<div onmouseover=\"XBT(this, {text: '"+new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment + "', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", new String("<b>"+nametouse + "</b>-H<sub>a</sub>" + (hvisible ? " (H " + v.get(0) + ")" : "")), v.get(0).intValue()));
                      } else {
                        elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i + ".1"), i + "001", prediction.length==20 ? 0 : prediction[0], prediction.length==20 ? 1000 : prediction[2], prediction.length==20 ? new String("No data available") : "<div onmouseover=\"XBT(this, {text: '"+new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment + "', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", new String("<b>"+nametouse + "</b>-H<sub>a</sub>"), v.get(0).intValue()));
                      }
                      if (v.size() > 1) {
                        elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i + ".2"), i + "002", prediction.length==20 ? 0 : prediction[0], prediction.length==20 ? 1000 : prediction[2], prediction.length==20 ? new String("No data available") : "<div onmouseover=\"XBT(this, {text: '" + new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment + "', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", new String("<b>"+nametouse + "</b>-H<sub>b</sub>" + (hvisible ? " (H " + v.get(1) + ")" : "")), v.get(1).intValue()));
                      } else {
                        elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i + ".2"), i + "002", prediction.length==20 ? 0 : prediction[0], prediction.length==20 ? 1000 : prediction[2], prediction.length==20 ? new String("No data available") : "<div onmouseover=\"XBT(this, {text: '" + new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment + "', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", new String("<b>"+nametouse + "</b>-H<sub>b</sub>"), v.get(1).intValue()));
                      }
                    } else {
                      for (int k = 0; k < v.size(); k++) {
                        hnumbers.append("H " + v.get(k) + ", ");
                      }
                      GeneralUtils.removeLastComma(hnumbers);
                      elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), "" + i, prediction.length==20 ? 0 : prediction[0], prediction.length==20 ? 1000 : prediction[2], prediction.length==20 ? new String("No data available") : "<div onmouseover=\"XBT(this, {text: '" + new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment + "', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", new String("<b>"+ nametouse + "</b>-H" + (hvisible ? " (" + hnumbers + ")" : "" )), v.get(0).intValue()));
                    }
                    if(v.size()>0){
                      if(twoHs){
                          elementsOfChoosenType.get(elementsOfChoosenType.size()-1).setMultiplicity((v.get(1).intValue()-1)+"");
                          //isInRange tells if an identifier box has to be shown or not
                          elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIsInRange(false);
                          elementsOfChoosenType.get(elementsOfChoosenType.size()-2).setMultiplicity((v.get(0).intValue()-1)+"");
                          if(identifierHeavyAtom!=null){
                              elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIdentifier(identifierHeavyAtom);
                              elementsOfChoosenType.get(elementsOfChoosenType.size() - 2).setIdentifier(identifierHeavyAtom);
                          }
                      }else{
                          elementsOfChoosenType.get(elementsOfChoosenType.size()-1).setMultiplicity((v.get(0).intValue()-1)+"");
                          if(identifierHeavyAtom!=null)
                              elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIdentifier(identifierHeavyAtom);
                      }
                    }
                } else {
                  //This means we only need the possibility to renumber the atom
                  elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), i + "", -1, -1, new String(""), new String(nametouse)));
                  if(identifierHeavyAtom!=null)
                      elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIdentifier(identifierHeavyAtom);
                }
                multiheaders.add("<b>"+ nametouse + "</b>-H (" + hnumbers + ")");
              } else {
                boolean rightAtom = mol.getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getElementSymbol());
                if (rightAtom) {
                  //This means the user needs to choose the shift
                  try {
                    StringBuffer comment = new StringBuffer();
                    double prediction[] = AtomUtils.predictRange(mol, mol.getAtom(i), false, true, true, false, ignoreMolecule, comment, false, true, runData, null, -1,true, null,((Boolean)runData.getSession().getAttribute("trueonly")).booleanValue(), null);
                    if (prediction[0] > prediction[1]) {
                      prediction[0] = prediction[1];
                    }
                    if (prediction[2] < prediction[1]) {
                      prediction[2] = prediction[1];
                    }
                    elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), i + "", prediction[0], prediction[2], "<div onmouseover=\"XBT(this, {text: '"+new String(format.format(prediction[0]) + "-<b>" + format.format(prediction[1]) + "</b>-" + format.format(prediction[2])) + "; " + comment+"', className: 'xbtooltip'})\">" + format.format(prediction[1]) + "</div>", nametouse, i+1));
                  } catch (CDKException ex) {
                    elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), i + "", 0, 1000, new String("No data available"), nametouse, i+1));
                  } catch (Exception ex) {
                    elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), i + "", 0, 1000, new String("No data available"), nametouse, i+1));
                    GeneralUtils.logError(ex, "prediction", runData,true);
                  }
                  elementsOfChoosenType.get(elementsOfChoosenType.size()-1).setMultiplicity(i+"");
                } else {
                  //This means we only need the possibility to renumber the atom
                  elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(i), i + "", -1, -1, new String(""), nametouse));
                }
                multiheaders.add(new String(nametouse));
                if (subData.getIdentifiers().get(new Integer(i)) != null)
                    elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIdentifier((String) subData.getIdentifiers().get(new Integer(i)));
                else
                    elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).setIdentifier(elementsOfChoosenType.get(elementsOfChoosenType.size() - 1).getDisplayText());
              }
            }
            ValuesForVelocityComparator vfvbc=new ValuesForVelocityComparator(-3, false, 1);
            Collections.sort(elementsOfChoosenType,vfvbc);
            //Here follows handling of couplings
            int i=0;
            for(ValuesForVelocityBean vfvb : elementsOfChoosenType){
                ArrayList<Integer> v2 = new ArrayList<Integer>();
                ArrayList<StringAndStringAndBoolean> couplings=new ArrayList<StringAndStringAndBoolean>();
                int NumberOfHs = 0;
                if(vfvb.getAtom()>-1){
                    List<IAtom> atoms = mol.getConnectedAtomsList(mol.getConnectedAtomsList(mol.getAtom(vfvb.getAtom()-1)).get(0));
                    for (int k = 0; k < atoms.size(); k++) {
                        if (atoms.get(k).getSymbol().equals("H")) {
                          v2.add(new Integer(mol.getAtomNumber(atoms.get(k))));
                          NumberOfHs++;
                        }
                    }
                }
                for (int n = 0; n < elementsOfChoosenType.size(); n++) {
                  if (isHSpectrum) {
                    int NumberOfHs2 = 0;
                    ArrayList<Integer> v = new ArrayList<Integer>();
                    if(elementsOfChoosenType.get(n).getAtom()>-1){
                        List<IAtom> atoms = mol.getConnectedAtomsList(mol.getConnectedAtomsList(mol.getAtom(elementsOfChoosenType.get(n).getAtom()-1)).get(0));
                        for (int k = 0; k < atoms.size(); k++) {
                          if (atoms.get(k).getSymbol().equals("H")) {
                            v.add(new Integer(mol.getAtomNumber(atoms.get(k))));
                            NumberOfHs2++;
                          }
                        }
                    }
                    if (NumberOfHs2 != 0 && n>i && NumberOfHs != 0) {
                        StringAndStringAndBoolean ssb=new StringAndStringAndBoolean();
                        ssb.string1="coup"+(vfvb.getAtom()-1)+""+((elementsOfChoosenType.get(n).getAtom()-1));
                        ssb.string2=(vfvb.getAtom()-1)+","+((elementsOfChoosenType.get(n).getAtom()-1));
                        couplings.add(ssb);
                    } else {
                      StringAndStringAndBoolean ssb=new StringAndStringAndBoolean();
                      ssb.string1="-";
                      ssb.string2="";
                      couplings.add(ssb);
                    }
                  } else {
                    boolean rightAtom=false;
                    if(vfvb.getAtom()>-1){
                        rightAtom = mol.getAtom(vfvb.getAtom()-1).getSymbol().equals(subData.getChoosenSpectrumType().getElementSymbol());
                    }
                    if(rightAtom && elementsOfChoosenType.get(n).getAtom()>-1){
                        rightAtom = mol.getAtom(elementsOfChoosenType.get(n).getAtom()-1).getSymbol().equals(subData.getChoosenSpectrumType().getElementSymbol());
                    }
                    if(elementsOfChoosenType.get(n).getAtom()==-1)
                        rightAtom=false;
                    if (rightAtom && n>i ) {
                            StringAndStringAndBoolean ssb=new StringAndStringAndBoolean();
                            ssb.string1="coup"+(vfvb.getAtom()-1)+""+((elementsOfChoosenType.get(n).getAtom()-1));
                            ssb.string2=(vfvb.getAtom()-1)+","+((elementsOfChoosenType.get(n).getAtom()-1));
                            couplings.add(ssb);
                    }else{
                      StringAndStringAndBoolean ssb=new StringAndStringAndBoolean();
                      ssb.string1="-";
                      ssb.string2="";
                      couplings.add(ssb);
                    }
                  }
                }
                elementsOfChoosenType.get(i).setCouplings(couplings);
                i++;
            }
            //These are the shifts
            List<CMLPeak> shifts = new ArrayList<CMLPeak>();
            CMLPeakList v = subData.getSignalstable();
            for (int k = 0; k < v.getPeakElements().size(); k++) {
              CMLPeak vt=new CMLPeak();
              vt.setXValue(v.getPeakElements().get(k).getXValue());
              vt.setYValue(v.getPeakElements().get(k).getYValue());
              if(v.getPeakElements().get(k).getAtomRefs()!=null)
                  vt.setAtomRefs(v.getPeakElements().get(k).getAtomRefs());
              vt.setTitle(Integer.toString(k + 1));
              shifts.add(vt);
            }
            Collections.sort(shifts,new SignalComparator(false));
            context.put("multiheaders",multiheaders);
            session.setAttribute("multiheaders",multiheaders);
            context.put("shifts", shifts);
            session.setAttribute("shifts", shifts);
            context.put("elements", elementsOfChoosenType);
            session.setAttribute("elements", elementsOfChoosenType);
          }
          if (subData.getLevel()==3){
            if(subData.getSignalstable().getPeakElements().size()==0)
                subData.shiftlist="1";
            context.put("entercouplings",subData.enterconstants+"");
            @SuppressWarnings("unchecked")
            int couplingssize=((ValuesForVelocityBean) ((ArrayList<ValuesForVelocityBean>)session.getAttribute("elements")).get(0)).getCouplings().size();
            if (subData.getExistingMoleculeId() != 0 && subData.getEditSpectrum() == 0)
              context.put("size",(5+couplingssize)+"");
            else
              context.put("size",(6+couplingssize)+"");
            context.put("couplingssize",couplingssize+"");
          }else{
              subData.shiftlist="0";
          }
          context.put("shiftlist",subData.shiftlist);
          if (subData.getLevel() == 3 && session.getAttribute("shifts") != null) {
            context.put("shifts", session.getAttribute("shifts"));
            session.removeAttribute("shifts");
            context.put("elements", session.getAttribute("elements"));
            session.removeAttribute("elements");
            context.put("multiheaders", session.getAttribute("multiheaders"));
            session.removeAttribute("multiheaders");
          }
          if (subData.getLevel() == 7) {
              //look if we have to ask for unassigned spectra
              context.put("unassigned","false");
              for (int i = 0; i < subData.getSignalstable().getPeakElements().size(); i++) {
                  if (subData.getSignalstable().getPeakElements().get(i).getAtomRefs()==null || subData.getSignalstable().getPeakElements().get(i).getAtomRefs().length == 0) {
                    context.put("unassigned","true");
                    break;
                  }
              }
          }
          if (subData.getLevel() == 4) {
            //Put the existing keywords into the context
            List<DBKeyword> v = DBKeywordPeer.getExistingMoleculeKeywords();
            List<DBKeyword> v2 = DBKeywordPeer.getExistingSpectrumKeywords();
            context.put("existingSpectrumKeywords", v2);
            context.put("existingMoleculeKeywords", v);
            //Populate with experiment types if labsystem
            if(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()!=null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()>0){
                String sql5="select EXPERIMENT_CATEGORY_ID from EXPERIMENT_CATEGORY order by ORDER_POSITION";
                List<Record> v9=DBSamplePeer.executeQuery(sql5);
                List<List<DBExperiment>> experiments = new ArrayList<List<DBExperiment>>();
                for(int i=0;i<v9.size();i++){
                    Criteria crit = new Criteria();
                    crit.add(DBExperimentPeer.EXPERIMENT_CATEGORY_ID,v9.get(i).getValue(1).asInt());
                    crit.addAscendingOrderByColumn(DBExperimentPeer.EXPERIMENT_ID);
                    experiments.add(DBExperimentPeer.doSelect(crit));
                }
                context.put("experiments", experiments);
                if(subData.getAssignmentMethod()!=null){
                    Map<Integer,Integer> presets=new HashMap<Integer,Integer>();
                    for(int i=0;i<experiments.size();i++){
                        for(int k=0;k<experiments.get(i).size();k++){
                            if(subData.getAssignmentMethod().getCurrentValue().indexOf(experiments.get(i).get(k).getName())>-1){
                                presets.put(experiments.get(i).get(k).getExperimentId(), 1);
                            }
                        }
                    }
                    context.put("presets",presets);
                }
            }
          }
          if (subData.getLevel() == 0) {
            for (int i = 0; i < subData.getLiterature().size(); i++) {
              if (((DBLiterature) subData.getLiterature().get(i)).getType().equals(NmrshiftdbConstants.MONOGRAPH)) {
                if (context.get("monograph1") == null) {
                  context.put("monograph1", subData.getLiterature().get(i));
                } else {
                  context.put("monograph2", subData.getLiterature().get(i));
                }
              }
              if (((DBLiterature) subData.getLiterature().get(i)).getType().equals(NmrshiftdbConstants.JOURNAL_ARTICLE)) {
                if (context.get("journalarticle1") == null) {
                  context.put("journalarticle1", subData.getLiterature().get(i));
                } else {
                  context.put("journalarticle2", subData.getLiterature().get(i));
                }
              }
              if (((DBLiterature) subData.getLiterature().get(i)).getType().equals(NmrshiftdbConstants.OTHER_ARTICLE)) {
                if (context.get("otherarticle1") == null) {
                  context.put("otherarticle1", subData.getLiterature().get(i));
                } else {
                  context.put("otherarticle2", subData.getLiterature().get(i));
                }
              }
            }
            if (context.get("otherarticle1") == null) {
              context.put("otherarticle1", new DBLiterature());
            }
            if (context.get("otherarticle2") == null) {
              context.put("otherarticle2", new DBLiterature());
            }
            if (context.get("journalarticle1") == null) {
              context.put("journalarticle1", new DBLiterature());
            }
            if (context.get("journalarticle2") == null) {
              context.put("journalarticle2", new DBLiterature());
            }
            if (context.get("monograph1") == null) {
              context.put("monograph1", new DBLiterature());
            }
            if (context.get("monograph2") == null) {
              context.put("monograph2", new DBLiterature());
            }
            Criteria crit=new Criteria();
            crit.addAscendingOrderByColumn(DBJournalPeer.TITLE);
            context.put("existingjournals", DBJournalPeer.doSelect(crit));
            crit=new Criteria();
            crit.addAscendingOrderByColumn(DBPublisherPeer.NAME);
            context.put("existingplacepublishers", DBPublisherPeer.doSelect(crit));
            context.put("existingbookauthors", DBAuthorPeer.getExistingBookAuthors());
            context.put("existingbooktitles", DBBookPeer.getExistingBookTitles());
          }
          //Search for existing spectra of this molecule
          String inchi = GeneralUtils.generateInchi(subData.getMolWithH(),false);
            List<DBMolecule> v1 = DBMoleculePeer.doSelectByInchi(inchi,false,false);
            if (v1.size() > 0) {
              context.put("nochiral", v1.get(0).selectSpectra(subData.getEditSpectrum()));
            }
            if (subData.getIsChiralProv()) {
              List<DBMolecule> v2 = DBMoleculePeer.doSelectByInchi(inchi,true,false);
              if (v2.size() > 0) {
                context.put("thischiral", v2.get(0).selectSpectra(subData.getEditSpectrum()));
              }
            }
            List<DBMolecule> v3 = DBMoleculePeer.doSelectByInchi(inchi,true,false);
            if (v3.size() > 0) {
              ArrayList<DBSpectrum> v4 = new ArrayList<DBSpectrum>();
              for (int i = 0; i < v3.size(); i++) {
                v4.addAll(v3.get(i).selectSpectra(subData.getEditSpectrum()));
              }
              context.put("otherchiral", v4);
            }
          //the number of rows the table for the assignments will have (10 is minimum)
          int tableSize;
          if (context.get("elements") != null && ((ArrayList<ValuesForVelocityBean>) context.get("elements")).size() > 9) {
            tableSize = ((ArrayList<ValuesForVelocityBean>) context.get("elements")).size() + 2;
          } else {
            tableSize = 12;
          }
          context.put("subdata", subData);
          context.put("isinlabgroup", TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()!=null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()>0 ? "true" : "false");
          context.put("tablesize", tableSize + "");
          //we put existing identfier sets into context if any
          if(subData.getExistingMoleculeId()!=0){
              Map<Integer,String> identifiersets=new HashMap<Integer,String>();
              DBMolecule mol=DBMoleculePeer.retrieveByPK(subData.getExistingMoleculeId());
              for(Object spectrum : mol.getDBSpectrums()){
                  if(((DBSpectrum)spectrum).hasAnyIdentifier()){
                      StringBuffer identifiersstring=new StringBuffer();
                      DBSpectrum thisspectrum =(DBSpectrum)spectrum;
                      for(Object atom : mol.getDBAtoms()){
                          if(!((DBAtom)atom).getIdentifier(thisspectrum).equals("")){
                              identifiersstring.append(((DBAtom)atom).getIdentifier(thisspectrum)+",");
                          }
                      }
                      identifiersets.put(thisspectrum.getSpectrumId(), GeneralUtils.removeLastComma(identifiersstring).toString());
                  }
              }
              if(identifiersets.size()>0)
                  context.put("identifiersets",identifiersets);
          }
          //ArrayList of possible double bond configurations in case specified
          if (subData.getLevel() == 1 && subData.getDoubleBondConfiguration()) {
            if (subData.getEditSpectrum() == 0) {
              context.put("configs", AtomUtils.makeDoubleBondSet(subData.getMolWithH(), null, subData.getIsDoubleBondSpecified(),null));
            } else {
              context.put("configs", AtomUtils.makeDoubleBondSet(subData.getMolWithH(), DBSpectrumPeer.retrieveByPK(subData.getEditSpectrum()).getDBMolecule().getBonds(), subData.getIsDoubleBondSpecified(),null));
            }
          }
          if (subData.getLevel() == 1 && subData.getMolWithH()!=null) {
            context.put("showoldinput", "true");
          }
          if (subData.getEditSpectrum() != 0) {
            context.put("editspectrum", "true");
          }
          if (subData.getSignalstable() != null && subData.getLevel() < 3 && subData.getKeepAssignmentEntries().equals("")) {
            subData.setKeepAssignmentEntries(ParseUtils.toInputSpectrum(subData.getSignalstable()));
          }
          context.put("jsarraylength", "" + (subData.getSignalstable().getPeakElements().size() + 1));
          if (subData.getExistingMoleculeId() != 0 && subData.getEditSpectrum() == 0) {
            DBMolecule existingMolecule = DBMoleculePeer.retrieveByPK(subData.getExistingMoleculeId());
            if (existingMolecule.getCasNumber().equals("")) {
              context.put("existingcas", "false");
            }
            if(existingMolecule.getDBSpectrums().size()>0){
                context.put("existingmolhasspectra", "true");
            }else{
                context.put("existingmolhasspectra", "false");
            }
            boolean hasautonom=false;
            List<DBCanonicalName> v=existingMolecule.getDBCanonicalNames();
            for(int i=0;i<v.size();i++){
              if(v.get(i).getCanonicalNameTypeId()==1){
                hasautonom=true;
                break;
              }
            }
            if (!hasautonom) {
              context.put("existingcanname", "false");
            }
          }else{
              context.put("existingmolhasspectra", "false");
          }
          //Type with id>=100 are technical (all heavy atoms etc).
          Criteria crit = new Criteria();
          if(runData.getSession().getAttribute("subDataH")!=null){
              //if there was an mnova or topspin upload we only show 1h and 13c
        	  crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID,1);
        	  Criteria.Criterion c1 = crit.getNewCriterion(DBSpectrumTypePeer.SPECTRUM_TYPE_ID,2,Criteria.EQUAL);
        	  crit.or(c1);
        	  //and 2d as needed for topspin or nmredata
        	  Map<String,SubmittingData> otherspectra = (Map<String,SubmittingData>)runData.getSession().getAttribute("otherspectra");
        	  if(otherspectra!=null) {
	        	  for(String key : otherspectra.keySet()){
	        		  if(otherspectra.get(key).getChoosenSpectrumType()!=null){
	        			  c1 = crit.getNewCriterion(DBSpectrumTypePeer.SPECTRUM_TYPE_ID,otherspectra.get(key).getChoosenSpectrumType().getSpectrumTypeId(),Criteria.EQUAL);
	        			  crit.or(c1);
	        		  }
	        	  }
        	  }
          }else{
              crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID,100, Criteria.LESS_THAN);
              if(!urltool.use2DSpectra()){
                  crit.add(DBSpectrumTypePeer.DIMENSIONALITY,1);
              }
          }
          List<DBSpectrumType> spectrumTypes = DBSpectrumTypePeer.doSelect(crit);
          context.put("spectrumtypes", spectrumTypes);
          context.put("request",runData.getRequest());
          context.put("session",session);
          context.put("link",new BaseJetspeedLink(runData));
          context.put("literaturecomplete",subData.literaturecomplete);
          context.put(NmrshiftdbConstants.JCAMP_STRICT,runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT));
          context.put("baseurl", urltool.getBaseUrl());
          context.put("data",runData);
          //handle the export templates
          File templatedir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/templates/vm/exporttemplates"));
          context.put("templates",templatedir.listFiles());
          TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
          if(tuser.getLiteratureOldSubmit()!=null)
              context.put("recentliteratures","true");
          if(subData.getMolWithH()!=null){
            //If we are not in applet mode, we need image
            if(!(session.getAttribute("applet") !=null && session.getAttribute("applet").equals("applet"))){
              String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "submit1.jpg";
              GeneralUtils.makeJpg(subData.getInputMolAsMdl(null,null,false),"/"+filename,runData.getServletConfig(),300,250,true,subData.getChoosenSpectrumType(),subData.getIdentifiers(), Color.LIGHT_GRAY);
              context.put("image",filename);
            }
            //If we are in level 1 (=structure input) or applet mode, we need the mol file
            if(subData.getLevel()==1 || (session.getAttribute("applet") !=null && session.getAttribute("applet").equals("applet"))){
              if(GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("JCP")){
                  //The mol is put into the MView applet via a file
                  File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/" + System.currentTimeMillis() + "submit.mol"));
                  FileWriter out = new FileWriter(outputFile);
                  out.write(subData.getInputMolAsMdl(null,null,false));
                  out.close();
                  context.put("molfile", "nmrshiftdbhtml/" + outputFile.getName());
              }else{
                  context.put("molfile", GeneralUtils.replace(subData.getInputMolAsMdl(null,null,false),"\n","\\n"));
              }
            }
          }
          context.put("ordercolumn",subData.ordercolumn);
          context.put("updownstring",subData.updown ? "down" : "up");
          context.put("updown", subData.updown);
          context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",runData));
          context.put("tab", "P-Submit");
          context.put("accesscontroller", new TurbineAccessController());
          context.put("tab_smiles", "P-Submit");
          if(runData.getSession().getAttribute("subDataH")!=null)
        	  context.put("chosemnova", true);
      }catch(Throwable ex){
          context.put("error",GeneralUtils.logError(ex,"submit/buildnormalcontext",runData,true));
      }
  }

  /**
   * The action run when shifts are submitted from the peak picker.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitfrompeakpicker(RunData runData, Context context){
      try{
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          HttpServletRequest req = runData.getRequest();
          String peakstring=runData.getParameters().get("pickedpeaks");
          List<CMLPeak> peakList=ParseUtils.parseSpectrumFromSpecFile(peakstring);
          CMLPeakList peaks = new CMLPeakList();
          for(int i=0;i<peakList.size();i++){
                peaks.addPeak(peakList.get(i));         
          }
          subData.setSignalstable(peaks);
          subData.setLevel(3);
          subData.setShowpeakpicker(false);
          if (req.getParameter("spectrumType") != null) {
              Criteria crit = new Criteria();
              crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID, req.getParameter("spectrumType"));
              List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
              subData.setChoosenSpectrumType(v.get(0));
          }
          //At every stage we save the submittingData object to disk
          saveSubdataToDisk(subData, runData);
          context.put("messages","");
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/submitfrompeakpicker",runData,true));
      }
  }

  /**
   * The action for sorting by a column in the table in the overview.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSortcolumn(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      int ordercolumn=-3;
      if(runData.getParameters().get("ordercolumn")!=null){
        ordercolumn=Integer.parseInt(runData.getParameters().get("ordercolumn"));
      }
      subData.ordercolumn=ordercolumn;
      boolean updown=false;
      if(runData.getParameters().get("updown")!=null && runData.getParameters().get("updown").equals("up")){
        updown=true;
      }
      subData.updown=updown;
      context.put("messages","");
  }

  /**
   * Performs the display of the literature form for updating the literature entries of a database.
   * 
   * @param runData
   * @param context
   */
  public void doDatasetliteratureedit(RunData runData, Context context){
     try{
       DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInteger("spectrumid"));
       context.put("editdatasetliterature",true);
       if(spectrum.getLiteraturesNew().size()>0){
           context.put("journalarticle1", spectrum.getLiteraturesNew().get(0));
       }else{
           context.put("journalarticle1", new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE));
       }
       context.put("molecule", spectrum.getDBMolecule());
       context.put("spectrumid",spectrum.getSpectrumId());
       context.put("datasetname", spectrum.getDatasetName());
       Criteria crit=new Criteria();
       crit.addAscendingOrderByColumn(DBJournalPeer.TITLE);
       context.put("existingjournals", DBJournalPeer.doSelect(crit));
       crit=new Criteria();
       crit.addAscendingOrderByColumn(DBPublisherPeer.NAME);
       context.put("existingplacepublishers", DBPublisherPeer.doSelect(crit));
    } catch (Exception ex) {
       context.put("error",GeneralUtils.logError(ex,"submit/submitfrompeakpicker",runData,true));
    }
  }

  /**
   * The action for showing a different set of samples (this is for lab system users only).
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doChangesamples(RunData runData, Context context){
      Date start=new Date();
      start.setDate(Integer.parseInt(runData.getParameters().get("fromday")));
      start.setMonth(Integer.parseInt(runData.getParameters().get("frommonth"))-1);
      start.setYear(Integer.parseInt(runData.getParameters().get("fromyear"))-1900);
      runData.getSession().setAttribute("startsample",start);
      Date end=new Date();
      end.setDate(Integer.parseInt(runData.getParameters().get("today")));
      end.setMonth(Integer.parseInt(runData.getParameters().get("tomonth"))-1);
      end.setYear(Integer.parseInt(runData.getParameters().get("toyear"))-1900);
      runData.getSession().setAttribute("endsample",end);
      context.put("messages","");
  }
  
  /**
   * The action for removing a file from submit.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doRemovefile(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      if(runData.getParameters().getString("type").equals("image"))
          subData.setImage(null, "");
      else if(runData.getParameters().getString("type").equals("rawdata"))
          subData.setRawData(null, "");
      else if(runData.getParameters().getString("type").equals("pdf"))
          subData.setPdf(null);
      else if(runData.getParameters().getString("type").equals("jcamp"))
          subData.setJcamp(null);
      else if(runData.getParameters().getString("type").equals("otherfiles"))
          subData.setOtherFiles(null);
      else if(runData.getParameters().getString("type").equals("cmcse"))
          subData.setCmcseFile(null);
      //At every stage we save the submittingData object to disk
      saveSubdataToDisk(subData, runData);
      context.put("messages","");
  }
  
  public void doChooseidentifiers(RunData runData, Context context){
      try{
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          if(runData.getParameters().getString("identifierset").equals("-1")){
              subData.setSpectrumForIdentifiers(-1);
              for(int i=0;i<subData.getMolWithH().getAtomCount();i++)
                  subData.removeIdentifier(i);
          }else{
              DBMolecule mol=DBMoleculePeer.retrieveByPK(subData.getExistingMoleculeId());
              DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInt("identifierset"));
              subData.setSpectrumForIdentifiers(spectrum.getSpectrumId());
              int i=0;
              for(Object atom : mol.getDBAtoms()){
                  subData.removeIdentifier(i);
                  if(!((DBAtom)atom).getIdentifier(spectrum).equals("")){
                      subData.addIdentifier(i, ((DBAtom)atom).getIdentifier(spectrum));
                  }
                  i++;
              }
          }
          saveSubdataToDisk(subData, runData);
          context.put("messages","");
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/chooseidentifiers",runData,true));
      }
  }

  /**
   * The action for uploading a jcamp file (for peaks, not just as attachment).
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doUploadjcamp(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      HttpServletRequest req = runData.getRequest();
      StringBuffer messages = new StringBuffer();
      String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
      File file=new File(relativepath+"/uploadjcamp"+req.getSession().getId());
      if(file.exists()){
        try{
          BufferedReader rdr = 
                new BufferedReader(
                    new InputStreamReader(new FileInputStream(file),
                                          "ISO-8859-1"));
          StringBuffer dx=new StringBuffer();
          String line="";
          while((line=rdr.readLine())!=null){
              dx.append(line+"\r\n");
          }
          rdr.close();
          file.delete();
          if(runData.getParameters().getString("spectrumType")!=null){
              Criteria crit=new Criteria();
              crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID,runData.getParameters().getString("type"));
              List<DBSpectrumType> v=DBSpectrumTypePeer.doSelect(crit);
              if(v.size()>0){
                  subData.setChoosenSpectrumType(v.get(0));
              }
          }else{
              subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
          }
          subData.setShowpeakpicker(true);
          subData.setJcamp(dx.toString());
          if(runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)).booleanValue()==true)
              GeneralUtils.parseJcamp(subData, dx.toString(),runData, JCAMPReader.STRICT);
          else
              GeneralUtils.parseJcamp(subData, dx.toString(),runData, JCAMPReader.RELAXED);
            saveSubdataToDisk(subData, runData);
        } catch (JCAMPException ex) {
           messages.append("Something was wrong with your file: " + ex.getMessage() + "!");
        } catch (IOException ex) {
            messages.append("Something was wrong when reading your file: " + ex.getMessage() + "!");
        } catch (Throwable ex) {
            messages.append("Something was wrong when reading your file: " + ex.getMessage() + "!");
        }
      }else{
          messages.append("Problems uploading file, sorry");
      }
      context.put("messages",messages);
  }

  /**
   * The action for switching to applet mode.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doApplet(RunData runData, Context context){
      runData.getSession().setAttribute("applet","applet");
      context.put("messages","");
  }
  
  /**
   * The action for switching off applet mode.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doNoapplet(RunData runData, Context context){
      runData.getSession().setAttribute("applet","noapplet");
      context.put("messages","");
  }

  /**
   * The action for submitting the signals.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitspectrum(RunData runData, Context context){
      try {
          SubmittingData subData = (SubmittingData)runData.getSession().getAttribute("subData");
          HttpServletRequest req = runData.getRequest();
          StringBuffer messages = new StringBuffer();
          CMLPeakList oldSignalstable = null;
          if (subData.getSignalstable() != null) {
            oldSignalstable = subData.getSignalstable();
          }
          CMLPeakList numberSpectrum = null;
          if(subData.getMolOriginal()==null)
            subData.setLevel(1);
          else if(!subData.getChoosenSpectrumType().requiresPeaks())
            subData.setLevel(4);
          else
            subData.setLevel(3);
          try {
            if(subData.shiftlist.equals("0")){
              String  spectrum = GeneralUtils.replace(req.getParameter("spectrum"), " ", "");
              if(subData.getChoosenSpectrumType()==null)
                  subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
              if(subData.getChoosenSpectrumType().getDimensionality()==1){
                numberSpectrum = ParseUtils.parseSpectrum(spectrum,null);
                SpectrumUtils.scaleIntensities(numberSpectrum.getPeakElements());
                if (numberSpectrum.getPeakElements().size() == 0) {
                  messages.append("<br>You did not enter any signal!");
                } else {
                  for (int i = numberSpectrum.getPeakElements().size()-1; i>-1;i--) {
                    subData.addToSignalstable(numberSpectrum.getPeakElements().get(i));
                    subData.setKeepAssignmentEntries("");
                  }
                }
              }else if(subData.getChoosenSpectrumType().getDimensionality()==2){
                numberSpectrum = ParseUtils.parse2DSpectrum(spectrum);
                for (int i = numberSpectrum.getPeakElements().size()-1; i>-1;i--) {
                    subData.addToSignalstable(numberSpectrum.getPeakElements().get(i));
                    subData.setKeepAssignmentEntries("");
                }
              }
            }
          } catch (NumberFormatException ex) {
            messages.append("<br>Your input included wrong characters!");
            subData.setKeepAssignmentEntries(req.getParameter("spectrum"));
          }
          if(subData.getChoosenSpectrumType().getDimensionality()==1){
            ParseUtils.removeDuplicates(subData.getSignalstable());
            if (oldSignalstable != null) {
              CMLPeakList signalstable = subData.getSignalstable();
              for (int i = 0; i < signalstable.getPeakElements().size(); i++) {
                for (int k = 0; k < oldSignalstable.getPeakElements().size(); k++) {
                  if (signalstable.getPeakElements().get(i).getXValue() == oldSignalstable.getPeakElements().get(k).getXValue()) {
                    if(oldSignalstable.getPeakElements().get(k).getAtomRefs()!=null){
                      for(int l=0;l<oldSignalstable.getPeakElements().get(k).getAtomRefs().length;l++){
                        if(Arrays.binarySearch(signalstable.getPeakElements().get(i).getAtomRefs(),oldSignalstable.getPeakElements().get(k).getAtomRefs()[l])<0){
                          signalstable.getPeakElements().get(i).setAtomRefs(SubmittingData.addAtomToAtomRefs(signalstable.getPeakElements().get(i).getAtomRefs(),
                          oldSignalstable.getPeakElements().get(k).getAtomRefs()[l]));
                        }
                      }
                    }
                  }
                }
              }
            }
            //If only one shift is given, we assign it to all relevant nuclei
            if(subData.getSignalstable().getPeakElements().size()==1 && subData.getMolWithH()!=null){
              for(int i=0;i<subData.getMolWithH().getAtomCount();i++){
                  if(subData.getMolWithH().getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol()))
                    subData.getSignalstable().getPeakElements().get(0).setAtomRefs(SubmittingData.addAtomToAtomRefs(subData.getSignalstable().getPeakElements().get(0).getAtomRefs(),subData.getMolWithH().getAtom(i)));
              }
            }else if(subData.getSignalstable()!=null && subData.getSignalstable().getPeakChildren().size()>0 && subData.notYetAnyAssignments()){
              try{
                subData.guessAssignment();
              }catch(Exception ex){
                ex.printStackTrace();
              }
            }
          }
          //At every stage we save the submittingData object to disk
          saveSubdataToDisk(subData, runData);
          context.put("messages",messages);
      } catch (Exception ex) {
        context.put("error",GeneralUtils.logError(ex,"submit/submitspectrum",runData,true));
      }
  }
  
  /**
   * Performs the update of the literature entries for a dataset (the submit of the form displayed via doDatasetliteratureedit).
   * 
   * @param runData
   * @param context
   * @throws SQLException 
   */
  public void doSubmitliteratureedit(RunData runData, Context context) throws TorqueException, SQLException{
      Connection dbconn=null;
      Statement stmt=null;
      try{
          dbconn=Torque.getConnection();
          stmt =dbconn.createStatement();
          stmt.executeUpdate("BEGIN");
          StringBuffer messages = new StringBuffer();
          DBLiterature lit = new DBLiterature();
          lit.type = NmrshiftdbConstants.JOURNAL_ARTICLE;
          lit.setAuthor(runData.getParameters().getString("authorjournal1"));
          lit.setTitle(runData.getParameters().getString("titlejournal1"));
          lit.setSubtitle(runData.getParameters().getString("subtitlejournal1"));
          lit.setDoi(runData.getParameters().getString("doijournal1"));
          if (runData.getParameters().getString("etaljournal1") != null && runData.getParameters().getString("etaljournal1").equals(NmrshiftdbConstants.TRUE)) {
            lit.etalprov = true;
          }
          lit.setUrl(runData.getParameters().getString("linkjournal1"));
          try {
            lit.from = Integer.parseInt(runData.getParameters().getString("fromjournal1"));
          } catch (NumberFormatException ex) {
            messages.append("<br>From page must be an integer figure!");
          }
          try {
            lit.to = Integer.parseInt(runData.getParameters().getString("tojournal1"));
          } catch (NumberFormatException ex) {
            messages.append("<br>To page must be an integer figure!");
          }
          if (!runData.getParameters().getString("journaltitlejournal1").equals("")) {
            lit.journaltitle = runData.getParameters().getString("journaltitlejournal1");
            lit.journalsubtitle = runData.getParameters().getString("journalsubtitlejournal1");
            lit.ita = runData.getParameters().getString("itajournal1");
          } else {
            lit.choosenJournal = Integer.parseInt(runData.getParameters().getString("choosejournal1"));
          }
          try {
            if(runData.getParameters().getString("volumejournal1").equals(""))
              lit.volume=-1;
            else
              lit.volume = Integer.parseInt(runData.getParameters().getString("volumejournal1"));
          } catch (NumberFormatException ex) {
            messages.append("<br>Volume of journal entry 1 must be an integer figure!");
          }
          try {
            lit.year = Integer.parseInt(runData.getParameters().getString("yearjournal1"));
          } catch (NumberFormatException ex) {
            messages.append("<br>Year must be an integer figure!");
          }
          try {
            lit.issue = Integer.parseInt(runData.getParameters().getString("issuejournal1"));
          } catch (NumberFormatException ex) {
            messages.append("<br>Issue must be an integer figure!");
          }
          lit.save(-1,dbconn);
		  DBMolecule moleculeToEdit = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInt("spectrumid")).getDBMolecule();
		  String datasetname=runData.getParameters().get("datasetname");
		  //We change the literature entry for all spectra from that dataset
		  for(int i=0;i<moleculeToEdit.getDBSpectrums().size();i++){
			  DBSpectrum spectrum=(DBSpectrum)moleculeToEdit.getDBSpectrums().get(i);
			  if(spectrum.getDatasetName().equals(datasetname)){
				  List<DBSpectrumDBLiterature> speclits = spectrum.getDBSpectrumDBLiteratures();
				  if(speclits.size()>0){
					  for(int k=0;k<speclits.size();k++){
						  DBSpectrumDBLiterature speclit=speclits.get(k);
						  DBSpectrumDBLiteraturePeer.doDelete(speclit,dbconn);
					  }
				  }
				  DBSpectrumDBLiterature speclit=new DBSpectrumDBLiterature();
				  speclit.setDBLiterature(lit);
				  speclit.setDBSpectrum(spectrum);
				  speclit.save(dbconn);
			  }
		  }
		  stmt.executeUpdate("COMMIT");
      } catch (Exception ex) {
          stmt.executeUpdate("ROLLBACK");
          context.put("error",GeneralUtils.logError(ex,"submit/finalsubmit",runData,true));
      }
      finally{
          Torque.closeConnection(dbconn);
      }
  }
    
  /**
   * The action for finally submitting to the database.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doFinalsubmit(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      HttpServletRequest req = runData.getRequest();
      Date now = null;
      TurbineUser user = null;
      //Save the input
      try {
        if(subData.getChoosenSpectrumType().requiresPeaks() && (req.getParameter("deleteunassigned")==null || !req.getParameter("deleteunassigned").equals("deleteunassigned"))){
            for (int i = 0; i < subData.getSignalstable().getPeakElements().size(); i++) {
                if ( subData.getSignalstable().getPeakElements().get(i).getAtomRefs()==null || subData.getSignalstable().getPeakElements().get(i).getAtomRefs().length == 0) {
                  subData.getSignalstable().removeChild(subData.getSignalstable().getPeakElements().get(i));
                }
            }
        }
        //This is the user for which the spectrum is saved. This may not be the same as the logged in user
        int saveUser = 0;
        user = TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
        saveUser = Integer.parseInt(((BaseJetspeedUser) runData.getUser()).getUserId());
        now = new Date();
        String url=req.getRequestURL().toString();
        if (subData.getEditSpectrum() != 0) {
          user = TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
          now=DBSpectrumPeer.retrieveByPK(subData.getEditSpectrum()).getDate();
          saveUser = DBSpectrumPeer.retrieveByPK(subData.getEditSpectrum()).getUserId();
          if(user.isReviewer() && DBSpectrumPeer.retrieveByPK(subData.getEditSpectrum()).getReviewFlag().equals(NmrshiftdbConstants.TRUE))
              subData.setNoreview(true);
        }
        String reviewflag=NmrshiftdbConstants.FALSE;
        if (subData.isNoreview()){
            reviewflag=NmrshiftdbConstants.TRUE;
        }
        if(runData.getParameters().getString("privatesubmit","false").equals("true")){
            reviewflag=NmrshiftdbConstants.PRIVATE;
            subData.setNoreview(true);
        }
        /*if(runData.getParameters().getString("userobot","false").equals("true")){
            subData.setSendCsearch(true);
        }*/
        subData.setSendCsearch(false);
        subData.setReviewFlag(reviewflag);
        subData.setThisDate(now);
        subData.setThisURL(url);
        subData.setThisUserId(saveUser);
        subData.setSendEmailUserId(((BaseJetspeedUser) runData.getUser()).getUserId());
        String subDataXML = subData.data.toXML();
        String subDataFile = ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/xml/" + subData.getThisDate().getTime() + "_" + subData.getThisUserId() + ".xml");
        FileWriter out = new FileWriter(subDataFile);
        out.write(subDataXML);
        out.close();
        now=new Date();
        if (runData.getSession().getAttribute("contributoredit") == null){
            TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
            if(tuser.getBrokensubmit() != null && subData.getEditSpectrum()==0) {
              tuser.setBrokensubmit(null);
              try {
                tuser.save();
              } catch (Exception ex) {
                GeneralUtils.logError(ex, "deleting broken submit", runData,true);
              }
            }
            if(tuser.getBrokenEdit() != null && subData.getEditSpectrum()!=0) {
              tuser.setBrokenEdit(null);
              try {
                tuser.save();
              } catch (Exception ex) {
                GeneralUtils.logError(ex, "deleting broken submit", runData,true);
              }
            }
        }
        //We need to remove some flags
        if (runData.getSession().getAttribute("contributoredit") != null) {
            runData.getSession().removeAttribute("contributoredit");
        }
        if(runData.getParameters().get("morespectra")!=null && runData.getParameters().get("morespectra").equals("true")){
            if(runData.getSession().getAttribute("topspinfile")!=null){
            	doSubmittopspin(runData, context);
            }else{
	            SubmittingData subDataNew=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	            subDataNew.setMol(subData.getMolOriginal(), false, false, true);
	            subDataNew.setChemName(subData.getChemName());
	            subDataNew.setMoleculeKeywords(subData.getMoleculeKeywords());
	            subDataNew.setAutonomeName(subData.getAutonomeName());
	            subDataNew.setMoleculeLinks(subData.getMoleculeLinks());
	            subDataNew.setSmilesChiralProv(subData.getSmilesChiralProv());
	            subDataNew.setSmilesNonChiralProv(subData.getSmilesNonChiralProv());
	            subDataNew.setIsChiralProv(subData.getIsChiralProv());
	            subDataNew.setCasNumber(subData.getCasNumber());
	            subDataNew.setConditions(subData.getConditions());
	            subDataNew.setComment(subData.getComment());
	            subDataNew.setLiterature(subData.getLiterature());
	            subDataNew.setSpectrumKeywords(subData.getSpectrumKeywords());
	            subDataNew.setSpectrumLinks(subData.getSpectrumLinks());
	            runData.getSession().removeAttribute("subData");
	            subDataNew.setLevel(6);
	            runData.getSession().setAttribute("subData", subDataNew);
            }
        }else{
            runData.getSession().removeAttribute("subData");
            if(runData.getSession().getAttribute("otherSubData")!=null)
				context.put("otherquickcheck", "true");
            context.put("finished","true");
        }
        context.put("messages","");
        context.put("reviewflag", subData.getReviewFlag());
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/finalsubmit",runData,true));
      }      
  }

  /**
   * The action which is called if the user submtted from quick check and wants to submit the second spectrum.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doOtherquickcheck(RunData runData, Context context){
    try {
	    runData.getSession().setAttribute("subData", runData.getSession().getAttribute("otherSubData"));
	    runData.getSession().removeAttribute("otherSubData");
	    SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
	    subData.setLevel(3);
	    StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), subData.getIsDoubleBondSpecified(), runData.getServletConfig(), subData.getInputMolAsMdl(null,null,false), ssb);
		subData.setSmilesNonChiralProv(ssb.string1);
		subData.setSmilesChiralProv(ssb.string2);
		subData.setIsChiralProv(ssb.bool);	  
        context.put("messages", new StringBuffer());
        saveSubdataToDisk(subData, runData);
  	} catch (Exception e) {
        context.put("error",GeneralUtils.logError(e,"submit/otherqc",runData,true));
	}
  }
  
  /**
   * The action for switching to input all types of literature data.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doLiteraturecomplete(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      subData.literaturecomplete="true";
      context.put("messages","");
  }

  /**
   * Attaches files to the submit.
   * 
   * @param runData The current RunData object.
   * @param context The current context.
   */
  public void doAttachfiles(RunData runData, Context context) {
      try{
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          HttpServletRequest req = runData.getRequest();
          StringBuffer messages = new StringBuffer();
          //These are the four file uploads which are possible
          String[] files={"attachjcamp","attachpdf","attachraw","attachimage","attachotherfiles"};
          for(int i=0;i<files.length;i++){
              String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
              File file=new File(relativepath+"/"+files[i]+req.getSession().getId());
              if(!file.exists()){
                if (files[i].equals("attachjcamp") && runData.getParameters().getString("removejcamp","").equals("removejcamp")) {
                    subData.setJcamp("");
                }else if(files[i].equals("attachraw") && runData.getParameters().getString("removeraw","").equals("removeraw")){
                    subData.setRawData(null,"");
                }else if(files[i].equals("attachpdf") && runData.getParameters().getString("removepdf","").equals("removepdf")){
                    subData.setPdf(null);
                }else if(files[i].equals("attachotherfiles") && runData.getParameters().getString("removeotherfiles","").equals("removeotherfiles")){
                    subData.setOtherFiles(null);
                }else if(runData.getParameters().getString("removeimage","").equals("removeimage")){
                    subData.setImage(null,"");
                }
              }else{
                  if(file.length()>30*1024*1024){//files larger 30mb cannot be attached
                      messages.append("Your "+files[i]+" file is larger than 30 MB, this is the limit for file attachments!");
                      file.delete();
                  }else{
                      if (files[i].equals("attachjcamp")) {
                        try{
                          BufferedReader rdr = 
                                new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file),
                                                          "ISO-8859-1"));
                          StringBuffer dx=new StringBuffer();
                          String line="";
                          while((line=rdr.readLine())!=null){
                              dx.append(line+"\r\n");
                          }
                          rdr.close();
                          file.delete();
                          //we do not need to parse thefile herer
                          //GeneralUtils.parseJcamp(subData, dx.toString(),runData, JCAMPReader.RELAXED);
                          subData.setJcamp(dx.toString());
                          saveSubdataToDisk(subData, runData);
                        } catch (IOException ex) {
                            messages.append("Something was wrong when reading your file: " + ex.getMessage() + "!");
                        } catch (Throwable ex) {
                            messages.append("Something was wrong when reading your file: " + ex.getMessage() + "!");
                        }
                      }else{
                          int BUFFER_SIZE = 500 * 1024;  
                          ByteArrayOutputStream bao = new ByteArrayOutputStream (BUFFER_SIZE);  
                          byte[] buffer = new byte[BUFFER_SIZE];  
                          BufferedInputStream bi = new BufferedInputStream (new FileInputStream (file));  
                          while (true) {  
                              int bytesRead = bi.read(buffer);  
                              if (bytesRead == -1) {  
                                  break;  
                              } else {  
                                  bao.write(buffer, 0, bytesRead);  
                              }  
                          }  
                          bi.close();   
                          file.delete();
                          if(files[i].equals("attachraw"))
                              subData.setRawData(bao.toByteArray(),runData.getParameters().get("formatattachraw"));
                          else if(files[i].equals("attachpdf"))
                              subData.setPdf(bao.toByteArray());
                          else if(files[i].equals("attachotherfiles"))
                              subData.setOtherFiles(bao.toByteArray());
                          else
                              subData.setImage(bao.toByteArray(),runData.getParameters().get("formatattachimage").indexOf('.')==-1 ? runData.getParameters().get("formatattachimage") : runData.getParameters().get("formatattachimage").substring(runData.getParameters().get("formatattachimage").indexOf('.')+1));
                          saveSubdataToDisk(subData, runData);
                      }
                  }
              }
          }
          if(messages.toString().equals("")){
              subData.setLevel(7);
              doChecksFinish(messages, subData);
          }else{
              subData.setLevel(-1);
          }
          context.put("messages",messages);
          saveSubdataToDisk(subData, runData);
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/finalsubmit",runData,true));
      }      
  }
  
  /**
   * The action for submitting the miscellaneous data.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitmiscdata(RunData runData, Context context){
      try {
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          HttpServletRequest req = runData.getRequest();
          StringBuffer messages = new StringBuffer();
          if (req.getParameter("chemName") != null) {
            subData.setChemName(runData.getParameters().getString("chemName"));
          }
          if (req.getParameter("moleculelinks") != null) {
            subData.setMoleculeLinks(runData.getParameters().getString("moleculelinks"));
          }
          boolean lastWasEnd = true;
          for (int i = 0; i < subData.getMoleculeLinks().length(); i++) {
            if (subData.getMoleculeLinks().charAt(i) != '\r') {
              if (lastWasEnd && subData.getMoleculeLinks().charAt(i) == '\n') {
                messages.append("<br>Your links for the molecule contains an empty line!");
              }
              lastWasEnd = subData.getMoleculeLinks().charAt(i) == '\n';
            }
          }
          if (req.getParameter("casnumber") != null) {
            subData.setCasNumber(runData.getParameters().getString("casnumber"));
          }
          subData.setSpectrumLinks(runData.getParameters().getString("spectrumlinks"));
          if(subData.getSpectrumLinks()==null)
          subData.setSpectrumLinks("");
          //TODO indexof not working if keyword starts with antoher keyword
          String[] moleculeKeywords = req.getParameterValues("moleculekeywords");
          if (moleculeKeywords != null) {
            for (int i = 0; i < moleculeKeywords.length; i++) {
              if (!moleculeKeywords[i].equals("none") && subData.getMoleculeKeywords().indexOf(moleculeKeywords[i].trim())==-1) {
                subData.setMoleculeKeywords(subData.getMoleculeKeywords()+"\r\n"+moleculeKeywords[i].trim());
              }
            }
          }
          String[] spectrumKeywords = req.getParameterValues("spectrumkeywords");
          if (spectrumKeywords != null) {
            for (int i = 0; i < spectrumKeywords.length; i++) {
              if (!spectrumKeywords[i].equals("none") && subData.getSpectrumKeywords().indexOf(spectrumKeywords[i].trim())==-1) {
                subData.setSpectrumKeywords(subData.getSpectrumKeywords()+"\r\n"+spectrumKeywords[i].trim());
              }
            }
          }
          lastWasEnd = true;
          for (int i = 0; i < subData.getMoleculeLinks().length(); i++) {
            if (subData.getMoleculeLinks().charAt(i) != '\r') {
              if (lastWasEnd && subData.getMoleculeLinks().charAt(i) == '\n') {
                messages.append("<br>Your links for the spectrum contains an empty line!");
              }
              lastWasEnd = subData.getMoleculeLinks().charAt(i) == '\n';
            }
          }
          if (req.getParameter("textmoleculekeywords") != null) {
            subData.setMoleculeKeywords(runData.getParameters().getString("textmoleculekeywords"));
          }
          subData.setSpectrumKeywords(runData.getParameters().getString("textspectrumkeywords"));
          if (req.getParameter("autonomename") != null) {
            subData.setAutonomeName(runData.getParameters().getString("autonomename"));
          }
          if(req.getParameter("comment")!=null)
              subData.setComment(runData.getParameters().getString("comment"));
          char measuredorcalculated = 'm';
          boolean changed=false;
          if (req.getParameter("radiomeasured").equals("calculated")) {
            measuredorcalculated = 'c';
          }
          if (measuredorcalculated != subData.getMeasuredOrCalculated()) {
              //Change the flag
              if (measuredorcalculated == 'c') {
                subData.setCalculated();
              } else {
                subData.setMeasured();
              }
              changed=true;
          }else{
              List<DBConditionType> conds = subData.getConditions();
              for (int i = 0; i < conds.size(); i++) {
                conds.get(i).setCurrentValue(runData.getParameters().getString(conds.get(i).getConditionName()), subData.getChoosenSpectrumType(), true);
                //Assignment method requies special handling on lab server
                if (conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals("")) {
                    if(conds.get(i).getConditionName().equals("Assignment Method") && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()!=null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getLabgroupId()>0){
                        List<DBExperiment> experiments = DBExperimentPeer.doSelect(new Criteria());
                        StringBuffer method=new StringBuffer();
                        for(int k=0;k<experiments.size();k++){
                            DBExperiment currentExperiment = experiments.get(k);
                            if(runData.getParameters().get("experiment"+currentExperiment.getExperimentId())!=null && runData.getParameters().get("experiment"+currentExperiment.getExperimentId()).equals("experiment"+currentExperiment.getExperimentId())){
                                method.append(currentExperiment.getName()+"; ");
                            }
                        }
                        if(method.toString().equals("")){
                            messages.append("<br>You did not enter a value for  " + conds.get(i).getConditionName() + ". Do that !");
                        }else{
                            conds.get(i).setCurrentValue(GeneralUtils.removeLastComma(method).toString());
                        }                   
                    }else{
                      try {
                        if (runData.getParameters().getString("existing" + conds.get(i).getConditionName() + "conditions") == null) {
                          messages.append("<br>You did not enter a value for  " + conds.get(i).getConditionName() + ". Do that !");
                        } else {
                          conds.get(i).setCurrentValue(DBConditionPeer.retrieveByPK(new NumberKey(runData.getParameters().getString("existing" + conds.get(i).getConditionName() + "conditions"))).getValue(), subData.getChoosenSpectrumType(), true);
                        }
                      } catch (NumberFormatException ex) {
                        context.put("error",GeneralUtils.logError(ex, "A cast error occured when trying to make an integer out of the value of one of the conditions drop down boxes", runData,true));
                      }
                    }
                }
                if (!conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals(NmrshiftdbConstants.UNREPORTED) && !conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals(NmrshiftdbConstants.UNKNOWN) && !conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals("") && conds.get(i).getDataType().equals("int")) {
                  try {
                    new Integer(conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()));
                  } catch (NumberFormatException ex) {
                    messages.append("<br>Your  " + conds.get(i).getConditionNameInput() + " is not an integer figure as required. Correct it!");
                  }
                }
                if (!conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals(NmrshiftdbConstants.UNREPORTED) && !conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals(NmrshiftdbConstants.UNKNOWN) && !conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()).equals("") && conds.get(i).getDataType().equals("float")) {
                  try {
                    new Float(conds.get(i).getCurrentValue(subData.getChoosenSpectrumType()));
                  } catch (NumberFormatException ex) {
                    messages.append("<br>Your  " + conds.get(i).getConditionName() + " is not a floating point figure as required. Correct it!");
                  }
                }
              }
              subData.setConditions(conds);
          }
          if (messages.toString().equals("") && !changed) {
            //go back to level literature input
              subData.setLevel(0);
          }
          saveSubdataToDisk(subData, runData);
          context.put("messages",messages);
        } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/submitmiscdata",runData,true));
        }
  }
  
  /**
   * The action for submitting the assignment.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitassignment(RunData runData, Context context){
      try {
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          HttpServletRequest req = runData.getRequest();
          StringBuffer messages = new StringBuffer();
          if(subData.getMolOriginal()!=null){
              subData.assignmentGuessed=false;
              IAtomContainer molWithH = subData.getMolWithH();
              int highestNonHAtom = 0;
              for (int i = molWithH.getAtomCount() - 1; i > -1; i--) {
                  if (!molWithH.getAtom(i).getSymbol().equals("H")) {
                      highestNonHAtom = i;
                      break;
                  }
              }
              int[] usedAtomNumbers = new int[highestNonHAtom + 1];
              for(int i=0;i<usedAtomNumbers.length;i++)
                  usedAtomNumbers[i]=-1;
              boolean illegalCharacter=false;
              //We empty the assignment ArrayLists in the valueTriples - there may be some in case the user corrected spectrum or molecule, but we build the assignments totally new
              subData.clearAssignments();
              if (!subData.getChoosenSpectrumType().getElementSymbol().equals("H")) {
                  for (int i = 0; i < molWithH.getAtomCount(); i++) {
                      if (molWithH.getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getElementSymbol())) {
                          if(subData.shiftlist.equals("1")){
                              if(!req.getParameter("atomSelect" + i).equals("")){
                                  try{
                                      double signal = Double.parseDouble(req.getParameter("atomSelect" + i));
                                      double intensity = 0;
                                      if(!req.getParameter("intensity" + i).equals("")){
                                          intensity = Double.parseDouble(req.getParameter("intensity" + i));
                                      }
                                      CMLPeak peak=new CMLPeak();
                                      peak.setXValue(signal);
                                      peak.setYValue(intensity);
                                      peak.setAtomRefs(new String[]{molWithH.getAtom(i).getID()});
                                      subData.addToSignalstable(peak);
                                  }catch(Exception ex){
                                      messages.append("<br>Shift or intensity for atom "+(i+1)+" is not a figure or empty!");
                                      illegalCharacter=true;
                                  }
                              }
                          }else{
                              int signal = (int)Float.parseFloat(req.getParameter("atomSelect" + i));
                              if (signal != 32767) {
                                  subData.getSignalstable().getPeakElements().get(signal - 1).setAtomRefs(SubmittingData.addAtomToAtomRefs(subData.getSignalstable().getPeakElements().get(signal - 1).getAtomRefs(),molWithH.getAtom(i)));
                              }
                          }
                      }
                      if (i <= highestNonHAtom && req.getParameter("identifier" + i) != null){
                          if(!req.getParameter("identifier" + i).equals("")) {
                              subData.addIdentifier(new Integer(i), runData.getParameters().getString("identifier" + i));
                          }
                      }
                  }
              } else {
                  for (int i = 0; i < molWithH.getAtomCount(); i++) {
                      int numberOfHs = 0;
                      int numberOfH1s = 0;
                      List<IAtom> atoms = molWithH.getConnectedAtomsList(molWithH.getAtom(i));
                      for (int k = 0; k < atoms.size(); k++) {
                          if (atoms.get(k).getSymbol().equals("H")) {
                              numberOfHs++;
                              if (atoms.get(k).getMassNumber()==null || atoms.get(k).getMassNumber()==0 || atoms.get(k).getMassNumber()==1) {
                                  numberOfH1s++;
                              }
                          }
                      }
                      if (numberOfH1s != 0) {
                          if (numberOfHs != 2 || numberOfH1s != 2) {
                              if(subData.shiftlist.equals("1")){
                                  if(!req.getParameter("atomSelect" + i).equals("")){
                                      try{
                                          double signal = Double.parseDouble(req.getParameter("atomSelect" + i));
                                          double intensity = 0;
                                          if(!req.getParameter("intensity" + i).equals("")){
                                              intensity = Double.parseDouble(req.getParameter("intensity" + i));
                                          }
                                          CMLPeak vt=new CMLPeak();
                                          vt.setXValue(signal);
                                          vt.setYValue(intensity);
                                          String[] atomrefs=new String[atoms.size()];
                                          for(int k=0;k<atomrefs.length;k++)
                                              atomrefs[k]=atoms.get(k).getID();
                                          vt.setAtomRefs(atomrefs);
                                          subData.addToSignalstable(vt);
                                      }catch(Exception ex){
                                          messages.append("<br>Shift or intensity for atom "+(i+1)+" is not a figure or empty!");
                                          illegalCharacter=true;
                                      }
                                  }
                              }else{
                                  if(req.getParameter("atomSelect" + i)!=null){
                                      int signal = new Integer(req.getParameter("atomSelect" + i)).intValue();
                                      if (signal != 32767) {
                                          for (int k = 0; k < atoms.size(); k++) {
                                              if (atoms.get(k).getSymbol().equals("H")) {
                                                  subData.getSignalstable().getPeakElements().get(signal - 1).setAtomRefs(SubmittingData.addAtomToAtomRefs(subData.getSignalstable().getPeakElements().get(signal - 1).getAtomRefs(),atoms.get(k)));
                                              }
                                          }
                                      }
                                  }
                              }
                          } else {
                              if(subData.shiftlist.equals("1")){
                                  if(!req.getParameter("atomSelect" + i + "001").equals("")){
                                      try{
                                          double signal = Double.parseDouble(req.getParameter("atomSelect" + i + "001"));
                                          double intensity = 0;
                                          if(!req.getParameter("intensity" + i + "001").equals("")){
                                              intensity = Double.parseDouble(req.getParameter("intensity" + i + "001"));
                                          }
                                          CMLPeak vt=new CMLPeak();
                                          vt.setXValue(signal);
                                          vt.setYValue(intensity);
                                          for (int m = 0; m < atoms.size(); m++) {
                                              if (atoms.get(m).getSymbol().equals("H")) {
                                                  vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(),atoms.get(m)));
                                                  break;
                                              }
                                          }
                                          subData.getSignalstable().appendChild(vt);
                                      }catch(Exception ex){
                                          messages.append("<br>Shift or intensity for atom "+(i+1)+" is not a figure or empty!");
                                          illegalCharacter=true;
                                      }
                                  }
                                  if(!req.getParameter("atomSelect" + i + "002").equals("")){
                                      try{
                                          double signal = Double.parseDouble(req.getParameter("atomSelect" + i + "002"));
                                          double intensity = 0;
                                          if(!req.getParameter("intensity" + i + "002").equals("")){
                                              intensity = Double.parseDouble(req.getParameter("intensity" + i + "002"));
                                          }
                                          CMLPeak vt=new CMLPeak();
                                          vt.setXValue(signal);
                                          vt.setYValue(intensity);
                                          boolean alreadyOnePassed = false;
                                          for (int m = 0; m < atoms.size(); m++) {
                                              if (atoms.get(m).getSymbol().equals("H")) {
                                                  if (!alreadyOnePassed) {
                                                      alreadyOnePassed = true;
                                                  } else {
                                                      vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(),atoms.get(m)));
                                                      break;
                                                  }
                                              }
                                          }
                                          subData.getSignalstable().appendChild(vt);
                                      }catch(Exception ex){
                                          messages.append("<br>Shift or intensity for atom "+(i+1)+" is not a figure or empty!");
                                          illegalCharacter=true;
                                      }
                                  }
                              }else{
                                  if(req.getParameter("atomSelect" + i + "001")!=null && req.getParameter("atomSelect" + i + "001")!=null){
                                      int signal1 = (int)Float.parseFloat(req.getParameter("atomSelect" + i + "001"));
                                      int signal2 = (int)Float.parseFloat(req.getParameter("atomSelect" + i + "002"));
                                      boolean alreadyOnePassed = false;
                                      for (int m = 0; m < atoms.size(); m++) {
                                          if (atoms.get(m).getSymbol().equals("H")) {
                                              if (!alreadyOnePassed) {
                                                  if (signal1 != 32767) {
                                                      subData.getSignalstable().getPeakElements().get(signal1 - 1).setAtomRefs(SubmittingData.addAtomToAtomRefs(subData.getSignalstable().getPeakElements().get(signal1 - 1).getAtomRefs(),atoms.get(m)));
                                                  }
                                                  alreadyOnePassed = true;
                                              } else {
                                                  if (signal2 != 32767) {
                                                      subData.getSignalstable().getPeakElements().get(signal2 - 1).setAtomRefs(SubmittingData.addAtomToAtomRefs(subData.getSignalstable().getPeakElements().get(signal2 - 1).getAtomRefs(),atoms.get(m)));
                                                  }
                                                  break;
                                              }
                                          }
                                      }
                                  }
                              }
                          }
                      }
                      if (i <= highestNonHAtom && (req.getParameter("identifier" + i) != null || req.getParameter("identifier" + i + "001") != null)) {
                          if (numberOfH1s != 2 && req.getParameter("identifier" + i) != null) {
                              if(!req.getParameter("identifier" + i).equals("")){
                                  subData.addIdentifier(new Integer(i), runData.getParameters().getString("identifier" + i));
                              }
                          } else if(req.getParameter("identifier" + i + "001") != null){
                              if(!req.getParameter("identifier" + i + "001").equals("")){
                                  subData.addIdentifier(new Integer(i), runData.getParameters().getString("identifier" + i + "001"));
                              }
                          }
                      }
                  }
              }
              boolean newlevel=false;
              if(!runData.getParameters().getString("couplings").equals("true")){
                  newlevel=true;
              }
              if(newlevel && !illegalCharacter)
                  subData.setLevel(4);
              else
                  subData.setLevel(3);
              //Read multiplicity/coupling stuff
              for (int i = 0; i < subData.getMolWithH().getAtomCount(); i++) {
                  int atomNumber = i;
                  if(runData.getParameters().get("multi"+i)!=null){
                      AtomUtils.setChildElement(subData.getCMLMolecule().getAtom(atomNumber), SubmittingData.MULTIPLICITY, runData.getParameters().get("multi"+i));
                  }
                  if(subData.enterconstants){
                      while(subData.getCMLMolecule().getAtoms().get(atomNumber).getChildElements(SubmittingData.COUPLING,"http://www.nmrshiftdb.org/").size()>0){
                          subData.getCMLMolecule().getAtoms().get(atomNumber).removeChild(subData.getCMLMolecule().getAtoms().get(i).getChildElements(SubmittingData.COUPLING,"http://www.nmrshiftdb.org/").get(0));
                      }
                      for(int k=0;k<subData.getMolWithH().getAtomCount();k++){
                          if(runData.getParameters().get("coup"+i+""+k)!=null){
                              String coup=runData.getParameters().get("coup"+i+""+k).replace(',','.');
                              if(!coup.equals("")){
                                  int otherAtomNumber=k;
                                  try{
                                      Float.parseFloat(coup);
                                      Element coupling = new Element(SubmittingData.COUPLING,"http://www.nmrshiftdb.org/");
                                      coupling.addAttribute(new Attribute("otherAtom",otherAtomNumber+""));
                                      coupling.addAttribute(new Attribute("value",coup));
                                      subData.getCMLMolecule().getAtoms().get(atomNumber).appendChild(coupling);
                                  }catch(NumberFormatException ex){
                                      messages.append("<br>Coupling "+(atomNumber+1)+"/"+(otherAtomNumber+1)+" is not a figure!");
                                  }
                              }
                          }
                      }
                  }
              }
              if(runData.getParameters().getString("couplings").equals("true")){
                  subData.enterconstants=true;
                  subData.setLevel(3);
              }
              saveSubdataToDisk(subData, runData);
              subData.shiftlist="0";
              runData.getSession().removeAttribute("shifts");
          }else{
              messages.append("You need to enter a structure before doing the assignment!<br>");
          }
          context.put("messages",messages);
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/submitassignments",runData,true));
      }
  }

  public void doResolvedoi(RunData runData, Context context){
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      StringBuffer messages = new StringBuffer();
      HttpURLConnection urlConnection=null;
      try {
    	  URL url = new URL("https://api.crossref.org/works/"+runData.getParameters().get("doijournal"+runData.getParameters().get("literatureid")));
    	  urlConnection = (HttpURLConnection)  url.openConnection();
          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          String page = IOUtils.toString(in, "UTF-8")+"<S";
          ObjectMapper mapper = new ObjectMapper();
          JsonNode node = mapper.readTree(page);
          DBLiterature literature=null;
          List<DBLiterature> oldLits = subData.getLiterature();
          if(runData.getParameters().get("literatureid").equals("1") && subData.getLiterature().size()>0){
              literature = oldLits.get(0);
          }
          if(runData.getParameters().get("literatureid").equals("2") && subData.getLiterature().size()>1){
              literature = oldLits.get(1);
          }
          if(literature==null){
              literature = new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE);
              oldLits.add(literature);
          }
          literature.journaltitle=node.at("/message/container-title").get(0).asText();
       	  literature.setTitle(node.at("/message/title").get(0).asText());
          String pages=node.at("/message/page").asText();
          StringTokenizer st=new StringTokenizer(pages,"-");
          if(st.hasMoreElements())
        	  literature.from=Integer.parseInt(st.nextToken());
          if(st.hasMoreElements())
        	  literature.to=Integer.parseInt(st.nextToken());
          StringBuffer authors=new StringBuffer();
          for(JsonNode authornode : node.at("/message/author")){
        	  authors.append(authornode.at("/given").asText());
        	  authors.append(" ");
        	  authors.append(authornode.at("/family").asText());
        	  authors.append(";");
          }
          literature.setAuthor(GeneralUtils.removeLastComma(authors).toString());
          literature.setDoi(runData.getParameters().get("doijournal"+runData.getParameters().get("literatureid")));
          String year=node.at("/message/published-print/date-parts").get(0).get(0).asText();
    	  literature.year=Integer.parseInt(year);
          String volume=node.at("/message/volume").asText();
       	  literature.volume=Integer.parseInt(volume);
          subData.setLiterature(oldLits);
          //we handle a submit from the edit literature for dataset
          if(runData.getParameters().get("datasetname")!=null){
              context.put("journalarticle1", subData.getLiterature().get(0));
              DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInteger("spectrumid"));
              context.put("editdatasetliterature",true);
              context.put("molecule", spectrum.getDBMolecule());
              context.put("spectrumid",spectrum.getSpectrumId());
              context.put("datasetname", spectrum.getDatasetName());
              Criteria crit=new Criteria();
              crit.addAscendingOrderByColumn(DBJournalPeer.TITLE);
              context.put("existingjournals", DBJournalPeer.doSelect(crit));
              crit=new Criteria();
              crit.addAscendingOrderByColumn(DBPublisherPeer.NAME);
              context.put("existingplacepublishers", DBPublisherPeer.doSelect(crit));
          }
      }catch(Exception ex){
          GeneralUtils.logError(ex, "DOI resolution", runData, false);
          messages.append("Sorry, DOI resolution for "+runData.getParameters().get("doijournal"+runData.getParameters().get("literatureid"))+" not possible. Please enter data manually!");
      } finally {
          urlConnection.disconnect();
      }
      saveSubdataToDisk(subData, runData);
      context.put("messages",messages);
  }

  /**
   * The action for submitting the spectrum type.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitspectrumtype(RunData runData, Context context){
      try{
          HttpServletRequest req = runData.getRequest();
    	  if(runData.getSession().getAttribute("subDataH")!=null){
    		  //we have 13c chosen
    		  if(req.getParameter("spectrumType").equals("2")){//TODO not hard coded
    			  runData.getSession().setAttribute("subData",runData.getSession().getAttribute("subDataH"));
    		  }else if(runData.getParameters().getInt("spectrumType")>11){//TODO not hard coded
    			  Map<String,SubmittingData> otherspectra = (Map<String,SubmittingData>)runData.getSession().getAttribute("otherspectra");
    			  for(String key : otherspectra.keySet()){
    				  if(otherspectra.get(key).getChoosenSpectrumType()!=null && otherspectra.get(key).getChoosenSpectrumType().getSpectrumTypeId()==runData.getParameters().getInt("spectrumType")){
    					  runData.getSession().setAttribute("subData",otherspectra.get(key));  
    				  }
    			  }
    		  }
			  runData.getSession().removeAttribute("subDataH");
			  runData.getSession().removeAttribute("otherspectra");
    	  }else{
	          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
	          if (req.getParameter("spectrumType") != null) {
	              Criteria crit = new Criteria();
	              crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID, req.getParameter("spectrumType"));
	              List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
	              subData.setChoosenSpectrumType(v.get(0));
	          }
    	  }
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          if(!subData.getChoosenSpectrumType().requiresPeaks())
              subData.setLevel(4);
          else
              subData.setLevel(2);
          //At every stage we save the submittingData object to disk
          saveSubdataToDisk(subData, runData);
          context.put("messages","");
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/submitspectrumtype",runData,true));
      }
  }
  
  /**
   * The action for starting an assignment from an experiment record.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doAssignrawdata(RunData runData, Context context) {
      try{
        SubmittingData subData =  new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
        runData.getSession().setAttribute("subData", subData);
        Criteria crit=new Criteria();
        crit.add(DBRawFilePeer.RAW_FILE_ID,runData.getParameters().get("id"));
        DBRawFile rawfile=((DBRawFile)DBRawFilePeer.doSelect(crit).get(0));
        subData.rawfile=rawfile;
        DBSample sample=DBSamplePeer.retrieveByPK(rawfile.getSampleId());
        if(sample.getProbableStructure()!=0){
            sample.getDBMolecule().addValuesToSubmittingData(subData,null,false, true);
            subData.setLevel(6);
        }else{
            subData.setLevel(1);
        }
        subData.setSpectrumKeywords(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getDBLabGroup().getLabgroupName());
        subData.fullfillsOrder=Integer.parseInt(runData.getParameters().get("id"));
        subData.setNoreview(true);
        List<DBConditionType> conds = subData.getConditions();
        for(int i=0;i<conds.size();i++){
            DBConditionType cond=conds.get(i);
            if(cond.getConditionTypeId()==2){
                //solvent
                cond.setCurrentValue(sample.getDBCondition().getValue());
            }else if(cond.getConditionTypeId()==3){
                //frequency
                conds.get(i).setCurrentValue(sample.getDBMachine().getDBCondition().getValue(),subData.getChoosenSpectrumType(), true);
            }else if(cond.getConditionTypeId()==11){
                //assignment method
                StringBuffer method=new StringBuffer();
                for(int k=0;k<sample.getDBExperimentDBSamples().size();k++){
                    if(k>0)
                        method.append(", ");
                    method.append(((DBExperimentDBSample)sample.getDBExperimentDBSamples().get(k)).getDBExperiment().getName());
                }
                conds.get(i).setCurrentValue(method.toString(),subData.getChoosenSpectrumType(), true);
            }else{
                conds.get(i).setCurrentValue("Unreported");
            }
        }
        subData.setConditions(conds);
        context.put("messages","");
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/assignrawdata",runData,true));
      }
    }

  /**
   * The action for submitting the literature data.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitliterature(RunData runData, Context context){
      try {
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          StringBuffer messages = new StringBuffer();
          subData.getLiterature().clear();
          List<DBLiterature> literatures = new ArrayList<DBLiterature>();
          for (int i = 1; i < 3; i++) {
            DBLiterature lit = null;
            if(subData.literaturecomplete.equals("true") || (i==1)){
              if (!runData.getParameters().getString("titlejournal" + i).equals("") || !runData.getParameters().getString("authorjournal" + i).equals("")) {
                lit = new DBLiterature();
                lit.type = NmrshiftdbConstants.JOURNAL_ARTICLE;
                lit.setAuthor(runData.getParameters().getString("authorjournal" + i));
                lit.setTitle(runData.getParameters().getString("titlejournal" + i));
                lit.setSubtitle(runData.getParameters().getString("subtitlejournal" + i));
                lit.setDoi(runData.getParameters().getString("doijournal" + i));
                if (runData.getParameters().getString("etaljournal" + i) != null && runData.getParameters().getString("etaljournal" + i).equals(NmrshiftdbConstants.TRUE)) {
                  lit.etalprov = true;
                }
                lit.setUrl(runData.getParameters().getString("linkjournal" + i));
                try {
                  lit.from = Integer.parseInt(runData.getParameters().getString("fromjournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>From page of journal entry " + i + " must be an integer figure!");
                }
                try {
                  lit.to = Integer.parseInt(runData.getParameters().getString("tojournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>To page of journal entry " + i + " must be an integer figure!");
                }
                if (!runData.getParameters().getString("journaltitlejournal" + i).equals("")) {
                  lit.journaltitle = runData.getParameters().getString("journaltitlejournal" + i);
                  lit.journalsubtitle = runData.getParameters().getString("journalsubtitlejournal" + i);
                  lit.ita = runData.getParameters().getString("itajournal" + i);
                } else {
                  lit.choosenJournal = Integer.parseInt(runData.getParameters().getString("choosejournal" + i));
                }
                try {
                  if(runData.getParameters().getString("volumejournal" + i).equals(""))
                    lit.volume=-1;
                  else
                    lit.volume = Integer.parseInt(runData.getParameters().getString("volumejournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>Volume of journal entry " + i + " must be an integer figure!");
                }
                try {
                  lit.year = Integer.parseInt(runData.getParameters().getString("yearjournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>Year of journal entry " + i + " must be an integer figure!");
                }
                try {
                  lit.issue = Integer.parseInt(runData.getParameters().getString("issuejournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>Issue of journal entry " + i + " must be an integer figure!");
                }
                literatures.add(lit);
              }
            }
            if(subData.literaturecomplete.equals("true")){
              if (!runData.getParameters().getString("titlemonographie" + i).equals("") || !runData.getParameters().getString("authormonographie" + i).equals("") || !runData.getParameters().getString("chooseauthormonographie" + i).equals("") || !runData.getParameters().getString("choosetitlemonographie" + i).equals("")) {
                lit = new DBLiterature();
                lit.type = NmrshiftdbConstants.MONOGRAPH;
                if (runData.getParameters().getString("authormonographie" + i).equals("")) {
                  lit.setAuthor(runData.getParameters().getString("chooseauthormonographie" + i));
                } else {
                  lit.setAuthor(runData.getParameters().getString("authormonographie" + i));
                }
                if (runData.getParameters().getString("titlemonographie" + i).equals("")) {
                  StringTokenizer st = new StringTokenizer(runData.getParameters().getString("choosetitlemonographie" + i), ".");
                  if (st.hasMoreTokens()) {
                    lit.setTitle(st.nextToken());
                  }
                  if (st.hasMoreTokens()) {
                    lit.setSubtitle(st.nextToken());
                  }
                } else {
                  lit.setTitle(runData.getParameters().getString("titlemonographie" + i));
                  lit.setSubtitle(runData.getParameters().getString("subtitlemonographie" + i));
                }
                lit.setDoi(runData.getParameters().getString("doimonographie" + i));
                if (runData.getParameters().getString("etalmonographie" + i) != null && runData.getParameters().getString("etalmonographie" + i).equals(NmrshiftdbConstants.TRUE)) {
                  lit.etalprov = true;
                }
                lit.setUrl(runData.getParameters().getString("linkmonographie" + i));
                try {
                  lit.year = Integer.parseInt(runData.getParameters().getString("yearmonographie" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>The publication year of monograph entry " + i + " must be an integer figure!");
                }
                if (!runData.getParameters().getString("publishermonographie" + i).equals("")) {
                  lit.publisher = runData.getParameters().getString("publishermonographie" + i);
                  lit.place = runData.getParameters().getString("placemonographie" + i);
                } else {
                  DBPublisher publisher = DBPublisherPeer.retrieveByPK(Integer.parseInt(runData.getParameters().getString("placepublisherarticle" + i)));
                  lit.publisher =  publisher.getName();
                  lit.place = publisher.getPlace();
                }
                literatures.add(lit);
              }
              if (!runData.getParameters().getString("titlearticle" + i).equals("") || !runData.getParameters().getString("authorarticle" + i).equals("")) {
                lit = new DBLiterature();
                lit.type = NmrshiftdbConstants.OTHER_ARTICLE;
                lit.setAuthor(runData.getParameters().getString("authorarticle" + i));
                lit.setTitle(runData.getParameters().getString("titlearticle" + i));
                lit.setSubtitle(runData.getParameters().getString("subtitlearticle" + i));
                lit.setDoi(runData.getParameters().getString("doiarticle" + i));
                if (runData.getParameters().getString("etalarticle" + i) != null && runData.getParameters().getString("etalarticle" + i).equals(NmrshiftdbConstants.TRUE)) {
                  lit.etalprov = true;
                }
                lit.setUrl(runData.getParameters().getString("linkarticle" + i));
                try {
                  lit.from = Integer.parseInt(runData.getParameters().getString("fromarticle" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>From page of other article entry " + i + " must be an integer figure!");
                }
                try {
                  lit.to = Integer.parseInt(runData.getParameters().getString("toarticle" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>To page of other article entry " + i + " must be an integer figure!");
                }
                lit.setAuthorbook(runData.getParameters().getString("authorarticlebook" + i));
                if (runData.getParameters().getString("etalarticlebook" + i) != null && runData.getParameters().getString("etalarticlebook" + i).equals(NmrshiftdbConstants.TRUE)) {
                  lit.etalbook = true;
                }
                lit.setTitleBook(runData.getParameters().getString("titlearticlebook" + i));
                lit.setSubtitleBook(runData.getParameters().getString("subtitlearticlebook" + i));
                if (!runData.getParameters().getString("publisherarticle" + i).equals("")) {
                  lit.publisher = runData.getParameters().getString("publisherarticle" + i);
                  lit.place = runData.getParameters().getString("placearticle" + i);
                } else {
                  DBPublisher publisher = DBPublisherPeer.retrieveByPK(Integer.parseInt(runData.getParameters().getString("placepublisherarticle" + i)));
                  lit.publisher =  publisher.getName();
                  lit.place = publisher.getPlace();
                }
                try {
                  lit.year = Integer.parseInt(runData.getParameters().getString("yearjournal" + i));
                } catch (NumberFormatException ex) {
                  messages.append("<br>Year of journal entry " + i + " must be an integer figure!");
                }
                literatures.add(lit);
              }
            }
          }
          subData.setLiterature(literatures);
          subData.setLevel(5);
          saveSubdataToDisk(subData, runData);
          if(subData.getLiterature().size()>0){
              TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
              tuser.setLiteratureOldSubmit(subData.getLiterature());
              tuser.save();
          } else {
              TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
              tuser.setLiteratureOldSubmit(null);
              tuser.save();
          }
          context.put("messages",messages);
        } catch (Exception ex) {
            context.put("error",GeneralUtils.logError(ex,"submit/submitliterature",runData,true));
        }
  }
  
  /**
   * The action for changing the submit level via the "timeline".
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doChangelevel(RunData runData, Context context){
      try {
          StringBuffer messages = new StringBuffer();
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          subData.setLevel(Integer.parseInt(runData.getParameters().get("newlevel")));
          if(subData.getLevel()==7){
            doChecksFinish(messages, subData);
          }
          if(subData.getLevel()==3){
              //do some checks
              if(subData.getChoosenSpectrumType()==null){
                  messages.append("You must choose the spectrum type before you can do an assignment!<br>");
              }
              if(subData.getMolOriginal()==null){
                  messages.append("You must enter a structure before you can do an assignment!<br>");
              }
          }
          if(!messages.toString().equals("")){
              subData.setLevel(-1);
          }
          context.put("messages",messages);
      } catch (Exception ex) {
          context.put("error",GeneralUtils.logError(ex,"submit/changelevel",runData,true));
    }
  }

  /**
   * The action for submitting a double bond specification.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitdoublebondspecifications(RunData runData, Context context){
      try{
          HttpServletRequest req = runData.getRequest();
          StringBuffer messages = new StringBuffer();
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
          for(int c=0;c<isDoubleBondSpecified.length;c++){
              if (req.getParameter("doublebond" + c) != null) {
                isDoubleBondSpecified[c] = true;
              } else {
                isDoubleBondSpecified[c] = false;
              }
          }
          subData.setExistingMoleculeId(0);
          subData.setLevel(6);
          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
          DBMolecule oldMol = GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), isDoubleBondSpecified, runData.getServletConfig(), subData.getInputMolAsMdl(null,null,false), ssb);
          subData.setSmilesNonChiralProv(ssb.string1);
          subData.setSmilesChiralProv(ssb.string2);
          subData.setIsChiralProv(ssb.bool);
          if (oldMol != null) {
            subData.setExistingMoleculeId(oldMol.getMoleculeId());
            oldMol.addValuesToSubmittingData(subData,null,false, true);
          }
          saveSubdataToDisk(subData, runData);
          context.put("messages",messages);
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"submit/submitMolecule",runData,true));
      }
  }
  
  /**
   * In case the user doesn't want to continue a broken submit, we delete the file
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */ 
  public void doNocontinue(RunData runData, Context context){
    try {
      TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
      tuser.setBrokensubmit(null);
      tuser.save();
      context.put("messages","");
    } catch (Exception ex) {
      context.put("error",GeneralUtils.logError(ex,"submit/nocontinue",runData,true));
    }
  }

  /**
   * User wants to continue a broken submit
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */ 
  public void doContinue(RunData runData, Context context){
    try {
      //We get the old submittingData object from the file
      HttpSession session = runData.getSession();
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
      CMLBuilder builder = new CMLBuilder(false);
      CMLCml cml= (CMLCml) builder.parseString(new String(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getBrokensubmit()));
      subData.data = cml;
      //and put it into the session
      session.setAttribute("subData", subData);
      context.put("messages","");
    } catch (Exception ex) {
      context.put("error",GeneralUtils.logError(ex,"submit/continue",runData,true));
    }
  }

  /**
   * In case the user doesn't want to continue a broken edit, we delete the file.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */ 
  public void doNocontinueedit(RunData runData, Context context){
    try {
      TurbineUser tuser=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
      tuser.setBrokenEdit(null);
      tuser.save();
      context.put("messages","");
    } catch (Exception ex) {
      context.put("error",GeneralUtils.logError(ex,"submit/nocontinueedit",runData,true));
    }
  }

  /**
   * User wants to continue a broken edit.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */ 
  public void doContinueedit(RunData runData, Context context){
    try {
      //We get the old submittingData object from the file
      HttpSession session = runData.getSession();
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
      CMLBuilder builder = new CMLBuilder(false);
      CMLCml cml= (CMLCml) builder.parseString(new String(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getBrokenEdit()));
      subData.data = cml;
      //and put it into the session
      session.setAttribute("subData", subData);
      context.put("messages","");
    } catch (Exception ex) {
      context.put("error",GeneralUtils.logError(ex,"submit/continueedit",runData,true));
    }
  }

  /**
   * User wants to edit a spectrum.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */ 
  public void doEditspectrum(RunData runData, Context context){
    try {
      SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
      subData = DBSpectrumPeer.retrieveByPK(new NumberKey(runData.getParameters().get("spectrum"))).getMeAsSubmittingData(false,ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"),false, "");
      subData.setEditSpectrum(Integer.parseInt(runData.getParameters().get("spectrum")));
      subData.setThisEditorId(Integer.parseInt(((BaseJetspeedUser) runData.getUser()).getUserId()));
      int level=0;
      try{
        level=Integer.parseInt(runData.getParameters().get("level"));
      }catch(Exception ex){
        //We ignore level if reading not possible, editing then starts from scratch
      }
      subData.setLevel(level);
      runData.getSession().setAttribute("subData", subData);
      context.put("messages","");
    } catch (Exception ex) {
      context.put("error",GeneralUtils.logError(ex,"submit/doeditspectrum",runData,true));
    }
  }

  /**
   * The action for linking to the submit page if a user is not necessarily logged in.
   * This checks if user is logged in, if yes, forwards to the action in the nextaction parameter, 
   * else shows a login page, which then forwards to nextaction.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doChecklogin(RunData runData, Context context){
      String nextaction=runData.getParameters().get("nextaction");
      if(runData.getUser().getUserName().equals("anon")){
          context.put("login","true");
          if(runData.getParameters().get("spectrum")!=null)
        	  context.put("spectrum",Encode.forHtmlAttribute(runData.getParameters().get("spectrum")));
          else
        	  context.put("spectrum","");
          if(runData.getParameters().get("molecule")!=null)
        	  context.put("molecule",Encode.forHtmlAttribute(runData.getParameters().get("molecule")));
          else
        	  context.put("molecule","");
          if(runData.getParameters().get("type")!=null) 
        	  context.put("type",Integer.parseInt(runData.getParameters().get("type")));
          else
        	  context.put("type","1");
          if(nextaction!=null)
        	  context.put("nextaction",Encode.forHtmlAttribute(nextaction));
          else
        	  context.put("nextaction","");
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
      HttpSession session = runData.getSession();
      int submitlevel = Integer.parseInt(runData.getParameters().get("submitlevel"));
      if(action.equals("submitfromassignment")){
          SubmittingData subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml")); 
          session.setAttribute("subData", subData);
          subData.setMol((IAtomContainer) session.getAttribute("mol"), true, false, true);
          CMLPeakList numberSpectrum = ParseUtils.parseSpectrum((String)session.getAttribute("spectrumassignment"),null);
          for(CMLPeak peak : numberSpectrum.getPeakChildren()){
              subData.getSignalstable().addPeak(peak);
          }
          subData.setChoosenSpectrumType(DBSpectrumTypePeer.retrieveByPK(new NumberKey((String) session.getAttribute("spectype"))));
          subData.guessAssignment();
          subData.setLevel(submitlevel);
          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), subData.getIsDoubleBondSpecified(), runData.getServletConfig(), subData.getInputMolAsMdl(null,null,false), ssb);
          subData.setSmilesNonChiralProv(ssb.string1);
          subData.setSmilesChiralProv(ssb.string2);
          subData.setIsChiralProv(ssb.bool);
          session.setAttribute("subData", subData);
      }
      if(action.equals("submitfromquickcheck")){
          if(runData.getParameters().getString("type").equals("c")){
              session.setAttribute("subData", runData.getSession().getAttribute("subDataQCc"));
              session.setAttribute("otherSubData", runData.getSession().getAttribute("subDataQCh"));
          }else{
              session.setAttribute("subData", runData.getSession().getAttribute("subDataQCh"));
              session.setAttribute("otherSubData", runData.getSession().getAttribute("subDataQCc"));
          }
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          subData.setLevel(submitlevel);
          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), subData.getIsDoubleBondSpecified(), runData.getServletConfig(), subData.getInputMolAsMdl(null,null,false), ssb);
          subData.setSmilesNonChiralProv(ssb.string1);
          subData.setSmilesChiralProv(ssb.string2);
          subData.setIsChiralProv(ssb.bool);
      }
      if (action.equals("acceptandedit")) {
        SubmittingData subData = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInt("spectrumid")).getMeAsSubmittingData(false, ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"),false, "");
        subData.accept=new NumberKey(runData.getParameters().get("spectrumid"));
        subData.setLevel(submitlevel);
        doEditspectrum(runData, context);
      }
      //User wants to add a spectrum to a molecule
      if (action.equals("addSpectrumToMolecule")) {
          SubmittingData subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
          subData.setExistingMoleculeId(Integer.parseInt(runData.getRequest().getParameter("molecule")));
          subData.setLevel(submitlevel);
          Criteria crit = new Criteria();
          crit.add(DBSpectrumTypePeer.NAME, "13C");
          List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
          subData.setChoosenSpectrumType(v.get(0));
          DBMoleculePeer.retrieveByPK(subData.getExistingMoleculeId()).addValuesToSubmittingData(subData,null,false, true);
          //If the logged in user has already submitted identifiers for this molecule we use them
          crit = new Criteria();
          crit.add(DBSpectrumPeer.MOLECULE_ID,subData.getExistingMoleculeId());
          crit.add(DBSpectrumPeer.USER_ID, ((BaseJetspeedUser)runData.getUser()).getUserId());
          List<DBSpectrum> v2=DBSpectrumPeer.doSelect(crit);
          if(v2.size()>0 && v2.get(0).getDBIdentifiers().size()>0){
              for(int i=0;i<v2.get(0).getDBIdentifiers().size();i++){
                  subData.addIdentifier(((DBIdentifier)v2.get(0).getDBIdentifiers().get(i)).getDBAtom().getMdlNumber(),((DBIdentifier)v2.get(0).getDBIdentifiers().get(i)).getIdentifier());
              }
          }
          runData.getSession().setAttribute("subData", subData);
      }
      context.put("messages","");
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
        if(runData.getParameters().get("spectrum")!=null)
      	  context.put("spectrum",Encode.forHtmlAttribute(runData.getParameters().get("spectrum")));
        else
      	  context.put("spectrum","");
        if(runData.getParameters().get("molecule")!=null)
      	  context.put("molecule",Encode.forHtmlAttribute(runData.getParameters().get("molecule")));
        else
      	  context.put("molecule","");
        if(nextaction!=null)
      	  context.put("nextaction",Encode.forHtmlAttribute(nextaction));
        else
      	  context.put("nextaction","");
        context.put("message", "Problem: "+ex.getMessage()+" logging in. Try again!");
    }
    context.put("reload","true");
    context.put("submitlevel",Integer.parseInt(runData.getParameters().get("submitlevel")));
    doNextaction(nextaction, runData, context);
  }


  /**
   * The action for submitting a structure.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitmolecule(RunData runData, Context context){
      try{
          HttpServletRequest req = runData.getRequest();
          HttpSession session = req.getSession();
          StringBuffer messages = new StringBuffer();
          SubmittingData subData = (SubmittingData) runData.getSession().getAttribute("subData");
          IAtomContainer molold=subData.getMolWithHCount();
          IAtomContainer mololdh=subData.getMolWithH();
          int returnset = 0;
          if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("JCP") || session.getAttribute("applet") !=null && session.getAttribute("applet").equals("applet")){
              returnset = subData.setMol(req.getParameter("MolTxt"), runData.getParameters().get("saltconfirm")==null || runData.getParameters().get("saltconfirm").equals("0"), false, false);
          }
          if (returnset == 2) {
            messages.append("<br> Your molecule contains an NO<sub>2</sub> group drawn like O=N=O. This will be changed to the standard NRMShiftDB representation like O=N<sup>+</sup>-O<sup>-</sup> when saved !");
          }
          if (returnset == 1) {
            messages.append("<br> You did not draw anything. Do that!");
          }
          if (runData.getParameters().get("saltconfirm")==null && returnset == 3) {
            context.put("saltconfirm","true");
            subData.setMol(req.getParameter("MolTxt"), false, false, false);
            messages.append("<br> You drew multiple structures. This is only possible for salts. If the molecule you entered is a salt, check and click Submit, if not, repaint it!");
          }
          Map<Integer, String> newIdentifiers = subData.getIdentifiers();
          if (returnset == 0 || returnset==2 || (returnset==3 && runData.getParameters().get("saltconfirm")!=null && runData.getParameters().get("saltconfirm").equals("1"))) {
            subData.setExistingMoleculeId(0);
            subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
            String mdl = AtomUtils.replace9by6inmolfile(subData.getInputMolAsMdl(null,null,false));
            GeneralUtils.addToStructureHistory(mdl, runData);
            if (req.getParameter("doublebond").equals("1")) {
              subData.setDoubleBondConfiguration(true);
            } else {
                Connection dbconn=null;
                try{
                    dbconn = Torque.getConnection();
                      subData.setLevel(6);
                      StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                      DBMolecule oldMol = GeneralUtils.getExistingMolecule((IAtomContainer)AtomContainerManipulator.removeHydrogens(subData.getMolWithH()), subData.getMolWithH(), null, runData.getServletConfig(), subData.getInputMolAsMdl(null,null,false), ssb, AtomUtils.isOrganometallic(subData.getMolWithH()));
                      subData.setSmilesNonChiralProv(ssb.string1);
                      subData.setSmilesChiralProv(ssb.string2);
                      subData.setIsChiralProv(ssb.bool);
                      if (oldMol != null && subData.getEditSpectrum() == 0) {
                        List<RMap> l=new UniversalIsomorphismTester().getSubgraphAtomsMap(subData.getMolWithHCount(),oldMol.getAsCDKMoleculeWithoutH());
                        subData.setExistingMoleculeId(oldMol.getMoleculeId());
                        oldMol.addValuesToSubmittingData(subData,null,false, true);
                        if(l!=null){
                            for(int k=0;k<l.size();k++){
                                if(newIdentifiers.get(l.get(k).getId1())!=null)
                                    subData.addIdentifier(l.get(k).getId2(), newIdentifiers.get(l.get(k).getId1()));
                            }
                        }
                      }
                      if (subData.getEditSpectrum() != 0 && oldMol != null) {
                        subData.setExistingMoleculeId(oldMol.getMoleculeId());
                      }
                }
                catch(Exception ex){
                    throw ex;
                }
                finally{
                    Torque.closeConnection(dbconn);
                }
            }
            try{
            	if(!AtomUtils.isOrganometallic(subData.getMolWithH())) {
		            String[] pubchemnames = GeneralUtils.getNamesFromPubchem(GeneralUtils.generateInchi(subData.getMolWithH(),false));
		            if(pubchemnames!=null){
			            if(pubchemnames[pubchemnames.length-2]!=null)
			            	subData.setCasNumber(pubchemnames[pubchemnames.length-2]);
			            CMLMolecule molecule =subData.getCMLMolecule();
			            for(int i=0;i<pubchemnames.length-2;i++){
			            	if(pubchemnames[i]!=null){
				            	CMLName name = new CMLName();
				  		      	name.setXMLContent( pubchemnames[i] );
				  		      	molecule.addName( name );
			            	}
			            }
		            }
            	}else {
            		List<Integer> equivalences=new ArrayList<Integer>();
            		IAtomContainer mol=subData.getMolWithH();
            		AtomUtils.makeOrganometallicEquivalences(mol, equivalences);
            		Export export=new Export(mol,mol);
                	Map<String,List<IAtom>> equivalencesets=new HashMap<>();
            		for(int i=0;i<mol.getAtomCount();i++) {
            			IAtom atom=mol.getAtom(i);
            			if((Integer)atom.getProperty("equivalence")!=null) {
            				if(!equivalencesets.containsKey((Integer)atom.getProperty("equivalence")+""))
            					equivalencesets.put((Integer)atom.getProperty("equivalence")+"",new ArrayList<IAtom>());
            				equivalencesets.get((Integer)atom.getProperty("equivalence")+"").add(mol.getAtom(i));
            			}
            		}
            		String smiles=subData.getSmilesNonChiralProv();
                    String smileschiral=subData.getSmilesChiralProv();
                    boolean ischiral=subData.getIsChiralProv();
                    subData.setMol(mol, false, false, false);
                    subData.setIsChiralProv(ischiral);
                    subData.setSmilesNonChiralProv(smiles);
                    subData.setSmilesChiralProv(smileschiral);
                    String url=export.getImage(true,"jpeg",ServletUtils.expandRelative(runData.getServletConfig(),"/nmrshiftdbhtml/tmp/") + System.currentTimeMillis(),350,250,false,null,null,null,Color.BLUE, null, null, null, null, equivalencesets.values(), null);
            		context.put("equivalenceimage","/nmrshiftdbhtml/tmp/"+new File(url).getName());
            	}
            }catch(Exception ex){
            	//we ignore errors here
            }
          }
          saveSubdataToDisk(subData, runData);
          if(molold!=null){
              List<RMap> l=null;
              if(molold.getAtomCount()<subData.getMolWithHCount().getAtomCount()){
                l=new UniversalIsomorphismTester().getSubgraphAtomsMap(subData.getMolWithHCount(),molold);
              }else{
                l=new UniversalIsomorphismTester().getSubgraphAtomsMap(molold,subData.getMolWithHCount());
                if(l!=null){
                  List<RMap> match=new ArrayList<RMap>();
                  for(int i=0;i<l.size();i++){
                    match.add(new RMap(l.get(i).getId2(),l.get(i).getId1()));
                  }
                  l=match;
                }
              }
              if(l!=null){
                for(int i=0;i<subData.getSignalstable().getPeakElements().size();i++){
                    if(subData.getSignalstable().getPeakElements().get(i).getAtomRefs()!=null){
                        for (int n = 0; n < subData.getSignalstable().getPeakElements().get(i).getAtomRefs().length; n++) {
                            IAtom atom=AtomUtils.getAtomById(subData.getMolWithH(), subData.getSignalstable().getPeakElements().get(i).getAtomRefs()[n]);
                            int number=mololdh.getAtomNumber(atom);
                            for(int k=0;k<l.size();k++){
                                if(l.get(k).getId2()==number){
                                    subData.getSignalstable().getPeakElements().get(i).getAtomRefs()[n]=subData.getMolWithH().getAtom(l.get(k).getId2()).getID();
                                }
                            }
                        }
                    }
                }
                for(int k=0;k<l.size();k++){
                    if(newIdentifiers.get(l.get(k).getId2())!=null){
                        subData.addIdentifier(l.get(k).getId2(), newIdentifiers.get(l.get(k).getId1()));
                    }
                }
              }
              //we get multiplicities from h count
              if(subData.getChoosenSpectrumType()!=null){
                  IAtomContainer mol = subData.getMolWithH();
                  for (int i = 0; i < mol.getAtomCount(); i++) {
                      if (mol.getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol())) {
                        if (!subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")) {
                            try{
                                AtomUtils.setChildElement(subData.getCMLMolecule().getAtoms().get(i), SubmittingData.MULTIPLICITY, GeneralUtils.getMultiplicity(mol.getAtom(i), mol, false));
                            }catch(Exception ex){
                                //we get npes here for unclear reasons. a bit dirty like this, but no time to do it better
                            }
                        }
                      }
                  }
              }
          }
          context.put("messages",messages);
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"submit/submitMolecule",runData,true));
      }
  }
  
  /**
   * The action for uploading an mnova file.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitmnova(RunData runData, Context context){
	  try{
		  StringBuffer messages = new StringBuffer();
	      context.put("messages",messages);
		  SubmittingData subDataC=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
		  SubmittingData subDataH=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
		  //we look if there is an mnova file
	      String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
	      File mnovafile=new File(relativepath+"/uploadmnova"+runData.getSession().getId());
	      if(!mnovafile.exists()){
	    	  messages.append("It seems you did not upload a file!");
	    	  return;
	      }
		  GeneralUtils.readMnovafile(mnovafile, subDataC, subDataH, messages, null, null, null, null, null);
		  if(subDataC.getSignalstable().getPeakChildren().size()>0 && subDataH.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataC);
			  runData.getSession().setAttribute("subDataH", subDataH);
			  subDataC.setLevel(6);
		  }else if(subDataC.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataC);
			  subDataC.setLevel(3);
		  }else if(subDataH.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataH);
			  subDataH.setLevel(3);
		  }
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"submit/submitMolecule",runData,true));
      }
  }


  /**
   * The action for uploading an nmredata file.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmitnmredata(RunData runData, Context context){
	  try{
		  StringBuffer messages = new StringBuffer();
	      context.put("messages",messages);
		  SubmittingData subDataC=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
		  SubmittingData subDataH=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
		  //we look if there is an mnova file
	      String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
	      File nmredatafile=new File(relativepath+"/uploadnmredata"+runData.getSession().getId());
	      if(!nmredatafile.exists()){
	    	  messages.append("It seems you did not upload a file!");
	    	  return;
	      }
          NmredataReader reader = new NmredataReader(new FileInputStream(nmredatafile));
          NmreData data = reader.read();
          Map<String,SubmittingData> otherspectra=new HashMap<String,SubmittingData>();
		  GeneralUtils.analyzeNmredata(data, runData, context, subDataC, subDataH, otherspectra);
		  if(subDataC.getSignalstable().getPeakChildren().size()>0 && subDataH.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataC);
			  runData.getSession().setAttribute("subDataH", subDataH);
			  subDataC.setLevel(6);
		  }else if(subDataC.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataC);
			  subDataC.setLevel(3);
		  }else if(subDataH.getSignalstable().getPeakChildren().size()>0){
			  runData.getSession().setAttribute("subData", subDataH);
			  subDataH.setLevel(3);
		  }
		  if(otherspectra.size()>0)
			  runData.getSession().setAttribute("otherspectra", otherspectra);
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"submit/submitMolecule",runData,true));
      }
  }

  
  /**
   * The action for uploading an topspin archive.
   * 
   * @param runData The current RunData object.
   * @param context The context to fill.
   */
  public void doSubmittopspin(RunData runData, Context context){
	  try{
		  SubmittingData subDataC=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
		  SubmittingData subDataH=new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		  subDataH.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("1H"));
	      Map<String,SubmittingData> otherspectra=new HashMap<String,SubmittingData>();
		  GeneralUtils.analyzeTopspinDir(runData, context, subDataC, subDataH, otherspectra, null, null, null, null, null, new StringBuffer());
		  runData.getSession().setAttribute("subData", subDataC);
		  runData.getSession().setAttribute("subDataH", subDataH);
		  if(subDataC.getSignalstable().getPeakChildren().size()==0){
			  runData.getSession().setAttribute("subData", subDataH);
		  }
		  if(otherspectra.size()>0)
			  runData.getSession().setAttribute("otherspectra", otherspectra);
      }
      catch(Throwable ex){
          context.put("error",GeneralUtils.logError(ex,"submit/submitMolecule",runData,true));
      }
  }
}
