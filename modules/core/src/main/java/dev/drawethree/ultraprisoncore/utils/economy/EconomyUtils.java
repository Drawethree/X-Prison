package dev.drawethree.ultraprisoncore.utils.economy;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class EconomyUtils {

    private static final Economy ECONOMY = UltraPrisonCore.getInstance().getEconomy();

    public static EconomyResponse deposit(Player player, double amount) {
        return ECONOMY.depositPlayer(player, amount);
    }

    public static EconomyResponse withdraw(Player player, double amount) {
        return ECONOMY.withdrawPlayer(player, amount);
    }
}
