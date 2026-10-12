/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright 2010 Stefan Kuhn
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *     "Apache Jetspeed" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" or
 *    "Apache Jetspeed", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jetspeed.daemon;

import javax.servlet.ServletConfig;

import org.apache.jetspeed.services.daemonfactory.DaemonFactory;
import org.apache.turbine.util.Log;
import org.openscience.nmrshiftdb.daemons.AbstractDaemonWithServletConfig;

/**
@author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
@version $Id: DaemonThread.java,v 1.6 2005/08/04 21:55:11 shk3 Exp $
*/
public class DaemonThread extends Thread {

    private Daemon daemon = null;
    //shk3
    private ServletConfig servcon=null;
    
    /**
    */
    public DaemonThread( DaemonEntry entry ) {
        
        super( "DaemonThread:" + entry.getName() );
        try {
            this.daemon = DaemonFactory.getDaemon( entry );

            this.setPriority( Thread.MIN_PRIORITY );
        } catch (DaemonException e) {
            //really screwed here.
            Log.error(e);
        }
    }
    
    //shk3
    public DaemonThread( DaemonEntry entry, ServletConfig servcon ) {
        
        super( "DaemonThread:" + entry.getName() );
	this.servcon=servcon;
        try {
            this.daemon = DaemonFactory.getDaemon( entry );

            this.setPriority( Thread.MIN_PRIORITY );
        } catch (DaemonException e) {
            //really screwed here.
            Log.error(e);
        }
    }
    
    /**
    */
    public Daemon getDaemon() {
        return this.daemon;
    }
    
    /**
    */
    public void run() {
        //at the very minimum this daemon is processing...
        
        DaemonEntry de = this.getDaemon().getDaemonEntry();
        
        Log.note( "DaemonThread: started processing daemon " + de.getName() );

        if ( de.onStartup() ) {
            this.runDaemon( this.getDaemon() );
        }
        

        while( true ) {

            //move the seconds to miliseconds
            try {

                synchronized( this ) {
                    this.wait( de.getInterval() * 1000 );
                }

            } catch (InterruptedException e) {
                //this is a normal situation.  
                //the DaemonFactory may want to stop this thread form 
                //sleeping and call interrupt() on this thread.
            }

            this.runDaemon( this.getDaemon() );
        
        }
    

    }

    /**
    */
    private void runDaemon( Daemon daemon ) {
 
        daemon.setStatus( Daemon.STATUS_PROCESSING );

        Log.note( "DaemonThread -> PROCESSING daemon -> " + daemon.getDaemonEntry().getName() );
        
        try {
          if(daemon instanceof AbstractDaemonWithServletConfig){
            ((AbstractDaemonWithServletConfig)daemon).run(servcon);
          }
          else
          {
            daemon.run();
          }
        } catch ( Throwable t ) {
            Log.error( "Could not process Daemon: " + daemon.getDaemonEntry().getName(), t );
        }

        Log.note( "DaemonThread -> *DONE* PROCESSING daemon -> " + daemon.getDaemonEntry().getName() );        

        daemon.setStatus( Daemon.STATUS_PROCESSED );        

    }

    
}
