/* ===========================================================================
 * $RCS$
 * Version: $Id: JdbcUtil.java,v 1.1 2007/07/14 15:23:43 shahzad Exp $
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

import java.io.*;
import java.sql.*;
import javax.sql.DataSource;
import com.mchange.v2.c3p0.DataSources;

import java.util.*;
import org.apache.commons.logging.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public class JdbcUtil {
  private static Log log = LogFactory.getLog(JdbcUtil.class);
  private static String DRIVER = "SQLite.JDBCDriver";
  private static DataSource unpooled;
  private static DataSource pooled;


  static RuntimeException exception;
  static {
    try {
      DriverManager.registerDriver((Driver)Class.forName(DRIVER).newInstance());
      File home = new File(System.getProperty("user.home"));
      unpooled = DataSources.unpooledDataSource("jdbc:sqlite://" + home.getAbsolutePath() + "/testplayer", "", "");
      pooled = DataSources.pooledDataSource( unpooled );
    } catch (Throwable e) {
      exception = new RuntimeException("Failed to register " + DRIVER, e);
      log.error("Failed to register " + DRIVER, e);
    }
  }

  public static Connection newConnection() throws SQLException {
    if (exception != null) throw exception;
    return pooled.getConnection();
  }

  public static DataSource getPooledDataSource() {
    if (exception != null) throw exception;
    return pooled;
  }

  static void attemptClose(ResultSet o) {
    try { 
      if (o != null) o.close();
    } catch (Exception e) { 
      log.error("Failed to close " + o, e);
    }
  }
  static void attemptClose(PreparedStatement o) {
    try { 
      if (o != null) o.close();
    } catch (Exception e) { 
      log.error("Failed to close " + o, e);
    }
  }
  static void attemptClose(Statement o) {
    try { 
      if (o != null) o.close();
    } catch (Exception e) { 
      log.error("Failed to close " + o, e);
    }
  }
  static void attemptClose(Connection o) {
    try { 
      if (o != null) o.close();
    } catch (Exception e) { 
      log.error("Failed to close " + o, e);
    }
  }
}
