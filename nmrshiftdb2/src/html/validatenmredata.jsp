<%
/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013 Stefan Kuhn
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
 */%>
 
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openscience.nmrshiftdb.util.*" %>
<%@ page import = "de.unikoeln.chemie.nmr.io.NmredataReader" %>
<%@ page import = "de.unikoeln.chemie.nmr.data.NmreData" %>
<%@ page import = "com.oreilly.servlet.multipart.*" %>
<%@ page import = "org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator" %>
<%@ page import = "org.openscience.cdk.interfaces.IMolecularFormula" %>
<%@ page import = "org.jcamp.spectrum.NMRSpectrum" %>
<%@ page import = "de.unikoeln.chemie.nmr.data.NMR2DSpectrum" %>

<html>
   <head>
      <title>Validate NMReDATA files</title>
      <link rel="shortcut icon" type="image/x-icon" href="logos/nmredata-favicon.ico">
      <style>
         .grey-box {
                     background-color: #f0f0f0; /* Light grey */
                           padding: 15px;             /* Adds space inside the box */
                              border-radius: 5px;        /* Optional: rounds the corners */
                                 }

         /* Style for your box inscription */
         .box-inscription {
               display: block;      /* Makes it sit on its own line */
               font-weight: bold;   /* Makes the text bold */
               font-size: 0.9em;    /* Slightly smaller font size (optional) */
               margin-bottom: 12px; /* Adds a clean gap between the title and the file picker */
               color: #333333;      /* Slightly darker grey for text contrast */
         }
      </style>
   </head>
   <body>
      <h1>NMReDATA Validation Service</h1>
      <img src="logos/nmredata.png"></br>
      <p>This service allows upload of an NMReDATA file, which will be parsed and checked using the <a href="https://github.com/NMReDATAInitiative/javatools">javatools</a> software. Any problems found will be printed out. No data are saved on the server from doing checks.</p>
      <div class="grey-box">
         <span class="box-inscription">File Upload Zone</span>
         <form action="validatenmredata.jsp" method="post" enctype="multipart/form-data">
            Select NMReDATA file to upload:
            <input type="file" name="fileToUpload" id="fileToUpload" accept=".sd, .sdf">
            <input type="submit" value="Upload file" name="submit">
         </form>
      </div>
      <br><br>
      <div class="grey-box">
<%
      // Verify the content type
      String contentType = request.getContentType();
      if (contentType!=null && (contentType.indexOf("multipart/form-data") >= 0)) {
         try {
            com.oreilly.servlet.multipart.Part part=null;
            MultipartParser mp = new MultipartParser( request, 64*1024*1024 );
            while ((part = mp.readNextPart()) != null) {
               if (!part.isParam()) {
                  FilePart filePart = (FilePart) part;
                  String fileName = filePart.getFileName();
                  if (fileName != null) {
                     // the part actually contained a file
                     out.println("<span class=\"box-inscription\">Result Zone for "+fileName+"</span>");
                     NmredataReader reader = new NmredataReader(filePart.getInputStream());
                     NmreData data = reader.read();
                     IMolecularFormula mfa = MolecularFormulaManipulator.getMolecularFormula(data.getMolecule());
                     out.println("The molecule in your file has formula "+MolecularFormulaManipulator.getString(mfa)+"<br>");
                     if(data.getSpectra()==null){
                        out.println("There are no spectra");
                     }else{
                        out.println("Your file contains "+data.getSpectra().size()+" spectra<br>");
                        for(int i=0; i<data.getSpectra().size(); i++){
                           if(data.getSpectra().get(i) instanceof NMRSpectrum)
                              out.println("1D spectrum "+(i+1)+" has "+((NMRSpectrum)data.getSpectra().get(i)).getPeakTable().length+" peaks<br>");
                           if(data.getSpectra().get(i) instanceof NMR2DSpectrum)
                              out.println("2D spectrum "+(i+1)+" has "+((NMR2DSpectrum)data.getSpectra().get(i)).getPeakTable().length+" peaks<br>");
                        }
                     }
                  }
               }
            }
         } catch(Exception ex) {
            out.println(ex.getMessage());
         }
      } else {
      out.println("<span class=\"box-inscription\">No file uploaded yet!</span>");
      }
%>
      </div>
   </body>
</html>
