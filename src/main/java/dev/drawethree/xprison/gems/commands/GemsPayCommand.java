package dev.drawethree.xprison.gems.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.gems.managers.CommandManager;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class GemsPayCommand extends GemsCommand {

	private static final String COMMAND_NAME = "pay";
	private static final String[] COMMAND_ALIASES = {"send"};

	public GemsPayCommand(CommandManager manager) {
		super(manager, COMMAND_NAME, COMMAND_ALIASES);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 2 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				OfflinePlayer target = Players.getOfflineNullable(args.get(0));

				if (!target.isOnline()) {
					PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("player_not_online").replace("%player%", target.getName()));
					return true;
				}

				long amount = Long.parseLong(args.get(1).replace(",", ""));

				if (0 >= amount) {
					return false;
				}

				if (target.getUniqueId().equals(p.getUniqueId())) {
					PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("gems_cant_send_to_yourself"));
					return true;
				}

				this.commandManager.getPlugin().getGemsManager().payGems(p, amount, target);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("not_a_number").replace("%input%", String.valueOf(args.get(1))));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonGems.GEMS_ADMIN_PERM) || sender.hasPermission(this.getRequiredPermission());
	}

	@Override
	public String getUsage() {
		return "/gems pay [player] [amount] - Send gems to a player.";
	}

	@Override
	public List<String> getTabComplete(List<String> args) {
		List<String> list = new ArrayList<>();

		if (args.size() == 1) {
			list = Players.all().stream().map(Player::getName).collect(Collectors.toList());
		}

		return list;
	}
}
