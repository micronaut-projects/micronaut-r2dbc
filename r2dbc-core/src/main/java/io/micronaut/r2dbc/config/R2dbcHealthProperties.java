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
package io.micronaut.r2dbc.config;

/**
 * Default database r2dbc health queries.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.1.0
 */
public final class R2dbcHealthProperties {

    /**
     * Return only info about SQL server version and do not leak environment details.
     * Example: 'Microsoft SQL Server 2017'
     *
     * @see <a href='https://www.mssqltips.com/sqlservertip/1140/how-to-tell-what-sql-server-version-you-are-running/'>MS SQL Version</a>
     */
    public static final String MSSQL_QUERY = "SELECT TOP 1 value FROM STRING_SPLIT(@@VERSION, '(');";
    public static final String COMMON_QUERY = "SELECT version();";
    public static final String ORACLE_QUERY = "select * from dual";

    public static final String POSTGRES = "PostgreSQL";
    public static final String MARIADB = "MariaDB";
    public static final String MYSQL = "MySQL";
    public static final String MSSQL = "Microsoft SQL Server";
    public static final String ORACLE = "Oracle Database";

    private R2dbcHealthProperties() { }
}
