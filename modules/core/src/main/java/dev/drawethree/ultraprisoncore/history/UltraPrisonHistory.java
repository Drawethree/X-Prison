package dev.drawethree.ultraprisoncore.history;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutoMinerTimeReceiveEvent;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutomineEvent;
import dev.drawethree.ultraprisoncore.gangs.api.events.GangCreateEvent;
import dev.drawethree.ultraprisoncore.gangs.api.events.GangDisbandEvent;
import dev.drawethree.ultraprisoncore.gangs.api.events.GangJoinEvent;
import dev.drawethree.ultraprisoncore.gangs.api.events.GangLeaveEvent;
import dev.drawethree.ultraprisoncore.gems.api.events.PlayerGemsLostEvent;
import dev.drawethree.ultraprisoncore.gems.api.events.PlayerGemsReceiveEvent;
import dev.drawethree.ultraprisoncore.history.api.UltraPrisonHistoryAPI;
import dev.drawethree.ultraprisoncore.history.api.UltraPrisonHistoryAPIImpl;
import dev.drawethree.ultraprisoncore.history.gui.PlayerHistoryGUI;
import dev.drawethree.ultraprisoncore.history.manager.HistoryManager;
import dev.drawethree.ultraprisoncore.history.repo.HistoryRepository;
import dev.drawethree.ultraprisoncore.history.repo.impl.HistoryRepositoryImpl;
import dev.drawethree.ultraprisoncore.history.service.HistoryService;
import dev.drawethree.ultraprisoncore.history.service.impl.HistoryServiceImpl;
import dev.drawethree.ultraprisoncore.multipliers.api.events.PlayerMultiplierReceiveEvent;
import dev.drawethree.ultraprisoncore.prestiges.api.events.PlayerPrestigeEvent;
import dev.drawethree.ultraprisoncore.ranks.api.events.PlayerRankUpEvent;
import dev.drawethree.ultraprisoncore.tokens.api.events.PlayerTokensLostEvent;
import dev.drawethree.ultraprisoncore.tokens.api.events.PlayerTokensReceiveEvent;
import dev.drawethree.ultraprisoncore.utils.misc.TimeUtil;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

public final class UltraPrisonHistory implements UltraPrisonModule {

	private static final String MODULE_NAME = "History";
	@Getter
	private final UltraPrisonCore core;

	@Getter
	private HistoryManager historyManager;

	@Getter
	private HistoryRepository historyRepository;

	@Getter
	private HistoryService historyService;

	@Getter
	private UltraPrisonHistoryAPI api;

	private boolean enabled;

	public UltraPrisonHistory(UltraPrisonCore core) {
		this.core = core;
		this.enabled = false;
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.historyRepository = new HistoryRepositoryImpl(this.core.getPluginDatabase());
		this.historyRepository.createTables();
		this.historyService = new HistoryServiceImpl(this.historyRepository);
		this.historyManager = new HistoryManager(this);
		this.api = new UltraPrisonHistoryAPIImpl(this);
		this.registerCommands();
		this.registerEvents();
	}

	private void registerEvents() {
		Events.subscribe(PlayerGemsReceiveEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getGems(), String.format("&a&l+%,d GEMS &f(%s). &7Current Gems: &e%,d", e.getAmount(), e.getCause().name(), this.core.getGems().getApi().getPlayerGems(e.getPlayer())));
				}).bindWith(this.core);
		Events.subscribe(PlayerGemsLostEvent.class, EventPriority.MONITOR)
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getGems(), String.format("&c&l-%,d GEMS &f(%s). &7Current Gems: &e%,d", e.getAmount(), e.getCause().name(), this.core.getTokens().getApi().getPlayerTokens(e.getPlayer())));
				}).bindWith(this.core);
		Events.subscribe(PlayerTokensReceiveEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getTokens(), String.format("&a&l+%,d TOKENS &f(%s).&7Current Tokens: &e%,d", e.getAmount(), e.getCause().name(), this.core.getTokens().getApi().getPlayerTokens(e.getPlayer())));
				}).bindWith(this.core);
		Events.subscribe(PlayerTokensLostEvent.class, EventPriority.MONITOR)
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getTokens(), String.format("&c&l-%,d TOKENS &f(%s).&7Current Tokens: &e%,d", e.getAmount(), e.getCause().name(), this.core.getTokens().getApi().getPlayerTokens(e.getPlayer())));
				}).bindWith(this.core);
		Events.subscribe(PlayerRankUpEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getRanks(), String.format("Rank Up: %s&r -> %s", e.getOldRank().getPrefix(), e.getNewRank().getPrefix()));
				}).bindWith(this.core);
		Events.subscribe(PlayerPrestigeEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getPrestiges(), String.format("Prestige Up:  %s&r -> %s", e.getOldPrestige().getPrefix(), e.getNewPrestige().getPrefix()));
				}).bindWith(this.core);
		Events.subscribe(GangLeaveEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getGangs(), String.format("Left Gang: &e%s", e.getGang().getName()));
				}).bindWith(this.core);
		Events.subscribe(GangJoinEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getGangs(), String.format("Joined Gang: &e%s", e.getGang().getName()));
				}).bindWith(this.core);
		Events.subscribe(GangCreateEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					if (e.getCreator() instanceof Player) {
						this.historyManager.createPlayerHistoryLine((OfflinePlayer) e.getCreator(), this.core.getGangs(), String.format("Created Gang: &e%s", e.getGang().getName()));
					}
				}).bindWith(this.core);
		Events.subscribe(GangDisbandEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getGang().getOwnerOffline(), this.core.getGangs(), String.format("Disbanded Gang: &e%s", e.getGang().getName()));
				}).bindWith(this.core);
		Events.subscribe(PlayerMultiplierReceiveEvent.class, EventPriority.MONITOR)
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getMultipliers(), String.format("Received x%,.2f %s Multiplier for %,d %s", e.getMultiplier(), e.getType(), e.getDuration(), e.getTimeUnit().name()));
				}).bindWith(this.core);
		Events.subscribe(PlayerAutomineEvent.class, EventPriority.MONITOR)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getAutoMiner(), String.format("Player is Auto-Mining. Time left: %s", TimeUtil.getTime(e.getTimeLeft() - 1)));
				}).bindWith(this.core);
		Events.subscribe(PlayerAutoMinerTimeReceiveEvent.class, EventPriority.MONITOR)
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getAutoMiner(), String.format("Received %,d %s of Auto-Miner time.", e.getDuration(), e.getTimeUnit().name()));
				}).bindWith(this.core);
	}

	private void registerCommands() {
		Commands.create()
				.assertPermission("ultraprison.history")
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() != 1) {
						PlayerUtils.sendMessage(c.sender(),"&c/history <player>");
						return;
					}
					OfflinePlayer target = c.arg(0).parseOrFail(OfflinePlayer.class);
					new PlayerHistoryGUI(c.sender(), target, this).open();
				}).registerAndBind(this.core, "history");
	}

	@Override
	public void disable() {
		this.enabled = false;
	}

	@Override
	public void reload() {
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}

	@Override
	public boolean resetAllData() {
		this.historyRepository.clearTableData();
		return true;
	}
}
