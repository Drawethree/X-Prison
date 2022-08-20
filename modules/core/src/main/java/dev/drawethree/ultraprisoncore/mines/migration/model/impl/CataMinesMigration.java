package dev.drawethree.ultraprisoncore.mines.migration.model.impl;

import de.c4t4lysm.catamines.CataMines;
import de.c4t4lysm.catamines.schedulers.MineManager;
import de.c4t4lysm.catamines.utils.mine.components.CataMineBlock;
import de.c4t4lysm.catamines.utils.mine.mines.CuboidCataMine;
import dev.drawethree.ultraprisoncore.mines.migration.model.MinesMigration;
import dev.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public final class CataMinesMigration extends MinesMigration<CuboidCataMine> {

	private final CataMines plugin;

	public CataMinesMigration() {
		super("CataMines");
		this.plugin = ((CataMines) Bukkit.getPluginManager().getPlugin(this.fromPlugin));
	}

	@Override
	protected boolean preValidateMigration() {
		return plugin != null;
	}

	@Override
	protected Mine migrate(CuboidCataMine mine) {
		Mine.Builder builder = new Mine.Builder();

		// Name
		String name = mine.getName();
		builder.name(name);

		// Teleport Point
		Location tp = mine.getTeleportLocation();
		Point point = Point.of(tp);
		builder.teleportLocation(point);

		// Region
		String world = mine.getWorld();

		int x = mine.getRegion().getMinimumPoint().getBlockX();
		int y = mine.getRegion().getMinimumPoint().getBlockY();
		int z = mine.getRegion().getMinimumPoint().getBlockZ();
		int x1 = mine.getRegion().getMaximumPoint().getBlockX();
		int y1 = mine.getRegion().getMaximumPoint().getBlockY();
		int z1 = mine.getRegion().getMaximumPoint().getBlockZ();

		Position pos = Position.of(x, y, z, world);
		Position pos1 = Position.of(x1, y1, z1, world);

		me.lucko.helper.serialize.Region reg = me.lucko.helper.serialize.Region.of(pos, pos1);
		builder.region(reg);

		BlockPalette palette = new BlockPalette();

		for (CataMineBlock cblock : mine.getBlocks()) {
			palette.addToPalette(CompMaterial.fromMaterial(cblock.getBlockData().getMaterial()), cblock.getChance());
		}
		builder.blockPalette(palette);

		return builder.build();
	}

	@Override
	protected List<CuboidCataMine> getMinesToMigrate() {
		return new ArrayList<>(MineManager.getInstance().getMines());
	}
}
