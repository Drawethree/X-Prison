package me.drawethree.wildprisontokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TokensSetCommand extends TokensCommand {
    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if(args.size() == 2) {
            try {
                long amount = Long.parseLong(args.get(0));
                OfflinePlayer target = Players.getOfflineNullable(args.get(1));
                WildPrisonTokens.getInstance().getTokensManager().setTokens(target, amount, sender);
                return true;
            } catch (Exception e) {
                sender.sendMessage(WildPrisonTokens.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }
}
