package io.github.mickablondo.aturkey;

import io.github.mickablondo.aturkey.exception.AturKeyConfigurationException;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Finds and updates a managed entity by its {@link AturKey} field.
 */
public final class AturKeyUpdater {
    private final AturKeyResolver resolver;

    /**
     * Creates an updater using the supplied persistence context.
     *
     * @param entityManager persistence context; the caller must run this operation in a transaction
     */
    public AturKeyUpdater(EntityManager entityManager) {
        this.resolver = new AturKeyResolver(Objects.requireNonNull(entityManager, "entityManager must not be null"));
    }

    /**
     * Copies the incoming entity's persistent values onto the existing managed entity.
     * Identifier and version fields are never copied.
     *
     * @param incoming entity carrying the new values and business key
     * @param <T> entity type
     * @return the updated managed entity
     */
    public <T> T update(T incoming) {
        AturKeyMetadata metadata = AturKeyMetadata.forInstance(incoming);
        T managed = resolver.resolve(incoming);
        copyPersistentFields(incoming, managed, metadata.isIgnoreNullLikeAtur());
        return managed;
    }

    //region PRIVATE METHODS
    private void copyPersistentFields(Object source, Object target, boolean ignoreNull) {
        Class<?> entityClass = AturKeyMetadata.entityClass(source);
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field sourceField : type.getDeclaredFields()) {
                if (shouldSkip(sourceField, entityClass)) {
                    continue;
                }
                Field targetField = findField(target.getClass(), sourceField.getName());
                if (targetField == null || shouldSkip(targetField, entityClass)) {
                    continue;
                }
                copyField(sourceField, targetField, source, target, ignoreNull, entityClass);
            }
        }
    }

    private static boolean shouldSkip(Field field, Class<?> entityClass) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers)
                || Modifier.isTransient(modifiers)
                || hasAnnotation(field, entityClass, Transient.class)
                || hasAnnotation(field, entityClass, Id.class)
                || hasAnnotation(field, entityClass, EmbeddedId.class)
                || hasAnnotation(field, entityClass, Version.class);
    }

    private static boolean hasAnnotation(
            Field field, Class<?> entityClass, Class<? extends Annotation> annotationType) {
        if (field.isAnnotationPresent(annotationType)) {
            return true;
        }

        String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        for (Class<?> type = entityClass; type != null && type != Object.class;
             type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                String name = method.getName();
                boolean getter = method.getParameterCount() == 0
                        && (name.equals("get" + suffix) || name.equals("is" + suffix));
                if (getter && method.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Continue through the entity hierarchy.
            }
        }
        return null;
    }

    private static void copyField(
            Field sourceField, Field targetField, Object source, Object target, boolean ignoreNull, Class<?> entityClass) {
        try {
            if ((!sourceField.canAccess(source) && !sourceField.trySetAccessible())
                    || (!targetField.canAccess(target) && !targetField.trySetAccessible())) {
                throw new AturKeyConfigurationException(
                        "Persistent field '" + sourceField.getName() + "' on " + entityClass.getName()
                                + " is not accessible.");
            }
            Object value = sourceField.get(source);
            if (!ignoreNull || value != null) {
                targetField.set(target, value);
            }
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            throw new AturKeyConfigurationException(
                    "Cannot copy persistent field '" + sourceField.getName() + "' on " + entityClass.getName() + ".",
                    exception);
        }
    }
    //endregion
}
