package me.drawethree.ultraprisoncore.mines.managers;

import com.google.gson.JsonObject;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.model.Mine;
import me.drawethree.ultraprisoncore.mines.model.MineSelection;
import me.drawethree.ultraprisoncore.utils.LocationUtils;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.serialize.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineManager {

	public static final ItemStack SELECTION_TOOL = ItemStackBuilder.of(Material.STICK).name("&eMine Selection").lore("&aRight-Click &fto set &aPosition 1 &7(Minimum)", "&aLeft-Click &fto set &aPosition 2 &7(Maximum)").build();

	private final UltraPrisonMines plugin;

	private Map<UUID, MineSelection> mineSelections;
	private Map<String, Mine> mines;
	private File minesDirectory;

	public MineManager(UltraPrisonMines plugin) {
		this.plugin = plugin;
		this.mineSelections = new HashMap<>();
		this.setupMinesDirectory();
		this.loadMines();
	}

	private void setupMinesDirectory() {
		File directory = new File("mines");

		if (!directory.exists()) {
			directory.mkdir();
			this.plugin.getCore().getLogger().info("Created /mines directory");
		}
		this.minesDirectory = directory;
	}

	private void loadMines() {
		this.mines = new HashMap<>();
		//TODO: Load Mines from folder ../mines
		File[] files = this.minesDirectory.listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			FileReader reader = null;
			try {
				reader = new FileReader(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			JsonObject jsonObject = GsonProvider.readObject(reader);

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

}
