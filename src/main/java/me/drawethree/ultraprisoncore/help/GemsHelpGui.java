package me.drawethree.ultraprisoncore.help;

import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
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
				populator.accept(ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name(" ").buildItem().build());
			}

			populator = LAYOUT_RED.newPopulator(this);
			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(14).name(" ").buildItem().build());
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
						this.getPlayer().sendMessage(" ");
						this.getPlayer().sendMessage(Text.colorize("&eUltraPrisonCore - Wiki"));
						this.getPlayer().sendMessage(Text.colorize("&7https://github.com/Drawethree/UltraPrisonCore/wiki"));
						this.getPlayer().sendMessage(" ");
					}, () -> {
						this.close();
						this.getPlayer().sendMessage(" ");
						this.getPlayer().sendMessage(Text.colorize("&eUltraPrisonCore - Discord"));
						this.getPlayer().sendMessage(Text.colorize("&7https://discord.com/invite/cssWTNK"));
						this.getPlayer().sendMessage(" ");
					}));
		}


	}
}
