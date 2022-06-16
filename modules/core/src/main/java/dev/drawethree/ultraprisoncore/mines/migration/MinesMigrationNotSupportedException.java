package dev.drawethree.ultraprisoncore.mines.migration;

public class MinesMigrationNotSupportedException extends Throwable {

	public MinesMigrationNotSupportedException(String pluginName) {
		super("Mines migration from plugin " + pluginName + " is not supported!");
	}
}
