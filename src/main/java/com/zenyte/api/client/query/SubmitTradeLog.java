package com.zenyte.api.client.query;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.TradeLog;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Corey
 * @since 19/06/19
 */
public class SubmitTradeLog {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubmitTradeLog.class);
    private final TradeLog transaction;

    public void execute() {
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(transaction);
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("user").addPathSegment("log").addPathSegment("trade").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public SubmitTradeLog(final TradeLog transaction) {
        this.transaction = transaction;
    }

    public TradeLog getTransaction() {
        return this.transaction;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof SubmitTradeLog)) return false;
        final SubmitTradeLog other = (SubmitTradeLog) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$transaction = this.getTransaction();
        final Object other$transaction = other.getTransaction();
        if (this$transaction == null ? other$transaction != null : !this$transaction.equals(other$transaction)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof SubmitTradeLog;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $transaction = this.getTransaction();
        result = result * PRIME + ($transaction == null ? 43 : $transaction.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "SubmitTradeLog(transaction=" + this.getTransaction() + ")";
    }
}
