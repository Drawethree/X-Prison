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

public class MinesHelpGui extends Gui {

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


	public MinesHelpGui(Player player) {
		super(player, 5, "Mines Help");
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
					"&7With Mines module",
					"&7you can create unlimited",
					"&7amount of prison mines",
					"&7where your players can",
					"&7mine blocks and progress."
			).buildItem().build());

			//Commands
			this.setItem(21, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&eAdmin Commands").lore(
					"&f/mines create [name]",
					"&7Create a mine.",
					" ",
					"&f/mines delete [name]",
					"&7Deletes a mine.",
					" ",
					"&f/mines redefine [name]",
					"&7Redefine mine region.",
					" ",
					"&f/mines rename [name] [new_name]",
					"&7Renames a mine.",
					" ",
					"&f/mines reset [name]",
					"&7Resets a mine contents.",
					" ",
					"&f/mines panel [name]",
					"&7Opens admin panel for a mine.",
					" ",
					"&f/mines save [name]",
					"&7Saves a mine."
			).buildItem().build());

			//Commands
			this.setItem(23, ItemStackBuilder.of(SkullUtils.COMMAND_BLOCK_SKULL.clone()).name("&eAdmin Commands").lore(
					"&f/mines addblock [name]",
					"&7Adds a block to a mine you hold in hand.",
					" ",
					"&f/mines settp [name]",
					"&7Sets teleport location for a mine.",
					" ",
					"&f/mines tp [name]",
					"&7Teleports to a mine.",
					" ",
					"&f/mines list",
					"&7Shows all mines.",
					" ",
					"&f/mines tool",
					"&7Gives you a mine selection tool.",
					" ",
					"&f/mines migrate <plugin>",
					"&7Migrates mines from other plugins.",
					"&7Currently supported: &fJetsPrisonMines, MineResetLite&7."
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
