/* ===========================================================================
 * $RCS$
 * Version: $Id: BaseTestRunner.java,v 1.13 2006/08/26 20:58:53 shahzad Exp $
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

package com.plexobject.testplayer.runner;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.visitor.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

import junit.framework.*;
import junit.textui.TestRunner;


/**
 * This class defines base runner
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 2/13/05      SB              created.
 */
public abstract class BaseTestRunner extends TestCase {
  /**
   * BaseTestRunner constructor
   * @file - file containing serialized method entry
   */
  public BaseTestRunner(File file) {
    super("testMethod"); // this is must
    this.file = file;
  }


  /**
   * creates method fixture object to invoke method using reflection
   */
  protected void setUp() throws Exception {
  }


  /**
   * creates method fixture object to invoke method using reflection
   */
  protected void tearDown() throws Exception {
  }

  /**
   *
   * @testng.test data-provider="testRunner" 
   *
   */
  public void verifyMethod(File file) throws Exception {
    this.file = file;
    setUp();
    testMethod();
  }

  /**
   * testMethod defines template test method which is called repeatedly
   * with each unique file.
   */
  public void testMethod() throws Exception {
    MethodEntry method = BaseTestLoader.loadMethod(file);
    if (logger.isEnabledFor(Level.INFO)) {
       logger.info("Loaded method " + method);
    }
    if (method == null) throw new TestSystemException("No method initialized");
    try {
      Object matchReturn = method.invoke();
      if (method.getException() != null) throw new TestSystemException("Method " + this + " was expected to throw " + method.getException());
      if (method.getMethodReturnType().endsWith("[]")) {
         try {
	   junitx.framework.Assert.assertArrayElementsEquals((Object[]) matchReturn, (Object[]) method.getReturnValue());
	 } catch (ClassCastException e) {
	   ReflectHelper.invokeAnyMethodWithArgs(junitx.framework.Assert.class, null, new Object[] {matchReturn, method.getReturnValue()});
	 }
      } else {
         junit.framework.Assert.assertEquals(matchReturn, method.getReturnValue());
      }
 
      //if (logger.isEnabledFor(Level.INFO)) logger.info("Verified method " + method);
    } catch (NoSuchMethodException e) {
      logger.info("Method " + method + " not found -- " + e);
    } catch (Exception e) {
      if (method != null && method.getException() != null && method.getException().getClass().isAssignableFrom(e.getClass())) {
         logger.info("Verified method " + method + " with exception " + e);
         return;
      }
      logger.error("Failed to verify method " + method, e);
      throw e;
    }
  }

  /**
   *
   * @return string representation
   */
  public String toString() {
    return file.getName();
  }

  /**
   *
   * @return test name
   */
  public String getName() {
    return file.getName();
  }

  ///////////////////////////////////////////////////////////////////
  protected transient static ApplicationContext context = new ApplicationContext(); 
  protected transient static Logger logger = Logger.getLogger(BaseTestRunner.class.getName());
  protected transient File file;
}
