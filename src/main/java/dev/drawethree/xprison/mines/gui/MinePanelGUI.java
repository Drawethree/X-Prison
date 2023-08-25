package dev.drawethree.xprison.mines.gui;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class MinePanelGUI extends Gui {

	private Mine mine;

	public MinePanelGUI(Mine mine, Player player) {
		super(player, 5, mine.getName() + " Panel");
		this.mine = mine;
	}

	@Override
	public void redraw() {
		for (int i = 0; i < 5 * 9; i++) {
			this.setItem(i, ItemStackBuilder.of(CompMaterial.BLACK_STAINED_GLASS_PANE.toItem()).name("&a").buildItem().build());
		}

		this.setItem(11, ItemStackBuilder.of(CompMaterial.DIAMOND_ORE.toItem()).name("&eBlock Percentages").lore(" ", "&7Click to modify blocks in this mine.").build(() -> {
			this.close();
			new MineBlocksGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(13, ItemStackBuilder.of(CompMaterial.BEACON.toItem()).name("&eSpawn Location").lore(" ", "&aLeft-Click &7to teleport to mine", "&aRight-Click &7to set the location").build(() -> {
			if (XPrisonMines.getInstance().getManager().setTeleportLocation(this.getPlayer(), this.mine)) {
				this.close();
			}
		}, () -> {
			this.close();
			XPrisonMines.getInstance().getManager().teleportToMine(this.getPlayer(), this.mine);
		}));

		this.setItem(15, ItemStackBuilder.of(CompMaterial.COMPARATOR.toItem()).name("&eMine Reset Options").lore(" ", "&7Click to modify the reset options.").build(() -> {
			this.close();
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(29, ItemStackBuilder.of(CompMaterial.NAME_TAG.toItem()).name("&eMine Holograms").lore(" ", "&7Click to modify mine's holograms").build(() -> {
			this.close();
			new MineHologramsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(31, ItemStackBuilder.of(CompMaterial.NETHER_STAR.toItem()).name("&eMine Player Effects").lore(" ", "&7Click to modify mine player effects").build(() -> {
			this.close();
			new MineEffectsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(36, ItemStackBuilder.of(CompMaterial.ARROW.toItem()).name("&cBack").lore("&7Click to show all mines").build(() -> {
			this.close();
			XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
		}));

		this.setItem(44, ItemStackBuilder.of(CompMaterial.BARRIER.toItem()).name("&c&lDELETE MINE").lore("&7This action cannot be undone!", " ", "&aShift-Left-Click &7to delete this mine.").build(ClickType.SHIFT_LEFT, () -> {
			if (XPrisonMines.getInstance().getManager().deleteMine(this.getPlayer(), mine)) {
				this.close();
				XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
			}
		}));

	}
}
