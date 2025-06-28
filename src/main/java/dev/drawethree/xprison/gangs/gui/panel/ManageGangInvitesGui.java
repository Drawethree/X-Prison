package dev.drawethree.xprison.gangs.gui.panel;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.gangs.model.GangInvitationImpl;
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
	private final GangImpl gangImpl;

	public ManageGangInvitesGui(XPrisonGangs plugin, GangImpl gangImpl, Player player) {
		super(player, 6, "Pending Invites");
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
		this.gangImpl.getPendingInvitations().forEach(gangInvitation -> this.addItem(createInviteItem(gangInvitation)));
	}

	private Item createInviteItem(GangInvitationImpl invitation) {

		String statusColor = invitation.getInvitedPlayer().isOnline() ? "&a" : "&c";
		String status = invitation.getInvitedPlayer().isOnline() ? "Online" : "Offline";

		return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
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
					this.gangImpl.removeInvitation(invitation);
					redraw();
				});
	}
}
