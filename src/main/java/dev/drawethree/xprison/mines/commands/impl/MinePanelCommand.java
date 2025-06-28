package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.gui.MinePanelGUI;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MinePanelCommand extends MineCommand {

	public MinePanelCommand(XPrisonMines plugin) {
		super(plugin, "panel", "editor");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.size() != 1) {
			return false;
		}

		MineImpl mineImpl = this.plugin.getManager().getMineByName(args.get(0));

		if (mineImpl == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		new MinePanelGUI(mineImpl, (Player) sender).open();
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines panel <mine> - Opens a editor for a specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
