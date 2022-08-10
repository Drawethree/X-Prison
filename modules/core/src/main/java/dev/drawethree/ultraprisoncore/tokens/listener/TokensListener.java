package dev.drawethree.ultraprisoncore.tokens.listener;

import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.reflect.MinecraftVersion;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.Arrays;

public class TokensListener {

	private final UltraPrisonTokens plugin;

	public TokensListener(UltraPrisonTokens plugin) {

		this.plugin = plugin;
	}

	public void subscribeToEvents() {
		this.subscribeToPlayerJoinEvent();
		this.subscribeToPlayerQuitEvent();
		this.subscribeToPlayerInteractEvent();
		this.subscribeToBlockBreakEvent();
	}

	private void subscribeToBlockBreakEvent() {
		Events.subscribe(BlockBreakEvent.class)
				.filter(EventFilters.ignoreCancelled())
				.filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch(region -> region.getId().toLowerCase().startsWith("mine")))
				.filter(e -> e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.handler(e -> this.plugin.getTokensManager().handleBlockBreak(e.getPlayer(), Arrays.asList(e.getBlock()), true)).bindWith(plugin.getCore());
	}

	private void subscribeToPlayerInteractEvent() {

		Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
				.filter(e -> e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getType() == this.plugin.getTokensConfig().getTokenItemMaterial() && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
				.handler(e -> {

					e.setCancelled(true);
					e.setUseInteractedBlock(Event.Result.DENY);

					boolean offHandClick = false;

					if (MinecraftVersion.getRuntimeVersion().isAfter(MinecraftVersion.of(1, 8, 9))) {
						offHandClick = e.getHand() == EquipmentSlot.OFF_HAND;
					}

					this.plugin.getTokensManager().redeemTokens(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking(), offHandClick);

				}).bindWith(plugin.getCore());
	}

	private void subscribeToPlayerQuitEvent() {
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.plugin.getTokensManager().savePlayerData(e.getPlayer(), true, true);
					e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
				}).bindWith(plugin.getCore());
	}

	private void subscribeToPlayerJoinEvent() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {

					this.plugin.getTokensManager().addIntoTable(e.getPlayer());
					this.plugin.getTokensManager().loadPlayerData(e.getPlayer());

					if (this.plugin.getTokensConfig().isDisplayTokenMessages() && this.plugin.getTokensManager().hasOffTokenMessages(e.getPlayer())) {
						this.plugin.getTokensManager().addPlayerIntoTokenMessageOnPlayers(e.getPlayer());
					}

				}).bindWith(plugin.getCore());
	}
}
