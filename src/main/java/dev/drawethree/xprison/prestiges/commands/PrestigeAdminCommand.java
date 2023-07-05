package dev.drawethree.xprison.prestiges.commands;

import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class PrestigeAdminCommand {
    private final XPrisonPrestiges plugin;

    public PrestigeAdminCommand(XPrisonPrestiges plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPermission("xprison.prestiges.admin")
                .handler(c -> {
                    if (c.args().size() == 3) {

                        Player target = c.arg(1).parseOrFail(Player.class);
                        int amount = c.arg(2).parseOrFail(Integer.class);

                        switch (c.rawArg(0).toLowerCase()) {
                            case "set":
                                this.plugin.getPrestigeManager().setPlayerPrestige(c.sender(), target, amount);
                                break;
                            case "add":
                                this.plugin.getPrestigeManager().addPlayerPrestige(c.sender(), target, amount);
                                break;
                            case "remove":
                                this.plugin.getPrestigeManager().removePlayerPrestige(c.sender(), target, amount);
                                break;
                            default:
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                PlayerUtils.sendMessage(c.sender(), "&e&lPRESTIGE ADMIN HELP MENU ");
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin add [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin remove [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e/prestigeadmin set [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                break;
                        }
                    }
                }).registerAndBind(this.plugin.getCore(), "prestigeadmin", "prestigea");
    }
}
