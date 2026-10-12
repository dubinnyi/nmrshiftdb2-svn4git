/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2014,2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.modules.actions.portlets.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBExperiment;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

import com.workingdogs.village.Record;

/**
 * Handles actions for the sample search portlet.
 * 
 * @author shk3
 *
 */
public class SearchSamplePropertiesAction extends VelocityPortletAction{
	
	
	/**
	 * Default behavior for the portlet action.
	 * 
	 * @param context The context to fill.
	 * @param data The current RunData.
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
		try{
		    //Populate with experiment types
		    String sql5="select EXPERIMENT_CATEGORY_ID from EXPERIMENT_CATEGORY order by ORDER_POSITION";
		    List<Record> v9=DBSamplePeer.executeQuery(sql5);
		    List<List<DBExperiment>> experiments = new ArrayList<List<DBExperiment>>();
		    for(int i=0;i<v9.size();i++){
		    	Criteria crit = new Criteria();
		    	crit.add(DBExperimentPeer.EXPERIMENT_CATEGORY_ID,((Record)v9.get(i)).getValue(1).asInt());
		    	crit.add(DBExperimentPeer.VALID, NmrshiftdbConstants.TRUE);
		    	crit.addAscendingOrderByColumn(DBExperimentPeer.EXPERIMENT_ID);
		    	experiments.add(DBExperimentPeer.doSelect(crit));
		    }
		    context.put("experiments", experiments);
			context.put("searchName", data.getSession().getAttribute("searchstringsample"));
			context.put("searchaction",data.getSession().getAttribute("searchactionsample"));
			context.put("searchfield",data.getSession().getAttribute("sampleprops"));
			context.put("searchsampleexperiments",data.getSession().getAttribute("searchsampleexperiments")==null ? new ArrayList<Integer>() : data.getSession().getAttribute("searchsampleexperiments"));
			context.put("constants",new NmrshiftdbConstants());
		}catch(Exception ex){
	    	context.put("error",GeneralUtils.logError(ex,"order action",data,true));			
		}
	}
}
