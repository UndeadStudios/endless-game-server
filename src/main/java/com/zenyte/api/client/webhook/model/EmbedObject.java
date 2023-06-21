package com.zenyte.api.client.webhook.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Corey
 * @since 06/04/2020
 */
public class EmbedObject {
    private static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm\'Z\'");
    private List<Field> fields;
    private String title;
    private String description;
    private String url;
    private int color;
    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private String timestamp;
    private Author author;


    public static class EmbedObjectBuilder {
        private List<Field> fields;
        private String title;
        private String description;
        private String url;
        private int color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private String timestamp;
        private Author author;

        public EmbedObjectBuilder field(String name, String value, boolean inline) {
            if (this.fields == null) {
                this.fields = new ArrayList<>();
            }
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        public EmbedObjectBuilder timestamp(final Date date) {
            this.timestamp = timestampFormat.format(date);
            return this;
        }

        EmbedObjectBuilder() {
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder fields(final List<Field> fields) {
            this.fields = fields;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder title(final String title) {
            this.title = title;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder description(final String description) {
            this.description = description;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder url(final String url) {
            this.url = url;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder color(final int color) {
            this.color = color;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder footer(final Footer footer) {
            this.footer = footer;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder thumbnail(final Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder image(final Image image) {
            this.image = image;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject.EmbedObjectBuilder author(final Author author) {
            this.author = author;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public EmbedObject build() {
            return new EmbedObject(this.fields, this.title, this.description, this.url, this.color, this.footer, this.thumbnail, this.image, this.timestamp, this.author);
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.EmbedObjectBuilder(fields=" + this.fields + ", title=" + this.title + ", description=" + this.description + ", url=" + this.url + ", color=" + this.color + ", footer=" + this.footer + ", thumbnail=" + this.thumbnail + ", image=" + this.image + ", timestamp=" + this.timestamp + ", author=" + this.author + ")";
        }
    }


    public static class Footer {
        private final String text;
        @SerializedName("icon_url")
        private final String iconUrl;

        public Footer(final String text, final String iconUrl) {
            this.text = text;
            this.iconUrl = iconUrl;
        }

        public String getText() {
            return this.text;
        }

        public String getIconUrl() {
            return this.iconUrl;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof EmbedObject.Footer)) return false;
            final EmbedObject.Footer other = (EmbedObject.Footer) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$text = this.getText();
            final Object other$text = other.getText();
            if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
            final Object this$iconUrl = this.getIconUrl();
            final Object other$iconUrl = other.getIconUrl();
            if (this$iconUrl == null ? other$iconUrl != null : !this$iconUrl.equals(other$iconUrl)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof EmbedObject.Footer;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $text = this.getText();
            result = result * PRIME + ($text == null ? 43 : $text.hashCode());
            final Object $iconUrl = this.getIconUrl();
            result = result * PRIME + ($iconUrl == null ? 43 : $iconUrl.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.Footer(text=" + this.getText() + ", iconUrl=" + this.getIconUrl() + ")";
        }
    }


    public static class Thumbnail {
        private final String url;

        public Thumbnail(final String url) {
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof EmbedObject.Thumbnail)) return false;
            final EmbedObject.Thumbnail other = (EmbedObject.Thumbnail) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$url = this.getUrl();
            final Object other$url = other.getUrl();
            if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof EmbedObject.Thumbnail;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $url = this.getUrl();
            result = result * PRIME + ($url == null ? 43 : $url.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.Thumbnail(url=" + this.getUrl() + ")";
        }
    }


    public static class Image {
        private final String url;

        public Image(final String url) {
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof EmbedObject.Image)) return false;
            final EmbedObject.Image other = (EmbedObject.Image) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$url = this.getUrl();
            final Object other$url = other.getUrl();
            if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof EmbedObject.Image;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $url = this.getUrl();
            result = result * PRIME + ($url == null ? 43 : $url.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.Image(url=" + this.getUrl() + ")";
        }
    }


    public static class Author {
        private final String name;
        private final String url;
        @SerializedName("icon_url")
        private final String iconUrl;

        public Author(final String name, final String url, final String iconUrl) {
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
        }

        public String getName() {
            return this.name;
        }

        public String getUrl() {
            return this.url;
        }

        public String getIconUrl() {
            return this.iconUrl;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof EmbedObject.Author)) return false;
            final EmbedObject.Author other = (EmbedObject.Author) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            final Object this$url = this.getUrl();
            final Object other$url = other.getUrl();
            if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
            final Object this$iconUrl = this.getIconUrl();
            final Object other$iconUrl = other.getIconUrl();
            if (this$iconUrl == null ? other$iconUrl != null : !this$iconUrl.equals(other$iconUrl)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof EmbedObject.Author;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final Object $url = this.getUrl();
            result = result * PRIME + ($url == null ? 43 : $url.hashCode());
            final Object $iconUrl = this.getIconUrl();
            result = result * PRIME + ($iconUrl == null ? 43 : $iconUrl.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.Author(name=" + this.getName() + ", url=" + this.getUrl() + ", iconUrl=" + this.getIconUrl() + ")";
        }
    }


    public static class Field {
        private final String name;
        private final String value;
        private final boolean inline;

        public Field(final String name, final String value, final boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public boolean isInline() {
            return this.inline;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof EmbedObject.Field)) return false;
            final EmbedObject.Field other = (EmbedObject.Field) o;
            if (!other.canEqual((Object) this)) return false;
            if (this.isInline() != other.isInline()) return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            final Object this$value = this.getValue();
            final Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof EmbedObject.Field;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isInline() ? 79 : 97);
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final Object $value = this.getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "EmbedObject.Field(name=" + this.getName() + ", value=" + this.getValue() + ", inline=" + this.isInline() + ")";
        }
    }

    EmbedObject(final List<Field> fields, final String title, final String description, final String url, final int color, final Footer footer, final Thumbnail thumbnail, final Image image, final String timestamp, final Author author) {
        this.fields = fields;
        this.title = title;
        this.description = description;
        this.url = url;
        this.color = color;
        this.footer = footer;
        this.thumbnail = thumbnail;
        this.image = image;
        this.timestamp = timestamp;
        this.author = author;
    }

    @org.jetbrains.annotations.NotNull
    public static EmbedObject.EmbedObjectBuilder builder() {
        return new EmbedObject.EmbedObjectBuilder();
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUrl() {
        return this.url;
    }

    public int getColor() {
        return this.color;
    }

    public Footer getFooter() {
        return this.footer;
    }

    public Thumbnail getThumbnail() {
        return this.thumbnail;
    }

    public Image getImage() {
        return this.image;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public Author getAuthor() {
        return this.author;
    }

    public void setFields(final List<Field> fields) {
        this.fields = fields;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public void setFooter(final Footer footer) {
        this.footer = footer;
    }

    public void setThumbnail(final Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public void setAuthor(final Author author) {
        this.author = author;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof EmbedObject)) return false;
        final EmbedObject other = (EmbedObject) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getColor() != other.getColor()) return false;
        final Object this$fields = this.getFields();
        final Object other$fields = other.getFields();
        if (this$fields == null ? other$fields != null : !this$fields.equals(other$fields)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        final Object this$footer = this.getFooter();
        final Object other$footer = other.getFooter();
        if (this$footer == null ? other$footer != null : !this$footer.equals(other$footer)) return false;
        final Object this$thumbnail = this.getThumbnail();
        final Object other$thumbnail = other.getThumbnail();
        if (this$thumbnail == null ? other$thumbnail != null : !this$thumbnail.equals(other$thumbnail)) return false;
        final Object this$image = this.getImage();
        final Object other$image = other.getImage();
        if (this$image == null ? other$image != null : !this$image.equals(other$image)) return false;
        final Object this$timestamp = this.getTimestamp();
        final Object other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !this$timestamp.equals(other$timestamp)) return false;
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof EmbedObject;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getColor();
        final Object $fields = this.getFields();
        result = result * PRIME + ($fields == null ? 43 : $fields.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $footer = this.getFooter();
        result = result * PRIME + ($footer == null ? 43 : $footer.hashCode());
        final Object $thumbnail = this.getThumbnail();
        result = result * PRIME + ($thumbnail == null ? 43 : $thumbnail.hashCode());
        final Object $image = this.getImage();
        result = result * PRIME + ($image == null ? 43 : $image.hashCode());
        final Object $timestamp = this.getTimestamp();
        result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "EmbedObject(fields=" + this.getFields() + ", title=" + this.getTitle() + ", description=" + this.getDescription() + ", url=" + this.getUrl() + ", color=" + this.getColor() + ", footer=" + this.getFooter() + ", thumbnail=" + this.getThumbnail() + ", image=" + this.getImage() + ", timestamp=" + this.getTimestamp() + ", author=" + this.getAuthor() + ")";
    }
}
