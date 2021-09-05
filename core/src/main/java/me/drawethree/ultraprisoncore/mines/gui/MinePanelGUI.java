package me.drawethree.ultraprisoncore.mines.gui;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

public class MinePanelGUI extends Gui {

	private Mine mine;

	public MinePanelGUI(Mine mine, Player player) {
		super(player, 5, mine.getName());
		this.mine = mine;

	}

	@Override
	public void redraw() {

	}
}
