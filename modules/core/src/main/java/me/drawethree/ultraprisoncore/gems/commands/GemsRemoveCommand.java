package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.api.enums.LostCause;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class GemsRemoveCommand extends GemsCommand {

	public GemsRemoveCommand(UltraPrisonGems plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {

		if (args.size() == 2) {
			try {
				long amount = Long.parseLong(args.get(1));
				OfflinePlayer target = Players.getOfflineNullable(args.get(0));
				plugin.getGemsManager().removeGems(target, amount, sender, LostCause.ADMIN);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
			}
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonGems.GEMS_ADMIN_PERM);
	}
}
