/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodStats.java,v 1.4 2007/07/17 01:31:35 shahzad Exp $
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
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import java.lang.reflect.*;
import org.apache.log4j.*;
import java.io.Serializable;
import org.joda.time.DateTime;


/**
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 7/12/07     SB              created.
 */
public class MethodStats implements Serializable {
  private static long serialVersionUID = 1L;
  //
  public static class InvokedComparator implements Comparator<MethodStats> {
    public int compare(MethodStats d1, MethodStats d2) {
      if (d1 == null) return +1;	// DESC
      if (d2 == null) return -1;
      if (d2.timesCalled - d1.timesCalled > 0) return +1;
      else if (d2.timesCalled - d1.timesCalled < 0) return -1;
      else return 0;
    }
  }
  public static class TotalResponseComparator implements Comparator<MethodStats> {
    public int compare(MethodStats d1, MethodStats d2) {
      if (d1 == null) return +1;	// DESC
      if (d2 == null) return -1;
      if (d2.getTotalResponseTime() - d1.getTotalResponseTime() > 0) return +1;
      else if (d2.getTotalResponseTime() - d1.getTotalResponseTime() < 0) return -1;
      else return 0;
    }
  }
  public static class AverageResponseComparator implements Comparator<MethodStats> {
    public int compare(MethodStats d1, MethodStats d2) {
      if (d1 == null) return +1;	// DESC
      if (d2 == null) return -1;
      if (d2.getAverageResponseTime() - d1.getAverageResponseTime() > 0) return +1;
      else if (d2.getAverageResponseTime() - d1.getAverageResponseTime() < 0) return -1;
      else return 0;
    }
  }

  public static class SizeComparator implements Comparator<MethodStats> {
    public int compare(MethodStats d1, MethodStats d2) {
      if (d1 == null) return +1;	// DESC
      if (d2 == null) return -1;
      if (d2.getSize() - d1.getSize() > 0) return +1;
      else if (d2.getSize() - d1.getSize() < 0) return -1;
      else return 0;
    }
  }

  private String signature;
  private long timesCalled;
  private long totalResponseTime;
  private long totalArgsSize;
  private long totalRvalueSize;
  private boolean constructor;
  private Date created = new Date();
  public MethodStats(String signature, long timesCalled, long totalResponseTime, long totalArgsSize, long totalRvalueSize, boolean constructor, Date created) {
    setSignature(signature);
    setTimesCalled(timesCalled);
    setTotalResponseTime(totalResponseTime);
    setTotalArgsSize(totalArgsSize);
    setTotalRvalueSize(totalRvalueSize);
    setConstructor(constructor);
    setCreated(created);
  }
  public MethodStats(String signature, boolean constructor) {
    setSignature(signature);
    setConstructor(constructor);
  }
  @Override
  public String toString() {
    return signature + getInstantiated() + ";" + timesCalled + ";" + getAverageResponseTime() + ";" + (getTotalResponseTime() * .000001) + ";" + getSize();
  }
  @Override
  public boolean equals(Object o) {
    if (o == null || o instanceof MethodStats == false) return false;
    if (o == this) return true;
    MethodStats other = (MethodStats) o;
    return signature.equals(other.signature);
  }
  @Override
  public int hashCode() {
    return signature.hashCode();
  }
  public synchronized void incrInvoked(long n) {
    this.timesCalled += n;
    this.created = new Date();
  }
  public synchronized void incrTotalResponseTime(long n) {
    this.totalResponseTime += n;
    this.created = new Date();
  }
  public synchronized void incrTotalArgsSize(long n) {
    this.totalArgsSize += n;
    this.created = new Date();
  }
  public synchronized void incrTotalRvalueSize(long n) {
    this.totalRvalueSize += n;
    this.created = new Date();
  }
  public long getSize() {
    return this.totalRvalueSize + this.totalArgsSize / timesCalled;
  }
  public double getAverageResponseTime() {
    return (this.totalResponseTime / timesCalled * .000001);	// in Millis
  }
  public String getInstantiated() {
    return constructor ? " Object instantiated" : " ";
  }
  public static String getHeader() {
    return "Signature;Invoked Called;Avg Response (in millis); Total Response (in millis); Avg Size of Args";
  }

  /**
   * @return signature
   */
  public String getSignature() {
    return this.signature;
  }

  /**
   * @param signature
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }
  /**
   * @return created
   */
  public Date getCreated() {
    return this.created;
  }

  /**
   * @param created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /**
   * @return timescalled
   */
  public long getTimesCalled() {
    return this.timesCalled;
  }

  /**
   * @param timescalled
   */
  public void setTimesCalled(long timesCalled) {
    this.timesCalled = timesCalled;
  }

  /**
   * @return totalresponsetime
   */
  public long getTotalResponseTime() {
    return this.totalResponseTime;
  }

  /**
   * @param totalresponsetime
   */
  public void setTotalResponseTime(long totalResponseTime) {
    this.totalResponseTime = totalResponseTime;
  }

  /**
   * @return totalargssize
   */
  public long getTotalArgsSize() {
    return this.totalArgsSize;
  }

  /**
   * @param totalargssize
   */
  public void setTotalArgsSize(long totalArgsSize) {
    this.totalArgsSize = totalArgsSize;
  }

  /**
   * @return totalrvaluesize
   */
  public long getTotalRvalueSize() {
    return this.totalRvalueSize;
  }

  /**
   * @param totalrvaluesize
   */
  public void setTotalRvalueSize(long totalRvalueSize) {
    this.totalRvalueSize = totalRvalueSize;
  }

  /**
   * @return constructor
   */
  public boolean isConstructor() {
    return this.constructor;
  }

  /**
   * @param constructor
   */
  public void setConstructor(boolean constructor) {
    this.constructor = constructor;
  }

  public static SortedSet<MethodStats> newSetByInvokedTimes() {
    return new TreeSet<MethodStats>(new InvokedComparator());
  }
  public static SortedSet<MethodStats> newSetByAverageResponseTimes() {
    return new TreeSet<MethodStats>(new AverageResponseComparator());
  }
  public static SortedSet<MethodStats> newSetByTotalResponseTimes() {
    return new TreeSet<MethodStats>(new TotalResponseComparator());
  }
  public static SortedSet<MethodStats> newSetBySize() {
    return new TreeSet<MethodStats>(new SizeComparator());
  }
}

