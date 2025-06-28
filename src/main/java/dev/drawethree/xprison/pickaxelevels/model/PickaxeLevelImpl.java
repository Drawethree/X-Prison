package dev.drawethree.xprison.pickaxelevels.model;

import dev.drawethree.xprison.api.pickaxelevels.model.PickaxeLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PickaxeLevelImpl implements PickaxeLevel {

	private int level;
	private long blocksRequired;
	private String displayName;
	private List<String> rewards;
}
