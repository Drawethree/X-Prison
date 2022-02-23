package me.drawethree.ultraprisoncore.history.gui;

import me.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import me.drawethree.ultraprisoncore.history.gui.confirmation.PlayerClearHistoryConfirmationGUI;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.drawethree.ultraprisoncore.utils.item.ItemStackBuilder;
import me.drawethree.ultraprisoncore.utils.misc.SkullUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PlayerHistoryGUI extends Gui {

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

	private final OfflinePlayer target;
	private final UltraPrisonHistory plugin;

	public PlayerHistoryGUI(Player viewer, OfflinePlayer target, UltraPrisonHistory plugin) {
		super(viewer, 5, "History Management");
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
		}

		//Player
		this.setItem(13, ItemStackBuilder.of(SkullUtils.createPlayerHead(target, "&e&l" + target.getName(), Arrays.asList("&7Currently viewing history profile", "&7of this player."))).buildItem().build());

		//Filter
		this.setItem(20, ItemStackBuilder.of(CompMaterial.HOPPER.toItem()).name("&eFilter History").lore("&7Click to select a filter").build(() -> {
			this.close();
			new PlayerHistoryFilterGUI(this.getPlayer(), this.target, this.plugin).open();
		}));

		//Full History
		this.setItem(22, ItemStackBuilder.of(CompMaterial.BOOK.toItem()).name("&eFull History").lore("&7Click to show full history").build(() -> {
			this.close();
			this.plugin.getHistoryManager().openPlayerHistoryGui(this.getPlayer(), this.target, null);
		}));

		//Clear History
		this.setItem(24, ItemStackBuilder.of(CompMaterial.BARRIER.toItem()).name("&eClear History").lore("&7Click to clear history").build(() -> {
			this.close();
			new PlayerClearHistoryConfirmationGUI(this.getPlayer(), this.target, this.plugin).open();
		}));

		this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lClose").lore("&7Click to close the gui.").build(this::close));
	}
}
