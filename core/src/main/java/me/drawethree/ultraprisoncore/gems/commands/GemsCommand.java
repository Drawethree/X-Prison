package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import org.bukkit.command.CommandSender;

public abstract class GemsCommand {

    protected UltraPrisonGems plugin;

    GemsCommand(UltraPrisonGems plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public abstract boolean canExecute(CommandSender sender);

}
