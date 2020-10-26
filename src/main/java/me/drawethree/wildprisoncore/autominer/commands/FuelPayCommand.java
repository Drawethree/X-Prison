package me.drawethree.wildprisoncore.autominer.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FuelPayCommand extends FuelCommand {

	public FuelPayCommand(WildPrisonAutoMiner plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 2 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(0).replace(",", ""));

				if (0 >= amount) {
					return false;
				}

				OfflinePlayer target = Players.getOfflineNullable(args.get(1));

				if (!target.isOnline()) {
					sender.sendMessage(plugin.getMessage("player_not_online").replace("%player%", target.getName()));
					return true;
				}

				plugin.payFuel(p, amount, (Player) target);
				return true;
			} catch (NumberFormatException e) {
				sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
			}
		}
		return false;
	}
}
