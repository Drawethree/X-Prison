package me.drawethree.ultraprisoncore.mines.model.mine.reset;

import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;

public class GradualReset extends ResetType {

	private final int DELAY = 1;

	private final int CHANGES_PER_TICK = 350;

	GradualReset() {
		super("Gradual");
	}

	@Override
	public void reset(Mine paramMine, BlockPalette blockPalette) {
		//TODO: implement
	}
}