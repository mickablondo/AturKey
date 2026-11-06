package io.github.mickablondo.aturkey;

import io.github.mickablondo.aturkey.exception.AturKeyConfigurationException;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Cached reflection metadata for an entity's single {@link AturKey} field.
 */
public final class AturKeyMetadata {
    private static final ClassValue<AturKeyMetadata> CACHE = new ClassValue<>() {
        @Override
        protected AturKeyMetadata computeValue(Class<?> type) {
            return inspect(type);
        }
    };

    private final Class<?> entityClass;
    private final Field keyField;
    private final boolean ignoreNullLikeAtur;

    private AturKeyMetadata(Class<?> entityClass, Field keyField, boolean ignoreNullLikeAtur) {
        this.entityClass = entityClass;
        this.keyField = keyField;
        this.ignoreNullLikeAtur = ignoreNullLikeAtur;
    }

    /**
     * Finds the concrete JPA entity class, including when {@code instance} is a provider proxy.
     *
     * @param instance entity or proxy instance
     * @return the class annotated with {@link Entity}
     */
    public static Class<?> entityClass(Object instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }

        for (Class<?> type = instance.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            if (type.isAnnotationPresent(Entity.class)) {
                return type;
            }
        }
        throw new AturKeyConfigurationException(
                "Class " + instance.getClass().getName() + " is not a JPA entity or an entity proxy.");
    }

    /**
     * Returns cached metadata for the entity represented by {@code instance}.
     *
     * @param instance entity or proxy instance
     * @return the entity metadata
     */
    public static AturKeyMetadata forInstance(Object instance) {
        return CACHE.get(entityClass(instance));
    }

    /**
     * @return the concrete JPA entity class
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * @return the annotated business-key field
     */
    public Field getKeyField() {
        return keyField;
    }

    /**
     * @return true if incoming null values should be ignored during updates
     */
    public boolean isIgnoreNullLikeAtur() {
        return ignoreNullLikeAtur;
    }

    /**
     * Reads this entity's business-key value.
     *
     * @param instance entity or proxy instance
     * @return key value
     */
    public Object readKey(Object instance) {
        try {
            if (!keyField.canAccess(instance) && !keyField.trySetAccessible()) {
                throw new AturKeyConfigurationException(
                        "AturKey field '" + keyField.getName() + "' on " + entityClass.getName() + " is not accessible.");
            }
            return keyField.get(instance);
        } catch (IllegalAccessException exception) {
            throw new AturKeyConfigurationException(
                    "Cannot read AturKey field '" + keyField.getName() + "' on " + entityClass.getName() + ".", exception);
        }
    }

    /**
     * Inspects the entity class and finds the single {@link AturKey} field.
     * @param entityClass the entity class to inspect
     * @return the metadata for the entity class
     */
    private static AturKeyMetadata inspect(Class<?> entityClass) {
        List<Field> keyFields = new ArrayList<>();
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.isAnnotationPresent(AturKey.class)) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())
                        || field.isAnnotationPresent(Transient.class)) {
                    throw new AturKeyConfigurationException(
                            "AturKey field '" + field.getName() + "' on " + entityClass.getName()
                                    + " must be a persistent instance field.");
                }
                keyFields.add(field);
            }
        }

        if (keyFields.size() != 1) {
            throw new AturKeyConfigurationException(
                    "Entity " + entityClass.getName() + " must declare exactly one @AturKey field; found "
                            + keyFields.size() + ".");
        }

        Field keyField = keyFields.getFirst();
        return new AturKeyMetadata(
                entityClass, keyField, keyField.getAnnotation(AturKey.class).ignoreNullLikeAtur());
    }
}
