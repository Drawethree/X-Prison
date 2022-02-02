package me.drawethree.ultraprisoncore.history.manager;

import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import me.drawethree.ultraprisoncore.history.model.HistoryLine;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.item.ItemStackBuilder;
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

	private final UltraPrisonHistory plugin;

	public HistoryManager(UltraPrisonHistory plugin) {
		this.plugin = plugin;
	}

	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		return this.plugin.getCore().getPluginDatabase().getPlayerHistory(player);
	}

	public void createPlayerHistoryLine(OfflinePlayer player, UltraPrisonModule module, String context) {
		HistoryLine history = createHistoryLineObject(player, module, context);
		this.plugin.getCore().getPluginDatabase().addHistoryLine(player, history);
	}

	public void clearPlayerHistory(OfflinePlayer target) {
		this.plugin.getCore().getPluginDatabase().clearHistory(target);
	}

	public void openPlayerHistoryGui(Player sender, OfflinePlayer target, Predicate<HistoryLine> filter) {
		PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();
		builder.lines(6);
		builder.title("History: " + target.getName());
		builder.nextPageSlot(53);
		builder.previousPageSlot(45);
		builder.build(sender, gui -> {
			Stream<HistoryLine> historyLinesStream = getPlayerHistory(target).stream();
			if (filter != null) {
				historyLinesStream = historyLinesStream.filter(filter);
			}
			List<HistoryLine> historyLines = historyLinesStream.sorted(Comparator.comparing(HistoryLine::getCreatedAt).reversed()).collect(Collectors.toList());
			if (historyLines.isEmpty()) {
				return Collections.singletonList(getEmptyHistoryItem());
			} else {
				return historyLines.stream().map(this::createHistoryLineGuiItem).collect(Collectors.toList());
			}
		}).open();
	}

	private Item getEmptyHistoryItem() {
		return ItemStackBuilder.of(CompMaterial.BARRIER.toItem()).name("&4&lNo History").lore("&cNo history is present for this player.").buildItem().build();
	}

	private HistoryLine createHistoryLineObject(OfflinePlayer player, UltraPrisonModule module, String context) {
		Validate.notNull(player, "Player cannot be null!");
		Validate.notNull(module, "Module cannot be null!");
		Validate.notNull(context, "Context cannot be null!");

		HistoryLine history = new HistoryLine();
		history.setCreatedAt(new Date());
		history.setContext(context);
		history.setPlayerUuid(player.getUniqueId());
		history.setModule(module.getName());
		history.setUuid(UUID.randomUUID());
		return history;
	}

	private Item createHistoryLineGuiItem(HistoryLine line) {
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
			case UltraPrisonTokens.MODULE_NAME:
			case UltraPrisonMultipliers.MODULE_NAME:
				return SkullUtils.COIN_SKULL.clone();
			case UltraPrisonGems.MODULE_NAME:
				return ItemStackBuilder.of(CompMaterial.EMERALD.toItem()).build();
			case UltraPrisonGangs.MODULE_NAME:
				return SkullUtils.GANG_SKULL.clone();
			case UltraPrisonPrestiges
					.MODULE_NAME:
				return SkullUtils.DIAMOND_P_SKULL.clone();
			case UltraPrisonRanks
					.MODULE_NAME:
				return SkullUtils.DIAMOND_R_SKULL.clone();
			case UltraPrisonAutoMiner
					.MODULE_NAME:
				return ItemStackBuilder.of(CompMaterial.DIAMOND_PICKAXE.toItem()).build();
			default:
				return ItemStackBuilder.of(CompMaterial.BOOK.toItem()).build();
		}

	}
}
