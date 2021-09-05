package me.drawethree.ultraprisoncore.mines.model.reset;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;

import java.util.Map;

public class GradualReset extends ResetType {

	private final int DELAY = 1;

	private final int CHANGES_PER_TICK = 350;

	public GradualReset() {
		super("Gradual");
	}

	@Override
	public void reset(Mine paramMine, Map<CompMaterial, Double> blocks) {

	}
}