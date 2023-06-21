package com.zenyte.game.item;

import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import mgi.types.config.items.ItemDefinitions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kris | 15/03/2019 18:42
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public enum SkillcapePerk {
    ATTACK(9747, 9748, 10639, 13280, 13282, 13342), COOKING(9801, 9802, 10658, 13280, 13282, 13342), FARMING(9810, 9811, 10661, 13280, 13282, 13342), MINING(9792, 9793, 10655, 13280, 13282, 13342), RANGED(9756, 9757, 10642, 13280, 13282, 13342, 13337, 21898), SLAYER(9786, 9787, 10653, 13280, 13282, 13342), THIEVING(9777, 9778, 10650, 13280, 13282, 13342), WOODCUTTING(9807, 9808, 10660, 13280, 13282, 13342), FIREMAKING(9804, 9805, 10659, 13280, 13282, 13342), RUNECRAFTING(9765, 9766, 10645, 13280, 13282, 13342), HITPOINTS(9768, 9769, 10647, 13280, 13282, 13342), HERBLORE(9774, 9775, 10649, 13280, 13282, 13342), AGILITY(9771, 9772, 10648, 13340, 13341, 13280, 13282, 13342), PRAYER(9759, 9760, 10643, 13280, 13282, 13342), SMITHING(9795, 9796, 10656, 13280, 13282, 13342), STRENGTH(9750, 9751, 10640, 13280, 13282, 13342), HUNTER(9948, 9949, 10646, 13280, 13282, 13342), FISHING(9798, 9799, 10657, 13280, 13282, 13342), CONSTRUCTION(9789, 9790, 10654, 13280, 13282, 13342), CRAFTING(9780, 9781, 10651, 13280, 13282, 13342), DEFENCE(9753, 9754, 10641, 13280, 13282, 13342), MAGIC(9762, 9763, 10644, 13280, 13282, 13342), FLETCHING(9783, 9784, 10652, 13280, 13282, 13342);
    private final int[] capes;
    private final int[] skillCapes;

    SkillcapePerk(final int... capes) {
        this.capes = capes;
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        for (final int cape : capes) {
            final mgi.types.config.items.ItemDefinitions definitions = ItemDefinitions.get(cape);
            if (definitions == null) continue;
            if (definitions.getName().toLowerCase().contains("max")) continue;
            list.add(cape);
        }
        this.skillCapes = list.toIntArray();
    }

    public boolean isEffective(@NotNull final Player player) {
        final int cape = player.getEquipment().getId(EquipmentSlot.CAPE);
        return cape != -1 && ArrayUtils.contains(capes, cape);
    }

    public boolean isCarrying(@NotNull final Player player) {
        return player.carryingAny(capes);
    }

    public int[] getCapes() {
        return this.capes;
    }

    public int[] getSkillCapes() {
        return this.skillCapes;
    }
}
