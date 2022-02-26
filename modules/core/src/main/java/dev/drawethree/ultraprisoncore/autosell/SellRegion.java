package dev.drawethree.ultraprisoncore.autosell;

import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class SellRegion {

	@Getter
	private IWrappedRegion region;
	@Getter
	private String permissionRequired;
	private Map<CompMaterial, Double> sellPrices;

	public double getSellPriceFor(CompMaterial m) {
		return this.sellPrices.getOrDefault(m, 0.0);
	}

	public boolean sellsMaterial(CompMaterial m) {
		return this.sellPrices.containsKey(m);
	}

	public void addSellPrice(CompMaterial material, double price) {
		this.sellPrices.put(material, price);
	}

	public Set<CompMaterial> getSellingMaterials() {
		return this.sellPrices.keySet();
	}

	public boolean contains(Location loc) {
		return this.region.contains(loc);
	}
}
