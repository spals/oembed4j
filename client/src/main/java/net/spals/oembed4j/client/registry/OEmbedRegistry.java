package net.spals.oembed4j.client.registry;

import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedProvider;

import java.net.URI;
import java.util.Optional;

/**
 * A service contract which defines a static lookup registry
 * for {@link OEmbedProvider}s and {@link OEmbedEndpoint}s.
 *
 * @author tkral
 */
public interface OEmbedRegistry {

    /**
     * Attempt to find an {@link OEmbedEndpoint} which
     * matches the given resource {@link URI}.
     *
     * @param resourceURI A resource {@link URI} for which a
     *                    matching {@link OEmbedEndpoint} is to be found
     * @return The {@link OEmbedEndpoint} matching the resource {@link URI}, if possible.
     *         Otherwise, {@code Optional.empty()} if not such match exists.
     */
    Optional<OEmbedEndpoint> getEndpoint(URI resourceURI);

    /**
     * Attempt to find an {@link OEmbedProvider} which matches the given name.
     * Matching is case-insensitive.
     *
     * @param name The name of the provider that is to be found
     * @return The {@link OEmbedProvider} matching the name, if possible.
     *         Otherwise, {@code Optional.empty()} if no such match exists.
     */
    Optional<OEmbedProvider> getProvider(String name);

    /**
     * Returns the number of {@link OEmbedProvider}s registered.
     *
     * @return The number of {@link OEmbedProvider}s registered
     */
    int numProviders();
}
