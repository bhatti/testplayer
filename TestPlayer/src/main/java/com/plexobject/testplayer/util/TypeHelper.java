/* ===========================================================================
 * $RCS$
 * Version: $Id: TypeHelper.java,v 2.6 2007/07/11 13:53:48 shahzad Exp $
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

package com.plexobject.testplayer.util;
import com.plexobject.testplayer.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import java.beans.*;


/**
 * This class is helper class for finding types or serializing objects
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/7/05       SB              created.
 */
public class TypeHelper {
  /**
   * @return isPrimitive returns true if given type is primitive
   */
  public static boolean isPrimitive(String type) {
    if (type == null) return false;
    String[] types = {"boolean", "int", "byte", "short", "long", "char", "float", "double"};
    for (int i=0; i<types.length; i++) {
      if (types[i].equals(type)) return true;
    }
    try {
      return Class.forName(type).isPrimitive();
    } catch (Exception e) {
      return false;
    }
  }


  /**
   * @return isPrimitiveWrapper returns true if given type is primitive wrapper
   */
  public static boolean isPrimitiveWrapper(String type) {
    if (type == null) return false;
    String[] types = {"java.lang.Boolean", "java.lang.Integer", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Character", "java.lang.Float", "java.lang.Double", "java.lang.String"}; //, "java.lang.StringBuilder"};
    for (int i=0; i<types.length; i++) {
      if (types[i].equals(type)) return true;
    }
    try {
      return Class.forName(type).isPrimitive();
    } catch (Exception e) {
      return false;
    }
  }


  /**
   * @return wrapper class for given primitive type to be
   */
  public static String getPrimitiveArrayMethod(String type) {
    if (type == null) return null;
    String[] types = {"boolean", "int", "byte", "short", "long", "char", "float", "double"};
    String[] wrappers = {"getBoolean", "getInt", "getByte", "getShort", "getLong", "getChar", "getFloat", "getDouble"};
    for (int i=0; i<types.length; i++) {
      if (type.startsWith(types[i])) return wrappers[i];
    }
    return null;
  }


  /**
   * @return wrapper class for given primitive type
   */
  public static String getPrimitiveWrapper(String type) {
    if (type == null) return null;
    String[] types = {"boolean", "int", "byte", "short", "long", "char", "float", "double"};
    String[] wrappers = {"java.lang.Boolean", "java.lang.Integer", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Character", "java.lang.Float", "java.lang.Double"};
    for (int i=0; i<types.length; i++) {
      if (type.startsWith(types[i])) return wrappers[i];
    }
    return null;
  }

  /**
   * @return wrapper class for given primitive type
   */
  public static Class getPrimitiveClass(String type) {
    if ("boolean".equals(type)) return boolean.class;
    else if ("int".equals(type)) return int.class;
    else if ("byte".equals(type)) return byte.class;
    else if ("short".equals(type)) return short.class;
    else if ("long".equals(type)) return long.class;
    else if ("char".equals(type)) return char.class;
    else if ("float".equals(type)) return float.class;
    else if ("double".equals(type)) return double.class;
    return null;
  }

  /**
   * getMethodCount checks if given method is defined more than once or only
   * once
   * @param type - type of class
   * @param method - method name
   * @return number of times the method is defined or -1 if unknown error
   * is occurred.
   */
  public static int getMethodCount(String type, String method) {
    try {
      Class clazz = Class.forName(type);
      Method[] methods = clazz.getMethods();
      int size = 0;
      for (int i=0; i<methods.length; i++) {
        if (methods[i].getName().equals(method)) size++;
      }
      return size;
    } catch (Exception e) {
      return -1;
    }
  }


  /**
   * @return isPrimitiveArray returns true if given type is array of primitive
   */
  public static boolean isPrimitiveArray(String type) {
    if (type == null) return false;
    if (!type.endsWith("[]")) return false;
    return isPrimitive(type.substring(0, type.length()-2));
  }


  /**
   * @return isPrimitiveWrapperArray returns true if given type is array of primitive
   */
  public static boolean isPrimitiveWrapperArray(String type) {
    if (type == null) return false;
    if (!type.endsWith("[]")) return false;
    return isPrimitiveWrapper(type.substring(0, type.length()-2));
  }

  /**
   * @return isArray returns true if given type is array
   */
  public static boolean isArray(String type) {
    if (type == null) return false;
    return type.endsWith("[]");
  }

  /**
   * @return - returns true if type is of collection type
   */
  public static boolean isCollection(String type) {
    try {
      return Collection.class.isAssignableFrom(Class.forName(type));
    } catch (Exception e) {}
    return false;
  }



  /**
   * @return class for array of primitive types
   */
  public static Class getPrimitiveArrayType(String type) {
    if (type == null) return null;
    String[] types = {"boolean", "int", "byte", "short", "long", "char", "float", "double"};
    Class[] clazzes = {boolean[].class, int[].class, byte[].class, short[].class, long[].class, char[].class, float[].class, double[].class};
    for (int i=0; i<types.length; i++) {
      if (type.startsWith(types[i])) return clazzes[i];
    }
    return null;
  }



  /**
   * @return hasStringConstructor returns true if given type can be initialized from String constructor
   */
  public static boolean hasStringConstructor(Class clazz) {
    return getStringConstructor(clazz) != null;
  }


  /**
   * @return getStringConstructor returns constructor if given type can be initialized from String constructor
   */
  public static Constructor getStringConstructor(Class clazz) {
    try {
      return clazz.getConstructor(new Class[] {String.class});
    } catch (Exception e) {
      //e.printStackTrace();
      return null;
    }
  }
  /**
   * sets field of an object to given value even if that field is private
   * @param object - object whose value are to be set
   * @param name name of attribute
   * @param value - value of attribute
   * Note that type of attribute is not necessary
   */
  public static void setField(Object object, String name, Object value) 
        throws NoSuchFieldException, IllegalAccessException {
    if (object == null || name == null) return;
    Field field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(object, value);
  }


  /**
   * modifies type from internal type to external type
   * @param - type - internal type 
   * @return - typed 
   */
  public static String toExternalType(String type) {
    if (type.startsWith("L") && type.endsWith(";")) {
      return type.substring(1, type.length()-1).replace('.', '/');
    }
    int dims = 0;
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<type.length(); i++) {
      char ch = type.charAt(i);
      if (ch == '[') {
        dims++;
      } else if (dims > 0) {
        switch (ch) {
          case 'B':
            sb.append("byte");
            break;
          case 'C':
            sb.append("char");
            break;
          case 'D':
            sb.append("double");
            break;
          case 'F':
            sb.append("float");
            break;
          case 'I':
            sb.append("int");
            break;
          case 'J':
            sb.append("long");
            break;
          case 'S':
            sb.append("short");
            break;
          case 'Z':
            sb.append("boolean");
            break;
          case 'L':
            i++; // skip L
            int semi = type.indexOf(";", i);
            sb.append(type.substring(i, semi).replace('.', '/'));
            break;
        }
        sb.append(getBrackets(dims));
        dims = 0;
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /**
   * modifies type from internal type to external type
   * @param - type - internal type 
   * @return - typed 
   */
  public static String toInternalType(String type, boolean useWrapper) {
/*
    if (type.startsWith("L") && type.endsWith(";")) {
      return type.substring(1, type.length()-1).replace('/', '.');
    }
*/
    String basic = null;
    int bracketIndex = type.indexOf("[]");
    if (bracketIndex != -1) basic = type.substring(0, bracketIndex).replace('/', '.');
    else basic = type.replace('/', '.');
    boolean primitive = TypeHelper.isPrimitive(basic);

    StringBuilder sb = new StringBuilder();
    if (bracketIndex != -1) {
      for (int i=0; i<type.length()-bracketIndex; i+=2) {
        sb.append("[");
      }
    }
    if (primitive && !useWrapper) {
      if (basic.equals("byte")) sb.append("B");
      else if (basic.equals("char")) sb.append("C");
      else if (basic.equals("double")) sb.append("D");
      else if (basic.equals("float")) sb.append("F");
      else if (basic.equals("int")) sb.append("I");
      else if (basic.equals("long")) sb.append("J");
      else if (basic.equals("short")) sb.append("S");
      else if (basic.equals("boolean")) sb.append("Z");
      else if (basic.equals("boolean")) sb.append("Z");
    } else {
      if (primitive && useWrapper) basic = TypeHelper.getPrimitiveWrapper(basic);
      if (bracketIndex != -1) sb.append("L" + basic + ";");
      else sb.append(basic);
    }
    return sb.toString();
  }

  private static String getBrackets(int dims) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<dims; i++) {
      sb.append("[]");
    }
    return sb.toString();
  }


  private TypeHelper() {
  }
}

