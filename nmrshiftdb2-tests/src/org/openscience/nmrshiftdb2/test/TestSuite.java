package org.openscience.nmrshiftdb2.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openscience.nmrshiftdb2.test.java.ExtendedHoseCode;
import org.openscience.nmrshiftdb2.test.java.Similarity;
import org.openscience.nmrshiftdb2.test.servlet.NmrshiftdbServlet;
import org.openscience.nmrshiftdb2.test.web.QuickCheck;
import org.openscience.nmrshiftdb2.test.web.Searches;
import org.openscience.nmrshiftdb2.test.webservices.NmrshiftdbWebservices;

@RunWith(Suite.class)

@Suite.SuiteClasses({
   NmrshiftdbServlet.class,
   QuickCheck.class,
   Searches.class,
   ExtendedHoseCode.class,
   Similarity.class,
   NmrshiftdbWebservices.class   
})

public class TestSuite {   
}  	