/* ===========================================================================
 * $RCS$
 * Version: $Id: CallInterceptorAspect.java,v 2.39 2007/07/17 01:33:05 shahzad Exp $
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

package com.plexobject.testplayer.aspects;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;

/*
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.definition.Pointcut;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;
import org.codehaus.aspectwerkz.joinpoint.CodeRtti;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.ConstructorRtti;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.joinpoint.MemberSignature;
import org.codehaus.aspectwerkz.joinpoint.CodeSignature;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.annotation.Annotation;
*/


import org.aspectj.lang.*; 
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.*; 
import org.aspectj.lang.annotation.Pointcut;

//perInstance name=NAME
//percflow(topCall()) 

/**
 * @Aspect perJVM
 * 
 * CallInterceptorAspect aspect is an abstract base aspect that gathers 
 * call information from specified packages and notifies
 * MethodPluginDispatcher for all calls. The subclass must override scope 
 * pointcut. This aspect probably is best put in user's application using
 * Participitant pattern.
 * This aspect is created per top level flow or per thread. This means
 * that method dispatcher for notification is created per aspect and it
 * will only notify listeners about method invocation for this thread.
 * Note that all exceptions thrown by any method that is intercepted 
 * are automatically converted into RuntimeException.
 * This is implemented using aspectwerkz
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 9/13/05      SB              created.
 */
//@Aspect
//public abstract class CallInterceptorAspect {
public abstract privileged aspect CallInterceptorAspect percflow(topCall()) {
  /**
   * CallInterceptorAspect  constructor
   * @param info - aspect context
   */
  //public CallInterceptorAspect(AspectContext info) {
  //  this.info = info;
  //}



  /**
   * xExpression !within(com.plexobject.testplayer..*) 
   * xExpression within(com.plexobject.mini..*)
   * @Expression within(graph..*)
   * scope should be overrided by the derived aspect to narrow the calls 
   * that must be traced.
   * abstract pointcut: no expression is defined
   */
  //Pointcut scope;
  //@Pointcut abstract void scope();
  public pointcut scope() : !within(com.plexobject.testplayer..*) && 
	!within(net.sf.cglib..*) && within(graph..*) && !within(graph.InputParser.*) && !within(graph.Main.*);

  /**
   * @Expression cflow(adviceexecution())
   */
  //Pointcut adviceCflow;
  //<pointcut name="adviceCflow" expression="cflow(adviceexecution())"/> 
  //@Expression (cflow(adviceexecution()))
  //@Pointcut abstract void adviceCflow();
  pointcut adviceCflow() : cflow(adviceexecution());

  /**
   * pointcut that defines interception for all public calls
   * @Expression (call(public * *(..)) OR call(public *.new(..))) AND scope
   * - to disable within test add !within(junit.framework.TestCase+)
   * -  !within(CallInterceptorAspect+)
   */
  //Pointcut userCalls; 
  //@Expression (call(* graph..*.*(..)) || execution(* graph..*.*(..)))
  //@Pointcut abstract void userCalls();
  public pointcut userCalls() : (call(graph..*.new(..)) || call(* graph..*.*(..)) || execution(* graph..*.*(..)));



  /**
   * @Around userCalls(), pointcut="userCalls")
   * around advice that is invoked when public calls that user is 
   * interested in are invoked
   */
  //@Around("userCalls() AND scope()")
  //<pointcut name="userCalls" expression="(call(* graph..*.*(..)) || execution(* graph..*.*(..)))"/>
  //@Around("userCalls()")
  //public Object traceMethod(final JoinPoint joinPoint) throws Throwable {


/*
  before() : execution(* org.hibernate.jdbc.*.getPreparedStatement(..)) {
    logger.info("UUUUUUUUUUUUUUUUUargs for getprepared");
    try {
      Object[] args = thisJoinPoint.getArgs();
      for (int i=0; args != null && i<args.length; i++) {
        logger.info("UUUUUUUUUUUUUUUUUArg " + i + "=" + args[i]);
      }
    } catch (Throwable e) {
      logger.error("Internal error while handling before " + thisJoinPoint, e);
    }
  }
*/



 
  before() : userCalls() && scope() {
    try {
      handleBefore(thisJoinPoint);
    } catch (Throwable e) {
      logger.error("Internal error while handling before " + thisJoinPoint, e);
    }
  }

  after() returning (Object rvalue) : userCalls() && scope() { 
    try {
      handleReturn(rvalue); 
    } catch (Throwable e) {
      logger.error("Internal error while handling after returning " + thisJoinPoint, e);
    } finally {
      handleFinally(null); 
    }
  }


  after() throwing (Exception exception) : userCalls() && scope() { 
    try {
      handleException(exception); 
    } catch (Throwable e) {
      logger.error("Internal error while handling after returning exception " + thisJoinPoint, e);
    } finally {
      handleFinally(exception); 
    }
  }


/*
  Object around() : userCalls() && scope() {
    handleBefore(thisJoinPoint);

    Exception exception = null;
    try {
      //
      //Object rvalue = ((ProceedingJoinPoint)joinPoint).proceed();
      Object rvalue = proceed();
      //CatchClauseRtti crtti = (CatchClauseRtti) joinPoint.getRtti();
      //Exception e = (Exception) crtti.getParameterValue();
      //if (e != null) throw e;
      handleReturn(rvalue); 
      return rvalue;
    } catch (Exception e) {
      handleException(e); 
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new TestSystemException(e);
    } finally {
      handleFinally(exception); 
    }
  }
*/

  private void handleBefore(JoinPoint jp) {
    Object caller = jp.getThis();
    Object callee = jp.getTarget();


    //if (joinPoint.getSignature() instanceof CodeSignature == false) {
    //  //throw new TestSystemException("Unknown type " + joinPoint.getSignature().getClass().getName() + " for join-point " + joinPoint);
    //  return ((ProceedingJoinPoint)joinPoint).proceed();
    //}


    //CodeSignature signature = (CodeSignature) joinPoint.getStaticPart().getSignature();
    //if (signature == null) throw new IllegalArgumentException("Null method signature " + joinPoint);
    ////MemberSignature, MethodSignature see signature.getName(), signature.getParameterTypes()
    //String signature = thisJoinPointStaticPart.getSignature().toLongString();

    String signature = jp.getStaticPart().getSignature().toLongString();
    boolean constructor = signature.indexOf(".new(") != -1 || jp.getStaticPart().getSignature() instanceof ConstructorSignature;

    /////////////////////////////////////////////////////////////////////////////////////
    //


    //CodeRtti rtti = (CodeRtti) joinPoint.getRtti();
    //boolean constructor = rtti instanceof ConstructorRtti;
    //if (rtti == null) throw new IllegalArgumentException("Null RTTI " + joinPoint);
    MethodEntry call = new MethodEntry(
                context.nextMethodEntryId(),
                null,
                caller,
                callee,
                signature.toString(),
                jp.getArgs(), 		//thisJoinPoint.getArgs(),		//rtti.getParameterValues(),
                constructor,
		CallStackUtils.getDepth() 
        );

    if (constructor) {
      if (logger.isEnabledFor(Level.DEBUG)) {
        logger.debug("*** Constructor " + call);
      }
    }

    String calleeName = call.getCalleeName();
    //MethodEntry last = caller != callee ? CallStackUtils.getLastFor(call) : null;
    MethodEntry last = CallStackUtils.getLastFor(call);
    if (last != null && last.sameSignature(call)) {
      call.incrTimesCalled();
    } else {
      call.setParent(last);
    }

    String pkg = call.getCalleePackageName();
    if (context.isPermittedPackage(pkg)) {
      //???????CallStackUtils.setLast(call); 
    } else {
      //logger.debug("*** Skipping notifying " + call + " because package is not permitted");
      call = null;
    }
    boolean sameCallerCallee = caller == callee;

    if (call != null) {
      Runtime runtime = Runtime.getRuntime();
      call.setTotalMemory((int) (runtime.totalMemory() / 1000 / 1000));
      call.setFreeMemory((int) (runtime.freeMemory() / 1000 / 1000));
    }


    currentMethod.set(call);
    notified.set(new Boolean(call != null && !dispatcher.isDestroyed()));
    //notified.set(Boolean.FALSE);

    if (logger.isEnabledFor(Level.DEBUG)) {
      //logger.debug("*** Begin intercepting " + call + ", will notify? " + isNotified());
    }


    if (isNotified()) {
      dispatcher.notifyCall(new MethodEvent(this, MethodEvent.BEFORE, call));
    }
  }


  private void handleReturn(Object rvalue) {
    MethodEntry call = currentMethod.get();
    if (call != null) call.setReturnValue(rvalue);
  }

  private void handleException(Exception exception) {
    MethodEntry call = currentMethod.get();
    if (call != null) {
       call.setException(exception);
       String pkg = call.getCalleePackageName();
       logger.error("Method failed in CallInterceptorAspect permitted ?" + context.isPermittedPackage(pkg) + " call " + call, exception);
    }
  }



  private void handleFinally(Exception exception) {
    MethodEntry call = currentMethod.get();
   
    try {
      if (call != null) {
        if (call.getTimesCalled() == 1) {
          call.setTotalResponseTime(call.getResponseTime());
        } else if (call.getTimesCalled() > 1) {
          call.setTotalResponseTime(call.getTotalResponseTime() + call.getResponseTime());
        }
      }

      //
      if (logger.isEnabledFor(Level.DEBUG)) {
        //logger.debug("*** End intercepting " + call + ", exception " + exception + ", will notify ?" + isNotified());
      }
      //
      CallStackUtils.setLast(call); 
      //
      if (isNotified()) {
        dispatcher.notifyCall(new MethodEvent(this, MethodEvent.AFTER, call));
      }
    } catch (Throwable e) {
      logger.error("Internal error while handling finally " + call, e);
    }
  }


  /**
   * @Expression userCalls AND !cflowbelow(userCalls)
   */
  //Pointcut topCall; 
  //@Pointcut abstract void topCall();
  public pointcut topCall() : (userCalls() && !cflowbelow(userCalls()));

  /**
   * @After(type="java.lang.String", pointcut="topCall")
   * cleans stack trace of method calls within this thread and top call
   */
  //<pointcut name="topCall" expression="userCalls AND !cflowbelow(userCalls)"/>
  //public void cleanStack(final StaticJoinPoint joinPoint) throws Throwable {
  //@After("topCall()")
  //@Expression (userCalls AND !cflowbelow(userCalls))
  //public void cleanStack(final JoinPoint joinPoint) throws Throwable {
  //  CallStackUtils.cleanStack();
  //}

  after() : topCall() {
    CallStackUtils.cleanStack();
  }

  private String getIndent() {
    int depth = CallStackUtils.getDepth();
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<depth; i++) sb.append(' ');
    return sb.toString(); 
  }
  private static boolean isNotified() {
     Boolean boo = notified.get();
     return boo != null && boo.booleanValue();
  }
  //////////////////////////////////////////////////////////////////////////


  ///////////////////////////////////////////////////////////////////////////
  //
  //private AspectContext info;
  private static ApplicationContext context = new ApplicationContext(); 
  private static MethodPluginDispatcher dispatcher = new MethodPluginDispatcher(context);
  private static ThreadLocal<MethodEntry> currentMethod = new ThreadLocal<MethodEntry>(); 
  private static ThreadLocal<Boolean> notified = new ThreadLocal<Boolean>(); 
  private static Logger logger = Logger.getLogger(CallInterceptorAspect.class.getName());
}
