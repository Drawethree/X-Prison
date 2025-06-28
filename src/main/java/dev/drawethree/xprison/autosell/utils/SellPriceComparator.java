package dev.drawethree.xprison.autosell.utils;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.model.SellRegionImpl;

import java.util.Comparator;

public class SellPriceComparator implements Comparator<XMaterial> {

	private final SellRegionImpl region;

	public SellPriceComparator(SellRegionImpl region) {
		this.region = region;
	}

	@Override
	public int compare(XMaterial o1, XMaterial o2) {
		double sellPrice1 = region.getSellPriceForMaterial(o1);
		double sellPrice2 = region.getSellPriceForMaterial(o2);
		return Double.compare(sellPrice1, sellPrice2);
	}
}
