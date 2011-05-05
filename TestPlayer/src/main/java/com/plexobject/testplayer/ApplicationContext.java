/* ===========================================================================
 * $RCS$
 * Version: $Id: ApplicationContext.java,v 2.10 2007/07/11 13:53:45 shahzad Exp $
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

package com.plexobject.testplayer;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.marshal.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.apache.log4j.*;


/**
 * ApplicationContext maintains application wide state
 * @author shahzad bhatti
 *
 * Version: $Id: ApplicationContext.java,v 2.10 2007/07/11 13:53:45 shahzad Exp $
 *
 * modification history
 * date         who             what
 * 9/11/05      SB              created.
 */
public class ApplicationContext { 
  /**
   * ApplicationContext constructor
   */
  public ApplicationContext() {
    try {
      config = new Configuration(); 
      this.basedir = config.getProperty("testplayer.base.dir");
      if (this.basedir == null || this.basedir.equals("~")) this.basedir = System.getProperty("user.home");
      else if (this.basedir.equals(".")) this.basedir = System.getProperty("user.dir");
      this.allowedPackages = Pattern.compile(config.getProperty("testplayer.packages.white.list", ".*"));
      this.disAllowedPackages = Pattern.compile(config.getProperty("testplayer.packages.black.list", ""));
      this.mocksAllowedPackages = Pattern.compile(config.getProperty("testplayer.mocks.white.list", ".*"));
      this.mocksDisAllowedPackages = Pattern.compile(config.getProperty("testplayer.mocks.black.list", ""));
      this.skipConstructorTest = config.getBoolean("testplayer.skip.constructor.tests", true);
      this.addTimestampToOutputDirname = config.getBoolean("testplayer.add.timestamp.output.dirname");
      this.notifyAsynchronously = config.getBoolean("testplayer.process.calls.asynchronously");
      this.tabSize = config.getInteger("testplayer.tab.size", 2);
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<this.tabSize; i++) {
	sb.append(' ');
      }
      this.tab = sb.toString();
      if (addTimestampToOutputDirname) {
         this.basedir = this.basedir + FS + TIMESTAMP;
      }
      if (logger.isEnabledFor(Level.INFO)) {
         logger.info("base dir " + this.basedir);
         logger.info("allowed packages " + this.allowedPackages.pattern());
         logger.info("dis-allowed packages " + this.disAllowedPackages.pattern());
         //list(System.out);
      }
      this.mockLibrary = config.getProperty("testplayer.mock.library", "EasyMock");
      this.marshaller = (IMarshaller) Class.forName(config.getProperty("testplayer.marshalling.strategy.class", "com.plexobject.testplayer.marshal.XStreamMarshaller")).newInstance();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new TestSystemException("******* testplayer failed to initialize *******", e);
    }
  }



  /**
   * @return - return true if given module matches one of the allowed list
   * and does not match disallowed list (blacklist).
   */
  public boolean isPermittedPackage(String pkg) {
    if (pkg == null || pkg.length() == 0) {
      return (allowedPackages == null ||
        allowedPackages.toString().length() == 0) &&
      (disAllowedPackages == null ||
        disAllowedPackages.toString().length() == 0);
    }
    int n = pkg.indexOf('[');
    if (n != -1) pkg = pkg.substring(0, n);

    if (allowedPackages != null && 
        allowedPackages.toString().length() > 0) {
       Matcher m = allowedPackages.matcher(pkg);
       if (!m.find()) {
         return false;
       }
    }
    if (disAllowedPackages != null && 
        disAllowedPackages.toString().length() > 0) {
       Matcher m = disAllowedPackages.matcher(pkg);
       if (m.find()) return false;
    }
    return true;
  }


  /**
   * @return - return true if given module matches list for mock package.
   */
  public boolean useMockForPackage(String pkg) {
    if (pkg == null || pkg.length() == 0) return false;
    if (mocksAllowedPackages.toString().length() > 0) {
       Matcher m = mocksAllowedPackages.matcher(pkg);
       if (!m.find()) return false;
    }
    if (mocksDisAllowedPackages.toString().length() > 0) {
       Matcher m = mocksDisAllowedPackages.matcher(pkg);
       if (m.find()) return false;
    }
    return true;
  }

  /**
   * @return - returns base directory where all output files are stored.
   */
  public String getBaseDir() {
    return this.basedir;
  }

  /**
   * @param name - filename 
   * @return - creates new file in the base directory 
   */
  public File newFile(String name) {
    return new File(basedir + FS + name);
  }


  /**
   * @param dir - dir where file will be created.
   * @param name - filename 
   * @return - creates new file in the given directory 
   */
  public File newFile(String dir, String name) {
    return new File(basedir + FS + dir + FS + name);
  }

  /**
   * generates a unique identifier for each method invocation
   */
  public synchronized long nextMethodEntryId() {
    return callids++;
  }

  /**
   * newThread instantiates new thread given a runnable class. It is 
   * recommended that plugins use this method instead of simply creating 
   * raw threads to best utilize resources. 
   * This method also starts thread if necessary, so caller must not call 
   * start on the thread.
   *
   * @param r - runnable interface
   * @return - new Thread
   */
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    t.start();
    return t;
  }


  /**
   * @param name - property name
   * @return returns File property for given property name
   */
  public File getFile(String name) {
    String value = config.getProperty(name);
    if (value == null) return null;
    return new File(value);
  }

  /**
   * @return - return true if constructors should be tested.
   */
  public boolean isSkipConstructorTest() {
    return this.skipConstructorTest;
  }

  /**
   * @return - return default marshalling class for serializing objects
   */
  public IMarshaller getDefaultMarshaller() {
    return marshaller;
  }


  /**
   * @return - return name of test application being tested.
   */
  public String getTestAppName() {
    return config.getProperty("testplayer.test.appname", "TestPlayerSample");
  }



  /**
   * @return - configuration object that maintains properties
   */
  public Configuration getConfig() {
    return config;
  }
 
  /**
   * @return  Returns the notifyAsynchronously.
   * @uml.property  name="notifyAsynchronously"
   */
  public boolean isNotifyAsynchronously() {
    return this.notifyAsynchronously;
  }

  public void println(String line, StringBuilder sb) {
    sb.append(tab + line + LF);
  }
  public void println(String line, PrintWriter out) {
    out.println(tab + line);
  }

  public void println(String line, int numTabs, StringBuilder sb) {
    sb.append(mtab(numTabs) + line + LF);
  }
  public void println(String line, int numTabs, PrintWriter out) {
    out.println(mtab(numTabs) + line);
  }


  public String mtab(int numTabs) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<numTabs; i++) sb.append(tab);
    return sb.toString();
  }

  public String tab() {
    return tab;
  }

  public float getJavaVersion() {
    if (javaVersion <= 0) {
      String version = System.getProperty("java.version");
      int index = version.indexOf('.'); 
      index = version.indexOf('.', index+1); 
      if (index == -1) index = version.length();
      javaVersion = new Float(version.substring(0, index)).floatValue();
    }
    return javaVersion;
  }


  public String getMockLibrary() {
    return mockLibrary;
  }
  public boolean isjMockLibrary() {
    return "jMock".equalsIgnoreCase(mockLibrary);
  }
  public boolean isEasyMockLibrary() {
    return "EasyMock".equalsIgnoreCase(mockLibrary);
  }
  public static String getTestPlayerVersion() {
    return ApplicationContext.class.getPackage().getSpecificationVersion();
  }


  private transient Configuration config;
  private transient IMarshaller marshaller;
  private transient Pattern allowedPackages;
  private transient Pattern disAllowedPackages;
  private transient Pattern mocksAllowedPackages;
  private transient Pattern mocksDisAllowedPackages;
  private transient String basedir;
  private transient boolean skipConstructorTest;
  private transient boolean addTimestampToOutputDirname;
  private transient boolean notifyAsynchronously;
  private transient int tabSize;
  private transient String tab;
  private transient long callids;
  private transient float javaVersion;
  private transient String mockLibrary;
  private transient Logger logger = Logger.getLogger(ApplicationContext.class.getName());
  private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
  public static final String LF = System.getProperty("line.separator");
  public static final String FS = System.getProperty("file.separator");
}

