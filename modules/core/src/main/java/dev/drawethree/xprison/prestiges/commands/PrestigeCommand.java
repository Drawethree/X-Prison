package dev.drawethree.xprison.prestiges.commands;

import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import me.lucko.helper.Commands;

public class PrestigeCommand {

    private final XPrisonPrestiges plugin;

    public PrestigeCommand(XPrisonPrestiges plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getPrestigeManager().buyNextPrestige(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "prestige");
    }
}
