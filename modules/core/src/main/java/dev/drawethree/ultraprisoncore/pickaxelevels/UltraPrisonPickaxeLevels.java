package dev.drawethree.ultraprisoncore.pickaxelevels;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.pickaxelevels.api.UltraPrisonPickaxeLevelsAPI;
import dev.drawethree.ultraprisoncore.pickaxelevels.api.UltraPrisonPickaxeLevelsAPIImpl;
import dev.drawethree.ultraprisoncore.pickaxelevels.config.PickaxeLevelsConfig;
import dev.drawethree.ultraprisoncore.pickaxelevels.listener.PickaxeLevelsListener;
import dev.drawethree.ultraprisoncore.pickaxelevels.manager.PickaxeLevelsManager;
import lombok.Getter;

public final class UltraPrisonPickaxeLevels implements UltraPrisonModule {

    public static final String MODULE_NAME = "Pickaxe Levels";

    @Getter
    private PickaxeLevelsConfig pickaxeLevelsConfig;
    @Getter
    private PickaxeLevelsManager pickaxeLevelsManager;
    @Getter
    private UltraPrisonPickaxeLevelsAPI api;

    @Getter
    private final UltraPrisonCore core;

    private boolean enabled;

    public UltraPrisonPickaxeLevels(UltraPrisonCore core) {
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

        this.api = new UltraPrisonPickaxeLevelsAPIImpl(this.pickaxeLevelsManager);
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
    public String[] getTables() {
        return new String[0];
    }

    @Override
    public String[] getCreateTablesSQL(DatabaseType type) {
        return new String[0];
    }

    @Override
    public boolean isHistoryEnabled() {
        return false;
    }
}
