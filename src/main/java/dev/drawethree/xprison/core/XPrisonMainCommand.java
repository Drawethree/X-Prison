package dev.drawethree.xprison.core;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleAbstract;
import dev.drawethree.xprison.mainmenu.MainMenu;
import dev.drawethree.xprison.mainmenu.help.HelpGui;
import dev.drawethree.xprison.utils.text.TextUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

import java.util.List;

public class XPrisonMainCommand {

    private final XPrison plugin;

    public XPrisonMainCommand(XPrison plugin) {
        this.plugin = plugin;
    }

    public void register() {
        registerMainCommand();
    }

    private void registerMainCommand() {

        List<String> commandAliases = plugin.getConfig().getStringList("main-command-aliases");
        String[] commandAliasesArray = commandAliases.toArray(new String[commandAliases.size()]);

        Commands.create()
                .assertPermission("xprison.admin")
                .handler(c -> {
                    if (c.args().isEmpty() && c.sender() instanceof Player) {
                        new MainMenu(plugin, (Player) c.sender()).open();
                    } else if (c.args().size() >= 1) {
                        if ("reload".equalsIgnoreCase(c.rawArg(0))) {
                            final String name = c.args().size() >= 2 ? c.rawArg(1).trim().toLowerCase().replace("-", "") : "all";
                            switch (name) {
                                case "all":
                                case "*":
                                    plugin.getModules().forEach(plugin::reloadModule);
                                    plugin.getItemMigrator().reload();
                                    c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded all the plugin"));
                                    break;
                                case "migrator":
                                case "itemmigrator":
                                    plugin.getItemMigrator().reload();
                                    c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded item migrator"));
                                    break;
                                default:
                                    final XPrisonModuleAbstract module = plugin.getModuleByName(name);
                                    if (module != null) {
                                        plugin.reloadModule(module);
                                        c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded &f" + name + " &amodule"));
                                    } else {
                                        c.sender().sendMessage(TextUtils.applyColor("&cThe module &6" + c.rawArg(1) + " &cdoesn't exist"));
                                    }
                                    break;
                            }
                        } else if (c.sender() instanceof Player && "help".equalsIgnoreCase(c.rawArg(0)) || "?".equalsIgnoreCase(c.rawArg(0))) {
                            new HelpGui((Player) c.sender()).open();
                        }
                    }
                }).registerAndBind(plugin, commandAliasesArray);
    }
}