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
import org.apache.torque.om.Persistent;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    31. Mai 2002
 */
public class DBSpectrumHyperlink
		 extends org.openscience.nmrshiftdb.om.BaseDBSpectrumHyperlink
		 implements Persistent {
	/**
	 *The equals method comparing by the Hyperlink fiels
	 *
	 * @param  hl  The DBSpectrumHyperlink to compare with
	 * @return     false=unequal, true=equal
	 */
	public boolean equals(DBSpectrumHyperlink hl) {
		if (this.getHyperlink().equals(hl.getHyperlink())) {
			return true;
		} else {
			return false;
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


