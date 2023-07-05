package dev.drawethree.xprison.history.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryLine {

	private UUID uuid;
	private UUID playerUuid;
	private String module;
	private String context;
	private Date createdAt;
}
