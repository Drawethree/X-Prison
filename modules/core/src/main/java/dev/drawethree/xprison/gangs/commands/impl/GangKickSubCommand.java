package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GangKickSubCommand extends GangSubCommand {

	public GangKickSubCommand(GangCommand command) {
		super(command, "kick", "remove");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang kick <player>";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			Optional<Gang> gang = this.command.getPlugin().getGangsManager().getPlayerGang(p);

			if (!gang.isPresent()) {
				PlayerUtils.sendMessage(p, this.command.getPlugin().getConfig().getMessage("not-in-gang"));
				return false;
			}

			OfflinePlayer target = Players.getOfflineNullable(args.get(0));

			return this.command.getPlugin().getGangsManager().removeFromGang(p, gang.get(), target);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		return Players.all().stream().map(Player::getName).collect(Collectors.toList());
	}

}
