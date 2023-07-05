package dev.drawethree.xprison.pickaxelevels.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PickaxeLevel {

	private int level;
	private long blocksRequired;
	private String displayName;
	private List<String> rewards;
}
