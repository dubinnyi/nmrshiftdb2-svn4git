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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
//import javax.xml.bind.JAXBException;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IElectronContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.HOSECodeGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBLabGroupPeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NNResult;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.OneToManyTable;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ValuesForVelocityComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import VisualNumerics.math.Statistics;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_predict;

/**
 * This is the portlet handling predictions of spectra.
 * 
 * @author Stefan Kuhn
 * @created 18. Februar 2002
 */
public class PredictAction extends VelocityPortletAction {

    protected static final String MODE = "mode";
    protected static final Object MODE_ASSIGNMENT = "mode_assignment";
    private static final Object MODE_PREDICTION = "mode_prediction";
    private static svm_model model[] = new svm_model[5];

    // private static Instances instances=null;
    // private static J48[] j48=null;

    /**
     * Builds a context for the predict page.
     * 
     * @param session The current session.
     * @param calculated Use calculated.
     * @param measured Use measured.
     * @param messages Messages to display.
     * @param servconorrundata Description of Parameter
     * @param request Description of Parameter
     * @param print Description of Parameter
     * @param force3d				true=Use 3d hose codes always, 2d predictions are ignored, false=fall back on 2d for every sphere.
     * @exception IOException Description of Exception.
     * @exception FileNotFoundException Description of Exception.
     * @throws TorqueException 
     * @throws TooManyRowsException 
     * @throws NoRowsException 
     * @throws CloneNotSupportedException 
     * @throws CDKException 
     * @throws ClassNotFoundException 
     * @throws SQLException 
     * @throws DataSetException 
     * @throws NmrshiftdbException 
     * @throws TranscoderException 
     * @throws JAXBException 
     */
    public static void fillPredictContext(Context context, HttpSession session, boolean calculated, boolean measured, boolean hose3d, boolean force3d, StringBuffer messages, Object servconorrundata, HttpServletRequest request, boolean print, boolean doAssignment, List<CMLPeak> peaksToAssign) throws IOException, FileNotFoundException, NoRowsException, TooManyRowsException, TorqueException, CloneNotSupportedException, ClassNotFoundException, CDKException, SQLException, NmrshiftdbException, DataSetException, TranscoderException {
        DecimalFormat myFormatter = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        ServletConfig servcon;
        if (servconorrundata instanceof RunData) {
            servcon = ((RunData) servconorrundata).getServletConfig();
        } else {
            servcon = (ServletConfig) servconorrundata;
        }
        String input = ((StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT)).toString();
        context.put(NmrshiftdbConstants.INPUT, input);
        // formatting stuff
        if (session.getAttribute("twospheres") == null) {
            session.setAttribute("twospheres", "false");
        }
        if (request.getParameter("twospheres") != null) {
            session.setAttribute("twospheres", request.getParameter("twospheres"));
        }
        if (session.getAttribute("hosecode") == null) {
            session.setAttribute("hosecode", "false");
        }
        if (request.getParameter("hosecode") != null) {
            session.setAttribute("hosecode", request.getParameter("hosecode"));
        }
        context.put("hosecode", session.getAttribute("hosecode"));
        context.put("twospheres", session.getAttribute("twospheres"));
        // The atoms in the current molecule of the element of the choosen spectrum type
        Vector<ValuesForVelocityBean> elementsOfChoosenType = new Vector<ValuesForVelocityBean>();
        // This is used for remembering the predictions which are used for sorting the shifts
        List<CMLPeak> predictions = new Vector<CMLPeak>();
        Map<Double, double[]> predictionValuesForAppletMap = new HashMap<Double, double[]>();
        context.put("choosennumber", session.getAttribute("choosennumber"));
        if (print) {
            context.put("print", "1");
        } else {
            context.put("print", "0");
        }
        Vector<StringAndInt> numbers = new Vector<StringAndInt>();
        for (int i = 0; i <= Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres", servcon)); i++) {
            if (i == 0) {
                numbers.add(new StringAndInt("Cdk", i));
            } else {
                numbers.add(new StringAndInt(i + "", i));
            }
        }
        context.put("numbers", numbers);
        StringBuffer spectrumForApplet = new StringBuffer();
        StringBuffer tooltips = new StringBuffer();
        String predictAtomType = "";
        String solventid=request.getParameter("solvent");
        DBCondition solvent=null;
        if(solventid!=null && !solventid.equals("-1"))
        	solvent=DBConditionPeer.retrieveByPK(Integer.parseInt(solventid));
        IAtomContainer mol = (IAtomContainer) session.getAttribute("mol");
        if (session.getAttribute("trueonly") == null)
            session.setAttribute("trueonly", new Boolean(true));
        if (servconorrundata instanceof RunData) {
            if(((RunData) servconorrundata).getParameters().getString("spectrumType")!=null)
                session.setAttribute("spectype", ((RunData) servconorrundata).getParameters().getString("spectrumType"));
            if (session.getAttribute("spectype") == null)
                session.setAttribute("spectype", "1");
            if(((RunData) servconorrundata).getParameters().getString("solvent")!=null)
                session.setAttribute("solvent", ((RunData) servconorrundata).getParameters().getString("solvent"));
            if (session.getAttribute("solvent") == null)
                session.setAttribute("solvent", "-1");
        } else {
            if (request.getParameter("spectrumType") != null) {
                session.setAttribute("spectype", request.getParameter("spectrumType"));
            } else if (session.getAttribute("spectype") == null) {
                session.setAttribute("spectype", "1");
            }
            if (request.getParameter("solvent") != null) {
                session.setAttribute("solvent", request.getParameter("solvent"));
            } else if (session.getAttribute("solvent") == null) {
                session.setAttribute("solvent", "-1");
            }
        }
        if (((String) session.getAttribute("spectype")).equals("100")) {
            predictAtomType = "Hsvm";
        } else if (((String) session.getAttribute("spectype")).equals("101")) {
            predictAtomType = "Hnn";
        } else if (((String) session.getAttribute("spectype")).equals("102")) {
            predictAtomType = "Cnn";
        } else if (((String) session.getAttribute("spectype")).equals("103")) {
            predictAtomType = "All";
        } else {
            predictAtomType = DBSpectrumTypePeer.retrieveByPK(new NumberKey((String) session.getAttribute("spectype"))).getDBIsotope(1).getElementSymbol();
        }
        IAtomContainer molh = null;
        Map<Integer,String> shifts=new HashMap<>();
        if (input.equals("0")) {
            molh = (IAtomContainer) mol.clone();
            Iterator<IBond> iter = molh.bonds().iterator();
            List<IBond> bondstoremove=new ArrayList<>();
            while(iter.hasNext()){
              IBond bond=iter.next();
              if(bond.getOrder()==null) {
                IBond newBond=bond.getBuilder().newBond();
                newBond.setAtom(bond.getAtom(0), 0);
                newBond.setAtom(bond.getAtom(1), 1);
                newBond.setStereo(bond.getStereo());
                newBond.setOrder(IBond.Order.ZERO);
                bondstoremove.add(bond);
                molh.addBond(newBond);
              }
            }
            for(IBond bond : bondstoremove){
              molh.removeBond(bond);
            }
            //List<Integer> equivalences=new ArrayList<Integer>();
            if(AtomUtils.isOrganometallic(molh)) {
                AtomUtils.doSimpleSaturation(molh,null);
                for(IAtom atom : molh.atoms()){
                  if(atom.getPoint2d()==null)
                    atom.setPoint2d(new Point2d(0,0));
                }
                if(predictAtomType.equals("Hsvm") || predictAtomType.equals("Hnn") || predictAtomType.equals("Cnn")) {
                	messages.append("For organometallic compounds, only the HOSE code prediction is possible");
                }
                AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(molh);
                AtomUtils.doReplacement(molh);
            }else {
                try {
                    AtomUtils.normalize(mol, ServletUtils.expandRelative(servcon, "/WEB-INF/conf/normalizer.xml"));
                } catch (Exception ex) {
                    // Ignore if normalization fails
                }
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                CDKHueckelAromaticityDetector.detectAromaticity(mol);
	            AtomUtils.addAndPlaceHydrogens(molh);
	            AtomUtils.makeUpDownBonds(molh);
	            for(IAtom atom: mol.atoms())
	            	atom.setImplicitHydrogenCount(0);
	            Kekulization.kekulize(mol);
            }
            HOSECodeGenerator hcg = new HOSECodeGenerator();
            if (predictAtomType.equals("Hsvm")) {
                boolean predictbyhose = ((String) session.getAttribute("hosecode")).equals("true");
                HashMap<IAtom, String> hcmap = new HashMap<IAtom, String>();
                StringBuffer[] comments = new StringBuffer[molh.getAtomCount()];
                //We make a completely new copy of the molecule via smiles (not nice, but there were problesm with clone etc.)
                SmilesGenerator sg = SmilesGenerator.unique().aromatic();;
                String smiles = sg.create(molh);
                SmilesParser sp = new SmilesParser(mol.getBuilder());
                IAtomContainer mol2 = sp.parseSmiles(smiles);
                List<RMap> list=new UniversalIsomorphismTester().getSubgraphAtomsMap(mol2,mol);
                IAtom[] newatoms=new IAtom[mol2.getAtomCount()];
                for(int i=0;i<list.size();i++){
                  newatoms[((RMap)list.get(i)).getId2()]=mol2.getAtom(((RMap)list.get(i)).getId1());
                }
                int free=list.size();
                for(int i=0;i<mol2.getAtomCount();i++){
                  if(mol2.getAtom(i).getSymbol().equals("H")){
                    newatoms[free]=mol2.getAtom(i);
                    free++;
                  }
                }
                mol2.setAtoms(newatoms);
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol2);
                ElectronDonation modeled       = ElectronDonation.cdk();
                CycleFinder      cycles      = Cycles.cdkAromaticSet();
                Aromaticity      aromaticity = new Aromaticity(modeled, cycles);
                aromaticity.apply(mol2);
                try{
                    mol2 = new DeduceBondSystemTool().fixAromaticBondOrders(mol2);
                }catch(Exception ex){
                    //this gives errors in case molecule has no aromatic rings
                }
                AllRingsFinder arf = new AllRingsFinder();
                IRingSet ringSet = arf.findAllRings(mol);
                Criteria crit = new Criteria();
                crit.add(DBConditionPeer.CONDITION_TYPE_ID, "1");
                crit.add(DBConditionPeer.VALUE, "298");
                DBCondition temp = (DBCondition) DBConditionPeer.doSelect(crit).get(0);
                crit = new Criteria();
                crit.add(DBConditionPeer.CONDITION_TYPE_ID, "3");
                crit.add(DBConditionPeer.VALUE, "125");
                DBCondition field = (DBCondition) DBConditionPeer.doSelect(crit).get(0);
                if(solvent==null){
                	//TODO use undefined?
	                crit = new Criteria();
	                crit.add(DBConditionPeer.CONDITION_TYPE_ID, "2");
	                crit.add(DBConditionPeer.VALUE, "Chloroform-D1 (CDCl3)");
	                solvent = (DBCondition) DBConditionPeer.doSelect(crit).get(0);
                }
                try {
                    mol = GeneralUtils.getModelBuilder("mm2").generate3DCoordinates(mol2, false);
                } catch (Exception ex) {
                    messages.append("We could not generate 3d coordinates, will use 2d coordianates. Probably the prediction is not as reliable as it could be!<br>");
                    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                    sdg.setMolecule(mol2);
                    sdg.generateCoordinates();
                    mol = sdg.getMolecule();
                    for (int i = 0; i < mol.getAtomCount(); i++) {
                        mol.getAtom(i).setPoint3d(new Point3d(mol.getAtom(i).getPoint2d().x, mol.getAtom(i).getPoint2d().y, 0));
                    }
                }
                CharArrayWriter[] writer = new CharArrayWriter[5];
                int numberofentries[] = new int[5];
                ResultSetMetaData[] meta = new ResultSetMetaData[5];
                OneToManyTable<String, Double> otmt = new OneToManyTable<String, Double>();
                Connection dbconn = null;
                Statement stmt = null;
                float[][] divisor = new float[4][];
                float[][] minvalue = new float[4][];
                try{
                    dbconn = Torque.getConnection();
                    stmt = dbconn.createStatement();
                    for (int i = 1; i < 5; i++) {
                        writer[i] = new CharArrayWriter();
                        ResultSet rs = stmt.executeQuery("select * from DESCRIPTORS_" + i + " limit 1");
                        meta[i] = rs.getMetaData();
                    }
                    for (int i = 0; i < 4; i++) {
                        // perhaps use true value instead of 400
                        divisor[i] = new float[600];
                        minvalue[i] = new float[600];
                    }
                    String sql = "SELECT MIN_VALUE, DIVISOR, DESCRIPTOR, PROTONCLASS from DESCRIPTOR_FACTORS";
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        minvalue[rs.getInt(4) - 1][Integer.parseInt(rs.getString(3).substring(3))] = rs.getFloat(1);
                        divisor[rs.getInt(4) - 1][Integer.parseInt(rs.getString(3).substring(3))] = rs.getFloat(2);
                    }
                }finally{
                    Torque.closeConnection(dbconn);
                }
                long[] mn = null;
                if (!predictbyhose)
                    mn = MorganNumbersTools.getMorganNumbers(mol);
                for (int l = 0; l < mol.getAtomCount(); l++) {
                    if (mol.getAtom(l).getSymbol().equals("H")) {
                        int protonclass = AtomUtils.getProtonClass(mol, l, ringSet);
                        StringBuffer hoseCode = new StringBuffer();
                        if (predictbyhose) {
                            hoseCode.append(hcg.getHOSECode(mol, (IAtom) mol.getConnectedAtomsList(mol.getAtom(l)).get(0), 10, true));
                        } else {
                            hoseCode.append(mn[l] + "");
                        }
                        IAtom heavyatom = (IAtom) mol.getConnectedAtomsList(mol.getAtom(l)).get(0);
                        int protoncount = 0;
                        for (int i = 0; i < mol.getConnectedAtomsList(heavyatom).size(); i++) {
                            if (((IAtom) mol.getConnectedAtomsList(heavyatom).get(i)).getSymbol().equals("H"))
                                protoncount++;
                        }
                        boolean second = false;
                        if (protoncount == 2) {
                            for (int i = 0; i < mol.getConnectedAtomsList(heavyatom).size(); i++) {
                                if (((IAtom) mol.getConnectedAtomsList(heavyatom).get(i)).getSymbol().equals("H")) {
                                    if (((IAtom) mol.getConnectedAtomsList(heavyatom).get(i)) == mol.getAtom(l)) {
                                        break;
                                    } else {
                                        second = true;
                                        break;
                                    }
                                }
                            }
                            hoseCode.append(second ? "2" : "1");
                        }
                        hcmap.put(mol.getAtom(l), hoseCode.toString());
                        float[] descriptors = AtomUtils.getDescriptorSet(temp, field, solvent, mol, mol.getAtom(l), AtomUtils.getAllRings(), protonclass);
                        mol.getAtom(l).setProperty("protonclass", new Integer(protonclass));
                        StringBuffer line = new StringBuffer();
                        line.append("0 ");
                        int colsnumber = meta[protonclass].getColumnCount();
                        int currentnumber = 1;
                        for (int k = 3; k <= colsnumber; k++) {
                            int desnumber = Integer.parseInt(meta[protonclass].getColumnLabel(k).substring(3));
                            line.append((currentnumber) + ":" + ((descriptors[desnumber] - minvalue[protonclass - 1][desnumber]) / divisor[protonclass - 1][desnumber]) + " ");
                            currentnumber++;
                        }
                        line.append("\n");
                        writer[protonclass].write(line.toString());
                        numberofentries[protonclass]++;
                        comments[l] = new StringBuffer("");
                    }
                }
                BufferedReader[] in = new BufferedReader[5];
                for (int i = 1; i < 5; i++) {
                    if (numberofentries[i] != 0) {
                        writer[i].close();
                        BufferedReader inputreader = new BufferedReader(new CharArrayReader(writer[i].toCharArray()));
                        if (model[i] == null)
                            model[i] = svm.svm_load_model(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/descsvm.model" + i));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        svm_predict.predict(inputreader, new DataOutputStream(baos), model[i], 0);
                        in[i] = new BufferedReader(new StringReader(baos.toString()));
                    }
                }
                for (int l = 0; l < mol.getAtomCount(); l++) {
                    if (mol.getAtom(l).getSymbol().equals("H")) {
                        int protonclass = ((Integer) mol.getAtom(l).getProperty("protonclass")).intValue();
                        String hoseCode = (String) hcmap.get(mol.getAtom(l));
                        Double value = new Double(in[protonclass].readLine());
                        otmt.put(hoseCode, value);
                        if (predictbyhose)
                            comments[l].append("SVM: " + (value.floatValue()));
                    }
                }
                boolean[] alreadyDone = new boolean[mol.getAtomCount()];
                for (int l = 0; l < mol.getAtomCount(); l++) {
                    if (mol.getAtom(l).getSymbol().equals("H")) {
                        String hoseCode = (String) hcmap.get(mol.getAtom(l));
                        IAtom heavyatom = (IAtom) mol.getConnectedAtomsList(mol.getAtom(l)).get(0);
                        int protoncount = 0;
                        for (int i = 0; i < mol.getConnectedAtomsList(heavyatom).size(); i++) {
                            if (((IAtom) mol.getConnectedAtomsList(heavyatom).get(i)).getSymbol().equals("H"))
                                protoncount++;
                        }
                        int mothernumber = mol.getAtomNumber((IAtom) mol.getConnectedAtomsList(mol.getAtom(l)).get(0));
                        if (!alreadyDone[mothernumber] || protoncount == 2) {
                            if (protoncount != 2) {
                                Vector<Double> ev = new Vector<Double>();
                                for (Iterator<Double> e = otmt.get(hoseCode).iterator(); e.hasNext();) {
                                    ev.add(e.next());
                                }
                                double[] values = new double[ev.size()];
                                for (int m = 0; m < ev.size(); m++) {
                                    values[m] = ev.get(m).doubleValue();
                                }
                                double value = Statistics.average(values);
                                String valuestr = myFormatter.format(value);
                                elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(value), mothernumber + "", 0, 1000, l + "", new String("" + (mothernumber + 1) + "-H")));
                                double[] statistics = { 0, value, 0, 0, 0, 0, 0, 0, 0, 0 };
                                StringBuffer comment = new StringBuffer();
                                StringBuffer hoseCodePrediction = new StringBuffer();
                                if (predictbyhose) {
                                    double[] statisticshose = AtomUtils.predictRange(mol, mol.getAtom(l), calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, hoseCodePrediction, ((Boolean) session.getAttribute("trueonly")).booleanValue(), solvent);
                                    comments[l].append("; HOSE code: " + myFormatter.format(statisticshose[1]) + " with code " + hoseCodePrediction);
                                } else {
                                    alreadyDone[mothernumber] = true;
                                }
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).statistics = statistics;
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setCurrentSelectedNumber(new Integer(mothernumber + 1));
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setDelta(comments[l].toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setIntensity(hoseCodePrediction.toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity(comment.toString());
                                if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) {
                                    spectrumForApplet.append("S(0," + protoncount + "," + (mol.getAtomNumber(heavyatom) + 1) + ")=" + valuestr + "|");
                                } else {
                                    spectrumForApplet.append((valuestr) + ";" + 0 + ";" + l + "|");
                                }
                                tooltips.append((mol.getAtomNumber(heavyatom) + 1) + "|" + valuestr + "|");
                            } else {
                                Vector<Double> ev = new Vector<Double>();
                                for (Iterator<Double> e = otmt.get(hoseCode).iterator(); e.hasNext();) {
                                    ev.add(e.next());
                                }
                                double[] values = new double[ev.size()];
                                for (int m = 0; m < ev.size(); m++) {
                                    values[m] = ev.get(m);
                                }
                                double value = Statistics.average(values);
                                String valuestr = myFormatter.format(value);
                                elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(value), mothernumber + "", 0, 1000, l + "", new String("" + (mothernumber + 1) + "-H (H " + (l + 1) + ")")));
                                double[] statistics = { 0, value, 0, 0, 0, 0, 0, 0, 0, 0 };
                                StringBuffer comment = new StringBuffer();
                                StringBuffer hoseCodePrediction = new StringBuffer();
                                if (predictbyhose) {
                                    double[] statisticshose = AtomUtils.predictRange(mol, mol.getAtom(l), calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, hoseCodePrediction, ((Boolean) session.getAttribute("trueonly")).booleanValue(), solvent);
                                    comments[l].append("; HOSE code: " + myFormatter.format(statisticshose[1]) + " with code " + hoseCodePrediction);
                                }
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).statistics = statistics;
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setCurrentSelectedNumber(new Integer(mothernumber + 1));
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setDelta(comments[l].toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setIntensity(hoseCodePrediction.toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity(comment.toString());
                                if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) {
                                    spectrumForApplet.append("S(0," + protoncount + "," + (mol.getAtomNumber(heavyatom) + 1) + ")=" + valuestr + "|");
                                } else {
                                    spectrumForApplet.append((valuestr) + ";" + 0 + ";" + l + "|");
                                }
                                tooltips.append((mol.getAtomNumber(heavyatom) + 1) + "|" + valuestr + "|");
                            }
                        }
                    }
                }
                context.put("coords", "3d");
            } else if (predictAtomType.equals("Hnn") || predictAtomType.equals("Cnn")) {
            	try{
            		boolean found=false;
            		for(IAtom atom : mol.atoms()){
            			if("H, C, O, N, P, S, F, Cl".indexOf(atom.getSymbol())<0){
            				messages.append("For NN prediction, only atoms H, C, O, N, P, S, F, and Cl are allowed");
            				found=true;
            			}
            		}
            		if(!found){
		            	FileOutputStream out=new FileOutputStream(new File(System.getProperty("java.io.tmpdir")+"/nnmol.sdf"));
		            	MDLV2000Writer mdlwriter=new MDLV2000Writer(out);
		            	mdlwriter.writeMolecule(mol);
		            	mdlwriter.close();
		            	Process p = Runtime.getRuntime().exec("docker run -v "+System.getProperty("java.io.tmpdir")+":/mydata ericmjonas/respredict:beta  --nuc "+(predictAtomType.equals("Hnn")?"1H":"13C")+" --sdf_file /mydata/nnmol.sdf");
		            	BufferedReader br = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
		                String s;
		                StringBuffer sb=new StringBuffer();
		                while ((s = br.readLine()) != null)
		                	if(!s.trim().equals("[") && !s.trim().equals("]"))
		                		sb.append(s);
		                p.waitFor();
                                br.close();
		                ObjectMapper mapper = new ObjectMapper();
		                NNResult result = mapper.readValue(sb.toString().getBytes(), NNResult.class);
		                for(NNResult.Shift shift : result.getShifts()){
		                	int atomid=shift.getAtom_idx();
		                	if(predictAtomType.equals("Hnn")) {
	                            List<IAtom> connatoms = molh.getConnectedAtomsList(molh.getAtom(atomid));
	                            for (int k = 0; k < connatoms.size(); k++) {
	                                if (!((IAtom) connatoms.get(k)).getSymbol().equals("H")) {
	                                    atomid=molh.getAtomNumber((IAtom) connatoms.get(k));
	                                }
	                            }
		                	}
		                    elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(shift.getPred_mu()), atomid + "", 0, 1000, atomid + "", new String("" + (atomid + 1))));
		                	((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity(Float.toString(shift.getPred_std()));
		                	if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) {
		                	    spectrumForApplet.append("S(0," + 1 + "," + (atomid + 1) + ")=" + shift.getPred_mu() + "|");
		                	} else {
		                	    spectrumForApplet.append((shift.getPred_mu()) + ";" + 0 + ";" + (atomid+1) + "|");
		                	}
		                	shifts.put(atomid,((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).getValueAsString());
		                }
            		}
                } catch (Exception ex) {
                	context.put("error",GeneralUtils.logError(ex, "Problem using NN", null, true));
            	}
            } else {
            	//we deal with organometallic equivalences first
            	/*Map<Integer, double[]> equivalenceresults=new HashMap<>();
            	if(AtomUtils.isOrganometallic(molh)) {
                	Map<Integer,List<double[]>> equivalncepredictions = new HashMap<>();
                	Map<Integer, Integer> maxspheres=new HashMap<>();
                    for (int i = 0; i < mol.getAtomCount(); i++) {
                    	
                        if ((mol.getAtom(i).getSymbol().equals(predictAtomType) || predictAtomType.equals("H") || predictAtomType.equals("All")) && molh.getAtom(i).getProperty("equivalence")!=null) {
                    		if(!equivalncepredictions.keySet().contains(molh.getAtom(i).getProperty("equivalence")))
                    			equivalncepredictions.put(molh.getAtom(i).getProperty("equivalence"), new ArrayList<double[]>());
                            double[] statistics = null;
                            if (predictAtomType.equals("H")) {
                                List<IAtom> connatoms = molh.getConnectedAtomsList(molh.getAtom(i));
                                for (int k = 0; k < connatoms.size(); k++) {
                                    if (((IAtom) connatoms.get(k)).getSymbol().equals("H")) {
                                        IAtom hatom = (IAtom) connatoms.get(k);                                        
                                        statistics = AtomUtils.predictRange(molh, hatom, calculated, measured, hose3d, force3d, 0, new StringBuffer(), true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, new StringBuffer(), ((Boolean) session.getAttribute("trueonly")).booleanValue(),solvent);
                                    }
                                }
                            } else {
                                statistics = AtomUtils.predictRange(molh, molh.getAtom(i), calculated, measured, hose3d, force3d, 0, new StringBuffer(), true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, new StringBuffer(), ((Boolean) session.getAttribute("trueonly")).booleanValue(),solvent);
                            }
                            if(!maxspheres.keySet().contains(molh.getAtom(i).getProperty("equivalence")))
                            	maxspheres.put(molh.getAtom(i).getProperty("equivalence"), 0);
                            if(statistics[4]>maxspheres.get(molh.getAtom(i).getProperty("equivalence"))) {
                            	equivalncepredictions.get(molh.getAtom(i).getProperty("equivalence")).clear();
                            	equivalncepredictions.get(molh.getAtom(i).getProperty("equivalence")).add(statistics);
                            	maxspheres.put(molh.getAtom(i).getProperty("equivalence"),(int)statistics[4]);
                            }
                    	}
                    }
                	for(int equivalence : equivalences) {
                		double[] avgresult=new double[11];
                		avgresult[0]=Double.MAX_VALUE;
                		avgresult[2]=Double.MIN_VALUE;
                		avgresult[4]=Double.MIN_VALUE;
                		avgresult[7]=Double.MAX_VALUE;
                		avgresult[8]=Double.MIN_VALUE;
                		for(double[] statistics : equivalncepredictions.get(equivalence)) {
                			if(avgresult[0]>statistics[0])
                				avgresult[0]=statistics[0];
                			if(avgresult[2]<statistics[2])
                				avgresult[2]=statistics[2];
                			avgresult[1]+=statistics[1];
                			avgresult[3]+=statistics[3];
                			if(avgresult[4]<statistics[4])
                				avgresult[4]=statistics[4];
                			avgresult[5]+=statistics[5];
                			avgresult[6]+=statistics[6];
                			if(avgresult[7]>statistics[7])
                				avgresult[7]=statistics[7];
                			if(avgresult[8]<statistics[8])
                				avgresult[8]=statistics[8];
                			if(avgresult[9]==1)
                				avgresult[9]=1;
                			if(avgresult[10]==1)
                				avgresult[10]=1;
                		}
                		avgresult[1]=avgresult[1]/equivalncepredictions.get(equivalence).size();
                		avgresult[3]=avgresult[3]/equivalncepredictions.get(equivalence).size();
                		avgresult[6]=avgresult[6]/equivalncepredictions.get(equivalence).size();
                		equivalenceresults.put(equivalence, avgresult);
                	}
            	}*/
                for (int i = 0; i < mol.getAtomCount(); i++) {
                    if (mol.getAtom(i).getSymbol().equals(predictAtomType) || predictAtomType.equals("H") || predictAtomType.equals("All")) {
                        boolean added = false;
                        try {
                            IAtom hatom = null;
                            StringBuffer comment = new StringBuffer();
                            StringBuffer hoseCode = new StringBuffer();
                            StringBuffer hoseCode2 = new StringBuffer();
                            double[] statistics = null;
                            /*if(AtomUtils.isOrganometallic(molh) && molh.getAtom(i).getProperty("equivalence")!=null) {
                            	statistics=equivalenceresults.get(molh.getAtom(i).getProperty("equivalence"));
                            }else {*/
	                            if (predictAtomType.equals("H")) {
	                                List<IAtom> connatoms = molh.getConnectedAtomsList(molh.getAtom(i));
	                                for (int k = 0; k < connatoms.size(); k++) {
	                                    if (((IAtom) connatoms.get(k)).getSymbol().equals("H")) {
	                                        hatom = (IAtom) connatoms.get(k);
	                                        hoseCode = new StringBuffer();
	                                        statistics = AtomUtils.predictRange(molh, hatom, calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, hoseCode, ((Boolean) session.getAttribute("trueonly")).booleanValue(),solvent);
	                                    }
	                                }
	                            } else {
	                                statistics = AtomUtils.predictRange(molh, molh.getAtom(i), calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, ((Integer) session.getAttribute("choosennumber")).intValue(), true, hoseCode, ((Boolean) session.getAttribute("trueonly")).booleanValue(),solvent);
	                            }
                            //}
                            if (statistics != null) {
                                double[] statistics2 = null;
                                elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String("" + (i + 1))));
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).statistics = statistics;
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setDelta(hoseCode.toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setIntensity(hoseCode.toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setDelta(hoseCode.toString());
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity(comment.toString());
                                if(shifts.containsKey(i)){
                                	shifts.put(i,shifts.get(i)+", "+((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).getValueAsString());
                                }else{
                                	shifts.put(i,((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).getValueAsString());
                                }
                                added = true;
                                CMLPeak vt = new CMLPeak();
                                boolean alreadyContainsShift = false;
                                String newMultiplicity = "s";
                                if (AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(i)) == 1) {
                                    newMultiplicity = "d";
                                } else if (AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(i)) == 2) {
                                    newMultiplicity = "t";
                                } else if (AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(i)) == 3) {
                                    newMultiplicity = "q";
                                }
                                for (int k = 0; k < predictions.size(); k++) {
                                    if (predictions.get(k).getXValue() == statistics[1] && predictions.get(k).getPeakShape().equals(newMultiplicity)) {
                                        vt = predictions.get(k);
                                        vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(), mol.getAtom(i)));
                                        alreadyContainsShift = true;
                                        break;
                                    }
                                }
                                if (!alreadyContainsShift) {
                                    vt.setXValue(statistics[1]);
                                    vt.setAtomRefs(SubmittingData.addAtomToAtomRefs(vt.getAtomRefs(), mol.getAtom(i)));
                                    // we misuse peakShape for keeping a record
                                    vt.setXMin(AtomContainerManipulator.countExplicitHydrogens(mol, mol.getAtom(i)));
                                    vt.setPeakShape(newMultiplicity);
                                    vt.setPeakMultiplicity(GeneralUtils.getMultiplicity(AtomContainerManipulator.countExplicitHydrogens(molh, molh.getAtom(i)), false));
                                    predictions.add(vt);
                                }
                                if (statistics[4] > -1) {
                                    if (predictAtomType.equals("H")) {
                                        statistics2 = AtomUtils.predictRange(molh, hatom, calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, (int) statistics[4] - 1, true, hoseCode2, ((Boolean) session.getAttribute("trueonly")).booleanValue(), solvent);
                                    } else {
                                        statistics2 = AtomUtils.predictRange(molh, molh.getAtom(i), calculated, measured, hose3d, force3d, 0, comment, true, false, null, predictionValuesForAppletMap, (int) statistics[4] - 1, true, hoseCode2, ((Boolean) session.getAttribute("trueonly")).booleanValue(), solvent);
                                    }
                                }
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).statistics2 = statistics2;
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setIdentifier(hoseCode2.toString());
                            }
                        } catch (CDKException ex) {
                            if (!added) {
                                elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(Float.MIN_VALUE), i + "", 0, 1000, new String("No data available"), new String("" + (i + 1))));
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity("No data available");
                            }
                        } catch (Exception ex) {
                            if (!added) {
                                elementsOfChoosenType.add(new ValuesForVelocityBean(new Float(Float.MIN_VALUE), i + "", 0, 1000, new String("No data available"), new String("" + (i + 1))));
                                ((ValuesForVelocityBean) elementsOfChoosenType.get(elementsOfChoosenType.size() - 1)).setMultiplicity("No data available");
                            }
                            GeneralUtils.logError(ex, "prediction", null, true);
                        }
                    }
                }
                context.put("coords", "2d");
            }
            context.put("spectrumtype", predictAtomType.equals("Hsvm") ? "1H" : (predictAtomType.equals("All") ? "All" : DBSpectrumTypePeer.retrieveByPK(new NumberKey((String) session.getAttribute("spectype"))).getName()));
            try {
                SmilesGenerator sg = SmilesGenerator.unique().aromatic();
                String smilesNonChiral = sg.create(mol);
                Criteria crit = new Criteria();
                crit.add(DBMoleculePeer.SMILES_STRING, smilesNonChiral);
                List<DBMolecule> v = DBMoleculePeer.doSelect(crit);
                if (v.size() > 0) {
                    context.put("linkmessage", "portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/"+((DBMolecule) v.get(0)).getMoleculeId() + ";jsessionid=" + session.getId());
                } else {
                    context.put("linkmessage", "none");
                }
            } catch (Exception ex) {
                context.put("linkmessage", "none");
            }
            context.put("spectype", ((String) session.getAttribute("spectype")).equals("103") ? "1" : session.getAttribute("spectype"));
            if (predictAtomType.equals("Hsvm") || predictAtomType.equals("Hnn") || predictAtomType.equals("Cnn")) {
                context.put("method", "nn");
            } else {
                context.put("method", "hose");
            }
            // We fetch the link page from csearch
            try{
	            String inchikey = GeneralUtils.generateInchiKey(mol);
	            if (!inchikey.isEmpty()) {
	                StringTokenizer st = new StringTokenizer(inchikey, "-");
	                String csearchurl = "http://nmrpredict.orc.univie.ac.at/inchikeysync/" + st.nextToken() + ".html";
	                try {
	                    HttpParams httpParams = new BasicHttpParams();
	                    HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
	                    HttpConnectionParams.setSoTimeout(httpParams, 1000);
	                    HttpClient httpclient = new DefaultHttpClient(httpParams);
	                    HttpGet httpget = new HttpGet(csearchurl);
	                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                    String responseBody = httpclient.execute(httpget, responseHandler);
	                    st = new StringTokenizer(responseBody, "\n");
	                    StringBuffer result = new StringBuffer();
	                    boolean before = true;
	                    while (st.hasMoreTokens()) {
	                        String token = st.nextToken();
	                        if (token.equals("<center><big><big><b>NMR-Spectra available online</center><br><br>"))
	                            token = "<center><big><big><b>NMR-Spectra available online for this compound</center><br><br>";
	                        if (token.startsWith("<center><b>Page created by Wolfgang Robien"))
	                            token = "<center><b>Linklist created by Wolfgang Robien" + token.substring(42);
	                        if (!before)
	                            result.append(token + "\n");
	                        else if (token.startsWith("<br><br>"))
	                            before = false;
	                        else if (token.equals("</body>"))
	                            before = true;
	                    }
	                    context.put("csearch", result.toString());
	                } catch (IOException ex) {
	                    // this is if the page doesn't exist, we do nothing then
	                }
	            }
            }catch(Exception ex){
            	//we do nothing if this fails
            }
        }
        Collections.sort(predictions, new SignalComparator(false));
        for (int i = 0; i < predictions.size(); i++) {
            CMLPeak it = predictions.get(i);
            for (int k = 0; k < it.getAtomRefs().length; k++) {
                if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) {
                    spectrumForApplet.append("S(0," + it.getXMin() + "," + (mol.getAtomNumber(AtomUtils.getAtomById(mol, it.getAtomRefs()[k])) + 1) + (predictAtomType.equals("C") ? "," + it.getPeakShape() : "") + ")=" + myFormatter.format(it.getXValue()) + "|");
                } else {
                    spectrumForApplet.append(myFormatter.format(it.getXValue()) + (predictAtomType.equals("C") ? it.getPeakShape() : "") + ";0;" + mol.getAtomNumber(AtomUtils.getAtomById(mol, it.getAtomRefs()[k])) + "|");
                }
                tooltips.append((mol.getAtomNumber(AtomUtils.getAtomById(mol, it.getAtomRefs()[k])) + 1) + "|" + myFormatter.format(it.getXValue()) + "|");
            }
        }
        if (predictAtomType.equals("Hsvm")) {
            Collections.sort(elementsOfChoosenType, new ValuesForVelocityComparator(2,false,1));
        }
        if (doAssignment && peaksToAssign != null) {
            spectrumForApplet = new StringBuffer();
            tooltips = new StringBuffer();
            Map<Double, Double> map;
            double sumOfDifferences = 0;
            int numOfValues = 0;
            if (predictions.size() >= peaksToAssign.size()) {
                map = GeneralUtils.getMatchingSearchShift(predictions, peaksToAssign, 100, new Vector<Double>(), true);
            } else {
                Map<Double, Double> mapWrongWayRound = GeneralUtils.getMatchingSearchShift(peaksToAssign, predictions, 100, new Vector<Double>(), false);
                map = new HashMap<Double, Double>();
                for (Double value : mapWrongWayRound.keySet()) {
                    map.put(mapWrongWayRound.get(value), value);
                }
            }
            StringBuffer unusedString = new StringBuffer();
            for (int i = 0; i < peaksToAssign.size(); i++) {
                if (!map.containsValue(new Double(peaksToAssign.get(i).getXValue())))
                    unusedString.append(peaksToAssign.get(i).getXValue() + ", ");
            }
            if (!unusedString.toString().equals(""))
                context.put("unused", "The following shifts you entered have not been used in the assignment: " + GeneralUtils.removeLastComma(unusedString));
            for (int i = 0; i < elementsOfChoosenType.size(); i++) {
                if (((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics==null || map.get(((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics[1]) == null) {
                    ((ValuesForVelocityBean) elementsOfChoosenType.get(i)).setValue(Float.MIN_VALUE);
                    if(((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics!=null)
                        ((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics[3] = Float.MIN_VALUE;
                } else {
                    float shift = map.get(((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics[1]).floatValue();
                    ((ValuesForVelocityBean) elementsOfChoosenType.get(i)).setValue(shift);
                    double difference = (double) shift - ((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics[1];
                    ((ValuesForVelocityBean) elementsOfChoosenType.get(i)).statistics[3] = Math.abs(difference);
                    sumOfDifferences += Math.abs(difference);
                    String valuestr = myFormatter.format(shift);
                    int protoncount = 0;
                    if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) {
                        spectrumForApplet.append("S(0," + protoncount + "," + (mol.getAtomNumber((IAtom) mol.getConnectedAtomsList(mol.getAtom(i)).get(0)) + 1) + ")=" + valuestr + "|");
                    } else {
                        spectrumForApplet.append((valuestr) + ";" + 0 + ";" + i + "|");
                    }
                    tooltips.append(((ValuesForVelocityBean) elementsOfChoosenType.get(i)).getDisplayText() + "|" + myFormatter.format(((ValuesForVelocityBean) elementsOfChoosenType.get(i)).getValue()) + "|");
                    numOfValues++;
                }
            }
            float similarity = SpectrumUtils.calculateSimilarity(map, 100f / predictions.size(), predictions, new ArrayList<Double>());
            context.put("similarity", (java.lang.Math.floor(similarity * 100)) / 100);
            context.put("averagedifference", myFormatter.format(sumOfDifferences / numOfValues));
        }
        context.put("spectrumForApplet", spectrumForApplet.toString());
        context.put("tooltips", tooltips.toString());
        StringBuffer predictionValuesForApplet = new StringBuffer();
        Iterator<Double> iterator = predictionValuesForAppletMap.keySet().iterator();
        while (iterator.hasNext()) {
            Double thisPrediction = (Double) iterator.next();
            double[] values = (double[]) predictionValuesForAppletMap.get(thisPrediction);
            if (values != null) {
                predictionValuesForApplet.append(myFormatter.format(thisPrediction.doubleValue()) + "|");
                for (int k = 0; k < values.length; k++) {
                    predictionValuesForApplet.append(myFormatter.format(values[k]) + ";");
                }
                predictionValuesForApplet.append("|");
            }
        }
        context.put("predictionValuesForApplet", predictionValuesForApplet);
        context.put("messages", messages);
        context.put("elements", elementsOfChoosenType);
        context.put("horiorverti", session.getAttribute("horiorverti"));
        context.put("typeid", session.getAttribute("spectype"));
        context.put("selectedType", Integer.parseInt((String) session.getAttribute("spectype")));
        if(Integer.parseInt((String) session.getAttribute("solvent"))!=-1)
        	context.put("selectedSolvent", DBConditionPeer.retrieveByPK(Integer.parseInt((String) session.getAttribute("solvent"))));
        Criteria crit = new Criteria();
        crit.add(DBSpectrumTypePeer.DIMENSIONALITY,1);
        List<DBSpectrumType> v = DBSpectrumTypePeer.doSelect(crit);
        List<DBSpectrumType> v2 = new Vector<DBSpectrumType>();
        for (int i = 0; i < v.size(); i++) {
            if (((DBSpectrumType) v.get(i)).getName().equals("1H")) {
                ((DBSpectrumType) v.get(i)).setName("1H (using HOSE code)");
            }
            if (((DBSpectrumType) v.get(i)).getName().equals("13C")) {
                ((DBSpectrumType) v.get(i)).setName("13C (using HOSE code)");
            }
            v2.add((DBSpectrumType) v.get(i));
        }
        context.put("types", v2);
        List<Record> solventssql=DBConditionPeer.executeQuery("select count(*) as c, NMR_CONDITION.CONDITION_ID, NMR_CONDITION.VALUE from NMR_CONDITION join SPECTRUM_CONDITION using (CONDITION_ID) where CONDITION_TYPE_ID =2 group by CONDITION_ID order by c desc limit 12");
        List<DBCondition> solvents = new ArrayList<DBCondition>();
        DBCondition all = new DBCondition();
        all.setConditionId(-1);
        all.setValue("Any");
        solvents.add(all);
        for(Record record : solventssql){
        	if(!record.getValue(3).asString().equals("Unreported") && !record.getValue(3).asString().equals("Unknown")){
        		solvents.add(DBConditionPeer.retrieveByPK(record.getValue(2).asInt()));
        	}
        }
        context.put("solvents", solvents);
        // The mol is put into the MView applet via a file
        if (session.getAttribute("mol") != null) {
            String molfile = doImageOrMolfile(session, predictAtomType.equals("Hsvm") && molh!=null ?  (IAtomContainer) molh.clone() : (IAtomContainer) mol.clone(), print, servcon, context, input, spectrumForApplet.toString(), shifts, DBSpectrumTypePeer.retrieveByPK(new NumberKey((String) session.getAttribute("spectype"))));
            if (input.equals("0")) {
                if (servconorrundata instanceof RunData) {
                	GeneralUtils.addToStructureHistory(molfile, (RunData)servconorrundata);
                }
                if (doAssignment)
                    GeneralUtils.logToSql("Assignment - " + (String) session.getAttribute("spectype") + " " + molfile, servconorrundata instanceof RunData ? (RunData) servconorrundata : null);
                else
                    GeneralUtils.logToSql("Prediction - " + (String) session.getAttribute("spectype") + " " + molfile, servconorrundata instanceof RunData ? (RunData) servconorrundata : null);
            }
        }
        context.put("constants", new NmrshiftdbConstants());
        context.put("session", session);
        context.put("servcon", servcon);
        context.put("request", request);
        if (servconorrundata instanceof RunData) {
            UrlTool urltool = new UrlTool();
            urltool.init((RunData) servconorrundata);
            context.put("baseurl", urltool.getBaseUrl());
        }
    }

    /**
     * Writes out a molfile or image file and puts their names into the context for display.
     * 
     * @param session The current session.
     * @param mol The molecule to write out.
     * @param print Is a print page requested (print page never contains an applet)?
     * @param servcon The current servlet context.
     * @param context The velocity context to fill.
     * @param input The input flag from the getContext method (indicates if the input page or the result page should be displayed).
     * @param spectrumForApplet The spectrum in the format required for the applet.
     * @param shifts A map of atom numbers and shifts for the image download.
     * @return The V2000 mol file for mol.
     * @throws NoRowsException
     * @throws TooManyRowsException
     * @throws TorqueException
     * @throws IOException 
     * @throws TranscoderException 
     * @throws CDKException 
     * @throws Exception
     */
    public static String doImageOrMolfile(HttpSession session, IAtomContainer mol, boolean print, ServletConfig servcon, Context context, String input, String spectrumForApplet, Map<Integer, String> shifts, DBSpectrumType spectrumType) throws NoRowsException, TooManyRowsException, TorqueException, IOException, CDKException, TranscoderException {
        StringWriter sw = new StringWriter();
        for (int f = 0; f < mol.getAtomCount(); f++) {
            mol.getAtom(f).setFlag(CDKConstants.ISAROMATIC, false);
        }
        for (int f = 0; f < mol.getElectronContainerCount(); f++) {
            IElectronContainer electronContainer = mol.getElectronContainer(f);
            if (electronContainer instanceof IBond) {
                electronContainer.setFlag(CDKConstants.ISAROMATIC, false);
            }
        }
        try{
            MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
            mdlwriter.writeMolecule(mol);
            mdlwriter.close();
        }catch(Exception ex){
            //MDLV2000Writer still throws Exception
        	throw new CDKException(ex.getMessage());
        }
        if (session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet") && !print) {
            File outputFile = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/" + System.currentTimeMillis() + "predict.mol"));
            FileWriter out = new FileWriter(outputFile);
            out.write(sw.toString());
            out.close();
            context.put(NmrshiftdbConstants.MOLFILE, "nmrshiftdbhtml/" + outputFile.getName());
        } else {
            if(GeneralUtils.getNmrshiftdbProperty("structureeditor", servcon).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", servcon).equals("GGAKETCHER")){
                context.put(NmrshiftdbConstants.MOLFILE, GeneralUtils.replace(sw.toString(),"\n","\\n"));
            }
            String filename = "nmrshiftdbhtml/" + System.currentTimeMillis() + "predict.jpg";
            GeneralUtils.makeJpg(sw.toString(), "/" + filename, servcon, 300, 250, true, null, null, Color.LIGHT_GRAY);
            context.put("imagefile", filename);
            if (input.equals("0")) {
                filename = "nmrshiftdbhtml/" + System.currentTimeMillis() + "submitspec.jpg";
                String spectrum = Export.getSpecSvg(spectrumForApplet.toString(), DBSpectrumTypePeer.retrieveByPK(new NumberKey((String) session.getAttribute("spectype"))).getName(), "", 300, 250, false);
                TranscoderInput transinput = new TranscoderInput(new StringReader(spectrum));
                File imagefile = new File(ServletUtils.expandRelative(servcon, "/" + filename));
                OutputStream ostream = new FileOutputStream(imagefile);
                TranscoderOutput output = new TranscoderOutput(ostream);
                ImageTranscoder it = new JPEGTranscoder();
                it.transcode(transinput, output);
                ostream.flush();
                ostream.close();
                context.put("specfile", filename);
            }
        }
        //make an image with shifts
        String filename = "nmrshiftdbhtml/" + System.currentTimeMillis() + "predictshifts.jpg";
        int number=((int)Math.sqrt(mol.getAtomCount()))+1;
        GeneralUtils.makeJpg(sw.toString(), "/" + filename, servcon, number*100, number*100, true, spectrumType, shifts, Color.WHITE);
        context.put("shiftsfile", filename);
        context.put("showoldinput", "true");
        return sw.toString();
    }

    /**
     * Sets molh as an attribute into the session, key mol, and saturates,
     * normalizes, adds hs, places these.
     * 
     * @param molh The molecule to treat.
     * @param runData The current runData
     * @throws CDKException 
     */
    private static void doWithh(RunData runData) throws CDKException, InvalidSmilesException {
        IAtomContainer mol = (IAtomContainer) runData.getSession().getAttribute("mol");
        for (int i = 0; i < mol.getAtomCount(); i++) {
            mol.getAtom(i).setID("a" + (i + 1));
        }
        runData.getSession().setAttribute("mol", mol);
    }

    /* (non-Javadoc)
     * @see org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction#buildNormalContext(org.apache.jetspeed.portal.portlets.VelocityPortlet, org.apache.velocity.context.Context, org.apache.turbine.util.RunData)
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
        HttpSession session = data.getSession();
        session.setAttribute(MODE, MODE_PREDICTION);
        session.removeAttribute("spectrumassignment");
        fillContext(context, data);
    }

    /**
     * Gets the content to be displayed in the portlet as HTML
     * 
     * @param runData Created by turbine
     * @return The content to be displayed as the portlet
     * @since
     */
    public void fillContext(Context context, RunData runData) {
        try {
            HttpSession session = runData.getSession();
            HttpServletRequest req = runData.getRequest();
            StringBuffer messages = new StringBuffer();
            // This is only to trigger a session count
            GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session, NmrshiftdbConstants.SEARCHHISTORY);
            if (session.getAttribute(NmrshiftdbConstants.INPUT) == null) {
                session.setAttribute(NmrshiftdbConstants.INPUT, new StringBuffer("0"));
            }
            try {
                if (session.getAttribute("choosennumber") == null) {
                    session.setAttribute("choosennumber", new Integer(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres", runData)));
                }
            } catch (FileNotFoundException ex) {
                GeneralUtils.logError(ex, "choosennumber", runData, true);
            } catch (IOException ex) {
                GeneralUtils.logError(ex, "choosennumber", runData, true);
            }
            // User submitted a molecule for upload
            if (req.getContentType() != null && req.getContentType().indexOf("multipart/form-data") > -1) {
                FileItem[] fi = runData.getParameters().getFileItems("Datei");
                if (fi == null) {
                    messages.append("It seems you did not choose a file!<br>");
                } else {
                    GeneralUtils.processFileItem(fi[0], messages, runData, "mol");
                    if (messages.toString().equals("")) {
                        doWithh(runData);
                    }
                }
                fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
            }
            if (context.get("structureeditor")==null) {
                StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
                input.setCharAt(0, '1');
                session.removeAttribute("mol");
                session.removeAttribute("spectrumassignment");
                fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
            }
        } catch (Throwable ex) {
            context.put("error",GeneralUtils.logError(ex,"predict",runData,true));
        }
    }
    
    /**
     * This action chages applet/no applet mode.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void eventSubmit_doApplet(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '0');
              session.setAttribute("applet", "applet");
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action chages applet/no applet mode.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void eventSubmit_doNopplet(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '0');
              runData.getSession().setAttribute("applet", "noapplet");
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action is called from the "Return to prediction/assignment" links on result page.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doChangenumberofspheres(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              session.setAttribute("choosennumber", new Integer(runData.getParameters().get("numberofspheres")));
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }
    
    /**
     * This action changes the sorting of the spectrum.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doChangelayout(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              List<CMLPeak> numberSpectrum = null;
              if (session.getAttribute(MODE).equals(MODE_ASSIGNMENT) && session.getAttribute("spectrumassignment") != null) {
                  String  spectrum = GeneralUtils.replace((String)session.getAttribute("spectrumassignment"), " ", "");
                  numberSpectrum = ParseUtils.parseSpectrum(spectrum,null).getPeakChildren();
                  Collections.sort(numberSpectrum, new SignalComparator(false));
              }
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), session.getAttribute(MODE).equals(MODE_ASSIGNMENT) ? numberSpectrum : null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action is called if an actual predict is done.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doPredict(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '0');
              IAtomContainer mol = null;
              if ((session.getAttribute("applet") != null && session.getAttribute("applet").equals("applet")) || GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("GGAKETCHER")) {
                  try {
                      String mdl = runData.getParameters().get("MolTxt");
                      if (mdl != null) {
                          mol = AtomUtils.readMolBothVersions(mdl);
                      } else {
                          String smiles = runData.getParameters().get("smiles");
                          mol = AtomUtils.readFromSmilesAndLayout(smiles);
                      }
                      ((AtomContainer)mol).putHsToEnd(); 
                      for(IBond bond : mol.bonds()) {
                    	  if(bond.getOrder()==null)
                    		  bond.setOrder(IBond.Order.ZERO);
                      }
                      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                      CDKHueckelAromaticityDetector.detectAromaticity(mol);
                      runData.getSession().setAttribute("mol", mol);
                  } catch (Exception ex) {
                      GeneralUtils.logError(ex, "problem reading " + runData.getParameters().getString("MolTxt"), runData, true);
                      context.put("error","Problems reading your mol file/smiles. Probably it was corrupt. For questions, ask the <a href=\"mailto:" + GeneralUtils.getAdminEmail(runData) + "\">adminstrator</a>!");
                  }
              } else {
                  mol = (IAtomContainer) session.getAttribute("mol");
              }
              if (mol.getAtomCount() == 0) {
                  messages.append("Your structure containted no atoms. Please enter a valid structure!");
                  input.setCharAt(0, '1');
                  fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
              } else {
                  doWithh(runData);
                  
                  if (session.getAttribute(MODE).equals(MODE_ASSIGNMENT)) {
                      try {
                          String  spectrum = GeneralUtils.replace(runData.getParameters().get("spectrum"), " ", "");
                          CMLPeakList numberSpectrum = ParseUtils.parseSpectrum(spectrum,null);
                          if (numberSpectrum.getPeakElements().size() == 0) {
                              messages.append("<br>You did not enter any signal!");
                          }
                          session.setAttribute("spectrumassignment", runData.getParameters().get("spectrum"));
                          fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), numberSpectrum.getPeakChildren());
                      } catch (NumberFormatException ex) {
                          messages.append("It seems your input was wrongly formatted!");
                          input.setCharAt(0, '1');
                          fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
                      }
                  } else {
                      fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
                  }
              }
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action is called from the "Return to prediction/assignment" links on result page.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doShowfromsession(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '0');
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action is called from the correct molecule button.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doCorrectmolecule(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '1');
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }

    /**
     * This action is called from the new prediction button.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doNewpredict(RunData runData, Context context){
          try{
              HttpSession session = runData.getSession();
              StringBuffer messages = new StringBuffer();
              StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
              input.setCharAt(0, '1');
              session.removeAttribute("mol");
              session.removeAttribute("spectrumassignment");
              fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
          }
    }
    
    
    public void doPredictfromdb(RunData runData, Context context){
        try {
            HttpSession session = runData.getSession();
            StringBuffer messages = new StringBuffer();
            String molid = runData.getParameters().get("molid");
            IAtomContainer mol = DBMoleculePeer.retrieveByPK(new NumberKey(molid)).getAsCDKMoleculeAsEntered(1, true);
            runData.getSession().setAttribute("mol", mol);
            if (session.getAttribute(NmrshiftdbConstants.INPUT) == null) {
                session.setAttribute(NmrshiftdbConstants.INPUT, new StringBuffer("0"));
            }
            if (session.getAttribute("choosennumber") == null) {
                session.setAttribute("choosennumber", new Integer(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres", runData)));
            }
            if (session.getAttribute("trueonly") == null)
                session.setAttribute("trueonly", new Boolean(true));
            StringBuffer input = (StringBuffer) session.getAttribute(NmrshiftdbConstants.INPUT);
            input.setCharAt(0, '0');
            doWithh(runData);
            if(session.getAttribute(MODE)==null)
            	session.setAttribute(MODE,MODE_PREDICTION);
            fillPredictContext(context, messages, runData, session, session.getAttribute(MODE).equals(MODE_ASSIGNMENT), null);
        }catch(Exception ex){
            context.put("error",GeneralUtils.logError(ex,"predict/doaccept",runData,true));
        }
    }

    /**
     * Fills the context with prediction-specific content
     * 
     * @param context The context to fill
     * @param messages Collected messages for the user
     * @param runData The RunData object from turbine
     * @param session The current session
     * @throws TorqueException 
     * @throws TurbineException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws TranscoderException 
     * @throws DataSetException 
     * @throws NmrshiftdbException 
     * @throws SQLException 
     * @throws CDKException 
     * @throws CloneNotSupportedException 
     * @throws ClassNotFoundException 
     * @throws JAXBException 
     * @exception Exception Description of Exception
     */
    private void fillPredictContext(Context context, StringBuffer messages, RunData runData, HttpSession session, boolean doAssignment, List<CMLPeak> peaksToAssign) throws TorqueException, TurbineException, FileNotFoundException, IOException, ClassNotFoundException, CloneNotSupportedException, CDKException, SQLException, NmrshiftdbException, DataSetException, TranscoderException {
        boolean measured = false;
        if (runData.getRequest().getParameter("measuredonly") != null) {
            measured = runData.getRequest().getParameter("measuredonly").equals("true");
        }
        boolean calculated = false;
        if (runData.getRequest().getParameter("calculatedonly") != null) {
            calculated = runData.getRequest().getParameter("calculatedonly").equals("true");
        }
        boolean hose3d = false;
        if(runData.getRequest().getParameter("MolTxt") ==null ){
            //this means we come to page for the first time, we want hose3d to be preset
            hose3d=true;
        }
        if (runData.getRequest().getParameter("hose3d") != null) {
            hose3d = runData.getRequest().getParameter("hose3d").equals("true");
        }
        boolean force3d = false;
        if (runData.getRequest().getParameter("force3d") != null && runData.getRequest().getParameter("force3d").equals("true")) {
        	force3d = runData.getRequest().getParameter("force3d").equals("true");
        }
        //we cannot force 3d and not have 3d at the same time
        if(force3d && !hose3d)
            hose3d=true;
        fillPredictContext(context, session, calculated, measured, hose3d, force3d, messages, runData, runData.getRequest(), false, doAssignment, peaksToAssign);
        context.put("measuredonly", Boolean.toString(measured));
        context.put("calculatedonly", Boolean.toString(calculated));
        context.put("hose3d", Boolean.toString(hose3d));
        context.put("force3d", Boolean.toString(force3d));
        StringBuffer labgroups = new StringBuffer();
        Criteria crit = new Criteria();
        crit.add(DBLabGroupPeer.LABGROUP_ID, 0, Criteria.GREATER_THAN);
        List<DBLabGroup> groups = DBLabGroupPeer.doSelect(crit);
        for (int i = 0; i < groups.size(); i++) {
            labgroups.append(((DBLabGroup) groups.get(i)).getLabgroupName());
            labgroups.append("; ");
        }
        context.put("labgroup", GeneralUtils.removeLastComma(labgroups));
        context.put("doAssignment", doAssignment);
        context.put("tab", doAssignment ? "P-Assign" : "P-Predict");
        context.put("tab_smiles", doAssignment ? "P-Assign" : "P-Predict");
        context.put("link", JetspeedLinkFactory.getInstance(runData));
        context.put("assignSpectrum", session.getAttribute("spectrumassignment"));
        context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",runData));
    }
}
