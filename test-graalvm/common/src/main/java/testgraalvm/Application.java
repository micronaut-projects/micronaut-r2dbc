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
package testgraalvm;

import testgraalvm.domain.Owner;
import testgraalvm.domain.Pet;
import testgraalvm.repositories.OwnerRepository;
import testgraalvm.repositories.PetRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

import jakarta.inject.Singleton;
import java.util.Arrays;

@Singleton
public class Application {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final R2dbcOperations operations;

    public Application(OwnerRepository ownerRepository, PetRepository petRepository, R2dbcOperations operations) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.operations = operations;
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @EventListener
    void init(StartupEvent event) {
        Owner fred = new Owner();
        fred.setName("Fred");
        fred.setAge(45);
        Owner barney = new Owner();
        barney.setName("Barney");
        barney.setAge(40);

        Pet dino = new Pet();
        dino.setName("Dino");
        dino.setOwner(fred);
        Pet bp = new Pet();
        bp.setName("Baby Puss");
        bp.setOwner(fred);
        bp.setType(Pet.PetType.CAT);
        Pet hoppy = new Pet();
        hoppy.setName("Hoppy");
        hoppy.setOwner(barney);

        Flowable.fromPublisher(operations.withTransaction((status) ->
            Flowable.fromPublisher(ownerRepository.saveAll(Arrays.asList(fred, barney), status.getConnection())).toList().flatMapPublisher(owners ->
                    petRepository.saveAll(Arrays.asList(dino, bp, hoppy), status.getConnection())
            )
        )).blockingSubscribe(
                (pet) -> {},
                (error) -> {
                    throw new ApplicationStartupException("Error saving initial data: " + error.getMessage());
                }
        );
    }
}