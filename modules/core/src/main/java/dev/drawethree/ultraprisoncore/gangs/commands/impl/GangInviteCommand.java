package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangInviteCommand extends GangCommand {

	public GangInviteCommand(UltraPrisonGangs plugin) {
		super(plugin, "invite", "inv");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang invite [player]";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender instanceof Player && args.size() == 1) {
			Player p = (Player) sender;
			Player target = Players.getNullable(args.get(0));
			return this.plugin.getGangsManager().invitePlayer(p, target);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
