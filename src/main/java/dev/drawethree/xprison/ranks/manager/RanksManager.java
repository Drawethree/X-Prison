package dev.drawethree.xprison.ranks.manager;

import dev.drawethree.xprison.api.ranks.events.PlayerRankUpEvent;
import dev.drawethree.xprison.api.ranks.model.Rank;
import dev.drawethree.xprison.api.shared.currency.enums.LostCause;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.prestiges.manager.PrestigeManager;
import dev.drawethree.xprison.prestiges.model.PrestigeImpl;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.ranks.model.RankImpl;
import dev.drawethree.xprison.utils.misc.ProgressBar;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class RanksManager {

    private final XPrisonRanks plugin;
    private final Map<UUID, Integer> onlinePlayersRanks;

    public RanksManager(XPrisonRanks plugin) {
        this.plugin = plugin;
        this.onlinePlayersRanks = new ConcurrentHashMap<>();
    }

    private void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersRanks.keySet()) {
            this.plugin.getRanksService().setRank(Players.getOfflineNullable(uuid), onlinePlayersRanks.get(uuid));
        }
        info("&aRanks saved.");
    }

    private void loadAllData() {
        loadPlayerRank(Players.all());
    }

    public void savePlayerRank(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getRanksService().setRank(player, this.getPlayerRank(player).getId());
            this.onlinePlayersRanks.remove(player.getUniqueId());
            this.plugin.getCore().debug("Saved " + player.getName() + "'s rank.", this.plugin);
        });
    }

    public void loadPlayerRank(Collection<Player> players) {
        Schedulers.async().run(() -> {
            for (Player player : players) {
                this.plugin.getRanksService().createRank(player);
                int rank = this.plugin.getRanksService().getPlayerRank(player);
                this.onlinePlayersRanks.put(player.getUniqueId(), rank);
                this.plugin.getCore().debug("Loaded " + player.getName() + "'s rank.", this.plugin);
            }
        });
    }

    public Optional<RankImpl> getNextRank(int id) {
        return this.getRankById(id + 1);
    }

    public RankImpl getPlayerRank(Player p) {
        int rankId = this.onlinePlayersRanks.getOrDefault(p.getUniqueId(), getDefaultRank().getId());
        return this.getRankById(rankId).orElse(this.getDefaultRank());
    }

    private RankImpl getDefaultRank() {
        return this.plugin.getRanksConfig().getDefaultRank();
    }

    public boolean isMaxRank(Player p) {
        return this.getPlayerRank(p).getId() == getMaxRank().getId();
    }

    public boolean buyMaxRank(Player p) {

        if (isMaxRank(p)) {
            PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("prestige_needed"));
            return false;
        }

        RankImpl maxRankImpl = getMaxRank();
        RankImpl currentRankImpl = this.getPlayerRank(p);

        int finalRankId = currentRankImpl.getId();

        for (int i = currentRankImpl.getId(); i < maxRankImpl.getId(); i++) {
            Optional<RankImpl> rank = this.getRankById(i + 1);
            if (!rank.isPresent()) {
                break;
            }
            double cost = rank.get().getCost();
            if (!this.isTransactionAllowed(p, cost)) {
                break;
            }
            if (!this.completeTransaction(p, cost)) {
                break;
            }
            finalRankId = i + 1;
        }

        Optional<RankImpl> nextRankOptional = this.getNextRank(currentRankImpl.getId());

        if (finalRankId == currentRankImpl.getId() && nextRankOptional.isPresent()) {
            PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("not_enough_money").replace("%cost%", String.format("%,.0f", nextRankOptional.get().getCost())));
            return false;
        }

        Optional<RankImpl> finalRankOptional = this.getRankById(finalRankId);

        if (!finalRankOptional.isPresent()) {
            return false;
        }

        RankImpl finalRankImpl = finalRankOptional.get();

        PlayerRankUpEvent event = new PlayerRankUpEvent(p, currentRankImpl, finalRankImpl);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
            return false;
        }

        for (int i = currentRankImpl.getId() + 1; i <= finalRankImpl.getId(); i++) {
            this.getRankById(i).ifPresent(r -> runCommands(r, p));
        }

        this.onlinePlayersRanks.put(p.getUniqueId(), finalRankImpl.getId());
        PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("rank_up").replace("%Rank-1%", currentRankImpl.getPrefix()).replace("%Rank-2%", finalRankImpl.getPrefix()));
        return true;
    }

    private RankImpl getMaxRank() {
        return this.plugin.getRanksConfig().getMaxRank();
    }

    public boolean buyNextRank(Player p) {

        if (isMaxRank(p)) {
            PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("prestige_needed"));
            return false;
        }

        RankImpl currentRankImpl = this.getPlayerRank(p);
        Optional<RankImpl> toBuyOptional = getNextRank(currentRankImpl.getId());

        if (!toBuyOptional.isPresent()) {
            PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("prestige_needed"));
            return false;
        }

        RankImpl toBuy = toBuyOptional.get();

        if (!this.isTransactionAllowed(p, toBuy.getCost())) {
            if (this.plugin.getRanksConfig().isUseTokensCurrency()) {
                PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("not_enough_tokens").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            } else {
                PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("not_enough_money").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            }
            return false;
        }

        PlayerRankUpEvent event = new PlayerRankUpEvent(p, currentRankImpl, toBuy);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
            return false;
        }

        if (!this.completeTransaction(p, toBuy.getCost())) {
            return false;
        }

        runCommands(toBuy, p);

        this.onlinePlayersRanks.put(p.getUniqueId(), toBuy.getId());

        PlayerUtils.sendMessage(p, this.plugin.getRanksConfig().getMessage("rank_up").replace("%Rank-1%", currentRankImpl.getPrefix()).replace("%Rank-2%", toBuy.getPrefix()));
        return true;
    }

    private boolean completeTransaction(Player p, double cost) {
        if (this.plugin.getRanksConfig().isUseTokensCurrency()) {
            this.plugin.getCore().getTokens().getApi().remove(p, (long) cost, LostCause.RANKUP);
            return true;
        } else {
            return this.plugin.getCore().getEconomy().withdrawPlayer(p, cost).transactionSuccess();
        }
    }

    private boolean isTransactionAllowed(Player p, double cost) {
        if (this.plugin.getRanksConfig().isUseTokensCurrency()) {
            return this.plugin.getCore().getTokens().getApi().hasEnough(p, (long) cost);
        } else {
            return this.plugin.getCore().getEconomy().has(p, cost);
        }
    }

    public void setRank(Player target, Rank rank, CommandSender sender) {
        RankImpl rankImpl = getRankById(rank.getId()).orElse(null);
        setRank(target,rankImpl,sender);
    }


    public void setRank(Player target, RankImpl rankImpl, CommandSender sender) {

        Rank currentRankImpl = this.getPlayerRank(target);

        PlayerRankUpEvent event = new PlayerRankUpEvent(target, currentRankImpl, rankImpl);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
            return;
        }

        this.runCommands(rankImpl, target);

        this.onlinePlayersRanks.put(target.getUniqueId(), rankImpl.getId());

        if (sender != null) {
            PlayerUtils.sendMessage(sender, this.plugin.getRanksConfig().getMessage("rank_set").replace("%rank%", rankImpl.getPrefix()).replace("%player%", target.getName()));
            PlayerUtils.sendMessage(target, this.plugin.getRanksConfig().getMessage("rank_up").replace("%Rank-1%", currentRankImpl.getPrefix()).replace("%Rank-2%", rankImpl.getPrefix()));
        }

    }

    public int getRankupProgress(Player player) {

        if (this.isMaxRank(player)) {
            if (arePrestigesEnabled()) {
                return getPrestigeManager().getPrestigeProgress(player);
            }
            return 100;
        }

        RankImpl current = this.getPlayerRank(player);
        Optional<RankImpl> nextRankOptional = this.getNextRank(current.getId());

        if (!nextRankOptional.isPresent()) {
            return 100;
        }

        RankImpl next = nextRankOptional.get();

        double currentBalance = this.plugin.getRanksConfig().isUseTokensCurrency() ?
                this.plugin.getCore().getTokens().getApi().getAmount(player) : this.plugin.getCore().getEconomy().getBalance(player);

        int progress = (int) ((currentBalance / next.getCost()) * 100);

        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    public double getNextRankCost(Player player) {
        if (this.isMaxRank(player)) {
            if (arePrestigesEnabled()) {
                if (getPrestigeManager().isMaxPrestige(player)) {
                    return 0.0;
                } else {
                    PrestigeImpl prestigeImpl = getPrestigeManager().getPlayerPrestige(player);
                    PrestigeImpl next = getPrestigeManager().getNextPrestige(prestigeImpl);
                    if (next != null) {
                        return next.getCost();
                    } else {
                        return 0.0;
                    }
                }
            } else {
                return 0.0;
            }
        }

        RankImpl current = this.getPlayerRank(player);
        Optional<RankImpl> nextRankOptional = this.getNextRank(current.getId());
        return nextRankOptional.map(RankImpl::getCost).orElse(0.0);
    }

    public void resetPlayerRank(Player p) {
        setRank(p, getDefaultRank(), null);
    }

    private boolean arePrestigesEnabled() {
        return this.plugin.getCore().isModuleEnabled(XPrisonPrestiges.MODULE_NAME);
    }

    private PrestigeManager getPrestigeManager() {
        if (!arePrestigesEnabled()) {
            throw new IllegalStateException("Prestiges module is not enabled");
        }
        return this.plugin.getCore().getPrestiges().getPrestigeManager();
    }

    public String getRankupProgressBar(Player player) {

        double currentProgress = 0, required = 100;
        if (this.isMaxRank(player)) {
            currentProgress = 100;
            if (arePrestigesEnabled()) {
                currentProgress = getPrestigeManager().getPrestigeProgress(player);
            }
        } else {
            RankImpl current = this.getPlayerRank(player);
            Optional<RankImpl> next = this.getNextRank(current.getId());
            if (next.isPresent()) {
                double currentBalance = this.plugin.getRanksConfig().isUseTokensCurrency() ? this.plugin.getCore().getTokens().getApi().getAmount(player) : this.plugin.getCore().getEconomy().getBalance(player);
                currentProgress = (currentBalance / next.get().getCost()) * 100;
            }
        }

        if (currentProgress > 100) {
            currentProgress = 100;
        }

        return ProgressBar.getProgressBar(this.plugin.getRanksConfig().getProgressBarLength(), this.plugin.getRanksConfig().getProgressBarDelimiter(), currentProgress, required);
    }

    public void runCommands(RankImpl rankImpl, Player p) {
        if (rankImpl.getCommandsToExecute() != null) {

            if (!Bukkit.isPrimaryThread()) {
                Schedulers.async().run(() -> {
                    executeCommands(rankImpl, p);
                });
            } else {
                executeCommands(rankImpl, p);
            }
        }
    }

    public Optional<RankImpl> getRankById(int id) {
        return Optional.ofNullable(this.plugin.getRanksConfig().getRankById(id));
    }

    private void executeCommands(RankImpl rankImpl, Player p) {
        for (String cmd : rankImpl.getCommandsToExecute()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Rank%", rankImpl.getPrefix()));
        }
    }

    public void disable() {
        this.saveAllDataSync();
    }

    public void enable() {
        this.loadAllData();
    }
}
