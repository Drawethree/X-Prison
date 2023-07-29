package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MineSetTpCommand extends MineCommand {

	public MineSetTpCommand(XPrisonMines plugin) {
		super(plugin, "settp", "tpset");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.size() != 1) {
			return false;
		}

		Mine mine = this.plugin.getManager().getMineByName(args.get(0));

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		return this.plugin.getManager().setTeleportLocation((Player) sender, mine);
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines settp <mine> - Sets the teleport location of specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
