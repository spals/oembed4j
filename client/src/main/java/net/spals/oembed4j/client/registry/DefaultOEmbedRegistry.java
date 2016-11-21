package net.spals.oembed4j.client.registry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.net.InternetDomainName;
import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link OEmbedRegistry}.
 *
 * To create a registry dynamically from the official oEmbed
 * provider list, you can use the following:
 *
 * {@code DefaultOEmbedRegistry.loadFromURI(DEFAULT_OEMBED_PROVIDER_URI)}
 *
 * Otherwise, you can use an alternative provider list uri
 * or local file with the appropriate static initializer.
 *
 * NOTE: Any provider list is assumed to be in JSON format.
 *
 * @author tkral
 */
public final class DefaultOEmbedRegistry implements OEmbedRegistry {

    public static final URI DEFAULT_OEMBED_PROVIDER_URI = URI.create("http://oembed.com/providers.json");
    private static final ObjectMapper mapper = new ObjectMapper();


    private final Map<String, OEmbedProvider> providersByName;
    private final Map<InternetDomainName, OEmbedProvider> providersByDomain;

    public static DefaultOEmbedRegistry loadFromFile(final File file) {
        try {
            final List<OEmbedProvider> providerList = mapper.readValue(file,
                    new TypeReference<List<OEmbedProvider>>() {});
            return new DefaultOEmbedRegistry(providerList);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static DefaultOEmbedRegistry loadFromURI(final URI uri) {
        try {
            final List<OEmbedProvider> providerList = mapper.readValue(uri.toURL(),
                    new TypeReference<List<OEmbedProvider>>() {});
            return new DefaultOEmbedRegistry(providerList);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    DefaultOEmbedRegistry(final List<OEmbedProvider> providerList) {
        this.providersByName = Collections.unmodifiableMap(providerList.stream()
                .collect(Collectors.toMap(OEmbedProvider::getName, Function.identity())));
        this.providersByDomain = Collections.unmodifiableMap(providerList.stream()
                .collect(Collectors.toMap(OEmbedProvider::getDomain, Function.identity())));
    }

    /**
     * @see OEmbedRegistry#getEndpoint(URI)
     */
    @Override
    public Optional<OEmbedEndpoint> getEndpoint(final URI resourceURI) {
        final InternetDomainName resourceDomain = InternetDomainName.from(resourceURI.getHost());
        // Attempt to short-circuit the endpoint lookup by seeing if we can find a matching
        // provider by the domain. This will cover a lot of cases, but not every case.
        final Optional<OEmbedProvider> providerByDomain = Optional.ofNullable(providersByDomain.get(resourceDomain));
        final Optional<OEmbedEndpoint> endpointByDomain = providerByDomain.map(OEmbedProvider::getEndpoints)
                .flatMap(endpoints -> endpoints.stream().filter(endpoint -> endpoint.matchesResourceURI(resourceURI)).findAny());
        if (endpointByDomain.isPresent()) {
            return endpointByDomain;
        }

        // Fallback to a linear search through all registered endpoints
        return providersByName.values().stream()
                .flatMap(provider -> provider.getEndpoints().stream())
                .filter(endpoint -> endpoint.matchesResourceURI(resourceURI)).findAny();
    }

    /**
     * @see OEmbedRegistry#getProvider(String)
     */
    @Override
    public Optional<OEmbedProvider> getProvider(final String name) {
        return Optional.ofNullable(providersByName.get(name));
    }

    /**
     * @see OEmbedRegistry#numProviders()
     */
    @Override
    public int numProviders() {
        return providersByName.size();
    }
}
