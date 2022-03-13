package dev.drawethree.ultraprisoncore.tokens.managers;

import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.tokens.commands.*;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens.TOKENS_ADMIN_PERM;

public class CommandManager {

	@Getter
	private final UltraPrisonTokens plugin;
	private final Set<TokensCommand> commands;
	private CooldownMap<CommandSender> tokensCommandCooldownMap;

	public CommandManager(UltraPrisonTokens plugin) {
		this.plugin = plugin;
		this.commands = new HashSet<>();
		this.tokensCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getCommandCooldown(), TimeUnit.SECONDS));
	}

	private boolean checkCommandCooldown(CommandSender sender) {
		if (sender.hasPermission(TOKENS_ADMIN_PERM)) {
			return true;
		}
		if (!tokensCommandCooldownMap.test(sender)) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("cooldown").replace("%time%", String.format("%,d", this.tokensCommandCooldownMap.remainingTime(sender, TimeUnit.SECONDS))));
			return false;
		}
		return true;
	}

	public void registerCommands() {
		this.commands.clear();

		this.registerCommand(new TokensGiveCommand(this));
		this.registerCommand(new TokensGiveCommand(this));
		this.registerCommand(new TokensPayCommand(this));
		this.registerCommand(new TokensRemoveCommand(this));
		this.registerCommand(new TokensSetCommand(this));
		this.registerCommand(new TokensWithdrawCommand(this));
		this.registerCommand(new TokensHelpCommand(this));

		// /tokens, /token
		Commands.create()
				.tabHandler(c -> this.commands.stream().map(TokensCommand::getName).collect(Collectors.toList()))
				.handler(c -> {
					if (c.args().size() == 0 && c.sender() instanceof Player) {
						this.plugin.getTokensManager().sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(), true);
						return;
					}
					TokensCommand subCommand = this.getCommand(c.rawArg(0));
					if (subCommand != null) {
						if (subCommand.canExecute(c.sender())) {
							subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
						} else {
							PlayerUtils.sendMessage(c.sender(), this.plugin.getMessage("no_permission"));
						}
					} else {
						if (!checkCommandCooldown(c.sender())) {
							return;
						}
						OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
						this.plugin.getTokensManager().sendInfoMessage(c.sender(), target, true);
					}
				}).registerAndBind(this.plugin.getCore(), "tokens", "token");

		// /tokenmessage
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					this.plugin.getTokensManager().toggleTokenMessage(c.sender());
				}).registerAndBind(this.plugin.getCore(), "tokenmessage");

		// /blockstop, / blocktop
		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getTokensManager().sendBlocksTop(c.sender());
					}
				})
				.registerAndBind(this.plugin.getCore(), "blockstop", "blocktop");

		// /blockstopweekly, /blockstopw
		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getTokensManager().sendBlocksTopWeekly(c.sender());
					}
				})
				.registerAndBind(this.plugin.getCore(), "blockstopweekly", "blockstopw");

		// /blockstopweeklyreset
		Commands.create()
				.assertPermission(TOKENS_ADMIN_PERM, this.plugin.getMessage("no_permission"))
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getTokensManager().resetBlocksTopWeekly(c.sender());
					}
				})
				.registerAndBind(this.plugin.getCore(), "blockstopweeklyreset");

		// /tokenstop, /tokentop
		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getTokensManager().sendTokensTop(c.sender());
					}
				})
				.registerAndBind(this.plugin.getCore(), "tokenstop", "tokentop");

		// /blocks
		Commands.create()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getTokensManager().sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(), false);
					} else if (c.args().size() == 1) {
						if (!checkCommandCooldown(c.sender())) {
							return;
						}
						OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
						this.plugin.getTokensManager().sendInfoMessage(c.sender(), target, false);
					}
				})
				.registerAndBind(this.plugin.getCore(), "blocks");

		// /blocksadmin, /blocksa
		Commands.create()
				.tabHandler(c -> Arrays.asList("add","remove","set"))
				.assertPermission(TOKENS_ADMIN_PERM, this.plugin.getMessage("no_permission"))
				.handler(c -> {
					if (c.args().size() == 3) {

						OfflinePlayer target = c.arg(1).parseOrFail(OfflinePlayer.class);
						long amount = c.arg(2).parseOrFail(Long.class);

						switch (c.rawArg(0).toLowerCase()) {
							case "add":
								this.plugin.getTokensManager().addBlocksBroken(c.sender(), target, amount);
								break;
							case "remove":
								this.plugin.getTokensManager().removeBlocksBroken(c.sender(), target, amount);
								break;
							case "set":
								this.plugin.getTokensManager().setBlocksBroken(c.sender(), target, amount);
								break;
							default:
								PlayerUtils.sendMessage(c.sender(), "&c/blocksadmin <add/set/remove> <player> <amount>");
								break;
						}
					} else {
						PlayerUtils.sendMessage(c.sender(), "&c/blocksadmin <add/set/remove> <player> <amount>");
					}
				})
				.registerAndBind(this.plugin.getCore(), "blocksadmin", "blocksa");
	}

	private void registerCommand(TokensCommand command) {
		this.commands.add(command);
	}

	private TokensCommand getCommand(String arg) {
		for (TokensCommand command : this.commands) {

			if (command.getName().equalsIgnoreCase(arg)) {
				return command;
			}

			if (command.getAliases() == null) {
				continue;
			}

			for (String alias : command.getAliases()) {
				if (alias.equalsIgnoreCase(arg)) {
					return command;
				}
			}
		}
		return null;
	}

	public Set<TokensCommand> getAll() {
		return new HashSet<>(this.commands);
	}

	public void reload() {
		Map<CommandSender, Cooldown> cooldownMap = this.tokensCommandCooldownMap.getAll();
		this.tokensCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getCommandCooldown(), TimeUnit.SECONDS));
		cooldownMap.forEach((commandSender, cooldown) -> this.tokensCommandCooldownMap.put(commandSender, cooldown));
	}
}
