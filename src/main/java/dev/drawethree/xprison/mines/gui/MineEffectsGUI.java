package dev.drawethree.xprison.mines.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.potion.PotionEffectType;

public class MineEffectsGUI extends Gui {

	private MineImpl mineImpl;

	public MineEffectsGUI(MineImpl mineImpl, Player player) {
		super(player, 3, "Player effects");
		this.mineImpl = mineImpl;
	}

	@Override
	public void redraw() {

		this.clearItems();

		for (PotionEffectType type : PotionEffectType.values()) {
			if (type == null) {
				continue;
			}
			this.addItem(this.getItemForEffect(type));
		}

		this.setItem(26, ItemStackBuilder.of(Material.ARROW).name("&cBack").lore("&7Click to go back to panel").build(() -> {
			this.close();
			new MinePanelGUI(this.mineImpl, this.getPlayer()).open();
		}));
	}

	private Item getItemForEffect(PotionEffectType type) {
		boolean enabled = this.mineImpl.isEffectEnabled(type);

		if (enabled) {
			return ItemStackBuilder.of(XMaterial.GLOWSTONE_DUST.parseItem()).name("&7" + type.getName() + " &aENABLED &b(" + this.mineImpl.getEffectLevel(type) + ")").lore("&aShift-Left-Click &7to &aincrease.", "&aShift-Right-Click &7to &cdecrease.", "&aClick &7to disable.").buildItem().bind(event -> {
				switch (event.getClick()) {
					case LEFT:
						this.mineImpl.disableEffect(type);
						break;
					case SHIFT_LEFT:
						this.mineImpl.increaseEffect(type);
						break;
					case SHIFT_RIGHT:
						this.mineImpl.decreaseEffect(type);
						break;
				}
				this.redraw();
			}, ClickType.LEFT, ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT).build();
		} else {
			return ItemStackBuilder.of(XMaterial.GUNPOWDER.parseItem()).name("&7" + type.getName() + " &cDISABLED").lore("&aClick &7to enable.").build(() -> {
				this.mineImpl.enableEffect(type);
				this.redraw();
			});
		}
	}
}
