package net.spals.oembed4j.client;

import com.google.common.annotations.VisibleForTesting;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.spals.oembed4j.client.parser.OEmbedResponseParser;
import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link OEmbedClient} based
 * on a Jersey client.
 *
 * @author tkral
 * @author spags
 */
public class JerseyOEmbedClient implements OEmbedClient {

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
    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyOEmbedClient.class);
    private final Client client;
    private final OEmbedRegistry registry;
    private final ExpiringMap<OEmbedRequest, OEmbedResponse> responseCache;
    private final OEmbedResponseParser responseParser;

    @VisibleForTesting
    JerseyOEmbedClient(
        final Client client,
        final OEmbedRegistry registry,
        final ExpiringMap<OEmbedRequest, OEmbedResponse> responseCache,
        final OEmbedResponseParser responseParser
    ) {
        this.client = client;
        this.registry = registry;
        this.responseCache = responseCache;
        this.responseParser = responseParser;
    }

    public static JerseyOEmbedClient create(final OEmbedRegistry registry) {
        try {
            // 1. Configure the Jersey client
            // Setup SSL management
            // TODO: DO we need some real SSL management?
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null /*KeyManagers*/, new TrustManager[]{DEFAULT_TRUST_MANAGER}, new SecureRandom());

            final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            clientBuilder.sslContext(sslContext).hostnameVerifier((s, sslSession) -> true);
            clientBuilder.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE);

            // 2. Build response cache
            final ExpiringMap<OEmbedRequest, OEmbedResponse> responseCache = ExpiringMap.builder()
                    .expirationPolicy(ExpirationPolicy.CREATED)
                    .variableExpiration()
                    .build();
            // 3. Build response parser
            final OEmbedResponseParser responseParser = new OEmbedResponseParser();

            return new JerseyOEmbedClient(clientBuilder.build(), registry, responseCache, responseParser);
        } catch (final Exception e) {
            throw new RuntimeException(e);
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
        final Optional<OEmbedResponse> cachedResponse = Optional.ofNullable(responseCache.get(request));
        // If we got a cache hit, return immediately
        if (cachedResponse.isPresent()) {
            return cachedResponse;
        }

        // Otherwise, run the request and see if we can cache it
        final Optional<OEmbedResponse> response = executeSkipCache(request);
        response.filter(resp -> resp.getCacheAge().isPresent())
                .ifPresent(resp -> responseCache.put(request, resp, resp.getCacheAge().get(), TimeUnit.SECONDS));
        return response;
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

    @VisibleForTesting
    ExpiringMap<OEmbedRequest, OEmbedResponse> getResponseCache() {
        return responseCache;
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
                    LOGGER.info("failed to read entity", e);
                    // ignore the error
                    return Optional.empty();
                }
            case REDIRECTION:
                if (numberOfRedirects == 0) {
                    return runTarget(response.getLocation(), numberOfRedirects + 1);
                }
                LOGGER.info("too many redirects: " + numberOfRedirects);
                return Optional.empty();
            default:
                LOGGER.info("unsuccessful response: " + response.getStatusInfo());
                return Optional.empty();
        }
    }
}
