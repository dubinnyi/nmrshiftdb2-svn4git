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

import java.sql.Connection;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

// JDK classes


/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 2, 2003
 */
public class DBJournalVolumePeer
     extends org.openscience.nmrshiftdb.om.BaseDBJournalVolumePeer {
  /** Gets a DBJournalVolume with these data, if existing, or creates a new one.
   *
   * @param  journal        The journal.
   * @param  volume         The volume.
   * @param  year           The year.
   * @return                The DBJournalVolum with these data.
   * @exception  TorqueException  Database problems.
   */
  public static DBJournalVolume getOldOrMakeNew(DBJournal journal, int volume, int year, Connection conn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBJournalVolumePeer.JOURNAL_ID, journal.getJournalId());
    crit.add(DBJournalVolumePeer.VOLUME, volume);
    crit.add(DBJournalVolumePeer.YEAR, year);
    List<DBJournalVolume> v = DBJournalVolumePeer.doSelect(crit,conn);
    DBJournalVolume dbjour = null;
    if (v.size() > 0) {
      dbjour = (DBJournalVolume) v.get(0);
    } else {
      dbjour = new DBJournalVolume();
      dbjour.setDBJournal(journal);
      dbjour.setVolume(volume);
      dbjour.setYear(year);
      dbjour.save(conn);
    }
    return (dbjour);
  }
}

