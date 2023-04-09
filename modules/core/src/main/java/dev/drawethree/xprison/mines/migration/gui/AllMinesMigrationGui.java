package dev.drawethree.xprison.mines.migration.gui;

import dev.drawethree.xprison.mines.migration.model.MinesMigration;
import dev.drawethree.xprison.mines.migration.model.impl.MineResetLiteMigration;
import dev.drawethree.xprison.mines.migration.utils.MinesMigrationUtils;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AllMinesMigrationGui extends Gui {

	private static final MenuScheme LAYOUT_WHITE = new MenuScheme()
			.mask("011111110")
			.mask("110000011")
			.mask("100000001")
			.mask("110000011")
			.mask("011111110");

	private static final MenuScheme LAYOUT_RED = new MenuScheme()
			.mask("100000001")
			.mask("000000000")
			.mask("000000000")
			.mask("000000000")
			.mask("100000001");

	public AllMinesMigrationGui(Player player) {
		super(player, 5, "Mines Migration");
	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {
			populateLayout();
		}
		this.populateAvailableMigrations();
	}

	private void populateAvailableMigrations() {
		try {
			MinesMigration migration = new MineResetLiteMigration();
			this.addItem(createItemForMigration(migration));
		} catch (NoClassDefFoundError e) {

		}
	}

	private Item createItemForMigration(MinesMigration migration) {
		return ItemStackBuilder.of(Material.DIAMOND_PICKAXE).name("&a" + migration.getFromPlugin()).lore(" ", "&7Click to migrate mines from this plugin.", " ").build(() -> MinesMigrationUtils.openMinesMigrationGui(getPlayer(), migration));
	}

	private void populateLayout() {
		MenuPopulator populator = LAYOUT_WHITE.newPopulator(this);

		while (populator.hasSpace()) {
			populator.accept(ItemStackBuilder.of(CompMaterial.WHITE_STAINED_GLASS_PANE.toItem()).name(" ").buildItem().build());
		}

		populator = LAYOUT_RED.newPopulator(this);

		while (populator.hasSpace()) {
			populator.accept(ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name(" ").buildItem().build());
		}
	}
}
