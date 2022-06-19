package dev.drawethree.ultraprisoncore.mines.commands.impl;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MineResetCommand extends MineCommand {

	public MineResetCommand(UltraPrisonMines plugin) {
		super(plugin, "reset");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (args.size() != 1) {
			return false;
		}

		if ("all".equalsIgnoreCase(args.get(0))) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_all_reset_started"));
			this.plugin.getManager().resetAllMines();
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_all_reset_success"));
			return true;
		}

		Mine mine = this.plugin.getManager().getMineByName(args.get(0));

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		if (mine.isResetting()) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_already_reset").replace("%mine%", args.get(0)));
			return true;
		}

		this.plugin.getManager().resetMine(mine);
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
