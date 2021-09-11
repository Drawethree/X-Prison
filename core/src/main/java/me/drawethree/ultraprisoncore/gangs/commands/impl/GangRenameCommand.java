package me.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import me.drawethree.ultraprisoncore.gangs.enums.GangRenameResult;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangRenameCommand extends GangCommand {

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang rename";
	}

	public GangRenameCommand(UltraPrisonGangs plugin) {
		super(plugin, "rename", "");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			String newName = args.get(0);

			Optional<Gang> gangOptional = this.plugin.getGangsManager().getPlayerGang(p);

			if (!gangOptional.isPresent()) {
				p.sendMessage(this.plugin.getMessage("not-in-gang"));
				return false;
			}

			Gang gang = gangOptional.get();

			if (!gang.isOwner(p)) {
				p.sendMessage(this.plugin.getMessage("gang-not-owner"));
				return false;
			}

			return this.plugin.getGangsManager().renameGang(gang, newName, p) == GangRenameResult.SUCCESS;
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
