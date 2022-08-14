package dev.drawethree.ultraprisoncore.enchants.command;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import dev.drawethree.ultraprisoncore.utils.inventory.InventoryUtils;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantMenuCommand {

	private final UltraPrisonEnchants plugin;

	public EnchantMenuCommand(UltraPrisonEnchants plugin) {

		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					ItemStack pickAxe = c.sender().getItemInHand();

					if (!validatePickaxe(pickAxe)) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("no_pickaxe_found"));
						return;
					}

					openEnchantMenu(pickAxe, c.sender());
				}).registerAndBind(this.plugin.getCore(), "enchantmenu", "enchmenu");
	}

	private void openEnchantMenu(ItemStack pickAxe, Player player) {
		int pickaxeSlot = InventoryUtils.getInventorySlot(player, pickAxe);
		this.plugin.getCore().debug("Pickaxe slot is: " + pickaxeSlot, this.plugin);
		new EnchantGUI(this.plugin, player, pickAxe, pickaxeSlot).open();
	}

	private boolean validatePickaxe(ItemStack pickAxe) {
		return pickAxe != null && this.plugin.getCore().isPickaxeSupported(pickAxe.getType());
	}
}
