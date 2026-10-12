/*
 * Copyright 2010-2015 Stefan Kuhn
 * 
 * This file is part of nmrshiftdb2.
 *
 * nmrshiftdb2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * nmrshiftdb2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with nmrshiftdb2.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscience.nmrshiftdb;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.vecmath.Point2d;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.TurbineConfig;
import org.apache.turbine.util.mail.EmailAttachment;
import org.apache.turbine.util.mail.MailMessage;
import org.apache.turbine.util.mail.MultiPartEmail;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jcamp.parser.JCAMPException;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.assignments.AtomReference;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.graph.invariant.Canon;
import org.openscience.cdk.graph.invariant.InChINumbersTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.Mol2Reader;
import org.openscience.cdk.io.Mol2Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.modules.actions.portlets.result.DetailsAction;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBAtomPeer;
import org.openscience.nmrshiftdb.om.DBBond;
import org.openscience.nmrshiftdb.om.DBBondDBAtom;
import org.openscience.nmrshiftdb.om.DBBondPeer;
import org.openscience.nmrshiftdb.om.DBCanonicalName;
import org.openscience.nmrshiftdb.om.DBCanonicalNamePeer;
import org.openscience.nmrshiftdb.om.DBChemicalName;
import org.openscience.nmrshiftdb.om.DBChemicalNamePeer;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.om.DBCoordinateSet2d;
import org.openscience.nmrshiftdb.om.DBCoordinateSet3d;
import org.openscience.nmrshiftdb.om.DBCoupling;
import org.openscience.nmrshiftdb.om.DBDatasetDoi;
import org.openscience.nmrshiftdb.om.DBDatasetDoiDBKeyword;
import org.openscience.nmrshiftdb.om.DBDatasetDoiDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBDatasetDoiPeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBSample;
import org.openscience.nmrshiftdb.om.DBExperimentDBSamplePeer;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSignalDBAtom;
import org.openscience.nmrshiftdb.om.DBSignalDBAtomPeer;
import org.openscience.nmrshiftdb.om.DBSignalPeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBCondition;
import org.openscience.nmrshiftdb.om.DBSpectrumDBConditionPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumFile;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumMark;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.ExtendedHOSECodeGenerator;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.JsonExport;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.pdf.PdfWriter;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import VisualNumerics.math.Statistics;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.NmreData.NmredataVersion;
import de.unikoeln.chemie.nmr.data.NmreDataRecord;
import de.unikoeln.chemie.nmr.data.Peak2D;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import de.unikoeln.chemie.nmr.io.NmredataRecordWriter;
import de.unikoeln.chemie.nmr.io.NmredataWriter;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class AdminTasks {
	
   final static String URL="https://www.nmrshiftdb.org/";

    /**
      *The main program for the AdminPanel class.
      *
      * @param  args  The directory whre the log files are and the src/java/ directory of nmrshiftdb distribution
      * @exception  Exception  Description of Exception
      */
	public static void main(String[] args) throws Exception{
	    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
	    Properties nmrprops = new Properties();
	    nmrprops.load(new FileInputStream(nmrpropsFile));
	    GeneralUtils.nmrprops=nmrprops;
	    if (args.length != 3) {
	      System.out.println("Please give the directory to read from, the src/java directory of nmrshiftdb and the server name as parameters!");
	      System.exit(0);
	    }
	    for (; ; ) {
	      System.err.println("Choose your action:");
	      System.err.println("");
	      System.err.println("[1]Update formula and multiplicities");
	      System.err.println("[2]Update specfiles");
	      System.err.println("[3]Delete spectrum");
	      System.err.println("[4]Delete molecule");
	      System.err.println("[5]Read uni mainz carbon");
	      System.err.println("[6]Remove duplicates");
	      System.err.println("[7]Make chemspider files");
	      System.err.println("[8]Make pubchem files");
	      System.err.println("[9]Make dump files");
	      System.err.println("[10]Read pubchem file");
	      System.err.println("[11]Make HOSE codes");
          System.err.println("[12]Analyze logs");
          System.err.println("[13]Do a crossvalidation");
          System.err.println("[14]Write thomsonreuters file");
          System.err.println("[15]Do database checks");
          System.err.println("[16]Fetch names from pubchem");
          System.err.println("[17]Rebuild aromaticity flag");
          System.err.println("[18]Read CORINA file");
          System.err.println("[19]Make c descriptors");
          System.err.println("[20]Remove double bond configs in small rings");
          System.err.println("[21]Write arff");
          System.err.println("[22]Write chebi mapping");
          System.err.println("[23]Export file for paper");
          System.err.println("[24]Read bochum files");
          System.err.println("[25]Write unichem file");
          System.err.println("[26]Read wsu files");
          System.err.println("[27]Write Json files");
          System.err.println("[28]Read oms");
          System.err.println("[29]Send doi emails");
          System.err.println("[30]Write HOSE code dump");
          System.err.println("[31]Generate coordinates");
          System.err.println("[32]Read correlations");
          System.err.println("[33]Publish dataset");
          System.err.println("[34]Write couplings");
          System.err.println("[35]Predict couplings");
          System.err.println("[36]Read bmrb");
		  System.err.println("[37]Check dois");
          System.err.println("[q]uit");
	      System.err.println("");
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	      String action = "19";//null;
	      try {
	        action = br.readLine();
	      } catch (IOException ioe) {
	        System.out.println("IO error trying to read menu choice!");
	        System.exit(1);
	      }
	      if (action.equals("q")) {
	          System.exit(0);
	      } else if (action.equals("1")) {
	        try {
	          if (warn("This will build formulas/multiplicites", new BufferedReader(new InputStreamReader(System.in)))) {
	            executeFormulaAndMultis(args);
	          }
	        } catch (Exception ex) {
	          ex.printStackTrace();
	        }
	      } else if (action.equals("2")) {
		        try {
		          if (warn("This will build specfiles", new BufferedReader(new InputStreamReader(System.in)))) {
		            executeSpecfiles(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("3")) {
		        try {
		          if (warn("This will delete a spectrum", new BufferedReader(new InputStreamReader(System.in)))) {
		            deleteSpectrum(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("4")) {
		        try {
		          if (warn("This will delete a molecule", new BufferedReader(new InputStreamReader(System.in)))) {
		            deleteMolecule(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("5")) {
		        try {
		          if (warn("This will read c files from mainz", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  readOmGeitner(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("6")) {
		        try {
		          if (warn("This will remove duplcate molecules", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeCheckDoubleMolecules(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("7")) {
		        try {
		          if (warn("This will write chemspider files", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeMakeChemspider(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("8")) {
		        try {
		          if (warn("This will write a pubchemr file", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeMakePubchem(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("9")) {
		        try {
		          if (warn("This will create nmrshiftdb2 dump files", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeMakeDumps(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("10")) {
		        try {
		          if (warn("This will add stuff from pubchem file", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeReadAlk3(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
	      } else if (action.equals("11")) {
		        try {
		          if (warn("This will rebuild hose codes", new BufferedReader(new InputStreamReader(System.in)))) {
		        	  executeMakeHose(args);
		          }
		        } catch (Exception ex) {
		          ex.printStackTrace();
		        }
          } else if (action.equals("12")) {
                    try {
                      if (warn("This will analyze log files", new BufferedReader(new InputStreamReader(System.in)))) {
                              executeAnalyzeSql(args);
                      }
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
          } else if (action.equals("13")) {
              try {
                if (warn("This will do a crossvalidation", new BufferedReader(new InputStreamReader(System.in)))) {
                	crossvalidateHose(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("14")) {
              try {
                if (warn("This will write a thomsonreuters file", new BufferedReader(new InputStreamReader(System.in)))) {
                	writeThomsonreuters(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("15")) {
              try {
                if (warn("This will do database checks", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeCheckAssignments(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("16")) {
              try {
                if (warn("This will fetch names from pubchem", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeInsertNamesFromPubchem(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("17")) {
              try {
                if (warn("This will rebuild the aromaticty flags", new BufferedReader(new InputStreamReader(System.in)))) {
                  executeMakeAromaticity(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("18")) {
              try {
                if (warn("This will read a corina file", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeReadCorina(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("19")) {
              //try {
                //if (warn("This will make C descriptors", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeMakeDescriptorsAll(args);
                //}
              /*} catch (Exception ex) {
                ex.printStackTrace();
              }*/
          } else if (action.equals("20")) {
              try {
                if (warn("This will remove double bond configs in small rings", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeRemoveDoubleBondConfigsInSmallRings(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("21")) {
              try {
                if (warn("This will write arff file", new BufferedReader(new InputStreamReader(System.in)))) {
                	makeArff(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("22")) {
              try {
                if (warn("This will write a chebi mapping", new BufferedReader(new InputStreamReader(System.in)))) {
                	makeChebiMapping(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("23")) {
              try {
                if (warn("This will write an export for the paper", new BufferedReader(new InputStreamReader(System.in)))) {
                    executeMakeRobienexport(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("24")) {
              try {
                if (warn("This will read bochum files", new BufferedReader(new InputStreamReader(System.in)))) {
                    executeReadBochum(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("25")) {
              try {
                if (warn("This will write unichem file", new BufferedReader(new InputStreamReader(System.in)))) {
                    executeWriteUnichem(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("26")) {
              try {
                if (warn("This will read wsu files", new BufferedReader(new InputStreamReader(System.in)))) {
                    executeReadWsu(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("27")) {
              try {
                if (warn("This will write json files", new BufferedReader(new InputStreamReader(System.in)))) {
                    writeJson(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("28")) {
              try {
                if (warn("This will read om files", new BufferedReader(new InputStreamReader(System.in)))) {
                     readOm(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("29")) {
              try {
                if (warn("This will send doi emails", new BufferedReader(new InputStreamReader(System.in)))) {
                     writeDois(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("30")) {
              try {
                if (warn("This will write hose code dump", new BufferedReader(new InputStreamReader(System.in)))) {
                     doHoseDump(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("31")) {
              try {
                if (warn("This will generate coordinates", new BufferedReader(new InputStreamReader(System.in)))) {
                     doCoordinates(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("32")) {
              try {
                if (warn("This will read correlations", new BufferedReader(new InputStreamReader(System.in)))) {
                	readCorrs(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("33")) {
              try {
                if (warn("This will publish a dataset", new BufferedReader(new InputStreamReader(System.in)))) {
                	publishDataset(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("34")) {
              try {
                if (warn("This will write couplings file", new BufferedReader(new InputStreamReader(System.in)))) {
                	executeMakeDumpcouplings(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("35")) {
              try {
                if (warn("This will preidct couplings", new BufferedReader(new InputStreamReader(System.in)))) {
                	predictCouplings(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("36")) {
              try {
                if (warn("This will read bmrb data", new BufferedReader(new InputStreamReader(System.in)))) {
                	readBmrb3(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else if (action.equals("37")) {
              try {
                if (warn("This will check dois", new BufferedReader(new InputStreamReader(System.in)))) {
                	doCheckDois(args);
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
          } else {
	        System.out.println("Unknown action. Choose from menu");
	      }
	  }
	  }


	private static void deleteMolecule(String[] args) throws Exception {
		  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		  System.out.println("Enter id");
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	      String id = br.readLine();
		  System.out.println("Enter id");
		  String id2 = br.readLine();
		  if(!id.equals(id2))
			  return;				  
          Connection dbconn = Torque.getConnection();
          Statement stmt = dbconn.createStatement();
          stmt.executeUpdate("BEGIN");
          DBMolecule molecule=DBMoleculePeer.retrieveByPK(Integer.parseInt(id));
          //Do we have any spectra to delete? If yes
          molecule.delete(dbconn);
          stmt.executeUpdate("COMMIT");
          stmt.close();
          dbconn.close();
	}


	/**
	   *  Prints a warning and asks if to continue
	   *
	   * @param  text  The warning text
	   * @param  br    The buffered reader from System.in
	   * @return       True=continue, false=break
	   */
	  private static boolean warn(String text, BufferedReader br) {
	    System.out.println(text + ". Do you want to continue (y/n) ?");
	    String action = null;
	    try {
	      action = br.readLine();
	    } catch (IOException ioe) {
	      System.out.println("IO error trying to read your name!");
	      System.exit(1);
	    }
	    if (action.equals("y")) {
	      return (true);
	    } else {
	      return (false);
	    }
	  }

	  private static void executeFormulaAndMultis(String[] args) throws Exception{
		  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
		  config.initialize();
		  Connection dbconn = Torque.getConnection();
		  Statement stmt = dbconn.createStatement();
		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE where MOLECULE_ID=20182362");
		    while (rs.next()) {
		    try {
		    	DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(30132185);
		    	NmreData data = new NmreData();
		    	data.setVersion(NmreData.NmredataVersion.ONE);
		    	data.setMolecule(spectrum.getDBMolecule().getAsCDKMolecule(1));
		    	spectrum.getNmreData(false, true, spectrum, 400, new double[1][1], "jlkj", "https://nmrshiftdb.nmr.uni-koeln.de/", data);
		    	ByteArrayOutputStream baos=new ByteArrayOutputStream();
		    	NmredataWriter wrtier=new NmredataWriter(baos);
		    	wrtier.write(data,NmreData.NmredataVersion.ONE);
		    	wrtier.close();
		    	System.out.println(baos.toString());
		    	DBMolecule dbmol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
		    	System.err.println(dbmol.getMoleculeId());
		    	IAtomContainer mol=dbmol.getAsCDKMolecule(1);
		    	/*AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		    	new org.openscience.cdk.tools.AtomTypeAwareSaturationChecker().decideBondOrder(mol);
		    	List<DBBond> bonds = dbmol.getBonds();
		    	for(int i=0;i<bonds.size();i++){
		    		if(mol.getBond(i).getOrder().ordinal()!=bonds.get(i).getDegree()) {
		    			bonds.get(i).setDegree(mol.getBond(i).getOrder().ordinal());
		    			bonds.get(i).save();
		    		}
		    	}*/
		    	
		    	
		    	
		    	List bonds = dbmol.getBonds();
		    	boolean[] dbc = new boolean[bonds.size()];
		    	for(int i=0;i<bonds.size();i++){
		    		dbc[i]=((DBBond)bonds.get(i)).getIsConfigurationSpecified().equals(NmrshiftdbConstants.TRUE);
		    	}
		        /*dbmol.setFormula(MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(mol)));
		        mol = dbmol.getAsCDKMoleculeWithoutH();
		        int[] mults=new int[4];
		        for(int k=0;k<mol.getAtomCount();k++){
		          if( mol.getAtom(k).getSymbol().equals("C")){
		    		if(mol.getAtom(k).getHydrogenCount()==null)
		    			mults[0]++;
		    	   	else if(mol.getAtom(k).getHydrogenCount()<4)
		    		    mults[mol.getAtom(k).getHydrogenCount()]++;
		          }
		        }
		        dbmol.setMultiplicity0(mults[0]);
		        dbmol.setMultiplicity1(mults[1]);
		        dbmol.setMultiplicity2(mults[2]);
		        dbmol.setMultiplicity3(mults[3]);*/
		    	StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		    	GeneralUtils.getExistingMolecule(dbmol.getAsCDKMoleculeWithoutH(), mol, dbc, null, dbmol.getStructureFile(1,false), ssb);
System.err.println(ssb.string1+" "+ssb.bool+" "+ssb.string2);
		    	dbmol.setSmilesString(ssb.string1.getBytes());
		    	if(ssb.bool)
		    		dbmol.setSmilesStringChiral(ssb.string2.getBytes());
		    	else
		    		dbmol.setSmilesStringChiral(null);
                        dbmol.save();
                String inchi=GeneralUtils.generateInchi(dbmol.getAsCDKMoleculeWithoutH(),false);
                System.out.println(inchi);
                String sql ="update CANONICAL_NAME set NAME='"+inchi+"', NAME_SOUNDEX ='"+Soundex.soundex(inchi, "9", new StringAndInt())+"' where MOLECULE_ID ="+dbmol.getMoleculeId()+" and CANONICAL_NAME_TYPE_ID =4";
                System.out.println(sql);
                DBCanonicalNamePeer.executeStatement(sql);
		    	/*List bonds = dbmol.getBonds();
		    	boolean[] dbc = new boolean[bonds.size()];
		    	for(int i=0;i<bonds.size();i++){
		    		dbc[i]=((DBBond)bonds.get(i)).getIsConfigurationSpecified().equals(NmrshiftdbConstants.TRUE);
		    	}
		    	StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		    	GeneralUtils.getExistingMolecule(dbmol.getAsCDKMoleculeWithoutH(), mol, dbc, null, dbmol.getStructureFile(1,false), ssb);
		    	dbmol.setSmilesString(ssb.string1.getBytes());
		    	if(ssb.bool)
		    		dbmol.setSmilesStringChiral(ssb.string2.getBytes());
		    	else
		    		dbmol.setSmilesStringChiral(null);
		        dbmol.save();*/
/*String mdl = dbmol.getStructureFile(1,false);
		    	String inchi = GeneralUtils.generateInchi(mol,false);
		    	String sql ="update CANONICAL_NAME set NAME='"+inchi+"', NAME_SOUNDEX ='"+Soundex.soundex(inchi, "9", new StringAndInt())+"' where MOLECULE_ID ="+dbmol.getMoleculeId()+" and CANONICAL_NAME_TYPE_ID =4";
System.out.println(sql);
		    	DBCanonicalNamePeer.executeStatement(sql);
		    	String inchikey = GeneralUtils.generateInchiKey(mol);
		    	sql ="update CANONICAL_NAME set NAME='"+inchikey+"', NAME_SOUNDEX ='"+Soundex.soundex(inchikey, "9", new StringAndInt())+"' where MOLECULE_ID ="+dbmol.getMoleculeId()+" and CANONICAL_NAME_TYPE_ID =5";
		    	DBCanonicalNamePeer.executeStatement(sql);
                inchi = GeneralUtils.generateInchi(mol,true);
                sql ="insert into CANONICAL_NAME values ("+dbmol.getMoleculeId()+",6,'"+inchi+"','"+Soundex.soundex(inchi, "9", new StringAndInt())+"')";//update CANONICAL_NAME set NAME='"+inchi+"', NAME_SOUNDEX ='"+Soundex.soundex(inchi, "9", new StringAndInt())+"' where MOLECULE_ID ="+dbmol.getMoleculeId()+" and CANONICAL_NAME_TYPE_ID =6";
System.out.println(sql);
                DBCanonicalNamePeer.executeStatement(sql);*/
			  //dbmol.setFingerprint();
			  //dbmol.save();
		    }catch(Exception ex){
		    	ex.printStackTrace();
		    }
		    }
			
		}

	  private static void executeSpecfiles(String[] args) throws Exception{
		  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
		  config.initialize();
		  Connection dbconn = Torque.getConnection();
		  Statement stmt = dbconn.createStatement();
		  ResultSet rs = stmt.executeQuery("select SPECTRUM_ID from SPECTRUM order by SPECTRUM_ID;");
		    while (rs.next()) {
		    try {
		    	DBSpectrum dbspec = DBSpectrumPeer.retrieveByPK(new NumberKey(rs.getInt(1)));
		    	SubmittingData subdata = dbspec.getMeAsSubmittingData(true,args[1]+"/conf/jetspeed/WEB-INF/conf/normalizer.xml",false, "");
		    	File file=new File(dbspec.getSpectrumId()+".xml");
		    	FileOutputStream fos=new FileOutputStream(file);
		    	fos.write(subdata.data.toXML().getBytes());
		    	/*System.err.println(dbspec.getSpectrumId());
		        dbspec.setSpecfile(getSpecfile2(dbspec));
		        dbspec.setSimpleSpecfile(getSimpleSpecfile2(dbspec));
		        dbspec.save(dbconn);*/
		    }catch(Exception ex){
		    	ex.printStackTrace();
		    }
		    }			
		}
	  
	  
	  public static String getSimpleSpecfile2(DBSpectrum spectrum)
	  {
		    StringBuffer returnValue = new StringBuffer();
		    try{
		      for (int i = 0; i < spectrum.getDBSignals().size(); i++) {
		        DBSignal it = (DBSignal) spectrum.getDBSignals().get(i);
		        returnValue.append(it.getFirstShift().getValue() + ";" + it.getIntensity() + "" + it.getMultiplicity() + "|");
		      }
		    }catch(Exception ex){
		      GeneralUtils.logError(ex,"in getSpecfile()",null,true);
		    }
		    if (returnValue.equals("")) {
		      return ("|");
		    } else {
		      return (returnValue.toString());
		    }
	  }
	  
	  public static String getSpecfile2(DBSpectrum spectrum){
	    StringBuffer returnValue = new StringBuffer();
	    try{
	      for (int i = 0; i < spectrum.getDBSignals().size(); i++) {
	        DBSignal it = (DBSignal) spectrum.getDBSignals().get(i);
	        if(spectrum.getDBSpectrumType().getDimensionality()>1) {
		        for (int k = 0; k < it.getDBAtomsByAxis().get(0).size(); k++) {
		          DBAtom mainAtom = (DBAtom) it.getAtoms().get(k);
		          if (spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && mainAtom.getIsVisible().equals(NmrshiftdbConstants.FALSE)) {
		            mainAtom = (DBAtom) mainAtom.getConnectedAtoms().get(0);
		          }
		          DBAtom mainAtom2 = it.getDBAtomsByAxis().get(1).get(k);
		          if (spectrum.getDBSpectrumType().getDBIsotope(2).getElementSymbol().equals("H") && mainAtom.getIsVisible().equals(NmrshiftdbConstants.FALSE)) {
		            mainAtom2 = (DBAtom) mainAtom2.getConnectedAtoms().get(0);
		          }
		          returnValue.append(it.getDBShift(1).getValue()+"-"+it.getDBShift(2).getValue() + ";" + it.getIntensity() + ";" + mainAtom.getMdlNumberVisible()+"-"+mainAtom2.getMdlNumberVisible() + "|");
		        }
	        }else {
		        for (int k = 0; k < it.getAtoms().size(); k++) {
		          DBAtom mainAtom = (DBAtom) it.getAtoms().get(k);
		          if (spectrum.getDBSpectrumType().getName().equals("1H") && mainAtom.getIsVisible().equals(NmrshiftdbConstants.FALSE)) {
		            mainAtom = (DBAtom) mainAtom.getConnectedAtoms().get(0);
		          }
		          returnValue.append(it.getFirstShift().getValue() + ";" + it.getIntensity() + "" + it.getMultiplicity() + ";" + mainAtom.getMdlNumberVisible() + "|");
		        }
	        }
	      }
	    }catch(Exception ex){
	      GeneralUtils.logError(ex,"in getSpecfile()",null,true);
	    }
	    if (returnValue.equals("")) {
	      return ("|");
	    } else {
	      return (returnValue.toString());
	    }
	  }


		private static void deleteSpectrum(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  System.out.println("Enter id");
		      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		      String id = br.readLine();
			  System.out.println("Enter id");
			  String id2 = br.readLine();
			  if(!id.equals(id2))
				  return;				  
	          Connection dbconn = Torque.getConnection();
	          Statement stmt = dbconn.createStatement();
	          stmt.executeUpdate("BEGIN");
	          DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(Integer.parseInt(id));
	          //Do we have any spectra to delete? If yes
	          spectrum.delete(dbconn,false);
	          stmt.executeUpdate("COMMIT");
	          stmt.close();
	          dbconn.close();
		}
		
		private static void executeSample(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  String query="select distinct SAMPLE.SAMPLE_ID, USERS_ID, MACHINE.NAME, PROCESS from (SAMPLE left join RAW_FILE using (SAMPLE_ID)) join MACHINE on SAMPLE.MACHINE=MACHINE.MACHINE_ID where URL is null and FINISHED=\"false\" and (PROCESS=\""+NmrshiftdbConstants.PROCESS_OPERATOR+"\") and SAMPLE.DATE>=\"2023-01-01\" order by MACHINE.NAME";
            Statement stmt = dbconn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String lastMachineUsed="";
            //this is the sftp version
                //this is the locally mounted version
                //this assumes the file system to be like: rawfiledirectoryrobot/instrumentname/ordername
                File rawfiledir=new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryrobot"));
            	while(rs.next()){
                	String usersid=rs.getString(2);
                	int sampleid=rs.getInt(1);
                	String process=rs.getString(4);
                	//we find out how many different experiments should be measured
                	List<Record> result = DBExperimentDBSamplePeer.executeQuery("select distinct EXPERIMENT_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where SUB_EXPERIMENT_1_ID is null and  SAMPLE_ID ="+sampleid+" union select distinct SUB_EXPERIMENT_1_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_1_ID is not null union select distinct SUB_EXPERIMENT_2_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_2_ID is not null union select distinct SUB_EXPERIMENT_3_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_3_ID is not null");
                	int spectrainorder=result.size();
                	File datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid);
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.toLowerCase());
                	}
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.toUpperCase());
                	}
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.substring(0,1).toUpperCase()+usersid.substring(1).toLowerCase());
                	}
            		if(datadir.exists() && (System.currentTimeMillis()-datadir.lastModified())>300000 && datadir.list().length>=spectrainorder){
            			if(process.equals(NmrshiftdbConstants.PROCESS_OPERATOR)) {
	    	                addfiles(datadir, null, null, 0, usersid);            				
            			}else {
	    	                File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory")+"/"+usersid+".zip");
	    	                StringBuffer attachedfiles=new StringBuffer();
	    	                if(!zipfile.exists()){
		    	                FileOutputStream baos=new FileOutputStream(zipfile);
		    	                ZipOutputStream zipout=new ZipOutputStream(baos);
		    	                addfiles(datadir, zipout, attachedfiles, 0, usersid);
		    	                zipout.close();
	    	                }
	    	                if(zipfile.length()<50*1024*1024 && zipfile.length()>100){//>20 mb oder ganz klein stimmt was nicht
	    	                    FileInputStream fis = new FileInputStream(zipfile);
	    	                    String md5String = DigestUtils.md5Hex(fis);
	    	                    fis.close();
	    						DBRawFile rawfile=new DBRawFile();
	    		        		rawfile.setSampleId(sampleid);
	    					  	rawfile.setUrl(GeneralUtils.getNmrshiftdbProperty("urlcompressedfiles")+"/"+usersid+".zip");
	    					  	rawfile.setContainedFiles(attachedfiles.toString());
	    					  	rawfile.setAssigned("false");
	    					  	rawfile.setMd5(md5String);
	    					  	rawfile.setDate(new Date());
	    					  	rawfile.save();
	    					  	DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(sampleid));
	    					  	sample.setFinished("true");
	    					  	sample.save();
	    					}
            			}
            		}
            	//this does a search on all levels - slow, but not depending an file system architecture
                /*ArrayList<String> tosearch=new ArrayList<String>();
                HashMap<String,Integer> idmap=new HashMap<String,Integer>();
                while(rs.next()){
                	int sampleid=rs.getInt(1);
                	String usersid=rs.getString(2).toLowerCase();
                	idmap.put(usersid,sampleid);
                	tosearch.add(usersid);
                }
                HashMap<String,ZipOutputStream> zipoutmap=new HashMap<String,ZipOutputStream>();
                HashMap<String,StringBuffer> filenamemap=new HashMap<String,StringBuffer>();
            	searchfiles(rawfiledir,tosearch,zipoutmap,filenamemap, new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)), servcon,0);
            	rs.beforeFirst();
                while(rs.next()){
                	int sampleid=rs.getInt(1);
                	String usersid=rs.getString(2).toLowerCase();
    	        	if(filenamemap.get(usersid)!=null){
    	        		ZipOutputStream zipout=zipoutmap.get(usersid);
    	        		zipout.close();
    	        		DBRawFile rawfile=new DBRawFile();
    	        		rawfile.setSampleId(sampleid);
    				  	rawfile.setUrl(GeneralUtils.getNmrshiftdbProperty("urlcompressedfiles",servcon)+"/"+usersid+".zip");
    				  	rawfile.setContainedFiles(filenamemap.get(usersid).toString());
    				  	rawfile.setAssigned("false");
    				  	rawfile.setDate(new Date());
    				  	rawfile.save();
    				  	DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(sampleid));
    				  	sample.setFinished("true");
    				  	sample.save();
    	        	}
                }*/
            }
    }
    
    private static int countOccurences(String string, char character) {
		int count=0;
		for(int i=0;i<string.length();i++){
			if(string.charAt(i)==character)
				count++;
		}
		return count;
	}

    private static void searchfiles(File tolookin, ArrayList<String> usersid, HashMap<String,ZipOutputStream> zipoutputstream, HashMap<String,StringBuffer> attachedfiles, File dirtoignore, ServletConfig servcon, int depth, int currentexpid) throws IOException, TorqueException, Exception{
    	File[] filesindir=tolookin.listFiles();
    	//System.err.println("search "+tolookin+"  "+usersid+"  "+filesindir[0].getName());
    	if(filesindir!=null){
	    	for(int i=0;i<filesindir.length;i++){
	    		if(filesindir[i].isDirectory() && arrayindex(usersid,filesindir[i].getName()).equals("") && !filesindir[i].getName().equals(".") && !filesindir[i].getName().equals("..") && !filesindir[i].equals(dirtoignore) && depth<=3){
	    			if(NumberUtils.isNumber(filesindir[i].getName())) {
	    				currentexpid=Integer.parseInt(filesindir[i].getName());
	    			}
	    			searchfiles(filesindir[i],usersid,zipoutputstream,attachedfiles, dirtoignore,servcon,depth+1,currentexpid);
	    		}else if(filesindir[i].isDirectory() && !arrayindex(usersid,filesindir[i].getName()).equals("")){
	    			//in order to avoid to attach directories currently written by the spectrometer we take only files older than 5 minute
	    			String currentid=arrayindex(usersid,filesindir[i].getName());
	    			if((System.currentTimeMillis()-filesindir[i].lastModified())>300000){
	    				if(zipoutputstream.get(currentid)==null){
	    	                File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)+"/"+currentid+".zip");
	    	                FileOutputStream baos=new FileOutputStream(zipfile);
	    	                ZipOutputStream zipout=new ZipOutputStream(baos);
	    					zipoutputstream.put(currentid, zipout);
	    					attachedfiles.put(currentid, new StringBuffer());
	    					System.err.println("made a stream for "+currentid);
	    				}
	    				addfiles(filesindir[i], zipoutputstream.get(currentid), attachedfiles.get(currentid),currentexpid,currentid);
	    			}
	    		}else{
	    			String currentid=arrayindex(usersid,filesindir[i].getName());
	    			if(!currentid.equals("")){
	    				if(zipoutputstream.get(currentid)==null){
	    					File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)+"/"+currentid+".zip");
    		                FileOutputStream baos=new FileOutputStream(zipfile);
	    		            ZipOutputStream zipout=new ZipOutputStream(baos);
	    					zipoutputstream.put(currentid, zipout);
	    					attachedfiles.put(currentid, new StringBuffer());
	    				}
	    				File rawfiledir=new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryrobot"));
	    				ZipOutputStream zipout=zipoutputstream.get(currentid);
	    				zipout.putNextEntry(new ZipEntry(GeneralUtils.getRelativePath(rawfiledir,filesindir[i])));
	    				FileInputStream fis=new FileInputStream(filesindir[i]);
	    				int read=0;
	    				while((read=fis.read())!=-1){
	    					zipout.write(read);
	    				}
	    				fis.close();
	    				if(filesindir[i].getName().equals("audita.txt")) {
	    					analyzeAudita(currentexpid, currentid, filesindir[i], zipoutputstream!=null);
	    				}
	    				attachedfiles.get(currentid).append(filesindir[i]+"; ");
	    				zipout.closeEntry();
	    			}
	    		}
	    	}
    	}
    }
    
    private static String arrayindex(ArrayList<String> list, String id){
    	for(int i=0;i<list.size();i++){
    		if(list.get(i).indexOf(id.toLowerCase())==0)
    			return id.toLowerCase();
    	}
    	return "";
    }
    
    private static void addfiles(File toadd, ZipOutputStream zipoutputstream, StringBuffer attachedfiles, int currentexpid, String usersid) throws IOException, TorqueException, Exception{
    	File[] filesindir=toadd.listFiles();
    	if(filesindir!=null){
	    	for(int i=0;i<filesindir.length;i++){
	    		if(filesindir[i].isDirectory() && !filesindir[i].getName().equals(".") && !filesindir[i].getName().equals("..")){
	    			if(NumberUtils.isNumber(filesindir[i].getName())) {
	    				currentexpid=Integer.parseInt(filesindir[i].getName());
	    			}
	    			addfiles(filesindir[i],zipoutputstream,attachedfiles,currentexpid, usersid);
	    		}else if(filesindir[i].isFile()){
    				if(filesindir[i].getName().equals("audita.txt") && DBSamplePeer.doSelectByUsersId(usersid).getDBExperimentDBSamplesOrderedByCategory().size()>currentexpid-10) {
    					analyzeAudita(currentexpid, usersid, filesindir[i], zipoutputstream!=null);
    				}
    				if(zipoutputstream!=null && attachedfiles!=null) {
    					File rawfiledir=new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryrobot"));
	    				zipoutputstream.putNextEntry(new ZipEntry(GeneralUtils.getRelativePath(rawfiledir,filesindir[i])));
	    				FileInputStream fis=new FileInputStream(filesindir[i]);
	    				int read=0;
	    				while((read=fis.read())!=-1){
	    					zipoutputstream.write(read);
	    				}
	    				fis.close();
	    				attachedfiles.append(filesindir[i]+"; ");
	    				zipoutputstream.closeEntry();
    				}
	    		}
	    	}
    	}
    }
    
    private static void analyzeAudita(int currentexpid, String usersid, File file, boolean dominusten) throws TorqueException, IOException {
        DBExperimentDBSample sample = null;
        if(dominusten)
        	sample = DBSamplePeer.doSelectByUsersId(usersid).getDBExperimentDBSamplesOrderedByCategory().get(currentexpid-10);
        else
        	sample = DBSamplePeer.doSelectByUsersId(usersid).getDBExperimentDBSamplesOrderedByCategory().get(currentexpid);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        LocalDateTime start = null;
        LocalDateTime end = null;
        int duration=0;
        int counter=1;
        while (line != null) {
          String padded = String.format("% 4d" , counter);
          if(line.contains("("+padded+",<" )) {
        		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        		end=LocalDateTime.parse(line.substring(line.indexOf("("+padded+",<" )+7,line.indexOf(">")-6), formatter);
            counter++;
        	}
            if(line.contains("started at")) {
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            	start=LocalDateTime.parse(line.substring(line.indexOf("started at" )+11,line.indexOf(",")-6), formatter);
            	duration+=(int)(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()-start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
            line = reader.readLine();
        }
        reader.close();
        sample.setDuration(duration);
        sample.save();    	
    }
		
		  /**
		   *  Reads file from uni mainz.(carbon)
		   *
		   * @param  args           The command line args.
		   * @exception  Exception  Database problems.
		   */
		  private static void executeReadMainzC(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    File[] files = new File(args[0]).listFiles();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "mainz2");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
		    for (int i = 0; i < files.length; i++) {
		    	try{
		    		if(files[i].getName().length()<15)
		    			throw new NumberFormatException("too short");
		    		int molid=Integer.parseInt(files[i].getName().substring(0, 8));
		    		Criteria crit2=new Criteria();
		    		if(files[i].getName().contains("HMBC"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 15);
		    		else if(files[i].getName().contains("HSQCed"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 24);
		    		else if(files[i].getName().contains("COSY"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 12);
		    		else if(files[i].getName().contains("ROESY"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 13);
		    		else if(files[i].getName().contains("HN-HMQC"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 18);
		    		else if(files[i].getName().contains("NOESY"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 17);
		    		else if(files[i].getName().contains("HSQC"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 14);
		    		else if(files[i].getName().contains("APT"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 20);
		    		else if(files[i].getName().contains("J-res"))
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 43);
		    		crit2.add(DBSpectrumPeer.MOLECULE_ID, molid);
		    		crit2.add(DBSpectrumPeer.REVIEW_FLAG,"true");
		    		List<DBSpectrum> v2=DBSpectrumPeer.doSelect(crit2);
		    		if(v2.size()==0 && files[i].getName().contains("HSQCed")) {
		    			crit2.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, 14);
		    			v2=DBSpectrumPeer.doSelect(crit2);
		    		}
		    		if(v2.size()!=1) {
		    			System.out.println("problem with "+files[i].getName());
		    		}else {
		    			DBSpectrumFile spectrumfile=new DBSpectrumFile();
		    			spectrumfile.setDBSpectrum(v2.get(0));
		    			spectrumfile.setType("rawdata");
		    			spectrumfile.setFormat("zip");
		    			byte[] content = FileUtils.readFileToByteArray(files[i]);
		    			spectrumfile.setData(new Base64().encode(content));
		    			spectrumfile.save();
		    			files[i].delete();
		    			//System.out.println(spectrumfile);
		    		}
		    	}catch(NumberFormatException ex) {
		    		
		    	}
		        if (files[i].getName().substring(files[i].getName().length() - 4).equals(".MOL")) {//MOL
		            try{
		          System.err.println("Reading "+files[i].getName());
		          BufferedReader buffRead = new BufferedReader(new FileReader(files[i]));
		          String line = "";
		          StringBuffer newMdl=new StringBuffer();
		          Vector<String> assignments=new Vector<String>();
		          while ((line = buffRead.readLine()) != null) {
		            if (line.indexOf("A ") == 0) {
		              assignments.add(line);
		              assignments.add(buffRead.readLine());
		            }else{
		              newMdl.append(line+"\n");
		            }
		          }
				      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
			          subData.setMol(newMdl.toString(),false,false,false);
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "13C");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			            
			            
			            
			            
		          subData.setSpectrumKeywords("ocmainz inhouse database");
		          //IBond[] bonds = subData.getMolWithH().getBonds();
		          boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
		          AllRingsFinder arf = new AllRingsFinder();
			        IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
		          Iterator<IBond> it=subData.getMolWithH().bonds().iterator();
		          int c=0;
		          while(it.hasNext()) {
		        	IBond bond=it.next();
		            if (AtomUtils.isValidDoubleBondConfiguration(subData.getMolWithH(),bond,ringSet) && files[i].getName().indexOf("1.")==-1) {
		              isDoubleBondSpecified[c] = true;
		            }
		            c++;
		          }
		          subData.setDoubleBondConfiguration(true);
		          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
		          subData.setComment(files[i].getName()+".mz2; multplicities generated from proton count");
		          for(int k=0;k<assignments.size();k++){
		            int atom=Integer.parseInt(((String)assignments.get(k)).substring(1).trim());
		            k++;
		            double value=Double.parseDouble((String)assignments.get(k));
		            CMLPeakList signals=subData.getSignalstable();
		            boolean added=false;
		            for(int l=0;l<signals.getPeakElements().size();l++){
		              if(signals.getPeakElements().get(l).getXValue()==value){
		            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
		                added=true;
		              }
		            }
		            if(!added){
		            	CMLPeak peak = new CMLPeak();
		            	peak.setXValue(value);
		            	peak.setYValue(0);
		            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
		            	subData.addToSignalstable(peak);
		            }
		          }
		          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
		          subData.setSmilesNonChiralProv(ssb.string1);
		          subData.setIsChiralProv(ssb.bool);
		          if(ssb.bool){
		        	  subData.setSmilesChiralProv(ssb.string2);
		          }
		          crit = new Criteria();
		          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
		          v = DBSpectrumPeer.doSelect(crit);
		          //if(v.size()==0){
		            /*crit=new Criteria();
		            crit.add(DBLiteraturePeer.TITLE,"13-C-NMR-Spektroskopie");
		            v=DBLiteraturePeer.doSelect(crit);
		            if(v.size()>0){
		              DBLiterature lit=(DBLiterature)v.get(0);
		              lit.read();
		              subData.getLiterature().add(lit);
		            }*/
		          	List<DBConditionType> conds = subData.getConditions();
		          	for(int k=0;k<conds.size();k++){
		          		DBConditionType cond=conds.get(k);
		          		cond.setCurrentValue(new String("Unreported"));
		          	}
		          	subData.setConditions(conds);
		            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
		            /*if (nmrprops.getProperty("writeserializedtodir").equals("true")) {
		              FileWriter out = new FileWriter(nmrprops.getProperty("serializeddir") + File.separatorChar + subData.spectrum.getNmrshiftdbNumber().replace('/', '|') + ".xml");
		              out.write(subData.serializeMe());
		              out.close();
		            }
		            if (nmrprops.getProperty("sendserializedemail").equals("true")) {
		              MailMessage email = new org.apache.turbine.util.mail.MailMessage(jetspeedprops.getProperty("mail.server"), nmrprops.getProperty("serializedemail"), jetspeedprops.getProperty("mail.support"), "Serialized input from" + subData.spectrum.getNmrshiftdbNumber().replace('/', '|'), subData.serializeMe());
		              if (email.send() == false) {
		                Log.error("Sending email with serialized input failed for spectrum " + subData.spectrum.getSpectrumId());
		              }
		            }*/
		            System.err.println("Wrote "+files[i].getName());
		          //}
		          files[i].delete();
		      } catch (Exception ex) {
		        ex.printStackTrace();
		      }
		        }
		    }
		  }
		  
		    private static String extractFieldName(String str) {
		        int index = str.indexOf('<');
		        if (index != -1) {
		            int index2 = str.indexOf('>', index);
		            if (index2 != -1) {
		                return str.substring(index + 1, index2);
		            }
		        }
		        return null;
		    }


		    
		  private static void executeReadAlk2(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
		    File file=new File("b2testset_shiftUNDshieldNOlit.sdf");
		    int i=-1;
      	  BufferedReader buffRead = new BufferedReader(new FileReader(file));
      	  String line = "";
      	  StringBuffer newMdl=new StringBuffer();
      	  Map<String,StringBuffer> assignments=new HashMap<>();
      	  while ((line = buffRead.readLine()) != null) {
              if (line.startsWith("> ")) {
            	  String dataHeader = extractFieldName(line);
            	  StringBuffer value=new StringBuffer();
            	  while(!line.isBlank()) {
            		  value.append(line);
                  	  line = buffRead.readLine();
                  }
            	  if(!assignments.containsKey(dataHeader))
            		  assignments.put(dataHeader,value);
            	  else
            		  assignments.get(dataHeader).append(value.toString());
              }
      		  if(line.equals("$$$$")){
			  try{
				  i++;
		          System.err.println("Reading "+i);
				  SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				  if(newMdl.toString().startsWith("\n\n"))
					  newMdl=new StringBuffer(newMdl.substring(1));
			      subData.setMeasured();
				  subData.setMol(newMdl.toString(),false,false,false);
				  for(CMLAtom atom : subData.getCMLMolecule().getAtomArray().getAtoms()) {
					  atom.setX2(atom.getX3());
					  atom.setY2(atom.getY3());
				  }
			          IAtomContainer molwithH=subData.getMolWithH();
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "11B");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			            
			            
			            
			            
		          subData.setSpectrumKeywords("nmr sharc");
		          //IBond[] bonds = subData.getMolWithH().getBonds();
		          boolean[] isDoubleBondSpecified = new boolean[molwithH.getBondCount()];
		          AllRingsFinder arf = new AllRingsFinder();
			        IRingSet ringSet=arf.findAllRings(molwithH);
		          Iterator<IBond> it=molwithH.bonds().iterator();
		          int c=0;
		          while(it.hasNext()) {
		        	IBond bond=it.next();
		            if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
		              isDoubleBondSpecified[c] = true;
		            }
		            c++;
		          }
		          subData.setDoubleBondConfiguration(true);
		          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
		          subData.setChemName(assignments.get("MOL_NAME").toString());
		          subData.setComment(assignments.get("NMR_SHARC_ID")+"; "+assignments.get("Comment")+assignments.get("COMMENT")+"; multiplicities generated from proton count");
		            	CMLPeak peak = new CMLPeak();
		            	peak.setXValue(assignments.get("NMRSignal.d").toString());
		            	peak.setYValue(0);
		            	for(IAtom atom : subData.getMolWithH().atoms())
		            		if(atom.getSymbol().equals("B"))
		            			peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),atom));
            	subData.addToSignalstable(peak);
		          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
		          subData.setSmilesNonChiralProv(ssb.string1);
		          subData.setIsChiralProv(ssb.bool);
		          if(ssb.bool){
		        	  subData.setSmilesChiralProv(ssb.string2);
		          }
		          crit = new Criteria();
		          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
		          v = DBSpectrumPeer.doSelect(crit);
		          //if(v.size()==0){
		            /*crit=new Criteria();
		            crit.add(DBLiteraturePeer.TITLE,"13-C-NMR-Spektroskopie");
		            v=DBLiteraturePeer.doSelect(crit);
		            if(v.size()>0){
		              DBLiterature lit=(DBLiterature)v.get(0);
		              lit.read();
		              subData.getLiterature().add(lit);
		            }*/
		          	List<DBConditionType> conds = subData.getConditions();
		          	for(int k=0;k<conds.size();k++){
		          		DBConditionType cond=conds.get(k);
		          		if(cond.getConditionTypeId()==4)
		          			cond.setCurrentValue("Gaussian");
		          		else if(cond.getConditionTypeId()==6)
		          			cond.setCurrentValue(assignments.get("MODEL_GEO_MTD").toString());
		          		else if(cond.getConditionTypeId()==7)
		          			cond.setCurrentValue(assignments.get("MODEL_GEO_BAS").toString());
		          		else if(cond.getConditionTypeId()==9)
		          			cond.setCurrentValue(assignments.get("MODEL_NMR_BAS").toString());
		          		else
		          			cond.setCurrentValue(new String("Unreported"));
		          	}
		          	subData.setConditions(conds);
		            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
		            /*if (nmrprops.getProperty("writeserializedtodir").equals("true")) {
		              FileWriter out = new FileWriter(nmrprops.getProperty("serializeddir") + File.separatorChar + subData.spectrum.getNmrshiftdbNumber().replace('/', '|') + ".xml");
		              out.write(subData.serializeMe());
		              out.close();
		            }
		            if (nmrprops.getProperty("sendserializedemail").equals("true")) {
		              MailMessage email = new org.apache.turbine.util.mail.MailMessage(jetspeedprops.getProperty("mail.server"), nmrprops.getProperty("serializedemail"), jetspeedprops.getProperty("mail.support"), "Serialized input from" + subData.spectrum.getNmrshiftdbNumber().replace('/', '|'), subData.serializeMe());
		              if (email.send() == false) {
		                Log.error("Sending email with serialized input failed for spectrum " + subData.spectrum.getSpectrumId());
		              }
		            }*/
		            System.err.println("Wrote "+subData.getComment());
		          //}
		      } catch (Exception ex) {
		        ex.printStackTrace();
		      }
			  newMdl=new StringBuffer("");
			  assignments=new HashMap<>();


		  }else{
			  newMdl.append(line+"\n");
		  }
	  }
	  buffRead.close();
		  }
		  
		  private static void executeReadAlk1(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
		    IteratingSDFReader iter = new IteratingSDFReader(new FileReader("SiNMR2008r1open4frank.sdf"), DefaultChemObjectBuilder.getInstance());
		    int i=-1;
		    while(iter.hasNext()){
			  try{
				  i++;
				  IAtomContainer mol = (IAtomContainer)iter.next();
		          System.err.println("Reading "+i);
				      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
			          subData.setMol(mol,false,false,false);
			          IAtomContainer molwithH=subData.getMolWithH();
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "29Si");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			            
			            
			            
			            
		          subData.setSpectrumKeywords(mol.getProperty("Keyword"));
		          //IBond[] bonds = subData.getMolWithH().getBonds();
		          boolean[] isDoubleBondSpecified = new boolean[molwithH.getBondCount()];
		          AllRingsFinder arf = new AllRingsFinder();
			        IRingSet ringSet=arf.findAllRings(molwithH);
		          Iterator<IBond> it=molwithH.bonds().iterator();
		          int c=0;
		          while(it.hasNext()) {
		        	IBond bond=it.next();
		            if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
		              isDoubleBondSpecified[c] = true;
		            }
		            c++;
		          }
		          subData.setDoubleBondConfiguration(true);
		          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
		          subData.setCasNumber(mol.getProperty("CAS_RN"));
		          subData.setChemName(mol.getProperty("MOL_NAME"));
		          subData.setComment(mol.getProperty("REFNO")+"; "+mol.getProperty("Mol_ID")+"; NMR standard: "+mol.getProperty("NMRSignal.Standard")+"; generated from proton count");
		            	CMLPeak peak = new CMLPeak();
		            	peak.setXValue(mol.getProperty("NMRSignal.d"));
		            	peak.setYValue(0);
		            	for(IAtom atom : mol.atoms())
		            		if(atom.getSymbol().equals("Si"))
		            			peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),atom));
            	subData.addToSignalstable(peak);
            	DBLiterature lit=new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE);
            	lit.journaltitle=mol.getProperty("Source");
            	lit.setAuthor(mol.getProperty("Author"));
            	lit.volume=Integer.parseInt(mol.getProperty("Volume"));
            	lit.from=Integer.parseInt(mol.getProperty("Page"));
            	lit.year=Integer.parseInt(mol.getProperty("Year"));
            	ArrayList<DBLiterature> lits=new ArrayList<DBLiterature>();
            	lits.add(lit);
            	subData.setLiterature(lits);
		          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
		          subData.setSmilesNonChiralProv(ssb.string1);
		          subData.setIsChiralProv(ssb.bool);
		          if(ssb.bool){
		        	  subData.setSmilesChiralProv(ssb.string2);
		          }
		          crit = new Criteria();
		          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
		          v = DBSpectrumPeer.doSelect(crit);
		          //if(v.size()==0){
		            /*crit=new Criteria();
		            crit.add(DBLiteraturePeer.TITLE,"13-C-NMR-Spektroskopie");
		            v=DBLiteraturePeer.doSelect(crit);
		            if(v.size()>0){
		              DBLiterature lit=(DBLiterature)v.get(0);
		              lit.read();
		              subData.getLiterature().add(lit);
		            }*/
		          	List<DBConditionType> conds = subData.getConditions();
		          	for(int k=0;k<conds.size();k++){
		          		DBConditionType cond=conds.get(k);
		          		if(cond.getConditionTypeId()==2)
		          			cond.setCurrentValue(mol.getProperty("NMRSignal.Solvent"));
		          		else
		          			cond.setCurrentValue(new String("Unreported"));
		          	}
		          	subData.setConditions(conds);
		            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
		            /*if (nmrprops.getProperty("writeserializedtodir").equals("true")) {
		              FileWriter out = new FileWriter(nmrprops.getProperty("serializeddir") + File.separatorChar + subData.spectrum.getNmrshiftdbNumber().replace('/', '|') + ".xml");
		              out.write(subData.serializeMe());
		              out.close();
		            }
		            if (nmrprops.getProperty("sendserializedemail").equals("true")) {
		              MailMessage email = new org.apache.turbine.util.mail.MailMessage(jetspeedprops.getProperty("mail.server"), nmrprops.getProperty("serializedemail"), jetspeedprops.getProperty("mail.support"), "Serialized input from" + subData.spectrum.getNmrshiftdbNumber().replace('/', '|'), subData.serializeMe());
		              if (email.send() == false) {
		                Log.error("Sending email with serialized input failed for spectrum " + subData.spectrum.getSpectrumId());
		              }
		            }*/
		            System.err.println("Wrote "+subData.getComment());
		          //}
		      } catch (Exception ex) {
		        ex.printStackTrace();
		      }
		    }
		  }

		  
		  private static void executeReadAlk3(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
		    File file=new File("sinmr_Freiberg2011_T5fstrictOK_nmrE.sdf");
		    System.err.println("Reading "+file.getName());
		    BufferedReader buffRead = new BufferedReader(new FileReader(file));
      	  	String line = "";
      	  	StringBuffer newMdl=new StringBuffer();
      	  	int count=0;
      	  	while ((line = buffRead.readLine()) != null) {
      		  if(line.equals("$$$$")){
      			  try{
      				  if(newMdl.toString().startsWith("\n\n"))
      					  newMdl=new StringBuffer(newMdl.substring(1));
      				  InputStream ins = new ByteArrayInputStream(newMdl.toString().getBytes());
      				  NmredataReader reader = new NmredataReader(ins);
      				  NmreData data = reader.read();
      				  System.err.println("Reading "+count);
      				  IAtomContainer mol = data.getMolecule();
						for(int k=0;k<data.getSpectra().size();k++) {
						      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
					          subData.setMol(mol,false,false,false);
					          IAtomContainer molwithH=subData.getMolWithH();
								crit = new Criteria();
								crit.add(DBSpectrumTypePeer.NAME, ((NMRSpectrum)data.getSpectra().get(k)).getNucleus());
								v = DBSpectrumTypePeer.doSelect(crit);
								subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
								subData.setSpectrumKeywords("Si-Datenbank");
								boolean[] isDoubleBondSpecified = new boolean[molwithH.getBondCount()];
								AllRingsFinder arf = new AllRingsFinder();
								IRingSet ringSet=arf.findAllRings(molwithH);
								Iterator<IBond> it=molwithH.bonds().iterator();
								int c=0;
								while(it.hasNext()) {
									IBond bond=it.next();
									if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
										isDoubleBondSpecified[c] = true;
									}
									c++;
								}
								subData.setDoubleBondConfiguration(true);
								subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
								subData.setCasNumber(mol.getProperty("CAS_RN"));
								subData.setChemName(GeneralUtils.replace(mol.getProperty("MOL_NAME")," , ","\r\n"));
								subData.setComment(mol.getProperty("COMMENT"));
								StringTokenizer st=new StringTokenizer(mol.getProperty("NMREDATA_ASSIGNMENT"),"\r\n");
								List<Double> peaks=new ArrayList<>();
								while(st.hasMoreTokens()) {
									String inline=removeBackslash(st.nextToken());
									CMLPeak peak = new CMLPeak();
									System.out.println(inline);
									peak.setXValue(Double.parseDouble(inline.split(",")[1].trim()));
									peak.setYValue(0);
									peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),mol.getAtom(Integer.parseInt(inline.split(",")[2].trim())-1)));
									subData.addToSignalstable(peak);
									peaks.add(peak.getXValue());
								
								}
							for(int i=0;i<((NMRSpectrum)data.getSpectra().get(k)).getPeakTable().length;i++) {
								double shift=((NMRSpectrum)data.getSpectra().get(k)).getPeakTable()[i].getPosition()[0];
								if(!peaks.contains(shift)) {
									CMLPeak peak = new CMLPeak();
									peak.setXValue(shift);
									peak.setYValue(0);
									subData.addToSignalstable(peak);								
								}
							}
							DBLiterature lit=new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE);
							lit.journaltitle=removeBackslash(mol.getProperty("Source"));
							System.out.println(lit.journaltitle);
							lit.setAuthor(processAuthorsAlk(mol.getProperty("Author")));
							lit.volume=Integer.parseInt(mol.getProperty("Volume"));
							String[] years=((String)mol.getProperty("Page")).split("-");
							lit.from=Integer.parseInt(years[0]);
							lit.to=Integer.parseInt(years[1]);
							lit.year=Integer.parseInt(mol.getProperty("YEAR"));
							String id=mol.getProperty("NMREDATA_ID");
							String doi=id.substring(4,id.indexOf(" ")).trim();
							doi=GeneralUtils.replace(doi, "=PLUS=", "");
							lit.setDoi(doi);
							String cas=id.substring(id.indexOf("CAS=")+4).trim();
						        cas=cas.substring(0,cas.length()-2);	
							subData.setCasNumber(cas);
							ArrayList<DBLiterature> lits=new ArrayList<DBLiterature>();
							lits.add(lit);
							subData.setLiterature(lits);
							StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
							GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
							subData.setSmilesNonChiralProv(ssb.string1);
							subData.setIsChiralProv(ssb.bool);
							if(ssb.bool){
								subData.setSmilesChiralProv(ssb.string2);
							}
							System.out.println(""+subData.getExistingMoleculeId()+"_"+ssb.string1);
							crit = new Criteria();
							crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
							v = DBSpectrumPeer.doSelect(crit);
				          	List<DBConditionType> conds = subData.getConditions();
				          	for(int l=0;l<conds.size();l++){
				          		DBConditionType cond=conds.get(l);
				          		if(cond.getConditionTypeId()==2 && mol.getProperty("NMRSignal.Solvent")!=null)
				          			cond.setCurrentValue(mol.getProperty("NMRSignal.Solvent"));
				          		else if(cond.getConditionTypeId()==3 && mol.getProperty("NMRSignal.Solvent")!=null)
                                                                cond.setCurrentValue(""+((NMRSpectrum)data.getSpectra().get(k)).getFrequency());
                                                        else
                                                                cond.setCurrentValue(new String("Unreported"));
				          	}
				          	subData.setConditions(conds);
							//if(v.size()==0){
								subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
							//}
							System.err.println("Wrote "+subData.getComment());
						}
			      } catch (Exception ex) {
			        ex.printStackTrace();
			      }
			  	newMdl=new StringBuffer("");
        		  }else{
        			  newMdl.append(line+"\n");
        		  }
		    }
		  }

		  public static String removeBackslash(String old){
			  old=old.trim();
			  if(old.charAt(old.length()-1)=='\\' || old.charAt(old.length()-1)=='.'){
				  old=old.substring(0,old.length()-1);
			  }
			  if(old.charAt(0)=='.')
				  old=old.substring(1);
			  return old.trim();
		  }

		  public static String processAuthorsAlk(String old) {
			  StringBuffer news=new StringBuffer();
			  StringTokenizer st=new StringTokenizer(old,";");
			  while(st.hasMoreTokens()){
				  String name=st.nextToken();
				  news.append(name.split(",")[1].trim()+" "+name.split(",")[0].trim()).append(";");  
			  }
			  return GeneralUtils.removeLastComma(news).toString();
		  }
		  
		  /**
           *  Reads file from uni mainz.(carbon)
           *
           * @param  args           The command line args.
           * @exception  Exception  Database problems.
           */
          private static void executeReadWavefun(String[] args) throws Exception {
            Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
            TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
            config.initialize();
            File[] files = new File(args[0]).listFiles();
            TurbineUser user = null;
            Criteria crit = new Criteria();
            crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "mainz2");
            List v = NmrshiftdbUserPeer.doSelect(crit);
            //Look if the bulkoad user exists
            if (v.size() > 0) {
              user = (TurbineUser) v.get(0);
            } else {
              throw new Exception("no bulkload user");
            }
            String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
            String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
            Properties nmrprops = new Properties();
            Properties jetspeedprops = new Properties();
            nmrprops.load(new FileInputStream(nmrpropsFile));
            jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().substring(files[i].getName().length() - 4).equals(".cml")) {
                    try{
                  System.err.println("Reading "+files[i].getName());
                  CMLBuilder builder = new CMLBuilder(false);
                  Document cmldoc = builder.build(new FileReader(files[i]));
                  CMLCml cmlcml = (CMLCml) builder.parseString(cmldoc.toXML());
                  SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
                  subData.setCalculated();
                  CMLReader cmlreader=new CMLReader(new ByteArrayInputStream(cmlcml.getChildCMLElement("molecule", 0).toXML().getBytes()));
                  IChemFile mol = new ChemFile();
                  mol=(IChemFile)cmlreader.read(mol);
                  subData.setMol(mol.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0),false,false,false);
                  subData.setChemName(files[i].getName().substring(0,files[i].getName().length()-4));
                  Elements spectra = cmlcml.getChildCMLElements("spectrum");
                  for(int k=0;k<spectra.size();k++){
                      CMLSpectrum spectrum=(CMLSpectrum)spectra.get(k);
                      crit = new Criteria();
                      crit.add(DBSpectrumTypePeer.NAME, spectrum.getMetadataListElements().get(0).getMetadataDescendants().get(0).getContentAttribute().getValue());
                      v = DBSpectrumTypePeer.doSelect(crit);
                      subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
                      subData.setSpectrumKeywords("spartan");
                      boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
                        AllRingsFinder arf = new AllRingsFinder();
                        IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
                      Iterator<IBond> it=subData.getMolWithH().bonds().iterator();
                      int c=0;
                      while(it.hasNext()) {
                        IBond bond=it.next();
                        if (AtomUtils.isValidDoubleBondConfiguration(subData.getMolWithH(),bond,ringSet) && files[i].getName().indexOf("1.")==-1) {
                          isDoubleBondSpecified[c] = true;
                        }
                        c++;
                      }
                      subData.setDoubleBondConfiguration(true);
                      subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
                      subData.setComment(files[i].getName()+"; multplicities generated from proton count");
                      CMLPeakList peaklist= spectrum.getPeakListElements().get(0);
                      subData.setSignalstable(peaklist);
                      StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                      GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
                      subData.setSmilesNonChiralProv(ssb.string1);
                      subData.setIsChiralProv(ssb.bool);
                      if(ssb.bool){
                          subData.setSmilesChiralProv(ssb.string2);
                      }
                    List<DBConditionType> conds = subData.getConditions();
                    for(int m=0;m<conds.size();m++){
                        DBConditionType cond=conds.get(m);
                        if(cond.getConditionName().equals("Program"))
                            cond.setCurrentValue(new String("Spartan"));
                        else
                            cond.setCurrentValue(new String("Unreported"));
                    }
                    subData.setConditions(conds);
                    subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
                  }
                  System.err.println("Wrote "+files[i].getName());
                  //files[i].delete();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
                }
            }
          }

          
          /**
           *  Reads file from uni bochum.(carbon)
           *
           * @param  args           The command line args.
           * @exception  Exception  Database problems.
           */
          private static void executeReadBochum(String[] args) throws Exception {
            Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
            TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
            config.initialize();
            File[] files = new File(args[0]).listFiles();
            TurbineUser user = null;
            Criteria crit = new Criteria();
            crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
            List v = NmrshiftdbUserPeer.doSelect(crit);
            //Look if the bulkoad user exists
            if (v.size() > 0) {
              user = (TurbineUser) v.get(0);
            } else {
              throw new Exception("no bulkload user");
            }
            String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
            String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
            Properties nmrprops = new Properties();
            Properties jetspeedprops = new Properties();
            nmrprops.load(new FileInputStream(nmrpropsFile));
            jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
            for (int i = 0; i < files.length; i++) {
              try {
                if (files[i].getName().substring(files[i].getName().length() - 4).equals(".mol")) {
                  System.err.println("Reading "+files[i].getName());
                  BufferedReader buffRead = new BufferedReader(new FileReader(files[i]));
                  String line = "";
                  StringBuffer newMdl=new StringBuffer();
                  Vector<String> assignments=new Vector<String>();
                  String name="";
                  String temp="";
                  String fstrength="";
                  String solvent="";
                  while ((line = buffRead.readLine()) != null) {
                    if (line.indexOf("A") == 0) {
                      assignments.add(line);
                      assignments.add(buffRead.readLine());
                    }else if(line.indexOf("<Name [IUPAC]>")>-1){
                        name=buffRead.readLine().trim();
                    }else if(line.indexOf("<Temperature [K]>")>-1){
                        temp=buffRead.readLine().trim();
                    }else if(line.indexOf("<Field Strength [MHz]>")>-1){
                        fstrength=buffRead.readLine().trim();
                    }else if(line.indexOf("<Solvent>")>-1){
                        solvent=buffRead.readLine().trim();
                    /*}else if(line.indexOf("<Name [IUPAC]>")>-1){
                        name=buffRead.readLine().trim();*/
                    }else{
                      newMdl.append(line+"\n");
                    }
                  }
                      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
                      subData.setMol(newMdl.toString(),false,false,false);
                        crit = new Criteria();
                        crit.add(DBSpectrumTypePeer.NAME, "13C");
                        v = DBSpectrumTypePeer.doSelect(crit);
                        subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
                  subData.setSpectrumKeywords("paper2013b");
                  subData.setChemName(name);
                  //IBond[] bonds = subData.getMolWithH().getBonds();
                  boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
                    AllRingsFinder arf = new AllRingsFinder();
                    IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
                  Iterator<IBond> it=subData.getMolWithH().bonds().iterator();
                  int c=0;
                  while(it.hasNext()) {
                    IBond bond=it.next();
                    if (AtomUtils.isValidDoubleBondConfiguration(subData.getMolWithH(),bond,ringSet) && files[i].getName().indexOf("1.")==-1) {
                      isDoubleBondSpecified[c] = true;
                    }
                    c++;
                  }
                  subData.setDoubleBondConfiguration(true);
                  subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
                  subData.setComment(files[i].getName()+"; multplicities generated from proton count");
                  for(int k=0;k<assignments.size();k++){
                    int atom=Integer.parseInt(((String)assignments.get(k)).substring(1).trim());
                    k++;
                    double value=Double.parseDouble((String)assignments.get(k));
                    CMLPeakList signals=subData.getSignalstable();
                    boolean added=false;
                    for(int l=0;l<signals.getPeakElements().size();l++){
                      if(signals.getPeakElements().get(l).getXValue()==value){
                        signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
                        added=true;
                      }
                    }
                    if(!added){
                        CMLPeak peak = new CMLPeak();
                        peak.setXValue(value);
                        peak.setYValue(0);
                        peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
                        subData.addToSignalstable(peak);
                    }
                  }
                  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
                  subData.setSmilesNonChiralProv(ssb.string1);
                  subData.setIsChiralProv(ssb.bool);
                  if(ssb.bool){
                      subData.setSmilesChiralProv(ssb.string2);
                  }
                  crit = new Criteria();
                  crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
                  v = DBSpectrumPeer.doSelect(crit);
                    List<DBConditionType> conds = subData.getConditions();
                    for(int k=0;k<conds.size();k++){
                        DBConditionType cond=conds.get(k);
                        if(cond.getConditionTypeId()==1)
                            cond.setCurrentValue(temp);
                        else if(cond.getConditionTypeId()==2)
                            cond.setCurrentValue(solvent);
                        else if(cond.getConditionTypeId()==3)
                            cond.setCurrentValue(fstrength);
                        else
                            cond.setCurrentValue(new String("Unreported"));
                    }
                    subData.setConditions(conds);
                    subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
                    System.err.println("Wrote "+files[i].getName());
                  files[i].delete();
                }
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          }

          
          /**
		   *  Executes checking of double molecules
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeCheckDoubleMolecules(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select distinct SMILES_STRING from MOLECULE");
			  int fehler = 0;
			  while (rs.next()) {
				  try {
					  Statement stmt2=dbconn.createStatement();
					  Statement stmt3=dbconn.createStatement();
					  String sql="select SMILES_STRING_CHIRAL, count(*) from MOLECULE where SMILES_STRING=\""+rs.getString(1)+"\" group by SMILES_STRING_CHIRAL;";
					  ResultSet rs2=stmt2.executeQuery(sql);
					  while(rs2.next()){
						  if(rs2.getInt(2)>1){
							  ResultSet rs3=null;
							  if(rs2.getString(1)==null){
								  sql="select MOLECULE_ID from MOLECULE where SMILES_STRING=\""+rs.getString(1)+"\" and SMILES_STRING_CHIRAL "+(rs2.getString(1)==null ? "is null" : "= \""+GeneralUtils.replace(rs2.getString(1),"\\","\\\\")+"\"")+";";
								  rs3=stmt3.executeQuery(sql);
							  }else{
								  PreparedStatement updateSales = dbconn.prepareStatement("select MOLECULE_ID from MOLECULE where SMILES_STRING= ? and SMILES_STRING_CHIRAL = ?");
								  updateSales.setString(1, rs.getString(1)); 
								  updateSales.setString(2, rs2.getString(1)); 
								  rs3=updateSales.executeQuery();
							  }
							  rs3.next();
							  DBMolecule mol1=DBMoleculePeer.retrieveByPK(new NumberKey(rs3.getInt(1)));
							  rs3.next();
							  DBMolecule mol2=DBMoleculePeer.retrieveByPK(new NumberKey(rs3.getInt(1)));
							  if(mol1.selectSpectraByUser(null,false, false, null).size()>0 && mol2.selectSpectraByUser(null,false, false, null).size()>0){
								  System.out.println("Seems there are more than one entry for "+rs.getString(1)+" "+rs2.getString(1)+" "+mol1.getMoleculeId()+" "+mol2.getMoleculeId());
								  fehler++;
								  if (warn("I can try to correct this", new BufferedReader(new InputStreamReader(System.in)))) {
									  DBAtom[] atomsmol1=mol1.getAtomsAsArrayInMdlOrder();
									  List list=new UniversalIsomorphismTester().getIsomorphAtomsMap(mol1.getAsCDKMolecule(1), mol2.getAsCDKMolecule(1));
									  if(list==null){
										  System.out.println("Oops. Seems they are not actually isomorph");
									  }else{
										  //Merge chemnames
										  List chemnames=mol2.getDBChemicalNames();
										  for(int k=0;k<chemnames.size();k++){
											  DBChemicalName chemname=(DBChemicalName)chemnames.get(k);
											  boolean mol1hasname=false;
											  List chemnames1=mol1.getDBChemicalNames();
											  for(int l=0;l<chemnames1.size();l++){
												  if(((DBChemicalName)chemnames1.get(l)).getName().equals(chemname.getName()))
													  mol1hasname=true;
											  }
											  if(!mol1hasname){
												  chemname.setMoleculeId(mol1.getMoleculeId());
												  chemname.save();
											  }
										  }
										  //Change spectra
										  List spectra=mol2.selectSpectraByUser(null, false, false, null);
										  for(int k=0;k<spectra.size();k++){
											  DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
											  spectrum.setMoleculeId(mol1.getMoleculeId());
											  //Change signals
											  List signals=spectrum.getDBSignals();
											  for(int l=0;l<signals.size();l++){
												  DBSignal signal=(DBSignal)signals.get(l);
												  List atoms=signal.getAtoms();
												  Criteria crit=new Criteria();
												  crit.add(DBSignalDBAtomPeer.SIGNAL_ID,signal.getSignalId());
												  DBSignalDBAtomPeer.doDelete(crit);
												  for(int m=0;m<atoms.size();m++){
													  DBAtom atom=(DBAtom)atoms.get(m);
													  Iterator it=list.listIterator();
													  while(it.hasNext()){
														  RMap rmap=(RMap)it.next();
														  if(rmap.getId2()==atom.getMdlNumber()){
															  DBSignalDBAtom signalatom=new DBSignalDBAtom();
															  signalatom.setDBShiftRelatedBySignalId(signal.getFirstShift());
															  signalatom.setDBAtom(atomsmol1[rmap.getId1()]);
															  signalatom.setAxis(1);
															  signalatom.save();
															  break;
														  }
													  }
												  }
											  }
											  //This is to reload signals list in spectrum object
											  spectrum.save();
											  spectrum=DBSpectrumPeer.retrieveByPK(spectrum.getSpectrumId());
											  spectrum.setSpecfile(getSpecfile2(spectrum));
											  spectrum.setSimpleSpecfile(getSimpleSpecfile2(spectrum));
											  spectrum.save();
										  }
										  mol2.delete(dbconn);
									  }
								  }
							  }
						  }
					  }
				  } catch (Exception ex) {
					  ex.printStackTrace();
				  }
			  }
			  System.out.println("There were "+fehler+" double entries !");
		  }
		  
		  private static void executeMakeRobienexport(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
              try{
                  List l = DBMoleculePeer.executeQuery("select SPECTRUM_ID from SPECTRUM join SPECTRUM_KEYWORD using (SPECTRUM_ID) join KEYWORD using (KEYWORD_ID ) where REVIEW_FLAG='true' and KEYWORD.KEYWORD='paper2013b'");
                  File outputFile = new File("/tmp/exportpaper.sd");
                  FileOutputStream fos = new FileOutputStream(outputFile);
                  for(int i=0;i<l.size();i++){
                      try{
                          DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
                          DBMolecule mol=spectrum.getDBMolecule();
                          System.err.println(mol.getMoleculeId()+"");
                          IAtomContainer cdkmol = mol.getAsCDKMolecule(1);
                          StringWriter writer = new StringWriter();
                          MDLV2000Writer mdlWriter = new MDLV2000Writer(writer);
                          mdlWriter.write(cdkmol);
                          StringBuffer emailtext=new StringBuffer(writer.toString());
                          emailtext.append(">  <chemical.shift.values>\r\n");
                          for(int m=0;m<spectrum.getDBSignals().size();m++){
                              for(int k=0;k<((DBSignal)spectrum.getDBSignals().get(m)).getDBSignalDBAtoms().size();k++){
                                  emailtext.append(((DBSignalDBAtom)((DBSignal)spectrum.getDBSignals().get(m)).getDBSignalDBAtoms().get(k)).getDBAtom().getMdlNumberPlus1());
                                  emailtext.append(" = ");
                                  emailtext.append(((DBSignal)spectrum.getDBSignals().get(m)).getFirstShift().getValue());
                                  emailtext.append("\r\n");
                              }
                          }
                          emailtext.append("\r\n\r\n$$$$\r\n");
                          fos.write(emailtext.toString().getBytes());
                      }
                      catch(Exception ex){
                          ex.printStackTrace();
                      }
                  }
                  fos.close();
              }catch(Exception ex){
                  ex.printStackTrace();
              }
		  }

		  private static void executeMakeChemspider(String[] args) throws Exception{
			  File multfile = new File("/home/shk3/Desktop/NMRShiftDBlinks_25_Feb_2011.txt");
			  BufferedReader buffRead0 = new BufferedReader(new FileReader(multfile));
			  String line0 = buffRead0.readLine();
			  FileOutputStream outadd=new FileOutputStream("/home/shk3/Desktop/add.sd");
			  FileOutputStream outremove=new FileOutputStream("/home/shk3/Desktop/remove.text");
			  FileOutputStream outwrong=new FileOutputStream("/home/shk3/Desktop/wrong.text");
			  outwrong.write(new String("NMRShiftDB ID|NMRShiftDB InChI|CSID|CS InChI\r\n").getBytes());
			  outremove.write(new String("NMRShiftDB ID|CSID\r\n").getBytes());
			  Map<Integer,Integer> alreadyinchemspider=new HashMap<Integer,Integer>();
			  Map<Integer,String> alreadyinchemspiderinchis=new HashMap<Integer,String>();
			  int countdouble=0;
			  while ((line0 = buffRead0.readLine()) != null) {
				  StringTokenizer st= new StringTokenizer(line0,"|");
				  int csid=Integer.parseInt(st.nextToken());
				  int nmrid=Integer.parseInt(st.nextToken());
				  String inchi="InChI=1"+st.nextToken().substring(8);
				  if(alreadyinchemspider.containsKey(nmrid)){
					  countdouble++;
				  }else{
					  alreadyinchemspider.put(nmrid,csid);
					  alreadyinchemspiderinchis.put(nmrid,inchi);
				  }
			  }
			  System.err.println(countdouble);
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select distinct MOLECULE_ID from MOLECULE join SPECTRUM using (MOLECULE_ID) where REVIEW_FLAG='true'");
			  while (rs.next()) {
				  try {
					  DBMolecule dbmol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  System.err.println(dbmol.getMoleculeId());
					  if(!alreadyinchemspider.containsKey(dbmol.getMoleculeId())){
						  HashMap map=new HashMap();
						  map.put("PUBCHEM_EXT_DATASOURCE_REGID",dbmol.getMoleculeId());
						  map.put("PUBCHEM_EXT_DATASOURCE_URL","http://www.nmrshiftdb.org");
						  map.put("PUBCHEM_EXT_SUBSTANCE_URL", "http://www.nmrshiftdb.org/portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/" + dbmol.getMoleculeId());
						  if(!dbmol.getCasNumber().equals(""))
							  map.put("PUBCHEM_GENERIC_REGISTRY_NAME",dbmol.getCasNumber()+"");
						  StringBuffer synonym=new StringBuffer();
						  List v=dbmol.getDBChemicalNames();
						  for(int k=0;k<v.size();k++){
							  synonym.append(((DBChemicalName)v.get(k)).getName()+"\r\n");
						  }
						  v=dbmol.getDBCanonicalNames();
						  for(int k=0;k<v.size();k++){
							  synonym.append(((DBCanonicalName)v.get(k)).getName()+"\r\n");
						  }
						  if(synonym.length()>4)
							  map.put("PUBCHEM_SUBSTANCE_SYNONYM",synonym.substring(0,synonym.length()-3).toString());
						  outadd.write(dbmol.getStructureFile(2,map,true).getBytes());
						  Set set = map.keySet();
						  Iterator iterator = set.iterator();
						  while (iterator.hasNext()) {
							  Object element = iterator.next();
							  outadd.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
						  }
						  outadd.write(new String("$$$$\r\n").getBytes());
					  }else{
						  if(alreadyinchemspiderinchis.get(dbmol.getMoleculeId()).indexOf(dbmol.getInchi())==-1){
							  outwrong.write(new String(dbmol.getMoleculeId()+"|"+dbmol.getInchi()+"|"+alreadyinchemspider.get(dbmol.getMoleculeId())+"|"+alreadyinchemspiderinchis.get(dbmol.getMoleculeId())+"\r\n").getBytes());
						  }
						  alreadyinchemspider.remove(dbmol.getMoleculeId());
					  }
				  }catch(Exception ex){
					  ex.printStackTrace();
				  }
			  }
			  for(int id : alreadyinchemspider.keySet()){
				  outremove.write(new String(id+"|"+alreadyinchemspider.get(id)+"\r\n").getBytes());
			  }
			  outwrong.close();
			  outremove.close();
			  outadd.close();
		  }

		  /**
		   *  Creates a file for pubchem submit
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Description of Exception
		   */
		  private static void executeMakePubchem(String[] args) throws Exception {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  /*try{
				  //This file is created by searching for source "nmrshiftdb" in pubchem and downloading the result as xml
				  File multfile = new File("/home/shk3/Desktop/nmrshiftdb.xml");
				  BufferedReader buffRead0 = new BufferedReader(new FileReader(multfile));
				  String line0 = "";
				  FileOutputStream out=new FileOutputStream("/home/shk3/Desktop/nmrshiftdbtodelete.sd");
				  Map<Integer,String> alreadyinpubchem=new HashMap<Integer,String>();
				  int nmrshiftdbnumber=0;
				  while ((line0 = buffRead0.readLine()) != null) {
					  if(!line0.trim().equals("") && line0.indexOf("<Object-id_str>")>-1){
						  nmrshiftdbnumber=Integer.parseInt(line0.substring(line0.lastIndexOf("<Object-id_str>")+15,line0.lastIndexOf("</Object-id_str>")));
					  }
					  if(!line0.trim().equals("") && line0.indexOf("<PC-CompoundType_id_cid>")>-1){
						  String cid=line0.substring(line0.lastIndexOf("<PC-CompoundType_id_cid>")+24,line0.lastIndexOf("</PC-CompoundType_id_cid>"));
						  //We get inchi for this compound from pubchem
					      URL url;
					      HttpURLConnection conn;
					      BufferedReader rd;
					      String line;
					      String inchi = "";
					      try {
					         url = new URL("http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+cid+"/property/InChI/TXT");
					         conn = (HttpURLConnection) url.openConnection();
					         conn.setRequestMethod("GET");
					         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					         while ((line = rd.readLine()) != null) {
					            inchi += line;
					         }
					         rd.close();
					      } catch (IOException e) {
					         e.printStackTrace();
					      } catch (Exception e) {
					         e.printStackTrace();
					      }
						  try{
							  DBMolecule mol=DBMoleculePeer.retrieveByPK(nmrshiftdbnumber);
							  if(mol.selectSpectra().size()==0)
								  throw new Exception ("No valid spectra");
							  alreadyinpubchem.put(new Integer(nmrshiftdbnumber),inchi);
						  }catch(Exception ex){
							  out.write(new String("\r\n-OEChem-11050415022D\r\n\r\n  0  0  0  0  0  0  0  0  0  0999 V2000\r\nM  END\r\n> <PUBCHEM_EXT_DATASOURCE_REGID>\r\n"+nmrshiftdbnumber+"\r\n\r\n> <PUBCHEM_REVOKE_SUBSTANCE>\r\nThis is no longer a valid nmrshfitdb id\r\n\r\n$$$$\r\n").getBytes());
						  }  	  			
					  }
				  }    
				  out.close();
				  List l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM where REVIEW_FLAG=\"true\";");
				  File outputFile = new File("/home/shk3/Desktop/NmrshiftdbForPubchem.sdf.zip");
				  FileOutputStream baos=new FileOutputStream(outputFile);
				  ZipOutputStream zipout=new ZipOutputStream(baos);
				  zipout.putNextEntry(new ZipEntry("nmrshiftdb.sdf"));
				  for(int i=0;i<l.size();i++){
					  try{
						  DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
						  System.err.println(mol.getMoleculeId()+"");
						  boolean isedited=false;
						  List spectra=mol.getDBSpectrums();
						  for(int k=0;k<spectra.size();k++){
							  System.err.println(spectra.size()+ " "+k);
							  DBSpectrum spectrum=(DBSpectrum)spectra.get(k);
							  List edits=spectrum.getDBUserDBSpectrumEdits();
							  for(int m=0;m<edits.size();m++){
								  if(((DBUserDBSpectrumEdit)edits.get(m)).getEdit().after(new Date(2007,1,1)))
									  isedited=true;
							  }
						  }
						  if(!alreadyinpubchem.keySet().contains(new Integer(mol.getMoleculeId())) || isedited || !mol.getInchi().equals(alreadyinpubchem.get(mol.getMoleculeId()))){
							  System.err.println(mol.getInchi()+" "+alreadyinpubchem.get(mol.getMoleculeId()));
							  HashMap map=new HashMap();
							  map.put("PUBCHEM_EXT_DATASOURCE_REGID",mol.getMoleculeId());
							  map.put("PUBCHEM_EXT_DATASOURCE_URL","http://www.nmrshiftdb.org");
							  map.put("PUBCHEM_EXT_SUBSTANCE_URL", "http://www.nmrshiftdb.org/portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/" + mol.getMoleculeId());
							  if(!mol.getCasNumber().equals(""))
								  map.put("PUBCHEM_GENERIC_REGISTRY_NAME",mol.getCasNumber()+"");
							  StringBuffer synonym=new StringBuffer();
							  List v=mol.getDBChemicalNames();
							  for(int k=0;k<v.size();k++){
								  synonym.append(((DBChemicalName)v.get(k)).getName()+"\r\n");
							  }
							  v=mol.getDBCanonicalNames();
							  for(int k=0;k<v.size();k++){
								  synonym.append(((DBCanonicalName)v.get(k)).getName()+"\r\n");
							  }
							  if(synonym.length()>4)
								  map.put("PUBCHEM_SUBSTANCE_SYNONYM",synonym.substring(0,synonym.length()-3).toString());
							  zipout.write(mol.getStructureFile(2,map,true).getBytes());
							  Set set = map.keySet();
							  Iterator iterator = set.iterator();
							  while (iterator.hasNext()) {
								  Object element = iterator.next();
								  zipout.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
							  }
							  zipout.write(new String("$$$$\r\n").getBytes());
						  }
					  }
					  catch(Exception ex){
						  ex.printStackTrace();
					  }
				  }
				  zipout.closeEntry();
				  zipout.close();
			  }catch(Exception ex){
				  ex.printStackTrace();
			  }*/
			  //reads pubchem ids, which come from a file from Gets SIDs in upload
			  FileInputStream fstream = new FileInputStream(args[0]+"/pubchem.csv");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  br.readLine();
			  String strLine;
			  while ((strLine = br.readLine()) != null)   {
				  String[] parts=strLine.split(",");
				  String sql = "insert into MOLECULE_HYPERLINK values("+parts[1]+",\"http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid="+parts[2]+"\",\"Deposition in PubChem\",\"\")";
				  System.out.println(sql);
				  DBMoleculePeer.executeStatement(sql);
			  }
			  in.close();

		  }

		  private static void executeReadJeanMarc(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
			    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
			    Properties nmrprops = new Properties();
			    Properties jetspeedprops = new Properties();
			    nmrprops.load(new FileInputStream(nmrpropsFile));
			    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
			    TurbineUser user = null;
			    Criteria crit = new Criteria();
			    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "mainz2");
			    List v = NmrshiftdbUserPeer.doSelect(crit);
			    //Look if the bulkoad user exists
			    if (v.size() > 0) {
			      user = (TurbineUser) v.get(0);
			    } else {
			      throw new TorqueException("no bulkload user");
			    }
			    IteratingSDFReader iter = new IteratingSDFReader(new FileReader("acd_lotusv7.sdf"), DefaultChemObjectBuilder.getInstance());
			  int i=0;
			  while(iter.hasNext()){
				  try{
					  IAtomContainer mol = (IAtomContainer)iter.next();
					  String shift = (String)mol.getProperty("CNMR_SHIFTS");
					  shift = shift.replace("\n", "").replace("\r", "");
					  System.out.println(shift);
				      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
			          subData.setMol(mol,false,false,false);
			          IAtomContainer molwithH=subData.getMolWithH();
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "13C");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			            
			            
			            
			            
		          subData.setSpectrumKeywords("acd_lotusv7");
		          //IBond[] bonds = subData.getMolWithH().getBonds();
		          boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
		          AllRingsFinder arf = new AllRingsFinder();
			        IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
		          Iterator<IBond> it=molwithH.bonds().iterator();
		          int c=0;
		          while(it.hasNext()) {
		        	IBond bond=it.next();
		            if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
		              isDoubleBondSpecified[c] = true;
		            }
		            c++;
		          }
		          subData.setDoubleBondConfiguration(true);
		          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
		          subData.setComment("ID: "+mol.getProperty("ID")+";  SMILES_ID: "+mol.getProperty("SMILES_ID")+"; multplicities generated from proton count");
		          StringTokenizer st=new StringTokenizer(mol.getProperty("SMILES_ID"), "_");
		          subData.setMoleculeLinks("https://www.wikidata.org/wiki/"+st.nextToken()+";wikidata");
		          st=new StringTokenizer(shift, ";");
		          while(st.hasMoreTokens()) {
		        	  String assignment=st.nextToken();
		            int atom=Integer.parseInt(assignment.substring(assignment.indexOf(":")+1, assignment.indexOf("|")).trim());
		            double value=Double.parseDouble(assignment.substring(assignment.indexOf("|")+1));
		            CMLPeakList signals=subData.getSignalstable();
		            boolean added=false;
		            for(int l=0;l<signals.getPeakElements().size();l++){
		              if(signals.getPeakElements().get(l).getXValue()==value){
		            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
		                added=true;
		              }
		            }
		            if(!added){
		            	CMLPeak peak = new CMLPeak();
		            	peak.setXValue(value);
		            	peak.setYValue(0);
		            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
		            	subData.addToSignalstable(peak);
		            }
		          }
		          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
		          subData.setSmilesNonChiralProv(ssb.string1);
		          subData.setIsChiralProv(ssb.bool);
		          if(ssb.bool){
		        	  subData.setSmilesChiralProv(ssb.string2);
		          }
		          subData.setCalculated();
		          crit = new Criteria();
		          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
		          v = DBSpectrumPeer.doSelect(crit);
		          if(v.size()==0){
		          	List<DBConditionType> conds = new ArrayList<>();
					subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
					crit=new Criteria();
					crit.add(DBSpectrumDBConditionPeer.SPECTRUM_ID,subData.getSpectrumid());
					DBSpectrumDBConditionPeer.doDelete(crit);
					DBCondition csearchcondition = (DBCondition) DBConditionPeer
							.getByValue("ACD/Labs C+H NMR Predictors and DB, 2020.1.0").get(0);
					DBSpectrumDBCondition spectrumcondition = new DBSpectrumDBCondition();
					spectrumcondition.setDBCondition(csearchcondition);
					spectrumcondition.setSpectrumId(subData.getSpectrumid());
					spectrumcondition.save();
		          }
		          }catch(Exception ex) {
					  ex.printStackTrace();
				  }
			  }
		  }

		  
		  private static void executeReadIlm31P(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
			    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
			    Properties nmrprops = new Properties();
			    Properties jetspeedprops = new Properties();
			    nmrprops.load(new FileInputStream(nmrpropsFile));
			    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
			    TurbineUser user = null;
			    Criteria crit = new Criteria();
			    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
			    List v = NmrshiftdbUserPeer.doSelect(crit);
			    //Look if the bulkoad user exists
			    if (v.size() > 0) {
			      user = (TurbineUser) v.get(0);
			    } else {
			      throw new TorqueException("no bulkload user");
			    }
			    IteratingSDFReader iter = new IteratingSDFReader(new FileReader("acd_lotusv7.sdf"), DefaultChemObjectBuilder.getInstance());
			  int i=0;
			  while(iter.hasNext()){
				  try{
					  IAtomContainer mol = (IAtomContainer)iter.next();
					  System.out.println(i);
				      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
			          subData.setMol(mol,false,false,false);
			          IAtomContainer molwithH=subData.getMolWithH();
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "31P");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
		          subData.setSpectrumKeywords("Ilm-NMR-P31");
		          boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
		          AllRingsFinder arf = new AllRingsFinder();
			        IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
		          Iterator<IBond> it=molwithH.bonds().iterator();
		          int c=0;
		          while(it.hasNext()) {
		        	IBond bond=it.next();
		            if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
		              isDoubleBondSpecified[c] = true;
		            }
		            c++;
		          }
		          subData.setDoubleBondConfiguration(true);
		          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
		          subData.setComment("ID: "+i);
		          String shift = ((String)mol.getProperty("NMREDATA_1D_31P")).trim();
		          double value=Double.parseDouble(shift);
		            int pnumber=-1;
		            for(IAtom atom:molwithH.atoms()) {
		            	if(atom.getSymbol().equals("P"))
		            		pnumber=molwithH.getAtomNumber(atom);
		            }
	            	CMLPeak peak = new CMLPeak();
	            	peak.setXValue(value);
	            	peak.setYValue(0);
	            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(pnumber)));
	            	subData.addToSignalstable(peak);
		          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
		          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
		          subData.setSmilesNonChiralProv(ssb.string1);
		          subData.setIsChiralProv(ssb.bool);
		          if(ssb.bool){
		        	  subData.setSmilesChiralProv(ssb.string2);
		          }
		          subData.setCalculated();
		          crit = new Criteria();
		          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
		          v = DBSpectrumPeer.doSelect(crit);
		          if(v.size()==0){
					subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
		          }
		          }catch(Exception ex) {
					  ex.printStackTrace();
				  }
			  }
		  }

		  
		  private static void executeReadMihaly(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
			    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
			    Properties nmrprops = new Properties();
			    Properties jetspeedprops = new Properties();
			    nmrprops.load(new FileInputStream(nmrpropsFile));
			    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
			    File[] files = new File(args[0]).listFiles();
			    TurbineUser user = null;
			    Criteria crit = new Criteria();
			    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
			    List v = NmrshiftdbUserPeer.doSelect(crit);
			    //Look if the bulkoad user exists
			    if (v.size() > 0) {
			      user = (TurbineUser) v.get(0);
			    } else {
			      throw new IOException("no bulkload user");
			    }
			    for (int i = 0; i < files.length; i++) {
			    	try{
			    		if(files[i].getName().endsWith(".mol2")) {
			    			Mol2Reader reader=new Mol2Reader(new FileInputStream(files[i]));
			    			IAtomContainer mol3d = reader.read(new AtomContainer());
			    			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                            sdg.setMolecule(mol3d);
                            sdg.generateCoordinates();
                            IAtomContainer mol = sdg.getMolecule();
                            ByteArrayOutputStream baos=new ByteArrayOutputStream();
                            MDLV2000Writer mdlWriter = new MDLV2000Writer(baos);
                            mdlWriter.write(mol);
                            mdlWriter.close();
					  System.out.println(files[i].getName());
				      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				      subData.setCalculated();
			          subData.setMol(new String(baos.toByteArray()),false,false,false);
			          IAtomContainer molwithH=subData.getMolWithH();
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "13C");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			          subData.setSpectrumKeywords("Rimpido");
			          boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
			          AllRingsFinder arf = new AllRingsFinder();
				        IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
			          Iterator<IBond> it=molwithH.bonds().iterator();
			          int c=0;
			          while(it.hasNext()) {
			        	IBond bond=it.next();
			            if (AtomUtils.isValidDoubleBondConfiguration(molwithH,bond,ringSet)) {
			              isDoubleBondSpecified[c] = true;
			            }
			            c++;
			          }
			          subData.setDoubleBondConfiguration(true);
			          subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
			          subData.setComment("ID: "+files[i].getName().substring(0, files[i].getName().indexOf(".")));
				      SubmittingData subDatah=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				      subDatah.setCalculated();
			          subDatah.setMol(mol,false,false,false);
			            crit = new Criteria();
			            crit.add(DBSpectrumTypePeer.NAME, "1H");
			            v = DBSpectrumTypePeer.doSelect(crit);
			            subDatah.setChoosenSpectrumType((DBSpectrumType) v.get(0));
			          subDatah.setSpectrumKeywords("Rimpido");
			          subDatah.setDoubleBondConfiguration(true);
			          subDatah.setIsDoubleBondSpecified(isDoubleBondSpecified);
			          subDatah.setComment("ID: "+files[i].getName().substring(0, files[i].getName().indexOf(".")));
			          File shiftfile=new File(files[i].getAbsolutePath().replaceFirst("mol2", "out"));
			          BufferedReader buffRead = new BufferedReader(new FileReader(shiftfile));
			          String line = "";
			          StringBuffer newMdl=new StringBuffer();
			          Vector<String> assignments=new Vector<String>();
			          while ((line = buffRead.readLine()) != null) {
			        	  if(line.matches(".*Atom.*Total sigma:.*")) {
					            int atom=Integer.parseInt(line.substring(9, 13).trim());
					            double value=Double.parseDouble(line.substring(line.lastIndexOf(" ")+1));
					            CMLPeakList signals=subData.getSignalstable();
					            if(line.contains("  H   "))
					            	signals=subData.getSignalstable();
					            if(line.contains("  C   ") || line.contains("  H   ")) {
						            boolean added=false;
						            for(int l=0;l<signals.getPeakElements().size();l++){
						              if(signals.getPeakElements().get(l).getXValue()==value){
						            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
						                added=true;
						              }
						            }
						            if(!added){
						            	CMLPeak peak = new CMLPeak();
						            	peak.setYValue(0);
						            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
						            	if(line.contains("  C   ")) {
							            	peak.setXValue(189.05-value);
						            		subData.addToSignalstable(peak);
						            	}else {
							            	peak.setXValue(29.68-value);
						            		subDatah.addToSignalstable(peak);
						            	}
						            }
					            }
			        	  }
			          }
			          StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
			          GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
			          subData.setSmilesNonChiralProv(ssb.string1);
			          subData.setIsChiralProv(ssb.bool);
			          if(ssb.bool){
			        	  subData.setSmilesChiralProv(ssb.string2);
			          }
			          subData.setCalculated();
			          crit = new Criteria();
			          crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
			          v = DBSpectrumPeer.doSelect(crit);
			          List<DBConditionType> conds = subData.getConditions();
			          for(DBConditionType cond : conds){
			        	if(cond.getConditionName().equals("Program"))
			        		cond.setCurrentValue("Quantum ESPRESSO");
			          }
			          subData.setConditions(conds);
			          subData.molcoord=mol3d;
			          subData.coordinatesetid3=7;
			          if(v.size()==0){
						subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
			          }
			          subDatah.setSmilesNonChiralProv(ssb.string1);
			          subDatah.setIsChiralProv(ssb.bool);
			          if(ssb.bool){
			        	  subDatah.setSmilesChiralProv(ssb.string2);
			          }
			          subDatah.setCalculated();
			          subDatah.setConditions(conds);
			          subDatah.molcoord=mol3d;
			          subDatah.coordinatesetid3=7;
			          if(v.size()==0){
						subDatah.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
			          }
			    	}
		          }catch(Exception ex) {
					  ex.printStackTrace();
				  }
			  }
		  }

		  
		  private static void executeReadPubchem(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  IteratingSDFReader iter = new IteratingSDFReader(new FileReader("/root/6356563125250031038.sdf"), DefaultChemObjectBuilder.getInstance());
			  int i=0;
			  while(iter.hasNext()){
				  try{
					  IAtomContainer mol = (IAtomContainer)iter.next();
					  String inchi = "InChI=1"+((String)mol.getProperty("PUBCHEM_IUPAC_INCHI")).substring(8);
					  String name =(String)mol.getProperty("PUBCHEM_IUPAC_NAME");
					  String compoundid =(String)mol.getProperty("PUBCHEM_COMPOUND_CID");
					  System.out.println((i++)+" "+compoundid);
					  String sql="select MOLECULE_ID from CANONICAL_NAME where NAME='"+inchi+"'";
					  int nmrid=((Record)DBCanonicalNamePeer.executeQuery(sql).get(0)).getValue(1).asInt();
					  sql ="select count(*) from CHEMICAL_NAME where MOLECULE_ID="+nmrid;
					  int countnames=((Record)DBCanonicalNamePeer.executeQuery(sql).get(0)).getValue(1).asInt();
					  if(countnames==0){
						  sql="insert into CHEMICAL_NAME values("+nmrid+",'"+name+"','"+Soundex.soundex(name,"9", new StringAndInt())+"')";
						  DBChemicalNamePeer.executeStatement(sql);
					  }
					  sql="select count(*) from MOLECULE_HYPERLINK where MOLECULE_ID="+nmrid+" and DESCRIPTION='Compound in PubChem'";
					  int countlinks=((Record)DBCanonicalNamePeer.executeQuery(sql).get(0)).getValue(1).asInt();
					  if(countlinks==0){
						  sql="insert into MOLECULE_HYPERLINK values("+nmrid+",'http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid="+compoundid+"','Compound in PubChem','251535125')";
						  DBChemicalNamePeer.executeStatement(sql);
					  }
				  }catch(Exception ex){
					  ex.printStackTrace();
				  }
			  }
		  }
		  
		  private static void executeMakeHose(String[] args) throws TorqueException, SQLException{
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();
		      Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE");
			  while (rs.next()) {
				  try {
					  DBMolecule dbmol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  System.err.println("Making hose codes for "+dbmol.getMoleculeId());
					  HOSECodeGenerator hcg=new HOSECodeGenerator();
					  ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
					  IAtomContainer molWithH = dbmol.getAsCDKMolecule(1);
					  AtomUtils.makeUpDownBonds(molWithH);
					  //IMolecule molWithHCount = dbmol.getAsCDKMoleculeWithoutH();
					  DBAtom[] dbatoms=dbmol.getAtomsAsArrayInMdlOrder();
					  Iterator it=molWithH.atoms().iterator();
					  int i=0;
					  while(it.hasNext()){
						  IAtom atom=(IAtom)it.next();
					      if (GeneralUtils.getAtomsWithHoseCode().contains(atom.getSymbol())) {
					          /*int l = i;
					          for (int k = 0; k < i; k++) {
					            if (molWithH.getAtom(k).getSymbol().equals("H")) {
					              l--;
					            }
					          }*/
					          String hoseCode = hcg.getHOSECode(molWithH, atom, 6,false);
							  dbatoms[i].setHoseCode(hoseCode.getBytes());
							  try{
								  dbatoms[i].setHoseCodeWithRings(ehcg.getHOSECode(molWithH, atom, 6,true).getBytes());
							  }catch(Exception ex){
								  dbatoms[i].setHoseCodeWithRings(hcg.getHOSECode(molWithH, atom, 6,true).getBytes());
								  System.out.println("Fehler");
							  }
							  //System.out.println(dbatoms[i].getHoseCodeWithRings());
							  dbatoms[i].save();
					        }
					      i++;
					  }
				  } catch (Exception ex) {
					  ex.printStackTrace();
				  }
			  }
		  }
		  
		  
		  private static void executeMakeChiuzDumps(String[] args) throws TorqueException, IOException, SQLException, CloneNotSupportedException, CDKException, DataSetException, JCAMPException, NmrshiftdbException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		      List<Record> l = DBMoleculePeer.executeQuery("select distinct(MOLECULE_ID) from SPECTRUM join SPECTRUM_KEYWORD using (SPECTRUM_ID) where KEYWORD_ID =101");
          	  FileOutputStream fosall=new FileOutputStream(new File("chiuz.nmredata.zip"));
          	  ZipOutputStream zipout=new ZipOutputStream(fosall);
          	  NmredataRecordWriter writer=new NmredataRecordWriter(zipout);
          	  for(int k=0;k<l.size();k++){
		    	  try {
		    	  		DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(k)).getValue(1).asString()));
			            System.out.println(mol.getMoleculeId());
			            zipout.putNextEntry(new ZipEntry(mol.getMoleculeId()+""));
			            List<File> files=new ArrayList<File>();
		            	int i=10;
		            	List<String> locations=new ArrayList<String>();
		            	DBSpectrum spectrum=null;
		                for(DBSpectrum spectrumlocal : mol.selectSpectra()){
		                	if(spectrumlocal.getRawData()!=null && spectrumlocal.getKeywords().contains("ChiuZ")){
		                		File file=new File(System.getProperty("java.io.tmpdir")+File.separator+i+".zip");
		                		FileOutputStream fos=new FileOutputStream(file);
		                		fos.write(spectrumlocal.getRawDataDecoded());
		                		fos.close();
		                		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir")+File.separator+i));
		                		ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
		                		ZipEntry zipEntry = zis.getNextEntry();
		                		byte[] buffer = new byte[1024];
		                		while(zipEntry != null){
		                			String fileName = zipEntry.getName();
		                			File newFile = new File(System.getProperty("java.io.tmpdir")+File.separator+i+"/" + fileName);
		                			if(zipEntry.isDirectory()){
		                				newFile.mkdirs();
		                			}else{
		                			newFile.getParentFile().mkdirs();
		                			FileOutputStream fos2 = new FileOutputStream(newFile);
		                			int len;
		                			while ((len = zis.read(buffer)) > 0) {
		                				fos2.write(buffer, 0, len);
		                			}
		                			fos2.close();
		                			}
		                			zipEntry = zis.getNextEntry();
		                		}
		                		zis.closeEntry();
		                		zis.close();
		                		files.add(new File(System.getProperty("java.io.tmpdir")+File.separator+i));
		                		locations.add("file:./"+mol.getMoleculeId()+"/"+i);
		                		i++;
		                		spectrum=spectrumlocal;
		                	}
		                }
		            	NmreDataRecord record=new NmreDataRecord(files, mol.getNmreData(spectrum, null, locations, true, false, URL));
		            	writer.write(record, spectrum.getDBMolecule().getMoleculeId()+"", NmredataVersion.ONEPOINTONE);
		    	  }catch(Exception ex) {
		    		  ex.printStackTrace();
		    	  }
			  }
		      zipout.close();
		  }
		  
		  private static void executeMakeDumps(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		        List<Record> l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM_FILE join SPECTRUM using (SPECTRUM_ID) left join SPECTRUM_LITERATURE using (SPECTRUM_ID) left join USER_SPECTRUM_MARK using (SPECTRUM_ID) where TYPE=\"rawdata\" and REVIEW_FLAG=\"true\" and (USER_SPECTRUM_MARK.MARK>=5 or MARK is null) order by MOLECULE_ID;");
		        File outputFile = new File("nmrshiftdb2.sd");
		        File outputFileCml = new File("nmrshiftdb2.xml");
		        File outputFileWithSignals = new File("nmrshiftdb2withsignals.sd");
		        File outputFileNmredata = new File("nmrshiftdb2.nmredata.sd");
		        FileOutputStream baos=new FileOutputStream(outputFile);
		        FileOutputStream baossig=new FileOutputStream(outputFileWithSignals);
		        FileOutputStream baoscml=new FileOutputStream(outputFileCml);
		        FileOutputStream baosnmredata=new FileOutputStream(outputFileNmredata);
		        Writer cmlout = new OutputStreamWriter(baoscml, "UTF8");
		        File outputFile3d = new File("nmrshiftdb2_3d.sd");
		        File outputFileCml3d = new File("nmrshiftdb2_3d.xml");
		        File outputFileWithSignals3d = new File("nmrshiftdb2withsignals_3d.sd");
		        FileOutputStream baos3d=new FileOutputStream(outputFile3d);
		        FileOutputStream baossig3d=new FileOutputStream(outputFileWithSignals3d);
		        FileOutputStream baoscml3d=new FileOutputStream(outputFileCml3d);
		        Writer cmlout3d = new OutputStreamWriter(baoscml3d, "UTF8");
		        cmlout.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<cml xmlns=\"http://www.xml-cml.org/schema\">"));
		        cmlout3d.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<cml xmlns=\"http://www.xml-cml.org/schema\">"));
		        List<DBConditionType> conditiontypes=DBConditionTypePeer.doSelect(new Criteria());
		        Connection dbconn = Torque.getConnection();
		        Statement stmt = dbconn.createStatement();
		        for(int i=0;i<l.size();i++){
		         try{
		            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
		            System.out.println(mol.getMoleculeId());
		            Map map=new HashMap();
		            map.put("nmrshiftdb2 ID", mol.getMoleculeId());
		            byte[] structurefile = mol.getStructureFile(1,map,true).getBytes();
		            baos.write(structurefile);
					  Set set = map.keySet();
					  Iterator iterator = set.iterator();
					  while (iterator.hasNext()) {
						  Object element = iterator.next();
						  baos.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
					  }
		            baos.write(new String("$$$$\r\n").getBytes());
		            if(mol.hasCoordinates(2)){
		            	byte[] structurefile3d = mol.getStructureFile(2,map,true).getBytes();
			            baos3d.write(structurefile3d);
						  set = map.keySet();
						  iterator = set.iterator();
						  while (iterator.hasNext()) {
							  Object element = iterator.next();
							  baos3d.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
						  }
			            baos3d.write(new String("$$$$\r\n").getBytes());
		            }
		            List<DBSpectrum> spectra=mol.selectSpectra();
		            Iterator<DBSpectrum> it = spectra.iterator();
		            while (it.hasNext()) {
		                DBSpectrum o= (DBSpectrum)it.next();
		                if(o.getMark()<5)
		                    it.remove();
		            }
		            for(int m=0;m<conditiontypes.size();m++){
		              int ctid=((DBConditionType)conditiontypes.get(m)).getConditionTypeId();
		              String ctname=((DBConditionType)conditiontypes.get(m)).getConditionName();
		              String sql="select VALUE, SPECTRUM_ID from SPECTRUM join SPECTRUM_CONDITION using (SPECTRUM_ID) join `NMR_CONDITION` using (CONDITION_ID)  where MOLECULE_ID ="+mol.getMoleculeId()+" and CONDITION_TYPE_ID="+ctid+" order by SPECTRUM.SPECTRUM_TYPE_ID";
		              ResultSet rs=stmt.executeQuery(sql);
		              StringBuffer valuesline=new StringBuffer();
		              while(rs.next()){
		            	  if(getSpectrumNumber(spectra, rs.getInt(2))>-1)
		            		  valuesline.append(getSpectrumNumber(spectra, rs.getInt(2))+":"+rs.getString(1)+" ");
		              }
		              if(!valuesline.toString().equals(""))
		                map.put(ctname,valuesline.toString());
		            }
		            StringTokenizer st=new StringTokenizer(mol.getCML(1,true).getChild(0).toXML(),"<");
		            while(st.hasMoreTokens())
	            		cmlout.write(new String("\r\n<"+st.nextToken()));
		            for(int k=0;k<spectra.size();k++){
		              DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
		              if(spectrum.getMark()>spectrum.getDisplaymark()){
			              st=new StringTokenizer(spectrum.getCmlSpect(false, URL).getChild(0).toXML(),"<");
			              while(st.hasMoreTokens())
			            	  cmlout.write(new String("\r\n<"+st.nextToken()));
			              map.put("Spectrum "+spectrum.getDBSpectrumType().getName()+" "+k,getSpecfile2(spectrum));
			              if(spectrum.getRawData()!=null) {
			            	  map.put("rawdata "+spectrum.getDBSpectrumType().getName()+" "+k,spectrum.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, URL));
			              }
		              }
		            }
		            baossig.write(structurefile);
					  set = map.keySet();
					  iterator = set.iterator();
					  while (iterator.hasNext()) {
						  Object element = iterator.next();
						  baossig.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
					}
		            baossig.write(new String("$$$$\r\n").getBytes());
		            Set<String> solventtemps=new HashSet<>();
            		for(DBSpectrum spectrum : spectra) {
            			if(solventtemps.add(spectrum.getSolvent()+spectrum.getTemperature()) && spectrum.getRawData()!=null) {
	            			NmreData data = spectrum.getDBMolecule().getNmreData(spectrum, null, null, false, true, URL);
	            			Map<Object,Object> props=new HashMap<Object,Object>();
	            			if(spectrum.getLiteraturesNew().size()>0) {
	            				
	            			}
			            	ByteArrayOutputStream baoslocal=new ByteArrayOutputStream();
		                	NmredataWriter writer=new NmredataWriter(baoslocal);
		                    writer.write(data, NmredataVersion.ONEPOINTONE,props);
		                    writer.close();
		                    baosnmredata.write(baoslocal.toByteArray());
            			}
		            }
		            if(mol.hasCoordinates(2)){
		            	byte[] structurefile3d = mol.getStructureFile(2,map,true).getBytes();
			            st=new StringTokenizer(mol.getCML(2,true).getChild(0).toXML(),"<");
			            while(st.hasMoreTokens()){
				            while(st.hasMoreTokens())
			            		cmlout3d.write(new String("\r\n<"+st.nextToken()));
			            }
			            for(int k=0;k<spectra.size();k++){
			              DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
			              if(spectrum.getMark()>spectrum.getDisplaymark()){
				              st=new StringTokenizer(spectrum.getCmlSpect(false, URL).getChild(0).toXML(),"<");
				              while(st.hasMoreTokens())
				            	  cmlout3d.write(new String("\r\n<"+st.nextToken()));
				              map.put("Spectrum "+spectrum.getDBSpectrumType().getName()+" "+k,getSpecfile2(spectrum));
			              }
			            }
			            baossig3d.write(structurefile3d);
						  set = map.keySet();
						  iterator = set.iterator();
						  while (iterator.hasNext()) {
							  Object element = iterator.next();
							  baossig3d.write(new String("> <"+(String)element+">\r\n"+map.get(element)+"\r\n\r\n").getBytes());
						  }
			            baossig3d.write(new String("$$$$\r\n").getBytes());
		            }
		          }
		          catch(Exception ex){
		            GeneralUtils.logError(ex,"loop in buildsdf",null,false);
		          }
		        }
		        cmlout.write(new String("\r\n</cml>"));
		        baos.close();
		        baos3d.close();
		        baossig.close();
		        baossig3d.close();
		        cmlout.close();
		        cmlout3d.close();
		        baosnmredata.close();
		        //write the hose code files
                baos=new FileOutputStream(new File("hosecodesc.txt"));
                FileOutputStream baos2=new FileOutputStream(new File("hosecodesh.txt"));
                int i=0;
                Connection conn=Torque.getConnection();
                Map<String, Map<String, Map<String,ArrayList<Double>>>> mapsmap = new HashMap<String,Map<String,Map<String,ArrayList<Double>>>>();
                while(true){
                    ResultSet rs=stmt.executeQuery("select HOSE_CODE, HOSE_CODES.VALUE, SYMBOL, NMR_CONDITION.VALUE from HOSE_CODES join NMR_CONDITION using (CONDITION_ID) where CONDITION_TYPE='m' limit "+(i* 1000)+", 1000");
                    int m=0;
                    while(rs.next()){
                        String code = rs.getString(1);
                        Double value = new Double(rs.getString(2));
                        String symbol = rs.getString(3);
                        if (mapsmap.get(symbol) == null) {
                            mapsmap.put(symbol, new HashMap<String,Map<String,ArrayList<Double>>>());
                        }
                        String solvent = rs.getString(4);
                        if(mapsmap.get(symbol).get(solvent) == null) {
                        	mapsmap.get(symbol).put(solvent, new HashMap<String,ArrayList<Double>>());
                        }
                        for (int spheres = 6; spheres > 0; spheres--) {
                            StringBuffer hoseCodeBuffer = new StringBuffer();
                            StringTokenizer st = new StringTokenizer(code, "()/");
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
                            String hoseCode = hoseCodeBuffer.toString();
                            if (mapsmap.get(symbol).get(solvent).get(hoseCode) == null) {
                                mapsmap.get(symbol).get(solvent).put(hoseCode, new ArrayList<Double>());
                            }
                            mapsmap.get(symbol).get(solvent).get(hoseCode).add(value);
                        }
                        m++;
                    }
                    i++;
                    if(m==0)
                        break;
                }
                Torque.closeConnection(conn);
                Set<String> keySet=mapsmap.keySet();
                Iterator<String> it=keySet.iterator();
                while(it.hasNext()){
                    String symbol=(String)it.next();
                    Set<String> keySetSolvents=mapsmap.get(symbol).keySet();
                    Iterator<String> itsolvents=keySetSolvents.iterator();
                    while(itsolvents.hasNext()){
                        String solvent=(String)itsolvents.next();
                        Map<String,ArrayList<Double>> hosemap= mapsmap.get(symbol).get(solvent);
                        Set<String> keySet2=hosemap.keySet();
                        Iterator<String> it2=keySet2.iterator();
                        while(it2.hasNext()){
                        	String hoseCode=it2.next();
                        	ArrayList<Double> list=hosemap.get(hoseCode);
                        	double[] values = new double[list.size()];
                        	for (int k = 0; k < list.size(); k++) {
                        		values[k] = list.get(k).doubleValue();
                        	}
                        	if(symbol.equals("H"))
                        		baos2.write(new String(symbol+"_"+solvent+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());
                        	else if(symbol.equals("C"))
                       		baos.write(new String(symbol+"_"+solvent+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());
                        }
                    }
                }
                baos.close();
                baos2.close();
		  }
		  
		  private static void executeMakeDumpcouplings(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		        List<Record> l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM left join USER_SPECTRUM_MARK using (SPECTRUM_ID) where REVIEW_FLAG=\"true\" and (USER_SPECTRUM_MARK.MARK>=5 or MARK is null) order by MOLECULE_ID;");
		        Connection dbconn = Torque.getConnection();
		        Statement stmt = dbconn.createStatement();
		        for(int i=0;i<l.size();i++){
		         try{
		            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
		            System.out.println(mol.getMoleculeId());
		            List spectra=mol.selectSpectra();
		            for(int k=0;k<spectra.size();k++){
		              DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
		              System.out.println(spectrum.getSpectrumId());
		              if(spectrum.getDBCouplings().size()>0 && spectrum.getSpectrumTypeId()==2) {
		            	File outputFileCml = new File(spectrum.getSpectrumId()+".xml");
		  		        FileOutputStream baoscml=new FileOutputStream(outputFileCml);
		  		        Writer cmlout = new OutputStreamWriter(baoscml, "UTF8");
		  		        cmlout.write(new String("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<spin_system>\n"));
		  		        int m=1;
		  		        Map<Integer, Integer> atomtoid=new HashMap<Integer,Integer>();
		  		        for(DBSignal signal : spectrum.getDBSignals()) {
		  		        	cmlout.write("  <spin id=\""+m+"\" isotope=\"1H\" >\n");
		  		        	cmlout.write("    <coordinates x=\""+signal.getDBAtom(1, 0).getCoordinates(1)[0]+"\" y=\""+signal.getDBAtom(1, 0).getCoordinates(1)[1]+"\" z=\"0.0\" />\n");
		  		        	cmlout.write("  </spin>\n");
		  		        	atomtoid.put(signal.getDBAtom(1, 0).getMdlNumber(), m);
		  		        	System.out.println(signal.getDBAtom(1, 0).getMdlNumber()+"  "+ m);
		  		        	m++;
		  		        }
		  		        m=1;
		  		        for(DBSignal signal : spectrum.getDBSignals()) {
		  		        	cmlout.write("  <interaction kind=\"shielding\" id=\""+m+"\" units=\"ppm\" spin_a=\""+m+"\" reference=\"\">\n" + 
		  		        			"    <tensor xx=\""+(31.296-signal.getFirstShift().getValue())+"\" xy=\"0\" xz=\"0\"\n" + 
		  		        			"            yx=\"0\" yy=\""+(31.296-signal.getFirstShift().getValue())+"\" yz=\"0\"\n" + 
		  		        			"            zx=\"0\" zy=\"0\" zz=\""+(31.296-signal.getFirstShift().getValue())+"\" />\n" + 
		  		        			"  </interaction>\n");
		  		        	m++;
		  		        }
		  		        for(DBCoupling coupling : spectrum.getDBCouplings()) {
		  		        	cmlout.write("  <interaction kind=\"jcoupling\" id=\""+m+"\" units=\"Hz\" spin_a=\""+atomtoid.get(coupling.getDBAtomRelatedByAtomId1().getMdlNumber())+"\" spin_b=\""+atomtoid.get(coupling.getDBAtomRelatedByAtomId2().getMdlNumber())+"\" >\n" + 
		  		        			"    <scalar iso=\""+coupling.getConstant()+"\" />\n" + 
		  		        			"  </interaction>\n");
		  		        	System.out.println(coupling.getDBAtomRelatedByAtomId1().getMdlNumber()+"_"+coupling.getDBAtomRelatedByAtomId2().getMdlNumber());
		  		        	m++;
		  		        }
		  		        cmlout.write("</spin_system>\n");
		  		        cmlout.close();
		              }
		            }
		          }
		          catch(Exception ex){
		            GeneralUtils.logError(ex,"loop in buildsdf",null,false);
		          }
		        }
		  }

		  private static int getSpectrumNumber(List<DBSpectrum> spectra, int id){
			  for(int i=0;i<spectra.size();i++){
				  if(spectra.get(i).getSpectrumId()==id)
					  return i;
			  }
			  return -1;
		  }

		  /**
		   *  Analyzes the current sql logfile
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeAnalyzeSql(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
			  String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
			  Properties nmrprops = new Properties();
			  Properties jetspeedprops = new Properties();
			  nmrprops.load(new FileInputStream(nmrpropsFile));
			  jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
			  File sqlfile=new File("/root/sql.log");
			  BufferedReader sqllog=new BufferedReader(new InputStreamReader(new FileInputStream(sqlfile)));
			  String line="";
			  String sql=null;
			  String[] months={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			  int fehler=0;
			  int lines=0;
			  Map<String, Integer> hosts = new HashMap<String,Integer>();
			  StringBuffer entry=new StringBuffer();
			  while((line=sqllog.readLine())!=null){
				  try{
					  if(line.indexOf("INFO")>-1 && !entry.toString().equals("")){
						  System.err.println(line);
						  StringTokenizer st=new StringTokenizer(entry.toString(),"-");
						  st.nextToken();
							st.nextToken();
							st.nextToken();
							st.nextToken();
						  String date=st.nextToken().trim();
						  StringTokenizer st2=new StringTokenizer(date);
						  st2.nextToken();
						  String month=st2.nextToken();
						  int monthint=0;
						  for(int k=0;k<12;k++){
							  if(months[k].equals(month))
								  monthint=k+1;
						  }
						  st2.nextToken();
						  st2.nextToken();
						  st2.nextToken();
						  String year=st2.nextToken();
						  st.nextToken();
						  String type=st.nextToken();
						  String query=st.nextToken();
						  String ip=st.nextToken().trim();
						  InetAddress adress = InetAddress.getByName(ip);
						  String hostname = adress.getCanonicalHostName();
						  if(!hostname.equals("")){
							  if(hosts.containsKey(hostname))
								  hosts.put(hostname,hosts.get(hostname)+1);
							  else
								  hosts.put(hostname, 1);
						  }
						  if(type.indexOf("Prediction")!=-1){
							  sql="update STATISTICS set PREDICTION=PREDICTION+1";
                          }else if(type.indexOf("QuickCheck")!=-1){
                              sql="update STATISTICS set QUICK_CHECK=QUICK_CHECK+1";
						  }else if(type.indexOf("Search")!=-1){
							  if(query.indexOf("MOLECULE.SMILES_STRING")!=-1){
								  sql="update STATISTICS set EXACT_SUBSTRUCTURE=EXACT_SUBSTRUCTURE+1";
							  }else if(query.indexOf("spectrumsimilarity")!=-1){
								  sql="update STATISTICS set LINE_SEARCH=LINE_SEARCH+1";
							  }else if(query.indexOf("fingerprint_compare")!=-1){
								  sql="update STATISTICS set SUBSTRUCTURE_FUZZY_SEARCH=SUBSTRUCTURE_FUZZY_SEARCH+1";
							  }else if(query.indexOf("`NMR_CONDITION`.VALUE=")!=-1){
								  sql="update STATISTICS set WEIGHT_SEARCH=WEIGHT_SEARCH+1";
							  }else if(query.indexOf("CAS_NUMBER=")!=-1){
								  sql="update STATISTICS set CAS=CAS+1";
							  }else if(query.indexOf("CHEMICAL_NAME.NAME=")!=-1){
								  sql="update STATISTICS set NAME=NAME+1";
							  }else{
								  sql="update STATISTICS set OTHER_SEARCH=OTHER_SEARCH+1";
							  }
						  }
						  ResultSet rs=stmt.executeQuery("select * from STATISTICS where YEAR="+year+" and MONTH="+monthint);
						  if(!rs.next()){
							  stmt.executeUpdate("insert into STATISTICS values("+year+","+monthint+",0,0,0,0,0,0,0,0)");
						  }  
						  if(sql!=null)
							  stmt.executeUpdate(sql+" where YEAR="+year+" and MONTH="+monthint);
						  sql=null;
						  entry=new StringBuffer();
					  }
					  entry.append(line+"\r\n");
					  lines++;
				  }catch(Exception ex){
                      sql=null;
					  entry=new StringBuffer();
					  ex.printStackTrace();
					  fehler++;
				  }
			  }
			  System.err.println(lines+" "+fehler);
			  for(String hostname : hosts.keySet()){
				  System.out.println(hostname+" "+hosts.get(hostname));
			  }
			  //sqlfile.renameTo(new File("/root/sql.log"+formatter.format(new Date())));
			  //sqlfile=new File("/root/sql.log");
			  sqlfile.createNewFile();
		  }

		  private static void crossvalidateHose(String[] args){
			  try{
				  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
				  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
				  config.initialize();
				  Connection dbconn = Torque.getConnection();
				  DBSpectrumType spectype=DBSpectrumTypePeer.retrieveByPK(1);
				  List<DBMolecule> mols=DBMoleculePeer.doSelect(new Criteria());				  
                  int count=0;
                  float error3d=0;
                  float error2d=0;
                  for(DBMolecule mol : mols) {
                          System.out.println(mol.getMoleculeId());
                          int i=0;
                          for(DBAtom atom : mol.getDBAtoms()) {
                                  for(DBSignal signal : atom.getDBSignalsBySpectrumType(spectype.getName())) {
                                	  try{
                                          StringBuffer hose=new StringBuffer();
                                          double[] result = PredictionTool.generalPredict(mol.getAsCDKMolecule(1), mol.getAsCDKMolecule(1).getAtom(i), true, false, true, true, signal.getSpectrumId(), -1, new StringBuffer(), false, false, null,  null, -1, true, hose, 6, true, true, null);
                                          //System.out.println(result[4]);
                                          if(result[4]>3){
                                                  double[] result2 = PredictionTool.generalPredict(mol.getAsCDKMolecule(1), mol.getAsCDKMolecule(1).getAtom(i), true, false, false, false, signal.getSpectrumId(), (int)result[4], new StringBuffer(), false, false, null,  null, (int)result[4], true, new StringBuffer(), 6, true, true, null);
                                                  if(result[1]!=result2[1] && (hose.indexOf("@")>-1 || hose.indexOf("|")>-1 || hose.indexOf("\\")>-1)){
                                                	  count++;
                                                	  error3d+=Math.abs(signal.getFirstShift().getValue()-result[1]);
                                                	  error2d+=Math.abs(signal.getFirstShift().getValue()-result2[1]);
                                                  }
                                                  System.out.println(count+" "+signal.getFirstShift().getValue()+" "+result[1]+"  "+result2[1]+"  "+hose+" "+error3d+" "+error2d);
                                          }
                                	  }catch(Exception ex){
                                		  //ex.printStackTrace();
                                	  }
                                  }
                                  i++;
                          }
                  	}
			  }catch(Exception ex){
				  ex.printStackTrace();
			  }
		  }


		  private static void crossvalidateHoseOld(String[] args){
			  try{
				  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
				  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
				  config.initialize();
				  Connection dbconn = Torque.getConnection();
				  Statement stmt = dbconn.createStatement();				  
				  DBSpectrumType spectype=DBSpectrumTypePeer.retrieveByPK(1);
				  double[] errors=new double[100];
                  int[] hits=new int[100];
                  int[] errorrange=new int[3];
                  for(int i=0;i<100;i++){
                          ResultSet rs=stmt.executeQuery("select count(*) from SPECTRUM where SPECTRUM_TYPE_ID="+spectype.getSpectrumTypeId()+" and REVIEW_FLAG='true'");
                          rs.next();
                          int numofmols=rs.getInt(1);
                          rs=stmt.executeQuery("select SPECTRUM_ID, MOLECULE_ID from SPECTRUM where SPECTRUM_TYPE_ID="+spectype.getSpectrumTypeId()+" and REVIEW_FLAG='true' order by MOLECULE_ID");
                          for(int k=0;k<i;k++){
                                  for(int l=0;l<(int)numofmols/100;l++){
                                          rs.next();
                                  }
                          }
                          rs.next();
                          int startmol=rs.getInt(2);
                          Vector topredict=new Vector();
                          for(int k=0;k<(int)numofmols/100;k++){
                                  topredict.add(DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(2))));
                                  if(rs.isAfterLast()){
                                          rs.last();
                                          break;
                                  }
                                  rs.next();
                          }
                          if(rs.isAfterLast()){
                                  rs.last();
                          }
                          int endmol=rs.getInt(2);
                          System.err.println(i+"  "+numofmols+"  "+startmol+"  "+endmol);
                          HashMap<String, Map<String,Object>> mapsmap = new HashMap<String,Map<String,Object>>();
                          rs=stmt.executeQuery("select HOSE_CODE, VALUE, SYMBOL, MOLECULE_ID from HOSE_CODES where CONDITION_TYPE='m' and SYMBOL='"+spectype.getDBIsotope(1).getElementSymbol()+"' and (MOLECULE_ID<"+startmol+" or MOLECULE_ID>"+endmol+")");
                          while(rs.next()){
                              String code = rs.getString(1);
                              Double value = new Double(rs.getString(2));
                              String symbol = rs.getString(3);
                              if (mapsmap.get(symbol) == null) {
                                  mapsmap.put(symbol, new HashMap<String,Object>());
                              }
                              for (int spheres = 6; spheres > 0; spheres--) {
                                  StringBuffer hoseCodeBuffer = new StringBuffer();
                                  StringTokenizer st = new StringTokenizer(code, "()/");
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
                                  String hoseCode = hoseCodeBuffer.toString();
                                  if ((mapsmap.get(symbol)).get(hoseCode) == null) {
                                      (mapsmap.get(symbol)).put(hoseCode, new ArrayList<Double>());
                                  }
                                  ((ArrayList<Double>)mapsmap.get(symbol).get(hoseCode)).add(value);
                              }
                          }
                          rs.close();
                          rs=null;
                          Set<String> keySet=mapsmap.keySet();
                          Iterator<String> it=keySet.iterator();
                          while(it.hasNext()){
                              String symbol=(String)it.next();
                              Map<String,Object> hosemap= mapsmap.get(symbol);
                              Set<String> keySet2=new HashSet<String>();
                              keySet2.addAll(hosemap.keySet());
                              Iterator<String> it2=keySet2.iterator();
                              while(it2.hasNext()){
                                  String hoseCode=it2.next();
                                  ArrayList<Double> list=(ArrayList<Double>)hosemap.get(hoseCode);
                                  double[] values = new double[list.size()];
                                  for (int k = 0; k < list.size(); k++) {
                                      values[k] = list.get(k).doubleValue();
                                  }
                                  mapsmap.get(symbol).put(hoseCode, new PredictionTool.ValueBean((float)Statistics.minimum(values),(float)Statistics.average(values),(float)Statistics.maximum(values), values.length));
                              }
                          }
                          PredictionTool.mapsmap=mapsmap;
                          int m=0;
                          for(int k=0;k<topredict.size();k++){
                            DBMolecule mol=(DBMolecule)topredict.get(k);
                            if(m%100==0)
                                System.out.println(mol.getMoleculeId());
                            try{
                                IAtomContainer cdkmol=mol.getAsCDKMolecule(2);
                                for(int l=0;l<cdkmol.getAtomCount();l++){
                                    if(cdkmol.getAtom(l).getSymbol().equals("C") && mol.getAtomsAsArrayInMdlOrder()[l].getDBSignalsBySpectrumType("13C").size()>0){// && ((IAtom)cdkmol.getConnectedAtomsList(cdkmol.getAtom(l)).get(0)).getSymbol().equals("C")){
                                        try{
                                            double[] results=PredictionTool.generalPredict(cdkmol, cdkmol.getAtom(l), true, true, true, false, startmol, endmol, new StringBuffer(""), false, false, null, new HashMap(), -1, true, new StringBuffer(), 6, false, true, null);
                                                double predicted=results[1];
                                                System.err.println("HOSE "+predicted+" "+results[4]);
                                                double real=((DBSignal)mol.getAtomsAsArrayInMdlOrder()[l].getDBSignalsBySpectrumType("13C").get(0)).getFirstShift().getValue();
                                                if(Math.abs(predicted-real)>10)
                                                        errorrange[0]++;
                                                else if(Math.abs(predicted-real)>5)
                                                        errorrange[1]++;
                                                else
                                                        errorrange[2]++;
                                                m++;
                                        }catch(Exception ex){ex.printStackTrace();}
                                    }
                                }
                            }catch(Exception ex){
                                    System.out.println("error "+mol.getMoleculeId());
                                    ex.printStackTrace();
                            }
                          }
                          errors[i]=errors[i]/m;
                          System.err.println("m "+m+" "+errors[i]);
                          System.err.println(errorrange[0]+" "+errorrange[1]+" "+errorrange[2]);
                          mapsmap=null;
                          PredictionTool.mapsmap=null;
                        }
                        System.err.println(Statistics.average(errors));
                        System.err.println(hits[0]+"  "+hits[1]+"  "+hits[2]+"  "+hits[3]+"  "+hits[4]+"  "+hits[5]+"  "+hits[6]+"  "+hits[7]+"  "+hits[8]+"  "+hits[9]);
                        System.err.println(errorrange[0]+" "+errorrange[1]+" "+errorrange[2]);
                }catch(Exception ex){
				  ex.printStackTrace();
			  }
		  }

		  private static void writeThomsonreuters(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
	            List l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM join SPECTRUM_LITERATURE using (SPECTRUM_ID) where REVIEW_FLAG=\"true\" order by MOLECULE_ID;");
	            File outputFileCml = new File("nmrshiftdb2tr.xml");
	            FileOutputStream baoscml=new FileOutputStream(outputFileCml);
	            Writer out = new OutputStreamWriter(baoscml, "UTF8");
	            out.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<nmrshiftdb2>"));
	            for(int i=0;i<l.size();i++){
	             try{
	                DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
	                System.out.println(mol.getMoleculeId());
	                StringBuffer local=new StringBuffer();
	                local.append("<molecule>\r\n");
	                String name = mol.getChemicalNamesAsOneString(false,false);
	                if(name.equals("")){
	                    String[] names= GeneralUtils.getNamesFromPubchem(mol.getInchi());
	                    if(names!=null){
	                            if(names[names.length-1]!=null)
	                                    name=names[names.length-1];
	                            else if(names[names.length-2]!=null)
	                                    name=names[names.length-2];
	                            else if(mol.getIUPACName()!=null)
	                                    name=mol.getIUPACName();
	                            else
	                                    name=mol.getInchi();
	                    }
	                }
	                name=StringEscapeUtils.escapeXml(name);
	                local.append("<name>"+name+"</name>\r\n");
	                local.append("<link>http://www.nmrshiftdb.org/portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/"+mol.getMoleculeId()+"</link>\r\n");
	                local.append("<date>"+mol.getDate()+"</date>\r\n");
	                local.append("<abstract>Characterisation of "+name+" by its "+getSpectraTypes(mol) +" NMR spectra</abstract>\r\n");
	                local.append("<keywords>"+StringEscapeUtils.escapeXml(mol.getMoleculeKeywordsAsOneString(", "))+"</keywords>\r\n");
	                List spectra=mol.selectSpectra();
	                boolean hasliterature=false;
	                for(int k=0;k<spectra.size();k++){
	                  DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
	                  if(spectrum.getMark()>spectrum.getDisplaymark() && spectrum.getLiteraturesNew().size()>0){
	                      hasliterature=true;
	                          local.append("<spectrum>\r\n");
	                      local.append("<name>"+spectrum.getDBSpectrumType().getName()+"</name>\r\n");
	                      local.append("<date>"+spectrum.getDate()+"</date>\r\n");
	                      local.append("<link>http://www.nmrshiftdb.org/portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/"+mol.getMoleculeId()+"#spectrum"+spectrum.getSpectrumId()+"</link>\r\n");
	                      local.append("<abstract>A "+spectrum.getDBSpectrumType().getName()+" spectrum measured at ");
	                      boolean condfound=false;
	                      for(int m=0;m<spectrum.getMeasurementConditions().size();m++){
	                              if(!((DBCondition)spectrum.getMeasurementConditions().get(m)).getValue().equals("Unreported") && !((DBCondition)spectrum.getMeasurementConditions().get(m)).getValue().equals("Unknown")){
	                                      local.append(StringEscapeUtils.escapeXml(((DBCondition)spectrum.getMeasurementConditions().get(m)).getValue()+" ("+((DBCondition)spectrum.getMeasurementConditions().get(m)).getDBConditionType().getConditionName()+"), "));
	                                      condfound=true;
	                              }
	                      }
	                      local = GeneralUtils.removeLastComma(local);
	                      if(!condfound)
	                              local.append(" unreported conditions");
	                      else
	                              local.append(" conditions");
	                      local.append("</abstract>\r\n");
	                      for(int m=0;m<spectrum.getLiteraturesNew().size();m++){
	                              local.append(spectrum.getLiteraturesNew().get(m).getAsBibtex().toXML());
	                      }
	                      local.append("</spectrum>\r\n");
	                  }
	                }
	                local.append("</molecule>\r\n");
	                if(hasliterature)
	                    out.write(local.toString());
	              }
	              catch(Exception ex){
	                GeneralUtils.logError(ex,"loop in buildsdf",null,false);
	              }
	            }
	            out.write(new String("</nmrshiftdb2>"));
	            out.close();
			  /*TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();;
		        List l = DBMoleculePeer.executeQuery("select * from AUTHOR where SURNAME like '%,%' and AUTHOR_ID>=20002042");
		        for(int i=0;i<l.size();i++){
		            String firstnameold=((Record) l.get(i)).getValue(2).asString();
		            String surnameold=((Record) l.get(i)).getValue(3).asString();
		            System.out.println(firstnameold+" | "+surnameold);
		            System.out.println("case [1]");
		            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    	    String action = br.readLine();
		    	    int fall=1;
		    	    if(!action.equals(""))
		    	    	fall=Integer.parseInt(action);
		    	    if(fall==1){
			            StringTokenizer st=new StringTokenizer(firstnameold,",");
			            String lastname=st.nextToken();
			            String firstname=surnameold;
			            if(st.hasMoreTokens())
			            	firstname=st.nextToken()+(surnameold.charAt(0)=='-' ? "" : " ")+surnameold;
			            String sql="update AUTHOR set SURNAME='"+firstname.trim()+"', NAME='"+lastname.trim()+"', NAME_TOTAL='"+firstname+lastname+"' where AUTHOR_ID="+((Record) l.get(i)).getValue(1).asInt();
			            System.out.println(sql);
			            DBMoleculePeer.executeStatement(sql);
		    	    }else if(fall==2){
			    	    List<String> authors=new ArrayList<String>();
			    	    StringTokenizer st=new StringTokenizer(firstnameold+" "+surnameold,",");
			    	    while(st.hasMoreTokens()){
			    	    	String author=st.nextToken();
			    	    	if(author.contains(" and")){
			    	    		authors.add(author.substring(0,author.indexOf("and")));
			    	    		author=author.substring(author.indexOf("and")+4);
			    	    	}
			    	    	authors.add(author.trim());
			    	    }
			            System.out.println("Zahl der autoren ["+authors.size()+"] ");
			            br = new BufferedReader(new InputStreamReader(System.in));
			    	    action = br.readLine();
			    	    int authorcount=authors.size();
			    	    if(!action.equals(""))
			    	    	authorcount=Integer.parseInt(action);
			    	    StringBuffer authorsstring=new StringBuffer();
			    	    for(int k=0;k<authorcount;k++){
			    	    	System.out.println("author "+(k+1)+" ["+(k<authors.size() ? authors.get(k) : "")+"] ");
			    	    	br = new BufferedReader(new InputStreamReader(System.in));
				    	    action = br.readLine();
			    	    	if(!action.equals(""))
			    	    		authorsstring.append(action+";");
			    	    	else
			    	    		authorsstring.append(authors.get(k)+";");
			    	    }
			    	    System.out.println(authorsstring);
		    	    	System.out.println("conintue?");
		    	    	br = new BufferedReader(new InputStreamReader(System.in));
			    	    action = br.readLine();
			    	    if(action.equals("y")){
				            List list = DBLiterature.analyzeAuthor(authorsstring.toString());
				            for (int k = 0; k < list.size(); k++) {
				              StringAndStringAndBoolean ssb = (StringAndStringAndBoolean) list.get(k);
				              DBAuthor dbauthor = DBAuthorPeer.getOldOrMakeNew(ssb.string1, ssb.string2,dbconn);
				              System.out.println(dbauthor.getSurname()+"|"+dbauthor.getName());
				              dbauthor.save();
						        List l2 = DBMoleculePeer.executeQuery("select * from IS_AUTHOR where AUTHOR_ID="+((Record) l.get(i)).getValue(1).asInt());
						        System.out.println(l2.size());
						        for(int m=0;m<l2.size();m++){
					              IsAuthor litaut = new IsAuthor();
					              litaut.setDBAuthor(dbauthor);
					              litaut.setDBLiteratureKey(new NumberKey(((Record) l2.get(m)).getValue(2).asInt()));
					              litaut.setAuthorOrder(k);
					              System.out.println(litaut.getAuthorId()+"|"+litaut.getLiteratureId());
					              litaut.save(dbconn);
						        }
						        
				            }
				            String sql="delete from IS_AUTHOR where AUTHOR_ID="+((Record) l.get(i)).getValue(1).asInt();
				            System.out.println(sql);
				            DBMoleculePeer.executeStatement(sql);
				            sql="delete from AUTHOR where AUTHOR_ID="+((Record) l.get(i)).getValue(1).asInt();
				            System.out.println(sql);
				            DBMoleculePeer.executeStatement(sql);
			    	    }
                    }
				}*/
                /*TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
                config.initialize();
                Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
                Connection dbconn = Torque.getConnection();;
                List l = DBMoleculePeer.executeQuery("select count(*) as c, LITERATURE from SPECTRUM where LITERATURE<>'' group by LITERATURE having c>=1;");
                for(int i=0;i<l.size();i++){
					DBLiterature lit=new DBLiterature();
                    String literature=((Record) l.get(i)).getValue(2).asString();
		            System.out.println(literature);
		    	    List<String> authors=new ArrayList<String>();
		    	    StringTokenizer st=new StringTokenizer(literature,",");
					if(literature.contains(";"))
						st=new StringTokenizer(literature,";");
		    	    while(st.hasMoreTokens()){
		    	    	String author=st.nextToken().trim();;
		    	    	if(author.contains("and")){
		    	    		authors.add(author.substring(0,author.indexOf("and")));
		    	    		author=author.substring(author.indexOf("and")+4);
		    	    	}
						if(author.contains(",")){
							if(author.endsWith(" et al.")){
								author=author.substring(0, author.length()-7);
								lit.etalprov=true;
							}
							author=author.substring(author.indexOf(",")+1)+" "+author.substring(0,author.indexOf(","));
							author=author.trim();
						}
		    	    	authors.add(author);
		    	    }
		            System.out.println("Zahl der autoren ["+authors.size()+"] ");
		            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    	    String action = br.readLine();
		    	    int authorcount=authors.size();
		    	    if(!action.equals(""))
		    	    	authorcount=Integer.parseInt(action);
		    	    StringBuffer authorsstring=new StringBuffer();
		    	    for(int k=0;k<authorcount;k++){
		    	    	System.out.println("author "+(k+1)+" ["+(k<authors.size() ? authors.get(k) : "")+"] ");
		    	    	action=br.readLine();
		    	    	if(!action.equals(""))
		    	    		authorsstring.append(action+"; ");
		    	    	else
		    	    		authorsstring.append(authors.get(k)+"; ");
		    	    }
		            lit.type = NmrshiftdbConstants.JOURNAL_ARTICLE;
		            lit.setAuthor(GeneralUtils.removeLastComma(authorsstring).toString());
		            System.out.println("Title ");
		            action=br.readLine();
		            lit.setTitle(action);
		            System.out.println("subtitle ");
		            action=br.readLine();
		            lit.setSubtitle(action);
		            System.out.println("et al ["+lit.etalprov+"] ");
		            action=br.readLine();
		            if (action.equals("y")) {
		                lit.etalprov = true;
		            }
                    if (action.equals("n")) {
                        lit.etalprov = false;
                    }
					if(literature.matches(".*[0-9]*-[0-9]*.*")){
							Matcher matcher = Pattern.compile( "[0-9]*-[0-9]*" ).matcher( literature ); 
							matcher.find();
							String ft=matcher.group();
						try{
							lit.from=Integer.parseInt(ft.substring(0,ft.indexOf("-")));
							lit.to=Integer.parseInt(ft.substring(ft.indexOf("-")+1)); 
						}catch(Exception ex){}
					}
		            System.out.println("from page ["+lit.from+"] ");
		            action=br.readLine();
		            if(!action.equals(""))
		            	lit.from = Integer.parseInt(action);
		            System.out.println("to page ["+lit.to+"] ");
		            action=br.readLine();
		            if(!action.equals(""))
		            	lit.to = Integer.parseInt(action);
		            System.out.println("Title journal [Phytochemistry]");
		            action=br.readLine();
	                lit.journaltitle = action;
					if(action.equals(""))
					action="Phytochemistry";
		            System.out.println("subTitle journal ");
		            action=br.readLine();
		            lit.journalsubtitle = action;
		            System.out.println("ita ");
		            action=br.readLine();
	                lit.ita = action;
		            System.out.println("volume ");
		            action=br.readLine();
		            if(action.equals(""))
		            	lit.volume=-1;
		            else
		                lit.volume = Integer.parseInt(action);
		            System.out.println("year ");
		            action=br.readLine();
		            if(!action.equals(""))
		            	lit.year = Integer.parseInt(action);
		            System.out.println("issue ");
		            action=br.readLine();
		            if(!action.equals(""))
		            	lit.issue = Integer.parseInt(action);
	                System.out.println(lit.toString());
	                System.out.println("save ?");
	                action=br.readLine();
	                if(action.equals("y")){
						  List spectra =DBSpectrumPeer.executeQuery("select SPECTRUM_ID from SPECTRUM where LITERATURE='"+literature+"'");
						  System.out.println(spectra.size());
						  for(int m=0;m<spectra.size();m++){
							  int spectrumid = ((Record) spectra.get(m)).getValue(1).asInt();
							  System.out.println(spectrumid);
							  lit.save(spectrumid,dbconn);
							  DBSpectrumPeer.executeStatement("update SPECTRUM set LITERATURE='' where SPECTRUM_ID="+spectrumid);
						  }
	                }
		        }*/
		  }


		private static String getSpectraTypes(DBMolecule mol) throws TorqueException {
			Set<String> types = new HashSet<String>();
			for(int i=0;i<mol.selectSpectra().size();i++){
				types.add(((DBSpectrum)mol.selectSpectra().get(i)).getDBSpectrumType().getName());
			}
			StringBuffer typesstring=new StringBuffer();
			Iterator<String> it=types.iterator();
			while(it.hasNext()){
				typesstring.append(it.next()+", ");
			}
			return GeneralUtils.removeLastComma(typesstring).toString();
		}
		
		  /**
		   *  Checks if shifts are assigned to other atom the spectrum type suggests
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeCheckAssignments(String[] args) throws Exception {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select SPECTRUM_ID from SPECTRUM where REVIEW_FLAG='true';");
			  while (rs.next()) {
				  try {
					  DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  List v=spectrum.getDBSignals();
					  String element=spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol();
					  for(int i=0;i<v.size();i++){
						  DBSignal signal=(DBSignal)v.get(i);
						  List atoms=signal.getAtoms();
						  for(int k=0;k<atoms.size();k++){
							  if(!((DBAtom)atoms.get(k)).getSymbol().equals(element)){
								  System.err.println("Wrong assigenment in "+spectrum.getSpectrumId()+", signal "+signal.getFirstShift().getValue());
							  }
						  }
					  }
				  }
				  catch(Exception ex){
					  ex.printStackTrace();
				  }
			  }
			  //Check if atoms have correct valency
			  rs = stmt.executeQuery("select ATOM_ID from ATOM");
			  Map<String, Integer> bondorders = new HashMap<String, Integer>();
			  bondorders.put("H", 1);
			  bondorders.put("C", 4);
			  bondorders.put("N", 3);
			  bondorders.put("O", 2);
			  while (rs.next()) {
				  try {
					  DBAtom atom = DBAtomPeer.retrieveByPK(rs.getInt(1));
					  if(bondorders.containsKey(atom.getSymbol())){
						  int bondcount=0;
						  for(int i=0;i<atom.getDBBondDBAtoms().size();i++){
							  bondcount+=((DBBondDBAtom)atom.getDBBondDBAtoms().get(i)).getDBBond().getDegree();
						  }
						  if(bondcount-atom.getFormalCharge()!=bondorders.get(atom.getSymbol())){
							  System.out.println("Molecule "+atom.getMoleculeId()+", atom "+atom.getMdlNumberPlus1());
						  }
					  }
				  } catch (Exception ex) {
					  System.err.println("Problems with atom " + rs.getInt(1));
					  ex.printStackTrace();
				  }
			  }
			  rs = stmt.executeQuery("select SPECTRUM_ID from SPECTRUM where REVIEW_FLAG='true';");
			  while (rs.next()) {
				  try {
					  DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  List signals = spectrum.getDBSignals();
					  for (int i = 0; i < signals.size(); i++) {
						  DBAtom atom = (DBAtom)((DBSignal) signals.get(i)).getAtoms().get(0);
						  if(spectrum.getSpectrumTypeId()==2)
							  atom = atom.getConnectedAtoms().get(0);
						  if (AtomUtils.getValuesForAtom(atom.getMdlNumber(), spectrum.getSpecfile()).equals("")) {
							  System.err.println("Empty signal from String, but signal in DB! "+spectrum.getSpectrumId());
						  } else {
							  if (AtomUtils.getValuesForAtom(atom.getMdlNumber(), spectrum.getSpecfile()).indexOf(Float.toString(((DBSignal) signals.get(i)).getFirstShift().getValue()))==-1) {
								  System.err.println("Wrong for Atom " + atom.getMdlNumber()+" "+spectrum.getSpectrumId());
							  }
						  }
					  }
				  } catch (Exception ex) {
					  System.err.println("Start checking shifts for spectrum " + rs.getInt(1));
					  ex.printStackTrace();
				  }
			  }
			  rs.first();
			  while (rs.next()) {
				  try {
					  DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  for(int i=0;i<spectrum.getDBSignals().size();i++){
					      IAtomContainer mol = spectrum.getDBMolecule().getAsCDKMolecule(1);
						  for(int k=0;k<((DBSignal)spectrum.getDBSignals().get(i)).getAtoms().size();k++){
							  double predicted = PredictionTool.generalPredict(mol, mol.getAtom(((DBAtom)((DBSignal)spectrum.getDBSignals().get(i)).getAtoms().get(k)).getMdlNumber()), false, true, true, false, spectrum.getSpectrumId(), -1, new StringBuffer(), false, false, null, null, 6, true, new StringBuffer(), 6, true, true, null)[1];
							  if(Math.abs(predicted-((DBSignal)spectrum.getDBSignals().get(i)).getFirstShift().getValue())>50)
								  System.out.println("shift "+((DBSignal)spectrum.getDBSignals().get(i)).getFirstShift().getValue()+" in spectrum "+spectrum.getSpectrumId()+" is more than 50 ppm off");
						  }
					  }
				  } catch (Exception ex) {
					  ex.printStackTrace();
				  }
			  }
			  
		  }
		  
		  
		  private static void executeInsertNamesFromPubchem(String[] args) throws Exception {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select MOLECULE_ID, CANONICAL_NAME.NAME from (MOLECULE left join CHEMICAL_NAME using (MOLECULE_ID )) join CANONICAL_NAME using (MOLECULE_ID) where CHEMICAL_NAME.NAME is null and CANONICAL_NAME.CANONICAL_NAME_TYPE_ID=4");
			  int[] namescount=new int[3];
			  while (rs.next()) {
				  try {
					  String[] names = GeneralUtils.getNamesFromPubchem(rs.getString(2));
					  /*TODO this is unclear 
					  if(names!=null){
					    if(names[0]!=null){
							  DBChemicalName canname=new DBChemicalName(rs.getInt(1),names[0]);
							  //canname.save();
							  namescount[0]++;
						  }
						  if(names[1]!=null){
							  namescount[1]++;
						  }
						  if(names[2]!=null){
							  namescount[2]++;
						  }
						  System.out.println(rs.getInt(1)+" "+names[0]+" "+names[1]+" "+names[2]);
					  }*/
				  }catch(Exception ex){
					  ex.printStackTrace();
				  }
			  }
			  System.out.println(namescount[0]+" "+namescount[1]+" "+namescount[2]);
		  }

		  /**
		   *  Executes making of aromaticities
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeMakeAromaticity(String[] args) throws Exception{
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE");
			  int i = -1;
			  int fehler = 0;
			  while (rs.next()) {
				  try {
					  i++;
					  DBMolecule mol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  System.err.println("Start making aromaticity for molecule " + mol.getMoleculeId());
					  IAtomContainer molWithH = (IAtomContainer) new MDLV2000Reader(new StringReader(mol.getStructureFile(1,false))).read(new org.openscience.cdk.AtomContainer());
					  AtomUtils.addAndPlaceHydrogens(molWithH);
					  AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
			          ElectronDonation model       = ElectronDonation.cdk();
			          CycleFinder      cycles      = Cycles.cdkAromaticSet();
			          Aromaticity      aromaticity = new Aromaticity(model, cycles);
			          aromaticity.apply(molWithH);
					  DBAtom[] dbatoms=mol.getAtomsAsArrayInMdlOrder();
					  for(int k=0;k<molWithH.getAtomCount();k++){
						  if(molWithH.getAtom(k).getFlag(CDKConstants.ISAROMATIC))
							  dbatoms[k].setIsAromatic(NmrshiftdbConstants.TRUE);
						  else
							  dbatoms[k].setIsAromatic(NmrshiftdbConstants.FALSE);
						  dbatoms[k].save();
					  }
					  List bonds=mol.getBonds();
					  for(int k=0;k<molWithH.getBondCount();k++){
						  if(molWithH.getBond(k).getFlag(CDKConstants.ISAROMATIC))
							  ((DBBond)bonds.get(k)).setIsAromatic(NmrshiftdbConstants.TRUE);
						  else
							  ((DBBond)bonds.get(k)).setIsAromatic(NmrshiftdbConstants.FALSE);
						  ((DBBond)bonds.get(k)).save();
					  }
				  } catch (Exception ex) {
					  System.err.println("Fehler: " + ex.getMessage() + ", " + rs.getInt(1));
					  fehler++;
				  }
			  }
			  System.err.println("There were " + fehler + " errors!");
		  }


		  /**
		   *  Reads corina 3d coordinates
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeReadCorina(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  File[] files = new File(args[0]).listFiles();
			  int fehler=0;
			  for (int i = 0; i < files.length; i++) {
				  if (files[i].getName().equals("nmrshiftdb_3D.sdf")) {
				      IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(files[i]), DefaultChemObjectBuilder.getInstance());
					  while(reader.hasNext()){
							  IAtomContainer molWithHfromCorina =  reader.next();
							  AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithHfromCorina);
					          ElectronDonation model       = ElectronDonation.cdk();
					          CycleFinder      cycles      = Cycles.cdkAromaticSet();
					          Aromaticity      aromaticity = new Aromaticity(model, cycles);
					          aromaticity.apply(molWithHfromCorina);
							  try{
								  String nmrshiftdbnr= (String)molWithHfromCorina.getProperty("nmrshiftdb2 ID");
								  System.err.println(nmrshiftdbnr);
								  DBMolecule dbmol=DBMoleculePeer.retrieveByPK(new NumberKey(nmrshiftdbnr));
								  if(!dbmol.hasCoordinates(2)){
								      IAtomContainer nmrshiftdbWithH=dbmol.getAsCDKMolecule(1);
									  List list=new UniversalIsomorphismTester().getIsomorphAtomsMap(molWithHfromCorina, nmrshiftdbWithH);
									  /*if(list==null && molWithHfromCorina.getAtomCount()==nmrshiftdbWithH.getAtomCount()){
									    list=new Vector();
									    for(int k=0;k<molWithHfromCorina.getAtomCount();k++){
									    list.add(new RMap(k,k));
									    }
									    }*/
									  if(list==null)
										  System.err.println("kein isomorphismus "+nmrshiftdbnr);
									  for (int k = 0; k < list.size(); k++) {
										  int numbernmrshiftdb=((RMap) list.get(k)).getId2();
										  int corinanumber=((RMap) list.get(k)).getId1();
										  int atomid=dbmol.getAtomsAsArrayInMdlOrder()[numbernmrshiftdb].getAtomId();
										  DBCoordinateSet3d coords3d=new DBCoordinateSet3d(molWithHfromCorina.getAtom(corinanumber).getPoint3d().x,molWithHfromCorina.getAtom(corinanumber).getPoint3d().y,molWithHfromCorina.getAtom(corinanumber).getPoint3d().z,2);
										  coords3d.setAtomId(atomid);
										  try{
											  coords3d.save();
										  }catch(Exception ex){
											  //this is in case coords already read
										  }
									  }
								  }
							  }catch(Exception ex){
								  System.err.println("Failed doing "+(String)molWithHfromCorina.getProperty(CDKConstants.TITLE)+". "+ex.getMessage());
								  ex.printStackTrace();
								  fehler++;
							  }
						  }
					  }
				  }
			  System.err.println("There were "+fehler+" errors!");
		  }


		  /**
		   *  Creates the proton descriptors
		   *
		   * @param  args           The command line args
		   * @exception  Exception  Databse problems
		   */
		  private static void executeMakeDescriptorsAll(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  /*Statement stmt = dbconn.createStatement();
			  Statement stmt2 = dbconn.createStatement();
			  Statement stmt3 = dbconn.createStatement();
c			  jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
			  stmt.executeUpdate("drop table if exists DESCRIPTORS_ALL_C");
			  ResultSet rs = stmt.executeQuery("select distinct MOLECULE.MOLECULE_ID from MOLECULE join SPECTRUM using (MOLECULE_ID) join SPECTRUM_TYPE using (SPECTRUM_TYPE_ID) where NAME='13C' order by MOLECULE_ID");
			  int fehler = 0;
			  boolean first=true;
			  float[] floats=null;
			  AllRingsFinder arf=new AllRingsFinder();
			  while (rs.next()) {
				  try {
					  DBMolecule mol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
					  if(mol.hasCoordinates(2)){
						  System.err.println(mol.getMoleculeId());
						  IMolecule cdkmol=mol.getAsCDKMoleculeWithAny3d();
						  AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkmol);
						  Criteria crit=new Criteria();
						  crit.add(DBSpectrumPeer.MOLECULE_ID,mol.getMoleculeId());
						  crit.add(DBSpectrumPeer.SPECTRUM_TYPE_ID,1);
						  DBSpectrum hspectrum=(DBSpectrum)DBSpectrumPeer.doSelect(crit).get(0);
						  List signals=hspectrum.getDBSignals();
						  for(int i=0;i<signals.size();i++){
							  DBSignal signal=(DBSignal)signals.get(i);
							  for(int h=0;h<signal.getAtoms().size();h++){
								  int atomnumber=((DBAtom)signal.getAtoms().get(h)).getMdlNumber();
								  //System.out.println(signal.getFirstShift().getValue()+" "+atomnumber);
								  DBSpectrum spectrum=signal.getDBSpectrum();
								  DBCondition temp=spectrum.getMeasurementConditionWithName("Temperature [K]",null);
								  DBCondition field=spectrum.getMeasurementConditionWithName("Field Strength [MHz]",null);
								  DBCondition solvent=spectrum.getMeasurementConditionWithName("Solvent",null);
								  int protonclass=AtomUtils.getProtonClass(cdkmol, atomnumber, arf.findAllRings(cdkmol));
								  floats=AtomUtils.getDescriptorSet(temp, field,solvent, cdkmol, cdkmol.getAtom(atomnumber), cdkmol.getAtom(atomnumber), AtomUtils.getAllRings(),protonclass);
								  if(floats!=null){
									  if(first){
										  String sql1="create table DESCRIPTORS_ALL_C";
										  String sql2=" (ATOM_ID int not null, SIGNAL_ID int not null,";
										  for(int k=0;k<floats.length;k++){
											  sql2+="des"+k+" float not null default 0,";
										  }
										  sql2+="des"+floats.length+" float not null default 0,";
										  sql2+="PRIMARY KEY  (ATOM_ID, SIGNAL_ID))";
										  stmt3.executeUpdate(sql1+""+sql2);
										  first=false;
									  }
									  StringBuffer sql=new StringBuffer("insert into DESCRIPTORS_ALL_C values("+((DBAtom)signal.getAtoms().get(h)).getAtomId()+","+signal.getSignalId()+",");
									  for(int k=0;k<floats.length;k++){
										  if(Float.isNaN(floats[k]) || Float.isInfinite(floats[k]))
											  sql.append("-1,");
										  else
											  sql.append(floats[k]+",");
									  }
									  stmt3.executeUpdate(sql.toString()+""+protonclass+")");
								  }
							  }
						  }
					  }
				  }catch(Exception ex){
					  ex.printStackTrace();
					  fehler++;
				  }
			  }
			  stmt.executeUpdate("drop table if exists DESCRIPTORS_1_C");
			  stmt.executeUpdate("drop table if exists DESCRIPTORS_2_C");
			  stmt.executeUpdate("drop table if exists DESCRIPTORS_3_C");
			  stmt.executeUpdate("drop table if exists DESCRIPTORS_4_C");
			  stmt.executeUpdate("drop table if exists DESCRIPTOR_C_FACTORS");
			  stmt.executeUpdate("create table DESCRIPTOR_C_FACTORS (PROTONCLASS INT, DESCRIPTOR VARCHAR(6), MIN_VALUE FLOAT, DIVISOR FLOAT)");
			  for(int k=1;k<5;k++){
				  stmt.executeUpdate("create table DESCRIPTORS_"+k+"_C select * from DESCRIPTORS_ALL_C where des"+(floats.length)+"="+k);
				  for(int i=0;i<floats.length+1;i++){
					  System.err.println(k+"  "+i);
					  String sql="select min(des"+i+"), max(des"+i+") from DESCRIPTORS_"+k+"_C";
					  rs=stmt.executeQuery(sql);
					  rs.next();
					  float min=rs.getFloat(1);
					  float max=rs.getFloat(2);
					  if(min==max){
						  System.err.println("min==max for "+i);
						  stmt2.executeUpdate("alter table DESCRIPTORS_"+k+"_C drop column des"+i);
					  }else{
						  float divisor=max-min;
						  if(divisor==0)
							  divisor=1;
						  sql="select ATOM_ID, des"+i+" from DESCRIPTORS_"+k+"_C";
						  rs=stmt.executeQuery(sql);
						  stmt2.executeUpdate("insert into DESCRIPTOR_C_FACTORS values("+k+",'des"+i+"',"+min+","+divisor+");");
						  stmt2.executeUpdate("update DESCRIPTORS_"+k+"_C set des"+i+"=(des"+i+"-"+min+")/"+divisor);
					  }
				  }
			  }
			  System.err.println("There were "+fehler+" errors!");*/
			  Statement stmt = dbconn.createStatement();
			  String sql="select DESCRIPTORS_3_C.* from DESCRIPTORS_3_C limit 30000";
			  double[] errors=new double[10];
			  double[] hits=new double[10];
			  for(int i=0;i<10;i++){
System.err.println(i);
				  int numberoftrainings=0;
				  File trainingfile=new File(args[0]+"/descsvm.training");
				  File testfile=new File(args[0]+"/descsvm.test");
				  BufferedWriter writertraining=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainingfile)));
				  BufferedWriter writertest=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testfile)));
				  ResultSet rs=stmt.executeQuery(sql);
System.err.println(i);
				  int m=0;
				  ResultSetMetaData meta=rs.getMetaData();
				  int colsnumber = meta.getColumnCount();
				  Vector<Float> shifts=new Vector<Float>();
				  while(rs.next()){
					  StringBuffer line=new StringBuffer();
					  DBSignal signal=DBSignalPeer.retrieveByPK(new NumberKey(rs.getInt(2)));
					  //line.append((int)(signal.getFirstShift().getValue()*10)+" ");
					  line.append(signal.getFirstShift().getValue()+" ");
					  for(int k=0;k<colsnumber-2;k++){
						  line.append((k+1)+":"+rs.getDouble(k+3)+" ");
						  //System.err.println((k+1)+"  "+meta.getColumnName(k+3));
					  }
					  line.append("\n");
					  if(m % 10==i){
						  writertest.write(line.toString());
						  shifts.add(new Float(signal.getFirstShift().getValue()));
						  numberoftrainings++;
					  }else{
						  writertraining.write(line.toString());
					  }
					  m++;
				  }
				  writertraining.close();
				  writertest.close();
				  System.err.println("ready");
				  File modelfile=new File(args[0]+"/descsvm.model");
				  String[] args2={"-s","3","-t","1","-e","0.001","-n",".5","-c","32","-g","0.0078125",trainingfile+"",modelfile+""};
				  libsvm.svm_train t = new libsvm.svm_train();
				  t.run(args2);
				  //trainingfile.delete();
				  File outputfile=new File(args[0]+"/descsvm.output");
				  String[] args3={testfile+"",modelfile+"",outputfile+""};
				  libsvm.svm_predict.main(args3);
				  testfile.delete();
				  FileInputStream fstreamout = new FileInputStream(outputfile);
				  DataInputStream in = new DataInputStream(fstreamout);
				  double sqerror=0;
				  for(int l=0;l<shifts.size();l++){
					  float value=new Float(in.readLine()).floatValue();
					  float shift=shifts.get(l).floatValue();
					  System.err.println((value)+"  "+(shift));
					  if(((int)(value*10))==((int)(shift*10)))
						  hits[i]++;
					  sqerror+=(value-shift)*(value-shift);
				  }
				  errors[i]=sqerror/numberoftrainings;
				  System.err.println("hits "+hits[i]+" num "+numberoftrainings);
				  hits[i]=hits[i]/numberoftrainings;
				  System.err.println("error "+errors[i]+", hits: "+hits[i]);
				  in.close();
				  outputfile.delete();
				  modelfile.delete();
			  }
			  System.err.println("hits average :"+Statistics.average(hits));
			  System.err.println("mse "+Statistics.average(errors));
System.exit(0);
		  }
		  
		  private static void executeRemoveDoubleBondConfigsInSmallRings(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE where MOLECULE_ID=20226960 order by MOLECULE_ID");
			  int cases=0;
			    while (rs.next()) {
				    try {
				    	DBMolecule dbmol = DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
				    	IAtomContainer cdkmol = dbmol.getAsCDKMolecule(1);
				    	AllRingsFinder arf = new AllRingsFinder();
				        IRingSet ringSet=arf.findAllRings(cdkmol);
				        List bonds=dbmol.getBonds();
				        Iterator<IBond> it=cdkmol.bonds().iterator();
				        boolean[] dbc=new boolean[cdkmol.getBondCount()];
				        boolean changed=false;
				        int c=0;
				        while(it.hasNext()) {
				        	IBond bond=it.next();
				            if (!AtomUtils.isValidDoubleBondConfiguration(cdkmol,bond,ringSet) && ((DBBond)bonds.get(c)).getIsConfigurationSpecified().equals("true") ) {
				            	((DBBond)bonds.get(c)).setIsConfigurationSpecified("false");
				            	((DBBond)bonds.get(c)).save();
				            	changed=true;
				            }
				            c++;
				        }
				        if(changed){
				        	cases++;
					    	System.err.println(dbmol.getMoleculeId());
				        	StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
					    	GeneralUtils.getExistingMolecule(dbmol.getAsCDKMoleculeWithoutH(), cdkmol, dbc, null, dbmol.getStructureFile(1,false), ssb);
System.err.println(new String(dbmol.getSmilesString())+" "+new String(dbmol.getSmilesStringChiral())+" "+ssb.string1+" "+ssb.string2);
					    	dbmol.setSmilesString(ssb.string1.getBytes());
					    	if(ssb.bool)
					    		dbmol.setSmilesStringChiral(ssb.string2.getBytes());
					    	else
					    		dbmol.setSmilesStringChiral(null);
			                //dbmol.save();
				        }
				    }
				    catch(Exception ex){
				    	ex.printStackTrace();
				    }
			    }
			    System.out.println("cases "+cases);
		  }

		  private static void makeArff(String[] args){
			  try{
				  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
				  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
				  config.initialize();
				  Connection dbconn = Torque.getConnection();
				  Statement stmt = dbconn.createStatement();
				  ResultSet rs=stmt.executeQuery("select * from DESCRIPTORS_ALL_C limit 25000");
				  ResultSetMetaData meta=rs.getMetaData();
				  int colsnumber = meta.getColumnCount();
				  OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(new File("data.csv")));
				    for(int k=3;k<=colsnumber-1;k++){
				    	osw.write("v"+k+",");
				    	
				    }
				    osw.write("shift\r\n");
				  int i=1;
				  while(rs.next()){
					  try{
					System.err.println(i);
				    DBSignal signal=DBSignalPeer.retrieveByPK(new NumberKey(rs.getInt(2)));
				    for(int k=3;k<=colsnumber-1;k++){
				    	if(rs.getDouble(k)==-1){
				    		osw.write("?,");
				    	}else{
			    			osw.write(rs.getDouble(k)+",");
				    	}
				    }
			    	osw.write((int)(signal.getFirstShift().getValue()*10)+"\r\n");
				    i++;
					  }catch(Exception ex){}
				  }
				  osw.close();
		      }catch(Exception ex){
		    	  ex.printStackTrace();
		      }
		 	 
		      }
		  
		  private static void makeChebiMapping(String[] args) throws Exception {
			  try{
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
			  ResultSet rs = stmt.executeQuery("select MOLECULE_ID, CANONICAL_NAME.NAME from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4");
			  FileInputStream fstream = new FileInputStream(args[0]+"/chebiInchiExport.csv");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  Map<String, List<Integer>> chebi=new HashMap<String, List<Integer>>();
			  while ((strLine = br.readLine()) != null)   {
				  List<Integer> ids=new Vector<Integer>();
				  StringTokenizer st = new StringTokenizer(strLine,"\t");
				  String string1=st.nextToken();
				  String string2=st.nextToken();
				  if(st.hasMoreTokens()){
					  ids.add(new Integer(string1));
					  ids.add(new Integer(string2));
					  chebi.put(st.nextToken(),ids);
				  }else{
					  ids.add(0);
					  ids.add(new Integer(string1));
					  chebi.put(string2,ids);
				  }
			  }
			  in.close();
			  int found=0;
			  int notfound=0;
			  int nmrshiftdbentries=0;
			  OutputStreamWriter osw4=new OutputStreamWriter(new FileOutputStream(new File("chebimapping.csv")));
			  Map<String, Integer> nmrshiftdb = new HashMap<String, Integer>();
			  while (rs.next()) {
				  if(notfound%100 == 0)
					  System.err.println(found+" "+notfound);
				  int molid=rs.getInt(1);
				  String inchi=rs.getString(2);
				  if(nmrshiftdb.get(inchi)==null){
					  nmrshiftdb.put(inchi,new Integer(molid));
					  nmrshiftdbentries++;
				  if(chebi.get(inchi)!=null){
					  System.err.println(chebi.get(inchi).get(0)+" "+chebi.get(inchi).get(1)+" "+molid);
					  //osw4.write("insert into MOLECULE_HYPERLINK values("+molid+",'http://www.ebi.ac.uk/chebi/searchId.do?chebiId="+(chebi.get(inchi).get(0)!=0 ? chebi.get(inchi).get(0) : chebi.get(inchi).get(1))+"','ChEBI','21');\r\n");
					  osw4.write(chebi.get(inchi).get(0)+"\t"+chebi.get(inchi).get(1)+"\t"+molid+"\r\n");
					  found++;
				  }else{
					  //System.err.println("not found for "+inchikey);
					  notfound++;
				  }
				  }
			  }
			  System.err.println(found+" "+notfound+" "+nmrshiftdbentries);
			  osw4.close();
			  dbconn.close();
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }
		  }
		  
		  
          private static void executeWriteMol2(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		        List<Record> l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM left join USER_SPECTRUM_MARK using (SPECTRUM_ID) where REVIEW_FLAG=\"true\" and (USER_SPECTRUM_MARK.MARK>=5 or MARK is null) and SPECTRUM_TYPE_ID=5  order by MOLECULE_ID;");
		        File outputFile = new File("/tmp/spectra.csv");
		        FileOutputStream baos=new FileOutputStream(outputFile);
		        for(int i=0;i<l.size();i++){
		            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
			        File outputFile2 = new File("/tmp/"+mol.getMoleculeId()+".mol2");
			        FileOutputStream baos2=new FileOutputStream(outputFile2);
			        Mol2Writer writer=new Mol2Writer(baos2);
			        writer.write(mol.getAsCDKMolecule(1));
			        writer.close();
			        for(DBSpectrum spectrum : mol.selectSpectra()) {
			        	System.out.println(mol.getMoleculeId());
			        	if(spectrum.getReviewFlag().equals("true") && spectrum.getSpectrumTypeId()==5) {
			        		for(DBSignal signal : spectrum.getDBSignals()) {
			        			for(DBAtom atom : signal.getAtoms()) {
			        				baos.write(new String(mol.getMoleculeId()+","+atom.getMdlNumber()+","+signal.getFirstShift().getValue()+"\r\n").getBytes());
			        			}
			        		}
			        	}
			        }
		            
                }
                baos.close();
        }

          
          private static void executeWriteUnichem(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
                File outputFileCml = new File("nmrshiftdb2unichem.txt");
                FileOutputStream baoscml=new FileOutputStream(outputFileCml);
                Writer out = new OutputStreamWriter(baoscml, "UTF8");
                Connection dbconn = Torque.getConnection();
      		  Statement stmt = dbconn.createStatement();
      		  ResultSet rs = stmt.executeQuery("select distinct MOLECULE_ID from SPECTRUM join SPECTRUM_LITERATURE using (SPECTRUM_ID) where REVIEW_FLAG='true' order by MOLECULE_ID;");
      		  while(rs.next()){
      			  DBMolecule mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
                    System.out.println(mol.getMoleculeId());
                    if(mol.getInchi()!=null && mol.getInchiKey()!=null)
                        out.write(mol.getMoleculeId()+"\t"+GeneralUtils.replace(mol.getInchi(),"=1/","=1s/")+"\t"+mol.getInchiKey().trim()+"\r\n");
                }
                out.close();
        }
          
          /**
           *  Reads file from uni mainz.(carbon)
           *
           * @param  args           The command line args.
           * @exception  Exception  Database problems.
           */
          private static void executeReadWsu(String[] args) throws Exception {
            Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
            TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
            config.initialize();
            TurbineUser user = null;
            Criteria crit = new Criteria();
            crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
            List v = NmrshiftdbUserPeer.doSelect(crit);
            //Look if the bulkoad user exists
            if (v.size() > 0) {
              user = (TurbineUser) v.get(0);
            } else {
              throw new Exception("no bulkload user");
            }
            String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
            String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
            Properties nmrprops = new Properties();
            Properties jetspeedprops = new Properties();
            nmrprops.load(new FileInputStream(nmrpropsFile));
            jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
            BufferedReader buffRead = new BufferedReader(new FileReader("record.tsv"));
            String line = "";
            buffRead.readLine();
            while ((line = buffRead.readLine()) != null) {
            	try{
            	String[] tokens = line.split("\t",-1);
                int id=Integer.parseInt(tokens[0]);
                  System.err.println("Reading "+id);
                      SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
                      crit = new Criteria();
                      crit.add(DBSpectrumTypePeer.NAME, "13C");
                      v = DBSpectrumTypePeer.doSelect(crit);
                      subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
                      String cas =tokens[1];
                      String chemname=tokens[2];
                      String ref=tokens[7];
                      DBLiterature lit = new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE);
                      String authors = ref.substring(0,ref.indexOf("("));
                      StringTokenizer authorstokenizer=new StringTokenizer(authors,",");
                      boolean lastName=true;
                      String lastNameString="";
                      String authorsfinal="";
                      while(authorstokenizer.hasMoreTokens()){
                    	  if(lastName){
                    		  lastNameString=authorstokenizer.nextToken().trim();
                    		  if(lastNameString.indexOf("and ")>-1)
                    			  lastNameString=lastNameString.substring(4);
                    		  lastName=false;
                    	  }else{
                    		  authorsfinal=authorsfinal+authorstokenizer.nextToken().trim()+ " "+lastNameString+"; ";
                    		  lastName=true;
                    	  }
                      }
                      lit.setAuthor(authorsfinal.substring(0, authorsfinal.length()-2));
                      lit.year=Integer.parseInt(ref.substring(ref.indexOf("(")+1,ref.indexOf(")")));
                      ref=ref.substring(ref.indexOf(")")+2);
                      lit.setTitle(ref.substring(0,ref.indexOf(".")));
                      ref=ref.substring(ref.indexOf(".")+2);
                      String journaltitle=ref.substring(0,ref.indexOf(","));
                      lit.journaltitle=journaltitle.substring(0,journaltitle.lastIndexOf(" "));
                      lit.volume=Integer.parseInt(journaltitle.substring(journaltitle.lastIndexOf(" ")+1));
                      lit.from=Integer.parseInt(ref.substring(ref.indexOf(",")+2,ref.indexOf("–")));
                      lit.to=Integer.parseInt(ref.substring(ref.indexOf("–")+1,ref.length()-1));
                      List<DBLiterature> literatures = new ArrayList<DBLiterature>();
                      literatures.add(lit);
                      subData.setLiterature(literatures);
                      String solvent=tokens[8];
                      String freq=tokens[9];
                      subData.setComment("wsu "+id+"; "+tokens[11]);
                      int carbonPeaknumber=Integer.parseInt(tokens[13]);
                      //subData.setComment(files[i].getName()+".mz2; multplicities generated from proton count");
                      String content = new Scanner(new File("mols/"+id+".mol")).useDelimiter("\\Z").next();
                      subData.setMol(content,false,false,false);
                      List<DBConditionType> conds = subData.getConditions();
                      for(int k=0;k<conds.size();k++){
                          DBConditionType cond=conds.get(k);
                          if(cond.getConditionTypeId()==2)
                              cond.setCurrentValue(solvent);
                          else if(cond.getConditionTypeId()==3)
                              cond.setCurrentValue(freq);
                          else
                              cond.setCurrentValue(new String("Unreported"));
                      }
                      subData.setConditions(conds);
                      subData.setCasNumber(cas);
                      subData.setChemName(chemname);
                  subData.setSpectrumKeywords("wsu");
                  //IBond[] bonds = subData.getMolWithH().getBonds();
                  boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
                    AllRingsFinder arf = new AllRingsFinder();
                    IRingSet ringSet=arf.findAllRings(subData.getMolWithH());
                    IAtomContainer molwithh=subData.getMolWithH();
                  Iterator<IBond> it=molwithh.bonds().iterator();
                  int c=0;
                  while(it.hasNext()) {
                    IBond bond=it.next();
                    if (AtomUtils.isValidDoubleBondConfiguration(subData.getMolWithH(),bond,ringSet)) {
                      isDoubleBondSpecified[c] = true;
                    }
                    if((Boolean)bond.getProperty(CDKConstants.ISAROMATIC)==true){
                        bond.getAtom(0).setProperty(CDKConstants.ISAROMATIC,true);
                        bond.getAtom(1).setProperty(CDKConstants.ISAROMATIC,true);
                    }
                    c++;
                  }
                  subData.setDoubleBondConfiguration(true);
                  subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
                  new SaturationChecker().newSaturate(molwithh);
                  subData.setMol(molwithh,false,false, false);
                  BufferedReader buffRead2 = new BufferedReader(new FileReader("carbon.tsv"));
                  String line2 = "";
                  buffRead2.readLine();
                  while ((line2 = buffRead2.readLine()) != null) {
                      StringTokenizer st2=new StringTokenizer(line2,"\t");
                      st2.nextToken();
                      int id2=Integer.parseInt(st2.nextToken());
                      if(id2==id){
                          st2.nextToken();
                          CMLPeak peak=new CMLPeak();
                          peak.setXValue(Double.parseDouble(st2.nextToken()));
                          peak.setYValue(0);
                          subData.addToSignalstable(peak);
                      }
                  }
                  buffRead2.close();
                  subData.guessAssignment();
                  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
                  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
                  subData.setSmilesNonChiralProv(ssb.string1);
                  subData.setIsChiralProv(ssb.bool);
                  if(ssb.bool){
                      subData.setSmilesChiralProv(ssb.string2);
                  }
                  crit = new Criteria();
                  crit.add(DBSpectrumPeer.SPECFILE, subData.getSpectrumForApplet());
                  v = DBSpectrumPeer.doSelect(crit);
                  System.out.println(subData.data.toXML());
                    subData.save(user.getUserId(), "false", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
                    SpectrumUtils.doReview(subData, new Date(), user);
                    StringBuffer emailtext=new StringBuffer(subData.getInputMolAsMdl("spectrum "+subData.getSpectrumid(),nmrprops.getProperty("csearchsendemail"),false));
                    emailtext.append(">  <chemical.shift.values>\r\n");
                    IAtomContainer mol = subData.getMolWithH();
                    for(int l=0;l<subData.getSignalstable().getPeakChildren().size();l++){
                    	if(subData.getSignalstable().getPeakChildren().get(l).getAtomRefs()!=null){
	                        for(int k=0;k<subData.getSignalstable().getPeakChildren().get(l).getAtomRefs().length;k++){
	                            emailtext.append(mol.getAtomNumber(AtomUtils.getAtomById(mol, subData.getSignalstable().getPeakChildren().get(l).getAtomRefs()[k]))+1);
	                            emailtext.append(" = ");
	                            emailtext.append(subData.getSignalstable().getPeakChildren().get(l).getXValue());
	                            emailtext.append("\r\n");
	                        }
                        }
                    }
                    if(subData.getLiterature().size()>0){
                        DBLiterature literature = subData.getLiterature().get(0);
                        if(literature.isPublishedInJournalFromAttributes()){
                            emailtext.append("\r\n>  <author>\r\n");
                            emailtext.append(literature.getAuthor()+"\r\n");
                            emailtext.append("\r\n>  <literature>\r\n");
                            emailtext.append(literature.getJournalData()+"\r\n");
                        }
                    }
                    emailtext.append("\r\n>  <solvent>\r\n");
                    DBConditionType solvent2 = subData.getSolvent();
                    if(solvent2.getCurrentValue().equals("") && solvent2.getCurrentSelectedCondition() != null)
                        emailtext.append(solvent2.getCurrentSelectedCondition().getValue() +"\r\n");
                    else
                        emailtext.append(solvent2.getCurrentValue() +"\r\n");
                    if(subData.getChemName()!=null && !subData.getChemName().equals("")){
                        emailtext.append("\r\n>  <compound.name>\r\n");
                        emailtext.append(subData.getChemName()+"\r\n");
                        
                    }
                    emailtext.append("\r\n>  <source.code>\r\n");
                    emailtext.append("nmrshiftdb2\r\n");
                    emailtext.append("\r\n>  <source.number>\r\n");
                    emailtext.append(subData.getSpectrumid()+"\r\n");
                    emailtext.append("\r\n>  <date.of.creation>\r\n");
                    emailtext.append(subData.getThisDate()+"\r\n");
                    emailtext.append("\r\n>  <proton.frequency>\r\n");
                    emailtext.append(subData.getFrequency()+"\r\n");
                    emailtext.append("\r\n>  <temperature>\r\n");
                    emailtext.append(subData.getTemperature()+"\r\n");
                    emailtext.append("\r\n>  <password>\r\n");
                    emailtext.append("public\r\n");
                    emailtext.append("\r\n\r\n$$$$\r\n");
                    MailMessage email = new org.apache.turbine.util.mail.MailMessage("localhost", nmrprops.getProperty("csearchemail"), nmrprops.getProperty("csearchsendemail"), "nmrshiftdb2 referee request", emailtext.toString());
                    if (email.send() == false) {
                        System.err.println("Sending email to csearch failed for spectrum ");
                    }
                    System.err.println("Wrote "+id);
            	}catch(Exception ex){
            		ex.printStackTrace();
            	}
            }
          }
          
          public static void writeJson(String[] args) throws Exception{
    		  Torque.init(args[1] + "../conf/jetspeed/WEB-INF/conf/Torque.properties");
    		  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
    		  config.initialize();
    		  Connection dbconn = Torque.getConnection();
    		  Statement stmt = dbconn.createStatement();
    		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE");
    		  while(rs.next()){
    			  try{
    			  DBMolecule mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
    			  System.out.println(mol.getMoleculeId());
    			  FileOutputStream fos=new FileOutputStream(args[0]+"/nmrshiftdb2_"+mol.getMoleculeId()+".json");
    			  JsonExport export=new JsonExport();
    			  export.setId(mol.getMoleculeId());
    			  export.setMolfile(mol.getStructureFile(1, true));
    			  String[] names=new String[mol.getDBChemicalNames().size()];
    			  int i=0;
    			  for(DBChemicalName name : mol.getDBChemicalNames()){
    				  names[i]=name.getName();
    				  i++;
    			  }
    			  export.setNames(names);
    			  List<DBSpectrum> spectra = mol.selectSpectra();
    			  JsonExport.Nmr[] nmr = new JsonExport.Nmr[spectra.size()];
    			  i=0;
    			  for(DBSpectrum spectrum : spectra){
    				  nmr[i]=export.new Nmr();
    				  nmr[i].setDate(spectrum.getDate());
    				  nmr[i].setDimension(spectrum.getDBSpectrumType().getDimensionality());
    				  if(spectrum.getDBSpectrumType().getDimensionality()==1)
    					  nmr[i].setExperiment("1d");
    				  else
    					  nmr[i].setExperiment(spectrum.getSpectrumTypeName());
    				  nmr[i].setFrequency(spectrum.getFrequency());
    				  if(spectrum.getJcamp()!=null)
    					  nmr[i].setJcamp(new String(spectrum.getJcamp()));
    				  String[] nuclei=new String[spectrum.getDBSpectrumType().getDimensionality()];
    				  for(int l=0;l<spectrum.getDBSpectrumType().getDimensionality();l++){
    					  nuclei[l]=spectrum.getDBSpectrumType().getDBIsotope(l+1).getElementSymbol();
    				  }
    				  nmr[i].setNucleus(nuclei);
    				  nmr[i].setSolvent(spectrum.getSolvent());
    				  if(spectrum.getRawData()!=null)
    					  nmr[i].setZip(new String(spectrum.getRawData()));
    				  List<DBSignal> shifts = spectrum.getDBSignals();
    				  List<JsonExport.Nmr.Assignment> assignments = new ArrayList<JsonExport.Nmr.Assignment>();
    				  for(DBSignal signal : shifts){
    					  for(DBAtom atom : signal.getAtoms()){
    						  JsonExport.Nmr.Assignment assignment=nmr[i].new Assignment();
    						  assignment.setAtom(atom.getMdlNumberPlus1());
    						  assignment.setDelta(signal.getDBShift(1).getValue());
    						  assignment.setMultiplicity(signal.getMultiplicity());
    						  assignments.add(assignment);
    					  }
    				  }
    				  nmr[i].setAssignment(assignments.toArray(new JsonExport.Nmr.Assignment[0]));
    				  i++;
    			  }
    			  export.setNmr(nmr);
    			  ObjectMapper mapper = new ObjectMapper();
    			  mapper.writeValue(fos,export);
    			  fos.close();
              	}catch(Exception ex){
            		ex.printStackTrace();
            	}
    		  }

          }

          private static void doHs(String[] args) throws Exception {
        	  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
        	  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
        	  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
    		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE where MOLECULE_ID=20182362");
    	        HOSECodeGenerator hcg = new HOSECodeGenerator();
    	        ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
    		  while (rs.next()) {
    			  try {
    				  DBMolecule mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
    				  IAtomContainer molwithh=mol.getAsCDKMolecule(1);
    				  for(IAtom atom : molwithh.atoms()) {
    					  if(atom.getSymbol().equals("C")) {
    						  int bondsum=0;
    						  for(IBond bond : molwithh.getConnectedBondsList(atom)){
    						          bondsum+=bond.getOrder().ordinal();
    						  }
    						  if(bondsum<4) {
    							  for(int i=bondsum;i<4;i++) {
    								  IAtom h=molwithh.getBuilder().newAtom();
    								  h.setSymbol("H");
    								  molwithh.addAtom(h);
    								  IBond b=molwithh.getBuilder().newBond();
    								  b.setAtom(h, 0);
    								  b.setAtom(atom, 1);
    								  b.setOrder(IBond.Order.SINGLE);
    								  molwithh.addBond(b);
    								  DBAtom dbatom=new DBAtom(h, molwithh, mol.getMoleculeId(), hcg.getHOSECode(molwithh, h, 6), ehcg.getHOSECode(molwithh, h, 6), false, 1, 0);
    								  dbatom.save();
    								  DBBond bond=new DBBond(b,false);
    								  bond.save();
    								  DBBondDBAtom bondatom=new DBBondDBAtom();
    								  bondatom.setAtomId(dbatom.getAtomId());
    								  bondatom.setBondId(bond.getBondId());
    								  bondatom.setOrderNumber(0);
    								  bondatom.save();
    								  bondatom=new DBBondDBAtom();
    								  bondatom.setAtomId(mol.getAtomsAsArrayInMdlOrder()[molwithh.getAtomNumber(atom)].getAtomId());
    								  bondatom.setBondId(bond.getBondId());
    								  bondatom.setOrderNumber(1);
    								  bondatom.save();
    							  }
    						  }else if(bondsum>4){
    							  int i=bondsum-4;
								  DBAtom[] localatoms = mol.getAtomsAsArrayInMdlOrder();
								  for(DBAtom localatom : localatoms) {
									  if(localatom.getConnectedAtoms().contains(localatoms[molwithh.getAtomNumber(atom)])) {
										  DBBondPeer.doDelete(localatom.getBonds().get(0));
										  DBAtomPeer.doDelete(localatom);
										  i--;
	    								  if(i==0)
	    									  break;
									  }
								  }
    						  }
    					  }
    				  }
    				  mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
    				  for(DBCanonicalName canname: mol.getDBCanonicalNames()) {
    					  if(canname.getCanonicalNameTypeId()==7) {
    						  canname.setName(GeneralUtils.generateTucan(mol.getAsCDKMolecule(0)));
    						  canname.save();
    					  }
    				  }
    			  }catch(Exception ex) {
    				  ex.printStackTrace();
    			  }
    		  }
          }
    				  
    				  
          private static void correctOm(String[] args) throws Exception{
        	  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
        	  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
        	  config.initialize();
			  Connection dbconn = Torque.getConnection();
			  Statement stmt = dbconn.createStatement();
    		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE where MOLECULE_ID=20182362");
    	      HOSECodeGenerator hcg = new HOSECodeGenerator();
    	      ExtendedHOSECodeGenerator ehcg = new ExtendedHOSECodeGenerator();
    		  while (rs.next()) {
    			  DBMolecule dbmol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
    			  System.out.println(new String(dbmol.getSmilesString())+" "+dbmol.getFormula()+" "+dbmol.getFp0()+" "+dbmol.getFp1()+" "+dbmol.getFp2()+" "+dbmol.getFp3()+" "+dbmol.getFp4()+" "+dbmol.getFp5()+" "+dbmol.getFp6()+" "+dbmol.getFp7()+" "+dbmol.getFp8()+" "+dbmol.getFp9()+" "+dbmol.getFp10()+" "+dbmol.getFp11()+" "+dbmol.getFp12()+" "+dbmol.getFp13()+" "+dbmol.getFp14()+" "+dbmol.getFp15());
    			  AtomUtils.doSimpleSaturation(dbmol.getAsCDKMolecule(1),dbmol);
    			  IAtomContainer ac=dbmol.getAsCDKMolecule(1);
    			  AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(ac);
    			  int i=0;
    			  for(IAtom atom: ac.atoms()) {
    				  DBAtom dbatom =dbmol.getAtomAt(i);
    				  if(atom.getFlag(CDKConstants.ISAROMATIC)!=(dbatom.getIsAromatic().equals(NmrshiftdbConstants.TRUE))) {
    					  dbatom.setIsAromatic(atom.getFlag(CDKConstants.ISAROMATIC) ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
    					  //dbatom.save();
    					  System.out.println("changed "+i);
    				  }
    			  }
    			  dbmol.setFingerprint(dbmol.getAsCDKMoleculeWithoutHAndCount());
    			  dbmol.setFormula(MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(ac)));
    			  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
  		    	  GeneralUtils.getExistingMolecule(dbmol.getAsCDKMoleculeWithoutH(), ac, new boolean[ac.getBondCount()], null, dbmol.getStructureFile(1,false), ssb);
  		    	  dbmol.setSmilesString(ssb.string1.getBytes());    			  
  		    	  System.out.println(new String(dbmol.getSmilesString())+" "+dbmol.getFormula()+" "+dbmol.getFp0()+" "+dbmol.getFp1()+" "+dbmol.getFp2()+" "+dbmol.getFp3()+" "+dbmol.getFp4()+" "+dbmol.getFp5()+" "+dbmol.getFp6()+" "+dbmol.getFp7()+" "+dbmol.getFp8()+" "+dbmol.getFp9()+" "+dbmol.getFp10()+" "+dbmol.getFp11()+" "+dbmol.getFp12()+" "+dbmol.getFp13()+" "+dbmol.getFp14()+" "+dbmol.getFp15());
  		    	  //dbmol.save();
  		    	  IAtomContainer molWithH=dbmol.getAsCDKMolecule(1);
                  AtomUtils.makeUpDownBonds(molWithH);
                  AtomUtils.doReplacement(molWithH);
                  DBAtom[] dbatoms=dbmol.getAtomsAsArrayInMdlOrder();
                  Iterator it=molWithH.atoms().iterator();
                  IAtomContainer molWithoutH=AtomUtils.convertImplicitToExplicitHydrogens(molWithH);
                  i=0;
                  while(it.hasNext()){
                      IAtom atom=(IAtom)it.next();
                      if (GeneralUtils.getAtomsWithHoseCode().contains(atom.getSymbol())) {
                          String hoseCode = hcg.getHOSECode(molWithH, atom, 6,false);
                          if(!atom.getSymbol().equals("H")){
                                  hoseCode = hcg.getHOSECode(molWithoutH, molWithoutH.getAtom(molWithH.getAtomNumber(atom)), 6,false);
                          }
                          dbatoms[i].setHoseCode(hoseCode.getBytes());
                          try{
                                  dbatoms[i].setHoseCodeWithRings(ehcg.getHOSECode(molWithH, atom, 6,true).getBytes());
                          }catch(Exception ex){
                                  dbatoms[i].setHoseCodeWithRings(hcg.getHOSECode(molWithH, atom, 6,true).getBytes());
                                  System.out.println("Fehler");
                          }
                          System.out.println(i+" "+dbatoms[i].getHoseCode()+" "+dbatoms[i].getHoseCodeWithRings());
                          dbatoms[i].save();
                      }
                      i++;
                  }

    		  }        	  
          }


          private static void readOm(String[] args) throws Exception {
        	  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
        	  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
        	  config.initialize();
        	  File file = new File(args[0]);
        	  TurbineUser user = null;
        	  Criteria crit = new Criteria();
        	  crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
        	  List v = NmrshiftdbUserPeer.doSelect(crit);
        	  //Look if the bulkoad user exists
        	  if (v.size() > 0) {
        		  user = (TurbineUser) v.get(0); 
        	  } else {
        		  throw new Exception("no bulkload user");
        	  }
        	  String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
        	  String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
        	  Properties nmrprops = new Properties();
        	  Properties jetspeedprops = new Properties();
        	  nmrprops.load(new FileInputStream(nmrpropsFile));
        	  jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
        	  System.err.println("Reading "+file.getName());
        	  BufferedReader buffRead = new BufferedReader(new FileReader(file));
        	  String line = "";
        	  StringBuffer newMdl=new StringBuffer();
        	  while ((line = buffRead.readLine()) != null) {
        		  if(line.equals("$$$$")){
        			  try{
        				  if(newMdl.toString().startsWith("\n\n"))
        					  newMdl=new StringBuffer(newMdl.substring(1));
        				  MDLV2000Reader mdlreader = new MDLV2000Reader(new StringReader(newMdl.toString()));
        				  IAtomContainer molWithH = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        				  molWithH = (IAtomContainer)mdlreader.read(molWithH);
        			      mdlreader.close();
						  for(IAtom atom : molWithH.atoms()){
							  atom.setProperty("visible","true");
						  }
        			      try{
        			    	  	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
        			    	  	Isotopes.getInstance().configureAtoms(molWithH);
        			      }catch(Exception ex){
        			    	  System.out.println(ex.getMessage());
        			      }
        			      ElectronDonation model       = ElectronDonation.cdk();
        			      CycleFinder      cycles      = Cycles.cdkAromaticSet();
        			      Aromaticity      aromaticity = new Aromaticity(model, cycles);
       			    	  aromaticity.apply(molWithH);
        				  IteratingSDFReader reader = new IteratingSDFReader(new StringReader(newMdl.toString()+"\r\n$$$$"), DefaultChemObjectBuilder.getInstance());
        				  IAtomContainer molwithprops = reader.next();
        				  reader.close();
        				  Map<String, IAtom> assignments=new HashMap<>();
        				  //find metal
        				  IAtom metal=null;
        				  for(IAtom atom : molWithH.atoms()) {
        					  List<String> list=new ArrayList<>(List.of("Zr","Co","Hf","Pt","Os","W","Ti","Ni"));
        					  if(list.contains(atom.getSymbol())) {
        						  metal=atom;
        						  break;
        					  }
        				  }
        				  System.out.println(metal.getSymbol());
        				  //set bond order zero for all bonds from metal to atoms with wrong valency and not first in an odd number ring
        				  AllRingsFinder arf = new AllRingsFinder();
        				  IRingSet rings=arf.findAllRings(molWithH);
        				  Iterator<IAtomContainer> itr = rings.atomContainers().iterator();
					      while(itr.hasNext()){
							if(itr.next().contains(metal))
								itr.remove();
						  }
					      IRingSet newrings=rings.getBuilder().newInstance(IRingSet.class);
					      int ringcount=1;
						  for(IAtom atom : molWithH.getConnectedAtomsList(metal)) {
        					  CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molWithH.getBuilder());    	
        					  IAtomType type = matcher.findMatchingAtomType(molWithH, atom);
        					  boolean isfirst=false;
        					  IAtomContainer thisring=null;
        					  for(IAtomContainer ring : rings.atomContainers()) {
        						  if(ring.contains(atom)){
								  thisring=ring;
							  }
							  if(ring.getAtomCount()%2==1 && ring.contains(atom) && ring.getAtomNumber(atom)==0) {
        							  isfirst=true;
        							  break;
        						  }        							  
        					  }
        					  int bondsum=0;
        					  for(IBond bond : atom.bonds()){
        						  bondsum+=bond.getOrder().ordinal();
        					  }
        					  if(type.getValency()!=bondsum && !isfirst && thisring!=null) {
        						  molWithH.getBond(metal,atom).setOrder(IBond.Order.ZERO);
        						  newrings.addAtomContainer(thisring);
							  }
        				  }
						  for(IAtom atom : molWithH.getConnectedAtomsList(metal)) {
							  for(IAtomContainer ring : newrings.atomContainers()) {
								  if(ring.getAtomCount()%2==1 && ring.contains(atom) && ring.getAtomNumber(atom)==0) {
									  IAtom newatom=atom.getBuilder().newInstance(IAtom.class, "H");
									  molWithH.addAtom(newatom);
									  ring.addAtom(newatom);
									  IBond newbond=atom.getBuilder().newInstance(IBond.class,newatom,atom,IBond.Order.SINGLE);
									  molWithH.addBond(newbond);
									  ring.addBond(newbond);
									  break;
								  }
								  if(atom==molWithH.getConnectedAtomsList(metal).get(0)){
									  for(IAtom atom2 : ring.atoms()){
										  atom2.setProperty("equivalence",ringcount);
									  }
									  ringcount++;
								  }
							  }
						  }
        	   	    	  for(IAtomContainer ring : newrings.atomContainers()) {
        	   	    		  IBond[] bonds=new IBond[ring.getBondCount()];
        	   	    		  int i=0;
        	   	    		  for(IBond bond : ring.bonds())
        	   	    			  bonds[i++]=bond;
        	   	    		  new SaturationChecker().newSaturate(bonds, ring);
        	   	    	  }
        	   	    	  SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
        	   	    	  subData.setMol(molWithH, false, false, false);
        	   	    	  Map<Integer,Integer> mapping=GeneralUtils.getTucanLabels(molWithH);
				          for(int key : mapping.keySet()) {
							AtomUtils.setChildElement(subData.getCMLMolecule().getAtom(key), SubmittingData.IDENTIFIER, Integer.toString(mapping.get(key)+1));
				          }
        				  if(molwithprops.getProperty("assignments")!=null){
        					  StringTokenizer st=new StringTokenizer((String)molwithprops.getProperty("assignments"),";\n");
        					  while(st.hasMoreTokens()){
        						  String line2=st.nextToken();
        						  if(line2.split("\\s+").length>=3){
									  Point2d point=new Point2d(Double.parseDouble(line2.split("\\s+")[1]),Double.parseDouble(line2.split("\\s+ *")[2]));
	        						  IAtom nearestatom=null;
	        						  double distance=Double.MAX_VALUE;
	        						  for(IAtom atom : subData.getMolOriginal().atoms()) {
	        							  double deltaX = point.x - atom.getPoint2d().x;
	        							  double deltaY = point.y - atom.getPoint2d().y;
	        							  double result = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	        							  if(result<distance) {
	        								  distance=result;
	        								  nearestatom=atom;
	        							  }
	        						  }
	        						  String label=st.nextToken().trim();
	        						  assignments.put(label,nearestatom);
	        					  }else{
	        						  st.nextToken();
	        					  }
        					  }
        				  }
        				  crit = new Criteria();
        				  crit.add(DBSpectrumTypePeer.NAME, "13C");
        				  v = DBSpectrumTypePeer.doSelect(crit);
        				  subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
        				  subData.setSpectrumKeywords("MPI-KoFo");
        				  //IBond[] bonds = subData.getMolWithH().getBonds();
        				  boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
        				  Iterator<IBond> it=molWithH.bonds().iterator();
        				  int c=0;
        				  while(it.hasNext()) {
        					  IBond bond=it.next();
        					  if (AtomUtils.isValidDoubleBondConfiguration(molWithH,bond,rings)) {
        						  isDoubleBondSpecified[c] = true;
        					  }
        					  c++;
        				  }
        				  subData.setDoubleBondConfiguration(true);
        				  subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
        				  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
        				  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
        				  subData.setSmilesNonChiralProv(ssb.string1);
        				  subData.setIsChiralProv(ssb.bool);
        				  if(ssb.bool){
        					  subData.setSmilesChiralProv(ssb.string2);
        				  }
        				  if(molwithprops.getProperty("C13 Shifts")!=null){
        					  StringTokenizer st=new StringTokenizer((String)molwithprops.getProperty("C13 Shifts"),";\n");
        					  Pattern p1= Pattern.compile("[0-9]+\\)");
        					  Pattern p2= Pattern.compile("[0-9]+\\.[0-9]+");
        					  while(st.hasMoreTokens()){
        						  String line2=st.nextToken();
        						  Matcher m=p1.matcher(line2);
        						  if(m.find()){
        							  String atomnumber=m.group().substring(0,m.group().length()-1);
        							  Matcher m2=p2.matcher(line2);
        							  if(m2.find()){
        							  double value=Double.parseDouble(m2.group());
        							  CMLPeakList signals=subData.getSignalstable();
        							  boolean added=false;
        							  for(int l=0;l<signals.getPeakElements().size();l++){
        								  if(signals.getPeakElements().get(l).getXValue()==value){
        									  signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(assignments.get(atomnumber).getIndex())));
        									  added=true;
        								  }
        							  }
        							  if(!added){
        								  CMLPeak peak = new CMLPeak();
        								  peak.setXValue(value);
        								  peak.setYValue(0);
        								  peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(assignments.get(atomnumber).getIndex())));
        								  subData.addToSignalstable(peak);
        							  }
        							  if(m.find()){
        								  atomnumber=m.group().substring(0,m.group().length()-1);
        								  m2.find();
        								  value=Double.parseDouble(m2.group());
        								  added=false;
        								  for(int l=0;l<signals.getPeakElements().size();l++){
        									  if(signals.getPeakElements().get(l).getXValue()==value){
        										  signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(assignments.get(atomnumber).getIndex())));
        										  added=true;
        									  }
        								  }
        								  if(!added){
        									  CMLPeak peak = new CMLPeak();
        									  peak.setXValue(value);
        									  peak.setYValue(0);
        									  peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(assignments.get(atomnumber).getIndex())));
        									  subData.addToSignalstable(peak);
        								  }
        							  }}
        						  }
        					  }
        					  subData.setMeasured();
        					  List<DBConditionType> conds = subData.getConditions();
        					  for(int k=0;k<conds.size();k++){
        						  DBConditionType cond=conds.get(k);
        						  if(cond.getConditionTypeId()==1 && molwithprops.getProperty("Temp-13C")!=null && !((String)molwithprops.getProperty("Temp-13C")).trim().equals("GT")){
        							  cond.setCurrentValue(((String)molwithprops.getProperty("Temp-13C")).trim().substring(0, 3));
        						  }else if(cond.getConditionTypeId()==1 && molwithprops.getProperty("Temp-13C")!=null && (((String)molwithprops.getProperty("Temp-13C")).trim().equals("GT") || ((String)molwithprops.getProperty("Temp-13C")).trim().equals("RT"))){
        							  cond.setCurrentValue("298");
        						  }else if(cond.getConditionTypeId()==2 && molwithprops.getProperty("LM-13C")!=null){
        							  cond.setCurrentValue(((String)molwithprops.getProperty("LM-13C")).trim());
        						  }else{
        							  cond.setCurrentValue(new String("Unreported"));
        						  }
        					  }
        					  subData.setConditions(conds);
            				  subData.setComment(molwithprops.getProperty("Entry number")+ " "+molwithprops.getProperty("Origin-C13")+ " "+molwithprops.getProperty("Sample No."));
            				  crit = new Criteria();
            				  crit.add(DBSpectrumPeer.COMMENT, subData.getComment());
			                  v = DBSpectrumPeer.doSelect(crit);
						      if(v.size()==0)
								subData.save(user.getUserId(), "false_om", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true,true);
        				  }
        				  subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
        				  subData.setMol(molWithH, false, false, false);
        	   	    	  for(int key : mapping.keySet()) {
	        	   	    		AtomUtils.setChildElement(subData.getCMLMolecule().getAtom(key), SubmittingData.IDENTIFIER, Integer.toString(mapping.get(key)+1));
	              	   	  }
        	   	    	  crit = new Criteria();
        				  crit.add(DBSpectrumTypePeer.NAME, "1H");
        				  v = DBSpectrumTypePeer.doSelect(crit);
        				  subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
        				  subData.setSpectrumKeywords("MPI-KoFo");
        				  subData.setDoubleBondConfiguration(true);
        				  subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
        				  ssb = new StringAndStringAndBoolean();
        				  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
        				  subData.setSmilesNonChiralProv(ssb.string1);
        				  subData.setIsChiralProv(ssb.bool);
        				  if(ssb.bool){
        					  subData.setSmilesChiralProv(ssb.string2);
        				  }
        				  if(molwithprops.getProperty("H1 shifts")!=null){
        					  StringTokenizer st=new StringTokenizer((String)molwithprops.getProperty("H1 shifts"),";\n");
        					  Pattern p1= Pattern.compile("(^|[^,])[0-9]+[ a-z]?\\)");
        					  Pattern p2= Pattern.compile("[0-9]+\\.[0-9]+");
        					  Pattern p3= Pattern.compile("[0-9]+[a-z]?,[0-9PR]+[a-z]?\\)");
        					  List<IAtom> usedatoms=new ArrayList<>();
        					  IAtomContainer ac=subData.getMolWithH();
        					  Map<String,IAtom> atomnames=new HashMap<>();
        					  while(st.hasMoreTokens()){
        						  String line2=st.nextToken();
        						  Matcher m=p1.matcher(line2);
        						  if(m.find()){
        							  String atomnumberstringlong=m.group().substring(0,m.group().length()-1);
        							  String atomnumberstring=atomnumberstringlong.trim();
        							  if(!Character.isDigit(atomnumberstringlong.charAt(atomnumberstringlong.length()-1)))
        								  atomnumberstring=atomnumberstringlong.substring(0,atomnumberstringlong.length()-1).trim();
        							  int atomnumber=Integer.parseInt(atomnumberstring);
        							  Matcher m2=p2.matcher(line2);
        							  if(m2.find()){
        								  double value=Double.parseDouble(m2.group());
	        							  CMLPeakList signals=subData.getSignalstable();
	        							  IAtom realatom=null;
	        							  if(assignments.get(atomnumberstring)!=null){
	        								  for(IAtom atom : ac.getConnectedAtomsList(ac.getAtom(assignments.get(atomnumberstring).getIndex()))){
		        								  if(atom.getSymbol().equals("H") && !usedatoms.contains(atom)){
		        									  realatom=atom;
		        									  usedatoms.add(atom);
		        									  atomnames.put(atomnumberstringlong.trim(),atom);
		        									  break;
		        								  }
	        								  }
	        								  boolean added=false;
	        								  for(int l=0;l<signals.getPeakElements().size();l++){
	        									  if(signals.getPeakElements().get(l).getXValue()==value){
	        										  signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),realatom));
	        										  added=true;
	        									  }
	        								  }
	        								  if(!added){
	        									  CMLPeak peak = new CMLPeak();
	        									  peak.setXValue(value);
	        									  peak.setYValue(0);
	        									  peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),realatom));
	        									  subData.addToSignalstable(peak);
	        								  }
	        							  }
	        						  }
        						  }
        					  }
        					  st=new StringTokenizer((String)molwithprops.getProperty("H1 shifts"),"\n");
        					  try{
						          while(st.hasMoreTokens()){
	        						  String line2=st.nextToken();
	        						  Matcher m2=p2.matcher(line2);
	        						  if(line.length()>5 && !line2.substring(0,5).equals("     "))
									  m2.find();
	        						  if(m2.find()){
	        							  double couplingvalue=Double.parseDouble(m2.group());
									  Matcher m3=p3.matcher(line2);
	        							  m3.find();
	        							  String couplingline=m3.group();
	        							  StringTokenizer st2=new StringTokenizer(couplingline,",)");
	
	        							  Element coupling = new Element(SubmittingData.COUPLING,"http://www.nmrshiftdb.org/");
	        							  int atomnumber=0;
	        							  String atomname=st2.nextToken();
	        							  if(!atomname.matches("[0-9]*")){
	        								  for(IAtom atom : ac.atoms()){
	        									  if(atom.getSymbol().equals(atomname)){
	        										  atomnumber=ac.getAtomNumber(atom);
	        										  break;
	        									  }
	        								  }
	        							  }else{
	        								  atomnumber=ac.getAtomNumber(atomnames.get(atomname));
	        							  }
	        							  coupling.addAttribute(new Attribute("otherAtom",atomnumber+""));
	        							  coupling.addAttribute(new Attribute("value",Double.toString(couplingvalue)));
	        							  atomnumber=0;
	        							  atomname=st2.nextToken();
	        							  if(!atomname.matches("[0-9]*")){
	        								  for(IAtom atom : ac.atoms()){
	        									  if(atom.getSymbol().equals(atomname)){
	        										  atomnumber=ac.getAtomNumber(atom);
	        										  break;
	        									  }
	        								  }
	        							  }else{
	        								  atomnumber=ac.getAtomNumber(atomnames.get(atomname));
	        							  }
	        							  subData.getCMLMolecule().getAtoms().get(atomnumber).appendChild(coupling);
	        						  }
	        					  }
							  }catch(Exception ex){
								  //couplings are sometimes oddly written
							  }
						      subData.setMeasured();
							  List<DBConditionType> conds = subData.getConditions();
	        				  for(int k=0;k<conds.size();k++){
	        					  DBConditionType cond=conds.get(k);
	        					  if(cond.getConditionTypeId()==1 && molwithprops.getProperty("Temp-1H")!=null && !((String)molwithprops.getProperty("Temp-1H")).trim().equals("GT")){
	        						  cond.setCurrentValue(((String)molwithprops.getProperty("Temp-1H")).trim().substring(0, 3));
								  }else if(cond.getConditionTypeId()==1 && molwithprops.getProperty("Temp-13C")!=null && (((String)molwithprops.getProperty("Temp-13C")).trim().equals("GT") || ((String)molwithprops.getProperty("Temp-13C")).trim().equals("RT"))){
									  cond.setCurrentValue("298");
	       						  }else if(cond.getConditionTypeId()==2 && molwithprops.getProperty("LM-1H")!=null){
	       							  cond.setCurrentValue(((String)molwithprops.getProperty("LM-1H")).trim());
	       						  }else{
	       							  cond.setCurrentValue(new String("Unreported"));
	       						  }
	       					  }
	       					  subData.setConditions(conds);
	           				  subData.setComment(molwithprops.getProperty("Entry number")+ " "+molwithprops.getProperty("Origin-H1")+ " "+molwithprops.getProperty("Sample No."));
	           				  crit = new Criteria();
	                          crit.add(DBSpectrumPeer.COMMENT, subData.getComment());
				              v = DBSpectrumPeer.doSelect(crit);
							  if(v.size()==0)
							    subData.save(user.getUserId(), "false_om", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true,true);
        				  }
        				  System.err.println("Wrote "+molwithprops.getProperty("Entry number"));
        			  } catch (Exception ex) {
        				  ex.printStackTrace();
        			  }
        			  newMdl=new StringBuffer("");
        		  }else{
        			  newMdl.append(line+"\n");
        		  }
        	  }
        	  buffRead.close();
          }

          private static void readOmGeitner(String[] args) throws Exception {
        	  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
        	  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
        	  config.initialize();
        	  File file = new File("MetalComplexes_31P.csv");
        	  TurbineUser user = null;
        	  Criteria crit = new Criteria();
        	  crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
        	  List v = NmrshiftdbUserPeer.doSelect(crit);
        	  //Look if the bulkoad user exists
        	  if (v.size() > 0) {
        		  user = (TurbineUser) v.get(0); 
        	  } else {
        		  throw new Exception("no bulkload user");
        	  }
        	  String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
        	  String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
        	  Properties nmrprops = new Properties();
        	  Properties jetspeedprops = new Properties();
        	  nmrprops.load(new FileInputStream(nmrpropsFile));
        	  jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
        	  System.err.println("Reading "+file.getName());
        	  BufferedReader buffRead = new BufferedReader(new FileReader(file));
        	  String line = "";
        	  buffRead.readLine();
        	  while ((line = buffRead.readLine()) != null) {
        		  String filename="mol-files/"+line.split(",")[0]+".mol";
        		  //if(line.split(",")[0].equals("HBP-Cha14-CMP1798")){
				  try {
        				  MDLV2000Reader mdlreader = new MDLV2000Reader(new FileReader(new File(filename)));
        				  IAtomContainer molWithH = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        				  molWithH = (IAtomContainer)mdlreader.read(molWithH);
        			      mdlreader.close();
				      for(IBond bond : molWithH.bonds()){if(bond.getOrder()==null)bond.setOrder(IBond.Order.ZERO);}
						  for(IAtom atom : molWithH.atoms()){
							  atom.setProperty("visible","true");
							  atom.setImplicitHydrogenCount(0);
							  if(atom.getPoint2d()==null)
								  atom.setPoint2d(new Point2d(atom.getPoint3d().x,atom.getPoint3d().y));
							  if(atom.getSymbol().equals("H") && !molWithH.getConnectedAtomsList(atom).get(0).getSymbol().equals("P"))
                                                                  atom.setProperty("visible",NmrshiftdbConstants.FALSE);
						  }
        			      try{
        			    	  	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
        			    	  	Isotopes.getInstance().configureAtoms(molWithH);
        			      }catch(Exception ex){
        			    	  System.out.println(ex.getMessage());
        			      }
        			      ElectronDonation model       = ElectronDonation.cdk();
        			      CycleFinder      cycles      = Cycles.cdkAromaticSet();
        			      Aromaticity      aromaticity = new Aromaticity(model, cycles);
       			    	  aromaticity.apply(molWithH);
        				  //find metal
        				  IAtom metal=null;
        				  for(IAtom atom : molWithH.atoms()) {
        					  List<String> list=new ArrayList<>(List.of("Zr","Co","Hf","Pt","Os","W","Ti","Ni","Fe","V","Fe", "Zn", "Pt", "W",  "Au", "Ti", "Pd", "Rh",  "Mo", "Mn", "Ni", "Os", "Ru", "Re", "Cr", "Ag", "Nb", "Ta", "Hg",  "Y",  "V"));
        					  if(list.contains(atom.getSymbol())) {
        						  metal=atom;
        						  break;
        					  }
        				  }
        				  IAtom patom=null;
        				  for(IAtom atom : molWithH.atoms()) {
        					  if(atom.getSymbol().equals("P"))
        						  patom=atom;
        				  }
        				  System.out.println(metal.getSymbol());
        				  //set bond order zero for all bonds from metal to atoms with wrong valency and not first in an odd number ring
        				  AllRingsFinder arf = new AllRingsFinder();
        				  IRingSet rings=arf.findAllRings(molWithH);
        				  Iterator<IAtomContainer> itr = rings.atomContainers().iterator();
					      while(itr.hasNext()){
							if(itr.next().contains(metal))
								itr.remove();
						  }
					      IRingSet newrings=rings.getBuilder().newInstance(IRingSet.class);
					      int ringcount=1;
						  for(IAtom atom : molWithH.getConnectedAtomsList(metal)) {
        					  CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molWithH.getBuilder());    	
        					  IAtomType type = matcher.findMatchingAtomType(molWithH, atom);
        					  boolean isfirst=false;
        					  IAtomContainer thisring=null;
        					  for(IAtomContainer ring : rings.atomContainers()) {
        						  if(ring.contains(atom)){
								  thisring=ring;
							  }
							  if(ring.getAtomCount()%2==1 && ring.contains(atom) && ring.getAtomNumber(atom)==0) {
        							  isfirst=true;
        							  break;
        						  }        							  
        					  }
        					  int bondsum=0;
        					  for(IBond bond : atom.bonds()){
        						  bondsum+=bond.getOrder().ordinal();
        					  }
        					  if(type.getValency()!=bondsum && !isfirst && thisring!=null) {
        						  molWithH.getBond(metal,atom).setOrder(IBond.Order.ZERO);
        						  newrings.addAtomContainer(thisring);
							  }
        				  }
						  for(IAtom atom : molWithH.getConnectedAtomsList(metal)) {
							  for(IAtomContainer ring : newrings.atomContainers()) {
								  if(ring.getAtomCount()%2==1 && ring.contains(atom) && ring.getAtomNumber(atom)==0) {
									  IAtom newatom=atom.getBuilder().newInstance(IAtom.class, "H");
									  molWithH.addAtom(newatom);
									  ring.addAtom(newatom);
									  IBond newbond=atom.getBuilder().newInstance(IBond.class,newatom,atom,IBond.Order.SINGLE);
									  molWithH.addBond(newbond);
									  ring.addBond(newbond);
									  break;
								  }
								  if(atom==molWithH.getConnectedAtomsList(metal).get(0)){
									  for(IAtom atom2 : ring.atoms()){
										  atom2.setProperty("equivalence",ringcount);
									  }
									  ringcount++;
								  }
							  }
						  }
        	   	    	  for(IAtomContainer ring : newrings.atomContainers()) {
        	   	    		  IBond[] bonds=new IBond[ring.getBondCount()];
        	   	    		  int i=0;
        	   	    		  for(IBond bond : ring.bonds())
        	   	    			  bonds[i++]=bond;
        	   	    		  new SaturationChecker().newSaturate(bonds, ring);
        	   	    	  }
        	   	    	  SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
        	   	    	  subData.setMol(molWithH, false, false, false);
        	   	    	  Map<Integer,Integer> mapping=GeneralUtils.getTucanLabels(molWithH);
				          for(int key : mapping.keySet()) {
							AtomUtils.setChildElement(subData.getCMLMolecule().getAtom(key), SubmittingData.IDENTIFIER, Integer.toString(mapping.get(key)+1));
				          }
        				  crit = new Criteria();
        				  crit.add(DBSpectrumTypePeer.NAME, "31P");
        				  v = DBSpectrumTypePeer.doSelect(crit);
        				  subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
        				  subData.setSpectrumKeywords("Ilm-Om");
        				  boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
        				  Iterator<IBond> it=molWithH.bonds().iterator();
        				  int c=0;
        				  while(it.hasNext()) {
        					  IBond bond=it.next();
        					  if (AtomUtils.isValidDoubleBondConfiguration(molWithH,bond,rings)) {
        						  isDoubleBondSpecified[c] = true;
        					  }
        					  c++;
        				  }
        				  subData.setDoubleBondConfiguration(true);
        				  subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
        				  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
        				  DBMolecule existingmol=GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
					  System.out.println("existingmol "+ (existingmol==null ? "no" : existingmol.getMoleculeId()));
        				  subData.setSmilesNonChiralProv(ssb.string1);
        				  subData.setIsChiralProv(ssb.bool);
        				  if(ssb.bool){
        					  subData.setSmilesChiralProv(ssb.string2);
        				  }
        				  CMLPeak peak = new CMLPeak();
        				  peak.setXValue(line.split(",")[3]);
  		            		peak.setYValue(0);
  		            		peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),patom));
  		            		subData.addToSignalstable(peak);
        					  subData.setMeasured();
        					  List<DBConditionType> conds = subData.getConditions();
        					  for(int k=0;k<conds.size();k++){
        						  DBConditionType cond=conds.get(k);
        						  if(cond.getConditionTypeId()==1){
        							  cond.setCurrentValue("298");
        						  }else if(cond.getConditionTypeId()==2){
        							  cond.setCurrentValue(line.split(",")[2]);
        						  }else{
        							  cond.setCurrentValue(new String("Unreported"));
        						  }
        					  }
        					  subData.setConditions(conds);
            				  subData.setComment("ilm "+line.split(",")[0]);
            				  crit = new Criteria();
            				  crit.add(DBSpectrumPeer.COMMENT, subData.getComment());
			                  v = DBSpectrumPeer.doSelect(crit);
						      if(v.size()==0 && existingmol==null)
								subData.save(user.getUserId(), "false_om", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true,true);
        				  System.err.println("Wrote "+line.split(",")[0]);
        			  } catch (Exception ex) {
        				  ex.printStackTrace();
				  }
        		  }
        	  
        	  buffRead.close();
          }

          
          public static void writeDois(String[] args) throws Exception{
    		  Torque.init(args[1] + "../conf/jetspeed/WEB-INF/conf/Torque.properties");
    		  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
    		  config.initialize();
              String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties";
              Properties velocityprops = new Properties();
              velocityprops.load(new FileInputStream(nmrpropsFile));
    		  Velocity.init(velocityprops);
    		  Connection dbconn = Torque.getConnection();
    		  Statement stmt = dbconn.createStatement();
    		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE");
    		  while(rs.next()){
    			  DBMolecule mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
    			  if(mol.hasValidPublicSpectra()) {
	    			  if(mol.getDoi()==null || mol.getDoi().equals("")) {
		    			  VelocityContext context = new VelocityContext();
		    		      context.put("year", mol.getDate().getYear()+1900);
		    		      context.put("name", mol.getChemicalNamesAsOneStringWithFallbackEasy());
		    		      context.put("molid", mol.getMoleculeId());
		    			  File att=new File("/tmp/metadata_molecule_"+mol.getMoleculeId()+".xml");
		    			  Writer writer = new FileWriter (att);
		    		      Velocity.mergeTemplate("src/vmtemplates/doi_mol.vm", "UTF-8", context, writer);
		    		      writer.flush();
		    			  // Create the attachment
		    			  EmailAttachment attachment = new EmailAttachment();
		    			  attachment.setPath("/tmp/metadata_molecule_"+mol.getMoleculeId()+".xml");
		    			  attachment.setDisposition(EmailAttachment.ATTACHMENT);
		    			  attachment.setDescription("metedata");
		    			  attachment.setName("Metadata");
		    			  // Create the email message
		    			  MultiPartEmail email = new MultiPartEmail();
		    			  //email.setHostName(GeneralUtils.getSmtpServer((ServletConfig)null));
		    			  email.addTo("doimailreg@ub.uni-koeln.de", "DOI");
		    			  email.addTo("stefhk3@web.de","DOI");
		    			  email.setFrom("stefhk3@web.de", "Stefan Kuhn");
		    			  email.setSubject("DOI");
		    			  email.setMsg("https://nmrshiftdb.nmr.uni-koeln.de/molecule/"+mol.getMoleculeId());
		
		    			  // add the attachment
		    			  email.attach(attachment);
		
		    			  // send the email
		    			  email.send();
		    			  mol.setDoi("10.18716/nmrshiftdb2/"+mol.getMoleculeId());
		    			  mol.save();
	    			  }
	    			  Map<String,List<DBSpectrum>> spectralists = DetailsAction.expandSpectraByKeyword(mol.selectSpectra());
	    			  for(int i=0;i<spectralists.keySet().size();i++) {
	    				  if(((String)spectralists.keySet().toArray()[i]).split("/").length>1){
	                        String datasetkeyword=((String)spectralists.keySet().toArray()[i]).split("/")[0];
	                        String datasetsolvent=((String)spectralists.keySet().toArray()[i]).split("/")[1];
	                        Criteria crit= new Criteria();
	                        crit.add(DBConditionPeer.VALUE,datasetsolvent);
	    	    		      if(datasetkeyword.indexOf("_")==-1)
	    	    		    	  crit.add(DBKeywordPeer.KEYWORD, datasetkeyword);
	    	    		      else
	    	    		    	  crit.add(DBKeywordPeer.KEYWORD, datasetkeyword.substring(0,datasetkeyword.indexOf("_")));	    	    		      crit.addJoin(DBSpectrumDBConditionPeer.SPECTRUM_ID,  DBSpectrumPeer.SPECTRUM_ID);
	                        crit.add(DBDatasetDoiPeer.MOLECULE_ID, mol.getMoleculeId());
	                        crit.addJoin(DBDatasetDoiPeer.CONDITION_ID,  DBConditionPeer.CONDITION_ID);
	                        crit.addJoin(DBDatasetDoiPeer.DATASET_DOI_ID, DBDatasetDoiDBKeywordPeer.DATASET_DOI_ID);
	                        crit.addJoin(DBDatasetDoiDBKeywordPeer.KEYWORD_ID, DBKeywordPeer.KEYWORD_ID);
	                        List<DBDatasetDoi> dois=DBDatasetDoiPeer.doSelect(crit);
	                        if(dois.size()==0 && datasetsolvent.matches(".*\\(.*\\)")) {
	      	    			  VelocityContext context = new VelocityContext();
	    	    		      context.put("name", mol.getChemicalNamesAsOneStringWithFallbackEasy());
	    	    		      context.put("molid", mol.getMoleculeId());
	    	    		      context.put("solvent", datasetsolvent);
	    	    		      context.put("keyword", datasetkeyword);
	    	    		      String shortkeyword=GeneralUtils.replace(datasetkeyword, " ", "_").toLowerCase();
	    	    		      context.put("datasetname", (String)spectralists.keySet().toArray()[i]);
	    	    		      String shortsolvent=datasetsolvent.substring(datasetsolvent.indexOf("(")+1, datasetsolvent.length()-1).toLowerCase();
	    	    		      if(shortsolvent.contains(", "))
	    	    		    	  shortsolvent=shortsolvent.substring(0,  shortsolvent.indexOf(","));
	                          String doistring="10.18716/nmrshiftdb2/"+mol.getMoleculeId()+"/"+shortkeyword+"_"+shortsolvent;
	    	    		      context.put("doi", doistring);
	    	    		      crit=new Criteria();
	    	    		      crit.add(DBConditionPeer.VALUE, datasetsolvent);
	    	    		      crit.add(DBSpectrumPeer.MOLECULE_ID, mol.getMoleculeId());
	    	    		      if(datasetkeyword.indexOf("_")==-1)
	    	    		    	  crit.add(DBKeywordPeer.KEYWORD, datasetkeyword);
	    	    		      else
	    	    		    	  crit.add(DBKeywordPeer.KEYWORD, datasetkeyword.substring(0,datasetkeyword.indexOf("_")));	    	    		      crit.addJoin(DBSpectrumDBConditionPeer.SPECTRUM_ID,  DBSpectrumPeer.SPECTRUM_ID);
	    	    		      crit.addJoin(DBSpectrumDBConditionPeer.CONDITION_ID,  DBConditionPeer.CONDITION_ID);
	    	    		      crit.addJoin(DBSpectrumDBKeywordPeer.SPECTRUM_ID,  DBSpectrumPeer.SPECTRUM_ID);
	    	    		      crit.addJoin(DBSpectrumDBKeywordPeer.KEYWORD_ID,  DBKeywordPeer.KEYWORD_ID);
	    	    		      List<DBSpectrum> spectrum = DBSpectrumPeer.doSelect(crit);
	    	    		      context.put("year", spectrum.get(0).getDate().getYear()+1900);
	    	    		      context.put("authorname",spectrum.get(0).getNmrshiftdbUser().getLastName()+", "+spectrum.get(0).getNmrshiftdbUser().getFirstName());
	    	    		      context.put("affiliation", spectrum.get(0).getNmrshiftdbUser().getCurrentUserHistory().getAffiliation1()+", "+spectrum.get(0).getNmrshiftdbUser().getCurrentUserHistory().getAffiliation2());
	    	    			  File att=new File("/tmp/metadata_molecule_"+mol.getMoleculeId()+".xml");
	    	    			  Writer writer = new FileWriter (att);
	    	    		      Velocity.mergeTemplate("src/vmtemplates/doi_dataset.vm", "UTF-8", context, writer);
	    	    		      writer.flush();
	    	    			  // Create the attachment
	    	    			  EmailAttachment attachment = new EmailAttachment();
	    	    			  attachment.setPath("/tmp/metadata_molecule_"+mol.getMoleculeId()+".xml");
	    	    			  attachment.setDisposition(EmailAttachment.ATTACHMENT);
	    	    			  attachment.setDescription("metedata");
	    	    			  attachment.setName("Metadata");
	    	    			  // Create the email message
	    	    			  MultiPartEmail email = new MultiPartEmail();
	    	    			  //email.setHostName(GeneralUtils.getSmtpServer((ServletConfig)null));
	    	    			  email.addTo("doimailreg@ub.uni-koeln.de", "DOI");
	    	    			  email.addTo("stefhk3@web.de","DOI");
	    	    			  email.setFrom("stefhk3@web.de", "Stefan Kuhn");
	    	    			  email.setSubject("DOI");
	    	    			  email.setMsg("https://nmrshiftdb.nmr.uni-koeln.de/molecule/"+mol.getMoleculeId()+"/"+URLEncoder.encode(datasetkeyword+"_"+datasetsolvent, "UTF-8"));
	    	
	    	    			  // add the attachment
	    	    			  email.attach(attachment);
	    	
	    	    			  // send the email
	    	    			  email.send();
	    	    			  
	    	    			  DBDatasetDoi doi=new DBDatasetDoi();
	    	    			  doi.setMoleculeId(mol.getMoleculeId());
	    	    			  doi.setDBCondition(DBConditionPeer.getByValue(datasetsolvent).get(0));
	    	    			  doi.setDoi(doistring);
	    	    			  doi.save();
	    	    			  StringTokenizer st=new StringTokenizer(datasetkeyword,"_");
	    	    			  while(st.hasMoreTokens()){
	    	    				  DBDatasetDoiDBKeyword doikeyword=new DBDatasetDoiDBKeyword();
	    	    				  doikeyword.setDBDatasetDoi(doi);
	    	    				  doikeyword.setDBKeyword(DBKeywordPeer.doSelectByKeyword(st.nextToken()));
	    	    				  doikeyword.save();
	    	    			  }
	                        }
	    				  }
    				  }
                  }
    		  }
          }
          
          private static void doHoseDump(String[] args) throws IOException, SQLException, TorqueException{
        	  boolean withSolvents=true;
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		      int i=0;
              Connection conn=Torque.getConnection();
              HashMap<String, Map<String, Map<String,ArrayList<Double>>>> mapsmap = new HashMap<String,Map<String,Map<String,ArrayList<Double>>>>();
              while(true){
                  Statement stmt=conn.createStatement();
                  ResultSet rs=stmt.executeQuery("select HOSE_CODE, VALUE, SYMBOL, CONDITION_ID from HOSE_CODES where CONDITION_TYPE='m' and WITH_RINGS=0  limit "+(i* 1000)+", 1000");
                  int m=0;
                  System.out.println(i);
                  while(rs.next()){
                      String code = rs.getString(1);
                      Double value = new Double(rs.getString(2));
                      String symbol = rs.getString(3);
                      String solvent = "Unreported";
                      if(withSolvents && (rs.getInt(4)==121 || rs.getInt(4)==123 || rs.getInt(4)==137))
                    	  solvent = DBConditionPeer.retrieveByPK(rs.getInt(4)).getValue();
                      if (mapsmap.get(solvent) == null) {
                          mapsmap.put(solvent, new HashMap<String, Map<String,ArrayList<Double>>>());
                      }
                      if (mapsmap.get(solvent).get(symbol) == null) {
                          mapsmap.get(solvent).put(symbol, new HashMap<String,ArrayList<Double>>());
                      }
                      for (int spheres = 6; spheres > 0; spheres--) {
                          StringBuffer hoseCodeBuffer = new StringBuffer();
                          StringTokenizer st = new StringTokenizer(code, "()/");
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
                          String hoseCode = hoseCodeBuffer.toString();
                          if (mapsmap.get(solvent).get(symbol).get(hoseCode) == null) {
                              mapsmap.get(solvent).get(symbol).put(hoseCode, new ArrayList<Double>());
                          }
                          mapsmap.get(solvent).get(symbol).get(hoseCode).add(value);
                          if(!solvent.equals("Unreported")){
                        	  String solvent2="Unreported";
                              if (mapsmap.get(solvent2) == null) {
                                  mapsmap.put(solvent2, new HashMap<String, Map<String,ArrayList<Double>>>());
                              }
                              if (mapsmap.get(solvent2).get(symbol) == null) {
                                  mapsmap.get(solvent2).put(symbol, new HashMap<String,ArrayList<Double>>());
                              }
                              if (mapsmap.get(solvent2).get(symbol).get(hoseCode) == null) {
                                  mapsmap.get(solvent2).get(symbol).put(hoseCode, new ArrayList<Double>());
                              }
                        	  mapsmap.get(solvent2).get(symbol).get(hoseCode).add(value);
                          }
                      }
                      m++;
                  }
                  i++;
                  stmt.close();
                  if(m==0)
                      break;
              }
              Torque.closeConnection(conn);
        	  File fileh=new File("/tmp/nmrshiftdbh.csv");
		      FileOutputStream fosh=new FileOutputStream(fileh);
		      File filec=new File("/tmp/nmrshiftdbc.csv");
		      FileOutputStream fosc=new FileOutputStream(filec);
              Set<String> keySet=mapsmap.keySet();
              Iterator<String> it=keySet.iterator();
              while(it.hasNext()){
            	  String solvent=it.next();
            	  Set<String> keySet2=mapsmap.get(solvent).keySet();
            	  Iterator<String> it2=keySet2.iterator();
            	  while(it2.hasNext()){
            		  String symbol=(String)it2.next();
            		  Map<String,ArrayList<Double>> hosemap= mapsmap.get(solvent).get(symbol);
            		  Set<String> keySet3=hosemap.keySet();
            		  Iterator<String> it3=keySet3.iterator();
            		  while(it3.hasNext()){
            			  String hoseCode=it3.next();
            			  ArrayList<Double> list=hosemap.get(hoseCode);
            			  double[] values = new double[list.size()];
            			  for (int k = 0; k < list.size(); k++) {
            				  values[k] = list.get(k).doubleValue();
            			  }
            			  if(withSolvents){
	            			  if(symbol.equals("C"))
	            				  fosc.write(new String(symbol+"_"+solvent+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());
	            			  else
	            				  fosh.write(new String(symbol+"_"+solvent+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());
            			  }else{
	            			  if(symbol.equals("C"))
	            				  fosc.write(new String(symbol+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());
	            			  else
	            				  fosh.write(new String(symbol+"_"+hoseCode+"_"+Statistics.minimum(values)+"_"+Statistics.average(values)+"_"+Statistics.maximum(values)+"_"+values.length+"\r\n").getBytes());            				  
            			  }
            		  }
                  }
              }
              fosh.close();
              fosc.close();
          }


          private static void doCoordinates(String[] args) throws Exception {
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
              Connection dbconn = Torque.getConnection();
          Statement stmt = dbconn.createStatement();
              ResultSet rs = stmt.executeQuery("select distinct MOLECULE_ID from SPECTRUM join SPECTRUM_KEYWORD using (SPECTRUM_ID) join KEYWORD using (KEYWORD_ID) where KEYWORD.KEYWORD=\"dkfz spektren database\";");
              while (rs.next()) {
                      try {
                              DBMolecule dbmol= DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
                              System.err.println(dbmol.getMoleculeId());
                              IAtomContainer ac=dbmol.getAsCDKMoleculeAsEntered(1, true);
                              StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                              sdg.setMolecule(ac);
                              sdg.generateCoordinates();
                              IAtomContainer layedOutMol = sdg.getMolecule();
                              for(int i=0;i<layedOutMol.getAtomCount();i++) {
                                      DBCoordinateSet2d dbcoords=dbmol.getAtomAt(i).getDBCoordinateSet2ds().get(0);
                                      dbcoords.setXCoordinate(layedOutMol.getAtom(i).getPoint2d().x);
                                      dbcoords.setYCoordinate(layedOutMol.getAtom(i).getPoint2d().y);
                                      dbcoords.save();
                              }
                      }
                      catch(Exception ex) {
                              ex.printStackTrace();
                      }
              }
          }
          
          private static void readCorrs(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
        	  System.out.println("Enter filename");
    	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	      String filename = br.readLine();
    	      System.out.println("Enter mol id");
    	      br = new BufferedReader(new InputStreamReader(System.in));
    	      String id = br.readLine();
    	      DBMolecule dbmol=DBMoleculePeer.retrieveByPK(Integer.parseInt(id));
    	      System.out.println("Enter keyword");
    	      br = new BufferedReader(new InputStreamReader(System.in));
    	      String keyword = br.readLine();
    	      File file =  new File(args[0]+"/"+filename);
    	      InputStream is = new FileInputStream(file);
    	      BufferedReader buf = new BufferedReader(new InputStreamReader(is));
    	      String line = buf.readLine();
    	      StringBuffer correlationFile = new StringBuffer();
    	      while(line != null){
    	    	  correlationFile.append(line).append("\n");
    	         line = buf.readLine();
    	      }
    	      System.out.println("Enter molfile");
    	      br = new BufferedReader(new InputStreamReader(System.in));
    	      String molfilename = br.readLine();
    	      File molfile =  new File(args[0]+"/"+molfilename);
    	      MDLV2000Reader mdlreader = new MDLV2000Reader(new FileInputStream(molfile));
    		  IAtomContainer mol = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
    		  mol = (IAtomContainer)mdlreader.read(mol);
    	      mdlreader.close();
    	      try{
    	    	  	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
    	    	  	Isotopes.getInstance().configureAtoms(mol);
    	      }catch(Exception ex){
    	    	  System.out.println(ex.getMessage());
    	      }
    	      is = new FileInputStream(molfile);
    	      buf = new BufferedReader(new InputStreamReader(is));
    	      line = buf.readLine();
    	      StringBuffer molFile = new StringBuffer();
    	      while(line != null){
    	          molFile.append(line).append("\n");
    	          line = buf.readLine();
    	      }
    	      ElectronDonation model       = ElectronDonation.cdk();
    	      CycleFinder      cycles      = Cycles.cdkAromaticSet();
    	      Aromaticity      aromaticity = new Aromaticity(model, cycles);
   	    	  aromaticity.apply(mol);
   	    	  AtomUtils.addExplicitHydrogensToSatisfyValency(mol);
   	    	  IAtomContainer molorig=dbmol.getAsCDKMolecule(1);
   	    	  List<RMap> list=new UniversalIsomorphismTester().getIsomorphAtomsMap(molorig, mol);
   	    	  Map<Integer,Integer> atommap=new HashMap<>();
   	    	  for(RMap map : list) {
   	    		  atommap.put(map.getId2(),  map.getId1());
   	    	  }
    	      SubmittingData subDataC=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
    	      SubmittingData subDataH=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
    	      Map<String,SubmittingData> otherspectra=new HashMap<>();
        	  List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
        	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
        	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
        	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
        	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
    	      GeneralUtils.readTopsinfile(correlationFile, molFile, subDataC, subDataH, otherspectra, new StringBuffer(), cosypeaks, tocsypeaks, hmbcpeaks, hsqcpeaks, noesypeaks, args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
        	  /*NmredataReader reader = new NmredataReader(new FileInputStream(file));
              NmreData data = reader.read();
              for(Spectrum spectrum : data.getSpectra()) {
            	  if(spectrum instanceof NMR2DSpectrum){
            		  String spectrumtype=null;
            		  for(Object note :  ((NMR2DSpectrum)spectrum).getNotes()){
            			  if(note instanceof Note && ((Note)note).getDescriptor().getKey().equals("CorType")){
            				  spectrumtype=(String)((Note)note).getValue();
            				  break;
            			  }
            		  }
            		  if(spectrumtype!=null){
                		  List<Peak2D> peaks=new ArrayList<Peak2D>();
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
                					peak2d.setAtoms1(((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber1()[0]+"");
                					peak2d.setAtoms2(((TwoAtomsReference)((NMR2DSpectrum)spectrum).getAssignments()[k].getTargets()[0]).getAtomNumber2()[0]+"");
            						peaks.add(peak2d);
                					k++;
                				}
                		  }
                		  if(spectrumtype.equals("HSQC")){
                           	  hsqcpeaks=peaks;
                		  }
                		  if(spectrumtype.equals("HMBC")){
                           	  hmbcpeaks=peaks;
                		  }
                		  if(spectrumtype.equals("TOCSY")){
                           	  tocsypeaks=peaks;
                		  }
                		  if(spectrumtype.equals("NOESY")){
                           	  noesypeaks=peaks;
                		  }
                		  if(spectrumtype.equals("COSY")){
                           	  cosypeaks=peaks;
                		  }
            		  }
            	  }
              }*/
              for(DBSpectrum spectrum : dbmol.selectSpectra()) {
            	  List<Peak2D> peaks=null;
            	  if(spectrum.getKeywords().contains(keyword)) {
            		  if(spectrum.getDBSpectrumType().getName().contains("COSY"))
            			  peaks=cosypeaks;
            		  else if(spectrum.getDBSpectrumType().getName().contains("TOCSY"))
            			  peaks=tocsypeaks;
            		  else if(spectrum.getDBSpectrumType().getName().contains("NOESY"))
            			  peaks=noesypeaks;
            		  else if(spectrum.getDBSpectrumType().getName().contains("HMBC"))
            			  peaks=hmbcpeaks;
            		  else if(spectrum.getDBSpectrumType().getName().contains("HSQC"))
            			  peaks=hsqcpeaks;
            	  }
            	  if(peaks!=null) {
	            	  for(Peak2D peak : peaks) {
	            		  DBSignal signal=new DBSignal();
	            		  signal.setIntensity(0);
	            		  signal.setSpectrumId(spectrum.getSpectrumId());
	            		  signal.save();
	            		  DBShift shift1=new DBShift();
	            		  shift1.setSignalId(signal.getSignalId());
	            		  shift1.setAxis(1);
	            		  shift1.setValue((float)peak.getFirstShift());
	            		  shift1.save();
	            		  DBShift shift2=new DBShift();
	            		  shift2.setSignalId(signal.getSignalId());
	            		  shift2.setAxis(2);
	            		  shift2.setValue((float)peak.getSecondShift());
	            		  shift2.save();
	            		  DBSignalDBAtom sigatom1=new DBSignalDBAtom();
	            		  sigatom1.setAxis(1);
	            		  sigatom1.setAtomId(spectrum.getDBMolecule().getAtomAt(atommap.get(((AtomReference)peak.assignments.get(0).getTargets()[1]).getAtomNumber()-1)).getAtomId());
	            		  sigatom1.setSignalId(signal.getSignalId());
	            		  sigatom1.save();
	            		  DBSignalDBAtom sigatom2=new DBSignalDBAtom();
	            		  sigatom2.setAxis(2);
	            		  sigatom2.setAtomId(spectrum.getDBMolecule().getAtomAt(atommap.get(((AtomReference)peak.assignments.get(0).getTargets()[0]).getAtomNumber()-1)).getAtomId());
	            		  sigatom2.setSignalId(signal.getSignalId());
	            		  sigatom2.save();
	        		  }
            	  }
              }
          }
          
          private static void publishDataset(String[] args) throws Exception {
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
    	      System.out.println("Enter mol id");
    	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	      String id = br.readLine();
    	      DBMolecule dbmol=DBMoleculePeer.retrieveByPK(Integer.parseInt(id));
    	      System.out.println("Enter keyword");
    	      br = new BufferedReader(new InputStreamReader(System.in));
    	      String keyword = br.readLine();
    	      for(DBSpectrum spectrum : dbmol.getDBSpectrums()) {
    	    	  if(spectrum.getKeywords().contains(keyword)) {
    	    		  spectrum.publish("server", "username", "password");
    	    	  }
    	      }
          }

		  private static void writeCouplings(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
	            List l = DBMoleculePeer.executeQuery("select distinct COUPLING.SPECTRUM_ID from ATOM join COUPLING on ATOM.ATOM_ID=COUPLING.ATOM_ID_1 order by SPECTRUM_ID;");
	            File outputFile = new File("nmrshiftdb2couplings.csv");
	            FileOutputStream baos=new FileOutputStream(outputFile);
	            Writer out = new OutputStreamWriter(baos, "UTF8");
	            for(int i=0;i<l.size();i++){
	                DBSpectrum spec=DBSpectrumPeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
	                for(DBCoupling coupl : spec.getDBCouplings()) {
	                	out.write(spec.getMoleculeId()+","+spec.getSpectrumId()+","+spec.getSpectrumTypeName()+","+coupl.getDBAtomRelatedByAtomId1().getMdlNumberPlus1()+","+coupl.getDBAtomRelatedByAtomId2().getMdlNumberPlus1()+","+coupl.getConstant()+"\n");
	                }
	                File molfile = new File("mols/"+spec.getMoleculeId()+"mol");
	                FileWriter writer = new FileWriter(molfile);
	                MDLV2000Writer mdlWriter = new MDLV2000Writer(writer);
	                IAtomContainer cdkmol=null;
	                if(spec.getDBMolecule().hasCoordinates(2)) {
	                	cdkmol=spec.getDBMolecule().getAsCDKMolecule(2);
	                }else {
	                	cdkmol=spec.getDBMolecule().getAsCDKMolecule(1);
	                	cdkmol=GeneralUtils.getModelBuilder("mm2").generate3DCoordinates(cdkmol,false);
	                }
	                mdlWriter.write(cdkmol);
	                mdlWriter.close();
	            }
	            out.close();
		  }


		  private static void predictCouplings(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
	            List l = DBMoleculePeer.executeQuery("select COUPLING.* from COUPLING join SPECTRUM using (SPECTRUM_ID) where SPECTRUM.REVIEW_FLAG=\"true\" and SPECTRUM_TYPE_ID=2;");
	            HOSECodeGenerator hcg = new HOSECodeGenerator();
	            int noresult=0;
	            int spherescum=0;
	            int result=0;
	            float diffsum=0;
	            int resultok=0;
	            float diffsumok=0;
	            for(int i=0;i<l.size();i++){
	            	boolean success=false;
            		System.out.println(((Record) l.get(i)).getValue(1).asString());
	            	for (int spheres = 6; spheres > 0; spheres--) {
		            	String hose1=new String(DBAtomPeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(2).asString())).getHoseCode());
				        StringTokenizer st = new StringTokenizer(hose1, "()/");
						StringBuffer hoseCodeBuffer1 = new StringBuffer();
				        for (int k = 0; k < spheres; k++) {
				          if (st.hasMoreTokens()) {
				            String partcode = st.nextToken();
				            hoseCodeBuffer1.append(partcode);
				          }
				          if (k == 0) {
				            hoseCodeBuffer1.append("(");
				          } else if (k == 3) {
				            hoseCodeBuffer1.append(")");
				          } else {
				            hoseCodeBuffer1.append("/");
				          }
				        }
				        String hoseCodeUnescaped1 = hoseCodeBuffer1.toString();
				        String hoseCode1 = GeneralUtils.escape(hoseCodeUnescaped1,false);
						hoseCode1 = GeneralUtils.replace(hoseCode1,"\\\\", "\\\\\\\\");
	
		            	String hose2=new String(DBAtomPeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(3).asString())).getHoseCode());
				        st = new StringTokenizer(hose2, "()/");
						StringBuffer hoseCodeBuffer2 = new StringBuffer();
				        for (int k = 0; k < spheres; k++) {
				          if (st.hasMoreTokens()) {
				            String partcode = st.nextToken();
				            hoseCodeBuffer2.append(partcode);
				          }
				          if (k == 0) {
				            hoseCodeBuffer2.append("(");
				          } else if (k == 3) {
				            hoseCodeBuffer2.append(")");
				          } else {
				            hoseCodeBuffer2.append("/");
				          }
				        }
				        String hoseCodeUnescaped2 = hoseCodeBuffer2.toString();
				        String hoseCode2 = GeneralUtils.escape(hoseCodeUnescaped2,false);
						hoseCode2 = GeneralUtils.replace(hoseCode2,"\\\\", "\\\\\\\\");
            	
		            	String sql="select COUPLING.*, atom1.HOSE_CODE, atom2.HOSE_CODE from COUPLING join SPECTRUM using (SPECTRUM_ID) join ATOM as atom1 on (COUPLING.ATOM_ID_1=atom1.ATOM_ID)  join ATOM as atom2 on (COUPLING.ATOM_ID_2=atom2.ATOM_ID) where SPECTRUM.REVIEW_FLAG=\"true\" and SPECTRUM_TYPE_ID=2 and ((atom1.HOSE_CODE like '"+hoseCode1+"%' and atom2.HOSE_CODE like '"+hoseCode2+"%') or (atom1.HOSE_CODE like '"+hoseCode2+"%' and atom2.HOSE_CODE like '"+hoseCode1+"%')) and COUPLING.SPECTRUM_ID<>"+((Record) l.get(i)).getValue(1).asString();
			            List<Record> l2 = DBMoleculePeer.executeQuery(sql);
			            if(l2.size()>0) {
			            	System.out.println("result "+spheres);
			            	spherescum+=spheres;
			            	result++;
			            	success=true;
			            	float diff=Math.abs(Math.abs(((Record) l.get(i)).getValue(4).asFloat())-Math.abs(((Record) l2.get(0)).getValue(4).asFloat()));
			            	System.out.println(diff);
			            	diffsum+=diff;
			            	if(spheres>4) {
			            		diffsumok+=diff;
			            		resultok++;
			            	}
			            	break;
			            }
	            	}
	            	if(!success) {
			            System.out.println("no result");
			            noresult++;
	            	}
	            }
	            System.out.println(noresult+" "+result+" "+(spherescum/(float)result)+" "+(diffsum/result)+" "+resultok+" "+(diffsumok/resultok));
		  }


		  private static void readBmrb(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
	        URL url = new URL("https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_lists/experimental/"); 
			Scanner sc = new Scanner(url.openStream());
			FileOutputStream fos=new FileOutputStream(new File("bmrb1d.csv"));
			while(sc.hasNextLine()) {
				String id=sc.nextLine();
				if(id.indexOf("bms")>-1) {
		            try{
						id=id.substring(id.indexOf("bms"));
						id=id.substring(0,id.indexOf("."));
						System.out.println(id);
						String urlmolstring="https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_directories/"+id+"/"+id+".mol";
						StringBuffer mdl=new StringBuffer();
						URL urlmol = new URL(urlmolstring);
						Scanner sc2 = new Scanner(urlmol.openStream());
						while(sc2.hasNextLine()) {
							String line = sc2.nextLine();
							mdl.append(line+"\r\n");
						}
						mdl.append("> <BMRBID>\r\n"+id+"\r\n\r\n");
						sc2.close();
			        	List<RMap> mappinglist=null;
						urlmolstring="https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_lists/experimental/"+id+".str";
						urlmol = new URL(urlmolstring);
						sc2 = new Scanner(urlmol.openStream());
		            	boolean in1h=false;
		            	boolean in13c=false;
		            	int inshifts=0;
		            	int skip1=0;
		            	int skip2=0;
		            	SubmittingData subData=null;
		            	int sampleid=0;
		            	int conditionid=0;
		            	String fieldstring="";
		            	Map<Integer,String> temp=new HashMap<Integer,String>();
		            	Map<Integer,String> solvent=new HashMap<Integer,String>();
		            	IAtomContainer mol3d=null;
		            	StringBuffer unassignedshifts=new StringBuffer("");
	                    int move=0;
	                    int saved=0;
	                    while(sc2.hasNextLine()) {
							String line=sc2.nextLine();
							if(line.contains("_Sample_condition_list.ID") || line.contains("_Spectral_peak_list.Sample_condition_list_ID")) {
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    conditionid=Integer.parseInt(st.nextToken());
				        	}
				        	if(line.contains("_Sample.ID") || line.contains("_Spectral_peak_list.Sample_ID")) {
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    sampleid=Integer.parseInt(st.nextToken());
				        	}
				        	if(line.contains("temperature")) {
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    temp.put(conditionid, st.nextToken());
				        	}
				        	if((line.contains("Solvent") || line.contains("solvent")) && !(line.contains(".Solvent") || line.contains(".solvent"))) {
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    solvent.put(sampleid, st.nextToken());
				        	}
				        	if(line.contains("_NMR_spectrometer.Field_strength")) {
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    fieldstring=st.nextToken();
				        	}
				        	if(line.contains("save_spectral_peak_1H") || line.contains("save_spectral_peak_13C")) {
				            	subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				            	subData.setMol(mdl.toString(),false,false,false);
				            	mol3d=subData.getMolWithH();
			                    int wash=-1;
			                    IAtomContainer mol=subData.getMolWithH();
			                    for(IAtom atom : mol.atoms()) {
			                    	if(atom.getSymbol().equals("H"))
			                    		wash=mol.getAtomNumber(atom)-1;
			                    	else if(wash>0 && !atom.getSymbol().equals("H")) {
			                    		move=mol.getAtomNumber(atom);
			                    	}
			                    }
					            //we only have 3d coordinates, so generate 2d
				            	StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			                    sdg.setMolecule(subData.getMolOriginal());
			                    sdg.generateCoordinates();
			                    IAtomContainer mol2 = sdg.getMolecule();
			                    for(IAtom atom : mol2.atoms()) {
			                    	atom.setPoint3d(null);
			                    }
			                    mol2=AtomContainerManipulator.removeNonChiralHydrogens(mol2);
					  	        StringWriter sw = new StringWriter();
					  	        MDLV2000Writer mdlwriter=new MDLV2000Writer(sw);
					  	        mdlwriter.writeMolecule(mol2);
					  	        mdlwriter.close();
                                                        subData.setMol(sw.toString(),false,false,false);
							crit = new Criteria();
					            if(line.equals("save_spectral_peak_1H")) {
						            crit.add(DBSpectrumTypePeer.NAME, "1H");
					            	in1h=true;
					            }
					            if(line.equals("save_spectral_peak_13C")) {
						            crit.add(DBSpectrumTypePeer.NAME, "13C");
					            	in13c=true;
					            }
					            v = DBSpectrumTypePeer.doSelect(crit);
					            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
					            subData.setSpectrumKeywords("bmrb");
					            boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
					            AllRingsFinder arf = new AllRingsFinder();
					            mol=subData.getMolWithH();
					            IRingSet ringSet=arf.findAllRings(mol);
					            Iterator<IBond> it=mol.bonds().iterator();
					            int c=0;
					            while(it.hasNext()) {
					            	IBond bond=it.next();
					            	if (AtomUtils.isValidDoubleBondConfiguration(mol,bond,ringSet)) {
					            		isDoubleBondSpecified[c] = true;
					            	}
					            	c++;
					            }
					            subData.setDoubleBondConfiguration(true);
					            subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
					            if(in13c)
					            	subData.setComment(urlmolstring+"; multplicities generated from proton count");
					            else
					            	subData.setComment(urlmolstring);
					            subData.setSpectrumLinks("https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_directories/"+id+";link to bmrb data");
								  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
								  DBMolecule dbmol=GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
								  subData.setSmilesNonChiralProv(ssb.string1);
								  subData.setIsChiralProv(ssb.bool);
								  if(ssb.bool){
									  subData.setSmilesChiralProv(ssb.string2);
								  }
				        	}
				        	if(inshifts==0 && line.contains("_Assigned_peak_chem_shift") && (in1h || in13c)) {
				        		inshifts=1;
				        	}else if(inshifts==1 && line.contains("_Assigned_peak_chem_shift.Val")) {
				        		inshifts=2;
				        	}else if(inshifts==2 && line.contains("_Assigned_peak_chem_shift.Atom_ID")) {
				        		inshifts=3;
				        	}
				        	if(inshifts==1 && line.contains("_Assigned_peak_chem_shift")) {
				        		skip1++;
				        	}
				        	if(inshifts==2 && line.contains("_Assigned_peak_chem_shift")) {
				        		skip2++;
				        	}
				        	if(inshifts==3 && line.contains(id)) {
				        		StringTokenizer st=new StringTokenizer(line," ");
				        		for(int k=0;k<skip1;k++)
				        			st.nextToken();
				        		String values=st.nextToken();
				        		if(!values.equals("?")) {
					        		double value=Double.parseDouble(values);
					        		for(int k=0;k<skip2-1;k++)
					        			st.nextToken();		
					        		String atoms=st.nextToken();
					        		if(!atoms.equals("?")) {
						        		int atom=Integer.parseInt(atoms.substring(1));
						        		if(move>0 && in1h) {
						        			atom+=1;
						        		}
							            CMLPeakList signals=subData.getSignalstable();
							            boolean added=false;
							            for(int l=0;l<signals.getPeakElements().size();l++){
							              if(signals.getPeakElements().get(l).getXValue()==value){
							            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
							                added=true;
							              }
							            }
							            if(!added){
							            	CMLPeak peak = new CMLPeak();
							            	peak.setXValue(value);
							            	peak.setYValue(0);
							            	peak.setAtomRefs(SubmittingData.addAtomToAtomRefs(peak.getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
							            	subData.addToSignalstable(peak);
							            }
						            }else {
						            	unassignedshifts.append(value+";");
						            }
				        		}
				        	}
				        	if(line.contains("stop_")) {
				        		inshifts=0;
				        		skip1=0;
				        		skip2=0;
				        	}
				        	if(line.equals("save_")) {
				        		if((in1h || in13c) && subData.getSignalstable().getPeakChildren().size()>0) {
					            	List<DBConditionType> conds=DBSpectrumPeer.retrieveByPK(57728).getMeasurementConditions().stream().map(x -> {
										try {
											return x.getDBConditionType();
										} catch (TorqueException e) {
											e.printStackTrace();
											return new DBConditionType();
										}
									}).collect(Collectors.toList());
					            	for(int k=0;k<conds.size();k++){
					            		DBConditionType cond=conds.get(k);
								        if(cond.getConditionTypeId()==1)
								        	cond.setCurrentValue(temp.get(conditionid));
								        if(cond.getConditionTypeId()==2)
								          	cond.setCurrentValue(solvent.get(sampleid));
								        if(cond.getConditionTypeId()==3)
								        	cond.setCurrentValue(fieldstring);
								        if(cond.getConditionTypeId()==11)
								        	cond.setCurrentValue("Unreported");
					            	}
						        	subData.setConditions(conds);
						        	if(unassignedshifts.length()>0) {
						        		subData.setComment(subData.getComment()+"; unassigned shifts at "+GeneralUtils.removeLastComma(unassignedshifts));
						        	}
						            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
				                    com.lowagie.text.Document reportpdf = new com.lowagie.text.Document();
				                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				                    PdfWriter.getInstance(reportpdf, baos);
				                    reportpdf.open();
				                    long[] morgan=Canon.symmetry(subData.getMolOriginal(), GraphUtil.toAdjList(subData.getMolOriginal()));
				    				double[] mark=SpectrumUtils.calculateScore(subData.getMolWithH(), subData.getMolOriginal(), subData, null, subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") ? GeneralUtils.getPredictionLimits("1H") : GeneralUtils.getPredictionLimits("13C"), "", reportpdf, null, morgan, GeneralUtils.getNmrshiftdbProperty("defaulturl"), null, null, null, null, null, null, new StringBuffer(),null);
				    				reportpdf.close();
				                    DBSpectrumFile specfile = new DBSpectrumFile();
				                    specfile.setSpectrumId(subData.getSpectrumid());
				                    specfile.setType(DBSpectrum.NMRSHIFTDB_TYPE);
				                    specfile.setFormat("nmrshiftdb2");
				                    byte[] base64 = new Base64().encode(baos.toByteArray());
				                    specfile.setData(base64);
				                    specfile.save();
				                    String marktext=SpectrumUtils.makeJudgement((int)mark[0], mark[5],true);
				                    DBUserDBSpectrumMark specrummark=new DBUserDBSpectrumMark();
				                    specrummark.setUserId(NmrshiftdbUserPeer.getByName("admin").getUserId());
				                    specrummark.setSpectrumId(subData.getSpectrumid());
				                    specrummark.setMarkDate(new Date());
				                    specrummark.setComment(marktext);
				                    specrummark.setMark((int)mark[0]);
				                    specrummark.save();
				                    saved++;
				        		}
				        		if(in1h) {
				        			in1h=false;
				        		}
				        		if(in13c) {
				        			in13c=false;
				        		}
				        	}
				        }
				        System.out.println("Wrote "+id);
				        if(saved==0)
				        	System.out.println("none saved");
		            } catch (Exception ex) {
		            	ex.printStackTrace();
		            }
		        }
		    }
		  }
		  


		//this reads files in the style of 001204 etc.
		  private static void readBmrb2(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    File[] files = new File(args[0]).listFiles();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
	        URL url = new URL("https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_lists/experimental/"); 
			Scanner sc = new Scanner(url.openStream());
			FileOutputStream fos=new FileOutputStream(new File("bmrb1d.csv"));
			while(sc.hasNextLine()) {
				String id=sc.nextLine();
				if(id.indexOf("bms")>-1) {
		            try{
						id=id.substring(id.indexOf("bms"));
						id=id.substring(0,id.indexOf("."));
						System.out.println(id);
						String urlmolstring="https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_directories/"+id+"/"+id+".mol";
						StringBuffer mdl=new StringBuffer();
						URL urlmol = new URL(urlmolstring);
						Scanner sc2 = new Scanner(urlmol.openStream());
						while(sc2.hasNextLine()) {
							String line = sc2.nextLine();
							mdl.append(line+"\r\n");
						}
						mdl.append("> <BMRBID>\r\n"+id+"\r\n\r\n");
						sc2.close();
			        	List<RMap> mappinglist=null;
		            	SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
		            	subData.setMol(mdl.toString(),false,false,false);
		            	IAtomContainer mol3d = subData.getMolWithH();
	                    int wash=-1;
	                    int move=0;
	                    IAtomContainer mol=subData.getMolWithH();
	                    for(IAtom atom : mol.atoms()) {
	                    	if(atom.getSymbol().equals("H"))
	                    		wash=mol.getAtomNumber(atom)-1;
	                    	else if(wash>0 && !atom.getSymbol().equals("H")) {
	                    		move=mol.getAtomNumber(atom);
	                    	}
	                    }
						urlmolstring="https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_lists/experimental/"+id+".str";
						urlmol = new URL(urlmolstring);
						sc2 = new Scanner(urlmol.openStream());
		            	boolean in1h=false;
		            	boolean in13c=false;
		            	int inshifts=0;
		            	int skip1=0;
		            	int skip2=0;
		            	int sampleid=0;
		            	int conditionid=0;
		            	String fieldstring="";
		            	Map<Integer,String> temp=new HashMap<Integer,String>();
		            	Map<Integer,String> solvent=new HashMap<Integer,String>();
		            	String line="";
		            	StringBuffer unassignedshifts=new StringBuffer("");
	                    int saved=0;
			        	boolean type=false;
			        	CMLPeakList peaks13c=new CMLPeakList();
			        	CMLPeakList peaks1h=new CMLPeakList();
	                    while(sc2.hasNextLine()) {
							line=sc2.nextLine();
							if(line.contains("_Sample_condition_list.ID") || line.contains("_Spectral_peak_list.Sample_condition_list_ID")) {
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    conditionid=Integer.parseInt(st.nextToken());
				        	}
				        	if(line.contains("_Sample.ID") || line.contains("_Spectral_peak_list.Sample_ID")) {
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    sampleid=Integer.parseInt(st.nextToken());
				        	}
				        	if(line.contains("temperature")) {
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    temp.put(conditionid, st.nextToken());
				        	}
				        	if((line.contains("Solvent") || line.contains("solvent")) && !(line.contains(".Solvent") || line.contains(".solvent"))) {
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    solvent.put(sampleid, st.nextToken());
				        	}
				        	if(line.contains("_NMR_spectrometer.Field_strength")) {
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    fieldstring=st.nextToken();
				        	}
				        	if(line.contains("save_spectral_peak_1H") || line.contains("save_spectral_peak_13C") || line.contains("save_spectral_peaks_1D_1H_set01") || line.contains("save_spectral_peaks_1D_13C_set01")) {
				            	subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				            	subData.setMol(mdl.toString(),false,false,false);
					            //we only have 3d coordinates, so generate 2d
				            	StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			                    sdg.setMolecule(subData.getMolOriginal());
			                    sdg.generateCoordinates();
			                    IAtomContainer mol2 = sdg.getMolecule();
			                    for(IAtom atom : mol2.atoms()) {
			                    	atom.setPoint3d(null);
			                    }
			                    mol2=AtomContainerManipulator.removeNonChiralHydrogens(mol2);
					  	        StringWriter sw = new StringWriter();
					  	        MDLV2000Writer mdlwriter=new MDLV2000Writer(sw);
					  	        mdlwriter.writeMolecule(mol2);
					  	        mdlwriter.close();
				            	subData.setMol(sw.toString(),false,false,false);
					            crit = new Criteria();
					            if(line.equals("save_spectral_peak_1H") || line.contains("save_spectral_peaks_1D_1H_set")) {
						            crit.add(DBSpectrumTypePeer.NAME, "1H");
					            	in1h=true;
					            }
					            if(line.equals("save_spectral_peak_13C") || line.contains("save_spectral_peaks_1D_13C_set")) {
						            crit.add(DBSpectrumTypePeer.NAME, "13C");
					            	in13c=true;
					            }
					            v = DBSpectrumTypePeer.doSelect(crit);
					            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
					            subData.setSpectrumKeywords("bmrb");
					            boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
					            AllRingsFinder arf = new AllRingsFinder();
					            mol=subData.getMolWithH();
					            IRingSet ringSet=arf.findAllRings(mol);
					            Iterator<IBond> it=mol.bonds().iterator();
					            int c=0;
					            while(it.hasNext()) {
					            	IBond bond=it.next();
					            	if (AtomUtils.isValidDoubleBondConfiguration(mol,bond,ringSet)) {
					            		isDoubleBondSpecified[c] = true;
					            	}
					            	c++;
					            }
					            subData.setDoubleBondConfiguration(true);
					            subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
					            if(in13c)
					            	subData.setComment(urlmolstring+"; multplicities generated from proton count");
					            else
					            	subData.setComment(urlmolstring);
					            subData.setSpectrumLinks("https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_directories/"+id+";link to bmrb data");
								  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
								  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
								  subData.setSmilesNonChiralProv(ssb.string1);
								  subData.setIsChiralProv(ssb.bool);
								  if(ssb.bool){
									  subData.setSmilesChiralProv(ssb.string2);
								  }
				        	}
				        	if(inshifts==0 && (line.contains("_Assigned_peak_chem_shift") || line.contains("_Atom_chem_shift"))) {
				        		inshifts=1;
				        	}else if(inshifts==1 && (line.contains("_Assigned_peak_chem_shift.Val") || line.contains("_Atom_chem_shift.Val") || line.contains("_Assigned_peak_chem_shift.Atom_ID") || line.contains("_Atom_chem_shift.Atom_ID")  )) {
				        		inshifts=2;
				        	}else if(inshifts==2 && (line.contains("_Assigned_peak_chem_shift.Atom_ID") || line.contains("_Atom_chem_shift.Atom_ID") || line.contains("_Assigned_peak_chem_shift.Val") || line.contains("_Atom_chem_shift.Val")  )) {
				        		inshifts=3;
				        	}
				        	if(inshifts==1 && (line.contains("_Assigned_peak_chem_shift") || line.contains("_Atom_chem_shift"))) {
				        		skip1++;
				        	}
				        	if(inshifts==2 && (line.contains("_Assigned_peak_chem_shift") || line.contains("_Atom_chem_shift"))) {
				        		skip2++;
				        	}
				        	if(inshifts==2 && line.contains("_Assigned_peak_chem_shift"))
				        		type=false;
				        	if(inshifts==2 && line.contains("_Atom_chem_shift")) {
				        		type=true;
				        	}
				        	if(inshifts==3 && line.contains(id)) {
				        		StringTokenizer st=new StringTokenizer(line," ");
				        		for(int k=0;k<skip1;k++)
				        			st.nextToken();
				        		String values=null;
				        		String atoms=null;
				        		if(!type)
				        			values=st.nextToken();
				        		else
				        			atoms=st.nextToken();
				        		for(int k=0;k<skip2-1;k++)
				        			st.nextToken();		
				        		if(type)
				        			values=st.nextToken();
				        		else
				        			atoms=st.nextToken();
				        		double value=0;
				        		if(!values.equals("?") && !atoms.equals("?")) {
					        		value=Double.parseDouble(values);
					        		int atom=Integer.parseInt(atoms.substring(1));
					        		if(move>0 && atoms.charAt(0)=='H') {
					        			atom+=1;
					        		}
						            CMLPeakList signals;
						            if(atoms.charAt(0)=='C')
						            	signals=peaks13c;
						            else
						            	signals=peaks1h;
						            /*boolean added=false;
						            for(int l=0;l<signals.getPeakElements().size();l++){
						              if(signals.getPeakElements().get(l).getXValue()==value){
						            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
						                added=true;
						              }
						            }
						            if(!added){*/
					            	CMLPeak peak = new CMLPeak();
					            	peak.setXValue(value);
					            	peak.setYValue(0);						            	
					            	String[] oldRefs=peak.getAtomRefs();
					            	if(oldRefs==null)
					            		oldRefs=new String[0];
					            	String[] newRefs=new String[oldRefs.length+1];
					            	newRefs[newRefs.length-1]="a"+atom;
					            	peak.setAtomRefs(newRefs);
					            	signals.addPeak(peak);
					            }else if(!values.equals("?")){
					            	unassignedshifts.append(value+";");
					            }
				        	}
				        	if(line.contains("stop_")) {
				        		inshifts=0;
				        		skip1=0;
				        		skip2=0;
				        	}
				        	if(line.equals("save_")) {
				        		inshifts=0;
				        		skip1=0;
				        		skip2=0;
				        		if((in1h || in13c)) {
				        			if(in1h) {
				        				for(CMLPeak peak : peaks1h.getPeakChildren())
				        					subData.addToSignalstable(peak);
				        			}else {
				        				for(CMLPeak peak : peaks13c.getPeakChildren())
				        					subData.addToSignalstable(peak);				        				
				        			}
				        		}
				        		if((in1h || in13c) && subData.getSignalstable().getPeakChildren().size()>0) {
					            	List<DBConditionType> conds=DBSpectrumPeer.retrieveByPK(57728).getMeasurementConditions().stream().map(x -> {
										try {
											return x.getDBConditionType();
										} catch (TorqueException e) {
											e.printStackTrace();
											return new DBConditionType();
										}
									}).collect(Collectors.toList());
					            	for(int k=0;k<conds.size();k++){
					            		DBConditionType cond=conds.get(k);
								        if(cond.getConditionTypeId()==1)
								        	cond.setCurrentValue(temp.get(conditionid));
								        if(cond.getConditionTypeId()==2)
								          	cond.setCurrentValue(solvent.get(sampleid));
								        if(cond.getConditionTypeId()==3)
								        	cond.setCurrentValue(fieldstring);
								        if(cond.getConditionTypeId()==11)
								        	cond.setCurrentValue("Unreported");
					            	}
						        	subData.setConditions(conds);
						        	if(unassignedshifts.length()>0) {
						        		subData.setComment(subData.getComment()+"; unassigned shifts at "+GeneralUtils.removeLastComma(unassignedshifts));
						        	}
						            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
						            if(subData.getExistingMoleculeId()==0) {
							            DBAtom[] atoms=DBMoleculePeer.retrieveByPK(subData.getMoleculeId()).getAtomsAsArrayInMdlOrder();
							            int k=0;
							            for(IAtom atom : mol3d.atoms()) {
							            	try {
							            	DBCoordinateSet3d set=new DBCoordinateSet3d(atom.getPoint3d().x, atom.getPoint3d().y, atom.getPoint3d().z, 7, atoms[k].getAtomId());
							            	set.save();
							            	k++;
							            	}catch(Exception ex) {
							            		
							            	}
							            }
						            }
				                    com.lowagie.text.Document reportpdf = new com.lowagie.text.Document();
				                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				                    PdfWriter.getInstance(reportpdf, baos);
				                    reportpdf.open();
				                    long[] morgan=Canon.symmetry(subData.getMolOriginal(), GraphUtil.toAdjList(subData.getMolOriginal()));
				    				double[] mark=SpectrumUtils.calculateScore(subData.getMolWithH(), subData.getMolOriginal(), subData, null, subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") ? GeneralUtils.getPredictionLimits("1H") : GeneralUtils.getPredictionLimits("13C"), "", reportpdf, null, morgan, GeneralUtils.getNmrshiftdbProperty("defaulturl"), null, null, null, null, null, null, new StringBuffer(),null);
				    				reportpdf.close();
				                    DBSpectrumFile specfile = new DBSpectrumFile();
				                    specfile.setSpectrumId(subData.getSpectrumid());
				                    specfile.setType(DBSpectrum.NMRSHIFTDB_TYPE);
				                    specfile.setFormat("nmrshiftdb2");
				                    byte[] base64 = new Base64().encode(baos.toByteArray());
				                    specfile.setData(base64);
				                    specfile.save();
				                    String marktext=SpectrumUtils.makeJudgement((int)mark[0], mark[5],true);
				                    DBUserDBSpectrumMark specrummark=new DBUserDBSpectrumMark();
				                    specrummark.setUserId(NmrshiftdbUserPeer.getByName("admin").getUserId());
				                    specrummark.setSpectrumId(subData.getSpectrumid());
				                    specrummark.setMarkDate(new Date());
				                    specrummark.setComment(marktext);
				                    specrummark.setMark((int)mark[0]);
				                    specrummark.save();
				                    saved++;
				        		}
				        		if(in1h) {
				        			in1h=false;
				        		}
				        		if(in13c) {
				        			in13c=false;
				        		}
				        	}
				        }
				        System.err.println("Wrote "+id);
				        if(saved==0)
				        	System.out.println("none saved");
		            } catch (Exception ex) {
		            	ex.printStackTrace();
		            }
		        }
		    }
		  }

		  //this read files in the syle of 10001 etc.
		  private static void readBmrb3(String[] args) throws Exception {
			Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
			TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			config.initialize();
		    File[] files = new File(args[0]).listFiles();
		    TurbineUser user = null;
		    Criteria crit = new Criteria();
		    crit.add(NmrshiftdbUserPeer.LOGIN_NAME, "bulkload");
		    List v = NmrshiftdbUserPeer.doSelect(crit);
		    //Look if the bulkoad user exists
		    if (v.size() > 0) {
		      user = (TurbineUser) v.get(0);
		    } else {
		      throw new Exception("no bulkload user");
		    }
		    String nmrpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/NMRShiftDB.properties";
		    String jetspeedpropsFile = args[1] + "../conf/jetspeed/WEB-INF/conf/JetspeedResources.properties";
		    Properties nmrprops = new Properties();
		    Properties jetspeedprops = new Properties();
		    nmrprops.load(new FileInputStream(nmrpropsFile));
		    jetspeedprops.load(new FileInputStream(jetspeedpropsFile));
		    for (int i = 0; i < files.length; i++) {
		        if (files[i].getName().substring(files[i].getName().length() - 4).equals(".mol")) {
		            try{
		            	System.err.println("Reading "+files[i].getName());
		            	BufferedReader buffRead = new BufferedReader(new FileReader(files[i]));
		            	String line = "";
		            	StringBuffer newMdl=new StringBuffer();
				        while ((line = buffRead.readLine()) != null) {
				        	newMdl.append(line+"\n");
				        }
		            	SubmittingData subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
		            	subData.setMol(newMdl.toString(),false,false,false);
		            	IAtomContainer mol3d = subData.getMolWithH();
	                    int wash=-1;
	                    int move=0;
	                    IAtomContainer mol=subData.getMolWithH();
	                    for(IAtom atom : mol.atoms()) {
	                    	if(atom.getSymbol().equals("H"))
	                    		wash=mol.getAtomNumber(atom)-1;
	                    	else if(wash>0 && !atom.getSymbol().equals("H")) {
	                    		move=mol.getAtomNumber(atom);
	                    	}
	                    }
	                    File strfile=new File(files[i].getAbsolutePath().substring(0,files[i].getPath().length() - 4)+".str");
			            buffRead = new BufferedReader(new FileReader(strfile));
		            	boolean in1h=false;
		            	boolean in13c=false;
		            	int inshifts=0;
		            	int skip1=0;
		            	int skip2=0;
		            	int sampleid=0;
		            	int conditionid=0;
		            	String fieldstring="";
		            	Map<Integer,String> temp=new HashMap<Integer,String>();
		            	Map<Integer,String> solvent=new HashMap<Integer,String>();
		            	line="";
		            	StringBuffer unassignedshifts=new StringBuffer("");
	                    int saved=0;
			        	boolean type=false;
			        	CMLPeakList peaks13c=new CMLPeakList();
			        	CMLPeakList peaks1h=new CMLPeakList();
			        	int setid=0;
			        	boolean inset=false;
				        while ((line = buffRead.readLine()) != null) {
				        	if(line.contains("save_sample")) {//
				        		sampleid=Integer.parseInt(""+line.charAt(line.length()-1));
				        	}
				        	if(line.contains("temperature")) {//
				        		StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    temp.put(conditionid, st.nextToken());
				        	}
				        	if((line.contains("solvent") || line.contains("Solvent")) && sampleid>0){//
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    solvent.put(sampleid, st.nextToken());
				        	}
				        	if(line.contains("_NMR_spectrometer.Field_strength")) {//
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    fieldstring=st.nextToken();
				        	}
				        	if(line.contains("save_assigned_chemical_shifts_")) {//
				        		inset=true;
				        	}
				        	if(inset && line.contains("'1D 13C'")){//
				        		in13c=true;
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    st.nextToken();
				        	    st.nextToken();
				        	    setid=Integer.parseInt(st.nextToken());
				        	}
				        	if(inset && line.contains("'1D 1H'")){//
				        		in1h=true;
				        	    StringTokenizer st=new StringTokenizer(line);
				        	    st.nextToken();
				        	    st.nextToken();
				        	    st.nextToken();
				        	    setid=Integer.parseInt(st.nextToken());				        	}
				        	if(line.contains("_Atom_chem_shift.ID") && (in13c || in1h)) {//
				            	subData=new SubmittingData(args[1] + "../conf/jetspeed/WEB-INF/conf/normalizer.xml");
				            	subData.setMol(newMdl.toString(),false,false,false);
					            //we only have 3d coordinates, so generate 2d
				            	StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			                    sdg.setMolecule(subData.getMolOriginal());
			                    sdg.generateCoordinates();
			                    IAtomContainer mol2 = sdg.getMolecule();
			                    for(IAtom atom : mol2.atoms()) {
			                    	atom.setPoint3d(null);
			                    }
			                    mol2=AtomContainerManipulator.removeNonChiralHydrogens(mol2);
					  	        StringWriter sw = new StringWriter();
					  	        MDLV2000Writer mdlwriter=new MDLV2000Writer(sw);
					  	        mdlwriter.writeMolecule(mol2);
					  	        mdlwriter.close();
				            	subData.setMol(sw.toString(),false,false,false);
					            crit = new Criteria();
					            if(in1h) {
						            crit.add(DBSpectrumTypePeer.NAME, "1H");
					            	in1h=true;
					            }
					            if(in13c) {
						            crit.add(DBSpectrumTypePeer.NAME, "13C");
					            	in13c=true;
					            }
					            v = DBSpectrumTypePeer.doSelect(crit);
					            subData.setChoosenSpectrumType((DBSpectrumType) v.get(0));
					            subData.setSpectrumKeywords("bmrb");
					            boolean[] isDoubleBondSpecified = new boolean[subData.getMolWithH().getBondCount()];
					            AllRingsFinder arf = new AllRingsFinder();
					            mol=subData.getMolWithH();
					            IRingSet ringSet=arf.findAllRings(mol);
					            Iterator<IBond> it=mol.bonds().iterator();
					            int c=0;
					            while(it.hasNext()) {
					            	IBond bond=it.next();
					            	if (AtomUtils.isValidDoubleBondConfiguration(mol,bond,ringSet)) {
					            		isDoubleBondSpecified[c] = true;
					            	}
					            	c++;
					            }
					            subData.setDoubleBondConfiguration(true);
					            subData.setIsDoubleBondSpecified(isDoubleBondSpecified);
					            if(in13c)
					            	subData.setComment(files[i].getName()+"; multplicities generated from proton count");
					            else
					            	subData.setComment(files[i].getName());
					            subData.setSpectrumLinks("https://bmrb.io/ftp/pub/bmrb/metabolomics/entry_directories/"+files[i].getName().substring(0,files[i].getName().length() - 4)+";link to bmrb data");
								  StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
								  GeneralUtils.getExistingMolecule(subData.getMolWithHCount(), subData.getMolWithH(), new boolean[subData.getMolWithH().getBondCount()], null, subData.getInputMolAsMdl(null,null,false), ssb);
								  subData.setSmilesNonChiralProv(ssb.string1);
								  subData.setIsChiralProv(ssb.bool);
								  if(ssb.bool){
									  subData.setSmilesChiralProv(ssb.string2);
								  }
								  inshifts=1;
				        	}
				        	if(inshifts==1 && (line.contains("_Assigned_peak_chem_shift.Val") || line.contains("_Atom_chem_shift.Val") || line.contains("_Assigned_peak_chem_shift.Atom_ID") || line.contains("_Atom_chem_shift.Atom_ID")  )) {//
				        		inshifts=2;
				        	}else if(inshifts==2 && (line.contains("_Assigned_peak_chem_shift.Atom_ID") || line.contains("_Atom_chem_shift.Atom_ID") || line.contains("_Assigned_peak_chem_shift.Val") || line.contains("_Atom_chem_shift.Val")  )) {//
				        		inshifts=3;
				        	}
				        	if(inshifts==1 && (line.contains("_Assigned_peak_chem_shift") || line.contains("_Atom_chem_shift"))) {//
				        		skip1++;
				        	}
				        	if(inshifts==2 && (line.contains("_Assigned_peak_chem_shift") || line.contains("_Atom_chem_shift"))) {//
				        		skip2++;
				        	}
				        	if(inshifts==2 && line.contains("_Assigned_peak_chem_shift"))//
				        		type=false;
				        	if(inshifts==2 && line.contains("_Atom_chem_shift")) {//
				        		type=true;
				        	}
				        	if(inshifts==3 && line.contains(files[i].getName().substring(0, files[i].getName().length() - 4))) {//
				        		StringTokenizer st=new StringTokenizer(line," ");
				        		for(int k=0;k<skip1;k++)
				        			st.nextToken();
				        		String values=null;
				        		String atoms=null;
				        		if(!type)
				        			values=st.nextToken();
				        		else
				        			atoms=st.nextToken();
				        		for(int k=0;k<skip2-1;k++)
				        			st.nextToken();		
				        		if(type)
				        			values=st.nextToken();
				        		else
				        			atoms=st.nextToken();
				        		double value=0;
				        		if(!values.equals("?") && !atoms.equals("?")) {
					        		value=Double.parseDouble(values);
					        		int atom=Integer.parseInt(atoms.substring(1));
					        		if(move>0 && atoms.charAt(0)=='H') {
					        			atom+=1;
					        		}
						            CMLPeakList signals;
						            if(atoms.charAt(0)=='C')
						            	signals=peaks13c;
						            else
						            	signals=peaks1h;
						            /*boolean added=false;
						            for(int l=0;l<signals.getPeakElements().size();l++){
						              if(signals.getPeakElements().get(l).getXValue()==value){
						            	signals.getPeakElements().get(l).setAtomRefs(SubmittingData.addAtomToAtomRefs(signals.getPeakElements().get(l).getAtomRefs(),subData.getMolWithH().getAtom(atom-1)));
						                added=true;
						              }
						            }
						            if(!added){*/
					            	CMLPeak peak = new CMLPeak();
					            	peak.setXValue(value);
					            	peak.setYValue(0);						            	
					            	String[] oldRefs=peak.getAtomRefs();
					            	if(oldRefs==null)
					            		oldRefs=new String[0];
					            	String[] newRefs=new String[oldRefs.length+1];
					            	newRefs[newRefs.length-1]="a"+atom;
					            	peak.setAtomRefs(newRefs);
					            	signals.addPeak(peak);
					            }else if(!values.equals("?")){
					            	unassignedshifts.append(value+";");
					            }
				        	}
				        	if(line.contains("stop_")) {
				        		inshifts=0;
				        		skip1=0;
				        		skip2=0;
				        	}
				        	if(line.equals("save_")) {
				        		inshifts=0;
				        		skip1=0;
				        		skip2=0;
				        		inset=false;
				        		sampleid=0;
				        		if((in1h ^ in13c)) {
				        			if(in1h) {
				        				for(CMLPeak peak : peaks1h.getPeakChildren())
				        					subData.addToSignalstable(peak);
				        			}else {
				        				for(CMLPeak peak : peaks13c.getPeakChildren())
				        					subData.addToSignalstable(peak);				        				
				        			}
				        		}
				        		if((in1h ^ in13c) && subData.getSignalstable().getPeakChildren().size()>0) {
					            	List<DBConditionType> conds=DBSpectrumPeer.retrieveByPK(57728).getMeasurementConditions().stream().map(x -> {
										try {
											return x.getDBConditionType();
										} catch (TorqueException e) {
											e.printStackTrace();
											return new DBConditionType();
										}
									}).collect(Collectors.toList());
					            	for(int k=0;k<conds.size();k++){
					            		DBConditionType cond=conds.get(k);
								        if(cond.getConditionTypeId()==1)
								        	cond.setCurrentValue(temp.get(conditionid));
								        if(cond.getConditionTypeId()==2)
								          	cond.setCurrentValue(solvent.get(setid));
								        if(cond.getConditionTypeId()==3)
								        	cond.setCurrentValue(fieldstring);
								        if(cond.getConditionTypeId()==11)
								        	cond.setCurrentValue("Unreported");
					            	}
						        	subData.setConditions(conds);
						        	if(unassignedshifts.length()>0) {
						        		subData.setComment(subData.getComment()+"; unassigned shifts at "+GeneralUtils.removeLastComma(unassignedshifts));
						        	}
						            subData.save(user.getUserId(), "true", args[2], new Date(), null, Integer.parseInt(nmrprops.getProperty("hosecode.spheres")), true);
						            if(subData.getExistingMoleculeId()==0) {
							            DBAtom[] atoms=DBMoleculePeer.retrieveByPK(subData.getMoleculeId()).getAtomsAsArrayInMdlOrder();
							            int k=0;
							            for(IAtom atom : mol3d.atoms()) {
							            	try {
							            	DBCoordinateSet3d set=new DBCoordinateSet3d(atom.getPoint3d().x, atom.getPoint3d().y, atom.getPoint3d().z, 7, atoms[k].getAtomId());
							            	set.save();
							            	k++;
							            	}catch(Exception ex) {
							            		
							            	}
							            }
						            }
				                    com.lowagie.text.Document reportpdf = new com.lowagie.text.Document();
				                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				                    PdfWriter.getInstance(reportpdf, baos);
				                    reportpdf.open();
				                    long[] morgan=Canon.symmetry(subData.getMolOriginal(), GraphUtil.toAdjList(subData.getMolOriginal()));
				    				double[] mark=SpectrumUtils.calculateScore(subData.getMolWithH(), subData.getMolOriginal(), subData, null, subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") ? GeneralUtils.getPredictionLimits("1H") : GeneralUtils.getPredictionLimits("13C"), "", reportpdf, null, morgan, GeneralUtils.getNmrshiftdbProperty("defaulturl"), null, null, null, null, null, null, new StringBuffer(),null);
				    				reportpdf.close();
				                    DBSpectrumFile specfile = new DBSpectrumFile();
				                    specfile.setSpectrumId(subData.getSpectrumid());
				                    specfile.setType(DBSpectrum.NMRSHIFTDB_TYPE);
				                    specfile.setFormat("nmrshiftdb2");
				                    byte[] base64 = new Base64().encode(baos.toByteArray());
				                    specfile.setData(base64);
				                    specfile.save();
				                    String marktext=SpectrumUtils.makeJudgement((int)mark[0], mark[5],true);
				                    DBUserDBSpectrumMark specrummark=new DBUserDBSpectrumMark();
				                    specrummark.setUserId(NmrshiftdbUserPeer.getByName("admin").getUserId());
				                    specrummark.setSpectrumId(subData.getSpectrumid());
				                    specrummark.setMarkDate(new Date());
				                    specrummark.setComment(marktext);
				                    specrummark.setMark((int)mark[0]);
				                    specrummark.save();
				                    saved++;
				        		}
				        		if(in1h) {
				        			in1h=false;
				        		}
				        		if(in13c) {
				        			in13c=false;
				        		}
				        	}
				        }
				        System.err.println("Wrote "+files[i].getName());
				        if(saved==0)
				        	System.out.println("none saved");
				        else
				        	files[i].delete();
		            } catch (Exception ex) {
		            	ex.printStackTrace();
		            }
		        }
		    }
		  }

		  private static void executeMakeDumpSelective(String[] args) throws TorqueException, IOException, SQLException {
			  TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
			  config.initialize();
			  Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
		        List<Record> l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM left join USER_SPECTRUM_MARK using (SPECTRUM_ID) where REVIEW_FLAG=\"true\" and (USER_SPECTRUM_MARK.MARK>=5 or MARK is null) and SPECTRUM_TYPE_ID=4 order by MOLECULE_ID;");
		        File outputFileCml = new File("nmrshiftdb2ph.xml");
		        FileOutputStream baoscml=new FileOutputStream(outputFileCml);
		        Writer cmlout = new OutputStreamWriter(baoscml, "UTF8");
		        cmlout.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<cml xmlns=\"http://www.xml-cml.org/schema\">"));
		        List<DBConditionType> conditiontypes=DBConditionTypePeer.doSelect(new Criteria());
		        Connection dbconn = Torque.getConnection();
		        Statement stmt = dbconn.createStatement();
		        for(int i=0;i<l.size();i++){
		         try{
		            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
		            System.out.println(mol.getMoleculeId());
		            List spectra=mol.selectSpectra();
		            StringTokenizer st=new StringTokenizer(mol.getCML(1,true).getChild(0).toXML(),"<");
		            while(st.hasMoreTokens())
	            		cmlout.write(new String("\r\n<"+st.nextToken()));
		            for(int k=0;k<spectra.size();k++){
		              DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
		              if(spectrum.getMark()>spectrum.getDisplaymark() && spectrum.getSpectrumTypeId()==4){
			              st=new StringTokenizer(spectrum.getCmlSpect(false, URL).getChild(0).toXML(),"<");
			              while(st.hasMoreTokens())
			            	  cmlout.write(new String("\r\n<"+st.nextToken()));
		              }
		            }
		          }
		          catch(Exception ex){
		            GeneralUtils.logError(ex,"loop in buildsdf",null,false);
		          }
		        }
		        cmlout.write(new String("\r\n</cml>"));
		        cmlout.close();
		  }
          private static void doCheckDois(String[] args) throws Exception {
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
              Connection dbconn = Torque.getConnection();
	          Statement stmt = dbconn.createStatement();
              ResultSet rs = stmt.executeQuery("select distinct MOLECULE_ID from MOLECULE where DOI is not null");
              while (rs.next()) {
                      try {
                              DBMolecule dbmol= DBMoleculePeer.retrieveByPK(new NumberKey(rs.getInt(1)));
                              System.err.println(dbmol.getMoleculeId());
							  URL url=new URL("https://doi.org/"+dbmol.getDoi());    
							  URLConnection urlConnection = url.openConnection(); // creating a urlconnection object  
						      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));  
						      String line;  
						      while ((line = bufferedReader.readLine()) != null)  
      						  {  
								if(line.contains("Error: DOI Not Found")){
									System.out.println(dbmol.getMoleculeId());
									break;
								}
							  }
						      bufferedReader.close();  
                      }
                      catch(Exception ex) {
                              ex.printStackTrace();
                      }
              }
              rs = stmt.executeQuery("select DATASET_DOI_ID from DATASET_DOI where DOI is not null");
              while (rs.next()) {
				      DBDatasetDoi dbmol=null;
                      try {
                              dbmol= DBDatasetDoiPeer.retrieveByPK(new NumberKey(rs.getInt(1)));
                              //System.err.println(dbmol.getMoleculeId());
							  URL url=new URL("https://doi.org/"+dbmol.getDoi());    
							  URLConnection urlConnection = url.openConnection(); // creating a urlconnection object  
						      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));  
						      String line;  
						      while ((line = bufferedReader.readLine()) != null)  
      						  {  
								if(line.contains("Error: DOI Not Found")){
									System.out.println(dbmol.getMoleculeId());
									break;
								}
							  }
						      bufferedReader.close();  
                      }
                      catch(Exception ex) {
                              ex.printStackTrace();
                      }
              }
          }
          
		  private static void writeReversibleData(String[] args) throws Exception{
              TurbineConfig config = new TurbineConfig(args[1], "../conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
              config.initialize();
              Torque.init(args[1]+"../conf/jetspeed/WEB-INF/conf/Torque.properties");
	                    List list = DBMoleculePeer.executeQuery("SELECT DISTINCT MOLECULE.MOLECULE_ID, FORMULA FROM MOLECULE, SPECTRUM  LEFT JOIN (select MOLECULE_ID, SYMBOL, count(*) AS COUNT from ATOM group by MOLECULE_ID, SYMBOL) AS C_AC ON SPECTRUM.MOLECULE_ID = C_AC.MOLECULE_ID  LEFT JOIN (select MOLECULE_ID, SYMBOL, count(*) AS COUNT from ATOM group by MOLECULE_ID, SYMBOL) AS H_AC ON SPECTRUM.MOLECULE_ID = H_AC.MOLECULE_ID  LEFT JOIN (select MOLECULE_ID , count(Distinct SYMBOL) as c from ATOM group by MOLECULE_ID) as d3 ON SPECTRUM.MOLECULE_ID = d3.MOLECULE_ID  WHERE SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and  (SPECTRUM.REVIEW_FLAG = 'true' or  SPECTRUM.REVIEW_FLAG =  'edited' ) and (C_AC.SYMBOL = 'C' AND C_AC.COUNT >= 0 AND C_AC.COUNT <= 15 AND H_AC.SYMBOL = 'H' AND H_AC.COUNT >= 0 AND H_AC.COUNT <= 2147483647 AND d3.c = 2 ) group by SMILES_STRING order by MOLECULAR_WEIGHT");                    File outputFile = new File("reversibledata.csv");
	                    FileOutputStream baos=new FileOutputStream(outputFile);
	                    Writer out = new OutputStreamWriter(baos, "UTF8");
	                    for(int i=0;i<list.size();i++){
	                        DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) list.get(i)).getValue(1).asString()));
	System.out.println(mol.getMoleculeId());
	DBSpectrum s13c=null;
	                        for(DBSpectrum spectrum : mol.selectSpectra()) {
	                                if(spectrum.getSpectrumTypeId()==1 && spectrum.getConditionType().equals("m")) {                                        s13c=spectrum;
	                                        break;
	                                }
	                                if(spectrum.getSpectrumTypeId()==1 && spectrum.getConditionType().equals("p")) {                                        s13c=spectrum;
	                                        break;
	                                }
	                        }
	//System.out.println(s13c);
	if(s13c!=null) {
	                                out.write(mol.getMoleculeId()+","+s13c.getSpectrumId()+",");
	                                IAtomContainer cdkmol=mol.getAsCDKMoleculeWithoutH();
                        //Build adj matrix and write
                        int[][] matrix=new int[15][15];
                        long[] numbers = InChINumbersTools.getNumbers(cdkmol);
                        Map<Integer,Long> atommap=new HashMap<Integer, Long>();
                        for(int k=0;k<numbers.length;k++) {
                                atommap.put(k, numbers[k]);
                                //System.out.println(k+" "+numbers[k]);
                        }
                        //System.out.println(cdkmol.getBondCount());
                        for(IBond bond : cdkmol.bonds()) {
                                if(cdkmol.getAtomNumber(bond.getAtom(0))<15 && cdkmol.getAtomNumber(bond.getAtom(1))<15){
                                long a1=atommap.get(cdkmol.getAtomNumber(bond.getAtom(0)));
                                long a2=atommap.get(cdkmol.getAtomNumber(bond.getAtom(1)));
                                if(a1<15 && a2<15){
                                        matrix[(int)a1][(int)a2]=bond.getOrder().ordinal();
                                        matrix[(int)a2][(int)a1]=bond.getOrder().ordinal();
                                }}
                        }
                        for(int k=0;k<15;k++) {
                            for(int l=k+1;l<15;l++) {
                                    System.out.println(k+" "+l);
                                    out.write(Character.forDigit(matrix[k][l],10));
                            }
                    }
                    out.write(",");
                    //Buils spectrum string and write
                    boolean[] bits=new boolean[225];
                    for(DBSignal signal : s13c.getDBSignals()) {
                            if(signal.getDBShift(1).getValue()<=0)
                                    bits[0]=true;
                            else if(signal.getDBShift(1).getValue()>223)
                                    bits[224]=true;
                            else
                                    bits[(int)signal.getDBShift(1).getValue()]=true;
                    }
                    for(int k=0;k<225;k++) {
                            out.write(bits[k] ? "1" : "0");
                    }
                    out.write("\n");
                }
	            }
	            out.close();
		  }
          
          
          

}
