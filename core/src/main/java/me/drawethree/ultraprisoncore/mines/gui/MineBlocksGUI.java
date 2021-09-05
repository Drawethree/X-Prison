package me.drawethree.ultraprisoncore.mines.gui;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

public class MineBlocksGUI extends Gui {

	private Mine mine;

	public MineBlocksGUI(Mine mine, Player player) {
		super(player, 5, mine.getName());
		this.mine = mine;
	}

	@Override
	public void redraw() {

	}
}
