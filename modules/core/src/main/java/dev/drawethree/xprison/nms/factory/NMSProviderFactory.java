package dev.drawethree.xprison.nms.factory;

import dev.drawethree.xprison.nms.NMSProvider;

public interface NMSProviderFactory {

	NMSProvider createNMSProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException;
}
