package net.spals.oembed4j.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.EnumSet;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Unit tests for {@link OEmbedResponse}
 *
 * @author tkral
 */
public class OEmbedResponseTest {

    @DataProvider
    Object[][] requiredFieldsByTypeProvider() {
        return new Object[][]{
            // Case: No fields are required by the link type
            {new OEmbedResponse.Builder(), OEmbedType.link},
            {
                new OEmbedResponse.Builder().setUrl("http://www.example.com/myimage").setHeight(1).setWidth(2),
                OEmbedType.photo
            },
            {
                new OEmbedResponse.Builder().setHtml("<html></html>").setHeight(1).setWidth(2),
                OEmbedType.rich
            },
            {
                new OEmbedResponse.Builder()
                    .setHtml("<iframe src=\"http://www.example.com/myvideo\"></iframe>")
                    .setHeight(1)
                    .setWidth(2),
                OEmbedType.video
            },
        };
    }

    @Test(dataProvider = "requiredFieldsByTypeProvider")
    public void testRequiredFieldsByType(
        final OEmbedResponse.Builder responseBuilder,
        final OEmbedType type
    ) {
        assertThat(responseBuilder.setType(type).build(), notNullValue());
    }

    @DataProvider
    Object[][] missingRequiredFieldsByTypeProvider() {
        return EnumSet.complementOf(EnumSet.of(OEmbedType.link))
            .stream().map(type -> new Object[]{type}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "missingRequiredFieldsByTypeProvider")
    public void testMissingRequiredFieldsByType(final OEmbedType type) {
        catchException(() -> new OEmbedResponse.Builder().setType(type).build());
        assertThat(caughtException(), instanceOf(IllegalStateException.class));
    }
}
