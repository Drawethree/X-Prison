package me.drawethree.ultraprisoncore.mines.migration;

import com.koletar.jj.mineresetlite.Mine;
import com.koletar.jj.mineresetlite.MineResetLite;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.utils.MigrationUtils;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class MineResetLiteMigration extends MinesMigration {

	private final MineResetLite plugin;

	MineResetLiteMigration() {
		super(UltraPrisonMines.getInstance(), "MineResetLite");
		this.plugin = ((MineResetLite) Bukkit.getPluginManager().getPlugin(this.fromPlugin));
	}

	@Override
	public boolean migrate(CommandSender sender) {
		if (this.plugin == null) {
			PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_plugin_not_present").replace("%plugin%", this.fromPlugin));
			return false;
		}

		int completed = 0, failed = 0, skipped = 0;

		PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_started").replace("%plugin%", this.fromPlugin));

		for (Mine mine : this.plugin.mines) {
			String name = mine.getName();

			PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_mine_started").replace("%plugin%", this.fromPlugin).replace("%mine%", name));

			if (this.mines.getManager().getMineByName(name) != null) {
				PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_mine_skipped").replace("%plugin%", this.fromPlugin).replace("%mine%", name));
				skipped++;
				continue;
			}

			me.drawethree.ultraprisoncore.mines.model.mine.Mine migrated = MigrationUtils.migrate(mine);
			if (this.mines.getManager().addMineFromMigration(sender, migrated)) {
				PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_mine_completed").replace("%plugin%", this.fromPlugin).replace("%mine%", name));
				completed++;
			} else {
				PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_mine_failed").replace("%plugin%", this.fromPlugin).replace("%mine%", name));
				failed++;
			}
		}

		PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_completed").replace("%plugin%", this.fromPlugin));
		PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_result").replace("%plugin%", this.fromPlugin).replace("%completed%", String.format("%,d", completed)).replace("%skipped%", String.format("%,d", skipped)).replace("%failed%", String.format("%,d", failed)));

		return true;
	}
}
