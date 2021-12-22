package me.drawethree.ultraprisoncore.autominer;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPI;
import me.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPIImpl;
import me.drawethree.ultraprisoncore.autominer.api.events.PlayerAutoMinerTimeReceiveEvent;
import me.drawethree.ultraprisoncore.autominer.api.events.PlayerAutomineEvent;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {


	public static final String TABLE_NAME = "UltraPrison_AutoMiner";
    public static final String MODULE_NAME = "Auto Miner";

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

    @Getter
    private UltraPrisonAutoMinerAPI api;

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
        this.api = new UltraPrisonAutoMinerAPIImpl(this);
    }

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.saveAutoMiner(e.getPlayer(), true)).bindWith(this.core);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.loadAutoMiner(e.getPlayer())).bindWith(this.core);
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(this::loadAutoMiner);
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
        int blocksBroken = getConfig().get().getInt("auto-miner-region.blocks-broken");

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

        this.region = new AutoMinerRegion(this, world, optRegion.get(), rewards, seconds, blocksBroken);
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
        return MODULE_NAME;
    }

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case SQLITE:
			case MYSQL: {
				return new String[]{"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, time int, primary key (UUID))"};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerCommands() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), messages.get("auto_miner_time").replace("%time%", this.getTimeLeft(c.sender())));
					}
				}).registerAndBind(core, "miner", "autominer");
		Commands.create()
				.assertPermission("ultraprison.autominer.admin")
				.handler(c -> {
					if (c.args().size() == 4 && c.rawArg(0).equalsIgnoreCase("give")) {
						Player target = c.arg(1).parseOrFail(Player.class);
						long time = c.arg(2).parseOrFail(Long.class);

						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ",")));
							return;
                        }

                        this.givePlayerAutoMinerTime(c.sender(), target, time, timeUnit);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
    }

    private void givePlayerAutoMinerTime(CommandSender sender, Player p, long time, TimeUnit unit) {

		if (p == null || !p.isOnline()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&cPlayer is not online!"));
			return;
		}

		int currentTime = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
		currentTime += unit.toSeconds(time);

		autoMinerTimes.put(p.getUniqueId(), currentTime);

		this.callAutoMinerTimeReceiveEvent(p, time, unit);

		PlayerUtils.sendMessage(sender, messages.get("auto_miner_time_add").replace("%time%", String.valueOf(time)).replace("%timeunit%", unit.name()).replace("%player%", p.getName()));
	}

	private PlayerAutoMinerTimeReceiveEvent callAutoMinerTimeReceiveEvent(Player p, long time, TimeUnit unit) {
		PlayerAutoMinerTimeReceiveEvent event = new PlayerAutoMinerTimeReceiveEvent(p, unit, time);
		Events.callSync(event);
		return event;
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

        int timeLeft = autoMinerTimes.get(p.getUniqueId());

        long days = timeLeft / (24 * 60 * 60);
        timeLeft -= days * (24 * 60 * 60);

        long hours = timeLeft / (60 * 60);
        timeLeft -= hours * (60 * 60);

        long minutes = timeLeft / (60);
        timeLeft -= minutes * (60);

        long seconds = timeLeft;

        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }


    private void toggleAutoMiner(Player sender) {
        if (disabledAutoMiner.contains(sender.getUniqueId())) {
			PlayerUtils.sendMessage(sender, getMessage("autominer_enabled"));
            disabledAutoMiner.remove(sender.getUniqueId());
        } else {
			PlayerUtils.sendMessage(sender, getMessage("autominer_disabled"));
            disabledAutoMiner.add(sender.getUniqueId());
        }
    }

    public boolean hasAutominerOff(Player p) {
        return disabledAutoMiner.contains(p.getUniqueId());
    }

	public int getAutoMinerTime(Player player) {
		return this.autoMinerTimes.getOrDefault(player.getUniqueId(), 0);
	}

	public boolean isInAutoMinerRegion(Player player) {
		if (this.region == null) {
			return false;
		}
		return this.region.getRegion().contains(player.getLocation());
	}

	public PlayerAutomineEvent callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = new PlayerAutomineEvent(p, this.getAutoMinerTime(p));
		Events.callSync(event);
		return event;
	}
}
