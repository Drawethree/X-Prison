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

public class TokensHelpGui extends Gui {

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


	public TokensHelpGui(Player player) {
		super(player, 5, "Tokens Help");
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
					"&7Tokens is a currency that allows",
					"&7your players to upgrade their",
					"&7pickaxe enchants."
			).buildItem().build());

			//Commands
			this.setItem(22, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&eCommands").lore(
					"&f/tokens [player]",
					"&7View yours or player's tokens balance.",
					" ",
					"&f/tokenstop",
					"&7View a list of players with most tokens.",
					" ",
					"&f/tokens [add/remove/set] [player] [amount]",
					"&7Add, remove or set player's tokens balance.",
					" ",
					"&f/tokens pay [player] [amount]",
					"&7Give an other player tokens from your balance.",
					" ",
					"&f/tokens withdraw [amount] [value]",
					"&7Withdraw your tokens to a physical form.",
					" ",
					"&f/tokens help",
					"&7Displays the help usage for tokens."
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
