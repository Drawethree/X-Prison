package dev.drawethree.xprison.autosell.command;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.utils.AutoSellContants;
import me.lucko.helper.Commands;

public class AutoSellCommand {

    private static final String COMMAND_NAME = "autosell";
    private final XPrisonAutoSell plugin;

    public AutoSellCommand(XPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .assertPermission(AutoSellContants.AUTOSELL_PERMISSION, this.plugin.getAutoSellConfig().getMessage("no_permission_autosell_toggle"))
                .handler(c -> {
                    if (c.args().isEmpty()) {
                        this.plugin.getManager().toggleAutoSell(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), COMMAND_NAME);
    }
}
