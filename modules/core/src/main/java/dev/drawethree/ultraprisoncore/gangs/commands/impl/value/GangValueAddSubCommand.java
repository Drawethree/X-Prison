package dev.drawethree.ultraprisoncore.gangs.commands.impl.value;

import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.commands.GangSubCommand;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.utils.GangsConstants;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GangValueAddSubCommand extends GangSubCommand {
	public GangValueAddSubCommand(GangCommand command) {
		super(command, "add");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 2) {
			try {
				Optional<Gang> gang = this.command.getPlugin().getGangsManager().getGangWithName(args.get(0));

				if (!gang.isPresent()) {
					gang = this.command.getPlugin().getGangsManager().getPlayerGang(Players.getOfflineNullable(args.get(0)));
				}

				if (!gang.isPresent()) {
					PlayerUtils.sendMessage(sender, this.command.getPlugin().getConfig().getMessage("gang-not-exists"));
					return false;
				}

				int amount = Integer.parseInt(args.get(1));
				String operation = "add";

				return this.command.getPlugin().getGangsManager().modifyValue(sender, gang.get(), amount, operation);
			} catch (Exception e) {
				sender.sendMessage("§cInternal error.");
				return false;
			}
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang value add <gang/player> <value>";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(GangsConstants.GANGS_ADMIN_PERM);
	}

	@Override
	public List<String> getTabComplete() {
		return this.command.getPlugin().getGangsManager().getAllGangs().stream().map(Gang::getName).collect(Collectors.toList());
	}
}
