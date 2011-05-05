/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodEntry.java,v 2.28 2007/07/15 19:30:44 shahzad Exp $
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
import com.plexobject.testplayer.util.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.lang.reflect.*;
import org.apache.log4j.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;

/**
 * This class stores call record for each method invocation that matched 
 * user specification
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/11/05      SB              created.
 */
public class MethodEntry implements Serializable {
  public static class MarshallError extends RuntimeException {
    public final int size;
    public MarshallError(int size) {
      this.size = size;
    }
  }



  public MethodEntry() {
  }


  /**
   * MethodEntry initializes call record upon entry to the method
   * @param id - unique id of call throughout all method invocations
   * @param caller - this object that is invoking method
   * @param callee - target object whose method is being invoked
   * @param signature - signature of the method
   * @param args - argument values
   */
  public MethodEntry(
        long id, 
        long parentID, 
        Object caller, 
        Object callee, 
        String signature, 
        Object[] args,
        boolean constructor,
	int depth) {
    this(id, null, caller, callee, signature, args, constructor, depth);
    this.parentID = parentID;
  }

  /**
   * MethodEntry initializes call record upon entry to the method
   * @param parent - caller's method that is invoking this method
   * @param caller - this object that is invoking method
   * @param callee - target object whose method is being invoked
   * @param signature - signature of the method
   * @param args - argument values
   */
  public MethodEntry(
        long id, 
        MethodEntry parent, 
        Object caller, 
        Object callee, 
        String signature, 
        Object[] args,
        boolean constructor,
	int depth) {
    this.parent = parent;
    this.id = id;
    this.parentID = parent != null ? parent.getId() : Long.MIN_VALUE;
    this.caller = caller;
    this.callee = callee;
    this.signature = signature;
    this.args = args;
    this.constructor = constructor;
    this.started = System.nanoTime();
    this.depth = depth;
  }


  /**
   * @return class name of caller for the method invocation
   */
  public String getCallerName() {
    return caller == null ? null : caller.getClass().getName();
  }


  public String getCallerNameWithoutPackage() {
    return CodeHelper.getTypeWithoutPackage(getCallerName());
  }


  /**
   * @return package name of caller's class for the method invocation
   */
  public String getCallerPackageName() {
    return CodeHelper.getPackageName(getCallerName());
  }


  /**
   * @return class name of callee or target for the method invocation
   */
  public String getCalleeName() {
    if (signature == null) throw new IllegalArgumentException("Null signature");
    if (callee != null) return callee.getClass().getName();
    int end = signature.indexOf('(');
    String name = signature.substring(0, end);
    name = name.replaceAll(keywords, "");
    String[] tokens = name.split("\\s");
    name = tokens[tokens.length-1];
    //
    if (!isConstructor()) { 
      end = name.lastIndexOf('.');
      name = name.substring(0, end);
    }
    return name;
  }

  /**
   * @return modifiers of method
   */
  public synchronized String getModifiers() {
    if (modifier == null) {
      int end = signature.indexOf('(');
      modifier = signature.substring(0, end);
    }
    return modifier;
  }


  public boolean isPrivate() {
    return getModifiers().indexOf("private") != -1;
  } 
  public boolean isProtected() {
    return getModifiers().indexOf("protected") != -1;
  } 
  public boolean isPublic() {
    return getModifiers().indexOf("public") != -1;
  } 
  public boolean isStatic() {
    return getModifiers().indexOf("static") != -1;
  } 
  public boolean isAbstract() {
    return getModifiers().indexOf("abstract") != -1;
  } 


  /**
   * @return class name of callee without package
   */
  public String getCalleeNameWithoutPackage() {
    return CodeHelper.getTypeWithoutPackage(getCalleeName());
  }


  /**
   * @return class name of callee without package. If the type is array
   * then basic type is returned
   */
  public String getBasicCalleeNameWithoutPackage() {
    return CodeHelper.getBasicTypeWithoutPackage(getCalleeName());
  }


  
  /**
   * @return package name of caller's class for the method invocation
   */
  public String getCalleePackageName() {
    return CodeHelper.getPackageName(getCalleeName());
  }


  /**
   * @return - true if method is void, i.e., it does not return anything
   */
  public boolean isVoidMethod() {
    return "void".equals(getMethodReturnType()); 
  }


  /**
   * @return - name of the method 
   */
  public String getMethodName() {
    if (signature == null) throw new IllegalArgumentException("Null signature");
    int end = signature.indexOf('(');
    if (end == -1) throw new IllegalArgumentException("Bad signature " + signature);
    String name = signature.substring(0, end);
    int start = name.lastIndexOf('.');
    return name.substring(start+1);
  }


  public boolean isObjectMethod() {
    String method = getMethodName();
    if ("hashCode".equals(method)) {
      return args.length == 0;
    } else if ("toString".equals(method)) {
      return args.length == 0;
    } else if ("equals".equals(method)) {
      return args.length == 1 && getArgumentTypes()[0].indexOf("Object") != -1;
    }
    return false;
  }



  /**
   * @return - name of the method with number of times it is invoked
   * with different arguments 
   */
  public String getMethodNameWithInvocationCount() {
    String method = getUniqueMethodName(); 
    return method.substring(0, 1).toUpperCase() + method.substring(1) + sameMethodNumber;
  }

  /**
   * @return - name of the method, however if given method is defined 
   * more than once then type of arguments are added to the name.
   */
  public String getUniqueMethodName() {
    String method = getMethodName(); 
    if (TypeHelper.getMethodCount(getCalleeName(), method) == 1) {
       return method;
    } else {
      StringBuilder sb = new StringBuilder(method);
      String[] types = getArgumentTypes();
      for (int i=0; types != null && i<types.length; i++) {
        String type = CodeHelper.getTypeWithoutPackage(types[i]);
        for (int j=0; j<type.length(); j++) {
          char ch = type.charAt(j);
          if (!Character.isWhitespace(ch) && Character.isLetterOrDigit(ch)) {
            sb.append(ch);
          }
        }
      }
      return sb.toString();
    }
  }




  /**
   * @return - return type of the method 
   */
  public String getMethodReturnType() {
    if (signature == null) throw new IllegalArgumentException("Null signature");
    String name = signature.substring(0, signature.indexOf('('));
    name = name.replaceAll(keywords, "");
    String[] tokens = name.split("\\s");
    return tokens[0];
  }

  /**
   * @return - returns list of exceptions thrown by this method or empty array
   * if none.
   */
  public String[] throwsMethodExceptions() {
    if (signature == null) throw new IllegalArgumentException("Null signature");
    int start = signature.indexOf("throws ");
    if (start == -1) return new String[0];
    return signature.substring(start+7).split(", ");
  }


  /**
   * @return - return type of the method 
   */
  public String getMethodReturnTypeWithoutPackage() {
    return CodeHelper.getTypeWithoutPackage(getMethodReturnType()); 
  }

  /**
   * @return - argument types for the method
   */
  public String[] getArgumentTypes() {
    if (signature == null) throw new IllegalArgumentException("Null signature");
    int end = signature.indexOf('(');
    String args = signature.substring(end+1, signature.indexOf(')'));
    List types = new ArrayList();
    StringTokenizer st = new StringTokenizer(args, ", ");
    while (st.hasMoreTokens()) {
      String next = (String) st.nextToken();
      types.add(next);
    }
    return (String[]) types.toArray(new String[types.size()]);
  }

  /**
   * @return - stringified version of this object
   */
  public String toString() {
    return toShortString();
  }


  /**
   * @return - long stringified version of this object
   */
  public String toLongString() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    String callerBuffer = "";
    if (getCaller() != null) callerBuffer = getCallerNameWithoutPackage() + "=>";
    return 
        //id + "/" + parentID + ":" + sdf.format(new Date(started)) + 
        callerBuffer +
	getCalleeNameWithoutPackage() + "." + getMethodName() + "(" +
        //getCalleeName() + "><" + signature + ">(" + 
        argsAsString() + ")->" + rvalue + "/" + exception; // + "{" + getResponseTime() + " millis/#" + getTimesCalled() + "}";
  }

  public String toShortString() {
    //String pinfo = parent != null ? parent.getId() + "/" + parent.getCalleeNameWithoutPackage() + "." + parent.getMethodName() : "";
    List list = new ArrayList();
    MethodEntry next = this;
    while (next != null) {
      list.add(0, next);
      next = next.getParent();
    }
    StringBuilder sb = new StringBuilder(); // id + ":" + depth +

    for (int i=0; i<list.size(); i++) {
      next = (MethodEntry) list.get(i);
      if (i==0 && next.getCaller() != null) sb.append(next.getCallerNameWithoutPackage() + "=>");
      if (i>0) sb.append("->");
      sb.append(getCalleeNameWithoutPackage() + "." + getMethodName()); 
    }
    //sb.append("!");
    sb.append("{" + getResponseTime() + " millis/#" + getTimesCalled() + "}");
    return sb.toString();
  }

  public String toSimpleString() {
    StringBuilder sb = new StringBuilder();
    if (getCaller() != null) sb.append(getCallerNameWithoutPackage() + "=>");
    sb.append(getCalleeNameWithoutPackage() + "." + getMethodName()); 
    sb.append("!");
    return sb.toString();
  }

  public String toMiniString() {
    return getCalleeNameWithoutPackage() + "." + getMethodName(); 
  }


  /**
   * @return - overrides hashcode - it uses id to create hashcode
   */
  public int hashCode() {
    return (int) id;
  }


  /**
   * @return - true if other object is same as this 
   */
  public boolean equals(Object o) {
    if (o == null || o instanceof MethodEntry == false) return false;
    if (o == this) return true;
    MethodEntry other = (MethodEntry) o;
    //return other.id == id;
    return this.toShortString().equals(other.toShortString());
  }
  /**
   * @return - true if other object has same Signature
   */
  public boolean sameSignature(MethodEntry other) {
    if (other == null) return false;
    if (other == this) return true;
    try {
      return this.signature.equals(other.signature);
    } catch (RuntimeException e) {
      return false;
    }
  }


  /**
   * @return  - unique identifier for this method invocation
   * @uml.property  name="id"
   */
  public long getId() {
    return id;
  }


  /**
   * @param id  The id to set.
   * @uml.property  name="id"
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return  - parent identifier for this method invocation
   * @uml.property  name="parentID"
   */
  public long getParentID() {
    return parentID;
  }


  /**
   * @param parentID  The parentID to set.
   * @uml.property  name="parentID"
   */
  public void setParentID(long parentID) {
    this.parentID = parentID;
  }


  /**
   * @return  - returns object that is invoking method
   * @uml.property  name="caller"
   */
  public Object getCaller() {
    return caller;
  }


  /**
   * @return  - returns object that is being invoked
   * @uml.property  name="callee"
   */
  public Object getCallee() {
    return callee;
  }


  /**
   * @return  - method signature
   * @uml.property  name="signature"
   */
  public String getSignature() {
    return signature;
  }
  /**
   * @param signature  The signature to set.
   * @uml.property  name="signature"
   */
  public void setSignature(String sig) {
    this.signature = sig;
  }

  /**
   * @return signature string along with actual values of all parameters
   */
  public String getSignatureValues() {
    return signature + argsAsString();
  }


  /**
   * @return - returns argument values for the method
   */
  public Object[] getArgumentValues() {
    return args;
  }
  public void setArgumentValues(Object[] args) {
    this.args = args;
  }


  /**
   * @return - returns method return value or exception if it failed
   * @throws Exception
   */
  public Object getReturnValue() throws Exception {
    if (exception != null) throw exception;
    return rvalue;
  }


  /**
   * @param rvalue - sets return type 
   */
  public void setReturnValue(Object rvalue) {
    this.rvalue = rvalue;
    this.finished = System.nanoTime();
  }

  /**
   * @return  - returns exception if method was failed otherwise null
   * @uml.property  name="exception"
   */
  public Exception getException() {
    return exception;
  }

  /**
   * @return  - returns true if this method threw exception.
   */
  public boolean hasException() {
    return exception != null; 
  }

  /**
   * @return  - returns true if this method threw exception.
   */
  public String getExceptionType() {
    if (exception != null) return exception.getClass().getName(); 
    return null;
  }


  /**
   * @param rvalue  - return value of the method or exception if it
   *  threw exception
   * @uml.property  name="exception"
   */
  public void setException(Exception e) {
    this.exception = e;
    this.finished = System.nanoTime();
  }

  /**
   * @return - get response time for this method or -1 if method is 
   * not finished yet.
   */
  public long getResponseTime() {
    if (this.finished < 0) return -1;
    return this.finished - this.started;
  }


  public Date getStarted() {
    return new Date(started);
  }
  public Date getFinished() {
    return new Date(finished);
  }

  public boolean isCompleted() {
    return this.finished >= 0;
  }


  /**
   * @return  parent of this call, i.e., caller's method that is invoking   this method
   * @uml.property  name="parent"
   */
  public MethodEntry getParent() {
    return this.parent;
  }

  /**
   * @param parent  of this call, i.e., caller's method that is invoking   this method
   * @uml.property  name="parent"
   */
  public void setParent(MethodEntry rec) {
    this.parent = rec;
    if (rec != null) this.parentID = rec.getId();
    else this.parentID = Integer.MIN_VALUE;
  }


  /**
   * isTopMethod - checks if this method call is top-most method
   * @return true if this call is topmost method call
   */
  public boolean isTopMethod() {
    return caller == null;
  }


  /**
   * getTopMethod return top-most method
   * @return - returns topmost method call
   */
  public MethodEntry getTopMethod() {
    if (caller == null) return this;
    MethodEntry p = this;
    while (p.parent != null) {
      p = p.parent;
    }
    return p;
  }

  /**
   * getMethodDepth return depth of call stack for this method
   * @return - return depth of call stack for this method
   */
  public int getMethodDepth() {
    if (caller == null) return 0;
    int depth = 0;
    MethodEntry p = this;
    while (p.parent != null) {
      p = p.parent;
      depth++;
    }
    return depth;
  }

  /**
   * getSameMethodNumber 
   * @return  - if same method is called multiple times but with different  arguments, then it returns the number for that invocation.  Note that testplayer will ignore methods invoked multiple times  with same arguments.
   * @uml.property  name="sameMethodNumber"
   */
  public int getSameMethodNumber() {
    return sameMethodNumber;
  }

  /**
   * setSameMethodNumber 
   * @param  - num if same method is called multiple times but with different  
   * arguments, then it sets the number for that invocation.  
   * Note that testplayer will ignore methods invoked multiple times 
   * with same arguments.
   * @uml.property  name="sameMethodNumber"
   */
  public void setSameMethodNumber(int num) {
    this.sameMethodNumber = num;
  }
  /**
   * getTimesCalled
   * @return  - # of times the method is called consecutively in a loop
   */
  public int getTimesCalled() {
    return timesCalled;
  }
  public void setTimesCalled(int times) {
    this.timesCalled = times;
  }

  /**
   * incrTimesCalled
   * This method increments # of times this method is called in a loop.
   */
  public void incrTimesCalled() {
    this.timesCalled++;
  }

  /**
   * return true if this method is constructor
   */
  public boolean isConstructor() {
    return constructor; //getMethodName().equals(getCalleeNameWithoutPackage()) || getMethodName().equals("new");
  }

  /**
   * return true if this method is setter
   */
  public boolean isSetterMethod() {
    String[] args = getArgumentTypes(); 
    String method = getMethodName();
    return args.length == 1 && method.startsWith("set");
  }


  /**
   * return true if this method is getter
   */
  public boolean isGetterMethod() {
    String[] args = getArgumentTypes(); 
    String method = getMethodName();
    return args.length == 0 && (
	method.startsWith("get") || method.startsWith("is"));
  }



  /**
   * @return - returns true if all of the arguments are of primitive type
   */
  public boolean allPrimitiveArgs() {
    //if (exception != null) return false;
    String[] types = getArgumentTypes(); 
    if (types == null || types.length == 0) return true;

    for (int i=0; types != null && i<types.length; i++) {
      if (types[i] != null && !TypeHelper.isPrimitive(types[i]) &&
        !TypeHelper.isPrimitiveWrapper(types[i])) return false;
    }
    return true;
  }


  /**
   * @return - returns true if return value is of primitive type
   */
  public boolean isPrimitiveReturn() {
    String rtype = getMethodReturnType();
    return TypeHelper.isPrimitive(rtype) || 
        TypeHelper.isPrimitiveWrapper(rtype);
  }


  /**
   * @return - returns true if return value is of primitive type
   */
  public boolean isPrimitiveArrayReturn() {
    String rtype = getMethodReturnType();
    return TypeHelper.isPrimitiveArray(rtype) || 
        TypeHelper.isPrimitiveWrapperArray(rtype);
  }


  /**
   * @return - returns true if all of the arguments and return value
   * are of primitive type
   */
  public boolean allPrimitiveArgsAndReturn() {
    return allPrimitiveArgs() && isPrimitiveReturn(); 
  }

  /**
   * @return - returns true if return value are of collection type
   */
  public boolean isReturnCollectionType() {
    return TypeHelper.isCollection(getMethodReturnType()); 
  }

  /**
   * @return - returns true if return value are of array type
   */
  public boolean isReturnArrayType() {
    String type = getMethodReturnType(); 
    try {
      return type.endsWith("[]");
    } catch (Exception e) {}
    return false;
  }


  /**
   * @return - returns all arguments as comma delimited string
   */
  public String argsAsString() {
    StringBuilder sb = new StringBuilder();
    for (int i=0; args != null && i<args.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(args[i]); // + " (" + args.getClass().getName() + ")");
    }
    return sb.toString();
  }


  /**
   * @return - returns all types as comma delimited string
   */
  public String typesAsString() {
    String[] types = getArgumentTypes(); 
    StringBuilder sb = new StringBuilder();
    for (int i=0; types != null && i<types.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(types[i]);
    }
    return sb.toString();
  }


  /**
   * returns class type using reflection
   * @return - class type type using reflection
   */
  public Class getMetaClass() throws Exception {
    return callee != null ? callee.getClass() : Class.forName(getCalleeName());
  }



  /**
   * returns method type using reflection
   * @return - method type using reflection
   */
  public Method getMetaMethod() throws Exception {
    Class kass = getMetaClass();
    try {
      String[] typeNames = getArgumentTypes();
      Class[] types = new Class[typeNames.length];
      for (int i=0; i<typeNames.length; i++) {
        try {
          if (TypeHelper.isPrimitive(typeNames[i])) {
            types[i] = TypeHelper.getPrimitiveClass(typeNames[i]);
          } else {
            types[i] = Class.forName(TypeHelper.toInternalType(typeNames[i], false));
          }
        } catch (ClassNotFoundException e) {
          logger.error("Failed to find type name " + typeNames[i] + ", type " + TypeHelper.toInternalType(typeNames[i], true));
        }
      }
      return kass.getMethod(getMethodName(), types);
    } catch (NoSuchMethodException e) {
      if (kass != null) {
        String methodName = getMethodName();
        Method[] methods = kass.getMethods();
        for (int i=0; methods != null && i<methods.length; i++) {
          if (methods[i].getName().equals(methodName)) {
            logger.error("Found similar method " + methods[i] + ", but failed to find with signature " + e);
          }
        }
      }
      throw e;
    }
  }


  /**
   * invokes the method using reflection
   * @return - executes method using reflection and returns method return value
   */
  public Object invoke() throws Exception {
    try {
      Method method = getMetaMethod();
      method.setAccessible(true);
      return method.invoke(callee, args);
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getTargetException() instanceof Exception) throw (Exception) e.getTargetException();
      throw new TestSystemException(e.getTargetException());
    }
  }


  public int getDepth() {
    return this.depth;
  }
  public void setDepth(int d) {
    this.depth = d;
  }

  public void setProperty(String name, String value) {
    getProperties().setProperty(name, value);
  }
  public String getProperty(String name) {
    return getProperties().getProperty(name);
  }


  public synchronized Properties getProperties() {
    if (properties == null) properties = new Properties();
    return properties;
  }
  public void setProperties(Properties p) {
    this.properties = p;
  }

  public byte[] getBinaryArgs() {
    return toBytes(args);
  }
  public void setBinaryArgs(byte[] b) {
    args = (Object[]) toObject(b);
  }
  public byte[] getBinaryRvalue() {
    return toBytes(rvalue);
  }
  public void setBinaryRvalue(byte[] b) {
    rvalue = toObject(b);
  }
  public byte[] getBinaryException() {
    return toBytes(exception);
  }
  public void setBinaryException(byte[] b) {
    exception = (Exception) toObject(b);
  }
  public byte[] getBinaryCallee() {
    return toBytes(callee);
  }
  public void setBinaryCallee(byte[] b) {
    callee = toObject(b);
  }
  public byte[] getBinaryCaller() {
    return toBytes(caller);
  }
  public void setBinaryCaller(byte[] b) {
    callee = toObject(b);
  }
  public byte[] getBinaryProperties() {
    return toBytes(properties);
  }
  public void setBinaryProperties(byte[] b) {
    properties = (Properties) toObject(b);
  }


  public String getSerializedArgs() {
    try {
      String xml = marshal(args);
      if (xml != null) this.argsSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.argsSize = e.size;
      return null;
    }
  }


  public void setSerializedArgs(String b) {
    args = (Object[]) unmarshal(b);
  }
  public String getSerializedRvalue() {
    try {
      String xml = marshal(rvalue);
      if (xml != null) this.rvalueSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.rvalueSize = e.size;
      return null;
    }
  }
  public void setSerializedRvalue(String b) {
    rvalue = unmarshal(b);
  }
  public String getSerializedException() {
    try {
      String xml = marshal(exception);
      if (xml != null) this.rvalueSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.rvalueSize = e.size;
      return null;
    }
  }
  public void setSerializedException(String b) {
    exception = (Exception) unmarshal(b);
  }
  public String getSerializedCallee() {
    try {
      String xml = marshal(callee);
      if (xml != null) this.calleeSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.calleeSize = e.size;
      return null;
    }
  }
  public void setSerializedCallee(String b) {
    callee = unmarshal(b);
  }
  public String getSerializedCaller() {
    try {
      String xml = marshal(caller);
      if (xml != null) this.callerSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.callerSize = e.size;
      return null;
    }
  }
  public void setSerializedCaller(String b) {
    callee = unmarshal(b);
  }
  public String getSerializedProperties() {
    try {
      String xml = marshal(properties);
      if (xml != null) this.propertiesSize = xml.length();
      return xml;
    } catch (MarshallError e) {
      this.propertiesSize = e.size;
      return null;
    }
  }
  public void setSerializedProperties(String b) {
    properties = (Properties) unmarshal(b);
  }


  public long getAverageTotalElapsed() {
    return totalElapsed / timesCalled;
  }
  public long getTotalResponseTime() {
    return totalElapsed;
  }
  public void setTotalResponseTime(long l) {
    this.totalElapsed = l;
  }
  public void incrTotalResponseTime(long l) {
    this.totalElapsed += l;
  }
  public int getTotalMemory() {
    return totalMemory;
  }
  public void setTotalMemory(int t) {
    this.totalMemory = t;
  }
  public int getFreeMemory() {
    return freeMemory;
  }
  public void setFreeMemory(int t) {
    this.freeMemory = t;
  }
  public int getArgsSize() {
    return argsSize;
  }
  public void setArgsSize(int t) {
    this.argsSize = t;
  }
  public int getRvalueSize() {
    return rvalueSize;
  }
  public void setRvalueSize(int t) {
    this.rvalueSize = t;
  }
  public int getCalleeSize() {
    return calleeSize;
  }
  public void setCalleeSize(int t) {
    this.calleeSize = t;
  }
  public int getCallerSize() {
    return callerSize;
  }
  public void setCallerSize(int t) {
    this.callerSize = t;
  }
  public int getPropertiesSize() {
    return propertiesSize;
  }
  public void setPropertiesSize(int t) {
    this.propertiesSize = t;
  }



  public void setStarted(Date d) {
    this.started = d != null ? d.getTime() : 0;
  }
  public void setFinished(Date d) {
    this.finished = d != null ? d.getTime() : 0;
  }
  public void setConstructor(boolean b) {
    this.constructor = b;
  }
  public void setResponseTime(long l) {
  }

  public static String marshal(Object object) {
    if (object instanceof Serializable == false || object == null) return null;
    String xml = context.getDefaultMarshaller().marshal(object);
    if (xml.length() > maxFieldSize) {
       throw new IllegalArgumentException("XML Size " + xml.length() + " is too big");
       //logger.error("Skipping saving large object of size " + xml.length() + ": " + object);
       //xml = null;
    }
    return xml;
    //return new String(toBytes(object));
  }
  protected static byte[] toBytes(Object object) {
    if (object instanceof Serializable == false || object == null) return null;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(object);
      oos.close();
      bos.close();
      byte[] b = bos.toByteArray();
      if (b.length > maxFieldSize) {
        logger.error("Skipping saving large object of size " + b.length + ": " + object);
        return null;
      } else {
        return b;
      }
    } catch (Throwable e) {
      logger.error("Failed to serialize " + object, e);
      return null;
    }
  }


  public static Object unmarshal(String s) {
    if (s == null || s.length() == 0) return null;
    return context.getDefaultMarshaller().unmarshal(s);
    //return toObject(s.getBytes());
  }

  protected static Object toObject(byte[] bytes) {
    if (bytes == null || bytes.length == 0) return null;
    try {
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
      Object object = ois.readObject();
      ois.close();
      return object;
    } catch (Throwable e) {
      logger.error("Failed to deserialize " + bytes.length, e);
      return null;
    }
  }

  ///////////////////////////////////////////////////////////////////////
  private long id;
  private long parentID = Long.MIN_VALUE;
  private String signature;
  private Object[] args;
  private Object rvalue;
  private Exception exception;
  private int depth;
  private int sameMethodNumber;
  private int timesCalled = 1;
  private boolean constructor;


  private transient Object callee;
  private transient String modifier;
  //
  private transient Object caller;
  private transient long started = -1;
  private transient long finished = -1;
  private transient MethodEntry parent;
  private transient Properties properties;
  private long totalElapsed;
  private int totalMemory;
  private int freeMemory;
  private int argsSize;
  private int rvalueSize;
  private int calleeSize;
  private int callerSize;
  private int propertiesSize;
  private static final Logger logger = Logger.getLogger(MethodEntry.class.getName());
  private static final String keywords = "(public|native|final|protected|private|volatile|abstract|synchronized|interface|static|strict|transient)\\s+";
  private static ApplicationContext context = new ApplicationContext();
  public static final String MAX_FIELD_SIZE = "testplayer.dao.max_field_size.classes";
  public static final int maxFieldSize = context.getConfig().getInteger(MAX_FIELD_SIZE, 1024);
}


