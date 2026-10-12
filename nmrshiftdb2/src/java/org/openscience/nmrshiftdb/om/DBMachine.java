/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2013-2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.om;


import java.io.File;
import java.io.FileInputStream;
// JDK classes
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/** 
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public  class DBMachine 
    extends org.openscience.nmrshiftdb.om.BaseDBMachine
    implements Persistent
{
	public String getOnModeChangeCodeAutomation() throws TorqueException{
		StringBuffer sb =new StringBuffer("");
		Criteria crit = new Criteria();
		crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
		List<DBMachine> l=DBMachinePeer.doSelect(crit);
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("setSolvents();document.getElementById('care').disabled=true;if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){document.getElementById('strucoptional').style.visibility='"+(machine.getMeasureChangerEnforceStructure().equals("true") ? "hidden" : "visible")+"';document.getElementById('strucmandatory').style.visibility='"+(machine.getMeasureChangerEnforceStructure().equals("false") ? "hidden" : "visible")+"';document.getElementById('robotplace').style.visibility='"+(machine.getIconnmrIntegration().equals("true") ? "visible" : "hidden")+"';}else ");
		}
		//we need to cut off the last else
		return sb.toString().substring(0,sb.toString().length()-5);
	}
	
	public String getOnModeChangeCodeBarcode() throws TorqueException{
		StringBuffer sb =new StringBuffer("");
		Criteria crit = new Criteria();
		crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
		List<DBMachine> l=DBMachinePeer.doSelect(crit);
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("setSolvents();document.getElementById('care').disabled=true;if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){document.getElementById('strucoptional').style.visibility='"+(machine.getMeasureBarcodeEnforceStructure().equals("true") ? "hidden" : "visible")+"';document.getElementById('strucmandatory').style.visibility='"+(machine.getMeasureBarcodeEnforceStructure().equals("false") ? "hidden" : "visible")+"';document.getElementById('robotplace').style.visibility='hidden'; }else ");
		}
		//we need to cut off the last else
		return sb.toString().substring(0,sb.toString().length()-5);
	}

	public String getOnModeChangeCodeWorker() throws TorqueException{
		StringBuffer sb =new StringBuffer("");
		Criteria crit = new Criteria();
		crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
		List<DBMachine> l=DBMachinePeer.doSelect(crit);
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("setSolvents();document.getElementById('care').disabled=false;if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){document.getElementById('strucoptional').style.visibility='"+(machine.getMeasureOperatorEnforceStructure().equals("true") ? "hidden" : "visible")+"';document.getElementById('strucmandatory').style.visibility='"+(machine.getMeasureOperatorEnforceStructure().equals("false") ? "hidden" : "visible")+"';document.getElementById('robotplace').style.visibility='hidden'; }else ");
		}
		//we need to cut off the last else
		return sb.toString().substring(0,sb.toString().length()-5);
	}

	public String getOnModeChangeCodeSelf() throws TorqueException{
		StringBuffer sb =new StringBuffer("");
		Criteria crit = new Criteria();
		crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
		List<DBMachine> l=DBMachinePeer.doSelect(crit);
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("setSolvents();document.getElementById('care').disabled=false;if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){document.getElementById('strucoptional').style.visibility='"+(machine.getMeasureSelfEnforceStructure().equals("true") ? "hidden" : "visible")+"';document.getElementById('strucmandatory').style.visibility='"+(machine.getMeasureSelfEnforceStructure().equals("false") ? "hidden" : "visible")+"';document.getElementById('robotplace').style.visibility='"+(machine.getIconnmrIntegration().equals("true") ? "visible" : "hidden")+"';}else ");
		}
		//we need to cut off the last else
		return sb.toString().substring(0,sb.toString().length()-5);
	}

	public String getOnChangeCode(int solventsNumber, TurbineUser loggedInUser, RunData data) throws TorqueException, DataSetException, FileNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		StringBuffer sb=new StringBuffer("<script type=\"text/javascript\">\r\nfunction hideorshow(doSetDefault){");
        sb.append("setSolvents();\r\n");
		Criteria crit = new Criteria();
		crit.add(DBMachinePeer.VALID,NmrshiftdbConstants.TRUE);
		List<DBMachine> l=DBMachinePeer.doSelect(crit);
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("if(document.getElementById('fstrengths').value==\""+machine.getMachineId()+"\"){\r\n");
			if(machine.getShiftOperation(new Date()) && machine.isDuringDayShift()){
				sb.append("document.getElementById('operator').style.visibility='"+(machine.getMeasureOperator().equals("true") ? "visible" : "hidden") +"';document.getElementById('self').style.visibility='"+(machine.getMeasureSelf().equals("true") ? "visible" : "hidden") +"';document.getElementById('changer').style.visibility='"+(machine.getMeasureChanger().equals("true") ? "visible" : "hidden") +"';document.getElementById('barcode').style.visibility='"+(machine.getMeasureBarcode().equals("true") ? "visible" : "hidden") +"';setDefaultStrucinput();if(doSetDefault){setDefault();}\r\n");
				if(machine.getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && machine.getMeasureChanger().equals(NmrshiftdbConstants.TRUE)){
					sb.append("if(document.forms['MolForm'].process[0].checked == true || document.forms['MolForm'].process[2].checked == true){");
					sb.append("document.getElementById('robotplace').style.visibility='visible';\r\n");
					sb.append("}else{");
					sb.append("document.getElementById('robotplace').style.visibility='hidden';\r\n");
					sb.append("}");
				}else{
					sb.append("document.getElementById('robotplace').style.visibility='hidden';\r\n");
				}
				if(machine.getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && machine.getMeasureBarcode().equals(NmrshiftdbConstants.TRUE) && ipinprintrange(data)){
					sb.append("document.getElementById('print').style.visibility='visible';\r\n");
				}else {
					sb.append("document.getElementById('print').style.visibility='hidden';\r\n");
				}
				if(machine.getHasLowPrio()!=null && machine.getHasLowPrio().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('divlowprio').style.visibility='visible';\r\n");
				}else {
					sb.append("document.getElementById('divlowprio').style.visibility='hidden';\r\n");
				}
			}else{
				sb.append("document.getElementById('operator').style.visibility='"+(machine.getMeasureOperatorNight().equals("true") ? "visible" : "hidden") +"';document.getElementById('self').style.visibility='"+(machine.getMeasureSelfNight().equals("true") ? "visible" : "hidden") +"';document.getElementById('changer').style.visibility='"+(machine.getMeasureChangerNight().equals("true") ? "visible" : "hidden") +"';document.getElementById('barcode').style.visibility='"+(machine.getMeasureBarcodeNight().equals("true") ? "visible" : "hidden") +"';setDefaultStrucinput();if(doSetDefault){setDefault();}\r\n");
				if(machine.getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && machine.getMeasureChangerNight().equals(NmrshiftdbConstants.TRUE)){
					sb.append("if(document.forms['MolForm'].process[0].checked == true || document.forms['MolForm'].process[2].checked == true){");
					sb.append("document.getElementById('robotplace').style.visibility='visible';\r\n");
					sb.append("}else{");
					sb.append("document.getElementById('robotplace').style.visibility='hidden';\r\n");
					sb.append("}");
				}else{
					sb.append("document.getElementById('robotplace').style.visibility='hidden';\r\n");
				}
				if(machine.getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && machine.getMeasureBarcodeNight().equals(NmrshiftdbConstants.TRUE) && ipinprintrange(data)){
					sb.append("document.getElementById('print').style.visibility='visible';\r\n");
				}else{
					sb.append("document.getElementById('print').style.visibility='hidden';\r\n");
				}
				if(machine.getHasLowPrio()!=null && machine.getHasLowPrio().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('divlowprio').style.visibility='visible';\r\n");
				}else {
					sb.append("document.getElementById('divlowprio').style.visibility='hidden';\r\n");
				}
			}
			sb.append("}");
			if(i<l.size()-1){
				sb.append("else ");
			}
		}
		sb.append("}\r\n");
        sb.append("function disableHolders(){\r\n");
        //This deals with a) disabling used holders (only if invalidateholder is set) and b) disabling holders larger than the number of holders for this machine (the dropdown has elements for the maximum of all machines)
        crit=new Criteria();
        crit.add(DBMachinePeer.LABGROUP_ID, loggedInUser.getLabgroupId());
        crit.add(DBMachinePeer.VALID, "true");
        crit.add(DBMachinePeer.MEASURE_CHANGER,NmrshiftdbConstants.TRUE);
        crit.add(DBMachinePeer.ICONNMR_INTEGRATION,NmrshiftdbConstants.TRUE);
        for(int i=0;i<l.size();i++){
            DBMachine machine=l.get(i);
            sb.append("if(document.getElementById('fstrengths').value==\""+machine.getMachineId()+"\"){\r\n");
            List<Integer> freeHolders=machine.getFreeHolders();
            for(int k=1;k<=this.getMaxNumberOfHolders();k++){
            	if((GeneralUtils.getNmrshiftdbProperty("invalidateholder", data).equals("true") && !freeHolders.contains(k)) || k>machine.getNumberOfHolders() ){
                    sb.append("document.getElementById('holder"+k+"').disabled=true;");
                }else{
                    sb.append("document.getElementById('holder"+k+"').disabled=false;");
                }
            }
            sb.append("}");
            if(i<l.size()-1){
                sb.append("else ");
            }
        }
        sb.append("}\r\n");
        sb.append("function setDefaultStrucinput(){\r\n");
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){");
			if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_SELF)){
				if(machine.getMeasureSelfEnforceStructure().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('strucoptional').style.visibility='hidden';document.getElementById('strucmandatory').style.visibility='visible';");
				}else{
					sb.append("document.getElementById('strucoptional').style.visibility='visible';document.getElementById('strucmandatory').style.visibility='hidden';");
				}
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_OPERATOR)){
				if(machine.getMeasureOperatorEnforceStructure().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('strucoptional').style.visibility='hidden';document.getElementById('strucmandatory').style.visibility='visible';");
				}else{
					sb.append("document.getElementById('strucoptional').style.visibility='visible';document.getElementById('strucmandatory').style.visibility='hidden';");
				}
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_CHANGER)){
				if(machine.getMeasureChangerEnforceStructure().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('strucoptional').style.visibility='hidden';document.getElementById('strucmandatory').style.visibility='visible';");
				}else{
					sb.append("document.getElementById('strucoptional').style.visibility='visible';document.getElementById('strucmandatory').style.visibility='hidden';");
				}
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_BARCODE)){
				if(machine.getMeasureBarcodeEnforceStructure().equals(NmrshiftdbConstants.TRUE)){
					sb.append("document.getElementById('strucoptional').style.visibility='hidden';document.getElementById('strucmandatory').style.visibility='visible';");
				}else{
					sb.append("document.getElementById('strucoptional').style.visibility='visible';document.getElementById('strucmandatory').style.visibility='hidden';");
				}
			}
			sb.append("}else ");
		}
		sb = new StringBuffer(sb.toString().substring(0,sb.toString().length()-5));
		sb.append("}\r\nfunction setDefault(){\r\n");
        sb.append("setSolvents();\r\n");
		for(int i=0;i<l.size();i++){
			DBMachine machine=l.get(i);
			sb.append("if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){");
			if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_SELF)){
				sb.append("document.getElementsByName('process')[3].checked=true;document.getElementById('care').disabled=false;");
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_OPERATOR)){
				sb.append("document.getElementsByName('process')[2].checked=true;document.getElementById('care').disabled=false;");
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_BARCODE)){
				sb.append("document.getElementsByName('process')[1].checked=true;document.getElementById('care').disabled=true;");
			}else if(machine.getDefaultModeActual().equals(NmrshiftdbConstants.PROCESS_CHANGER)){
				sb.append("document.getElementsByName('process')[0].checked=true;document.getElementById('care').disabled=true;");
			}
			sb.append("}else ");
		}
        sb = new StringBuffer(sb.toString().substring(0,sb.toString().length()-5));
        sb.append("}\r\nfunction setSolvents(){\r\n");
        sb.append("if(document.getElementsByName('process')[0].checked || document.getElementsByName('process')[1].checked){");
        sb.append("document.getElementById('solventfield').style.visibility='hidden';");
        sb.append("}else{");
        sb.append("document.getElementById('solventfield').style.visibility='visible';");
        sb.append("}");
        for(int i=0;i<l.size();i++){
            DBMachine machine=l.get(i);
            //wenn diese machine im automationmode ist und wir sind self/auto, dann die gewählten aktivieren
            //sonst nur den Standard zeigen
            if(machine.getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE)){
                sb.append("if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){");
                //sb.append("if(document.getElementsByName('process')[0].checked || document.getElementsByName('process')[1].checked){");
                doSolventsForIconNMR(sb, machine,solventsNumber);
                //sb.append("}else{");
                //doSolventsStandard(sb, solventsNumber);
                //sb.append("}");
                sb.append("}else ");                
            }else{
                sb.append("if(document.getElementById('fstrengths').value=="+machine.getMachineId()+"){");
                doSolventsStandard(sb, solventsNumber);
                sb.append("document.getElementById('solventfield').style.visibility='visible';");
                sb.append("}else ");     
            }
        }
		//we need to cut off the last else
		return sb.toString().substring(0,sb.toString().length()-5)+"}\r\n</script>";
	}
	
	public int getMaxNumberOfHolders() throws DataSetException, TorqueException {
		String query="select MAX(NUMBER_OF_HOLDERS) from MACHINE";
		List<Record> resultmachine = DBMachinePeer.executeQuery(query);
		if(resultmachine.size()==0)
			return 60;
		else
			return resultmachine.get(0).getValue(1).asInt();
	}
	
	private void doSolventsForIconNMR(StringBuffer sb, DBMachine machine, int solventsNumber) throws TorqueException, DataSetException{
	    List<Record> resultmachine = DBConditionPeer.executeQuery("select VALUE, CONDITION_ID from MACHINE_CONDITION join `NMR_CONDITION` using (CONDITION_ID) where MACHINE_ID="+machine.getMachineId()+" group by VALUE, CONDITION_ID, ORDER_POSITION order by ORDER_POSITION");
	    List<Record> resultall = DBConditionPeer.executeQuery("select VALUE, CONDITION_ID from MACHINE_CONDITION join `NMR_CONDITION` using (CONDITION_ID) where MACHINE_ID<>"+machine.getMachineId()+" or MACHINE_ID is null group by VALUE, CONDITION_ID, ORDER_POSITION order by ORDER_POSITION");
	    for(Record record : resultall) {
	    	sb.append("hideOption('solventid"+record.getValue(2).asString()+"');");
	    }
	    for(Record record : resultmachine) {
	    	sb.append("showOption('solventid"+record.getValue(2).asString()+"');");
	    }
	}
	
    /**
     * Adds javascript to show only the standard solvents.
     * 
     * @param sb The stringbuffer to append to.
     * @param solventsNumber This is the number of solvents to choose by use, if this is activ in DBConditionPeer.getSolventValues
     * @throws TorqueException
     * @throws DataSetException
     */
    private void doSolventsStandard(StringBuffer sb, int solventsNumber) throws TorqueException, DataSetException{
	    List<Record> resultmachine = DBConditionPeer.executeQuery("select VALUE, CONDITION_ID from MACHINE_CONDITION join `NMR_CONDITION` using (CONDITION_ID) where MACHINE_ID is null group by VALUE, CONDITION_ID, ORDER_POSITION order by ORDER_POSITION");
	    List<Record> resultall = DBConditionPeer.executeQuery("select VALUE, CONDITION_ID from MACHINE_CONDITION join `NMR_CONDITION` using (CONDITION_ID) where MACHINE_ID is not null group by VALUE, CONDITION_ID, ORDER_POSITION order by ORDER_POSITION");
	    for(Record record : resultall) {
	    	sb.append("hideOption('solventid"+record.getValue(2).asString()+"');");
	    }
	    for(Record record : resultmachine) {
	    	sb.append("showOption('solventid"+record.getValue(2).asString()+"');");
	    }
    }

    
    /**
     * Returns the preferential processing for this machine, where changer>self>operator.
     * 
     * @return One of the constants for processing.
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    public String getValidProcessing() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	if(this.getShiftOperation(new Date()) && this.isDuringDayShift()){
			if(this.getMeasureBarcode().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_BARCODE;
			else if(this.getMeasureChanger().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_CHANGER;
			else if(this.getMeasureSelf().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_SELF;
			else
				return NmrshiftdbConstants.PROCESS_OPERATOR;    		
    	}else{
			if(this.getMeasureBarcodeNight().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_BARCODE;
			else if(this.getMeasureChangerNight().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_CHANGER;
			else if(this.getMeasureSelfNight().equals(NmrshiftdbConstants.TRUE))
				return NmrshiftdbConstants.PROCESS_SELF;
			else
				return NmrshiftdbConstants.PROCESS_OPERATOR;
    	}
	}
    
    /**
     * Tells if a certain experiment is a night experiment (ie. takes longer than the limit) on this machine.
     * 
     * @param experimentId The id of the experiment.
     * @param data
     * @return true=is night, false=is not
     * @throws NumberFormatException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws TorqueException
     */
    public boolean getExperimentIsNight(int experimentId, RunData data) throws NumberFormatException, FileNotFoundException, IOException, TorqueException{
    	Criteria crit = new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, this.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, experimentId);
		List<DBExperimentDBMachine> visibleexperiments = DBExperimentDBMachinePeer.doSelect(crit);
		//it should  not happen that there is not exactly one entry
		if(visibleexperiments.size()!=1){
		    return false;
		}
		return ((DBExperimentDBMachine)visibleexperiments.get(0)).getDuration()>this.getNightLimit();
    }
	
	/**
	 * Tells if a certain experiment is not possible on this machine.
	 * 
	 * @param experimentId The id of the experiment to check for.
	 * @return true=experiment is not possible, false= it is possible.
	 * @throws TorqueException Database problems.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public boolean getExperimentInVisible(int experimentId) throws TorqueException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		Criteria crit = new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, this.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, experimentId);
		List<DBExperimentDBMachine> visibleexperiments = DBExperimentDBMachinePeer.doSelect(crit);
		//if combination is not in table, the experiment cannot be executed
		if(visibleexperiments.size()==0){
		    return true;
		}
        Date now = new Date();
        DBExperimentDBMachine expmachine = visibleexperiments.get(0);
        //if the experiment is blocked we do not show it
        if(expmachine.getHideHourBegin()!=0 && getShiftOperation(now) &&
        		((now.getHours()>expmachine.getHideHourBegin() || (now.getHours()==expmachine.getHideHourBegin() && now.getMinutes()>=expmachine.getHideMinuteBegin()))
        				&& (now.getHours()<expmachine.getHideHourEnd() || (now.getHours()==expmachine.getHideHourEnd() && now.getMinutes()<=expmachine.getHideMinuteEnd())))){
        	return true;
		}else{
		    //if we are not in shift operation, the combination is valid anyway
		    if(!this.getShiftOperation(new Date())){
		        return false;
		    }else{
		        //else check times
		    	Calendar calendar=new GregorianCalendar();
		    	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
		    	Method hourDay = this.getClass().getMethod("getHourDay"+day);
		    	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
		    	Method hourNight = this.getClass().getMethod("getHourNight"+day);
		    	Method minuteNight = this.getClass().getMethod("getMinuteNight"+day);
		        if(expmachine.getValidNight().equals(NmrshiftdbConstants.TRUE) && 
		                now.getHours()*60+now.getMinutes()>=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this) && 
		                now.getHours()*60+now.getMinutes()>=(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this))
	                return false;
		        else if(expmachine.getValidNight().equals(NmrshiftdbConstants.TRUE) &&
                        now.getHours()*60+now.getMinutes()<=(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this) &&
                        now.getHours()*60+now.getMinutes()<=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this))
                    return false;
		        else if(expmachine.getValidDay().equals(NmrshiftdbConstants.TRUE) &&
                        now.getHours()*60+now.getMinutes()<=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this) &&
                        now.getHours()*60+now.getMinutes()>=(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this))
                    return false;
		        else
		            return true;
		    }
		}
	}
	
	/**
	 * Tells if this machine is currently in day shift. If the machine is not for shift operation the result is always false.
	 * 
	 * @return True=day shift, false=night shift.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public boolean isDuringDayShift() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Date now = new Date();
    	Calendar calendar=new GregorianCalendar();
    	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    	Method hourDay = this.getClass().getMethod("getHourDay"+day);
    	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
    	Method hourNight = this.getClass().getMethod("getHourNight"+day);
    	Method minuteNight = this.getClass().getMethod("getMinuteNight"+day);
        if(this.getShiftOperation(now) && now.getHours()*60+now.getMinutes()<=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this) && now.getHours()*60+now.getMinutes()>=(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this))
        	return true;
        else
            return false;
	}

	/**
	 * Tells if the Machine is in shift operation on the day given in date (depending on day of week).
	 * 
	 * @param date The data to check for.
	 * @return false=not in shift operation on that day of week, true=in shift operation
	 */
	public boolean getShiftOperation(Date date) {
		if(this.getForceWeekend().equals(NmrshiftdbConstants.TRUE))
			return false;
	    if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY && this.getShiftOperationMon().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.TUESDAY && this.getShiftOperationTue().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY && this.getShiftOperationWed().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.THURSDAY && this.getShiftOperationThu().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY && this.getShiftOperationFri().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY && this.getShiftOperationSat().equals(NmrshiftdbConstants.TRUE) ||
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY && this.getShiftOperationSun().equals(NmrshiftdbConstants.TRUE)
        )
	        return true;
	    else
	        return false;
    }

    /**
	 * Tells if a structure input is required for this machine and the processing mode process.
	 * 
	 * @param process Which mode to check.
	 * @return true=structure input required, false=not required.
	 */
	public boolean isProableStructureMandatory(String process) {
		if(process.equals(NmrshiftdbConstants.PROCESS_CHANGER) && this.getMeasureChangerEnforceStructure().equals(NmrshiftdbConstants.TRUE))
			return true;
		if(process.equals(NmrshiftdbConstants.PROCESS_OPERATOR) && this.getMeasureOperatorEnforceStructure().equals(NmrshiftdbConstants.TRUE))
			return true;
		if(process.equals(NmrshiftdbConstants.PROCESS_SELF) && this.getMeasureSelfEnforceStructure().equals(NmrshiftdbConstants.TRUE))
			return true;
		if(process.equals(NmrshiftdbConstants.PROCESS_BARCODE) && this.getMeasureBarcodeEnforceStructure().equals(NmrshiftdbConstants.TRUE))
			return true;
		return false;
	}
	
	/**
	 * Tells if a certain experiment is possible on this machine during daytime.
	 * 
	 * @param experimentid The id of the experiment.
	 * @return true=possible, false=not possible.
	 * @throws TorqueException
	 */
	public boolean isExperimentPossibleDay(int experimentid) throws TorqueException{
		Criteria crit = new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID,this.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID,experimentid);
		crit.add(DBExperimentDBMachinePeer.VALID_DAY,NmrshiftdbConstants.TRUE);
		return DBExperimentDBMachinePeer.doSelect(crit).size()>0;
	}
	
	/**
	 * Tells if a certain experiment is possible on this machine during daytime.
	 * 
	 * @param experimentid The id of the experiment.
	 * @return true=possible, false=not possible.
	 * @throws TorqueException
	 */
	public boolean isExperimentCompulsory(int experimentid) throws TorqueException{
		Criteria crit = new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID,this.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID,experimentid);
		if(DBExperimentDBMachinePeer.doSelect(crit).size()>0){
			DBExperimentDBMachine expmach = ((DBExperimentDBMachine)DBExperimentDBMachinePeer.doSelect(crit).get(0));
			return expmach.getCompulsory().equals(NmrshiftdbConstants.TRUE);
		}else{
			return false;
		}
	}

    /**
     * Tells if a certain experiment is possible on this machine during nightime.
     * 
     * @param experimentid The id of the experiment.
     * @return true=possible, false=not possible.
     * @throws TorqueException
     */
    public boolean isExperimentPossibleNight(int experimentid) throws TorqueException{
        Criteria crit = new Criteria();
        crit.add(DBExperimentDBMachinePeer.MACHINE_ID,this.getMachineId());
        crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID,experimentid);
        crit.add(DBExperimentDBMachinePeer.VALID_NIGHT,NmrshiftdbConstants.TRUE);
        return DBExperimentDBMachinePeer.doSelect(crit).size()>0;
    }

    /**
	 * Tells the iconnmr name of a certain experiment on this machine ("" if not possible).
	 * 
	 * @param experimentid The id of the experiment.
	 * @return The name.
	 * @throws TorqueException
	 */
	public String getExperimentName(int experimentid) throws TorqueException{
		Criteria crit = new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID,this.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID,experimentid);
		if(DBExperimentDBMachinePeer.doSelect(crit).size()>0){
			DBExperimentDBMachine expmach = ((DBExperimentDBMachine)DBExperimentDBMachinePeer.doSelect(crit).get(0));
			return expmach.getNameIconnmr();
		}else{
			return "";
		}
	}
	
    /**
     * Tells the duration name of a certain experiment on this machine ("" if not possible).
     * 
     * @param experimentid The id of the experiment.
     * @return The duration in min:sec format.
     * @throws TorqueException
     */
    public String getExperimentDuration(int experimentid) throws TorqueException{
        Criteria crit = new Criteria();
        crit.add(DBExperimentDBMachinePeer.MACHINE_ID,this.getMachineId());
        crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID,experimentid);
        if(DBExperimentDBMachinePeer.doSelect(crit).size()>0){
            DBExperimentDBMachine expmach = ((DBExperimentDBMachine)DBExperimentDBMachinePeer.doSelect(crit).get(0));
            int min=expmach.getDuration()/60;
            return min+":"+(expmach.getDuration()-min*60);
        }else{
            return "";
        }
    }

    
    /**
	 * Gets all Machine_Conditions for this machine order by the "order by" column.
	 * 
	 * @return A list of DBMachineConditionObjects.
	 * @throws TorqueException
	 */
	public List<DBMachineCondition> getDBMachineConditionsOrdered() throws TorqueException{
        Criteria crit = new Criteria();
        crit.add(DBMachineConditionPeer.MACHINE_ID,this.getMachineId());
        crit.addAscendingOrderByColumn(DBMachineConditionPeer.ORDER_POSITION);
        return DBMachineConditionPeer.doSelect(crit);
	}
	
    /**
	 * Gets all Machine_Conditions for all machines order by the "order by" column.
	 * 
	 * @return A list of DBMachineConditionObjects.
	 * @throws TorqueException
	 */
	public List<DBMachineCondition> getDBMachineConditionsOrderedAllMachines() throws TorqueException{
        Criteria crit = new Criteria();
        List<DBMachineCondition> result = DBMachineConditionPeer.doSelect(crit);
        ListIterator<DBMachineCondition> iter = result.listIterator();
        while(iter.hasNext()){
        	DBMachineCondition machcon=iter.next();
            if(machcon.getMachineId()!=0){
                iter.remove();
            }
        }
        return result;
	}
	
	/**
	 * Tells if this machine is already fully booked. This is done by summing up the time needed 
	 * for the booked experiments and the changeover times. If this takes longer than the time limit 
	 * the machine is fully booked. The time limit is either the end of the current shift, if the machine 
	 * is in shift operation, or a single date during a 24 hour period, which is saved in the hour/minute 
	 * day fields.
	 * 
	 * @return true=fully booked, false=capacity available until end of period.
	 * @throws DataSetException 
	 * @throws TorqueException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public boolean runOutOfTime() throws DataSetException, TorqueException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
       Date endofperiod=new Date();
	   	Calendar calendar=new GregorianCalendar();
	   	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
	   	Method hourDay = this.getClass().getMethod("getHourDay"+day);
	   	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
	   	Method hourNight = this.getClass().getMethod("getHourNight"+day);
	   	Method minuteNight = this.getClass().getMethod("getMinuteNight"+day);
       if(!this.getShiftOperation(new Date())){
           //if we are not in shift operation, we use the day date as end of current period
           endofperiod.setHours((int)hourDay.invoke(this));
           endofperiod.setMinutes((int)minuteDay.invoke(this));
           //if the end of period is in the past, we add one day
           if(endofperiod.before(new Date()))
                   endofperiod=new Date(endofperiod.getTime()+ 1 * 24 * 60 * 60 * 1000);
       }else{
           Date now = new Date();
           if(now.getHours()>12 && now.getHours()*60+now.getMinutes()>=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this)){
               //if its past 12 and we are after the night shift start, then the day shift start is the end of period 
               endofperiod.setHours((int)hourDay.invoke(this));
               endofperiod.setMinutes((int)minuteDay.invoke(this));
               //and next day actually
               endofperiod=new Date(endofperiod.getTime()+ 1 * 24 * 60 * 60 * 1000);
           } else if(now.getHours()<=12 && now.getHours()*60+now.getMinutes()<(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this)){
               //if its before 12 and we are before the day shift start, then the day shift start is the end of period 
               endofperiod.setHours((int)hourDay.invoke(this));
               endofperiod.setMinutes((int)minuteDay.invoke(this));
               //and the night shift start is start of period
           } else {
               //else the night shift start is the end of the period
               endofperiod.setHours((int)hourNight.invoke(this));
               endofperiod.setMinutes((int)minuteNight.invoke(this));
           }
       }
       long duration = getDurationOfExperiments();
	   //get the difference from now to the end of period
	   long lengthofperiod = endofperiod.getTime()-new Date().getTime();
	   if(lengthofperiod>duration*1000)
	       return false;
	   else
	       return true;
	}
	
	/**
	 * Tells when this machine will have finished its current samples.
	 * 
	 * @return The end time like hour:minute.
	 * @throws TorqueException
	 * @throws DataSetException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public String getBusyEnd() throws TorqueException, DataSetException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
	    Date dateend=new Date();
	    dateend.setTime(System.currentTimeMillis()+this.getDurationOfExperiments()*1000);
	    return dateend.getHours()+":"+dateend.getMinutes();
	}
	
	/**
	 * Tells the duration of all outstanding experiments including switchover times (in seconds).
	 * 
	 * @return The duration.
	 * @throws TorqueException 
	 * @throws DataSetException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private long getDurationOfExperiments() throws TorqueException, DataSetException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
	    	Calendar calendar=new GregorianCalendar();
	    	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
	    	Method hourDay = this.getClass().getMethod("getHourDay"+day);
	    	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
	    	Method hourNight = this.getClass().getMethod("getHourNight"+day);
	    	Method minuteNight = this.getClass().getMethod("getMinuteNight"+day);
	       Date startofperiod=new Date();
	       if(!this.getShiftOperation(new Date())){
	           //if we are not in shift operation, we use the day date as start of current period
	           startofperiod.setHours((int)hourDay.invoke(this));
	           startofperiod.setMinutes((int)minuteDay.invoke(this));
	           //if the end of period is in the future, we subtract one day
	           if(startofperiod.after(new Date()))
	               startofperiod=new Date(startofperiod.getTime()- 1 * 24 * 60 * 60 * 1000);
	       }else{
	           Date now = new Date();
	           if(now.getHours()>12 && now.getHours()*60+now.getMinutes()>=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this)){
	               //and the night shift start is start of period
	               startofperiod.setHours((int)hourNight.invoke(this));
	               startofperiod.setMinutes((int)minuteDay.invoke(this));
	           } else if(now.getHours()<=12 && now.getHours()*60+now.getMinutes()<(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this)){
	               //and the night shift start is start of period
	               startofperiod.setHours((int)hourNight.invoke(this));
	               startofperiod.setMinutes((int)minuteNight.invoke(this));
	               //and the day before actually
	               startofperiod=new Date(startofperiod.getTime()- 1 * 24 * 60 * 60 * 1000);
	           } else {
	               //and the day shift start is start of period
	               startofperiod.setHours((int)hourDay.invoke(this));
	               startofperiod.setMinutes((int)minuteDay.invoke(this));               
	           }
	       }
	       //This gives us the duration of all the experiments (in seconds)
	       SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
	       String sql = "select sum(EXPERIMENT_MACHINE.DURATION) from SAMPLE join EXPERIMENT_SAMPLE on (SAMPLE.SAMPLE_ID=EXPERIMENT_SAMPLE.SAMPLE_ID) join EXPERIMENT_MACHINE on (SAMPLE.MACHINE=EXPERIMENT_MACHINE.MACHINE_ID and EXPERIMENT_SAMPLE.EXPERIMENT_ID=EXPERIMENT_MACHINE.EXPERIMENT_ID) where SAMPLE.FINISHED ='false' and MACHINE_ID="+this.getMachineId()+" and SAMPLE.DATE>='"+formatter.format(startofperiod)+"'";
	       List<Record> durationresult=DBMachinePeer.executeQuery(sql);
	       long duration = 0;
	       if(!((Record)durationresult.get(0)).getValue(1).isNull())
	           duration = ((Record)durationresult.get(0)).getValue(1).asLong();
	       //This gives us the changeover times (in seconds)
	       sql = "select count(*) * MACHINE.CHANGE_TIME from SAMPLE join MACHINE on SAMPLE.MACHINE=MACHINE.MACHINE_ID where FINISHED ='false' and MACHINE="+this.getMachineId()+" and SAMPLE.DATE>='"+formatter.format(startofperiod)+"'";
	       durationresult=DBMachinePeer.executeQuery(sql);
	       if(!((Record)durationresult.get(0)).getValue(1).isNull())
	           duration += ((Record)durationresult.get(0)).getValue(1).asLong();
    	   return duration;
    }

    /**
     * Tells which holders are available on this machine, considering samples since start of period.
     * 
     * @return List of free holders.
     * @throws TorqueException 
     * @throws DataSetException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public List<Integer> getFreeHolders() throws TorqueException, DataSetException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileNotFoundException, IOException {
	    	Calendar calendar=new GregorianCalendar();
	    	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
	    	Method hourDay = this.getClass().getMethod("getHourDay"+day);
	    	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
           //Look back to start of day period
           Date startofperiod=new Date();
           //if we are from midnight to start of period, we nned -24h
           if(startofperiod.getHours()<(int)hourDay.invoke(this) || (startofperiod.getHours()==(int)hourDay.invoke(this) && startofperiod.getMinutes()<(int)minuteDay.invoke(this))){
        	   Calendar cal = Calendar.getInstance();
        	   cal.add(Calendar.HOUR, -24);
               startofperiod=cal.getTime();
           }
	       boolean isshiftoperation=getShiftOperation(startofperiod);
	       int days=0;
	       while(!isshiftoperation  && days<7) {
	    	   Calendar cal = Calendar.getInstance();
        	   cal.add(Calendar.HOUR, -24);
               startofperiod=cal.getTime();
	    	   isshiftoperation=getShiftOperation(startofperiod);
	    	   days++;
	       }
           startofperiod.setHours((int)hourDay.invoke(this));
           startofperiod.setMinutes((int)minuteDay.invoke(this));
           //We get the time from /etc/clear/machine
           File clearfile=new File(File.separator+"etc"+File.separator+"clear"+File.separator+this.getName());
           if(clearfile.exists()) {
        	   Date cleartime=new Date(1000*Long.parseLong(IOUtils.toString(new FileInputStream(clearfile)).trim()));
       		   startofperiod=cleartime;
           }
           //This gives us busy holders
           SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
           String sql = "select distinct(SAMPLE.HOLDER_NO) from SAMPLE join EXPERIMENT_SAMPLE on (SAMPLE.SAMPLE_ID=EXPERIMENT_SAMPLE.SAMPLE_ID) join EXPERIMENT_MACHINE on (SAMPLE.MACHINE=EXPERIMENT_MACHINE.MACHINE_ID and EXPERIMENT_SAMPLE.EXPERIMENT_ID=EXPERIMENT_MACHINE.EXPERIMENT_ID) where MACHINE_ID="+this.getMachineId()+" and SAMPLE.DATE>='"+formatter.format(startofperiod)+"' and HOLDER_NO>-1";
           List<Integer> holders=new ArrayList<Integer>();
           for(int i=1;i<=this.getNumberOfHolders();i++){
               holders.add(i);
           }
           List<Record> sqlresult = DBMachinePeer.executeQuery(sql);
           for(int i=0;i<sqlresult.size();i++){
               holders.remove(((Record)sqlresult.get(i)).getValue(1).asIntegerObj());
           }
           return holders;
    }

    
    /**
	 * Tells the time this machine takes to change between samples.
	 * 
	 * @return The change time in min:sec format.
	 */
	public String getChangeTimeString(){
        int min=this.getChangeTime()/60;
        return min+":"+(this.getChangeTime()-min*60);	    
	}
	
	/**
	 * Tells if the shift operation is on at least one any day in the week.
	 * 
	 * @return true=shift operation is on at leat one day a week, false=off seven days a week.
	 */
	public boolean getAnyShiftOperation(){
	    if(this.getShiftOperationMon().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationTue().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationWed().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationThu().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationFri().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationSat().equals(NmrshiftdbConstants.TRUE) ||
            this.getShiftOperationSun().equals(NmrshiftdbConstants.TRUE)
        )
	        return true;
	    else
	        return false;
	}
	
	/**
	 * Gives a string which contains the end of the "busy until" period.
	 * 
	 * @return The end of the "busy until" period.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public String getPeriodEnd() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    	Calendar calendar=new GregorianCalendar();
    	String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    	Method hourDay = this.getClass().getMethod("getHourDay"+day);
    	Method minuteDay = this.getClass().getMethod("getMinuteDay"+day);
    	Method hourNight = this.getClass().getMethod("getHourNight"+day);
    	Method minuteNight = this.getClass().getMethod("getMinuteNight"+day);
       Date now = new Date();
       if(!this.getShiftOperation(new Date())){
           //if we are not in shift operation, we use the day date as end of current period
           return (int)hourDay.invoke(this)+":"+(int)minuteDay.invoke(this);
       }else{
           if(now.getHours()>12 && now.getHours()*60+now.getMinutes()>=(int)hourNight.invoke(this)*60+(int)minuteNight.invoke(this)){
               //if its past 12 and we are after the night shift start, then the day shift start is the end of period 
               return (int)hourDay.invoke(this)+":"+(int)minuteDay.invoke(this);
           } else if(now.getHours()<=12 && now.getHours()*60+now.getMinutes()<(int)hourDay.invoke(this)*60+(int)minuteDay.invoke(this)){
               //if its before 12 and we are before the day shift start, then the day shift start is the end of period 
               return (int)hourDay.invoke(this)+":"+(int)minuteDay.invoke(this);
           } else {
               //else the night shift start is the end of the period
               return (int)hourNight.invoke(this)+":"+(int)minuteNight.invoke(this);
           }
       }
	}
	
    /**
     * Get the DefaultMode. This gives day or night mode depending on time.
     *
     * @return String
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    public String getDefaultModeActual() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
		if(isDuringDayShift())
			return super.getDefaultMode();
		else
			return super.getDefaultModeNight();
    }
    
    /**
     * Sets the shift operation value for a particular day of the weeek.
     * 
     * @param day The day given as Monday, Tuesday....
     * @param value true=shift operation active on that day, false=not active.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public void setShiftOperation(String day, boolean value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	Method method = this.getClass().getMethod("setShiftOperation"+day.substring(0,3), String.class);
    	method.invoke(this,value ? NmrshiftdbConstants.TRUE : NmrshiftdbConstants.FALSE);    	
    }
    
    /**
     * Tells if this machine is in shift operation for a particular day of the week.
     * 
     * @param day The day given as Monday, Tuesday....
     * @return true=shift operation active on that day, false=not active.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public boolean getShiftOperation(String day) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	Method method = this.getClass().getMethod("getShiftOperation"+day.substring(0,3));
    	return ((String)method.invoke(this)).equals("true");    	
    }
    
    /**
     * Gets the hour value of the day shift start.
     * 
     * @param day The day given as Monday, Tuesday....
     * @return The hour value.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public int getHourDay(String day) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    	Method method = this.getClass().getMethod("getHourDay"+day);
    	return (int)method.invoke(this);    	
    }

    /**
     * Gets the minute value of the day shift start.
     * 
     * @param day The day given as Monday, Tuesday....
     * @return The minute value.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public int getMinuteDay(String day) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    	Method method = this.getClass().getMethod("getMinuteDay"+day);
    	return (int)method.invoke(this);    	
    }

    /**
     * Gets the hour value of the night shift start.
     * 
     * @param day The day given as Monday, Tuesday....
     * @return The hour value.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public int getHourNight(String day) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    	Method method = this.getClass().getMethod("getHourNight"+day);
    	return (int)method.invoke(this);    	
    }

    /**
     * Gets the minute value of the night shift start.
     * 
     * @param day The day given as Monday, Tuesday....
     * @return The minute value.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public int getMinuteNight(String day) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
    	Method method = this.getClass().getMethod("getMinuteNight"+day);
    	return (int)method.invoke(this);    	
    }

	/**
	 * Tells if there are any experiments for this machine in a specific category.
	 * 
	 * @param tab The category.
	 * @return false=no experiments, yes=at least one experiment.
	 * @throws TorqueException
	 */
	public boolean hasExperimentsForTab(DBExperimentCategory tab) throws TorqueException {
		Criteria crit=new Criteria();
		crit.addJoin(DBExperimentDBMachinePeer.EXPERIMENT_ID, DBExperimentPeer.EXPERIMENT_ID);
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, this.getMachineId());
		crit.add(DBExperimentPeer.EXPERIMENT_CATEGORY_ID, tab.getExperimentCategoryId());
		return DBExperimentPeer.doSelect(crit).size()>0;
	}
	

    /**
     * Gets the solvents for this machine in automation mode, ordered alphabetically.
     *
     * @return the collection of associated objects
     * @throws TorqueException
     */
	public List<DBMachineCondition> getDBMachineConditions()
	        throws TorqueException
	    {
			Criteria crit=new Criteria();
			crit.add(DBMachineConditionPeer.MACHINE_ID, this.getMachineId());
			crit.addJoin(DBMachineConditionPeer.CONDITION_ID, DBConditionPeer.CONDITION_ID);
			crit.addAscendingOrderByColumn(DBConditionPeer.VALUE);
			return DBMachineConditionPeer.doSelect(crit);
	    }
	
	public static boolean ipinprintrange(RunData data) throws FileNotFoundException, IOException {
		boolean ipinprintrange=true;
	    String ips=GeneralUtils.getNmrshiftdbProperty("ipsforprint", data);
	    if(ips!=null) {
	    	String[] rangeips=ips.split("-");
	    	String remoteaddr=data.getRequest().getRemoteAddr();
	    	long localip = GeneralUtils.ipToLong(remoteaddr);
	    	long fromip = GeneralUtils.ipToLong(rangeips[0]);
	    	long toip = GeneralUtils.ipToLong(rangeips[1]);
	    	if(localip < fromip || localip > toip) {
	    		ipinprintrange=false;
	    	}
	    }
	    return ipinprintrange;
	}
}



