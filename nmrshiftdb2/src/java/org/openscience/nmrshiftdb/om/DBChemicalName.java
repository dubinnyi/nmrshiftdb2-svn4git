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
import org.apache.torque.om.Persistent;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    October 30, 2003
 */
public class DBChemicalName
     extends org.openscience.nmrshiftdb.om.BaseDBChemicalName
     implements Persistent {
  /**
   *Constructor for the DBChemicalName object
   */
  public DBChemicalName() {
    super();
  }


  /**
   *Constructor for the DBChemicalName object
   *
   * @param  dbmol          The molecule of which this is a chemical name.
   * @param  name           The name.
   * @exception  Exception  Database problems.
   */
  public DBChemicalName(int dbmol, String name) throws Exception {
    setMoleculeId(dbmol);
    setName(name);
    setNameSoundex(Soundex.soundex(name, "9", new StringAndInt()));
  }


  /**
   *  Saves a chemical name, checking that the name does not yet exist.
   *
   * @param  dbconn         The connection to use.
   * @exception  Exception  Description of Exception.
   */
  public void save(Connection dbconn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBChemicalNamePeer.MOLECULE_ID, this.getMoleculeId());
    crit.add(DBChemicalNamePeer.NAME, getName());
    List<DBChemicalName> v = DBChemicalNamePeer.doSelect(crit,dbconn);
    if (v.size() == 0) {
      super.save(dbconn);
    }
  }
  
  public DBMolecule getDBMolecule(Connection dbconn) throws TorqueException{
	  return DBMoleculePeer.retrieveByPK(SimpleKey.keyFor(this.getMoleculeId()),dbconn);
  }
}


