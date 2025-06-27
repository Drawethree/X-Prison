package dev.drawethree.xprison.mines.model.mine;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockPalette implements GsonSerializable {

	private Map<XMaterial, Double> blockPercentages;

	public BlockPalette() {
		this.blockPercentages = new HashMap<>();
	}

	private BlockPalette(Map<XMaterial, Double> blockPercentages) {
		this.blockPercentages = blockPercentages;
	}

	public static BlockPalette deserialize(JsonElement element) {
		Preconditions.checkArgument(element.isJsonObject());
		JsonObject object = element.getAsJsonObject();

		Map<XMaterial, Double> blocks = new HashMap<>();

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			XMaterial material = XMaterial.valueOf(entry.getKey());
			double percentage = entry.getValue().getAsDouble();
			if (percentage <= 0.0) {
				continue;
			}
			blocks.put(material, percentage);
		}

		return new BlockPalette(blocks);
	}

	public boolean contains(XMaterial material) {
		return blockPercentages.containsKey(material);
	}

	public double getPercentage(XMaterial material) {
		return blockPercentages.getOrDefault(material, 0.0);
	}

	public void setPercentage(XMaterial material, double newPercentage) {
		if (newPercentage <= 0.0) {
			this.blockPercentages.remove(material);
		} else {
			this.blockPercentages.put(material, newPercentage);
		}
	}

	public void addToPalette(XMaterial material, double percentage) {
		this.blockPercentages.put(material, percentage);
	}

	public void removeFromPalette(XMaterial material) {
		this.blockPercentages.remove(material);
	}

	public Set<XMaterial> getMaterials() {
		return this.blockPercentages.keySet();
	}

	public Set<XMaterial> getValidMaterials() {
		return this.blockPercentages.keySet().stream().filter(material -> getPercentage(material) > 0.0).collect(Collectors.toSet());
	}

	public double getTotalPercentage() {
		return this.blockPercentages.values().stream().mapToDouble(Double::valueOf).sum();
	}

	@Nonnull
	@Override
	public JsonElement serialize() {
		JsonBuilder.JsonObjectBuilder builder = JsonBuilder.object();

		for (Map.Entry<XMaterial, Double> entry : blockPercentages.entrySet()) {
			builder.addIfAbsent(entry.getKey().name(), entry.getValue());
		}
		return builder.build();
	}

	public boolean isEmpty() {
		return this.blockPercentages.isEmpty() || this.getTotalPercentage() <= 0.0;
	}
}
