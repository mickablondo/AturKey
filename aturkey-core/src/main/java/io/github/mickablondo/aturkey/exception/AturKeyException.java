package io.github.mickablondo.aturkey.exception;

/**
 * Quand le Atur n'est pas content !
 */
public class AturKeyException extends RuntimeException {
    /**
     * Creates an AturKey failure with a message.
     *
     * @param message failure detail
     */
    public AturKeyException(String message) {
        super(message);
    }

    /**
     * Creates an AturKey failure with a message and cause.
     *
     * @param message failure detail
     * @param cause underlying cause
     */
    public AturKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
