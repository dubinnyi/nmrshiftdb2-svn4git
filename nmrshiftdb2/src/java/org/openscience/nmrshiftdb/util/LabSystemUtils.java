/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2013,2015 Stefan Kuhn
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBRawFile;
import org.openscience.nmrshiftdb.om.DBSample;
import org.openscience.nmrshiftdb.om.DBSamplePeer;

/**
 * Various utility methods for the lab system portlets.
 * 
 * @author shk3
 *
 */
public class LabSystemUtils {

	  /**
	   * Puts the content of a directory into the context. This is used if the user attaches files to an order and browses into a new directory.
	   * 
	   * @param context The current context.
	   * @param data The current RunData.
	   * @param toshow The sample currently shown.
	   * @param fromUser true=a user is browsing, false=a lab worker is browsing.
	   * @throws FileNotFoundException
	   * @throws IOException
	   * @throws TorqueException
	   */
	public static void processBrowse(Context context, RunData data, DBSample toshow, boolean fromUser) throws FileNotFoundException, IOException, TorqueException {
	      File rawfiledir=new File(GeneralUtils.getNmrshiftdbProperty(fromUser ? "rawfiledirectoryuser" : "rawfiledirectory",data));
	      File currentraw=((File)data.getSession().getAttribute("currentraw"));
	      StringBuffer choosenfiles=new StringBuffer();
	      if(data.getSession().getAttribute("choosenfiles")==null){
	        data.getSession().setAttribute("choosenfiles", new Vector<File>());
	      }
	      @SuppressWarnings("unchecked")
	      Vector<File> v=(Vector<File>)data.getSession().getAttribute("choosenfiles");
	      if(data.getParameters().get("nmrshiftdbaction")!=null && data.getParameters().get("nmrshiftdbaction").equals("addtochoosenfiles")){
	    	if(!data.getParameters().get("filename").equals("..")){
	          v.add(new File(currentraw+"/"+data.getParameters().get("filename")));
	        }else{
	          data.getSession().setAttribute("currentraw",new File(((File)data.getSession().getAttribute("currentraw")).toString().substring(0,((File)data.getSession().getAttribute("currentraw")).toString().lastIndexOf(File.separator))));
	        }
	      }
	      if(data.getSession().getAttribute("currentraw")==null)
	          data.getSession().setAttribute("currentraw",rawfiledir);
	      if(data.getParameters().get("nmrshiftdbaction")!=null && data.getParameters().get("nmrshiftdbaction").equals("browseto")){
	        if(!data.getParameters().get("filename").equals("..")){
	          data.getSession().setAttribute("currentraw",new File(((File)data.getSession().getAttribute("currentraw"))+File.separator+data.getParameters().get("filename")));
	        }else{
	          data.getSession().setAttribute("currentraw",new File(((File)data.getSession().getAttribute("currentraw")).toString().substring(0,((File)data.getSession().getAttribute("currentraw")).toString().lastIndexOf(File.separator))));
	        }
	      }
	      if(data.getParameters().get("nmrshiftdbaction")!=null && data.getParameters().get("nmrshiftdbaction").equals("browsetofull")){
	    	  if(data.getParameters().get("filename").equals("."))
	    		  data.getSession().setAttribute("currentraw",rawfiledir);
	    	  else
	    		  data.getSession().setAttribute("currentraw",new File(data.getParameters().get("filename")));
	      }
	      for(int i=0;i<v.size();i++){
	        choosenfiles.append(((File)v.get(i)).getName()+"\r\n");
	      }
	      context.put("choosenfiles",choosenfiles);
	      List<DBRawFile> attachedfiles=toshow.getDBRawFiles();
	      StringBuffer oldfiles=new StringBuffer();
	      for(int i=0;i<attachedfiles.size();i++){
	    	  oldfiles.append(((DBRawFile)attachedfiles.get(i)).getContainedFiles());
	      }
	      context.put("oldfiles",oldfiles.toString());
	      List<List<String>> files=new Vector<List<String>>();
	      File[] filesindir=((File)data.getSession().getAttribute("currentraw")).listFiles();
	      for(int i=0;i<filesindir.length;i++){
	    	  //The directory must not be compressedfile directory (which should not be browseable)  and we must either be not resricted or not on the machine level or the dirctory must be for the right machine
	    	  //The second part ensures that only file for the right machine can be selected
	    	  if(!filesindir[i].getAbsolutePath().equals(GeneralUtils.getNmrshiftdbProperty("compressedfiledirectory",data))
	    			  && (!fromUser 
	    					  || !filesindir[i].getAbsolutePath().matches(rawfiledir.getAbsolutePath()+File.separator+"[a-zA-Z0-9]*") 
	    					  || filesindir[i].getAbsolutePath().equals(rawfiledir.getAbsolutePath()+File.separator+DBSamplePeer.retrieveByPK(new NumberKey(data.getParameters().get("id"))).getDBMachine().getName()))){
		        Vector<String> local=new Vector<String>();
		        local.add("portal/js_pane/P-NMR+lab+administration;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=addtochoosenfiles&filename="+filesindir[i].getName()+"&submitorshow=show&id="+toshow.getSampleId());
		        local.add(filesindir[i].getName());
		        local.add("portal/js_pane/P-NMR+lab+administration;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=browseto&filename="+filesindir[i].getName()+"&submitorshow=show&id="+toshow.getSampleId());
		        files.add(local);
		        local.add(new Boolean(!filesindir[i].isFile()).toString());
	    	  }
	      }
	      Collections.sort(files, new LocalComparator());
	      if(!((File)data.getSession().getAttribute("currentraw")).getCanonicalFile().equals(rawfiledir.getCanonicalFile())){
	    	  Vector<String> local=new Vector<String>();
	          local.add("portal/js_pane/P-NMR+lab+administration;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=addtochoosenfiles&filename=..&submitorshow=show&id="+toshow.getSampleId());
	          local.add("..");
	          local.add("portal/js_pane/P-NMR+lab+administration;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=browseto&filename=..&submitorshow=show&id="+toshow.getSampleId());
	          files.add(local);
	          local.add(new Boolean(true).toString());
	    	  context.put("israwfiledir","false");
	      }else{
	    	  context.put("israwfiledir","true");
	      }
	      context.put("files",files);
	      StringTokenizer st= new StringTokenizer(((File)data.getSession().getAttribute("currentraw")).getCanonicalPath(), File.separator);
	      Map<String, String> breadcrumb=new HashMap<String,String>();
	      StringBuffer fulldir=new StringBuffer();
	      while(st.hasMoreTokens()){
	    	  String directory=st.nextToken();
	    	  fulldir.append(File.separator+directory);	    		  
	    	  if(rawfiledir.getCanonicalPath().indexOf(directory)==-1)
	    		  breadcrumb.put(directory, "portal/js_pane/P-NMR+lab+administration;jsessionid="+data.getSession().getId()+"?nmrshiftdbaction=browsetofull&filename="+fulldir.toString()+"&submitorshow=show&id="+toshow.getSampleId());
	      }
	      context.put("breadcrumb",breadcrumb);
	  }

	
	   public static class LocalComparator implements Comparator<List<String>>{
	        public int compare(List<String> o1, List<String> o2){
	            return ((String)o1.get(1)).compareTo(((String)o2.get(1)));
	        }
	   }
}
