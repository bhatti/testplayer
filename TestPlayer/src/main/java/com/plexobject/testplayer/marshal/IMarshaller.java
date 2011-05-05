/* ===========================================================================
 * $RCS$
 * Version: $Id: IMarshaller.java,v 2.2 2006/02/25 20:50:45 shahzad Exp $
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

package com.plexobject.testplayer.marshal;
import com.plexobject.testplayer.tree.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class marshals and unmarshals java objects into text
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 1/11/06      SB              created.
 */
public interface IMarshaller {
  /**
   * marshalls object into text
   *
   * @param object - the object to be marshalled.
   * @param parent - parent node
   */
  public String marshal(Object object);

  /**
   * unmarshalls - constructs object from text 
   *
   * @param node - node
   */
  public Object unmarshal(String text);
}
