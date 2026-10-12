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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineGroupPeer;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBExperiment;
import org.openscience.nmrshiftdb.om.DBExperimentCategory;
import org.openscience.nmrshiftdb.om.DBExperimentCategoryPeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachine;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachineDBParameterPeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachinePeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBSample;
import org.openscience.nmrshiftdb.om.DBExperimentDBSamplePeer;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBExperimentReportCategory;
import org.openscience.nmrshiftdb.om.DBExperimentReportCategoryPeer;
import org.openscience.nmrshiftdb.om.DBMachine;
import org.openscience.nmrshiftdb.om.DBMachineCondition;
import org.openscience.nmrshiftdb.om.DBMachineConditionPeer;
import org.openscience.nmrshiftdb.om.DBMachinePeer;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserHistory;
import org.openscience.nmrshiftdb.om.DBUserHistoryPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.LabSystemUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.VectorWithSessionBinding;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * Handles actions for the labsystem worker portlet.
 *
 * @author     shk3
 * @created    10. February 2004
 */
public class OrderFullfillAction extends VelocityPortletAction {
  
  
	/**
	 * Default behavior for the portlet action. Delegates to buildOrderContext.
	 * 
	 * @param context The context to fill.
	 * @param data The current RunData.
	 */
    @Override
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
		try{
    		//This is only to trigger a session count
    		data.getSession().setAttribute(VectorWithSessionBinding.REQUEST_IP, data.getRequest().getRemoteAddr());
    		if(data.getSession().getAttribute(NmrshiftdbConstants.SEARCHHISTORY)==null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getSearchhistory()!=null && !data.getUser().getUserName().equals("anon")){
    			data.getSession().setAttribute(NmrshiftdbConstants.SEARCHHISTORY, GeneralUtils.getFromXMLString(new String(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser)data.getUser()).getUserId())).getSearchhistory())));
    		}
    		buildOrderContext(context,data);
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
   * @throws TorqueException 
   * @throws TooManyRowsException 
   * @throws NoRowsException 
   * @throws DataSetException 
   * @throws IOException 
   * @throws FileNotFoundException 
   * @throws Exception
   */
  private void buildOrderContext(Context context, RunData data) throws NoRowsException, TooManyRowsException, TorqueException, DataSetException, FileNotFoundException, IOException{
    Criteria crit=new Criteria();
    TurbineUser tuser= TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId()));
    List<DBMachine> machines=DBMachinePeer.getValidMachinesOfGroup(tuser.getLabgroupId());
    context.put("machines",machines);
    crit=new Criteria();
    context.put("reportcategories", DBExperimentReportCategoryPeer.doSelect(crit));
    crit=new Criteria();
    context.put("categories",DBExperimentCategoryPeer.doSelect(crit));
    crit=new Criteria();
    crit.add(DBExperimentPeer.SUB_EXPERIMENT_1_ID,(Object)new String(DBExperimentPeer.SUB_EXPERIMENT_1_ID+" is NULL"), Criteria.CUSTOM);
    context.put("experimentssimple",DBExperimentPeer.doSelect(crit));
    crit=new Criteria();
    crit.addJoin(DBSamplePeer.USER_ID,TurbineUserPeer.USER_ID);
    crit.addJoin(TurbineUserPeer.USER_ID,DBUserHistoryPeer.USER_ID);
    crit.add(DBUserHistoryPeer.LEAVE_DATE, (Object)new String(DBUserHistoryPeer.LEAVE_DATE+" is NULL"), Criteria.CUSTOM);
    crit.add(DBUserHistoryPeer.LABGROUP_ID,(new UrlTool().getTurbineUser((JetspeedRunData)data).getLabgroupId()));
    crit.add(DBSamplePeer.FINISHED, "false");
    crit.add(DBSamplePeer.PROCESS,NmrshiftdbConstants.PROCESS_OPERATOR);
    crit.addDescendingOrderByColumn(DBSamplePeer.DATE);
    HashMap<Integer,List<DBSample>> openorders=new HashMap<Integer,List<DBSample>>();
    for(int i=0;i<machines.size();i++){
    	crit.add(DBSamplePeer.MACHINE,((DBMachine)machines.get(i)).getMachineId());
    	openorders.put(((DBMachine)machines.get(i)).getMachineId(),DBSamplePeer.doSelect(crit));
    }
    context.put("openorders",openorders);
    context.put("data", data);
    context.put("submitorshow","submit");
	UrlTool urltool = new UrlTool();
	urltool.init(data);
	context.put("baseurl", urltool.getBaseUrl());
    List<Record> v=BasePeer.executeQuery("select min(year(DATE)) from SAMPLE");
    int yearmin=((Record)v.get(0)).getValue(1).asInt();
    v=BasePeer.executeQuery("select max(year(DATE)) from SAMPLE");
    int yearmax=((Record)v.get(0)).getValue(1).asInt();
    Vector<Integer> years=new Vector<Integer>();
    for (int i=yearmin;i<=yearmax;i++){
    	years.add(new Integer(i));
    }
    context.put("years", years);
    Vector<Integer> months=new Vector<Integer>();
    for (int i=1;i<=12;i++){
    	months.add(new Integer(i));
    }
    context.put("months", months);
    Vector<Integer> days=new Vector<Integer>();
    for (int i=1;i<32;i++){
  	  days.add(new Integer(i));
    }
    context.put("days",days );
    Vector<Integer> reporttypes=new Vector<Integer>();
    reporttypes.add(new Integer(1));
    reporttypes.add(new Integer(2));
    context.put("types",reporttypes);
    crit=new Criteria();
    crit.add(DBConditionPeer.CONDITION_TYPE_ID,3);
    context.put("frequencies", DBConditionPeer.doSelect(crit));
    crit=new Criteria();
    crit.add(DBConditionPeer.CONDITION_TYPE_ID,2);
    crit.addAscendingOrderByColumn(DBConditionPeer.VALUE);
    int solventsNumber = 5;
    context.put("solventsNumber", solventsNumber);
    context.put("solvents", DBConditionPeer.doSelect(crit));
    Calendar start=(Calendar)data.getSession().getAttribute("start");
    if(start==null){
        start=java.util.Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -1);
    	data.getSession().setAttribute("start",start);
    }
    context.put("start",start);
    Calendar end=(Calendar)data.getSession().getAttribute("end");
    if(end==null){
        end=java.util.Calendar.getInstance();
    	data.getSession().setAttribute("end",end);
    }
    context.put("end",end);
    Integer selection=(Integer)data.getSession().getAttribute("selection");
    if(selection==null){
    	data.getSession().setAttribute("selection",new Integer(0));
    	selection=0;
    }
    context.put("selection",selection);
    Vector daysend=new Vector();
    daysend.add("---");
    for (int i=1;i<32;i++){
  	  daysend.add(new Integer(i));
    }
    context.put("daysend",daysend );
    Vector monthsend=new Vector();
    monthsend.add("---");
    for (int i=1;i<13;i++){
  	  monthsend.add(new Integer(i));
    }
    context.put("monthsend",monthsend );
    Vector yearsend=new Vector();
    yearsend.add("---");
    for (int i=yearmin;i<=new Date().getYear()+1900;i++){
  	  yearsend.add(new Integer(i));
    }
    context.put("yearsend",yearsend );
    if(data.getParameters().get("applet") !=null && data.getParameters().get("applet").equals("applet")){
        data.getSession().setAttribute("applet","applet");
    }
    if(data.getParameters().get("applet") !=null && data.getParameters().get("applet").equals("noapplet")){
        data.getSession().setAttribute("applet","noapplet");
    }
    //Emails for groups    
    String sql = "select USER_HISTORY.AFFILIATION_1, USER_HISTORY.AFFILIATION_2, EMAIL from TURBINE_USER join USER_HISTORY using (USER_ID) where USER_HISTORY.LEAVE_DATE is null and LABGROUP_ID = "+TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getLabgroupId();
    List<Record> emails = DBUserHistoryPeer.executeQuery(sql);
    Map<String,String> results=new TreeMap<String,String>(); 
    for(int i=0;i<emails.size();i++){
        String group=((Record)emails.get(i)).getValue(1).asString()+"/"+((Record)emails.get(i)).getValue(2).asString();
        if(!results.containsKey(group)){
            results.put(group, ((Record)emails.get(i)).getValue(3).asString());
        }else{
            if(results.get(group).indexOf(((Record)emails.get(i)).getValue(3).asString().trim())==-1)
                results.put(group, results.get(group)+","+((Record)emails.get(i)).getValue(3).asString().trim());
        }
    }
    context.put("groups", results);
    String[] daysofweek={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    context.put("daysofweek", daysofweek);
    if(data.getParameters().get("submitorshow")!=null && (data.getParameters().get("submitorshow").equals("show") || data.getParameters().get("submitorshow").equals("showshort"))){
      DBSample toshow=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
      context.put("date", toshow.getDate());
      context.put("submitorshow",data.getParameters().get("submitorshow"));
      context.put("sample",toshow);
      //Populate with experiment types
      String sql5="select distinct EXPERIMENT_CATEGORY_ID, ORDER_POSITION from EXPERIMENT_CATEGORY order by ORDER_POSITION";
      List<Record> v9=DBSamplePeer.executeQuery(sql5);
      List<List<DBExperiment>> experiments = new ArrayList<List<DBExperiment>>();
      for(int i=0;i<v9.size();i++){
      	crit = new Criteria();
      	crit.add(DBExperimentPeer.EXPERIMENT_CATEGORY_ID,((Record)v9.get(i)).getValue(1).asInt());
      	experiments.add(DBExperimentPeer.doSelect(crit));
      }
      context.put("experiments", experiments);
      List<DBExperiment> allexperiments = DBExperimentPeer.doSelect(new Criteria());
      Map<Integer,Integer> presets=new HashMap<Integer,Integer>();
      for(int i=0;i<allexperiments.size();i++){
    	  DBExperiment currentExperiment = (DBExperiment)allexperiments.get(i);
    	  int count=0;
    	  crit = new Criteria();
    	  crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, toshow.getSampleId());
    	  crit.add(DBExperimentDBSamplePeer.EXPERIMENT_ID, currentExperiment.getExperimentId());
    	  List<DBExperimentDBSample> l = DBExperimentDBSamplePeer.doSelect(crit);
    	  if(l.size()>0){
    		  count = ((DBExperimentDBSample)l.get(0)).getCount();
    	  }
    	  presets.put(currentExperiment.getExperimentId(),count);
      }
      context.put("presets",presets);
      try{
    	if(data.getSession().getAttribute("applet") !=null && data.getSession().getAttribute("applet").equals("applet")){
        	if(GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", data).equals("GGAKETCHER")){
        		context.put("molfile", GeneralUtils.replace(toshow.getDBMolecule().getStructureFile(1,true).toString(),"\n","\\n"));
        	}else{
        		context.put("molfile", "download/NmrshiftdbServlet/"+toshow.getDBMolecule().getMoleculeId()+".mol;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+toshow.getDBMolecule().getMoleculeId()+"&coordsetid=1");
        	}
    	}else{
	        context.put("image","download/NmrshiftdbServlet/"+toshow.getDBMolecule().getMoleculeId()+".jpg;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+toshow.getDBMolecule().getMoleculeId()+"&format=jpeg&size=200x200&backcolor=12632256&shownumbers=false");
    	}
      }catch(Exception ex){
        //this means no structure entered
      }
      LabSystemUtils.processBrowse(context, data, toshow, false);
      toshow.setFinished(OrderAction.PENDING);
      crit=new Criteria();
      crit.add(DBMachinePeer.LABGROUP_ID, tuser.getLabgroupId());
      crit.add(DBMachinePeer.VALID, NmrshiftdbConstants.TRUE);
      context.put("fstrengths", DBMachinePeer.doSelect(crit));
      context.put("ordertype", toshow.getProcess());
      context.put("now", new Date());
  	}
  }
  
  
  /**
   * "Deletes" a user (actually only remove him from the labgroup.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doSubmitdelete(RunData data, Context context){
	    try{
	    	if(data.getParameters().getString("deleteconfirm").equals("true")){
	    		DBUserHistory userHistory = TurbineUserPeer.getByName(data.getParameters().getString("username")).getCurrentUserHistory();
	    		userHistory.setLeaveDate(new Date());
	    		userHistory.save();
	    		DBUserHistory newUserHistory=new DBUserHistory();
	    		newUserHistory.setAffiliation1(userHistory.getAffiliation1());
    			newUserHistory.setAffiliation2(userHistory.getAffiliation2());
	    		newUserHistory.setJoinDate(userHistory.getLeaveDate());
	    		newUserHistory.setLabgroupId(0);
	    		newUserHistory.setUserId(userHistory.getUserId());
	    		newUserHistory.save();
			    buildOrderContext(context,data);
			    JetspeedSecurity.revokeRole(data.getParameters().getString("username"),"labgroup_member","Jetspeed");
	    	}
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/dosubmitdelete",data,true));
	    }
	}
  
  /**
   * Inactivates (removes the labgroup_member privilege).
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doSubmitinactivate(RunData data, Context context){
	    try{
	    	JetspeedSecurity.revokeRole(data.getParameters().getString("username"),"labgroup_member","Jetspeed");
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/domakemember",data,true));
	    }
	}

  /**
   * Resets mnova host id
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doSubmitmnova(RunData data, Context context){
	    try{
	    	if(data.getParameters().getString("mnovaconfirm").equals("true")){
	    		TurbineUserPeer.executeStatement("update TURBINE_USER set HOST_ID=null where LOGIN_NAME='"+data.getParameters().getString("username")+"'");
	    	}
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/domakemember",data,true));
	    }
	}

  /**
   * Makes a user a labgroup member (grants the role).
   *
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doMakemember(RunData data, Context context){
	    try{
	    	JetspeedSecurity.grantRole(data.getParameters().getString("username"),"labgroup_member","Jetspeed");
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/domakemember",data,true));
	    }
	}
  
  /**
   * Changes the order type (self,worker,robot) of an order.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doReassignorder(RunData data, Context context){
      try{
          DBSample sample = DBSamplePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("sampleid")));
          Criteria crit=new Criteria();
          crit.add(DBMachinePeer.MACHINE_ID,data.getParameters().get("fstrengths"));
          DBMachine fstrength=(DBMachine)DBMachinePeer.doSelect(crit).get(0);
          sample.setDBMachine(fstrength);
          sample.setProcess(data.getParameters().get("process"));
          sample.save();
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"orderfullfill/doreassignorder",data,true));
      }
  }
  
  public void doMakecondition(RunData data, Context context){
      try{
          DBCondition solvent=DBConditionPeer.retrieveByPK(Integer.parseInt(data.getParameters().get("solventtoadd")));
          DBMachineCondition machcond=new DBMachineCondition();
          if(data.getParameters().get("machineid").equals("0")) {
        	  machcond.setMachineId(0);
          }else {
              DBMachine machine= DBMachinePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("machineid")));
        	  machcond.setMachineId(machine.getMachineId());
          }
          machcond.setConditionId(solvent.getConditionId());
          if(!data.getParameters().getString("iconnmrname","").equals(""))
              machcond.setNameIconnmr(data.getParameters().get("iconnmrname"));
          else
              machcond.setNameIconnmr(solvent.getValue());
          String sql="select max(ORDER_POSITION) from MACHINE_CONDITION where MACHINE_ID="+machcond.getMachineId();
          if(data.getParameters().get("machineid").equals("0")) {
        	  sql="select max(ORDER_POSITION) from MACHINE_CONDITION where MACHINE_ID is null";
          }
          int maxorder = ((Record)DBMachineConditionPeer.executeQuery(sql).get(0)).getValue(1).asInt();
          machcond.setOrderPosition(maxorder+1);
          if(data.getParameters().get("machineid").equals("0")) {
        	  sql="insert into MACHINE_CONDITION values (null,"+machcond.getConditionId()+",'"+machcond.getNameIconnmr()+"',"+machcond.getOrderPosition()+")";
        	  DBMachineConditionPeer.executeStatement(sql);
          }else {
        	  machcond.save();
          }
          buildOrderContext(context,data);
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"orderfullfill/doreassignorder",data,true));
      }
  }
  
  public void doDeleteconditionsfrommachine(RunData data, Context context){
      try{    	  
          List<DBMachineCondition> machconds = null;
          if(data.getParameters().get("machineid").equals("0")) {
        	  machconds=DBMachineConditionPeer.doSelect(new Criteria());
        	  ListIterator<DBMachineCondition> iter = machconds.listIterator();
              while(iter.hasNext()){
              	DBMachineCondition machcon=iter.next();
                if(machcon.getMachineId()!=0){
                    iter.remove();
                }
              }
          }else {
              DBMachine machine= DBMachinePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("machineid")));
              machconds=machine.getDBMachineConditions();
          }
          for(int i=0;i<machconds.size();i++){
              DBMachineCondition machcond = (DBMachineCondition)machconds.get(i);
              if(data.getParameters().getString(machcond.getConditionId()+"_deleteconfirm1","false").equals("true") && data.getParameters().getString(machcond.getConditionId()+"_deleteconfirm2","false").equals("true")){
                  //DBMachineConditionPeer.doDelete(machcond);
            	  String sql="delete from MACHINE_CONDITION where CONDITION_ID="+machcond.getConditionId()+" and MACHINE_ID";
            	  if(machcond.getMachineId()==0)
            		  sql=sql+" is null";
            	  else
            		  sql=sql+"="+machcond.getMachineId();
            	  DBMachineConditionPeer.executeStatement(sql);
              }else{
                  int neworderpos = data.getParameters().getInt(machcond.getConditionId()+"_orderposition");
                  if(neworderpos!=machcond.getOrderPosition()){
                	  if(data.getParameters().get("machineid").equals("0")) {
                		  String sql="update MACHINE_CONDITION set ORDER_POSITION="+neworderpos+" where CONDITION_ID="+machcond.getConditionId()+" and MACHINE_ID is null";
                		  DBMachineConditionPeer.executeStatement(sql); 
                	  }else {
	                      machcond.setOrderPosition(neworderpos);
	                      machcond.save();
                	  }
                  }
              }
          }
          buildOrderContext(context,data);
      }
      catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"orderfullfill/doreassignorder",data,true));
      }
  }
  
  /**
   * Performs the "show orders overview" button task
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doShowoverview(RunData data, Context context) {
	    try{
            Calendar start = new GregorianCalendar();
            start.set(Calendar.DAY_OF_MONTH,Integer.parseInt(data.getParameters().get("fromday")));
            start.set(Calendar.MONTH,Integer.parseInt(data.getParameters().get("frommonth"))-1);
            start.set(Calendar.YEAR,Integer.parseInt(data.getParameters().get("fromyear")));
            data.getSession().setAttribute("start",start);
            Calendar end = new GregorianCalendar();
	    	boolean dateonly=true;
	    	if(!data.getParameters().get("today").equals("---")){
	            end.set(Calendar.DAY_OF_MONTH,Integer.parseInt(data.getParameters().get("today")));
	            end.set(Calendar.MONTH,Integer.parseInt(data.getParameters().get("tomonth"))-1);
	            end.set(Calendar.YEAR,Integer.parseInt(data.getParameters().get("toyear")));
	            data.getSession().setAttribute("end",end);
		    	dateonly=false;
	    	}
	    	Integer selfrobot=0;
	    	if(data.getParameters().get("method").equals("robot"))
	    		selfrobot=1;
	    	if(data.getParameters().get("method").equals("worker"))
	    		selfrobot=2;
	    	if(data.getParameters().get("method").equals("barcode"))
	    		selfrobot=3;
	    	data.getSession().setAttribute("selection",selfrobot);
	    	Criteria crit=new Criteria();
	    	SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	    	if(dateonly){
	    		Calendar calendar = java.util.Calendar.getInstance();
	    		calendar.setTime(start.getTime());
	    		calendar.add(Calendar.DAY_OF_YEAR, 1);
	    		Date dateplus = calendar.getTime();
	    		crit.add(DBSamplePeer.DATE,(Object)formatter2.format(start.getTime()),Criteria.GREATER_EQUAL);
	    		Criteria.Criterion criterion=crit.getCriterion(DBSamplePeer.DATE);
	    		criterion.and(crit.getNewCriterion(criterion.getTable(), criterion.getColumn(),(Object)formatter2.format(dateplus),Criteria.LESS_THAN));
	    	}else{
	    		crit.add(DBSamplePeer.DATE,(Object)formatter2.format(start.getTime()),Criteria.GREATER_EQUAL);
	    		Criteria.Criterion criterion=crit.getCriterion(DBSamplePeer.DATE);
	    		Calendar calendar = java.util.Calendar.getInstance();
	    		calendar.setTime(end.getTime());
	    		calendar.add(Calendar.DAY_OF_YEAR, 1);
	    		Date endplus = calendar.getTime();
	    		crit.and(crit.getNewCriterion(criterion.getTable(), criterion.getColumn(),(Object)formatter2.format(endplus),Criteria.LESS_EQUAL));
	    	}
	    	crit.add(DBSamplePeer.PROCESS,selfrobot==0 ? NmrshiftdbConstants.PROCESS_SELF : (selfrobot==1 ? NmrshiftdbConstants.PROCESS_CHANGER : (selfrobot==2 ? NmrshiftdbConstants.PROCESS_OPERATOR : NmrshiftdbConstants.PROCESS_BARCODE)));
	    	Criteria crit2=new Criteria();
	    	//this is the history, so all machines are needed
	    	//crit2.add(DBMachinePeer.VALID,"true");
	    	List<DBMachine> machines=DBMachinePeer.doSelect(crit2);
	    	HashMap<String,List<DBSample>> results=new HashMap<String,List<DBSample>>();
	    	for(int i=0;i<machines.size();i++){
	    		DBMachine currentmachine=(DBMachine)machines.get(i);
	    		crit.add(DBSamplePeer.MACHINE,currentmachine.getMachineId());
	    		List<DBSample> l=DBSamplePeer.doSelect(crit);
	    		results.put(currentmachine.getName(), l);
	    	}	    	
	    	context.put("overviewresults",results);	    	
		    buildOrderContext(context,data);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/doshowoverview",data,true));
	    }
	}

  /**
   * Performs switching on/off of experiments for a machine by the admin.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   * @throws SQLException
   */
  public void doAdminexperimentmachineassignments(RunData data, Context context) throws SQLException{
	  Connection conn=null;
      Statement stmt=null;
      try{
    	  conn=Torque.getConnection();
          stmt =conn.createStatement();
          stmt.executeUpdate("BEGIN");
      	  List<DBExperiment> experiments = new UrlTool().getTurbineUser((JetspeedRunData)data).getDBLabGroup().getDBExperimentsValid();
		  List<DBMachine> machines = new UrlTool().getTurbineUser((JetspeedRunData)data).getDBLabGroup().getDBMachinesValid();
		  for(int i=0;i<experiments.size();i++){
			  for(int k=0;k<machines.size();k++){
				  int machineid = ((DBMachine)machines.get(k)).getMachineId();
				  int experimentid =((DBExperiment)experiments.get(i)).getExperimentId();
				  String paramvalue = data.getParameters().get(machineid+"_"+experimentid+"_ispossible_day");
				  Criteria crit = new Criteria();
				  crit.add(DBExperimentDBMachinePeer.MACHINE_ID, ((DBMachine)machines.get(k)).getMachineId());
				  crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, ((DBExperiment)experiments.get(i)).getExperimentId());
				  List<DBExperimentDBMachine> l = DBExperimentDBMachinePeer.doSelect(crit, conn);
				  String paramvaluenight = null;
				  if(DBMachinePeer.retrieveByPK(machineid).getAnyShiftOperation()){
		                paramvaluenight = data.getParameters().get(machineid+"_"+experimentid+"_ispossible_night");
				  }
                  String duration = data.getParameters().get(machineid+"_"+experimentid+"_duration");
                  int durationsec=0;
                  if(!duration.matches("[0-9]*:[0-9]*") && !duration.equals("")){
                      context.put("message_experimentsmachinesassignments", "The duration for experiment "+DBExperimentPeer.retrieveByPK(experimentid).getName()+" on machine "+DBMachinePeer.retrieveByPK(machineid).getName()+" is not in the min:sec format. Correct this!");
                  }else if(!duration.equals("")){
                      durationsec=Integer.parseInt(duration.substring(duration.indexOf(":")+1));
                      durationsec=durationsec+Integer.parseInt(duration.substring(0,duration.indexOf(":")))*60;
                  }
                  boolean iscompulsory = data.getParameters().get(machineid+"_"+experimentid+"_compulsory")!=null && data.getParameters().get(machineid+"_"+experimentid+"_compulsory").equals(machineid+"_"+experimentid+"_compulsory");
                  List<Record> composedexperiments = DBExperimentPeer.executeQuery("select  * from EXPERIMENT where SUB_EXPERIMENT_1_ID ="+experimentid+" or SUB_EXPERIMENT_2_ID ="+experimentid+" or SUB_EXPERIMENT_3_ID ="+experimentid);
                  if((paramvalue!=null && (paramvalue.equals(machineid+"_"+experimentid+"_ispossible_day")) || (paramvaluenight!=null && paramvaluenight.equals(machineid+"_"+experimentid+"_ispossible_night")))){
					  if(l.size()==0){
						  DBExperimentDBMachine expmachine = new DBExperimentDBMachine();
						  expmachine.setMachineId(machineid);
						  expmachine.setExperimentId(experimentid);
						  expmachine.setNameIconnmr(data.getParameters().get(machineid+"_"+experimentid+"_name"));
						  expmachine.setValidDay((paramvalue!=null && paramvalue.equals(machineid+"_"+experimentid+"_ispossible_day")) ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
                          expmachine.setValidNight((paramvaluenight!=null && paramvaluenight.equals(machineid+"_"+experimentid+"_ispossible_night")) ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
                          expmachine.setDuration(durationsec);
                          expmachine.setCompulsory(iscompulsory ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
						  expmachine.save(conn);						 
					  }else{
						  DBExperimentDBMachine expmachine = (DBExperimentDBMachine)l.get(0);
						  expmachine.setNameIconnmr(data.getParameters().get(machineid+"_"+experimentid+"_name"));
                          expmachine.setValidDay((paramvalue!=null && paramvalue.equals(machineid+"_"+experimentid+"_ispossible_day")) ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
                          expmachine.setValidNight((paramvaluenight!=null && paramvaluenight.equals(machineid+"_"+experimentid+"_ispossible_night")) ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
                          expmachine.setDuration(durationsec);
                          expmachine.setCompulsory(iscompulsory ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);
						  expmachine.save(conn);						 
					  }
				  }else if(composedexperiments.size()==0){
					  if(l.size()>0){
						  for(DBExperimentDBMachine experimentmachine : l) {
							  Criteria crit2=new Criteria();
							  crit2.add(DBExperimentDBMachineDBParameterPeer.EXPERIMENT_MACHINE_ID, experimentmachine.getExperimentMachineId());
							  DBExperimentDBMachinePeer.doDelete(crit2,conn);
						  }
						  DBExperimentDBMachinePeer.doDelete(crit,conn);
					  }
				  }
			  }
		  }
      	stmt.executeUpdate("COMMIT");
        buildOrderContext(context,data);
    }catch(Exception ex){
      context.put("error",GeneralUtils.logError(ex,"orderfullfill/doAdminexperimentmachineassignments",data,true));
      stmt.executeUpdate("ROLLBACK");
    }
    finally{
       Torque.closeConnection(conn);
    }
  }
  
  /**
   * Handles submit of new experiment types by admin.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doSubmitaddexperiment(RunData data, Context context){
	    try{
	    	DBExperiment experiment = new DBExperiment();
	    	experiment.setName(data.getParameters().getString("name"));
	    	experiment.setValid("true");
	    	experiment.setLabgroupId(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getLabgroupId());
	    	experiment.setMaxNumber(1);
	    	experiment.setLineBefore("");
	    	experiment.setDBExperimentReportCategory((DBExperimentReportCategory)DBExperimentReportCategoryPeer.doSelect(new Criteria()).get(0));
	    	experiment.setDBExperimentCategory((DBExperimentCategory)DBExperimentCategoryPeer.doSelect(new Criteria()).get(0));
			experiment.setAutomationOrder(1);
			experiment.setOrderPosition(1);
	    	experiment.save();
	    	buildOrderContext(context,data);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/doSubmitaddexperiment",data,true));
	    }
	}
  
  /**
   * Adding of a new Machien by admin.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
  public void doSubmitaddmachine(RunData data, Context context) {
	    try{
	    	DBMachine newmachine=new DBMachine();
	    	newmachine.setName(data.getParameters().getString("name"));
	    	newmachine.setLabgroupId(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getLabgroupId());
	    	newmachine.setValid("true");
	    	newmachine.setDefaultMachine(0);
	    	newmachine.setMeasureSelf("true");
	    	newmachine.setMeasureOperator("true");
	    	newmachine.setMeasureChanger("true");
	    	newmachine.setMeasureBarcode("true");
	    	newmachine.setMeasureSelfNight("true");
	    	newmachine.setMeasureOperatorNight("true");
	    	newmachine.setMeasureChangerNight("true");
	    	newmachine.setMeasureBarcodeNight("true");
	    	newmachine.setMeasureChangerEnforceStructure(NmrshiftdbConstants.TRUE);
	    	newmachine.setMeasureOperatorEnforceStructure(NmrshiftdbConstants.TRUE);
	    	newmachine.setMeasureSelfEnforceStructure(NmrshiftdbConstants.TRUE);
	    	newmachine.setMeasureBarcodeEnforceStructure(NmrshiftdbConstants.TRUE);
	    	newmachine.setDefaultMode(NmrshiftdbConstants.PROCESS_SELF);
	    	newmachine.setDefaultModeNight(NmrshiftdbConstants.PROCESS_SELF);
	    	newmachine.setIconnmrIntegration("false");
	    	newmachine.setScpMachineName("");
	    	newmachine.setScpPath("");
	    	if(data.getParameters().get("frequency").equals("")){
	    		newmachine.setFieldStrength(data.getParameters().getInt("existingfrequency"));
	    	}else{
	    		Criteria crit=new Criteria();
	    		crit.add(DBConditionPeer.CONDITION_TYPE_ID,3);
	    		crit.add(DBConditionPeer.VALUE,data.getParameters().get("frequency"));
	    		List<DBCondition> v=DBConditionPeer.doSelect(crit);
	    		if(v.size()==0){
	    			try{
	    				float freq=Float.parseFloat(data.getParameters().get("frequency"));
	    				DBCondition cond=new DBCondition();
	    				cond.setValue(freq+"");
	    				cond.setConditionTypeId(3);
	    				cond.setUserId(Integer.parseInt(((BaseJetspeedUser) data.getUser()).getUserId()));
	    				cond.save();
	    				newmachine.setDBCondition(cond);
	    			}catch(Exception ex){
	    				context.put("message","It seems the frequency is not a numeric value");
	    				buildOrderContext(context,data);
	    				return;
	    			}
	    		}else{
	    			newmachine.setDBCondition((DBCondition)v.get(0));
	    		}
	    	}
                newmachine.setShiftOperationMon(NmrshiftdbConstants.TRUE);
                newmachine.setShiftOperationTue(NmrshiftdbConstants.TRUE);
                newmachine.setShiftOperationWed(NmrshiftdbConstants.TRUE);
                newmachine.setShiftOperationThu(NmrshiftdbConstants.TRUE);
                newmachine.setShiftOperationFri(NmrshiftdbConstants.TRUE);
                newmachine.setShiftOperationSat(NmrshiftdbConstants.FALSE);
                newmachine.setShiftOperationSun(NmrshiftdbConstants.FALSE);
		    	newmachine.setHourDayMonday(0);
				newmachine.setMinuteDayMonday(0);
		    	newmachine.setHourNightMonday(0);
				newmachine.setMinuteNightMonday(0);
		    	newmachine.setHourDayTuesday(0);
				newmachine.setMinuteDayTuesday(0);
		    	newmachine.setHourNightTuesday(0);
				newmachine.setMinuteNightTuesday(0);
		    	newmachine.setHourDayWednesday(0);
				newmachine.setMinuteDayWednesday(0);
		    	newmachine.setHourNightWednesday(0);
				newmachine.setMinuteNightWednesday(0);
		    	newmachine.setHourDayThursday(0);
				newmachine.setMinuteDayThursday(0);
		    	newmachine.setHourNightThursday(0);
				newmachine.setMinuteNightThursday(0);
		    	newmachine.setHourDayFriday(0);
				newmachine.setMinuteDayFriday(0);
		    	newmachine.setHourNightFriday(0);
				newmachine.setMinuteNightFriday(0);
		    	newmachine.setHourDaySaturday(0);
				newmachine.setMinuteDaySaturday(0);
		    	newmachine.setHourNightSaturday(0);
				newmachine.setMinuteNightSaturday(0);
		    	newmachine.setHourDaySunday(0);
				newmachine.setMinuteDaySunday(0);
		    	newmachine.setHourNightSunday(0);
				newmachine.setMinuteNightSunday(0);
				newmachine.setChangeTime(0);
				newmachine.setForceWeekend("false");
				newmachine.setDefaultOnClient("");
				newmachine.setHasLowPrio("false");
				newmachine.setNumberOfHolders(0);
	    	newmachine.save();
	    	buildOrderContext(context,data);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/doSubmitaddmachine",data,true));
	    }
	}
  
	/**
	 * Clears the selection of appended files during append of files to order.
	 * 
     * @param context The context to fill.
     * @param data The current RunData.
	 */
	public void doClear(RunData data, Context context){
	    try{
	      data.getSession().setAttribute("choosenfiles", new Vector<File>());
	      buildOrderContext(context,data);
	    }
	    catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/doClear",data,true));
	    }
	}
  
	/**
	 * Attaches files to an order (="fullfillment" of an order).
	 * 
     * @param context The context to fill.
     * @param data The current RunData.
	 * @throws IOException 
	 * @throws DataSetException 
	 * @throws FileNotFoundException 
	 * @throws TorqueException 
	 * @throws TooManyRowsException 
	 * @throws NoRowsException 
	 */
	public void doFullfill(RunData data, Context context) throws NoRowsException, TooManyRowsException, TorqueException, FileNotFoundException, DataSetException, IOException{
		//we check if adduration is a number
		String addduration=data.getParameters().get("addduration");
		try {
			Integer.parseInt(addduration);
		}catch(NumberFormatException ex) {
			context.put("message","It seems the manual duration was not a number!");
		    context.put("submitorshow","show");
		    context.put("submitorshow","submit");
			buildOrderContext(context,data);
			return;
		}		
	    try{
	      DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id")));
	      @SuppressWarnings("unchecked")
	      Vector<File> v=(Vector<File>)data.getSession().getAttribute("choosenfiles");
	      String filename=sample.getUsersId()+System.currentTimeMillis()+".zip";
	      String filenameFull=GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",data)+"/"+filename;
	      FileOutputStream baos=new FileOutputStream(filenameFull);
	      ZipOutputStream zipout=new ZipOutputStream(baos);
	      StringBuffer attachedfiles=new StringBuffer();
	      GeneralUtils.alreadyAttached.clear();
	      for(int i=0;i<v.size();i++){
	        GeneralUtils.zipDir(((File)v.get(i)), zipout,new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectory",data)), sample.getUsersId(), 0);
	        attachedfiles.append(((File)v.get(i)).getName()+"; ");
	      }
	      zipout.close();
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
	      if(data.getParameters().get("finished")!=null && data.getParameters().get("finished").equals("finished")){
	        sample.setFinished(NmrshiftdbConstants.TRUE);
	        sample.setAdditionalDuration(Integer.parseInt(addduration));	        
	        sample.save();
	      }
	      data.getSession().removeAttribute("choosenfiles");
	      data.getSession().setAttribute("currentraw",new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectory",data)));
	      buildOrderContext(context,data);
	    }
	    catch(Throwable  ex){
	    	ex.printStackTrace();
	    	context.put("error",GeneralUtils.logError(ex,"orderfullfill/doFullfill",data,true));
	    }
	}
	
    /**
     * Saves settings for a machine.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     * @throws SQLException
     */
    public void doAdminmachines(RunData data, Context context) throws SQLException{
    	Connection conn=null;
        Statement stmt=null;
        try{
        	conn=Torque.getConnection();
            stmt =conn.createStatement();
            stmt.executeUpdate("BEGIN");
            List<DBMachine> machines=DBMachinePeer.getValidMachinesOfGroup(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getLabgroupId());
            for(int i=0;i<machines.size();i++){
            	DBMachine machine=(DBMachine)machines.get(i);
            	if(data.getParameters().get(machine.getMachineId()+"_deleteconfirm1")!=null && data.getParameters().get(machine.getMachineId()+"_deleteconfirm2")!=null && data.getParameters().get(machine.getMachineId()+"_deleteconfirm1").equals(NmrshiftdbConstants.TRUE) && data.getParameters().get(machine.getMachineId()+"_deleteconfirm2").equals(NmrshiftdbConstants.TRUE) && machine.getDefaultMachine()==0){
    	    		machine.setValid("false");
            	}
            	machine.setDefaultMachine(data.getParameters().getInt(machine.getMachineId()+"_defaultmachine"));
	            if(data.getParameters().get(machine.getMachineId()+"_changer")!=null && data.getParameters().get(machine.getMachineId()+"_changer").equals(machine.getMachineId()+"_changer")){
	            	machine.setMeasureChanger(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureChanger(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_changer_night")!=null && data.getParameters().get(machine.getMachineId()+"_changer_night").equals(machine.getMachineId()+"_changer_night")){
	            	machine.setMeasureChangerNight(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureChangerNight(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_changer_structureinput")!=null && data.getParameters().get(machine.getMachineId()+"_changer_structureinput").equals(machine.getMachineId()+"_changer_structureinput")){
	            	machine.setMeasureChangerEnforceStructure(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureChangerEnforceStructure(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_operator")!=null && data.getParameters().get(machine.getMachineId()+"_operator").equals(machine.getMachineId()+"_operator")){
	            	machine.setMeasureOperator(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureOperator(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_operator_night")!=null && data.getParameters().get(machine.getMachineId()+"_operator_night").equals(machine.getMachineId()+"_operator_night")){
	            	machine.setMeasureOperatorNight(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureOperatorNight(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_operator_structureinput")!=null && data.getParameters().get(machine.getMachineId()+"_operator_structureinput").equals(machine.getMachineId()+"_operator_structureinput")){
	            	machine.setMeasureOperatorEnforceStructure(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureOperatorEnforceStructure(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_self")!=null && data.getParameters().get(machine.getMachineId()+"_self").equals(machine.getMachineId()+"_self")){
	            	machine.setMeasureSelf(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureSelf(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_self_night")!=null && data.getParameters().get(machine.getMachineId()+"_self_night").equals(machine.getMachineId()+"_self_night")){
	            	machine.setMeasureSelfNight(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureSelfNight(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_self_structureinput")!=null && data.getParameters().get(machine.getMachineId()+"_self_structureinput").equals(machine.getMachineId()+"_self_structureinput")){
	            	machine.setMeasureSelfEnforceStructure(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureSelfEnforceStructure(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_self_structureinput")!=null && data.getParameters().get(machine.getMachineId()+"_self_structureinput").equals(machine.getMachineId()+"_self_structureinput")){
	            	machine.setMeasureSelfEnforceStructure(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureSelfEnforceStructure(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_barcode")!=null && data.getParameters().get(machine.getMachineId()+"_barcode").equals(machine.getMachineId()+"_barcode")){
	            	machine.setMeasureBarcode(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureBarcode(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_barcode_night")!=null && data.getParameters().get(machine.getMachineId()+"_barcode_night").equals(machine.getMachineId()+"_barcode_night")){
	            	machine.setMeasureBarcodeNight(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureBarcodeNight(NmrshiftdbConstants.FALSE);
	            }
	            if(data.getParameters().get(machine.getMachineId()+"_barcode_structureinput")!=null && data.getParameters().get(machine.getMachineId()+"_barcode_structureinput").equals(machine.getMachineId()+"_barcode_structureinput")){
	            	machine.setMeasureBarcodeEnforceStructure(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setMeasureBarcodeEnforceStructure(NmrshiftdbConstants.FALSE);
	            }
                machine.setScpPath(data.getParameters().get(machine.getMachineId()+"_scppath"));
                machine.setScpMachineName(data.getParameters().get(machine.getMachineId()+"_scphost"));
	            if(data.getParameters().get(machine.getMachineId()+"_iconnmr")!=null && data.getParameters().get(machine.getMachineId()+"_iconnmr").equals(machine.getMachineId()+"_iconnmr")){
	            	machine.setIconnmrIntegration(NmrshiftdbConstants.TRUE);
	            }else{
	            	machine.setIconnmrIntegration(NmrshiftdbConstants.FALSE);
	            }
                String changetime = data.getParameters().get(machine.getMachineId()+"_changetime");
                if(!changetime.matches("[0-9]*:[0-9]*")){
                    context.put("message", "The change time for machine "+machine.getName()+"is not in the min:sec format. Correct this!");
                }else{
                    int changetimesec=Integer.parseInt(changetime.substring(changetime.indexOf(":")+1));
                    changetimesec=changetimesec+Integer.parseInt(changetime.substring(0,changetime.indexOf(":")))*60;
                    machine.setChangeTime(changetimesec);
                }
                String numberofholders = data.getParameters().get(machine.getMachineId()+"_numberofholders");
                if(!numberofholders.matches("[0-9]*")){
                    context.put("message", "The number of holders for machine "+machine.getName()+"is not an integer. Correct this!");
                }else{
                    int numberofholdersint=Integer.parseInt(numberofholders);
                    machine.setNumberOfHolders(numberofholdersint);
                }
                for(String day : new GregorianCalendar().getDisplayNames(Calendar.DAY_OF_WEEK,  Calendar.LONG, Locale.UK).keySet()){
                	Method hourDay = machine.getClass().getMethod("setHourDay"+day, int.class);
    		    	hourDay.invoke(machine, Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_daytime_"+day+"_hour")));
    		    	Method minuteDay = machine.getClass().getMethod("setMinuteDay"+day, int.class);
    		    	minuteDay.invoke(machine, Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_daytime_"+day+"_minute")));
    		    	boolean shiftOperation=false;
    		    	if(data.getParameters().get(machine.getMachineId()+"_shift_"+day)!=null && data.getParameters().get(machine.getMachineId()+"_shift_"+day).equals(machine.getMachineId()+"_shift_"+day)){
    		    		machine.setShiftOperation(day, true);
    		    		shiftOperation=true;
    		    	}else{
    		    		machine.setShiftOperation(day, false);
    		    	}
    		    	if(shiftOperation){
    		    		if(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_hour")!=null){
    		    			Method hourNight = machine.getClass().getMethod("setHourNight"+day, int.class);
    		    			hourNight.invoke(machine, Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_hour")));
    		    		}
        		    	if(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_minute")!=null){
        		    		Method minuteNight = machine.getClass().getMethod("setMinuteNight"+day, int.class);
        		    		minuteNight.invoke(machine, Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_minute")));
        		    	}
        		    	if(Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_daytime_"+day+"_hour"))*60+Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_daytime_"+day+"_minute"))>=Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_hour"))*60+Integer.parseInt(data.getParameters().get(machine.getMachineId()+"_nighttime_"+day+"_minute"))){
        		    		context.put("message", "The day-night time is before the night-day time for machine "+machine.getName()+". This is not possible, please correct");
        		    	}
    		    	}
	            }
                if(data.getParameters().get(machine.getMachineId()+"_forceweekend")!=null && data.getParameters().get(machine.getMachineId()+"_forceweekend").equals(machine.getMachineId()+"_forceweekend")){
                	machine.setForceWeekend(NmrshiftdbConstants.TRUE);
                }else{
                	machine.setForceWeekend(NmrshiftdbConstants.FALSE);
                }
	            machine.setDefaultMode(data.getParameters().get(machine.getMachineId()+"_defaultmode"));
	            machine.setDefaultModeNight(data.getParameters().get(machine.getMachineId()+"_defaultmode_night"));
	    		machine.save(conn);
            }
        	stmt.executeUpdate("COMMIT");
            buildOrderContext(context,data);
        }catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"fullfillorder/adminmachines",data,true));
          stmt.executeUpdate("ROLLBACK");
        }
        finally{
          Torque.closeConnection(conn);
        }
    }
    
    public void doForceweekend(RunData data, Context context) throws TorqueException {
    	Criteria crit=new Criteria();
    	crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
    	List<DBMachine> machines = DBMachinePeer.doSelect(crit);
    	for(DBMachine machine : machines){
    		String checkvalue=data.getParameters().getString("fw"+machine.getMachineId(), "false");
    		if(!checkvalue.equals(machine.getForceWeekend().equals("true"))){
    			machine.setForceWeekend(checkvalue);
    			machine.save();
    		}
    	}
    }
    
    /**
     * Saves settings for experiments.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     * @throws SQLException
     */
    public void doAdminexperiments(RunData data, Context context) throws SQLException {
    	Connection conn=null;
        Statement stmt=null;
        try{
        	conn=Torque.getConnection();
            stmt =conn.createStatement();
            stmt.executeUpdate("BEGIN");
            List<DBExperiment> experiments=DBExperimentPeer.getValidExperimentsOfGroup(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getLabgroupId());
            for(int i=0;i<experiments.size();i++){
            	DBExperiment experiment=(DBExperiment)experiments.get(i);
            	if(data.getParameters().get(experiment.getExperimentId()+"_deleteconfirm1")!=null && data.getParameters().get(experiment.getExperimentId()+"_deleteconfirm2")!=null && data.getParameters().get(experiment.getExperimentId()+"_deleteconfirm1").equals(NmrshiftdbConstants.TRUE) && data.getParameters().get(experiment.getExperimentId()+"_deleteconfirm2").equals(NmrshiftdbConstants.TRUE)){
    	    		experiment.setValid("false");
            	}
            	experiment.setMaxNumber(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_maxnumber")));
            	experiment.setDBExperimentReportCategoryKey(new NumberKey(data.getParameters().get(experiment.getExperimentId()+"_reportcategories")));
            	experiment.setDBExperimentCategoryKey(new NumberKey(data.getParameters().get(experiment.getExperimentId()+"_categories")));
            	experiment.setLineBefore(data.getParameters().get(experiment.getExperimentId()+"_linebefore"));
            	experiment.setAutomationOrder(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_automationorder")));
            	experiment.setOrderPosition(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_screenorder")));
            	if(!data.getParameters().get(experiment.getExperimentId()+"_subexp1").equals("none") && !data.getParameters().get(experiment.getExperimentId()+"_subexp1").equals("non")){
            		experiment.setSubExperiment1Id(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_subexp1")));
            		experiment.setSubExperiment2Id(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_subexp2")));
            	}
            	if(!data.getParameters().get(experiment.getExperimentId()+"_subexp3").equals("none") && !data.getParameters().get(experiment.getExperimentId()+"_subexp3").equals("non")){
            		experiment.setSubExperiment3Id(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_subexp3")));
            	}
            	if(!data.getParameters().get(experiment.getExperimentId()+"_identical").equals("none") && !data.getParameters().get(experiment.getExperimentId()+"_identical").equals("non")){
            		experiment.setSubExperiment3Id(Integer.parseInt(data.getParameters().get(experiment.getExperimentId()+"_identical")));
            	}
	    		experiment.save(conn);
            }
        	stmt.executeUpdate("COMMIT");
            buildOrderContext(context,data);
        }catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"fullfillorder/adminmachines",data,true));
          stmt.executeUpdate("ROLLBACK");
        }
        finally{
          Torque.closeConnection(conn);
        }
    }
    
    /**
     * Deletes an order.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     * @throws TorqueException
     */
    public void doDeleteorder(RunData data, Context context) throws TorqueException{
  	    Connection conn=null;
        try{
    	  conn=Transaction.begin(TurbineGroupPeer.DATABASE_NAME);
          if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
        	  DBSamplePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("id"))).delete(conn);
        	  Transaction.commit(conn);
              buildOrderContext(context,data);
          }
        }catch(Exception ex){
          Transaction.rollback(conn);
          context.put("error",GeneralUtils.logError(ex,"orderfullfill/doDeleteorder",data,true));
        }
    }

    /**
     * Changes an order to worker processing.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    public void doChangetosubmit(RunData data, Context context){
        try{
          if(data.getParameters().get("confirm")!=null && data.getParameters().get("confirm").equals("confirm")){
        	  DBSample sample = DBSamplePeer.retrieveByPK(Integer.parseInt(data.getParameters().get("id")));
        	  sample.setProcess(NmrshiftdbConstants.PROCESS_OPERATOR);
        	  sample.setFinished(NmrshiftdbConstants.FALSE);
        	  sample.save();
          }
        }catch(Exception ex){
          context.put("error",GeneralUtils.logError(ex,"orderfullfill/doChangetosubmit",data,true));
        }
    }
}
