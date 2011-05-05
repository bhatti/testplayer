/* ===========================================================================
 * $RCS$
 * Version: $Id: PerformanceRunner.java,v 1.9 2006/02/25 20:50:56 shahzad Exp $
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
 * This class runs performance tests for timing and load.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 2/13/05      SB              created.
 */
public class PerformanceRunner extends BaseTestRunner {
  /**
   * PerformanceLoader - runs performance tests for timing and load.
   * @param file - directory where serialized method data is stored.
   */
  public PerformanceRunner(File file) {
    super(file);
  }

  /**
   * @return - return new suite of performance tests.
   */
  public static Test suite() {
    if (logger.isEnabledFor(Level.INFO)) {
       logger.info("building suite");
    }
    return new PerformanceLoader().newSuite();
  }

  /**
   * testDummy
   */
  public void testDummy() throws Exception {
  }
    
  ///////////////////////////////////////////////////////////////////
  public static void main(String[] args) { 
    if (logger.isEnabledFor(Level.INFO)) {
       logger.info("PerformanceRunner running suites of tests using data driven pattern");
    }
    TestRunner.run(suite());
  }


  ///////////////////////////////////////////////////////////////////
}
