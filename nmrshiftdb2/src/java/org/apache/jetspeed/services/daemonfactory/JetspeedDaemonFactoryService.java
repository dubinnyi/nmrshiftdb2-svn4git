/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (c) 2010 Stefan Kuhn (changes marked with shk3)
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

package org.apache.jetspeed.services.daemonfactory;

import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletConfig;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.jetspeed.daemon.DaemonConfig;
import org.apache.jetspeed.daemon.DaemonContext;
import org.apache.jetspeed.daemon.DaemonEntry;
import org.apache.jetspeed.daemon.DaemonException;
import org.apache.jetspeed.daemon.DaemonNotFoundException;
import org.apache.jetspeed.daemon.DaemonThread;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.turbine.services.TurbineBaseService;
import org.apache.turbine.util.Log;

/**

@author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @author <a href="mailto:sgala@hisitech.com">Santiago Gala</a>
@version $Id: JetspeedDaemonFactoryService.java,v 1.2 2005/08/04 21:55:12 shk3 Exp $
*/
public class JetspeedDaemonFactoryService 
    extends TurbineBaseService
    implements DaemonFactoryService {

    //BEGIN define the keys for various/default Daemons
    public final static String FEEDDAEMON_KEY           = "org.apache.jetspeed.daemon.impl.FeedDaemon";
    public final static String DISKCACHEDAEMON_KEY      = "org.apache.jetspeed.daemon.impl.DiskCacheDaemon";
    public final static String BADURLMANAGERDAEMON_KEY  = "org.apache.jetspeed.daemon.impl.BadURLManagerDaemon";
    
    //END 
    
    private DaemonContext context = null;

    /**
    Stores mappings of DaemonEntry -> DaemonThreads
    */
    private Hashtable daemons = new Hashtable();
    private Hashtable threads = new Hashtable();
    
    private DaemonEntry[] entries = null;
    
    //shk3
    private ServletConfig servcon=null;


    /**
     * Late init. Don't return control until early init says we're done.
     */
    public void init( )
    {
        Log.note( "Late init for DaemonFactory called" );
        while( !getInit() ) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie ) {
                Log.note("DaemonFactory service: Waiting for init()..." );
            }
        }

    }


    /**
    Perform initialization of the DaemonFactory.  Note that this should return
    right away so that processing can continue (IE thread off everything)
    */
    public synchronized void init(ServletConfig config) {

        // already initialized
        if (getInit()) return;

        Log.note( "Early init for DaemonFactory called..." );
        
        this.context = new DaemonContext();
	//shk3
	servcon=config;

        //init daemons from config file
        Vector raw = JetspeedResources.getVector( JetspeedResources.DAEMON_ENTRY );
        this.entries = new DaemonEntry[raw.size()];
        
        for( int i = 0; i < raw.size(); ++i ) {
                
            String name = (String)raw.elementAt(i);
            String classname = JetspeedResources.getString( "daemon." + name + ".classname" );
            long interval = JetspeedResources.getLong( "daemon." + name + ".interval" );
            boolean onstartup = JetspeedResources.getBoolean( "daemon." + name + ".onstartup" );
                    
            entries[i]= new DaemonEntry( name,
                                         interval,
                                         classname,
                                         onstartup );
            
        }

        setInit(true);
        Log.note( "Early init for DaemonFactory done" );

        //Finish by starting requested Daemons...
        this.start();
    }

    /**
    <p>
    Starts any daemons that need processing.
    </p>
    
    <p>
    This should be called right after init() so that any daemons that need to be 
    started will be.  If you need to do any per-daemon initialization then do so 
    before calling start()
    </p>
    */
    public void start() {

        Log.note( "DaemonFactory:  Starting up necessary daemons." );
        
        //get all the entries.. 
        DaemonEntry[] entries = this.getDaemonEntries();
        
        for (int i = 0; i < entries.length; ++i) {

            //create Daemon threads for them and pa
            if( entries[i].onStartup() ) {
                Log.note( "DaemonFactory:  start(): starting daemon -> " + entries[i].getName() );
		//shk3
                DaemonThread dt = new DaemonThread( entries[i], servcon);         
                this.threads.put( entries[i], dt );
                dt.start();
            }
            
        }
        
    }
    
    
    /**
    Allows a Daemon to define its Thread priority through a factory.  The Thread
    that this object should return should be an implementation of itself.
    */
    public Daemon getDaemon( DaemonEntry entry ) throws DaemonException {

        //FIX ME: before instantiating a daemon ... find out if it is already setup
        
        Daemon daemon = (Daemon)this.daemons.get( entry );
        
        if( daemon != null ) {
            return daemon;
        } else {
            Log.note( "Creating daemon: " + entry.getName() );
        }
        
        try {

            daemon = (Daemon)Class.forName( entry.getClassname() ).newInstance();

            DaemonConfig dc = new DaemonConfig();
            
            daemon.init( dc, entry );
            
            this.daemons.put( entry, daemon );
            
            return daemon;
            
        } catch (ClassNotFoundException e) {
            Log.error( e );
            throw new DaemonException( "daemon not found: " + e.getMessage() );
        } catch (InstantiationException e) {
            Log.error( e );
            throw new DaemonException( "couldn't instantiate daemon: " + e.getMessage() );
        } catch (IllegalAccessException e) {
            Log.error( e );
            throw new DaemonException( e.getMessage() );
        }

    }

    /**
    Get a daemon with the given classname.
    
    @see    #getDaemon( DaemonEntry entry )
    */
    public Daemon getDaemon( String classname ) throws DaemonException {

        DaemonEntry[] entries = this.getDaemonEntries();
        
        for (int i = 0; i < entries.length; ++i) {
            if ( entries[i].getClassname().equals( classname ) ) {
                return getDaemon( entries[i] );
            }
        }

       throw new DaemonException( "daemon not found: " + classname );

    }

    /**
    */
    public DaemonContext getDaemonContext() {
        return this.context;
    }

    /**
    Kicks of processing of a Daemon.  Does the same thing as getDaemon() but
    also creates a thread and runs the daemon.
    */
    public void process( DaemonEntry entry ) throws DaemonException {

        //FIX ME:  get the status of this daemon before kicking it off again.
        int status = this.getStatus( entry );

        DaemonThread dt = (DaemonThread)this.threads.get( entry );
        
        if ( status != Daemon.STATUS_PROCESSING &&
             status != Daemon.STATUS_UNKNOWN ) {
                 
            //tell this thread to stop waiting and process immediately
            synchronized (dt) {
                dt.notify();
            }
            
        }

        if ( dt.isAlive() == false ) {
            dt.start();
        }

    }

    /**
    */
    public int getStatus(DaemonEntry entry) {

        try {
            Daemon daemon = this.getDaemon(entry);
            return daemon.getStatus();
        } catch (DaemonException e) {
            Log.error(e);
            return Daemon.STATUS_UNKNOWN;
        }
    }

    /**
    Get the last known result of the given DaemonEntry's processing
    */
    public int getResult(DaemonEntry entry) {

        try {
            Daemon daemon = this.getDaemon(entry);
            return daemon.getResult();
        } catch (DaemonException e) {
            Log.error(e);
            return Daemon.RESULT_UNKNOWN;
        }
        
    }


    /**
    Get the last known message of the given DaemonEntry's processing
    */
    public String getMessage( DaemonEntry entry ) {

        try {
            Daemon daemon = this.getDaemon(entry);
            return daemon.getMessage();
        } catch (DaemonException e) {
            Log.error(e);
            return null;
        }        
    }
    
    /**
    Get the current known DaemonEntries within the DaemonFactory
    */
    public DaemonEntry[] getDaemonEntries() {
        return this.entries;
    }

    /**
    Given the name of a DaemonEntry... get it from the DaemonFactory 
    */
    public DaemonEntry getDaemonEntry(String name) 
        throws DaemonNotFoundException 
    {

        DaemonEntry[] entries = this.getDaemonEntries();
        for (int i = 0; i < entries.length; ++i) {
            if ( entries[i].getName().equals( name ) ) {
                return entries[i];
            }
        }
        
        throw new DaemonNotFoundException( "Could not find daemon named: " + name );
    }
    
}
