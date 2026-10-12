/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2013,2015 Stefan Kuhn
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.peer.RolePeer;
import org.apache.turbine.om.security.peer.UserGroupRolePeer;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrum;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.SubmittingData;

import com.workingdogs.village.Record;

/**
 *  This is the portlet handling the reassignment of reviews
 *
 * @author     Stefan Kuhn
 * @created    15. May 2002
 */
public class CheckReviewsAction extends VelocityPortletAction
{
	long reassignmentperiod=604800000;
	String reassignmentvalueshow="1";
	
	/**
	 *  Gets the HTML to be displayed in the portlet
	 *
	 * @param  runData  Created by turbine
	 * @return          The content value
	 * @since
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data)
	{
	    try{
    		HttpServletRequest req = data.getRequest();
    		HttpSession session = req.getSession();
    		if(session.getAttribute("reassignments")!=null)
    		    session.removeAttribute("reassignments");
    	    Vector<DBSpectrum> v=new Vector<DBSpectrum>();
    	    Criteria crit=new Criteria();
    	    crit.add(DBUserDBSpectrumPeer.ASSIGNMENT_DATE,new Date(System.currentTimeMillis()-new Long(reassignmentperiod).longValue()),Criteria.LESS_THAN);
    	    crit.addJoin(DBUserDBSpectrumPeer.SPECTRUM_ID,DBSpectrumPeer.SPECTRUM_ID);
    	    crit.add(DBSpectrumPeer.REVIEW_FLAG,"false");
    	    crit.addAscendingOrderByColumn(DBUserDBSpectrumPeer.ASSIGNMENT_DATE);
    	    List<DBUserDBSpectrum> v2=DBUserDBSpectrumPeer.doSelect(crit);
    	    List<Record> l=DBSpectrumPeer.executeQuery("select SPECTRUM.SPECTRUM_ID from SPECTRUM left join USER_SPECTRUM on SPECTRUM.SPECTRUM_ID = USER_SPECTRUM.SPECTRUM_ID where USER_SPECTRUM.SPECTRUM_ID is null and SPECTRUM.REVIEW_FLAG=\"false\";");
    	    for(int k=0;k<l.size();k++){
    	        v.add(DBSpectrumPeer.retrieveByPK(new NumberKey(l.get(k).getValue(1).asInt())));
    	    }
    	    for(int i=0;i<v2.size();i++){
    	        try{
    	            List<DBUserDBSpectrum> v3=DBSpectrumPeer.retrieveByPK(v2.get(i).getSpectrumId()).getReviewAssignments();
    	            DBUserDBSpectrum assignment=v3.get(v3.size()-1);
    	            if(assignment.getAssignmentDate().getTime()<System.currentTimeMillis()-reassignmentperiod)
    	                if(!v.contains(DBSpectrumPeer.retrieveByPK(v2.get(i).getSpectrumId())))
    	                    v.add(DBSpectrumPeer.retrieveByPK(v2.get(i).getSpectrumId()));
    	        }catch(Exception ex){
    	            v.add(DBSpectrumPeer.retrieveByPK(v2.get(i).getSpectrumId()));
    	        }
    	    }
    		context.put("spectra", v);
    		context.put("show",reassignmentvalueshow);
    		session.setAttribute("reassignments",v);
	    }catch(Exception ex) {
	        context.put("error",GeneralUtils.logError(ex,"checkreviews/buildnormal",data,true));
	    }
	}

	public void doSetassignmentperiod(RunData data, Context context) {
        reassignmentperiod=new Long(data.getParameters().get("assignmentperiod")).longValue()*7*24*60*60*1000;
        reassignmentvalueshow=data.getParameters().get("assignmentperiod");
	}
	
	public void doAction(RunData data, Context context) {
	    try{
            HttpServletRequest req = data.getRequest();
            String action = req.getParameter("nmrshiftdbaction");
            HttpSession session = req.getSession();
            if (action.equals("doreassignment"))
            {
                int numberOfReassigned=0;
                Criteria crit = new Criteria();
                crit.addJoin(NmrshiftdbUserPeer.USER_ID, UserGroupRolePeer.USER_ID);
                crit.addJoin(UserGroupRolePeer.ROLE_ID, RolePeer.ROLE_ID);
                crit.add(RolePeer.NAME, "reviewer");
                List<TurbineUser> reviewers = NmrshiftdbUserPeer.doSelect(crit);
                @SuppressWarnings("unchecked")
                List<DBSpectrum> v=(Vector<DBSpectrum>)session.getAttribute("reassignments");
                for(int i=0;i<v.size();i++){
                    if(req.getParameter(((DBSpectrum)v.get(i)).getSpectrumId()+"")!=null){
                        //Reviews are distributed by random to reviewers
                        DBUserDBSpectrum reviewAssignment=null;
                        List<DBUserDBSpectrum> alreadyDoneAssignments=((DBSpectrum)v.get(i)).getReviewAssignments();
                        if(reviewers.size()==alreadyDoneAssignments.size()){
                            reviewAssignment = (DBUserDBSpectrum)(alreadyDoneAssignments.get(alreadyDoneAssignments.size()-1));
                        }
                        else{
                            TurbineUser reviewer=null;
                            int l=0;
                            while(l<10){
                                reviewer = GeneralUtils.selectReviewer(((DBSpectrum)v.get(i)).getNmrshiftdbUser());
                                boolean oldAssignment=false;
                                for(int k=0;k<alreadyDoneAssignments.size();k++){
                                    if(((DBUserDBSpectrum)alreadyDoneAssignments.get(k)).getUserId()==reviewer.getUserId())
                                        oldAssignment=true;
                                }
                                if(!oldAssignment)
                                    break;
                                l++;
                            }
                            //Save the review assignment
                              crit=new Criteria();
                              crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID,((DBSpectrum)v.get(i)).getSpectrumId());
                              crit.add(DBUserDBSpectrumPeer.USER_ID,reviewer.getUserId());
                              List<DBUserDBSpectrum> v2=DBUserDBSpectrumPeer.doSelect(crit);
                              if(v2.size()>0)
                                reviewAssignment = (DBUserDBSpectrum)v2.get(0);
                              else
                                reviewAssignment = new DBUserDBSpectrum(((DBSpectrum)v.get(i)).getSpectrumId(), reviewer, new Date());
                                            reviewAssignment.save();
                              try {
                                String dir = GeneralUtils.getNmrshiftdbProperty("serializeddir",data);
                                SubmittingData sd = ((DBSpectrum)v.get(i)).getMeAsSubmittingData(true,ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml"),false, "");
                                File file = new File(dir + File.separatorChar + ((DBSpectrum)v.get(i)).getSpectrumId() + ".xml");
                                FileWriter out = new FileWriter(file);
                                out.write(sd.data.toXML());
                                out.close();
                              } catch (FileNotFoundException ex) {
                                Log.debug("No serialized file found for " + ((DBSpectrum)v.get(i)).getSpectrumId());
                              } catch (Exception ex) {
                                GeneralUtils.logError(ex,"Problems when reading serilized file " + ((DBSpectrum)v.get(i)).getSpectrumId(),data,false);
                              }
                        }
                        if (GeneralUtils.getNmrshiftdbProperty("sendreviewmail",data).equals("true"))
                        {
                            StringBuffer url = req.getRequestURL();
                            url.append("/js_pane/P-Review?eventSubmit_doReviewbyurl=reviewbyurl&id=" + ((DBSpectrum)v.get(i)).getSpectrumId() + "&reviewkey=" + ((DBSpectrum)v.get(i)).getReviewKey() + "&userId="+reviewAssignment.getNmrshiftdbUser().getUserId());
                            MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(data), reviewAssignment.getNmrshiftdbUser().getEmail(), GeneralUtils.getAdminEmail(data), "Review this input!", "Some input to nmrshiftdb2 has been assigned to you for review, the id is " + ((DBSpectrum)v.get(i)).getSpectrumId() + ", the URL " + url+". For contacting the submitter please quote "+((DBSpectrum)v.get(i)).getSpectrumId()+"."+GeneralUtils.getEmailSignature(data));
                            if (email.send() == false)
                            {
                                Log.error("Sending email to reviewer failed for spectrum " + ((DBSpectrum)v.get(i)).getSpectrumId());
                            }
                        }
                        numberOfReassigned++;
                    }
                }
                context.put("message",numberOfReassigned+" were reassigned !");
            }
            if(action.equals("sendreminder")){
                int numberOfReminded=0;
                int numberOfFailed=0;
                @SuppressWarnings("unchecked")
                Vector<DBSpectrum> v=(Vector<DBSpectrum>)session.getAttribute("reassignments");
                for(int i=0;i<v.size();i++){
                    if(req.getParameter(((DBSpectrum)v.get(i)).getSpectrumId()+"")!=null){
                        List<DBUserDBSpectrum> alreadyDoneAssignments=((DBSpectrum)v.get(i)).getReviewAssignments();
                        if(alreadyDoneAssignments.size()>0){
                          DBUserDBSpectrum reviewAssignment=(DBUserDBSpectrum)alreadyDoneAssignments.get(alreadyDoneAssignments.size()-1);
                          StringBuffer url = req.getRequestURL();
                          url.append("/js_pane/P-Review?eventSubmit_doReviewbyurl=reviewbyurl&id=" + reviewAssignment.getDBSpectrum().getSpectrumId() + "&reviewkey=" + reviewAssignment.getDBSpectrum().getReviewKey() + "&userId="+reviewAssignment.getNmrshiftdbUser().getUserId());
                          MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(data), reviewAssignment.getNmrshiftdbUser().getEmail(), GeneralUtils.getAdminEmail(data), "Review reminder!", "This is to remind you that a spectrum has been assigned to you for review and has not yet been reviewed, the id is " + reviewAssignment.getDBSpectrum().getSpectrumId() + ", the URL " + url+". For contacting the submitter please quote "+((DBSpectrum)v.get(i)).getSpectrumId()+"."+GeneralUtils.getEmailSignature(data));
                          if (email.send() == false)
                          {
                            Log.error("Sending email to reviewer failed for spectrum " + ((DBSpectrum)v.get(i)).getSpectrumId());
                            numberOfFailed++;
                          }else{
                            numberOfReminded++;
                          }
                        }
                    }
                }
                context.put("message",numberOfReminded+" were reminded, "+numberOfFailed+" failed !");
            }
	    }catch(Exception ex){
	        context.put("error",GeneralUtils.logError(ex,"checkreviews/doaction",data,true));
        }
    }
}
