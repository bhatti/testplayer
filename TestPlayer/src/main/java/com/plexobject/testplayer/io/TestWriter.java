/* ===========================================================================
 * $RCS$
 * Version: $Id: TestWriter.java,v 1.1 2006/08/15 16:18:38 shahzad Exp $
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

package com.plexobject.testplayer.io;
import com.plexobject.testplayer.*;
import java.io.*;
import java.util.*;

/**
 * This class encapsulates PrintWriter and adds indentation and eol.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 8/5/06       SB              created.
 */
public class TestWriter {
  public TestWriter(ApplicationContext context, PrintWriter out) {
    this.context = context;
    this.out = out;
    this.numIndents = 1;
  }
  public TestWriter println(String line) {
    out.println(context.mtab(numIndents) + line);
    return this;
  }

  public TestWriter println(String line, int numIndents) {
    this.numIndents = numIndents;
    out.println(context.mtab(numIndents) + line);
    return this;
  }


  public TestWriter indents(int numIndents) {
    this.numIndents = numIndents;
    return this;
  }

  private ApplicationContext context;
  private PrintWriter out;
  private int numIndents;
}
