package me.drawethree.ultraprisoncore.ranks.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
@Getter
public class Rank {

    private int id;
    private long cost;
    private String prefix;
    private List<String> commandsToExecute;

    public void runCommands(Player p) {
        if (commandsToExecute != null) {
            for (String cmd : commandsToExecute) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige-2%", prefix));
            }
        }
    }

}
