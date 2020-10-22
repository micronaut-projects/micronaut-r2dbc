package io.micronaut.r2dbc;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.naming.Named;
import io.r2dbc.spi.ConnectionFactoryOptions;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;

/**
 * An interface to model configuring basic R2DBC properties
 *
 * @since 1.0.0
 * @author graemerocher
 */
public interface BasicR2dbcProperties<O> extends Named {
    /**
     * The prefix for configuration
     */
    String PREFIX = "r2dbc.datasources";

    /**
     * Sets the host.
     * @param host The host
     * @return These properties
     */
    BasicR2dbcProperties<O> setHost(@NotBlank String host);

    /**
     * Sets the port.
     * @param port The port
     * @return These properties
     */
    BasicR2dbcProperties<O> setPort(@Positive int port);

    /**
     * Sets the protocol.
     * @param protocol The protocol
     * @return These properties
     */
    BasicR2dbcProperties<O> setProtocol(@NotBlank String protocol);

    /**
     * Sets the connect timeout.
     * @param duration The duration
     * @return These properties
     */
    BasicR2dbcProperties<O> setConnectTimeout(Duration duration);

    /**
     * Sets whether to prefer SSL configuration
     * @param ssl Sets whether to prefer SSL
     * @return These properties
     */
    BasicR2dbcProperties<O> setSsl(boolean ssl);

    /**
     * Sets the username.
     * @param username The username
     * @return These properties
     */
    BasicR2dbcProperties<O> setUsername(@NotBlank String username);

    /**
     * Sets the password.
     * @param password The password
     * @return These properties
     */
    BasicR2dbcProperties<O> setPassword(CharSequence password);

    /**
     * Sets the initial database name.
     * @param database The database
     * @return These properties
     */
    BasicR2dbcProperties<O> setDatabase(@NotBlank String database);

    /**
     * Sets the connection properties.
     * @param options The options
     * @return These properties
     */
    BasicR2dbcProperties<O> setOptions(
            @Nullable Map<O, String> options);

    /**
     * @return Return the current builder
     */
    @NonNull ConnectionFactoryOptions.Builder builder();
}
