package com.plexobject.testplayer.plugin.test.functional;

import junit.framework.TestCase;
// JUnitDoclet begin import
import com.plexobject.testplayer.plugin.test.functional.FunctionalTestPlugin;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class FunctionalTestPluginTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  com.plexobject.testplayer.plugin.test.functional.FunctionalTestPlugin functionaltestplugin = null;
  // JUnitDoclet end class
  
  public FunctionalTestPluginTest(String name) {
    // JUnitDoclet begin method FunctionalTestPluginTest
    super(name);
    // JUnitDoclet end method FunctionalTestPluginTest
  }
  
  public com.plexobject.testplayer.plugin.test.functional.FunctionalTestPlugin createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new com.plexobject.testplayer.plugin.test.functional.FunctionalTestPlugin();
    // JUnitDoclet end method testcase.createInstance
  }
  
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    functionaltestplugin = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    functionaltestplugin = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  
  
  /**
  * JUnitDoclet moves marker to this method, if there is not match
  * for them in the regenerated code and if the marker is not empty.
  * This way, no test gets lost when regenerating after renaming.
  * Method testVault is supposed to be empty.
  */
  public void testVault() throws Exception {
    // JUnitDoclet begin method testcase.testVault
    // JUnitDoclet end method testcase.testVault
  }
  
  public static void main(String[] args) {
    // JUnitDoclet begin method testcase.main
    junit.textui.TestRunner.run(FunctionalTestPluginTest.class);
    // JUnitDoclet end method testcase.main
  }
}
