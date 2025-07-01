package dev.drawethree.xprison.utils.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ConfirmationGui extends Gui {

	private static final ItemStack YES_ITEM = ItemStackBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).name("&a&lYES").build();
	private static final ItemStack NO_ITEM = ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name("&c&cNO").build();

	public ConfirmationGui(Player player, String title) {
		super(player, 6, title);
	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {

			this.setItem(13, this.getInfoItem());
			for (int i = 19; i < 22; i++) {
				this.setItem(i, getItem(true));
				this.setItem(i + 9, getItem(true));
				this.setItem(i + 18, getItem(true));
			}
			for (int i = 23; i < 26; i++) {
				this.setItem(i, getItem(false));
				this.setItem(i + 9, getItem(false));
				this.setItem(i + 18, getItem(false));
			}
		}
	}


	public abstract void confirm(boolean confirm);


	private Item getItem(boolean action) {
		return ItemStackBuilder.of(action ? YES_ITEM : NO_ITEM).build(() -> {
			confirm(action);
		});
	}


	private Item getInfoItem() {
		return ItemStackBuilder.of(XMaterial.BOOK.parseItem()).name(this.getInitialTitle()).lore("&c&lWARNING!", "&7This action cannot be undone.").buildItem().build();
	}
}
