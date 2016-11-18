package net.spals.oembed4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.net.InternetDomainName;
import org.inferred.freebuilder.FreeBuilder;

import java.net.URI;
import java.util.List;

/**
 * A Java bean representation of an oEmbed provider.
 *
 * @author tkral
 */
@FreeBuilder
@JsonDeserialize(builder = OEmbedProvider.Builder.class)
public interface OEmbedProvider {

    @JsonProperty("provider_name")
    String getName();

    @JsonProperty("provider_url")
    URI getURI();

    @JsonIgnore
    InternetDomainName getDomain();

    @JsonProperty("endpoints")
    List<OEmbedEndpoint> getEndpoints();

    class Builder extends OEmbedProvider_Builder {

        @Override
        public Builder setDomain(final InternetDomainName domain) {
            throw new UnsupportedOperationException("Domain is a derived field and cannot be set manually");
        }

        @Override
        public OEmbedProvider build() {
            // The provider domain is 100% derived from the provider uri.
            // We will completely ignore any domain set manually in the builder.
            super.setDomain(InternetDomainName.from(getURI().getHost()));
            return super.build();
        }
    }
}
