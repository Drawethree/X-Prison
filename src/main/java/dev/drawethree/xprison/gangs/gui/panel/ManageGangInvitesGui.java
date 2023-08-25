package dev.drawethree.xprison.gangs.gui.panel;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gangs.model.GangInvitation;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.SimpleDateFormat;

public final class ManageGangInvitesGui extends Gui {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

	private static final MenuScheme LAYOUT = new MenuScheme()
			.mask("111111111")
			.mask("100000001")
			.mask("100000001")
			.mask("100000001")
			.mask("100000001")
			.mask("111111111");

	private final XPrisonGangs plugin;
	private final Gang gang;

	public ManageGangInvitesGui(XPrisonGangs plugin, Gang gang, Player player) {
		super(player, 6, "Pending Invites");
		this.plugin = plugin;
		this.gang = gang;
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
			populator.accept(ItemStackBuilder.of(CompMaterial.BLACK_STAINED_GLASS_PANE.toItem()).name("&a").buildItem().build());
		}
	}

	private void populateButtons() {
		this.gang.getPendingInvites().forEach(gangInvitation -> this.addItem(createInviteItem(gangInvitation)));
	}

	private Item createInviteItem(GangInvitation invitation) {

		String statusColor = invitation.getInvitedPlayer().isOnline() ? "&a" : "&c";
		String status = invitation.getInvitedPlayer().isOnline() ? "Online" : "Offline";

		return ItemStackBuilder.of(CompMaterial.PLAYER_HEAD.toItem())
				.name(statusColor + invitation.getInvitedPlayer().getName())
				.lore(
						" ",
						"&8» &7Online Status: " + statusColor + status,
						"&8» &7Invited By: &e" + invitation.getInvitedBy().getName(),
						"&8» &7Invited At: &e" + DATE_FORMAT.format(invitation.getInviteDate()),
						" ",
						"&7Right-click to &cCancel"
				)
				.build(ClickType.RIGHT, () -> {
					this.gang.removeInvitation(invitation);
					redraw();
				});
	}
}
