package dev.drawethree.xprison.tokens.listener;

import dev.drawethree.xprison.tokens.XPrisonTokens;
import me.lucko.helper.Events;
import me.lucko.helper.reflect.MinecraftVersion;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokensListener {

	private final XPrisonTokens plugin;

	public TokensListener(XPrisonTokens plugin) {

		this.plugin = plugin;
	}

	public void subscribeToEvents() {
		this.subscribeToPlayerJoinEvent();
		this.subscribeToPlayerQuitEvent();
		this.subscribeToPlayerInteractEvent();
		this.subscribeToBlockBreakEvent();
	}

	private void subscribeToBlockBreakEvent() {
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(e -> !e.isCancelled())
				.filter(e -> e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.filter(e -> {
					final List<String> whitelist = this.plugin.getTokensConfig().getWorldWhitelist();
					return whitelist.isEmpty() || whitelist.contains(e.getBlock().getWorld().getName());
				})
				.handler(e -> {
					List<Block> blocks = new ArrayList<>();
					blocks.add(e.getBlock());
					this.plugin.getTokensManager().handleBlockBreak(e.getPlayer(), blocks, true);
				}).bindWith(plugin.getCore());
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
					this.plugin.getTokensManager().savePlayerData(Collections.singletonList(e.getPlayer()), true, true);
					e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
				}).bindWith(plugin.getCore());
	}

	private void subscribeToPlayerJoinEvent() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.plugin.getTokensManager().loadPlayerData(Collections.singleton(e.getPlayer()));

					if (this.plugin.getTokensConfig().isDisplayTokenMessages() && this.plugin.getTokensManager().hasOffTokenMessages(e.getPlayer())) {
						this.plugin.getTokensManager().addPlayerIntoTokenMessageOnPlayers(e.getPlayer());
					}

				}).bindWith(plugin.getCore());
	}
}
