package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineBlocksGUI extends Gui {

	private final MineImpl mineImpl;

	public MineBlocksGUI(MineImpl mineImpl, Player player) {
		super(player, 5, mineImpl.getName() + " - Blocks");
		this.mineImpl = mineImpl;
	}

	@Override
	public void redraw() {
		this.clearItems();
		for (XMaterial material : this.mineImpl.getBlockPaletteImpl().getMaterials()) {
			double chance = this.mineImpl.getBlockPaletteImpl().getPercentage(material);
			this.addItem(ItemStackBuilder.of(material.parseItem()).name(material.name()).lore(" ", "&7Chance of spawning this blocks", String.format("&7is &b%,.2f%%", chance), " ", "&aLeft-Click &7to edit the chance", "&aRight-Click &7to remove.").build(() -> {
				this.mineImpl.getBlockPaletteImpl().remove(material);
				this.redraw();
			}, () -> {
				new MineEditBlockChanceGUI(this.getPlayer(), mineImpl, material).open();
			}));
		}

		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to panel").build(() -> {
			this.close();
			new MinePanelGUI(this.mineImpl, this.getPlayer()).open();
		}));
	}
}
