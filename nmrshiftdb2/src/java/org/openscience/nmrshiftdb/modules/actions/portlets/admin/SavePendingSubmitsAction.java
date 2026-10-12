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
package org.openscience.nmrshiftdb.modules.actions.portlets.admin;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;

/**
 *  This is the portlet handling the restore of failed submits
 *
 * @author     Stefan Kuhn
 * @created    15. May 2002
 */
public class SavePendingSubmitsAction extends VelocityPortletAction
{
    /**
     * Default behavior for the portlet action.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data)
    {
        HttpServletRequest req = data.getRequest();
        HttpSession session = req.getSession();
        if(session.getAttribute("backups")!=null)
            session.removeAttribute("backups");
        File serializedDir=new File(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/xml"));
        File[] filesindir=serializedDir.listFiles();
        Vector<File> v=new Vector<File>();
        for(int i=0;i<filesindir.length;i++){
            if(filesindir[i].getName().substring(filesindir[i].getName().length()-4).equals(".xml"))
                v.add(filesindir[i]);
        }
        context.put("backupfiles",v);
        session.setAttribute("backups",v);
    }


    /**
     * Executes saving of the submits.
     * 
     * @param data
     * @param context
     */
    public void doSavesubmits(RunData data, Context context) {
        try{
            StringBuffer message = new StringBuffer();
            HttpServletRequest req = data.getRequest();
            HttpSession session = req.getSession();
            int numberOfRestoredFiles=0;
            @SuppressWarnings("unchecked")
            Vector<File> v=(Vector<File>)session.getAttribute("backups");
            for(int i=0;i<v.size();i++){
                if(req.getParameter(((File)v.get(i)).getName())!=null){
                    try{
                        CMLCml cmlcml = (CMLCml)new CMLBuilder().build(new FileInputStream((File)v.get(i))).getRootElement();
                        SubmittingData sd= new SubmittingData(cmlcml);
                        StringTokenizer st=new StringTokenizer(((File)v.get(i)).toString(),"_");
                        st.nextToken();
                        String x=st.nextToken();
                        st=new StringTokenizer(x,".");
                        TurbineUser user=TurbineUserPeer.retrieveByPK(new NumberKey(st.nextToken()));
                        Date now = new Date();
                        String reviewFlag=NmrshiftdbConstants.FALSE;
                        if(sd.getEditSpectrum()!=0)
                            reviewFlag=NmrshiftdbConstants.TRUE;//edits do not need a review
                        sd.save(user.getUserId(), reviewFlag, req.getRequestURL().toString(), now, data.getServletConfig(), Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres",data)), false);
                        ((File)v.get(i)).delete();
                        numberOfRestoredFiles++;
                        SpectrumUtils.doReview(sd, now, user);
                    }
                    catch(Exception ex)
                    {
                        GeneralUtils.logError(ex,"BackupPortlet1", data,true);
                        message.append("restore of "+((File)v.get(i)).getName()+" failed because of "+ex.getMessage()+".<br>");
                    }
                }
            }
            message.append("<br>"+numberOfRestoredFiles+" were restored !");
            context.put("message",message);
        }
        catch(Exception ex){
            context.put("error",GeneralUtils.logError(ex,"orderaction/dochangedates",data,true));
        }
    }
}
