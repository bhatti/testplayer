/* ===========================================================================
 * $RCS$
 * Version: $Id: InterceptorPluginAdapter.java,v 2.11 2006/08/23 02:13:05 shahzad Exp $
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

package com.plexobject.testplayer.plugin;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.util.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This class is base class for all plugin generators
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/18/05      SB              created.
 */
public abstract class InterceptorPluginAdapter implements InterceptorPlugin {
  /**
   * Base class that implements InterceptorPlugin and defines helper methods    
   * for children classes
   *
   * @param context - application context
   * @param dirName - directory where the tests will be generated
   * @param namePrefix - prefix that is attached to the output file name
   * @param nameSuffix - suffix that is attached to the output file name
   * @param nameExtension - file extension that is attached to the output file name
   */
  public InterceptorPluginAdapter(
        ApplicationContext context, 
        String dirName, 
        String namePrefix, 
        String nameSuffix, 
        String nameExtension) {
    this.context = context;
    this.dirName = dirName;
    this.namePrefix = namePrefix;
    this.nameSuffix = nameSuffix;
    this.nameExtension = nameExtension;
  }


  /**
   * before - receives notification before a method is invoked
   * @param context - application context
   * @param event - call event
   */
  public void before(ApplicationContext context, MethodEvent event) {
    try {
      /////////////////////////////////////////////////////////////////
      if (!context.isPermittedPackage(event.call.getCalleePackageName())) {
        if (logger.isEnabledFor(Level.DEBUG)) {
           logger.debug("!!!!!before listener could not match event against packages,  skipping " + event);
        }
        return;
      }
      String pkg = event.call.getCalleePackageName();

      //////////////////////////////////////////////////////////////////
      // Create name of class for Base
      //////////////////////////////////////////////////////////////////
      Object out = createOutput(event.call, getFilename(event.call), pkg);
      if (acceptBefore(event)) {
        if (event.call.isTopMethod()) {
          cflowBegin(context, event, out);
        }
        before(context, event, out);
      }


      ////
      if (logger.isEnabledFor(Level.DEBUG)) {
        //logger.debug("Processed before event " + event);
      }
    } catch (Throwable e) {
      logger.error("Failed to handle before event " + event, e);
    }
  }



  /**
   * after - receives notification after a method is invoked
   * @param context - application context
   * @param event - call event
   */
  public void after(ApplicationContext context, MethodEvent event) {
    try {
      /////////////////////////////////////////////////////////////////
      if (!context.isPermittedPackage(event.call.getCalleePackageName())) {
        if (logger.isEnabledFor(Level.DEBUG)) {
           //logger.debug("!!!!!after listener could not match event against packages,  skipping " + event);
        }
        return;
      }


      String pkg = event.call.getCalleePackageName();

      //////////////////////////////////////////////////////////////////
      // Create name of class for Base
      //////////////////////////////////////////////////////////////////
      String fileName = getFilename(event.call);
      Object out = createOutput(event.call, fileName, pkg);
      if (acceptAfter(event)) {
        // if method throws exception then following method fails
        after(context, event, out);
        if (event.call.isTopMethod()) {
          cflowEnd(context, event, out);
        }
      }

      if (logger.isEnabledFor(Level.DEBUG)) {
        //??????logger.debug("Processed [" + event.call.getException() + "] after event " + event);
      }
    } catch (com.thoughtworks.xstream.converters.reflection.ObjectAccessException e) {
      logger.error("Failed to handle after event " + event + " due to " + e);
    } catch (com.thoughtworks.xstream.converters.ConversionException e) {
      logger.error("Failed to handle after event " + event + " due to " + e);
    } catch (Throwable e) {
      logger.error("Failed to handle after event " + event + " -----due to " + e.getClass().getName(), e);
    }
  }


  /**
   * init - method can be overridden to provide any setup.
   * @param context - application context
   */
  public void init(ApplicationContext context) {
  }


  /**
   * destroy - method can be overridden to provide any cleanup.
   * @param context - application context
   */
  public void destroy(ApplicationContext context) {
    try {
      cleanup();
    } catch (Exception e) {
      logger.error("Failed to cleanup ", e);
    }


    Iterator it = fileWriterMap.values().iterator();
    while (it.hasNext()) {
      try {
        Object writer = it.next();
        closeFile(writer);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * creates filename for this generator.
   * @param call - method call
   * @return - returns filename
   */
  protected String getFilename(MethodEntry call) { 
    String callee = call.getCalleeName();
    if (callee == null) throw new IllegalArgumentException("No callee found for " + call);
    return namePrefix + callee.substring(callee.lastIndexOf('.')+1) + nameSuffix;
  }


  /**
   * override newOutput to create output stream, writer or encoder
   * @param call - method call information
   * @param file - name of file
   */
  protected abstract Object newOutput(
        MethodEntry call, 
        File file) throws IOException;


  /**
   * override initFile to initialize output file
   * @param call - method call information
   * @param file - name of file
   */
  protected abstract void initFile(
        MethodEntry call, 
        File file, 
        Object out, 
        String pkg) throws IOException; 


  /**
   * override closeFile to close output file
   * @param out - output object
   */
  protected abstract void closeFile(Object out) throws IOException; 


  /**
   * override cleanup to do any householding stuff, it is called before
   * closing all files
   */
  protected abstract void cleanup() throws Exception;


  /**
   * override before to process method call processing before invocation
   * It is recommended that instead of before method of InterceptorPlugin,
   * derived classes override this method, because the base class's before
   * method maintains files, removes duplicate method invocation with
   * same parameters and output.
   */
  protected abstract void before(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) throws Exception; 



  /**
   * override after to process method call processing after invocation
   * It is recommended that instead of before method of InterceptorPlugin,
   * derived classes override this method, because the base class's before
   * method maintains files, removes duplicate method invocation with
   * same parameters and output.
   */
  protected abstract void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) throws Exception; 


  /**
   * override cflowBegin to process method call processing before top level
   * method is invoked.
   * Note: This method is invoked before the abstract "before" method.
   */
  protected void cflowBegin(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) throws Exception {
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


  /**
   * filters before events.
   * @return return true if before events should be processed.
   */
  protected boolean acceptBefore(MethodEvent event) {
    return true;
  }


  /**
   * filters after events.
   * @return return true if after events should be processed.
   */
  protected boolean acceptAfter(MethodEvent event) {
    return true;
  }

  /**
   * @param writer - output stream
   * @return return filename for given output stream
   */
  protected File getOutputFile(Object writer) {
    return (File) writerFileMap.get(writer);
  }


  /**
   * @param writer - output stream
   * @param file - file object
   * @return return filename for given output stream
   */
  protected void setOutputFile(Object writer, File file) {
    writerFileMap.put(writer, file);
    String fileName = file.getName();
    int n = fileName.indexOf(nameExtension);
    if (n != -1) fileName = fileName.substring(0, n);
    fileWriterMap.put(fileName, writer);
  }

  /**
   * @param name - partial file name without directory and without extension
   * @return return file object 
   */
  protected File newFile(String name, String pkg) {
    File dir = context.newFile(
                dirName, pkg.replace('.', ApplicationContext.FS.charAt(0)));
    dir.mkdirs();
    return new File(dir, name + nameExtension);
  }


  /////////////////////////////////////////////////////////////////
  // Create output and write basic initial code if needed
  /////////////////////////////////////////////////////////////////
  private Object createOutput(
        MethodEntry call, 
        String fileName,
        String pkg) throws IOException {
    synchronized (fileWriterMap) {
      Object out = fileWriterMap.get(fileName);
      if (out == null) {
        File file = newFile(fileName, pkg);
        if (logger.isEnabledFor(Level.INFO)) {
          logger.info(" Writing " + file);
        }
        out = newOutput(call, file);
        setOutputFile(out, file);
        initFile(call, file, out, pkg);
      }
      return out;
    }
  }


  protected transient final String dirName;
  protected transient final String namePrefix;
  protected transient final String nameSuffix;
  protected transient final String nameExtension;
  protected transient final ApplicationContext context;
  protected transient final Logger logger = Logger.getLogger(getClass().getName());
  private transient final Map fileWriterMap = new HashMap();
  private transient final Map writerFileMap = new HashMap();
}
