package dev.drawethree.xprison.core;

import dev.drawethree.xprison.XPrisonLite;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.utils.text.TextUtils;
import me.lucko.helper.Commands;

import java.util.List;

public class XPrisonMainCommand {

    private final XPrisonLite plugin;

    public XPrisonMainCommand(XPrisonLite plugin) {
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
                    if (!c.args().isEmpty()) {
                        if ("reload".equalsIgnoreCase(c.rawArg(0))) {
                            final String name = c.args().size() >= 2 ? c.rawArg(1).trim().toLowerCase().replace("-", "") : "all";
                            switch (name) {
                                case "all":
                                case "*":
                                    plugin.getModules().forEach(plugin::reloadModule);
                                    c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded all the plugin"));
                                    break;
                                default:
                                    final XPrisonModuleBase module = plugin.getModuleByName(name);
                                    if (module != null) {
                                        plugin.reloadModule(module);
                                        c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded &f" + name + " &amodule"));
                                    } else {
                                        c.sender().sendMessage(TextUtils.applyColor("&cThe module &6" + c.rawArg(1) + " &cdoesn't exist"));
                                    }
                                    break;
                            }
                        }
                    }
                }).registerAndBind(plugin, commandAliasesArray);
    }
}