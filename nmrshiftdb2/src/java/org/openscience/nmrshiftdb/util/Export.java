/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012-2014 Stefan Kuhn
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JPanel;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.torque.TorqueException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.libio.cml.Convertor;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.RendererModel.ColorHash;
import org.openscience.cdk.renderer.RendererModel.ExternalHighlightColor;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator.BondDistance;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.CouplingGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumModel;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumNavigation;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xmlcml.cml.element.CMLPeakList;

import com.workingdogs.village.DataSetException;

import graph.Axis;
import graph.DataSet;
import graph.Graph2D;
import graph.Markers;
import nu.xom.Element;

/**
 *  This class converts spectra/molecules to various other formats
 *
 * @author     shk3
 * @created    5. August 2002
 */
public class Export{

  /**
   *  This are the image files used for making pdf
   */
  public String[] pictures = new String[2];
  public Color backColor=Color.white;
  private DBSpectrum spectrum;
  public IAtomContainer cdkmol;
  private IAtomContainer cdkmolwithh;
  private boolean useCSS = false;
  public int selected=-1;
  private boolean compact = false;
  


  public void setCompact(boolean compact) {
	this.compact = compact;
  }


  /**
   *Constructor for the Export object
   *
   * @param  spectrum       The spectrum to export
   * @throws DataSetException 
   * @throws IOException 
   * @throws TorqueException 
   * @throws OptionalDataException 
   */
  public Export(DBSpectrum spectrum) throws OptionalDataException, TorqueException, IOException, DataSetException {
    this.spectrum = spectrum;
    if(spectrum.getDBMolecule()!=null){
    	cdkmol = spectrum.getDBMolecule().getAsCDKMoleculeAsEntered(1,false);
    	cdkmolwithh = spectrum.getDBMolecule().getAsCDKMolecule(1);
    	setIDs();
    }
  }


  /**
   *Constructor for the Export object
   *
   * @param  molecle        The molecule to export
   * @throws DataSetException 
   * @throws IOException 
   * @throws TorqueException 
   * @throws OptionalDataException 
   */
  public Export(DBMolecule molecule) throws OptionalDataException, TorqueException, IOException, DataSetException {
    cdkmol = molecule.getAsCDKMoleculeAsEntered(1,false);
    cdkmolwithh = molecule.getAsCDKMolecule(1);
    setIDs();
  }


  /**
   *Constructor for the Export object
   *
   * @param cdkmol			The molecule to export.
   * @param cdkmolwithh		The molcule to export with all Hs explicit.
   * @param  spectrum       The spectrum to export
   */
  public Export(IAtomContainer cdkmol, IAtomContainer cdkmolwithh, DBSpectrum spectrum) {
	this.spectrum = spectrum;
	this.cdkmol = cdkmol;
	this.cdkmolwithh = cdkmolwithh;
	setIDs();
  }
  
  /**
   * Constructor for the Export object.
   * 
   * @param cdkmol			The molecule to export.
   * @param cdkmolwithh		The molcule to export with all Hs explicit.
   */
  public Export(IAtomContainer cdkmol, IAtomContainer cdkmolwithh) {
	this.cdkmol = cdkmol;
	this.cdkmolwithh = cdkmolwithh;
	setIDs();
  }

  private void setIDs() {
	int i=1;
	for(IAtom atom : cdkmol.atoms()) {
		if(atom.getID()==null)
			atom.setID("a"+i);
		i++;
	}
	i=1;
	for(IAtom atom : cdkmolwithh.atoms()) {
		if(atom.getID()==null)
			atom.setID("a"+i);
		i++;
	}
}


/**
   *  Gets the molecule/spectrum as a simple html file
   *
   * @param request The current servlet request.
   * @param subData If this is not null, it will be used for export instead of the spectrum/molecule object in the class.
   * @return    The html value
   * @exception  Exception  Database problems
   */
  public String getHtml(HttpServletRequest request, SubmittingData subData) throws Exception {
    VelocityContext context = new VelocityContext();
    context.put("spectrum", spectrum);
    context.put("atoms", subData!=null ? subData.getConfirmShifts() : spectrum.getOptions(1,false));
    context.put("moleculepicture", pictures[0]);
    context.put("spectrumpicture", pictures[1]);
    context.put("cas", subData!=null ? subData.getCasNumber() : spectrum.getDBMolecule().getCasNumber());
    context.put("formula", subData!=null ? subData.getMolecularFormula(true) : spectrum.getDBMolecule().getMolecularFormula(true));
    context.put("cas", subData!=null ? subData.getCasNumber() : spectrum.getDBMolecule().getCasNumber());
    context.put("canname", subData!=null ? subData.getAutonomeName() : spectrum.getDBMolecule().getCanonicalNameNormal());
    context.put("names", subData!=null ? subData.getChemNameWithSemicolon() : spectrum.getDBMolecule().getChemicalNamesAsOneString(true, true));
    UrlTool urltool = new UrlTool();
	context.put("baseurl", urltool.getBaseUrl(request));
    //lets render the template
    StringWriter w = new StringWriter();
    Velocity.mergeTemplate("exporthtml.vm", "ISO-8859-1", context, w);
    w.flush();
    return (w.toString());
  }


  /**
   *  Gets the spectrum and molecule as docbook
   *
   * @param  format         rtf=no nested tables, pdf=nested tables
   * @param  formatPic      Description of Parameter
   * @param subData If this is not null, it will be used for export instead of the spectrum/molecule object in the class.
   * @return                The docbook document
   * @exception  Exception  Description of Exception
   */
  public String getDocbook(String format, String formatPic, SubmittingData subData) throws Exception {
    VelocityContext context = new VelocityContext();
    context.put("spectrum", spectrum);
    context.put("atoms", subData!=null ? subData.getConfirmShifts() : spectrum.getOptions(1,false));
    context.put("moleculepicture", pictures[0]);
    context.put("spectrumpicture", pictures[1]);
    context.put("format", format);
    context.put("formatpic", formatPic);
    context.put("chemnames", subData!=null ? subData.getChemNameWithSemicolon() : spectrum.getDBMolecule().getChemicalNamesAsOneString(true, true));
    context.put("canname", subData!=null ? subData.getAutonomeName() : spectrum.getDBMolecule().getCanonicalNameNormal());
    context.put("cas", subData!=null ? subData.getCasNumber() : spectrum.getDBMolecule().getCasNumber());
    context.put("formula", subData!=null ? subData.getMolecularFormula(false) : spectrum.getDBMolecule().getMolecularFormula(false));
    //lets render the template
    StringWriter w = new StringWriter();
    Velocity.mergeTemplate("export.vm", "ISO-8859-1", context, w);
    w.flush();
    return (w.toString());
  }


  /**
   *  Gets the molecule as an svg, with and height set to a reasonable value for the molecule.
   *
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The molecule as svg.
   * @exception  Exception  Description of Exception.
   */
  public String getMolSvg(boolean drawNumbers) throws Exception {
	  return getMolSvg(-1, -1,drawNumbers, null, null, null, null, null, null, null);
  }
  
  
  /**
   *  Gets the molecule as an svg
   *
   * @param      width      The width of the svg
   * @param      height     The height of the svg
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The molecule as svg
   * @throws TorqueException 
   * @throws IOException 
   * @throws DataSetException 
   * @throws OptionalDataException 
   * @throws NmrshiftdbException 
   * @exception  Exception  Description of Exception
   */
  public String getMolSvg(int width, int height, boolean drawNumbers, Color arccolor, List<IAtom> greenAtoms, List<IAtom> orangeAtoms, List<IAtom> redAtoms, List<IAtom> grayAtoms, List<List<Pair<Integer, Integer>>> result, Collection<List<IAtom>> atomsets) throws TorqueException, OptionalDataException, DataSetException, IOException, NmrshiftdbException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
    generators.add(new BasicBondGenerator());
    generators.add(new BasicAtomGenerator());
    generators.add(new CouplingGenerator());
    //generators.add(new ExtendedAtomGenerator());
    //generators.add(new AtomNumberGenerator());
    generators.add(new BasicSceneGenerator());
    AtomContainerRenderer renderer = new AtomContainerRenderer(generators,new AWTFontManager());
    RendererModel r2dm = renderer.getRenderer2DModel();
    r2dm.registerParameters(new AtomNumberGenerator());
    //r2dm.set( WillDrawAtomNumbers.class, drawNumbers);
    r2dm.set(BasicSceneGenerator.BackgroundColor.class,backColor);
    r2dm.set(CompactAtom.class,compact);
    //r2dm.set(ShowImplicitHydrogens.class,false);
    //r2dm.set(ShowEndCarbons.class,false);
    if(selected>-1){
      r2dm.set(ExternalHighlightColor.class,Color.RED);
      IAtomContainer ac=cdkmol.getBuilder().newInstance(IAtomContainer.class);
      if(selected>=cdkmol.getAtomCount()){
    	  selected=cdkmolwithh.getAtomNumber((IAtom)cdkmolwithh.getConnectedAtomsList(cdkmolwithh.getAtom(selected)).get(0));
      }
      ac.addAtom(cdkmol.getAtom(selected));
      r2dm.setExternalSelectedPart(ac);
    }
    Map<IChemObject, Color> colorHash = r2dm.getParameter(ColorHash.class).getValue();
    colorHash.clear();
    IAtomContainer hightlightcontainer=cdkmol.getBuilder().newAtomContainer(); 
    if (greenAtoms!=null) {
    	for(IAtom atom : greenAtoms){
            colorHash.put(atom, Color.GREEN);
        	hightlightcontainer.addAtom(atom);
        }
    }
    if (orangeAtoms!=null) {
    	for(IAtom atom : orangeAtoms){
            colorHash.put(atom, Color.ORANGE);
        	hightlightcontainer.addAtom(atom);
        }
    }
    if (redAtoms!=null) {
    	for(IAtom atom : redAtoms){
            colorHash.put(atom, Color.RED);
        	hightlightcontainer.addAtom(atom);
        }
    }
    if (grayAtoms!=null) {
    	for(IAtom atom : grayAtoms){
            colorHash.put(atom, Color.GRAY);
        	hightlightcontainer.addAtom(atom);
        }
    }
    if(hightlightcontainer.getAtomCount()>0){
    	for(IAtom atom  : hightlightcontainer.atoms()){
    		if(atom.getSymbol().equals("H") && atom.getProperty(SubmittingData.VISIBLE)!=null && atom.getProperty(SubmittingData.VISIBLE).equals(NmrshiftdbConstants.FALSE)){
    			hightlightcontainer.addAtom(spectrum.getDBMolecule().getAsCDKMolecule(1).getConnectedAtomsList(atom).get(0));
    			colorHash.put(spectrum.getDBMolecule().getAsCDKMolecule(1).getConnectedAtomsList(atom).get(0), colorHash.get(atom));
    		}
    	}
    	r2dm.setExternalSelectedPart(hightlightcontainer);
    }
    if(atomsets!=null) {
    	int i=0;
    	for(List<IAtom> atomlist : atomsets) {
    		Color color=KELLY_COLORS[i];
    		for(IAtom atom : atomlist) {
    			colorHash.put(atom, color);
            		hightlightcontainer.addAtom(atom);
    		}    		
    		i++;	
    	}
    	r2dm.setExternalSelectedPart(hightlightcontainer);
    }
    r2dm.set(ColorHash.class, colorHash);
    for(IAtom atom : cdkmol.atoms()){
        if(atom.getProperty(BasicAtomGenerator.IDENTIFIER)==null){
            boolean hasshift=false;
            if(spectrum!=null){
                if(spectrum.getDBSpectrumType().getDBIsotopes().size()>0 && !spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0){
                    hasshift=true;
                }
                if(spectrum.getDBSpectrumType().getDimensionality()==2 && spectrum.getDBSpectrumType().getDBIsotopes().size()>1 && !spectrum.getDBSpectrumType().getDBIsotope(2).getElementSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0){
                    hasshift=true;
                }
                if(spectrum.getDBSpectrumType().getDBIsotopes().size()>0 && spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") || (spectrum.getDBSpectrumType().getDimensionality()==2 && spectrum.getDBSpectrumType().getDBIsotopes().size()>1 && spectrum.getDBSpectrumType().getDBIsotope(2).getElementSymbol().equals("H"))){
                    for(IAtom atomlocal : cdkmolwithh.getConnectedAtomsList(cdkmolwithh.getAtom(cdkmol.getAtomNumber(atom)))){
                        if(spectrum.getSignals(cdkmolwithh.getAtomNumber(atomlocal), spectrum.getDBMolecule()).size()>0)
                            hasshift=true;
                    }
                    if(atom.getSymbol().equals("H") && spectrum.getSignals(cdkmol.getAtomNumber(atom), spectrum.getDBMolecule()).size()>0)
                        hasshift=true;
                }
            }
            if(spectrum!=null && (spectrum.getDBSpectrumType().getDimensionality()==1 || spectrum.getDBSpectrumType().getDimensionality()==2) && spectrum.getMoleculeId()!=-1 && hasshift && (atom.getSymbol().equals(spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol()) || (spectrum.getDBSpectrumType().getDBIsotope(1).getElementSymbol().equals("H") && AtomContainerManipulator.countExplicitHydrogens(cdkmolwithh, cdkmolwithh.getAtom(cdkmol.getAtomNumber(atom)))>0))){
                if(spectrum.getDBMolecule().getAtomAt(cdkmol.getAtomNumber(atom)).getIdentifier(spectrum).equals(""))
                    atom.setProperty(BasicAtomGenerator.IDENTIFIER, ""+(cdkmol.getAtomNumber(atom)+1));
                else
                    atom.setProperty(BasicAtomGenerator.IDENTIFIER, spectrum.getDBMolecule().getAtomAt(cdkmol.getAtomNumber(atom)).getIdentifier(spectrum));
            }else if(drawNumbers){
                atom.setProperty(BasicAtomGenerator.IDENTIFIER, ""+(cdkmol.getAtomNumber(atom)+1));
            }else{
                atom.setProperty(BasicAtomGenerator.IDENTIFIER, "");
            }
        }
    }
    int number=((int)Math.sqrt(cdkmol.getAtomCount()))+1;
    int moleculewidth = number*100;
    int moleculeheight = number*100;
    if(width>-1){
	    moleculewidth=width;
	    moleculeheight=height;
    }
    if(moleculeheight<200 || moleculewidth<200){
      r2dm.set(CompactAtom.class, true);
      r2dm.set(BondDistance.class, 3.0);
    }
    if(result!=null){
    	Map<Color,List<IBond>> couplings=new HashMap<>();
    	for(List<Pair<Integer, Integer>> bondlist : result){
        	List<IBond> bonds=new ArrayList<IBond>();
    		if(couplings.size()==0)
    			couplings.put(Color.GREEN, bonds);
    		else if(couplings.size()==1)
    			couplings.put(Color.ORANGE, bonds);
    		else if(couplings.size()==2)
    			couplings.put(Color.RED, bonds);
    		for(Pair<Integer, Integer> bond : bondlist){
    			if(cdkmol.getAtomCount()>bond.getFirst() && cdkmol.getAtomCount()>bond.getSecond())
    				bonds.add(new Bond(cdkmol.getAtom(bond.getFirst()),cdkmol.getAtom(bond.getSecond())));
    		}
    	}
    	cdkmol.setProperty("couplings", couplings);
    }else if(spectrum!=null && spectrum.getDBSpectrumType().getDimensionality()==2 && arccolor!=null){
    	IAtomContainer ac = spectrum.getDBMolecule().getAsCDKMolecule(1);
    	Map<Color,List<IBond>> couplings=new HashMap<>();
        couplings.put(arccolor, new ArrayList<IBond>());
        Map<IAtom,List<IAtom>> alreadyDone=new HashMap<IAtom,List<IAtom>>();
    	for(DBSignal signal : spectrum.getDBSignals()){
    		IAtom atom1 = ac.getAtom(signal.getAtoms().get(0).getMdlNumber());
    		IAtom atom1orig=atom1;
    		IAtom atom2 = ac.getAtom(signal.getAtoms().get(1).getMdlNumber());
			IAtom atom2orig = atom2;
			if(!atom1.getProperty(SubmittingData.VISIBLE).equals("true")){
				atom1=ac.getConnectedAtomsList(atom1).get(0);
			}			
			if(!atom2.getProperty(SubmittingData.VISIBLE).equals("true")){
				atom2=ac.getConnectedAtomsList(atom2).get(0);
			}
    		if(alreadyDone.get(atom2)==null || !alreadyDone.get(atom2).contains(atom1)){
				ShortestPaths sp = new ShortestPaths(ac, atom1orig);
				if(sp.distanceTo(atom2orig)>1){
					Bond bond=new Bond(atom1, atom2);
					couplings.get(arccolor).add(bond);
				}
				if(alreadyDone.get(atom1)==null)
					alreadyDone.put(atom1, new ArrayList<IAtom>());
				alreadyDone.get(atom1).add(atom2);
    		}
    	}
        cdkmol.setProperty("couplings", couplings);
    }
    Rectangle drawArea = new Rectangle(moleculewidth, moleculeheight);
    renderer.setup(cdkmol, drawArea);
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document = domImpl.createDocument(null, "svg", null);
    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
    svgGenerator.setBackground(backColor);
    svgGenerator.setColor(backColor);
    svgGenerator.fill(new Rectangle(0, 0, moleculewidth, moleculeheight));
    renderer.paint(cdkmol, new AWTDrawVisitor(svgGenerator), drawArea, false);
    boolean useCSS = false;
    baos = new ByteArrayOutputStream();
    Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
    StringBuffer sb = new StringBuffer();
    svgGenerator.stream(outwriter, useCSS);
    StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
    while (tokenizer.hasMoreTokens()) {
      String name = tokenizer.nextToken();
      if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
        sb.append(name.substring(0, name.length())).append(" width=\"" + moleculewidth + "\" height=\"" + moleculeheight + "\"" + "\n\r");
      } else {
        sb.append(name + "\n\r");
      }
    }
    return (sb.toString());
  }

  private static final Color[] KELLY_COLORS = {
		  Color.decode("0xFFB300"),    // Vivid Yellow
		    Color.decode("0x803E75"),    // Strong Purple
		    Color.decode("0xFF6800"),    // Vivid Orange
		    Color.decode("0xA6BDD7"),    // Very Light Blue
		    Color.decode("0xC10020"),    // Vivid Red
		    Color.decode("0xCEA262"),    // Grayish Yellow
		    Color.decode("0x817066"),    // Medium Gray

		    Color.decode("0x007D34"),    // Vivid Green
		    Color.decode("0xF6768E"),    // Strong Purplish Pink
		    Color.decode("0x00538A"),    // Strong Blue
		    Color.decode("0xFF7A5C"),    // Strong Yellowish Pink
		    Color.decode("0x53377A"),    // Strong Violet
		    Color.decode("0xFF8E00"),    // Vivid Orange Yellow
		    Color.decode("0xB32851"),    // Strong Purplish Red
		    Color.decode("0xF4C800"),    // Vivid Greenish Yellow
		    Color.decode("0x7F180D"),    // Strong Reddish Brown
		    Color.decode("0x93AA00"),    // Vivid Yellowish Green
		    Color.decode("0x593315"),    // Deep Yellowish Brown
		    Color.decode("0xF13A13"),    // Vivid Reddish Orange
		    Color.decode("0x232C16"),    // Dark Olive Green
		};

  /**
   *  Gets the spectrum as svg which was set by the constructor.
   *
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg
   * @return                The spectrum as svg
   * @throws DataSetException 
   * @throws IOException 
   * @throws NmrshiftdbException 
   * @throws TorqueException 
   * @throws SVGGraphics2DIOException 
   * @throws UnsupportedEncodingException 
   * @throws OptionalDataException 
   */
  public String getSpecSvg(int spectrumwidth, int spectrumheight) throws OptionalDataException, UnsupportedEncodingException, SVGGraphics2DIOException, TorqueException, NmrshiftdbException, IOException, DataSetException {
    return getSpecSvg(spectrum, spectrumwidth, spectrumheight, this.useCSS,null,null);
  }
  
  /**
   * @param spectrumwidth   Width of svg
   * @param spectrumheight  Height of svg
   * @param specfile     The spectrum to export, a string of type shift,intensity|...
   * @return The spectrum as svg
   * @throws TorqueException 
   * @throws SVGGraphics2DIOException 
   * @throws UnsupportedEncodingException 
   */
  public String getSpecSvg(int spectrumwidth, int spectrumheight, String specfile) throws UnsupportedEncodingException, SVGGraphics2DIOException, TorqueException {
	    return getSpecSvg(specfile,spectrum.getDBSpectrumType().getNameAsString(),(spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null)==null || spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue().equals("unknown") || spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue().equals("Unreported")) ? "": spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue(), spectrumwidth, spectrumheight, useCSS);
  }
  
  /**
   *  Gets the spectrum as svg which was set by the constructor.
   *
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg   
   * @param      atoms      If this array is not null, it will be used for generating the specfile, else atoms will be taken from database via spectrum (used in generating exports for not yet saved data, normally null is fine).
   * @param  searchshifts   If not null, these shifts will be shown as search shifts in the image (2d only).
   * @return                The spectrum as svg
   * @throws DataSetException 
   * @throws IOException 
   * @throws NmrshiftdbException 
   * @throws TorqueException 
   * @throws SVGGraphics2DIOException 
   * @throws UnsupportedEncodingException 
   * @throws OptionalDataException
   */
  public String getSpecSvg(int spectrumwidth, int spectrumheight, DBAtom[] atoms, CMLPeakList searchshifts) throws OptionalDataException, UnsupportedEncodingException, SVGGraphics2DIOException, TorqueException, NmrshiftdbException, IOException, DataSetException {
    return getSpecSvg(spectrum, spectrumwidth, spectrumheight, this.useCSS, atoms, searchshifts);
  }
  
  
  /**
   *  Gets a spectrum as svg, given by spectrum
   *
   * @param      spectrum   The spectrum to export.
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg
   * @param      useCSS
   * @param      atoms      If this array is not null, it will be used for generating the specfile, else atoms will be taken from databse via spectrum (used in generating exports for not yet saved data, normally null is fine).
   * @param  searchshifts   If not null, these shifts will be shown as search shifts in the image (2d only).
   * @return                The spectrum as svg
   * @throws DataSetException 
   * @throws IOException 
   * @throws NmrshiftdbException 
   * @throws TorqueException 
   * @throws SVGGraphics2DIOException 
   * @throws UnsupportedEncodingException 
   * @throws OptionalDataException 
   */
  public String getSpecSvg(DBSpectrum spectrum, int spectrumwidth, int spectrumheight, boolean useCSS, DBAtom[] atoms, CMLPeakList searchshifts) throws OptionalDataException, UnsupportedEncodingException, SVGGraphics2DIOException, TorqueException, NmrshiftdbException, IOException, DataSetException {
		if(spectrum.getDBSpectrumType().getDBIsotopes().size()==2){
			return get2DSpecSvg(spectrum,spectrumwidth,spectrumheight,searchshifts);
		}else{
			return getSpecSvg(atoms==null ? spectrum.getSpectrumForAppletNewFormat() : spectrum.getSpectrumForAppletNewFormat(atoms,cdkmolwithh),spectrum.getDBSpectrumType().getNameAsString(),(spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null)==null || spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue().equals("unknown") || spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue().equals("Unreported")) ? "": spectrum.getMeasurementConditionWithName(NmrshiftdbConstants.FREQUENCY,null).getValue(), spectrumwidth, spectrumheight, useCSS);
		}
  }
    
    
  
  	/**
  	 * Generates a 2d spectrum svg.
  	 * 
	 * @param spectrum2 The spectrum.
	 * @param spectrumwidth Width of svg.
	 * @param spectrumheight Height of svg.
	 * @param searchshifts f not null, these shifts will be shown as search shifts in the image.
	 * @return The spectrum svg.
	 * @throws TorqueException
	 * @throws IOException 
	 */
	private String get2DSpecSvg(DBSpectrum spectrum2, int spectrumwidth, int spectrumheight, CMLPeakList searchshifts) throws TorqueException, IOException{
     Graph2D graph;
     DataSet data1;
     DataSet data2=null;
     Axis    xaxis;
     Axis    yaxis_left;
     int i;
      int j;
      double data[] = new double[2*spectrum2.getDBSignals().size()];
      if(spectrumwidth==-1)
   	      spectrumwidth=300;
      if(spectrumheight==-1)
   	      spectrumheight=200;
      graph = new Graph2D();
      graph.drawzero = false;
      graph.drawgrid = false;
      graph.setDataBackground(Color.LIGHT_GRAY);
      graph.setGraphBackground(Color.LIGHT_GRAY);
      graph.setMarkers(new Markers());
      for(i=j=0; i<spectrum2.getDBSignals().size(); i++,j+=2) {
          data[j] = ((DBSignal)spectrum2.getDBSignals().get(i)).getDBShiftsArray()[0].getValue();
          data[j+1] = ((DBSignal)spectrum2.getDBSignals().get(i)).getDBShiftsArray()[1].getValue();
      }
      data1 = graph.loadDataSet(data,spectrum2.getDBSignals().size()*2);
      data1.linestyle = 0;
      data1.marker    = 1;
      data1.markerscale = 1.5;
      data1.markercolor = new Color(0,0,255);
      if(searchshifts!=null && searchshifts.getPeakElements().size()>0){
	       data = new double[2*searchshifts.getPeakElements().size()];
	       for(i=j=0; i<searchshifts.getPeakElements().size(); i++,j+=2) {
	           data[j] = searchshifts.getPeakElements().get(i).getYValue();
	           data[j+1] = searchshifts.getPeakElements().get(i).getXValue();
	       }
	
	       data2 = graph.loadDataSet(data,searchshifts.getPeakElements().size()*2);
	       data2.linestyle = 0;
	       data2.marker    = 1;
	       data2.markerscale = 1.5;
	       data2.markercolor = new Color(255,0,0);
      }
      xaxis = graph.createAxis(Axis.BOTTOM);
      xaxis.attachDataSet(data1);
      if(data2!=null)
   	   xaxis.attachDataSet(data2);
      xaxis.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
      xaxis.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
      yaxis_left = graph.createAxis(Axis.LEFT);
      yaxis_left.attachDataSet(data1);
      if(data2!=null)
   	   yaxis_left.attachDataSet(data2);
      yaxis_left.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
      yaxis_left.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
      yaxis_left.setTitleColor( new Color(0,0,255) );
      DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
      Document document = domImpl.createDocument(null, "svg", null);
      SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
      HeadlessPanel win=new HeadlessPanel();
      win.setLayout( new BorderLayout() );
      win.add("Center", graph);
      win.setBackground(Color.LIGHT_GRAY);
      Dimension spectrumdim=new Dimension(spectrumwidth,spectrumheight);
      win.setSize(spectrumdim);
      win.setPreferredSize(spectrumdim);
      win.setMinimumSize(spectrumdim);
      win.setMaximumSize(spectrumdim);
      win.layout();
      graph.paint(svgGenerator);
	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
	   StringBuffer sb = new StringBuffer();
	   svgGenerator.stream(outwriter, useCSS);
	   StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
	   while (tokenizer.hasMoreTokens()) {
	     String name = tokenizer.nextToken();
	     if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
	       sb.append(name.substring(0, name.length() - 1)).append(" width=\"" + spectrumwidth + "\" height=\"" + spectrumheight + "\">");
	     } else {
	       sb.append(name);
	     }
	   }
	   return (GeneralUtils.replace(sb.toString(),"clip-path=\"url(#clipPath1)\" "," "));
  }

  
  /**
   *  Gets a spectrum as svg, given by specfile
   *
   * @param      specfile   The spectrum to export, a string of type shift,intensity|...
   * @param      spectype   The type of spectrum, string like 13C, 1H ...
   * @param      fieldstrength The fieldstrength
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg
   * @param      useCSS
   * @return                The spectrum as svg
   * @throws UnsupportedEncodingException 
   * @throws SVGGraphics2DIOException
   */
  public static String getSpecSvg(String specfile, String spectype, String fieldstrength, int spectrumwidth, int spectrumheight, boolean useCSS) throws UnsupportedEncodingException, SVGGraphics2DIOException {
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document = domImpl.createDocument(null, "svg", null);
    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
    if(spectrumwidth==-1)
      spectrumwidth=300;
    if(spectrumheight==-1)
      spectrumheight=200;
    try{
        SpectrumModel model = new SpectrumModel();
        model.setShowNavigationView(false);
        SpectrumNavigation sn=new SpectrumNavigation(spectype, fieldstrength, "", specfile, "", "", model, null, true);
        Dimension spectrumdim=new Dimension(spectrumwidth,spectrumheight);
        sn.setSize(spectrumdim);
        sn.setPreferredSize(spectrumdim);
        sn.setMinimumSize(spectrumdim);
        sn.setMaximumSize(spectrumdim);
        sn.printAll(svgGenerator);
    }catch(Exception ex){
      //do nothing, better no spec image than error message
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
    StringBuffer sb = new StringBuffer();
    svgGenerator.stream(outwriter, useCSS);
    StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
    while (tokenizer.hasMoreTokens()) {
      String name = tokenizer.nextToken();
      if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
        sb.append(name.substring(0, name.length())).append(" width=\"" + spectrumwidth + "\" height=\"" + spectrumheight + "\"");
      } else {
        sb.append(name);
      }
    }
    return (GeneralUtils.replace(sb.toString(),"clip-path=\"url(#clipPath1)\" "," "));
  }

  /**
   *  Gets an image of spectrum or molecule, width/height set to a reasonable value for molecules (Hint: don't use this for spectra).
   *
   * @param  molecule       true=image of molecule, falso=of spectrum
   * @param  format         jpeg, tiff or png
   * @param  path           Where to save the image
   * @param      drawnumbers Should the svg contain atom numbers?
   * @param  searchshifts   If not null, these shifts will be shown as search shifts in the image (2d only).
   * @return                The path to the image
   * @exception  Exception  Description of Exception
   */
  public String getImage(boolean molecule, String format, String path, boolean drawNumbers, CMLPeakList searchshifts, Color arccolor) throws Exception {
	  return getImage(molecule, format, path, -1, -1, drawNumbers,null,null, searchshifts, arccolor, null, null, null, null, null);
  }
  
  /**
   * Gets the mdl file of the molecule in this export object without explicit hs.
   * 
   * @return
   * @throws CDKException
   * @throws IOException 
   */
  public String getMdl() throws CDKException, IOException{
      MDLV2000Writer mdlWriter=null;
      try{
          StringWriter writer = new StringWriter();
          mdlWriter = new MDLV2000Writer(writer);
          mdlWriter.write(cdkmol);
          return writer.toString();
      }
      finally{
          mdlWriter.close();
      }
  }
  
  /**
   * Gets the cml file of the molecule in this export object without explicit hs.
   * @return
   */
	public String getCml(){
	  Convertor convertor=new Convertor(true,null);
	  Element cmlDOM = convertor.cdkAtomContainerToCMLMolecule(cdkmol);
      String cmlString = cmlDOM.toXML();
      return cmlString;
  }


  public String getImage(boolean molecule, String format, String path, int width, int height, boolean drawNumbers, DBAtom[] atoms, String specfile, CMLPeakList searchshifts) throws Exception {
	  return getImage(molecule, format, path, width, height, drawNumbers, atoms, specfile, searchshifts, null, null, null, null, null, null);
  }

	  
  /**
   *  Gets an image of spectrum or molecule
   *
   * @param  molecule       true=image of molecule, falso=of spectrum
   * @param  format         jpeg, tiff or png
   * @param  path           Where to save the image
   * @param      width      The width of the svg
   * @param      height     The height of the svg
   * @param      drawnumbers Should the svg contain atom numbers?
   * @param      atoms      If this array is not null, it will be used for generating the specfile, else atoms will be taken from databse (used in generating exports for not yet saved data, normally null is fine).
   * @param  specfile		If this is not null, this specfile will be used for the image. atoms or specfile must be set.
   * @param  searchshifts   If not null, these shifts will be shown as search shifts in the image (2d only).
   * @return                The path to the image
   * @exception  Exception  Description of Exception
   */
  public String getImage(boolean molecule, String format, String path, int width, int height, boolean drawNumbers, DBAtom[] atoms, String specfile, CMLPeakList searchshifts, Color arccolor, List<IAtom> greenAtoms, List<IAtom> orangeAtoms, List<IAtom> redAtoms, List<IAtom> grayAtoms, Collection<List<IAtom>> atomsets) throws Exception {
	  return getImage(molecule,format,path, width, height, drawNumbers, atoms, specfile, searchshifts, arccolor, greenAtoms, orangeAtoms, redAtoms, grayAtoms, atomsets, null);
  }

  public String getImage(boolean molecule, String format, String path, int width, int height, boolean drawNumbers, DBAtom[] atoms, String specfile, CMLPeakList searchshifts, Color arccolor, List<IAtom> greenAtoms, List<IAtom> orangeAtoms, List<IAtom> redAtoms, List<IAtom> grayAtoms, Collection<List<IAtom>> atomsets, List<List<Pair<Integer, Integer>>> result) throws Exception {
	String inputstr;
    if (molecule) {
      inputstr = getMolSvg(width, height, drawNumbers, arccolor, greenAtoms, orangeAtoms, redAtoms, grayAtoms, result, atomsets);
    } else {
    	if(specfile!=null)
    		inputstr = getSpecSvg(width, height, specfile);
    	else
    		inputstr = getSpecSvg(width, height,atoms, searchshifts);
    }
    String imagefile = path;
    if(format.equals("pdf")) {
    	imagefile += ".pdf";
    	SVGConverter converter = new SVGConverter();
    	converter.setDestinationType(DestinationType.PDF);
    	File inputFile = File.createTempFile("input-"+System.currentTimeMillis(), ".svg");
    	FileOutputStream fos=new FileOutputStream(inputFile);
    	fos.write(inputstr.getBytes());
    	fos.close();
    	converter.setSources(new String[] { inputFile.getAbsoluteFile().getPath() });
    	converter.setDst(new File(imagefile));
    	converter.execute();
    	inputFile.delete();
    }else {
	    ImageTranscoder it = null;
	    if (format.equals("jpeg")) {
	      it = new JPEGTranscoder();
	      it.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
	      imagefile += ".jpg";
	    }
	    if (format.equals("tiff")) {
	      it = new TIFFTranscoder();
	      imagefile += ".tif";
	    }
	    if (format.equals("png")) {
	      it = new PNGTranscoder();
	      imagefile += ".png";
	    }
	    TranscoderInput input = new TranscoderInput(new StringReader(inputstr));
	    OutputStream ostream = new FileOutputStream(imagefile);
	    TranscoderOutput output = new TranscoderOutput(ostream);
	    it.transcode(input, output);
	    ostream.flush();
	    ostream.close();
    }
    return (imagefile);
  }


  /**
   *  Makes a zip file out of several files
   *
   * @param  filenames        The files to be zipped
   * @return                  The zip file
   * @exception  IOException  Problem reading files
   */
  public ByteArrayOutputStream makeZip(String[] filenames) throws IOException {
    // Create a buffer for reading the files
    byte[] buf = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // Create the ZIP file
    ZipOutputStream out = new ZipOutputStream(baos);
    // Compress the files
    for (int i = 0; i < filenames.length; i++) {
      FileInputStream in = new FileInputStream(filenames[i]);
      // Add ZIP entry to output stream.
      out.putNextEntry(new ZipEntry(new File(filenames[i]).getName()));
      // Transfer bytes from the file to the ZIP file
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      // Complete the entry
      out.closeEntry();
      in.close();
    }
    out.flush();
    out.close();
    return (baos);
  }
}

@SuppressWarnings("serial")
class HeadlessPanel extends JPanel
{
    public HeadlessPanel()
    {
        setBackground(Color.LIGHT_GRAY);
    }
    
    /**
     * Paint the component.
     * @param g The graphics context onto which the component is to be painted.
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        // As the panel doesn't belong to a window, we need to explicitly layout the components
        doLayout();
        
        Graphics gr = g.create();
 
        // Print the component 
        printComponent(gr);
 
        // ... any border
        printBorder(gr);
        
        // ... and all of the children.
        for (int c = 0; c < getComponentCount(); c++)
        {
            Component comp = getComponent(c);
            Rectangle cr = comp.getBounds();
 
            Graphics cg = gr.create(cr.x, cr.y, cr.width, cr.height);
            comp.printAll(cg);
        }
    }
    
    /**
     * Is this component showing.
     * @return This is always <code>true</code> so that the panel will be rendered.
     * @see java.awt.Component#isShowing()
     */
    public boolean isShowing()
    {
        return true;
    }
 
}
