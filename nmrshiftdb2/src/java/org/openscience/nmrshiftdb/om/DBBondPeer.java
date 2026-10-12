/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010 Stefan Kuhn
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


/** 
  *  You should add additional methods to this class to meet the
  *  application requirements.  This class will only be generated as
  *  long as it does not already exist in the output directory.
  */
@SuppressWarnings("serial")
public class DBBondPeer 
    extends org.openscience.nmrshiftdb.om.BaseDBBondPeer
{
  /**
   *  Gets the bonds of a certain molecule orderd by bondid.
   *
   * @param  molid          The id of the molecule.
   * @return                The bonds.
   * @exception  TorqueException  Database problems.
   */
  public static List<DBBond> getBonds(int molid) throws TorqueException {
        Criteria crit=new Criteria();
    crit.add(DBAtomPeer.MOLECULE_ID,molid);
    crit.addJoin(DBAtomPeer.ATOM_ID,DBBondDBAtomPeer.ATOM_ID);
    crit.addJoin(DBBondPeer.BOND_ID,DBBondDBAtomPeer.BOND_ID);
    crit.addAscendingOrderByColumn(DBBondPeer.BOND_ID);
    crit.setDistinct();
    return (DBBondPeer.doSelect(crit));
  }
}

