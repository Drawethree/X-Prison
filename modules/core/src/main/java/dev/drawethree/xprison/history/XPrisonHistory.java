package dev.drawethree.xprison.history;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.autominer.api.events.PlayerAutoMinerTimeModifyEvent;
import dev.drawethree.xprison.autominer.api.events.PlayerAutomineEvent;
import dev.drawethree.xprison.gangs.api.events.GangCreateEvent;
import dev.drawethree.xprison.gangs.api.events.GangDisbandEvent;
import dev.drawethree.xprison.gangs.api.events.GangJoinEvent;
import dev.drawethree.xprison.gangs.api.events.GangLeaveEvent;
import dev.drawethree.xprison.gems.api.events.PlayerGemsLostEvent;
import dev.drawethree.xprison.gems.api.events.PlayerGemsReceiveEvent;
import dev.drawethree.xprison.history.api.XPrisonHistoryAPI;
import dev.drawethree.xprison.history.api.XPrisonHistoryAPIImpl;
import dev.drawethree.xprison.history.gui.PlayerHistoryGUI;
import dev.drawethree.xprison.history.manager.HistoryManager;
import dev.drawethree.xprison.history.repo.HistoryRepository;
import dev.drawethree.xprison.history.repo.impl.HistoryRepositoryImpl;
import dev.drawethree.xprison.history.service.HistoryService;
import dev.drawethree.xprison.history.service.impl.HistoryServiceImpl;
import dev.drawethree.xprison.multipliers.api.events.PlayerMultiplierReceiveEvent;
import dev.drawethree.xprison.prestiges.api.events.PlayerPrestigeEvent;
import dev.drawethree.xprison.ranks.api.events.PlayerRankUpEvent;
import dev.drawethree.xprison.tokens.api.events.PlayerTokensLostEvent;
import dev.drawethree.xprison.tokens.api.events.PlayerTokensReceiveEvent;
import dev.drawethree.xprison.utils.misc.TimeUtil;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

public final class XPrisonHistory implements XPrisonModule {

	private static final String MODULE_NAME = "History";
	@Getter
	private final XPrison core;

	@Getter
	private HistoryManager historyManager;

	@Getter
	private HistoryRepository historyRepository;

	@Getter
	private HistoryService historyService;

	@Getter
	private XPrisonHistoryAPI api;

	private boolean enabled;

	public XPrisonHistory(XPrison core) {
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
		this.api = new XPrisonHistoryAPIImpl(this);
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
		Events.subscribe(PlayerAutoMinerTimeModifyEvent.class, EventPriority.MONITOR)
				.handler(e -> {
					this.historyManager.createPlayerHistoryLine(e.getPlayer(), this.core.getAutoMiner(), String.format("Received %,d %s of Auto-Miner time.", e.getDuration(), e.getTimeUnit().name()));
				}).bindWith(this.core);
	}

	private void registerCommands() {
		Commands.create()
				.assertPermission("xprison.history")
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
	public void resetPlayerData() {
		this.historyRepository.clearTableData();
	}
}
