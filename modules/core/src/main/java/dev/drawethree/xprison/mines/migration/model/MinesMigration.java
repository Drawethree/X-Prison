package dev.drawethree.xprison.mines.migration.model;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.Mine;
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

		List<Mine> migrated = migrateAllMines(sender);

		int completed = 0, skipped = 0;

		for (Mine mine : migrated) {
			if (this.getMinesPlugin().getManager().addMineFromMigration(mine)) {
				PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_mine_completed").replace("%plugin%", this.fromPlugin).replace("%mine%", mine.getName()));
				completed++;
			} else {
				PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_mine_skipped").replace("%plugin%", this.fromPlugin).replace("%mine%", mine.getName()));
				skipped++;
			}
		}

		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_completed").replace("%plugin%", this.fromPlugin));
		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_result").replace("%plugin%", this.fromPlugin).replace("%completed%", String.format("%,d", completed)).replace("%skipped%", String.format("%,d", skipped)));
	}

	private List<Mine> migrateAllMines(CommandSender sender) {

		List<Mine> migratedMines = new ArrayList<>();

		if (!this.preValidateMigration()) {
			PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_plugin_not_present").replace("%plugin%", this.fromPlugin));
			return migratedMines;
		}

		PlayerUtils.sendMessage(sender, this.getMinesPlugin().getMessage("mine_migration_started").replace("%plugin%", this.fromPlugin));

		for (T type : this.getMinesToMigrate()) {

			Mine mine = this.migrate(type);

			if (mine != null) {
				migratedMines.add(mine);
			}
		}

		return migratedMines;
	}

	private XPrisonMines getMinesPlugin() {
		return XPrisonMines.getInstance();
	}

	protected abstract boolean preValidateMigration();

	protected abstract Mine migrate(T mineClass);

	protected abstract List<T> getMinesToMigrate();

}
