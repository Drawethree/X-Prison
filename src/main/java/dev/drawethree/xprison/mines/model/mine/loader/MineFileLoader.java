package dev.drawethree.xprison.mines.model.mine.loader;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.mines.managers.MineManager;
import dev.drawethree.xprison.mines.model.mine.BlockPaletteImpl;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Region;

import java.io.File;
import java.io.FileReader;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;

public class MineFileLoader implements MineLoader<File> {

	private final MineManager manager;

	public MineFileLoader(MineManager manager) {
		this.manager = manager;
	}

	@Override
	public MineImpl load(File file) {
		try (FileReader reader = new FileReader(file)) {
			JsonObject obj = GsonProvider.readObject(reader);

			String name = obj.get("name").getAsString();
			Point teleportLocation = obj.get("teleport-location").isJsonNull() ? null : Point.deserialize(obj.get("teleport-location"));
			Region region = obj.get("region").isJsonNull() ? null : Region.deserialize(obj.get("region"));
			BlockPaletteImpl palette = obj.get("blocks").isJsonNull() ? new BlockPaletteImpl() : BlockPaletteImpl.deserialize(obj.get("blocks"));

			ResetType resetType = obj.get("reset-type").isJsonNull() ? ResetType.INSTANT : ResetType.of(obj.get("reset-type").getAsString());
			double resetPercentage = obj.get("reset-percentage").getAsDouble();
			int resetTime = obj.has("reset-time") ? obj.get("reset-time").getAsInt() : 10;
			boolean broadcastReset = obj.get("broadcast-reset").isJsonNull() || obj.get("broadcast-reset").getAsBoolean();

			return new MineImpl(this.manager, name, region, teleportLocation, palette, resetType, resetPercentage, broadcastReset, resetTime);
		} catch (Exception e) {
			error("Unable to load mine " + file.getName() + "!");
			e.printStackTrace();
		}
		return null;
	}
}
