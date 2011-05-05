/* ===========================================================================
 * $RCS$
 * Version: $Id: Constants.java,v 1.2 2006/03/10 15:15:03 shahzad Exp $
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
import java.io.*;
import java.text.*;
import java.util.*;


/**
 * Constants defines common constants for generating tests.
 *
 * @author shahzad bhatti
 *
 * Version: $Id: Constants.java,v 1.2 2006/03/10 15:15:03 shahzad Exp $
 *
 * modification history
 * date         who             what
 * 9/11/05      SB              created.
 */

public interface Constants { 
  public final static String LICENSE                         = "testplayer.license";
  public final static String JUNIT_VERSION                   = "junit.version";
  public final static String TEMPLATE_NAME                   = "testplayer.template.name";

  //
  public final static String MARKER_IMPORT_BEGIN             = "testplayer.marker.import.begin";
  public final static String MARKER_IMPORT_END               = "testplayer.marker.import.end";
  public final static String MARKER_EXTENDS_IMPLEMENTS_BEGIN = "testplayer.marker.extends_implements.begin";
  public final static String MARKER_EXTENDS_IMPLEMENTS_END   = "testplayer.marker.extends_implements.end";
  public final static String MARKER_CLASS_BEGIN              = "testplayer.marker.class.begin";
  public final static String MARKER_CLASS_END                = "testplayer.marker.class.end";
  public final static String MARKER_METHOD_BEGIN             = "testplayer.marker.method.begin";
  public final static String MARKER_METHOD_END               = "testplayer.marker.method.end";
  public final static String MARKER_JAVADOC_CLASS_BEGIN      = "testplayer.marker.javadoc_class.begin";
  public final static String MARKER_JAVADOC_CLASS_END        = "testplayer.marker.javadoc_class.end";
  public final static String MARKER_JAVADOC_METHOD_BEGIN     = "testplayer.marker.javadoc_method.begin";
  public final static String MARKER_JAVADOC_METHOD_END       = "testplayer.marker.javadoc_method.end";

  //
  public final static String ACCESSOR_TESTS                  = "testplayer.accessor.tests";
  public final static String ACCESSOR_NAME                   = "testplayer.accessor.name";
  public final static String ACCESSOR_TYPE_NAME              = "testplayer.accessor.type.name";
  public final static String ACCESSOR_SET_NAME               = "testplayer.accessor.set.name";
  public final static String ACCESSOR_GET_NAME               = "testplayer.accessor.get.name";

  //
  public final static String TESTSUITE_PACKAGE_NAME          = "testplayer.testsuite.package.name";
  public final static String TESTSUITE_IMPORTS               = "testplayer.testsuite.imports";
  public final static String TESTSUITE_CLASS_NAME            = "testplayer.testsuite.class.name";
  public final static String TESTSUITE_INSTANCE_NAME         = "testplayer.testsuite.instance.name";
  public final static String TESTSUITE_ADD_TESTCASES         = "testplayer.testsuite.add.testcases";
  public final static String TESTSUITE_ADD_TESTSUITES        = "testplayer.testsuite.add.testsuites";

  //
  public final static String TESTCASE_PACKAGE_NAME           = "testplayer.testcase.package.name";
  public final static String TESTCASE_CLASS_NAME             = "testplayer.testcase.class.name";
  public final static String TESTCASE_INSTANCE_NAME          = "testplayer.testcase.instance.name";
  public final static String TESTCASE_INSTANCE_TYPE          = "testplayer.testcase.instance.type";
  public final static String TESTCASE_TESTMETHODS            = "testplayer.testcase.testmethods";
  public final static String TESTCASE_UNMATCHED              = "testplayer.testcase.unmatched";
  public final static String TESTCASE_METHOD_UNMATCHED       = "testplayer.testcase.method.unmatched";

  //
  public final static String ADD_TESTSUITE_TO_TESTSUITE      = "testplayer.add.testsuite.to.testsuite";
  public final static String ADD_TESTCASE_TO_TESTSUITE       = "testplayer.add.testcase.to.testsuite";
  public final static String ADD_IMPORT_TESTSUITE            = "testplayer.add.import.testsuite";
  public final static String ADD_TESTSUITE_NAME              = "testplayer.add.testsuite.name";
  public final static String ADD_TESTCASE_NAME               = "testplayer.add.testcase.name";
  public final static String ADD_IMPORT_NAME                 = "testplayer.add.import.name";

  //
  public final static String TESTMETHOD_NAME                 = "testplayer.testmethod.name";

  // variables holding informations about the device under test. (usefull in javadoc)
  public final static String PACKAGE_NAME                    = "testplayer.package.name";
  public final static String CLASS_NAME                      = "testplayer.class.name";
  public final static String METHOD_NAME                     = "testplayer.method.name";
  public final static String METHOD_SIGNATURE                = "testplayer.method.signature";

  // variables, not required to be defined
  public final static String VALUE_LICENSE =
          "/**\n"+
          " * Generated by TestPlayer, an automated test harness builder (GPL)\n"+
          " *\n" + 
          " * Copyright (c) 2005-2006 Shahzad Bhatti (bhatti@plexobject.com)\n" +
          " *\n" + 
          " * Please see http://testplayer.dev.java.net\n" +
          " */\n";

  public final static String VALUE_MARKER_BEGIN                    = "// TestPlayer begin ";
  public final static String VALUE_MARKER_END                      = "// TestPlayer end ";
  public final static String VALUE_MARKER_IMPORT                   = "import";
  public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS       = "extends_implements";
  public final static String VALUE_MARKER_CLASS                    = "class";
  public final static String VALUE_MARKER_METHOD                   = "method";
  public final static String VALUE_MARKER_JAVADOC_CLASS            = "javadoc_class";
  public final static String VALUE_MARKER_JAVADOC_METHOD           = "javadoc_method";
  public final static String VALUE_MARKER_IMPORT_BEGIN             = VALUE_MARKER_BEGIN + VALUE_MARKER_IMPORT;
  public final static String VALUE_MARKER_IMPORT_END               = VALUE_MARKER_END + VALUE_MARKER_IMPORT;
  public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN = VALUE_MARKER_BEGIN + VALUE_MARKER_EXTENDS_IMPLEMENTS;
  public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS_END   = VALUE_MARKER_END + VALUE_MARKER_EXTENDS_IMPLEMENTS;
  public final static String VALUE_MARKER_CLASS_BEGIN              = VALUE_MARKER_BEGIN + VALUE_MARKER_CLASS;
  public final static String VALUE_MARKER_CLASS_END                = VALUE_MARKER_END + VALUE_MARKER_CLASS;
  public final static String VALUE_MARKER_METHOD_BEGIN             = VALUE_MARKER_BEGIN + VALUE_MARKER_METHOD;
  public final static String VALUE_MARKER_METHOD_END               = VALUE_MARKER_END + VALUE_MARKER_METHOD;
  public final static String VALUE_MARKER_JAVADOC_CLASS_BEGIN      = VALUE_MARKER_BEGIN + VALUE_MARKER_JAVADOC_CLASS;
  public final static String VALUE_MARKER_JAVADOC_CLASS_END        = VALUE_MARKER_END + VALUE_MARKER_JAVADOC_CLASS;
  public final static String VALUE_MARKER_JAVADOC_METHOD_BEGIN     = VALUE_MARKER_BEGIN + VALUE_MARKER_JAVADOC_METHOD;
  public final static String VALUE_MARKER_JAVADOC_METHOD_END       = VALUE_MARKER_END + VALUE_MARKER_JAVADOC_METHOD;
  public final static String VALUE_METHOD_UNMATCHED_NAME           = "testVault";
  public final static String VALUE_METHOD_UNMATCHED_NAME_MARKER    = "testcase." + VALUE_METHOD_UNMATCHED_NAME;

  public final static String TEMPLATE_ATTRIBUTE_DEFAULT            = "default";
  public final static String TEMPLATE_ATTRIBUTE_ARRAY              = "array";
  public final static String TEMPLATE_ATTRIBUTE_ACCESSOR           = "accessor";
}
