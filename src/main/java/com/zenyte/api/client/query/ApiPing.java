package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import okhttp3.FormBody;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;

/**
 * @author Corey
 * @since 01/05/19
 */
public class ApiPing {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiPing.class);

    public boolean execute() {
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("ping").build();
        final okhttp3.FormBody body = new FormBody.Builder().add("payload", "ping").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try (okhttp3.Response response = APIClient.CLIENT.newCall(request).execute()) {
            final okhttp3.ResponseBody responseBody = response.body();
            if (responseBody == null || !response.isSuccessful()) {
                return false;
            }
            final java.lang.String string = responseBody.string();
            if (!"pong".equals(string)) {
                log.error("Returned invalid response: " + string);
                return false;
            }
            return true;
        } catch (IOException e) {
            log.error(Strings.EMPTY, e);
            return false;
        }
    }
}
