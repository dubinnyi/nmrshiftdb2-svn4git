/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2012 Stefan Kuhn
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
 * @created    December 3, 2003
 */
public class DBSpectrumTypePeer
     extends org.openscience.nmrshiftdb.om.BaseDBSpectrumTypePeer {

  /**
   *  Gets a DBSpectrumType by name attribute.
   *
   * @param  name  The name.
   * @return       The associated DBSpectrumType value
   */
  public static DBSpectrumType getByName(String name) throws NmrshiftdbException, TorqueException{
    Criteria crit = new Criteria();
    crit.add(NAME, name);
    List<DBSpectrumType> v = doSelect(crit);
    if (v.size() != 1) {
      throw new NmrshiftdbException("Failed to select one and only one spectrum type with " + name);
    }
    return ((DBSpectrumType) v.get(0));
  }
}

