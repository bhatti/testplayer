package com.plexobject.testplayer.plugin.test.unit;

import junit.framework.TestCase;
// JUnitDoclet begin import
import com.plexobject.testplayer.plugin.test.unit.UnitTestPlugin;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class UnitTestPluginTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  com.plexobject.testplayer.plugin.test.unit.UnitTestPlugin unitplugin = null;
  // JUnitDoclet end class
  
  public UnitTestPluginTest(String name) {
    // JUnitDoclet begin method UnitTestPluginTest
    super(name);
    // JUnitDoclet end method UnitTestPluginTest
  }
  
  public com.plexobject.testplayer.plugin.test.unit.UnitTestPlugin createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new com.plexobject.testplayer.plugin.test.unit.UnitTestPlugin();
    // JUnitDoclet end method testcase.createInstance
  }
  
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    unitplugin = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    unitplugin = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  public void testVisit() throws Exception {
    // JUnitDoclet begin method visit
    // JUnitDoclet end method visit
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
    junit.textui.TestRunner.run(UnitTestPluginTest.class);
    // JUnitDoclet end method testcase.main
  }
}