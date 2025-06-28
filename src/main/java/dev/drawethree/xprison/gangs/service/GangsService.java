package dev.drawethree.xprison.gangs.service;

import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.gangs.model.GangInvitationImpl;

import java.util.List;

public interface GangsService {

	void updateGang(GangImpl g);

	void deleteGang(GangImpl g);

	void createGang(GangImpl g);

	List<GangImpl> getAllGangs();

	List<GangInvitationImpl> getGangInvitations(GangImpl gangImpl);

	void createGangInvitation(GangInvitationImpl gangInvitationImpl);

	void deleteGangInvitation(GangInvitationImpl gangInvitationImpl);
}
