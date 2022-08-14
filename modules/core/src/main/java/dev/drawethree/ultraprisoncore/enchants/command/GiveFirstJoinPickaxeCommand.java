package dev.drawethree.ultraprisoncore.enchants.command;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class GiveFirstJoinPickaxeCommand {

	private final UltraPrisonEnchants plugin;

	public GiveFirstJoinPickaxeCommand(UltraPrisonEnchants plugin) {

		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), "&c/givefirstjoinpickaxe <player>");
						return;
					}

					Player target = c.arg(0).parseOrFail(Player.class);

					this.plugin.getEnchantsManager().giveFirstJoinPickaxe(target);
					PlayerUtils.sendMessage(c.sender(), "&aYou have given first join pickaxe to &e" + target.getName());
				}).registerAndBind(this.plugin.getCore(), "givefirstjoinpickaxe");
	}
}
