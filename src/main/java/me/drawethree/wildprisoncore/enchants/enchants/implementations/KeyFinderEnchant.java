package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KeyFinderEnchant extends WildPrisonEnchantment {

	private final double chance;
    private final List<String> commandsToExecute;
	//private static final HashMap<UUID, Long> LAST_CLAIMED = new HashMap<>();

    public KeyFinderEnchant(WildPrisonEnchants instance) {
        super(instance, 15);
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        this.commandsToExecute = plugin.getConfig().get().getStringList("enchants." + id + ".Commands");

		/*Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					LAST_CLAIMED.remove(e.getPlayer().getUniqueId());
				}).bindWith(instance.getCore());

		 */
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)/*!LAST_CLAIMED.containsKey(e.getPlayer().getUniqueId()) || getDelayBetweenKeys(enchantLevel) <= TimeUnit.MILLISECONDS.toSeconds(Time.nowMillis() - LAST_CLAIMED.get(e.getPlayer().getUniqueId()))*/) {
			String randomCmd = this.commandsToExecute.get(ThreadLocalRandom.current().nextInt(commandsToExecute.size()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.replace("%player%", e.getPlayer().getName()));
			//LAST_CLAIMED.put(e.getPlayer().getUniqueId(), Time.nowMillis());
		}
	}


	private int getDelayBetweenKeys(int enchantLevel) {
		if (enchantLevel <= 49) {
			return 30;
		} else if (enchantLevel <= 99) {
			return 25;
		} else if (enchantLevel <= 149) {
			return 20;
		} else if (enchantLevel <= 199) {
			return 15;
		} else {
			return 10;
		}
	}
}
