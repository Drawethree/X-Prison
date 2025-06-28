package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.HologramType;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MineHologramsGUI extends Gui {

	private final MineImpl mineImpl;

	public MineHologramsGUI(MineImpl mineImpl, Player player) {
		super(player, 1, "Mine Holograms");
		this.mineImpl = mineImpl;
	}

	@Override
	public void redraw() {
		this.setItem(0, ItemStackBuilder.of(XMaterial.NAME_TAG.parseItem()).name("&eBlocks Mined Hologram").lore(" ", "&aLeft-Click &7to spawn on your location.", "&aRight-Click &7to remove.").build(() -> {
			this.close();
			this.mineImpl.getManager().deleteHologram(this.mineImpl, HologramType.BLOCKS_MINED, this.getPlayer());
		}, () -> {
			this.close();
			this.mineImpl.getManager().createHologram(this.mineImpl, HologramType.BLOCKS_MINED, this.getPlayer());
		}));
		this.setItem(1, ItemStackBuilder.of(XMaterial.NAME_TAG.parseItem()).name("&eBlocks Left Hologram").lore(" ", "&aLeft-Click &7to spawn on your location.", "&aRight-Click &7to remove.").build(() -> {
			this.close();
			this.mineImpl.getManager().deleteHologram(this.mineImpl, HologramType.BLOCKS_LEFT, this.getPlayer());
		}, () -> {
			this.close();
			this.mineImpl.getManager().createHologram(this.mineImpl, HologramType.BLOCKS_LEFT, this.getPlayer());
		}));
		this.setItem(2, ItemStackBuilder.of(XMaterial.NAME_TAG.parseItem()).name("&eTimed Reset Hologram").lore(" ", "&aLeft-Click &7to spawn on your location.", "&aRight-Click &7to remove.").build(() -> {
			this.close();
			this.mineImpl.getManager().deleteHologram(this.mineImpl, HologramType.TIMED_RESET, this.getPlayer());
		}, () -> {
			this.close();
			this.mineImpl.getManager().createHologram(this.mineImpl, HologramType.TIMED_RESET, this.getPlayer());
		}));

		this.setItem(8, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to panel").build(() -> {
			this.close();
			new MinePanelGUI(this.mineImpl, this.getPlayer()).open();
		}));
	}
}
