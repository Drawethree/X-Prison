package me.drawethree.wildprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokensWithdrawCommand extends TokensCommand {


    public TokensWithdrawCommand(WildPrisonTokens plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 2 && sender instanceof Player) {
            Player p = (Player) sender;
            try {
                long amount = Long.parseLong(args.get(0));
                int value = Integer.parseInt(args.get(1));
                if (0 >= amount || 0 >= value) {
                    return false;
                }
                plugin.getTokensManager().withdrawTokens(p, amount, value);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
            }
        } else if (args.size() == 1 && sender instanceof Player) {
            Player p = (Player) sender;
            try {
                long amount = Long.parseLong(args.get(0));
                int value = 1;
                if (0 >= amount) {
                    return false;
                }
                plugin.getTokensManager().withdrawTokens(p, amount, value);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
            }
        }
        return false;
    }
}
