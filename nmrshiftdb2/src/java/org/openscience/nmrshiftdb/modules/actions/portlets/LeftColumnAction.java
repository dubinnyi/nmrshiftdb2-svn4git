/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2014 Stefan Kuhn
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

import java.io.IOException;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.UrlTool;

public class LeftColumnAction extends VelocityPortletAction {
    
    /**
     * Default behavior for the portlet action. Delegates to buildOrderContext.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
        try {
            context.put("labserver", new UrlTool().isLabserver());
        } catch (IOException e) {
            GeneralUtils.logError(e, "leftcolumn",  data, false);
        }
    }
}
