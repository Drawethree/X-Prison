package dev.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gems.managers.CommandManager;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsPayCommand extends GemsCommand {

	private static final String COMMAND_NAME = "pay";
	private static final String[] COMMAND_ALIASES = {"send"};

	public GemsPayCommand(CommandManager manager) {
		super(manager, COMMAND_NAME,COMMAND_ALIASES);
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
					PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("player_not_online").replace("%player%", target.getName()));
					return true;
				}

				if (target.getUniqueId().equals(p.getUniqueId())) {
					PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("gems_cant_send_to_yourself"));
					return true;
				}

				this.commandManager.getPlugin().getGemsManager().payGems(p, amount, target);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, this.commandManager.getPlugin().getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(this.getRequiredPermission());
	}

	@Override
	public String getUsage() {
		return "/gems pay [player] [amount] - Send gems to a player.";
	}
}
