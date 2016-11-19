package net.spals.oembed4j.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    Object[][] defaultFormatDerivedProvider() {
        return new Object[][] {
                // Case: No supported formats explicitly set
                {new OEmbedEndpoint.Builder()
                        .setURITemplate("https://www.example.com/oembed/"), OEmbedFormat.json},
                // Case: Explicitly set supported format
                {new OEmbedEndpoint.Builder()
                        .addSupportedFormats(OEmbedFormat.xml)
                        .setURITemplate("https://www.example.com/oembed/"), OEmbedFormat.xml},
        };
    }

    @Test(dataProvider = "defaultFormatDerivedProvider")
    public void testDefaultFormatDerived(final OEmbedEndpoint.Builder endpointBuilder,
                                         final OEmbedFormat expectedDefaultFormat) {
        assertThat(endpointBuilder.build().getDefaultFormat(), is(expectedDefaultFormat));
    }

    @Test
    public void testDefaultFormatManual() {
        catchException(new OEmbedEndpoint.Builder()).setDefaultFormat(OEmbedFormat.json);
        assertThat(caughtException(), instanceOf(UnsupportedOperationException.class));
    }

    @DataProvider
    Object[][] schemePatternDerivedProvider() {
        return new Object[][] {
                {"https://www.example.com/*", ImmutableSet.of("https://www.example.com/(.*)")},
                {"https://www.example.com/*/*", ImmutableSet.of("https://www.example.com/(.*)/(.*)")},
                {"https://www.example.com/*/path/*", ImmutableSet.of("https://www.example.com/(.*)/path/(.*)")},
                {"http://www.example.com/*", ImmutableSet.of("http://www.example.com/(.*)", "https://www.example.com/(.*)")},
        };
    }

    @Test(dataProvider = "schemePatternDerivedProvider")
    public void testSchemePatternDerived(final String schemeTemplate,
                                         final Set<String> expectedSchemePattern) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder()
                .addSchemeTemplates(schemeTemplate)
                .setURITemplate("https://www.example.com/oembed")
                .build();
        assertThat(endpoint.getSchemePatterns().stream()
                .map(schemePattern -> schemePattern.pattern())
                .collect(Collectors.toSet()), is(expectedSchemePattern));
    }

    @Test
    public void testSchemePatternManual() {
        catchException(new OEmbedEndpoint.Builder()).addSchemePatterns(Pattern.compile(".*"));
        assertThat(caughtException(), instanceOf(UnsupportedOperationException.class));
    }

    @DataProvider
    Object[][] uriDomainDerivedProvider() {
        return new Object[][] {
                {"https://www.example.com/oembed", "www.example.com"},
                {"https://www.example.com/oembed.{format}", "www.example.com"},
                {"https://*.example.com/oembed", "(.*).example.com"},
        };
    }

    @Test(dataProvider = "uriDomainDerivedProvider")
    public void testURIDomainPatternDerived(final String uriTemplate,
                                     final String expectedURIDomainPattern) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder()
                .setURITemplate(uriTemplate).build();
        assertThat(endpoint.getURIDomainPattern().pattern(), is(expectedURIDomainPattern));
    }

    @Test
    public void testURIDomainPatternManual() {
        catchException(new OEmbedEndpoint.Builder()).setURIDomainPattern(Pattern.compile(".*"));
        assertThat(caughtException(), instanceOf(UnsupportedOperationException.class));
    }

    @DataProvider
    Object[][] domainMatchesResourceURIProvider() {
        return new Object[][] {
                {"https://www.example.com/oembed", URI.create("https://www.example.com/myphoto.jpg"), true},
                {"https://www.example.com/oembed", URI.create("http://www.example.com/myphoto.jpg"), true},
                {"https://www.example.com/oembed", URI.create("https://subdomain.example.com/myphoto.jpg"), false},
                {"https://*.example.com/oembed", URI.create("https://subdomain.example.com/myphoto.jpg"), true},
        };
    }

    @Test(dataProvider = "domainMatchesResourceURIProvider")
    public void testDomainMatchesResourceURI(final String uriTemplate, final URI resourceURI, final boolean expectedResult) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder().setURITemplate(uriTemplate).build();
        assertThat(endpoint.matchesResourceURI(resourceURI), is(expectedResult));
    }

    @DataProvider
    Object[][] schemeMatchesResourceURIProvider() {
        return new Object[][] {
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/video/2"), true},
                {ImmutableList.of("https://vimeo.com/album/*/video/*"), URI.create("https://vimeo.com/album/1/deadbeef/2"), false},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1/2"), true},
                {ImmutableList.of("https://vimeo.com/channels/*/*"), URI.create("https://vimeo.com/channels/1"), false},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1"), true},
                {ImmutableList.of("https://player.vimeo.com/video/*"), URI.create("https://player.vimeo.com/video/1/2"), true},
        };
    }

    @Test(dataProvider = "schemeMatchesResourceURIProvider")
    public void testSchemeMatchesResourceURI(final List<String> schemeTemplates, final URI resourceURI, final boolean expectedResult) {
        final OEmbedEndpoint endpoint = new OEmbedEndpoint.Builder().setURITemplate("https://www.example.com/oembed")
                .addAllSchemeTemplates(schemeTemplates).build();
        assertThat(endpoint.matchesResourceURI(resourceURI), is(expectedResult));
    }
}
