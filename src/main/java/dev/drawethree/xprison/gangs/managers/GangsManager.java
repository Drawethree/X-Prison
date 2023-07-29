package dev.drawethree.xprison.gangs.managers;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.api.events.GangCreateEvent;
import dev.drawethree.xprison.gangs.api.events.GangDisbandEvent;
import dev.drawethree.xprison.gangs.enums.GangCreateResult;
import dev.drawethree.xprison.gangs.enums.GangLeaveReason;
import dev.drawethree.xprison.gangs.enums.GangNameCheckResult;
import dev.drawethree.xprison.gangs.enums.GangRenameResult;
import dev.drawethree.xprison.gangs.gui.admin.DisbandGangAdminGUI;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gangs.model.GangInvitation;
import dev.drawethree.xprison.gangs.model.GangTopProvider;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GangsManager {

	private final XPrisonGangs plugin;
	private final Map<UUID, Gang> gangs;
	private final List<UUID> gangChatEnabledPlayers;
	private List<Gang> topGangs;

	public GangsManager(XPrisonGangs plugin) {
		this.plugin = plugin;
		this.gangChatEnabledPlayers = new ArrayList<>();
		this.gangs = new ConcurrentHashMap<>();
		this.topGangs = new ArrayList<>();
	}

	public void enable() {
		this.loadGangs();
	}

	public boolean arePlayersInSameGang(Player player1, Player player2) {
		Optional<Gang> player1Gang = this.getPlayerGang(player1);
		Optional<Gang> player2Gang = this.getPlayerGang(player2);

		if (!player1Gang.isPresent() || !player2Gang.isPresent()) {
			return false;
		}

		return player1Gang.get().equals(player2Gang.get());
	}

	private void loadGangs() {
		this.gangs.clear();
		Schedulers.async().run(() -> {
			for (Gang g : this.plugin.getGangsService().getAllGangs()) {
				this.gangs.put(g.getUuid(), g);
			}
		});
	}

	private void saveDataOnDisable() {
		for (Gang g : this.gangs.values()) {
			this.plugin.getGangsService().updateGang(g);
		}
		this.plugin.getCore().getLogger().info("Saved all gangs.");
	}

	public Optional<Gang> getPlayerGang(OfflinePlayer p) {
		return this.gangs.values().stream().filter(gang -> gang.containsPlayer(p)).findFirst();
	}

	public Optional<Gang> getGangWithName(String name) {
		return this.gangs.values().stream().filter(gang -> ChatColor.stripColor(TextUtils.applyColor(gang.getName())).equalsIgnoreCase(name)).findFirst();
	}

	public GangRenameResult renameGang(Gang gang, String newName, CommandSender whoRenamed) {

		GangNameCheckResult nameCheckResult = this.performNameCheck(newName, whoRenamed);
		if (nameCheckResult != GangNameCheckResult.SUCCESS) {
			return GangRenameResult.valueOf(nameCheckResult.name());
		}

		gang.setName(newName);

		PlayerUtils.sendMessage(whoRenamed, this.plugin.getConfig().getMessage("gang-rename").replace("%gang%", TextUtils.applyColor(gang.getName())));
		return GangRenameResult.SUCCESS;
	}

	public GangCreateResult createGang(String name, Player creator) {

		if (this.getPlayerGang(creator).isPresent()) {
			PlayerUtils.sendMessage(creator, this.plugin.getConfig().getMessage("gang-cant-create"));
			return GangCreateResult.PLAYER_HAS_GANG;
		}

		GangNameCheckResult nameCheckResult = this.performNameCheck(name, creator);

		if (nameCheckResult != GangNameCheckResult.SUCCESS) {
			return GangCreateResult.valueOf(nameCheckResult.name());
		}

		Gang g = new Gang(name, creator.getUniqueId());

		GangCreateEvent gangCreateEvent = new GangCreateEvent(creator, g);

		this.plugin.getCore().debug("Calling GangCreateEvent for gang " + g.getName() + ".", this.plugin);

		Events.call(gangCreateEvent);

		if (gangCreateEvent.isCancelled()) {
			this.plugin.getCore().debug("GangCreateEvent for gang " + g.getName() + " was cancelled.", this.plugin);
			return GangCreateResult.EVENT_CANCELLED;
		}

		this.gangs.put(g.getUuid(), g);

		PlayerUtils.sendMessage(creator, this.plugin.getConfig().getMessage("gang-created").replace("%name%", TextUtils.applyColor(name)));

		this.plugin.getGangsService().createGang(g);
		Players.all().forEach(player1 -> PlayerUtils.sendMessage(player1, this.plugin.getConfig().getMessage("gang-create-broadcast").replace("%gang%", TextUtils.applyColor(g.getName())).replace("%player%", creator.getName())));
		return GangCreateResult.SUCCESS;
	}

	private GangNameCheckResult performNameCheck(String name, CommandSender sender) {
		GangNameCheckResult nameCheck = checkGangName(name);

		if (nameCheck == GangNameCheckResult.NAME_TOO_LONG) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-name-long"));
		} else if (nameCheck == GangNameCheckResult.NAME_CONTAINS_COLORS) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-name-colors"));
		} else if (nameCheck == GangNameCheckResult.NAME_TAKEN) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-already-exists").replace("%name%", TextUtils.applyColor(name)));
		} else if (nameCheck == GangNameCheckResult.NAME_EMPTY) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-invalid-name"));
		} else if (nameCheck == GangNameCheckResult.NAME_RESTRICTED) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-name-restricted"));
		}

		return nameCheck;
	}

	private GangNameCheckResult checkGangName(String name) {

		if (name.isEmpty()) {
			return GangNameCheckResult.NAME_EMPTY;
		}

		for (String s : this.plugin.getConfig().getRestrictedNames()) {
			if (name.contains(s)) {
				return GangNameCheckResult.NAME_RESTRICTED;
			}
		}

		if (this.plugin.getConfig().isEnableColorCodes()) {
			if (ChatColor.stripColor(TextUtils.applyColor(name)).length() > this.plugin.getConfig().getMaxGangNameLength()) {
				return GangNameCheckResult.NAME_TOO_LONG;
			}
		} else {

			if (!ChatColor.translateAlternateColorCodes('&', name).equals(name)) {
				return GangNameCheckResult.NAME_CONTAINS_COLORS;
			}

			if (name.length() > this.plugin.getConfig().getMaxGangNameLength()) {
				return GangNameCheckResult.NAME_TOO_LONG;
			}

		}
		if (!this.getGangWithName(name).isPresent()) {
			return GangNameCheckResult.SUCCESS;
		} else {
			return GangNameCheckResult.NAME_TAKEN;
		}
	}

	public boolean invitePlayer(Player invitedBy, Player invited) {

		if (invited == null || !invited.isOnline()) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("player-not-online"));
			return false;
		}

		Optional<Gang> gangOptional = this.getPlayerGang(invitedBy);

		if (!gangOptional.isPresent()) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("not-in-gang"));
			return false;
		}

		Gang gang = gangOptional.get();

		if (!gang.isOwner(invitedBy)) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("gang-not-owner"));
			return false;
		}

		if (gang.getMembersOffline().size() >= this.plugin.getConfig().getMaxGangMembers()) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("gang-full"));
			return false;
		}

		Optional<Gang> gang1 = this.getPlayerGang(invited);

		if (gang1.isPresent()) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("gang-cant-invite"));
			return false;
		}

		if (gang.hasPendingInvite(invited)) {
			PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("gang-invite-pending"));
			return false;
		}

		GangInvitation invitation = gang.invitePlayer(invitedBy, invited);

		PlayerUtils.sendMessage(invitedBy, this.plugin.getConfig().getMessage("gang-invite-success").replace("%player%", invited.getName()));
		PlayerUtils.sendMessage(invited, this.plugin.getConfig().getMessage("gang-invite-received").replace("%gang%", gang.getName()));

		Schedulers.sync().runLater(() -> gang.removeInvitation(invitation), 5, TimeUnit.MINUTES);
		return true;
	}

	public boolean leaveGang(Player player, GangLeaveReason reason) {

		Optional<Gang> optGang = this.getPlayerGang(player);

		if (!optGang.isPresent()) {
			PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("not-in-gang"));
			return false;
		}

		Gang gang = optGang.get();

		if (gang.isOwner(player)) {
			PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("gang-please-disband"));
			return false;
		}

		if (gang.leavePlayer(player, reason)) {
			gang.getOnlinePlayers().forEach(player1 -> PlayerUtils.sendMessage(player1, this.plugin.getConfig().getMessage("gang-player-left").replace("%player%", player.getName())));
			PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("gang-left").replace("%gang%", gang.getName()));
			return true;
		}

		return false;
	}

	public boolean joinGang(OfflinePlayer player, Gang gang) {

		Optional<Gang> optGang = this.getPlayerGang(player);

		if (optGang.isPresent()) {
			if (player.isOnline()) {
				PlayerUtils.sendMessage(player.getPlayer(), this.plugin.getConfig().getMessage("gang-cant-join"));
			}
			return false;
		}

		if (gang.joinPlayer(player)) {
			if (player.isOnline()) {
				PlayerUtils.sendMessage(player.getPlayer(), this.plugin.getConfig().getMessage("gang-joined").replace("%gang%", gang.getName()));
			}
			gang.getOnlinePlayers().stream().filter(player1 -> player1 != player).forEach(player1 -> PlayerUtils.sendMessage(player1, this.plugin.getConfig().getMessage("gang-player-joined").replace("%player%", player.getName())));
			return true;
		} else {
			return false;
		}
	}

	private List<String> getGangInfoFormat(Gang g) {
		List<String> originalFormat = this.plugin.getConfig().getGangInfoFormat();
		List<String> returnList = new ArrayList<>();

		for (String s : originalFormat) {
			returnList.add(s
					.replace("%gang_top%", String.format("%,d", this.getGangTopPosition(g)))
					.replace("%gang_value%", String.format("%,d", g.getValue()))
					.replace("%gang%", TextUtils.applyColor(g.getName()))
					.replace("%gang_owner%", g.getOwnerOffline().getName())
					.replace("%gang_members%", StringUtils.join(g.getMembersOffline().stream().map(OfflinePlayer::getName).toArray(), ", ")));
		}
		return returnList;
	}

	public boolean sendGangInfo(Player p, OfflinePlayer target) {
		Optional<Gang> targetGang = this.getPlayerGang(target);

		if (!targetGang.isPresent()) {
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-player-not-in-gang"));
			return true;
		}

		for (String s : this.getGangInfoFormat(targetGang.get())) {
			PlayerUtils.sendMessage(p, s);
		}
		return true;
	}

	public boolean sendGangInfo(Player p, String gangName) {
		Optional<Gang> targetGang = this.getGangWithName(gangName);

		if (!targetGang.isPresent()) {
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-not-exists"));
			return true;
		}

		for (String s : this.getGangInfoFormat(targetGang.get())) {
			PlayerUtils.sendMessage(p, s);
		}
		return true;
	}

	public boolean hasGangChatEnabled(Player p) {
		return this.gangChatEnabledPlayers.contains(p.getUniqueId());
	}

	public void disbandGang(Player player, Gang gang, boolean force) {

		if (!gang.isOwner(player) && !force) {
			PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("gang-not-owner"));
			return;
		}

		GangDisbandEvent gangDisbandEvent = new GangDisbandEvent(gang);

		this.plugin.getCore().debug("Calling GangDisbandEvent for gang " + gang.getName() + ".", this.plugin);

		Events.call(gangDisbandEvent);

		if (gangDisbandEvent.isCancelled()) {
			this.plugin.getCore().debug("GangDisbandEvent for gang " + gang.getName() + " was cancelled.", this.plugin);
			return;
		}

		gang.disband();

		this.gangs.remove(gang.getUuid());
		this.plugin.getGangsService().deleteGang(gang);

		Players.all().forEach(player1 -> PlayerUtils.sendMessage(player1, this.plugin.getConfig().getMessage("gang-disband-broadcast").replace("%gang%", gang.getName()).replace("%player%", player.getName())));
	}

	public boolean acceptInvite(Player player, Gang gang) {

		if (!gang.hasPendingInvite(player)) {
			PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("gang-no-invite-pending"));
			return false;
		}

		return joinGang(player, gang);
	}

	public void sendHelpMenu(CommandSender sender) {
		List<String> gangHelpMenu = this.plugin.getConfig().getGangHelpMenu();
		gangHelpMenu.forEach(s -> PlayerUtils.sendMessage(sender, s));
	}

	public void sendAdminHelpMenu(CommandSender sender) {
		List<String> gangAdminHelpMenu = this.plugin.getConfig().getGangHelpMenu();
		gangAdminHelpMenu.forEach(s -> PlayerUtils.sendMessage(sender, s));
	}

	public int getGangTopPosition(Gang gang) {
		if (!this.topGangs.contains(gang)) {
			return -1;
		}
		return this.topGangs.indexOf(gang) + 1;
	}

	public boolean removeFromGang(Player p, Gang gang, OfflinePlayer target) {

		if (!gang.isOwner(p)) {
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-not-owner"));
			return false;
		}

		if (target == null) {
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("player-not-online"));
			return false;
		}

		this.kickPlayerFromGang(gang, target);
		return true;
	}

	public void kickPlayerFromGang(Gang gang, OfflinePlayer target) {
		if (gang.kickPlayer(target)) {
			gang.getOnlinePlayers().forEach(player -> PlayerUtils.sendMessage(player, this.plugin.getConfig().getMessage("gang-player-kicked").replace("%player%", target.getName())));
			if (target.isOnline()) {
				PlayerUtils.sendMessage(target.getPlayer(), this.plugin.getConfig().getMessage("gang-kicked").replace("%gang%", gang.getName()));
			}
		}
	}

	public boolean sendGangTop(CommandSender sender) {
		List<String> gangTopFormat = this.plugin.getConfig().getGangTopFormat();
		for (String s : gangTopFormat) {
			if (s.startsWith("{FOR_EACH_GANG}")) {
				String rawContent = s.replace("{FOR_EACH_GANG} ", "");
				for (int i = 0; i < 10; i++) {
					try {
						Gang gang = this.topGangs.get(i);
						PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%gang%", gang.getName()).replace("%value%", String.format("%,d", gang.getValue())));
					} catch (Exception e) {
						break;
					}
				}
			} else {
				PlayerUtils.sendMessage(sender, s);
			}
		}
		return true;
	}

	public boolean forceAdd(CommandSender sender, Player target, Gang gang) {

		if (target == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("player-not-online"));
			return false;
		}

		Optional<Gang> currentGang = this.getPlayerGang(target);

		if (currentGang.isPresent()) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-cant-invite"));
			return false;
		}

		return joinGang(target, gang);
	}

	public boolean forceRemove(CommandSender sender, Player target) {

		if (target == null) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("player-not-online"));
			return false;
		}

		Optional<Gang> currentGang = this.getPlayerGang(target);

		if (!currentGang.isPresent()) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-player-not-in-gang"));
			return false;
		}

		return leaveGang(target, GangLeaveReason.ADMIN);
	}

	public boolean forceDisband(CommandSender sender, Gang gang) {

		if (sender instanceof Player) {
			new DisbandGangAdminGUI(this.plugin, (Player) sender, gang).open();
		} else {
			PlayerUtils.sendMessage(sender, "§cOnly for players.");
		}
		return true;
	}

	public boolean forceRename(CommandSender sender, String oldName, String newName) {

		Optional<Gang> targetGang = this.getGangWithName(oldName);

		if (!targetGang.isPresent()) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-not-exists"));
			return true;
		}

		Gang gang = targetGang.get();

		gang.setName(newName);
		PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-force-rename").replace("%old_gang%", oldName).replace("%gang%", gang.getName()));
		return true;
	}

	public boolean toggleGangChat(Player p) {

		if (!getPlayerGang(p).isPresent()) {
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("not-in-gang"));
			return false;
		}

		if (this.gangChatEnabledPlayers.contains(p.getUniqueId())) {
			this.gangChatEnabledPlayers.remove(p.getUniqueId());
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-chat-off"));
		} else {
			this.gangChatEnabledPlayers.add(p.getUniqueId());
			PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-chat-on"));
		}
		return true;
	}

	public boolean modifyValue(CommandSender sender, Gang gang, long amount, String operation) {

		if (amount <= 0) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("invalid-value"));
			return false;
		}

		if (operation.equalsIgnoreCase("add")) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-value-add").replace("%value%", String.valueOf(amount)).replace("%gang%", gang.getName()));
			gang.setValue(gang.getValue() + amount);
			return true;
		} else if (operation.equalsIgnoreCase("remove")) {
			PlayerUtils.sendMessage(sender, this.plugin.getConfig().getMessage("gang-value-remove").replace("%value%", String.valueOf(amount)).replace("%gang%", gang.getName()));
			gang.setValue(gang.getValue() - amount);
			return true;
		} else {
			PlayerUtils.sendMessage(sender, "§cInvalid operation given.");
			return false;
		}
	}

	public Collection<Gang> getAllGangs() {
		return this.gangs.values();
	}

	public void disable() {
		this.saveDataOnDisable();
	}

	public void disableGangChat(Player player) {
		this.gangChatEnabledPlayers.remove(player.getUniqueId());
	}

	public void updateGangTop(GangTopProvider provider) {
		this.topGangs = provider.provide();
	}
}
