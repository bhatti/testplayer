/* ===========================================================================
 * $RCS$
 * Version: $Id: Tree.java,v 2.4 2006/09/02 14:38:36 shahzad Exp $
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
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.visitor.*;
import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This class is used to build tree of objects 
 * Though, technically tree does not allow nodes to be shared across multiple
 * parents, otherwise it would be called graph. However, there is no 
 * such contraint in this class.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/7/05       SB              created.
 */
public class Tree {
  /**
   * Tree constructor
   */
  public Tree() {
  }

  /**
   * @return - stringified version of this object
   */
  public String toString() {
    return root.toString();
  }


  /**
   * @return - true if other object is same as this 
   */
  public boolean equals(Object o) {
    if (o == null || o instanceof Tree == false) return false;
    return o == this;
  }


  /**
   * addNode - add object 
   * @param object - object to add
   */
  public Node add(Object object, Object parentObject) {
    Node parent = parentObject != null ? find(parentObject) : root;
    if (parent == null) parent = root;

    Node node = find(object);
    if (node != null) {
       parent.addChild(node);
       return node;
    }
    //
    node = new Node(object, parent, null);
    parent.addChild(node);
    if (logger.isEnabledFor(Level.DEBUG)) {
      if (parent == root) logger.debug("###### added " + object + " to root|" + parent.childrenCount());
      else logger.debug("###### added " + object + " to parent " + parent.getObject() + "|" + parent.childrenCount());
    }
    return node;
  }




  /**
   * visit all children recursively
   * @param visitor - implementation of visitor interface
   */
  public void visitChildren(Visitor visitor) {
    root.visitChildren(visitor);
  }


  // visit all children whose parent is the root node
  public void visitParents(Visitor visitor) {
    root.visitParents(visitor);
  }

  /**
   * @return - returns iterator for all children.
   */
  public Iterator iterator() {
    return root.iterator();
  }


  /**
   * @param - list of children
   */
  public int childrenCount() {
    return root.childrenCount();
  } 

  /**
   * @return - finds node with given id
   */
  public Node find(Object object) {
    return root.find(object);
  }

  public static void main(String[] args) {
    Tree tree = new Tree();
    tree.add("1", null);
    tree.add("1.1", "1");
    tree.add("1.2", "1");
    tree.add("1.3", "1");
    tree.add("1.1.1", "1.1");
    tree.add("1.1.2", "1.1");
    tree.add("1.1.3", "1.1");
    tree.add("1.2.1", "1.2");
    tree.add("1.2.2", "1.2");
    tree.add("1.2.3", "1.2");
    tree.add("1.4", "1");
    tree.add("1.4.1", "1.4");
    tree.add("1.4.1.1", "1.4.1");
    tree.add("1.5", "1");
    tree.add("1.5.1", "1.5");
    tree.visitChildren(new Visitor() {
      public void visit(Node node) {
        System.out.println(node.getObject() + ", parents " + node.parentsCount());
      }
    });
    System.out.println("childrenCount " + tree.childrenCount());
  }
  private Node root = new Node();
  private static Logger logger = Logger.getLogger(Tree.class.getName());
}
