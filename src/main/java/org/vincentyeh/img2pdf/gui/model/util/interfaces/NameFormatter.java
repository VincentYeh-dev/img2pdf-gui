package org.vincentyeh.img2pdf.gui.model.util.interfaces;

/**
 * Abstract base class for pattern-based name formatters.
 * <p>
 * Subclasses receive a pattern string containing placeholder tokens and
 * implement {@link #format(Object)} to substitute those tokens with values
 * derived from the provided data object.
 * </p>
 *
 * @param <D> the type of data used to resolve the pattern tokens
 */
public abstract class NameFormatter<D> {

    /** The pattern string containing placeholder tokens to be replaced. */
    protected final String pattern;

    /**
     * Constructs a formatter with the given pattern.
     *
     * @param pattern the token pattern; must not be {@code null}
     * @throws IllegalArgumentException if {@code pattern} is {@code null}
     */
    public NameFormatter(String pattern) {
        this.pattern = pattern;
        if (pattern==null)
            throw new IllegalArgumentException("data==null");
    }

    /**
     * Formats the given data by resolving all pattern tokens.
     *
     * @param data the data object used to resolve token values
     * @return the formatted string with all tokens replaced
     * @throws FormatException if a token cannot be resolved or the pattern is invalid
     */
    public abstract String format(D data) throws FormatException;

    /**
     * Checked exception thrown when {@link NameFormatter#format(Object)} fails to
     * produce a valid result due to an unresolvable pattern token or other error.
     */
    public static class FormatException extends Exception{

        /** Creates a {@code FormatException} with no detail message. */
        public FormatException() {
        }

        /**
         * Creates a {@code FormatException} with the given detail message.
         *
         * @param message the detail message
         */
        public FormatException(String message) {
            super(message);
        }

        /**
         * Creates a {@code FormatException} with the given detail message and cause.
         *
         * @param message the detail message
         * @param cause   the underlying cause
         */
        public FormatException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a {@code FormatException} wrapping the given cause.
         *
         * @param cause the underlying cause
         */
        public FormatException(Throwable cause) {
            super(cause);
        }

        /**
         * Creates a {@code FormatException} with full control over suppression and
         * stack-trace writability.
         *
         * @param message            the detail message
         * @param cause              the underlying cause
         * @param enableSuppression  whether suppression is enabled
         * @param writableStackTrace whether the stack trace is writable
         */
        public FormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
