/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2015 Stefan Kuhn
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import javax.servlet.ServletConfig;

import libsvm.svm_train;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.torque.Torque;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.ServletUtils;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSignalPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;

/**
A daemon that builds svm modles from the descriptor tables


@author Stefan Kuhn
*/
public class BuildSvmDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon){
      Log.info("bulding svm");
      boolean done=false;
      Connection dbconn=null;
      try{
        dbconn=Torque.getConnection();
        Statement stmt=dbconn.createStatement();
        //FileReader reader=new FileReader(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/header.arff"));
        //ResultSet rs=stmt.executeQuery("select count(*) from DESCRIPTORS_ALL join ATOM using (ATOM_ID ) where HETERO ='false'");
        //rs.next();
        //int desallcount=rs.getInt(1);
        //lr
        //rs=stmt.executeQuery("select DESCRIPTORS_ALL.* from DESCRIPTORS_ALL join ATOM using (ATOM_ID ) where HETERO ='false'");        
        //ResultSetMetaData meta=rs.getMetaData();
        //int colsnumber = meta.getColumnCount();
        //j48
        /*Instances[] instances=new Instances[5];
        instances[1]=new Instances(new FileReader(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/header.arff")), desallcount);
        instances[2]=new Instances(new FileReader(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/header.arff")), desallcount);
        instances[3]=new Instances(new FileReader(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/header.arff")), desallcount);
        instances[4]=new Instances(new FileReader(ServletUtils.expandRelative(servcon, "/WEB-INF/conf/header.arff")), desallcount);
        rs.beforeFirst();
        instances[1].setClassIndex(colsnumber-2);
        instances[2].setClassIndex(colsnumber-2);
        instances[3].setClassIndex(colsnumber-2);
        instances[4].setClassIndex(colsnumber-2);
        while(rs.next()){
          int protonclass=rs.getInt(colsnumber);
          Instance instance=new Instance(colsnumber-1);
          instance.setDataset(instances[protonclass]);
          for(int k=3;k<=colsnumber;k++){
            if(instances[protonclass].attribute(k-3).isNumeric()){
              instance.setValue(k-3,rs.getDouble(k));
            }else{
              instance.setValue(k-3,""+rs.getInt(k));
            }
          }
          DBSignal signal=DBSignalPeer.retrieveByPK(new NumberKey(rs.getInt(2)));
          int shift=((int)(signal.getFirstShift().getValue()*100));
          if(shift>1500)
            shift=1500;          
          instance.setClassValue(shift);
          instances[protonclass].add(instance);
        }
        J48 j48=new J48();
        for(int i=1;i<5;i++){
        	j48.buildClassifier(instances[i]);
        	instances[i].delete();
        	File j48filenew=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/j48"+i+".new"));
        	OutputStream os = new FileOutputStream(j48filenew);
        	ObjectOutput oo = new ObjectOutputStream(os);
        	oo.writeObject(j48);
        	oo.close();
        	File j48file=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/j48"+i));
        	j48file.delete();
        	j48filenew.renameTo(j48file);
        	File j48instancesfilenew=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/j48instances"+i+".new"));
        	os = new FileOutputStream(j48instancesfilenew);
        	oo = new ObjectOutputStream(os);
        	oo.writeObject(instances[i]);
        	oo.close();
        	File j48instancesfile=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/j48instances"+i));
        	j48instancesfile.delete();
        	j48instancesfilenew.renameTo(j48instancesfile);
        }*/
        //svm
        while(!done){
              for(int h=1;h<5;h++){
                try{
                   File trainingfile=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/descsvm"+h));
                   BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainingfile)));
                   stmt=dbconn.createStatement();
                   ResultSet rs=stmt.executeQuery("select DESCRIPTORS_"+h+".* from DESCRIPTORS_"+h+" join ATOM using (ATOM_ID ) where HETERO ='false'");
                   ResultSetMetaData meta=rs.getMetaData();
                   int colsnumber = meta.getColumnCount();
                   while(rs.next()){
                	   try{
                     StringBuffer line=new StringBuffer();
                     DBSignal signal=DBSignalPeer.retrieveByPK(new NumberKey(rs.getInt(2)));
                     line.append(signal.getFirstShift().getValue()+" ");
                     for(int k=0;k<colsnumber-2;k++){
                       line.append((k+1)+":"+rs.getDouble(k+3)+" ");
                     }
                     line.append("\n");
                     writer.write(line.toString());
						}catch(Exception ex){}
                   }
                   writer.close();
                   File modelfilenew=new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp/descsvm.modelnew"));
                   File modelfile=new File(ServletUtils.expandRelative(servcon,"/WEB-INF/tmp/descsvm.model"+h));
                   svm_train t = new svm_train();
                   String[] args={"-s","3","-t","1","-e","0.001","-n",".5","-c","32","-g","0.0078125",trainingfile+"",modelfilenew+""};
                   t.run(args);
                   trainingfile.delete();
                   modelfile.delete();
                   modelfilenew.renameTo(modelfile);
                   done=true;
                }catch(Exception ex){
                  GeneralUtils.logError(ex,"build svm model",null,false);
                }
            }
        }
      }catch(Exception ex){
        GeneralUtils.logError(ex,"build svm model",null,false);
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
