package dev.drawethree.xprison.mines.model.mine.loader;

import dev.drawethree.xprison.mines.model.mine.MineImpl;

public interface MineLoader<T> {

	MineImpl load(T type);
}
