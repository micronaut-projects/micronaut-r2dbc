package io.micronaut.data.r2dbc.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.annotation.TypeRole;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.r2dbc.operations.R2dbcRepositoryOperations;
import io.r2dbc.spi.Connection;

import java.lang.annotation.*;

/**
 * Stereotype repository that configures a {@link Repository} as a {@link R2dbcRepository} using
 * raw SQL encoding and {@link R2dbcRepositoryOperations} as the runtime engine.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@RepositoryConfiguration(
        queryBuilder = SqlQueryBuilder.class,
        operations = R2dbcRepositoryOperations.class,
        implicitQueries = false,
        namedParameters = false,
        typeRoles = @TypeRole(
                role = R2dbcRepository.PARAMETER_CONNECTION,
                type = Connection.class
        )
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Repository
public @interface R2dbcRepository {

    String PARAMETER_CONNECTION = "connection";

    /**
     * @return The dialect to use.
     */
    @AliasFor(annotation = Repository.class, member = "dialect")
    Dialect dialect() default Dialect.ANSI;

    /**
     * @return The dialect to use.
     */
    @AliasFor(annotation = Repository.class, member = "dialect")
    @AliasFor(member = "dialect")
    String dialectName() default "ANSI";
}
