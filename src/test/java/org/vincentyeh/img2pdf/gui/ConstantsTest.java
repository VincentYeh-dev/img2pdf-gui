package org.vincentyeh.img2pdf.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that Constants.APP_VERSION and Constants.APP_TITLE are correctly
 * initialised at class-load time.
 *
 * <p>When running under {@code mvn test} (no packaged JAR),
 * {@code Class.getPackage().getImplementationVersion()} returns {@code null},
 * so the static initialiser must fall back to {@code "dev"}.
 * When running from the shaded JAR, the MANIFEST entry
 * {@code Implementation-Version} is populated by the ManifestResourceTransformer
 * and the real version string is returned instead.</p>
 */
class ConstantsTest {

    /**
     * White-box: APP_VERSION is initialised from the static block;
     * it must never be null or empty regardless of the runtime environment.
     */
    @Test
    void appVersionIsNotNullOrEmpty() {
        assertNotNull(Constants.APP_VERSION);
        assertFalse(Constants.APP_VERSION.isEmpty());
    }

    /**
     * White-box: when {@code getImplementationVersion()} returns {@code null}
     * (i.e. running in the IDE / mvn test without a packaged JAR),
     * APP_VERSION must fall back to {@code "dev"}.
     * When the method returns a non-null value (packaged JAR), APP_VERSION
     * must equal that value.
     */
    @Test
    void appVersionFallsBackToDevWhenNoManifest() {
        String fromPackage = Constants.class.getPackage().getImplementationVersion();
        if (fromPackage == null) {
            assertEquals("dev", Constants.APP_VERSION);
        } else {
            assertEquals(fromPackage, Constants.APP_VERSION);
        }
    }

    /**
     * White-box: APP_TITLE is built as {@code "img2pdf-gui v" + APP_VERSION}
     * in the static initialiser; verify both prefix and suffix.
     */
    @Test
    void appTitleHasExpectedFormat() {
        assertTrue(Constants.APP_TITLE.startsWith("img2pdf-gui v"),
                "APP_TITLE should start with 'img2pdf-gui v', but was: " + Constants.APP_TITLE);
        assertTrue(Constants.APP_TITLE.endsWith(Constants.APP_VERSION),
                "APP_TITLE should end with APP_VERSION");
    }
}
