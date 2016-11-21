package net.spals.oembed4j.client;

import net.spals.oembed4j.client.cache.OEmbedResponseCache;
import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;

import java.util.Optional;

/**
 * A service contract which defines an oEmbed client
 * to execute {@link OEmbedRequest}s and return
 * {@link OEmbedResponse}s.
 *
 * @author tkral
 */
public interface OEmbedClient extends AutoCloseable {

    /**
     * @see AutoCloseable#close()
     */
    void close();

    /**
     * Finds a matching {@link OEmbedEndpoint} for the given
     * {@link OEmbedRequest} and executes the request against it.
     *
     * This method will check the {@link OEmbedResponseCache}
     * for a result before executing the request.
     *
     * @param request A {@link OEmbedRequest} which represents
     *                the resource for which we wish to get oEmbed
     *                information
     * @return An {@link OEmbedResponse}, if possible.
     *         Otherwise, {@code Optional.empty()}, if the request cannot
     *         be successfully executed.
     */
    Optional<OEmbedResponse> execute(OEmbedRequest request);

    /**
     * Finds a matching {@link OEmbedEndpoint} for the given
     * {@link OEmbedRequest} and executes the request against it.
     *
     * This method will *never* check the {@link OEmbedResponseCache}
     * for a result before executing the request.
     *
     * @param request A {@link OEmbedRequest} which represents
     *                the resource for which we wish to get oEmbed
     *                information
     * @return An {@link OEmbedResponse}, if possible.
     *         Otherwise, {@code Optional.empty()}, if the request cannot
     *         be successfully executed.
     */
    Optional<OEmbedResponse> executeSkipCache(OEmbedRequest request);

    /**
     * Executes the given {@link OEmbedRequest} against the given
     * {@link OEmbedEndpoint}.
     *
     * This method will *never* check the {@link OEmbedResponseCache}
     * for a result before executing the request.
     *
     * @param request A {@link OEmbedRequest} which represents
     *                the resource for which we wish to get oEmbed
     *                information
     * @param endpoint The {@link OEmbedEndpoint} of the oEmbed
     *                 provider.
     * @return An {@link OEmbedResponse}, if possible.
     *         Otherwise, {@code Optional.empty()}, if the request cannot
     *         be successfully executed.
     */
    Optional<OEmbedResponse> executeSkipCache(OEmbedRequest request, OEmbedEndpoint endpoint);
}
