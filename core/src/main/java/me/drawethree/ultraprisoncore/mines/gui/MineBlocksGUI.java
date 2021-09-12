package me.drawethree.ultraprisoncore.mines.gui;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineBlocksGUI extends Gui {

	private Mine mine;

	public MineBlocksGUI(Mine mine, Player player) {
		super(player, 5, mine.getName() + " - Blocks");
		this.mine = mine;
	}

	@Override
	public void redraw() {
		this.clearItems();
		for (CompMaterial material : this.mine.getBlockPalette().getMaterials()) {
			double chance = this.mine.getBlockPalette().getPercentage(material);
			this.addItem(ItemStackBuilder.of(material.toItem()).name(material.name()).lore(" ", "&7Chance of spawning this blocks", String.format("&7is &b%,.2f%%", chance), " ", "&aLeft-Click &7to edit the chance", "&aRight-Click &7to remove.").build(() -> {
				this.mine.getBlockPalette().removeFromPalette(material);
				this.redraw();
			}, () -> {
				new MineEditBlockChanceGUI(this.getPlayer(), mine, material).open();
			}));
		}

		this.setItem(36, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to panel").build(() -> {
			this.close();
			new MinePanelGUI(this.mine, this.getPlayer()).open();
		}));
	}
}
