package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

public class MineHelpCommand extends MineCommand {

	public MineHelpCommand(UltraPrisonMines plugin) {
		super(plugin, "help", "?");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM)) {
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e&lMINES ADMIN HELP MENU ");
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e/mines create [mine] - Creates a new mine");
			PlayerUtils.sendMessage(sender, "&e/mines delete [mine] - Deletes a mine");
			PlayerUtils.sendMessage(sender, "&e/mines rename [mine] [new_name] - Renames a mine");
			PlayerUtils.sendMessage(sender, "&e/mines reset [mine/all] - Reset a mine");
			PlayerUtils.sendMessage(sender, "&e/mines panel [mine] - Opens a Mine Panel");
			PlayerUtils.sendMessage(sender, "&e/mines save [mine] - Force-save a mine");
			PlayerUtils.sendMessage(sender, "&e/mines addblock [mine] - Adds a block you hold in hand to a mine");
			PlayerUtils.sendMessage(sender, "&e/mines settp [mine] - Sets teleport location of a mine");
			PlayerUtils.sendMessage(sender, "&e/mines tp [mine] - Teleports to a mine");
			PlayerUtils.sendMessage(sender, "&e/mines list - Shows all Mines");
			PlayerUtils.sendMessage(sender, "&e/mines tool - Gives you a selection tool");
			PlayerUtils.sendMessage(sender, "&e/mines migrate <plugin> - Migrate mines from other plugins. [JetsPrisonMines,MineResetLite]");
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
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
