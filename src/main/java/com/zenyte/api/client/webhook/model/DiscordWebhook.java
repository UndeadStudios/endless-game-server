package com.zenyte.api.client.webhook.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb
 */
public class DiscordWebhook {
    private List<EmbedObject> embeds;
    private String content;
    private String username;
    @SerializedName("avatar_url")
    private String avatarUrl;
    private boolean tts;


    public static class DiscordWebhookBuilder {
        private List<EmbedObject> embeds;
        private String content;
        private String username;
        private String avatarUrl;
        private boolean tts;

        public DiscordWebhookBuilder embed(final EmbedObject embed) {
            if (this.embeds == null) {
                this.embeds = new ArrayList<>();
            }
            this.embeds.add(embed);
            return this;
        }

        DiscordWebhookBuilder() {
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook.DiscordWebhookBuilder embeds(final List<EmbedObject> embeds) {
            this.embeds = embeds;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook.DiscordWebhookBuilder content(final String content) {
            this.content = content;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook.DiscordWebhookBuilder username(final String username) {
            this.username = username;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook.DiscordWebhookBuilder avatarUrl(final String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook.DiscordWebhookBuilder tts(final boolean tts) {
            this.tts = tts;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public DiscordWebhook build() {
            return new DiscordWebhook(this.embeds, this.content, this.username, this.avatarUrl, this.tts);
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "DiscordWebhook.DiscordWebhookBuilder(embeds=" + this.embeds + ", content=" + this.content + ", username=" + this.username + ", avatarUrl=" + this.avatarUrl + ", tts=" + this.tts + ")";
        }
    }

    DiscordWebhook(final List<EmbedObject> embeds, final String content, final String username, final String avatarUrl, final boolean tts) {
        this.embeds = embeds;
        this.content = content;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.tts = tts;
    }

    @org.jetbrains.annotations.NotNull
    public static DiscordWebhook.DiscordWebhookBuilder builder() {
        return new DiscordWebhook.DiscordWebhookBuilder();
    }

    public List<EmbedObject> getEmbeds() {
        return this.embeds;
    }

    public String getContent() {
        return this.content;
    }

    public String getUsername() {
        return this.username;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public boolean isTts() {
        return this.tts;
    }

    public void setEmbeds(final List<EmbedObject> embeds) {
        this.embeds = embeds;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTts(final boolean tts) {
        this.tts = tts;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof DiscordWebhook)) return false;
        final DiscordWebhook other = (DiscordWebhook) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isTts() != other.isTts()) return false;
        final Object this$embeds = this.getEmbeds();
        final Object other$embeds = other.getEmbeds();
        if (this$embeds == null ? other$embeds != null : !this$embeds.equals(other$embeds)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (this$content == null ? other$content != null : !this$content.equals(other$content)) return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        final Object this$avatarUrl = this.getAvatarUrl();
        final Object other$avatarUrl = other.getAvatarUrl();
        if (this$avatarUrl == null ? other$avatarUrl != null : !this$avatarUrl.equals(other$avatarUrl)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof DiscordWebhook;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isTts() ? 79 : 97);
        final Object $embeds = this.getEmbeds();
        result = result * PRIME + ($embeds == null ? 43 : $embeds.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        final Object $avatarUrl = this.getAvatarUrl();
        result = result * PRIME + ($avatarUrl == null ? 43 : $avatarUrl.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "DiscordWebhook(embeds=" + this.getEmbeds() + ", content=" + this.getContent() + ", username=" + this.getUsername() + ", avatarUrl=" + this.getAvatarUrl() + ", tts=" + this.isTts() + ")";
    }
}
