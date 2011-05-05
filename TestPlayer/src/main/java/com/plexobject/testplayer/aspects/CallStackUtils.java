package com.plexobject.testplayer.aspects;
import com.plexobject.testplayer.*;
import org.apache.log4j.*;
import java.util.*;


/**
 * CallStackUtils providers helper methods for stack trace of methods.
 *
 */
public class CallStackUtils {
  private static Logger logger = Logger.getLogger(CallStackUtils.class.getName());
  private CallStackUtils() {
  }
  //////////////////////////////////////////////////////////////////////////
  public static MethodEntry _getLastFor(MethodEntry call) {
    String caller = call.getCallerName();
    if (caller == null) return null;
    List list = (List) callStacks.get();
    int initialSize = list.size();
    try {
      for (int i=list.size()-1; i>= 0; i--) {
        MethodEntry last = (MethodEntry) list.get(i);
        if (caller.equals(last.getCalleeName())) {
          return last;
        }
      }
    } catch (RuntimeException e) {
      logger.error("Failed to find calling method for [" + caller + "]", e);
    }
    return null;
  }

  public static MethodEntry xgetLastFor(MethodEntry call) {
    // String caller = call.getCallerName();
    //if (caller == null) return null;
    int stacks = Thread.currentThread().countStackFrames(); // getStackTrace().length
    Map map = (Map) callStacks.get();
    Stack stack = (Stack) map.get(new Integer(stacks-1));
    try {
      if (stack != null && stack.size() > 0) {
        if (logger.isEnabledFor(Level.DEBUG)) logger.debug("getLastFor(" + call + ") at level " + stacks + " found parent " + stack.peek());
        return (MethodEntry) stack.peek();
      }
    } catch (EmptyStackException e) {
    }
    return null;
  }


  public static int getDepth() {
    Stack stack = (Stack) callStacks.get();
    return stack.size();
  }

  public static MethodEntry getLastFor(MethodEntry call) {
    Stack stack = (Stack) callStacks.get();
    MethodEntry old = null;
    try {
      if (stack != null && stack.size() > 0) {
        old = (MethodEntry) stack.peek();
      }
    } catch (EmptyStackException e) {
    } finally {
      stack.push(call);
      if (stack.size() > 100 && logger.isEnabledFor(Level.DEBUG)) {
        int[] n = (int[]) stackPrinted.get();
        if (n[0] % 100 == 0) {
	  logger.debug("Stack Size is too big [" + stack.size() + "] getLastFor(" + call + "), last " + old);
        }
        n[0]++;
      }
    }

    //
    //if (logger.isEnabledFor(Level.DEBUG)) logger.debug("[" + stack.size() + "] getLastFor(" + call + "), last " + old);
    return old;
  }



  public static void _setLast(MethodEntry call) {
    List list = (List) callStacks.get();
    list.add(call);
  }


  public static void xsetLast(MethodEntry call) {
    Map map = (Map) callStacks.get();
    int stacks = Thread.currentThread().countStackFrames(); // getStackTrace().length
    Stack stack = (Stack) map.get(new Integer(stacks));
    if (stack == null) {
      stack = new Stack();
      map.put(new Integer(stacks), stack);
    }
    //if (logger.isEnabledFor(Level.DEBUG)) logger.debug("setLastFor(" + call + ") at level " + stacks);
    stack.push(call);
  }
  
  
  public static boolean setLast(MethodEntry call) {
    Stack stack = (Stack) callStacks.get();
    MethodEntry old = null;

    try {
      if (stack != null && stack.size() > 0) {
        old = (MethodEntry) stack.pop();
      }
    } catch (EmptyStackException e) {
    }
    boolean matched = old == call;
    //if (logger.isEnabledFor(Level.DEBUG)) logger.debug("[" + stack.size() + "] setLast(" + call + "), old " + old + ", matched ? " + matched);
    return matched;
  }
  
  

  /**
   * cleans stack trace of method calls within this thread and top call
   */
  public static void _cleanStack() {
    List list = (List) callStacks.get();
    list.clear();
  }


  /**
   * cleans stack trace of method calls within this thread and top call
   */
  public static void cleanStack() {
  }

  ///////////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////////
  //
  private static ThreadLocal _callStacks = new ThreadLocal() {
    protected Object initialValue() {
       return new ArrayList();
    }
  };
  //
  private static ThreadLocal xcallStacks = new ThreadLocal() {
    protected Object initialValue() {
       return new HashMap();
    }
  };
  private static ThreadLocal callStacks = new ThreadLocal() {
    protected Object initialValue() {
       return new Stack();
    }
  };
  private static ThreadLocal stackPrinted = new ThreadLocal() {
    protected Object initialValue() {
       return new int[] {0};
    }
  };
} 

