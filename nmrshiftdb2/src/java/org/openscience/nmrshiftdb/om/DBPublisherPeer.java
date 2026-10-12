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
import java.sql.Connection;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 2, 2003
 */
public class DBPublisherPeer
     extends org.openscience.nmrshiftdb.om.BaseDBPublisherPeer {
  /**
   *  Gets a publisher with these data, if existing, or makes a new one.
   *
   * @param  place          The place.
   * @param  publisher      The publisher.
   * @return                The publisher.
   * @exception  TorqueException  Database problems.
   */
  public static DBPublisher getOldOrMakeNew(String place, String publisher, Connection dbconn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBPublisherPeer.PLACE, place);
    crit.add(DBPublisherPeer.NAME, publisher);
    List<DBPublisher> v = DBPublisherPeer.doSelect(crit,dbconn);
    DBPublisher dbpub = null;
    if (v.size() > 0) {
      dbpub = (DBPublisher) v.get(0);
    } else {
      dbpub = new DBPublisher();
      dbpub.setPlace(place);
      dbpub.setName(publisher);
      dbpub.save(dbconn);
    }
    return (dbpub);
  }
}

