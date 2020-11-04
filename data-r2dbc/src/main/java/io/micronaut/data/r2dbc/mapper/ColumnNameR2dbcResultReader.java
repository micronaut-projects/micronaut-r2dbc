package io.micronaut.data.r2dbc.mapper;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.r2dbc.spi.Row;

import java.math.BigDecimal;
import java.util.Date;

public class ColumnNameR2dbcResultReader implements ResultReader<Row, String> {
    private final ConversionService<?> conversionService = ConversionService.SHARED;

    @Override
    public long readLong(Row resultSet, String name) {
        Long l = resultSet.get(name, Long.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public char readChar(Row resultSet, String name) {
        Character character = resultSet.get(name, Character.class);
        if (character != null) {
            return character;
        }
        return 0;
    }

    @Override
    public Date readDate(Row resultSet, String name) {
        return resultSet.get(name, Date.class);
    }

    @Override
    public Date readTimestamp(Row resultSet, String index) {
        return resultSet.get(index, Date.class);
    }

    @Nullable
    @Override
    public String readString(Row resultSet, String name) {
        return resultSet.get(name, String.class);
    }

    @Override
    public int readInt(Row resultSet, String name) {
        Integer l = resultSet.get(name, Integer.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public boolean readBoolean(Row resultSet, String name) {
        Boolean l = resultSet.get(name, Boolean.class);
        if (l != null) {
            return l;
        } else {
            return false;
        }
    }

    @Override
    public float readFloat(Row resultSet, String name) {
        Float l = resultSet.get(name, Float.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public byte readByte(Row resultSet, String name) {
        Byte l = resultSet.get(name, Byte.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public short readShort(Row resultSet, String name) {
        Short l = resultSet.get(name, Short.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public double readDouble(Row resultSet, String name) {
        Double l = resultSet.get(name, Double.class);
        if (l != null) {
            return l;
        } else {
            return 0;
        }
    }

    @Override
    public BigDecimal readBigDecimal(Row resultSet, String name) {
        return resultSet.get(name, BigDecimal.class);
    }

    @Override
    public byte[] readBytes(Row resultSet, String name) {
        return resultSet.get(name, byte[].class);
    }

    @Nullable
    @Override
    public <T> T getRequiredValue(Row resultSet, String name, Class<T> type) throws DataAccessException {
        try {
            return resultSet.get(name, type);
        } catch (IllegalArgumentException | ConversionErrorException e) {
            try {
                return conversionService.convertRequired(resultSet.get(name), type);
            } catch (Exception exception) {
                throw exceptionForColumn(name, e);
            }
        }
    }

    @Override
    public boolean next(Row resultSet) {
        // not used
        return false;
    }

    private DataAccessException exceptionForColumn(String name, Exception e) {
        return new DataAccessException("Error reading object for name [" + name + "] from result set: " + e.getMessage(), e);
    }
}
