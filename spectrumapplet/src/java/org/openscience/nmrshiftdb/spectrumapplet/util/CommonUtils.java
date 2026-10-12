/*
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 *
 * Created on 28. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

import java.io.*;

import java.net.*;

import java.rmi.dgc.*;

import java.util.*;

/**
 * Common utils
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class CommonUtils {
    private static final String location = "conf/spec.properties";
    private static Properties props;

    public static String getNewGUID() {
        //The UID class can generate identifiers that are unique over time within a JVM:
        //java.rmi.server.UID uid = new java.rmi.server.UID();
        //For the detail heads, UID consists of a unique number based on a hashcode, 
        //system time and a counter, and a VMID contains a UID and adds a SHA hash 
        // based on IP address.
        return new VMID().toString();
    }


    public static String getProperty(String key, String defaultValue) {
        String r = getProperty(key);
        if (r == null) {
            r = defaultValue;
        }
        return r;
    }


    public static String getProperty(String key) {
        if (props == null) {
            props = new Properties();
            InputStream is = null;
            try {
                is = CommonUtils.getResource(location).openStream();
                props.load(is);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String r = null;
        if (props != null) {
            r = props.getProperty(key);
        }
        return r;
    }


    public static String getFileContent(String resource) {
        String r                  = null;
        InputStream is            = null;
        ByteArrayOutputStream out = null;
        try {
            is  = CommonUtils.getResource(resource).openStream();
            out = new ByteArrayOutputStream();
            FileUtil.copyStreamToStream(is, out);
            r = out.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return r;
    }


    public static double getNumberProperty(String key, double defaultValue) {
        double r = Double.NaN;
        try {
            r = Double.parseDouble(getProperty(key));
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            r = defaultValue;
        }
        return r;
    }
    
    public static boolean getBooleanProperty(String key) {
        boolean r = Boolean.valueOf(getProperty(key)).booleanValue();
        return r; 
    }


    public static double[] getNumberArrayProperty(String key) {
        double[] r = null;
        try {
            String s          = getProperty(key);
            StringTokenizer t = new StringTokenizer(s, ",");
            r                 = new double[t.countTokens()];
            int z             = 0;
            while (t.hasMoreTokens()) {
                r[z] = Double.parseDouble(t.nextToken());
                z++;
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            r = null;
        }
        return r;
    }


    /**
     * Return a resource URL from resource name, e.g.: 'conf/log4j.properties'
     * @param resource resource to find
     * @return resource url or NULL if the resource was not found
     */
    public static URL getResource(String resource) {
        URL url = null;
        try {
            url = Thread.currentThread().getContextClassLoader().getResource(resource);
        }
        catch (Exception e) {
            // ignore
            ;
        }
        if (url == null) {
            url = CommonUtils.class.getClassLoader().getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        return url;
    }


    public static void main(String[] args) {

        /*
           java.rmi.server.UID uid = new java.rmi.server.UID();
           for (int i = 0; i < 50; i++) {
               System.out.println("GUID: " + getNewGUID());
               //System.out.println("isUnique: "+new VMID().isUnique());
           }
         */
        System.out.println("j(CH) is " + CommonUtils.getNumberProperty("j(CH)", -999));
    }
}
