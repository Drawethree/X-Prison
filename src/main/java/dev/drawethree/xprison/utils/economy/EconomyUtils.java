package dev.drawethree.xprison.utils.economy;

import dev.drawethree.xprison.XPrison;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class EconomyUtils {

    private static final Economy ECONOMY = XPrison.getInstance().getEconomy();

    public static EconomyResponse deposit(Player player, double amount) {
        return ECONOMY.depositPlayer(player, amount);
    }

    public static EconomyResponse withdraw(Player player, double amount) {
        return ECONOMY.withdrawPlayer(player, amount);
    }
}
