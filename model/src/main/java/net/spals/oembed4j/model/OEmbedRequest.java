package net.spals.oembed4j.model;

import org.inferred.freebuilder.FreeBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

/**
 * A Java bean representation of an oEmbed query response according
 * to the full oEmbed spec at http://oembed.com/.
 *
 * @author tkral
 */
@FreeBuilder
public interface OEmbedRequest {

    OEmbedFormat DEFAULT_FORMAT = OEmbedFormat.json;

    URI getResourceURI();

    OEmbedFormat getFormat();

    Optional<Integer> getMaxHeight();

    Optional<Integer> getMaxWidth();

    default URI toURI(final OEmbedEndpoint endpoint) {
        final UriBuilder uriBuilder = UriBuilder.fromUri(endpoint.getURITemplate());
        if (endpoint.getURITemplate().contains("format")) {
            uriBuilder.resolveTemplate("format", getFormat());
        } else {
            uriBuilder.queryParam("format", getFormat());
        }

        getMaxHeight().ifPresent(maxHeight -> uriBuilder.queryParam("maxheight", maxHeight));
        getMaxWidth().ifPresent(maxWidth -> uriBuilder.queryParam("maxwidth", maxWidth));

        return uriBuilder.queryParam("url", getResourceURI()).build();
    }

    class Builder extends OEmbedRequest_Builder {

        public Builder() {
            setFormat(DEFAULT_FORMAT);
        }
    }
}
