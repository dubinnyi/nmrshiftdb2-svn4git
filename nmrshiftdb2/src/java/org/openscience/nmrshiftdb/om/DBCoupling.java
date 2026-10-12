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

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

// JDK classes

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    March 30, 2004
 */
@SuppressWarnings("serial")
public class DBCoupling
     extends org.openscience.nmrshiftdb.om.BaseDBCoupling
     implements Persistent {

  /**
   *Constructor for the DBCoupling object.
   */
  public DBCoupling() {
    super();
  }


  /**
   *Constructor for the DBCoupling object.
   *
   * @param  atom1          First atom to couple.
   * @param  atom2          Second atom to couple.
   * @param  spectrum       Spectrum this is part of
   * @param  constant       The coupling constant.
   * @exception  TorqueException  Description of Exception.
   */
  public DBCoupling(int atom1, int atom2, int spectrumid, double constant) throws TorqueException {
    this.setAtomId1(atom1);
    this.setAtomId2(atom2);
    setSpectrumId(spectrumid);
    setConstant(constant);
  }
}


