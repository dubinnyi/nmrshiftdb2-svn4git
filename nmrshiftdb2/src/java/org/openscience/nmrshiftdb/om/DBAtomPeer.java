/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb.om;

// JDK classes
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    February 26, 2003
 */
@SuppressWarnings("serial")
public class DBAtomPeer
     extends org.openscience.nmrshiftdb.om.BaseDBAtomPeer {


  /**
   *  Gets an atom by MoleculeId And MdlNumber
   *
   * @param  moleculeId               The molecule id
   * @param  mdlNumber                The mdl number
   * @return                          The molecule
   * @exception  Exception            Database problems
   * @exception  NmrshiftdbException  Description of Exception
   */
  public static DBAtom getByMoleculeIdAndMdlNumber(int moleculeId, int mdlNumber) throws TorqueException {
    List<DBAtom> v=DBAtomPeer.getAtoms(moleculeId);
    return((DBAtom)v.get(mdlNumber));
  }

/**
   *  Gets the atoms of a certain molecule.
   *
   * @param  molid          The molecule.
   * @return                The atoms.
   * @exception  Exception  Database problems.
   */
  public static List<DBAtom> getAtoms(int molid) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID, molid);
    crit.addAscendingOrderByColumn(DBAtomPeer.ATOM_NUMBER);
    crit.addAscendingOrderByColumn(DBAtomPeer.ATOM_ID);
    return (DBAtomPeer.doSelect(crit));
  }

/**
   *  Gets the atoms of a certain molecule withouth the Hs.
   *
   * @param  molid          The molecule.
   * @return                The atoms.
   * @exception  Exception  Database problems.
   */
  public static List<DBAtom> getAtomsWithoutHs(int molid) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID, molid);
    crit.addAscendingOrderByColumn(DBAtomPeer.ATOM_ID);
    crit.add(DBAtomPeer.SYMBOL,(Object)"H",Criteria.NOT_EQUAL);
    return (DBAtomPeer.doSelect(crit));
  }
}

