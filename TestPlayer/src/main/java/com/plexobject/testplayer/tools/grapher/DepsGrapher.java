/* ===========================================================================
 * $RCS$
 * Version: $Id: DepsGrapher.java,v 1.2 2006/08/14 03:05:42 shahzad Exp $
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

package com.plexobject.testplayer.tools;
import com.plexobject.testplayer.util.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.io.*;
import org.apache.log4j.*;

/**
 * <B>CLASS COMMENTS</B>
 * Class Name: DepsGrapher
 * Class Description: 
 *   DepsGrapher draws compile time class-level or package level dependencies
 *   using DOT package.
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 * Modification History
 * Initial      Date            Changes
 * SAB          Jan. 18, 2006   Created
*/

public class DepsGrapher {
  private static final String DISALLOWED_PACKAGES = "DISALLOWED_PACKAGES";
  private static final String[] SUN_PACKAGES = {
    "java.",
    "javax.",
    "sun.",
    "com.sun.",
    "org.omg."
  };
  //
  private static final String[] SUN_CLASSES = {
    "boolean",
    "byte",
    "char",
    "float",
    "double",
    "int",
    "long",
    "short",
    "I",
    "B",
    "C",
    "F",
    "D",
    "J",
    "S",
    "V",
    "Z",
    "void"
  };


  /**
   * Default constructor
   */
  public DepsGrapher() {
    this(new ArrayList(), Arrays.asList(SUN_PACKAGES), false, false);
  }


  /**
   * DepsGrapher constructor
   * @param mustPackages - list of package names that must appear
   * @param skipPackages - list of package names that must not appear
   * @param recursive - whether to recursively find dependencies of all classes
   * @param showOnlyPackageNames - whether to show only package names
   */
  public DepsGrapher(List mustPackages, List skipPackages, boolean recursive, boolean showOnlyPackageNames) {
    setMustPackages(mustPackages);
    setSkipPackages(skipPackages);
    setRecursive(recursive);
    setShowOnlyPackageNames(showOnlyPackageNames);
    processed = new ArrayList();
    dependencies = new HashMap();
  }


  /**
   * @return mustPackages
   */
  public List getMustPackages() {
    return this.mustPackages;
  }

  /**
   * @param mustPackages
   */
  public void setMustPackages(List mustPackages) {
    if (mustPackages == null) throw new IllegalArgumentException("Unspecified mustPackages");
    this.mustPackages = mustPackages;
  }

  /**
   * @return skipPackages
   */
  public List getSkipPackages() {
    return this.skipPackages;
  }

  /**
   * @param skipPackages
   */
  public void setSkipPackages(List skipPackages) {
    if (skipPackages == null) throw new IllegalArgumentException("Unspecified skipPackages");
    this.skipPackages = skipPackages;
  }

  /**
   * @return processed
   */
  public List getProcessed() {
    return this.processed;
  }

  /**
   * @param processed
   */
  public void setProcessed(List processed) {
    if (processed == null) throw new IllegalArgumentException("Unspecified processed");
    this.processed = processed;
  }

  /**
   * @return recursive
   */
  public boolean isRecursive() {
    return this.recursive;
  }

  /**
   * @param recursive
   */
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  /**
   * @return dependencies
   */
  public Map getDependencies() {
    return this.dependencies;
  }

  /**
   * @param dependencies
   */
  public void setDependencies(Map dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * @return showOnlyPackageNames
   */
  public boolean isShowOnlyPackageNames() {
    return this.showOnlyPackageNames;
  }

  /**
   * @param showOnlyPackageNames
   */
  public void setShowOnlyPackageNames(boolean showOnlyPackageNames) {
    this.showOnlyPackageNames = showOnlyPackageNames;
  }

  public String toString() {
    return super.toString()
     + ",mustPackages=" + mustPackages
     + ",skipPackages=" + skipPackages
     + ",processed=" + processed
     + ",recursive=" + recursive
     + ",dependencies=" + dependencies
     + ",showOnlyPackageNames=" + showOnlyPackageNames;
  }

  /**
   * This method print dependencies in DOT format
   * @param fileName
   */
  public void printDeps(final String fileName) throws IOException {
    File file = new File(fileName);
    PrintStream out = new PrintStream(new FileOutputStream(file));
    printDeps(out);
    out.close();
  }
 
  /**
   * This method print dependencies in DOT format
   * @param out - output stream
   */
  public void printDeps(PrintStream out) {
    Map duplicates = new HashMap();
    out.println("digraph G {");
    //out.println("  orientation = landscape;");
    out.println("  size=\"10,8\";");
    out.println("  page=\"8.5,11\";");
    out.println("  ratio=\"fill\";");
    out.println("node [shape=box];");
    Iterator it = dependencies.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      if (showOnlyPackageNames)  key = getPackageName(key);
      String[] depend = (String[]) dependencies.get(key);
      for (int i=0; depend != null && i<depend.length; i++) {
        if (acceptClass(null, depend[i]))  {
          if (showOnlyPackageNames) depend[i] = getPackageName(depend[i]);
          String line = "\"" + key + "\"" + " -> " + "\"" + depend[i] + "\"";
          if (duplicates.get(line) == null) {
             duplicates.put(line, Boolean.TRUE);
             out.println(line);
          }
        }
      }
    }
    out.println("}");
  }

  /**
   * This method find dependent types for given class name and adds them to the
   * internal map, which is used later to print dependencies.
   * @param klass - type name
   */
  public void addClassDeps(String klass) {
    if (processed.indexOf(klass) != -1) {
      if (logger.isEnabledFor(Level.DEBUG)) {
        logger.debug("addClassDeps(" + klass + ") already processed");
      }
      return;
    }
    processed.add(klass);
    if (logger.isEnabledFor(Level.DEBUG)) {
      logger.debug("addClassDeps(" + klass + ") processing...");
    }


    if (!isSecurityManagerLoaded && System.getSecurityManager() != null) {
      if (logger.isEnabledFor(Level.INFO)) {
         logger.info("addClassDeps(" + klass + ") found security manager");
      }
      isSecurityManagerLoaded = true;
    }

    if (acceptClass(null, klass)) {
      String[] deps = getDepends(klass);
      if (logger.isEnabledFor(Level.INFO)) {
        logger.info("addClassDeps(" + klass + ") adding " + deps.length);
      }
      for (int i=0; deps != null && i<deps.length; i++) {
        if (logger.isEnabledFor(Level.DEBUG)) {
          logger.debug("addClassDeps(" + klass + ") adding " + deps[i]);
        }
      }
      dependencies.put(klass, deps);
      if (recursive) {
        if (logger.isEnabledFor(Level.DEBUG)) {
          logger.debug("addClassDeps(" + klass + ") will recursively check " + deps.length + " classes");
        }
        for (int i=0; deps != null && i<deps.length; i++) {
          addClassDeps(deps[i]);
        }
      } 
    } else {
      if (logger.isEnabledFor(Level.DEBUG)) {
        logger.debug("addClassDeps(" + klass + ") rejected");
      }
    }
  }


  /**
   * This method find dependent types for all classes in given zip 
   * and adds them to the internal map, which is used later to 
   * print dependencies.
   * @param zip - zip file name
   */
  public void addZipDeps(String zip) {
    JarResources jr = new JarResources(zip);
    String[] names = jr.getResourceNames();
    for (int i=0; i<names.length; i++) {
      if (names[i].endsWith(".class")) addClassDeps(file2class(names[i]));
    }
  }


  /**
   * This method find dependent types for all classes in given classes dir 
   * and adds them to the internal map, which is used later to 
   * print dependencies.
   * @param dirName - directory name
   */
  public void addDirDeps(String dirName) {
    File dir = new File(dirName);
    List files = new ArrayList();
    getFiles(dir.getAbsolutePath().length(), dir, files);
    Iterator it = files.iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      addClassDeps(file2class(name));
    }
  }


  /**
   * This method finds name of all dependent classes for given type
   * @param typeName - type name of class
   * @return names of classes that given class depends on
   */
  public String[] getDepends(final String typeName) {
    List list = new ArrayList();
    try {
      ///
      Class klass = Class.forName(typeName);
      Class type = TypesExtractor.getComponentType(klass);

      String[] reflTypes = TypesExtractor.extractTypesUsingReflection(type);
      for (int i=0; i<reflTypes.length; i++) {
        if (acceptClass(typeName, reflTypes[i]) && 
            list.indexOf(reflTypes[i]) == -1) {
          list.add(reflTypes[i]);
        }
      }

      List extracted = new ArrayList();
      TypesExtractor.extractTypesUsingJavap(type, extracted); 
      Iterator it = extracted.iterator();
      while (it.hasNext()) {
        String extype = (String) it.next();
        if (acceptClass(typeName, extype) && list.indexOf(extype) == -1) {
          list.add(extype);
        }
      }
      String[] listTypes = TypesExtractor.extractTypesUsingListing(type);
      for (int i=0; i<listTypes.length; i++) {
        if (acceptClass(typeName, listTypes[i]) && 
          list.indexOf(listTypes[i]) == -1) {
          list.add(listTypes[i]);
        }
      }
    } catch (java.lang.ClassNotFoundException e) {
      System.err.println("Failed to add (" + typeName + ") " + e);
      return new String[0];
    } catch (java.lang.NoClassDefFoundError e) {
      System.err.println("Failed to add (" + typeName + ") " + e);
      return new String[0];
    } catch (Throwable e) {
      e.printStackTrace();
      return new String[0];
    }

    String[] depsArray = new String[list.size()];
    Iterator it = list.iterator();
    int i=0;
    while (it.hasNext()) {
      String next = (String) it.next();
      depsArray[i] = next.replace('$', '.');
      i++;
    }
    Arrays.sort(depsArray);
    return depsArray;
  }


  private boolean includes(List list, String pattern) {
    Iterator it = list.iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      if (pattern.indexOf(name) != -1) return true;
    }
    return false;
  }


  private boolean acceptClass(String originalType, String depType) {
    if (includes(skipPackages, depType)) return false;
    if (includes(Arrays.asList(SUN_CLASSES), depType)) return false;
    if (mustPackages.size() > 0 && !includes(mustPackages, depType)) return false;

    int n = depType.lastIndexOf('$');
    if (n != -1 && skipInner) return false;

    // skip anonymous classes
    if (n != -1 && Character.isDigit(depType.charAt(n+1))) {
      return false;
    }
    if (originalType != null && originalType.equals(depType)) return false;
    return true;
  }


  private void getFiles(int top, File dir, List files) {
    File[] list = dir.listFiles();
    for (int i=0; list != null && i<list.length; i++) {
      if (list[i].isDirectory()) getFiles(top, list[i], files);
      else {
        String name = list[i].getAbsolutePath();
        if (name.endsWith(".class")) files.add(name.substring(top+1));
        else if (name.endsWith(".jar")) addZipDeps(name);
      }
    }
  }

  private static String file2class(String name) {
    int n = name.indexOf(".class");
    if (n != -1) name = name.substring(0, n);
    name = name.replace('\\', '.');
    name = name.replace('/', '.');
    return name;
  }

  private String getPackageName(String name) {
    int n = name.lastIndexOf('.');
    if (n != -1) name = name.substring(0, n);
    return name;
  }


  ////////////////////////////////////////////////////////////////////////
  private static void usage() {
    System.err.println("Usage: java " + DepsGrapher.class.getName() + 
        " -m [list of packages/class-names that must match] -s [list of packages/class-names that must not match] -r -o output-dot-file classes/packages/dirs");
    System.err.println("If -r is specified, DepsGrapher will recursively find dependent classes for all types");
    System.exit(1);
  }


  public static void main(String[] args) throws Exception {
    DepsGrapher grapher = new DepsGrapher();
    String fileName = null;
    for (int i=0; i<args.length; i++) {
      File file = new File(args[i]);
      if (args[i].equals("-m")) {
         String[] t = StringHelper.split(args[++i], " ,;");
         for (int j=0; j<t.length; j++) grapher.mustPackages.add(t[j]);
      } else if (args[i].equals("-s")) {
         String[] t = StringHelper.split(args[++i], " ,;");
         for (int j=0; j<t.length; j++) grapher.skipPackages.add(t[j]);
      } else if (args[i].equals("-r")) {
         grapher.recursive = true;
      } else if (args[i].equals("-p")) {
         grapher.showOnlyPackageNames = true;
      } else if (args[i].equals("-h")) {
         usage();
      } else if (args[i].equals("-o")) {
         fileName = args[++i];
      } else if (args[i].endsWith(".jar") || args[i].endsWith(".zip")) {
         grapher.addZipDeps(args[i]);
      } else if (file.exists() && file.isDirectory()) {
         grapher.addDirDeps(args[i]);
      } else if (file.exists() && file.isFile()) {
         grapher.addClassDeps(file2class(args[i]));
      } else {
         grapher.addClassDeps(args[i]);
      }
    }
    if (fileName == null) grapher.printDeps(System.out);
    else grapher.printDeps(fileName); 
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////
  // Attributes
  private List mustPackages;
  private List skipPackages;
  private List processed;
  private boolean recursive;
  private Map dependencies;
  private boolean showOnlyPackageNames;
  private boolean skipInner;
  private boolean isSecurityManagerLoaded;
  private transient Logger logger = Logger.getLogger(DepsGrapher.class.getName());
}






