package me.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangKickCommand extends GangCommand {

	public GangKickCommand(UltraPrisonGangs plugin) {
		super(plugin, "kick", "remove");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang kick <player>";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			Optional<Gang> gang = this.plugin.getGangsManager().getPlayerGang(p);
			OfflinePlayer target = Players.getOfflineNullable(args.get(0));
			return this.plugin.getGangsManager().removeFromGang(p, gang, target);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

}
