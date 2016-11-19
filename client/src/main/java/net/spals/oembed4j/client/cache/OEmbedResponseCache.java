package net.spals.oembed4j.client.cache;

import com.google.common.annotations.VisibleForTesting;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringEntryLoader;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpiringValue;
import net.spals.oembed4j.client.OEmbedClient;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A cache to store {@link OEmbedResponse}s based
 * on their cache_age property.
 *
 * @author tkral
 */
public class OEmbedResponseCache {

    public static OEmbedResponseCache create(final OEmbedClient oEmbedClient) {
        final ExpiringMap<URI, Optional<OEmbedResponse>> cacheDelegate = ExpiringMap.builder()
                // Always time cache expiration from creation time
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expiringEntryLoader(new OEmbedResponseExpiringEntryLoader(oEmbedClient))
                .build();

        return new OEmbedResponseCache(cacheDelegate);
    }

    private final ExpiringMap<URI, Optional<OEmbedResponse>> cacheDelegate;

    @VisibleForTesting
    OEmbedResponseCache(final ExpiringMap<URI, Optional<OEmbedResponse>> cacheDelegate) {
        this.cacheDelegate = cacheDelegate;
    }

    /**
     * Check the cache for the given resource {@link URI}.
     * If the cache does not contain it, then it will attempt
     * an automatic load.
     *
     * @param resourceURI
     * @return
     */
    public Optional<OEmbedResponse> get(final URI resourceURI) {
        return cacheDelegate.get(resourceURI);
    }

    static class OEmbedResponseExpiringEntryLoader implements ExpiringEntryLoader<URI, Optional<OEmbedResponse>> {

        private final OEmbedClient oEmbedClient;

        OEmbedResponseExpiringEntryLoader(final OEmbedClient oEmbedClient) {
            this.oEmbedClient = oEmbedClient;
        }

        @Override
        public ExpiringValue<Optional<OEmbedResponse>> load(final URI resourceURI) {
            final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI(resourceURI).build();
            final Optional<OEmbedResponse> response = oEmbedClient.executeSkipCache(request);

            // If a response was given *and* the cache_age property was set,
            // then add the response to the cache with the appropriate expiration
            // (which is in seconds according to the oEmbed spec)
            return response.filter(resp -> resp.getCacheAge().isPresent())
                    .map(resp -> new ExpiringValue(response, resp.getCacheAge().get(), TimeUnit.SECONDS))
                    // Otherwise, return the response with an immediate expiration
                    .orElseGet(() -> new ExpiringValue(response, 0L , TimeUnit.NANOSECONDS));
        }
    }
}
