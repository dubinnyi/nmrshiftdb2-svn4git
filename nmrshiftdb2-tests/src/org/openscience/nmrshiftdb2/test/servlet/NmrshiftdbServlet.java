/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2012,2014 Stefan Kuhn
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
package org.openscience.nmrshiftdb2.test.servlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;
import org.openscience.nmrshiftdb2.test.TestConstants;

/**
 *  Description of the Class
 *
 */
public class NmrshiftdbServlet {

	@Test
	public void testRss() throws Exception{
	      doTest("rss","nmrshiftdb2 is an open-source, open-access");
	}	
	
	@Test
	public void testTest() throws Exception{
	      doTest("test","success");
	}	

	@Test
	public void testExportinchi() throws Exception{
	      doTest("exportcmlbyinchi&inchi=1S/C6H12/c1-2-4-6-5-3-1/h1-6H2&spectrumtype=13C","cyclohexane");
	      doTestURL("https://nmrshiftdb.nmr.uni-koeln.de/NmrshiftdbServlet?nmrshiftdbaction=exportcmlbyinchi&inchi=InChI%3D1S%2FC6H6%2Fc1-2-4-6-5-3-1%2Fh1-6H&spectrumtype=13C","molecule title=\"Benzene");
	}	
	
	@Test
	public void testSearchOrPredict() throws Exception{
		doTestURL("NmrshiftdbServlet/nmrshiftdbaction/searchorpredict/smiles/CCCC/spectrumtype/13C","<peak xValue=");
		doTestURL("NmrshiftdbServlet/nmrshiftdbaction/searchorpredict/smiles/C(H)(H)(H)C(H)(H)C(H)(H)C(H)(H)(H)/spectrumtype/1H","<peak xMin=");
		doTestURL("NmrshiftdbServlet/nmrshiftdbaction/searchorpredict/smiles/CCCC/spectrumtype/13C/format/jcamp","##TITLE=SPECTRUM");
	}

	@Test
	public void testExprtmdl() throws Exception{
		doTest("exportmdl&moleculeid=234&coordsetid=1","warburganal");
	}
	
	@Test
	public void testExprtSpec() throws Exception{
	      doTest("exportspec&spectrumid=10088436&format=cml","nmrshiftdb10088436");
	}
	
	@Test
	public void testExprtMol() throws Exception{
	      doTest("exportspec&spectrumid=10088436&format=cml","nmrshiftdb10027408");
	}
	
	@Test
	public void testExprtBoth() throws Exception{
	      doTest("exportspec&spectrumid=10088436&format=cmlboth","nmrshiftdb10088436");
	}
	
	@Test
	public void testExportLastInputs() throws Exception{
	      doTest("exportlastinputs&username=shk3","<table>");
	}
	
	@Test
	public void testAssignCml() throws Exception{
	      doTest("assignspectrum&cml="+URLEncoder.encode("<cml xmlns=\"http://www.xml-cml.org/schema\"><molecule title=\"Manaarenolide G\" id=\"nmrshiftdb20116829\"><atomArray><atom id=\"a1\" elementType=\"C\" x2=\"186.0\" y2=\"283.0\" hydrogenCount=\"1\"/><atom id=\"a2\" elementType=\"C\" x2=\"137.0001\" y2=\"266.0888\" hydrogenCount=\"2\"/>"+
	              "<atom id=\"a3\" elementType=\"C\" x2=\"211.0\" y2=\"258.0\" hydrogenCount=\"0\"/><atom id=\"a4\" elementType=\"O\" x2=\"195.0\" y2=\"317.0\" hydrogenCount=\"1\"/><atom id=\"a5\" elementType=\"C\" x2=\"112.0\" y2=\"295.0\" hydrogenCount=\"2\"/><atom id=\"a6\" elementType=\"C\" x2=\"242.0\" y2=\"275.0\" hydrogenCount=\"2\"/><atom id=\"a7\" elementType=\"C\" x2=\"211.0\" y2=\"222.0\" hydrogenCount=\"3\"/>"+
	              "<atom id=\"a8\" elementType=\"C\" x2=\"133.0\" y2=\"314.0\" hydrogenCount=\"0\"/><atom id=\"a9\" elementType=\"C\" x2=\"278.0\" y2=\"275.0\" hydrogenCount=\"2\"/><atom id=\"a10\" elementType=\"H\" x2=\"242.0\" y2=\"311.0\" hydrogenCount=\"0\"/><atom id=\"a11\" elementType=\"C\" x2=\"116.0\" y2=\"345.0\" hydrogenCount=\"1\"/><atom id=\"a12\" elementType=\"C\" x2=\"158.0\" y2=\"314.0\" hydrogenCount=\"3\"/>"+
	              "<atom id=\"a13\" elementType=\"C\" x2=\"296.0\" y2=\"306.0\" hydrogenCount=\"1\"/><atom id=\"a14\" elementType=\"C\" x2=\"142.0\" y2=\"379.0\" hydrogenCount=\"2\"/><atom id=\"a15\" elementType=\"C\" x2=\"265.0\" y2=\"324.0\" hydrogenCount=\"2\"/><atom id=\"a16\" elementType=\"C\" x2=\"330.0\" y2=\"315.0\" hydrogenCount=\"0\"/><atom id=\"a17\" elementType=\"C\" x2=\"183.0\" y2=\"364.0\" hydrogenCount=\"2\"/>"+
	              "<atom id=\"a18\" elementType=\"C\" x2=\"265.0\" y2=\"360.0\" hydrogenCount=\"1\"/><atom id=\"a19\" elementType=\"C\" x2=\"348.0\" y2=\"346.0\" hydrogenCount=\"0\"/><atom id=\"a20\" elementType=\"C\" x2=\"219.0\" y2=\"384.0\" hydrogenCount=\"0\"/><atom id=\"a21\" elementType=\"O\" x2=\"299.0\" y2=\"369.0\" hydrogenCount=\"0\"/><atom id=\"a22\" elementType=\"O\" x2=\"236.0\" y2=\"407.0\" hydrogenCount=\"1\"/>"+
	              "<atom id=\"a23\" elementType=\"C\" x2=\"207.0\" y2=\"416.0\" hydrogenCount=\"3\"/><atom id=\"a24\" elementType=\"O\" x2=\"382.3607\" y2=\"356.7396\" hydrogenCount=\"0\"/><atom id=\"a25\" elementType=\"C\" x2=\"351.8369\" y2=\"286.3792\" hydrogenCount=\"2\"/></atomArray><bondArray><bond id=\"b1\" atomRefs2=\"a2 a1\" order=\"S\"/><bond id=\"b2\" atomRefs2=\"a1 a3\" order=\"S\"/><bond id=\"b3\" atomRefs2=\"a1 a4\" order=\"S\"><bondStereo dictRef=\"cml:H\"/></bond>"+
	              "<bond id=\"b4\" atomRefs2=\"a2 a5\" order=\"S\"/><bond id=\"b5\" atomRefs2=\"a3 a6\" order=\"D\"/><bond id=\"b6\" atomRefs2=\"a3 a7\" order=\"S\"/><bond id=\"b7\" atomRefs2=\"a5 a8\" order=\"S\"/><bond id=\"b8\" atomRefs2=\"a6 a9\" order=\"S\"/><bond id=\"b9\" atomRefs2=\"a6 a10\" order=\"S\"/><bond id=\"b10\" atomRefs2=\"a8 a11\" order=\"D\"/><bond id=\"b11\" atomRefs2=\"a8 a12\" order=\"S\"/><bond id=\"b12\" atomRefs2=\"a9 a13\" order=\"S\"/><bond id=\"b13\" atomRefs2=\"a14 a11\" order=\"S\"/><bond id=\"b14\" atomRefs2=\"a13 a15\" order=\"S\"/>"+
	              "<bond id=\"b15\" atomRefs2=\"a13 a16\" order=\"S\"><bondStereo dictRef=\"cml:H\"/></bond><bond id=\"b16\" atomRefs2=\"a17 a14\" order=\"S\"/><bond id=\"b17\" atomRefs2=\"a15 a18\" order=\"S\"/><bond id=\"b18\" atomRefs2=\"a16 a19\" order=\"S\"/><bond id=\"b19\" atomRefs2=\"a20 a17\" order=\"S\"/><bond id=\"b20\" atomRefs2=\"a18 a20\" order=\"S\"/><bond id=\"b21\" atomRefs2=\"a18 a21\" order=\"S\"><bondStereo dictRef=\"cml:H\"/></bond><bond id=\"b22\" atomRefs2=\"a19 a21\" order=\"S\"/><bond id=\"b23\" atomRefs2=\"a20 a22\" order=\"S\"><bondStereo dictRef=\"cml:H\"/>"+
	              "</bond><bond id=\"b24\" atomRefs2=\"a20 a23\" order=\"S\"><bondStereo dictRef=\"cml:W\"/></bond><bond id=\"b25\" atomRefs2=\"a19 a24\" order=\"D\"/><bond id=\"b26\" atomRefs2=\"a16 a25\" order=\"D\"/></bondArray><scalar dictRef=\"cdk:molecularProperty\" title=\"Remark\" dataType=\"xsd:string\">NMRShiftDB 20116829 bioclipse_shk3_2006-08-29_08:11:32_0000</scalar></molecule><spectrum type=\"NMR\" id=\"sid_51c9184d34673989f6a5b668e4f91667335cf2bd\"><peakList><peak xValue=\"11.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p0\" atomRefs=\"\"/>"+
	              "<peak xValue=\"14.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p1\" atomRefs=\"\"/><peak xValue=\"22.1\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p2\" atomRefs=\"\"/><peak xValue=\"24.7\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p3\" atomRefs=\"\"/><peak xValue=\"27.7\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p4\" atomRefs=\"\"/><peak xValue=\"29.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p5\" atomRefs=\"\"/>"+
	              "<peak xValue=\"29.7\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p6\" atomRefs=\"\"/><peak xValue=\"34.1\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p7\" atomRefs=\"\"/><peak xValue=\"38.2\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p8\" atomRefs=\"\"/><peak xValue=\"38.4\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p9\" atomRefs=\"\"/><peak xValue=\"73.8\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p10\" atomRefs=\"\"/><peak xValue=\"77.0\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p11\" atomRefs=\"\"/><peak xValue=\"85.1\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p12\" atomRefs=\"\"/><peak xValue=\"124.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p13\" atomRefs=\"\"/><peak xValue=\"124.7\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p14\" atomRefs=\"\"/>"+
	    		  "<peak xValue=\"125.9\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p15\" atomRefs=\"\"/><peak xValue=\"135.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p16\" atomRefs=\"\"/><peak xValue=\"138.8\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p17\" atomRefs=\"\"/><peak xValue=\"165.8\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p18\" atomRefs=\"\"/><peak xValue=\"138.8\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p19\" atomRefs=\"\"/></peakList><metadataList><metadata name=\"nmr:OBSERVENUCLEUS\" content=\"13C\" /></metadataList></spectrum></cml>", StandardCharsets.UTF_8),"atomRefs=\"a7\" /><peak");
	}
	
	@Test
	public void testQuickCheckViaURL() throws Exception{
		String url="nmrshiftdb/media-type/html/user/anon/page/default.psml/js_pane/P-Quickcheck";
		String firstreturn=doTestURL(url,"<sup>13</sup>C Shift");
		String sessionid=firstreturn.substring(firstreturn.indexOf("jsessionid=")+11,firstreturn.indexOf("\">",firstreturn.indexOf("jsessionid=")+11));
		url=url+";jsessionid="+sessionid+"?eventSubmit_doSubmitquickcheck=Transfer&";
		url=url+"inputh13=7.45648&";
		url=url+"inputh14=7.52601&";
		url=url+"inputh11=0.91704&";
		url=url+"inputh12=7.45648&";
		url=url+"inputh4=6.90978&";
		url=url+"inputh5=4.86272&";
		url=url+"inputh10=1.37074&";
		url=url+"inputh6=2.07826&";
		url=url+"nmrshiftdbaction=quickcheck&";
		url=url+"inputh7=7.79883&";
		url=url+"inputh8=7.79883&";
		url=url+"inputh9=1.4326&";
		url=url+"shiftsc=&";
		url=url+"shiftsh=&";
		url=url+"MolTxt=%0D%0ASciformation+1479162891027%0D%0A%0D%0A+15+15++0++0++0++0++0++0++0++0999+V2000%0D%0A++431.0000+-828.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++431.0000+-993.0000++++0.0000+O+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++576.0000+-743.0000++++0.0000+N+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++286.0000+-743.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++576.0000+-579.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++717.0000+-497.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++717.0000+-331.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++286.0000+-578.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++145.0000+-824.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++859.0000+-250.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++859.0000++-83.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A+1000.0000++++0.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++145.0000+-495.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++++0.0000+-742.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++++0.0000+-575.0000++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%0D%0A++1++2++2++0++0++0++0%0D%0A++1++3++1++0++0++0++0%0D%0A++1++4++1++0++0++0++0%0D%0A++3++5++1++0++0++0++0%0D%0A++4++8++1++0++0++0++0%0D%0A++4++9++2++0++0++0++0%0D%0A++5++6++2++0++0++0++0%0D%0A++6++7++1++0++0++0++0%0D%0A++7+10++1++0++0++0++0%0D%0A++8+13++2++0++0++0++0%0D%0A++9+14++1++0++0++0++0%0D%0A+10+11++1++0++0++0++0%0D%0A+11+12++1++0++0++0++0%0D%0A+13+15++1++0++0++0++0%0D%0A+14+15++2++0++0++0++0%0D%0AM++END";
		doTestURL(url,"<input type=\"text\"  size=\"4\" name=\"inputh4\" value=\"6.90978\">");
	}
	
	public void doTest(String action, String expectedText) throws Exception{
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

        });

		TrustManager[] trustManagers = null;
        SSLContext context = null;
        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new HttpsTrustManager()};
        }

        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        Thread.sleep(1000);
        HttpsURLConnection.setDefaultSSLSocketFactory(context != null ? context.getSocketFactory() : null);	      
        String data = "?nmrshiftdbaction="+action;
	      URL url = new URL( TestConstants.TESTSERVER+TestConstants.NMRSHIFTDB_APPLICATION_PATH+"NmrshiftdbServlet"+data);
          HttpURLConnection huc = (HttpURLConnection) url.openConnection();
          huc.setUseCaches(false);
          huc.setRequestMethod("GET");
          huc.connect();
	      // Get the response
          BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream())); 
	      for ( String s; ( s = in.readLine() ) != null; ){
	    	  System.out.println(s+"\r\n");
	    	  if(s.contains(expectedText)){
	    		  in.close();
	    		  return;    		 
	    	  }
	      }
	      in.close();
	      throw new Exception("unexpected text, expected: "+expectedText);
	}	
	
	public String doTestURL(String urlstring, String expectedText) throws Exception{
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

        });

		TrustManager[] trustManagers = null;
        SSLContext context = null;
        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new HttpsTrustManager()};
        }

        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        
        Thread.sleep(1000);
        HttpsURLConnection.setDefaultSSLSocketFactory(context != null ? context.getSocketFactory() : null);	      
        URL url = new URL( TestConstants.TESTSERVER+TestConstants.NMRSHIFTDB_APPLICATION_PATH+urlstring);
          HttpURLConnection huc = (HttpURLConnection) url.openConnection();
          huc.setUseCaches(false);
          huc.setRequestMethod("GET");
          huc.connect();

	      // Get the response
	      StringBuffer value=new StringBuffer();
	      BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
	      for ( String s; ( s = in.readLine() ) != null; ){
	    	  System.out.println(s+"\r\n");
	    	  value.append(s);
	    	  if(s.contains(expectedText)){
	    		  in.close();
	    		  return value.toString();    		 
	    	  }
	      }
	      in.close();
	      throw new Exception("unexpected text, expected: "+expectedText);
	}
	
	@Test
	public void testQuickCheck() throws Exception{
		doTest("quickcheck&data=%7B%22inputs%22%3A%5B%7B%22id%22%3A1%2C%22type%22%3A%22nmr%3B13C%3B1d%22%2C%22shifts%22%3A%22117%3B123%3B103%22%2C%22solvent%22%3A%22Chloroform-D1%20%28CDCl3%29%22%7D%2C%7B%22id%22%3A2%2C%22type%22%3A%22ms%22%2C%22shifts%22%3A%221%3B2%3B3%22%2C%22solvent%22%3A%22Chloroform-D1%20%28CDCl3%29%22%7D%5D%2C%22moltxt%22%3A%2262%2D53%2D3%5Cn%5Cn%5Cn++7++7++0++0++0++0++0++0++0++0999+V2000%5Cn+++%2D0.1116++++1.3830++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn+++%2D0.8260++++0.9705++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn+++%2D0.8260++++0.1454++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn+++%2D0.1116+++%2D0.2670++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn++++0.6028++++0.1454++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn++++0.6028++++0.9705++++0.0000+C+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn+++%2D0.1116+++%2D1.0920++++0.0000+N+++0++0++0++0++0++0++0++0++0++0++0++0%5Cn++1++2++2++0++0++0++0%5Cn++2++3++1++0++0++0++0%5Cn++3++4++2++0++0++0++0%5Cn++4++5++1++0++0++0++0%5Cn++5++6++2++0++0++0++0%5Cn++6++1++1++0++0++0++0%5Cn++4++7++1++0++0++0++0%5CnM++END%22%7D","\"accept\":0,\"warning\":0,\"reject\":5,\"missing\":1,\"total\":6");
	}
}

class HttpsTrustManager implements X509TrustManager {
    private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

    @Override
    public void checkClientTrusted(
            X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {

    }

    @Override
    public void checkServerTrusted(
            X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {

    }

    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
        return true;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return _AcceptedIssuers;
    }
}
