package dev.drawethree.xprison.history.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.api.XPrisonModule;
import dev.drawethree.xprison.api.history.model.HistoryLine;
import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.history.XPrisonHistory;
import dev.drawethree.xprison.history.model.HistoryLineImpl;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryManager {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

	private final XPrisonHistory plugin;

	public HistoryManager(XPrisonHistory plugin) {
		this.plugin = plugin;
	}

	public List<HistoryLineImpl> getPlayerHistoryImpl(OfflinePlayer player) {
		return this.plugin.getHistoryService().getPlayerHistory(player);
	}

	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		return Collections.unmodifiableList(new ArrayList<>(getPlayerHistoryImpl(player)));
	}

	public HistoryLine createPlayerHistoryLine(OfflinePlayer player, XPrisonModule module, String context) {
		HistoryLineImpl history = createHistoryLineObject(player, module, context);
		this.plugin.getHistoryService().createHistoryLine(player, history);
		return history;
	}

	public void clearPlayerHistory(OfflinePlayer target) {
		this.plugin.getHistoryService().deleteHistory(target);
	}

	public void openPlayerHistoryGui(Player sender, OfflinePlayer target, Predicate<HistoryLineImpl> filter) {
		PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();
		builder.lines(6);
		builder.title("History: " + target.getName());
		builder.nextPageSlot(53);
		builder.previousPageSlot(45);
		builder.build(sender, gui -> {
			Stream<HistoryLineImpl> historyLinesStream = getPlayerHistoryImpl(target).stream();
			if (filter != null) {
				historyLinesStream = historyLinesStream.filter(filter);
			}
			List<HistoryLineImpl> historyLineImpls = historyLinesStream.sorted(Comparator.comparing(HistoryLineImpl::getCreatedAt).reversed()).collect(Collectors.toList());
			if (historyLineImpls.isEmpty()) {
				return Collections.singletonList(getEmptyHistoryItem());
			} else {
				return historyLineImpls.stream().map(this::createHistoryLineGuiItem).collect(Collectors.toList());
			}
		}).open();
	}

	private Item getEmptyHistoryItem() {
		return ItemStackBuilder.of(XMaterial.BARRIER.parseItem()).name("&4&lNo History").lore("&cNo history is present for this player.").buildItem().build();
	}

	private HistoryLineImpl createHistoryLineObject(OfflinePlayer player, XPrisonModule module, String context) {
		Validate.notNull(player, "Player cannot be null!");
		Validate.notNull(module, "Module cannot be null!");
		Validate.notNull(context, "Context cannot be null!");

		HistoryLineImpl history = new HistoryLineImpl();
		history.setCreatedAt(new Date());
		history.setContext(context);
		history.setPlayerUuid(player.getUniqueId());
		history.setModule(module.getName());
		history.setUuid(UUID.randomUUID());
		return history;
	}

	private Item createHistoryLineGuiItem(HistoryLineImpl line) {
		return ItemStackBuilder
				.of(getIconForModule(line.getModule()))
				.name("&e" + line.getModule())
				.lore(
						" ",
						"&7Module: &e" + line.getModule(),
						"&7Date: &e" + DATE_FORMAT.format(line.getCreatedAt()),
						"&7Context:",
						"&f" + line.getContext(),
						" ").buildItem().build();
	}

	private ItemStack getIconForModule(String moduleName) {
		switch (moduleName) {
			case XPrisonTokens.MODULE_NAME:
			case XPrisonMultipliers.MODULE_NAME:
				return SkullUtils.COIN_SKULL.clone();
			case XPrisonGems.MODULE_NAME:
				return ItemStackBuilder.of(XMaterial.EMERALD.parseItem()).build();
			case XPrisonGangs.MODULE_NAME:
				return SkullUtils.GANG_SKULL.clone();
			case XPrisonPrestiges
					.MODULE_NAME:
				return SkullUtils.DIAMOND_P_SKULL.clone();
			case XPrisonRanks
					.MODULE_NAME:
				return SkullUtils.DIAMOND_R_SKULL.clone();
			case XPrisonAutoMiner
					.MODULE_NAME:
				return ItemStackBuilder.of(XMaterial.DIAMOND_PICKAXE.parseItem()).build();
			default:
				return ItemStackBuilder.of(XMaterial.BOOK.parseItem()).build();
		}

	}
}
