/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2014 Stefan Kuhn
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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Criteria.Criterion;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class DBExperiment
    extends org.openscience.nmrshiftdb.om.BaseDBExperiment
    implements Persistent
{
	private static final long serialVersionUID = -8904832186889842386L;
	private boolean chosen;
	private int count;

	/*
	 * Used in rendering of vm only
	 */
	public boolean isChosen() {
		return chosen;
	}

	/*
	 * Used in rendering of vm only
	 */
	public void setChosen(boolean chosen) {
		this.chosen = chosen;
	}

	/**
	 * Builds a JavaScript code for hiding experiments according to chosen machine.
	 * Not specific for an experiment, is in here only be able to use it directly in vm.
	 *
	 * @return The JS code.
	 * @throws TorqueException Database problems.
	 * @throws DataSetException Database problems.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public String getOnChangeCode(Map<String,Float> parampresets, RunData runData) throws TorqueException, DataSetException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileNotFoundException, IOException{
		StringBuffer sb=new StringBuffer("<script type=\"text/javascript\">\r\n");
		//We create a variable for each experiment to save height of parameter field
	    TurbineUser tuser= TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
		for(DBExperiment experiment : DBExperimentPeer.doSelect(new Criteria())){
			for(DBMachine machine : DBMachinePeer.getValidMachinesOfGroup(tuser.getLabgroupId())) {
				sb.append("var height"+experiment.getExperimentId()+"_"+machine.getMachineId()+"="+(experiment.getParametersForMachine(machine).size()*40+40)+";\r\n");//TODO size should not be fixed
			}
		}
		sb.append("function hideorshowexperiment(){");
		List<DBMachine> l=DBMachinePeer.doSelect(new Criteria());
		for(int i=0;i<l.size();i++){
			DBMachine machine=(DBMachine)l.get(i);
			DBExperimentCategory activetab=null;
			sb.append("if(document.getElementById('fstrengths').value==\""+machine.getMachineId()+"\"){\r\n");
			String sql ="select EXPERIMENT.EXPERIMENT_ID , EXPERIMENT_MACHINE.MACHINE_ID, COMPULSORY, DURATION from EXPERIMENT left join EXPERIMENT_MACHINE on (EXPERIMENT.EXPERIMENT_ID=EXPERIMENT_MACHINE.EXPERIMENT_ID and MACHINE_ID="+machine.getMachineId()+") where EXPERIMENT.VALID='true'";
			List<Record> experimentsformachine=DBExperimentPeer.executeQuery(sql);
			for(int k=0;k<experimentsformachine.size();k++){
				DBExperiment experiment = DBExperimentPeer.retrieveByPK(((Record)experimentsformachine.get(k)).getValue(1).asInt());//((Record)experimentsformachine.get(k)).getValue(2).isNull() || 
				sb.append("document.getElementById('experiment"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').style.visibility='"+(machine.getExperimentInVisible(((Record)experimentsformachine.get(k)).getValue(1).asInt()) ?  "hidden" : "visible") +"';\r\ndocument.getElementById('experiment"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').style.height='"+(machine.getExperimentInVisible(((Record)experimentsformachine.get(k)).getValue(1).asInt()) ?  "0px" : "20px") +"';\r\n");
				if(!((Record)experimentsformachine.get(k)).getValue(3).isNull() && ((Record)experimentsformachine.get(k)).getValue(3).asString().equals("true")){
					sb.append("document.getElementById('exp"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').disabled=true;\r\n");
					sb.append("document.getElementById('exp"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').checked=true;\r\n");
				}else{// if(!((Record)experimentsformachine.get(k)).getValue(3).isNull()){
					sb.append("document.getElementById('exp"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').disabled=false;\r\n");
					sb.append("document.getElementById('exp"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').checked=false;\r\n");
				}
				if(experiment.getParametersForMachine(machine).size()>0){
					sb.append("document.getElementById('span"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine.getMachineId()+"').style.visibility='"+(experiment.getParametersForMachine(machine, true, false).size()==0 || machine.getExperimentInVisible(((Record)experimentsformachine.get(k)).getValue(1).asInt()) ?  "hidden" : "visible") +"';\r\n");
					sb.append("document.getElementById('div"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine.getMachineId()+"').style.visibility='hidden';\r\n");
					sb.append("document.getElementById('div"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine.getMachineId()+"').style.height='0px';\r\n");
					List<DBExperimentDBMachineDBParameter> params = experiment.getExperimentSampleParametersForMachine(machine);
					for(DBExperimentDBMachineDBParameter expsampleparam : params){
						if(parampresets!=null && parampresets.containsKey(expsampleparam.getDBExperimentDBMachine().getExperimentId()+"_"+expsampleparam.getParameterId()))
							sb.append("document.getElementById('parameter_"+experiment.getExperimentId()+"_"+expsampleparam.getParameterId()+"_"+machine.getMachineId()+"').value='"+parampresets.get(expsampleparam.getDBExperimentDBMachine().getExperimentId()+"_"+expsampleparam.getParameterId())+"';\r\n");
						else
							sb.append("document.getElementById('parameter_"+experiment.getExperimentId()+"_"+expsampleparam.getParameterId()+"_"+machine.getMachineId()+"').value='"+expsampleparam.getDefaultValue()+"';\r\n");
					}
				}
				//we need to close the parameter form for all other machines
				for(int m=0;m<l.size();m++){
					if(i!=m) {
						DBMachine machine2=(DBMachine)l.get(m);
						if(experiment.getParametersForMachine(machine2).size()>0){
							sb.append("document.getElementById('span"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine2.getMachineId()+"').style.visibility='hidden';\r\n");
							sb.append("document.getElementById('div"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine2.getMachineId()+"').style.visibility='hidden';\r\n");
							sb.append("document.getElementById('div"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"_"+machine2.getMachineId()+"').style.height='0px';\r\n");
						}
					}
				}
				sb.append("document.getElementById('experimentnight"+((Record)experimentsformachine.get(k)).getValue(1).asInt()+"').style.visibility='"+(machine.getNightLimit()!=0 && ((Record)experimentsformachine.get(k)).getValue(4).asInt()>machine.getNightLimit() && !machine.getExperimentInVisible(((Record)experimentsformachine.get(k)).getValue(1).asInt()) ?  "visible" : "hidden") +"';\r\n");
			}
			//hide/show tabs
			List<DBExperimentCategory> tabs=DBExperimentCategoryPeer.doSelect(new Criteria());
			for(DBExperimentCategory tab : tabs){
				sb.append("document.getElementById('tabheader"+tab.getExperimentCategoryId()+"').style.display='"+(machine.hasExperimentsForTab(tab) ?  "block" : "none") +"';\r\n");
				if(machine.hasExperimentsForTab(tab) && activetab==null){
					activetab=tab;
				}
			}
			if(activetab!=null)
				sb.append("document.getElementById('tabheader"+activetab.getExperimentCategoryId()+"').click();\r\n");
			sb.append("}");
			if(i<l.size()-1){
				sb.append("else ");
			}
		}
		sb.append("}\r\n");
		sb.append("</script>");
		return sb.toString();
	}
	
	public List<DBExperimentParameter> getParametersForMachine() throws TorqueException{
		return getParametersForMachine(null);
	}
	
	public List<DBExperimentParameter> getParametersForMachine(DBMachine machine) throws TorqueException{
		Criteria crit = new Criteria();
		crit.addJoin(DBExperimentParameterPeer.PARAMETER_ID, DBExperimentDBMachineDBParameterPeer.PARAMETER_ID);
		crit.addJoin(DBExperimentDBMachineDBParameterPeer.EXPERIMENT_MACHINE_ID,DBExperimentDBMachinePeer.EXPERIMENT_MACHINE_ID);
		if(machine!=null) {
			crit.addJoin(DBExperimentDBMachinePeer.EXPERIMENT_MACHINE_ID,DBExperimentDBMachinePeer.EXPERIMENT_MACHINE_ID);
			crit.add(DBExperimentDBMachinePeer.MACHINE_ID, machine.getMachineId());
		}
		crit.add(DBExperimentParameterPeer.VISIBLE,NmrshiftdbConstants.TRUE);
		if(this.getDBExperimentRelatedBySubExperiment1Id()==null){
			crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getExperimentId());
		}else{
			Criterion c1= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment1Id(),Criteria.EQUAL);
			Criterion c2= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment2Id(),Criteria.EQUAL);
			if(this.getDBExperimentRelatedBySubExperiment3Id()!=null){
				Criterion c3= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment3Id(),Criteria.EQUAL);
				crit.and(c1.or(c2).or(c3));
			}else{
				crit.and(c1.or(c2));
			}
		}
		return DBExperimentParameterPeer.doSelect(crit);
	}
	
	public List<DBExperimentParameter> getParametersForMachine(DBMachine machine, boolean recurse, boolean showinvisible) throws TorqueException{
		Criteria crit = new Criteria();
		crit.addJoin(DBExperimentParameterPeer.PARAMETER_ID, DBExperimentDBMachineDBParameterPeer.PARAMETER_ID);
		crit.addJoin(DBExperimentDBMachineDBParameterPeer.EXPERIMENT_MACHINE_ID,DBExperimentDBMachinePeer.EXPERIMENT_MACHINE_ID);
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, machine.getMachineId());
		if(!showinvisible)
			crit.add(DBExperimentParameterPeer.VISIBLE,NmrshiftdbConstants.TRUE);
		if(!recurse || this.getDBExperimentRelatedBySubExperiment1Id()==null){
			crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getExperimentId());
		}else{
			Criterion c1= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment1Id(),Criteria.EQUAL);
			Criterion c2= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment2Id(),Criteria.EQUAL);
			if(this.getDBExperimentRelatedBySubExperiment3Id()!=null){
				Criterion c3= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment3Id(),Criteria.EQUAL);
				crit.and(c1.or(c2).or(c3));
			}else{
				crit.and(c1.or(c2));
			}
		}
		return DBExperimentParameterPeer.doSelect(crit);
	}
	
	public List<DBExperimentDBMachineDBParameter> getExperimentSampleParametersForMachine(DBMachine machine) throws TorqueException{
		Criteria crit = new Criteria();
		crit.addJoin(DBExperimentParameterPeer.PARAMETER_ID, DBExperimentDBMachineDBParameterPeer.PARAMETER_ID);
		crit.addJoin(DBExperimentDBMachineDBParameterPeer.EXPERIMENT_MACHINE_ID,DBExperimentDBMachinePeer.EXPERIMENT_MACHINE_ID);
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, machine.getMachineId());
		crit.add(DBExperimentParameterPeer.VISIBLE,NmrshiftdbConstants.TRUE);
		if(this.getDBExperimentRelatedBySubExperiment1Id()==null){
			crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getExperimentId());
		}else{
			Criterion c1= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment1Id(),Criteria.EQUAL);
			Criterion c2= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment2Id(),Criteria.EQUAL);
			if(this.getDBExperimentRelatedBySubExperiment3Id()!=null){
				Criterion c3= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment3Id(),Criteria.EQUAL);
				crit.and(c1.or(c2).or(c3));
			}else{
				crit.and(c1.or(c2));
			}
		}
		return DBExperimentDBMachineDBParameterPeer.doSelect(crit);
	}
	
	public List<DBExperimentDBSampleDBParameter> getParametersForSample(DBSample sample, int sequence) throws TorqueException{
		Criteria crit = new Criteria();
		crit.add(DBExperimentDBSampleDBParameterPeer.SAMPLE_ID, sample.getSampleId());
		crit.add(DBExperimentDBSampleDBParameterPeer.SEQUENCE,sequence);
		if(this.getDBExperimentRelatedBySubExperiment1Id()==null){
			crit.add(DBExperimentDBSampleDBParameterPeer.EXPERIMENT_ID, this.getExperimentId());
		}else{
			Criterion c1= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment1Id(),Criteria.EQUAL);
			Criterion c2= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment2Id(),Criteria.EQUAL);
			if(this.getDBExperimentRelatedBySubExperiment3Id()!=null){
				Criterion c3= crit.getNewCriterion(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getSubExperiment3Id(),Criteria.EQUAL);
				crit.and(c1.or(c2).or(c3));
			}else{
				crit.and(c1.or(c2));
			}
		}
		return DBExperimentDBSampleDBParameterPeer.doSelect(crit);
	}
	
	public String getOnClickForExludingExperiments(int experimentId) throws TorqueException{
	    Criteria crit=new Criteria();
        Criteria.Criterion criterion = crit.getNewCriterion(DBExcludingExperimentsPeer.EXPERIMENT_ID_1,this.getExperimentId(), Criteria.EQUAL);
        Criteria.Criterion criterion2 = crit.getNewCriterion(DBExcludingExperimentsPeer.EXPERIMENT_ID_2,this.getExperimentId(), Criteria.EQUAL);
        criterion.or(criterion2);
        crit.or(criterion);
        List<DBExcludingExperiments> excludings=DBExcludingExperimentsPeer.doSelect(crit);
        StringBuffer js=new StringBuffer();
        js.append("onclick=\"");
        for(int i=0;i<excludings.size();i++){
            DBExcludingExperiments excluding=(DBExcludingExperiments)excludings.get(i);
            js.append("if(document.getElementById('fstrengths').value=="+excluding.getMachineId()+"){\r\n");
            if(excluding.getExperimentId1()==experimentId){
                js.append("if(document.getElementById('exp"+excluding.getExperimentId1()+"').type == 'checkbox' && document.getElementById('exp"+excluding.getExperimentId1()+"').checked){\r\n");
                js.append("document.getElementById('exp"+excluding.getExperimentId2()+"').checked=false;}\r\n");
                js.append("if(document.getElementById('exp"+excluding.getExperimentId1()+"').tagName === 'SELECT' && document.getElementById('exp"+excluding.getExperimentId1()+"').checked){\r\n");
                js.append("document.getElementById('exp"+excluding.getExperimentId2()+"').selectedIndex = 0;}\r\n");
            }
            if(excluding.getExperimentId2()==experimentId){
                js.append("if(document.getElementById('exp"+excluding.getExperimentId2()+"').type == 'checkbox' && document.getElementById('exp"+excluding.getExperimentId2()+"').checked){\r\n");
                js.append("document.getElementById('exp"+excluding.getExperimentId1()+"').checked=false;}\r\n");
                js.append("if(document.getElementById('exp"+excluding.getExperimentId2()+"').tagName === 'SELECT' && document.getElementById('exp"+excluding.getExperimentId2()+"').checked){\r\n");
                js.append("document.getElementById('exp"+excluding.getExperimentId1()+"').selectedIndex = 0;}\r\n");
            }
            js.append("};");
        }
        js.append("\"");
        return js.toString();
	}
	
	/**
	 * Generates a list of integers from 0 to max_number of this experiment.
	 * 
	 * @return The list.
	 */
	public List<Integer> getFigures(){
		List<Integer> figures = new ArrayList<Integer>();
		for(int i=0;i<=getMaxNumber();i++){
			figures.add(new Integer(i));			
		}
		return figures;
	}

	/*
	 * Used in rendering of vm only
	 */
	public int getCount() {
		return count;
	}

	/*
	 * Used in rendering of vm only
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean isDefaultInMachine(DBMachine machine) throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, machine.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getExperimentId());
		List<DBExperimentDBMachine> result=DBExperimentDBMachinePeer.doSelect(crit);
		if(result.size()>0 && result.get(0).getCompulsory().equals("true"))
			return true;
		else
			return false;
	}

	public DBExperimentDBMachine getDBExperimentDBMachine(DBMachine dbMachine) throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBExperimentDBMachinePeer.MACHINE_ID, dbMachine.getMachineId());
		crit.add(DBExperimentDBMachinePeer.EXPERIMENT_ID, this.getExperimentId());
		List<DBExperimentDBMachine> result=DBExperimentDBMachinePeer.doSelect(crit);
		if(result.size()>0)
			return result.get(0);
		else
			return null;
	}
	
	public boolean isSelectedInSample(DBSample sample) throws TorqueException{
		Criteria crit=new Criteria();
		crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, sample.getSampleId());
		crit.add(DBExperimentDBSamplePeer.EXPERIMENT_ID, this.getExperimentId());
		return DBExperimentDBSamplePeer.doSelect(crit).size()>0;
	}
}
