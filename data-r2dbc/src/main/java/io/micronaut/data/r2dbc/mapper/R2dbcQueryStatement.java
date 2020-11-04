package io.micronaut.data.r2dbc.mapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.runtime.mapper.QueryStatement;
import io.r2dbc.spi.Statement;

import java.math.BigDecimal;
import java.util.Date;

public class R2dbcQueryStatement implements QueryStatement<Statement, Integer> {
    @Override
    public QueryStatement<Statement, Integer> setValue(Statement statement, Integer index, Object value) throws DataAccessException {
        if (value == null) {
            statement.bindNull(index, Object.class);
        } else {
            statement.bind(index, value);
        }
        return this;
    }

    @Nullable
    @Override
    public <T> T convertRequired(@Nullable Object value, Class<T> type) {
        return ConversionService.SHARED.convertRequired(
                value,
                type
        );
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setLong(Statement statement, Integer name, long value) {
        setValue(statement, name, value);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setChar(Statement statement, Integer name, char value) {
        setValue(statement, name, value);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setDate(Statement statement, Integer name, Date date) {
        if (date == null) {
            statement.bindNull(name, Date.class);
        } else {
            statement.bind(name, date);
        }
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setTimestamp(Statement statement, Integer name, Date date) {
        if (date == null) {
            statement.bindNull(name, Date.class);
        } else {
            statement.bind(name, date);
        }
        return this;
    }

    @Override
    public QueryStatement<Statement, Integer> setString(Statement statement, Integer name, String string) {
        if (string == null) {
            statement.bindNull(name, String.class);
        } else {
            statement.bind(name, string);
        }
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setInt(Statement statement, Integer name, int integer) {
        setValue(statement, name, integer);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setBoolean(Statement statement, Integer name, boolean bool) {
        setValue(statement, name, bool);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setFloat(Statement statement, Integer name, float f) {
        setValue(statement, name, f);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setByte(Statement statement, Integer name, byte b) {
        setValue(statement, name, b);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setShort(Statement statement, Integer name, short s) {
        setValue(statement, name, s);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setDouble(Statement statement, Integer name, double d) {
        setValue(statement, name, d);
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setBigDecimal(Statement statement, Integer name, BigDecimal bd) {
        if (bd == null) {
            statement.bindNull(name, BigDecimal.class);
        } else {
            statement.bind(name, bd);
        }
        return this;
    }

    @NonNull
    @Override
    public QueryStatement<Statement, Integer> setBytes(Statement statement, Integer name, byte[] bytes) {
        if (bytes == null) {
            statement.bindNull(name, byte[].class);
        } else {
            statement.bind(name, bytes);
        }
        return this;
    }
}
