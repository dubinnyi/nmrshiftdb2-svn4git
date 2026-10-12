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
import javax.servlet.ServletConfig;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.jetspeed.daemon.DaemonConfig;
import org.apache.jetspeed.daemon.DaemonEntry;


/**
An abstract daemon. You should not use this class to implement a daemon. but AbstractDaemonWith(out)ServletConfig.
This makes sure that the appropriate run-method is called from DaemonThread.


@author Stefan Kuhn
*/
public abstract class AbstractDaemon implements Daemon {


    private int status = Daemon.STATUS_NOT_PROCESSED;
    private int result = Daemon.RESULT_UNKNOWN;
    private DaemonConfig config = null;
    private DaemonEntry entry = null;
    
    public abstract void run();
    /**
    Does the task
    */
    public abstract void run(ServletConfig servcon) throws Exception;

    /**
    Init this Daemon from the DaemonFactory
    @see Daemon#init
    */
    public void init(DaemonConfig config, DaemonEntry entry) {
        this.config = config;
        this.entry = entry;
    }
    
    /**
    */
    public DaemonConfig getDaemonConfig() {
        return this.config;
    }

    /**
    */
    public DaemonEntry getDaemonEntry() {
        return this.entry;
    }
    
    /**
    Return the status for this Daemon

    @see Daemon#STATUS_NOT_PROCESSED
    @see Daemon#STATUS_PROCESSED
    @see Daemon#STATUS_PROCESSING
    */
    public int getStatus() {
        return this.status;
    }
    
    /**
    Set the status for this Daemon

    @see #STATUS_NOT_PROCESSED
    @see #STATUS_PROCESSED
    @see #STATUS_PROCESSING
    */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
    @see Daemon#getResult()
    */
    public int getResult() {
        return this.result;
    }

    /**
    @see Daemon#setResult(int result)
    */
    public void setResult( int result ) {
        this.result = result;
    }
    
    /**
    @see Daemon#getMessage()
    */
    public String getMessage() {
        return null;
    }
}

