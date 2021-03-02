package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.r2dbc.spi.ConnectionFactory
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:mariadb:///databasename?TC_IMAGE_TAG=10.3.6")
@Property(name = "r2dbc.datasources.default.schema-generate", value = "CREATE_DROP")
@Property(name = "r2dbc.datasources.default.dialect", value = "MYSQL")
class MariaDbReactiveRepositorySpec extends AbstractReactiveRepositorySpec {
    @Inject
    @Shared
    MySqlPersonRepository personReactiveRepository

    @Inject
    @Shared
    MySqlProductRepository productReactiveRepository

    @Inject
    @Shared
    ConnectionFactory connectionFactory

    @Inject
    @Shared
    R2dbcOperations r2dbcOperations

    @Override
    PersonReactiveRepository getPersonRepository() {
        return personReactiveRepository
    }

    @Override
    ProductReactiveRepository getProductRepository() {
        return productReactiveRepository
    }

}
