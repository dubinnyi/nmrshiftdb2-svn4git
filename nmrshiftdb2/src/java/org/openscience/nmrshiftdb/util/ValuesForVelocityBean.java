/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2014 Stefan Kuhn
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
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.om.DBExperimentPeer;
import org.openscience.nmrshiftdb.om.DBSignal;

/**
 *This is a java bean needed for putting data into the velocity context
 *
 * @author     shk3
 * @created    3. May 2002
 */
public class ValuesForVelocityBean implements Serializable{
  private static final long serialVersionUID = 1L;
  public double[] statistics;
  public double[] statistics2;
  //The text displayed in <select> etc.
  private String displayText = "";
  //This is the HOSE-Code range
  private String range = "";
  //This is the value-property of <select> etc
  private Float value = new Float(0);
  //The lower limit of the range
  private double lower = 0;
  //The upper limit of the range
  private double upper = 0;
  //Used for giving JavaScript names to elements in the assignments field
  private String nameForElements = "";
  //Says if value is between lower and upper
  private boolean isInRange = true;
  //This is used in predictions
  private Integer currentSelectedNumber = null;
  //These are for building the shift tables in details only
  private String multiplicity = "";
  private String delta = "";
  private String identifier = "";
  private String intensity = "";
  private int atom = -1;
  private ArrayList<StringAndStringAndBoolean> couplings = null;
  private float[] shifts;
  private String csearch = "";
  //Fpr 2d spectra, the signal is used directly
  private DBSignal signal;

  /**
   * Sets the value at position axis in th shifts array. If shifts array is null,
   * a new array of length axisnumber is created.
   * 
   * @param axisnumber 
   * @param axis
   * @param value
   */
  public void setShift(int axisnumber,int axis,float value){
	  if(shifts==null)
	  	shifts=new float[axisnumber];
	  shifts[axis]=value;
  }
  
  /**
   * Gets the value at axis from the shifts array.
   * 
   * @param axis Position in array.
   * @return The value from the shifts array.
   */
  public String getShift(int axis){
	  if(shifts==null)
		  return "__";
	  return shifts[axis]+"";
  }
  
  /**
   * Gets the shifts array.
   * 
   * @return The shifts array.
   */
  public float[] getShifts() {
	return shifts;
  }

  /**
   *Constructor for the ValuesForVelocityBean object
   */
  public ValuesForVelocityBean() { }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  displayText      The text to display
   * @param  range            The range
   * @param  nameForElements  Description of Parameter
   */
  public ValuesForVelocityBean(String displayText, String nameForElements, String range) {
    this.displayText = displayText;
    this.range = range;
    this.nameForElements = nameForElements;
  }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  value            The value
   * @param  nameForElements  The name for elements
   * @param  range            The range
   * @param  lower            Upper
   * @param  upper            Lower
   * @param  displayText      The text to display
   */
  public ValuesForVelocityBean(Float value, String nameForElements, double lower, double upper, String range, String displayText) {
    this.nameForElements = nameForElements;
    this.range = range;
    this.value = value;
    this.lower = lower;
    this.upper = upper;
    this.displayText = displayText;
  }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  value            The value
   * @param  nameForElements  The name for elements
   * @param  range            The range
   * @param  lower            Upper
   * @param  upper            Lower
   * @param  displayText      The text to display
   * @param  atom             The atom this line refers to.
   */
  public ValuesForVelocityBean(Float value, String nameForElements, double lower, double upper, String range, String displayText, int atom) {
    this.nameForElements = nameForElements;
    this.range = range;
    this.value = value;
    this.lower = lower;
    this.upper = upper;
    this.displayText = displayText;
    this.atom = atom;
  }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  value            The value
   * @param  nameForElements  The name for elements
   * @param  range            The range
   * @param  lower            Upper
   * @param  upper            Lower
   * @param  displayText      The text to display
   * @param  delta            Description of Parameter
   */
  public ValuesForVelocityBean(Float value, String nameForElements, double lower, double upper, String range, String displayText, String delta) {
    this.nameForElements = nameForElements;
    this.range = range;
    this.value = value;
    this.lower = lower;
    this.upper = upper;
    this.displayText = displayText;
    this.delta = delta;
  }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  value            The value
   * @param  nameForElements  The name for elements
   * @param  range            The range
   * @param  lower            Upper
   * @param  upper            Lower
   * @param  displayText      The text to display
   * @param  isInRange        Says if value is between lower and upper
   */
  public ValuesForVelocityBean(Float value, String nameForElements, double lower, double upper, String range, String displayText, boolean isInRange) {
    this(value, nameForElements, lower, upper, range, displayText);
    setIsInRange(isInRange);
  }


  /**
   *Constructor for the ValuesForVelocityBean object
   *
   * @param  value            The value
   * @param  nameForElements  The name for elements
   * @param  range            The range
   * @param  lower            Upper
   * @param  upper            Lower
   * @param  displayText      The text to display
   * @param  delta            Description of Parameter
   * @param  multis           Description of Parameter
   */
  public ValuesForVelocityBean(Float value, String nameForElements, double lower, double upper, String range, String displayText, String delta, String multis) {
    this.nameForElements = nameForElements;
    this.range = range;
    this.value = value;
    this.lower = lower;
    this.upper = upper;
    this.displayText = displayText;
    this.delta = delta;
    multiplicity = multis;
  }

  public ValuesForVelocityBean(DBSignal signal) { 
	  this.signal=signal;
  }

  /**
   *Sets the value attribute of the ValuesForVelocityBean object
   *
   * @param  f  The new value value
   */
  public void setValue(Float f) {
    value = new Float(f);
  }


  /**
   *Sets the nameForElements attribute of the ValuesForVelocityBean object
   *
   * @param  s  The new nameForElements value
   */
  public void setNameForElements(String s) {
    nameForElements = s;
  }


  /**
   *  Sets the isInRange attribute of the ValuesForVelocityBean object
   *
   * @param  s  The new isInRange value
   */
  public void setIsInRange(boolean s) {
    isInRange = s;
  }


  /**
   *Sets the range attribute of the ValuesForVelocityBean object
   *
   * @param  s  The new range value
   */
  public void setRange(String s) {
    range = s;
  }


  /**
   *Sets the upper attribute of the ValuesForVelocityBean object
   *
   * @param  d  The new upper value
   */
  public void setUpper(float d) {
    upper = d;
  }


  /**
   *Sets the lower attribute of the ValuesForVelocityBean object
   *
   * @param  d  The new lower value
   */
  public void setLower(float d) {
    lower = d;
  }


  /**
   *Sets the displayText attribute of the ValuesForVelocityBean object
   *
   * @param  t  The new displayText value
   */
  public void setDisplayText(String t) {
    displayText = t;
  }


  /**
   *  Sets the currentSelectedNumber attribute of the ValuesForVelocityBean object
   *
   * @param  i  The new currentSelectedNumber value
   */
  public void setCurrentSelectedNumber(Integer i) {
    this.currentSelectedNumber = i;
  }


  /**
   *  Sets the multiplicity attribute of the ValuesForVelocityBean object
   *
   * @param  m  The new multiplicity value
   */
  public void setMultiplicity(String m) {
    multiplicity = m;
  }


  /**
   *  Sets the delta attribute of the ValuesForVelocityBean object
   *
   * @param  delta  The new delta value
   */
  public void setDelta(String delta) {
    this.delta = delta;
  }


  /**
   *  Sets the identifier attribute of the ValuesForVelocityBean object
   *
   * @param  i  The new identifier value
   */
  public void setIdentifier(String i) {
    identifier = i;
  }


  /**
   *  Sets the intensity attribute of the ValuesForVelocityBean object
   *
   * @param  intensity  The new intensity value
   */
  public void setIntensity(String intensity) {
    this.intensity = intensity;
  }


  /**
   *  Sets the couplings attribute of the ValuesForVelocityBean object
   *
   * @param  v  The new couplings value
   */
  public void setCouplings(ArrayList<StringAndStringAndBoolean> v) {
    couplings = v;
  }


  /**
   *  Gets the delta attribute of the ValuesForVelocityBean object
   *
   * @return    The delta value
   */
  public String getDelta() {
    return (delta);
  }


  /**
   *  Gets the delta attribute of the ValuesForVelocityBean object. & escaped
   *
   * @return    The delta value
   */
  public String getDeltaEsc() {
    String prov=GeneralUtils.replace(delta,"%","%25");
    prov=GeneralUtils.replace(prov,"&","%26");
    prov=GeneralUtils.replace(prov,"#","%23");
    return GeneralUtils.replace(prov,"+","%2B");
  }


  /**
   *  Gets the value attribute +1 of the ValuesForVelocityBean object as an Integer.
   *
   * @return    The value as an Integer.
   */
  public Integer getValueAsIntPlus1() {
    return (new Integer(value.intValue()+1));
  }


  /**
   *  Gets the value attribute of the ValuesForVelocityBean object as an Integer.
   *
   * @return    The value as an Integer.
   */
  public Integer getValueAsInt() {
    return (new Integer(value.intValue()));
  }


  /**
   *  Gets the isInRange attribute of the ValuesForVelocityBean object
   *
   * @return    The isInRange value
   */
  public boolean getIsInRange() {
    return (isInRange);
  }


  /**
   *Gets the nameForElements attribute of the ValuesForVelocityBean object
   *
   * @return    The nameForElements value
   */
  public String getNameForElements() {
    return (nameForElements);
  }


  /**
   *Gets the value attribute of the ValuesForVelocityBean object
   *
   * @return    The value value
   */
  public Float getValue() {
    return (value);
  }

  /**
   *Gets the value attribute of the ValuesForVelocityBean object, formatted as string with two digits after ., -- if value=FLoat.MIN_VALUE
   *
   * @return    The value value
   */
  public String getValueAsString() {
	  if(value==Float.MIN_VALUE)
	  	return "--";
	  DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
	  return format.format(value);
  }


  /**
   *Gets the range attribute of the ValuesForVelocityBean object
   *
   * @return    The range value
   */
  public String getRange() {
    return (range);
  }

  
  /**
   *Gets the range attribute of the ValuesForVelocityBean object, removes everything in front of first dash - needed for spectra searches.
   *
   * @return    The range value
   */
  public String getRangeForSpectrum() {
    return (range.substring(range.indexOf("-")+1));
  }
  

  /**
   *Gets the upper attribute of the ValuesForVelocityBean object
   *
   * @return    The upper value
   */
  public double getUpper() {
    return (upper);
  }


  /**
   *Gets the lower attribute of the ValuesForVelocityBean object
   *
   * @return    The lower value
   */
  public double getLower() {
    return (lower);
  }


  /**
   *  Gets the lower attribute of the ValuesForVelocityBean object as an integer
   *
   * @return    The lower value
   */
  public int getLowerAsInt() {
    return (new Float(lower).intValue());
  }


  /**
   *  Gets the identifier attribute of the ValuesForVelocityBean object
   *
   * @return    The identifier value
   */
  public String getIdentifier() {
    return (identifier);
  }


  /**
   *  Gets the identifier attribute of the ValuesForVelocityBean object, & escaped.
   *
   * @return    The identifier value
   */
  public String getIdentifierEsc() {
    String prov=GeneralUtils.replace(delta,"%","%25");
    prov=GeneralUtils.replace(prov,"&","%26");
    prov=GeneralUtils.replace(prov,"&","%23");
    return GeneralUtils.replace(prov,"+","%2B");
  }


  /**
   *Gets the displayText attribute of the ValuesForVelocityBean object
   *
   * @return    The displayText value
   */
  public String getDisplayText() {
    return (displayText);
  }


  /**
   *Gets the displayText attribute of the ValuesForVelocityBean object, removing all html formatting.
   *
   * @return    The displayText value
   */
  public String getDisplayTextNoFormatting() {
    String provtext = displayText.replaceAll("\\<[^>]*>","");
    return provtext.replace("'", "\\'");
  }

  /**
   *  Gets the currentSelectedNumber attribute of the ValuesForVelocityBean object
   *
   * @return    The currentSelectedNumber value
   */
  public Integer getCurrentSelectedNumber() {
    return (currentSelectedNumber);
  }


  /**
   *  Gets the multiplicity attribute of the ValuesForVelocityBean object
   *
   * @return    The multiplicity value
   */
  public String getMultiplicity() {
    return (multiplicity);
  }


  /**
   *  Gets the range attribute formatted without the assignemnts and intensities, if zero.
   *
   * @return    The formatted range.
   */
  public String getRangeWithoutZeros(RunData runData) throws Exception{
    if ((displayText.equals(NmrshiftdbConstants.TOTALSPECTRUM) || displayText.equals(NmrshiftdbConstants.SUBSPECTRUM)) && getRange().indexOf("-")>0) {
      String specfile=GeneralUtils.makeSimpleSpecfile(getRangeForSpectrum(), true);
      if(specfile.length()>20)
    	  return getRange().substring(0,getRange().indexOf("-"))+" "+specfile.substring(0,20)+"...";
      else
    	  return (getRange().substring(0,getRange().indexOf("-"))+" "+specfile);
    } else if((displayText.equals("similarity search") || displayText.equals("substructure search") || displayText.equals("identity search")) && runData!=null){
      String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "history.jpg";
      GeneralUtils.makeJpg(range,"/"+filename,runData.getServletConfig(),100,100,false,null,null, Color.LIGHT_GRAY);
      return("<img src=\""+filename+"\">");
    } else if(displayText.equals(NmrshiftdbConstants.SAMPLE_EXPERIMENTS)){
    	StringBuffer experiments = new StringBuffer();
    	StringTokenizer st = new StringTokenizer(range);
    	while(st.hasMoreTokens()){
    		experiments.append(DBExperimentPeer.retrieveByPK(Integer.parseInt(st.nextToken())).getName()+" ");
    	}
    	return experiments.toString().trim();
    } else {
      return (new UrlTool().escapeHtml(range));
    }
  }


  /**
   *  Gets the intensity attribute of the ValuesForVelocityBean object
   *
   * @return    The intensity value
   */
  public String getIntensity() {
    return (intensity);
  }


  /**
   *  Gets the intensity attribute of the ValuesForVelocityBean object
   *
   * @return    The intensity value
   */
  public String getIntensityEsc() {
    String prov=GeneralUtils.replace(intensity,"%","%25");
    prov=GeneralUtils.replace(prov,"&","%26");
    prov=GeneralUtils.replace(prov,"#","%23");
    return GeneralUtils.replace(prov,"+","%2B");
  }
  
  /**
   * Tells if the ith values of the statistics attribute of the ValuesForVelocityBean object is smaller than value.
   * 
   * @param i index.
   * @param value The value to compare to.
   * @return true=less, false=equals or more.
   */
  public boolean statisticsIsLessThan(int i, double value) {
	  if(statistics==null || statistics[i]==Float.MIN_VALUE)
		  return false;
	  else
		  return statistics[i]<value;
  }
  
  /**
   * Tells if the difference between statics[i] and statistics[k] is more than value.
   * 
   * @param i
   * @param k
   * @param value
   * @return
   */
  public boolean statisticsDiffMoreThan(int i, int k, double value){
      if(statistics==null)
          return false;
      else
          return Math.abs(statistics[i]-statistics[k])>value;
  }
  
  /**
   *  Gets the ith values of the statistics attribute of the ValuesForVelocityBean object.
   *  values are formatted with 2 digits after comma.
   *
   * @param  i  index.
   * @return    The ith statistics value.
   */
  public String getStatistics(int i) {
    if (statistics == null) {
      return ("?");
    }
    if(Double.isNaN(statistics[i]))
    	return "--";
    DecimalFormat format;
    if (i == 4 || i == 5) {
      format = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
    } else {
      format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    }
    if(i==3 && statistics[3]==Float.MIN_VALUE){
    		return "--";
    }
    if (statistics[4] == -1 && i == 4) {
      return ("cdk");
    }
    if (i==5 && statistics[5]==1000) {
      return (">999");
    }
    return (format.format(statistics[i]));
  }


  /**
   *  Gets the ith values of the statistics2 attribute of the ValuesForVelocityBean object.
   *
   * @param  i  index.
   * @return    The ith statistics value.
   */
  public String getStatistics2(int i) {
    if (statistics2 == null) {
      return ("--");
    }
    DecimalFormat format;
    if (i == 4 || i == 5) {
      format = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
    } else {
      format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    }
    if (statistics2[4] == -1 && i == 4) {
      return ("cdk");
    }
    if (i==5 && statistics2[5]==1000) {
      return (">999");
    }
    return (format.format(statistics2[i]));
  }


  /**
   *  Gets the atom attribute of the ValuesForVelocityBean object
   *
   * @return    The atom value
   */
  public int getAtom() {
    return (atom);
  }
  

	/**
	 * Sets the atom attribute of the ValuesForVelocityBean object
	 * 
	 * @param atom
	 */
	public void setAtom(int atom) {
		this.atom=atom;		
	}


  /**
   *  Gets the couplings attribute of the ValuesForVelocityBean object
   *
   * @return    The couplings value
   */
  public ArrayList<StringAndStringAndBoolean> getCouplings() {
    return (couplings);
  }


  /**
   * Gets the csearch attribute of the ValuesForVelocityBean object.
   * 
   * @return The csearch value.
   */
  public String getCsearch() {
	return csearch;
  }


  /**
   * Sets the csearch attribute of the ValuesForVelocityBean object.
   * 
   * @param csearch The csearch value.
   */
  public void setCsearch(String csearch) {
	this.csearch = csearch;
  }

	/**
	 * Gets the signal value.
	 * 
	 * @return The signal value.
	 */
	public DBSignal getSignal() {
		return signal;
	}
	
	/**
	 * Sets the signal value.
	 * 
	 * @param signal The signal value.
	 */
	public void setSignal(DBSignal signal) {
		this.signal = signal;
	}
}

