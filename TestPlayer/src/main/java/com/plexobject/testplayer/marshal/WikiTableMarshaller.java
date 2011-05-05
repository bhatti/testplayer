/* ===========================================================================
 * $RCS$
 * Version: $Id: WikiTableMarshaller.java,v 2.2 2006/02/25 20:50:46 shahzad Exp $
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

package com.plexobject.testplayer.marshal;
import com.plexobject.testplayer.tree.*;
import com.plexobject.testplayer.fitnesse.*;
import java.io.*;
import java.text.*;
import java.util.*;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;
import com.thoughtworks.xstream.io.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.core.util.QuickWriter;
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
 * This class marshals and unmarshals java objects
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 1/11/06      SB              created.
 */
public class WikiTableMarshaller implements IMarshaller {
  /**
   * marshalls object into text
   *
   * @param object - the object to be marshalled.
   * @param parent - parent node
   */
  public String marshal(Object object) {
    XStream xstream = new XStream(new WikiTableDriver());
    return xstream.toXML(object);
  }

  /**
   * unmarshalls - constructs object from text 
   *
   * @param node - node
   */
  public Object unmarshal(String text) {
    XStream xstream = new XStream(new WikiTableDriver());
    return xstream.fromXML(text);
  }
}
