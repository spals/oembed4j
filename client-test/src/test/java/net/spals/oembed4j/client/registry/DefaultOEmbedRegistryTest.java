package net.spals.oembed4j.client.registry;

import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

/**
 * Unit tests for {@link DefaultOEmbedRegistry}
 *
 * @author tkral
 */
public class DefaultOEmbedRegistryTest {

    @Test
    public void testLoad() throws IOException, URISyntaxException {
        final URI providersURI = Resources.getResource(DefaultOEmbedRegistryTest.class,
                "/providers.json").toURI();
        final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(providersURI);
        assertThat(registry.numProviders(), greaterThan(0));
        // Spot check some common providers
        assertThat(registry.getProvider("Vimeo"), not(Optional.empty()));
        assertThat(registry.getProvider("YouTube"), not(Optional.empty()));

    }
}
