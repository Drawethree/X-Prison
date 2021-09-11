package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;

public class MineResetCommand extends MineCommand {

	public MineResetCommand(UltraPrisonMines plugin) {
		super(plugin, "reset");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		return false;
	}

	@Override
	public String getUsage() {
		return "/mines reset <mine> - Resets mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
