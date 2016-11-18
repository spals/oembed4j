package net.spals.oembed4j.client.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.spals.oembed4j.model.OEmbedFormat;
import net.spals.oembed4j.model.OEmbedResponse;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author tkral
 */
public class OEmbedResponseParser {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();

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
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
