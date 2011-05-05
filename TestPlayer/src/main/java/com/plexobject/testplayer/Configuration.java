/* ===========================================================================
 * $RCS$
 * Version: $Id: Configuration.java,v 1.9 2007/07/11 13:53:45 shahzad Exp $
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

package com.plexobject.testplayer;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.marshal.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.apache.log4j.*;


/**
 * Configuration stores application-wide configuration.
 * @author shahzad bhatti
 *
 * Version: $Id: Configuration.java,v 1.9 2007/07/11 13:53:45 shahzad Exp $
 *
 * modification history
 * date         who             what
 * 9/11/05      SB              created.
 */
public class Configuration extends Properties implements Constants { 
  /**
   * Configuration constructor
   */
  public Configuration() {
    this(DEFAULT_PROPERTY_FILE_NAME);
  }
  public Configuration(String propertyFileName) {
    this.propertyFileName = propertyFileName;
    try {
      setProperty(LICENSE, VALUE_LICENSE);
      setProperty(MARKER_IMPORT_BEGIN,             VALUE_MARKER_IMPORT_BEGIN);
      setProperty(MARKER_IMPORT_END,               VALUE_MARKER_IMPORT_END);
      setProperty(MARKER_EXTENDS_IMPLEMENTS_BEGIN, VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN);
      setProperty(MARKER_EXTENDS_IMPLEMENTS_END,   VALUE_MARKER_EXTENDS_IMPLEMENTS_END);
      setProperty(MARKER_CLASS_BEGIN,              VALUE_MARKER_CLASS_BEGIN);
      setProperty(MARKER_CLASS_END,                VALUE_MARKER_CLASS_END);
      setProperty(MARKER_METHOD_BEGIN,             VALUE_MARKER_METHOD_BEGIN);
      setProperty(MARKER_METHOD_END,               VALUE_MARKER_METHOD_END);
      setProperty(MARKER_JAVADOC_CLASS_BEGIN,      VALUE_MARKER_JAVADOC_CLASS_BEGIN);
      setProperty(MARKER_JAVADOC_CLASS_END,        VALUE_MARKER_JAVADOC_CLASS_END);
      setProperty(MARKER_JAVADOC_METHOD_BEGIN,     VALUE_MARKER_JAVADOC_METHOD_BEGIN);
      setProperty(MARKER_JAVADOC_METHOD_END,       VALUE_MARKER_JAVADOC_METHOD_END);
      InputStream inputStream = getPropertyInputStream(propertyFileName);
      load(inputStream);
      license = getTemplate("license", null);
      setProperty(LICENSE, license);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new TestSystemException("******* testplayer failed to initialize *******", e);
    }
  }




  /**
   * @param name - property name
   * @return returns boolean property for given property name
   */
  public boolean getBoolean(String name) {
    return getBoolean(name, false);
  }


  /**
   * @param name - property name
   * @param def - default value
   * @return returns boolean property for given property name
   */
  public boolean getBoolean(String name, boolean def) {
    String value = getProperty(name);
    if (value == null) return def;
    return value.equalsIgnoreCase("true") ||
           value.equalsIgnoreCase("yes");
  }



  /**
   * @param name - property name
   * @return returns integer property for given property name
   */
  public int getInteger(String name) {
    return getInteger(name, 0);
  }


  /**
   * @param name - property name
   * @param def - default value
   * @return returns integer property for given property name
   */
  public int getInteger(String name, int def) {
    String value = getProperty(name);
    if (value == null) return def;
    return new Integer(value).intValue();
  }


  private InputStream getPropertyInputStream(String propertyFileName) {
    InputStream returnValue = null;
    File file = new File(propertyFileName);
    if (file.exists()) {
      try {
        returnValue = new BufferedInputStream(new FileInputStream(file));
      } catch (IOException e) {
        throw new TestSystemException("Failed to load " + file + " due to " + e);
      }
    } else {
      returnValue = getClass().getClassLoader().getResourceAsStream(propertyFileName);
      if (returnValue == null) {
        returnValue = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFileName);
      }
      if (returnValue == null) {
        throw new TestSystemException("Failed to load '" + propertyFileName + "' in class path); // " + System.getProperty("java.class.path"));
      }
    }
    return returnValue;
  }

  public String getTemplate(String templateName, String attribute) {
    return getTemplate(this, templateName, attribute);
  }

 
  public String getTemplate(Properties properties, String templateName, String attribute) {
    String returnValue       = null;
    String qualifiedTemplate = null;
    if (templateName != null) {
      if (attribute != null) {
        qualifiedTemplate = "template." + templateName + "." + attribute;
      } else {
        qualifiedTemplate = "template." + templateName;
      }
      if (!templateCache.containsKey(qualifiedTemplate)) {
        templateCache.put(qualifiedTemplate, createTemplate(properties, qualifiedTemplate));
      }
      returnValue = (String) templateCache.get(qualifiedTemplate);
    } else {
      throw new TestSystemException("Failed to load template name " + templateName + " attribute == " + attribute);
    }
    return returnValue;
  }
  public String createTemplate(Properties properties, String templateName) {
    SortedMap lines = new TreeMap();
    Enumeration e  = properties.propertyNames();
    String propLine;
    Integer key;

    while (e.hasMoreElements()) {
      propLine = (String) e.nextElement();
      if (propLine.startsWith(templateName + ".")) {
        key = new Integer(propLine.substring(templateName.length() + 1));
        lines.put(key, properties.getProperty(propLine));
      }
    }
    StringBuilder sb = new StringBuilder();
    Iterator iterator = lines.values().iterator();
    while (iterator.hasNext()) {
      sb.append((String) iterator.next());
      sb.append("\n");
    }
    return sb.toString();
  }


  private String license;
  private String propertyFileName;
  private static Map templateCache = new HashMap();
  private transient Logger logger = Logger.getLogger(Configuration.class.getName());
  public static final String LF = System.getProperty("line.separator");
  public static final String FS = System.getProperty("file.separator");
  protected static final String DEFAULT_PROPERTY_FILE_NAME  = "testplayer.properties";
}

