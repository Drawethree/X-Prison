package dev.drawethree.xprison.bombs.handler.impl;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.mines.model.Mine;
import dev.drawethree.xprison.bombs.handler.BlockHandler;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;

public final class BlockHandlerImpl implements BlockHandler {

	public BlockHandlerImpl() {
	}

	@Override
	public List<Block> handle(List<Block> blocks) {
		blocks.removeIf(block -> {
			MineImpl mine = (MineImpl) getMineByLocation(block.getLocation());
			if (mine != null) {
				mine.handleBlockBreak(Arrays.asList(block));
			}
			return mine == null;
		});
		return blocks;
	}

	private Mine getMineByLocation(Location location) {
		return XPrison.getInstance().getMines().getApi().getMineAtLocation(location);
	}
}
