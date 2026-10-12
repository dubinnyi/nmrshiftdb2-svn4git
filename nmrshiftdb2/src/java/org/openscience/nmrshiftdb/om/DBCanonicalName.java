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
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.openscience.nmrshiftdb.util.Soundex;
import org.openscience.nmrshiftdb.util.StringAndInt;

/** 
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public  class DBCanonicalName 
    extends org.openscience.nmrshiftdb.om.BaseDBCanonicalName
    implements Persistent
{
  
  public DBCanonicalName(){
    super();
  }
  
  public DBCanonicalName(String autonom, int mol) throws TorqueException{
    super();
    setMoleculeId(mol);
    setCanonicalNameTypeId(1);
    setName(autonom);
    setNameSoundex(Soundex.soundex(autonom, "9", new StringAndInt()));
  }

  public DBCanonicalName(String name, int mol, int typeindex) throws Exception{
    super();
    setMoleculeId(mol);
    setCanonicalNameTypeId(typeindex);
    setName(name);
    setNameSoundex(Soundex.soundex(name, "9", new StringAndInt()));
  }
}



