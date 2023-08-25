package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MineSaveCommand extends MineCommand {


	public MineSaveCommand(XPrisonMines plugin) {
		super(plugin, "save");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (args.size() != 1) {
			return false;
		}

		Mine mine = this.plugin.getManager().getMineByName(args.get(0));

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		this.plugin.getManager().getMineSaver().save(mine);

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_saved").replace("%mine%", mine.getName()));
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines save <mine> - Saves a mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
