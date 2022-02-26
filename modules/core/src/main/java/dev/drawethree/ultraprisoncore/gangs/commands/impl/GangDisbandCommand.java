package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.gui.DisbandGangGUI;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangDisbandCommand extends GangCommand {

	public GangDisbandCommand(UltraPrisonGangs plugin) {
		super(plugin, "disband", "dis");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang disband [gang]";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender instanceof Player && args.size() == 0) {

			Player player = (Player) sender;
			Optional<Gang> gangOptional = this.plugin.getGangsManager().getPlayerGang(player);

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(player, this.plugin.getMessage("not-in-gang"));
				return false;
			}

			Gang gang = gangOptional.get();

			if (!gang.isOwner(player)) {
				PlayerUtils.sendMessage(player, this.plugin.getMessage("gang-not-owner"));
				return false;
			}

			new DisbandGangGUI(this.plugin, player).open();
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
