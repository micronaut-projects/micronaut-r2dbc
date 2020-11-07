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

import example.controllers.dto.OwnerDto
import example.controllers.dto.PetDto
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

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
    }
}
