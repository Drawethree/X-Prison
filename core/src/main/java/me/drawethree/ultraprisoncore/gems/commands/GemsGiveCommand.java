package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class GemsGiveCommand extends GemsCommand {

    public GemsGiveCommand(UltraPrisonGems plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {

        if(args.size() == 2) {
            try {
                long amount = Long.parseLong(args.get(1));
                OfflinePlayer target = Players.getOfflineNullable(args.get(0));
                plugin.getGemsManager().giveGems(target, amount, sender, ReceiveCause.GIVE);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(UltraPrisonGems.GEMS_ADMIN_PERM);
    }
}
