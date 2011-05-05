/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodEvent.java,v 2.3 2006/02/25 20:50:42 shahzad Exp $
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

package com.plexobject.testplayer.events;
import com.plexobject.testplayer.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class stores method event for notification purpose.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/14/05      SB              created.
 */
public class MethodEvent extends EventObject {
  public static final int INIT = 2 << 0;
  public static final int DESTROY = 2 << 1;
  public static final int BEFORE = 2 << 2;
  public static final int AFTER = 2 << 3;


  /**
   * MethodEvent initializes call call upon entry to the method
   * @param source -- source of event
   * @param call -- call call
   * @param args - argument values
   */
  public MethodEvent(Object source, int type, MethodEntry call) {
    super(source);
    this.type = type;
    this.call = call;
  }


  /**
   * @return - returns method event type and call information
   */
  public String toString() {
    return getTypeAsString() + " " + call;
  }

  /**
   * @return - returns type as string
   */
  public String getTypeAsString() {
    switch (type) {
      case INIT:
        return "init";
      case DESTROY:
        return "destroy";
      case BEFORE:
        return "before";
      case AFTER:
        return "after";
      default:
        return null;
    }
  }


  /**
   * @return - returns type - public constant attribute
   */
  public final int type;
  /**
   * @return - method call - public constant attribute
   */
  public final MethodEntry call;
}
