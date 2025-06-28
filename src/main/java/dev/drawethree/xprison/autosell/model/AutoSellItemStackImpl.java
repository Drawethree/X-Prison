package dev.drawethree.xprison.autosell.model;

import dev.drawethree.xprison.api.autosell.model.AutoSellItemStack;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class AutoSellItemStackImpl implements AutoSellItemStack {

	private final ItemStack itemStack;

	public AutoSellItemStackImpl(ItemStack stack) {
		this.itemStack = stack;
	}

	public static AutoSellItemStackImpl of(ItemStack item) {
		return new AutoSellItemStackImpl(item);
	}
}
