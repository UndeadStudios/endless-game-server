package mgi.types.config.npcs;

import com.esotericsoftware.kryo.Kryo;
import com.zenyte.Constants;
import com.zenyte.Game;
import com.zenyte.game.world.entity.masks.RenderType;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.GroupType;
import mgi.tools.jagcached.cache.File;
import mgi.types.Definitions;
import mgi.types.config.TransmogrifiableType;
import mgi.utilities.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;

public class NPCDefinitions implements Definitions, Cloneable, TransmogrifiableType, RenderType {
    public static NPCDefinitions[] definitions;
    private int id;
    private String name;
    private transient String lowercaseName;
    private String[] options;
    private String[] filteredOptions;
    private int filterFlag;
    private int varp;
    private int varbit;
    private int[] transmogrifiedIds;
    private int[] models;
    private int[] chatModels;
    private int standAnimation;
    private int walkAnimation;
    private int rotate90Animation;
    private int rotate180Animation;
    private int rotate270Animation;
    private int size;
    private int combatLevel;
    private boolean minimapVisible;
    private boolean visible;
    private boolean clickable;
    private boolean clippedMovement;
    private boolean isFamiliar;
    private int resizeX;
    private int resizeY;
    private int direction;
    private int headIcon;
    private int ambience;
    private int contrast;
    private short[] originalColours;
    private short[] replacementColours;
    private short[] originalTextures;
    private short[] replacementTextures;
    private int field3568;
    private int field3580;
    private Int2ObjectOpenHashMap<Object> parameters;
    private int finalTransmogrification;

    @Override
    public int defaultId() {
        return finalTransmogrification;
    }

    public NPCDefinitions(final int id, final ByteBuffer buffer) {
        this.id = id;
        setDefaults();
        decode(buffer);
    }

    public NPCDefinitions clone() throws CloneNotSupportedException {
        return (NPCDefinitions) super.clone();
    }

    public static NPCDefinitions get(final int id) {
        if (id < 0 || id >= definitions.length) {
            return null;
        }
        return definitions[id];
    }

    public static NPCDefinitions getOrThrow(final int id) {
        final mgi.types.config.npcs.NPCDefinitions definitions = NPCDefinitions.definitions[id];
        if (definitions == null) {
            throw new RuntimeException("NPCDefinitions missing for id: " + id);
        }
        return definitions;
    }

    @Override
    public void load() {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
        final mgi.tools.jagcached.cache.Group npcs = configs.findGroupByID(GroupType.NPC);
        definitions = new NPCDefinitions[20000];
        for (int id = 0; id < npcs.getHighestFileId(); id++) {
            final mgi.tools.jagcached.cache.File file = npcs.findFileByID(id);
            if (file == null) {
                continue;
            }
            final mgi.utilities.ByteBuffer buffer = file.getData();
            if (buffer == null) {
                continue;
            }
            buffer.setPosition(0);
            definitions[id] = new NPCDefinitions(id, buffer);
        }
    }

    public static void filter() {
        if (/*Constants.WORLD_PROFILE.isDevelopment() && */!Constants.SPAWN_MODE) return;
        System.err.println("Filtering definitions.");
        for (final mgi.types.config.npcs.NPCDefinitions definition : definitions) {
            if (definition == null) continue;
            for (int i = 0; i < 5; i++) {
                final java.lang.String option = definition.filteredOptions[i];
                if (Constants.SPAWN_MODE) {
                    if (i == 0) {
                        definition.filteredOptions[i] = "Teleport to me";
                        definition.filterFlag |= 1 << i;
                        continue;
                    }
                    if (i == 1) {
                        definition.filteredOptions[i] = "Set radius";
                        definition.filterFlag |= 1 << i;
                        continue;
                    }
                    if (i == 4) {
                        definition.filteredOptions[i] = "Remove spawn";
                        definition.filterFlag |= 1 << i;
                        continue;
                    }
                }
                if (option == null) continue;
                if (NPCPlugin.getHandler(definition.id, option) == null) {
                    definition.filteredOptions[i] = null;
                    definition.filterFlag |= 1 << i;
                }
            }
        }
    }

    private void setDefaults() {
        name = lowercaseName = "null";
        size = 1;
        standAnimation = -1;
        walkAnimation = -1;
        rotate180Animation = -1;
        rotate90Animation = -1;
        rotate270Animation = -1;
        field3568 = -1;
        field3580 = -1;
        options = new String[5];
        filteredOptions = new String[5];
        minimapVisible = true;
        combatLevel = -1;
        resizeX = 128;
        resizeY = 128;
        visible = false;
        ambience = 0;
        contrast = 0;
        headIcon = -1;
        direction = 32;
        varbit = -1;
        varp = -1;
        clickable = true;
        clippedMovement = true;
        isFamiliar = false;
    }

    @Override
    public void decode(final ByteBuffer buffer) {
        while (true) {
            final int opcode = buffer.readUnsignedByte();
            if (opcode == 0) {
                return;
            }
            decode(buffer, opcode);
        }
    }

    @Override
    public void decode(final ByteBuffer buffer, final int opcode) {
        switch (opcode) {
        case 1: 
            {
                final int size = buffer.readUnsignedByte();
                models = new int[size];
                for (int i = 0; i < size; i++) {
                    models[i] = buffer.readUnsignedShort();
                }
                return;
            }
        case 2: 
            name = buffer.readString();
            lowercaseName = name.toLowerCase();
            return;
        case 12: 
            size = buffer.readUnsignedByte();
            return;
        case 13: 
            standAnimation = buffer.readUnsignedShort();
            return;
        case 14: 
            walkAnimation = buffer.readUnsignedShort();
            return;
        case 15: 
            field3568 = buffer.readUnsignedShort();
            return;
        case 16: 
            field3580 = buffer.readUnsignedShort();
            return;
        case 17: 
            walkAnimation = buffer.readUnsignedShort();
            rotate180Animation = buffer.readUnsignedShort();
            rotate90Animation = buffer.readUnsignedShort();
            rotate270Animation = buffer.readUnsignedShort();
            return;
        case 30: 
        case 31: 
        case 32: 
        case 33: 
        case 34: 
            options[opcode - 30] = buffer.readString();
            if (options[opcode - 30].equalsIgnoreCase("Hidden")) {
                options[opcode - 30] = null;
            }
            filteredOptions[opcode - 30] = options[opcode - 30];
            return;
        case 40: 
            {
                final int size = buffer.readUnsignedByte();
                originalColours = new short[size];
                replacementColours = new short[size];
                for (int i = 0; i < size; i++) {
                    originalColours[i] = (short) (buffer.readUnsignedShort());
                    replacementColours[i] = (short) (buffer.readUnsignedShort());
                }
                return;
            }
        case 41: 
            {
                final int size = buffer.readUnsignedByte();
                originalTextures = new short[size];
                replacementTextures = new short[size];
                for (int i = 0; i < size; i++) {
                    originalTextures[i] = (short) (buffer.readUnsignedShort());
                    replacementTextures[i] = (short) (buffer.readUnsignedShort());
                }
                return;
            }
        case 60: 
            {
                final int size = buffer.readUnsignedByte();
                chatModels = new int[size];
                for (int i = 0; i < size; i++) {
                    chatModels[i] = buffer.readUnsignedShort();
                }
                return;
            }
        case 93: 
            minimapVisible = false;
            return;
        case 95: 
            combatLevel = buffer.readUnsignedShort();
            return;
        case 97: 
            resizeX = buffer.readUnsignedShort();
            return;
        case 98: 
            resizeY = buffer.readUnsignedShort();
            return;
        case 99: 
            visible = true;
            return;
        case 100: 
            ambience = buffer.readByte();
            return;
        case 101: 
            contrast = buffer.readByte();
            return;
        case 102: 
            headIcon = buffer.readUnsignedShort();
            return;
        case 103: 
            direction = buffer.readUnsignedShort();
            return;
        case 106: 
        case 118: 
            {
                varbit = buffer.readUnsignedShort();
                if (varbit == 65535) {
                    varbit = -1;
                }
                varp = buffer.readUnsignedShort();
                if (varp == 65535) {
                    varp = -1;
                }
                finalTransmogrification = -1;
                if (opcode == 118) {
                    finalTransmogrification = buffer.readUnsignedShort();
                    if (finalTransmogrification == 65535) {
                        finalTransmogrification = -1;
                    }
                }
                final int size = buffer.readUnsignedByte();
                transmogrifiedIds = new int[size + 2];
                for (int int_3 = 0; int_3 <= size; int_3++) {
                    transmogrifiedIds[int_3] = buffer.readUnsignedShort();
                    if (transmogrifiedIds[int_3] == 65535) {
                        transmogrifiedIds[int_3] = -1;
                    }
                }
                transmogrifiedIds[size + 1] = finalTransmogrification;
                return;
            }
        case 107: 
            clippedMovement = false;
            return;
        case 109: 
            clickable = false;
            return;
        case 111: 
            isFamiliar = true;
            return;
        case 249: 
            parameters = buffer.readParameters();
            return;
        }
    }

    public String getOption(final int option) {
        if (options == null || options.length < option || option == 0) {
            return "";
        }
        return options[option - 1];
    }

    public boolean containsOptionCaseSensitive(final String option) {
        return ArrayUtils.contains(options, option);
    }

    public boolean containsOption(final String o) {
        if (options == null) {
            return false;
        }
        for (final String option : options) {
            if (option == null) {
                continue;
            }
            if (option.equalsIgnoreCase(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public mgi.utilities.ByteBuffer encode() {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(4 * 1024);
        if (models != null) {
            buffer.writeByte(1);
            buffer.writeByte(models.length);
            for (int index = 0; index < models.length; index++) {
                buffer.writeShort(models[index]);
            }
        }
        if (!name.equals("null")) {
            buffer.writeByte(2);
            buffer.writeString(name);
        }
        if (size != 0) {
            buffer.writeByte(12);
            buffer.writeByte(size);
        }
        if (standAnimation != -1) {
            buffer.writeByte(13);
            buffer.writeShort(standAnimation == 65535 ? -1 : standAnimation);
        }
        final boolean extendedWalkAnimations = (rotate90Animation & 65535) != 65535 || (rotate180Animation & 65535) != 65535 || (rotate270Animation & 65535) != 65535;
        if (walkAnimation != -1 && !extendedWalkAnimations) {
            buffer.writeByte(14);
            buffer.writeShort(walkAnimation == 65535 ? -1 : walkAnimation);
        }
        if (field3568 != -1) {
            buffer.writeByte(15);
            buffer.writeShort(field3568);
        }
        if (field3580 != -1) {
            buffer.writeByte(16);
            buffer.writeShort(field3580);
        }
        if (extendedWalkAnimations) {
            buffer.writeByte(17);
            buffer.writeShort(walkAnimation == 65535 ? -1 : walkAnimation);
            buffer.writeShort(rotate180Animation == 65535 ? -1 : rotate180Animation);
            buffer.writeShort(rotate90Animation == 65535 ? -1 : rotate90Animation);
            buffer.writeShort(rotate270Animation == 65535 ? -1 : rotate270Animation);
        }
        for (int index = 0; index < 5; index++) {
            if (options[index] != null && !options[index].equals("Hidden")) {
                buffer.writeByte((30 + index));
                buffer.writeString(options[index]);
            }
        }
        if (originalColours != null && replacementColours != null && originalColours.length != 0 && replacementColours.length != 0) {
            buffer.writeByte(40);
            buffer.writeByte(originalColours.length);
            for (int index = 0; index < originalColours.length; index++) {
                buffer.writeShort(originalColours[index]);
                buffer.writeShort(replacementColours[index]);
            }
        }
        if (originalTextures != null && replacementTextures != null && originalTextures.length != 0 && replacementTextures.length != 0) {
            buffer.writeByte(41);
            buffer.writeByte(originalTextures.length);
            for (int index = 0; index < originalTextures.length; index++) {
                buffer.writeShort(originalTextures[index]);
                buffer.writeShort(replacementTextures[index]);
            }
        }
        if (chatModels != null) {
            buffer.writeByte(60);
            buffer.writeByte(chatModels.length);
            for (int index = 0; index < chatModels.length; index++) {
                buffer.writeShort(chatModels[index]);
            }
        }
        if (!minimapVisible) {
            buffer.writeByte(93);
        }
        if (combatLevel != -1) {
            buffer.writeByte(95);
            buffer.writeShort(combatLevel);
        }
        if (resizeX != 0) {
            buffer.writeByte(97);
            buffer.writeShort(resizeX);
        }
        if (resizeY != 0) {
            buffer.writeByte(98);
            buffer.writeShort(resizeY);
        }
        if (visible) {
            buffer.writeByte(99);
        }
        if (ambience != 0) {
            buffer.writeByte(100);
            buffer.writeByte(ambience);
        }
        if (contrast != 0) {
            buffer.writeByte(101);
            buffer.writeByte(contrast);
        }
        if (headIcon != -1) {
            buffer.writeByte(102);
            buffer.writeShort(headIcon);
        }
        if (direction != -1) {
            buffer.writeByte(103);
            buffer.writeShort(direction);
        }
        if (!clippedMovement) {
            buffer.writeByte(107);
        }
        if (!clickable) {
            buffer.writeByte(109);
        }
        if (isFamiliar) {
            buffer.writeByte(111);
        }
        if (transmogrifiedIds != null && transmogrifiedIds.length > 0) {
            buffer.writeByte(106);
            buffer.writeShort(varbit == -1 ? 65535 : varbit);
            buffer.writeShort(varp == -1 ? 65535 : varp);
            buffer.writeByte(transmogrifiedIds.length - 2);
            for (int index = 0; index <= transmogrifiedIds.length - 2; index++) {
                buffer.writeShort(transmogrifiedIds[index] == -1 ? 65535 : transmogrifiedIds[index]);
            }
        }
        if (transmogrifiedIds != null) {
            buffer.writeByte(118);
            buffer.writeShort(varbit == -1 ? 65535 : varbit);
            buffer.writeShort(varp == -1 ? 65535 : varp);
            buffer.writeShort(transmogrifiedIds[transmogrifiedIds.length - 1] == -1 ? 65535 : transmogrifiedIds[transmogrifiedIds.length - 1]);
            buffer.writeByte(transmogrifiedIds.length - 2);
            for (int index = 0; index <= transmogrifiedIds.length - 2; index++) {
                buffer.writeShort(transmogrifiedIds[index] == -1 ? 65535 : transmogrifiedIds[index]);
            }
        }
        if (parameters != null && !parameters.isEmpty()) {
            buffer.writeByte(249);
            buffer.writeParameters(parameters);
        }
        buffer.writeByte(0);
        return buffer;
    }

    @Override
    public void pack() {
        definitions[id] = this;
        try {
            Game.getCacheMgi().getArchive(ArchiveType.CONFIGS).findGroupByID(GroupType.NPC).addFile(new File(id, encode()));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(id);
        }
    }

    @Override
    public int getVarbitId() {
        return varbit;
    }

    @Override
    public int getVarpId() {
        return varp;
    }

    @Override
    public int getStand() {
        return this.standAnimation;
    }

    @Override
    public int getStandTurn() {
        return -1;
    }

    @Override
    public int getWalk() {
        return this.walkAnimation;
    }

    @Override
    public int getRotate180() {
        return this.rotate180Animation;
    }

    @Override
    public int getRotate90() {
        return this.rotate90Animation;
    }

    @Override
    public int getRotate270() {
        return this.rotate270Animation;
    }

    @Override
    public int getRun() {
        return this.walkAnimation;
    }

    public void setOption(final int index, final String option) {
        if (options == null) {
            options = new String[5];
        }
        options[index] = option.isEmpty() ? null : option;
    }

    public void setFilteredOption(final int index, final String option) {
        if (filteredOptions == null) {
            filteredOptions = new String[5];
        }
        filteredOptions[index] = option.isEmpty() ? null : option;
    }

    public NPCDefinitions copy() {
        Kryo kryo = new Kryo();
        kryo.register(NPCDefinitions.class);
        kryo.register(int[].class);
        kryo.register(short[].class);
        kryo.register(String[].class);
        return kryo.copy(this);
    }


    public static class NPCDefinitionsBuilder {
        private int id;
        private String name;
        private String lowercaseName;
        private String[] options;
        private String[] filteredOptions;
        private int filterFlag;
        private int varp;
        private int varbit;
        private int[] transmogrifiedIds;
        private int[] models;
        private int[] chatModels;
        private int standAnimation;
        private int walkAnimation;
        private int rotate90Animation;
        private int rotate180Animation;
        private int rotate270Animation;
        private int size;
        private int combatLevel;
        private boolean minimapVisible;
        private boolean visible;
        private boolean clickable;
        private boolean clippedMovement;
        private boolean isFamiliar;
        private int resizeX;
        private int resizeY;
        private int direction;
        private int headIcon;
        private int ambience;
        private int contrast;
        private short[] originalColours;
        private short[] replacementColours;
        private short[] originalTextures;
        private short[] replacementTextures;
        private int field3568;
        private int field3580;
        private Int2ObjectOpenHashMap<Object> parameters;
        private int finalTransmogrification;

        NPCDefinitionsBuilder() {
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder id(final int id) {
            this.id = id;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder name(final String name) {
            this.name = name;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder lowercaseName(final String lowercaseName) {
            this.lowercaseName = lowercaseName;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder options(final String[] options) {
            this.options = options;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder filteredOptions(final String[] filteredOptions) {
            this.filteredOptions = filteredOptions;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder filterFlag(final int filterFlag) {
            this.filterFlag = filterFlag;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder varp(final int varp) {
            this.varp = varp;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder varbit(final int varbit) {
            this.varbit = varbit;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder transmogrifiedIds(final int[] transmogrifiedIds) {
            this.transmogrifiedIds = transmogrifiedIds;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder models(final int[] models) {
            this.models = models;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder chatModels(final int[] chatModels) {
            this.chatModels = chatModels;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder standAnimation(final int standAnimation) {
            this.standAnimation = standAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder walkAnimation(final int walkAnimation) {
            this.walkAnimation = walkAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder rotate90Animation(final int rotate90Animation) {
            this.rotate90Animation = rotate90Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder rotate180Animation(final int rotate180Animation) {
            this.rotate180Animation = rotate180Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder rotate270Animation(final int rotate270Animation) {
            this.rotate270Animation = rotate270Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder size(final int size) {
            this.size = size;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder combatLevel(final int combatLevel) {
            this.combatLevel = combatLevel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder minimapVisible(final boolean minimapVisible) {
            this.minimapVisible = minimapVisible;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder visible(final boolean visible) {
            this.visible = visible;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder clickable(final boolean clickable) {
            this.clickable = clickable;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder clippedMovement(final boolean clippedMovement) {
            this.clippedMovement = clippedMovement;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder isFamiliar(final boolean isFamiliar) {
            this.isFamiliar = isFamiliar;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder resizeX(final int resizeX) {
            this.resizeX = resizeX;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder resizeY(final int resizeY) {
            this.resizeY = resizeY;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder direction(final int direction) {
            this.direction = direction;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder headIcon(final int headIcon) {
            this.headIcon = headIcon;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder ambience(final int ambience) {
            this.ambience = ambience;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder contrast(final int contrast) {
            this.contrast = contrast;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder originalColours(final short[] originalColours) {
            this.originalColours = originalColours;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder replacementColours(final short[] replacementColours) {
            this.replacementColours = replacementColours;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder originalTextures(final short[] originalTextures) {
            this.originalTextures = originalTextures;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder replacementTextures(final short[] replacementTextures) {
            this.replacementTextures = replacementTextures;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder field3568(final int field3568) {
            this.field3568 = field3568;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder field3580(final int field3580) {
            this.field3580 = field3580;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder parameters(final Int2ObjectOpenHashMap<Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions.NPCDefinitionsBuilder finalTransmogrification(final int finalTransmogrification) {
            this.finalTransmogrification = finalTransmogrification;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public NPCDefinitions build() {
            return new NPCDefinitions(this.id, this.name, this.lowercaseName, this.options, this.filteredOptions, this.filterFlag, this.varp, this.varbit, this.transmogrifiedIds, this.models, this.chatModels, this.standAnimation, this.walkAnimation, this.rotate90Animation, this.rotate180Animation, this.rotate270Animation, this.size, this.combatLevel, this.minimapVisible, this.visible, this.clickable, this.clippedMovement, this.isFamiliar, this.resizeX, this.resizeY, this.direction, this.headIcon, this.ambience, this.contrast, this.originalColours, this.replacementColours, this.originalTextures, this.replacementTextures, this.field3568, this.field3580, this.parameters, this.finalTransmogrification);
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "NPCDefinitions.NPCDefinitionsBuilder(id=" + this.id + ", name=" + this.name + ", lowercaseName=" + this.lowercaseName + ", options=" + java.util.Arrays.deepToString(this.options) + ", filteredOptions=" + java.util.Arrays.deepToString(this.filteredOptions) + ", filterFlag=" + this.filterFlag + ", varp=" + this.varp + ", varbit=" + this.varbit + ", transmogrifiedIds=" + java.util.Arrays.toString(this.transmogrifiedIds) + ", models=" + java.util.Arrays.toString(this.models) + ", chatModels=" + java.util.Arrays.toString(this.chatModels) + ", standAnimation=" + this.standAnimation + ", walkAnimation=" + this.walkAnimation + ", rotate90Animation=" + this.rotate90Animation + ", rotate180Animation=" + this.rotate180Animation + ", rotate270Animation=" + this.rotate270Animation + ", size=" + this.size + ", combatLevel=" + this.combatLevel + ", minimapVisible=" + this.minimapVisible + ", visible=" + this.visible + ", clickable=" + this.clickable + ", clippedMovement=" + this.clippedMovement + ", isFamiliar=" + this.isFamiliar + ", resizeX=" + this.resizeX + ", resizeY=" + this.resizeY + ", direction=" + this.direction + ", headIcon=" + this.headIcon + ", ambience=" + this.ambience + ", contrast=" + this.contrast + ", originalColours=" + java.util.Arrays.toString(this.originalColours) + ", replacementColours=" + java.util.Arrays.toString(this.replacementColours) + ", originalTextures=" + java.util.Arrays.toString(this.originalTextures) + ", replacementTextures=" + java.util.Arrays.toString(this.replacementTextures) + ", field3568=" + this.field3568 + ", field3580=" + this.field3580 + ", parameters=" + this.parameters + ", finalTransmogrification=" + this.finalTransmogrification + ")";
        }
    }

    @org.jetbrains.annotations.NotNull
    public static NPCDefinitions.NPCDefinitionsBuilder builder() {
        return new NPCDefinitions.NPCDefinitionsBuilder();
    }

    @org.jetbrains.annotations.NotNull
    public NPCDefinitions.NPCDefinitionsBuilder toBuilder() {
        return new NPCDefinitions.NPCDefinitionsBuilder().id(this.id).name(this.name).lowercaseName(this.lowercaseName).options(this.options).filteredOptions(this.filteredOptions).filterFlag(this.filterFlag).varp(this.varp).varbit(this.varbit).transmogrifiedIds(this.transmogrifiedIds).models(this.models).chatModels(this.chatModels).standAnimation(this.standAnimation).walkAnimation(this.walkAnimation).rotate90Animation(this.rotate90Animation).rotate180Animation(this.rotate180Animation).rotate270Animation(this.rotate270Animation).size(this.size).combatLevel(this.combatLevel).minimapVisible(this.minimapVisible).visible(this.visible).clickable(this.clickable).clippedMovement(this.clippedMovement).isFamiliar(this.isFamiliar).resizeX(this.resizeX).resizeY(this.resizeY).direction(this.direction).headIcon(this.headIcon).ambience(this.ambience).contrast(this.contrast).originalColours(this.originalColours).replacementColours(this.replacementColours).originalTextures(this.originalTextures).replacementTextures(this.replacementTextures).field3568(this.field3568).field3580(this.field3580).parameters(this.parameters).finalTransmogrification(this.finalTransmogrification);
    }

    public NPCDefinitions() {
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "NPCDefinitions(id=" + this.getId() + ", name=" + this.getName() + ", lowercaseName=" + this.getLowercaseName() + ", options=" + java.util.Arrays.deepToString(this.getOptions()) + ", filteredOptions=" + java.util.Arrays.deepToString(this.getFilteredOptions()) + ", filterFlag=" + this.getFilterFlag() + ", varp=" + this.getVarp() + ", varbit=" + this.getVarbit() + ", transmogrifiedIds=" + java.util.Arrays.toString(this.getTransmogrifiedIds()) + ", models=" + java.util.Arrays.toString(this.getModels()) + ", chatModels=" + java.util.Arrays.toString(this.getChatModels()) + ", standAnimation=" + this.getStandAnimation() + ", walkAnimation=" + this.getWalkAnimation() + ", rotate90Animation=" + this.getRotate90Animation() + ", rotate180Animation=" + this.getRotate180Animation() + ", rotate270Animation=" + this.getRotate270Animation() + ", size=" + this.getSize() + ", combatLevel=" + this.getCombatLevel() + ", minimapVisible=" + this.isMinimapVisible() + ", visible=" + this.isVisible() + ", clickable=" + this.isClickable() + ", clippedMovement=" + this.isClippedMovement() + ", isFamiliar=" + this.isFamiliar() + ", resizeX=" + this.getResizeX() + ", resizeY=" + this.getResizeY() + ", direction=" + this.getDirection() + ", headIcon=" + this.getHeadIcon() + ", ambience=" + this.getAmbience() + ", contrast=" + this.getContrast() + ", originalColours=" + java.util.Arrays.toString(this.getOriginalColours()) + ", replacementColours=" + java.util.Arrays.toString(this.getReplacementColours()) + ", originalTextures=" + java.util.Arrays.toString(this.getOriginalTextures()) + ", replacementTextures=" + java.util.Arrays.toString(this.getReplacementTextures()) + ", field3568=" + this.getField3568() + ", field3580=" + this.getField3580() + ", parameters=" + this.getParameters() + ", finalTransmogrification=" + this.getFinalTransmogrification() + ")";
    }

    public NPCDefinitions(final int id, final String name, final String lowercaseName, final String[] options, final String[] filteredOptions, final int filterFlag, final int varp, final int varbit, final int[] transmogrifiedIds, final int[] models, final int[] chatModels, final int standAnimation, final int walkAnimation, final int rotate90Animation, final int rotate180Animation, final int rotate270Animation, final int size, final int combatLevel, final boolean minimapVisible, final boolean visible, final boolean clickable, final boolean clippedMovement, final boolean isFamiliar, final int resizeX, final int resizeY, final int direction, final int headIcon, final int ambience, final int contrast, final short[] originalColours, final short[] replacementColours, final short[] originalTextures, final short[] replacementTextures, final int field3568, final int field3580, final Int2ObjectOpenHashMap<Object> parameters, final int finalTransmogrification) {
        this.id = id;
        this.name = name;
        this.lowercaseName = lowercaseName;
        this.options = options;
        this.filteredOptions = filteredOptions;
        this.filterFlag = filterFlag;
        this.varp = varp;
        this.varbit = varbit;
        this.transmogrifiedIds = transmogrifiedIds;
        this.models = models;
        this.chatModels = chatModels;
        this.standAnimation = standAnimation;
        this.walkAnimation = walkAnimation;
        this.rotate90Animation = rotate90Animation;
        this.rotate180Animation = rotate180Animation;
        this.rotate270Animation = rotate270Animation;
        this.size = size;
        this.combatLevel = combatLevel;
        this.minimapVisible = minimapVisible;
        this.visible = visible;
        this.clickable = clickable;
        this.clippedMovement = clippedMovement;
        this.isFamiliar = isFamiliar;
        this.resizeX = resizeX;
        this.resizeY = resizeY;
        this.direction = direction;
        this.headIcon = headIcon;
        this.ambience = ambience;
        this.contrast = contrast;
        this.originalColours = originalColours;
        this.replacementColours = replacementColours;
        this.originalTextures = originalTextures;
        this.replacementTextures = replacementTextures;
        this.field3568 = field3568;
        this.field3580 = field3580;
        this.parameters = parameters;
        this.finalTransmogrification = finalTransmogrification;
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLowercaseName() {
        return this.lowercaseName;
    }

    public String[] getOptions() {
        return this.options;
    }

    public void setOptions(final String[] options) {
        this.options = options;
    }

    public void setFilteredOptions(final String[] filteredOptions) {
        this.filteredOptions = filteredOptions;
    }

    public String[] getFilteredOptions() {
        return this.filteredOptions;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public int getVarp() {
        return this.varp;
    }

    public int getVarbit() {
        return this.varbit;
    }

    public void setVarp(final int varp) {
        this.varp = varp;
    }

    public void setVarbit(final int varbit) {
        this.varbit = varbit;
    }

    public int[] getTransmogrifiedIds() {
        return this.transmogrifiedIds;
    }

    public int[] getModels() {
        return this.models;
    }

    public int[] getChatModels() {
        return this.chatModels;
    }

    public void setTransmogrifiedIds(final int[] transmogrifiedIds) {
        this.transmogrifiedIds = transmogrifiedIds;
    }

    public void setModels(final int[] models) {
        this.models = models;
    }

    public void setChatModels(final int[] chatModels) {
        this.chatModels = chatModels;
    }

    public int getStandAnimation() {
        return this.standAnimation;
    }

    public int getWalkAnimation() {
        return this.walkAnimation;
    }

    public int getRotate90Animation() {
        return this.rotate90Animation;
    }

    public int getRotate180Animation() {
        return this.rotate180Animation;
    }

    public int getRotate270Animation() {
        return this.rotate270Animation;
    }

    public void setStandAnimation(final int standAnimation) {
        this.standAnimation = standAnimation;
    }

    public void setWalkAnimation(final int walkAnimation) {
        this.walkAnimation = walkAnimation;
    }

    public void setRotate90Animation(final int rotate90Animation) {
        this.rotate90Animation = rotate90Animation;
    }

    public void setRotate180Animation(final int rotate180Animation) {
        this.rotate180Animation = rotate180Animation;
    }

    public void setRotate270Animation(final int rotate270Animation) {
        this.rotate270Animation = rotate270Animation;
    }

    public int getSize() {
        return this.size;
    }

    public int getCombatLevel() {
        return this.combatLevel;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public void setCombatLevel(final int combatLevel) {
        this.combatLevel = combatLevel;
    }

    public boolean isMinimapVisible() {
        return this.minimapVisible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isClickable() {
        return this.clickable;
    }

    public boolean isClippedMovement() {
        return this.clippedMovement;
    }

    public boolean isFamiliar() {
        return this.isFamiliar;
    }

    public void setMinimapVisible(final boolean minimapVisible) {
        this.minimapVisible = minimapVisible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public void setClickable(final boolean clickable) {
        this.clickable = clickable;
    }

    public void setClippedMovement(final boolean clippedMovement) {
        this.clippedMovement = clippedMovement;
    }

    public void setFamiliar(final boolean isFamiliar) {
        this.isFamiliar = isFamiliar;
    }

    public int getResizeX() {
        return this.resizeX;
    }

    public int getResizeY() {
        return this.resizeY;
    }

    public int getDirection() {
        return this.direction;
    }

    public int getHeadIcon() {
        return this.headIcon;
    }

    public int getAmbience() {
        return this.ambience;
    }

    public int getContrast() {
        return this.contrast;
    }

    public void setResizeX(final int resizeX) {
        this.resizeX = resizeX;
    }

    public void setResizeY(final int resizeY) {
        this.resizeY = resizeY;
    }

    public void setDirection(final int direction) {
        this.direction = direction;
    }

    public void setHeadIcon(final int headIcon) {
        this.headIcon = headIcon;
    }

    public void setAmbience(final int ambience) {
        this.ambience = ambience;
    }

    public void setContrast(final int contrast) {
        this.contrast = contrast;
    }

    public short[] getOriginalColours() {
        return this.originalColours;
    }

    public short[] getReplacementColours() {
        return this.replacementColours;
    }

    public short[] getOriginalTextures() {
        return this.originalTextures;
    }

    public short[] getReplacementTextures() {
        return this.replacementTextures;
    }

    public void setOriginalColours(final short[] originalColours) {
        this.originalColours = originalColours;
    }

    public void setReplacementColours(final short[] replacementColours) {
        this.replacementColours = replacementColours;
    }

    public void setOriginalTextures(final short[] originalTextures) {
        this.originalTextures = originalTextures;
    }

    public void setReplacementTextures(final short[] replacementTextures) {
        this.replacementTextures = replacementTextures;
    }

    public int getField3568() {
        return this.field3568;
    }

    public int getField3580() {
        return this.field3580;
    }

    public Int2ObjectOpenHashMap<Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(final Int2ObjectOpenHashMap<Object> parameters) {
        this.parameters = parameters;
    }

    public int getFinalTransmogrification() {
        return this.finalTransmogrification;
    }
}
