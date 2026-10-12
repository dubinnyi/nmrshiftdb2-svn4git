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
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;

import com.workingdogs.village.Record;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 2, 2003
 */
@SuppressWarnings("serial")
public class DBAuthorPeer
     extends org.openscience.nmrshiftdb.om.BaseDBAuthorPeer {
  /**
   *  Either gets an old author with the given data, if existing, or creates a new one if not.
   *
   * @param  name           The name of the author.
   * @param  surname        The surname of the author.
   * @return                The author with this data
   * @exception  TorqueException  Database problems
   */
  public static DBAuthor getOldOrMakeNew(String name, String surname, Connection dbconn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBAuthorPeer.NAME, name);
    crit.add(DBAuthorPeer.SURNAME, surname);
    List<DBAuthor> v = DBAuthorPeer.doSelect(crit,dbconn);
    DBAuthor author = null;
    if (v.size() > 0) {
      author = (DBAuthor) v.get(0);
    } else {
      author = new DBAuthor();
      author.setSurname(surname);
      author.setName(name);
      author.setNameTotalSoundex(Soundex.soundex(surname + name, "9", new StringAndInt()));
      author.setNameTotal(surname + name);
      author.save(dbconn);
    }
    return (author);
  }


  /**
   *  Gets a list of existing authors.
   *
   * @return                The existing authors.
   * @exception  Exception  Database problems.
   */
  public static List<String> getExistingBookAuthors() throws Exception {
    String sql = "select SURNAME, NAME, BOOK.LITERATURE_ID from AUTHOR, IS_AUTHOR, BOOK left join ARTICLE on BOOK.LITERATURE_ID=ARTICLE.LITERATURE_ID where ARTICLE.LITERATURE_ID is null and AUTHOR.AUTHOR_ID=IS_AUTHOR.AUTHOR_ID and IS_AUTHOR.LITERATURE_ID=BOOK.LITERATURE_ID order by BOOK.LITERATURE_ID;";
    List<Record> l = executeQuery(sql);
    int lastId = 0;
    if (l.size() > 0) {
      lastId = ((Record) l.get(0)).getValue(3).asInt();
    }
    StringBuffer currentAuthor = new StringBuffer();
    List<String> returnList = new ArrayList<String>();
    returnList.add("");
    for (int i = 0; i < l.size(); i++) {
      if (((Record) l.get(i)).getValue(3).asInt() != lastId) {
        if (!returnList.contains(currentAuthor.toString())) {
          returnList.add(currentAuthor.toString());
        }
        lastId=((Record) l.get(i)).getValue(3).asInt();
        currentAuthor = new StringBuffer();
      }
      if (!currentAuthor.toString().equals("")) {
        currentAuthor.append("; ");
      }
      currentAuthor.append(((Record) l.get(i)).getValue(1).asString() + " " + ((Record) l.get(i)).getValue(2).asString());
    }
    if (!currentAuthor.toString().equals("")) {
      returnList.add(currentAuthor.toString());
    }
    return (returnList);
  }
}

