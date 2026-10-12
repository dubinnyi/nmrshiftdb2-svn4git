/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013,2014 Stefan Kuhn
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    17. April 2002
 */
public class DBConditionType
		 extends org.openscience.nmrshiftdb.om.BaseDBConditionType
		 implements Persistent {
	/**
	 *For saving temporarily the value entered by the user
	 */
	private String currentValue = "";
	/**
	 *For saving temporarily the value selected in the drop-down box by the user
	 */
	private DBCondition currentSelectedCondition = null;


	/**
	 *Sets the currentValue attribute of the DBConditionType object, transforming frequency if property is frequency.
	 *
	 * @param currentSpectrumType The spectrum type to use for calculation of frequency.    
	 * @param  value  The new currentValue value
	 * @param changeFrequency If true the value in case of a frequency is divided by the frequency ratio of the spectrum type.
	 */
	public void setCurrentValue(String value, DBSpectrumType currentSpectrumType, boolean changeFrequency) {
		//TODO have 3 hardcoded here is not great, but I can't see a better solution
        if(changeFrequency && getConditionTypeId()==3 && currentSpectrumType!=null && !value.equals("") && !value.equals(NmrshiftdbConstants.UNREPORTED) && !value.equals(NmrshiftdbConstants.UNKNOWN)){
      	  float frequency1H=Float.parseFloat(value);
      	  currentValue=Double.toString(frequency1H/currentSpectrumType.getFrequencyRatio());
        }else{
        	currentValue = value;
        }
	}


	/**
	 *Sets the currentValue attribute of the DBConditionType object
	 *
	 * @param  value  The new currentValue value
	 */
	public void setCurrentValue(String value) {
       	currentValue = value;
	}
	
	
	/**
	 *Sets the currentSelectedCondition attribute of the DBConditionType object
	 *
	 * @param  value  The new currentSelectedCondition value
	 */
	public void setCurrentSelectedCondition(DBCondition cond) {
		currentSelectedCondition = cond;
	}


	/**
	 *Gets the currentValue attribute of the DBConditionType object, transforming frequency if property is frequency.
	 *
	 * @param currentSpectrumType The spectrum type to use for calculation of frequency.    
	 * @return    The currentValue value
	 */
	public String getCurrentValue(DBSpectrumType currentSpectrumType) {
		//TODO have 3 hardcoded here is not great, but I can't see a better solution
		if(this.getConditionTypeId()==3 && currentSpectrumType!=null && !currentValue.equals("") && !currentValue.equals(NmrshiftdbConstants.UNREPORTED) && !currentValue.equals(NmrshiftdbConstants.UNKNOWN)){
    		float frequency1H=Float.parseFloat(this.currentValue);
        	return Double.toString(frequency1H*currentSpectrumType.getFrequencyRatio());
		}
		return (currentValue);
	}


	/**
	 *Gets the currentValue attribute of the DBConditionType object
	 *
	 * @return    The currentValue value
	 */
	public String getCurrentValue() {
		return (currentValue);
	}

	
	/**
	 *Gets the currentSelectedCondition attribute of the DBConditionType object
	 *
	 * @return    The currentSelectedCondition value
	 */
	public DBCondition getCurrentSelectedCondition() {
		return (currentSelectedCondition);
	}


	/**
	 *Gets the existing DBConditions for this DBConditionType object ordered by frequency
	 *
	 * @return                The existing DBConditions
	 * @throws TorqueException 
	 * @throws SQLException 
	 * @exception  Exception  Problems with reading database
	 */
	public List<DBCondition> getExistingConditions() throws TorqueException, SQLException  {
		Connection dbconn = null;
		Vector<DBCondition> result=new Vector<DBCondition>();
		try{
			Vector<DBCondition> unconds=new Vector<DBCondition>();
			dbconn = Torque.getConnection();
			Statement stmt = dbconn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT COUNT(`NMR_CONDITION`.CONDITION_ID) AS FREQUENCY, `NMR_CONDITION`.CONDITION_ID FROM `NMR_CONDITION` LEFT JOIN SPECTRUM_CONDITION ON `NMR_CONDITION`.CONDITION_ID=SPECTRUM_CONDITION.CONDITION_ID WHERE `NMR_CONDITION`.CONDITION_TYPE_ID="+this.getConditionTypeId()+" GROUP BY `NMR_CONDITION`.CONDITION_ID ORDER BY FREQUENCY DESC;");
			while(rs.next()){
				Criteria crit = new Criteria();
				crit.add(DBConditionPeer.CONDITION_ID, rs.getString(2));
				List<DBCondition> v= DBConditionPeer.doSelect(crit);
				if(((DBCondition)v.get(0)).getValue().equalsIgnoreCase("unknown") || ((DBCondition)v.get(0)).getValue().equalsIgnoreCase("unreported") && (this.getRequired().equals("true"))){
					unconds.add((DBCondition)v.get(0));
				}else{
					result.add(v.get(0));
				}
			}
			if(this.getRequired().equals("true")){
				for(int i=0;i<unconds.size();i++)
					result.add(unconds.get(i));
			}
		}
		finally
		{
			try
			{
				Torque.closeConnection(dbconn);
			}
			catch (Exception ex)
			{
				throw ex;
			}
		}
		return (result);
	}
}


