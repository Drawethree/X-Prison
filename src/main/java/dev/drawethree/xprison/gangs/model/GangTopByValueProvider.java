package dev.drawethree.xprison.gangs.model;

import dev.drawethree.xprison.gangs.managers.GangsManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class GangTopByValueProvider implements GangTopProvider {

	private final GangsManager manager;

	public GangTopByValueProvider(GangsManager manager) {
		this.manager = manager;
	}

	@Override
	public List<Gang> provide() {
		return getAllGangs().stream().sorted(Comparator.comparingLong(Gang::getValue).reversed()).collect(Collectors.toList());
	}

	private Collection<Gang> getAllGangs() {
		return manager.getAllGangs();
	}
}
