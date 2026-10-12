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


import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.User;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class DBMixture
    extends org.openscience.nmrshiftdb.om.BaseDBMixture
    implements Persistent
{
	  /**
	   *Gets the DBSpectra associated with this DBMolecule (reviewed as true)
	   *
	   * @return                The spectra
	   * @exception  Exception  Something wrong with reading database
	   */
	  public List<DBSpectrum> selectSpectra() throws Exception {
	    return (selectSpectraByUser(null, true));
	  }


	  /**
	   *Gets the DBSpectra associated with this DBMolecule (reviewed as true) except one given by editSpectrum.
	   *
	   * @param  editSpectrum   The spectrum to be ignored, null if nothing to ignore.
	   * @return                The spectra.
	   * @exception  Exception  Something wrong with reading database.
	   */
	  public List<DBSpectrum> selectSpectra(int editSpectrum) throws Exception {
	    List<DBSpectrum> v = selectSpectraByUser(null, true);
	    for (int i = 0; i < v.size(); i++) {
	      if (((DBSpectrum) v.get(i)).getSpectrumId()==editSpectrum) {
	        v.remove(i);
	        break;
	      }
	    }
	    return (v);
	  }


	  /**
	   *Gets the DBSpectra of a certain user associated with this DBMolecule (reviewed as true, false, rejected)
	   *
	   * @param  user           The user of whom the spectra are to be selected
	   * @param  trueOnly       Select only spectra reviewed as true.
	   * @return                The spectra
	   * @exception  Exception  Something wrong with reading database
	   */
	  public List<DBSpectrum> selectSpectraByUser(User user, boolean trueOnly) throws TorqueException {
	    Criteria crit = new Criteria();
	    crit.add(DBSpectrumPeer.MIXTURE_ID, this.getMixtureId());
	    crit.addAscendingOrderByColumn(DBSpectrumPeer.SPECTRUM_TYPE_ID);
	    if (user != null) {
	      crit.addJoin(DBSpectrumPeer.USER_ID, NmrshiftdbUserPeer.USER_ID);
	      crit.add(NmrshiftdbUserPeer.LOGIN_NAME, user.getUserName());
	    }
	    if (trueOnly) {
	      crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
	    } else {
	      crit.add(DBSpectrumPeer.REVIEW_FLAG, NmrshiftdbConstants.TRUE);
	      Criteria.Criterion criterion = crit.getCriterion(DBSpectrumPeer.REVIEW_FLAG);
	      criterion.or(crit.getNewCriterion(criterion.getTable(), criterion.getColumn(), NmrshiftdbConstants.FALSE, Criteria.EQUAL));
	    }
	    return DBSpectrumPeer.doSelect(crit);
	  }


	  /**
	   *Gets the DBSpectra associated with this DBMolecule and matching a fingerprint
	   *
	   * @param  fingerprint    The fingerprint to search by
	   * @return                The spectra
	   * @exception  Exception  Something wrong with reading database
	   */
	  public List<DBSpectrum> selectSpectra(BigInteger fingerprint) throws TorqueException {
	    Connection dbconn = null;
	    List<DBSpectrum> spectraInDB = new ArrayList<DBSpectrum>();
	    try {
	      dbconn = Torque.getConnection();
	      Statement stmt = dbconn.createStatement();
	      String query = "SELECT SPECTRUM.SPECTRUM_ID FROM SPECTRUM WHERE MIXTURE_ID=" + this.getMixtureId() + " AND fingerprint_compare64(SPECTRUM.FINGERPRINT, " + fingerprint.longValue() + ") = 'Y' AND SPECTRUM.REVIEW_FLAG='true' order by SPECTRUM_TYPE_ID";
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next()) {
	        Criteria crit = new Criteria();
	        crit.add(DBSpectrumPeer.SPECTRUM_ID, rs.getString(1));
	        List<DBSpectrum> v = DBSpectrumPeer.doSelect(crit);
	        spectraInDB.add(v.get(0));
	      }
	      stmt.close();
	    } catch (Exception ex) {
	      throw new TorqueException(ex);
	    } finally {
	      try {
	        Torque.closeConnection(dbconn);
	      } catch (Exception ex) {
	        throw new TorqueException(ex);
	      }
	    }
	    return (spectraInDB);
	  }

	  /**
	   *  Gets a link for imediate display of this molecule.
	   *
	   * @param  req            The current requst.
	   * @return                The easylink value.
	   * @exception  Exception  Description of Exception.
	   */
	  public String getEasylink(HttpServletRequest req) throws Exception {
	    return ("portal/js_pane/P-Results/nmrshiftdbaction/showDetailsFromHome/molNumber/" + this.getMixtureId());
	  }
	  
	  public List<DBMolecule> getDBMolecules() throws Exception{
		  Criteria crit=new Criteria();
		  crit.addJoin(DBMoleculePeer.MOLECULE_ID, DBMixtureDBMoleculePeer.MOLECULE_ID);
		  crit.add(DBMixtureDBMoleculePeer.MIXTURE_ID,this.getMixtureId());
		  return DBMoleculePeer.doSelect(crit);
	  }

	    /**
	     * Returns true if this molecule has a public spectrum.
	     * 
	     * @return
	     * @throws TorqueException
	     * @throws SQLException 
	     */
	    public boolean isPublic() throws TorqueException, SQLException {
	        List<DBSpectrum> spectra = this.getDBSpectrums();
	        for(Object spectrum : spectra){
	            if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.TRUE))
	                return true;
	            else if(((DBSpectrum)spectrum).getReviewFlag().equals(NmrshiftdbConstants.FALSE))
	                return true;
	        }
	        return false;
	    }
}
