/* ===========================================================================
 * $RCS$
 * Version: $Id: SequencePlugin.java,v 2.3 2006/02/25 20:50:53 shahzad Exp $
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
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.tree.*;
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
 * It uses sequence library from  
 * http://www.zanthan.com/itymbi/archives/cat_sequence.html
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/4/05      SB              created.
 */
public class SequencePlugin extends BaseSequencePlugin {
  public static final String TAG_DATA_DIR = "testplayer.uml.sequence.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.uml.sequence.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.uml.sequence.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.uml.sequence.file.ext";
  /**
   * SequencePlugin- creates sequence diagrams 
   * @param context - application context
   */
  public SequencePlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "uml"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "Sequence"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".txt")
        );
  }
}
