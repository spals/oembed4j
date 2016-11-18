package net.spals.oembed4j.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author tkral
 */
public class OEmbedEndpointTest {

    @Test
    public void testDeserializeFromJson() throws IOException {
        final OEmbedEndpoint endpoint = new ObjectMapper()
                .readValue(Resources.getResource(OEmbedEndpointTest.class, "/endpoint.json"), OEmbedEndpoint.class);
        assertThat(endpoint.getDiscoveryEnabled(), is(true));
        assertThat(endpoint.getFormats(), is(EnumSet.allOf(OEmbedFormat.class)));
        assertThat(endpoint.getURITemplate(), is("https://vimeo.com/api/oembed.{format}"));
    }

    @DataProvider
    Object[][] getURIProvider() {
        return new Object[][] {
                {"https://vimeo.com/api/oembed.{format}",
                        URI.create("https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fwww.example.com")},
                {"http://www.youtube.com/oembed",
                        URI.create("http://www.youtube.com/oembed?format=json&url=https%3A%2F%2Fwww.example.com")},
        };
    }

    @Test(dataProvider = "getURIProvider")
    public void testGetURI(final String uriTemplate, final URI expectedURI) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder().setURITemplate(uriTemplate).buildPartial();
        assertThat(endpoint.getURI(OEmbedFormat.json, URI.create("https://www.example.com")), is(expectedURI));
    }

    @DataProvider
    Object[][] matchesResourceURIProvider() {
        return new Object[][] {
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/video/2"), true},
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/deadbeef/2"), false},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1/2"), true},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1"), false},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1"), true},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1/2"), true},
        };
    }

    @Test(dataProvider = "matchesResourceURIProvider")
    public void testMatchesResourceURI(final List<String> schemeTemplates, final URI resourceURI, final boolean expectedResult) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder().setURITemplate("https://www.example.com/oembed")
                .addAllSchemeTemplates(schemeTemplates).build();
        assertThat(endpoint.matchesResourceURI(resourceURI), is(expectedResult));
    }
}
