/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2014 Stefan Kuhn
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

import java.util.Collections;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.comparator.SolventComparator;

import com.workingdogs.village.Record;

// JDK classes


/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    February 19, 2003
 */
public class DBConditionPeer
     extends org.openscience.nmrshiftdb.om.BaseDBConditionPeer {

  /**
   *  Gets a DBCondition by a value compared with like
   *
   * @param      value      The value to compare
   * @return                The result
   * @exception  Exception  Database problems
   */
  public static List<DBCondition> getByValueLike(String value) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBConditionPeer.VALUE, (Object) ("%"+value+"%"), Criteria.LIKE);
    return (DBConditionPeer.doSelect(crit));
  }

  
  /**
   *  Gets a DBCondition by a value 
   *
   * @param      value      The value to findcompare
   * @return                The result
   * @exception  Exception  Database problems
   */
  public static List<DBCondition> getByValue(String value) throws Exception {
    Criteria crit = new Criteria();
    crit.add(DBConditionPeer.VALUE, value);
    return (DBConditionPeer.doSelect(crit));
  }
  
  public static DBCondition makeNewOrGetExisting(DBConditionType type, String value,int userid) throws TorqueException{
	  Criteria crit=new Criteria();
	  crit.add(DBConditionPeer.CONDITION_TYPE_ID, type.getConditionTypeId());
	  crit.add(DBConditionPeer.VALUE, value);
	  List<DBCondition> v=DBConditionPeer.doSelect(crit);
	  if(v.size()>0){
		  v.get(0).getDBConditionType();
		  return v.get(0);
	  }else{
		  DBCondition cond=new DBCondition();
		  cond.setDBConditionType(type);
		  cond.setValue(value);
		  cond.setUserId(userid);
		  return cond;
	  }
  }
  
  /**
   * Gets all solvents configured for some machine or all machines in automation mode.
   * 
   * @return The solvents.
   * @throws TorqueException Database problems.
   */
  public static List<Record> getSolventValuesAll() throws TorqueException {
    List<Record> result = executeQuery("select VALUE, CONDITION_ID from MACHINE_CONDITION join `NMR_CONDITION` using (CONDITION_ID) group by VALUE, CONDITION_ID, ORDER_POSITION order by ORDER_POSITION");
    return result;
  }

}

