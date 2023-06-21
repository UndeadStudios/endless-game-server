package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import okhttp3.Request;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class TotalDonatedRequest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TotalDonatedRequest.class);
    private final String username;

    public TotalDonatedRequest(final String username) {
        this.username = username.replaceAll("_", " ");
    }

    public int execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("account").addPathSegment("spent").addPathSegment(username).build();
        final okhttp3.Request request = new Request.Builder().url(url).get().build();
        try {
            try (okhttp3.Response response = http.newCall(request).execute()) {
                final okhttp3.ResponseBody responseBody = response.body();
                if (responseBody == null || !response.isSuccessful()) {
                    return -1;
                }
                final java.lang.String body = responseBody.string();
                //If empty besides {}
                if (body.length() == 0) {
                    return -1;
                }
                return NumberUtils.isDigits(body) ? Integer.parseInt(body) : 0;
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
        return -1;
    }
}
