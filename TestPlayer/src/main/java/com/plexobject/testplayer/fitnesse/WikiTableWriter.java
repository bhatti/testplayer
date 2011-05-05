/* ===========================================================================
 * $RCS$
 * Version: $Id: WikiTableWriter.java,v 2.4 2006/08/20 23:31:07 shahzad Exp $
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
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.util.*;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.*;
import com.thoughtworks.xstream.io.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
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
 * modification history
 * date         who             what
 * 1/9/06       SB              created.
 */

public class WikiTableWriter implements ExtendedHierarchicalStreamWriter {
  private final PointableWriter writer;
  private final FastStack elementStack = new FastStack(16);
  private final char[] lineIndenter;

  private boolean useLineSeparateBetweenTags;
  private boolean tagInProgress;
  private int startPointer;
  private int depth;
  private boolean readyForNewLine;
  private boolean tagIsEmpty;
  private boolean quickNodeValue;
  private boolean lastTagHadAttributes;

  private static final char[] AMP = "&amp;".toCharArray();
  private static final char[] LT = "&lt;".toCharArray();
  private static final char[] GT = "&gt;".toCharArray();
  private static final char[] SLASH_R = "&#x0D;".toCharArray();
  private static final char[] QUOT = "&quot;".toCharArray();
  private static final char[] APOS = "&apos;".toCharArray();
  private static final char[] CLOSE = "</".toCharArray();
  private static final String LF = System.getProperty("line.separator");


  /**
   * WikiTableWriter constructor
   * @param writer - output writer object
   * @param lineIdenter - indentation
   */
  public WikiTableWriter(Writer writer, char[] lineIndenter) {
    this.writer = new PointableWriter(writer);
    this.lineIndenter = lineIndenter;
    this.useLineSeparateBetweenTags = true; //context.getConfig().getBoolean("testplayer.fitnesse.use.line.separator.between.tags", true);
  }

  /**
   * WikiTableWriter constructor
   * @param writer - output writer object
   * @param lineIdenter - indentation
   */
  public WikiTableWriter(Writer writer, String lineIndenter) {
    this(writer, lineIndenter.toCharArray());
  }      

  /**
   * WikiTableWriter constructor
   * @param writer - print writer object
   */
  public WikiTableWriter(PrintWriter writer) {
    this(writer, new char[]{'|'});
  }

  /**
   * WikiTableWriter constructor
   * @param writer - writer object
   */
  public WikiTableWriter(Writer writer) {
    this(new PrintWriter(writer));
  }

  /**
   * Marks start of XML node
   * @param name - name of node
   */
  public void startNode(String name, Class clazz) {
    startNode(name);
  }
  public void startNode(String name) {
    startPointer = writer.getPointer();
    quickNodeValue = false;
    lastTagHadAttributes = false;
    tagIsEmpty = false;
    finishTag();
    if (depth != 0) writer.write(LF);
    writer.write(
        WikiTableDriver.DELIM + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.START_NODE + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.DELIM);
 
     if (this.useLineSeparateBetweenTags) {
        writer.write(
            LF +
            WikiTableDriver.DELIM); 
     }

     writer.write(
        name + 
        WikiTableDriver.DELIM);
    elementStack.push(name);
    tagInProgress = true;
    depth++;
    readyForNewLine = true;
    tagIsEmpty = true;
  }

  /**
   * Stores value of previously defined XML node
   * @param text - value of node
   */
  public void setValue(String text) {
    readyForNewLine = false;
    tagIsEmpty = false;
    finishTag();

    if (lastTagHadAttributes) {
      writer.write(
        LF + 
        WikiTableDriver.DELIM + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.SET_VALUE + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.DELIM);
      quickNodeValue = false;
    } else {
      String startTag = StringHelper.replace(
                        writer.substring(startPointer), 
                        WikiTableDriver.START_NODE, 
                        WikiTableDriver.SET_NODE_VALUE);

      if (this.useLineSeparateBetweenTags) {
        writer.write(
          LF +
          WikiTableDriver.DELIM); 
      }

      writer.setPointer(startPointer);
      writer.write(startTag);
      quickNodeValue = true;
    }

        

    writeText(writer, text);
    writer.write(WikiTableDriver.DELIM);
  }

  /**
   * Adds attribute for XML node
   * @param key - name of attribute
   * @param value - value of attribute
   */
  public void addAttribute(String key, String value) {
    writer.write(
        LF + 
        WikiTableDriver.DELIM + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.ADD_ATTRIBUTE + 
        WikiTableDriver.QUOTES + 
        WikiTableDriver.DELIM);

    if (this.useLineSeparateBetweenTags) {
      writer.write(
        LF +
        WikiTableDriver.DELIM); 
    }

    writer.write(
        key + 
        WikiTableDriver.DELIM);



        
    writeAttributeValue(writer, value);
    writer.write(WikiTableDriver.DELIM);
    lastTagHadAttributes = true;
  }


  /**
   * Marks end of node
   */
  public void endNode() {
    depth--;
    if (tagIsEmpty) {
      String prevTag = (String) elementStack.pop(); //Silently();
      if (!quickNodeValue) {
        writer.write(
                LF + 
                WikiTableDriver.DELIM + 
                WikiTableDriver.QUOTES + 
                WikiTableDriver.END_NODE + 
                WikiTableDriver.QUOTES + 
                WikiTableDriver.DELIM); 
        if (this.useLineSeparateBetweenTags) {
          writer.write(
            LF +
            WikiTableDriver.DELIM); 
        }
        writer.write(
                prevTag + 
                WikiTableDriver.DELIM);
      }
      readyForNewLine = false;
    } else {
      String prevTag = (String) elementStack.pop();
      if (!quickNodeValue) {
        writer.write(
                LF + 
                WikiTableDriver.DELIM + 
                WikiTableDriver.QUOTES + 
                WikiTableDriver.END_NODE + 
                WikiTableDriver.QUOTES + 
                WikiTableDriver.DELIM); 
        if (this.useLineSeparateBetweenTags) {
          writer.write(
            LF +
            WikiTableDriver.DELIM); 
        }
        writer.write(
                prevTag + 
                WikiTableDriver.DELIM);
      }
    }
    finishTag();
    readyForNewLine = true;
    if (depth == 0 ) {
      writer.flush();
    }
    quickNodeValue = false;
  }

  /**
   * Flushes output
   */
  public void flush() {
    writer.flush();
  }

  /**
   * Closes output
   */
  public void close() {
    writer.close();
  }

  /**
   * Returns underlying writer
   */
  public HierarchicalStreamWriter underlyingWriter() {
    return this;
  }



  /////////////////////////////////////////////////////////////////////////
  //
  private void finishTag() {
    tagInProgress = false;
    readyForNewLine = false;
    tagIsEmpty = false;
  }

  private void endOfLine() {
    writer.write(LF);
    for (int i = 0; i < depth; i++) {
      writer.write(lineIndenter);
    }
  }

  private void writeAttributeValue(PointableWriter writer, String text) {
    int length = text.length();
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':
          this.writer.write(AMP);
          break;
        case '<':
          this.writer.write(LT);
          break;
        case '>':
          this.writer.write(GT);
          break;
        case '"':
          this.writer.write(QUOT);
          break;
        case '\'':
          this.writer.write(APOS);
          break;
        case '\r':
          this.writer.write(SLASH_R);
          break;
        default:
          this.writer.write(c);
      }
    }
  }

  private void writeText(PointableWriter writer, String text) {
    int length = text.length();
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      switch (c) {
        case '&':
          this.writer.write(AMP);
          break;
        case '<':
          this.writer.write(LT);
          break;
        case '>':
          this.writer.write(GT);
          break;
        case '"':
          this.writer.write(QUOT);
          break;
        case '\'':
          this.writer.write(APOS);
          break;
        case '\r':
          this.writer.write(SLASH_R);
          break;
        default:
          this.writer.write(c);
      }
    }
  }

}
