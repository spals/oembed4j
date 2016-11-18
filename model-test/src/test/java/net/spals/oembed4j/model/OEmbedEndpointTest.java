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
 * Unit tests for {@link OEmbedEndpoint}
 *
 * @author tkral
 */
public class OEmbedEndpointTest {

    @Test
    public void testDeserializeFromJson() throws IOException {
        final OEmbedEndpoint endpoint = new ObjectMapper()
                .readValue(Resources.getResource(OEmbedEndpointTest.class, "/endpoint.json"), OEmbedEndpoint.class);
        assertThat(endpoint.getDiscoveryEnabled(), is(true));
        assertThat(endpoint.getSupportedFormats(), is(EnumSet.allOf(OEmbedFormat.class)));
        assertThat(endpoint.getURITemplate(), is("https://vimeo.com/api/oembed.{format}"));
    }

    @DataProvider
    Object[][] matchesURIProvider() {
        return new Object[][] {
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/video/2"), true},
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/deadbeef/2"), false},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1/2"), true},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1"), false},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1"), true},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1/2"), true},
        };
    }

    @Test(dataProvider = "matchesURIProvider")
    public void testMatchesURI(final List<String> schemeTemplates, final URI resourceURI, final boolean expectedResult) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder().setURITemplate("https://www.example.com/oembed")
                .addAllSchemeTemplates(schemeTemplates).build();
        assertThat(endpoint.matchesURI(resourceURI), is(expectedResult));
    }
}
