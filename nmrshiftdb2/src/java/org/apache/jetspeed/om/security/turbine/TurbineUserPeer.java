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
package org.apache.jetspeed.om.security.turbine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;

import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.om.security.JetspeedUserFactory;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.openscience.nmrshiftdb.om.DBUserHistory;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.Schema;
import com.workingdogs.village.Value;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class TurbineUserPeer
    extends org.apache.jetspeed.om.security.turbine.BaseTurbineUserPeer
{
	
    /** The schema. */
    private static Schema schema = initTableSchema(TABLE_NAME);

    /** The columns. */
    private static com.workingdogs.village.Column[] columns = 
        initTableColumns(schema);


    /** The names of the columns. */
    public static String[] columnNames = initColumnNames(columns);
	
    /** 
     * Selects Jetspeed Users or a derived class there of, creating the
     * JetspeedUsers,  and populating each user's hashtable of attributes
     * from the result set of the query
     *
     * @param criteria the query criteria.
     * @return a List of JetspeedUsers
     */
   public static List doSelectUsers( Criteria criteria ) throws TorqueException
   {
       return populateUserObjects( doSelectVillageRecords(criteria) );
   }


   /**
    * The returned list will contain objects of the default type or
    * objects that inherit from the default.
    */
   public static List populateUserObjects(List records)
       throws TorqueException
   {
       List results = new ArrayList(records.size());

       // populate the object(s)
       for ( int i=0; i<records.size(); i++ )
       {
           Record row = (Record)records.get(i);
           results.add(TurbineUserPeer.row2UserObject(row, 1, TurbineUserPeer.getOMClass()));
       }
       return results;
   }


   /**
    * Create a new object of type cls from a resultset row starting
    * from a specified offset.  This is done so that you can select
    * other rows than just those needed for this object.  You may
    * for example want to create two objects from the same row.
    */
   public static JetspeedUser row2UserObject (Record row,
                                             int offset,
                                             Class cls )
       throws TorqueException
   {
       try
       {
           //TurbineUser obj = (TurbineUser)cls.newInstance();
           //populateObject(row, offset, obj);
           //obj.setModified(false);
           //obj.setNew(false);
           JetspeedUser user = JetspeedUserFactory.getInstance(false);
           populateUserObject(row, offset, user);
           return user;
       }
       catch (Exception e)
       {
           throw new TorqueException(e);
       }
   }

   protected static final String TURBINE_OBJECTDATA = "OBJECTDATA";

   /*
    * Populates a JetspeedUser from a single row in the result set.
    * The hashtable of attributes for each user is populated with columns
    * from the result set. 
    *
    * @param row a row containing a single user's attributes.
    * @param user the JetspeedUser object to be populated.
    *
    */
   protected static void populateUserObject(Record row, int offset, JetspeedUser user)
       throws Exception
   {
       // Set values are where columns are expected.  They are not
       // required to be in these positions, as we set the positions
       // immediately following.
       int idPosition = 1;
       int objectDataPosition = columnNames.length;
       for( int i=0; i<columnNames.length; i++ )
       {
           if (columnNames[i].equals(USER_ID))
               idPosition = i+1;

           if (columnNames[i].equals(TURBINE_OBJECTDATA))
               objectDataPosition = i+1;
       }

       // Restore the Permanent Storage Hashtable.  First the
       // Hashtable is restored, then any explicit table columns
       // which should be included in the Hashtable are added.
       byte[] objectData = (byte[])row.getValue(objectDataPosition).asBytes();
       Hashtable tempHash = (Hashtable)ObjectUtils.deserialize(objectData);
       if (tempHash == null)
       {
           tempHash = new Hashtable(10);
       }

       for( int j=0; j<columnNames.length; j++ )
       {
           if (! columnNames[j].equalsIgnoreCase( TURBINE_OBJECTDATA ) )
           {
               Object obj2 = null;
               Value value = row.getValue(j+1);
               if (value.isString()) 
                   obj2 = value.asString();
               else if (value.isBigDecimal()) 
                   obj2 = value.asBigDecimal();
               else if (value.isBytes()) 
                   obj2 = value.asBytes();
               else if (value.isDate()) 
                   obj2 = value.asDate();
               else if (value.isShort()) 
                   obj2 = new Short(value.asShort());
               else if (value.isInt()) 
                   obj2 = new Integer(value.asInt());
               else if (value.isLong()) 
                   obj2 = new Long(value.asLong());
               else if (value.isDouble()) 
                   obj2 = new Double(value.asDouble());
               else if (value.isFloat()) 
                   obj2 = new Float(value.asFloat());
               else if (value.isBoolean()) 
                   obj2 = new Boolean(value.asBoolean());
               else if (value.isTime()) 
                   obj2 = value.asTime();
               else if (value.isTimestamp()) 
                   obj2 = value.asTimestamp();
               else if (value.isUtilDate()) 
                   obj2 = value.asUtilDate();
               else if (value.isByte()) 
                   obj2 = new Byte(value.asByte());

               if ( obj2 != null )
               {
                   if (columnNames[j].equalsIgnoreCase( JetspeedUser.USER_ID ))
                   {
                       obj2 = value.toString();
                   }

                   tempHash.put( columnNames[j], obj2 );
               }
           }
       }
       user.setPermStorage( tempHash );
   }


   /*
    * Populates a JetspeedUser from a single row in the result set.
    * The hashtable of attributes for each user is populated with columns
    * from the result set.
    *
    * @param row a row containing a single user's attributes.
    * @param user the JetspeedUser object to be populated.
    *
    */
   /*
   DST: COMMITTED 8/19 FROM AMIT AMTE - IT HAS SOME BUGS, GOING TO REMOVE UNTIL RESOLVED
   
   protected static void populateUserObject(Record row, int offset, JetspeedUser user)
       throws Exception
   {
       // Restore the Permanent Storage Hashtable.  First the
       // Hashtable is restored, then any explicit table columns
       // which should be included in the Hashtable are added.
       byte[] objectData = (byte[])row.getValue(OBJECTDATA).asBytes();
       Hashtable tempHash = (Hashtable)ObjectUtils.deserialize(objectData);
       if (tempHash == null)
       {
           tempHash = new Hashtable(10);
       }

       for( int j=0; j<columnNames.length; j++ )
       {
           if (! columnNames[j].equalsIgnoreCase( OBJECTDATA ) )
           {
               Object obj2 = null;
               Value value = null;
               try {
                   value = row.getValue(columnNames[j]);
               } catch (Exception ex) {
                   // Column missing from the torque record ..
                   // Probable/intentional mis-match between the
                   // torque schema and db table
                   continue;
               }

               if (value.isString())
                   obj2 = value.asString();
               else if (value.isBigDecimal())
                   obj2 = value.asBigDecimal();
               else if (value.isBytes())
                   obj2 = value.asBytes();
               else if (value.isDate())
                   obj2 = value.asDate();
               else if (value.isShort())
                   obj2 = new Short(value.asShort());
               else if (value.isInt())
                   obj2 = new Integer(value.asInt());
               else if (value.isLong())
                   obj2 = new Long(value.asLong());
               else if (value.isDouble())
                   obj2 = new Double(value.asDouble());
               else if (value.isFloat())
                   obj2 = new Float(value.asFloat());
               else if (value.isBoolean())
                   obj2 = new Boolean(value.asBoolean());
               else if (value.isTime())
                   obj2 = value.asTime();
               else if (value.isTimestamp())
                   obj2 = value.asTimestamp();
               else if (value.isUtilDate())
                   obj2 = value.asUtilDate();
               else if (value.isByte())
                   obj2 = new Byte(value.asByte());

               if ( obj2 != null )
               {
                   if (columnNames[j].equalsIgnoreCase( JetspeedUser.USER_ID ))
                   {
                       obj2 = value.toString();
                   }

                   System.out.println(columnNames[j] + ": " + obj2.toString());
                   tempHash.put( columnNames[j], obj2 );
               }
           }
       }
       user.setPermStorage( tempHash );
   }
   */

   /**
    * Builds a criteria object based upon an User object
    */
   public static Criteria buildCriteria(JetspeedUser user)
   {
       Hashtable permData = (Hashtable) user.getPermStorage().clone();
       Criteria criteria = new Criteria();
       if ( !user.isNew() )
       {
           criteria.add(USER_ID, new NumberKey(user.getUserId()));
       }

       for (int i=1; i < columnNames.length; i++ )
       {
           if ( permData.containsKey(columnNames[i]) )
           {
               String key = new StringBuffer(TABLE_NAME)
                   .append('.').append(columnNames[i]).toString();

               criteria.add( key,
                             permData.remove(TurbineUserPeer.columnNames[i]) );
           }
       }
       
       // fix for MySQL failing to retrieve bad values generated by Village
       if (user.isNew())
       {
           Date created = user.getCreateDate();
           if (created == null)
           {
               created = new Date();
           }
           criteria.add( "TURBINE_USER.CREATED", created);
           criteria.add( "TURBINE_USER.PASSWORD_CHANGED", created);
       }
       
       String dataKey = new StringBuffer(TABLE_NAME).append('.').append(TURBINE_OBJECTDATA).toString();
       criteria.add( dataKey, permData );
       return criteria;
   }
   
	/**
	 *Gets the x best submitters
	 *
	 * @param  numberOfTopUsers  Number of user to return (i. e. x)
	 * @return                   Vector of NmrshiftdbUser
	 * @exception  Exception     Database problems
	 */
	public static List<TurbineUser> getUsersByContribution(int numberOfTopUsers, ServletConfig servcon) throws Exception {
		Connection dbconn = null;
		List<TurbineUser> users = new Vector<>();
		try{
			dbconn = Torque.getConnection();
			Statement stmt = dbconn.createStatement();
			ResultSet rs = stmt.executeQuery("select DISTINCT TURBINE_USER.USER_ID, COUNT(SPECTRUM_ID) AS CONTRIBUTIONS from TURBINE_USER, SPECTRUM, USER_HISTORY where TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null AND TURBINE_USER.HALL_OF_FAME=\"true\" and TURBINE_USER.USER_ID = SPECTRUM.USER_ID AND "+(!new UrlTool().isLabserver() ? "" : "USER_HISTORY.LABGROUP_ID >0 AND ")+"(SPECTRUM.REVIEW_FLAG=\"true\" OR SPECTRUM.REVIEW_FLAG=\"false\") GROUP BY SPECTRUM.USER_ID ORDER BY CONTRIBUTIONS DESC, LOGIN_NAME;");
			int i = 0;
			while (rs.next()) {
				if (i == numberOfTopUsers) {
					break;
				}
				Criteria crit = new Criteria();
				crit.add(TurbineUserPeer.USER_ID, rs.getInt(1));
				List<TurbineUser> v = TurbineUserPeer.doSelect(crit);
				TurbineUser user = (TurbineUser) v.get(0);
				user.setContributions(rs.getInt(2));
				users.add(user);
				i++;
			}
		}
		catch(Exception ex){
			throw ex;
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
		return (users);
	}


	/**
	 *Gets an NmrshiftdbUser by login name.
	 *
	 * @param  name           The login name.
	 * @return                The user.
	 * @exception  Exception  Database problems.
  * @exception  NmrshiftdbException  Failed to select one and only one row.
	 */
	public static TurbineUser getByName(String name) throws TorqueException {
		Criteria criteria = new Criteria();
		criteria.add(LOGIN_NAME, name);
		List v = doSelect(criteria);
		if (v.size() != 1) {
			throw new TorqueException("Failed to select one and only one row.");
		} else {
			return (TurbineUser) v.get(0);
		}
	}
	
	
	  /**
	   *  Gets a  User by Name Or Makes a New one with this name (but not a working one!).
	   *
	   * @param  userName       The name.
	   * @return                The byUserNameOrMakeNew value
	   * @exception  Exception  Database problems.
	   */
	  public static TurbineUser getByUserNameOrMakeNew(String userName) throws Exception {
	      Criteria crit = new Criteria();
	      crit.add(TurbineUserPeer.LOGIN_NAME, userName);
	      List v = TurbineUserPeer.doSelect(crit);
	      if (v.size() > 0) {
	        return((TurbineUser) v.get(0));
	      } else {
	 				TurbineUser user = new TurbineUser();
					user.setLoginName(userName);
					user.setPasswordValue("");
					user.setCreated(new Date());
					user.setLastLogin(new Date(0));
					user.setFirstName("");
					user.setLastName("");
					user.setTitle("");
					user.setAddress("");
					user.setCity("");
					user.setState("");
					user.setZipCode("");
					user.setCountry("");
					user.setWebPage("");
					user.setEmail("");
	        user.save();
	        DBUserHistory userHistory=new DBUserHistory();
	        userHistory.setAffiliation1("");
	        userHistory.setAffiliation2("");
	        userHistory.setJoinDate(user.getCreated());
	        userHistory.save();
	        return(user);
	      }
	  }
	  
	  public static String getUserTableHtml() throws TorqueException, DataSetException{
		  StringBuffer sb=new StringBuffer();
		  List result=TurbineUserPeer.executeQuery("select LOGIN_NAME, FIRST_NAME, LAST_NAME, HALL_OF_FAME, count(*) from TURBINE_USER join SPECTRUM using (USER_ID) where LOGIN_NAME not in ('bulkload', 'mainz', 'mainz2', 'admin', 'dkfz') and HALL_OF_FAME='true' group by USER_ID order by LAST_NAME, FIRST_NAME");
		  sb.append("<table><tr><th>User</th><th>Number of submits</th></tr>");
		  for(int i=0;i<result.size();i++){
			  if(((Record)result.get(i)).getValue(4).asString().equals(NmrshiftdbConstants.TRUE))
				  sb.append("<tr><td>"+((Record)result.get(i)).getValue(3).asString()+" "+((Record)result.get(i)).getValue(2).asString()+"</td><td>"+((Record)result.get(i)).getValue(5).asInt()+"</td></tr>");
			  else
				  sb.append("<tr><td>"+((Record)result.get(i)).getValue(1).asString()+"</td><td>"+((Record)result.get(i)).getValue(5).asInt()+"</td></tr>");
		  }
		  sb.append("</table>");
		  return sb.toString();
	  }

}
