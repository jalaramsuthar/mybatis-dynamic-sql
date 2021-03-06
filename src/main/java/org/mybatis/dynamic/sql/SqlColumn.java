/**
 *    Copyright 2016-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql;

import java.sql.JDBCType;
import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.TableAliasCalculator;

public class SqlColumn<T> implements BindableColumn<T>, SortSpecification {
    
    protected String name;
    protected SqlTable table;
    protected JDBCType jdbcType;
    protected boolean isDescending = false;
    protected Optional<String> alias = Optional.empty();
    protected Optional<String> typeHandler;
    
    private SqlColumn(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        jdbcType = Objects.requireNonNull(builder.jdbcType);
        table = Objects.requireNonNull(builder.table);
        typeHandler = Optional.ofNullable(builder.typeHandler);
    }
    
    protected SqlColumn(SqlColumn<?> sqlColumn) {
        name = sqlColumn.name;
        table = sqlColumn.table;
        jdbcType = sqlColumn.jdbcType;
        isDescending = sqlColumn.isDescending;
        alias = sqlColumn.alias;
        typeHandler = sqlColumn.typeHandler;
    }
    
    public String name() {
        return name;
    }
    
    @Override
    public JDBCType jdbcType() {
        return jdbcType;
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }
    
    @Override
    public Optional<String> typeHandler() {
        return typeHandler;
    }
    
    @Override
    public SortSpecification descending() {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.isDescending = true;
        return column;
    }
    
    @Override
    public SqlColumn<T> as(String alias) {
        SqlColumn<T> column = new SqlColumn<>(this);
        column.alias = Optional.of(alias);
        return column;
    }
    
    @Override
    public boolean isDescending() {
        return isDescending;
    }
    
    @Override
    public String aliasOrName() {
        return alias.orElse(name);
    }
    
    @Override
    public String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        return tableAliasCalculator.aliasForColumn(table)
                .map(this::applyTableAlias)
                .orElseGet(this::name);
    }
    
    public <S> SqlColumn<S> withTypeHandler(String typeHandler) {
        SqlColumn<S> column = new SqlColumn<>(this);
        column.typeHandler = Optional.of(typeHandler);
        return column;
    }

    private String applyTableAlias(String tableAlias) {
        return tableAlias + "." + name(); //$NON-NLS-1$
    }
    
    public static <T> SqlColumn<T> of(String name, SqlTable table, JDBCType jdbcType) {
        return SqlColumn.withName(name)
                .withTable(table)
                .withJdbcType(jdbcType)
                .build();
    }
    
    public static Builder withName(String name) {
        return new Builder().withName(name);
    }
    
    public static class Builder {
        private SqlTable table;
        private String name;
        private JDBCType jdbcType;
        private String typeHandler;
        
        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withJdbcType(JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }
        
        public Builder withTypeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }
        
        public <T> SqlColumn<T> build() {
            return new SqlColumn<>(this);
        }
    }
}
