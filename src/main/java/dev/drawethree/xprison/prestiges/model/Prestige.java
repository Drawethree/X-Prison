package dev.drawethree.xprison.prestiges.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Prestige {

	private final long id;
	private final double cost;
	private final String prefix;
	private final List<String> commandsToExecute;
}
