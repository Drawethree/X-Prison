package dev.drawethree.xprison.autosell.utils;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.autosell.manager.AutoSellManager;

import java.util.Comparator;

public class SellPriceComparator implements Comparator<XMaterial> {

	private final AutoSellManager manager;

	public SellPriceComparator(AutoSellManager manager) {
		this.manager = manager;
	}

	@Override
	public int compare(XMaterial o1, XMaterial o2) {
		double sellPrice1 = manager.getSellPriceForMaterial(o1);
		double sellPrice2 = manager.getSellPriceForMaterial(o2);
		return Double.compare(sellPrice1, sellPrice2);
	}
}
