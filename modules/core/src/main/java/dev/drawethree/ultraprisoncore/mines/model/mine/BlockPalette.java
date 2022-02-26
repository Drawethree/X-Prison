package dev.drawethree.ultraprisoncore.mines.model.mine;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlockPalette implements GsonSerializable {

	private Map<CompMaterial, Double> blockPercentages;

	public BlockPalette() {
		this.blockPercentages = new HashMap<>();
	}

	private BlockPalette(Map<CompMaterial, Double> blockPercentages) {
		this.blockPercentages = blockPercentages;
	}

	public static BlockPalette deserialize(JsonElement element) {
		Preconditions.checkArgument(element.isJsonObject());
		JsonObject object = element.getAsJsonObject();

		Map<CompMaterial, Double> blocks = new HashMap<>();

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			CompMaterial material = CompMaterial.valueOf(entry.getKey());
			double percentage = entry.getValue().getAsDouble();
			blocks.put(material, percentage);
		}

		return new BlockPalette(blocks);
	}

	public boolean contains(CompMaterial material) {
		return blockPercentages.containsKey(material);
	}

	public double getPercentage(CompMaterial material) {
		return blockPercentages.getOrDefault(material, 0.0);
	}

	public void setPercentage(CompMaterial material, double newPercentage) {
		this.blockPercentages.put(material, newPercentage);
	}

	public void addToPalette(CompMaterial material, double percentage) {
		this.blockPercentages.put(material, percentage);
	}

	public void removeFromPalette(CompMaterial material) {
		this.blockPercentages.remove(material);
	}

	public Set<CompMaterial> getMaterials() {
		return this.blockPercentages.keySet();
	}

	public double getTotalPercentage() {
		return this.blockPercentages.values().stream().mapToDouble(Double::valueOf).sum();
	}

	@Nonnull
	@Override
	public JsonElement serialize() {
		JsonBuilder.JsonObjectBuilder builder = JsonBuilder.object();

		for (Map.Entry<CompMaterial, Double> entry : blockPercentages.entrySet()) {
			builder.addIfAbsent(entry.getKey().name(), entry.getValue());
		}
		return builder.build();
	}

	public boolean isEmpty() {
		return this.blockPercentages.isEmpty() || this.getTotalPercentage() <= 0.0;
	}
}
