package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineSetTpCommand extends MineCommand {

	public MineSetTpCommand(UltraPrisonMines plugin) {
		super(plugin, "settp", "tpset");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {

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
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
