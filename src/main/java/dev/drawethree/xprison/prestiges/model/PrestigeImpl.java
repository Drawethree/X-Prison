package dev.drawethree.xprison.prestiges.model;

import dev.drawethree.xprison.api.prestiges.model.Prestige;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PrestigeImpl implements Prestige {

	private final long id;
	private final double cost;
	private final String prefix;
	private final List<String> commandsToExecute;
}
