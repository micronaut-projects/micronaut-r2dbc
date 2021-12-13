/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.r2dbc.health;

import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides Health Query for selection version from database.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.0.1
 */
@Singleton
public class R2dbcHealthQueryProvider {

    /**
     * Return only info about SQL server version and do not leak environment details.
     * Example: 'Microsoft SQL Server 2017'
     * @see <a href='https://www.mssqltips.com/sqlservertip/1140/how-to-tell-what-sql-server-version-you-are-running/'>MS SQL Version</a>
     */
    private static final String MSSQL_QUERY = "SELECT TOP 1 value FROM STRING_SPLIT(@@VERSION, '(');";
    private static final String COMMON_QUERY = "SELECT version();";

    private static final String POSTGRES = "PostgreSQL";
    private static final String MARIADB = "MariaDB";
    private static final String MYSQL = "MySQL";
    private static final String MSSQL = "Microsoft SQL Server";

    /**
     * {@link io.r2dbc.spi.ConnectionFactoryMetadata#getName()} to SQL query for database version.
     */
    private final Map<String, String> metadataNameToQuery;

    public R2dbcHealthQueryProvider() {
        this.metadataNameToQuery = new HashMap<>(12);
        this.metadataNameToQuery.put(POSTGRES, COMMON_QUERY);
        this.metadataNameToQuery.put(MARIADB, COMMON_QUERY);
        this.metadataNameToQuery.put(MYSQL, COMMON_QUERY);
        this.metadataNameToQuery.put(MSSQL, MSSQL_QUERY);
    }

    /**
     * @param metadataName name of r2dbc driver from metadata {@link io.r2dbc.spi.ConnectionFactoryMetadata#getName()}
     * @return SQL query to return version for specified database
     * @see #COMMON_QUERY
     */
    public Optional<String> getHealthQuery(String metadataName) {
        return Optional.ofNullable(metadataNameToQuery.get(metadataName));
    }
}
