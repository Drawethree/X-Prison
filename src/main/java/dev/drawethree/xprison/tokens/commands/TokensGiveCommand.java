package dev.drawethree.xprison.tokens.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.api.enums.ReceiveCause;
import dev.drawethree.xprison.tokens.managers.CommandManager;
import dev.drawethree.xprison.tokens.utils.TokensConstants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class TokensGiveCommand extends TokensCommand {

	private static final String COMMAND_NAME = "give";

	public TokensGiveCommand(CommandManager commandManager) {
		super(commandManager, COMMAND_NAME);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 2) {
			try {
				OfflinePlayer target = Players.getOfflineNullable(args.get(0));
				long amount = Long.parseLong(args.get(1));
				commandManager.getPlugin().getTokensManager().giveTokens(target, amount, sender, ReceiveCause.GIVE);
				return true;
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(sender, commandManager.getPlugin().getTokensConfig().getMessage("not_a_number").replace("%input%", String.valueOf(args.get(1))));
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
		return "/tokens give [player] [tokens] - Gives tokens to player.";
	}

	@Override
	public List<String> getTabComplete(List<String> args) {
		List<String> list = new ArrayList<>();

		if (args.size() == 1) {
			list = Players.all().stream().map(Player::getName).collect(Collectors.toList());
		}

		return list;
	}
}
