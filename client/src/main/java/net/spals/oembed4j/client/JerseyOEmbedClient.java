package net.spals.oembed4j.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import net.spals.oembed4j.client.cache.OEmbedResponseCache;
import net.spals.oembed4j.client.parser.OEmbedResponseParser;
import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link OEmbedClient} based
 * on a Jersey client.
 *
 * @author tkral
 */
public final class JerseyOEmbedClient implements OEmbedClient {

    private static final X509TrustManager DEFAULT_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {
        }
    };
    private static final Logger LOGGER = Logger.getLogger(JerseyOEmbedClient.class.getName());
    private final Client client;
    private final OEmbedRegistry registry;
    private final OEmbedResponseCache responseCache;
    private final OEmbedResponseParser responseParser;

    @VisibleForTesting
    JerseyOEmbedClient(
        final Client client,
        final OEmbedRegistry registry
    ) {
        this.client = client;
        this.registry = registry;
        this.responseCache = OEmbedResponseCache.create(this);
        this.responseParser = new OEmbedResponseParser();
    }

    @VisibleForTesting
    JerseyOEmbedClient(
        final Client client,
        final OEmbedRegistry registry,
        final OEmbedResponseCache responseCache,
        final OEmbedResponseParser responseParser
    ) {
        this.client = client;
        this.registry = registry;
        this.responseCache = responseCache;
        this.responseParser = responseParser;
    }

    public static JerseyOEmbedClient create(final OEmbedRegistry registry) {
        try {
            // Configure the Jersey client
            // Setup SSL management
            // TODO: DO we need some real SSL management?
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null /*KeyManagers*/, new TrustManager[]{DEFAULT_TRUST_MANAGER}, new SecureRandom());

            final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            clientBuilder.sslContext(sslContext).hostnameVerifier((s, sslSession) -> true);
            clientBuilder.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE);

            return new JerseyOEmbedClient(clientBuilder.build(), registry);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * @see OEmbedClient#close()
     */
    @Override
    public void close() {
        client.close();
    }

    /**
     * @see OEmbedClient#execute(OEmbedRequest)
     */
    @Override
    public Optional<OEmbedResponse> execute(final OEmbedRequest request) {
        return responseCache.get(request.getResourceURI());
    }

    /**
     * @see OEmbedClient#executeSkipCache(OEmbedRequest)
     */
    @Override
    public Optional<OEmbedResponse> executeSkipCache(final OEmbedRequest request) {
        return registry.getEndpoint(request.getResourceURI())
            .flatMap(endpoint -> executeSkipCache(request, endpoint));
    }

    /**
     * @see OEmbedClient#executeSkipCache(OEmbedRequest, OEmbedEndpoint)
     */
    @Override
    public Optional<OEmbedResponse> executeSkipCache(final OEmbedRequest request, final OEmbedEndpoint endpoint) {
        return runTarget(request.toURI(endpoint), 0);
    }

    private Optional<OEmbedResponse> runTarget(final URI uri, final int numberOfRedirects) {
        final Response response = client.target(uri)
            .request(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_XML_TYPE)
            .get();

        switch (response.getStatusInfo().getFamily()) {
            case SUCCESSFUL:
                try {
                    try (final InputStream inputStream = response.readEntity(InputStream.class)) {
                        return responseParser.parse(inputStream, response.getHeaderString(HttpHeaders.CONTENT_TYPE));
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.INFO, "failed to read entity", e);
                    // ignore the error
                    return Optional.empty();
                }
            case REDIRECTION:
                if (numberOfRedirects == 0) {
                    return runTarget(response.getLocation(), numberOfRedirects + 1);
                }
                LOGGER.log(Level.INFO, "too many redirects: " + numberOfRedirects);
                return Optional.empty();
            default:
                LOGGER.log(Level.INFO, "unsuccessful response: " + response.getStatusInfo());
                return Optional.empty();
        }
    }
}
