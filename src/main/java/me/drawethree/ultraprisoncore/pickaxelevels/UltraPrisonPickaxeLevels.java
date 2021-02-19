package me.drawethree.ultraprisoncore.pickaxelevels;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.pickaxelevels.api.UltraPrisonPickaxeLevelsAPI;
import me.drawethree.ultraprisoncore.pickaxelevels.api.UltraPrisonPickaxeLevelsAPIImpl;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.lucko.helper.text.Text;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UltraPrisonPickaxeLevels implements UltraPrisonModule {


	private static final String ADMIN_PERMISSION = "ultraprison.pickaxe.admin";

	@Getter
	private FileManager.Config config;

	private Map<Integer, PickaxeLevel> pickaxeLevels;
	private Map<String, String> messages;
	private PickaxeLevel defaultLevel;
	@Getter
	private UltraPrisonPickaxeLevelsAPI api;
	@Getter
	private UltraPrisonCore core;
	private boolean enabled;

	public UltraPrisonPickaxeLevels(UltraPrisonCore UltraPrisonCore) {
		this.core = UltraPrisonCore;
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), Text.colorize(this.getConfig().get().getString("messages." + key)));
		}
	}

	private void loadPickaxeLevels() {
		pickaxeLevels = new LinkedHashMap<>();

		ConfigurationSection section = this.getConfig().get().getConfigurationSection("levels");
		if (section == null) {
			return;
		}

		for (String level : section.getKeys(false)) {

			int levelId = Integer.parseInt(level);

			String displayName = Text.colorize(this.getConfig().get().getString("levels." + level + ".display_name"));
			long blocksRequire = this.getConfig().get().getLong("levels." + level + ".blocks_required");
			List<String> rewards = this.getConfig().get().getStringList("levels." + level + ".rewards");


			PickaxeLevel pickaxeLevel = new PickaxeLevel(levelId, blocksRequire, displayName, rewards);

			if (levelId == 1) {
				this.defaultLevel = pickaxeLevel;
			}

			this.pickaxeLevels.put(levelId, pickaxeLevel);

			this.core.getLogger().info("Loaded Pickaxe Level " + levelId);

		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.config.reload();
		this.loadMessages();
		this.loadPickaxeLevels();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.config = this.core.getFileManager().getConfig("pickaxe-levels.yml").copyDefaults(true).save();


		this.loadPickaxeLevels();
		this.loadMessages();
		this.registerCommands();
		this.registerListeners();

		this.api = new UltraPrisonPickaxeLevelsAPIImpl(this);
	}

	private void registerListeners() {

	}

	@Override
	public void disable() {
		this.enabled = false;
	}

	@Override
	public String getName() {
		return "Pickaxe Levels";
	}

	private void registerCommands() {

	}


	public String getMessage(String key) {
		return messages.get(key.toLowerCase());
	}

}
