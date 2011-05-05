/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodDaoHibernate.java,v 1.9 2007/07/15 20:24:25 shahzad Exp $
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

public class MethodDaoHibernate extends GenericDaoHibernate<MethodEntry, Long> implements  MethodDao {
  //static final String CREATE = "create table METHODS ( id bigint not null, parentID bigint, signature varchar(255), args LONGVARCHAR, rvalue LONGVARCHAR, exception LONGVARCHAR, callee LONGVARCHAR, caller LONGVARCHAR, properties LONGVARCHAR, depth integer, sameMethodNumber integer, timesCalled integer, constructor bit, started timestamp, finished timestamp, elapsed bigint, primary key (id))";
  static final String CREATE = " create table METHODS ( id bigint not null, parentID bigint, signature varchar(255), ARGS VARCHAR(1024), RVALUE VARCHAR(1024), EXCEPTION VARCHAR(1024), CALLEE VARCHAR(1024), CALLER VARCHAR(1024), PROPERTIES VARCHAR(1024), depth integer, sameMethodNumber integer, timesCalled integer, constructor bit, started datetime, finished datetime, elapsed bigint, totalElapsed bigint, totalMemory integer, freeMemory integer, argsSize integer, rvalueSize integer, calleeSize integer, callerSize integer, propertiesSize integer, primary key (id)) type=InnoDB";

  static final String ALL_QUERY = "select methodentr_.id, methodentr_.parentID as parentID0_, methodentr_.signature as signature0_, methodentr_.args as args0_, methodentr_.rvalue as rvalue0_, methodentr_.exception as exception0_, methodentr_.callee as callee0_, methodentr_.caller as caller0_, methodentr_.properties as properties0_, methodentr_.depth as depth0_, methodentr_.sameMethodNumber as sameMet11_0_, methodentr_.timesCalled as timesCa12_0_, methodentr_.constructor as constru13_0_, methodentr_.started as started0_, methodentr_.finished as finished0_, methodentr_.elapsed as elapsed0_ from METHODS methodentr_";
  static final String FINDER_QUERY = "select methodentr_.id, methodentr_.parentID as parentID0_, methodentr_.signature as signature0_, methodentr_.args as args0_, methodentr_.rvalue as rvalue0_, methodentr_.exception as exception0_, methodentr_.callee as callee0_, methodentr_.caller as caller0_, methodentr_.properties as properties0_, methodentr_.depth as depth0_, methodentr_.sameMethodNumber as sameMet11_0_, methodentr_.timesCalled as timesCa12_0_, methodentr_.constructor as constru13_0_, methodentr_.started as started0_, methodentr_.finished as finished0_, methodentr_.elapsed as elapsed0_ from METHODS methodentr_ WHERE methodentr_.id = ?";

/*
  static class HsqlServer extends Thread {
     public HsqlServer() {
	setDaemon(true);
	start();
     }
     public void run() {
	try {
	    org.hsqldb.Server.main(new String[] {"-database.0", "mytestplayer", "-dbname.0", "testplayer"});
	} catch (Exception e) {
	    e.printStackTrace();
	}
     }
  }
  static {
     new HsqlServer();
  }
*/


 
  static class MethodMapper implements RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
	int n = 1;
        long id = rs.getLong(n++);
        long parent = rs.getLong(n++);
        String signature = rs.getString(n++);
	String args = rs.getString(n++);
	String rvalue = rs.getString(n++);
	String exception = rs.getString(n++);
	String callee = rs.getString(n++);
	String caller = rs.getString(n++);
	String properties = rs.getString(n++);
	int depth = rs.getInt(n++);
	int sameMethodNumber = rs.getInt(n++);
	int timesCalled = rs.getInt(n++);
	boolean constructor = rs.getBoolean(n++);
	Date started = rs.getDate(n++);
	Date finished = rs.getDate(n++);
	long elapsed = rs.getLong(n++);
	MethodEntry method = new MethodEntry(
        		id, 
        		parent,
        		MethodEntry.unmarshal(caller),
        		MethodEntry.unmarshal(callee),
        		signature, 
        		(Object[]) MethodEntry.unmarshal(args),
        		constructor,
			depth);
	method.setReturnValue(MethodEntry.unmarshal(rvalue));
	method.setException((Exception)MethodEntry.unmarshal(exception));
	method.setProperties((Properties) MethodEntry.unmarshal(properties));
	method.setSameMethodNumber(sameMethodNumber);
	method.setTimesCalled(timesCalled);

 	// do this after return value
	method.setStarted(started);
	method.setFinished(finished);
        return method;
    }
  }


  public MethodDaoHibernate() {
    super(new MethodMapper(), ALL_QUERY, FINDER_QUERY);
  }

  public static void createTable() {
     try {
       JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
       jdbc.update(CREATE, new Object[0]);
     } catch (Exception e) {}
  }



  public static void main(String[] args) throws Exception {
     MethodDaoHibernate dao = new MethodDaoHibernate();
     createTable();
/*
     for (int i=0; i<10; i++) {
         MethodEntry method = new MethodEntry(
                        2000+i,
                        2000+i-1,
                        "caller",
                        "callee",
                        "void main(String[] args)",
                        new Object[] {"args"},
                        true,
                        i);
          dao.save(method);
     }
*/
     List<MethodEntry> list = dao.findAll();
     System.out.println("Printing " + list.size() + ":\n" + list);
     if (list.size() > 0) {
        System.out.println("First " + dao.findById(list.get(0).getId()));
     }
  }
}
