package me.drawethree.wildprisontokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokensPayCommand extends TokensCommand {
    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 2 && sender instanceof Player) {
            Player p = (Player) sender;
            try {
                long amount = Long.parseLong(args.get(0));
                OfflinePlayer target = Players.getOfflineNullable(args.get(1));

                if(!target.isOnline()) {
                    sender.sendMessage(WildPrisonTokens.getMessage("player_not_online").replace("%player%", target.getName()));
                    return true;
                }

                WildPrisonTokens.getInstance().getTokensManager().payTokens(p, amount, target);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(WildPrisonTokens.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }
}
