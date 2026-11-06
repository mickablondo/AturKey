package io.github.mickablondo.aturkey.exception;

/**
 * Indicates an invalid AturKey entity configuration.
 */
public class AturKeyConfigurationException extends AturKeyException {
    /**
     * Creates a configuration failure with a message.
     *
     * @param message configuration problem
     */
    public AturKeyConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a configuration failure with a message and cause.
     *
     * @param message configuration problem
     * @param cause underlying cause
     */
    public AturKeyConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
