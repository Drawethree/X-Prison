package me.drawethree.ultraprisoncore.gangs.managers;

import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.api.events.GangCreateEvent;
import me.drawethree.ultraprisoncore.gangs.api.events.GangDisbandEvent;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GangsManager {

    private UltraPrisonGangs plugin;

    private Map<String, Gang> gangs;
    private Map<UUID, Gang> pendingInvites;

    private List<String> gangInfoFormat;

    public GangsManager(UltraPrisonGangs plugin) {
        this.plugin = plugin;
        this.pendingInvites = new HashMap<>();

        this.gangInfoFormat = this.plugin.getConfig().get().getStringList("gang-info-format");
        this.loadGangs();
    }

    private void loadGangs() {
        this.gangs = new HashMap<>();
        Schedulers.async().run(() -> {
            for (Gang g : this.plugin.getCore().getPluginDatabase().getAllGangs()) {
                this.gangs.put(g.getName(), g);
            }
        });
    }

    public void reloadConfig() {
        this.gangInfoFormat = this.plugin.getConfig().get().getStringList("gang-info-format");
    }

    public void saveDataOnDisable() {
        for (Gang g : this.gangs.values()) {
            this.plugin.getCore().getPluginDatabase().updateGang(g);
        }
    }

    public Optional<Gang> getPlayerGang(OfflinePlayer p) {
        return this.gangs.values().stream().filter(gang -> gang.containsPlayer(p)).findFirst();
    }

    public Optional<Gang> getGangWithName(String name) {
        return this.gangs.values().stream().filter(gang -> gang.getName().equalsIgnoreCase(name)).findFirst();
    }

    public boolean createGang(String name, Player creator) {

        if (name.isEmpty()) {
            creator.sendMessage(this.plugin.getMessage("gang-invalid-name"));
            return false;
        }

        if (this.getPlayerGang(creator).isPresent()) {
            creator.sendMessage(this.plugin.getMessage("gang-cant-create"));
            return false;
        }

        if (this.getGangWithName(name).isPresent()) {
            creator.sendMessage(this.plugin.getMessage("gang-already-exists").replace("%name%", name));
            return false;
        }


        Gang g = new Gang(name, creator.getUniqueId());

        GangCreateEvent gangCreateEvent = new GangCreateEvent(creator,g);

        Events.call(gangCreateEvent);

        if (gangCreateEvent.isCancelled()) {
            return true;
        }

        this.gangs.put(name, g);

        creator.sendMessage(this.plugin.getMessage("gang-created").replace("%name%", name));

        this.plugin.getCore().getPluginDatabase().createGang(g);
        Players.all().forEach(player1 -> player1.sendMessage(this.plugin.getMessage("gang-create-broadcast").replace("%gang%", g.getName()).replace("%player%", creator.getName())));
        return true;
    }

    public boolean invitePlayer(Player invitedBy, Player invited) {

        if (invited == null || !invited.isOnline()) {
            invitedBy.sendMessage(this.plugin.getMessage("player-not-online"));
            return false;
        }

        Optional<Gang> gangOptional = this.getPlayerGang(invitedBy);

        if (!gangOptional.isPresent()) {
            invitedBy.sendMessage(this.plugin.getMessage("not-in-gang"));
            return false;
        }

        Gang gang = gangOptional.get();

        if (!gang.isOwner(invitedBy)) {
            invitedBy.sendMessage(this.plugin.getMessage("gang-not-owner"));
            return false;
        }

        Optional<Gang> gang1 = this.getPlayerGang(invited);

        if (gang1.isPresent()) {
            invitedBy.sendMessage(this.plugin.getMessage("gang-cant-invite"));
            return false;
        }

        if (this.pendingInvites.containsKey(invited.getUniqueId())) {
            invitedBy.sendMessage(this.plugin.getMessage("gang-invite-pending"));
            return false;
        }

        this.pendingInvites.put(invited.getUniqueId(), gang);

        invitedBy.sendMessage(this.plugin.getMessage("gang-invite-success").replace("%player%", invited.getName()));
        invited.sendMessage(this.plugin.getMessage("gang-invite-received").replace("%gang%", gang.getName()));

        Schedulers.sync().runLater(() -> this.pendingInvites.remove(invited.getUniqueId()), 5, TimeUnit.MINUTES);
        return true;
    }

    public boolean leaveGang(Player player) {

        Optional<Gang> optGang = this.getPlayerGang(player);
        if (!optGang.isPresent()) {
            player.sendMessage(this.plugin.getMessage("not-in-gang"));
            return false;
        }

        Gang gang = optGang.get();

        if (gang.isOwner(player)) {
            player.sendMessage(this.plugin.getMessage("gang-please-disband"));
            return false;
        }

        return gang.leavePlayer(player);
    }

    public boolean joinGang(Player player, Gang gang) {

        Optional<Gang> optGang = this.getPlayerGang(player);

        if (optGang.isPresent()) {
            player.sendMessage(this.plugin.getMessage("gang-cant-join"));
            return false;
        }


        if (gang.joinPlayer(player)) {
            this.pendingInvites.remove(player.getUniqueId());
            return true;
        } else {
            return false;
        }
    }

    private List<String> getGangInfoFormat(Gang g) {
        List<String> returnList = new ArrayList<>(this.gangInfoFormat.size());

        for (String s : this.gangInfoFormat) {
            returnList.add(s
                    .replace("%gang%", g.getName())
                    .replace("%gang_owner%", g.getOwnerOffline().getName())
                    .replace("%gang_members%", StringUtils.join(g.getMembersOffline().stream().map(OfflinePlayer::getName).toArray(), ", ")));
        }
        return returnList;
    }

    public boolean sendGangInfo(Player p, OfflinePlayer target) {
        Optional<Gang> targetGang = this.getPlayerGang(target);

        if (!targetGang.isPresent()) {
            p.sendMessage(this.plugin.getMessage("gang-player-not-in-gang"));
            return true;
        }

        for (String s : this.getGangInfoFormat(targetGang.get())) {
            p.sendMessage(Text.colorize(s));
        }
        return true;
    }

    public boolean disbandGang(Player player) {
        Optional<Gang> gangOptional = this.getPlayerGang(player);

        if (!gangOptional.isPresent()) {
            player.sendMessage(this.plugin.getMessage("not-in-gang"));
            return false;
        }

        Gang gang = gangOptional.get();

        if (!gang.isOwner(player)) {
            player.sendMessage(this.plugin.getMessage("gang-not-owner"));
            return false;
        }

        GangDisbandEvent gangDisbandEvent = new GangDisbandEvent(gang);

        Events.call(gangDisbandEvent);

        if (gangDisbandEvent.isCancelled()) {
            return true;
        }


        gang.disband();
        this.gangs.remove(gang.getName());
        this.plugin.getCore().getPluginDatabase().deleteGang(gang);

        Players.all().forEach(player1 -> player1.sendMessage(this.plugin.getMessage("gang-disband-broadcast").replace("%gang%", gang.getName()).replace("%player%", player.getName())));
        return true;
    }

    public boolean acceptInvite(Player player) {

        Gang gangToJoin = this.pendingInvites.get(player.getUniqueId());

        if (gangToJoin == null) {
            player.sendMessage(this.plugin.getMessage("gang-no-invite-pending"));
            return false;
        }

        return joinGang(player, gangToJoin);
    }
}
