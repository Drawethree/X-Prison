package me.drawethree.ultraprisoncore.mines.utils;

import com.google.gson.JsonObject;
import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.mines.model.mine.reset.ResetType;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MineLoader {

	public static Mine load(FileReader reader) {
		JsonObject obj = GsonProvider.readObject(reader);

		String name = obj.get("name").getAsString();
		Position teleportLocation = obj.get("teleport-location").isJsonNull() ? null : Position.deserialize(obj.get("teleport-location"));
		Region region = obj.get("region").isJsonNull() ? null : Region.deserialize(obj.get("region"));
		BlockPalette palette = obj.get("blocks").isJsonNull() ? new BlockPalette() : BlockPalette.deserialize(obj.get("blocks"));

		ResetType resetType = obj.get("reset-type").isJsonNull() ? ResetType.INSTANT : ResetType.of(obj.get("reset-type").getAsString());
		double resetPercentage = obj.get("reset-percentage").getAsDouble();

		return new Mine(name, region, teleportLocation, palette, resetType, resetPercentage);
	}

	public static void save(Mine mine) {
		try (FileWriter writer = new FileWriter(mine.getFile())) {
			GsonProvider.writeObject(writer, mine.serialize().getAsJsonObject());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
