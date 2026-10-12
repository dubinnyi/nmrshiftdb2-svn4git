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
package org.openscience.nmrshiftdb.util;

/**
 *This is a class containing a String and an int
 *
 * @author     shk3
 * @created    4. M�rz 2002
 */
public class StringAndInt {
  /**
   *  The string to hold
   */
  public String myString = "";
  /**
   *  The int to hold
   */
  public int myInt = 0;


  /**
   *Constructor for the StringAndInt object
   */
  public StringAndInt() { }


  /**
   *Constructor for the StringAndInt object
   *
   * @param  str  The string
   * @param  i    The int
   */
  public StringAndInt(String str, int i) {
    myString = str;
    myInt = i;
  }


  /**
   *  Gets the string attribute of the StringAndInt object
   *
   * @return    The string value
   */
  public String getString() {
    return (myString);
  }


  /**
   *  Gets the int attribute of the StringAndInt object
   *
   * @return    The int value
   */
  public int getInt() {
    return myInt;
  }


  /**
   *  Gets the integer attribute of the StringAndInt object
   *
   * @return    The integer value
   */
  public Integer getInteger() {
    return (new Integer(myInt));
  }
}

