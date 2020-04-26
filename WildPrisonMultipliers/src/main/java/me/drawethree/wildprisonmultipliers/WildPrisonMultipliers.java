package me.drawethree.wildprisonmultipliers;

import lombok.Getter;
import me.drawethree.wildprisonmultipliers.api.WildPrisonMultipliersAPI;
import me.drawethree.wildprisonmultipliers.api.WildPrisonMultipliersAPIImpl;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public final class WildPrisonMultipliers extends ExtendedJavaPlugin {

    private static Multiplier GLOBAL_MULTIPLIER = new Multiplier(null, 0.0, -1);

    @Getter
    private static WildPrisonMultipliersAPI api;

    private static HashMap<UUID, Multiplier> rankMultipliers;
    private static HashMap<UUID, Multiplier> personalMultipliers;

    private static HashMap<String, String> messages;
    private static LinkedHashMap<String, Double> permissionToMultiplier;

    @Override
    protected void load() {
        rankMultipliers = new HashMap<>();
        personalMultipliers = new HashMap<>();
        saveDefaultConfig();
        this.loadMessages();
        this.loadRankMultipliers();
    }

    private void loadRankMultipliers() {
        permissionToMultiplier = new LinkedHashMap<>();
        for (String rank : getConfig().getConfigurationSection("ranks").getKeys(false)) {
            String perm = "multiplier." + rank;
            double multiplier = getConfig().getDouble("ranks." + rank);
            permissionToMultiplier.put(perm, multiplier);
            getLogger().info("Loaded rank multiplier." + rank + " with multiplier " + multiplier);
        }
    }

    @Override
    protected void enable() {
        api = new WildPrisonMultipliersAPIImpl(this);
        this.registerCommands();
        this.registerEvents();
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    rankMultipliers.put(e.getPlayer().getUniqueId(), this.calculateRankMultiplier(e.getPlayer()));
                }).bindWith(this);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    rankMultipliers.remove(e.getPlayer().getUniqueId());
                }).bindWith(this);
    }

    @Override
    protected void disable() {

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().getString("messages." + key)));
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
                }).registerAndBind(this, "globalmultiplier", "gmulti");
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3) {
                        Player onlinePlayer = Players.getNullable(c.rawArg(0));
                        double amount = c.arg(1).parseOrFail(Double.class).doubleValue();
                        int minutes = c.arg(2).parseOrFail(Integer.class).intValue();
                        setupPersonalMultiplier(c.sender(), onlinePlayer, amount, minutes);
                    }
                }).registerAndBind(this, "personalmultiplier", "pmulti");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    c.sender().sendMessage(messages.get("global_multi").replace("%multiplier%", String.valueOf(GLOBAL_MULTIPLIER.getMultiplier())).replace("%duration%", GLOBAL_MULTIPLIER.getTimeLeft()));
                    c.sender().sendMessage(messages.get("rank_multi").replace("%multiplier%", String.valueOf(rankMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier())));
                    c.sender().sendMessage(messages.get("vote_multi").replace("%multiplier%", String.valueOf(personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getMultiplier())).replace("%duration%", personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultMultiplier()).getTimeLeft()));
                }).registerAndBind(this, "multiplier", "multi");
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
            personalMultipliers.put(onlinePlayer.getUniqueId(), new Multiplier(onlinePlayer.getUniqueId(), amount, minutes));
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

    public static void removePersonalMultiplier(UUID uuid) {
        personalMultipliers.remove(uuid);
    }



    private Multiplier calculateRankMultiplier(Player p) {
        for (String perm : permissionToMultiplier.keySet()) {
            if (p.hasPermission(perm)) {
                return new Multiplier(p.getUniqueId(), permissionToMultiplier.get(perm), -1);
            }
        }
        return new Multiplier( p.getUniqueId(), 0.0, -1);
    }
}
