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
package org.openscience.nmrshiftdb.daemons;

//nmrshiftdb stuff
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.torque.Torque;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;

import com.workingdogs.village.Record;

/**
A daemon that deletes all spectra which have been 
rejected and are older than two months. It also deletes
serialized files of rejected or edited spectra.
This is not run in normal installations, we want to keep things for documentation.

@author Stefan Kuhn
*/
public class ReviewDaemon extends AbstractDaemonWithServletConfig {
  
  public static float[] predictedvaluesfloat=null;
  
    /**
    Does the task
    */
    public void run(ServletConfig servcon) {
        Log.info("checking review entries");
        this.setResult( Daemon.RESULT_PROCESSING );
        Connection dbconn = null;
        Statement stmt = null;
        try{
          Criteria critSpectra=new Criteria();
          dbconn = Torque.getConnection();
          stmt = dbconn.createStatement();
          stmt.executeUpdate("BEGIN");
          String sql="select NMRSHIFTDB_NUMBER from SPECTRUM where REVIEW_FLAG=\"edited\" or REVIEW_FLAG=\"rejected\"";
          ResultSet rs=stmt.executeQuery(sql);
          while(rs.next()){
            File file=new File(GeneralUtils.getNmrshiftdbProperty("serializeddir", servcon) + File.separatorChar + rs.getString(1).replace('/', '|') + ".xml");
            if(file.exists())
              file.delete();
          }
          stmt.executeUpdate("BEGIN");
          critSpectra.add(DBSpectrumPeer.REVIEW_FLAG, "rejected");
          Criteria.Criterion criterion = critSpectra.getCriterion(DBSpectrumPeer.REVIEW_FLAG);
          criterion.or(critSpectra.getNewCriterion(criterion.getTable(), criterion.getColumn(), "edited", Criteria.EQUAL));
          criterion.or(critSpectra.getNewCriterion(criterion.getTable(), criterion.getColumn(), "change", Criteria.EQUAL));
          Date x=new Date(System.currentTimeMillis()-5184000000L);//This is 60 days ~ 2 month
          critSpectra.add(DBSpectrumPeer.DATE, x , Criteria.LESS_THAN);
          List<DBSpectrum> spectra=DBSpectrumPeer.doSelect(critSpectra);
          //Do we have any spectra to delete? If yes
          if(spectra.size()>0){
            for(int i=0;i<spectra.size();i++){
              ((DBSpectrum)spectra.get(i)).delete(dbconn,true);
            }
          }
          //Find molecules with no spectra (this should never happen, but for database maintanance we look for it) in spectrum and wishlist and not used in sample
          List<Record> l=DBMoleculePeer.executeQuery("SELECT MOLECULE.MOLECULE_ID FROM ((MOLECULE LEFT JOIN SPECTRUM ON MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID) left join SAMPLE on MOLECULE.MOLECULE_ID=SAMPLE.PROBABLE_STRUCTURE) left join WISHLIST on MOLECULE.MOLECULE_ID=WISHLIST.MOLECULE_ID WHERE SPECTRUM.MOLECULE_ID IS NULL and SAMPLE.PROBABLE_STRUCTURE is null and WISHLIST.MOLECULE_ID is null;");
          for(int i=0;i<l.size();i++){
            DBMolecule mol=DBMoleculePeer.retrieveByPK(new NumberKey(((Record) l.get(i)).getValue(1).asString()));
            if(mol.getDBWishlists().size()==0 && mol.getDBSamples().size()==0)
              mol.delete(dbconn);
          }
          stmt.executeUpdate("COMMIT");
        }
        catch(Exception ex){
          try {
            stmt.executeUpdate("ROLLBACK");
          } catch (SQLException e) {
            e.printStackTrace();
          }
          GeneralUtils.logError(ex,"Error in ReviewDaemon.run", null,false);
        }
        finally
        {
          try
          {
            Torque.closeConnection(dbconn);
          }
          catch (Exception ex)
          {
            Log.error("Error in ReviewDaemon");
          }
        }
        this.setResult( Daemon.RESULT_SUCCESS );
    }
}
