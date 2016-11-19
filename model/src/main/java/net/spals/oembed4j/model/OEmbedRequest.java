package net.spals.oembed4j.model;

import org.inferred.freebuilder.FreeBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A Java bean representation of an oEmbed query response according
 * to the full oEmbed spec at http://oembed.com/.
 *
 * @author tkral
 */
@FreeBuilder
public interface OEmbedRequest {

    URI getResourceURI();

    Optional<Integer> getMaxHeight();

    Optional<Integer> getMaxWidth();

    default URI toURI(final OEmbedEndpoint endpoint) {
        checkArgument(endpoint.matchesResourceURI(getResourceURI()),
                "OEmbedEndpoint (%s) does not have a matching scheme for resource URI %s",
                endpoint.getURITemplate(), getResourceURI());

        final UriBuilder uriBuilder = UriBuilder.fromUri(endpoint.getURITemplate());
        if (endpoint.getURITemplate().contains("format")) {
            uriBuilder.resolveTemplate("format", endpoint.getDefaultFormat());
        } else {
            uriBuilder.queryParam("format", endpoint.getDefaultFormat());
        }

        getMaxHeight().ifPresent(maxHeight -> uriBuilder.queryParam("maxheight", maxHeight));
        getMaxWidth().ifPresent(maxWidth -> uriBuilder.queryParam("maxwidth", maxWidth));

        return uriBuilder.queryParam("url", getResourceURI()).build();
    }

    class Builder extends OEmbedRequest_Builder {  }
}
