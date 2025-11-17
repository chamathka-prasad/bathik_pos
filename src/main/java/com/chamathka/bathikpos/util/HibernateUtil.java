package com.chamathka.bathikpos.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing Hibernate SessionFactory
 * Follows the Singleton pattern to ensure only one SessionFactory instance exists
 */
public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

            logger.info("Hibernate SessionFactory created successfully");
        } catch (Exception e) {
            logger.error("Failed to create SessionFactory", e);
            throw new ExceptionInInitializerError("Failed to initialize Hibernate: " + e.getMessage());
        }
    }

    /**
     * Get the SessionFactory instance
     * @return SessionFactory instance
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Close the SessionFactory and release all resources
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Shutting down Hibernate SessionFactory");
            sessionFactory.close();
        }
    }
}