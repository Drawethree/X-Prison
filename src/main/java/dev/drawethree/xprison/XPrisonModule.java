package dev.drawethree.xprison;

public interface XPrisonModule {

	void enable();

	void disable();

	void reload();

	boolean isEnabled();

	String getName();

	boolean isHistoryEnabled();

	void resetPlayerData();
}
