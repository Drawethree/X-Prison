package dev.drawethree.ultraprisoncore.history.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryLine {

	private UUID uuid;
	private UUID playerUuid;
	private String module;
	private String context;
	private Date createdAt;

}
