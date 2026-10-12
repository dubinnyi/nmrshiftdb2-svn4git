/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2013, 2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.daemons;

//jetspeed stuff
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.servlet.ServletConfig;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.Torque;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openscience.nmrshiftdb.modules.actions.portlets.result.ResultAction;
import org.openscience.nmrshiftdb.om.DBKeyword;
import org.openscience.nmrshiftdb.om.DBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBMoleculeDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumDBKeywordPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.om.DBSpectrumType;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.om.NmrshiftdbUserPeer;
import org.openscience.nmrshiftdb.util.Export;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;


/**
A daemon that builds a homepage to nmrshiftdbhtml. Much better for permormance than doing it in a portlet every time


@author Stefan Kuhn
*/
public class BuildHomeDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon) throws Exception{
      Log.info("bulding home");
      Connection dbconn=null;
      String query="select distinct MOLECULE.MOLECULE_ID from MOLECULE, SPECTRUM, TURBINE_USER, USER_HISTORY, USER_SPECTRUM where TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null and SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and SPECTRUM.SPECTRUM_ID = USER_SPECTRUM.SPECTRUM_ID and SPECTRUM.REVIEW_FLAG ='true' and (USER_HISTORY.LABGROUP_ID is null or USER_HISTORY.LABGROUP_ID=0) order by USER_SPECTRUM.REVIEW_DATE desc limit 4";
      List<TurbineUser> users=null;
      VelocityContext context = new VelocityContext();
      UrlTool urltool=new UrlTool();
      try{
          users = NmrshiftdbUserPeer.getUsersByContribution(20,servcon);
          dbconn = Torque.getConnection();
          Statement stmt = dbconn.createStatement();
          ResultSet rs = stmt.executeQuery(query);
          int i=0;
          String relativepath = ServletUtils.expandRelative(servcon,"/nmrshiftdbhtml");
          while(rs.next() && i<4){
            try{
              Export export=new Export((DBSpectrum)DBMoleculePeer.retrieveByPK(rs.getInt(1)).selectSpectra().get(0));
              export.backColor=new Color(ResultAction.STANDARD_BACKGROUND_COLOR);
              export.getImage(true,"jpeg",relativepath + "/latest"+i,150,150,false,null,null,null);
              context.put("latest"+i+"link","portal/js_pane/P-Results;jsessionid=$data.getSession().getId()?nmrshiftdbaction=showDetailsFromHome&newmol=true&molNumber="+rs.getString(1));
            } catch (Exception ex) {
              Log.error("BuildHome2"+ex.getMessage());
            }
            i++;
          }
          if(new UrlTool().isLabserver()){
              context.put("labserver","true");
              query="select distinct MOLECULE.MOLECULE_ID from MOLECULE, SPECTRUM, TURBINE_USER, USER_HISTORY, USER_SPECTRUM where TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null and SPECTRUM.MOLECULE_ID = MOLECULE.MOLECULE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and SPECTRUM.SPECTRUM_ID = USER_SPECTRUM.SPECTRUM_ID and SPECTRUM.REVIEW_FLAG ='true' and USER_HISTORY.LABGROUP_ID>0 order by USER_SPECTRUM.REVIEW_DATE desc limit 4";
              rs = stmt.executeQuery(query);
              i=0;
              while(rs.next() && i<4){
                try{
                  Export export=new Export((DBSpectrum)DBMoleculePeer.retrieveByPK(rs.getInt(1)).selectSpectra().get(0));
                  export.backColor=new Color(ResultAction.LABGROUP_BACKGROUND_COLOR);
                  export.getImage(true,"jpeg",relativepath + "/latestlab"+i,150,150,false,null,null,null);
                  context.put("latest"+i+"linklab","portal/js_pane/P-Results;jsessionid=$data.getSession().getId()?nmrshiftdbaction=showDetailsFromHome&newmol=true&molNumber="+rs.getString(1));
                } catch (Exception ex) {
                  Log.error("BuildHome2"+ex.getMessage());
                }
                i++;
              }
          }else{
        	  context.put("labserver","false");
          }
      } catch (Exception ex) {
        Log.error("BuildHome2"+ex.getMessage());
      } finally {
        try {
          Torque.closeConnection(dbconn);
        } catch (Exception ex) {
          Log.error("BuildHome3"+ex.getMessage());
        }
      }
      //lets make a Context and put thedata into it
      context.put("email",GeneralUtils.getAdminEmail(servcon));
      context.put("users", users);
      context.put("url","portal");
      context.put("marvinurl","nmrshiftdbhtml");
      context.put("size","444");
      context.put("mode","all");
      context.put("urltool",urltool);
      List<DBSpectrumType> v=DBSpectrumTypePeer.doSelect(new Criteria());
      StringBuffer nuclei=new StringBuffer();
      for(int i=0;i<v.size();i++){
    	  nuclei.append(((DBSpectrumType)v.get(i)).getName()+", ");
      }
      context.put("predictablenuclei",GeneralUtils.removeLastComma(nuclei).toString());
      // lets render the template
      FileWriter out=null;
      try{
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("homeportlet.vm", "ISO-8859-1", context, w);
        File outputFile = new File(ServletUtils.expandRelative(servcon, "/WEB-INF/templates/vm/portlets/html/myhome.vm"));
        out = new FileWriter(outputFile);
        out.write(GeneralUtils.replace(w+"","urltool","$urltool"));
      }catch(Exception ex){
        GeneralUtils.logError(ex,"buildhome mitte",null,false);
      }
      finally {
    	  if(out!=null)
    		  out.close();
      }
      File outputFile = new File(ServletUtils.expandRelative(servcon, "/WEB-INF/templates/vm/spectrumkeywords.vm"));
      FileOutputStream fos= new FileOutputStream(outputFile);
      Criteria crit=new Criteria();
      crit.addJoin(DBKeywordPeer.KEYWORD_ID,DBSpectrumDBKeywordPeer.KEYWORD_ID);
      crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
      crit.setDistinct();
      crit.addJoin(DBSpectrumDBKeywordPeer.SPECTRUM_ID,DBSpectrumPeer.SPECTRUM_ID);
      Criteria.Criterion c0 = crit.getNewCriterion(
    		  DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE, Criteria.EQUAL);
          Criteria.Criterion c1 = crit.getNewCriterion(
        		  DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.PRIVATE, Criteria.EQUAL);
              c0.or(c1);
      crit.add(c0);
      List<DBKeyword> selectedSpectrumKeywords=DBKeywordPeer.doSelect(crit);
      for(int i=0;i<selectedSpectrumKeywords.size();i++){
    	fos.write(new String("<option value=\""+((DBKeyword)selectedSpectrumKeywords.get(i)).getKeyword()+"\" ").getBytes());
    	if(((DBKeyword)selectedSpectrumKeywords.get(i)).getIsChoosen()){
    		fos.write(new String("selected").getBytes());
    	}
    	fos.write(new String(">"+((DBKeyword)selectedSpectrumKeywords.get(i)).getKeyword()+"</option>").getBytes());
      }
      fos.close();
      crit=new Criteria();
      crit.addJoin(DBKeywordPeer.KEYWORD_ID,DBMoleculeDBKeywordPeer.KEYWORD_ID);
      crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
      crit.setDistinct();
      crit.addJoin(DBMoleculeDBKeywordPeer.MOLECULE_ID,DBSpectrumPeer.MOLECULE_ID);
      c0 = crit.getNewCriterion(
    		  DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE, Criteria.EQUAL);
          c1 = crit.getNewCriterion(
        		  DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.PRIVATE, Criteria.EQUAL);
              c0.or(c1);
      crit.add(c0);
      List<DBKeyword> selectedMoleculeKeywords=DBKeywordPeer.doSelect(crit);
      outputFile = new File(ServletUtils.expandRelative(servcon, "/WEB-INF/templates/vm/moleculekeywords.vm"));
      fos= new FileOutputStream(outputFile);
      for(int i=0;i<selectedMoleculeKeywords.size();i++){
      	fos.write(new String("<option value=\""+((DBKeyword)selectedMoleculeKeywords.get(i)).getKeyword()+"\" ").getBytes());
      	if(((DBKeyword)selectedMoleculeKeywords.get(i)).getIsChoosen()){
      		fos.write(new String("selected").getBytes());
      	}
      	fos.write(new String(">"+((DBKeyword)selectedMoleculeKeywords.get(i)).getKeyword()+"</option>").getBytes());
      }
      fos.close();
      this.setResult( Daemon.RESULT_PROCESSING );
      this.setResult( Daemon.RESULT_SUCCESS );
    }
}
