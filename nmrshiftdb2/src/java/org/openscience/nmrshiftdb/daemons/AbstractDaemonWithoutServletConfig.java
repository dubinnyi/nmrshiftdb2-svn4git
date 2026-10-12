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
package org.openscience.nmrshiftdb.daemons;

//java stuff
import javax.servlet.ServletConfig;

/**
An abstract daemon where run() is supposed to be implemented and not run(ServletConfig).


@author Stefan Kuhn
*/
public abstract class AbstractDaemonWithoutServletConfig extends AbstractDaemon {
    
    /**
    Does the task
    */
    public abstract void run();
    /**
    Does nothing
    */
    public void run(ServletConfig servcon) throws Exception{}
}

