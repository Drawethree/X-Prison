package me.drawethree.ultraprisoncore.mainmenu.help;

import me.drawethree.ultraprisoncore.utils.Constants;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.drawethree.ultraprisoncore.utils.item.ItemStackBuilder;
import me.drawethree.ultraprisoncore.utils.misc.SkullUtils;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MultipliersHelpGui extends Gui {

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


	public MultipliersHelpGui(Player player) {
		super(player, 5, "Multipliers Help");
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
					"&7Multipliers allows your players",
					"&7to earn more money when they",
					"&7are mining."
			).buildItem().build());

			//Commands
			this.setItem(22, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&eCommands").lore(
					"&f/gmulti [sell/token] [multiplier] [duration] [time_unit] ",
					"&7Example: /gmulti sell 1.0 10 MINUTES",
					"&7Set the global sell / token multiplier for all players on server.",
					" ",
					"&f/gmulti [sell/token] reset",
					"&7Resets the global sell / token multiplier.",
					" ",
					"&f/sellmulti [player] [multiplier] [duration] [time_unit] ",
					"&7Example: /sellmulti Drawethree 1.0 10 MINUTES",
					"&7Set the personal sell multiplier for player.",
					" ",
					"&f/tokenmulti [player] [multiplier] [duration] [time_unit] ",
					"&7Example: /tokenmulti Drawethree 1.0 10 MINUTES",
					"&7Set the personal token multiplier for player.",
					" ",
					"&f/multi",
					"&7Check your active multipliers."
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
						PlayerUtils.sendMessage(this.getPlayer(), "&eUltraPrisonCore - Wiki");
						PlayerUtils.sendMessage(this.getPlayer(), "&7https://github.com/Drawethree/UltraPrisonCore/wiki");
						PlayerUtils.sendMessage(this.getPlayer(), " ");
					}, () -> {
						this.close();
						PlayerUtils.sendMessage(this.getPlayer(), " ");
						PlayerUtils.sendMessage(this.getPlayer(), "&eUltraPrisonCore - Discord");
						PlayerUtils.sendMessage(this.getPlayer(), "&7" + Constants.DISCORD_LINK);
						PlayerUtils.sendMessage(this.getPlayer(), " ");
					}));
		}
	}
}
