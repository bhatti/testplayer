/* ===========================================================================
 * $RCS$
 * Version: $Id: DaoException.java,v 1.1 2007/07/13 19:42:22 shahzad Exp $
 * ===========================================================================
 *
 * TestPlayer - an automated test harness builder
 *
 * Copyright (c) 2005-2006 Shahzad Bhatti (bhatti@plexobject.com)
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * The author may be contacted at bhatti@plexobject.com 
 * See http://testplayer.dev.java.net/ for more details.
 *
 */

package com.plexobject.testplayer.dao;



/**
 * DaoException - base exception for database operations.
 *
 * <p><a href="DaoException.java.html"><i>View Source</i></a></p>
 * 
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }


    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
