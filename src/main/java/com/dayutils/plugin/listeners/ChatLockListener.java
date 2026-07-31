package com.dayutils.plugin.listeners;

import com.dayutils.plugin.DayUtils;
import com.dayutils.plugin.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatLockListener implements Listener {

    private final DayUtils plugin;

    public ChatLockListener(DayUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getChatLockManager().isLocked()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("dayutils.bypass.lockchat") || player.isOp()) {
            return;
        }

        event.setCancelled(true);
        String message = ColorUtils.translate(
                plugin.getConfig().getString("lockchat.blocked-message", "&#FF5555¡El chat está bloqueado por el staff!")
        );
        player.sendMessage(message);
    }
}
