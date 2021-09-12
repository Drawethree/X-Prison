package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.lucko.helper.text3.Text;
import org.bukkit.command.CommandSender;

public class MineHelpCommand extends MineCommand {

	public MineHelpCommand(UltraPrisonMines plugin) {
		super(plugin, "help", "?");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM)) {
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			sender.sendMessage(Text.colorize("&e&lMINES ADMIN HELP MENU "));
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			sender.sendMessage(Text.colorize("&e/mines create [mine] - Creates a new mine"));
			sender.sendMessage(Text.colorize("&e/mines delete [mine] - Deletes a mine"));
			sender.sendMessage(Text.colorize("&e/mines reset [mine] - Reset a mine"));
			sender.sendMessage(Text.colorize("&e/mines panel [mine] - Opens a Mine Panel"));
			sender.sendMessage(Text.colorize("&e/mines save [mine] - Force-save a mine"));
			sender.sendMessage(Text.colorize("&e/mines addblock [mine] - Adds a block you hold in hand to a mine"));
			sender.sendMessage(Text.colorize("&e/mines settp [mine] - Sets teleport location of a mine"));
			sender.sendMessage(Text.colorize("&e/mines tp [mine] - Teleports to a mine"));
			sender.sendMessage(Text.colorize("&e/mines list - Shows all Mines"));
			sender.sendMessage(Text.colorize("&e/mines tool - Gives you a selection tool"));
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines help - Shows usage";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
