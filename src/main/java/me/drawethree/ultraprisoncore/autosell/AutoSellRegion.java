package me.drawethree.ultraprisoncore.autosell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class AutoSellRegion {

    private IWrappedRegion region;
    @Getter
    private String permissionRequired;
    private Map<Material, Double> sellPrices;


    public double getSellPriceFor(Material m) {
        return this.sellPrices.getOrDefault(m, 0.0);
    }

    public boolean sellsMaterial(Material m) {
        return this.sellPrices.containsKey(m);
    }

    public void addSellPrice(Material material, double price) {
        this.sellPrices.put(material, price);
    }

    public Set<Material> getSellingMaterials() {
        return this.sellPrices.keySet();
    }
}
