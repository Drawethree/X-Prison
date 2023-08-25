package dev.drawethree.xprison.nicknames.repo;

import dev.drawethree.xprison.interfaces.UPCRepository;
import org.bukkit.OfflinePlayer;

public interface NicknameRepository extends UPCRepository {

	void updatePlayerNickname(OfflinePlayer player);

}
