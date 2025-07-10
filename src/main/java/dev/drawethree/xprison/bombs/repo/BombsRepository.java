package dev.drawethree.xprison.bombs.repo;

import com.cryptomorin.xseries.XSound;
import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.config.BombsConfig;
import dev.drawethree.xprison.bombs.model.BombImpl;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import me.lucko.helper.item.ItemStackReader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public final class BombsRepository  {


    private final BombsConfig config;
    private final Map<String, Bomb> bombs;

    public BombsRepository(BombsConfig config) {
        this.config = config;
        this.bombs = new HashMap<>();
    }

    public void load() {
        loadBombs();
    }

    public void reload() {
        loadBombs();
    }

    private void loadBombs() {
        this.bombs.clear();

        YamlConfiguration configuration = config.getYamlConfig();

        ConfigurationSection section = configuration.getConfigurationSection("bombs");

        if (section == null) {
            return;
        }

        for (String type : section.getKeys(false)) {
            Bomb bomb = this.loadBombFromConfig(configuration, type);
            this.bombs.put(type.toLowerCase(), bomb);
            info("&aLoaded Bomb: &e" + type);
        }
    }


    private Bomb loadBombFromConfig(YamlConfiguration config, String type) {

        String rootPath = "bombs." + type + ".";

        int radius = config.getInt(rootPath + "radius");
        ItemStack item = ItemStackReader.DEFAULT.read(config.getConfigurationSection(rootPath + "item")).build();
        int customModelData = config.getInt(rootPath + "custom_model_data");
        item = ItemStackBuilder.of(item).customModelData(customModelData).build();
        XSound dropSound = XSound.of(config.getString(rootPath + "drop_sound")).get();
        XSound explodeSound = XSound.of(config.getString(rootPath + "explode_sound")).get();
        int explosionDelay = config.getInt(rootPath + "explosion_delay");
        return new BombImpl(type, radius, item, dropSound, explodeSound, explosionDelay);
    }

    public Collection<Bomb> getBombs() {
        return this.bombs.values();
    }

    public Optional<Bomb> getBombByName(String name) {
        return Optional.ofNullable(this.bombs.get(name.toLowerCase()));
    }
}
