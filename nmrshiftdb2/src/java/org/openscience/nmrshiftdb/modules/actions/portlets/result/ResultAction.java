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
package org.openscience.nmrshiftdb.modules.actions.portlets.result;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.vecmath.Point2d;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.context.Context;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.modules.actions.portlets.search.SearchConditionAction;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.om.DBExperiment;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBKeyword;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBLabGroup;
import org.openscience.nmrshiftdb.om.DBLabGroupPeer;
import org.openscience.nmrshiftdb.om.DBMixture;
import org.openscience.nmrshiftdb.om.DBMixturePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculeDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.DBSubstance;
import org.openscience.nmrshiftdb.om.DBSubstancePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.SearchUtils;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.VectorWithSessionBinding;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.workingdogs.village.Record;

/**
 * This is the action for handling searches
 *
 * @author     Stefan Kuhn
 * @created    12. Februar 2002
 */
public class ResultAction extends VelocityPortletAction {
    //The number of molecules displayed per result page (must be an even figure!)
    private final int MOLECULES_PER_PAGE = 10;
    //The number of pages for which links appear in the browsing line
    private final int PAGES_PER_BROWSE_LINE = 10;
    public static boolean structcent=false;
    public static final int LABGROUP_BACKGROUND_COLOR=12632256;
    public static final int STANDARD_BACKGROUND_COLOR=14737632;

    /**
     * Builds the context for the portlet.
     *
     * @param  runData  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
     */
    @Override
    protected void buildNormalContext(VelocityPortlet portlet, Context context,
            RunData runData) {
        try{
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //the results.vm template is the default template
            if(!context.containsKey("template"))
                context.put("template","result/results");
            String action = runData.getParameters().getString("nmrshiftdbaction");
            Integer browseBefore=new Integer(-1);
            Integer browseAfter=new Integer(-1);
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            if (action == null) {
                action = "";
            }
            if(action.equals("submitfromlabjournal")){
                try{
                    ActionLoader.getInstance().exec(runData, "LoginUser");
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                runData.getResponse().getOutputStream().print("<head><meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=NMR+lab+administration;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=submitfromlabjournal&submitorshow=submit&other="+URLEncoder.encode(runData.getParameters().getString("other", ""))+"&othernuc="+URLEncoder.encode(runData.getParameters().getString("othernuc",""))+"&care="+URLEncoder.encode(runData.getParameters().getString("care",""))+"&type="+URLEncoder.encode(runData.getParameters().getString("type",""))+"&strength="+URLEncoder.encode(runData.getParameters().getString("machine",""))+"&solvent="+URLEncoder.encode(runData.getParameters().getString("solvent",""))+"&usersid="+URLEncoder.encode(runData.getParameters().getString("usersid",""))+"&usersidcomment="+URLEncoder.encode(runData.getParameters().getString("usersidcomment",""))+"&process="+URLEncoder.encode(runData.getParameters().getString("process",""))+"&probablestructure="+URLEncoder.encode(runData.getParameters().getString("probablestructure",""))+"\"></head><a href=\"\">If you are not redirected, please click here</a>");
            }
            //The molecule displaying starts with 0 if new search
            int startMolecule = 0;
            String header = "";
            if(session.getAttribute("startmolecule")!=null)
                startMolecule=((Integer)session.getAttribute("startmolecule")).intValue();
            //Not yet done anything
            if (action.equals("") || action.equals("showDetailsFromHome") || action.equals("showrandom") || action.equals("submitforreview") || action.equals("publish") || action.equals("showprivatebykey") && action.equals("submitforreviewdataset")) {
                if(session.getAttribute("spectrumassignment")!=null)
                    context.put("showassignment","true");
                else if(session.getAttribute("mol")!=null)
                    context.put("showpredict","true");
                if(results==null && !context.get("template").equals("result/message")){
                    context.put("template","result/nosearch");
                }
            }
            boolean onlyassigned=false;
            if(runData.getSession().getAttribute("assignedonly")!=null && ((Boolean)runData.getSession().getAttribute("assignedonly")).booleanValue())
                onlyassigned=true;
            //This is for direct browsing
            if(action.equals("showDetails")){
                if(results==null)
                    results=new ArrayList<String>();
                if(!runData.getParameters().getString("molNumber").equals("")){
                    try {
                        int molNumber = new Integer(runData.getParameters().getString("molNumber")).intValue();
                        if(molNumber - 1 >-1)
                            browseBefore=new Integer(molNumber-1);
                        if(molNumber+1 < MOLECULES_PER_PAGE && startMolecule + molNumber+1 < results.size())
                            browseAfter=new Integer(molNumber+1);
                        else
                            browseAfter=new Integer(-2);
                    } catch (NumberFormatException ex) {
                        GeneralUtils.logError(ex,"building browse in result",runData,true);
                    }
                }
            }
            StringBuffer sql=new StringBuffer();
            if(!action.equals(NmrshiftdbConstants.BROWSE) && !action.equals("showDetails") && !action.equals(NmrshiftdbConstants.BROWSESPEC)){
                session.removeAttribute("browseallall");
            }
            //Browse everything
            if(action.equals(NmrshiftdbConstants.BROWSE) || (((String)runData.getSession().getAttribute("browseallall")) !=null && ((String)runData.getSession().getAttribute("browseallall")).equals("mol"))){
                try{
                    session.removeAttribute("startmolecule");
                    startMolecule=0;
                    @SuppressWarnings("unchecked")
                    ArrayList<DBSpectrumType> types=(ArrayList<DBSpectrumType>)runData.getSession().getAttribute("choosenspectrumtypes");
                    boolean measuredonly=session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null;
                    boolean calculatedonly=session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null;
                    boolean jcamponly=false;
                    if (runData.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY)).booleanValue()==true) {
                        jcamponly=true;
                    }
                    boolean rawonly=false;
                    if (runData.getSession().getAttribute(NmrshiftdbConstants.RAWONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.RAWONLY)).booleanValue()==true) {
                    	rawonly=true;
                    }
                    String start=runData.getParameters().getString("startbrowseall");
                    sql.append("select DISTINCT MOLECULE.MOLECULE_ID from MOLECULE");
                    if(onlyassigned || calculatedonly || measuredonly || types!=null){
                        sql.append(", SPECTRUM ");
                        if(jcamponly || rawonly){
                            sql.append("left join SPECTRUM_FILE using (SPECTRUM_ID)");
                        }
                    }
                    if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                        sql.append(" join TURBINE_USER using (USER_ID) join USER_HISTORY on TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and LEAVE_DATE is null");
                    }
                    if (calculatedonly || measuredonly || types!=null) {
                        sql.append(", SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE ");
                    }
                    sql.append(" where ");
                    if(onlyassigned ||calculatedonly || measuredonly || types!=null) {
                        sql.append("MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID and (SPECTRUM.REVIEW_FLAG='true' ");
                    }
                    if(!runData.getUser().getUserName().equals("anon")){
                        sql.append("or (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+((BaseJetspeedUser)runData.getUser()).getUserId()+") ");
                    }
                    if(onlyassigned ||calculatedonly || measuredonly || types!=null) {
                        sql.append(") and ");
                    }
                    if(types!=null && onlyassigned){
                        sql.append("(");
                        for(int i=0;i<types.size();i++){
                            if(((DBSpectrumType)types.get(i)).getChoosen())
                                sql.append(" SPECTRUM.SPECTRUM_TYPE_ID="+((DBSpectrumType)types.get(i)).getSpectrumTypeId()+" or ");
                        }
                        sql=new StringBuffer(sql.toString().substring(0,sql.length()-4));
                        sql.append(") and ");
                    }
                    if(start!=null){
                        sql.append("MOLECULE.MOLECULE_ID>="+start+" and ");
                    }
                    if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                        sql.append("LABGROUP_ID>0 and ");
                    }	
                    if (measuredonly || calculatedonly || types!=null) {
                        sql.append("SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (");
                        if (measuredonly) {
                            sql.append(" CONDITION_TYPE. CONDITION_TYPE='m'");
                        }
                        if (calculatedonly && measuredonly) {
                            sql.append(" or");
                        }
                        if (calculatedonly) {
                            sql.append(" CONDITION_TYPE. CONDITION_TYPE='c'");
                        }
                        if(!calculatedonly && !measuredonly)
                            sql.append("true");
                        sql.append(" ) and ");
                    }
                    if(jcamponly){
                        sql.append("(SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"') and ");
                    }
                    if(rawonly){
                        sql.append("(SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') and ");
                    }
                    sql.append(" true order by MOLECULE.MOLECULE_ID;");
                    //And we do the search
                    results=new ArrayList<String>();
                    List<Record> l= DBMoleculePeer.executeQuery(sql.toString());
                    for(int i=0;i<l.size();i++){
                        results.add(((Record) l.get(i)).getValue(1).asString());
                        if(i>MOLECULES_PER_PAGE-2)
                            break;
                    }
                    if(l.size()==0){
                        context.put("message","There are no "+(measuredonly ? "measured" : "")+" "+(calculatedonly ? "calculated" : "")+" spectra of choosen types!");
                        context.put("template","result/message");
                        return;
                    }
                    session.setAttribute("browseallall","mol");
                    session.setAttribute("results", results);
                } catch (Exception ex) {
                    context.put("message",GeneralUtils.logError(ex, "ResultPortlet/browseall", runData,true));
                    context.put("template","result/message");
                    return;
                }
            }
            if(action.equals(NmrshiftdbConstants.BROWSESPEC) || (((String)runData.getSession().getAttribute("browseallall")) !=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec"))){
                try{        	
                    @SuppressWarnings("unchecked")
                    ArrayList<DBSpectrumType> types=(ArrayList<DBSpectrumType>)runData.getSession().getAttribute("choosenspectrumtypes");
                    boolean measuredonly=session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null;
                    boolean calculatedonly=session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null;
                    String start=runData.getParameters().getString("startbrowseall");
                    sql=new StringBuffer();
                    sql.append("select DISTINCT SPECTRUM.SPECTRUM_ID from SPECTRUM");
                    if(onlyassigned)
                        sql.append(",MOLECULE");
                    if (calculatedonly || measuredonly) {
                        sql.append(", SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE ");
                    }
                    sql.append(" where (SPECTRUM.REVIEW_FLAG='true' ");
                    if(!runData.getUser().getUserName().equals("anon")){
                        sql.append("or (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+((BaseJetspeedUser)runData.getUser()).getUserId()+") ");
                    }
                    sql.append(") and ");
                    if(types!=null){
                        sql.append("(");
                        for(int i=0;i<types.size();i++){
                            if(((DBSpectrumType)types.get(i)).getChoosen())
                                sql.append(" SPECTRUM.SPECTRUM_TYPE_ID="+((DBSpectrumType)types.get(i)).getSpectrumTypeId()+" or ");
                        }
                        sql=new StringBuffer(sql.toString().substring(0,sql.toString().length()-4));
                        sql.append(") and ");
                    }
                    if(start!=null){
                        sql.append(" SPECTRUM.SPECTRUM_ID>="+start);
                    }
                    if(onlyassigned)
                        sql.append(" and SPECTRUM.MOLECULE_ID=MOLECULE.MOLECULE_ID");
                    if (measuredonly || calculatedonly) {
                        sql.append(" and SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDTION_TYPE_ID and (");
                        if (measuredonly) {
                            sql.append(" CONDITION_TYPE.CONDITION_TYPE='m'");
                        }
                        if (calculatedonly && measuredonly) {
                            sql.append(" or");
                        }
                        if (calculatedonly) {
                            sql.append(" CONDITION_TYPE.CONDITION_TYPE='c'");
                        }
                        sql.append(" ) ");
                    }
                    sql.append(" order by SPECTRUM.SPECTRUM_ID");
                    //And we do the search
                    results=new ArrayList<String>();
                    List<Record> l= DBSpectrumPeer.executeQuery(sql.toString());
                    for(int i=0;i<l.size();i++){
                        results.add(((Record) l.get(i)).getValue(1).asString());
                        if(i>MOLECULES_PER_PAGE-2)
                            break;
                    }
                    session.setAttribute("browseallall","spec");
                    session.setAttribute("results", results);
                    if(l.size()==0){
                        context.put("message","There are no "+(measuredonly ? "measured" : "")+" "+(calculatedonly ? "calculated" : "")+" spectra of choosen types!");
                        context.put("template","result/message");
                        return;
                    }
                } catch (Exception ex) {
                    context.put("message",GeneralUtils.logError(ex, "ResultPortlet/browsespectra", runData,true));
                    context.put("template","result/message");
                    return;
                }
            }
            //This displays the results for all searches
            if(results!=null){
                context.put("numberofresults", "" + results.size());
                context.put("browseBefore",browseBefore);
                if(session.getAttribute("spectrumassignment")!=null)
                    context.put("showassignment","true");
                else if(session.getAttribute("mol")!=null)
                    context.put("showpredict","true");
                StringBuffer labgroups=new StringBuffer();
                Criteria crit= new Criteria();
                crit.add(DBLabGroupPeer.LABGROUP_ID,0,Criteria.GREATER_THAN);
                List<DBLabGroup> groups = DBLabGroupPeer.doSelect(crit);
                for(int i=0;i<groups.size();i++){
                    labgroups.append(((DBLabGroup)groups.get(i)).getLabgroupName());
                    labgroups.append("; ");
                }
                context.put("labgroup",GeneralUtils.removeLastComma(labgroups));
                if(results.size()>1 && browseAfter.intValue()==-1)
                    browseAfter=new Integer(1);
                context.put("browseAfter",browseAfter);
                if(results.size()==SearchUtils.MAX_HITS_TO_DISPLAY)
                    header="There were more than "+SearchUtils.MAX_HITS_TO_DISPLAY+" results. The rest was cut off !";
                context.put("header", header);
                StringBuffer browsingLine = new StringBuffer();
                float numberOfPages = new Float(results.size()).floatValue() / new Float(MOLECULES_PER_PAGE).floatValue();
                if ((int) numberOfPages != numberOfPages) {
                    numberOfPages = ((int) numberOfPages) + 1;
                }
                //Put together the line for browsing the pages
                String browsingLineStartWith = runData.getParameters().getString("browsingLineStartWith");
                if (browsingLineStartWith == null) {
                    browsingLineStartWith = "0";
                }
                int browsingLineStartWithInt = new Integer(browsingLineStartWith).intValue();
                int browsingLineEndWithInt = browsingLineStartWithInt + PAGES_PER_BROWSE_LINE;
                if (browsingLineEndWithInt > numberOfPages) {
                    browsingLineEndWithInt = (int) numberOfPages;
                }
                if (browsingLineStartWithInt > 0) {
                    browsingLine.append("<a href=\"portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?eventSubmit_doShownewpage=showNewPage&newmol=true&startMolecule=" + startMolecule + "&browsingLineStartWith=" + (browsingLineStartWithInt - PAGES_PER_BROWSE_LINE) + "\">&lt;&lt;</a>   ");
                }
                for (int i = browsingLineStartWithInt; i < browsingLineEndWithInt; i++) {
                    if (i == (startMolecule / MOLECULES_PER_PAGE)) {        browsingLine.append("<span style=\"color:red\"><b>" + (i + 1) + "<b></span>   ");
                    } else {
                        browsingLine.append("<a href=\"portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?eventSubmit_doShownewpage=showNewPage&newmol=true&startMolecule=" + i * MOLECULES_PER_PAGE + "&browsingLineStartWith=" + browsingLineStartWithInt + "\">" + (i + 1) + "</a>   ");
                    }
                }
                if (browsingLineEndWithInt < numberOfPages) {
                    browsingLine.append("<a href=\"portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?eventSubmit_doShownewpage=showNewPage&newmol=true&startMolecule=" + startMolecule + "&browsingLineStartWith=" + (browsingLineStartWithInt + PAGES_PER_BROWSE_LINE) + "\">&gt;&gt;</a>   ");
                }
                //Browsing line is different in case of browse all
                if(runData.getParameters().getString("startbrowseall")!=null){
                    try{
                        boolean measuredonly=session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null;
                        boolean calculatedonly=session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null;
                        @SuppressWarnings("unchecked")
                        List<DBSpectrumType> types=(ArrayList<DBSpectrumType>)runData.getSession().getAttribute("choosenspectrumtypes");
                        browsingLine=new StringBuffer();
                        String start=runData.getParameters().getString("startbrowseall");
                        if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                            sql=new StringBuffer("select DISTINCT SPECTRUM_ID from MOLECULE");
                        }else{
                            sql=new StringBuffer("select DISTINCT MOLECULE.MOLECULE_ID from MOLECULE");
                        }
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                            sql.append(" join TURBINE_USER using (USER_ID)");
                        }
                        sql.append(", SPECTRUM ");
                        boolean jcamponly=false;
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY)).booleanValue()==true) {
                            jcamponly=true;
                        }
                        boolean rawonly=false;
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.RAWONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.RAWONLY)).booleanValue()==true) {
                        	rawonly=true;
                        }
                        if(jcamponly || rawonly){
                            sql.append("left join SPECTRUM_FILE using (SPECTRUM_ID)");
                        }
                        if (calculatedonly || measuredonly) {
                            sql.append(", SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE ");
                        }
                        sql.append(" where MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID and SPECTRUM.REVIEW_FLAG='true' and (");
                        if(types==null){
                            sql.append("true or ");
                        }else{
                            for(int i=0;i<types.size();i++){
                                if(((DBSpectrumType)types.get(i)).getChoosen())
                                    sql.append(" SPECTRUM.SPECTRUM_TYPE_ID="+((DBSpectrumType)types.get(i)).getSpectrumTypeId()+" or ");
                            }
                        }
                        if(start!=null){
                            if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                                sql.append("false) and SPECTRUM_ID<"+start);
                            }else{
                                sql.append("false) and MOLECULE.MOLECULE_ID<"+start);
                            }
                        }
                        if(jcamponly){
                            sql.append(" and (SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"')");
                        }
                        if(rawonly){
                            sql.append("(SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') and ");
                        }
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                            sql.append(" and LABGROUP_ID>0");
                        }
                        if (measuredonly || calculatedonly) {
                            sql.append(" and SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (");
                            if (measuredonly) {
                                sql.append(" CONDITION_TYPE. CONDITION_TYPE='m'");
                            }
                            if (calculatedonly && measuredonly) {
                                sql.append(" or");
                            }
                            if (calculatedonly) {
                                sql.append(" CONDITION_TYPE. CONDITION_TYPE='c'");
                            }
                            if(!calculatedonly && !measuredonly)
                                sql.append("true");
                            sql.append(" ) ");
                        }
                        if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                            sql.append(" order by SPECTRUM_ID;");
                        }else{
                            sql.append(" order by MOLECULE.MOLECULE_ID;");
                        }
                        List<Record> l=DBMoleculePeer.executeQuery(sql.toString());
                        if(l.size()>1){
                            String newStart="";
                            for(int i=l.size()-1;i>=l.size()-MOLECULES_PER_PAGE;i--){
                                if(i<0)
                                    break;
                                newStart=((Record) l.get(i)).getValue(1).asString();
                            }
                            browsingLine.append("<a href=\"portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction="+(runData.getSession().getAttribute("browseallall")!=null && session.getAttribute("browseallall").equals("spec") ? NmrshiftdbConstants.BROWSESPEC : NmrshiftdbConstants.BROWSE)+"&startbrowseall=" + newStart + "\">&lt;&lt;</a>");
                        }
                        if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                            browsingLine.append("<span style=\"color:red\"><b>" + DBSpectrumPeer.retrieveByPK(new NumberKey(start)).getMoleculeId() + "<b></span> ");
                        }else{
                            browsingLine.append("<span style=\"color:red\"><b>" + DBMoleculePeer.retrieveByPK(new NumberKey(start)).getMoleculeId() + "<b></span> ");
                        }
                        if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                            sql=new StringBuffer("select DISTINCT SPECTRUM_ID from MOLECULE");
                        }else{
                            sql=new StringBuffer("select DISTINCT MOLECULE.MOLECULE_ID from MOLECULE");
                        }
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                            sql.append(" join TURBINE_USER using (USER_ID)");
                        }
                        sql.append(", SPECTRUM ");
                        if(jcamponly || rawonly){
                            sql.append("left join SPECTRUM_FILE using (SPECTRUM_ID)");
                        }
                        if (calculatedonly || measuredonly) {
                            sql.append(", SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE ");
                        }
                        sql.append(" where MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID and SPECTRUM.REVIEW_FLAG='true' and (");
                        if(types==null){
                            sql.append("true or ");
                        }else{
                            for(int i=0;i<types.size();i++){
                                if(((DBSpectrumType)types.get(i)).getChoosen())
                                    sql.append(" SPECTRUM.SPECTRUM_TYPE_ID="+((DBSpectrumType)types.get(i)).getSpectrumTypeId()+" or ");
                            }
                        }
                        if(start!=null){
                            if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                                sql.append("false) and SPECTRUM_ID>="+start);
                            }else{
                                sql.append("false) and MOLECULE.MOLECULE_ID>="+start);
                            }
                        }
                        if(jcamponly){
                            sql.append(" and (SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"')");
                        }
                        if (runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true) {
                            sql.append(" and LABGROUP_ID>0");
                        }
                        if (measuredonly || calculatedonly) {
                            sql.append(" and SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and (");
                            if (measuredonly) {
                                sql.append(" CONDITION_TYPE. CONDITION_TYPE='m'");
                            }
                            if (calculatedonly && measuredonly) {
                                sql.append(" or");
                            }
                            if (calculatedonly) {
                                sql.append(" CONDITION_TYPE. CONDITION_TYPE='c'");
                            }
                            if(!calculatedonly && !measuredonly)
                                sql.append("true");
                            sql.append(" ) ");
                        }
                        if(runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                            sql.append(" order by SPECTRUM_ID;");
                        }else{
                            sql.append(" order by MOLECULE.MOLECULE_ID;");
                        }
                        l=DBMoleculePeer.executeQuery(sql.toString());
                        if(l.size()>MOLECULES_PER_PAGE){
                            String newStart="";
                            for(int i=0;i<MOLECULES_PER_PAGE+1;i++){
                                if(i>=l.size())
                                    break;
                                newStart=((Record) l.get(i)).getValue(1).asString();
                            }
                            browsingLine.append("<a href=\"portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction="+(runData.getSession().getAttribute("browseallall")!=null && session.getAttribute("browseallall").equals("spec") ? NmrshiftdbConstants.BROWSESPEC : NmrshiftdbConstants.BROWSE)+"&startbrowseall=" + newStart + "\">&gt;&gt;</a>");
                        }
                        if(session.getAttribute("searchesV")==null)
                            session.setAttribute("searchesV", new ArrayList<ValuesForVelocityBean>());
                        context.put("start", "startbrowseall="+start);
                    }
                    catch(Exception ex){
                        GeneralUtils.logError(ex,"browsing line for browse all",runData,true);
                    }
                }
                context.put("browsingline", browsingLine);
                String type="All";
                if(session.getAttribute("selectedType")!=null)
                    type=(String)session.getAttribute("selectedType");
                context.put("type",type);
                context.put("data", runData);
                context.put("molnumber", runData.getParameters().getString("molNumber")!=null ? runData.getParameters().getString("molNumber") : "0");
                DecimalFormat format;
                FieldPosition f = new FieldPosition(0);
                @SuppressWarnings("unchecked")
                ArrayList<ValuesForVelocityBean> searchesV=(ArrayList<ValuesForVelocityBean>)session.getAttribute("searchesV");
                if(searchesV==null){
                    searchesV=new ArrayList<ValuesForVelocityBean>();
                    session.setAttribute("searchesV",searchesV);
                }
                ValuesForVelocityBean lastSearch;
                if(getSearches(session)==null){
                    lastSearch=new ValuesForVelocityBean();
                }else if(runData.getParameters().getString("startbrowseall")==null){
                    lastSearch = (ValuesForVelocityBean) session.getAttribute("lastsearch");
                }else{
                    if(runData.getParameters().getString("startbrowseall")!=null && runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                        lastSearch = new ValuesForVelocityBean(NmrshiftdbConstants.BROWSESPEC, "--", "--");
                    }else{
                        lastSearch = new ValuesForVelocityBean(NmrshiftdbConstants.BROWSE, "--", "--");
                    }
                    if(searchesV.size() == 0 || !((ValuesForVelocityBean)searchesV.get(searchesV.size()-1)).getDisplayText().equals(NmrshiftdbConstants.BROWSE)){
                        searchesV.clear();
                        searchesV.add(lastSearch);
                    }
                }
                if(runData.getParameters().getString("startbrowseall")!=null && runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("mol")){
                    session.setAttribute(NmrshiftdbConstants.SEARCH_TYPE, NmrshiftdbConstants.STRUCCENT);
                }
                if(runData.getParameters().getString("startbrowseall")!=null && runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec")){
                    session.setAttribute(NmrshiftdbConstants.SEARCH_TYPE, NmrshiftdbConstants.SPECCENT);
                }
                for (int i = startMolecule; i < MOLECULES_PER_PAGE + startMolecule; i++) {
                    if (i < results.size()) {
                        try {
                            String inscription = "";
                            DBMolecule mol=null;
                            DBSpectrum spec=null;
                            DBMixture mix=null;
                            DBSample sample=null;
                            DBSubstance sub=null;
                            if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.STRUCCENT)){
                                context.put("temp"+(i-startMolecule)+"link","portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&newmol=true&molNumber="+(i-startMolecule)+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : ""));
                                mol=DBMoleculePeer.retrieveByPK(new NumberKey(results.get(i)));
                                context.put("temp"+(i-startMolecule)+"file", mol.getSymbolImageLink(runData, lastSearch));
                            }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.SAMPLECENT)){
                                context.put("temp"+(i-startMolecule)+"link","portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?startMolecule="+startMolecule+"&nmrshiftdbaction=showDetails&newmol=true&molNumber="+(i-startMolecule));
                                sample=DBSamplePeer.retrieveByPK(new NumberKey(results.get(i)));
                                if(sample.getProbableStructure()>0){
                                    if(UrlTool.doImageCache()){
                                        context.put("temp"+(i-startMolecule)+"file", "images/molecules/small/"+sample.getProbableStructure()+".jpeg");
                                    }else{
                                        StringBuffer sb= new StringBuffer("download/NmrshiftdbServlet/").append(sample.getProbableStructure()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid=").append(sample.getProbableStructure()).append("&format=jpeg&size=small&shownumbers=false&backcolor=").append(LABGROUP_BACKGROUND_COLOR);
                                        context.put("temp"+(i-startMolecule)+"file", sb.toString());
                                    }
                                }else{
                                    context.put("temp"+(i-startMolecule)+"file", "nmrshiftdbhtml/nostruc.gif");
                                }
                                inscription=sample.getUsersId();
                                if(sample.getUsersIdComment()!=null && !sample.getUsersIdComment().equals(""))
                                    inscription+="<br>"+sample.getUsersIdComment();
                                if(sample.getSpecialCare()!=null && !sample.getSpecialCare().equals(""))
                                    inscription+="<br>"+sample.getSpecialCare();
                            }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.MIXCENT)){
                                mix=DBMixturePeer.retrieveByPK(new NumberKey(results.get(i)));
                                context.put("temp"+(i-startMolecule)+"link","portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&molNumber="+(i-startMolecule)+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : ""));
                                context.put("temp"+(i-startMolecule)+"file", "dasfsd");
                            }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.SUBCENT)){
                                sub=DBSubstancePeer.retrieveByPK(new NumberKey(results.get(i)));
                                context.put("temp"+(i-startMolecule)+"link","portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&molNumber="+(i-startMolecule)+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : ""));
                                context.put("temp"+(i-startMolecule)+"file", "noimage");
                            }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.SPECCENT)){
                                spec=DBSpectrumPeer.retrieveByPK(new NumberKey(results.get(i)));
                                StringBuffer linkstring=new StringBuffer("portal/js_pane/P-Results;jsessionid="+runData.getSession().getId()+"?startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&molNumber="+(i-startMolecule)+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : ""));
                                if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)){
                                    linkstring.append("&spectrum="+URLEncoder.encode(lastSearch.getRange(),"UTF-8"));
                                }
                                context.put("temp"+(i-startMolecule)+"link",linkstring.toString());
                                StringBuffer filestring=new StringBuffer("download/NmrshiftdbServlet/"+spec.getSpectrumId()+".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportspec&spectrumid="+ spec.getSpectrumId() +"&format=jpeg&size=150x150&shownumbers=false&backcolor=12632256");
                                if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)){
                                    filestring.append("&searchspec="+URLEncoder.encode(lastSearch.getRange(),"UTF-8"));
                                }
                                context.put("temp"+(i-startMolecule)+"file", filestring.toString());
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.HOSECODE))
                                context.put("temp"+(i-startMolecule)+"file",context.get("temp"+(i-startMolecule)+"file")+"&markatom="+mol.getAtomByHoseCode(lastSearch.getRange()).getMdlNumberPlus1());
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY)) {
                                StringBuffer s = new StringBuffer();
                        		format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
                        		if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY))
                        			format.applyPattern("0");
                                else
                                	format.applyPattern("0.00");
                                inscription = (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY) ? "Tanimoto Coefficient" : "Similarity")+": " + format.format(((Double)((ArrayList<Double>)session.getAttribute("similarities")).get(i)).doubleValue(), s, f) + " %";
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.FORMULA)) {
                                inscription = mol.getMolecularFormula(false);
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.CHEMNAME) || (lastSearch.getDisplayText().equals(NmrshiftdbConstants.BROWSE) && runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("mol")) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSTRUCTURE)) {
                                if(session.getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.MIXCENT)){
                                    inscription=mix.getMixtureId()+"";
                                }else{
                                    inscription = mol.getChemicalNamesAsOneString(true, true);
                                }
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.CASNUMBER)) {
                                inscription = mol.getCasNumber();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_FRAGMENT) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_TOTAL)) {
                                inscription = mol.getMoleculeKeywordsAsOneString();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_FRAGMENT) | lastSearch.getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_TOTAL) | (lastSearch.getDisplayText().equals(NmrshiftdbConstants.BROWSE) && runData.getSession().getAttribute("browseallall")!=null && ((String)runData.getSession().getAttribute("browseallall")).equals("spec"))) {
                                if(mol!=null)
                                    inscription = mol.getSpectrumKeywordsAsOneString();
                                else
                                    inscription = spec.getKeywords();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.LITERATURE_AUTHOR) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.LITERATURE_TITLE)) {
                                inscription = mol.getLiteratureAsOneString();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.COMMENT)) {
                                if(mol!=null)
                                    inscription = mol.getCommentAsOneString();
                                else
                                    inscription = spec.getCommentAsString();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.CANNAME)) {
                                inscription = new String(mol.getCanonicalName());
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SPECLINK)) {
                                if(mol!=null)
                                    inscription = mol.getSpectrumLinksAsOneString();
                                else
                                    spec.getLinksAsString();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.MOLLINK)) {
                                inscription = mol.getMoleculeLinksAsOneString();
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
                                inscription = mol.getMySpectraInscription(runData.getUserFromSession());
                            }
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.WEIGHT)) {
                                format = new DecimalFormat("0.000");
                                inscription = format.format(mol.getMolecularWeight());
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.CONDITIONS)){
                                if(mol!=null)
                                    inscription = mol.getCondition(lastSearch.getNameForElements());
                                else
                                    spec.getCondition(lastSearch.getNameForElements());
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTKEYWORD)){
                                inscription = sub.getSubstanceComment();
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SPECTRUM_NR)){
                                inscription = mol.getSpectrumIds();
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.MOLECULE_NR)){
                                inscription = mol.getMoleculeId()+"";
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.HOSECODE)){
                                StringBuffer inscriptionbuffer= new StringBuffer();
                                List<DBSignal> v=mol.getAtomByHoseCode(lastSearch.getRange()).getDBSignals();
                                for(int k=0;k<v.size();k++){
                                    inscriptionbuffer.append(Float.toString(((DBSignal)v.get(k)).getFirstShift().getValue()));
                                    inscriptionbuffer.append("; ");
                                }
                                inscription = GeneralUtils.removeLastComma(inscriptionbuffer).toString();
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.DBE_RINGS)){
                                inscription = "DBEs: "+mol.getDbe()+", SSSR: "+mol.getSssr()+", All rings: "+mol.getSar();
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY)) {
                                inscription = mol.getPotentialMultiplicities();
                            }
                            if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY)) {
                                StringBuffer inscriptionbuffer=new StringBuffer();
                                List<DBSpectrum> spectra=mol.selectSpectra();
                                for(int k=0;k<spectra.size();k++){
                                    List<DBSignal> signals=((DBSpectrum)spectra.get(k)).getDBSignals();
                                    //List signals=spec.getDBSignals();
                                    HashMap<String, Integer> mults=new HashMap<String, Integer>();
                                    for(int l=0;l<signals.size();l++){
                                        String mult=((DBSignal)signals.get(l)).getMultiplicity();
                                        if(mults.get(mult)==null){
                                            mults.put(mult,new Integer(((DBSignal)signals.get(l)).getAtoms().size()));
                                        }else{
                                            mults.put(mult,new Integer(((Integer)mults.get(mult)).intValue()+(((DBSignal)signals.get(l)).getAtoms().size())));
                                        }
                                    }
                                    Iterator<String> iterator = mults.keySet().iterator();
                                    while (iterator.hasNext()) {
                                        String element = (String) iterator.next();
                                        inscriptionbuffer.append(element+((Integer)mults.get(element)).intValue());
                                    }
                                    inscription+=";";
                                }
                                inscription=GeneralUtils.removeLastComma(inscriptionbuffer).toString();
                            }
                            if (!lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT) && !lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSTKEYWORD) && inscription.length() > 29) {
                                inscription = inscription.substring(0, 25) + "...";
                            }
                            context.put("inscription"+(i-startMolecule), inscription);
                        } catch (Exception ex) {
                            context.put("message",GeneralUtils.logError(ex,"ResultPortlet/resultPage/building inscriptions",runData,true));
                            context.put("template","result/message");
                            return;
                        }
                    }else{
                        context.put("temp"+(i-startMolecule)+"file", "none");
                    }
                }
                context.put("startmolecule", "" + startMolecule);
                context.put("nmrshiftdbconstants",new NmrshiftdbConstants());
                @SuppressWarnings("unchecked")
                ArrayList<ValuesForVelocityBean> searchesWithMeasuredOrCalculatdOnly=(ArrayList<ValuesForVelocityBean>)searchesV.clone();
                if(session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null)
                    searchesWithMeasuredOrCalculatdOnly.add(new ValuesForVelocityBean(NmrshiftdbConstants.MEASUREDONLY, "--", "--"));
                if(session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null)
                    searchesWithMeasuredOrCalculatdOnly.add(new ValuesForVelocityBean(NmrshiftdbConstants.CALCULATEDONLY, "--", "--"));
                if(session.getAttribute(NmrshiftdbConstants.JCAMPONLY)!=null && ((Boolean)session.getAttribute(NmrshiftdbConstants.JCAMPONLY)).booleanValue())
                    searchesWithMeasuredOrCalculatdOnly.add(new ValuesForVelocityBean(NmrshiftdbConstants.JCAMPONLY, "--", "--"));
                if(session.getAttribute(NmrshiftdbConstants.RAWONLY)!=null && ((Boolean)session.getAttribute(NmrshiftdbConstants.RAWONLY)).booleanValue())
                    searchesWithMeasuredOrCalculatdOnly.add(new ValuesForVelocityBean(NmrshiftdbConstants.RAWONLY, "--", "--"));
                context.put("searches",searchesWithMeasuredOrCalculatdOnly);
                context.put("data",runData);
            }
            context.put("link", JetspeedLinkFactory.getInstance(runData));
        }catch(Exception ex){
            context.put("message",GeneralUtils.logError(ex,"ResultPortlet",runData,true));
            context.put("template","result/message");
            return;
        }
    }

    /**
     *  Gets the searches attribute of the session
     *
     * @param  session  The current searches
     * @return          The searches value
     */
    public static int[] getSearches(HttpSession session) {
        return ((int[]) session.getAttribute("searches"));
    }


    /**
     *  Executes the last search
     *
     * @param  session        Current session
     * @return                ArrayList of mdls from MOLECULE table.
     * @exception  Exception  Database problems
     */
    private List<String> executeLastSearch(HttpSession session, StringAndInt chiralMessage, RunData runData, StringBuffer message, BaseJetspeedUser loggedInUser, SearchUtils searchUtils) throws Exception {
        List<String> v=executeSearch(session, new int[]{GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size() - 1}, chiralMessage, runData, message, loggedInUser, searchUtils);
        return(v);
    }


    /**
     *  Executes searches from the searchHistory.
     *
     * @param  session        The current session.
     * @param  searches       The searches to execute. Each figure in searches means the search in this position in sarchHistory will be executed, all searches are AND linked.
     * @return                ArrayList of mdls from MOLECULE table.
     * @exception  Exception  Database problems
     */
    private List<String> executeSearch(HttpSession session, int[] searches, StringAndInt chiralMessage, RunData runData, StringBuffer message, BaseJetspeedUser loggedInUser, SearchUtils searchUtils) throws NmrshiftdbException, Exception {
        return executeSearch(session, searches, chiralMessage, runData, false, true, message, loggedInUser, searchUtils);
    }


    /**
     *  Executes searches from the searchHistory.
     *
     * @param  session        The current session.
     * @param  searches       The searches to execute. Each figure in searches means the search in this position in sarchHistory will be executed, all searches are AND linked.
     * @return                ArrayList of mdls from MOLECULE table.
     * @exception  Exception  Database problems
     */
    private List<String> executeSearch(HttpSession session, int[] searches, StringAndInt chiralMessage, RunData runData, boolean not, boolean andor, StringBuffer message, BaseJetspeedUser loggedInUser, SearchUtils searchUtils) throws NmrshiftdbException, Exception {
        ArrayList<ValuesForVelocityBean> searchesV=new ArrayList<ValuesForVelocityBean>();
        ArrayList<ValuesForVelocityBean> searchHistory=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
        for (int i = 0; i < searches.length; i++) {
            searchesV.add(searchHistory.get(searches[i]));
        }
        session.setAttribute("searchesV",searchesV);
        session.setAttribute("searches", searches);
        ArrayList<Double> similarities = new ArrayList<Double>();
        session.setAttribute("similarities",similarities);
        return (SearchUtils.executeSearch(searchHistory, searches, chiralMessage, similarities, session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null, session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null, runData, ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"), not, andor,(ArrayList<DBSpectrumType>)runData.getSession().getAttribute("choosenspectrumtypes"),true, message, session.getAttribute(NmrshiftdbConstants.JCAMPONLY)!=null  && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY)).booleanValue()==true, session.getAttribute(NmrshiftdbConstants.RAWONLY)!=null  && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.RAWONLY)).booleanValue()==true, loggedInUser));
    }



    /**
     * The action for showing results as id list.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doShowids(RunData runData, Context context){
        try{
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) runData.getSession().getAttribute("results");
            context.put("results",results);
            context.put("peer",new DBMoleculePeer());
            context.put("int",new Integer(0));
            context.put("template","result/showids");
        } catch (Exception ex) {
            context.put("error",GeneralUtils.logError(ex,"submit/finalsubmit",runData,true));
        }      
    }


    /**
     * The action for doing all the text searches.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doGeneralsearch(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            String searchstring = runData.getParameters().getString("searchstring");
            String searchaction = runData.getParameters().getString("searchaction");
            String searchfield = runData.getParameters().getString("searchfield");
            String message="";
            SearchUtils searchUtils=new SearchUtils();
            if ((searchaction.equals(NmrshiftdbConstants.REGEXP) || searchaction.equals(NmrshiftdbConstants.FRAGMENT)) && searchfield.equals(NmrshiftdbConstants.FORMULA)) {
                message="Regular expression/fragment search not possible in chemical formula search!";
            }
            if ((searchaction.equals(NmrshiftdbConstants.REGEXP) || searchaction.equals(NmrshiftdbConstants.FRAGMENT)) && (searchfield.equals(NmrshiftdbConstants.MULTIPLICITY) || searchfield.equals(NmrshiftdbConstants.POTMULTIPLICITY))) {
                message="Regular expression/fragment search not possible in multiplicity search!";
            }
            if ((searchaction.equals(NmrshiftdbConstants.DBE_RINGS)  && (searchfield.equals(NmrshiftdbConstants.MULTIPLICITY) || searchfield.equals(NmrshiftdbConstants.POTMULTIPLICITY)))) {
                message="Regular expression/fragment search not possible in dbe/sssr search!";
            }
            if (searchaction.equals(NmrshiftdbConstants.FUZZY) && searchfield.equals(NmrshiftdbConstants.CASNUMBER)) {
                message="Fuzzy CAS number search is not possible";
            }
            if (searchaction.equals(NmrshiftdbConstants.FUZZY) && (searchfield.equals(NmrshiftdbConstants.SPECTRUM_NR) || searchfield.equals(NmrshiftdbConstants.MOLECULE_NR))) {
                message="Fuzzy nmrshiftdb2 number search is not possible";
            }
            if(searchfield.equals(NmrshiftdbConstants.WEIGHT) && !searchaction.equals(NmrshiftdbConstants.EXACT)){
                message="Weight search is only possible in exact mode";
            }
            if(searchstring.equals("")){
                message="Empty search input! Enter a search term.";
            }
            if(searchfield.equals(NmrshiftdbConstants.WEIGHT) && searchstring.indexOf("-")==searchstring.length()-1){
                searchstring=searchstring+""+Integer.MAX_VALUE;
            }
            if(searchfield.equals(NmrshiftdbConstants.WEIGHT) && searchstring.indexOf("-")==0){
                searchstring=Integer.MIN_VALUE+""+searchstring;
            }
            if(searchfield.equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM)){
            	try{
	                if(GeneralUtils.resolveNameFromPubchem(searchstring)==null){
	                    chiralMessage.myString=searchstring+" was not found in Pubchem. Are you sure this is some sort of chemical name? We still try if it is in nmrshiftdb2";
	                    searchfield=NmrshiftdbConstants.CHEMNAME;
	                }
            	}catch(Exception ex){
                    chiralMessage.myString="It seems there is a problem with Pubchem. We still try if it is in nmrshiftdb2";
                    searchfield=NmrshiftdbConstants.CHEMNAME;            		
            	}
            }
            if(searchfield.equals(NmrshiftdbConstants.WEIGHT)){
                StringTokenizer st=new StringTokenizer(searchstring,"-");
                if(!st.hasMoreTokens()){
                    message="Invalid format for weight. Must be like 4.5-12.6";
                }else{
                    String first=st.nextToken();
                    if(!st.hasMoreTokens()){
                        message="Invalid format for weight. Must be like 4.5-12.6";
                    }else{
                        String second=st.nextToken();
                        try{
                            Double.parseDouble(first);
                            Double.parseDouble(second);
                        }catch(NumberFormatException ex){
                            message="Invalid format for weight. Must be like 4.5-12.6";
                        }
                    }
                }
            }
            if (searchaction.equals(NmrshiftdbConstants.EXACT) && (searchfield.equals(NmrshiftdbConstants.FORMULA) || searchfield.equals(NmrshiftdbConstants.FORMULA_WITH_OTHER))) {
                for (int i = 0; i < searchstring.length(); i++) {
                    if (!Character.isLetterOrDigit(searchstring.charAt(i)) == true && searchstring.charAt(i)!='*' && searchstring.charAt(i)!='-') {
                        message="Your input is not a valid chemical formula!";
                    }
                }
            }
            if (searchaction.equals(NmrshiftdbConstants.EXACT) && searchfield.equals(NmrshiftdbConstants.POTMULTIPLICITY)) {
                for (int i = 0; i < searchstring.length(); i++) {
                    if (!Character.isLetterOrDigit(searchstring.charAt(i)) == true) {
                        message="Your input may only contain S D T Q and a figure after each!";
                    }
                }
            }
            if(!message.equals("")){
                results=new ArrayList<String>();
                session.setAttribute("results", results);
                context.put("message",message);
                context.put("template","result/message");
                return;
            }
            session.setAttribute("searchaction", searchaction);
            session.setAttribute("searchfield", searchfield);
            session.setAttribute("searchstring", searchstring);
            if(runData.getParameters().getString("spectype")!=null)
                searchaction=runData.getParameters().getString("spectype");
            if(runData.getParameters().getString("solvent")!=null)
            	searchaction=searchaction + " " + runData.getParameters().getString("solvent");
            //And we do the search
            try {
                if(searchfield.equals(NmrshiftdbConstants.DBE_RINGS)){
                    StringTokenizer st=new StringTokenizer(searchstring,"/");
                    GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).add(new ValuesForVelocityBean(NmrshiftdbConstants.DBE, searchaction, st.nextToken()));
                    GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).add(new ValuesForVelocityBean(NmrshiftdbConstants.SSSR, searchaction, st.nextToken()));
                    int[] searches = new int[2];
                    searches[0]=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size()-2;
                    searches[1]=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size()-1;
                    results = executeSearch(session, searches, chiralMessage, runData, false, true, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
                }else{
                    GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).add(new ValuesForVelocityBean(searchfield, searchaction, searchstring));
                    results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
                }
                session.setAttribute("results", results);
                context.put("chiralMessage", chiralMessage);
                ((ValuesForVelocityBean)GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).get(GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size()-1)).setLower(results.size());
            } catch (NmrshiftdbException ex){
                GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).remove(GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size() - 1);
                results=new ArrayList<String>();
                session.setAttribute("results", results);
                context.put("message",GeneralUtils.logError(ex, "ResultPortlet/generalsearch", runData,true));
                context.put("template","result/message");
                return;
            } catch (Exception ex) {
                if(ex.getMessage()!=null && ex.getMessage().indexOf("from regexp")!=-1){
                    GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).remove(GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size() - 1);
                    results=new ArrayList<String>();
                    session.setAttribute("results", results);
                    context.put("message","Your regular expression search is invalid! For in introduction to regular expressions see e. g. <a href=\"http://www.phpbuilder.com/columns/dario19990616.php3\" target=\"_blank\">PHPBuilder.com</a>. <a href=\"portal/js_pane/P-Search;jsessionid="+runData.getSession().getId()+"\">Back to search page.</a>");
                    context.put("template","result/message");
                    return;
                }else{
                    throw ex;
                }
            }
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/generalsearch", runData,true));
            context.put("template","result/message");
            return;
        }
    }


    /**
     * The action for browsing between pages.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doShownewpage(RunData runData, Context context){
        int startMolecule = new Integer(runData.getParameters().getString("startMolecule")).intValue();
        runData.getSession().setAttribute("startmolecule",new Integer(startMolecule));
    }


    /**
     * The action for searching by conditions.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doConditionsearch(RunData runData, Context context){
        try{
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            ArrayList<ValuesForVelocityBean> v = GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
            ((ArrayList<String>)session.getAttribute(SearchConditionAction.LASTACTION_KEY)).clear();
            List<DBConditionType> conditions=DBConditionTypePeer.doSelect(new Criteria());
            List<Integer> searchestodoV=new ArrayList<Integer>();
            for(int i=0;i<conditions.size();i++){
                if(runData.getParameters().getString(Integer.toString(((DBConditionType)conditions.get(i)).getConditionTypeId()))!=null){
                    String conditionname=((DBConditionType)conditions.get(i)).getConditionName();
                    ((ArrayList<Integer>)session.getAttribute(SearchConditionAction.LASTACTION_KEY)).add(((DBConditionType)conditions.get(i)).getConditionTypeId());
                    String conditionvalue=runData.getParameters().getString("existing"+((DBConditionType)conditions.get(i)).getConditionTypeId()+"conditions");
                    GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).add(new ValuesForVelocityBean(NmrshiftdbConstants.CONDITIONS, conditionname, conditionvalue));
                    searchestodoV.add(new Integer(GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size()-1));
                }
            }
            //And we do the search
            int[] searchestodo=new int[searchestodoV.size()];
            for(int i=0;i<searchestodoV.size();i++){
                searchestodo[i]=((Integer)searchestodoV.get(i)).intValue();
            }
            results = executeSearch(session, searchestodo, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            ((ValuesForVelocityBean)v.get(v.size()-1)).setLower(results.size());
            session.setAttribute("results", results);
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/conditionsearch", runData,true));
            context.put("template","result/message");
            return;
        }
    }

    /**
     * The action for searching by experiment type.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doExperimenttypes(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            List<DBExperiment> experimentsList = DBExperimentPeer.doSelect(new Criteria());
            StringBuffer chosenExperiments = new StringBuffer();
            List<Integer> chosenExperimentsList = new ArrayList<Integer>();
            for(DBExperiment experiment : experimentsList){
                if(runData.getParameters().get("experiment"+experiment.getExperimentId())!=null && runData.getParameters().get("experiment"+experiment.getExperimentId()).equals("experiment"+experiment.getExperimentId())){
                    chosenExperiments.append(experiment.getExperimentId()).append(" ");
                    chosenExperimentsList.add(experiment.getExperimentId());
                }
            }
            if(chosenExperiments.length()>0){
                GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).add(new ValuesForVelocityBean(NmrshiftdbConstants.SAMPLE_EXPERIMENTS, "--", chosenExperiments.toString().trim()));
                session.setAttribute("searchsampleexperiments", chosenExperimentsList);
            }else{
                session.removeAttribute("searchsampleexperiments");
            }
            //And we do the search
            results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/experimenttypes", runData,true));
            context.put("template","result/message");
            return;
        }
    }


    /**
     * The action for searching by spectrum.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doSearchbyspectrumspec(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            CMLPeakList numberSpectrum = null;
            String spectrum=null;
            //The input is parsed
            spectrum = runData.getParameters().getString("spectrum").trim();
            spectrum = GeneralUtils.replace(spectrum, " ", "");
            try {
                numberSpectrum = ParseUtils.parseSpectrum(spectrum,null);
            } catch (NumberFormatException ex) {
                context.put("message","Your input included wrong characters (e. g. letters)!<br>The input format must be like this:<br>56.2;.42<br>64.1;.6<br>125.6;.3<br>Message: "+ex.getMessage());
                context.put("template","result/message");
                return;
            }
            if (numberSpectrum.getPeakElements().size() == 0) {
                context.put("message","Your input did not include a valid signal!");
                context.put("template","result/message");
            }
            List<CMLPeak> toremove = new ArrayList<CMLPeak>();
            //auto-scale input
            if(!DBSpectrumTypePeer.retrieveByPK(runData.getParameters().getInt("spectrumtypespectrumsearch")).getName().contains("correlation")) 
            	SpectrumUtils.scaleIntensities(numberSpectrum.getPeakElements());
            for (int i = 0; i < numberSpectrum.getPeakElements().size(); i++) {
                for (int k = 0; k < i; k++) {
                    if (numberSpectrum.getPeakElements().get(k).getXValue() == numberSpectrum.getPeakElements().get(i).getXValue()) {
                        toremove.add(numberSpectrum.getPeakElements().get(i));
                    }
                }
            }
            for(int i=0;i<toremove.size();i++){
                numberSpectrum.removeChild(toremove.get(i));
            }
            SignalComparator sigComp = new SignalComparator(false);
            Collections.sort(numberSpectrum.getChildCMLElements(), sigComp);
            //We need to put the input file into an array of DBSignals because the fingerprint is calculated from that. The initSimilarity()-method of DBSpectrum in showDetails also uses this
            DBSignal[] searchSpectrum = new DBSignal[numberSpectrum.getPeakElements().size()];
            for (int i = 0; i < numberSpectrum.getPeakElements().size(); i++) {
                DBSignal sig = new DBSignal();
                DBShift[] shifts = new DBShift[1];
                DBShift shift = new DBShift();
                shift.setAxis(1);
                shift.setValue((float)numberSpectrum.getPeakElements().get(i).getXValue());
                shifts[0] = shift;
                sig.setDBShiftsArray(shifts);
                sig.addDBShift(shift);
                sig.setIntensity((float)numberSpectrum.getPeakElements().get(i).getYValue());
                searchSpectrum[i] = sig;
            }
            //For the UDF the spectrum is needed as a string like the specFile
            StringBuffer searchSpectrumBuffer = new StringBuffer();
            if(runData.getParameters().getString("correlation","no").equals("correlation")) {
            	searchSpectrumBuffer.append("correlation-");
            	session.setAttribute("correlation","yes");
            }else {
            	session.setAttribute("correlation","no");
            }
            searchSpectrumBuffer.append(DBSpectrumTypePeer.retrieveByPK(runData.getParameters().getInt("spectrumtypespectrumsearch")).getName()+"-");
            session.setAttribute("spectrumtype",DBSpectrumTypePeer.retrieveByPK(runData.getParameters().getInt("spectrumtypespectrumsearch")));
            for (int i = 0; i < numberSpectrum.getPeakElements().size(); i++) {
                searchSpectrumBuffer.append(Double.toString(numberSpectrum.getPeakElements().get(i).getXValue()) + ";" + Double.toString(numberSpectrum.getPeakElements().get(i).getYValue()) + (numberSpectrum.getPeakElements().get(i).getPeakMultiplicity()!=null ? numberSpectrum.getPeakElements().get(i).getPeakMultiplicity() : "")+";0|");
            }
            boolean useintensities=false;
            if(runData.getParameters().get("useintensities")!=null && runData.getParameters().get("useintensities").equals("useintensities"))
                useintensities=true;
            //We put that into the search history
            byte sub = 0;
            if(runData.getParameters().getString("suborwhole").equals("sub"))
                sub=1;
            if(runData.getParameters().getString("suborwhole").equals("fast"))
                sub=2;
            ArrayList<ValuesForVelocityBean> v = GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
            if (sub==0) {
                v.add(new ValuesForVelocityBean(NmrshiftdbConstants.TOTALSPECTRUM, useintensities ? "use intensities" : "--", searchSpectrumBuffer.toString()));
            }else if(sub==1){
                v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SUBSPECTRUM, useintensities ? "use intensities" : "--", searchSpectrumBuffer.toString()));
            }
            //And we do the search
            results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
            ((ValuesForVelocityBean)v.get(v.size()-1)).setLower(results.size());
            //We remember the values
            if(spectrum!=null)
                session.setAttribute("spectrum", spectrum);
            session.setAttribute("subortotal", runData.getParameters().getString("suborwhole"));
            session.setAttribute("correlation", runData.getParameters().getString("correlation","false").equals("correlation") ? "true" : "false");
            
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/spectrum search", runData,true));
            context.put("template","result/message");
            return;
        }
    }

    /**
     * The action for searching by sample properties.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doSearchbysampleprops(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            String searchstring = runData.getParameters().getString("searchstringsample");
            if(searchstring!=null && !searchstring.equals("")){
                String searchaction = runData.getParameters().getString("searchactionsample");
                String searchfield = runData.getParameters().getString("sampleprops");
                session.setAttribute("searchactionsample", searchaction);
                session.setAttribute("sampleprops", searchfield);
                session.setAttribute("searchstringsample", searchstring);
                ((List<Object>)GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY)).add(new ValuesForVelocityBean(searchfield, searchaction, searchstring));
            }else{
                session.removeAttribute("searchactionsample");
                session.removeAttribute("sampleprops");
                session.removeAttribute("searchstringsample");
            }
            //And we do the search
            results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/sampleprops", runData,true));
            context.put("template","result/message");
            return;
        }
    }


    /**
     * The action for repeating searches from the search history.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doRepeatsearches(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            boolean not=false;
            if(runData.getParameters().getString("not")!=null && runData.getParameters().getString("not").equals("not"))
                not=true;
            boolean andor=true;
            if(runData.getParameters().getString("andor").equals("or"))
                andor=false;
            ArrayList<Integer> v = new ArrayList<Integer>();
            runData.getSession().setAttribute(VectorWithSessionBinding.REQUEST_IP, runData.getRequest().getRemoteAddr());
            for (int i = 0; i < GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY).size(); i++) {
                if (runData.getParameters().getString("dosearch" + (i + 1)) != null && runData.getParameters().getString("dosearch" + (i + 1)).equals("dosearch")) {
                    v.add(new Integer(i));
                }
            }
            if(v.size()==0){
                context.put("message","You did not choose any search to repeat!");
                context.put("template","result/message");
                return;
            }
            int[] searches = new int[v.size()];
            for (int i = 0; i < v.size(); i++) {
                searches[i] = ((Integer) v.get(i)).intValue();
            }
            StringBuffer messageDuration = new StringBuffer();
            results = executeSearch(session, searches, chiralMessage, runData, not, andor, messageDuration, (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
            context.put("messageduration", messageDuration);
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/repeat search", runData,true));
            context.put("template","result/message");
            return;
        }
    }


    /**
     * The action for searching spectra of the logged in user.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doMyspectra(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            ArrayList<ValuesForVelocityBean> v = GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
            v.add(new ValuesForVelocityBean(NmrshiftdbConstants.MYSPECTRA, "--", runData.getUserFromSession().getUserName()));
            //And we do the search
            results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
            ((ValuesForVelocityBean)v.get(v.size()-1)).setLower(results.size());
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/myspectra", runData,true));
            context.put("template","result/message");
            return;
        }
    }

    /**
     * The action for publishing a spectrum.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doPublish(RunData runData, Context context){
        try{
            DBSpectrum dbspectrum = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInt("spectrumid"));
            String messageDuration=dbspectrum.publish(runData.getParameters().getString("server"), TurbineUserPeer.retrieveByPK(Integer.parseInt(((JetspeedRunData)runData).getUserId())).getLoginName(), (String)runData.getSession().getAttribute("pwd"));
            context.put("messageduration", messageDuration);
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/publish", runData,true));
            context.put("template","result/message");
            return;
        }
    }

    /**
     * The action for publishing a spectrum.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doPublishdataset(RunData runData, Context context){
        try{
            DBSpectrum dbspectrum = DBSpectrumPeer.retrieveByPK(runData.getParameters().getInt("spectrumid"));
            List<DBSpectrum> spectra = dbspectrum.getDBMolecule().getDBSpectrums();
            StringBuffer messageDuration=new StringBuffer();
            for(DBSpectrum spectrum : spectra) {
            	if(spectrum.getSolvent().equals(dbspectrum.getSolvent()) && spectrum.getKeywords().equals(dbspectrum.getKeywords())) {
            		messageDuration.append(spectrum.publish(runData.getParameters().getString("server"), TurbineUserPeer.retrieveByPK(Integer.parseInt(((JetspeedRunData)runData).getUserId())).getLoginName(), (String)runData.getSession().getAttribute("pwd"))+"; ");
            	}
            }
            context.put("messageduration", GeneralUtils.removeLastComma(messageDuration));
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/publish", runData,true));
            context.put("template","result/message");
            return;
        }
    }
    
    /**
     * The action for searching by keyword selection.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doKeywordselection(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            //Get keywords used for spectra
            Criteria crit = new Criteria();
            crit.addJoin(DBKeywordPeer.KEYWORD_ID, DBSpectrumDBKeywordPeer.KEYWORD_ID);
            crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
            crit.setDistinct();
            List<DBKeyword> spectrumKeywords = DBKeywordPeer.doSelect(crit);
            //and for molecules and put them in ArrayLists
            crit = new Criteria();
            crit.addJoin(DBKeywordPeer.KEYWORD_ID, DBMoleculeDBKeywordPeer.KEYWORD_ID);
            crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
            crit.setDistinct();
            List<DBKeyword> moleculeKeywords = DBKeywordPeer.doSelect(crit);
            for (int k = 0; k < moleculeKeywords.size(); k++) {
                ((DBKeyword) moleculeKeywords.get(k)).setIsChoosen(false);
            }
            String[] choosenMoleculeKeywords = runData.getParameters().getStrings("moleculekeywords");
            StringBuffer mk = new StringBuffer();
            if (choosenMoleculeKeywords != null) {
                for (int i = 0; i < choosenMoleculeKeywords.length; i++) {
                    for (int k = 0; k < moleculeKeywords.size(); k++) {
                        if (((DBKeyword) moleculeKeywords.get(k)).getKeyword().equals(choosenMoleculeKeywords[i])) {
                            ((DBKeyword) moleculeKeywords.get(k)).setIsChoosen(true);
                            mk.append(choosenMoleculeKeywords[i] + "|");
                        }
                    }
                }
            }
            for (int k = 0; k < spectrumKeywords.size(); k++) {
                ((DBKeyword) spectrumKeywords.get(k)).setIsChoosen(false);
            }
            String[] choosenSpectrumKeywords = runData.getParameters().getStrings("spectrumkeywords");
            StringBuffer sk = new StringBuffer();
            if (choosenSpectrumKeywords != null) {
                for (int i = 0; i < choosenSpectrumKeywords.length; i++) {
                    for (int k = 0; k < spectrumKeywords.size(); k++) {
                        if (((DBKeyword) spectrumKeywords.get(k)).getKeyword().equals(choosenSpectrumKeywords[i])) {
                            ((DBKeyword) spectrumKeywords.get(k)).setIsChoosen(true);
                            sk.append(choosenSpectrumKeywords[i] + "|");
                        }
                    }
                }
            }
            boolean fragment=runData.getParameters().getString("fragmentortotal").equals("fragment");
            session.setAttribute("frag", runData.getParameters().getString("fragmentortotal"));
            ArrayList<ValuesForVelocityBean> v = GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
            if (!mk.toString().equals("")) {
                if(fragment)
                    v.add(new ValuesForVelocityBean(NmrshiftdbConstants.MOLECULEKEYWORDS_FRAGMENT, "--", mk.toString()));
                else
                    v.add(new ValuesForVelocityBean(NmrshiftdbConstants.MOLECULEKEYWORDS_TOTAL, "--", mk.toString()));
            }
            if (!sk.toString().equals("")) {
                if(fragment)
                    v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SPECTRUMKEYWORDS_FRAGMENT, "--", sk.toString()));
                else
                    v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SPECTRUMKEYWORDS_TOTAL, "--", sk.toString()));
            }
            //And we do the search
            results = executeLastSearch(session, chiralMessage, runData, new StringBuffer(), (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
            ((ValuesForVelocityBean)v.get(v.size()-1)).setLower(results.size());
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/keywordselection", runData,true));
            context.put("template","result/message");
            return;
        }
    }


    /**
     * The action for searching by structure.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doStructuresearch(RunData runData, Context context){
        String mdl=null;
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            //used for storing the results for browsing between result pages
            @SuppressWarnings("unchecked")
            List<String> results = (ArrayList<String>) session.getAttribute("results");
            session.removeAttribute("startmolecule");
            StringAndInt chiralMessage=new StringAndInt();
            SearchUtils searchUtils=new SearchUtils();
            session.removeAttribute("startmolecule");
            String type = runData.getParameters().getString("type");
            if(GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("GGAKETCHER") || runData.getSession().getAttribute("applet")!=null && ((String)runData.getSession().getAttribute("applet")).equals("applet")){
                mdl = runData.getParameters().getString("MolTxt");
            }else{
                StringWriter sw = new StringWriter();
                MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
                mdlwriter.writeMolecule((IAtomContainer)session.getAttribute("moltxt"));
                mdl=sw.toString();
                mdlwriter.close();
            }
            //Check if it was a valid input
            IAtomContainer molWithH;
            try{
                molWithH = AtomUtils.readMolBothVersions(mdl);
            }
            catch(CDKException ex){
                if(ex.getMessage().indexOf("Detected 'query bond type '")>-1){
                    context.put("message","You included a query bond like 'any bond' - this is currently not possible!");
                    context.put("template","result/message");
                    return;
                } else
                    throw ex;
            }
            for(int i=0;i<molWithH.getAtomCount();i++){
		if(molWithH.getAtom(i).getPoint2d()==null)
                    molWithH.getAtom(i).setPoint2d(new Point2d(molWithH.getAtom(i).getPoint3d().x,molWithH.getAtom(i).getPoint3d().y));
            }
            session.setAttribute("sub", type);
            session.setAttribute("moltxt", molWithH);
            if (molWithH.getAtomCount() == 0) {
                context.put("message","You did not draw anything");
                context.put("template","result/message");
                return;
            }
            ArrayList<ValuesForVelocityBean> v = GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
            GeneralUtils.addToStructureHistory(mdl, runData);
            if(runData.getParameters().getString("mixture","").equals("mixture")){
                v.add(new ValuesForVelocityBean(type, "mixture", mdl));
                session.setAttribute("mixture",true);
            }else{
                v.add(new ValuesForVelocityBean(type, "--", mdl));
                session.setAttribute("mixture",false);
            }
            //And we do the search
            StringBuffer messageDuration = new StringBuffer();
            results = executeLastSearch(session, chiralMessage, runData, messageDuration, (BaseJetspeedUser)runData.getUser(), searchUtils);
            session.setAttribute("results", results);
            context.put("messageduration", messageDuration);
            ((ValuesForVelocityBean)v.get(v.size()-1)).setLower(results.size());
        } catch (Exception ex) {
            context.put("message",GeneralUtils.logError(ex, "ResultPortlet/searchByStructure "+mdl, runData,true));
            context.put("template","result/message");
            return;
        }
    }
}
