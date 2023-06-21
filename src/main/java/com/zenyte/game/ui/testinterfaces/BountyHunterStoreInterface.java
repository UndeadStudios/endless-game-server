package com.zenyte.game.ui.testinterfaces;

import com.google.common.base.Preconditions;
import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.content.bountyhunter.BountyHunterVar;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.util.Examine;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.LootingBag;
import com.zenyte.plugins.item.TomeOfFire;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mgi.types.config.enums.Enums;
import mgi.types.config.items.ItemDefinitions;

import java.util.Optional;

import static com.zenyte.game.util.AccessMask.*;

/**
 * @author Kris | 07/05/2019 01:28
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class BountyHunterStoreInterface extends Interface {

    public enum Reward {
        DRAGON_LONGSWORD(1305, 300000), DRAGON_BATTLEAXE(1377, 400000), DRAGON_MACE(1434, 150000), DRAGON_HALBERD(3204, 900000), HELM_OF_NEITIZNOT(10828, 150000), BERSERKER_HELM(3751, 234000), WARRIOR_HELM(3753, 234000), ARCHER_HELM(3749, 234000), FARSEER_HELM(3755, 234000), GREEN_DARK_BOW_PAINT(12759, 500000), YELLOW_DARK_BOW_PAINT(12761, 500000), WHITE_DARK_BOW_PAINT(12763, 500000), BLUE_DARK_BOW_PAINT(12757, 500000), PADDEWWA_TELEPORT(12781, 10000), SENNTISTEN_TELEPORT(12782, 10000), ANNAKARL_TELEPORT(12775, 10000), CARRALLANGAR_TELEPORT(12776, 10000), DAREEYAK_TELEPORT(12777, 10000), GHORROCK_TELEPORT(12778, 10000), KHARYRLL_TELEPORT(12779, 10000), LASSAR_TELEPORT(12780, 10000), VOLCANIC_WHIP_MIX(12771, 500000), FROZEN_WHIP_MIX(12769, 500000), STEAM_STAFF_UPGRADE_KIT(12798, 250000), LAVA_STAFF_UPGRADE_KIT(21202, 250000), DRAGON_PICKAXE_UPGRADE_KIT(12800, 300000), WARD_UPGRADE_KIT(12802, 350000), RING_OF_WEALTH_SCROLL(12783, 50000), MAGIC_SHORTBOW_SCROLL(12786, 100000), SARADOMINS_TEAR(12804, 25000000), RUNE_POUCH(12791, 1200000), LOOTING_BAG(11941, 10000), BOLT_RACK(4740, 360), RUNE_ARROW(892, 600), ADAMANT_ARROW(890, 240), GRANITE_CLAMP(12849, 250000), HUNTERS_HONOUR(12855, 2500000), REVENANT_CAVE_TELEPORT(21802, 75000), BURNING_AMULET(21166, 10000), ROYAL_SEED_POD(19564, 30000), SUPER_ATTACK(2436, 7500), SUPER_STRENGTH(2440, 8500), SUPER_DEFENCE(2442, 6000), RANGING_POTION(2444, 7500), MAGIC_POTION(3040, 5000), SUPER_COMBAT_POTION(12695, 25000), SUPER_RESTORE(3024, 12500), SANFEW_SERUM(10925, 15000), PRAYER_POTION(2434, 10000), SARADOMIN_BREW(6685, 13500), STAMINA_POTION(12625, 17500), ANTI_VENOM_PLUS(12913, 15000), FIGHTER_TORSO(10551, 600000), MASTER_WAND(6914, 35000000), DRAGON_CROSSBOW(21902, 30000000), DRAGON_THROWNAXE(20849, 5500), DRAGON_KNIFE(22804, 3000), DRAGON_BOLT(21905, 5000), ANCIENT_MACE(11061, 550000), DECORATIVE_RANGE_TOP(11899, 150000), DECORATIVE_RANGE_BOTTOM(11900, 150000), DECORATIVE_MAGE_TOP(11896, 150000), DECORATIVE_MAGE_BOTTOM(11897, 150000), SARADOMIN_HALO(12637, 300000), ZAMORAK_HALO(12638, 300000), GUTHIX_HALO(12639, 300000), TOME_OF_FIRE(TomeOfFire.TOME_OF_FIRE_EMPTY, 15000000), CRYSTAL_SEED(4207, 540000);
        private static final Reward[] values = values();
        private static final Int2ObjectMap<Reward> map = new Int2ObjectOpenHashMap<>();

        static {
            for (final com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface.Reward value : values) {
                map.put(value.id, value);
            }
        }

        private static final Optional<Reward> get(final int id) {
            return Optional.ofNullable(map.get(id));
        }

        private final int id;
        private final int cost;

        private Reward(final int id, final int cost) {
            this.id = id;
            this.cost = cost;
        }

        public int getId() {
            return this.id;
        }

        public int getCost() {
            return this.cost;
        }
    }

    @Override
    protected void attach() {
        put(2, "Item layer");
        put(3, "Scrollbar");
    }

    @Override
    public void open(final Player player) {
        final com.zenyte.game.packet.PacketDispatcher dispatcher = player.getPacketDispatcher();
        final com.zenyte.game.world.entity.player.VarManager varManager = player.getVarManager();
        final com.zenyte.game.content.bountyhunter.BountyHunter bounty = player.getBountyHunter();
        final mgi.types.config.enums.IntEnum layerEnum = Enums.BOUNTY_HUNTER_REWARDS;
        player.getInterfaceHandler().sendInterface(this);
        varManager.sendVarInstant(1137, bounty.getValue(BountyHunterVar.CURRENT_HUNTER_KILLS));
        varManager.sendVarInstant(1138, bounty.getValue(BountyHunterVar.CURRENT_ROGUE_KILLS));
        dispatcher.sendClientScript(23, id << 16 | getComponent("Item layer"), id << 16 | getComponent("Scrollbar"), layerEnum.getId());
        dispatcher.sendComponentSettings(id, getComponent("Item layer"), 0, layerEnum.getSize(), CLICK_OP1, CLICK_OP2, CLICK_OP3, CLICK_OP4, CLICK_OP5, CLICK_OP10);
    }

    @Override
    protected void build() {
        bind("Item layer", (player, slotId, itemId, optionId) -> {
            final mgi.types.config.enums.IntEnum layerEnum = Enums.BOUNTY_HUNTER_REWARDS;
            final java.util.OptionalInt optionalItem = layerEnum.getValue(slotId);
            if (!optionalItem.isPresent()) {
                return;
            }
            int item = optionalItem.getAsInt();
            final java.util.Optional<com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface.Reward> optionalReward = Reward.get(item);
            if (!optionalReward.isPresent()) {
                return;
            }
            final com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface.Reward reward = optionalReward.get();
            if (reward == Reward.HUNTERS_HONOUR && player.getVarManager().getValue(1137) > player.getVarManager().getValue(1138)) {
                item = 12856;
            }
            final com.zenyte.game.ui.testinterfaces.BountyHunterStoreInterface.Option option = Option.get(optionId - 1);
            if (option == Option.EXAMINE) {
                Examine.sendItemExamine(player, item);
                return;
            }
            if (option == Option.VALUE) {
                final mgi.types.config.items.ItemDefinitions itemDefinitions = ItemDefinitions.getOrThrow(item);
                player.sendMessage(itemDefinitions.getName() + ": currently costs " + Utils.format(reward.cost) + " Bounty Hunter points.");
                return;
            }
            int amount = option.amount;
            final int points = player.getBountyHunter().getPoints();
            final int cost = reward.cost;
            if (amount > (points / cost)) {
                amount = points / cost;
            }
            final com.zenyte.game.world.entity.player.container.Container inventory = player.getInventory().getContainer();
            int freeSlots = inventory.getFreeSlotsSize();
            final int affordableAmount = amount;
            final int inInventory = inventory.getAmountOf(item);
            if (amount + inInventory < 0) {
                amount = Integer.MAX_VALUE - inInventory;
            }
            final mgi.types.config.items.ItemDefinitions definitions = ItemDefinitions.getOrThrow(item);
            if (definitions.isStackable()) {
                if (freeSlots == 0 && inInventory == 0) {
                    amount = 0;
                }
            } else {
                amount = Math.min(freeSlots, amount);
            }
            final Optional<String> message = affordableAmount != option.amount ? Optional.of("You don\'t have enough Bounty Hunter points.") : amount != affordableAmount ? Optional.of("Not enough space in your inventory.") : Optional.empty();
            message.ifPresent(mes -> player.sendMessage(mes));
            if (amount <= 0) {
                return;
            }
            if (reward == Reward.LOOTING_BAG) {
                if (LootingBag.hasBag(player)) {
                    player.sendMessage("You can only have one looting bag with you at all times.");
                    return;
                }
                if (amount > 1) {
                    amount = 1;
                    player.sendMessage("You can only have one looting bag with you at all times.");
                }
            } else if (reward == Reward.RUNE_POUCH) {
                if (player.containsItem(ItemId.RUNE_POUCH)) {
                    player.sendMessage("You can only own one rune pouch at a time!");
                    return;
                }
                if (amount > 1) {
                    amount = 1;
                    player.sendMessage("You can only own one rune pouch at a time!");
                }
            }
            player.getBountyHunter().setPoints(points - (cost * amount));
            player.getInventory().addOrDrop(new Item(item, amount));
        });
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.BOUNTY_HUNTER_STORE;
    }


    private enum Option {
        VALUE(-1), BUY_1(1), BUY_5(5), BUY_10(10), BUY_50(50), EXAMINE(-1);
        private final int amount;
        private static final Option[] values = values();

        private static final Option get(final int id) {
            if (id == 9) {
                return EXAMINE;
            }
            Preconditions.checkArgument(!(id < 0 || id >= values.length));
            return values[id];
        }

        private Option(final int amount) {
            this.amount = amount;
        }
    }
}
