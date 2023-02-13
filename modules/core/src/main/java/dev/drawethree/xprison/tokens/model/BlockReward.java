package dev.drawethree.xprison.tokens.model;

import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
@Getter
public class BlockReward {

    private final long blocksRequired;
    private final List<String> commandsToRun;
    private final String message;

    public void giveTo(Player p) {

        if (!Bukkit.isPrimaryThread()) {
            Schedulers.sync().run(() -> executeCommands(p));
        } else {
            executeCommands(p);
        }

        PlayerUtils.sendMessage(p, this.message);
    }

    private void executeCommands(Player p) {
        for (String s : this.commandsToRun) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
        }
    }
}