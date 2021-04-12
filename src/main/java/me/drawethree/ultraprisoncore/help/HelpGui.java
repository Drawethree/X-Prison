package me.drawethree.ultraprisoncore.help;

import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HelpGui extends Gui {

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


	public HelpGui(Player player) {
		super(player, 5, "UltraPrisonCore - Help Menu");
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
			this.setItem(12, ItemStackBuilder.of(Material.DIAMOND_PICKAXE).name("&e&lAutoMiner").lore("&7Click to see detailed info", "&7about AutoMiner feature.").build(() -> {
				this.close();
				new AutoMinerHelpGui(this.getPlayer()).open();
			}));
			//Autosell
			this.setItem(13, ItemStackBuilder.of(SkullUtils.MONEY_SKULL.clone()).name("&e&lAutoSell").lore("&7Click to see detailed info", "&7about AutoSell feature.").build(() -> {
				this.close();
				new AutoSellHelpGui(this.getPlayer()).open();
			}));
			//Enchants
			this.setItem(14, ItemStackBuilder.of(Material.BOOK).name("&e&lEnchants").lore("&7Click to see detailed info", "&7about Enchants feature.").build(() -> {
				this.close();
				new EnchantsHelpGui(this.getPlayer()).open();
			}));
			//Gangs
			this.setItem(20, ItemStackBuilder.of(SkullUtils.GANG_SKULL.clone()).name("&e&lGangs").lore("&7Click to see detailed info", "&7about Gangs feature.").build(() -> {
				this.close();
				new GangsHelpGui(this.getPlayer()).open();
			}));
			//Gems
			this.setItem(21, ItemStackBuilder.of(Material.EMERALD).name("&e&lGems").lore("&7Click to see detailed info", "&7about Gems feature.").build(() -> {
				this.close();
				new GemsHelpGui(this.getPlayer()).open();
			}));
			//Ranks
			this.setItem(22, ItemStackBuilder.of(SkullUtils.DIAMOND_R_SKULL.clone()).name("&e&lRanks").lore("&7Click to see detailed info", "&7about Ranks feature.").build(() -> {
				this.close();
				new RanksHelpGui(this.getPlayer()).open();
			}));
			//Prestiges
			this.setItem(23, ItemStackBuilder.of(SkullUtils.DIAMOND_P_SKULL.clone()).name("&e&lPrestiges").lore("&7Click to see detailed info", "&7about Prestiges feature.").build(() -> {
				this.close();
				new PrestigesHelpGui(this.getPlayer()).open();
			}));
			//PickaxeLevels
			this.setItem(24, ItemStackBuilder.of(Material.EXP_BOTTLE).name("&e&lPickaxe Levels").lore("&7Click to see detailed info", "&7about Pickaxe leveling feature.").build(() -> {
				this.close();
				new PickaxeLevelsHelpGui(this.getPlayer()).open();
			}));
			//Tokens
			this.setItem(30, ItemStackBuilder.of(SkullUtils.COIN_SKULL.clone()).name("&e&lTokens").lore("&7Click to see detailed info", "&7about Tokens feature.").build(() -> {
				this.close();
				new TokensHelpGui(this.getPlayer()).open();
			}));
			//Multipliers
			this.setItem(31, ItemStackBuilder.of(Material.GOLD_INGOT).name("&e&lMultipliers").lore("&7Click to see detailed info", "&7about Multipliers feature.").build(() -> {
				this.close();
				new MultipliersHelpGui(this.getPlayer()).open();
			}));

			this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lClose").lore("&7Click to close the gui.").build(this::close));
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
