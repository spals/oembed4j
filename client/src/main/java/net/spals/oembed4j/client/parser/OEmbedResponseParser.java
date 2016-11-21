package net.spals.oembed4j.client.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.spals.oembed4j.model.OEmbedFormat;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A service to parse a client response as an {@link OEmbedResponse}.
 *
 * @author tkral
 */
public class OEmbedResponseParser {

    private static final ObjectMapper jsonMapper = new ObjectMapper()
        .registerModule(new Jdk8Module());
    private static final ObjectMapper xmlMapper = new XmlMapper()
        .registerModule(new Jdk8Module());
    private static final Logger LOGGER = LoggerFactory.getLogger(OEmbedResponse.class);

    /**
     * Parse the given response {@link InputStream} which is paired with
     * the given response media type.
     *
     * @param inputStream The {@link InputStream} from the client response.
     * @param mediaType   The media type of the client response.
     * @return The parsed {@link InputStream} as an {@link OEmbedResponse}, if possible.
     * Otherwise, {@code Optional.empty()} for an unrecognized media type or for
     * any parsing error.
     */
    public Optional<OEmbedResponse> parse(final InputStream inputStream, final String mediaType) {
        checkNotNull(inputStream);
        checkNotNull(mediaType);

        switch (mediaType) {
            case MediaType.TEXT_XML:
                return parse(inputStream, OEmbedFormat.xml);
            case MediaType.APPLICATION_JSON:
                return parse(inputStream, OEmbedFormat.json);
        }

        return Optional.empty();
    }

    /**
     * Parse the given response {@link InputStream} for the given {@link OEmbedFormat}.
     *
     * @param inputStream The {@link InputStream} from the client response.
     * @param format      The {@link OEmbedFormat} used in the {@link OEmbedRequest}
     * @return The parsed {@link InputStream} as an {@link OEmbedResponse}, if possible.
     * Otherwise, {@code Optional.empty()} for any parsing error.
     */
    public Optional<OEmbedResponse> parse(final InputStream inputStream, final OEmbedFormat format) {
        checkNotNull(inputStream);
        checkNotNull(format);

        try {
            switch (format) {
                case xml:
                    return Optional.of(xmlMapper.readValue(inputStream, OEmbedResponse.class));
                case json:
                default:
                    return Optional.of(jsonMapper.readValue(inputStream, OEmbedResponse.class));
            }
        } catch (final IOException e) {
            LOGGER.info("failed to parse input stream", e);
            return Optional.empty();
        }
    }
}
