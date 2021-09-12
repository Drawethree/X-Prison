package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import org.bukkit.command.CommandSender;

public class MineResetCommand extends MineCommand {

	public MineResetCommand(UltraPrisonMines plugin) {
		super(plugin, "reset");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {

		if (args.size() != 1) {
			return false;
		}

		Mine mine = this.plugin.getManager().getMineByName(args.get(0));

		if (mine == null) {
			sender.sendMessage(this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		if (mine.isResetting()) {
			sender.sendMessage(this.plugin.getMessage("mine_already_reset").replace("%mine%", args.get(0)));
			return true;
		}

		mine.resetMine();
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage:/mines reset <mine> - Resets mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
