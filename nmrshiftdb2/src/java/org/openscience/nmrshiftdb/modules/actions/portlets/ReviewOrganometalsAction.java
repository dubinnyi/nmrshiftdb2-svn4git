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
package org.openscience.nmrshiftdb.modules.actions.portlets;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.vecmath.Point2d;

import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.services.security.turbine.TurbineAccessController;
import org.apache.torque.Torque;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.Expr;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBBond;
import org.openscience.nmrshiftdb.om.DBIdentifierPeer;
import org.openscience.nmrshiftdb.om.DBBondDBAtom;
import org.openscience.nmrshiftdb.om.DBBondDBAtomPeer;
import org.openscience.nmrshiftdb.om.DBCanonicalName;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBUserDBSpectrum;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

/**
 * This is the portlet handling reviewing spectra/molecules. The portlet is based on the principle of either displaying an input form 
 * where the user can input an id for review or display a spectrum for review, where the user can hit accept/reject buttons. The display 
 * of a spectrum can either be reached by entering the id or clicking a url from a review email. The display is chosen by having a message 
 * in the context or not.
 *
 * @author     Stefan Kuhn
 * @created    26. Februar 2002
 */
public class ReviewOrganometalsAction extends VelocityPortletAction {

	/* (non-Javadoc)
	 * @see org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction#buildNormalContext(org.apache.jetspeed.portal.portlets.VelocityPortlet, org.apache.velocity.context.Context, org.apache.turbine.util.RunData)
	 */
	protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
	    try{
    		HttpServletRequest req = data.getRequest();
    		HttpSession session = req.getSession();
    	        Criteria crit = new Criteria();
    	        crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    	        crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.FALSE);
    	        crit.setDistinct();
    		List<DBMolecule> compounds = DBMoleculePeer.doSelect(crit);
    		Iterator<DBMolecule> it = compounds.iterator();
    		int left=0;
    		if(data.getParameters().get("left")!=null)
    			left=data.getParameters().getInt("left");
    		if(data.getParameters().get("right")!=null)
    			left=data.getParameters().getInt("right");
    		int count=0;
    		List<Integer> before=new ArrayList<>();
		List<Integer> after=new ArrayList<>();
    		while(it.hasNext()) {
    			DBMolecule compound=it.next();
			GeneralUtils.logToSql(compound.getMoleculeId()+" "+count,data);
    			if(compound.getMoleculeId()>=left) {
    				if(!AtomUtils.hasOrganometall(compound.getAsCDKMolecule(1)) || count>=20) {
					if(count>=20)
						break;
	    				it.remove();
    				}else {
    					count++;
    				}
    			}
    			if(compound.getMoleculeId()<left) {
    				before.add(compound.getMoleculeId());
				it.remove();
    			}
    		}
    		if(before.size()>0 && before.size()>=20) {
    			context.put("left", before.get(before.size()-20));
    		}
    		count=0;
   		while(it.hasNext()) {
			DBMolecule compound=it.next();
			if(AtomUtils.hasOrganometall(compound.getAsCDKMolecule(1))){
    				if(count==0) {
    					context.put("right", compound.getMoleculeId());
    				}
    				count++;
    			}
			it.remove();
    		}
		context.put("compounds",compounds);
    		context.put("session",session);
    		UrlTool urltool = new UrlTool();
		urltool.init(data);
		context.put("baseurl", urltool.getBaseUrl());
		context.put("accesscontroller", new TurbineAccessController());
           }
           catch(Exception ex){
                context.put("error",GeneralUtils.logError(ex,"review/byurl",data,true));
           }
	}

	
	/**
	 * 
         * @param runData The current RunData object.
         * @param context The context to fill.
	 */
	public void doSelect(RunData runData, Context context){
            try{
		DBMolecule mol=DBMoleculePeer.retrieveByPK(runData.getParameters().getInt("molid"));
		context.put("compound",mol);
		String smol=mol.getStructureFile(1,null,false,true);
		String smol2=AtomUtils.replace9by6inmolfile(smol, true);
		String smol3=GeneralUtils.replace(smol2,"\r\n","\\n");
		context.put(NmrshiftdbConstants.MOLFILE, smol3);
		context.put("showoldinput", "true");
		context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",runData));
		context.put("nohistory","true");
	    }
	    catch(Exception ex){
		context.put("error",GeneralUtils.logError(ex,"reviewom/doselect",runData,true));
	    }
	}

	private int translate(List<RMap> input, int oldid) throws NmrshiftdbException {
            if(input==null)
            	return oldid;
		    for(RMap map : input) {
                if(map.getId1()==oldid)
                    return map.getId2();
            }
            throw new NmrshiftdbException("No such mapping "+oldid);
        }


	/**
	 * 
	 * @param runData The current RunData object.
	 * @param context The context to fill.
	 */
	public void doSubmit(RunData runData, Context context){
	    Connection dbconn = null;
	    Statement stmt = null;
	    try {
	        dbconn = Torque.getConnection();
	        stmt = dbconn.createStatement();
	        try {
	          stmt.executeUpdate("BEGIN");
	        } catch (SQLException ex) {
	          //Since hsql does not understand transactions, we do nothing
	        }
	        String molString=runData.getParameters().getString("MolTxt");
		molString=AtomUtils.replace9by6inmolfile(molString);
	        IAtomContainer mol = AtomUtils.readMolBothVersions(molString);
GeneralUtils.logToSql(mol.getClass().getName(),runData);
DBMolecule dbmol=DBMoleculePeer.retrieveByPK(runData.getParameters().getInt("molid"));
		IAtomContainer asEnteredorig = dbmol.getAsCDKMoleculeAsEntered(1, false);
		IQueryAtomContainer asEntered = QueryAtomContainer.create(asEnteredorig, Expr.Type.ELEMENT, Expr.Type.IS_IN_RING, Expr.Type.IS_IN_CHAIN);
		List<RMap> listatoms=new UniversalIsomorphismTester().getIsomorphAtomsMap(mol,asEntered);
		int i=0;
	      	for(IAtom atom : mol.atoms()) {
	      		DBAtom dbatom = dbmol.getAtomAt(translate(listatoms,i));
			    dbatom.setFormalCharge(atom.getFormalCharge());
	      		dbatom.save(dbconn);
	      		i++;
	      	}
	      	i=0;
			for(IBond bond : mol.bonds()) {
				DBBond dbbond=null;
				int cdk1=AtomUtils.getAtomNumberInCdkMol(mol, bond.getAtom(0));
				int cdk2=AtomUtils.getAtomNumberInCdkMol(mol, bond.getAtom(1));
				for(DBBond dbbondloop : dbmol.getBonds()) {
					int mdl1=dbbondloop.getAtoms().get(0).getMdlNumberVisible();
					int mdl2=dbbondloop.getAtoms().get(1).getMdlNumberVisible();
					if(cdk1==mdl1 && cdk2==mdl2) {
						dbbond=dbbondloop;
						break;
					}
					if(cdk1==mdl2 && cdk2==mdl1) {
						dbbond=dbbondloop;
						break;
					}
				}
				if(bond.getOrder()==null || bond instanceof IQueryBond){
					dbbond.setDegree(0);
				}else{
		      			dbbond.setDegree(new Double(bond.getOrder().ordinal()).intValue());
				}
		      	dbbond.save(dbconn);
	      		i++;
	      	}
	        if(runData.getParameters().getString("review","false").equals("review")) {
	    	    for(DBSpectrum spectrum : dbmol.getDBSpectrums()){
	    		spectrum.setReviewFlag(NmrshiftdbConstants.TRUE);
	    		spectrum.save(dbconn);
			TurbineUser user=TurbineUserPeer.retrieveByPK(new NumberKey(((BaseJetspeedUser) runData.getUser()).getUserId()));
			DBUserDBSpectrum userSpectrum = new DBUserDBSpectrum(spectrum.getSpectrumId(), user, null);
			userSpectrum.setReviewDate(new Date());
			userSpectrum.save(dbconn);
	    	    }   
			}
	                try {
		            stmt.executeUpdate("COMMIT");
		        } catch (SQLException ex) {
			     //Since hsql does not understand transactions, we do nothing
			}
                dbmol=DBMoleculePeer.retrieveByPK(runData.getParameters().getInt("molid"));
                IAtomContainer molWithH=dbmol.getAsCDKMolecule(1);
		for(DBCanonicalName canname: dbmol.getDBCanonicalNames()) {
                    if(canname.getCanonicalNameTypeId()==7) {
            	       canname.setName(GeneralUtils.generateTucan(molWithH));
                    }
                    canname.save(dbconn);
	        } 
                AtomUtils.doSimpleSaturation(molWithH,dbmol);
	        SmilesGenerator sg =  new SmilesGenerator(SmiFlavor.Unique | SmiFlavor.UseAromaticSymbols);
                String smilesNonChiral = sg.create(dbmol.getAsCDKMoleculeWithoutHAndCount());
                dbmol.setSmilesString(smilesNonChiral.getBytes());
                dbmol.save();
	        IMolecularFormula mfa=MolecularFormulaManipulator.getMolecularFormula(molWithH);
	        dbmol.setFormula(MolecularFormulaManipulator.getString(mfa));
	        dbmol.save();
	    try {
	        stmt.executeUpdate("COMMIT");
	      } catch (SQLException ex) {
	        //Since hsql does not understand transactions, we do nothing
	      }
	    } catch (Exception ex) {
	      context.put("error",GeneralUtils.logError(ex,"reviewom/dosubmit",runData,true));
	      try {
			stmt.executeUpdate("ROLLBACK");
	      } catch (SQLException e) {
	        //Since hsql does not understand transactions, we do nothing
	      }
	    }
	    finally{
              Torque.closeConnection(dbconn);
            }
	}
}

