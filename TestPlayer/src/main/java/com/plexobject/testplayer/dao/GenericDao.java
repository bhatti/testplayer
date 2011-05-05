/* ===========================================================================
 * $RCS$
 * Version: $Id: GenericDao.java,v 1.2 2007/07/14 15:23:42 shahzad Exp $
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

import java.util.List;
import java.io.Serializable;

/**
 * An interface shared by all business data access objects.
 *
 */
public interface GenericDao<T, ID extends Serializable> {

    T findById(ID id);

    List<T> findAll();

    T save(T entity);

    /**
     * Affects every managed instance in the current persistence context!
     */
    void clear();
}
