/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013 Stefan Kuhn
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
 * @created    January 15, 2004
 */
public class DBMoleculeHyperlink
     extends org.openscience.nmrshiftdb.om.BaseDBMoleculeHyperlink
     implements Persistent {

  /**
   *Constructor for the DBMoleculeHyperlink object
   */
  public DBMoleculeHyperlink() {
    super();
  }


  /**
   *Constructor for the DBMoleculeHyperlinkPeer object
   *
   * @param  dbmol          The molecule this should be a link for.
   * @param  link           The link.
   * @param  des            The description ("" if none".
   * @exception  Exception  Description of Exception
   */
  public DBMoleculeHyperlink(int dbmol, String link, String des) throws TorqueException {
    super();
    setMoleculeId(dbmol);
    if (link.length() < 8 || (!link.substring(0, 7).equals("http://") && !link.substring(0, 8).equals("https://"))) {
      link = "http://" + link;
    }
    setHyperlink(link);
    if (!des.equals("")) {
      setDescription(des);
      setDescriptionSoundex(Soundex.soundex(des, "9", new StringAndInt()));
    } else {
      setDescription(link);
      setDescriptionSoundex("");
    }
  }


  /**
   *The equals method comparing by the Hyperlink fiels
   *
   * @param  hl  The DBMoleculeHyperlink to compare with
   * @return     false=unequal, true=equal
   */
  public boolean equals(DBMoleculeHyperlink hl) {
    if (this.getHyperlink().equals(hl.getHyperlink())) {
      return true;
    } else {
      return false;
    }
  }


  /**
   *  Saves this to db. Also checks if entry is already existing.
   *
   * @param  dbconn         Description of Parameter
   * @exception  Exception  Description of Exception
   */
  public void save(Connection dbconn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBMoleculeHyperlinkPeer.MOLECULE_ID, getMoleculeId());
    crit.add(DBMoleculeHyperlinkPeer.HYPERLINK, getHyperlinkSimple());
    List<DBMoleculeHyperlink> v = DBMoleculeHyperlinkPeer.doSelect(crit);
    if (v.size() == 0) {
      super.save(dbconn);
    }
  }
  
  /**
   * Get the Hyperlink with derefer
   *
   * @return String
   */
  public String getHyperlink()
  {
      return "derefer.jsp?url="+super.getHyperlink();
  }  
  
  /**
   * Get the Hyperlink without derefer
   *
   * @return String
   */
  public String getHyperlinkSimple()
  {
      return super.getHyperlink();
  }  
}


