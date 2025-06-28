package dev.drawethree.xprison.prestiges.manager;

import dev.drawethree.xprison.api.prestiges.events.PlayerPrestigeEvent;
import dev.drawethree.xprison.api.shared.currency.enums.LostCause;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.prestiges.config.PrestigeConfig;
import dev.drawethree.xprison.prestiges.model.PrestigeImpl;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.ranks.manager.RanksManager;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class PrestigeManager {

    private final XPrisonPrestiges plugin;

    private final Map<UUID, Long> onlinePlayersPrestige;
    private final List<UUID> prestigingPlayers;

    public PrestigeManager(XPrisonPrestiges plugin) {
        this.plugin = plugin;
        this.onlinePlayersPrestige = new ConcurrentHashMap<>();
        this.prestigingPlayers = new ArrayList<>(10);
    }


    private void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersPrestige.keySet()) {
            this.plugin.getPrestigeService().setPrestige(Players.getOfflineNullable(uuid), onlinePlayersPrestige.get(uuid));
        }
        info("&aPrestiges saved.");
    }

    private void loadAllData() {
        for (Player p : Players.all()) {
            loadPlayerPrestige(p);
        }
    }

	public void savePlayerData(Player player, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> savePlayerDataLogic(player, removeFromCache));
		} else {
			savePlayerDataLogic(player, removeFromCache);
		}
	}

	private void savePlayerDataLogic(Player player, boolean removeFromCache) {
        this.plugin.getPrestigeService().setPrestige(player, this.getPlayerPrestige(player).getId());
		if (removeFromCache) {
			this.onlinePlayersPrestige.remove(player.getUniqueId());
		}
        this.plugin.getCore().debug("Saved " + player.getName() + "'s prestige to database.", this.plugin);
	}


    public void loadPlayerPrestige(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getPrestigeService().createPrestige(player);
            long prestige = this.plugin.getPrestigeService().getPlayerPrestige(player);
            this.onlinePlayersPrestige.put(player.getUniqueId(), prestige);
            this.plugin.getCore().debug("Loaded " + player.getName() + "'s prestige.", this.plugin);
        });
    }

    private double calculateNextPrestigeCost(PrestigeImpl origin) {

        if (!this.getConfig().isIncreaseCostEnabled()) {
            return origin.getCost();
        }

        return origin.getId() == 0 ? (this.getConfig().getUnlimitedPrestigeCost()) : (origin.getCost() * this.getConfig().getIncreaseCostBy());
    }

    private PrestigeConfig getConfig() {
        return this.plugin.getPrestigeConfig();
    }


    public PrestigeImpl getNextPrestige(PrestigeImpl prestigeImpl) {
        if (this.getConfig().isUnlimitedPrestiges()) {
            return new PrestigeImpl(prestigeImpl.getId() + 1, this.calculateNextPrestigeCost(prestigeImpl), this.getConfig().getUnlimitedPrestigePrefix().replace("%prestige%", String.format("%,d", prestigeImpl.getId() + 1)), this.getConfig().getUnlimitedPrestigesRewards().getOrDefault(prestigeImpl.getId() + 1, null));
        }
        return this.getConfig().getPrestigeById().getOrDefault(prestigeImpl.getId() + 1, null);
    }

    public PrestigeImpl getPrestigeById(long id) {
        if (this.getConfig().isUnlimitedPrestiges()) {
            return new PrestigeImpl(id, id * this.getConfig().getIncreaseCostBy(), this.getConfig().getUnlimitedPrestigePrefix().replace("%prestige%", String.format("%,d", id)), this.getConfig().getUnlimitedPrestigesRewards().getOrDefault(id, null));
        }
        return this.getConfig().getPrestigeById().getOrDefault(id, null);
    }

    public synchronized PrestigeImpl getPlayerPrestige(Player p) {
        if (this.getConfig().isUnlimitedPrestiges()) {
            long prestige = this.onlinePlayersPrestige.getOrDefault(p.getUniqueId(), 0L);
            return new PrestigeImpl(prestige, this.calculatePrestigeCost(prestige), this.getConfig().getUnlimitedPrestigePrefix().replace("%prestige%", String.format("%,d", prestige)), null);
        } else {
            return this.getConfig().getPrestigeById().getOrDefault(this.onlinePlayersPrestige.get(p.getUniqueId()), this.getConfig().getPrestigeById().get(0L));
        }
    }

    private double calculatePrestigeCost(long prestige) {

        if (!this.getConfig().isIncreaseCostEnabled()) {
            return this.getConfig().getUnlimitedPrestigeCost();
        }

        double origin = this.getConfig().getUnlimitedPrestigeCost();

        for (long i = 0; i < prestige; i++) {
            if (i == 0) {
                continue;
            }
            origin = origin * this.getConfig().getIncreaseCostBy();
        }

        return origin;
    }

    public boolean isMaxPrestige(Player p) {
        if (this.getConfig().isUnlimitedPrestiges()) {
            return this.getPlayerPrestige(p).getId() >= this.getConfig().getUnlimitedPrestigeMax();
        }
        return this.getPlayerPrestige(p).getId() == this.getConfig().getMaxPrestige().getId();
    }

    private boolean completeTransaction(Player p, double cost) {
		if (this.getConfig().isUseTokensCurrency()) {
            this.plugin.getCore().getTokens().getApi().remove(p, (long) cost, LostCause.RANKUP);
            return true;
        } else {
            return this.plugin.getCore().getEconomy().withdrawPlayer(p, cost).transactionSuccess();
        }
    }

    private boolean isTransactionAllowed(Player p, double cost) {
        if (this.getConfig().isUseTokensCurrency()) {
            return this.plugin.getCore().getTokens().getApi().hasEnough(p, (long) cost);
        } else {
            return this.plugin.getCore().getEconomy().has(p, cost);
        }

    }

    public boolean buyNextPrestige(Player p) {

        if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
            PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_last_rank"));
            return false;
        }

        if (isMaxPrestige(p)) {
            PlayerUtils.sendMessage(p, this.getConfig().getMessage("last_prestige"));
            return false;
        }

        PrestigeImpl currentPrestigeImpl = this.getPlayerPrestige(p);
        PrestigeImpl toBuy = getNextPrestige(currentPrestigeImpl);

        if (!this.isTransactionAllowed(p, toBuy.getCost())) {
            if (this.getConfig().isUseTokensCurrency()) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_tokens_prestige").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            } else {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            }
            return false;
        }

        PlayerPrestigeEvent event = new PlayerPrestigeEvent(p, currentPrestigeImpl, toBuy);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("PlayerPrestigeEvent was cancelled.", this.plugin);
            return false;
        }

        doPrestige(p, toBuy);

        PlayerUtils.sendMessage(p, this.getConfig().getMessage("prestige_up").replace("%Prestige%", toBuy.getPrefix()));

        return true;
    }

    public void sendPrestigeTop(CommandSender sender) {

        List<String> prestigeTopFormat = this.getConfig().getPrestigeTopFormat();
        Map<UUID, Long> topPrestige = this.plugin.getPrestigeService().getTopPrestiges(this.getConfig().getTopPlayersAmount());

		for (String s : prestigeTopFormat) {
            if (s.startsWith("{FOR_EACH_PLAYER}")) {
                String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
                for (int i = 0; i < 10; i++) {
                    try {
                        UUID uuid = (UUID) topPrestige.keySet().toArray()[i];
                        OfflinePlayer player = Players.getOfflineNullable(uuid);
                        String name;
                        if (player.getName() == null) {
                            name = "Unknown Player";
                        } else {
                            name = player.getName();
                        }
                        long prestige = topPrestige.get(uuid);
                        PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%prestige%", String.format("%,d", prestige)));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    } catch (Exception e) {
                        error("Exception during sending PrestigeTop to " + sender.getName());
                        e.printStackTrace();
                    }
                }
            } else {
                PlayerUtils.sendMessage(sender, s);
            }
        }
    }

    public void buyMaxPrestige(Player p) {

        Schedulers.async().run(() -> {
            if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_last_rank"));
                return;
            }

            if (isMaxPrestige(p)) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("last_prestige"));
                return;
            }

            PrestigeImpl startPrestigeImpl = this.getPlayerPrestige(p);

            PrestigeImpl currentPrestigeImpl = startPrestigeImpl;

            PrestigeImpl nextPrestigeImpl = this.getNextPrestige(startPrestigeImpl);

            if (!this.isTransactionAllowed(p, nextPrestigeImpl.getCost())) {
                if (this.getConfig().isUseTokensCurrency()) {
                    PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_tokens_prestige").replace("%cost%", String.format("%,.0f", nextPrestigeImpl.getCost())));
                } else {
                    PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", nextPrestigeImpl.getCost())));
                }
                return;
            }

            PlayerUtils.sendMessage(p, this.getConfig().getMessage("max_prestige_started"));

            this.prestigingPlayers.add(p.getUniqueId());
            while (p.isOnline() && !isMaxPrestige(p) && this.isTransactionAllowed(p, nextPrestigeImpl.getCost())) {

                if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
                    break;
                }

                doPrestige(p, nextPrestigeImpl);
                currentPrestigeImpl = nextPrestigeImpl;
                nextPrestigeImpl = this.getNextPrestige(nextPrestigeImpl);
            }

            PlayerPrestigeEvent event = new PlayerPrestigeEvent(p, startPrestigeImpl, currentPrestigeImpl);

            Events.callSync(event);

            this.prestigingPlayers.remove(p.getUniqueId());

            if (startPrestigeImpl.getId() < this.onlinePlayersPrestige.get(p.getUniqueId())) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("max_prestige_done").replace("%start_prestige%", String.format("%,d", startPrestigeImpl.getId())).replace("%prestige%", String.format("%,d", this.onlinePlayersPrestige.get(p.getUniqueId()))));
            }
        });
    }

    private void doPrestige(Player p, PrestigeImpl nextPrestigeImpl) {

        if (!this.completeTransaction(p, nextPrestigeImpl.getCost())) {
            return;
        }

        this.onlinePlayersPrestige.put(p.getUniqueId(), nextPrestigeImpl.getId());

        givePrestigeRewards(nextPrestigeImpl,p);

        List<String> rewardsPerPrestige = this.getConfig().getUnlimitedPrestigesRewardPerPrestige();
        if (rewardsPerPrestige != null) {
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> rewardsPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
            } else {
                rewardsPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
            }
        }

        if (this.getConfig().isResetRankAfterPrestige() && areRanksEnabled()) {
            getRankManager().resetPlayerRank(p);
        }
    }

    public void addPlayerPrestige(CommandSender sender, Player target, int amount) {

        if (0 > amount) {
            return;
        }

        if (isMaxPrestige(target)) {
            return;
        }

        PrestigeImpl startPrestigeImpl = this.getPlayerPrestige(target);

        long maxPrestige = this.getConfig().isUnlimitedPrestiges() ? this.getConfig().getUnlimitedPrestigeMax() : this.getConfig().getMaxPrestige().getId();
        if (startPrestigeImpl.getId() + amount > maxPrestige) {
            this.onlinePlayersPrestige.put(target.getUniqueId(), maxPrestige);
        } else {
            this.onlinePlayersPrestige.put(target.getUniqueId(), this.onlinePlayersPrestige.get(target.getUniqueId()) + amount);
        }

        PrestigeImpl currentPrestigeImpl = this.getPlayerPrestige(target);

        long prestigeGained = currentPrestigeImpl.getId() - startPrestigeImpl.getId();

        for (int i = 0; i < prestigeGained; i++) {
            PrestigeImpl toGive = this.getPrestigeById(currentPrestigeImpl.getId() + 1 + i);

            if (toGive == null) {
                break;
            }

            givePrestigeRewards(toGive,target);

            List<String> rewardsPerPrestige = this.getConfig().getUnlimitedPrestigesRewardPerPrestige();
            if (rewardsPerPrestige != null) {
                if (!Bukkit.isPrimaryThread()) {
                    Schedulers.sync().run(() -> rewardsPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", target.getName()))));
                } else {
                    rewardsPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", target.getName())));
                }
            }
        }

        PlayerPrestigeEvent event = new PlayerPrestigeEvent(target, startPrestigeImpl, currentPrestigeImpl);

        Events.callSync(event);

        PlayerUtils.sendMessage(sender, this.getConfig().getMessage("prestige_add").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public void setPlayerPrestige(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        long maxPretige = this.getConfig().isUnlimitedPrestiges() ? this.getConfig().getUnlimitedPrestigeMax() : this.getConfig().getMaxPrestige().getId();

        this.onlinePlayersPrestige.put(target.getUniqueId(), Math.min(amount, maxPretige));

        if (sender != null) {
            PlayerUtils.sendMessage(sender, this.getConfig().getMessage("prestige_set").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
        }
    }

    public void removePlayerPrestige(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        PrestigeImpl currentPrestigeImpl = this.getPlayerPrestige(target);

        if (currentPrestigeImpl.getId() - amount < 0) {
            this.onlinePlayersPrestige.put(target.getUniqueId(), 0L);
        } else {
            this.onlinePlayersPrestige.put(target.getUniqueId(), this.onlinePlayersPrestige.get(target.getUniqueId()) - amount);
        }

        PlayerUtils.sendMessage(sender, this.getConfig().getMessage("prestige_remove").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public int getPrestigeProgress(Player player) {

        if (this.isMaxPrestige(player)) {
            return 100;
        }

        PrestigeImpl current = this.getPlayerPrestige(player);
        PrestigeImpl next = this.getNextPrestige(current);

        double currentBalance = this.getConfig().isUseTokensCurrency() ? this.plugin.getCore().getTokens().getApi().getAmount(player) : this.plugin.getCore().getEconomy().getBalance(player);

        int progress = (int) ((currentBalance / next.getCost()) * 100);

        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    private boolean areRanksEnabled() {
        return this.plugin.getCore().isModuleEnabled(XPrisonRanks.MODULE_NAME);
    }

    private RanksManager getRankManager() {
        if (!areRanksEnabled()) {
            throw new IllegalStateException("Ranks module is not enabled");
        }
        return this.plugin.getCore().getRanks().getRanksManager();
    }

    public boolean isPrestiging(Player sender) {
        return this.prestigingPlayers.contains(sender.getUniqueId());
    }

    public void givePrestigeRewards(PrestigeImpl prestigeImpl, Player p) {
        if (prestigeImpl.getCommandsToExecute() != null) {
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> executeCommands(prestigeImpl, p));
            } else {
                executeCommands(prestigeImpl, p);
            }
        }
    }

    private void executeCommands(PrestigeImpl prestigeImpl, Player p) {
        for (String cmd : prestigeImpl.getCommandsToExecute()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige%", prestigeImpl.getPrefix()));
        }
    }

    public void enable() {
        this.loadAllData();
    }

	public void disable() {
		this.saveAllDataSync();
	}
}
