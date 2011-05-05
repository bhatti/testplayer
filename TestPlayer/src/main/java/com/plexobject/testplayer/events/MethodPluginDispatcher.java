/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodPluginDispatcher.java,v 2.9 2006/08/23 20:13:19 shahzad Exp $
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

package com.plexobject.testplayer.events;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This class is used to notify listeners of MethodPluginInterceptor(s). 
 * It uses mediator pattern to recieve updates on calls and notifies listeners
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/14/05      SB              created.
 */
public class MethodPluginDispatcher implements Runnable {
  public static final String TAG_PLUGIN = "testplayer.plugin";
  /**
   * MethodPluginDispatcher constructor
   * @param context - application context
   */
  public MethodPluginDispatcher(ApplicationContext context) {
    this.context = context;
    for (int i=1;;i++) {
      String pluginType = context.getConfig().getProperty(TAG_PLUGIN + i);
      try {
        if (pluginType == null) break;
        //if (logger.isEnabledFor(Level.DEBUG)) logger.debug("Adding Plugin type " + pluginType);
        Class type = Class.forName(pluginType);
        InterceptorPlugin plugin = null;
        try {
          Constructor ctor = type.getConstructor(
                                new Class[] {ApplicationContext.class});
          plugin = (InterceptorPlugin) ctor.newInstance(new Object[] {context});
        } catch (java.lang.reflect.InvocationTargetException e) {
          if (e.getTargetException() instanceof IllegalStateException) {
            continue;
          }
          plugin = (InterceptorPlugin) type.newInstance();
        } catch (Exception e) {
          plugin = (InterceptorPlugin) type.newInstance();
        }
        if (listeners.indexOf(plugin) == -1) {
          plugin.init(context);
          listeners.add(plugin);
          if (logger.isEnabledFor(Level.INFO)) {
            logger.info("Added Plugin " + plugin);
          }
        }
      } catch (Exception e) {
        logger.error("Failed to add plugin " + pluginType, e);
      }

      ///////////////////////////////////////////////////////////////////
      if (context.isNotifyAsynchronously()) {
        context.newThread(this);
      }
    }

    /////////////////////////////////////////////////////////////////////
    try {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          die();
          //if (logger.isEnabledFor(Level.DEBUG)) logger.debug("!!!!!Shutting down!!!!");
        }
      });
    } catch (IllegalStateException e) {
      destroy = true;
      throw e;
      //logger.debug("!!!!!Shutting down!!!!", e);
    }
  }


  /**
   * notifyCall - notifies all listeners asynchronously, meaning caller 
   * will not wait.
   * @param event - call event
   */
  public void notifyCall(MethodEvent event) {
    if (context.isNotifyAsynchronously()) {
      synchronized (queue) {
        queue.add(event);
        queue.notify();
      }
    } else {
      notifyCallImpl(event);
    }
  }



  /**
   * addListener - registers listener which will receive call events
   * @param l - listener callback interface
   */
  public void addPlugin(InterceptorPlugin l) {
    synchronized (listeners) {
      if (listeners.indexOf(l) == -1) listeners.add(l);
    }
  }


  /**
   * removeListener - unregisters listener which receives call events
   * @param l - listener callback interface
   */
  public void removePlugin(InterceptorPlugin l) {
    synchronized (listeners) {
      int n = listeners.indexOf(l);
      if (n != -1) listeners.remove(n);
    }
  }

  /**
   * @return - returns true if event dispatcher is destroyed or 
   * shutdown is in progress...
   */
  public boolean isDestroyed() {
    return destroy;
  }

  /**
   * die event dispatcher and exits dispatcher thread
   */
  public void die() {
    this.destroy = true;
    notifyDestroy(); 
    synchronized (queue) {
      queue.notify();
    }
    try {
      waitUntilQueueIsEmpty();
    } catch (Exception e) {
      //e.printStackTrace();
    }
  }

  /**
   * run - implements background thread that checks call events in queue and 
   * notifies all listeners
   */
  public void run() {
    while (true) {
      try {
        List events = getNewEvents(); 
        if (events.size() == 0 && destroy) break;
        for (int i=0; i<events.size(); i++) {
          MethodEvent event = (MethodEvent) events.get(i);
          notifyCallImpl(event); 
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////
  private void notifyDestroy() {
    List copy = null;
    synchronized (listeners) {
      copy = new ArrayList(listeners);
    }
    Iterator it = copy.iterator();
    while (it.hasNext()) {
      InterceptorPlugin l = (InterceptorPlugin) it.next();
      //if (logger.isEnabledFor(Level.INFO)) logger.info("Disposing plugins " + l);
      try {
        l.destroy(context);
      } catch (Exception e) {
        logger.error("Failed to destroy plugin " + l, e);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  //
  private void waitUntilQueueIsEmpty() {
    synchronized (queue) {
      while (queue.size() > 0) {
        try {
          queue.wait(500);
        } catch (InterruptedException e) { }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  //
  private List getNewEvents() {
    synchronized (queue) {
      while (queue.size() == 0) {
        try {
          queue.wait(500);
        } catch (InterruptedException e) {
        }
        if (destroy) break;
      }
      List events = new ArrayList(queue);
      queue.clear();
      return events;
    }
  }


  //////////////////////////////////////////////////////////////////////////
  //
  private void notifyCallImpl(MethodEvent event) {
    List copy = null;
    synchronized (listeners) {
      copy = new ArrayList(listeners);
    }
    Iterator it = copy.iterator();
    while (it.hasNext()) {
      InterceptorPlugin l = (InterceptorPlugin) it.next();
      if (l == null) throw new IllegalArgumentException("Null listener");
      if (logger.isEnabledFor(Level.DEBUG)) {
         //logger.debug("Notifying listener " + l + " with event " + event);
      }
      try {
         switch (event.type) {
           case MethodEvent.BEFORE:
             l.before(context, event);
             break;
           case MethodEvent.AFTER:
             l.after(context, event);
             break;
         }
      } catch (com.thoughtworks.xstream.converters.reflection.ObjectAccessException e) {
         logger.error("Failed to notify listener " + l + " with event " + event + " due to " + e);
      } catch (com.thoughtworks.xstream.converters.ConversionException e) {
         logger.error("Failed to notify listener " + l + " with event " + event + " due to " + e);
      } catch (Throwable e) {
         logger.error("Failed to notify listener " + l + " with event " + event, e);
      }
    }
  }




  private transient List listeners = new ArrayList(); 
  private transient List queue = new ArrayList(); 
  private transient boolean destroy;
  private transient ApplicationContext context;
  private transient static Logger logger = Logger.getLogger(MethodPluginDispatcher.class.getName());
}
