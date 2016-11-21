# oEmbed4j

A Java implementation of the [oEmbed specification](http://oembed.com).

## Quick Start

### Installation

oEmbed4j requires Java 8 and is available via Maven. You may also (build it from source)[#Building].

```xml
<dependency>
    <groupId>net.spals.oembed4j</groupId>
    <artifactId>spals-oembed4j-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Using oEmbed4j Client
Dynamically lookup oEmbed provider:
```java
// Load singleton provider registry and client
final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI);
final OEmbedClient client = JerseryOEmbedClient.create(oEmbedRegistry);

// Build request
final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI("http://www.youtube.com?watch=myvideo")
    .build();
    
// Run request
final Optional<OEmbedResponse> response = client.execute(request);
```

Make request against a specific provider:
```java
// Load singleton provider registry and client
final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI);
final OEmbedClient client = JerseryOEmbedClient.create(oEmbedRegistry);

// Build request
final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI("http://www.youtube.com?watch=myvideo")
    .build();

// Lookup provider
final Optional<OEmbedProvider> provider = oEmbedRegistry.getProvider("YouTube");

// Run request against our provider
final Optional<OEmbedResponse> response = provider.map(p -> p.getEndpoints().get(0))
    .map(endpoint -> client.executeSkipCache(request, endpoint));
```

## Design

oEmbed4j is divided into two decoupled modules: `spals-oembed4j-model` and `spals-oembed4j-client`. These two modules may be used independently. For example, if you are writing server-side provider code, you may only be interested in the oEmbed model classes.

### Model

The model module provides Java class representations of the oEmbed provider, request, and response models described in the [oEmbed specification](http://oembed.com).

All model classes use the Builder pattern with specification requirements enforced at runtime. For example:
```java
// Good request
final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI("http://www.youtube.com?watch=myvideo").build();

// Bad request - throws a runtime exception because resourceURI is a required field
final OEmbedRequest request = new OEmbedRequest.Builder().build();
```

`OEmbedProvider` may be serialized and deserialized to / from JSON. `OEmbedResponse` may be serialized and deserialized to / from JSON or XML.

### Client

The client module holds classes for looking up oEmbed providers and running oEmbed requests.

#### OEmbedRegistry

At the center of the client module is the `OEmbedRegistry`. This service keep static oEmbed provider information and allows it to be accessed at runtime. The default implemenation uses a simple in memory Java map.

To use an `OEmbedRegistry`, you must first load it. This can either be done from a `URI` endpoint which serves JSON or a local JSON file. The default provider `URI` at [http://oembed.com/providers.json](http://oembed.com/providers.json) is made available. For example:
```java
// Load registry from the default provider list
final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI);

// Load registry from a local file
final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromFile(new File("/path/to/myProviderList.json"));
```

Note that an `OEmbedRegistry` can be treated as a singleton service in your dependency injection framework.

Once the registry is loaded, you can ask it for information about a specific provider or ask it to dynamically match a resource URI to a provider endpoint:
```java
// Ask registry for a specific provider
final Optional<OEmbedProvider> provider = registry.getProvider("YouTube");

// Ask registry to match a resource URI
final URI resourceURI = URI.create("http://www.youtube.com?watch=myvideo");
final Optional<OEmbedEndpoint> endpoint = registry.getEndpoint(resourceURI);
```

Notice in either case, the `OEmbedRegistry` will never return a `null` value. If a provider or endpoint cannot be found, `Optional.empty()` will be returned.

#### OEmbedClient

The `OEmbedClient` allows for a full end-to-end flow of an oEmbed request. The client incorporates an `OEmbedRegistry` as well as a parser and a cache.
```java
// Load registry from the default provider list
final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI);
// Create a client using our registry to handle automatic provider lookups
final OEmbedClient client = JerseyOEmbedClient.create(registry);

// Build request
final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI("http://www.youtube.com?watch=myvideo")
    .build();

// OPTION 1: Run request without using the client cache and have the client automatically lookup the provider
final Optional<OEmbedResponse> response = client.executeSkipCache(request);
// OPTION 2: Run request using the client cache and have the client automatically lookup the provider
final Optional<OEmbedResponse> response = client.execute(request);

// OPTION 3: If we know the provider endpoint beforehand, we can run the request with that
final OEmbedEndpoint endpoint = registry.getProvider("YouTube").get().getEndpoints().get(0);
final Optional<OEmbedResponse> response = client.executeSkipCache(request, endpoint);
```

## <a name="Building"></a> Building

Prerequisites:

- Java 8+
- Maven 3.3.x+

oEmbed4j follows standard Maven practices:

- Run tests and build local JAR: `mvn package`
- Build local JAR without tests: `mvn package -DskipTests`
- Install JAR into local Maven repository: `mvn install`

## License

oEmbed4j is licensed under the BSD-3 License. See
[LICENSE](https://github.com/spals/oembed4j/blob/master/LICENSE) for the full
license text.
