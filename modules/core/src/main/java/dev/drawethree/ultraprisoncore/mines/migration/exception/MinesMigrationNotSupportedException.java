package dev.drawethree.ultraprisoncore.mines.migration.exception;

public class MinesMigrationNotSupportedException extends Throwable {

	public MinesMigrationNotSupportedException(String pluginName) {
		super("Mines migration from plugin " + pluginName + " is not supported!");
	}
}
