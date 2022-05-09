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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
@DataSourceDefinition(
        name = "jdbc/orderDB",
//        name = "java:/jdbc/orderDB",
//        name = "java:jboss/exported/jdbc/orderDB",
        className = "org.h2.jdbcx.JdbcDataSource",
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        user = "sa",
        password = "sa",
        properties = {"use-java-context=false"})
public class BatchTckConfigBean {
    private static final String SQL_FILE = "h2.ddl.jbatch-tck.sql";
    private static final String SELECT_NUMBERS = "select * from Numbers";
    private static final String SELECT_INVENTORY = "select * from Inventory";
    Logger logger = Logger.getLogger(BatchTckConfigBean.class.getName());
    @Resource(lookup = "jdbc/orderDB")
    private DataSource dataSource;

    @PostConstruct
    private void postConstruct() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            final List<String> sqls = loadSql();
            for (String sql : sqls) {
                try (Statement statement1 = connection.createStatement()) {
                    statement1.execute(sql);
                } catch (SQLException f) {
                    if(!sql.toLowerCase(Locale.ROOT).startsWith("drop")) {
                        logger.log(Level.SEVERE, "failed to init database:", f);
                    }
                }
            }
            logger.log(Level.INFO, "executed " + sqls.size() + " statements");
            verifyDataBase(SELECT_NUMBERS);
            verifyDataBase(SELECT_INVENTORY);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to init database:", e);
        } finally {
            safeClose(connection);
        }
    }

    private List<String> loadSql() {
        final ArrayList<String> result = new ArrayList<>();
        final InputStream asStream = getClass().getClassLoader().getResourceAsStream(SQL_FILE);
        if (asStream == null) {
            throw new IllegalStateException("failed to load sql file: " + SQL_FILE);
        }
        Scanner scanner = new Scanner(asStream, StandardCharsets.UTF_8);
        scanner.useDelimiter(";");
        while (scanner.hasNext()) {
            result.add(scanner.next().trim());
        }
        return result;
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
