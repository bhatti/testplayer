/* ===========================================================================
 * $RCS$
 * Version: $Id: BaseDotPlugin.java,v 2.5 2006/02/27 21:50:42 shahzad Exp $
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

package com.plexobject.testplayer.plugin.dot;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.tree.*;
import com.plexobject.testplayer.visitor.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

/**
 * This class displays package dependencies graphically using DOT package
 * @see http://www.graphviz.org/
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/12/05     SB              created.
 */
public abstract class BaseDotPlugin extends InterceptorPluginAdapter {
  public static final String TAG_DELETE_EMPTY_FILE = "testplayer.delete.empty.file";
  public static final String TAG_DOT_EXEC = "testplayer.dot.exec";
  /**
   * BaseDotPlugin - constructor
   * @param context - application context
   * @param context - application context
   * @param dirName - directory where the tests will be generated
   * @param namePrefix - prefix that is attached to the output file name
   * @param nameSuffix - suffix that is attached to the output file name
   * @param nameExtension - file extension that is attached to the output file name
   */
  public BaseDotPlugin(
        ApplicationContext context, 
        String dirName, 
        String namePrefix, 
        String nameSuffix, 
        String nameExtension) {
    super(context, dirName, namePrefix, nameSuffix, nameExtension); 
    deleteEmptyFile = context.getConfig().getBoolean(TAG_DELETE_EMPTY_FILE);
  }


  /**
   * newOutput creates PrintWriter object
   * @param call - method call information
   * @param file - name of file
   */
  protected Object newOutput(
        MethodEntry call, 
        File file) throws IOException {
    return new PrintWriter(new FileWriter(file));
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
        String pkg) throws IOException {
    setOutputFile(writer, file); 
    PrintWriter out = (PrintWriter) writer;
    out.println("digraph G {");
    //out.println("  orientation=\"landscape\";");
    //out.println("  size=\"10,8\";");
    out.println("  ratio=\"fill\";");
    //out.println("  page=\"8.5,11\"");;
    out.println("node [shape=box];");
  }


  /**
   * exec executes dot application passing given file
   * @param file - name of file
   */
  protected void exec(File file) throws InterruptedException, IOException {
    if (!file.exists() || file.length() == 0) {
      if (deleteEmptyFile) {
        file.delete();
        if (logger.isEnabledFor(Level.INFO)) {
           logger.info("****** Deleting file " + file + " becaues there were no associations in it");
        }
      }
      return;
    }

    File dotFile = context.getFile(TAG_DOT_EXEC);
    if (dotFile == null || !dotFile.exists()) {
      logger.error("Download dot package from http://www.graphviz.org/Download..php and add set 'testplayer.dot.exec' in testplayer.properties to dot.exe, e.g.\n testplayer.dot.exec='C:\\Program Files\\ATT\\Graphviz\\bin\\dot.exe'");
      return;
    }
    String inFile = file.getAbsolutePath();
    int dot = inFile.lastIndexOf('.');
    String outFile;
    if (dot != -1) outFile = inFile.substring(0, dot) + ".gif";
    else outFile = inFile + ".gif";

    String cmd = dotFile.getAbsolutePath() + " -Tgif " + inFile + " -o " + outFile;
    if (logger.isEnabledFor(Level.INFO)) {
      logger.info("Executing " + cmd);
    }
    Process p = Runtime.getRuntime().exec(cmd);
    p.waitFor();
  }
  private boolean deleteEmptyFile;
}
