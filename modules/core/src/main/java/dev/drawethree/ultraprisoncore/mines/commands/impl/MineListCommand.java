package dev.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineListCommand extends MineCommand {

	public MineListCommand(UltraPrisonMines plugin) {
		super(plugin, "list");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender instanceof Player) {
			this.plugin.getManager().openMinesListGUI((Player) sender);
		} else {
			PlayerUtils.sendMessage(sender, "All mines:");
			for (Mine mine : this.plugin.getManager().getMines()) {
				PlayerUtils.sendMessage(sender, mine.getName());
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines list - Display all mines";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
