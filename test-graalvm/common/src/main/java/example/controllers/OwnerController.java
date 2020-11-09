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

import example.controllers.dto.OwnerDto;
import example.domain.Owner;
import example.repositories.OwnerRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Controller("/owners")
class OwnerController {

    private final OwnerRepository ownerRepository;

    OwnerController(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Get
    Flowable<OwnerDto> all() {
        return ownerRepository.list();
    }

    @Get("/{name}")
    Maybe<OwnerDto> byName(@NotBlank String name) {
        return ownerRepository.findByName(name);
    }

    @Post("/")
    Single<Owner> save(@Valid Owner owner) {
        return Single.fromPublisher(ownerRepository.save(owner));
    }

    @Delete("/{id}")
    Single<HttpResponse<?>> delete(@NotNull Long id) {
        return Single.fromPublisher(ownerRepository.deleteById(id))
                     .map(c -> c > 0 ? HttpResponse.noContent() : HttpResponse.notFound());
    }

    @Put("/")
    Maybe<Owner> update(@Valid Owner owner) {
        return Single.fromPublisher(ownerRepository.update(owner)).toMaybe();
    }

}