package com.plexobject.testplayer.util;

import junit.framework.TestSuite;


// JUnitDoclet begin import
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class UtilSuite
// JUnitDoclet begin extends_implements
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  // JUnitDoclet end class
  
  public static TestSuite suite() {
    
    TestSuite suite;
    
    suite = new TestSuite("com.plexobject.testplayer.util");
    
    suite.addTestSuite(com.plexobject.testplayer.util.TypeHelperTest.class);
    suite.addTestSuite(com.plexobject.testplayer.util.StringHelperTest.class);
    suite.addTestSuite(com.plexobject.testplayer.util.ReflectHelperTest.class);
    suite.addTestSuite(com.plexobject.testplayer.util.CodeHelperTest.class);
    
    
    
    // JUnitDoclet begin method suite
    // JUnitDoclet end method suite
    
    return suite;
  }
  
  public static void main(String[] args) {
    // JUnitDoclet begin method testsuite.main
    junit.textui.TestRunner.run(suite());
    // JUnitDoclet end method testsuite.main
  }
}