package io.github.mickablondo.aturkey.exception;

/**
 * Indicates that the incoming entity has a null value for its business key.
 */
public class AturKeyNullValueException extends AturKeyException {
    /**
     * Creates a failure for a null business-key value.
     *
     * @param entityClassName fully qualified entity class name
     * @param fieldName business-key field name
     */
    public AturKeyNullValueException(String entityClassName, String fieldName) {
        super("AturKey field '" + fieldName + "' on " + entityClassName + " must not be null.");
    }
}
