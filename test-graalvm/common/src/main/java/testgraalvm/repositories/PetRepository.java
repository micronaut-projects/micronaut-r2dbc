package testgraalvm.repositories;

import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import testgraalvm.controllers.dto.PetDto;
import testgraalvm.domain.Pet;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.Transactional;

@Transactional(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
public interface PetRepository extends ReactorCrudRepository<Pet, Long> {
    Flux<PetDto> list();

    Mono<PetDto> findByName(String name);
}
