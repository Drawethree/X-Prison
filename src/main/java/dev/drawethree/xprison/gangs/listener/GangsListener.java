package dev.drawethree.xprison.gangs.listener;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class GangsListener {

	private final XPrisonGangs plugin;

	public GangsListener(XPrisonGangs plugin) {
		this.plugin = plugin;
	}

	public void register() {
		this.subscribeToEntityDamageByEntityEvent();
		this.subscribeToAsyncPlayerChatEvent();
	}

	private void subscribeToEntityDamageByEntityEvent() {
		Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGHEST)
				.filter(e -> e.getDamager() instanceof Player && (e.getEntity() instanceof Player || e.getEntity() instanceof Projectile))
				.handler(e -> {

					if (this.plugin.getConfig().isGangFriendlyFire()) {
						return;
					}

					Player player = (Player) e.getEntity();
					Player damager = null;
					if (e.getDamager() instanceof Player) {
						damager = (Player) e.getDamager();
					} else if (e.getDamager() instanceof Projectile) {
						Projectile projectile = (Projectile) e.getDamager();
						if (projectile.getShooter() instanceof Player) {
							damager = (Player) projectile.getShooter();
						}
					}

					if (damager == null) {
						return;
					}

					if (this.plugin.getGangsManager().arePlayersInSameGang(player, damager)) {
						e.setCancelled(true);
					}

				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToAsyncPlayerChatEvent() {
		Events.subscribe(AsyncPlayerChatEvent.class, this.plugin.getConfig().getGangChatPriority())
				.filter(e -> this.plugin.getGangsManager().hasGangChatEnabled(e.getPlayer()))
				.handler(e -> {

					Optional<Gang> gangOptional = this.plugin.getGangsManager().getPlayerGang(e.getPlayer());

					if (!gangOptional.isPresent()) {
						this.plugin.getGangsManager().disableGangChat(e.getPlayer());
						return;
					}

					e.setCancelled(true);
					e.getRecipients().clear();

					Gang gang = gangOptional.get();

					for (Player p : gang.getOnlinePlayers()) {
						PlayerUtils.sendMessage(p, this.plugin.getConfig().getMessage("gang-chat-format").replace("%player%", e.getPlayer().getName()).replace("%message%", e.getMessage()).replace("%gang%", gang.getName()));
					}
				}).bindWith(this.plugin.getCore());
	}

}
