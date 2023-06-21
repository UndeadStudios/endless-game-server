package com.zenyte.game.content.minigame.pestcontrol.plugins;

import com.zenyte.game.content.minigame.pestcontrol.PestControlInstance;
import com.zenyte.game.content.minigame.pestcontrol.PestControlUtilities;
import com.zenyte.game.item.Item;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;

/**
 * @author Kris | 27. juuni 2018 : 16:09:03
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class BarricadePlugin implements ObjectAction {
	private static final Animation HAMMERING = new Animation(3676);
	private static final Item LOGS = new Item(1511);
	private static final Item HAMMER = new Item(2347);

	@Override
	public void handleObjectAction(final Player player, final WorldObject object, final String name, final int optionId, final String option) {
		if (option.equals("Repair")) {
			final com.zenyte.game.world.region.Area area = player.getArea();
			if (!(area instanceof PestControlInstance)) {
				return;
			}
			final com.zenyte.game.content.minigame.pestcontrol.PestControlInstance instance = (PestControlInstance) area;
			final com.zenyte.game.world.entity.player.container.impl.Inventory inventory = player.getInventory();
			if (!inventory.containsItem(LOGS)) {
				player.sendMessage("You need some logs to repair the barricade.");
				return;
			}
			if (!inventory.containsItem(HAMMER)) {
				player.sendMessage("You need a hammer to repair the barricade.");
				return;
			}
			player.lock(2);
			player.setAnimation(HAMMERING);
			inventory.deleteItem(LOGS);
			instance.addActivity(player, PestControlUtilities.MODERATE_ACTIVITY_POINTS);
			final int id = object.getId();
			World.removeObject(object);
			final int offset = (id - 14224) % 3;
			System.err.println(14224 + offset);
			World.spawnObject(new WorldObject(14224 + offset, id == 14227 || id == 14230 ? 0 : 9, object.getRotation(), object));
		}
	}

	@Override
	public Object[] getObjects() {
		return new Object[] {14227, 14228, 14229, 14230, 14231, 14232};
	}
}
