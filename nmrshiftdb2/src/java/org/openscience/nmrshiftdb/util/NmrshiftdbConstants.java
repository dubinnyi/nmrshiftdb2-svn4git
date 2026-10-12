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
package org.openscience.nmrshiftdb.util;

import java.util.Arrays;
import java.util.List;

public class NmrshiftdbConstants{
	//These are the possible review flags in table Spectrum
	public static String EDITED="edited";
	public static String TRUE="true";
	public static String FALSE="false";
	public static String FALSE_OM = "false_om";
	public static String REJECTED="rejected";
  public static String CHANGE="change";
  public static String PRIVATE="private";
  //This are possible entries in SearchHistory
  public static String CALCULATEDONLY="calculated spectra only";
  public static String MEASUREDONLY="measured spectra only";
  public static String JCAMPONLY="full spectra only";
  public static String RAWONLY="raw spectra only";
  public static String EXACT="exact";
  public static String REGEXP="regular expression";
  public static String FUZZY="fuzzy";
  public static String FRAGMENT="fragment";
  public static String SUBSPECTRUM="subspectrum search";
  public static String TOTALSPECTRUM="total similarity spectrum search";
  public static String SUBSTRUCTURE_SIMILARITY="similarity search";
  public static String SUBSTRUCTURE_EXACT="substructure search";
  public static String TOTALSTRUCTURE="identity search";
  public static String MOLECULEKEYWORDS_TOTAL="search by molecule keyword";
  public static String SPECTRUMKEYWORDS_TOTAL="search by spectrum keyword";
  public static String MOLECULEKEYWORDS_FRAGMENT="search by molecule keyword fragements";
  public static String SPECTRUMKEYWORDS_FRAGMENT="search by spectrum keyword fragements";
  public static String LITERATURE_TITLE="literature/title";
  public static String LITERATURE_AUTHOR="literature/author";
  public static String COMMENT="comment";
  public static String CANNAME="canonical name";
  public static String SPECLINK="description of spectrum link";
  public static String MOLLINK="description of molecule link";
  public static String MOLKEY="molecule keyword";
  public static String SPECKEY="spectrum keyword";
  public static String CASNUMBER="CAS number";
  public static String CHEMNAME="chemical name";
  public static String CHEMNAMEPUBCHEM="chemical name using Pubchem";
  public static String FORMULA="chemical formula";
  public static String FORMULA_WITH_OTHER="chemical formula (with other elements)";
  public static String MULTIPLICITY="multiplicities";
  public static String POTMULTIPLICITY="potential multiplicities";
  public static String MYSPECTRA="my spectra";
  public static String BROWSE="browse all structures";
  public static String BROWSESPEC="browse all spectra";
  public static String WEIGHT="molecular weight search";
  public static String CONDITIONS="condition search";
  public static String MOLECULE_NR="molecule id";
  public static String SPECTRUM_NR="spectrum id";
  public static String HOSECODE="HOSE code";
  public static String DBE="double bond equivalents";
  public static String SSSR="number of rings in smallest set of smallest rings";
  public static String DBE_RINGS="double bond equivalents/smallest set of smallest rings";
  public static String SUBSTCOMMENT="substance comment";
  public static String SUBSTKEYWORD="substance keyword";
  //Sample fields
  public static String SAMPLE_USERS_ID="Sample I. D.";
  public static String SAMPLE_KEYWORD="Keyword";
  public static String SAMPLE_CARE="Special care";
  public static String SAMPLE_EXPERIMENTS="Experiments";
  //Actions
  public static String UPLOAD_JCAMP_SEARCH="uploadjcampsearch";
  //Session attributes
  public static String STRUCTURESHISTORY="structureshistory";
  public static String STRUCTURESHISTORY_SMILES="structureshistory_smiles";
  public static String SEARCHHISTORY="searchhistory";
  public static String INPUT="input";
  public static String MOLFILE="molfile";
  public static String MOLFILEH="molfileh";
  //Type of literature
  public static String JOURNAL_ARTICLE="journal_article";
  public static String MONOGRAPH="monograph";
  public static String OTHER_ARTICLE="other_article";
  
  public static String UNREPORTED="Unreported";
  public static String UNKNOWN="Unknown";
  
  public static String METADATA="metadata";
  public static String SUBSTANCE="substance";
  public static String CONDITION="condition";
  
  //These are used for handling of properties in cml submits. need to duplicate the Bc_nmrshfitdbConstants in bc_nmrshiftdb of bioclipse
	public static String frequency="jcamp:dotOBSERVEFREQUENCY";
	public static String solvent="jcamp:dotSOLVENTNAME";
	public static String temperature="jcamp:TEMPERATURE";
	public static String assignment="nmr:assignmentMethod";
	public static String nucleus="nmr:OBSERVENUCLEUS";
	public static String nmrid="nmr:nmrshiftdbid";
	
	public static String CML_NAMESPACE="http://www.xml-cml.org/schema";
	
	public static String PROCESS_SELF="self";
	public static String PROCESS_OPERATOR="worker";
	public static String PROCESS_CHANGER="robot";
	public static String PROCESS_BARCODE="barcode";
	
	//keys used in db for conditions
	public static String FREQUENCY="Field Strength [MHz]";
	public static String TEMPERATURE="Temperature [K]";
  
	  public static final String SEARCH_TYPE = "search_type";
	  public static final String STRUCCENT = "struccent";
	  public static final String SPECCENT = "speccent";
	  public static final String MIXCENT = "mixcent";
	  public static final String SAMPLECENT = "samplecent";
	  public static final String SUBCENT = "substcent";

  public static final String UNASSIGNED_SPECTRA_ALLOWED = "unassigned_spectra_allowed";
  public static final String MIXTURE_SEARCH_POSSIBLE = "mixture_search_possible";

  public static final String JCAMP_STRICT = "jcamp_strict";
  public static final String LABGROUP_ONLY = "labgroup_only";
  public static final List<String> METALS = Arrays.asList("Hf","Co","Zr","W","Ni","Pt","Os","W","Ti","Ni","Rh","Fe");

  //These are needed for velocity which cannot read attributes
  public String getSUBSPECTRUM(){
    return(SUBSPECTRUM);
  }
  public String getTOTALSPECTRUM(){
    return(TOTALSPECTRUM);
  }
  public String getFUZZY(){
    return(FUZZY);
  }
  public String getEXACT(){
    return(EXACT);
  }
  public String getREGEXP(){
    return(REGEXP);
  }
  public String getFRAGMENT(){
    return(FRAGMENT);
  }
  public String getLITERATURE_TITLE(){
    return(LITERATURE_TITLE);
  }
  public String getLITERATURE_AUTHOR(){
    return(LITERATURE_AUTHOR);
  }
  public String getCOMMENT(){
    return(COMMENT);
  }
  public String getCANNAME(){
    return(CANNAME);
  }
  public String getSPECLINK(){
    return(SPECLINK);
  }
  public String getMOLLINK(){
    return(MOLLINK);
  }
  public String getMOLKEY(){
    return(MOLKEY);
  }
  public String getSPECKEY(){
    return(SPECKEY);
  }
  public String getCASNUMBER(){
    return(CASNUMBER);
  }
  public String getCHEMNAME(){
    return(CHEMNAME);
  }
  public String getCHEMNAMEPUBCHEM(){
	    return(CHEMNAMEPUBCHEM);
  }
  public String getFORMULA(){
    return(FORMULA);
  }
  public String getFORMULA_WITH_OTHER(){
	    return(FORMULA_WITH_OTHER);
  }
  public String getMULTIPLICITY(){
    return(MULTIPLICITY);
  }
  public String getPOTMULTIPLICITY(){
    return(POTMULTIPLICITY);
  }
  public String getMYSPECTRA(){
    return(MYSPECTRA);
  }
  public String getSUBSTRUCTURE_SIMILARITY(){
    return(SUBSTRUCTURE_SIMILARITY);
  }
  public String getSUBSTRUCTURE_EXACT(){
    return(SUBSTRUCTURE_EXACT);
  }
  public String getTOTALSTRUCTURE(){
    return(TOTALSTRUCTURE);
  }
  public String getBROWSE(){
    return(BROWSE);
  }
  public String getBROWSESPEC(){
	  return BROWSESPEC;
  }
  public String getSPECTRUM_NR(){
    return(SPECTRUM_NR);
  }
  public String getMOLECULE_NR(){
    return(MOLECULE_NR);
  }
  public String getHOSECODE(){
    return(HOSECODE);
  }
  public String getDBE_RINGS(){
	  return DBE_RINGS;
  }
  public String getWEIGHT(){
	  return WEIGHT;
  }
  public static String getSUBSTCOMMENT() {
	  return SUBSTCOMMENT;
  }
  public static String getSUBSTKEYWORD() {
	  return SUBSTKEYWORD;
  }
  public static String getFREQUENCY() {
	  return FREQUENCY;
  } 
  public static String getSAMPLE_USERS_ID() {
	  return SAMPLE_USERS_ID;
  }
  public static String getSAMPLE_KEYWORD() {
	  return SAMPLE_KEYWORD;
  }
  public static String getSAMPLE_CARE() {
	  return SAMPLE_CARE;
  }
}
