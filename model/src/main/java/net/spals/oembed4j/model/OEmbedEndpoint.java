package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.inferred.freebuilder.FreeBuilder;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
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

    // Derived field for default pattern matching
    @JsonIgnore
    Pattern getURIDomainPattern();

    @JsonIgnore
    default boolean matchesResourceURI(final URI resourceURI) {
        // If there are no scheme patterns to check, then
        // fallback to comparing the URI domains
        if (getSchemePatterns().isEmpty()) {
            return getURIDomainPattern().matcher(resourceURI.getHost()).matches();
        }

        return getSchemePatterns().stream()
            .anyMatch(pattern -> pattern.matcher(resourceURI.toString()).matches());
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
        public Builder setURIDomainPattern(final Pattern uriDomainPattern) {
            throw new UnsupportedOperationException("URI domain is a derived field and cannot be set manually");
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

            // Scheme patterns are 100% derived from the scheme templates.
            // We will completely ignore any scheme patterns set manually in the builder.
            getSchemeTemplates().stream()
                .map(schemeTemplate -> schemeTemplate.replaceAll("\\*", "(.*)"))
                // Sigh. Some providers list only http:// schemes when
                // their endpoints will also accept https:// so a straight
                // scheme match will miss these cases. So we'll err on the
                // permissive side, by checking for https:// in those cases
                // as well. Worst case is we send the request and it's rejected.
                .flatMap(schemePatternStr ->
                    ImmutableSet.of(schemePatternStr, schemePatternStr.replaceFirst("http:", "https:")).stream())
                .map(Pattern::compile)
                .collect(Collectors.toList())
                .forEach(super::addSchemePatterns);

            // URI domain pattern is derived from the URI template
            checkNotNull(getURITemplate(), "A non-empty URI template is required for an oEmbed endpoint");
            checkState(!getURITemplate().isEmpty(), "A non-empty URI template is required for an oEmbed endpoint");
            final String[] parsedURITemplate = getURITemplate().split("://");
            final String uriTemplateWithoutSchema = parsedURITemplate[parsedURITemplate.length - 1];
            final String uriTemplateHost = uriTemplateWithoutSchema.split("/")[0];
            super.setURIDomainPattern(Pattern.compile(uriTemplateHost.replaceAll("\\*", "(.*)")));

            return super.build();
        }
    }
}
