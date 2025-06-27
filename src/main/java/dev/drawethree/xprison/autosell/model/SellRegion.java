package dev.drawethree.xprison.autosell.model;

import com.cryptomorin.xseries.XMaterial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@ToString
public class SellRegion {

    @Getter
    private final IWrappedRegion region;
    @Getter
    private final World world;

    @Getter
    private final String permissionRequired;
    private final Map<XMaterial, Double> sellPrices;


    public SellRegion(IWrappedRegion region, World world) {
        this.region = region;
        this.world = world;
        this.permissionRequired = null;
        this.sellPrices = new HashMap<>();
    }

    public double getSellPriceForMaterial(XMaterial material) {
        return sellPrices.getOrDefault(material, 0.0);
    }

    public Set<XMaterial> getSellingMaterials() {
        return this.sellPrices.keySet();
    }

    public Set<XMaterial> getSellingMaterialsSorted(Comparator<XMaterial> comparator) {
        return this.sellPrices.keySet().stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void addSellPrice(XMaterial material, double price) {
        this.sellPrices.put(material, price);
    }

    public boolean contains(Location loc) {
        return Objects.equals(loc.getWorld(), this.world) && this.region.contains(loc);
    }

    public boolean canPlayerSellInRegion(Player player) {
        if (this.permissionRequired == null || this.permissionRequired.isEmpty()) {
            return true;
        }
        return player.hasPermission(this.permissionRequired);
    }

    public double getPriceForItem(ItemStack item) {
        XMaterial material = XMaterial.matchXMaterial(item);
        return item.getAmount() * this.sellPrices.getOrDefault(material, 0.0);
    }

    public Map<AutoSellItemStack, Double> previewInventorySell(Player player) {
        return previewItemsSell(Arrays.asList(player.getInventory().getContents()));
    }

    public Map<AutoSellItemStack, Double> previewItemsSell(Collection<ItemStack> items) {

        Map<AutoSellItemStack, Double> itemsToSell = new HashMap<>();

        for (ItemStack item : items) {

            if (item == null) {
                continue;
            }

            double priceForItem = this.getPriceForItem(item);

            if (priceForItem <= 0.0) {
                continue;
            }

            itemsToSell.put(new AutoSellItemStack(item), priceForItem);
        }

        return itemsToSell;
    }

    public boolean sellsMaterial(XMaterial material) {
        return this.sellPrices.containsKey(material);
    }

    public void removeSellPrice(XMaterial material) {
        this.sellPrices.remove(material);
    }
}
