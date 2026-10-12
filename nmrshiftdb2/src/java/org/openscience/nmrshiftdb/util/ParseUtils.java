/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2012 Stefan Kuhn
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

/**
 *This class contains utitilies for parsing input strings
 *
 * @author     shk3
 * @created    18. M�rz 2002
 */
public class ParseUtils {


  /**
   *Returns an ArrayList of ValueTriples out of an input string like 3,4\n5;6...
   *
   * @param  input                      The input.
   * @param  diastereotopic             If not null, any lines startig with + are put into this map (with the line before as key) instead of the returned list.
   * @return                            An list of CMLPeaks, one per line.
   * @exception  NumberFormatException  User inputed non-numerical values.
   */
  public static CMLPeakList parseSpectrum(String input, Map<Double,Double> diastereotopic)
       throws NumberFormatException {
	CMLPeakList numberSpectrum = new CMLPeakList();
    input += "\n";
    String shift = "";
    String intensity = "";
    boolean addingShift = true;
    char[] charSpectrum = input.toCharArray();
    for (int i = 0; i < charSpectrum.length; i++) {
      if (charSpectrum[i] == ';' || charSpectrum[i] == ':') {
        intensity = "";
        addingShift = false;
      } else {
        if (charSpectrum[i] == '\n' || charSpectrum[i] == ' ') {
          CMLPeak figures = new CMLPeak();
          if (!shift.trim().equals("")) {
            shift=shift.trim();
            shift = shift.replace(',', '.');
            intensity=intensity.trim();
            intensity = intensity.replace(',', '.');
            if(shift.length()>0 && (shift.charAt(shift.length()-1)=='S' || shift.charAt(shift.length()-1)=='D' ||shift.charAt(shift.length()-1)=='T' ||shift.charAt(shift.length()-1)=='Q')){
              figures.setPeakMultiplicity(shift.substring(shift.length()-1));
              shift=shift.substring(0,shift.length()-1);
            }
            if(shift.charAt(0)=='+' && diastereotopic!=null){
                shift=shift.substring(1);
                diastereotopic.put(numberSpectrum.getPeakChildren().get(numberSpectrum.getChildCount()-1).getXValue(), Double.parseDouble(shift.trim()));
            }else{
                figures.setXValue(Double.parseDouble(shift.trim()));
                if (intensity.trim().equals("")) {
                  figures.setYValue(0);
                } else {
                  if(intensity.charAt(intensity.length()-1)=='S' || intensity.charAt(intensity.length()-1)=='D' ||intensity.charAt(intensity.length()-1)=='T' ||intensity.charAt(intensity.length()-1)=='Q'){
                    figures.setPeakMultiplicity(intensity.substring(intensity.length()-1));
                    intensity=intensity.substring(0,intensity.length()-1);
                  }
                  figures.setYValue(Double.parseDouble(intensity.trim()));
                }
                numberSpectrum.addPeak(figures);
            }
            shift = "";
            intensity = "";
            addingShift = true;          }
        } else {
          if (addingShift) {
            shift = shift + charSpectrum[i];
          } else {
            intensity = intensity + charSpectrum[i];
          }
        }
      }
    }
    return (numberSpectrum);
  }
  
  /**
   * Parses a spectrum of the format axis1;axis2;coupling. Coupling goes into peakHeight.
   *
   * @param input
   * @return
   */
  public static CMLPeakList parse2DSpectrum(String input){
      CMLPeakList numberSpectrum = new CMLPeakList();
      StringTokenizer st=new StringTokenizer(input);
      while(st.hasMoreTokens()){
          String line=st.nextToken();
          StringTokenizer stinner=new StringTokenizer(line,";");
          int counttokensinline=0;
          CMLPeak peak=new CMLPeak();
          numberSpectrum.addPeak(peak);
          peak.setPeakHeight(Double.NaN);
          while(stinner.hasMoreTokens()){
              String innertoken=stinner.nextToken();
              if(counttokensinline==0){
                  peak.setXValue(Double.parseDouble(innertoken));
              }else if(counttokensinline==1){
                  peak.setYValue(Double.parseDouble(innertoken));
              }else{
                  peak.setPeakHeight(Double.parseDouble(innertoken));
              }
              counttokensinline++;
              if(counttokensinline>3)
                  throw new NumberFormatException("Too many ;");
          }          
      }
      return numberSpectrum;
  }

  /**
   * Returns a List of CMLPeaks out of an input string like 3,4,1|5,6,2|..., as found in the specfile attribute of Spectrum in the database
   *
   * @param  spectrum                   The string to parse.
   * @return                            A List of CMLPeaks.
   * @exception  NumberFormatException  User inputed non-numerical values.
   */
  public static List<CMLPeak> parseSpectrumFromSpecFile(String spectrum)
       throws NumberFormatException {
    spectrum += "|";
    char[] charSpectrum = spectrum.toCharArray();
    String shift = "";
    String intensity = "";
    String atom = "";
    List<CMLPeak> numberSpectrum = new ArrayList<CMLPeak>();
    boolean addingShift = true;
    boolean addingIntensity = false;
    //The string is parsed
    for (int i = 0; i < charSpectrum.length; i++) {
      if (charSpectrum[i] == ';' && addingShift == true) {
        intensity = "";
        addingShift = false;
        addingIntensity = true;
      } else {
        if (charSpectrum[i] == ';' && addingIntensity == true) {
          atom = "";
          addingIntensity = false;
        } else {
          if (charSpectrum[i] == '|') {
            CMLPeak figures = new CMLPeak();
            if (!shift.trim().equals("")) {
              try {
                figures.setXValue(Double.parseDouble(shift.trim()));
                if (intensity.trim().equals("")) {
                  figures.setYValue(0);
                } else {
                  String intensitynew="";
                  for(int l=0;l<intensity.length();l++){
                    if(Character.isLetter(intensity.charAt(l))){
                      figures.setPeakMultiplicity(figures.getPeakMultiplicity()!=null ? figures.getPeakMultiplicity()+intensity.charAt(l) : ""+intensity.charAt(l));
                    }else{
                      intensitynew=intensitynew+intensity.charAt(l);
                    }
                  }
                  if(!intensitynew.trim().equals("")){
                	  figures.setYValue(Double.parseDouble(intensitynew.trim()));
                  }
                }
                int addTo = -1;
                for (int k = 0; k < numberSpectrum.size(); k++) {
                  if (numberSpectrum.get(k).getXValue() == figures.getXValue() && numberSpectrum.get(k).getPeakMultiplicity()!=null && numberSpectrum.get(k).getPeakMultiplicity().equals(figures.getPeakMultiplicity())) {
                    addTo = k;
                  }
                }
                if (addTo > -1) {
                	numberSpectrum.get(addTo).setAtomRefs(SubmittingData.addAtomToAtomRefs(numberSpectrum.get(addTo).getAtomRefs(),"a"+atom));
                } else {
                	if(!atom.trim().equals("")){
                		figures.setAtomRefs(SubmittingData.addAtomToAtomRefs(figures.getAtomRefs(),"a"+atom));
                	}
            		numberSpectrum.add(figures);
                }
              } catch (NumberFormatException ex) {
                throw ex;
              }
              shift = "";
              addingShift = true;
            }
          } else {
            if (addingShift) {
              shift = shift + charSpectrum[i];
            } else {
              if (addingIntensity) {
                intensity = intensity + charSpectrum[i];
              } else {
                atom = atom + charSpectrum[i];
              }
            }
          }
        }
      }
    }
    return (numberSpectrum);
  }


  /**
   *Removes duplicate entries from an ArrayList of ValueTriples
   *
   * @param  al  The ArrayList to process
   */
  public static void removeDuplicates(CMLPeakList al) {
	List<CMLPeak> toremove=new ArrayList<CMLPeak>();
    //compare each entry to each other entry
    for (int i = 0; i < al.getPeakElements().size(); i++) {
      for (int k = i + 1; k < al.getPeakElements().size(); k++) {
        if (al.getPeakElements().get(i).getXValue() == al.getPeakElements().get(k).getXValue()) {
          //in case there is a duplicate
          al.getPeakElements().get(i).setYValue(al.getPeakElements().get(i).getYValue()+al.getPeakElements().get(k).getYValue());
          //add the intensities in the first entry
          if (al.getPeakElements().get(i).getYValue() > 1) {
            al.getPeakElements().get(i).setYValue(1);
            //ensure the intensity is not higher than one
          }
          if(al.getPeakElements().get(k).getAtomRefs()!=null){
	          for(int m=0;m<al.getPeakElements().get(k).getAtomRefs().length;m++){
	        	  al.getPeakElements().get(i).setAtomRefs(SubmittingData.addAtomToAtomRefs(al.getPeakElements().get(i).getAtomRefs(),al.getPeakElements().get(k).getAtomRefs()[m]));
	          }
          }
          toremove.add(al.getPeakElements().get(k));
          //remove the other entry
        }
      }
    }
    for(int i=0;i<toremove.size();i++){
    	if(al.indexOf(toremove.get(i))>-1)
    		al.removeChild(toremove.get(i));
    }
  }


  /**
   * Returns an array of ValueTriple in the format of the input String
   *
   * @param  v  The valueTriples
   * @return    The input String
   */
  public static String toInputSpectrum(CMLPeakList v) {
    StringBuffer inputSpectrum = new StringBuffer();
    for (int i = 0; i < v.getPeakElements().size(); i++) {
      CMLPeak thisValueTriple = v.getPeakElements().get(i);
      inputSpectrum.append(thisValueTriple.getXValue() + (thisValueTriple.getYValue() == 0.0 ? "" : ";" + thisValueTriple.getYValue()) + "\n");
    }
    return (inputSpectrum.toString());
  }
}


