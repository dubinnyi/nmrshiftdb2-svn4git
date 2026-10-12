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
 * Created on 28. Januar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

import java.io.*;

import java.util.*;

/**
 * Utilities useful for file operations
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class FileUtil {
    static int z                          = 0;
    public static final String[] ENCODING = { "ISO-8859-1", "UTF-8", "UTF-16" };
    public static final int ISO_LATIN_1   = 0;
    public static final int UTF8          = 1;
    public static final int UTF16         = 2;
    private static final int bufsize      = 16384;

    /** reset the file counter */
    public static void resetFileCounter() {
        z = 0;
    }


    /** get the number of files found in a folder
     * (with or without subfolders see: <CODE>getFilesInFolder<CODE>
     * @return number of files */
    public static int getFileCounter() {
        return z;
    }


    /**
     * Return a list of absolute paths for files found in a folder
     * (with or without subfolders)
     * @param dir the folder
     * @param filter the file filter
     * @param ignoreSubdirs if 'true': ignore subfolders
     * @return list of absolute paths */
    public static ArrayList getFilesInFolder(File dir, FileFilter filter, boolean ignoreSubdirs) {
        ArrayList l = new ArrayList();
        File[] f    = dir.listFiles();
        if (f != null) {
            for (int i = 0; i < f.length; i++) {
                if (f[i].isDirectory()) {
                    if (!ignoreSubdirs) {
                        ArrayList sub = getFilesInFolder(f[i], filter, ignoreSubdirs);
                        l.addAll(sub);
                    }
                }
                else {
                    z++;
                    if (filter == null) {
                        l.add(f[i].getAbsolutePath());
                    }
                    else {
                        if (filter.accept(f[i])) {
                            l.add(f[i].getAbsolutePath());
                        }
                    }
                }
            }
        }
        return l;
    }


    /** deletes all file in the folder with the specified extension
     * @param folder the target folder
     * @param extension the file extension, if null all file in the folder are deleted
     */
    public static void deleteFilesInFolder(File folder, final String extension) {
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                boolean toReturn = false;
                if (!f.isDirectory()) {
                    if ((extension == null) || (f.getName().toLowerCase().endsWith(extension))) {
                        toReturn = true;
                    }
                }
                return toReturn;
            }
        };
        ArrayList l = getFilesInFolder(folder, ff, true);
        for (int i = 0; i < l.size(); i++) {
            deleteFile((String) l.get(i));
        }
    }


    /** write data in a file, appends data to an existing file
     * @param filePath the absolute file path
     * @param contents data to write in file */
    public static void writeToFile(String filePath, String contents) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath, true);
            PrintWriter out      = new PrintWriter(fos);
            out.print(contents);
            out.close();
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }


    /**
     * Write data in a file, appends data to an existing file
     * @param filePath the absolute file path
     * @param contents data to write in file
     * @param encoding data encoding, e.g. "UTF8"
     * @param append if TRUE: append to an existing file
     */
    public static void writeToFile(String filePath, String contents, String encoding, boolean append) {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, append),
                                                                   encoding));
            out.write(contents);
            out.close();
        }
        catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    /**
     * Delete the specified file if exist
     * @param filePath the absolute file path
     */
    public static void deleteFile(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
    }


    /**
      * Deletes a file or a directory recursively
      * @param file the file or directory to delete
      * @return true if the file/directory has been successfully removed
      */
    public static boolean deleteRec(File file) {
        boolean success = true;
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                success = deleteRec(files[i]) && success;
            }
            success = file.delete() && success;
        }
        else {
            // delete file and return
            success = file.exists() && file.delete();
        }
        return success;
    }


    /**
     * Get the filename without the last extension.
     * @param f the file
     * @return the filename without extension
     */
    public static String getFileNameWithoutExtension(File f) {
        String name = null;
        String s    = f.getName();
        int i       = s.lastIndexOf('.');
        if ((i > 0) && (i < (s.length() - 1))) {
            name = s.substring(0, i).toLowerCase();
        }
        return name;
    }


    /**
     * Stores a byte array in the specified file
     * @param f the file to store the array in
     * @param data the byte array to store
     * @return TRUE if the data is stored, FALSE if storage failed
     */
    public static boolean saveByteArray(File f, byte[] data) {
        boolean toReturn         = true;
        FileOutputStream ostream = null;
        ByteArrayOutputStream p  = null;
        try {
            ostream = new FileOutputStream(f);
            int z = data.length;
            p = new ByteArrayOutputStream(z);
            p.write(data, 0, z);
            p.writeTo(ostream);
        }
        catch (FileNotFoundException fnf) {
            toReturn = false;
        }
        catch (IOException ioe) {
            toReturn = false;
        }
        finally {
            try {
                if (p != null) {
                    p.close();
                }
                if (ostream != null) {
                    ostream.close();
                }
            }
            catch (IOException ioe) {
                toReturn = false;
            }
        }
        return toReturn;
    }


    /**
     * Load the data in the specified file into a byte array
     * @param f the file containing the data
     * @return the byte array, if loading failed returns byte[0]
     */
    public static byte[] loadByteArray(File f) {
        FileInputStream fis    = null;
        BufferedInputStream bu = null;
        byte[] returnByteArray = new byte[0];
        try {
            fis = new FileInputStream(f);
            bu  = new BufferedInputStream(fis);
            int filesize = (int) f.length();
            byte[] data = new byte[filesize];
            bu.read(data, 0, filesize);
            returnByteArray = data;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            try {
                if (bu != null) {
                    bu.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
        return returnByteArray;
    }


    /**
     * Get the extension of a file.
     * @param f the file
     * @return the extension of the file
     */
    public static String getExtension(File f) {
        String ext = null;
        String s   = f.getName();
        int i      = s.lastIndexOf('.');
        if ((i > 0) && (i < (s.length() - 1))) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }


    /**
     * Copy specified source file to a new target file, existing target will be deleted
     * @param sourceFilePath the source file path
     * @param targetFilePath the target file path
     * @return TRUE if the copy was successful
     */
    public static boolean copyFiles(String sourceFilePath, String targetFilePath) {
        boolean success = true;
        try {
            deleteFile(targetFilePath);
            FileInputStream in = new FileInputStream(sourceFilePath);
            File out           = new File(targetFilePath);
            success            = copyStreamToFile(in, out);
        }
        catch (Exception e) {
            success = false;
            System.err.println(e.getMessage());
        }
        return success;
    }


    /**
     * Copies the content of the specified stream into a file
     * @param in the input stream
     * @param outFile the file to copy the stream content
     * @return TRUE if the copy was successful
     */
    public static boolean copyStreamToFile(InputStream in, File outFile) {
        boolean success = true;
        if (in != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFile);
                copyStreamToStream(in, out);
            }
            catch (IOException e) {
                success = false;
                System.err.println(e.getMessage());
            }
            finally {
                try {
                    in.close();
                    if (out != null) {
                        out.close();
                    }
                }
                catch (IOException e) {
                    success = false;
                    System.err.println(e.getMessage());
                }
            }
        }
        else {
            success = false;
        }
        return success;
    }


    /**
     * Copies the content of one stream to another stream,
     * the caller method is responsible to close the streams and for exception handling
     * @param in the input stream
     * @param out the output stream
     * @throws IOException excetion thrown if there are problems during copy
     * @return TRUE if the copy was successful
     */
    public static boolean copyStreamToStream(InputStream in, OutputStream out)
                                      throws IOException {
        boolean success = true;
        byte[] buffer   = new byte[bufsize];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        return success;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        FileUtil u = new FileUtil();
        byte[] ba  = new byte[2000000];
        byte b     = 1;
        for (int i = 0; i < 2000000; i++) {
            ba[i] = new Integer(i % 254).byteValue();
        }
        File f = new File("d:/temp/data.tmp");

        //        u.deleteFile("d:/temp/data.tmp");
        long t = System.currentTimeMillis();
        u.saveByteArray(f, ba);
        System.out.println("Time for save: " + (System.currentTimeMillis() - t));
        u.deleteFile("d:/temp/data_2.tmp");
        t = System.currentTimeMillis();
        byte[] n = u.loadByteArray(f);
        System.out.println("Time for load: " + (System.currentTimeMillis() - t));
        f = new File("d:/temp/data_2.tmp");
        u.saveByteArray(f, n);
        t = System.currentTimeMillis();
        copyFiles("d:/temp/data_2.tmp", "d:/temp/data_3.tmp");
        System.out.println("Time for copy: " + (System.currentTimeMillis() - t));
        //        u.writeToFile("e:/t/test1.xxx", "xxx");
        //        u.writeToFile("e:/t/test2.xxx", "xxx");
        //        u.writeToFile("e:/t/test2.yyy", "yyy");
        //        u.deleteFilesInFolder(new File("e:/t"), "xxx");
        //        File f = new File("D:/user/schneidg");
        //        long t = System.currentTimeMillis();
        //        System.out.println("Start search:");
        //        ArrayList l = u.getFilesInFolder(f, (FileFilter)(new lion.bayer.vis.bayerforms.util.LbQueryFilter()), false);
        //        System.out.println("Time for search: "+(System.currentTimeMillis()-t));
        //        for (int i=0; i<l.size(); i++)
        //            System.out.println(i+": "+l.get(i));
        //        System.out.println("Files checked: "+z);
    }
}
