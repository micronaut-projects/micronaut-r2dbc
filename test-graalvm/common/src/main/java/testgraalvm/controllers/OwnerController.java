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
package testgraalvm.controllers;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import testgraalvm.controllers.dto.OwnerDto;
import testgraalvm.domain.Owner;
import testgraalvm.repositories.OwnerRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

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
    Flux<OwnerDto> all() {
        return ownerRepository.list();
    }

    @Get("/{name}")
    Mono<OwnerDto> byName(@NotBlank String name) {
        return ownerRepository.findByName(name);
    }

    @Post("/")
    Mono<OwnerDto> save(@Valid OwnerDto owner) {
        return Mono.from(ownerRepository.save(new Owner(owner.getName(), owner.getAge()))
            .map(o -> new OwnerDto(o.getId(), o.getName(), o.getAge())));
    }

    @Delete("/{id}")
    Mono<HttpResponse<?>> delete(@NotNull Long id) {
        return Mono.from(ownerRepository.deleteById(id))
                     .map(c -> c > 0 ? HttpResponse.noContent() : HttpResponse.notFound());
    }

    @Put("/")
    Mono<OwnerDto> update(@Valid OwnerDto owner) {
        return Mono.from(ownerRepository.update(new Owner(owner.getId(), owner.getName(), owner.getAge()))
            .map(o -> new OwnerDto(o.getId(), o.getName(), o.getAge())));
    }

}
