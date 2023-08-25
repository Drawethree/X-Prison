package dev.drawethree.xprison.autosell.utils;

import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.utils.compat.CompMaterial;

import java.util.Comparator;

public class SellPriceComparator implements Comparator<CompMaterial> {

	private final SellRegion region;

	public SellPriceComparator(SellRegion region) {
		this.region = region;
	}

	@Override
	public int compare(CompMaterial o1, CompMaterial o2) {
		double sellPrice1 = region.getSellPriceForMaterial(o1);
		double sellPrice2 = region.getSellPriceForMaterial(o2);
		return Double.compare(sellPrice1, sellPrice2);
	}
}
