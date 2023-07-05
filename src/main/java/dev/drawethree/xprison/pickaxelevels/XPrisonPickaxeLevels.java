package dev.drawethree.xprison.pickaxelevels;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.pickaxelevels.api.XPrisonPickaxeLevelsAPI;
import dev.drawethree.xprison.pickaxelevels.api.XPrisonPickaxeLevelsAPIImpl;
import dev.drawethree.xprison.pickaxelevels.config.PickaxeLevelsConfig;
import dev.drawethree.xprison.pickaxelevels.listener.PickaxeLevelsListener;
import dev.drawethree.xprison.pickaxelevels.manager.PickaxeLevelsManager;
import lombok.Getter;

public final class XPrisonPickaxeLevels implements XPrisonModule {

    public static final String MODULE_NAME = "Pickaxe Levels";

    @Getter
    private PickaxeLevelsConfig pickaxeLevelsConfig;
    @Getter
    private PickaxeLevelsManager pickaxeLevelsManager;
    @Getter
    private XPrisonPickaxeLevelsAPI api;

    @Getter
    private final XPrison core;

    private boolean enabled;

    public XPrisonPickaxeLevels(XPrison core) {
        this.core = core;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.pickaxeLevelsConfig.reload();
	}

    @Override
    public void enable() {
        this.pickaxeLevelsConfig = new PickaxeLevelsConfig(this);
        this.pickaxeLevelsConfig.load();
        this.pickaxeLevelsManager = new PickaxeLevelsManager(this);

        this.registerListeners();

        this.api = new XPrisonPickaxeLevelsAPIImpl(this.pickaxeLevelsManager);
        this.enabled = true;
    }

    private void registerListeners() {
        new PickaxeLevelsListener(this).register();
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public boolean isHistoryEnabled() {
        return false;
    }

    @Override
    public void resetPlayerData() {
    }
}
