package me.drawethree.ultraprisoncore.gangs.gui;

import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.lucko.helper.Services;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.signprompt.SignPromptFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GangHelpGUI extends Gui {

	// the keyboard buttons
	private static final MenuScheme LAYOUT = new MenuScheme()
			.mask("111111111")
			.mask("100000001")
			.mask("111111111");

	private static final MenuScheme BUTTONS = new MenuScheme()
			.mask("000000000")
			.mask("001010100")
			.mask("100000000");

	public GangHelpGUI(Player player) {
		super(player, 3, "Gang Menu");
	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {
			MenuPopulator populator = LAYOUT.newPopulator(this);
			while (populator.hasSpace()) {
				populator.accept(ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(15).name("&a").buildItem().build());
			}

			populator = BUTTONS.newPopulator(this);
			populator.acceptIfSpace(ItemStackBuilder.of(Material.BOOK).name("&eGangs Info").lore("&7Create gangs to dominate the server!", "&7Maximum Gang Size: 5").buildItem().build());
			populator.acceptIfSpace(ItemStackBuilder.of(Material.WORKBENCH).name("&eCreate a Gang").lore("&7Click to create a new gang.").build(() -> {
				SignPromptFactory factory = Services.load(SignPromptFactory.class);
				factory.openPrompt(this.getPlayer(), Arrays.asList("", "§e^ ^ ^", "§7Input gang name", ""), responseHandler -> {
					if (responseHandler.get(0).isEmpty()) {
						this.getPlayer().sendMessage(UltraPrisonGangs.getInstance().getMessage("gang-invalid-name"));
						return SignPromptFactory.Response.ACCEPTED;
					}
					UltraPrisonGangs.getInstance().getGangsManager().createGang(responseHandler.get(0), this.getPlayer());
					return SignPromptFactory.Response.ACCEPTED;
				});
			}));
			populator.acceptIfSpace(ItemStackBuilder.of(Material.BEACON).name("&eGang Top").lore("&7Click to show current gang leaderboard.").build(() -> {
				this.close();
				this.getPlayer().performCommand("gang top");
			}));
			populator.acceptIfSpace(ItemStackBuilder.of(Material.ARROW).name("&7Back").build(() -> this.close()));

		}
	}
}
