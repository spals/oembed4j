package net.spals.oembed4j.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link OEmbedRequest}
 *
 * @author tkral
 */
public class OEmbedRequestTest {

    @DataProvider
    Object[][] toURIProvider() {
        return new Object[][]{
            {
                "https://vimeo.com/api/oembed.{format}",
                URI.create("https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fwww.example.com%2F")
            },
            {
                "http://www.youtube.com/oembed",
                URI.create("http://www.youtube.com/oembed?format=json&url=https%3A%2F%2Fwww.example.com%2F")
            },
        };
    }

    @Test(dataProvider = "toURIProvider")
    public void testToURIWithURI(final String uriTemplate, final URI expectedURI) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder()
            .addSchemeTemplates("https://www.example.com/*")
            .setURITemplate(uriTemplate)
            .build();
        final OEmbedRequest request = new OEmbedRequest.Builder()
            .setResourceURI(URI.create("https://www.example.com/"))
            .build();

        assertThat(request.toURI(endpoint), is(expectedURI));
    }

    @Test(dataProvider = "toURIProvider")
    public void testToURIWithString(final String uriTemplate, final URI expectedURI) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder()
            .addSchemeTemplates("https://www.example.com/*")
            .setURITemplate(uriTemplate)
            .build();
        final OEmbedRequest request = new OEmbedRequest.Builder()
            .setResourceURI("https://www.example.com/")
            .build();

        assertThat(request.toURI(endpoint), is(expectedURI));
    }

    @Test
    public void testToURIWithMaxHeightAndMaxWidth() {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder()
            .addSchemeTemplates("https://www.example.com/*")
            .setURITemplate("http://www.youtube.com/oembed")
            .build();
        final OEmbedRequest request = new OEmbedRequest.Builder()
            .setResourceURI(URI.create("https://www.example.com/"))
            .setMaxHeight(1)
            .setMaxWidth(2)
            .build();

        assertThat(
            request.toURI(endpoint),
            is(
                URI.create(
                    "http://www.youtube.com/oembed?format=json&maxheight=1&maxwidth=2&url=https%3A%2F%2Fwww.example.com%2F"))
        );
    }
}
