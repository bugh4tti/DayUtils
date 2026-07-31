package com.dayutils.plugin.listeners;

import com.dayutils.plugin.DayUtils;
import com.dayutils.plugin.utils.NightVisionApplier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class NightVisionListener implements Listener {

    private final DayUtils plugin;

    public NightVisionListener(DayUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getModifiedType() != PotionEffectType.NIGHT_VISION) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.getNightVisionManager().isEnabled(player.getUniqueId())) {
            return;
        }

        if (event.getCause() == Cause.PLUGIN && event.getNewEffect() != null) {
            return;
        }

        event.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    NightVisionApplier.apply(player);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        reapplyIfNeeded(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        reapplyIfNeeded(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        reapplyIfNeeded(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        reapplyIfNeeded(event.getPlayer());
    }

    private void reapplyIfNeeded(Player player) {
        if (plugin.getNightVisionManager().isEnabled(player.getUniqueId())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        NightVisionApplier.apply(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }
            }
