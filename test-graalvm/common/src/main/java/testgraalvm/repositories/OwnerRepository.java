package testgraalvm.repositories;

import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import io.micronaut.transaction.reactive.ReactiveTransactionStatus;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import testgraalvm.controllers.dto.OwnerDto;
import testgraalvm.domain.Owner;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.Transactional;

@Transactional(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
public interface OwnerRepository extends ReactorCrudRepository<Owner, Long> {
    Flux<OwnerDto> list();

    Mono<OwnerDto> findByName(String name);

    Mono<OwnerDto> getByName(String name);

    Flux<Owner> saveAll(Iterable<Owner> entities, ReactiveTransactionStatus<Connection> transactionStatus);
}
