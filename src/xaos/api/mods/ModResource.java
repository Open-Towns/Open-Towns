package xaos.api.mods;

/**
 * A resource registered by a mod that can be released by the mod loader.
 *
 * Typical resources include event subscriptions and periodic behaviour
 * registrations.
 */
@FunctionalInterface
public interface ModResource extends AutoCloseable {

    /**
     * Releases this resource.
     *
     * Unlike AutoCloseable, mod resources do not throw checked exceptions.
     */
    @Override
    void close();
}
