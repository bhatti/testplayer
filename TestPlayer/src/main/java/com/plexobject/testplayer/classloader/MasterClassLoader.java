/* ===========================================================================
 * $RCS$
 * Version: $Id: MasterClassLoader.java,v 1.2 2006/08/14 03:05:39 shahzad Exp $
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

/**
 * ComponentClassLoader defines class loader for each component
 * @author shahzad bhatti
 *
 * Version: $Id: MasterClassLoader.java,v 1.2 2006/08/14 03:05:39 shahzad Exp $
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
public class MasterClassLoader extends ClassLoader {
  public MasterClassLoader (ClassLoader parent) {
    super(parent);
  }

  public ComponentClassLoader getComponentClassLoader (File file)
        throws MalformedURLException, IOException {
    ComponentClassLoader ccl = (ComponentClassLoader)componentClassLoaders.get(file);
    if (ccl == null) {
      ccl = new ComponentClassLoader(this, file);
      componentClassLoaders.put(file, ccl);
    }
    return ccl;
  }

  public Class loadClassForComponent (String name, boolean resolve, List files)
        throws ClassNotFoundException {
    try {
      Class klass = loadClass(name, resolve);
      //PackageClassMapper.getInstance().addClass(klass);
      return klass;
    } catch (ClassNotFoundException ex) {}

    for (Iterator i = files.iterator(); i.hasNext(); ) {
      File f = (File)i.next();
      ComponentClassLoader ccl;
      try {
        ccl = getComponentClassLoader(f);
      } catch (MalformedURLException ex) {
        continue;
      } catch (IOException ex) {
        continue;
      }
      try {
        Class klass = ccl.loadClassForComponent(name, resolve);
        //PackageClassMapper.getInstance().addClass(klass);
        return klass;
      } catch (ClassNotFoundException ex) {}
    }

    throw new ClassNotFoundException(name);
  }

  public URL getResourceForComponent (String name, List files) {
    URL url = getResource(name);
    if (url != null) {
      return url;
    }

    for (Iterator i = files.iterator(); i.hasNext(); ) {
      File f = (File)i.next();
      ComponentClassLoader ccl;
      try {
        ccl = getComponentClassLoader(f);
      } catch (MalformedURLException ex) {
        continue;
      } catch (IOException ex) {
        continue;
      }

      url = ccl.getResourceForComponent(name);
      if (url != null) {
        return url;
      }
    }
    return null;
  }

  public Enumeration getResourcesForComponent (String name, List files)
                throws IOException {
    Vector vec = new Vector();
    Enumeration e = getResources(name);
    while (e.hasMoreElements()) {
      vec.add(e.nextElement());
    }

    for (Iterator i = files.iterator(); i.hasNext(); ) {
      File f = (File)i.next();

      ComponentClassLoader ccl;
      try {
        ccl = getComponentClassLoader(f);
      } catch (MalformedURLException ex) {
        continue;
      } catch (IOException ex) {
        continue;
      }

      Enumeration e2 = ccl.getResourcesForComponent(name);
      while (e2.hasMoreElements()) {
        vec.add(e2.nextElement());
      }
    }
    return vec.elements();
  }
  private Map componentClassLoaders = new HashMap();
}
