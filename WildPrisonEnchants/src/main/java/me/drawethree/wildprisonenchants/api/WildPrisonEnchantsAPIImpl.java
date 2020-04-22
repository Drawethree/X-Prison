package me.drawethree.wildprisonenchants.api;

import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisonenchants.managers.EnchantsManager;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class WildPrisonEnchantsAPIImpl implements WildPrisonEnchantsAPI {

    private EnchantsManager enchantsManager;

    public WildPrisonEnchantsAPIImpl(EnchantsManager enchantsManager) {

        this.enchantsManager = enchantsManager;
    }

    @Override
    public HashMap<WildPrisonEnchantment, Integer> getPlayerEnchants(Player p) {
        return this.enchantsManager.getPlayerEnchants(p);
    }

    @Override
    public boolean hasEnchant(Player p, int id) {
        return this.enchantsManager.hasEnchant(p, id);
    }

    @Override
    public int getEnchantLevel(Player p, int id) {
        return this.enchantsManager.getEnchantLevel(p, id);
    }
}
