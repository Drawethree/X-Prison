package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.gui.DisbandGangGUI;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangDisbandCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang disband [gang]";
    }

    public GangDisbandCommand(UltraPrisonGangs plugin) {
        super(plugin, "disband", "dis");
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (sender instanceof Player && args.size() == 0) {

            Player player = (Player) sender;
            Optional<Gang> gangOptional = this.plugin.getGangsManager().getPlayerGang(player);

            if (!gangOptional.isPresent()) {
                player.sendMessage(this.plugin.getMessage("not-in-gang"));
                return false;
            }

            Gang gang = gangOptional.get();

            if (!gang.isOwner(player)) {
                player.sendMessage(this.plugin.getMessage("gang-not-owner"));
                return false;
            }

            new DisbandGangGUI(this.plugin,player).open();
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
