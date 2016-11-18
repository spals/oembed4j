package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Iterables;
import org.inferred.freebuilder.FreeBuilder;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

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
    Set<OEmbedFormat> getSupportedFormats();

    // Derived field which returns a default format
    // for this endpoint.
    @JsonIgnore
    OEmbedFormat getDefaultFormat();

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

        private static final OEmbedFormat DEFAULT_FORMAT = OEmbedFormat.json;

        public Builder() {
            setDiscoveryEnabled(false);
        }

        @Override
        public Builder setDefaultFormat(final OEmbedFormat defaultFormat) {
            throw new UnsupportedOperationException("Default format is a derived field and cannot be set manually");
        }

        @Override
        public Builder addSchemePatterns(final Pattern schemePattern) {
            throw new UnsupportedOperationException("Scheme patterns are derived fields and cannot be set manually");
        }

        @Override
        public OEmbedEndpoint build() {
            // By default, support all formats
            if (getSupportedFormats().isEmpty()) {
                super.addAllSupportedFormats(EnumSet.allOf(OEmbedFormat.class));
            }
            // We don't really care which format is used since any response will get
            // deserialized into an OEmbedResponse bean anyway. The only important thing
            // is that we use a format supported by the endpoint. So use the DEFAULT_FORMAT
            // if it's supported, otherwise, just grab the first format in the supported set.
            super.setDefaultFormat(getSupportedFormats().stream()
                .filter(supportedFormat -> supportedFormat == DEFAULT_FORMAT).findFirst()
                .orElseGet(() -> Iterables.getFirst(getSupportedFormats(), DEFAULT_FORMAT)));

            checkState(!getSchemeTemplates().isEmpty(), "OEmbedEndpoint must contain at least one scheme template");
            // Scheme patterns are 100% derived from the scheme templates.
            // We will completely ignore any scheme patterns set manually in the builder.
            super.clearSchemePatterns();
            getSchemeTemplates().stream()
                    .map(schemeTemplate -> schemeTemplate.replaceAll("\\*", "(.*)"))
                    .map(schemePatterStr -> Pattern.compile(schemePatterStr))
                    .collect(Collectors.toList())
                    .forEach(schemePattern -> super.addSchemePatterns(schemePattern));
            return super.build();
        }
    }
}
