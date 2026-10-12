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

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;

// JDK classes


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    February 3, 2004
 */
@SuppressWarnings("serial")
public class DBIdentifier
     extends org.openscience.nmrshiftdb.om.BaseDBIdentifier
     implements Persistent {
  /**
   *Constructor for the DBIdentifier object
   */
  public DBIdentifier() {
    super();
  }


  /**
   *Constructor for the DBIdentifier object
   *
   * @param  atom           The atom.
   * @param  spectrum     The literature.
   * @param  identifier     The identifier.
   * @exception  TorqueException  Database problems.
   */
  public DBIdentifier(int atom, DBSpectrum spectrum, String identifier) throws TorqueException {
    setIdentifier(identifier);
    setAtomId(atom);
    setDBSpectrum(spectrum);
  }


  /**
   *  Saves this object in case there is not yet one.
   *
   * @exception  Exception  Database problems.
   */
  public void save(Connection dbconn) throws TorqueException {
	  if(this.getIdentifier().equals(""))
		  return;
	    Criteria crit = new Criteria();
	    crit.add(DBIdentifierPeer.ATOM_ID, getDBAtom(dbconn).getAtomId());
	    crit.add(DBIdentifierPeer.SPECTRUM_ID, getDBSpectrum().getSpectrumId());
	    if (DBIdentifierPeer.doSelect(crit).size() == 0) {
	      super.save(dbconn);
	    }
  }
  
  public DBAtom getDBAtom(Connection conn) throws TorqueException{
	  return DBAtomPeer.retrieveByPK(SimpleKey.keyFor(this.getAtomId()),conn);
  }
}


