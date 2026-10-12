/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (c) 2010,2013,2015 Stefan Kuhn
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *     "Apache Jetspeed" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" or
 *    "Apache Jetspeed", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 
package org.openscience.nmrshiftdb.modules.actions.portlets.security;

// velocity
import java.util.List;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.modules.actions.portlets.security.SecurityConstants;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.Permission;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.security.EntityExistsException;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBReviewGroupDBUser;
import org.openscience.nmrshiftdb.om.DBReviewGroupDBUserPeer;
import org.openscience.nmrshiftdb.om.DBReviewGroupPeer;


/**
 * This action sets up the template context for editing review groups in the Turbine database. 
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>Adopted by shk3
 */
public class ReviewGroupsUpdateAction extends VelocityPortletAction
{
    private static final String TEMP_PERMISSION = "tempPermission";

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
    
            /*
             * Grab the mode for the user form.
             */
            String mode = rundata.getParameters().getString(SecurityConstants.PARAM_MODE);
            
            //
            // if we are updating or deleting - put the name in the context
            //
            if (mode != null && (mode.equals(SecurityConstants.PARAM_MODE_UPDATE) || 
                                 mode.equals(SecurityConstants.PARAM_MODE_DELETE)))
            {
                // get the primary key and put the object in the context
                //String permissionname = rundata.getParameters().getString(SecurityConstants.PARAM_ENTITY_ID);
                DBReviewGroup rg=DBReviewGroupPeer.retrieveByPK(new NumberKey(rundata.getParameters().getString(SecurityConstants.PARAM_ENTITY_ID)));
                context.put("rg", rg);
            }

            //
            // if there was an error, display the message
            //
            String msgid = rundata.getParameters().getString(SecurityConstants.PARAM_MSGID);
            if (msgid != null)
            {
                int id = Integer.parseInt(msgid);
                if (id < SecurityConstants.MESSAGES.length)
                    context.put(SecurityConstants.PARAM_MSG, SecurityConstants.MESSAGES[id]);

                // get the bad entered data and put it back for convenient update
                Permission tempPermission = (Permission)rundata.getUser().getTemp(TEMP_PERMISSION);
                if (tempPermission != null)
                    context.put(SecurityConstants.CONTEXT_PERMISSION, tempPermission);
            }
            context.put("mode", mode);

        }
        catch (Exception e)
        {
            Log.error(e);
            rundata.setMessage("Error in Jetspeed User Security: " + e.toString());
            rundata.setStackTrace(StringUtils.stackTrace(e), e);
            rundata.setScreenTemplate("Error.vm");            
        }
    }

    /** 
     * Database Insert Action for Security Permissions. Performs inserts into security database.
     * 
     * @param rundata The turbine rundata context for this request.
     * @param context The velocity context for this request.
     */
    public void doInsert(RunData rundata, Context context)
        throws Exception
    {
        DBReviewGroup rg = null;
        try
        {    
            // 
            // validate that its not an 'blank' review group name -- not allowed
            //
            String name = rundata.getParameters().getString("name");        
            if (name == null || name.trim().length() == 0)
            {
                DynamicURI duri = new DynamicURI (rundata);
                duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_PERMISSION_UPDATE);
                duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_INVALID_ENTITY_NAME);
                rundata.getResponse().sendRedirect(duri.toString());
                rundata.getUser().setTemp(TEMP_PERMISSION, null);
                return;
            }

            //
            // generate a new review group
            //
            rg=new DBReviewGroup();
		rg.setReviewgroupName(name);

            //
            // add the review group
            ///
		rg.save();
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_PERMISSION_BROWSER);
            rundata.getResponse().sendRedirect(duri.toString());

        }
        catch (EntityExistsException e)
        {
            // log the error msg
            Log.error(e);

            //
            // dup key found - display error message - bring back to same screen
            //
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_PERMISSION_UPDATE);
            duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_ENTITY_ALREADY_EXISTS);
            rundata.getResponse().sendRedirect(duri.toString());

            // save values that user just entered so they don't have to re-enter
           if (rg != null)
               rundata.getUser().setTemp(TEMP_PERMISSION, rg);
        }
    }


    /** 
     * Database Delete Action for review groups.
     * 
     * @param rundata The turbine rundata context for this request.
     * @param context The velocity context for this request.
     */
    public void doDelete(RunData rundata, Context context)
        throws Exception
    {
        DBReviewGroup rg = null;;

        try
        {
            //Check if the reviewgroup is empty
            Criteria crit=new Criteria();
            crit.add(DBReviewGroupDBUserPeer.REVIEWGROUP_ID,rundata.getParameters().getString("entityid"));
            List<DBReviewGroupDBUser> v=DBReviewGroupDBUserPeer.doSelect(crit);
            if(v.size()==0){
              crit=new Criteria();
              crit.add(DBReviewGroupPeer.REVIEWGROUP_ID,rundata.getParameters().getString("entityid"));
              DBReviewGroupPeer.doDelete(crit);
            }

            //
            // success or not empty -- bring user back to review groups browser
            //
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_PERMISSION_BROWSER);
            if(v.size()!=0){
              rundata.getResponse().sendRedirect("portal/select-panel2/Review+Groups+Browser?message=There+are+still+members+in+this+group!");
            }
            rundata.getResponse().sendRedirect(duri.toString());

        }
        catch (Exception e)
        {
           // log the error msg
            Log.error(e);

            //
            // error on delete - display error message
            //
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_PERMISSION_UPDATE);
            duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_DELETE_FAILED);
            if (rg != null)
                duri.addPathInfo(SecurityConstants.PARAM_ENTITY_ID, rg.getReviewgroupName());
            duri.addQueryData(SecurityConstants.PARAM_MODE, SecurityConstants.PARAM_MODE_DELETE);
            rundata.getResponse().sendRedirect(duri.toString());

            // save values that user just entered so they don't have to re-enter
           if (rg != null)
               rundata.getUser().setTemp(TEMP_PERMISSION, rg);

        }

    }


}
