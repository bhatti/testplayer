/* ===========================================================================
 * $RCS$
 * Version: $Id: BinaryBlobType.java,v 1.2 2007/07/13 23:58:00 shahzad Exp $
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

package com.plexobject.testplayer.dao.hibernate;
import java.io.Serializable;
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Types; 
import java.sql.Blob; 

import org.hibernate.Hibernate; 
import org.hibernate.HibernateException; 
import org.hibernate.usertype.UserType; 



public class BinaryBlobType implements UserType { 
  public int[] sqlTypes() { 
    return new int[] { Types.BLOB }; 
  }

  public Class returnedClass() { 
    return byte[].class; 
  } 

  public boolean equals(Object x, Object y) { 
    return (x == y) 
      || (x != null 
        && y != null 
        && java.util.Arrays.equals((byte[]) x, (byte[]) y)); 
  } 

  public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException { 
     Blob blob = rs.getBlob(names[0]); 
     return blob.getBytes(1, (int) blob.length()); 
  } 

  public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException { 
     if (value != null) st.setBlob(index, Hibernate.createBlob((byte[]) value)); 
  } 

  public Object deepCopy(Object value) { 
    if (value == null) return null; 

    byte[] bytes = (byte[]) value; 
    byte[] result = new byte[bytes.length]; 
    System.arraycopy(bytes, 0, result, 0, bytes.length); 

    return result; 
  } 

  public boolean isMutable() { 
    return true; 
  } 

  public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
     return deepCopy(arg0);
  }

  public Serializable disassemble(Object value) {
     return (Serializable) deepCopy(value);
  }
        
  public int hashCode(Object arg0) throws HibernateException {
    return arg0.hashCode();
  }
    
  public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
     return deepCopy(arg0);
  }
}
