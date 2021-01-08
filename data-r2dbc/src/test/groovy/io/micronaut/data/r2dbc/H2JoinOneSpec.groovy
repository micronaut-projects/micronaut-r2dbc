package io.micronaut.data.r2dbc

import io.micronaut.context.annotation.Property
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.tck.entities.Owner
import io.micronaut.data.tck.entities.Pet
import io.micronaut.data.tck.entities.Product
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rollback = false)
@H2Properties
class H2JoinOneSpec extends Specification {

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
}
