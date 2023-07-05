package dev.drawethree.xprison.autosell.gui;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGui;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public final class AllSellRegionsGui {

	private AllSellRegionsGui() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}

	public static void createAndOpenTo(Player player) {

		PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();

		builder.title("Sell Regions");
		builder.lines(6);
		builder.previousPageSlot(45);
		builder.nextPageSlot(53);
		builder.nextPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&aNext Page").lore("&7Click to see next page.").build());
		builder.previousPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&aPrevious Page").lore("&7Click to see previous page.").build());

		Collection<SellRegion> regions = getSellRegions();
		PaginatedGui gui = builder.build(player, paginatedGui -> regions.stream().map(sellRegion -> buildItemForSellRegion(sellRegion, player)).collect(Collectors.toList()));
		gui.open();
	}

	private static Item buildItemForSellRegion(SellRegion sellRegion, Player player) {
		return ItemStackBuilder.of(Material.DIAMOND_PICKAXE)
				.name(sellRegion.getRegion().getId())
				.lore(" ", "&7Click to edit sell prices.", " ")
				.build(() -> new SellRegionGui(sellRegion, player).open());
	}

	private static Collection<SellRegion> getSellRegions() {
		return XPrisonAutoSell.getInstance().getManager().getAutoSellRegions();
	}
}
