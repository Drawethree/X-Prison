package dev.drawethree.xprison.blocks;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleAbstract;
import dev.drawethree.xprison.api.blocks.XPrisonBlocksAPI;
import dev.drawethree.xprison.blocks.api.XPrisonBlocksAPIImpl;
import dev.drawethree.xprison.blocks.config.BlockRewardsConfig;
import dev.drawethree.xprison.blocks.config.BlocksConfig;
import dev.drawethree.xprison.blocks.listener.BlocksListener;
import dev.drawethree.xprison.blocks.managers.BlocksManager;
import dev.drawethree.xprison.blocks.managers.CommandManager;
import dev.drawethree.xprison.blocks.repo.BlocksRepository;
import dev.drawethree.xprison.blocks.repo.impl.BlocksRepositoryImpl;
import dev.drawethree.xprison.blocks.service.BlocksService;
import dev.drawethree.xprison.blocks.service.impl.BlocksServiceImpl;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.utils.task.RepeatingTask;
import lombok.Getter;
import me.lucko.helper.utils.Players;

import java.util.concurrent.TimeUnit;

public final class XPrisonBlocks implements XPrisonModuleAbstract, PlayerDataHolder {

    public static final String MODULE_NAME = "Blocks";

    @Getter
    private static XPrisonBlocks instance;

    @Getter
    private BlockRewardsConfig blockRewardsConfig;

    @Getter
    private BlocksConfig blocksConfig;

    @Getter
    private XPrisonBlocksAPI api;

    @Getter
    private BlocksManager blocksManager;

    @Getter
    private CommandManager commandManager;

    @Getter
    private BlocksRepository blocksRepository;

    @Getter
    private BlocksService blocksService;

    @Getter
    private final XPrison core;

    private RepeatingTask savePlayerDataTask;

    private boolean enabled;


    public XPrisonBlocks(XPrison prisonCore) {
        instance = this;
        this.core = prisonCore;
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.blockRewardsConfig.reload();
        this.blocksManager.reload();
        this.commandManager.reload();
    }


    @Override
    public void enable() {

        this.blocksConfig = new BlocksConfig(this);
        this.blocksConfig.load();

        this.blockRewardsConfig = new BlockRewardsConfig(this);
        this.blockRewardsConfig.load();

        this.blocksRepository = new BlocksRepositoryImpl(this.core.getPluginDatabase());
        this.blocksRepository.createTables();

        this.blocksService = new BlocksServiceImpl(this.blocksRepository);

        this.blocksManager = new BlocksManager(this);
        this.blocksManager.enable();

        this.commandManager = new CommandManager(this);
        this.commandManager.enable();

        this.savePlayerDataTask = new RepeatingTask.Builder().
                initialDelay(30).
                initialDelayUnit(TimeUnit.SECONDS).
                interval(getBlocksConfig().getSavePlayerDataInterval()).intervalUnit(TimeUnit.MINUTES).
                task(() ->getBlocksManager().savePlayerData(Players.all(), false, true)).build();
        this.savePlayerDataTask.start();

        this.registerListeners();

        this.commandManager.enable();

        this.api = new XPrisonBlocksAPIImpl(this.blocksManager);

        this.enabled = true;
    }

    private void registerListeners() {
        new BlocksListener(this).subscribeToEvents();
    }


    @Override
    public void disable() {
        this.blocksManager.disable();

        this.savePlayerDataTask.stop();

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
        this.blocksRepository.clearTableData();
    }

}
