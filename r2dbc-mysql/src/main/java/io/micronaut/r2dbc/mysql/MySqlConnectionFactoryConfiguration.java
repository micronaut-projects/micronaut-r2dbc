package io.micronaut.r2dbc.mysql;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.r2dbc.AbstractBasicR2dbcProperties;
import io.micronaut.r2dbc.BasicR2dbcProperties;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.Map;

/**
 * Implementation of {@link BasicR2dbcProperties} for MySQL.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@EachProperty(value = BasicR2dbcProperties.PREFIX, primary = "default")
public class MySqlConnectionFactoryConfiguration extends AbstractBasicR2dbcProperties<String> {
    /**
     * Default constructor.
     *
     * @param name    The name of the datasource
     * @param environment The environment
     */
    protected MySqlConnectionFactoryConfiguration(@Parameter String name, Environment environment) {
        super(name, newConnectionFactoryOptionsBuilder(name, environment, null));
    }

    /**
     * Sets the connection properties.
     *
     * @param options The options
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setOptions(
            @MapFormat(keyFormat = StringConvention.RAW)
            @Nullable Map<String, String> options) {
        if (CollectionUtils.isNotEmpty(options)) {
            options.forEach((key, value) ->
                    getBuilder().option(
                            Option.valueOf(key),
                            value
                    )
            );
        }
        return this;
    }

    /**
     * Sets the host.
     *
     * @param host The host
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setHost(@NotBlank String host) {
        return (MySqlConnectionFactoryConfiguration) super.setHost(host);
    }

    /**
     * Sets the port.
     *
     * @param port The port
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setPort(@Positive int port) {
        return (MySqlConnectionFactoryConfiguration) super.setPort(port);
    }

    /**
     * Sets the username.
     *
     * @param username The username
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setUsername(@NotBlank String username) {
        return (MySqlConnectionFactoryConfiguration) super.setUsername(username);
    }

    /**
     * Sets the password.
     *
     * @param password The password
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setPassword(CharSequence password) {
        return (MySqlConnectionFactoryConfiguration) super.setPassword(password);
    }

    /**
     * Sets the initial database name.
     *
     * @param database The database
     * @return These properties
     */
    @Override
    public MySqlConnectionFactoryConfiguration setDatabase(@NotBlank String database) {
        return (MySqlConnectionFactoryConfiguration) super.setDatabase(database);
    }
}
