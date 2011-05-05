/* ===========================================================================
 * $RCS$
 * Version: $Id: ComponentClassLoader.java,v 1.2 2006/08/14 03:05:39 shahzad Exp $
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
 * Version: $Id: ComponentClassLoader.java,v 1.2 2006/08/14 03:05:39 shahzad Exp $
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
public class ComponentClassLoader extends URLClassLoader {
  public ComponentClassLoader (MasterClassLoader master, File file)
        throws MalformedURLException, IOException {
    super(new URL[] {file.toURL()}, null);
    this.file = file;
    this.master = master;
    this.dependencies = extractDependencies();
  }
    
  public String toString () {
    return file.toString();
  }

  private List extractDependencies () throws IOException {
    JarFile jar = new JarFile(file);
    Manifest man = jar.getManifest();
    Attributes attr = man.getMainAttributes();

    List l = new ArrayList();
    String str = attr.getValue("Restricted-Class-Path");
    if (str != null) {
      StringTokenizer tok = new StringTokenizer(str);
      while (tok.hasMoreTokens()) {
        l.add(new File(file.getParentFile(), tok.nextToken()));
      }
    }
    return l;
  }

  public Class loadClass (String name, boolean resolve)
        throws ClassNotFoundException {
    try {
      // Try to load the class from our JAR.
      Class klass = loadClassForComponent(name, resolve);
      //PackageClassMapper.getInstance().addClass(klass);
      return klass;
    } catch (ClassNotFoundException ex) {}
    // Couldn't find it -- let the master look for it in another components.
    Class klass = master.loadClassForComponent(name, resolve, dependencies);
    //PackageClassMapper.getInstance().addClass(klass);
    return klass;
  }

  public Class loadClassForComponent (String name, boolean resolve)
        throws ClassNotFoundException {
    Class c = findLoadedClass(name);
    // Even if findLoadedClass returns a real class, we might simply
    // be its initiating ClassLoader.  Only return it if we're actually
    // its defining ClassLoader (as determined by Class.getClassLoader).
    if (c == null || c.getClassLoader() != this) {
      c = findClass(name);
      if (resolve) {
         resolveClass(c);
      }
    }
    return c;
  }

  public URL findResource (String name) {
    // Try to load the resource from our JAR.
    URL url = getResourceForComponent(name);
    if (url != null) {
      return url;
    }

    // Couldn't find it -- let the master look for it in another components.
    return master.getResourceForComponent(name, dependencies);
  }

  public URL getResourceForComponent (String name) {
    return super.findResource(name);
  }

  public Enumeration findResources (String name) throws IOException {
    Vector vec = new Vector();
    // Try to load the resource from our JAR.
    Enumeration e = getResourcesForComponent(name);
    while (e.hasMoreElements()) {
      vec.add(e.nextElement());
    }
    // Now let the master look for it in another components.
    Enumeration e2 = master.getResourcesForComponent(name, dependencies);
    while (e2.hasMoreElements()) {
      vec.add(e2.nextElement());
    }
    return vec.elements();
  }

  public Enumeration getResourcesForComponent (String name)
                throws IOException {
    try {
      return super.findResources(name);
    } catch (IOException ex) {
      return new Vector().elements();
    }
  }
  private MasterClassLoader master;
  private File file;
  private List dependencies;
}


