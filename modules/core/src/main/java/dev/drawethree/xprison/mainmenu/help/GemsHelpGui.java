package dev.drawethree.xprison.mainmenu.help;

import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GemsHelpGui extends Gui {

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


	public GemsHelpGui(Player player) {
		super(player, 5, "Gems Help");
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

			//Info
			this.setItem(13, ItemStackBuilder.of(SkullUtils.INFO_SKULL.clone()).name("&eWhat it is ?").lore(
					"&7Gems allows you to create any",
					"&7shop you want. Gems is a currency",
					"&7with tons of uses!"
			).buildItem().build());

			//Commands
			this.setItem(22, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&ePlayer Commands").lore(
					"&f/gems [player]",
					"&7View yours or player's balance.",
					" ",
					"&f/gemstop",
					"&7View a list of players with most gems.",
					" ",
					"&f/gems [add/remove/set] [player] [amount]",
					"&7Add, remove or set player's gems balance.",
					" ",
					"&f/gems pay [player] [amount]",
					"&7Give an other player gems from your balance.",
					" ",
					"&f/gems help",
					"&7Displays the help usage for gems."
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
