/* ===========================================================================
 * $RCS$
 * Version: $Id: BaseTestPlugin.java,v 2.18 2007/07/11 13:53:46 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test;
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

/**
 * This class defines behavior for test generators
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 10/5/05      SB              created.
 */
public abstract class BaseTestPlugin extends InterceptorPluginAdapter {
  private static final String TAG_TEST_FRAMEWORK_JUNIT3 = "junit";
  private static final String TAG_TEST_FRAMEWORK_JUNIT4 = "junit4";
  private static final String TAG_TEST_FRAMEWORK_TESTNG = "testng";
  private static final int TEST_FRAMEWORK_JUNIT3 = 2 << 1;
  private static final int TEST_FRAMEWORK_JUNIT4 = 2 << 2;
  private static final int TEST_FRAMEWORK_TESTNG = 2 << 3;


  public static final String TAG_DELETE_EMPTY_FILE = "testplayer.delete.empty.file";
  public static final String TAG_USE_CONSTRUCTORS = "testplayer.fixtures.use.constructors";
  public static final String TAG_SKIP_METHODS = "testplayer.skip.methods";
  public static final String TAG_TEST_FRAMEWORKS = "testplayer.test.frameworks";


  /**
   * BaseTestPlugin - base class for code generators for unit, integration, etc.
   * @param context - application context
   * @param dirName - directory where the tests will be generated
   * @param namePrefix - prefix that is attached to the output file name
   * @param nameSuffix - suffix that is attached to the output file name
   * @param nameExtension - file extension that is attached to the output file name 
   */
  public BaseTestPlugin(
        ApplicationContext context, 
        String dirName, 
        String namePrefix, 
        String nameSuffix, 
        String nameExtension,
        boolean skipSameMethods,
        int maxDepth) {
    super(context, dirName, namePrefix, nameSuffix, nameExtension); 
    this.skipSameMethods = skipSameMethods;
    this.maxDepth = maxDepth;
    this.useConstructors = context.getConfig().getBoolean(TAG_USE_CONSTRUCTORS);
    this.deleteEmptyTestsFile = context.getConfig().getBoolean(TAG_DELETE_EMPTY_FILE);
    this.blockMethods = Pattern.compile(context.getConfig().getProperty(TAG_SKIP_METHODS, ".*java.lang.Class java.lang.Object.getClass\\(\\).*"));

    String[] tf = StringHelper.split(context.getConfig().getProperty(TAG_TEST_FRAMEWORKS, "junit").toLowerCase(), " \t\r\n,;:");
    for (int i=0; tf != null && i<tf.length; i++) {
      if (TAG_TEST_FRAMEWORK_JUNIT3.equals(tf[i])) {
         testFramework |= TEST_FRAMEWORK_JUNIT3; 
      } else if (TAG_TEST_FRAMEWORK_JUNIT4.equals(tf[i])) {
         testFramework |= TEST_FRAMEWORK_JUNIT4; 
      } else if (TAG_TEST_FRAMEWORK_TESTNG.equals(tf[i])) {
         testFramework |= TEST_FRAMEWORK_TESTNG; 
      }
    }
    if (testFramework == 0) {
      testFramework |= TEST_FRAMEWORK_JUNIT3; 
    }
    logger.debug("will use testframework " + testFramework + ": " + Arrays.asList(tf) + ", java version " + context.getJavaVersion());
  }


  /**
   * newOuput creates PrintWriter object
   * @param call - method call information
   * @param file - name of file
   */
  protected Object newOutput(
        MethodEntry call, 
        File file
        ) throws IOException {
    return new PrintWriter(new FileWriter(file));
  }


  /**
   * closeFile close output file
   * @param out - output object
   */
  protected void closeFile(Object writer) throws IOException {
    PrintWriter out = (PrintWriter) writer;
    out.println("}");
    out.close();

    if (deleteEmptyTestsFile && !doesWriterHasMethods(writer)) {
      File file = getOutputFile(writer); 
      file.delete();
      if (logger.isEnabledFor(Level.INFO)) {
         logger.info("****** Deleting file " + file + " becaues there were no test methods in it");
      }
    }
  }





  /**
   * createFixture - defines method to instantiate fixture which is used
   * to test methods.
   * @param call - method information
   * @param out - output file
   */
  protected String createFixture(
        MethodEntry call,
        PrintWriter out
        ) throws Exception {
    if (call == null) throw new IllegalArgumentException("Null call writer");
    if (out == null) throw new IllegalArgumentException("Null output writer");
    if (call.getCalleeName() != null) {
        return createFixture(
                call.getCalleeName(), 
                call.getCalleeNameWithoutPackage(),
                call.getCallee(),
                call.getMethodNameWithInvocationCount(), 
                out);
    }
    if (call.getCallerName() != null) {
        return createFixture(
                call.getCallerName(), 
                call.getCallerNameWithoutPackage(),
                call.getCaller(),
                call.getMethodNameWithInvocationCount(), 
                out);
    }
    return null;
  }

  /**
   * createFixture - defines method to instantiate fixture which is used
   * to test methods. It also caches fixtures so that when multiple methods
   * are called for the same object whose state does not change then it
   * reuses those fixutres.
   *
   * @param type - type of class
   * @param typeWithoutPackage - type of class without package
   * @param value - state of object
   * @param suffix - fixture suffix
   * @param out - output file
   * @return name of fixture
   */
  protected String createFixture(
        String type,
        String typeWithoutPackage,
        Object value,
        String suffix,
        PrintWriter out
        ) throws Exception {
    if (type == null) throw new IllegalArgumentException("Null type");
    if (typeWithoutPackage == null) throw new IllegalArgumentException("Null type package");
    String line =  "    return (" + type + ") marshaller.unmarshal(\"" + 
        marshal(value) + "\");";

    String fixtureName = getDuplicateFixture(line);
    if (fixtureName != null) return fixtureName; 
    String key = System.identityHashCode(out) + type + suffix;
    if (fixtures.get(key) == null) {
      fixtureName = "new" + typeWithoutPackage + suffix;
      out.println();
      out.println("  // Defining fixture for " + type);
      out.println("  protected " + type + " " + fixtureName + "() throws Exception {");
      out.println(line);
      out.println("  }");
      out.println();
      out.println();
      fixtures.put(key, Boolean.TRUE);
      sameFixtures.put(fixtureName, line);
      return fixtureName;
    }
    return null;
  }



  /**
   * before - receives notification before a method is invoked
   * @param context - application context
   * @param event - method call
   * @param writer - output
   */
  protected void before(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer)
        throws Exception {
  }

  protected void writeTestBodyInit(
        MethodEntry call, 
        String fixtureName, 
        PrintWriter out) 
        throws Exception {
    String method = call.getMethodName();
    String tmethod = call.getMethodNameWithInvocationCount();
    String[] args = call.getArgumentTypes(); 
    Object[] argvalues = call.getArgumentValues(); 

    out.println("    " + call.getCalleeName() + " fx = " + fixtureName + "();");
                
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<args.length; i++) {
      CodeHelper.declare(context, args[i], argvalues[i], sb, "arg" + i, null);
    }
    out.print(sb.toString());

    //////////////////////////////////////////////////////////////////////
    //
    if (call.hasException()) {
       sb = new StringBuilder();
       CodeHelper.declare(context, call.getExceptionType(), call.getException(), sb, "matchException", null);
       out.print(sb.toString());
    } else if (!call.isVoidMethod() && !call.isConstructor()) {
      sb = new StringBuilder();
      CodeHelper.declare(context, call.getMethodReturnType(), call.getReturnValue(), sb, "matchReturn", null);
      out.print(sb.toString());
    }
  }


  protected void writeTestBodyInvoke(
        MethodEntry call, 
        PrintWriter out,
	String printBeforeInvocation,
	String printAfterInvocation) 
        throws Exception {
    String method = call.getMethodName();
    String tmethod = call.getMethodNameWithInvocationCount();
    String[] args = call.getArgumentTypes(); 
    Object[] argvalues = call.getArgumentValues(); 

    if (!call.isVoidMethod() && args.length == 0 && method.startsWith("get")) {
      if (ReflectHelper.hasMethod(call.getCalleeName(), "get", new Class[0])) {
        out.println("    fx.s" + method.substring(1) + "(matchReturn);");
      }
    }

    if (call.hasException()) {
       out.println("    try {");
       out.print("  ");
    }

    if (printBeforeInvocation != null) out.println(printBeforeInvocation);

    if (!call.isVoidMethod()) {
        out.print("    " + call.getMethodReturnType() + " rvalue = ");
    } else {
        out.print("    ");
    }
    if (call.isConstructor()) {
        out.println(" fx;");
    } else {
      out.println("fx." + method + "(");
      for (int i=0; i<args.length; i++) {
        out.print("        arg" + i);
        if (i < args.length-1) out.print(",");
        out.println();
      }
      out.println("      );");
    }
	
    if (printAfterInvocation != null) out.println(printAfterInvocation);


    //////////////////////////////////////////////////////////////////////
    //
    if (call.hasException()) {
           out.println("      fail(\"should have thrown " + call.getException() + "\");");
           out.println("    } catch (" + call.getExceptionType() + " e) {}");
    } else if (!call.isVoidMethod() && !call.isConstructor()) {
        if (call.getMethodReturnType().endsWith("[]")) {
           out.println("    junitx.framework.Assert.assertArrayElementsEquals(matchReturn, rvalue);");
        } else {
           //out.println("    System.out.println(\"Comparing expected '\" + matchReturn + \"', and return value '\" + rvalue + \"'\");");
           out.println("    assertEquals(matchReturn, rvalue);");
        }
    } else {
        if (args.length == 1 && method.startsWith("set")) {
          if (ReflectHelper.hasMethod(call.getCalleeName(), "get", new Class[0])) {
              out.println("    assertEquals(arg0, fx.g" + method.substring(1) + "());");
          }
        }
    }
  }




  protected String indent(int n) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<n; i++) sb.append(' ');
    return sb.toString();
  }


  protected boolean acceptBefore(MethodEvent event) {
    if (event == null || event.call == null) return false;
    //if (event.call.isAbstract() || event.call.isPrivate()) return false;
    if (event.call.isAbstract()) return false; // || !event.call.isPublic()) return false;
    return true;
  }


  protected boolean acceptAfter(MethodEvent event) {
    if (event == null || event.call == null) return false;
    if (event.call.isAbstract()) return false; // || !event.call.isPublic()) return false;


    String pkg = event.call.getCalleePackageName();
    String fileName = getFilename(event.call);
    ////////////////////////////////////////////////////////////////////
    // packages map stores list of all test classes for given package
    ////////////////////////////////////////////////////////////////////
    synchronized (packages) {
      List allBases = (List) packages.get(pkg);
      if (allBases == null) {
          allBases = new ArrayList();
          packages.put(pkg, allBases);
      }
      if (allBases.indexOf(fileName) == -1) allBases.add(fileName);
    }

    //////////////////////////////////////////////////////////////////////
    // packages map stores list of all test classes for given package
    //////////////////////////////////////////////////////////////////////
    String method = event.call.getMethodName(); 

    if (skipSameMethods) {
      synchronized (sameMethods) {
        if (sameMethods.get(event.call.getSignatureValues()) != null) {
           if (logger.isEnabledFor(Level.DEBUG)) {
             //logger.debug("!!!!!!Skipping already processed same method, event " + event + "!!!!!!???????");
           }
           return false;
        }
        sameMethods.put(event.call.getSignatureValues(), Boolean.TRUE);
      }
    }
    int methodCount = 0;
    //
    synchronized (allMethods) {
      List previousMethods = (List) allMethods.get(event.call.getSignature());
      if (previousMethods == null) {
          previousMethods = new ArrayList();
      } 
      previousMethods.add(Boolean.TRUE);
      allMethods.put(event.call.getSignature(), previousMethods);
      methodCount = previousMethods.size();
    }
    event.call.setSameMethodNumber(methodCount);

    if (maxDepth > 0 && event.call.getMethodDepth() > maxDepth) {
       if (logger.isEnabledFor(Level.DEBUG)) {
          logger.debug("!!!!!!Skipping method because depth " + event.call.getMethodDepth() + " is higher than max " + maxDepth + " for event " + event + "!!!!!!");
       }
       return false;
    }

    Matcher m = blockMethods.matcher(event.call.getSignature());
    if (m.find()) {
       if (logger.isEnabledFor(Level.DEBUG)) {
          logger.debug("!!!!!!Skipping blocked method(" + blockMethods.pattern() + ", signature " + event.call.getSignature() + "!!!!!!");
       }
       return false;
    }


    if (event.call.getCalleeName().indexOf("EnhancerByCGLIB") != -1) return false;
    if (event.call.getCallee() == null) return true;
    return context.isPermittedPackage(event.call.getCallee().getClass().getName());
    //return true;
  }

  protected void writeSuites() throws IOException {
    //
    //////////////////////////////////////////////////////////////////////
    Iterator it = packages.keySet().iterator();
    while (it.hasNext()) {
      String pkg = (String) it.next();
      String basepkg = pkg.substring(pkg.lastIndexOf('.') + 1);
      basepkg = basepkg.substring(0, 1).toUpperCase() + basepkg.substring(1);
      List tests = (List) packages.get(pkg);
      File dir = context.newFile(dirName, pkg.replace('.', ApplicationContext.FS.charAt(0)));
      dir.mkdirs();
      File file = new File(dir, basepkg + nameSuffix + "Suite.java");
      PrintWriter out = new PrintWriter(new FileWriter(file));
      out.println("package " + pkg + ";");
      out.println("import junit.framework.*;");
      out.println();
      out.println();
      out.println("/**");
      out.println(" * defines test suite to run all package tests for " + pkg);
      out.println(" * @author - autogenerated by testplayer");
      out.println(" */"); 
      out.println("public class " + basepkg + nameSuffix + "Suite {");
      out.println("  public static TestSuite suite() {");
      out.println("    TestSuite suite = new TestSuite(\"" + pkg + "\");");
      Iterator iit = tests.iterator();
      while (iit.hasNext()) {
        String test = (String) iit.next();
        out.println("    suite.addTestSuite(" + pkg + "." + test + ".class);");
      }
      out.println("    return suite;");
      out.println("  }");
      out.println();
      out.println("  public static void main(String[] args) {");
      out.println("    junit.textui.TestRunner.run(suite());");
      out.println("  }");
      out.println("}");
      out.close();
      file = new File(dir, basepkg + nameSuffix + "TestNG.xml");
      out = new PrintWriter(new FileWriter(file));
      out.println("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\" >");
      out.println("<suite name=\"" + nameSuffix + " Suite\" verbose=\"1\" parallel=\"true\" thread-count=\"1\">");
      out.println("  <test name=\"" + nameSuffix + "\">");
      out.println("    <groups>");
      out.println("      <run>");
      out.println("        <include name=\"unit.*\"/>");
      out.println("        <include name=\"integration.*\"/>");
      out.println("      </run>");
      out.println("    </groups>");
      out.println("    <classes>");
      out.println("      <class name=\"" + pkg + "." + nameSuffix + "Test\"/>");
      out.println("    </classes>");
      out.println("  </test>");
      out.println("</suite>");
      out.close();
    }
  }


  protected boolean doesWriterHasMethods(Object writer) {
    return methodPresenceMap.get(writer) != null;
  }
  protected void setWriterHasMethods(Object writer) {
    methodPresenceMap.put(writer, Boolean.TRUE);
  }

  protected boolean isTestFrameworkJUnit() {
    return (testFramework & TEST_FRAMEWORK_JUNIT3) != 0;
  }
  protected boolean isTestFrameworkJUnit4() {
    return (testFramework & TEST_FRAMEWORK_JUNIT4) != 0;
  }
  protected boolean isTestFrameworkTestNG() {
    return (testFramework & TEST_FRAMEWORK_TESTNG) != 0;
  }

  protected String marshal(Object value) {
    return StringHelper.replace(
		context.getDefaultMarshaller().marshal(value),
		"\"", "\\\\\""); 
  }


  private String getDuplicateFixture(Object value) {
    Iterator it = sameFixtures.keySet().iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      Object next = sameFixtures.get(name);
      if (value == null) {
         if (next == null) return name;
      } else if (value.equals(next)) {
         return name;
      }
    }
    return null;
  }


  private final boolean skipSameMethods;
  private final Map packages = new HashMap();
  private final Map sameMethods = new HashMap();
  private final Map allMethods = new HashMap();
  private final Map sameFixtures = new HashMap();
  private final Map fixtures = new HashMap();
  private final Map methodPresenceMap = new HashMap();
  private final boolean useConstructors;
  private final boolean deleteEmptyTestsFile;
  private final Pattern blockMethods;
  private final int maxDepth;
  private int testFramework;
}

