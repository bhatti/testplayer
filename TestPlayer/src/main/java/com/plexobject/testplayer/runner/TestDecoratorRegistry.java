/* ===========================================================================
 * $RCS$
 * Version: $Id: TestDecoratorRegistry.java,v 1.5 2006/02/25 20:50:55 shahzad Exp $
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
 * This class maintains test decorators for different runners
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 2/13/05      SB              created.
 */
public class TestDecoratorRegistry {
  /**
   * register TestDecorator 
   * @param runner - test runner 
   * @param decorator - test decorator
   */
  public synchronized void register(
        BaseTestRunner runner, 
        TestDecorator decorator) {
    this.registry.put(runner, decorator);
  }


  /**
   * unregister TestDecorator 
   * @param runner - test runner 
   */
  public synchronized void unregister(
        BaseTestRunner runner) {
    this.registry.remove(runner);
  }

  /**
   * find TestDecorator 
   * @param runner - test runner 
   */
  public synchronized TestDecorator find(
        BaseTestRunner runner) {
    return (TestDecorator) this.registry.get(runner);
  }

  /**
   * @return returns singleton instance
   */
  public static TestDecoratorRegistry getInstance() {
    return instance;
  }

  private TestDecoratorRegistry() {
  }

  private transient static TestDecoratorRegistry instance = new TestDecoratorRegistry();
  private transient Map registry = new HashMap();
}
