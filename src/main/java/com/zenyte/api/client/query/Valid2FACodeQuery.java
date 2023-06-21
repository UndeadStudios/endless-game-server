package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;

/**
 * @author Corey
 * @since 01/06/19
 */
public class Valid2FACodeQuery {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Valid2FACodeQuery.class);
    private final int memberId;
    private final String code;

    public boolean execute() {
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("user").addPathSegment(String.valueOf(memberId)).addPathSegment("check2fa").addQueryParameter("code", code).build();
        final okhttp3.Request request = new Request.Builder().url(url).get().build();
        try (okhttp3.Response response = APIClient.CLIENT.newCall(request).execute()) {
            final okhttp3.ResponseBody responseBody = response.body();
            if (responseBody == null || !response.isSuccessful()) {
                return false;
            }
            final java.lang.String string = responseBody.string();
            if (!"true".equals(string)) {
                log.error("[member_id=" + memberId + ", code=" + code + "] Invalid response or code; response: " + string);
                return false;
            }
            return true;
        } catch (IOException e) {
            log.error(Strings.EMPTY, e);
            return false;
        }
    }

    public Valid2FACodeQuery(final int memberId, final String code) {
        this.memberId = memberId;
        this.code = code;
    }

    public int getMemberId() {
        return this.memberId;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof Valid2FACodeQuery)) return false;
        final Valid2FACodeQuery other = (Valid2FACodeQuery) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getMemberId() != other.getMemberId()) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof Valid2FACodeQuery;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getMemberId();
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "Valid2FACodeQuery(memberId=" + this.getMemberId() + ", code=" + this.getCode() + ")";
    }
}
