package mgi.types.config.items;

import com.zenyte.Game;
import com.zenyte.GameEngine;
import com.zenyte.game.content.grandexchange.JSONGEItemDefinitionsLoader;
import com.zenyte.game.item.ItemExamineLoader;
import com.zenyte.game.parser.impl.ItemRequirements;
import com.zenyte.game.parser.impl.JSONItemDefinitionsLoader;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.masks.RenderAnimation;
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentType;
import com.zenyte.plugins.PluginManager;
import com.zenyte.plugins.events.ItemDefinitionsLoadedEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mgi.tools.jagcached.ArchiveType;
import mgi.tools.jagcached.GroupType;
import mgi.tools.jagcached.cache.File;
import mgi.types.Definitions;
import mgi.utilities.ByteBuffer;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Kris | 22. jaan 2018 : 21:35.27
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class ItemDefinitions implements Definitions, Cloneable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ItemDefinitions.class);
    private static final ItemDefinitions DEFAULT = new ItemDefinitions(-1);
    private static final IntOpenHashSet packedIds = new IntOpenHashSet();
    public static ItemDefinitions[] definitions;

    public static final int getSellPrice(final int itemId) {
        final mgi.types.config.items.ItemDefinitions definitions = ItemDefinitions.get(itemId);
        if (definitions == null) {
            return 0;
        }
        final boolean noted = definitions.isNoted();
        final int id = noted ? definitions.getNotedId() : itemId;
        final com.zenyte.game.content.grandexchange.JSONGEItemDefinitions gePrice = JSONGEItemDefinitionsLoader.lookup(id);
        return gePrice != null && gePrice.getPrice() != 0 ? gePrice.getPrice() : definitions.getPrice();
    }

    @Override
    public void load() {
        final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
        final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
        final mgi.tools.jagcached.cache.Group items = configs.findGroupByID(GroupType.ITEM);
        definitions = new ItemDefinitions[items.getHighestFileId()];
        for (int id = 0; id < items.getHighestFileId(); id++) {
            final mgi.tools.jagcached.cache.File file = items.findFileByID(id);
            if (file == null) {
                continue;
            }
            final mgi.utilities.ByteBuffer buffer = file.getData();
            if (buffer == null) {
                continue;
            }
            definitions[id] = new ItemDefinitions(id, buffer);
        }
        for (int id = 0; id < items.getHighestFileId(); id++) {
            final mgi.types.config.items.ItemDefinitions defs = get(id);
            if (defs == null || defs.notedTemplate == -1) {
                continue;
            }
            defs.toNote();
        }
        GameEngine.appendPostLoadTask(() -> PluginManager.post(new ItemDefinitionsLoadedEvent()));
    }

    public static boolean isValid(final int id) {
        return id >= 0 && id < definitions.length;
    }

    public static boolean isInvalid(final int id) {
        return id < 0 || id >= definitions.length;
    }

    public final List<String> printFields() {
        final List<String> strings = new ArrayList<String>(getClass().getDeclaredFields().length);
        for (final Field field : getClass().getDeclaredFields()) {
            if ((field.getModifiers() & 8) != 0) {
                continue;
            }
            try {
                final Object val = getValue(field);
                if (val == DEFAULT) {
                    continue;
                }
                final String[] fieldName = field.getName().split("(?=[A-Z])");
                final StringBuilder fieldBuilder = new StringBuilder();
                fieldBuilder.append(Utils.formatString(fieldName[0]));
                for (int i = 1; i < fieldName.length; i++) {
                    fieldBuilder.append(" " + (fieldName[i].length() == 1 ? fieldName[i].toUpperCase() : fieldName[i].toLowerCase()));
                }
                strings.add(fieldBuilder.toString() + ": " + val);
                System.out.println(fieldBuilder.toString() + ": " + val);
            } catch (final Throwable e) {
                log.error(Strings.EMPTY, e);
            }
        }
        return strings;
    }

    private final Object getValue(final Field field) throws Throwable {
        field.setAccessible(true);
        final Class<?> type = field.getType();
        if (field.get(this) == null || field.get(this).equals(DEFAULT.getClass().getDeclaredField(field.getName()).get(DEFAULT))) {
            return DEFAULT;
        }
        if (type == int[][].class) {
            return Arrays.toString((int[][]) field.get(this));
        } else if (type == int[].class) {
            return Arrays.toString((int[]) field.get(this));
        } else if (type == byte[].class) {
            return Arrays.toString((byte[]) field.get(this));
        } else if (type == short[].class) {
            return Arrays.toString((short[]) field.get(this));
        } else if (type == double[].class) {
            return Arrays.toString((double[]) field.get(this));
        } else if (type == float[].class) {
            return Arrays.toString((float[]) field.get(this));
        } else if (type == String[].class) {
            if (field.get(this) == null) {
                return "null";
            }
            return "[" + String.join(", ", (String[]) field.get(this)) + "]";
        } else if (type == Object[].class) {
            return Arrays.toString((Object[]) field.get(this));
        }
        return field.get(this);
    }

    private String name;
    private transient String lowercaseName;
    private int id;
    private String[] inventoryOptions;
    private String[] groundOptions;
    private boolean grandExchange;
    private boolean isMembers;
    private int isStackable;
    private int price;
    private int notedTemplate;
    private int notedId;
    private int bindTemplateId;
    private int bindId;
    private int placeholderTemplate;
    private int placeholderId;
    private int[] stackIds;
    private int[] stackAmounts;
    private int maleOffset;
    private int primaryMaleHeadModelId;
    private int secondaryMaleHeadModelId;
    private int primaryMaleModel;
    private int secondaryMaleModel;
    private int tertiaryMaleModel;
    private int femaleOffset;
    private int primaryFemaleHeadModelId;
    private int secondaryFemaleHeadModelId;
    private int primaryFemaleModel;
    private int secondaryFemaleModel;
    private int tertiaryFemaleModel;
    private int inventoryModelId;
    private int shiftClickIndex;
    private int teamId;
    private int zoom;
    private int offsetX;
    private int offsetY;
    private int modelPitch;
    private int modelRoll;
    private int modelYaw;
    private int resizeX;
    private int resizeY;
    private int resizeZ;
    private short[] originalColours;
    private short[] replacementColours;
    private short[] originalTextureIds;
    private short[] replacementTextureIds;
    private int ambient;
    private int contrast;
    private Int2ObjectOpenHashMap<Object> parameters;
    private String examine;
    private float weight;
    private int slot = -1;
    // @Getter
    // private HashMap<Integer, Integer> requirements;
    private int[] bonuses;
    private EquipmentType equipmentType;
    private boolean twoHanded;
    private int blockAnimation;
    private int standAnimation = RenderAnimation.STAND;
    private int walkAnimation = RenderAnimation.WALK;
    private int runAnimation = RenderAnimation.RUN;
    private int standTurnAnimation = RenderAnimation.STAND_TURN;
    private int rotate90Animation = RenderAnimation.ROTATE90;
    private int rotate180Animation = RenderAnimation.ROTATE180;
    private int rotate270Animation = RenderAnimation.ROTATE270;
    private int accurateAnimation;
    private int aggressiveAnimation;
    private int controlledAnimation;
    private int defensiveAnimation;
    private int attackSpeed;
    private int interfaceVarbit;
    private int normalAttackDistance;
    private int longAttackDistance;

    public int getNotedOrDefault() {
        if (isNoted() || notedId == -1) {
            return id;
        }
        return notedId;
    }

    public int getUnnotedOrDefault() {
        if (isNoted()) {
            return notedId;
        }
        return id;
    }

    public ItemRequirements.ItemRequirement getRequirements() {
        return ItemRequirements.getRequirement(id);
    }

    public boolean containsOption(final String option) {
        if (inventoryOptions == null) {
            return false;
        }
        for (final String o : inventoryOptions) {
            if (o == null || !o.equalsIgnoreCase(option)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public int getSlotForOption(final String option) {
        if (inventoryOptions == null) {
            return -1;
        }
        for (int i = 0; i < inventoryOptions.length; i++) {
            final java.lang.String o = inventoryOptions[i];
            if (o == null || !o.equalsIgnoreCase(option)) {
                continue;
            }
            return i + 1;
        }
        return -1;
    }

    public String getOption(final int option) {
        if (inventoryOptions == null) {
            return null;
        }
        if (option < 0 || option >= inventoryOptions.length) {
            return null;
        }
        return inventoryOptions[option];
    }

    private static boolean loaded;

    public static final void loadDefinitions() {
        if (loaded) {
            return;
        }
        loaded = true;
        final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> list = new ArrayList<Callable<Void>>();
        list.add(() -> {
            try {
                new JSONItemDefinitionsLoader().parse();
            } catch (Throwable throwable) {
                log.error(Strings.EMPTY, throwable);
            }
            return null;
        });
        list.add(() -> {
            try {
                new ItemExamineLoader().parse();
            } catch (Throwable throwable) {
                log.error(Strings.EMPTY, throwable);
            }
            return null;
        });
        list.add(() -> {
            try {
                ItemRequirements.parse();
            } catch (Throwable throwable) {
                log.error(Strings.EMPTY, throwable);
            }
            return null;
        });
        final java.util.concurrent.ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invokeAll(list);
        list.clear();
        try {
            final mgi.tools.jagcached.cache.Cache cache = Game.getCacheMgi();
            final mgi.tools.jagcached.cache.Archive configs = cache.getArchive(ArchiveType.CONFIGS);
            final mgi.tools.jagcached.cache.Group items = configs.findGroupByID(GroupType.ITEM);
            final int length = items.getHighestFileId();
            for (int itemId = 0; itemId < length; itemId++) {
                final mgi.types.config.items.ItemDefinitions def = get(itemId);
                if (def == null) {
                    continue;
                }
                final com.zenyte.game.util.Examine examine = ItemExamineLoader.DEFINITIONS.get(itemId);
                if (examine != null) {
                    def.examine = examine.getExamine();
                }
                final mgi.types.config.items.JSONItemDefinitions jsonDefs = JSONItemDefinitionsLoader.lookup(itemId);
                if (jsonDefs == null) {
                    continue;
                }
                final mgi.types.config.items.WearableDefinition wearDef = jsonDefs.getEquipmentDefinition();
                def.weight = jsonDefs.getWeight();
                def.slot = jsonDefs.getSlot();
                // if (jsonDefs.getTradable() != null) {
                // def.tradable = jsonDefs.getTradable();
                // }
                def.equipmentType = jsonDefs.getEquipmentType();
                if (wearDef != null) {
                    // def.requirements = wearDef.getRequirements();
                    final String bonuses = wearDef.getBonuses();
                    final String[] splitBonuses = bonuses.split(", ");
                    try {
                        def.bonuses = new int[splitBonuses.length];
                        for (int i = 0; i < splitBonuses.length; i++) {
                            def.bonuses[i] = Integer.valueOf(splitBonuses[i]);
                        }
                    } catch (final Exception e) {
                        log.error(Strings.EMPTY, e);
                    }
                    final WieldableDefinition wieldDef = wearDef.getWeaponDefinition();
                    if (wieldDef != null) {
                        def.twoHanded = wieldDef.isTwoHanded();
                        if (wieldDef.getBlockAnimation() != 0) {
                            def.blockAnimation = wieldDef.getBlockAnimation();
                        }
                        if (wieldDef.getStandAnimation() != 0) {
                            def.standAnimation = wieldDef.getStandAnimation();
                        }
                        if (wieldDef.getWalkAnimation() != 0) {
                            def.walkAnimation = wieldDef.getWalkAnimation();
                        }
                        if (wieldDef.getRunAnimation() != 0) {
                            def.runAnimation = wieldDef.getRunAnimation();
                        }
                        if (wieldDef.getStandTurnAnimation() != 0) {
                            def.standTurnAnimation = wieldDef.getStandTurnAnimation();
                        }
                        if (wieldDef.getRotate90Animation() != 0) {
                            def.rotate90Animation = wieldDef.getRotate90Animation();
                        }
                        if (wieldDef.getRotate180Animation() != 0) {
                            def.rotate180Animation = wieldDef.getRotate180Animation();
                        }
                        if (wieldDef.getRotate270Animation() != 0) {
                            def.rotate270Animation = wieldDef.getRotate270Animation();
                        }
                        def.accurateAnimation = wieldDef.getAccurateAnimation();
                        def.aggressiveAnimation = wieldDef.getAggressiveAnimation();
                        def.controlledAnimation = wieldDef.getControlledAnimation();
                        def.defensiveAnimation = wieldDef.getDefensiveAnimation();
                        def.attackSpeed = wieldDef.getAttackSpeed();
                        def.interfaceVarbit = wieldDef.getInterfaceVarbit();
                        def.normalAttackDistance = wieldDef.getNormalAttackDistance();
                        def.longAttackDistance = wieldDef.getLongAttackDistance();
                    }
                }
            }
        } catch (final Exception e) {
            log.error(Strings.EMPTY, e);
        }
    }

    public ItemDefinitions(final int id) {
        this.id = id;
        setDefaults();
    }

    public ItemDefinitions(final int id, final ByteBuffer buffer) {
        this.id = id;
        setDefaults();
        decode(buffer);
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
            inventoryModelId = buffer.readUnsignedShort();
            return;
        case 2: 
            name = buffer.readString();
            lowercaseName = name.toLowerCase();
            return;
        case 4: 
            zoom = buffer.readUnsignedShort();
            return;
        case 5: 
            modelPitch = buffer.readUnsignedShort();
            return;
        case 6: 
            modelRoll = buffer.readUnsignedShort();
            return;
        case 7: 
            offsetX = buffer.readUnsignedShort();
            if (offsetX > 32767) {
                offsetX -= 65536;
            }
            return;
        case 8: 
            offsetY = buffer.readUnsignedShort();
            if (offsetY > 32767) {
                offsetY -= 65536;
            }
            return;
        case 11: 
            isStackable = 1;
            return;
        case 12: 
            price = buffer.readInt();
            return;
        case 16: 
            isMembers = true;
            return;
        case 23: 
            primaryMaleModel = buffer.readUnsignedShort();
            maleOffset = buffer.readUnsignedByte();
            return;
        case 24: 
            secondaryMaleModel = buffer.readUnsignedShort();
            return;
        case 25: 
            primaryFemaleModel = buffer.readUnsignedShort();
            femaleOffset = buffer.readUnsignedByte();
            return;
        case 26: 
            secondaryFemaleModel = buffer.readUnsignedShort();
            return;
        case 30: 
        case 31: 
        case 32: 
        case 33: 
        case 34: 
            groundOptions[opcode - 30] = buffer.readString();
            if (groundOptions[opcode - 30].equalsIgnoreCase("Hidden")) {
                groundOptions[opcode - 30] = null;
            }
            return;
        case 35: 
        case 36: 
        case 37: 
        case 38: 
        case 39: 
            inventoryOptions[opcode - 35] = buffer.readString();
            return;
        case 40: 
            {
                final int amount = buffer.readUnsignedByte();
                originalColours = new short[amount];
                replacementColours = new short[amount];
                for (int index = 0; index < amount; index++) {
                    originalColours[index] = (short) (buffer.readUnsignedShort());
                    replacementColours[index] = (short) (buffer.readUnsignedShort());
                }
                return;
            }
        case 41: 
            {
                final int amount = buffer.readUnsignedByte();
                originalTextureIds = new short[amount];
                replacementTextureIds = new short[amount];
                for (int index = 0; index < amount; index++) {
                    originalTextureIds[index] = (short) (buffer.readUnsignedShort());
                    replacementTextureIds[index] = (short) (buffer.readUnsignedShort());
                }
                return;
            }
        case 42: 
            shiftClickIndex = buffer.readByte();
            return;
        case 65: 
            grandExchange = true;
            return;
        case 78: 
            tertiaryMaleModel = buffer.readUnsignedShort();
            return;
        case 79: 
            tertiaryFemaleModel = buffer.readUnsignedShort();
            return;
        case 90: 
            primaryMaleHeadModelId = buffer.readUnsignedShort();
            return;
        case 91: 
            primaryFemaleHeadModelId = buffer.readUnsignedShort();
            return;
        case 92: 
            secondaryMaleHeadModelId = buffer.readUnsignedShort();
            return;
        case 93: 
            secondaryFemaleHeadModelId = buffer.readUnsignedShort();
            return;
        case 95: 
            modelYaw = buffer.readUnsignedShort();
            return;
        case 97: 
            notedId = buffer.readUnsignedShort();
            return;
        case 98: 
            notedTemplate = buffer.readUnsignedShort();
            return;
        case 100: 
        case 101: 
        case 102: 
        case 103: 
        case 104: 
        case 105: 
        case 106: 
        case 107: 
        case 108: 
        case 109: 
            if (stackIds == null) {
                stackIds = new int[10];
                stackAmounts = new int[10];
            }
            stackIds[opcode - 100] = buffer.readUnsignedShort();
            stackAmounts[opcode - 100] = buffer.readUnsignedShort();
            return;
        case 110: 
            resizeX = buffer.readUnsignedShort();
            return;
        case 111: 
            resizeY = buffer.readUnsignedShort();
            return;
        case 112: 
            resizeZ = buffer.readUnsignedShort();
            return;
        case 113: 
            ambient = buffer.readByte();
            return;
        case 114: 
            contrast = buffer.readByte();
            return;
        case 115: 
            teamId = buffer.readUnsignedByte();
            return;
        case 139: 
            bindId = buffer.readUnsignedShort();
            return;
        case 140: 
            bindTemplateId = buffer.readUnsignedShort();
            return;
        case 148: 
            placeholderId = buffer.readUnsignedShort();
            return;
        case 149: 
            placeholderTemplate = buffer.readUnsignedShort();
            return;
        case 249: 
            parameters = buffer.readParameters();
            return;
        }
    }

    private void setDefaults() {
        name = lowercaseName = "null";
        zoom = 2000;
        modelPitch = 0;
        modelRoll = 0;
        modelYaw = 0;
        offsetX = 0;
        offsetY = 0;
        isStackable = 0;
        price = 1;
        isMembers = false;
        groundOptions = new String[] {null, null, "Take", null, null};
        inventoryOptions = new String[] {null, null, null, null, "Drop"};
        shiftClickIndex = -2;
        primaryMaleModel = -1;
        secondaryMaleModel = -1;
        maleOffset = 0;
        primaryFemaleModel = -1;
        secondaryFemaleModel = -1;
        femaleOffset = 0;
        tertiaryMaleModel = -1;
        tertiaryFemaleModel = -1;
        primaryMaleHeadModelId = -1;
        secondaryMaleHeadModelId = -1;
        primaryFemaleHeadModelId = -1;
        secondaryFemaleHeadModelId = -1;
        notedId = -1;
        notedTemplate = -1;
        resizeX = 128;
        resizeY = 128;
        resizeZ = 128;
        ambient = 0;
        contrast = 0;
        teamId = 0;
        grandExchange = false;
        bindId = -1;
        bindTemplateId = -1;
        placeholderId = -1;
        placeholderTemplate = -1;
    }

    public static ItemDefinitions get(final int id) {
        if (id < 0 || id >= definitions.length) {
            //throw new RuntimeException("Invalid item requested.");
            return null;//cant throw an exception because so much code is unable to handle exceptions.
        }
        return definitions[id];
    }

    public static ItemDefinitions getOrThrow(final int id) {
        if (id < 0 || id >= definitions.length) {
            throw new RuntimeException("Invalid item requested.");
        }
        return definitions[id];
    }

    public static final String nameOf(final int id) {
        final mgi.types.config.items.ItemDefinitions def = get(id);
        return def == null ? "null" : Utils.getOrDefault(def.getName(), "null");
    }

    public boolean isPlaceholder() {
        return placeholderTemplate != -1;
    }

    public boolean isNoted() {
        return notedTemplate != -1;
    }

    public boolean isStackable() {
        return isStackable == 1 || isNoted();
    }

    public int getPrice() {
        if (isNoted()) {
            return get(getNotedId()).getPrice();
        }
        return price;
    }

    private void toNote() {
        final ItemDefinitions realItem = get(notedId);
        isMembers = realItem.isMembers;
        price = realItem.price;
        name = realItem.name;
        grandExchange = realItem.grandExchange;
        isStackable = 1;
    }

    public int getHighAlchPrice() {
        return (int) (getPrice() * 0.6);
    }

    public String getStringParam(final int key) {
        if (parameters == null) {
            return "null";
        }
        final java.lang.Object object = parameters.get(key);
        if (!(object instanceof String)) {
            return "null";
        }
        return (String) object;
    }

    public int getIntParam(final int key) {
        if (parameters == null) {
            return -1;
        }
        final java.lang.Object object = parameters.get(key);
        if (!(object instanceof Integer)) {
            return -1;
        }
        return (Integer) object;
    }

    public boolean containsParamByValue(final Object value) {
        if (parameters == null) {
            return false;
        }
        final it.unimi.dsi.fastutil.objects.ObjectIterator<java.lang.Object> iterator = parameters.values().iterator();
        final java.lang.String lowercaseValue = value.toString().toLowerCase();
        while (iterator.hasNext()) {
            final java.lang.Object next = iterator.next();
            if (next.toString().toLowerCase().equals(lowercaseValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public mgi.utilities.ByteBuffer encode() {
        final mgi.utilities.ByteBuffer buffer = new ByteBuffer(512);
        buffer.writeByte(1);
        buffer.writeShort(inventoryModelId);
        if (!name.equals("null") && notedTemplate == -1) {
            buffer.writeByte(2);
            buffer.writeString(name);
        }
        if (zoom != 2000) {
            buffer.writeByte(4);
            buffer.writeShort(zoom);
        }
        if (modelPitch != 0) {
            buffer.writeByte(5);
            buffer.writeShort(modelPitch);
        }
        if (modelRoll != 0) {
            buffer.writeByte(6);
            buffer.writeShort(modelRoll);
        }
        if (offsetX != 0) {
            buffer.writeByte(7);
            buffer.writeShort(offsetX);
        }
        if (offsetY != 0) {
            buffer.writeByte(8);
            buffer.writeShort(offsetY);
        }
        if (isStackable == 1 && notedTemplate == -1) {
            buffer.writeByte(11);
        }
        if (price != 1 && notedTemplate == -1) {
            buffer.writeByte(12);
            buffer.writeInt(price);
        }
        if (isMembers && notedTemplate == -1) {
            buffer.writeByte(16);
        }
        if (primaryMaleModel != -1) {
            buffer.writeByte(23);
            buffer.writeShort(primaryMaleModel);
            buffer.writeByte(maleOffset);
        }
        if (secondaryMaleModel != -1) {
            buffer.writeByte(24);
            buffer.writeShort(secondaryMaleModel);
        }
        if (primaryFemaleModel != -1) {
            buffer.writeByte(25);
            buffer.writeShort(primaryFemaleModel);
            buffer.writeByte(femaleOffset);
        }
        if (secondaryFemaleModel != -1) {
            buffer.writeByte(26);
            buffer.writeShort(secondaryFemaleModel);
        }
        if (groundOptions != null) {
            for (int index = 0; index < 5; index++) {
                if (groundOptions[index] != null) {
                    buffer.writeByte((30 + index));
                    buffer.writeString(groundOptions[index]);
                }
            }
        }
        for (int index = 0; index < 5; index++) {
            if (inventoryOptions[index] != null) {
                buffer.writeByte((35 + index));
                buffer.writeString(inventoryOptions[index]);
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
        if (originalTextureIds != null && replacementTextureIds != null && originalTextureIds.length != 0 && replacementTextureIds.length != 0) {
            buffer.writeByte(41);
            buffer.writeByte(originalTextureIds.length);
            for (int index = 0; index < originalTextureIds.length; index++) {
                buffer.writeShort(originalTextureIds[index]);
                buffer.writeShort(replacementTextureIds[index]);
            }
        }
        if (shiftClickIndex != -1) {
            buffer.writeByte(42);
            buffer.writeByte(shiftClickIndex);
        }
        if (grandExchange) {
            buffer.writeByte(65);
        }
        if (tertiaryMaleModel != -1) {
            buffer.writeByte(78);
            buffer.writeShort(tertiaryMaleModel);
        }
        if (tertiaryFemaleModel != -1) {
            buffer.writeByte(79);
            buffer.writeShort(tertiaryFemaleModel);
        }
        if (primaryMaleHeadModelId != -1) {
            buffer.writeByte(90);
            buffer.writeShort(primaryMaleHeadModelId);
        }
        if (primaryFemaleHeadModelId != -1) {
            buffer.writeByte(91);
            buffer.writeShort(primaryFemaleHeadModelId);
        }
        if (secondaryMaleHeadModelId != -1) {
            buffer.writeByte(92);
            buffer.writeShort(secondaryMaleHeadModelId);
        }
        if (secondaryFemaleHeadModelId != -1) {
            buffer.writeByte(93);
            buffer.writeShort(secondaryFemaleHeadModelId);
        }
        if (modelYaw != -1) {
            buffer.writeByte(95);
            buffer.writeShort(modelYaw);
        }
        if (notedId != -1) {
            buffer.writeByte(97);
            buffer.writeShort(notedId);
        }
        if (notedTemplate != -1) {
            buffer.writeByte(98);
            buffer.writeShort(notedTemplate);
        }
        if (stackIds != null && stackAmounts != null && stackIds.length != 0 && stackAmounts.length != 0) {
            for (int index = 0; index < stackIds.length; index++) {
                if (stackIds[index] != 0 || stackAmounts[index] != 0) {
                    buffer.writeByte((100 + index));
                    buffer.writeShort(stackIds[index]);
                    buffer.writeShort(stackAmounts[index]);
                }
            }
        }
        if (resizeX != 128) {
            buffer.writeByte(110);
            buffer.writeShort(resizeX);
        }
        if (resizeY != 128) {
            buffer.writeByte(111);
            buffer.writeShort(resizeY);
        }
        if (resizeZ != 128) {
            buffer.writeByte(112);
            buffer.writeShort(resizeZ);
        }
        if (ambient != 1) {
            buffer.writeByte(113);
            buffer.writeByte(ambient);
        }
        if (contrast != 1) {
            buffer.writeByte(114);
            buffer.writeByte(contrast);
        }
        if (teamId != 1) {
            buffer.writeByte(115);
            buffer.writeByte(teamId);
        }
        if (bindId != -1) {
            buffer.writeByte(139);
            buffer.writeShort(bindId);
        }
        if (bindTemplateId != -1) {
            buffer.writeByte(140);
            buffer.writeShort(bindTemplateId);
        }
        if (placeholderId != -1) {
            buffer.writeByte(148);
            buffer.writeShort(placeholderId);
        }
        if (placeholderTemplate != -1) {
            buffer.writeByte(149);
            buffer.writeShort(placeholderTemplate);
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
        if (!packedIds.add(id)) {
            log.info("Overlapping an item in cachepacking: " + id);
        }
        Game.getCacheMgi().getArchive(ArchiveType.CONFIGS).findGroupByID(GroupType.ITEM).addFile(new File(id, encode()));
    }

    public void setOption(final int index, final String option) {
        if (inventoryOptions == null) {
            inventoryOptions = new String[5];
        }
        inventoryOptions[index] = option.isEmpty() ? null : option;
    }


    public static class ItemDefinitionsBuilder {
        private String name;
        private String lowercaseName;
        private int id;
        private String[] inventoryOptions;
        private String[] groundOptions;
        private boolean grandExchange;
        private boolean isMembers;
        private int isStackable;
        private int price;
        private int notedTemplate;
        private int notedId;
        private int bindTemplateId;
        private int bindId;
        private int placeholderTemplate;
        private int placeholderId;
        private int[] stackIds;
        private int[] stackAmounts;
        private int maleOffset;
        private int primaryMaleHeadModelId;
        private int secondaryMaleHeadModelId;
        private int primaryMaleModel;
        private int secondaryMaleModel;
        private int tertiaryMaleModel;
        private int femaleOffset;
        private int primaryFemaleHeadModelId;
        private int secondaryFemaleHeadModelId;
        private int primaryFemaleModel;
        private int secondaryFemaleModel;
        private int tertiaryFemaleModel;
        private int inventoryModelId;
        private int shiftClickIndex;
        private int teamId;
        private int zoom;
        private int offsetX;
        private int offsetY;
        private int modelPitch;
        private int modelRoll;
        private int modelYaw;
        private int resizeX;
        private int resizeY;
        private int resizeZ;
        private short[] originalColours;
        private short[] replacementColours;
        private short[] originalTextureIds;
        private short[] replacementTextureIds;
        private int ambient;
        private int contrast;
        private Int2ObjectOpenHashMap<Object> parameters;
        private String examine;
        private float weight;
        private int slot;
        private int[] bonuses;
        private EquipmentType equipmentType;
        private boolean twoHanded;
        private int blockAnimation;
        private int standAnimation;
        private int walkAnimation;
        private int runAnimation;
        private int standTurnAnimation;
        private int rotate90Animation;
        private int rotate180Animation;
        private int rotate270Animation;
        private int accurateAnimation;
        private int aggressiveAnimation;
        private int controlledAnimation;
        private int defensiveAnimation;
        private int attackSpeed;
        private int interfaceVarbit;
        private int normalAttackDistance;
        private int longAttackDistance;

        ItemDefinitionsBuilder() {
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder name(final String name) {
            this.name = name;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder lowercaseName(final String lowercaseName) {
            this.lowercaseName = lowercaseName;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder id(final int id) {
            this.id = id;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder inventoryOptions(final String[] inventoryOptions) {
            this.inventoryOptions = inventoryOptions;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder groundOptions(final String[] groundOptions) {
            this.groundOptions = groundOptions;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder grandExchange(final boolean grandExchange) {
            this.grandExchange = grandExchange;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder isMembers(final boolean isMembers) {
            this.isMembers = isMembers;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder isStackable(final int isStackable) {
            this.isStackable = isStackable;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder price(final int price) {
            this.price = price;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder notedTemplate(final int notedTemplate) {
            this.notedTemplate = notedTemplate;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder notedId(final int notedId) {
            this.notedId = notedId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder bindTemplateId(final int bindTemplateId) {
            this.bindTemplateId = bindTemplateId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder bindId(final int bindId) {
            this.bindId = bindId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder placeholderTemplate(final int placeholderTemplate) {
            this.placeholderTemplate = placeholderTemplate;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder placeholderId(final int placeholderId) {
            this.placeholderId = placeholderId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder stackIds(final int[] stackIds) {
            this.stackIds = stackIds;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder stackAmounts(final int[] stackAmounts) {
            this.stackAmounts = stackAmounts;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder maleOffset(final int maleOffset) {
            this.maleOffset = maleOffset;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder primaryMaleHeadModelId(final int primaryMaleHeadModelId) {
            this.primaryMaleHeadModelId = primaryMaleHeadModelId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder secondaryMaleHeadModelId(final int secondaryMaleHeadModelId) {
            this.secondaryMaleHeadModelId = secondaryMaleHeadModelId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder primaryMaleModel(final int primaryMaleModel) {
            this.primaryMaleModel = primaryMaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder secondaryMaleModel(final int secondaryMaleModel) {
            this.secondaryMaleModel = secondaryMaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder tertiaryMaleModel(final int tertiaryMaleModel) {
            this.tertiaryMaleModel = tertiaryMaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder femaleOffset(final int femaleOffset) {
            this.femaleOffset = femaleOffset;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder primaryFemaleHeadModelId(final int primaryFemaleHeadModelId) {
            this.primaryFemaleHeadModelId = primaryFemaleHeadModelId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder secondaryFemaleHeadModelId(final int secondaryFemaleHeadModelId) {
            this.secondaryFemaleHeadModelId = secondaryFemaleHeadModelId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder primaryFemaleModel(final int primaryFemaleModel) {
            this.primaryFemaleModel = primaryFemaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder secondaryFemaleModel(final int secondaryFemaleModel) {
            this.secondaryFemaleModel = secondaryFemaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder tertiaryFemaleModel(final int tertiaryFemaleModel) {
            this.tertiaryFemaleModel = tertiaryFemaleModel;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder inventoryModelId(final int inventoryModelId) {
            this.inventoryModelId = inventoryModelId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder shiftClickIndex(final int shiftClickIndex) {
            this.shiftClickIndex = shiftClickIndex;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder teamId(final int teamId) {
            this.teamId = teamId;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder zoom(final int zoom) {
            this.zoom = zoom;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder offsetX(final int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder offsetY(final int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder modelPitch(final int modelPitch) {
            this.modelPitch = modelPitch;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder modelRoll(final int modelRoll) {
            this.modelRoll = modelRoll;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder modelYaw(final int modelYaw) {
            this.modelYaw = modelYaw;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder resizeX(final int resizeX) {
            this.resizeX = resizeX;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder resizeY(final int resizeY) {
            this.resizeY = resizeY;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder resizeZ(final int resizeZ) {
            this.resizeZ = resizeZ;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder originalColours(final short[] originalColours) {
            this.originalColours = originalColours;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder replacementColours(final short[] replacementColours) {
            this.replacementColours = replacementColours;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder originalTextureIds(final short[] originalTextureIds) {
            this.originalTextureIds = originalTextureIds;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder replacementTextureIds(final short[] replacementTextureIds) {
            this.replacementTextureIds = replacementTextureIds;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder ambient(final int ambient) {
            this.ambient = ambient;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder contrast(final int contrast) {
            this.contrast = contrast;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder parameters(final Int2ObjectOpenHashMap<Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder examine(final String examine) {
            this.examine = examine;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder weight(final float weight) {
            this.weight = weight;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder slot(final int slot) {
            this.slot = slot;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder bonuses(final int[] bonuses) {
            this.bonuses = bonuses;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder equipmentType(final EquipmentType equipmentType) {
            this.equipmentType = equipmentType;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder twoHanded(final boolean twoHanded) {
            this.twoHanded = twoHanded;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder blockAnimation(final int blockAnimation) {
            this.blockAnimation = blockAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder standAnimation(final int standAnimation) {
            this.standAnimation = standAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder walkAnimation(final int walkAnimation) {
            this.walkAnimation = walkAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder runAnimation(final int runAnimation) {
            this.runAnimation = runAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder standTurnAnimation(final int standTurnAnimation) {
            this.standTurnAnimation = standTurnAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder rotate90Animation(final int rotate90Animation) {
            this.rotate90Animation = rotate90Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder rotate180Animation(final int rotate180Animation) {
            this.rotate180Animation = rotate180Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder rotate270Animation(final int rotate270Animation) {
            this.rotate270Animation = rotate270Animation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder accurateAnimation(final int accurateAnimation) {
            this.accurateAnimation = accurateAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder aggressiveAnimation(final int aggressiveAnimation) {
            this.aggressiveAnimation = aggressiveAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder controlledAnimation(final int controlledAnimation) {
            this.controlledAnimation = controlledAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder defensiveAnimation(final int defensiveAnimation) {
            this.defensiveAnimation = defensiveAnimation;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder attackSpeed(final int attackSpeed) {
            this.attackSpeed = attackSpeed;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder interfaceVarbit(final int interfaceVarbit) {
            this.interfaceVarbit = interfaceVarbit;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder normalAttackDistance(final int normalAttackDistance) {
            this.normalAttackDistance = normalAttackDistance;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions.ItemDefinitionsBuilder longAttackDistance(final int longAttackDistance) {
            this.longAttackDistance = longAttackDistance;
            return this;
        }

        @org.jetbrains.annotations.NotNull
        public ItemDefinitions build() {
            return new ItemDefinitions(this.name, this.lowercaseName, this.id, this.inventoryOptions, this.groundOptions, this.grandExchange, this.isMembers, this.isStackable, this.price, this.notedTemplate, this.notedId, this.bindTemplateId, this.bindId, this.placeholderTemplate, this.placeholderId, this.stackIds, this.stackAmounts, this.maleOffset, this.primaryMaleHeadModelId, this.secondaryMaleHeadModelId, this.primaryMaleModel, this.secondaryMaleModel, this.tertiaryMaleModel, this.femaleOffset, this.primaryFemaleHeadModelId, this.secondaryFemaleHeadModelId, this.primaryFemaleModel, this.secondaryFemaleModel, this.tertiaryFemaleModel, this.inventoryModelId, this.shiftClickIndex, this.teamId, this.zoom, this.offsetX, this.offsetY, this.modelPitch, this.modelRoll, this.modelYaw, this.resizeX, this.resizeY, this.resizeZ, this.originalColours, this.replacementColours, this.originalTextureIds, this.replacementTextureIds, this.ambient, this.contrast, this.parameters, this.examine, this.weight, this.slot, this.bonuses, this.equipmentType, this.twoHanded, this.blockAnimation, this.standAnimation, this.walkAnimation, this.runAnimation, this.standTurnAnimation, this.rotate90Animation, this.rotate180Animation, this.rotate270Animation, this.accurateAnimation, this.aggressiveAnimation, this.controlledAnimation, this.defensiveAnimation, this.attackSpeed, this.interfaceVarbit, this.normalAttackDistance, this.longAttackDistance);
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String toString() {
            return "ItemDefinitions.ItemDefinitionsBuilder(name=" + this.name + ", lowercaseName=" + this.lowercaseName + ", id=" + this.id + ", inventoryOptions=" + java.util.Arrays.deepToString(this.inventoryOptions) + ", groundOptions=" + java.util.Arrays.deepToString(this.groundOptions) + ", grandExchange=" + this.grandExchange + ", isMembers=" + this.isMembers + ", isStackable=" + this.isStackable + ", price=" + this.price + ", notedTemplate=" + this.notedTemplate + ", notedId=" + this.notedId + ", bindTemplateId=" + this.bindTemplateId + ", bindId=" + this.bindId + ", placeholderTemplate=" + this.placeholderTemplate + ", placeholderId=" + this.placeholderId + ", stackIds=" + java.util.Arrays.toString(this.stackIds) + ", stackAmounts=" + java.util.Arrays.toString(this.stackAmounts) + ", maleOffset=" + this.maleOffset + ", primaryMaleHeadModelId=" + this.primaryMaleHeadModelId + ", secondaryMaleHeadModelId=" + this.secondaryMaleHeadModelId + ", primaryMaleModel=" + this.primaryMaleModel + ", secondaryMaleModel=" + this.secondaryMaleModel + ", tertiaryMaleModel=" + this.tertiaryMaleModel + ", femaleOffset=" + this.femaleOffset + ", primaryFemaleHeadModelId=" + this.primaryFemaleHeadModelId + ", secondaryFemaleHeadModelId=" + this.secondaryFemaleHeadModelId + ", primaryFemaleModel=" + this.primaryFemaleModel + ", secondaryFemaleModel=" + this.secondaryFemaleModel + ", tertiaryFemaleModel=" + this.tertiaryFemaleModel + ", inventoryModelId=" + this.inventoryModelId + ", shiftClickIndex=" + this.shiftClickIndex + ", teamId=" + this.teamId + ", zoom=" + this.zoom + ", offsetX=" + this.offsetX + ", offsetY=" + this.offsetY + ", modelPitch=" + this.modelPitch + ", modelRoll=" + this.modelRoll + ", modelYaw=" + this.modelYaw + ", resizeX=" + this.resizeX + ", resizeY=" + this.resizeY + ", resizeZ=" + this.resizeZ + ", originalColours=" + java.util.Arrays.toString(this.originalColours) + ", replacementColours=" + java.util.Arrays.toString(this.replacementColours) + ", originalTextureIds=" + java.util.Arrays.toString(this.originalTextureIds) + ", replacementTextureIds=" + java.util.Arrays.toString(this.replacementTextureIds) + ", ambient=" + this.ambient + ", contrast=" + this.contrast + ", parameters=" + this.parameters + ", examine=" + this.examine + ", weight=" + this.weight + ", slot=" + this.slot + ", bonuses=" + java.util.Arrays.toString(this.bonuses) + ", equipmentType=" + this.equipmentType + ", twoHanded=" + this.twoHanded + ", blockAnimation=" + this.blockAnimation + ", standAnimation=" + this.standAnimation + ", walkAnimation=" + this.walkAnimation + ", runAnimation=" + this.runAnimation + ", standTurnAnimation=" + this.standTurnAnimation + ", rotate90Animation=" + this.rotate90Animation + ", rotate180Animation=" + this.rotate180Animation + ", rotate270Animation=" + this.rotate270Animation + ", accurateAnimation=" + this.accurateAnimation + ", aggressiveAnimation=" + this.aggressiveAnimation + ", controlledAnimation=" + this.controlledAnimation + ", defensiveAnimation=" + this.defensiveAnimation + ", attackSpeed=" + this.attackSpeed + ", interfaceVarbit=" + this.interfaceVarbit + ", normalAttackDistance=" + this.normalAttackDistance + ", longAttackDistance=" + this.longAttackDistance + ")";
        }
    }

    @org.jetbrains.annotations.NotNull
    public static ItemDefinitions.ItemDefinitionsBuilder builder() {
        return new ItemDefinitions.ItemDefinitionsBuilder();
    }

    @org.jetbrains.annotations.NotNull
    public ItemDefinitions.ItemDefinitionsBuilder toBuilder() {
        return new ItemDefinitions.ItemDefinitionsBuilder().name(this.name).lowercaseName(this.lowercaseName).id(this.id).inventoryOptions(this.inventoryOptions).groundOptions(this.groundOptions).grandExchange(this.grandExchange).isMembers(this.isMembers).isStackable(this.isStackable).price(this.price).notedTemplate(this.notedTemplate).notedId(this.notedId).bindTemplateId(this.bindTemplateId).bindId(this.bindId).placeholderTemplate(this.placeholderTemplate).placeholderId(this.placeholderId).stackIds(this.stackIds).stackAmounts(this.stackAmounts).maleOffset(this.maleOffset).primaryMaleHeadModelId(this.primaryMaleHeadModelId).secondaryMaleHeadModelId(this.secondaryMaleHeadModelId).primaryMaleModel(this.primaryMaleModel).secondaryMaleModel(this.secondaryMaleModel).tertiaryMaleModel(this.tertiaryMaleModel).femaleOffset(this.femaleOffset).primaryFemaleHeadModelId(this.primaryFemaleHeadModelId).secondaryFemaleHeadModelId(this.secondaryFemaleHeadModelId).primaryFemaleModel(this.primaryFemaleModel).secondaryFemaleModel(this.secondaryFemaleModel).tertiaryFemaleModel(this.tertiaryFemaleModel).inventoryModelId(this.inventoryModelId).shiftClickIndex(this.shiftClickIndex).teamId(this.teamId).zoom(this.zoom).offsetX(this.offsetX).offsetY(this.offsetY).modelPitch(this.modelPitch).modelRoll(this.modelRoll).modelYaw(this.modelYaw).resizeX(this.resizeX).resizeY(this.resizeY).resizeZ(this.resizeZ).originalColours(this.originalColours).replacementColours(this.replacementColours).originalTextureIds(this.originalTextureIds).replacementTextureIds(this.replacementTextureIds).ambient(this.ambient).contrast(this.contrast).parameters(this.parameters).examine(this.examine).weight(this.weight).slot(this.slot).bonuses(this.bonuses).equipmentType(this.equipmentType).twoHanded(this.twoHanded).blockAnimation(this.blockAnimation).standAnimation(this.standAnimation).walkAnimation(this.walkAnimation).runAnimation(this.runAnimation).standTurnAnimation(this.standTurnAnimation).rotate90Animation(this.rotate90Animation).rotate180Animation(this.rotate180Animation).rotate270Animation(this.rotate270Animation).accurateAnimation(this.accurateAnimation).aggressiveAnimation(this.aggressiveAnimation).controlledAnimation(this.controlledAnimation).defensiveAnimation(this.defensiveAnimation).attackSpeed(this.attackSpeed).interfaceVarbit(this.interfaceVarbit).normalAttackDistance(this.normalAttackDistance).longAttackDistance(this.longAttackDistance);
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return "ItemDefinitions(name=" + this.getName() + ", lowercaseName=" + this.getLowercaseName() + ", id=" + this.getId() + ", inventoryOptions=" + java.util.Arrays.deepToString(this.getInventoryOptions()) + ", groundOptions=" + java.util.Arrays.deepToString(this.getGroundOptions()) + ", grandExchange=" + this.isGrandExchange() + ", isMembers=" + this.isMembers() + ", isStackable=" + this.isStackable + ", price=" + this.getPrice() + ", notedTemplate=" + this.getNotedTemplate() + ", notedId=" + this.getNotedId() + ", bindTemplateId=" + this.getBindTemplateId() + ", bindId=" + this.getBindId() + ", placeholderTemplate=" + this.getPlaceholderTemplate() + ", placeholderId=" + this.getPlaceholderId() + ", stackIds=" + java.util.Arrays.toString(this.getStackIds()) + ", stackAmounts=" + java.util.Arrays.toString(this.getStackAmounts()) + ", maleOffset=" + this.getMaleOffset() + ", primaryMaleHeadModelId=" + this.getPrimaryMaleHeadModelId() + ", secondaryMaleHeadModelId=" + this.getSecondaryMaleHeadModelId() + ", primaryMaleModel=" + this.getPrimaryMaleModel() + ", secondaryMaleModel=" + this.getSecondaryMaleModel() + ", tertiaryMaleModel=" + this.getTertiaryMaleModel() + ", femaleOffset=" + this.getFemaleOffset() + ", primaryFemaleHeadModelId=" + this.getPrimaryFemaleHeadModelId() + ", secondaryFemaleHeadModelId=" + this.getSecondaryFemaleHeadModelId() + ", primaryFemaleModel=" + this.getPrimaryFemaleModel() + ", secondaryFemaleModel=" + this.getSecondaryFemaleModel() + ", tertiaryFemaleModel=" + this.getTertiaryFemaleModel() + ", inventoryModelId=" + this.getInventoryModelId() + ", shiftClickIndex=" + this.getShiftClickIndex() + ", teamId=" + this.getTeamId() + ", zoom=" + this.getZoom() + ", offsetX=" + this.getOffsetX() + ", offsetY=" + this.getOffsetY() + ", modelPitch=" + this.getModelPitch() + ", modelRoll=" + this.getModelRoll() + ", modelYaw=" + this.getModelYaw() + ", resizeX=" + this.getResizeX() + ", resizeY=" + this.getResizeY() + ", resizeZ=" + this.getResizeZ() + ", originalColours=" + java.util.Arrays.toString(this.getOriginalColours()) + ", replacementColours=" + java.util.Arrays.toString(this.getReplacementColours()) + ", originalTextureIds=" + java.util.Arrays.toString(this.getOriginalTextureIds()) + ", replacementTextureIds=" + java.util.Arrays.toString(this.getReplacementTextureIds()) + ", ambient=" + this.getAmbient() + ", contrast=" + this.getContrast() + ", parameters=" + this.getParameters() + ", examine=" + this.getExamine() + ", weight=" + this.getWeight() + ", slot=" + this.getSlot() + ", bonuses=" + java.util.Arrays.toString(this.getBonuses()) + ", equipmentType=" + this.getEquipmentType() + ", twoHanded=" + this.isTwoHanded() + ", blockAnimation=" + this.getBlockAnimation() + ", standAnimation=" + this.getStandAnimation() + ", walkAnimation=" + this.getWalkAnimation() + ", runAnimation=" + this.getRunAnimation() + ", standTurnAnimation=" + this.getStandTurnAnimation() + ", rotate90Animation=" + this.getRotate90Animation() + ", rotate180Animation=" + this.getRotate180Animation() + ", rotate270Animation=" + this.getRotate270Animation() + ", accurateAnimation=" + this.getAccurateAnimation() + ", aggressiveAnimation=" + this.getAggressiveAnimation() + ", controlledAnimation=" + this.getControlledAnimation() + ", defensiveAnimation=" + this.getDefensiveAnimation() + ", attackSpeed=" + this.getAttackSpeed() + ", interfaceVarbit=" + this.getInterfaceVarbit() + ", normalAttackDistance=" + this.getNormalAttackDistance() + ", longAttackDistance=" + this.getLongAttackDistance() + ")";
    }

    public ItemDefinitions(final String name, final String lowercaseName, final int id, final String[] inventoryOptions, final String[] groundOptions, final boolean grandExchange, final boolean isMembers, final int isStackable, final int price, final int notedTemplate, final int notedId, final int bindTemplateId, final int bindId, final int placeholderTemplate, final int placeholderId, final int[] stackIds, final int[] stackAmounts, final int maleOffset, final int primaryMaleHeadModelId, final int secondaryMaleHeadModelId, final int primaryMaleModel, final int secondaryMaleModel, final int tertiaryMaleModel, final int femaleOffset, final int primaryFemaleHeadModelId, final int secondaryFemaleHeadModelId, final int primaryFemaleModel, final int secondaryFemaleModel, final int tertiaryFemaleModel, final int inventoryModelId, final int shiftClickIndex, final int teamId, final int zoom, final int offsetX, final int offsetY, final int modelPitch, final int modelRoll, final int modelYaw, final int resizeX, final int resizeY, final int resizeZ, final short[] originalColours, final short[] replacementColours, final short[] originalTextureIds, final short[] replacementTextureIds, final int ambient, final int contrast, final Int2ObjectOpenHashMap<Object> parameters, final String examine, final float weight, final int slot, final int[] bonuses, final EquipmentType equipmentType, final boolean twoHanded, final int blockAnimation, final int standAnimation, final int walkAnimation, final int runAnimation, final int standTurnAnimation, final int rotate90Animation, final int rotate180Animation, final int rotate270Animation, final int accurateAnimation, final int aggressiveAnimation, final int controlledAnimation, final int defensiveAnimation, final int attackSpeed, final int interfaceVarbit, final int normalAttackDistance, final int longAttackDistance) {
        this.name = name;
        this.lowercaseName = lowercaseName;
        this.id = id;
        this.inventoryOptions = inventoryOptions;
        this.groundOptions = groundOptions;
        this.grandExchange = grandExchange;
        this.isMembers = isMembers;
        this.isStackable = isStackable;
        this.price = price;
        this.notedTemplate = notedTemplate;
        this.notedId = notedId;
        this.bindTemplateId = bindTemplateId;
        this.bindId = bindId;
        this.placeholderTemplate = placeholderTemplate;
        this.placeholderId = placeholderId;
        this.stackIds = stackIds;
        this.stackAmounts = stackAmounts;
        this.maleOffset = maleOffset;
        this.primaryMaleHeadModelId = primaryMaleHeadModelId;
        this.secondaryMaleHeadModelId = secondaryMaleHeadModelId;
        this.primaryMaleModel = primaryMaleModel;
        this.secondaryMaleModel = secondaryMaleModel;
        this.tertiaryMaleModel = tertiaryMaleModel;
        this.femaleOffset = femaleOffset;
        this.primaryFemaleHeadModelId = primaryFemaleHeadModelId;
        this.secondaryFemaleHeadModelId = secondaryFemaleHeadModelId;
        this.primaryFemaleModel = primaryFemaleModel;
        this.secondaryFemaleModel = secondaryFemaleModel;
        this.tertiaryFemaleModel = tertiaryFemaleModel;
        this.inventoryModelId = inventoryModelId;
        this.shiftClickIndex = shiftClickIndex;
        this.teamId = teamId;
        this.zoom = zoom;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.modelPitch = modelPitch;
        this.modelRoll = modelRoll;
        this.modelYaw = modelYaw;
        this.resizeX = resizeX;
        this.resizeY = resizeY;
        this.resizeZ = resizeZ;
        this.originalColours = originalColours;
        this.replacementColours = replacementColours;
        this.originalTextureIds = originalTextureIds;
        this.replacementTextureIds = replacementTextureIds;
        this.ambient = ambient;
        this.contrast = contrast;
        this.parameters = parameters;
        this.examine = examine;
        this.weight = weight;
        this.slot = slot;
        this.bonuses = bonuses;
        this.equipmentType = equipmentType;
        this.twoHanded = twoHanded;
        this.blockAnimation = blockAnimation;
        this.standAnimation = standAnimation;
        this.walkAnimation = walkAnimation;
        this.runAnimation = runAnimation;
        this.standTurnAnimation = standTurnAnimation;
        this.rotate90Animation = rotate90Animation;
        this.rotate180Animation = rotate180Animation;
        this.rotate270Animation = rotate270Animation;
        this.accurateAnimation = accurateAnimation;
        this.aggressiveAnimation = aggressiveAnimation;
        this.controlledAnimation = controlledAnimation;
        this.defensiveAnimation = defensiveAnimation;
        this.attackSpeed = attackSpeed;
        this.interfaceVarbit = interfaceVarbit;
        this.normalAttackDistance = normalAttackDistance;
        this.longAttackDistance = longAttackDistance;
    }

    public ItemDefinitions() {
    }

    public static IntOpenHashSet getPackedIds() {
        return ItemDefinitions.packedIds;
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

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String[] getInventoryOptions() {
        return this.inventoryOptions;
    }

    public void setInventoryOptions(final String[] inventoryOptions) {
        this.inventoryOptions = inventoryOptions;
    }

    public String[] getGroundOptions() {
        return this.groundOptions;
    }

    public boolean isGrandExchange() {
        return this.grandExchange;
    }

    public void setGrandExchange(final boolean grandExchange) {
        this.grandExchange = grandExchange;
    }

    public boolean isMembers() {
        return this.isMembers;
    }

    public void setIsStackable(final int isStackable) {
        this.isStackable = isStackable;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public int getNotedTemplate() {
        return this.notedTemplate;
    }

    public void setNotedTemplate(final int notedTemplate) {
        this.notedTemplate = notedTemplate;
    }

    public int getNotedId() {
        return this.notedId;
    }

    public void setNotedId(final int notedId) {
        this.notedId = notedId;
    }

    public int getBindTemplateId() {
        return this.bindTemplateId;
    }

    public int getBindId() {
        return this.bindId;
    }

    public int getPlaceholderTemplate() {
        return this.placeholderTemplate;
    }

    public void setPlaceholderTemplate(final int placeholderTemplate) {
        this.placeholderTemplate = placeholderTemplate;
    }

    public int getPlaceholderId() {
        return this.placeholderId;
    }

    public void setPlaceholderId(final int placeholderId) {
        this.placeholderId = placeholderId;
    }

    public int[] getStackIds() {
        return this.stackIds;
    }

    public void setStackIds(final int[] stackIds) {
        this.stackIds = stackIds;
    }

    public int[] getStackAmounts() {
        return this.stackAmounts;
    }

    public void setStackAmounts(final int[] stackAmounts) {
        this.stackAmounts = stackAmounts;
    }

    public int getMaleOffset() {
        return this.maleOffset;
    }

    public void setMaleOffset(final int maleOffset) {
        this.maleOffset = maleOffset;
    }

    public int getPrimaryMaleHeadModelId() {
        return this.primaryMaleHeadModelId;
    }

    public void setPrimaryMaleHeadModelId(final int primaryMaleHeadModelId) {
        this.primaryMaleHeadModelId = primaryMaleHeadModelId;
    }

    public int getSecondaryMaleHeadModelId() {
        return this.secondaryMaleHeadModelId;
    }

    public void setSecondaryMaleHeadModelId(final int secondaryMaleHeadModelId) {
        this.secondaryMaleHeadModelId = secondaryMaleHeadModelId;
    }

    public int getPrimaryMaleModel() {
        return this.primaryMaleModel;
    }

    public void setPrimaryMaleModel(final int primaryMaleModel) {
        this.primaryMaleModel = primaryMaleModel;
    }

    public int getSecondaryMaleModel() {
        return this.secondaryMaleModel;
    }

    public void setSecondaryMaleModel(final int secondaryMaleModel) {
        this.secondaryMaleModel = secondaryMaleModel;
    }

    public int getTertiaryMaleModel() {
        return this.tertiaryMaleModel;
    }

    public void setTertiaryMaleModel(final int tertiaryMaleModel) {
        this.tertiaryMaleModel = tertiaryMaleModel;
    }

    public int getFemaleOffset() {
        return this.femaleOffset;
    }

    public void setFemaleOffset(final int femaleOffset) {
        this.femaleOffset = femaleOffset;
    }

    public int getPrimaryFemaleHeadModelId() {
        return this.primaryFemaleHeadModelId;
    }

    public void setPrimaryFemaleHeadModelId(final int primaryFemaleHeadModelId) {
        this.primaryFemaleHeadModelId = primaryFemaleHeadModelId;
    }

    public int getSecondaryFemaleHeadModelId() {
        return this.secondaryFemaleHeadModelId;
    }

    public void setSecondaryFemaleHeadModelId(final int secondaryFemaleHeadModelId) {
        this.secondaryFemaleHeadModelId = secondaryFemaleHeadModelId;
    }

    public int getPrimaryFemaleModel() {
        return this.primaryFemaleModel;
    }

    public void setPrimaryFemaleModel(final int primaryFemaleModel) {
        this.primaryFemaleModel = primaryFemaleModel;
    }

    public int getSecondaryFemaleModel() {
        return this.secondaryFemaleModel;
    }

    public void setSecondaryFemaleModel(final int secondaryFemaleModel) {
        this.secondaryFemaleModel = secondaryFemaleModel;
    }

    public int getTertiaryFemaleModel() {
        return this.tertiaryFemaleModel;
    }

    public void setTertiaryFemaleModel(final int tertiaryFemaleModel) {
        this.tertiaryFemaleModel = tertiaryFemaleModel;
    }

    public int getInventoryModelId() {
        return this.inventoryModelId;
    }

    public void setInventoryModelId(final int inventoryModelId) {
        this.inventoryModelId = inventoryModelId;
    }

    public int getShiftClickIndex() {
        return this.shiftClickIndex;
    }

    public int getTeamId() {
        return this.teamId;
    }

    public int getZoom() {
        return this.zoom;
    }

    public void setZoom(final int zoom) {
        this.zoom = zoom;
    }

    public int getOffsetX() {
        return this.offsetX;
    }

    public void setOffsetX(final int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public void setOffsetY(final int offsetY) {
        this.offsetY = offsetY;
    }

    public int getModelPitch() {
        return this.modelPitch;
    }

    public void setModelPitch(final int modelPitch) {
        this.modelPitch = modelPitch;
    }

    public int getModelRoll() {
        return this.modelRoll;
    }

    public void setModelRoll(final int modelRoll) {
        this.modelRoll = modelRoll;
    }

    public int getModelYaw() {
        return this.modelYaw;
    }

    public void setModelYaw(final int modelYaw) {
        this.modelYaw = modelYaw;
    }

    public int getResizeX() {
        return this.resizeX;
    }

    public int getResizeY() {
        return this.resizeY;
    }

    public int getResizeZ() {
        return this.resizeZ;
    }

    public short[] getOriginalColours() {
        return this.originalColours;
    }

    public void setOriginalColours(final short[] originalColours) {
        this.originalColours = originalColours;
    }

    public short[] getReplacementColours() {
        return this.replacementColours;
    }

    public void setReplacementColours(final short[] replacementColours) {
        this.replacementColours = replacementColours;
    }

    public short[] getOriginalTextureIds() {
        return this.originalTextureIds;
    }

    public short[] getReplacementTextureIds() {
        return this.replacementTextureIds;
    }

    public int getAmbient() {
        return this.ambient;
    }

    public int getContrast() {
        return this.contrast;
    }

    public Int2ObjectOpenHashMap<Object> getParameters() {
        return this.parameters;
    }

    public void setParameters(final Int2ObjectOpenHashMap<Object> parameters) {
        this.parameters = parameters;
    }

    public String getExamine() {
        return this.examine;
    }

    public void setExamine(final String examine) {
        this.examine = examine;
    }

    public float getWeight() {
        return this.weight;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(final int slot) {
        this.slot = slot;
    }

    public int[] getBonuses() {
        return this.bonuses;
    }

    public EquipmentType getEquipmentType() {
        return this.equipmentType;
    }

    public boolean isTwoHanded() {
        return this.twoHanded;
    }

    public int getBlockAnimation() {
        return this.blockAnimation;
    }

    public int getStandAnimation() {
        return this.standAnimation;
    }

    public int getWalkAnimation() {
        return this.walkAnimation;
    }

    public int getRunAnimation() {
        return this.runAnimation;
    }

    public int getStandTurnAnimation() {
        return this.standTurnAnimation;
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

    public int getAccurateAnimation() {
        return this.accurateAnimation;
    }

    public int getAggressiveAnimation() {
        return this.aggressiveAnimation;
    }

    public int getControlledAnimation() {
        return this.controlledAnimation;
    }

    public int getDefensiveAnimation() {
        return this.defensiveAnimation;
    }

    public int getAttackSpeed() {
        return this.attackSpeed;
    }

    public int getInterfaceVarbit() {
        return this.interfaceVarbit;
    }

    public int getNormalAttackDistance() {
        return this.normalAttackDistance;
    }

    public int getLongAttackDistance() {
        return this.longAttackDistance;
    }
}
