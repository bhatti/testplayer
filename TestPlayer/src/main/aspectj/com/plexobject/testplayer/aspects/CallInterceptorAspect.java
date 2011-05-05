package com.plexobject.testplayer.aspects;
import com.plexobject.testplayer.util.*;
import com.plexobject.testplayer.events.*;
import com.plexobject.testplayer.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.aspectj.lang.*;



/**
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
 */
public abstract privileged aspect CallInterceptorAspect percflow(topCall()) {
  public CallInterceptorAspect() {
  }
  /**
   * scope should be overrided by the derived aspect to narrow the calls 
   * that must be traced.
   */
  public pointcut scope();


  pointcut adviceCflow() : cflow(adviceexecution());

  /**
   * pointcut that defines interception for all public calls
   */
  //public pointcut userCalls() : (call(public * *(..)) || call(public *.new(..))) && scope();//  && !within(CallInterceptorAspect+); && !within(junit.framework.TestCase+); 
  //public pointcut userCalls() : (execution(* com.plexobject.mini..*.*(..)));
  public pointcut userCalls() : (call(* graph..*.*(..)) || execution(* graph..*.*(..)));


  public pointcut topCall() : userCalls() && !cflowbelow(userCalls());



  /**
   * around advice that is invoked when public calls that user is 
   * interested in are invoked
   */
  Object around() : userCalls() {
    Object caller = thisJoinPoint.getThis();
    Object callee = thisJoinPoint.getTarget();
    String signature = thisJoinPointStaticPart.getSignature().toLongString();
    boolean constructor = signature.indexOf(".new(") != -1;

    MethodEntry call = new MethodEntry(
                context.nextMethodEntryId(),
                null,
                caller,
                callee,
                signature,
                thisJoinPoint.getArgs(),
                constructor,
		CallStackUtils.getDepth() 
        );
    String calleeName = call.getCalleeName();
    //MethodEntry last = caller != callee ? CallStackUtils.getLastFor(call) : null;
    MethodEntry last = CallStackUtils.getLastFor(call);
    call.setParent(last);

    boolean sameCallerCallee = caller == callee;

    String pkg = call.getCalleePackageName();
    if (context.isPermittedPackage(pkg)) {
      CallStackUtils.setLast(call);
    } else {
      call = null;
    }


    boolean notified = false;
    if (call != null && !dispatcher.isDestroyed()) {
      dispatcher.notifyCall(new MethodEvent(this, MethodEvent.BEFORE, call));
      notified = true;
    }

    if (logger.isEnabledFor(Level.DEBUG)) {
       logger.debug("*** Begin intercepting " + System.identityHashCode(call) + call + " NOTIFIED " + notified);
    }

    ////////////////////////////////////////////////////////////////////////
    //
    Exception exception = null;
    try {
      if (logger.isEnabledFor(Level.DEBUG)) {
         logger.debug("*** Begin intercepting " + System.identityHashCode(call) + call + ", signature " + signature + ", will notify? " + notified);
      }
      Object rvalue = proceed();
      if (call != null) call.setReturnValue(rvalue);
      return rvalue;
    } catch (Exception e) {
      exception = e;
      if (call != null) call.setException(e);
      logger.error("Aspect failed to permitted(" + pkg + "/" + calleeName + ") ?" + context.isPermittedPackage(pkg) + " signature (" + signature + ") call " + call, e);
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new TestSystemException(e);
      //return null;
    } finally {
      if (logger.isEnabledFor(Level.DEBUG)) {
        logger.debug("*** End intercepting " + System.identityHashCode(call) + call + " signature " + signature + ", exception " + exception + ", will notify ?" + notified);
      }
      CallStackUtils.setLast(call); 
      if (notified) {
        dispatcher.notifyCall(new MethodEvent(this, MethodEvent.AFTER, call));
      }
    }
  }



  before() : topCall() {
  }


  after() : topCall() {
    CallStackUtils.cleanStack();
  }


  /**
   * @After(type="java.lang.String", pointcut="topCall")
   * cleans stack trace of method calls within this thread and top call
   */
  //public void cleanStack(StaticJoinPoint joinPoint) throws Throwable {
  //  CallStackUtils.cleanStack();
  //}

  ///////////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////////
  //
  private static ApplicationContext context = new ApplicationContext(); 
  private static MethodPluginDispatcher dispatcher = new MethodPluginDispatcher(context);
  protected static Logger logger = Logger.getLogger(CallInterceptorAspect.class.getName());
} 

