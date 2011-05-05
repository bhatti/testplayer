/* ===========================================================================
 * $RCS$
 * Version: $Id: Node.java,v 2.4 2006/09/02 14:38:36 shahzad Exp $
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

package com.plexobject.testplayer.tree;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.visitor.*;
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class is used to build tree of objects.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/7/05       SB              created.
 */
public class Node implements Visitable {
  private static class IntegerHolder {
    public int number;
  }
  public Node() {
    this(null, null, null);
  }

  /**
   * Node initializes object record upon entry to the method
   * @param object - object record
   */
  public Node(Object object, Node parent, List children) {
    this.object = object;
    this.parent = parent;
    this.children = children != null ? children : new ArrayList();
  }

  /**
   * @return - stringified version of this object
   */
  public String toString() {
    return object != null ? object.toString() : "root";
  }


  /**
   * @return - overrides hashcode - it uses id to create hashcode
   */
  public int hashCode() {
    return object != null ? object.hashCode() : Integer.MIN_VALUE;
  }


  /**
   * @return - true if other object is same as this 
   */
  public boolean equals(Object o) {
    if (o == null || o instanceof Node == false) return false;
    if (o == this) return true;
    Node other = (Node) o;
    if (other.object == object) return true;
    if (other.object == null || object == null) return false;
    return other.object.equals(object);
  }


  /**
   * @return  - parent for this method invocation
   * @uml.property  name="parent"
   */
  public Node getParent() {
    return parent;
  }



  /**
   * @return  - parent object
   */
  public Object getParentObject() {
    if (parent == null) return null;
    return parent.getObject();
  }



  /**
   * @param parent  The parent to set.
   * @uml.property  name="parent"
   */
  public void setParent(Node parent) {
    this.parent = parent;
  }

  /**
   * @return - returns object object
   */
  public Object getObject() {
    return object;
  }


  /**
   * @param object - object to be saved in node.
   */
  public void setObject(Object object) {
    this.object = object;
  }


  /**
   * @return  - children of this node -- that is the objects that this 
   * method makes in sequential order
   * @uml.property  name="children"
   */
  public List getChildren() {
    return children;
  }


  /**
   * @returns children values.
   */
  public List getChildrenValues() {
    List list = new ArrayList();
    Iterator it = iterator();
    while (it.hasNext()) {
      Node next = (Node) it.next();
      list.add(next.object);
    }
    return list;
  }


  /**
   * visit all children recursively
   * @param visitor - implementation of visitor interface
   */
  public void visitChildren(Visitor visitor) {
    Map visited = new HashMap();
    visitChildren(visitor, visited);
  }


  /**
   * visit all children recursively
   * @param visitor - implementation of visitor interface
   * @param visited - all nodes that have been visited in case of cyclical
   *    relationships.
   */
  public void visitChildren(Visitor visitor, Map visited) {
    //doVisit(visitor, this, visited);
    List copy = null;
    synchronized (children) {
      copy = new ArrayList(children);
    }
    try {
      synchronized (visited) {
        Iterator it = copy.iterator();
        while (it.hasNext()) {
          Node next = (Node) it.next();
          doVisit(visitor, next, visited);
          next.visitChildren(visitor, visited);
        }
      }
    } catch (ConcurrentModificationException e) {}
  }



  /**
   * visit all children whose parent is the this node
   * @param visitor - implementation of visitor interface
   */
  public void visitParents(final Visitor visitor) {
    Node p = getParent();
    while (p != null) {
      visitor.visit(p);
      p = p.getParent();
    }

/*
    Visitor proxyVisitor = new Visitor() {
      public void visit(Node node) {
        if (node.parent == Node.this) visitor.visit(node);
      }
    };
    visitChildren(proxyVisitor);
*/
  }


  private static void doVisit(Visitor visitor, Node node, Map visited) {
    if (node == null || node.getObject() == null || visited.get(node.getObject()) != null) return;
    visitor.visit(node);
    visited.put(node.getObject(), node.getObject());
  }


  /**
   * @return - returns iterator for all children.
   */
  public Iterator iterator() {
    return children.iterator();
  }


  /**
   * @param - list of children
   */
  public void setChildren(List children) {
    this.children = children;
  }


  /**
   * @param node - node
   */
  public void addChild(Node node) {
    if (this.children.indexOf(node) == -1) this.children.add(node);
  }

  /**
   * @param - list of children
   */
  public Object getAttribute(Object key) {
    return attributes.get(key);
  }


  /**
   * sets user-defined attributes on this node in addition to node value
   * @param key - key
   * @param value - value
   */
  public void setAttribute(Object key, Object value) {
    attributes.put(key, value);
  }

  /**
   * @return - number of children 
   */
  public int childrenCount() {
    //return children.size();
    final IntegerHolder size = new IntegerHolder();
    Visitor proxyVisitor = new Visitor() {
      public void visit(Node node) {
        size.number++;
      }
    };
    visitChildren(proxyVisitor);
    return size.number;
  }
 
  /**
   * @return - number of parents
   */
  public int parentsCount() {
    int count = 0;
    Node p = getParent();
    while (p != null) {
      count++;
      p = p.getParent();
    }
/*
    final IntegerHolder size = new IntegerHolder();
    Visitor proxyVisitor = new Visitor() {
      public void visit(Node node) {
        if (node.parent == Node.this) size.number++;
      }
    };
    visitChildren(proxyVisitor);
    return size.number;
*/
    return count;
  }

  /**
   * @return - finds node with given id among its children
   */
  public Node find(Object rec) {
    if (rec == null) return null;
    if (rec == this.object || rec.equals(this.object)) return this;
    Iterator it = iterator();
    while (it.hasNext()) {
      Node next = (Node) it.next();
      Node child = next.find(rec);
      if (child != null) return child;
    }
    return null;
  }


  /**
   * accepts visitor 
   * @param visitor - interface implementing visitor 
   */
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  private Object object;
  private Node parent;
  private List children;
  private Map attributes = new HashMap();
}

