/* ===========================================================================
 * $RCS$
 * Version: $Id: ArrayFixtureWrapper.java,v 2.3 2006/02/25 20:50:43 shahzad Exp $
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

package com.plexobject.testplayer.fitnesse;
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.text.*;
import java.util.*;
import fitlibrary.*;
import fit.*;

/**
 * This class wrapps ArrayFixture fixture
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 12/9/05      SB              created.
 */
public class ArrayFixtureWrapper extends ArrayFixture {
  /**
   * ArrayFixtureWrapper constructor
   * @param array - array of objects
   * @param type - type
   */
  public ArrayFixtureWrapper(Object[] array, Class type) {
    set(array); 
    this.type = type;
  }

  /**
   * @return query returns array of objects
   */
  public Object[] query() throws Exception {
    return list.toArray();
  }

  /**
   * @return - returns array of objects
   */
  public List getBackend() throws Exception {
    return list;
  }


  /**
   * @param array - sets array elements
   */
  public void set(Object[] array) {
    for (int i=0; array != null && i<array.length; i++) {
      list.add(array[i]);
    }
  }

  /**
   * @param object - object to add
   */
  public void add(Object object) {
    list.add(object);
  }


  /**
   * @return type of objects
   */
  public Class getTargetClass() {
    return this.type;
  }

  ////////////////////////////////////////////////////////

  private transient List list = new ArrayList();
  private transient Class type;
}
