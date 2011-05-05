/* ===========================================================================
 * $RCS$
 * Version: $Id: PerformanceStatsPlugin.java,v 2.5 2007/07/11 13:53:47 shahzad Exp $
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
public class PerformanceStatsPlugin extends BaseSequencePlugin {
  public static final String TAG_DATA_DIR = "testplayer.uml.performance.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.uml.performance.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.uml.performance.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.uml.performance.file.ext";
  /**
   * PerformanceStatsPlugin - creates sequence diagrams with timings
   * @param context - application context
   */
  public PerformanceStatsPlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "uml"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "PerformanceStats"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".txt")
        );
    int len = String.valueOf(Long.MAX_VALUE).length();
    for (int i=0; i<len; i++) {
      formatBuffer.append(' ');
    }
  }



  /**
   * printReturnType overrides method to save position so that timing
   * information can be inserted later.
   * @param call - method info
   * @param out - output object
   */
  protected void printReturnType(MethodEntry call, PrintWriter out) {
    if (buffer == null) throw new IllegalArgumentException("Null buffer");
    positions.put(new Long(call.getId()), new Long(buffer.length()));
    //buffer.append(filledBuffer);
    buffer.append(" {" + context.LF);
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
    if (buffer == null) throw new IllegalArgumentException("Null buffer");
    Long pos = (Long) positions.get(new Long(event.call.getId()));
    if (pos == null) {
      logger.warn("No position for " + event.call);
    } else {
      buffer.insert(pos.intValue(), formatBuffer.toString());
      buffer.insert(pos.intValue(), String.valueOf(event.call.getResponseTime()) + "_millis");
    }
    super.after(context, event, writer);
  }


  ////////////////////////////////////////////////////////////////////
  //
  private final Map positions = new HashMap();
  private StringBuilder formatBuffer = new StringBuilder();
}
