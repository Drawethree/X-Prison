package dev.drawethree.xprison.blocks.api;

import dev.drawethree.xprison.api.blocks.XPrisonBlocksAPI;
import dev.drawethree.xprison.blocks.managers.BlocksManager;

public class XPrisonBlocksAPIImpl implements XPrisonBlocksAPI {

    private final BlocksManager manager;

    public XPrisonBlocksAPIImpl(BlocksManager manager) {
        this.manager = manager;
    }
}