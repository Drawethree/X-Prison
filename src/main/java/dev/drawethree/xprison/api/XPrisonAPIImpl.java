package dev.drawethree.xprison.api;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.autominer.XPrisonAutoMinerAPI;
import dev.drawethree.xprison.api.autosell.XPrisonAutoSellAPI;
import dev.drawethree.xprison.api.bombs.XPrisonBombsAPI;
import dev.drawethree.xprison.api.enchants.XPrisonEnchantsAPI;
import dev.drawethree.xprison.api.gangs.XPrisonGangsAPI;
import dev.drawethree.xprison.api.gems.XPrisonGemsAPI;
import dev.drawethree.xprison.api.history.XPrisonHistoryAPI;
import dev.drawethree.xprison.api.mines.XPrisonMinesAPI;
import dev.drawethree.xprison.api.multipliers.XPrisonMultipliersAPI;
import dev.drawethree.xprison.api.pickaxelevels.XPrisonPickaxeLevelsAPI;
import dev.drawethree.xprison.api.prestiges.XPrisonPrestigesAPI;
import dev.drawethree.xprison.api.ranks.XPrisonRanksAPI;
import dev.drawethree.xprison.api.tokens.XPrisonTokensAPI;
import org.jetbrains.annotations.NotNull;

public class XPrisonAPIImpl implements XPrisonAPI {

    private final XPrison plugin;

    public XPrisonAPIImpl(XPrison plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull XPrisonAutoMinerAPI getAutoMinerApi() {
        return plugin.getAutoMiner().getApi();
    }

    @Override
    public @NotNull XPrisonAutoSellAPI getAutoSellApi() {
        return plugin.getAutoSell().getApi();
    }

    @Override
    public @NotNull XPrisonEnchantsAPI getEnchantsApi() {
        return plugin.getEnchants().getApi();
    }

    @Override
    public @NotNull XPrisonGangsAPI getGangsApi() {
        return plugin.getGangs().getApi();
    }

    @Override
    public @NotNull XPrisonGemsAPI getGemsApi() {
        return plugin.getGems().getApi();
    }

    @Override
    public @NotNull XPrisonHistoryAPI getHistoryApi() {
        return plugin.getHistory().getApi();
    }

    @Override
    public @NotNull XPrisonMinesAPI getMinesApi() {
        return plugin.getMines().getApi();
    }

    @Override
    public @NotNull XPrisonMultipliersAPI getMultipliersApi() {
        return plugin.getMultipliers().getApi();
    }

    @Override
    public @NotNull XPrisonPickaxeLevelsAPI getPickaxeLevelsApi() {
        return plugin.getPickaxeLevels().getApi();
    }

    @Override
    public @NotNull XPrisonPrestigesAPI getPrestigesApi() {
        return plugin.getPrestiges().getApi();
    }

    @Override
    public @NotNull XPrisonRanksAPI getRanksApi() {
        return plugin.getRanks().getApi();
    }

    @Override
    public @NotNull XPrisonTokensAPI getTokensApi() {
        return plugin.getTokens().getApi();
    }

    @Override
    public @NotNull XPrisonBombsAPI getBombsApi() {
        return plugin.getBombs().getApi();
    }
}