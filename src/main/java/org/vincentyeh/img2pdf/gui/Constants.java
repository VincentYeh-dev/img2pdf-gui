package org.vincentyeh.img2pdf.gui;

/**
 * Global constants shared across the application.
 * <p>
 * This is a utility class and cannot be instantiated.
 * </p>
 */
public class Constants {

    /** Prevents instantiation of this utility class. */
    private Constants() {
    }

    /**
     * The application version read from the JAR manifest ({@code Implementation-Version})
     * at runtime. Falls back to {@code "dev"} when running outside a packaged JAR
     * (e.g. directly in the IDE).
     */
    public static final String APP_VERSION;

    /** The display title shown in the main window title bar, including the version. */
    public static final String APP_TITLE;

    static {
        String v = Constants.class.getPackage().getImplementationVersion();
        APP_VERSION = (v != null) ? v : "dev";
        APP_TITLE = "img2pdf-gui v" + APP_VERSION;
    }
}
