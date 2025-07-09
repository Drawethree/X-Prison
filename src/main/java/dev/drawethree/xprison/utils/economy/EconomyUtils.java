package dev.drawethree.xprison.utils.economy;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.currency.CurrencyType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public class EconomyUtils {

    private static final Economy ECONOMY = XPrison.getInstance().getEconomy();

    public static EconomyResponse deposit(Player player, double amount) {
        return ECONOMY.depositPlayer(player, amount);
    }

    public static EconomyResponse withdraw(Player player, double amount) {
        return ECONOMY.withdrawPlayer(player, amount);
    }

    public static String getCurrencyName(CurrencyType type) {
        switch (type) {
            case GEMS, TOKENS -> {
                return StringUtils.capitalize(type.name().toLowerCase());
            } case VAULT -> {
                return ECONOMY.getName();
            } default -> throw new IllegalArgumentException("Invalid currencyType: " + type);
        }
    }
}
