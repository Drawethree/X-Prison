package me.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TokensRemoveCommand extends TokensCommand {

    public TokensRemoveCommand(UltraPrisonTokens plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {

        if(args.size() == 2) {
            try {
                long amount = Long.parseLong(args.get(1));
                OfflinePlayer target = Players.getOfflineNullable(args.get(0));
                plugin.getTokensManager().removeTokens(target, amount, sender);
                return true;
            } catch (NumberFormatException e) {
                PlayerUtils.sendMessage(sender, plugin.getMessage("not_a_number").replace("%input%", String.valueOf(args.get(0))));
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(UltraPrisonTokens.TOKENS_ADMIN_PERM);
    }
}
