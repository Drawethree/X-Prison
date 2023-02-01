package dev.drawethree.xprison.history.gui;

import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.history.XPrisonHistory;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerHistoryFilterGUI extends Gui {

	private static final MenuScheme LAYOUT_WHITE = new MenuScheme()
			.mask("011111110")
			.mask("100000001")
			.mask("100000001")
			.mask("011111110");

	private static final MenuScheme LAYOUT_RED = new MenuScheme()
			.mask("100000001")
			.mask("000000000")
			.mask("000000000")
			.mask("100000001");

	private final OfflinePlayer target;
	private final XPrisonHistory plugin;

	public PlayerHistoryFilterGUI(Player player, OfflinePlayer target, XPrisonHistory plugin) {
		super(player, 4, "History Filter");
		this.target = target;
		this.plugin = plugin;
	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {

			MenuPopulator populator = LAYOUT_WHITE.newPopulator(this);

			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(CompMaterial.WHITE_STAINED_GLASS_PANE.toItem()).name(" ").buildItem().build());
			}

			populator = LAYOUT_RED.newPopulator(this);

			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(CompMaterial.RED_STAINED_GLASS_PANE.toItem()).name(" ").buildItem().build());
			}

			for (XPrisonModule module : this.plugin.getCore().getModules()) {
				if (!module.isHistoryEnabled()) {
					continue;
				}
				this.addItem(ItemStackBuilder.of(CompMaterial.HOPPER.toItem()).name("&e" + module.getName()).lore("&7Show only history related to this module.").build(() -> {
					this.close();
					this.plugin.getHistoryManager().openPlayerHistoryGui(this.getPlayer(), this.target, historyLine -> historyLine.getModule().equalsIgnoreCase(module.getName()));
				}));
			}
			this.setItem(27, ItemStackBuilder.of(Material.ARROW).name("&c&lBack").lore("&7Click to go back.").build(() -> {
				this.close();
				new PlayerHistoryGUI(this.getPlayer(), this.target, this.plugin).open();
			}));

		}
	}
}
