package me.drawethree.wildprisontokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokensWithdrawCommand extends TokensCommand {
    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if(args.size() == 2 && sender instanceof Player) {
            Player p = (Player) sender;
            try {
                long amount = Long.parseLong(args.get(0));
                int value = Integer.parseInt(args.get(1));
                WildPrisonTokens.getInstance().getTokensManager().withdrawTokens(p, amount, value);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(WildPrisonTokens.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0) + " or " + args.get(1))));
            }
        }
        return false;
    }
}
