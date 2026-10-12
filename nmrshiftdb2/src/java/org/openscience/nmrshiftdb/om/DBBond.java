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
import java.util.Vector;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    14. August 2002
 */
@SuppressWarnings("serial")
public class DBBond
     extends org.openscience.nmrshiftdb.om.BaseDBBond
     implements Persistent {

  /**
   *Constructor for the DBBond object
   */
  public DBBond() {
    super();
  }


  /**
   *Constructor for the DBBond object
   *
   * @param  bond                   The cdk bond this bond should represent.
   * @param  isDoubleBondSpecified  is this a specified double bond configuration?
   */
  public DBBond(IBond bond, boolean isDoubleBondSpecified) {
    super();
    setDegree(new Double(bond.getOrder().ordinal()).intValue());
    if (bond.getFlag(CDKConstants.ISAROMATIC)) {
      setIsAromatic(NmrshiftdbConstants.TRUE);
    } else {
      setIsAromatic(NmrshiftdbConstants.FALSE);
    }
    setStereo(bond.getStereo().ordinal());
    if (isDoubleBondSpecified == true) {
      setIsConfigurationSpecified(NmrshiftdbConstants.TRUE);
    } else {
      setIsConfigurationSpecified(NmrshiftdbConstants.FALSE);
    }
  }


  /**
   *  Gets the atoms of the DBBond object
   *
   * @return                The atoms
   * @exception  TorqueException  database problems
   */
  public Vector<DBAtom> getAtoms() throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBondDBAtomPeer.BOND_ID, this.getBondId());
    crit.addAscendingOrderByColumn(DBBondDBAtomPeer.ORDER_NUMBER);
    List<DBBondDBAtom> v = DBBondDBAtomPeer.doSelect(crit);
    Vector<DBAtom> atoms = new Vector<DBAtom>();
    for (int i = 0; i < v.size(); i++) {
      atoms.add(DBAtomPeer.retrieveByPK(((DBBondDBAtom) v.get(i)).getAtomId()));
    }
    return atoms;
  }


  /**
   *  Deletes a bond
   *
   * @param  conn           The connection to use.
   * @exception  Exception  Database problems.
   */
  public void delete(Connection conn) throws TorqueException {
    Criteria crit = new Criteria();
    crit.add(DBBondPeer.BOND_ID, getBondId());
    if(conn==null)
    	DBBondPeer.doDelete(crit);
    else
    	DBBondPeer.doDelete(crit,conn);    	
    return;
  }
}


