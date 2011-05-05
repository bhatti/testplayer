/* ===========================================================================
 * $RCS$
 * Version: $Id: TestingStrategy.java,v 1.7 2007/07/11 13:53:47 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test.strategy;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.marshal.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;
import java.util.regex.*;
import java.lang.reflect.*;

/**
 * This interface defines test writing strategy
 *
 * @author shahzad bhatti
 *
 * modification history
 * date     who       what
 * 2/5/06     SB        created.
 */
public interface TestingStrategy extends Constants {
  // one test suite per package
  public boolean isTestablePackage(String pkg, NamingStrategy naming);

  public Configuration getConfig(); 

  public Properties getTestSuiteProperties(String[] packages, int indexPackage, NamingStrategy naming, Properties properties);

  public boolean codeTestSuite(String[] packages, int indexPackage, NamingStrategy naming, StringBuilder newCode, Properties properties);

  // one test case per public (non abstract) class
  public boolean isTestableClass(Class klass, NamingStrategy naming);

  public Properties getTestCaseProperties(Class klass, String pkg, NamingStrategy naming, Properties properties);

  public boolean codeTestCase(Class klass, String pkg, NamingStrategy naming, StringBuilder newCode, Properties properties) throws IOException;

  //
  public boolean isValid(String sourceCode);

  public boolean merge(StringBuilder inOutCode, StringBuilder inCode, String fullClassName);
}
