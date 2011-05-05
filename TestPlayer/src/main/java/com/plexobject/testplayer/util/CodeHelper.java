/* ===========================================================================
 * $RCS$
 * Version: $Id: CodeHelper.java,v 2.15 2007/07/11 13:53:48 shahzad Exp $
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
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;


/**
 * This class is helper class for creating unit tests
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/7/05       SB              created.
 */
public class CodeHelper {
  private CodeHelper() {
  }

  /**
   * declare - declares given type and value
   * @param context - application context
   * @param type - type of the object
   * @param value - object itself
   * @param sb - output buffer
   * @param prefix - variable declaration get this prefix
   * @param mocks - stores mock references that are used instead of
   *    simple object creation.
   */
  public static void declare(
        ApplicationContext context,
        String type, 
        Object value, 
        StringBuilder sb, 
        String prefix, 
        Map mocks) {
    type = TypeHelper.toExternalType(type);

    //context.println("// declaring type " + type, 2, sb);
    if (TypeHelper.isPrimitive(type)) {
      if (value == null) value = "0";
      if (type.equals("float")) value = value + "F";
      context.println(type + " " + prefix + " = " + value + ";", 2, sb);
    } else if (value == null) {
      context.println(type + " " + prefix + " = null;", 2, sb);
    } else if (value.toString() == null) {
      String mockName = null;
      if (mocks != null) {
         mockName = (String) mocks.get(type);
      }
      if (mockName != null) {
        context.println(type + " " + prefix + " = (" + type + ") " + mockName + ".proxy();", 2, sb);
      } else {
        context.println(type + " " + prefix + " = new " + value.getClass().getName() + "();", 2, sb);
      }
    } else if (value == null) {
      context.println(type + " " + prefix + " = null;", 2, sb);
    } else if (value instanceof Exception) {
      Constructor[] ctors = value.getClass().getConstructors();
      boolean found = false;
      for (int i=0; i<ctors.length; i++) {
      	Class[] pt = ctors[i].getParameterTypes();
        if (pt.length == 0) {
          context.println(type + " " + prefix + " = new " + type + "();", 2, sb);
      	  found = true;
	  break;
        } else if (pt.length == 1) {
          context.println(type + " " + prefix + " = new " + type + "(\"" + value + "\");", 2, sb);
      	  found = true;
	  break;
	}
      }
      if (!found) {
        Map getters = isJavaBean(value.getClass());
        if (getters != null) {
          declareJavaBean(context, type, value, sb, prefix, mocks, getters);
	} else {
          context.println(type + " " + prefix + " = (" + type + 
		") marshaller.unmarshal(\"" + 
		marshal(context, value) + "\");", 2, sb);
        }
      }
    } else if (TypeHelper.isPrimitiveWrapper(type) || 
        StringBuilder.class.getName().equals(type)) {
      context.println(type + " " + prefix + " = new " + type + "(\"" + value + "\");", 2, sb);
    } else if (TypeHelper.isPrimitiveArray(type) || TypeHelper.isPrimitiveWrapperArray(type)) {
      int count = 0;
      try {
        for (int i=0; ; i++) {
          Class clazz = TypeHelper.getPrimitiveArrayType(type);
          Object result = null;
          if (TypeHelper.isPrimitiveArray(type)) {
            result = ReflectHelper.readPrimitiveArray(type, value, i);
          } else {
            result = ReflectHelper.readArray(type, value, i);
          }
          //logger.warn("\n\n\n\nArray(" + value + ")[" + i + "] = " + result + ", type " + type);
          String atype = null;
          if (true || result == null) {
            if (type.endsWith("[]")) atype = type.substring(0, type.length()-2);
            else atype = type;
          } else {
            atype = result.getClass().getName();
          }
          String typePre = "";
          String typeSuf = "";
          if (atype.equals("char")) {
            typePre = "'";
            typeSuf = "'";
          } else if (atype.equals("float")) {
            typeSuf = "F";
          } else if (TypeHelper.isPrimitiveWrapperArray(type)) {
            typePre = " new " + atype + "(\"";
            typeSuf = "\")";
          } 
          context.println(atype + " " + prefix + i + " = " + typePre + result + typeSuf + ";", 2, sb);
          count++;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
      } catch (Exception e) {
        e.printStackTrace();
      }
      context.println(type + " " + prefix + " = {", 2, sb);
      for (int i=0; i<count; i++) {
        context.println(prefix + i + ",", 3, sb);
      }
      context.println("};", 2, sb);
    } else {
      String mockName = null;
      if (mocks != null) {
         mockName = (String) mocks.get(type);
      }
      if (mockName != null) {
        context.println(type + " " + prefix + " = (" + type + ") " + mockName + ".proxy();", 2, sb);
      } else {
        Map getters = isJavaBean(value.getClass());
        if (getters != null) {
          declareJavaBean(context, type, value, sb, prefix, mocks, getters);
	} else {
          context.println(type + " " + prefix + " = (" + type + 
		") marshaller.unmarshal(\"" + 
		marshal(context, value) + "\");", 2, sb);
        }
      }
    }
  }

  public static void declareJavaBean(
        ApplicationContext context,
        String type, 
        Object value, 
        StringBuilder sb, 
        String prefix, 
        Map mocks,
        Map getters) {
    context.println(type + " " + prefix + " = new " + value.getClass().getName() + "();", 2, sb);
    int count = 0;
    Iterator it = getters.values().iterator();
    while (it.hasNext()) {
      Method method = (Method) it.next();
      try {
        String setter = null;
        if (method.getName().startsWith("get")) setter = "set" + method.getName().substring(3);
        else if (method.getName().startsWith("is")) setter = "set" + method.getName().substring(2);
        method.setAccessible(true);
        Object attrValue = method.invoke(value, new Object[0]);
        declare(context, attrValue.getClass().getName(), attrValue, sb, prefix + count, mocks);
        context.println(prefix + "." + setter + "(" + prefix + count + ");", 2, sb);
        count++;
      } catch (Throwable e) {
        context.println("// failed to set setter for " + method + " due to " + e, 2, sb);
      }
    }
  }

  /**
   * @return package name of caller's class for the method invocation
   */
  public static String getPackageName(String name) {
    if (name == null) return null;
    int end = name.lastIndexOf('.');
    if (end == -1) return name;
    return name.substring(0, end);
  }


  /**
   * @return type without package
   */
  public static String getTypeWithoutPackage(String name) {
    if (name == null) return null;
    return name.substring(name.lastIndexOf('.')+1);
  }


  /**
   * @return type without package. If the type is array
   * then basic type is returned
   */
  public static String getBasicTypeWithoutPackage(String name) {
    name = getTypeWithoutPackage(name); 
    if (name == null) return null;
    int n = name.indexOf('[');
    if (n != -1) name = name.substring(0, n);
    return name;
  }



  /**
   * loads file into list of string
   * @param file - file
   * @return list of string
   */
  public static List readLines(File file) throws IOException {
    List list = new ArrayList();
    if (!file.exists()) return list;
    BufferedReader in = new BufferedReader(new FileReader(file));
    String line;
    while ((line=in.readLine()) != null) {
      list.add(line.trim());
    }
    in.close();
    return list;
  }

  /**
   * writes array of lines into file
   * @param file - file
   * @return array of lines
   */
  public static void writeLines(File file, String[] lines) 
        throws IOException {
    writeLines(file, Arrays.asList(lines));
  }


  /**
   * writes array of lines into file
   * @param file - file
   * @return collection of lines
   */
  public static void writeLines(File file, Collection lines) 
        throws IOException {
    PrintWriter out = new PrintWriter(new FileWriter(file));
    Iterator it = lines.iterator();
    while (it.hasNext()) {
      String next = (String) it.next();
      out.println(next.trim());
    }
    out.close();
  }

  // has default constructor and matching getters/setters for all attributes
  public static Map isJavaBean(Class clazz) {
    if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) return null;
    if (getInterfaceFor(clazz) != null) return null;
    Map setters = new HashMap();
    Map getters = new HashMap();
    boolean defaultConstructor = false;
    Constructor[] ctors = clazz.getConstructors();
    for (int i=0; i<ctors.length; i++) {
      if (ctors[i].getParameterTypes().length == 0) {
	defaultConstructor = true;
 	break;
      }
    }
    if (!defaultConstructor) return null;

    List fields = new ArrayList();
    Field[] dfields = clazz.getDeclaredFields();
    for (int i=0; i<dfields.length; i++) {
      fields.add(dfields[i].getName());
    }
    Class[] parents = clazz.getDeclaredClasses();
    for (int i=0; i<parents.length; i++) {
      dfields = parents[i].getDeclaredFields();
      for (int j=0; j<dfields.length; j++) {
        fields.add(dfields[j].getName());
      }
    }

    Method[] methods = clazz.getMethods();
    for (int i=0; i<methods.length; i++) {
      if (methods[i].getName().startsWith("set")) setters.put(methods[i].getName(), methods[i]);
      else if (methods[i].getName().startsWith("is") || methods[i].getName().startsWith("get")) getters.put(methods[i].getName(), methods[i]);
    }
    //if (getters.size() != setters.size()) return null;


    Iterator it = new ArrayList(getters.keySet()).iterator();
    while (it.hasNext()) {
      String getter = (String) it.next();
      String setter = getter.startsWith("get") ? "set" + getter.substring(3) : "set" + getter.substring(2);
      if (setters.get(setter) == null) {
        String field = setter.substring(3);
	if (field.length() <= 1) {
	  logger.warn("Skipping field " + field + " for getter " + getter);
	  continue;
	}
        field = Character.toLowerCase(field.charAt(0)) + field.substring(1);
        if (fields.indexOf(field) != -1) {
	  logger.warn("Could not find setter for " + field + ", but had getter " + getter);
	  return null;
        } else {
	  getters.remove(getter);
        }
      }
    }
    // TODO verify fields
    return getters;
  }

  /////////////////////////////////////////////////////////////////////////
  public static String getSetterFor(Class parent, Class child) {
    try {
      Class iface = getInterfaceFor(child);
      if (iface != null) child = iface;
      Method[] methods = parent.getMethods();
      for (int i=0; i<methods.length; i++) {
        if (methods[i].getParameterTypes().length == 1) {
	  Class clazz = methods[i].getParameterTypes()[0];
	  if (clazz == Object.class) continue;
	  Class rtype = methods[i].getReturnType();
	  boolean assignable = clazz.isAssignableFrom(child);
          if (rtype == Void.TYPE && assignable) {
	     return methods[i].getName();
	  }
	}
      }
    } catch (Throwable e) {
    }
    return null;
  }

  public static Class getInterfaceFor(Class childClass) {
    Class[] ifaces = childClass.getInterfaces();
    for (int i=0; i<ifaces.length; i++) {
      String name = ifaces[i].getName();
      name = name.substring(name.lastIndexOf('.')+1);
      if (name.startsWith("I")) name = name.substring(1);
      if (name.endsWith("I")) name = name.substring(0, name.length()-1);
      if (childClass.getName().indexOf(name) != -1) {
	  return ifaces[i];
      }
    }
    return null;
  }

  public static boolean hasDefaultConstructor(String type) {
    try {
      return hasDefaultConstructor(Class.forName(type));
    } catch (Throwable e) {
    }
    return false;
  }


  public static boolean hasDefaultConstructor(Class type) {
    try {
      Constructor[] ctors = type.getConstructors();
      for (int i=0; i<ctors.length; i++) {
        Class[] params = ctors[i].getParameterTypes();
        if (params.length == 0) return true;
      }
    } catch (Throwable e) {
    }
    return false;
  }

  public static boolean hasDelegateConstructorFor(Class type, Class argType) {
    try {
      if (!argType.isInterface()) {
	 Class iface = getInterfaceFor(argType);
	 if (iface != null) argType = iface;
      }
      Constructor[] ctors = type.getConstructors();
      for (int i=0; i<ctors.length; i++) {
        Class[] params = ctors[i].getParameterTypes();
        if (params.length == 1 && argType.isAssignableFrom(params[0])) {
           return true;
        }
      }
    } catch (Throwable e) {
    }
    return false;
  }

  protected static String marshal(ApplicationContext context, Object value) {
    return StringHelper.replace(
		context.getDefaultMarshaller().marshal(value),
		"\"", "\\\""); 
  }

  ////////////////////////////////////////////////////////////////////
  private transient static Logger logger = Logger.getLogger(CodeHelper.class.getName());
}

