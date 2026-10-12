/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012-2015 Stefan Kuhn
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

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;


/**
 * Handles actions for the search by conditions portlet.
 *
 * @author     shk3
 * @created    23. April 2003
 */
public class SearchConditionAction extends VelocityPortletAction {
	public static final String CONDITIONS_MEASURED_KEY = "conditionsmeasured";
	public static final String CONDITIONS_CALCULATED_KEY = "conditionscalculated";
	public static final String CONDITIONS_PREDICTED_KEY = "conditionspredicted";
	public static final String LASTACTION_KEY="lastaction";

	/**
	 * Default behavior for the portlet action. Delegates to buildConditionContext.
	 * 
	 * @param context The context to fill.
	 * @param data The current RunData.
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
	    try{
	    	buildConditionContext(context, data);
	    }
	    catch(Exception ex){
	      GeneralUtils.logError(ex,"search condition",data,true);
	    }
	}
  
	/**
	 * Fills the context with all the data to display the portlet.
	 * 
	 * @throws TorqueException 
	 */
	protected void buildConditionContext(Context context, RunData data) throws TorqueException{
      Criteria crit=new Criteria();
      crit.add(DBConditionTypePeer.CONDITION_TYPE,"m");
      context.put(CONDITIONS_MEASURED_KEY,DBConditionTypePeer.doSelect(crit));
      crit.add(DBConditionTypePeer.CONDITION_TYPE,"c");
      context.put(CONDITIONS_CALCULATED_KEY,DBConditionTypePeer.doSelect(crit));
      crit.add(DBConditionTypePeer.CONDITION_TYPE,"p");
      context.put(CONDITIONS_PREDICTED_KEY,DBConditionTypePeer.doSelect(crit));
      if(data.getSession().getAttribute(LASTACTION_KEY)==null){
    	  ArrayList<Integer> v=new ArrayList<Integer>();
    	  v.add(1);
    	  data.getSession().setAttribute(LASTACTION_KEY,v);
      }
      context.put(LASTACTION_KEY,data.getSession().getAttribute(LASTACTION_KEY));
      context.put("mode",data.getSession().getAttribute("mode"));
	}
}


