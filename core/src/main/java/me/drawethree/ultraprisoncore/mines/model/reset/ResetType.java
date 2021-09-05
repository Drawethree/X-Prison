package me.drawethree.ultraprisoncore.mines.model.reset;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;

import java.util.Map;

public abstract class ResetType {

	public static final GradualReset GRADUAL = new GradualReset();

	public static final InstantReset INSTANT = new InstantReset();

	private final String name;

	ResetType(String paramString) {
		this.name = paramString;
	}

	public abstract void reset(Mine paramMine, Map<CompMaterial, Double> blocks);

	public String getName() {
		return this.name;
	}
}