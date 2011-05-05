/* ===========================================================================
 * $RCS$
 * Version: $Id: MethodDaoJdbc.java,v 1.2 2007/07/14 18:06:22 shahzad Exp $
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
import com.plexobject.testplayer.dao.*;
import com.plexobject.testplayer.*;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import org.springframework.jdbc.core.RowMapper;
import java.sql.*;

public class MethodDaoJdbc extends GenericDaoJdbc<MethodEntry, Long> implements  MethodDao {
  static final String ID_QUERY = "select methodentr_.id, methodentr_.parentID as parentID0_, methodentr_.signature as signature0_, methodentr_.args as args0_, methodentr_.rvalue as rvalue0_, methodentr_.exception as exception0_, methodentr_.callee as callee0_, methodentr_.caller as caller0_, methodentr_.properties as properties0_, methodentr_.depth as depth0_, methodentr_.sameMethodNumber as sameMet11_0_, methodentr_.timesCalled as timesCa12_0_, methodentr_.constructor as constru13_0_, methodentr_.started as started0_, methodentr_.finished as finished0_, methodentr_.elapsed as elapsed0_ from METHODS methodentr_ where methodentr_.id=?";
  static final String ALL_QUERY = "select methodentr_.id, methodentr_.parentID as parentID0_, methodentr_.signature as signature0_, methodentr_.args as args0_, methodentr_.rvalue as rvalue0_, methodentr_.exception as exception0_, methodentr_.callee as callee0_, methodentr_.caller as caller0_, methodentr_.properties as properties0_, methodentr_.depth as depth0_, methodentr_.sameMethodNumber as sameMet11_0_, methodentr_.timesCalled as timesCa12_0_, methodentr_.constructor as constru13_0_, methodentr_.started as started0_, methodentr_.finished as finished0_, methodentr_.elapsed as elapsed0_ from METHODS methodentr_";
  static final String CLEAR_QUERY = "DELETE * FROM METHODS";
  static final String INSERT = "insert into METHODS (parentID, signature, args, rvalue, exception, callee, caller, properties, depth, sameMethodNumber, timesCalled, constructor, started, finished, elapsed, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  static final String CREATE = "create table METHODS ( id bigint not null, parentID bigint, signature varchar(255), args longvarbinary, rvalue longvarbinary, exception longvarbinary, callee longvarbinary, caller longvarbinary, properties longvarbinary, depth integer, sameMethodNumber integer, timesCalled integer, constructor bit, started timestamp, finished timestamp, elapsed bigint, primary key (id)";


  static class MethodMapper implements RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
	int n = 1;
        long id = rs.getLong(n++);
        long parent = rs.getLong(n++);
        String signature = rs.getString(n++);
	Blob args = rs.getBlob(n++);
	Blob rvalue = rs.getBlob(n++);
	Blob exception = rs.getBlob(n++);
	Blob callee = rs.getBlob(n++);
	Blob caller = rs.getBlob(n++);
	Blob properties = rs.getBlob(n++);
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
        		toObject(caller),
        		toObject(callee),
        		signature, 
        		(Object[]) toObject(args),
        		constructor,
			depth);
	method.setReturnValue(toObject(rvalue));
	method.setException((Exception)toObject(exception));
	method.setProperties((Properties) toObject(properties));
	method.setSameMethodNumber(sameMethodNumber);
	method.setTimesCalled(timesCalled);

 	// do this after return value
	method.setStarted(started);
	method.setFinished(finished);
        return method;
    }
  }


  public MethodDaoJdbc() {
    super(new MethodMapper(), CREATE, ID_QUERY, ALL_QUERY, CLEAR_QUERY);
  }


  public MethodEntry save(MethodEntry m) {
    //createTable(); 
    int count = update(INSERT, new Object[] {
	m.getParentID(),
	m.getSignature(),
	m.getSerializedArgs(),
	m.getSerializedRvalue(),
	m.getSerializedException(),
	m.getSerializedCallee(),
	m.getSerializedCaller(),
	m.getSerializedProperties(),
	m.getDepth(),
	m.getSameMethodNumber(),
	m.getTimesCalled(),
	m.isConstructor(),
	m.getStarted(),
	m.getFinished(),
	m.getResponseTime(),
	m.getId()
	});
    if (count != 1) throw new DaoException("Failed to insert " + m + ", sql " + INSERT);
    return m;
  }

  public static void main(String[] args) throws Exception {
     MethodDaoJdbc dao = new MethodDaoJdbc();
     for (int i=0; i<10; i++) {
         MethodEntry method = new MethodEntry(
                        1000+i,
                        1000+i-1,
                        "caller",
                        "callee",
                        "signature",
                        new Object[] {"args"},
                        true,
                        i);
         dao.save(method);
     }
     System.out.println(dao.findAll());
  }
}
