/* ===========================================================================
 * $RCS$
 * Version: $Id: RegressionDataBuilder.java,v 2.11 2007/08/07 16:50:05 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test.regression;
import com.plexobject.testplayer.plugin.test.*;
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

/**
 * This class runs data file for regression test.
 *
 * @DataProvider
 * public Object[][] algorithms() {
 * return new Object[][] {
 * new Object[] { new Algorithm1() , "input1", "expectedresult1" },
 * new Object[] { new Algorithm2() , "input2", "expectedresult2" },
 * new Object[] { new Algorithm3() , "input3", "expectedresult3" },
 * }
 * }
 * 
 * @Test(dataProvider = "algorithms" )
 * public void verifyAlgorithm( Algorithm a, String input, String result) {
 * assertEquals( result, a.calculate( input));
 * }
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/13/05     SB              created.
 */
public class RegressionDataBuilder extends BaseTestPlugin {
  public static final String TAG_DATA_DIR = "testplayer.regression.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.regression.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.regression.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.regression.file.ext";
  public static final String TAG_MARSHAL_SCHEME = "testplayer.regression.marshalling.scheme";
  /**
   * RegressionDataBuilder - creates regression tests
   * @param context - application context
   */
  public RegressionDataBuilder(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "data"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, ""),
        context.getConfig().getProperty(TAG_FILE_EXT, ".xml"),
        true,
        context.getConfig().getInteger("testplayer.regression.tests.max.depth")
        );
    appendFile = "true".equals(context.getConfig().getProperty("testplayer.append.xml"));
    binaryMarshalling = context.getConfig().getProperty(TAG_MARSHAL_SCHEME, "xml").equals("binary");
  }


  /**
   * initFile initialize output file
   * @param call - method call information
   * @param file - name of file
   */
  protected void initFile(
        MethodEntry call, 
        File file, 
        Object writer, 
        String pkg
        ) throws IOException {
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
    return null;
  }



  /**
   * closeFile close output file
   * @param out - output object
   */
  protected void closeFile(Object writer) throws IOException {
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
    String name = event.call.getCalleeNameWithoutPackage() + "_" + 
        event.call.getMethodNameWithInvocationCount();
    String pkg = event.call.getCalleePackageName();
    File file = newFile(name, pkg);

    if (binaryMarshalling) {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
      out.writeObject(event.call);
      out.close();
    } else {
      BufferedWriter out = new BufferedWriter(new FileWriter(file, appendFile));
      String text = marshal(event.call);
      out.write(text, 0, text.length());
      out.close();
    }

    if (logger.isEnabledFor(Level.INFO)) {
      //logger.info("Wrote regression data for " + event.call + " to " + file);
    }
  }

  protected String marshal(Object value) {
    return StringHelper.replace(
		context.getDefaultMarshaller().marshal(value),
		"\"", "\\\""); 
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



  protected void cleanup() throws Exception {
  }

  private final boolean appendFile;
  private final boolean binaryMarshalling;
}
