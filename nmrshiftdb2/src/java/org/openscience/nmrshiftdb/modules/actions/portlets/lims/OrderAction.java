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
package org.openscience.nmrshiftdb.modules.actions.portlets.lims;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineGroupPeer;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBExperiment;
import org.openscience.nmrshiftdb.om.DBExperimentCategory;
import org.openscience.nmrshiftdb.om.DBExperimentCategoryPeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachine;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachinePeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBSample;
import org.openscience.nmrshiftdb.om.DBExperimentDBSampleDBParameter;
import org.openscience.nmrshiftdb.om.DBExperimentDBSamplePeer;
import org.openscience.nmrshiftdb.om.DBExperimentParameter;
import org.openscience.nmrshiftdb.om.DBExperimentParameterPeer;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBMachine;
import org.openscience.nmrshiftdb.om.DBMachineCondition;
import org.openscience.nmrshiftdb.om.DBMachineDBUserExcludedPeer;
import org.openscience.nmrshiftdb.om.DBMachinePeer;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBRawFilePeer;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.LabSystemUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.VectorWithSessionBinding;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;

/**
 * Handles actions for the labsystem user portlet.
 *
 * @author     shk3
 * @created    10. February 2004
 */
public class OrderAction extends VelocityPortletAction {
	
	public static String PENDING="pending";
    static Logger limsLogger = null;

	/**
	 * Default behavior for the portlet action. Delegates to buildOrderContext.
	 * 
	 * @param context The context to fill.
	 * @param data The current RunData.
	 */
	@Override
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
	    try{
	    	if(context.get("ordertype")==null){
	    		TurbineUser tuser= TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId()));
	    		Criteria crit=new Criteria();
	        	crit.add(DBMachinePeer.DEFAULT_MACHINE,1);
	        	crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
	        	List<DBMachine> l=DBMachinePeer.doSelect(crit);
	        	if(l.size()!=0)
		        	context.put("ordertype",((DBMachine)l.get(0)).getValidProcessing());
	    	}
    		buildOrderContext(context,data, false);
    		//This is only to trigger a session count
    		data.getSession().setAttribute(VectorWithSessionBinding.REQUEST_IP, data.getRequest().getRemoteAddr());
    		if(data.getSession().getAttribute(NmrshiftdbConstants.SEARCHHISTORY)==null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getSearchhistory()!=null && !data.getUser().getUserName().equals("anon")){
    			data.getSession().setAttribute(NmrshiftdbConstants.SEARCHHISTORY, GeneralUtils.getFromXMLString(new String(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser)data.getUser()).getUserId())).getSearchhistory())));
    		}
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"order action",data,true));
	    }
	}

  /**
   * Fills the context with all the data to display the portlet.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   * @þaram edit Are we currently in an edit process? (Needed for correct submit in order form)
   * @throws NoRowsException
   * @throws TooManyRowsException
   * @throws TorqueException
   * @throws DataSetException
   * @throws FileNotFoundException
   * @throws IOException
 * @throws InvocationTargetException 
 * @throws IllegalArgumentException 
 * @throws IllegalAccessException 
 * @throws SecurityException 
 * @throws NoSuchMethodException 
   */
  private void buildOrderContext(Context context, RunData data, boolean edit) throws NoRowsException, TooManyRowsException, TorqueException, DataSetException, FileNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    TurbineUser tuser= TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId()));
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -2);
    Date twomonthsago = cal.getTime();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    //open worker orders
    String sql="select SAMPLE.SAMPLE_ID from SAMPLE left join RAW_FILE using (SAMPLE_ID) where URL is null and PROCESS=\""+NmrshiftdbConstants.PROCESS_OPERATOR+"\" and USER_ID ="+((BaseJetspeedUser)data.getUser()).getUserId()+" and SAMPLE.DATE>'"+formatter.format(twomonthsago)+"' order by SAMPLE.DATE";
    List<Record> v1=DBSamplePeer.executeQuery(sql);
    List<DBSample> v2=new ArrayList<DBSample>();
    for(int i=0;i<v1.size();i++){
      v2.add(DBSamplePeer.retrieveByPK(new NumberKey(((Record) v1.get(i)).getValue(1).asInt())));
    }
    context.put("samples",v2);
    //done orders
    Criteria crit=new Criteria();
    crit.add(DBSamplePeer.USER_ID, ((BaseJetspeedUser)data.getUser()).getUserId());
    crit.addJoin(DBSamplePeer.SAMPLE_ID,DBRawFilePeer.SAMPLE_ID);
    crit.setDistinct();
    crit.addDescendingOrderByColumn(DBSamplePeer.DATE);
    List<DBSample> v=DBSamplePeer.doSelect(crit);
    List<DBSample> v3=new ArrayList<DBSample>();
    Boolean showassigned=(Boolean)data.getSession().getAttribute("showassigned");
    if(showassigned==null){
    	showassigned=new Boolean(false);
    	data.getSession().setAttribute("showassigned",showassigned);
    }
    Calendar end=(Calendar)data.getSession().getAttribute("end");
    if(end==null){
    	end=java.util.Calendar.getInstance();
    	data.getSession().setAttribute("end",end);
    }
    Calendar start=(Calendar)data.getSession().getAttribute("start");
    if(start==null){
        start=java.util.Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -7);
    	data.getSession().setAttribute("start",start);
    }
    context.put("start",start);
    context.put("end",end);
    context.put("showassigned", showassigned);
    crit=new Criteria();
    crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
    crit.add(DBMachinePeer.VALID, "true");
    context.put("fstrengths", DBMachinePeer.doSelect(crit));
    crit=new Criteria();
    crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
    crit.add(DBMachinePeer.VALID, "true");
    crit.addJoin(DBMachinePeer.MACHINE_ID, DBMachineDBUserExcludedPeer.MACHINE_ID);
    crit.add(DBMachineDBUserExcludedPeer.USER_ID,((BaseJetspeedUser)data.getUser()).getUserId());
    context.put("fstrengthsexcluded", DBMachinePeer.doSelect(crit));
    
    for(int i=0;i<v.size();i++){
	  Calendar calendar = java.util.Calendar.getInstance();
	  calendar.setTime(end.getTime());
	  calendar.add(Calendar.DAY_OF_YEAR, 1);
	  Date dateplus = calendar.getTime();
      sql="select distinct SAMPLE.SAMPLE_ID from SAMPLE join RAW_FILE using(SAMPLE_ID) left join RAW_FILE_SPECTRUM using (RAW_FILE_ID) where SAMPLE.SAMPLE_ID ="+((DBSample)v.get(i)).getSampleId()+" and RAW_FILE_SPECTRUM.SPECTRUM_ID is null and SAMPLE.DATE>='"+formatter.format(start.getTime())+"' and SAMPLE.DATE<='"+formatter.format(dateplus)+"'" +(showassigned.booleanValue() ? "" : "and RAW_FILE.ASSIGNED='false'");
      List<Record> v4=DBSamplePeer.executeQuery(sql);
      if(v4.size()>0)
        v3.add(DBSamplePeer.retrieveByPK(new NumberKey(((Record) v4.get(0)).getValue(1).asInt())));
    }
    context.put("samplesdone",v3);
    //open self orders
    String sql3="select SAMPLE.SAMPLE_ID from SAMPLE left join RAW_FILE using (SAMPLE_ID) where URL is null and PROCESS=\""+NmrshiftdbConstants.PROCESS_SELF+"\" and USER_ID ="+((BaseJetspeedUser)data.getUser()).getUserId()+" and SAMPLE.DATE>'"+formatter.format(twomonthsago)+"' order by SAMPLE.DATE";
    List<Record> v5=DBSamplePeer.executeQuery(sql3);
    List<DBSample> v6=new ArrayList<DBSample>();
    for(int i=0;i<v5.size();i++){
      v6.add(DBSamplePeer.retrieveByPK(new NumberKey(((Record) v5.get(i)).getValue(1).asInt())));
    }
    context.put("samplesself",v6);
    //open robot orders
    String sql4="select SAMPLE.SAMPLE_ID from SAMPLE left join RAW_FILE using (SAMPLE_ID) where URL is null and (PROCESS=\""+NmrshiftdbConstants.PROCESS_CHANGER+"\" or PROCESS=\""+NmrshiftdbConstants.PROCESS_BARCODE+"\") and USER_ID ="+((BaseJetspeedUser)data.getUser()).getUserId()+" and SAMPLE.DATE>'"+formatter.format(twomonthsago)+"' order by SAMPLE.DATE";
    List<Record> v7=DBSamplePeer.executeQuery(sql4);
    List<DBSample> v8=new ArrayList<DBSample>();
    for(int i=0;i<v7.size();i++){
      v8.add(DBSamplePeer.retrieveByPK(new NumberKey(((Record) v7.get(i)).getValue(1).asInt())));
    }
    context.put("samplesrobot",v8);
    context.put("data", data);
    context.put("session", data.getSession());
	UrlTool urltool = new UrlTool();
	urltool.init(data);
	context.put("baseurl", urltool.getBaseUrl());
    int solventsNumber = 5;
    context.put("solventsNumber", solventsNumber);
    context.put("solvents", DBConditionPeer.getSolventValuesAll());
	if(data.getParameters().get("submitorshow")!=null && (data.getParameters().get("submitorshow").equals("show") || data.getParameters().get("submitorshow").equals("edit"))){
      DBSample toshow=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
      context.put("date", toshow.getDate());
      context.put("submitorshow",data.getParameters().get("submitorshow"));
      context.put("sample",toshow);
      List<DBExperiment> experiments = DBExperimentPeer.doSelect(new Criteria());
      Map<Integer,Integer> presets=new HashMap<Integer,Integer>();
      StringBuffer presetexperiments=new StringBuffer();
      for(int i=0;i<experiments.size();i++){
    	  DBExperiment currentExperiment = (DBExperiment)experiments.get(i);
    	  int count=0;
    	  crit = new Criteria();
    	  crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, toshow.getSampleId());
    	  crit.add(DBExperimentDBSamplePeer.EXPERIMENT_ID, currentExperiment.getExperimentId());
    	  List<DBExperimentDBSample> l = DBExperimentDBSamplePeer.doSelect(crit);
    	  if(l.size()>0){
    		  count = ((DBExperimentDBSample)l.get(0)).getCount();
    	  }
    	  presets.put(currentExperiment.getExperimentId(),count);
    	  if(count>0)
    		  presetexperiments.append("document.getElementById('exp"+currentExperiment.getExperimentId()+"').checked=true;");
      }
      context.put("presetexperiments", presetexperiments.toString());
      context.put("presets",presets);
      if(toshow.getProbableStructure()==0)
        context.put("probablestructure",null);
      else
        context.put("probablestructure",toshow.getProbableStructure());
      //this is for the self fullfill
      if(toshow.getProcess().equals(NmrshiftdbConstants.PROCESS_SELF) && toshow.getFinished().equals("false")){
    	  LabSystemUtils.processBrowse(context,data, toshow, true);
      }
      if(data.getParameters().get("submitorshow").equals("edit")){
          context.put("ordertype",toshow.getProcess());
          context.put("usersid",strip(toshow.getUsersId(), data));
          context.put("solvent", toshow.getDBCondition().getValue());
          context.put("solventid", toshow.getDBCondition().getConditionId());
          context.put("solventnotindropdown", Boolean.TRUE);
          for(Object solvent : DBConditionPeer.getSolventValuesAll()){
        	  if(((Record)solvent).getValue(1).asString().equals(toshow.getDBCondition().getValue()))
        		  context.put("solventnotindropdown", Boolean.FALSE);
          }
          context.put("care",toshow.getSpecialCare());
          context.put("otherwishednuclei",toshow.getOtherWishedSpectrum());
          context.put("strength",toshow.getMachine());
          context.put("holderno",toshow.getHolderNo());
          context.put("defaultmachine",toshow.getDBMachine());
          Map<String,String> parampresets=new HashMap<String,String>();
          int currentsequence=0;
		  StringBuffer param=new StringBuffer();
		  for(DBExperimentDBSampleDBParameter parameter : toshow.getDBExperimentDBSampleDBParametersOrderedBySequence()){
			  if(parameter.getSequence()!=currentsequence){
				  parampresets.put(parameter.getExperimentId()+"_"+parameter.getParameterId(), param.substring(0, param.length()-1));
				  param=new StringBuffer();
				  currentsequence=parameter.getSequence();
			  }
			  param.append(parameter.getValue());
			  param.append("/");
		  }
      	  context.put("parampresets",parampresets);
          if(toshow.getProbableStructure()!=0){
        	  context.put("imagefile","download/NmrshiftdbServlet/"+toshow.getProbableStructure()+".jpg;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+toshow.getProbableStructure()+"&format=jpeg&size=200x200&backcolor=12632256");
        	  if(GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("GGAKETCHER")){
	        	  context.put("molfile", GeneralUtils.replace(toshow.getDBMolecule().getStructureFile(1,false),"\n","\\n"));
        	  }else{
            	  context.put("molfile", "download/NmrshiftdbServlet/"+toshow.getProbableStructure()+".mol;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+toshow.getDBMolecule().getMoleculeId()+"&coordsetid=1");
        	  }
  	        context.put("showoldinput","true");
          }else{
        	  context.put("imagefile", "none");
          }
          if(toshow.getPrint()==1) {
        	  context.put("print", data.getParameters().getInt("print"));
          }
          context.put("lowprio", toshow.getLowPrio());
      }else if(data.getParameters().get("submitorshow").equals("show")){
    	  context.put("defaultmachine",toshow.getMachine());
    	  context.put("strength",toshow.getMachine());
    	  context.put("printshow", toshow.getPrint()==0 ? "Label not printed" : "Label printed");
      }
    }else{
      context.put("date", new Date());
      if(edit)
    	  context.put("submitorshow","edit");
      else
    	  context.put("submitorshow","submit");
      context.put("data",data);
      //this is for the submit from url
      if(data.getParameters().getString("nmrshiftdbaction","").equals("submitfromlabjournal")){
	      context.put("other", data.getParameters().getString("other",""));
	      context.put("othernuc", data.getParameters().getString("othernuc",""));
	      context.put("care", data.getParameters().getString("care",""));
	      context.put("lowprio", data.getParameters().getString("lowprio","false"));
	      context.put("strength", ""+data.getParameters().getString("strength",""));
	      context.put("solvent", data.getParameters().getString("solvent",""));
	      context.put("nuclei", data.getParameters().getString("type",""));
	      context.put("usersid", data.getParameters().getString("usersid",""));
	      context.put("usersidcomment", data.getParameters().getString("usersidcomment",""));
	      context.put("ordertype", data.getParameters().getString("process",""));
	      if(data.getParameters().getString("probablestructure")!=null && !data.getParameters().getString("probablestructure").equals("")){
	    	  context.put("imagefile","download/NmrshiftdbServlet/"+data.getParameters().getString("probablestructure","")+".jpg;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+data.getParameters().getString("probablestructure","")+"&format=jpeg&size=200x200&backcolor=12632256");
	    	  context.put("urlmol",data.getParameters().getString("probablestructure"));
	      }else{
	    	  context.put("imagefile", "none");
	      }
      }else{
    	  if(context.get("imagefile")==null)
    		  context.put("imagefile", "none");
      }
    }
    //Populate with experiment types
    String sql5="select EXPERIMENT_CATEGORY_ID from EXPERIMENT_CATEGORY order by ORDER_POSITION";
    List<Record> v9=DBSamplePeer.executeQuery(sql5);
    Map<DBExperimentCategory, List<DBExperiment>> experiments = new HashMap<DBExperimentCategory, List<DBExperiment>>();
    List<DBExperimentCategory> experimentlist = new ArrayList<>();
    for(int i=0;i<v9.size();i++){
    	crit = new Criteria();
    	DBExperimentCategory expcat=DBExperimentCategoryPeer.retrieveByPK(((Record)v9.get(i)).getValue(1).asInt());
    	crit.add(DBExperimentPeer.EXPERIMENT_CATEGORY_ID,expcat.getExperimentCategoryId());
        crit.add(DBExperimentPeer.VALID,NmrshiftdbConstants.TRUE);
    	crit.addAscendingOrderByColumn(DBExperimentPeer.ORDER_POSITION);
    	if(data.getParameters().get("submitorshow")!=null && data.getParameters().get("submitorshow").equals("show")){
    		crit.addJoin(DBExperimentPeer.EXPERIMENT_ID, DBExperimentDBMachinePeer.EXPERIMENT_ID);
    		DBSample toshow=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
    		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, toshow.getDBMachine().getMachineId());
    	}
        experiments.put(expcat, DBExperimentPeer.doSelect(crit));
        experimentlist.add(expcat);
    }
    context.put("experiments", experiments);
    context.put("experimentlist", experimentlist);
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
    if(data.getParameters().get("applet") !=null && data.getParameters().get("applet").equals("applet")){
        data.getSession().setAttribute("applet","applet");
    }
    if(data.getParameters().get("applet") !=null && data.getParameters().get("applet").equals("noapplet")){
        data.getSession().setAttribute("applet","noapplet");
    }
    context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",data));
    //determine if print should be shown
    context.put("ipinprintrange", DBMachine.ipinprintrange(data));
    if(context.get("strength")==null){
    	//we check if for this IP a default is set
    	crit=new Criteria();
    	crit.add(DBMachinePeer.DEFAULT_ON_CLIENT, data.getRequest().getRemoteAddr());
    	List<DBMachine> l=DBMachinePeer.doSelect(crit);
    	//if none we fall back to general default
    	if(l.size()==0){
	    	crit=new Criteria();
	    	crit.add(DBMachinePeer.DEFAULT_MACHINE,1);
	    	crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
	    	l=DBMachinePeer.doSelect(crit);
	    	if(l.size()==0)
	    		return;
    	}
    	//if the default machine is not allowed for the current user we fall back to second option
    	if(tuser.isMachineForbidden(l.get(0))){
        	crit=new Criteria();
        	crit.add(DBMachinePeer.DEFAULT_MACHINE,2);
        	crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
        	l=DBMachinePeer.doSelect(crit);
        	if(l.size()==0)
        		return;
    	}
    	context.put("strength",((DBMachine)l.get(0)).getMachineId());
    	context.put("defaultmachine",l.get(0));
    	context.put("ordertype",((DBMachine)l.get(0)).getValidProcessing());
    }
  }

  /**
   * Strips prefix and postfix from a sample id, if configured.
   * 
   * @param usersId The id to strip.
   * @return The stripped id.
   * @throws IOException 
   * @throws FileNotFoundException 
   */
  private String strip(String usersId, RunData runData) throws FileNotFoundException, IOException {
    if(GeneralUtils.getNmrshiftdbProperty("prepostfix", runData).equals("true")){
        return usersId.substring(usersId.indexOf('-')+1).substring(0, usersId.substring(usersId.indexOf('-')+1).lastIndexOf('-'));
    }else{
        return usersId;
    }
  }

  public void doChangedates(RunData data, Context context) throws Exception {
	    try{
	        Calendar start = new GregorianCalendar();
	    	start.set(Calendar.DAY_OF_MONTH,Integer.parseInt(data.getParameters().get("fromday")));
	    	start.set(Calendar.MONTH,Integer.parseInt(data.getParameters().get("frommonth"))-1);
	    	start.set(Calendar.YEAR,Integer.parseInt(data.getParameters().get("fromyear")));
	    	data.getSession().setAttribute("start",start);
	    	Calendar end = new GregorianCalendar();
	    	end.set(Calendar.DAY_OF_MONTH,Integer.parseInt(data.getParameters().get("today")));
	    	end.set(Calendar.MONTH,Integer.parseInt(data.getParameters().get("tomonth"))-1);
	    	end.set(Calendar.YEAR,Integer.parseInt(data.getParameters().get("toyear")));
	    	data.getSession().setAttribute("end",end);
	    	data.getSession().setAttribute("showassigned",new Boolean(data.getParameters().get("showassigned")!=null));
	    	buildOrderContext(context,data, false);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/dochangedates",data,true));
	    }
	}

  
  private void writeToLog(DBSample sample, List<DBExperimentDBSample> experimentSamples) throws TorqueException {
      if (limsLogger == null) {
      	limsLogger = Logger.getLogger("lims");
      }
      StringBuffer exps=new StringBuffer();
      for(DBExperimentDBSample experiment : experimentSamples){
    	  exps.append(experiment.getDBExperiment().getDBExperimentDBMachine(sample.getDBMachine()).getNameIconnmr()+", ");
      }
      limsLogger.info((new Date().getTime()/1000) + "/" + sample.getNmrshiftdbUser().getLoginName() + "/" + sample.getNmrshiftdbUser().getFirstName()+" "+sample.getNmrshiftdbUser().getLastName()+"/"+sample.getNmrshiftdbUser().getEmail()+"/"+sample.getNmrshiftdbUser().getAffiliation1()+"/"+sample.getNmrshiftdbUser().getAffiliation2()+"/"+sample.getUsersId()+"/"+sample.getDBMachine().getName()+"/"+(sample.getPrint()==1 ? "BC"+sample.getSampleId()+","+sample.getShortId() : sample.getHolderNo())+"/"+GeneralUtils.removeLastComma(exps).toString()+"/");
		
  }

	public void doSubmitorder(RunData data, Context context) throws Exception {
	    try{
	      String message="";
	      DBSample sample=new DBSample();
	      String usersid=data.getParameters().get("sampleid");
          Criteria crit=new Criteria();
          crit.add(DBMachinePeer.MACHINE_ID,data.getParameters().get("fstrengths"));
          DBMachine fstrength=(DBMachine)DBMachinePeer.doSelect(crit).get(0);
	      if(GeneralUtils.getNmrshiftdbProperty("prepostfix", data).equals("true")){
	          usersid=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getAffiliation2()+"-"+usersid+"-"+fstrength.getName();
	      }
	      sample.setUsersId(usersid);
	      sample.setUsersIdComment(data.getParameters().get("sampleidcomment"));
	      sample.setUserId(Integer.parseInt(((BaseJetspeedUser)data.getUser()).getUserId()));
	      DBCondition solvent;
	      if(!data.getParameters().get("solventtext").equals("")){
              crit=new Criteria();
              crit.add(DBConditionPeer.VALUE,data.getParameters().get("solventtext"));
              if(DBConditionPeer.doSelect(crit).size()>0){
                  solvent=(DBCondition)DBConditionPeer.doSelect(crit).get(0);
                  sample.setDBCondition(solvent);
              }else{
                  solvent = new DBCondition();
                  solvent.setConditionTypeId(2);
                  solvent.setValue(data.getParameters().get("solventtext"));
                  solvent.setUserId(1);
                  solvent.save();
                  sample.setDBCondition(solvent);
              }
	      }else{
    	      crit=new Criteria();
    	      crit.add(DBConditionPeer.CONDITION_ID,data.getParameters().get("solvents"));
    	      solvent=(DBCondition)DBConditionPeer.doSelect(crit).get(0);
    	      sample.setDBCondition(solvent);
	      }
	      sample.setDBMachine(fstrength);
	      if((fstrength.isDuringDayShift() && fstrength.getMeasureChanger().equals(NmrshiftdbConstants.TRUE) || (!fstrength.isDuringDayShift() && fstrength.getMeasureChangerNight().equals(NmrshiftdbConstants.TRUE))))
	    	  sample.setHolderNo(data.getParameters().getInt("holderno"));
	      else
	    	  sample.setHolderNo(-1);
	      IAtomContainer mol=null;
	      if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("JCP") || data.getSession().getAttribute("applet") !=null && data.getSession().getAttribute("applet").equals("applet")){
		      String molecule = data.getParameters().get("MolTxt");
		      mol = AtomUtils.readMolBothVersions(molecule);
		      if(mol!= null && mol.getAtomCount()>0){
		        SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		        sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
		        GeneralUtils.addToStructureHistory(molecule, data);
		      }
	      }else{
	    	  if(data.getParameters().get("urlmol")!=null){
	    	      String molecule = data.getParameters().get("urlmol");
	    	      mol = AtomUtils.readMolBothVersions(molecule);
	    	      if(mol!= null && mol.getAtomCount()>0){
	    	        SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
	    	        sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
	    	      }    		  
	    	  }else if(data.getRequest().getContentType()!=null && data.getRequest().getContentType().indexOf("multipart/form-data")>-1){
		          FileItem[] fi = data.getParameters().getFileItems("datei");
		          if(fi!=null){
		            StringBuffer messages=new StringBuffer();
		            GeneralUtils.processFileItem(fi[0],messages,data,"moltxt");
		            message+=messages.toString();
		            mol=(IAtomContainer)data.getSession().getAttribute("moltxt");
		            data.getSession().removeAttribute("moltxt");
		            if(mol!= null && mol.getAtomCount()>0){
			  	        SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
			  	        StringWriter sw = new StringWriter();
			  	        MDLV2000Writer mdlwriter=new MDLV2000Writer(sw);
			  	        mdlwriter.writeMolecule(mol);
			  	        mdlwriter.close();
			  	        sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
			  	        GeneralUtils.addToStructureHistory(sw.toString(), data);
			  	    }
		          }
	    	  }
	      }
	      sample.setDate(new Date());
	      sample.setOtherWishedSpectrum("");
	      sample.setSpecialCare(data.getParameters().get("specialcare"));
	      if(sample.getSpecialCare()==null)
	    	  sample.setSpecialCare("");
	      sample.setLowPrio(data.getParameters().getString("lowprio","false").equals("lowprio") ? "true" : "false");
	      sample.setFinished("false");
	      sample.setProcess(data.getParameters().get("process"));
          String relativepath = ServletUtils.expandRelative(data.getServletConfig(),"/WEB-INF/tmp");
          File attachmentfile=new File(relativepath+"/uploadorderattachment"+data.getSession().getId());
          if(attachmentfile.exists()){
            sample.setAttachment(FileUtils.readFileToByteArray(attachmentfile));
            sample.setAttachmentName("attachmentfile");
	      }else{
	    	  sample.setAttachmentName("");
	    	  sample.setAttachment(new byte[0]);
	      }
          if((fstrength.isDuringDayShift() && fstrength.getMeasureBarcode().equals(NmrshiftdbConstants.TRUE) || (!fstrength.isDuringDayShift() && fstrength.getMeasureBarcodeNight().equals(NmrshiftdbConstants.TRUE))) && data.getParameters().get("print")!=null && data.getParameters().get("print").equals("print")) {
        	  sample.setPrint(1);
        	  String sql="select SHORT_ID from SAMPLE where SHORT_ID<>0 order by DATE desc limit 1";
        	  List<Record> result = DBSamplePeer.executeQuery(sql);
        	  if(result.size()>0 && result.get(0).getValue(1)!=null) {
        		  sample.setShortId(result.get(0).getValue(1).asInt()+1);
        		  if(sample.getShortId()>99999) {
        			  sample.setShortId(1);
        		  }
        	  }else {
        		  sample.setShortId(1);
        	  }
          }else {
        	  sample.setPrint(2);
          }          
	      List<DBExperiment> experiments = DBExperimentPeer.doSelect(new Criteria());
	      List<DBExperimentDBSample> experimentSamples = new ArrayList<DBExperimentDBSample>();
	      Map<Integer,Integer> presets=new HashMap<Integer,Integer>();
	      Map<String,String> parampresets=new HashMap<String,String>();
	      StringBuffer presetexperiments=new StringBuffer();
	      for(int i=0;i<experiments.size();i++){
	    	  DBExperiment currentExperiment = (DBExperiment)experiments.get(i);
	    	  int count=0;
	    	  if(currentExperiment.getMaxNumber()>1){
	    		  count = data.getParameters().getInt("experiment"+currentExperiment.getExperimentId());
	    	  }else{
	    		  if(data.getParameters().get("experiment"+currentExperiment.getExperimentId())!=null && data.getParameters().get("experiment"+currentExperiment.getExperimentId()).equals("experiment"+currentExperiment.getExperimentId())){
	    			  count=1;
	    		  }
	    		  if(currentExperiment.isDefaultInMachine(fstrength))
	    			  count=1;
	    	  }
	    	  if(count>0){
	    		  int maxexp=1;
	    		  for(DBExperimentParameter parameter : currentExperiment.getParametersForMachine(fstrength, true, false)){
	    			  if(StringUtils.countMatches(data.getParameters().getString("parameter_"+currentExperiment.getExperimentId()+"_"+parameter.getParameterId()+"_"+fstrength.getMachineId()),"/")+1>maxexp)
	    				  maxexp=StringUtils.countMatches(data.getParameters().getString("parameter_"+currentExperiment.getExperimentId()+"_"+parameter.getParameterId()+"_"+fstrength.getMachineId()),"/")+1;
	    		  }
	    		  for(DBExperimentParameter parameter : currentExperiment.getParametersForMachine(fstrength, true, false)){
	    			  parampresets.put(currentExperiment.getExperimentId()+"_"+parameter.getParameterId(),data.getParameters().getString("parameter_"+currentExperiment.getExperimentId()+"_"+parameter.getParameterId()+"_"+fstrength.getMachineId()));
	    		  }
	    		  for(int k=0;k<maxexp;k++){
		    		  DBExperimentDBSample experimentsample = new DBExperimentDBSample();
		    		  experimentsample.setExperimentId(currentExperiment.getExperimentId());
		    		  experimentsample.setSequence(k);
		    		  experimentsample.setCount(count);
		    		  experimentSamples.add(experimentsample);
		    		  saveParams(currentExperiment, fstrength, data, sample, currentExperiment, k);
		    		  saveParams(currentExperiment.getDBExperimentRelatedBySubExperiment1Id(), fstrength, data, sample, currentExperiment, k);
		    		  saveParams(currentExperiment.getDBExperimentRelatedBySubExperiment2Id(), fstrength, data, sample, currentExperiment, k);
		    		  saveParams(currentExperiment.getDBExperimentRelatedBySubExperiment3Id(), fstrength, data, sample, currentExperiment, k);
	    		  }
	    	  }
	    	  presets.put(currentExperiment.getExperimentId(),count);
	    	  if(count>0)
	    		  presetexperiments.append("document.getElementById('exp"+currentExperiment.getExperimentId()+"').checked=true;");
	      }
	      Criteria crit2=new Criteria();
	      crit2.add(DBSamplePeer.USERS_ID,sample.getUsersId());
	      List<DBSample> oldsamples=DBSamplePeer.doSelect(crit2);
	      Pattern pattern = Pattern.compile("[0-9A-Za-z-_]*", Pattern.CASE_INSENSITIVE);	  
	      Matcher matcher = pattern.matcher(sample.getUsersId());
	      boolean matchFound = matcher.matches();
	      if(experimentSamples.size()==0){
	        message+="You did not enter a spectrum type!";
	      }else if(sample.getUsersId().equals("")){
	    	  message+="Your id is empty, please use an id!";
	      }else if(sample.getUsersId().contains(" ")){
	    	  message+="Your id contains spaces, spaces are not allowed!";
	      }else if(oldsamples.size()>0){
	    	  message+="Your id has already been used. Choose a different one!";
	      }else if(data.getParameters().get("applet") !=null && !data.getParameters().get("applet").equals("")){
	    	  message+=" ";
	      }else if(data.getParameters().getString("nmrshiftdbaction","none").equals("submitfromlabjournal")){
	    	  //this means the link from the lab journal system is used, so we do not want immediate submit
	    	  message+=" ";
	      }else if(fstrength.isProableStructureMandatory(sample.getProcess()) && sample.getProbableStructure()==0){
	    	  message +="You did not suggest a probable structure!";
	      }else if(sample.getHolderNo()>-1 && sample.getDBCondition().getDBMachineConditions().size()==0){
	          message +="In automation mode you can only chose solvents from the dropdown list!";
          }else if(data.getParameters().get("sampleid").length()>16){
              message +="Sample I. D. is restricted to 16 characters!";
          }else if(!matchFound) {
        	  message +="Sample I. D. is restricted to characters 0-9, A-Z, a-z, -, and _!";
	      }else{
	    	  if(sample.getDBMachine().getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_BARCODE))
	    		  sample.setProcess(NmrshiftdbConstants.PROCESS_BARCODE);
	          sample.save();
	          try{
	        	  writeToLog(sample, experimentSamples);
	          }
        	  catch(Exception ex){
        		 GeneralUtils.logError(ex, "problem logging lims", data, false);
        	  }
	          for(int i=0;i<experimentSamples.size();i++){
	        	  experimentSamples.get(i).setSampleId(sample.getSampleId());
	        	  experimentSamples.get(i).save();
	          }
	          if(sample.getHolderNo()>-1)
	        	  writeAutomationFile(sample, data);
	      }
	      if(!data.getParameters().getString("smiles","").equals("")){
	    	  message="smilesupload";
              String smiles = data.getParameters().get("smiles");
              mol = AtomUtils.readFromSmilesAndLayout(smiles);
	      }
	      if(!message.equals("")){
	    	  if(message.equals("smilesupload")){
	    		  message="";
	    	  }
		      context.put("presetexperiments", presetexperiments.toString());	      
		      context.put("presets",presets);
		      context.put("parampresets",parampresets);
	          context.put("ordertype",data.getParameters().get("process"));
	          context.put("usersid",data.getParameters().get("sampleid"));
	          context.put("usersidcomment",data.getParameters().get("sampleidcomment"));
	          context.put("solvent", solvent.getValue());
	          context.put("solventid", solvent.getConditionId());
	          context.put("care",sample.getSpecialCare());
	          context.put("lowprio",sample.getLowPrio());
	          context.put("strength",fstrength.getMachineId());
	          context.put("defaultmachine",fstrength);
	          context.put("holderno",data.getParameters().getInt("holderno"));
	          context.put("print", data.getParameters().getInt("print"));
	          if(mol!= null && mol.getAtomCount()>0){
				  StringWriter sw = new StringWriter();
				  MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
				  mdlwriter.writeMolecule(mol);
				  mdlwriter.close();
				  String molString=sw.toString();
				  if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("JCP") || (data.getSession().getAttribute("applet") !=null && data.getSession().getAttribute("applet").equals("applet"))){
					File outputFile = new File(ServletUtils.expandRelative(data.getServletConfig(), "/nmrshiftdbhtml/"+System.currentTimeMillis()+"order.mol"));
					FileWriter out = new FileWriter(outputFile);
					out.write(molString);
					out.close();
					if(GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("GGAKETCHER")){
						context.put("molfile", GeneralUtils.replace(sw.toString(),"\n","\\n"));
					}else{						
						context.put("molfile", "nmrshiftdbhtml/"+outputFile.getName());
					}
	  	  	        context.put("showoldinput","true");
	              }else{
					String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "search.jpg";
					GeneralUtils.makeJpg(molString,"/"+filename,data.getServletConfig(),200,200,false,null,null, Color.LIGHT_GRAY);
					context.put("imagefile",filename);
	              }
	          }
	      }
	      context.put("message",message);
	      buildOrderContext(context,data, false);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/dosubmitorder",data,true));
	    }
	}
	
	
	private void saveParams(DBExperiment currentExperiment, DBMachine fstrength, RunData data, DBSample sample, DBExperiment rootExperiment, int sequence) throws TorqueException {
		  if(currentExperiment==null)
			  return;
		  double factorfortd=0;
		  int hastd=0;
		  for(DBExperimentParameter parameter : currentExperiment.getParametersForMachine(fstrength, false, true)){
			  if(parameter.getVisible().equals(NmrshiftdbConstants.TRUE)){
				  String parameterString=data.getParameters().getString("parameter_"+rootExperiment.getExperimentId()+"_"+parameter.getParameterId()+"_"+fstrength.getMachineId());
				  StringTokenizer st=new StringTokenizer(parameterString,"/");
				  int k=0;
				  float parameterValue=0;
				  while(k<=sequence && st.hasMoreTokens()){
					  parameterValue=Float.parseFloat(st.nextToken());
					  k++;
				  }
				  if(parameter.getNameDisplay().equals("sw") && parameterValue!=parameter.getDefaultValue(fstrength, currentExperiment)){
					  factorfortd=parameterValue/parameter.getDefaultValue(fstrength, currentExperiment);
				  }
				  if(parameterValue!=parameter.getDefaultValue(fstrength, currentExperiment)){
					  DBExperimentDBSampleDBParameter expmachineparam=new DBExperimentDBSampleDBParameter();
					  expmachineparam.setValue(parameterValue);
					  expmachineparam.setDBSample(sample);
					  expmachineparam.setDBExperiment(currentExperiment);
					  expmachineparam.setDBExperimentParameter(parameter);
					  expmachineparam.setSequence(sequence);
					  sample.addDBExperimentDBSampleDBParameter(expmachineparam);
				  }
			  }
			  if(parameter.getNameDisplay().equals("td")){
				  hastd=parameter.getParameterId();
			  }
		  }
		  if(hastd!=0 && factorfortd!=0){
			  DBExperimentParameter parameter=DBExperimentParameterPeer.retrieveByPK(hastd);
			  DBExperimentDBSampleDBParameter expmachineparam=new DBExperimentDBSampleDBParameter();
			  //we want to make sure td is an even number always
			  int newtd=(int)(parameter.getDefaultValue(fstrength, currentExperiment)*factorfortd);
			  if(newtd%2!=0)
				  newtd=newtd-1;
			  expmachineparam.setValue(newtd);
			  expmachineparam.setDBSample(sample);
			  expmachineparam.setDBExperiment(currentExperiment);
			  expmachineparam.setDBExperimentParameter(parameter);
			  expmachineparam.setSequence(sequence);
			  sample.addDBExperimentDBSampleDBParameter(expmachineparam);
		  }
	}

	/**
	 * Writes an IconNMR automation file.
	 * 
	 * @param sample The sample to write out.
	 * @param data Current RunData object.
	 * @throws IOException
	 * @throws TorqueException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private void writeAutomationFile(DBSample sample, RunData data) throws IOException, TorqueException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
  	    //Write the automation file (only on machines with changer and in changer or self mode)
		//Transfer method depends on system setup, so things are hard coded here - change if e. g. samba mount is used
	    if((sample.getDBMachine().isDuringDayShift() && sample.getDBMachine().getMeasureChanger().equals(NmrshiftdbConstants.TRUE) || (!sample.getDBMachine().isDuringDayShift() && sample.getDBMachine().getMeasureChangerNight().equals(NmrshiftdbConstants.TRUE))) && (sample.getProcess().equals(NmrshiftdbConstants.PROCESS_CHANGER) || sample.getProcess().equals(NmrshiftdbConstants.PROCESS_SELF))){
          File autofile = new File("/tmp/holder"+sample.getHolderNo()+"_"+sample.getMachine()+(sample.getLowPrio().equals(NmrshiftdbConstants.TRUE) ? "_LP" : "")+".txt");
      	  FileOutputStream autofilestream = new FileOutputStream(autofile);
      	  if(GeneralUtils.getNmrshiftdbProperty("iconnmruser", data).equals("USERNAME"))
      	    autofilestream.write(new String("USER "+((BaseJetspeedUser) data.getUser()).getUserName()+"\r\n").getBytes());
      	  else
      	      autofilestream.write(new String("USER "+GeneralUtils.getNmrshiftdbProperty("iconnmruser", data)+"\r\n").getBytes());
      	  autofilestream.write(new String("HOLDER "+sample.getHolderNo()+"\r\n").getBytes());
      	  //autofilestream.write(new String("NO_SUBMIT\r\n").getBytes());
      	  autofilestream.write(new String("NAME "+sample.getUsersId()+"\r\n").getBytes());
      	  autofilestream.write(new String("SOLVENT "+sample.getDBCondition().getDBMachineCondition(sample.getDBMachine()).getNameIconnmr()+"\r\n").getBytes());
      	  List<String> expdone=new ArrayList<String>();
      	  int expno=10;
      	  for(DBExperimentDBSample experiment : sample.getDBExperimentDBSamplesOrderedByCategory()){
          	  Criteria crit=new Criteria();
          	  crit.add(DBExperimentDBMachinePeer.MACHINE_ID, sample.getDBMachine().getMachineId());
          	  crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, ((DBExperimentDBSample)experiment).getDBExperiment().getExperimentId());
          	  DBExperimentDBMachine expMasch = (DBExperimentDBMachine)DBExperimentDBMachinePeer.doSelect(crit).get(0);
          	  if(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id()!=null){
              	  StringBuffer params = doParamsOfSubexperiment(sample, expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getDBExperimentDBMachine(sample.getDBMachine()), experiment.getDBExperiment(), experiment.getSequence());
          		  if(!expdone.contains(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getExperimentId()+params.toString())){
	          		  autofilestream.write(new String("EXPNO "+expno+"\r\n").getBytes());
	              	  expno++;
	              	  if(expMasch.getDBMachine().getNightLimit()>0 && expMasch.getDuration()>expMasch.getDBMachine().getNightLimit())
	              		autofilestream.write(new String("NIGHT\r\n").getBytes());
	              	  if(params.length()>0){
	              		autofilestream.write(new String("PARAMETERS "+GeneralUtils.removeLastComma(params)+"\r\n").toString().getBytes());
	              	  }
	              	  autofilestream.write(new String("EXPERIMENT "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getDBExperimentDBMachine(sample.getDBMachine()).getNameIconnmr()+"\r\n").getBytes());
	              	  autofilestream.write(new String("TITLE "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getName()+"\r\n").getBytes());
	              	  expdone.add(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment1Id().getExperimentId()+params.toString());
          		  }
              	  params = doParamsOfSubexperiment(sample, expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getDBExperimentDBMachine(sample.getDBMachine()), experiment.getDBExperiment(), experiment.getSequence());
          		  if(!expdone.contains(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getExperimentId()+params.toString())){
          			  autofilestream.write(new String("EXPNO "+expno+"\r\n").getBytes());
	                  expno++;
	                  if(expMasch.getDBMachine().getNightLimit()>0 && expMasch.getDuration()>expMasch.getDBMachine().getNightLimit())
	              		autofilestream.write(new String("NIGHT\r\n").getBytes());
	              	  if(params.length()>0){
	              		autofilestream.write(new String("PARAMETERS "+GeneralUtils.removeLastComma(params)+"\r\n").toString().getBytes());
	              	  }
	              	  autofilestream.write(new String("EXPERIMENT "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getDBExperimentDBMachine(sample.getDBMachine()).getNameIconnmr()+"\r\n").getBytes());
	              	  autofilestream.write(new String("TITLE "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getName()+"\r\n").getBytes());
	              	  expdone.add(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment2Id().getExperimentId()+params.toString());
          		  }
          		  if(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id()!=null){
	              	  params = doParamsOfSubexperiment(sample, expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getDBExperimentDBMachine(sample.getDBMachine()), experiment.getDBExperiment(), experiment.getSequence());
	              	  if(!expdone.contains(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getExperimentId()+params.toString())){
	                      autofilestream.write(new String("EXPNO "+expno+"\r\n").getBytes());
	                      expno++;
	                  	  if(expMasch.getDBMachine().getNightLimit()>0 && expMasch.getDuration()>expMasch.getDBMachine().getNightLimit())
	                  		autofilestream.write(new String("NIGHT\r\n").getBytes());
	                  	  if(params.length()>0){
	                  		autofilestream.write(new String("PARAMETERS "+GeneralUtils.removeLastComma(params)+"\r\n").toString().getBytes());
	                  	  }
	                  	  autofilestream.write(new String("EXPERIMENT "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getDBExperimentDBMachine(sample.getDBMachine()).getNameIconnmr()+"\r\n").getBytes());
	                  	  autofilestream.write(new String("TITLE "+expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getName()+"\r\n").getBytes());
	                  	  expdone.add(expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getDBExperimentRelatedByIdenticalWith()!=null ? expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getIdenticalWith()+params.toString() : expMasch.getDBExperiment().getDBExperimentRelatedBySubExperiment3Id().getExperimentId()+params.toString());
	              	  }
          		  }
          	  }else{
              	  StringBuffer params = doParamsOfSubexperiment(sample, expMasch, experiment.getDBExperiment(), experiment.getSequence());
          		  if(!expdone.contains(expMasch.getDBExperiment().getExperimentId()+params.toString())){
	          		  autofilestream.write(new String("EXPNO "+expno+"\r\n").getBytes());
	              	  expno++;
	              	  if(expMasch.getDBMachine().getNightLimit()>0 && expMasch.getDuration()>expMasch.getDBMachine().getNightLimit())
	              		autofilestream.write(new String("NIGHT\r\n").getBytes());
                  	  if(params.length()>0){
                  		autofilestream.write(new String("PARAMETERS "+GeneralUtils.removeLastComma(params)+"\r\n").toString().getBytes());
                  	  }
	          		  autofilestream.write(new String("EXPERIMENT "+expMasch.getNameIconnmr()+"\r\n").getBytes());
	          		  autofilestream.write(new String("TITLE "+expMasch.getDBExperiment().getName()+"\r\n").getBytes());
	                  expdone.add(expMasch.getDBExperiment().getExperimentId()+params.toString());
          		  }
          	  }
      	  }
      	  autofilestream.write(new String("END\r\n").getBytes());
      	  autofilestream.close();
      	  SSHClient ssh = new SSHClient();
      	  ssh.loadKnownHosts();
      	  ssh.connect(sample.getDBMachine().getScpMachineName());
      	  try {
            ssh.authPassword(sample.getDBMachine().getScpUsername(),sample.getDBMachine().getScpPassword());
            //ssh.useCompression();
            ssh.newSCPFileTransfer().upload(new FileSystemFile(autofile), sample.getDBMachine().getScpPath());
          } finally {
            ssh.disconnect();
            ssh.close();
          }
        }
	}
	
	private StringBuffer doParamsOfSubexperiment(DBSample sample, DBExperimentDBMachine expMasch, DBExperiment experiment, int sequence) throws TorqueException{
		  //if for a subexperiment is no experiment_machine relation we use that
		  if(expMasch!=null)
			  experiment=expMasch.getDBExperiment();
		  StringBuffer params=new StringBuffer();
		  if(sample.getDBExperimentDBSampleDBParameters(experiment, sequence).size()>0){
  			  for(DBExperimentDBSampleDBParameter expsampleparam : expMasch.getDBExperiment().getParametersForSample(sample, sequence)){
  				  params.append(expsampleparam.getDBExperimentParameter().getNameIconnmr()+","+expsampleparam.getValue()+",");
  			  }
  		  }
		  return params;
	}

	public void doEditsubmitorder(RunData data, Context context) throws Exception {
	    try{
	      String message="";
	      DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("editid")));
          String usersid=data.getParameters().get("sampleid");
          Criteria crit=new Criteria();
          crit.add(DBMachinePeer.MACHINE_ID,data.getParameters().get("fstrengths"));
          DBMachine fstrength=(DBMachine)DBMachinePeer.doSelect(crit).get(0);
          if(GeneralUtils.getNmrshiftdbProperty("prepostfix", data).equals("true")){
              usersid=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getAffiliation2()+"-"+usersid+"-"+fstrength.getName();
          }
          sample.setUsersId(usersid);
	      sample.setUsersIdComment(data.getParameters().get("sampleidcomment"));
	      DBCondition solvent;
          if(!data.getParameters().get("solventtext").equals("")){
              crit=new Criteria();
              crit.add(DBConditionPeer.VALUE,data.getParameters().get("solventtext"));
              if(DBConditionPeer.doSelect(crit).size()>0){
                  solvent=(DBCondition)DBConditionPeer.doSelect(crit).get(0);
                  sample.setDBCondition(solvent);
              }else{
                  solvent = new DBCondition();
                  solvent.setConditionTypeId(2);
                  solvent.setValue(data.getParameters().get("solventtext"));
                  solvent.setUserId(1);
                  solvent.save();
                  sample.setDBCondition(solvent);
              }
          }else{
              crit=new Criteria();
              crit.add(DBConditionPeer.CONDITION_ID,data.getParameters().get("solvents"));
              solvent=(DBCondition)DBConditionPeer.doSelect(crit).get(0);
              sample.setDBCondition(solvent);
          }
	      sample.setDBMachine(fstrength);
	      int oldHolderNo=sample.getHolderNo();
	      if((fstrength.isDuringDayShift() && fstrength.getMeasureChanger().equals(NmrshiftdbConstants.TRUE) || (!fstrength.isDuringDayShift() && fstrength.getMeasureChangerNight().equals(NmrshiftdbConstants.TRUE))))
	    	  sample.setHolderNo(data.getParameters().getInt("holderno"));
	      else
	    	  sample.setHolderNo(-1);
	      IAtomContainer mol=null;
	      if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("JCP") || data.getSession().getAttribute("applet") !=null && data.getSession().getAttribute("applet").equals("applet")){
		      String molecule = data.getParameters().get("MolTxt");
		      mol = AtomUtils.readMolBothVersions(molecule);
		      if(mol!= null && mol.getAtomCount()>0){
		        SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
		        sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
		        GeneralUtils.addToStructureHistory(molecule, data);
		      }
	      }else{
	    	  if(data.getRequest().getContentType()!=null && data.getRequest().getContentType().indexOf("multipart/form-data")>-1){
		          FileItem[] fi = data.getParameters().getFileItems("datei");
		          if(fi!=null){
		            StringBuffer messages=new StringBuffer();
		            GeneralUtils.processFileItem(fi[0],messages,data,"moltxt");
		            message+=messages.toString();
		            mol=(IAtomContainer)data.getSession().getAttribute("moltxt");
		            data.getSession().removeAttribute("moltxt");
		            if(mol!= null && mol.getAtomCount()>0){
			  	        SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
			  	        StringWriter sw = new StringWriter();
			  	        MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
			  	        mdlwriter.writeMolecule(mol);
			  	        mdlwriter.close();
			  	        sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
			  	        GeneralUtils.addToStructureHistory(sw.toString(), data);
			  	    }
		          }
	    	  }
	      }
	      sample.setDate(new Date());
	      sample.setSpecialCare(data.getParameters().get("specialcare"));
	      if(sample.getSpecialCare()==null)
	    	  sample.setSpecialCare("");
	      sample.setLowPrio(data.getParameters().getString("lowprio","false").equals("lowprio") ? "true" : "false");
	      sample.setFinished("false");
	      sample.setProcess(data.getParameters().get("process"));
	      sample.setOtherWishedSpectrum("");
	      List<DBExperiment> experiments = DBExperimentPeer.doSelect(new Criteria());
	      List<String> experimentSamples = new ArrayList<String>();
	      List<DBExperimentDBSample> experimentSamplesToDelete = new ArrayList<DBExperimentDBSample>();
	      Map<Integer,Integer> presets=new HashMap<Integer,Integer>();
	      StringBuffer presetexperiments=new StringBuffer();
	      for(int i=0;i<experiments.size();i++){
	    	  DBExperiment currentExperiment = (DBExperiment)experiments.get(i);
	    	  int count=0;
	    	  if(currentExperiment.getMaxNumber()>1){
	    		  count = data.getParameters().getInt("experiment"+currentExperiment.getExperimentId());
	    	  }else{
	    		  if(data.getParameters().get("experiment"+currentExperiment.getExperimentId())!=null && data.getParameters().get("experiment"+currentExperiment.getExperimentId()).equals("experiment"+currentExperiment.getExperimentId())){
	    			  count=1;
	    		  }
	    	  }
	    	  if(count>0){
	        	  crit = new Criteria();
	        	  crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, sample.getSampleId());
	        	  crit.add(DBExperimentDBSamplePeer.EXPERIMENT_ID, currentExperiment.getExperimentId());
	        	  List<DBExperimentDBSample> l = DBExperimentDBSamplePeer.doSelect(crit);
	        	  if(l.size()>0){
	        		  experimentSamples.add("update EXPERIMENT_SAMPLE set COUNT="+count+" where SAMPLE_ID="+((DBExperimentDBSample)l.get(0)).getSampleId()+" and EXPERIMENT_ID="+((DBExperimentDBSample)l.get(0)).getExperimentId());
	        	  }else{
		    		  experimentSamples.add("insert into EXPERIMENT_SAMPLE values("+currentExperiment.getExperimentId()+","+sample.getSampleId()+","+count+",0)");
	        	  }
	    	  }else{
	        	  crit = new Criteria();
	        	  crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, sample.getSampleId());
	        	  crit.add(DBExperimentDBSamplePeer.EXPERIMENT_ID, currentExperiment.getExperimentId());
	        	  List<DBExperimentDBSample> l = DBExperimentDBSamplePeer.doSelect(crit);
	        	  if(l.size()>0)
	        		  experimentSamplesToDelete.add((DBExperimentDBSample)l.get(0));
	    	  }
	    	  presets.put(currentExperiment.getExperimentId(),count);
	    	  if(count>0)
	    		  presetexperiments.append("document.getElementById('exp"+currentExperiment.getExperimentId()+"').checked=true;");
	      }
	      context.put("presetexperiments", presetexperiments.toString());
	      context.put("presets",presets);
	      if(experimentSamples.size()==0){
	        message+="You did not enter a spectrum type!";
	      }
	      if(!data.getParameters().getString("smiles","").equals("")){
	    	  message="smilesupload";
              String smiles = data.getParameters().get("smiles");
              mol = AtomUtils.readFromSmilesAndLayout(smiles);
	          SubmittingData sd=new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"));
              sample.setProbableStructure(sd.saveMoleculeOnly(mol, data, new UrlTool().getTurbineUser((JetspeedRunData)data)));
	      }
	      if(sample.getHolderNo()>-1 && sample.getDBCondition().getDBMachineConditions().size()==0){
            message +="In automation mode you can only chose solvents from the dropdown list!";
	      }
          if(fstrength.isProableStructureMandatory(sample.getProcess()) && sample.getProbableStructure()==0){
            message +="You did not suggest a probable structure!";
          }
   	      if(!message.equals("")){
	    	  if(message.equals("smilesupload")){
	    		  message="";
	    	  }
	          context.put("ordertype",data.getParameters().get("process"));
	          context.put("usersid",data.getParameters().get("sampleid"));
	          context.put("usersidcomment",data.getParameters().get("sampleidcomment"));
	          context.put("solvent", solvent.getValue());
	          context.put("solventid", solvent.getConditionId());
	          context.put("care",sample.getSpecialCare());
	          context.put("lowprio",sample.getLowPrio());
	          context.put("strength",fstrength.getMachineId());
	          context.put("sample",sample);
	          context.put("otherwishednuclei",sample.getOtherWishedSpectrum());
	          context.put("defaultmachine",fstrength);
	          if(mol!= null && mol.getAtomCount()>0){
	              if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("JCP") || (data.getSession().getAttribute("applet") !=null && data.getSession().getAttribute("applet").equals("applet"))){
	            	if(GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("GGAKETCHER")){
	            		context.put("molfile", GeneralUtils.replace(sample.getDBMolecule().getStructureFile(1,true).toString(),"\n","\\n"));
	            	}else{
	            		context.put("molfile", "download/NmrshiftdbServlet/"+sample.getDBMolecule().getMoleculeId()+".mol;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+sample.getDBMolecule().getMoleculeId()+"&coordsetid=1");
	            	}
	  	  	        context.put("showoldinput","true");
	              }else{
	        	    context.put("imagefile","download/NmrshiftdbServlet/"+sample.getDBMolecule().getMoleculeId()+".jpg;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+sample.getDBMolecule().getMoleculeId()+"&format=jpeg&size=200x200&backcolor=12632256");
	              }
	          }
	      }else{
	        sample.save();
	        for(int i=0;i<experimentSamples.size();i++){
	        	DBExperimentDBSamplePeer.executeStatement(experimentSamples.get(i));
	        }
	        for(int i=0;i<experimentSamplesToDelete.size();i++){
	        	DBExperimentDBSamplePeer.doDelete(experimentSamplesToDelete.get(i));
	        }
	        //If holder was changed from later to something, we write file
	        if(oldHolderNo==-1 && sample.getHolderNo()>-1)
	        	writeAutomationFile(sample, data);
	      }
	      context.put("message",message);
	      buildOrderContext(context,data,true);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/doeditsubmitorder",data,true));
	    }
	}


	public void doEditorder(RunData data, Context context) throws Exception {
		this.buildOrderContext(context, data, false);
	}
	
	public void doNoassignment(RunData data, Context context) throws Exception {
	    try{
	      if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
	        Criteria crit=new Criteria();
	        crit.add(DBRawFilePeer.SAMPLE_ID,data.getParameters().get("id"));
	        List<DBRawFile> v=DBRawFilePeer.doSelect(crit);
	        for(int i=0;i<v.size();i++){
	        	DBRawFile rf=(DBRawFile)v.get(i);
	        	rf.setAssigned("true");
	        	rf.save();
	        }
	        buildOrderContext(context,data, false);
	      }
	    }catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/donoassignment",data,true));
	    }
	}
	
	
	public void doRemeasurement(RunData data, Context context) throws Exception {
	    try{
	      if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
	        Criteria crit=new Criteria();
	        crit.add(DBRawFilePeer.SAMPLE_ID,data.getParameters().get("id"));
	        List<DBRawFile> v=DBRawFilePeer.doSelect(crit);
	        for(int i=0;i<v.size();i++){
	        	DBRawFile rf=(DBRawFile)v.get(i);
	        	rf.setAssigned("true");
	        	rf.save();
	        }
	        DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
            context.put("ordertype",sample.getProcess());
            context.put("usersid",strip(sample.getUsersId(), data)+"-2");
            context.put("solvent", sample.getDBCondition().getValue());
            context.put("solventid", sample.getDBCondition().getConditionId());
            context.put("care",sample.getSpecialCare());
            context.put("lowprio", sample.getLowPrio());
            context.put("nuclei","");
            context.put("strength",sample.getMachine());
            context.put("defaultmachine",sample.getDBMachine());
            if(sample.getProbableStructure()!=0){
            	if(GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("GGAKETCHER")){
            		context.put("molfile", GeneralUtils.replace(sample.getDBMolecule().getStructureFile(1,true).toString(),"\n","\\n"));
            	}else{
            		context.put("molfile", "download/NmrshiftdbServlet/"+sample.getDBMolecule().getMoleculeId()+".mol;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+sample.getDBMolecule().getMoleculeId()+"&coordsetid=1");
            	}
    	        context.put("showoldinput","true");
	        }
	        context.put("message","The new ID is a suggestion, you can change it, but it must be different from the old one");
	        buildOrderContext(context,data, false);
	      }
	    }catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/doremeasurement",data,true));
	    }
	}
    	
    public void doDeleteorder(RunData data, Context context) throws Exception {
  	    Connection conn=null;
        try{
    	  conn=Transaction.begin(TurbineGroupPeer.DATABASE_NAME);
	      if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
	        DBSamplePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("id"))).delete(conn);
	        Transaction.commit(conn);
	        buildOrderContext(context,data, false);
	      }
	    }catch(Exception ex){
	    	Transaction.rollback(conn);
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/dodeleteorder",data,true));
	    }
	}
	
	public void doClear(RunData data, Context context) throws Exception {
	    try{
	      data.getSession().setAttribute("choosenfiles", new Vector<File>());
	      buildOrderContext(context,data, false);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/doclear",data,true));
	    }
	}
	
	
	public void doFullfill(RunData data, Context context) throws Exception {
	    try{
	      @SuppressWarnings("unchecked")
		  Vector<File> v=(Vector<File>)data.getSession().getAttribute("choosenfiles");
		  if((v==null || v.size()==0) && data.getParameters().get("finished")==null){
	    	context.put("message","You selected no files to attach!");
	    	data.getParameters().add("submitorshow","show");
		  }else{
			  DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
		      String filename=sample.getUsersId()+"_"+System.currentTimeMillis()+".zip";
	          String filenameFull=GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",data)+"/"+filename;
		      FileOutputStream baos=new FileOutputStream(filenameFull);
		      ZipOutputStream zipout=new ZipOutputStream(baos);
		      StringBuffer attachedfiles=new StringBuffer();
    		  GeneralUtils.alreadyAttached.clear();
		      for(int i=0;i<v.size();i++){
		    	  if(((File)v.get(i)).getCanonicalPath().indexOf(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryuser",data))>-1){
		    		  GeneralUtils.zipDir(((File)v.get(i)), zipout,new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryuser",data)), sample.getUsersId(), 0);
		    		  attachedfiles.append(((File)v.get(i)).getName()+"; ");
		    	  }else{
		    		  context.put("message","You somehow managed to attach a file not inside the "+GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",data)+"directory. This is not allowed!");
		    	  }
		      }
		      if(!attachedfiles.toString().equals("")){
		    	  zipout.close();
		      }
		      if(!attachedfiles.toString().equals("")){
		          FileInputStream fis = new FileInputStream(filenameFull);
		          String md5String = DigestUtils.md5Hex(fis);
		          fis.close();
			      DBRawFile rawfile=new DBRawFile();
			      rawfile.setSampleId(sample.getSampleId());
			      rawfile.setUrl(GeneralUtils.getNmrshiftdbProperty("urlcompressedfiles",data)+"/"+filename);
			      rawfile.setContainedFiles(attachedfiles.toString());
			      rawfile.setAssigned("false");
			      rawfile.setMd5(md5String);
			      rawfile.setDate(new Date());
			      rawfile.save();
		      }
		      if(data.getParameters().get("finished")!=null && data.getParameters().get("finished").equals("finished")){
		        sample.setFinished("true");
		        sample.save();
		      }else{
			    	data.getParameters().add("submitorshow","show");
		      }
		      data.getSession().removeAttribute("choosenfiles");
		      data.getSession().setAttribute("currentraw",new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectory",data)));
		  }
	      buildOrderContext(context,data, false);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderaction/dofullfill",data,true));
	    }
	}
	
    public void doChangetoself(RunData data, Context context) throws Exception {
        try{
          if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
        	  DBSample sample = DBSamplePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("id")));
        	  sample.setProcess(NmrshiftdbConstants.PROCESS_SELF);
        	  sample.save();
          }
        }catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"orderaction/doChangetoself",data,true));
        }
    }
}
