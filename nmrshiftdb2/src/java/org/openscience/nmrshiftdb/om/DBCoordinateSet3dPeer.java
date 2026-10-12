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

import org.apache.torque.util.Criteria;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    September 23, 2004
 */
public class DBCoordinateSet3dPeer
     extends org.openscience.nmrshiftdb.om.BaseDBCoordinateSet3dPeer {
  /**
   *  Gets a coordinate set by atom id.
   *
   * @param  nk             The atom id.
   * @return                The coordinate set of this atom, null if none.
   * @exception  Exception  Description of Exception
   */
  public static DBCoordinateSet3d getByAtomId(int nk) throws Exception {
    Criteria crit = new Criteria();
    crit.add(ATOM_ID, nk);
    crit.add(COORDINATE_SET_TYPE_ID, 1);
    List<DBCoordinateSet3d> v = doSelect(crit);
    if (v.size() == 0) {
      return (null);
    } else {
      return ((DBCoordinateSet3d) v.get(0));
    }
  }
}

