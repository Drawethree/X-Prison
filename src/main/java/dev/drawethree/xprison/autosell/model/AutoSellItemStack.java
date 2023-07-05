package dev.drawethree.xprison.autosell.model;

import org.bukkit.inventory.ItemStack;

public class AutoSellItemStack {

	private final ItemStack itemStack;

	public AutoSellItemStack(ItemStack stack) {
		this.itemStack = stack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public static AutoSellItemStack of(ItemStack item) {
		return new AutoSellItemStack(item);
	}
}
