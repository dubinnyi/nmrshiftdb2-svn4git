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
package org.openscience.nmrshiftdb.modules.screens;

import org.apache.torque.util.Criteria;
import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBGuestbookPeer;

/**
 * The screen class for the guestbook screen, corresponds with Guestbook.vm
 * 
 * @author shk3
 *
 */
public class Guestbook extends VelocityScreen {
	public static final String ENTRIES_KEY = "entries";

	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {
		Criteria crit = new Criteria();
		crit.add(DBGuestbookPeer.VALID, "true");
		crit.addDescendingOrderByColumn(DBGuestbookPeer.DATE);
		context.put(ENTRIES_KEY, DBGuestbookPeer.doSelect(crit));

	}
}
