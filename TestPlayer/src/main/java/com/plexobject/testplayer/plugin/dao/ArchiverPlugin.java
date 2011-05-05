/* ===========================================================================
 * $RCS$
 * Version: $Id: ArchiverPlugin.java,v 1.2 2007/07/15 20:24:49 shahzad Exp $
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

package com.plexobject.testplayer.plugin.archive;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.tree.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.dao.hibernate.MethodDaoHibernate;
import com.plexobject.testplayer.dao.MethodDao;
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
public class ArchiverPlugin extends InterceptorPluginAdapter {
  public static final String TAG_DATA_DIR = "testplayer.archive.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.archive.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.archive.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.archive.file.ext";
  public static final String TAG_TOP_METHODS_SIZE = "testplayer.archive.top.methods";


  /**
   * ArchiverPlugin - constructor
   * @param context - application context
   */
  public ArchiverPlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "archive"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "Archiver"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".dat")
        );
     MethodDaoHibernate.createTable(); 
     this.dao = new MethodDaoHibernate();
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
    try {
      dao.save(event.call);
    } catch (Exception e) {
      logger.error("Failed to save " + event, e);
    }
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
  }

  protected void cleanup() throws Exception {
  }

  protected String getFilename(MethodEntry call) { 
    call = call.getTopMethod();
    return super.getFilename(call); 
  }

  //
  private MethodDao dao;

}
