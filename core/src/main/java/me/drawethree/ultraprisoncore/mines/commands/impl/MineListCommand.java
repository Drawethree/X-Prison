package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
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
			sender.sendMessage("All mines:");
			for (Mine mine : this.plugin.getManager().getMines()) {
				sender.sendMessage(mine.getName());
			}
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "/mines list - Display all mines";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
