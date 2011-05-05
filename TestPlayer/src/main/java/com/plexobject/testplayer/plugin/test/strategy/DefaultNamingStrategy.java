/* ===========================================================================
 * $RCS$
 * Version: $Id: DefaultNamingStrategy.java,v 1.9 2007/07/11 13:53:47 shahzad Exp $
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
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.util.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

public class DefaultNamingStrategy implements NamingStrategy {
  //
  private static final String TEST_CASE_EXT     = "Test";
  private static final String TEST_SUITE_EXT    = "Suite";
  private static final String TEST_METHOD_PREFIX  = "test";

  private String cachedDefaultTestSuiteName = null;
  private String subPackage = null;

  public boolean isTestInTest() {
    return isTestInTest;
  }

  public void setTestInTest(boolean testInTest) {
    isTestInTest = testInTest;
  }

  public DefaultNamingStrategy(Configuration config) {
    this.config = config;
  }

  public Configuration getConfig() {
    return config;
  }
  public String getSubPackage() {
    return subPackage;
  }

  public void setSubPackage(String subPackage) {
    this.subPackage = subPackage;
  }

  public boolean isTestPackageName(String packageName) {
    boolean returnValue = false;
    String  subName;

    subName = getSubPackage();
    if (subName != null) {
      returnValue = packageName != null;
      returnValue = returnValue && packageName.endsWith(subName);
    }
    return returnValue;
  }

  public boolean isTestClassName(String fullClassName) {
    boolean returnValue = true;
    String  subName;

    subName = getSubPackage();

    if (fullClassName != null) {
      // check option -subpackage
      if (subName != null) {
        // in general all classes in subpackage are considered to be tests
        returnValue &= isTestClassNameInSubpackage(fullClassName, subName);
        // is not considered a test with option -testintest and name does not sound like one
        returnValue &= !isTestInTest() || endsLikeTestClassName(fullClassName);
      } else {
        // check class name ending
        returnValue &= endsLikeTestClassName(fullClassName);
      }
    } else {
      returnValue = false;
    }

    return returnValue;
  }

  private boolean isTestClassNameInSubpackage(String fullClassName, String subName) {
    return ((fullClassName.indexOf("." + subName + ".") != -1) || fullClassName.startsWith(subName + "."));
  }

  private boolean endsLikeTestClassName(String fullClassName) {
    return ((fullClassName.endsWith(TEST_SUITE_EXT)) || (fullClassName.endsWith(TEST_CASE_EXT)));
  }

  public String stripParentPackage(String className) {

    String returnValue = null;
    int  index;
    char   ch;

    index = className.lastIndexOf(".");

    if (index > 0) {
      returnValue = className.substring(index + 1);
    } else {
      returnValue = className;
    }

    return returnValue;
  }

  public String getTestCaseName(String fullClassName) {

    String returnValue   = null;

    returnValue = stripParentPackage(fullClassName);
    returnValue = returnValue + TEST_CASE_EXT;

    return returnValue;
  }

  public String getTestSuiteName(String packageName) {

    String returnValue   = null;

    if (cachedDefaultTestSuiteName == null) {
      cachedDefaultTestSuiteName = config.getProperty(TESTSUITE_CLASS_NAME, "");
    }

    if (cachedDefaultTestSuiteName.length()>0) {
      returnValue = cachedDefaultTestSuiteName;
    } else {
      returnValue = stripParentPackage(packageName);
      returnValue = StringHelper.firstToUpper(returnValue);
      returnValue = returnValue + TEST_SUITE_EXT;
    }
    return returnValue;
  }

  public String getPackageName(String fullClassName) {

    String returnValue = null;
    int  index;

    index = fullClassName.lastIndexOf(".");

    if (index > 0) {
      returnValue = fullClassName.substring(0, index);
    } else {
      returnValue = "";
    }

    return returnValue;
  }

  public String getTestPackageName(String packageName) {
    String returnValue = null;
    if (getSubPackage() != null) {
      returnValue = packageName+"."+getSubPackage();
    } else {
      returnValue = packageName;
    }
    return returnValue;
  }

  public String getFullTestCaseName(String fullClassName) {
    String testPackageName;
    String testCaseName;

    testPackageName = getTestPackageName(getPackageName(fullClassName));
    testCaseName  = getTestCaseName(stripParentPackage(fullClassName));
    return combinePackageClass(testPackageName, testCaseName);
  }

  public String getFullTestSuiteName(String packageName) {
    String testPackageName;
    String testSuiteName;

    testPackageName = getTestPackageName(packageName);
    testSuiteName   = getTestSuiteName(packageName);
    return combinePackageClass(testPackageName, testSuiteName);
  }

  public String combinePackageClass(String packageName, String className) {
    String returnValue;
    if ((packageName != null) && (packageName.length()>0)) {
      returnValue= packageName+"."+className;
    } else {
      returnValue = className;
    }
    return returnValue;
  }

  public String getTestMethodName(String methodName) {
    StringBuilder sb  = new StringBuilder(methodName);
    String first = sb.substring(0, 1);
    sb.replace(0, 1, first.toUpperCase());
    sb.insert(0, TEST_METHOD_PREFIX);
    return sb.toString();
  }

  public String getTestAccessorName(String prefixSet, String prefixGet, String accessorName) {

    StringBuilder sb  = new StringBuilder(accessorName);
    String first = sb.substring(0, 1);
    sb.replace(0, 1, first.toUpperCase());

    // insert "get" or "is" as "Get" or "Is"
    sb.insert(0, prefixGet);
    first = sb.substring(0, 1);
    sb.replace(0, 1, first.toUpperCase());

    // insert "set" as "Set"
    sb.insert(0, prefixSet);
    first = sb.substring(0, 1);
    sb.replace(0, 1, first.toUpperCase());
    sb.insert(0, TEST_METHOD_PREFIX);
    return sb.toString();
  }


  private boolean isTestInTest;
  private Configuration config;
  protected static final Logger logger = Logger.getLogger(DefaultNamingStrategy.class.getName()); 
}

