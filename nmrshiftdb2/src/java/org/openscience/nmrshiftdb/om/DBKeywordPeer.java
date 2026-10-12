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

import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    14. August 2002
 */
@SuppressWarnings("serial")
public class DBKeywordPeer
     extends org.openscience.nmrshiftdb.om.BaseDBKeywordPeer {

  /**
   *  Gets all keywords assigned to any molecule.
   *
   * @return                The existingMoleculeKeywords, Vector of DBKeywords.
   * @exception  Exception  Description of Exception
   */
  public static List<DBKeyword> getExistingMoleculeKeywords() throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBKeywordPeer.KEYWORD_ID, DBMoleculeDBKeywordPeer.KEYWORD_ID);
    crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
    crit.setDistinct();
    crit.addJoin(DBMoleculeDBKeywordPeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
    return (DBKeywordPeer.doSelect(crit));
  }


  /**
   *  Gets all keywords assigned to any spectrum.
   *
   * @return                The existingSpectrumKeywords.
   * @exception  Exception  Description of Exception
   */
  public static String[] getExistingSpectrumKeywordsAsArray() throws Exception {
    List<DBKeyword> v=getExistingSpectrumKeywords();
    String[] keywords=new String[v.size()];
    for(int i=0;i<v.size();i++){
      keywords[i]=((DBKeyword)v.get(i)).getKeyword();
    }
    return(keywords);
  }
      

  /**
   *  Gets all keywords assigned to any molecule.
   *
   * @return                The existingMoleculeKeywords.
   * @exception  Exception  Description of Exception
   */
  public static String[] getExistingMoleculeKeywordsAsArray() throws Exception {
    List<DBKeyword> v=getExistingMoleculeKeywords();
    String[] keywords=new String[v.size()];
    for(int i=0;i<v.size();i++){
      keywords[i]=((DBKeyword)v.get(i)).getKeyword();
    }
    return(keywords);
  }


  /**
   *  Gets all keywords assigned to any spectrum.
   *
   * @return                The existingSpectrumKeywords, Vector of DBKeywords.
   * @exception  Exception  Description of Exception
   */
  public static List<DBKeyword> getExistingSpectrumKeywords() throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBKeywordPeer.KEYWORD_ID, DBSpectrumDBKeywordPeer.KEYWORD_ID);
    crit.addAscendingOrderByColumn(DBKeywordPeer.KEYWORD);
    crit.setDistinct();
    crit.addJoin(DBSpectrumDBKeywordPeer.SPECTRUM_ID, DBSpectrumPeer.SPECTRUM_ID);
    crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
    return (DBKeywordPeer.doSelect(crit));
  }


  /**
   *  Gets a DBKeyword by a keyword given as a string
   *
   * @param  kw             The keyword
   * @return                The DBKeyword
   * @exception  Exception  Database problems or no such keywords
   */
  public static DBKeyword doSelectByKeyword(String kw) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBKeywordPeer.KEYWORD, kw);
    List<DBKeyword> v = DBKeywordPeer.doSelect(crit);
    if (v.size() != 1) {
      throw new Exception("Failed to select one and only one keyword!");
    } else {
      return (DBKeyword) v.get(0);
    }
  }
}

