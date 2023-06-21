package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Kris | 03/05/2019 21:22
 * @author Corey
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class AccountInformationRequest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccountInformationRequest.class);
    private final String username;

    public AccountInformationRequestResults execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("user").addPathSegment("columns").addPathSegment(username.replaceAll("_", " ")).addQueryParameter("columns", "joined,member_id,msg_count_new,members_pass_hash,mfa_details").build();
        final okhttp3.Request request = new Request.Builder().url(url).get().build();
        try {
            try (okhttp3.Response response = http.newCall(request).execute()) {
                final okhttp3.ResponseBody responseBody = response.body();
                if (responseBody == null || !response.isSuccessful()) {
                    return null;
                }
                final java.lang.String body = responseBody.string();
                //If empty besides {}
                if (body.length() == 2) {
                    return null;
                }
                return new AccountInformationRequestResults(body);
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
        return null;
    }

    public AccountInformationRequest(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof AccountInformationRequest)) return false;
        final AccountInformationRequest other = (AccountInformationRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof AccountInformationRequest;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "AccountInformationRequest(username=" + this.getUsername() + ")";
    }
}
