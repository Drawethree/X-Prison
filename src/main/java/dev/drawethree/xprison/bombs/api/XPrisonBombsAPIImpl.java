package dev.drawethree.xprison.bombs.api;

import dev.drawethree.xprison.api.bombs.XPrisonBombsAPI;
import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.XPrisonBombs;
import org.bukkit.entity.Player;

import java.util.Optional;

public class XPrisonBombsAPIImpl implements XPrisonBombsAPI {

    private final XPrisonBombs module;

    public XPrisonBombsAPIImpl(XPrisonBombs module) {
        this.module = module;
    }

    @Override
    public void giveBomb(Bomb bomb, int amount, Player player) {
        this.module.getBombsService().giveBomb(bomb, amount, player);
    }

    @Override
    public Optional<Bomb> getBombByName(String name) {
        return this.module.getBombsRepository().getBombByName(name);
    }
}