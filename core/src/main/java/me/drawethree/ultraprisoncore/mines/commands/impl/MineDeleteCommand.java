package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;

public class MineDeleteCommand extends MineCommand {

	public MineDeleteCommand(UltraPrisonMines plugin) {
		super(plugin, "delete", "remove");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() != 1) {
			return false;
		}

		return this.plugin.getManager().deleteMine(sender, args.get(0));
	}

	@Override
	public String getUsage() {
		return "/mines delete <name> - Delete a mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
