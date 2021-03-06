package com.plexobject.testplayer.plugin;

import junit.framework.TestSuite;
import com.plexobject.testplayer.plugin.dot.DotSuite;
import com.plexobject.testplayer.plugin.test.TestSuite;
import com.plexobject.testplayer.plugin.uml.UmlSuite;


// JUnitDoclet begin import
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class PluginSuite
// JUnitDoclet begin extends_implements
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  // JUnitDoclet end class
  
  public static TestSuite suite() {
    
    TestSuite suite;
    
    suite = new TestSuite("com.plexobject.testplayer.plugin");
    
    
    suite.addTest(com.plexobject.testplayer.plugin.dot.DotSuite.suite());
    suite.addTest(com.plexobject.testplayer.plugin.test.TestSuite.suite());
    suite.addTest(com.plexobject.testplayer.plugin.uml.UmlSuite.suite());
    
    
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
