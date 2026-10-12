/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2015 Stefan Kuhn
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
package org.openscience.nmrshiftdb;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.cage.Cage;
import com.github.cage.GCage;

public class CaptchaServlet extends HttpServlet {
    private static final long serialVersionUID = 1490947492185481844L;

    private static final Cage cage = new GCage();

    /**
     * Generates a captcha token and stores it in the session.
     * 
     * @param session
     *            where to store the captcha.
     */
    public static void generateToken(HttpSession session) {
      String token = cage.getTokenGenerator().next();

      session.setAttribute("captchaToken", token);
      markTokenUsed(session, false);
    }

    /**
     * Used to retrieve previously stored captcha token from session.
     * 
     * @param session
     *            where the token is possibly stored.
     * @return token or null if there was none
     */
    public static String getToken(HttpSession session) {
      Object val = session.getAttribute("captchaToken");

      return val != null ? val.toString() : null;
    }

    /**
     * Marks token as used/unused for image generation.
     * 
     * @param session
     *            where the token usage flag is possibly stored.
     * @param used
     *            false if the token is not yet used for image generation
     */
    protected static void markTokenUsed(HttpSession session, boolean used) {
      session.setAttribute("captchaTokenUsed", used);
    }

    /**
     * Checks if the token was used/unused for image generation.
     * 
     * @param session
     *            where the token usage flag is possibly stored.
     * @return true if the token was marked as unused in the session
     */
    protected static boolean isTokenUsed(HttpSession session) {
      return !Boolean.FALSE.equals(session.getAttribute("captchaTokenUsed"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      HttpSession session = req.getSession(false);
      generateToken(session);
      String token = session != null ? getToken(session) : null;
      if (token == null || isTokenUsed(session)) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND,
            "Captcha not found.");

        return;
      }

      setResponseHeaders(resp);
      markTokenUsed(session, true);
      cage.draw(token, resp.getOutputStream());
    }

    /**
     * Helper method, disables HTTP caching.
     * 
     * @param resp
     *            response object to be modified
     */
    protected void setResponseHeaders(HttpServletResponse resp) {
      resp.setContentType("image/" + cage.getFormat());
      resp.setHeader("Cache-Control", "no-cache, no-store");
      resp.setHeader("Pragma", "no-cache");
      long time = System.currentTimeMillis();
      resp.setDateHeader("Last-Modified", time);
      resp.setDateHeader("Date", time);
      resp.setDateHeader("Expires", time);
    }
  }
/*public class CaptchaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String sessionid = req.getSession().getId();
        Locale loc = req.getLocale();
        byte[] captcha = getCaptcha(sessionid, loc);
        res.setDateHeader("Expires", 0);
        res.setContentType("image/jpeg");
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        ServletOutputStream outstream = res.getOutputStream();
        outstream.write(captcha);
        outstream.flush();
        outstream.close();
    }

    private synchronized byte[] getCaptcha(String token, Locale loc) throws IOException {

        byte[] captcha = null;
        BufferedImage captchaImage = CaptchaServiceSingleton.getInstance().getImageChallengeForID(token, loc);
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ImageIO.write(captchaImage,"jpg", jpegOutputStream);
        captcha = jpegOutputStream.toByteArray();
        return captcha;
    }        
}*/
