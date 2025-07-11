package dev.drawethree.xprison.mainmenu.help;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GangsHelpGui extends Gui {

	protected static final MenuScheme LAYOUT_WHITE = new MenuScheme()
			.mask("011111110")
			.mask("110000011")
			.mask("100000001")
			.mask("110000011")
			.mask("011111110");

	protected static final MenuScheme LAYOUT_RED = new MenuScheme()
			.mask("100000001")
			.mask("000000000")
			.mask("000000000")
			.mask("000000000")
			.mask("100000001");


	public GangsHelpGui(Player player) {
		super(player, 5, "Gangs Help");
	}

	@Override
	public void redraw() {

		if (isFirstDraw()) {

			MenuPopulator populator = LAYOUT_WHITE.newPopulator(this);

			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
			}

			populator = LAYOUT_RED.newPopulator(this);

			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
			}

			//Info
			this.setItem(13, ItemStackBuilder.of(SkullUtils.INFO_SKULL.clone()).name("&eWhat it is ?").lore("&7Gangs are a way to let", "&7your players play together in", "&7competitive groups!").buildItem().build());

			//Commands
			this.setItem(21, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&ePlayer Commands").lore(
					"&f/gang create [name]",
					"&7Create a gang.",
					" ",
					"&f/gang invite [player]",
					"&7Invite a player to your gang.",
					" ",
					"&f/gang kick [player]",
					"&7Kick a player for your gang.",
					" ",
					"&f/gang info [player/gang]",
					"&7Shows information about gang.",
					" ",
					"&f/gang chat",
					"&7Toggle gang chat.",
					" ",
					"&f/gang top",
					"&7Display top gangs based on their value.",
					" ",
					"&f/gang accept",
					"&7Accept a gang invite.",
					" ",
					"&f/gang disband",
					"&7Disband your gang."
			).buildItem().build());

			//Commands
			this.setItem(23, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&eAdmin Commands").lore(
					"&f/gang value [add/remove] [gang] [amount]",
					"&7Add/remove value from/to a gang.",
					" ",
					"&f/gang admin [add/remove] [player] [gang]",
					"&7Forcibly add or remove player from a gang.",
					" ",
					"&f/gang admin disband [gang]",
					"&7Forcibly disband a gang."
			).buildItem().build());

			//Back
			this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lBack").lore("&7Back to main gui.").build(() -> {
				this.close();
				new HelpGui(this.getPlayer()).open();
			}));

			this.setItem(44, ItemStackBuilder.of(SkullUtils.HELP_SKULL.clone()).name("&e&lNeed more help?").lore("&7Right-Click to see plugin's Wiki", "&7Left-Click to join Discord Support.")
					.build(() -> {
						this.close();
						PlayerUtils.sendMessage(this.getPlayer(), " ");
						PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Wiki");
						PlayerUtils.sendMessage(this.getPlayer(), "&7https://github.com/Drawethree/X-Prison/wiki");
						PlayerUtils.sendMessage(this.getPlayer(), " ");
					}, () -> {
						this.close();
						PlayerUtils.sendMessage(this.getPlayer(), " ");
						PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Discord");
						PlayerUtils.sendMessage(this.getPlayer(), "&7" + Constants.DISCORD_LINK);
						PlayerUtils.sendMessage(this.getPlayer(), " ");
					}));
		}
	}
}
