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
package org.openscience.nmrshiftdb.daemons;

//jetspeed stuff
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletConfig;

import org.apache.commons.codec.binary.Base64;
import org.apache.jetspeed.daemon.Daemon;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.graph.invariant.Canon;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBQuickCheck;
import org.openscience.nmrshiftdb.om.DBQuickCheckPeer;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSignalDBAtom;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBCondition;
import org.openscience.nmrshiftdb.om.DBSpectrumFile;
import org.openscience.nmrshiftdb.om.DBSpectrumHyperlink;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumMark;
import org.openscience.nmrshiftdb.om.DBWishlist;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

import nu.xom.Builder;
import nu.xom.Element;


/**
A daemon that saves submits which have been serialized to disk
It also handles csearch results if configured


@author Stefan Kuhn
*/
public class SaveSubmitsDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon) throws Exception{
      Log.info("saving submits");
      this.setResult( Daemon.RESULT_PROCESSING );
      try{
    	File serializedDir=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/xml"));
		File[] filesindir=serializedDir.listFiles();
		for(int i=0;i<filesindir.length;i++){
			if(filesindir[i].getName().substring(filesindir[i].getName().length()-4).equals(".xml")){
			  try{
				CMLCml cmlcml = (CMLCml)new CMLBuilder().build(new FileInputStream(filesindir[i])).getRootElement();
				SubmittingData sd= new SubmittingData(cmlcml);
				sd.save(servcon);
				try {
					//We save a report
					if(sd.getChoosenSpectrumType().getName().equals("13C") || sd.getChoosenSpectrumType().getName().equals("1H")){
	                    Document reportpdf = new Document();
	                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                    PdfWriter.getInstance(reportpdf, baos);
	                    reportpdf.open();
	                    long[] morgan=Canon.symmetry(sd.getMolOriginal(), GraphUtil.toAdjList(sd.getMolOriginal()));
	    				double[] mark=SpectrumUtils.calculateScore(sd.getMolWithH(), sd.getMolOriginal(), sd, null, sd.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") ? GeneralUtils.getPredictionLimits("1H") : GeneralUtils.getPredictionLimits("13C"), "", reportpdf, servcon, morgan, GeneralUtils.getNmrshiftdbProperty("defaulturl"), null, null, null, null, null, null, new StringBuffer(),null);
	    				reportpdf.close();
	                    DBSpectrumFile specfile = new DBSpectrumFile();
	                    specfile.setSpectrumId(sd.getSpectrumid());
	                    specfile.setType(DBSpectrum.NMRSHIFTDB_TYPE);
	                    specfile.setFormat("nmrshiftdb2");
	                    byte[] base64 = new Base64().encode(baos.toByteArray());
	                    specfile.setData(base64);
	                    specfile.save();
	                    String marktext=SpectrumUtils.makeJudgement((int)mark[0], mark[5],true);
	                    DBUserDBSpectrumMark specrummark=new DBUserDBSpectrumMark();
	                    specrummark.setUserId(NmrshiftdbUserPeer.getByName("admin").getUserId());
	                    specrummark.setSpectrumId(sd.getSpectrumid());
	                    specrummark.setMarkDate(new Date());
	                    specrummark.setComment(marktext);
	                    specrummark.setMark((int)mark[0]);
	                    specrummark.save();
					}
				}catch(Exception ex) {
					GeneralUtils.logError(ex,"Saving submits", null, true);
				}
				if(sd.getEditSpectrum()<=0){
					MailMessage email;
					if(sd.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE))
					    email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(servcon), TurbineUserPeer.retrieveByPK(sd.getThisUserId()).getEmail(), GeneralUtils.getAdminEmail(servcon), "Your nmrshiftdb2 submit is saved!", "Thanks for you submit. You can view it view it by using the 'Show all my contributions' on the search page or by any search which includes it.");
					else
					    email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(servcon), TurbineUserPeer.retrieveByPK(sd.getThisUserId()).getEmail(), GeneralUtils.getAdminEmail(servcon), "Your nmrshiftdb2 submit is saved!", "Thanks for you submit. You can view it on your personal page.");
		            if (email.send() == false) {
		              Log.error("Sending email to submitter failed for spectrum " + sd.getSpectrumid());
		            }
				}
				filesindir[i].delete();
				DBMolecule.deleteCacheFiles(sd.getMoleculeId(), servcon,"");
				if(new Date().getTime()-sd.getThisDate().getTime()>1000*60*60*24)
				{
		        	//TODO Signature should be in here, but needs request
					GeneralUtils.sendEmailToEventReceivers("Problem saving submit!", "Submit "+filesindir[i].getName()+" could not be saved for more than a day. Please check!", null, 1);
				}
		        if(!sd.isNoreview()){
		        	SpectrumUtils.doReview(sd, new Date(), TurbineUserPeer.retrieveByPK(sd.getThisUserId()));
		        }
		        //send csearch referee email, if configured
		        //csearch email is send for public 13C data in any case and for private 13C data where selected
		        DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(sd.getSpectrumid());
		        /*if ((!spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) || (spectrum.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE) && sd.isSendCsearch())) && sd.getChoosenSpectrumType().getName().equals("13C") && GeneralUtils.getNmrshiftdbProperty("csearchemail", (RunData)null)!=null) {
		            SpectrumUtils.sendRobotRefereeRequest(sd, "spectrum "+sd.getSpectrumid());
		        }*/
		        if(sd.accept!=null){
	        		//TODO this was ... but needs a request
	        		//new ReviewPortlet().review(runData, NmrshiftdbConstants.TRUE, subData.getSpectrumid());
	        		spectrum.setReviewFlag(NmrshiftdbConstants.TRUE);
	        		spectrum.save();
	        	}
		        //Remove from wishlist, if the molecule is in wishlist
		        List<DBWishlist> v=DBMoleculePeer.retrieveByPK(sd.getMoleculeId()).getDBWishlists();
		        for(int k=0;k<v.size();k++){
		          DBWishlist wl=(DBWishlist)v.get(k);
		          if(wl.getDBSpectrumType().equals(sd.getChoosenSpectrumType())){
		            if(!wl.getTurbineUser().getLoginName().equals("admin")){
		              MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(servcon), wl.getTurbineUser().getEmail(), GeneralUtils.getAdminEmail(servcon), "Requested spectrum has been submitted!", "You added a "+wl.getDBSpectrumType().getName()+ " to the nmrshiftdb2 wishlist. Such a spectrum has now been submitted!");
		              if (email.send() == false) {
			        	//TODO Signature should be in here, but needs request
		                Log.error("Sending email to wishlist user failed!");
		              }
		            }
		            wl.delete();
		          }
		        }
			  }catch(Exception ex){
			      if(new Date().getTime()-filesindir[i].lastModified()>1000*60*60*24)
			          GeneralUtils.sendEmailToEventReceivers("Problem saving submit!", "Submit "+filesindir[i].getName()+" could not be saved for more than a day. Please check!", null, 1);
				  GeneralUtils.logError(ex,"Saving submits", null, true);
			  }
		    }
		}
        if (GeneralUtils.getNmrshiftdbProperty("csearchemail", servcon)!=null) {
        	String host = GeneralUtils.getNmrshiftdbProperty("csearchreceivehost", servcon);
        	String username = GeneralUtils.getNmrshiftdbProperty("csearchreceiveuser", servcon);
        	String password = GeneralUtils.getNmrshiftdbProperty("csearchreceivepassword", servcon);
    		// Create empty properties
    		Properties props = new Properties();
    		// Get session
    		Session session = Session.getDefaultInstance(props, null);
    		// Get the store
    		Store store = session.getStore("pop3s");
    		store.connect(host, username, password);
    		// Get folder
    		Folder folder = store.getFolder("INBOX");
    		folder.open(Folder.READ_WRITE);
    		// Get directory
    		Message message[] = folder.getMessages();
    		for (int i=0, n=message.length; i<n; i++) {
    			if(message[i].getSubject().equals("CSEARCH-Robot-Referee")){
    				try{
    				    MimeMultipart multipart = (MimeMultipart)message[i].getContent();
	    				BodyPart body = multipart.getBodyPart(0);
	    				String content = (String)body.getContent();
	    				if(content.contains("predictions/reports has been exceeded")){
	    					//messages about exceeded limits get deleted
	    					message[i].setFlag(Flags.Flag.DELETED, true);
	    				}else if(analyzeMail(content, GeneralUtils.getNmrshiftdbProperty("csearchuser", servcon),GeneralUtils.getNmrshiftdbProperty("csearchpwd", servcon),servcon)){
	    					message[i].setFlag(Flags.Flag.DELETED, true);
	    				}
    				}catch(Exception ex){
    					//we skip problems with individual emails
    					ex.printStackTrace();
    			        Log.error("Reading csearc email "+ex.getMessage());
    				}
    			}
    		}
    		// Close connection 
    		folder.close(true);
    		store.close();
        }
      } catch (Exception ex) {
		ex.printStackTrace();
        Log.error("Save submit "+ex.getMessage());
      }
      this.setResult( Daemon.RESULT_SUCCESS );
    }

	/**
	 * Reads an csearch url from an email text and extracts the data from the website, then saves the data as a new spectrum.
	 * 
	 * @param mail The text of the email.
	 * @param csearchuser User for csearch connection.
	 * @param csearchpwd Password for csearch connection.
	 * @throws IOException
	 * @throws TorqueException
	 * @throws Exception
	 */
	private boolean analyzeMail(String mail, String csearchuser, String csearchpwd, ServletConfig servcon) throws IOException, TorqueException, Exception{
	    Connection dbconn = null;
	    Statement stmt = null;
	    try {
	        dbconn = Torque.getConnection();
			stmt = dbconn.createStatement();
			stmt.executeUpdate("BEGIN");
			int start = mail.indexOf("http://nmrpredict.orc.univie.ac.at/");
			int end = mail.indexOf("Complete evaluation - show summary");
			String link = mail.substring(start, end).trim();
			String resultpage = fetchURL(link, csearchuser, csearchpwd);
			Pattern pattern = Pattern.compile("Compound: (spectrum|qc)_[0-9]*");
			Matcher matcher = pattern.matcher(resultpage);
			// Check all occurance
			int spectrumid = 0;
			boolean isQuickCheck=false;
            DBSpectrum spectrumold = null;
            DBMolecule molecule = null;
			while (matcher.find()) {
				String line = matcher.group();
				if(line.contains("spectrum")){
				    spectrumid = Integer.parseInt(line.substring(19));
		            spectrumold = DBSpectrumPeer.retrieveByPK(spectrumid);
		            molecule = spectrumold.getDBMolecule();

				}else{
				    isQuickCheck=true;
				    spectrumid = Integer.parseInt(line.substring(13));
				}
			}
			resultpage = resultpage
					.substring(resultpage
							.indexOf("Preferred Value<br>from both Predictions</b>"));
			pattern = Pattern
					.compile("[0-9]*(</b>)*</td>[ ]*<td style=\"background-color: rgb\\([, 0-9]*\\);\">(<b>)*[. 0-9]*(</b>)*</td>[ ]*<td style=\"background-color: rgb\\([ ,0-9]*\\);\">(<b>)*[ .0-9]*(</b>)*</td>[ ]*<td style=\"background-color: rgb\\([ ,0-9]*\\);\">(<b>)*[ .0-9]*");
			matcher = pattern.matcher(resultpage);
			Criteria crit=new Criteria();
			crit.add(DBSpectrumPeer.SPECTRUM_ID,spectrumid);
			if(!isQuickCheck && DBSpectrumPeer.doSelect(crit).size()==0)
				return false;
			if(spectrumid<Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("myidstart", servcon)) || spectrumid>Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("myidend", servcon)))
				return false;
			String csearchquality="Accept";
			int csearchmark=10;
			if(resultpage.contains("The CSEARCH Robot Referee recommends: Minor revision")){
				csearchquality="Minor revision";
				csearchmark=8;
			}else if(resultpage.contains("The CSEARCH Robot Referee recommends: Major revision")){
				csearchquality="Major revision";
				csearchmark=3;
			}else if(resultpage.contains("The CSEARCH Robot Referee recommends: Reject")){
				csearchquality="Reject";
				csearchmark=1;
			}
			String color="<span style=\"background-color:lime\">";
			if(csearchquality.equals("Minor revision")){
				color="<span style=\"background-color:yellow\">";
			}else if(csearchquality.equals("Major revision")){
				color="<span style=\"background-color:orange\">";
			}else if(csearchquality.equals("Reject")){
				color="<span style=\"background-color:red\">";
			}
			if(!isQuickCheck){
    			DBUserDBSpectrumMark mark=new DBUserDBSpectrumMark();
    			mark.setUserId(NmrshiftdbUserPeer.getByName("admin").getUserId());
    			mark.setSpectrumId(spectrumold.getSpectrumId());
    			mark.setMarkDate(new Date());
    			mark.setComment(color+"<a href=\"http://nmrpredict.orc.univie.ac.at/c13robot/robot.php\">CSEARCH quality check: "+csearchquality+"</a></span>");
    			mark.setMark(csearchmark);
    			mark.save(dbconn);
			}
			//We save the report url
			//summary is the url with .htm instead of .html
			String linksummary = link.substring(0,link.length()-1);
			if(isQuickCheck || new UrlTool().isLabserver() || spectrumold.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE)){
				//We save the report privately
				String summarypage = fetchURL(linksummary, csearchuser, csearchpwd);
				StringBuffer savestring=new StringBuffer();
				Pattern pattern2 = Pattern.compile("[a-zA-Z0-9_]*.png");
				Matcher matcher2 = pattern2.matcher(summarypage);
				String summarypageChanged = new String(summarypage);
				// Save the images
                //these are absolute relative urls in the file pointing to static images, we make them absolute
                summarypageChanged = GeneralUtils.replace(summarypageChanged, "reject.png", "http://nmrpredict.orc.univie.ac.at/wr/reject.png");
                summarypageChanged = GeneralUtils.replace(summarypageChanged, "major.png", "http://nmrpredict.orc.univie.ac.at/wr/major.png");
                summarypageChanged = GeneralUtils.replace(summarypageChanged, "minor.png", "http://nmrpredict.orc.univie.ac.at/wr/minor.png");
                summarypageChanged = GeneralUtils.replace(summarypageChanged, "accept.png", "http://nmrpredict.orc.univie.ac.at/wr/accept.png");
                summarypageChanged = GeneralUtils.replace(summarypageChanged, "fingerprint-456483_640.jpg", "http://nmrpredict.orc.univie.ac.at/wr/fingerprint-456483_640.jpg");
                int i=0;
				while (matcher2.find()) {
				    String img = summarypage.substring(matcher2.start(),matcher2.end());
					URL imgurl=new URL(linksummary.substring(0,linksummary.lastIndexOf('/'))+"/"+img);
	                //special handling for the summaries1
	                if(summarypage.substring(summarypage.indexOf(img)-12,summarypage.indexOf(img)-5).equals("summary")){
	                    summarypageChanged = GeneralUtils.replace(summarypageChanged, summarypage.substring(summarypage.indexOf(img)-47,summarypage.indexOf(img))+img,"");
	                    //these are non-report specific urls in the file, we keep them as URLs
	                }else if(!(img.startsWith("csearch_logo.png") || img.startsWith("database_upload.png") || img.startsWith("reject.png") || img.startsWith("major.png") || img.startsWith("minor.png") || img.startsWith("accept.png") || img.startsWith("thumb1.png") || img.startsWith("thumb2.png") || img.startsWith("fingerprint-456483_640.jpg") || img.startsWith("wilhelmbusch.png"))){
					    //if it's an image specific to the report we save the imageto the db
    					InputStream imgstream=imgurl.openStream();
    					ByteArrayOutputStream baos = new ByteArrayOutputStream();
        				int read=0;
        				while((read=imgstream.read())!=-1){
        					baos.write(read);
        				}
        				imgstream.close();
    					String base64 = new Base64().encodeToString(baos.toByteArray());
    					summarypageChanged = GeneralUtils.replace(summarypageChanged, img, "download/NmrshiftdbServlet/"+spectrumid+"image"+i+".png?nmrshiftdbaction=exportcsearchimage&spectrumid="+(isQuickCheck ? "qc" : "")+spectrumid+"&image="+i);
    					savestring.append("<image"+i+">");
    					savestring.append(base64);
    					savestring.append("</image"+i+">");
    					i++;
					}
				}
				if(isQuickCheck){
	                savestring.append("<page><![CDATA[");
	                savestring.append(summarypageChanged);
	                savestring.append("]]></page>");
				    DBQuickCheck quickcheck=DBQuickCheckPeer.retrieveByPK(spectrumid);
		            Builder builder = new Builder();
		            nu.xom.Document quickcheckXML = builder.build(new ByteArrayInputStream(quickcheck.getCml().getBytes()));
		            Builder parser = new Builder();
		            nu.xom.Document doc = parser.build("<csearchreport>"+savestring+"</csearchreport>", null);
		            if(quickcheckXML.getChild(0).getChildCount()==5)
		                quickcheckXML.getChild(0).getChild(4).getParent().replaceChild(quickcheckXML.getChild(0).getChild(4), doc.getChild(0).copy());
		            else
		                ((Element)quickcheckXML.getChild(0)).appendChild(doc.getChild(0).copy());
		            quickcheck.setCml(quickcheckXML.toXML());
		            quickcheck.save(dbconn);
				}else{
	                savestring.append("<page>");
	                savestring.append(summarypageChanged);
	                savestring.append("</page>");
    		        DBSpectrumFile specfile = new DBSpectrumFile();
    		        specfile.setSpectrumId(spectrumid);
    		        specfile.setType(DBSpectrum.CSEARCH_TYPE);
    		        specfile.setFormat("csearchfull");
    		        specfile.setData(savestring.toString().getBytes());
    		        specfile.save(dbconn);
				}
			}else{
				//In case of public entries, we just link to csearch
                DBSpectrumFile specfile = new DBSpectrumFile();
                specfile.setSpectrumId(spectrumid);
                specfile.setType(DBSpectrum.CSEARCH_TYPE);
                specfile.setFormat("csearchurl");
                specfile.setData(GeneralUtils.replace(linksummary,"/wr/","/nmrshiftdb/").getBytes());
                specfile.save(dbconn);
			}
			if(molecule != null && !molecule.hasCSearchSpectrum()){
				DBSpectrum spectrum = new DBSpectrum();
				spectrum.setSpectrumTypeId(1);
				spectrum
						.setComment(new String("Prediction via CSEARCH").getBytes());
				spectrum.setDate(new Date());
				if(spectrumold.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE))
					spectrum.setReviewFlag(NmrshiftdbConstants.PRIVATE);
				else
					spectrum.setReviewFlag(NmrshiftdbConstants.TRUE);
				spectrum.setUserId(1);
				spectrum.setCommentSoundex(Soundex.soundex(new String(spectrum
						.getComment()), "9", new StringAndInt()));
				spectrum.setMoleculeId(molecule.getMoleculeId());
				spectrum.setSpecfile("");
				spectrum.setSimpleSpecfile("");
				spectrum.setReviewKey("");
				spectrum.setPublished(NmrshiftdbConstants.FALSE);
				spectrum.save(dbconn);
				DBCondition csearchcondition = (DBCondition) DBConditionPeer
						.getByValue("CSEARCH").get(0);
				DBSpectrumDBCondition spectrumcondition = new DBSpectrumDBCondition();
				spectrumcondition.setDBCondition(csearchcondition);
				spectrumcondition.setSpectrumId(spectrum.getSpectrumId());
				spectrumcondition.save(dbconn);
				DBSpectrumHyperlink dblink = new DBSpectrumHyperlink();
				dblink.setDBSpectrum(spectrum);
				dblink.setHyperlink("http://nmrpredict.orc.univie.ac.at/c13robot/robot.php");
				dblink.setDescription("CSEARCH");
				dblink.setDescriptionSoundex(Soundex.soundex(dblink.getDescription(), "9", new StringAndInt()));
				dblink.save(dbconn);
				StringBuffer specFile = new StringBuffer();
				StringBuffer simpleSpecfile = new StringBuffer();
				while (matcher.find()) {
					String line = matcher.group();
					Pattern pattern2 = Pattern.compile("[.0-9]*$");
					Matcher matcher2 = pattern2.matcher(line);
					matcher2.find();
					if(!matcher2.group().equals("")){
						float shiftvalue = Float.parseFloat(matcher2.group());
						pattern2 = Pattern.compile("^[0-9]*");
						matcher2 = pattern2.matcher(line);
						matcher2.find();
						int atom = Integer.parseInt(matcher2.group());
						//csearch reports do not count heteroatoms, so we need to find the carbon with number atom
						int ccount=0;
						for(int i=0;i<molecule.getAtomsAsArrayInMdlOrder().length;i++){
						    if(molecule.getAtomAt(i).getSymbol().equals("C")){
						        ccount++;
						        if(ccount==atom){
						            atom=i;
						            break;
						        }
						    }
						}
						DBSignal signal = new DBSignal();
						signal.setSpectrumId(spectrum.getSpectrumId());
						signal.setIntensity(0);
						signal.setMultiplicity(GeneralUtils.getMultiplicity(molecule
								.getAtomAt(atom).getHydrogenCount(),false));
						signal.save(dbconn);
						DBShift shift = new DBShift();
						shift.setAxis(1);
						shift.setValue(shiftvalue);
						shift.setSignalId(signal.getSignalId());
						shift.save(dbconn);
						DBSignalDBAtom signalatom = new DBSignalDBAtom();
						signalatom.setSignalId(signal.getSignalId());
						signalatom.setAtomId(molecule.getAtomAt(atom).getAtomId());
						signalatom.setAxis(1);
						signalatom.save(dbconn);
						String multiplicity = GeneralUtils.getMultiplicity(molecule
								.getAtomAt(atom).getHydrogenCount(), false);
						simpleSpecfile.append(shift.getValue()).append(";").append(
								signal.getIntensity()).append(multiplicity).append("|");
						specFile.append(shift.getValue()).append(";").append(
								signal.getIntensity()).append(multiplicity).append(";")
								.append(atom).append("|");
					}
				}
				long fingerprint = SpectrumUtils.makeFingerprint(spectrum);
				DBSpectrumPeer.executeStatement("update SPECTRUM set FINGERPRINT="
						+ fingerprint + ", SPECFILE='" + specFile.toString()
						+ "', SIMPLE_SPECFILE='" + simpleSpecfile.toString()
						+ "' where SPECTRUM_ID=" + spectrum.getSpectrumId(), dbconn);
			}
			stmt.executeUpdate("COMMIT");
			return true;
		} catch (Exception ex) {
			stmt.executeUpdate("ROLLBACK");
			throw ex;
		}
	    finally{
	        Torque.closeConnection(dbconn);
	    }
	}
	
	/**
	 * Fetches the text of a url, using http authentication.
	 * 
	 * @param urlString The url to fetch.
	 * @param user The user to use.
	 * @param pass The password to use.
	 * @return The text of the URL.
	 * @throws IOException
	 */
	private String fetchURL (String urlString,String user,String pass) throws IOException {
		Authenticator.setDefault(new MyAuthenticator(user,pass));
		URL url = new URL(urlString);
	    // Read all the text returned by the server
	    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	    String str;
	    StringBuffer sb = new StringBuffer();
	    while ((str = in.readLine()) != null) {
	        sb.append(str);
	    }
	    in.close();
	    return sb.toString();
	}
}

class MyAuthenticator extends Authenticator {
	
	String user;
	String pass;
	public MyAuthenticator(String user, String pass){
		this.user=user;
		this.pass=pass;
	}
    // This method is called when a password-protected URL is accessed
    protected PasswordAuthentication getPasswordAuthentication() {
        // Return the information
        return new PasswordAuthentication(user, pass.toCharArray());
    }
}
