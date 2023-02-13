package dev.drawethree.xprison.nms.factory.impl;

import dev.drawethree.xprison.nms.NMSProvider;
import dev.drawethree.xprison.nms.factory.NMSProviderFactory;
import org.bukkit.Bukkit;

public class NMSProviderFactoryImpl implements NMSProviderFactory {


	public NMSProviderFactoryImpl() {
	}

	@Override
	public NMSProvider createNMSProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String packageName = NMSProvider.class.getPackage().getName();
		String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		NMSProvider nmsProvider = (NMSProvider) Class.forName(packageName + ".NMSProvider_" + internalsName).newInstance();
		return nmsProvider;
	}
}
