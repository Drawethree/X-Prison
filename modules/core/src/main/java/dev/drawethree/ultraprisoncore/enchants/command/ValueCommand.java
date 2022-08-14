package dev.drawethree.ultraprisoncore.enchants.command;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.managers.CooldownManager;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ValueCommand {

	private static final String COMMAND_NAME = "value";

	private final UltraPrisonEnchants plugin;

	public ValueCommand(UltraPrisonEnchants plugin) {
		this.plugin = plugin;
	}


	public void register() {
		Commands.create()
				.assertPlayer()
				.assertPermission("ultraprison.value", this.plugin.getEnchantsConfig().getMessage("value_no_permission"))
				.handler(c -> {

					if (!checkCooldown(c.sender())) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("value_cooldown").replace("%time%", String.valueOf(this.getCooldownManager().getRemainingTime(c.sender()))));
						return;
					}

					ItemStack pickAxe = c.sender().getItemInHand();

					if (!validatePickaxe(pickAxe)) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("value_no_pickaxe"));
						return;
					}

					PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("value_value").replace("%player%", c.sender().getName()).replace("%tokens%", String.format("%,d", this.plugin.getEnchantsManager().getPickaxeValue(pickAxe))));
				}).registerAndBind(plugin.getCore(), COMMAND_NAME);
	}

	private boolean validatePickaxe(ItemStack pickAxe) {
		return pickAxe != null && this.plugin.getCore().isPickaxeSupported(pickAxe.getType());
	}

	private boolean checkCooldown(Player sender) {
		return (sender.isOp() || !getCooldownManager().hasValueCooldown(sender));
	}

	private CooldownManager getCooldownManager() {
		return this.plugin.getCooldownManager();
	}
}
