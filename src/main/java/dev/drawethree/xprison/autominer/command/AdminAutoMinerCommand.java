package dev.drawethree.xprison.autominer.command;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.autominer.utils.AutoMinerConstants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AdminAutoMinerCommand {

	private static final String[] COMMAND_ALIASES = {"adminautominer", "aam"};

	private final XPrisonAutoMiner plugin;

	public AdminAutoMinerCommand(XPrisonAutoMiner plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPermission(AutoMinerConstants.ADMIN_PERMISSION)
				.tabHandler(this::createTabHandler)
				.handler(c -> {

					if (!validateArguments(c)) {
						return;
					}

					String action = c.rawArg(0);

					Player target = c.arg(1).parseOrFail(Player.class);
					long time = c.arg(2).parseOrFail(Long.class);

					TimeUnit timeUnit;
					try {
						timeUnit = TimeUnit.valueOf(Objects.requireNonNull(c.rawArg(3)).toUpperCase());
					} catch (IllegalArgumentException e) {
						PlayerUtils.sendMessage(c.sender(), "&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ","));
						return;
					}

					if ("remove".equalsIgnoreCase(action)) {
						time = -time;
					}

					this.plugin.getManager().modifyPlayerAutoMinerTime(c.sender(), target, time, timeUnit);
				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}

	private List<String> createTabHandler(CommandContext<CommandSender> context) {
		List<String> list = new ArrayList<>();

		if (context.args().size() == 1) {
			list = Arrays.asList("give", "remove");
		} else if (context.args().size() == 2) {
			list = Players.all().stream().map(Player::getName).collect(Collectors.toList());
		} else if (context.args().size() == 4) {
			list = Arrays.stream(TimeUnit.values()).map(TimeUnit::name).collect(Collectors.toList());
		}

		return list;
	}

	private boolean validateArguments(CommandContext<CommandSender> c) {
		return c.args().size() == 4 && ("give".equalsIgnoreCase(c.rawArg(0)) || "remove".equalsIgnoreCase(c.rawArg(0)));
	}
}
