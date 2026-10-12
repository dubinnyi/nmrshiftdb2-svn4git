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

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    September 23, 2004
 */
public class DBSpectrumDBKeyword
     extends org.openscience.nmrshiftdb.om.BaseDBSpectrumDBKeyword
     implements Persistent {
  DBKeyword keyword = null;


  /**
   *  Calls getDBKeyword of base class with caching.
   *
   * @return                The dBKeyword value
   * @exception  Exception  Description of Exception
   */
  public DBKeyword getDBKeyword() throws TorqueException {
    if (keyword == null) {
      keyword = super.getDBKeyword();
    }
    return (keyword);
  }
}


