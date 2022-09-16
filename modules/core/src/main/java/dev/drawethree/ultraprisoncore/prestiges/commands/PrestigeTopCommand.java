package dev.drawethree.ultraprisoncore.prestiges.commands;

import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.lucko.helper.Commands;

public class PrestigeTopCommand {

    private final UltraPrisonPrestiges plugin;

    public PrestigeTopCommand(UltraPrisonPrestiges plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getPrestigeManager().sendPrestigeTop(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "prestigetop");
    }
}
