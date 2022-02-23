package me.drawethree.ultraprisoncore.mines.migration;

import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.utils.MigrationUtils;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.jet315.prisonmines.JetsPrisonMines;
import me.jet315.prisonmines.JetsPrisonMinesAPI;
import me.jet315.prisonmines.mine.Mine;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class JetsPrisonMinesMigration extends MinesMigration {

	private JetsPrisonMinesAPI api;

	JetsPrisonMinesMigration() {
		super(UltraPrisonMines.getInstance(), "JetsPrisonMines");
		this.api = ((JetsPrisonMines) Bukkit.getPluginManager().getPlugin(this.fromPlugin)).getAPI();
	}

	@Override
	public boolean migrate(CommandSender sender) {

		if (this.api == null) {
			PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_plugin_not_present").replace("%plugin%", this.fromPlugin));
			return false;
		}

		int completed = 0, failed = 0, skipped = 0;

		PlayerUtils.sendMessage(sender, this.mines.getMessage("mine_migration_started").replace("%plugin%", this.fromPlugin));

		for (Mine mine : this.api.getMines()) {
			String name = mine.getCustomName();

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