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

/**
 *  This is the portlet showing the screen for selecting keywords
 *
 * @author     Stefan Kuhn
 * @created    11. April 2002
 */
public class SearchKeywordsAction extends VelocityPortletAction
{

	/**
	 * Builds the context for the portlet.
	 *
     * @param  data  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
	 */
	public void buildNormalContext(VelocityPortlet portlet, Context context, RunData runData)
	{
		try{
	      HttpServletRequest req = runData.getRequest();
	      HttpSession session = req.getSession();
	      if(session.getAttribute("frag")==null)
	        context.put("frag","total");
	      else
	        context.put("frag",session.getAttribute("frag"));
	      context.put("data",runData);
	      context.put("mode",session.getAttribute("mode"));
		}
		catch (Exception ex)
		{
		    GeneralUtils.logError(ex,"search keywords",runData,true);
		}
	}
}

