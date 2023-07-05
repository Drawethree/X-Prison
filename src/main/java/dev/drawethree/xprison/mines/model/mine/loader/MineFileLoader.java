package dev.drawethree.xprison.mines.model.mine.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.drawethree.xprison.mines.managers.MineManager;
import dev.drawethree.xprison.mines.model.mine.BlockPalette;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Region;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class MineFileLoader implements MineLoader<File> {

	private final MineManager manager;

	public MineFileLoader(MineManager manager) {
		this.manager = manager;
	}

	@Override
	public Mine load(File file) {
		try (FileReader reader = new FileReader(file)) {
			JsonObject obj = GsonProvider.readObject(reader);

			String name = obj.get("name").getAsString();
			Point teleportLocation = obj.get("teleport-location").isJsonNull() ? null : Point.deserialize(obj.get("teleport-location"));
			Region region = obj.get("region").isJsonNull() ? null : Region.deserialize(obj.get("region"));
			BlockPalette palette = obj.get("blocks").isJsonNull() ? new BlockPalette() : BlockPalette.deserialize(obj.get("blocks"));

			ResetType resetType = obj.get("reset-type").isJsonNull() ? ResetType.INSTANT : ResetType.of(obj.get("reset-type").getAsString());
			double resetPercentage = obj.get("reset-percentage").getAsDouble();
			int resetTime = obj.has("reset-time") ? obj.get("reset-time").getAsInt() : 10;
			boolean broadcastReset = obj.get("broadcast-reset").isJsonNull() || obj.get("broadcast-reset").getAsBoolean();

			Hologram blocksLeftHologram = obj.has("hologram-blocks-left") ? obj.get("hologram-blocks-left").isJsonNull() ? null : Hologram.deserialize(obj.get("hologram-blocks-left")) : null;
			Hologram blocksMinedHologram = obj.has("hologram-blocks-mined") ? obj.get("hologram-blocks-mined").isJsonNull() ? null : Hologram.deserialize(obj.get("hologram-blocks-mined")) : null;
			Hologram timedResetHologram = obj.has("hologram-timed-reset") ? obj.get("hologram-timed-reset").isJsonNull() ? null : Hologram.deserialize(obj.get("hologram-timed-reset")) : null;

			Map<PotionEffectType, Integer> mineEffects = new HashMap<>();

			JsonElement mineEffectsObj = obj.get("effects");

			if (mineEffectsObj != null) {
				for (Map.Entry<String, JsonElement> entry : mineEffectsObj.getAsJsonObject().entrySet()) {
					PotionEffectType type = PotionEffectType.getByName(entry.getKey());
					int amplifier = entry.getValue().getAsInt();
					mineEffects.put(type, amplifier);
				}
			}

			return new Mine(this.manager, name, region, teleportLocation, palette, resetType, resetPercentage, broadcastReset, blocksLeftHologram, blocksMinedHologram, timedResetHologram, mineEffects, resetTime);
		} catch (Exception e) {
			this.manager.getPlugin().getCore().getLogger().warning("Unable to load mine " + file.getName() + "!");
			e.printStackTrace();
		}
		return null;
	}
}
