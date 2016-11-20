package net.spals.oembed4j.client;

import net.spals.oembed4j.client.registry.DefaultOEmbedRegistry;
import net.spals.oembed4j.client.registry.OEmbedRegistry;
import net.spals.oembed4j.model.OEmbedRequest;
import net.spals.oembed4j.model.OEmbedResponse;
import net.spals.oembed4j.model.OEmbedType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static net.spals.oembed4j.client.registry.DefaultOEmbedRegistry.DEFAULT_OEMBED_PROVIDER_URI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Unit tests for {@link JerseyOEmbedClient}
 *
 * @author tkral
 */
public class JerseyOEmbedClientTest {

    private OEmbedClient oEmbedClient;

    @BeforeClass
    void classSetup() {
        final OEmbedRegistry registry = DefaultOEmbedRegistry.loadFromURI(DEFAULT_OEMBED_PROVIDER_URI);
        this.oEmbedClient = JerseyOEmbedClient.create(registry);
    }

    @AfterClass
    void classTearDown() {
        this.oEmbedClient.close();
    }

    @DataProvider
    Object[][] executeProvider() {
        // Spot check common oEmbed providers
        return new Object[][] {
                flickr(),
                vimeo(),
                youTube(),
        };
    }

    @Test(enabled = false, dataProvider = "executeProvider")
    public void testExecute(final URI resourceURI,
                            final OEmbedResponse expectedResponse) {
        final OEmbedRequest request = new OEmbedRequest.Builder().setResourceURI(resourceURI).build();
        final Optional<OEmbedResponse> response = oEmbedClient.execute(request);
        assertThat(response, not(Optional.empty()));
        assertThat(response.get(), is(expectedResponse));
    }

    private Object[] flickr() {
        final URI uri = URI.create("https://www.flickr.com/photos/lilithis/2207159142/in/photolist" +
                "-4n3gvY-6i4qpS-oztpWs-74scWq-9XLrR4-qYAD3D-oztstS-e6u5Ej-drMXnV" +
                "-nXnhrm-9pcCvQ-qJMG4L-bXqvdN-fTMsFJ-aDqw2i-dGpYSH-9yhe1y-dw3Lkk" +
                "-oztsCu-48pLfd-mbmQ8B-sd77Bo-gu8Wa-8HhZTe-qLVZYW-fZh6UW-7b4y4a" +
                "-abGk8F-4HauQG-mjuMaD-fZgT9e-avqKos-7c8PDh-fJDXRa-jgG2MB-djzdH3" +
                "-nNqZfY-bZ2XPu-fZgaN7-broQ6u-92DZ8q-aAZG8X-oKG9Sj-4x7r8n-qJc99b" +
                "-oQWRV8-4BignY-dxTfvt-84219z-bqLqDp");

        final OEmbedResponse expectedResponse = new OEmbedResponse.Builder()
                .setAuthorName("Lilithis")
                .setAuthorURI(URI.create("https://www.flickr.com/photos/lilithis/"))
                .setCacheAge(3600L)
                .setHeight(500)
                .setHtml("<a data-flickr-embed=\"true\" href=\"https://www.flickr.com/photos/lilithis/2207159142/\" title=\"Cat by Lilithis, on Flickr\">" +
                          "<img src=\"https://farm3.staticflickr.com/2106/2207159142_8206ab6984.jpg\" width=\"334\" height=\"500\" alt=\"Cat\">" +
                         "</a>" +
                         "<script async src=\"https://embedr.flickr.com/assets/client-code.js\" charset=\"utf-8\">" +
                         "</script>")
                .setProviderName("Flickr")
                .setProviderURI(URI.create("https://www.flickr.com/"))
                .setThumbnailHeight(150)
                .setThumbnailURI(URI.create("https://farm3.staticflickr.com/2106/2207159142_8206ab6984_q.jpg"))
                .setThumbnailWidth(150)
                .setTitle("Cat")
                .setType(OEmbedType.photo)
                .setUrl("https://farm3.staticflickr.com/2106/2207159142_8206ab6984.jpg")
                .setWidth(334)
                .putCustomProperties("flickr_type", "photo")
                .putCustomProperties("license", "Attribution-ShareAlike License")
                .putCustomProperties("license_id", "5")
                .putCustomProperties("license_url", "https://creativecommons.org/licenses/by-sa/2.0/")
                .putCustomProperties("web_page", "https://www.flickr.com/photos/lilithis/2207159142/")
                .putCustomProperties("web_page_short_url", "https://flic.kr/p/4n3gvY")
                .build();

        return new Object[]{uri, expectedResponse};
    }

    private Object[] vimeo() {
        final URI uri = URI.create("https://vimeo.com/189789787");

        final OEmbedResponse expectedResponse = new OEmbedResponse.Builder()
                .setAuthorName("Uncorked Productions")
                .setAuthorURI(URI.create("https://vimeo.com/uncorkedprods"))
                .setHeight(338)
                .setHtml("<iframe src=\"https://player.vimeo.com/video/189789787\" width=\"640\" height=\"338\" frameborder=\"0\" title=\"Maxine the Fluffy Corgi\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>")
                .setProviderName("Vimeo")
                .setProviderURI(URI.create("https://vimeo.com/"))
                .setThumbnailHeight(337)
                .setThumbnailURI(URI.create("https://i.vimeocdn.com/video/600355807_640.jpg"))
                .setThumbnailWidth(640)
                .setTitle("Maxine the Fluffy Corgi")
                .setType(OEmbedType.video)
                .setWidth(640)
                .putCustomProperties("description", "Short legs. Big city.\n\nhttps://www.instagram.com/madmax_fluffyroad/\n\nmade by: bryan reisberg (& Maxine's owner)\ncamera: owen levelle\nvoice: jon st. john")
                .putCustomProperties("duration", 34)
                .putCustomProperties("is_plus", 0)
                .putCustomProperties("thumbnail_url_with_play_button", "https://i.vimeocdn.com/filter/overlay?src0=https%3A%2F%2Fi.vimeocdn.com%2Fvideo%2F600355807_640.jpg&src1=http%3A%2F%2Ff.vimeocdn.com%2Fp%2Fimages%2Fcrawler_play.png")
                .putCustomProperties("upload_date", "2016-11-01 10:40:37")
                .putCustomProperties("uri", "/videos/189789787")
                .putCustomProperties("video_id", 189789787)
                .build();

        return new Object[]{uri, expectedResponse};
    }

    private Object[] youTube() {
        final URI uri = URI.create("https://www.youtube.com/watch?v=qtNI1WbOp5Q");

        final OEmbedResponse expectedResponse = new OEmbedResponse.Builder()
                .setAuthorName("Real Grumpy Cat")
                .setAuthorURI(URI.create("https://www.youtube.com/user/SevereAvoidance"))
                .setHeight(270)
                .setHtml("<iframe width=\"480\" height=\"270\" src=\"https://www.youtube.com/embed/qtNI1WbOp5Q?feature=oembed\" frameborder=\"0\" allowfullscreen></iframe>")
                .setProviderName("YouTube")
                .setProviderURI(URI.create("https://www.youtube.com/"))
                .setThumbnailHeight(360)
                .setThumbnailURI(URI.create("https://i.ytimg.com/vi/qtNI1WbOp5Q/hqdefault.jpg"))
                .setThumbnailWidth(480)
                .setTitle("Grumpy Cat's Worst #IceBucketChallenge Ever!")
                .setType(OEmbedType.video)
                .setWidth(480)
                .build();

        return new Object[]{uri, expectedResponse};
    }
}
