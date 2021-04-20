package me.drawethree.ultraprisoncore.gangs.managers;

import lombok.Getter;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.api.events.GangCreateEvent;
import me.drawethree.ultraprisoncore.gangs.api.events.GangDisbandEvent;
import me.drawethree.ultraprisoncore.gangs.gui.DisbandGangAdminGUI;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.drawethree.ultraprisoncore.utils.MapUtil;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GangsManager {

    private int maxGangMembers = 5;
    private int gangUpdateDelay = 1;
    private int maxGangNameLength = 10;

    private UltraPrisonGangs plugin;

    private Map<String, Gang> gangs;
    private Map<UUID, Gang> pendingInvites;
    private List<UUID> gangChatEnabledPlayers;

    @Getter
    private String gangDisbandGUITitle;

    private List<String> gangInfoFormat;
    private List<String> gangTopFormat;
    private List<String> gangAdminHelpMenu;
    private List<String> gangHelpMenu;

    private boolean updating;
    private boolean enableColorCodes;
	private Map<Gang, BigInteger> topGangs;

    private Task task;

    public GangsManager(UltraPrisonGangs plugin) {
        this.plugin = plugin;
        this.pendingInvites = new HashMap<>(25);
        this.gangChatEnabledPlayers = new ArrayList<>(50);
		this.topGangs = new HashMap<>();

        this.reloadConfig();

        this.loadGangs();
        this.updateTop10();

        Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST)
                .filter(e -> this.hasGangChatEnabled(e.getPlayer()))
                .handler(e -> {

                    Optional<Gang> gangOptional = this.getPlayerGang(e.getPlayer());
                    if (!gangOptional.isPresent()) {
                        return;
                    }

                    e.setCancelled(true);

                    Gang gang = gangOptional.get();

                    e.getRecipients().removeIf(player -> !gang.containsPlayer(player));
                    for (Player p : e.getRecipients()) {
                        p.sendMessage(String.format(Text.colorize("&e&l[Gang] &f%s &8» &f%s"), e.getPlayer().getDisplayName(), e.getMessage()));
                    }
                }).bindWith(this.plugin.getCore());
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
        this.gangHelpMenu = this.plugin.getConfig().get().getStringList("gang-help-menu");
        this.gangDisbandGUITitle = this.plugin.getConfig().get().getString("gang-disband-gui-title");
        this.gangAdminHelpMenu = this.plugin.getConfig().get().getStringList("gang-admin-help-menu");
        this.gangTopFormat = this.plugin.getConfig().get().getStringList("gang-top-format");
        this.gangUpdateDelay = this.plugin.getConfig().get().getInt("gang-top-update");
        this.maxGangMembers = this.plugin.getConfig().get().getInt("max-gang-members");
        this.maxGangNameLength = this.plugin.getConfig().get().getInt("max-gang-name-length");
        this.enableColorCodes = this.plugin.getConfig().get().getBoolean("color-codes-in-gang-name");
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
        return this.gangs.values().stream().filter(gang -> ChatColor.stripColor(Text.colorize(gang.getName())).equalsIgnoreCase(name)).findFirst();
    }

    public boolean createGang(String name, Player creator) {

        if (name.isEmpty()) {
            creator.sendMessage(this.plugin.getMessage("gang-invalid-name"));
            return false;
        }

        GangCreateResult nameCheck = checkGangName(name);

        if (nameCheck == GangCreateResult.NAME_TOO_LONG) {
            creator.sendMessage(this.plugin.getMessage("gang-name-long"));
            return false;
        } else if (nameCheck == GangCreateResult.NAME_CONTAINS_COLORS) {
            creator.sendMessage(this.plugin.getMessage("gang-name-colors"));
            return false;
        }

        if (this.getPlayerGang(creator).isPresent()) {
            creator.sendMessage(this.plugin.getMessage("gang-cant-create"));
            return false;
        }

        if (this.getGangWithName(name).isPresent()) {
            creator.sendMessage(this.plugin.getMessage("gang-already-exists").replace("%name%", Text.colorize(name)));
            return false;
        }


        Gang g = new Gang(name, creator.getUniqueId());

        GangCreateEvent gangCreateEvent = new GangCreateEvent(creator, g);

        this.plugin.getCore().debug("Calling GangCreateEvent for gang " + g.getName() + ".");

        Events.callSync(gangCreateEvent);

        if (gangCreateEvent.isCancelled()) {
            this.plugin.getCore().debug("GangCreateEvent for gang " + g.getName() + " was cancelled.");
            return true;
        }

        this.gangs.put(name, g);

        creator.sendMessage(this.plugin.getMessage("gang-created").replace("%name%", Text.colorize(name)));

        this.plugin.getCore().getPluginDatabase().createGang(g);
        Players.all().forEach(player1 -> player1.sendMessage(this.plugin.getMessage("gang-create-broadcast").replace("%gang%", Text.colorize(g.getName())).replace("%player%", creator.getName())));
        return true;
    }

    private GangCreateResult checkGangName(String name) {
        if (this.enableColorCodes) {
            if (ChatColor.stripColor(Text.colorize(name)).length() > this.maxGangNameLength) {
                return GangCreateResult.NAME_TOO_LONG;
            }
            return GangCreateResult.VALID;
        } else {

            if (!ChatColor.translateAlternateColorCodes('&', name).equals(name)) {
                return GangCreateResult.NAME_CONTAINS_COLORS;
            }

            if (name.length() > this.maxGangNameLength) {
                return GangCreateResult.NAME_TOO_LONG;
            }
            return GangCreateResult.VALID;
        }
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

        if (gang.getMembersOffline().size() >= maxGangMembers) {
            invitedBy.sendMessage(this.plugin.getMessage("gang-full"));
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
        invited.sendMessage(this.plugin.getMessage("gang-invite-received").replace("%gang%", Text.colorize(gang.getName())));

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
                    .replace("%gang_top%", String.format("%,d", this.getGangTopPosition(g)))
					.replace("%gang_value%", String.format("%,d", this.topGangs.getOrDefault(g, BigInteger.ZERO)))
                    .replace("%gang%", Text.colorize(g.getName()))
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

    public boolean sendGangInfo(Player p, String gangName) {
        Optional<Gang> targetGang = this.getGangWithName(gangName);

        if (!targetGang.isPresent()) {
            p.sendMessage(this.plugin.getMessage("gang-not-exists"));
            return true;
        }

        for (String s : this.getGangInfoFormat(targetGang.get())) {
            p.sendMessage(Text.colorize(s));
        }
        return true;
    }

    public boolean hasGangChatEnabled(Player p) {
        return this.gangChatEnabledPlayers.contains(p.getUniqueId());
    }

    public boolean disbandGang(CommandSender sender, Gang gang) {

        gang.disband();
        this.gangs.remove(gang.getName());
        this.plugin.getCore().getPluginDatabase().deleteGang(gang);

        Players.all().forEach(player1 -> player1.sendMessage(this.plugin.getMessage("gang-disband-broadcast").replace("%gang%", Text.colorize(gang.getName())).replace("%player%", sender.getName())));
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

        this.plugin.getCore().debug("Calling GangDisbandEvent for gang " + gang.getName() + ".");

        Events.callSync(gangDisbandEvent);

        if (gangDisbandEvent.isCancelled()) {
            this.plugin.getCore().debug("GangDisbandEvent for gang " + gang.getName() + " was cancelled.");
            return true;
        }


        gang.disband();
        this.gangs.remove(gang.getName());
        this.plugin.getCore().getPluginDatabase().deleteGang(gang);

        Players.all().forEach(player1 -> player1.sendMessage(this.plugin.getMessage("gang-disband-broadcast").replace("%gang%", Text.colorize(gang.getName())).replace("%player%", player.getName())));
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

    public void sendHelpMenu(CommandSender sender) {
        this.gangHelpMenu.forEach(s -> sender.sendMessage(Text.colorize(s)));
    }

    public void sendAdminHelpMenu(CommandSender sender) {
        this.gangAdminHelpMenu.forEach(s -> sender.sendMessage(Text.colorize(s)));
    }


    public int getGangTopPosition(Gang gang) {
		if (!this.topGangs.containsKey(gang)) {
            return -1;
        }

		int index = 0;

		for (Gang g : this.topGangs.keySet()) {
			if (g == gang) {
				return index + 1;
			}
			index++;
		}
		return -1;
    }

    private void updateTop10() {
        this.updating = true;
        task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
			this.topGangs = new HashMap<>();

			for (Gang g : this.gangs.values()) {
				BigInteger blocksBroken = BigInteger.ZERO;
				for (OfflinePlayer p : g.getMembersOffline()) {
					long pBlocks = this.plugin.getCore().getTokens().getTokensManager().getPlayerBrokenBlocks(p);
					blocksBroken = blocksBroken.add(BigInteger.valueOf(pBlocks));
				}
				blocksBroken = blocksBroken.add(BigInteger.valueOf(this.plugin.getCore().getTokens().getTokensManager().getPlayerTokens(g.getOwnerOffline())));
				topGangs.put(g, blocksBroken);
			}

			this.topGangs = MapUtil.sortByValue(topGangs);



            this.updating = false;
        }, this.gangUpdateDelay, TimeUnit.MINUTES, this.gangUpdateDelay, TimeUnit.MINUTES);
    }

    public boolean removeFromGang(Player p, Optional<Gang> gangOpt, OfflinePlayer target) {

        if (!gangOpt.isPresent()) {
            p.sendMessage(this.plugin.getMessage("not-in-gang"));
            return false;
        }

        Gang gang = gangOpt.get();

        if (!gang.isOwner(p)) {
            p.sendMessage(this.plugin.getMessage("gang-not-owner"));
            return false;
        }

        if (target == null) {
            p.sendMessage(this.plugin.getMessage("player-not-online"));
            return false;
        }

        gang.kickPlayer(target);
        return true;
    }

    public boolean sendGangTop(CommandSender sender) {

        if (this.updating) {
            sender.sendMessage(this.plugin.getMessage("gang-top-updating"));
            return true;
        }

        for (String s : this.gangTopFormat) {
            if (s.startsWith("{FOR_EACH_GANG}")) {
                String rawContent = s.replace("{FOR_EACH_GANG} ", "");
				Iterator<Gang> gangIterator = this.topGangs.keySet().iterator();
                for (int i = 0; i < 10; i++) {
                    try {
						Gang gang = gangIterator.next();
						sender.sendMessage(Text.colorize(rawContent.replace("%position%", String.valueOf(i + 1)).replace("%gang%", Text.colorize(gang.getName())).replace("%value%", String.format("%,d", this.topGangs.get(gang)))));
                    } catch (Exception e) {
                        break;
                    }
                }
            } else {
                sender.sendMessage(Text.colorize(s));
            }
        }
        return true;
    }

    public boolean forceAdd(CommandSender sender, Player target, Optional<Gang> gangOptional) {

        if (target == null) {
            sender.sendMessage(this.plugin.getMessage("player-not-online"));
            return false;
        }

        if (!gangOptional.isPresent()) {
            sender.sendMessage(this.plugin.getMessage("gang-not-exists"));
            return false;
        }

        Gang targetGang = gangOptional.get();

        Optional<Gang> currentGang = this.getPlayerGang(target);

        if (currentGang.isPresent()) {
            sender.sendMessage(this.plugin.getMessage("gang-cant-invite"));
            return false;
        }

        return joinGang(target, targetGang);
    }

    public boolean forceRemove(CommandSender sender, Player target) {

        if (target == null) {
            sender.sendMessage(this.plugin.getMessage("player-not-online"));
            return false;
        }

        Optional<Gang> currentGang = this.getPlayerGang(target);

        if (!currentGang.isPresent()) {
            sender.sendMessage(this.plugin.getMessage("gang-player-not-in-gang"));
            return false;
        }

        return leaveGang(target);
    }

    public boolean forceDisband(CommandSender sender, Optional<Gang> gangOptional) {

        if (!gangOptional.isPresent()) {
            sender.sendMessage(this.plugin.getMessage("gang-not-exists"));
            return false;
        }

        if (sender instanceof Player) {
            new DisbandGangAdminGUI(this.plugin, (Player) sender, gangOptional.get()).open();
        } else {
            sender.sendMessage("§cOnly for players.");
        }
        return true;
    }

    public boolean toggleGangChat(Player p) {

        if (!getPlayerGang(p).isPresent()) {
            p.sendMessage(this.plugin.getMessage("not-in-gang"));
            return false;
        }

        if (this.gangChatEnabledPlayers.contains(p.getUniqueId())) {
            this.gangChatEnabledPlayers.remove(p.getUniqueId());
            p.sendMessage(this.plugin.getMessage("gang-chat-off"));
        } else {
            this.gangChatEnabledPlayers.add(p.getUniqueId());
            p.sendMessage(this.plugin.getMessage("gang-chat-on"));
        }
        return true;
    }

    public boolean modifyValue(CommandSender sender, Optional<Gang> gang, int amount, String operation) {

        if (!gang.isPresent()) {
            sender.sendMessage(this.plugin.getMessage("gang-not-exists"));
            return false;
        }

        if (amount <= 0) {
            sender.sendMessage(this.plugin.getMessage("invalid-value"));
            return false;
        }

        if (operation.equalsIgnoreCase("add")) {
            sender.sendMessage(this.plugin.getMessage("gang-value-add").replace("%value%", String.valueOf(amount)).replace("%gang%", Text.colorize(gang.get().getName())));
            gang.get().setValue(gang.get().getValue() + amount);
            return true;
        } else if (operation.equalsIgnoreCase("remove")) {
            sender.sendMessage(this.plugin.getMessage("gang-value-remove").replace("%value%", String.valueOf(amount)).replace("%gang%", Text.colorize(gang.get().getName())));
            gang.get().setValue(gang.get().getValue() - amount);
            return true;
        } else {
            sender.sendMessage("§cInvalid operation given.");
            return false;
        }
    }
}
