package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.tck.entities.Owner
import io.micronaut.data.tck.entities.Pet
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb;DB_CLOSE_ON_EXIT=FALSE")
@Stepwise
class H2TransactionManagementSpec extends AbstractR2dbcSpec {

    @Shared @Inject H2OwnerRepository ownerRepository
    @Shared @Inject H2PetRepository petRepository

    void 'test rollback only'() {
        when:"When setRollbackOnly is called"
        ownerRepository.testSetRollbackOnly().block()

        then:"The transaction is rolled back"
        Mono.from(ownerRepository.count()).block() == 0
    }

    void 'test rollback on exception'() {
        when:"When setRollbackOnly is called"
        ownerRepository.testRollbackOnException().block()

        then:"The transaction is rolled back"
        def e = thrown(RuntimeException)
        e.message == "Something bad happened"
        Mono.from(ownerRepository.count()).block() == 0
    }

    void 'test success'() {
        when:"When setRollbackOnly is called"
        ownerRepository.setupData().block()

        then:"The transaction is rolled back"
        Mono.from(ownerRepository.count()).block() == 2
    }

    @Override
    protected Dialect getDialect() {
        return Dialect.H2
    }

    @Override
    protected List<Class<? extends Object>> getEntities() {
        return [Pet, Owner]
    }
}
