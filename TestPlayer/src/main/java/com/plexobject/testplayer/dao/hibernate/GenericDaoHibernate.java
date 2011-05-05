/* ===========================================================================
 * $RCS$
 * Version: $Id: GenericDaoHibernate.java,v 1.13 2007/07/16 17:37:15 shahzad Exp $
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
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.commons.dbcp.BasicDataSource;

import com.plexobject.testplayer.dao.*;
import com.plexobject.testplayer.*;
import org.hibernate.*;
import org.hibernate.criterion.*;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.util.Date;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.hibernate.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


public abstract class GenericDaoHibernate<T, ID extends Serializable> implements GenericDao<T, ID> {
    private Class<T> persistentClass;
    private Session session;
    private RowMapper mapper;
    private String allQuery;
    private String finderQuery;

    public GenericDaoHibernate(RowMapper mapper, String allQuery, String finderQuery) {
        this.mapper = mapper;
        this.allQuery = allQuery;
        this.finderQuery = finderQuery;
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
     }

    public void setSession(Session s) {
        this.session = s;
    }

    protected Session getSession() {
        if (session == null || !session.isOpen())
            session = HibernateUtil.getSessionFactory().getCurrentSession();
        return session;
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id) {
	JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
        List<T> list = (List<T>) jdbc.query(finderQuery, new Object[] {id}, mapper);
        if (list.size() == 0) throw new DaoException("Failed to find method with id " + id);
        if (list.size() > 1) throw new DaoException("Found multiple methods with id " + id + ": " + list);
	return list.get(0);
/*
        T entity; 
  	boolean lock = false;
        Transaction tx = getSession().beginTransaction();
        try {
            if (lock) entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
            else entity = (T) getSession().load(getPersistentClass(), id);

            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            try {
              tx.rollback();
            } catch (RuntimeException ex) {}
            throw e;
        }
*/
    }

    protected static DataSource _getDataSource() {
	BasicDataSource source = new BasicDataSource();
	source.setDriverClassName("org.hsqldb.jdbcDriver");
	source.setUrl("jdbc:hsqldb:file:testplayer.db");
	//source.setUrl("jdbc:hsqldb:hsql://localhost/testplayer");
	source.setUsername("sa");
	source.setPassword("");
	source.setMaxActive(15);
	source.setMaxIdle(4);
	return source;
    }

    protected static DataSource getDataSource() {
	BasicDataSource source = new BasicDataSource();
	source.setDriverClassName("com.mysql.jdbc.Driver");
	source.setUrl("jdbc:mysql://localhost/testplayer");
	source.setUsername("root");
	source.setPassword("root");
	source.setMaxActive(15);
	source.setMaxIdle(4);
	return source;
    }


    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        //if (true) return findByCriteria();

	JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
        return jdbc.query(allQuery, new Object[0], mapper);

/*
        Transaction tx = getSession().beginTransaction();
        try {
            //Criteria criteria = getSession().createCriteria(getPersistentClass());
            Criteria criteria = getSession().createCriteria(MethodEntry.class);
            tx.commit();
            return criteria.list();
        } catch (RuntimeException e) {
            try {
              tx.rollback();
            } catch (RuntimeException ex) {}
            throw e;
        }
*/
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T save(T entity) {
        Transaction tx = getSession().beginTransaction();
        try {
            getSession().saveOrUpdate(entity);
            tx.commit();
        } catch (RuntimeException e) {
            try {
              tx.rollback();
            } catch (RuntimeException ex) {}
            throw e;
        }
        return entity;
    }

    public void makeTransient(T entity) {
        getSession().delete(entity);
    }

    public void flush() {
        getSession().flush();

    }

    public void clear() {
        getSession().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Transaction tx = getSession().beginTransaction();
        try {
            Criteria crit = getSession().createCriteria(getPersistentClass());
            for (Criterion c : criterion) {
                crit.add(c);
            }
            tx.commit();
            return crit.list();
        } catch (RuntimeException e) {
            try {
              tx.rollback();
            } catch (RuntimeException ex) {}
            throw e;
        }
   }
}
