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

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBAtomPeer;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBCoordinateSet2d;
import org.openscience.nmrshiftdb.om.DBCoordinateSet3d;
import org.openscience.nmrshiftdb.om.DBCoordinateSetType;
import org.openscience.nmrshiftdb.om.DBDatasetDoiPeer;
import org.openscience.nmrshiftdb.om.DBDatasetReviewKey;
import org.openscience.nmrshiftdb.om.DBDatasetReviewKeyDBKeyword;
import org.openscience.nmrshiftdb.om.DBDatasetReviewKeyPeer;
import org.openscience.nmrshiftdb.om.DBKeyword;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBMixture;
import org.openscience.nmrshiftdb.om.DBMixturePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBRawFilePeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSubstance;
import org.openscience.nmrshiftdb.om.DBSubstancePeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrum;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.comparator.DBSpectrumComparator;
import org.openscience.nmrshiftdb.util.comparator.SignalComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import com.workingdogs.village.Record;

/**
 *  This is the action for handling the display of details
 *
 * @author     Stefan Kuhn
 * @created    12. Februar 2002
 */
public class DetailsAction extends VelocityPortletAction {

    /**
     * Builds the context for the portlet.
     *
     * @param  runData  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
     */
    @Override
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData runData) {
        String action = runData.getParameters().getString("nmrshiftdbaction");
        if (action == null || action.equals("nocontinue")) {
            action = "";
            if(runData.getSession().getAttribute("results")!=null)
                action = "dummyaction";        }
        if(runData.getParameters().getString("applet") !=null && runData.getParameters().getString("applet").equals("applet")){
            runData.getSession().setAttribute("applet","applet");
        }
        if(runData.getParameters().getString("applet") !=null && runData.getParameters().getString("applet").equals("noapplet")){
            runData.getSession().setAttribute("applet","noapplet");
        }
        //used for storing the results for browsing between result pages
        @SuppressWarnings("unchecked")
        ArrayList<String> results = (ArrayList<String>) runData.getSession().getAttribute("results");
        if (results == null) {
            results = new ArrayList<String>();
        }
        //The molecule displaying starts with 0 if new search
        int startMolecule = 0;
        int molNumber = -1;
        context.put("LABGROUP_BACKGROUND_COLOR",ResultAction.LABGROUP_BACKGROUND_COLOR);
        context.put("STANDARD_BACKGROUND_COLOR",ResultAction.STANDARD_BACKGROUND_COLOR);
        if (!action.equals("") && (results.size()>0 || action.equals("showDetailsFromHome") || action.equals("showprivatebykey") || action.equals("submitforreview") || action.equals("submitforreviewdataset"))){
            int updateViews=0;
            try {
                context.put("random", new java.util.Random());
                if(action.equals("submitforreview") || action.equals("submitforreviewdataset")){
                	List<DBSpectrum> spectraToReview=new ArrayList<>();
                	StringBuffer message=new StringBuffer();
                	if(action.equals("submitforreviewdataset")) {
                		String tab=runData.getParameters().get("dataset");
                    	String datasetkeyword=tab.split(";")[0];
                        String datasetsolvent=tab.split(";")[1];
                        spectraToReview=DBMoleculePeer.retrieveByPK(runData.getParameters().getInt("moleculeid")).selectSpectra(datasetsolvent, datasetkeyword.replace('_', ';'), true);
                	}else {
                		spectraToReview.add(DBSpectrumPeer.retrieveByPK(Integer.parseInt(runData.getParameters().get("spectrumid"))));
                	}
                	for(DBSpectrum spectrumToReview : spectraToReview) {
	                    TurbineUser reviewer = GeneralUtils.selectReviewer(TurbineUserPeer.retrieveByPK(Integer.parseInt(((BaseJetspeedUser)runData.getUser()).getUserId())));
	                    new DBUserDBSpectrum(spectrumToReview.getSpectrumId(), reviewer, new Date()).save();
	                    spectrumToReview.setReviewFlag(NmrshiftdbConstants.FALSE);
	                    spectrumToReview.save();
	                    SpectrumUtils.doEmailForReviewAndSerialize(spectrumToReview.getMeAsSubmittingData(true, ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/normalizer.xml"), false, ((BaseJetspeedUser) runData.getUser()).getUserId()), reviewer, TurbineUserPeer.retrieveByPK(Integer.parseInt(((BaseJetspeedUser)runData.getUser()).getUserId())));
	                    message.append("The spectrum "+spectrumToReview.getSpectrumId()+" has been assigned for review. You should get an email and also find it on your personal page in \"My unreviewed submissions\"!<br>");
                	}
                    context.put("message",message.toString());
                    context.put("template","result/message");
                    return;
                }
                if(runData.getParameters().getString("molNumber")!=null && runData.getParameters().getString("molNumber").matches("^\\d+$"))
                    molNumber = new Integer(runData.getParameters().getString("molNumber").trim()).intValue();
                if(runData.getParameters().getString("startMolecule")!=null)
                	startMolecule = new Integer(runData.getParameters().getString("startMolecule")).intValue();
                if (molNumber == -1 && action.equals("showDetailsFromHome") && runData.getParameters().getString("molNumber").indexOf("atom")==-1 && runData.getParameters().getString("molNumber").indexOf("spectrum")==-1 && runData.getParameters().getString("molNumber").indexOf("substance")==-1) {
                    context.put("message","You did not choose a molecule !<script LANGUAGE=\"JavaScript1.1\">location.href=\"portal/js_pane/P-Home;jsessionid="+runData.getSession().getId()+"\"</script>");
                    context.put("template","result/message");
                    return;
                }
                if(molNumber==-1)
                    molNumber=0;
                if (molNumber != -1 && startMolecule + molNumber < results.size() || action.equals("showDetailsFromHome") || action.equals("visitbookmark") || action.equals("showprivatebykey")) {
                    context.put("startmolecule", "" + startMolecule);
                    context.put("startbrowseall",(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : ""));
                    DBMolecule mol = null;
                    DBSpectrum spec=null;
                    DBMixture mixture=null;
                    DBSample sample=null;
                    DBSubstance substance=null;
                    //If the structure to display is the same as the last one, we get objects to display from session.
                    boolean newStructureView=false;
                    if(runData.getSession().getAttribute("lastmoleculeviewed")==null || ((Integer)runData.getSession().getAttribute("lastmoleculeviewed")).intValue()!=molNumber){
                        newStructureView=true;
                    }
                    if (action.equals("showDetailsFromHome") || action.equals("visitbookmark") || action.equals("showprivatebykey")) {
                    	if(newStructureView){
	                        try{
	                            if(runData.getParameters().getString("molNumber").indexOf("atom")>-1){
	                                Criteria crit = new Criteria();
	                                crit.add(DBAtomPeer.ATOM_ID,runData.getParameters().getString("molNumber").substring(runData.getParameters().getString("molNumber").indexOf(":")+1));
	                                List<DBAtom> l=DBAtomPeer.doSelect(crit);
	                                if(l.size()>0){
	                                    molNumber=((DBAtom)l.get(0)).getMoleculeId();
	                                    mol = DBMoleculePeer.retrieveByPK(new NumberKey(molNumber));
	                                }else{
	                                    context.put("message","There is no atom with this number. Most likely it was edited. Please use the search functions to find the molecule!");
	                                    context.put("template","result/message");
	                                    return;
	                                }
	                            }else if(runData.getParameters().getString("molNumber").indexOf("spectrum")>-1){
	                                Criteria crit = new Criteria();
	                                crit.add(DBSpectrumPeer.SPECTRUM_ID,runData.getParameters().getString("molNumber").substring(runData.getParameters().getString("molNumber").indexOf(":")+1));
	                                List<DBSpectrum> l=DBSpectrumPeer.doSelect(crit);
	                                if(l.size()>0){
	                                    molNumber=((DBSpectrum)l.get(0)).getMoleculeId();
	                                    mol = DBMoleculePeer.retrieveByPK(new NumberKey(molNumber));
	                                }else{
	                                    context.put("message","There is no atom with this number. Most likely it was edited. Please use the search functions to find the molecule!");
	                                    context.put("template","result/message");
	                                    return;
	                                }
	                            }else if(runData.getParameters().getString("molNumber").indexOf("substance")>-1){
	                                Criteria crit = new Criteria();
	                                crit.add(DBSubstancePeer.SUBSTANCE_ID,runData.getParameters().getString("molNumber").substring(runData.getParameters().getString("molNumber").indexOf(":")+1));
	                                List<DBSubstance> l=DBSubstancePeer.doSelect(crit);
	                                if(l.size()>0){
	                                    substance=((DBSubstance)l.get(0));
	                                }else{
	                                    context.put("message","There is no substance with this number. Most likely it was edited. Please use the search functions to find the molecule!");
	                                    context.put("template","result/message");
	                                    return;
	                                }
	                            }else if(molNumber==0){
	                                mol = DBMoleculePeer.retrieveByPK(new NumberKey(runData.getParameters().getString("molNumber")));
	                            }else{
	                                mol = DBMoleculePeer.retrieveByPK(new NumberKey(molNumber));
	                            }
	                            if(mol!=null && !mol.isPublicOrOwnPrivate(TurbineUserPeer.retrieveByPK(Integer.parseInt(((BaseJetspeedUser)runData.getUser()).getUserId())),"-1") && !action.equals("showprivatebykey")){
	                                //If this is a private entry from another user, we show nothing
	                                context.put("message","There is no molecule with this number. Most likely it was edited. Please use the search functions to find the molecule!");
	                                context.put("template","result/message");
	                                return;
	                            }
	                            runData.getSession().setAttribute("moldetails",mol);
	                        }catch(Exception ex){
	                            context.put("message","There is no molecule with this number. Most likely it was edited. Please use the search functions to find the molecule!");
	                            context.put("template","result/message");
	                            return;
	                        }
                    	}else{
                    		mol=(DBMolecule)runData.getSession().getAttribute("moldetails");
                    	}
                    } else {
                        if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.STRUCCENT)){
                            mol = DBMoleculePeer.retrieveByPK(new NumberKey((String)results.get(startMolecule + molNumber)));
                        }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.MIXCENT)){
                            mixture=DBMixturePeer.retrieveByPK(new NumberKey((String)results.get(startMolecule + molNumber)));
                        }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.SAMPLECENT)){
                            sample=DBSamplePeer.retrieveByPK(new NumberKey((String)results.get(startMolecule + molNumber)));
                        }else if(runData.getSession().getAttribute(NmrshiftdbConstants.SEARCH_TYPE).equals(NmrshiftdbConstants.SUBCENT)){
                            substance=DBSubstancePeer.retrieveByPK(new NumberKey((String)results.get(startMolecule + molNumber)));
                        }else{
                            spec = DBSpectrumPeer.retrieveByPK(new NumberKey((String)results.get(startMolecule + molNumber)));
                            mol=spec.getDBMolecule();
                        }
                        runData.getSession().setAttribute("moldetails",mol);
                    }
                    if(runData.getParameters().getString("stereolink")!=null)
                        mol = DBMoleculePeer.retrieveByPK(new NumberKey(runData.getParameters().getString("stereonumber")));
                    ValuesForVelocityBean lastSearch = null;
                    Map<String,List<CMLPeak>> subspectrumSearches=new HashMap<String,List<CMLPeak>>();
                    if(ResultAction.getSearches(runData.getSession())!=null){
                        for(int i=0;i<ResultAction.getSearches(runData.getSession()).length;i++){
                            ValuesForVelocityBean search = ((ValuesForVelocityBean) GeneralUtils.getVectorWithSessionBindingAttributeFromSession(runData.getSession(), NmrshiftdbConstants.SEARCHHISTORY).get(ResultAction.getSearches(runData.getSession())[i]));
                            if(search.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)){
                                subspectrumSearches.put(search.getRange().substring(0, search.getRange().indexOf("-")),ParseUtils.parseSpectrumFromSpecFile(search.getRangeForSpectrum()));
                            }
                        }
                    }
                    if (!action.equals("showDetailsFromHome") && !action.equals("visitbookmark") && !action.equals("showprivatebykey") && runData.getSession().getAttribute("browseallall")==null && runData.getSession().getAttribute("searches")!=null) {
                        lastSearch = (ValuesForVelocityBean) GeneralUtils.getVectorWithSessionBindingAttributeFromSession(runData.getSession(), NmrshiftdbConstants.SEARCHHISTORY).get(ResultAction.getSearches(runData.getSession())[ResultAction.getSearches(runData.getSession()).length - 1]);
                    } else {
                        lastSearch = new ValuesForVelocityBean("test", "test", "test");
                    }
                    if(mol!=null){
                    	StringBuffer filename=new StringBuffer(Integer.toString(mol.getMoleculeId()));
                        if(runData.getSession().getAttribute("applet")!=null && ((String)runData.getSession().getAttribute("applet")).equals("applet")){ 
                            filename.append("_1");
                        }else {
                            filename.append("_0");
                        }
                        if(runData.getParameters().getString("displayall")!=null) {
                            filename.append("_1");
                        }else {
                            filename.append("_0");
                        }
                        //This is for the keyword tabs
                        @SuppressWarnings("unchecked")
                        List<Integer> showtabs=(List<Integer>)runData.getSession().getAttribute("showtabs");
                        if(showtabs==null || runData.getParameters().getString("newmol", "false").equals("true")){
                            showtabs=new ArrayList<Integer>();
                            showtabs.add(0);
                            for(int i=0;i<20;i++)
                                showtabs.add(0);
                        }else if(showtabs.size()<20+1){
                            //+1 since counter in velocity start with 1
                            for(int i=0;i<20-showtabs.size()+1;i++)
                                showtabs.add(0);
                        }
                        //for unknown reasons, we still got out of bounds exceptions sometimes
                        for(int i=showtabs.size();i<100;i++){
                            showtabs.add(0);
                        }
                        runData.getSession().setAttribute("showtabs",showtabs);
                        if(runData.getParameters().getString("tab")!=null){
                            String[] parts=runData.getParameters().getString("tab").split("_");
                            //it seems sometimes there are requests with a wrong format, I have no idea why
                            //in order not to spam the errors I check this
                            if(parts.length==2)
                                showtabs.set(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));		        	  
                        }
                        context.put("innertabs",showtabs);
                        //This is for the keyword tabs
                        Integer showoutertab=(Integer)runData.getSession().getAttribute("showoutertab");
                        if(showoutertab==null)
                            showoutertab=0;
                        if(runData.getParameters().get("showoutertab")!=null)
                            showoutertab=Integer.parseInt(runData.getParameters().get("showoutertab").substring(2));
                        runData.getSession().setAttribute("showoutertab",showoutertab);
                        filename.append("_"+Integer.toString(showoutertab));
                        for(int i=0;i<showtabs.size();i++) {
                        	filename.append("_"+Integer.toString(showtabs.get(i)));
                        }
                        File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), "WEB-INF/templates/vm/portlets/html/result/detailcache/"+filename+".vm"));
                        if(outputFile.exists() && runData.getUser().getUserName().equals("anon") && !(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) && runData.getParameters().getString("coordinateselect")==null) {
                            //for unknown reasons, giving the original file as templates fails, we make a copy
                            int filenumber=new Date().getSeconds();
                            File templateFile=new File(ServletUtils.expandRelative(runData.getServletConfig(), "WEB-INF/templates/vm/portlets/html/result/details/details"+filenumber+".vm"));
                            FileInputStream in = new FileInputStream(outputFile);
                            FileOutputStream out = new FileOutputStream(templateFile);
                            IOUtils.copy(in, out);
                            in.close();
                            out.close();
                            context.put("sessionidext",runData.getSession().getId());
                            context.put("template","result/details/details"+filenumber);
                        	return;
                        }else {
	                        if(runData.getSession().getAttribute("lastmoleculeviewed")==null || ((Integer)runData.getSession().getAttribute("lastmoleculeviewed")).intValue()!=mol.getMoleculeId()){
	                            newStructureView=true;
	                        }
	                        context.put("data", runData);
	                        context.put("molNumber", molNumber + "");
	                        context.put("constants",new NmrshiftdbConstants());
	                        context.put("random", new java.util.Random());
	                        context.put("easylink",mol.getEasylink(runData.getRequest()));
	                        
	                        context.put("genutil", GeneralUtils.getInstance());
	                        context.put("publishserver", GeneralUtils.getNmrshiftdbProperty("publishserver", runData));
	                        if(action.equals("showDetailsFromHome"))
	                            context.put("lastaction",action);
	                        else
	                            context.put("lastaction","showDetails");
	                        context.put("startmolecule",""+startMolecule);
	                        if((newStructureView && mol.selectSpectraByUser(runData.getUser(),true,false,null).size()>0 || action.equals("showprivatebykey") || (runData.getParameters().getString("unreviewed")!=null && mol.selectSpectraByUser(runData.getUser(),false,true,null).size()>0)) ||
	                           (!newStructureView && runData.getSession().getAttribute("hasspectra")!=null)){
	                            List<DBSpectrum> spectra = null;
	                            context.put("mol", mol);
	                            List<CMLPeak> numberSpectrum = null;
	                            StringBuffer searchspectrum = null;
	                            //we do not by type after spectrum search if this was disabled
	                            if(runData.getParameters().get("showalltypes")!=null){
	                            	runData.getSession().setAttribute("showalltypes", "showalltypes");
	                            	newStructureView=true;
	                            }
	                            boolean showalltypes=runData.getSession().getAttribute("showalltypes")!=null;
	                            if(newStructureView){
	                                //We count this as a view
	                                updateViews=mol.getMoleculeId();
	                                //If the search action was a spectrum search for we filter by spectrum type
                                    if (!showalltypes && (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM))) {
                                		String spectrumtype = lastSearch.getRange().substring(0,lastSearch.getRange().indexOf("-"));
                                    	if(!spectrumtype.equals("13C/1H correlation")){
	                                    	spectra = mol.selectSpectraByUser(runData.getUser(), false, false, spectrumtype);
		                                    context.put("showtypebutton",spectrumtype);
                                    	}else {
                                    		spectra = mol.selectSpectraByUser(runData.getUser(), false, true, null);
                                    	}
	                                } else if(action.equals("showprivatebykey")){
	                                	DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(runData.getParameters().getString("molNumber").substring(runData.getParameters().getString("molNumber").indexOf(":")+1)));
	                                	spectra=new ArrayList<DBSpectrum>();
	                                	if(spectrum.getReviewKey().equals(runData.getParameters().get("viewkey")))
	                                		spectra.add(spectrum);
	                                	context.put("reviewkey", spectrum.getReviewKey());
	                                }else {
	                                    if (lastSearch!=null && lastSearch.getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
	                                        spectra = mol.selectSpectraByUser(runData.getUser(), false, true, null);
	                                    } else {
	                                        if(runData.getParameters().getString("unreviewed")!=null){
	                                            spectra = mol.selectSpectraByUser(runData.getUser(), false, false, null);
	                                        }else{
	                                            spectra = mol.selectSpectraByUser(runData.getUser(), true, false, null);
	                                        }
	                                    }
	                                }
	                                //initialize the mol images
	                                for (int i = 0; i < spectra.size(); i++) {
	                                    if(UrlTool.doImageCache() && !action.equals("showprivatebykey") && mol.isPublic() && !((DBSpectrum) spectra.get(i)).getReviewFlag().equals(NmrshiftdbConstants.PRIVATE)){
	                                        ((DBSpectrum) spectra.get(i)).setMolImage("images/molecules/large/"+mol.getMoleculeId()+"_"+((DBSpectrum) spectra.get(i)).getSpectrumId()+".jpeg");
	                                    }else{
	                                        StringBuffer sb = new StringBuffer("download/NmrshiftdbServlet/").append(mol.getMoleculeId()).append("_").append(((DBSpectrum) spectra.get(i)).getSpectrumId()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&spectrumid=").append(((DBSpectrum) spectra.get(i)).getSpectrumId()).append("&format=jpeg&size=large&backcolor=");
	                                        //TODO how was this supposed to work? if(spectrum.getNmrshiftdbUser().getLabgroupId()!=null && spectrum.getNmrshiftdbUser().getLabgroupId()>0)
	                                        //    sb.append(ResultAction.LABGROUP_BACKGROUND_COLOR+"&shownumbers=false");
	                                        //else
	                                            sb.append(ResultAction.STANDARD_BACKGROUND_COLOR+"&shownumbers=false");
	                                        if(action.equals("showprivatebykey") &&  ((DBSpectrum) spectra.get(i)).getReviewKey().equals(runData.getParameters().get("viewkey")))
	                                        	sb.append("&viewkey="+runData.getParameters().get("viewkey"));
	                                        ((DBSpectrum) spectra.get(i)).setMolImage(sb.toString());
	                                    }
	                                }
	                                if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)) {
	                                    numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
	                                    DBSignal[] searchSpectrum = new DBSignal[numberSpectrum.size()];
	                                    for (int i = 0; i < numberSpectrum.size(); i++) {
	                                        DBSignal sig = new DBSignal();
	                                        DBShift[] shifts = new DBShift[1];
	                                        DBShift shift = new DBShift();
	                                        shift.setAxis(1);
	                                        shift.setValue((float)numberSpectrum.get(i).getXValue());
	                                        shifts[0] = shift;
	                                        sig.setDBShiftsArray(shifts);
	                                        sig.addDBShift(shift);
	                                        sig.setIntensity((float)numberSpectrum.get(i).getYValue());
	                                        searchSpectrum[i] = sig;
	                                    }
	                                    boolean is2d=false;
	                                    List<CMLPeak> searchspec=ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
	                                    if(runData.getSession().getAttribute("useintensities")==null){
	                                        for(int i=0;i<searchspec.size();i++){
	                                            if(searchspec.get(i).getYValue()>0)
	                                                is2d=true;
	                                        }
	                                    }
	                                    searchspectrum=new StringBuffer();
	                                    for(int i=0;i<searchspec.size();i++){
	                                        searchspectrum.append(searchspec.get(i).getXValue()+";"+searchspec.get(i).getYValue()+"|");
	                                    }
	                                    if(is2d){
	                                        context.put("searchspectrum", searchspectrum.toString());
	                                    }
	                                }else if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)) {
	                                    context.put("searchspectrum", runData.getSession().getAttribute("searchspectrum"));
	                                }
	                                Collections.sort(spectra, new SpectraTypeComparator());
	                                //If a spectrum search was done we init the similarities of the spectra and indicate this with the viewingaction attribute in the context
	                                if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)){
		                                String spectrumtype = lastSearch.getRange().substring(0,lastSearch.getRange().indexOf("-"));
		                                numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
	                                	if(!spectrumtype.equals("13C/1H correlation")) {
		                                    Collections.sort(numberSpectrum, new SignalComparator(false));
		                                    StringBuffer searchSpectrumForApplet = new StringBuffer();
		                                    for (int i = 0; i < numberSpectrum.size(); i++) {
		                                        searchSpectrumForApplet.append(numberSpectrum.get(i).getXValue() + "|");
		                                    }
		                                    context.put("viewingaction", "searchbyspectrum");
		                                    context.put("searchtype",lastSearch.getDisplayText());
		                                    for (int i = 0; i < spectra.size(); i++) {
		                                        ((DBSpectrum) spectra.get(i)).initSimilarity(subspectrumSearches.get(((DBSpectrum) spectra.get(i)).getDBSpectrumType().getName())!=null ? subspectrumSearches.get(((DBSpectrum) spectra.get(i)).getDBSpectrumType().getName()) : numberSpectrum, lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM));
		                                    }
		                                    context.put("searchSpectrumForApplet", searchSpectrumForApplet.toString());
                                        }else {
		                                    context.put("viewingaction", "searchbyspectrum");
		                                    context.put("searchtype","13C/1H correlation, "+lastSearch.getDisplayText());
		                                    for (int i = 0; i < spectra.size(); i++) {
		                                         numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
		                                         for(CMLPeak peak : numberSpectrum) {
		                                             if(((DBSpectrum) spectra.get(i)).getDBSpectrumType().getName().equals("13C")) {
		                                    	         peak.setYValue(0);
		                                    	     }else {
		                                    	         peak.setXValue(peak.getYValue());
		                                    	         peak.setYValue(0);
		                                    	    }
		                                    	 }
		                                    	 if(((DBSpectrum) spectra.get(i)).getDBSpectrumType().getName().equals("13C") || ((DBSpectrum) spectra.get(i)).getDBSpectrumType().getName().equals("1H")) {
		                                    	     ((DBSpectrum) spectra.get(i)).initSimilarity(numberSpectrum, lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM));
		                                    	 }
		                                    }
	                                	}
	                                }
	                                Collections.sort(spectra, new DBSpectrumComparator());
	                            }else{
	                                spectra=(List<DBSpectrum>)runData.getSession().getAttribute("spectra");
	                                context.put("searchspectrum", runData.getSession().getAttribute("searchspectrum"));
	                            }
	                            String typename="";
	                            if(runData.getParameters().getString("spectrumType")!=null && !runData.getParameters().getString("spectrumType").equals("All")){
	                                typename=(String)runData.getParameters().getString("spectrumType");
	                                Iterator<DBSpectrum> it=spectra.iterator();
	                                while(it.hasNext()){
	                                    DBSpectrum spectrum=(DBSpectrum)it.next();
	                                    if(!spectrum.getDBSpectrumType().getName().equals(typename))
	                                        it.remove();
	                                }
	                                typename="(type: "+typename+") ";
	                            }
	                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)) {
	                                if (spectra.size() == 1) {
	                                    context.put("header", "1 "+typename);
	                                } else {
	                                    context.put("header", spectra.size() + " "+typename);
	                                }
	                            } else {
	                                if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
	                                    if (spectra.size() == 1) {
	                                        context.put("header", "1 submitted by you");
	                                    } else {
	                                        context.put("header", spectra.size() + " submitted by you");
	                                    }
	                                } else {
	                                    if (spectra.size() == 1) {
	                                        context.put("header", "1 "+typename);
	                                    } else {
	                                        context.put("header", spectra.size() + " "+typename);
	                                    }
	                                }
	                            }
	                            if(runData.getUser().hasLoggedIn()){
	                                Criteria crit=new Criteria();
	                                crit.addJoin(DBSamplePeer.SAMPLE_ID, DBRawFilePeer.SAMPLE_ID);
	                                if(!TurbineUserPeer.retrieveByPK(Integer.parseInt(((BaseJetspeedUser)runData.getUser()).getUserId())).isLabgroupWorker()){
	                                    crit.add(DBSamplePeer.USER_ID,((BaseJetspeedUser)runData.getUser()).getUserId());
	                                }
	                                context.put("samples", mol.getDBSamples(crit));
	                            }else{
	                                //The anonymous user doesn't see any samples
	                                context.put("samples", new ArrayList<DBSample>());
	                            }
	                            for (int i = 0; i < spectra.size(); i++) {
	                                ((DBSpectrum) spectra.get(i)).runData=runData;
	                            }
	                            if (action.equals("showDetailsFromHome")) {
	                                context.put("viewingaction", "fromlast");
	                            }
	                            context.put("lastsearch", lastSearch);
	                            if(runData.getParameters().getString("markspectrum")!=null){
	                                try{
	                                    int mark=new Integer(runData.getParameters().getString("mark")).intValue();
	                                    if(mark<1 || mark>10)
	                                        throw new NumberFormatException();
	                                    DBSpectrumPeer.retrieveByPK(new NumberKey(runData.getParameters().getString("spectrum"))).addMark(mark,runData.getUser(),runData.getParameters().getString("comment"), runData.getServletConfig());
	                                    context.put("message","You marked the spectrum with "+runData.getParameters().getString("mark"));
	                                }catch(NmrshiftdbException ex){
	                                    context.put("message",ex.getMessage());
	                                }
	                                catch(NumberFormatException ex){
	                                    context.put("message","The mark must be in the range from 1 to 10!");
	                                }
	                            }else{
	                                context.put("message","");
	                            }
	                            context.put("displayall", runData.getParameters().getString("displayall")!=null ? "true" : "false");
	                            context.put("displayallurl", runData.getParameters().getString("displayall")!=null ? "&displayall=displayall" : "");
	                            //The mol is put into the MView applet via a file
	                            int set=1;
	                            if(runData.getParameters().getString("coordinateselect")!=null){
	                                StringTokenizer st=new StringTokenizer(runData.getParameters().getString("coordinateselect"),".");
	                                set=Integer.parseInt(st.nextToken());
	                                //this is to prevent xss
	                                Float.parseFloat(runData.getParameters().getString("coordinateselect"));
	                                context.put("choosenset",runData.getParameters().getString("coordinateselect"));
	                            }
	                            List<DBCoordinateSet2d> sets2 = ((DBAtom) mol.getDBAtoms().get(0)).getDBCoordinateSet2ds();
	                            List<DBCoordinateSet3d> sets3 = ((DBAtom) mol.getDBAtoms().get(0)).getDBCoordinateSet3ds();
	                            for (int i = 0; i < sets2.size(); i++) {
	                                DBCoordinateSetType type = ((DBCoordinateSet2d) sets2.get(i)).getDBCoordinateSetType();
	                                if(type.getCoordinateSetTypeId()==set)
	                                    context.put("coords","2d");
	                            }
	                            for (int i = 0; i < sets3.size(); i++) {
	                                DBCoordinateSetType type = ((DBCoordinateSet3d) sets3.get(i)).getDBCoordinateSetType();
	                                if(type.getCoordinateSetTypeId()==set) {
	                                    context.put("coords","3d");
	                                    context.put("molfile", "download/NmrshiftdbServlet/"+mol.getMoleculeId()+".mol;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+ mol.getMoleculeId()+"&coordsetid="+set);
	                                }
	                            }
	                            context.put("data",runData);
	                            //This is for the keyword tabs
	                            showoutertab=(Integer)runData.getSession().getAttribute("showoutertab");
	                            if(showoutertab==null)
	                                showoutertab=0;
	                            if(runData.getParameters().get("showoutertab")!=null)
	                                showoutertab=Integer.parseInt(runData.getParameters().get("showoutertab").substring(2));
	                            List<Integer> outertabs=new ArrayList<Integer>();
	                            Map<String,List<DBSpectrum>> spectralists = expandSpectraByKeyword(spectra);
	                   			if(showoutertab>=spectralists.size())
	                                showoutertab=0;
	                            if(runData.getParameters().get("dataset")!=null){
	                              String tab=runData.getParameters().get("dataset");
	                              int i=0;
	                              for(String key : spectralists.keySet()){
	                                if(key.replace('/', '_').equals(tab)){
	                                  showoutertab=i;
	                                  break;
	                                }
	                                i++;
	                              }
	                              //if it is a reviewer, we add private spectra
	                              if(runData.getParameters().get("viewkey")!=null) {
	                            	String datasetkeyword=tab.split(";")[0];
	  	                            String datasetsolvent=tab.split(";")[1];
	  	                            StringBuffer sql=new StringBuffer("SELECT DATASET_REVIEW_KEY.REVIEW_KEY FROM NMR_CONDITION, DATASET_REVIEW_KEY, ");
	  	                            StringTokenizer st=new StringTokenizer(datasetkeyword,"_");
	  	                            i=1;
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
	  	                            sql.append("NMR_CONDITION.CONDITION_ID=DATASET_REVIEW_KEY.CONDITION_ID AND DATASET_REVIEW_KEY.MOLECULE_ID="+mol.getMoleculeId()+" AND NMR_CONDITION.VALUE='"+GeneralUtils.escape(datasetsolvent,false)+"'");
	  	                            List<Record> rkeys = DBDatasetReviewKeyPeer.executeQuery(sql.toString());
	  	                            if(rkeys.size()>0 &&runData.getParameters().getInt("viewkey")==rkeys.get(0).getValue(1).asInt()) {
	  	                            	spectralists=new HashMap<String,List<DBSpectrum>>();
	  	                            	spectralists.put(tab, new ArrayList<DBSpectrum>());
	                                    List<DBSpectrum> spectraall = mol.selectSpectraByUser(runData.getUser(), false, false, null);
	  	                            	for(DBSpectrum spectrum : spectraall) {
	  	                                  //initialize the mol images
	  	                                      if(UrlTool.doImageCache() && mol.isPublic()){
	  	                                          spectrum.setMolImage("images/molecules/large/"+mol.getMoleculeId()+"_"+spectrum.getSpectrumId()+".jpeg");
	  	                                      }else{
	  	                                          StringBuffer sb = new StringBuffer("download/NmrshiftdbServlet/").append(mol.getMoleculeId()).append("_").append(spectrum.getSpectrumId()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&spectrumid=").append(spectrum.getSpectrumId()).append("&format=jpeg&size=large&backcolor=");
	  	                                          sb.append(ResultAction.STANDARD_BACKGROUND_COLOR+"&shownumbers=false");
	  	                                          if(action.equals("showprivatebykey"))
	  	                                          	sb.append("&viewkey="+spectrum.getReviewKey());
	  	                                          spectrum.setMolImage(sb.toString());
	  	                                      }
	  	                            		spectralists.get(tab).add(spectrum);
	  	                            	}
	  	                            }
	                              }
	                            }
	                            outertabs.add(showoutertab);
	                            context.put("tab",outertabs);
	                            runData.getSession().setAttribute("showoutertab",showoutertab);
	                            context.put("spectra", spectralists.get(spectralists.keySet().toArray()[showoutertab]));
	                            context.put("spectralists", spectralists);
	                            if(((String)spectralists.keySet().toArray()[showoutertab]).split("/").length>1){
		                            context.put("easylinkdataset",mol.getEasylink(runData.getRequest())+"/dataset/"+URLEncoder.encode(((String)spectralists.keySet().toArray()[showoutertab]).replace('/', '_'),"UTF-8"));
		                            String datasetkeyword=((String)spectralists.keySet().toArray()[showoutertab]).split("/")[0];
		                            String datasetsolvent=((String)spectralists.keySet().toArray()[showoutertab]).split("/")[1];
		                            StringBuffer sql=new StringBuffer("SELECT DATASET_DOI.DATASET_DOI_ID, DATASET_DOI.DOI FROM NMR_CONDITION, DATASET_DOI, ");
		                            StringTokenizer st=new StringTokenizer(datasetkeyword,"_");
		                            int i=1;
		                            while(st.hasMoreTokens()) {
		                            	if(i>1)
		                            		sql.append(", ");
		                            	sql.append("DATASET_DOI_KEYWORD as d"+i+", KEYWORD as k"+i+" ");
		                            	st.nextToken();
		                            	i++;
		                            }
		                            sql.append("WHERE ");
		                            i=1;
		                            st=new StringTokenizer(datasetkeyword,"_");
		                            while(st.hasMoreTokens()) {
		                            	sql.append("DATASET_DOI.DATASET_DOI_ID=d"+i+".DATASET_DOI_ID and d"+i+".KEYWORD_ID=k"+i+".KEYWORD_ID and k"+i+".KEYWORD='"+st.nextToken()+"' and ");
		                            	i++;
		                            }
		                            sql.append("NMR_CONDITION.CONDITION_ID=DATASET_DOI.CONDITION_ID AND DATASET_DOI.MOLECULE_ID="+mol.getMoleculeId()+" AND NMR_CONDITION.VALUE='"+GeneralUtils.escape(datasetsolvent, false)+"'");
		                            List<Record> dois = DBDatasetDoiPeer.executeQuery(sql.toString());
		                            if(dois.size()>0)
		                            	context.put("datasetdoi",dois.get(0).getValue(2).asString());
		                            context.put("datasetspecid",spectralists.get((String)spectralists.keySet().toArray()[showoutertab]).get(0).getSpectrumId());
		                            boolean hasPrivate=false;
		                            for(DBSpectrum spectrum : spectralists.get(spectralists.keySet().toArray()[showoutertab])) {
		                            	if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE))
		                            		hasPrivate=true;
		                            }
		                            if(hasPrivate){
		                            	DBSpectrum spectrum=spectralists.get(spectralists.keySet().toArray()[showoutertab]).get(0);
		                            	int viewkey=spectrum.getDatasetReviewKey(datasetkeyword, datasetsolvent);
		                            	if(viewkey==0) {
		                            		 viewkey = new Double("" + Math.random() * 100000).intValue();
		                            		 DBDatasetReviewKey rk=new DBDatasetReviewKey();
		                            		 rk.setDBCondition(DBConditionPeer.getByValue(datasetsolvent).get(0));
		                            		 rk.setDBMolecule(spectrum.getDBMolecule());
		                            		 rk.setReviewKey(""+viewkey);
		                            		 rk.save();
		                            		 st=new StringTokenizer(datasetkeyword,"_");
		     	                            	while(st.hasMoreTokens()) {
		     	                            		DBDatasetReviewKeyDBKeyword rkk=new DBDatasetReviewKeyDBKeyword();
		     	                            		rkk.setDBKeyword(DBKeywordPeer.doSelectByKeyword(st.nextToken()));
		     	                            		rkk.setDBDatasetReviewKey(rk);
		     	                            		rkk.save();
		     	                            	}
		                            	}
		                            	context.put("easylinkdatasetreview",mol.getEasylink(runData.getRequest())+"/dataset/"+URLEncoder.encode(((String)spectralists.keySet().toArray()[showoutertab]).replace('/', ';'),"UTF-8")+"?viewkey="+viewkey);
		                            	context.put("submitdatasetvorreview",URLEncoder.encode(((String)spectralists.keySet().toArray()[showoutertab]).replace('/', ';'),"UTF-8"));
		                            }
		                        }
	                            //Check if change all literatures should be possible
	                            List<DBSpectrum> currentspectra = spectralists.get(spectralists.keySet().toArray()[showoutertab]);
	                            int userid=currentspectra.get(0).getUserId();
	                            boolean allspectrabysameuser=true;
	                            for(DBSpectrum spectrum : currentspectra){
	                                if(spectrum.getUserId()!=userid){
	                                    allspectrabysameuser=false;
	                                    break;
	                                }
	                            }
	                            if(userid!=Integer.parseInt(((BaseJetspeedUser) runData.getUser()).getUserId()))
	                                allspectrabysameuser=false;
	                            if(currentspectra.size()==1)
	                                allspectrabysameuser=false;
	                            context.put("showliteratureedit",allspectrabysameuser);
	                            if(action.equals("showDetailsFromHome") || action.equals("showprivatebykey")){
	                                StringBuffer query=new StringBuffer();
	                                @SuppressWarnings("unchecked")
	                                Enumeration<String> parameternames=runData.getParameters().getRequest().getParameterNames();
	                                while(parameternames.hasMoreElements()){
	                                    String paramname=(String)parameternames.nextElement();
	                                    String param=runData.getParameters().getString(paramname);
	                                    if(!paramname.equals("tab") && !paramname.equals("updown") && !paramname.equals("newmol") && !paramname.equals("ordercolumn") && !paramname.equals("showoutertab"))
	                                        query.append(paramname+"="+param+"&");
	                                }
	                                if(query.length()>0)
	                                    context.put("query",query.substring(0,query.length()-1));
	                                else
	                                    context.put("query","");
	                            }else{
	                                String type="All";
	                                if(runData.getSession().getAttribute("selectedType")!=null)
	                                    type=(String)runData.getSession().getAttribute("selectedType");
	                                String query="startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&molNumber="+(runData.getParameters().getString("molNumber")==null ? "0" : runData.getParameters().getString("molNumber"))+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : "");
	                                context.put("query",query.toString());
	                            }
	                            int ordercolumn=-1;
	                            if(runData.getParameters().get("ordercolumn")!=null){
	                                ordercolumn=Integer.parseInt(runData.getParameters().get("ordercolumn"));
	                            }
	                            context.put("ordercolumn",ordercolumn);
	                            boolean updown=false;
	                            if(runData.getParameters().get("updown")!=null && runData.getParameters().get("updown").equals("up")){
	                                updown=true;
	                            }
	                            //handle the export templates
	                            File templatedir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/templates/vm/exporttemplates"));
	                            context.put("templates",templatedir.listFiles());
	                            UrlTool urltool = new UrlTool();
	                            urltool.init(runData);
	                            context.put("baseurl", urltool.getBaseUrl());
	                            context.put("link", JetspeedLinkFactory.getInstance(runData));
	                            context.put("updownstring",updown ? "down" : "up");
	                            context.put("updown", updown);
	                            context.put("template","result/details/details");
	                            if(newStructureView){
	                                runData.getSession().setAttribute("hasspectra",true);
	                                runData.getSession().setAttribute("lastmoleculeviewed",mol.getMoleculeId());
	                                runData.getSession().setAttribute("spectra",spectra);
	                                if(searchspectrum!=null)
	                                    runData.getSession().setAttribute("searchspectrum",searchspectrum.toString());
	                            }
	                            context.put("sessionidext","$sessionidext");
	                            if(runData.getUser().getUserName().equals("anon") && !(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM))) {
	                                FileWriter w = new FileWriter(outputFile);
	                                try
	                                {
	                                    Velocity.mergeTemplate("portlets/html/result/details/details.vm", "UTF-8", context, w);
	                                }
	                                catch (Exception ex)
	                                {
	                                    GeneralUtils.logError(ex, "Error rendering details", runData, true);
	                                }
	                                finally {
	                                    w.close();
	                                }
	                            }
	                            context.put("tab",outertabs);
	                            context.put("sessionidext",runData.getSession().getId());
	                            return;
	                        }else{
	                            //structure only
	                            context.put("mol", mol);
	                            int set=1;
	                            if(runData.getParameters().getString("coordinateselect")!=null){
	                                StringTokenizer st=new StringTokenizer(runData.getParameters().getString("coordinateselect"),".");
	                                set=Integer.parseInt(st.nextToken());
	                                //this is to prevent xss
	                                Float.parseFloat(runData.getParameters().getString("coordinateselect"));
	                                context.put("choosenset",runData.getParameters().getString("coordinateselect"));
	                            }
	                            List<DBCoordinateSet2d> sets2 = ((DBAtom) mol.getDBAtoms().get(0)).getDBCoordinateSet2ds();
	                            List<DBCoordinateSet3d> sets3 = ((DBAtom) mol.getDBAtoms().get(0)).getDBCoordinateSet3ds();
	                            for (int i = 0; i < sets2.size(); i++) {
	                                DBCoordinateSetType type = ((DBCoordinateSet2d) sets2.get(i)).getDBCoordinateSetType();
	                                if(type.getCoordinateSetTypeId()==set)
	                                    context.put("coords","2d");
	                            }
	                            for (int i = 0; i < sets3.size(); i++) {
	                                DBCoordinateSetType type = ((DBCoordinateSet3d) sets3.get(i)).getDBCoordinateSetType();
	                                if(type.getCoordinateSetTypeId()==set)
	                                    context.put("coords","3d");
	                            }
	                            if(runData.getSession().getAttribute("applet")!=null && ((String)runData.getSession().getAttribute("applet")).equals("applet")){ 
	                                context.put("molfile", "download/NmrshiftdbServlet/"+mol.getMoleculeId()+".mol;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+ mol.getMoleculeId()+"&coordsetid="+set);
	                            }else{
	                                context.put("image", "download/NmrshiftdbServlet/"+mol.getMoleculeId()+".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+ mol.getMoleculeId() +"&format=jpeg&size=350x250&backcolor=12632256");
	                            }
	                            Criteria crit=new Criteria();
	                            crit.addJoin(DBSamplePeer.SAMPLE_ID, DBRawFilePeer.SAMPLE_ID);
	                            context.put("samples", mol.getDBSamples(crit));
	                            //handle the export templates
	                            File templatedir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/templates/vm/exporttemplates"));
	                            context.put("templates",templatedir.listFiles());
	                            context.put("link", JetspeedLinkFactory.getInstance(runData));
	                            context.put("template","result/details/struconly");
	                            context.put("message","");
                                context.put("sessionidext","$sessionidext");
                                if(runData.getUser().getUserName().equals("anon")) {
	                        		FileWriter w = new FileWriter(outputFile);
	                        		try
	                        		{
	                        			Velocity.mergeTemplate("portlets/html/result/details/details.vm", "ISO-8859-1", context, w);
	                        		}
	                        		catch (Exception ex)
	                        		{
	                        			GeneralUtils.logError(ex, "Error rendering details", runData, true);
	                        		}
	                        		finally {
	                        		    w.close();
	                        		}
                                }
	                            context.put("sessionidext",runData.getSession().getId());
	                            return;
	                        }
                        }
                    }else if(mixture!=null || (spec!=null && spec.getDBMixture()!=null)){
                        //mixture
                        if(spec!=null && spec.getDBMixture()!=null)
                            mixture=spec.getDBMixture();
                        List<DBSpectrum> spectra = null;
                        List<CMLPeak> numberSpectrum = null;
                        //If the search action was a subspectrum search we do not show all spectra but only these matching the fingerprint
                        if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)) {
                            numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
                            DBSignal[] searchSpectrum = new DBSignal[numberSpectrum.size()];
                            for (int i = 0; i < numberSpectrum.size(); i++) {
                                DBSignal sig = new DBSignal();
                                DBShift[] shifts = new DBShift[1];
                                DBShift shift = new DBShift();
                                shift.setAxis(1);
                                shift.setValue((float)numberSpectrum.get(i).getXValue());
                                shifts[0] = shift;
                                sig.setDBShiftsArray(shifts);
                                sig.addDBShift(shift);
                                sig.setIntensity((float)numberSpectrum.get(i).getYValue());
                                searchSpectrum[i] = sig;
                            }
                            BigInteger fingerprint = BigInteger.valueOf(SpectrumUtils.makeFingerprint(searchSpectrum, false));
                            spectra = mixture.selectSpectra(fingerprint);
                        } else {
                            if (lastSearch!=null && lastSearch.getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
                                spectra = mixture.selectSpectraByUser(runData.getUser(), false);
                            } else {
                                if(runData.getParameters().getString("unreviewed")!=null){
                                    spectra = mixture.selectSpectraByUser(runData.getUser(), false);
                                }else{
                                    spectra = mixture.selectSpectra();
                                }
                            }
                        }
                        String typename="";
                        if(runData.getParameters().getString("spectrumType")!=null && !runData.getParameters().getString("spectrumType").equals("All")){
                            typename=(String)runData.getParameters().getString("spectrumType");
                            Iterator<DBSpectrum> it=spectra.iterator();
                            while(it.hasNext()){
                                DBSpectrum spectrum=it.next();
                                if(!spectrum.getDBSpectrumType().getName().equals(typename))
                                    it.remove();
                            }
                            typename="(type: "+typename+") ";
                        }
                        if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM)) {
                            if (spectra.size() == 1) {
                                context.put("header", "1 "+typename);
                            } else {
                                context.put("header", spectra.size() + " "+typename);
                            }
                        } else {
                            if (lastSearch.getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
                                if (spectra.size() == 1) {
                                    context.put("header", "1 submitted by you");
                                } else {
                                    context.put("header", spectra.size() + " submitted by you");
                                }
                            } else {
                                if (spectra.size() == 1) {
                                    context.put("header", "1 "+typename);
                                } else {
                                    context.put("header", spectra.size() + " "+typename);
                                }
                            }
                        }
                        //This is for the keyword tabs
                        Integer showoutertab=(Integer)runData.getSession().getAttribute("showoutertab");
                        if(showoutertab==null)
                            showoutertab=0;
                        if(runData.getParameters().get("showoutertab")!=null)
                            showoutertab=Integer.parseInt(runData.getParameters().get("showoutertab").substring(2));
                        runData.getSession().setAttribute("showoutertab",showoutertab);
                        List<Integer> outertabs=new ArrayList<Integer>();
                        outertabs.add(showoutertab);
                        context.put("tab",outertabs);
                        Map<String,List<DBSpectrum>> spectralists = expandSpectraByKeyword(spectra);
                        context.put("spectra", spectralists.get(spectralists.keySet().toArray()[showoutertab]));
                        context.put("spectralists", spectralists);
                        for (int i = 0; i < spectra.size(); i++) {
                            ((DBSpectrum) spectra.get(i)).runData=runData;
                        }
                        String spectrumtype = lastSearch.getRange().substring(0,lastSearch.getRange().indexOf("-"));
                        //If a spectrum search was done we init the similarities of the spectra and indicate this with the viewingaction attribute in the context
                        if ((lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) && !spectrumtype.equals("13C/1H correlation")) {
                            numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
                            Collections.sort(numberSpectrum, new SignalComparator(false));
                            StringBuffer searchSpectrumForApplet = new StringBuffer();
                            for (int i = 0; i < numberSpectrum.size(); i++) {
                                searchSpectrumForApplet.append(numberSpectrum.get(i).getXValue() + "|");
                            }
                            context.put("viewingaction", "searchbyspectrum");
                            context.put("searchtype",lastSearch.getDisplayText());
                            for (int i = 0; i < spectra.size(); i++) {
                                ((DBSpectrum) spectra.get(i)).initSimilarity(numberSpectrum, lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM));
                            }
                            context.put("searchSpectrumForApplet", searchSpectrumForApplet.toString());
                        }
                        if (action.equals("showDetailsFromHome")) {
                            context.put("viewingaction", "fromlast");
                        }
                        context.put("lastsearch", lastSearch);
                        Collections.sort(spectra, new DBSpectrumComparator());
                        context.put("molNumber", molNumber + "");
                        context.put("startMolecule", startMolecule + "");
                        context.put("constants",new NmrshiftdbConstants());
                        context.put("easylink",mixture.getEasylink(runData.getRequest()));
                        context.put("genutil", GeneralUtils.getInstance());
                        context.put("mixture",mixture);
                        if(action.equals("showDetailsFromHome"))
                            context.put("lastaction",action);
                        else
                            context.put("lastaction","showDetails");
                        context.put("startmolecule",""+startMolecule);
                        if(runData.getParameters().getString("markspectrum")!=null){
                            try{
                                int mark=new Integer(runData.getParameters().getString("mark")).intValue();
                                if(mark<1 || mark>10)
                                    throw new NumberFormatException();
                                DBSpectrumPeer.retrieveByPK(new NumberKey(runData.getParameters().getString("spectrum"))).addMark(mark,runData.getUser(),runData.getParameters().getString("comment"), runData.getServletConfig());
                                context.put("message","You marked the spectrum with "+runData.getParameters().getString("mark"));
                            }catch(NmrshiftdbException ex){
                                context.put("message",ex.getMessage());
                            }
                            catch(NumberFormatException ex){
                                context.put("message","The mark must be in the range from 1 to 10!");
                            }
                        }else{
                            context.put("message","");
                        }
                        context.put("displayall", runData.getParameters().getString("displayall")!=null ? "true" : "false");
                        if(runData.getSession().getAttribute("applet")==null || !((String)runData.getSession().getAttribute("applet")).equals("applet")){ 
                            ArrayList<String> specimage=new ArrayList<String>();
                            specimage.add("");//dummy entry
                            for(int k=0;k<spectra.size();k++){
                                DBSpectrum spectrum=(DBSpectrum)spectra.get(k);
                                Export export=new Export(spectrum);
                                export.backColor=Color.lightGray;
                                if(UrlTool.doImageCache() && mixture.isPublic()){
                                    spectrum.setMolImage("images/molecules/large/"+mixture.getMixtureId()+"_"+spectrum.getSpectrumId()+".jpeg");
                                }else{
                                    StringBuffer sb = new StringBuffer("download/NmrshiftdbServlet/").append(mixture.getMixtureId()).append("_").append(spectrum.getSpectrumId()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmixture&spectrumid=").append(mixture.getMixtureId()).append("&format=jpeg&size=large&backcolor=");
                                    //TODO how was this supposed to work? if(spectrum.getNmrshiftdbUser().getLabgroupId()!=null && spectrum.getNmrshiftdbUser().getLabgroupId()>0)
                                    //    sb.append(ResultAction.LABGROUP_BACKGROUND_COLOR+"&shownumbers=false");
                                    //else
                                        sb.append(ResultAction.STANDARD_BACKGROUND_COLOR+"&shownumbers=false");        
                                    spectrum.setMolImage(sb.toString());
                                }
                                String filename="/nmrshiftdbhtml/" + System.currentTimeMillis() + "detailspec"+k;
                                File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), filename));
                                export.backColor=Color.lightGray;
                                export.getImage(false,"jpeg",outputFile.toString(),350,250,false,null,null,runData.getParameters().getString("spectrum")!=null ? ParseUtils.parseSpectrum(runData.getParameters().getString("spectrum").trim(),null) : new CMLPeakList());
                                specimage.add(filename+".jpg");
                            }
                            context.put("specimage",specimage);
                        }
                        if(runData.getParameters().getString("tab")!=null) {
                            Integer.parseInt(runData.getParameters().getString("tab").split("_")[0]);
                            Integer.parseInt(runData.getParameters().getString("tab").split("_")[1]);
                            context.put("tab",runData.getParameters().getString("tab"));
                        }else {
                            context.put("tab","0");
                        }
                        if(action.equals("showDetailsFromHome") || action.equals("showprivatebykey")){
                            StringBuffer query=new StringBuffer();
                            @SuppressWarnings("unchecked")
                            Enumeration<String> parameternames=runData.getRequest().getParameterNames();
                            while(parameternames.hasMoreElements()){
                                String paramname=(String)parameternames.nextElement();
                                String param=runData.getParameters().getString(paramname);
                                if(!paramname.equals("tab"))
                                    query.append(paramname+"="+param+"&");
                            }
                            if(query.length()>0)
                                context.put("query",query.substring(0,query.length()-1));
                            else
                                context.put("query","");
                        }else{
                            String type="All";
                            if(runData.getSession().getAttribute("selectedType")!=null)
                                type=(String)runData.getSession().getAttribute("selectedType");
                            String query="startMolecule="+startMolecule+"&spectrumType="+type+"&nmrshiftdbaction=showDetails&molNumber="+(runData.getParameters().getString("molNumber")==null ? "0" : runData.getParameters().getString("molNumber"))+(runData.getParameters().getString("startbrowseall")!=null ? "&startbrowseall=" + runData.getParameters().getString("startbrowseall") : "");
                            context.put("query",query.toString());
                        }
                        //handle the export templates
                        File templatedir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/templates/vm/exporttemplates"));
                        context.put("templates",templatedir.listFiles());
                        context.put("sessionidext","$sessionidext");
                        context.put("template","result/details/mixture");
                        return;
                    }else if(sample!=null){
                        context.put("sample",sample);
                        context.put("data", runData);
                        context.put("link", JetspeedLinkFactory.getInstance(runData));
                        context.put("sessionidext","$sessionidext");
                        context.put("template","result/details/sample");
                        return;
                    }else if(substance!=null){
                    	//substance
                    		List<DBSpectrum> spectra = substance.getDBSpectrums();
                            @SuppressWarnings("unchecked")
                            List<Integer> showtabs=(List<Integer>)runData.getSession().getAttribute("showtabs");
                            if(showtabs==null || runData.getParameters().getString("newmol", "false").equals("true")){
                                showtabs=new ArrayList<Integer>();
                                showtabs.add(0);
                                for(int i=0;i<spectra.size();i++)
                                    showtabs.add(0);
                            }else if(showtabs.size()<spectra.size()+1){
                                //+1 since counter in velocity start with 1
                                for(int i=0;i<spectra.size()-showtabs.size()+1;i++)
                                    showtabs.add(0);
                            }
                            //for unknown reasons, we still got out of bounds exceptions sometimes
                            for(int i=showtabs.size();i<100;i++){
                                showtabs.add(0);
                            }
                            runData.getSession().setAttribute("showtabs",showtabs);
                            if(runData.getParameters().getString("tab")!=null){
                                String[] parts=runData.getParameters().getString("tab").split("_");
                                //it seems sometimes there are requests with a wrong format, I have no idea why
                                //in order not to spam the errors I check this
                                if(parts.length==2)
                                    showtabs.set(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));		        	  
                            }
                            context.put("innertabs",showtabs);
                            if(runData.getParameters().getString("tab")!=null)
                            	context.put("tab",runData.getParameters().getString("tab"));
                            else
                            	context.put("tab","0");
                            context.put("spectra", spectra);
                            context.put("substance",substance);
                            context.put("data", runData);
                            context.put("link", JetspeedLinkFactory.getInstance(runData));
                            context.put("query", "nmrshiftdbaction=showDetailsFromHome&newmol=true&molNumber=substance:"+substance.getSubstanceId());
                            context.put("displayallurl","");
                            context.put("sessionidext",runData.getSession().getId());
                            context.put("template","result/details/substance");                    
                            context.put("message","");
                            return;
                    }else{
                        //spectrum only 
                        context.put("spectrum",spec);
                        if(runData.getSession().getAttribute("applet")==null || !((String)runData.getSession().getAttribute("applet")).equals("applet")){ 
                            ArrayList<String> specimage=new ArrayList<String>();
                            specimage.add("");//dummy entry
                            DBSpectrum spectrum=spec;
                            Export export=new Export(spectrum);
                            export.backColor=Color.lightGray;
                            String filename="/nmrshiftdbhtml/" + System.currentTimeMillis() + "detailspec"+0;
                            File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), filename));
                            export.backColor=Color.lightGray;
                            export.getImage(false,"jpeg",outputFile.toString(),350,250,false,null,null,lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) ? new CMLPeakList(ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum())) : new CMLPeakList());
                            specimage.add(filename+".jpg");
                            context.put("specimage",specimage);
                        }
                        if(lastSearch.getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || lastSearch.getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)){
                            boolean is2d=false;
                            List<CMLPeak> searchspec=ParseUtils.parseSpectrumFromSpecFile(lastSearch.getRangeForSpectrum());
                            if(runData.getSession().getAttribute("useintensities")==null){
                                for(int i=0;i<searchspec.size();i++){
                                    if(searchspec.get(i).getYValue()>0)
                                        is2d=true;
                                }
                            }
                            StringBuffer searchspectrum=new StringBuffer();
                            for(int i=0;i<searchspec.size();i++){
                                searchspectrum.append(searchspec.get(i).getXValue()+";"+searchspec.get(i).getYValue()+"|");
                            }
                            if(is2d){
                                context.put("searchspectrum", searchspectrum.toString());
                            }
                        }
                        int ordercolumn=-1;
                        if(runData.getParameters().get("ordercolumn")!=null){
                            ordercolumn=Integer.parseInt(runData.getParameters().get("ordercolumn"));
                        }
                        context.put("ordercolumn",ordercolumn);
                        boolean updown=false;
                        if(runData.getParameters().get("updown")!=null && runData.getParameters().get("updown").equals("up")){
                            updown=true;
                        }
                        context.put("updownstring",updown ? "down" : "up");
                        context.put("updown", updown);
                        context.put("sessionidext","$sessionidext");
                        //handle the export templates
                        File templatedir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/templates/vm/exporttemplates"));
                        context.put("templates",templatedir.listFiles());
                        context.put("template","result/details/speconly");
                        return;
                    }
                }
            } catch (Throwable ex) {
                context.put("message",GeneralUtils.logError(ex, "ResultPortlet/details of "+molNumber+" "+runData.getRequest().getRequestURL()+"?"+runData.getRequest().getQueryString(), runData,true));
                context.put("template","result/message");
                return;
            }
            finally{
                if(updateViews!=0)
                    try {
                        DBMoleculePeer.executeStatement("update MOLECULE set VIEWS=VIEWS+1 where MOLECULE_ID="+updateViews);
                    } catch (TorqueException e) {
                        GeneralUtils.logError(e, "updating views", runData, false);
                    }
            }
        }
        context.put("message","You did not yet choose a molecule !");
        context.put("template","result/message");
        return;
    }


    /**
     * This function takes a list of spectra and returns a hashmap with all keywords
     * anywhere as keys and a list of spectra with that keyword as value.
     * 
     * @param spectra All spectra.
     * @return The hashmap.
     * @throws TorqueException 
     */
    public static Map<String,List<DBSpectrum>> expandSpectraByKeyword(List<DBSpectrum> spectra) throws TorqueException {
        Map<String,List<DBSpectrum>> result=new LinkedHashMap<String,List<DBSpectrum>>();
        //We make sure general comes first
        result.put("General", new ArrayList<DBSpectrum>());
        for(Object o : spectra){
            DBSpectrum spectrum=(DBSpectrum)o;
            String key="General";
            if(spectrum.getDBKeywords().size()>0){
                for(int i=0;i<spectrum.getDBKeywords().size();i++){
                    key=((DBKeyword)spectrum.getDBKeywords().get(i)).getKeyword();
                    if(!spectrum.getSolvent().equals("Unknown") && !spectrum.getSolvent().equals("Unreported"))
                        key=key+"/"+spectrum.getSolvent();
                    if(!result.containsKey(key))
                        result.put(key, new ArrayList<DBSpectrum>());
                    result.get(key).add(spectrum);
                }
            }else{
                if(!result.containsKey(key))
                    result.put(key, new ArrayList<DBSpectrum>());
                result.get(key).add(spectrum);
            }
        }
        //We remove sets with only one entry
        boolean change=true;
        while(change) {
        	change=false;
	        Iterator<String> it=result.keySet().iterator();
	        Map<String,String> contents=new HashMap<String,String>();
	        List<String> toremove=new ArrayList<String>();
	        Map<String,List<DBSpectrum>> toadd=new HashMap<String,List<DBSpectrum>>();
	        while(it.hasNext()){
	        	String key=it.next();
	        	if(!key.equals("General") && result.get(key).size()==1){
	        		result.get("General").addAll(result.get(key));
	        		it.remove();
	        	}else{
		        	//if there are two datasets with same entries, they get merged
		        	StringBuffer spectrahere=new StringBuffer();
		        	for(DBSpectrum spectrum : result.get(key)){
		        		spectrahere.append(spectrum.getSpectrumId()).append(";");
		        	}
		        	if(contents.keySet().contains(spectrahere.toString())){
		        		String newkeyword=contents.get(spectrahere.toString()).substring(0,contents.get(spectrahere.toString()).lastIndexOf("/"))+"_"+key;
		        		toadd.put(newkeyword, result.get(key));
		        		it.remove();
		        		toremove.add(contents.get(spectrahere.toString()));
		        		contents.remove(spectrahere.toString());
		        		change=true;
		        	}else{
		        		contents.put(spectrahere.toString(),key);
		        	}
	        	}
	        }
	        for(String remove : toremove){
	        	result.remove(remove);
	        }
	        for(String key : toadd.keySet()){
	        	result.put(key, toadd.get(key));
	        }
        }
        //General is removed if empty
        if(result.get("General").size()==0)
        	result.remove("General");
        return result;
    }


    public class SpectraTypeComparator implements Comparator<DBSpectrum> {

        @Override
        public int compare(DBSpectrum o1, DBSpectrum o2) {
            try{
                if(o1.getSpectrumTypeId()!=o2.getSpectrumTypeId()){
                    return Integer.compare(o1.getSpectrumTypeId(), o2.getSpectrumTypeId());
                }else{
                    if(((DBSpectrum)o1).getConditionType().equals("m")){
                        return -1;
                    }else if(((DBSpectrum)o1).getConditionType().equals("p") && ((DBSpectrum)o2).getConditionType().equals("c") ){
                        return -1;
                    }else if(((DBSpectrum)o2).getConditionType().equals("m")){
                        return 1;
                    }else if(((DBSpectrum)o2).getConditionType().equals("p") && ((DBSpectrum)o2).getConditionType().equals("c") ){
                        return 1;
                    }
                }
            }catch(TorqueException ex){
            }
            return 0;
        }

    }
}

