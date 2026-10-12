/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013,2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.modules.actions.portlets.security;

import java.util.List;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBReviewGroupPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;

/**
 * This action sets up the template context for browsing of reviewgroups in the Turbine database. 
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a> Adopted by shk3
 */
public class ReviewGroupsBrowserAction extends VelocityPortletAction
{
    /** 
     * Build the maximized state content for this portlet. (Same as normal state).
     * 
     * @param portlet The velocity-based portlet that is being built.
     * @param context The velocity context for this request.
     * @param rundata The turbine rundata context for this request.
     */
    protected void buildMaximizedContext( VelocityPortlet portlet, 
                                          Context context,
                                          RunData rundata )
    {
        buildNormalContext( portlet, context, rundata);        
    }

    /** 
     * Build the configure state content for this portlet.
     * 
     * @param portlet The velocity-based portlet that is being built.
     * @param context The velocity context for this request.
     * @param rundata The turbine rundata context for this request.
     */
    protected void buildConfigureContext( VelocityPortlet portlet, 
                                          Context context,
                                          RunData rundata )
    {

        buildNormalContext( portlet, context, rundata);        
    }

    /** 
     * Build the normal state content for this portlet.
     * 
     * @param portlet The velocity-based portlet that is being built.
     * @param context The velocity context for this request.
     * @param rundata The turbine rundata context for this request.
     */
    protected void buildNormalContext( VelocityPortlet portlet, 
                                       Context context,
                                       RunData rundata )
    {
        try
        {
            Criteria criteria = new Criteria();
            List<DBReviewGroup> v=DBReviewGroupPeer.doSelect(criteria);
            context.put("reviewgroups",v);
        }
        catch (Exception e)
        {
            GeneralUtils.logError(e, e.getMessage(), rundata, true);
        }
    }
}
