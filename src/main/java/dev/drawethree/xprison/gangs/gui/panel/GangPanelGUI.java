package dev.drawethree.xprison.gangs.gui.panel;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.Services;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.signprompt.SignPromptFactory;
import org.bukkit.entity.Player;

import java.util.Arrays;

public final class GangPanelGUI extends Gui {

	private static final MenuScheme LAYOUT = new MenuScheme()
			.mask("111111111")
			.mask("100000001")
			.mask("111111111");

	private static final MenuScheme BUTTONS = new MenuScheme()
			.mask("000000000")
			.mask("011111110")
			.mask("000000000");


	private final XPrisonGangs plugin;
	private final GangImpl gangImpl;

	public GangPanelGUI(XPrisonGangs plugin, GangImpl gangImpl, Player player) {
		super(player, 3, "Gang Panel");
		this.plugin = plugin;
		this.gangImpl = gangImpl;
	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {
			populateLayout();
			populateButtons();
		}
	}

	private void populateLayout() {
		MenuPopulator populator = LAYOUT.newPopulator(this);
		while (populator.hasSpace()) {
			populator.accept(ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name("&a").buildItem().build());
		}
	}

	private void populateButtons() {
		MenuPopulator populator = BUTTONS.newPopulator(this);

		populator.acceptIfSpace(createGangInfoItem());

		if (gangImpl.canRenameGang(getPlayer())) {
			populator.acceptIfSpace(createGangRenameItem());
		}
		if (gangImpl.canManageMembers(getPlayer())) {
			populator.acceptIfSpace(createManageMembersItem());
		}
		if (gangImpl.canManageInvites(getPlayer())) {
			populator.acceptIfSpace(createManageInvitesItem());
		}
		if (gangImpl.canDisband(getPlayer())) {
			populator.acceptIfSpace(createDisbandGangItem());
		}

	}

	private Item createManageInvitesItem() {
		return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem()).name("&eManage Invites").lore("&7Click to manage pending invites.").build(this::openManageInvitesGui);
	}

	private Item createDisbandGangItem() {
		return ItemStackBuilder.of(XMaterial.BARRIER.parseItem()).name("&cDisband Gang").lore("&7Click to disband your gang.").build(this::openDisbandGangGui);
	}

	private void openDisbandGangGui() {
		close();
		new DisbandGangGUI(this.plugin, this.getPlayer(), this.gangImpl).open();
	}

	private Item createManageMembersItem() {
		return ItemStackBuilder.of(XMaterial.PLAYER_HEAD.parseItem()).name("&eManage Members").lore("&7Click to manage your gang members.").build(this::openManageMembersGui);
	}

	private void openManageMembersGui() {
		close();
		new ManageGangMembersGui(this.plugin, this.gangImpl, this.getPlayer()).open();
	}

	private void openManageInvitesGui() {
		close();
		new ManageGangInvitesGui(this.plugin, this.gangImpl, this.getPlayer()).open();
	}

	private Item createGangRenameItem() {
		return ItemStackBuilder.of(XMaterial.OAK_SIGN.parseItem()).name("&eRename Gang").lore("&7Click to rename your gang.").build(() -> {
			SignPromptFactory factory = Services.load(SignPromptFactory.class);
			factory.openPrompt(this.getPlayer(), Arrays.asList("", "§e^ ^ ^", "§7Enter gang name", ""), responseHandler -> {
				if (responseHandler.get(0).isEmpty()) {
					return SignPromptFactory.Response.ACCEPTED;
				}
				this.plugin.getGangsManager().renameGang(this.gangImpl, responseHandler.get(0), this.getPlayer());
				return SignPromptFactory.Response.ACCEPTED;
			});
		});
	}

	private Item createGangInfoItem() {
		int gangTopPosition = getGangTopPosition();

		return ItemStackBuilder.of(XMaterial.BOOK.parseItem()).name("&eGang Info").lore(
				" ",
				String.format("&8» &e%s &7Gang", this.gangImpl.getName()),
				String.format("&8» &7Owner: &e%s", this.gangImpl.getOwnerOffline().getName()),
				String.format("&8» &7Members: &e%,d", this.gangImpl.getMembersOffline().size()),
				String.format("&8» &7Value: &e%,d", this.gangImpl.getValue()),
				String.format("&8» &7Top Placement: &e%s", gangTopPosition == -1 ? "Please Wait" : String.format("#%,d", gangTopPosition)),
				" "
		).buildItem().build();
	}

	private int getGangTopPosition() {
		return this.plugin.getGangsManager().getGangTopPosition(this.gangImpl);
	}
}
