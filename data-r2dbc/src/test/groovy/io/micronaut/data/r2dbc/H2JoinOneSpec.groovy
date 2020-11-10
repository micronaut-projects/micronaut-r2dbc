package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.data.tck.entities.Owner
import io.micronaut.data.tck.entities.Pet
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared

import javax.inject.Inject

@MicronautTest(rollback = false)
@Property(name = "r2dbc.datasources.default.url", value = "r2dbc:h2:mem:///testdb;DB_CLOSE_ON_EXIT=FALSE")
class H2JoinOneSpec extends AbstractR2dbcSpec {

    @Shared @Inject H2OwnerRepository ownerRepository
    @Shared @Inject H2PetRepository petRepository

    def setupSpec() {
        ownerRepository.setupData().block()
    }

    void 'test apply join to many to one association'() {
        when:
        def dino = petRepository.findByName("Dino").block()
        then:
        dino.name == "Dino"
        dino.owner.name == "Fred"

        when:
        def rabbid = petRepository.findByName("Rabbid").block()

        then:
        rabbid.owner.name == "Barney"
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
