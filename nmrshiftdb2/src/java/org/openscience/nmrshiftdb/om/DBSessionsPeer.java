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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.mail.MailMessage;
import org.openscience.nmrshiftdb.util.GeneralUtils;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 *
 * @author     shk3
 * @created    September 29, 2003
 */
public class DBSessionsPeer
     extends org.openscience.nmrshiftdb.om.BaseDBSessionsPeer {
  /**
   *  This is a method only used in index.jsp. It only calls doSelect(criteria) and sends an email in case there are connection problems.
   *
   * @param  criteria       Description of Parameter
   * @param  config         Description of Parameter
   * @param  request        Description of Parameter
   * @return                Description of the Returned Value
   * @exception  Exception  Description of Exception
   */
  public static List<DBSessions> doSelect(Criteria criteria, ServletConfig config, HttpServletRequest request)
       throws Exception {
    try {
      return doSelect(criteria);
    } catch (TorqueException ex) {
      MailMessage email = new org.apache.turbine.util.mail.MailMessage(GeneralUtils.getSmtpServer(config), "stefan.kuhn@uni-koeln.de", "stefan.kuhn@uni-koeln.de", "[NMRSHIFTDB-ERROR] Lost Connection", "Seems server " + javax.servlet.http.HttpUtils.getRequestURL(request).toString() + " lost connection to database");
      email.send();
      return (null);
    }
  }
}

