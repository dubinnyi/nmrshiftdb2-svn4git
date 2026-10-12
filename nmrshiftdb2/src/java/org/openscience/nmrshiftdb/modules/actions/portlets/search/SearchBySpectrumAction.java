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
package org.openscience.nmrshiftdb.modules.actions.portlets.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.modules.actions.portlets.VelocityPortletAction;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServletUtils;
import org.apache.velocity.context.Context;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.om.DBSpectrumTypePeer;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.NmrshiftdbConstants;
import org.openscience.nmrshiftdb.util.SubmittingData;

import com.workingdogs.village.Record;

/**
 *  This is the portlet showing the search by spectrum form and filling it with the
 *  data from the previous submit
 *
 * @author     Stefan Kuhn
 * @created    11. April 2002
 */
public class SearchBySpectrumAction extends VelocityPortletAction
{

    /**
     * Default behavior for the portlet action. Delegates to buildTypesContext.
     * 
     * @param context The context to fill.
     * @param data The current RunData.
     */
    protected void buildNormalContext(VelocityPortlet portlet, Context context, RunData runData) {
        try {
            HttpServletRequest req = runData.getRequest();
            HttpSession session = req.getSession();
            String relativepath = ServletUtils.expandRelative(
                    runData.getServletConfig(), "/WEB-INF/tmp");
            File file = new File(relativepath + "/uploadjcamp"
                    + req.getSession().getId());
            if (file.exists()) {
                try {
                    BufferedReader rdr = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file),
                                    "ISO-8859-1"));
                    StringBuffer dx = new StringBuffer();
                    String line = "";
                    while ((line = rdr.readLine()) != null) {
                        dx.append(line + "\r\n");
                    }
                    rdr.close();
                    file.delete();
                    SubmittingData subData = new SubmittingData(
                            ServletUtils.expandRelative(
                                    runData.getServletConfig(),
                                    "/WEB-INF/conf/normalizer.xml"));
                    if (runData.getSession().getAttribute(
                            NmrshiftdbConstants.JCAMP_STRICT) != null
                            && ((Boolean) runData.getSession().getAttribute(
                                    NmrshiftdbConstants.JCAMP_STRICT))
                                    .booleanValue() == true)
                        GeneralUtils.parseJcamp(subData, dx.toString(),
                                runData, JCAMPReader.STRICT);
                    else
                        GeneralUtils.parseJcamp(subData, dx.toString(),
                                runData, JCAMPReader.RELAXED);
                    StringBuffer spectrum = new StringBuffer("");
                    for (int i = 0; i < subData.getSignalstable()
                            .getPeakElements().size(); i++) {
                        spectrum.append(subData.getSignalstable()
                                .getPeakElements().get(i).getXValue()
                                + ";"
                                + subData.getSignalstable().getPeakElements()
                                        .get(i).getYValue() + "\n");
                    }
                    session.setAttribute("spectrum", spectrum.toString());
                } catch (JCAMPException ex) {
                    context.put(
                            "message",
                            "Something was wrong with your file: "
                                    + ex.getMessage() + "!");
                } catch (IOException ex) {
                    context.put(
                            "message",
                            "Something was wrong when reading your file: "
                                    + ex.getMessage() + "!");
                } catch (Throwable ex) {
                    context.put(
                            "message",
                            "Something was wrong when reading your file: "
                                    + ex.getMessage() + "!");
                }
            }
            String sql = null;
            if (GeneralUtils.getNmrshiftdbProperty(
                    NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED).equals(
                    "true")) {
                boolean jcamponly = false;
                if (runData.getSession().getAttribute(
                        NmrshiftdbConstants.JCAMPONLY) != null
                        && ((Boolean) runData.getSession().getAttribute(
                                NmrshiftdbConstants.JCAMPONLY)).booleanValue() == true) {
                    jcamponly = true;
                }
                boolean rawonly = false;
                if (runData.getSession().getAttribute(
                        NmrshiftdbConstants.RAWONLY) != null
                        && ((Boolean) runData.getSession().getAttribute(
                                NmrshiftdbConstants.RAWONLY)).booleanValue() == true) {
                    rawonly = true;
                }
                sql = "select min(SPECTRUM_ID) from MOLECULE, SPECTRUM where MOLECULE.MOLECULE_ID=SPECTRUM.MOLECULE_ID";
                if (jcamponly)
                    sql = sql
                            + " and (SPECTRUM_FILE.TYPE='"+DBSpectrum.PDF_TYPE+"' or SPECTRUM_FILE.TYPE='"+DBSpectrum.IMAGE_TYPE+"')";
                if (rawonly)
                    sql = sql
                            + " and (SPECTRUM_FILE.TYPE='"+DBSpectrum.RAW_DATE_TYPE+"')";
                if (runData.getSession().getAttribute(
                        NmrshiftdbConstants.LABGROUP_ONLY) != null
                        && ((Boolean) runData.getSession().getAttribute(
                                NmrshiftdbConstants.LABGROUP_ONLY))
                                .booleanValue() == true) {
                    sql = sql
                            + " join TURBINE_USER using (USER_ID) where LABGROUP_ID>0";
                }
            } else {
                sql = "select min(MOLECULE_ID) from MOLECULE";
            }
            List<Record> l = DBMoleculePeer.executeQuery(sql);
            context.put("firstmolecule", ((Record) l.get(0)).getValue(1)
                    .asString());
            if (session.getAttribute("spectrum") == null) {
                session.setAttribute(
                        "spectrum",
                        "Format ()=optional:\r\nShift(;Intensity)(Multiplicity)\r\nShift(;Intensity)(Multiplicity)");
            }
            if (session.getAttribute("spectrumtype") == null) {
                session.setAttribute("spectrumtype", 1);
            }
            context.put("spectrumtypeselected",
                    session.getAttribute("spectrumtype"));
            Criteria crit = new Criteria();
            crit.add(DBSpectrumTypePeer.DIMENSIONALITY, 1);
            crit.add(DBSpectrumTypePeer.SPECTRUM_TYPE_ID, 100,
                    Criteria.LESS_THAN);
            context.put("spectrumtypes", DBSpectrumTypePeer.doSelect(crit));
            context.put("mode", session.getAttribute("mode"));
            context.put("searchSpectrum", session.getAttribute("spectrum"));
            context.put("numberOfHits", session.getAttribute("numberOfHits"));
            context.put("subortotal",
                    session.getAttribute("subortotal") == null ? "whole"
                            : session.getAttribute("subortotal"));
            context.put("data", runData);
            context.put("constants", new NmrshiftdbConstants());
            context.put("types", DBSpectrumTypePeer.doSelect(new Criteria()));
            context.put("selectedType", session.getAttribute("selectedType"));
            context.put("data", runData);
            context.put(
                    "unassigned",
                    GeneralUtils.getNmrshiftdbProperty(
                            NmrshiftdbConstants.UNASSIGNED_SPECTRA_ALLOWED)
                            .equals("true"));
        } catch (Exception ex) {
            context.put("message", GeneralUtils.logError(ex,
                    "search condition", runData, true));
        }
    }
    
    /**
     * The action for switching to expert mode.
     * 
     * @param runData The current RunData object.
     * @param context The context to fill.
     */
    public void doSwitchmode(RunData runData, Context context){
        HttpServletRequest req = runData.getRequest();
        HttpSession session = req.getSession();
        if (session.getAttribute("mode") == null) {
            session.setAttribute("mode", "expert");
        } else if (session.getAttribute("mode").equals("expert")) {
            session.setAttribute("mode", "normal");
        } else {
            session.setAttribute("mode", "expert");
        }
    }
}
