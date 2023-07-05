package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MineListCommand extends MineCommand {

	public MineListCommand(XPrisonMines plugin) {
		super(plugin, "list");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player) {
			this.plugin.getManager().openMinesListGUI((Player) sender);
		} else {
			PlayerUtils.sendMessage(sender, "All mines:");
			for (Mine mine : this.plugin.getManager().getMines()) {
				PlayerUtils.sendMessage(sender, mine.getName());
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines list - Display all mines";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
