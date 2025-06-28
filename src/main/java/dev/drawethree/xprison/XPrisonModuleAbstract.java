package dev.drawethree.xprison;

import dev.drawethree.xprison.api.XPrisonModule;

public interface XPrisonModuleAbstract extends XPrisonModule {

	void enable();

	void disable();

	void reload();
}
