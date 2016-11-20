package net.spals.oembed4j.model;

import com.google.common.net.InternetDomainName;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link OEmbedProvider}
 *
 * @author tkral
 */
public class OEmbedProviderTest {

    @DataProvider
    Object[][] domainDerivedProvider() {
        return new Object[][] {
                {URI.create("https://vimeo.com/"), "vimeo.com"},
                {URI.create("http://www.youtube.com/"), "www.youtube.com"},
        };
    }

    @Test(dataProvider = "domainDerivedProvider")
    public void testDomainDerived(final URI uri, final String expectedDomainName) {
        final OEmbedProvider provider = new OEmbedProvider.Builder()
                .setName("MyProvider").setURI(uri).build();
        assertThat(provider.getDomain().toString(), is(expectedDomainName));
    }

    @Test
    public void testDomainManual() {
        catchException(new OEmbedProvider.Builder()).setDomain(InternetDomainName.from("www.example.com"));
        assertThat(caughtException(), instanceOf(UnsupportedOperationException.class));
    }
}
