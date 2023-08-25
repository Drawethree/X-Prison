package dev.drawethree.xprison.mainmenu;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.history.gui.PlayerHistoryGUI;
import dev.drawethree.xprison.mainmenu.reload.ReloadSelectionGui;
import dev.drawethree.xprison.mainmenu.reset.ResetSelectionGui;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.stream.Collectors;

public class MainMenu extends Gui {

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

	private static final MenuScheme CONTENT = new MenuScheme()
			.mask("000000000")
			.mask("000111000")
			.mask("001111100")
			.mask("000111000")
			.mask("000000000");


	private final XPrison core;

	public MainMenu(XPrison core, Player player) {
		super(player, 5, "X-Prison - Main Menu");
		this.core = core;
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

		//Information
		this.setItem(13, ItemStackBuilder.of(SkullUtils.INFO_SKULL.clone()).name("&e&lInformation").lore("&7Author: &f" + StringUtils.join(this.core.getDescription().getAuthors(), ", "), "&7Version: &f" + this.core.getDescription().getVersion()).build(() -> {

		}));

		//Reload
		this.setItem(21, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&e&lReload Modules").lore("&7Click to reload specific module").build(() -> {
			if (!this.getPlayer().hasPermission("xprison.mainmenu.reload")) {
				return;
			}
			this.close();
			new ReloadSelectionGui(this.core, this.getPlayer()).open();
		}));

		//Debug
		this.setItem(22, ItemStackBuilder.of(this.core.isDebugMode() ? SkullUtils.CHECK_SKULL.clone() : SkullUtils.CROSS_SKULL.clone()).name("&e&lDebug Mode: " + (this.core.isDebugMode() ? "&2&lON" : "&c&lOFF")).lore("&7Click to toggle debug mode.").build(() -> {
			if (!this.getPlayer().hasPermission("xprison.mainmenu.debug")) {
				return;
			}
			this.core.setDebugMode(!this.core.isDebugMode());
			this.redraw();
		}));

		//Reset Data
		this.setItem(23, ItemStackBuilder.of(SkullUtils.DANGER_SKULL.clone()).name("&e&lReset Player Data").lore("&7Click to select which module data", "&7would you like to wipe.").build(() -> {
			if (!this.getPlayer().hasPermission("xprison.mainmenu.reset")) {
				return;
			}
			this.close();
			new ResetSelectionGui(this.core, this.getPlayer()).open();
		}));

		//Players History
		this.setItem(31, ItemStackBuilder.of(CompMaterial.BOOK.toItem()).name("&e&lPlayers History").lore("&7Click to see players history.").build(() -> {
			if (!this.getPlayer().hasPermission("xprison.mainmenu.history")) {
				return;
			}
			this.close();
			this.openHistorySelectorGui();
		}));

		this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lClose").lore("&7Click to close the gui.").build(this::close));
		this.setItem(44, ItemStackBuilder.of(SkullUtils.HELP_SKULL.clone()).name("&e&lNeed more help?").lore("&7Right-Click to see plugin's Wiki", "&7Left-Click to join Discord Support.")
				.build(() -> {
					this.close();
					PlayerUtils.sendMessage(this.getPlayer(), " ");
					PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Wiki");
					PlayerUtils.sendMessage(this.getPlayer(), "&7" + Constants.DISCORD_LINK);
					PlayerUtils.sendMessage(this.getPlayer(), " ");
				}, () -> {
					this.close();
					PlayerUtils.sendMessage(this.getPlayer(), " ");
					PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Discord");
					PlayerUtils.sendMessage(this.getPlayer(), "&7" + Constants.DISCORD_LINK);
					PlayerUtils.sendMessage(this.getPlayer(), " ");
				}));

	}

	private void openHistorySelectorGui() {
		PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();
		builder.lines(6);
		builder.title("Select a player");
		builder.build(this.getPlayer(), gui -> Players.all().stream().map(p -> ItemStackBuilder.of(SkullUtils.createPlayerHead(p, p.getName(), Collections.singletonList("&7Click to view history of this player."))).build(() -> {
			new PlayerHistoryGUI(this.getPlayer(), p, this.core.getHistory()).open();
		})).collect(Collectors.toList())).open();
	}
}
