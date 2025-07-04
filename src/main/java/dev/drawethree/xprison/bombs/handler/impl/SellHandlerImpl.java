package dev.drawethree.xprison.bombs.handler.impl;


import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.bombs.handler.SellHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public final class SellHandlerImpl implements SellHandler {

	@Override
	public void sell(Player player, List<Block> blocks) {
		this.getAutoSell().getApi().sellBlocks(player, blocks);
	}

	private XPrisonAutoSell getAutoSell() {
		return XPrison.getInstance().getAutoSell();
	}

}
