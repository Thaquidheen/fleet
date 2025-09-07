package com.fleetmanagement.userservice.config;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Custom Hibernate type for PostgreSQL enums
 * Handles the casting issue between Java enums and PostgreSQL custom enum types
 */
public class PostgreSQLEnumType extends EnumType<Enum<?>> {

    @Override
    public JdbcType getJdbcType(JdbcTypeIndicators context) {
        return new PostgreSQLEnumJdbcType();
    }

    private static class PostgreSQLEnumJdbcType implements JdbcType {
        @Override
        public int getJdbcTypeCode() {
            return Types.OTHER;
        }

        @Override
        public void bind(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
                throws SQLException {
            if (value == null) {
                st.setNull(index, Types.OTHER);
            } else {
                st.setObject(index, value.toString(), Types.OTHER);
            }
        }
    }
}