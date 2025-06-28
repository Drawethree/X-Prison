package dev.drawethree.xprison.mines.migration.model;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class MinesMigration<T> {

	@Getter
	protected String fromPlugin;

	public MinesMigration(String fromPlugin) {
		this.fromPlugin = fromPlugin;
	}

	public void migrate(CommandSender sender) {

		List<MineImpl> migrated = migrateAllMines(sender);

		int completed = 0, skipped = 0;

		for (MineImpl mineImpl : migrated) {
			if (this.getMinesPlugin().getManager().addMineFromMigration(mineImpl)) {
				PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_mine_completed").replace("%plugin%", this.fromPlugin).replace("%mine%", mineImpl.getName()));
				completed++;
			} else {
				PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_mine_skipped").replace("%plugin%", this.fromPlugin).replace("%mine%", mineImpl.getName()));
				skipped++;
			}
		}

		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_completed").replace("%plugin%", this.fromPlugin));
		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_result").replace("%plugin%", this.fromPlugin).replace("%completed%", String.format("%,d", completed)).replace("%skipped%", String.format("%,d", skipped)));
	}

	private List<MineImpl> migrateAllMines(CommandSender sender) {

		List<MineImpl> migratedMineImpls = new ArrayList<>();

		if (!this.preValidateMigration()) {
			PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_plugin_not_present").replace("%plugin%", this.fromPlugin));
			return migratedMineImpls;
		}

		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_started").replace("%plugin%", this.fromPlugin));

		for (T type : this.getMinesToMigrate()) {

			MineImpl mineImpl = this.migrate(type);

			if (mineImpl != null) {
				migratedMineImpls.add(mineImpl);
			}
		}

		return migratedMineImpls;
	}

	private XPrisonMines getMinesPlugin() {
		return XPrisonMines.getInstance();
	}

	protected abstract boolean preValidateMigration();

	protected abstract MineImpl migrate(T mineClass);

	protected abstract List<T> getMinesToMigrate();

}
