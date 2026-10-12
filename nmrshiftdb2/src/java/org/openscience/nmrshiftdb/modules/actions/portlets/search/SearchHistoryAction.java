/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2015 Stefan Kuhn
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.VectorWithSessionBinding;

/**
 *  This is the portlet showing the choice of measured or calculated spectra.
 *
 * @author     Stefan Kuhn
 * @created    11. April 2002
 */
public class SearchHistoryAction extends VelocityPortletAction
{

	/**
     * Builds the context for the portlet.
     *
     * @param  data  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
	 */
    @Override
    protected void buildNormalContext(VelocityPortlet portlet, Context context,
            RunData data) {
 		HttpServletRequest req = data.getRequest();
		HttpSession session = req.getSession();
		String action = req.getParameter("nmrshiftdbaction");
		if (action == null || action.equals("nocontinue")) {
			action = "";
		}
		try{
			data.getSession().setAttribute(VectorWithSessionBinding.REQUEST_IP, data.getRequest().getRemoteAddr());
			if(session.getAttribute(NmrshiftdbConstants.SEARCHHISTORY)==null && TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getSearchhistory()!=null && !data.getUser().getUserName().equals("anon")){
				session.setAttribute(NmrshiftdbConstants.SEARCHHISTORY, GeneralUtils.getFromXMLString(new String(TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) data.getUser()).getUserId())).getSearchhistory())));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		ArrayList<ValuesForVelocityBean> v=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.SEARCHHISTORY);
		context.put("searchhistory",v);
    	context.put("nmrshiftdbconstants", new NmrshiftdbConstants());
	}

    /**
     * Clears the search history.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    public void doClearhistory(RunData data, Context context) {
        ArrayList<ValuesForVelocityBean> v=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(data.getSession(),NmrshiftdbConstants.SEARCHHISTORY);
        v.clear();
    }
}

