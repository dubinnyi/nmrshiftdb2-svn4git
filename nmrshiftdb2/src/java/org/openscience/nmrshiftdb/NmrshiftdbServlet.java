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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.util.parser.DefaultJetspeedParameterParser;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunDataFactory;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
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
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.Spectrum1D;
import org.jcamp.spectrum.assignments.AtomReference;
import org.jcamp.spectrum.notes.NoteDescriptor;
import org.jcamp.units.CommonUnit;
import org.jcamp.units.Unit;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.RssWriter;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.modules.actions.portlets.PredictAction;
import org.openscience.nmrshiftdb.modules.actions.rest.NmrshiftdbRestServices;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBCanonicalName;
import org.openscience.nmrshiftdb.om.DBCanonicalNamePeer;
import org.openscience.nmrshiftdb.om.DBChemicalName;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBExperimentReportCategory;
import org.openscience.nmrshiftdb.om.DBExperimentReportCategoryPeer;
import org.openscience.nmrshiftdb.om.DBMachine;
import org.openscience.nmrshiftdb.om.DBMachinePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBQuickCheck;
import org.openscience.nmrshiftdb.om.DBQuickCheckPeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBSessionsPeer;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.QuickCheckRequest;
import org.openscience.nmrshiftdb.util.QuickCheckResponse;
import org.openscience.nmrshiftdb.util.ReportUtil;
import org.openscience.nmrshiftdb.util.SearchUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMoleculeList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowagie.text.pdf.PdfWriter;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import de.unikoeln.chemie.nmr.data.NMR2DSpectrum;
import de.unikoeln.chemie.nmr.data.NmreData;
import de.unikoeln.chemie.nmr.data.NmreData.NmredataVersion;
import de.unikoeln.chemie.nmr.data.NmreDataRecord;
import de.unikoeln.chemie.nmr.data.Peak2D;
import de.unikoeln.chemie.nmr.io.LSDWriter;
import de.unikoeln.chemie.nmr.io.NmredataReader;
import de.unikoeln.chemie.nmr.io.NmredataRecordWriter;
import de.unikoeln.chemie.nmr.io.NmredataWriter;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Serializer;

/**
 * A servlet handling nmrshiftdb tasks outside of the jetspeed screen.
 *
 * @author     shk3
 * @created    12. August 2002
 */
public class NmrshiftdbServlet extends HttpServlet {
	private static final long serialVersionUID = -2110379645892443298L;

	/**
     *  The get method delegates to doPost.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }


    /**
     *  The doPost Method.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //This section handles file upload requests. Files get written to tmp dir and a redirection to the 
        //jetspeed pane where the file was uploaded is sent back. The portlet is responsible for actually handling files
        if(req.getContentType()!=null && req.getContentType().indexOf("multipart/form-data")>-1){
            Part part=null;
            MultipartParser mp = new MultipartParser( req, 64*1024*1024 );
            String tab="P-Submit";
            String action = "doUploadjcamp";
            String type="1";
            StringBuffer formats=new StringBuffer();
            StringBuffer allparams=new StringBuffer();
            while ((part = mp.readNextPart()) != null) {
                    if (part.isParam()) {
                      // it's a parameter part
                      String name = part.getName();
                      ParamPart paramPart = (ParamPart) part;
                      String value = paramPart.getStringValue();
                      if(name.equals("nmrshiftdbaction") && value.equals("uploadjcampsearch"))
                          tab="P-Search";
                      if(name.equals("nmrshiftdbaction") && value.equals("doAttachfiles"))
                    	  action="doAttachfiles";
                      if(name.equals("nmrshiftdbaction") && value.equals("doUploadjchempaint"))
                          action="doUploadjchempaint";
                      if(name.equals("nmrshiftdbaction") && value.equals("quickcheck"))
                          tab="P-Quickcheck";
                      if(name.equals("nmrshiftdbaction") && value.equals("submitmnova"))
                          tab="P-Submitmnova";
                      if(name.equals("nmrshiftdbaction") && value.equals("submittopspin"))
                          tab="P-Submittopspin";
                      if(name.equals("nmrshiftdbaction") && value.equals("submitnmredata"))
                          tab="P-Submittopspin";
                      if(name.equals("nmrshiftdbaction") && value.equals("order"))
                          tab="P-NMR+lab+administration";
                      if(name.equals("spectrumType"))
                          type=value;
                      if(name.equals("tab"))
                          tab=value;
                      if(name.equals(NmrshiftdbConstants.JCAMP_STRICT) && value.equals(NmrshiftdbConstants.JCAMP_STRICT)){
                          req.getSession().setAttribute(NmrshiftdbConstants.JCAMP_STRICT,new Boolean(true));
                      }else if(name.equals(NmrshiftdbConstants.JCAMP_STRICT) && !value.equals(NmrshiftdbConstants.JCAMP_STRICT)){
                    	  req.getSession().setAttribute(NmrshiftdbConstants.JCAMP_STRICT,new Boolean(false));
                      }
                      allparams.append(name).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
                    }
                    else if (part.isFile()) {
                      // it's a file part
                      FilePart filePart = (FilePart) part;
                      String fileName = filePart.getFileName();
                      if (fileName != null) {
                        // the part actually contained a file
                        String name = part.getName();//name will be "attachjcamp","attachpdf","attachraw","attachotherfiles" or "attachimage"
                        String relativepath = ServletUtils.expandRelative(this.getServletConfig(),"/WEB-INF/tmp");
                        filePart.writeTo(new File(relativepath));
                        File file=new File(relativepath+"/"+filePart.getFileName());
                        file.renameTo(new File(relativepath+"/"+name+req.getSession().getId()));
                        formats.append("&format"+name+"="+(fileName.indexOf(".")>-1 ? fileName.substring(fileName.indexOf(".")+1) : fileName));
                      }
                    }
            }
            res.setContentType("text/html");
            PrintWriter out = res.getWriter();
            if(tab.equals("P-Quickcheck") || tab.equals("P-NMR+lab+administration"))
                res.sendRedirect("portal/js_pane/"+tab+";jsessionid="+req.getSession().getId()+"?"+allparams);
            else if(tab.equals("P-Submitmnova") || tab.equals("P-Submittopspin") || tab.equals("P-Submitnmredata"))
                res.sendRedirect("portal/js_pane/P-Submit;jsessionid="+req.getSession().getId()+"?"+allparams);
            else
                res.sendRedirect("portal/js_pane/"+tab+";jsessionid="+req.getSession().getId()+"?type="+type+"&eventSubmit_"+action+"=eventSubmit_"+action+formats);
            out.flush();
            return;
        }
        //Here follow the various other actions the servlet can do. It is basically a list of ifs, 
        //each if reacting to a certain nmrshiftdbaction parameter in the http request.
        PrintWriter out = null;
        ServletOutputStream outstream=null;
        try {
        	DefaultJetspeedParameterParser parameterParser = new DefaultJetspeedParameterParser();
        	parameterParser.setRequest(req);
            String action = parameterParser.getString("nmrshiftdbaction");
            if(action==null) {
                res.setContentType("text/plain");
                out = res.getWriter();
                out.write("No valid nmrshiftdb action given, see documentation at https://sourceforge.net/p/nmrshiftdb2/wiki/AutomationInterfaces/ for options!");
                out.flush();
                return;            	
            }
            String relativepath = ServletUtils.expandRelative(this.getServletConfig(),"/WEB-INF");
            if(!Torque.isInit())
            	Torque.init(relativepath + "/conf/TurbineResources.properties");
            DBSpectrum spectrum=null;
            DBMolecule molecule=null;
            Export export =null;
            String format = parameterParser.getString("format");
            String specfileFromSubdata=null;
            if (action.equals("test")){
                try{
                    res.setContentType("text/plain");
                    out = res.getWriter();
                    List<Record> l=DBSpectrumPeer.executeQuery("select SPECTRUM_ID from SPECTRUM limit 1");
                    if(l.size()>0)
                        ((Record)l.get(0)).getValue(1).asInt();
                    out.write("success");
                }
                catch(Exception ex){
                    out.write("failure");
                }
            }else if(action.equals("rss")){
                out = res.getWriter();
                int numbertoexport=10;
                if(parameterParser.getString("numbertoexport")!=null){
                    try{
                        numbertoexport=Integer.parseInt(parameterParser.getString("numbertoexport"));
                        if(numbertoexport<1 || numbertoexport>20)
                            throw new NumberFormatException("Number to small/large");
                    }
                    catch(NumberFormatException ex){
                        out.println("The parameter <code>numbertoexport</code>must be an integer from 1 to 20");
                    }
                }
                String query="select distinct MOLECULE.MOLECULE_ID from MOLECULE, SPECTRUM where SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and SPECTRUM.REVIEW_FLAG =\"true\" order by MOLECULE.DATE desc;";
                writeRSS(res, query, numbertoexport, req);
            }else if(action.equals("rssmine")){
                out = res.getWriter();
                int numbertoexport=10;
                if(parameterParser.getString("numbertoexport")!=null){
                    try{
                        numbertoexport=Integer.parseInt(parameterParser.getString("numbertoexport"));
                        if(numbertoexport<1 || numbertoexport>20)
                            throw new NumberFormatException("Number to small/large");
                    }
                    catch(NumberFormatException ex){
                        out.println("The parameter <code>numbertoexport</code>must be an integer from 1 to 20");
                    }
                }
                if(parameterParser.getString("user")!=null){
                    String query="select distinct MOLECULE.MOLECULE_ID from MOLECULE, SPECTRUM, TURBINE_USER where SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and SPECTRUM.REVIEW_FLAG ='true' and TURBINE_USER.LOGIN_NAME='"+parameterParser.getString("user")+"' order by MOLECULE.DATE desc";
                    writeRSS(res, query, numbertoexport, req);
                }else{
                    out.println("The parameter <code>user</code>must be a valid user id");
                }
            }else if(action.equals("spectrometerstatus")) {
                out = res.getWriter();
            	DBMachine machine = DBMachinePeer.retrieveByName(req.getParameter("name"));
            	if(machine==null) {
            		out.println("No machine named "+req.getParameter("name")+" exists!");
            	}else {
            		out.println(machine.getDefaultModeActual());
            	}
            }else if(action.equals("getattachment")){
                res.setContentType("application/zip");
                outstream=res.getOutputStream();
                DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(parameterParser.getString("sampleid")));
                outstream.write(sample.getAttachment());
            }else if(action.equals("createreport")){
                res.setContentType("application/pdf");
                outstream=res.getOutputStream();//out = res.getWriter();
                //First, load JasperDesign from XML and compile it into JasperReport
                boolean yearly=parameterParser.getString("style").equals("yearly");
                int yearstart=Integer.parseInt(parameterParser.getString("yearstart"));
                int yearend=Integer.parseInt(parameterParser.getString("yearend"));
                int monthstart=0;
                int monthend=0;
                if(!yearly){
                    monthstart=Integer.parseInt(parameterParser.getString("monthstart"));
                    monthend=Integer.parseInt(parameterParser.getString("monthend"));
                }
                int type=Integer.parseInt(parameterParser.getString("type"));
                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(relativepath+"/reports/"+(yearly ? "yearly" : "monthly")+"_report_"+type+".jasper");
                //Second, create a map of parameters to pass to the report.
                Map<String,Object> parameters = new HashMap<String,Object>();
                if(yearly)
                    parameters.put("HEADER", "Report for years "+yearstart+" - "+yearend);
                else
                    parameters.put("HEADER", "Report for "+monthstart+"/"+yearstart+" - "+monthend+"/"+yearend);
                //Third, get a database connection
                Connection conn = null;
                try{
                    conn = Torque.getConnection();
                    Statement stmt=conn.createStatement();
                    ResultSet rs=null;
                    StringBuffer sql=new StringBuffer();
                    Criteria crit = new Criteria();
                    crit.addAscendingOrderByColumn(DBExperimentReportCategoryPeer.ORDER_POSITION);
                    List<DBExperimentReportCategory> reportCategories = DBExperimentReportCategoryPeer.doSelect(crit);
                    if(type==1){
                        sql.append("select YEAR(DATE) as YEAR, "+(yearly? "": " MONTH(DATE) as MONTH, ")+"AFFILIATION_1, AFFILIATION_2, MACHINE.NAME as NAME, count(*) as C, ");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append("sum(IFNULL("+((DBExperimentReportCategory)reportCategories.get(i)).getName()+",0)) as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+", ");
                        }
                        sql.append(" sum(PROCESS='self') as SELF, sum(PROCESS='robot') as ROBOT, sum(PROCESS='worker') as OPERATOR from ");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append("(");
                        }
                        sql.append("((SAMPLE join TURBINE_USER using (USER_ID) join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null)) join MACHINE on MACHINE.MACHINE_ID=SAMPLE.MACHINE)  join (select SAMPLE_ID, count(*) as exp_count from SAMPLE join EXPERIMENT_SAMPLE using (SAMPLE_ID) group by SAMPLE_ID) as exp_counts using (SAMPLE_ID)");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append(") left join (select SAMPLE_ID, count(*) as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+" from SAMPLE join EXPERIMENT_SAMPLE using (SAMPLE_ID) join EXPERIMENT using (EXPERIMENT_ID) where EXPERIMENT_REPORT_CATEGORY_ID="+((DBExperimentReportCategory)reportCategories.get(i)).getExperimentReportCategoryId()+" group by SAMPLE_ID )as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+"_table using (SAMPLE_ID)");
                        }                    
                        sql.append(" where ");
                        if(yearly) {
                        	sql.append("YEAR(DATE)>="+yearstart+" and YEAR(DATE)<="+yearend);
                        }else {
                        	sql.append("((YEAR(DATE)>="+(yearstart+1)+" and YEAR(DATE)<="+(yearend-1)+") or (YEAR(DATE)>="+yearstart+" and MONTH(DATE)='"+monthstart+"') or (YEAR(DATE)="+yearend+" and MONTH(DATE)<='"+monthend+"'))");
                        }
                        sql.append(" and LOGIN_NAME<>'testuser' group by YEAR, "+(yearly ? "" : "MONTH, ")+"AFFILIATION_1, AFFILIATION_2, MACHINE.NAME");
                    }else if(type==2){
                        sql.append("select YEAR(DATE) as YEAR, "+(yearly? "": " MONTH(DATE) as MONTH, ")+"MACHINE.NAME as NAME, count(*) as C, ");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append("sum(IFNULL("+((DBExperimentReportCategory)reportCategories.get(i)).getName()+",0)) as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+", ");
                        }
                        sql.append("sum(PROCESS='self') as SELF, sum(PROCESS='robot') as ROBOT, sum(PROCESS='worker') as OPERATOR from ");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append("(");
                        }
                        sql.append("((SAMPLE join TURBINE_USER using (USER_ID) join USER_HISTORY on (TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null)) join MACHINE on MACHINE.MACHINE_ID=SAMPLE.MACHINE)  join (select SAMPLE_ID, count(*) as exp_count from SAMPLE join EXPERIMENT_SAMPLE using (SAMPLE_ID) group by SAMPLE_ID) as exp_counts using (SAMPLE_ID)");
                        for(int i=0;i<reportCategories.size();i++){
                        	sql.append(") left join (select SAMPLE_ID, count(*) as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+" from SAMPLE join EXPERIMENT_SAMPLE using (SAMPLE_ID) join EXPERIMENT using (EXPERIMENT_ID) where EXPERIMENT_REPORT_CATEGORY_ID="+((DBExperimentReportCategory)reportCategories.get(i)).getExperimentReportCategoryId()+" group by SAMPLE_ID )as "+((DBExperimentReportCategory)reportCategories.get(i)).getName()+"_table using (SAMPLE_ID)");
                        }                    
                        sql.append("where ");
                        if(yearly) {
                        	sql.append("YEAR(DATE)>="+yearstart+" and YEAR(DATE)<="+yearend);
                        }else {
                        	sql.append("((YEAR(DATE)>="+(yearstart+1)+" and YEAR(DATE)<="+(yearend-1)+") or (YEAR(DATE)>="+yearstart+" and MONTH(DATE)='"+monthstart+"') or (YEAR(DATE)="+yearend+" and MONTH(DATE)<='"+monthend+"'))");
                        }
                        sql.append(" and LOGIN_NAME<>'testuser' group by YEAR, "+(yearly ? "" : "MONTH, ")+"MACHINE.NAME");
                    }
                    rs=stmt.executeQuery(sql.toString());
                    //Fourth, create JasperPrint using fillReport() method
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parameters,new JRResultSetDataSource(rs));
                    //You can use JasperPrint to create PDF
                    JasperExportManager.exportReportToPdfStream(jasperPrint, outstream);
                }
                catch(Exception ex){
                    throw ex;
                }
                finally{
                    Torque.closeConnection(conn);
                }
            }else if(action.equals("exportcmlbyinchi")){
            	if(parameterParser.getString("format","cml").equals("jcamp"))
            		res.setContentType("chemical/x-jcamp-dx");
            	else
            		res.setContentType("text/xml");
                out = res.getWriter();
                String inchi=parameterParser.getString("inchi");
                inchi=GeneralUtils.replace(inchi, "%3D", "=");
                inchi=GeneralUtils.replace(inchi, "%2F", "/");
                if(inchi.indexOf("InChI=")==-1)
                    inchi="InChI="+inchi;
                String spectrumtype=parameterParser.getString("spectrumtype");
                Criteria crit=new Criteria();
                crit.add(DBCanonicalNamePeer.NAME,inchi);
                crit.addJoin(DBCanonicalNamePeer.MOLECULE_ID,DBSpectrumPeer.MOLECULE_ID);
                crit.addJoin(DBSpectrumPeer.SPECTRUM_TYPE_ID,DBSpectrumTypePeer.SPECTRUM_TYPE_ID);
                crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
                crit.add(DBSpectrumTypePeer.NAME, spectrumtype);
                try{
                    GeneralUtils.logToSql("Search - " + crit.toString().substring(crit.toString().indexOf("(may not be complete or applicable):")+37),null);
                }catch(Exception ex){
                    //ok
                }
                List<DBSpectrum> spectra=DBSpectrumPeer.doSelect(crit);
                //We do a fallback if no result
                if(spectra.size()==0){
          		  StringBuffer inchinew=new StringBuffer("");
        		  //
        		  int slashcount=0;
        		  for(int l=0;l<inchi.length();l++){
        			  if(inchi.charAt(l)=='/')
        				  slashcount++;
        			  if(slashcount==4)
        				  break;
        			  inchinew.append(inchi.charAt(l));
        		  }
        		  inchinew.append("%");
                  crit=new Criteria();
                  crit.add(DBCanonicalNamePeer.NAME,(Object)inchinew.toString(),Criteria.LIKE);
                  crit.addJoin(DBCanonicalNamePeer.MOLECULE_ID,DBSpectrumPeer.MOLECULE_ID);
                  crit.addJoin(DBSpectrumPeer.SPECTRUM_TYPE_ID,DBSpectrumTypePeer.SPECTRUM_TYPE_ID);
                  crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
                  crit.add(DBSpectrumTypePeer.NAME, spectrumtype);
                  try{
                      GeneralUtils.logToSql("Search - " + crit.toString().substring(crit.toString().indexOf("(may not be complete or applicable):")+37),null);
                  }catch(Exception ex){
                      //ok
                  }
                  spectra=DBSpectrumPeer.doSelect(crit);
                }
                if(spectra.size()==0){
                    out.write("No such molecule or spectrum");
                }else{
                	if(parameterParser.getString("format","cml").equals("jcamp")){
                		double[][] xy = new double[2][((DBSpectrum)spectra.get(0)).getDBSignals().size()];
                		//this array needs to be filled with x/y values in 0/1 position respectivly
                		IOrderedDataArray1D x = new OrderedArrayData(xy[0],CommonUnit.mz);
                		IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);
                		double frequency=0;
                		try{
                			frequency=Double.parseDouble(((DBSpectrum)spectra.get(0)).getFrequency());
                		}
                		catch(NumberFormatException ex){
                		}
                		NMRSpectrum jdxspectrum = new NMRSpectrum(x, y, ((DBSpectrum)spectra.get(0)).getDBSpectrumType().getName(), frequency, 0D, false, JCAMPReader.STRICT);
                		Peak1D[] jcampPeaks = new Peak1D[((DBSpectrum)spectra.get(0)).getDBSignals().size()];
                		for(int i=0;i<((DBSpectrum)spectra.get(0)).getDBSignals().size();i++){
                			Peak1D jcampPeak = new Peak1D(((DBSignal)((DBSpectrum)spectra.get(0)).getDBSignals().get(i)).getFirstShift().getValue(),((DBSignal)((DBSpectrum)spectra.get(0)).getDBSignals().get(i)).getIntensity());
                			jcampPeaks[i] = jcampPeak;
                		}
                		jdxspectrum.setPeakTable(jcampPeaks);
                		jdxspectrum.setSolvent(((DBSpectrum)spectra.get(0)).getMeasurementConditionValueWithName(NmrshiftdbConstants.solvent));
                		JCAMPWriter jcamp = JCAMPWriter.getInstance();
                		String jcampString = jcamp.toJCAMP(jdxspectrum);
                		out.write(jcampString);
                	}else{
	                    Element cmlElement = new Element("cml");
	                    cmlElement.addAttribute(new Attribute("convention","nmrshiftdb-convention"));
	                    cmlElement.setNamespaceURI("http://www.xml-cml.org/schema");
	                    Element parent=((DBSpectrum)spectra.get(0)).getDBMolecule().getCML(1);
	                    nu.xom.Node cmldoc = parent.getChild(0);
	                    ((Element)cmldoc).setNamespaceURI("http://www.xml-cml.org/schema");
	                    parent.removeChildren();
	                    cmlElement.appendChild(cmldoc);
	                    for(int k=0;k<spectra.size();k++){
	                        Element parentspec=((DBSpectrum)spectra.get(k)).getCmlSpect(false, new UrlTool().getBaseUrl(req)+"/../");
	                        Node spectrumel=parentspec.getChild(0);
	                        parentspec.removeChildren();
	                        cmlElement.appendChild(spectrumel);
	                        ((Element)spectrumel).setNamespaceURI("http://www.xml-cml.org/schema");
	                    }
	                    ByteArrayOutputStream localout = new ByteArrayOutputStream();
	                    Serializer ser = new Serializer(localout, "ISO-8859-1");
	                    ser.setIndent(1);
	                    ser.write(new Document(cmlElement));
	                    out.write(localout.toString());
                	}
                }
            }else if(action.equals("namelist")){
                res.setContentType("application/zip");
                outstream=res.getOutputStream();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                ZipOutputStream zipout=new ZipOutputStream(baos);
				Criteria crit=new Criteria();
				crit.addJoin(DBMoleculePeer.MOLECULE_ID,DBSpectrumPeer.MOLECULE_ID);
				crit.add(DBSpectrumPeer.REVIEW_FLAG,"true");
				List<DBMolecule> v=DBMoleculePeer.doSelect(crit);
				for(int i=0;i<v.size();i++){
                    if(i % 500 ==0){
                        if(i!=0){
                            zipout.write(new String("<p>The list is continued <a href=\"nmrshiftdb.names."+i+".html\">here</a></p></body></html>").getBytes());
                            zipout.closeEntry();
                        }
                        zipout.putNextEntry(new ZipEntry("nmrshiftdb.names."+i+".html"));
                        zipout.write(new String("<html><body><h1>This is a list of strcutures in <a href=\"http://www.nmrshiftdb.org\">nmrshiftdb2</a>, starting at "+i+", Its main purpose is to be found by search engines</h1>").getBytes());
                    }
                    DBMolecule mol=(DBMolecule)v.get(i);
                    zipout.write(new String("<p><a href=\""+mol.getEasylink(req)+"/tab/1\">").getBytes());
                    List<DBCanonicalName> cannames=mol.getDBCanonicalNames();
                    for(int k=0;k<cannames.size();k++){
                        zipout.write(new String(((DBCanonicalName)cannames.get(k)).getName()+" ").getBytes());
                    }
                    List<DBChemicalName> chemnames=mol.getDBChemicalNames();
                    for(int k=0;k<chemnames.size();k++){
                        zipout.write(new String(((DBChemicalName)chemnames.get(k)).getName()+" ").getBytes());
                    }
                    zipout.write(new String("</a>. Information we have got: NMR spectra ").getBytes());
                    List<DBSpectrum> spectra=mol.selectSpectra();
                    for(int k=0;k<spectra.size();k++){
                        zipout.write(new String(((DBSpectrum)spectra.get(k)).getDBSpectrumType().getName()+", ").getBytes());
                    }
                    if(mol.hasAny3d())
                        zipout.write(new String("3D coordinates, ").getBytes());
                    zipout.write(new String("File formats: CML, mol, png, jpeg").getBytes());
                    zipout.write(new String("</p>").getBytes());
                    i++;
                }
                zipout.write(new String("</body></html>").getBytes());
                zipout.closeEntry();
                zipout.close();
                InputStream is =  new ByteArrayInputStream(baos.toByteArray());
                byte[] buf = new byte[32 * 1024]; // 32k buffer
                int nRead = 0;
                while( (nRead=is.read(buf)) != -1 ) {
                    outstream.write(buf, 0, nRead);
                }
            }else if(action.equals("predictor")){
                res.setContentType("text/plain");
                out = res.getWriter();
                out.write("Please see http://sourceforge.net/p/nmrshiftdb2/wiki/PredictorJars/ for information about the predictor tools");
            }else if(action.equals("exportspec") || action.equals("exportmol") || action.equals("exportdataset")){
                BaseJetspeedUser user=(BaseJetspeedUser)req.getSession().getAttribute("turbine.user");
                String viewkey=parameterParser.getString("viewkey","-1");
                if(parameterParser.getString("spectrumid")!=null){
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		SubmittingData subData=((SubmittingData) req.getSession().getAttribute("subData"));
                		spectrum = subData.toDBSpectrum();
                		export = new Export(subData.getMolOriginal(),subData.getMolWithH(),spectrum);
                		specfileFromSubdata=subData.getSpectrumForApplet();
                	}else{                   			
                		spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(parameterParser.getString("spectrumid")));
                        if(!spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) || (spectrum.getReviewKey().equals(viewkey)) || (user!=null && spectrum.getUserId()==Integer.parseInt(user.getUserId()))){
                        	export = new Export(spectrum);
                        }
                	}
                }else{
                	if(parameterParser.getString("moleculeid").equals("subdata")){
                		SubmittingData subData=((SubmittingData) req.getSession().getAttribute("subData"));
                		export = new Export(subData.getMolOriginal(),subData.getMolWithH());
                	}else{
                        String molid=parameterParser.getString("moleculeid");
                        if(molid.indexOf("_")>0){
                            molid=molid.substring(molid.indexOf("_")+1);
                            spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(molid));
                            if(!spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) || (spectrum.getReviewKey().equals(viewkey)) || (user!=null && spectrum.getUserId()==Integer.parseInt(user.getUserId()))){
                                export = new Export(spectrum);
                            }
                        }else{
                            molecule = DBMoleculePeer.retrieveByPK(new NumberKey(molid));
                            if(molecule.isPublicOrOwnPrivate(user==null ? null : TurbineUserPeer.retrieveByPK(Integer.parseInt(user.getUserId())),viewkey)){
                                export = new Export(molecule);
                            }
                        }
                	}
                }
                if(spectrum!=null && !parameterParser.getString("spectrumid","").equals("subdata")){
                    molecule = spectrum.getDBMolecule();
                }
                if(export==null){
                	res.setContentType("text/plain");
                	out = res.getWriter();
                	out.write("Not properly logged in");
                	out.close();
	                return;
                }
            } else if(action.equals("qcdownload")){
                out = res.getWriter();
                SubmittingData subDataC = (SubmittingData) req.getSession().getAttribute("subDataQCc");
                SubmittingData subDataH = (SubmittingData) req.getSession().getAttribute("subDataQCh");
                if(subDataC==null && subDataH==null){
                    res.setContentType("text/html");
                    out.println("It seems there is no QuickCheck done in your session!");
                    out.flush();
                    return;
                }
                NmreData data=new NmreData();
                String solvent=DBConditionPeer.retrieveByPK(Integer.parseInt(req.getParameter("solvent"))).getValue();
    			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.*?)\\((.*?)\\)");
    			Matcher matcher = pattern.matcher(solvent);
    			if(matcher.find())
    				solvent=matcher.group(2);
                data.setSolvent(solvent);
                double freq=0;
                if(!req.getParameter("freqtext").equals("")){
                	try{
                		freq=Double.parseDouble(req.getParameter("freqtext"));
                	}catch(NumberFormatException ex){
                    	res.setContentType("text/plain");
                    	out.write("Frequency must be a decimal number like 250.5!");
                    	out.close();
    	                return;
                	}
                }else{
                	freq=Double.parseDouble(req.getParameter("freq"));
                }
                IAtomContainer molwithh=subDataC.getMolWithH();
                NMRSpectrum nmrspectrumc=null;
                NMRSpectrum nmrspectrumh=null;
                if(subDataC!=null){
	                Peak1D[] peaks1d = new Peak1D[subDataC.getSignalstable().getPeakChildren().size()];
	                for(int i=0;i<subDataC.getSignalstable().getPeakChildren().size();i++){
	                	peaks1d[i]=new Peak1D(subDataC.getSignalstable().getPeakChildren().get(i).getXValue(),0);
	                }
	                Unit xUnit =  CommonUnit.hertz;
	                Unit yUnit = CommonUnit.intensity;
	                double reference = 0;
	                List<Assignment> assignmentslist=new ArrayList<>();
	                for(int i=0;i<subDataC.getSignalstable().getPeakChildren().size();i++){
	                	for(int k=0;k<subDataC.getSignalstable().getPeakChildren().get(i).getAtomRefs().length;k++){
	                		assignmentslist.add(new Assignment(new Pattern(peaks1d[i].getPosition()[0], Multiplicity.UNKNOWN), new IAssignmentTarget[]{new AtomReference(null, molwithh.getAtomNumber(AtomUtils.getAtomById(molwithh, subDataC.getSignalstable().getPeakChildren().get(i).getAtomRefs()[k])))}));
	                	}
	                }
	        		Assignment[] assignmentslocal=(Assignment[])assignmentslist.toArray(new Assignment[assignmentslist.size()]);
	                double[][] xy=new double[0][];
	                IOrderedDataArray1D x = new OrderedArrayData(new double[0], xUnit);
	                IDataArray1D y = new ArrayData(new double[0], yUnit);
	                if(peaks1d.length>0){
	        	        xy = NmredataReader.peakTableToPeakSpectrum(peaks1d);
	        	        x = new OrderedArrayData(xy[0], xUnit);
	        	        y = new ArrayData(xy[1], yUnit);
	                }
	                nmrspectrumc = new NMRSpectrum(x, y, "13C", freq/4, reference, false, JCAMPReader.RELAXED);
	                nmrspectrumc.setPeakTable(peaks1d);
	                nmrspectrumc.setAssignments(assignmentslocal);
	                data.addSpectrum(nmrspectrumc);
                }
                if(subDataH!=null){
	                Peak1D[] peaks1d = new Peak1D[subDataH.getSignalstable().getPeakChildren().size()];
	                for(int i=0;i<subDataH.getSignalstable().getPeakChildren().size();i++){
	                	peaks1d[i]=new Peak1D(subDataH.getSignalstable().getPeakChildren().get(i).getXValue(),0);
	                }
	                Unit xUnit =  CommonUnit.hertz;
	                Unit yUnit = CommonUnit.intensity;
	                double reference = 0;
	                List<Assignment> assignmentslist=new ArrayList<>();
	                for(int i=0;i<subDataH.getSignalstable().getPeakChildren().size();i++){
	                	for(int k=0;k<subDataH.getSignalstable().getPeakChildren().get(i).getAtomRefs().length;k++){
	                		int molnumber=molwithh.getAtomNumber(AtomUtils.getAtomById(molwithh, subDataH.getSignalstable().getPeakChildren().get(i).getAtomRefs()[k]));
	                		if(molnumber>=subDataC.getMolOriginal().getAtomCount()){
	                			molnumber=molwithh.getAtomNumber(molwithh.getConnectedAtomsList(molwithh.getAtom(molnumber)).get(0));
	                		}
	                		assignmentslist.add(new Assignment(new Pattern(peaks1d[i].getPosition()[0], Multiplicity.UNKNOWN), new IAssignmentTarget[]{new AtomReference(null, molnumber)}));
	                	}
	                }
	        		Assignment[] assignmentslocal=(Assignment[])assignmentslist.toArray(new Assignment[assignmentslist.size()]);
	                double[][] xy=new double[0][];
	                IOrderedDataArray1D x = new OrderedArrayData(new double[0], xUnit);
	                IDataArray1D y = new ArrayData(new double[0], yUnit);
	                if(peaks1d.length>0){
	        	        xy = NmredataReader.peakTableToPeakSpectrum(peaks1d);
	        	        x = new OrderedArrayData(xy[0], xUnit);
	        	        y = new ArrayData(xy[1], yUnit);
	                }
	                nmrspectrumh = new NMRSpectrum(x, y, "1H", freq, reference, false, JCAMPReader.RELAXED);
	                nmrspectrumh.setPeakTable(peaks1d);
	                nmrspectrumh.setAssignments(assignmentslocal);
	                data.addSpectrum(nmrspectrumh);
                }
               	write2d("hmbcqc", freq, req.getSession(), data, "HMBC");
               	write2d("hsqcqc", freq, req.getSession(), data, "HSQC");
               	write2d("noesyqc", freq, req.getSession(), data, "NOESY");
               	write2d("tocsyqc", freq, req.getSession(), data, "TOCSY");
               	write2d("cosyqc", freq, req.getSession(), data, "COSY");
                data.setMolecule(subDataC.getMolOriginal());
                if(req.getParameter("format").equals("jcampc") && nmrspectrumc!=null){
                    res.setContentType("chemical/x-jcamp-dx");
        			JCAMPWriter jcamp = JCAMPWriter.getInstance();
        			String jcampString = jcamp.toJCAMP(nmrspectrumc);
        			out.println(jcampString);
                }else if(req.getParameter("format").equals("jcamph") && nmrspectrumh!=null){
                    res.setContentType("chemical/x-jcamp-dx");
        			JCAMPWriter jcamp = JCAMPWriter.getInstance();
        			String jcampString = jcamp.toJCAMP(nmrspectrumh);
        			out.println(jcampString);
                }else if(req.getParameter("format").equals("nmredata")){
                	res.setContentType("chemical/x-mdlsdfile");
                	//causes problems with jcamp writing, so we do it only for nmredata
                    NoteDescriptor noteDescriptor=new NoteDescriptor("Spectrum_Location");
            		String location="none";
	                nmrspectrumc.setNote(noteDescriptor, location);
	                nmrspectrumh.setNote(noteDescriptor, location);
                    NmredataWriter writer=new NmredataWriter(out);
                    writer.write(data, NmredataVersion.ONEPOINTONE);
                    writer.close();
                }else if(req.getParameter("format").equals("lsd")){
                	res.setContentType("text/plain");
	                LSDWriter writer=new LSDWriter(out);
	                writer.write(data);
	                writer.close();                	
                }else{
                    res.setContentType("text/html");
                    out.println("Invalid format!");
                }
                out.flush();
                return;
            } else if(action.equals("exportmdl")) {
                res.setContentType("text/plain");
                outstream=res.getOutputStream();
                DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(parameterParser.getString("moleculeid")));
                outstream.write(mol.getStructureFile(Integer.parseInt(parameterParser.getString("coordsetid")),false).getBytes());
            } else if(action.equals("exportcsearchreport")){
            	res.setContentType("text/html");
                out = res.getWriter();
                if(parameterParser.getString("spectrumid")!=null){
                    spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(parameterParser.getString("spectrumid")));
                    String htmlstring = new String(spectrum.getReportUrl()).substring(new String(spectrum.getReportUrl()).indexOf("<page>")+6,new String(spectrum.getReportUrl()).indexOf("</page>"));
                    String viewkey=parameterParser.getString("viewkey","-1");
                    if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && !spectrum.getReviewKey().equals(viewkey)){
                        TurbineUser user=null;
                        try{
                            user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                        }
                        catch(TorqueException ex){
                            out.println("Seems you are not properly logged in");
                        }
                        if(user!=null){
                        	if(spectrum.getUserId()!=user.getUserId()){
                        		out.println("Wrong user logged in");
                        	}else{
                        		out.println(htmlstring);
                        	}
                        }
                    }else{
                        out.println(htmlstring);
                    }
                }else{
                    TurbineUser user=null;
                    DBQuickCheck quickcheck=null;
                    String hmtlstring=null;
                    try{
                        user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                        quickcheck = DBQuickCheckPeer.retrieveByNameAndUser(parameterParser.getString("quickcheckid"),user.getUserId());
                        Builder builder = new Builder();
                        nu.xom.Document quickcheckXML = builder.build(new ByteArrayInputStream(quickcheck.getCml().getBytes()));
                        String csearchreport=quickcheckXML.getChild(0).getChild(4).toXML();
                        hmtlstring=csearchreport.substring(csearchreport.indexOf("<page><![CDATA[")+15,csearchreport.indexOf("]]></page>"));
                    }
                    catch(TorqueException ex){
                        out.println("Seems you are not properly logged in");
                    }
                    if(user!=null){
                        if(quickcheck.getUserId()!=user.getUserId()){
                            out.println("Wrong user logged in");
                        }else{
                            out.println(hmtlstring);
                        }
                    }
                }
            } else if(action.equals("exportnmrshiftdbreport")){
                res.setContentType("application/pdf");
                outstream=res.getOutputStream();
                if(parameterParser.getString("spectrumid")!=null){
                    spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(parameterParser.getString("spectrumid")));
                    byte[] report = new Base64().decode(spectrum.getReportNmrshiftdb());
                    String viewkey=parameterParser.getString("viewkey","-1");
                    if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && !spectrum.getReviewKey().equals(viewkey)){
                        TurbineUser user=null;
                        try{
                            user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                        }
                        catch(TorqueException ex){
                            out = res.getWriter();
                            out.println("Seems you are not properly logged in");
                        }
                        if(user!=null){
                            if(spectrum.getUserId()!=user.getUserId()){
                                out = res.getWriter();
                                out.println("Wrong user logged in");
                            }else{
                                outstream.write(report);
                            }
                        }
                    }else{
                        outstream.write(report);
                    }
                }
            } else if(action.equals("exportcsearchimage")){
                res.setContentType("image/png");
                outstream=res.getOutputStream();
                if(parameterParser.getString("spectrumid").indexOf("qc")==-1){
                    spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(parameterParser.getString("spectrumid")));
                    String imgid=parameterParser.getString("image");                        
                    byte[] image = new Base64().decode(new String(spectrum.getReportUrl()).substring(new String(spectrum.getReportUrl()).indexOf("<image"+imgid+">")+8,new String(spectrum.getReportUrl()).indexOf("</image"+imgid+">")));
                    String viewkey=parameterParser.getString("viewkey","-1");
                    if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && !spectrum.getReviewKey().equals(viewkey)){
                        TurbineUser user=null;
                        try{
                            user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                        }
                        catch(TorqueException ex){
                            outstream.println("Seems you are not properly logged in");
                        }
                        if(user!=null){
                        	if(spectrum.getUserId()!=user.getUserId()){
                        		outstream.println("Wrong user logged in");
                        	}else{
                                outstream.write(image, 0, image.length);
                        	}
                        }
                    }else{
                        outstream.write(image, 0, image.length);
                    }
                }else{
                    DBQuickCheck quickcheck = DBQuickCheckPeer.retrieveByPK(new NumberKey(parameterParser.getString("spectrumid").substring(2)));
                    String imgid=parameterParser.getString("image");                        
                    Builder builder = new Builder();
                    nu.xom.Document quickcheckXML = builder.build(new ByteArrayInputStream(quickcheck.getCml().getBytes()));
                    String csearchreport=quickcheckXML.getChild(0).getChild(4).toXML();
                    byte[] image = new Base64().decode(csearchreport.substring(csearchreport.indexOf("<image"+imgid+">")+8,csearchreport.indexOf("</image"+imgid+">")));
                    TurbineUser user=null;
                    try{
                        user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                    }
                    catch(TorqueException ex){
                        outstream.println("Seems you are not properly logged in");
                    }
                    if(user!=null){
                        if(quickcheck.getUserId()!=user.getUserId()){
                            outstream.println("Wrong user logged in");
                        }else{
                            outstream.write(image, 0, image.length);
                        }
                    }                    
                }
            } else if(action.equals("exportlastinputs")) {
                format=action;
            } else if(action.equals("printpredict")){
                res.setContentType("text/html");
                out = res.getWriter();
                HttpSession session=req.getSession();
                VelocityContext context = new VelocityContext();
                PredictAction.fillPredictContext(context, session, true,true,true,false,new StringBuffer(),getServletConfig(),req,true,false,null);
                context.put("baseurl", new UrlTool().getBaseUrl(req));
                //lets render the template
                StringWriter w = new StringWriter();
                Velocity.mergeTemplate("predictprint.vm", "ISO-8859-1", context, w);
                out.println(w.toString());
            } else if (action.equals("assignspectrum")) {
                res.setContentType("text/xml");
                out = res.getWriter();
                String cml=parameterParser.getString("cml");
                CMLBuilder builder = new CMLBuilder(false);
                CMLCml cmlcml = (CMLCml) builder.parseString(cml);
                GeneralUtils.getNmrshiftdbProperty("hosecode.spheres",this.getServletConfig());
                GeneralUtils.assignCML(cmlcml);
                out.print(cmlcml.toXML());
                try{
                    GeneralUtils.logToSql("assignspectrum - " + cml,null);
                }catch(Exception ex){
                    //ok
                }
            } else if (action.equals("doSearch")) {
            	out = res.getWriter();
            	NmrshiftdbRestServices.doSearch(parameterParser, out, RunDataFactory.getRunData(req, res, getServletConfig()));
            } else if (action.equals("dereplicate") || action.equals("doEludicate")) {
            	out = res.getWriter();
            	NmrshiftdbRestServices.doDereplicate(parameterParser, out);
            } else if (action.equals("publish")) {
            	out = res.getWriter();
            	NmrshiftdbRestServices.doPublish(parameterParser, out, getServletConfig());
            } else if (action.equals("doAssignment")) {
            	out = res.getWriter();
            	NmrshiftdbRestServices.doAssignment(parameterParser, out);
            } else if (action.equals("doSubmit")) {
            	out = res.getWriter();
            	NmrshiftdbRestServices.doSubmit(parameterParser, out, getServletConfig(),res);
            } else if (action.equals("getUserNumber")) {
            	out = res.getWriter();
            	List<Record> l=DBSessionsPeer.executeQuery("select SESSIONS from SESSIONS");
    	        out.write(l.get(0).getValue(1).asString());
            } else if (action.equals("getSpectrumTypes")){
            	out = res.getWriter();
    	        List<DBSpectrumType> v=DBSpectrumTypePeer.doSelect(new Criteria());
    	        out.print("[");
    	        for (int i = 0; i < v.size(); i++) {
    	            if (i > 0) {
    	                out.print(",");
    	            }
    	            out.print("\"" + v.get(i).getNameAsString() + "\"");
    	        }
    	        out.print("]");
            } else if (action.equals("searchorpredict") || action.equals("searchstructure") || action.equals("predict")) {
                boolean forcepredict=false;
            	boolean forcesearch=false;
            	String mode="exact";
            	if(action.equals("predict")){
                	action="searchorpredict";
                	forcepredict=true;            	
                } 
            	if (action.equals("searchstructure")) {
                	action="searchorpredict";
                	forcesearch=true;
                	mode=parameterParser.getString("mode","exact");
            	}
            	if (action.equals("searchorpredict")) {
	            	if(parameterParser.getString("format","cml").equals("jcamp"))
	            		res.setContentType("chemical/x-jcamp-dx");
	            	else
	            		res.setContentType("text/xml");
	            	if(parameterParser.getString("forcepredict","false").equals("true"))
	            		forcepredict=true;
	            	if(parameterParser.getString("forcesearch","false").equals("true"))
	            		forcesearch=true;
	                out = res.getWriter();
	                String smiles=parameterParser.getString("smiles");
	                String spectrumtype=parameterParser.getString("spectrumtype");
	                String solvent=parameterParser.getString("solvent");
	                SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	                IAtomContainer mol = sp.parseSmiles(smiles);
	                StructureDiagramGenerator sdg = new StructureDiagramGenerator();
	                sdg.setMolecule((IAtomContainer)mol);
	                sdg.generateCoordinates();
	                mol = sdg.getMolecule();
	                StringWriter sw = new StringWriter();
	                MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
	                mdlwriter.writeMolecule((IAtomContainer)mol);
	                String mdl=sw.toString();
	                mdlwriter.close();
	                ArrayList<ValuesForVelocityBean> v = new ArrayList<ValuesForVelocityBean>();
	                if(mode.equals("exact"))
	                	v.add(new ValuesForVelocityBean(NmrshiftdbConstants.TOTALSTRUCTURE, "--", mdl));
	                else if(mode.equals("substructure"))
	                	v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SUBSTRUCTURE_EXACT, "--", mdl));
	                else
	                	v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY, "--", mdl));
	                List<String> results = SearchUtils.executeSearch(v, new int[]{v.size()-1}, new StringAndInt(), new ArrayList<Double>(), false, false, RunDataFactory.getRunData(req, res, getServletConfig()), ServletUtils.expandRelative(this.getServletConfig(), "/WEB-INF/conf/normalizer.xml"), false, false, null, true, new StringBuffer(),false, false, null, false);
	                //check if something has been found
	                boolean found = false;
	                if(!forcepredict && results.size()>0){
	                    //check if it has a spectrum of desired type
	                    DBMolecule dbmol=DBMoleculePeer.retrieveByPK(new NumberKey((String)results.get(0)));
	                    List<DBSpectrum> spectra = dbmol.getDBSpectrums();
	                    for(int i=0;i<spectra.size();i++){
	                        if((spectrumtype==null || ((DBSpectrum)spectra.get(i)).getDBSpectrumType().getName().equals(spectrumtype)) && (solvent==null || spectra.get(i).getSolvent().equals(solvent)) ){
	                        	if(parameterParser.getString("format","cml").equals("jcamp")){
	                        		out.print(((DBSpectrum)spectra.get(i)).getPeakJcamp());
	                        	}else{
	                        		CMLMoleculeList moleculelist=new CMLMoleculeList();
	                        		moleculelist.appendChild(((DBSpectrum)spectra.get(i)).getCmlSpect(true, new UrlTool().getBaseUrl(req)+"/../"));
	                        		out.print(moleculelist.toXML());
	                        	}
	                            found=true;
	                            break;
	                        }
	                    }
	                }
	                if(forcesearch && !found) {
	                	res.sendError(HttpServletResponse.SC_NOT_FOUND);
	            	}
	                if(!found || forcepredict){
	                    try{
	                    	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
	                        ElectronDonation model       = ElectronDonation.cdk();
	                        CycleFinder      cycles      = Cycles.cdkAromaticSet();
	                        Aromaticity      aromaticity = new Aromaticity(model, cycles);
	                        aromaticity.apply(mol);
	                    }catch(CDKException ex){
	                        //do nothing
	                    }
	                    if(spectrumtype.equals("1H"))
	                    	AtomUtils.addExplicitHydrogensToSatisfyValency(mol);
	                    DBSpectrumType dbspectype=DBSpectrumTypePeer.getByName(spectrumtype);
	                    CMLPeakList peakList=new CMLPeakList();
	                    CMLCml cml=new CMLCml();
	                	if(parameterParser.getString("format","cml").equals("cml")){
	                        CMLSpectrum cmlspectrum=new CMLSpectrum();
		                    cmlspectrum.setType("NMR");
		                    cmlspectrum.setNamespaceURI(NmrshiftdbConstants.CML_NAMESPACE);
		                    cmlspectrum.addPeakList(peakList);
		                    cmlspectrum.setMoleculeRef("m1");
		                    CMLMetadataList ml=new CMLMetadataList();
		                    CMLMetadata metadata=new CMLMetadata();
		                    metadata.setName("nmr:method");
		                    metadata.setContent("HOSE code prediction");
		                    ml.addMetadata(metadata);
		                    cmlspectrum.addMetadataList(ml);
		                    cml.appendChild(cmlspectrum);
		                    cml.appendChild(new Convertor(true,null).cdkAtomContainerToCMLMolecule(mol));
	                	}
	                    for(int i=0;i<mol.getAtomCount();i++){
	                        if(dbspectype.getDBIsotope(1).getElementSymbol().equals(mol.getAtom(i).getSymbol())){
	                            try{
	                                double[] result=PredictionTool.generalPredict(mol,mol.getAtom(i),true,true,true,false,-1,-1,new StringBuffer(),true,true,null,new HashMap<Double,double[]>(),-1,true,new StringBuffer(),6,true,true,solvent==null ? null : DBConditionPeer.getByValue(solvent).get(0),null);
	                                CMLPeak peak=new CMLPeak();
	                                peak.setXMin(result[0]);
	                                peak.setXValue(result[1]);
	                                peak.setXMax(result[2]);
	                                peak.setXUnits("units:ppm");
	                                peak.setXWidth(result[3]);
	                                peak.setYMin(result[4]);
	                                peak.setYMax(result[5]);
	                                peak.setId("p"+i);
	                                if(solvent!=null)
	                                	peak.setProperty("commment", "solvent:"+solvent);
	                                String[] atomrefs=new String[]{mol.getAtom(i).getID()};
	                                peak.setAtomRefs(atomrefs);
	                                peakList.addPeak(peak);
	                            }catch(Exception ex){
	                                CMLPeak peak=new CMLPeak();
	                                peak.setConvention("Prediction impossible");
	                                peak.setId("p"+i);
	                                String[] atomrefs=new String[]{mol.getAtom(i).getID()};
	                                peak.setAtomRefs(atomrefs);
	                                peakList.addPeak(peak);
	                            }
	                        }
	                    }
	                	if(parameterParser.getString("format","cml").equals("jcamp")){
	                		double[][] xy = new double[2][peakList.getPeakChildren().size()];
	                		//this array needs to be filled with x/y values in 0/1 position respectivly
	                		IOrderedDataArray1D x = new OrderedArrayData(xy[0],CommonUnit.mz);
	                		IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);
	                		Spectrum1D jdxspectrum = new NMRSpectrum(x, y, dbspectype.getName(), 0D, 0D);
	                		Peak1D[] jcampPeaks = new Peak1D[peakList.getPeakChildren().size()];
	                		for(int i=0;i<peakList.getPeakChildren().size();i++){
	                			Peak1D jcampPeak = new Peak1D(peakList.getPeakChildren().get(i).getXValue(),0);
	                			jcampPeaks[i] = jcampPeak;
	                		}
	                		jdxspectrum.setPeakTable(jcampPeaks);
	                		JCAMPWriter jcamp = JCAMPWriter.getInstance();
	                		String jcampString = jcamp.toJCAMP(jdxspectrum);
	                		out.print(jcampString);
	                	}else{
	                		out.print(cml.toXML());
	                	}
	                }
	                try{
	                    GeneralUtils.logToSql("assignspectrum - " + smiles,null);
	                }catch(Exception ex){
	                    //ok
	                }
            	}
            } else if (action.equals("madices")) {
            	int id=parameterParser.getInt("nmrshiftdbid");
            	DBMolecule mol=DBMoleculePeer.retrieveByPK(id);
            	if(mol.isPublic()) {
                    res.setContentType("application/zip");
                    outstream=res.getOutputStream();
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    ZipOutputStream zipout=new ZipOutputStream(baos);
                    ByteArrayOutputStream baos2=new ByteArrayOutputStream();
                    HashMap<String,List<String>> solventandtemp=new HashMap<>();
                    for(DBSpectrum spectrumlocal : mol.getDBSpectrums()) {
                    	if(!solventandtemp.keySet().contains(spectrumlocal.getSolvent()))
                    		solventandtemp.put(spectrumlocal.getSolvent(),new ArrayList<String>());
                    	if(!solventandtemp.get(spectrumlocal.getSolvent()).contains(spectrumlocal.getTemperature())) {
	                    	NmreData data=mol.getNmreData(mol.getDBSpectrums().get(0), null, null, false, true, "https://www.nmrshiftdb.org/");
		                    NmredataWriter writer=new NmredataWriter(baos2);
		                    writer.write(data, NmredataVersion.ONEPOINTONE);
		                    writer.close();
		                    zipout.putNextEntry(new ZipEntry("nmrshiftdb"+mol.getMoleculeId()+"_"+spectrumlocal.getSolvent()+"_"+spectrumlocal.getTemperature()+".nmredata.sd"));
		                    zipout.write(baos2.toByteArray());
		                    zipout.closeEntry();
		                    solventandtemp.get(spectrumlocal.getSolvent()).add(spectrumlocal.getTemperature());
                    	}
                    }
                    RoCrate crate=mol.getRoCrate(baos2.toByteArray().length);
                    RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
                    File dir=new File(System.getProperty("java.io.tmpdir")+"/nmrshiftdb"+mol.getMoleculeId());
                    folderRoCrateWriter.save(crate, dir.getAbsolutePath());
                    zipout.putNextEntry(new ZipEntry("ro-crate-metadata.json"));
                    File filein=new File(dir.getAbsolutePath()+"/ro-crate-metadata.json");
                    InputStream is =  new FileInputStream(filein);
                    byte[] buf = new byte[32 * 1024]; // 32k buffer
                    int nRead = 0;
                    while( (nRead=is.read(buf)) != -1 ) {
                        zipout.write(buf, 0, nRead);
                    }
                    zipout.closeEntry();
                    for(DBSpectrum spectrumlocal : mol.selectSpectra()) {
                            if(spectrumlocal.getRawData()!=null && spectrumlocal.getRawData().length!=0) {
                                    zipout.putNextEntry(new ZipEntry(spectrumlocal.getSpectrumId()+"_"+spectrumlocal.getSpectrumTypeName()+".zip"));
                                    zipout.write(spectrumlocal.getRawDataDecoded());
                                    zipout.closeEntry();
                            }
                    }
                    zipout.close();
                    is =  new ByteArrayInputStream(baos.toByteArray());
                    nRead = 0;
                    while( (nRead=is.read(buf)) != -1 ) {
                        outstream.write(buf, 0, nRead);
                    }
	            }else {
                    res.setContentType("text/html");
                    out = res.getWriter();
                    out.println("Invalid molecule id");            		
            	}
            } else if (action.equals("quickcheck")) {
            	ObjectMapper mapper = new ObjectMapper();
            	QuickCheckRequest qcrequest = mapper.readValue(req.getReader(), QuickCheckRequest.class);
            	IAtomContainer ac=null;
            	if(qcrequest.getMoltxt().indexOf("V2000")>-1) {
	            	MDLV2000Reader reader=new MDLV2000Reader(new StringReader(qcrequest.getMoltxt()));
	            	ac=reader.read(new AtomContainer());
	            	reader.close();
            	}else {
	            	MDLV3000Reader reader=new MDLV3000Reader(new StringReader(qcrequest.getMoltxt()));
	            	ac=reader.read(new AtomContainer());
	            	reader.close();            		
            	}
            	AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
                ElectronDonation model       = ElectronDonation.cdk();
                CycleFinder      cycles      = Cycles.cdkAromaticSet();
                Aromaticity      aromaticity = new Aromaticity(model, cycles);
                aromaticity.apply(ac);
                AtomUtils.addAndPlaceHydrogens(ac);
        	    QuickCheckResponse qcr=new QuickCheckResponse();
        	    List<QuickCheckResponse.Result> results=new ArrayList<QuickCheckResponse.Result>();
                for(QuickCheckRequest.Input input : qcrequest.getInputs()){
	    	    	String type=input.getType();
	    	    	if(type.startsWith("nmr") && type.endsWith("1d")){
	    	    		String solventstring=input.getSolvent();
	    	    		DBCondition solvent = null;
	    	    		if(!solventstring.equals("Any") && !solventstring.equals("None")) {
	    	    			List<DBCondition> solvents = DBConditionPeer.getByValue(solventstring);
	    	    			if(solvents.size()==0) {
	    	    				QuickCheckResponse.Result result=qcr.new Result();
	    	    				result.setType("non-existing solvent");
	    		        	    results.add(result);
	    		        	    break;
	    	    			}
	    	    			solvent = solvents.get(0);
	    	    		}
		    	    	type=input.getType().substring(type.indexOf(";")+1);
		    	    	type=type.substring(0,type.indexOf(";"));
		            	CMLCml cml=new CMLCml();
		            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		            	CMLWriter cmlwriter=new CMLWriter(baos);
		            	cmlwriter.write(ac);
		                CMLBuilder builder = new CMLBuilder(false);
		                CMLMolecule cmlmol = (CMLMolecule) builder.parseString(baos.toString());
		    	    	cml.appendChild(cmlmol);
		    	    	cmlwriter.close();
		    	    	CMLSpectrum cmlspectrum=new CMLSpectrum();
		    	    	CMLPeakList peaklist=new CMLPeakList();
		    	    	cmlspectrum.appendChild(peaklist);
		    	    	CMLMetadataList mdl=new CMLMetadataList();
		    	    	cmlspectrum.addMetadataList(mdl);
		    	    	CMLMetadata md=new CMLMetadata();
		    	    	mdl.addMetadata(md);
		    	    	md.setName("nmr:OBSERVENUCLEUS");
		    	    	DBSpectrumType spectype=DBSpectrumTypePeer.getByName(type);
		    	    	double[] limits=GeneralUtils.getPredictionLimits(type);
		    	    	md.setContent(spectype.getName());
		            	StringTokenizer st=new StringTokenizer(input.getShifts(),";");
		    	    	while(st.hasMoreTokens()){
		    	    		CMLPeak peak=new CMLPeak();
		    	    		peak.setXValue(st.nextToken());
		    	    		peaklist.appendChild(peak);
		    	    	}
		        	    cml.appendChild(cmlspectrum);    
		        	    List<Map<Integer, Object>> assignresult = GeneralUtils.assignCML(cml);
		        	    QuickCheckResponse.Result result=qcr.new Result();
		        	    results.add(result);
		        	    List<QuickCheckResponse.Shift> shifts=new ArrayList<QuickCheckResponse.Shift>();
		        	    int missing=0;
		        	    int accept=0;
		        	    int warning=0;
		        	    int reject=0;
		        	    boolean[] atomshit=new boolean[ac.getAtomCount()];
		        	    Map<String, Double> predresults = new HashMap<>();
		        	    for(CMLPeak peak : ((CMLSpectrum)cml.getChildCMLElements("spectrum").get(0)).getPeakListElements().get(0).getPeakChildren()){
		        	    	for(String atom : peak.getAtomRefs()) {
		        	    		QuickCheckResponse.Shift shift=qcr.new Shift();
		        	    		shift.setAtom(ac.getAtomNumber(AtomUtils.getAtomById(ac, atom))+1);
		        	    		atomshit[ac.getAtomNumber(AtomUtils.getAtomById(ac, atom))]=true;
		        	    		shift.setReal(peak.getXValue());
		        	    		try {
		        	    			String hose=(String)assignresult.get(0).get(ac.getAtomNumber(AtomUtils.getAtomById(ac, atom)));
		        	    			if(!predresults.containsKey(hose)) {
		        	    				predresults.put(hose, ((double[])assignresult.get(1).get(ac.getAtomNumber(AtomUtils.getAtomById(ac, atom))))[1]);
		        	    			}
		        	    			shift.setPrediction(predresults.get(hose));
			        	    		shift.setDiff(Math.abs(shift.getReal()-shift.getPrediction()));
			        	    		shift.setHoseCode(hose);
			        	    		shift.setSpheres((int)((double[])assignresult.get(1).get(ac.getAtomNumber(AtomUtils.getAtomById(ac, atom))))[4]);
			        	    		if(shift.getDiff()<limits[1]){
			        	    			shift.setStatus("accept");
			        	    			accept++;
			        	    		}else if(shift.getDiff()<limits[0]){
			        	    			shift.setStatus("warning");
			        	    			warning++;
			        	    		}else{
			        	    			shift.setStatus("reject");
			        	    			reject++;
			        	    		}
		        	    		}catch(Exception ex) {
			       	    			shift.setStatus("prediction impossible");
			       	    			shift.setDiff(0);
		        	    		}
		        	    		shifts.add(shift);
		        	    	}
		        	    	if(peak.getAtomRefs().length==0){
		        	    		QuickCheckResponse.Shift shift=qcr.new Shift();
		        	    		shift.setAtom(0);
		        	    		shift.setReal(peak.getXValue());
		        	    		shift.setPrediction(0);
		        	    		shift.setDiff(0);
		       	    			shift.setStatus("missing");
		        	    		shifts.add(shift);
		        	    		missing++;
		        	    	}
		        	    }
		        	    for(int i=0;i<atomshit.length;i++){
		        	    	if(!atomshit[i] && ac.getAtom(i).getSymbol().equals(spectype.getElementSymbol())){
		        	    		QuickCheckResponse.Shift shift=qcr.new Shift();
		        	    		shift.setAtom(i+1);
		        	    		shift.setReal(0);
		        	    		try {
		        	    			String hose=(String)assignresult.get(0).get(i);
		        	    			if(!predresults.containsKey(hose)) {
		        	    				predresults.put(hose, ((double[])assignresult.get(1).get(i))[1]);
		        	    			}
		        	    			shift.setPrediction(predresults.get(hose));
			       	    			shift.setStatus("missing");
			        	    		shift.setHoseCode(hose);
			        	    		shift.setSpheres((int)((double[])assignresult.get(1).get(i))[4]);
		        	    		}catch(Exception ex) {
			       	    			shift.setStatus("prediction impossible");		        	    			
		        	    		}
		        	    		shift.setDiff(0);
		        	    		shifts.add(shift);
		        	    		missing++;
		        	    	}
		        	    }
		        	    result.setShifts(shifts.toArray(new QuickCheckResponse.Shift[shifts.size()]));
		        	    result.setType(input.getType());
		        	    result.setId(input.getId());
		        	    QuickCheckResponse.Statistics stats=qcr.new Statistics();
		            	stats.setMissing(missing);
		            	stats.setAccept(accept);
		            	stats.setWarning(warning);
		            	stats.setReject(reject);
		            	stats.setTotal(missing+accept+warning+reject);
		            	result.setStatistics(stats);
	    	    	}
                }
        	    qcr.setResult(results.toArray(new QuickCheckResponse.Result[results.size()]));
            	res.setContentType("application/json");
                out = res.getWriter();
            	mapper.writeValue(out, qcr);
            	try{
	            	GeneralUtils.logToSql("QuickCheck - " + qcrequest.getMoltxt(), null);
	            }catch(Exception ex){
	                //ok
	            }
            } else {
                res.setContentType("text/html");
                out = res.getWriter();
                out.println("No valid action");
            }
            if(format==null)
                format="";
            if (format.equals("pdf") || format.equals("pdforig") || format.equals("rtf") || format.equals("pdfimg")) {
                res.setContentType("application/"+format);
                outstream=res.getOutputStream();
            }
            if ( format.equals("docbook")) {
                res.setContentType("application/zip");
                outstream=res.getOutputStream();
            }
            if ( format.equals("nmredata.zip") || format.equals("otherfiles") ) {
                res.setContentType("application/zip");
                outstream=res.getOutputStream();
            }
            if ( format.equals("cmcse")) {
                res.setContentType("application/zip");
                outstream=res.getOutputStream();
            }
            if (format.equals("svg")) {
                res.setContentType("image/x-svg");
                out = res.getWriter();
            }
            if (format.equals("tiff")) {
                res.setContentType("image/tiff");
                outstream=res.getOutputStream();
            }
            if (format.equals("jpeg")) {
                res.setContentType("image/jpeg");
                outstream=res.getOutputStream();
            }
            if (format.equals("png")) {
                res.setContentType("image/png");
                outstream=res.getOutputStream();
            }
            if (format.equals("rawdata")) {
                outstream=res.getOutputStream();
                DBSpectrumPeer.executeStatement("update SPECTRUM set RAW_DATA_VIEWS=RAW_DATA_VIEWS+1 where SPECTRUM_ID="+spectrum.getSpectrumId());
                if(spectrum.getRawData()!=null && (spectrum.getRawDataFormat()=="fid" || spectrum.getRawDataFormat()=="ser" || spectrum.getRawDataFormat()=="mnova")){
                    res.setContentType("application/octet-stream");
                }else{
                    res.setContentType("application/zip");
                }
            }
            if (format.equals("mdl") || format.equals("txt") || format.equals("cml") || format.equals("cmlboth") || format.equals("nmredata.sd") || format.indexOf("exsection")==0 || format.indexOf("findingaid")==0) {
                res.setContentType("text/plain");
                out = res.getWriter();
            }
            if (format.equals("simplehtml") || format.equals("exportlastinputs")) {
                res.setContentType("text/html");
                out = res.getWriter();
            }
            if (format.equals("jcamp") || format.equals("jcamppeaks")) {
                res.setContentType("chemical/x-jcamp-dx");
                out = res.getWriter();
            }
            if (format.equals("originalimage")) {
                res.setContentType("image/"+spectrum.getImageFormat());
                outstream=res.getOutputStream();
            }
            if (action.equals("exportlastinputs")) {
                int numbertoexport=4;
                if(parameterParser.getString("numbertoexport")!=null){
                    try{
                        numbertoexport=Integer.parseInt(parameterParser.getString("numbertoexport"));
                        if(numbertoexport<1 || numbertoexport>20)
                            throw new NumberFormatException("Number to small/large");
                    }
                    catch(NumberFormatException ex){
                        out.println("The parameter <code>numbertoexport</code>must be an integer from 1 to 20");
                    }
                }
                TurbineUser user=null;
                try{
                    user=NmrshiftdbUserPeer.getByName(parameterParser.getString("username"));
                }
                catch(TorqueException ex){
                    out.println("Seems <code>username</code> is not OK: "+ex.getMessage());
                }
                if(user!=null){
                    Vector<String> parameters=new Vector<String>();
                    String query="select distinct MOLECULE.MOLECULE_ID from MOLECULE, SPECTRUM where SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and SPECTRUM.REVIEW_FLAG =\"true\" and SPECTRUM.USER_ID="+user.getUserId()+" order by MOLECULE.DATE desc;";
                    List<Record> l=NmrshiftdbUserPeer.executeQuery(query);
                    String url=req.getRequestURL().toString();
                    url=url.substring(0,url.length()-17);
                    for(int i=0;i<numbertoexport;i++){
                        if(i==l.size())
                            break;
                        DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asInt()));
                        parameters.add(new String("<a href=\""+new UrlTool().getBaseUrl(req)+"/js_pane/P-Results?nmrshiftdbaction=showDetailsFromHome&molNumber="+mol.getMoleculeId()+"\"><img src=\""+req.getRequestURL().toString()+"?nmrshiftdbaction=exportmol&spectrumid="+ ((DBSpectrum)mol.getDBSpectrums().get(0)).getSpectrumId() +"&format=jpeg&size=150x150&backcolor=12632256\"></a>"));
                    }
                    //lets make a context and put the data into it
                    VelocityContext context=new VelocityContext();
                    context.put("results",parameters);
                    //lets render the template
                    StringWriter w = new StringWriter();
                    Velocity.mergeTemplate("lateststructures.vm", "ISO-8859-1", context, w);
                    out.println(w.toString());
                }
            }
            if (action.equals("exportspec")) {
                //User requested ascii table
                if (format.equals("txt")) {
                	//we have to distinguish between export from db and during submit
                	if(specfileFromSubdata!=null){
                		SubmittingData subData=((SubmittingData) req.getSession().getAttribute("subData"));
                		IMolecularFormula mfa = MolecularFormulaManipulator.getMolecularFormula(subData.getMolWithH());
                        out.print(subData.getChemNameWithSemicolon()+" "+MolecularFormulaManipulator.getString(mfa)+"; "+MolecularFormulaManipulator.getNaturalExactMass(mfa)+" Dalton\n\r");
                        out.print("\n\rAtom\t");
                        if(spectrum.getDBSpectrumType().getElementSymbol()==("H"))
                            out.print("Mult.\t");
                        out.print("Meas.");
                        out.print("\n\r");
                        out.print("No.\t");
                        if(spectrum.getDBSpectrumType().getElementSymbol()==("H"))
                            out.print("\t");
                        out.print("Shift");
                        out.print("\n\r");
                        for(int i=0;i<subData.getConfirmShifts().size();i++){
                                out.print(subData.getConfirmShifts().get(i).getDisplayText()+"\t"+subData.getConfirmShifts().get(i).getRange());
                                out.print("\n\r");
                        }
                	}else{
                        String lastsearchtype=parameterParser.getString("lastsearchtype");
                        if(lastsearchtype.equals(NmrshiftdbConstants.TOTALSPECTRUM) || lastsearchtype.equals(NmrshiftdbConstants.SUBSPECTRUM)){
                        	List<CMLPeak> l=ParseUtils.parseSpectrumFromSpecFile(parameterParser.getString("lastsearchvalues"));
                            spectrum.initSimilarity(l,lastsearchtype.equals(NmrshiftdbConstants.SUBSPECTRUM));
                        }
                        List<ValuesForVelocityBean> v=spectrum.getOptions(1,false);
                        DBMolecule mol=spectrum.getDBMolecule();
                        if(mol!=null){
    	                    out.print(mol.getChemicalNamesAsOneString(false,false)+"; "+mol.getMolecularFormula(false)+"; "+mol.getMolecularWeight()+" Dalton\n\r");
    	                    out.print("\n\rAtom\t");
                        }
                        if(spectrum.getDBSpectrumType().getElementSymbol()==("H"))
                            out.print("Mult.\t");
                        out.print("Meas.");
                        if(lastsearchtype.equals(NmrshiftdbConstants.TOTALSPECTRUM) || lastsearchtype.equals(NmrshiftdbConstants.SUBSPECTRUM)){
                            out.print("\tInput\tDiff");
                        }
                        out.print("\n\r");
                        if(mol!=null){
                        	out.print("No.\t");
                        }
                        if(spectrum.getDBSpectrumType().getElementSymbol()==("H"))
                            out.print("\t");
                        out.print("Shift");
                        if(lastsearchtype.equals(NmrshiftdbConstants.TOTALSPECTRUM) || lastsearchtype.equals(NmrshiftdbConstants.SUBSPECTRUM)){
                            out.print("\tShift\tM-I");
                        }
                        out.print("\n\r");
                        for(int i=0;i<v.size();i++){
    						if(mol!=null)
    							out.print(((ValuesForVelocityBean)v.get(i)).getDisplayText());
    						if(spectrum.getDBSpectrumType().getDimensionality()>1){
    							for(int k=0;k<spectrum.getDBSpectrumType().getDimensionality();k++){
    								out.print(((ValuesForVelocityBean)v.get(i)).getShift(k)+";");
    							}
    						}else{
    							out.print("\t"+((ValuesForVelocityBean)v.get(i)).getRange());
    						}
                            if(lastsearchtype.equals(NmrshiftdbConstants.TOTALSPECTRUM) || lastsearchtype.equals(NmrshiftdbConstants.SUBSPECTRUM)){
                                out.print("\t"+((ValuesForVelocityBean)v.get(i)).getNameForElements()+"\t"+((ValuesForVelocityBean)v.get(i)).getDelta());
	                        }
	                        out.print("\n\r");
	                    }
                	}
                }
                if (format.equals("jcamp")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getJcamp()));
                	}else{
                		out.print(new String(spectrum.getJcamp()));
                	}
                }
                if (format.equals("otherfiles")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getOtherFiles()));
                	}else{
                		byte[] otherfiles = spectrum.getOtherFilesDecoded();
                		outstream.write(otherfiles,0,otherfiles.length);
                	}
                }
                if (format.equals("cmcse")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getCmcseFile()));
                	}else{
                		byte[] otherfiles = spectrum.getCmcseFilesDecoded();
                		outstream.write(otherfiles,0,otherfiles.length);
                	}
                }
                if (format.equals("pdforig")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getPdf()));
                	}else{
	                    byte[] pdf = spectrum.getPdfDecoded();
	                    if(UrlTool.doImageCache() && molecule.isPublic()){
	                        FileOutputStream fos = new FileOutputStream(new File(ServletUtils.expandRelative(this.getServletConfig(), "/images/spectra/pdf/"+spectrum.getSpectrumId()+".pdf")));
	                        fos.write(pdf);
	                        fos.close();
	                    }
	                    outstream.write(pdf,0,pdf.length);
                	}
                }
                if (format.equals("rawdata")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getRawData()));
                	}else if(spectrum.getRawData()!=null){
                        byte[] rawdata = spectrum.getRawDataDecoded();
                        outstream.write(rawdata,0,rawdata.length);
                    }else if(spectrum.hasRawDataInFilesystem()){
                        res.sendRedirect(spectrum.getRawDataFromFileSystemURL());
                    }
                }
                if (format.equals("jcamppeaks")) {
                	out.print(new String(spectrum.getPeakJcamp()));
                }
                if (format.equals("originalimage")) {
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                		outstream.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getImage()));
                	}else if(spectrum.getImage()!=null){
                        byte[] image=spectrum.getImageDecoded();
                        if(UrlTool.doImageCache() && molecule.isPublic()){
                            FileOutputStream fos = new FileOutputStream(new File(ServletUtils.expandRelative(this.getServletConfig(), "/images/spectra/original/"+spectrum.getSpectrumId()+"."+spectrum.getImageFormat())));
                            fos.write(image);
                            fos.close();
                        }
                        outstream.write(image);
                    }
                }
                //User requested simple html
                if (format.equals("simplehtml")) {
                    String i1;
                    if(spectrum.getImage()!=null){
                    	i1 = ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis()+"."+spectrum.getImageFormat();
                        OutputStream ostream = new FileOutputStream(i1);
                        ostream.write(spectrum.getImageDecoded());
                        ostream.close();
                    }else if(specfileFromSubdata!=null){
                        i1 = export.getImage(false, "jpeg", ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis(), -1, -1, true, null, specfileFromSubdata, null);
                    }else{
                        i1 = export.getImage(false, "jpeg", ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis(),true,null,null);
                    }
                    String i2 = export.getImage(true, "jpeg", ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis(),false,null,Color.BLUE);
                    export.pictures[0] = new File(i2).getName();
                    export.pictures[1] = new File(i1).getName();
                    String docbook = export.getHtml(req, specfileFromSubdata!=null ? ((SubmittingData) req.getSession().getAttribute("subData")) : null);
                    out.print(docbook);
                }         
                //User requested pdf or rtf
                if (format.equals("pdf") || format.equals("rtf")) {
                    //Create both svgs
                	FileOutputStream fos;
                    if(spectrum.getImage()!=null){
                    	String i1 = ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis()+"."+spectrum.getImageFormat();
                        OutputStream ostream = new FileOutputStream(i1);
                        ostream.write(spectrum.getImageDecoded());
                        ostream.close();
                        export.pictures[1] = i1;
                    }else {
                        String svgSpec;
                    	if(specfileFromSubdata!=null){
                    		svgSpec=export.getSpecSvg(400,200,specfileFromSubdata);
                    	}else{
                    		svgSpec = export.getSpecSvg(400,200);
                    	}
                        String svgspecfile = relativepath + "/tmp/" + System.currentTimeMillis() + "s.svg";
                        fos = new FileOutputStream(svgspecfile);
                        fos.write(svgSpec.getBytes());
                        fos.close();
                        export.pictures[1] = svgspecfile;
                    }
                    String molSvg = export.getMolSvg(false);
                    String svgmolfile = relativepath + "/tmp/" + System.currentTimeMillis() + "m.svg";
                    fos = new FileOutputStream(svgmolfile);
                    fos.write(molSvg.getBytes());
                    fos.close();
                    export.pictures[0] = svgmolfile;
                    //Make the docbook
                    String docbook = export.getDocbook("pdf","SVG", specfileFromSubdata!=null ? ((SubmittingData) req.getSession().getAttribute("subData")) : null);
                    //Make fo via xalan
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer(new StreamSource("file:" + ServletUtils.expandRelative(this.getServletConfig(),"/WEB-INF/docbook-xsl/fo/docbook.xsl")));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    transformer.transform(new StreamSource(new StringReader(docbook)), new StreamResult(baos));
                    FopFactory fopFactory = FopFactory.newInstance(new File(ServletUtils.expandRelative(this.getServletConfig(), "/WEB-INF/conf/fop.xconf")));
                    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                    Fop fop = fopFactory.newFop(format.equals("rtf") ? MimeConstants.MIME_RTF : MimeConstants.MIME_PDF, out2);
                    TransformerFactory factory = TransformerFactory.newInstance();
                    transformer = factory.newTransformer();
                    Source src = new StreamSource(new StringReader(baos.toString()));
                    Result res2 = new SAXResult(fop.getDefaultHandler());
                    transformer.transform(src, res2);
                    res.setContentLength(out2.size());
                    outstream.write(out2.toByteArray());
                }
                //User requested docbook
                if (format.equals("docbook")) {
                    String i1 = relativepath + "/tmp/" + System.currentTimeMillis() + ".svg";
                    if(spectrum.getImage()!=null){
                    	i1 = ServletUtils.expandRelative(this.getServletConfig(),"/nmrshiftdbhtml") + "/tmp/" + System.currentTimeMillis()+"."+spectrum.getImageFormat();
                        OutputStream ostream = new FileOutputStream(i1);
                        ostream.write(spectrum.getImageDecoded());
                        ostream.close();
                        export.pictures[1] = i1;
                    }else if(specfileFromSubdata!=null){
                    	new FileOutputStream(i1).write(export.getSpecSvg(400,200,specfileFromSubdata).getBytes());
                    }else{
                    	new FileOutputStream(i1).write(export.getSpecSvg(300,200).getBytes());
                    }
                    export.pictures[0] = new File(i1).getName();
                    String i2 = relativepath + "/tmp/" + System.currentTimeMillis() + ".svg";
                    FileOutputStream fos = new FileOutputStream(i2);
                    fos.write(export.getMolSvg(false).getBytes());
                    fos.close();
                    export.pictures[1] = new File(i2).getName();
                    String docbook = export.getDocbook("pdf","SVG",specfileFromSubdata!=null ? ((SubmittingData) req.getSession().getAttribute("subData")) : null);
                    String docbookfile = relativepath + "/tmp/" + System.currentTimeMillis() + ".xml";
                    fos= new FileOutputStream(docbookfile);
                    fos.write(docbook.getBytes());
                    fos.close();
                    ByteArrayOutputStream baos=export.makeZip(new String[]{docbookfile,i1,i2});
                    outstream.write(baos.toByteArray());
                }
                //User requested spectrum as svg
                if (format.equals("svg")) {
                	if(specfileFromSubdata!=null)
                		out.print(export.getSpecSvg(400,200,specfileFromSubdata));
                	else
                		out.print(export.getSpecSvg(400,200));
                }
                if(format.equals("tiff")||format.equals("jpeg")||format.equals("png")||format.equals("pdfimg")){
                    int width=-1;
                    int height=-1;
                    if(parameterParser.getString("size")!=null){
                    	String size = parameterParser.getString("size");
                    	if(size.equals("large")){
                    		width=350;
                    		height=250;
                    	}else if(size.equals("small")){
                    		width=150;
                    		height=150;                    		
                    	}else{
	                        StringTokenizer st=new StringTokenizer(size,"x");
	                        width=Integer.parseInt(st.nextToken());
	                        height=Integer.parseInt(st.nextToken());
                    	}
                    }
                    if(parameterParser.getString("backcolor")!=null){
                        export.backColor=new Color(Integer.parseInt(parameterParser.getString("backcolor")));
                    }
                    InputStream is;
                    if(specfileFromSubdata!=null)
                        is =  new FileInputStream(export.getImage(false,format.equals("pdfimg") ? "pdf" : format,relativepath + "/tmp/" + System.currentTimeMillis(),width,height,true,null,specfileFromSubdata, null));
                    else
                        is =  new FileInputStream(export.getImage(false,format.equals("pdfimg") ? "pdf" : format,relativepath + "/tmp/" + System.currentTimeMillis(),width,height,true,null,null,null));
                    byte[] buf = new byte[32 * 1024]; // 32k buffer
                    int nRead = 0;
                    FileOutputStream fos = null;
                    if(!parameterParser.getString("spectrumid","").equals("subdata") && !parameterParser.getString("moleculeid","").equals("subdata") && UrlTool.doImageCache() && molecule.isPublic() && format.equals("jpeg") && parameterParser.getString("size")!=null && (parameterParser.getString("size").equals("small") || parameterParser.getString("size").equals("large"))){
                    	fos = new FileOutputStream(new File(ServletUtils.expandRelative(this.getServletConfig(), "/images/spectra/"+parameterParser.getString("size")+"/"+spectrum.getSpectrumId()+".jpeg")));
                    }
                    while( (nRead=is.read(buf)) != -1 ) {
                        if(fos!=null)
                        	fos.write(buf, 0, nRead);
                        outstream.write(buf, 0, nRead);
                    }
                    if(fos!=null){
                    	fos.close();
                    }
                    if(is!=null){
                    	is.close();
                    }
                }
                if(format.equals("cml")){
                    ByteArrayOutputStream outlocal = new ByteArrayOutputStream();
                    Serializer ser = new Serializer(outlocal, "ISO-8859-1");
                    ser.setIndent(1);
                    ser.write(new Document(spectrum.getCmlSpect(false, new UrlTool().getBaseUrl(req)+"/../")));
                    out.print(outlocal);
                }
                if(format.equals("nmredata.sd")){
                	NmreData data=null;
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                        data=new NmreData();
                        String solvent=spectrum.getSolvent();
                		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.*?)\\((.*?)\\)");
                		Matcher matcher = pattern.matcher(solvent);
                		if(matcher.find())
                			solvent=matcher.group(2);
                        data.setSolvent(solvent);
                        double freq=0;
                        try{
                        	freq=Double.parseDouble(spectrum.getFrequency());
                        }catch(NumberFormatException ex){
                        	//do nothing
                        }
                        IAtomContainer molwithh=spectrum.getDBMolecule().getAsCDKMolecule(1);
                        data.setMolecule(molwithh);
                        double[][] xy=new double[0][];
                       	spectrum.getNmreData(false, false, spectrum, freq, xy, "", new UrlTool().getBaseUrl(req)+"/../", data);
                        StringBuffer sb=new StringBuffer();
	                	sb.append("DB_ID=undefined");
	                	data.setID(sb.toString());
                	}else {
	                    data=spectrum.getDBMolecule().getNmreData(spectrum, req,null,false, false, spectrum.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, new UrlTool().getBaseUrl(req)+"/../"));
                	}
                	NmredataWriter writer=new NmredataWriter(out);
                    writer.write(data, NmredataVersion.ONEPOINTONE);
                    writer.close();
                }
                if ( format.equals("nmredata.zip")){
                	List<File> files=new ArrayList<File>();
                	List<String> locations=new ArrayList<String>();
                	boolean hasrawdata=false;
            		File file=new File(System.getProperty("java.io.tmpdir")+File.separator+"10.zip");
            		FileOutputStream fos=new FileOutputStream(file);
                	if(parameterParser.getString("spectrumid").equals("subdata") && ((SubmittingData) req.getSession().getAttribute("subData")).getRawData()!=null){
                		hasrawdata=true;
                		fos.write(new Base64().decode(((SubmittingData) req.getSession().getAttribute("subData")).getRawData()));
                	}else if(spectrum.getRawData()!=null){
                		hasrawdata=true;
                		fos.write(spectrum.getRawDataDecoded());
                	}
            		fos.close();
            		if(hasrawdata) {
                		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir")+File.separator+"10"));
                		ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
                		ZipEntry zipEntry = zis.getNextEntry();
                		byte[] buffer = new byte[1024];
                		while(zipEntry != null){
                			String fileName = zipEntry.getName();
                			File newFile = new File(System.getProperty("java.io.tmpdir")+File.separator+"10/" + fileName);
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
                		files.add(new File(System.getProperty("java.io.tmpdir")+File.separator+"10"));
                		locations.add("file:./"+spectrum.getDBMolecule().getMoleculeId()+"/10");
                	}
                	NmreData data=null;
                	if(parameterParser.getString("spectrumid").equals("subdata")){
                        data=new NmreData();
                        String solvent=spectrum.getSolvent();
                		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.*?)\\((.*?)\\)");
                		Matcher matcher = pattern.matcher(solvent);
                		if(matcher.find())
                			solvent=matcher.group(2);
                        data.setSolvent(solvent);
                        double freq=0;
                        try{
                        	freq=Double.parseDouble(spectrum.getFrequency());
                        }catch(NumberFormatException ex){
                        	//do nothing
                        }
                        IAtomContainer molwithh=spectrum.getDBMolecule().getAsCDKMolecule(1);
                        data.setMolecule(molwithh);
                        double[][] xy=new double[0][];
                       	spectrum.getNmreData(false, false, spectrum, freq, xy, hasrawdata? locations.get(0) : "", new UrlTool().getBaseUrl(req)+"/../", data);
                        StringBuffer sb=new StringBuffer();
	                	sb.append("DB_ID=undefined");
	                	data.setID(sb.toString());
                	}else {
	                    data=spectrum.getDBMolecule().getNmreData(spectrum, req,locations,false,false, spectrum.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, new UrlTool().getBaseUrl(req)+"/../"));
                	}
                	NmreDataRecord record=new NmreDataRecord(files, data);
                	NmredataRecordWriter writer=new NmredataRecordWriter(outstream);
                    writer.write(record, spectrum.getDBMolecule().getMoleculeId()+"", NmredataVersion.ONEPOINTONE);
                }
                if(format.equals("findingaid") || format.equals("findingaiddata")) {
            		JsonObject jsonroot = new JsonObject();
            		JsonObject firstlevel = new JsonObject();
            		jsonroot.add("IUPAC.FAIRSpec.findingAid", firstlevel);
            		firstlevel.addProperty("id", "nmrsiftdb2:"+spectrum.getMoleculeId());
            		firstlevel.addProperty("version", "IFD 0.1.0-beta+2025.07.24;FAIRSpec 0.1.0-beta+2025.07.23");
            		firstlevel.addProperty("created", Instant.now().toString());
            		firstlevel.addProperty("createdBy", "nmrshiftdb2 at "+new UrlTool().getBaseUrl(req));
            		JsonArray collections=new JsonArray();
            		JsonObject structures=new JsonObject();
      	          	structures.addProperty("count",1);
      	          	structures.addProperty("id", "structures");
      	          	collections.add(structures);
      	          	JsonObject spectra=new JsonObject();
      	          	spectra.addProperty("count",spectrum.getDBMolecule().selectSpectra().size());
      	          	spectra.addProperty("id", "spectra");
    	          	collections.add(spectra);
    	          	JsonObject contents = new JsonObject();
    	          	contents.add("collections", collections);
    	          	contents.addProperty("relatedCount",0);
    	          	contents.addProperty("resourceCount",0);
    	          	firstlevel.add("contents",contents);
    	          	JsonObject collectionset=new JsonObject();
    	          	collectionset.addProperty("propertyPrefix","IFD.property.collectionset");
    	          	collectionset.addProperty("resourceID","FAIRSpecDataCollection");
    	          	JsonObject ifdProperties=new JsonObject();
    	          	ifdProperties.addProperty("source_publication_uri","https://www.nmrshiftdb.org/molecule/"+spectrum.getDBMolecule().getMoleculeId());
    	          	collectionset.add("ifdProperties",ifdProperties);
    	          	if(spectrum.getDBMolecule().getDoi()!=null && !spectrum.getDBMolecule().getDoi().equals(""))
            	    	collectionset.addProperty("doi", spectrum.getDBMolecule().getDoi());
            	    collectionset.addProperty("byID","true");
            	    firstlevel.add("collectionSet", collectionset);
            	    JsonObject itemsbyid=new JsonObject();
            	    collectionset.add("itemsByID",itemsbyid);
            	    JsonObject structures2=new JsonObject();
            	    itemsbyid.add("structures",structures2);
            	    JsonObject itemsbyid2=new JsonObject();
            	    structures2.add("itemsByID",itemsbyid2);
            	    JsonObject one=new JsonObject();
            	    structures2.add("[1]", one);
            	    one.addProperty("propertyPrefix","IFD.property.structure");
            	    JsonArray representations=new JsonArray();
            	    one.add("representations", representations);
            	    JsonObject mdl=new JsonObject();
            	    representations.add(mdl);
            	    mdl.addProperty("representationType","IFD.representation.structure.mol");
            	    mdl.addProperty("mediaType","chemical/x-mdl-molfile");
            	    JsonObject ref=new JsonObject();
            	    mdl.add("ref", ref);
            	    if(format.equals("findingaid")) {
                	    ref.addProperty("ifdType", "org.iupac.fairdata.core.IFDReference");
            	    	ref.addProperty("url",  new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum.getSpectrumId()+".mol?spectrumid="+spectrum.getSpectrumId()+"&nmrshiftdbaction=exportmol&format=mdl");
                	    mdl.addProperty("len", spectrum.getDBMolecule().getStructureFile(1,null,false,true).length());
                	    ref.addProperty("localName",spectrum.getDBMolecule().getMoleculeId()+".mol");
            	    } else {
                	    ref.addProperty("ifdType", "org.iupac.fairdata.core.IFDRepresentation");
                        byte[] fileContent = spectrum.getDBMolecule().getStructureFile(1,null,false,true).getBytes();
                        byte[] encoded = new Base64().encode(fileContent);
            	    	ref.addProperty("data", ";base64,"+new String(encoded));
                	    mdl.addProperty("len", encoded.length);
            	    }
            	    JsonObject png=new JsonObject();
            	    representations.add(png);
            	    png.addProperty("representationType","IFD.representation.structure.jpg");
            	    png.addProperty("mediaType", "image/jpg");
            	    JsonObject ref2=new JsonObject();
            	    png.add("ref", ref2);
            	    if(format.equals("findingaid")) {
                	    ref2.addProperty("ifdType", "org.iupac.fairdata.core.IFDReference");
            	    	ref2.addProperty("url",  new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum.getMoleculeId()+".jpeg?spectrumid="+spectrum.getSpectrumId()+"&nmrshiftdbaction=exportmol&shownumbers=false&format=jpeg");
                	    ref2.addProperty("localName",spectrum.getDBMolecule().getMoleculeId()+"_"+spectrum.getSpectrumId()+".jpeg");            	    
            	    } else {
                	    ref2.addProperty("ifdType", "org.iupac.fairdata.core.IFDRepresentation");
                        String imagefile =  export.getImage(true,"jpeg",relativepath + "/tmp/" + System.currentTimeMillis(),600,400,true,null,null,null);
                        byte[] fileContent = Files.readAllBytes(new File(imagefile).toPath());
                        byte[] encoded = new Base64().encode(fileContent);
            	    	ref2.addProperty("data", ";base64,"+new String(encoded));
                	    png.addProperty("len", encoded.length );
            	    }
            	    JsonObject spectra2=new JsonObject();
            	    itemsbyid.add("spectra",spectra2);
            	    spectra2.addProperty("id", "spectra");
            	    spectra2.addProperty("itemType", "org.iupac.fairdata.contrib.fairspec.dataobject.nmr.FAIRSpecNMRData");
            	    spectra2.addProperty("itemTypeExtends", "org.iupac.fairdata.contrib.fairspec.dataobject.FAIRSpecDataObject;org.iupac.fairdata.dataobject.IFDDataObject;org.iupac.fairdata.core.IFDRepresentableObject");
            	    JsonObject itemsbyid3=new JsonObject();
            	    spectra2.add("itemsByID",itemsbyid3);
            	    int i=1;
            	    for (DBSpectrum spectrum2 : spectrum.getDBMolecule().selectSpectra()) {
	            	    JsonObject one2=new JsonObject();
	            	    spectra2.add("["+i+"]", one2);
	            	    one2.addProperty("id","["+i+"]");
	            	    one2.addProperty("label", spectrum2.getMoleculeId()+"."+spectrum2.getDBSpectrumType().getNameAsString());
	            	    one2.addProperty("description", spectrum2.getRawData()==null ? "NMR peak list" : "Primary NMR Data from spectrometer");
	            	    one2.addProperty("propertyPrefix", "IFD.property.dataobject.fairspec");
	            	    JsonObject properties = new JsonObject();
	            	    one2.add("properties",properties);
	            	    properties.addProperty("nmr.expt_description", spectrum2.getDBSpectrumType().getDimensionality()+"D");
	            	    properties.addProperty("nmr.expt_nucl1", spectrum2.getDBSpectrumType().getDBIsotope(1).getAtomicMass()+spectrum2.getDBSpectrumType().getDBIsotope(1).getElementSymbol());
	            	    if(spectrum2.getDBSpectrumType().getDimensionality()==2)
	            	    	properties.addProperty("nmr.expt_nucl2", spectrum2.getDBSpectrumType().getDBIsotope(2).getAtomicMass()+spectrum2.getDBSpectrumType().getDBIsotope(2).getElementSymbol());
	            	    properties.addProperty("nmr.expt_solvent", spectrum2.getSolvent());
	            	    one2.addProperty("exptMethod","NMR");
	            	    JsonArray representations2=new JsonArray();
	            	    one2.add("representations", representations2);
	            	    JsonObject jcamp=new JsonObject();
	            	    representations2.add(jcamp);
	            	    jcamp.addProperty("ifdType","org.iupac.fairdata.contrib.fairspec.dataobject.nmr.FAIRSpecNMRDataRepresentation");
	            	    jcamp.addProperty("ifdTypeExtends", "org.iupac.fairdata.dataobject.IFDDataObjectRepresentation;org.iupac.fairdata.core.IFDRepresentation");
	            	    jcamp.addProperty("representationType","IFD.property.dataobject.fairspec.nmr");
	            	    jcamp.addProperty("mediaType","chemical/x-jcamp-dx");
	            	    JsonObject ref4=new JsonObject();
	            	    jcamp.add("ref", ref4);
	            	    if(format.equals("findingaid")) {
		            	    ref4.addProperty("ifdType", "org.iupac.fairdata.core.IFDReference");
		            	    if(spectrum2.getJcamp()!=null)
		            	    	ref4.addProperty("url", new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum2.getSpectrumId()+".jdx?spectrumid="+spectrum2.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=jcamp");
		            	    else
		            	    	ref4.addProperty("url", new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum2.getSpectrumId()+".jdx?spectrumid="+spectrum2.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=jcamppeaks");
		            	    if(spectrum2.getJcamp()!=null)
		            	    	jcamp.addProperty("len", new String(spectrum2.getJcamp()).length());
		            	    else
		            	    	jcamp.addProperty("len", spectrum2.getPeakJcamp().length());
		            	    ref4.addProperty("localName",spectrum2.getSpectrumId()+".jdx");
	            	    } else {
		            	    ref4.addProperty("ifdType", "org.iupac.fairdata.core.IFDRepresentation");
	                        byte[] fileContent;
		            	    if(spectrum2.getJcamp()!=null)
		            	    	fileContent=spectrum2.getJcamp();
		            	    else
		            	    	fileContent=spectrum2.getPeakJcamp().getBytes();
	                        byte[] encoded = new Base64().encode(fileContent);
	            	    	ref4.addProperty("data", ";base64,"+new String(encoded));
	            	    	jcamp.addProperty("len", encoded.length );
	            	    }
	            	    if(spectrum2.getRawDataDecoded()!=null) {
		            	    JsonObject rawdata=new JsonObject();
		            	    representations2.add(rawdata);
		            	    rawdata.addProperty("ifdType","org.iupac.fairdata.contrib.fairspec.dataobject.nmr.FAIRSpecNMRDataRepresentation");
		            	    rawdata.addProperty("ifdTypeExtends", "org.iupac.fairdata.dataobject.IFDDataObjectRepresentation;org.iupac.fairdata.core.IFDRepresentation");
		            	    rawdata.addProperty("representationType","IFD.property.dataobject.fairspec.nmr");
		            	    rawdata.addProperty("mediaType","application/zip");
		            	    JsonObject ref5=new JsonObject();
		            	    rawdata.add("ref", ref5);
		            	    if(format.equals("findingaid")) {
			            	    ref5.addProperty("ifdType", "org.iupac.fairdata.core.IFDReference");
		            	    	ref5.addProperty("url", new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum2.getSpectrumId()+"zip?spectrumid="+spectrum2.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=rawdata");
			            	    rawdata.addProperty("len", new String(spectrum2.getRawDataDecoded()).length());
			            	    ref5.addProperty("localName",spectrum2.getSpectrumId()+".jdx");
		            	    }else {
			            	    ref5.addProperty("ifdType", "org.iupac.fairdata.core.IFDRepresentation");
		                        byte[] encoded = spectrum2.getRawData();
		            	    	ref5.addProperty("data", ";base64,"+new String(encoded));
		            	    	rawdata.addProperty("len", encoded.length );		            	    	
		            	    }
	            	    }
	            	    JsonObject pdf=new JsonObject();
	            	    representations2.add(pdf);
	            	    pdf.addProperty("ifdType","org.iupac.fairdata.contrib.fairspec.dataobject.nmr.FAIRSpecNMRDataRepresentation");
	            	    pdf.addProperty("ifdTypeExtends", "org.iupac.fairdata.dataobject.IFDDataObjectRepresentation;org.iupac.fairdata.core.IFDRepresentation");
	            	    pdf.addProperty("representationType","IFD.property.dataobject.fairspec.nmr");
	            	    pdf.addProperty("mediaType","application/pdf");
	            	    JsonObject ref6=new JsonObject();
	            	    pdf.add("ref", ref6);
	            	    if(format.equals("findingaid")) {
		            	    ref6.addProperty("ifdType", "org.iupac.fairdata.core.IFDReference");
		            	    if(spectrum2.getPdf()!=null)
		            	    	ref6.addProperty("url", new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum2.getSpectrumId()+".pdf?spectrumid="+spectrum2.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=pdforig");
		            	    else
		            	    	ref6.addProperty("url", new UrlTool().getBaseUrl(req)+"/../download/NmrshiftdbServlet/"+spectrum2.getSpectrumId()+".pdf?spectrumid="+spectrum2.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=pdf");
		            	    if(spectrum2.getPdf()!=null)
		            	    	pdf.addProperty("len", new String(spectrum2.getPdfDecoded()).length());
		            	    ref6.addProperty("localName",spectrum2.getSpectrumId()+".pdf");
	            	    }else {
		            	    ref6.addProperty("ifdType", "org.iupac.fairdata.core.IFDRepresentation");
	            	    	if(spectrum2.getPdf()!=null) {
	            	    		byte[] encoded = spectrum2.getPdf();
	            	    		ref6.addProperty("data", new String(encoded));
	            	    		pdf.addProperty("len", encoded.length );
	            	    	}else {
	                            String imagefile =  export.getImage(false,"pdf",relativepath + "/tmp/" + System.currentTimeMillis(),600,400,true,null,null,null);
	                            byte[] fileContent = Files.readAllBytes(new File(imagefile).toPath());
	                            byte[] encoded = new Base64().encode(fileContent);
	                	    	ref6.addProperty("data", ";base64,"+new String(encoded));
	                    	    pdf.addProperty("len", encoded.length );
	            	    	}
	            	    }
	            	    i++;
            	    }
               	    out.write(jsonroot.toString());
   	            }
                if(format.equals("cmlboth")){
                    Element cmlElement = new Element("cml");
                    cmlElement.addAttribute(new Attribute("convention","nmrshiftdb-convention"));
                    cmlElement.setNamespaceURI("http://www.xml-cml.org/schema");
                    Element parent;
                    if(parameterParser.getString("spectrumid").equals("subdata")){
                        SubmittingData subData=((SubmittingData) req.getSession().getAttribute("subData"));
                        CMLCml cml=new CMLCml();
                        cml.appendChild(subData.getCMLMolecule());
                        parent=cml;
                    }else{
                        parent=spectrum.getDBMolecule().getCML(1, spectrum.getDBSpectrumType().getName().equals("1H"));
                    }  
                    nu.xom.Node cmldoc = parent.getChild(0);
                    ((Element)cmldoc).setNamespaceURI("http://www.xml-cml.org/schema");
                    parent.removeChildren();
                    cmlElement.appendChild(cmldoc);
                    Element parentspec=spectrum.getCmlSpect(false, new UrlTool().getBaseUrl(req)+"/../");
                    Node spectrumel=parentspec.getChild(0);
                    parentspec.removeChildren();
                    cmlElement.appendChild(spectrumel);
                    ((Element)spectrumel).setNamespaceURI("http://www.xml-cml.org/schema");
                    ByteArrayOutputStream outlocal = new ByteArrayOutputStream();
                    Serializer ser = new Serializer(outlocal, "ISO-8859-1");
                    ser.setIndent(1);
                    ser.write(new Document(cmlElement));
                    out.write(outlocal.toString());
                }
                if(format.indexOf("exsection")==0){
                    StringTokenizer st=new StringTokenizer(format,"-");
                    st.nextToken();
                    String template=st.nextToken();
                    Criteria crit=new Criteria();
                    crit.add(DBSpectrumPeer.USER_ID,spectrum.getUserId());
                    //handle the export template
                    VelocityContext context = new VelocityContext();
                    SubmittingData subData = ((SubmittingData) req.getSession().getAttribute("subData"));
                    context.put("spectrum", specfileFromSubdata!=null ? subData.toDBSpectrum() : spectrum);
                    context.put("formula", specfileFromSubdata!=null ? subData.getMolecularFormula(false) : spectrum.getDBMolecule().getMolecularFormula(false));
                    context.put("weight", specfileFromSubdata!=null ? subData.getMolecularWeight() : spectrum.getDBMolecule().getMolecularWeight());
                    // lets render the template
                    StringWriter w = new StringWriter();
                    Velocity.mergeTemplate("exporttemplates/"+template, "ISO-8859-1", context, w);
                    out.write(w.toString());
                }
            }else if (action.equals("exportmol")) {
				int width=-1;
				int height=-1;
				if(parameterParser.getString("size")!=null){
					String size = parameterParser.getString("size");
					if(size.equals("large")){
						width=350;
						height=250;
					}else if(size.equals("small")){
						width=150;
						height=150;                    		
					}else{
						StringTokenizer st=new StringTokenizer(size,"x");
						width=Integer.parseInt(st.nextToken());
						height=Integer.parseInt(st.nextToken());
					}
				}
                boolean shownumbers=true;
                //if image is small, never use atom numbers 
                if((parameterParser.getString("size")!=null && parameterParser.getString("size").equals("small")) || (parameterParser.getString("shownumbers")!=null && parameterParser.getString("shownumbers").equals("false"))){
                    shownumbers=false;
                }
                if(parameterParser.getString("backcolor")!=null){
                    export.backColor=new Color(Integer.parseInt(parameterParser.getString("backcolor")));
                }
                if(parameterParser.getString("markatom")!=null){
                    export.selected=Integer.parseInt(parameterParser.getString("markatom"))-1;
                }
                if(parameterParser.getString("compact")!=null){
                    export.setCompact(true);
                }
                //User requested svg
                if (format.equals("svg")) {
                    out.print(export.getMolSvg(true));
                }
                if(format.equals("tiff")||format.equals("jpeg")||format.equals("png")){                	
                	Map<String,List<IAtom>> equivalencesets=null;
                	if(parameterParser.getString("equivalences", "").equals("true")) {
                		equivalencesets=new HashMap<>();
                		for(int i=0;i<molecule.getAtomsAsArrayInMdlOrder().length;i++) {
                			DBAtom atom=molecule.getAtomsAsArrayInMdlOrder()[i];
                			if(atom.getIsVisible().equals(NmrshiftdbConstants.TRUE) && atom.getEquivalenceNumber()>0) {
                				if(!equivalencesets.containsKey(atom.getEquivalenceNumber()+""))
                					equivalencesets.put(atom.getEquivalenceNumber()+"",new ArrayList<IAtom>());
                				equivalencesets.get(atom.getEquivalenceNumber()+"").add(molecule.getAsCDKMolecule(1).getAtom(i));
                			}
                		}
                	}
                    InputStream is =  new FileInputStream(export.getImage(true,format,relativepath + "/tmp/" + System.currentTimeMillis(),width,height,shownumbers,null,null,null,Color.BLUE, null, null, null, null, equivalencesets==null ? null : equivalencesets.values(), null));
                    byte[] buf = new byte[32 * 1024]; // 32k buffer
                    int nRead = 0;
                    FileOutputStream fos = null;
                    if(!parameterParser.getString("spectrumid","").equals("subdata") && !parameterParser.getString("moleculeid","").equals("subdata") && UrlTool.doImageCache() && molecule.isPublic() && format.equals("jpeg") && parameterParser.getString("size")!=null && (parameterParser.getString("size").equals("small") || parameterParser.getString("size").equals("large")) && (spectrum==null || !spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE))){
                        if(parameterParser.getString("moleculeid").indexOf('_')>0)
                            fos = new FileOutputStream(new File(ServletUtils.expandRelative(this.getServletConfig(), "/images/molecules/"+parameterParser.getString("size")+"/"+spectrum.getMoleculeId()+"_"+spectrum.getSpectrumId()+".jpeg")));
                        else
                            fos = new FileOutputStream(new File(ServletUtils.expandRelative(this.getServletConfig(), "/images/molecules/"+parameterParser.getString("size")+"/"+molecule.getMoleculeId()+".jpeg")));
                    }
                    while( (nRead=is.read(buf)) != -1 ) {
                        outstream.write(buf, 0, nRead);
                        if(fos!=null)
                        	fos.write(buf, 0, nRead);
                    }
                    if(fos!=null){
                    	fos.close();
                    }
                    if(is!=null){
                    	is.close();
                    }
                }
                if(format.equals("mdl")){
                	if(spectrum==null){
                		out.println(export.getMdl());
                	}else{
                		out.println(spectrum.getDBMolecule().getStructureFile(1,null,false,true));
                	}
                }
                if(format.equals("cml")){
                	if(spectrum==null){
                		out.println(export.getCml());
                	}else{
                		out.println(spectrum.getDBMolecule().getCMLString(1));
                	}
                }
            }else if(action.equals("exportdataset")){
            	if(format.equals("nmredata.zip")){
	            	String solvent=spectrum.getSolvent();
	            	String keyword=spectrum.getKeywords();
	            	List<File> files=new ArrayList<File>();
	            	int i=10;
	            	List<String> locations=new ArrayList<String>();
	                for(DBSpectrum spectrumlocal : spectrum.getDBMolecule().selectSpectra()){
	                	if(spectrumlocal.getRawData()!=null && spectrumlocal.getSolvent().equals(solvent) && spectrumlocal.getKeywords().equals(keyword)){
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
	                		locations.add("file:./"+spectrum.getDBMolecule().getMoleculeId()+"/"+i);
	                		i++;
	                	}
	                }
	            	NmreDataRecord record=new NmreDataRecord(files, spectrum.getDBMolecule().getNmreData(spectrum, req, locations, true, false, spectrum.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, new UrlTool().getBaseUrl(req)+"/../")));
	            	NmredataRecordWriter writer=new NmredataRecordWriter(outstream);
	                writer.write(record, spectrum.getDBMolecule().getMoleculeId()+"", NmredataVersion.ONEPOINTONE);
            	}else if(format.equals("cml")){
                    Element cmlElement = new Element("cml");
                    cmlElement.addAttribute(new Attribute("convention","nmrshiftdb-convention"));
                    cmlElement.setNamespaceURI("http://www.xml-cml.org/schema");
                    Element parent=spectrum.getDBMolecule().getCML(1, spectrum.getDBSpectrumType().getName().equals("1H"));
                    nu.xom.Node cmldoc = parent.getChild(0);
                    ((Element)cmldoc).setNamespaceURI("http://www.xml-cml.org/schema");
                    parent.removeChildren();
                    cmlElement.appendChild(cmldoc);
                    List<DBSpectrum> spectra = spectrum.getDBMolecule().selectSpectra();
	                for(DBSpectrum spectrumlocal : spectra){
	                	if(spectrumlocal.getSolvent().equals(spectrum.getSolvent()) && spectrumlocal.getKeywords().equals(spectrum.getKeywords())){
		                    Element parentspec=spectrumlocal.getCmlSpect(false, new UrlTool().getBaseUrl(req)+"/../");
		                    Node spectrumel=parentspec.getChild(0);
		                    parentspec.removeChildren();
		                    cmlElement.appendChild(spectrumel);
		                    ((Element)spectrumel).setNamespaceURI("http://www.xml-cml.org/schema");
	                	}
	                }
                    ByteArrayOutputStream outlocal = new ByteArrayOutputStream();
                    Serializer ser = new Serializer(outlocal, "ISO-8859-1");
                    ser.setIndent(1);
                    ser.write(new Document(cmlElement));
                    out.write(outlocal.toString());
            	}else if(format.equals("nmredata.sd")){
            		NmreData data = spectrum.getDBMolecule().getNmreData(spectrum, req, null, true, false, spectrum.getUrlForSpectrumFile(DBSpectrum.RAW_DATE_TYPE, new UrlTool().getBaseUrl(req)+"/../"));
                	NmredataWriter writer=new NmredataWriter(out);
                    writer.write(data, NmredataVersion.ONEPOINTONE);
                    writer.close();
            	}else if(format.equals("pdf")){
                    res.setContentType("application/pdf");
                    com.lowagie.text.Document document = new com.lowagie.text.Document();
                    ByteArrayOutputStream baos=new ByteArrayOutputStream(1024*1024);
                    PdfWriter.getInstance(document, baos);
                    ReportUtil.createFullReport(document, this.getServletConfig(), req.getSession(), req, spectrum.getDBMolecule(), spectrum.getSolvent(), spectrum.getKeywords(), GeneralUtils.getPredictionLimits("13C"), GeneralUtils.getPredictionLimits("1H"));
                    document.close();
                    outstream.write(baos.toByteArray());
            	}
            }else if (action.equals("showmoldetails")) {
				  res.setContentType("text/html");
				  out = res.getWriter();
				  VelocityContext context = new VelocityContext();
				  context.put("mol", molecule);
		          context.put("image", "download/NmrshiftdbServlet/"+molecule.getMoleculeId()+".jpg;jsessionid="+req.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+ molecule.getMoleculeId() +"&format=jpeg&size=350x250&backcolor=12632256");
		          context.put("message","");
		          context.put("genutil", GeneralUtils.getInstance());
	  	          // lets render the template
	  	          StringWriter w = new StringWriter();
	  	          Velocity.mergeTemplate("detailsstruconly.vm", "ISO-8859-1", context, w);
	  	          out.println(w.toString());
			}
            if(out!=null)
                out.flush();
            else if(outstream!=null)
                outstream.flush();
        } 
        catch (Exception ex) {
            ex.printStackTrace();
            out.print(GeneralUtils.logError(ex,"NmrshiftdbServlet",null,true));
            out.flush();
        }
    }
    
    private void write2d(String attributename, double freq, HttpSession session, NmreData data, String type) throws JCAMPException {
    	if(session.getAttribute(attributename)==null)
    		return;
        Unit xUnit =  CommonUnit.hertz;
        Unit yUnit =  CommonUnit.hertz;
        Unit zUnit = CommonUnit.intensity;
    	double reference = 0;
    	Peak2D[] peaks2d = ((List<Peak2D>)session.getAttribute(attributename)).toArray(new Peak2D[((List<Peak2D>)session.getAttribute(attributename)).size()]);
        double[][] xy=new double[0][];
        IOrderedDataArray1D x = new OrderedArrayData(new double[0], xUnit);
        IOrderedDataArray1D y = new OrderedArrayData(new double[0], xUnit);
        IDataArray1D z = new ArrayData(new double[0], yUnit);
        if(peaks2d.length>0){
	        xy = NmredataReader.peakTableToPeakSpectrum(peaks2d);
	        x = new OrderedArrayData(xy[0], xUnit);
	        y = new OrderedArrayData(xy[1], yUnit);
	        z = new ArrayData(xy[2], zUnit);
        }
    	NMR2DSpectrum cosy = new NMR2DSpectrum(x, y, z, new String[] {"1H","1H"}, new double[] { freq, freq}, new double[] {reference,reference});
    	cosy.setPeakTable(peaks2d);
        data.addSpectrum(cosy);      
        NoteDescriptor noteDescriptor=new NoteDescriptor("Spectrum_Location");
		String location="none";
		cosy.setNote(noteDescriptor, location);		
        noteDescriptor=new NoteDescriptor("CorType");
		cosy.setNote(noteDescriptor, type);
	}


	/**
     * Writes a rss cml feed to res.
     * 
     * @param res                The response to write to.
     * @param query              The molecules to write (query must return a list of MoleculeIds).
     * @param numbertoexport     The number of mols to export.
     * @param req                The reqeust to answer.
     * @throws IOException
     * @throws CDKException
     * @throws TorqueException
     * @throws CMLException
     * @throws DataSetException
     * @throws NmrshiftdbException 
     */
    private void writeRSS(HttpServletResponse res, String query, int numbertoexport, HttpServletRequest req) throws IOException, CDKException, TorqueException, DataSetException, NmrshiftdbException{
        res.setContentType("text/xml");
        RssWriter rssWriter=new RssWriter();
        rssWriter.setWriter(res.getWriter());
        AtomContainerSet soac=new AtomContainerSet();
        List<Record> l=NmrshiftdbUserPeer.executeQuery(query);
        for(int i=0;i<numbertoexport;i++){
            if(i==l.size())
                break;
            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asInt()));
            IAtomContainer cdkmol=mol.getAsCDKMoleculeAsEntered(1,false);
            soac.addAtomContainer(cdkmol);
            rssWriter.getLinkmap().put(cdkmol,mol.getEasylink(req));
            rssWriter.getDatemap().put(cdkmol,mol.getDate());
            rssWriter.getTitlemap().put(cdkmol,mol.getChemicalNamesAsOneStringWithFallback());
            rssWriter.getCreatormap().put(cdkmol,mol.getNmrshiftdbUser().getLoginName());
            rssWriter.setCreator(GeneralUtils.getAdminEmail(getServletConfig()));
            List<DBCanonicalName> v = mol.getDBCanonicalNames();
            for (int k = 0; k < v.size(); k++) {
                DBCanonicalName canonName = (DBCanonicalName)v.get(k);
                if (canonName.getDBCanonicalNameType().getCanonicalNameType()=="INChI") {
                    rssWriter.getInchimap().put(cdkmol,canonName.getName());
                    break;
                }
            }
            rssWriter.setTitle("nmrshiftdb2");
            rssWriter.setLink("http://www.nmrshiftdb.org");
            rssWriter.setDescription("nmrshiftdb2 is an open-source, open-access, open-submission, open-content web database for chemical structures and their nuclear magnetic resonance data");
            rssWriter.setPublisher("nmrshiftdb.org");
            rssWriter.setImagelink("http://www.nmrshiftdb.org/images/nmrshift-logo.gif");
            rssWriter.setAbout("http://www.nmrshiftdb.org/NmrshiftdbServlet?nmrshiftdbaction=rss");
            Collection<Element> coll=new ArrayList<Element>();
            List<DBSpectrum> spectra=mol.selectSpectra(0);
            for(int k=0;k<spectra.size();k++){
                Element el=((DBSpectrum)spectra.get(k)).getCmlSpect(false, new UrlTool().getBaseUrl(req)+"/../");
                Element el2=el.getChildElements().get(0);
                el.removeChild(el2);
                coll.add(el2);
            }
            rssWriter.getMultiMap().put(cdkmol,coll);
        }
        rssWriter.write(soac);
        rssWriter.close();
    }
}

