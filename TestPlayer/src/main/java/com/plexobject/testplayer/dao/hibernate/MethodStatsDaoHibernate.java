/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodStatsDaoHibernate.java,v 1.2 2007/07/16 17:37:15 shahzad Exp $
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
import com.plexobject.testplayer.dao.*;
import com.plexobject.testplayer.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class MethodStatsDaoHibernate extends GenericDaoHibernate<MethodStats, String> implements  MethodStatsDao {
  static final String CREATE = " create table METHODSTATS ( signature varchar(255) not null, timesCalled bigint, totalResponseTime bigint, totalArgsSize bigint, totalRvalueSize bigint, constructor bit, created datetime, primary key (signature)) type=InnoDB";
  static final String INSERT = " insert into METHODSTATS (timesCalled, totalResponseTime, totalArgsSize, totalRvalueSize, constructor, created, signature) values (?, ?, ?, ?, ?, ?, ?)";
  static final String FINDER_QUERY = "select methodstat_.signature, methodstat_.timesCalled, methodstat_.totalResponseTime, methodstat_.totalArgsSize, methodstat_.totalRvalueSize, methodstat_.constructor, methodstat_.created from METHODSTATS methodstat_ where methodstat_.signature=?";

  static final String ALL_QUERY = "select methodstat_.signature, methodstat_.timesCalled, methodstat_.totalResponseTime, methodstat_.totalArgsSize, methodstat_.totalRvalueSize, methodstat_.constructor, methodstat_.created from METHODSTATS methodstat_";
  static final String CLEAR_QUERY = "DELETE * FROM METHODSTATS";

  static class MethodMapper implements RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
      ResultSetMetaData metadata = rs.getMetaData();
      int columnCount = metadata.getColumnCount();
      return new MethodStats(
		rs.getString("signature"),
		rs.getLong("timesCalled"), 
		rs.getLong("totalResponseTime"), 
		rs.getLong("totalArgsSize"), 
		rs.getLong("totalRvalueSize"), 
		rs.getBoolean("constructor"), 
		rs.getDate("created")
		);
    }
  }
 
  public MethodStatsDaoHibernate() {
    super(new MethodMapper(), ALL_QUERY, FINDER_QUERY);
  }

  public static void createTable() {
     try {
       JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
       jdbc.update(CREATE, new Object[0]);
     } catch (Exception e) {}
  }



  public static void main(String[] args) throws Exception {
     MethodStatsDaoHibernate dao = new MethodStatsDaoHibernate();
     createTable();
     for (int i=0; i<10; i++) {
         MethodStats method = new MethodStats(
                        "void main" + i + "(String[] args)",
                        2000+i,
                        2000+i,
                        2000+i,
                        2000+i,
                        false,
			new Date()
			);
          dao.save(method);
     }
/*
*/
     List<MethodStats> list = dao.findAll();
     System.out.println("Printing " + list.size() + ":\n" + list);
     if (list.size() > 0) {
        System.out.println("First " + dao.findById(list.get(0).getSignature()));
     }
  }
}
