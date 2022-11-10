package dev.drawethree.ultraprisoncore.nms.factory;

import dev.drawethree.ultraprisoncore.nms.NMSProvider;

public interface NMSProviderFactory {

	NMSProvider createNMSProvider() throws ClassNotFoundException, InstantiationException, IllegalAccessException;
}
