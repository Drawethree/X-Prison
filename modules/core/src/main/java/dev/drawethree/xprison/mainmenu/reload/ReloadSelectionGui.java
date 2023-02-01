package dev.drawethree.xprison.mainmenu.reload;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.mainmenu.confirmation.ReloadModuleConfirmationGui;
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

public class ReloadSelectionGui extends Gui {

	private static final MenuScheme LAYOUT_WHITE = new MenuScheme()
			.mask("011111110")
			.mask("110000011")
			.mask("100000001")
			.mask("110000011")
			.mask("011111110");

	private static final MenuScheme LAYOUT_RED = new MenuScheme()
			.mask("100000001")
			.mask("000000000")
			.mask("000000000")
			.mask("000000000")
			.mask("100000001");

	private static final MenuScheme CONTENT = new MenuScheme()
			.mask("000000000")
			.mask("000111000")
			.mask("001111100")
			.mask("000111000")
			.mask("000000000");

	private final XPrison core;

	public ReloadSelectionGui(XPrison core, Player player) {
		super(player, 5, "UPC - Reload Module");
		this.core = core;
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

			//AutoMiner
			this.setItem(11, ItemStackBuilder.of(Material.DIAMOND_PICKAXE).name("&e&lAutoMiner").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getAutoMiner()).open();
			}));

			//Autosell
			this.setItem(12, ItemStackBuilder.of(SkullUtils.MONEY_SKULL.clone()).name("&e&lAutoSell").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getAutoSell()).open();
			}));

			//Enchants
			this.setItem(13, ItemStackBuilder.of(CompMaterial.ENCHANTED_BOOK.toItem()).name("&e&lEnchants").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getEnchants()).open();
			}));

			//Gangs
			this.setItem(14, ItemStackBuilder.of(SkullUtils.GANG_SKULL.clone()).name("&e&lGangs").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getGangs()).open();
			}));

			//Gems
			this.setItem(15, ItemStackBuilder.of(Material.EMERALD).name("&e&lGems").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getGems()).open();
			}));

			//Ranks
			this.setItem(19, ItemStackBuilder.of(SkullUtils.DIAMOND_R_SKULL.clone()).name("&e&lRanks").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getRanks()).open();
			}));

			//Prestiges
			this.setItem(20, ItemStackBuilder.of(SkullUtils.DIAMOND_P_SKULL.clone()).name("&e&lPrestiges").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getPrestiges()).open();
			}));

			//PickaxeLevels
			this.setItem(21, ItemStackBuilder.of(CompMaterial.EXPERIENCE_BOTTLE.toItem()).name("&e&lPickaxe Levels").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getPickaxeLevels()).open();
			}));

			//Tokens
			this.setItem(22, ItemStackBuilder.of(SkullUtils.COIN_SKULL.clone()).name("&e&lTokens").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getTokens()).open();
			}));

			//Multipliers
			this.setItem(23, ItemStackBuilder.of(Material.GOLD_INGOT).name("&e&lMultipliers").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getMultipliers()).open();
			}));

			//Mines
			this.setItem(24, ItemStackBuilder.of(Material.DIAMOND_ORE).name("&e&lMines").lore("&7Click to reload this module.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getMines()).open();
			}));

			//History
			this.setItem(25, ItemStackBuilder.of(CompMaterial.BOOK.toItem()).name("&e&lHistory").lore("&7Click to reset this module", "&7player data.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), this.core.getHistory()).open();
			}));

			//All Modules
			this.setItem(31, ItemStackBuilder.of(CompMaterial.REDSTONE_BLOCK.toItem()).name("&e&lReload All Modules").lore("&7Click to reload all modules.").build(() -> {
				new ReloadModuleConfirmationGui(this.getPlayer(), null).open();
			}));

			this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lClose").lore("&7Click to close the gui.").build(this::close));
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
