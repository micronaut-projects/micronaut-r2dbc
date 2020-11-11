package example.repositories;

import example.controllers.dto.PetDto;
import example.domain.Pet;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import io.r2dbc.spi.Connection;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.reactivestreams.Publisher;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@TransactionalAdvice(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
public interface PetRepository extends ReactiveStreamsCrudRepository<Pet, Long> {
    Flowable<PetDto> list();

    Maybe<PetDto> findByName(String name);

    <S extends Pet> Publisher<S> saveAll(@Valid @NotNull Iterable<S> entities, Connection connection);
}
