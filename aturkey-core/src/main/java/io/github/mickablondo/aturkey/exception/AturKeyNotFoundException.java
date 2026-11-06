package io.github.mickablondo.aturkey.exception;

/**
 * Indicates that no entity matched the supplied business key.
 */
public class AturKeyNotFoundException extends AturKeyException {
    /**
     * Creates a not-found failure with a message.
     *
     * @param message lookup details
     */
    public AturKeyNotFoundException(String message) {
        super(message);
    }
}
