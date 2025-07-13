package dev.drawethree.xprison.enchants.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.drawethree.xprison.XPrisonLite;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.enchants.repo.EnchantsRepository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.*;

public class EnchantLoader {

    private static final File ENCHANTS_DIR = new File(XPrisonLite.getInstance().getDataFolder().getPath() + "/enchants/");
    private final EnchantsRepository repository;

    public EnchantLoader(EnchantsRepository repository) {
        this.repository = repository;
    }

    public void load() {
        loadFromFolder(ENCHANTS_DIR);
    }


    private void loadFromFolder(File folder) {
        info("Loading enchantments from folder " + folder.getPath());
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {

                info("Loading enchantment from file " + file.getName());

                JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
                JsonElement classNameElement = config.get("class");

                if (classNameElement == null) {
                    warning(file.getName() + " is missing required 'class' element! Skipping loading of this enchant");
                    continue;
                }

                String className = classNameElement.getAsString();

                Class<?> clazz = Class.forName(className);

                XPrisonEnchantmentBaseCore enchant = (XPrisonEnchantmentBaseCore) clazz.getDeclaredConstructor().newInstance();
                enchant.setEnchantConfigFile(file);
                enchant.load();

                repository.register(enchant);
            } catch (IOException | JsonSyntaxException e) {
                error("Failed to load enchant file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                error("Enchant class not found while loading file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                error("Failed to invoke constructor of enchant class in file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (InstantiationException e) {
                error("Failed to instantiate enchant class in file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                error("Illegal access while instantiating enchant class in file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                error("No suitable constructor found for enchant class in file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}