package dev.drawethree.xprison.bombs.service;

import com.saicone.rtag.RtagItem;
import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.bombs.handler.BlockHandler;
import dev.drawethree.xprison.bombs.handler.SellHandler;
import dev.drawethree.xprison.bombs.handler.impl.BlockHandlerImpl;
import dev.drawethree.xprison.bombs.handler.impl.SellHandlerImpl;
import lombok.Getter;
import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class BombsService {

	private static final String BOMB_NBT_TAG = "x-prison-bomb";

	private final XPrisonBombs plugin;
	@Getter
	private BlockHandler blockHandler;
	@Getter
	private SellHandler sellHandler;

	public BombsService(XPrisonBombs plugin) {
		this.plugin = plugin;
	}

	public void enable() {
		this.loadBlocksHandler();
		this.loadSellHandler();
	}

	private void loadSellHandler() {
		this.sellHandler = new SellHandlerImpl();
	}

	private void loadBlocksHandler() {
		this.blockHandler = new BlockHandlerImpl();
	}

	public void disable() {

	}

	public void reload() {
		this.loadBlocksHandler();
		this.loadSellHandler();
	}

	public ItemStack createBomb(Bomb bomb, int amount) {
		ItemStack rawItem = ItemStackBuilder.of(bomb.getItem().clone()).amount(amount).build();

		RtagItem.edit(rawItem, tag -> {
			tag.set(bomb.getName(), BOMB_NBT_TAG);
		});

		return rawItem;
	}

	public void giveBomb(Bomb bomb, int amount, Player player) {
		ItemStack item = createBomb(bomb, amount);
		player.getInventory().addItem(item);

		player.sendMessage(this.plugin.getConfig().getMessage("bomb_received").
				replace("%amount%", String.valueOf(amount)).
				replace("%item%", bomb.getItem().getItemMeta().getDisplayName()));
	}

	public Optional<Bomb> getBombFromItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return Optional.empty();
		}
		Object obj = RtagItem.of(item).get(BOMB_NBT_TAG);
		if (obj == null) {
			return Optional.empty();
		} else if (!(obj instanceof String)) {
			return Optional.empty();
		} else {
			return this.plugin.getBombsRepository().getBombByName((String) obj);
		}
	}
}
