package me.drawethree.ultraprisoncore.mines.model.mine.reset;

import lombok.Getter;
import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;

public abstract class ResetType {

	public static final GradualReset GRADUAL = new GradualReset();
	public static final InstantReset INSTANT = new InstantReset();

	@Getter
	private final String name;

	ResetType(String paramString) {
		this.name = paramString;
	}

	public abstract void reset(Mine paramMine, BlockPalette blockPalette);

	public static ResetType of(String name) {
		switch (name.toLowerCase()) {
			case "gradual":
				return GRADUAL;
			case "instant":
				return INSTANT;
		}
		return null;
	}
}