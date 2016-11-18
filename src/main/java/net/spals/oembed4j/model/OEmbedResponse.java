package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
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

    // ========== Common required fields ==========
    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    OEmbedType getType();

    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    default String getVersion() {
        // As per the oEmbed spec, version should always be 1.0
        return "1.0";
    }

    // ========== Common optional fields ==========
    @JsonProperty("author_name")
    @JacksonXmlProperty(localName = "author_name")
    Optional<String> getAuthorName();

    @JsonProperty("author_url")
    @JacksonXmlProperty(localName = "author_url")
    Optional<URI> getAuthorURI();

    @JsonProperty("cache_age")
    @JacksonXmlProperty(localName = "cache_age")
    Optional<Long> getCacheAge();

    @JsonProperty("provider_name")
    @JacksonXmlProperty(localName = "provider_name")
    Optional<String> getProviderName();

    @JsonProperty("provider_url")
    @JacksonXmlProperty(localName = "provider_url")
    Optional<URI> getProviderURI();

    @JsonProperty("thumbnail_height")
    @JacksonXmlProperty(localName = "thumbnail_height")
    Optional<Integer> getThumbnailHeight();

    @JsonProperty("thumbnail_url")
    @JacksonXmlProperty(localName = "thumbnail_url")
    Optional<URI> getThumbnailURI();

    @JsonProperty("thumbnail_width")
    @JacksonXmlProperty(localName = "thumbnail_width")
    Optional<Integer> getThumbnailWidth();

    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type")
    Optional<String> getTitle();

    // ========== Optional fields by type ==========
    @JsonProperty("height")
    @JacksonXmlProperty(localName = "height")
    Optional<Integer> getHeight();

    @JsonProperty("html")
    @JacksonXmlProperty(localName = "html")
    Optional<String> getHtml();

    @JsonProperty("url")
    @JacksonXmlProperty(localName = "url")
    Optional<String> getUrl();

    @JsonProperty("width")
    @JacksonXmlProperty(localName = "width")
    Optional<Integer> getWidth();

    // ========== Custom fields by provider ==========
    @JsonAnyGetter
    Map<String, Object> getCustomProperties();

    default boolean hasCustomProperties() {
        return !getCustomProperties().isEmpty();
    }

    class Builder extends OEmbedResponse_Builder {

        @Override
        public OEmbedResponse build() {
            // Check optional fields by type
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
