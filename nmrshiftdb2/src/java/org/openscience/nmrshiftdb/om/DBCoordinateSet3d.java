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
 * @created    September 23, 2004
 */
public class DBCoordinateSet3d
     extends org.openscience.nmrshiftdb.om.BaseDBCoordinateSet3d
     implements Persistent {

  /**
   *Constructor for the DBCoordinateSet3d object.
   */
  public DBCoordinateSet3d() {
    super();
  }
  

  /**
   *Constructor for the DBCoordinateSet3d object.
   *
   * @param  x              x coordinate.
   * @param  y              y coordinate.
   * @param  z              z coordinate.
   * @param  typeId         The coordinate set type id.
   * @exception  TorqueException  Database problems.
   */
  public DBCoordinateSet3d(double x, double y, double z, int typeId) throws TorqueException {
    if (Double.isNaN(x)) {
      setXCoordinate(0);
    } else {
      setXCoordinate(x);
    }
    if (Double.isNaN(y)) {
      setYCoordinate(0);
    } else {
      setYCoordinate(y);
    }
    if (Double.isNaN(z)) {
      setZCoordinate(0);
    } else {
      setZCoordinate(z);
    }
    setCoordinateSetTypeId(typeId);
  }


  /**
   *Constructor for the DBCoordinateSet3d object.
   *
   * @param  x              x coordinate.
   * @param  y              y coordinate.
   * @param  z              z coordinate.
   * @param  typeId         The coordinate set type id.
   * @param  atom           The atom the coordinates come from.
   * @exception  Exception  Description of Exception.
   */
  public DBCoordinateSet3d(double x, double y, double z, int typeId, int atom) throws Exception {
    this(x,y,z,typeId);
    setAtomId(atom);
  }
}


