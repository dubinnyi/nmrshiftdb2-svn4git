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

// java util
import java.util.List;
import java.util.Vector;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.modules.actions.portlets.security.SecurityConstants;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.security.DataBackendException;
import org.apache.turbine.util.security.UnknownEntityException;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBReviewGroupDBUser;
import org.openscience.nmrshiftdb.om.DBReviewGroupDBUserPeer;
import org.openscience.nmrshiftdb.om.DBReviewGroupPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;

/**
 * This action sets up the template context for editing review groups roles in the Turbine database
 * for a given user.
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>Adopted by shk3
 */
public class UserReviewgroupUpdateAction extends VelocityPortletAction
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
            DBReviewGroup rg = null;
            /*
             * Grab the mode for the user form.
             */
            String mode = rundata.getParameters().getString(SecurityConstants.PARAM_MODE);

            //
            // check to see if we are adding a role for a single user
            // 
            String entityid = rundata.getParameters().getString(SecurityConstants.PARAM_ENTITY_ID);
            if (entityid == null || entityid.trim().length() == 0)
            {
                return;
            }

            buildUserRoleContext(portlet, context, rundata, entityid);

            //
            // if there was an error, display the message
            //
            String msgid = rundata.getParameters().getString(SecurityConstants.PARAM_MSGID);
            if (msgid != null)
            {
                int id = Integer.parseInt(msgid);
                if (id < SecurityConstants.MESSAGES.length)
                    context.put(SecurityConstants.PARAM_MSG, SecurityConstants.MESSAGES[id]);
            }

        }
        catch (Exception e)
        {
            Log.error(e);
            rundata.setMessage("Error in Jetspeed User Role Security: " + e.toString());
            rundata.setStackTrace(StringUtils.stackTrace(e), e);
            rundata.setScreenTemplate("Error.vm");            
        }
    }


    /** 
     * Database Update Action for Security Roles. Performs updates into security database.
     * 
     * @param rundata The turbine rundata context for this request.
     * @param context The velocity context for this request.
     */
    public void doUpdate(RunData rundata, Context context)
        throws Exception
    {
        // TODO: if the user is the current user, we will need to refresh the 
        // rundata's ACL list as described in the constructor for AccessControlList.java               

        String entityid = rundata.getParameters().getString(SecurityConstants.PARAM_ENTITY_ID);
        if (entityid == null || entityid.trim().length() == 0)
        {
            Log.error("UserRoleBrowser: Failed to get entity: " + entityid );
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_USERROLE_UPDATE);
            duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_MISSING_PARAMETER);
            rundata.getResponse().sendRedirect(duri.toString());
            return;
        }

        User user = JetspeedSecurity.getUser(entityid);
        if (null == user)
        {
            Log.error("UserRoleBrowser: Failed to get user: " + entityid );
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_USERROLE_UPDATE);
            duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_MISSING_PARAMETER);
            rundata.getResponse().sendRedirect(duri.toString());
            return;
        }
        

        try
        {
            List v = (List)rundata.getUser().getTemp("reviewgroups");
            List selected = (List)rundata.getUser().getTemp(SecurityConstants.CONTEXT_SELECTED);

            if (v == null || selected == null)
            {
                DynamicURI duri = new DynamicURI (rundata);
                duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_USERROLE_UPDATE);
                duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_MISSING_PARAMETER);
                rundata.getResponse().sendRedirect(duri.toString());
                return;
            }

            //
            // walk thru all the roles, see if anything changed
            // if changed, update the database
            //
            for (int ix = 0; ix < v.size(); ix++)
            {
                boolean newValue = rundata.getParameters().getBoolean("box_" + ((DBReviewGroup)v.get(ix)).getReviewgroupName(), false);
                boolean oldValue = ((Boolean)selected.get(ix + 1)).booleanValue();
                if (newValue != oldValue)
                {
                    if (newValue == true)
                    {
                        // grant a role to a user
                        //JetspeedSecurity.grant( user, 
                        //                        JetspeedSecurity.getGroup(JetspeedSecurity.JETSPEED_GROUP),
                        //                        roles[ix] );
			Criteria crit=new Criteria();
			crit.add(DBReviewGroupDBUserPeer.USER_ID, ((BaseJetspeedUser)user).getUserId());
			crit.add(DBReviewGroupDBUserPeer.REVIEWGROUP_ID,((DBReviewGroup)v.get(ix)).getReviewgroupId());
			List<DBReviewGroupDBUser> v2=DBReviewGroupDBUserPeer.doSelect(crit);
			if(v2.size()==0)
				DBReviewGroupDBUserPeer.doInsert(crit);
                    }
                    else
                    {
                        // revoke a role from a user
                        /*JetspeedSecurity.revoke( user, 
                                                JetspeedSecurity.getGroup(JetspeedSecurity.JETSPEED_GROUP),
                                                roles[ix] );*/
			Criteria crit=new Criteria();
			crit.add(DBReviewGroupDBUserPeer.USER_ID, ((BaseJetspeedUser)user).getUserId());
			crit.add(DBReviewGroupDBUserPeer.REVIEWGROUP_ID,((DBReviewGroup)v.get(ix)).getReviewgroupId());
			DBReviewGroupDBUserPeer.doDelete(crit);
                    }
                }
            }

            // clear the temp values
            rundata.getUser().setTemp("reviewgroups", null);
            rundata.getUser().setTemp(SecurityConstants.CONTEXT_SELECTED, null);
 
            //
            // success -- bring user back to user browser
            //
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_USER_BROWSER);
            rundata.getResponse().sendRedirect(duri.toString());

        }
        catch (Exception e)
        {
           // log the error msg
            Log.error("Failed update role+reviewgroup: " + e);

            //
            // error on update - display error message
            //
            DynamicURI duri = new DynamicURI (rundata);
            duri.addPathInfo(JetspeedResources.PATH_PANEL_KEY, SecurityConstants.PANEID_USERROLE_UPDATE);
            duri.addPathInfo(SecurityConstants.PARAM_MSGID, SecurityConstants.MID_UPDATE_FAILED);
            if (user != null)
                duri.addPathInfo(SecurityConstants.PARAM_ENTITY_ID, user.getUserName());
            rundata.getResponse().sendRedirect(duri.toString());

        }
    }

    /** 
     * Build the context for a role browser for a specific user.
     * 
     * @param portlet The velocity-based portlet that is being built.
     * @param context The velocity context for this request.
     * @param rundata The turbine rundata context for this request.
     * @param userid The userid of the user that we are building a role context for.
     */
    private void buildUserRoleContext(VelocityPortlet portlet, 
                                       Context context,
                                       RunData rundata,
                                       String userid)
        throws UnknownEntityException, DataBackendException, Exception
    {
        // get master list of roles         
        Criteria criteria = new Criteria();
	List<DBReviewGroup> v=DBReviewGroupPeer.doSelect(criteria);
	criteria.addJoin(DBReviewGroupPeer.REVIEWGROUP_ID,DBReviewGroupDBUserPeer.REVIEWGROUP_ID);
	criteria.addJoin(DBReviewGroupDBUserPeer.USER_ID,NmrshiftdbUserPeer.USER_ID);
	criteria.add(NmrshiftdbUserPeer.LOGIN_NAME,userid);
	List<DBReviewGroup> usergroups=DBReviewGroupPeer.doSelect(criteria);
        //RoleSet master = JetspeedSecurity.getRoles(criteria);
        // get the user object
        User user = JetspeedSecurity.getUser(userid);
        if (null == user)
        {
            // no ACL found
            Log.error("UserRoleBrowser: Failed to get user: " + userid );
            return;
        }
        // get the access control list for the given user
        /*AccessControlList acl = JetspeedSecurity.getACL(user);
        if (null == acl)
        {
            // no ACL found
            Log.error("RoleBrowser: NO ACL found for user: " + user.getUserName() );
            return;
        }*/
        // get all the roles for this user in the Jetspeed Group
        /*Group jetGroup = JetspeedSecurity.getGroup(JetspeedSecurity.JETSPEED_GROUP);
        RoleSet userRoles = acl.getRoles( jetGroup );

        Role[] roles = master.getRolesArray();*/        
        Vector<Boolean> selected = new Vector<Boolean>(v.size()+1);           

        selected.add(0, new Boolean(false));
        boolean sel = false;
        for ( int ix = 0; ix < v.size(); ix++ )
        {
            if (null != usergroups)
                sel = usergroups.contains((DBReviewGroup)v.get(ix));
            else
                sel = false;
            selected.add(ix + 1, new Boolean(sel));
        } 
        
        rundata.getUser().setTemp("reviewgroups", v);
        rundata.getUser().setTemp(SecurityConstants.CONTEXT_SELECTED, selected);
        context.put(SecurityConstants.CONTEXT_USER, user);
        context.put(SecurityConstants.CONTEXT_ROLES, v);
        context.put(SecurityConstants.CONTEXT_SELECTED, selected);
    }


}
