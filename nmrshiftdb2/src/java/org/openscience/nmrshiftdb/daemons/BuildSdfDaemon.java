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
package org.openscience.nmrshiftdb.daemons;

//jetspeed stuff
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;

import nu.xom.Serializer;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.torque.Torque;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.ServletUtils;
import org.openscience.nmrshiftdb.om.DBConditionType;
import org.openscience.nmrshiftdb.om.DBConditionTypePeer;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.UrlTool;

import com.workingdogs.village.Record;

/**
A daemon that builds the sdf file
Since this takes a lot of resources, it is not run in the standard install, but files are built manually in AdminTasks.
Can still be used depending on database size and resources available. 

@author Stefan Kuhn
*/
public class BuildSdfDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon){
      Log.info("bulding sdf file");
      Connection dbconn=null;
      try{
        List<Record> l = DBMoleculePeer.executeQuery("select distinct MOLECULE_ID from SPECTRUM where REVIEW_FLAG=\"true\";");
        File outputFile = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb.sdf.zip.new"));
        File outputFileCml = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb.xml.zip.new"));
        File outputFileWithSignals = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/NmrshiftdbWithSignals.sdf.zip.new"));
        File outputFile3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb3d.sdf.zip.new"));
        File outputFileCml3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb3d.xml.zip.new"));
        File outputFileWithSignals3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/NmrshiftdbWithSignals3d.sdf.zip.new"));
        FileOutputStream baos=new FileOutputStream(outputFile);
        ZipOutputStream zipout=new ZipOutputStream(baos);
        zipout.putNextEntry(new ZipEntry("nmrshiftdb.sdf"));
        FileOutputStream baossig=new FileOutputStream(outputFileWithSignals);
        ZipOutputStream zipoutwithsignals=new ZipOutputStream(baossig);
        zipoutwithsignals.putNextEntry(new ZipEntry("nmrshiftdb.sdf"));
        FileOutputStream baoscml=new FileOutputStream(outputFileCml);
        ZipOutputStream zipoutcml=new ZipOutputStream(baoscml);
        FileOutputStream baos3d=new FileOutputStream(outputFile3d);
        ZipOutputStream zipout3d=new ZipOutputStream(baos3d);
        zipout3d.putNextEntry(new ZipEntry("nmrshiftdb3d.sdf"));
        FileOutputStream baossig3d=new FileOutputStream(outputFileWithSignals3d);
        ZipOutputStream zipoutwithsignals3d=new ZipOutputStream(baossig3d);
        zipoutwithsignals3d.putNextEntry(new ZipEntry("nmrshiftdb3d.sdf"));
        FileOutputStream baoscml3d=new FileOutputStream(outputFileCml3d);
        ZipOutputStream zipoutcml3d=new ZipOutputStream(baoscml3d);
        zipoutcml.putNextEntry(new ZipEntry("nmrshiftdb.xml"));
        zipoutcml3d.putNextEntry(new ZipEntry("nmrshiftdb3d.xml"));
        zipoutcml.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<cml xmlns=\"http://www.xml-cml.org/schema\">").getBytes());
        Serializer serializer = new Serializer(zipoutcml, "ISO-8859-1");
        serializer.setIndent(4);
        zipoutcml3d.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<cml xmlns=\"http://www.xml-cml.org/schema\">").getBytes());
        Serializer serializer3d = new Serializer(zipoutcml3d, "ISO-8859-1");
        serializer3d.setIndent(4);
        List<DBConditionType> conditiontypes=DBConditionTypePeer.doSelect(new Criteria());
        dbconn = Torque.getConnection();
        Statement stmt = dbconn.createStatement();
        for(int i=0;i<l.size();i++){
         try{
            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
            Map<String,String> map=new HashMap<String,String>();
            map.put("nmrshiftdb2 ID", Integer.toString(mol.getMoleculeId()));
            if(!mol.getChemicalNamesAsOneString(false, false).equals(""))
                map.put("chemical names", mol.getChemicalNamesAsOneString(false, false));
            zipout.write(mol.getStructureFile(1,map,true).getBytes());
            zipout.write(new String("$$$$\r\n").getBytes());
            List<DBSpectrum> spectra=mol.selectSpectra();
            for(int m=0;m<conditiontypes.size();m++){
              int ctid=((DBConditionType)conditiontypes.get(m)).getConditionTypeId();
              String ctname=((DBConditionType)conditiontypes.get(m)).getConditionName();
              String sql="select VALUE from SPECTRUM join SPECTRUM_CONDITION using (SPECTRUM_ID) join `NMR_CONDITION` using (CONDITION_ID)  where MOLECULE_ID ="+mol.getMoleculeId()+" and CONDITION_TYPE_ID="+ctid+" order by SPECTRUM.SPECTRUM_TYPE_ID";
              ResultSet rs=stmt.executeQuery(sql);
              StringBuffer valuesline=new StringBuffer();
              int n=0;
              while(rs.next()){
                valuesline.append(n+":"+rs.getString(1)+" ");
                n++;
              }
              if(!valuesline.toString().equals(""))
                map.put(ctname,valuesline.toString());
            }
            StringTokenizer st=new StringTokenizer(mol.getCML(1).getChild(0).toXML(),"<");
            while(st.hasMoreTokens())
              zipoutcml.write(new String("\r\n<"+st.nextToken()).getBytes());
            for(int k=0;k<spectra.size();k++){
              DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
              if(spectrum.getMark()>spectrum.getDisplaymark()){
	              st=new StringTokenizer(spectrum.getCmlSpect(false, null).getChild(0).toXML(),"<");
	              while(st.hasMoreTokens())
	                zipoutcml.write(new String("\r\n<"+st.nextToken()).getBytes());
	              map.put("Spectrum "+spectrum.getDBSpectrumType().getName()+" "+k,spectrum.getSpecfile());
              }
            }
            zipoutwithsignals.write(mol.getStructureFile(1, map, true).getBytes());
            zipoutwithsignals.write(new String("$$$$\r\n").getBytes());
            if(mol.hasCoordinates(2)){
                map=new HashMap<String,String>();
                map.put("nmrshiftdb2 ID", Integer.toString(mol.getMoleculeId()));
                if(!mol.getChemicalNamesAsOneString(false,false).equals(""))
                    map.put("chemical names", mol.getChemicalNamesAsOneString(false,false));
                zipout3d.write(mol.getStructureFile(2,map,true).getBytes());
                zipout3d.write(new String("$$$$\r\n").getBytes());
                spectra=mol.selectSpectra();
                for(int m=0;m<conditiontypes.size();m++){
                  int ctid=((DBConditionType)conditiontypes.get(m)).getConditionTypeId();
                  String ctname=((DBConditionType)conditiontypes.get(m)).getConditionName();
                  String sql="select VALUE from SPECTRUM join SPECTRUM_CONDITION using (SPECTRUM_ID) join `NMR_CONDITION` using (CONDITION_ID)  where MOLECULE_ID ="+mol.getMoleculeId()+" and CONDITION_TYPE_ID="+ctid+" order by SPECTRUM.SPECTRUM_TYPE_ID";
                  ResultSet rs=stmt.executeQuery(sql);
                  StringBuffer valuesline=new StringBuffer();
                  int n=0;
                  while(rs.next()){
                    valuesline.append(n+":"+rs.getString(1)+" ");
                    n++;
                  }
                  if(!valuesline.toString().equals(""))
                    map.put(ctname,valuesline.toString());
                }
                st=new StringTokenizer(mol.getCML(2).getChild(0).toXML(),"<");
                while(st.hasMoreTokens())
                  zipoutcml3d.write(new String("\r\n<"+st.nextToken()).getBytes());
                for(int k=0;k<spectra.size();k++){
                  DBSpectrum spectrum=((DBSpectrum)spectra.get(k));
                  if(spectrum.getMark()>spectrum.getDisplaymark()){
    	              st=new StringTokenizer(spectrum.getCmlSpect(false, null).getChild(0).toXML(),"<");
    	              while(st.hasMoreTokens())
    	                zipoutcml3d.write(new String("\r\n<"+st.nextToken()).getBytes());
    	              map.put("Spectrum "+spectrum.getDBSpectrumType().getName()+" "+k,spectrum.getSpecfile());
                  }
                }
                zipoutwithsignals3d.write(mol.getStructureFile(2, map, true).getBytes());
                zipoutwithsignals3d.write(new String("$$$$\r\n").getBytes());
            	
            }
          }
          catch(Exception ex){
            GeneralUtils.logError(ex,"loop in buildsdf",null,false);
          }
        }
        zipoutcml.write(new String("\r\n</cml>").getBytes());
        zipout.closeEntry();
        zipout.close();
        zipoutwithsignals.closeEntry();
        zipoutwithsignals.close();
        zipoutcml.closeEntry();
        zipoutcml.close();
        zipoutcml3d.write(new String("\r\n</cml>").getBytes());
        zipout3d.closeEntry();
        zipout3d.close();
        zipoutwithsignals3d.closeEntry();
        zipoutwithsignals3d.close();
        zipoutcml3d.closeEntry();
        zipoutcml3d.close();
        File oldFile = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb.sdf.zip"));
        File oldFilesig = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/NmrshiftdbWithSignals.sdf.zip"));
        File oldFileCml = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb.xml.zip"));
        File oldFile3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb3d.sdf.zip"));
        File oldFilesig3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/NmrshiftdbWithSignals3d.sdf.zip"));
        File oldFileCml3d = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml/Nmrshiftdb3d.xml.zip"));
        try{
          oldFile.delete();
          oldFileCml.delete();
          oldFilesig.delete();
          oldFile3d.delete();
          oldFileCml3d.delete();
          oldFilesig3d.delete();
        }catch(Exception ex){
          //we do nothing if file is not there
        }
        outputFile.renameTo(oldFile);
        outputFileCml.renameTo(oldFileCml);
        outputFileWithSignals.renameTo(oldFilesig);
        outputFile3d.renameTo(oldFile3d);
        outputFileCml3d.renameTo(oldFileCml3d);
        outputFileWithSignals3d.renameTo(oldFilesig3d);
      } catch (Exception ex) {
        GeneralUtils.logError(ex,"build sdf",null,false);
      } finally {
        try {
          Torque.closeConnection(dbconn);
        } catch (Exception ex) {
          Log.error("BuildHome3"+ex.getMessage());
        }
      }
      this.setResult( Daemon.RESULT_PROCESSING );
      this.setResult( Daemon.RESULT_SUCCESS );
    }
}
