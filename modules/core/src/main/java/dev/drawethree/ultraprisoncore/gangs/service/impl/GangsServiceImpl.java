package dev.drawethree.ultraprisoncore.gangs.service.impl;

import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.model.GangInvitation;
import dev.drawethree.ultraprisoncore.gangs.repo.GangsRepository;
import dev.drawethree.ultraprisoncore.gangs.service.GangsService;

import java.util.List;

public class GangsServiceImpl implements GangsService {

	private final GangsRepository repository;

	public GangsServiceImpl(GangsRepository repository) {
		this.repository = repository;
	}

	@Override
	public void updateGang(Gang g) {
		repository.updateGang(g);
	}

	@Override
	public void deleteGang(Gang g) {
		repository.deleteGang(g);
	}

	@Override
	public void createGang(Gang g) {
		repository.createGang(g);
	}

	@Override
	public List<Gang> getAllGangs() {
		return repository.getAllGangs();
	}

	@Override
	public List<GangInvitation> getGangInvitations(Gang gang) {
		return repository.getGangInvitations(gang);
	}

	@Override
	public void createGangInvitation(GangInvitation gangInvitation) {
		repository.createGangInvitation(gangInvitation);
	}

	@Override
	public void deleteGangInvitation(GangInvitation gangInvitation) {
		repository.deleteGangInvitation(gangInvitation);
	}
}
