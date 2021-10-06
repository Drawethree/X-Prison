package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.text3.Text;
import org.bukkit.command.CommandSender;

public class MineHelpCommand extends MineCommand {

	public MineHelpCommand(UltraPrisonMines plugin) {
		super(plugin, "help", "?");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM)) {
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&lMINES ADMIN HELP MENU "));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines create [mine] - Creates a new mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines delete [mine] - Deletes a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines reset [mine] - Reset a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines panel [mine] - Opens a Mine Panel"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines save [mine] - Force-save a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines addblock [mine] - Adds a block you hold in hand to a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines settp [mine] - Sets teleport location of a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines tp [mine] - Teleports to a mine"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines list - Shows all Mines"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines tool - Gives you a selection tool"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/mines migrate <plugin> - Migrate mines from other plugins. [JetsPrisonMines,MineResetLite]"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
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
