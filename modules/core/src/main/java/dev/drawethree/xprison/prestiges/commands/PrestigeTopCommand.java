package dev.drawethree.xprison.prestiges.commands;

import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import me.lucko.helper.Commands;

public class PrestigeTopCommand {

    private final XPrisonPrestiges plugin;

    public PrestigeTopCommand(XPrisonPrestiges plugin) {

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
