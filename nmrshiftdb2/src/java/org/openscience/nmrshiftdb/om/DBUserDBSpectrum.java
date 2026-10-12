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
import java.util.Date;
import java.util.List;

import org.apache.jetspeed.om.security.turbine.TurbineUser;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    May 19, 2004
 */
public class DBUserDBSpectrum
     extends org.openscience.nmrshiftdb.om.BaseDBUserDBSpectrum
     implements Persistent {
  /**
   *Constructor for the DBUserDBSpectrum object
   */
  public DBUserDBSpectrum() {
    super();
  }


  /**
   *Constructor for the DBUserDBSpectrum object
   *
   * @param  spectrum         The spectrum.
   * @param  reviewer         The reviewer.
   * @param  assignementDate  Date of assignment (normally now).
   * @throws TorqueException 
   * @exception  Exception    Database problems.
   */
  public DBUserDBSpectrum(int spectrum, TurbineUser reviewer, Date assignementDate) throws TorqueException {
    super();
    setSpectrumId(spectrum);
    this.setUserId(reviewer.getUserId());
    setAssignmentDate(assignementDate);
    Criteria crit = new Criteria();
    crit.add(DBUserDBSpectrumPeer.SPECTRUM_ID, spectrum);
    crit.add(DBUserDBSpectrumPeer.USER_ID, reviewer.getUserId());
    List<DBUserDBSpectrum> v = DBUserDBSpectrumPeer.doSelect(crit);
    int largestId = -1;
    for (int i = 0; i < v.size(); i++) {
      DBUserDBSpectrum uspec = (DBUserDBSpectrum) v.get(i);
      if (uspec.getUserSpectrumId() > largestId) {
        largestId = uspec.getUserSpectrumId();
      }
    }
    setUserSpectrumId(largestId + 1);
  }
}


