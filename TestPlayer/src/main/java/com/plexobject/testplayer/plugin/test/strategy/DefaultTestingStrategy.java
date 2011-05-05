/* ===========================================================================
 * $RCS$
 * Version: $Id: DefaultTestingStrategy.java,v 1.12 2007/07/11 13:53:47 shahzad Exp $
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
import com.plexobject.testplayer.classloader.*;
import com.plexobject.testplayer.util.*;
import java.util.Properties;
import java.util.LinkedList;
import java.io.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

public class DefaultTestingStrategy implements TestingStrategy {
  protected static final String TESTSUITE_SUITE_METHOD_NAME = "suite";
  protected static final String JUNIT_TEST_CLASS_NAME     = "junit.framework.Test";
  protected static final String ACCESSOR_STARTS_WITH[][] = {{"set", "get"},{"set", "is"}};
  protected static int INDEX_SET = 0;
  protected static int INDEX_GET = 1;

  private static String[] requiredStrings = null;
  public static final String[] MINIMUM_MARKER_SET = {
    VALUE_MARKER_IMPORT_BEGIN,
    VALUE_MARKER_IMPORT_END,
    VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN,
    VALUE_MARKER_EXTENDS_IMPLEMENTS_END,
    VALUE_MARKER_CLASS_BEGIN,
    VALUE_MARKER_CLASS_END
  };

  public DefaultTestingStrategy(Configuration config) {
    this.config = config;
  }
  public Configuration getConfig() {
    return config;
  }

  public boolean isTestablePackage(String pkg, NamingStrategy naming) {
    boolean returnValue = (pkg != null);
    returnValue = returnValue && (naming != null) && !naming.isTestPackageName(pkg);
    return returnValue;
  }

  public boolean isTestableClass(Class klass, NamingStrategy naming) {
    boolean  returnValue = klass != null;
    returnValue = returnValue && !ReflectHelper.isAbstract(klass);
    returnValue = returnValue && !ReflectHelper.isInterface(klass);
    returnValue = returnValue && !ReflectHelper.isProtected(klass);
    returnValue = returnValue && !ReflectHelper.isPrivate(klass);
    returnValue = returnValue && !isInnerClass(klass);
    returnValue = returnValue && ReflectHelper.isPublic(klass);
    returnValue = returnValue && !isATest(klass);
    returnValue = returnValue && (naming != null) && !naming.isTestClassName(klass.getName());
    returnValue = returnValue && !hasSuiteMethod(klass);
    return returnValue;
  }

  public boolean isTestableMethod(Method method) {
    boolean returnValue = method != null;
    returnValue = returnValue &&!ReflectHelper.isAbstract(method);
    returnValue = returnValue &&!ReflectHelper.isProtected(method);
    returnValue = returnValue &&!ReflectHelper.isPrivate(method);
    returnValue = returnValue && ReflectHelper.isPublic(method);
    return returnValue;
  }

  public boolean codeTestSuite(
        String[] packages, 
        int indexPackage, 
        NamingStrategy naming,
        StringBuilder newCode, 
        Properties properties) {
    boolean returnValue = packages != null;
    returnValue = returnValue && (indexPackage >= 0);
    returnValue = returnValue && (indexPackage < packages.length);
    returnValue = returnValue && (naming != null);
    returnValue = returnValue && (newCode != null);
    returnValue = returnValue && (properties != null);

    if (returnValue) {
      returnValue = isTestablePackage(packages[indexPackage], naming);

      if (returnValue) {  // test this package
        Properties addProps = getTestSuiteProperties(packages, indexPackage, naming, properties);
        String template = config.getTemplate(addProps, "testsuite", addProps.getProperty(TEMPLATE_NAME));
        newCode.append(StringHelper.replaceVariables(template, addProps));
      }
    } else {
      logger.error("DefaultTestingStrategy.codeTestSuite() parameter error");
    }

    return returnValue;
  }

  public boolean codeTestCase(
        Class klass, 
        String pkg, 
        NamingStrategy naming,
        StringBuilder newCode, 
        Properties properties) throws IOException {
    boolean  returnValue;
    Properties addProps;
    String   template;

    // check if all parameters are non-null
    returnValue = (klass != null);
    returnValue = returnValue && (pkg != null);
    returnValue = returnValue && (naming != null);
    returnValue = returnValue && (newCode != null);
    returnValue = returnValue && (properties != null);

    if (returnValue) {
      returnValue = isTestableClass(klass, naming);

      if (returnValue) {  // test this class
        addProps = getTestCaseProperties(klass, pkg, naming, properties);
        template = config.getTemplate(addProps, "testcase", addProps.getProperty(TEMPLATE_NAME));

        newCode.append(StringHelper.replaceVariables(template, addProps));
      }           // no else
    } else {
      logger.error("DefaultTestingStrategy.codeTestCase() parameter error");
    }

    return returnValue;
  }

  public boolean codeTest(
        Method[] methods, 
        int index, 
        Class klass, 
        String pkg,
        NamingStrategy naming, 
        StringBuilder newCode, 
        Properties properties) {
    boolean  returnValue;
    Properties addProps;
    String   template;

    // check if all parameters are non-null and index is in range
    returnValue = (methods != null);
    returnValue = returnValue && (index >= 0);
    returnValue = returnValue && (index < methods.length);
    returnValue = returnValue && (klass != null);
    returnValue = returnValue && (pkg != null);
    returnValue = returnValue && (naming != null);
    returnValue = returnValue && (newCode != null);
    returnValue = returnValue && (properties != null);

    if (returnValue) {
      returnValue = isTestableMethod(methods[index]);

      if (returnValue) {  // test this method
        addProps = getTestProperties(
                        methods, 
                        index, 
                        klass, 
                        pkg, 
                        naming, 
                        properties);
        if (addProps != null) {
          // test if not tested already
          template = config.getTemplate(addProps, "testmethod", addProps.getProperty(TEMPLATE_NAME));
          newCode.append(StringHelper.replaceVariables(template, addProps));
        } // no else  // tested already
      }           // no else
    } else {
      logger.error("DefaultTestingStrategy.codeTestCase() parameter error");
    }

    return returnValue;
  }

  public Properties getTestSuiteProperties(
        String[] packages, 
        int indexPackage, 
        NamingStrategy naming,
        Properties properties) {
    Properties returnValue = new Properties(properties);
    returnValue.setProperty(TESTSUITE_PACKAGE_NAME, naming.getTestPackageName(packages[indexPackage]));
    returnValue.setProperty(TESTSUITE_CLASS_NAME, naming.getTestSuiteName(packages[indexPackage]));
    returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
    returnValue.setProperty(TESTSUITE_ADD_TESTSUITES, getTestSuiteAddTestSuites(packages, indexPackage, naming, properties));
    returnValue.setProperty(TESTSUITE_ADD_TESTCASES, getTestSuiteAddTestCases(packages, indexPackage, naming, properties));
    returnValue.setProperty(TESTSUITE_IMPORTS, getTestSuiteImports(packages, indexPackage, naming, properties));
    returnValue.setProperty(PACKAGE_NAME, packages[indexPackage]);
    return returnValue;
  }

  /**
   * Comment on DBC:<br>
   * \@pre (klass != null) && (pkg != null) && (naming != null) && (properties != null) <br>
   * \@post return != null <br>
   *
   * @return new Properties instance with all properties for parameter 'properties'
   * and test case specific properties
   */
  public Properties getTestCaseProperties(
        Class klass, 
        String pkg, 
        NamingStrategy naming,
        Properties properties) {
    Properties returnValue = new Properties(properties);
    returnValue.setProperty(TESTCASE_PACKAGE_NAME, naming.getTestPackageName(pkg));
    returnValue.setProperty(TESTCASE_CLASS_NAME, naming.getTestCaseName(klass.getName()));
    returnValue.setProperty(TESTCASE_INSTANCE_NAME, klass.getName().toLowerCase());
    returnValue.setProperty(TESTCASE_INSTANCE_TYPE, klass.getName());
    returnValue.setProperty(TESTCASE_TESTMETHODS, getTestMethods(klass, pkg, naming, returnValue));
    returnValue.setProperty(TESTCASE_METHOD_UNMATCHED, VALUE_METHOD_UNMATCHED_NAME);
    returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
    returnValue.setProperty(PACKAGE_NAME, pkg);
    returnValue.setProperty(CLASS_NAME, klass.getName());
    return returnValue;
  }

  private String getTestMethods(
        Class klass, 
        String pkg, 
        NamingStrategy naming, 
        Properties properties) {
    StringBuilder sb;
    Method[] methods;

    methods = klass.getMethods();
    sb = new StringBuilder();
    for (int i=0; i< methods.length; i++) {
      if (isTestableMethod(methods[i])) {
        codeTest(methods, i, klass, pkg, naming, sb, properties);
      }
    }
    return sb.toString();
  }

  /**
   * Comment on DBC:<br>
   * \@pre (method != null) && (klass != null) && (pkg != null) && (naming != null) && (properties != null) <br>
   *
   * @return if the method specified by 'index' needs a test, new Properties instance with all properties for parameter 'properties'
   *       and test method specific properties;
   *       null if the method specified by 'index' needs no test
   */
  public Properties getTestProperties(
        Method[] methods, 
        int index, 
        Class klass,
        String pkg, 
        NamingStrategy naming, 
        Properties properties) {
    Properties returnValue = null;
    StringBuilder signature = null;
    Class[] parameters = null;
    returnValue = getTestAccessorProperties(methods, index, klass, pkg, naming, properties);
    // returnValue == null means, no test for this accessor

    if ((returnValue == properties) && (returnValue != null)) {
      // not an accessor

      if (isFirstTestableMethodWithName(methods, index)) {
        returnValue = new Properties(properties);
        returnValue.setProperty(TESTMETHOD_NAME, naming.getTestMethodName(methods[index].getName()));
        returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
        returnValue.setProperty(METHOD_NAME, methods[index].getName());
        signature = new StringBuilder("");
        for (int i=0; i<getNumberOfParameters(methods[index]); i++) {
          parameters = methods[index].getParameterTypes();
          if (i>0) {
            signature.append(", ");
          }
          signature.append(parameters[i].getName());
        }
        returnValue.setProperty(METHOD_SIGNATURE, signature.toString());
      } else {
        // not the first overloaded method (multiple methods sharing one name and one test)
        returnValue = null;
      }
    } // no else
    return returnValue;
  }

  public String getTestSuiteAddTestSuites(
        String[] packages, 
        int indexPackage, 
        NamingStrategy naming, 
        Properties properties) {
    StringBuilder sb;
    String template;
    Properties addProps;
    String[] subPackages;

    sb = new StringBuilder();
    addProps = new Properties(properties);
    template = config.getTemplate(properties, ADD_TESTSUITE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);

    subPackages = getDirectSubPackages(packages, indexPackage);
    for (int i=0; i<subPackages.length; i++) {
      if (isTestablePackage(subPackages[i], naming)) {
        addProps.setProperty(ADD_TESTSUITE_NAME, naming.getTestSuiteName(subPackages[i]));
        addProps.setProperty(TESTSUITE_PACKAGE_NAME, naming.getTestPackageName(subPackages[i]));
        sb.append(StringHelper.replaceVariables(template, addProps));
      }
    }

    return sb.toString();
  }

  public String getTestSuiteAddTestCases(
        String[] packages, 
        int indexPackage, 
        NamingStrategy naming, 
        Properties properties) {
    StringBuilder sb = new StringBuilder();
    Properties addProps = new Properties(properties);
    String template = config.getTemplate(properties, ADD_TESTCASE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);
    Class[] classes  = PackageClassMapper.getInstance().getClassesForPackage(packages[indexPackage]);
    for (int i=0; i<classes.length; i++) {
      if (isTestableClass(classes[i], naming)) {
        addProps.setProperty(ADD_TESTCASE_NAME, naming.getTestCaseName(classes[i].getName()));
        addProps.setProperty(TESTSUITE_PACKAGE_NAME, naming.getTestPackageName(packages[indexPackage]));
        sb.append(StringHelper.replaceVariables(template, addProps));
      }
    }

    return sb.toString();
  }

  public String getTestSuiteImports(
        String[] packages, 
        int indexPackage, 
        NamingStrategy naming, 
        Properties properties) {
    StringBuilder sb;
    String template;
    Properties addProps;
    String[] subPackages;

    sb = new StringBuilder();
    addProps = new Properties(properties);
    template = config.getTemplate(properties, ADD_IMPORT_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);

    subPackages = getDirectSubPackages(packages, indexPackage);
    for (int i=0; i<subPackages.length; i++) {
      if (isTestablePackage(subPackages[i], naming)) {
        addProps.setProperty(ADD_TESTSUITE_NAME, naming.getTestSuiteName(subPackages[i]));
        addProps.setProperty(TESTSUITE_PACKAGE_NAME, naming.getTestPackageName(subPackages[i]));
        sb.append(StringHelper.replaceVariables(template, addProps));
      }
    }

    return sb.toString();
  }

  public boolean isFirstTestableMethodWithName(Method[] methods, int index) {
    boolean returnValue = true;
    String reference;

    reference = methods[index].getName();

    for (int i=0; (i<index) && returnValue; i++) {
      if (reference.equals(methods[i].getName()) && isTestableMethod(methods[i])) {
        returnValue = false;
      }
    }

    return returnValue;
  }

  public int countTestableMethodsWithName(Method[] methods, String methodName) {
    int returnValue =0;

    for (int i=0; (i<methods.length); i++) {
      if (methodName.equals(methods[i].getName()) && isTestableMethod(methods[i])) {
        returnValue++;
      }
    }

    return returnValue;

  }

  /**
   * Builds accessor specific properties if the method specified by 'index' is an accessor method.
   *
   * @return if specfied method is an set accessor, returns properties with all properties from
   *     parameter 'properties' and accessor specific properties;
   *     if specfied method is an get accessor, return null;
   *     if  specfied method is not an accessor, returns parameter 'properties' unchanged
   */
  public Properties getTestAccessorProperties(
        Method[] methods, 
        int index, 
        Class klass,
        String pkg, 
        NamingStrategy naming, 
        Properties properties) {
    Properties returnValue = null;
    String testMethodName;
    int  indexArray;
    String accessedPropertyName;
    String setAccessorName;
    String getAccessorName;
    String testsByType;
    String accessorTypeName;
    Class[] parameters;


    String methodName = methods[index].getName();
    int  indexAccessorPair = getAccessorPairIndex(methods, index);

    if (indexAccessorPair >= 0) {

      if ((methodName.startsWith(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET])) &&
        (isFirstTestableMethodWithName(methods, index))) {
        // testSetGet

        accessedPropertyName = getAccessedPropertyName(methodName, indexAccessorPair);

        if ((accessedPropertyName != null) && (accessedPropertyName.length() > 0))
        {
          testMethodName = naming.getTestAccessorName(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET],
                                ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET],
                                accessedPropertyName);
          setAccessorName = ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET]+accessedPropertyName;
          getAccessorName = ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET]+accessedPropertyName;
          parameters = methods[index].getParameterTypes();
          if ((parameters != null) && (parameters.length == 1)) {
            accessorTypeName = parameters[0].getName();
            indexArray = accessorTypeName.indexOf("[]");
            if (indexArray == -1) {
              testsByType = getAccessorTestsByType(properties, TEMPLATE_ATTRIBUTE_DEFAULT, accessorTypeName);
            } else{
              testsByType = getAccessorTestsByType(properties, TEMPLATE_ATTRIBUTE_ARRAY, accessorTypeName.substring(0, indexArray));
            }
            returnValue = new Properties(properties);
            returnValue.setProperty(ACCESSOR_TESTS, testsByType);
            returnValue.setProperty(ACCESSOR_NAME, testMethodName);
            returnValue.setProperty(ACCESSOR_SET_NAME, setAccessorName);
            returnValue.setProperty(ACCESSOR_GET_NAME, getAccessorName);
            returnValue.setProperty(ACCESSOR_TYPE_NAME, accessorTypeName);
            returnValue.setProperty(TESTMETHOD_NAME, testMethodName);
            returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_ACCESSOR);
            returnValue.setProperty(METHOD_NAME, methods[index].getName());
          }
        } else {
          // method is not an accessor
          returnValue = properties;
        }
      }

      if (methodName.startsWith(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET])) {
        // if method is a get-accessor and there is a set accessor  -> nothing to do here
        returnValue = null;
      }

    } else {
      returnValue = properties;
    }

    return returnValue;
  }


  /**
   * A method is considered an accessor if (i) method name starts with certain prefixes,
   * (ii) prefix is followed by a property name (that is longer than the empyt string ""),
   * (iii) there are methods with this property name for both 'get' and 'set' prefixes,
   * (iv) number of parameters for the get method is 0 and number of parameter for the set method is 1.
   *
   * @return -1 = not both accessors found or not an accessor method,
   *     0 or above = index of prefix in ACCESSOR_STARTS_WITH method of the method specified by 'index'
   */
  public int getAccessorPairIndex(Method[] methods, int index) {
    int returnValue = -1;
    String accessedPropertyName;
    String setAccessorName;
    String getAccessorName;
    boolean foundSet = false;
    boolean foundGet = false;
    boolean exactlyOneParamSet = true;
    boolean exactlyZeroParamGet = true;

    if (isTestableMethod(methods[index])) {
      for (int i = 0; (returnValue == -1) && (i < ACCESSOR_STARTS_WITH.length); i++) {
        accessedPropertyName = getAccessedPropertyName(methods[index].getName(), i);

        if ((accessedPropertyName != null) && (accessedPropertyName.length() > 0 )) {
          setAccessorName = ACCESSOR_STARTS_WITH[i][INDEX_SET]+accessedPropertyName;
          getAccessorName = ACCESSOR_STARTS_WITH[i][INDEX_GET]+accessedPropertyName;

          for (int j=0; (returnValue == -1) && (j<methods.length); j++) {
            if (isTestableMethod(methods[j])) {
              if (getAccessorName.equals(methods[j].getName())) {
                foundGet      |= true;
                exactlyZeroParamGet &= (getNumberOfParameters(methods[j]) == 0);
              } else if (setAccessorName.equals(methods[j].getName())) {
                foundSet      |= true;
                exactlyOneParamSet   &= (getNumberOfParameters(methods[j]) == 1);
              }

            } // no else
          }
          if (foundGet && foundSet && exactlyOneParamSet && exactlyZeroParamGet)
          {
            returnValue = i;
          } // no else
        } // no else, is not an accessor method
      }
    } // no else

    return returnValue;
  }

  /**
   * Comment on DBC:<br>
   * \@pre method != null <br>
   */
  private static int getNumberOfParameters(Method method)
  {
    if (method.getParameterTypes() != null) {
      return method.getParameterTypes().length;
    } else {
      return 0;
    }
  }

  /**
   * @return name of accessed property if 'accessorMethodName' starts with an accessor prefix
   *   specified by 'indexAccessorPair' (see field ACCESSOR_STARTS_WITH),
   *   null in all other cases.
   */
  public String getAccessedPropertyName(String accessorMethodName, int indexAccessorPair) {
    String returnValue = null;
    String prefix;
    if ((accessorMethodName != null) && (accessorMethodName.length()>0)) {
      for (int setOrGet =0; ((returnValue == null) && (setOrGet<ACCESSOR_STARTS_WITH[indexAccessorPair].length)); setOrGet++) {
        prefix = ACCESSOR_STARTS_WITH[indexAccessorPair][setOrGet];
        if (accessorMethodName.startsWith(prefix)) {
          returnValue = accessorMethodName.substring(prefix.length());
        }
      }
    }
    return returnValue;
  }

  public String getAccessorTestsByType(Properties properties, String templateAttribute, String type) {

    String returnValue = null;
    String template;
    Properties addProps;

    if (TEMPLATE_ATTRIBUTE_DEFAULT.equals(templateAttribute)) {
      returnValue = properties.getProperty(ACCESSOR_TESTS + "." + type);
    }

    if (returnValue == null) {
      template = config.getTemplate(properties, ACCESSOR_TESTS, templateAttribute);
      addProps = new Properties(properties);
      addProps.put(ACCESSOR_TYPE_NAME, type);
      returnValue = StringHelper.replaceVariables(template, addProps);
    }

    if (returnValue != null) {
      returnValue = returnValue.trim();
    }
    return returnValue;
  }

  public boolean isInnerClass(Class clazz) {
    boolean returnValue = false;

    if (clazz != null) {
      returnValue = (-1 < clazz.getName().indexOf("."));
    }

    return returnValue;
  }

  public boolean isATest(Class clazz) {
    boolean returnValue = false;

    Class temp;
    String tempName;
    Class interfaces[];

    temp = clazz;
    // iterate over this class and all super classes
    while (!returnValue && (temp != null)) {
      tempName = temp.getName();
      if (tempName.equals(JUNIT_TEST_CLASS_NAME)) {
        returnValue = true;  // Is junit.framework.Test a super class? (true for very old versions of JUnit)
      } else {
        interfaces = temp.getInterfaces();
        // iterate over all interfaces
        for (int i=0; ((interfaces != null) && (i<interfaces.length)); i++) {
          tempName = interfaces[i].getName();
          if (tempName.equals(JUNIT_TEST_CLASS_NAME)) {
            returnValue = true; // Is this class or any super class implementing junit.framework.Test?
          }
        }
      }
      temp = temp.getSuperclass();
    }
    return returnValue;
  }

  public boolean hasSuiteMethod(Class clazz) {

    boolean   returnValue = false;
    Method[] methods   = clazz.getMethods();

    for (int i = 0; !returnValue && (i < methods.length); i++) {
      Method method = methods[i];

      returnValue |= TESTSUITE_SUITE_METHOD_NAME.equals(method.getName()) && ReflectHelper.isStatic(method);
    }

    return returnValue;
  }

  public String[] getDirectSubPackages(
        String[] packages, 
        int indexCurrentPackage) {
    LinkedList list;
    String subStart;
    String tempPackageName;

    list = new LinkedList();
    subStart = packages[indexCurrentPackage] + ".";

    for (int i=0; i< packages.length; i++) {
      tempPackageName = packages[i];
      if ((i != indexCurrentPackage) &&                // is not current
        tempPackageName.startsWith(subStart) &&          // is sub package (may be indirect)
        (-1 == tempPackageName.indexOf(".", subStart.length()))) { // is direct sub package (no further ".")
        list.add(packages[i]);
      }
    }
    return (String[]) list.toArray( new String[0]);
  }

  public boolean isValid(String code) {
    return hasAllRequiredStrings(code) && isValidStructure(code);
  }

  public boolean hasAllRequiredStrings(String code) {
    boolean returnValue = true;

    // create array w/ required strings
    if (requiredStrings == null) {
      requiredStrings = new String[MINIMUM_MARKER_SET.length];

      for (int i = 0; i < MINIMUM_MARKER_SET.length; i++) {
        requiredStrings[i] = MINIMUM_MARKER_SET[i].trim();
      }
    }

    // check if code contains all required markers
    for (int i = 0; i < requiredStrings.length; i++) {
      if (code.indexOf(requiredStrings[i]) == -1) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  public boolean isValidStructure(String code) {

    boolean returnValue = true;
    int   indexBegin;
    int   indexEnd;
    int   indexContentBegin;
    int   indexContentEnd;
    String  markDescription;

    if (code != null) {

      indexBegin = code.indexOf(VALUE_MARKER_BEGIN);
      indexEnd = code.indexOf(VALUE_MARKER_END);

      while (returnValue && (indexBegin < indexEnd) && (indexBegin > -1)) {
        markDescription = code.substring(indexBegin + VALUE_MARKER_BEGIN.length(),
                         code.indexOf("\n", indexBegin));

        indexEnd = indexBegin+VALUE_MARKER_BEGIN.length();
        do {
          indexEnd = code.indexOf(VALUE_MARKER_END + markDescription, indexEnd);
        } while ((indexEnd>0) && (Character.isWhitespace(code.charAt(indexEnd))));

        if (indexEnd > -1) {
          indexContentBegin = code.indexOf("\n", indexBegin+VALUE_MARKER_BEGIN.length());
          indexContentEnd   = code.lastIndexOf("\n", indexEnd);
          if (indexContentBegin < indexContentEnd) {
            returnValue = isValidStructure(code.substring(indexContentBegin, indexContentEnd));
          }
        } else {
          returnValue = false;
        }
        indexBegin = code.indexOf(VALUE_MARKER_BEGIN, indexEnd+1);
        indexEnd = code.indexOf(VALUE_MARKER_END, indexEnd+1);
      }

      returnValue = returnValue && (indexBegin * indexEnd > 0); // existing pairwise, if existing at all
      returnValue = returnValue && ((indexBegin<0) || (indexBegin<indexEnd));
    } else {
      logger.error("DefaultTestingStrategy.isValidStructure() code == null");

      returnValue = false;
    }

    return returnValue;
  }

  /**
   * Merges all markers from inCode into inOutCode. In the end all markers from oldCode
   * will be in newCode as well. If nessesary some new generated default content in
   * newCode gets overwritten. If some markers are not in newCode any more, they will
   * be moved to testVault, a special test method.
   *
   * @param inOutCode points to the in-out StringBuilder with the new code
   * @param inCode holds all markers to be merged into to newCode
   * @param fullClassName is used only for the error message, if anything goes wrong.
   * @return true if successfully merged, false if old code contains no markers.
   */

  public boolean merge(StringBuilder inOutCode, StringBuilder inCode, String fullClassName) {

    boolean    returnValue = true;
    String     newContent;
    String     oldContent;
    String     markDescription;
    String     markContent;
    int      oldIndexLeft;
    int      oldIndexRight;
    int      insertFromIndex;
    int      insertToIndex;
    StringBuilder unmatched;

    if (inOutCode != null) {
      if (inCode != null) {
        oldContent     = inCode.toString();
        unmatched    = new StringBuilder();
        oldIndexLeft   = oldContent.indexOf(VALUE_MARKER_BEGIN, 0);
        oldIndexRight  = oldContent.indexOf("\n", oldIndexLeft) + "\n".length();

        if (isValid(oldContent)) {
          while ((oldIndexRight > -1) && (oldIndexLeft > -1)) {
            markDescription = oldContent.substring(oldIndexLeft + VALUE_MARKER_BEGIN.length(), oldIndexRight).trim();
            oldIndexLeft  = oldIndexRight;
            oldIndexRight   = oldContent.indexOf(VALUE_MARKER_END + markDescription, oldIndexLeft);
            oldIndexRight   = oldContent.lastIndexOf("\n", oldIndexRight) + "\n".length();
            markContent   = oldContent.substring(oldIndexLeft, oldIndexRight);
            newContent    = inOutCode.toString();

            insertFromIndex = 0;
            do {
              insertFromIndex = newContent.indexOf(VALUE_MARKER_BEGIN + markDescription, insertFromIndex);
              if (insertFromIndex > -1) {
                insertFromIndex = insertFromIndex + VALUE_MARKER_BEGIN.length() + markDescription.length();
              }
            } while ((insertFromIndex > -1) && (!Character.isWhitespace(newContent.charAt(insertFromIndex))));

            if (insertFromIndex > -1) {
              // go to end of line
              while ((insertFromIndex - 1 < newContent.length())
                  && (newContent.charAt(insertFromIndex - 1) != '\n')) {
                insertFromIndex++;
              }
              insertToIndex = newContent.indexOf(VALUE_MARKER_END + markDescription, insertFromIndex);
            } else {
              insertToIndex = -1;
            }

            // go back to begin of line
            while ((insertToIndex > 0) && (newContent.charAt(insertToIndex - 1) == ' ')) {
              insertToIndex--;
            }

            if ((insertFromIndex != -1) && (insertToIndex != -1)) {
              if (containsCodeOrComment(markContent)) {
                // replace only, if old marker was empty
                inOutCode.replace(insertFromIndex, insertToIndex, markContent);
              } // no else
            } else {

              // no match found -> append special method if there is some content
              if (containsCodeOrComment(markContent)) {
                unmatched.append(VALUE_MARKER_BEGIN + markDescription);
                unmatched.append("\n");
                unmatched.append(markContent);
                unmatched.append(VALUE_MARKER_END + markDescription);
                unmatched.append("\n");
              } // no else
            }

            oldIndexLeft  = oldContent.indexOf(VALUE_MARKER_BEGIN, oldIndexRight);
            oldIndexRight = oldContent.indexOf("\n", oldIndexLeft) + "\n".length();
          }

          if (unmatched.length() > 0) {

            // there have been unmatched blocks
            newContent  = inOutCode.toString();
            insertToIndex = newContent.lastIndexOf(VALUE_METHOD_UNMATCHED_NAME);

            // go back to begin of line
            while ((insertToIndex > 0) && (newContent.charAt(insertToIndex - 1) != '\n')) {
              insertToIndex--;
            }

            if (insertToIndex != -1) {
              inOutCode.insert(insertToIndex, unmatched.toString());
            } // no else
          }

          if (hasUnmatchedMarkers(inOutCode.toString())) {
            logger.warn("Class " + fullClassName + " contains unmatched tests.");
          }

        } else {
          logger.warn("Class " + fullClassName + " was not generated by TestPlayer. It's not overwritten.\n"+ "Please rename and start TestPlayer again.");
          returnValue = false;
        }
      } // no else
    } else {
      logger.error("DefaultTestingStrategy.merge() inOutCode == null");
    }
    return returnValue;
  }

  public boolean containsCodeOrComment(String markContent) {
    boolean returnValue = false;
    char  ch;

    if ((markContent != null) && (markContent.length() > 0)) {
      for (int i=0; (!returnValue && (i<markContent.length())); i++) {
        ch = markContent.charAt(i);
        returnValue = !Character.isWhitespace(ch);
      }
    } // no else

    return returnValue;
  }

  public boolean hasUnmatchedMarkers(String code) {
    boolean returnValue = false;
    int   beginUnmatched;
    int   endUnmatched;
    int   tempUnmatched;

    beginUnmatched = StringHelper.indexOfTwoPartString(code, VALUE_MARKER_METHOD_BEGIN, VALUE_METHOD_UNMATCHED_NAME_MARKER, 0);
    endUnmatched   = StringHelper.indexOfTwoPartString(code, VALUE_MARKER_METHOD_END, VALUE_METHOD_UNMATCHED_NAME_MARKER, beginUnmatched);
    // TODO better search algorithm for beginUnmatched and endUnmatched
    if ((beginUnmatched != -1) && (endUnmatched != -1) && (endUnmatched > beginUnmatched)) {
      tempUnmatched = beginUnmatched + VALUE_MARKER_METHOD_BEGIN.length()
              + VALUE_METHOD_UNMATCHED_NAME.length();

      while ('\n' != code.charAt(tempUnmatched)) {
        tempUnmatched++;
      }

      while ((tempUnmatched < endUnmatched)
          && (Character.isWhitespace(code.charAt(tempUnmatched)))) {
        tempUnmatched++;
      }

      if (tempUnmatched < endUnmatched) {
        returnValue = true;
      } // no else
    }
    return returnValue;
  }

  private Configuration config;
  protected static final Logger logger = Logger.getLogger(DefaultTestingStrategy.class.getName()); 
  //private static ThreadLocal testSuiteProperties = new ThreadLocal(); 
  //private static ThreadLocal testCaseProperties = new ThreadLocal(); 
  //private static ThreadLocal testProperties = new ThreadLocal(); 
  //private static ThreadLocal testAccessorProperties = new ThreadLocal(); 
}
