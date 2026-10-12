/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2015 Stefan Kuhn
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.openscience.nmrshiftdb.modules.actions.portlets.result.ResultAction;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.UrlTool;

import com.workingdogs.village.DataSetException;

// JDK classes


/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    April 22, 2005
 */
@SuppressWarnings("serial")
public class DBBookmark
     extends org.openscience.nmrshiftdb.om.BaseDBBookmark
     implements Persistent {

  /**
   *Constructor for the DBBookmark object
   *
   * @param  userid         The user the bookmar is for.
   * @param  moleculeid     Molecule to bookmark.
   * @exception  TorqueException  Description of Exception
   */
  public DBBookmark(int userid, int moleculeid) throws TorqueException {
    super();
    this.setMoleculeId(moleculeid);
    this.setUserId(userid);
  }


  /**
   *Constructor for the DBBookmark object
   */
  public DBBookmark() {
    super();
  }
  
  /**
   * Stores the object in the database.  If the object is new and not yet existing,
   * it inserts it; otherwise an update is performed.
   */
  public void save() throws TorqueException{
	  if(this.isNew()){
	      Criteria criteria=new Criteria();
	      criteria.add(DBBookmarkPeer.MOLECULE_ID, this.getMoleculeId());
	      criteria.add(DBBookmarkPeer.USER_ID, this.getUserId());
	      List<DBBookmark> v = DBBookmarkPeer.doSelect(criteria);
	      if ( v.size() >0)
	    	  return;
	  }
  	  super.save();
  }
}


