package dev.drawethree.xprison.tokens.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.tokens.managers.CommandManager;
import dev.drawethree.xprison.tokens.utils.TokensConstants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TokensWithdrawCommand extends TokensCommand {

	private static final String COMMAND_NAME = "withdraw";

	public TokensWithdrawCommand(CommandManager commandManager) {
		super(commandManager, COMMAND_NAME);
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

				commandManager.getPlugin().getTokensManager().withdrawTokens(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, commandManager.getPlugin().getTokensConfig().getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		} else if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			try {
				long amount = Long.parseLong(args.get(0));
				int value = 1;
				if (0 >= amount) {
					return false;
				}
				commandManager.getPlugin().getTokensManager().withdrawTokens(p, amount, value);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, commandManager.getPlugin().getTokensConfig().getMessage("not_a_number").replace("%input%", args.get(0) + " or " + args.get(1)));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(TokensConstants.TOKENS_ADMIN_PERM) || sender.hasPermission(getRequiredPermission());
	}

	@Override
	public String getUsage() {
		return "/tokens withdraw [amount] [value] - Withdraw tokens to physical item.";
	}

	@Override
	public List<String> getTabComplete(List<String> args) {
		return new ArrayList<>();
	}
}
