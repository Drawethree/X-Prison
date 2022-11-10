package dev.drawethree.ultraprisoncore.nms.factory.impl;

import dev.drawethree.ultraprisoncore.nms.NMSProvider;
import dev.drawethree.ultraprisoncore.nms.factory.NMSProviderFactory;
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
