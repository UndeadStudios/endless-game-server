package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import okhttp3.Request;

/**
 * @author Corey
 * @since 07/06/19
 */
public class VoteCheckRequest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VoteCheckRequest.class);
    private final String username;

    public int execute() throws RuntimeException {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("account").addPathSegment("vote").addPathSegment(username.replaceAll("_", " ")).build();
        final okhttp3.Request request = new Request.Builder().url(url).get().build();
        try {
            try (okhttp3.Response response = http.newCall(request).execute()) {
                final okhttp3.ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new RuntimeException("Response body is not present.");
                }
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Response is not successful.");
                }
                final java.lang.String body = responseBody.string();
                //If empty besides {}
                if (body.length() == 0) {
                    throw new RuntimeException("Response body is empty.");
                }
                return Integer.parseInt(body);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VoteCheckRequest(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof VoteCheckRequest)) return false;
        final VoteCheckRequest other = (VoteCheckRequest) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof VoteCheckRequest;
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
        return "VoteCheckRequest(username=" + this.getUsername() + ")";
    }
}
