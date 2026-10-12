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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.vecmath.Point2d;
//import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.services.rundata.DefaultTurbineRunData;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.Assignment;
import org.jcamp.spectrum.IAssignmentTarget;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.Pattern;
import org.jcamp.spectrum.Peak;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.spectrum.assignments.AtomReference;
import org.jcamp.spectrum.notes.Note;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.modeling.builder3d.TemplateHandler3D;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.BremserOneSphereHOSECodePredictor;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.nmrshiftdb.daemons.AssignRobotDaemon;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBIsotope;
import org.openscience.nmrshiftdb.om.DBIsotopePeer;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBShiftPeer;
import org.openscience.nmrshiftdb.om.DBSignalDBAtomPeer;
import org.openscience.nmrshiftdb.om.DBSignalPeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import de.unikoeln.chemie.nmr.data.NMR2DSpectrum;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.Peak2D;
import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;
import nu.xom.Builder;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;

/**
 *Contains some utils not fitting in somewhere else
 *
 * @author     shk3
 * @created    23. April 2002
 */
public class GeneralUtils {
  /**
   *  Description of the Field
   */
  public static Properties nmrprops = null;
  
  static Logger sqlLogger = null;
  static Category nmrshiftdbLogger = null;
  static Properties jetspeedprops = null;
  static String emailSignature = null;
  static ArrayList<String> atomsWithHoseCode = null;
  static HashMap<String,ModelBuilder3D> mb3d = new HashMap<String,ModelBuilder3D>();
  static GeneralUtils instance=new GeneralUtils();
  
  //attention - problem with static
  public static ArrayList<String> alreadyAttached=new ArrayList<String>();
  
  public static HashMap<String,HashMap<Integer,List<List<Object>>>> spectramap=null;
  public static HashMap<Integer,BitSet> list2d=null;
  
  /**
   *Constructor for the GeneralUtils object (private since only static methods)
   */
  private GeneralUtils() { }
  
  public static GeneralUtils getInstance(){
	  return instance;
  }

  public static BitSet getBitSetOf2D(DBSpectrum spectrum, boolean tolerant) throws NmrshiftdbException, TorqueException{
	  BitSet bs=new BitSet();
	  for(int i=0;i<74;i++){
		  for(int k=0;k<41;k++){
			  if(tolerant){
				  if(spectrum.hasValue((float)(i-1)*3,(float)(i+2)*3,(float)((k-1)*.3),(float)((k+2)*.3))){
					  bs.set(i*73+k);
				  }
			  }else{
				  if(spectrum.hasValue((float)(i)*3,(float)(i+1)*3,(float)((k)*.3),(float)((k+1)*.3))){
					  bs.set(i*73+k);
				  }				  
			  }
		  }
	  }
	  return bs;
  }

  /**
   * Returns a random salt to be used to hash a password.
   *
   * @return a 16 bytes random salt
   */
  public static String getNextSalt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[32];
    random.nextBytes(salt);
    return new String(salt);
  }

  /**
   *  Gets the adminEmail from TurbineRessources.properties
   *
   * @param  runData          The RunData object
   * @return                  The adminEmail value
   * @exception  IOException  Error reading file
   */
  public static String getAdminEmail(RunData runData)
       throws IOException {
    if (runData == null) {
      return (jetspeedprops.getProperty("mail.support"));
    } else {
      return (getAdminEmail(runData.getServletConfig()));
    }
  }
  
  /**
   * Return the input, cut down to length and with a popup link if longer than length.
   * 
   * @param input The input.
   * @param length The length we want to see.
   * @return The modified version.
   */
  public static String getShort(String input, int length){
	  if(input.length()<length){
		  return input;
	  }else{
		  //If there are links in the text we remove links in short display 
		  if(input.indexOf("<a href=")>-1){
			  String inputnolinks = input.replaceAll("</?a(|\\s+[^>]+)>","");
			  if(inputnolinks.length()<length)
				  return input;
			  else
				  return "<a href=\"javascript:window.open('about:blank', '_blank', 'width=1000,height=1000').document.write('"+input.replace(">","&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("(","").replace(")","").replace("&#39;", "").replace("'", "\'")+"')\">"+inputnolinks.substring(0,length)+"... (truncated)</a>";
		  }else{
			  return "<a href=\"javascript:window.open('about:blank', '_blank', 'width=1000,height=100,menubar=no,toolbar=no,status=no,location=no').document.write('"+escape(input,false)+"')\">"+input.substring(0,length)+"... (truncated)</a>";
		  }
	  }
  }

  public static String getShortInChI(String input){
          if(input.length()<30){
                  return input;
          }else{
                  return "<a href=\"javascript:window.open('about:blank', '_blank', 'width=1000,height=100,menubar=no,toolbar=no,status=no,location=no').document.write('"+input+"')\">"+input.substring(0,30)+"... (truncated)</a>";
                  // Can't use the next, because Google might mark the page as spam, and won't index it anyway
                  // "<span class=\"chem:inchi\" style=\"font-size: 0%; visibility: hidden;\">" + input + "</span>"; // and an invisible full InChI for indexing and others
          }
  }

  /**
   *  Gets a ArrayList containing all element symbols from isotope table except H.
   *
   * @return                The atomsWithHoseCode value
   * @throws TorqueException 
   */
  public static ArrayList<String> getAtomsWithHoseCode() throws TorqueException {
    if (atomsWithHoseCode == null) {
      List<DBIsotope> v = DBIsotopePeer.doSelect(new Criteria());
      atomsWithHoseCode = new ArrayList<String>();
      for (int i = 0; i < v.size(); i++) {
        if (!((DBIsotope) v.get(i)).getElementSymbol().equals("H")) {
          atomsWithHoseCode.add(((DBIsotope) v.get(i)).getElementSymbol());
        }
      }
    }
    return (atomsWithHoseCode);
  }


  /**
   *Gets the adminEmail from TurbineRessources.properties
   *
   * @param  config           Teh servletConfig object
   * @return                  The adminEmail value
   * @exception  IOException  Error reading file
   */
  public static String getAdminEmail(ServletConfig config)
       throws IOException {
    if (jetspeedprops == null) {
      String jetspeedpropsFile = ServletUtils.expandRelative(config, "/WEB-INF/conf/JetspeedResources.properties");
      FileInputStream fis=null;
      jetspeedprops = new Properties();
      try{
          fis=new FileInputStream(jetspeedpropsFile);
          jetspeedprops.load(fis);
      }
      finally{
          fis.close();
      }
    }
    return (jetspeedprops.getProperty("mail.support"));
  }


  /**
   *  Gets the smtpServer from TurbineRessources.properties
   *
   * @param  runData          The RunData object
   * @return                  The smtpServer value
   * @exception  IOException  Error reading file
   */
  public static String getSmtpServer(RunData runData)
       throws IOException {
    if (runData == null) {
      return (jetspeedprops.getProperty("mail.server"));
    } else {
      return (getSmtpServer(runData.getServletConfig()));
    }
  }


  /**
   *  Gets the smtpServer from TurbineRessources.properties
   *
   * @param  config           Description of Parameter
   * @return                  The smtpServer value
   * @exception  IOException  Error reading file
   */
  public static String getSmtpServer(ServletConfig config)
       throws IOException {
    if (jetspeedprops == null) {
      String jetspeedpropsFile = ServletUtils.expandRelative(config, "/WEB-INF/conf/JetspeedResources.properties");
      jetspeedprops = new Properties();
      FileInputStream fis=null;
      try{
          fis=new FileInputStream(jetspeedpropsFile);
          jetspeedprops.load(fis);
      }
      finally{
          fis.close();
      }
    }
    return (jetspeedprops.getProperty("mail.server"));
  }


  /**
   *  Gets an ArrayListWithSessionBinding attribute from the session
 * @param <T>
   *
   * @param  session    The current session
   * @param  attribute  Description of Parameter
   * @return            The StructuresHistory value
   */
  @SuppressWarnings("unchecked")
  public static <T> ArrayList<T> getVectorWithSessionBindingAttributeFromSession(HttpSession session, String attribute) {
    if (session.getAttribute(attribute) != null) {
      return (VectorWithSessionBinding<T>) session.getAttribute(attribute);
    } else {
      VectorWithSessionBinding<T> v = null;
      if(DefaultTurbineRunData.getUserFromSession(session)==null){
    	 v = new VectorWithSessionBinding<T>(null);
      }else{
    	 v = new VectorWithSessionBinding<T>(DefaultTurbineRunData.getUserFromSession(session).getUserName());
      }
      session.setAttribute(attribute, v);
      return (v);
    }
  }


  /**
   *  Gets a property from the NMRSHiftDB.properties file
   *
   * @param  property                   The property to get
   * @param  servcon                    The current ServletConfig object
   * @return                            The nmrshiftdbProperty value
   * @exception  FileNotFoundException  Properties file not found
   * @exception  IOException            Could not read properties file
   */
  public static String getNmrshiftdbProperty(String property, ServletConfig servcon) throws FileNotFoundException, IOException {
    if (nmrprops == null) {
      String nmrpropsFile = ServletUtils.expandRelative(servcon, "/WEB-INF/conf/NMRShiftDB.properties");
      nmrprops = new Properties();
      FileInputStream fis=null;
      try{
          fis=new FileInputStream(nmrpropsFile);
          nmrprops.load(fis);
      }
      finally{
          fis.close();
      }
    }
    return (nmrprops.getProperty(property));
  }
  
  public static void initProps(ServletConfig servcon) throws FileNotFoundException, IOException{
	    if (nmrprops == null) {
	        String nmrpropsFile = ServletUtils.expandRelative(servcon, "/WEB-INF/conf/NMRShiftDB.properties");
	        nmrprops = new Properties();
	        FileInputStream fis=null;
	        try{
	            fis=new FileInputStream(nmrpropsFile);
	            nmrprops.load(fis);
	        }
	        finally{
	            fis.close();
	        }
	    }
	    if (jetspeedprops == null) {
	        String jetspeedpropsFile = ServletUtils.expandRelative(servcon, "/WEB-INF/conf/JetspeedResources.properties");
	        jetspeedprops = new Properties();
	        FileInputStream fis=null;
	        try{
	            fis=new FileInputStream(jetspeedpropsFile);
	            jetspeedprops.load(fis);
	        }
	        finally{
	            fis.close();
	        }
	    }
  }


  /**
   *  Gets a property from the NMRSHiftDB.properties file (the nmrprops argument needs to be set before!).
   *
   * @param  property                   The property to get
   * @return                            The nmrshiftdbProperty value
   * @exception  FileNotFoundException  Properties file not found
   * @exception  IOException            Could not read properties file
   */
  public static String getNmrshiftdbProperty(String property) throws FileNotFoundException, IOException {
    if (nmrprops == null) {
      return (null);
    } else {
      return (nmrprops.getProperty(property));
    }
  }


  /**
   *  Gets a property from the NMRSHiftDB.properties file
   *
   * @param  property                   The property to get
   * @param  runData                    The current RunData object
   * @return                            The nmrshiftdbProperty value
   * @exception  FileNotFoundException  Properties file not found
   * @exception  IOException            Could not read properties file
   */
  public static String getNmrshiftdbProperty(String property, RunData runData) throws FileNotFoundException, IOException {
    if (runData == null) {
      return (getNmrshiftdbProperty(property));
    } else {
      return (getNmrshiftdbProperty(property, runData.getServletConfig()));
    }
  }


  public static String generateTucan(IAtomContainer ac) throws Exception{
	  FileWriter fw=new FileWriter(new File("/tmp/test.mol"));
	  MDLV2000Writer mdlwriter = new MDLV2000Writer(fw);
	  mdlwriter.writeMolecule(ac);
	  mdlwriter.close();
	  Process p = new ProcessBuilder("tucan").start();
	  String stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
	  String[] splits=stdout.trim().split("/");
	  return splits[0]+"/"+splits[1];
  }
  
  
  public static Map<Integer,Integer> getTucanLabels(IAtomContainer ac) throws Exception{
	  FileWriter fw=new FileWriter(new File("/tmp/test.mol"));
	  MDLV2000Writer mdlwriter = new MDLV2000Writer(fw);
	  mdlwriter.writeMolecule(ac);
	  mdlwriter.close();
	  Process p = new ProcessBuilder("tucanlabels").start();
	  String stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset()).trim();
	  StringTokenizer st=new StringTokenizer(stdout.substring(1,stdout.length()-1),",");
	  Map<Integer,Integer> result = new HashMap<>();
	  while(st.hasMoreTokens()) {
		  String[] line = st.nextToken().split(":");
		  result.put(Integer.parseInt(line[1].trim()),Integer.parseInt(line[0].trim()));
	  }	  
	  return result;
  }
  
  
  public static DBMolecule getExistingMolecule(IAtomContainer molWithHCount, IAtomContainer molWithH, boolean[] isDoubleBondSpecified, ServletConfig servcon, String molecule, StringAndStringAndBoolean ssb) throws IOException, TorqueException, SQLException, CDKException, DataSetException, Exception{
	  return getExistingMolecule(molWithHCount, molWithH, isDoubleBondSpecified, servcon, molecule, ssb, false);
  }

  /**
   *  Tells if a molecule is already in the DB
   *
   * @param  molWithHCount          The mol without the Hs
   * @param  molWithH               With Hs
   * @param  isDoubleBondSpecified  The ArrayList indicating if double bond orientations are specified as needed for SmilesGenerator, null if none is specified
   * @param  servcon                The current ServletConfig
   * @param  molecule               The mdl file
   * @param  ssb                    Description of Parameter
   * @return                        The existing molecule if there is one, null if none
   * @throws  IOException        Problems reading properties files
   * @throws TorqueException        Database Problems.
   * @throws SQLException 
   * @throws CDKException 
   * @throws DataSetException 
   */
  public static DBMolecule getExistingMolecule(IAtomContainer molWithHCount, IAtomContainer molWithH, boolean[] isDoubleBondSpecified, ServletConfig servcon, String molecule, StringAndStringAndBoolean ssb, boolean isOrganometal) throws IOException, TorqueException, SQLException, CDKException, DataSetException, Exception{
      String inchinew="";
      String inchinewwithh="";
      String metal="";
      //if we have an organometel, we use tucan and generate the smiles with a dummy atom
      System.out.println(isOrganometal);
      if(isOrganometal) {
    	  inchinew=generateTucan(molWithH);
    	  inchinewwithh=inchinew;
    	  for(IAtom atom : molWithH.atoms()) {
    		  if(NmrshiftdbConstants.METALS.contains(atom.getSymbol())) {
    			  metal=atom.getSymbol();
    			  atom.setSymbol("Be");
    			  break;
    		  }
    	  }
      }else {
    	  inchinew=generateInchi(molWithH,false);
          inchinewwithh=generateInchi(molWithH,true);
      }
      SmilesGenerator sg =  new SmilesGenerator(SmiFlavor.Unique | SmiFlavor.UseAromaticSymbols);
      String smilesNonChiral = sg.create(molWithHCount);
      String smilesChiral = "";
      /*try {
        sg = new SmilesGenerator(SmiFlavor.Absolute | SmiFlavor.UseAromaticSymbols);
        smilesChiral = sg.create(molWithH);
      } catch (Exception ex) {
	smilesChiral = smilesNonChiral;
        MailMessage email = new org.apache.turbine.util.mail.MailMessage(getSmtpServer(servcon), "stefan.kuhn@ebi.ac.uk", getAdminEmail(servcon), "Chiral SMILES error", "Chiral SMILES failed for " + molecule);
        GeneralUtils.logError(ex, "smiles", null,true);
        if (email.send() == false) {
          Log.error("Sending email to failed for error creating smiles " + molecule);
        }
      }*/
      if(isOrganometal) {
    	  for(IAtom atom : molWithH.atoms()) {
    		  if(atom.getSymbol().equals("Be")) {
    			  atom.setSymbol(metal);
    			  break;
    		  }
    	  }
    	  smilesNonChiral=replace(smilesNonChiral, "Be", metal);
    	  smilesChiral=replace(smilesChiral, "Be", metal);
      }
      if (ssb != null) {
          ssb.string1 = smilesNonChiral;
          if(StringUtils.countMatches(inchinew, "/")>3){
            ssb.bool = true;
            ssb.string2 = smilesChiral;
          }
      }
      String sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME = '"+inchinewwithh+"'";
      if(isOrganometal)
    	  sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =7 and CANONICAL_NAME.NAME = '"+inchinewwithh+"'";
      List<Record> l = DBMoleculePeer.executeQuery(sql);
      if(l.size()>0)
          return DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt()));
      sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME = '"+inchinew+"'";
      l = DBMoleculePeer.executeQuery(sql);
      if(l.size()>0)
          return DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(0)).getValue(1).asInt()));
      return null;
    /*boolean isChiral = false;
    SmilesGenerator sg =  new SmilesGenerator(SmiFlavour.Unique | SmiFlavour.UseAromaticSymbols);
    String smilesNonChiral = sg.create(molWithHCount);
    String smilesChiral = "";
    try {
      sg = new SmilesGenerator(SmiFlavour.Absolute | SmiFlavour.UseAromaticSymbols);
      smilesChiral = sg.create(molWithH);
    } catch (Exception ex) {
      smilesChiral = smilesNonChiral;
      //MailMessage email = new org.apache.turbine.util.mail.MailMessage(getSmtpServer(servcon), "stefan.kuhn@ebi.ac.uk", getAdminEmail(servcon), "Chiral SMILES error", "Chiral SMILES failed for " + molecule);
      //GeneralUtils.logError(ex, "smiles", null,true);
      //if (email.send() == false) {
      //  Log.error("Sending email to failed for error creating smiles " + molecule);
      //}
    }
    if (smilesChiral.indexOf("/") != -1 || smilesChiral.indexOf("\\") != -1 || smilesChiral.indexOf("@") != -1) {
      isChiral = true;
    }
    //We use a prepared statement in order to escape \ in chiral smiles correctly
    Connection dbconn=null;
    try{
	    dbconn = Torque.getConnection();
	    ResultSet rs = null;
	    if (isChiral) {
	      PreparedStatement checksmiles = dbconn.prepareStatement("select MOLECULE.MOLECULE_ID from MOLECULE where SMILES_STRING= ? and SMILES_STRING_CHIRAL = ?");
	      checksmiles.setString(1, smilesNonChiral);
	      checksmiles.setString(2, smilesChiral);
	      rs = checksmiles.executeQuery();
	    } else {
	      String sql = "select MOLECULE.MOLECULE_ID from MOLECULE where SMILES_STRING= '" + smilesNonChiral + "' and SMILES_STRING_CHIRAL is null";
	      rs = dbconn.createStatement().executeQuery(sql);
	    }
	    ArrayList<DBMolecule> v = new ArrayList<DBMolecule>();
	    while (rs.next()) {
	      v.add(DBMoleculePeer.retrieveByPK(new NumberKey(rs.getString(1))));
	    }
	    if (ssb != null) {
	        ssb.string1 = smilesNonChiral;
	        if (isChiral) {
	          ssb.string2 = smilesChiral;
	        }
	        ssb.bool = isChiral;
		  }
		  if (v.size() > 0) {
			//Check the inchi as a fallback
			String inchiold=((DBMolecule) v.get(0)).getInchi();
			String inchinew=generateInchi(molWithH,false);
			String inchioldwithh=((DBMolecule) v.get(0)).getInchiWithHLayer();
			String inchinewwithh=generateInchi(molWithH,true);
			if(inchiold.equals(inchinew) && inchioldwithh.equals(inchinewwithh))
				return ((DBMolecule) v.get(0));
			else 
				return null;
		  } else {
		    return (null);
		  }
    }
    finally{
        Torque.closeConnection(dbconn);
    }*/
  }


  /**
   *  Gets a standard email signature.
   *
   * @param  runData          Ther runData object.
   * @return                  The emailSignature value.
   * @exception  IOException  Problems reading properties.
   */
  public static String getEmailSignature(RunData runData) throws IOException {
    if (emailSignature == null) {
      if (runData == null) {
        return ("");
      }
      String url = runData.getRequest().getRequestURL().toString();
      StringTokenizer st = new StringTokenizer(url, "/");
      st.nextToken();
      emailSignature = "\n\r-----------------------------\n\rThis is an email automatically generated by the nmrshiftdb system on " + st.nextToken() + ". It is sent to you because you used a service from nmrshiftdb. For any questions, including exclusion of further emails, mail the administrator (email: " + GeneralUtils.getAdminEmail(runData) + ").";
    }
    return (emailSignature);
  }


  /**
   *  Gets an object as an JSX XML String.
   *
   * @param  obj                      The object to serialize.
   * @return                          The xml string.
   * @throws JAXBException 
   */
  public static String getAsXMLString(Object obj)  {
    XStream xstream = new XStream(new DomDriver());
    String xml = xstream.toXML(obj);
    return xml;
  }


  /**
   *  Gets an object from an JSX xml string.
   *
   * @param  xmlString                             The xml string.
   * @return                                       The object.
   * @throws JAXBException 
   */
  public static Object getFromXMLString(String xmlString) {
     XStream xstream = new XStream(new DomDriver());
     Object obj = xstream.fromXML(xmlString);
     return obj;
  }


  /**
   *  Returns a letter multiplicity from an int.
   *
   * @param  multiplicity  The mult to translate.
   * @param  totalname     true=long name, false=one letter name.
   * @return               The multiplicity value.
   */
  public static String getMultiplicity(int multiplicity, boolean totalname) {
    int protonCount = multiplicity;
    if (protonCount == 0) {
      if (totalname) {
        return ("Singlet");
      } else {
        return ("S");
      }
    } else if (protonCount == 1) {
      if (totalname) {
        return ("Duplet");
      } else {
        return ("D");
      }
    } else if (protonCount == 2) {
      if (totalname) {
        return ("Triplet");
      } else {
        return ("T");
      }
    } else if (protonCount == 3) {
      if (totalname) {
        return ("Quadruplet");
      } else {
        return ("Q");
      }
    } else {
      return ("");
    }
  }


  /**
   * Returns multiplicity from counting neighbouring Hs.
   *
   * @param  atom          the atom to look at (must be in mol).
   * @param  mol           the molecule the atom is in. 
   * @param  totalname     true=long name, false=one letter name.
   * @return               The multiplicity value.
   */
  public static String getMultiplicity(IAtom atom, IAtomContainer mol, boolean totalname) {
      List<IAtom> conatoms = mol.getConnectedAtomsList(atom);
      int hcount=0;
      for(int i=0;i<conatoms.size();i++){
          if(conatoms.get(i).getSymbol().equals("H"))
              hcount++;
      }
      return getMultiplicity(hcount, totalname);
  }
  
  /**
   *  Tells how many digits after the comma (or .) a float has got.
   *
   * @param  value  The float to axamine.
   * @return        Number of digits.
   */
  public static int getDigitsAfterComma(float value) {
    int numberOfDigitsAfterComma = 0;
    String str = new String(value + "");
    if (str.length() < 1) {
      return (0);
    }
    int k = 0;
    for (int i = str.length() - 1; i > -1; i--) {
      if (str.substring(i, i + 1).equals(".")) {
        numberOfDigitsAfterComma = k;
        break;
      }
      k++;
    }
    return (numberOfDigitsAfterComma);
  }


  /**
   *  Gets the modelBuilder attribute of the GeneralUtils class
   *
   * @param  ff                Description of Parameter
   * @return                   The modelBuilder value
   * @exception  CDKException  Description of Exception
   */
  public static ModelBuilder3D getModelBuilder(String ff) throws CDKException {
    if (mb3d.get(ff) == null) {
      ModelBuilder3D mb3dlocal = ModelBuilder3D.getInstance(TemplateHandler3D.getInstance(),ff, DefaultChemObjectBuilder.getInstance());
      mb3d.put(ff, mb3dlocal);
    }
    return (ModelBuilder3D) mb3d.get(ff);
  }


  /**
   *  Removes the last character from a string buffer if it is a , or ;.
   *
   * @param  s  The string buffer to deal with.
   * @return    Description of the Returned Value
   */
  public static StringBuffer removeLastComma(StringBuffer s) {
    if (s.length() > 0 && (s.charAt(s.length() - 1) == ' ')) {
      s.deleteCharAt(s.length() - 1);
    }
    if (s.length() > 0 && (s.charAt(s.length() - 1) == ';' || s.charAt(s.length() - 1) == ',')) {
      s.deleteCharAt(s.length() - 1);
    }
    return (s);
  }


  /**
   *Logs an error
   *
   * @param  ex       The thrown excpetion
   * @param  message  Some individual message
   * @param  data     The current RunData
   * @return          A text to display
   */
  public static String logError(Throwable ex, String message, RunData data, boolean critical){
	if (nmrshiftdbLogger == null) {
		if(data!=null){
			try{
				nmrshiftdbLogger = Category.getInstance("nmrshiftdb");
				//nmrshiftdbLogger.addAppender(new FileAppender(new SimpleLayout(),"$CATALINA_BASE/logs/nmrshiftdb.log"));
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			ex.printStackTrace();
			return "<img src=\"images/bug.png\">An error occured. This error has been reported to the adminstrator! He/she might contact you for details.";
		}
	}
    nmrshiftdbLogger.error("There was an error at "+new Date()+", described as " + message + ", the Exception message is: " + ex.getMessage());
    StringWriter strWr = new StringWriter();
    PrintWriter prWr = new PrintWriter(strWr);
    ex.printStackTrace(prWr);
    nmrshiftdbLogger.error(strWr.toString());
    String x = "";
    if (data != null && data.getUser().hasLoggedIn()) {
      try {
        x = new String("<img src=\"images/bug.png\">An error occured. This error has been reported to the <a href=\"mailto:" + getAdminEmail(data) + "\">adminstrator</a>! If you want this bug to be fixed, please contact the <a href=\"mailto:" + getAdminEmail(data) + "\">adminstrator</a> with details of your action or file a bug report in the <a href=\"https://sourceforge.net/p/nmrshiftdb2/bugs/\">bug tracker</a>.");
      } catch (Exception e) {
        x = new String("<img src=\"images/bug.png\">An error occured. This error has been reported to the adminstrator! If you want this bug to be fixed, please contact the adminstrator with details of what your action or file a bug report in the <a href=\"https://sourceforge.net/p/nmrshiftdb2/bugs/\">bug tracker</a>.");
      }
    } else {
      try {
        x = new String("<img src=\"images/bug.png\">An error occured. Please report this error to the <a href=\"mailto:" + getAdminEmail(data) + "\">adminstrator</a>! If you want this bug to be fixed, please contact the <a href=\"mailto:" + getAdminEmail(data) + "\">adminstrator</a> with details of your action or file a bug report in the <a href=\"https://sourceforge.net/p/nmrshiftdb2/bugs/\">bug tracker</a>.");
      } catch (Exception e) {
        x = new String("<img src=\"images/bug.png\">An error occured. Please report this error to the adminstrator! If you want this bug to be fixed, please contact the adminstrator with details of your action or file a bug report in the <a href=\"https://sourceforge.net/p/nmrshiftdb2/bugs/\">bug tracker</a>.");
      }
    }
    if (data != null) {
      try {
        sendEmailToEventReceivers("Exception occured in nmrshiftdb", "There was an exception " + ex.getMessage() + " thrown on " + data.getServerName() + " at " + new Date() + ". The logged in user was " + (data.getUser().hasLoggedIn() ? data.getUser().getUserName() : "nobody") + ".", data, critical ? 2 : 1);
      } catch (Exception e) {
        Log.error("sending email in logError failed");
      }
    }
    return (x);
  }


  /**
   *  Sends an email to all admineventreceiver role owners
   *
   * @param  header   The header of the email
   * @param  body     The body of the email
   * @param  runData  The runData object
   * @param  error    Is this an error or an info message (0=info, 1=non-critical error, 2=error)
   * @return          false=sending failed, true=worked
   */
  public static boolean sendEmailToEventReceivers(String header, String body, RunData runData, int error) {
    Connection dbconn = null;
    try {
      String sql = "select TURBINE_USER.EMAIL from TURBINE_USER, TURBINE_USER_GROUP_ROLE, TURBINE_ROLE where TURBINE_USER.USER_ID=TURBINE_USER_GROUP_ROLE.USER_ID and TURBINE_USER_GROUP_ROLE.ROLE_ID=TURBINE_ROLE.ROLE_ID AND TURBINE_ROLE.ROLE_NAME='admineventreceiver';";
      dbconn = Torque.getConnection();
      Statement stmt = dbconn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(runData), rs.getString(1), GeneralUtils.getAdminEmail(runData), (error>0 ? (error==1 ? "[NMRSHIFTDB-ERROR-NON-CRITICAL]" : "[NMRSHIFTDB-ERROR]") : "[NMRSHIFTDB-INFO]") + " " + header, body + GeneralUtils.getEmailSignature(runData));
        if (email.send() == false) {
          Log.error("Sending email to receiver " + rs.getString(1) + " failed.");
        }
      }
      return (true);
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "in sendEmailToEventReceivers", runData,true);
      return (false);
    } finally {
      try {
        Torque.closeConnection(dbconn);
      } catch (Exception ex) {
        GeneralUtils.logError(ex, "SubmitPortlet/trying to close connection", runData,true);
      }
    }
  }


  /**
   *  Selects a reviewer
   *
   * @param  user           The user currently logged in, this user cannot become the reviewer.
   * @return                The choosen reviewer
   * @throws DataSetException
   * @throws TorqueException 
   */
  public static TurbineUser selectReviewer(TurbineUser user) throws TorqueException, DataSetException {
    //Look if this contributor is member of a review group
    List<DBReviewGroup> v = user.getReviewGroups();
    List<Record> records = null;
    //if yes, select only those from the group
    if (v.size() != 0) {
    	//if any of the reviewgroups has a superreviewer, we chose that
    	for(DBReviewGroup rg : v) {
    		if(rg.getNmrshiftdbUser()!=null)
    			return rg.getNmrshiftdbUser();
    	}
      String sql = "select TURBINE_USER.USER_ID, count(USER_SPECTRUM.USER_ID) AS REVIEWS from TURBINE_USER left join USER_SPECTRUM on TURBINE_USER.USER_ID = USER_SPECTRUM.USER_ID, REVIEWGROUP_USER where TURBINE_USER.USER_ID=REVIEWGROUP_USER.USER_ID and REVIEWGROUP_USER.REVIEWGROUP_ID='" + ((DBReviewGroup) v.get(0)).getReviewgroupId() + "' and TURBINE_USER.LOGIN_NAME<>\"" + user.getLoginName() + "\" group by USER_ID order by REVIEWS;";
      records = NmrshiftdbUserPeer.executeQuery(sql);
    }
    //If there is an editor, it goes to the editor
    else {
        String sql="select TURBINE_USER.USER_ID from TURBINE_USER join TURBINE_USER_GROUP_ROLE using (USER_ID ) join TURBINE_ROLE_PERMISSION using (ROLE_ID) join TURBINE_PERMISSION using (PERMISSION_ID ) where PERMISSION_NAME =\"edit\"";
        records = NmrshiftdbUserPeer.executeQuery(sql);
        if(records.size()==0) {
            //If not, select all reviewers
            sql = "select TURBINE_USER.USER_ID, count(USER_SPECTRUM.USER_ID) AS REVIEWS from (TURBINE_USER left join USER_SPECTRUM on TURBINE_USER.USER_ID = USER_SPECTRUM.USER_ID) join TURBINE_USER_GROUP_ROLE on TURBINE_USER.USER_ID=TURBINE_USER_GROUP_ROLE.USER_ID join TURBINE_ROLE on TURBINE_USER_GROUP_ROLE.ROLE_ID=TURBINE_ROLE.ROLE_ID where TURBINE_ROLE.ROLE_NAME='c' and  TURBINE_USER.LOGIN_NAME<>\"" + user.getLoginName() + "\" group by USER_ID order by REVIEWS;";
            records = NmrshiftdbUserPeer.executeQuery(sql);
        }
    }
    if (records.size() > 0) {
      return NmrshiftdbUserPeer.retrieveByPK(new NumberKey(((Record) records.get(new Random().nextInt(records.size()))).getValue(1).asString()));
    } else {
      return user;
    }
  }


  /**
   *  Adds an mdl file to the StructureHistory
   *
   * @param  mdl      The mdl to be added
   * @param  runData  The current RunData
   * @throws CDKException 
   * @throws IllegalArgumentException 
   * @throws TorqueException 
   * @throws TooManyRowsException 
   * @throws NoRowsException 
   * @throws JAXBException 
   * @throws IOException 
   * @throws Exception 
   */
  public static void addToStructureHistory(String mdl, RunData runData) throws IllegalArgumentException, CDKException, NoRowsException, TooManyRowsException, TorqueException, IOException {
    ArrayList<String> v = getVectorWithSessionBindingAttributeFromSession(runData.getSession(), NmrshiftdbConstants.STRUCTURESHISTORY);
    ArrayList<String> vsmiles = getVectorWithSessionBindingAttributeFromSession(runData.getSession(), NmrshiftdbConstants.STRUCTURESHISTORY_SMILES);
    IAtomContainer mol = AtomUtils.readMolBothVersions(mdl);
    for(IAtom atom : mol.atoms()){
      if(atom.getImplicitHydrogenCount()==null)
        atom.setImplicitHydrogenCount(0);
    }
    //we treat 6 bonds as zero bonds for the smiles
    for(IBond bond : mol.bonds()) {
        if(bond.getOrder()==null)
            bond.setOrder(IBond.Order.ZERO);
    }
    String smiles = new SmilesGenerator().createSMILES(mol);
    //in order to avoid double entries, we remove old entries of the same structure. smiles is used for identity checking.
    for(int i=0;i<vsmiles.size();i++){
        if(vsmiles.get(i).equals(smiles)){
            vsmiles.remove(i);
            v.remove(i);
        }
    }
    while (v.size() >= 10) {
      v.remove(0);
      vsmiles.remove(0);
    }
    v.add(mdl);
    vsmiles.add(smiles);
    TurbineUser loggedInUser = TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
    if(!loggedInUser.getLoginName().equals("admin")){
    	loggedInUser.setStructureshistory(GeneralUtils.getAsXMLString(v).getBytes());
    	loggedInUser.save();
    }
  }


  /**
   *  Logs something to the sql log file
   *
   * @param  message        The message to log
   * @param  runData        The runData object
   */
  public static void logToSql(String message, RunData runData) {
    if (sqlLogger == null) {
    	sqlLogger = Logger.getLogger("sql");
    }
    sqlLogger.info(new Date() + " - " + (runData==null || runData.getUser()==null ? "" : runData.getUser().getUserName()) + " - " + message+ " - "+(runData==null ? "" : runData.getRequest().getRemoteAddr()));
  }


  /**
   * Adds an element to a chemical formual string
   *
   * @param  sm          The map containing the elements
   * @param  symbol      The symbol to add
   * @param  formula     The chemical formula
   * @param  withFormat  The feature to be added to the SymbolToFormula attribute
   * @return             Description of the Returned Value
   */
  public static String addSymbolToFormula(SortedMap<String,Integer> sm, String symbol, String formula, boolean withFormat) {
    if (sm.get(symbol) != null) {
      formula += symbol;
      if (!sm.get(symbol).equals(new Integer(1))) {
        if (withFormat) {
          formula += "<sub>";
        }
        formula += sm.get(symbol).toString();
        if (withFormat) {
          formula += "</sub>";
        }
      }
    }
    return (formula);
  }

  
  /**
   *  Replaces a string in a string with another string
   *
   * @param  str      The string in which replacing has to take place
   * @param  pattern  The pattern to replace
   * @param  replace  The string the pattern has to be replaced with
   * @return          The changed string
   */
  public static String replace(String str, String pattern, String replace) {
    int s = 0;
    int e = 0;
    StringBuffer result = new StringBuffer();

    while ((e = str.indexOf(pattern, s)) >= 0) {
      result.append(str.substring(s, e));
      result.append(replace);
      s = e + pattern.length();
    }
    result.append(str.substring(s));
    return result.toString();
  }


  /**
   *  Checks if the values of the C spectra of a certain DBMolecule are inside the CDK prediction range.
   *
   * @param  mol              The CDMolecule to check.
   * @return                  A text either "" or a description of problems.
   * @exception  IOException  Problems with serialisation.
   * @exception  Exception    Database problems.
   */
  public static String checkAgainstCdk(DBMolecule mol) throws IOException, Exception {
    try {
      HOSECodeGenerator hcg = new HOSECodeGenerator();
      BremserOneSphereHOSECodePredictor bremser = new BremserOneSphereHOSECodePredictor();
      StringBuffer returnValue = new StringBuffer();
      IAtomContainer cdkmolwithh = mol.getAsCDKMolecule(1);
      IAtomContainer cdkmolwithouth = (IAtomContainer) AtomContainerManipulator.removeHydrogens(cdkmolwithh);
      Iterator<IAtom> atoms = cdkmolwithh.atoms().iterator();
      DBAtom[] dbatoms = mol.getAtomsAsArrayInMdlOrder();
      int i=0;
      while(atoms.hasNext()){
    	IAtom atom=(IAtom)atoms.next();
        if (atom.getSymbol().equals("C")) {
          String hc = hcg.getHOSECode(cdkmolwithouth, atom, 1,true);
          StringTokenizer st = new StringTokenizer(hc, ";");
          st.nextToken();
          String simpleHoseCode = st.nextToken();
          double predict = bremser.predict(simpleHoseCode);
          double lower = predict - bremser.getConfidenceLimit(simpleHoseCode);
          double upper = predict + bremser.getConfidenceLimit(simpleHoseCode);
          Criteria crit = new Criteria();
          crit.addJoin(DBSpectrumPeer.SPECTRUM_TYPE_ID, DBSpectrumTypePeer.SPECTRUM_TYPE_ID);
          crit.add(DBSpectrumTypePeer.NAME, "13C");
          crit.addJoin(DBSpectrumPeer.SPECTRUM_ID, DBSignalPeer.SPECTRUM_ID);
          crit.addJoin(DBSignalPeer.SIGNAL_ID, DBShiftPeer.SIGNAL_ID);
          crit.addJoin(DBSignalPeer.SIGNAL_ID, DBSignalDBAtomPeer.SIGNAL_ID);
          crit.add(DBSignalDBAtomPeer.ATOM_ID, dbatoms[i].getAtomId());
          List<DBShift> v = DBShiftPeer.doSelect(crit);
          for (int k = 0; k < v.size(); k++) {
            if (((DBShift) v.get(k)).getValue() < lower || ((DBShift) v.get(k)).getValue() > upper) {
              returnValue.append("Value from DB is " + ((DBShift) v.get(k)).getValue() + ", the cdk intervall is " + lower + " - " + upper + " for atom " + i + " of Molecule " + mol.getMoleculeId() + ".\n\r");
            }
          }
          i++;
        }
      }
      return (returnValue.toString());
    } catch (CDKException ex) {
      return ("");
    }
  }


  /**
   *  Removes atom numbers from a specfile string.
   *
   * @param  specfile       The original specfile.
   * @param  dropIntensity  true=also drop intensities if they are zero.
   * @return                The new specfile.
   */
  public static String makeSimpleSpecfile(String specfile, boolean dropIntensity) {
	    List<CMLPeak> numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(specfile);
	    StringBuffer display = new StringBuffer();
	    for (int k = 0; k < numberSpectrum.size(); k++) {
	      double value1 = numberSpectrum.get(k).getXValue();
	      if ((int) value1 == value1) {
	        display.append((int) value1);
	      } else {
	        display.append(value1);
	      }
	      if (numberSpectrum.get(k).getYValue() != 0 || !dropIntensity) {
	        display.append(";");
	        double value2 = numberSpectrum.get(k).getYValue();
	        if ((int) value2 == value2) {
	          display.append((int) value2);
	        } else {
	          display.append(value2);
	        }
	      }
	      if (numberSpectrum.get(k).getPeakMultiplicity()!=null) {
	        display.append(numberSpectrum.get(k).getPeakMultiplicity());
	      }
	      display.append("|");
	    }
	    return (display.toString());
	  }


  /**
   *  Prepares a string for sql queries by escaping " and ' and \.
   *
   * @param  text  The text to be escaped.
   * @param  forLike  Escape so that like queries work.
   * @return       The escaped text
   */
  public static String escape(String text, boolean forLike) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\'' || text.charAt(i) == '\"' || text.charAt(i) == '\\' || text.charAt(i) == '%' || text.charAt(i) == '\'') {
        buffer.append('\\');
      }
      if (text.charAt(i) == '\\' && forLike){
    	  buffer.append("\\\\");
      }
   	  buffer.append(text.charAt(i));
    }
    return buffer.toString();
  }


  /**
   *  Escapes a string for html (& and <).
   *
   * @param  text  The text to escape.
   * @param  escapeQuotations If true, " becomes \"
   * @return       Description of the Returned Value
   */
  public static String escapeForHtml(String text, boolean escapeQuotations) {
	text = escapeHtml(text);
	if(escapeQuotations)
	  text = replace(text, "\"", "&quot;");
    return (text);
  }
  
  public static double similarity(List<CMLPeak> al1, List<CMLPeak> al2, boolean isSubSpectrum){
        double similarityPerSignal;
        double similarity = 0;
	    int greaterNumber;
	    int smallerNumber;
	    List<CMLPeak> smallerArray;
	    List<CMLPeak> greaterArray;
	    //If it's a subspectrum search, the search spectrum is always taken as basis for search
	    //The similarity per signal is calculated from that spectrum
	    //If there is a signal in the spectrum in the DB for every signal in the search spectrum, similarity will be 100 %
        boolean smallervectorissearchspectrum = true;
	    if (isSubSpectrum) {
	      smallerNumber = al2.size();
	      greaterNumber = al1.size();
	      smallerArray = al2;
	      greaterArray = al1;
	      similarityPerSignal = 100f / smallerNumber;
	    } else {
	      //The variables smallerArraya/smallerNubmer and greaterArray/greaterNumber are set so that calculation is independent of which spectrum having more values
	      //For 100% similarity the spectra need to have the same number of signals
	      if (al1.size() > al2.size()) {
	        greaterNumber = al1.size();
	        smallerNumber = al2.size();
	        smallerArray = al2;
	        greaterArray = al1;
	      } else {
	        greaterNumber = al2.size();
	        smallerNumber = al1.size();
	        smallerArray = al1;
	        greaterArray = al2;
	        smallervectorissearchspectrum = false;
	      }
	      //From the 100% maximum similarity, one signal can have 100/greater number of signals at best
	      similarityPerSignal = 100f / greaterNumber;
	    }
	    Vector<Double> unused = new Vector<Double>();
	    Map<Double,Double> map = getMatchingSearchShift(greaterArray, smallerArray, similarityPerSignal, unused, smallervectorissearchspectrum);
	    Set<Double> set = map.keySet();
	    Iterator<Double> iterator = set.iterator();
	    while (iterator.hasNext()) {
	      Object element = iterator.next();
	      float difference = java.lang.Math.abs(((Double) element).floatValue() - (map.get(element)).floatValue());
	      if (!isSubSpectrum && difference < similarityPerSignal) {
	        similarity += similarityPerSignal - difference;
	      }
	      //we integrate the subspectrum search here 
	      if (isSubSpectrum && difference < similarityPerSignal && difference<8) {
		        similarity += similarityPerSignal - difference;
	      }
	    }
	    return similarity;
  }
  
  /**
   *  Creates a map with shifts as keys and the nearest match in the search spectrum as value
   *
   * @param  largerSpectrum     The original spectrum
   * @param  smallerSpectrum       The search spectrum
   * @param  similaritypersignal  Description of Parameter
   * @param  unused               These shifts from largerSpectrum have not been used.
   * @param  smallerSpectrumIsSearchSpectrum               If true, smallerSpectrum is the search spectrum, else largerSpectrum is the search spectrum (this difference 
   * is important for matching multiplicities, where no multiplicity in search spectrum matches anything).
   * @return                      The matchingSearchShift value
   */
  public static Map<Double,Double> getMatchingSearchShift(List<CMLPeak> largerSpectrum, List<CMLPeak> smallerSpectrum, double similaritypersignal, List<Double> unused, boolean smallerSpectrumIsSearchSpectrum) {
      double[][] costmatrix=new double[largerSpectrum.size()][smallerSpectrum.size()];
      for(int i=0;i<largerSpectrum.size();i++){
          CMLPeak largerPeak = largerSpectrum.get(i);
          for(int k=0;k<smallerSpectrum.size();k++){
              CMLPeak smallerPeak = smallerSpectrum.get(k);
              if(smallerSpectrumIsSearchSpectrum && 
                      ((smallerPeak.getPeakMultiplicity()==null || smallerPeak.getPeakMultiplicity().equals("")) ||
                              smallerPeak.getPeakMultiplicity().equals(largerPeak.getPeakMultiplicity()))
                      || (!smallerSpectrumIsSearchSpectrum &&
                          ((largerPeak.getPeakMultiplicity()==null || largerPeak.getPeakMultiplicity().equals("")) ||
                                  largerPeak.getPeakMultiplicity().equals(smallerPeak.getPeakMultiplicity()))))
            	  costmatrix[i][k]=Math.abs(Math.abs(largerPeak.getXValue())-Math.abs(smallerPeak.getXValue()));
              else
            	  costmatrix[i][k]=Double.MAX_VALUE;
          }
      }
      HungarianAlgorithm hungarian = new HungarianAlgorithm(costmatrix);
      int[] hungarianresult = hungarian.execute();
      Map<Double,Double> result=new HashMap<Double,Double>();
      for(int i=0;i<hungarianresult.length;i++){
          if(hungarianresult[i]>-1 && costmatrix[i][hungarianresult[i]]<Double.MAX_VALUE){
              result.put(largerSpectrum.get(i).getXValue(), smallerSpectrum.get(hungarianresult[i]).getXValue());
          }else if(unused!=null){
              unused.add(largerSpectrum.get(i).getXValue());
          }
      }
      return result;
      
      
	    /*boolean signalsUsed[] = new boolean[largerSpectrum.size()];
	    Map<Double,Double> map = new HashMap<Double,Double>();
	    for (int i = 0; i < smallerSpectrum.size(); i++) {
	      int nearestSignal = -1;
	      double difference = Double.MAX_VALUE;
	      for (int m = 0; m < largerSpectrum.size(); m++) {
	        if (java.lang.Math.abs(largerSpectrum.get(m).getXValue() - smallerSpectrum.get(i).getXValue()) < difference &&
	                signalsUsed[m] == false &&
	                (smallerSpectrumIsSearchSpectrum && 
	                        ((smallerSpectrum.get(i).getPeakMultiplicity()==null || smallerSpectrum.get(i).getPeakMultiplicity().equals("")) ||
	                                smallerSpectrum.get(i).getPeakMultiplicity().equals(largerSpectrum.get(m).getPeakMultiplicity()))
	                        || (!smallerSpectrumIsSearchSpectrum &&
	                            ((largerSpectrum.get(m).getPeakMultiplicity()==null || largerSpectrum.get(m).getPeakMultiplicity().equals("")) ||
	                                    largerSpectrum.get(m).getPeakMultiplicity().equals(smallerSpectrum.get(i).getPeakMultiplicity()))))){
	        	difference = java.lang.Math.abs(largerSpectrum.get(m).getXValue() - smallerSpectrum.get(i).getXValue());
		          nearestSignal = m;
		    }
	      }
	      if(nearestSignal>-1){
		      signalsUsed[nearestSignal] = true;
		      map.put(new Double(largerSpectrum.get(nearestSignal).getXValue()), new Double(smallerSpectrum.get(i).getXValue()));
		      if (difference > similaritypersignal) {
		        unused.add(new Double(smallerSpectrum.get(i).getXValue()));
		      }
	      }
	    }
	    return (map);*/
	  }

  /**
   *  Parses a jcmap file to a submittingData object.
   *
   * @param  subData                  The signalstable of this will be filled.
   * @param  jcamp                    The path to the file to read.
   * @param  mode                     JCAMPReader.RELAXED or  JCAMPReader.STRICT
   * @param  runData                  The current runData object
   * @exception  IOException          Description of Exception
   * @exception  JCAMPException       Description of Exception
   * @exception  NmrshiftdbException  Description of Exception
   * @throws TorqueException          Description of Exception
   * @throws CDKException             Description of Exception 
   */
  public static void parseJcamp(SubmittingData subData, String jcamp, RunData runData, String mode) throws IOException, JCAMPException, NmrshiftdbException, TorqueException, CDKException {
    Spectrum jcampSpectrum = JCAMPReader.getInstance(true, mode).createSpectrum(jcamp);
    if (!(jcampSpectrum instanceof NMRSpectrum)) {
      throw new NmrshiftdbException("Spectrum in file is not an NMR spectrum!");
    }
    NMRSpectrum nmrspectrum = (NMRSpectrum) jcampSpectrum;
    for(int i=0;i<subData.getConditions().size();i++){
        DBConditionType cond=(DBConditionType)subData.getConditions().get(i);
        if(cond.getConditionTypeId()==3){
            //frequency
            cond.setCurrentValue(nmrspectrum.getFrequency()+"");
        }
    }
    CMLSpectrum cmlSpec = JcampToCMLSpectrumMapper.mapJcampToCMLSpectrum(nmrspectrum, runData);
	List<CMLElement> l=cmlSpec.getConditionListElements().get(0).getChildCMLElements();
	for(int k=0;k<l.size();k++){
		if(l.get(k).getAttribute("id")!=null && l.get(k).getAttribute("id").getValue().equals("dotOBSERVENUCLEUS")){
	          Criteria crit = new Criteria();
	          crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID, l.get(k).getValue());
	          List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
	          subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
		}										
	}
	CMLPeakList numberSpectrum = parseJcamp(jcamp, mode, nmrspectrum);
    CMLPeakList oldSignalstable = null;
    if (subData.getSignalstable() != null) {
      oldSignalstable = subData.getSignalstable();
    }
    if (oldSignalstable != null) {
      CMLPeakList signalstable = subData.getSignalstable();
      for (int i = 0; i < signalstable.getPeakElements().size(); i++) {
        for (int k = 0; k < oldSignalstable.getPeakElements().size(); k++) {
           if (signalstable.getPeakElements().get(i).getXValue() == oldSignalstable.getPeakElements().get(k).getXValue()) {
            signalstable.getPeakElements().get(i).setAtomRefs(oldSignalstable.getPeakElements().get(k).getAtomRefs());
          }
        }
      }
    }
    subData.setKeepAssignmentEntries("");
    while(numberSpectrum.getPeakElements().size()>0) {
      subData.addToSignalstable(numberSpectrum.getPeakElements().get(0));
    }
  }

  /**
   *  Parses a jcmap file to a CMLPeakList.
   *
   * @param  jcamp                    The path to the file to read.
   * @param  mode                     JCAMPReader.RELAXED or  JCAMPReader.STRICT
   * @exception  JCAMPException       Description of Exception
   * @exception  NmrshiftdbException  Description of Exception
   */
  public static CMLPeakList parseJcamp(String jcamp, String mode) throws JCAMPException, NmrshiftdbException{
      Spectrum jcampSpectrum = JCAMPReader.getInstance(true, mode).createSpectrum(jcamp);
      if (!(jcampSpectrum instanceof NMRSpectrum)) {
        throw new NmrshiftdbException("Spectrum in file is not an NMR spectrum!");
      }
      NMRSpectrum nmrspectrum = (NMRSpectrum) jcampSpectrum;
      return parseJcamp(jcamp, mode, nmrspectrum);
  }

  /**
   *  Parses a jcmap file to a CMLPeakList.
   *
   * @param  jcamp                    The path to the file to read.
   * @param  mode                     JCAMPReader.RELAXED or  JCAMPReader.STRICT
   * @param  nmrspectrum              The NMRSpectrum object of the file in jcamp.
   * @exception  JCAMPException       Description of Exception
   * @exception  NmrshiftdbException  Description of Exception
   */
  public static CMLPeakList parseJcamp(String jcamp, String mode, NMRSpectrum nmrspectrum) throws NmrshiftdbException, JCAMPException{
      CMLPeakList numberSpectrum = new CMLPeakList();
      CMLSpectrum cmlSpec = JcampToCMLSpectrumMapper.mapJcampToCMLSpectrum(nmrspectrum, null);
      if (nmrspectrum.hasPeakTable()) {
        Peak[] peaks = nmrspectrum.getPeakTable();
        for (int i = 0; i < peaks.length; i++) {
          CMLPeak peak = new CMLPeak();
          peak.setXValue(Math.round( peaks[i].getPosition()[0] * 100d ) / 100d);
          peak.setYValue(Math.round( peaks[i].getHeight() * 100d ) / 100d);
          numberSpectrum.appendChild(peak);
        }
      } else {
          PeakPicker picker = new PeakPicker(cmlSpec.getSpectrumDataElements().get(0));
          numberSpectrum = picker.getPeakArray();
      }
      ParseUtils.removeDuplicates(numberSpectrum);
      double maxIntensity = 0;
      for (int i = 0; i < numberSpectrum.getPeakElements().size(); i++) {
        numberSpectrum.getPeakElements().get(i).setXValue(Math.round( numberSpectrum.getPeakElements().get(i).getXValue() * 100d ) / 100d);
        numberSpectrum.getPeakElements().get(i).setYValue(Math.round( numberSpectrum.getPeakElements().get(i).getYValue() * 100d ) / 100d);
        if (numberSpectrum.getPeakElements().get(i).getYValue() > maxIntensity) {
          maxIntensity = numberSpectrum.getPeakElements().get(i).getYValue();
        }
      }
      if (maxIntensity > 1) {
        for (int i = 0; i < numberSpectrum.getPeakElements().size(); i++) {
          BigDecimal bd = new BigDecimal(numberSpectrum.getPeakElements().get(i).getYValue() / maxIntensity);
          bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
          numberSpectrum.getPeakElements().get(i).setYValue(bd.doubleValue());
        }
      }
      return numberSpectrum;
  }
  
  
  /**
   *  Tries to read a FileItem via cdk to a cdk molecule, which is set to the session with attributename as key.
   *
   * @param  fi             The FileItem to read.
   * @param  messages       Error messages get appended here.
   * @param  data           The current runData
   * @param  attributename  The key for the session.
   */
  public static void processFileItem(FileItem fi, StringBuffer messages, RunData data, String attributename) {
    IChemFile chemFile = null;
    ISimpleChemObjectReader cor = null;
    /*
     *  Have the ReaderFactory determine the file format
     */
    try {
      cor = getChemObjectReader(fi.getString());
    } catch (Exception exc) {
    	exc.printStackTrace();
      messages.append("Could not determine file format.<br>");
      return;
    }
    if (cor == null) {
      messages.append("Could not determine file format.<br>");
      return;
    }
    String error = null;
    IChemModel chemModel = null;
    if (cor.accepts(new org.openscience.cdk.ChemFile().getClass())) {
      // try to read a ChemFile
      try {
        chemFile = (IChemFile) cor.read((IChemObject) new org.openscience.cdk.ChemFile());
        if (chemFile != null) {
          processChemFile(chemFile, messages, data, attributename);
          return;
        } else {
          //logger.warn("The object chemFile was empty unexpectedly!");
        }
      } catch (Exception exception) {
        error = "Error while reading file: " + exception.getMessage();
        //logger.warn(error);
        //logger.debug(exception);
      }
    }
    if (error != null) {
      messages.append(error + "<br>");
      return;
    }
    if (cor.accepts(new org.openscience.cdk.ChemModel().getClass())) {
      // try to read a ChemModel
      try {
        chemModel = (IChemModel) cor.read((IChemObject) new org.openscience.cdk.ChemModel());
        if (chemModel != null) {
          processChemModel(chemModel, messages, data, attributename);
          return;
        } else {
          //logger.warn("The object chemModel was empty unexpectedly!");
        }
        error = null;
        // overwrite previous problems, it worked now
      } catch (Exception exception) {
        error = "Error while reading file: " + exception.getMessage();
        //logger.error(error);
        //logger.debug(exception);
      }
    }
    if (error != null) {
      messages.append(error + "<br>");
    }
  }


  /**
   *  Generates in inchi for  a molecule.
   *
   * @param  mol            The molecule to generate for.
   * @return                The inchi
   */
  public static String generateInchi(IAtomContainer mol, boolean fixedH) {
      try{
    	  InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    	  InChIGenerator gen;
          if(fixedH){
              List<INCHI_OPTION> options = new ArrayList<INCHI_OPTION>();
    	      options.add(INCHI_OPTION.FixedH);
    	      gen = factory.getInChIGenerator(mol, options);
          }else{
              gen = factory.getInChIGenerator(mol);
    	  }
    	  INCHI_RET ret = gen.getReturnStatus();
    	  if (ret != INCHI_RET.OKAY && ret != INCHI_RET.WARNING)
    		  return "";
    	  else
    		  return gen.getInchi();
      }
      catch(Exception ex){
          return "";
      }
  }
  
  /**
   * Tells if a compound is in pubchem.
   * 
   * @param inchi The inchi of the structure (version 1).
   * @return true=in pubchem, false=not in pubchem.
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XPathExpressionException
   */
  public static boolean isInPubchem(String inchi) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException{
	    String endpoint = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pccompound&term=%22"+URLEncoder.encode(inchi,"UTF-8")+"%22[InChI]";
	    URL url = new URL(endpoint);
	    InputStream in = url.openStream();
	    BufferedInputStream bufIn = new BufferedInputStream(in);
	    StringBuffer doc=new StringBuffer();
	    int data;
	    while((data = bufIn.read())!=-1){
	    	doc.append((char)data);
	    }
	    if(doc.indexOf("<Id>")==-1)
	    	return false;
	    else{
	    	return true;
	    }
  }
  
  
  /**
   * Gets the names of a compound from pubchem. The last entry is the IUPAC name (null if not found), the penulatimate the CAS number (null if not found). 
   * The rest of the entries are mesh terms.
   * 
   * @param inchi The inchi of the structure (version 1).
   * @return The names.
   * @throws IOException
   * @throws ParsingException 
   */
  public static String[] getNamesFromPubchem(String inchi) throws IOException, ParsingException{
	    String endpoint = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pccompound&term=%22"+URLEncoder.encode(inchi,"UTF-8")+"%22[InChI]";
	    URL url = new URL(endpoint);
	    InputStream in = url.openStream();
	    BufferedInputStream bufIn = new BufferedInputStream(in);
	    StringBuffer doc=new StringBuffer();
	    int data;
	    while((data = bufIn.read())!=-1){
	    	doc.append((char)data);
	    }
	    if(doc.indexOf("<Id>")==-1)
	    	return null;
	    else{
	    	String id=doc.substring(doc.indexOf("<Id>")+4,doc.indexOf("</Id>"));
		    endpoint = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pccompound&id="+id;
		    url = new URL(endpoint);
		    in = url.openStream();
		    Builder builder = new Builder();
            nu.xom.Document xmldoc = builder.build(in);
            Nodes result = xmldoc.query("//Item[@Name='SynonymList']/Item/text()");
	    	  String cas=null;
	    	  String trivialname=null;
	    	  for(int i=0;i<result.size();i++){
	    		  if(i==0)
	    			  trivialname=result.get(i).getValue();
    			  if(result.get(i).getValue().matches("[0-9]*-[0-9]*-[0-9]*")){
    				  cas=result.get(i).getValue();
    				  break;
    			  }
	    	  }
	    	  result=xmldoc.query("//Item[@Name='MeSHTermList']/Item/text()");
	    	  String[] names;
	    	  if(result.size()>0)
	    		  names=new String[result.size()+2];
	    	  else{
	    		  names=new String[3];
	    		  names[0]=trivialname;
	    	  }
	    	  if(cas!=null){
	    		  names[names.length-2]=cas;
	    	  }
	    	  for(int i=0;i<result.size();i++){
	    		  names[i]=result.get(i).getValue();
	    	  }
	    	  result = xmldoc.query("//Item[@Name='IUPACName']/text()");
	    	  if(result.size()>0)
	    	  	names[names.length-1]=result.get(0).getValue();
	    	  return names;
	    }
  }

  /**
   * Converts any sort of name into an InChI using Pubchem.
   * 
   * @param name The name
   * @return The InChI. Null if not found in pubchem.
   * @throws IOException Probelems reading from pubchem.
   */
  public static List<String> resolveNameFromPubchem(String name) throws IOException{
	  	BufferedInputStream bufIn = null;
	  	StringBuffer doc=new StringBuffer();
	    try{
	  		String endpoint = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/"+URLEncoder.encode(name,"UTF-8")+"/cids/JSON";
		    URL url = new URL(endpoint);
		    InputStream in = url.openStream();
		    bufIn = new BufferedInputStream(in);
		    int data;
		    while((data = bufIn.read())!=-1){
		    	doc.append((char)data);
		    }
	  	}finally {
	  		if(bufIn!=null)
	  			bufIn.close();
	  	}
	    if(doc.indexOf("CID")==-1)
	    	return null;
	    else{
            ArrayList<String> result = new ArrayList<String>();
            int start=doc.indexOf("[");
            int end=Math.min(doc.indexOf("]", start), doc.indexOf(",", start) > -1 ? doc.indexOf(",", start) : Integer.MAX_VALUE);
   		    String endpoint = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pccompound&id="+doc.substring(start+1,end).trim();
   		  	StringBuffer doc2=new StringBuffer();
   		    try {
   		    	URL url = new URL(endpoint);
    		    InputStream in = url.openStream();
    		    bufIn = new BufferedInputStream(in);
    		    int data;
    		    while((data = bufIn.read())!=-1){
    		    	doc2.append((char)data);
    		    }
        	}finally {
        		bufIn.close();
        	}
	    	if(doc2.indexOf("InChI=")==-1)
	    		return null;
	    	else{
	    		int lastIndex = 0;
	    		while(lastIndex != -1){
	    		       lastIndex = doc2.indexOf("InChI=",lastIndex);
	    		       if( lastIndex != -1){
		    		       String rest = doc2.substring(doc2.indexOf("InChI=",lastIndex));
		    		       result.add(rest.substring(0,rest.indexOf("</Item>")));//GeneralUtils.replace(rest.substring(0,rest.indexOf("</Item>")),"InChI=1S","InChI=1"));
		    		       lastIndex ++;
	    		      }
	    		}
	    	}
	        return result;
	    }
  }

  /**
   *  Generates in inchi key for  a molecule.
   *
   * @param  mdl            The mol to generate for.
   * @return                The inchi key.
   * @exception  Exception  Description of Exception
   */
  public static String generateInchiKey(IAtomContainer mol) throws Exception {
      try{
    	  InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    	  InChIGenerator gen = factory.getInChIGenerator(mol);
    	  INCHI_RET ret = gen.getReturnStatus();
    	  if (ret != INCHI_RET.OKAY && ret != INCHI_RET.WARNING)
    		  return "";
    	  else
    		  return gen.getInchiKey();
      }
      catch(Exception ex){
          return "";
      }
  }
  
  /**
   *  Makes a jpeg out of a mol file.
   *
   * @param  mol            The mol file.
   * @param  filename       The file to write the jpeg to
   * @param  servletConfig  The current servlet configuration.
   * @param  width          Width of jpeg.
   * @param  height         Height of jpeg
   * @param  drawnumbers    Should numbers be contained=
   * @throws CDKException 
   * @throws IOException 
   * @throws TranscoderException 
   * @throws TorqueException 
   */
  public static void makeJpg(String mol, String filename, ServletConfig servletConfig, int width, int height, boolean drawnumbers, DBSpectrumType spectrumType, Map<Integer,String> identifiers, Color backColor) throws CDKException, IOException, TranscoderException, TorqueException {
    IAtomContainer cdkmol = AtomUtils.readMolBothVersions(mol);
    for(int k=0;k<cdkmol.getAtomCount();k++){
        if(cdkmol.getAtom(k).getPoint2d()==null)
        	cdkmol.getAtom(k).setPoint2d(new Point2d(cdkmol.getAtom(k).getPoint3d().x,cdkmol.getAtom(k).getPoint3d().y));
    }
    //We display 6 bonds as zero bonds
    for(IBond bond : cdkmol.bonds()) {
        if(bond.getOrder()==null)
            bond.setOrder(IBond.Order.ZERO);
    }
    if(!AtomUtils.isOrganometallic(cdkmol)) {
        try{
            cdkmol = saturate(cdkmol);
    	}catch(Exception ex){
    	    //fails with unusual elements
    	}
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for(int k=0;k<cdkmol.getAtomCount();k++){
        cdkmol.getAtom(k).setValency(null);
    }        
    List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
    generators.add(new BasicBondGenerator());
    generators.add(new BasicAtomGenerator());
    generators.add(new BasicSceneGenerator());
    AtomContainerRenderer renderer = new AtomContainerRenderer(generators,new AWTFontManager());
    RendererModel r2dm = renderer.getRenderer2DModel();
    r2dm.registerParameters(new AtomNumberGenerator());
    r2dm.set(BasicSceneGenerator.BackgroundColor.class,backColor);
    if(drawnumbers) {
	    for(IAtom atom : cdkmol.atoms()){
	        if(spectrumType!=null && spectrumType.getDBIsotopes().size()>0 && (atom.getSymbol().equals(spectrumType.getDBIsotope(1).getElementSymbol()) || (spectrumType.getDBIsotope(1).getElementSymbol().equals("H") && AtomContainerManipulator.countHydrogens(cdkmol, atom)>0))){
	            if(identifiers.get(cdkmol.getAtomNumber(atom))==null || identifiers.get(cdkmol.getAtomNumber(atom)).equals(""))
	                atom.setProperty(BasicAtomGenerator.IDENTIFIER, ""+(cdkmol.getAtomNumber(atom)+1));
	            else
	                atom.setProperty(BasicAtomGenerator.IDENTIFIER, identifiers.get(cdkmol.getAtomNumber(atom)));
	        }else if(spectrumType==null){
	            atom.setProperty(BasicAtomGenerator.IDENTIFIER, ""+(cdkmol.getAtomNumber(atom)+1));
	        }else{
	            atom.setProperty(BasicAtomGenerator.IDENTIFIER, "");
	        }
	    }
    }
    if (height < 200 || width < 200) {
      r2dm.set(CompactAtom.class,true);
      r2dm.set(BondDistance.class,3.0);
    }
    Rectangle drawArea = new Rectangle(width, height);
    renderer.setup(cdkmol, drawArea);
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document = domImpl.createDocument(null, "svg", null);
    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
    svgGenerator.setBackground(backColor);
    svgGenerator.setColor(backColor);
    svgGenerator.fill(new Rectangle(0, 0, width, height));
    renderer.paint(cdkmol, new AWTDrawVisitor(svgGenerator), drawArea, false);
    boolean useCSS = false;
    baos = new ByteArrayOutputStream();
    Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
    StringBuffer sb = new StringBuffer();
    svgGenerator.stream(outwriter, useCSS);
    StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
    while (tokenizer.hasMoreTokens()) {
      String name = tokenizer.nextToken();
      if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
        sb.append(name.substring(0, name.length())).append(" width=\"" + width + "\" height=\"" + height + "\"" + "\n\r");
      } else {
        sb.append(name + "\n\r");
      }
    }
    File outputFile = null;
    if(servletConfig==null)
    	outputFile = new File(filename);
    else
    	outputFile = new File(ServletUtils.expandRelative(servletConfig, filename));
    ImageTranscoder it = new JPEGTranscoder();
    it.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
    TranscoderInput input = new TranscoderInput(new StringReader(sb.toString()));
    OutputStream ostream = new FileOutputStream(outputFile);
    TranscoderOutput output = new TranscoderOutput(ostream);
    it.transcode(input, output);
    ostream.flush();
    ostream.close();
  }


  /**
   *  saturates a molecule with (implicit) protons and bonds.
   *
   * @param  cdkmol         The molecule to saturate.
   * @return                The molecule with implicit protons. original molecule will have explicit protons.
   * @exception  CDKException  Description of Exception
   */
  public static IAtomContainer saturate(IAtomContainer cdkmol) throws CDKException {
	    AtomUtils.addImplicitHydrogensToSatisfyValency(cdkmol);
	    IBond[] bonds=new IBond[cdkmol.getBondCount()];
	    for (int i = 0; i < cdkmol.getBondCount(); i++) {
	      bonds[i]=cdkmol.getBond(i);
	      if (cdkmol.getBond(i).getFlag(CDKConstants.ISAROMATIC) == true) {
	        cdkmol.getBond(i).setOrder(IBond.Order.SINGLE);
	      }
	    }
	   
	    new SaturationChecker().newSaturate(bonds, cdkmol);
	    for (int i = 0; i < cdkmol.getBondCount(); i++) {
	      cdkmol.getBond(i).setFlag(CDKConstants.ISAROMATIC, false);
	    }
	    return cdkmol;
  }


  /**
   *  Recursivly zips a directory in an zipoutputstream
   *
   * @param  zipDir           The directory.
   * @param  zos              The stream
   * @exception  IOException  Description of Exception
   */
  public static void zipDir(File zipDir, ZipOutputStream zos, File rawdatadir, String usersid,  int expcount) throws IOException, TorqueException {
    if(zipDir.isFile()){
    	int bytesIn = 0;
    	byte[] readBuffer = new byte[2156];
        if(!alreadyAttached.contains(zipDir.getPath())){
        	FileInputStream fis = new FileInputStream(zipDir);
            ZipEntry anEntry = new ZipEntry(zipDir.getPath());
        	alreadyAttached.add(zipDir.getPath());
        	//place the zip entry in the ZipOutputStream object
        	zos.putNextEntry(anEntry);
        	//now write the content of the file to the ZipOutputStream
        	while ((bytesIn = fis.read(readBuffer)) != -1) {
        		zos.write(readBuffer, 0, bytesIn);
        	}
        	//close the Stream
        	fis.close();
        }
    }else{
	    //get a listing of the directory content
	    String[] dirList = zipDir.list();
	    byte[] readBuffer = new byte[2156];
	    int bytesIn = 0;
	    //loop through dirList, and zip the files
	    for (int i = 0; i < dirList.length; i++) {
	      File f = new File(zipDir, dirList[i]);
	      if (f.isDirectory()) {
	        //if the File object is a directory, call this
	        //function again to add its content recursively
	        String filePath = f.getPath();
	        zipDir(new File(filePath), zos,rawdatadir,usersid,expcount);
	        //loop again
	        continue;
	      }else if(f.getName().equals("audita.txt")) {
				AssignRobotDaemon.analyzeAudita(expcount, usersid, f);
				expcount++;
	      }
	      if(!alreadyAttached.contains(getRelativePath(rawdatadir,f))){
		      //if we reached here, the File object f was not a directory
		      //create a FileInputStream on top of f
		      FileInputStream fis = new FileInputStream(f);
		      ZipEntry anEntry = new ZipEntry(getRelativePath(rawdatadir,f));
		      alreadyAttached.add(getRelativePath(rawdatadir,f));
		      //place the zip entry in the ZipOutputStream object
		      zos.putNextEntry(anEntry);
		      //now write the content of the file to the ZipOutputStream
		      while ((bytesIn = fis.read(readBuffer)) != -1) {
		        zos.write(readBuffer, 0, bytesIn);
		      }
		      //close the Stream
		      fis.close();
	      }
	  }
    }
  }


	/**
	 * break a path down into individual elements and add to a list.
	 * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
	 * @param f input file
	 * @return a List collection with the individual elements of the path in reverse order
	 */
	private static List<String> getPathList(File f) {
		List<String> l = new ArrayList<String>();
		File r;
		try {
			r = f.getCanonicalFile();
			while(r != null) {
				l.add(r.getName());
				r = r.getParentFile();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			l = null;
		}
		return l;
	}

	/**
	 * figure out a string representing the relative path of
	 * 'f' with respect to 'r'
	 * @param r home path
	 * @param f path of file
	 */
	private static String matchPathLists(List<String> r,List<String> f) {
		int i;
		int j;
		String s;
		// start at the beginning of the lists
		// iterate while both lists are equal
		s = "";
		i = r.size()-1;
		j = f.size()-1;

		// first eliminate common root
		while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for(;i>=0;i--) {
			s += ".." + File.separator;
		}

		// for each level in the file path, add the path
		for(;j>=1;j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		s += f.get(j);
		return s;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory
	 * example : home = /a/b/c
	 *           f    = /a/d/e/x.txt
	 *           s = getRelativePath(home,f) = ../../d/e/x.txt
	 * @param home base path, should be a directory, not a file, or it doesn't make sense
	 * @param f file to generate path for
	 * @return path from home to f as a string
	 */
	public static String getRelativePath(File home,File f){
		List<String> homelist;
		List<String> filelist;
		String s;

		homelist = getPathList(home);
		filelist = getPathList(f);
		s = matchPathLists(homelist,filelist);

		return s;
	}


	/**
   *  Gets a chemObjectReader for a file.
   *
   * @param  file             The file.
   * @return                  The chemObjectReader.
   * @exception  IOException  Description of Exception
   */
  private static ISimpleChemObjectReader getChemObjectReader(String file) throws IOException {
    Reader fileReader = new StringReader(file);
    ReaderFactory factory = new ReaderFactory();
    ISimpleChemObjectReader reader = factory.createReader(fileReader);
    return reader;
  }


  /**
   *  Tries to read a chemfile via cdk to a cdk molecule, which is set to the session with attributename as key.
   *
   * @param  chemFile       The chemfile.
   * @param  messages       Error messages get appended here.
   * @param  data           The current runData
   * @param  attributename  The key for the session.
   */
  private static void processChemFile(IChemFile chemFile, StringBuffer messages, RunData data, String attributename) {
    int chemSequenceCount = chemFile.getChemSequenceCount();
    IChemSequence chemSequence = null;
    IChemModel chemModel = null;

    for (int i = 0; i < chemSequenceCount; i++) {
      chemSequence = chemFile.getChemSequence(i);

      int chemModelCount = chemSequence.getChemModelCount();

      for (int j = 0; j < chemModelCount; j++) {
        chemModel = chemSequence.getChemModel(j);
        processChemModel(chemModel, messages, data, attributename);
      }
    }
  }


  /**
   *  Tries to read a chemmodel via cdk to a cdk molecule, which is set to the session with attributename as key.
   *
   * @param  chemModel      The chemModel to read.
   * @param  messages       Error messages get appended here.
   * @param  data           The current runData
   * @param  attributename  The key for the session.
   */
  private static void processChemModel(IChemModel chemModel, StringBuffer messages, RunData data, String attributename) {
    // check for bonds
    if (ChemModelManipulator.getBondCount(chemModel) == 0) {
      String error = "Model does not have bonds. Cannot depict contents.";
      messages.append(error + "<br>");
      return;
    }

    // check for coordinates
    List<IAtomContainer> acs = ChemModelManipulator.getAllAtomContainers(chemModel);
    for(int i=0;i<acs.size();i++){
	    if (GeometryTools.has2DCoordinatesNew(acs.get(i))!=2) {
	
	      String error = "Model does not have coordinates. Cannot open file.";
	      messages.append(error + "<br>");
	      return;
	    }
    }
    IAtomContainer mol = chemModel.getMoleculeSet().getAtomContainer(0);
    data.getSession().setAttribute(attributename, mol);
  }

  
  	public static String readToMol(String input) throws Exception{
	  IChemFile chemFile=null;
	  ISimpleChemObjectReader cor=null;
	  /* Have the ReaderFactory determine the file format */
	  cor = getChemObjectReader(input);
	  if (cor == null) {
		  throw new NmrshiftdbException("Could not determine file format");
	  }
	  IChemModel chemModel = null;
	  if (cor.accepts(new org.openscience.cdk.ChemFile().getClass())) { // try to read a ChemFile
	          chemFile = (IChemFile)cor.read((IChemObject)new org.openscience.cdk.ChemFile());
	          if (chemFile != null) {
	              return processChemFile(chemFile);
	          }
	  }
	  if (cor.accepts(new org.openscience.cdk.ChemModel().getClass())) { // try to read a ChemModel
	          chemModel = (IChemModel)cor.read((IChemObject)new org.openscience.cdk.ChemModel());
	          if (chemModel != null) {
	              return processChemModel(chemModel);
	          }
	  }
	  throw new NmrshiftdbException("Problems reading your input");
  	}

  	
	private static String processChemFile(IChemFile chemFile) throws Exception {
        if(chemFile.getChemSequenceCount()>0 && chemFile.getChemSequence(0).getChemModelCount()>0)
            return processChemModel(chemFile.getChemSequence(0).getChemModel(0));
		throw new NmrshiftdbException("Problems reading your input");
	}


	private static String processChemModel(IChemModel chemModel) throws Exception{
		// check for bonds
		if (ChemModelManipulator.getBondCount(chemModel) == 0) {
			throw new NmrshiftdbException("Model does not have bonds. Cannot depict contents");
		}
		
		// check for coordinates
	    // check for coordinates
	    List<IAtomContainer> acs = ChemModelManipulator.getAllAtomContainers(chemModel);
	    for(int i=0;i<acs.size();i++){
		    if (GeometryTools.has2DCoordinatesNew(acs.get(i))!=2) {
		    	throw new NmrshiftdbException("Model does not have coordinates. Cannot open file");
		    }
	    }
	    IAtomContainer mol = chemModel.getMoleculeSet().getAtomContainer(0);
        StringWriter sw = new StringWriter();
		MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
		mdlwriter.writeMolecule(mol);
		mdlwriter.close();
		return sw.toString();
	}

	public static Properties getJetspeedprops() {
		return jetspeedprops;
	}

	public static void setJetspeedprops(Properties jetspeedprops) {
		GeneralUtils.jetspeedprops = jetspeedprops;
	}

	
	/**
	 * Doeas an assignemnt for a spectrum and molecule. The input must conform to NMRShiftDB
	 * convention (http://nmrshiftdb2.sourceforge.net/nmrshiftdb-convention.html). If a 1H spectrum 
	 * is predicted, the molecule must contain all explicit Hs, in other cases it may contain some or all, 
	 * but this must be after all atoms with other elements in the atomlist. The input file must not contain 
	 * atomRefs in the signals. The assignments are added to the CML file
	 * 
	 * @param cmlcml The input
	 * @return Two maps, the first is a map from atom number to hose codes, and the second from atom number to result.
	 * @throws Exception
	 */
	public static List<Map<Integer, Object>> assignCML(CMLCml cmlcml) throws Exception{
        if(cmlcml.getCMLChildCount("molecule")!=1){
            throw new NmrshiftdbException("There must be exactly one molcule in cml");
	    }
        List<Map<Integer, Object>> sideresults=new ArrayList<Map<Integer, Object>>();
        Map<Integer,Object> hosemap=new HashMap<>();
        sideresults.add(hosemap);
        Map<Integer,Object> resultmap=new HashMap<>();
        sideresults.add(resultmap);
	    CMLMolecule cmlmol=(CMLMolecule)cmlcml.getChildCMLElement("molecule",0);
		IChemFile cfile=new ChemFile();
		CMLReader cmlreader=new CMLReader(new ByteArrayInputStream(cmlmol.toXML().getBytes()));
		cfile=(IChemFile)cmlreader.read(cfile);
		cmlreader.close();
		IAtomContainer mol=cfile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
		AtomUtils.addAndPlaceHydrogens(mol);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		Elements spectra=cmlcml.getChildCMLElements("spectrum");
	    if(spectra.size()==0){
	            throw new NmrshiftdbException("There must be at least one spectrum in cml");
	    }
		for(int i=0;i<spectra.size();i++){
			CMLSpectrum spectrum=(CMLSpectrum)spectra.get(i);
			String spectype=GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(), "nmr:OBSERVENUCLEUS").get(0).getContent();
			DBSpectrumType dbspectype=DBSpectrumTypePeer.getByName(spectype);
            Multimap<Double,Integer> predictions=ArrayListMultimap.create();
            List<CMLPeak> spectrumPredicted = new ArrayList<CMLPeak>();
            boolean[] atomassigned=new boolean[mol.getAtomCount()];
            for(int k=0;k<mol.getAtomCount();k++){
                    if(dbspectype.getDBIsotope(1).getElementSymbol().equals(mol.getAtom(k).getSymbol())){
                            try{
                                    StringBuffer hosecode=new StringBuffer();
                                    StringBuffer maxhosecode=new StringBuffer();
                                    double[] result=AtomUtils.predictRange((IAtomContainer)mol,mol.getAtom(k),false,true,true,false,-1,new StringBuffer(),true,true,null,new HashMap<Double,double[]>(),6,true,hosecode,true,null,maxhosecode);
                                    if(!predictions.containsKey(result[1])){
	                                    CMLPeak peak = new CMLPeak();
	                                    peak.setXValue(result[1]);
	                                    spectrumPredicted.add(peak);
                                    }
                                    predictions.put(result[1], k);
                                    resultmap.put(k, result);
                                    hosemap.put(k, maxhosecode.toString());
                            }catch(Exception ex){
                                    ex.printStackTrace();
                                    //do nothing if predidiciton fails for single atom
                            }
                    }
            }
            List<CMLPeak> realpeaks = spectrum.getPeakListElements().get(0).getPeakChildren();
            //TODO should multiplicities be used?
            Map<Double, Double> matches = GeneralUtils.getMatchingSearchShift(realpeaks, spectrumPredicted, 0, new Vector<Double>(), false);
            Iterator<Double> it = matches.keySet().iterator();
            List<Double> usedpredictions=new ArrayList<>();
            while(it.hasNext()){
                double realpeak = it.next();
                for(int l=0;l<realpeaks.size();l++){
                    if(Math.abs(realpeaks.get(l).getXValue()-realpeak)<.0001){
                        double predictedpeak=matches.get(realpeak);
                        Iterator<Double> it2 = predictions.keySet().iterator();
                        while(it2.hasNext()){
                            double predictionkey = it2.next();
                           if(Math.abs(predictionkey-predictedpeak)<0.001 && !usedpredictions.contains(predictionkey)){
                            	usedpredictions.add(predictionkey);
                            	for(int atomnumber : predictions.get(predictionkey)){
                            		realpeaks.get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(realpeaks.get(l).getAtomRefs(),mol.getAtom(atomnumber)));
                            		atomassigned[atomnumber]=true;
                            	}
                                break;
                            }
                        }
                        break;
                    }
                }
            }
		}
		return sideresults;
	}
	
    /**
     * THIS METHOD IS COPIED FROM JUMBO CMLMetadataListElement, since it is private there.
     * 
     * gets metadata elements with a given name. if either param is null,
     * returns empty list
     *
     * @param metadataList
     *            list to filter
     * @param name
     *            value of name attribute
     * @return filtered list
     */
    public static List<CMLMetadata> getMetadataDescendantsByName(
            List<CMLMetadata> metadataList, String name) {
        List<CMLMetadata> newMetadataList = new ArrayList<CMLMetadata>();
        if (name != null && metadataList != null) {
            for (CMLMetadata metadata : metadataList) {
                if (name.equals(metadata.getName())) {
                    newMetadataList.add(metadata);
                }
            }
        }
        return newMetadataList;
    }
    
    /**
     * Ggets all CMLMetadataLists in an CMLmolecule.
     * 
     * @param molecule The molecule to search in.
     * @return The CMLMetadataLists.
     */
    public static CMLElements<CMLMetadataList> getMetadataListElements(CMLMolecule molecule) {
        Elements elements = molecule.getChildElements("metadataList", NmrshiftdbConstants.CML_NAMESPACE);
        return new CMLElements<CMLMetadataList>(elements);
    }
    
    public static byte[] downloadUrl(String url) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        InputStream stream = new URL(url).openStream();

        while ((bytesRead = stream.read(chunk)) > 0) {
            outputStream.write(chunk, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    /**
     * Sends an email to all workers in a labgroup.
     * 
     * @param dbLabGroup
     * @param header
     * @param emailtext
     * @param servcon
     * @throws TorqueException 
     * @throws IOException 
     * @throws SQLException 
     */
    public static void sendEmailToLabgroupWorkers(DBLabGroup dbLabGroup, String header, String emailtext, ServletConfig servcon) throws TorqueException, IOException, SQLException {
        List<TurbineUser> members = dbLabGroup.getMembers();
        for(int i=0;i<members.size();i++){
            if(((TurbineUser)members.get(i)).isLabgroupWorker()){
                MailMessage email = new org.apache.turbine.util.mail.MailMessage(getSmtpServer(servcon), ((TurbineUser)members.get(i)).getEmail(), getAdminEmail(servcon), header, emailtext);
                if (email.send() == false) {
                  Log.error("Sending email to failed for "+emailtext);
                }                
            }
        }
    }

    /**
     * Removes duplicates (defined by xValue) from a list of CMLPeaks.
     * 
     * @param peaks The peaks to consider.
     */
    public static void removeDuplicates(CMLPeakList peaks) {
        TreeMap<Integer,CMLPeak> duplicates=new TreeMap<Integer, CMLPeak>();
        for(int i=0;i<peaks.getPeakChildren().size();i++){
            for(int k=i+1;k<peaks.getPeakChildren().size();k++){
                if(peaks.getPeakChildren().get(i).getXValue()==peaks.getPeakChildren().get(k).getXValue()){
                    duplicates.put(k,peaks.getPeakChildren().get(k));
                    break;
                }
            }
        }
        for(Integer peak : duplicates.keySet()){
           peaks.removeChild(duplicates.get(peak));
        }
    }

	/**
	 * Reads an mnova exported sdf into two SubmittingData objects. If there is no 13c or 1H spectrum the peaklist of the respective object will be empty. 
	 * Structure etc. are put into both object.
	 * 
	 * @param mnovafile  The file to read.
	 * @param subDataC   The 13C data.
	 * @param subDataH   The 1H data.
	 * @param messages   Error messages get appended here.
	 * @param noesypeaks 
	 * @param hsqcpeaks 
	 * @param hmbcpeaks 
	 * @param tocsypeaks 
	 * @param cosypeaks 
	 * @throws CDKException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws TorqueException
	 */
	public static void readMnovafile(File mnovafile, SubmittingData subDataC, SubmittingData subDataH, StringBuffer messages, List<Peak2D> cosypeaks, List<Peak2D> tocsypeaks, List<Peak2D> hmbcpeaks, List<Peak2D> hsqcpeaks, List<Peak2D> noesypeaks) throws CDKException, ClassNotFoundException, IOException, TorqueException, DataSetException {
        MDLV2000Reader mdlreader = null;
        IAtomContainer molWithH;
        //TODO read 2d
        try{
            mdlreader=new MDLV2000Reader(new FileReader(mnovafile));
            molWithH = (IAtomContainer) mdlreader.read(new org.openscience.cdk.AtomContainer());
        }finally{
            mdlreader.close();
        }
        mdlreader.close();
        subDataC.setMol(molWithH, false, false, false);
        subDataH.setMol(molWithH, false, false, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mnovafile)));
        String line = null;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("M  ZZC")){
                StringTokenizer st2=new StringTokenizer(line, " ");
                st2.nextToken();
                st2.nextToken();
                int atomnumber=Integer.parseInt(st2.nextToken())-1;
                String label=st2.nextToken().trim();
                subDataC.addIdentifier(atomnumber, label);
                subDataH.addIdentifier(atomnumber, label);
            }
            if(line.startsWith("V  ")){
                StringTokenizer st2=new StringTokenizer(line, " ");
                st2.nextToken();
                int atomnumber=Integer.parseInt(st2.nextToken())-1;
                String label=st2.nextToken().trim();
                subDataC.addIdentifier(atomnumber, label);
                subDataH.addIdentifier(atomnumber, label);
            }
            if(line.startsWith(">  <CHEMICAL_SHIFTS.13C>")){
                while (!(line = br.readLine()).trim().equals("")) {
                    int atomnumber=Integer.parseInt(line.substring(0,line.indexOf(',')))-1;
                    double shift=Double.parseDouble(line.substring(line.indexOf(',')+1));
                    CMLPeak peak = new CMLPeak();
                    peak.setXValue(shift);
                    peak.setYValue(0);
                    peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), subDataC.getMolWithH().getAtom(atomnumber)));
                    subDataC.addToSignalstable(peak);
                }
            }
            if(line.startsWith(">  <CHEMICAL_SHIFTS.1H>")){
                while (!(line = br.readLine()).trim().equals("")) {
                    String atomnumberstring=line.substring(0,line.indexOf(','));
                    boolean isSecondh=false;
                    if(atomnumberstring.indexOf("''")>-1){
                        isSecondh=true;
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-2);
                    }else if(atomnumberstring.indexOf("'")>-1){
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-1);
                    }else if(atomnumberstring.indexOf("alpha")>-1){
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-5);
                    }else if(atomnumberstring.indexOf("beta")>-1){
                        isSecondh=true;
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-4);
                    }else if(atomnumberstring.indexOf("ax")>-1){
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-2);
                    }else if(atomnumberstring.indexOf("eq")>-1){
                        isSecondh=true;
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-2);
                    }else if(atomnumberstring.indexOf("cis")>-1){
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-3);
                    }else if(atomnumberstring.indexOf("trans")>-1){
                        isSecondh=true;
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-5);
                    }else if(atomnumberstring.indexOf("a")>-1){
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-1);
                    }else if(atomnumberstring.indexOf("b")>-1){
                        isSecondh=true;
                        atomnumberstring=atomnumberstring.substring(0,atomnumberstring.length()-1);
                    }
                    int atomnumber=Integer.parseInt(atomnumberstring)-1;
                    double shift=Double.parseDouble(line.substring(line.indexOf(',')+1));
                    CMLPeak peak = new CMLPeak();
                    peak.setXValue(shift);
                    peak.setYValue(0);
                    if(subDataH.getMolWithH().getAtom(atomnumber).getSymbol().equals("H")){
                        peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), subDataH.getMolWithH().getAtom(atomnumber)));
                    }else{
                        int firsthnumber=0;
                        int secondhnumber=0;
                        boolean firsth=true;
                        for(int k=0;k<molWithH.getConnectedAtomsCount(molWithH.getAtom(atomnumber));k++){
                            if(molWithH.getConnectedAtomsList(molWithH.getAtom(atomnumber)).get(k).getSymbol().equals("H")){
                                if(firsth){
                                    firsthnumber=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(atomnumber)).get(k));
                                    firsth=false;
                                }else{
                                    secondhnumber=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(atomnumber)).get(k));
                                    break;
                                }
                            }
                        }
                        if(isSecondh)
                            peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), subDataH.getMolWithH().getAtom(secondhnumber)));
                        else
                            peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), subDataH.getMolWithH().getAtom(firsthnumber)));
                    }
                    subDataH.addToSignalstable(peak);
                }
            }
        }
        br.close();
        //we get multiplicities from h count
        IAtomContainer mol = subDataC.getMolWithH();
        for (int i = 0; i < mol.getAtomCount(); i++) {
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
        mnovafile.delete();
	}

	public static void readTopsinfile(StringBuffer correlationFile, StringBuffer structure, SubmittingData subDataC, SubmittingData subDataH, Map<String,SubmittingData> otherspectra,
			StringBuffer messages, List<Peak2D> cosypeaks, List<Peak2D> tocsypeaks, List<Peak2D> hmbcpeaks, List<Peak2D> hsqcpeaks, List<Peak2D> noesypeaks, String pathtowebapp) throws ClassNotFoundException, IOException, CDKException, NoRowsException, TooManyRowsException, TorqueException, NmrshiftdbException, DataSetException {
		MDLV2000Reader mdlreader = new MDLV2000Reader(new StringReader(structure.toString()));
	    IAtomContainer mol = (IAtomContainer) mdlreader.read(new AtomContainer());
	    mdlreader.close();
	    subDataC.setMol(mol, false, false, false);
	    //since setmol adds Hs, we need a fresh  molecule
		mdlreader = new MDLV2000Reader(new StringReader(structure.toString()));
	    mol = (IAtomContainer) mdlreader.read(new AtomContainer());
	    mdlreader.close();
	    subDataH.setMol(mol, false, false, false);
	    AtomUtils.addAndPlaceHydrogens(mol);
		StringTokenizer st=new StringTokenizer(correlationFile.toString(),"\n\r");
		st.nextToken();//we skip header line
		List<Integer> alreadydonehs=new ArrayList<Integer>();
		Map<String,Integer> alreadydonehshifts=new HashMap<String,Integer>();
		Map<Integer,Integer> htoheavy=new HashMap<Integer,Integer>();
		String typesofar=null;
		while(st.hasMoreTokens()){
			String line=st.nextToken();
			if(line.trim().equals(""))
				break;
			StringTokenizer st2=new StringTokenizer(line,",");
			String type=st2.nextToken();
			if(type.equals("ROESY"))
				type="NOESY";
			if(otherspectra.get(type)==null){
				SubmittingData subData = new SubmittingData(pathtowebapp);
				subData.setMol(subDataC.getMolOriginal(), false, false, false);
				//TODO better mapping?
				if(type.equals("HSQC"))
					subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("H,C-HSQC"));
				else if(type.equals("HMBC"))
					subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("H,C-HMBC"));
				else if(type.equals("COSY"))
					subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("H,H-COSY"));
				else if(type.equals("NOESY"))
					subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("H,H-NOESY"));
				otherspectra.put(type, subData);
			}
			if(typesofar!=null && !typesofar.equals(type)){
				alreadydonehs=new ArrayList<Integer>();
			}
			typesofar=type;
			int hnumber=Integer.parseInt(st2.nextToken().substring(1))-1;
			String shiftstring=st2.nextToken();
			IAtomContainer molwithh=subDataH.getMolWithH();
			if(!shiftstring.equals("?")){
				double hshift=Double.parseDouble(shiftstring);
				String atomline=st2.nextToken();
				if((type.equals("HSQC") || type.equals("HMBC"))){
					int cnumber=0;
					if(atomline.indexOf(' ')>0)
						cnumber=Integer.parseInt(atomline.substring(1,atomline.indexOf(' ')))-1;//the line is line CX ..., we want X
					else
						cnumber=Integer.parseInt(atomline.substring(1))-1;
					String heavyelement=atomline.substring(0,1);
					if(type.equals("HSQC"))
						htoheavy.put(hnumber, cnumber);
					st2.nextToken();
					String cshiftstring=st2.nextToken();
		            int realhnumber=0;
		            List<IAtom> connatoms = mol.getConnectedAtomsList(mol.getAtom(htoheavy.get(hnumber)));
		            boolean firsthpassed=false;
		            boolean secondh=false;
		            for(int i=0;i<connatoms.size();i++){
		            	if(connatoms.get(i).getSymbol().equals("H")){
		            		boolean alreadydone=alreadydonehs.contains(htoheavy.get(hnumber));
		            		if((alreadydone && firsthpassed) || (!alreadydone && !firsthpassed)){
		            			alreadydonehs.add(htoheavy.get(hnumber));
		            			realhnumber=mol.getAtomNumber(connatoms.get(i));
		            			secondh=true;
		            			break;
		            		}
	        				firsthpassed=true;
		            	}
		            }
					if(!cshiftstring.equals("?")){
						double cshift=Double.parseDouble(cshiftstring);
			            CMLPeak peak = new CMLPeak();
			            peak.setXValue(cshift);
			            peak.setYValue(0);
			            peak.setPeakMultiplicity(GeneralUtils.getMultiplicity(AtomContainerManipulator.countExplicitHydrogens(molwithh, molwithh.getAtom(cnumber)), false));
			            peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), "a"+Integer.toString(cnumber+1)));
			            if(heavyelement.equals("C"))
			            	subDataC.addToSignalstable(peak);
			            if(!alreadydonehshifts.keySet().contains(htoheavy.get(hnumber)+" "+hshift)){
				            peak = new CMLPeak();
				            peak.setXValue(hshift);
				            peak.setYValue(0);
				            if(secondh)
		            			peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), "a"+Integer.toString(realhnumber+1)));
				            else
				            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(), "a"+Integer.toString(cnumber+1)));
				            peak.setPeakMultiplicity("");
				            if(peak.getAtomRefs()!=null && peak.getAtomRefs().length>0){
				            	subDataH.addToSignalstable(peak);
				            	alreadydonehshifts.put(htoheavy.get(hnumber)+" "+hshift, realhnumber);
				            }
			            }else{
			            	realhnumber=alreadydonehshifts.get(htoheavy.get(hnumber)+" "+hshift);
			            }
			            //2d peak
			            peak = new CMLPeak();
			            peak.setXValue(hshift);
			            peak.setYValue(cshift);
			            peak.setPeakMultiplicity("");
			            String[] refs = {"a"+(cnumber+1), "a"+(realhnumber+1)};
			            peak.setAtomRefs(refs);
			            otherspectra.get(type).getSignalstable().addPeak(peak);
						Peak2D peak2d=new Peak2D(hshift,cshift,0);
			            peak2d.assignments=new ArrayList<Assignment>();
			            Assignment ass=new Assignment(new Pattern(0.0,"test"));
			            ass.setTargets(new IAssignmentTarget[] {new AtomReference("", (cnumber+1)), new AtomReference("", (realhnumber+1))});
			            peak2d.assignments.add(ass);
						if(type.equals("HSQC") && hsqcpeaks!=null){
							hsqcpeaks.add(peak2d);
						}else if(hmbcpeaks!=null){
							hmbcpeaks.add(peak2d);
						}
					}
				}
				if(type.equals("COSY") || type.equals("NOESY")){
					int cnumber=0;
					if(atomline.indexOf(' ')>0)
						cnumber=Integer.parseInt(atomline.substring(1,atomline.indexOf(' ')))-1;//the line is line HX ..., we want X
					else
						cnumber=Integer.parseInt(atomline.substring(1))-1;
					st2.nextToken();
					String cshiftstring=st2.nextToken();//this is a h shift as well
					if(!cshiftstring.equals("?")){
						double cshift=Double.parseDouble(cshiftstring);
						if(htoheavy!=null && htoheavy.get(hnumber)!=null && htoheavy.get(cnumber)!=null && alreadydonehshifts.get(htoheavy.get(hnumber)+" "+hshift)!=null && alreadydonehshifts.get(htoheavy.get(cnumber)+" "+cshift)!=null) {
							int realhnumber=alreadydonehshifts.get(htoheavy.get(hnumber)+" "+hshift);
							int realhnumber2=alreadydonehshifts.get(htoheavy.get(cnumber)+" "+cshift);
				            CMLPeak peak = new CMLPeak();
				            peak.setXValue(hshift);
				            peak.setYValue(cshift);
				            peak.setPeakMultiplicity("");
				            String[] refs = {"a"+(realhnumber2+1), "a"+(realhnumber+1)};
				            peak.setAtomRefs(refs);
				            otherspectra.get(type).getSignalstable().addPeak(peak);
				            //lower signal needs to go first
							Peak2D peak2d=new Peak2D(hshift,cshift,0);
				            if(hshift>cshift)
								peak2d=new Peak2D(cshift,hshift,0);
				            peak2d.assignments=new ArrayList<Assignment>();
				            Assignment ass=new Assignment(new Pattern(0.0,"test"));
				            ass.setTargets(new IAssignmentTarget[] {new AtomReference("", (realhnumber2+1)), new AtomReference("", (realhnumber+1))});
				            peak2d.assignments.add(ass);
							if(type.equals("COSY") && cosypeaks!=null){
								cosypeaks.add(peak2d);
							}else if(noesypeaks!=null){
								noesypeaks.add(peak2d);
							}
						}
					}
				}
			}
		}		
	}

	public static void analyzeTopspinDir(RunData runData, Context context, SubmittingData subDataC, SubmittingData subDataH, Map<String,SubmittingData> otherspectra, List<Peak2D> cosypeaks, List<Peak2D> tocsypeaks, List<Peak2D> hmbcpeaks, List<Peak2D> hsqcpeaks, List<Peak2D> noesypeaks, StringBuffer messages) throws SQLException, DataSetException, Exception {
		  Map<String,String> brukertypes=new HashMap<String,String>();
		  brukertypes.put(";zg", "1H");
		  brukertypes.put(";zgdc", "13C");
	      brukertypes.put(";cosy", "H,H-COSY");
	      brukertypes.put(";hmbc", "H,C-HMBC");
	      brukertypes.put(";hsqc", "H,C-HSQC");
	      brukertypes.put(";hmqc", "H,C-HMQC");
	      brukertypes.put(";deptq", "DEPTQ");
	      brukertypes.put(";apt", "APT");
	      brukertypes.put(";jres","J-resolved");
	      brukertypes.put(";noesy","H,H-NOESY");
	      context.put("messages",messages);
		  //we look if there is a zip file
	      String relativepath = ServletUtils.expandRelative(runData.getServletConfig(),"/WEB-INF/tmp");
	      File topspinfile=new File(relativepath+"/uploadtopspin"+runData.getSession().getId());
	      if(!topspinfile.exists()){
	    	  messages.append("It seems you did not upload a file!");
	    	  return;
	      }
	      runData.getSession().setAttribute("topspinfile", topspinfile);
	      ZipInputStream zipIn = new ZipInputStream(new FileInputStream(topspinfile));
	      ZipEntry entry = null;
	      // iterates over entries in the zip file
	      StringBuffer correlationFile=null;
	      StringBuffer structure=null;
	      boolean struceludir=false;
	      //Here we create a new zip for each experiment (i.e. numbered subfolder)
	      Map<Integer,ZipOutputStream> zips=new HashMap<Integer,ZipOutputStream>();
	      Map<Integer,String> metadata=new HashMap<Integer,String>();
	      Map<Integer,ByteArrayOutputStream> baoses=new HashMap<Integer,ByteArrayOutputStream>();
	      Map<Integer,DBSpectrumType> types = new HashMap<Integer,DBSpectrumType>();
	      int type1h=0;
	      int type13c=0;
	      StringBuffer assignmentMethod=new StringBuffer();
		  ByteArrayOutputStream strucelubaos=new ByteArrayOutputStream();
		  ZipOutputStream struceluzos=new ZipOutputStream(strucelubaos);
		  int deptq=0;
		  int apt=0;
		  while ((entry=zipIn.getNextEntry()) != null) {
	    	  if(entry.getName().contains("strucelu"))
	    		  struceludir=true;
	    	  String file = Paths.get(entry.getName()).toString();
	    	  if(!entry.isDirectory()){	    		  
	    		  if(file.contains("strucelu")){
	    			  struceluzos.putNextEntry(new ZipEntry(file));
	    			  if(file.endsWith("AtomCorrelationTable.csv")){
	    				  correlationFile=new StringBuffer();
	    			  }
	    			  if(file.endsWith(".sdf") && !file.endsWith("Fragments.sdf")){
	    				  structure=new StringBuffer();	    				  
	    			  }
	    			  byte[] buffer = new byte[1024];
	    			  int read=1;
	    			  while((read=zipIn.read(buffer))>0){
	    				  struceluzos.write(buffer);
		    			  if(file.endsWith("AtomCorrelationTable.csv")){
		    				  correlationFile.append(new String(buffer,0,read,"US-ASCII"));
		    			  }
		    			  if(file.endsWith(".sdf") && !file.endsWith("Fragments.sdf")){
		    				  structure.append(new String(buffer,0,read,"US-ASCII"));		    				  
		    			  }
	    			  }
	    			  struceluzos.closeEntry();
	    		  }else if(file.matches(".*/[0-9]*/.*") && !file.matches(".*/[0-9]*/pdata/.*/1i") && !file.matches(".*/[0-9]*/pdata/.*/1r") && !file.matches(".*/[0-9]*/pdata/.*/2rr") && !file.matches(".*/[0-9]*/pdata/.*/2ii") && !file.matches(".*/[0-9]*/pdata/.*/2ir") && !file.matches(".*/[0-9]*/pdata/.*/2ri")){
	    			  //we have a file from an experiment directory
	    			  byte[] buffer = new byte[2048];
	    			  java.util.regex.Pattern pattern=java.util.regex.Pattern.compile(".*?(/[0-9]*?/).*");
	    			  Matcher matcher=pattern.matcher(file);
	    			  matcher.find();
	    			  String folder=matcher.group(1);
	    			  ZipOutputStream fos=zips.get(Integer.parseInt(folder.substring(1,folder.length()-1)));
	    			  fos.putNextEntry(new ZipEntry(file));
					  StringBuffer filecontent=new StringBuffer();
					  int bytesRead=0;
	    			  while((bytesRead=zipIn.read(buffer))>0){
	    				  fos.write(buffer,0,bytesRead);
	    				  filecontent.append(new String(buffer));
	    			  }
	    			  if(file.matches(".*/pulseprogram")){
	    				  boolean foundtype=false;
	    				  for(String brukertype : brukertypes.keySet()){
	    					  if(filecontent.toString().contains(brukertype)){
	    						  types.put(Integer.parseInt(folder.substring(1,folder.length()-1)), DBSpectrumTypePeer.getByName(brukertypes.get(brukertype)));
	    						  assignmentMethod.append(brukertypes.get(brukertype)+", ");
	    						  if(brukertype.startsWith(";deptq"))
	    							  deptq=Integer.parseInt(folder.substring(1,folder.length()-1));
	    						  if(brukertype.equals(";apt"))
	    							  apt=Integer.parseInt(folder.substring(1,folder.length()-1));
	    						  foundtype=true;
	    					  }
	    				  }
	    				  if(!foundtype){
	    					  types.put(Integer.parseInt(folder.substring(1,folder.length()-1)), null);
	    				  }
	    			  }
	    			  fos.closeEntry();
	    			  if(file.matches(".*/[0-9]*/acqus")){
	    				  metadata.put(Integer.parseInt(folder.substring(1,folder.length()-1)), filecontent.toString());
	    			  }
	    		  }
	    	  }else if(file.matches(".*/[0-9]*") && !file.matches(".*/[0-9]*/.*/[0-9]")){
	    		  //we have a new experiment subfolder
	    		  ByteArrayOutputStream baos=new ByteArrayOutputStream();
	    		  ZipOutputStream zos=new ZipOutputStream(baos);
	    		  zips.put(Integer.parseInt(file.substring(file.lastIndexOf('/')+1)), zos);
	    		  baoses.put(Integer.parseInt(file.substring(file.lastIndexOf('/')+1)), baos);
	    	  }
	      }
	      zipIn.close();
	      struceluzos.close();
	      assignmentMethod = GeneralUtils.removeLastComma(assignmentMethod);
  	  if(!struceludir){
  		  messages.append("We cannot find a strucelu directory in your file. Did you use CMC-SE?");
	    	  return;
  	  }
  	  if(correlationFile==null){
  		  messages.append("There is no correlation file in your strucelu directory!");
	    	  return;
  	  }	      
  	  if(structure==null){
  		  messages.append("There is no structure file in your strucelu directory!");
	    	  return;
  	  }
	  GeneralUtils.readTopsinfile(correlationFile, structure, subDataC, subDataH, otherspectra, messages, cosypeaks, tocsypeaks, hmbcpeaks, hsqcpeaks, noesypeaks, ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
	  GeneralUtils.getExistingMolecule(subDataH.getMolWithHCount(), subDataH.getMolWithH(), subDataH.getIsDoubleBondSpecified(), runData.getServletConfig(), subDataH.getInputMolAsMdl(null,null,false), ssb, false);
      for(int type : zips.keySet()){
    	  zips.get(type).close();
    	  boolean done=false;
    	  for(String keysubdata : otherspectra.keySet()){
	    	  if(types.get(type)!=null && otherspectra.get(keysubdata).getChoosenSpectrumType()!=null && otherspectra.get(keysubdata).getChoosenSpectrumType().getSpectrumTypeId()==types.get(type).getSpectrumTypeId()){
	    		  otherspectra.get(keysubdata).setRawData(baoses.get(type).toByteArray(), "zip");
		    	  String solvent="";
		    	  String temp="";
		    	  String freq="";
		    	  StringTokenizer st = new StringTokenizer(metadata.get(type),"\n");
		    	  while(st.hasMoreTokens()){
		    		  String line = st.nextToken();
		    		  if(line.startsWith("##$SOLVENT=")){
		    			  solvent = line.substring(line.indexOf('<')+1, line.indexOf('>'));
		    		  }else if(line.startsWith("##$TE=")){
		    			  temp = line.substring(line.indexOf(' ')+1);
		    		  }else if(line.startsWith("##$BF1=")){
		    			  freq = line.substring(line.indexOf(' ')+1);
		    		  }		    		  
		    	  }
		    	  List<DBConditionType> conds = otherspectra.get(keysubdata).getConditions();
		    	  for(DBConditionType cond : conds){
		    		  if(cond.getConditionTypeId()==2){
		    			  cond.setCurrentValue(solvent);
		    		  }else if(cond.getConditionTypeId()==1){
		    			  cond.setCurrentValue(temp);
		    		  }else if(cond.getConditionTypeId()==3){
		    			  cond.setCurrentValue(freq);
		    		  }else if(cond.getConditionTypeId()==11){
		    			  cond.setCurrentValue(assignmentMethod.toString());
		    		  }
		    	  }
		    	  otherspectra.get(keysubdata).setConditions(conds);
		    	  otherspectra.get(keysubdata).setSmilesNonChiralProv(ssb.string1);
		    	  otherspectra.get(keysubdata).setSmilesChiralProv(ssb.string2);
		    	  otherspectra.get(keysubdata).setIsChiralProv(ssb.bool);
		    	  done=true;
	    	  }
    	  }
		  if(types.get(type)!=null && types.get(type).getSpectrumTypeId()==1){
			  type13c=type;
			  done=true;
		  } else if(types.get(type)!=null && types.get(type).getSpectrumTypeId()==2){
			  type1h=type;
			  done=true;
		  }
		  if(!done && types.get(type)!=null){
			  //We have a spectrum which is not in the correlation table, but there is a folder for it
			  String realtype=types.get(type).getName();
			  SubmittingData subData = new SubmittingData(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
			  subData.setMol(subDataC.getMolOriginal(), false, false, false);
			  subData.setChoosenSpectrumType(DBSpectrumTypePeer.getByName(realtype));
			  otherspectra.put(realtype, subData);
			  otherspectra.get(realtype).setRawData(baoses.get(type).toByteArray(), "zip");
			  String solvent="";
			  String temp="";
			  String freq="";
			  StringTokenizer st = new StringTokenizer(metadata.get(type),"\n");
			  while(st.hasMoreTokens()){
				  String line = st.nextToken();
				  if(line.startsWith("##$SOLVENT=")){
					  solvent = line.substring(line.indexOf('<')+1, line.indexOf('>'));
				  }else if(line.startsWith("##$TE=")){
					  temp = line.substring(line.indexOf(' ')+1);
				  }else if(line.startsWith("##$BF1=")){
					  freq = line.substring(line.indexOf(' ')+1);
				  }		    		  
			  }
			  List<DBConditionType> conds = otherspectra.get(realtype).getConditions();
			  for(DBConditionType cond : conds){
				  if(cond.getConditionTypeId()==2){
					  cond.setCurrentValue(solvent);
				  }else if(cond.getConditionTypeId()==1){
					  cond.setCurrentValue(temp);
				  }else if(cond.getConditionTypeId()==3){
					  cond.setCurrentValue(freq);
				  }else if(cond.getConditionTypeId()==11){
					  cond.setCurrentValue(assignmentMethod.toString());
				  }
			  }
			  otherspectra.get(realtype).setConditions(conds);
			  otherspectra.get(realtype).setSmilesNonChiralProv(ssb.string1);
			  otherspectra.get(realtype).setSmilesChiralProv(ssb.string2);
			  otherspectra.get(realtype).setIsChiralProv(ssb.bool);
		  }
      }
	  if(subDataC.getSignalstable().getPeakChildren().size()>0){
		  subDataC.setLevel(6);
    	  String solvent="";
    	  String temp="";
    	  String freq="";
		  if(type13c>0){
			  subDataC.setRawData(baoses.get(type13c).toByteArray(), "zip");
	    	  StringTokenizer st = new StringTokenizer(metadata.get(type13c),"\n");
	    	  while(st.hasMoreTokens()){
	    		  String line = st.nextToken();
	    		  if(line.startsWith("##$SOLVENT=")){
	    			  solvent = line.substring(line.indexOf('<')+1, line.indexOf('>'));
	    		  }else if(line.startsWith("##$TE=")){
	    			  temp = line.substring(line.indexOf(' ')+1);
	    		  }else if(line.startsWith("##$BF1=")){
	    			  freq = line.substring(line.indexOf(' ')+1);
	    		  }		    		  
	    	  }
		  }
    	  List<DBConditionType> conds = subDataC.getConditions();
    	  for(DBConditionType cond : conds){
    		  if(cond.getConditionTypeId()==2){
    			  cond.setCurrentValue(solvent);
    		  }else if(cond.getConditionTypeId()==1){
    			  cond.setCurrentValue(temp);
    		  }else if(cond.getConditionTypeId()==3){
    			  cond.setCurrentValue(freq);
    		  }else if(cond.getConditionTypeId()==11){
    			  cond.setCurrentValue(assignmentMethod.toString());
    		  }
    	  }
    	  subDataC.setConditions(conds);
    	  subDataC.setSmilesNonChiralProv(ssb.string1);
    	  subDataC.setSmilesChiralProv(ssb.string2);
    	  subDataC.setIsChiralProv(ssb.bool);
          //we get multiplicities from h count
          IAtomContainer mol = subDataC.getMolWithH();
          for (int i = 0; i < mol.getAtomCount(); i++) {
              if (mol.getAtom(i).getSymbol().equals("C")) {
                 try{
                    AtomUtils.setChildElement(subDataC.getCMLMolecule().getAtoms().get(i), SubmittingData.MULTIPLICITY, GeneralUtils.getMultiplicity(mol.getAtom(i), mol, false));
                 }catch(Exception ex){
                    //we get npes here for unclear reasons. a bit dirty like this, but no time to do it better
                 }
              }
          }
          if(deptq>0)
    		  otherspectra.get(types.get(deptq).getName()).setSignalstable((CMLPeakList)subDataC.getSignalstable().copy());
          if(apt>0)
    		  otherspectra.get(types.get(apt).getName()).setSignalstable((CMLPeakList)subDataC.getSignalstable().copy());
	  }
      if(otherspectra.get("H,C-HMQC")!=null && otherspectra.get("HSQC")!=null)
		  otherspectra.get("H,C-HMQC").setSignalstable(otherspectra.get("HSQC").getSignalstable());
	  if(subDataH.getSignalstable().getPeakChildren().size()>0){
		  subDataH.setLevel(6);
    	  String solvent="";
    	  String temp="";
    	  String freq="";
		  if(type1h>0){
			  subDataH.setRawData(baoses.get(type1h).toByteArray(), "zip");
	    	  StringTokenizer st = new StringTokenizer(metadata.get(type1h),"\n");
	    	  while(st.hasMoreTokens()){
	    		  String line = st.nextToken();
	    		  if(line.startsWith("##$SOLVENT=")){
	    			  solvent = line.substring(line.indexOf('<')+1, line.indexOf('>'));
	    		  }else if(line.startsWith("##$TE=")){
	    			  temp = line.substring(line.indexOf(' ')+1);
	    		  }else if(line.startsWith("##$BF1=")){
	    			  freq = line.substring(line.indexOf(' ')+1);
	    		  }		    		  
	    	  }
		  }
    	  List<DBConditionType> conds = subDataH.getConditions();
    	  for(DBConditionType cond : conds){
    		  if(cond.getConditionTypeId()==2){
    			  cond.setCurrentValue(solvent);
    		  }else if(cond.getConditionTypeId()==1){
    			  cond.setCurrentValue(temp);
    		  }else if(cond.getConditionTypeId()==3){
    			  cond.setCurrentValue(freq);
    		  }else if(cond.getConditionTypeId()==11){
    			  cond.setCurrentValue(assignmentMethod.toString());
    		  }
    	  }
    	  subDataH.setConditions(conds);
    	  subDataH.setSmilesNonChiralProv(ssb.string1);
    	  subDataH.setSmilesChiralProv(ssb.string2);
    	  subDataH.setIsChiralProv(ssb.bool);
    	  subDataH.setCmcseFile(strucelubaos.toByteArray());
      }
	}

	public static void analyzeNmredata(NmreData data, RunData runData, Context context, SubmittingData subDataC,
			SubmittingData subDataH, Map<String, SubmittingData> otherspectra) throws TorqueException, NmrshiftdbException, CDKException, IOException, ClassNotFoundException, DataSetException {
        for(Spectrum spectrum : data.getSpectra()) {
      	  StringBuffer unusedString = new StringBuffer();
      	  if(spectrum instanceof NMRSpectrum && (((NMRSpectrum)spectrum).getNucleus().equals("13C") || ((NMRSpectrum)spectrum).getNucleus().equals("C")) || spectrum instanceof NMRSpectrum && (((NMRSpectrum)spectrum).getNucleus().equals("1H")|| ((NMRSpectrum)spectrum).getNucleus().equals("H"))) {
      		  SubmittingData subData;
      		  if(spectrum instanceof NMRSpectrum && (((NMRSpectrum)spectrum).getNucleus().equals("1H")|| ((NMRSpectrum)spectrum).getNucleus().equals("H")))
      			  subData=subDataH;
      		  else
      			  subData=subDataC;
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
      	  }else if(spectrum instanceof NMR2DSpectrum){
      		  String spectrumtype=null;
      		  for(Object note :  ((NMR2DSpectrum)spectrum).getNotes()){
      			  if(note instanceof Note && ((Note)note).getDescriptor().getKey().equals("CorType")){
      				  spectrumtype=(String)((Note)note).getValue();
      				  break;
      			  }
      		  }
      		  if(spectrumtype!=null){
      			  DBSpectrumType spectype=null;
          		  if(spectrumtype.equals("HSQC")){
          			  spectype=DBSpectrumTypePeer.getByName("H,C-HSQC");
          		  }
          		  if(spectrumtype.equals("HMBC")){
          			  spectype=DBSpectrumTypePeer.getByName("H,C-HMBC");
          		  }
          		  if(spectrumtype.equals("TOCSY")){
          			  spectype=DBSpectrumTypePeer.getByName("H,H-TOCSY");
          		  }
          		  if(spectrumtype.equals("NOESY")){
          			  spectype=DBSpectrumTypePeer.getByName("H,H-NOESY");
          		  }
          		  if(spectrumtype.equals("COSY")){
          			  spectype=DBSpectrumTypePeer.getByName("H,H-COSY");
          		  }
      			  SubmittingData subData=new SubmittingData(spectype, ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
          		  subData.setMol(data.getMolecule(), false, false, false);
          		  CMLPeakList peaks=new CMLPeakList();
          		  if(spectrumtype.equals("HSQC") || spectrumtype.equals("HMBC")){
          				for (Peak2D peak2d : ((NMR2DSpectrum)spectrum).getPeakTable()) {
          					CMLPeak cmlpeak=new CMLPeak();
          					cmlpeak.setXValue(peak2d.getPosition()[0]);
          					cmlpeak.setYValue(peak2d.getPosition()[1]);
          					peaks.addPeak(cmlpeak);
          				}
          		  }
          		  if(spectrumtype.equals("HSQC") || spectrumtype.equals("HMBC") || spectrumtype.equals("TOCSY") || spectrumtype.equals("NOESY") || spectrumtype.equals("COSY")){
          				for (Peak2D peak2d : ((NMR2DSpectrum)spectrum).getPeakTable()) {
          					CMLPeak cmlpeak=new CMLPeak();
          					cmlpeak.setXValue(peak2d.getPosition()[1]);
          					cmlpeak.setYValue(peak2d.getPosition()[0]);
          					peaks.addPeak(cmlpeak);
          				}
          		  }
          		  subData.setSignalstable(peaks);
          		otherspectra.put(spectype.getName(), subData);    		  
          	}
      	  }
        }		
	}

	public static double[] getPredictionLimits(String name) throws TorqueException, NmrshiftdbException {
		DBSpectrumType type=DBSpectrumTypePeer.getByName(name);
		return new double[] {type.getRedLimit(), type.getOrangeLimit()};
	}
	
	public static long ipToLong(String ipstring) throws UnknownHostException {
	  InetAddress ip = InetAddress.getByName(ipstring);
      byte[] octets = ip.getAddress();
      long result = 0;
      for (byte octet: octets) {
         result <<= 8;
         result |= octet & 0xff;
      }
      return result;
	}
}
