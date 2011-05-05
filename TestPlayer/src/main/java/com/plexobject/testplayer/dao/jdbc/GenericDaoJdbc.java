/* ===========================================================================
 * $RCS$
 * Version: $Id: GenericDaoJdbc.java,v 1.2 2007/07/14 18:06:22 shahzad Exp $
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

package com.plexobject.testplayer.dao.jdbc;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.plexobject.testplayer.dao.*;
import com.plexobject.testplayer.*;

import java.sql.*;
import java.util.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public abstract class GenericDaoJdbc<T, ID extends Serializable> extends JdbcTemplate implements GenericDao<T, ID> {
  private Class<T> persistentClass;
  private RowMapper mapper;
  private String create;
  private String idQuery;
  private String allQuery;
  private String clearQuery;

  public GenericDaoJdbc(RowMapper mapper, String create, String idQuery, String allQuery, String clearQuery) {
    super(JdbcUtil.getPooledDataSource());
    this.mapper = mapper;
    this.create = create;
    this.idQuery = idQuery;
    this.allQuery = allQuery;
    this.clearQuery = clearQuery;
    this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  public Class<T> getPersistentClass() {
    return persistentClass;
  }


  public void createTable() {
    update(create);
  }

  @SuppressWarnings("unchecked")
  public T findById(ID id) {
    List<T> list = (List<T>) query(idQuery, new Object[] {id}, mapper);
    if (list == null || list.size() == 0) throw new DaoException("ID with " + id + " not found");
    if (list.size() > 1) throw new DaoException("ID with " + id + " returned multiple rows " + list);
    return list.get(0);
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll() {
    return query(allQuery, new Object[0], mapper);
  }

  public void clear() {
    update(clearQuery, new Object[0]);
  }


  protected static Object getBlobObject(ResultSet rs, int n) throws SQLException {
    Blob blob = rs.getBlob(n);
    return toObject(blob);
  }

  protected static Object toObject(Blob blob) {
    try {
      if (blob == null) return null;
      InputStream in = blob.getBinaryStream();
      if (in == null) return null;
      int c;
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
      while ((c = in.read()) != -1) {
        out.write(c);
      }
      byte[] b = out.toByteArray();
      if (b.length == 0) return null;
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
      Object object = ois.readObject();
      ois.close();
      return object;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new DaoException("Failed to deserialize", e);
    }
  }
}
