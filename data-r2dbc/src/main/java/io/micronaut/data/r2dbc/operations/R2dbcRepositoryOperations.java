package io.micronaut.data.r2dbc.operations;

import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.operations.async.AsyncCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveCapableRepository;

/**
 * An interface for R2DBC repository operations.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public interface R2dbcRepositoryOperations extends RepositoryOperations, ReactiveCapableRepository, AsyncCapableRepository {
}
