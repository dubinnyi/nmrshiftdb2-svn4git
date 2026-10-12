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

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Point2d;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainerCreator;
import org.openscience.cdk.isomorphism.matchers.QueryBond;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.DeAromatizationTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.om.DBMixture;
import org.openscience.nmrshiftdb.om.DBMixturePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBShift;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.comparator.StringAndIntComparator;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.element.CMLPeak;

import com.workingdogs.village.DataSetException;

import uk.ac.ebi.orchem.fingerprint.OrchemFingerprinter;

public class SearchUtils {
	
	  /**
	   *  Maximum number of hits to display
	   */
	  public final static int MAX_HITS_TO_DISPLAY = 300;
	  
	  /**
	   *  Executes searches from the searchHistory.
	   *
	   * @param  searches                 The searches to execute. Each figure in searches means the search in this position in sarchHistory will be executed, all searches are AND linked.
	   * @param  searchHistory            The search history.
	   * @param  chiralMessage            Return messages about chiral/non-chiral structure search.
	   * @param  similarities             Holds similarites (as Doubles) in case similarity search was done (must be empty ArrayList).
	   * @param  calculatedonly           Search only calculated spectra.
	   * @param  measuredonly             Search only measured spectra.
	   * @param  runData                  Current runData, null if used in client.
       * @param  path                     Path to normalizer.xml file.
       * @param  not                      If true, the overall query will get a not.
       * @param  andor                    true=queries (if several executed) will be and-linked, false=or-linked
       * @param  spectrumTypes            The spectrum types to search for.
       * @param  logtosql                 should query be logged?
       * @param  message                  Will contain a message about duration of query.
       * @param  jcamponly                Only spectra with a jcamp file get searched.
       * @param  loggedInUser             the currently logged in user (used for private data, if null, no private data is found)
       * @param  labgrouponly             Only spectra from the users labgroup get searched.
	   * @return                          ArrayList of molecule ids (as Strings).
	   * @exception  Exception            Database problems
	   * @exception  NmrshiftdbException  Description of Exception
	   */
	  public static List<String> executeSearch(ArrayList<ValuesForVelocityBean> searchHistory, int[] searches, StringAndInt chiralMessage, ArrayList<Double> similarities, boolean calculatedonly, boolean measuredonly, RunData runData, String path, boolean not, boolean andor, List<DBSpectrumType> spectrumTypes, boolean logtosql, StringBuffer message, boolean jcamponly, boolean rawonly, BaseJetspeedUser loggedInUser) throws NmrshiftdbException, Exception {
		    return executeSearch(searchHistory, searches, chiralMessage, similarities, calculatedonly, measuredonly, runData, path, not, andor, spectrumTypes, logtosql, message, jcamponly, rawonly, loggedInUser, runData.getSession()!=null && runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)!=null && ((Boolean)runData.getSession().getAttribute(NmrshiftdbConstants.LABGROUP_ONLY)).booleanValue()==true);
	  }
	  
	  
	  /**
	   *  Executes searches from the searchHistory.
	   *
	   * @param  searches                 The searches to execute. Each figure in searches means the search in this position in sarchHistory will be executed, all searches are AND linked.
	   * @param  searchHistory            The search history.
	   * @param  chiralMessage            Return messages about chiral/non-chiral structure search.
	   * @param  similarities             Holds similarites (as Doubles) in case similarity search was done (must be empty ArrayList).
	   * @param  calculatedonly           Search only calculated spectra.
	   * @param  measuredonly             Search only measured spectra.
	   * @param  runData                  Current runData, null if used in client.
	   * @param  path                     Path to normalizer.xml file.
	   * @param  not                      If true, the overall query will get a not.
	   * @param  andor                    true=queries (if several executed) will be and-linked, false=or-linked
	   * @param  spectrumTypes            The spectrum types to search for.
	   * @param  logtosql                 should query be logged?
	   * @param  message                  Will contain a message about duration of query.
	   * @param  jcamponly                Only spectra with a jcamp file get searched.
	   * @param  loggedInUser             the currently logged in user (used for private data, if null, no private data is found)
	   * @param  labgrouponly             Only spectra from the users labgroup get searched.
	   * @return                          ArrayList of molecule ids (as Strings).
	   * @exception  Exception            Database problems
	   * @exception  NmrshiftdbException  Description of Exception
	   */
	  public static List<String> executeSearch(ArrayList<ValuesForVelocityBean> searchHistory, int[] searches, StringAndInt chiralMessage, ArrayList<Double> similarities, boolean calculatedonly, boolean measuredonly, RunData runData, String path, boolean not, boolean andor, List<DBSpectrumType> spectrumTypes, boolean logtosql, StringBuffer message, boolean jcamponly, boolean rawonly, BaseJetspeedUser loggedInUser, boolean labgrouponly) throws NmrshiftdbException, Exception {
		//This is to avoid errors in case somebody has not got the spectrum choice portlet in his ui
		if(spectrumTypes==null)
			spectrumTypes=DBSpectrumTypePeer.doSelect(new Criteria());
	    String concatenator = " AND ";
	    if (!andor) {
	      concatenator = " OR ";
	    }
	    //A map used for building the sql statement
	    HashMap<String,String> fields = new HashMap<String,String>();
	    fields.put(NmrshiftdbConstants.LITERATURE_AUTHOR, "AUTHOR.NAME_TOTAL");
	    fields.put(NmrshiftdbConstants.LITERATURE_TITLE, "LITERATURE.TITLE_TOTAL");
	    fields.put(NmrshiftdbConstants.COMMENT, "SPECTRUM.COMMENT");
	    fields.put(NmrshiftdbConstants.CANNAME, "CANONICAL_NAME.NAME");
	    fields.put(NmrshiftdbConstants.SPECLINK, "SPECTRUM_HYPERLINK.DESCRIPTION");
	    fields.put(NmrshiftdbConstants.MOLLINK, "MOLECULE_HYPERLINK.DESCRIPTION");
	    fields.put(NmrshiftdbConstants.CASNUMBER, "MOLECULE.CAS_NUMBER");
	    fields.put(NmrshiftdbConstants.CHEMNAME, "CHEMICAL_NAME.NAME");
	    fields.put(NmrshiftdbConstants.CHEMNAMEPUBCHEM, "CANONICAL_NAME.NAME");
	    fields.put(NmrshiftdbConstants.MOLKEY, "MK.KEYWORD");
	    fields.put(NmrshiftdbConstants.SPECKEY, "SK.KEYWORD");
	    fields.put(NmrshiftdbConstants.SPECTRUM_NR, "SPECTRUM.SPECTRUM_ID");
	    fields.put(NmrshiftdbConstants.MOLECULE_NR, "MOLECULE.MOLECULE_ID");
	    fields.put(NmrshiftdbConstants.HOSECODE, "ATOM.HOSE_CODE");
	    fields.put(NmrshiftdbConstants.DBE, "MOLECULE.DBE");
	    fields.put(NmrshiftdbConstants.SSSR, "MOLECULE.SSSR");
	    fields.put(NmrshiftdbConstants.SUBSTCOMMENT, "SUBSTANCE.SUBSTANCE_COMMENT");
	    fields.put(NmrshiftdbConstants.SUBSTKEYWORD, "SUK.KEYWORD");
	    fields.put(NmrshiftdbConstants.SAMPLE_USERS_ID, "SAMPLE.USERS_ID");
	    fields.put(NmrshiftdbConstants.SAMPLE_KEYWORD, "SAMPLE.USERS_ID_COMMENT");
	    fields.put(NmrshiftdbConstants.SAMPLE_CARE, "SAMPLE.SPECIAL_CARE");
        //We determine searchtype
        boolean struccent=false;
        boolean mixcent = false;
        boolean speccent = false;
        boolean samplecent=false;
        boolean substcent=false;
        ValuesForVelocityBean lastSearchStruc=null;
        ValuesForVelocityBean lastSearchMix=null;
        ValuesForVelocityBean lastSearchSample=null;
        ValuesForVelocityBean lastSearchSpec=null;
        for (int i = 0; i < searches.length; i++) {
            if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT)  || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSTRUCTURE) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY)){
                if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals("mixture")){
                    mixcent=true;
                    lastSearchMix=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }else{
                    struccent=true;
                    lastSearchStruc=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }
            }
            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_TOTAL) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_FRAGMENT)) {
                struccent=true;
                lastSearchStruc=(ValuesForVelocityBean) searchHistory.get(searches[i]);
            }
            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)  || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CONDITIONS) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_FRAGMENT) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_TOTAL) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECKEY) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA) || notAllSelected(spectrumTypes)) {
                speccent=true;
                if(GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED, runData).equals(NmrshiftdbConstants.TRUE)){
                	lastSearchSpec=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }else{
                	lastSearchStruc=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }
            }
            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAME) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CASNUMBER)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA_WITH_OTHER)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CASNUMBER)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CANNAME)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLLINK)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLKEY)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLECULE_NR)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.WEIGHT)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SSSR)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.DBE)){
                struccent=true;
                lastSearchStruc=(ValuesForVelocityBean) searchHistory.get(searches[i]);
            }
            if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.LITERATURE_AUTHOR)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.LITERATURE_TITLE)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.COMMENT)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECLINK)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECLINK)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUM_NR)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT)){
                speccent=true;
                if(GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED, runData).equals(NmrshiftdbConstants.TRUE)){
                    lastSearchSpec=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }else{
                    lastSearchStruc=(ValuesForVelocityBean) searchHistory.get(searches[i]);
                }
            }
            if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_EXPERIMENTS)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_CARE)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_KEYWORD)|| ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_USERS_ID)){
                samplecent=true;
                lastSearchSample=(ValuesForVelocityBean) searchHistory.get(searches[i]);
            }
            if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTKEYWORD)){
                substcent=true;
                samplecent=false;
                speccent=false;
                struccent=false;
                lastSearchSample=(ValuesForVelocityBean) searchHistory.get(searches[i]);
            }
        }
        //we always need the spectrum to make sure private structures (=those with only private spectra) are not found by other users
        if(struccent)
            speccent=true;
        //samples and spectra are linked via structure
        if(samplecent && speccent && !struccent){
            struccent=true;
        }
        //there is a hierarchy here, specifically if any structure search is involved, the whole search becomes a structure search
        //a real spectrum centric search is only necessary if unassigned spectra are allowed and the "only assigned" option is not chosen, else we still do a struccent search
        String searchtype=NmrshiftdbConstants.STRUCCENT;
        if(speccent && (GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED, runData).equals(NmrshiftdbConstants.TRUE) && !( runData.getSession().getAttribute("assignedonly")!=null && ((Boolean)runData.getSession().getAttribute("assignedonly")).booleanValue()))){
            searchtype = NmrshiftdbConstants.SPECCENT;
            if(runData!=null)
                runData.getSession().setAttribute("lastsearch",lastSearchSpec);
        }else if(speccent){
            searchtype = NmrshiftdbConstants.STRUCCENT;
            if(runData!=null)
                runData.getSession().setAttribute("lastsearch",lastSearchStruc);
            struccent=true;
        }
        if(mixcent){
            searchtype = NmrshiftdbConstants.MIXCENT;
            if(runData!=null)
               runData.getSession().setAttribute("lastsearch",lastSearchMix);
            }
        if(struccent){
            searchtype = NmrshiftdbConstants.STRUCCENT;
            if(runData!=null)
                runData.getSession().setAttribute("lastsearch",lastSearchStruc);
        }
        if(samplecent){
            searchtype = NmrshiftdbConstants.SAMPLECENT;
            if(runData!=null)
                runData.getSession().setAttribute("lastsearch",lastSearchSample);
        }
        if(substcent){
            searchtype = NmrshiftdbConstants.SUBCENT;
            if(runData!=null)
                runData.getSession().setAttribute("lastsearch",lastSearchSample);
        }
        //for the join, we need structure if mixsearch is done (the search is still for mixtures);
        if(mixcent)
            struccent=true;
        if(runData!=null && runData.getSession()!=null)
            runData.getSession().setAttribute(NmrshiftdbConstants.SEARCH_TYPE, searchtype);
	    ArrayList<String> v = new ArrayList<String>();
	    boolean isAChiralSearch = false;
	    boolean chiral = false;
	    for (int i = 0; i < searches.length; i++) {
	      //if user searches for an edited spectrum we replace its id by the id of the current spectrum
	      if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUM_NR) && ((ValuesForVelocityBean) searchHistory.get(searches[i])).getLowerAsInt()!=0) {
	          DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(Integer.parseInt(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange()));
	          if(spectrum.getReviewFlag().equals(NmrshiftdbConstants.EDITED)){
	              ((ValuesForVelocityBean) searchHistory.get(searches[i])).setRange(Integer.toString(spectrum.getCurrentSpectrum().getSpectrumId()));
	          }
	      }
          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSTRUCTURE)) {
        	String mdl=((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
            //this is to handle dative bonds for organometals
        	IAtomContainer molWithH = AtomUtils.readMolBothVersions(mdl);
	        AtomUtils.normalize(molWithH, path);
	        for(int k=0;k<molWithH.getAtomCount();k++){
	          if(molWithH.getAtom(k).getPoint2d()==null)
	            molWithH.getAtom(k).setPoint2d(new Point2d(molWithH.getAtom(k).getPoint3d().x,molWithH.getAtom(k).getPoint3d().y));
	          }
	          for(IBond bond : molWithH.bonds()) {
	            if(bond.getOrder()==null)
	              bond.setOrder(IBond.Order.ZERO);
	          }
	          AtomUtils.addAndPlaceHydrogens(molWithH);
	          ElectronDonation model       = ElectronDonation.cdk();
	          CycleFinder      cycles      = Cycles.cdkAromaticSet();
	          Aromaticity      aromaticity = new Aromaticity(model, cycles);
	          aromaticity.apply(molWithH);
	          String inchi=GeneralUtils.generateInchi(molWithH,false);
			  if(!AtomUtils.isOrganometallic(molWithH)) {
		        if(StringUtils.countMatches(inchi, "/")>3){
		             isAChiralSearch = true;
		             chiral = true;
		        }
			  }else {
				  chiral=true;
			  }
	      }
	    }
	    while (true) {
		      StringBuffer query = new StringBuffer();
		      //We handle single structure/substructure queries separately using the HEAP tables. If combined queries are done, we use the normal tables (joins needed).
		      if (searches.length == 1 && (((ValuesForVelocityBean) searchHistory.get(searches[0])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY) || ((ValuesForVelocityBean) searchHistory.get(searches[0])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT)) && runData != null) {
		    	IAtomContainer molWithH=doStructure(((ValuesForVelocityBean) searchHistory.get(searches[0])).getRange(), path);
			    BitSet bsor = new OrchemFingerprinter().getFingerprint(molWithH);
		        BitSet bs=new BitSet(OrchemFingerprinter.FINGERPRINT_SIZE+2);
		        bs.or(bsor);
		        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE, AtomUtils.hasOrganometall(molWithH));
		        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE+1, AtomUtils.hasZeroBond(molWithH));
		        StringBuffer fingerprintstring = new StringBuffer();
		        fingerprintstring.append("(");
		        for (int i = 0; i < 16; i++) {
		          fingerprintstring.append("fp" + i + ",");
		        }
		        for (int i = 0; i < 15; i++) {
		          fingerprintstring.append(AtomUtils.getBigIntegerValue(bs, i).longValue() + ",");
		        }
		        fingerprintstring.append(AtomUtils.getBigIntegerValue(bs, 15).longValue() + ")");
		        if(searchtype.equals(NmrshiftdbConstants.MIXCENT)){
		        	query.append("SELECT DISTINCT MIXTURE.MIXTURE_ID " + (((ValuesForVelocityBean) searchHistory.get(searches[0])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY) ? ", tanimoto_coefficient" + fingerprintstring + " tani" : "") + " FROM MIXTURE join MIXTURE_MOLECULE using (MIXTURE_ID) join FINGERPRINTS using (MOLECULE_ID) ");
		        }else{
		        	query.append("SELECT DISTINCT FINGERPRINTS.MOLECULE_ID " + (((ValuesForVelocityBean) searchHistory.get(searches[0])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY) ? ", tanimoto_coefficient" + fingerprintstring + " tani" : "") + " FROM FINGERPRINTS ");
		        }
		        query.append("join SPECTRUM using (MOLECULE_ID) ");
		        //labgroup only
		        if (labgrouponly) {
		        	query.append("join TURBINE_USER using (USER_ID) join USER_HISTORY on TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and LEAVE_DATE is null ");
		        }
                if(jcamponly || rawonly){
                    query.append("left join SPECTRUM_FILE using (SPECTRUM_ID) ");
                }
                query.append("WHERE ");
		        if(notAllSelected(spectrumTypes)){
		        	query.append(" (");
			        for(int i=0;i<spectrumTypes.size();i++){
			        	if(((DBSpectrumType)spectrumTypes.get(i)).getChoosen())
			        		query.append("SPECTRUM.SPECTRUM_TYPE_ID=" + (((DBSpectrumType)spectrumTypes.get(i)).getSpectrumTypeId()==99 ? 1 : ((DBSpectrumType)spectrumTypes.get(i)).getSpectrumTypeId()) + " OR ");
			        }
			        query=new StringBuffer(query.substring(0,query.length()-3));
			        query.append(") and ");
		        }
	        	query.append("fingerprint_compare");
	        	query.append(fingerprintstring + " = 'Y'");
		        //labgroup only
		        if (labgrouponly) {
		        	query.append("and LABGROUP_ID>0 ");
		        }
                if(rawonly){
                    query.append("(SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') and ");
                }
		        if(jcamponly){
		        	query.append(" and (SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') ");
		        }
                query.append(" and (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.EDITED + "'");
                if(loggedInUser!=null && !loggedInUser.getUserName().equals("anon")){
                    query.append("or (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+loggedInUser.getUserId()+") ");
                }
                query.append(") ");
		        if (((ValuesForVelocityBean) searchHistory.get(searches[0])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY)) {
		          query.append(" order by tani desc");
		        }
		        query.append(";");
		      } else {
		    	if(searchtype.equals(NmrshiftdbConstants.STRUCCENT)){
		      		query.append("SELECT DISTINCT MOLECULE.MOLECULE_ID");
		      	}else if(searchtype.equals(NmrshiftdbConstants.MIXCENT)){
		      		query.append("SELECT DISTINCT MIXTURE.MIXTURE_ID");
		      	}else if(searchtype.equals(NmrshiftdbConstants.SPECCENT)){
		      		query.append("SELECT DISTINCT SPECTRUM.SPECTRUM_ID");
		      	}else if(searchtype.equals(NmrshiftdbConstants.SAMPLECENT)){
		      		query.append("SELECT DISTINCT SAMPLE.SAMPLE_ID");
		      	}else if(searchtype.equals(NmrshiftdbConstants.SUBCENT)){
		      		query.append("SELECT DISTINCT SUBSTANCE.SUBSTANCE_ID");
		      	}
		    	int spectrumSearches=0;
		        for (int i = 0; i < searches.length; i++) {
		        	//if a pubchem name resolution is aked and has not yet been done (the input not being an inchi) we do this
		        	if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM) && ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().indexOf("InChI=")==-1){
		        		((ValuesForVelocityBean) searchHistory.get(searches[i])).setNameForElements(NmrshiftdbConstants.EXACT);
		        	}
		        	String function="spectrumsimilarity";
		        	String field="SIMPLE_SPECFILE";
		        	 if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().indexOf("-")>0 && ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().substring(0,((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().indexOf("-")).equals("13C/1H correlation")) {
		        		function="spectrumsimilarity2d";
		        		field="CORRELATION_SPECFILE";
		        	}
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) && runData != null) {
		            query.append(", "+function+"(SPECTRUM_"+spectrumSearches+"."+field+",'" + ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRangeForSpectrum() + "', 'subspectrum') as similarity");
		            spectrumSearches++;
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) && runData != null) {
		            query.append(", "+function+"(SPECTRUM_"+spectrumSearches+"."+field+",'" + ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRangeForSpectrum() + "', 'total') as similarity");
		            spectrumSearches++;
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FUZZY) & !(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA_WITH_OTHER) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY))) {
		            query.append(", " + fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()));
		          }
		        }
		        query.append(" FROM ");
		        if(samplecent){
		        	query.append("SAMPLE ");
			        for (int i = 0; i < searches.length; i++) {
				          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_EXPERIMENTS)) {
				        	  query.append(" join EXPERIMENT_SAMPLE using (SAMPLE_ID) ");
				        	  break;
				          }
			        }
			        query.append(", ");
		        }
		        if(struccent){
		        	query.append("MOLECULE, "); 
		        }
		        if(mixcent){
		        	query.append("MIXTURE, MIXTURE_MOLECULE, ");
		        }
		        if(speccent){
                    spectrumSearches=0;
                    for (int i = 0; i < searches.length; i++) {
                      if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) {
                            query.append("SPECTRUM as SPECTRUM_"+spectrumSearches+" ");
                            if(jcamponly || rawonly){
                                query.append("left join SPECTRUM_FILE using (SPECTRUM_ID)");
                            }
                            query.append(", ");
                            spectrumSearches++;
                      }
                    }
                    if(spectrumSearches==0 || searches.length>spectrumSearches || calculatedonly || measuredonly){
                        query.append("SPECTRUM ");
                        if(jcamponly || rawonly){
                            query.append("left join SPECTRUM_FILE using (SPECTRUM_ID)");
                        }
                        query.append(", ");
                    }
		        }
		        if(substcent){
		        	query.append("SUBSTANCE, SPECTRUM, ");
		        }
		        query.delete(query.length()-2,query.length());
		        ArrayList<String> simplehistory = new ArrayList<String>();
		        for (int i = 0; i < searches.length; i++) {
		          if (!simplehistory.contains(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText())) {
		            simplehistory.add(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText());
		          }
		        }
		        for (int i = 0; i < simplehistory.size(); i++) {
		          //Multiplicty search
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.MULTIPLICITY)) {
		            Map<String,String[]> map = AtomUtils.parseFuzzyCriteria(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), false, true);
		            Iterator<String> it = map.keySet().iterator();
		            while (it.hasNext()) {
		              String key = (String) it.next();
		              if (!((String[]) map.get(key))[0].equals("0") || !((String[]) map.get(key))[1].equals("0")) {
		                query.append(" LEFT JOIN PC AS " + key + "_PC ON");
		                query.append(" SPECTRUM.SPECTRUM_ID = " + key + "_PC.SPECTRUM_ID ");
		              }
		            }
		            query.append(" LEFT JOIN d2 ON SPECTRUM.SPECTRUM_ID = d2.SPECTRUM_ID ");
		          }
		          //show my spectra or from labgroup only
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.MYSPECTRA) || labgrouponly) {
		            query.append(" left join TURBINE_USER on TURBINE_USER.USER_ID = SPECTRUM.USER_ID join USER_HISTORY on TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and LEAVE_DATE is null ");
		          }
		          //literature
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.LITERATURE_TITLE) || ((String) simplehistory.get(i)).equals(NmrshiftdbConstants.LITERATURE_AUTHOR)) {
		            query.append(" left join SPECTRUM_LITERATURE on SPECTRUM.SPECTRUM_ID = SPECTRUM_LITERATURE.SPECTRUM_ID left join LITERATURE on SPECTRUM_LITERATURE.LITERATURE_ID=LITERATURE.LITERATURE_ID ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.LITERATURE_AUTHOR)) {
		            query.append(" left join IS_AUTHOR on LITERATURE.LITERATURE_ID=IS_AUTHOR.LITERATURE_ID left join AUTHOR on IS_AUTHOR.AUTHOR_ID=AUTHOR.AUTHOR_ID ");
		          }
		          if (((String) simplehistory.get(i)).indexOf(NmrshiftdbConstants.MOLECULEKEYWORDS_TOTAL) != -1) {
		            query.append(", MOLECULE_KEYWORD,KEYWORD AS KWM ");
		          }
		          if (((String) simplehistory.get(i)).indexOf(NmrshiftdbConstants.SPECTRUMKEYWORDS_TOTAL) != -1) {
		            query.append(", SPECTRUM_KEYWORD, KEYWORD AS KWS ");
		          }
		          //If only calculated or measured spectra are wanted we need the condition/condition type in FROM
		          if (calculatedonly || measuredonly) {
		            query.append(", SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.CHEMNAME)) {
		            query.append(", CHEMICAL_NAME ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.CANNAME) || ((String) simplehistory.get(i)).equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM) || chiral) {
		            query.append(", CANONICAL_NAME ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.MOLKEY)) {
		            query.append(", KEYWORD AS MK, MOLECULE_KEYWORD ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.SPECKEY)) {
		            query.append(", KEYWORD AS SK, SPECTRUM_KEYWORD ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.SUBSTKEYWORD)) {
			            query.append(", KEYWORD AS SUK, SUBSTANCE_KEYWORD ");
			          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.MOLLINK)) {
		            query.append(", MOLECULE_HYPERLINK ");
		          }
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.SPECLINK)) {
		            query.append(", SPECTRUM_HYPERLINK ");
		          }
		          /*if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT)) {
			            query.append(", SUBSTANCE ");
		          }*/
		        }
		        for (int i = 0; i < simplehistory.size(); i++) {
		          if ((((String) simplehistory.get(i)).equals(NmrshiftdbConstants.FORMULA) || ((String) simplehistory.get(i)).equals(NmrshiftdbConstants.FORMULA_WITH_OTHER)) && ((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FUZZY)) {
		            Map<String,String[]> map = AtomUtils.parseFuzzyCriteria(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), true, false);
		            Iterator<String> it = map.keySet().iterator();
		            while (it.hasNext()) {
		              String key = (String) it.next();
		              query.append(" LEFT JOIN (select MOLECULE_ID, SYMBOL, count(*) AS COUNT from ATOM group by MOLECULE_ID, SYMBOL) AS " + key + "_AC ON");
		              query.append(" SPECTRUM.MOLECULE_ID = " + key + "_AC.MOLECULE_ID ");
		            }
		            query.append(" LEFT JOIN (select MOLECULE_ID , count(Distinct SYMBOL) as c from ATOM group by MOLECULE_ID) as d3 ON SPECTRUM.MOLECULE_ID = d3.MOLECULE_ID ");
		          }
		          //hose code
		          if (((String) simplehistory.get(i)).equals(NmrshiftdbConstants.LITERATURE_TITLE) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE)) {
		            query.append(" join ATOM using (MOLECULE_ID) ");
		          }
			    }
		        for (int i = 0; i < searches.length; i++) {
		          //If only calculated or measured spectra are wanted we need the condition/condition type in FROM / or if a hose code search from prediction is done
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CONDITIONS) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE)) {
		            query.append(", SPECTRUM_CONDITION as SPECTRUM_CONDITION_" + i + ", `NMR_CONDITION` as CONDITION_" + i + ", CONDITION_TYPE as CONDITION_TYPE_" + i + " ");
		          }
		        }
		        query.append(" WHERE ");
		        //join spectrum if necessary
		        if(speccent && struccent){
    	        	spectrumSearches=0;
    		        for (int i = 0; i < searches.length; i++) {
    		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) {
    			        	query.append("SPECTRUM_"+spectrumSearches+".MOLECULE_ID = MOLECULE.MOLECULE_ID and ");
    			        	spectrumSearches++;
    		          }
    		        }
    		        if(spectrumSearches==0 || searches.length>spectrumSearches){
    		        	query.append("SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and ");
    		        }
		        }
		        if(substcent) {
		        	query.append("SPECTRUM.SUBSTANCE_ID = SUBSTANCE.SUBSTANCE_ID and ");	
		        }
		        if(samplecent && struccent){
                    query.append("SAMPLE.PROBABLE_STRUCTURE = MOLECULE.MOLECULE_ID and ");		            
		        }
		        if(mixcent){
		        	query.append(" MIXTURE.MIXTURE_ID =MIXTURE_MOLECULE.MIXTURE_ID and MIXTURE_MOLECULE.MOLECULE_ID =MOLECULE.MOLECULE_ID and ");
		        }
		        if(speccent && notAllSelected(spectrumTypes)){
		        	query.append(" (");
			        for(int i=0;i<spectrumTypes.size();i++){
			        	if(((DBSpectrumType)spectrumTypes.get(i)).getChoosen()){
			        		query.append("SPECTRUM");
			        		if(spectrumSearches>0)
			        			query.append("_0");
			        		query.append(".SPECTRUM_TYPE_ID=" + (((DBSpectrumType)spectrumTypes.get(i)).getSpectrumTypeId()==99 ? 1 : ((DBSpectrumType)spectrumTypes.get(i)).getSpectrumTypeId()) + " OR ");
			        	}
			        }
			        query=new StringBuffer(query.substring(0, query.length()-3));
			        query.append(") and ");
		        }
		        //labgroup only
		        if (labgrouponly) {
		        	query.append("LABGROUP_ID>0 and ");
		        }
		        if(samplecent && loggedInUser!=null && TurbineUserPeer.retrieveByPK(Integer.parseInt(loggedInUser.getUserId())).getDBLabGroup()!=null && !TurbineUserPeer.retrieveByPK(Integer.parseInt(loggedInUser.getUserId())).isLabgroupWorker()){
		        	query.append(" SAMPLE.USER_ID="+loggedInUser.getUserId()+" and");
		        }
		        boolean hasmy = false;
		        for (int i = 0; i < searches.length; i++) {
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLKEY)) {
		            query.append(" MK.KEYWORD_ID=MOLECULE_KEYWORD.KEYWORD_ID AND MOLECULE.MOLECULE_ID=MOLECULE_KEYWORD.MOLECULE_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECKEY)) {
		            query.append(" SK.KEYWORD_ID=SPECTRUM_KEYWORD.KEYWORD_ID AND SPECTRUM.SPECTRUM_ID=SPECTRUM_KEYWORD.SPECTRUM_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTKEYWORD)) {
			            query.append(" SUK.KEYWORD_ID=SUBSTANCE_KEYWORD.KEYWORD_ID AND SUBSTANCE.SUBSTANCE_ID=SUBSTANCE_KEYWORD.SUBSTANCE_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLLINK)) {
		            query.append(" MOLECULE_HYPERLINK.MOLECULE_ID=MOLECULE.MOLECULE_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECLINK)) {
		            query.append(" SPECTRUM_HYPERLINK.SPECTRUM_ID=SPECTRUM.SPECTRUM_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAME)) {
		            query.append(" CHEMICAL_NAME.MOLECULE_ID=MOLECULE.MOLECULE_ID AND ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CANNAME) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM) || chiral) {
		            query.append(" CANONICAL_NAME.MOLECULE_ID=MOLECULE.MOLECULE_ID AND ");
		          }
		          //show my spectra
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
		            query.append("(TURBINE_USER.LOGIN_NAME='" + ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange() + "' AND (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.FALSE + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.REJECTED + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.EDITED + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "')) and ");
		            hasmy = true;
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CONDITIONS)) {
		            query.append(" CONDITION_TYPE_" + i + ".CONDITION_NAME='" + ((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements() + "' and ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CONDITIONS) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE)) {
		            query.append(" SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION_" + i + ".SPECTRUM_ID and SPECTRUM_CONDITION_" + i + ".CONDITION_ID = CONDITION_" + i + ".CONDITION_ID and CONDITION_" + i + ".CONDITION_TYPE_ID= CONDITION_TYPE_" + i + ".CONDITION_TYPE_ID and ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTCOMMENT) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTKEYWORD)) {
			            query.append(" SPECTRUM.SUBSTANCE_ID = SUBSTANCE.SUBSTANCE_ID and  ");
		          }
		        }
		        if (calculatedonly || measuredonly) {
		          query.append(" SPECTRUM.SPECTRUM_ID = SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID = `NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID= CONDITION_TYPE.CONDITION_TYPE_ID and ");
		        }
		        //If search is restricted to calculated or measured spectra, we need that in WHERE
		        if (measuredonly || calculatedonly) {
		          query.append(" (");
		          if (measuredonly) {
		            query.append(" CONDITION_TYPE. CONDITION_TYPE='m'");
		          }
		          if (calculatedonly && measuredonly) {
		            query.append(" or");
		          }
		          if (calculatedonly) {
		            query.append(" CONDITION_TYPE. CONDITION_TYPE='c'");
		          }
		          query.append(" ) and ");
		        }
		        if(jcamponly){
		        	query.append(" (SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') and ");
		        }
                if(rawonly){
                    query.append("(SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"') and ");
                }
		        if (!hasmy && searchtype.equals(NmrshiftdbConstants.SPECCENT)) {
                  spectrumSearches=0;
                  for (int i = 0; i < searches.length; i++) {
                    if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) {
                        query.append(" (SPECTRUM_"+spectrumSearches+".REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or SPECTRUM_"+spectrumSearches+".REVIEW_FLAG = '" + NmrshiftdbConstants.EDITED + "'");
                        if(loggedInUser!=null && !loggedInUser.getUserName().equals("anon")){
                            query.append("or (SPECTRUM_"+spectrumSearches+".REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+loggedInUser.getUserId()+")");
                        }
                        spectrumSearches++;
                        query.append(") and ");
                    }
                  }
                  if(spectrumSearches==0 || searches.length>spectrumSearches){
                      query.append(" (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.EDITED + "'");
                      if(loggedInUser!=null && !loggedInUser.getUserName().equals("anon")){
                          query.append("or (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+loggedInUser.getUserId()+")");
                      }
                      query.append(") and ");
                  }
		        }
                if (!hasmy && searchtype.equals(NmrshiftdbConstants.STRUCCENT)) {
                    spectrumSearches=0;
                    for (int i = 0; i < searches.length; i++) {
                      if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) {
                          query.append(" (SPECTRUM_"+spectrumSearches+".REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or  SPECTRUM_"+spectrumSearches+".REVIEW_FLAG =  '"+ NmrshiftdbConstants.EDITED + "' ");
                          if(loggedInUser!=null && !loggedInUser.getUserName().equals("anon")){
                              query.append("or (SPECTRUM_"+spectrumSearches+".REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM_"+spectrumSearches+".USER_ID="+loggedInUser.getUserId()+")");
                          }
                          spectrumSearches++;
                          query.append(") and ");
                      }
                    }
                    if(spectrumSearches==0 || searches.length>spectrumSearches){
                        query.append(" (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.TRUE + "' or  SPECTRUM.REVIEW_FLAG =  '"+ NmrshiftdbConstants.EDITED + "' ");
                        if(loggedInUser!=null && !loggedInUser.getUserName().equals("anon")){
                            query.append("or (SPECTRUM.REVIEW_FLAG = '" + NmrshiftdbConstants.PRIVATE + "' and SPECTRUM.USER_ID="+loggedInUser.getUserId()+")");
                        }
                        query.append(") and ");
                    }
                }
		        if (not) {
		          query.append("not");
		        }
		        query.append("(");
		        spectrumSearches=0;
		        for (int i = 0; i < searches.length; i++) {
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA_WITH_OTHER)) {
		        	  if(((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FUZZY)){
			            Map<String,String[]> map = AtomUtils.parseFuzzyCriteria(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), true, false);
			            Iterator<String> it = map.keySet().iterator();
			            int k = 0;
			            while (it.hasNext()) {
			              String key = (String) it.next();
			              if (k > 0) {
			                query.append(" AND ");
			              }
			              query.append(key + "_AC.SYMBOL = '" + key + "'");
			              query.append(" AND " + key + "_AC.COUNT >= ");
			              query.append(((String[]) map.get(key))[0]);
			              query.append(" AND " + key + "_AC.COUNT <= ");
			              query.append(((String[]) map.get(key))[1]);
			              k++;
			            }
			            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA)) {
			              query.append(" AND d3.c = " + map.size());
			            }
		        	  }else{
		        		  String formulaoriginal = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		        		  IMolecularFormula cdkformula = MolecularFormulaManipulator.getMolecularFormula(formulaoriginal, DefaultChemObjectBuilder.getInstance());  
			              if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA)) {
			            	  query.append(" MOLECULE.FORMULA='" + MolecularFormulaManipulator.getString(cdkformula) + "' ");
			              }else{
			            	  Pattern p = Pattern.compile("([a-zA-Z]*[0-9]*)");
			            	  Matcher m = p.matcher(MolecularFormulaManipulator.getString(cdkformula));
			            	  query.append(" MOLECULE.FORMULA regexp'" + m.replaceAll("$1\\.\\*") + "' ");
			              }
		        	  }
		            query.append(concatenator);
		          }
		          //potential multiplicities
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY)) {
		            Map<String,String[]> map = AtomUtils.parseFuzzyCriteria(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), false, true);
		            Iterator<String> it = map.keySet().iterator();
		            int k = 0;
		            while (it.hasNext()) {
		              String key = (String) it.next();
		              int count = -1;
		              if (key.equals("S")) {
		                count = 0;
		              }
		              if (key.equals("D")) {
		                count = 1;
		              }
		              if (key.equals("T")) {
		                count = 2;
		              }
		              if (key.equals("Q")) {
			                count = 3;
		              }
		              if (k > 0) {
		                query.append(" AND ");
		              }
		              if(count==-1)
			                throw new NmrshiftdbException("Multiplicity search only possible with S/D/T/Q symbols");
		              if (((String[]) map.get(key))[0].equals("0") && ((String[]) map.get(key))[1].equals("0")) {
		                query.append("(");
		              }
		              query.append("(MULTIPLICITY_" + count + " >= ");
		              query.append(((String[]) map.get(key))[0]);
		              query.append(" AND MULTIPLICITY_" + count + " <= ");
		              query.append(((String[]) map.get(key))[1] + ")");
		              if (((String[]) map.get(key))[0].equals("0") && ((String[]) map.get(key))[1].equals("0")) {
		                query.append(" or MULTIPLICITY_" + count + " is null )");
		              }
		              k++;
		            }
		            query.append(" " + concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY)) {
		            Map<String,String[]> map = AtomUtils.parseFuzzyCriteria(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), false, true);
		            Iterator<String> it = map.keySet().iterator();
		            int k = 0;
		            while (it.hasNext()) {
		              String key = (String) it.next();
		              if (!((String[]) map.get(key))[0].equals("0") || !((String[]) map.get(key))[1].equals("0")) {
		                if (k > 0) {
		                  query.append(" AND ");
		                }
		                query.append(key + "_PC.MULTIPLICITY = '" + key + "'");
		                query.append(" AND " + key + "_PC.COUNT >= ");
		                query.append(((String[]) map.get(key))[0]);
		                query.append(" AND " + key + "_PC.COUNT <= ");
		                query.append(((String[]) map.get(key))[1]);
		                k++;
		              }
		            }
		            query.append(" AND d2.c=" + k + "" + concatenator);
		          }
		          //If a subspectrum search is done we need the fingerprint in WHERE
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) && runData != null) {
		            List<CMLPeak> numberSpectrum = ParseUtils.parseSpectrumFromSpecFile(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRangeForSpectrum());
		            //We need to put the input file into an array of DBSignals because the fingerprint is calculated from that. The initSimilarity()-method of DBSpectrum in showDetails also uses this
		            DBSignal[] searchSpectrum = new DBSignal[numberSpectrum.size()];
		            for (int k = 0; k < numberSpectrum.size(); k++) {
		              DBSignal sig = new DBSignal();
		              DBShift[] shifts = new DBShift[1];
		              DBShift shift = new DBShift();
		              shift.setAxis(1);
		              shift.setValue((float)numberSpectrum.get(k).getXValue());
		              shifts[0] = shift;
		              sig.setDBShiftsArray(shifts);
		              sig.addDBShift(shift);
		              sig.setIntensity((float)numberSpectrum.get(k).getYValue());
		              searchSpectrum[k] = sig;
		            }
		            //calculate the fingerprint and do the search with it
		            long fingerprint = SpectrumUtils.makeFingerprint(searchSpectrum, false);
		            DBSpectrumType spectrumType = DBSpectrumTypePeer.getByName(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().substring(0,((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().indexOf("-")));
		            query.append(" (fingerprint_compare64(SPECTRUM_"+spectrumSearches+".FINGERPRINT, " + fingerprint + ") = 'Y' and SPECTRUM_"+spectrumSearches+".SPECTRUM_TYPE_ID="+ (spectrumType.getSpectrumTypeId()==99 ? 1 : spectrumType.getSpectrumTypeId()) +") "+concatenator);
		            spectrumSearches++;
		          }
		          //experiments for sample
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SAMPLE_EXPERIMENTS)) {
		        	  query.append("(");
		        	  StringTokenizer st = new StringTokenizer(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange());
		        	  //st contains id of experiments to search for
		        	  boolean first=true;
		        	  while(st.hasMoreTokens()){
		        		  if(!first){
		        			  query.append(" AND ");
		        		  }
	        			  first=false;
		        		  String id=st.nextToken();
		        		  query.append("EXPERIMENT_SAMPLE.EXPERIMENT_ID=").append(id);
		        	  }		        	  
		        	  query.append(") ").append(concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) && runData != null) {
			          DBSpectrumType spectrumType = DBSpectrumTypePeer.getByName(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().substring(0,((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange().indexOf("-")));
		        	  query.append(" SPECTRUM_"+spectrumSearches+".SPECTRUM_TYPE_ID="+ (spectrumType.getSpectrumTypeId()==99 ? 1 : spectrumType.getSpectrumTypeId()) +" "+concatenator);
			          spectrumSearches++;
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_FRAGMENT)) {
		            query.append(" MOLECULE.MOLECULE_ID=MOLECULE_KEYWORD.MOLECULE_ID AND MOLECULE_KEYWORD.KEYWORD_ID=KWM.KEYWORD_ID AND ");
		            String keywords = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            StringTokenizer tokenizer = new StringTokenizer(keywords, " |");
		            if (tokenizer.hasMoreTokens()) {
		              query.append("( ");
		            }
		            while (tokenizer.hasMoreTokens()) {
		              String name = tokenizer.nextToken();
		              query.append("KWM.KEYWORD like '%" + name + "%' ");
		              if (tokenizer.hasMoreTokens()) {
		                query.append("OR ");
		              } else {
		                query.append(" ) ");
		              }
		            }
		            query.append(concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MOLECULEKEYWORDS_TOTAL)) {
		            query.append(" MOLECULE.MOLECULE_ID=MOLECULE_KEYWORD.MOLECULE_ID AND MOLECULE_KEYWORD.KEYWORD_ID=KWM.KEYWORD_ID AND ");
		            String keywords = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            StringTokenizer tokenizer = new StringTokenizer(keywords, "|");
		            if (tokenizer.hasMoreTokens()) {
		              query.append("( ");
		            }
		            while (tokenizer.hasMoreTokens()) {
		              query.append("KWM.KEYWORD='" + tokenizer.nextToken() + "' ");
		              if (tokenizer.hasMoreTokens()) {
		                query.append("OR ");
		              } else {
		                query.append(" ) ");
		              }
		            }
		            query.append(concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_FRAGMENT)) {
		            query.append(" SPECTRUM.SPECTRUM_ID=SPECTRUM_KEYWORD.SPECTRUM_ID AND SPECTRUM_KEYWORD.KEYWORD_ID=KWS.KEYWORD_ID AND ");
		            String keywords = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            StringTokenizer tokenizer = new StringTokenizer(keywords, " |");
		            if (tokenizer.hasMoreTokens()) {
		              query.append("( ");
		            }
		            while (tokenizer.hasMoreTokens()) {
		              String name = tokenizer.nextToken();
		              query.append("KWS.KEYWORD like '%" + name + "%' ");
		              if (tokenizer.hasMoreTokens()) {
		                query.append("OR ");
		              } else {
		                query.append(" ) ");
		              }
		            }
		            query.append(concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SPECTRUMKEYWORDS_TOTAL)) {
		            query.append(" SPECTRUM.SPECTRUM_ID=SPECTRUM_KEYWORD.SPECTRUM_ID AND SPECTRUM_KEYWORD.KEYWORD_ID=KWS.KEYWORD_ID AND ");
		            String keywords = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            StringTokenizer tokenizer = new StringTokenizer(keywords, "|");
		            if (tokenizer.hasMoreTokens()) {
		              query.append("( ");
		            }
		            while (tokenizer.hasMoreTokens()) {
		              query.append("KWS.KEYWORD = '" + tokenizer.nextToken() + "' ");
		              if (tokenizer.hasMoreTokens()) {
		                query.append("OR ");
		              } else {
		                query.append(" ) ");
		              }
		            }
		            query.append(concatenator);
		          }
		          //Search for conditions
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CONDITIONS)) {
		            query.append(" CONDITION_" + i + ".VALUE='" + ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange() + "'" + concatenator);
		          }
		          //weight
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.WEIGHT)) {
		            String weights = ((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            StringTokenizer tokenizer = new StringTokenizer(weights, "-");
		            query.append(" MOLECULE.MOLECULAR_WEIGHT >= " + tokenizer.nextToken() + " and MOLECULE.MOLECULAR_WEIGHT <= " + tokenizer.nextToken() + concatenator);
		          }
		          if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT)) && runData != null) {
		            IAtomContainer molWithH = doStructure(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), path);
			        BitSet bsor = new OrchemFingerprinter().getFingerprint(molWithH);
			        BitSet bs=new BitSet(OrchemFingerprinter.FINGERPRINT_SIZE+2);
			        bs.or(bsor);
			        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE, AtomUtils.hasOrganometall(molWithH));
			        bs.set(OrchemFingerprinter.FINGERPRINT_SIZE+1, AtomUtils.hasZeroBond(molWithH));
			        StringBuffer fingerprintstring = new StringBuffer();
			        fingerprintstring.append("(");
			        query.append(" fingerprint_compare(");
		            for (int k = 0; k < 16; k++) {
		              query.append("fp" + k + ",");
		            }
		            for (int k = 0; k < 15; k++) {
		              query.append(AtomUtils.getBigIntegerValue(bs, k).longValue() + ",");
		            }
		            query.append(AtomUtils.getBigIntegerValue(bs, 15).longValue() + ") = 'Y' " + concatenator);
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSTRUCTURE)) {
		        	String mdl=((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange();
		            //this is to handle dative bonds for organometals
                    IAtomContainer molWithH = AtomUtils.readMolBothVersions(mdl);
		            AtomUtils.normalize(molWithH, path);
		            IRingSet sssr=new SSSRFinder(molWithH).findSSSR();
		            if(sssr!=null){
			            for(int k=0;k<sssr.getAtomContainerCount();k++){
			            	DeAromatizationTool.deAromatize((IRing)sssr.getAtomContainer(k));
			            }
    	            }
                    for(int k=0;k<molWithH.getAtomCount();k++){
                        if(molWithH.getAtom(k).getPoint2d()==null)
                          molWithH.getAtom(k).setPoint2d(new Point2d(molWithH.getAtom(k).getPoint3d().x,molWithH.getAtom(k).getPoint3d().y));
                    }
                    for(IBond bond : molWithH.bonds()) {
      	                if(bond.getOrder()==null)
      	                    bond.setOrder(IBond.Order.ZERO);
      	            }
                    String smiles = "";
                    String inchi="";
                    if(AtomUtils.isOrganometallic(molWithH)) {
                        AtomUtils.doSimpleSaturation(molWithH,null);
                        AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(molWithH);
                        inchi=GeneralUtils.generateTucan(molWithH);
                        chiral=true;//to search by inchi
                    }else {            
                        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
                        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
                        ElectronDonation model       = ElectronDonation.cdk();
                        CycleFinder      cycles      = Cycles.cdkAromaticSet();
                        Aromaticity      aromaticity = new Aromaticity(model, cycles);
                        aromaticity.apply(molWithH);
                        IAtomContainer molWithoutH = (IAtomContainer) AtomContainerManipulator.removeHydrogens(molWithH);
                        SmilesGenerator sg = SmilesGenerator.unique().aromatic();
                        smiles = sg.create(molWithoutH);
                        inchi=GeneralUtils.generateInchi(molWithH,false);
                    }
                    if(chiral)
                        query.append(" CANONICAL_NAME.NAME='"+inchi+"' "+concatenator);
                    else
                        query.append(" MOLECULE.SMILES_STRING = '" + smiles + "' " + concatenator);
                  }
                  if (!((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.WEIGHT) && !((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA)&& !((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA_WITH_OTHER) && !((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY) && !((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE) && !((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY)) {
                    if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.EXACT)) {
                          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.CHEMNAMEPUBCHEM)) {
                              try{
                                  List<String> results = GeneralUtils.resolveNameFromPubchem(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange());
				            	  query.append("(");
				            	  for(int k=0;k<results.size();k++){
				            		  StringBuffer inchi=new StringBuffer("");
				            		  //
				            		  int slashcount=0;
				            		  for(int l=0;l<results.get(k).length();l++){
				            			  if(results.get(k).charAt(l)=='/')
				            				  slashcount++;
				            			  if(slashcount==4)
				            				  break;
				            			  inchi.append(results.get(k).charAt(l));
				            		  }
				            		  query.append(fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + " like '%" + GeneralUtils.escape(inchi.toString(),false) + "%' ");
				            		  if(k<results.size()-1)
				            			  query.append(" OR ");
				            	  }
			            	  }catch(Exception ex){
			            		  chiralMessage.myString="It seems there is a problem with Pubchem. Please try another search!";
			            		  return new ArrayList<String>();
			            	  }
			            	  query.append(") "+concatenator);
			              }else{
			            	  query.append(fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + "='" + GeneralUtils.escape(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(),false) + "' " + concatenator);
			              }
		            }
		            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FUZZY)) {
		              String searchSoundex = Soundex.soundex(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(), "9", new StringAndInt());
		              query.append(fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + "_SOUNDEX regexp '" + searchSoundex + "' " + concatenator);
		            }
		            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.REGEXP)) {
		              query.append(fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + " regexp '" + GeneralUtils.escape(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(),false) + "' " + concatenator);
		            }
		            if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FRAGMENT)) {
		              query.append(fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + " like '%" + GeneralUtils.escape(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(),false) + "%' " + concatenator);
		            }
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.HOSECODE)) {
		            query.append("( "+fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + " like '" + GeneralUtils.escape(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(),true) + "%' OR "+fields.get(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText()) + "_WITH_RINGS like '" + GeneralUtils.escape(((ValuesForVelocityBean) searchHistory.get(searches[i])).getRange(),true) + "%' )" + concatenator);
		            //if the search is from the prediction spectrum type and solvent are in search type
		            Pattern p = Pattern.compile("[0-9]* -*[0-9]*");
		            String ids=((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements();
		            Matcher m = p.matcher(ids);
		            if(m.matches()){
		              int spectrumtypeid = Integer.parseInt(ids.substring(0,ids.indexOf(' ')));
		              int solventid = Integer.parseInt(ids.substring(ids.indexOf(' ')+1));
		              query.append("(SPECTRUM.SPECTRUM_TYPE_ID=" + (spectrumtypeid==99 ? 1 : spectrumtypeid));
		              if(solventid>-1)
		            	  query.append(" and CONDITION_0.CONDITION_ID="+solventid);
		              query.append(") " + concatenator);
		            }
		          }
		          if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) && runData != null) {
		            query.append((andor ? "true" : "false")+" "+concatenator);
		          }
		        }
		        if (!hasmy) {
		          query.setLength(query.length() - 4);
		        }
		        if (hasmy) {
		          query.append("true");
		        }
		        query.append(")");
		        for (int i = 0; i < searches.length; i++) {
			          if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM)) && runData != null) {
			            query.append(" having similarity>0");
			            break;
			          }
		        }
		        for (int i = 0; i < searches.length; i++) {
		          if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) && runData != null) {
		            query.append(" order by ");
		            break;
		          }
		        }
		        for (int i = 0; i < searches.length; i++) {
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) && runData != null) {
		            query.append("similarity desc, ");
		          }
		          if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) && runData != null) {
		            query.append("similarity desc, ");
		          }
			        if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) {
			            query.append("SPECTRUM.DATE desc, ");
			        }
		        }
		        for (int i = 0; i < searches.length; i++) {
		          if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MYSPECTRA)) && runData != null) {
		            query.append("1 desc");
		            break;
		          }
		        }
		        query.append(" limit "+MAX_HITS_TO_DISPLAY);
		      }
		      Log.info(query.toString());
              if (runData != null && logtosql) {
		        GeneralUtils.logToSql("Search - " + query, runData);
		      }
		      Connection conn=null;
		      try{
			      //We use a prepared statement since normal execution has problems with fingerprint figures.
			      conn = Transaction.begin("default");
			      PreparedStatement checksmiles = conn.prepareStatement(query.toString());
			      long start=System.currentTimeMillis();
			      ResultSet rs = checksmiles.executeQuery();
			      Transaction.commit(conn);
			      conn=null;
			      long sqlDuration=(System.currentTimeMillis()-start)/1000;
			      List<NumberKey> l = new ArrayList<NumberKey>();
			      List<String> l2 = new ArrayList<String>();
			      //Check exact subgraph isomorphism, if necessary
			      boolean alreadylist = false;
			      int sqlHits=0;
			      int hadtofilter=0;
			      int filterHits=0;
			      long filterDuration=0;
			      for (int k = 0; k < searches.length; k++) {
			        if (((ValuesForVelocityBean) searchHistory.get(searches[k])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT)) {
			          start=System.currentTimeMillis();
			          alreadylist = true;
			          IAtomContainer molWithH = doStructure(((ValuesForVelocityBean) searchHistory.get(searches[0])).getRange(), path);
			          boolean queryHasCharge = false;
			          for (int m = 0; m < molWithH.getAtomCount(); m++) {
			            if (molWithH.getAtom(m).getFormalCharge() != 0) {
			              queryHasCharge = true;
			            }
			          }
			          Iterator<IBond> iter = molWithH.bonds().iterator();
			          while(iter.hasNext()){
			            IBond bond=iter.next();
			            if(bond.getOrder()==null) {
			              IBond newBond=bond.getBuilder().newBond();
			              newBond.setAtom(bond.getAtom(0), 0);
			              newBond.setAtom(bond.getAtom(1), 1);
			              newBond.setStereo(bond.getStereo());
			              newBond.setOrder(IBond.Order.ZERO);
			              iter.remove();
			              molWithH.addBond(newBond);
			            }
			          }
			          if(AtomUtils.isOrganometallic(molWithH)) {
                        AtomUtils.doSimpleSaturation(molWithH,null);
                        List<IBond> toremove = new ArrayList<>();
                        for(IBond bond : molWithH.bonds()) {
                        	if(bond instanceof IQueryBond) {
                        		IBond newbond=bond.getBuilder().newInstance(IBond.class, bond.getAtom(0), bond.getAtom(1), bond.getOrder(), bond.getStereo());
                        		molWithH.addBond(newbond);
                        		toremove.add(bond);
                        	}
                        }
                        for(IBond bond : toremove) {
                        	molWithH.removeBond(bond);
                        }
                      }else {
                        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
                        ElectronDonation model       = ElectronDonation.cdk();
                        CycleFinder      cycles      = Cycles.cdkAromaticSet();
                        Aromaticity      aromaticity = new Aromaticity(model, cycles);
                        aromaticity.apply(molWithH);
                      }
			          IAtomContainer mol = (IAtomContainer) AtomContainerManipulator.removeHydrogens(molWithH);
			          if (queryHasCharge) {
			            mol = QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(mol);
			          }
			          while (rs.next()) {
			            hadtofilter++;
			            sqlHits++;
			            if(searchtype.equals(NmrshiftdbConstants.MIXCENT)){
			            	DBMixture mixture = DBMixturePeer.retrieveByPK(rs.getInt(1));
			            	for(int i=0;i<mixture.getDBMolecules().size();i++){
					            IAtomContainer thisMol = ((DBMolecule)mixture.getDBMolecules().get(i)).getAsCDKMolecule(1);
					            UniversalIsomorphismTester checker = new UniversalIsomorphismTester();
					            if (checker.isSubgraph(mol, thisMol)) {
					              l.add(new NumberKey(rs.getString(1)));
					              filterHits++;
					              if (rs.getMetaData().getColumnCount() > 1) {
					                l2.add(rs.getString(2));
					              }
					            }
			            	}
			            }else{
				            IAtomContainer thisMol = DBMolecule.getAsCDKMoleculeFromId(rs.getInt(1));
				            List<IBond> toremove = new ArrayList<>();
                            for(IBond bond : mol.bonds()) {
                                if(bond instanceof IQueryBond) {
                                    IBond newbond=DefaultChemObjectBuilder.getInstance().newInstance(IBond.class, bond.getAtom(0), bond.getAtom(1), IBond.Order.ZERO);
                                    mol.addBond(newbond);
                                    toremove.add(bond);
                                }
                            }
                            for(IBond bond : toremove) {
                                mol.removeBond(bond);
                            }
                            if (new UniversalIsomorphismTester().isSubgraph(thisMol, mol)) {
				              l.add(new NumberKey(rs.getString(1)));
				              filterHits++;
				              if (rs.getMetaData().getColumnCount() > 1) {
				                l2.add(rs.getString(2));
				              }
				            }
			            }
			            if (l.size() > MAX_HITS_TO_DISPLAY) {
			              break;
			            }
			          }
	                  filterDuration=(System.currentTimeMillis()-start)/1000;
			          while (rs.next()) {
			        	  sqlHits++;
			          }
			        }
			      }
			      if (!alreadylist) {
			        while (rs.next()) {
			          sqlHits++;
			          filterHits++;
			          l.add(new NumberKey(rs.getString(1)));
			          if (rs.getMetaData().getColumnCount() > 1) {
			            l2.add(rs.getString(2));
			          }
			        }
			      }
			      checksmiles.close();
			      rs.close();
			      message.append("Duration SQL (s) "+sqlDuration+", hits SQL "+sqlHits+". Duration filter (s) "+filterDuration+", had to filter: "+hadtofilter+", hits filter "+filterHits+".");
			      int isFuzzy = -1;
			      for (int i = searches.length - 1; i > -1; i--) {
			        if (((ValuesForVelocityBean) searchHistory.get(searches[i])).getNameForElements().equals(NmrshiftdbConstants.FUZZY) & !(((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.FORMULA_WITH_OTHER) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.MULTIPLICITY) || ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.POTMULTIPLICITY))) {
			          isFuzzy = i;
			        }
			      }
			      //If thereis a fuzzy search done, we need to sort accordingly
			      if (isFuzzy != -1) {
			        String searchSoundex = Soundex.soundex(((ValuesForVelocityBean) searchHistory.get(searches[isFuzzy])).getRange(), "9", new StringAndInt());
			        ArrayList<StringAndInt> firstResults = new ArrayList<StringAndInt>();
			        for (int i = 0; i < l2.size(); i++) {
			          String name = (String) l2.get(i);
			          StringAndInt sai = new StringAndInt();
			          sai.myString = ((NumberKey) l.get(i)).toString();
			          Soundex.soundex(name, searchSoundex, sai);
			          if (sai.myInt != 0) {
			            int end = (sai.myInt) + ((ValuesForVelocityBean) searchHistory.get(searches[isFuzzy])).getRange().length() - 1;
			            if (end >= name.length() - 1) {
			              end = name.length() - 1;
			            }
			            String subString = name.substring((sai.myInt) - 1, end);
			            sai.myInt = lDistance.getLevenshteinDistance(((ValuesForVelocityBean) searchHistory.get(searches[isFuzzy])).getRange(), subString);
			            firstResults.add(sai);
			          }
			        }
			        Collections.sort(firstResults, new StringAndIntComparator());
			        for (int k = 0; k < firstResults.size(); k++) {
			          if (k == MAX_HITS_TO_DISPLAY) {
			            break;
			          }
			          v.add(((StringAndInt) firstResults.get(k)).myString);
			        }
			      } else {
			        for (int i = 0; i < l.size(); i++) {
			          if (i == MAX_HITS_TO_DISPLAY) {
			            break;
			          }
			          v.add(l.get(i).toString());
			        }
			      }
			      for (int i = 0; i < searches.length; i++) {
			        if ((((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.TOTALSPECTRUM) | ((ValuesForVelocityBean) searchHistory.get(searches[i])).getDisplayText().equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY)) && runData != null) {
			          for (int k = 0; k < l2.size(); k++) {
			            similarities.add(new Double((String) l2.get(k)));
			          }
			          break;
			        }
			      }
			      if (chiral && isAChiralSearch && v.size() == 0) {
			        chiral = false;
			      } else {
			        break;
			      }
		      }
		      finally{
		    	  Transaction.safeRollback(conn);
		      }
	    }
	    if (isAChiralSearch && chiral) {
	      //chiral erfolgreich
	      chiralMessage.myInt = 2;
	    }
	    if (isAChiralSearch && !chiral) {
	      //chiral nicht erfolgreich
	      chiralMessage.myInt = 1;
	    }
	    return (v);
	  }


	private static IAtomContainer doStructure(String mdl, String path) throws CDKException, TorqueException, IOException, DataSetException {
	      IAtomContainer molWithH = AtomUtils.readMolBothVersions(mdl);
	      AtomUtils.normalize(molWithH, path);
          for(IBond bond : molWithH.bonds()) {
          if(bond.getOrder()==null)
              bond.setOrder(IBond.Order.ZERO);
	      }
	      if(AtomUtils.isOrganometallic(molWithH)) {
              AtomUtils.doSimpleSaturation(molWithH,null);
              AtomUtils.doSimpleAromaticyForOrganometallicEquivalences(molWithH);
	      } else {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molWithH);
                ElectronDonation model       = ElectronDonation.cdk();
                CycleFinder      cycles      = Cycles.cdkAromaticSet();
                Aromaticity      aromaticity = new Aromaticity(model, cycles);
                aromaticity.apply(molWithH);
	      }
	      molWithH = (IAtomContainer) AtomContainerManipulator.removeHydrogens(molWithH);
	      for (int m = 0; m < molWithH.getAtomCount(); m++) {
	         molWithH.getAtom(m).setImplicitHydrogenCount(0);
	      }
	      return molWithH;
    }


	private static boolean notAllSelected(List<DBSpectrumType> spectrumTypes) {
			for(int i=0;i<spectrumTypes.size();i++){
				if(!((DBSpectrumType)spectrumTypes.get(i)).getChoosen())
					return true;
			}
			return false;
	  }
}
