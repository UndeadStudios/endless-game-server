package com.zenyte.api.client.query.hiscores;

import com.zenyte.api.client.APIClient;
import com.zenyte.api.model.ExpMode;
import com.zenyte.api.model.ExpModeUpdate;
import com.zenyte.game.world.entity.player.Player;
import okhttp3.Request;
import org.apache.logging.log4j.util.Strings;

/**
 * @author Corey
 * @since 05/05/19
 */
public class UpdateHiscoreExpMode {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateHiscoreExpMode.class);
    private final Player player;
    private final ExpMode oldMode;
    private final ExpMode newMode;

    public void execute() {
        if (player.inArea("Tutorial Island")) {
            log.info("User \'" + player.getName() + "\' in tutorial island, holding off sending hiscores data");
            return;
        }
        final java.lang.String username = player.getUsername();
        final okhttp3.OkHttpClient http = APIClient.CLIENT;
        final okhttp3.RequestBody body = APIClient.jsonBody(new ExpModeUpdate(oldMode, newMode, player.getGameMode().getApiRole()));
        final okhttp3.HttpUrl url = APIClient.urlBuilder().addPathSegment("hiscores").addPathSegment("user").addPathSegment(username.replaceAll("_", " ")).addPathSegment("update").addPathSegment("expmode").build();
        final okhttp3.Request request = new Request.Builder().url(url).post(body).build();
        try {
            final okhttp3.Response response = http.newCall(request).execute();
            response.close();
            log.info("Sent exp mode update to the api for \'" + username + "\'");
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public UpdateHiscoreExpMode(final Player player, final ExpMode oldMode, final ExpMode newMode) {
        this.player = player;
        this.oldMode = oldMode;
        this.newMode = newMode;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ExpMode getOldMode() {
        return this.oldMode;
    }

    public ExpMode getNewMode() {
        return this.newMode;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof UpdateHiscoreExpMode)) return false;
        final UpdateHiscoreExpMode other = (UpdateHiscoreExpMode) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$player = this.getPlayer();
        final Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        final Object this$oldMode = this.getOldMode();
        final Object other$oldMode = other.getOldMode();
        if (this$oldMode == null ? other$oldMode != null : !this$oldMode.equals(other$oldMode)) return false;
        final Object this$newMode = this.getNewMode();
        final Object other$newMode = other.getNewMode();
        if (this$newMode == null ? other$newMode != null : !this$newMode.equals(other$newMode)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof UpdateHiscoreExpMode;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        final Object $oldMode = this.getOldMode();
        result = result * PRIME + ($oldMode == null ? 43 : $oldMode.hashCode());
        final Object $newMode = this.getNewMode();
        result = result * PRIME + ($newMode == null ? 43 : $newMode.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "UpdateHiscoreExpMode(player=" + this.getPlayer() + ", oldMode=" + this.getOldMode() + ", newMode=" + this.getNewMode() + ")";
    }
}
