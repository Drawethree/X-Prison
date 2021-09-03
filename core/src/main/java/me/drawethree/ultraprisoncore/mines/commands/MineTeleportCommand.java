package me.drawethree.ultraprisoncore.mines.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import org.bukkit.command.CommandSender;

public class MineTeleportCommand extends MineCommand {

	public MineTeleportCommand(UltraPrisonMines plugin) {
		super(plugin, "teleport", "tp");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		return false;
	}

	@Override
	public String getUsage() {
		return "/mines teleport <mine> - Teleports you to a specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
