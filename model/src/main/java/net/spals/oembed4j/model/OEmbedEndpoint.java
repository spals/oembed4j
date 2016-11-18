package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Java bean representation of an oEmbed endpoint.
 *
 * @author tkral
 */
@FreeBuilder
@JsonDeserialize(builder = OEmbedEndpoint.Builder.class)
public interface OEmbedEndpoint {

    @JsonProperty("discovery")
    boolean getDiscoveryEnabled();

    @JsonProperty("formats")
    Set<OEmbedFormat> getFormats();

    @JsonProperty("schemes")
    List<String> getSchemeTemplates();

    // Derived field for pattern matching convenience
    @JsonIgnore
    List<Pattern> getSchemePatterns();

    @JsonProperty("url")
    String getURITemplate();

    @JsonIgnore
    default boolean matchesURI(final URI uri) {
        return getSchemePatterns().stream()
                .filter(pattern -> pattern.matcher(uri.toString()).matches())
                .findAny().isPresent();
    }

    class Builder extends OEmbedEndpoint_Builder {

        public Builder() {
            setDiscoveryEnabled(false);
            // By default, support all formats
            addFormats(OEmbedFormat.values());
        }

        @Override
        public OEmbedEndpoint build() {
            // Scheme patterns are 100% derived from the scheme templates.
            // We will completely ignore any scheme patterns set manually in the builder.
            clearSchemePatterns();
            addAllSchemePatterns(getSchemeTemplates().stream()
                    .map(schemeTemplate -> schemeTemplate.replaceAll("\\*", "(.*)"))
                    .map(schemaPatterStr -> Pattern.compile(schemaPatterStr))
                    .collect(Collectors.toList()));
            return super.build();
        }
    }
}
