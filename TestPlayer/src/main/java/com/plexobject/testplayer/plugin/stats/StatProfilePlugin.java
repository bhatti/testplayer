/* ===========================================================================
 * $RCS$
 * Version: $Id: StatProfilePlugin.java,v 1.7 2007/07/19 17:10:30 shahzad Exp $
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

package com.plexobject.testplayer.plugin.stats;
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
 * 7/12/07     SB              created.
 */
public class StatProfilePlugin extends InterceptorPluginAdapter {
  public static final String TAG_DATA_DIR = "testplayer.stats.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.stats.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.stats.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.stats.file.ext";
  public static final String TAG_TOP_METHODS_SIZE = "testplayer.stats.top.methods";
  public static final String TAG_PRINT_INTERVAL = "testplayer.stats.print.interval";


  /**
   * StatProfilePlugin - constructor
   * @param context - application context
   */
  public StatProfilePlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "stats"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "StatProfile"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".dat")
        );
    topMethodsSize = context.getConfig().getInteger(TAG_TOP_METHODS_SIZE, 100);
    printInterval = context.getConfig().getInteger(TAG_PRINT_INTERVAL, 1000*60*15);
    // NOTE this output is shared output not per top level 
    File file = newFile("StatProfile", "graph");
    try {
      out = new PrintWriter(new FileWriter(file));
    } catch (IOException e) {
      throw new RuntimeException("Failed to create " + file, e);
    }
  }


  /**
   * newOutput creates PrintWriter object
   * @param call - method call information
   * @param file - name of file
   */
  protected synchronized Object newOutput(
        MethodEntry call, 
        File name ) throws IOException {
    return null; //new PrintWriter(new FileWriter(name));
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
  }

  protected void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) 
        throws Exception {
    synchronized (event.call.getSignature().intern()) {
      MethodStats data = getStatMap().get(event.call.getSignature());
      if (data == null) {
        data = new MethodStats(event.call.getSignature(), event.call.isConstructor());
      }
      data.incrInvoked(event.call.getTimesCalled());
      data.incrTotalResponseTime(event.call.getTotalResponseTime());
      data.incrTotalArgsSize(event.call.getArgsSize());
      data.incrTotalRvalueSize(event.call.getRvalueSize());
      getStatMap().put(event.call.getSignature(), data);
    }

    if (System.currentTimeMillis()-lastPrintedAt > printInterval) {
      printStats();
      lastPrintedAt = System.currentTimeMillis();
    } 
  }


  private Map<String, MethodStats> getStatMap() {
    return statMap;
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
    printStats(); 
    flush();	//out.close()??????????????????????
    removeOldMethods();
  }

  private void flush() {
    synchronized (out) {
      out.flush();
    }
  }
  private void removeOldMethods() {
    //getStatMap().clear();
  }

  private void println(Object o) {
    synchronized (out) {
      out.println(o);
    }
  }
  private void printStats() {
    println(new Date() + " Printing Stats from " + getStatMap().size() + " statMap");
    SortedSet<MethodStats> topInstants = MethodStats.newSetByInvokedTimes();
    SortedSet<MethodStats> topInvoked = MethodStats.newSetByInvokedTimes();
    SortedSet<MethodStats> averageResponse = MethodStats.newSetByAverageResponseTimes();
    SortedSet<MethodStats> totalResponse = MethodStats.newSetByTotalResponseTimes();
    SortedSet<MethodStats> topSize = MethodStats.newSetBySize();
    for (Map.Entry<String, MethodStats> e : getStatMap().entrySet()) {
      addData(topInstants, e.getValue(), true);
      addData(topInvoked, e.getValue(), false);
      addData(averageResponse, e.getValue(), false);
      addData(totalResponse, e.getValue(), false);
      addData(topSize, e.getValue(), false);
    }
      
    printData(topInstants , "Top Object Instants: ");
    printData(topInvoked, "Most called Methods: ");
    printData(averageResponse, "Slowest Methods by Average Response Time: ");
    printData(totalResponse, "Slowest Methods by Total Response Time: ");
    printData(topSize, "Methods with biggest Args/Return value: ");
  }

  private void addData(SortedSet<MethodStats> set, MethodStats data, boolean constructor) {
     if (data.isConstructor() == constructor) {
       set.add(data);
       if (set.size() > topMethodsSize) {
         MethodStats last = set.last();
         set.remove(last);
       }
     }
  }


  private void printData(SortedSet<MethodStats> set, String head) {
    println(head);
    println(MethodStats.getHeader());
    for (MethodStats data : set) {
      println(data.toString());
    }
    println("-------");
    println("");
    println("");
  }


  protected void cleanup() throws Exception {
  }

  protected String getFilename(MethodEntry call) { 
    return null;
  }


  private long lastPrintedAt;
  private PrintWriter out;
  private Map<String, MethodStats> statMap = new HashMap<String, MethodStats>();
  private int topMethodsSize;
  private long printInterval;
}
