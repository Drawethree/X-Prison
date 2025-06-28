package dev.drawethree.xprison.gangs.gui.panel;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class ManageGangMembersGui extends Gui {


	private static final MenuScheme LAYOUT = new MenuScheme()
			.mask("111111111")
			.mask("100000001")
			.mask("100000001")
			.mask("100000001")
			.mask("100000001")
			.mask("111111111");

	private final XPrisonGangs plugin;
	private final GangImpl gangImpl;

	public ManageGangMembersGui(XPrisonGangs plugin, GangImpl gangImpl, Player player) {
		super(player, 6, "Gang Members");
		this.plugin = plugin;
		this.gangImpl = gangImpl;
	}

	@Override
	public void redraw() {
		clearItems();
		populateLayout();
		populateButtons();
	}

	private void populateLayout() {
		MenuPopulator populator = LAYOUT.newPopulator(this);
		while (populator.hasSpace()) {
			populator.accept(ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name("&a").buildItem().build());
		}
	}

	private void populateButtons() {
		this.gangImpl.getMembersOffline().forEach(player -> this.addItem(createGangMemberItem(player)));
	}

	private Item createGangMemberItem(OfflinePlayer player) {

		String statusColor = player.isOnline() ? "&a" : "&c";
		String status = player.isOnline() ? "Online" : "Offline";

		return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
				.name(statusColor + player.getName())
				.lore(
						" ",
						"&8» &7Online Status: " + statusColor + status,
						"&8» &7Role: &e" + (gangImpl.isOwner(player) ? "Owner" : "Member"),
						" ",
						"&7Right-Click to &cKICK"
				)
				.build(ClickType.RIGHT, () -> {
					this.plugin.getGangsManager().kickPlayerFromGang(this.gangImpl, player);
					redraw();
				});
	}

}
