package dev.drawethree.xprison.ranks.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Rank {

	private int id;
	private double cost;
	private String prefix;
	private List<String> commandsToExecute;

}
