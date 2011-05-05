/* ===========================================================================
 * $RCS$
 * Version: $Id: TestGenerator.java,v 1.6 2007/07/11 13:53:47 shahzad Exp $
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

package com.plexobject.testplayer.tools.testgen;
import com.plexobject.testplayer.plugin.test.strategy.*;
import com.plexobject.testplayer.classloader.*;
import com.plexobject.testplayer.util.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.io.*;
import org.apache.log4j.*;

/**
 * <B>CLASS COMMENTS</B>
 * Class Name: TestGenerator
 * Class Description: 
 *   TestGenerator creates test stubs using reflection
 *
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 * Modification History
 * Initial    Date      Changes
 * SAB      Mar. 12, 2006   Created
*/

public class TestGenerator {
  private String                sourcePath;
  private NamingStrategy        namingStrategy;
  private WritingStrategy       writingStrategy;
  private TestingStrategy       testingStrategy;
  private String                namingStrategyName;
  private String                writingStrategyName;
  private String                testingStrategyName;
  private String                propertyFileName;
  private String                subPackage;
  private boolean               buildAll;
  private boolean               isTestInTest;

  public TestGenerator() {
    //setNamingStrategyName(DefaultNamingStrategy.getName());
    //setWritingStrategyName(DefaultWritingStrategy.getName());
    //setTestingStrategyName(DefaultTestingStrategy.getName());
    //setPropertyFileName(DefaultConfigurableStrategy.DEFAULT_PROPERTY_FILE_NAME);
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(String theSourcePath) {
    sourcePath = theSourcePath;
  }

  public String getPropertyFileName() {
    return propertyFileName;
  }

  public NamingStrategy getNamingStrategy() {
    if (namingStrategy == null) {
      setNamingStrategy((NamingStrategy) createByClassName(getNamingStrategyName()));
    }

    return namingStrategy;
  }

  public void setNamingStrategy(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
    if (namingStrategy != null) {
      //namingStrategy.setPropertyFileName(getPropertyFileName());
      namingStrategy.setSubPackage(getSubPackage());
      namingStrategy.setTestInTest(isTestInTest());
    }
  }

  public WritingStrategy getWritingStrategy() {

    if (writingStrategy == null) {
      setWritingStrategy((WritingStrategy) createByClassName(getWritingStrategyName()));
    }

    return writingStrategy;
  }

  public void setWritingStrategy(WritingStrategy writingStrategy) {
    this.writingStrategy = writingStrategy;
    if (writingStrategy != null) {
      //writingStrategy.setPropertyFileName(getPropertyFileName());
    }
  }

  public TestingStrategy getTestingStrategy() {

    if (testingStrategy == null) {
      setTestingStrategy((TestingStrategy) createByClassName(getTestingStrategyName()));
    }

    return testingStrategy;
  }

  public void setTestingStrategy(TestingStrategy testingStrategy) {
    this.testingStrategy = testingStrategy;
    if (testingStrategy != null) {
      //testingStrategy.setPropertyFileName(getPropertyFileName());
    }
  }

  public String getNamingStrategyName() {
    return namingStrategyName;
  }

  public void setNamingStrategyName(String namingStrategyName) {
    this.namingStrategyName = namingStrategyName;
    setNamingStrategy(null);
  }

  public String getWritingStrategyName() {
    return writingStrategyName;
  }

  public void setWritingStrategyName(String writingStrategyName) {
    this.writingStrategyName = writingStrategyName;
    setWritingStrategy(null);
  }

  public String getTestingStrategyName() {
    return testingStrategyName;
  }

  public void setTestingStrategyName(String testingStrategyName) {
    this.testingStrategyName = testingStrategyName;
    setTestingStrategy(null);
  }

  public boolean isBuildAll() {
    return buildAll;
  }

  public void setBuildAll(boolean buildAll) {
    this.buildAll = buildAll;
  }

  public boolean isTestInTest() {
    return isTestInTest;
  }

  public void setTestInTest(boolean testInTest) {
    isTestInTest = testInTest;
  }

  public String getSubPackage() {
    return subPackage;
  }

  public void setSubPackage(String subPackage) {
    this.subPackage = subPackage;
    getNamingStrategy().setSubPackage(subPackage);
  }

  private Object createByClassName(String className) {
    Object returnValue = null;
    Class  clazz;
    try {
      clazz = Class.forName(className);
      returnValue = clazz.newInstance();
    } catch (Exception e) {
      // not a valid class name
    }
    return returnValue;
  }

  public boolean processPackage(String[] pkgs, int index) throws IOException {
    boolean     returnValue = true;
    StringBuilder  newCode, oldCode;
    String      fullTestSuiteName;
    TestingStrategy testing;
    WritingStrategy writing;
    NamingStrategy  naming;

    testing       = getTestingStrategy();
    writing       = getWritingStrategy();
    naming      = getNamingStrategy();

    if (testing.isTestablePackage(pkgs[index], naming)) {

      fullTestSuiteName = naming.getFullTestSuiteName(pkgs[index]);
      oldCode = writing.loadClassSource(outputRoot, fullTestSuiteName);


      if ((oldCode == null) || testing.isValid(oldCode.toString())) {
        newCode   = new StringBuilder();
        returnValue = testing.codeTestSuite(pkgs, index, naming, newCode, testing.getConfig());
        if (testing.isValid(newCode.toString())) {
          writing.indent(newCode);

          if (testing.merge(newCode, oldCode, fullTestSuiteName)) {
            if (isWritingNeeded(newCode, oldCode)) {
              if (logger.isEnabledFor(Level.INFO)) {
                logger.info("Writing TestSuite "+fullTestSuiteName+".");
              }
              writing.writeClassSource(outputRoot, fullTestSuiteName, newCode);
            } // no else
          } // no else
        } else {
          logger.error("Could not generate TestSuite "+fullTestSuiteName+ " (possible reason: missing or wrong properties).");
        }
      } else {
        if (oldCode != null) {
          logger.warn("TestSuite "+fullTestSuiteName+ " is invalid. It's not overwritten.");
        }
      }
    }

    return returnValue;
  }

  public boolean processClass(Class klass, String pkg) throws IOException {

    boolean returnValue = true;
    StringBuilder  oldCode, newCode;
    TestingStrategy testing = getTestingStrategy();
    WritingStrategy writing = getWritingStrategy();
    NamingStrategy naming  = getNamingStrategy();

    String fullClassName  = klass.getName();
    String fullTestCaseName = naming.getFullTestCaseName(fullClassName);

    if (testing.isTestableClass(klass, naming)) {

      // generate TestCase only if it does not exist or is older than application class.
      if (isGenerationNeeded(fullClassName, fullTestCaseName)) {

        oldCode = writing.loadClassSource(outputRoot, fullTestCaseName);

        if ((oldCode == null) ||testing.isValid(oldCode.toString())) {
          newCode   = new StringBuilder();
          returnValue = testing.codeTestCase(klass, pkg, getNamingStrategy(), newCode,
                             testing.getConfig());
          if (testing.isValid(newCode.toString())) {
            writing.indent(newCode);

            if (testing.merge(newCode, oldCode, fullTestCaseName)) {
              if (isWritingNeeded(newCode, oldCode)) {
                if (logger.isEnabledFor(Level.INFO)) {
                  logger.info("Writing TestCase "+fullTestCaseName+".");
                }
                writing.writeClassSource(outputRoot, fullTestCaseName, newCode);
              } else {
                if (logger.isEnabledFor(Level.INFO)) {
                  logger.info("TestCase "+fullTestCaseName+ " did not change but "+fullClassName+" did.");
                }
              }

            } // no else
          } else {
            logger.error("Could not generate TestCase "+fullTestCaseName+ " (possible reason: missing or wrong properties).");
          }
        } else {
          if (oldCode != null) {
            logger.warn("TestCase "+fullTestCaseName+ " is invalid. It's not overwritten.");
          }
        }
      } else {

        // Do not regenerate TestCase.
        returnValue = true;
      }
    } else {

      // Do not generate test case.
      returnValue = true;
    }

    return returnValue;
  }

  /**
   * Checks if file of application class is modified later than TestCase file.
   */
  public boolean isGenerationNeeded(String fullClassName, String fullTestCaseName) {

    boolean returnValue;

    returnValue = isBuildAll();
    returnValue = returnValue || !getWritingStrategy().isExistingAndNewer(
                                        outputRoot,
                                        fullTestCaseName,
                                        getSourcePath(),
                                        fullClassName);
    return returnValue;
  }

  public boolean isWritingNeeded(StringBuilder newCode, StringBuilder oldCode) {

    boolean returnValue;

    returnValue = isBuildAll();
    returnValue = returnValue || (oldCode == null);
    returnValue = returnValue || ((newCode != null) && (oldCode != null) && (!newCode.toString().equals(oldCode.toString())));
    return returnValue;
  }

  public boolean execute() throws IOException {
    boolean returnValue = true;
    Class[] classes = PackageClassMapper.getInstance().getAllClasses();
    for (int i = 0; i < classes.length; i++) {
      returnValue = returnValue && processClass(classes[i], null);
    }

    String[] packages = PackageClassMapper.getInstance().getPackages();
    for (int i = 0; i < packages.length; i++) {
      classes = PackageClassMapper.getInstance().getClassesForPackage(packages[i]);
      for (int j = 0; j < classes.length; j++) {
        returnValue = returnValue && processClass(classes[j], packages[i]);
      }
      returnValue = returnValue && processPackage(packages, i);
    }
    return returnValue;
  }


  public static void main(String[] args) throws Exception {
    new TestGenerator().execute();
  }

  private String outputRoot;
  private static final Logger logger = Logger.getLogger(TestGenerator.class.getName());
}
