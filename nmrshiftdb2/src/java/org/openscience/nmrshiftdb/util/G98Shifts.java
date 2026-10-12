/*
 *  g98shifts.java
 *
 *  Copyright (C) 1997, 1998  Dr. Christoph Steinbeck
 *
 *  Contact: steinbeck@ice.mpg.de
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  All I ask is that proper credit is given for my work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.openscience.nmrshiftdb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.tools.SaturationChecker;


/**
 *  A class to extract a chemical structure and an carbon nmr spectrum from a
 *  multi-part Gaussian 98 output file
 *
 *@author     steinbeck
 *@created    25. April 2002
 */

public class G98Shifts
{
	/**
	 *  Description of the Method
	 *
	 *@param  file           Description of Parameter
	 *@param  args           Description of Parameter
	 *@return                Description of the Returned Value
	 *@exception  Exception  Description of Exception
	 */
	public synchronized AbInitioDataset readG9x(File file, String[] args) throws Exception
	{
		Hashtable ht = extractReferences(args);
		Vector bonds = new Vector();
		StringTokenizer strTok;
		String line;
		String atomSymbol;
		AbInitioDataset aid = new AbInitioDataset();
		//ElementFactory elFac = new ElementFactory();
		IElement element = null;
		IsotopeFactory isoFac = Isotopes.getInstance();
		IIsotope helpIsotope = null;
		IAtom atom = null;
		int atomicNumber;
		int atomicType;
		int partCounter;
		int partCount;
		int soCounter;
		int soCount;
		int tempInt;
		int p1;
		int p2;
		int index = -1;
		float xCoord;
		float yCoord;
		float zCoord;
		double shielding;
		double chemShift;
		boolean NMR = false;
		boolean OPT = false;
		boolean calcChemShift = false;
		double reference = 0;
		String temp;
		String optMethod = "";
		String optBasisSet = "";
		String nmrMethod = "";
		String nmrBasisSet = "";
		Vector chemicalShifts = new Vector();
		aid.parselog.append("<pre>");
		aid.parselog.append(file.getAbsolutePath() + "\n");
		BufferedReader buffRead = new BufferedReader(new FileReader(file));

		aid.parselog.append("Parsing Gaussian logfile for structure definition..." + "\n");
		/*
		 *  Find last occurence of the string "Standard orientation".
		 *  It is assumed that, in a multi-part job, the last occurence of
		 *  the structure is the most refined one
		 */
		partCounter = 0;
		soCount = 0;
		extractChemName(buffRead, aid);
		buffRead = new BufferedReader(new FileReader(file));
		//if (true) return aid;
		do
		{
			try
			{
				line = buffRead.readLine().trim();
				
				//aid.parselog.append(line + "\n");
				if (line.indexOf("Standard orientation:") >= 0)
				{
					partCounter++;
				}
				if (line.indexOf("Stationary point found.") > 0)
				{
					aid.parselog.append("-- Stationary point found.\n");
					bonds = new Vector();
					do
					{
						line = buffRead.readLine().trim();
						if (line.indexOf("R(") >= 0)
						{
							strTok = new StringTokenizer(line, "(,)");
							strTok.nextToken();
							//aid.parselog.append("*****************\n");
							bonds.add(new java.awt.Point(new Integer(strTok.nextToken()).intValue() - 1, new Integer(strTok.nextToken()).intValue() -1));
							aid.parselog.append(((java.awt.Point)bonds.lastElement()).x + " - " + ((java.awt.Point)bonds.lastElement()).y + "\n");

						}
					} while (line.indexOf("A(") < 1);
				}

				if (line.startsWith("#"))
				{
					soCount++;
					buffRead.mark(512 * 1024);
					if (line.toUpperCase().indexOf("OPT") > 0)
					{
						OPT = true;
						strTok = new StringTokenizer(line, " /");
						strTok.nextToken();
						optMethod = strTok.nextToken().toUpperCase();
						optMethod = cleanString(optMethod, "(),");
						optBasisSet = strTok.nextToken().toUpperCase();
						optBasisSet = cleanString(optBasisSet, "(),");
					}
					if (line.toUpperCase().indexOf("NMR") > 0)
					{
						NMR = true;
						// Analyse the level of theory and basis set used
						strTok = new StringTokenizer(line, " /");
						strTok.nextToken();
						nmrMethod = strTok.nextToken().toUpperCase();
						nmrMethod = cleanString(nmrMethod, "(),");
						nmrBasisSet = strTok.nextToken().toUpperCase();
						nmrBasisSet = cleanString(nmrBasisSet, "(),");
						break;
					}
					else
					{
						NMR = false;
					}
				}
			}
			catch (Exception exc)
			{
				aid.parselog.append(exc + "\n");
				break;
			}
		} while (true);
		if (partCounter > 1)
		{
			aid.parselog.append("Multipart output file consisting of at least " + soCount + " parts detected." + "\n");
		}
		if (OPT)
		{
			aid.parselog.append("Geometry optimization detected" + "\n");
            		aid.parselog.append("Method: " + optMethod + " - Basis set: " + optBasisSet + "\n");
		}
		if (NMR)
		{
			aid.parselog.append("NMR Shielding tensor calculation detected" + "\n");
			aid.parselog.append("Method: " + nmrMethod + " - Basis set: " + nmrBasisSet + "\n");
		}
		else
		{
			aid.parselog.append("No NMR Shielding tensor calculation detected" + "\n");
		}

		buffRead.reset();
		partCount = partCounter;
		try
		{
			do
			{
				line = buffRead.readLine().trim();
				if (line.indexOf("Standard orientation:") >= 0)
				{
					break;
				}
			} while (true);
			aid.parselog.append("Structure found. Reading..." + "\n");
			buffRead.readLine();
			buffRead.readLine();
			buffRead.readLine();
			buffRead.readLine();
			line = buffRead.readLine().trim();
			while (!line.startsWith("-------------------"))
			{
				strTok = new StringTokenizer(line);
				strTok.nextToken();
				if (aid.molecule == null) aid.molecule = new org.openscience.cdk.AtomContainer();
				// AtomCounter
				atomicNumber = new Integer(strTok.nextToken()).intValue();
				aid.parselog.append("Atom with atomic number: " + atomicNumber + "\n");
				helpIsotope = isoFac.getMajorIsotope(atomicNumber);
				atom = new org.openscience.cdk.Atom(helpIsotope.getSymbol());
				isoFac.configure(atom, helpIsotope);
				
				aid.parselog.append("Isotope: " + helpIsotope + "\n");
				while (strTok.countTokens() > 3)
				{
					strTok.nextToken();
					// This makes the difference between G94 and G98
				}
				xCoord = new Float(strTok.nextToken()).floatValue();
				yCoord = new Float(strTok.nextToken()).floatValue();
				zCoord = new Float(strTok.nextToken()).floatValue();
				atom.setPoint2d(new Point2d((double)xCoord,(double)yCoord));
				atom.setPoint3d(new Point3d((double)xCoord,(double)yCoord,(double)zCoord));
				aid.molecule.addAtom(atom);
				aid.parselog.append("Coordinates [x, y, z]: " + xCoord + ", " +yCoord + ", " + zCoord + "\n");
				line = buffRead.readLine().trim();
				
			}
			soCounter = 0;
			if (NMR)
			{
				aid.parselog.append("NMR shieldings: \n");
				do
				{
					line = buffRead.readLine().trim();
					
					if (line.indexOf("Isotropic") >= 0)
					{
						aid.parselog.append("NMR shielding line found \n");
						strTok = new StringTokenizer(line);
						tempInt = new Integer(strTok.nextToken()).intValue();
						atomSymbol = strTok.nextToken().trim();
						do
						{
							temp = strTok.nextToken();
						} while (!temp.equals("="));
						shielding = new Float(strTok.nextToken()).floatValue();
						aid.parselog.append("Atom number " + soCounter + "\n");
						aid.parselog.append("'Isotropic Shielding: " + shielding + "\n");
						if (ht.containsKey(atomSymbol))
						{
							reference = ((Double)ht.get(atomSymbol)).doubleValue();	
						}
						chemShift = reference - shielding;
						if (atomSymbol.equals("H"))
						{
							aid.molecule.getAtom(soCounter).setProperty(CDKConstants.NMRSHIFT_HYDROGEN, new Double(chemShift));	
						}
						if (atomSymbol.equals("C"))
						{
							aid.molecule.getAtom(soCounter).setProperty(CDKConstants.NMRSHIFT_CARBON, new Double(chemShift));	
						}

						aid.parselog.append("Reference: " + reference + "\n");
						aid.parselog.append("Resulting Chemical Shift: " + shielding + "\n\n");
						soCounter++;
					}
				} while (soCounter < aid.molecule.getAtomCount());
			}
		}
		catch (Exception exc)
		{
			aid.parselog.append("Error while trying to read Gaussian output" + "\n");
			exc.printStackTrace();
		}
		try
		{
			aid.parselog.append("Parsing of structure definition done." + "\n");
			for (int f = 0; f < bonds.size(); f++)
			{
				p1 = ((java.awt.Point) bonds.elementAt(f)).x;
				p2 = ((java.awt.Point) bonds.elementAt(f)).y;
				aid.molecule.addBond(p1, p2, IBond.Order.SINGLE);
			}
			String filename = "test" + System.currentTimeMillis() + ".mol";
		}
		catch (Exception exc)
		{
			aid.parselog.append("Error while trying to produce structure from Gaussian output" + "\n");
			aid.parselog.append(exc.toString() + "\n");
		}
		/*Atom[] atoms = null;
		int hydrogenCounter;
		for (int f = 0; f < aid.molecule.getAtomCount(); f++)
		{
			hydrogenCounter = 0;
			atom = aid.molecule.getAtomAt(f);
			atoms = aid.molecule.getConnectedAtoms(atom);
			//System.out.println("Vorher: " + atom.getHydrogenCount());
			for (int g = 0; g < atoms.length; g++)
			{
				//System.out.println(atoms[g].getSymbol());
				if (atoms[g].getSymbol().equals("H"))
				{
					hydrogenCounter ++;	
				}
			}
			atom.setHydrogenCount(hydrogenCounter);
			//System.out.println("Nachher: " + atom.getHydrogenCount());
		}*/
		SaturationChecker sc = new SaturationChecker();
		sc.saturateRingSystems(aid.molecule);
		sc.saturate(aid.molecule);
		display(aid.molecule);
		buffRead.close();
		aid.optMethod = optMethod;
		aid.optBasisSet = optBasisSet;
		aid.nmrMethod = nmrMethod; 
		aid.nmrBasisSet = nmrBasisSet;
		return aid;
	}



	/**
	 *  Removes commas and brackets from a String
	 *
	 *@param  thisString        Description of Parameter
	 *@param  charsToBeRemoved  Description of Parameter
	 *@return                   Description of the Returned Value
	 */
	static String cleanString(String thisString, String charsToBeRemoved)
	{
		String cleaned = "";
		StringTokenizer sT = new StringTokenizer(thisString, charsToBeRemoved);
		do
		{
			cleaned += sT.nextToken();
		} while (sT.hasMoreTokens());
		return cleaned;
	}

	
	private static void display(IAtomContainer molecule)
	{	
		/*StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		MoleculeViewer2D mv = new MoleculeViewer2D();
		Renderer2DModel r2dm = mv.getRenderer2DModel();
		r2dm.setDrawNumbers(true);
		
		try
		{
			sdg.setMolecule((Molecule)molecule.clone());
			sdg.generateCoordinates(new Vector2d(0,1));
			mv.setAtomContainer(sdg.getMolecule());
			mv.display();
		}
		catch(Exception exc)
		{
			System.out.println("*** Exit due to an unexpected error during coordinate generation ***");
			exc.printStackTrace();
		}*/
	}

	private static Hashtable extractReferences(String[] args)
	{
		Hashtable ht = new Hashtable();
		Double d = null;
		try
		{
			for (int f = 0; f < args.length; f++)
			{
				if (args[f].equals("--protonref"))
				{
					f++;
					ht.put("H", new Double(Double.parseDouble(args[f])));
				}
				if (args[f].equals("--carbonref"))
				{
					f++;
					ht.put("C", new Double(Double.parseDouble(args[f])));
				}
				if (args[f].equals("--nitrogenref"))
				{
					f++;
					ht.put("N", new Double(Double.parseDouble(args[f])));
				}

			}
		}
		catch(Exception exc)
		{
			System.out.println("G98Shifts usage example: \n");
			System.out.println("G98Shifts g98.log --protonref 3.12 -- carbonref 234.3 \n");
		}
		return ht;
	}
	
	/* This method is a horrible construct*/
	private void extractChemName(BufferedReader buffRead, AbInitioDataset aid) throws java.io.IOException
	{
		String line = null;
		StringBuffer strBuff = new StringBuffer();
		boolean done = false;
		do{
			line = buffRead.readLine();
			if(line==null)
				break;
			else
				line=line.trim(); 
			if (line.indexOf("<chemname>") >= 0)
			{
				do 
				{
					strBuff.append(line);
					line = buffRead.readLine();
					if(line==null)
						break;
					else
						line=line.trim();
				}
				while (!(line.indexOf("</casno>") >= 0));
				strBuff.append(line);
				done = true;
			}
		}
		while(!done);
		int closeCounter = 0, openCounter = 0;
		StringBuffer chemName = new StringBuffer();
		StringBuffer casno = new StringBuffer();
		char c;
		for(int f = 0; f < strBuff.length(); f++)
		{
			c = strBuff.charAt(f);
			if (c == '>')
			{
				closeCounter ++;
			}
			if (c == '<')
			{
				openCounter ++;
			}
			if (openCounter == 1 && closeCounter == 1  && c != '<' && c != '>')
			{
				chemName.append(c);
			}
			if (openCounter == 3 && closeCounter == 3  && c != '<' && c != '>')
			{
				casno.append(c);
			}
		}
		aid.chemicalName = chemName.toString().trim();
		aid.casNumber = casno.toString().trim();
		if(aid.casNumber.equals(aid.chemicalName))
			aid.casNumber="";
		if(aid.chemicalName.substring(aid.chemicalName.length()-4).equals(".mol"))
			aid.chemicalName=aid.chemicalName.substring(0,aid.chemicalName.length()-4);
	}
	
	/**
	 *  Main method. Takes a Filename
	 * as its only parameter
	 *
	 *@param  args  A filename
	 */
	public static void main(String args[])
	{
		try
		{
			File file = new File(args[0]);
			AbInitioDataset aid = (new G98Shifts()).readG9x(file, args);
		
			//System.out.println(aid.toString());
		}
		catch(Exception exc)
		{
			System.err.println("Could not open file "  + args[0] +". \n");
			System.err.println("My subsystem said: \n");
			exc.printStackTrace();
		}
		
	}
}

