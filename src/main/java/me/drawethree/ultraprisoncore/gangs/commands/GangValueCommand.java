package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class GangValueCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang value <add/remove> <gang> <amount>";
    }

    public GangValueCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 3) {
            try {
                Optional<Gang> gang = this.plugin.getGangsManager().getGangWithName(args.get(1));

                if (!gang.isPresent()) {
                    gang = this.plugin.getGangsManager().getPlayerGang(Players.getOfflineNullable(args.get(1)));
                }

                int amount = Integer.parseInt(args.get(2));
                String operation = args.get(0);

                return this.plugin.getGangsManager().modifyValue(sender, gang, amount, operation);
            } catch (Exception e) {
                sender.sendMessage("§cInternal error.");
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(UltraPrisonGangs.GANGS_ADMIN_PERM);
    }
}
