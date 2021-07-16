package me.drawethree.ultraprisoncore.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;


public class FileManager {

    private final JavaPlugin plugin;
    private HashMap<String, Config> configs = new HashMap<String, Config>();

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the config by the name(Don't forget the .yml)
     *
     * @param name
     * @return
     */
    public Config getConfig(String name) {
        if (!configs.containsKey(name))
            configs.put(name, new Config(name));

        return configs.get(name);
    }

    /**
     * Save the config by the name(Don't forget the .yml)
     *
     * @param name
     * @return
     */
    public Config saveConfig(String name) {
        return getConfig(name).save();
    }

    /**
     * Reload the config by the name(Don't forget the .yml)
     *
     * @param name
     * @return
     */
    public Config reloadConfig(String name) {
        return getConfig(name).reload();
    }

    public class Config {

        private String name;
        private File file;
        private YamlConfiguration config;

        public Config(String name) {
            this.name = name;
        }

        /**
         * Saves the config as long as the config isn't empty
         *
         * @return
         */
        public Config save() {
            if ((this.config == null) || (this.file == null))
                return this;
            try {
                if (config.getConfigurationSection("").getKeys(true).size() != 0)
                    config.save(this.file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return this;
        }

        /**
         * Gets the config as a YamlConfiguration
         *
         * @return
         */
        public YamlConfiguration get() {
            if (this.config == null)
                reload();

            return this.config;
        }

        /**
         * Saves the default config(Will overwrite anything in the current config's file)
         * <p>
         * Don't forget to reload after!
         *
         * @return
         */
        public Config saveDefaultConfig() {
            file = new File(plugin.getDataFolder(), this.name);

            plugin.saveResource(this.name, false);

            return this;
        }

        /**
         * Reloads the config
         *
         * @return
         */
        public Config reload() {
            if (file == null)
                this.file = new File(plugin.getDataFolder(), this.name);

            this.config = YamlConfiguration.loadConfiguration(file);

            Reader defConfigStream;
            try {
                defConfigStream = new InputStreamReader(plugin.getResource(this.name), "UTF8");

                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    this.config.setDefaults(defConfig);
                }
            } catch (UnsupportedEncodingException | NullPointerException e) {

            }
            return this;
        }

        /**
         * Copies the config from the resources to the config's default settings.
         * <p>
         * Force = true ----> Will add any new values from the default file
         * <p>
         * Force = false ---> Will NOT add new values from the default file
         *
         * @param force
         * @return
         */
        public Config copyDefaults(boolean force) {
            get().options().copyDefaults(force);
            return this;
        }

        /**
         * An easy way to set a value into the config
         *
         * @param key
         * @param value
         * @return
         */
        public Config set(String key, Object value) {
            get().set(key, value);
            return this;
        }

        /**
         * An easy way to get a value from the config
         *
         * @param key
         * @return
         */
        public Object get(String key) {
            return get().get(key);
        }
    }

}