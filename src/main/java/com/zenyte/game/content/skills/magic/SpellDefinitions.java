package com.zenyte.game.content.skills.magic;

import com.zenyte.game.item.Item;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mgi.types.config.enums.Enums;
import mgi.types.config.enums.IntEnum;
import mgi.types.config.items.ItemDefinitions;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kris | 23. mai 2018 : 17:31:51
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class SpellDefinitions {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpellDefinitions.class);
	public static final Map<String, SpellDefinitions> SPELLS = new HashMap<String, SpellDefinitions>();
	private static final Int2ObjectOpenHashMap<String> spellComponentMap = new Int2ObjectOpenHashMap<>();
	public static final Object2IntOpenHashMap<String> autocastSlotMap = new Object2IntOpenHashMap<>();

	public SpellDefinitions(final int level, final Item[] runes) {
		this.level = level;
		this.runes = runes;
	}

	private final int level;
	private final Item[] runes;
	private static final int[] RUNE_INDEXES = new int[] {365, 367, 369, 606};
	private static final int[] RUNE_AMOUNTS = new int[] {366, 368, 370, 607};

	static {
		final mgi.types.config.enums.IntEnum[] enums = new IntEnum[] {Enums.REGULAR_SPELLS_ENUM, Enums.ANCIENT_SPELLS_ENUM, Enums.LUNAR_SPELLS_ENUM, Enums.ARCEUUS_SPELLS_ENUM};
		for (final mgi.types.config.enums.IntEnum spellbookEnum : enums) {
			for (final it.unimi.dsi.fastutil.ints.Int2IntMap.Entry enumEntry : spellbookEnum.getValues().int2IntEntrySet()) {
				final int spellItem = enumEntry.getIntValue();
				final mgi.types.config.items.ItemDefinitions definitions = ItemDefinitions.getOrThrow(spellItem);
				final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.lang.Object> params = definitions.getParameters();
				if (params == null) throw new RuntimeException("Spell item parameters are null!");
				final java.lang.String name = (String) params.get(601);
				final int level = (int) params.get(604);
				final java.util.ArrayList<com.zenyte.game.item.Item> runes = new ArrayList<Item>(4);
				final int componentId = ((int) params.get(596)) & 65535;
				for (int a = 0; a < 4; a++) {
					final int runeIndex = RUNE_INDEXES[a];
					final java.lang.Object entry = params.get(runeIndex);
					if (entry == null) continue;
					final int amount = RUNE_AMOUNTS[a];
					runes.add(new Item((int) entry, (int) params.get(amount)));
				}
				final com.zenyte.game.content.skills.magic.SpellDefinitions definition = new SpellDefinitions(level, runes.toArray(new Item[0]));
				final java.lang.String refactoredName = name.replaceAll("-", "").toLowerCase();
				SPELLS.put(refactoredName, definition);
				spellComponentMap.put(componentId, refactoredName);
			}
		}
		for (final it.unimi.dsi.fastutil.ints.Int2IntMap.Entry entry : Enums.AUTOCASTABLE_SPELLS_ENUM.getValues().int2IntEntrySet()) {
			final int key = entry.getIntKey();
			final int value = entry.getIntValue();
			final mgi.types.config.items.ItemDefinitions itemDefinitions = ItemDefinitions.getOrThrow(value);
			final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.lang.Object> params = Objects.requireNonNull(itemDefinitions.getParameters());
			final java.lang.String name = (String) params.get(601);
			final java.lang.String refactoredName = name.replaceAll("-", "").toLowerCase();
			autocastSlotMap.put(refactoredName, key);
		}
	}

	public static final String getSpellName(final int componentId) {
		try {
			return spellComponentMap.get(componentId);
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
		return null;
	}

	public static final int getSpellComponent(final String spellName) {
		try {
			for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.lang.String> entry : spellComponentMap.int2ObjectEntrySet()) {
				if (entry.getValue().equalsIgnoreCase(spellName)) {
					return entry.getIntKey();
				}
			}
		} catch (final Exception e) {
			log.error(Strings.EMPTY, e);
		}
		return -1;
	}

	public int getLevel() {
		return this.level;
	}

	public Item[] getRunes() {
		return this.runes;
	}
}
