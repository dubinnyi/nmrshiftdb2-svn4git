/*****************************************************************************
* Copyright (c) 2008 Bioclipse Project
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
*****************************************************************************/
package org.openscience.nmrshiftdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.jcamp.parser.Utils;
import org.jcamp.spectrum.IRSpectrum;
import org.jcamp.spectrum.MassSpectrum;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.spectrum.Spectrum1D;
import org.jcamp.spectrum.notes.Note;
import org.jcamp.spectrum.notes.NoteDescriptor;
import org.xmlcml.cml.element.CMLArray;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSpectrumData;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;
import org.xmlcml.cml.element.CMLXaxis;
import org.xmlcml.cml.element.CMLYaxis;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * Maps a JCamp spectrum to a CMLSpectrum
 * 
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 */
public class JcampToCMLSpectrumMapper {

	/**
	 * Main method, responsible for calling the mapping methods and setting
	 * general settings
	 * 
	 * @param spectrum
	 *            the JCamp spectrum
	 * @return the CMLSpectrum element
	 */
	public static CMLSpectrum mapJcampToCMLSpectrum(Spectrum spectrum, RunData runData) {
		CMLSpectrum cmlSpectrum = new CMLSpectrum();
		Spectrum1D spectrum1d = (Spectrum1D) spectrum;

		if (spectrum instanceof NMRSpectrum) {
			cmlSpectrum.setType("NMR");
		} else if (spectrum instanceof MassSpectrum) {
			cmlSpectrum.setType("massSpectrum");
		} else if (spectrum instanceof IRSpectrum) {
			cmlSpectrum.setType("infrared");
		}
		cmlSpectrum.setTitle(spectrum.getTitle());
		if (spectrum1d.hasPeakTable()) {
			cmlSpectrum.addPeakList(mapPeaks(spectrum1d));
		}
		if (spectrum1d.isFullSpectrum()) {
			cmlSpectrum.addSpectrumData(mapContData(spectrum1d));
		}
		if (spectrum.getNotes().size() != 0 && runData!=null) {
			mapNotes(spectrum, cmlSpectrum, runData);
		}



		/*
		 * else if (spectrum instanceof IRSpectrum) { SpokIRSpectrum irSpectrum =
		 * new SpokIRSpectrum(); irSpectrum.setTitle(spectrum.getTitle()); if
		 * (spectrum1d.hasPeakTable()) {
		 * nmrSpectrum.addPeakList(mapPeaks(spectrum1d)); } if
		 * (spectrum1d.isFullSpectrum()) {
		 * nmrSpectrum.addXYDataList(mapContData(spectrum1d)); }
		 * 
		 * if (spectrum.getNotes().size() != 0) {
		 * nmrSpectrum.setNotesTable(mapNotes(spectrum)); } spokSpectrum =
		 * (SpokSpectrum) irSpectrum; }
		 */

		NoteDescriptor notedescriptor = new NoteDescriptor("SPECTRUMID");
		String id = null;
		if (spectrum.getNotes(notedescriptor) != null && spectrum.getNotes(notedescriptor).size()>0) {
			Note note = (Note) spectrum.getNotes(notedescriptor).get(0);
			id = note.getValue().toString();
		}
		if (id == null) {
			id = GenerateId.generateId();
		}
		cmlSpectrum.setId(id);
		return cmlSpectrum;
	}

	/**
	 * Maps the peak list
	 * 
	 * @param spectrum1d
	 *            the JCamp spectrum1d
	 * @return a CMLPeakList element
	 */
	private static CMLPeakList mapPeaks(Spectrum1D spectrum1d) {
		CMLPeakList cmlPeaks = new CMLPeakList();
		Peak1D[] peaks = (spectrum1d).getPeakTable();
		for (int i = 0; i < peaks.length; i++) {
			CMLPeak peak = new CMLPeak();
			peak.setXValue(peaks[i].getPosition()[0]);
			peak.setYValue(peaks[i].getHeight());
			peak.setXUnits("jcampdx:" + spectrum1d.getXAxisLabel());
			peak.setYUnits("jcampdx:" + spectrum1d.getYAxisLabel());
			cmlPeaks.addPeak(peak);
		}
		return cmlPeaks;
	}
	
	
	/**
	 * Map the notes/metadata
	 * 
	 * @param spectrum
	 *            the JCamp spectrum
	 * @param cmlSpectrum
	 * @return a CMLMetadataList element
	 */
	private static void mapNotes(Spectrum spectrum, CMLSpectrum cmlSpectrum, RunData runData) {

		Builder builder = new Builder();
		Document metadataMapping = null;

//		URL varPluginUrl = Platform.getBundle(
//		"net.bioclipse.plugins.bc_spectrum").getEntry("/mappingFiles/");
//		String varInstallPath = null;
//		try {
//		varInstallPath = Platform.asLocalURL(varPluginUrl).getFile();
//		} catch (IOException e) {
//		StringWriter strWr = new StringWriter();
//		PrintWriter prWr = new PrintWriter(strWr);
//		}

//		try {
//		metadataMapping = builder.build(varInstallPath + "jcampMetadataMapping.xml");
//		} catch (ValidityException e) {
//		e.printStackTrace();
//		} catch (ParsingException e) {
//		e.printStackTrace();
//		} catch (IOException e) {
//		e.printStackTrace();
//		}
		ArrayList<Element> mappingListArray = new ArrayList<Element>();

		//get a file list of contained files and iterate over them
		File dir = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/mapping"));
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++) {
			File file = files[i];
			if (file.getName().startsWith(".")) {
				continue;
			}
			else {
				try {
					metadataMapping = builder.build(file);
				} catch (ValidityException e) {
					e.printStackTrace();
				} catch (ParsingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Element rootElem = metadataMapping.getRootElement();
				mappingListArray.add(rootElem);
			}
		}

		CMLMetadataList metadataList = new CMLMetadataList();
		CMLConditionList conditionList = new CMLConditionList();
		CMLSubstanceList substanceList = new CMLSubstanceList();


		Properties notesProps = new Properties();
		java.io.InputStream is = null;
		try {
			is = new FileInputStream(ServletUtils.expandRelative(runData.getServletConfig(), "/WEB-INF/conf/notes.properties"));
			if (is == null)
				return;
			notesProps.load(is);
		}
		catch (java.io.IOException e) {
			e.printStackTrace();
		}

		Collection notesCollection = spectrum.getNotes();
		Iterator notesIterator = notesCollection.iterator();
		while (notesIterator.hasNext()) {
			Note note = (Note) notesIterator.next();
			if (note.getValue() != " ") {
				String key = (String) note.getDescriptor().getKey();
				String jcamp = (String) notesProps.get(key + ".jcamp");
				if (jcamp != null) {
					key = jcamp;
				}
				boolean foundInAMappingFile = false;
				String oldKey = key;
				for (int j=0; j<mappingListArray.size(); j++) {
					Element rootElem = mappingListArray.get(j);
					
					Attribute prefixAttr = rootElem.getAttribute("prefix");
					
					if (prefixAttr.getValue().compareTo("jcampdx") == 0) {
						key = Utils.normalizeLabel(key);
						if (key.startsWith(".")) {
							key = "dot" + key.substring(1);
						}
					}

					Nodes label = rootElem.query("//entry[@id='" + key +"']");
					Nodes result = rootElem.query("//entry[@id='" + key +"']/parent::*");
					if (result.size() == 0) {
						key = "dot" + key;
						label = rootElem.query("//entry[@id='" + key +"']");
						result = rootElem.query("//entry[@id='" + key +"']/parent::*");
					}
					if (result.size() > 0) {
						foundInAMappingFile = true;
						String title;
						if (label.size() == 1) {
							title =((Element)label.get(0)).getAttributeValue("label");
						}
						else {
							title = sanitize(note.getDescriptor().getName());
						}	
						String listName = ((Element)result.get(0)).getAttributeValue("name");
						if (listName.equals("conditionList")) {
							CMLScalar condition = new CMLScalar();		
							condition.setId(key);
							condition.setTitle(title);
							condition.setValue(note.getValue().toString());
							conditionList.appendChild(condition);
						}
						else if (listName.equals("substanceList")) {
							CMLSubstance substance = new CMLSubstance();	
							substance.setTitle(title);
							substance.setId(key);
							nu.xom.Text textNode = new nu.xom.Text(note.getValue().toString());
							substance.appendChild(textNode);
							substanceList.appendChild(substance);
						}
						else {
							CMLMetadata metadata = new CMLMetadata();
							metadata.setName(prefixAttr.getValue() + ":" + title);
							metadata.setTitle(title);
							metadata.setId(key);
							metadata.setContent(note.getValue().toString());
							metadataList.appendChild(metadata);
						}
						break;
					}
					else {
						key = oldKey;
					}
				}
				if (!foundInAMappingFile) {
//					key = key.substring(3);
					CMLMetadata metadata = new CMLMetadata();
					String name = "jcampdx:" + sanitize(note.getDescriptor().getName());
					metadata.setName(name);
					metadata.setTitle(sanitize(note.getDescriptor().getName()));
					metadata.setId(key);
					metadata.setContent(note.getValue().toString());
					metadataList.appendChild(metadata);
				}
			}
		}
		if (metadataList.getChildCount() > 0) {
			cmlSpectrum.addMetadataList(metadataList);
		}
		if (conditionList.getChildCount() > 0) {
			cmlSpectrum.addConditionList(conditionList);
		}
		if (substanceList.getChildCount() > 0) {
			cmlSpectrum.appendChild(substanceList);
		}
	}

	/**
	 * Maps continuous data
	 * 
	 * @param spectrum1d
	 *            the JCamp spectrum1d
	 * @return a CMLSpectrumData element
	 */
	private static CMLSpectrumData mapContData(Spectrum1D spectrum1d) {
		CMLSpectrumData xyData = new CMLSpectrumData();
		double[] xData = spectrum1d.getXData().toArray();
		double[] yData = spectrum1d.getYData().toArray();
		CMLXaxis xAxis = new CMLXaxis();
		CMLYaxis yAxis = new CMLYaxis();
		xAxis.addArray(new CMLArray(xData));
		yAxis.addArray(new CMLArray(yData));
		xyData.addXaxis(xAxis);
		xyData.addYaxis(yAxis);
		xAxis.setTitle(spectrum1d.getXAxisLabel());
		yAxis.setTitle(spectrum1d.getYAxisLabel());

		return xyData;
	}


	/**
	 * Ensures the output has the pattern "[A-Za-z][A-Za-z0-9_\.\-]*".
	 * 
	 * @param key
	 * @return
	 */
	public static String sanitize(String key) {
		// assume key length > 1
		if (key.length() < 2) {
			return key;
		}
		StringBuffer sanatizedString = new StringBuffer();
		int firstCharInt = 0;
		char firstChar;
		do {
			firstChar = key.charAt(firstCharInt);
			if (Character.isLetter(firstChar))
				sanatizedString.append(firstChar);
			firstCharInt++;
		} while (Character.isLetter(firstChar)
				&& !(firstCharInt < key.length()));
		for (int i = firstCharInt; i < key.length(); i++) {
			char character = key.charAt(i);
			if (Character.isDigit(character) || Character.isLetter(character)
					|| character == '_' || character == '.' || character == '-') {
				sanatizedString.append(character);
			} // else: bad char, skip
		}
		return sanatizedString.toString();
	}	
}
