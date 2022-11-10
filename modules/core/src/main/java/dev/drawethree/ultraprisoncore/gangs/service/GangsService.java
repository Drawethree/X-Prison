package dev.drawethree.ultraprisoncore.gangs.service;

import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.model.GangInvitation;

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
