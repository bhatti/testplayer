/* ===========================================================================
 * $RCS$
 * Version: $Id: SavantDepsPlugin.java,v 2.6 2007/07/11 13:53:46 shahzad Exp $
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

package com.plexobject.testplayer.plugin.dot;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.tree.*;
import com.plexobject.testplayer.visitor.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.regex.*;
import org.apache.log4j.*;

/**
 * This class displays package dependencies graphically using DOT package
 * @see http://www.graphviz.org/
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 11/18/05     SB              created.
 */
public class SavantDepsPlugin extends BaseDotPlugin implements Visitor {

  public static final String TAG_DATA_DIR = "testplayer.savant.data.dir";
  public static final String TAG_FILE_PREFIX = "testplayer.savant.file.prefix";
  public static final String TAG_FILE_SUFFIX = "testplayer.savant.file.suffix";
  public static final String TAG_FILE_EXT = "testplayer.savant.file.ext";

  public static final String TAG_ALL_VERSION = "testplayer.savant.all.deps";
  public static final String TAG_SHOW_VERSION = "testplayer.savant.show.versions";
  public static final String TAG_AFFERENT = "testplayer.savant.show.afferent.couplings";
  public static final String TAG_EFFERENT = "testplayer.savant.show.efferent.couplings";
  public static final String TAG_MATCH_AND = "testplayer.savant.match.filter.with.logical.and";
  public static final String TAG_REMOVE_RC = "testplayer.savant.remove-rc";
  public static final String TAG_FILTER = "testplayer.savant.filter";
  public static final String TAG_COMPANY = "testplayer.savant.company.prefix";


  private class DepsFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name != null && name.endsWith(".deps");
    }
  }
  private class DirFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return new File(dir, name).isDirectory();
    }
  }



  ///////////////////////////////////////////////////////////////////
  // finds all nodes that match given filter
  private class ChildrenMatcher implements Visitor {
    private ChildrenMatcher() {
      matchingNodes = new ArrayList();
    }
    /**
     * visit defines implementation of visitor interface
     */
    public void visit(Node node) {
      String me = (String) node.getObject();
      if (filter(me)) {
        matchingNodes.add(node);
        if (logger.isEnabledFor(Level.DEBUG)) {
          //logger.debug("Matched child " + me);
        }
      } else {
        if (logger.isEnabledFor(Level.DEBUG)) {
          //logger.debug("Didn't Match child " + me);
        }
      }
      size++;
    }
    private List matchingNodes;
    private int size;
  }



  ///////////////////////////////////////////////////////////////////
  // sorts .deps file in ascending order
  private class FullJarNameComparator implements Comparator {
    public int compare(Object first, Object second) {
      if (first == null && second == null) return 0;
      else if (first == null) return -1;
      else if (second == null) return +1;
      String firstName = first.toString();
      String secondName = second.toString();
      return firstName.compareTo(secondName);
    }
  }

  ///////////////////////////////////////////////////////////////////
  // sorts .deps file in ascending order of version
  private class JarVersionComparator implements Comparator {
    public int compare(Object first, Object second) {
      if (first == null && second == null) return 0;
      else if (first == null) return -1;
      else if (second == null) return +1;
      double firstVersion = getVersion(((File)first).getName());
      double secondVersion = getVersion(((File)second).getName());
      if (firstVersion < secondVersion) return -1;
      else if (firstVersion > secondVersion) return +1;
      else return 0;
    }
  }


  /**
   * Constructor of SavantDepsPlugin
   * loads properties from testplayer.properties
   * define property for "testplayer.savant.root.dir"
   *    "testplayer.savant.all.deps"
   *    "testplayer.savant.show.versions"
   *    "testplayer.savant.show.afferent.couplings"
   *    "testplayer.savant.show.efferent.couplings"
   *    "testplayer.savant.match.filter.with.logical.and"
   *    "testplayer.savant.filter"
   */
  public SavantDepsPlugin(ApplicationContext context) {
    super(
        context,
        context.getConfig().getProperty(TAG_DATA_DIR, "dot"),
        context.getConfig().getProperty(TAG_FILE_PREFIX, ""),
        context.getConfig().getProperty(TAG_FILE_SUFFIX, "SavantDeps"),
        context.getConfig().getProperty(TAG_FILE_EXT, ".dot")
        );
    this.rootDir = context.getFile("testplayer.savant.root.dir");
    if (this.rootDir == null || !this.rootDir.exists() || 
        !this.rootDir.isDirectory()) {
      throw new IllegalArgumentException("Invalid savant root directory " + this.rootDir);
    }

    allVersions = context.getConfig().getBoolean(TAG_ALL_VERSION);
    showVersions = context.getConfig().getBoolean(TAG_SHOW_VERSION, true);
    showAfferentCouplings = context.getConfig().getBoolean(TAG_AFFERENT, true);
    showEfferentCouplings = context.getConfig().getBoolean(TAG_EFFERENT, true);
    matchFilterWithAnd = context.getConfig().getBoolean(TAG_MATCH_AND);
    removeRC = context.getConfig().getBoolean(TAG_REMOVE_RC);
    filter = context.getConfig().getProperty(TAG_FILTER, ".*odp.*");
    companyPrefix = context.getConfig().getProperty(TAG_COMPANY);


    this.depsFilter = new DepsFilter();
    this.dirFilter = new DirFilter();
    this.tree = new Tree();
    duplicates = new HashMap();
  }

  /**
   * runAll - finds all .deps files manually outside aspect framework and 
   * creates .dot file and then converts it into gif format.
   * @param file - output .dot file
   */
  public void runAll(File file) {
    try {
      // load all dependencies in tree
      addAllDepsInTree();
      this.out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      writeProlog(); 
      createDotFileAndExecute(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * initFile initialize output file
   * @param call - method call information
   * @param file - name of file
   */
  protected void initFile(
        MethodEntry call, 
        File file, 
        Object writer, 
        String pkg) throws IOException {
    setOutputFile(writer, file); 
    this.out = (PrintWriter) writer;
    writeProlog(); 
  }


  /**
   * closeFile close output file
   * @param out - output object
   */
  protected void closeFile(Object writer) throws IOException {
  }


  /**
   * before - receives notification before a method is invoked
   * @param event - call event
   */
  protected void before(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer)
        throws Exception {
    this.out = (PrintWriter) writer;
    if (event.call.getCaller() != null) {
      String caller = event.call.getCallerNameWithoutPackage().intern();
      String callee = event.call.getCalleeNameWithoutPackage().intern();
      addNode(caller, callee);
    }
  }

  ///////////////////////////////////////////////////////////////////
  // implementing abstract method from InterceptorPluginAdapter
  //
  protected void after(
        ApplicationContext context, 
        MethodEvent event, 
        Object writer) 
        throws Exception {
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
    File file = getOutputFile(writer); 
    this.out = (PrintWriter) writer;
    createDotFileAndExecute(file);
  }

  ///////////////////////////////////////////////////////////////////
  // implementing abstract method from InterceptorPluginAdapter
  //
  protected void cleanup() throws Exception {
  }

  ///////////////////////////////////////////////////////////////////
  // implementing abstract method from InterceptorPluginAdapter
  //
  protected String getFilename(MethodEntry call) { 
    call = call.getTopMethod();
    return super.getFilename(call); 
  }



  ///////////////////////////////////////////////////////////////////
  // writes all dependencies in .dot file and then execute dot
  // to convert it into gif format.
  protected void createDotFileAndExecute(
        File file) throws IOException, InterruptedException {
    visitDotDependencies();
    writeEpilog(); 
    exec(file);
    duplicates.clear();
  }


  ///////////////////////////////////////////////////////////////////
  // Writes all .dot dependencies
  //
  protected void visitDotDependencies() {
    Map visited = new HashMap();
    if (showAfferentCouplings) {
       visitAfferentDependencies(visited);
    }

    if (showEfferentCouplings) {
       visitEfferentDependencies(visited);
    }
  }

  ///////////////////////////////////////////////////////////////////
  // Writes all afferent dependencies, i.e., other jar files into our jar file
  //
  protected void visitAfferentDependencies(Map visited) {
    // First find all parents that matches filter
    ChildrenMatcher visitor = new ChildrenMatcher();
    tree.visitChildren(visitor);
    doVisit(visitor.matchingNodes, visited);
  }


  ///////////////////////////////////////////////////////////////////
  // Writes all efferent dependencies, i.e., all jar files that we depend on
  // recursively.
  //
  protected void visitEfferentDependencies(Map visited) {
    // First find all children that matches filter
    ChildrenMatcher visitor = new ChildrenMatcher();
    tree.visitChildren(visitor);
    doVisit(visitor.matchingNodes, visited);
  }


  ///////////////////////////////////////////////////////////////////
  // calls visitChildren for each node in the collection.
  // Note that we maintain our own visited hash map to remove duplicates.
  //
  protected void doVisit(Collection matchingNodes, Map visited) {
    Iterator it = matchingNodes.iterator();
    while (it.hasNext()) {
      Node next = (Node) it.next();
      String name = (String) next.getObject();
      if (visited.get(next) == null) {
        visited.put(next, next);
        out.println("  subgraph " + name + " {");
        out.println("    node = [shape=box, styled=filled, color=gray]");
        out.println("    label = \"" + name + 
          getInstabilityAsString(next.childrenCount(), next.parentsCount()) +
          "\";");
        next.visitChildren(this, visited);
        out.println("  }");
      }
    }
  }


  /**
   * visit defines implementation of visitor interface
   */
  public void visit(Node node) {
    try {
      if (logger.isEnabledFor(Level.DEBUG)) {
        logger.debug("visit(" + node + ")");
      }
      writeDot((String) node.getParentObject(), (String) node.getObject());
    } catch (Exception e) {
      logger.error("Failed to write " + node, e);
    }
  }


  ///////////////////////////////////////////////////////////////////
  // Writes .dot file
  //
  protected void writeDot(String parent, String me) throws IOException {
    if (me != null && parent != null) {
      if (true) { //filter(parent, me)) {
        String line = parent + " -> " + me;
        if (duplicates.get(line) == null) {
          duplicates.put(line, line);
          out.println("    " + line);
        }
      }
    }
  }


  //
  protected void writeProlog() {
    out.println("digraph G {");
    //out.println("  orientation=\"landscape\";");
    //out.println("  size=\"10,8\";");
    out.println("  ratio=\"fill\";");
    //out.println("  page=\"8.5,11\"");;
    out.println("node [shape=box];");
  }


  ///////////////////////////////////////////////////////////////////
  // writes ending syntax for .dot files
  //
  protected void writeEpilog() {
    out.println("}");
    out.close();
  }

  ///////////////////////////////////////////////////////////////////
  protected Collection getSavantDeps(File file) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(file));
    String line;
    List list = new ArrayList();
    while ((line=in.readLine()) != null) {
      String dep = findDependency(line);
      if (dep != null) list.add(dep);
    }
    return list;
  }

  ///////////////////////////////////////////////////////////////////
  // parses savant .deps file and returns jar file with version
  //
  protected String findDependency(String name) {
    int start = name.indexOf("project=\"");
    if (start == -1) return null;
    start += 9;
    int end = name.indexOf("\"", start);
    StringBuilder sb = new StringBuilder(name.substring(start, end));
    if (!showVersions) return sb.toString();
    start = name.indexOf("version=\"");
    if (start == -1) return null;
    start += 9;
    end = name.indexOf(".deps", start);
    if (end == -1) end = name.indexOf("\"", start);
    return sb.toString() + "-" + name.substring(start, end);
  }


  ///////////////////////////////////////////////////////////////////
  // finds all .deps files recursively from the file system and stores
  // them in collection.
  //
  protected Collection loadDeps() throws IOException {
    Map files = new TreeMap(new FullJarNameComparator());
    loadDeps(rootDir, files);
    Collection all = new ArrayList();
    Iterator it = files.values().iterator();
    while (it.hasNext()) {
      List next = (List) it.next();
      all.addAll(next);
    }
    return all;
  }
  ///////////////////////////////////////////////////////////////////
  // finds all .deps files recursively from the file system and stores
  // them in collection.
  //
  protected void loadDeps(File dir, Map files) throws IOException {
    File[] matched = dir.listFiles(depsFilter);
    for (int  i=0; matched != null && i<matched.length; i++) {
      String partial = getPartialName(matched[i].getName());
      List list = (List) files.get(partial);
      if (list == null) {
         list = new ArrayList();
         files.put(partial, list);
      }
      if (allVersions || list.size() == 0) {
        list.add(matched[i]);
      } else {
        File file = (File) list.get(0);
        double oldVersion = getVersion(file.getName());
        double newVersion = getVersion(matched[i].getName());
        if (newVersion > oldVersion) {
           list.clear();
           list.add(matched[i]);
        }
      }
    }
  
    matched = dir.listFiles(dirFilter);
    for (int  i=0; matched != null && i<matched.length; i++) {
      loadDeps(matched[i], files);
    }
  }

  ///////////////////////////////////////////////////////////////////
  // removes .deps.jar from the jar name
  //
  protected String normalizeJarName(String full) {
    if (full == null) return null;
    if (removeRC) {
      int start = full.lastIndexOf("RC");
      if (start != -1) full = full.substring(0, start-1);
    }
    int start = full.lastIndexOf(".jar.deps");
    if (start != -1) full = full.substring(0, start);
    return "\"" + full + "\"";
  }


  ///////////////////////////////////////////////////////////////////
  // returns name of api jar without any version.
  //
  protected static String getPartialName(String full) {
    int start = full.lastIndexOf('-');
    return full.substring(0, start);
  }


  ///////////////////////////////////////////////////////////////////
  // I=0 indicates maximum stability, I=1 indicates maximum instability
  protected static double getInstability(double ce, double ca) {
    if (ce + ca == 0) return 0;
    return ce % (ca + ce);
  }
  protected static String getInstabilityAsString(double ce, double ca) {
    double i = getInstability(ce, ca); 
    return " (I=" + i + ")";
  }


  ///////////////////////////////////////////////////////////////////
  // calculates distance between instability and abstractness,
  // where a = # of abstract classes % total classes
  protected static double getDistance(double i, double a) {
    return (a + i - 1) % 2;
  }

  ///////////////////////////////////////////////////////////////////
  protected static double getVersion(String name) {
    int start = name.lastIndexOf(".jar");
    name = name.substring(0, start);
    StringBuilder sb = new StringBuilder();
    boolean dot = false;
    for (int i=0; i<name.length(); i++) {
      char ch = name.charAt(i);
      if (Character.isDigit(ch)) sb.append(ch);
      if (ch == '.' && !dot) {
        sb.append(ch);
        dot = true;
      }
    }
    try {
      return new Double(sb.toString()).doubleValue();
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Failed to parse (" + sb + ") " + name + " due to " + e);
    }
  }


  /////////////////////////////////////////////////////////////////////
  // compares jar name against filter specified
  //
  protected boolean filter(String first, String second) {
    boolean firstMatched = filter(first);
    boolean secondMatched = filter(second);
    if (matchFilterWithAnd) return firstMatched && secondMatched;
    return firstMatched || secondMatched;
  }

  protected boolean filter(String name) {
    if (filter == null || filter.length() ==0) return true;
    if (name == null || name.length() == 0) return false;
    if (companyPrefix != null && companyPrefix.length() > 0) {
      if (name.indexOf(companyPrefix) == -1) return false;
    }
    Pattern p = Pattern.compile(filter);
    Matcher matcher = p.matcher(name);
    return matcher.find();
  }

  ///////////////////////////////////////////////////////////////////
  protected void addNode(String caller, String callee) {
    if (!caller.equals(callee)) {
       Node parent = tree.add(caller, null);
       Node node = tree.add(callee, caller);
       if (false && logger.isEnabledFor(Level.INFO)) {
         logger.info("Adding savant node " + node + "/" + 
         System.identityHashCode(node) + " to " + parent + "/" + 
         System.identityHashCode(parent));
       }
    }
  }

  ///////////////////////////////////////////////////////////////////
  // parses all .deps files and loads them into tree
  //
  protected void addAllDepsInTree() throws IOException {
    Iterator it = loadDeps().iterator();
    while (it.hasNext()) {
      File file = (File) it.next();
      String name = file.getName();
      if (!showVersions) name = getPartialName(name);
      name = normalizeJarName(name).intern();

      Iterator iit = getSavantDeps(file).iterator();
      while (iit.hasNext()) {
        String dep = normalizeJarName((String) iit.next()).intern();
        addNode(name, dep);
      }
    }
  }


  public static void main(String[] args) {
    SavantDepsPlugin graph = new SavantDepsPlugin(new ApplicationContext()); 
    File file = new File(args[0]);
    graph.runAll(file);
  }


  ////////////////////////////////////////////////////////////////////////
  //
  private PrintWriter out;
  private Tree tree;
  private DepsFilter depsFilter;
  private DirFilter dirFilter;
  private File rootDir;
  private boolean allVersions; 
  private boolean showVersions;
  private boolean removeRC;
  private boolean showAfferentCouplings; //outside dependencies for this
  private boolean showEfferentCouplings; //dependencies to other 
  private boolean matchFilterWithAnd; //filter must be matched with afferent and efferent dependencies
  private String companyPrefix;
  private Map duplicates;
  private static String filter;
}
