package dev.drawethree.xprison.gems.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.gems.managers.CommandManager;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GemsWithdrawCommand extends GemsCommand {

	private static final String COMMAND_NAME = "withdraw";

	public GemsWithdrawCommand(CommandManager manager) {
		super(manager, COMMAND_NAME);
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
				this.commandManager.getPlugin().getGemsManager().withdrawGems(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		} else if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(0));
				int value = 1;
				if (0 >= amount) {
					return false;
				}
				this.commandManager.getPlugin().getGemsManager().withdrawGems(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonGems.GEMS_ADMIN_PERM) || sender.hasPermission(getRequiredPermission());
	}

	@Override
	public String getUsage() {
		return "/gems withdraw [amount] [value] - Withdraw gems to physical item.";
	}

	@Override
	public List<String> getTabComplete(List<String> args) {
		return new ArrayList<>();
	}
}
