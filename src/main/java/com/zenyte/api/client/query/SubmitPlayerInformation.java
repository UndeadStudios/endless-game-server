package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.PlayerInformation;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @author Corey
 * @since 20:24 - 25/06/2019
 */
public class SubmitPlayerInformation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubmitPlayerInformation.class);
    private final PlayerInformation info;

    public void execute() {
        final okhttp3.RequestBody body = APIClient.jsonBody(info);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("user").addPathSegment("info").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = APIClient.CLIENT.newCall(request).execute();
            response.close();
            log.info("Sent player information to api for \'" + info.getUsername() + "\'");
        } catch (final SocketException | SocketTimeoutException ignored) {
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public SubmitPlayerInformation(final PlayerInformation info) {
        this.info = info;
    }

    public PlayerInformation getInfo() {
        return this.info;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SubmitPlayerInformation)) return false;
        final SubmitPlayerInformation other = (SubmitPlayerInformation) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$info = this.getInfo();
        final Object other$info = other.getInfo();
        if (this$info == null ? other$info != null : !this$info.equals(other$info)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SubmitPlayerInformation;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $info = this.getInfo();
        result = result * PRIME + ($info == null ? 43 : $info.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SubmitPlayerInformation(info=" + this.getInfo() + ")";
    }
}
