/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013-2015 Stefan Kuhn
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

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.turbine.util.upload.FileItem;
import org.apache.velocity.context.Context;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.DBWishlist;
import org.openscience.nmrshiftdb.om.DBWishlistPeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUser;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.StringAndStringAndBoolean;
import org.openscience.nmrshiftdb.util.SubmittingData;
import org.openscience.nmrshiftdb.util.UrlTool;

/**
 *  Handles actions for the wishlist portlet.
 *
 * @author     shk3
 * @created    February 16, 2004
 */
public class WishlistPortletAction extends VelocityPortletAction {
  /**
   *  a key for the context.
   */
  public final static String CELLS_KEY = "cells";

  /**
   * puts an entry into wishlist.
   *
   * @param  data           Description of Parameter
   * @param  context        Description of Parameter
   * @exception  Exception  Description of Exception
   */
  public void doEntry(RunData data, Context context){
    try {
      handleEntry(data, context, data.getParameters().get("MolTxtWithH"));
      buildWishlistContext(context, data);
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "wishlist/doentry", data,true);
    }
  }

  /**
   * puts an entry into wishlist.
   *
   * @param  data           Description of Parameter
   * @param  context        Description of Parameter
   * @param  molecule       The structure to save in mol format.
   * @exception  Exception  Description of Exception
   */
  public void handleEntry(RunData data, Context context, String molecule) throws Exception{
    StringAndStringAndBoolean ssb = new StringAndStringAndBoolean();
    IAtomContainer molWithH = AtomUtils.readMolBothVersions(molecule);
    IAtomContainer original = (IAtomContainer)molWithH.clone();
    AtomUtils.addAndPlaceHydrogens(molWithH);
    IAtomContainer molWithHCount = (IAtomContainer)AtomContainerManipulator.removeHydrogens((IAtomContainer)molWithH.clone());
    DBMolecule existingMolecule = GeneralUtils.getExistingMolecule(molWithHCount, molWithH, new boolean[molWithH.getAtomCount()], data.getServletConfig(), molecule, ssb);
    TurbineUser user=NmrshiftdbUserPeer.getByName("admin");
    if (existingMolecule != null) {
      Criteria crit = new Criteria();
      crit.add(DBSpectrumPeer.MOLECULE_ID, existingMolecule.getMoleculeId());
      crit.add(DBSpectrumPeer.SPECTRUM_TYPE_ID, data.getParameters().get("spectrumType"));
      if (DBSpectrumPeer.doSelect(crit).size() > 0) {
        context.put("messages", "There is already such a spectrum in Database!<br>");
        buildWishlistContext(context, data);
        return;
      }
    }else{
      if(data.getUser()!=null && data.getUser().hasLoggedIn())
        user=(NmrshiftdbUser) data.getUser();
      existingMolecule=DBMoleculePeer.retrieveByPK(new SubmittingData(ServletUtils.expandRelative(data.getServletConfig(), "/WEB-INF/conf/normalizer.xml")).saveMoleculeOnly(original, data, user));
    }
    DBWishlist entry = new DBWishlist();
    entry.setUserId(user.getUserId());
    entry.setDate(new Date());
    entry.setSpectrumTypeId(Integer.parseInt(data.getParameters().get("spectrumType")));
    entry.setDBMolecule(existingMolecule);
    entry.save();
  }

  /**
   *  Handles an mdl upload action.
   *
   * @param  data           Description of Parameter
   * @param  context        Description of Parameter
   */
  public void doUpload(RunData data, Context context) {
    try {
      FileItem[] fi = data.getParameters().getFileItems("Datei");
      if (fi[0] != null) {
        if(data.getRequest().getParameter("applet") !=null && data.getRequest().getParameter("applet").equals("applet")){
          File outputFile = new File(ServletUtils.expandRelative(data.getServletConfig(), "nmrshiftdbhtml/" + System.currentTimeMillis() + "wishlist.mol"));
          FileWriter out = new FileWriter("/"+outputFile);
          out.write(fi[0].getString());
          out.close();
          context.put("moltxt", "nmrshiftdbhtml/" + outputFile.getName());
          context.put("upload", "true");
        }else{
          handleEntry(data, context,fi[0].getString());
        }
      }
      buildWishlistContext(context, data);
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "wishlist/doupdate", data,true);
    }
  }


  /**
   * Default behavior for the portlet action.
   *
   * @param  portlet  Description of Parameter
   * @param  context  Description of Parameter
   * @param  data     Description of Parameter
   */
  protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData data) {
    buildWishlistContext(context, data);
  }


  /**
   * Default behavior for the portlet action.
   *
   * @param  context  Description of Parameter
   * @param  data     Description of Parameter
   */
  protected void buildWishlistContext(Context context, RunData data) {
    try {
      Criteria crit = new Criteria();
      crit.addAscendingOrderByColumn(DBWishlistPeer.DATE);
      List<DBWishlist> v = DBWishlistPeer.doSelect(crit);
      List<List<List<String>>> rows=new ArrayList<List<List<String>>>();
      List<List<String>> row=new ArrayList<List<String>>();
      for (int i = 0; i < v.size(); i++) {
        if(i%2==0){
          row=new ArrayList<List<String>>();
          rows.add(row);
        }
        DBWishlist wl=(DBWishlist)v.get(i);
        List<String> field=new ArrayList<String>();
        field.add("portal/js_pane/P-Submit?nmrshiftdbaction=submitforwishlist&molNumber="+wl.getDBMolecule().getMoleculeId()+"&specnr="+wl.getDBSpectrumType().getSpectrumTypeId());
        field.add("download/NmrshiftdbServlet/"+wl.getDBMolecule().getMoleculeId()+".jpg;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=exportmol&moleculeid="+wl.getDBMolecule().getMoleculeId()+"&format=jpeg&size=150x150&backcolor=12632256&compact=compact");
        row.add(field);
        String inscription = null;
        if(wl.getUserId()==0 || wl.getTurbineUser().getLoginName().equals("admin"))
          inscription="Anonymous wishes a "+wl.getDBSpectrumType().getName()+" spectrum";
        else
          inscription=wl.getTurbineUser().getLoginName()+" wishes a "+wl.getDBSpectrumType().getName()+" spectrum";
        field.add(inscription);
      }
      context.put("rows",rows);
      context.put("types", DBSpectrumTypePeer.doSelect(new Criteria()));
      if(data.getRequest().getParameter("applet") !=null && data.getRequest().getParameter("applet").equals("applet")){
        data.getSession().setAttribute("applet","applet");
      }
	  	UrlTool urltool = new UrlTool();
		urltool.init(data);
		context.put("baseurl", urltool.getBaseUrl());
    } catch (Exception ex) {
      GeneralUtils.logError(ex, "Exception in WishlistPortletAction.buildWishlistContext.", data,true);
    }
  }  
}

