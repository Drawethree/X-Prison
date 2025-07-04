package dev.drawethree.xprison.bombs.commands.subcommand.impl;

import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.commands.BombsCommand;
import dev.drawethree.xprison.bombs.commands.subcommand.BombsSubCommand;
import dev.drawethree.xprison.utils.text.TextUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GiveSubCommand extends BombsSubCommand {

	private static final String COMMAND_NAME = "give";

	public GiveSubCommand(BombsCommand parent) {
		super(parent, COMMAND_NAME);
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (args.size() != 3) {
			return false;
		}

		Player target = Bukkit.getPlayer(args.get(0));

		if (target == null) {
			sender.sendMessage(this.command.getPlugin().getConfig().getMessage("player_offline").replace("%player%", args.get(0)));
			return true;
		}

		Optional<Bomb> bombOptional = this.command.getPlugin().getBombsRepository().getBombByName(args.get(1));

		if (!bombOptional.isPresent()) {
			sender.sendMessage(this.command.getPlugin().getConfig().getMessage("bomb_invalid_type").replace("%type%", args.get(1)));
			return true;
		}

		int amount;
		try {
			amount = Integer.parseInt(args.get(2));
		} catch (NumberFormatException e) {
			sender.sendMessage(TextUtils.applyColor("&cInvalid amount!"));
			return true;
		}

		Bomb bomb = bombOptional.get();

		this.command.getPlugin().getBombsService().giveBomb(bomb, amount, target);

		sender.sendMessage(this.command.getPlugin().getConfig().getMessage("bomb_given").
				replace("%player%", target.getName()).
				replace("%amount%", String.valueOf(amount)).
				replace("%item%", bomb.getItem().getItemMeta().getDisplayName()));
		return true;
	}

	@Override
	public String getUsage() {
		return TextUtils.applyColor("&c/bombs give [player] [type] [amount]");
	}

	@Override
	public List<String> getTabComplete() {
		return Players.all().stream().map(Player::getName).collect(Collectors.toList());
	}
}
