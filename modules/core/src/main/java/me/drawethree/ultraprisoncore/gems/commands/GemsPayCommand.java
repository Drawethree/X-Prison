package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsPayCommand extends GemsCommand {

	public GemsPayCommand(UltraPrisonGems plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 2 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(1).replace(",", ""));

				if (0 >= amount) {
					return false;
				}

				OfflinePlayer target = Players.getOfflineNullable(args.get(0));

				if (!target.isOnline()) {
					PlayerUtils.sendMessage(sender, plugin.getMessage("player_not_online").replace("%player%", target.getName()));
					return true;
				}

				if (target.getUniqueId().equals(p.getUniqueId())) {
					PlayerUtils.sendMessage(sender, plugin.getMessage("gems_cant_send_to_yourself"));
					return true;
				}

				plugin.getGemsManager().payGems(p, amount, target);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
