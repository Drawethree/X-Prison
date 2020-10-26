package me.drawethree.wildprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.api.enums.ReceiveCause;
import me.drawethree.wildprisoncore.gems.WildPrisonGems;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class GemsGiveCommand extends GemsCommand {

    public GemsGiveCommand(WildPrisonGems plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {

        if(!sender.isOp()) {
            return false;
        }

        if(args.size() == 2) {
            try {
                long amount = Long.parseLong(args.get(0));
                OfflinePlayer target = Players.getOfflineNullable(args.get(1));
                plugin.getGemsManager().giveGems(target, amount, sender, ReceiveCause.GIVE);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }
}
