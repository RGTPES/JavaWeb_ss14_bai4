package org.example.javaweb_ss14_bai4.utils;

import org.example.javaweb_ss14_bai4.model.Order;
import org.example.javaweb_ss14_bai4.model.Product;
import org.example.javaweb_ss14_bai4.model.StockReservation;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Properties;
public class HibernateUtils {
    private static final SessionFactory sessionFactory = buildSessionFactory();
    private static SessionFactory buildSessionFactory() {
        try {
            Configuration config = new Configuration();
            Properties props = new Properties();
            props.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            props.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/javaweb_ss14_bai4");
            props.put("hibernate.connection.username", "root");
            props.put("hibernate.connection.password", "123456");

            props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.hbm2ddl.auto", "update");

            config.setProperties(props);

            config.addAnnotatedClass(Product.class);
            config.addAnnotatedClass(Order.class);
            config.addAnnotatedClass(StockReservation.class);

            return config.buildSessionFactory();

        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}