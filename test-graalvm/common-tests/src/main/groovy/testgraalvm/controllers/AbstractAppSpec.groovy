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
package testgraalvm.controllers

import io.micronaut.core.annotation.Nullable
import testgraalvm.controllers.dto.OwnerDto
import testgraalvm.controllers.dto.PetDto
import testgraalvm.domain.Owner
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Stepwise
abstract class AbstractAppSpec extends Specification {

    @Inject
    PetClient petClient

    @Inject
    OwnersClient ownersClient

    def 'should fetch owners'() {
        when:
            def results = ownersClient.list()
        then:
            results.size() == 2
            results[0].name == "Fred"
            results[1].name == "Barney"
    }

    def 'should fetch owner by name'() {
        when:
            def result = ownersClient.get("Fred")
        then:
            result.name == "Fred"
    }

    def 'should fetch pets'() {
        when:
            def results = petClient.list()
        then:
            results.size() == 3
            results[0].name == "Dino"
            results[1].name == "Baby Puss"
            results[2].name == "Hoppy"
    }

    def 'should fetch pet by name'() {
        when:
            def result = petClient.get("Dino")
        then:
            result.name == "Dino"
    }

    def 'test CRUD'() {
        when:
        def owner = ownersClient.save(new Owner(name: "Joe", age: 25))

        then:
        owner
        owner.id
        owner.name == 'Joe'
        owner.age == 25
        ownersClient.get(owner.name).id == owner.id
        ownersClient.list().size() == 3

        when:
        owner.setAge(22)
        def updatedOwner = ownersClient.update(owner)

        then:
        updatedOwner
        updatedOwner.age == 22
        ownersClient.get(owner.name).age == 22

        when:
        def status = ownersClient.delete(owner.id).status()

        then:
        status == HttpStatus.NO_CONTENT
        ownersClient.delete(owner.id).status() == HttpStatus.NOT_FOUND
        ownersClient.get(owner.name) == null
        ownersClient.list().size() == 2
    }

    @Client("/pets")
    static interface PetClient {
        @Get("/")
        List<PetDto> list();

        @Get("/{name}")
        PetDto get(String name);
    }

    @Client("/owners")
    static interface OwnersClient {
        @Get("/")
        List<OwnerDto> list();

        @Get("/{name}")
        OwnerDto get(String name);

        @Post("/")
        Owner save(@Valid Owner owner);

        @Delete("/{id}")
        HttpResponse<?> delete(@NotNull Long id);

        @Put("/")
        @Nullable Owner update(@Valid Owner owner);
    }
}
