package dev.drawethree.xprison.prestiges.commands;

import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import me.lucko.helper.Commands;

public class MaxPrestigeCommand {

    private final XPrisonPrestiges plugin;

    public MaxPrestigeCommand(XPrisonPrestiges plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPermission("xprison.prestiges.maxprestige", this.plugin.getPrestigeConfig().getMessage("no_permission"))
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {

                        if (this.plugin.getPrestigeManager().isPrestiging(c.sender())) {
                            return;
                        }

                        this.plugin.getPrestigeManager().buyMaxPrestige(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "maxprestige", "maxp", "mp");
    }
}
