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
import java.sql.Connection;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    January 21, 2004
 */
public class DBMoleculeDBKeyword
     extends org.openscience.nmrshiftdb.om.BaseDBMoleculeDBKeyword
     implements Persistent {
  DBKeyword dbkeyword = null;
  private String keyword;
  private int dbmol;


  /**
   *Constructor for the DBMoleculeDBKeyword object
   */
  public DBMoleculeDBKeyword() {
    super();
  }


  /**
   *Constructor for the DBMoleculeDBKeyword object
   *
   * @param  keyword  The new keyword.
   * @param  dbmol    The molecuel this is supposed to be a keyword of.
   */
  public DBMoleculeDBKeyword(String keyword, int dbmol) {
    this.keyword = keyword;
    this.dbmol = dbmol;
  }


  /**
   *  Calls getDBKeyword of Base class with caching.
   *
   * @return                The dBKeyword value
   * @exception  Exception  Description of Exception
   */
  public DBKeyword getDBKeyword() throws TorqueException {
    if (dbkeyword == null) {
      dbkeyword = super.getDBKeyword();
    }
    return (dbkeyword);
  }


  /**
   *  Saves this keyword, using values set via the constructor, if any.
   *
   * @param  dbconn         Connection to use.
   * @exception  Exception  Database problems.
   */
  public void save(Connection dbconn) throws TorqueException {
    if (dbmol == 0) {
      super.save(dbconn);
      return;
    }
    Criteria crit = new Criteria();
    crit.add(DBKeywordPeer.KEYWORD, keyword);
    List<DBKeyword> v = DBKeywordPeer.doSelect(crit);
    if (v.size() != 0) {
      crit = new Criteria();
      crit.add(DBMoleculeDBKeywordPeer.KEYWORD_ID, ((DBKeyword) v.get(0)).getKeywordId());
      crit.add(DBMoleculeDBKeywordPeer.MOLECULE_ID, dbmol);
      List<DBKeyword> v2 = DBKeywordPeer.doSelect(crit);
      if (v2.size() == 0) {
        setMoleculeId(dbmol);
        setDBKeyword((DBKeyword) v.get(0));
        super.save(dbconn);
      }
    } else {
      Criteria crit2 = new Criteria();
      crit2.add(DBKeywordPeer.KEYWORD, keyword);
      crit2.add(DBKeywordPeer.KEYWORD_SOUNDEX, Soundex.soundex(keyword, "9", new StringAndInt()));
      int ok = ((NumberKey) DBKeywordPeer.doInsert(crit2)).intValue();
      setMoleculeId(dbmol);
      setKeywordId(ok);
      super.save(dbconn);
    }
  }
}


