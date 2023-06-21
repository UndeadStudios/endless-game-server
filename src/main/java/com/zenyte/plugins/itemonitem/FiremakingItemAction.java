package com.zenyte.plugins.itemonitem;

import com.zenyte.game.content.skills.firemaking.Firemaking;
import com.zenyte.game.content.skills.firemaking.FiremakingAction;
import com.zenyte.game.content.skills.firemaking.FiremakingTool;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemOnItemAction;
import com.zenyte.game.world.entity.player.Player;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

public final class FiremakingItemAction implements ItemOnItemAction {
    @Override
    public void handleItemOnItemAction(final Player player, final Item from, final Item to, final int fromSlot, final int toSlot) {
        player.stop(Player.StopType.WALK, Player.StopType.ROUTE_EVENT);
        final java.util.Optional<com.zenyte.game.content.skills.firemaking.FiremakingTool> optionalTool = FiremakingTool.getTool(from.getId(), to.getId());
        if (!optionalTool.isPresent()) {
            return;
        }
        final com.zenyte.game.content.skills.firemaking.FiremakingTool tool = optionalTool.get();
        final com.zenyte.game.item.Item logs = ArrayUtils.contains(tool.getBowIds(), from.getId()) ? to : from;
        final com.zenyte.game.content.skills.firemaking.Firemaking firemakingType = Firemaking.MAP.get(logs.getId());
        if (firemakingType == null) {
            return;
        }
        player.getActionManager().setAction(new FiremakingAction(firemakingType, logs == from ? fromSlot : toSlot, false, tool, Optional.empty()));
    }

    @Override
    public int[] getItems() {
        final it.unimi.dsi.fastutil.ints.IntArrayList list = new IntArrayList();
        list.add(FiremakingAction.TINDERBOX.getId());
        for (final com.zenyte.game.content.skills.firemaking.Firemaking data : Firemaking.VALUES) {
            list.add(data.getLogs().getId());
        }
        for (final com.zenyte.game.content.skills.firemaking.FiremakingTool bows : FiremakingTool.values) {
            for (int id : bows.getBowIds()) {
                list.add(id);
            }
        }
        return list.toArray(new int[list.size()]);
    }
}
