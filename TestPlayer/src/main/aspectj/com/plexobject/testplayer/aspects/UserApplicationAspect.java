package com.plexobject.testplayer.aspects;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.io.*;
import java.util.*;


/**
 * UserApplicationAspect is a concrete aspect that extends CallNotifierAspect 
 * defines pointcuts to narrow interception of methods that the user
 * is interested in. Though a user can simply define !within(testplayer's code),
 * but it is recommended that they modify this class in addition to specifying
 * packages that the user is interested in.
 */
public aspect UserApplicationAspect extends CallInterceptorAspect { 
  /**
   * scope defines scope
   */
  public pointcut scope() : !within(com.plexobject.testplayer..*) && 
	!within(net.sf.cglib..*) && 
        //within(com.orbitz.odp..*);
        //within(com.plexobject.mini..*);
        within(graph..*);
}

