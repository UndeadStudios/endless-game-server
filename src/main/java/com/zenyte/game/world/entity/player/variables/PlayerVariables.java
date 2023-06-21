package com.zenyte.game.world.entity.player.variables;

import com.google.common.eventbus.Subscribe;
import com.zenyte.Constants;
import com.zenyte.GameEngine;
import com.zenyte.cores.CoresManager;
import com.zenyte.game.BonusXpManager;
import com.zenyte.game.ui.InterfacePosition;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.TimeUnit;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.action.combat.PlayerCombat;
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot;
import com.zenyte.game.world.entity.player.loyalty.LoyaltyManager;
import com.zenyte.game.world.region.area.EvilBobIsland;
import com.zenyte.game.world.region.area.plugins.RandomEventRestrictionPlugin;
import com.zenyte.plugins.events.LogoutEvent;
import org.apache.logging.log4j.util.Strings;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PlayerVariables {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerVariables.class);
	private transient Player player;

	public PlayerVariables(final Player player) {
		this.player = player;
		resetCharacterSaveTimer();
	}

	public void set(final PlayerVariables copy) {
		copy.scheduled.forEach((k, v) -> {
			try {
				final com.zenyte.game.world.entity.player.variables.TickVariable tickTask = TickVariable.valueOf(TickVariable.class, k);
				scheduled.put(k, new Variable(v.ticks, tickTask.task, tickTask.messages));
			} catch (Exception e) {
				log.error(Strings.EMPTY, e);
			}
		});
		bonusXP = copy.bonusXP;
		raidsBoost = copy.raidsBoost;
		runEnergy = copy.runEnergy;
		playTime = copy.playTime;
		absorption = copy.absorption;
		skulled = copy.skulled;
		lastLogin = copy.lastLogin;
		raidAdvertsQuota = copy.raidAdvertsQuota;
		ardougneFarmTeleports = copy.ardougneFarmTeleports;
		fountainOfRuneTeleports = copy.fountainOfRuneTeleports;
		fishingColonyTeleports = copy.fishingColonyTeleports;
		rellekkaTeleports = copy.rellekkaTeleports;
		sherlockTeleports = copy.sherlockTeleports;
		faladorPrayerRecharges = copy.faladorPrayerRecharges;
		runReplenishments = copy.runReplenishments;
		freeAlchemyCasts = copy.freeAlchemyCasts;
		cabbageFieldTeleports = copy.cabbageFieldTeleports;
		nardahTeleports = copy.nardahTeleports;
		spellbookSwaps = copy.spellbookSwaps;
		claimedBattlestaves = copy.claimedBattlestaves;
		grappleAndCrossbowSearches = copy.grappleAndCrossbowSearches;
		this.teletabPurchases = copy.teletabPurchases;
		this.zulrahResurrections = copy.zulrahResurrections;
		zulrahResurrections = copy.zulrahResurrections;
		kourendWoodlandTeleports = copy.kourendWoodlandTeleports;
		mountKaruulmTeleports = copy.mountKaruulmTeleports;
	}

	private transient long lastClanKick;
	private double runEnergy = 100;
	private transient int potionDelay;
	private transient int foodDelay;
	private transient int karambwanDelay;
	private transient int nextScheduledCharacterSave;
	private transient int ticksInterval;
	private int bonusXP;
	private int raidsBoost;
	private int playTime;
	private int lastLogin;
	private int raidAdvertsQuota = 15;
	private int slimePitTeleports;
	private int ardougneFarmTeleports;
	private int fountainOfRuneTeleports;
	private int fishingColonyTeleports;
	private int sherlockTeleports;
	private int faladorPrayerRecharges;
	private int rellekkaTeleports;
	private int runReplenishments;
	private int freeAlchemyCasts;
	private int cabbageFieldTeleports;
	private int nardahTeleports;
	private int kourendWoodlandTeleports;
	private int mountKaruulmTeleports;
	private int spellbookSwaps;
	private int zulrahResurrections;
	private int absorption;
	private int overloadType;
	private int toleranceTimer;
	private int specRegeneration;
	private int statRegeneration;
	private int healthRegeneration;
	private int grappleAndCrossbowSearches;
	private int teletabPurchases;
	private boolean skulled;
	private boolean claimedBattlestaves;
	private final Map<String, Variable> scheduled = new LinkedHashMap<>();

	private void resetCharacterSaveTimer() {
		nextScheduledCharacterSave = Constants.WORLD_PROFILE.isDevelopment() ? (int) TimeUnit.SECONDS.toTicks(15) : ((int) TimeUnit.MINUTES.toTicks(5));
	}


	public enum HealthRegenBoost {
		RAPID_HEAL, REGEN_BRACELET, HITPOINTS_CAPE;
	}

	private final transient Set<HealthRegenBoost> healthRegenBoosts = EnumSet.noneOf(HealthRegenBoost.class);
	private transient int cycle = calculateCycle();

	public void addBoost(final HealthRegenBoost boost) {
		healthRegenBoosts.add(boost);
		final int current = cycle;
		cycle = calculateCycle();
		if (current > cycle || boost == HealthRegenBoost.RAPID_HEAL) {
			healthRegeneration = 0;
		}
	}

	public void removeBoost(final HealthRegenBoost boost) {
		healthRegenBoosts.remove(boost);
		cycle = calculateCycle();
		if (boost == HealthRegenBoost.RAPID_HEAL) {
			healthRegeneration = 0;
		}
	}

	public void resetTeleblock() {
		scheduled.remove(TickVariable.TELEBLOCK.toString());
		scheduled.remove(TickVariable.TELEBLOCK_IMMUNITY.toString());
	}

	@Subscribe
	public static final void onLogout(final LogoutEvent event) {
		event.getPlayer().getVariables().resetTeleblock();
	}

	private int calculateCycle() {
		if (healthRegenBoosts.isEmpty()) {
			return 100;
		}
		int delay = 100;
		if (healthRegenBoosts.contains(HealthRegenBoost.REGEN_BRACELET)) {
			delay /= 2;
		}
		if (healthRegenBoosts.contains(HealthRegenBoost.RAPID_HEAL) || healthRegenBoosts.contains(HealthRegenBoost.HITPOINTS_CAPE)) {
			delay /= 2;
		}
		return delay;
	}

	/**
	 * Schedules a tickvariable to be executed on the player if there isn't one
	 * of the same type with longer duration already executing.
	 * 
	 * @param duration
	 *            the duration of the variable.
	 * @param variable
	 *            the variable itself.
	 */
	public void schedule(final int duration, final TickVariable variable) {
		final com.zenyte.game.world.entity.player.variables.Variable existing = scheduled.get(variable.toString());
		if (existing != null) {
			if (existing.ticks >= duration) {
				return;
			}
		}
		scheduled.put(variable.toString(), new Variable(duration, variable.task, variable.messages));
	}

	public void cancel(final TickVariable variable) {
		scheduled.remove(variable.toString());
	}

	public void resetScheduled() {
		scheduled.forEach((k, v) -> {
			if (v.task != null) {
				v.task.run(player, 0);
			}
		});
		scheduled.clear();
		if (player.getEquipment().getId(EquipmentSlot.AMULET) == 22557) {
			player.getVariables().setPermanentSkull();
		}
	}

	public int getTime(final TickVariable variable) {
		final com.zenyte.game.world.entity.player.variables.Variable scheduled = this.scheduled.get(variable.toString());
		if (scheduled == null) {
			return 0;
		}
		return scheduled.ticks;
	}

	private static final int[] processedSkills;

	static {
		processedSkills = new int[Skills.SKILLS.length - 1];
		int index = 0;
		for (int i = 0; i < Skills.SKILLS.length; i++) {
			if (i == Skills.PRAYER) continue;
			processedSkills[index++] = i;
		}
	}

	public void onLogin() {
		if (getTime(TickVariable.STAMINA_ENHANCEMENT) > 0) {
			player.getVarManager().sendBit(25, 1);
		}
	}

	public void process() {
		playTime++;
		specRegeneration++;
		toleranceTimer++;
		if (raidsBoost > 0) {
			final int raidsBitValue = player.getVarManager().getBitValue(5425);
			if (raidsBitValue > 0 && raidsBitValue < 5) {
				raidsBoost--;
			}
		}
		if (bonusXP > 0) {
			if (!Constants.BOOSTED_XP) {
				bonusXP--;
				if (bonusXP == 0) {
					player.sendMessage(Colour.RED.wrap("Your private bonus experience has ran out!"));
				}
			}
		}
		if (Utils.random(Constants.randomEvent) == 0) {
			if (!player.isLocked() && !player.isFinished() && !player.isDead() && !player.getInterfaceHandler().containsInterface(InterfacePosition.CENTRAL) && !player.isStaff()) {
				final long lastEvent = player.getNumericAttribute("last random event").longValue();
				if (lastEvent + TimeUnit.HOURS.toMillis(1) < System.currentTimeMillis() && player.getActionManager().getLastAction() >= (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5))) {
					if (!player.isUnderCombat() && !(player.getActionManager().getAction() instanceof PlayerCombat)) {
						final com.zenyte.game.world.region.Area area = player.getArea();
						if (!(area instanceof RandomEventRestrictionPlugin)) {
							EvilBobIsland.teleport(player);
						}
					}
				}
			}
		}
		//Resynchronize the client timers for time spent online and server online time in general.
		if (++ticksInterval % 100 == 0) {
			final com.zenyte.game.world.entity.player.VarManager varManager = player.getVarManager();
			varManager.sendVar(3500, (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - GameEngine.SERVER_START_TIME));
			varManager.sendVar(3501, (int) (playTime * 0.6F));
			varManager.sendVar(3506, Math.max(0, (int) TimeUnit.MILLISECONDS.toSeconds(BonusXpManager.expirationDate - System.currentTimeMillis())));
			varManager.sendVar(3507, (int) (raidsBoost * 0.6F));
			varManager.sendVar(3801, (int) (bonusXP * 0.6F));
			player.refreshGameClock();
		}
		if (ticksInterval % LoyaltyManager.LOYALTY_POINTS_INTERVAL_TICKS == 0) {
			player.getLoyaltyManager().informSession((int) (ticksInterval / LoyaltyManager.LOYALTY_POINTS_INTERVAL_TICKS));
		}
		if (--nextScheduledCharacterSave <= 0) {
			resetCharacterSaveTimer();
			CoresManager.getLoginManager().submitSaveRequest(player);
		}
		if (foodDelay > 0) {
			foodDelay--;
		}
		if (potionDelay > 0) {
			potionDelay--;
		}
		if (karambwanDelay > 0) {
			karambwanDelay--;
		}
		if (player.getCombatDefinitions().getSpecialEnergy() == 100) {
			specRegeneration = 0;
		}
		if (specRegeneration % 50 == 0) {
			if (player.getCombatDefinitions().getSpecialEnergy() < 100) {
				final int energy = player.getCombatDefinitions().getSpecialEnergy();
				player.getCombatDefinitions().setSpecialEnergy(Math.min(100, energy + 10));
			}
		}
		statRegeneration++;
		if ( /*(manager.isPrayerActive(Prayer.RAPID_RESTORE) ? 25 : 50)*/statRegeneration % 100 == 0) {
			//TODO: Rewrite
			// this entire shit.
			for (int skill : processedSkills) {
				final int currentLevel = player.getSkills().getLevel(skill);
				final int normalLevel = player.getSkills().getLevelForXp(skill);
				if (currentLevel > normalLevel) {
					player.getSkills().setLevel(skill, currentLevel - 1);
				} else if (currentLevel < normalLevel) {
					if (skill != Skills.HITPOINTS) {
						player.getSkills().setLevel(skill, currentLevel + 1);
					}
				}
			}
		}
		healthRegeneration++;
		if (healthRegeneration % cycle == 0) {
			if (!player.isDead() && player.getHitpoints() < player.getMaxHitpoints()) {
				player.setHitpoints(player.getHitpoints() + 1);
			}
		}
		if (!scheduled.isEmpty()) {
			scheduled.values().removeIf(variable -> {
				variable.ticks--;
				if (variable.messages != null) {
					final java.lang.String message = variable.messages.get(variable.ticks);
					if (message != null) {
						player.sendMessage(message);
					}
				}
				if (variable.task != null) {
					variable.task.run(player, variable.ticks);
				}
				return variable.ticks <= 0;
			});
		}
	}

	public void setRunEnergy(double runEnergy) {
		if (runEnergy > 100) {
			runEnergy = 100;
		} else if (runEnergy < 0) {
			runEnergy = 0;
		}
		if (this.runEnergy == runEnergy) {
			return;
		}
		this.runEnergy = runEnergy;
		player.getPacketDispatcher().sendRunEnergy();
	}

	public void forceRunEnergy(double runEnergy) {
		if (runEnergy < 0) {
			runEnergy = 0;
		}
		this.runEnergy = runEnergy;
		player.getPacketDispatcher().sendRunEnergy();
	}

	public void setPermanentSkull() {
		this.skulled = true;
		schedule(Integer.MAX_VALUE, TickVariable.SKULL);
		player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
	}

	public void setSkull(final boolean skulled) {
		this.skulled = skulled;
		if (skulled) {
			schedule((int) TimeUnit.MINUTES.toTicks(20), TickVariable.SKULL);
		}
		player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
	}

	public void removeSkull() {
		this.skulled = false;
		scheduled.remove(TickVariable.SKULL.toString());
		player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
	}

	public Player getPlayer() {
		return this.player;
	}

	public long getLastClanKick() {
		return this.lastClanKick;
	}

	public double getRunEnergy() {
		return this.runEnergy;
	}

	public int getPotionDelay() {
		return this.potionDelay;
	}

	public int getFoodDelay() {
		return this.foodDelay;
	}

	public int getKarambwanDelay() {
		return this.karambwanDelay;
	}

	public int getNextScheduledCharacterSave() {
		return this.nextScheduledCharacterSave;
	}

	public int getTicksInterval() {
		return this.ticksInterval;
	}

	public int getBonusXP() {
		return this.bonusXP;
	}

	public int getRaidsBoost() {
		return this.raidsBoost;
	}

	public int getPlayTime() {
		return this.playTime;
	}

	public int getLastLogin() {
		return this.lastLogin;
	}

	public int getRaidAdvertsQuota() {
		return this.raidAdvertsQuota;
	}

	public int getSlimePitTeleports() {
		return this.slimePitTeleports;
	}

	public int getArdougneFarmTeleports() {
		return this.ardougneFarmTeleports;
	}

	public int getFountainOfRuneTeleports() {
		return this.fountainOfRuneTeleports;
	}

	public int getFishingColonyTeleports() {
		return this.fishingColonyTeleports;
	}

	public int getSherlockTeleports() {
		return this.sherlockTeleports;
	}

	public int getFaladorPrayerRecharges() {
		return this.faladorPrayerRecharges;
	}

	public int getRellekkaTeleports() {
		return this.rellekkaTeleports;
	}

	public int getRunReplenishments() {
		return this.runReplenishments;
	}

	public int getFreeAlchemyCasts() {
		return this.freeAlchemyCasts;
	}

	public int getCabbageFieldTeleports() {
		return this.cabbageFieldTeleports;
	}

	public int getNardahTeleports() {
		return this.nardahTeleports;
	}

	public int getKourendWoodlandTeleports() {
		return this.kourendWoodlandTeleports;
	}

	public int getMountKaruulmTeleports() {
		return this.mountKaruulmTeleports;
	}

	public int getSpellbookSwaps() {
		return this.spellbookSwaps;
	}

	public int getZulrahResurrections() {
		return this.zulrahResurrections;
	}

	public int getAbsorption() {
		return this.absorption;
	}

	public int getOverloadType() {
		return this.overloadType;
	}

	public int getToleranceTimer() {
		return this.toleranceTimer;
	}

	public int getSpecRegeneration() {
		return this.specRegeneration;
	}

	public int getStatRegeneration() {
		return this.statRegeneration;
	}

	public int getHealthRegeneration() {
		return this.healthRegeneration;
	}

	public int getGrappleAndCrossbowSearches() {
		return this.grappleAndCrossbowSearches;
	}

	public int getTeletabPurchases() {
		return this.teletabPurchases;
	}

	public boolean isSkulled() {
		return this.skulled;
	}

	public boolean isClaimedBattlestaves() {
		return this.claimedBattlestaves;
	}

	public Map<String, Variable> getScheduled() {
		return this.scheduled;
	}

	public int getCycle() {
		return this.cycle;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public void setLastClanKick(final long lastClanKick) {
		this.lastClanKick = lastClanKick;
	}

	public void setPotionDelay(final int potionDelay) {
		this.potionDelay = potionDelay;
	}

	public void setFoodDelay(final int foodDelay) {
		this.foodDelay = foodDelay;
	}

	public void setKarambwanDelay(final int karambwanDelay) {
		this.karambwanDelay = karambwanDelay;
	}

	public void setNextScheduledCharacterSave(final int nextScheduledCharacterSave) {
		this.nextScheduledCharacterSave = nextScheduledCharacterSave;
	}

	public void setTicksInterval(final int ticksInterval) {
		this.ticksInterval = ticksInterval;
	}

	public void setBonusXP(final int bonusXP) {
		this.bonusXP = bonusXP;
	}

	public void setRaidsBoost(final int raidsBoost) {
		this.raidsBoost = raidsBoost;
	}

	public void setPlayTime(final int playTime) {
		this.playTime = playTime;
	}

	public void setLastLogin(final int lastLogin) {
		this.lastLogin = lastLogin;
	}

	public void setRaidAdvertsQuota(final int raidAdvertsQuota) {
		this.raidAdvertsQuota = raidAdvertsQuota;
	}

	public void setSlimePitTeleports(final int slimePitTeleports) {
		this.slimePitTeleports = slimePitTeleports;
	}

	public void setArdougneFarmTeleports(final int ardougneFarmTeleports) {
		this.ardougneFarmTeleports = ardougneFarmTeleports;
	}

	public void setFountainOfRuneTeleports(final int fountainOfRuneTeleports) {
		this.fountainOfRuneTeleports = fountainOfRuneTeleports;
	}

	public void setFishingColonyTeleports(final int fishingColonyTeleports) {
		this.fishingColonyTeleports = fishingColonyTeleports;
	}

	public void setSherlockTeleports(final int sherlockTeleports) {
		this.sherlockTeleports = sherlockTeleports;
	}

	public void setFaladorPrayerRecharges(final int faladorPrayerRecharges) {
		this.faladorPrayerRecharges = faladorPrayerRecharges;
	}

	public void setRellekkaTeleports(final int rellekkaTeleports) {
		this.rellekkaTeleports = rellekkaTeleports;
	}

	public void setRunReplenishments(final int runReplenishments) {
		this.runReplenishments = runReplenishments;
	}

	public void setFreeAlchemyCasts(final int freeAlchemyCasts) {
		this.freeAlchemyCasts = freeAlchemyCasts;
	}

	public void setCabbageFieldTeleports(final int cabbageFieldTeleports) {
		this.cabbageFieldTeleports = cabbageFieldTeleports;
	}

	public void setNardahTeleports(final int nardahTeleports) {
		this.nardahTeleports = nardahTeleports;
	}

	public void setKourendWoodlandTeleports(final int kourendWoodlandTeleports) {
		this.kourendWoodlandTeleports = kourendWoodlandTeleports;
	}

	public void setMountKaruulmTeleports(final int mountKaruulmTeleports) {
		this.mountKaruulmTeleports = mountKaruulmTeleports;
	}

	public void setSpellbookSwaps(final int spellbookSwaps) {
		this.spellbookSwaps = spellbookSwaps;
	}

	public void setZulrahResurrections(final int zulrahResurrections) {
		this.zulrahResurrections = zulrahResurrections;
	}

	public void setAbsorption(final int absorption) {
		this.absorption = absorption;
	}

	public void setOverloadType(final int overloadType) {
		this.overloadType = overloadType;
	}

	public void setToleranceTimer(final int toleranceTimer) {
		this.toleranceTimer = toleranceTimer;
	}

	public void setSpecRegeneration(final int specRegeneration) {
		this.specRegeneration = specRegeneration;
	}

	public void setStatRegeneration(final int statRegeneration) {
		this.statRegeneration = statRegeneration;
	}

	public void setHealthRegeneration(final int healthRegeneration) {
		this.healthRegeneration = healthRegeneration;
	}

	public void setGrappleAndCrossbowSearches(final int grappleAndCrossbowSearches) {
		this.grappleAndCrossbowSearches = grappleAndCrossbowSearches;
	}

	public void setTeletabPurchases(final int teletabPurchases) {
		this.teletabPurchases = teletabPurchases;
	}

	public void setSkulled(final boolean skulled) {
		this.skulled = skulled;
	}

	public void setClaimedBattlestaves(final boolean claimedBattlestaves) {
		this.claimedBattlestaves = claimedBattlestaves;
	}

	public void setCycle(final int cycle) {
		this.cycle = cycle;
	}

	public Set<HealthRegenBoost> getHealthRegenBoosts() {
		return this.healthRegenBoosts;
	}
}
