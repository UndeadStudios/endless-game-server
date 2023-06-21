package com.zenyte.game.world.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zenyte.api.model.WorldLocation;
import com.zenyte.api.model.WorldType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Corey
 * @since 20/05/19
 */
public class WorldProfile {
    @JsonIgnore
    private String key;
    private int number;
    private String host;
    private int port;
    private String activity;
    private boolean isPrivate;
    private boolean development;
    private boolean verifyPasswords;
    private WorldLocation location;
    private List<WorldType> flags;
    private ApiSettings api;

    public WorldProfile() {
    }

    public WorldProfile(String key, int number, String host, int port, String activity, boolean isPrivate, boolean development, boolean verifyPasswords, WorldLocation location, List<WorldType> flags, ApiSettings api) {
        this.key = key;
        this.number = number;
        this.host = host;
        this.port = port;
        this.activity = activity;
        this.isPrivate = isPrivate;
        this.development = development;
        this.verifyPasswords = verifyPasswords;
        this.location = location;
        this.flags = flags;
        this.api = api;
    }

    public WorldProfile(String key) throws IOException {
        this.key = key;
        WorldProfile world;
        final org.yaml.snakeyaml.Yaml yaml = new Yaml(new Constructor(Worlds.class));
        try (java.io.BufferedReader br = new BufferedReader(new FileReader(new File("worlds.yml")))) {
            Worlds worlds = yaml.load(br);
            world = worlds.worlds.get(key);
            this.number = world.number;
            this.host = world.host;
            this.port = world.port;
            this.activity = world.activity;
            this.isPrivate = world.isPrivate;
            this.development = world.development;
            this.verifyPasswords = world.verifyPasswords;
            this.location = world.location;
            this.flags = world.flags;
            this.api = world.api;
        }
    }

    public boolean isBeta() {
        return this.flags.contains(WorldType.BETA);
    }


    static class Worlds {
        public Map<String, WorldProfile> worlds;

        public Map<String, WorldProfile> getWorlds() {
            return this.worlds;
        }

        public void setWorlds(final Map<String, WorldProfile> worlds) {
            this.worlds = worlds;
        }

        @Override
        public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
            if (o == this) return true;
            if (!(o instanceof WorldProfile.Worlds)) return false;
            final WorldProfile.Worlds other = (WorldProfile.Worlds) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$worlds = this.getWorlds();
            final Object other$worlds = other.getWorlds();
            if (this$worlds == null ? other$worlds != null : !this$worlds.equals(other$worlds)) return false;
            return true;
        }

        protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
            return other instanceof WorldProfile.Worlds;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $worlds = this.getWorlds();
            result = result * PRIME + ($worlds == null ? 43 : $worlds.hashCode());
            return result;
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "WorldProfile.Worlds(worlds=" + this.getWorlds() + ")";
        }

        public Worlds(final Map<String, WorldProfile> worlds) {
            this.worlds = worlds;
        }

        public Worlds() {
        }
    }

    public String getKey() {
        return this.key;
    }

    public int getNumber() {
        return this.number;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getActivity() {
        return this.activity;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public boolean isDevelopment() {
        return this.development;
    }

    public boolean isVerifyPasswords() {
        return this.verifyPasswords;
    }

    public WorldLocation getLocation() {
        return this.location;
    }

    public List<WorldType> getFlags() {
        return this.flags;
    }

    public ApiSettings getApi() {
        return this.api;
    }

    @JsonIgnore
    public void setKey(final String key) {
        this.key = key;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setActivity(final String activity) {
        this.activity = activity;
    }

    public void setPrivate(final boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setDevelopment(final boolean development) {
        this.development = development;
    }

    public void setVerifyPasswords(final boolean verifyPasswords) {
        this.verifyPasswords = verifyPasswords;
    }

    public void setLocation(final WorldLocation location) {
        this.location = location;
    }

    public void setFlags(final List<WorldType> flags) {
        this.flags = flags;
    }

    public void setApi(final ApiSettings api) {
        this.api = api;
    }

    @Override
    public boolean equals(@org.jetbrains.annotations.Nullable final Object o) {
        if (o == this) return true;
        if (!(o instanceof WorldProfile)) return false;
        final WorldProfile other = (WorldProfile) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getNumber() != other.getNumber()) return false;
        if (this.getPort() != other.getPort()) return false;
        if (this.isPrivate() != other.isPrivate()) return false;
        if (this.isDevelopment() != other.isDevelopment()) return false;
        if (this.isVerifyPasswords() != other.isVerifyPasswords()) return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
        final Object this$host = this.getHost();
        final Object other$host = other.getHost();
        if (this$host == null ? other$host != null : !this$host.equals(other$host)) return false;
        final Object this$activity = this.getActivity();
        final Object other$activity = other.getActivity();
        if (this$activity == null ? other$activity != null : !this$activity.equals(other$activity)) return false;
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        final Object this$flags = this.getFlags();
        final Object other$flags = other.getFlags();
        if (this$flags == null ? other$flags != null : !this$flags.equals(other$flags)) return false;
        final Object this$api = this.getApi();
        final Object other$api = other.getApi();
        if (this$api == null ? other$api != null : !this$api.equals(other$api)) return false;
        return true;
    }

    protected boolean canEqual(@org.jetbrains.annotations.Nullable final Object other) {
        return other instanceof WorldProfile;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getNumber();
        result = result * PRIME + this.getPort();
        result = result * PRIME + (this.isPrivate() ? 79 : 97);
        result = result * PRIME + (this.isDevelopment() ? 79 : 97);
        result = result * PRIME + (this.isVerifyPasswords() ? 79 : 97);
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $host = this.getHost();
        result = result * PRIME + ($host == null ? 43 : $host.hashCode());
        final Object $activity = this.getActivity();
        result = result * PRIME + ($activity == null ? 43 : $activity.hashCode());
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $flags = this.getFlags();
        result = result * PRIME + ($flags == null ? 43 : $flags.hashCode());
        final Object $api = this.getApi();
        result = result * PRIME + ($api == null ? 43 : $api.hashCode());
        return result;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "WorldProfile(key=" + this.getKey() + ", number=" + this.getNumber() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", activity=" + this.getActivity() + ", isPrivate=" + this.isPrivate() + ", development=" + this.isDevelopment() + ", verifyPasswords=" + this.isVerifyPasswords() + ", location=" + this.getLocation() + ", flags=" + this.getFlags() + ", api=" + this.getApi() + ")";
    }
}
