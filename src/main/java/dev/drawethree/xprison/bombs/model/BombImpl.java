package dev.drawethree.xprison.bombs.model;

import com.cryptomorin.xseries.XSound;
import dev.drawethree.xprison.api.bombs.model.Bomb;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class BombImpl implements Bomb {

	private String name;
	private int radius;
	private ItemStack item;
	private XSound dropSound;
	private XSound explodeSound;
	private int explosionDelay;

	@Override
	public Sound getDropSound() {
		return dropSound.get();
	}

	@Override
	public Sound getExplodeSound() {
		return explodeSound.get();
	}
}
