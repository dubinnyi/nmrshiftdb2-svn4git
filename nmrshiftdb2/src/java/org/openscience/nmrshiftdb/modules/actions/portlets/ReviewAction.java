/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2015 Stefan Kuhn
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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.security.turbine.TurbineAccessController;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.apache.velocity.context.Context;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.nmrshiftdb.modules.actions.portlets.result.ResultAction;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBReviewGroup;
import org.openscience.nmrshiftdb.om.DBReviewTypePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrum;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

/**
 * This is the portlet handling reviewing spectra/molecules. The portlet is based on the principle of either displaying an input form 
 * where the user can input an id for review or display a spectrum for review, where the user can hit accept/reject buttons. The display 
 * of a spectrum can either be reached by entering the id or clicking a url from a review email. The display is chosen by having a message 
 * in the context or not.
 *
 * @author     Stefan Kuhn
 * @created    26. Februar 2002
 */
public class ReviewAction extends VelocityPortletAction {

	/* (non-Javadoc)
	 * @see org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction#buildNormalContext(org.apache.jetspeed.portal.portlets.VelocityPortlet, org.apache.velocity.context.Context, org.apache.turbine.util.RunData)
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
	    try{
    		HttpServletRequest req = data.getRequest();
    		HttpSession session = req.getSession();
    		if(req.getParameter("applet") !=null && req.getParameter("applet").equals("applet")){
    			session.setAttribute("applet","applet");
    		}
    		if(req.getParameter("applet") !=null && req.getParameter("applet").equals("noapplet")){
    			data.getSession().setAttribute("applet","noapplet");
    		}
            //Sinece this method is called always, no matter if an action was done before or not, we need to 
            //distinguish if we want to display the input form or the display of a spectrum. If there is neither 
    		//a spectrum in the context nor an (error-)message at this point, we know that the portlet has been 
    		//called directly and we put an empty message into the context to display the input form without any message.
    		if(context.get("spectrum")==null && context.get("message")==null){
    		    context.put("message", "");
    		}
            //if the user is an editor, we need some more data in the context
    		else if(new TurbineAccessController().checkPermission((JetspeedUser)data.getUserFromSession(), portlet, "edit")  && context.get("spectrum")!=null) {
                context.put("types", DBReviewTypePeer.doSelect(new Criteria()));
                String sql = "select TURBINE_USER.USER_ID, count(USER_SPECTRUM.USER_ID) AS REVIEWS from (TURBINE_USER left join USER_SPECTRUM on TURBINE_USER.USER_ID = USER_SPECTRUM.USER_ID) join TURBINE_USER_GROUP_ROLE on TURBINE_USER.USER_ID=TURBINE_USER_GROUP_ROLE.USER_ID join TURBINE_ROLE on TURBINE_USER_GROUP_ROLE.ROLE_ID=TURBINE_ROLE.ROLE_ID where TURBINE_ROLE.ROLE_NAME='c' and  TURBINE_USER.LOGIN_NAME<>\"" + ((DBSpectrum)context.get("spectrum")).getUserId() + "\" group by USER_ID order by REVIEWS";
                List<Record> records = NmrshiftdbUserPeer.executeQuery(sql);
                context.put("reviewers", records);
            }
            String sql="select TURBINE_USER.USER_ID from TURBINE_USER join TURBINE_USER_GROUP_ROLE using (USER_ID ) join TURBINE_ROLE_PERMISSION using (ROLE_ID) join TURBINE_PERMISSION using (PERMISSION_ID ) where PERMISSION_NAME =\"edit\" and TURBINE_USER.LOGIN_NAME=\""+data.getUser().getUserName()+"\"";
            List<Record> records = NmrshiftdbUserPeer.executeQuery(sql);
            context.put("iseditor", records.size()>0);
    		context.put("session",session);
    		context.put("accesscontroller", new TurbineAccessController());
        }
        catch(Exception ex){
            context.put("error",GeneralUtils.logError(ex,"review/byurl",data,true));
        }
	}

	
	/**
	 * This method is called if a specrum is reviewed via a url given in a review email. No login is necessary. The url 
	 * must look like js_pane/P-Review?eventSubmit_doReviewbyurl=reviewbyurl&id=$spectrumid&reviewkey=$reviewkey&userId=$userid of reviewer.
	 * 
     * @param runData The current RunData object.
     * @param context The context to fill.
	 */
	public void doReviewbyurl(RunData runData, Context context){
		try{
		    HttpServletRequest req = runData.getRequest();
			String spectrumId = req.getParameter("id");
			String reviewKey = req.getParameter("reviewkey");
			runData.getSession().setAttribute("user", req.getParameter("userId"));
			DBSpectrum spectrum = null;
			try{
				spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(spectrumId));
			}catch(Exception ex){
				context.put("message", "Seems there is no such spectrum. Log in and try to enter the id given in the email manually, if the problem persist please contact the administrator!");
				return;
			}
			String dbReviewKey = spectrum.getReviewKey();
			if (!dbReviewKey.equals(reviewKey)) {
				context.put("message", "The keys don't mach. Log in and try to enter the id given in the email manually, if the problem persist please contact the administrator!");
				return;
			}
			runData.getSession().setAttribute("reviewbyurl", "true");
			makeReviewScreen(spectrum.getSpectrumId(), runData, context);
		}
		catch(Exception ex){
		    context.put("error",GeneralUtils.logError(ex,"review/byurl",runData,true));
		}
	}


	/**
	 * This action is called if an email is sent to the submitter.
	 * 
	 * @param runData The current RunData object.
	 * @param context The context to fill.
	 */
	public void doChangeemail(RunData runData, Context context){
        try {
            HttpServletRequest req = runData.getRequest();
            DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(new NumberKey(req.getParameter("spectrumid")));
            String email = "";
            if (runData.getSession().getAttribute("user") != null) {
                email = NmrshiftdbUserPeer.retrieveByPK(new NumberKey((String) runData.getSession().getAttribute("user"))).getEmail();
                runData.getSession().removeAttribute("user");
            } else {
                email =  TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getEmail();
            }
            MailMessage mail = new MailMessage(GeneralUtils.getSmtpServer(runData), NmrshiftdbUserPeer.retrieveByPK(spectrum.getUserId()).getEmail(),  TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getEmail(), "Reviewer questions regarding your nmrshiftdb2 submit", "The reviewer of your input from " + spectrum.getDate() + " (id:"+spectrum.getSpectrumId()+") sends you the following comment: \r\n\r\n" + req.getParameter("comment") + ".\r\n\r\nIf you have questions about this, you can contact the reviewer by email:" + email+"."+GeneralUtils.getEmailSignature(runData)+" or by replying to this message. You may also edit the spectrum via your personal page.");
            if(req.getParameter("cc")!=null && req.getParameter("cc").equals("true"))
                mail.setCc( TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId())).getEmail());
            boolean success = mail.send();
            doReview(runData, context);
            if (!success) {
                context.put("message","Problem sending email!");
            }else{
                context.put("message","The email was successfully sent!");
            }
        } catch (Exception ex) {
            context.put("error",GeneralUtils.logError(ex,"review/sendemail",runData,true));
        }
	}
	
	/**
	 * This action is called from the input form if the user enters an ID to review manually.
	 * 
	 * @param runData The current RunData object.
	 * @param context The context to fill.
     */
	public void doReview(RunData runData, Context context){
        Connection conn=null;
        HttpServletRequest req=runData.getRequest();
        try{
            String moleculeId = req.getParameter("moleculeid");
            String spectrumId = req.getParameter("spectrumid");
            String sql="select SPECTRUM.SPECTRUM_ID from MOLECULE join SPECTRUM using (MOLECULE_ID) where ";
            if(spectrumId!=null)
                sql+="SPECTRUM.SPECTRUM_ID="+spectrumId;
            else
                sql+="MOLECULE.MOLECULE_ID="+moleculeId+" and SPECTRUM.REVIEW_FLAG='false'";
            conn=Torque.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            if(!rs.next()){
                context.put("message", "Seems there is no such spectrum!");
                return;
            }
            makeReviewScreen(Integer.parseInt(rs.getString(1)), runData, context);
        }
        catch(Exception ex){
            context.put("error",GeneralUtils.logError(ex,"review/review",runData,true));
        }
        finally{
            Torque.closeConnection(conn);
        }	    
	}
	
    /**
     * This action is called if the reviewer presses the reject button.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doReject(RunData runData, Context context){
        try{
            review(runData, NmrshiftdbConstants.REJECTED, Integer.parseInt(runData.getRequest().getParameter("spectrumid")));
            context.put("message",runData.getRequest().getParameter("spectrumid") + " has been reviewed as invalid. Thanks for your participation!");
        }catch(Exception ex){
            context.put("error",GeneralUtils.logError(ex,"review/accept",runData,true));
        }
    }

    
    /**
     * This action is called if the reviewer presses the accept button.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doAccept(RunData runData, Context context){
	      try{
	          review(runData, NmrshiftdbConstants.TRUE,Integer.parseInt(runData.getParameters().get("spectrumid")));
	          context.put("message",runData.getRequest().getParameter("spectrumid") + " has been reviewed as valid. Thanks for your participation!");
	      }catch(Exception ex){
	          context.put("error",GeneralUtils.logError(ex,"review/accept",runData,true));
	      }
	}
	
	/**
	 * Puts together the review screen.
	 *
	 * @param  spectrumId  The spectrum to display
	 * @param  runData     The RunData object
	 * @return             Review screen for this spectrum
	 * @throws TranscoderException 
	 * @throws DataSetException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws OptionalDataException 
	 * @throws TorqueException 
	 * @throws NmrshiftdbException 
	 */
	public void makeReviewScreen(int spectrumId, RunData runData, Context context) throws OptionalDataException, FileNotFoundException, IOException, DataSetException, TranscoderException, TorqueException, NmrshiftdbException {
		DBSpectrum spectrum = null;
		try {
			spectrum = DBSpectrumPeer.retrieveByPK(spectrumId);
		} catch (Exception ex) {
		    context.put("message", "There is no such data!");
		    return;
		}
		if (spectrum.getReviewFlag().equals(NmrshiftdbConstants.EDITED)) {
            context.put("message", spectrumId + " has been edited. The edited spectrum has been reassigned. You do not need to do anything with respect to this review!");
            return;
		}
		if (!spectrum.getReviewFlag().equals(NmrshiftdbConstants.FALSE)) {
	         context.put("message", spectrumId + " has already been reviewed!");
	         return;
		}
		DBMolecule mol = spectrum.getDBMolecule();
		spectrum.predict=true;
		spectrum.runData=runData;
		context.put("spectrum", spectrum);
		context.put("mol", mol);
		if(runData.getSession().getAttribute("applet")!=null && ((String)runData.getSession().getAttribute("applet")).equals("applet")){ 
		    context.put("molfile", "download/NmrshiftdbServlet/"+mol.getMoleculeId()+".mol;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmdl&moleculeid="+mol.getMoleculeId()+"&coordsetid=1");
		}else{
		    //We create an image with shift values as option for the user
		    Export export = new Export(spectrum);
		    export.backColor=new Color(211,211,211);
		    for(ValuesForVelocityBean shift : spectrum.getOptions(-1, false)){
		        export.cdkmol.getAtom(shift.getValueAsInt()).setProperty(BasicAtomGenerator.IDENTIFIER, shift.getRange());
		    }
		    String moleculeimage = export.getMolSvg(700,500,false,Color.BLUE, null, null, null, null, null, null);
		    TranscoderInput transinput = new TranscoderInput(new StringReader(moleculeimage));
		    String filename = "nmrshiftdbhtml/" + System.currentTimeMillis() + "review.jpg";
		    File imagefile = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/" + filename));
		    OutputStream ostream = new FileOutputStream(imagefile);
		    TranscoderOutput output = new TranscoderOutput(ostream);
		    ImageTranscoder it = new JPEGTranscoder();
		    it.transcode(transinput, output);
		    ostream.flush();
		    ostream.close();
		    context.put("imagelink", filename);
		    if(UrlTool.doImageCache() && !spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE)){
                if(spectrum.getImage()!=null){
                    context.put("specimage","download/NmrshiftdbServlet/"+spectrum.getSpectrumId()+"."+spectrum.getImageFormat()+"?spectrumid="+spectrum.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=originalimage\" height=\"250\"");
                }else{
                    context.put("specimage","images/spectra/large/"+spectrum.getSpectrumId()+".jpeg");
                }
		        context.put("image", "images/molecules/large/"+mol.getMoleculeId()+"_"+spectrum.getSpectrumId()+".jpeg");
            }else{
		        if(spectrum.getImage()!=null){
		            context.put("specimage","download/NmrshiftdbServlet/"+spectrum.getSpectrumId()+"."+spectrum.getImageFormat()+"?spectrumid="+spectrum.getSpectrumId()+"&nmrshiftdbaction=exportspec&format=originalimage\" height=\"250\"");
		        }else{
		            context.put("specimage","download/NmrshiftdbServlet/"+spectrum.getSpectrumId()+".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportspec&spectrumid="+ spectrum.getSpectrumId() +"&format=jpeg&size=large&backcolor=12632256");
		        }
                StringBuffer sb = new StringBuffer("download/NmrshiftdbServlet/").append(mol.getMoleculeId()).append("_").append(spectrum.getSpectrumId()).append(".jpg;jsessionid="+runData.getSession().getId()+"?nmrshiftdbaction=exportmol&spectrumid=").append(spectrum.getSpectrumId()).append("&format=jpeg&size=large&backcolor=").append(ResultAction.STANDARD_BACKGROUND_COLOR+"&shownumbers=false");        
		        context.put("image", sb.toString());
		    }
		}
		context.put("data",runData);
		UrlTool urltool = new UrlTool();
		urltool.init(runData);
    	context.put("baseurl", urltool.getBaseUrl());
	}


	/**
	 * Reviews a spectrum.
	 *
	 * @param  runData        The RunData object
	 * @param  newFlag        The new review flag
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @exception  Exception  Database problems
	 */
	public void review(RunData runData, String newFlag, int spectrumId) throws FileNotFoundException, IOException, Exception {
		HttpServletRequest req = runData.getRequest();
		HttpSession session = req.getSession();
		DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(spectrumId);
		spectrum.setReviewFlag(newFlag);
		spectrum.save();
		GeneralUtils.logToSql("Reviewed as "+newFlag+" - "+ spectrum.getSpectrumId(), runData);
		Criteria crit = new Criteria();
		crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID, spectrumId);
		TurbineUser user = null;
		if (session.getAttribute("user") != null) {
			crit.add(DBUserDBSpectrumPeer.USER_ID, (String) session.getAttribute("user"));
			user = TurbineUserPeer.retrieveByPK(new NumberKey((String) session.getAttribute("user")));
			session.removeAttribute("user");
		} else {
			crit.add(DBUserDBSpectrumPeer.USER_ID,  ((BaseJetspeedUser) runData.getUser()).getUserId());
			user =  TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
		}
		crit.add(DBUserDBSpectrumPeer.REVIEW_DATE, null);
		List<DBUserDBSpectrum> v = DBUserDBSpectrumPeer.doSelect(crit);
		if (v.size() > 0) {
			DBUserDBSpectrum userSpectrum = (DBUserDBSpectrum) v.get(0);
			userSpectrum.setReviewDate(new Date());
			userSpectrum.save();
		} else {
			DBUserDBSpectrum userSpectrum = new DBUserDBSpectrum(spectrum.getSpectrumId(), user, null);
			userSpectrum.setReviewDate(new Date());
			userSpectrum.save();
		}
		DBMolecule.deleteCacheFiles(spectrum.getMoleculeId(), runData.getServletConfig(), "");
		//send email to contributor, if configured
		if (GeneralUtils.getNmrshiftdbProperty("sendreviewmail",runData).equals("true")) {
			MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(runData), spectrum.getContributor().getEmail(), GeneralUtils.getAdminEmail(runData), "Your input from " + spectrum.getDate() + " (id:"+spectrum.getSpectrumId()+") is "+(newFlag.equals("true")?"approved":"declined"), "The spectrum for "+ spectrum.getDBMolecule().getChemicalNamesAsOneString(false, false) + " you submitted at "+spectrum.getDate()+ " (full ID:"+spectrum.getSpectrumId()+") has been reviewed by <href=\"mailto:"+user.getEmail()+"\">"+user.getLoginName()+"</a> (email: "+ user.getEmail() +") as "+(newFlag.equals("true")?"valid":"invalid")+". For enquiries about this decision, please contact the reviewer. Thanks for your contribution!"+GeneralUtils.getEmailSignature(runData));
			if (email.send() == false) {
				Log.error("Sending email to contributor after review failed for spectrum " + spectrum.getSpectrumId());
			}
		}
		if (session.getAttribute("reviewbyurl") != null) {
			session.removeAttribute("reviewbyurl");
		}
		if(newFlag.equals(NmrshiftdbConstants.TRUE) && spectrum.getDBSpectrumType().getName().equals("13C")){
			String check=GeneralUtils.checkAgainstCdk(spectrum.getDBMolecule());
			if(!check.equals(""))
				GeneralUtils.sendEmailToEventReceivers("Accepted value is outside of cdk range",check,runData,0);
		}
	}

	/**
     * This action is called if the editor reassigns a review.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doAssigngroup(RunData runData, Context context){
          try{
              HttpServletRequest req=runData.getRequest();
              String spectrumId = req.getParameter("spectrumid");
              DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(Integer.parseInt(spectrumId));
              String reviewer = req.getParameter("reviewer");
              TurbineUser user=null;
              if(reviewer.charAt(0)=='r') {
                  //Assign to a reviewer
                  user = TurbineUserPeer.retrieveByPK(new NumberKey(reviewer.substring(1)));
              }else {
                  //Assign to a group
                  String sql = "select TURBINE_USER.USER_ID from TURBINE_USER, TURBINE_USER_GROUP_ROLE, TURBINE_ROLE, REVIEWTYPE_USER where TURBINE_USER.USER_ID=REVIEWTYPE_USER.USER_ID and TURBINE_USER.USER_ID=TURBINE_USER_GROUP_ROLE.USER_ID and TURBINE_USER_GROUP_ROLE.ROLE_ID=TURBINE_ROLE.ROLE_ID AND TURBINE_ROLE.ROLE_NAME='assign_reviews' and REVIEWTYPE_USER.REVIEWTYPE_ID="+reviewer.substring(1)+" and TURBINE_USER.LOGIN_NAME<>'"+spectrum.getNmrshiftdbUser().getLoginName()+"' group by USER_ID;";
                  List<Record> records = NmrshiftdbUserPeer.executeQuery(sql);
                  user=NmrshiftdbUserPeer.retrieveByPK(new NumberKey(((Record) records.get(new Random().nextInt(records.size()))).getValue(1).asString()));
              }              
              new DBUserDBSpectrum(Integer.parseInt(spectrumId), user, new Date()).save();
              SpectrumUtils.sendEmailForReview(spectrum.getSpectrumId(), spectrum.getMoleculeId(), spectrum.getNmrshiftdbUser(), user, spectrum.getDate(), spectrum.getReviewKey(), spectrum.getNmrshiftdbUser().getEmail());
              context.put("message",runData.getRequest().getParameter("spectrumid") + " has been reassigned to "+user.getFirstName()+" "+user.getLastName()+"!");
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"review/accept",runData,true));
          }
    }

    /**
     * This action is called if the reviewer returns a spectrum to the editor.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doReturntoeditor(RunData runData, Context context){
          try{
              HttpServletRequest req=runData.getRequest();
              String spectrumId = req.getParameter("spectrumid");
              DBSpectrum spectrum = DBSpectrumPeer.retrieveByPK(Integer.parseInt(spectrumId));
              String sql="select TURBINE_USER.USER_ID from TURBINE_USER join TURBINE_USER_GROUP_ROLE using (USER_ID ) join TURBINE_ROLE_PERMISSION using (ROLE_ID) join TURBINE_PERMISSION using (PERMISSION_ID ) where PERMISSION_NAME =\"edit\"";
              List<Record> records = NmrshiftdbUserPeer.executeQuery(sql);
              TurbineUser user= TurbineUserPeer.retrieveByPK(new NumberKey(records.get(0).getValue(1).asInt()));
              new DBUserDBSpectrum(spectrum.getSpectrumId(), user, new Date()).save();
              SpectrumUtils.sendEmailForReview(spectrum.getSpectrumId(), spectrum.getMoleculeId(), spectrum.getNmrshiftdbUser(), user, spectrum.getDate(), spectrum.getReviewKey(), spectrum.getNmrshiftdbUser().getEmail());
              context.put("message",runData.getRequest().getParameter("spectrumid") + " has been reassigned to the editor!");
          }catch(Exception ex){
              context.put("error",GeneralUtils.logError(ex,"review/accept",runData,true));
          }
    }
}

