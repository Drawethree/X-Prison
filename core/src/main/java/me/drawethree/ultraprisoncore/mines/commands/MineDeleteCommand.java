package me.drawethree.ultraprisoncore.mines.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import org.bukkit.command.CommandSender;

public class MineDeleteCommand extends MineCommand {

	public MineDeleteCommand(UltraPrisonMines plugin) {
		super(plugin, "delete", "remove");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		return false;
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
