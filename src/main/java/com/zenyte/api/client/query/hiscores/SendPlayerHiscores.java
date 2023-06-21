package com.zenyte.api.client.query.hiscores;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.SkillHiscore;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * @author Corey
 * @since 05/05/19
 */
public class SendPlayerHiscores {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendPlayerHiscores.class);
    private final String username;
    private final List<SkillHiscore> hiscores;

    public SendPlayerHiscores(final String username, final List<SkillHiscore> hiscores) {
        this.username = username;
        this.hiscores = hiscores;
    }

    public void execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(hiscores);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("hiscores").addPathSegment("user").addPathSegment(username.replaceAll("_", " ")).addPathSegment("update").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
            log.info("Sent hiscores data to api for \'" + username + "\'");
        } catch (final SocketException | SocketTimeoutException ignored) {
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }
}
