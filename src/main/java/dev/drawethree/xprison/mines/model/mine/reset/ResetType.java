package dev.drawethree.xprison.mines.model.mine.reset;

import dev.drawethree.xprison.mines.model.mine.BlockPaletteImpl;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import lombok.Getter;

public abstract class ResetType {

	public static final GradualReset GRADUAL = new GradualReset();
	public static final InstantReset INSTANT = new InstantReset();

	@Getter
	private final String name;

	ResetType(String paramString) {
		this.name = paramString;
	}

	public static ResetType of(String name) {
		switch (name.toLowerCase()) {
			case "gradual":
				return GRADUAL;
			case "instant":
				return INSTANT;
		}
		return null;
	}

	public abstract void reset(MineImpl paramMineImpl, BlockPaletteImpl blockPaletteImpl);
}