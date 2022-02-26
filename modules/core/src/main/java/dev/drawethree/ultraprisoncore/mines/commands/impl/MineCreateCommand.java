package dev.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineCreateCommand extends MineCommand {

	public MineCreateCommand(UltraPrisonMines plugin) {
		super(plugin, "create", "new");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() != 1) {
			return false;
		}
		if (!(sender instanceof Player)) {
			return false;
		}

		this.plugin.getManager().createMine((Player) sender, args.get(0));
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines create <name> - Creates a new mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
