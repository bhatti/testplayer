/* ===========================================================================
 * $RCS$
 * Version: $Id: WrapperClassLoader.java,v 1.3 2006/08/16 14:24:48 shahzad Exp $
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

package com.plexobject.testplayer.classloader;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import org.apache.log4j.*;

/**
 * WrapperClassLoader defines wrapper class loader 
 * @author shahzad bhatti
 *
 * Version: $Id: WrapperClassLoader.java,v 1.3 2006/08/16 14:24:48 shahzad Exp $
 *
 * Run java -Xbootclasspath/a:classloader.jar \
 * -Djava.system.class.loader=
 *  com.plexobject.testplayer.classloader.WrapperClassLoader \
 *  -jar testplayer.jar
 *
 * modification history
 * date         who             what
 * 3/11/05      SB              created.
 */
public class WrapperClassLoader extends ClassLoader {
  public WrapperClassLoader (ClassLoader parent)
                throws MalformedURLException, IOException {
    super(parent);
    classLoaders = initClassLoaders(new MasterClassLoader(parent));
  }
  public static List initClassLoaders (MasterClassLoader master)
                throws MalformedURLException, IOException {
    List loaders = new ArrayList();
    String classpath = System.getProperty("java.class.path");
    StringTokenizer tok = new StringTokenizer(classpath, File.pathSeparator);
    while (tok.hasMoreTokens()) {
      File file = new File(tok.nextToken());
      loaders.add(master.getComponentClassLoader(file));
    }
    return loaders;
  }
  public Class loadClass (String name, boolean resolve)
        throws ClassNotFoundException {
    for (Iterator i = classLoaders.iterator(); i.hasNext(); ) {
      ClassLoader cl = (ClassLoader)i.next();
      try {
        Class klass = cl.loadClass(name);
        PackageClassMapper.getInstance().addClass(klass);
        return klass;
      } catch (ClassNotFoundException ex) {}
    }
    Class klass = super.loadClass(name, resolve);
    PackageClassMapper.getInstance().addClass(klass);
    return klass;
  }

  public URL findResource (String name) {
    for (Iterator i = classLoaders.iterator(); i.hasNext(); ) {
      ClassLoader cl = (ClassLoader)i.next();
      URL url = cl.getResource(name);
      if (url != null) {
        return url;
      }
    }
    return super.findResource(name);
  }

  public Enumeration findResources (String name)
                throws IOException {
    Vector resources = new Vector();
    for (Iterator i = classLoaders.iterator(); i.hasNext(); ) {
      ClassLoader cl = (ClassLoader)i.next();
      Enumeration it = cl.getResources(name);
      while (it.hasMoreElements()) {
        resources.add(it.nextElement());
      }
    }
    Enumeration it = super.findResources(name);
    while (it.hasMoreElements()) {
      resources.add(it.nextElement());
    }
    return resources.elements();
  }


  private static final Logger logger = Logger.getLogger(WrapperClassLoader.class.getName());
  private List classLoaders;
}
