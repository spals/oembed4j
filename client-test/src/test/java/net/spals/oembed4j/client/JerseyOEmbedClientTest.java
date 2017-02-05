package net.spals.oembed4j.client;

import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import net.spals.oembed4j.model.OEmbedType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JerseyOEmbedClient}
 *
 * @author tkral
 */
public class JerseyOEmbedClientTest {

    @DataProvider
    Object[][] executeWithoutCachingProvider() {
        return new Object[][]{
            // Case: Empty response
            {Optional.empty()},
            // Case: Response without cache age
            {Optional.of(new OEmbedResponse.Builder().setType(OEmbedType.link).build())},
        };
    }

    @Test(dataProvider = "executeWithoutCachingProvider")
    public void testExecuteWithoutCaching(final Optional<OEmbedResponse> response) {
        final OEmbedRegistry registry = mock(OEmbedRegistry.class);
        final JerseyOEmbedClient client = spy(JerseyOEmbedClient.create(registry));
        doReturn(response).when(client).executeSkipCache(any(OEmbedRequest.class));

        final OEmbedRequest request = new OEmbedRequest.Builder()
            .setResourceURI("http://www.example.com/myresource").build();

        assertThat(client.execute(request), is(response));
        // Verify that we don't cache the result
        assertThat(client.getResponseCache(), is(anEmptyMap()));
    }

    @Test
    public void testExecuteWithCaching() {
        final Optional<OEmbedResponse> response = Optional.of(new OEmbedResponse.Builder()
            .setType(OEmbedType.link)
            .setCacheAge(2)
            .build());

        final OEmbedRegistry registry = mock(OEmbedRegistry.class);
        final JerseyOEmbedClient client = spy(JerseyOEmbedClient.create(registry));
        doReturn(response).when(client).executeSkipCache(any(OEmbedRequest.class));

        final OEmbedRequest request = new OEmbedRequest.Builder()
            .setResourceURI("http://www.example.com/myresource").build();

        assertThat(client.execute(request), is(response));
        // Verify that we don't cache the result
        assertThat(client.getResponseCache(), hasKey(request));
    }
}
