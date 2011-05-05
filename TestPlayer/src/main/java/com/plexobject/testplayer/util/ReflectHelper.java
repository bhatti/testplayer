/* ===========================================================================
 * $RCS$
 * Version: $Id: ReflectHelper.java,v 2.6 2006/08/18 01:59:13 shahzad Exp $
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
 * 12/21/05     SB              created.
 */
public class ReflectHelper {
  private ReflectHelper() {
  }


  /**
   * invokes given method name
   * @param object - object 
   * @param methodName - name of methods
   * @param methodTypes - types of arguments
   * @param args - arguments
   */
  public static Object invoke(
        Object object, 
        String methodName, 
        Class[] methodTypes, 
        Object[] args
        ) throws Exception {
    return invoke(object.getClass(), object, methodName, methodTypes, args);
  }

  public static Object invoke(
    	Class clazz,
        Object object, 
        String methodName, 
        Class[] methodTypes, 
        Object[] args
        ) throws Exception {
    Method method = clazz.getMethod(methodName, methodTypes);
    try {
      method.setAccessible(true);
      return method.invoke(object, args);
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getTargetException() instanceof Exception) throw (Exception) e.getTargetException();
      throw new TestSystemException(e.getTargetException());
    }
  }

  public static Object invokeAnyMethodWithArgs(
    	Class clazz,
        Object object, 
        Object[] args
        ) throws Exception {
    Method[] methods = clazz.getMethods();
    for (int i=0; i<methods.length; i++) {
      Class[] params = methods[i].getParameterTypes();
      if (params.length == args.length) {
	boolean matched = true;
	for (int j=0; j<params.length; j++) {
	  if (!params[j].isAssignableFrom(args[j].getClass())) {
	     matched = false;
	     break;
	  }
	}
	if (matched) {
          try {
            methods[i].setAccessible(true);
            return methods[i].invoke(object, args);
          } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception) throw (Exception) e.getTargetException();
            throw new TestSystemException(e.getTargetException());
	  }
	}
      }
    }
    throw new TestSystemException("No matching method found");
  }

  /**
   * return true if given method exists
   * @param clazz - meta class 
   * @param methodName - name of methods
   * @param methodTypes - types of arguments
   */
  public static boolean hasMethod(
        String clazz,
        String methodName, 
        Class[] methodTypes) {
    try {
      return hasMethod(Class.forName(clazz), methodName, methodTypes);
    } catch (Exception e) {
      return false;
    }
  }


  /**
   * return true if given method exists
   * @param clazz - meta class 
   * @param methodName - name of methods
   * @param methodTypes - types of arguments
   */
  public static boolean hasMethod(
        Class clazz,
        String methodName, 
        Class[] methodTypes) {
    try {
      Method method = clazz.getMethod(methodName, methodTypes);
      return method != null;
    } catch (Exception e) {
      return false;
    }
  }



  /**
   * returns primitive array element 
   * @param type - type of primitive
   * @param array - array
   * @param index - index of array
   */
  public static Object readPrimitiveArray(
        String type, 
        Object array, 
        int index
        ) throws  Exception {
    if (type == null) throw new IllegalArgumentException("type is not specified");
    if (array == null) throw new IllegalArgumentException("array is not specified");
    String methodName = TypeHelper.getPrimitiveArrayMethod(type);
    Method method = Array.class.getMethod(methodName, new Class[] {Object.class, int.class});
    try {
      method.setAccessible(true);
      Object value = method.invoke(null, new Object[] {array, new Integer(index)});
      return value;
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getTargetException() instanceof Exception) throw (Exception) e.getTargetException();
      throw new TestSystemException(e.getTargetException());
    }
  }

  /**
   * returns primitive array element 
   * @param type - type of primitive
   * @param array - array
   * @param index - index of array
   */
  public static Object readArray(
        String type, 
        Object array, 
        int index
        ) throws  Exception {
    if (type == null) throw new IllegalArgumentException("type is not specified");
    if (array == null) throw new IllegalArgumentException("array is not specified");
    Method method = Array.class.getMethod("get", new Class[] {Object.class, int.class});
    try {
      method.setAccessible(true);
      return method.invoke(null, new Object[] {array, new Integer(index)});
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getTargetException() instanceof Exception) throw (Exception) e.getTargetException();
      throw new TestSystemException(e.getTargetException());
    }
  }

  /**
   * returns field for given name in given type
   * @param type - type of primitive
   * @param name - name of field
   * @return - field
   */
  public static Field getField(Class type, String name) {
    try {
      Field result = type.getDeclaredField(name);
      result.setAccessible(true);
      return result;
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException("Could not access " + type.getName() + "." + name + " field");
    }
  }

  /**
   * writes attribute of field for given instance
   * @param field - class field
   * @param instance - object instance
   * @param value - attribute value
   */
  public static void writeField(Field field, Object instance, Object value) {
    try {
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Could not write " + field.getType().getName() + "." + field.getName() + " field");
    }
  }
  /**
   * writes attribute of field for given instance
   * @param name - name of field
   * @param instance - object instance
   * @param value - attribute value
   */
  public static void writeField(String name, Object instance, Object value) {
    Field field = getField(instance.getClass(), name);
    writeField(field, instance, value);
  }

  /**
   * reads attribute of field for given instance
   * @param name - name of field
   * @param instance - object instance
   * @return field value
   */
  public static Object readField(String name, Object instance) {
    Field field = getField(instance.getClass(), name);
    return readField(field, instance);
  }
 
  /**
   * reads attribute of field for given instance
   * @param field - class field
   * @param instance - object instance
   * @return field value
   */
  public static Object readField(Field field, Object instance) {
    try {
      field.setAccessible(true);
      return field.get(instance);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Could not read " + field.getType().getName() + "." + field.getName() + " field");
    }
  }


  /**
   * Return true if the given class, method, field or constructor includes the abstract modifer, 
   * false otherwise.
   */
  public static boolean isAbstract(Member member) {
    int mod = member.getModifiers();
    return Modifier.isAbstract(mod);
  }


  /**
   * Return true if the given class, method, field or constructor includes the final modifer, 
   * false otherwise.
   */
  public static boolean isFinal(Member member) {
    int mod = member.getModifiers();
    return Modifier.isFinal(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the interface modifer, 
   * false otherwise.
   */
  public static boolean isInterface(Member member) {
    int mod = member.getModifiers();
    return Modifier.isInterface(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the native modifer, 
   * false otherwise.
   */
  public static boolean isNative(Member member) {
    int mod = member.getModifiers();
    return Modifier.isNative(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the private modifer, 
   * false otherwise.
   */
  public static boolean isPrivate(Member member) {
    int mod = member.getModifiers();
    return Modifier.isPrivate(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the protected modifer, 
   * false otherwise.
   */
  public static boolean isProtected(Member member) {
    int mod = member.getModifiers();
    return Modifier.isProtected(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the public modifer, 
   * false otherwise.
   */
  public static boolean isPublic(Member member) {
    int mod = member.getModifiers();
    return Modifier.isPublic(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the static modifer, 
   * false otherwise.
   */
  public static boolean isStatic(Member member) {
    int mod = member.getModifiers();
    return Modifier.isStatic(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the strict modifer, 
   * false otherwise.
   */
  public static boolean isStrict(Member member) {
    int mod = member.getModifiers();
    return Modifier.isStrict(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the synchronized modifer, 
   * false otherwise.
   */
  public static boolean isSynchronized(Member member) {
    int mod = member.getModifiers();
    return Modifier.isSynchronized(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the transient modifer, 
   * false otherwise.
   */
  public static boolean isTransient(Member member) {
    int mod = member.getModifiers();
    return Modifier.isTransient(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the volatile modifer, 
   * false otherwise.
   */
  public static boolean isVolatile(Member member) {
    int mod = member.getModifiers();
    return Modifier.isVolatile(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the abstract modifer, 
   * false otherwise.
   */
  public static boolean isAbstract(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isAbstract(mod);
  }


  /**
   * Return true if the given class, method, field or constructor includes the final modifer, 
   * false otherwise.
   */
  public static boolean isFinal(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isFinal(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the interface modifer, 
   * false otherwise.
   */
  public static boolean isInterface(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isInterface(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the native modifer, 
   * false otherwise.
   */
  public static boolean isNative(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isNative(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the private modifer, 
   * false otherwise.
   */
  public static boolean isPrivate(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isPrivate(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the protected modifer, 
   * false otherwise.
   */
  public static boolean isProtected(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isProtected(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the public modifer, 
   * false otherwise.
   */
  public static boolean isPublic(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isPublic(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the static modifer, 
   * false otherwise.
   */
  public static boolean isStatic(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isStatic(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the strict modifer, 
   * false otherwise.
   */
  public static boolean isStrict(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isStrict(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the synchronized modifer, 
   * false otherwise.
   */
  public static boolean isSynchronized(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isSynchronized(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the transient modifer, 
   * false otherwise.
   */
  public static boolean isTransient(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isTransient(mod);
  }

  /**
   * Return true if the given class, method, field or constructor includes the volatile modifer, 
   * false otherwise.
   */
  public static boolean isVolatile(Class klass) {
    int mod = klass.getModifiers();
    return Modifier.isVolatile(mod);
  }
  private transient static Logger logger = Logger.getLogger(ReflectHelper.class.getName());
}
