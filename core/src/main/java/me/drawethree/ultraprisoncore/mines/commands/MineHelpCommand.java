package me.drawethree.ultraprisoncore.mines.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import org.bukkit.command.CommandSender;

public class MineHelpCommand extends MineCommand {

	public MineHelpCommand(UltraPrisonMines plugin) {
		super(plugin, "help", "?");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		return false;
	}

	@Override
	public String getUsage() {
		return "/mines help - Shows usage";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
