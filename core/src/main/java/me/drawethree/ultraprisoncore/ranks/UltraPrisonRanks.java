package me.drawethree.ultraprisoncore.ranks;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPI;
import me.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPIImpl;
import me.drawethree.ultraprisoncore.ranks.manager.RankManager;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
public final class UltraPrisonRanks implements UltraPrisonModule {

    public static final String TABLE_NAME = "UltraPrison_Ranks";
    public static final String MODULE_NAME = "Ranks & Prestiges";

    @Getter
    private FileManager.Config config;

    private RankManager rankManager;

    @Getter
    private UltraPrisonRanksAPI api;

    private HashMap<String, String> messages;

    private List<UUID> prestigingPlayers;

    @Getter
    private UltraPrisonCore core;
    private boolean enabled;

    public UltraPrisonRanks(UltraPrisonCore UltraPrisonCore) {
        this.core = UltraPrisonCore;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.config.reload();
        this.loadMessages();
        this.rankManager.reload();
    }

    @Override
    public void enable() {
        this.enabled = true;

        this.config = this.core.getFileManager().getConfig("ranks.yml").copyDefaults(true).save();

        this.loadMessages();
        this.rankManager = new RankManager(this);
        api = new UltraPrisonRanksAPIImpl(this);
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


    @Override
    public void disable() {
        this.rankManager.stopUpdating();
        this.rankManager.saveAllDataSync();
        this.enabled = false;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public String[] getTables() {
        return new String[]{TABLE_NAME};
    }

    @Override
    public String[] getCreateTablesSQL(DatabaseType type) {
        switch (type) {
            case MYSQL:
            case SQLITE: {
                return new String[]{
                        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_rank int, id_prestige bigint, primary key (UUID))"
                };
            }
            default:
                throw new IllegalStateException("Unsupported Database type: " + type);
        }
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
                .assertPermission("ultraprison.ranks.admin")
                .handler(c -> {
                    if (c.args().size() == 2) {
                        Player target = c.arg(0).parseOrFail(Player.class);
                        Rank rank = this.getRankManager().getRankById(c.arg(1).parseOrFail(Integer.class));

                        if (rank == null) {
                            PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid rank id provided."));
                            return;
                        }

                        this.rankManager.setRank(target, rank, c.sender());
                    }
                }).registerAndBind(core, "setrank");

        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyMaxRank(c.sender());
                    }
                }).registerAndBind(core, "maxrankup", "mru");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextPrestige(c.sender());
                    }
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
                .assertPermission("ultraprison.ranks.admin")
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
                                break;
                            default:
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e&lPRESTIGE ADMIN HELP MENU "));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e/prestigeadmin add [player] [amount]"));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e/prestigeadmin remove [player] [amount]"));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e/prestigeadmin set [player] [amount]"));
                                PlayerUtils.sendMessage(c.sender(), Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
                                break;
                        }
                    }
                }).registerAndBind(core, "prestigeadmin", "prestigea");
    }
}
