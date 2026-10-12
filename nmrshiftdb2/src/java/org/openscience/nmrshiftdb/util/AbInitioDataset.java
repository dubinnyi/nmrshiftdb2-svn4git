/*
 *  AbInitioDataset.java
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

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *  This class holds the information parsed from a Gaussian 98 NMR chemical
 *  shift calculation. This is a special version for the use in nmrshiftdb2
 *  (http://www.nmrshiftdb.org)
 *
 *@author     steinbeck
 *@created    13. Mai 2002
 */
public class AbInitioDataset
{

	/**
	 *  A chemical Name for this molecule
	 */
	public String chemicalName;
	/**
	 *  The Chemical Abstracts Services (CAS) Number of this molecule
	 */
	public String casNumber;
	/**
	 *  A molecule as it was read from the gaussian file. This molecule come with
	 *  computed 3D coordinates.
	 *  The NMR shifs are assigned to each atom and can by retrieved by
	 *  atom.getPhysicalProperty(Atom.NMRSHIFT_CARBON) or 
	 *  atom.getPhysicalProperty(Atom.NMRSHIFT_HYDROGEN).
	 *  See org.openscience.cdk.CDKConstants
	 *  for more constants.
	 */
	public IAtomContainer molecule;

	public String software = "Gaussian 98";
	public String nmrAlgorithm = "GIAO";
	public String nmrStandard = "Tetramethylsilan (TMS)";
	
	/**
	 *  Four fields that describe the way in which the geometry optimization of the
	 *  molecule and in which the subsequent nmr computation was were performed.
	 *  This should go into the experiment conditions.
	 */
	public String optMethod = "", optBasisSet = "", nmrMethod = "", nmrBasisSet = "";

	/**
	 *  A log file containing information about the parsing process For debugging
	 *  use only. Discard, if parse was successful.
	 */
	public StringBuffer parselog = new StringBuffer();

	public String toString()
	{
		IAtom atom = null;
		StringBuffer s = new StringBuffer();
		s.append("Molecule " + chemicalName + " with CAS Number " + casNumber + "\n");
		s.append("was optimized by " + software + ".\n");
		s.append("Geometry optimization with " + optMethod + "/" + optBasisSet + ".\n");
		s.append("NMR calculation with " + nmrAlgorithm + ", " + nmrMethod + "/" + nmrBasisSet + ".\n");
		s.append("Atom listing with chemical shifts: \n");
		for (int f = 0; f < molecule.getAtomCount();f++)
		{
			atom = molecule.getAtom(f);
			if (atom.getSymbol().equals("C"))
			{
				s.append(f + ". " + atom.getSymbol() + ": " + molecule.getAtom(f).getProperty(CDKConstants.NMRSHIFT_CARBON) + "\n");
			}
			else if (atom.getSymbol().equals("H"))
			{
				s.append(f + ". " + atom.getSymbol() + ": " + molecule.getAtom(f).getProperty(CDKConstants.NMRSHIFT_HYDROGEN) + "\n");
			} 
		}
		
		return s.toString();
	}
}

