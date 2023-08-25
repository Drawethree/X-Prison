package dev.drawethree.xprison.mines.model.mine.loader;

import dev.drawethree.xprison.mines.model.mine.Mine;

public interface MineLoader<T> {

	Mine load(T type);
}
