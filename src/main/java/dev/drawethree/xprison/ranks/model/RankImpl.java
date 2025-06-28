package dev.drawethree.xprison.ranks.model;

import dev.drawethree.xprison.api.ranks.model.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RankImpl implements Rank {

	private int id;
	private double cost;
	private String prefix;
	private List<String> commandsToExecute;

}
