/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2013,2015 Stefan Kuhn
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


// JDK classes
import java.sql.Connection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Criteria.Criterion;
import org.openscience.nmrshiftdb.modules.actions.portlets.lims.OrderAction;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;


/** 
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public  class DBSample 
    extends org.openscience.nmrshiftdb.om.BaseDBSample
    implements Persistent
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7839899133787633279L;

	/**
	 * Tells if this entry can be deleted by a lab user (defined by it not having any files attached and not being in state pending and not being an automation order).
	 * 
	 * @return True or false.
	 * @throws Exception Database problems.
	 */
	public boolean isDeletable() throws Exception{
		if(this.getFinished().equals(OrderAction.PENDING))
			return false;
		if(this.getDBMachine().getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && this.getFinished().equals(NmrshiftdbConstants.TRUE))
			return false;
		Date oneweekbefore=new Date(System.currentTimeMillis()-324000000l);
		if(this.getDBMachine().getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && this.getHolderNo()!=-1){
			if(this.getDBRawFiles().size()>0){
				return false;
			}else if(this.getDate().before(oneweekbefore)){
				return true;
			}else{
				return false;
			}
		}
		if(this.getDBMachine().getIconnmrIntegration().equals(NmrshiftdbConstants.TRUE) && this.getHolderNo()!=-1 && this.getDBRawFiles().size()>0){
		    return false;
		}
		Criteria crit=new Criteria();
		crit.add(DBRawFilePeer.SAMPLE_ID,this.getSampleId());
		List<DBRawFile> v=DBRawFilePeer.doSelect(crit);
		return v.size()==0;
	}
	
	/**
	 * Gets the attachment as a string.
	 * 
	 * @return The attachment.
	 */
	public String getAttachmentStr(){
		return new String(this.getAttachment());
	}
	
	/**
	 * Tell if the user comment of this sample is set.
	 * 
	 * @return True or false.
	 */
	public boolean getUsersIdCommentIsSet(){
		return !new String(this.getUsersIdComment()).equals("");
	}
	
	/**
	 * Tells if an attachment is set for this sample
	 * 
	 * @return True or false.
	 */
	public boolean getAttachmentIsSet(){
		return !new String(this.getAttachment()).equals("");
	}
	
	/**
	 * Tells if this entry can be deleted by a lab worker (defined by it not having any files attached).
	 *
	 * @return True or false.
	 * @throws Exception Database problems.
	 */
	public boolean isDeletableWorker() throws Exception{
		Criteria crit=new Criteria();
		crit.add(DBRawFilePeer.SAMPLE_ID,this.getSampleId());
		List<DBRawFile> v=DBRawFilePeer.doSelect(crit);
		return v.size()==0;
	}

	/**
	 * Deletes this sample and all entries depending on it via FK.
	 * 
	 * @throws TorqueException Database Problems.
	 */
	public void delete(Connection conn) throws TorqueException {
        Criteria crit=new Criteria();
        crit.add(DBRawFilePeer.SAMPLE_ID,this.getSampleId());
        List<DBRawFile> v=DBRawFilePeer.doSelect(crit, conn);
        for(int i=0;i<v.size();i++){
        	DBRawFile rf=(DBRawFile)v.get(i);
            Criteria crit2=new Criteria();
            crit2.add(DBRawFilePeer.RAW_FILE_ID,rf.getRawFileId());
            DBRawFileDBSpectrumPeer.doDelete(crit2, conn);        	
        }
        DBRawFilePeer.doDelete(crit, conn);
        crit=new Criteria();
        crit.add(DBExperimentDBSamplePeer.SAMPLE_ID,this.getSampleId());
        DBExperimentDBSamplePeer.doDelete(crit, conn);
        crit=new Criteria();
        crit.add(DBSamplePeer.SAMPLE_ID,this.getSampleId());
        DBSamplePeer.doDelete(crit, conn);
	}

    /**
     * Gets all experiment_samples for this sample, ordered in descending order by experiment_category_id.
     * This makes 13c experiments come first - a very specific method.
     * 
     * @return
     * @throws TorqueException
     */
    public List<DBExperimentDBSample> getDBExperimentDBSamplesOrderedByCategory() throws TorqueException {
        Criteria crit=new Criteria();
        crit.add(DBExperimentDBSamplePeer.SAMPLE_ID, this.getSampleId());
        crit.addJoin(DBExperimentDBSamplePeer.EXPERIMENT_ID, DBExperimentPeer.EXPERIMENT_ID);
        crit.addAscendingOrderByColumn(DBExperimentPeer.AUTOMATION_ORDER);
        return DBExperimentDBSamplePeer.doSelect(crit);
    }
    
    public int getExperimentNumber() throws TorqueException, DataSetException{
    	//we find out how many different experiments should be measured
    	List<Record> result = DBExperimentDBSamplePeer.executeQuery("select distinct EXPERIMENT_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where SUB_EXPERIMENT_1_ID is null and  SAMPLE_ID ="+this.getSampleId()+" union select distinct SUB_EXPERIMENT_1_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+this.getSampleId()+" and SUB_EXPERIMENT_1_ID is not null union select distinct SUB_EXPERIMENT_2_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+this.getSampleId()+" and SUB_EXPERIMENT_2_ID is not null union select distinct SUB_EXPERIMENT_3_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+this.getSampleId()+" and SUB_EXPERIMENT_3_ID is not null");
    	return result.size();
    }

    
    public List<DBExperimentDBSampleDBParameter> getDBExperimentDBSampleDBParameters(DBExperiment dbExperiment, int sequence) throws TorqueException {
		Criteria crit=new Criteria();
		crit.add(DBExperimentDBSampleDBParameterPeer.SAMPLE_ID,this.getSampleId());
		crit.add(DBExperimentDBSampleDBParameterPeer.SEQUENCE,sequence);
		if(dbExperiment.getDBExperimentRelatedBySubExperiment1Id()==null){
			crit.add(DBExperimentDBSampleDBParameterPeer.EXPERIMENT_ID, dbExperiment.getExperimentId());
		}else{
			Criterion c1= crit.getNewCriterion(DBExperimentDBSampleDBParameterPeer.EXPERIMENT_ID, dbExperiment.getSubExperiment1Id(),Criteria.EQUAL);
			Criterion c2= crit.getNewCriterion(DBExperimentDBSampleDBParameterPeer.EXPERIMENT_ID, dbExperiment.getSubExperiment2Id(),Criteria.EQUAL);
			if(dbExperiment.getDBExperimentRelatedBySubExperiment3Id()!=null){
				Criterion c3= crit.getNewCriterion(DBExperimentDBSampleDBParameterPeer.EXPERIMENT_ID, dbExperiment.getSubExperiment3Id(),Criteria.EQUAL);
				crit.and(c1.or(c2).or(c3));
			}else{
				crit.and(c1.or(c2));
			}
		}
		return DBExperimentDBSampleDBParameterPeer.doSelect(crit);
	}

	public List<DBExperimentDBSampleDBParameter> getDBExperimentDBSampleDBParametersOrderedBySequence() throws TorqueException {
		Criteria criteria=new Criteria();
		criteria.add(DBExperimentDBSampleDBParameterPeer.SAMPLE_ID, getSampleId() );
		criteria.addAscendingOrderByColumn(DBExperimentDBSampleDBParameterPeer.SEQUENCE);
        return DBExperimentDBSampleDBParameterPeer.doSelect(criteria);
	}
}



