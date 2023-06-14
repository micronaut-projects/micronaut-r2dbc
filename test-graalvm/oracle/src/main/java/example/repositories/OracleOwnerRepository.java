package example.repositories;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.Transactional;
import testgraalvm.repositories.OwnerRepository;

@R2dbcRepository(dialect = Dialect.ORACLE)
@Transactional(isolation = TransactionDefinition.Isolation.DEFAULT)
public interface OracleOwnerRepository extends OwnerRepository {
}
