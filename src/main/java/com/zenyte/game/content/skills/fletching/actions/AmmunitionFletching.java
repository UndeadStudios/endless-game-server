package com.zenyte.game.content.skills.fletching.actions;

import com.zenyte.game.content.achievementdiary.diaries.DesertDiary;
import com.zenyte.game.content.achievementdiary.diaries.VarrockDiary;
import com.zenyte.game.content.skills.fletching.FletchingDefinitions.AmmunitionFletchingData;
import com.zenyte.game.content.skills.fletching.FletchingDefinitions.AmmunitionType;
import com.zenyte.game.content.treasuretrails.clues.SherlockTask;
import com.zenyte.game.item.Item;
import com.zenyte.game.world.entity.player.Action;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.dailychallenge.challenge.SkillingChallenge;

/**
 * @author Tommeh | 25 aug. 2018 | 18:15:31
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server
 *      profile</a>}
 */
public class AmmunitionFletching extends Action {
	private final AmmunitionFletchingData data;
	private final int amount;
	private final boolean dialogue;
	private int cycle;
	private int ticks;

	@Override
	public boolean start() {
		final int sets = AmmunitionFletchingData.setsOf15(data) ? 15 : 10;
		final int quantity = dialogue ? (amount * sets / amount) : amount;
		for (final com.zenyte.game.item.Item item : data.getMaterials()) {
			if (!player.getInventory().containsItem(new Item(item.getId(), quantity * item.getAmount()))) {
				player.sendMessage("You don\'t have the necessary items to do this.");
				return false;
			}
		}
		if (!player.getInventory().hasFreeSlots() && (!data.getProduct().isStackable() || !player.getInventory().containsItem(data.getProduct().getId(), 1))) {
			player.sendMessage("You need some more free inventory space to do this.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		final boolean instant = data.getType().equals(AmmunitionType.DART) || data.getType().equals(AmmunitionType.BOLT) || data.getType().equals(AmmunitionType.TIPPED_BOLT);
		final int sets = AmmunitionFletchingData.setsOf15(data) ? 15 : 10;
		final int quantity = dialogue ? (amount * sets / amount) : amount;
		final boolean condition = cycle < (instant ? 1 : amount);
		if (!condition) {
			return false;
		}
		for (final com.zenyte.game.item.Item item : data.getMaterials()) {
			if (!player.getInventory().containsItem(new Item(item.getId(), quantity * item.getAmount()))) {
				player.sendMessage("You don\'t have the necessary items to do this.");
				return false;
			}
		}
		return true;
	}

	@Override
	public int processWithDelay() {
		final int sets = AmmunitionFletchingData.setsOf15(data) ? 15 : 10;
		final int quantity = dialogue ? (amount * sets / amount) : amount;
		final int productMultiplier = (amount > sets ? sets : quantity) * data.getProduct().getAmount();
		final int materialMultiplier = (amount > sets ? sets : quantity);
		final boolean instant = 
		/*data.getType().equals(AmmunitionType.ARROW) || */data.getType().equals(AmmunitionType.DART) || data.getType().equals(AmmunitionType.BOLT) || data.getType().equals(AmmunitionType.TIPPED_BOLT);
		if (data.equals(AmmunitionFletchingData.DRAGON_DART)) {
			player.getAchievementDiaries().update(DesertDiary.FLETCH_DRAGON_DARTS, productMultiplier);
		} else if (data.equals(AmmunitionFletchingData.RUNE_DART)) {
			SherlockTask.FLETCH_RUNE_DART.progress(player);
			if (productMultiplier >= 10) {
				player.getAchievementDiaries().update(VarrockDiary.SMITH_AND_FLETCH_10_RUNE_DARTS, 2);
			}
		} else if (data.equals(AmmunitionFletchingData.RUBY_BOLT)) {
			player.getDailyChallengeManager().update(SkillingChallenge.FLETCH_RUBY_BOLTS, productMultiplier);
		} else if (data.equals(AmmunitionFletchingData.DRAGON_BOLT)) {
			player.getDailyChallengeManager().update(SkillingChallenge.FLETCH_DRAGON_BOLTS, productMultiplier);
		} else if (data.equals(AmmunitionFletchingData.BROAD_BOLT)) {
			player.getDailyChallengeManager().update(SkillingChallenge.FLETCH_BROAD_BOLTS, productMultiplier);
		} else if (data.equals(AmmunitionFletchingData.IRON_DART)) {
			player.getDailyChallengeManager().update(SkillingChallenge.FLETCH_IRON_DARTS, productMultiplier);
		}
		if (data.getType().equals(AmmunitionType.ARROW) || instant) {
			player.sendFilteredMessage(data.getProcessMessage(data, sets, quantity, amount));
			player.getSkills().addXp(Skills.FLETCHING, data.getXp() * productMultiplier);
			for (final com.zenyte.game.item.Item item : data.getMaterials()) {
				player.getInventory().deleteItem(new Item(item.getId(), materialMultiplier * item.getAmount()));
			}
			player.getInventory().addOrDrop(new Item(data.getProduct().getId(), productMultiplier));
		} else {
			if (ticks++ == 1) {
				player.sendFilteredMessage(data.getProcessMessage(data, sets, quantity, amount));
				player.getSkills().addXp(Skills.FLETCHING, data.getXp() * productMultiplier);
				for (final com.zenyte.game.item.Item item : data.getMaterials()) {
					player.getInventory().deleteItem(new Item(item.getId(), materialMultiplier * item.getAmount()));
				}
				player.getInventory().addOrDrop(new Item(data.getProduct().getId(), productMultiplier));
				ticks = 0;
			}
		}
		cycle++;
		return data.getType().equals(AmmunitionType.ARROW) ? 1 : instant ? 0 : 1;
	}

	public AmmunitionFletching(final AmmunitionFletchingData data, final int amount, final boolean dialogue) {
		this.data = data;
		this.amount = amount;
		this.dialogue = dialogue;
	}
}
