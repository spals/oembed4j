package net.spals.oembed4j.client.parser;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import net.spals.oembed4j.model.OEmbedResponse;
import net.spals.oembed4j.model.OEmbedType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link OEmbedResponseParser}
 *
 * @author tkral
 */
public class OEmbedResponseParserTest {

    @DataProvider
    Object[][] parseFromJsonProvider() {
        return new Object[][] {
                {"/linkResponse.json", new OEmbedResponse.Builder().setType(OEmbedType.link).build()},
                {"/photoResponse.json", new OEmbedResponse.Builder().setType(OEmbedType.photo)
                    .setHeight(1).setWidth(2).setUrl("https://www.example.com/myphoto.jpg").build()},
                {"/richResponse.json", new OEmbedResponse.Builder().setType(OEmbedType.rich)
                        .setHeight(1).setWidth(2).setHtml("<script async src=\"https://www.example.com/myscript.js\"></script>").build()},
                {"/customFieldResponse.json", new OEmbedResponse.Builder().setType(OEmbedType.link)
                    .putAllCustomProperties(ImmutableMap.of("custom_field_1", "value", "custom_field_2", 2, "custom_field_3", true))
                    .build()},
        };
    }

    @Test(dataProvider = "parseFromJsonProvider")
    public void testParseFromJson(final String responseFile,
                                  final OEmbedResponse expectedResponse) throws IOException {
        final OEmbedResponseParser parser = new OEmbedResponseParser();
        try (final InputStream responseStream = Resources.getResource(OEmbedResponseParserTest.class, responseFile).openStream()) {
            final Optional<OEmbedResponse> response = parser.parse(responseStream, "application/json");
            assertThat(response, is(Optional.of(expectedResponse)));
        }
    }

    @DataProvider
    Object[][] parseFromXmlProvider() {
        return new Object[][] {
                {"/linkResponse.xml", new OEmbedResponse.Builder().setType(OEmbedType.link).build()},
                {"/photoResponse.xml", new OEmbedResponse.Builder().setType(OEmbedType.photo)
                    .setHeight(1).setWidth(2).setUrl("https://www.example.com/myphoto.jpg").build()},
                {"/richResponse.xml", new OEmbedResponse.Builder().setType(OEmbedType.rich)
                    .setHeight(1).setWidth(2).setHtml("<script async src=\"https://www.example.com/myscript.js\"></script>").build()},
                {"/customFieldResponse.xml", new OEmbedResponse.Builder().setType(OEmbedType.link)
                    .putAllCustomProperties(ImmutableMap.of("custom_field_1", "value", "custom_field_2", "2", "custom_field_3", "true"))
                    .build()},
        };
    }

    @Test(dataProvider = "parseFromXmlProvider")
    public void testParseFromXml(final String responseFile,
                                 final OEmbedResponse expectedResponse) throws IOException {
        final OEmbedResponseParser parser = new OEmbedResponseParser();
        try (final InputStream responseStream = Resources.getResource(OEmbedResponseParserTest.class, responseFile).openStream()) {
            final Optional<OEmbedResponse> response = parser.parse(responseStream, "text/xml");
            assertThat(response, is(Optional.of(expectedResponse)));
        }
    }
}
