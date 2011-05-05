/* ===========================================================================
 * $RCS$
 * Version: $Id: AssociationPlugin.java,v 2.5 2006/02/27 21:50:42 shahzad Exp $
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
public class AssociationPlugin extends BaseDotPlugin {
  public static final String TAG_DATA_DIR = "testplayer.association.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.association.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.association.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.association.file.ext";
  /**
   * AssociationPlugin - constructor
   * @param context - application context
   */
  public AssociationPlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "dot"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "Association"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".dot")
        );
    duplicates= new HashMap();
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
   * closeFile close output file
   * @param out - output object
   */
  protected void closeFile(Object writer) throws IOException {
    // already closed
  }


  /**
   * before - receives notification before a method is invoked
   * @param event - call event
   */
  protected void before(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer)
        throws Exception {
    PrintWriter out = (PrintWriter) writer;
    if (event.call.getCaller() != null) {
      String caller = event.call.getCallerNameWithoutPackage();
      String callee = event.call.getCalleeNameWithoutPackage();
      if (!caller.equals(callee)) {
        String line = caller + " -> " + callee;
        if (duplicates.get(line) == null) {
          out.println(line);
          duplicates.put(line, line);
        }
      }
    }
  }

  protected void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) 
        throws Exception {
    PrintWriter out = (PrintWriter) writer;
  }

  /**
   * override cflowEnd to process method call processing after top level
   * method is invoked.
   * Note: This method is invoked after the abstract "after" method.
   */
  protected void cflowEnd(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) throws Exception {
    File file = getOutputFile(writer); 
    PrintWriter out = (PrintWriter) writer;
    out.println("}");
    out.close();
    exec(file);
    duplicates.clear();
  }

  protected void cleanup() throws Exception {
  }

  protected String getFilename(MethodEntry call) { 
    call = call.getTopMethod();
    return super.getFilename(call); 
  }
  private Map duplicates;
}
