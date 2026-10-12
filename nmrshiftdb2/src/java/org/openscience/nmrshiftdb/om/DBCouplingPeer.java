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
import java.util.List;

import org.apache.torque.TorqueException;

import com.workingdogs.village.Record;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    March 1, 2004
 */
@SuppressWarnings("serial")
public class DBCouplingPeer
     extends org.openscience.nmrshiftdb.om.BaseDBCouplingPeer {

  /**
   *  Gets all couplings of an atom in a spectrum.
   *
   * @param  spectrum       The spectrum.
   * @param  atom           The atom.
   * @return                The DBCouplings.
   * @exception  Exception  Database problems.
   */
  public static List<Record> getBySpectrumAndAtom(DBSpectrum spectrum, DBAtom atom) throws TorqueException {
    String sql="SELECT CONSTANT FROM COUPLING WHERE (COUPLING.SPECTRUM_ID="+spectrum.getSpectrumId()+") AND (COUPLING.ATOM_ID_2="+atom.getAtomId()+" OR COUPLING.ATOM_ID_1="+atom.getAtomId()+")";
    List<Record> l=DBCouplingPeer.executeQuery(sql);
    return (l);
  }
}

