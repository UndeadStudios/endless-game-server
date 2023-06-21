package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.World;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Corey
 * @since 01/05/19
 */
public class SendWorldInfo {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendWorldInfo.class);
    private final World worldInfo;

    public SendWorldInfo(final World world) {
        this.worldInfo = world;
    }

    public void execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(worldInfo);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("worldinfo").addPathSegment("world").addPathSegment("update").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }
}
