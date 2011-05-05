/* ===========================================================================
 * $RCS$
 * Version: $Id: TestSystemException.java,v 2.3 2006/02/25 20:50:41 shahzad Exp $
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

package com.plexobject.testplayer;
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class defines system exception for unexpected errors.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/11/05      SB              created.
 */
public class TestSystemException extends RuntimeException {
  /**
   * Default constructor
   */
  public TestSystemException() {
  }
  /**
   * TestSystemException constructor
   * @param msg - exception message
   */
  public TestSystemException(String msg) {
    super(msg);
  }
  /**
   * TestSystemException constructor
   * @param e - throwable instance
   */
  public TestSystemException(Throwable e) {
    super(e);
  }
  /**
   * TestSystemException constructor
   * @param msg - exception message
   * @param e - throwable instance
   */
  public TestSystemException(String msg, Throwable e) {
    super(msg, e);
  }
}
