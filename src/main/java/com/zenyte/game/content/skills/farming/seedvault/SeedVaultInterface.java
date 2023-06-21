package com.zenyte.game.content.skills.farming.seedvault;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.constants.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.ui.Interface;
import com.zenyte.game.ui.SwitchPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.VarManager;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;
import com.zenyte.plugins.events.LoginEvent;
import com.zenyte.utils.StaticInitializer;
import mgi.types.config.items.ItemDefinitions;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.zenyte.game.util.AccessMask.*;

@StaticInitializer
public class SeedVaultInterface extends Interface implements SwitchPlugin {
    public static final int AMOUNT_VAR = 2195;
    private static final int CATEGORY_VARBIT = 8171;
    private static final int INPUT_SCREEN_CLOSE_SCRIPT = 101;
    private static final int SEARCH_TYPE = 11;

    static {
        VarManager.appendPersistentVarp(AMOUNT_VAR);
    }

    @Subscribe
    public static final void onLogin(final LoginEvent event) {
        final com.zenyte.game.world.entity.player.Player player = event.getPlayer();
        if (!FavouriteSlot.isSet(player)) {
            FavouriteSlot.reset(player);
            FavouriteSlot.setInitialized(player);
            player.getVarManager().sendVar(AMOUNT_VAR, 1);
        }
    }

    @Override
    protected void attach() {
        put(8, "Categories");
        put(15, "Container");
        put(19, "Withdraw-1");
        put(20, "Withdraw-5");
        put(21, "Withdraw-10");
        put(22, "Withdraw-X");
        put(23, "Withdraw-All");
        put(24, "Search");
        put(25, "Deposit-All");
    }

    @Override
    public void open(Player player) {
        player.getInterfaceHandler().sendInterface(getInterface());
        final com.zenyte.game.packet.PacketDispatcher dispatcher = player.getPacketDispatcher();
        final int vaultSize = ContainerType.SEED_VAULT.getSize();
        dispatcher.sendComponentSettings(getInterface(), getComponent("Categories"), 0, 10, CLICK_OP1);
        dispatcher.sendComponentSettings(getInterface(), getComponent("Container"), 0, vaultSize, CLICK_OP1, CLICK_OP2, CLICK_OP3, CLICK_OP4, CLICK_OP5, CLICK_OP6, CLICK_OP7, CLICK_OP8, CLICK_OP9, CLICK_OP10, DRAG_DEPTH2, DRAG_TARGETABLE);
        GameInterface.SEED_VAULT_INVENTORY.open(player);
        player.getVarManager().sendBit(CATEGORY_VARBIT, 0);
        final com.zenyte.game.content.skills.farming.seedvault.SeedVaultContainer container = player.getSeedVault().getContainer();
        container.setFullUpdate(true);
        container.refresh(player);
    }

    @Override
    public void close(final Player player, final Optional<GameInterface> replacement) {
        player.getPacketDispatcher().sendClientScript(INPUT_SCREEN_CLOSE_SCRIPT, SEARCH_TYPE);
    }

    @Override
    protected void build() {
        bind("Categories", ((player, slotId, itemId, option) -> player.getVarManager().sendBit(CATEGORY_VARBIT, slotId)));
        bind("Container", ((player, seedSlot, itemId, optionId) -> {
            final com.zenyte.game.content.skills.farming.seedvault.SeedVaultExchangeOption option = SeedVaultExchangeOption.of(optionId);
            switch (option) {
            case REMOVE_ALL_PLACE: 
                player.getSeedVault().releasePlaceholders();
                break;
            case NOTE_OR_REMOVE_PLACE: 
                final mgi.types.config.items.ItemDefinitions def = ItemDefinitions.getOrThrow(itemId);
                if (def.isPlaceholder()) {
                    player.getSeedVault().releasePlaceholder(seedSlot);
                } else {
                    withdraw(player, seedSlot, player.getVarManager().getValue(AMOUNT_VAR), true);
                }
                break;
            case SELECTED: 
                withdraw(player, seedSlot, player.getVarManager().getValue(AMOUNT_VAR), false);
                break;
            case X: 
                player.sendInputInt("How much would you like to deposit?", amt -> withdraw(player, seedSlot, amt, false));
                break;
            case EXAMINE: 
                break;
            case FAVORITE: 
                final java.util.Optional<com.zenyte.game.content.skills.farming.seedvault.FavouriteSlot> favouriteSlot = FavouriteSlot.getBySeedSlot(player, seedSlot);
                if (favouriteSlot.isPresent()) {
                    unfavourite(player, favouriteSlot.get(), seedSlot);
                } else {
                    favourite(player, seedSlot);
                }
                break;
            default: 
                withdraw(player, seedSlot, option.getAmount(), false);
                break;
            }
        }));
        bind("Container", "Container", this::switchItem);
        bind("Withdraw-1", player -> player.getVarManager().sendVar(AMOUNT_VAR, 1));
        bind("Withdraw-5", player -> player.getVarManager().sendVar(AMOUNT_VAR, 5));
        bind("Withdraw-10", player -> player.getVarManager().sendVar(AMOUNT_VAR, 10));
        bind("Withdraw-X", player -> {
            //Start off by resetting it to a quantity of 1.
            player.getVarManager().sendVar(AMOUNT_VAR, 1);
            player.sendInputInt("Enter amount:", amount -> player.getVarManager().sendVar(AMOUNT_VAR, Math.max(1, amount)));
        });
        bind("Withdraw-All", player -> player.getVarManager().sendVar(AMOUNT_VAR, Integer.MAX_VALUE));
        bind("Deposit-All", player -> {
            final com.zenyte.game.world.entity.player.container.Container inventory = player.getInventory().getContainer();
            for (int i = 0; i < 28; i++) {
                final com.zenyte.game.item.Item item = inventory.get(i);
                if (item == null) {
                    continue;
                }
                SeedVaultInventoryInterface.deposit(player, i, item.getAmount(), false);
            }
        });
    }

    private void switchItem(final Player player, final int fromSlot, final int toSlot) {
        // Moving from corresponding seed slot to favourite slot
        if (toSlot == ContainerType.SEED_VAULT.getSize()) {
            final java.util.Optional<com.zenyte.game.content.skills.farming.seedvault.FavouriteSlot> existingSlot = FavouriteSlot.getBySeedSlot(player, fromSlot);
            if (existingSlot.isPresent()) {
                unfavourite(player, existingSlot.get(), fromSlot);
                return;
            }
            final java.util.Optional<com.zenyte.game.content.skills.farming.seedvault.FavouriteSlot> favouriteSlot = FavouriteSlot.getFreeSlot(player);
            if (favouriteSlot.isPresent()) {
                favourite(player, fromSlot);
                return;
            }
            player.sendMessage("Your favourite slots are full.");
            player.getSeedVault().getContainer().refresh(player);
            return;
        }
        final com.zenyte.game.content.skills.farming.seedvault.SeedVault vault = player.getSeedVault();
        final com.zenyte.game.content.skills.farming.seedvault.SeedVaultContainer container = vault.getContainer();
        final com.zenyte.game.item.Item fromItem = container.get(fromSlot);
        final com.zenyte.game.item.Item toItem = container.get(toSlot);
        if (fromItem == null || toItem == null) {
            return;
        }
        final boolean categoryMatches = isSameCategory(fromItem, toItem);
        final boolean bothFavourites = FavouriteSlot.getBySeedSlot(player, fromSlot).isPresent() && FavouriteSlot.getBySeedSlot(player, toSlot).isPresent();
        if (categoryMatches || bothFavourites) {
            vault.switchItem(fromSlot, toSlot);
        } else {
            player.sendMessage("You can only swap seeds and saplings within their category.");
        }
    }

    private void withdraw(final Player player, final int slotId, final int amount, final boolean note) {
        final com.zenyte.game.content.skills.farming.seedvault.SeedVaultContainer seedVault = player.getSeedVault().getContainer();
        //player.sendMessage("Not enough space in your " + container.getType().getName() + ".")
        final int inVault = seedVault.get(slotId).getAmount();
        seedVault.withdraw(player, player.getInventory().getContainer(), slotId, amount, note, true);
        final com.zenyte.game.item.Item remaining = seedVault.get(slotId);
        if (remaining != null && inVault == remaining.getAmount()) {
            player.sendMessage("Not enough space in your inventory.");
            return;
        }
        seedVault.refresh(player);
        player.getInventory().refreshAll();
    }

    private void favourite(final Player player, final int seedSlot) {
        final java.util.Optional<com.zenyte.game.content.skills.farming.seedvault.FavouriteSlot> favouriteSlot = FavouriteSlot.getFreeSlot(player);
        if (!favouriteSlot.isPresent()) {
            player.sendMessage("You can only have eight types of seeds as favorites.");
            return;
        }
        final com.zenyte.game.item.Item seed = player.getSeedVault().getContainer().get(seedSlot);
        player.sendMessage("You add " + getSeedName(seed) + " to your favourites.");
        player.getVarManager().sendBit(favouriteSlot.get().getVarbit(), seedSlot);
    }

    private void unfavourite(final Player player, final FavouriteSlot favouriteSlot, final int seedSlot) {
        final com.zenyte.game.item.Item seed = player.getSeedVault().getContainer().get(seedSlot);
        player.getVarManager().sendBit(favouriteSlot.getVarbit(), 255);
        player.sendMessage("You remove " + getSeedName(seed) + " from your favourites.");
    }

    private String getSeedName(final Item seed) {
        final mgi.types.config.items.ItemDefinitions seedDef = seed.getDefinitions();
        if (seedDef.isPlaceholder()) {
            return ItemDefinitions.getOrThrow(seedDef.getPlaceholderId()).getName();
        }
        return seed.getName();
    }

    private boolean isSameCategory(final Item from, final Item to) {
        return getCategory(from) == getCategory(to);
    }

    private int getCategory(@NotNull final Item item) {
        final java.lang.String categoryString = getCategoryString(item).orElse("NAN");
        if (!NumberUtils.isDigits(categoryString)) {
            return -1;
        }
        return Integer.parseInt(categoryString);
    }

    public static Optional<String> getCategoryString(@NotNull final Item item) {
        final mgi.types.config.items.ItemDefinitions definitions = ItemDefinitions.getOrThrow(item.getDefinitions().getUnnotedOrDefault());
        final it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<java.lang.Object> params = definitions.getParameters();
        if (params == null) {
            return Optional.empty();
        }
        final java.lang.Object category = params.get(709);
        if (category == null) {
            return Optional.empty();
        }
        return Optional.of(category.toString());
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.SEED_VAULT;
    }
}
