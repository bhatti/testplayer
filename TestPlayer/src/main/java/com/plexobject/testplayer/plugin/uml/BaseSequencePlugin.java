/* ===========================================================================
 * $RCS$
 * Version: $Id: BaseSequencePlugin.java,v 2.6 2007/07/11 13:53:47 shahzad Exp $
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

package com.plexobject.testplayer.plugin.uml;
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
 * This class stores call event for each method invocation that 
 * matched user specification
 * It uses performance library from  
 * http://www.zanthan.com/itymbi/archives/cat_performance.html
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/13/05     SB              created.
 */
public abstract class BaseSequencePlugin extends InterceptorPluginAdapter {
  public static final String TAG_TOP_METHODS = "testplayer.uml.sequence.top.methods";
  /**
   * BaseSequencePlugin - base class for code generators for sequence diagrams
   * @param context - application context
   * @param dirName - directory where the tests will be generated
   * @param namePrefix - prefix that is attached to the output file name
   * @param nameSuffix - suffix that is attached to the output file name
   * @param nameExtension - file extension that is attached to the output file name 
   */
  public BaseSequencePlugin(
        ApplicationContext context, 
        String dirName, 
        String namePrefix, 
        String nameSuffix, 
        String nameExtension) {
    super(context, dirName, namePrefix, nameSuffix, nameExtension);
    onlyTopMethod = context.getConfig().getBoolean(TAG_TOP_METHODS, true);
    buffer = new StringBuilder();
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
    printTrace(event.call, out);
  }



  /**
   * override cflowBegin to process method call processing before top level
   * method is invoked.
   * Note: This method is invoked after the abstract "before" method.
   */
  protected void cflowBegin(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) throws Exception {
    PrintWriter out = (PrintWriter) writer;
    //printTrace(event.call, out);
    out.println(event.call.getCalleeNameWithoutPackage() + " {");
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
    PrintWriter out = (PrintWriter) writer;
    File file = getOutputFile(writer); 
    out.println(buffer.toString());
    out.println("}");
    out.close();
    com.zanthan.sequence.Main.main(
        new String[] {
                "--headless", file.getAbsolutePath()
        }
    );
  }


  /**
   * perform any last cleanup 
   */
  protected void cleanup() throws Exception {
  }


  /**
   * overrides getFilename to return the file name of top method only,
   * thus sequence diagrams will only be generated for top method.
   */
  protected String getFilename(MethodEntry call) { 
    if (onlyTopMethod) call = call.getTopMethod();
    return super.getFilename(call); 
  }



  /**
   * after - receives notification after a method is invoked
   * @param context - application context
   * @param event - call event
   * @param writer - output object
   */
  protected void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) 
        throws Exception {
    PrintWriter out = (PrintWriter) writer;
    //out.println("}");
    buffer.append("}" + context.LF);
  }



  protected void printReturnType(MethodEntry call, PrintWriter out) {
    String rtype = call.getMethodReturnTypeWithoutPackage();
    buffer.append(rtype + " {" + context.LF);
    //out.println(rtype + " {");
  }



  private void printTrace(MethodEntry call, PrintWriter out) {
    String name = null;
    if (call.isConstructor()) {
       name = "new_" + call.getMethodName(); 
    } else {
       name = call.getMethodName(); 
    }
    buffer.append(
        call.getCalleeNameWithoutPackage() + 
        "." + 
        name + 
        " -> ");
    printReturnType(call, out); 
  }


  ////////////////////////////////////////////////////////////////////
  //
  private boolean onlyTopMethod;
  protected StringBuilder buffer;
}
