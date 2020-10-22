package io.micronaut.r2dbc.h2;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.r2dbc.AbstractBasicR2dbcProperties;
import io.micronaut.r2dbc.BasicR2dbcProperties;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.Map;

/**
 * Implementation of {@link BasicR2dbcProperties} for H2.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@EachProperty(value = BasicR2dbcProperties.PREFIX, primary = "default")
public class H2ConnectionFactoryConfiguration extends AbstractBasicR2dbcProperties<H2ConnectionOption> {

    protected H2ConnectionFactoryConfiguration(@Parameter String name, Environment env) {
        super(name, newConnectionFactoryOptionsBuilder(name, env, null).option(
                ConnectionFactoryOptions.DRIVER,
                "h2"
        ));
    }

    /**
     * Sets the connection properties.
     * @param options The options
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setOptions(
            @MapFormat(keyFormat = StringConvention.UNDER_SCORE_SEPARATED)
            @Nullable Map<H2ConnectionOption, String> options) {
        if (CollectionUtils.isNotEmpty(options)) {
            options.forEach((key, value) ->
               getBuilder().option(
                       Option.valueOf(key.name()),
                       value
               )
            );
        }
        return this;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol The protocol
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setProtocol(@NotBlank String protocol) {
        return (H2ConnectionFactoryConfiguration) super.setProtocol(protocol);
    }

    /**
     * Sets the host.
     *
     * @param host The host
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setHost(@NotBlank String host) {
        return (H2ConnectionFactoryConfiguration) super.setHost(host);
    }

    /**
     * Sets the port.
     *
     * @param port The port
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setPort(@Positive int port) {
        return (H2ConnectionFactoryConfiguration) super.setPort(port);
    }

    /**
     * Sets the username.
     *
     * @param username The username
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setUsername(@NotBlank String username) {
        return (H2ConnectionFactoryConfiguration) super.setUsername(username);
    }

    /**
     * Sets the password.
     *
     * @param password The password
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setPassword(CharSequence password) {
        return (H2ConnectionFactoryConfiguration) super.setPassword(password);
    }

    /**
     * Sets the initial database name.
     *
     * @param database The database
     * @return These properties
     */
    @Override
    public H2ConnectionFactoryConfiguration setDatabase(@NotBlank String database) {
        return (H2ConnectionFactoryConfiguration) super.setDatabase(database);
    }
}
