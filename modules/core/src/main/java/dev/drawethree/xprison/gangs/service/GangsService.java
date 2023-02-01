package dev.drawethree.xprison.gangs.service;

import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gangs.model.GangInvitation;

import java.util.List;

public interface GangsService {

	void updateGang(Gang g);

	void deleteGang(Gang g);

	void createGang(Gang g);

	List<Gang> getAllGangs();

	List<GangInvitation> getGangInvitations(Gang gang);

	void createGangInvitation(GangInvitation gangInvitation);

	void deleteGangInvitation(GangInvitation gangInvitation);
}
