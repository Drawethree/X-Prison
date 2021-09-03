package me.drawethree.ultraprisoncore.mines.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import org.bukkit.command.CommandSender;

public class MineEditorCommand extends MineCommand {

	public MineEditorCommand(UltraPrisonMines plugin) {
		super(plugin, "editor");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		return false;
	}

	@Override
	public String getUsage() {
		return "/mines editor <mine> - Opens a editor for a specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
