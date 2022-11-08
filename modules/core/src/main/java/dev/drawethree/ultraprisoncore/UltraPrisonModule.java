package dev.drawethree.ultraprisoncore;

public interface UltraPrisonModule {

	void enable();

	void disable();

	void reload();

	boolean isEnabled();

	String getName();

	boolean isHistoryEnabled();

}
