package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.PunishmentLog;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Corey
 * @since 19/06/19
 */
public class SubmitUserPunishment {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubmitUserPunishment.class);
    private final PunishmentLog punishment;

    public void execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(punishment);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("user").addPathSegment("log").addPathSegment("punish").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public SubmitUserPunishment(final PunishmentLog punishment) {
        this.punishment = punishment;
    }

    public PunishmentLog getPunishment() {
        return this.punishment;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SubmitUserPunishment)) return false;
        final SubmitUserPunishment other = (SubmitUserPunishment) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$punishment = this.getPunishment();
        final Object other$punishment = other.getPunishment();
        if (this$punishment == null ? other$punishment != null : !this$punishment.equals(other$punishment)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SubmitUserPunishment;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $punishment = this.getPunishment();
        result = result * PRIME + ($punishment == null ? 43 : $punishment.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SubmitUserPunishment(punishment=" + this.getPunishment() + ")";
    }
}
