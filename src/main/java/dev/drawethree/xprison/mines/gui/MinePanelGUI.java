package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.Mine;
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
			this.setItem(i, ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name("&a").buildItem().build());
		}

		this.setItem(11, ItemStackBuilder.of(XMaterial.DIAMOND_ORE.parseItem()).name("&eBlock Percentages").lore(" ", "&7Click to modify blocks in this mine.").build(() -> {
			this.close();
			new MineBlocksGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(13, ItemStackBuilder.of(XMaterial.BEACON.parseItem()).name("&eSpawn Location").lore(" ", "&aLeft-Click &7to teleport to mine", "&aRight-Click &7to set the location").build(() -> {
			if (XPrisonMines.getInstance().getManager().setTeleportLocation(this.getPlayer(), this.mine)) {
				this.close();
			}
		}, () -> {
			this.close();
			XPrisonMines.getInstance().getManager().teleportToMine(this.getPlayer(), this.mine);
		}));

		this.setItem(15, ItemStackBuilder.of(XMaterial.COMPARATOR.parseItem()).name("&eMine Reset Options").lore(" ", "&7Click to modify the reset options.").build(() -> {
			this.close();
			new MineResetOptionsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(29, ItemStackBuilder.of(XMaterial.NAME_TAG.parseItem()).name("&eMine Holograms").lore(" ", "&7Click to modify mine's holograms").build(() -> {
			this.close();
			new MineHologramsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(31, ItemStackBuilder.of(XMaterial.NETHER_STAR.parseItem()).name("&eMine Player Effects").lore(" ", "&7Click to modify mine player effects").build(() -> {
			this.close();
			new MineEffectsGUI(this.mine, this.getPlayer()).open();
		}));

		this.setItem(36, ItemStackBuilder.of(XMaterial.ARROW.parseItem()).name("&cBack").lore("&7Click to show all mines").build(() -> {
			this.close();
			XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
		}));

		this.setItem(44, ItemStackBuilder.of(XMaterial.BARRIER.parseItem()).name("&c&lDELETE MINE").lore("&7This action cannot be undone!", " ", "&aShift-Left-Click &7to delete this mine.").build(ClickType.SHIFT_LEFT, () -> {
			if (XPrisonMines.getInstance().getManager().deleteMine(this.getPlayer(), mine)) {
				this.close();
				XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
			}
		}));

	}
}
