package xaos.api.mods;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import xaos.api.SdkVersion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaModLoaderMetadataTest {

    @Test
    void requiredPropertyReturnsTrimmedValue() {

        Properties properties =
                new Properties();

        properties.setProperty(
                "id",
                "  example-mod  "
        );

        assertEquals(
                "example-mod",
                JavaModLoader.requiredProperty(
                        properties,
                        "id"
                )
        );
    }

    @Test
    void missingRequiredPropertyIsRejected() {

        Properties properties =
                new Properties();

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.requiredProperty(
                        properties,
                        "id"
                )
        );
    }

    @Test
    void invalidIntegerPropertyIsRejected() {

        Properties properties =
                new Properties();

        properties.setProperty(
                "apiVersion",
                "not-a-number"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.requiredIntProperty(
                        properties,
                        "apiVersion"
                )
        );
    }

    @Test
    void currentApiVersionIsAccepted() {

        assertDoesNotThrow(
                () -> JavaModLoader.validateApiVersion(
                        SdkVersion.API_VERSION
                )
        );
    }

    @Test
    void newerApiVersionIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.validateApiVersion(
                        SdkVersion.API_VERSION + 1
                )
        );
    }

    @Test
    void unsupportedOldApiVersionIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.validateApiVersion(
                        SdkVersion.MIN_SUPPORTED_API_VERSION - 1
                )
        );
    }

    @Test
    void dependenciesAreTrimmedAndDeduplicated() {

        Properties properties =
                new Properties();

        properties.setProperty(
                "dependencies",
                " strip-mine, lava-logistics, strip-mine "
        );

        assertEquals(
                List.of(
                        "strip-mine",
                        "lava-logistics"
                ),
                JavaModLoader.parseDependencies(
                        properties
                )
        );
    }

    @Test
    void missingDependenciesPropertyReturnsEmptyList() {

        Properties properties =
                new Properties();

        assertEquals(
                List.of(),
                JavaModLoader.parseDependencies(
                        properties
                )
        );
    }

    @Test
    void selfDependencyIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.validateDependencies(
                        "example-mod",
                        List.of(
                                "example-mod"
                        )
                )
        );
    }

    @Test
    void normalDependenciesAreAccepted() {

        assertDoesNotThrow(
                () -> JavaModLoader.validateDependencies(
                        "example-mod",
                        List.of(
                                "library-mod",
                                "content-mod"
                        )
                )
        );
    }

    @Test
    void validModIdIsAccepted() {

        assertDoesNotThrow(
                () -> JavaModLoader.validateModId(
                        "example-mod"
                )
        );
    }

    @Test
    void uppercaseModIdIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.validateModId(
                        "Example-Mod"
                )
        );
    }

    @Test
    void spacesInModIdAreRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> JavaModLoader.validateModId(
                        "example mod"
                )
        );
    }
}