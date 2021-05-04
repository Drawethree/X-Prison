package me.drawethree.ultraprisoncore.autominer;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {


    @Getter
    private static UltraPrisonAutoMiner instance;
    @Getter
    private FileManager.Config config;

    private HashMap<String, String> messages;

    private HashMap<UUID, Integer> autoMinerTimes;

    @Getter
    private AutoMinerRegion region;
    @Getter
    private UltraPrisonCore core;
    private List<UUID> disabledAutoMiner;
    private boolean enabled;

    public UltraPrisonAutoMiner(UltraPrisonCore UltraPrisonCore) {
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
        this.loadAutoMinerRegion();
    }

    @Override
    public void enable() {
        this.enabled = true;

        instance = this;
        this.config = this.core.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();

        this.autoMinerTimes = new HashMap<>();
        this.disabledAutoMiner = new ArrayList<>();
        this.registerCommands();
        this.registerEvents();
        this.loadMessages();
        this.removeExpiredAutoMiners();
        this.loadAutoMinerRegion();
        this.loadPlayersAutoMiner();
    }

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.saveAutoMiner(e.getPlayer(), true)).bindWith(this.core);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.loadAutoMiner(e.getPlayer())).bindWith(this.core);
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(p -> loadAutoMiner(p));
    }

    private void removeExpiredAutoMiners() {
        Schedulers.async().run(() -> {
            this.core.getPluginDatabase().removeExpiredAutoMiners();
            this.core.getLogger().info("Removed expired AutoMiners from database");
        });
    }

    private void loadAutoMiner(Player p) {
        Schedulers.async().run(() -> {
            int timeLeft = this.core.getPluginDatabase().getPlayerAutoMinerTime(p);
            this.autoMinerTimes.put(p.getUniqueId(), timeLeft);
            this.core.getLogger().info(String.format("Loaded %s's AutoMiner Time.", p.getName()));
        });
    }

    private void saveAutoMiner(Player p, boolean async) {

        int timeLeft = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);

        if (async) {
            Schedulers.async().run(() -> {
                this.core.getPluginDatabase().saveAutoMiner(p, timeLeft);
                this.autoMinerTimes.remove(p.getUniqueId());
                this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
            });
        } else {
            this.core.getPluginDatabase().saveAutoMiner(p, timeLeft);
            this.autoMinerTimes.remove(p.getUniqueId());
            this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
        }
    }

    private void loadAutoMinerRegion() {
        String worldName = getConfig().get().getString("auto-miner-region.world");
        String regionName = getConfig().get().getString("auto-miner-region.name");

        List<String> rewards = getConfig().get().getStringList("auto-miner-region.rewards");

        int seconds = getConfig().get().getInt("auto-miner-region.reward-period");

        World world = Bukkit.getWorld(worldName);

		if (world == null) {
			return;
		}

		if (seconds <= 0) {
			core.getLogger().warning("reward-perion in autominer.yml needs to be greater than 0!");
			return;
        }

		Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world, regionName);

        if (!optRegion.isPresent()) {
            core.getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world.getName()));
            return;
        }

		this.region = new AutoMinerRegion(this, world, optRegion.get(), rewards, seconds);
        core.getLogger().info("AutoMiner region loaded!");

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    @Override
    public void disable() {
        Players.all().forEach(p -> saveAutoMiner(p, false));
        this.enabled = false;

    }

    @Override
    public String getName() {
        return "Auto Miner";
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("auto_miner_time").replace("%time%", this.getTimeLeft(c.sender())));
                    }
                }).registerAndBind(core, "miner", "autominer");
        Commands.create()
				.assertPermission("ultraprison.autominer.admin")
				.handler(c -> {
                    if (c.args().size() == 3 && c.rawArg(0).equalsIgnoreCase("give")) {
                        Player target = Players.getNullable(c.rawArg(1));
                        int time = c.arg(2).parseOrFail(Integer.class).intValue();
                        givePlayerAutoMinerTime(c.sender(), target, time);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
	}

    private void givePlayerAutoMinerTime(CommandSender sender, Player p, long time) {

        if (p == null || !p.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer is not online!"));
            return;
        }

        int currentTime = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
        currentTime += time;

        autoMinerTimes.put(p.getUniqueId(), currentTime);
        sender.sendMessage(messages.get("auto_miner_time_add").replace("%time%", String.valueOf(time)).replace("%player%", p.getName()));
    }

    public boolean hasAutoMinerTime(Player p) {
        return autoMinerTimes.containsKey(p.getUniqueId()) && autoMinerTimes.get(p.getUniqueId()) > 0;
    }

    public void decrementTime(Player p) {
        int newAmount = autoMinerTimes.get(p.getUniqueId()) - 1;
        autoMinerTimes.put(p.getUniqueId(), newAmount);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    public String getTimeLeft(Player p) {

        if (!autoMinerTimes.containsKey(p.getUniqueId())) {
            return "0s";
        }

        int secondsLeft = autoMinerTimes.get(p.getUniqueId());
        int timeLeft = secondsLeft;

        long days = timeLeft / (24 * 60 * 60);
        timeLeft -= days * (24 * 60 * 60);

        long hours = timeLeft / (60 * 60);
        timeLeft -= hours * (60 * 60);

        long minutes = timeLeft / (60);
        timeLeft -= minutes * (60);

        long seconds = timeLeft;

        timeLeft -= seconds;

        return new StringBuilder().append(days).append("d ").append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s").toString();
    }


    private void toggleAutoMiner(Player sender) {
        if (disabledAutoMiner.contains(sender.getUniqueId())) {
            sender.sendMessage(getMessage("autominer_enabled"));
            disabledAutoMiner.remove(sender.getUniqueId());
        } else {
            sender.sendMessage(getMessage("autominer_disabled"));
            disabledAutoMiner.add(sender.getUniqueId());
        }
    }

    public boolean hasAutominerOff(Player p) {
        return disabledAutoMiner.contains(p.getUniqueId());
    }
}
