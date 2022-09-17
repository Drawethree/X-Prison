package dev.drawethree.ultraprisoncore.prestiges.commands;

import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.lucko.helper.Commands;

public class MaxPrestigeCommand {

    private final UltraPrisonPrestiges plugin;

    public MaxPrestigeCommand(UltraPrisonPrestiges plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPermission("ultraprison.prestiges.maxprestige", this.plugin.getPrestigeConfig().getMessage("no_permission"))
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
