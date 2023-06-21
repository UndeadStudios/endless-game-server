package com.zenyte.api.client.webhook;

import com.zenyte.api.client.webhook.model.DiscordWebhook;
import com.zenyte.api.client.webhook.model.EmbedObject;
import com.zenyte.game.world.entity.player.GameMode;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Pattern;

public class GlobalBroadcastWebhook extends Webhook {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalBroadcastWebhook.class);
    private static final String ICON_URL_TEMPLATE = "https://zenyte.com/img/adventure-icons/%s";
    private static boolean disabled;
    private final String title;
    private final String icon;
    private final String message;

    public GlobalBroadcastWebhook(final String icon, final String message, final String title) {
        super("1065500109778993203", "OTA5NjE3MjgzMDcxMTA3MDcy.G7PuWp.DVDZhmyb4CwtQHazry8zRgpND5Qa_gZ0KDyuAE");
        this.title = title;
        this.icon = String.format(ICON_URL_TEMPLATE, icon);
        this.message = message;
    }

    public GlobalBroadcastWebhook(final int icon, final String message, final String title) {
        this(icon + ".png", message, title);
    }

    private static String emojiFromCrownId(final int crownId) {
        if (crownId == GameMode.STANDARD_IRON_MAN.getIcon()) {
            return "<:ironman:633279727641165825>";
        } else if (crownId == GameMode.ULTIMATE_IRON_MAN.getIcon()) {
            return "<:ultimate_ironman:633279727938961408>";
        } else if (crownId == GameMode.HARDCORE_IRON_MAN.getIcon()) {
            return "<:hardcore_ironman:633279727938830356>";
        }
        return "";
    }

    private static String replaceImgWithEmoji(String toReplace) {
        final java.lang.String pattern = "(<img=(\\d+)>)";
        final java.util.regex.Pattern r = Pattern.compile(pattern);
        final java.util.regex.Matcher m = r.matcher(toReplace);
        while (m.find()) {
            final java.lang.String emoji = emojiFromCrownId(NumberUtils.toInt(m.group(2), -1));
            toReplace = toReplace.replace(m.group(1), emoji);
        }
        return toReplace.replaceAll("<(shad|img|col)=.*>", ""); // remove any tags left over that we haven't already replaced
    }

    @Override
    public DiscordWebhook buildMessage() {
        return DiscordWebhook.builder().embed(EmbedObject.builder().title(title).description(replaceImgWithEmoji(message)).color(Webhook.EMBED_COLOUR).thumbnail(new EmbedObject.Thumbnail(icon)).build()).build();
    }

    public static boolean isDisabled() {
        return GlobalBroadcastWebhook.disabled;
    }

    public static void setDisabled(final boolean disabled) {
        GlobalBroadcastWebhook.disabled = disabled;
    }
}
