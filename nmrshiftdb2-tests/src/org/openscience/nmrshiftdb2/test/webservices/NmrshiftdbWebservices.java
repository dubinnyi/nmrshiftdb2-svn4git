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
package org.openscience.nmrshiftdb2.test.webservices;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.torque.NoRowsException;
import org.apache.torque.Torque;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Assert;
import org.junit.Test;
import org.nmrshiftdb.www.schemas._2006._10.nmrshiftdbtypes.DoAssignmentParameters;
import org.nmrshiftdb.www.schemas._2006._10.nmrshiftdbtypes.DoSubmitParameters;
import org.nmrshiftdb.www.schemas._2006._10.nmrshiftdbtypes.PublishParameters;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.DoAssignmentRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.DoAssignmentResponseValueDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.DoSubmitRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.DoSubmitResponseValueDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.GetSpectrumTypesRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.GetSpectrumTypesResponseValueDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.GetUserNumberRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.GetUserNumberResponseValueDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.PublishRequestParametersDocument;
import org.nmrshiftdb.www.ws._2006._10.nmrshiftdb.PublishResponseValueDocument;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.util.AtomUtils;
import org.openscience.nmrshiftdb.webservices.NMRShiftDBStub;
import org.openscience.nmrshiftdb2.test.TestConstants;
import org.xml_cml.www.schema.CmlDocument;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.element.CMLCml;

import nu.xom.Document;

public class NmrshiftdbWebservices {

	NMRShiftDBStub stub;
	

	@Test
	public void testDoPrediction() throws Exception{
		String filename = "org/openscience/nmrshiftdb2/test/webservices/butadiene.cml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
    	CMLReader cmlreader=new CMLReader(ins);
    	IChemFile cfile=new ChemFile();
    	cfile=(IChemFile)cmlreader.read(cfile);
    	cmlreader.close();
    	IAtomContainer mol=cfile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
    	AtomUtils.addImplicitHydrogensToSatisfyValency(mol);
        String smiles = SmilesGenerator.unique().aromatic().create(mol);
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/predict/smiles/"+smiles+"/spectrumtype/13C/format/cml?solvent=Chloroform-D1%20%28CDCl3%29");
        System.out.println(res.toString());
		CMLCml cmlcml = (CMLCml)new CMLBuilder().build(new ByteArrayInputStream(res.getBytes())).getRootElement();
        Assert.assertEquals(4,cmlcml.getChildCMLElement("spectrum",0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0).getChildElements("peak","http://www.xml-cml.org/schema").size());
	}
	
	@Test
	public void testDoPredictOrSearch() throws Exception{
		String filename = "org/openscience/nmrshiftdb2/test/webservices/butadiene.cml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
    	CMLReader cmlreader=new CMLReader(ins);
    	IChemFile cfile=new ChemFile();
    	cfile=(IChemFile)cmlreader.read(cfile);
    	cmlreader.close();
    	IAtomContainer mol=cfile.getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
    	AtomUtils.addImplicitHydrogensToSatisfyValency(mol);
        String smiles = SmilesGenerator.unique().aromatic().create(mol);
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/searchorpredict/smiles/"+smiles+"/spectrumtype/13C/format/cml?solvent=Chloroform-D1%20%28CDCl3%29");
        System.out.println(res.toString());
		CMLCml cmlcml = (CMLCml)new CMLBuilder().build(new ByteArrayInputStream(res.getBytes())).getRootElement();
        Assert.assertEquals(4,cmlcml.getChildCMLElement("spectrum",0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0).getChildElements("peak","http://www.xml-cml.org/schema").size());
	}
	
	@Test
	public void testDosearchstructure() throws Exception{
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/searchstructure/smiles/CCC/spectrumtype/13C/format/cml");
        System.out.println(res.toString());
		CMLCml cmlcml = (CMLCml)new CMLBuilder().build(new ByteArrayInputStream(res.getBytes())).getRootElement();
        Assert.assertEquals(2,cmlcml.getChildCMLElement("spectrum",0).getChildElements("peakList","http://www.xml-cml.org/schema").get(0).getChildElements("peak","http://www.xml-cml.org/schema").size());
	}
	
	public static String dorequest(String endpoint) throws IOException {
		    URL url = new URL(endpoint);
		    InputStream in = url.openStream();
		    BufferedInputStream bufIn = new BufferedInputStream(in);
		    StringBuffer doc=new StringBuffer();
		    int data;
		    while((data = bufIn.read())!=-1){
		    	doc.append((char)data);
		    }
		    return doc.toString();
	 }
	
	public static String dorequestIgnoringCertificates(String endpoint) throws Exception {

	    TrustManager[] trustAllCerts = new TrustManager[]{
	        new X509TrustManager() {
	            @Override
	            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

	            @Override
	            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

	            @Override
	            public X509Certificate[] getAcceptedIssuers() {
	                return new X509Certificate[0];
	            }
	        }
	    };

	    SSLContext sc = SSLContext.getInstance("TLS");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());

	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    HttpsURLConnection.setDefaultHostnameVerifier(
	        (hostname, session) -> true
	    );

	    URL url = new URL(endpoint);

	    try (InputStream in = url.openStream();
	         BufferedInputStream bufIn = new BufferedInputStream(in)) {

	        StringBuilder doc = new StringBuilder();
	        int data;

	        while ((data = bufIn.read()) != -1) {
	            doc.append((char) data);
	        }

	        return doc.toString();
	    }
	}
	
	public static String dopostrequest(String endpint, List<NameValuePair> params) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = SSLContextBuilder.create()
		        .loadTrustMaterial((chain, authType) -> true)
		        .build();

		SSLConnectionSocketFactory sslsf =
		        new SSLConnectionSocketFactory(
		                sslContext,
		                NoopHostnameVerifier.INSTANCE);

		HttpClient httpclient = HttpClients.custom()
		        .setSSLSocketFactory(sslsf)
		        .build();
		
		//HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(endpint);

		// Request parameters and other properties.
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		try (InputStream instream = entity.getContent()) {
		    	return IOUtils.toString(instream, StandardCharsets.UTF_8);
		}
	}
	
	@Test
	public void testGetSpectrumTypes() throws Exception{
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/getSpectrumTypes");
		System.out.println(res);
	    Assert.assertTrue(res.contains("\"13C\",\"1H\",\"15N\",\"31P\",\"19F\",\"11B\",\"29Si\""));
	}
	
	@Test
	public void testDoAssignment() throws Exception{
        String filename = "org/openscience/nmrshiftdb2/test/webservices/maarenolide_g.xml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        CMLBuilder builder = new CMLBuilder(false);
        Document cmlcml = builder.build(ins);
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("toassign", cmlcml.toXML()));
        String res=dopostrequest(TestConstants.TESTSERVER+"/doAssignment",params);
		System.out.println(res);
	    ins = new ByteArrayInputStream(res.getBytes());
        Document cmlcmlres = builder.build(ins);        
        Assert.assertEquals(5,cmlcmlres.getChild(0).getChildCount());
	    Assert.assertTrue(cmlcmlres.toXML().contains("<peak xValue=\"14.5\" xUnits=\"units:ppm\" peakShape=\"sharp\" id=\"p1\" atomRefs=\"a12\" />"));
	}
	
	@Test
	public void testUserNumber() throws Exception{
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/getUserNumber");
		System.out.println(res);
	    Assert.assertTrue(StringUtils.isNumeric(res));
	}
	
	@Test
	public void testPublish() throws Exception{
        String filename = "org/openscience/nmrshiftdb2/test/webservices/testpublish.xml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        CMLBuilder builder = new CMLBuilder(false);
        Document cmlcml = builder.build(ins);        
        String res=dorequestIgnoringCertificates(TestConstants.TESTSERVER+"/doPublish/username/shk3/password/test/topublish/"+cmlcml.toXML());
		System.out.println(res);
	    Assert.assertTrue(StringUtils.isNumeric(res));
        try{
        	Torque.init("../nmrshiftdb2/src/conf/jetspeed/WEB-INF/conf/Torque.properties");
        	DBSpectrumPeer.retrieveByPK(3407);
        	//if no error record exists, so the ws should give 2
        	Assert.assertEquals("2",res);
        }catch(NoRowsException ex){
        	//record does not exist, ws should give 0
        	Assert.assertEquals("0",res);
        }
	}

	@Test
    public void testSubmit() throws Exception{
        String filename = "org/openscience/nmrshiftdb2/test/webservices/maarenolide_g.xml";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        CMLBuilder builder = new CMLBuilder(false);
        Document cmlcml = builder.build(ins);        
        String request= cmlcml.toXML();
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("tosubmit", request));
        String res=dopostrequest(TestConstants.TESTSERVER+"/doSubmit/username/shk3/password/test/",params);
	    int spectrumid = Integer.parseInt(res.trim());
	    Assert.assertTrue(spectrumid>=0);
    }
}
