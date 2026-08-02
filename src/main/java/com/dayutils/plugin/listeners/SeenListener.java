package com.dayutils.plugin.listeners;

import com.dayutils.plugin.DayUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SeenListener implements Listener {

    private final DayUtils plugin;

    public SeenListener(DayUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getSeenManager().markQuit(event.getPlayer().getUniqueId());
    }
}
