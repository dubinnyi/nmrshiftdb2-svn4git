package org.openscience.nmrshiftdb.modules.actions.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.services.rundata.DefaultJetspeedRunData;
import org.apache.jetspeed.util.parser.DefaultJetspeedParameterParser;
import org.apache.torque.NoRowsException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.security.DataBackendException;
import org.apache.turbine.util.security.PasswordMismatchException;
import org.apache.turbine.util.security.UnknownEntityException;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.PublishResponseValueDocument;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.nmrshiftdb.om.DBConditionPeer;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBLiterature;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SearchUtils;
import org.openscience.nmrshiftdb.util.SpectrumUtils;
import org.openscience.nmrshiftdb.util.StringAndInt;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.ValuesForVelocityBean;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMoleculeList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;

import com.workingdogs.village.Record;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.ValidityException;

public class NmrshiftdbRestServices {

	public static void doDereplicate(DefaultJetspeedParameterParser parameterParser, PrintWriter out) throws TorqueException, NmrshiftdbException, Exception {
        String searchSpectrum=parameterParser.get("searchspectrum");
        if(searchSpectrum==null) {
            out.write("No searchspectrum given!");
            out.flush();
            return;            	
        }
        String type = parameterParser.getString("spectrumtype", "13C");
        searchSpectrum=type+"-"+searchSpectrum;
        String sub = parameterParser.getString("subspectrum", "whole");
        ArrayList<ValuesForVelocityBean> v=new ArrayList<ValuesForVelocityBean>();
        v.add(new ValuesForVelocityBean(sub, "--", searchSpectrum.toString()));
        //we log with an old sql query to keep the format
  	  	GeneralUtils.logToSql("Search - SELECT DISTINCT FINGERPRINTS.MOLECULE_ID, spectrumsimilarity(SPECTRUM_FINGERPRINTS.SIMPLE_SPECFILE,'"+searchSpectrum+"','total') FROM SPECTRUM_FINGERPRINTS, FINGERPRINTS WHERE SPECTRUM_FINGERPRINTS.MOLECULE_ID = FINGERPRINTS.MOLECULE_ID AND (SPECTRUM_FINGERPRINTS.NAME='13C' OR SPECTRUM_FINGERPRINTS.NAME='1H' OR SPECTRUM_FINGERPRINTS.NAME='15N' OR SPECTRUM_FINGERPRINTS.NAME='31P' OR SPECTRUM_FINGERPRINTS.NAME='19F' OR SPECTRUM_FINGERPRINTS.NAME='11B' OR SPECTRUM_FINGERPRINTS.NAME='29Si' OR SPECTRUM_FINGERPRINTS.NAME='17O' OR SPECTRUM_FINGERPRINTS.NAME='73Ge' OR SPECTRUM_FINGERPRINTS.NAME='195Pt' OR SPECTRUM_FINGERPRINTS.NAME='33S' OR false) and spectrumsimilarity(SPECTRUM_FINGERPRINTS.SIMPLE_SPECFILE, '"+searchSpectrum+"', 'total')  order by spectrumsimilarity(SPECTRUM_FINGERPRINTS.SIMPLE_SPECFILE,'"+searchSpectrum+"', 'total') desc;",null);
        //And we do the search
        ArrayList<Double> similarities=new ArrayList<Double>();
        if (sub.equals("sub")) {
            v.add(new ValuesForVelocityBean(NmrshiftdbConstants.SUBSPECTRUM, "--", searchSpectrum));
        } else if(sub.equals("whole")) {
            v.add(new ValuesForVelocityBean(NmrshiftdbConstants.TOTALSPECTRUM, "--", searchSpectrum));
        }
        List<String> results=SearchUtils.executeSearch(v, new int[1], new StringAndInt(), similarities, true, true, new DefaultJetspeedRunData(), null, false, false, DBSpectrumTypePeer.doSelect(new Criteria()),false, new StringBuffer(),false, false, null);
  	  	CMLCml cmlcml=new CMLCml();
  	  	DecimalFormat format = new DecimalFormat("0.00");
  	  	FieldPosition f = new FieldPosition(0);
  	  	for(int i=0;i<results.size();i++){
  	  		if(i>50)
  	  			break;
	  	  	StringBuffer s=new StringBuffer();
  	  		DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(results.get(i)));
            CMLCml parent=(CMLCml)mol.getCML(1);
            CMLElement cmlmol = parent.getChildCMLElements().get(0);
            parent.removeChildren();
  	  		CMLMetadataList mdl=new CMLMetadataList();
  	  		CMLMetadata metadata=new CMLMetadata();
  	  		metadata.setName("qname:similarity");
  	  		metadata.setContent(format.format(((Double)similarities.get(i)).doubleValue(), s, f) + " %");
  	  		CMLMetadata metadata2=new CMLMetadata();
  	  		metadata2.setName("qname:name");
  	  		metadata2.setContent(mol.getChemicalNamesAsOneStringWithFallback());
  	  		mdl.addMetadata(metadata);
  	  		mdl.addMetadata(metadata2);
  	  		cmlmol.appendChild(mdl);
  	  		cmlcml.appendChild(cmlmol);
  	  	}
  	  	out.print(cmlcml.toXML());
	}
	
	public static void doSearch(DefaultJetspeedParameterParser parameterParser, PrintWriter out, RunData rundata) throws TorqueException, NmrshiftdbException, Exception {
        String searchfield=parameterParser.getString("searchfield");
        String searchstring=parameterParser.get("searchstring");
        String searchtype=parameterParser.getString("searchtype","exact");
    	ArrayList<ValuesForVelocityBean> v=new ArrayList<ValuesForVelocityBean>();
        if(searchfield.replace('+',' ').equals(NmrshiftdbConstants.TOTALSTRUCTURE) || searchfield.replace('+',' ').equals(NmrshiftdbConstants.SUBSTRUCTURE_EXACT) || searchfield.replace('+',' ').equals(NmrshiftdbConstants.SUBSTRUCTURE_SIMILARITY)){
            CMLReader reader = new CMLReader(new ByteArrayInputStream(searchstring.getBytes()));
            IChemFile file = (IChemFile)reader.read(new org.openscience.cdk.ChemFile());
            reader.close();
            StringWriter writer = new StringWriter();
            MDLV2000Writer mdlWriter = new MDLV2000Writer(writer);
            mdlWriter.write(file.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0));
            searchstring=writer.toString();
            mdlWriter.close();
        }else{
        	searchstring=searchstring.replace('+',' ');
        }
        v.add(new ValuesForVelocityBean(searchfield,searchtype,searchstring));
        List<String> result=SearchUtils.executeSearch(v,new int[]{0},new StringAndInt(), new ArrayList<Double>(),false,false,rundata,null,false,true,null,true,new StringBuffer(),false,false, null);
        CMLMoleculeList moleculelist=new CMLMoleculeList();
        for(int i=0;i<result.size();i++){
        	DBMolecule dbmolecule=DBMoleculePeer.retrieveByPK(new NumberKey(result.get(i)));
			Element cmlElement = new Element("cml");
			cmlElement.addAttribute(new Attribute("convention","nmrshiftdb-convention"));
			cmlElement.setNamespaceURI("http://www.xml-cml.org/schema");
			Element parent=dbmolecule.getCML(1, true);
			nu.xom.Node cmldoc = parent.getChild(0);
			((Element)cmldoc).setNamespaceURI("http://www.xml-cml.org/schema");
			parent.removeChildren();
			cmlElement.appendChild(cmldoc);
			for(int k=0;k<dbmolecule.selectSpectra().size();k++){
				Element parentspec=((DBSpectrum)dbmolecule.selectSpectra().get(k)).getCmlSpect(false,null);
				Node spectrumel=parentspec.getChild(0);
				parentspec.removeChildren();
				cmlElement.appendChild(spectrumel);
				((Element)spectrumel).setNamespaceURI("http://www.xml-cml.org/schema");
			}
        	moleculelist.appendChild(cmlElement);
        }
        out.write(moleculelist.toXML());
	}
	
	private static void namespaceThemAll(Elements elements) {
		for(int i=0; i<elements.size(); i++) {
			Element elem = elements.get(i);
			if(!elem.getLocalName().equals("entry") && !elem.getLocalName().equals("doublebondconfiguration")){
				elem.setNamespaceURI(CMLUtil.CML_NS);
				if (elem.getChildCount() != 0) {
					namespaceThemAll(elem.getChildElements());
				}
			}
		}
	}
	
	public static void doSubmit(DefaultJetspeedParameterParser parameterParser, PrintWriter out, ServletConfig cfg, HttpServletResponse res) throws NumberFormatException, Exception {
        String username=parameterParser.getString("username");
        String password=parameterParser.getString("password");
        User user = TurbineSecurity.getAuthenticatedUser(username, password);
        TurbineUser tuser=NmrshiftdbUserPeer.getByName(username);
        CMLBuilder builder = new CMLBuilder(false);
    	Element element =  (Element) builder.parseString(parameterParser.getString("submit"));
        namespaceThemAll(element.getChildElements());
        element.setNamespaceURI(CMLUtil.CML_NS);
        CMLCml cmlcml = (CMLCml) builder.parseString(element.toXML());
        if(cmlcml.getCMLChildCount("molecule")!=1){
            out.write("There must be exactly one molcule in cml");
            res.sendError(406, "multiple molecules!!!" );
        }
        CMLMolecule cmlmol=(CMLMolecule)cmlcml.getChildCMLElement("molecule",0);
        Elements spectra=cmlcml.getChildCMLElements("spectrum");
        if(spectra.size()==0){
        	out.write("There must be at least one spectrum in cml");
        	res.sendError(406, "Not Found!!!" );
        }
    	StringBuffer sb=new StringBuffer();
    	for(int i=0;i<spectra.size();i++){
	    	CMLSpectrum spectrum=(CMLSpectrum)spectra.get(i);
	    	CMLMolecule molecule=(CMLMolecule)cmlcml.getChildCMLElements("molecule").get(0);
	    	SubmittingData subData=new SubmittingData(ServletUtils.expandRelative(cfg, "/WEB-INF/conf/normalizer.xml"));
	    	//is this an edit?
			if(spectrum.getMetadataListElements().size()>0 && GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(), "nmr:nmrshiftdbid").size()>0){
				if(GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(), "nmr:nmrshiftdbid").size()>0){
					List<Record> l=DBSpectrumPeer.executeQuery("select SPECTRUM_ID from SPECTRUM where SPECTRUM_ID='"+GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(), "nmr:nmrshiftdbid").get(0).getContent()+"'");
					subData.setEditSpectrum(((Record)l.get(0)).getValue(1).asInt());
				}
			}		    	
	    	subData.setMeasured();
            CMLReader cmlreader=new CMLReader(new ByteArrayInputStream(cmlmol.toXML().getBytes()));
            IAtomContainer cdkmol=((IChemFile)cmlreader.read(new ChemFile())).getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
            cmlreader.close();
            StringWriter writer=new StringWriter();
            MDLV2000Writer mdlwriter=new MDLV2000Writer(writer);
            mdlwriter.write(cdkmol);
            subData.setMol(writer.toString(),false,false,true);
            mdlwriter.close();
	    	StringBuffer names=new StringBuffer();
	    	for(int k=0;k<molecule.getNameElements().size();k++){
	    		if(molecule.getNameElements().get(k).getAttribute("convention")!=null && molecule.getNameElements().get(k).getAttribute("convention").getValue().equals("CAS")){
	    			subData.setCasNumber(molecule.getNameElements().get(k).getXMLContent());
	    		}else{
	    			names.append(molecule.getNameElements().get(k).getXMLContent()+"\r\n");
	    		}
	    	}
            subData.setChemName(names.toString());
		    if(GeneralUtils.getMetadataListElements(molecule).size()>0){
		    	CMLMetadataList mlist=GeneralUtils.getMetadataListElements(molecule).get(0);
		    	List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
		    	StringBuffer kw=new StringBuffer();
		    	StringBuffer links=new StringBuffer();
		    	for(int k=0;k<metadatas.size();k++){
		    		if(metadatas.get(k).getName().equals("nmr:keyword"))
		    			kw.append(metadatas.get(k).getContent()+";");
		    		if(metadatas.get(k).getName().equals("nmr:link"))
		    			links.append(metadatas.get(k).getContent()+"\r\n");
		    	}
		    	subData.setMoleculeKeywords(GeneralUtils.removeLastComma(kw).toString());
		    	subData.setMoleculeLinks(links.toString());
		    }
		    if(spectrum.getMetadataListElements().size()>0){
		    	CMLMetadataList mlist=spectrum.getMetadataListElements().get(0);
		    	List<CMLMetadata> metadatas=mlist.getMetadataDescendants();
		    	StringBuffer kw=new StringBuffer();
		    	StringBuffer links=new StringBuffer();
		    	for(int k=0;k<metadatas.size();k++){
		    		if(metadatas.get(k).getName().equals("nmr:keyword"))
		    			kw.append(metadatas.get(k).getContent()+";");
		    		if(metadatas.get(k).getName().equals("nmr:link"))
		    			links.append(metadatas.get(k).getContent()+"\r\n");
		    	}
		    	subData.setSpectrumKeywords(GeneralUtils.removeLastComma(kw).toString());
		    	subData.setSpectrumLinks(links.toString());
		    }
	    	DBSpectrumType type=DBSpectrumTypePeer.getByName(GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(), "nmr:OBSERVENUCLEUS").get(0).getContent());
	    	subData.setChoosenSpectrumType(type);
	    	//we remove all explicit Hs not around stereo centers/double bond configurations
	    	boolean[] doublebondconfigurationsprov=new boolean[cdkmol.getBondCount()];
	    	for(int k=0;k<cdkmol.getBondCount();k++){
	    		CMLBond bond=cmlmol.getBonds().get(k);
	    		if(bond.getFirstChildElement("doublebondconfiguration")!=null){
	    			doublebondconfigurationsprov[k]=true;
	    		}
	    	}
	    	List<IAtom> toremove=new ArrayList<IAtom>();
	    	for(int k=0;k<cdkmol.getBondCount();k++){
	    		IAtom atom1=cdkmol.getBond(k).getAtom(0);
	    		if(!doublebondconfigurationsprov[k] && !BondTools.isStereo(cdkmol, atom1)){
	    			toremove.add(atom1);
	    		}
	    		IAtom atom2=cdkmol.getBond(k).getAtom(1);
	    		if(!doublebondconfigurationsprov[k] && !BondTools.isStereo(cdkmol, atom2)){
	    			toremove.add(atom2);
	    		}
	    	}
	    	for(int k=0;k<toremove.size();k++){
    			List<IAtom> conn=cdkmol.getConnectedAtomsList((IAtom)toremove.get(k));
    			for(int l=0;l<conn.size();l++){
    				if(((IAtom)conn.get(l)).getSymbol().equals("H"))
    					cdkmol.removeAtomAndConnectedElectronContainers((IAtom)conn.get(l));
    			}
	    	}
	    	boolean[] doublebondconfigurations=new boolean[subData.getMolWithH().getBondCount()];
	    	for(int k=0;k<cmlmol.getBondCount();k++){
	    		if(doublebondconfigurationsprov.length>k && doublebondconfigurationsprov[k]==true){
	    			doublebondconfigurations[k]=true;
	    		}
	    		
	    	}
	    	subData.setIsDoubleBondSpecified(doublebondconfigurations);
	    	for(int k=0;k<cmlmol.getAtomCount();k++){
	    		subData.getMolWithH().getAtom(k).setID(cmlmol.getAtom(k).getId());
	    		if(k<subData.getMolWithHCount().getAtomCount())
	    			subData.getMolWithHCount().getAtom(k).setID(cmlmol.getAtom(k).getId());
	    	}
	    	//build the signalstable
	    	subData.setSignalstable(spectrum.getPeakListElements().get(0));
	    	for(CMLPeak peak : subData.getSignalstable().getPeakChildren()){
	    	    if(Double.isNaN(peak.getYValue()))
	    	        peak.setYValue(0);
	    	}
	    	Iterator<IAtom> it=subData.getMolWithH().atoms().iterator();
	    	while(it.hasNext()){
	    		it.next().setFlag(10,false);
	    	}
	    	for (int l = 0; l < subData.getConditions().size(); l++) {
            	Elements els=spectrum.getConditionListElements().get(0).getChildCMLElements("scalar");
	            if(((DBConditionType) subData.getConditions().get(l)).getConditionName().equals("Solvent")){
	            	for(int k=0;k<els.size();k++){
	            		if(els.get(k).getAttribute("dictRef")!=null && (els.get(k).getAttribute("dictRef").getValue().equals(NmrshiftdbConstants.solvent) || els.get(k).getAttribute("dictRef").getValue().equals("jcamp:.SOLVENTNAME"))){
	            			((DBConditionType) subData.getConditions().get(l)).setCurrentValue(((CMLScalar)els.get(k)).getValue());
	            			break;
	            		}
	            	}
	            }
	            if(((DBConditionType) subData.getConditions().get(l)).getConditionName().equals("Temperature [K]")){
	            	for(int k=0;k<els.size();k++){
	            		if(els.get(k).getAttribute("dictRef")!=null && els.get(k).getAttribute("dictRef").getValue().equals(NmrshiftdbConstants.temperature)){
	            			((DBConditionType) subData.getConditions().get(l)).setCurrentValue(((CMLScalar)els.get(k)).getValue());
	            			break;
	            		}
	            	}
	            }
	            if(((DBConditionType) subData.getConditions().get(l)).getConditionName().equals("Field Strength [MHz]")){
	            	for(int k=0;k<els.size();k++){
	            		if(els.get(k).getAttribute("dictRef")!=null && (els.get(k).getAttribute("dictRef").getValue().equals(NmrshiftdbConstants.frequency) || els.get(k).getAttribute("dictRef").getValue().equals("jcamp:.OBSERVE FREQUENCY"))){
	            			((DBConditionType) subData.getConditions().get(l)).setCurrentValue(((CMLScalar)els.get(k)).getValue());
	            			break;
	            		}
	            	}
	            }
	            if(((DBConditionType) subData.getConditions().get(l)).getConditionName().equals("Assignment Method")){
	            	((DBConditionType) subData.getConditions().get(l)).setCurrentValue(GeneralUtils.getMetadataDescendantsByName(spectrum.getMetadataListElements().get(0).getMetadataDescendants(),NmrshiftdbConstants.assignment).get(0).getContent());
	            }
	    	}
	    	Elements els=spectrum.getChildElements("entry","http://bibtexml.sf.net/");
	    	for(int k=0;k<els.size();k++){
	    		DBLiterature lit=null;
	    		for(int m=0;m<els.get(k).getChildElements().size();m++){
		    		Element el=els.get(k).getChildElements().get(m);
		    		if(el.getLocalName().equals("article")){
		    			lit=new DBLiterature(NmrshiftdbConstants.JOURNAL_ARTICLE);
		    			lit.setAuthor(el.getChildElements("author","http://bibtexml.sf.net/").get(0).getValue());
		    			lit.setTitle(el.getChildElements("title","http://bibtexml.sf.net/").get(0).getValue());
		    			lit.journaltitle=el.getChildElements("journal","http://bibtexml.sf.net/").get(0).getValue();
		    			lit.year=Integer.parseInt(el.getChildElements("year","http://bibtexml.sf.net/").get(0).getValue());
		    			lit.volume=Integer.parseInt(el.getChildElements("volume","http://bibtexml.sf.net/").get(0).getValue());
		    			if(el.getChildElements("doi","http://bibtexml.sf.net/").size()>0)
		    				lit.setDoi(el.getChildElements("doi","http://bibtexml.sf.net/").get(0).getValue());
		    			String pages=el.getChildElements("pages","http://bibtexml.sf.net/").get(0).getValue();
		    			StringBuffer sb1=new StringBuffer();
		    			StringBuffer sb2=new StringBuffer();
		    			boolean first=true;
		    			for(int o=0;o<pages.length();o++){
			    			if(first && Character.isDigit(pages.charAt(o))){
			    				sb1.append(pages.charAt(o));
			    			}else if(first && !Character.isDigit(pages.charAt(o))){
			    				first=false;
			    			}else{
			    				sb2.append(pages.charAt(o));
			    			}
		    			}
		    			lit.from=Integer.parseInt(sb1.toString());
		    			lit.to=Integer.parseInt(sb2.toString());
		    			//TODO consistrency checks?
		    		}
	    		}
	    		//TODO other types
	    		if(lit!=null){
	    			subData.getLiterature().add(lit);
	    		}
	    	}
	    	subData.setSendEmailUserId(Integer.toString(tuser.getUserId()));
	    	subData.save(tuser.getUserId(),"false","webservice", new Date(),null,Integer.parseInt(GeneralUtils.getNmrshiftdbProperty("hosecode.spheres")), false);
	    	out.write(subData.getSpectrumid());
            SpectrumUtils.doReview(subData, new Date(), tuser);
    	}
        return;		
	}
	
	public static void doPublish(DefaultJetspeedParameterParser parameterParser, PrintWriter out, ServletConfig servcfg) throws SQLException, DataBackendException, UnknownEntityException, PasswordMismatchException, TorqueException, ValidityException, ParsingException, IOException {
    	/*String torquefile = ServletUtils.expandRelative(servcfg, "/WEB-INF/conf/Torque.properties");
    	Torque.init(torquefile);
        TurbineConfig cfg = new TurbineConfig("", ServletUtils.expandRelative(servcfg, "/WEB-INF/conf/TurbineResources.properties"));
        cfg.initialize();
        Properties nmrprops = new Properties();
        File nmrpropsFile = new File(ServletUtils.expandRelative(servcfg, "/WEB-INF/conf/NMRShiftDB.properties"));
    	FileInputStream fis = new FileInputStream(nmrpropsFile);
        nmrprops.load(fis);*/
        String username=parameterParser.getString("username");
        String password=parameterParser.getString("password");
        User tuser = TurbineSecurity.getAuthenticatedUser(username, password);
        TurbineUser user=NmrshiftdbUserPeer.getByName(username);
        Builder parser = new Builder();
        Document doc = parser.build(new ByteArrayInputStream(parameterParser.getString("topublish").getBytes()));
        Element eldata=doc.getRootElement();
        Element elmolecule=eldata.getFirstChildElement("MOLECULE");
        int moleculeid=Integer.parseInt(elmolecule.getFirstChildElement("MOLECULE_ID").getValue());
        Element elspectrum=eldata.getFirstChildElement("SPECTRUM");
        int spectrumid=Integer.parseInt(elspectrum.getFirstChildElement("SPECTRUM_ID").getValue());
        boolean addMolecule=false;
        try{
        	DBMoleculePeer.retrieveByPK(moleculeid);
        	DBMolecule.deleteCacheFiles(moleculeid, servcfg, "../../../ROOT/");
        }catch(NoRowsException ex){
        	//Insert molecule and related tables if not yet existing
        	addMolecule=true;
        }
        //check if spectrum exists, if so, return 2. SpectrumIds must be unique across servers, this is so because of the number range
        Criteria crit=new Criteria();
        crit.add(DBSpectrumPeer.SPECTRUM_ID, spectrumid);
        List<DBSpectrum> l=DBSpectrumPeer.doSelect(crit);
        if(l.size()>0){
	        PublishResponseValueDocument response = PublishResponseValueDocument.Factory.newInstance();
	        out.write("2");
	        return;
        }else{
            Connection dbconn = null;
            Statement stmt = null;
            try{
                dbconn = Torque.getConnection();
                stmt = dbconn.createStatement();
                stmt.executeUpdate("BEGIN");
                //stmt.executeUpdate("set SQL_LOG_BIN=0");
	            for(int i=0;i<eldata.getChildCount();i++){
	            	if(!(eldata.getChild(i) instanceof Text)){
		            	if(((Element)eldata.getChild(i)).getLocalName().equals("entry")){
		            		//Literatures are bibtex entries
		            		DBLiterature literature = new DBLiterature((Element)eldata.getChild(i));
		            		literature.save(spectrumid,dbconn);
		            	}else if(addMolecule || ((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("COUPLING") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("RAW_FILE_SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("NMR_SIGNAL") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM_CONDITION") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("KEYWORD") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM_KEYWORD") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM_LITERATURE") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("USER_SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("USER_SPECTRUM_MARK") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SHIFT") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("RAW_FILE") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SIGNAL_ATOM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("USER_SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("USER_SPECTRUM_MARK") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("PUBLISHER") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM") || 
                                ((Element)eldata.getChild(i)).getLocalName().equals("NMR_CONDITION") || 
                                ((Element)eldata.getChild(i)).getLocalName().equals("SPECTRUM_FILE") || 
		            			((Element)eldata.getChild(i)).getLocalName().equals("IDENTIFIER"))  {
	                        boolean addEntry=true;
		            		if(((Element)eldata.getChild(i)).getLocalName().equals("KEYWORD")){
		            			//keywords are only saved if new 
		                        int keywordid=Integer.parseInt(((Element)eldata.getChild(i)).getFirstChildElement("KEYWORD_ID").getValue());
		                        addEntry=false;
		                        try{
		                        	DBKeywordPeer.retrieveByPK(keywordid);
		                        }catch(NoRowsException ex){
		                        	addEntry=true;
		                        }		            			
		            		}else if(((Element)eldata.getChild(i)).getLocalName().equals("NMR_CONDITION")){
		            			//conditions are only saved if new 
		                        int keywordid=Integer.parseInt(((Element)eldata.getChild(i)).getFirstChildElement("CONDITION_ID").getValue());
		                        addEntry=false;
		                        try{
		                        	DBConditionPeer.retrieveByPK(keywordid);
		                        }catch(NoRowsException ex){
		                        	addEntry=true;
		                        }		            			
		            		}

		            		if(addEntry){
			            		StringBuffer sql=new StringBuffer("insert into "+(((Element)eldata.getChild(i)).getLocalName().equals("NMR_CONDITION") ? "`NMR_CONDITION`" : ((Element)eldata.getChild(i)).getLocalName()) +"(");
			            		boolean first=true;
			            		for(int k=0;k<eldata.getChild(i).getChildCount();k++){
			            			if(!(eldata.getChild(i).getChild(k) instanceof Text)){
				            			if(!first)
				            				sql.append(",");
				            			first=false;
				            			sql.append(((Element)eldata.getChild(i).getChild(k)).getLocalName());
				           			}
			            		}
			            		sql.append(") values (");
			            		first=true;
			            		for(int k=0;k<eldata.getChild(i).getChildCount();k++){
			            			if(!(eldata.getChild(i).getChild(k) instanceof Text)){
				            			if(!first)
				            				sql.append(",");
				            			first=false;
				            			if(!((Element)eldata.getChild(i).getChild(k)).getValue().equals("null") && (((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("CHAR")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("VARCHAR")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("TEXT")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("MEDIUMBLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("DATETIME")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("BLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("MEDIUMBLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("LONGBLOB")))
				            				sql.append("'");
				            			if(((Element)eldata.getChild(i).getChild(k)).getLocalName().equals("USER_ID"))
				            				sql.append(user.getUserId());
				            			else
				            				sql.append(GeneralUtils.escape(((Element)eldata.getChild(i).getChild(k)).getValue(),false));
				            			if(!((Element)eldata.getChild(i).getChild(k)).getValue().equals("null") && (((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("CHAR")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("VARCHAR")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("TEXT")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("MEDIUMBLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("DATETIME")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("BLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("MEDIUMBLOB")
				            					|| ((Element)eldata.getChild(i).getChild(k)).getAttribute("type").getValue().equals("LONGBLOB")))
				            				sql.append("'");
				            			}
				            	}
			            		sql.append(")");
			            		stmt.execute(sql.toString());
		            		}
		            	}
		            }
	            }
	            stmt.executeUpdate("COMMIT");
	            out.write("0");
    	        return;
            }
            catch(Exception ex){
              stmt.executeUpdate("ROLLBACK");
              GeneralUtils.logError(ex,"Error in NMRShiftDBServiceBinding.publish", null,false);
    	      out.write("1");
    	      return;
            }
            finally
            {
            	//stmt.executeUpdate("set SQL_LOG_BIN=0");
                Torque.closeConnection(dbconn);
            }
        }
	}
	
	public static void doAssignment(DefaultJetspeedParameterParser parameterParser, PrintWriter out) throws Exception {
        CMLBuilder builder = new CMLBuilder(false);
    	Element element =  (Element) builder.parseString(parameterParser.getString("toassign"));
        namespaceThemAll(element.getChildElements());
        element.setNamespaceURI(CMLUtil.CML_NS);
        CMLCml cmlcml = (CMLCml) builder.parseString(element.toXML());
        GeneralUtils.assignCML(cmlcml);
        out.write(cmlcml.toXML());
        return;		
	}
}
