/* ===========================================================================
 * $RCS$
 * Version: $Id: WikiTableSetupFixture.java,v 2.3 2006/02/25 20:50:44 shahzad Exp $
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

package com.plexobject.testplayer.fitnesse;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.marshal.*;
import java.io.*;
import java.text.*;
import java.util.*;
import fitlibrary.*;
import fit.*;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;
import com.thoughtworks.xstream.io.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;


/**
 * This class wrapps WikiTableSetupFixture
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 12/9/05      SB              created.
 */
public class WikiTableSetupFixture extends SetUpFixture {
  /**
   * WikiTableSetupFixture constructor
   * @param rows - array of objects
   * @param type - type
   */
  public WikiTableSetupFixture() {
    this.buffer = new StringWriter();
    this.writer = new PrettyPrintWriter(buffer);
  }

  /**
   * begin - marks beginning of XML node
   * @param name - name of start node
   */
  public void begin(String name) {
    writer.startNode(name);
  }

  /**
   * end - marks ending of XML node
   * @param name - name of end node
   */
  public void end(String name) {
    writer.endNode();
  }

  /**
   * This is used to store short name/value XML pair
   * @param name - name of node
   * @param value - value of node
   */
  public void nodeValue(String name, String value) {
    begin(name);
    value(value);
    end(name);
  }


  /**
   * This is used to store value XML pair
   * @param value - value of node
   */
  public void value(String value) {
    writer.setValue(value);
  }

  /**
   * This is used to store attribute of xml node
   * @param name - name of attribute
   * @param value - value of attribute
   */
  public void attributeValue(String name, String value) {
    writer.addAttribute(name, value);
  }

  /**
   * Builds object from the XML
   * @return deserialized object
   */
  public Object getObject() throws Exception {
    XStream xstream = new XStream();
    try {
      return xstream.fromXML(new StringReader(buffer.toString()));
    } catch (RuntimeException e) {
      throw new MarshallingException("Failed to parse (" + buffer.toString().replace('<', '(').replace('>', ')'), e);
    }
  }

  private StringWriter buffer;
  private PrettyPrintWriter writer;
}

