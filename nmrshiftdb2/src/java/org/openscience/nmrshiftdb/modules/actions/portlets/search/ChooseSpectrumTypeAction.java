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
package org.openscience.nmrshiftdb.modules.actions.portlets.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

/**
 * Portlet for changing various settings for searches (spectrum types etc.).
 *
 * @author     shk3
 * @created    10. February 2004
 */
public class ChooseSpectrumTypeAction extends VelocityPortletAction {

	/**
	 * Default behavior for the portlet action. Delegates to buildTypesContext.
	 * 
	 * @param context The context to fill.
	 * @param data The current RunData.
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
	    try{
	      buildTypesContext(context,data);
	    }
	    catch(Exception ex){
	      GeneralUtils.logError(ex,"order action",data,true);
	    }
	}


  /**
   * Fills the context with all the data to display the portlet.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   * @throws TorqueException
   * @throws IOException 
   * @throws FileNotFoundException 
   */
  private void buildTypesContext(Context context, RunData data) throws TorqueException, FileNotFoundException, IOException {
    @SuppressWarnings("unchecked")
    List<DBSpectrumType> types=(List<DBSpectrumType>)data.getSession().getAttribute("choosenspectrumtypes");
    if(types==null){
      Criteria crit=new Criteria();
      crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID, 100, Criteria.LESS_THAN);
      types=DBSpectrumTypePeer.doSelect(crit);
      for(int i=0;i<types.size();i++){
        ((DBSpectrumType)types.get(i)).setChoosen(true);
      }
      data.getSession().setAttribute("trueonly",new Boolean(true));
      data.getSession().setAttribute(NmrshiftdbConstants.JCAMPONLY,new Boolean(false));
      data.getSession().setAttribute(NmrshiftdbConstants.RAWONLY,new Boolean(false));
      data.getSession().setAttribute(NmrshiftdbConstants.LABGROUP_ONLY,new Boolean(false));
      data.getSession().setAttribute("assignedonly",new Boolean(false));
    }
    data.getSession().setAttribute("choosenspectrumtypes",types);
    context.put("spectra",types);
    context.put("trueonly",data.getSession().getAttribute("trueonly"));
    context.put("jcamp",data.getSession().getAttribute(NmrshiftdbConstants.JCAMPONLY));
    context.put("raw",data.getSession().getAttribute(NmrshiftdbConstants.RAWONLY));
    context.put("labgroup_only",data.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY));
    context.put("assignedonly",data.getSession().getAttribute("assignedonly"));
    context.put("mode",data.getSession().getAttribute("mode"));
    if(data.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT)==null)
        data.getSession().setAttribute(NmrshiftdbConstants.JCAMP_STRICT,new Boolean(false));
    context.put(NmrshiftdbConstants.JCAMP_STRICT,data.getSession().getAttribute(NmrshiftdbConstants.JCAMP_STRICT));
    context.put("unassigned", GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED).equals("true"));
  }
  
  
  /**
   * Handles submitting of settings.
   * 
   * @param context The context to fill.
   * @param data The current RunData.
   */
	public void doSubmittypes(RunData data, Context context) {
	    try{
	      @SuppressWarnings("unchecked")
	      List<DBSpectrumType> types=(List<DBSpectrumType>)data.getSession().getAttribute("choosenspectrumtypes");
	      for(int i=0;i<types.size();i++){
	        if(data.getParameters().get(((DBSpectrumType)types.get(i)).getName())!=null && data.getParameters().get(((DBSpectrumType)types.get(i)).getName()).equals("true")){
	          ((DBSpectrumType)types.get(i)).setChoosen(true);
	        }else{
	          ((DBSpectrumType)types.get(i)).setChoosen(false);
	        }
	      }
	      if(data.getParameters().get("trueonly")!=null && data.getParameters().get("trueonly").equals("trueonly")){
	    	  data.getSession().setAttribute("trueonly",new Boolean(true));
	      }else{
	    	  data.getSession().setAttribute("trueonly",new Boolean(false));
	      }
	      if(data.getParameters().get(NmrshiftdbConstants.LABGROUP_ONLY)!=null && data.getParameters().get(NmrshiftdbConstants.LABGROUP_ONLY).equals(NmrshiftdbConstants.LABGROUP_ONLY)){
	    	  data.getSession().setAttribute(NmrshiftdbConstants.LABGROUP_ONLY,new Boolean(true));
	      }else{
	    	  data.getSession().setAttribute(NmrshiftdbConstants.LABGROUP_ONLY,new Boolean(false));
	      }
	      if(data.getParameters().get(NmrshiftdbConstants.JCAMP_STRICT)!=null && data.getParameters().get(NmrshiftdbConstants.JCAMP_STRICT).equals(NmrshiftdbConstants.JCAMP_STRICT)){
	          data.getSession().setAttribute(NmrshiftdbConstants.JCAMP_STRICT,new Boolean(true));
	      }else{
	          data.getSession().setAttribute(NmrshiftdbConstants.JCAMP_STRICT,new Boolean(false));
	      }
	      boolean jcamp=data.getParameters().get("jcamp")!=null && data.getParameters().get("jcamp").equals("jcamp");
	      if(jcamp)
	    	  data.getSession().setAttribute(NmrshiftdbConstants.JCAMPONLY,new Boolean(true));
	        else
	        	data.getSession().setAttribute(NmrshiftdbConstants.JCAMPONLY,new Boolean(false));
	      boolean raw=data.getParameters().get("raw")!=null && data.getParameters().get("raw").equals("raw");
	      if(raw)
	    	  data.getSession().setAttribute(NmrshiftdbConstants.RAWONLY,new Boolean(true));
	        else
	        	data.getSession().setAttribute(NmrshiftdbConstants.RAWONLY,new Boolean(false));
	      buildTypesContext(context,data);
	    }
	    catch(Exception ex){
	      GeneralUtils.logError(ex,"choosespectrumtype/buildcontext",data,true);
	    }
	}
}
