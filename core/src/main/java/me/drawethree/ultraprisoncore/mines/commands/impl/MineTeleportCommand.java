package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MineTeleportCommand extends MineCommand {

	public MineTeleportCommand(UltraPrisonMines plugin) {
		super(plugin, "teleport", "tp");
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
			sender.sendMessage(this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return false;
		}

		return this.plugin.getManager().teleportToMine((Player) sender, mine);
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines teleport <mine> - Teleports you to a specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
