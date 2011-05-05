/* ===========================================================================
 * $RCS$
 * Version: $Id: PointableWriter.java,v 2.3 2006/02/25 20:50:43 shahzad Exp $
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

package com.plexobject.testplayer.fitnesse;
import com.thoughtworks.xstream.io.StreamException;
import java.io.IOException;
import java.io.Writer;

/**
 * This class is used to write buffer and keeps track of position.
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 1/19/05      SB              created.
 */
public class PointableWriter {
  /**
   * PointableWriter decorator constructor
   * @param writer - writer object
   */
  public PointableWriter(Writer writer) {
    this.writer = writer;
    buffer = new char[1024];
  }

  /**
   * PointableWriter decorator constructor
   * @param writer - writer object
   * @param bufferSize - buffer size
   */
  public PointableWriter(Writer writer, int bufferSize) {
    this.writer = writer;
    buffer = new char[bufferSize];
  }

  /**
   * @return pointer of buffer
   */
  public int getPointer() {
    return this.pointer;
  }


  /**
   * @param pointer of buffer
   */
  public void setPointer(int pointer) {
    this.pointer = pointer;
  }
  /**
   * @param offset - pointer offset
   * @return - return substring starting with offset
   */
  public String substring(int offset) {
    return substring(offset, pointer-offset);
  }

  /**
   * @param offset - pointer offset
   * @param count - length of substring
   * @return - return substring starting with offset and length
   */
  public String substring(int offset, int count) {
    if (offset < 0 || offset >= this.pointer) throw new IllegalArgumentException("Illegal offset " + offset + " for buffer with pointer " + this.pointer);
    if (count < 0 || count > pointer-offset) throw new IllegalArgumentException("Illegal count " + count + " for buffer with offset " + offset + " and pointer " + this.pointer);
    return new String(buffer, offset, count);
  }

  /**
   * @param str - string to write
   */
  public void write(String str) {
    int len = str.length();
    if (pointer + len >= buffer.length) {
      flush();
      if (len > buffer.length) {
        raw(str.toCharArray());
        return;
      }
    }
    str.getChars(0, len, buffer, pointer);
    pointer += len;
  }

  /**
   * @param c - character to write
   */
  public void write(char c) {
    if (pointer + 1 >= buffer.length) {
      flush();
    }
    buffer[pointer++] = c;
  }

  /**
   * @param c - character array to write
   */
  public void write(char[] c) {
    int len = c.length;
    if (pointer + len >= buffer.length) {
      flush();
      if (len > buffer.length) {
        raw(c);
        return;
      }
    }
    System.arraycopy(c, 0, buffer, pointer, len);
    pointer += len;
  }

  /**
   * flushes buffer
   */
  public void flush() {
    try {
      writer.write(buffer, 0, pointer);
      pointer = 0;
      writer.flush();
    } catch (IOException e) {
      throw new StreamException(e);
    }
  }

  /**
   * closes buffer
   */
  public void close() {
    try {
      writer.write(buffer, 0, pointer);
      pointer = 0;
      writer.close();
    } catch (IOException e) {
      throw new StreamException(e);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  //
  private void raw(char[] c) {
    try {
      writer.write(c);
      writer.flush();
    } catch (IOException e) {
      throw new StreamException(e);
    }
  }
  private final Writer writer;
  private char[] buffer;
  private int pointer;

}
