package com.zenyte.game.content.skills.construction.objects.chapel;

import com.zenyte.game.content.skills.construction.Construction;
import com.zenyte.game.content.skills.construction.ObjectInteraction;
import com.zenyte.game.content.skills.construction.RoomReference;
import com.zenyte.game.content.skills.prayer.actions.Bones;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemOnObjectAction;
import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.Action;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.dailychallenge.challenge.SkillingChallenge;
import com.zenyte.game.world.object.WorldObject;

import java.util.ArrayList;

/**
 * @author Kris | 25. veebr 2018 : 21:12.34
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>}
 */
public final class Altar implements ObjectInteraction, ItemOnObjectAction {
	private static final Animation PRAY_ANIM = new Animation(645);
	private static final float[] BASE_MODIFIERS = new float[] {1.0F, 1.1F, 1.25F, 1.5F, 1.75F, 2.0F, 2.5F};
	public static final int[][][] LIGHTER_OFFSETS = new int[][][] {new int[][] {new int[] {-2, 0}, new int[] {3, 0}}, new int[][] {new int[] {0, 3}, new int[] {0, -2}}, new int[][] {new int[] {3, 0}, new int[] {-2, 0}}, new int[][] {new int[] {0, -2}, new int[] {0, 3}}};

	@Override
	public Object[] getObjects() {
		return new Object[] {13179, 13180, 13181, 13182, 13183, 13184, 13185, 13186, 13187, 13188, 13189, 13190, 13191, 13192, 13193, 13194, 13195, 13196, 13197, 13198, 13199};
	}

	@Override
	public void handleItemOnObjectAction(final Player player, final Item item, int slot, final WorldObject object) {
		if (object.getId() == 411 && object.getRegionId() != 11835) {
			player.sendMessage("Nothing interesting happens.");
			return;
		}
		final com.zenyte.game.content.skills.prayer.actions.Bones bone = Bones.getBone(item.getId());
		if (bone == null) {
			player.sendMessage("You can only offer bones to the gods.");
			return;
		}
		final int[][] offsets = LIGHTER_OFFSETS[object.getRotation()];
		final com.zenyte.game.world.entity.Location leftBurner = new Location(object.getX() + (offsets[0][0]), object.getY() + (offsets[0][1]), object.getPlane());
		final com.zenyte.game.world.entity.Location rightBurner = new Location(object.getX() + (offsets[1][0]), object.getY() + (offsets[1][1]), object.getPlane());
		player.getActionManager().setAction(new OfferingAction(bone, item, object, leftBurner, rightBurner));
	}

	@Override
	public Object[] getItems() {
		final java.util.ArrayList<java.lang.Object> list = new ArrayList<Object>(Bones.VALUES.length);
		for (final com.zenyte.game.content.skills.prayer.actions.Bones bone : Bones.VALUES) {
			for (final com.zenyte.game.item.Item b : bone.getBones()) {
				list.add(b.getId());
			}
		}
		return list.toArray(new Object[list.size()]);
	}

	@Override
	public void handleObjectAction(final Player player, final Construction construction, final RoomReference reference, final WorldObject object, final int optionId, final String option) {
		if (option.equals("Pray")) {
			if (player.getPrayerManager().getPrayerPoints() >= player.getSkills().getLevelForXp(Skills.PRAYER)) {
				player.sendMessage("You already have full prayer points.");
				return;
			}
			player.lock();
			player.sendMessage("You pray to the gods...");
			player.sendSound(2674);
			player.setAnimation(PRAY_ANIM);
			WorldTasksManager.schedule(() -> {
				player.getPrayerManager().restorePrayerPoints(99);
				player.sendMessage("... and recharge your prayer.");
				player.unlock();
			});
		}
	}


	public static final class OfferingAction extends Action {
		private static final String OFFERING_MESSAGE = "The gods are very pleased with your offering.";
		private static final String CHAOS_ALTAR_MESSAGE = "The Dark Lord spares your sacrifice but still rewards you for your efforts.";
		private static final Animation OFFERING_ANIM = new Animation(3705);
		private static final Graphics OFFERING_GFX = new Graphics(624);

		public OfferingAction(final Bones bone, final Item item, final WorldObject altar, final Location leftBurner, final Location rightBurner) {
			this.bone = bone;
			this.item = item;
			this.altar = altar;
			this.leftBurner = leftBurner;
			this.rightBurner = rightBurner;
			final int index = (altar.getId() - 13179) / 3;
			baseModifier = altar.getId() == 411 ? 3.5F : altar.getId() == 18258 ? 1.75F : (index >= BASE_MODIFIERS.length || index < 0) ? 1 : BASE_MODIFIERS[index];
		}

		private final Item item;
		private final Bones bone;
		private final WorldObject altar;
		private final Location leftBurner;
		private final Location rightBurner;
		private final float baseModifier;

		@Override
		public boolean initiateOnPacketReceive() {
			return true;
		}

		@Override
		public boolean start() {
			if (!player.getInventory().containsItem(item)) {
				player.sendMessage("You don\'t have any " + item.getName().toLowerCase() + " to sacrifice.");
				return false;
			}
			if (bone == Bones.SUPERIOR_DRAGON_BONES) {
				if (player.getSkills().getLevelForXp(Skills.PRAYER) < 70) {
					player.sendMessage("You need a Prayer level of at least 70 to sacrifice superior dragon bones.");
					return false;
				}
			}
			return true;
		}

		@Override
		public void stop() {
			player.getActionManager().setActionDelay(1);
		}

		@Override
		public boolean process() {
			return true;
		}

		@Override
		public int processWithDelay() {
			if (!player.getInventory().containsItem(item)) {
				return -1;
			}
			float modifier = baseModifier;
			final com.zenyte.game.world.object.WorldObject leftBurnerObj = World.getObjectWithType(leftBurner, 10);
			final com.zenyte.game.world.object.WorldObject rightBurnerObj = World.getObjectWithType(rightBurner, 10);
			if (leftBurnerObj != null) {
				final int id = leftBurnerObj.getId();
				if (id == 13209 || id == 13211 || id == 13213) {
					modifier += 0.5F;
				}
			}
			if (rightBurnerObj != null) {
				final int id = rightBurnerObj.getId();
				if (id == 13209 || id == 13211 || id == 13213) {
					modifier += 0.5F;
				}
			}
			player.setAnimation(OFFERING_ANIM);
			player.faceObject(altar);
			if (bone.equals(Bones.DRAGON_BONES)) {
				player.getDailyChallengeManager().update(SkillingChallenge.OFFER_DRAGON_BONES);
			}
			if (altar.getId() != 411) {
				player.sendFilteredMessage(OFFERING_MESSAGE);
				player.getInventory().deleteItem(item);
			} else {
				if (Utils.random(1) == 0) {
					player.sendFilteredMessage(CHAOS_ALTAR_MESSAGE);
				} else {
					player.getInventory().deleteItem(item);
				}
			}
			player.getSkills().addXp(Skills.PRAYER, bone.getXp() * modifier);
			if (altar.getId() != 18258 && altar.getId() != 411) {
				World.sendGraphics(OFFERING_GFX, altar);
			} else if (altar.getId() == 18258) {
				World.sendGraphics(OFFERING_GFX, altar.transform(0, 1, 0));
			}
			return 3;
		}
	}
}
