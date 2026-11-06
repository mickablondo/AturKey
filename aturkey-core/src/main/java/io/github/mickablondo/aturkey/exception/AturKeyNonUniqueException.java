package io.github.mickablondo.aturkey.exception;

/**
 * Indicates that more than one entity matched the supplied business key.
 */
public class AturKeyNonUniqueException extends AturKeyException {
    private final String entityClassName;
    private final String fieldName;
    private final Object keyValue;

    /**
     * Creates a duplicate-match failure.
     *
     * @param entityClassName fully qualified entity class name
     * @param fieldName business-key field name
     * @param keyValue duplicate key value
     */
    public AturKeyNonUniqueException(String entityClassName, String fieldName, Object keyValue) {
        super("Multiple " + entityClassName + " entities match AturKey field '" + fieldName
                + "' with value '" + keyValue + "'.");
        this.entityClassName = entityClassName;
        this.fieldName = fieldName;
        this.keyValue = keyValue;
    }

    /**
     * @return fully qualified name of the entity class
     */
    public String getEntityClassName() {
        return entityClassName;
    }

    /**
     * @return name of the field used as the business key
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return value that matched more than one entity
     */
    public Object getKeyValue() {
        return keyValue;
    }
}
