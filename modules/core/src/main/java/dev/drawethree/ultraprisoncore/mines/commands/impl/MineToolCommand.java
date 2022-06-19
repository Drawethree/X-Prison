package dev.drawethree.ultraprisoncore.mines.commands.impl;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MineToolCommand extends MineCommand {

	public MineToolCommand(UltraPrisonMines plugin) {
		super(plugin, "tool");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.size() != 0) {
			return false;
		}

		return this.plugin.getManager().giveTool((Player) sender);
	}

	@Override
	public String getUsage() {
		return "/mines tool - Gives you a selection tool";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
