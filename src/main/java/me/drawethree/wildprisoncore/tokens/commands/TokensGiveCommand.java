package me.drawethree.wildprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TokensGiveCommand extends TokensCommand {

    public TokensGiveCommand(WildPrisonTokens plugin) {
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
                plugin.getTokensManager().giveTokens(target, amount, sender);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }
}
