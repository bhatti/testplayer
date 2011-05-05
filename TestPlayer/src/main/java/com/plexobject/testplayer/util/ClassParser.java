/* ===========================================================================
 * $RCS$
 * Version: $Id: ClassParser.java,v 1.2 2007/07/11 13:53:48 shahzad Exp $
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
import java.util.*;
import java.io.*;


/**
 * <B>CLASS COMMENTS</B>
 * Class Name: ClassParser
 * Class Description: 
 *   ClassParser parses class 
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 *
 * Design
 *
 * ClassFile keeps array of cp_info (Constant Pool) that stores information
 * about the class. All other classes refer to this constant pool by index.
 * cp_info can have following types of information
 *      - CONSTANT_Class_info
 *         stores name_index that points back to constant pool of Utf8_info 
 *         structure that stores name of the class.             
 *      - CONSTANT_Fieldref_info
 *         stores class_index that points back to constant pool of 
 *         CONSTANT_Class_info type and name_and_type_index that points
 *         back to constant pool of type CONSTANT_NameAndType_info .
 *      - CONSTANT_Methodref_info
 *         stores class_index that points back to constant pool of 
 *         CONSTANT_Class_info type and name_and_type_index that points
 *         back to constant pool of type CONSTANT_NameAndType_info.
 *         If name starts with '<' it is special <init> function
 *      - CONSTANT_InterfaceMethodref_info
 *         stores class_index that points back to constant pool of 
 *         CONSTANT_Class_info type and name_and_type_index that points
 *         back to constant pool of type CONSTANT_NameAndType_info.
 *         If name starts with '<' it is special <init> function
 *      - CONSTANT_String_info
 *         stores string_index that points back to constant pool of type
 *         CONSTANT_Utf8_info.
 *      - CONSTANT_Integer_info
 *        It stores integer
 *      - CONSTANT_Float_info
 *        It stores float
 *      - CONSTANT_Long_info
 *        It stores long 
 *      - CONSTANT_Double_info
 *        It stores double
 *      - CONSTANT_NameAndType_info
 *         stores name_index that points back to constant pool of type
 *         CONSTANT_Utf8_info for the name and descriptor_index that points
 *         back to constant pool of type CONSTANT_Utf8_info.
 *      - CONSTANT_Utf8_info
 *         stores String value
 * Som of these above in turn can have indexes into constant pool.
 * ClassFile 
 *  It is starting point that reads information about version and loads 
 *  constant pool into an array. It stores this_class index into
 *  constant pool (CONSTANT_Class_info), super_class index into constant
 *  pool. It stores array of indexes of for interfaces, each points
 *  to constant pool of structure CONSTANT_Class_info. It also store
 *  array of classes of types field_info, method_info and attribute_info.
 * field_info
 *  It stores flags, name_index that points to constant pool of type
 *  CONSTANT_Class_info, descriptor_index that points to constant pool
 *  of type CONSTANT_Class_info, array of attribute_info.
 * method_info
 *  It stores flags, name_index that points to constant pool of type
 *  CONSTANT_Class_info, descriptor_index that points to constant pool
 * attribute_info
 *  It stores attribute_name_index that points to constant pool of type
 *  CONSTANT_Class_info, attribute_length and byte array of information.
 *  However in this class, I am not converting or interpreting this byte
 *  array to proper structure.
 *
 * Descriptors
 * MethodDescriptor:
 * ( ParameterDescriptor* ) ReturnDescriptor
 * states that a MethodDescriptor represents a left parenthesis, followed by zero or more ParameterDescriptor values, followed by a right parenthesis, followed by
 * a ReturnDescriptor.
 *  
 * 4.3.2 Field Descriptors
 * A field descriptor represents the type of a class, instance, or local variable.
 * It is a series of characters generated by the grammar:
 * FieldDescriptor:
 * FieldType         
 * ComponentType:
 * FieldType
 *  
 * FieldType:
 * BaseType
 * ObjectType
 * ArrayType
 *  
 * BaseType:
 * B
 * C
 * D
 * F
 * I
 * J
 * S
 * Z              
 * ObjectType:
 * L <classname> ;
 * ArrayType:
 * [ ComponentType
 * 
 * Machine Instructions
 * opcode  byte    short   int     long    float   double  char    reference
 * Tipush  bipush  sipush
 * Tconst                  iconst  lconst  fconst  dconst          aconst
 * Tload                   iload   lload   fload   dload           aload
 * Tstore                  istore  lstore  fstore  dstore          astore
 * Tinc                    iinc
 * Taload  baload  saload  iaload  laload  faload  daload  caload  aaload
 * Tastore bastore sastore iastore lastore fastore dastore castore aastore
 * Tadd                    iadd    ladd    fadd    dadd
 * Tsub                    isub    lsub    fsub    dsub
 * Tmul                    imul    lmul    fmul    dmul
 * Tdiv                    idiv    ldiv    fdiv    ddiv
 * Trem                    irem    lrem    frem    drem
 * Tneg                    ineg    lneg    fneg    dneg
 * Tshl                    ishl    lshl
 * Tshr                    ishr    lshr
 * Tushr                   iushr   lushr
 * Tand                    iand    land
 * Tor                     ior     lor
 * Txor                    ixor    lxor
 * i2T     i2b     i2s             i2l     i2f     i2d
 * l2T                     l2i             l2f     l2d
 * f2T                     f2i     f2l             f2d
 * d2T                     d2i     d2l     d2f
 * Tcmp                            lcmp
 * Tcmpl                                   fcmpl   dcmpl
 * Tcmpg                                   fcmpg   dcmpg
 * if_TcmpOP                       if_icmpOP
 * if_acmpOP
 * Treturn                 ireturn lreturn freturn dreturn         areturn
 *
 * Note
 *      BaseType Character      Type    Interpretation
 *      B       byte    signed byte
 *      C       char    Unicode character
 *      D       double  double-precision floating-point value
 *      F       float   single-precision floating-point value
 *      I       int     integer
 *      J       long    long integer
 *      L<classname>;   reference       an instance of class <classname>
 *      S       short   signed short
 *      Z       boolean         true or false
 *      [       reference       one array dimension
 *
 * Modification History
 * Initial      Date            Changes
 * SAB          Apr 22, 1999    Created
*/

public class ClassParser {
  public static final int MAGIC = 0xCAFEBABE;
  public static final int ACC_PUBLIC = 0x0001;  // Declared public
  public static final int ACC_FINAL = 0x0010;   // Declared final
  public static final int ACC_SUPER = 0x0020;   // Treat superclass methods 
                //specially when invoked by the invokespecial instruction.
  public static final int ACC_INTERFACE = 0x0200;  // Is an interface
  public static final int ACC_ABSTRACT = 0x0400; // Declared abstract

  public ClassParser() {
  }
  public ClassFile[] process(String filename) 
        throws java.io.IOException, ClassFormatError {
    return process(getStream(filename));
  }
  public ClassFile[] process(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
    Vector vecClassFiles = new Vector();
    ClassFile cf = new ClassFile(in);
    vecClassFiles.addElement(cf);
    String type = getClassName(cf.constant_pool, cf.this_class);
    String[] innerClasses = cf.getInnerClasses(type.replace('/', '.'));
    for (int i=0; i<innerClasses.length; i++) {
      String path = TypesExtractor.getPath(innerClasses[i]);
      if (path == null) continue;
      in = getStream(path, innerClasses[i]);
      if (in == null) return new ClassFile[0];
      ClassFile[] arrcf = process(in);
      for (int j=0; j<arrcf.length ; j++) {
        if (vecClassFiles.indexOf(arrcf[j]) == -1) 
          vecClassFiles.addElement(arrcf[j]);
      }
    }
    ClassFile[] classFiles = new ClassFile[vecClassFiles.size()];
    vecClassFiles.copyInto(classFiles);
    return classFiles;
  }

  public class ClassFile {
    public ClassFile(String filename) 
        throws java.io.IOException, ClassFormatError {
      this(getStream(filename));
    }
    public ClassFile(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      //magic = in.readInt() & 0xFFFFFFFF;
      magic = in.readInt();
      if (magic != MAGIC) {
        throw new ClassFormatError("Illegal magic code " + Integer.toHexString((int)magic));
      }
      minor_version = in.readUnsignedShort();
      major_version = in.readUnsignedShort();
      constant_pool_count = in.readUnsignedShort();
      constant_pool = new cp_info[constant_pool_count];
      for (int i=1; i<constant_pool_count; i++) {
        constant_pool[i] = new cp_info(in);
        if (constant_pool[i].tag == 0) {
          
        } else if (constant_pool[i].tag == cp_info.CONSTANT_Long ||
                  constant_pool[i].tag == cp_info.CONSTANT_Double) {
          //These two takes two spots in the table
          i++;
        }
      }
      access_flags = in.readUnsignedShort();
      this_class = in.readUnsignedShort();
      super_class = in.readUnsignedShort();
      interfaces_count = in.readUnsignedShort();
      interfaces = new int[interfaces_count];
      for (int i=0; i<interfaces_count; i++) {
        interfaces[i] = in.readUnsignedShort();;
      }
      fields_count = in.readUnsignedShort();
      fields = new field_info[fields_count];
      for (int i=0; i<fields_count ; i++) {
        fields[i] = new field_info(in);
      }
      methods_count = in.readUnsignedShort();
      methods = new method_info[methods_count];
      for (int i=0; i<methods_count; i++) {
        methods[i] = new method_info(in);
      }
      attributes_count = in.readUnsignedShort();
      attributes = new attribute_info[attributes_count];
      for (int i=0; i<attributes_count; i++) {
        attributes[i] = new attribute_info(in);
      }
      if (in.read() != -1) {
        System.err.println("**************** Data left in class *********");
      }
      in.close();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(8192);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("minor_version=" + minor_version + ',');
      buffer.append("major_version=" + major_version + '\n');
      buffer.append("constant_pool_count=" + constant_pool_count + ',');
      for (int i=1; i<constant_pool_count; i++) {
        buffer.append("constant_pool[" + i + "]=" + constant_pool[i] + ',');
      }
      buffer.append('\n');
      buffer.append("access_flags=" + access_flags + ',');
      buffer.append("this_class=" + this_class + ',');
      buffer.append("super_class=" + super_class + '\n');
      buffer.append("interfaces_count=" + interfaces_count + ',');
      for (int i=0; i<interfaces_count; i++) {
        buffer.append("interfaces[" + i + "]=" + interfaces[i] + ',');
      }
      buffer.append('\n');
      buffer.append("fields_count=" + fields_count + '\n');
      for (int i=0; i<fields_count; i++) {
        buffer.append("fields[" + i + "]=" + fields[i] + ',');
      }
      buffer.append('\n');
      buffer.append("methods_count=" + methods_count + '\n');
      for (int i=0; i<methods_count; i++) {
        buffer.append("methods[" + i + "]=" + methods[i] + ',');
      }
      buffer.append('\n');
      buffer.append("attributes_count=" + attributes_count + '\n');
      for (int i=0; i<attributes_count; i++) {
        buffer.append("attributes[" + i + "]=" + attributes[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }


    public String[] getInnerClasses(String type) {
      if (verbose) System.err.println("@@@@@@@@@@@@@@ Finding inner class for "
        + type);
      String[] types = getTypes();
      Vector vecInner = new Vector();
      for (int i=0; i<types.length; i++) {
        if (types[i].startsWith(type + '$') && 
          types[i].substring(type.length()+1).indexOf('.') == -1) {
          if (vecInner.indexOf(types[i]) == -1) {
            if (verbose) System.err.println("@@@@@@@@@@@@@@ Found Innerclass " + types[i]);
            vecInner.addElement(types[i]);
          }
        }
      }
      String[] inners = new String[vecInner.size()];
      vecInner.copyInto(inners);
      return inners;
    }
    public String[] getConstants() {
      Vector vecConst = new Vector();
      getConstants(vecConst); 
      String[] consts = new String[vecConst.size()];
      vecConst.copyInto(consts);
      return consts;
    }
    public void getConstants(Vector vecConst) {
      if (verbose) {
        for (int i=1; i<constant_pool_count; i++) {
          if (constant_pool[i] != null) {
            if (constant_pool[i].tag == cp_info.CONSTANT_Utf8) {
              System.err.println("---- UTF8 ----");
            }
            System.err.println("---- tag " + constant_pool[i].tag + 
                " *constant_pool[" + i + "] = " + constant_pool[i].info +
                ", Utf8 const " + cp_info.CONSTANT_Utf8);
          }
        }
      }
      for (int i=1; i<constant_pool_count; i++) {
        if (constant_pool[i] == null) continue;
        if (constant_pool[i].tag == cp_info.CONSTANT_Utf8) {
          String value = constant_pool[i].asCONSTANT_Utf8_info().value;
          vecConst.addElement(value);
        }
      }
    }



    public String[] getTypes() {
      Vector vecTypes = new Vector();
      getTypes(vecTypes); 
      String[] types = new String[vecTypes.size()];
      vecTypes.copyInto(types);
      return types;
    }
    public void getTypes(Vector vecTypes) {
      if (verbose) {
        for (int i=1; i<constant_pool_count; i++) {
          if (constant_pool[i] != null) {
            System.err.println("types -- *constant_pool[" + i + "]=" + 
                        constant_pool[i].info);
          }
        }
      }
/*
        for (int i=1; i<constant_pool_count; i++) {
          if (constant_pool[i].tag == cp_info.CONSTANT_Utf8) {
            System.err.println("utf at constant_pool[" + i + "]=" + getUtf8(constant_pool, i));
          }
        }
        for (int i=0; i<attributes_count; i++) {
          System.err.println("attributes[" + i + "]=" + attributes[i]);
        }
        for (int i=1; i<constant_pool_count; i++) {
          System.err.println("*constant_pool[" + i + "]=" + constant_pool[i]);
        }
*/
      addClass(constant_pool, this_class, vecTypes);
      if (super_class > 0) addClass(constant_pool, super_class, vecTypes);
      for (int i=0; i<interfaces_count; i++) {
        addClass(constant_pool, interfaces[i], vecTypes);
      }
      for (int i=0; i<fields_count; i++) {
        //addNondup(getUtf8(constant_pool, fields[i].name_index), vecTypes);
        addNondup(getUtf8(constant_pool, fields[i].descriptor_index), vecTypes);
        for (int j=0; j<fields[i].attributes_count; j++) {
          add(constant_pool, fields[i].attributes_count, fields[i].attributes, vecTypes);
        }
      }
      for (int i=0; i<methods_count; i++) {
        //addNondup(getUtf8(constant_pool, methods[i].name_index), vecTypes);
        addNondup(getUtf8(constant_pool, methods[i].descriptor_index), vecTypes);
        for (int j=0; j<methods[i].attributes_count; j++) {
          add(constant_pool, methods[i].attributes_count, methods[i].attributes, vecTypes);
        }
      }

      add(constant_pool, attributes_count, attributes, vecTypes);

      for (int i=1; i<constant_pool_count; i++) {
        if (constant_pool[i] == null) continue;
        switch (constant_pool[i].tag) {
          case cp_info.CONSTANT_Class:
            addClass(constant_pool, i, vecTypes);
            break;
          case cp_info.CONSTANT_NameAndType:
            add(constant_pool, constant_pool[i].asCONSTANT_NameAndType_info(), vecTypes);
            break;
          case cp_info.CONSTANT_Fieldref:
            add(constant_pool, constant_pool[i].asCONSTANT_Fieldref_info(), vecTypes);
            break;
          case cp_info.CONSTANT_Methodref:
            add(constant_pool, constant_pool[i].asCONSTANT_Methodref_info(), vecTypes);
            break;
          case cp_info.CONSTANT_InterfaceMethodref:
            add(constant_pool, constant_pool[i].asCONSTANT_InterfaceMethodref_info(),vecTypes);
            break;
/*
          case cp_info.CONSTANT_Utf8:
            add(constant_pool, constant_pool[i].asCONSTANT_Utf8_info(),vecTypes);
            break;
*/
        }
      }
    }



    public final long magic; // u4
    public final int minor_version; // u2
    public final int major_version; // u2
    // The value of the constant_pool_count item is equal to the number of 
    // entries in the constant_pool table plus one. A constant_pool index is 
    // considered valid if it is greater than zero and less than 
    // constant_pool_count, with the exception for constants of type long 
    // and double.
    public final int constant_pool_count; // u2

    // The constant_pool is a table of structures representing various string
    // constants, class and interface names, field names, and other 
    // constants that are referred to within the ClassFile structure and its 
    // substructures. The format of each constant_pool table entry is 
    // indicated by its first "tag" byte.  The constant_pool table is 
    // indexed from 1 to constant_pool_count-1.
    public final cp_info[] constant_pool;       //[constant_pool_count-1];
    public final int access_flags; // u2
    public final int this_class; // u2 -- index into constant_pool
                                   // CONSTANT_Class_info
    public final int super_class; // u2 -- index into constant_pool
                                    // CONSTANT_Class_info
                                    // zero or valid index into constant_pool
                                    // If zero this is Object class
    public final int interfaces_count; // u2
    public final int[] interfaces; // u2 [interfaces_count]
                        // Index into constant_pool - CONSTANT_Class_info
    public final int fields_count; // u2
    public final field_info[] fields; // [fields_count];
    public final int methods_count; // u2
    public final method_info[] methods; // [methods_count];
    public final int attributes_count; // u2
    public final attribute_info[] attributes; // [attributes_count];
  }

  // Java virtual machine instructions do not rely on the runtime layout of 
  // classes, interfaces, class instances, or arrays. Instead, instructions 
  // refer to symbolic information in the constant_pool table.
  public class cp_info {
    public static final byte CONSTANT_Class = 7;
    public static final byte CONSTANT_Fieldref = 9;
    public static final byte CONSTANT_Methodref = 10;
    public static final byte CONSTANT_InterfaceMethodref = 11;
    public static final byte CONSTANT_String = 8;
    public static final byte CONSTANT_Integer = 3;
    public static final byte CONSTANT_Float = 4;
    public static final byte CONSTANT_Long = 5;
    public static final byte CONSTANT_Double = 6;
    public static final byte CONSTANT_NameAndType = 12;
    public static final byte CONSTANT_Utf8 = 1;

    public cp_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      tag = in.readUnsignedByte();
      switch (tag) {
        case 0:
          info = "";
          break;
        case CONSTANT_Class:
          info = new ClassParser.CONSTANT_Class_info(in);
          break;
        case CONSTANT_Fieldref:
          info = new ClassParser.CONSTANT_Fieldref_info(in);
          break;
        case CONSTANT_Methodref:
          info = new ClassParser.CONSTANT_Methodref_info(in);
          break;
        case CONSTANT_InterfaceMethodref:
          info = new ClassParser.CONSTANT_InterfaceMethodref_info(in);
          break;
        case CONSTANT_String:
          info = new ClassParser.CONSTANT_String_info(in);
          break;
        case CONSTANT_Integer:
          info = new ClassParser.CONSTANT_Integer_info(in);
          break;
        case CONSTANT_Float:
          info = new ClassParser.CONSTANT_Float_info(in);
          break;
        case CONSTANT_Long:
          info = new ClassParser.CONSTANT_Long_info(in);
          break;
        case CONSTANT_Double:
          info = new ClassParser.CONSTANT_Double_info(in);
          break;
        case CONSTANT_NameAndType:
          info = new ClassParser.CONSTANT_NameAndType_info(in);
          break;
        case CONSTANT_Utf8:
          info = new ClassParser.CONSTANT_Utf8_info(in);
          break;
        default:
          throw new ClassFormatError("Illegal tag " + tag);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("info=" + info);
      buffer.append(")\n");
      return buffer.toString();
    }
    public ClassParser.CONSTANT_Class_info asCONSTANT_Class_info() {
      if (tag != CONSTANT_Class) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Class_info) info;
    }
    public ClassParser.CONSTANT_Fieldref_info asCONSTANT_Fieldref_info() {
      if (tag != CONSTANT_Fieldref) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Fieldref_info) info;
    }
    public ClassParser.CONSTANT_Methodref_info asCONSTANT_Methodref_info() {
      if (tag != CONSTANT_Methodref) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Methodref_info) info;
    }
    public ClassParser.CONSTANT_InterfaceMethodref_info asCONSTANT_InterfaceMethodref_info() {
      if (tag != CONSTANT_InterfaceMethodref) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_InterfaceMethodref_info) info;
    }
    public ClassParser.CONSTANT_String_info asCONSTANT_String_info() {
      if (tag != CONSTANT_String) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_String_info) info;
    }
    public ClassParser.CONSTANT_Integer_info asCONSTANT_Integer_info() {
      if (tag != CONSTANT_Integer) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Integer_info) info;
    }
    public ClassParser.CONSTANT_Float_info asCONSTANT_Float_info() {
      if (tag != CONSTANT_Float) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Float_info) info;
    }
    public ClassParser.CONSTANT_Long_info asCONSTANT_Long_info() {
      if (tag != CONSTANT_Long) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Long_info) info;
    }
    public ClassParser.CONSTANT_Double_info asCONSTANT_Double_info() {
      if (tag != CONSTANT_Double) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_Double_info) info;
    }
    public ClassParser.CONSTANT_NameAndType_info asCONSTANT_NameAndType_info() {
      if (tag != CONSTANT_NameAndType) 
         throw new IllegalArgumentException("Illegal tag " + tag + "[" + toString() + "]");
      return (ClassParser.CONSTANT_NameAndType_info) info;
    }
    public ClassParser.CONSTANT_Utf8_info asCONSTANT_Utf8_info() {
      if (tag != CONSTANT_Utf8) 
         throw new IllegalArgumentException("Illegal tag " + tag + " [" + toString() + "]");
      return (ClassParser.CONSTANT_Utf8_info) info;
    }
    public final int tag; // u1
    public final Object info;
    //public final byte[] info; // u1
  }

  public class field_info {
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010; 
    public static final int ACC_VOLATILE = 0x0040; // cannot be cached.
    public static final int ACC_TRANSIENT = 0x0080; 

    public field_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      access_flags = in.readUnsignedShort();
      name_index = in.readUnsignedShort();
      descriptor_index = in.readUnsignedShort();
      attributes_count = in.readUnsignedShort();
      attributes = new attribute_info[attributes_count];
      for (int i=0; i<attributes_count; i++) {
        attributes[i] = new attribute_info(in);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("access_flags=" + access_flags + ',');
      buffer.append("name_index=" + name_index + ',');
      buffer.append("descriptor_index=" + descriptor_index + '\n');
      buffer.append("attributes_count=" + attributes_count + ',');
      for (int i=0; i<attributes_count; i++) {
        buffer.append("attributes[" + i + "]=" + attributes[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int access_flags;
    public final int name_index; // index into constant pool
                        // CONSTANT_Utf8_info -- field name
    public final int descriptor_index; // index into constant pool
                        // CONSTANT_Utf8_info -- field descriptor
    public final int attributes_count;
    public final attribute_info[] attributes; // [attributes_count]
  }


  public class method_info {
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_ABSTRACT = 0x0400; 
    public static final int ACC_STRICT = 0x0800;  // Declared strictfp

    public method_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      access_flags = in.readUnsignedShort();
      name_index = in.readUnsignedShort();
      descriptor_index = in.readUnsignedShort();
      attributes_count = in.readUnsignedShort();
      attributes = new attribute_info[attributes_count];
      for (int i=0; i<attributes_count; i++) {
        attributes[i] = new attribute_info(in);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("access_flags=" + access_flags + ',');
      buffer.append("name_index=" + name_index + ',');
      buffer.append("descriptor_index=" + descriptor_index + '\n');
      buffer.append("attributes_count=" + attributes_count + ',');
      for (int i=0; i<attributes_count; i++) {
        buffer.append("attributes[" + i + "]=" + attributes[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int access_flags;
    public final int name_index; // index into constant pool
                        // CONSTANT_Utf8_info -- method name
    public final int descriptor_index; // index into constant pool
                        // CONSTANT_Utf8_info -- method descriptor
    public final int attributes_count;
    public final attribute_info[] attributes; // [attributes_count]
  }


  public class attribute_info {
    public attribute_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      attribute_name_index = in.readUnsignedShort();
      attribute_length = in.readInt();
      info = readBytes((int) attribute_length, in);
    }
    public String getTagName(cp_info[] constant_pool) {
      if (tagname == null || value == null) initValue(constant_pool);
      return tagname;
    }
    public Object getValue(cp_info[] constant_pool) {
      if (value == null) initValue(constant_pool);
      return value;
    }
    private synchronized void initValue(cp_info[] constant_pool) {
      if (value != null) return;
      tagname = constant_pool[attribute_name_index].asCONSTANT_Utf8_info().value;
      try {
        DataInputStream in =new DataInputStream(new ByteArrayInputStream(info));
        if (tagname.equals("Code")) {
          value =new Code_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("Exceptions")) {
          value = new Exceptions_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("InnerClasses")) {
          value = new InnerClasses_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("Synthetic")) {
          value = new Synthetic_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("SourceFile")) {
          value = new SourceFile_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("LocalVariableTable")) {
          value = new LocalVariableTable_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("LineNumberTable")) {
          value = new LineNumberTable_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("Deprecated")) {
          value = new Deprecated_attribute(attribute_name_index, attribute_length, in);
        } else if (tagname.equals("ConstantValue")) {
          value = new ConstantValue_attribute(attribute_name_index, attribute_length, in);
        }
        in.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public Code_attribute asCode_attribute() {
      if (tagname.equals("Code")) {
        return (Code_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public Exceptions_attribute asExceptions_attribute() {
      if (tagname.equals("Exceptions")) {
        return (Exceptions_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public InnerClasses_attribute asInnerClasses_attribute() {
      if (tagname.equals("InnerClasses")) {
        return (InnerClasses_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public Synthetic_attribute asSynthetic_attribute() {
      if (tagname.equals("Synthetic")) {
        return (Synthetic_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public SourceFile_attribute asSourceFile_attribute() {
      if (tagname.equals("SourceFile")) {
        return (SourceFile_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public LineNumberTable_attribute asLineNumberTable_attribute() {
      if (tagname.equals("LineNumberTable")) {
        return (LineNumberTable_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public LocalVariableTable_attribute asLocalVariableTable_attribute() {
      if (tagname.equals("LocalVariableTable")) {
        return (LocalVariableTable_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public Deprecated_attribute asDeprecated_attribute() {
      if (tagname.equals("Deprecated")) {
        return (Deprecated_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public ConstantValue_attribute asConstantValue_attribute() {
      if (tagname.equals("ConstantValue")) {
        return (ConstantValue_attribute) value;
      } else {
        throw new IllegalArgumentException("Illegal tagname " + tagname);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index+ ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("info=" + info.length);
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int attribute_name_index; // valid unsigned 16bit index into
                                // constant pool of type CONSTANT_Utf8_info
                                // that stores name of attribute
    public final long attribute_length; // length does not include initial six
                        // bytes that contain attribute_name_index & 
                        // attribute_length.
    public final byte[] info; // attribute_length
    private Object value;
    private String tagname; // added helper fieldd
  }


  public class Code_attribute {
    public class exception_info {
      public exception_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
        start_pc = in.readUnsignedShort();
        end_pc = in.readUnsignedShort();
        handler_pc = in.readUnsignedShort();
        catch_type = in.readUnsignedShort();
      }
      public String toString() {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(getClass().getName() + ".toString(");
        buffer.append("start_pc=" + start_pc + ',');
        buffer.append("end_pc=" + end_pc + ',');
        buffer.append("handler_pc=" + handler_pc + ',');
        buffer.append("catch_type=" + catch_type);
        buffer.append(")\n");
        return buffer.toString();
      }

      // The values of the two items start_pc and end_pc indicate the ranges 
      // in the code array at which the exception handler is active. The value 
      // of start_pc must be a valid index into the code array of the opcode 
      // of an instruction. The value of end_pc either must be a valid index 
      // into the code array of the opcode of an instruction or must be equal 
      // to code_length, the length of the code array. The value of start_pc 
      // must be less than the value of end_pc.
      // The start_pc is inclusive and end_pc is exclusive; that is, 
      // the exception handler must be active while the program counter is 
      // within the interval

      public final int start_pc; // range in code array at which exception
        // handler is active. start_pc is inclusive, but end_pc is exclusive
      public final int end_pc;
      public final int handler_pc; // start of exception handler
      public final int catch_type; // index into constant pool
                        // CONSTANT_Class_info type.
    }
    public Code_attribute (int attribute_name_index, long attribute_length,
        DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();
      max_stack = in.readUnsignedShort();
      max_locals = in.readUnsignedShort();
      code_length = in.readInt(); // must be less than 65536.
      //code = readBytes((int) code_length, in);
      code = readUBytes((int) code_length, in);
      exception_table_length = in.readUnsignedShort();
      exception_table = new exception_info[exception_table_length];
      for (int i=0; i<exception_table_length; i++) {
        exception_table[i] = new exception_info(in);
      }
      attributes_count = in.readUnsignedShort();
      attributes = new attribute_info[attributes_count];
      for (int i=0; i<attributes_count; i++) {
        attributes[i] = new attribute_info(in);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("max_stack=" + max_stack + ',');
      buffer.append("max_locals=" + max_locals + ',');
      buffer.append("code_length=" + code_length + '\n');
      buffer.append("exception_table_length=" + exception_table_length + ',');
      for (int i=0; i<exception_table_length; i++) {
        buffer.append("exception_table[" + i + "]=" + exception_table[i] + ',');
      }
      buffer.append('\n');
      buffer.append("attributes_count=" + attributes_count + ',');
      for (int i=0; i<attributes_count; i++) {
        buffer.append("attributes[" + i + "]=" + attributes[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int attribute_name_index; // u2 index into constant pool
                        // CONSTANT_Utf8_inf
    public final long attribute_length; // u4 length of attribute exclusing
                                // initial six bytes
    public final int max_stack; // u2 max stack size
    public final int max_locals; // u2 max number of local variables
                // The greatest local variable index for a value of type long 
                // or double is max_locals-2. The greatest local variable 
                // index for a value of any other type is max_locals-1.
    public final long code_length; // must be less than 65536.
    public final int[] code; //u1 code_length;
    public final int exception_table_length;
    public final exception_info[] exception_table;
    public final int attributes_count;
    public final attribute_info[] attributes; // [attributes_count]; 
  }

  public class Exceptions_attribute {
    public Exceptions_attribute(int attribute_name_index, 
        long attribute_length, DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

      number_of_exceptions = in.readUnsignedShort();
      exception_index_table = new int[number_of_exceptions];
      for (int i=0; i<number_of_exceptions; i++) {
        exception_index_table[i] = in.readUnsignedShort();
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("number_of_exceptions=" + number_of_exceptions + ',');
      for (int i=0; i<number_of_exceptions; i++) {
        buffer.append("exception_index_table[" + i + "]=" + exception_index_table[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int attribute_name_index; // u2 index into constant pool
                // CONSTANT_Utf8_info
    public final long attribute_length; // attribute length excluding initial
                        // six bytes
    public final int number_of_exceptions;
    public final int[] exception_index_table; // [number_of_exceptions];
                // each value is index into constant pool CONSTANT_Class_info
  }

  public class InnerClasses_attribute {
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;
                                 
    // Every CONSTANT_Class_info entry in the constant_pool table which 
    // represents a class or interface C that is not a package member must 
    // have exactly one corresponding entry in the classes array.
    public class innerclass_info {
      public innerclass_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
        inner_class_info_index = in.readUnsignedShort();
        outer_class_info_index = in.readUnsignedShort();
        inner_name_index = in.readUnsignedShort();
        inner_class_access_flags = in.readUnsignedShort();
      }
      public String toString() {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(getClass().getName() + ".toString(");
        buffer.append("inner_class_info_index=" + inner_class_info_index + ',');
        buffer.append("outer_class_info_index=" + outer_class_info_index + ',');
        buffer.append("inner_name_index=" + inner_name_index + ',');
        buffer.append("inner_class_access_flags=" + inner_class_access_flags);
        buffer.append(")\n");
        return buffer.toString();
      }

      public final int inner_class_info_index; // u2 must be zero or valid ***
                // index into constant pool CONSTANT_Class_info
      public final int outer_class_info_index; // u2 must be zero or valid ***
                // index into constant pool CONSTANT_Class_info
      public final int inner_name_index; // u2 If C is anonymous, value of 
                // inner_name_index must be zero, otherwise it is valid index
                // into constant pool at CONSTANT_Utf8_info that rep. name
      public final int inner_class_access_flags;
    }
    public InnerClasses_attribute(int attribute_name_index,
         long attribute_length, DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

      number_of_classes = in.readUnsignedShort();
      classes = new innerclass_info[number_of_classes];
      for (int i=0; i<number_of_classes; i++) {
        classes[i] = new innerclass_info(in);
      }
    }

    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("number_of_classes=" + number_of_classes + ',');
      for (int i=0; i<number_of_classes; i++) {
        buffer.append("classes[" + i + "]=" + classes[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 length of attribute excluding
                                // initial six bytes
    public final int number_of_classes; // u2 number of entires in class arr.
    public final innerclass_info[] classes; // number_of_classes
  }


  // fixed-length attribute in the attributes table of ClassFile, field_info,
  // and method_info structures
  public class Synthetic_attribute {
    public Synthetic_attribute(int attribute_name_index, long attribute_length,
        DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append(")\n");
      return buffer.toString();
    }

    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 length of attribute excluding
                                // initial six bytes
  }

  // optional fixed-length attribute in the attributes table of ClassFile
  // and method_info structures
  public class SourceFile_attribute {
    public SourceFile_attribute(int attribute_name_index, long attribute_length,
        DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

      sourcefile_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("sourcefile_index=" + sourcefile_index + ',');
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 length of attribute excluding
                                // initial six bytes
    public final int sourcefile_index; // u2 index into constant pool
                // CONSTANT_Utf8_info. String will be interpreted as name of
                // directory containg the file or an absolute path
  }

  // optional variable-length attribute in the attributes table of a Code 
  // (§4.7.3) attribute. 
  public class LineNumberTable_attribute {
    // Each entry in the line_number_table array indicates that the line number
    // in the original source file changes at a given point in the code array. 
    public class line_number_info {
      public line_number_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
        start_pc = in.readUnsignedShort();
        line_number = in.readUnsignedShort();
      }
      public String toString() {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(getClass().getName() + ".toString(");
        buffer.append("start_pc=" + start_pc+ ',');
        buffer.append("line_number=" + line_number);
        buffer.append(")\n");
        return buffer.toString();
      }
      // The value of the start_pc item must indicate the index into the 
      // code array at which the code for a new line in the original source 
      // file begins. The value of start_pc must be less than the value of 
      // the code_length item of the Code attribute of which this 
      // LineNumberTable_attribute is an attribute.
      public final int start_pc; // index into code array at which code for
                // new line in the original file begins
      public final int line_number; // line number in original source file
    }
    public LineNumberTable_attribute(int attribute_name_index, 
        long attribute_length, DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();
      line_number_table_length = in.readUnsignedShort();
      line_number_table = new line_number_info[line_number_table_length];
      for (int i=0; i<line_number_table_length; i++) {
        line_number_table[i] = new line_number_info(in);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("line_number_table_length=" + line_number_table_length + ',');
      for (int i=0; i<line_number_table_length; i++) {
        buffer.append("line_number_table=" + line_number_table[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 length of attribute excluding
                                // initial six bytes
    public final int line_number_table_length; // u2
    public final line_number_info[] line_number_table;//line_number_table_length
  }

  // optional variable-length attribute in the attributes table of a Code 
  // (§4.7.3) attribute. 
  public class LocalVariableTable_attribute {
    // It may be used by debuggers to determine the value of a
    // given local variable during the execution of a method.
    // Each entry in the local_variable_table array indicates a range of code 
    // array offsets within which a local variable has a value. It also 
    // indicates the index into the local variable array of the current frame 
    // at which that local variable can be found. 
    public class local_variable_table_info {
      public local_variable_table_info(DataInputStream in) 
        throws java.io.IOException, ClassFormatError {
        start_pc = in.readUnsignedShort();
        length = in.readUnsignedShort();
        name_index = in.readUnsignedShort();
        descriptor_index = in.readUnsignedShort();
        index = in.readUnsignedShort();
      }
      public String toString() {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(getClass().getName() + ".toString(");
        buffer.append("start_pc=" + start_pc + ',');
        buffer.append("length=" + length + ',');
        buffer.append("name_index=" + name_index + ',');
        buffer.append("descriptor_index=" + descriptor_index + ',');
        buffer.append("index=" + index + ',');
        buffer.append(")\n");
        return buffer.toString();
      }
      public final int start_pc; // given  local variable must have a value 
        // at indices into the code array in the interval 
        // [start_pc, start_pc+length]
      public final int length;
      public final int name_index; // index into constant pool 
                // CONSTANT_Utf8_info -- local variable name
      public final int descriptor_index; // index into constant pool
                // CONSTANT_Utf8_info - containing field descriptor
      public final int index; // index in local variable array of current
                        // frame.
    }
    public LocalVariableTable_attribute(int attribute_name_index, long attribute_length,
        DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

      local_variable_table_length = in.readUnsignedShort();
      local_variable_table = 
                new local_variable_table_info[local_variable_table_length];
      for (int i=0; i<local_variable_table_length; i++) {
        local_variable_table[i] = new local_variable_table_info(in);
      }
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("local_variable_table_length=" + local_variable_table_length + ',');
      for (int i=0; i<local_variable_table_length; i++) {
        buffer.append("local_variable_table=" + local_variable_table[i] + ',');
      }
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 length of attribute excluding
                                // initial six bytes
    public final int local_variable_table_length; // u2 
    public final local_variable_table_info[] local_variable_table;
  }


  public class Deprecated_attribute {
    public Deprecated_attribute(int attribute_name_index, long attribute_length,
        DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

      if (attribute_length != 0) throw new ClassFormatError("Illegal length");
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int attribute_name_index; // u2 index into constant pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 -- it is zero
  }


  // fixed-length attribute used in the attributes table of field_info
  // A ConstantValue attribute represents the value of a constant field 
  // that must be (explicitly or implicitly) static; that is, ACC_STATIC bit 
  public class ConstantValue_attribute {
    public ConstantValue_attribute(int attribute_name_index, 
        long attribute_length, DataInputStream in)
        throws java.io.IOException, ClassFormatError {
      this.attribute_name_index = attribute_name_index;//in.readUnsignedShort();
      this.attribute_length = attribute_length; // in.readInt();

       if (attribute_length != 2) throw new ClassFormatError("Illegal length");
       constantvalue_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("attribute_name_index=" + attribute_name_index + ',');
      buffer.append("attribute_length=" + attribute_length + ',');
      buffer.append("constantvalue_index=" + constantvalue_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int attribute_name_index; // u2 - index into constant_pool
                                // CONSTANT_Utf8_info
    public final long attribute_length; // u4 -- must be 2
    public final int constantvalue_index; // u2 - index into constant pool
                        // type long    CONSTANT_Long
                        // type float   CONSTANT_Float
                        // type double  CONSTANT_Double
                        // type int, short, char, byte, boolean CONSTANT_Integer
                        // type String  CONSTANT_String    
  }


  public class CONSTANT_Class_info {
    public CONSTANT_Class_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Class;
       name_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("name_index=" + name_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int name_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_Utf8_info
    // Because arrays are objects, the opcodes anewarray and multianewarray 
    // can reference array "classes" via CONSTANT_Class_info (§4.4.1) 
    // structures in the constant_pool table. For such array classes, the 
    // name of the class is the descriptor of the array type. 
    // For example, the class name representing a two-dimensional int array type
    //   int[][] is [[I
  }
  public class CONSTANT_Fieldref_info {
    public CONSTANT_Fieldref_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Fieldref;
       class_index = in.readUnsignedShort();
       name_and_type_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("class_index=" + class_index + ',');
      buffer.append("name_and_type_index=" + name_and_type_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int class_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_Class_info
    public final int name_and_type_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_NameAndType_info 
                                   // field descriptor
  }
  public class CONSTANT_Methodref_info {
    public CONSTANT_Methodref_info (DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Methodref;
       class_index = in.readUnsignedShort();
       name_and_type_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("class_index=" + class_index + ',');
      buffer.append("name_and_type_index=" + name_and_type_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int class_index; // u2 -- index into constant pool
                                   // entry at method type
    public final int name_and_type_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_NameAndType_info 
                                   // method descriptor
        // If the name of the method of a CONSTANT_Methodref_info structure 
        // begins with a' <' ('\u003c'), then the name must be the special 
        // name <init>
  }
  public class CONSTANT_InterfaceMethodref_info {
    public CONSTANT_InterfaceMethodref_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_InterfaceMethodref;
       class_index = in.readUnsignedShort();
       name_and_type_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("class_index=" + class_index + ',');
      buffer.append("name_and_type_index=" + name_and_type_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int class_index; // u2 -- index into constant pool
                                   // entry at interface type
    public final int name_and_type_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_NameAndType_info 
                                   // method descriptor
        // If the name of the method of a CONSTANT_Methodref_info structure 
        // begins with a' <' ('\u003c'), then the name must be the special 
        // name <init>
  }

  public class CONSTANT_String_info {
    public CONSTANT_String_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_String;
       string_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("string_index=" + string_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int string_index; // u2 -- index into constant pool
                                   // entry at CONSTANT_Utf8_info
  }
  public class CONSTANT_Integer_info {
    public CONSTANT_Integer_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Integer;
       bytes = in.readInt();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("bytes=" + bytes);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final long bytes; // u4 -- big-endian
  }
  public class CONSTANT_Float_info {
    public static final int POSITIVE_INFINITY = 0x7f800000;
    public static final int NEGATIVE_INFINITY = 0xff800000;
    public static final int NAN_RANGE1_BEGIN = 0x7f800001;
    public static final int NAN_RANGE1_END= 0x7fffffff;
    public static final int NAN_RANGE2_BEGIN = 0xff800001;
    public static final int NAN_RANGE2_END= 0xffffffff;
/*
    public static boolean isNan(int bits) {
      int s = ((bits >> 31) == 0) ? 1 : -1;
        int e = ((bits >> 23) & 0xff);
        int m = (e == 0) ?
                        (bits & 0x7fffff) << 1 :
                        (bits & 0x7fffff) | 0x800000;
Then the float value equals the result of the mathematical expression s·m·2e-150.
      return false;
    }
*/

    public CONSTANT_Float_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Float;
       bytes = in.readInt();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("bytes=" + bytes);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final long bytes; // u4 --  IEEE 754 floating-point single format
                             // big-endian
  }

  // All 8-byte constants take up two entries in the constant_pool table
  // If a CONSTANT_Long_info or CONSTANT_Double_info structure is the item in
  // the constant_pool table at index n, then the next usable item in the pool 
  // is located at index n+2. The constant_pool index n+1 must be valid but 
  // is considered unusable.
  // The unsigned high_bytes and low_bytes items of the CONSTANT_Long_info 
  // structure together represent the value of the long constant ((long) 
  // high_bytes << 32) + low_bytes, where the bytes of each of high_bytes 
  // and low_bytes are stored in big-endian (high byte first) order.
  public class CONSTANT_Long_info {
    public CONSTANT_Long_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Long;
       high_bytes = in.readInt();
       low_bytes = in.readInt();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("low_bytes=" + low_bytes + ',');
      buffer.append("high_bytes=" + high_bytes);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final long high_bytes; // u4 -- big-endian
    public final long low_bytes; // u4 -- big-endian
  }

  // All 8-byte constants take up two entries in the constant_pool table
  // The high_bytes and low_bytes items of the CONSTANT_Double_info structure 
  // together represent the double value in IEEE 754 floating-point double 
  // format (§3.3.2).
  // The value represented by the CONSTANT_Double_info structure is determined 
  // as follows. The high_bytes and low_bytes items are first converted into 
  // the long constant bits, which is equal to ((long) high_bytes << 32) + 
  // low_bytes. 
  public class CONSTANT_Double_info {
    public static final long POSITIVE_INFINITY = 0x7ff0000000000000L;
    public static final long NEGATIVE_INFINITY = 0xfff0000000000000L;
    public static final long NAN_RANGE1_BEGIN = 0x7ff0000000000001L;
    public static final long NAN_RANGE1_END= 0x7fffffffffffffffL;
    public static final long NAN_RANGE2_BEGIN = 0xfff0000000000001L;
    public static final long NAN_RANGE2_END= 0xffffffffffffffffL;
    public CONSTANT_Double_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Double;
       high_bytes = in.readInt();
       low_bytes = in.readInt();
    }
/*
In all other cases, let s, e, and m be three values that might be computed from
bits:
        int s = ((bits >> 63) == 0) ? 1 : -1;
        int e = (int)((bits >> 52) & 0x7ffL);
        long m = (e == 0) ?
                (bits & 0xfffffffffffffL) << 1 :
                (bits & 0xfffffffffffffL) | 0x10000000000000L;
 
 
Then the floating-point value equals the double value of the mathematical expression s·m·2e-1075.   
*/
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("low_bytes=" + low_bytes + ',');
      buffer.append("high_bytes=" + high_bytes);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final long high_bytes; // u4 -- big-endian
    public final long low_bytes; // u4 -- big-endian
  }

  public class CONSTANT_NameAndType_info {
    public CONSTANT_NameAndType_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_NameAndType;
       name_index = in.readUnsignedShort();
       descriptor_index = in.readUnsignedShort();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("name_index=" + name_index + ',');
      buffer.append("descriptor_index=" + descriptor_index);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    public final int name_index; // u2 -- index into constant pool of type
                                   // CONSTANT_Utf8_info -- field or method
    public final int descriptor_index; // u2 index into constant pool of type
                                   // CONSTANT_Utf8_info -- field/method
  }

  /*
   The CONSTANT_Utf8_info structure is used to represent constant string values.
   UTF-8 strings are encoded so that character sequences that contain only 
   non-null ASCII characters can be represented using only 1 byte per character,
   but characters of up to 16 bits can be represented. All characters in the 
   range '\u0001' to '\u007F' are represented by a single byte:
        0       bits 6-0
   The 7 bits of data in the byte give the value of the character represented. 
   The null character ('\u0000') and characters in the range '\u0080' to 
   '\u07FF' are represented by a pair of bytes x and y:
        x: 1    1       0       bits 10-6
        y: 1    0       bits 5-0 
   The bytes represent the character with the value ((x & 0x1f) << 6) + 
        (y & 0x3f). 
   Characters in the range '\u0800' to '\uFFFF' are represented by 3 bytes 
   x, y, and z:
        x: 1    1       1       0       bits 15-12
        y: 1    0       bits 11-6
        z: 1    0       bits 5-0
   The character with the value ((x & 0xf) << 12) + ((y & 0x3f) << 6) + 
        (z & 0x3f) is represented by the bytes.
   The bytes of multibyte characters are stored in the class file in big-endian
   (high byte first) order.
   There are two differences between this format and the "standard" UTF-8 
   format. First, the null byte (byte) is encoded using the 2-byte format 
   rather than the 1-byte format, so that Java virtual machine UTF-8 strings 
   never have embedded nulls. Second, only the 1-byte, 2-byte, and 3-byte 
   formats are used. The Java virtual machine does not recognize the longer 
   UTF-8 formats.  
*/
  public class CONSTANT_Utf8_info {
    public CONSTANT_Utf8_info(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Utf8;
       //length = in.readUnsignedShort();
       //bytes = readBytes(length, in);
       value = in.readUTF();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("value=" + value);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    //public final int length; // u2 -number of bytes in byte array
    //public final byte[] bytes; // no bytes can have value 0 or lie in the
                        // range of 0xf0-0xff
    public final String value;
  }


  public class Code_Instructions {
    public static final int OPCODE_nop = 0;
    public static final int OPCODE_aconst_null = 1;
    public static final int OPCODE_iconst_m1 = 2;
    public static final int OPCODE_iconst_0 = 3;
    public static final int OPCODE_iconst_1 = 4;
    public static final int OPCODE_iconst_2 = 5;
    public static final int OPCODE_iconst_3 = 6;
    public static final int OPCODE_iconst_4 = 7;
    public static final int OPCODE_iconst_5 = 8;
    public static final int OPCODE_lconst_0 = 9;
    public static final int OPCODE_lconst_1 = 10;
    public static final int OPCODE_fconst_0 = 11;
    public static final int OPCODE_fconst_1 = 12;
    public static final int OPCODE_fconst_2 = 13;
    public static final int OPCODE_dconst_0 = 14;
    public static final int OPCODE_dconst_1 = 15;
    public static final int OPCODE_bipush = 16;
    public static final int OPCODE_sipush = 17;
    public static final int OPCODE_ldc = 18;
    public static final int OPCODE_ldc_w = 19;
    public static final int OPCODE_ldc2_w = 20;
    public static final int OPCODE_iload = 21;
    public static final int OPCODE_lload = 22;
    public static final int OPCODE_fload = 23;
    public static final int OPCODE_dload = 24;
    public static final int OPCODE_aload = 25;
    public static final int OPCODE_iload_0 = 26;
    public static final int OPCODE_iload_1 = 27;
    public static final int OPCODE_iload_2 = 28;
    public static final int OPCODE_iload_3 = 29;
    public static final int OPCODE_lload_0 = 30;
    public static final int OPCODE_lload_1 = 31;
    public static final int OPCODE_lload_2 = 32;
    public static final int OPCODE_lload_3 = 33;
    public static final int OPCODE_fload_0 = 34;
    public static final int OPCODE_fload_1 = 35;
    public static final int OPCODE_fload_2 = 36;
    public static final int OPCODE_fload_3 = 37;
    public static final int OPCODE_dload_0 = 38;
    public static final int OPCODE_dload_1 = 39;
    public static final int OPCODE_dload_2 = 40;
    public static final int OPCODE_dload_3 = 41;
    public static final int OPCODE_aload_0 = 42;
    public static final int OPCODE_aload_1 = 43;
    public static final int OPCODE_aload_2 = 44;
    public static final int OPCODE_aload_3 = 45;
    public static final int OPCODE_iaload = 46;
    public static final int OPCODE_laload = 47;
    public static final int OPCODE_faload = 48;
    public static final int OPCODE_daload = 49;
    public static final int OPCODE_aaload = 50;
    public static final int OPCODE_baload = 51;
    public static final int OPCODE_caload = 52;
    public static final int OPCODE_saload = 53;
    public static final int OPCODE_istore = 54;
    public static final int OPCODE_lstore = 55;
    public static final int OPCODE_fstore = 56;
    public static final int OPCODE_dstore = 57;
    public static final int OPCODE_astore = 58;
    public static final int OPCODE_istore_0 = 59;
    public static final int OPCODE_istore_1 = 60;
    public static final int OPCODE_istore_2 = 61;
    public static final int OPCODE_istore_3 = 62;
    public static final int OPCODE_lstore_0 = 63;
    public static final int OPCODE_lstore_1 = 64;
    public static final int OPCODE_lstore_2 = 65;
    public static final int OPCODE_lstore_3 = 66;
    public static final int OPCODE_fstore_0 = 67;
    public static final int OPCODE_fstore_1 = 68;
    public static final int OPCODE_fstore_2 = 69;
    public static final int OPCODE_fstore_3 = 70;
    public static final int OPCODE_dstore_0 = 71;
    public static final int OPCODE_dstore_1 = 72;
    public static final int OPCODE_dstore_2 = 73;
    public static final int OPCODE_dstore_3 = 74;
    public static final int OPCODE_astore_0 = 75;
    public static final int OPCODE_astore_1 = 76;
    public static final int OPCODE_astore_2 = 77;
    public static final int OPCODE_astore_3 = 78;
    public static final int OPCODE_iastore = 79;
    public static final int OPCODE_lastore = 80;
    public static final int OPCODE_fastore = 81;
    public static final int OPCODE_dastore = 82;
    public static final int OPCODE_aastore = 83;
    public static final int OPCODE_bastore = 84;
    public static final int OPCODE_castore = 85;
    public static final int OPCODE_sastore = 86;
    public static final int OPCODE_pop = 87;
    public static final int OPCODE_pop2 = 88;
    public static final int OPCODE_dup = 89;
    public static final int OPCODE_dup_x1 = 90;
    public static final int OPCODE_dup_x2 = 91;
    public static final int OPCODE_dup2 = 92;
    public static final int OPCODE_dup2_x1 = 93;
    public static final int OPCODE_dup2_x2 = 94;
    public static final int OPCODE_swap = 95;
    public static final int OPCODE_iadd = 96;
    public static final int OPCODE_ladd = 97;
    public static final int OPCODE_fadd = 98;
    public static final int OPCODE_dadd = 99;
    public static final int OPCODE_isub = 100;
    public static final int OPCODE_lsub = 101;
    public static final int OPCODE_fsub = 102;
    public static final int OPCODE_dsub = 103;
    public static final int OPCODE_imul = 104;
    public static final int OPCODE_lmul = 105;
    public static final int OPCODE_fmul = 106;
    public static final int OPCODE_dmul = 107;
    public static final int OPCODE_idiv = 108;
    public static final int OPCODE_ldiv = 109;
    public static final int OPCODE_fdiv = 110;
    public static final int OPCODE_ddiv = 111;
    public static final int OPCODE_irem = 112;
    public static final int OPCODE_lrem = 113;
    public static final int OPCODE_frem = 114;
    public static final int OPCODE_drem = 115;
    public static final int OPCODE_ineg = 116;
    public static final int OPCODE_lneg = 117;
    public static final int OPCODE_fneg = 118;
    public static final int OPCODE_dneg = 119;
    public static final int OPCODE_ishl = 120;
    public static final int OPCODE_lshl = 121;
    public static final int OPCODE_ishr = 122;
    public static final int OPCODE_lshr = 123;
    public static final int OPCODE_iushr = 124;
    public static final int OPCODE_lushr = 125;
    public static final int OPCODE_iand = 126;
    public static final int OPCODE_land = 127;
    public static final int OPCODE_ior = 128;
    public static final int OPCODE_lor = 129;
    public static final int OPCODE_ixor = 130;
    public static final int OPCODE_lxor = 131;
    public static final int OPCODE_iinc = 132;
    public static final int OPCODE_i2l = 133;
    public static final int OPCODE_i2f = 134;
    public static final int OPCODE_i2d = 135;
    public static final int OPCODE_l2i = 136;
    public static final int OPCODE_l2f = 137;
    public static final int OPCODE_l2d = 138;
    public static final int OPCODE_f2i = 139;
    public static final int OPCODE_f2l = 140;
    public static final int OPCODE_f2d = 141;
    public static final int OPCODE_d2i = 142;
    public static final int OPCODE_d2l = 143;
    public static final int OPCODE_d2f = 144;
    public static final int OPCODE_i2b = 145;
    public static final int OPCODE_i2c = 146;
    public static final int OPCODE_i2s = 147;
    public static final int OPCODE_lcmp = 148;
    public static final int OPCODE_fcmpl = 149;
    public static final int OPCODE_fcmpg = 150;
    public static final int OPCODE_dcmpl = 151;
    public static final int OPCODE_dcmpg = 152;
    public static final int OPCODE_ifeq = 153;
    public static final int OPCODE_ifne = 154;
    public static final int OPCODE_iflt = 155;
    public static final int OPCODE_ifge = 156;
    public static final int OPCODE_ifgt = 157;
    public static final int OPCODE_ifle = 158;
    public static final int OPCODE_if_icmpeq = 159;
    public static final int OPCODE_if_icmpne = 160;
    public static final int OPCODE_if_icmplt = 161;
    public static final int OPCODE_if_icmpge = 162;
    public static final int OPCODE_if_icmpgt = 163;
    public static final int OPCODE_if_icmple = 164;
    public static final int OPCODE_if_acmpeq = 165;
    public static final int OPCODE_if_acmpne = 166;
    public static final int OPCODE_goto = 167;
    public static final int OPCODE_jsr = 168;
    public static final int OPCODE_ret = 169;
    public static final int OPCODE_tableswitch = 170;
    public static final int OPCODE_lookupswitch = 171;
    public static final int OPCODE_ireturn = 172;
    public static final int OPCODE_lreturn = 173;
    public static final int OPCODE_freturn = 174;
    public static final int OPCODE_dreturn = 175;
    public static final int OPCODE_areturn = 176;
    public static final int OPCODE_return = 177;
    public static final int OPCODE_getstatic = 178;
    public static final int OPCODE_putstatic = 179;
    public static final int OPCODE_getfield = 180;
    public static final int OPCODE_putfield = 181;
    public static final int OPCODE_invokevirtual = 182;
    public static final int OPCODE_invokespecial = 183;
    public static final int OPCODE_invokestatic = 184;
    public static final int OPCODE_invokeinterface = 185;
    public static final int OPCODE_xxxunusedxxx1 = 186;
    public static final int OPCODE_new = 187;
    public static final int OPCODE_newarray = 188;
    public static final int OPCODE_anewarray = 189;
    public static final int OPCODE_arraylength = 190;
    public static final int OPCODE_athrow = 191;
    public static final int OPCODE_checkcast = 192;
    public static final int OPCODE_instanceof = 193;
    public static final int OPCODE_monitorenter = 194;
    public static final int OPCODE_monitorexit = 195;
    public static final int OPCODE_wide = 196;
    public static final int OPCODE_multianewarray = 197;
    public static final int OPCODE_ifnull =  198;
    public static final int OPCODE_ifnonnull =  199;
    public static final int OPCODE_goto_w =  200;
    public static final int OPCODE_jsr_w =  201;
    public static final int OPCODE_breakpoint = 202;
    public static final int OPCODE_impdep1 = 254;
    public static final int OPCODE_impdep2 = 255;

    public Code_Instructions(DataInputStream in)
        throws java.io.IOException, ClassFormatError {
       this.tag = cp_info.CONSTANT_Utf8;
       //length = in.readUnsignedShort();
       //bytes = readBytes(length, in);
       value = in.readUTF();
    }
    public String toString() {
      StringBuilder buffer = new StringBuilder(1024);
      buffer.append(getClass().getName() + ".toString(");
      buffer.append("tag=" + tag + ',');
      buffer.append("value=" + value);
      buffer.append(")\n");
      return buffer.toString();
    }
    public final int tag; // u1
    //public final int length; // u2 -number of bytes in byte array
    //public final byte[] bytes; // no bytes can have value 0 or lie in the
                        // range of 0xf0-0xff
    public final String value;
  }

  public static String[] getTypes(ClassParser.ClassFile[] classInfo) {
    Vector vecTypes = new Vector();
    for (int i=0; i<classInfo.length; i++) {
      String[] types = classInfo[i].getTypes();
      for (int j=0; j<types.length; j++) {
        if (vecTypes.indexOf(types[j]) == -1) vecTypes.addElement(types[j]);
      }
    }
    String[] types = new String[vecTypes.size()];
    vecTypes.copyInto(types);
    return types;
  }
  public static String[] getConstants(ClassParser.ClassFile[] classInfo) {
    Vector vecConst = new Vector();
    for (int i=0; i<classInfo.length; i++) {
      String[] consts = classInfo[i].getConstants();
      for (int j=0; j<consts.length; j++) {
        if (vecConst.indexOf(consts[j]) == -1) vecConst.addElement(consts[j]);
      }
    }
    String[] consts = new String[vecConst.size()];
    vecConst.copyInto(consts);
    return consts;
  }

  private static byte[] readBytes(int length, DataInputStream in) 
        throws java.io.IOException {
    byte[] data = new byte[length];
    int offset = 0;
    int len = length;
    while (len > 0) {
      int n = in.read(data, offset, len);
      if (n == -1) throw new EOFException("EOF while reading " + len +
        "/" + length + " bytes");
      len -= n;
      offset += n;
    }
    return data;
  }
  private static int[] readUBytes(int length, DataInputStream in) 
        throws java.io.IOException {
    int[] data = new int[length];
    for (int i=0; i<length; i++) {
      data[i] = in.readUnsignedByte();
    }
    return data;
  }

  protected static DataInputStream getStream(String file) 
        throws java.io.IOException {
    return new DataInputStream(new BufferedInputStream(
                                new FileInputStream(file)));
  }

  protected static DataInputStream getStream(String file, String type) 
        throws java.io.IOException {
    //if (file == null || file.length() == 0 || file.indexOf("$") != -1) return null;
    if (file.endsWith(".class")) {  // ok
      return new DataInputStream(new BufferedInputStream(
                                new FileInputStream(file)));
    } else if (file.endsWith(".jar")) {
      JarResources jr = new JarResources(file);
      String entry = type.replace('.', '/') + ".class";
      byte[] data = jr.getResource(entry);
      if (data == null) {
        System.out.println("Resource [" + entry + "] not found in " + file);
        return null;
      }
      return new DataInputStream(new ByteArrayInputStream(data));
    } else {
      System.out.println("Unknown resource type " + file);
      return null;
    }
  }


  private static void add(cp_info[] constant_pool, int attributes_count, 
        attribute_info[] attributes, Vector vec) {
    for (int i=0; i<attributes_count; i++) {
      if (attributes[i].getTagName(constant_pool).equals("Code")) {
        Code_attribute attr = attributes[i].asCode_attribute();
        if (false && verbose) {
           System.err.println("Code " + attr.code_length +
                ":" + getUtf8(constant_pool, attr.attribute_name_index));
           for (int j=0; j<attr.code_length; j++) {
             System.err.print(attr.code[j] + "|");
           }
           System.err.println();
        }
        add(constant_pool, attr.attributes_count, attr.attributes, vec);
        // code_length and code
      } else if (attributes[i].getTagName(constant_pool).equals("Exceptions")) {
        Exceptions_attribute attr = attributes[i].asExceptions_attribute();
        for (int j=0; j<attr.number_of_exceptions; j++) {
          addClass(constant_pool, attr.exception_index_table[j], vec);
        }
      } else if (attributes[i].getTagName(constant_pool).equals("InnerClasses")) {
        InnerClasses_attribute attr =attributes[i].asInnerClasses_attribute();
        //addNondup(getUtf8(constant_pool, attr.attribute_name_index), vec);
        for (int j=0; j<attr.number_of_classes; j++) {
          if (attr.classes[j].inner_class_info_index > 0) 
            addClass(constant_pool, attr.classes[j].inner_class_info_index, vec);
          if (attr.classes[j].outer_class_info_index > 0) 
            addClass(constant_pool, attr.classes[j].outer_class_info_index, vec);
          if (attr.classes[j].inner_name_index > 0) {
            if (attr.classes[j].outer_class_info_index > 0) {
              String outer = 
                getClassName(constant_pool, attr.classes[j].outer_class_info_index);
              addNondup('L' + outer + '$' + 
                getUtf8(constant_pool, attr.classes[j].inner_name_index) + ';',
                vec);
            }
          }
        }
      } else if (attributes[i].getTagName(constant_pool).equals("LocalVariableTable")) {
        LocalVariableTable_attribute attr = 
                      attributes[i].asLocalVariableTable_attribute();
        for (int j=0; j<attr.local_variable_table_length; j++) {
          addNondup(getUtf8(constant_pool, 
                attr.local_variable_table[j].descriptor_index), vec);
        }
      } else if (attributes[i].getTagName(constant_pool).equals("Synthetic")) {
        Synthetic_attribute attr = 
                      attributes[i].asSynthetic_attribute();
        addNondup(getUtf8(constant_pool, attr.attribute_name_index), vec);
      } else if (attributes[i].getTagName(constant_pool).equals("LineNumberTable")) {
        LineNumberTable_attribute attr = attributes[i].asLineNumberTable_attribute();
/*
        for (int j=0; j<attr.line_number_table_length; j++) {
          System.err.println(line_number_table[j]);
        }
*/
      } else if (attributes[i].getTagName(constant_pool).equals("SourceFile")) {
        SourceFile_attribute attr = attributes[i].asSourceFile_attribute();
        //System.err.println("Source: " + getUtf8(constant_pool, attr.sourcefile_index));
      } else if (attributes[i].getTagName(constant_pool).equals("Deprecated")) {
      } else if (attributes[i].getTagName(constant_pool).equals("ConstantValue")) {
      } else {
        //System.err.println("???????????? UNKNOWN Attribute [" + attributes[i].getTagName(constant_pool) + "]");
      }
    }
  }

  private static void addClass(cp_info[] constant_pool, int n, Vector vec) {
    String type = getClassName(constant_pool, n);
    if (type.endsWith(";")) addNondup(type, vec);
    else if (type.startsWith("[")) addNondup(type, vec);
    else addNondup('L' + type + ';', vec);
  }

  private static String getClassName(cp_info[] constant_pool, int n) {
    CONSTANT_Class_info clazz = constant_pool[n].asCONSTANT_Class_info();
    CONSTANT_Utf8_info utf = null;
    String type = getUtf8(constant_pool, clazz.name_index);
    if (type.startsWith("(")) {
      type = type.substring(1, type.length()-1);
    }
    return type;
  }

  private static void add(cp_info[] constant_pool, CONSTANT_Fieldref_info info, Vector vec) {
    addClass(constant_pool, info.class_index, vec);
    add(constant_pool, constant_pool[info.name_and_type_index].asCONSTANT_NameAndType_info(), vec);
  }
  private static void add(cp_info[] constant_pool, CONSTANT_Methodref_info info,Vector vec) {
    addClass(constant_pool, info.class_index, vec);
    add(constant_pool, constant_pool[info.name_and_type_index].asCONSTANT_NameAndType_info(), vec);
  }
  private static void add(cp_info[] constant_pool, CONSTANT_InterfaceMethodref_info info,Vector vec) {
    addClass(constant_pool, info.class_index, vec);
    add(constant_pool, constant_pool[info.name_and_type_index].asCONSTANT_NameAndType_info(), vec);
  }
  private static void add(cp_info[] constant_pool, CONSTANT_NameAndType_info info,Vector vec) {
    addNondup(getUtf8(constant_pool, info.descriptor_index), vec);
  }
  private static void add(CONSTANT_Utf8_info info, Vector vec) {
    addNondup(info.value, vec);
  }
  private static String getUtf8(cp_info[] constant_pool, int n) {
    CONSTANT_Utf8_info utf = constant_pool[n].asCONSTANT_Utf8_info();
    return utf.value;
  }
  private static void addNondup(String value, Vector vector) {
    if (value == null || value.length() == 0) return;
    String[] types = null;
    if (value.charAt(0) == '(') {
      int starti = 1;
      int endi = value.indexOf(")");
      if (endi == -1) return;
      String parms = value.substring(starti, endi);
      String[] args = TypesExtractor.getComponentType(parms);
      if (args == null) args = new String[0];
      String rtype = null;
        if (value.length() > endi+1) {
        rtype = value.substring(endi+1);
        }
      String[] rtypes = rtype == null ? 
                new String[0] : TypesExtractor.getComponentType(rtype);
      if (rtypes == null) rtypes = new String[0];
      types = new String[args.length + rtypes.length];
      System.arraycopy(args, 0, types, 0, args.length);
      System.arraycopy(rtypes, 0, types, args.length, rtypes.length);
    } else {
      types = TypesExtractor.getComponentType(value);
    }
    if (debug) {
      if (types == null || types.length == 0) {
        System.err.println("****** failed (" + value + ")");
/*
        try {
          throw new Exception("value [" + value + "] failed");
        } catch (Exception e) {
          e.printStackTrace();
        }
*/
      } else {
        System.err.print("****** succeeded (" + value + ")");
        for (int i=0; types != null && i<types.length; i++) {
          System.err.print("  -- " + types[i]);
        }
        System.err.println();
      }
    }
    for (int i=0; types != null && i<types.length; i++) {
      if (vector.indexOf(types[i]) == -1) {
        if (types[i].startsWith("[")) {
          try {
            throw new Exception("*** invalid type " + types[i] + 
                " from value [" + value + "]");
          } catch (Exception e) {
          e.printStackTrace();
          }
        }
        vector.addElement(types[i]);
      }
    }
  }

  public static void main(String[] args) {
    try {
      String filepath = null;
      if (args[0].endsWith(".class")) filepath = args[0];
      else filepath = TypesExtractor.getPath(args[0]);
      if (filepath == null) return;

      ClassParser parser = new ClassParser();
      ClassParser.ClassFile[] classInfo = parser.process(filepath);
      String[] types = parser.getTypes(classInfo);
      for (int i=0; i<types.length; i++) {
        System.out.println(types[i]);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private static char separator =
        System.getProperty("file.separator").charAt(0);
  private static boolean debug = "true".equals(System.getProperty("debug"));
  private static boolean verbose = "true".equals(System.getProperty("verbose"));
}
