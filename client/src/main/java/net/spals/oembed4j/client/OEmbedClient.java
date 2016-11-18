package net.spals.oembed4j.client;

import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;

import java.util.Optional;

/**
 * @author tkral
 */
public interface OEmbedClient extends AutoCloseable {

    Optional<OEmbedResponse> execute(OEmbedRequest request);

    Optional<OEmbedResponse> execute(OEmbedRequest request, OEmbedEndpoint endpoint);
}
