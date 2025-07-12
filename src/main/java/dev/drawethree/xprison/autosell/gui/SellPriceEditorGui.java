package dev.drawethree.xprison.autosell.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.manager.AutoSellManager;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;

public final class SellPriceEditorGui extends Gui {

	private final AutoSellManager manager;

	public SellPriceEditorGui(AutoSellManager manager, Player player) {
		super(player, 6, "Sell Prices");
		this.manager = manager;
	}

	@Override
	public void redraw() {
		this.clearItems();
		this.setActionItems();
	}

	private void setActionItems() {
		for (XMaterial material : this.manager.getSellingMaterials()) {
			this.addItemForMaterial(material);
		}
	}


	private void addItemForMaterial(XMaterial material) {
		double price = this.manager.getSellPriceForMaterial(material);

		this.addItem(ItemStackBuilder.of(material.parseItem()).name(material.name()).lore(" ", String.format("&7Sell Price: &2$&a%,.2f", price), " ", "&aLeft-Click &7to edit the price", "&aRight-Click &7to remove.").build(() -> {
			this.deleteSellPrice(material);
			this.redraw();
		}, () -> {
			openUpdateSellPriceGui(material);
		}));
	}

	private void openUpdateSellPriceGui(XMaterial material) {
		UpdateSellPriceGui gui = new UpdateSellPriceGui(this.getPlayer(), material, manager);
		gui.setFallbackGui(player -> this);
		gui.open();
	}

	private void deleteSellPrice(XMaterial material) {
		this.manager.removeSellPrice(material);
	}
}
