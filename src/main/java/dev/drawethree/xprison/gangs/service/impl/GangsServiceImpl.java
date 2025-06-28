package dev.drawethree.xprison.gangs.service.impl;

import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.gangs.model.GangInvitationImpl;
import dev.drawethree.xprison.gangs.repo.GangsRepository;
import dev.drawethree.xprison.gangs.service.GangsService;

import java.util.List;

public class GangsServiceImpl implements GangsService {

	private final GangsRepository repository;

	public GangsServiceImpl(GangsRepository repository) {
		this.repository = repository;
	}

	@Override
	public void updateGang(GangImpl g) {
		repository.updateGang(g);
	}

	@Override
	public void deleteGang(GangImpl g) {
		repository.deleteGang(g);
	}

	@Override
	public void createGang(GangImpl g) {
		repository.createGang(g);
	}

	@Override
	public List<GangImpl> getAllGangs() {
		return repository.getAllGangs();
	}

	@Override
	public List<GangInvitationImpl> getGangInvitations(GangImpl gangImpl) {
		return repository.getGangInvitations(gangImpl);
	}

	@Override
	public void createGangInvitation(GangInvitationImpl gangInvitationImpl) {
		repository.createGangInvitation(gangInvitationImpl);
	}

	@Override
	public void deleteGangInvitation(GangInvitationImpl gangInvitationImpl) {
		repository.deleteGangInvitation(gangInvitationImpl);
	}
}
