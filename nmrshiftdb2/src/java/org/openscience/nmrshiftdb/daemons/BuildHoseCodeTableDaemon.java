/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010-2015 Stefan Kuhn
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

//jetspeed stuff
import javax.servlet.ServletConfig;
import java.util.List;

import org.apache.jetspeed.daemon.Daemon;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.turbine.util.Log;
import org.openscience.nmrshiftdb.om.DBSpectrumPeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;

import com.workingdogs.village.Record;

/**
A daemon that builds various heap tables for performance reasons.


@author Stefan Kuhn
*/
public class BuildHoseCodeTableDaemon extends AbstractDaemonWithServletConfig {
    /**
    Does the task
    */
    public void run(ServletConfig servcon){
      Log.info("bulding fingerprints table");
      try{
    	BasePeer.executeStatement("drop table if exists FINGERPRINTS_NEW;");
        BasePeer.executeStatement("create table FINGERPRINTS_NEW engine=memory max_rows=1000000 SELECT distinct MOLECULE.MOLECULE_ID, MOLECULE.fp0, MOLECULE.fp1, MOLECULE.fp2, MOLECULE.fp3, MOLECULE.fp4, MOLECULE.fp5, MOLECULE.fp6, MOLECULE.fp7, MOLECULE.fp8, MOLECULE.fp9, MOLECULE.fp10, MOLECULE.fp11, MOLECULE.fp12, MOLECULE.fp13, MOLECULE.fp14, MOLECULE.fp15 from MOLECULE, SPECTRUM where MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID and SPECTRUM.REVIEW_FLAG=\"true\";");
        BasePeer.executeStatement("drop table if exists FINGERPRINTS;");
        BasePeer.executeStatement("alter table FINGERPRINTS_NEW rename FINGERPRINTS;");
      } catch (Exception ex) {
        GeneralUtils.logError(ex,"BuildHose2",null,false);
      }
	  Log.info("bulding hose code table");
      try{
	    BasePeer.executeStatement("drop table if exists HOSE_CODES_NEW;");
        BasePeer.executeStatement("create table HOSE_CODES_NEW SELECT DISTINCT ATOM.HOSE_CODE_WITH_RINGS AS HOSE_CODE, SHIFT.VALUE, SPECTRUM.SPECTRUM_ID, SPECTRUM.MOLECULE_ID, CONDITION_TYPE.CONDITION_TYPE, ATOM.SYMBOL, REVIEW_FLAG, 1 as WITH_RINGS, USER_HISTORY.LABGROUP_ID, NMR_CONDITION.CONDITION_ID  FROM SPECTRUM_TYPE, ISOTOPE, SPECTRUM, ATOM, SIGNAL_ATOM, `NMR_SIGNAL`, SHIFT, MOLECULE, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE, TURBINE_USER, USER_HISTORY WHERE TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null and (SPECTRUM_TYPE.DIMENSIONALITY='1') AND (SPECTRUM.REVIEW_FLAG='true') and SPECTRUM.SPECTRUM_ID=SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID=`NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID=CONDITION_TYPE.CONDITION_TYPE_ID AND ATOM.ATOM_ID=SIGNAL_ATOM.ATOM_ID AND SIGNAL_ATOM.SIGNAL_ID=NMR_SIGNAL.SIGNAL_ID AND NMR_SIGNAL.SPECTRUM_ID=SPECTRUM.SPECTRUM_ID AND NMR_SIGNAL.SIGNAL_ID=SHIFT.SIGNAL_ID AND SPECTRUM.SPECTRUM_TYPE_ID=SPECTRUM_TYPE.SPECTRUM_TYPE_ID AND SPECTRUM.MOLECULE_ID=MOLECULE.MOLECULE_ID AND SPECTRUM_TYPE.SPECTRUM_TYPE_ID=ISOTOPE.SPECTRUM_TYPE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and CONDITION_TYPE.CONDITION_TYPE_ID=2 and (CONDITION_TYPE=\"c\" or CONDITION_TYPE=\"m\")");
        BasePeer.executeStatement("INSERT INTO HOSE_CODES_NEW SELECT DISTINCT ATOM.HOSE_CODE, SHIFT.VALUE, SPECTRUM.SPECTRUM_ID, SPECTRUM.MOLECULE_ID, CONDITION_TYPE.CONDITION_TYPE, ATOM.SYMBOL, REVIEW_FLAG, 0, USER_HISTORY.LABGROUP_ID, NMR_CONDITION.CONDITION_ID FROM SPECTRUM_TYPE, ISOTOPE, SPECTRUM, ATOM, SIGNAL_ATOM, `NMR_SIGNAL`, SHIFT, MOLECULE, SPECTRUM_CONDITION, `NMR_CONDITION`, CONDITION_TYPE, TURBINE_USER, USER_HISTORY  WHERE TURBINE_USER.USER_ID=USER_HISTORY.USER_ID and USER_HISTORY.LEAVE_DATE is null and (SPECTRUM_TYPE.DIMENSIONALITY='1') AND (SPECTRUM.REVIEW_FLAG='true') and SPECTRUM.SPECTRUM_ID=SPECTRUM_CONDITION.SPECTRUM_ID and SPECTRUM_CONDITION.CONDITION_ID=`NMR_CONDITION`.CONDITION_ID and `NMR_CONDITION`.CONDITION_TYPE_ID=CONDITION_TYPE.CONDITION_TYPE_ID AND ATOM.ATOM_ID=SIGNAL_ATOM.ATOM_ID AND SIGNAL_ATOM.SIGNAL_ID=NMR_SIGNAL.SIGNAL_ID AND NMR_SIGNAL.SPECTRUM_ID=SPECTRUM.SPECTRUM_ID AND NMR_SIGNAL.SIGNAL_ID=SHIFT.SIGNAL_ID AND SPECTRUM.SPECTRUM_TYPE_ID=SPECTRUM_TYPE.SPECTRUM_TYPE_ID AND SPECTRUM.MOLECULE_ID=MOLECULE.MOLECULE_ID AND SPECTRUM_TYPE.SPECTRUM_TYPE_ID=ISOTOPE.SPECTRUM_TYPE_ID and SPECTRUM.USER_ID=TURBINE_USER.USER_ID and CONDITION_TYPE.CONDITION_TYPE_ID=2 and (CONDITION_TYPE=\"c\" or CONDITION_TYPE=\"m\")");
        BasePeer.executeStatement("CREATE INDEX HOSE_CODE_INDEX ON HOSE_CODES_NEW (HOSE_CODE(100));");
        //we remove those entries with marks < minimum.mark.prediction
        BasePeer.executeStatement("drop table if exists SPECTRA_TO_DELETE");
        BasePeer.executeStatement("create table SPECTRA_TO_DELETE select SPECTRUM_ID from USER_SPECTRUM_MARK where SPECTRUM_ID in(select distinct(SPECTRUM_ID) from USER_SPECTRUM_MARK where MARK<"+GeneralUtils.getNmrshiftdbProperty("minimum.mark.prediction")+") group by SPECTRUM_ID having avg(MARK)<"+GeneralUtils.getNmrshiftdbProperty("minimum.mark.prediction"));
        //If this is done in one statement it is very slow (at least in mysql 5.0.x)
        BasePeer.executeStatement("delete from HOSE_CODES_NEW where SPECTRUM_ID in (select SPECTRUM_ID from SPECTRA_TO_DELETE)");
        BasePeer.executeStatement("drop table if exists SPECTRA_TO_DELETE");
        BasePeer.executeStatement("drop table if exists HOSE_CODES;");
        BasePeer.executeStatement("rename table HOSE_CODES_NEW to HOSE_CODES;");
      } catch (Exception ex) {
        GeneralUtils.logError(ex,"BuildHose2",null,false);
      }
      Log.info("bulding d2 table");
      try{
          BasePeer.executeStatement("drop table if exists d2;");
          BasePeer.executeStatement("create table d2 engine=MEMORY max_rows=1000000 select SPECTRUM_ID , count(Distinct MULTIPLICITY) as c from NMR_SIGNAL join SIGNAL_ATOM using (SIGNAL_ID) group by SPECTRUM_ID;");
          BasePeer.executeStatement("alter table d2 add index (SPECTRUM_ID,c);");
      } catch (Exception ex) {
          GeneralUtils.logError(ex,"BuildHose2",null,false);
      }
      Log.info("bulding correlation strings");
      try{
    	  List<Record> l = DBSpectrumPeer.executeQuery("select SPECTRUM_ID from SPECTRUM where CORRELATION_SPECFILE='' and SPECTRUM_TYPE_ID =1 and MOLECULE_ID in (select MOLECULE_ID from MOLECULE join SPECTRUM using (MOLECULE_ID ) where SPECTRUM_TYPE_ID =2)");
	      for(Record r:l) {
	    	  StringBuffer sb=new StringBuffer();
		      List<Record> l2 = DBSpectrumPeer.executeQuery("select distinct C_SHIFT.VALUE, SHIFT.VALUE from (select SHIFT.VALUE, ATOM.ATOM_ID from ATOM join SIGNAL_ATOM using (ATOM_ID) join NMR_SIGNAL using (SIGNAL_ID) join SHIFT using (SIGNAL_ID) where SPECTRUM_ID="+r.getValue(1).asInt()+") as C_SHIFT join BOND_ATOM as BA1 using (ATOM_ID) join BOND using (BOND_ID) join BOND_ATOM as BA2 using (BOND_ID) join ATOM on BA2.ATOM_ID=ATOM.ATOM_ID join SIGNAL_ATOM on ATOM.ATOM_ID=SIGNAL_ATOM.ATOM_ID join NMR_SIGNAL using (SIGNAL_ID) join SHIFT using (SIGNAL_ID) join SPECTRUM using (SPECTRUM_ID) where SPECTRUM.SPECTRUM_TYPE_ID=2 order by C_SHIFT.VALUE");
		      for(Record r2:l2) {
		    	  sb.append(r2.getValue(1).asFloat()+";"+r2.getValue(2).asFloat()+"|");
		      }
	    	  DBSpectrumPeer.executeStatement("update SPECTRUM set CORRELATION_SPECFILE=\""+sb+"\" where SPECTRUM_ID="+r.getValue(1).asInt()); 
	      }
      } catch (Exception ex) {
          GeneralUtils.logError(ex,"BuildHose2",null,false);
      }
      this.setResult( Daemon.RESULT_PROCESSING );
      this.setResult( Daemon.RESULT_SUCCESS );
    }
}
