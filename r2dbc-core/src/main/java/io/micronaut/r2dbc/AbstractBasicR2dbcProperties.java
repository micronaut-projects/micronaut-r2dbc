package io.micronaut.r2dbc;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.r2dbc.spi.ConnectionFactoryOptions;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;

/**
 * Abstract implementation of {@link BasicR2dbcProperties}.
 * @param <O> The option type
 * @author graemerocher
 * @since 1.0.0
 */
public abstract class AbstractBasicR2dbcProperties<O> implements BasicR2dbcProperties<O> {
    private final ConnectionFactoryOptions.Builder builder;
    private final String name;

    /**
     * Default constructor.
     * @param name The name of the datasource
     * @param builder The {@link io.r2dbc.spi.ConnectionFactoryOptions.Builder}
     */
    protected AbstractBasicR2dbcProperties(String name, ConnectionFactoryOptions.Builder builder) {
        this.builder = builder;
        this.name = name;
    }

    @Override
    @NonNull public ConnectionFactoryOptions.Builder builder() {
        return builder;
    }

    /**
     * Sets the protocol.
     * @param protocol The protocol
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setProtocol(@NotBlank String protocol) {
        if (protocol != null) {
            this.builder.option(ConnectionFactoryOptions.PROTOCOL, protocol);
        }
        return this;
    }

    /**
     * Sets the connect timeout.
     *
     * @param duration The duration
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setConnectTimeout(Duration duration) {
        if (duration != null) {
            this.builder.option(ConnectionFactoryOptions.CONNECT_TIMEOUT, duration);
        }
        return this;
    }

    /**
     * Sets whether to prefer SSL configuration
     *
     * @param ssl Sets whether to prefer SSL
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setSsl(boolean ssl) {
        this.builder.option(ConnectionFactoryOptions.SSL, ssl);
        return this;
    }

    /**
     * Create a {@link ConnectionFactoryOptions.Builder} from the configured URL if present.
     * @param name The name of the datasource
     * @param env The environment
     * @return The builder
     */
    protected static ConnectionFactoryOptions.Builder newConnectionFactoryOptionsBuilder(String name, Environment env, @Nullable String defaultUrl) {
        String property = PREFIX + "." + name + ".url";
        String url = env.getProperty(
                property, Argument.STRING
        ).orElse(defaultUrl);
        if (url != null) {
            return ConnectionFactoryOptions.parse(url).mutate();
        } else {
            return ConnectionFactoryOptions.builder();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The connection factory options builder.
     */
    public ConnectionFactoryOptions.Builder getBuilder() {
        return builder;
    }

    /**
     * Sets the host.
     * @param host The host
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setHost(@NotBlank String host) {
        builder.option(
                ConnectionFactoryOptions.HOST, host
        );
        return this;
    }

    /**
     * Sets the port.
     * @param port The port
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setPort(@Positive int port) {
        builder.option(
                ConnectionFactoryOptions.PORT, port
        );
        return this;
    }

    /**
     * Sets the username.
     * @param username The username
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setUsername(@NotBlank String username) {
        builder.option(
                ConnectionFactoryOptions.USER, username
        );
        return this;
    }

    /**
     * Sets the password.
     * @param password The password
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setPassword(CharSequence password) {
        builder.option(
                ConnectionFactoryOptions.PASSWORD, password
        );
        return this;
    }

    /**
     * Sets the initial database name.
     * @param database The database
     * @return These properties
     */
    @Override
    public BasicR2dbcProperties<O> setDatabase(@NotBlank String database) {
        builder.option(
                ConnectionFactoryOptions.DATABASE, database
        );
        return this;
    }
}
