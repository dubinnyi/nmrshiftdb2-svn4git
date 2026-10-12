/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2014 Stefan Kuhn
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;

import com.workingdogs.village.Record;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    8. April 2002
 */
public class DBMoleculePeer
     extends org.openscience.nmrshiftdb.om.BaseDBMoleculePeer {

  /**
   *Method returns molecules with a certain chemical name and at least one spectrum reviewed as true.
   *
   * @param  chemName       The chemical name.
   * @return                A vector of DBMolecules.
   * @exception  Exception  Something wrong with the DB.
   */
  public static List<DBMolecule> doSelectByChemicalName(String chemName) throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBChemicalNamePeer.MOLECULE_ID, DBMoleculePeer.MOLECULE_ID);
    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    crit.add(DBChemicalNamePeer.NAME, chemName);
    crit.add(DBSpectrumPeer.REVIEW_FLAG, "true");
    crit.setDistinct();
    return DBMoleculePeer.doSelect(crit);
  }


  /**
   *Method returns molecules with a certain smiles and at least one spectrum reviewed as true.
   *
   * @param  smiles         The smiles.
   * @param  smilesChiral   The chiral smiles.
   * @param  unequal        true=choose spectra with other chiral smiles, false=same chiral smiles.
   * @return                A vector of DBMolecules.
   * @exception  Exception  Something wrong with the DB.
   */
  public static DBMolecule doSelectByInchi(String inchi) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBCanonicalNamePeer.NAME, inchi);
    List<DBCanonicalName> v = DBCanonicalNamePeer.doSelect(crit);
    if(v.size()>0){
      return ((DBCanonicalName)v.get(0)).getDBMolecule();
    }else{
      return null;
    }
  }
  
  public static List<DBMolecule> doSelectByInchi(String inchi, boolean chiral, boolean unequal) throws Exception {
      String inchichiral=null;
      if(StringUtils.countMatches(inchi, "/")>3){
          StringTokenizer st=new StringTokenizer(inchi,"/");
          inchichiral=inchi;
          StringBuffer sb=new StringBuffer();
          for(int i=0;i<4;i++){
              sb.append(st.nextToken()).append("/");
          }
          inchi=sb.substring(0,sb.length()-1);
      }
      String sql=null;
      if(!chiral){
          sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME = '"+inchi+"'";
      }else if(!unequal){
          sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME like '"+inchichiral+"%'";
      }else{
          sql="select MOLECULE_ID from MOLECULE join CANONICAL_NAME using (MOLECULE_ID) where CANONICAL_NAME_TYPE_ID =4 and CANONICAL_NAME.NAME like '"+inchi+"%' and CANONICAL_NAME.NAME not like '"+inchichiral+"%' and CANONICAL_NAME.NAME != '"+inchi+"'";          
      }
      List<Record> l = DBMoleculePeer.executeQuery(sql);
      ArrayList<DBMolecule> v = new ArrayList<DBMolecule>();
      for(int k=0;k<l.size();k++){
          v.add(DBMoleculePeer.retrieveByPK(new NumberKey(l.get(k).getValue(1).asInt())));
          k++;
      }
      return v;
  }


  /**
   *Method returns molecules with a certain smiles and at least one spectrum reviewed as true.
   *
   * @param  smiles         The smiles.
   * @param  smilesChiral   The chiral smiles.
   * @param  unequal        true=choose spectra with other chiral smiles, false=same chiral smiles.
   * @return                A vector of DBMolecules.
   * @exception  Exception  Something wrong with the DB.
   */
  public static List<DBMolecule> doSelectBySmiles(String smiles, String smilesChiral, boolean unequal) throws Exception {
    Criteria crit = new Criteria();
    crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBSpectrumPeer.MOLECULE_ID);
    crit.add(DBMoleculePeer.SMILES_STRING, smiles);
    if (unequal) {
      crit.add(DBMoleculePeer.SMILES_STRING_CHIRAL, (Object) smilesChiral, Criteria.NOT_EQUAL);
      Criteria.Criterion criterion = crit.getCriterion(DBMoleculePeer.SMILES_STRING_CHIRAL);
      criterion.and(crit.getNewCriterion(criterion.getTable(), criterion.getColumn(), (Object) null, Criteria.NOT_EQUAL));
    } else {
      crit.add(DBMoleculePeer.SMILES_STRING_CHIRAL, smilesChiral);
    }
    crit.add(DBSpectrumPeer.REVIEW_FLAG, "true");
    crit.setDistinct();
   	return DBMoleculePeer.doSelect(crit);
  }
}

