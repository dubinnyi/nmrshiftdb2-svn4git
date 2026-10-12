/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2012,2013,2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.modules.actions.portlets;

import javax.servlet.http.HttpSession;

import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * This is the portlet handling auto assignments of spectra to molecules.
 *
 * @author     Stefan Kuhn
 * @created    7. Marchr 2012
 */
public class AssignmentToolAction extends PredictAction {

    /* (non-Javadoc)
     * @see org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction#buildNormalContext(org.apache.jetspeed.portal.portlets.VelocityPortlet, org.apache.velocity.context.Context, org.apache.turbine.util.RunData)
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
        HttpSession session = data.getSession();
        session.setAttribute(MODE,MODE_ASSIGNMENT);
        if (session.getAttribute("spectrumassignment") == null)
            session.setAttribute("spectrumassignment", "Format ()=optional:\r\nShift(;Intensity)(Multiplicity)\r\nShift(;Intensity)(Multiplicity)");
        fillContext(context, data);
    }
}

