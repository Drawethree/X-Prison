package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class MinePanelGUI extends Gui {

	private MineImpl mineImpl;

	public MinePanelGUI(MineImpl mineImpl, Player player) {
		super(player, 5, mineImpl.getName() + " Panel");
		this.mineImpl = mineImpl;
	}

	@Override
	public void redraw() {
		for (int i = 0; i < 5 * 9; i++) {
			this.setItem(i, ItemStackBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).name("&a").buildItem().build());
		}

		this.setItem(11, ItemStackBuilder.of(XMaterial.DIAMOND_ORE.parseItem()).name("&eBlock Percentages").lore(" ", "&7Click to modify blocks in this mine.").build(() -> {
			this.close();
			new MineBlocksGUI(this.mineImpl, this.getPlayer()).open();
		}));

		this.setItem(13, ItemStackBuilder.of(XMaterial.BEACON.parseItem()).name("&eSpawn Location").lore(" ", "&aLeft-Click &7to teleport to mine", "&aRight-Click &7to set the location").build(() -> {
			if (XPrisonMines.getInstance().getManager().setTeleportLocation(this.getPlayer(), this.mineImpl)) {
				this.close();
			}
		}, () -> {
			this.close();
			XPrisonMines.getInstance().getManager().teleportToMine(this.getPlayer(), this.mineImpl);
		}));

		this.setItem(15, ItemStackBuilder.of(XMaterial.COMPARATOR.parseItem()).name("&eMine Reset Options").lore(" ", "&7Click to modify the reset options.").build(() -> {
			this.close();
			new MineResetOptionsGUI(this.mineImpl, this.getPlayer()).open();
		}));

		this.setItem(36, ItemStackBuilder.of(XMaterial.ARROW.parseItem()).name("&cBack").lore("&7Click to show all mines").build(() -> {
			this.close();
			XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
		}));

		this.setItem(44, ItemStackBuilder.of(XMaterial.BARRIER.parseItem()).name("&c&lDELETE MINE").lore("&7This action cannot be undone!", " ", "&aShift-Left-Click &7to delete this mine.").build(ClickType.SHIFT_LEFT, () -> {
			if (XPrisonMines.getInstance().getManager().deleteMine(this.getPlayer(), mineImpl)) {
				this.close();
				XPrisonMines.getInstance().getManager().openMinesListGUI(this.getPlayer());
			}
		}));

	}
}
