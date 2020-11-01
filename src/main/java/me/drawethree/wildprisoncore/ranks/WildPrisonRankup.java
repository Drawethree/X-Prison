package me.drawethree.wildprisoncore.ranks;

import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.ranks.api.WildPrisonRankupAPI;
import me.drawethree.wildprisoncore.ranks.api.WildPrisonRankupAPIImpl;
import me.drawethree.wildprisoncore.ranks.manager.RankManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
public final class WildPrisonRankup {

    @Getter
    private FileManager.Config config;

    private RankManager rankManager;

    @Getter
    private WildPrisonRankupAPI api;

    private HashMap<String, String> messages;

    private List<UUID> prestigingPlayers;

    @Getter
    private WildPrisonCore core;

    public WildPrisonRankup(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("ranks.yml").copyDefaults(true).save();
    }

    public void enable() {
        this.loadMessages();
        this.rankManager = new RankManager(this);
        api = new WildPrisonRankupAPIImpl(this);
        this.prestigingPlayers = new ArrayList<>(10);
        this.registerCommands();
        this.rankManager.loadAllData();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(this.getConfig().get().getString("messages." + key)));
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key.toLowerCase(), Text.colorize("&cMessage " + key + " not found."));
    }


    public void disable() {
        this.rankManager.stopUpdating();
        this.rankManager.saveAllDataSync();
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextRank(c.sender());
                    }
                }).registerAndBind(core, "rankup");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        boolean buy = this.rankManager.buyNextRank(c.sender());
                        while (buy) {
                            buy = this.rankManager.buyNextRank(c.sender());
                        }
                    }
                }).registerAndBind(core, "maxrankup", "mru");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextPrestige(c.sender());
					} /*else if (c.args().size() == 1) {
                        int amountOfLevels = c.arg(0).parseOrFail(Integer.class);
                        this.rankManager.buyPrestige(c.sender(), amountOfLevels);
                    }
                    */
                }).registerAndBind(core, "prestige");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {

                        if (this.prestigingPlayers.contains(c.sender().getUniqueId())) {
                            return;
                        }

                        Schedulers.async().run(() -> {
                            this.prestigingPlayers.add(c.sender().getUniqueId());
                            this.rankManager.buyMaxPrestige(c.sender());
                            this.prestigingPlayers.remove(c.sender().getUniqueId());
                        });
                    }
                }).registerAndBind(core, "maxprestige", "maxp", "mp");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.sendPrestigeTop(c.sender());
                    }
                }).registerAndBind(core, "prestigetop");
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3) {

                        Player target = c.arg(1).parseOrFail(Player.class);
                        int amount = c.arg(2).parseOrFail(Integer.class);

                        switch (c.rawArg(0).toLowerCase()) {
                            case "set":
                                this.rankManager.setPlayerPrestige(c.sender(), target, amount);
                                break;
                            case "add":
                                this.rankManager.addPlayerPrestige(c.sender(), target, amount);
                                break;
                            case "remove":
                                this.rankManager.removePlayerPrestige(c.sender(), target, amount);
                            default:
                                c.sender().sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                c.sender().sendMessage(Text.colorize("&e&lPRESTIGE ADMIN HELP MENU "));
                                c.sender().sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                c.sender().sendMessage(Text.colorize("&e/prestigeadmin add [player] [amount]"));
                                c.sender().sendMessage(Text.colorize("&e/prestigeadmin remove [player] [amount]"));
                                c.sender().sendMessage(Text.colorize("&e/prestigeadmin set [player] [amount]"));
                                c.sender().sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                break;
                        }
                    }
                }).registerAndBind(core, "prestigeadmin", "prestigea");
    }
}
