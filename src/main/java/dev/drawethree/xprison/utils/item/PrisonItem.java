package dev.drawethree.xprison.utils.item;

import com.saicone.rtag.RtagItem;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.repo.EnchantsRepository;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PrisonItem extends RtagItem {

    private static final String MAIN = "upc";

    public PrisonItem(ItemStack item) {
        super(item);
    }

    public Map<XPrisonEnchantment, Integer> getEnchants(EnchantsRepository repository) {
        final Map<XPrisonEnchantment, Integer> enchants = new HashMap<>();
        final Map<String, Object> map = get(MAIN, "enchants");
        if (map != null) {
            map.forEach((id, level) -> enchants.put(repository.getEnchantBy(id), (int) level));
        }
        return enchants;
    }

    public int getEnchantLevel(XPrisonEnchantment enchant) {
        return getOptional(MAIN, "enchants", String.valueOf(enchant.getId())).or(0);
    }

    public long getBrokenBlocks() {
        return getOptional(MAIN, "blocks").or(0L);
    }

    public Long getGems() {
        return get(MAIN, "gems");
    }

    public Integer getLevel() {
        return get(MAIN, "level");
    }

    public Long getTokens() {
        return get(MAIN, "tokens");
    }

    public void setEnchant(XPrisonEnchantment enchant, int level) {
        if (level > 0) {
            set(level, MAIN, "enchants", String.valueOf(enchant.getId()));
        } else {
            remove(MAIN, "enchants", String.valueOf(enchant.getId()));
        }
    }

    public void setGems(long amount) {
        set(amount, MAIN, "gems");
    }

    public void setLevel(int level) {
        set(level, MAIN, "level");
    }

    public void setTokens(long amount) {
        set(amount, MAIN, "tokens");
    }

    public void addBrokenBlocks(int amount) {
        set(getBrokenBlocks() + amount, MAIN, "blocks");
    }
}
