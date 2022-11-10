package dev.drawethree.ultraprisoncore.nicknames.repo;

import dev.drawethree.ultraprisoncore.interfaces.UPCRepository;
import org.bukkit.OfflinePlayer;

public interface NicknameRepository extends UPCRepository {

	void updatePlayerNickname(OfflinePlayer player);

}
