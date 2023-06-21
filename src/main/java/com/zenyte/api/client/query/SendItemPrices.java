package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import com.zenyte.game.content.grandexchange.JSONGEItemDefinitions;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * @author Kris | 16/08/2019 16:29
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SendItemPrices {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendItemPrices.class);
    private final List<JSONGEItemDefinitions> prices;

    public SendItemPrices(final List<JSONGEItemDefinitions> prices) {
        this.prices = prices;
    }

    public void execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(prices);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("runelite").addPathSegment("items").addPathSegment("prices").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }
}
