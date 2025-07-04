package dev.drawethree.xprison.mainmenu.modules;

import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public class GuiModule {

    @Getter
    private final XPrisonModuleBase module;
    private final Supplier<ItemStack> iconSupplier;
    @Getter
    private final int slot;

    public GuiModule(XPrisonModuleBase module, Supplier<ItemStack> iconSupplier, int slot) {
        this.module = module;
        this.iconSupplier = iconSupplier;
        this.slot = slot;
    }

    public ItemStack getIcon() {
        return iconSupplier.get();
    }

    public void reload(Player player) {
        module.reload();
        PlayerUtils.sendMessage(player, "&aReloaded module: &e" + module.getName());
    }

    public void resetPlayerData(Player player) {
        if (module instanceof PlayerDataHolder) {
            ((PlayerDataHolder) module).resetPlayerData();
        }
        PlayerUtils.sendMessage(player, "&cReset player data for module: &e" + module.getName());
    }

    public void toggleEnable(Player player) {
        if (module.isEnabled()) {
            module.disable();
            PlayerUtils.sendMessage(player, "&cDisabled module: &e" + module.getName());
        } else {
            module.enable();
            PlayerUtils.sendMessage(player, "&aEnabled module: &e" + module.getName());
        }
    }
}
