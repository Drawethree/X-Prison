package dev.drawethree.xprison.autosell.model;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class AutoSellItemStackImpl {

	private final ItemStack itemStack;

	public AutoSellItemStackImpl(ItemStack stack) {
		this.itemStack = stack;
	}

	public static AutoSellItemStackImpl of(ItemStack item) {
		return new AutoSellItemStackImpl(item);
	}
}
