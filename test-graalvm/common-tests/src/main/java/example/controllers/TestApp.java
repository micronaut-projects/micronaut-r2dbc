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
package example.controllers;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.testresources.client.TestResourcesClient;
import io.micronaut.testresources.client.TestResourcesClientFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import testgraalvm.controllers.dto.OwnerDto;
import testgraalvm.controllers.dto.PetDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest(transactional = false)
public class TestApp implements TestPropertyProvider {

    @Inject
    PetClient petClient;

    @Inject
    OwnersClient ownersClient;

    @Inject
    ApplicationContext context;

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("micronaut.test.resources.scope", getClass().getName());
        return properties;
    }

    @AfterAll
    public void cleanup() {
        try {
            TestResourcesClient testResourcesClient = TestResourcesClientFactory.extractFrom(context);
            testResourcesClient.closeScope(getClass().getName());
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void crud() {
        // shouldFetchOwners
        List<OwnerDto> results = ownersClient.list();
        assertEquals(2, results.size());
        assertEquals("Fred", results.get(0).getName());
        assertEquals("Barney", results.get(1).getName());

       // shouldFetchOwnerByName() {
        OwnerDto owner = ownersClient.get("Fred");
        assertEquals("Fred", owner.getName());

        // shouldFetchPets()
        List<PetDto> pets = petClient.list();
        assertEquals(3, pets.size());
        assertEquals("Dino", pets.get(0).getName());
        assertEquals("Baby Puss", pets.get(1).getName());
        assertEquals("Hoppy", pets.get(2).getName());

        // shouldFetchPetByName() {
        PetDto pet = petClient.get("Dino");
        assertEquals("Dino", pet.getName());

        // testCRUD()
        owner = ownersClient.save(new OwnerDto(null, "Joe", 25));
        assertNotNull(owner);
        assertNotNull(owner.getId());
        assertEquals("Joe", owner.getName());
        assertEquals(25, owner.getAge());
        assertEquals(owner.getId(), ownersClient.get(owner.getName()).getId());
        assertEquals(3, ownersClient.list().size());

        owner.setAge(22);
        OwnerDto updatedOwner = ownersClient.update(owner);
        assertNotNull(updatedOwner);
        assertEquals(22, updatedOwner.getAge());
        assertEquals(22, ownersClient.get(owner.getName()).getAge());

        HttpStatus status = ownersClient.delete(owner.getId()).getStatus();
        assertEquals(HttpStatus.NO_CONTENT, status);

        status = ownersClient.delete(owner.getId()).getStatus();
        assertEquals(HttpStatus.NOT_FOUND, status);

        assertNull(ownersClient.get(owner.getName()));
        assertEquals(2, ownersClient.list().size());
    }

    @Client("/pets")
    interface PetClient {
        @Get("/")
        List<PetDto> list();

        @Get("/{name}")
        PetDto get(String name);
    }

    @Client("/owners")
    interface OwnersClient {
        @Get("/")
        List<OwnerDto> list();

        @Get("/{name}")
        OwnerDto get(String name);

        @Post("/")
        OwnerDto save(@Body OwnerDto owner);

        @Delete("/{id}")
        HttpResponse<?> delete(Long id);

        @Put("/")
        @Nullable
        OwnerDto update(@Body OwnerDto owner);
    }
}
