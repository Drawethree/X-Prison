package dev.drawethree.ultraprisoncore.prestiges.commands;

import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.lucko.helper.Commands;

public class PrestigeCommand {

    private final UltraPrisonPrestiges plugin;

    public PrestigeCommand(UltraPrisonPrestiges plugin) {
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
