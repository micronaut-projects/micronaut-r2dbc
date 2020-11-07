/*
 * Copyright 2017-2020 original authors
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
package example.controllers

import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.JdbcDatabaseContainer
import spock.lang.AutoCleanup
import spock.lang.Shared

abstract class AbstractDBContainerAppSpec extends AbstractAppSpec implements TestPropertyProvider {

    @Shared
    @AutoCleanup
    JdbcDatabaseContainer dbContainer = getJdbcDatabaseContainer()

    @Override
    Map<String, String> getProperties() {
        dbContainer.start()
        def props = [
                "datasources.default.url"            : dbContainer.getJdbcUrl(),
                "datasources.default.driverClassName": dbContainer.getDriverClassName(),
                "datasources.default.username"       : dbContainer.getUsername(),
                "datasources.default.password"       : dbContainer.getPassword(),
                "r2dbc.datasources.default.host"     : dbContainer.getHost(),
                "r2dbc.datasources.default.port"     : dbContainer.getFirstMappedPort(),
                "r2dbc.datasources.default.driver"   : getDriverName(),
                "r2dbc.datasources.default.username" : dbContainer.getUsername(),
                "r2dbc.datasources.default.password" : dbContainer.getPassword()
        ]
        try {
            def database = dbContainer.getDatabaseName()
            props.put("r2dbc.datasources.default.database" , database)
        } catch (e) {
        }
        return props
    }

    abstract JdbcDatabaseContainer getJdbcDatabaseContainer();

    abstract String getDriverName();

}
