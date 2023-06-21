package mgi.types.config.items;

import java.util.HashMap;

public class WearableDefinition {
	private HashMap<Integer, Integer> requirements;
	private String bonuses;
	WieldableDefinition weaponDefinition;

	public HashMap<Integer, Integer> getRequirements() {
		return this.requirements;
	}

	public void setRequirements(final HashMap<Integer, Integer> requirements) {
		this.requirements = requirements;
	}

	public String getBonuses() {
		return this.bonuses;
	}

	public void setBonuses(final String bonuses) {
		this.bonuses = bonuses;
	}

	public WieldableDefinition getWeaponDefinition() {
		return this.weaponDefinition;
	}

	public void setWeaponDefinition(final WieldableDefinition weaponDefinition) {
		this.weaponDefinition = weaponDefinition;
	}
}
