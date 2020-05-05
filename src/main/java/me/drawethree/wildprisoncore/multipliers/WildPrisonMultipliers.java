package me.drawethree.wildprisoncore.multipliers;

import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.multipliers.api.WildPrisonMultipliersAPI;
import me.drawethree.wildprisoncore.multipliers.api.WildPrisonMultipliersAPIImpl;
import me.drawethree.wildprisoncore.multipliers.multiplier.GlobalMultiplier;
import me.drawethree.wildprisoncore.multipliers.multiplier.Multiplier;
import me.drawethree.wildprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public final class WildPrisonMultipliers {

    private static Multiplier GLOBAL_MULTIPLIER = new GlobalMultiplier(0.0, -1);

    @Getter
    private static WildPrisonMultipliers instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private WildPrisonMultipliersAPI api;

    private HashMap<UUID, Multiplier> rankMultipliers;
    private HashMap<UUID, Multiplier> personalMultipliers;

    private HashMap<String, String> messages;
    private LinkedHashMap<String, Double> permissionToMultiplier;

    @Getter
    private WildPrisonCore core;

    public WildPrisonMultipliers(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("multipliers.yml").copyDefaults(true).save();
        this.rankMultipliers = new HashMap<>();
        this.personalMultipliers = new HashMap<>();
        this.loadMessages();
        this.loadRankMultipliers();
    }


    private void loadRankMultipliers() {
        permissionToMultiplier = new LinkedHashMap<>();
        for (String rank : getConfig().get().getConfigurationSection("ranks").getKeys(false)) {
            String perm = "multiplier." + rank;
            double multiplier = getConfig().get().getDouble("ranks." + rank);
            permissionToMultiplier.put(perm, multiplier);
            core.getLogger().info("Loaded rank multiplier." + rank + " with multiplier " + multiplier);
        }
    }


    public void enable() {
        api = new WildPrisonMultipliersAPIImpl(this);
        this.registerCommands();
        this.registerEvents();
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    rankMultipliers.put(e.getPlayer().getUniqueId(), this.calculateRankMultiplier(e.getPlayer()));
                }).bindWith(core);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    rankMultipliers.remove(e.getPlayer().getUniqueId());
                }).bindWith(core);
    }


    public void disable() {

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    private void registerCommands() {
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 2) {
                        double amount = c.arg(0).parseOrFail(Double.class).doubleValue();
                        int minutes = c.arg(1).parseOrFail(Integer.class).intValue();
                        setupGlobalMultiplier(c.sender(), minutes, amount);
                    }
                }).registerAndBind(core, "globalmultiplier", "gmulti");
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3) {
                        Player onlinePlayer = Players.getNullable(c.rawArg(0));
                        double amount = c.arg(1).parseOrFail(Double.class).doubleValue();
                        int minutes = c.arg(2).parseOrFail(Integer.class).intValue();
                        setupPersonalMultiplier(c.sender(), onlinePlayer, amount, minutes);
                    }
                }).registerAndBind(core, "personalmultiplier", "pmulti");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    c.sender().sendMessage(messages.get("global_multi").replace("%multiplier%", String.valueOf(GLOBAL_MULTIPLIER.getMultiplier())).replace("%duration%", GLOBAL_MULTIPLIER.getTimeLeft()));
                    c.sender().sendMessage(messages.get("rank_multi").replace("%multiplier%", String.valueOf(rankMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier())));
                    c.sender().sendMessage(messages.get("vote_multi").replace("%multiplier%", String.valueOf(personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier())).replace("%duration%", personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getTimeLeft()));
                }).registerAndBind(core, "multiplier", "multi");
    }

    private void setupPersonalMultiplier(CommandSender sender, Player onlinePlayer, double amount, int minutes) {
        if (amount > 1.5) {
            sender.sendMessage(Text.colorize("&cPersonal multiplier must be max 1.5!"));
            return;
        }
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer must be online!"));
            return;
        }

        if (personalMultipliers.containsValue(onlinePlayer.getUniqueId())) {
            Multiplier multiplier = personalMultipliers.get(onlinePlayer.getUniqueId());
            multiplier.setMultiplier(amount);
            multiplier.setDuration(minutes);
        } else {
            personalMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplier(onlinePlayer.getUniqueId(), amount, minutes));
        }

        onlinePlayer.sendMessage(messages.get("personal_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%minutes%", String.valueOf(minutes)));
        sender.sendMessage(Text.colorize(String.format("&aYou have set &e%s's &ePersonal Multiplier &ato &e%.2f &afor &e%d &aminutes.", onlinePlayer.getName(), amount, minutes)));
    }


    private void setupGlobalMultiplier(CommandSender sender, int time, double amount) {
        if (amount > 3.0) {
            sender.sendMessage(Text.colorize("&cGlobal multiplier must be max 3.0!"));
            return;
        }

        GLOBAL_MULTIPLIER.setMultiplier(amount);
        GLOBAL_MULTIPLIER.setDuration(time);
        sender.sendMessage(Text.colorize(String.format("&aYou have set the &eGlobal Multiplier &ato &e%.2f &afor &e%d &aminutes.", amount, time)));
    }


    public double getGlobalMultiplier() {
        return GLOBAL_MULTIPLIER.getMultiplier();
    }

    public double getPersonalMultiplier(Player p) {
        return personalMultipliers.getOrDefault(p.getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier();
    }

    public double getRankMultiplier(Player p) {
        return rankMultipliers.getOrDefault(p.getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier();
    }

    public void removePersonalMultiplier(UUID uuid) {
        personalMultipliers.remove(uuid);
    }


    private Multiplier calculateRankMultiplier(Player p) {
        for (String perm : permissionToMultiplier.keySet()) {
            if (p.hasPermission(perm)) {
                return new PlayerMultiplier(p.getUniqueId(), permissionToMultiplier.get(perm), -1);
            }
        }
        return new PlayerMultiplier(p.getUniqueId(), 0.0, -1);
    }
}
