package io.github.mickablondo.aturkey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the entity field used to find an existing entity for an update.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AturKey {
    /**
     * Whether null values from the incoming entity are ignored like AturKey's default update behavior.
     *
     * @return true to preserve existing values for incoming null fields
     */
    boolean ignoreNullLikeAtur() default false;
}
