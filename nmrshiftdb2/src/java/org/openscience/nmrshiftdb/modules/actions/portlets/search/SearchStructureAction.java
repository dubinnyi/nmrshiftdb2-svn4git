/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012-2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.modules.actions.portlets.search;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.AtomUtils;

import com.workingdogs.village.Record;

/**
 *  This is the portlet showing the search by structure form and filling it with the
 *  data from the previous submit
 *
 * @author     Stefan Kuhn
 * @created    11. April 2002
 */
public class SearchStructureAction extends VelocityPortletAction
{

	/**
	 * Builds the context for the portlet.
	 *
     * @param  data  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
	 */
    public void buildNormalContext(VelocityPortlet portlet, Context context, RunData runData){
		try{
			HttpServletRequest req = runData.getRequest();
			HttpSession session = req.getSession();
			if(req.getParameter("applet") !=null && req.getParameter("applet").equals("applet")){
				session.setAttribute("applet","applet");
			}
		    if(req.getParameter("applet") !=null && req.getParameter("applet").equals("noapplet")){
		        runData.getSession().setAttribute("applet","noapplet");
		    }
		    context.put("structureeditor", GeneralUtils.getNmrshiftdbProperty("structureeditor",runData));
			if(req.getContentType()!=null && req.getContentType().indexOf("multipart/form-data")>-1){
				FileItem[] fi = runData.getParameters().getFileItems("Datei");
				if(fi!=null){
					StringBuffer messages=new StringBuffer();
					GeneralUtils.processFileItem(fi[0],messages,runData,"moltxt");
					context.put("message2",messages.toString());
				}
			}
			if(session.getAttribute("moltxt")!=null){
				StringWriter sw = new StringWriter();
				MDLV2000Writer mdlwriter = new MDLV2000Writer(sw);
				Properties customSettings = new Properties();
				 customSettings.setProperty("WriteAromaticBondTypes", "true"
				);
				PropertiesListener listener =
				   new PropertiesListener(customSettings);
				mdlwriter.addChemObjectIOListener(listener);
				mdlwriter.writeMolecule((IAtomContainer)session.getAttribute("moltxt"));
				mdlwriter.close();
				String mol=sw.toString();
				mol=AtomUtils.replace9by6inmolfile(mol, true);
				if(!GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("JCP") || session.getAttribute("applet") !=null && session.getAttribute("applet").equals("applet")){
					if(GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("MARVINJS") || GeneralUtils.getNmrshiftdbProperty("structureeditor", runData).equals("GGAKETCHER")){
						context.put(NmrshiftdbConstants.MOLFILE, GeneralUtils.replace(GeneralUtils.replace(mol,"\n","\\n"),"\r","\\r"));
					}else{
						File outputFile = new File(ServletUtils.expandRelative(runData.getServletConfig(), "/nmrshiftdbhtml/"+System.currentTimeMillis()+"search.mol"));
						FileWriter out = new FileWriter(outputFile);
						out.write(mol);
						out.close();
						context.put("molfile", "nmrshiftdbhtml/"+outputFile.getName());
					}
				}else{
					String filename="nmrshiftdbhtml/" + System.currentTimeMillis() + "search.jpg";
					GeneralUtils.makeJpg(mol,"/"+filename,runData.getServletConfig(),200,200,false,null,null, Color.LIGHT_GRAY);
					context.put("imagefile",filename);
				}
				context.put("showoldinput","true");
			}else{
				context.put("showoldinput","false");
			}
			String sql="select min(MOLECULE_ID) from MOLECULE";
			List<Record> l=DBMoleculePeer.executeQuery(sql);
			context.put("firstmolecule", ((Record) l.get(0)).getValue(1).asString());
			context.put("sub",session.getAttribute("sub")==null ? NmrshiftdbConstants.TOTALSTRUCTURE : session.getAttribute("sub"));
			context.put("data",runData);
			context.put("session", runData.getSession());
			context.put("mixture",session.getAttribute("mixture")==null || !(Boolean)session.getAttribute("mixture") ? false : true);
			context.put("constants",new NmrshiftdbConstants());
			context.put("link", JetspeedLinkFactory.getInstance(runData));
			context.put("data",runData);
			context.put("tab", "P-Results");
			context.put("tab_smiles", "P-Search");
			UrlTool urltool = new UrlTool();
			urltool.init(runData);
			context.put("baseurl", urltool.getBaseUrl());
			context.put("mixturesearch", GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.MIXTURE_SEARCH_POSSIBLE).equals("true"));
			context.put("unassigned", GeneralUtils.getNmrshiftdbProperty(NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED).equals("true"));
		}
		catch (Exception ex)
		{
		    GeneralUtils.logError(ex,"search structure",runData,true);
		}
	}
}

