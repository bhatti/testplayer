/* ===========================================================================
 * $RCS$
 * Version: $Id: PackageClassMapper.java,v 1.7 2006/08/14 03:05:39 shahzad Exp $
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
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * PackageClassMapper defines wrapper class loader 
 * @author shahzad bhatti
 *
 * Version: $Id: PackageClassMapper.java,v 1.7 2006/08/14 03:05:39 shahzad Exp $
 *
 *
 * modification history
 * date         who             what
 * 3/11/05      SB              created.
 */
public class PackageClassMapper {
  public static final String TESTAPP_CLASSES = "testplayer.testapp.classes";
  public static final String TESTAPP_PRELOAD_JAR = "testplayer.testapp.preloadClassesFromJarFiles";
  private static final Logger logger = Logger.getLogger(PackageClassMapper.class.getName());
  private PackageClassMapper() {
    classes = new Vector();
    map = new HashMap();
    context = new ApplicationContext();
    parseJars = context.getConfig().getBoolean(TESTAPP_PRELOAD_JAR); 
    String classProp = context.getConfig().getProperty(TESTAPP_CLASSES);
    //if (logger.isInfoEnabled()) logger.info(">>>>>loading classes " + classProp);
    if (classProp != null) {
      String[] classNames = StringHelper.split(classProp, " \t,;");
      for (int i=0; i<classNames.length; i++) {
        try {
          addClass(classNames[i]);
        } catch (Throwable e) {
          logger.error("Failed to load type '" + classNames[i] + "' due to " + e);
        }
      }
    }
    String classpath = System.getProperty("java.class.path");
    StringTokenizer tok = new StringTokenizer(classpath, File.pathSeparator);
    while (tok.hasMoreTokens()) {
      File file = new File(tok.nextToken());
      if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
        if (parseJars) addClassesFromZip(file.getAbsolutePath());
      } else if (file.exists() && file.isDirectory()) {
        addClassesFromDir(file.getAbsolutePath());
      }
    }
  }

  public void addClass(String klass) throws ClassNotFoundException {
    klass = file2class(klass);
    addClass(Class.forName(klass));
  }
  public void addClass(Class klass) {
    if (klass == null) return;
    if (classes.indexOf(klass) != -1) return;
    if (logger.isInfoEnabled()) logger.info(">>>>> adding class " + klass.getName());
    Package pkg = klass.getPackage();
    if (pkg == null) return;
    String name = pkg.getName();
    if (name == null || name.length() == 0) return;
    if (!context.isPermittedPackage(name)) return;
    synchronized (pkg) {
      List list = (List) map.get(name);
      if (list == null) {
        list = new ArrayList();
        map.put(name, list);
      }
      if (list.indexOf(klass) == -1) list.add(klass);
    }
    classes.add(klass);
  }

  public static PackageClassMapper getInstance() {
    return instance;
  }

  public Class[] getClassesForPackage(String name) {
    List list = (List) map.get(name);
    if (list == null) list = new ArrayList();
    return (Class[]) list.toArray(new Class[list.size()]);
  }


  public String[] getPackages() {
    List list = new ArrayList(map.keySet());
    return (String[]) list.toArray(new String[list.size()]);
  }


  public Class[] getAllClasses() {
    return (Class[]) classes.toArray(new Class[classes.size()]);
  }



  /**
   * This method find dependent types for all classes in given zip 
   * and adds them to the internal map, which is used later to 
   * print dependencies.
   * @param zip - zip file name
   */
  public void addClassesFromZip(String zip) {
    JarResources jr = new JarResources(zip);
    String[] names = jr.getResourceNames();
    for (int i=0; i<names.length; i++) {
      if (names[i].endsWith(".class")) {
        String name = file2class(names[i]);
        try {
          addClass(name);
        } catch (Throwable e) {
          logger.error("Failed to load zip type '" + name + "' due to " + e);
        }
      }
    }
  }


  /**
   * This method find dependent types for all classes in given classes dir 
   * and adds them to the internal map, which is used later to 
   * print dependencies.
   * @param dirName - directory name
   */
  public void addClassesFromDir(String dirName) {
    File dir = new File(dirName);
    List files = new ArrayList();
    getFiles(dir.getAbsolutePath().length(), dir, files);
    Iterator it = files.iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      name = file2class(name);
      try {
        addClass(name);
      } catch (Throwable e) {
        logger.error("Failed to load class '" + name + "' from dir " + dirName + ":" + e);
      }
    }
  }

  private static String file2class(String name) {
    int n = name.lastIndexOf(".class");
    if (n != -1) name = name.substring(0, n);
    name = name.replace('\\', '.');
    name = name.replace('/', '.');
    return name;
  }
  private void getFiles(int top, File dir, List files) {
    File[] list = dir.listFiles();
    for (int i=0; list != null && i<list.length; i++) {
      if (list[i].isDirectory()) getFiles(top, list[i], files);
      else {
        String name = list[i].getAbsolutePath();
        if (name.endsWith(".class")) {
           name = name.substring(top+1);
           try {
             addClass(name);
           } catch (Throwable e) {
             logger.error("Failed to load file type " + name + ": " + e);
           }
        } else if (name.endsWith(".jar") || name.endsWith(".zip")) {
          if (parseJars) addClassesFromZip(name);
        }
      }
    }
  }


  private ApplicationContext context;
  private boolean parseJars;
  private static final PackageClassMapper instance = new PackageClassMapper(); 
  private Map map;
  private Vector classes;
}
