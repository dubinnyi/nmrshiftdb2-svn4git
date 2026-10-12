/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010 Stefan Kuhn
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

import javax.servlet.ServletConfig;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.ServletUtils;
/**
A daemon that deletes all mol files and files needed for export older than 60 minutes


@author Stefan Kuhn
*/
public class DeleteOldMolfiles extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon) {
        Log.info("checking old molfiles");
        this.setResult( Daemon.RESULT_PROCESSING );
        File nmrshiftdbDir = new File(ServletUtils.expandRelative(servcon, "/nmrshiftdbhtml"));
        File[] filesinnmrshiftdbdir=nmrshiftdbDir.listFiles();
        for(int i=0;i<filesinnmrshiftdbdir.length;i++){
          if(filesinnmrshiftdbdir[i].lastModified()<System.currentTimeMillis()-3600000&&filesinnmrshiftdbdir[i].getName().length()>4&&filesinnmrshiftdbdir[i].getName().substring(filesinnmrshiftdbdir[i].getName().length()-4).equals(".mol")){
            filesinnmrshiftdbdir[i].delete();
          }
        }
        File tmpDir = new File(ServletUtils.expandRelative(servcon, "/WEB-INF/tmp"));
        File[] filesintmpdir=tmpDir.listFiles();
        for(int i=0;i<filesintmpdir.length;i++){
          if(filesintmpdir[i].lastModified()<System.currentTimeMillis()-3600000 && !filesintmpdir[i].getName().startsWith("descsvm.model") && !filesintmpdir[i].getName().startsWith("j48") && !filesintmpdir[i].getName().startsWith("spectra")){
            filesintmpdir[i].delete();
          }
        }
        this.setResult( Daemon.RESULT_SUCCESS );
    }
}
