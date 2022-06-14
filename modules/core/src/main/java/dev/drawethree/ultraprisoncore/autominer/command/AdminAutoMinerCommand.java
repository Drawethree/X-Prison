package dev.drawethree.ultraprisoncore.autominer.command;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.autominer.utils.AutoMinerConstants;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AdminAutoMinerCommand {

	private static final String[] COMMAND_ALIASES = {"adminautominer", "aam"};

	private final UltraPrisonAutoMiner plugin;

	public AdminAutoMinerCommand(UltraPrisonAutoMiner plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPermission(AutoMinerConstants.ADMIN_PERMISSION)
				.handler(c -> {

					if (!validateArguments(c)) {
						return;
					}

					Player target = c.arg(1).parseOrFail(Player.class);
					long time = c.arg(2).parseOrFail(Long.class);

					TimeUnit timeUnit;
					try {
						timeUnit = TimeUnit.valueOf(Objects.requireNonNull(c.rawArg(3)).toUpperCase());
					} catch (IllegalArgumentException e) {
						PlayerUtils.sendMessage(c.sender(), "&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ","));
						return;
					}

					this.plugin.getManager().givePlayerAutoMinerTime(c.sender(), target, time, timeUnit);
				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}

	private boolean validateArguments(CommandContext<CommandSender> c) {
		return c.args().size() == 4 && "give".equalsIgnoreCase(c.rawArg(0));
	}
}
