package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.Preconditions;
import org.inferred.freebuilder.FreeBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * A Java bean representation of an oEmbed query response according
 * to the full oEmbed spec at http://oembed.com/.
 *
 * @author tkral
 */
@FreeBuilder
@JsonDeserialize(builder = OEmbedResponse.Builder.class)
@JacksonXmlRootElement(localName = "oembed")
public interface OEmbedResponse {

    /**
     * As per the oEmbed spec, version should always be 1.0
     */
    String DEFAULT_VERSION = "1.0";

    // ========== Common required fields ==========
    @JsonProperty("type")
    OEmbedType getType();

    @JsonProperty("version")
    String getVersion();

    // ========== Common optional fields ==========
    @JsonProperty("author_name")
    Optional<String> getAuthorName();

    @JsonProperty("author_url")
    Optional<URI> getAuthorURI();

    @JsonProperty("cache_age")
    Optional<Long> getCacheAge();

    @JsonProperty("provider_name")
    Optional<String> getProviderName();

    @JsonProperty("provider_url")
    Optional<URI> getProviderURI();

    @JsonProperty("thumbnail_height")
    Optional<Integer> getThumbnailHeight();

    @JsonProperty("thumbnail_url")
    Optional<URI> getThumbnailURI();

    @JsonProperty("thumbnail_width")
    Optional<Integer> getThumbnailWidth();

    @JsonProperty("title")
    Optional<String> getTitle();

    // ========== Required fields by type ==========
    @JsonProperty("height")
    Optional<Integer> getHeight();

    @JsonProperty("html")
    Optional<String> getHtml();

    @JsonProperty("url")
    Optional<String> getUrl();

    @JsonProperty("width")
    Optional<Integer> getWidth();

    // ========== Custom fields by provider ==========
    @JsonAnyGetter
    Map<String, Object> getCustomProperties();

    default boolean hasCustomProperties() {
        return !getCustomProperties().isEmpty();
    }

    class Builder extends OEmbedResponse_Builder {

        public Builder() {
            super.setVersion(DEFAULT_VERSION);
        }

        // Method exists to add {@link JsonAnySetter} annotation.
        @SuppressWarnings("EmptyMethod")
        @Override
        @JsonAnySetter
        public Builder putCustomProperties(final String key, final Object value) {
            return super.putCustomProperties(key, value);
        }

        @Override
        public Builder setVersion(String version) {
            Preconditions.checkState(DEFAULT_VERSION.equals(version), "Per the oEmbed spec, versions should always be %s", DEFAULT_VERSION);
            return super.setVersion(version);
        }

        @Override
        public OEmbedResponse build() {
            // Check required fields by type
            switch(getType()) {
                case photo:
                    checkState(getUrl().isPresent(), "Source url is required for %s content", getType());
                    checkState(getHeight().isPresent(), "Height is required for %s content", getType());
                    checkState(getWidth().isPresent(), "Width is required for %s content", getType());
                    break;
                case rich:
                case video:
                    checkState(getHtml().isPresent(), "Html is required for %s content", getType());
                    checkState(getHeight().isPresent(), "Height is required for %s content", getType());
                    checkState(getWidth().isPresent(), "Width is required for %s content", getType());
                    break;
            }
            return super.build();
        }
    }
}
