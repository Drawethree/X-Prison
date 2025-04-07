package dev.drawethree.xprison.enchants.command;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class GiveFirstJoinPickaxeCommand {

	private final XPrisonEnchants plugin;

	public GiveFirstJoinPickaxeCommand(XPrisonEnchants plugin) {

		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.handler(c -> {

					if (c.sender() instanceof Player) {
						if (!c.sender().isOp()){
							PlayerUtils.sendMessage(c.sender(), "&cComando desconocido.");
							return;
						}
					}

					if (c.args().isEmpty()) {
						PlayerUtils.sendMessage(c.sender(), "&c/givefirstjoinpickaxe <player>");
						return;
					}

					Player target = c.arg(0).parseOrFail(Player.class);

					this.plugin.getEnchantsManager().giveFirstJoinPickaxe(target);
					PlayerUtils.sendMessage(c.sender(), "&aYou have given first join pickaxe to &e" + target.getName());
				}).registerAndBind(this.plugin.getCore(), "givefirstjoinpickaxe");
	}
}
