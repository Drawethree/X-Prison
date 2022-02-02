package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsWithdrawCommand extends GemsCommand {

	public GemsWithdrawCommand(UltraPrisonGems plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 2 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(0));
				int value = Integer.parseInt(args.get(1));
				if (0 >= amount || 0 >= value) {
					return false;
				}
				plugin.getGemsManager().withdrawGems(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		} else if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(0));
				int value = 1;
				if (0 >= amount) {
					return false;
				}
				plugin.getGemsManager().withdrawGems(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
