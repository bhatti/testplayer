/* ===========================================================================
 * $RCS$
 * Version: $Id: NamingStrategy.java,v 1.5 2006/03/12 22:01:32 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test.strategy;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.marshal.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;
import java.util.regex.*;
import java.lang.reflect.*;

/**
 * This interface defines test writing strategy
 *
 * @author shahzad bhatti
 *
 * modification history
 * date      who         what
 * 2/5/06     SB          created.
 */
public interface NamingStrategy extends Constants {
   public void setSubPackage(String identifier);

   public Configuration getConfig(); 

   public void setTestInTest(boolean value);

   public boolean isTestPackageName(String packageName);

   public boolean isTestClassName(String fullClassName);

   public String stripParentPackage(String fullClassName);

   public String getTestCaseName(String fullClassName);

   public String getPackageName(String fullClassName);

   public String getTestSuiteName(String packageName);

   public String getTestPackageName(String packageName);

   public String getFullTestCaseName(String fullClassName);

   public String getFullTestSuiteName(String packageName);

   public String getTestMethodName(String methodName);

   public String getTestAccessorName(String prefixSet, String prefixGet, String accessorName);
}
