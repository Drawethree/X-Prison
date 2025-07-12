package dev.drawethree.xprison.autosell.command;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

public class SellAllCommand {

    private static final String COMMAND_NAME = "sellall";
    private final XPrisonAutoSell plugin;

    public SellAllCommand(XPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    this.plugin.getManager().sellAll(c.sender());
                }).registerAndBind(this.plugin, COMMAND_NAME);
    }
}
