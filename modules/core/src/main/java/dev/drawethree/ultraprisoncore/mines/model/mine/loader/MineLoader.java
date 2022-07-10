package dev.drawethree.ultraprisoncore.mines.model.mine.loader;

import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;

public interface MineLoader<T> {

	Mine load(T type);
}
