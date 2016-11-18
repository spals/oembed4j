package net.spals.oembed4j.client.registry;

import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedProvider;

import java.net.URI;
import java.util.Optional;

/**
 * An interface contract which defines a static lookup registry
 * for {@link OEmbedProvider}s and {@link OEmbedEndpoint}s.
 *
 * @author tkral
 */
public interface OEmbedRegistry {

    /**
     * Attempt to find an {@link OEmbedProvider} by name.
     */
    Optional<OEmbedProvider> getProvider(String name);

    /**
     * Attempt to find an {@link OEmbedEndpoint} which
     * matches the given resource uri.
     */
    Optional<OEmbedEndpoint> getEndpoint(URI resourceURI);

    /**
     * @return The number of providers registered
     */
    int numProviders();
}
