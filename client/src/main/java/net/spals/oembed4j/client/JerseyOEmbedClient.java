package net.spals.oembed4j.client;

import com.google.common.annotations.VisibleForTesting;
import net.spals.oembed4j.client.parser.OEmbedResponseParser;
import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * An implementation of {@link OEmbedClient} based
 * on a Jersey client.
 *
 * @author tkral
 */
public final class JerseyOEmbedClient implements OEmbedClient {

    public static JerseyOEmbedClient create(final OEmbedRegistry registry) {
        return new JerseyOEmbedClient(ClientBuilder.newClient(), registry, new OEmbedResponseParser());
    }

    private final Client client;
    private final OEmbedRegistry registry;
    private final OEmbedResponseParser responseParser;

    @VisibleForTesting
    JerseyOEmbedClient(final Client client,
                       final OEmbedRegistry registry,
                       final OEmbedResponseParser responseParser) {
        this.client = client;
        this.registry = registry;
        this.responseParser = responseParser;
    }

    @Override
    public Optional<OEmbedResponse> execute(final OEmbedRequest request) {
        return registry.getEndpoint(request.getResourceURI())
                .flatMap(endpoint -> execute(request, endpoint));
    }

    @Override
    public Optional<OEmbedResponse> execute(OEmbedRequest request, OEmbedEndpoint endpoint) {
        final Response response = client.target(request.toURI(endpoint))
                .request(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_XML_TYPE)
                .get();

        switch (response.getStatusInfo().getFamily()) {
            case SUCCESSFUL:
                final InputStream inputStream = response.readEntity(InputStream.class);
                try {
                    return responseParser.parse(inputStream, response.getHeaderString(HttpHeaders.CONTENT_TYPE));
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) { /*ignored*/ }
                }
            default:
                return Optional.empty();
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
