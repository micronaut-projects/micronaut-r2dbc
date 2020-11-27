package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.repositories.PersonReactiveRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import reactor.core.publisher.Mono
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:tc:mariadb:///databasename?TC_IMAGE_TAG=10.3.6")
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

    @Override
    void init() {
        Mono.from(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<Connection> status ->
            status.connection.createStatement(
                    "CREATE TABLE `person` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(255) NOT NULL, `age` INT, `enabled` BIT);"
            ).execute()
        })).block()
        Mono.from(r2dbcOperations.withTransaction({ ReactiveTransactionStatus<Connection> status ->
            status.connection.createStatement(
                "CREATE TABLE `product` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(255) NOT NULL, `price` DECIMAL, `date_created` DATETIME NULL, `last_updated` DATETIME NULL);"
            ).execute()
        })).block()
    }
}
