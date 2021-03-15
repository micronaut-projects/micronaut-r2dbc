package example.repositories;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import testgraalvm.repositories.OwnerRepository;

@R2dbcRepository(dialect = Dialect.H2)
public interface H2OwnerRepository extends OwnerRepository {

}
