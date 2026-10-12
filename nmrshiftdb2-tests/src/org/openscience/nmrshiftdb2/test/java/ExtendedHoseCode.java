package org.openscience.nmrshiftdb2.test.java;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.nmrshiftdb.util.ExtendedHOSECodeGenerator;


public class ExtendedHoseCode{
    @Test
    public void testOrganometalle1() throws Exception {
        String filename = "org/openscience/nmrshiftdb2/test/java/organometall1v3000.mol";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        MDLV3000Reader reader = new MDLV3000Reader(ins, Mode.STRICT);
        IAtomContainer mol1 = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
        reader.close();
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol1);
        Aromaticity.cdkLegacy().apply(mol1); 
        Assert.assertEquals("C-4;Co(@OSPNC/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(4), 6));
        Assert.assertEquals("C-4;Co(@CPSON/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(1), 6));
    }
    
    
    @Test
    public void testOrganometalle2() throws Exception {
        String filename = "org/openscience/nmrshiftdb2/test/java/organometall2v3000.mol";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        MDLV3000Reader reader = new MDLV3000Reader(ins, Mode.STRICT);
        IAtomContainer mol1 = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
        reader.close();
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol1);
        Aromaticity.cdkLegacy().apply(mol1);
        Assert.assertEquals("C-4;Co(@OSPNC/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(5), 6));
        Assert.assertEquals("C-4;Co(@CPSON/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(2), 6));
    }
    
    @Test
    public void testOrganometalle3() throws Exception {
        String filename = "org/openscience/nmrshiftdb2/test/java/organometall3v3000.mol";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
        MDLV3000Reader reader = new MDLV3000Reader(ins, Mode.STRICT);
        IAtomContainer mol1 = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
        reader.close();
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol1);
        Aromaticity.cdkLegacy().apply(mol1);
        Assert.assertEquals("C-4;Co(@OPSNC/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(5), 6));
        Assert.assertEquals("C-4;Co(@CSPON/,,,,/)//",new ExtendedHOSECodeGenerator().getHOSECode(mol1, mol1.getAtom(2), 6));
    }   
    
	@Test
	public void test2504() throws CDKException, IOException{
        InputStream in = ClassLoader.getSystemResourceAsStream("org/openscience/nmrshiftdb2/test/java//2504.mol");
        MDLV2000Reader reader = new MDLV2000Reader(in);
        IAtomContainer molecule    = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        molecule = reader.read(molecule);
        reader.close();
        ExtendedHOSECodeGenerator hcg=new ExtendedHOSECodeGenerator();
        String hc1=hcg.getHOSECode(molecule, molecule.getAtom(8), 6);
        String hc2=hcg.getHOSECode(molecule, molecule.getAtom(9), 6);
        Assert.assertFalse(hc1.equals(hc2));
	}

	@Test
	public void test2227() throws CDKException, IOException{
        InputStream in = ClassLoader.getSystemResourceAsStream("org/openscience/nmrshiftdb2/test/java//2227.mol");
        MDLV2000Reader reader = new MDLV2000Reader(in);
        IAtomContainer molecule    = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        molecule = reader.read(molecule);
        reader.close();
        ExtendedHOSECodeGenerator hcg=new ExtendedHOSECodeGenerator();
        String hc1=hcg.getHOSECode(molecule, molecule.getAtom(15), 6);
        String hc2=hcg.getHOSECode(molecule, molecule.getAtom(16), 6);
        Assert.assertFalse(hc1.equals(hc2));
	}

	@Test
	public void test2384() throws CDKException, IOException{
        InputStream in = ClassLoader.getSystemResourceAsStream("org/openscience/nmrshiftdb2/test/java//2384.mol");
        MDLV2000Reader reader = new MDLV2000Reader(in);
        IAtomContainer molecule    = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        molecule = reader.read(molecule);
        reader.close();
        ExtendedHOSECodeGenerator hcg=new ExtendedHOSECodeGenerator();
        String hc1=hcg.getHOSECode(molecule, molecule.getAtom(21), 6);
        String hc2=hcg.getHOSECode(molecule, molecule.getAtom(23), 6);
        Assert.assertFalse(hc1.equals(hc2));
	}
	@Test
	public void testPaperLeft() throws CDKException, IOException, ClassNotFoundException{
        InputStream in = ClassLoader.getSystemResourceAsStream("org/openscience/nmrshiftdb2/test/java/paperleft.mol");
        MDLV2000Reader reader = new MDLV2000Reader(in);
        IAtomContainer molecule    = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        molecule = reader.read(molecule);
        reader.close();
        ExtendedHOSECodeGenerator hcg=new ExtendedHOSECodeGenerator();
        String hc1=hcg.getHOSECode(molecule, molecule.getAtom(1), 6);
        Assert.assertEquals("C-4;CO(@CCN,/C,,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(2), 6);
        Assert.assertEquals("C-4;@CCNC(C,O,,/,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(3), 6);
        Assert.assertEquals("C-4;CC(@CNC,/O,,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(5), 6);
        Assert.assertEquals("C-4;C(@CNC/C,,O/,)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(6), 6);
        Assert.assertEquals("N-3;C(@CCC/C,O,/,)//",hc1);
	}
	@Test
	public void testPaperRight() throws CDKException, IOException, ClassNotFoundException{
        InputStream in = ClassLoader.getSystemResourceAsStream("org/openscience/nmrshiftdb2/test/java/paperright.mol");
        MDLV2000Reader reader = new MDLV2000Reader(in);
        IAtomContainer molecule    = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        molecule = reader.read(molecule);
        reader.close();
        ExtendedHOSECodeGenerator hcg=new ExtendedHOSECodeGenerator();
        String hc1=hcg.getHOSECode(molecule, molecule.getAtom(1), 6);
        Assert.assertEquals("C-4;CO(@CNC,/C,,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(2), 6);
        Assert.assertEquals("C-4;@CCCN(C,O,,/,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(3), 6);
        Assert.assertEquals("C-4;CC(@CCN,/O,,/)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(5), 6);
        Assert.assertEquals("C-4;C(@CCN/C,O,/,)//",hc1);
        hc1=hcg.getHOSECode(molecule, molecule.getAtom(6), 6);
        Assert.assertEquals("N-3;C(@CCC/C,,O/,)//",hc1);
	}
}
