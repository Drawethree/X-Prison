package dev.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.gui.MinePanelGUI;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinePanelCommand extends MineCommand {

	public MinePanelCommand(UltraPrisonMines plugin) {
		super(plugin, "panel", "editor");
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

		new MinePanelGUI(mine, (Player) sender).open();
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines panel <mine> - Opens a editor for a specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
