package example.repositories;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import testgraalvm.repositories.OwnerRepository;

@R2dbcRepository(dialect = Dialect.MYSQL)
public interface MariaOwnerRepository extends OwnerRepository {
}
