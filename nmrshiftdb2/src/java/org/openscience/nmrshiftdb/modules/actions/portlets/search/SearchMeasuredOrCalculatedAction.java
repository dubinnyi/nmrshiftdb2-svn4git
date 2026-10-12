/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2015 Stefan Kuhn
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

/**
 *  This is the portlet showing the choice of measured or calculated spectra.
 *
 * @author     Stefan Kuhn
 * @created    11. April 2002
 */
public class SearchMeasuredOrCalculatedAction extends VelocityPortletAction
{

    /**
     * Default behavior for the portlet action.
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
        context.put("measured",(session.getAttribute(NmrshiftdbConstants.MEASUREDONLY)!=null)+"");
        context.put("calculated",(session.getAttribute(NmrshiftdbConstants.CALCULATEDONLY)!=null)+"");
        context.put("mode",session.getAttribute("mode"));
    }

    /**
     * Handles submitting of settings.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    public void doMeasuredornot(RunData data, Context context) {
        try{
            HttpServletRequest req = data.getRequest();
            HttpSession session = req.getSession();
            boolean measured=req.getParameter("measured")!=null && req.getParameter("measured").equals("measured");
            boolean calculated=req.getParameter("calculated")!=null && req.getParameter("calculated").equals("calculated");
            if(measured)
                session.setAttribute(NmrshiftdbConstants.MEASUREDONLY,NmrshiftdbConstants.MEASUREDONLY);
            else
                session.removeAttribute(NmrshiftdbConstants.MEASUREDONLY);
            if(calculated)
                session.setAttribute(NmrshiftdbConstants.CALCULATEDONLY,NmrshiftdbConstants.CALCULATEDONLY);
            else
                session.removeAttribute(NmrshiftdbConstants.CALCULATEDONLY);
        }
        catch(Exception ex){
          GeneralUtils.logError(ex,"choosespectrumtype/buildcontext",data,true);
        }
    }
}

