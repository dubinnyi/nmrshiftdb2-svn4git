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

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

// JDK classes

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 22, 2005
 */
public class DBBookmarkPeer
     extends org.openscience.nmrshiftdb.om.BaseDBBookmarkPeer {
  /**
   *  deletes a certain bookmark
   *
   * @param  userid         to delete.
   * @param  moleculeid     to delete.
   * @exception  Exception  Description of Exception.
   */
  public static void delete(int userid, int moleculeid) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBookmarkPeer.USER_ID, userid);
    crit.add(DBBookmarkPeer.MOLECULE_ID, moleculeid);
    DBBookmarkPeer.doDelete(crit);
  }
}

