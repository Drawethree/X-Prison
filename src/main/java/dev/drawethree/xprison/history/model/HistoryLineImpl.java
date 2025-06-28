package dev.drawethree.xprison.history.model;

import dev.drawethree.xprison.api.history.model.HistoryLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryLineImpl implements HistoryLine {

	private UUID uuid;
	private UUID playerUuid;
	private String module;
	private String context;
	private Date createdAt;

	@Override
	public OfflinePlayer getOfflinePlayer() {
		return Players.getOfflineNullable(playerUuid);
	}
}
