package xaos.api.definitions;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable identifier used by the public modding API.
 *
 * <p>Qualified IDs use the form {@code namespace:path}. For compatibility with
 * Towns' existing XML content, an unqualified ID is interpreted as belonging
 * to the {@code towns} namespace.</p>
 */
public final class DefinitionId {

    public static final String DEFAULT_NAMESPACE = "towns";

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");

    private final String namespace;
    private final String path;

    private DefinitionId(String namespace, String path) {
        this.namespace = validateNamespace(namespace);
        this.path = validatePath(path);
    }

    public static DefinitionId of(String namespace, String path) {
        return new DefinitionId(namespace, path);
    }

    public static DefinitionId towns(String path) {
        return new DefinitionId(DEFAULT_NAMESPACE, path);
    }

    public static DefinitionId parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Definition ID cannot be null");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Definition ID cannot be empty");
        }

        int separator = trimmed.indexOf(':');
        if (separator < 0) {
            return towns(trimmed);
        }

        if (separator == 0 || separator == trimmed.length() - 1 || trimmed.indexOf(':', separator + 1) >= 0) {
            throw new IllegalArgumentException("Invalid definition ID: " + value);
        }

        return of(trimmed.substring(0, separator), trimmed.substring(separator + 1));
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    public boolean isDefaultNamespace() {
        return DEFAULT_NAMESPACE.equals(namespace);
    }

    private static String validateNamespace(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("Definition namespace cannot be null");
        }

        String normalized = namespace.trim().toLowerCase();
        if (!NAMESPACE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid definition namespace: " + namespace);
        }
        return normalized;
    }

    private static String validatePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Definition path cannot be null");
        }

        String trimmed = path.trim();
        if (trimmed.isEmpty() || trimmed.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Invalid definition path: " + path);
        }
        return trimmed;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DefinitionId other)) {
            return false;
        }
        return namespace.equals(other.namespace) && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
