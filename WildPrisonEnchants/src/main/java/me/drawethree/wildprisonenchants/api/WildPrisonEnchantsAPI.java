package me.drawethree.wildprisonenchants.api;

import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;

import java.util.HashMap;

public interface WildPrisonEnchantsAPI {


    HashMap<WildPrisonEnchantment, Integer> getPlayerEnchants(Player p);

    boolean hasEnchant(Player p, int id);

    int getEnchantLevel(Player p, int id);

    boolean addEnchant(Player p, int id, int level);

    boolean removeEnchnt(Player p, int id);

}
