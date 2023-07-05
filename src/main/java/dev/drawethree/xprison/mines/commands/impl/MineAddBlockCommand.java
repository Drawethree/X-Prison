package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MineAddBlockCommand extends MineCommand {

	public MineAddBlockCommand(XPrisonMines plugin) {
		super(plugin, "addblock", "blockadd");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.size() != 1) {
			return false;
		}

		Mine mine = this.plugin.getManager().getMineByName(args.get(0));

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", args.get(0)));
			return true;
		}

		ItemStack inHand = ((Player) sender).getItemInHand();
		if (inHand == null || inHand.getType() == Material.AIR) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_no_item_in_hand"));
			return true;
		}

		CompMaterial material = CompMaterial.fromItem(inHand);
		mine.getBlockPalette().addToPalette(material, 0.0);
		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_block_added").replace("%block%", material.name()).replace("%mine%", mine.getName()));
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines addblock <mine> - Adds a block in your hand to the specified mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
