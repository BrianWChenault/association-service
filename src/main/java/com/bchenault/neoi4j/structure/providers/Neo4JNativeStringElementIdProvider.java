package com.bchenault.neoi4j.structure.providers;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import org.neo4j.driver.types.Entity;

import java.util.Objects;

public class Neo4JNativeStringElementIdProvider implements Neo4JElementIdProvider<String> {
    @Override
    public String generate() {
        return null;
    }

    @Override
    public String fieldName() {
        return null;
    }

    @Override
    public String get(Entity entity) {
        Objects.requireNonNull(entity, "entity cannot be null");
        return String.valueOf(entity.id());
    }

    @Override
    public String processIdentifier(Object id) {
        Objects.requireNonNull(id, "Element identifier cannot be null");
        // check for Long
        if (id instanceof Long)
            return String.valueOf(id);
        // check for numeric types
        if (id instanceof Number)
            return String.valueOf(((Number) id).longValue());
        // check for string
        if (id instanceof String)
            return (String)id;
        // error
        throw new IllegalArgumentException(String.format("Expected an id that is convertible to Long but received %s", id.getClass()));
    }

    @Override
    public String matchPredicateOperand(String alias) {
        Objects.requireNonNull(alias, "alias cannot be null");
        return "ID(" + alias + ")";
    }
}
