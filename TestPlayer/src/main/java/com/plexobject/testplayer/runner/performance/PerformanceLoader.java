/* ===========================================================================
 * $RCS$
 * Version: $Id: PerformanceLoader.java,v 1.7 2006/02/25 20:50:55 shahzad Exp $
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

package com.plexobject.testplayer.runner.performance;
import com.plexobject.testplayer.runner.*;
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
import com.clarkware.junitperf.*;


/**
 * This class loads serialized methods and runs performance tests
 * for timing and load.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 2/13/05      SB              created.
 */
public class PerformanceLoader extends BaseTestLoader {
  public static final String TAG_ELAPSED = "testplayer.test.performance.timeoutSecs";
  public static final String TAG_USERS = "testplayer.test.performance.maxUsers";
  public static final String TAG_THROUGHPUT = "testplayer.test.performance.throughput";
  /**
   * PerformanceLoader - loads serialized methods and runs performance tests
   * for timing and load.
   */
  public PerformanceLoader() {
  }
  /**
   * newTest overrides template method and creates performance runner class.
   * @param file - serialized method.
   */
  public Test newTest(File file) {
    return decorateTest(new PerformanceRunner(file));
  }

  /**
   * decorateTest overrides template method and decorates tests with timing
   * and load test.
   * @param test - test to decorate
   * @return - decorated test.
   */
  public Test decorateTest(Test test) {
    if (throughputResponse) { // throughput under load test
      test = new LoadTest(test, maxUsers);
      test = new TimedTest(test, maxElapsedTime);
    } else { // response time under load test
      test = new TimedTest(test, maxElapsedTime);
      test = new LoadTest(test, maxUsers);
    }
    return test;
  }

  ///////////////////////////////////////////////////////////////////////
  //
  private static int maxElapsedTime = context.getConfig().getInteger(TAG_ELAPSED, 10) * 1000;
  private static int maxUsers = context.getConfig().getInteger(TAG_USERS, 10);
  private static boolean throughputResponse = context.getConfig().getBoolean(TAG_THROUGHPUT);
}
