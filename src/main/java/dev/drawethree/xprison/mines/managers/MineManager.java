package dev.drawethree.xprison.mines.managers;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.api.events.MineCreateEvent;
import dev.drawethree.xprison.mines.api.events.MineDeleteEvent;
import dev.drawethree.xprison.mines.api.events.MinePostResetEvent;
import dev.drawethree.xprison.mines.api.events.MinePreResetEvent;
import dev.drawethree.xprison.mines.gui.MinePanelGUI;
import dev.drawethree.xprison.mines.model.mine.HologramType;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.mines.model.mine.MineSelection;
import dev.drawethree.xprison.mines.model.mine.loader.MineFileLoader;
import dev.drawethree.xprison.mines.model.mine.loader.MineLoader;
import dev.drawethree.xprison.mines.model.mine.saver.MineFileSaver;
import dev.drawethree.xprison.mines.model.mine.saver.MineSaver;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.location.LocationUtils;
import dev.drawethree.xprison.utils.misc.TimeUtil;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MineManager {

	public static final ItemStack SELECTION_TOOL = ItemStackBuilder.of(Material.STICK).enchant(Enchantment.DURABILITY).name("&eMine Selection Tool").lore("&aRight-Click &fto set &aPosition 1 &7(MIN)", "&aLeft-Click &fto set &aPosition 2 &7(MAX)").build();

	private final MineLoader mineLoader;
	private final MineSaver mineSaver;

	@Getter
	private final XPrisonMines plugin;

	private final Map<UUID, MineSelection> mineSelections;
	private Map<String, Mine> mines;

	private List<String> hologramBlocksLeftLines;
	private List<String> hologramBlocksMinedLines;
	private List<String> hologramTimedResetLines;

	private File minesDirectory;

	public MineManager(XPrisonMines plugin) {
		this.plugin = plugin;
		this.mineSelections = new HashMap<>();
		this.hologramBlocksLeftLines = this.plugin.getConfig().get().getStringList("holograms.blocks_left");
		this.hologramBlocksMinedLines = this.plugin.getConfig().get().getStringList("holograms.blocks_mined");
		this.hologramTimedResetLines = this.plugin.getConfig().get().getStringList("holograms.timed_reset");
		this.mineLoader = new MineFileLoader(this);
		this.mineSaver = new MineFileSaver(this);
	}

	public void enable() {
		this.setupMinesDirectory();
		this.loadMines();
	}

	public List<String> getHologramBlocksLeftLines(Mine mine) {
		List<String> copy = new ArrayList<>();
		for (String s : this.hologramBlocksLeftLines) {
			copy.add(s.replace("%mine%", mine.getName()).replace("%blocks%", String.format("%,.2f", (double) mine.getCurrentBlocks() / mine.getTotalBlocks() * 100.0D)));
		}
		return copy;
	}


	public List<String> getHologramBlocksMinedLines(Mine mine) {
		List<String> copy = new ArrayList<>();
		for (String s : this.hologramBlocksMinedLines) {
			copy.add(s.replace("%mine%", mine.getName()).replace("%blocks%", String.format("%,d", mine.getTotalBlocks() - mine.getCurrentBlocks())));
		}
		return copy;
	}

	private void setupMinesDirectory() {
		File directory = new File(this.plugin.getCore().getDataFolder().getPath() + "/mines/");

		if (!directory.exists()) {
			directory.mkdir();
			this.plugin.getCore().getLogger().info("Created /mines directory");
		}
		this.minesDirectory = directory;
	}

	private void loadMines() {
		this.mines = new HashMap<>();
		File[] files = this.minesDirectory.listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			if (!file.getName().endsWith(".json")) {
				continue;
			}

			Mine mine = this.mineLoader.load(file);

			if (mine == null) {
				continue;
			}

			this.mines.put(mine.getName(), mine);
			this.plugin.getCore().getLogger().info("Loaded Mine " + mine.getName());

			double ratio = (double) mine.getCurrentBlocks() / mine.getTotalBlocks() * 100.0;

			if (ratio <= mine.getResetPercentage() && !mine.isResetting()) {
				this.resetMine(mine);
			}
		}
	}

	private void saveMines() {
		this.getMines().forEach(this.mineSaver::save);
	}

	public void selectPosition(Player player, int position, Position pos) {

		MineSelection selection;

		if (!mineSelections.containsKey(player.getUniqueId())) {
			this.mineSelections.put(player.getUniqueId(), new MineSelection());
		}

		selection = this.mineSelections.get(player.getUniqueId());

		switch (position) {
			case 1:
				selection.setPos1(pos);
				break;
			case 2:
				selection.setPos2(pos);
				break;
		}

		if (selection.isValid()) {
			PlayerUtils.sendMessage(player, this.plugin.getMessage("selection_valid"));
		}

		PlayerUtils.sendMessage(player, this.plugin.getMessage("selection_point_set").replace("%position%", String.valueOf(position)).replace("%location%", LocationUtils.toXYZW(pos.toLocation())));
	}

	public MineSelection getMineSelection(Player player) {
		return this.mineSelections.get(player.getUniqueId());
	}

	public boolean createMine(Player creator, String name) {
		MineSelection selection = this.getMineSelection(creator);

		if (selection == null || !selection.isValid()) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("selection_invalid"));
			return false;
		}

		if (this.getMineByName(name) != null) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_exists"));
			return false;
		}

		Mine mine = new Mine(this, name, selection.toRegion());

		MineCreateEvent event = new MineCreateEvent(creator, mine);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("MineCreateEvent was cancelled.", this.plugin);
			return true;
		}

		this.mines.put(mine.getName(), mine);

		this.mineSaver.save(mine);

		PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_created").replace("%mine%", name));
		return true;
	}

	public boolean deleteMine(CommandSender sender, String name) {
		Mine mine = this.getMineByName(name);

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", name));
			return false;
		}

		return deleteMine(sender, mine);
	}

	public boolean deleteMine(CommandSender sender, Mine mine) {
		MineDeleteEvent event = new MineDeleteEvent(mine);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("MineDeleteEvent was cancelled.", this.plugin);
			return true;
		}

		if (mine.getFile() != null) {
			mine.getFile().delete();
		}

		mine.stopTicking();

		this.despawnHolograms(mine);

		this.mines.remove(mine.getName());

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_deleted").replace("%mine%", mine.getName()));
		return true;
	}

	public Mine getMineByName(String name) {
		return this.mines.get(name);
	}

	public Mine getMineAtLocation(Location loc) {
		for (Mine mine : this.mines.values()) {
			if (mine.isInMine(loc)) {
				return mine;
			}
		}
		return null;
	}

	public void disable() {
		this.saveMines();
		this.getMines().forEach(this::despawnHolograms);
	}

	public Collection<Mine> getMines() {
		return this.mines.values();
	}

	public boolean teleportToMine(Player player, Mine mine) {

		if (mine.getTeleportLocation() == null) {
			PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_no_teleport_location").replace("%mine%", mine.getName()));
			return false;
		}

		player.teleport(mine.getTeleportLocation().toLocation());
		PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_teleport").replace("%mine%", mine.getName()));
		return true;
	}

	public void openMinesListGUI(Player player) {
		PaginatedGuiBuilder builder = PaginatedGuiBuilder.create();

		builder.lines(6);
		builder.title("Mine List");
		builder.nextPageSlot(53);
		builder.previousPageSlot(44);
		builder.nextPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&aNext Page").lore("&7Click to see next page.").build());
		builder.previousPageItem((pageInfo) -> ItemStackBuilder.of(Material.ARROW).name("&aPrevious Page").lore("&7Click to see previous page.").build());

		builder.build(player, paginatedGui -> {
			List<Item> items = new ArrayList<>();
			List<Mine> mines = this.mines.values().stream().sorted(Comparator.comparing(Mine::getName)).collect(Collectors.toList());
			for (Mine mine : mines) {
				items.add(ItemStackBuilder.of(Material.STONE).name(mine.getName()).lore("&aLeft-Click &7to open Mine Panel for this mine.", "&aRight-Click &7to teleport to this mine.").build(() -> {
					this.teleportToMine(player, mine);
				}, () -> {
					new MinePanelGUI(mine, player).open();
				}));
			}
			return items;
		}).open();

	}

	public boolean setTeleportLocation(Player player, Mine mine) {
		mine.setTeleportLocation(Point.of(player.getLocation()));
		PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_teleport_set").replace("%mine%", mine.getName()));
		return true;
	}

	public boolean giveTool(Player sender) {
		sender.getInventory().addItem(SELECTION_TOOL);
		PlayerUtils.sendMessage(sender, this.plugin.getMessage("selection_tool_given"));
		return true;
	}

	public void reload() {
		this.hologramBlocksLeftLines = this.plugin.getConfig().get().getStringList("holograms.blocks_left");
		this.hologramBlocksMinedLines = this.plugin.getConfig().get().getStringList("holograms.blocks_mined");
		this.hologramTimedResetLines = this.plugin.getConfig().get().getStringList("holograms.timed_reset");
	}

	public boolean addMineFromMigration(Mine migrated) {

		if (migrated == null) {
			return false;
		}

		if (this.mines.containsKey(migrated.getName())) {
			return false;
		}

		this.mines.put(migrated.getName(), migrated);

		return true;
	}

	public void resetAllMines() {
		this.getMines().forEach(this::resetMine);
	}

	public List<String> getHologramTimedResetLines(Mine mine) {
		List<String> copy = new ArrayList<>();
		for (String s : this.hologramTimedResetLines) {
			copy.add(s.replace("%mine%", mine.getName()).replace("%time%", TimeUtil.getTime(mine.getSecondsToNextReset())));
		}
		return copy;
	}

	public void resetMine(Mine mine) {

		if (mine == null) {
			return;
		}

		if (mine.isResetting()) {
			return;
		}

		MinePreResetEvent preResetEvent = new MinePreResetEvent(mine);

		Events.call(preResetEvent);

		if (preResetEvent.isCancelled()) {
			this.getPlugin().getCore().debug("MinePreResetEvent was cancelled.", this.getPlugin());
			return;
		}

		mine.setResetting(true);

		if (mine.isBroadcastReset()) {
			mine.getPlayersInMine().forEach(player -> PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_resetting").replace("%mine%", mine.getName())));
		}

		Schedulers.sync().runLater(() -> {

			if (mine.getTeleportLocation() != null) {
				mine.getPlayersInMine().forEach(player -> player.teleport(mine.getTeleportLocation().toLocation()));
			}

			mine.getResetType().reset(mine, mine.getBlockPalette());

			if (mine.isBroadcastReset()) {
				mine.getPlayersInMine().forEach(player -> PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_reset").replace("%mine%", mine.getName())));
			}

			mine.setNextResetDate(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(mine.getResetTime())));

			mine.setResetting(false);

			MinePostResetEvent postResetEvent = new MinePostResetEvent(mine);

			Events.call(postResetEvent);
		}, 5, TimeUnit.SECONDS);

	}

	public void giveMineEffects(Mine mine, Player player) {
		for (PotionEffectType type : mine.getMineEffects().keySet()) {
			player.removePotionEffect(type);
			player.addPotionEffect(mine.getEffect(type));
		}
	}


	public void createHologram(Mine mine, HologramType type, Player player) {
		switch (type) {
			case BLOCKS_LEFT: {
				if (mine.getBlocksLeftHologram() == null) {
					mine.setBlocksLeftHologram(Hologram.create(Position.of(player.getLocation()), this.getHologramBlocksLeftLines(mine)));
					mine.getBlocksLeftHologram().spawn();
				} else {
					mine.getBlocksLeftHologram().despawn();
					mine.getBlocksLeftHologram().updatePosition(Position.of(player.getLocation()));
					mine.getBlocksLeftHologram().spawn();
				}
				break;
			}
			case BLOCKS_MINED: {
				if (mine.getBlocksMinedHologram() == null) {
					mine.setBlocksMinedHologram(Hologram.create(Position.of(player.getLocation()), this.getHologramBlocksMinedLines(mine)));
					mine.getBlocksMinedHologram().spawn();
				} else {
					mine.getBlocksMinedHologram().despawn();
					mine.getBlocksMinedHologram().updatePosition(Position.of(player.getLocation()));
					mine.getBlocksMinedHologram().spawn();
				}
				break;
			}
			case TIMED_RESET: {
				if (mine.getTimedResetHologram() == null) {
					mine.setTimedResetHologram(Hologram.create(Position.of(player.getLocation()), this.getHologramTimedResetLines(mine)));
					mine.getTimedResetHologram().spawn();
				} else {
					mine.getTimedResetHologram().despawn();
					mine.getTimedResetHologram().updatePosition(Position.of(player.getLocation()));
					mine.getTimedResetHologram().spawn();
				}
				break;
			}
		}
		PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_hologram_create").replace("%type%", type.name()).replace("%mine%", mine.getName()));
	}

	public void deleteHologram(Mine mine, HologramType type, Player player) {
		switch (type) {
			case BLOCKS_LEFT: {
				if (mine.getBlocksLeftHologram() != null) {
					mine.getBlocksLeftHologram().despawn();
					mine.setBlocksLeftHologram(null);
				}
				break;
			}
			case BLOCKS_MINED: {
				if (mine.getBlocksMinedHologram() != null) {
					mine.getBlocksMinedHologram().despawn();
					mine.setBlocksMinedHologram(null);
				}
				break;
			}
			case TIMED_RESET: {
				if (mine.getTimedResetHologram() != null) {
					mine.getTimedResetHologram().despawn();
					mine.setTimedResetHologram(null);
				}
				break;
			}
		}
		PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_hologram_delete").replace("%type%", type.name()).replace("%mine%", mine.getName()));
	}

	private void despawnHolograms(Mine mine) {

		if (mine.getBlocksMinedHologram() != null) {
			mine.getBlocksMinedHologram().despawn();
		}

		if (mine.getBlocksLeftHologram() != null) {
			mine.getBlocksLeftHologram().despawn();
		}

		if (mine.getTimedResetHologram() != null) {
			mine.getTimedResetHologram().despawn();
		}
	}

	public boolean renameMine(Player sender, String oldMineName, String newName) {

		Mine mine = this.getMineByName(oldMineName);

		if (mine == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists"));
			return false;
		}

		if (mine.getFile() != null) {
			mine.getFile().delete();
		}

		this.mines.remove(oldMineName);
		mine.setName(newName);
		this.mines.put(newName, mine);

		this.mineSaver.save(mine);

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_renamed").replace("%mine%", oldMineName).replace("%new_name%", newName));
		return true;
	}

	public boolean redefineMine(Player creator, String name) {
		MineSelection selection = this.getMineSelection(creator);

		if (selection == null || !selection.isValid()) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("selection_invalid"));
			return false;
		}

		Mine mine = this.getMineByName(name);
		if (mine == null) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_not_exists"));
			return false;
		}

		Region region = selection.toRegion();
		mine.setMineRegion(region);

		PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_redefined").replace("%mine%", name));
		return true;
	}

	public MineLoader getMineLoader() {
		return mineLoader;
	}

	public MineSaver getMineSaver() {
		return mineSaver;
	}
}
