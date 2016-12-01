package net.spals.oembed4j.client;

import net.spals.midas.GoldFile;
import net.spals.midas.io.GoldPaths;
import net.spals.midas.serializer.Serializers;
import net.spals.oembed4j.client.registry.DefaultOEmbedRegistry;
import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

import static net.spals.oembed4j.client.registry.DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Functional tests for {@link JerseyOEmbedClient}
 *
 * @author tkral
 */
public class JerseyOEmbedClientFTest {

    private OEmbedClient oEmbedClient;

    @BeforeClass
    void classSetup() {
        final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DEFAULT_OEMBED_PROVIDER_URI);
        this.oEmbedClient = JerseyOEmbedClient.create(registry);
    }

    @AfterClass
    void classTearDown() {
        this.oEmbedClient.close();
    }

    @DataProvider
    Object[][] executeProvider() {
        // Spot check common oEmbed providers
        return new Object[][]{
            flickr(),
            vimeo(),
            youtube(),
        };
    }

    @Test(dataProvider = "executeProvider")
    public void testExecute(final URI resourceURI) {
        final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI(resourceURI).build();
        final Optional<OEmbedResponse> response = oEmbedClient.execute(request);
        assertThat(response, not(Optional.empty()));

        GoldFile.builder()
            .withPath(GoldPaths.simpleClass(GoldPaths.MAVEN, JerseyOEmbedClientFTest.class))
            .withReflectionSerializer()
            .withSerializer(Serializers.of())
            .build()
            .run(response.get(), Paths.get(resourceURI.getHost()));
    }

    private Object[] flickr() {
        final URI uri = URI.create("https://www.flickr.com/photos/lilithis/2207159142/in/photolist" +
            "-4n3gvY-6i4qpS-oztpWs-74scWq-9XLrR4-qYAD3D-oztstS-e6u5Ej-drMXnV" +
            "-nXnhrm-9pcCvQ-qJMG4L-bXqvdN-fTMsFJ-aDqw2i-dGpYSH-9yhe1y-dw3Lkk" +
            "-oztsCu-48pLfd-mbmQ8B-sd77Bo-gu8Wa-8HhZTe-qLVZYW-fZh6UW-7b4y4a" +
            "-abGk8F-4HauQG-mjuMaD-fZgT9e-avqKos-7c8PDh-fJDXRa-jgG2MB-djzdH3" +
            "-nNqZfY-bZ2XPu-fZgaN7-broQ6u-92DZ8q-aAZG8X-oKG9Sj-4x7r8n-qJc99b" +
            "-oQWRV8-4BignY-dxTfvt-84219z-bqLqDp");

        return new Object[]{uri};
    }

    private Object[] vimeo() {
        final URI uri = URI.create("https://vimeo.com/189789787");

        return new Object[]{uri};
    }

    private Object[] youtube() {
        final URI uri = URI.create("https://www.youtube.com/watch?v=qtNI1WbOp5Q");

        return new Object[]{uri};
    }
}
