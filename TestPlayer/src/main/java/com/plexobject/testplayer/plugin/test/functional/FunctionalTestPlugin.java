/* ===========================================================================
 * $RCS$
 * Version: $Id: FunctionalTestPlugin.java,v 1.3 2007/07/11 13:53:46 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test.functional;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.plugin.test.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.tree.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.marshal.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

/**
 * This class writes end-to-end functional tests
 * It only captures high level APIs.
 *
 *                              DESIGN OVERVIEW
 *
 * For each class create a fixture class that extends DoFixture
 *  Within each, class for each high level API:
 *   - check if all arguments and return types are primitive, in that case, 
 *     just create a simple method in the class fixture that takes arguments
 *     and returns method return type. If the method is void then you may
 *     need to verify the state, this may be simple for simple getter, but
 *     not simple for other cases.
 *   - If method takes object, then create SetUp Fixture.
 *   - If method returns array, return ArrayFixture.
 *   - If method returns list return RowFixture.
 * 
 *
 *
 * For each high level method, create a folder with the name that for the
 * package, i.e., com_userdomain_module and creates a file called "content.txt"
 * that has subwikis of all classes in that package, e.g.
 *      * All pass
 *      ^ClassName1
 *      ^ClassName2
 * Then create a folder for each class and create another file called 
 * "content.txt", which stores subwikis for all high level tests for that
 * class, e.g.
 *      * All pass
 *      ^ClassName1_API1
 *      ^ClassName1_API2
 * 
 * Then create a folder for API and create "content.txt"
 * We will use DoFixture pattern to create end-to-end tests, where the high
 * level API will be setup as DoFixture and all setups and sub-calls within
 * the high-level method will use SetupFixture, ActionFixture, ColumnFixture
 * RowFixture, etc.
 * The "content.txt" for each API will start with
 *              | !-APIFixture-! |
 * where APIFixture extends DoFixture, e.g. see ChatStart, Discounts, StartAppl
 *
 *
 *                              FITNESS OVERVIEW
 * Important Built-in Fixtures:
 *      ColumnFixture
 *      CalculateFixture
 *      RowFixture
 *      ParamRowFixture
 *      ArrayFixture
 *      SubsetFixture
 *      TableFixture
 *      ActionFixture
 *      DoFixture
 *      SetUpFixture
 *
 * For ActionFixture that takes primitive arguments and return primitive 
 * values, uncamel method and store the name in italic using double single
 * quote, e.g.
 * public boolean connectUser(String user) 
 * will be written as
 *              |''connect user''|anna|
 * setting up data 
 *      public Fixture setUps() {
 *        return new SetUpDiscounts(..)
 *      
 *      |set ups|
 *      |''future value''|''max balance''|''min purchase''|''discount %''|
 *      |low|0.00|0.00|0|
 *
 * Above the setup fixture extends SetUpFixture, which defines a method
 * public void futureValueMaxBalanceMinPurchaseDiscountPercent( //COPY:ALL
 *             String futureValue, double maxBalance, double minPurchase,
 *             double discountPercent) {
 * ...
 * space
 *      |''calculate with''|low|''future value''|
 *      |''owing''|''purchase''||''discount''|
 *      |0.00|1000.00||0.00|
 *      |1000.00|5000.00||0.00|
 *
 * Above first line calls calculateWithFutureValue(String fv) passing "low"
 * Second line calls double discountOwingOwing(double owing, double purchase)
 * Note, the third column is return value of this method
 *
 * Following will call orderedList() method that returns ArrayFixture
 *      |ordered list|
 *      |''future value''|''max owing''|''min purchase''|''discount %''|
 *      |low|0.00|0.00|0|
 *
 * Following will call subset() that returns SubsetFixture
 *      |subset|
 *      |''future value''|''max owing''|''min purchase''|''discount %''|
 *      |low|0.00|0.00|0|
 *
 *
 * You can also split uncammel method into multiple columns, but it is very
 * hard to do it automatically, e.g.
 * public boolean userCreatesRoom(String user, String room) 
 * can be written as 
 *              |''user''|anna|''creates''|lottr|''room''|
 * Above FitNesse will combine all italics user creates room 
 * In order to return different fixure, define as follows:
 *      public Fixture usersInRoom(String room) {
 *              ...
 *        return new ParamRowFixture(collection, UserCopy.class);
 *
 *              |''users in room''|lotr|
 *              |''name''|
 *              |anna|
 *
 * check in doFixture does the assertion
 *      public int occupantCount(String room)
 *              |check|''occupant count''|lotr|0|
 *
 * ColumnFixture example
 * declare all input primitive types as public
 *
 *      |!-FixtureName-!|
 *      |future value|max balance|min purchase|discount percent|add()|
 *      |low|0.00|0.00|0|true|
 *
 * See chapter 11 and TaxInvoice for TableFixture
 *
 * See chapter 11 and StartSokoban for ImageFixture, e.g.
 * StartSokoban extends from DoFixture and defines a method called 
 * Fixture board() { ... which return ImageFixture
 * It then defines map(int) method
 * Wiki will look like
 *      |!-StartSokoban-!|
 *      |board|
 *      |!img http://files/gameImages/wall.jpg | ...
 *
 * Note: StartApplication defines a method "setup" that returns SetupFixture:
 * |''setup''|
 * |''rental item name''|''count''|''$/hour''|''$/day''|''$/week''|''deposit''|
 * |coffee dispenser|10|1.50|8.20|60.00|0.00|
 * Above a method 
 * void rentalItemNameCountDollarSlashHourDollarSlashDayDollarSlashWeekDit(
 *       String name, int initialHash,
 *       Money hourlyRate, Money dailyRate, Money weeklyRate,
 *       Money bond) throws Exception {
 * 
 * Note: When fixture returns something, e.g. extend your class from 
 * CalculateFixture, e.g.
 * Money refundPaidTimeActualTime(Duration paid, Duration actual) {
 *      |''paid time''|''actual time''||''refund''|
 *      |1 week|1 week||0.00|
 *
 * RowFixture
 *      extend from RowFixture, define 
 * public Object[] query() throws Exception {
 *   ... return collection.toArray()
 *      |''user''|''room''|
 *      |anna|lotr|
 * Above user and room must be defined in the object, which is returned 
 * If you are passing arguments to RowFixture, e.g.
 *      |!-OccupantListInRoom-!|lotr|
 *      |''user''|
 *      |anna|
 * Then use args[0]
 *  public Object[] query() throws Exception {
 *      List occupancies = new ArrayList();
 *      collectOccupants(occupancies,chat.room(args[0]));
 *
 * ActionFixture
 *      extend from fit.Fixture, e.g. ChatServer2
 *
 *      |!-fit.ActionFixture-!|
 *      |start|!-ChatServer2-!|
 *       * Anna connects, creates a new
 *      |!-fit.ActionFixture-!|
 *      |enter|user|anna|
 *      |press|connect|
 *      |enter|room|lotr|
 *       * Luke also enters that room.
 *      |!-fit.ActionFixture-!|
 *      |enter|user|luke|
 *      |press|enter room|
 * ...
 * So there are now no occupants:
 *      |!-OccupantList2-!|                     This is RowFixture
 *      |room|user|
 *      |fit.Summary|
 *
 * Basic fixures, e.g. BuyActions extends fit.Fixture
 *   void price(double currentPrice) {
 *   void buy() { 
 *   public double total() {
 *
 * Setup -----
 * |''setup''|
 * |''name''|''phone''|
 * |my name|3333333333|
 *
 * |''address''|
 * |''street''|''city''|''state''|''zip''|
 * |my street|my city|my state|60103|
 *
 * |''department''|
 * |''code''|''name''|
 * |xxxxx|IS|
 *
 * Class Path
 *      !path fitLibraryFitNesse.jar
 *      !path Examples\BookExampleTables\build
 *      !path Examples\RPS\build
 *
 * properties.xml
 *      <?xml version="1.0"?>
 *      <properties>
 *              <Edit/>
 *              <Files/>
 *              <LastModified>20050604145212</LastModified>
 *
 *
 *              <Properties/>
 *              <RecentChanges/>
 *              <Refactor/>
 *              <Search/>
 *              <Test/>
 *              <Versions/>
 *              <WhereUsed/>
 *              <saveId>1117853529803</saveId>
 *              <ticketId>-7550265836511096246</ticketId>
 *      </properties>
 * 
 * @See ChatStart.java and Chapter10...
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/25/05     SB              created.
 *
 */

public class FunctionalTestPlugin extends BaseTestPlugin {
  private static final int FIXTURE_COLUMN             = 0; 
  private static final int FIXTURE_CALCULATE          = 1; 
  private static final int FIXTURE_COMPUTE            = 2; 
  private static final int FIXTURE_ROW                = 3; 
  private static final int FIXTURE_ARRAY              = 4; 
  private static final int FIXTURE_ACTION             = 5; 
  private static final int FIXTURE_DO                 = 6; 
  private static final int FIXTURE_SETUP              = 7; 
  private static final int FIXTURE_PARAM_ROW          = 8; 
  private static final int FIXTURE_SUBSET             = 9; 
  private static final int FIXTURE_TABLE              = 10; 
  private static final int FIXTURE_IMAGE              = 11; 

  public static final String TAG_DATA_DIR = "testplayer.functional.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.functional.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.functional.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.functional.file.ext";
  public static final String TAG_DEPTH = "testplayer.functional.depth";
  public static final String TAG_FITNESS_HOME = "testplayer.fitnesse.home";

  private static final String[] FIXTURE_TYPES = new String[] {
        "ColumnFixture",
        "CalculateFixture",
        "ComputeFixture",
        "RowFixture",
        "ArrayFixture",
        "ActionFixture",
        "DoFixture",
        "SetUpFixture",
        "ParamRowFixture",
        "SubsetFixture",
        "TableFixture",
        "ImageFixture",
  };

  /////////////////////////////////////////////////////////////////////////
  //
  private static class FixtureType {
    private FixtureType(int type, String returnStatement, String returnType) {
      this.type = type;
      this.parentType = FIXTURE_TYPES[type];
      this.returnStatement = returnStatement;
      this.returnType = returnType;
    }
    int type;
    String parentType;
    String returnStatement;
    String returnType;
  }

  /**
   * FunctionalTestPlugin - creates fitnesse tests
   * @param context - application context
   */
  public FunctionalTestPlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "java/functional"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "Fixture"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".java"),
        true,
        context.getConfig().getInteger(TAG_DEPTH, 5)
        );
    delegateMethodMap = new HashMap();
    resetMethodMap = new HashMap();
    definedBackendInitializer = new HashMap();
    String fitnesseHome = context.getConfig().getProperty(TAG_FITNESS_HOME);
    if (fitnesseHome == null) {
       fitnesseDir = context.newFile("fitnesse");
    } else {
       fitnesseDir = new File(fitnesseHome, "testplayer");
    }
    fitnesseDir.mkdirs();
    fixtureDir = context.newFile("java/functional");
    fitnesseDir.mkdirs();
    wikiMarshaller = new WikiTableMarshaller();
  }


  /**
   * close all fitnesse wiki files and related fixture files
   */
  protected void cleanup() throws Exception {
  }



  /**
   * create DoFixture per class.
   * @param call - method call information
   * @param file - name of file
   */
  protected void initFile(
        MethodEntry call, 
        File file, 
        Object writer, 
        String pkg
        ) throws IOException {
    String fileName = file.getName();
    String fileNameWithoutExt = fileName.substring(0, fileName.indexOf('.'));
    if (!initFile(
        writer, 
        pkg,
        call.getCalleeName(),
        call.getCallee() == null ? call.getCaller() : call.getCallee(),
        fileNameWithoutExt,
        "DoFixture",
        false)) {
      PrintWriter out = (PrintWriter) writer;
      out.println("  // ?????????????? Could not define getBackend() for " + call);
    }
  }


  private boolean initFile(
        Object writer, 
        String pkg,
        String type,
        Object value,
        String fileNameWithoutExt,
        String baseFixture,
        boolean defineFixture
        ) throws IOException {
    PrintWriter out = (PrintWriter) writer;
    if (pkg != null) out.println("package " + pkg + ";");
    //
    out.println("import fitlibrary.*;");
    out.println("import fit.*;");
    out.println("import java.beans.*;");
    out.println("import java.util.*;");
    out.println("import java.io.*;");
    out.println("import com.plexobject.testplayer.fitnesse.*;");
    out.println("import com.plexobject.testplayer.marshal.*;");
    out.println("import com.plexobject.testplayer.*;");
    out.println();
    out.println();
    out.println("/**");
    out.println(" * defines fitnesse fixture " + fileNameWithoutExt + " for functional testing");
    out.println(" * @author - autogenerated by testplayer");
    out.println(" */"); 
    out.println("public class " + fileNameWithoutExt + " extends " + baseFixture + " {");
    out.println("  private " + type + " _backend;");
    if (defineFixture) {
       out.println("  private WikiTableSetupFixture _fixture;");
       out.println("  /**");
       out.println("   * Before calling the method, call setup to initialize backend object");
       out.println("   * @return setup fixture");
       out.println("   */");
       out.println("  public Fixture setup() {");
       out.println("    if (_fixture == null) {");
       out.println("      _fixture = new WikiTableSetupFixture();");
       out.println("    }");
       out.println("    return _fixture;");
       out.println("  }");
       out.println();
       addResetMethod(writer, "    _fixture = null;");
    }

    if ("ArrayFixture".equals(baseFixture) || 
        "RowFixture".equals(baseFixture)) {
      out.println("  /**");
      out.println("   * @return target class ");
      out.println("   */"); 
      out.println("  public Class getTargetClass() {");
      out.println("    return " + type + ".class;");
      out.println("  }"); 
      out.println();
  
      out.println("  /**");
      out.println("   * @return target class ");
      out.println("   */"); 
      out.println("  public Object[] query() {");
      if ("ArrayFixture".equals(baseFixture)) {
        out.println("    return backend;");
      } else {
        out.println("    return (Object[]) getBackend().toArray();");
      }
      out.println("  }"); 
      out.println();
    }

    if (defineFixture || value != null) {
      return defineBackendInitializer(out, type, value, defineFixture);
    } 
    return false;
  }

   
  private boolean definedBackendInitializer(PrintWriter out) {
    return definedBackendInitializer.get(out) != null;
  }


  private boolean defineBackendInitializer(PrintWriter out, String type, Object value, boolean defineFixture) {
    if (definedBackendInitializer.get(out) != null) return false;

    out.println("  /**");
    out.println("   * @return returns backend object");
    out.println("   */"); 
    out.println("  public " + type + " getBackend() throws Exception {");
    out.println("    if (_backend == null) {");
    if (defineFixture) {
       out.println("      if (_fixture == null) {");
       out.println("        throw new TestSystemException(\"Must call setup before accessing backend object\");");
       out.println("      }");
       out.println("      _backend = (" + type + ") _fixture.getObject();");
       addResetMethod(out, "    _backend = null;");
    } else {
       out.println("      IMarshaller marshaller = new " + context.getDefaultMarshaller().getClass().getName() + "();");
       if (value == null) {
         if (CodeHelper.hasDefaultConstructor(type)) { 
           out.println("      _backend = new " + type + "();");
	 } else {
           out.println("      _backend = null; // initialize " + type + " here");
	 }
       } else {
         out.println("      _backend = (" + type + 
		") marshaller.unmarshal(\"" + 
		marshal(value) +
		"\");");
       }
       out.println("      //if (_backend != null) setSystemUnderTest(_backend);");
    }
    out.println("    }");
    out.println("    return _backend;");
    out.println("  }"); 
    out.println();
    definedBackendInitializer.put(out, Boolean.TRUE);
    return true;
  }

  /**
   * closeFile 
   */
  protected void closeFile(Object writer) throws IOException {
    PrintWriter out = (PrintWriter) writer;
    writeResetMethod(out);
    super.closeFile(writer);
  }


  /**
   * after - receives notification after a method is invoked
   * @param context - application context
   * @param event - method call
   * @param writer - output
   */
  protected void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer)
        throws Exception {
    if (event.call.isConstructor()) return;
    if (writer == null) throw new IllegalArgumentException("Null writer");
    //////////////////////////////////////////////////////////////////////
    //
    PrintWriter out = (PrintWriter) writer;
    String method = event.call.getMethodName(); //WithInvocationCount

    if (!definedBackendInitializer(out) && event.call.getCallee() != null) {
        if (!defineBackendInitializer(out, event.call.getCalleeName(), event.call.getCallee(), false)) {
           out.println("  // ===== Could not define getBackend() for " + event.call);
	}
    } else {
      if (!definedBackendInitializer(out)) out.println("  // ********* Could not define getBackend() for " + event.call);
    }

    //
    if (event.call.allPrimitiveArgs() &&
       (event.call.isPrimitiveReturn() || event.call.isVoidMethod())) {
        defineDelegateMethod(event.call, getFixtureType(event.call), out);
    } else {
        writeMethodFixture(event.call);
    }
    //////////////////////////////////////////////////////////////////////
    writeMethodWiki(event.call);
  }


  /**
   * writeSubWiki - creates a wrapper class to store method
   * arguments and return values.
   *
   * @param call - method call
   *
   */
  protected void writeMethodWiki(
        MethodEntry call) throws Exception {
    String type = call.getCalleeName();
    Object value = call.getCallee();
    StringBuilder wiki = new StringBuilder();
    String[] types = call.getArgumentTypes();
    Object[] args = call.getArgumentValues();

    wiki.append("!3 Testing " + call.getMethodName() + context.LF);
    if (call.allPrimitiveArgs() && 
       (call.isPrimitiveReturn() || call.isVoidMethod())) {
      wiki.append(" * all primitive types " + context.LF);
      wiki.append("| !-" + call.getCalleeName() + "Fixture-! |" + context.LF);
    } else {
      wiki.append(" * partial or none primitive types " + context.LF);
      wiki.append("| !-" + getMethodFixtureType(call) + "-! |" + context.LF);
      wiki.append(context.LF);
      wiki.append(" * begin initializing " + type + context.LF);
      appendSetupWiki("setup", value, wiki);
      wiki.append(" * end initializing " + type + context.LF);
      wiki.append(context.LF);
    }

    for (int i=0; args != null && i<args.length; i++) {
      if (!TypeHelper.isPrimitive(types[i]) &&
          !TypeHelper.isPrimitiveWrapper(types[i])) {
        wiki.append(" * begin setting up arg " + i + " of type " + types[i] + context.LF);
        appendSetupWiki("arg" + i + "Fixture", args[i], wiki);
        wiki.append(" * end setting up arg " + i + " of type " + types[i] + context.LF);
        wiki.append(context.LF);
      }
    }


    if (!call.isVoidMethod()) {
      String rtype = call.getMethodReturnType(); 
      if (!TypeHelper.isPrimitive(rtype) &&
          !TypeHelper.isPrimitiveWrapper(rtype)) {
        if (!call.hasException()) {
          wiki.append(" * begin setting up return type " + rtype + context.LF);
          appendSetupWiki("matchReturnFixture", call.getReturnValue(), wiki);
          wiki.append(" * end setting up return type " + rtype + context.LF);
          wiki.append(context.LF);
        }
      }
    }


    wiki.append("!3 invoking " + call.getUniqueMethodName() + context.LF);
    boolean check = false;
    if (call.hasException()) {
       //logger.error("Writing reject for call " + call, call.getException());
       //??????wiki.append("|error");
       wiki.append(" * will throw " + call.getException() + context.LF);
       wiki.append("|reject");
    } else if (!call.isVoidMethod() && !call.isConstructor()) {
      if (call.isPrimitiveReturn() || call.isPrimitiveArrayReturn()) {
         wiki.append("|check");
         check = true;
      }
    }

    wiki.append("|''" + StringHelper.uncamel(call.getMethodName()) + 
        "''|");
    for (int i=0, j=0; args != null && i<args.length; i++) {
      if (TypeHelper.isPrimitive(types[i]) ||
          TypeHelper.isPrimitiveWrapper(types[i])) {
        if (j > 0) wiki.append("arg" + (j+1) + "|");
        wiki.append(args[i] + "|");
        j++;
      }
    }
    if (check && !call.hasException()) {
      wiki.append("|" + call.getReturnValue());
    }
    wiki.append(context.LF);

    //
    File fitnesseFile = getFitnesseFile(call);
    PrintWriter fitnesseWriter = new PrintWriter(new FileWriter(fitnesseFile, true));
    if (logger.isEnabledFor(Level.INFO)) {
      logger.info("Writing wiki file " + fitnesseFile + " previous length " + fitnesseFile.length());
    }
    if (fitnesseFile.length() > 0) {
      fitnesseWriter.println();
    }
    fitnesseWriter.println(wiki);
    fitnesseWriter.close();
    addSubwiki(fitnesseFile.getParentFile());
  }

  /**
   * appendSetupWiki- 
   * This method creates fitnesse wiki tables
   * @param setupTag - name of setup method
   * @param value - object itself
   * @param wiki - wiki buffer
   */
  protected void appendSetupWiki(
        String setupTag,
        Object value,
        StringBuilder wiki) {
    String text = wikiMarshaller.marshal(value);
    StringTokenizer st = new StringTokenizer(text, context.LF);
    String lastMethod = null;
    while (st.hasMoreTokens()) {
      String next = st.nextToken();
      if (lastMethod == null || 
        (next.indexOf("'") != -1 && !next.equals(lastMethod))) {
        wiki.append(context.LF);
        wiki.append("|''" + setupTag + "''|" + context.LF);
        lastMethod = next;
      } else if (next.equals(lastMethod)) {
        continue;
      }
      wiki.append(next + context.LF);
    }  
    wiki.append(context.LF);
  }





  /**
   * writeMethodFixture - creates a wrapper class to store method
   * arguments and return values.
   *
   * @param call - method call
   * @param wiki - wiki buffer
   *
   */
  protected void writeMethodFixture(
        MethodEntry call) throws Exception {
    String type = call.getCalleeName();
    //if (!context.isPermittedPackage(type)) return;
    String pkg = call.getCalleePackageName(); 
    Object value = call.getCallee();
    //////////////////////////////////////////////////////////////////
    //
    String realType = value != null ? value.getClass().getName() : type;

    File dir = new File(fixtureDir, packageToDir(pkg)); 
    dir.mkdirs();

    String className = getMethodFixtureName(call);
    File file = new File(dir, className + ".java");
    if (file.exists() && file.length() > 100) return;

    FixtureType fixtureType = getFixtureType(call); 


    //
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    setOutputFile(out, file);
    if (logger.isEnabledFor(Level.INFO)) {
      logger.info("Writing method fixture in " + file + " for " + call);
    }
    String[] types = call.getArgumentTypes();
    Object[] args = call.getArgumentValues();
    //
    initFile(
        out, 
        pkg,
        call.getCalleeName(),
        call.getCallee(),
        className,
        fixtureType.parentType,
        true);

    ////////////////////////////////////////////////////
    //
    if (!call.isVoidMethod()) {
      String rtype = call.getMethodReturnType(); 
      out.println("  private " + rtype + " _matchReturn;");
      if (!call.isPrimitiveReturn()) {
        out.println("  private WikiTableSetupFixture _matchReturnFixture;");

        out.println("  public Fixture matchReturnFixture() throws Exception {");
        out.println("    if (_matchReturnFixture == null) {");
        out.println("      _matchReturnFixture = new WikiTableSetupFixture();");
        out.println("    }");
        out.println("    return _matchReturnFixture;");
        out.println("  }");
        out.println("  public " + rtype + " matchReturn() throws Exception {");
        out.println("    if (_matchReturnFixture == null) {");
        out.println("      throw new TestSystemException(\"Must call matchReturnFixture before accessing matchReturn\");");
        out.println("    }");
        out.println("    if (_matchReturn == null) {");
        out.println("      _matchReturn = (" + rtype + ") _matchReturnFixture.getObject();");
        out.println("    }");
        out.println("    return _matchReturn;");
        out.println("  }");
        addResetMethod(out, "    _matchReturn = null;");
        addResetMethod(out, "    _matchReturnFixture = null;");
      } else {
        out.println("  public " + rtype + " matchReturn() throws Exception {");
        out.println("    if (_matchReturn == null) {");
        out.println("      IMarshaller marshaller = new " + context.getDefaultMarshaller().getClass().getName() + "();");
        if (call.hasException()) {
          out.println("      _matchReturn = (" + rtype + 
		") marshaller.unmarshal(\"" + 
		marshal(call.getReturnValue()) +
		"\");");
        } else {
          out.println("      throw new " + call.getException().getClass().getName() + "(\"" + call.getException() + "\");");
        }
        out.println("    }");
        out.println("    return _matchReturn;");
        out.println("  }");
      }
    }

    ///////////////////////////////////////////////////////////////////////
    //
    for (int i=0; i<types.length; i++) {
      if (TypeHelper.isPrimitive(types[i]) ||
          TypeHelper.isPrimitiveWrapper(types[i])) {
        //out.println("  //");
        //?????out.println("  private " + types[i] + " _arg" + i + ";");
      } else {
        writeFixtureFactoryMethod(types[i], i, out);
      }
    }
    defineDelegateMethod(call, fixtureType, out);
    writeResetMethod(out);
    out.println("}");
    out.close();
    //definedBackendInitializer.remove(out);
  }








  /**
   * defineDelegateMethod - writes method declaration.
   * @param call - method name
   * @param out - output
   */
  protected void defineDelegateMethod(
        MethodEntry call,
        FixtureType fixtureType,
        PrintWriter out
        ) throws Exception {
    List oldDelegates = (List) delegateMethodMap.get(out);
    if (oldDelegates == null) {
       oldDelegates = new ArrayList();
       delegateMethodMap.put(out, oldDelegates);
    }
    String sig = call.getSignature();
    if (sig.indexOf("abstract") != -1) return;

    if (oldDelegates.indexOf(sig) != -1) return;
    oldDelegates.add(sig);


    //
    if (!definedBackendInitializer(out) && call.getCallee() != null) {
        if (!defineBackendInitializer(out, call.getCalleeName(), call.getCallee(), false)) {
           out.println("  // ===== Could not define getBackend() for " + call);
	}
    } else {
      if (!definedBackendInitializer(out)) out.println("  // ********* Could not define getBackend() for " + call);
    }


     
    String[] types = call.getArgumentTypes();
    String[] args = call.getArgumentTypes(); 
    String method = call.getMethodName();
    setWriterHasMethods(out);
    out.println("  ////////////////////////////////////////////////");
    out.println("  // delegate method for " + method);
    out.println("  // signature " + sig);
    out.println("  //"); 
    //////////////////////////////////////////////////////////////////
    //
    StringBuilder fixtureMethod = new StringBuilder(method);
    for (int i=0, j=0; args != null && i<args.length; i++) {
      if (TypeHelper.isPrimitive(types[i]) ||
          TypeHelper.isPrimitiveWrapper(types[i])) {
        if (j > 0) fixtureMethod.append("Arg" + (j+1));
        j++;
      }
    }

    out.print("  public " + fixtureType.returnType + " " + fixtureMethod + "(");
    for (int i=0, j=0; types != null && i<types.length; i++) {
      if (TypeHelper.isPrimitive(types[i]) ||
          TypeHelper.isPrimitiveWrapper(types[i])) {
        if (j > 0) out.print(", ");
        out.print(types[i] + " arg" + i);
        j++;
      }
    }
    out.println(") throws Exception {");

    //////////////////////////////////////////////////////////////////
    out.println("    try {");
    for (int i=0; args != null && i<args.length; i++) {
      if (!TypeHelper.isPrimitive(types[i]) &&
          !TypeHelper.isPrimitiveWrapper(types[i])) {
        out.println("      " + types[i] + " arg" + i + " = arg" + i + "();");
      }
    }
    out.println("      //" + call);
    out.print("      ");
    if (!call.isVoidMethod()) {
       //if (call.isPrimitiveReturn())  out.print(call.getMethodReturnType());
       out.print(call.getMethodReturnType() + " rvalue = ");
    }
    ///////////////////////////////////////////////////////////////
    //
    if (call.getModifiers().indexOf("static") != -1) {
      out.print(call.getCalleeName() + "." + method + "(");
    } else {
      if (!definedBackendInitializer(out)) out.println("  // ********* no backend");
      out.print("getBackend()." + method + "(");
    }
    for (int i=0; args != null && i<args.length; i++) {
      if (i > 0) out.print(", ");
      out.print("arg" + i);
    }
    out.println(");");


    if (call.hasException()) {
       out.println("      throw new TestSystemException(\"should have thrown " + call.getException() + "\");");
       out.println("    } catch (" + call.getExceptionType() + " e) {");
    } else if (!call.isVoidMethod() && !call.isConstructor()) {
      if (call.isPrimitiveReturn()) {
         StringBuilder sb = new StringBuilder();
         CodeHelper.declare(
                context,
                call.getMethodReturnType(),
                call.getReturnValue(),
                sb, 
                "matchReturn",
                null);
         out.println("  " + sb);
         out.println("      junit.framework.Assert.assertEquals(matchReturn, rvalue);");
      } else {
         if (call.getMethodReturnType().endsWith("[]")) {
           out.println("      junitx.framework.Assert.assertArrayElementsEquals(matchReturn(), rvalue);");
         } else {
           out.println("      junit.framework.Assert.assertEquals(matchReturn(), rvalue);");
         }
      }
    } else {
      if (args.length == 1 && method.startsWith("set")) {
         out.println("      junit.framework.Assert.assertEquals(arg0, getBackend().g" + method.substring(1) + "());");
      }
    }


    out.println("      //------");
    if (!call.hasException() && fixtureType.returnStatement != null && 
        fixtureType.returnStatement.length() > 0) {
      out.println("      " + fixtureType.returnStatement);
    }
    out.println("    } finally {");
    out.println("      reset();");
    out.println("    }");
    out.println("  }");
    out.println();
    out.flush();
  }






  ////////////////////////////////////////////////////////////////////////
  //
  private File getFitnesseFile(MethodEntry call) {
    String name = call.getUniqueMethodName();
    name = "Test" + name.substring(0, 1).toUpperCase() + name.substring(1);
    String dirName = StringHelper.camel(call.getCalleeName(), ".");
    dirName = dirName.substring(0, 1).toUpperCase() + dirName.substring(1);
    File dir = new File(fitnesseDir, dirName.replace('.', ApplicationContext.FS.charAt(0)));
    dir = new File(dir, name);
    dir.mkdirs();
    return new File(dir, "content.txt");
  }


  //
  private void addNonDuplicate(List list, Object object) {
    if (!find(list, object)) list.add(0, object);
  }
 
  private boolean find(List list, Object object) {
    Iterator it = list.iterator();
    while (it.hasNext()) {
      Object next = it.next();
      if (next.equals(object)) return true;
    }
    return false;
  }
  private void addSubwiki(File dir) throws IOException {
    File parent = dir.getParentFile();
    File file = new File(parent, "content.txt");
    List lines = CodeHelper.readLines(file);
    String wiki = "^" + dir.getName();
    if (parent.equals(fitnesseDir)) {
      File base = new File(System.getProperty("user.dir"));
      addNonDuplicate(lines, "!path fitLibraryFitNesse.jar");
      //addNonDuplicate(lines, "!path " + base.getAbsolutePath() + context.getConfig().FS + "target" + context.FS + "classes");
      addNonDuplicate(lines, "!path " + context.getTestAppName() + context.getConfig().FS + "classes");
      String[] jars = new String[] {
        "TestPlayer-" + context.getTestPlayerVersion() + ".jar",
	"commons-lang-2.1.jar",
	"cglib-nodep-2.1_3.jar",
	"xml-writer-0.2.jar",
	"stax-api-1.0.1.jar",
	"stax-1.2.0_rc2-dev.jar",
	"jdom-1.0.jar",
	"xpp3_min-1.1.3.4.O.jar",
	"xom-1.1.jar",
        "dom4j-1.6.1.jar",
        "xpp3_min-1.1.3.4.I.jar",
        "xstream-1.2.jar",
        "log4j-1.2.12.jar",
        "jakarta-oro-2.0.8.jar",
        "junit-4.1.jar",
        "junitx.jar",
        };
      for (int i=0; i<jars.length; i++) {
        addNonDuplicate(lines, "!path " + context.getTestAppName() + context.FS + jars[i]);
      }
    }
    addNonDuplicate(lines, wiki);
    CodeHelper.writeLines(file, lines);


    file = new File(parent, "properties.xml");
    if (!file.exists()) {
      long now = System.currentTimeMillis();
      final String[] PROP_XML = new String[] {
        "<?xml version=\"1.0\"?>",
        "<properties>",
        "  <Edit/>",
        "  <Files/>",
        "  <LastModified>" + now + "</LastModified>",
        "  <Properties/>",
        "  <RecentChanges/>",
        "  <Refactor/>",
        "  <Search/>",
        "  <Suite/>",
        "  <Versions/>",
        "  <WhereUsed/>",
        "  <saveId>" + now + "</saveId>",
        "  <ticketId>" + now + "</ticketId>",
        "</properties>",
      };
      CodeHelper.writeLines(file, PROP_XML);
    }


    if (!parent.equals(fitnesseDir)) {
      addSubwiki(parent);
    }
  }



  private String packageToDir(String pkg) {
    StringBuilder dir = new StringBuilder();
    if (pkg != null) {
      String[] tpkg = StringHelper.split(pkg, ".");
      for (int i=0; i<tpkg.length; i++) {
        dir.append(context.FS + tpkg[i]);
      }
    }
    return dir.toString();
  }  

  private static String getMethodFixtureType(MethodEntry call) {
    String type = call.getCalleeName();
    return call.getCalleeName() + "_" + call.getUniqueMethodName() + "Fixture";
  }

  private static String getMethodFixtureName(MethodEntry call) {
    String type = getMethodFixtureType(call); 
    return type.substring(type.lastIndexOf('.')+1);
  }


  private void writeFixtureFactoryMethod(String type, int arg, PrintWriter out)
        throws IOException {
    if (true || context.isPermittedPackage(type)) {
      /////////////////////////////////////////////////////////////////
      //
      out.println("  /**");
      out.println("   * defines setup fixture for " + type);
      out.println("   * call setup fixture to initialize data before calling this method");
      out.println("   */"); 
      out.println("  private WikiTableSetupFixture _argFixture" + arg + ";");
      out.println("  private " + type + " _arg" + arg + ";");

                
      out.println("  public Fixture arg" + arg + "Fixture() throws Exception {");
      out.println("    if (_argFixture" + arg + " == null) {");
      out.println("      _argFixture" + arg + " = new WikiTableSetupFixture();");
      out.println("    }");
      out.println("    return _argFixture" + arg + ";");
      out.println("  }");
      addResetMethod(out, "    _argFixture" + arg + " = null;");
      addResetMethod(out, "    _arg" + arg + " = null;");
      out.println("  public " + type + " arg" + arg + "() throws Exception {");
      out.println("    if (_arg" + arg + " == null) {");
      out.println("      _arg" + arg + " = (" + type + ") _argFixture" + arg + ".getObject();");
      out.println("    }");
      out.println("    return _arg" + arg + ";");
      out.println("  }");
    } else {
      out.println("  // skipping setup fixture for " + type);
    }
    out.println();
    out.flush();
  }





  private void addResetMethod(Object writer, String line) {
    synchronized (writer) {
      StringBuilder sb = (StringBuilder) resetMethodMap.get(writer);
      if (sb == null) {
        sb = new StringBuilder();
        resetMethodMap.put(writer, sb);
      }
      sb.append(line + context.LF);
    }
  }


  private void writeResetMethod(PrintWriter writer) {
    StringBuilder sb = (StringBuilder) resetMethodMap.get(writer);
    writer.println();
    writer.println("  public void reset() {");
    if (sb != null) {
      writer.print(sb);
    }
    writer.println("  }"); 
    writer.println();
  }



  private static FixtureType getFixtureType(MethodEntry call) {
    String rtype = call.getMethodReturnType();
    if (call.isPrimitiveReturn()) {
      return new FixtureType(
                FIXTURE_DO, //FIXTURE_COLUMN,
                "return rvalue;",
                rtype
                ); 
    } else if (call.isVoidMethod()) {
      return new FixtureType(
                FIXTURE_DO, //FIXTURE_COLUMN,
                "",
                rtype
                ); 
    } else {
      return new FixtureType(
                FIXTURE_DO, //FIXTURE_CALCULATE,
                "return rvalue.equals(matchReturn());",
                "boolean"
                ); 
    }
  }


  /////////////////////////////////////////////////////////////////////
  //

  private Map delegateMethodMap;
  private Map resetMethodMap;
  private File fitnesseDir;
  private File fixtureDir;
  private IMarshaller wikiMarshaller;
  private Map definedBackendInitializer;
}

