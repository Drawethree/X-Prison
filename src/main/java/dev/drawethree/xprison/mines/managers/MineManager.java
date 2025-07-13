package dev.drawethree.xprison.mines.managers;

import com.cryptomorin.xseries.XEnchantment;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.gui.MinePanelGUI;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.mines.model.mine.MineSelectionImpl;
import dev.drawethree.xprison.mines.model.mine.loader.MineFileLoader;
import dev.drawethree.xprison.mines.model.mine.loader.MineLoader;
import dev.drawethree.xprison.mines.model.mine.saver.MineFileSaver;
import dev.drawethree.xprison.mines.model.mine.saver.MineSaver;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.location.LocationUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class MineManager {

	public static final ItemStack SELECTION_TOOL = ItemStackBuilder.of(Material.STICK).enchant(XEnchantment.UNBREAKING.get()).name("&eMine Selection Tool").lore("&aRight-Click &fto set &aPosition 1 &7(MIN)", "&aLeft-Click &fto set &aPosition 2 &7(MAX)").build();

	private final MineLoader mineLoader;
	private final MineSaver mineSaver;

	@Getter
	private final XPrisonMines plugin;

	private final Map<UUID, MineSelectionImpl> mineSelections;
	private Map<String, MineImpl> mines;

	private File minesDirectory;

	public MineManager(XPrisonMines plugin) {
		this.plugin = plugin;
		this.mineSelections = new HashMap<>();
		this.mineLoader = new MineFileLoader(this);
		this.mineSaver = new MineFileSaver(this);
	}

	public void enable() {
		this.setupMinesDirectory();
		this.loadMines();
	}

	private void setupMinesDirectory() {
		File directory = new File(this.plugin.getCore().getDataFolder().getPath() + "/mines/");

		if (!directory.exists()) {
			directory.mkdir();
			info("&aCreated /mines directory");
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

			MineImpl mineImpl = this.mineLoader.load(file);

			if (mineImpl == null) {
				continue;
			}

			this.mines.put(mineImpl.getName(), mineImpl);
			info("&aLoaded Mine &e" + mineImpl.getName());

			double ratio = (double) mineImpl.getCurrentBlocks() / mineImpl.getTotalBlocks() * 100.0;

			if (ratio <= mineImpl.getResetPercentage() && !mineImpl.isResetting()) {
				this.resetMine(mineImpl);
			}
		}
	}

	private void saveMines() {
		this.getMines().forEach(this.mineSaver::save);
	}

	public void selectPosition(Player player, int position, Position pos) {

		MineSelectionImpl selection;

		if (!mineSelections.containsKey(player.getUniqueId())) {
			this.mineSelections.put(player.getUniqueId(), new MineSelectionImpl());
		}

		selection = this.mineSelections.get(player.getUniqueId());

		switch (position) {
			case 1:
				selection.setPosition1(pos);
				break;
			case 2:
				selection.setPosition2(pos);
				break;
		}

		if (selection.isValid()) {
			PlayerUtils.sendMessage(player, this.plugin.getMessage("selection_valid"));
		}

		PlayerUtils.sendMessage(player, this.plugin.getMessage("selection_point_set").replace("%position%", String.valueOf(position)).replace("%location%", LocationUtils.toXYZW(pos.toLocation())));
	}

	public MineSelectionImpl getMineSelection(Player player) {
		return this.mineSelections.get(player.getUniqueId());
	}

	public boolean createMine(Player creator, String name) {
		MineSelectionImpl selection = this.getMineSelection(creator);

		if (selection == null || !selection.isValid()) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("selection_invalid"));
			return false;
		}

		if (this.getMineByName(name) != null) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_exists"));
			return false;
		}

		MineImpl mineImpl = new MineImpl(this, name, selection.toRegion());

		this.mines.put(mineImpl.getName(), mineImpl);

		this.mineSaver.save(mineImpl);

		PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_created").replace("%mine%", name));
		return true;
	}

	public MineImpl createMine(MineSelectionImpl selection, String name) {

		MineSelectionImpl mineSelection = new MineSelectionImpl(selection.getPosition1(),selection.getPosition2());

		if (!mineSelection.isValid()) {
			return null;
		}

		if (this.getMineByName(name) != null) {
			return null;
		}

		MineImpl mineImpl = new MineImpl(this, name, mineSelection.toRegion());

		this.mines.put(mineImpl.getName(), mineImpl);

		this.mineSaver.save(mineImpl);

		PlayerUtils.sendMessage(null, this.plugin.getMessage("mine_created").replace("%mine%", name));
		return mineImpl;
	}

	public boolean deleteMine(CommandSender sender, String name) {
		MineImpl mineImpl = this.getMineByName(name);

		if (mineImpl == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists").replace("%mine%", name));
			return false;
		}

		return deleteMine(sender, mineImpl);
	}

	public boolean deleteMine(CommandSender sender, MineImpl mineImpl) {
		if (mineImpl.getFile() != null) {
			mineImpl.getFile().delete();
		}

		mineImpl.stopTicking();


		this.mines.remove(mineImpl.getName());

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_deleted").replace("%mine%", mineImpl.getName()));
		return true;
	}

	public MineImpl getMineByName(String name) {
		return this.mines.get(name);
	}

	public MineImpl getMineAtLocation(Location loc) {
		for (MineImpl mineImpl : this.mines.values()) {
			if (mineImpl.isInMine(loc)) {
				return mineImpl;
			}
		}
		return null;
	}

	public void disable() {
		this.saveMines();
	}

	public Collection<MineImpl> getMines() {
		return this.mines.values();
	}

	public boolean teleportToMine(Player player, MineImpl mineImpl) {

		if (mineImpl.getTeleportLocation() == null) {
			PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_no_teleport_location").replace("%mine%", mineImpl.getName()));
			return false;
		}

		player.teleport(mineImpl.getTeleportLocation().toLocation());
		PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_teleport").replace("%mine%", mineImpl.getName()));
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
			List<MineImpl> mineImpls = this.mines.values().stream().sorted(Comparator.comparing(MineImpl::getName)).collect(Collectors.toList());
			for (MineImpl mineImpl : mineImpls) {
				items.add(ItemStackBuilder.of(Material.STONE).name(mineImpl.getName()).lore("&aLeft-Click &7to open Mine Panel for this mine.", "&aRight-Click &7to teleport to this mine.").build(() -> {
					this.teleportToMine(player, mineImpl);
				}, () -> {
					new MinePanelGUI(mineImpl, player).open();
				}));
			}
			return items;
		}).open();

	}

	public boolean setTeleportLocation(Player player, MineImpl mineImpl) {
		mineImpl.setTeleportLocation(Point.of(player.getLocation()));
		PlayerUtils.sendMessage(player, this.plugin.getMessage("mine_teleport_set").replace("%mine%", mineImpl.getName()));
		return true;
	}

	public boolean giveTool(Player sender) {
		sender.getInventory().addItem(SELECTION_TOOL);
		PlayerUtils.sendMessage(sender, this.plugin.getMessage("selection_tool_given"));
		return true;
	}

	public void reload() {
	}

	public void resetAllMines() {
		this.getMines().forEach(this::resetMine);
	}

	public void resetMine(MineImpl mineImpl) {

		if (mineImpl == null) {
			return;
		}

		if (mineImpl.isResetting()) {
			return;
		}

		mineImpl.setResetting(true);

		if (mineImpl.isBroadcastReset()) {
			mineImpl.getPlayersInMine().forEach(player -> PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_resetting").replace("%mine%", mineImpl.getName())));
		}

		Schedulers.sync().runLater(() -> {

			if (mineImpl.getTeleportLocation() != null) {
				mineImpl.getPlayersInMine().forEach(player -> player.teleport(mineImpl.getTeleportLocation().toLocation()));
			}

			mineImpl.getResetType().reset(mineImpl, mineImpl.getBlockPaletteImpl());

			if (mineImpl.isBroadcastReset()) {
				mineImpl.getPlayersInMine().forEach(player -> PlayerUtils.sendMessage(player, this.getPlugin().getMessage("mine_reset").replace("%mine%", mineImpl.getName())));
			}

			mineImpl.setNextResetDate(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(mineImpl.getResetTime())));

			mineImpl.setResetting(false);

		}, 5, TimeUnit.SECONDS);

	}


	public boolean renameMine(Player sender, String oldMineName, String newName) {

		MineImpl mineImpl = this.getMineByName(oldMineName);

		if (mineImpl == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_not_exists"));
			return false;
		}

		if (mineImpl.getFile() != null) {
			mineImpl.getFile().delete();
		}

		this.mines.remove(oldMineName);
		mineImpl.setName(newName);
		this.mines.put(newName, mineImpl);

		this.mineSaver.save(mineImpl);

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_renamed").replace("%mine%", oldMineName).replace("%new_name%", newName));
		return true;
	}

	public boolean redefineMine(Player creator, String name) {
		MineSelectionImpl selection = this.getMineSelection(creator);

		if (selection == null || !selection.isValid()) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("selection_invalid"));
			return false;
		}

		MineImpl mineImpl = this.getMineByName(name);
		if (mineImpl == null) {
			PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_not_exists"));
			return false;
		}

		Region region = selection.toRegion();
		mineImpl.setMineRegion(region);

		PlayerUtils.sendMessage(creator, this.plugin.getMessage("mine_redefined").replace("%mine%", name));
		return true;
	}

	public MineLoader getMineLoader() {
		return mineLoader;
	}

	public MineSaver getMineSaver() {
		return mineSaver;
	}

	public static boolean isSelectionTool(ItemStack item) {
		if (item == null) return false;
		if (item.getType() != Material.STICK) return false;
		if (!item.hasItemMeta()) return false;

		ItemMeta meta = item.getItemMeta();
		if (!meta.hasDisplayName() || !meta.getDisplayName().equals(ChatColor.YELLOW + "Mine Selection Tool")) {
			return false;
		}

		if (!meta.hasLore()) return false;
		if (!meta.hasEnchant(XEnchantment.UNBREAKING.get())) return false;

		return true;
	}
}
