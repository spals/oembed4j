package net.spals.oembed4j.client.registry;

import net.spals.oembed4j.model.OEmbedEndpoint;
import net.spals.oembed4j.model.OEmbedProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static net.spals.oembed4j.client.registry.DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for {@link DefaultOEmbedRegistry}
 *
 * @author tkral
 */
public class DefaultOEmbedRegistryTest {

    private OEmbedRegistry registry;

    @BeforeClass
    void classSetup() {
        this.registry = DefaultOEmbedRegistry.loadFromURI(DEFAULT_OEMBED_PROVIDER_URI);
        assertThat(registry.numProviders(), greaterThan(0));
    }

    @DataProvider
    Object[][] getProviderProvider() {
        return new Object[][] {
                // Spot check some common providers
                {"Flickr"},
                {"Vimeo"},
                {"YouTube"},
        };
    }

    @Test(dataProvider = "getProviderProvider")
    public void testGetProvider(final String providerName) {
        assertThat(registry.getProvider(providerName), not(Optional.empty()));
    }

    @DataProvider
    Object[][] getEndpointProvider() {
        // Spot check some common providers
        return new Object[][] {
                {URI.create("https://www.flickr.com/photos/lilithis/2207159142/in/photolist" +
                 "-4n3gvY-6i4qpS-oztpWs-74scWq-9XLrR4-qYAD3D-oztstS-e6u5Ej-drMXnV" +
                 "-nXnhrm-9pcCvQ-qJMG4L-bXqvdN-fTMsFJ-aDqw2i-dGpYSH-9yhe1y-dw3Lkk" +
                 "-oztsCu-48pLfd-mbmQ8B-sd77Bo-gu8Wa-8HhZTe-qLVZYW-fZh6UW-7b4y4a" +
                 "-abGk8F-4HauQG-mjuMaD-fZgT9e-avqKos-7c8PDh-fJDXRa-jgG2MB-djzdH3" +
                 "-nNqZfY-bZ2XPu-fZgaN7-broQ6u-92DZ8q-aAZG8X-oKG9Sj-4x7r8n-qJc99b" +
                 "-oQWRV8-4BignY-dxTfvt-84219z-bqLqDp"), "Flickr"},
                {URI.create("https://vimeo.com/189789787"), "Vimeo"},
                {URI.create("https://www.youtube.com/watch?v=qtNI1WbOp5Q"), "YouTube"},
        };
    }

    @Test(dataProvider = "getEndpointProvider")
    public void testGetEndpoint(final URI resourceURI, final String expectedProviderName) {
        final Optional<OEmbedEndpoint> endpoint = registry.getEndpoint(resourceURI);
        assertThat(endpoint, not(Optional.empty()));

        final Optional<OEmbedProvider> provider = registry.getProvider(expectedProviderName);
        assertThat(provider.get().getEndpoints(), hasItem(endpoint.get()));
    }
}
