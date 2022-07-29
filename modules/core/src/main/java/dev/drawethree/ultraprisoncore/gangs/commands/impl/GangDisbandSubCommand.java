package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.commands.GangSubCommand;
import dev.drawethree.ultraprisoncore.gangs.gui.panel.DisbandGangGUI;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GangDisbandSubCommand extends GangSubCommand {

	public GangDisbandSubCommand(GangCommand command) {
		super(command, "disband", "dis");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang disband";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player && args.size() == 0) {

			Player player = (Player) sender;
			Optional<Gang> gangOptional = this.command.getPlugin().getGangsManager().getPlayerGang(player);

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(player, this.command.getPlugin().getConfig().getMessage("not-in-gang"));
				return false;
			}

			Gang gang = gangOptional.get();

			if (!gang.isOwner(player)) {
				PlayerUtils.sendMessage(player, this.command.getPlugin().getConfig().getMessage("gang-not-owner"));
				return false;
			}

			new DisbandGangGUI(this.command.getPlugin(), player, gang).open();
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		return new ArrayList<>();
	}
}
