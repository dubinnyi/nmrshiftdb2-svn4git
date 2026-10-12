/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012,2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.util;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.log4j.Category;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.cdk.Bond;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.graph.invariant.Canon;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.PredictionTool;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrum;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrumPeer;
import org.openscience.nmrshiftdb.util.comparator.DBSignalComparator;
import org.openscience.nmrshiftdb.util.comparator.IdentifierComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLPeak;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.workingdogs.village.DataSetException;

import de.unikoeln.chemie.nmr.data.Peak2D;

/**
 * Contains some utils for spectra
 *
 * @author     shk3
 * @created    23. May 2002
 */
public class SpectrumUtils {
	static long MAX_VALUE=(long)Math.pow(2,63);


	/**
	 * Makes a spectrum fingerprint
	 *
	 * @param  signals        The signals
	 * @param  fromSpec       true=shifts come from datbase; false=they do not.
	 * @return                The fingerprint
	 * @exception  TorqueException  Database problems
	 */
	public static long makeFingerprint(DBSignal[] signals, boolean fromSpec) throws TorqueException {
		BigInteger bi = new BigInteger("0");
		if (isSignalWithShiftArray(signals, Integer.MIN_VALUE, -1, fromSpec))
			bi = bi.add(BigInteger.valueOf(1));
		for (int i = 0; i < 62; i++) {
			int base = i * 4;
			int end = base + 8;
			if (isSignalWithShiftArray(signals, base, end, fromSpec))
				bi = bi.add(BigInteger.valueOf((long) Math.pow(2, i + 1)));
		}
		if (isSignalWithShiftArray(signals, 253, Integer.MAX_VALUE, fromSpec))
			bi = bi.add(BigInteger.valueOf((long) Math.pow(2, 63)));
		if (bi.longValue() >= MAX_VALUE) {
			long prov = bi.longValue();
			prov = prov - MAX_VALUE - MAX_VALUE;
			bi = BigInteger.valueOf(prov);
		}
		return bi.longValue();
	}


	/**
	 *Makes a fingerprint of a spectrum
	 *
	 * @param  spec           The spectrum
	 * @return                The fingerprint
	 * @throws TorqueException Database problems
	 */
	public static long makeFingerprint(DBSpectrum spec) throws TorqueException {
		List<DBSignal> signals = spec.getDBSignals();
		Collections.sort(signals, new DBSignalComparator());
		DBSignal[] signalsarray=new DBSignal[signals.size()];
		for(int i=0;i<signals.size();i++)
			signalsarray[i]=(DBSignal)signals.get(i);
		return(makeFingerprint(signalsarray,true));    
	}


	/**
	 *Determines if there is a signal in a certain range
	 *
	 * @param  v              The signals
	 * @param  base           Lower limit
	 * @param  end            Upper limit
	 * @param  fromSpec       true=shifts come from datbase; false=they do not.
	 * @return                false=no signal in that range, true=one or more signals in that range
	 * @exception  TorqueException  Description of Exception
	 */
	private static boolean isSignalWithShiftArray(DBSignal[] v, int base, int end, boolean fromSpec) throws TorqueException {
		for (int i = 0; i < v.length; i++) {
			if (!fromSpec && v[i].getDBShiftsArray()[0].getValue() >= base && v[i].getDBShiftsArray()[0].getValue() <= end) {
				return (true);
			}
			if (fromSpec && ((DBShift) (v[i].getDBShifts().get(0))).getValue() >= base && ((DBShift) (v[i].getDBShifts().get(0))).getValue() <= end) {
				return true;
			}
		}
		return (false);
	}
	
	public static void scaleIntensities(CMLElements<CMLPeak> peaks){
        double maxpeak=0;
        double minpeak=0;
        for (int i = 0; i < peaks.size(); i++) {
          if (peaks.get(i).getYValue() > maxpeak){
        	  maxpeak=peaks.get(i).getYValue();
          }if(peaks.get(i).getYValue() < minpeak) {
        	  minpeak=peaks.get(i).getYValue();
          }
        }
        if(minpeak<0){
        	for (int i = 0; i < peaks.size(); i++) {
        		peaks.get(i).setYValue(peaks.get(i).getYValue()-minpeak);//minpeak is negative, so - gives +
        	}
        }
        maxpeak=maxpeak-minpeak;//minpeak is negative, so - gives +
        if(maxpeak>1){
        	for (int i = 0; i < peaks.size(); i++) {
        		peaks.get(i).setYValue(peaks.get(i).getYValue()/maxpeak);
        	}
        }
	}
	/**
	 * Calculates the similarity of two spectra. This method is a second step, see 
	 * places where this method is used for usage examples.
	 * 
	 * @param map 					The map of matching shifts, as built by the getMatchingSearchShift method.
	 * @param similarityPerSignal 	Must bbe100 divided by the number of shifts in the spectrum with less shifts.
	 * @param greaterSpectrum 		The spectrum with the greater number of shifts.
	 * @param unused 				This contains the shifts not used in similarity calculation after the method ran.
	 * @return 						The similarity value (0-100).
	 */
	public static float calculateSimilarity(Map<Double, Double> map, float similarityPerSignal, List<CMLPeak> greaterSpectrum, List<Double> unused) {
		float similarity=0;
	    Set<Double> set = map.keySet();
	    Iterator<Double> iterator = set.iterator();
	    while (iterator.hasNext()) {
	      Double element = iterator.next();
	      float difference = java.lang.Math.abs(element.floatValue() - map.get(element).floatValue());
	      if (difference < similarityPerSignal) {
	        similarity += similarityPerSignal - difference;
	      }
	    }
	    //This is an optimisation
	    boolean change = true;
	    try{
	    	int h=0;
	        while (change) {
	          change = false;
	          for (int i = 0; i < unused.size(); i++) {
	            double unusedd = unused.get(i);
	            double currentdiff = 0;
	            Double element2 = null;
	            for (int k = 0; k < greaterSpectrum.size(); k++) {
	              element2 = new Double(greaterSpectrum.get(k).getXValue());
	              if (map.get(element2) != null && (map.get(element2)).floatValue() == unusedd) {
	            	  currentdiff = Math.abs(element2 - unusedd);
	                break;
	              }
	            }
	            int ingreater = -1;
	            for (int k = 0; k < greaterSpectrum.size(); k++) {
	              if (Math.abs(greaterSpectrum.get(k).getXValue() - unusedd) < currentdiff) {
	                currentdiff = Math.abs(greaterSpectrum.get(k).getXValue() - unusedd);
	                ingreater = k;
	              }
	            }
	            if (ingreater != -1) {
	              Double element = new Double(greaterSpectrum.get(ingreater).getXValue());
	              double diffinmap = Math.abs(element - map.get(element));
	              if (currentdiff < diffinmap) {
	                unused.remove(unused.get(i));
	                unused.add(map.get(element));
	                map.put(element2, map.get(element));
	                map.put(element, unusedd);
	                change = true;
	              }
	            }
	          }
	          //This prevents endless loops
	          h++;
	          if(h>1000)
	        	  break;
	        }
	    }catch(Exception ex){
	        //if something goes wrong in here, we can still calculate a (slightly off) similarity value
	    }
	    return similarity;
	}


      /**
       * Does everything connected with the review process.
       *
       * @param  subData        The current SubmittingData object.
       * @param  now            The Date considered to be input time.
       * @param  user           The user doing the submit.
       * @return                True=a reviewer did an edit; false=somebody else edited or it was a new submit.
       * @throws TorqueException 
       * @throws IOException 
       * @throws FileNotFoundException 
       * @throws DataSetException 
       */
      public static boolean doReview(SubmittingData subData, Date now, TurbineUser user) throws TorqueException, FileNotFoundException, IOException, DataSetException {
        TurbineUser reviewer = null;
        boolean showMessageWithReviewer = true;
        DBSpectrum oldSpectrum = null;
        if (subData.getEditSpectrum() != 0) {
          //If the submit is an edit, some special cases apply
          Criteria crit = new Criteria();
          crit.add(DBSpectrumPeer.SPECTRUM_ID, subData.getEditSpectrum());
          List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
          oldSpectrum = (DBSpectrum) v.get(0);
          //If a spectrum is edited by the contributor, the original reviewer gets it again. If the reviewer edits, no review necessary
          if (!user.isReviewer()) {
            //if it's an edit and the submitter is not a reviewer, the original reviewer gets it again
            crit = new Criteria();
            crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID, oldSpectrum.getSpectrumId());
            crit.addAscendingOrderByColumn(DBUserDBSpectrumPeer.REVIEW_DATE);
            List<DBUserDBSpectrum> v2 = DBUserDBSpectrumPeer.doSelect(crit);
            if (v2.size() > 0) {
              reviewer = ((DBUserDBSpectrum) v2.get(v2.size() - 1)).getNmrshiftdbUser();
            } else {
              //this is if for some reason the spectrum was edited, but we do not have the information who reviewed in first instance
              reviewer = GeneralUtils.selectReviewer(user);
            }
          } else {
            //it's an edit and the edit was done by a reviewer->no review necessary 
            showMessageWithReviewer = false;
          }
        } else {
          //If the spectrum is new, ie. not an edit, we just get a reviewer
          reviewer = GeneralUtils.selectReviewer(user);
        }
        if (reviewer != null) {
          //Save the review assignment
          new DBUserDBSpectrum(subData.getSpectrumid(), reviewer, now).save();
        }
        SpectrumUtils.doEmailForReviewAndSerialize(subData, reviewer, user);
        return (showMessageWithReviewer);
      }


      /**
       *Sends an email to the reviewer and serializes the data
       *
       * @param  reviewer                   The reviewer the spectrum is assigned to
       * @param  subData                    Description of Parameter
       * @param  user                       The user doint the submit.
       * @exception  IOException            Problems writing serialized file
       * @exception  FileNotFoundException  Nmrshfiftdb.properties not found
       * @throws TorqueException 
       * @throws TooManyRowsException 
       * @throws NoRowsException 
       */
      public static void doEmailForReviewAndSerialize(SubmittingData subData, TurbineUser reviewer, TurbineUser user) throws IOException, FileNotFoundException, NoRowsException, TooManyRowsException, TorqueException{
        //send email to reviewer, if configured
    	DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(subData.getSpectrumid());
        if (GeneralUtils.getNmrshiftdbProperty("sendreviewmail", (RunData)null).equals("true") && reviewer != null) {
            sendEmailForReview(subData.getSpectrumid(), subData.getMoleculeId(), user, reviewer, spectrum.getDate(), spectrum.getReviewKey(), TurbineUserPeer.retrieveByPK(Integer.parseInt(subData.getSendEmailUserId())).getEmail());
        }
        //write serialized data to dir, if configured
        if (GeneralUtils.getNmrshiftdbProperty("writeserializedtodir", (RunData)null).equals("true")) {
          FileWriter out = new FileWriter(GeneralUtils.getNmrshiftdbProperty("serializeddir", (RunData)null) + File.separatorChar + spectrum.getSpectrumId() + ".xml");
          out.write(subData.data.toXML());
          out.close();
        }
        //send serialized data by email, if configured
        if (GeneralUtils.getNmrshiftdbProperty("sendserializedemail", (RunData)null).equals("true")) {
          MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer((RunData)null), GeneralUtils.getNmrshiftdbProperty("serializedemail", (RunData)null), GeneralUtils.getAdminEmail((RunData)null), "Serialized input from " + spectrum.getSpectrumId(), subData.data.toXML() + GeneralUtils.getEmailSignature((RunData)null));
          if (email.send() == false) {
            Log.error("Sending email with serialized input from " + spectrum.getSpectrumId() + " failed for spectrum " + spectrum.getSpectrumId());
          }
        }
      }
      
      
    /**
     * Sends review emails (submitter and reviewer) for a spectrum.
     * 
     * @param spectrumid The id of the spectrum to reviews.
     * @param moleculeid The id of the molecule of the spectrum.
     * @param user The user who submitted the spectrum.
     * @param reviewer The reviewer.
     * @param date The submission date.
     * @param reviewkey The desired review key.
     * @param useremail The email of the submitter.
     * @throws NoRowsException
     * @throws TooManyRowsException
     * @throws TorqueException
     * @throws IOException
     */
    public static void sendEmailForReview(int spectrumid, int moleculeid, TurbineUser user, TurbineUser reviewer, Date date, String reviewkey, String useremail) throws NoRowsException, TooManyRowsException, TorqueException, IOException {
        StringBuffer url = new StringBuffer();
        url.append(GeneralUtils.getNmrshiftdbProperty("defaulturl"));
        String url2=url+"/js_pane/P-Results?nmrshiftdbaction=showDetailsFromHome&unreviewed=true&userid="+user.getLoginName()+"&molNumber="+moleculeid;
        url.append("/js_pane/P-Review?eventSubmit_doReviewbyurl=reviewbyurl&id=" + spectrumid + "&reviewkey=" + reviewkey + "&userId=" + ((NumberKey) reviewer.getPrimaryKey()).getBigDecimal().intValue());
        MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer((RunData)null), reviewer.getEmail(), GeneralUtils.getAdminEmail((RunData)null), "Review this input!", "Some input to nmrshiftdb2 has been assigned to you for review, the id is " + spectrumid + ", the URL " + url + ". For contacting the submitter please quote " + spectrumid + "." + GeneralUtils.getEmailSignature((RunData)null));
        if (email.send() == false) {
          Log.error("Sending email to reviewer failed for spectrum " + spectrumid);
        }
        email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer((RunData)null), useremail, GeneralUtils.getAdminEmail((RunData)null), "Your input from " + date + " (id:"+spectrumid+") is assigned for review", "The spectrum for " + DBMoleculePeer.retrieveByPK(moleculeid).getChemicalNamesAsOneString(false,false) + " you submitted at " + date + " has been assigned to for review to <href=\"mailto:" + reviewer.getEmail() + "\">" + reviewer.getLoginName() + "</a> (email: " + reviewer.getEmail() + "). For enquiries about the status of the review process, please contact the reviewer. You will be notified automatically if the spectrum is accepted or rejected!\n\rThe full ID is "+spectrumid+"\n\rIf you want to have a look at your submitted data, use this link "+ url2 +"" + GeneralUtils.getEmailSignature((RunData)null));
        if (email.send() == false) {
          Log.error("Sending email to contributor failed for spectrum " + spectrumid);
        }
    }


    /**
     * Calculates a score for a spectrum, using predictions.
     * 
     * @param molWithH         The molecule the spectrum is for.
     * @param subData          The submittingData containing the spectrum.
     * @param predictions      The predictions (an atom number/ValuesForVelocityBean map).
     * @param limits           limits[0]==the upper limit, limits[1]==the lower limit.
     * @param unusedString     A string showing shifts not used in the assignment.
     * @param documentc        The report which is filled.
     * @param servcon          The current ServletConfiguration.
     * @param morgan           The symmetry indicators of the molecule (e. g. morgan numbers). 
     * @param redAtoms 
     * @param orangeAtoms 
     * @param greenAtoms 
     * @param fullreport 
     * @return
     * @throws NmrshiftdbException 
     * @throws TranscoderException 
     * @throws IOException 
     * @throws CDKException 
     * @throws TorqueException 
     * @throws Exception 
     */
      public static double[] calculateScore(IAtomContainer molWithH, IAtomContainer molOriginal, SubmittingData subData, Map<Integer, ValuesForVelocityBean> predictions, double[] limits, String unusedString, Document documentc, ServletConfig servcon, long[] morgan, String baseUrl, PdfPTable alltable, PdfPTable alltable2, List<Integer> greenAtoms, List<Integer> orangeAtoms, List<Integer> redAtoms, List<Integer> grayAtoms, StringBuffer resultstring, List<Element> fullreport) throws TorqueException, CDKException, IOException, TranscoderException, NmrshiftdbException, Exception{
          int[] marks=new int[8];
          double sumDiffC=0;
          //We also build a report here
          Image image1 = null; 
   		  if(servcon==null) {
   	          String filename=System.currentTimeMillis() + "qc1.jpg";
   	          GeneralUtils.makeJpg(subData.getInputMolAsMdl(null,null,false),filename,servcon,300,250,true, subData.getChoosenSpectrumType(), subData.getIdentifiers(), Color.WHITE);
   	          image1=Image.getInstance(filename);
   		  }else {
   	          String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "qc1.jpg";
   	          GeneralUtils.makeJpg(subData.getInputMolAsMdl(null,null,false),"/"+filename,servcon,300,250,true, subData.getChoosenSpectrumType(), subData.getIdentifiers(), Color.WHITE);
   	          image1=Image.getInstance(ServletUtils.expandRelative(servcon, filename));
   		  }
          DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
  		  format.applyPattern("#####0.00");
          BaseFont bs = null;
          if(servcon==null)
        	  bs = BaseFont.createFont("src/conf/jetspeed/WEB-INF/conf/fonts/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
          else
        	  bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
          Font fontSym = new Font(bs,12);
          Chunk c = new Chunk("nmrshiftdb2 Quality Report - ");
          c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          Chunk c2=new Chunk(Integer.toString(subData.getChoosenSpectrumType().getDBIsotope(1).getAtomicMass()));
          c2.setTextRise(7);
          c2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          c2.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
          Chunk c3=new Chunk(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol());
          c3.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          Paragraph p = new Paragraph();
          p.add(c);
          p.add(c2);
          p.add(c3);
          p.add(new Paragraph(" "));
          documentc.add(p);
          documentc.add(Chunk.NEWLINE);
          documentc.add(image1);
          p = new Paragraph(" ");
          p.add(new Paragraph(" "));
          documentc.add(p);
          PdfPTable tablepdfc = new PdfPTable(6);
          tablepdfc.setWidthPercentage(100);
          tablepdfc.setWidths(new int[]{1,1,2,1,1,1});
          Chunk chunk=new Chunk("Atom");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell1 = new PdfPCell(new Paragraph(chunk));
          cell1.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT);
          cell1.setRowspan(2);
          cell1.setBackgroundColor(Color.LIGHT_GRAY);
          cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("\u03B4 [ppm]",fontSym);
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell2 = new PdfPCell(new Paragraph(chunk));
          cell2.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT | PdfPCell.LEFT);
          cell2.setRowspan(2);
          cell2.setBackgroundColor(Color.LIGHT_GRAY);
          cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("Deviation from\u00a0prediction");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell3 = new PdfPCell(new Paragraph(chunk));
          cell3.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT | PdfPCell.LEFT);
          cell3.setRowspan(2);
          cell3.setBackgroundColor(Color.LIGHT_GRAY);
          cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("Prediction");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell4 = new PdfPCell(new Paragraph(chunk));
          cell4.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
          cell4.setColspan(2);
          cell4.setBackgroundColor(Color.LIGHT_GRAY);
          cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("HOSE");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell5 = new PdfPCell(new Paragraph(chunk));
          cell5.setBorder(PdfPCell.LEFT);
          cell5.setBackgroundColor(Color.LIGHT_GRAY);
          cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("No. Spheres");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell6 = new PdfPCell(new Paragraph(chunk));
          cell6.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT | PdfPCell.LEFT);
          cell6.setBackgroundColor(Color.LIGHT_GRAY);
          cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("No. shift values");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell7 = new PdfPCell(new Paragraph(chunk));
          cell7.setBorder(PdfPCell.BOTTOM | PdfPCell.RIGHT | PdfPCell.LEFT);
          cell7.setBackgroundColor(Color.LIGHT_GRAY);
          cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
          chunk=new Chunk("Code");
          chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          PdfPCell cell8 = new PdfPCell(new Paragraph(chunk));
          cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
          cell8.setBackgroundColor(Color.LIGHT_GRAY);
          cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
          tablepdfc.addCell(cell1);
          tablepdfc.addCell(cell2);
          tablepdfc.addCell(cell3);
          tablepdfc.addCell(cell4);
          tablepdfc.addCell(cell5);
          tablepdfc.addCell(cell6);
          tablepdfc.addCell(cell7);
          tablepdfc.addCell(cell8);
          Map<String,List<PdfPCell>> rows=new TreeMap<String,List<PdfPCell>>(new IdentifierComparator());
          Map<String,List<PdfPCell>> rowsfull=new TreeMap<String,List<PdfPCell>>(new IdentifierComparator());
          Map<String,List<PdfPCell>> rowsfull2=new TreeMap<String,List<PdfPCell>>(new IdentifierComparator());
          List<Long> alreadyUsedForScore=new ArrayList<Long>();
          List<Long> alreadyUsedForScoreSecond=new ArrayList<Long>();
          boolean[] footnotesused=new boolean[3];
          for (int i = 0; i < molWithH.getAtomCount(); i++) {
              if ((!subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && molWithH.getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol())) || (molWithH.getAtom(i).getSymbol().equals("C") && subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H"))) {
                  int hcount=0;
                  boolean firsthfound=false;
                  int hnumber=0;
                  int hnumber2=0;
                  if(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")){
                      for(int k=0;k<molWithH.getConnectedAtomsCount(molWithH.getAtom(i));k++){
                          if(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k).getSymbol().equals("H")){
                              hcount++;
                              if(firsthfound){
                                  hnumber2=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k));
                              }else{
                                  hnumber=molWithH.getAtomNumber(molWithH.getConnectedAtomsList(molWithH.getAtom(i)).get(k));
                                  firsthfound=true;
                              }
                          }
                      }
                  }
                  if(!(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && hcount==0)){
                	  String identifier=Integer.toString(i+1);
                      if(subData.getChoosenSpectrumType()!=null && (molWithH.getAtom(i).getSymbol().equals(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol()) || (subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && AtomContainerManipulator.countHydrogens(molWithH, molWithH.getAtom(i))>0))){
                          if(subData.getIdentifiers().get(i)!=null && !subData.getIdentifiers().get(i).equals(""))
                              identifier = subData.getIdentifiers().get(i);
                      }
                      int atomnumber=i;
                      if(subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")){
                          atomnumber=hnumber;
                      }
                      if(subData.getShift(atomnumber).equals("")){
                          ReportUtil.addRedRow(marks, rows, identifier, alltable, rowsfull, rowsfull2, bs);
                      }else{
                          if(!subData.getShift(atomnumber).equals("")){
                              double assignedshift = Double.parseDouble(subData.getShift(atomnumber));
                        	  if(!subData.getChoosenSpectrumType().getDBIsotope(1).getElementSymbol().equals("H")) {
                        		  //a non-h spectrum
                        		  sumDiffC=ReportUtil.addToReport(molWithH, atomnumber, i, subData, predictions, alreadyUsedForScore, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift, assignedshift, false, bs);
                        	  }else {
                        		  //a h spectrum
	                              if(hcount==2) {
	                            	  //with 2hs
                                	  String identifier2=identifier+"a";
                                	  if(atomnumber<molOriginal.getAtomCount()) {
                                		  String identifier1=Integer.toString(atomnumber+1);
                                          if(subData.getIdentifiers().get(atomnumber)!=null && !subData.getIdentifiers().get(atomnumber).equals("")) {
                                                  identifier1 = subData.getIdentifiers().get(atomnumber);
                                          }
                                          identifier2=identifier2+" ("+identifier1+")";
                                	  }
	                            	  String identifier3=identifier+"b";
	                            	  if(hnumber2<molOriginal.getAtomCount()) {
	                            		  String identifier1=Integer.toString(hnumber2+1);
	                                      if(subData.getIdentifiers().get(hnumber2)!=null && !subData.getIdentifiers().get(hnumber2).equals("")) {
	                                    	  identifier1 = subData.getIdentifiers().get(hnumber2);
	                                      }
		                            	  identifier3=identifier3+" ("+identifier1+")";
	                            	  }
		                              //we find out if the two Hs are stereoconfigured
		                              String hose1=new ExtendedHOSECodeGenerator().getHOSECode(molWithH, molWithH.getAtom(atomnumber), 6);
		                              String hose2=new ExtendedHOSECodeGenerator().getHOSECode(molWithH, molWithH.getAtom(hnumber2), 6);
		                              if(subData.getShift(hnumber2).equals("")){
		                            	  sumDiffC=ReportUtil.addToReport(molWithH, atomnumber, i, subData, predictions, alreadyUsedForScore, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier2, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift, assignedshift, false, bs);
		                            	  //second shift is missing
		                                  ReportUtil.addRedRow(marks, rows, identifier3, alltable, rowsfull, rowsfull2, bs);		                            	  
		                              }else {
		                            	  double assignedshift2 = Double.parseDouble(subData.getShift(hnumber2));
			                              if(hose1.equals(hose2)) {
			                            	  //if they are, we use same prediction
			                            	  double assignedshiftav = (assignedshift2+assignedshift)/2;
			                            	  assignedshiftav = assignedshiftav*100;
			                            	  assignedshiftav = (double)((int) assignedshiftav);
			                            	  assignedshiftav = assignedshiftav /100;                           	  
			                            	  sumDiffC=ReportUtil.addToReport(molWithH, atomnumber, i, subData, predictions, alreadyUsedForScore, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier3, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift, assignedshiftav, true, bs);
			                            	  sumDiffC=ReportUtil.addToReport(molWithH, hnumber2, i, subData, predictions, alreadyUsedForScoreSecond, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier2, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift2, assignedshiftav, true, bs);
			                              }else {
			                                  //If we have stereoconfigs we do two predictions TODO
			                            	  sumDiffC=ReportUtil.addToReport(molWithH, atomnumber, i, subData, predictions, alreadyUsedForScore, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier3, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift, assignedshift, false, bs);
			                            	  sumDiffC=ReportUtil.addToReport(molWithH, hnumber2, i, subData, predictions, alreadyUsedForScoreSecond, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier2, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift2, assignedshift2, false, bs);
			                              }
		                              }
	                              }else {
	                            	  //with 1 h
	                            	  ReportUtil.addToReport(molWithH, atomnumber, i, subData, predictions, alreadyUsedForScore, morgan, marks, sumDiffC, format, footnotesused, limits, redAtoms, greenAtoms, orangeAtoms, grayAtoms, servcon, identifier, baseUrl, rows, alltable, alltable2, rowsfull, rowsfull2, assignedshift, assignedshift, false, bs);
	                              }
                        	  }
                          }else {
                        	 ReportUtil.addRedRow(marks, rows, identifier, alltable, rowsfull, rowsfull2, bs);	
                          }
                      }
                  }
              }
          }
          int k=0;
          Iterator<Entry<String, List<PdfPCell>>> it = rows.entrySet().iterator();
          while (it.hasNext()) {
              Entry<String, List<PdfPCell>> pair = it.next();
              for(int i=0;i<6;i++){
        		  if(pair.getValue().get(i)!=null){
	        		  if(k%2!=0){
	        			  pair.getValue().get(i).setBackgroundColor(Color.LIGHT_GRAY);
	        		  }
	        		  pair.getValue().get(i).setHorizontalAlignment(Element.ALIGN_CENTER);
	    			  pair.getValue().get(i).setVerticalAlignment(Element.ALIGN_MIDDLE);
	        		  tablepdfc.addCell(pair.getValue().get(i));
        		  }
              }
        	  k++;
          }
          k=0;
          it = rowsfull.entrySet().iterator();
          while (it.hasNext()) {
              Entry<String, List<PdfPCell>> pair = it.next();
			  for(int i=0;i<4;i++){
	    		  if(k%2!=0){
	    			  pair.getValue().get(i).setBackgroundColor(Color.LIGHT_GRAY);
	    		  }
	    		  pair.getValue().get(i).setHorizontalAlignment(Element.ALIGN_CENTER);
	    		  pair.getValue().get(i).setVerticalAlignment(Element.ALIGN_MIDDLE);
	    		  alltable.addCell(pair.getValue().get(i));
	    	  }
        	  k++;
          }
          k=0;
          it = rowsfull2.entrySet().iterator();
          while (it.hasNext()) {
              Entry<String, List<PdfPCell>> pair = it.next();
			  for(int i=0;i<3;i++){
        		  if(k%2!=0){
        			  pair.getValue().get(i).setBackgroundColor(Color.LIGHT_GRAY);
        		  }
        		  pair.getValue().get(i).setHorizontalAlignment(Element.ALIGN_CENTER);
        		  pair.getValue().get(i).setVerticalAlignment(Element.ALIGN_MIDDLE);
        		  alltable2.addCell(pair.getValue().get(i));
    		  }
        	  k++;
          }
          double[] result=new double[6];
          int overallMarkC=(int)(10.5-(marks[1]*.5+(sumDiffC/marks[0])*.5));
          if(overallMarkC<1)
              overallMarkC=1;
          result[0]=overallMarkC;
          result[1]=sumDiffC/marks[0];
          result[2]=marks[0];
          result[3]=marks[2];
          result[4]=marks[3];
          tablepdfc.setHorizontalAlignment(Element.ALIGN_LEFT);
          documentc.add(tablepdfc);
          if(!unusedString.equals(""))
              documentc.add(new Paragraph("The following shifts were not used: "+unusedString));
          if(marks[7]!=0)
        	  result[5]=1d-(marks[4]+marks[5]+marks[6])/(double)marks[7];
          else
        	  result[5]=1;
          String reliability=reliability(result[5]);
          documentc.add(Chunk.NEWLINE);
          resultstring.append("Overall mark "+overallMarkC+" (out of 1 to 10, 10 being best)\n");
          resultstring.append("Mean deviation from prediction: "+format.format(result[1])+" ppm \u2192 "+format.format(result[1]/2)+" Points\n");
          resultstring.append("No. of red or missing shifts: "+result[3]+" \u2192 "+(result[3]*5)+" Points\n");
          resultstring.append("No. of yellow shifts: "+result[4]+" \u2192 "+(result[4]*2.5)+" Points\n");
          documentc.add(new Paragraph(resultstring.toString(), fontSym));
          Font font = new Font();
          if((int)result[0]==1)
              font.setColor(Color.RED);
          else if((int)result[0]<=5)
              font.setColor(Color.ORANGE);
          else
              font.setColor(Color.GREEN);          
          documentc.add(new Paragraph("Result: "+overallMarkC+"/"+SpectrumUtils.getJudgement(overallMarkC),font));
          
          documentc.add(Chunk.NEWLINE);
          Paragraph p2=new Paragraph();
          if(marks[5]>0)
        	  p2.add(new Chunk(marks[5]+" yellow shift(s) "));
       	  if(marks[5]>0 && marks[4]>0)
        	  p2.add(new Chunk("and "));
          if(marks[4]>0)
        	  p2.add(new Chunk(marks[4]+" red shift(s) "));
          if(marks[5]>0 || marks[4]>0)
        	  p2.add(new Chunk("not taken into account because the underlying prediction was unreliable."));
          documentc.add(p2);
          p2=new Paragraph();
          p2.add(new Chunk("Reliability: "+reliability));
          documentc.add(Chunk.NEWLINE);
          c = new Chunk("Created "+new Date());
          p = new Paragraph(c);
          p.add(new Paragraph(" "));
          documentc.add(p);        
          if(footnotesused[0] || footnotesused[1] || footnotesused[2]){
        	  PdfPTable table=new PdfPTable(1);
        	  table.setWidthPercentage(100);
        	  PdfPCell cell=new PdfPCell(new Paragraph("                    "));
        	  cell.setBorder(Rectangle.BOTTOM);
        	  table.addCell(cell);
        	  documentc.add(table);
        	  if(fullreport!=null && fullreport.size()==0) {
	        	  fullreport.add(table);
	        	  Phrase phrase=new Phrase();
	        	  if(footnotesused[0]){
	        		  File warning=null;
	        		  if(servcon==null)
	        			  warning = new File("src/html/warning.png");
	        		  else
	        			  warning = new File(ServletUtils.expandRelative(servcon, "nmrshiftdbhtml/warning.png"));
	        		  Image image=Image.getInstance(warning.toURI().toURL());
	        		  phrase.add(new Chunk(image,0,0));
	        	  }
	        	  if(footnotesused[0] && footnotesused[1])
	        		  phrase.add(new Chunk("/"));
	        	  if(footnotesused[1]){
	        		  File warningstrong=null;
	        		  if(servcon==null)
	        			  warningstrong=new File("src/html/warningstrong.png");
	        		  else
	        			  warningstrong=new File(ServletUtils.expandRelative(servcon, "nmrshiftdbhtml/warningstrong.png"));
	        		  Image image=Image.getInstance(warningstrong.toURI().toURL());
	        		  phrase.add(new Chunk(image,0,0));
	        	  }
	        	  if(footnotesused[0] && footnotesused[1])
	        		  phrase.add(new Chunk(" prediction unreliable"));
	        	  p = new Paragraph(phrase);
	        	  documentc.add(p);
	        	  fullreport.add(p);
	        	  if(footnotesused[0]){
	        		  c = new Chunk("1");
	        		  c.setTextRise(7);
	        		  c.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
	                  c2 = new Chunk(" Less than 3 spheres used");
	                  p = new Paragraph();
	                  p.add(c);
	                  p.add(c2);
	                  documentc.add(p);
	                  fullreport.add(p);
	        	  }
	        	  if(footnotesused[1]){
	        		  c = new Chunk("2");
	        		  c.setTextRise(7);
	        		  c.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
	                  c2 = new Chunk(" Range > 10 ppm");
	                  p = new Paragraph();
	                  p.add(c);
	                  p.add(c2);
	                  documentc.add(p);
	                  fullreport.add(p);
	        	  }
	        	  if(footnotesused[2]){
	        		  c = new Chunk("3");
	        		  c.setTextRise(7);
	        		  c.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
	                  c2 = new Chunk(" Calculated from average of shifts");
	                  p = new Paragraph();
	                  p.add(c);
	                  p.add(c2);
	                  documentc.add(p);
	                  fullreport.add(p);
	        	  }        		  
        	  }
          }
          return result;
      }


	/**
     * Generates a reliability string.
     * 
     * @param reliabilityfraction  number of total shifts/number of unreliable shifts.
     * @return The reliability string.
     */
    private static String reliability(double reliabilityfraction) {
    	if(reliabilityfraction<.3)
    		return "poor";
    	else if(reliabilityfraction<.7)
    		return "mediocre";
    	else if(reliabilityfraction<.95)
    		return "good";
    	else 
    		return "excellent";
	}


	/**
     * Generates a report from an overall mark.
     * 
     * @param mark The mark.
     * @param reliabilityfraction number of total shifts/number of unreliable shifts.
     * @return     The report string.
     */
    public static String makeJudgement(int mark, double reliabilityfraction, boolean withHtml) {
    	String color="";
    	if(withHtml) {
	        color="<span style=\"background-color:lime\">";
	        if(mark==1){
	            color="<span style=\"background-color:red\">";
	        }else if(mark<=5){
	            color="<span style=\"background-color:orange\">";
	        }
    	}
        String confidencestring=reliability(reliabilityfraction);
        String judgement = getJudgement(mark);
        color="nmrshiftdb2 quality check: "+color+""+mark+" ("+judgement+")";
        if(reliabilityfraction!=-1)
        	color=color+"/reliability: "+confidencestring;
        if(!withHtml)
        	return color;
        else 
        	return color+" </span>";
    }
    
    /**
     * Gets a judgement string from a mark.
     * 
     * @param mark The mark.
     * @return accept/revision/reject.
     */
    public static String getJudgement(int mark){
        String judgement = "accept";
        if(mark==1){
            judgement="reject";
        }else if(mark<=5){
            judgement="revision";
        }
        return judgement;
    }


    /**
     * Sends a csearch robot referee request.
     * 
     * @param sd The submittingdata to use.
     * @param id The id to send (will be in response)
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public static void sendRobotRefereeRequest(SubmittingData sd, String id) throws FileNotFoundException, IOException, Exception {
        StringBuffer emailtext=new StringBuffer(sd.getInputMolAsMdl(id,GeneralUtils.getNmrshiftdbProperty("csearchsendemail",(RunData)null),false));
        emailtext.append(">  <chemical.shift.values>\r\n");
        IAtomContainer mol = sd.getMolWithH();
        for(int l=0;l<sd.getSignalstable().getPeakChildren().size();l++){
            if(sd.getSignalstable().getPeakChildren().get(l).getAtomRefs()!=null){
                for(int k=0;k<sd.getSignalstable().getPeakChildren().get(l).getAtomRefs().length;k++){
                    emailtext.append(mol.getAtomNumber(AtomUtils.getAtomById(mol, sd.getSignalstable().getPeakChildren().get(l).getAtomRefs()[k]))+1);
                    emailtext.append(" = ");
                    emailtext.append(sd.getSignalstable().getPeakChildren().get(l).getXValue());
                    emailtext.append("\r\n");
                }
            }
        }
        if(sd.getLiterature().size()>0){
            DBLiterature literature = sd.getLiterature().get(0);
            if(literature.isPublishedInJournalFromAttributes()){
                emailtext.append("\r\n>  <author>\r\n");
                emailtext.append(literature.getAuthor()+"\r\n");
                emailtext.append("\r\n>  <literature>\r\n");
                emailtext.append(literature.getJournalData()+"\r\n");
            }
        }
        emailtext.append("\r\n>  <solvent>\r\n");
        DBConditionType solvent = sd.getSolvent();
        if(solvent.getCurrentValue().equals("") && solvent.getCurrentSelectedCondition() != null)
            emailtext.append(solvent.getCurrentSelectedCondition().getValue() +"\r\n");
        else
            emailtext.append(solvent.getCurrentValue() +"\r\n");
        if(sd.getChemName()!=null && !sd.getChemName().equals("")){
            emailtext.append("\r\n>  <compound.name>\r\n");
            emailtext.append(sd.getChemName()+"\r\n");
            
        }
        emailtext.append("\r\n>  <source.code>\r\n");
        emailtext.append("nmrshiftdb2\r\n");
        emailtext.append("\r\n>  <source.number>\r\n");
        emailtext.append(sd.getSpectrumid()+"\r\n");
        emailtext.append("\r\n>  <date.of.creation>\r\n");
        emailtext.append(sd.getThisDate()+"\r\n");
        emailtext.append("\r\n>  <proton.frequency>\r\n");
        emailtext.append(sd.getFrequency()+"\r\n");
        emailtext.append("\r\n>  <temperature>\r\n");
        emailtext.append(sd.getTemperature()+"\r\n");
        emailtext.append("\r\n>  <password>\r\n");
        emailtext.append((new UrlTool().isLabserver() || (sd.getReviewFlag()!=null && sd.getReviewFlag().equals(NmrshiftdbConstants.PRIVATE)) ? "private" : "public") +"\r\n");
        emailtext.append("\r\n\r\n$$$$\r\n");
        MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer((RunData)null), GeneralUtils.getNmrshiftdbProperty("csearchemail", (RunData)null), GeneralUtils.getNmrshiftdbProperty("csearchsendemail",(RunData)null), "nmrshiftdb2 referee request", emailtext.toString());
        if (email.send() == false) {
            Log.error("Sending email to csearch failed for "+id);
        }else{
            Category nmrshiftdbLogger = Category.getInstance("nmrshiftdb");
            nmrshiftdbLogger.info(new Date().toString()+" csearch email sent "+emailtext);
        }
    }


	public static double[] calculateScore2d(IAtomContainer molWithH, SubmittingData subDataC, SubmittingData subDataH,
			List<Peak2D> cosypeaks, List<Peak2D> tocsypeaks, List<Peak2D> noesypeaks, List<Peak2D> hmbcpeaks,
			List<Peak2D> hsqcpeaks, Document document2d, ServletConfig servcon, boolean showtocsy, boolean shownoesy) throws CDKException, Exception {
		if(cosypeaks.size()==0 && tocsypeaks.size()==0 && noesypeaks.size()==0 && hmbcpeaks.size()==0 && hsqcpeaks.size()==0) {
	        double[] result=new double[6];
	        result[0]=-1;
	        document2d.add(Chunk.NEWLINE);
	        document2d.add(new Paragraph("JKLj;l"));
        	return result;
		}
        String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "qc1.jpg";
        //type identifiers
        GeneralUtils.makeJpg(subDataH.getInputMolAsMdl(null,null,false),"/"+filename,servcon,300,250,true, subDataC.getChoosenSpectrumType(), subDataC.getIdentifiers(), Color.WHITE);
        Image image1 = Image.getInstance(ServletUtils.expandRelative(servcon, filename));
        DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
		  format.applyPattern("#####0.00");
          BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
          Font fontSym = new Font(bs,12);
        Chunk c = new Chunk("nmrshiftdb2 Quality Report - ");
        c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
        Paragraph p = new Paragraph();
        p.add(c);
        p.add(new Paragraph(" "));
        document2d.add(p);
        document2d.add(Chunk.NEWLINE);
        document2d.add(image1);
        p = new Paragraph(" ");
        p.add(new Paragraph(" "));
        document2d.add(p);
        PdfPTable tablepdfc = new PdfPTable(subDataH.getSignalstable().getChildCount()+1);
        tablepdfc.setWidthPercentage(100);
        //tablepdfc.setWidths(new int[]{1,1,2,1,1,1});
        PdfPCell cell8 = new PdfPCell();
        cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
        cell8.setBackgroundColor(Color.LIGHT_GRAY);
        cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tablepdfc.addCell(cell8);
        for(CMLPeak peak : subDataH.getSignalstableOrderedDescending()){
            StringBuffer sb=new StringBuffer();
            if(peak.getAtomRefs()!=null) {
	        	for(String atomref : peak.getAtomRefs())
	        		sb.append(subDataH.getLabel(atomref)+" ");
            }
            sb.append("\n"+peak.getXValue());
            cell8 = new PdfPCell(new Paragraph(sb.toString()));
            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            cell8.setBackgroundColor(Color.LIGHT_GRAY);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tablepdfc.addCell(cell8);
        }
		int atomid1=1;
        int potentialpeaks=0;
        int correctpeaks=0;
        int falsemarked=0;
        for(CMLPeak peak : subDataH.getSignalstableOrderedAscending()){
            StringBuffer sb=new StringBuffer();
            if(peak.getAtomRefs()!=null) {
	        	for(String atomref : peak.getAtomRefs())
	        		sb.append(subDataH.getLabel(atomref)+" ");
            }
            sb.append("\n"+peak.getXValue());
            cell8 = new PdfPCell(new Paragraph(sb.toString()));
            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            if(atomid1%2==0)
            	cell8.setBackgroundColor(Color.LIGHT_GRAY);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tablepdfc.addCell(cell8);
            int atomid2=subDataH.getSignalstableOrderedAscending().size();
	        for(CMLPeak peaky : subDataH.getSignalstableOrderedDescending()){
	            Paragraph para = new Paragraph();
	            if(atomid2>atomid1){
		            sb = new StringBuffer();
		            if(SubmittingData.containsPeak(cosypeaks, peak, peaky, true) || subDataH.isValidCrosspeak("cosy",peaky,peak)){
		            	sb.append("  COSY");
			            Chunk chunk2=new Chunk(sb.toString());
			            Font font2 = new Font();
		            	if(subDataH.isValidCrosspeak("cosy",peaky,peak)){
		            		potentialpeaks++;
				            if(SubmittingData.containsPeak(cosypeaks, peak, peaky, true) == subDataH.isValidCrosspeak("cosy",peaky,peak)){
				            	correctpeaks++;
				            	font2.setColor(Color.GREEN);
				            }else{ 
				            	font2.setColor(Color.RED);
				            }
		            	}else{
		            		falsemarked++;
			            	font2.setColor(Color.RED);
			            }
			            chunk2.setFont(font2);
			            chunk2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, font2.getColor());
			            para.add(chunk2);
		            }
		            if(showtocsy){
			            sb = new StringBuffer();
			            if(SubmittingData.containsPeak(tocsypeaks, peak, peaky, true) || subDataH.isValidCrosspeak("tocsy",peaky,peak)){
			            	sb.append("  TOCSY");
				            Chunk chunk2=new Chunk(sb.toString());
				            Font font2 = new Font();
				            if(subDataH.isValidCrosspeak("tocsy",peaky,peak)){
				            	potentialpeaks++;
					            if(SubmittingData.containsPeak(tocsypeaks, peak, peaky, true) == subDataH.isValidCrosspeak("tocsy",peaky,peak)){
					            	correctpeaks++;
					            	font2.setColor(Color.GREEN);
					            }else{ 
					            	font2.setColor(Color.RED);
					            }
			            	}else{
			            		falsemarked++;
				            	font2.setColor(Color.RED);
				            }
				            chunk2.setFont(font2);
				            chunk2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, font2.getColor());
				            para.add(chunk2);
			            }
		            }
		            if(shownoesy){
			            sb = new StringBuffer();
			            if(SubmittingData.containsPeak(noesypeaks, peak, peaky, false) || subDataH.isValidCrosspeak("noesy",peaky,peak)){
			            	sb.append("  NOESY");
				            Chunk chunk2=new Chunk(sb.toString());
				            Font font2 = new Font();
			            	if(subDataH.isValidCrosspeak("noesy",peaky,peak)){
			            		potentialpeaks++;
					            if(SubmittingData.containsPeak(noesypeaks, peak, peaky, false) == subDataH.isValidCrosspeak("noesy",peaky,peak)){
					            	correctpeaks++;
					            	font2.setColor(Color.GREEN);
					            }else{ 
					            	font2.setColor(Color.RED);
					            }
			            	}else{
			            		falsemarked++;
				            	font2.setColor(Color.RED);
				            }
				            chunk2.setFont(font2);
				            chunk2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, font2.getColor());
				            para.add(chunk2);
		            	}
		            }
	            }
	            cell8 = new PdfPCell(para);
	            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
	            if(atomid1%2==0)
	            	cell8.setBackgroundColor(Color.LIGHT_GRAY);
	            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            tablepdfc.addCell(cell8);
	            atomid2--;
	        }
	        atomid1++;
        }
        document2d.add(tablepdfc);
        document2d.add(Chunk.NEWLINE);
        document2d.add(Chunk.NEWLINE);
        tablepdfc = new PdfPTable(subDataH.getSignalstable().getChildCount()+1);
        tablepdfc.setWidthPercentage(100);
        //tablepdfc.setWidths(new int[]{1,1,2,1,1,1});
        cell8 = new PdfPCell();
        cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
        cell8.setBackgroundColor(Color.LIGHT_GRAY);
        cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tablepdfc.addCell(cell8);
        for(CMLPeak peak : subDataH.getSignalstableOrderedDescending()){
            StringBuffer sb=new StringBuffer();
            if(peak.getAtomRefs()!=null) {
	        	for(String atomref : peak.getAtomRefs())
	        		sb.append(subDataH.getLabel(atomref)+" ");
            }
            sb.append("\n"+peak.getXValue());
            cell8 = new PdfPCell(new Paragraph(sb.toString()));
            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            cell8.setBackgroundColor(Color.LIGHT_GRAY);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tablepdfc.addCell(cell8);
        }
		atomid1=1;
        for(CMLPeak peak : subDataC.getSignalstableOrderedAscending()){
            StringBuffer sb=new StringBuffer();
            if(peak.getAtomRefs()!=null) {
	        	for(String atomref : peak.getAtomRefs())
	        		sb.append(subDataH.getLabel(atomref)+" ");
            }
            sb.append("\n"+peak.getXValue());
            cell8 = new PdfPCell(new Paragraph(sb.toString()));
            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            if(atomid1%2==0)
            	cell8.setBackgroundColor(Color.LIGHT_GRAY);
            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tablepdfc.addCell(cell8);
	        for(CMLPeak peaky : subDataH.getSignalstableOrderedDescending()){
	            sb = new StringBuffer();
	            Paragraph para = new Paragraph();
	            if(SubmittingData.containsPeak(hsqcpeaks, peaky, peak, false) || subDataH.isValidCrosspeak("hsqc",peaky,peak)){
	            	sb.append("HSQC");
		            Chunk chunk=new Chunk(sb.toString());
		            Font font = new Font();
	            	if(subDataH.isValidCrosspeak("hsqc",peaky,peak)){
	            		potentialpeaks++;
			            if(SubmittingData.containsPeak(hsqcpeaks, peaky, peak, false) == subDataH.isValidCrosspeak("hsqc",peaky,peak)){
			            	font.setColor(Color.GREEN);
			            	correctpeaks++;
			            }else{ 
			            	font.setColor(Color.RED);
			            }
	            	}else{
	            		falsemarked++;
		            	font.setColor(Color.RED);
		            }
		            chunk.setFont(font);
		            chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, font.getColor());
		            para.add(chunk);
	            }
	            if(SubmittingData.containsPeak(hmbcpeaks, peaky, peak, false) || subDataH.isValidCrosspeak("hmbc",peaky,peak)){
	            	sb = new StringBuffer();
	            	sb.append("  HMBC");
		            Chunk chunk2=new Chunk(sb.toString());
		            Font font2 = new Font();
	            	if(subDataH.isValidCrosspeak("hmbc",peaky,peak)){
	            		potentialpeaks++;
			            if(SubmittingData.containsPeak(hmbcpeaks, peaky, peak, false) == subDataH.isValidCrosspeak("hmbc",peaky,peak)){
			            	correctpeaks++;
			            	font2.setColor(Color.GREEN);
			            }else{ 
			            	font2.setColor(Color.RED);
			            }
	            	}else{
	            		falsemarked++;
		            	font2.setColor(Color.RED);
	            	}
		            chunk2.setFont(font2);
		            chunk2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, font2.getColor());
		            para.add(chunk2);
	        	}
	            cell8 = new PdfPCell(para);
	            cell8.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
	            if(atomid1%2==0)
	            	cell8.setBackgroundColor(Color.LIGHT_GRAY);
	            cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
	            cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            tablepdfc.addCell(cell8);
	        }
	        atomid1++;
        }
        document2d.add(tablepdfc);
        double[] result=new double[6];
        int overallMarkC=(int)((float)(correctpeaks-falsemarked)/(float)potentialpeaks*10);
        if(overallMarkC<1)
            overallMarkC=1;
        result[0]=overallMarkC;
        result[1]=correctpeaks;
        result[2]=potentialpeaks-correctpeaks;
        document2d.add(Chunk.NEWLINE);
        document2d.add(new Paragraph("Overall mark "+overallMarkC+" (out of 1 to 10, 10 being best)"));
        document2d.add(new Paragraph("No. of calculated peaks: "+potentialpeaks+", of which "+correctpeaks+" were correctly selected. "+falsemarked+" crosspeaks were selected, but are not in the calculated peaks.", fontSym));
        Font font = new Font();
        if((int)result[0]==1)
            font.setColor(Color.RED);
        else if((int)result[0]<=5)
            font.setColor(Color.ORANGE);
        else
            font.setColor(Color.GREEN);          
        document2d.add(new Paragraph("Result: "+overallMarkC+"/"+SpectrumUtils.getJudgement(overallMarkC),font));
        return result;
	}
	
	
	public static List<List<Pair<Integer,Integer>>> judgeCosy(DBSpectrum cosy, DBSpectrum proton) throws OptionalDataException, TorqueException, IOException, DataSetException, NmrshiftdbException{
		//We find all potential shifts
		List<DBSignal> potshifts=new ArrayList<DBSignal>();
		Map<Float, Float> done= new HashMap<Float,Float>();
		IAtomContainer mol=cosy.getDBMolecule().getAsCDKMolecule(1);
		for(IAtom hatom1 : mol.atoms()){
			ShortestPaths sp = new ShortestPaths(mol,hatom1);
			for(IAtom hatom2 : mol.atoms()){
				if(mol.getAtomNumber(hatom1)<mol.getAtomNumber(hatom2)){
					if(sp.distanceTo(hatom2)==3 && proton.getAssignedSignal(cosy.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom1)))!=null && proton.getAssignedSignal(cosy.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom2)))!=null){
						DBSignal signal=new DBSignal();
						Map<Integer,DBShift> shifts=new HashMap<Integer,DBShift>();
						DBShift shift1=new DBShift();
						shift1.setValue(proton.getAssignedSignal(cosy.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom1))).getFirstShift().getValue());
						DBShift shift2=new DBShift();
						shift2.setValue(proton.getAssignedSignal(cosy.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom2))).getFirstShift().getValue());
						if(!(done.containsKey(shift1.getValue()) && Math.abs(done.get(shift1.getValue())-shift2.getValue())<0.01) && !(done.containsKey(shift2.getValue()) && Math.abs(done.get(shift2.getValue())-shift1.getValue())<0.01)){
							shifts.put(1, shift1);
							shifts.put(2, shift2);
							signal.setShifts(shifts);
							int[] atomnumbers=new int[2];
							atomnumbers[0]=mol.getAtomNumber(hatom1);
							atomnumbers[1]=mol.getAtomNumber(hatom2);
							signal.atomnumbers=atomnumbers;
							potshifts.add(signal);
							done.put(shift1.getValue(), shift2.getValue());
						}
					}
				}
			}
		}
		List<Pair<Integer,Integer>> matched=new ArrayList<Pair<Integer,Integer>>();
		List<Pair<Integer,Integer>> unmatched=new ArrayList<Pair<Integer,Integer>>();
		List<Pair<Integer,Integer>> predictedleft=new ArrayList<Pair<Integer,Integer>>();
		boolean[] predictedhits=new boolean[potshifts.size()];
		//we check how many false positives and false negatives there are
		for(DBSignal peak : cosy.getDBSignals()){
			boolean match=false;
			int i=0;
			for(DBSignal predictedpeak : potshifts){
				if((Math.abs(peak.getDBShift(1).getValue()-predictedpeak.getDBShift(1).getValue())<0.01 && Math.abs(peak.getDBShift(2).getValue()-predictedpeak.getDBShift(2).getValue())<0.01) || (Math.abs(peak.getDBShift(1).getValue()-predictedpeak.getDBShift(2).getValue())<0.01 && Math.abs(peak.getDBShift(2).getValue()-predictedpeak.getDBShift(1).getValue())<0.01)){
					System.out.println(peak.getDBShift(1).getValue()+" "+peak.getDBShift(2).getValue());
					matched.add(new Pair(peak.getDBAtom(1, 0).getMdlNumberPlus1Heavy()-1,peak.getDBAtom(2,0).getMdlNumberPlus1Heavy()-1));
					match=true;
					predictedhits[i]=true;
					break;
				}
				i++;
			}
			if(!match)
				unmatched.add(new Pair(peak.getDBShift(1).getDBSignalDBAtomsRelatedByAxis().get(0).getDBAtom().getMdlNumber(),peak.getDBShift(2).getDBSignalDBAtomsRelatedByAxis().get(0).getDBAtom().getMdlNumber()));
		}
		for(int i=0;i<predictedhits.length;i++){
			if(!predictedhits[i])
				predictedleft.add(new Pair(potshifts.get(i).atomnumbers[0], potshifts.get(i).atomnumbers[1]));
		}
		//this is the quality result
		List<List<Pair<Integer,Integer>>> result=new ArrayList<List<Pair<Integer,Integer>>>();
		result.add(matched);
		result.add(unmatched);
		result.add(predictedleft);
		return result;
	}


	public static List<List<Pair<Integer,Integer>>> judgeHmbc(DBSpectrum hmbc, DBSpectrum proton, DBSpectrum carbon) throws OptionalDataException, TorqueException, IOException, DataSetException, NmrshiftdbException{
		//We find all potential shifts
		List<DBSignal> potshifts=new ArrayList<DBSignal>();
		Map<Float, Float> done= new HashMap<Float,Float>();
		IAtomContainer mol=hmbc.getDBMolecule().getAsCDKMolecule(1);
		for(IAtom hatom : mol.atoms()){
			if(hatom.getSymbol().equals("H")){
				ShortestPaths sp = new ShortestPaths(mol,hatom);
				for(IAtom catom : mol.atoms()){
					if(catom.getSymbol().equals("C") &&mol.getAtomNumber(hatom)<mol.getAtomNumber(catom)){
						if((sp.distanceTo(catom)==2 || sp.distanceTo(catom)==3) && proton.getAssignedSignal(hmbc.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom)))!=null && carbon.getAssignedSignal(hmbc.getDBMolecule().getAtomAt(mol.getAtomNumber(catom)))!=null){
							DBSignal signal=new DBSignal();
							Map<Integer,DBShift> shifts=new HashMap<Integer,DBShift>();
							DBShift shift1=new DBShift();
							shift1.setValue(proton.getAssignedSignal(hmbc.getDBMolecule().getAtomAt(mol.getAtomNumber(hatom))).getFirstShift().getValue());
							DBShift shift2=new DBShift();
							shift2.setValue(proton.getAssignedSignal(hmbc.getDBMolecule().getAtomAt(mol.getAtomNumber(catom))).getFirstShift().getValue());
							if(!(done.containsKey(shift1.getValue()) && Math.abs(done.get(shift1.getValue())-shift2.getValue())<0.01) && !(done.containsKey(shift2.getValue()) && Math.abs(done.get(shift2.getValue())-shift1.getValue())<0.01)){
								shifts.put(1, shift1);
								shifts.put(2, shift2);
								signal.setShifts(shifts);
								int[] atomnumbers=new int[2];
								atomnumbers[0]=mol.getAtomNumber(hatom);
								atomnumbers[1]=mol.getAtomNumber(catom);
								signal.atomnumbers=atomnumbers;
								potshifts.add(signal);
								done.put(shift1.getValue(), shift2.getValue());
							}
						}
					}
				}
			}
		}
		List<Pair<Integer,Integer>> matched=new ArrayList<Pair<Integer,Integer>>();
		List<Pair<Integer,Integer>> unmatched=new ArrayList<Pair<Integer,Integer>>();
		List<Pair<Integer,Integer>> predictedleft=new ArrayList<Pair<Integer,Integer>>();
		boolean[] predictedhits=new boolean[potshifts.size()];
		//we check how many false positives and false negatives there are
		for(DBSignal peak : hmbc.getDBSignals()){
			boolean match=false;
			int i=0;
			for(DBSignal predictedpeak : potshifts){
				if((Math.abs(peak.getDBShift(1).getValue()-predictedpeak.getDBShift(1).getValue())<0.01 && Math.abs(peak.getDBShift(2).getValue()-predictedpeak.getDBShift(2).getValue())<0.01) || (Math.abs(peak.getDBShift(1).getValue()-predictedpeak.getDBShift(2).getValue())<0.01 && Math.abs(peak.getDBShift(2).getValue()-predictedpeak.getDBShift(1).getValue())<0.01)){
					System.out.println(peak.getDBShift(1).getValue()+" "+peak.getDBShift(2).getValue());
					matched.add(new Pair(peak.getDBAtom(1, 0).getMdlNumberPlus1Heavy()-1,peak.getDBAtom(2,0).getMdlNumberPlus1Heavy()-1));
					match=true;
					predictedhits[i]=true;
					break;
				}
				i++;
			}
			if(!match)
				unmatched.add(new Pair(peak.getDBShift(1).getDBSignalDBAtomsRelatedByAxis().get(0).getDBAtom().getMdlNumber(),peak.getDBShift(2).getDBSignalDBAtomsRelatedByAxis().get(0).getDBAtom().getMdlNumber()));
		}
		for(int i=0;i<predictedhits.length;i++){
			if(!predictedhits[i])
				predictedleft.add(new Pair(potshifts.get(i).atomnumbers[0], potshifts.get(i).atomnumbers[1]));
		}
		//this is the quality result
		List<List<Pair<Integer,Integer>>> result=new ArrayList<List<Pair<Integer,Integer>>>();
		result.add(matched);
		result.add(unmatched);
		result.add(predictedleft);
		return result;
	}
}

