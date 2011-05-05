/* ===========================================================================
 * $RCS$
 * Version: $Id: HibernateUtil.java,v 1.5 2007/07/16 17:37:15 shahzad Exp $
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
import com.plexobject.testplayer.MethodEntry;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.apache.commons.logging.*;

import javax.naming.*;


public class HibernateUtil {

    private static Log log = LogFactory.getLog(HibernateUtil.class);

    private static Configuration configuration;
    private static SessionFactory sessionFactory;

    static {
        // Create the initial SessionFactory from the default configuration files
        try {
            log.debug("Initializing Hibernate");

            // Read hibernate.properties, if present
            configuration = new Configuration().
            setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:baseball");

            //setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider").
            //addClass(MethodEntry.class);


            // Use annotations: configuration = new AnnotationConfiguration();

            // Read hibernate.cfg.xml (has to be present)
            configuration.configure();

            // Build and store (either in JNDI or static variable)
            rebuildSessionFactory(configuration);


            log.debug("Hibernate initialized, call HibernateUtil.getSessionFactory()");
        } catch (Throwable ex) {
            // We have to catch Throwable, otherwise we will miss
            // NoClassDefFoundError and other subclasses of Error
            log.error("Building SessionFactory failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns the Hibernate configuration that was used to build the SessionFac
tory.
     *
     * @return Configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the global SessionFactory either from a static variable or a JNDI
 lookup.
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        String sfName = configuration.getProperty(Environment.SESSION_FACTORY_NAME);
        if ( sfName != null) {
            log.debug("Looking up SessionFactory in JNDI");
            try {
                return (SessionFactory) new InitialContext().lookup(sfName);
            } catch (NamingException ex) {
                throw new RuntimeException(ex);
            }
        } else if (sessionFactory == null) {
            rebuildSessionFactory();
        }
        return sessionFactory;
    }





    /**
     * Closes the current SessionFactory and releases all resources.
     * <p>
     * The only other method that can be called on HibernateUtil
     * after this one is rebuildSessionFactory(Configuration).
     */
    public static void shutdown() {
        log.debug("Shutting down Hibernate");
        // Close caches and connection pools
        getSessionFactory().close();

        // Clear static variables
        sessionFactory = null;
    }


    /**
     * Rebuild the SessionFactory with the static Configuration.
     * <p>
     * Note that this method should only be used with static SessionFactory
     * management, not with JNDI or any other external registry. This method als
o closes
     * the old static variable SessionFactory before, if it is still open.
     */
     public static void rebuildSessionFactory() {
        log.debug("Using current Configuration to rebuild SessionFactory");
        rebuildSessionFactory(configuration);
     }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     * <p>
     * HibernateUtil does not configure() the given Configuration object,
     * it directly calls buildSessionFactory(). This method also closes
     * the old static variable SessionFactory before, if it is still open.
     *
     * @param cfg
     */
     public static void rebuildSessionFactory(Configuration cfg) {
        log.debug("Rebuilding the SessionFactory from given Configuration");
        if (sessionFactory != null && !sessionFactory.isClosed())
            sessionFactory.close();
        if (cfg.getProperty(Environment.SESSION_FACTORY_NAME) != null) {
            log.debug("Managing SessionFactory in JNDI");

            cfg.buildSessionFactory();
        } else {
            log.debug("Holding SessionFactory in static variable");
            sessionFactory = cfg.buildSessionFactory();
        }
        configuration = cfg;
     }

}

