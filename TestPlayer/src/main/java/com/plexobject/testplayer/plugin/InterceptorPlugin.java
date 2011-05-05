/* ===========================================================================
 * $RCS$
 * Version: $Id: InterceptorPlugin.java,v 2.3 2006/02/25 20:50:47 shahzad Exp $
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
import java.io.*;
import java.util.*;
import java.text.*;

/**
 * This interface is used to hooks listener that are invoked for each method 
 * that the user is interested in. The listener will be notified before and 
 * after the method invocation. 
 * Note: There will be a single instance of plugin created at the start of
 * an application, so it is important that it cleansup any householding
 * stuff appropriately. Also, it is recommended that instead of implementing
 * this interface, you extend from BaseGenerator class, which provides
 * a lot of helper methods for householding.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/5/05       SB              created.
 */
public interface InterceptorPlugin extends java.util.EventListener {
  /**
   * init - is called when plugin is started
   * @param context - application context
   */
  public void init(ApplicationContext context); 


  /**
   * before - receives notification before method invocation
   * @param context - application context
   * @param event - method event
   */
  public void before(ApplicationContext context, MethodEvent event); 


  /**
   * after - receives notification after method invocation
   * @param context - application context
   * @param event - method event
   */
  public void after(ApplicationContext context, MethodEvent event); 


  /**
   * destroy - is called when plugin is destroyed
   * @param context - application context
   */
  public void destroy(ApplicationContext context); 
}
