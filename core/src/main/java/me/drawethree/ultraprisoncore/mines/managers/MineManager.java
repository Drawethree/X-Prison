package me.drawethree.ultraprisoncore.mines.managers;

import lombok.Getter;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.gui.MinePanelGUI;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.mines.model.mine.MineSelection;
import me.drawethree.ultraprisoncore.mines.utils.MineLoader;
import me.drawethree.ultraprisoncore.utils.LocationUtils;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.paginated.PaginatedGuiBuilder;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MineManager {

	public static final ItemStack SELECTION_TOOL = ItemStackBuilder.of(Material.STICK).enchant(Enchantment.DURABILITY).name("&eMine Selection Tool").lore("&aRight-Click &fto set &aPosition 1 &7(MIN)", "&aLeft-Click &fto set &aPosition 2 &7(MAX)").build();

	@Getter
	private final UltraPrisonMines plugin;

	private Map<UUID, MineSelection> mineSelections;
	private Map<String, Mine> mines;

	private List<String> hologramBlocksLeftLines;
	private List<String> hologramBlocksMinedLines;

	private File minesDirectory;

	public MineManager(UltraPrisonMines plugin) {
		this.plugin = plugin;
		this.mineSelections = new HashMap<>();
		this.hologramBlocksLeftLines = this.plugin.getConfig().get().getStringList("holograms.blocks_left");
		this.hologramBlocksMinedLines = this.plugin.getConfig().get().getStringList("holograms.blocks_mined");
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
			try (FileReader reader = new FileReader(file)) {
				Mine mine = MineLoader.load(this, reader);
				this.mines.put(mine.getName(), mine);
				this.plugin.getCore().getLogger().info("Loaded Mine " + mine.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveMines() {
		for (Mine mine : this.mines.values()) {
			MineLoader.save(mine);
		}
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
			player.sendMessage(this.plugin.getMessage("selection_valid"));
		}

		player.sendMessage(this.plugin.getMessage("selection_point_set").replace("%position%", String.valueOf(position)).replace("%location%", LocationUtils.toXYZW(pos.toLocation())));
	}

	public MineSelection getMineSelection(Player player) {
		return this.mineSelections.get(player.getUniqueId());
	}

	public boolean createMine(Player creator, String name) {
		MineSelection selection = this.getMineSelection(creator);

		if (selection == null || !selection.isValid()) {
			creator.sendMessage(this.plugin.getMessage("selection_invalid"));
			return false;
		}

		if (this.getMineByName(name) != null) {
			creator.sendMessage(this.plugin.getMessage("mine_exists"));
			return false;
		}

		Mine mine = new Mine(this, name, selection.toRegion());

		this.mines.put(mine.getName(), mine);

		creator.sendMessage(this.plugin.getMessage("mine_created").replace("%mine%", name));
		return true;
	}

	public boolean deleteMine(CommandSender sender, String name) {
		Mine mine = this.getMineByName(name);

		if (mine == null) {
			sender.sendMessage(this.plugin.getMessage("mine_not_exists").replace("%mine%", name));
			return false;
		}


		if (mine.getFile() != null) {
			mine.getFile().delete();
		}

		this.mines.remove(mine.getName());

		sender.sendMessage(this.plugin.getMessage("mine_deleted").replace("%mine%", name));
		return true;
	}

	public boolean deleteMine(CommandSender sender, Mine mine) {
		if (mine.getFile() != null) {
			mine.getFile().delete();
		}

		this.mines.remove(mine.getName());

		sender.sendMessage(this.plugin.getMessage("mine_deleted").replace("%mine%", mine.getName()));
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
		this.despawnHolograms();

	}

	private void despawnHolograms() {
		for (Mine mine : this.mines.values()) {
			mine.despawnHolograms();
		}
	}

	public Collection<Mine> getMines() {
		return this.mines.values();
	}

	public boolean teleportToMine(Player player, Mine mine) {
		if (mine.getTeleportLocation() == null) {
			player.sendMessage(this.plugin.getMessage("mine_no_teleport_location").replace("%mine%", mine.getName()));
			return false;
		}

		player.teleport(mine.getTeleportLocation().toLocation());
		player.sendMessage(this.plugin.getMessage("mine_teleport").replace("%mine%", mine.getName()));
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
			for (Mine mine : this.mines.values()) {
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
		player.sendMessage(this.plugin.getMessage("mine_teleport_set").replace("%mine%", mine.getName()));
		return true;
	}

	public boolean giveTool(Player sender) {
		sender.getInventory().addItem(SELECTION_TOOL);
		sender.sendMessage(this.plugin.getMessage("selection_tool_given"));
		return true;
	}

	public void reload() {
		this.hologramBlocksLeftLines = this.plugin.getConfig().get().getStringList("holograms.blocks_left");
		this.hologramBlocksMinedLines = this.plugin.getConfig().get().getStringList("holograms.blocks_mined");
	}

	public boolean addMineFromMigration(CommandSender sender, Mine migrated) {
		if (!this.mines.containsKey(migrated.getName())) {
			this.mines.put(migrated.getName(), migrated);
			return true;
		}
		return false;
	}
}
