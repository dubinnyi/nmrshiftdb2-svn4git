/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2011, 2013-2015 Stefan Kuhn
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.jetspeed.daemon.Daemon;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.nmrshiftdb.om.DBExperimentDBMachinePeer;
import org.openscience.nmrshiftdb.om.DBExperimentDBSample;
import org.openscience.nmrshiftdb.om.DBExperimentDBSamplePeer;
import org.openscience.nmrshiftdb.om.DBMachine;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.workingdogs.village.Record;

/**
 * This class adds spectrometer files to orders.
 * It takes all orders younger than 90 hours for robot or self service and looks if there are as many directories in the folder for this order as 
 * there are experiments in the order. If so, it zips the files, adds them to the order and marks the order as finished.
 * The daemon also handles publishing of orders on lab servers to the public instance.
 * 
 * @author shk3
 *
 */
public class AssignRobotDaemon extends AbstractDaemonWithServletConfig {
	
	int MAXDEPTH=3;
	File rawfiledir=null;
	
    /**
    Does the task
    */
    public void run(ServletConfig servcon) throws Exception{
        this.setResult( Daemon.RESULT_PROCESSING );
        Log.info("running robot stuff");
        Connection dbconn=null;
        Date oneweekbefore=new Date(System.currentTimeMillis()-324000000l);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
        String query="select distinct SAMPLE.SAMPLE_ID, USERS_ID, MACHINE.NAME, PROCESS from (SAMPLE left join RAW_FILE using (SAMPLE_ID)) join MACHINE on SAMPLE.MACHINE=MACHINE.MACHINE_ID where URL is null and FINISHED=\"false\" and (PROCESS=\""+NmrshiftdbConstants.PROCESS_CHANGER+"\" or PROCESS=\""+NmrshiftdbConstants.PROCESS_SELF+"\" or PROCESS=\""+NmrshiftdbConstants.PROCESS_BARCODE+"\" or PROCESS=\""+NmrshiftdbConstants.PROCESS_OPERATOR+"\") and SAMPLE.DATE>\""+formatter.format(oneweekbefore)+"\" order by MACHINE.NAME";
        try{
            dbconn = Torque.getConnection();
            Statement stmt = dbconn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            String lastMachineUsed="";
            //this is the sftp version
            if(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryrobot",servcon).equals("usesftp")){
                JSch jsch=new JSch();
                ChannelSftp c=null;
                Session session=null;
                Channel channel=null;
                while(rs.next()){
                    int sampleid=rs.getInt(1);
                	String process=rs.getString(4);
                	if(!process.equals(NmrshiftdbConstants.PROCESS_OPERATOR)) {
                		DBMachine machine=DBSamplePeer.retrieveByPK(new NumberKey(sampleid)).getDBMachine();
	                    if(!rs.getString(3).equals(lastMachineUsed)){
	                        if(c!=null){
	                            channel.disconnect();
	                            session.disconnect();
	                        }
	                        String host=machine.getSftpHost();
	                        String user=machine.getSftpUser();
	                        int port=22;
	                        session=jsch.getSession(user, host, port);
	                        session.setPassword(machine.getSftpPassword());
	                        session.setConfig("StrictHostKeyChecking", "no");
	                        session.connect();
	                        channel=session.openChannel("sftp");
	                        channel.connect();
	                        c=(ChannelSftp)channel;
	                    }
	                    DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(sampleid));
	                    int dircount=0;
	                    try{
	                        c.cd("/opt/topspin/data/"+sample.getNmrshiftdbUser().getAffiliation2()+"/nmr/"+sample.getUsersId());
	                        dircount = c.ls(".").size()-2;
	                    }catch(Exception ex){
	                        dircount=0;
	                    }
	                    sample.setFinishedExperiments(dircount);
	                    sample.save();
	                    int spectrainorder=sample.getExperimentNumber();
	                    if(dircount>=spectrainorder){
	                        sample.setFinished("true");
	                        sample.save();
	                        DBRawFile rawfile=new DBRawFile();
	                        rawfile.setSampleId(sampleid);
	                        rawfile.setUrl("sftp://"+machine.getSftpHost()+"/"+sample.getDBMachine().getName()+"/"+sample.getNmrshiftdbUser().getAffiliation2()+"/nmr/"+sample.getUsersId());
	                        rawfile.setContainedFiles("");
	                        rawfile.setAssigned("false");
	                        rawfile.setMd5("");
	                        rawfile.setDate(null);
	                        rawfile.save();
	                        MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(servcon), sample.getNmrshiftdbUser().getEmail(), GeneralUtils.getAdminEmail((RunData)null), "You order is finished!", "Order "+sample.getUsersId()+" is finished and can be collected." + GeneralUtils.getEmailSignature((RunData)null));
	                        if (email.send() == false) {
	                            Log.error("Sending sample completion email for "+sample.getUsersId()+" failed");
	                        }
	                    }else{
	                        sample.setFinishedExperiments(dircount);
	                    }
	                    sample.save();
                	}
                }
            }else{
                //this is the locally mounted version
                //this assumes the file system to be like: rawfiledirectoryrobot/instrumentname/ordername
                rawfiledir=new File(GeneralUtils.getNmrshiftdbProperty("rawfiledirectoryrobot",servcon));
            	while(rs.next()){
                	String usersid=rs.getString(2);
                	int sampleid=rs.getInt(1);
                	String process=rs.getString(4);
                	//we find out how many different experiments should be measured
                	List<Record> result = DBExperimentDBSamplePeer.executeQuery("select distinct EXPERIMENT_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where SUB_EXPERIMENT_1_ID is null and  SAMPLE_ID ="+sampleid+" union select distinct SUB_EXPERIMENT_1_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_1_ID is not null union select distinct SUB_EXPERIMENT_2_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_2_ID is not null union select distinct SUB_EXPERIMENT_3_ID from EXPERIMENT_SAMPLE join EXPERIMENT using (EXPERIMENT_ID) where  SAMPLE_ID ="+sampleid+" and SUB_EXPERIMENT_3_ID is not null");
                	int spectrainorder=result.size();
                	File datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid);
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.toLowerCase());
                	}
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.toUpperCase());
                	}
                	if(!datadir.exists()){
                		datadir=new File(rawfiledir+System.getProperty("file.separator")+rs.getString(3)+System.getProperty("file.separator")+usersid.substring(0,1).toUpperCase()+usersid.substring(1).toLowerCase());
                	}
        			//we find the minimum exp number
                	int minexp=-1;
                	for(File expdir : datadir.listFiles()) {
                		if(NumberUtils.isNumber(expdir.getName())){
                			if(minexp==-1 || Integer.parseInt(expdir.getName())<minexp)
                				minexp=Integer.parseInt(expdir.getName());
                		}
                	}
            		if(datadir.exists() && (System.currentTimeMillis()-datadir.lastModified())>300000 && (datadir.list().length>=spectrainorder || (DBSamplePeer.retrieveByPK(new NumberKey(sampleid)).getProcess().equals(NmrshiftdbConstants.PROCESS_OPERATOR) && minexp>-1))){
            			if(process.equals(NmrshiftdbConstants.PROCESS_OPERATOR)) {
	    	                this.addfiles(datadir, null, null, 0, usersid, minexp);            				
            			}else {
	    	                File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)+"/"+usersid+".zip");
	    	                StringBuffer attachedfiles=new StringBuffer();
	    	                if(!zipfile.exists()){
		    	                FileOutputStream baos=new FileOutputStream(zipfile);
		    	                ZipOutputStream zipout=new ZipOutputStream(baos);
		    	                this.addfiles(datadir, zipout, attachedfiles, 0, usersid, minexp);
		    	                zipout.close();
	    	                }
	    	                if(zipfile.length()<50*1024*1024 && zipfile.length()>100){//>20 mb oder ganz klein stimmt was nicht
	    	                    FileInputStream fis = new FileInputStream(zipfile);
	    	                    String md5String = DigestUtils.md5Hex(fis);
	    	                    fis.close();
	    						DBRawFile rawfile=new DBRawFile();
	    		        		rawfile.setSampleId(sampleid);
	    					  	rawfile.setUrl(GeneralUtils.getNmrshiftdbProperty("urlcompressedfiles",servcon)+"/"+usersid+".zip");
	    					  	rawfile.setContainedFiles(attachedfiles.toString());
	    					  	rawfile.setAssigned("false");
	    					  	rawfile.setMd5(md5String);
	    					  	rawfile.setDate(new Date());
	    					  	rawfile.save();
	    					  	DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(sampleid));
	    					  	sample.setFinished("true");
	    					  	sample.save();
	    					}else{
	    					    DBSample sample = DBSamplePeer.retrieveByPK(sampleid);
	    					    if(zipfile.length()<100){					        
	    					        GeneralUtils.sendEmailToLabgroupWorkers(sample.getNmrshiftdbUser().getDBLabGroup(),"Problem processing order", "Files for order "+ sample.getUsersId() +" by "+DBSamplePeer.retrieveByPK(sampleid).getNmrshiftdbUser().getLoginName()+" are less than 100 byte. Please check!", servcon);
	    					    }else{
	    					        GeneralUtils.sendEmailToLabgroupWorkers(sample.getNmrshiftdbUser().getDBLabGroup(),"Problem processing order", "Files for order "+ sample.getUsersId() +" by "+DBSamplePeer.retrieveByPK(sampleid).getNmrshiftdbUser().getLoginName()+" are more than 20 MB. Please check!", servcon);
	    					    }
	    						zipfile.delete();
	    					}
            			}
            		}
                }
            	//this does a search on all levels - slow, but not depending an file system architecture
                /*ArrayList<String> tosearch=new ArrayList<String>();
                HashMap<String,Integer> idmap=new HashMap<String,Integer>();
                while(rs.next()){
                	int sampleid=rs.getInt(1);
                	String usersid=rs.getString(2).toLowerCase();
                	idmap.put(usersid,sampleid);
                	tosearch.add(usersid);
                }
                HashMap<String,ZipOutputStream> zipoutmap=new HashMap<String,ZipOutputStream>();
                HashMap<String,StringBuffer> filenamemap=new HashMap<String,StringBuffer>();
            	searchfiles(rawfiledir,tosearch,zipoutmap,filenamemap, new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)), servcon,0,-1);
            	rs.beforeFirst();
                while(rs.next()){
                	int sampleid=rs.getInt(1);
                	String usersid=rs.getString(2).toLowerCase();
    	        	if(filenamemap.get(usersid)!=null){
    	        		ZipOutputStream zipout=zipoutmap.get(usersid);
    	        		zipout.close();
    	        		DBRawFile rawfile=new DBRawFile();
    	        		rawfile.setSampleId(sampleid);
    				  	rawfile.setUrl(GeneralUtils.getNmrshiftdbProperty("urlcompressedfiles",servcon)+"/"+usersid+".zip");
    				  	rawfile.setContainedFiles(filenamemap.get(usersid).toString());
    				  	rawfile.setAssigned("false");
    				  	rawfile.setDate(new Date());
    				  	rawfile.save();
    				  	DBSample sample=DBSamplePeer.retrieveByPK(new NumberKey(sampleid));
    				  	sample.setFinished("true");
    				  	sample.save();
    	        	}
                }*/
            }
            //We also use this to publish data older than publishaftermonths and in pubchem and not yet published
            Date xmonthsbefore=new Date(System.currentTimeMillis()-1000l*60l*60l*24l*30l*Long.parseLong(GeneralUtils.getNmrshiftdbProperty("publishaftermonths",servcon)));
            String sql="select SPECTRUM_ID from SPECTRUM join TURBINE_USER using (USER_ID) join USER_HISTORY using (USER_ID) where USER_HISTORY.LEAVE_DATE is null and USER_HISTORY.LABGROUP_ID >0 and SPECTRUM.DATE<'"+formatter.format(xmonthsbefore)+"' and SPECTRUM.PUBLISHED='false' and SPECTRUM.REVIEW_FLAG='true'";
            ResultSet spectra=stmt.executeQuery(sql);
            while(spectra.next()){
            	DBSpectrum spectrum=DBSpectrumPeer.retrieveByPK(spectra.getInt(1));
            	DBMolecule mol=spectrum.getDBMolecule();
            	if(mol.getInchi()!=null && !mol.getInchi().equals("")){
            		boolean isInPubchem=GeneralUtils.isInPubchem(mol.getInchi());
            		if(isInPubchem){
                                try{
                                        spectrum.publish(GeneralUtils.getNmrshiftdbProperty("publishserver", servcon), "publish", "publish");
                                }catch(Exception ex){
                                        GeneralUtils.logError(ex,"robot daemon/publish",null,true);
                                }
            		}
            	}
            }
            this.setResult( Daemon.RESULT_SUCCESS );
        }catch(Exception ex){
        	ex.printStackTrace();
        	GeneralUtils.logError(ex,"robot daemon",null,true);
        }
        finally{
            Torque.closeConnection(dbconn);
        }
    }
    
    @SuppressWarnings("unused")
    private int countOccurences(String string, char character) {
		int count=0;
		for(int i=0;i<string.length();i++){
			if(string.charAt(i)==character)
				count++;
		}
		return count;
	}

	@SuppressWarnings("unused")
    private void searchfiles(File tolookin, ArrayList<String> usersid, HashMap<String,ZipOutputStream> zipoutputstream, HashMap<String,StringBuffer> attachedfiles, File dirtoignore, ServletConfig servcon, int depth, int currentexpid, int minexp) throws IOException, TorqueException, Exception{
    	File[] filesindir=tolookin.listFiles();
    	//System.err.println("search "+tolookin+"  "+usersid+"  "+filesindir[0].getName());
    	if(filesindir!=null){
	    	for(int i=0;i<filesindir.length;i++){
	    		if(filesindir[i].isDirectory() && arrayindex(usersid,filesindir[i].getName()).equals("") && !filesindir[i].getName().equals(".") && !filesindir[i].getName().equals("..") && !filesindir[i].equals(dirtoignore) && depth<=MAXDEPTH){
	    			if(NumberUtils.isNumber(filesindir[i].getName())) {
	    				currentexpid=Integer.parseInt(filesindir[i].getName());
	    			}
	    			searchfiles(filesindir[i],usersid,zipoutputstream,attachedfiles, dirtoignore,servcon,depth+1,currentexpid,minexp);
	    		}else if(filesindir[i].isDirectory() && !arrayindex(usersid,filesindir[i].getName()).equals("")){
	    			//in order to avoid to attach directories currently written by the spectrometer we take only files older than 5 minute
	    			String currentid=arrayindex(usersid,filesindir[i].getName());
	    			if((System.currentTimeMillis()-filesindir[i].lastModified())>300000){
	    				if(zipoutputstream.get(currentid)==null){
	    	                File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)+"/"+currentid+".zip");
	    	                FileOutputStream baos=new FileOutputStream(zipfile);
	    	                ZipOutputStream zipout=new ZipOutputStream(baos);
	    					zipoutputstream.put(currentid, zipout);
	    					attachedfiles.put(currentid, new StringBuffer());
	    					System.err.println("made a stream for "+currentid);
	    				}
						//we find the minimum exp number
						minexp=-1;
						for(File expdir : filesindir[i].listFiles()) {
							if(NumberUtils.isNumber(expdir.getName())){
								if(minexp==-1 || Integer.parseInt(expdir.getName())<minexp)
									minexp=Integer.parseInt(expdir.getName());
							}
						}
	    				addfiles(filesindir[i], zipoutputstream.get(currentid), attachedfiles.get(currentid),currentexpid,currentid,minexp);
	    			}
	    		}else{
	    			String currentid=arrayindex(usersid,filesindir[i].getName());
	    			if(!currentid.equals("")){
	    				if(zipoutputstream.get(currentid)==null){
	    					File zipfile = new File(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",servcon)+"/"+currentid+".zip");
    		                FileOutputStream baos=new FileOutputStream(zipfile);
	    		            ZipOutputStream zipout=new ZipOutputStream(baos);
	    					zipoutputstream.put(currentid, zipout);
	    					attachedfiles.put(currentid, new StringBuffer());
	    				}
	    				ZipOutputStream zipout=zipoutputstream.get(currentid);
	    				zipout.putNextEntry(new ZipEntry(GeneralUtils.getRelativePath(rawfiledir,filesindir[i])));
	    				FileInputStream fis=new FileInputStream(filesindir[i]);
	    				int read=0;
	    				while((read=fis.read())!=-1){
	    					zipout.write(read);
	    				}
	    				fis.close();
	    				if(filesindir[i].getName().equals("audita.txt")) {
	    					analyzeAudita(currentexpid, currentid, filesindir[i]);
	    				}
	    				attachedfiles.get(currentid).append(filesindir[i]+"; ");
	    				zipout.closeEntry();
	    			}
	    		}
	    	}
    	}
    }
    
    private String arrayindex(ArrayList<String> list, String id){
    	for(int i=0;i<list.size();i++){
    		if(list.get(i).indexOf(id.toLowerCase())==0)
    			return id.toLowerCase();
    	}
    	return "";
    }
    
    private void addfiles(File toadd, ZipOutputStream zipoutputstream, StringBuffer attachedfiles, int currentexpid, String usersid, int minexp) throws IOException, TorqueException, Exception{
    	File[] filesindir=toadd.listFiles();
    	if(filesindir!=null){
	    	for(int i=0;i<filesindir.length;i++){
	    		if(filesindir[i].isDirectory() && !filesindir[i].getName().equals(".") && !filesindir[i].getName().equals("..")){
	    			if(NumberUtils.isNumber(filesindir[i].getName())) {
	    				currentexpid=Integer.parseInt(filesindir[i].getName());
	    			}
	    			addfiles(filesindir[i],zipoutputstream,attachedfiles,currentexpid, usersid,minexp);
	    		}else if(filesindir[i].isFile()){
    				if(filesindir[i].getName().equals("audita.txt") && DBSamplePeer.doSelectByUsersId(usersid).getDBExperimentDBSamplesOrderedByCategory().size()>currentexpid-minexp) {
    					analyzeAudita(currentexpid-minexp, usersid, filesindir[i]);
    				}
    				if(zipoutputstream!=null && attachedfiles!=null) {
	    				zipoutputstream.putNextEntry(new ZipEntry(GeneralUtils.getRelativePath(rawfiledir,filesindir[i])));
	    				FileInputStream fis=new FileInputStream(filesindir[i]);
	    				int read=0;
	    				while((read=fis.read())!=-1){
	    					zipoutputstream.write(read);
	    				}
	    				fis.close();
	    				attachedfiles.append(filesindir[i]+"; ");
	    				zipoutputstream.closeEntry();
    				}
	    		}
	    	}
    	}
    }
    
    public static void analyzeAudita(int currentexpidminusminexp, String usersid, File file) throws TorqueException, IOException {
        DBExperimentDBSample sample = DBSamplePeer.doSelectByUsersId(usersid).getDBExperimentDBSamplesOrderedByCategory().get(currentexpidminusminexp);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        LocalDateTime start = null;
        LocalDateTime end = null;
        int duration=0;
        int counter=1;
        while (line != null) {
          String padded = String.format("% 4d" , counter);
          if(line.contains("("+padded+",<" )) {
        	  String date=line.substring(line.indexOf("("+padded+",<" )+7,line.indexOf(">")-5);
        	  if(date.contains("T")) {
        		  date=date.replace('T',' ');
          		  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        		  end=LocalDateTime.parse(date, formatter);
        	  }
        	  else {
          		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
          		end=LocalDateTime.parse(line.substring(line.indexOf("("+padded+",<" )+7,line.indexOf(">")-6),formatter);
        	  }
            counter++;
        	}
            if(line.contains("started at")) {
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            	start=LocalDateTime.parse(line.substring(line.indexOf("started at" )+11,line.indexOf(",")-6), formatter);
            	duration+=(int)(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()-start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
            line = reader.readLine();
        }
        reader.close();
        sample.setDuration(duration);
        sample.save();    	
    }
}
