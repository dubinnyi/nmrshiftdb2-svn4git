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
public class DBJournalPeer
     extends org.openscience.nmrshiftdb.om.BaseDBJournalPeer {
  /**
   *  Either gets a journal with the given data, if existing, or creates a new one.
   *
   * @param  journaltitle     The title.
   * @param  journalsubtitle  The subtitle.
   * @param  ita              The ita.
   * @return                  The journal with these data.
   * @exception  TorqueException    Database problems.
   */
  public static DBJournal getOldOrMakeNew(String journaltitle, String journalsubtitle, String ita, Connection conn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBJournalPeer.TITLE, journaltitle);
    crit.add(DBJournalPeer.SUBTITLE, journalsubtitle);
    List<DBJournal> v = DBJournalPeer.doSelect(crit,conn);
    DBJournal dbjour = null;
    if (v.size() > 0) {
      dbjour = (DBJournal) v.get(0);
    } else {
      dbjour = new DBJournal();
      dbjour.setTitle(journaltitle);
      dbjour.setSubtitle(journalsubtitle);
      dbjour.setIta(ita);
      dbjour.save(conn);
    }
    return (dbjour);
  }
}

