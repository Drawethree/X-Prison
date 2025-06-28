package dev.drawethree.xprison.autosell.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegionImpl;
import dev.drawethree.xprison.autosell.utils.SellPriceComparator;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class SellRegionGui extends Gui {

	private final SellRegionImpl sellRegionImpl;

	public SellRegionGui(SellRegionImpl sellRegionImpl, Player player) {
		super(player, 6, sellRegionImpl.getRegion().getId() + " Prices");
		this.sellRegionImpl = sellRegionImpl;
	}

	@Override
	public void redraw() {
		this.clearItems();

		this.setActionItems();

		this.setBackItem();
	}

	private void setBackItem() {
		this.setItem(45, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to all regions").build(() -> {
			this.close();
			AllSellRegionsGui.createAndOpenTo(this.getPlayer());
		}));
	}

	private void setActionItems() {
		for (XMaterial material : this.sellRegionImpl.getSellingMaterialsSorted(new SellPriceComparator(sellRegionImpl))) {
			this.addItemForMaterial(material);
		}
	}


	private void addItemForMaterial(XMaterial material) {
		double price = this.sellRegionImpl.getSellPriceForMaterial(material);

		this.addItem(ItemStackBuilder.of(material.parseItem()).name(material.name()).lore(" ", String.format("&7Sell Price: &2$&a%,.2f", price), " ", "&aLeft-Click &7to edit the price", "&aRight-Click &7to remove.").build(() -> {
			this.deleteSellPrice(material);
			this.redraw();
		}, () -> {
			new UpdateSellPriceGui(this.getPlayer(), sellRegionImpl, material).open();
		}));
	}

	private void deleteSellPrice(XMaterial material) {
		this.sellRegionImpl.removeSellPrice(material);
		XPrisonAutoSell.getInstance().getAutoSellConfig().saveSellRegion(sellRegionImpl);
	}
}
