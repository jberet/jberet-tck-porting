/*
 * Copyright (c) 2022 Red Hat, Inc. and/or its affiliates.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.jberet.tck.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class TestBean {
    private static final String SELECT_NUMBERS = "select * from Numbers";
    private static final String SELECT_INVENTORY = "select * from Inventory";
    Logger logger = Logger.getLogger(TestBean.class.getName());

    private DataSource dataSource;

    private void initDataSource() {
        final String jndiName = "jdbc/orderDB";
        try {
            final InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup(jndiName);
            logger.log(Level.INFO, "looked up " + jndiName + ": " + dataSource);
        } catch (NamingException e) {
            throw new IllegalStateException("failed to lookup " + jndiName, e);
        }
    }

    @PostConstruct
    private void postConstruct() {
        initDataSource();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            verifyDataBase(SELECT_NUMBERS);
            verifyDataBase(SELECT_INVENTORY);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to verify database:", e);
        } finally {
            safeClose(connection);
        }
    }

    private void safeClose(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "failed to close " + resource, e);
            }
        }
    }

    private void verifyDataBase(String query) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            List<List<Integer>> numbers = new ArrayList<>();
            while (resultSet.next()) {
                final int num1 = resultSet.getInt(1);
                final int num2 = resultSet.getInt(2);
                numbers.add(List.of(num1, num2));
            }
            logger.log(Level.INFO, query + ": " + numbers);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "failed to verify data " + query, e);
        } finally {
            safeClose(resultSet);
            safeClose(statement);
            safeClose(connection);
        }
    }
}
