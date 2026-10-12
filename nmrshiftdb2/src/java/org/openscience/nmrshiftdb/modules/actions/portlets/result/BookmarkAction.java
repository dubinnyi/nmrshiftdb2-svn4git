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
package org.openscience.nmrshiftdb.modules.actions.portlets.result;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.apache.jetspeed.om.security.turbine.TurbineUserPeer;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.om.DBBookmark;
import org.openscience.nmrshiftdb.om.DBBookmarkPeer;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.comparator.BookmarksComparator;

/**
 *  This is the portlet handling the display of bookmarks
 *
 * @author     Stefan Kuhn
 * @created    15. May 2002
 */
public class BookmarkAction extends VelocityPortletAction
{
    /**
     * Builds the context for the portlet.
     *
     * @param  runData  The current runData object.
     * @param  portlet  The current portlet.
     * @param  context  The current context.
     */
    @Override
    protected void buildNormalContext(VelocityPortlet portlet, Context context,
            RunData runData) {
        try{
          HttpServletRequest req = runData.getRequest();
          String action = req.getParameter("nmrshiftdbaction");
          if (action == null) {
            action = "";
          }
          if(action.equals("addbookmark")){
            new DBBookmark(Integer.parseInt(((BaseJetspeedUser)runData.getUserFromSession()).getUserId()), Integer.parseInt(req.getParameter("moleculeid"))).save();
          }
          if(action.equals("deletebookmark")){
            DBBookmarkPeer.delete(Integer.parseInt(((BaseJetspeedUser)runData.getUserFromSession()).getUserId()), Integer.parseInt(req.getParameter("moleculeid")));
          }
          context.put("rundata", runData);
          Criteria crit=new Criteria();
          crit.add(DBBookmarkPeer.USER_ID,TurbineUserPeer.getByName(runData.getUserFromSession().getUserName()).getUserId());
          List<DBBookmark> bookmarks = DBBookmarkPeer.doSelect(crit);
          Collections.sort(bookmarks, new BookmarksComparator());
          context.put("bookmarks",bookmarks);
          context.put("moleculepeer",new DBMoleculePeer());
          context.put("data",runData);
        }
        catch (TorqueException ex)
        {
              GeneralUtils.logError(ex,"search keywords",runData,true);
        }
    }
}

