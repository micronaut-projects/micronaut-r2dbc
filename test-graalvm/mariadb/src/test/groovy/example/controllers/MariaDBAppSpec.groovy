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

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.utility.DockerImageName
import testgraalvm.controllers.AbstractDBContainerAppSpec

@MicronautTest
class MariaDBAppSpec extends AbstractDBContainerAppSpec {

    @Override
    JdbcDatabaseContainer getJdbcDatabaseContainer() {
        return new MariaDBContainer(DockerImageName.parse("mariadb:10.3.6"))
    }

    @Override
    String getDriverName() {
        return "mariadb"
    }

    @Override
    Dialect getDatabaseDialect() {
        return Dialect.MYSQL
    }
}
