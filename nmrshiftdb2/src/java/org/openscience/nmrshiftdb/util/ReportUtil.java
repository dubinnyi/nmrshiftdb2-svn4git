package org.openscience.nmrshiftdb.util;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.torque.TorqueException;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jcamp.spectrum.Assignment;
import org.jcamp.spectrum.IAssignmentTarget;
import org.jcamp.spectrum.Multiplicity;
import org.jcamp.spectrum.Pattern;
import org.jcamp.spectrum.assignments.TwoAtomsReference;
import org.openscience.cdk.Bond;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.graph.invariant.Canon;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.PredictionTool;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBCondition;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSignalDBAtom;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.comparator.DBSignalComparator;
import org.openscience.nmrshiftdb.util.comparator.ShiftsAsRecordComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLPeak;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import com.lowagie.text.pdf.draw.VerticalPositionMark;

import de.unikoeln.chemie.nmr.data.Peak2D;

/**
 * This class contains methods to create a pdf report. createFullReport is the most important method.
 * 
 * @author shk3
 *
 */
public class ReportUtil {

    static List<Integer> greenAtomsInt=new ArrayList<Integer>();
    static List<Integer> orangeAtomsInt=new ArrayList<Integer>();
    static List<Integer> redAtomsInt=new ArrayList<Integer>();
    static List<Integer> grayAtomsInt=new ArrayList<Integer>();

    /**
     * This is one of the steps in creating a full report. See createFullReport for an example.
     * 
     * @param reportpdf
     * @param servcon
     * @param cdkmoloriginal
     * @param cdkmol
     * @param solvent
     * @param smiles
     * @param temperature
     * @param types
     * @param frequency
     * @param comment
     * @param literature
     * @param spectrum1h
     * @param spectrum13c
     * @param name
     * @param id
     * @throws Exception
     */
    public static void startFullReport(Document reportpdf, ServletConfig servcon, IAtomContainer cdkmoloriginal, IAtomContainer cdkmol, String solvent, String smiles, List<String> temperature, List<String> types, List<String> frequency, List<String> comment, String literature, DBSpectrum spectrum1h, DBSpectrum spectrum13c, String name, int id) throws Exception {
    	BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	Font fontSym = new Font(bs,18);
        Font bold = new Font(bs,12,Font.BOLD);
        Chunk c = new Chunk("Structure Elucidation Report nmrshiftdb2", fontSym);
        c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLUE);
        Paragraph p = new Paragraph();
        p.add(c);
        reportpdf.add(p);
        fontSym = new Font(bs,12);
        Chunk c2 = new Chunk("<Name>", fontSym);
        if(name!=null)
        	c2 = new Chunk(name, fontSym);
        c2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
        Paragraph p2 = new Paragraph();
        p2.add(c2);
        Chunk glue = new Chunk(new VerticalPositionMark());
        p2.add(glue);
        p2.add(new Chunk(new Date().toString()));
        reportpdf.add(p2);
        String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "qc1.jpg";
        StringWriter sw = new StringWriter();
        MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
		mdlwriter.writeMolecule(cdkmoloriginal);
		mdlwriter.close();
        GeneralUtils.makeJpg(sw.toString(),"/"+filename,servcon,300,250,false, null, null, Color.WHITE);
        Image image1 = Image.getInstance(ServletUtils.expandRelative(servcon, filename));
        reportpdf.add(image1);
        c = new Chunk("Details", fontSym);
        c.setUnderline(0.1f, -2f);
        c.setFont(bold);
        p = new Paragraph();
        p.add(c);
        reportpdf.add(p);
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_LEFT);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        IMolecularFormula mfa = MolecularFormulaManipulator.getMolecularFormula(cdkmol);
        table.setWidths(new float[]{1, 3});
        table.addCell(new Phrase("nmrshiftdb2-ID: "));
        if(id==0)
        	table.addCell(new Phrase("<id>"));
        else
        	table.addCell(new Phrase(Integer.toString(id)));
        table.addCell(new Phrase("Name: "));
        if(name==null)
        	table.addCell(new Phrase("<name>"));
        else
        	table.addCell(new Phrase(name));
        table.addCell(new Phrase("Chemical Formula: "));
        table.addCell(new Phrase(MolecularFormulaManipulator.getString(mfa)));
        table.addCell(new Phrase("Mass [Da]: "));
        table.addCell(new Phrase(Float.toString((float)MolecularFormulaManipulator.getNaturalExactMass(mfa))));
        table.addCell(new Phrase("Solvent: "));
        table.addCell(new Phrase(solvent));
        reportpdf.add(table);
        if(literature!=null){
        	  reportpdf.add(new Paragraph("\n"));
	          c = new Chunk(literature, fontSym);
	          p = new Paragraph();
	          p.add(c);
	          reportpdf.add(p);
        }else{
	        c = new Chunk("<Literature>");
	        c.setUnderline(0.1f, -2f);
	        c.setFont(bold);
	        p = new Paragraph();
	        p.add(c);
        }        
        if(spectrum1h!=null){
            VelocityContext context = new VelocityContext();
            context.put("spectrum", spectrum1h);
            context.put("formula", spectrum1h.getDBMolecule().getMolecularFormula(false));
            context.put("weight", Float.toString(spectrum1h.getDBMolecule().getMolecularWeightFloat()));
            // lets render the template
            StringWriter w = new StringWriter();
            Velocity.mergeTemplate("exporttemplates/report.vm", "ISO-8859-1", context, w);
            c = new Chunk(w.toString(), fontSym);
            p = new Paragraph();
            p.add(new Paragraph("\n"));
            p.add(c);
            reportpdf.add(p);        	  
		}
        if(spectrum13c!=null){
            VelocityContext context = new VelocityContext();
            context.put("spectrum", spectrum13c);
            context.put("formula", spectrum13c.getDBMolecule().getMolecularFormula(false));
            context.put("weight", Float.toString(spectrum13c.getDBMolecule().getMolecularWeightFloat()));
            // lets render the template
            StringWriter w = new StringWriter();
            Velocity.mergeTemplate("exporttemplates/report.vm", "ISO-8859-1", context, w);
            c = new Chunk(w.toString(), fontSym);
            p = new Paragraph();
            p.add(new Paragraph("\n"));
            p.add(c);
            reportpdf.add(p);        	  
		}
        reportpdf.add(new Paragraph(new Chunk("   ")));
        c = new Chunk("Experimental Details", fontSym);
        c.setUnderline(0.1f, -2f);
        c.setFont(bold);
        p = new Paragraph();
        p.add(c);
        reportpdf.add(p);
        reportpdf.add(new Paragraph(new Chunk("   ")));
        table = new PdfPTable(6);
        table.setWidths(new float[]{1, 1, 1, 1, 1, 2});
        PdfPCell cell=new PdfPCell(new Paragraph("Experiment Type"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        cell=new PdfPCell(new Paragraph("v [Mhz]"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        cell=new PdfPCell(new Paragraph("Offset [ppm]"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        cell=new PdfPCell(new Paragraph("Resolution [Hz]"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        cell=new PdfPCell(new Paragraph("Temperature [K]"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        cell=new PdfPCell(new Paragraph("Comment"));
        cell.setBackgroundColor(new Color(53,151,255));
        table.addCell(cell);
        int l=0;
        for(String type : types){
      		c=new Chunk(type);
            cell=new PdfPCell(new Paragraph(c));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            cell=new PdfPCell(new Paragraph(frequency.get(l)));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            cell=new PdfPCell(new Paragraph(""));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            cell=new PdfPCell(new Paragraph(""));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            cell=new PdfPCell(new Paragraph(temperature.get(l)));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            cell=new PdfPCell(new Paragraph(comment.get(l)));
            if(l%2==0)
          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
            l++;
        }          
        reportpdf.add(table);
        reportpdf.add(new Paragraph(new Chunk("   ")));
        c = new Chunk("Descriptors", fontSym);
        c.setUnderline(0.1f, -2f);
        c.setFont(bold);
        p = new Paragraph();
        p.add(c);
        reportpdf.add(p);
        table = new PdfPTable(2);
        table.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_LEFT);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.setWidths(new float[]{1, 5});
        table.addCell(new Phrase("SMILES: "));
        table.addCell(new Phrase(smiles));
        table.addCell(new Phrase("INChI: "));
        table.addCell(new Phrase(GeneralUtils.generateInchi(cdkmol,false)));
        table.addCell(new Phrase("INChI key: "));
        table.addCell(new Phrase(GeneralUtils.generateInchiKey(cdkmol)));
        reportpdf.add(table);
	}

	/**
	 * This is one of the steps in creating a full report. See createFullReport for an example.
	 * 
	 * @param servcon
	 * @param spectrumtype
	 * @param cdkmolorig
	 * @param cdkmol
	 * @param subDataH
	 * @param subDataC
	 * @param table
	 * @param table2d
	 * @param couplings
	 * @param peaks
	 * @throws Exception
	 */
	public static void make2d(ServletConfig servcon, String spectrumtype, IAtomContainer cdkmolorig, IAtomContainer cdkmol, SubmittingData subDataH, SubmittingData subDataC, PdfPTable table, PdfPTable table2d, Map<Color,List<IBond>> couplings, List<Peak2D> peaks) throws Exception {
        BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), "CP1253", BaseFont.EMBEDDED);
        Font fontSym = new Font(bs,12);
        Font fontGreen = new Font(bs,12);
        fontGreen.setColor(Color.GREEN);
        Font fontOrange = new Font(bs,12);
        fontOrange.setColor(Color.ORANGE);
        Font fontRed = new Font(bs,12);
        fontRed.setColor(Color.RED);
		if(peaks.size()==0) {
			PdfPCell cell = new PdfPCell();
  		  cell.setBorder(Rectangle.NO_BORDER);
  		  cell.addElement(new Phrase(spectrumtype+" - no peaks"));
  		  table2d.setWidthPercentage(100);
  		  table.addCell(cell);
          Paragraph p = new Paragraph();
          p.add(new Chunk("No mark possible",fontRed));
          cell.addElement(p);
          cell.setBorder(Rectangle.NO_BORDER);
          table2d.addCell(cell);
		  return;
		}
		  if(spectrumtype.contains("TOCSY") || spectrumtype.contains("HMBC") || spectrumtype.contains("COSY")){
              String relativepath = ServletUtils.expandRelative(servcon,"/WEB-INF");
              cdkmolorig.setProperty("couplings", couplings);
    		  Export export = new Export(cdkmolorig,cdkmol);
    		  String image = export.getImage(true,"png",relativepath + "/tmp/" + System.currentTimeMillis(),300,300,true,null,null,null,Color.BLUE, null, null, null, null, null);
    		  Image image1 = Image.getInstance(image);
    		  PdfPCell cell = new PdfPCell();
    		  cell.setBorder(Rectangle.NO_BORDER);
    		  cell.addElement(new Phrase(spectrumtype));
    		  cell.addElement(image1);
    		  table.setWidthPercentage(100);
    	      table2d.setWidthPercentage(100);
    		  table.addCell(cell);
			  List<Pair<Integer,Integer>> matched=new ArrayList<Pair<Integer,Integer>>();
			  List<Pair<Integer,Integer>> unmatched=new ArrayList<Pair<Integer,Integer>>();
			  List<Pair<Integer,Integer>> predictedleft=new ArrayList<Pair<Integer,Integer>>();
			  IAtomContainer hmol=subDataH.getMolWithH();
              int cmolorigAtomCount=subDataH.getMolOriginal().getAtomCount();
    		  if(spectrumtype.contains("TOCSY") || spectrumtype.contains("COSY")){
        		  int k=0;
        		  for(CMLPeak peak : subDataH.getSignalstableOrderedAscending()){
        			  int atomid2=subDataH.getSignalstableOrderedAscending().size()-1;
        			  for(CMLPeak peaky : subDataH.getSignalstableOrderedDescending()){
        			      if(atomid2>k){
                        	  boolean containspeak = SubmittingData.containsPeak(peaks,peak,peaky, true);
                        	  boolean isvalidpeak = subDataH.isValidCrosspeak(spectrumtype.toLowerCase(),peaky,peak);
                        	  //System.out.println(peak.getXValue()+" "+peaky.getXValue()+" "+containspeak+" "+isvalidpeak);
                        	  if(peak.getAtomRefs()!=null && peaky.getAtomRefs()!=null && (containspeak || isvalidpeak)) {
                        		  boolean found=false;
	                        	  for(String atom1 : peak.getAtomRefs()){
	                    			  for(String atom2 : peaky.getAtomRefs()){
                    					  ShortestPaths sp = new ShortestPaths(hmol,AtomUtils.getAtomById(hmol, atom1));
                    					  int distance=sp.distanceTo(AtomUtils.getAtomById(hmol, atom2));
	                    				  IAtom atom1cdk=AtomUtils.getAtomById(hmol, atom1);
	                    				  int atom1int = hmol.getAtomNumber(atom1cdk);
	                    				  if(atom1cdk.getSymbol().equals("H") && atom1int>=cmolorigAtomCount)
	                    					  atom1int=hmol.getAtomNumber(hmol.getConnectedAtomsList(atom1cdk).get(0));
	                    				  IAtom atom2cdk=AtomUtils.getAtomById(hmol,atom2);
	                    				  int atom2int = hmol.getAtomNumber(atom2cdk);
	                    				  if(atom2cdk.getSymbol().equals("H") && atom2int>=cmolorigAtomCount)
	                    					  atom2int=hmol.getAtomNumber(hmol.getConnectedAtomsList(atom2cdk).get(0));
	                    				  if(atom1int!=atom2int) {
	                    					  if(spectrumtype.contains("TOCSY") || (spectrumtype.contains("COSY") && distance==3)){
			                                	  if(containspeak && isvalidpeak){
			                                		  matched.add(new Pair<Integer,Integer>(atom1int,atom2int));
			                                	  }else if(containspeak){
			                                		  unmatched.add(new Pair<Integer,Integer>(atom1int,atom2int));
			                                	  }else if(isvalidpeak){
			                                		  predictedleft.add(new Pair<Integer,Integer>(atom1int,atom2int));
			                                	  }
			                                	  found=true;
			                                	  break;
	                    					  }
		                    			  }
	                    			  }
	                    			  if(found)
	                    				  break;
                    			  }
                    		  }
                          }
                          atomid2--;
        			  }
        			  k++;
        		  }
    		  }
    		  if(spectrumtype.contains("HMBC")){
				  IAtomContainer cmol=subDataH.getMolWithH();
                  cmolorigAtomCount=subDataH.getMolOriginal().getAtomCount();
        		  for(CMLPeak peak : subDataH.getSignalstableOrderedAscending()){
        			  for(CMLPeak peaky : subDataC.getSignalstableOrderedDescending()){
                    	  boolean containspeak = SubmittingData.containsPeak(peaks,peak,peaky,false);
                    	  boolean isvalidpeak = subDataH.isValidCrosspeak(spectrumtype.toLowerCase(),peaky,peak);
                    	  //System.out.println(peak.getXValue()+" "+peaky.getXValue()+" "+containspeak+" "+isvalidpeak);
                    	  if((containspeak || isvalidpeak) && peak.getAtomRefs()!=null) {
                    		  boolean found=false;
	                    	  for(String atom1 : peak.getAtomRefs()){
	                    		  if(peaky.getAtomRefs()!=null) {
	                    			  for(String atom2 : peaky.getAtomRefs()){
                    					  ShortestPaths sp = new ShortestPaths(hmol,AtomUtils.getAtomById(hmol, atom1));
                    					  int distance=sp.distanceTo(AtomUtils.getAtomById(hmol, atom2));
                    					  //System.out.println(distance);
		                				  IAtom atom1cdk=AtomUtils.getAtomById(hmol, atom1);
		                				  int atom1int = hmol.getAtomNumber(atom1cdk);
		                				  if(atom1cdk.getSymbol().equals("H") && atom1int>=cmolorigAtomCount)
		                					  atom1int=hmol.getAtomNumber(hmol.getConnectedAtomsList(atom1cdk).get(0));
		                				  IAtom atom2cdk=AtomUtils.getAtomById(cmol, atom2);
		                				  int atom2int = cmol.getAtomNumber(atom2cdk);
		                				  if(atom2cdk.getSymbol().equals("H") && atom2int>=cmolorigAtomCount)
		                					  atom2int=cmol.getAtomNumber(cmol.getConnectedAtomsList(atom2cdk).get(0));
	                    				  if(atom1int!=atom2int) {
	                    					  if(distance==2 || distance==3) {
	                    						  //System.out.println(atom1int+"_"+atom2int+"_"+atom1+"_"+atom2);
				                            	  if(containspeak && isvalidpeak){
				                            		  matched.add(new Pair<Integer,Integer>(atom1int,atom2int));
				                            	  }else if(containspeak){
				                            		  unmatched.add(new Pair<Integer,Integer>(atom1int,atom2int));
				                            	  }else if(isvalidpeak){
				                            		  predictedleft.add(new Pair<Integer,Integer>(atom1int,atom2int));
				                            	  }
				                            	  found=true;
			                                	  break;
	                    					  }
		                				  }
	                    			  }
	                    			  if(found)
	                    				  break;	                    		  }
                			  }
                		  }
        			  }
        		  }
    		  }
    	      List<List<Pair<Integer,Integer>>> result2D=new ArrayList<List<Pair<Integer,Integer>>>();
    		  result2D.add(matched);
    		  result2D.add(unmatched);
    		  result2D.add(predictedleft);
        	  cell=new PdfPCell();
        	  cell.addElement(new Phrase(spectrumtype));
        	  cdkmolorig.removeProperty("couplings");
    		  export = new Export(cdkmolorig, cdkmol);
    		  String image2 = export.getImage(true,"png",relativepath + "/tmp/" + System.currentTimeMillis(),300,300,true,null,null,null,Color.BLUE, null, null, null, null, null, result2D);
    		  Image image3 = Image.getInstance(image2);
    		  cell.addElement(image3);
    		  cdkmolorig.removeProperty("couplings");
              Chunk c = new Chunk(result2D.get(0).size()+" of your peaks match expected peaks (",fontSym);
              Paragraph p = new Paragraph();
              p.add(c);
              c=new Chunk("green",fontGreen);
              p.add(c);
              c=new Chunk("). There are "+(result2D.get(1).size()==0 ? "no" : result2D.get(1).size()) + " peaks in your spectrum not expected (", fontSym);
              p.add(c);
              c=new Chunk("orange",fontOrange);
              p.add(c);
              c=new Chunk("), and there are "+(result2D.get(2).size()==0 ? "no" : result2D.get(2).size()) + " expected shifts not found in your spectrum (",fontSym);
              p.add(c);
              c=new Chunk("red",fontRed);
              p.add(c);
              c=new Chunk(").", fontSym);
              p.add(c);
              cell.addElement(p);
              cell.setBorder(Rectangle.NO_BORDER);
              table2d.addCell(cell);
          }
	}

	/**
	 * This is one of the steps in creating a full report. See createFullReport for an example.
	 * 
	 * @param servcon
	 * @param isotope
	 * @param nucleus
	 * @param subDataC
	 * @param carbons
	 * @param report1d
	 * @param morgan
	 * @param table1d
	 * @param tableouterprediction
	 * @param req
	 * @param fullreport 
	 * @return
	 * @throws TorqueException
	 * @throws CDKException
	 * @throws TranscoderException
	 * @throws NmrshiftdbException
	 * @throws Exception
	 */
	public static double[] start1d(ServletConfig servcon, String isotope, String nucleus, double[] limits, SubmittingData subDataC, Map<Integer, ValuesForVelocityBean> carbons, Document report1d, long[] morgan, PdfPTable table1d, PdfPTable tableouterprediction, HttpServletRequest req, List<Element> fullreport) throws TorqueException, CDKException, TranscoderException, NmrshiftdbException, Exception {
        PdfPCell cell1dshifts=new PdfPCell();
        PdfPTable table1dshifts = new PdfPTable(4);
  	  	PdfPCell cell1dpredictions=new PdfPCell();
        PdfPTable table1dprediction = new PdfPTable(3);
		BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), "CP1253", BaseFont.EMBEDDED);
        Font fontSym = new Font(bs,12);
        Font bold = new Font(bs,12,Font.BOLD);
        Font small = new Font(bs, 6, Font.BOLD);
        cell1dshifts.setBorder(Rectangle.NO_BORDER);
		Paragraph p = new Paragraph();
		Chunk c = new Chunk(isotope, small);
		c.setTextRise(7);
		p.add(c);
		c = new Chunk(nucleus, bold);
		p.add(c);
		c =new Chunk(" table of assignments",bold);
		p.add(c);
        cell1dshifts.addElement(p);
        cell1dshifts.addElement(new Paragraph(new Chunk("   ")));
  	  	cell1dpredictions.setBorder(Rectangle.NO_BORDER);
        p = new Paragraph();
		c = new Chunk(isotope, small);
		c.setTextRise(7);
		p.add(c);
		c = new Chunk(nucleus, bold);
		p.add(c);
		c =new Chunk(" assignments",bold);
		p.add(c);
		cell1dpredictions.addElement(p);
        cell1dpredictions.addElement(new Paragraph(new Chunk("   ")));
        table1dshifts.setWidthPercentage(90);
        table1dshifts.setWidths(new float[]{1, 1, 1, 1});
        PdfPCell cell=new PdfPCell(new Paragraph("Atom"));
        cell.setRowspan(2);
        cell.setBackgroundColor(new Color(53,151,255));
        table1dshifts.addCell(cell);
        cell=new PdfPCell(new Phrase("δ [ppm]", fontSym));
        cell.setBackgroundColor(new Color(53,151,255));
        cell.setRowspan(2);
        table1dshifts.addCell(cell);
        cell=new PdfPCell(new Paragraph("Multiplet structure"));
        cell.setBackgroundColor(new Color(53,151,255));
        cell.setColspan(2);
        table1dshifts.addCell(cell);
        cell=new PdfPCell(new Paragraph("Type"));
        cell.setBackgroundColor(new Color(53,151,255));
        table1dshifts.addCell(cell);
        cell=new PdfPCell();
        cell.addElement(new Paragraph("Coupling"));
        cell.addElement(new Paragraph("J [Hz]"));
        cell.setBackgroundColor(new Color(53,151,255));
        table1dshifts.addCell(cell);
        cell1dshifts.addElement(table1dshifts);
        table1d.addCell(cell1dshifts);
        //we make the prediction table
        table1dprediction.setWidthPercentage(90);
        table1dprediction.setWidths(new float[]{1, 1, 2});
        cell=new PdfPCell(new Paragraph("Atom"));
        cell.setBackgroundColor(Color.GREEN);
        table1dprediction.addCell(cell);
        cell=new PdfPCell(new Phrase("δ [ppm]", fontSym));
        cell.setBackgroundColor(Color.GREEN);
        table1dprediction.addCell(cell);
        cell=new PdfPCell();
        cell.addElement(new Paragraph("Deviation"));
        cell.addElement(new Paragraph("from prediction"));
        cell.setBackgroundColor(Color.GREEN);
        table1dprediction.addCell(cell);
        cell1dpredictions.addElement(table1dprediction);
        tableouterprediction.addCell(cell1dpredictions);
        greenAtomsInt=new ArrayList<Integer>();
        orangeAtomsInt=new ArrayList<Integer>();
        redAtomsInt=new ArrayList<Integer>();
        grayAtomsInt=new ArrayList<Integer>();
        StringBuffer resultstring=new StringBuffer();
        return SpectrumUtils.calculateScore(subDataC.getMolWithH(), subDataC.getMolOriginal(), subDataC, carbons, limits, "", report1d, servcon, morgan, new UrlTool().getBaseUrl(req), table1dshifts, table1dprediction, greenAtomsInt, orangeAtomsInt, redAtomsInt, grayAtomsInt, resultstring, fullreport);
	}

	/**
	 * Adds a red row to a 1d report table.
	 * 
	 * @param marks
	 * @param rows
	 * @param identifier
	 * @param alltable
	 * @param rowsfull
	 * @param rowsfull2
	 * @param bs
	 */
	public static void addRedRow(int[] marks, Map<String, List<PdfPCell>> rows, String identifier, PdfPTable alltable, Map<String,List<PdfPCell>> rowsfull, Map<String,List<PdfPCell>> rowsfull2, BaseFont bs) {
		PdfPCell cell1;
		PdfPCell cell2;
		PdfPCell cell3;
		PdfPCell cell4;
		PdfPCell cell5;
		PdfPCell cell6;
		//marks[1]+=10;
		  cell1 = new PdfPCell(new Paragraph(identifier));
		  cell1.setBorder(PdfPCell.RIGHT);
		  cell2 = new PdfPCell(new Paragraph("-"));
		  cell2.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
		  cell3 = new PdfPCell(new Paragraph("-"));
		  cell3.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
		  cell4 = new PdfPCell(new Paragraph("-"));
		  cell4.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
		  cell5 = new PdfPCell(new Paragraph("-"));
		  cell5.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
		  cell6 = new PdfPCell(new Paragraph("-"));
		  cell6.setBorder(PdfPCell.LEFT);
		  List<PdfPCell> row=new ArrayList<PdfPCell>();
		  row.add(cell1);
		  row.add(cell2);
		  row.add(cell3);
		  row.add(cell4);
		  row.add(cell5);
		  row.add(cell6);
		  String identifierlocal=new String(identifier);
		  while(rows.containsKey(identifierlocal)){
			  identifierlocal=identifierlocal+"'";
		  }
		  rows.put(identifierlocal, row);
		  marks[2]++;
          //This is for report all
          if(alltable!=null){
        	  List<PdfPCell> rowall = new ArrayList<>();
        	  List<PdfPCell> rowall2 = new ArrayList<>();
          	  Font bold = new Font(bs,12,Font.BOLD);
          	  Chunk c=new Chunk(identifier);
          	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          	  c.setFont(bold);
          	  PdfPCell cell=new PdfPCell(new Paragraph(c));
        	  rowall.add(cell);
        	  cell=new PdfPCell(new Paragraph("-"));
        	  rowall.add(cell);
        	  cell=new PdfPCell(new Paragraph(""));
        	  rowall.add(cell);
        	  cell=new PdfPCell(new Paragraph(""));
        	  rowall.add(cell);
        	  //we do the prediction table
          	  c=new Chunk(identifier);
          	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
          	  c.setFont(bold);
          	  cell=new PdfPCell(new Paragraph(c));
        	  rowall2.add(cell);
        	  cell=new PdfPCell(new Paragraph("-"));
        	  rowall2.add(cell);
        	  cell=new PdfPCell(new Paragraph("-"));
        	  rowall2.add(cell);
        	  rowsfull.put(identifier,rowall);
        	  rowsfull2.put(identifier,rowall2);
          }
	}
	
    /**
     * Creates a full report from a dataset, given by a molecule and solvent/keyword.
     * 
     * @param reportpdf The document to which the report is added (should be empty).
     * @param servcon The current ServletConfig.
     * @param session The current session.
     * @param req The current request.
     * @param mol The molecule for which the report should created.
     * @param solvent The solvent, name as in db.
     * @param keyword A list of keywords, separated by ;
     * @throws TorqueException
     * @throws DocumentException
     * @throws IOException
     * @throws CDKException
     * @throws TranscoderException
     * @throws NmrshiftdbException
     * @throws Exception
     */
    public static void createFullReport(Document reportpdf, ServletConfig servcon, HttpSession session, HttpServletRequest req, DBMolecule mol, String solvent, String keyword, double[] limits13c, double[] limits1h) throws TorqueException, DocumentException, IOException, CDKException, TranscoderException, NmrshiftdbException, Exception{
    	  OutputStream baosall = new FileOutputStream(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/tmp/qcall"+session.getId()+".pdf")));
        PdfWriter.getInstance(reportpdf, baosall);
        reportpdf.open();
        List<String> types1=new ArrayList<>();
        List<String> frequency1=new ArrayList<>();
        List<String> temperature1=new ArrayList<>();
        List<String> comment1=new ArrayList<>();
        List<String> types2=new ArrayList<>();
        List<String> frequency2=new ArrayList<>();
        List<String> temperature2=new ArrayList<>();
        List<String> comment2=new ArrayList<>();
        DBSpectrum spectrumc=null;
        DBSpectrum spectrumh=null;
        for(DBSpectrum spectrum : mol.selectSpectra(solvent, keyword, false)){
      	  String spectrumtype=spectrum.getDBSpectrumType().getName();
      	  	if(spectrumtype.contains("H,H-TOCSY") || spectrumtype.contains("H,C-HMBC") || spectrumtype.contains("H,H-COSY") || spectrum.getDBSpectrumType().getName().equals("13C") || spectrum.getDBSpectrumType().getName().equals("APT") || spectrum.getDBSpectrumType().getName().equals("1H") ){
      	  		types1.add(spectrum.getDBSpectrumType().getName());
      	  		frequency1.add(spectrum.getFrequency());
      	  		temperature1.add(spectrum.getTemperature());
      	  		comment1.add(new String(spectrum.getComment()));
      	  	}else {
      	  		types2.add(spectrum.getDBSpectrumType().getName());
      	  		frequency2.add(spectrum.getFrequency());
      	  		temperature2.add(spectrum.getTemperature());
      	  		comment2.add(new String(spectrum.getComment()));
      	  	}
	  			if(spectrum.getDBSpectrumType().getName().equals("13C") || spectrum.getDBSpectrumType().getName().equals("APT"))
	  				spectrumc=spectrum;
	  			if(spectrum.getDBSpectrumType().getName().equals("1H"))
	  				spectrumh=spectrum;
        }
        IAtomContainer cdkmoloriginal=mol.getAsCDKMoleculeAsEntered(1, true);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkmoloriginal);
        CDKHueckelAromaticityDetector.detectAromaticity(cdkmoloriginal);
        IAtomContainer cdkmol=mol.getAsCDKMolecule(1);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkmol);
        CDKHueckelAromaticityDetector.detectAromaticity(cdkmol);
        for(int i=0;i<cdkmol.getAtomCount();i++) {
      	  cdkmol.getAtom(i).setID("a"+i);
      	  cdkmol.getAtom(i).setImplicitHydrogenCount(0);
        }
        List<String> literatures=new ArrayList<>();
        StringBuffer literature=new StringBuffer();
        for(DBSpectrum spectrum : mol.selectSpectra(solvent, keyword, false)){
      	  for(DBLiterature lit : spectrum.getLiteraturesNew()){
      		  if(!literatures.contains(lit.toString(false))){
      			  literature.append(lit.toString(false));
      			  literature.append(", ");
      			  literatures.add(lit.toString(false));
      		  }
      	  }
        }
        if(mol.selectSpectra(solvent, keyword, false).size()>0)
		  setIdentifiers(mol.selectSpectra(solvent, keyword, false).get(0), cdkmoloriginal, cdkmol);
      List<Peak2D> cosypeaks=new ArrayList<Peak2D>();
  	  List<Peak2D> tocsypeaks=new ArrayList<Peak2D>();
  	  List<Peak2D> noesypeaks=new ArrayList<Peak2D>();
  	  List<Peak2D> hsqcpeaks=new ArrayList<Peak2D>();
  	  List<Peak2D> hmbcpeaks=new ArrayList<Peak2D>();
        SubmittingData subDataC=null;
        boolean cisfrom2d=false;
        if(spectrumc!=null) {
        	subDataC = spectrumc.getMeAsSubmittingData(true, ServletUtils.expandRelative(servcon, "/WEB-INF/conf/normalizer.xml"),false, "");
        }else {
        	subDataC = new SubmittingData(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/normalizer.xml"));
        	subDataC.setChoosenSpectrumType(DBSpectrumTypePeer.getByName("13C"));
        	subDataC.setMol(cdkmol, false, false, false);
        	//identifier nach cdkmol setzten
        	int i=0;
        	for(IAtom atom : cdkmol.atoms()) {
        		if(atom.getProperty(BasicAtomGenerator.IDENTIFIER)!=null)
        			subDataC.addIdentifier(i, atom.getProperty(BasicAtomGenerator.IDENTIFIER));
        		i++;
        	}
        	cisfrom2d=true;
        	spectrumc=new DBSpectrum();
        	spectrumc.setDBSpectrumType(DBSpectrumTypePeer.getByName("13C"));
        }
        SubmittingData subDataH = spectrumh.getMeAsSubmittingData(true, ServletUtils.expandRelative(servcon, "/WEB-INF/conf/normalizer.xml"),false, "");
        PdfPTable table = new PdfPTable(2);
	      PdfPTable table2d = new PdfPTable(2);
	      List<DBSignal> signals13c=new ArrayList<>();//this is used if 13c is from 2d
        for(DBSpectrum spectrum : mol.selectSpectra(solvent, keyword, false)) {
      	  String spectrumtype=spectrum.getDBSpectrumType().getName();
			  List<Peak2D> peaks=new ArrayList<Peak2D>();
  		  if(spectrumtype.contains("H,H-TOCSY") || spectrumtype.contains("H,C-HMBC") || spectrumtype.contains("H,H-COSY")){
  			  Map<Color,List<IBond>> couplings=new HashMap<>();
  			  couplings.put(Color.BLUE, new ArrayList<IBond>());
  			  for (DBSignal signal : spectrum.getDBSignals()) {
						for(int i=0;i<signal.getDBAtomsByAxis().size();i++){
							if(signal.getDBAtomsByAxis().get(i).size()==2){
								List<Integer> atoms1=new ArrayList<>();
								List<Integer> atoms2=new ArrayList<>();
								List<DBSignalDBAtom> sigatoms=signal.getDBSignalDBAtoms();
								for(DBSignalDBAtom atom : sigatoms) {
									if(atom.getAxis()==1) {
										atoms1.add(atom.getDBAtom().getMdlNumber());
									}else {
										atoms2.add(atom.getDBAtom().getMdlNumber());
									}
								}
								Peak2D peak2d;
								if(signal.getDBAtomsByAxis().get(i).get(0).getMdlNumber()>signal.getDBAtomsByAxis().get(i).get(1).getMdlNumber()){
									peak2d=new Peak2D(signal.getDBShift(1).getValue(),signal.getDBShift(2).getValue(),0);
									peak2d.assignments=new ArrayList<>();
									peak2d.assignments.add(new Assignment(new Pattern(0, Multiplicity.UNKNOWN), new IAssignmentTarget[]{new TwoAtomsReference(null, atoms1.stream().mapToInt(x->x).toArray(), atoms2.stream().mapToInt(x->x).toArray())}));
								}else{
									peak2d=new Peak2D(signal.getDBShift(2).getValue(),signal.getDBShift(1).getValue(),0);
									peak2d.assignments=new ArrayList<>();
									peak2d.assignments.add(new Assignment(new Pattern(0, Multiplicity.UNKNOWN), new IAssignmentTarget[]{new TwoAtomsReference(null, atoms2.stream().mapToInt(x->x).toArray(), atoms1.stream().mapToInt(x->x).toArray())}));
								}
								peaks.add(peak2d);
								IAtom atom1cdk=cdkmol.getAtom(signal.getDBAtomsByAxis().get(i).get(0).getMdlNumber());
	          				  	if(atom1cdk.getSymbol().equals("H"))
	          				  		atom1cdk=cdkmol.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom1cdk).get(0)));
	          					else
	          						atom1cdk=cdkmol.getAtom(cdkmol.getAtomNumber(atom1cdk));
	    						IAtom atom2cdk=cdkmol.getAtom(signal.getDBAtomsByAxis().get(i).get(1).getMdlNumber());
	              				if(atom2cdk.getSymbol().equals("H"))
	              				  	atom2cdk=cdkmol.getAtom(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(atom2cdk).get(0)));
	              				else
	              					atom2cdk=cdkmol.getAtom(cdkmol.getAtomNumber(atom2cdk));
								couplings.get(Color.BLUE).add(new Bond(atom1cdk,atom2cdk));
							}
						}
  			  }
  			  setIdentifiers(spectrum, cdkmoloriginal, cdkmol);
				  ReportUtil.make2d(servcon, spectrumtype, cdkmoloriginal, cdkmol, subDataH, subDataC, table, table2d, couplings, peaks);
	    		  if(spectrumtype.contains("TOCSY")){
	    			  tocsypeaks=peaks;
	    		  }
	    		  if(spectrumtype.contains("COSY")){
	    			  cosypeaks=peaks;
	    		  }
	    		  if(spectrumtype.contains("HMBC")){
	  				  hmbcpeaks=peaks;
	    		  }
  		  }
		  List<Integer> atomids=new ArrayList<>();
  		  for (DBSignal signal : spectrum.getDBSignals()) {
	  		  if(cisfrom2d && (spectrumtype.contains("H,C-HSQC") || spectrumtype.contains("H,C-HMBC"))){
	  			  //This means we have to use peak for the carbon
	  			  CMLPeak signalnew=new CMLPeak();
	  			  signalnew.setXValue(Double.parseDouble(Float.toString(signal.getDBShift(2).getValue())));
	  			  for(int i=0;i<signal.getDBSignalDBAtoms().size();i++){
	  				  if(signal.getDBSignalDBAtoms().get(i).getDBAtom().getSymbol().equals("C"))
	  					  signalnew.setAtomRefs("a"+signal.getDBSignalDBAtoms().get(i).getDBAtom().getMdlNumberPlus1());
	  			  }
	  			  subDataC.addToSignalstable(signalnew);
	  			  DBSignal dbsignal=new DBSignal();
	  			  DBShift shift=new DBShift();
	  			  shift.setAxis(1);
	  			  shift.setValue(signal.getDBShift(2).getValue());
	  			  Map<Integer, DBShift> map=new HashMap<>();
	  			  map.put(1, shift);
	  			  dbsignal.shifts=map;
	  			  dbsignal.addDBShift(shift);
	  			  dbsignal.setMultiplicity("");
	  			  List<DBAtom> atoms=new ArrayList<>();
	  			  boolean found=false;
	  			  for(DBSignal oldsignal : signals13c) {
	  				  if(signal.getDBShift(2).getValue()==oldsignal.shifts.get(1).getValue()) {
	  					  dbsignal=oldsignal;
	  					  atoms=oldsignal.getAtoms();
	  					  found=true;
	  					  break;
	  				  }
	  			  }
	  			  if(!found)
		  			  signals13c.add(dbsignal);
	  			  for(int i=0;i<signal.getDBSignalDBAtoms().size();i++){
	  				  if(signal.getDBSignalDBAtoms().get(i).getAxis()==2) {
	  					  boolean foundatom=false;
	  					  for(DBAtom atom : atoms) {
	  						  if(atom.identifier.equals(subDataC.getIdentifiers().get(signal.getDBSignalDBAtoms().get(i).getDBAtom().getMdlNumber()))) {
	  							  foundatom=true;
	  							  break;
	  						  }
	  					  }
	  					  if(!foundatom) {
			  				  DBAtom atom=new DBAtom();
			  				  atom.identifier=subDataC.getIdentifiers().get(signal.getDBSignalDBAtoms().get(i).getDBAtom().getMdlNumber());
			  				  atom.mdlNumberPlus1Heavy=signal.getDBSignalDBAtoms().get(i).getDBAtom().getMdlNumberPlus1();
			  				  atomids.add(signal.getDBSignalDBAtoms().get(i).getDBAtom().getMdlNumberPlus1());
			  				  atoms.add(atom);
	  					  }
	  				  }
	  			  }
	  			  dbsignal.setAtoms(atoms);
	  			  spectrumc.measurementConditions=spectrum.getMeasurementConditions();
	  			  spectrumc.setDBMolecule(spectrum.getDBMolecule());
	  		  }
  		  }
        }
        if(cisfrom2d) {
        	DBSignalComparator comp=new DBSignalComparator(1,  false);
        	Collections.sort(signals13c,comp);
        	spectrumc.signalsprov=signals13c;
        }
        ReportUtil.startFullReport(reportpdf, servcon, cdkmoloriginal, cdkmol, solvent, mol.getSmilesStringStr(), temperature1, types1, frequency1, comment1, literature.toString(), spectrumh, spectrumc, mol.getChemicalNamesAsOneString(false, false), mol.getMoleculeId());
    	reportpdf.add(table);
        reportpdf.add(new Paragraph(new Chunk("   ")));
        DecimalFormat format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.ENGLISH);
		format.applyPattern("#####0.00");
        PdfPTable tableouterprediction = new PdfPTable(2);
        tableouterprediction.setWidthPercentage(100);
        tableouterprediction.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableouterprediction.setWidths(new float[]{1, 1});
        PdfPTable tablegreenred = new PdfPTable(2);
        tablegreenred.setWidthPercentage(100);
        //We prepare two pdf reports
        Document documentc = new Document();
        OutputStream baosc = new FileOutputStream(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/tmp/qcc"+session.getId()+".pdf")));
        PdfWriter.getInstance(documentc, baosc);
        documentc.open();
        Document documenth = new Document();
        OutputStream baosh = new FileOutputStream(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/tmp/qch"+session.getId()+".pdf")));
        PdfWriter.getInstance(documenth, baosh);
        documenth.open();
        PdfPTable table1d = new PdfPTable(2);
        table1d.setWidthPercentage(100);
        table1d.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table1d.setWidths(new float[]{1, 1});
        Map<Integer, ValuesForVelocityBean> carbons=new HashMap<>();
        Map<Integer, ValuesForVelocityBean> hydrogens=new HashMap<>();
        for (int i = 0; i < cdkmol.getAtomCount(); i++) {
            if (subDataC!=null && cdkmol.getAtom(i).getSymbol().equals("C")) {
                double shift=-1;
                if(!cisfrom2d && spectrumc.getSignals(i, spectrumc.getDBMolecule()).size()>0)
              	  shift=spectrumc.getSignals(i, spectrumc.getDBMolecule()).get(0).getFirstShift().getValue();
                else if(cisfrom2d && !subDataC.getShift(i).equals(""))
                	shift=Double.parseDouble(subDataC.getShift(i));
                if(shift!=-1){
                    StringBuffer hoseCode = new StringBuffer();
                    StringBuffer commentpred = new StringBuffer();
                    double[] statistics = AtomUtils.predictRange(cdkmol, cdkmol.getAtom(i), true, true, true, false, spectrumc!=null ? spectrumc.getSpectrumId() : -1, commentpred, false, true, null, null, 6, true, hoseCode, true, null);
                    double difference = Math.abs(shift - statistics[1]);
                    statistics[3] = difference;
                    ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String(format.format(statistics[0]) + "-<b>" + format.format(statistics[1]) + "</b>-" + format.format(statistics[2])) + "; " + commentpred);
                    if(subDataC.getIdentifiers().get(i)!=null)
                        vfvb.setIdentifier(subDataC.getIdentifiers().get(i));
                    else
                        vfvb.setIdentifier(Integer.toString(i+1));
                    vfvb.statistics=statistics;
                    vfvb.setDelta(hoseCode.toString());
                    carbons.put(i,vfvb);
                }
            }
            if (cdkmol.getAtom(i).getSymbol().equals("H")) {
                double shift=-1;
                if(spectrumh.getSignals(i, spectrumh.getDBMolecule()).size()>0)
              	  shift=spectrumh.getSignals(i, spectrumh.getDBMolecule()).get(0).getFirstShift().getValue();
                if(shift!=-1){
                    StringBuffer hoseCode = new StringBuffer();
                    StringBuffer commentpred = new StringBuffer();
                    double[] statistics = AtomUtils.predictRange(cdkmol, cdkmol.getAtom(i), true, true, true, false, spectrumh.getSpectrumId(), commentpred, false, true, null, null, 6, true, hoseCode, true, null);
                    double difference = Math.abs(shift - statistics[1]);
                    statistics[3] = difference;
                    ValuesForVelocityBean vfvb = new ValuesForVelocityBean(new Float(statistics[1]), i + "", 0, 1000, i + "", new String(format.format(statistics[0]) + "-<b>" + format.format(statistics[1]) + "</b>-" + format.format(statistics[2])) + "; " + commentpred);
                    vfvb.statistics=statistics;
                    hydrogens.put(cdkmol.getAtomNumber(cdkmol.getConnectedAtomsList(cdkmol.getAtom(i)).get(0)),vfvb);              
                }
            }
        }
        long[] morgan=Canon.symmetry(cdkmol, GraphUtil.toAdjList(cdkmol));
        List<Element> footnotes=new ArrayList<Element>();
        //We build the c report
        double[] resultC=ReportUtil.start1d(servcon, "13","C", limits13c, subDataC, carbons, documentc, morgan, table1d, tableouterprediction, req, footnotes);
        int overallMarkC=(int)resultC[0];
        String reportc=SpectrumUtils.makeJudgement(overallMarkC,resultC[5],false);
        if(!cisfrom2d)
        	setIdentifiers(spectrumc, cdkmoloriginal, cdkmol);
        boolean footnotedone=ReportUtil.end1d(servcon, cdkmol, cdkmoloriginal, reportc, tablegreenred, cisfrom2d);
        //We build the h report
		double[] resultH=ReportUtil.start1d(servcon, "1", "H", limits1h, subDataH, hydrogens, documenth, morgan, table1d, tableouterprediction, req, footnotes);
        int overallMarkH=(int)resultH[0];
        String reporth=SpectrumUtils.makeJudgement(overallMarkH,resultH[5],false);
        documentc.close();
        documenth.close();
        setIdentifiers(spectrumh, cdkmoloriginal, cdkmol);
        footnotedone = footnotedone | ReportUtil.end1d(servcon, cdkmol, cdkmoloriginal, reporth, tablegreenred, false);
        reportpdf.add(table1d);
		  //Build 2d report
        Document document2d = new Document();
        OutputStream baos2d = new FileOutputStream(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/tmp/qc2d"+session.getId()+".pdf")));
        PdfWriter.getInstance(document2d, baos2d);
        document2d.open();
        double[] result2d=SpectrumUtils.calculateScore2d(subDataH.getMolWithH(), subDataC, subDataH, cosypeaks, tocsypeaks, noesypeaks, hmbcpeaks, hsqcpeaks, document2d, servcon, false, true);
        document2d.close();
        if(footnotedone) {
  		  	Chunk c = new Chunk("4");
  		  	c.setTextRise(7);
  		  	c.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
            Chunk c2 = new Chunk(" Gray atoms not used");
            Paragraph p = new Paragraph();
            p.add(c);
            p.add(c2);
            footnotes.add(p);
        }
        ReportUtil.endReport(servcon, reportpdf, mol.getChemicalNamesAsOneString(false, false),  temperature2, types2, frequency2, comment2, tableouterprediction, tablegreenred, table2d, (int)result2d[0], footnotes);
    }

	/**
	 * Helper method to set the identifiers in atomContainers from the database.
	 * 
	 * @param spectrum
	 * @param cdkmoloriginal
	 * @param cdkmol
	 * @throws TorqueException
	 */
	private static void setIdentifiers(DBSpectrum spectrum, IAtomContainer cdkmoloriginal, IAtomContainer cdkmol) throws TorqueException {
        for(IAtom atom : cdkmoloriginal.atoms()) {
            boolean hasshift=false;
            if(spectrum!=null){
                if(spectrum.getDBSpectrumType().getDBIsotopes().size()>0 && !spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0){
                    hasshift=true;
                }
                if(spectrum.getDBSpectrumType().getDimensionality()==2 && spectrum.getDBSpectrumType().getDBIsotopes().size()>1 && !spectrum.getDBSpectrumType().getDBIsotope(2).getElementSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0){
                    hasshift=true;
                }
                if(spectrum.getDBSpectrumType().getDBIsotopes().size()>0 && spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") || (spectrum.getDBSpectrumType().getDimensionality()==2 && spectrum.getDBSpectrumType().getDBIsotopes().size()>1 && spectrum.getDBSpectrumType().getDBIsotope(2).getElementSymbol().equals("H"))){
                    for(IAtom atomlocal : cdkmol.getConnectedAtomsList(cdkmol.getAtom(cdkmol.getAtomNumber(atom)))){
                        if(spectrum.getSignals(cdkmol.getAtomNumber(atomlocal), spectrum.getDBMolecule()).size()>0)
                            hasshift=true;
                    }
                    if(atom.getSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0)
                        hasshift=true;
                }
            }
            if(spectrum!=null && (spectrum.getDBSpectrumType().getDimensionality()==1 || spectrum.getDBSpectrumType().getDimensionality()==2) && spectrum.getMoleculeId()!=-1 && hasshift && (atom.getSymbol().equals(spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol()) || (spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && AtomContainerManipulator.countExplicitHydrogens(cdkmol, cdkmol.getAtom(cdkmol.getAtomNumber(atom)))>0))){
                if(spectrum.getDBMolecule().getAtomAt(cdkmol.getAtomNumber(atom)).getIdentifier(spectrum).equals(""))
                    atom.setProperty(BasicAtomGenerator.IDENTIFIER, ""+(cdkmol.getAtomNumber(atom)+1));
                else
                    atom.setProperty(BasicAtomGenerator.IDENTIFIER, spectrum.getDBMolecule().getAtomAt(cdkmol.getAtomNumber(atom)).getIdentifier(spectrum));
            }else{
                atom.setProperty(BasicAtomGenerator.IDENTIFIER, "");
            }
        }
	}


    /**
     * Adds a row to a 1d report table, generating color etc.
     * 
     * @param molWithH
     * @param atomnumber
     * @param heavyatomnumber
     * @param subData
     * @param predictions
     * @param alreadyUsedForScore
     * @param morgan
     * @param marks
     * @param sumDiffC
     * @param format
     * @param footnote1
     * @param footnote2
     * @param limits
     * @param redAtoms
     * @param greenAtoms
     * @param yellowAtoms
     * @param servcon
     * @param identifier
     * @param baseUrl
     * @param rows
     * @param alltable
     * @param alltable2
     * @param rowsfull
     * @param rowsfull2
     * @param assignedshift
     * @param assignedshiftcalc
     * @param averageused
     * @param bs
     * @return
     */
    public static double addToReport(IAtomContainer molWithH, int atomnumber, int heavyatomnumber, SubmittingData subData, Map<Integer, ValuesForVelocityBean> predictions, List<Long> alreadyUsedForScore, long[] morgan, int[] marks, double sumDiffC, DecimalFormat format, boolean[] footnotesused, double[] limits, List<Integer> redAtoms, List<Integer> greenAtoms, List<Integer> yellowAtoms, List<Integer> greyAtoms, ServletConfig servcon, String identifier, String baseUrl, Map<String, List<PdfPCell>> rows, PdfPTable alltable, PdfPTable alltable2, Map<String,List<PdfPCell>> rowsfull, Map<String,List<PdfPCell>> rowsfull2, double assignedshift, double assignedshiftcalc, boolean averageused, BaseFont bs) {
        try{
            double[] predicted;
            StringBuffer hose=new StringBuffer();
            if(predictions!=null){
                predicted = predictions.get(heavyatomnumber).statistics;
                hose = new StringBuffer(predictions.get(heavyatomnumber).getDelta());
            }else{
                predicted = PredictionTool.generalPredict(molWithH, molWithH.getAtom(atomnumber), true, true, true, false, -1,-1,new StringBuffer(),true,true,null,new HashMap<Double,double[]>(),-1,true,hose,6,true,true,null);
            }
            if(predicted!=null && !alreadyUsedForScore.contains(morgan[heavyatomnumber])){
          	  alreadyUsedForScore.add(morgan[heavyatomnumber]);
          	  marks[7]++;
                double diff = Math.abs(assignedshiftcalc-predicted[1]);
                marks[0]++;
                sumDiffC+=diff;
                Font font = new Font();
                boolean unreliable=false;
                Phrase diffPhrase=new Phrase();
                String difftext=format.format(diff);
                Chunk diffchunk=new Chunk(difftext);
                diffchunk.setFont(font);
                diffPhrase.add(diffchunk);
                if(averageused) {
                	Chunk chunk=new Chunk("3");
                	chunk.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
                	chunk.setTextRise(7);
                	diffPhrase.add(chunk);
                	footnotesused[2]=true;
                }
                if(predicted[4]<3 || Math.abs(predicted[7]-predicted[8])>10){
              	  Image image;
                    if(predicted[4]<3)
                    	footnotesused[0]=true;
                    if(Math.abs(predicted[7]-predicted[8])>10)
                    	footnotesused[1]=true;
                    if(predicted[4]<3 && Math.abs(predicted[7]-predicted[8])>10){
                    	if(servcon==null)
                    		image=Image.getInstance(new File("src/html/warningstrong.png").toURI().toURL().toString());
                    	else
                    		image=Image.getInstance(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/warningstrong.png")).toURI().toURL().toString());
                    }else {
                    	if(servcon==null)
                    		image=Image.getInstance(new File("src/html/warning.png").toURI().toURL().toString());
                    	else
                    		image=Image.getInstance(new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/warning.png")).toURI().toURL().toString());
                    }
                    diffPhrase.add(new Chunk(image,0,0));
                    String footnotes="";
                    if(predicted[4]<3)
                  	  footnotes="1";
                    if(predicted[4]<3 && Math.abs(predicted[7]-predicted[8])>10)
                  	  footnotes=footnotes+", ";
                    if(Math.abs(predicted[7]-predicted[8])>10)
                  	  footnotes=footnotes+"2";
                    Chunk chunk=new Chunk(footnotes);
                    chunk.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
                    chunk.setTextRise(7);
                    diffPhrase.add(chunk);
                	  unreliable=true;
                }
                if(diff>limits[0]){
                    if(!unreliable){
                    	marks[1]+=10;
                      	marks[2]++;
                        font.setColor(Color.RED);
                        if(redAtoms!=null)
                      	  	redAtoms.add(atomnumber);
                    }else{
                    	marks[4]++;
                        font.setColor(Color.GRAY);
                        if(greyAtoms!=null)
                      	  	greyAtoms.add(atomnumber);
                    }
                }else if(diff>limits[1]){
              	  if(!unreliable){
              		  marks[1]+=5;
              		  marks[3]++;
                      font.setColor(Color.ORANGE);
                      if(yellowAtoms!=null)
                      	  yellowAtoms.add(atomnumber);
              	  }else{
              		  marks[5]++;
              		  font.setColor(Color.GRAY);
                      if(greyAtoms!=null)
                    	  	greyAtoms.add(atomnumber);
              	  }
                }else{
                    if(unreliable) {
                    	marks[6]++;
                    	font.setColor(Color.GRAY);
                    	if(greyAtoms!=null)
                  	  		greyAtoms.add(atomnumber);
                    }else {
                        if(greenAtoms!=null)
                        	  greenAtoms.add(atomnumber);
                          font.setColor(Color.GREEN);
                    }
                }
                PdfPCell cell1 = new PdfPCell(new Paragraph(identifier));
                cell1.setBorder(PdfPCell.RIGHT );
                PdfPCell cell2 = new PdfPCell(new Paragraph(Double.toString(assignedshift),font));
                cell2.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                diffPhrase.setFont(font);
                PdfPCell cell3 = new PdfPCell(diffPhrase);
                cell3.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                PdfPCell cell4 = new PdfPCell(new Paragraph(Double.toString(predicted[4])));
                cell4.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                PdfPCell cell5 = new PdfPCell(new Paragraph(Double.toString(predicted[5])));
                cell5.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                Chunk hosechunk;
                if(predicted[10]==1)
              	  hosechunk=new Chunk("3D");
                else
              	  hosechunk=new Chunk("2D");
                hosechunk.setUnderline(1.5f, -1f);
                Font fontblue = new Font();
                fontblue.setColor(Color.BLUE);
                Phrase hosepara=new Phrase(hosechunk);
                hosepara.setFont(fontblue);
                hosechunk.setAction(PdfAction.gotoRemotePage(baseUrl+"/js_pane/P-Results?eventSubmit_doGeneralsearch=generalsearch&searchstring="+hose.toString()+"&searchaction=exact&searchfield=HOSE%20code&spectype=1&solvent=-1", "hose", false, true));
                PdfPCell cell6 = new PdfPCell(hosepara);
                cell6.setBorder(PdfPCell.LEFT);
                List<PdfPCell> row=new ArrayList<PdfPCell>();
                row.add(cell1);
                row.add(cell2);
                row.add(cell3);
                row.add(cell4);
                row.add(cell5);
                row.add(cell6);
                String identifierlocal=new String(identifier);
                while(rows.containsKey(identifierlocal)){
              	  identifierlocal=identifierlocal+"'";
                }
          	    rows.put(identifierlocal, row);
                //This is for report all
                if(alltable!=null){
              	  List<PdfPCell> rowall = new ArrayList<>();
              	  List<PdfPCell> rowall2 = new ArrayList<>();
              	  Font bold = new Font(bs,12,Font.BOLD);
              	  Chunk c=new Chunk(identifier);
              	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
              	  c.setFont(bold);
              	  PdfPCell cell=new PdfPCell(new Paragraph(c));
              	  rowall.add(cell);
              	  double shiftvalue=assignedshift;
              	  cell=new PdfPCell(new Paragraph(Double.toString(shiftvalue)));
              	  rowall.add(cell);
              	  cell=new PdfPCell(new Paragraph(subData.getMultiplicity(atomnumber)));
              	  rowall.add(cell);
              	  cell=new PdfPCell(new Paragraph(""));//TODO coupling
              	  rowall.add(cell);
              	  //we do the prediction table
              	  c=new Chunk(identifier);
              	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
              	  c.setFont(bold);
              	  cell=new PdfPCell(new Paragraph(c));
              	  rowall2.add(cell);
              	  cell=new PdfPCell(new Paragraph(Double.toString(shiftvalue)));
              	  rowall2.add(cell);
              	  cell=new PdfPCell(new Paragraph(diffPhrase));
              	  rowall2.add(cell);
              	  rowsfull.put(identifier,rowall);
              	  rowsfull2.put(identifier,rowall2);
                }
            }else if(predicted!=null){
          	  Font font = new Font();
          	  font.setColor(Color.BLACK);
          	  //Symmetric to which atom?
          	  int symmetricatom=0;
          	  for(int k=0;k<morgan.length;k++){
          		  if(k!=heavyatomnumber && morgan[k]==morgan[heavyatomnumber]){
          			  symmetricatom=k;
          			  break;
          		  }
          	  }
          	  Paragraph paragraph = new Paragraph("Symmetric to atom "+(subData.getIdentifiers().get(symmetricatom)!=null ? subData.getIdentifiers().get(symmetricatom) : symmetricatom+1), font);
          	  PdfPCell cell1 = new PdfPCell(new Paragraph(identifier));
                cell1.setBorder(PdfPCell.RIGHT);
                PdfPCell cell2 = new PdfPCell(new Paragraph(Double.toString(assignedshift),font));
                cell2.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                PdfPCell cell3 = new PdfPCell(paragraph);
                cell3.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                PdfPCell cell4 = new PdfPCell(new Paragraph(Double.toString(predicted[4])));
                cell4.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                PdfPCell cell5 = new PdfPCell(new Paragraph(Double.toString(predicted[5])));
                cell5.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
                Chunk hosechunk;
                if(predicted[10]==1)
              	  hosechunk=new Chunk("3D");
                else
              	  hosechunk=new Chunk("2D");
                hosechunk.setUnderline(1.5f, -1f);
          	  Font fontblue = new Font();
                fontblue.setColor(Color.BLUE);
                Phrase hosepara=new Phrase(hosechunk);
                hosepara.setFont(fontblue);
                hosechunk.setAction(PdfAction.gotoRemotePage(baseUrl+"/js_pane/P-Results;jsessionid=AF1A3B4AAF66EFC63EB6C95DA7412BF6?eventSubmit_doGeneralsearch=generalsearch&searchstring="+hose.toString()+"&searchaction=exact&searchfield=HOSE%20code&spectype=1&solvent=-1", "hose", false, true));
                PdfPCell cell6 = new PdfPCell(hosepara);
                cell6.setBorder(PdfPCell.LEFT);
                List<PdfPCell> row=new ArrayList<PdfPCell>();
                row.add(cell1);
                row.add(cell2);
                row.add(cell3);
                row.add(cell4);
                row.add(cell5);
                row.add(cell6);
                String identifierlocal=new String(identifier);
                while(rows.containsKey(identifierlocal)){
              	  identifierlocal=identifierlocal+"'";
                }
          	  rows.put(identifierlocal, row);
                //This is for report all
                if(alltable!=null){
              	  List<PdfPCell> rowall = new ArrayList<>();
              	  List<PdfPCell> rowall2 = new ArrayList<>();
              	  Font bold = new Font(bs,12,Font.BOLD);
              	  Chunk c=new Chunk(identifier);
              	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
              	  c.setFont(bold);
              	  PdfPCell cell=new PdfPCell(new Paragraph(c));
              	  rowall.add(cell);
              	  double shiftvalue=assignedshift;
              	  cell=new PdfPCell(new Paragraph(Double.toString(shiftvalue)));
              	  rowall.add(cell);
              	  cell=new PdfPCell(new Paragraph(subData.getMultiplicity(atomnumber)));
              	  rowall.add(cell);
              	  cell=new PdfPCell(new Paragraph(""));//TODO coupling
              	  rowall.add(cell);
              	  //we do the prediction table
              	  c=new Chunk(identifier);
              	  c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
              	  c.setFont(bold);
              	  cell=new PdfPCell(new Paragraph(c));
              	  rowall2.add(cell);
              	  cell=new PdfPCell(new Paragraph(Double.toString(shiftvalue)));
              	  rowall2.add(cell);
              	  c=new Chunk("Symmetric to atom "+(subData.getIdentifiers().get(symmetricatom)!=null ? subData.getIdentifiers().get(symmetricatom) : symmetricatom+1), font);
              	  c.setFont(font);
              	  cell=new PdfPCell(new Paragraph(c));
              	  rowall2.add(cell);
              	  rowsfull.put(identifier,rowall);
              	  rowsfull2.put(identifier,rowall2);
                }
            }
            return sumDiffC;
        }catch(Exception ex){
        	PdfPCell cell1 = new PdfPCell(new Paragraph(identifier));
            cell1.setBorder(PdfPCell.RIGHT);
            PdfPCell cell2 = new PdfPCell(new Paragraph(Double.toString(assignedshift)));
            cell2.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
            PdfPCell cell3 = new PdfPCell(new Paragraph("Not possible"));
            cell3.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
            PdfPCell cell4 = new PdfPCell(new Paragraph("-"));
            cell4.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
            PdfPCell cell5 = new PdfPCell(new Paragraph("-"));
            cell5.setBorder(PdfPCell.RIGHT | PdfPCell.LEFT);
            PdfPCell cell6 = new PdfPCell(new Paragraph("-"));
            cell6.setBorder(PdfPCell.LEFT);
            List<PdfPCell> row=new ArrayList<PdfPCell>();
            row.add(cell1);
            row.add(cell2);
            row.add(cell3);
            row.add(cell4);
            row.add(cell5);
            row.add(cell6);
            String identifierlocal=new String(identifier);
            while(rows.containsKey(identifierlocal)){
          	  identifierlocal=identifierlocal+"'";
            }
      	  rows.put(identifierlocal, row);
      	  return sumDiffC;
        }
	}


	/**
	 * This is one of the steps in creating a full report. See createFullReport for an example.
	 * 
	 * @param servcon
	 * @param cdkmol
	 * @param cdkmoloriginal
	 * @param resultstring
	 * @param tablegreenred
	 * @throws Exception
	 */
	public static boolean end1d(ServletConfig servcon, IAtomContainer cdkmol, IAtomContainer cdkmoloriginal, String resultstring, PdfPTable tablegreenred, boolean from2d) throws Exception {
        BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontSym = new Font(bs,12);
        List<IAtom> greenAtoms=new ArrayList<>();
        for(int i : greenAtomsInt){
          if(i<cdkmol.getAtomCount()) {
          	  IAtom atom = cdkmol.getAtom(i);
          	  if(atom.getSymbol().equals("H"))
          		  atom=cdkmol.getConnectedAtomsList(atom).get(0);
          	  greenAtoms.add(cdkmoloriginal.getAtom(cdkmol.getAtomNumber(atom)));
          }
        }
        List<IAtom> orangeAtoms=new ArrayList<>();
        for(int i : orangeAtomsInt){
          if(i<cdkmol.getAtomCount()) {
          	  IAtom atom = cdkmol.getAtom(i);
          	  if(atom.getSymbol().equals("H"))
          		  atom=cdkmol.getConnectedAtomsList(atom).get(0);
          	  orangeAtoms.add(cdkmoloriginal.getAtom(cdkmol.getAtomNumber(atom)));
          }
        }
        List<IAtom> redAtoms=new ArrayList<>();
        for(int i : redAtomsInt){
          if(i<cdkmol.getAtomCount()) {
          	  IAtom atom = cdkmol.getAtom(i);
          	  if(atom.getSymbol().equals("H"))
          		  atom=cdkmol.getConnectedAtomsList(atom).get(0);
          	  redAtoms.add(cdkmoloriginal.getAtom(cdkmol.getAtomNumber(atom)));
          }
        }
        List<IAtom> grayAtoms=new ArrayList<>();
        for(int i : grayAtomsInt){
          if(i<cdkmol.getAtomCount()) {
          	  IAtom atom = cdkmol.getAtom(i);
          	  if(atom.getSymbol().equals("H"))
          		  atom=cdkmol.getConnectedAtomsList(atom).get(0);
          	grayAtoms.add(cdkmoloriginal.getAtom(cdkmol.getAtomNumber(atom)));
          }
        }		
        Export export = new Export(cdkmoloriginal, cdkmol);
		String relativepath = ServletUtils.expandRelative(servcon,"/WEB-INF");
		String image = export.getImage(true,"png",relativepath + "/tmp/" + System.currentTimeMillis(),300,300,true,null,null,null,Color.BLUE, greenAtoms, orangeAtoms, redAtoms, grayAtoms, null);
		Image image1 = Image.getInstance(image);
		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		cell.addElement(image1);
		int mark=Integer.parseInt(resultstring.toString().substring(resultstring.toString().indexOf(": ")+2, resultstring.toString().indexOf(": ")+3));
		if(resultstring.toString().substring(resultstring.toString().indexOf(": ")+2, resultstring.toString().indexOf(": ")+4).equalsIgnoreCase("10"))
			mark=10;
		if(mark==1)
			fontSym.setColor(Color.RED);
		else if(mark<=5)
			fontSym.setColor(Color.ORANGE);
		else
			fontSym.setColor(Color.GREEN);
		Paragraph p=new Paragraph(resultstring.toString(), fontSym);
		if(grayAtoms.size()>0) {
        	Chunk chunk=new Chunk("4");
        	chunk.setFont(FontFactory.getFont(FontFactory.HELVETICA, 8));
        	chunk.setTextRise(7);
        	p.add(chunk);
		}
		cell.addElement(p);
		if(from2d)
			cell.addElement(new Paragraph("Shifts were taken from 2D spectra", fontSym));
		tablegreenred.addCell(cell);
		return grayAtoms.size()>0;
	}

	/**
	 * This is one of the steps in creating a full report. See createFullReport for an example.
	 * 
	 * @param servcon
	 * @param reportpdf
	 * @param name
	 * @param temperature
	 * @param types
	 * @param frequency
	 * @param comment
	 * @param tableouterprediction
	 * @param tablegreenred
	 * @param table2d
	 * @param result2d
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void endReport(ServletConfig servcon, Document reportpdf, String name, List<String> temperature, List<String> types, List<String> frequency, List<String> comment, PdfPTable tableouterprediction, PdfPTable tablegreenred, PdfPTable table2d, int result2d, List<Element> footnotes) throws DocumentException, IOException {
        reportpdf.newPage();
        BaseFont bs = BaseFont.createFont(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/fonts/FreeSans.ttf"), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontSym = new Font(bs,18);
        Chunk c = new Chunk("Structure Elucidation Report mrshiftdb2 - QuickCheck results", fontSym);
        c.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLUE);
        Paragraph p = new Paragraph();
        p.add(c);
        reportpdf.add(p);
        fontSym = new Font(bs,12);
        Chunk c2 = new Chunk("<name>", fontSym);
        if(name!=null)
        	c2 = new Chunk(name, fontSym);
        c2.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, Color.BLACK);
        Paragraph p2 = new Paragraph();
        p2.add(c2);
        Chunk glue = new Chunk(new VerticalPositionMark());
        p2.add(glue);
        p2.add(new Chunk(new Date().toString()));
        reportpdf.add(p2);
        reportpdf.add(tableouterprediction);
        reportpdf.add(tablegreenred);
        reportpdf.add(table2d);
        if(result2d!=-1){
        	if(result2d==1){
  	  			fontSym.setColor(Color.RED);
  	  		}else if(result2d<=5){
  	  			fontSym.setColor(Color.ORANGE);
  	  		}else {
  	  			fontSym.setColor(Color.GREEN);
  	  		}
            reportpdf.add(new Paragraph("Overall mark 2D: "+result2d,fontSym));
        }
        if(types!=null) {
        	reportpdf.add(new Paragraph("\n\n"));
            Font bold = new Font(bs,12,Font.BOLD);
        	c = new Chunk("Other spectra", bold);
            c.setUnderline(0.1f, -2f);
            p = new Paragraph();
            p.add(c);
            reportpdf.add(p);   
            reportpdf.add(new Paragraph("\n"));
            PdfPTable table = new PdfPTable(6);
	        table.setWidths(new float[]{1, 1, 1, 1, 1, 2});
	        PdfPCell cell=new PdfPCell(new Paragraph("Experiment Type"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        cell=new PdfPCell(new Paragraph("v [Mhz]"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        cell=new PdfPCell(new Paragraph("Offset [ppm]"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        cell=new PdfPCell(new Paragraph("Resolution [Hz]"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        cell=new PdfPCell(new Paragraph("Temperature [K]"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        cell=new PdfPCell(new Paragraph("Comment"));
	        cell.setBackgroundColor(new Color(53,151,255));
	        table.addCell(cell);
	        int l=0;
	        for(String type : types){
	      		c=new Chunk(type);
	            cell=new PdfPCell(new Paragraph(c));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            cell=new PdfPCell(new Paragraph(frequency.get(l)));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            cell=new PdfPCell(new Paragraph(""));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            cell=new PdfPCell(new Paragraph(""));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            cell=new PdfPCell(new Paragraph(temperature.get(l)));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            cell=new PdfPCell(new Paragraph(comment.get(l)));
	            if(l%2==0)
	          	  cell.setBackgroundColor(Color.LIGHT_GRAY);
	            table.addCell(cell);
	            l++;
	        }          
	        reportpdf.add(table);
        }
        reportpdf.add(new Paragraph("\n\n"));
        for(Element element : footnotes)
        	reportpdf.add(element);
	}
}
