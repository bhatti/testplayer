/* ===========================================================================
 * $RCS$
 * Version: $Id: BaseTestLoader.java,v 1.9 2006/02/25 20:50:54 shahzad Exp $
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
import com.plexobject.testplayer.plugin.test.regression.*;
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
 * This class loads serialized method objects
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 2/13/05      SB              created.
 */
public abstract class BaseTestLoader {
  private class XmlFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name != null && name.endsWith(".xml");
    }
  }
  private class DirFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return new File(dir, name).isDirectory();
    }
  }


  /**
   * BaseTestLoader constructor -
   * @param dir - root directory name where serialized method information 
   * is stored.
   */
  public BaseTestLoader() {
    String dirName = context.getConfig().getProperty(RegressionDataBuilder.TAG_DATA_DIR, "data");
    this.rootDir = context.newFile(dirName);
  }

  /**
   * newSuite defines helper methods to build TestSuite object. It creates
   * tests using template methods and passes name of file that stores
   * serialized method.
   * @return TestSuite with all files under root-directory.
   */
  public Test newSuite() {
    TestSuite suite = new TestSuite(context.getTestAppName());
    try {
      Object[][] dataFiles = loadDataFiles();
      for (int i=0; i<dataFiles.length; i++) {
        File file = (File) dataFiles[i][0];
        suite.addTest(newTest(file));

        //logger.info("**** loading method file " + CodeHelper.readLines(file));
        //logger.info("**** Method " + loadMethod(file));
      }
      if (logger.isEnabledFor(Level.INFO)) {
        logger.info("**** Loaded " + dataFiles.length + " methods");
      }
    } catch (Exception e) {
      logger.error("Failed to load regression data", e);
    }
    return suite;
  }

  /**
   * newTest is a template method, which is defined in derived classes
   * to instantiate Test (runner) class.
   * @param file - file containing serialized method
   */
  public abstract Test newTest(File file); 

  /**
   * decorateTest allows derived classes to decorate unit tests such as
   * timed test, load tests, etc.
   * @param test - JUnit test
   */
  public abstract Test decorateTest(Test test); 


  /**
   * loadMethod - deserialize method from given method and instantiates
   * method entry object.
   * @param file - file containing serialized method object
   * @return - method entry
   */
  public static MethodEntry loadMethod(File file) throws IOException {
    try {
      if (binaryMarshalling) {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        MethodEntry method = (MethodEntry) in.readObject();
        in.close();
        return method;
      } else {   
        BufferedReader in = new BufferedReader(new FileReader(file));
        StringWriter out = new StringWriter((int)file.length());
        int c;
        while ((c=in.read()) != -1) {
          out.write(c);
        }
        in.close();
        MethodEntry method = (MethodEntry) context.getDefaultMarshaller().unmarshal(out.toString());
        out.close();
        return method;
      }
    } catch (ClassNotFoundException e) {
      throw new IOException(e.toString());
    }
  }


  /**
   * finds all .xml files recursively from the file system and stores
   * them in collection.
   *
   * @testng.data-provider name="testRunner" 
   *
   * @return collection of all serialized files under root directory.
   */
  public Object[][] loadDataFiles() throws IOException {
    List files = new ArrayList();
    loadDataFiles(rootDir, files);
    Object[][] dataFiles = new Object[files.size()][1];
    for (int i=0; i<files.size(); i++) {
      dataFiles[i][0] = (File) files.get(i);
    }
    return dataFiles;
  }




  ///////////////////////////////////////////////////////////////////
  // finds all .xml files recursively from the file system and stores
  // them in collection.
  //
  private void loadDataFiles(File dir, List files) throws IOException {
    File[] xmlFiles = dir.listFiles(new XmlFilter());
    int xmlFilesCount = xmlFiles != null ? xmlFiles.length : -1;
    for (int  i=0; xmlFiles != null && i<xmlFiles.length; i++) {
      if (files.indexOf(xmlFiles[i]) == -1) files.add(xmlFiles[i]);
    }
    File[] subdirs = dir.listFiles(new DirFilter());
    int subdirsCount = subdirs != null ? subdirs.length : -1;
    if (logger.isEnabledFor(Level.INFO)) {
       logger.info("Found " + xmlFilesCount + " xml files and " + subdirsCount + " dirs in " + dir.getAbsolutePath());
    }

    for (int  i=0; subdirs != null && i<subdirs.length; i++) {
      loadDataFiles(subdirs[i], files);
    }
  }

  private transient File rootDir;
  protected transient static ApplicationContext context = new ApplicationContext(); 
  protected transient static Logger logger = Logger.getLogger(BaseTestRunner.class.getName());
  private transient static boolean binaryMarshalling = context.getConfig().getProperty(RegressionDataBuilder.TAG_MARSHAL_SCHEME, "xml").equals("binary");
}
