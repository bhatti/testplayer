/* ===========================================================================
 * $RCS$
 * Version: $Id: WikiTableDriver.java,v 2.3 2006/02/25 20:50:44 shahzad Exp $
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
 * This class wrapps WikiTableWriter
 *
 * @author shahzad bhatti
 * 
 * Example:
 *
 * XStream xstream = new XStream(new WikiTableDriver());
 * xstream.alias("person", Person.class);
 * xstream.alias("phonenumber", PhoneNumber.class);
 * Person joe = new Person("Joe", "Walnes");
 * String xml = xstream.toXML(map);
 * joe = (Person) xstream.fromXML(xml);
 *
 * modification history
 * date         who             what
 * 1/9/06       SB              created.
 */
public class WikiTableDriver extends DomDriver {
  public static final String DELIM = "|";
  public static final String QUOTES = "''";


  public static final String NODE = "node";
  public static final String SET_NODE_VALUE = NODE + QUOTES + DELIM + QUOTES + "value";
  public static final String START_NODE = "begin";
  public static final String SET_VALUE = "value";
  public static final String END_NODE = "end";
  public static final String ATTRIBUTE = "attribute"; 
  public static final String ADD_ATTRIBUTE = ATTRIBUTE + QUOTES + DELIM + QUOTES + "value";

  public static final int STATE_BEGIN = 0;
  public static final int STATE_NODE_VALUE = 1;
  public static final int STATE_VALUE = 2;
  public static final int STATE_ATTRIBUTE = 3;
  public static final int STATE_END = 4;
  
  /**
   * WikiTableDriver constructor
   */
  public WikiTableDriver() {
  }


  /**
   * Converts xml into fitnesse based tables
   * @param xml 
   * @return fitnesse table
   */
  public static String tableToXml(String xml) throws IOException {
    return tableToXml(new StringReader(xml));
  }


  /**
   * Converts fitnesse based tables into xml
   * @param fitnesse table
   * @return xml 
   */
  public static String tableToXml(Reader xml) throws IOException {
    StringWriter buffer = new StringWriter();
    PrettyPrintWriter writer = new PrettyPrintWriter(buffer);
    BufferedReader in = new BufferedReader(xml);
    String line;
    int state = -1;
    int lineno = 0;
    while ((line=in.readLine()) != null) {
      lineno++;
      line = StringHelper.replace(line, QUOTES, "");
      String[] t = line.split("\\" + DELIM);
      if (t.length < 2) {
         continue;
      }
      if (state < 0) {
        if (START_NODE.equals(t[1])) {
           if (t.length > 2) {
             writer.startNode(t[2]);
           }
           state = STATE_BEGIN;
        } else if (NODE.equals(t[1])) {
           if (t.length > 4) {
             writer.startNode(t[3]);
             writer.setValue(t[4]);
             writer.endNode();
           }
           state = STATE_NODE_VALUE;
        } else if (SET_VALUE.equals(t[1])) {
           if (t.length > 3) {
             writer.setValue(t[3]);
           }
           state = STATE_VALUE;
        } else if (ATTRIBUTE.equals(t[1])) {
           if (t.length > 4) {
             writer.addAttribute(t[3], t[4]);
           }
           state = STATE_ATTRIBUTE;
        } else if (END_NODE.equals(t[1])) {
           writer.endNode();
           state = STATE_END;
        }
      } else {
        switch (state) {
          case STATE_BEGIN:
            if (t.length > 1) {
              writer.startNode(t[1]);
            }
            break;
          case STATE_NODE_VALUE:
            if (t.length > 2) {
              writer.startNode(t[1]);
              writer.setValue(t[2]);
              writer.endNode();
            }
            break;
          case STATE_VALUE:
            if (t.length > 1) {
              writer.setValue(t[1]);
            }
            break;
          case STATE_ATTRIBUTE:
            if (t.length > 2) {
              writer.addAttribute(t[1], t[2]);
            }
            break;
          case STATE_END:
            break;
        }
        state = -1;
      }
    }
    System.out.println("=======Buffer " + buffer);
    return buffer.toString();
  }

  /**
   * Creates DOM Document from XML
   * @param xml
   * @return DOM Document
   */
  public static Document xmlToDocument(String xml) throws IOException, 
        javax.xml.parsers.ParserConfigurationException,
        org.xml.sax.SAXException {
    return xmlToDocument(new StringReader(xml));
  }

  /**
   * Creates DOM Document from XML Reader
   * @param xml reader
   * @return DOM Document
   */
  public static Document xmlToDocument(Reader xml) 
        throws IOException, 
        javax.xml.parsers.ParserConfigurationException,
        org.xml.sax.SAXException {
    DocumentBuilderFactory documentBuilderFactory = 
        DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = 
        documentBuilderFactory.newDocumentBuilder();
    InputSource source = new InputSource(xml);
    source.setEncoding("UTF-8");
    return documentBuilder.parse(source);
  }

  /**
   * Creates DOM Document Reader from XML Reader
   * @param xml reader
   * @return DOM Document Reader
   */
  public HierarchicalStreamReader createReader(Reader xml) {
    try {
      return new DomReader(xmlToDocument(tableToXml(xml)));
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Creates DOM Document Writer 
   * @param out - writer
   * @return WikiTableWriter writer that implements DOM Document Writer
   */
  public HierarchicalStreamWriter createWriter(final Writer out) {
    try {
      return new WikiTableWriter(out);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
